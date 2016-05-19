package me.adamoflynn.dynalarm;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
/*import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;*/
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.widget.Toast;


import java.util.Calendar;

import io.realm.Realm;
import me.adamoflynn.dynalarm.adapters.ViewPagerAdapter;
import me.adamoflynn.dynalarm.receivers.AlarmReceiver;
import me.adamoflynn.dynalarm.receivers.WakeUpReceiver;
import me.adamoflynn.dynalarm.services.AccelerometerService;
import me.adamoflynn.dynalarm.services.AlarmSound;
import me.adamoflynn.dynalarm.services.TrafficService;
import me.adamoflynn.dynalarm.services.WakeUpService;
import me.adamoflynn.dynalarm.utils.Utils;

public class MainActivity extends AppCompatActivity {

	private TabLayout tabLayout;
	private Realm db;
	private Context context;
	private boolean isAlarm = false;
	private boolean isDialogShowing = false;
	private SharedPreferences prefs;
	private AlertDialog alertDialog = null;

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
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Toolbar tb = (Toolbar) findViewById(R.id.toolbar);


		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		setupViewPager(viewPager);

		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		setupTabIcons();

	}

	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		isAlarm = intent.getBooleanExtra("isAlarmRinging", false);
		if(Utils.isMyServiceRunning(AlarmSound.class, context)){
			showAlarmDialog();
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
		if(Utils.isMyServiceRunning(AlarmSound.class, context) && isDialogShowing == false){
			Log.d("Running: ", "TRUE");
			showAlarmDialog();
		}
	}

	@Override
	protected void onRestart() {
		super.onRestart();
		Log.d("On restart", "true");
		if(Utils.isMyServiceRunning(AlarmSound.class, context) && isDialogShowing == false){
			Log.d("Running: ", "TRUE");
			showAlarmDialog();
		}
	}

	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("On destroy", "true");
		if (db != null) {
			db.close();
			db = null;
		}
	}

	private void showAlarmDialog(){
			isDialogShowing = true;
			final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
			builder.setTitle("Create A Routine");

			builder.setMessage("Wake Up!");
			builder.setIcon(R.drawable.ic_alarm_white_48dp);
			builder.setCancelable(false);
			builder.setTitle("DynAlarm");

			builder.setPositiveButton("Stop Alarm", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					cancelAlarm();
					NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.cancel(0);
					isAlarm = false;
					isDialogShowing = false;
					dismissAlarmDialog();
				}
			});

			builder.setNegativeButton("Snooze Alarm", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(context, AlarmSound.class);
					context.stopService(i);
					snoozeAlarms(context);
					NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.cancel(0);
					isDialogShowing = false;
					dismissAlarmDialog();
				}
			});

			alertDialog = builder.show();
	}

	private void dismissAlarmDialog() {
		alertDialog.dismiss();
	}

	private void cancelAlarm(){
		cancelWkAlarm();
		cancelWakeService();
		cancelTrafficService();

		if(Utils.isMyServiceRunning(AlarmSound.class, context)) {
			Log.d("Alarm sound", " is running... stopping");
			Intent stopAlarm = new Intent(context, AlarmSound.class);
			context.stopService(stopAlarm);
		}
	}

	private void cancelWkAlarm(){
		Intent intent = new Intent(this, AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();

		Intent stopAccel = new Intent(context, AccelerometerService.class);
		context.stopService(stopAccel);

		Toast.makeText(context, "Alarm Cancelled!", Toast.LENGTH_SHORT).show();
	}

	private void cancelWakeService() {
		Log.d("WAKE", " trying to cancel...");
		Intent intent = new Intent(getApplicationContext(), WakeUpReceiver.class);

		intent.setAction(WakeUpReceiver.WAKEUP);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();
	}

	private void cancelTrafficService(){
		Log.d("TRAFFIC", " trying to cancel...");
		Intent intent = new Intent(getApplicationContext(), WakeUpReceiver.class);
		intent.setAction(WakeUpReceiver.TRAFFIC);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 369, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		pendingIntent.cancel();
	}

	@TargetApi(Build.VERSION_CODES.KITKAT)
	private void snoozeAlarms(Context context){
		Intent intent = new Intent(getApplicationContext(), AlarmReceiver.class);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 123, intent, PendingIntent.FLAG_UPDATE_CURRENT);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    int snoozePrefs = Integer.parseInt(prefs.getString("snoozeTime", "1"));
		Log.d("SNOOZE", Integer.toString(snoozePrefs));
		long SNOOZE_TIME = 60000 * snoozePrefs;
		alarmManager.setExact(AlarmManager.RTC_WAKEUP, Calendar.getInstance().getTimeInMillis() + SNOOZE_TIME, pendingIntent);
	}
}

