package me.adamoflynn.dynalarm;

import android.Manifest;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import me.adamoflynn.dynalarm.model.Routine;
import me.adamoflynn.dynalarm.services.TrafficService;
import me.adamoflynn.dynalarm.utils.RoutineOnItemSelectedListener;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener, GoogleMap.OnMarkerDragListener {

	private GoogleMap mMap;
	private LatLng to, from = null;
	private TextView toText, fromText, arriveAt;
	private Button fetch, save;
	private Calendar alarmTime = Calendar.getInstance();
	private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final DateFormat hh = new SimpleDateFormat("HH:mm");
	private Marker fromMarker, toMarker;
	private Boolean fromLocationSet, toLocationSet, timeSet;
	private ProgressDialog progressDialog;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		progressDialog = ProgressDialog.show(this, "Loading Map", "Please Wait", true);

		toText = (TextView) findViewById(R.id.to);
		fromText = (TextView) findViewById(R.id.from);
		arriveAt = (TextView) findViewById(R.id.arriveAt);
		fetch = (Button) findViewById(R.id.fetchData);
		save = (Button) findViewById(R.id.save);
		fetch.setOnClickListener(this);
		arriveAt.setOnClickListener(this);
		save.setOnClickListener(this);
		arriveAt.setText(hh.format(alarmTime.getTime()));


		fromLocationSet = false;
		toLocationSet = false;
		timeSet = false;


		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
				&& ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// No location services so go to Dublin City Centre, else go to current location.
			mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.3441, -6.2675), 16));
			return;
		}

		mMap.setMyLocationEnabled(true);
		mMap.setOnMapLongClickListener(this);
		mMap.setOnMarkerDragListener(this);
		progressDialog.dismiss();
  }

	@Override
	public void onMapLongClick(LatLng point) {

		if(!fromLocationSet) {
			fromMarker = mMap.addMarker(new MarkerOptions().position(point).title("From").draggable(true));
			//fromMarker.setDraggable(true);
			fromMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE));
			fromLocationSet = true;

			from = point;
			fromText.setText(from.toString());
		} else if(!toLocationSet) {
			toMarker = mMap.addMarker(new MarkerOptions().position(point).title("To Here").draggable(true));
			//toMarker.setDraggable(true);
			toMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			//toMarker.setDraggable(true);
			toLocationSet = true;

			to = point;
			toText.setText(to.toString());
		}

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
			case R.id.save:
				sendData();
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
				timeSet = true;
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

	@Override
	public void onMarkerDragStart(Marker marker) {

	}

	@Override
	public void onMarkerDrag(Marker marker) {

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		if(marker.getTitle().equals("From")){
			updateFromMarker(marker);
		} else updateToMarker(marker);
	}

	private void updateFromMarker(Marker marker){
		fromMarker = marker;
		from = fromMarker.getPosition();
		fromText.setText(from.toString());
	}

	private void updateToMarker(Marker marker){
		toMarker = marker;
		to = toMarker.getPosition();
		toText.setText(to.toString());
	}

	private void sendData(){
		if(from == null | to == null | !timeSet ){
			showErrorDialog();
		} else{
			String fromA = Double.toString(from.latitude) + "," + Double.toString(from.longitude);
			String toB = Double.toString(to.latitude) + "," + Double.toString(to.longitude);
			checkDifference();

			Intent intent = new Intent();
			intent.putExtra("from", fromA);
			intent.putExtra("to", toB);
			intent.putExtra("time", sdf.format(alarmTime.getTime()));
			setResult(2, intent);
			finish();
		}
	}

	private void showErrorDialog()  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
		builder.setTitle("Enter Required Data");

		builder.setMessage("It seems you haven't entered the required data. Please check you have selected both your from and to locations and also your arrival time.");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		final AlertDialog dialog = builder.show();
	}

}
