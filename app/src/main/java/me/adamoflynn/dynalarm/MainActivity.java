package me.adamoflynn.dynalarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
/*import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;*/
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;


import java.util.Calendar;

import io.realm.Realm;
import me.adamoflynn.dynalarm.adapters.ViewPagerAdapter;
import me.adamoflynn.dynalarm.model.Routine;
import me.adamoflynn.dynalarm.receivers.AlarmReceiver;
import me.adamoflynn.dynalarm.receivers.WakeUpReceiver;
import me.adamoflynn.dynalarm.services.AccelerometerService;
import me.adamoflynn.dynalarm.services.AlarmSound;
import me.adamoflynn.dynalarm.utils.RoutineOnItemSelectedListener;

public class MainActivity extends AppCompatActivity {

	private Toolbar tb;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private Realm db;
	private final long SNOOZE_TIME = 60000;
	private Context context;

	private int[] tabsIcons = {
		R.drawable.ic_alarm_white_48dp,
		R.drawable.ic_timeline_white_48dp,
		R.drawable.ic_settings_white_48dp
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		context = getApplicationContext();

		onNewIntent(getIntent());

		db = Realm.getDefaultInstance();

		tb = (Toolbar) findViewById(R.id.toolbar);


		//getSupportActionBar().hide();

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		setupViewPager(viewPager);

		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		setupTabIcons();

	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		boolean isAlarm = intent.getBooleanExtra("isAlarmRinging", false);
		if(isAlarm){
			final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Create A Routine");

			LayoutInflater li = LayoutInflater.from(this);
			//View dialogView = li.inflate(R.layout.alarm, null);

			builder.setMessage("Wake Up!");
			builder.setIcon(R.drawable.ic_alarm_white_48dp);
			builder.setCancelable(false);
			builder.setTitle("DynAlarm");

			//builder.setView(dialogView);
			builder.setPositiveButton("Stop Alarm", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {

					Intent stopAlarm = new Intent(context, AlarmSound.class);
					context.stopService(stopAlarm);

					Intent goToAccel = new Intent(context, AccelerometerService.class);
					context.stopService(goToAccel);

					cancelAlarms(context);

					NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.cancel(0);

					Log.d("End of alarm recevier", " should be cancel/called");
				}
			});

			builder.setNegativeButton("Snooze Alarm", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Log.d("Snooze", " like a bitch");
					Intent i = new Intent(context, AlarmSound.class);
					context.stopService(i);
					snoozeAlarms(context);
				}
			});

			final AlertDialog dialog = builder.show();
		}
	}

	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getFragmentManager());
		adapter.addFragment(new AlarmFragment());
		adapter.addFragment(new AnalysisFragment());
		adapter.addFragment(new SettingsFragment());
		viewPager.setAdapter(adapter);
	}

	private void setupTabIcons(){
		tabLayout.getTabAt(0).setIcon(tabsIcons[0]);
		tabLayout.getTabAt(1).setIcon(tabsIcons[1]);
		tabLayout.getTabAt(2).setIcon(tabsIcons[2]);
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

		if (db != null) {
			db.close();
			db = null;
		}
	}

	private void cancelAlarms(Context context){

		Intent intent = new Intent(context, WakeUpReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();

		Toast.makeText(context, "Recurring Alarm Cancelled!", Toast.LENGTH_SHORT).show();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void snoozeAlarms(Context context){
		Intent intent = new Intent(context, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + SNOOZE_TIME, pendingIntent);
		Log.d("Alarm set", "for 1 min in future");
	}

}

