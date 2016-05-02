package me.adamoflynn.dynalarm.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.MainActivity;
import me.adamoflynn.dynalarm.R;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Sleep;

public class AccelerometerService extends Service implements SensorEventListener{

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	private long lastUpdate, lastUpdate5secs = 0;
	private int motions = 0;

	private float accelCurrent;
	private int sleepId;
	private Boolean first = true;
	private float maxVar = 0f;
	private float sumVar = 0f;
	private float avgVar = 0f;
	private final int SERVICE_ID = 99;

	private NotificationManager notificationManager = null;

	private Realm db;

	public AccelerometerService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}

	@Override
	public void onCreate() {
		db = Realm.getDefaultInstance();
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

		// Get most recent sleep ID + inc to get new unique iD.
		// sleepId = Application.sleepIDValue.incrementAndGet();

		//Get times
		lastUpdate = System.currentTimeMillis();
		lastUpdate5secs = System.currentTimeMillis();

		Log.d("Service", " Created");

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		sleepId = intent.getIntExtra("sleepId", 0);
		Log.d("Starting Accel", "with ID " + Integer.toString(sleepId));
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		Sleep sleep = new Sleep();
		sleep.setId(sleepId);
		sleep.setStartTime(Calendar.getInstance().getTimeInMillis());
		sleep.setDate(Calendar.getInstance().getTime());

		db.beginTransaction();
		db.copyToRealm(sleep);
		db.commitTransaction();

		Log.d("Service", " Started");
		createPersistentNotification();
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(this);
		writeToDB(Calendar.getInstance().getTimeInMillis(), motions, maxVar, avgVar);

		Log.d("Sleep Id mate", String.valueOf(sleepId));
		Sleep sleep = db.where(Sleep.class).equalTo("id", sleepId).findFirst();
		Log.d("Sleep data", sleep.toString());
		db.beginTransaction();
		sleep.setEndTime(Calendar.getInstance().getTimeInMillis());

		if(sleep.getEndTime() - sleep.getStartTime() < 1200000){
			Log.d("Sleep", "too short, deleting...");
			RealmResults<AccelerometerData> accData = db.where(AccelerometerData.class).equalTo("sleepId", sleepId).findAll();
			List<AccelerometerData> accDataDeleteable = accData;
			for (int i = 0; i < accData.size(); i++){
				accDataDeleteable.get(0).removeFromRealm();
			}
			sleep.removeFromRealm();
			sleepId--;
		}

		db.commitTransaction();

		// Increment for next trial/sleep
		sleepId++;

		stopForeground(true);

		if (db != null) {
			db.close();
			db = null;
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];
		long curTime = System.currentTimeMillis();

		// Was 100...
		if ((curTime - lastUpdate) > 50) {
			//lastUpdate = curTime;

			//Get history to check for variance
			float accelLast = accelCurrent;

			accelCurrent = (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
			float variance = accelCurrent - accelLast;
			float abs_var = Math.abs(variance);

			// .04 -> 0.025
			if(abs_var > 0.025){
				motions++;
				sumVar += abs_var;
				Log.d("Motion: ", Float.toString(abs_var));
				Log.d("Sleep ID motion", String.valueOf(sleepId));
			}

			//Commit every 5 minute
			// (curTime - lastUpdate5secs) >= 300000
			if((curTime - lastUpdate5secs) >= 60000  && !first) {
				lastUpdate5secs = curTime;
				avgVar = sumVar / motions;
				writeToDB(Calendar.getInstance().getTimeInMillis(), motions, maxVar, avgVar);
				Log.d("Motion: ", Integer.toString(motions));
				motions = 0;
				maxVar = 0;
				sumVar = 0;
				avgVar = 0;
			}

			if(abs_var > maxVar){
				maxVar = abs_var;
			}

			first = false;
		}
	}

	private void writeToDB(long timestamp, int amtMotion, float maxVar, float avgVar){
		db.beginTransaction();

		AccelerometerData acc = db.createObject(AccelerometerData.class);
		acc.setTimestamp(timestamp);
		acc.setSleepId(sleepId);
		acc.setAmtMotion(amtMotion);
		acc.setMaxAccel(maxVar);
		acc.setMinAccel(avgVar);

		db.commitTransaction();
	}

	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void createPersistentNotification(){
		Intent intent = new Intent(this, MainActivity.class);
		intent.putExtra("isAccelerometer", true);
		//intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentIntent = PendingIntent.getActivity(this, 113, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification not = new Notification.Builder(this)
				.setContentTitle("Alarm is running!")
				.setContentText("Click here to go to DynAlarm.")
				.setContentIntent(contentIntent)
				.setColor(Color.LTGRAY)
				.setSmallIcon(R.drawable.ic_alarm_white_48dp)
				.setOngoing(true)
				.build();

		startForeground(SERVICE_ID, not);
	}
}
