package me.adamoflynn.dynalarm;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Sleep;

public class AccelerometerService extends Service implements SensorEventListener{

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	private int i = 0;
	private long lastUpdate, lastUpdate5secs = 0;
	private int seconds, motions = 0;

	private float accel, accelCurrent, accelLast;
	private TextView acclX, acclY, acclZ, motion;
	private int sleepId;
	private Boolean first = true;

	private float x, y, z;

	private Realm db;

	public AccelerometerService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		// TODO: Return the communication channel to the service.
		throw new UnsupportedOperationException("Not yet implemented");
	}


	@Override
	public void onCreate() {
		db = Realm.getDefaultInstance();
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		sleepId = Application.sleepIDValue.incrementAndGet();
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
		x = event.values[0];
		y = event.values[1];
		z = event.values[2];
		long curTime = System.currentTimeMillis();

		if ((curTime - lastUpdate) > 100) {
			lastUpdate = curTime;
			long curTimeSec = curTime/1000;

			accelLast = accelCurrent;
			accelCurrent = (float)Math.sqrt(x*x + y*y + z*z);
			float variance = accelCurrent - accelLast;
			accel = accel * 0.9f + variance; //.1

			// This number allows for small motion detection, but none when laying still. (.06)
			if(accel > 0.05){
				motions++;
			}


			if((curTime - lastUpdate5secs) >= 60000  && first == false) {
				lastUpdate5secs = curTime;
				writeToDB(Calendar.getInstance().getTimeInMillis(), motions);
				Log.d("Motion: ", Integer.toString(motions));
				motions = 0;
			}

			first = false;
		}
	}

	protected void writeToDB(long timestamp, int amtMotion){
		db.beginTransaction();
		AccelerometerData acc = db.createObject(AccelerometerData.class);
		acc.setTimestamp(timestamp);
		acc.setSleepId(sleepId);
		acc.setAmtMotion(amtMotion);
		db.commitTransaction();
	}

}
