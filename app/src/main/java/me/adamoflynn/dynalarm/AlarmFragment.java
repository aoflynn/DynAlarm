package me.adamoflynn.dynalarm;

import android.annotation.TargetApi;
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
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import me.adamoflynn.dynalarm.receivers.AlarmReceiver;
import me.adamoflynn.dynalarm.services.AccelerometerService;

public class AlarmFragment extends Fragment implements View.OnClickListener {

	private Button alarm, start, accel, cancel;
	private TextView currentTime;
	private Date curTime;

	private AlarmManager alarmManager;
	private Calendar alarmTime = Calendar.getInstance();


	public AlarmFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_alarm, container, false);

		initializeTime(v);
		initializeButtons(v);

		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	private void initializeTime(View v){
		currentTime = (TextView) v.findViewById(R.id.time);
		curTime = Calendar.getInstance().getTime();
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
		String time = df.format(curTime);
		currentTime.setText(time);
	}

	private void initializeButtons(View v){
		alarm = (Button) v.findViewById(R.id.alarm);
		alarm.setOnClickListener(this);
		start = (Button) v.findViewById(R.id.start);
		start.setOnClickListener(this);
		accel = (Button) v.findViewById(R.id.accelButton);
		accel.setOnClickListener(this);
		cancel = (Button) v.findViewById(R.id.cancel);
		cancel.setOnClickListener(this);
	}

	public void onClick(View v){
		switch(v.getId()){
			case R.id.alarm:
				Log.d("Pressed: ", " alarm button");
				timePicker();
				break;
			case R.id.start:
				Log.d("Pressed: ", " start button");
				startAlarm();
				break;
			case R.id.accelButton:
				Intent intent = new Intent(getActivity(), RoutineActivity.class);
				getActivity().startActivity(intent);
				break;
			case R.id.cancel:
				cancelAlarm();
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
				currentTime.setText(hourOfDay + ":" + minute);
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
		//pendingIntent.cancel();
		alarmManager = (AlarmManager) getActivity().getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();

		Intent stopAccel = new Intent(getActivity(), AccelerometerService.class);
		getActivity().stopService(stopAccel);
		Log.d("Service? ", " Should Cancel");

		Toast.makeText(getActivity(), "Alarm Cancelled!", Toast.LENGTH_SHORT).show();
	}

	private void checkDifference(){
		long differenceInTime = Calendar.getInstance().getTimeInMillis() - alarmTime.getTimeInMillis();
		Log.d("Time diff", Long.toString(differenceInTime));
		if(differenceInTime > 0){
			alarmTime.add(Calendar.HOUR_OF_DAY, 24);
			long dif = Calendar.getInstance().getTimeInMillis() - alarmTime.getTimeInMillis();
			Log.d("Time diff after", Long.toString(dif));
		}
	}
}
