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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;
import io.realm.Sort;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Sleep;
import me.adamoflynn.dynalarm.receivers.AlarmReceiver;

/**
 *  Very similar to traffic service, look up that class for code comments.
 *
 */

public class WakeUpService extends IntentService {

	private final DateFormat hh = new SimpleDateFormat("HH:mm");
	private List<AccelerometerData> accelerometerData;

	public WakeUpService(String name) {
		super(name);
	}

	public WakeUpService(){
		super("WakeUpService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d("Wakeup Service", " Created");
	}


	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		String id = intent.getStringExtra("id");

		// Future uses
		int routineTime = intent.getIntExtra("routines", 0);
		Calendar wake_time = (Calendar) intent.getSerializableExtra("wake_time");

		int sleepId = Integer.valueOf(id);
		Realm realm = Realm.getDefaultInstance();

		RealmResults<AccelerometerData> sleep = realm.where(AccelerometerData.class).equalTo("sleepId", sleepId).findAll();
		sleep.sort("timestamp", Sort.DESCENDING);
		// DEBUGGING PURPOSES
		if(sleep.size() == 0){
			return;
		} else{
			accelerometerData = sleep;
			wakeUpCheck(sleepId);
		}

		Log.d("Traffic service ", "trying to stop...");
		realm.close();
		WakefulBroadcastReceiver.completeWakefulIntent(intent);
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
		int max = 0, maxIndex = 0;

		try {
			for (int i = 10, j = 0; i > 0; i--, j++){
				newestData.add(j, accelerometerData.get(i));
				if(max > newestData.get(j).getAmtMotion() && newestData.get(maxIndex).getMaxAccel() > 0.1){
					Log.d("ALARM", "Update");
					updateAlarm();
				} else if(max < newestData.get(j).getAmtMotion()){
					max = newestData.get(j).getAmtMotion();
					maxIndex = j;
					Log.d("UPDATE MAX", Integer.toString(max));
				}
			}

			RealmList<AccelerometerData> acc = new RealmList<>();
			for (AccelerometerData a: newestData){
				acc.add(a);
			}

			realm.beginTransaction();
			Sleep sleep = realm.where(Sleep.class).equalTo("id", sleepId).findFirst();
			sleep.setSleepData(acc);
			realm.commitTransaction();
		} catch (IndexOutOfBoundsException ioe) {
			Log.e("ERROR", "Index out of bounds error");
		}
	}

	// This method updates the alarms by creating the exact same intent that was used to schedule the alarm
	// orginally. This will trigger immediately and wake the user by starting the AlarmSound service
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void updateAlarm(){
		Intent intent = new Intent(this, AlarmReceiver.class);
		intent.putExtra("MESSAGE", "You seem to be moving, so we decided to wake you up a bit earlier");
		PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis(), pendingIntent);
	}
}
