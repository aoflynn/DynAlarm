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


	// Some global variable decalrations so other methods can access sensors and database connections
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;


	// Variables that track the last commit time of the data
	private long lastUpdate, lastUpdateDBCommit = 0;

	// This is the amount of body movements that the accelerometer recognises per minute
	private int motions = 0;

	private float accelCurrent;
	private int sleepId;
	private Boolean first = true;
	private float maxVar = 0f;
	private float sumVar = 0f;
	private float avgVar = 0f;
	private final int SERVICE_ID = 99;

	private Realm db;

	public AccelerometerService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		throw new UnsupportedOperationException("Not yet implemented");
	}


	// Get database connection and the accelerometer service required to read the data
	@Override
	public void onCreate() {
		db = Realm.getDefaultInstance();
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

		// Get start times
		lastUpdate = System.currentTimeMillis();
		lastUpdateDBCommit = System.currentTimeMillis();

		Log.d("Service", " Created");
	}


	// After onCreate() -> this method is called
	// We first register our listener which will read our data
	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

		// Get the sleep ID from the starting intent -> when we set the alarm in AlarmFragment
		sleepId = intent.getIntExtra("sleepId", 0);
		Log.d("Starting Accel", "with ID " + Integer.toString(sleepId));

		// Commit a new sleep entry
		Sleep sleep = new Sleep();
		sleep.setId(sleepId);
		sleep.setStartTime(Calendar.getInstance().getTimeInMillis());
		sleep.setDate(Calendar.getInstance().getTime());

		db.beginTransaction();
		db.copyToRealm(sleep);
		db.commitTransaction();

		Log.d("Service", " Started");

		// Provide feedback to user as to show service is running -> required as it is a foreground service
		createPersistentNotification();

		// This means that if the service is killed, it will restart itself and start running again
		// if at all possible
		return START_STICKY;
	}


	// When we stop a service, we close all our db connections and unregister our listener to
	// prevent battery drain.
	@Override
	public void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(this);

		// Write any last data to the database
		writeToDB(Calendar.getInstance().getTimeInMillis(), motions, maxVar, avgVar);

		// "Finish" off sleep by committing our end time
		Log.d("Sleep Id mate", String.valueOf(sleepId));
		Sleep sleep = db.where(Sleep.class).equalTo("id", sleepId).findFirst();
		Log.d("Sleep data", sleep.toString());
		db.beginTransaction();
		sleep.setEndTime(Calendar.getInstance().getTimeInMillis());


		// If the sleep is less than 20 minutes, we disregard it and delete it from the database
		if(sleep.getEndTime() - sleep.getStartTime() < 1200000){
			Log.d("Sleep", "too short, deleting...");
			RealmResults<AccelerometerData> accData = db.where(AccelerometerData.class).equalTo("sleepId", sleepId).findAll();

			// Issues with deleting from RealmResuls so I needed to assign them to a list instead
			List<AccelerometerData> accDataDeleteable = accData;
			for (int i = 0; i < accData.size(); i++){

				// Remove all accelerometer data
				accDataDeleteable.get(i).removeFromRealm();
			}

			// Remove the sleep
			sleep.removeFromRealm();
			sleepId--;
		}

		db.commitTransaction();

		// Increment for next trial/sleep
		sleepId++;


		// Stop running the service and remove the notification
		stopForeground(true);

		// Close out DB connections
		if (db != null) {
			db.close();
			db = null;
		}
	}


	// Unused method
	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {

	}

	// This method is where our algorithm to measure body motion is situated.

	// This method is called every time a value changes

	@Override
	public void onSensorChanged(SensorEvent event) {

		// We get our X, Y, Z values
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		long curTime = System.currentTimeMillis();

		// Simple check so accelerometer doesn't commit with no data immediately
		if ((curTime - lastUpdate) > 50) {

			//Get history to check for variance
			float accelLast = accelCurrent;

			// Calculate the acceleration in all planes - algorithm described in documentation
			accelCurrent = (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
			float variance = accelCurrent - accelLast;
			float abs_var = Math.abs(variance);

			if(abs_var > 0.025){
				motions++;
				sumVar += abs_var;
				Log.d("Motion: ", Float.toString(abs_var));
				Log.d("Sleep ID motion", String.valueOf(sleepId));
			}

			// If greater than current max, assign it to max
			if(abs_var > maxVar){
				maxVar = abs_var;
			}

			// Commit data every 1 minute
			if((curTime - lastUpdateDBCommit) >= 60000  && !first) {
				lastUpdateDBCommit = curTime;
				avgVar = sumVar / motions;
				writeToDB(Calendar.getInstance().getTimeInMillis(), motions, maxVar, avgVar);
				Log.d("Motion: ", Integer.toString(motions));

				// Reset values for next minute of analysis
				motions = 0;
				maxVar = 0;
				sumVar = 0;
				avgVar = 0;
			}
			first = false;
		}
	}

	// Method to write data to database as required
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

	// This method creates our sticky notification and starts our service
	@TargetApi(Build.VERSION_CODES.LOLLIPOP)
	private void createPersistentNotification(){
		// Create an intent that will go to main if clicked in notificaition bar, flags remove all
		// other activites below it in the stack so no issues with back presses
		Intent intent = new Intent(this, MainActivity.class);
		intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
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
