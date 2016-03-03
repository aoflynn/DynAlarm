package me.adamoflynn.dynalarm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.DropBoxManager;
import android.support.v7.app.AppCompatActivity;
import android.util.FloatMath;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Location;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

	private Realm db;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private final String FILE_DIR = "data/data/me.adamoflynn.dynalarm/files/";
	private final String FILE_NAME = "sleep_file";
	private int i = 0;
	private long lastUpdate = 0;
	private float[] mGravity;
	private float accel, accelCurrent, accelLast;
	private TextView acclX, acclY, acclZ, motion;
	private LineChart chart;
	private ArrayList<Entry> entries = new ArrayList<>();
	private ArrayList<String> labels = new ArrayList<>();
	private float x, y, z, lastX, lastY, lastZ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		dbSetup();
		initializeViews();
		//initializeChart();

		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

	}


	public void onStartClick(View v){
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onStopClick(View view) {
		mSensorManager.unregisterListener(this);
		initializeChart();
		Log.d("Size of data:", Integer.toString(entries.size()));
	}

	public void initializeViews(){
		acclX = (TextView) findViewById(R.id.acclX);
		//acclY = (TextView) findViewById(R.id.acclY);
		//acclZ = (TextView) findViewById(R.id.acclZ);
		motion = (TextView) findViewById(R.id.motion);
	}

	public void initializeChart(){
		LineChart chart = (LineChart)findViewById(R.id.chart);
		LineDataSet dataSet = new LineDataSet(entries, "movement/time");
		dataSet.setDrawCubic(true);
		dataSet.setDrawFilled(true);
		dataSet.setFillColor(1);

		LineData data = new LineData(labels, dataSet);
		chart.setData(data);
		chart.invalidate();
	}

	protected void dbSetup(){
		RealmConfiguration config = new RealmConfiguration.Builder(this)
				.name("default")
				.schemaVersion(1)
				.deleteRealmIfMigrationNeeded()
				.build();

		db = Realm.getInstance(config);
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
		x = event.values[0];
		y = event.values[1];
		z = event.values[2];
		long curTime = System.currentTimeMillis();

		if ((curTime - lastUpdate) > 100) {
			lastUpdate = curTime;
			long curTimeSec = curTime/1000;
			mGravity = event.values.clone();
			accelLast = accelCurrent;
			accelCurrent = (float)Math.sqrt(x*x + y*y + z*z);
			lastX = x;
			lastY = y;
			lastZ = z;
			float variance = accelCurrent - accelLast;
			accel = accel * 0.1f + variance; //.1

			Log.d("Accel Before data: ", Float.toString(accelCurrent));
			Log.d("Accel data: ", Float.toString(accel));
			Log.d("Delta data: ", Float.toString(variance));

			if(accel > 0.06){
				motion.setText("Motion!!!");
				acclX.setText(Float.toString(accel));
				entries.add(new Entry(accel, i++));
				labels.add(Long.toString(curTimeSec));
			}
			else{
				motion.setText("No motion!!");
			}

		}
	}
}

