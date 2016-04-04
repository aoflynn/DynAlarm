package me.adamoflynn.dynalarm;

import android.app.ActivityManager;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import me.adamoflynn.dynalarm.services.TrafficService;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener {

  private GoogleMap mMap;
	private LatLng to, from;
	private TextView toText, fromText, arriveAt;
	private Button fetch;
	private Calendar alarmTime = Calendar.getInstance();
	private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final DateFormat hh = new SimpleDateFormat("HH:mm");

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);

		toText = (TextView)findViewById(R.id.to);
	  fromText = (TextView)findViewById(R.id.from);
	  arriveAt = (TextView)findViewById(R.id.arriveAt);
	  fetch = (Button)findViewById(R.id.fetchData);
	  fetch.setOnClickListener(this);
	  arriveAt.setOnClickListener(this);


    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
	}

  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    from = new LatLng(53.382683, -6.245243);
    mMap.addMarker(new MarkerOptions().position(from).title("From Here"));
    mMap.moveCamera(CameraUpdateFactory.newLatLng(from));
	  mMap.moveCamera(CameraUpdateFactory.zoomTo(15));
    mMap.setOnMapLongClickListener(this);
    fromText.setText(from.toString());
  }

	@Override
	public void onMapLongClick(LatLng point) {
		to = point;
		mMap.addMarker(new MarkerOptions().position(to).title("To Here"));
		toText.setText(to.toString());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.fetchData:
				String fromA = Double.toString(from.latitude) + "," + Double.toString(from.longitude);
				String toB = Double.toString(to.latitude) + "," + Double.toString(to.longitude);
				checkDifference();

				Intent intent = new Intent(this, TrafficService.class);
				intent.putExtra("from", fromA);
				intent.putExtra("to", toB);
				intent.putExtra("time", sdf.format(alarmTime.getTime()));

				startService(intent);
				Log.d("Traffic Running?", Boolean.toString(isMyServiceRunning(TrafficService.class)));
				break;
			case R.id.arriveAt:
				timePicker();
				break;
		}
	}

	private void timePicker(){
		Calendar mCurrentTime = Calendar.getInstance();
		int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
		int minute = mCurrentTime.get(Calendar.MINUTE);
		TimePickerDialog pickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
			@Override
			public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
				alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
				alarmTime.set(Calendar.MINUTE, minute);
				arriveAt.setText(hh.format(alarmTime.getTime()));
			}
		}, hour, minute, true);
		pickerDialog.setTitle("Select Time");
		pickerDialog.show();
	}

	private void checkDifference(){
		long differenceInTime = Calendar.getInstance().getTimeInMillis() - alarmTime.getTimeInMillis();
		if(differenceInTime > 0){
			alarmTime.add(Calendar.HOUR_OF_DAY, 24);
			arriveAt.setText(hh.format(alarmTime.getTime()));
		}
	}


	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
