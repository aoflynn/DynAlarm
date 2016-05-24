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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.model.Location;
import me.adamoflynn.dynalarm.model.TrafficInfo;

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
	private ArrayList<String> locationSpinner;
	private ArrayList<Integer> locationIDs;
	private List<Location> locationList;
	private TrafficInfo trafficInfo;

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
		updateSpinner(); // populate the spinners with saved locations

		spinnerFrom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				updateMapFrom(i); // update from parts of map i.e. marker and text view
			}

			@Override
			public void onNothingSelected(AdapterView<?> adapterView) {
				return;
			}

		});

		spinnerTo.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
				updateMapTo(i); // update to parts of map i.e. marker and text view
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


		// booleans to track what data there has been set
		fromLocationSet = false;
		toLocationSet = false;
		timeSet = false;

		// Call map
		SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
				.findFragmentById(R.id.map);
		mapFragment.getMapAsync(this);
	}

	@Override
	public void onResume(){
		super.onResume();
		updateSpinner(); // When coming back from locations activity, should update spinner
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.maps_menu, menu); // Add my custom menu to the options menu
		return true;
	}

	// Menu method to handle clicks
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle item selection
		switch (item.getItemId()) {
			case R.id.manage_locs:
				Intent intent = new Intent(this, LocationActivity.class); // Go to manange location activity
				startActivity(intent);
				return true;
			case R.id.clear_map:
				showClearDialog(); // clear all markers
				return true;
			default:
				return super.onOptionsItemSelected(item);
		}
	}

	// Clear the map and the required text
	private void clearMap(){
		clearFrom();
		clearTo();
	}

	private void updateMapFrom(int i) {

		// If my filler value or my flag value for not updating, return spinner to 0
		if(i == -1 || locationIDs.get(i) < 0){
			spinnerFrom.setSelection(0);
			return; // Do nothing...
		}

		// other wise update map with respects to  the spinner selection of the saved location
		Log.d("SPINNER",locationSpinner.get(i) + Integer.toString(locationIDs.get(i)));

		if(fromLocationSet){ // Remove current marker before adding a new one
			fromMarker.remove();
		}

		ProgressDialog progressDialog = ProgressDialog.show(this, "Loading Location", "Please Wait", true);
		Location locationSelected = realm.where(Location.class).equalTo("id", locationIDs.get(i)).findFirst();
		Log.d("Location:", locationSelected.toString());

		// Assign the selected location to the from values and asynchronously call the geocoder API
		from = new LatLng(locationSelected.getLocLat(), locationSelected.getLocLon());
		fromMarker = mMap.addMarker(new MarkerOptions().position(from).title(FROM_TITLE).snippet("Tap here to remove this location!").draggable(true)); // update map marker
		fromMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
		fromLocationSet = true;
		new LatLngToStringFrom(this).execute(from);

		progressDialog.dismiss();
	}


	// same as from method but in relation to the to values in the activity
	private void updateMapTo(int i) {

		if(i == -1 || locationIDs.get(i) < 0){
			spinnerTo.setSelection(0);
			return; // Do nothing...
		}

		Log.d("SPINNER", locationSpinner.get(i) + Integer.toString(locationIDs.get(i)));

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

	// When map loads in, animate to Dublin
	@Override
	public void onMapReady(GoogleMap googleMap) {
		mMap = googleMap;
		mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(53.3441, -6.2675), 10));

		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
			// Required Wrap Around for getting location but actually only needed for Marshmallow.
			return;
		}
		// Get user location
		mMap.setMyLocationEnabled(true);
		mMap.getUiSettings().setZoomControlsEnabled(true);

		// Set required listeners
		mMap.setOnMapLongClickListener(this);
		mMap.setOnMarkerDragListener(this);
		mMap.setOnInfoWindowClickListener(this);
		progressDialog.dismiss();
	}


	// This method is used to add indiviudual markers to the map and update their respective values
	@Override
	public void onMapLongClick(LatLng point) {

		if(!fromLocationSet) { // No From location set yet, so set it
			fromMarker = mMap.addMarker(new MarkerOptions().position(point).title(FROM_TITLE).snippet("Tap here to remove this location!").draggable(true));
			fromMarker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE));
			fromLocationSet = true;
			from = point;
			new LatLngToStringFrom(this).execute(point); //  aynch geocoder call
		} else if(!toLocationSet) { // from location is set, now do to lcoation
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
				timePicker(); // show time picker
				break;
			case R.id.done:
				sendData(); // send data back to application
				break;
			case R.id.add_location_from: // Add location in the from text to the DB
				if(TextUtils.isEmpty(fromEditText.getText())) {
					Toast.makeText(this, "No From Location Specified!", Toast.LENGTH_SHORT).show();
				} else {
					buildAndShowInputDialog("from");
				}
				break;
			case R.id.add_location_to: // Add location in the to text to the DB
				if(TextUtils.isEmpty(toEditText.getText())){
					Toast.makeText(this, "No To Location Specified!", Toast.LENGTH_SHORT).show();
				} else {
					buildAndShowInputDialog("to");
				}
				break;
		}
	}

	// Similar to alarmfragment
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

	// This method makes sure that all "past" times are future times. This stops alarms going off
	// immediately because the system thought they were set for earlier in the current day, rather than the next day
	private void checkDifference(){
		long differenceInTime = Calendar.getInstance().getTimeInMillis() - alarmTime.getTimeInMillis();
		if(differenceInTime > 0){
			alarmTime.add(Calendar.HOUR_OF_DAY, 24);
			arriveAt.setText(hh.format(alarmTime.getTime()));
		}
	}

	// Add the saved locations to the spinner
	private void updateSpinner(){
		RealmResults<Location> locations = realm.where(Location.class).findAll();
		locationList = locations;

		Log.d("Locations", locationList.toString());
		locationSpinner = new ArrayList<>(); // lcoationName
		locationIDs = new ArrayList<>(); // locationID
		locationSpinner.add(0, ""); // Filler value for Name
		locationIDs.add(0, -1); // Filler value for ID
		for (Location l:locationList){
			locationSpinner.add(l.getLocation());
			locationIDs.add(l.getId());
		}

		// Set the spinners to the saved locations , while the other array tracks the unique id of the location
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

	// Drag the marker to specified location and update the required data and map
	@Override
	public void onMarkerDragEnd(Marker marker) {
		if(marker.getTitle().equals(FROM_TITLE)){ // If from marker, update that
			updateFromMarker(marker);
			new LatLngToStringFrom(this).execute(marker.getPosition());
		} else { // else, must be to marker, update that
			updateToMarker(marker);
			new LatLngToString(this).execute(marker.getPosition());
		}
	}

	// Update from data with new marker data
	private void updateFromMarker(Marker marker){
		fromMarker = marker;
		from = fromMarker.getPosition();
		updateMapFrom(-1);
	}

	private void updateToMarker(Marker marker){
		toMarker = marker;
		to = toMarker.getPosition();
		updateMapTo(-1);
	}

	// When user saves, this will send the data back to the alarm fragment
	private void sendData(){
		if(!fromLocationSet || !toLocationSet || !timeSet ){
			showErrorDialog();
		} else{
			final String fromA = Double.toString(from.latitude) + "," + Double.toString(from.longitude);
			final String toB = Double.toString(to.latitude) + "," + Double.toString(to.longitude);
			Log.d("DEETS", fromA + toB + sdf.format(alarmTime.getTime()));
			checkDifference();
			new GetJourneyDuration(this).execute(fromA, toB, sdf.format(alarmTime.getTime()));
			showConfirmationDialog();
		}
	}


	// Tell user how long it should usually take to go from A to B and arriving at Z
	private void showConfirmationDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
		builder.setTitle("Journey Details");


		final String fromA = Double.toString(from.latitude) + "," + Double.toString(from.longitude);
		final String toB = Double.toString(to.latitude) + "," + Double.toString(to.longitude);
		checkDifference();
		int journeyTime = (int) Math.round(trafficInfo.getHistoricTravelTime() / 60.00);

		//Log.d("TRaffic", trafficInfo.toString());
		builder.setMessage("The journey from " + fromEditText.getText().toString() + " to "
				+ toEditText.getText().toString() + " will take around " + journeyTime + " minutes if you leave at "
				+ hh.format(trafficInfo.getDepartureTime()));

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) { // Send required data back
				dialog.cancel();

				Intent intent = new Intent();
				intent.putExtra("from", fromA);
				intent.putExtra("to", toB);
				intent.putExtra("time", sdf.format(alarmTime.getTime()));

				setResult(2, intent);
				finish();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				progressDialog.dismiss();
			}
		});

		final AlertDialog dialog = builder.show();
	}

	// prompt to show users don't have enough data
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

	// dialog to clear map
	private void showClearDialog()  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
		builder.setTitle("Are you sure?");

		builder.setMessage("Do you want to clear the map and your selected locations?");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				clearMap();
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		final AlertDialog dialog = builder.show();
	}

	// remove individual markers from map
	@Override
	public void onInfoWindowClick(Marker marker) {
		if (fromMarker.equals(marker)){
			clearFrom();
		} else{
			clearTo();
		}
	}

	private void clearFrom(){
		fromMarker.remove();
		fromEditText.setText("");
		fromLocationSet = false;
		from = null;
		updateMapFrom(-1);
	}

	private void clearTo(){
		toMarker.remove();
		toEditText.setText("");
		toLocationSet = false;
		to = null;
		updateMapTo(-1);
	}

	// add location dialog
	private void buildAndShowInputDialog(final String dest)  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(MapsActivity.this);
		builder.setTitle("Add Location");

		LayoutInflater li = LayoutInflater.from(this);
		// custom layout
		View dialogView = li.inflate(R.layout.map_add_location, null);

		final EditText locationName = (EditText) dialogView.findViewById(R.id.locationName);
		final TextView address = (TextView) dialogView.findViewById(R.id.address);
		//depending on the button clicked, get the correct address to show user when saving
		if(dest.equals("from"))
			address.setText(fromEditText.getText().toString());
		else address.setText(toEditText.getText().toString());

		builder.setView(dialogView);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				if (dest.equals("from")) { // if from add location button, add the location to that address
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


		// Editor action to dismiss keyboard
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

	// Add location to database
	private void addLocation(int locId, String name, String address, LatLng loc){
		if ( name == null || name.length() == 0) {
			Toast.makeText(this, "Empty Name! Try Again", Toast.LENGTH_SHORT).show();
			return;
		}

		Location location = new Location(locId, name, address, loc.latitude, loc.longitude);
		realm.beginTransaction();
		realm.copyToRealm(location);
		realm.commitTransaction();
		locationId++;
		updateSpinner();
	}


	/**
	 *  AsyncTasks to get background geocode information
	 */


	// This method focuses on the "to" values
	private class LatLngToString extends AsyncTask<LatLng, Void, String> {
		ProgressDialog dialog;
		final Context mContext;
		String errorMessage;

		LatLngToString(Context context){
			mContext = context;
		}

		// Show progress dialog
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

			// Try to get the reverse location from geopoints
			try {
				addresses = geocoder.getFromLocation(latitude, longitude, 1);
			} catch (IOException e) { // Network error catch
				errorMessage = "Service not available. Please check network connectivity.";
				Log.e("Geocoder:", errorMessage);
			}

			// If an address is returned, get the first one and send it back
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

		// Update the to values on map and text views
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

	// Simialr to above just with from values
	private class LatLngToStringFrom extends AsyncTask<LatLng, Void, String> {
		String errorMessage = "";
		ProgressDialog dialog;
		final Context mContext;

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


	// AsyncTask to contact the TomTom API, same implementation to traffic service expect
	// this isn' a service but an async task
	private class GetJourneyDuration extends AsyncTask<String, String, String> {

		private ProgressDialog dialog;
		private final Context mContext;
		private final String BASE_URL = "https://api.tomtom.com/routing/1/calculateRoute/";
		private final String API_KEY = "nmqjmepdy9ppbp8yekvrsaet";
		private final String END_URL = "?key="+ API_KEY + "&routeType=fastest&traffic=true&computeTravelTimeFor=all&arriveAt=";

		private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

		GetJourneyDuration(Context context){
			mContext = context;
		}

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			dialog = new ProgressDialog(mContext);
			dialog.setMessage("Getting details about journey...");
			dialog.show();
		}

		@Override
		protected String doInBackground(String... params) {

			Log.d("Execute:", params[0] + params[1] + params[2]);
			String from = params[0];
			String to = params[1];
			String time = params[2];
			Log.d("DEETS", params[0]  +" " + params[1] + " " + sdf.format(alarmTime.getTime()));

			try {
				URL url = new URL(BASE_URL + from + ":" + to + "/json" + END_URL + time);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				int statusCode = urlConnection.getResponseCode();

				if(statusCode == 200){
					BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
					StringBuilder stringBuilder = new StringBuilder();
					String line;

					while ((line = bufferedReader.readLine()) != null) {
						stringBuilder.append(line).append("\n");
					}

					bufferedReader.close();
					urlConnection.disconnect();
					return stringBuilder.toString();
				} else if (statusCode != 200){
					Log.d("CODE:", Integer.toString(statusCode));
					return null;
				} else {throw new Exception("Download Error");}

			} catch (MalformedURLException e){
				Log.e("MalformedURLException", e.getMessage());
				return null;
			} catch (IOException e){
				Log.e("IOException", e.getMessage());
				return null;
			} catch (Exception e){
				Log.e("Error", e.getMessage());
				return null;
			}
		}

	protected void onPostExecute(String response){
		if(response == null) {
			response = "Error...";
		}
		parseJSON(response);
		dialog.dismiss();
		showConfirmationDialog();
	}

	public void parseJSON(String response) {

		try{

			JSONObject jsonObject = new JSONObject(response);
			JSONArray allRoutes = jsonObject.getJSONArray("routes");

			// Only need one route
			JSONObject route = allRoutes.getJSONObject(0);
			JSONObject summary = route.getJSONObject("summary");

			int lengthInMeters = summary.getInt("lengthInMeters");
			int travelTimeInSeconds = summary.getInt("travelTimeInSeconds");
			int travelDelayInSeconds = summary.getInt("trafficDelayInSeconds");
			Date dep = removeT(summary.getString("departureTime"));
			Date arr = removeT(summary.getString("arrivalTime"));
			int travelTimeNoTraffic = summary.getInt("noTrafficTravelTimeInSeconds");
			int historicTravelTime = summary.getInt("historicTrafficTravelTimeInSeconds");
			int liveIncidents = summary.getInt("liveTrafficIncidentsTravelTimeInSeconds");

			trafficInfo = new TrafficInfo(lengthInMeters, travelTimeInSeconds, travelDelayInSeconds, dep, arr, travelTimeNoTraffic, historicTravelTime, liveIncidents);
			Log.d("Data", trafficInfo.toString());
		}catch (JSONException e) {
			Log.e("JSON parse exception", e.getMessage());
		}

	}

	public Date removeT(String time){
		time = time.replace('T',' ');
		Date d = null;
		try{
			d = df.parse(time);
		}catch (Exception e){
			Log.d("Error", e.getMessage());
		}
		return d;
	}
}

}