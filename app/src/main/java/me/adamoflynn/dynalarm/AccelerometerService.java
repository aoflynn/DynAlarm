package me.adamoflynn.dynalarm;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import io.realm.Realm;
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
		sleepId = Application.sleepIDValue.incrementAndGet();
		lastUpdate = System.currentTimeMillis();
		lastUpdate5secs = System.currentTimeMillis();
		Log.d("Service? ", " Created");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId){
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		Sleep sleep = new Sleep();
		sleep.setId(sleepId);
		sleep.setStartTime(Calendar.getInstance().getTimeInMillis());
		sleep.setDate(Calendar.getInstance().getTime());
		db.beginTransaction();
		db.copyToRealm(sleep);
		db.commitTransaction();
		Log.d("Service? ", " Started");
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(this);
		writeToDB(Calendar.getInstance().getTimeInMillis(), motions);
		db.beginTransaction();
		Sleep sleep = db.where(Sleep.class).equalTo("id", sleepId).findFirst();
		sleep.setEndTime(Calendar.getInstance().getTimeInMillis());
		db.commitTransaction();
		// Increment for next trial/sleep
		sleepId++;
		Log.d("Service? ", " Stopped");
		Toast.makeText(this, "Service stopped!", Toast.LENGTH_SHORT).show();
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
		if ((curTime - lastUpdate) > 100) {
			//lastUpdate = curTime;

			//Get history to check for variance
			float accelLast = accelCurrent;

			accelCurrent = (float)Math.sqrt(Math.pow(x,2) + Math.pow(y,2) + Math.pow(z,2));
			float variance = accelCurrent - accelLast;
			float abs_var = Math.abs(variance);

			if(abs_var > 0.05){
				motions++;
				Log.d("Motion: ", Float.toString(abs_var));
			}

			//Commit every 1 minute
			if((curTime - lastUpdate5secs) >= 60000  && !first) {
				lastUpdate5secs = curTime;
				writeToDB(Calendar.getInstance().getTimeInMillis(), motions);
				Log.d("Motion: ", Integer.toString(motions));
				motions = 0;
			}

			first = false;
		}
	}

	private void writeToDB(long timestamp, int amtMotion){
		db.beginTransaction();

		AccelerometerData acc = db.createObject(AccelerometerData.class);
		acc.setTimestamp(timestamp);
		acc.setSleepId(sleepId);
		acc.setAmtMotion(amtMotion);

		db.commitTransaction();
	}

}
