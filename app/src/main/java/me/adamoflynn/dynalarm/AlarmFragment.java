package me.adamoflynn.dynalarm;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

import io.realm.Realm;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.receivers.AlarmReceiver;
import me.adamoflynn.dynalarm.receivers.WakeUpReceiver;
import me.adamoflynn.dynalarm.services.AccelerometerService;
import me.adamoflynn.dynalarm.services.AlarmSound;

public class AlarmFragment extends Fragment implements View.OnClickListener {

	private Button start, accel, stop, maps;
	private TextView currentTime, wakeUpTime;
	private CheckBox routineCheck, trafficCheck;

	private AlarmManager alarmManager;
	private Calendar alarmTime = Calendar.getInstance();
	private Calendar wkUpServiceTime = Calendar.getInstance();
	private Calendar timeSet = Calendar.getInstance();
	private final DateFormat sdf = new SimpleDateFormat("HH:mm");
	private HashSet<Integer> routinesChecked;
	private String fromA, toB, time;
	private long timeframe = 20 * 60 * 1000;
	private boolean isTimeSet, isMaps = false;
	private final long POLLING_TIME = 120000;
	private int sleepId;


	public AlarmFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_alarm, container, false);

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
		start = (Button) v.findViewById(R.id.start);
		start.setOnClickListener(this);

		accel = (Button) v.findViewById(R.id.routines);
		accel.setOnClickListener(this);

		stop = (Button) v.findViewById(R.id.stop);
		stop.setOnClickListener(this);

		maps = (Button) v.findViewById(R.id.mapsButton);
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
				startAlarm();
				break;
			case R.id.stop:
				cancelAlarm();
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

	private void startAlarm(){
		setWakeUpAlarm();
		if(isMaps){
			setUpWakeService();
		}
		timeSet = Calendar.getInstance();
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
	private void setUpWakeService(){

		if(!isTimeSet){
			Toast.makeText(getActivity(), "No Alarm Set!", Toast.LENGTH_SHORT).show();
			return;
		}

		checkDifference();

		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		intent.putExtra("from", fromA);
		intent.putExtra("to", toB);
		intent.putExtra("time", time);
		intent.putExtra("id", Integer.toString(sleepId));
		intent.putExtra("wake_time", alarmTime);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);

		// Set an inexact repeating alarm that goes off at *timeframe* mins before alarm goes off every 2 minutes repeats
		alarmManager.setInexactRepeating(AlarmManager.RTC_WAKEUP, wkUpServiceTime.getTimeInMillis(), POLLING_TIME, pendingIntent);
		Log.d("Wake up Service", "should start at " + sdf.format(wkUpServiceTime.getTime()));
	}


	private void cancelAlarm(){
		cancelWkAlarm();
		cancelWkService();

		if(isMyServiceRunning(AlarmSound.class)) {
			Log.d("Alarm sound", " is running... stopping");
			Intent stopAlarm = new Intent(getActivity(), AlarmSound.class);
			getActivity().stopService(stopAlarm);
		}
	}


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

	private void cancelWkService(){
		Intent intent = new Intent(getActivity().getApplicationContext(), WakeUpReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity().getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
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


	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data){
		if(requestCode == 1) {

			if(data == null){
				Log.d("Back button", "pressed");
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
				Log.d("Back button", "pressed");
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
