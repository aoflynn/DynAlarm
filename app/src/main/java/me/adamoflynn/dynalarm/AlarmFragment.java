package me.adamoflynn.dynalarm;

import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import me.adamoflynn.dynalarm.receivers.AlarmReceiver;
import me.adamoflynn.dynalarm.services.AccelerometerService;
import me.adamoflynn.dynalarm.services.TrafficService;

public class AlarmFragment extends Fragment implements View.OnClickListener {

	private Button start, accel, cancel, maps;
	private TextView currentTime, routine, traffic;
	private CheckBox routineCheck, trafficCheck;

	private AlarmManager alarmManager;
	private Calendar alarmTime = Calendar.getInstance();
	private final DateFormat sdf = new SimpleDateFormat("HH:mm");
	private boolean wantRoutines, wantTraffic = false;


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

	private void initializeTime(View v){
		currentTime = (TextView) v.findViewById(R.id.time);
		Date curTime = Calendar.getInstance().getTime();

		currentTime.setText(sdf.format(curTime));
		currentTime.setOnClickListener(this);
	}

	private void initializeButtons(View v){
		start = (Button) v.findViewById(R.id.start);
		start.setOnClickListener(this);

		accel = (Button) v.findViewById(R.id.accelButton);
		accel.setOnClickListener(this);

		cancel = (Button) v.findViewById(R.id.cancel);
		cancel.setOnClickListener(this);

		maps = (Button) v.findViewById(R.id.mapsButton);
		maps.setOnClickListener(this);
	}

	private void initializeExtras(View v){
		routine = (TextView) v.findViewById(R.id.routine);
		routineCheck = (CheckBox) v.findViewById(R.id.routineCheck);


		traffic = (TextView) v.findViewById(R.id.traffic);
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
			case R.id.cancel:
				cancelAlarm();
				break;
			case R.id.accelButton:
				Intent intent = new Intent(getActivity(), RoutineActivity.class);
				getActivity().startActivity(intent);
				break;
			case R.id.mapsButton:
				Intent maps = new Intent(getActivity(), MapsActivity.class);
				getActivity().startActivity(maps);
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
				}
			}, hour, minute, true);
		pickerDialog.setTitle("Select Time");
		pickerDialog.show();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void startAlarm(){
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		Intent intent = new Intent(getActivity(), AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

		if(alarmTime==null){
			Toast.makeText(getActivity(), "No Alarm Set!", Toast.LENGTH_SHORT).show();
			return;
		}

		checkDifference();

		alarmManager.setExact(AlarmManager.RTC, alarmTime.getTimeInMillis(), pendingIntent);
		Toast.makeText(getActivity(), "Alarm set!", Toast.LENGTH_SHORT).show();
		Intent goToAccel = new Intent(getActivity(), AccelerometerService.class);
		getActivity().startService(goToAccel);
		Log.d("Service? ", " Should Start");
	}

	private void cancelAlarm(){
		Intent myIntent = new Intent(getActivity(), AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getActivity(), 0, myIntent, PendingIntent.FLAG_UPDATE_CURRENT);
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();

		Intent stopAccel = new Intent(getActivity(), AccelerometerService.class);
		getActivity().stopService(stopAccel);

		Toast.makeText(getActivity(), "Alarm Cancelled!", Toast.LENGTH_SHORT).show();
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

}
