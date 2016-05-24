package me.adamoflynn.dynalarm.services;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Sleep;
import me.adamoflynn.dynalarm.model.TrafficInfo;
import me.adamoflynn.dynalarm.receivers.AlarmReceiver;

public class TrafficService extends IntentService {


	// constants to hold API url
	private final String BASE_URL = "https://api.tomtom.com/routing/1/calculateRoute/";
	private final String API_KEY = "nmqjmepdy9ppbp8yekvrsaet";
	private final String END_URL = "?key="+ API_KEY + "&routeType=fastest&traffic=true&computeTravelTimeFor=all&arriveAt=";

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private List<AccelerometerData> accelerometerData;
	private TrafficInfo trafficInfo;

	public TrafficService(String name) {
		super(name);
	}

	public TrafficService(){
		super("TrafficService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Traffic Service", " Created");
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {

		// Get data from wake up receiver
		String from = intent.getStringExtra("from");
		String to = intent.getStringExtra("to");
		String time = intent.getStringExtra("time");
		String id = intent.getStringExtra("id");
		Calendar wake_time = (Calendar) intent.getSerializableExtra("wake_time");
		int routineTime = intent.getIntExtra("routines", 0);
		Log.d("Accelerometer Sleep ID", id);

		Realm realm = Realm.getDefaultInstance();
		int sleepId = Integer.valueOf(id);


		// Try connection to the TomTom API using the specified calls
		try {
			URL url = new URL(BASE_URL + from + ":" + to + "/json" + END_URL + time);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			int statusCode = urlConnection.getResponseCode();

			// If we get a HTTP OK response, we can successfully read the JSON and traffic data
			if(statusCode == 200){
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				StringBuilder stringBuilder = new StringBuilder();
				String line;

				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line).append("\n");
				}

				bufferedReader.close();
				urlConnection.disconnect();

				// Pass data to analysis method
				analyseData(stringBuilder.toString(), realm, sleepId, routineTime, wake_time);
				Log.d("Traffic service ", "trying to stop...");

				// Close DB and complete the wakeful service
				realm.close();
				WakefulBroadcastReceiver.completeWakefulIntent(intent);
			}

			// If we any error codes, we should do the accelerometer reading only
			else {
				getAccelerometerReadings(realm, sleepId);
			}
		}

		// If the service has any issues, default back to the accelerometer analysis
		  catch (MalformedURLException e){
			Log.e("MalformedURLException", e.getMessage());
			getAccelerometerReadings(realm, sleepId);
		} catch (IOException e){
			Log.e("IOException", e.getMessage());
			getAccelerometerReadings(realm, sleepId);
		} catch (Exception e){
			Log.e("Download Error", e.getMessage());
			getAccelerometerReadings(realm, sleepId);
		}
	}


	private void analyseData(String response, Realm realm, int sleepId, int routineTime, Calendar wake_time) {

		// Parse the JSON data from the HTTP response
		try{

			JSONObject jsonObject = new JSONObject(response);
			JSONArray allRoutes = jsonObject.getJSONArray("routes");

			// Only need one route
			JSONObject route = allRoutes.getJSONObject(0);
			JSONObject summary = route.getJSONObject("summary");

			int lengthInMeters = summary.getInt("lengthInMeters");
			int travelTimeInSeconds = summary.getInt("travelTimeInSeconds");
			int travelDelayInSeconds = summary.getInt("trafficDelayInSeconds");
			Date dep = removeT(summary.getString("departureTime"));
			Date arr = removeT(summary.getString("arrivalTime"));
			int travelTimeNoTraffic = summary.getInt("noTrafficTravelTimeInSeconds");
			int historicTravelTime = summary.getInt("historicTrafficTravelTimeInSeconds");
			int liveIncidents = summary.getInt("liveTrafficIncidentsTravelTimeInSeconds");

			trafficInfo = new TrafficInfo(lengthInMeters, travelTimeInSeconds, travelDelayInSeconds, dep, arr, travelTimeNoTraffic, historicTravelTime, liveIncidents);
			Log.d("Data", trafficInfo.toString());

			// Check if there is an issue with traffic, if there is, immediately update alarms and wake up user
			if(trafficCheck(routineTime, wake_time)){
				updateAlarm();
			}
			// No traffic delay? Check accelerometer data
			else getAccelerometerReadings(realm, sleepId);
		}catch (JSONException e) {
			Log.e("JSON parse exception", e.getMessage());
		}
	}

	// Used to remove the T from the JSON responses
	private Date removeT(String time){
		time = time.replace('T',' ');
		Date d = null;
		try{
			d = df.parse(time);
		}catch (ParseException e){
			Log.e("Parse Error on time", e.getMessage());
		}
		return d;
	}

  // Simple Algorithm to check if the users calculated depature time (Wake time + routine time)
  // is later than the the API recommended departure time
	// If it is, wake up user immediately by updating alarms
	private boolean trafficCheck(int routineTime, Calendar wake_time) {
		Date departureTime = trafficInfo.getDepartureTime();
		Log.d("DEP TIME", departureTime.toString());


		wake_time.add(Calendar.MINUTE, routineTime);
		Date calculatedTime = wake_time.getTime();
		Log.d("CALC TIME", calculatedTime.toString());

		if(calculatedTime.after(departureTime)){
			Log.d("ALARM", " WAKE UP, YOU ARE GOING TO BE LATE");
			return true;
		}

		return false;
	}

	private void getAccelerometerReadings(Realm realm, int sleepId){

		RealmResults<AccelerometerData> sleep = realm.where(AccelerometerData.class).equalTo("sleepId", sleepId).findAll();

		// Get newest data first
		sleep.sort("timestamp", Sort.DESCENDING);

		// Check is used for debugging purposes...
		if(sleep.size() == 0){
			Log.d("No sleep", " don't do anything.");
		} else{
			accelerometerData = sleep;
			wakeUpCheck(sleepId);
		}
	}

  /**
   * This algorithm is desribed in the documentation
   * Go through the last ten minutes of data
   * If the value you’re at is greater than the max, assign max to it.
   * If at some stage the max value is actually greater than the next value, I check to see how big of a movement the max accelerometer data value was
   * If it is a bgi enough movement to signify waking up, I update the alarms and wake the user
	*/
	private void wakeUpCheck(int sleepId) {
		Realm realm = Realm.getDefaultInstance();
		Log.d("WAKE", "CHECK");
		ArrayList<AccelerometerData> newestData = new ArrayList<>();
		int max = 0;

		try {
			// Get last ten minutes of sleep data
			for (int i = 10, j = 0; i > 0; i--, j++){
				newestData.add(j, accelerometerData.get(i));
				if(max > newestData.get(j).getAmtMotion() && newestData.get(j).getMaxAccel() > 0.1){
					Log.d("ALARM", "Update");
					updateAlarm();
				} else if(max < newestData.get(j).getAmtMotion()){
					max = newestData.get(j).getAmtMotion();
					Log.d("UPDATE MAX", Integer.toString(max));
				}
			}
		} catch (IndexOutOfBoundsException ioe) {
			Log.e("ERROR", "Index out of bounds error");
		}

		// Testing & Debugging Purposes
		Log.d("MAX", Integer.toString(max));
		RealmList<AccelerometerData> acc = new RealmList<>();
		for (AccelerometerData a: newestData){
			acc.add(a);
			Log.d("Acc Data - TM", String.valueOf(a.getTimestamp()));
			Log.d("Acc Data - MNT", String.valueOf(a.getAmtMotion()));
			Log.d("Acc Data - MAX", String.valueOf(a.getMaxAccel()));
			Log.d("Acc Data - AVG", String.valueOf(a.getMinAccel()));
		}

		// Set the last ten minutes of sleep data in sleep table
		realm.beginTransaction();
		Sleep sleep = realm.where(Sleep.class).equalTo("id", sleepId).findFirst();
		sleep.setSleepData(acc);
		realm.commitTransaction();
	}


	// This method updates the alarms by creating the exact same intent that was used to schedule the alarm
	// orginally. This will trigger immediately and wake the user by starting the AlarmSound service
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void updateAlarm(){
		Intent intent = new Intent(this, AlarmReceiver.class);
		intent.putExtra("MESSAGE", "It looks like you might be late due to delays. Better wake up!");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), pendingIntent);
	}
}