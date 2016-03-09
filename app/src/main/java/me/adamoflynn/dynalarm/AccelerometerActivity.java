package me.adamoflynn.dynalarm;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

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

public class AccelerometerActivity extends Activity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mAccelerometer;

	private int i = 0;
	private long lastUpdate, lastUpdate5secs = 0;
	private int seconds, motions = 0;

	private float accel, accelCurrent, accelLast;
	private TextView acclX, acclY, acclZ, motion;
	private int sleepId;

	private ArrayList<Entry> entries = new ArrayList<>();
	private ArrayList<String> labels = new ArrayList<>();

	private float x, y, z;

	private Realm db;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_accelerometer);

		initializeViews();
		db = Realm.getDefaultInstance();
		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		sleepId = Application.sleepIDValue.incrementAndGet();

	}

	public void onStartClick(View v){
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		Log.d("Value ", Integer.toString(sleepId));
		Sleep sleep = new Sleep();
		sleep.setId(sleepId);
		sleep.setStartTime(Calendar.getInstance().getTimeInMillis());
		sleep.setDate(Calendar.getInstance().getTime());
		db.beginTransaction();
		db.copyToRealm(sleep);
		db.commitTransaction();
	}

	public void onStopClick(View view) {
		mSensorManager.unregisterListener(this);
		initializeChart();

		db.beginTransaction();
		Sleep sleep = db.where(Sleep.class).equalTo("id", sleepId).findFirst();
		sleep.setEndTime(Calendar.getInstance().getTimeInMillis());
		db.commitTransaction();
		// Increment for next trial/sleep
		sleepId++;
		Log.d("KFKF", Integer.toString(sleepId));
	}

	public void initializeViews(){
		acclX = (TextView) findViewById(R.id.acclX);
		motion = (TextView) findViewById(R.id.motion);
	}

	public void initializeChart(){
		LineChart chart = (LineChart)findViewById(R.id.chart);
		LineDataSet dataSet = new LineDataSet(entries, "movement/time");


		dataSet.setDrawCubic(true);
		dataSet.setDrawFilled(true);
		dataSet.setFillColor(ContextCompat.getColor(this, R.color.colorAccent));
		dataSet.setFillAlpha(255);


		chart.setTouchEnabled(true);
		chart.setDragEnabled(true);
		chart.setScaleEnabled(true);
		chart.setPinchZoom(true);


		LineData data = new LineData(labels, dataSet);
		chart.setData(data);
		chart.invalidate();
	}


	@Override
	protected void onResume() {
		super.onResume();

	}

	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		mSensorManager.unregisterListener(this);
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
			accel = accel * 0.1f + variance; //.1

			// This number allows for small motion detection, but none when laying still. (.06)
			if(accel > 0.05){
				motions++;
			}

			if((curTime - lastUpdate5secs) >= 60000) {
				lastUpdate5secs = curTime;
				entries.add(new Entry(motions, i++));
				labels.add(DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()));
				writeToDB(Calendar.getInstance().getTimeInMillis(), motions);
				motions = 0;
			}
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
