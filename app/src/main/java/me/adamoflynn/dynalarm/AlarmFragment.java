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
import android.os.Build;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.facebook.stetho.common.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import io.realm.Realm;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Routine;
import me.adamoflynn.dynalarm.receivers.AlarmReceiver;
import me.adamoflynn.dynalarm.receivers.WakeUpReceiver;
import me.adamoflynn.dynalarm.services.AccelerometerService;
import me.adamoflynn.dynalarm.services.AlarmSound;
import me.adamoflynn.dynalarm.services.TrafficService;
import me.adamoflynn.dynalarm.services.WakeUpService;
import me.adamoflynn.dynalarm.utils.Utils;

public class AlarmFragment extends Fragment implements View.OnClickListener {

	private TextView currentTime, wakeUpTime;
	private CheckBox routineCheck, trafficCheck;

	private AlarmManager alarmManager;
	private Calendar alarmTime = Calendar.getInstance();
	private Calendar wkUpServiceTime = Calendar.getInstance();
	private Calendar timeSet = Calendar.getInstance();
	private final DateFormat sdf = new SimpleDateFormat("HH:mm");
	private HashSet<Integer> routinesChecked = new HashSet<>();
	private String fromA, toB, time;
	private long timeframe = 20 * 60 * 1000;
	private boolean isTimeSet, isMaps = false;
	private final long POLLING_TIME = 120000;
	private int sleepId;
	private int routineTime = 0;
	private Realm realm;


	public AlarmFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_alarm, container, false);
		realm = Realm.getDefaultInstance();
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

	private void initializeTime(View v){
		currentTime = (TextView) v.findViewById(R.id.time);
		wakeUpTime = (TextView) v.findViewById(R.id.wakeUp);
		Date curTime = Calendar.getInstance().getTime();

		wkUpServiceTime.setTimeInMillis(curTime.getTime() - timeframe);

		currentTime.setText(sdf.format(curTime));
		wakeUpTime.setText("Wake up between " + sdf.format(wkUpServiceTime.getTime().getTime()) + " and " + sdf.format(curTime));
		currentTime.setOnClickListener(this);
	}

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

	public void onClick(View v){
		switch(v.getId()){
			case R.id.time:
				timePicker();
				break;
			case R.id.start:
				if(!Utils.isMyServiceRunning(AccelerometerService.class, getActivity())){
					timeSet = Calendar.getInstance();
					showAlarmConfirmation();
				} else Toast.makeText(getActivity(), "Alarm currently on, please cancel it to set another alarm.", Toast.LENGTH_LONG).show();
				break;
			case R.id.stop:
				if(Utils.isMyServiceRunning(AccelerometerService.class, getActivity())){
					showCancelConfirmation();
				} else Toast.makeText(getActivity(), "No alarm currently set.", Toast.LENGTH_LONG).show();
				break;
			case R.id.routines:
				Intent routines = new Intent(getActivity(), RoutineActivity.class);
				startActivityForResult(routines, 1);
				break;
			case R.id.mapsButton:
				Intent maps = new Intent(getActivity(), MapsActivity.class);
				startActivityForResult(maps, 2);
				break;
		}
	}

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
					wkUpServiceTime.setTimeInMillis(alarmTime.getTimeInMillis() - timeframe);
					wakeUpTime.setText("Wake up between " + sdf.format(wkUpServiceTime.getTime()) + " and " + sdf.format(alarmTime.getTime()));
					isTimeSet = true;
				}
			}, hour, minute, true);
		pickerDialog.setTitle("Select Time");
		pickerDialog.show();
	}


	private void showAlarmConfirmation()  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("Set Alarm");

		LayoutInflater li = LayoutInflater.from(getActivity());
		View dialogView = li.inflate(R.layout.confirm_alarm, null);
		final TextView time = (TextView) dialogView.findViewById(R.id.time);
		final TextView timeframe = (TextView) dialogView.findViewById(R.id.timeframe);
		final TextView options = (TextView) dialogView.findViewById(R.id.options);
		time.setText(sdf.format(timeSet.getTime()) + " to " + sdf.format(alarmTime.getTime()));
		timeframe.setText("with a wake timeframe of 20 minutes");

		final boolean usingMaps = isMaps && trafficCheck.isChecked();
		final boolean usingRoutines = routinesChecked.size() > 0 && routineCheck.isChecked();

		if(usingMaps && usingRoutines) {
			options.setText("You are using journey, routine, and sleep data to wake up.");
		} else if (usingMaps) {
			options.setText("You are only using journey and sleep data to wake up.");
		} else if (usingRoutines) {
			options.setText("You are only using routine and sleep data to wake up.");
		} else {
			options.setText("You are only using sleep data to wake up.");
		}


		builder.setView(dialogView);
		builder.setPositiveButton("Set Alarm", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				setWakeUpAlarm();
				if (usingMaps) {
					setUpTrafficService();
				} else {
					setUpWakeService();
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
		Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

		if(!isTimeSet){
			Toast.makeText(getActivity(), "No Alarm Set!", Toast.LENGTH_SHORT).show();
			return;
		}

		checkDifference();
		Realm realm = Realm.getDefaultInstance();
		Number newestData = realm.where(AccelerometerData.class).max("sleepId");
		sleepId = newestData.intValue();
		sleepId += 1;

		alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.getTimeInMillis(), pendingIntent);
		Toast.makeText(getActivity(), "Alarm set!", Toast.LENGTH_SHORT).show();
		Log.d("Service? ", " Should Start with Sleep Id" + Integer.toString(sleepId));
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

		checkDifference();

		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		String TRAFFIC = "me.adamoflynn.dynalarm.action.TRAFFIC";
		intent.setAction(TRAFFIC);
		intent.putExtra("from", fromA);
		intent.putExtra("to", toB);
		intent.putExtra("time", time);
		intent.putExtra("id", Integer.toString(sleepId));
		intent.putExtra("wake_time", alarmTime);

		if(routinesChecked.size() > 0) {
			intent.putExtra("routines", getRoutineTime());
		} else intent.putExtra("routines", 0);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

		// Set an inexact repeating alarm that goes off at *timeframe* mins before alarm goes off every 2 minutes repeats
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, wkUpServiceTime.getTimeInMillis(), POLLING_TIME, pendingIntent);
		Log.d("Traffic Service", "should start at " + sdf.format(wkUpServiceTime.getTime()));
	}

	// Doesn't use Maps or Traffic Data
	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void setUpWakeService(){

		if(!isTimeSet){
			Toast.makeText(getActivity(), "No Alarm Set!", Toast.LENGTH_SHORT).show();
			return;
		}

		checkDifference();

		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		String WAKEUP = "me.adamoflynn.dynalarm.action.WAKEUP";
		intent.setAction(WAKEUP);
		intent.putExtra("id", Integer.toString(sleepId));
		intent.putExtra("routines", getRoutineTime());
		intent.putExtra("wake_time", alarmTime);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

		// Set an inexact repeating alarm that goes off at *timeframe* mins before alarm goes off every 2 minutes repeats
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, wkUpServiceTime.getTimeInMillis(), POLLING_TIME, pendingIntent);
		Log.d("Wake up Service", "should start at " + sdf.format(wkUpServiceTime.getTime()));
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


	private void cancelAlarm(){
		cancelWkAlarm();

		if(Utils.isMyServiceRunning(WakeUpService.class, getActivity())) cancelWakeService();
		else if(Utils.isMyServiceRunning(TrafficService.class, getActivity())) cancelTrafficService();

		if(Utils.isMyServiceRunning(AlarmSound.class, getActivity())) {
			Log.d("Alarm sound", " is running... stopping");
			Intent stopAlarm = new Intent(getActivity(), AlarmSound.class);
			getActivity().stopService(stopAlarm);
		}
	}

	private void cancelWkAlarm(){
		Intent intent = new Intent(getActivity().getApplicationContext(), AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 123, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();

		Intent stopAccel = new Intent(getActivity(), AccelerometerService.class);
		getActivity().stopService(stopAccel);

		Toast.makeText(getActivity(), "Alarm Cancelled!", Toast.LENGTH_SHORT).show();
	}

	private void cancelWakeService() {
		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		String WAKEUP = "me.adamoflynn.dynalarm.action.WAKEUP";
		intent.setAction(WAKEUP);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();
	}

	private void cancelTrafficService(){
		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		String TRAFFIC = "me.adamoflynn.dynalarm.action.TRAFFIC";
		intent.setAction(TRAFFIC);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_CANCEL_CURRENT);
		AlarmManager alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();
		Toast.makeText(getActivity(), "Recurring Alarm Cancelled!", Toast.LENGTH_SHORT).show();
	}

	private void checkDifference(){
		long differenceInTime = Calendar.getInstance().getTimeInMillis() - alarmTime.getTimeInMillis();
		if(differenceInTime > 0){
			alarmTime.add(Calendar.HOUR_OF_DAY, 24);
		}
	}

	private int getRoutineTime(){
		int routineTime = 0;
		if(routinesChecked.size() > 0){
			for (int id : routinesChecked){
				Routine selectedRoutines = realm.where(Routine.class).equalTo("id", id).findFirst();
				routineTime += Integer.parseInt(selectedRoutines.getDesc());
			}
		} else {
			routineTime = 0;
		}

		return routineTime;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == 1) {

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
