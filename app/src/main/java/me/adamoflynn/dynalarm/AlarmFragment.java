package me.adamoflynn.dynalarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Locale;

import io.realm.Realm;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Routine;
import me.adamoflynn.dynalarm.receivers.AlarmReceiver;
import me.adamoflynn.dynalarm.receivers.WakeUpReceiver;
import me.adamoflynn.dynalarm.services.AccelerometerService;
import me.adamoflynn.dynalarm.services.AlarmSound;
import me.adamoflynn.dynalarm.utils.Utils;

public class AlarmFragment extends Fragment implements View.OnClickListener {

	private TextView currentTime, wakeUpTime;
	private CheckBox routineCheck, trafficCheck;

	private AlarmManager alarmManager;
	private Calendar alarmTime = Calendar.getInstance();
	private Calendar wkUpServiceTime = Calendar.getInstance();
	private Calendar timeSet = Calendar.getInstance();
	private final DateFormat sdf = new SimpleDateFormat("HH:mm");
	private final DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd, yyyy", Locale.ENGLISH);
	private HashSet<Integer> routinesChecked = new HashSet<>();
	private String fromA, toB, time;
	private long timeframe;
	private boolean isTimeSet, isMaps = false;
	private final long POLLING_TIME = 120000;
	private final long POLLING_TIME_NO_TRAFFIC = 60000;
	private int sleepId;
	private int routineTime = 0;
	private Realm realm;
	private SharedPreferences prefs;


	public AlarmFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		// Show custom view

		View v = inflater.inflate(R.layout.fragment_alarm, container, false);

		// Open DB and settings connections

		realm = Realm.getDefaultInstance();
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());

		// Get user defined timeframe from settings
		int tf = Integer.parseInt(prefs.getString("timeframe", "20"));
		timeframe = tf * 60000;

		// Set up UI views for fragment
		initializeTime(v);
		initializeButtons(v);
		initializeExtras(v);
		return v;
	}


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onResume() {
		super.onResume();
	}

	// Display the current times and the wake timeframe
	private void initializeTime(View v){
		currentTime = (TextView) v.findViewById(R.id.duration);
		wakeUpTime = (TextView) v.findViewById(R.id.wakeUp);
		Date curTime = Calendar.getInstance().getTime();

		wkUpServiceTime.setTimeInMillis(curTime.getTime() - timeframe);

		currentTime.setText(sdf.format(curTime));
		wakeUpTime.setText("Wake up between " + sdf.format(wkUpServiceTime.getTime().getTime()) + " and " + sdf.format(curTime));
		currentTime.setOnClickListener(this);
	}

	// Set click listensers for buttons
	private void initializeButtons(View v){
		Button start = (Button) v.findViewById(R.id.start);
		start.setOnClickListener(this);

		Button accel = (Button) v.findViewById(R.id.routines);
		accel.setOnClickListener(this);

		Button stop = (Button) v.findViewById(R.id.stop);
		stop.setOnClickListener(this);

		Button maps = (Button) v.findViewById(R.id.mapsButton);
		maps.setOnClickListener(this);
	}

	private void initializeExtras(View v){
		routineCheck = (CheckBox) v.findViewById(R.id.routineCheck);
		trafficCheck = (CheckBox) v.findViewById(R.id.trafficCheck);
	}


	// Method to check what got clicked
	public void onClick(View v){
		switch(v.getId()){
			case R.id.duration: // Time clicked, show time picker
				timePicker();
				break;
			case R.id.start: // Start clicked
				if(!Utils.isMyServiceRunning(AccelerometerService.class, getActivity())){ // Is the accelerometer already running? If it is, show toast
					if(!isTimeSet) showNoTimeSet(); // If no time set for alarm, should prompt
					else {
						timeSet = Calendar.getInstance(); // Get when alarm was set
						showAlarmConfirmation(); // Show prompt
					}
				} else Toast.makeText(getActivity(), "Alarm currently on, please cancel it to set another alarm.", Toast.LENGTH_LONG).show(); // Service already running
				break;
			case R.id.stop:
				if(Utils.isMyServiceRunning(AccelerometerService.class, getActivity())){ // Alarm running? Cancel it
					showCancelConfirmation();
				} else Toast.makeText(getActivity(), "No alarm currently set.", Toast.LENGTH_LONG).show();
				break;
			case R.id.routines: // Routines clicked, go to new routine activity and wait for result
				Intent routines = new Intent(getActivity(), RoutineActivity.class);
				startActivityForResult(routines, 1);
				break;
			case R.id.mapsButton: // Maps clicked, go to Maps activity
				Intent maps = new Intent(getActivity(), MapsActivity.class);
				startActivityForResult(maps, 2);
				break;
		}
	}

	// Show user the time picker dialog to specify alarm time
	private void timePicker(){
		Calendar mCurrentTime = Calendar.getInstance();
		int hour = mCurrentTime.get(Calendar.HOUR_OF_DAY);
		int minute = mCurrentTime.get(Calendar.MINUTE);
			TimePickerDialog pickerDialog = new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
				@Override
				public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
					alarmTime.set(Calendar.HOUR_OF_DAY, hourOfDay);
					alarmTime.set(Calendar.MINUTE, minute);
					currentTime.setText(sdf.format(alarmTime.getTime()));
					wkUpServiceTime.setTimeInMillis(alarmTime.getTimeInMillis() - timeframe); // get the time frame which will be used to set service alarms
					wakeUpTime.setText("Wake up between " + sdf.format(wkUpServiceTime.getTime()) + " and " + sdf.format(alarmTime.getTime()));
					isTimeSet = true; // timeSet so we can set alarm now
				}
			}, hour, minute, true);
		pickerDialog.setTitle("Select Time");
		pickerDialog.show();
	}


	private void showAlarmConfirmation()  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Set Alarm");

		// Inflate layout from my custom file confirm_alarm
		LayoutInflater li = LayoutInflater.from(getActivity());
		View dialogView = li.inflate(R.layout.confirm_alarm, null);

		// Set up UI resources in dialog
		final TextView time = (TextView) dialogView.findViewById(R.id.duration);
		final TextView timeframeText = (TextView) dialogView.findViewById(R.id.timeframe);
		final TextView options = (TextView) dialogView.findViewById(R.id.options);

		// Show times they specified
		time.setText(sdf.format(timeSet.getTime()) + " to " + sdf.format(alarmTime.getTime()));
		timeframeText.setText("with a wake timeframe of " + Long.toString(timeframe/60000L) + " minutes");

		// Check what the user has selected RE: wake up methods
		final boolean usingMaps = isMaps && trafficCheck.isChecked();
		final boolean usingRoutines = routinesChecked.size() > 0 && routineCheck.isChecked();

		// Set the text that correlates to what the user selected
		if(usingMaps && usingRoutines) {
			options.setText("You are using journey, routine, and sleep data to wake up.");
		} else if (usingMaps) {
			options.setText("You are only using journey and sleep data to wake up.");
		} else if (usingRoutines) {
			options.setText("You are only using routine and sleep data to wake up.");
		} else {
			options.setText("You are only using sleep data to wake up.");
		}

		// Set up view and action buttons
		builder.setView(dialogView);
		builder.setPositiveButton("Set Alarm", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setWakeUpAlarm(); // Set general alarm to go off at time specified
				if (usingMaps) {
					setUpTrafficService(); // If they're using maps, set up traffic service to go off
				} else {
					setUpWakeService(); // Else, just set up wake service (movement only)
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
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void setWakeUpAlarm(){

		if(!isTimeSet){
			Toast.makeText(getActivity(), "No Alarm Set!", Toast.LENGTH_SHORT).show();
			return;
		}

		// If alarm times are in future, we need to add 24 hours to them so they don't fire immediately
		// as if they were in the past. e.g. time is 23:12. User sets time to 7:30. The time selected as in the past unless
		// we add 24 hours.

		alarmTime = checkDifference(alarmTime);
		wkUpServiceTime = checkDifference(wkUpServiceTime);

		Realm realm = Realm.getDefaultInstance();

		// Get latest sleep ID so no conflicts
		Number newestData = realm.where(AccelerometerData.class).max("sleepId");
		if(newestData == null){
			sleepId = -1; // If no data in DB, give a base ID.
		} else {
			sleepId = newestData.intValue();
		}

		sleepId += 1; // Increment latest ID so we get a unique id

		// This sets up the base alarm that will wake the user depending on the situation
		Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
		intent.putExtra("MESSAGE", "Wake Up!");

		// This pending intent will trigger AlarmReceiver - a broadcast receiver
		// The alarm manager sets the exact time when the pendingintent should trigger - i.e. our wake time
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);

		Toast.makeText(getActivity(), "Alarm set for " + sdf.format(alarmTime.getTime()), Toast.LENGTH_SHORT).show();
		Log.d("Accelerometer Service", " Should Start with Sleep Id" + Integer.toString(sleepId));

		// This intent will then start the accelerometer service which will read the required data and commit it to DB.
		Intent goToAccel = new Intent(getActivity(), AccelerometerService.class);
		goToAccel.putExtra("sleepId", sleepId);
		getActivity().startService(goToAccel);
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void setUpTrafficService(){

		if(!isTimeSet){
			Toast.makeText(getActivity(), "No Alarm Set!", Toast.LENGTH_SHORT).show();
			return;
		}

		alarmTime = checkDifference(alarmTime);
		wkUpServiceTime = checkDifference(wkUpServiceTime);

		// This intent will send the required data to the wake up receiver class
		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		intent.setAction(WakeUpReceiver.TRAFFIC);

		intent.putExtra("from", fromA);
		intent.putExtra("to", toB);
		intent.putExtra("time", time);
		intent.putExtra("id", Integer.toString(sleepId));
		intent.putExtra("wake_time", alarmTime);
		intent.putExtra("routines", getRoutineTime());

		// This pending intent and alarm will set the intent above to go off at wake up time - timeframe
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

		// Once it goes off, I set it to repeat every 2 minutes until alarm is cancelled.
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, wkUpServiceTime.getTimeInMillis(), POLLING_TIME, pendingIntent);

		Log.d("ALARM:", Long.toString(wkUpServiceTime.getTimeInMillis()));
		Log.d("Traffic Service", "should start at " + sdf.format(wkUpServiceTime.getTime()));
	}

	// Doesn't use Maps or Traffic Data
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void setUpWakeService(){

		if(!isTimeSet){
			showNoTimeSet();
			return;
		}

		alarmTime = checkDifference(alarmTime);
		wkUpServiceTime = checkDifference(wkUpServiceTime);

		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		intent.setAction(WakeUpReceiver.WAKEUP);
		intent.putExtra("id", Integer.toString(sleepId));
		intent.putExtra("routines", getRoutineTime());
		intent.putExtra("wake_time", alarmTime);
		Log.d("ROUTINES:", Integer.toString(getRoutineTime()));

		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);


		// Set an inexact repeating alarm that goes off at *timeframe* mins before alarm goes off every 1 minute repeats
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, wkUpServiceTime.getTimeInMillis(), POLLING_TIME_NO_TRAFFIC, pendingIntent);
		Log.d("Wake up Service", "should start at " + sdf.format(wkUpServiceTime.getTime()));
		Log.d("Wake up Service", "should start at " + dateFormat.format(wkUpServiceTime.getTime()));
	}

	private void cancelAlarm(){
		// Cancel all alarms - normal, movement based, and traffic based
		cancelWkAlarm();
		cancelWakeService();
		cancelTrafficService();

		// This method checks to see if my service is running on the device. If it is, I know to stop
		// the ringtone alarm and vibrations etc.
		if(Utils.isMyServiceRunning(AlarmSound.class, getActivity())) {
			Log.d("Alarm sound", " is running... stopping");
			Intent stopAlarm = new Intent(getActivity(), AlarmSound.class);
			getActivity().stopService(stopAlarm);
		}
	}

	// These 3 methods are described in detail in the documentation - please consult that

	private void cancelWkAlarm(){
		Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();

		Intent stopAccel = new Intent(getActivity(), AccelerometerService.class);
		getActivity().stopService(stopAccel);

		Toast.makeText(getActivity(), "Alarm Cancelled!", Toast.LENGTH_SHORT).show();
	}

	private void cancelWakeService() {
		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		intent.setAction(WakeUpReceiver.WAKEUP);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();
	}

	private void cancelTrafficService(){
		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		intent.setAction(WakeUpReceiver.TRAFFIC);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();
	}

	private void showCancelConfirmation()  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Stop Alarm?");
		builder.setMessage("Are you sure you want to stop the alarm?");

		builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				cancelAlarm();
			}
		});

		builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		final AlertDialog dialog = builder.show();

	}

	private void showNoTimeSet()  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("No Time Set!");
		builder.setMessage("You haven't set a time yet, please specify a time to wake up before setting the alarm.");

		builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});

		builder.show();
	}

	// This method makes sure that all "past" times are future times. This stops alarms going off
	// immediately because the system thought they were set for earlier in the current day, rather than the next day
	private Calendar checkDifference(Calendar alarmTime){
		long differenceInTime = Calendar.getInstance().getTimeInMillis() - alarmTime.getTimeInMillis();
		if(differenceInTime > 0){
			alarmTime.add(Calendar.HOUR_OF_DAY, 24);
		}
		return alarmTime;
	}

	// Get the cumulative time for all selected routines the user specified
	private int getRoutineTime(){
		if(routinesChecked.size() > 0){
			for (int id : routinesChecked){
				Routine selectedRoutines = realm.where(Routine.class).equalTo("id", id).findFirst();
				routineTime += Integer.parseInt(selectedRoutines.getDesc());
			}
			return routineTime;
		}

		return 0;
	}

	// This method allows the app to get results from the user inputting routines and maps data
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){

		// Routine Activity
		if(requestCode == 1) {

			// User didn't do anything in the activity
			if(data == null){
				Log.d("BACK BUTTON", "pressed");
				setRoutineCheckboxes(false);
				return;
			}

			routinesChecked = (HashSet) data.getSerializableExtra("routineData");
			if(routinesChecked.size() == 0){
				Log.d("Data", "didn't select routines");
				setRoutineCheckboxes(false);
			} else {
				Log.d("Data in Alarm", routinesChecked.toString());
				setRoutineCheckboxes(true);
			}
		}

		// Maps Activity
		else if(requestCode == 2) {
			if(data == null){
				Log.d("BACK BUTTON", "pressed");
				setTrafficCheckboxes(false);
				isMaps = false;
				return;
			}

			fromA = data.getStringExtra("from");
			toB = data.getStringExtra("to");
			time = data.getStringExtra("time");
			Log.d("Data in Maps", fromA);
			Log.d("Data in Maps", toB);
			Log.d("Data in Maps", time);
			isMaps = true;
			setTrafficCheckboxes(true);
		}
	}

	// Methods to set the check boxes depending on user input
	private void setRoutineCheckboxes(Boolean state){
		if(state){
			routineCheck.setChecked(true);
		} else routineCheck.setChecked(false);
	}

	private void setTrafficCheckboxes(Boolean state){
		if(state){
			trafficCheck.setChecked(true);
		} else trafficCheck.setChecked(false);
	}
}
