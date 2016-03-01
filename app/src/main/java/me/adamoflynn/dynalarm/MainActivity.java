package me.adamoflynn.dynalarm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Calendar;
import java.util.Date;

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
	private int id = 0;
	private long lastUpdate = 0;
	private TextView acclX, acclY, acclZ;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		RealmConfiguration config = new RealmConfiguration.Builder(this)
				.name("default")
				.schemaVersion(1)
				.deleteRealmIfMigrationNeeded()
				.build();

		db = Realm.getInstance(config);


		initializeViews();


		mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
	}


	public void onStartClick(View v){
		mSensorManager.registerListener(this, mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void onStopClick(View view) {
		mSensorManager.unregisterListener(this);
		RealmResults<AccelerometerData> results = db.where(AccelerometerData.class).findAll();
		for (AccelerometerData a:results ) {
			Log.d("Data: ", a.toString());
		}

	}

	public void initializeViews(){
		acclX = (TextView) findViewById(R.id.acclX);
		acclY = (TextView) findViewById(R.id.acclY);
		acclZ = (TextView) findViewById(R.id.acclZ);
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
		float x = event.values[0];
		float y = event.values[1];
		float z = event.values[2];

		long curTime = System.currentTimeMillis();

		if ((curTime - lastUpdate) > 5000) {
			Log.d(" Data", x + "," + y + "," + z + "\n" );
			lastUpdate = curTime;
			acclX.setText(Float.toString(x));
			acclY.setText(Float.toString(y));
			acclZ.setText(Float.toString(z));

		  Date time = Calendar.getInstance().getTime();
			AccelerometerData acc = new AccelerometerData(time, x, y, z);
			db.beginTransaction();
			acc = db.copyToRealm(acc);
			db.commitTransaction();
			id++;
		}
	}
}

