package me.adamoflynn.dynalarm.services;
import android.app.Application;
import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.widget.Toast;
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
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Sleep;
import me.adamoflynn.dynalarm.model.TrafficInfo;

public class TrafficService extends IntentService {

	private TrafficInfo trafficInfo;
	private final String BASE_URL = "https://api.tomtom.com/routing/1/calculateRoute/";
	private final String API_KEY = "nmqjmepdy9ppbp8yekvrsaet";
	private final String END_URL = "?key="+ API_KEY + "&routeType=fastest&traffic=true&computeTravelTimeFor=all&arriveAt=";

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private final DateFormat hh = new SimpleDateFormat("HH:mm");


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

	/*@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		super.onStartCommand(intent, flags, startId);
		Log.d("Traffic Service", " Started");
		return START_STICKY;
	}*/

	@Override
	public void onDestroy() {
		super.onDestroy();
		String output = "It will take you " + trafficInfo.getTravelTime()/60 + " minutes if you leave at " + hh.format(trafficInfo.getDepartureTime());
		//Toast.makeText(this, output , Toast.LENGTH_LONG).show();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String from = intent.getStringExtra("from");
		String to = intent.getStringExtra("to");
		String time = intent.getStringExtra("time");
		String id = intent.getStringExtra("id");
		Log.d("Accelerometer Sleep ID", id);

		Realm realm = Realm.getDefaultInstance();

		int sleepId = Integer.valueOf(id);
		try {
			URL url = new URL(BASE_URL + from + ":" + to + "/json" + END_URL + time);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			int statusCode = urlConnection.getResponseCode();

			if(statusCode == 200){
				BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
				StringBuilder stringBuilder = new StringBuilder();
				String line;

				while ((line = bufferedReader.readLine()) != null) {
					stringBuilder.append(line).append("\n");
				}

				bufferedReader.close();
				urlConnection.disconnect();
				parseJSON(stringBuilder.toString(), realm, sleepId);
				Log.d("Traffic service ", "trying to stop...");
				realm.close();
				WakefulBroadcastReceiver.completeWakefulIntent(intent);
			} else throw new Exception("Download Error");

		} catch (MalformedURLException e){
			Log.e("MalformedURLException", e.getMessage());
		} catch (IOException e){
			Log.e("IOException", e.getMessage());
		} catch (Exception e){
			Log.e("Download Error", e.getMessage());
		}
	}


	public void parseJSON(String response, Realm realm, int sleepId) {

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

			//TrafficInfo trafficInfo = new TrafficInfo(lengthInMeters, travelTimeInSeconds, travelDelayInSeconds, dep, arr);
			trafficInfo = new TrafficInfo(lengthInMeters, travelTimeInSeconds, travelDelayInSeconds, dep, arr, travelTimeNoTraffic, historicTravelTime, liveIncidents);
			Log.d("Data", trafficInfo.toString());
			getAccelerometerReadings(realm, sleepId);
		}catch (JSONException e) {
			Log.e("JSON parse exception", e.getMessage());
		}

	}

	public Date removeT(String time){
		time = time.replace('T',' ');
		Date d = null;
		try{
			d = df.parse(time);
		}catch (ParseException e){
			Log.e("Parse Error on time", e.getMessage());
		}
		return d;
	}

	public void getAccelerometerReadings(Realm realm, int sleepId){
		RealmResults<AccelerometerData> sleep = realm.where(AccelerometerData.class).equalTo("sleepId", sleepId).findAll();
		if(sleep.size() == 0){
			Log.d("No sleep", "nya");
			return;
		} else{
			for (AccelerometerData a: sleep){
				Log.d("Acc Data - TM", String.valueOf(a.getTimestamp()));
				Log.d("Acc Data - MNT", String.valueOf(a.getAmtMotion()));
			}
		}

	}

}