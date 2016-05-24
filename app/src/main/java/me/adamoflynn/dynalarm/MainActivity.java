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
import me.adamoflynn.dynalarm.utils.Utils;


public class MainActivity extends AppCompatActivity {

	private TabLayout tabLayout;
	private Realm db;
	private Context context;
	private boolean isAlarm = false;
	private boolean isDialogShowing = false;
	private SharedPreferences prefs;
	private AlertDialog alertDialog = null;

	// Initialise tab icons
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

		// If i receive a new intent, go to this method
		onNewIntent(getIntent());

		// DB and prefs connections
		db = Realm.getDefaultInstance();
		prefs = PreferenceManager.getDefaultSharedPreferences(this);

		Toolbar tb = (Toolbar) findViewById(R.id.toolbar);

		// Implement the tab layout by populating the adapter with the required fragments
		ViewPager viewPager = (ViewPager) findViewById(R.id.viewpager);
		setupViewPager(viewPager);

		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		setupTabIcons();

	}


	// If intent is sent and the alarm sound is ringing, show cancel and snooze dialog
	@Override
	protected void onNewIntent(Intent intent){
		super.onNewIntent(intent);
		isAlarm = intent.getBooleanExtra("isAlarmRinging", false);
		if(Utils.isMyServiceRunning(AlarmSound.class, context)){
			showAlarmDialog();
		}
	}

	// Add frags to swipe view
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

	// If alarm sound service is running, show the dialog providing its not already showing.
	@Override
	protected void onResume() {
		super.onResume();
		if(Utils.isMyServiceRunning(AlarmSound.class, context) && isDialogShowing == false){
			Log.d("Running: ", "TRUE");
			showAlarmDialog();
		}
	}

	// If alarm sound service is running, show the dialog providing its not already showing.
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

	// Close DB
	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.d("On destroy", "true");
		if (db != null) {
			db.close();
			db = null;
		}
	}

	// This dialog prompts the user to cancel or snooze an alarm if the alarm sound is goign off
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
					cancelAlarm(); // Cancel Alarms
					NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE); // Remove notification
					notificationManager.cancel(0);
					isAlarm = false;
					isDialogShowing = false;
					dismissAlarmDialog();
				}
			});

			// Snooze alarm - i.e. set alarm for user snooze value in future
			builder.setNegativeButton("Snooze Alarm", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					Intent i = new Intent(context, AlarmSound.class);
					context.stopService(i); // stop alarm ringing for now
					snoozeAlarms(context); // snooze
					NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.cancel(0); // get rid of the alarm notification keep the persistent one
					isDialogShowing = false;
					dismissAlarmDialog();
				}
			});

			alertDialog = builder.show();
	}

	private void dismissAlarmDialog() {
		alertDialog.dismiss();
	}

	// Cancel all alarms in the application
	private void cancelAlarm(){
		cancelWkAlarm();
		cancelWakeService();
		cancelTrafficService();

		// If alarm sound running, stop it
		if(Utils.isMyServiceRunning(AlarmSound.class, context)) {
			Log.d("Alarm sound", " is running... stopping");
			Intent stopAlarm = new Intent(context, AlarmSound.class);
			context.stopService(stopAlarm);
		}
	}

	/**
	 *  These three cancel alarms methods are explained very well in the documentation
	 *  Basis is: have to recreate exact same intent used to intially schedule alarm to cancel it
	 */

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

	// update the alarm to the user defined snooze time in the future
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

