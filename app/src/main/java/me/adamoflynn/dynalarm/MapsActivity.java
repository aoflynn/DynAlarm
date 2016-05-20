package me.adamoflynn.dynalarm;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.model.Location;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener,
		View.OnClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnInfoWindowClickListener {

	private GoogleMap mMap;
	private LatLng to, from = null;
	private TextView toEditText, fromEditText, arriveAt;
	private Calendar alarmTime = Calendar.getInstance();
	private final DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
	private final DateFormat hh = new SimpleDateFormat("HH:mm");
	private Marker fromMarker, toMarker;
	private Boolean fromLocationSet, toLocationSet, timeSet;
	private ProgressDialog progressDialog;
	private final String FROM_TITLE = "From This Location";
	private final String TO_TITLE = "To Here";
	private int locationId;
	private Realm realm;
	private Spinner spinnerFrom, spinnerTo;
	private final String SNIP = "Tap here to remove this location!";
	private ArrayList<String> locationSpinner;
	private ArrayList<Integer> locationIDs;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_maps);

		progressDialog = ProgressDialog.show(this, "Loading Map", "Please Wait", true);

		locationId = Application.locationID.incrementAndGet();
		realm = Realm.getDefaultInstance();

		Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(tb);

		if (getSupportActionBar() != null) {
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
			upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
			getSupportActionBar().setHomeAsUpIndicator(upArrow);
			tb.setTitleTextColor(Color.WHITE);
		}

		tb.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});


		arriveAt = (TextView) findViewById(R.id.arriveAt);
		fromEditText = (TextView) findViewById(R.id.fromEdit);
		toEditText = (TextView) findViewById(R.id.toEdit);
		Button done = (Button) findViewById(R.id.done);
		Button add_from = (Button) findViewById(R.id.add_location_from);
		Button add_to = (Button) findViewById(R.id.add_location_to);
		spinnerFrom = (Spinner) findViewById(R.id.spinnerFrom);
		spinnerTo = (Spinner) findViewById(R.id.spinnerTo);
		updateSpinner();

		spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				updateMapFrom(i);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				return;
			}

		});

		spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				updateMapTo(i);
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				return;
			}

		});

		add_from.setOnClickListener(this);
		add_to.setOnClickListener(this);
		arriveAt.setOnClickListener(this);
		done.setOnClickListener(this);
		arriveAt.setText(hh.format(alarmTime.getTime()));

		fromLocationSet = false;
		toLocationSet = false;
		timeSet = false;

		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	private void updateMapFrom(int i) {

		Log.d("SPINNER",locationSpinner.get(i) + Integer.toString(locationIDs.get(i)));

		if(locationIDs.get(i) < 0){
			return; // Do nothing...
		}

		if(fromLocationSet){
			fromMarker.remove();
		}

		ProgressDialog progressDialog = ProgressDialog.show(this, "Loading Location", "Please Wait", true);
		Location locationSelected = realm.where(Location.class).equalTo("id", locationIDs.get(i)).findFirst();
		Log.d("Location:", locationSelected.toString());

		from = new LatLng(locationSelected.getLocLat(), locationSelected.getLocLon());
		fromMarker = mMap.addMarker(new MarkerOptions().position(from).title(FROM_TITLE).snippet("Tap here to remove this location!").draggable(true));
		fromMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
		fromLocationSet = true;
		new LatLngToStringFrom(this).execute(from);

		progressDialog.dismiss();
	}

	private void updateMapTo(int i) {

		Log.d("SPINNER", locationSpinner.get(i) + Integer.toString(locationIDs.get(i)));

		if(locationIDs.get(i) < 0){
			return; // Do nothing...
		}

		if(toLocationSet){
			toMarker.remove();
		}

		ProgressDialog progressDialog = ProgressDialog.show(this, "Loading Location", "Please Wait", true);
		Location locationSelected = realm.where(Location.class).equalTo("id", locationIDs.get(i)).findFirst();
		Log.d("Location:", locationSelected.toString());

		to = new LatLng(locationSelected.getLocLat(), locationSelected.getLocLon());
		toMarker = mMap.addMarker(new MarkerOptions().position(to).title(TO_TITLE).snippet("Tap here to remove this location!").draggable(true));
		toMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
		toLocationSet = true;
		new LatLngToString(this).execute(to);

		progressDialog.dismiss();
	}

	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.3441, -6.2675), 10));

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// TODO: Consider calling
			//    ActivityCompat#requestPermissions
			// here to request the missing permissions, and then overriding
			//   public void onRequestPermissionsResult(int requestCode, String[] permissions,
			//                                          int[] grantResults)
			// to handle the case where the user grants the permission. See the documentation
			// for ActivityCompat#requestPermissions for more details.
			return;
		}
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);
		mMap.setOnMapLongClickListener(this);
		mMap.setOnMarkerDragListener(this);
		mMap.setOnInfoWindowClickListener(this);
		progressDialog.dismiss();
	}

	@Override
	public void onMapLongClick(LatLng point) {

		if(!fromLocationSet) {
			fromMarker = mMap.addMarker(new MarkerOptions().position(point).title(FROM_TITLE).snippet("Tap here to remove this location!").draggable(true));
			fromMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
			fromLocationSet = true;
			from = point;
			new LatLngToStringFrom(this).execute(point);
		} else if(!toLocationSet) {
			toMarker = mMap.addMarker(new MarkerOptions().position(point).title(TO_TITLE).snippet("Tap here to remove this location!").draggable(true));
			toMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
			toLocationSet = true;
			to = point;
			new LatLngToString(this).execute(point);
		}

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.arriveAt:
				timePicker();
				break;
			case R.id.done:
				sendData();
				break;
			case R.id.add_location_from:
				if(TextUtils.isEmpty(fromEditText.getText())) {
					Toast.makeText(this, "No From Location Specified!", Toast.LENGTH_SHORT).show();
				} else {
					buildAndShowInputDialog("from");
				}
				break;
			case R.id.add_location_to:
				if(TextUtils.isEmpty(toEditText.getText())){
					Toast.makeText(this, "No To Location Specified!", Toast.LENGTH_SHORT).show();
				} else {
					buildAndShowInputDialog("to");
				}
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

	private void updateSpinner(){
		RealmResults<Location> locations = realm.where(Location.class).findAll();
		List<Location> locationList = locations;

		Log.d("Locations", locationList.toString());
		locationSpinner = new ArrayList<>();
		locationIDs = new ArrayList<>();
		locationSpinner.add(0, "");
		locationIDs.add(0, -1);
		for (Location l:locationList){
			locationSpinner.add(l.getLocation());
			locationIDs.add(l.getId());
		}

		ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, locationSpinner);
		spinnerFrom.setAdapter(arrayAdapter);
		spinnerTo.setAdapter(arrayAdapter);
	}

	@Override
	public void onMarkerDragStart(Marker marker) {

	}

	@Override
	public void onMarkerDrag(Marker marker) {

	}

	@Override
	public void onMarkerDragEnd(Marker marker) {
		if(marker.getTitle().equals(FROM_TITLE)){
			updateFromMarker(marker);
			new LatLngToStringFrom(this).execute(marker.getPosition());
		} else {
			updateToMarker(marker);
			new LatLngToString(this).execute(marker.getPosition());
		}
	}

	private void updateFromMarker(Marker marker){
		fromMarker = marker;
		from = fromMarker.getPosition();
	}

	private void updateToMarker(Marker marker){
		toMarker = marker;
		to = toMarker.getPosition();
	}

	private void sendData(){
		if(!fromLocationSet || !toLocationSet || !timeSet ){
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

	@Override
	public void onInfoWindowClick(Marker marker) {
		if (fromMarker.equals(marker)){
			fromMarker.remove();
			fromEditText.setText("");
			fromLocationSet = false;
			from = null;
		} else{
			toMarker.remove();
			toEditText.setText("");
			toLocationSet = false;
			to = null;
		}
	}

	private void buildAndShowInputDialog(final String dest)  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
		builder.setTitle("Add Location");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.map_add_location, null);

		final EditText locationName = (EditText) dialogView.findViewById(R.id.locationName);
		final TextView address = (TextView) dialogView.findViewById(R.id.address);
		if(dest.equals("from"))
			address.setText(fromEditText.getText().toString());
		else address.setText(toEditText.getText().toString());

		builder.setView(dialogView);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dest.equals("from")) {
					addLocation(locationId, locationName.getText().toString(), address.getText().toString(), from);
				} else {
					addLocation(locationId, locationName.getText().toString(), address.getText().toString(), to);
				}
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		final AlertDialog dialog = builder.show();

		locationName.setOnEditorActionListener(
				new EditText.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE ||
								(event.getAction() == KeyEvent.ACTION_DOWN &&
										event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							dialog.dismiss();
							if(dest.equals("from")) {
								addLocation(locationId, locationName.getText().toString(), address.getText().toString(), from);
								return true;
							}
							else {
								addLocation(locationId, locationName.getText().toString(), address.getText().toString(), to);
								return true;
							}
						}
						return false;
					}
				});
	}

	private void addLocation(int locationId, String name, String address, LatLng loc){
		if ( name == null || name.length() == 0) {
			Toast.makeText(this, "Empty Name! Try Again", Toast.LENGTH_SHORT).show();
			return;
		}
		Location location = new Location(locationId, name, address, loc.latitude, loc.longitude);
		realm.beginTransaction();
		realm.copyToRealm(location);
		realm.commitTransaction();
		locationId++;
		updateSpinner();
	}


	/**
	 *  AsyncTasks to get background geocode information
	 */

	private class LatLngToString extends AsyncTask<LatLng, Void, String> {
		ProgressDialog dialog;
		Context mContext;
		String errorMessage;

		LatLngToString(Context context){
			mContext = context;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Getting address...");
			dialog.show();
		}

		@Override
		protected String doInBackground(LatLng... params) {
			Geocoder geocoder = new Geocoder(mContext);
			double latitude = params[0].latitude;
			double longitude = params[0].longitude;

			List<Address> addresses = null;
			String addressText="";

			try {
				addresses = geocoder.getFromLocation(latitude, longitude, 1);
			} catch (IOException e) {
				errorMessage = "Service not available. Please check network connectivity.";
				Log.e("Geocoder:", errorMessage);
			}

			if(addresses != null && addresses.size() > 0 ){
				Address address = addresses.get(0);

				addressText = String.format("%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
						address.getLocality() == null ? "" : address.getLocality(),
						address.getCountryName());
				return addressText;
			} else {
				return null;
			}
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if(s != null){
				toEditText.setText(s);
				dialog.dismiss();
			} else {
				dialog.dismiss();
				Toast.makeText(mContext, "Error in getting address. Check network status.", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class LatLngToStringFrom extends AsyncTask<LatLng, Void, String> {
		String errorMessage = "";
		ProgressDialog dialog;
		Context mContext;

		LatLngToStringFrom(Context context){
			mContext = context;
		}
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Getting address...");
			dialog.show();
		}

		@Override
		protected String doInBackground(LatLng... params) {
			Geocoder geocoder = new Geocoder(mContext, Locale.UK);
			double latitude = params[0].latitude;
			double longitude = params[0].longitude;

			List<Address> addresses = null;
			String addressText="";

			try {
				addresses = geocoder.getFromLocation(latitude, longitude, 1);
			} catch (IOException e) {
				errorMessage = "Service not available. Please check network connectivity.";
				Log.e("Geocoder:", errorMessage);
			}

			if(addresses != null && addresses.size() > 0 ){
				Address address = addresses.get(0);

				addressText = String.format("%s, %s, %s",
						address.getMaxAddressLineIndex() > 0 ? address.getAddressLine(0) : "",
						address.getLocality() == null ? "" : address.getLocality(),
						address.getCountryName());
				return addressText;
			} else {
				return null;
			}

		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);
			if(s != null){
				fromEditText.setText(s);
				dialog.dismiss();
				Toast.makeText(mContext, s, Toast.LENGTH_SHORT).show();
			} else {
				dialog.dismiss();
				Toast.makeText(mContext, "Error in getting address. Check network status.", Toast.LENGTH_SHORT).show();
			}
		}
	}
}