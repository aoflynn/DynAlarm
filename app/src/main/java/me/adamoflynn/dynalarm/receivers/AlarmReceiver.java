package me.adamoflynn.dynalarm.receivers;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import me.adamoflynn.dynalarm.AlarmFragment;
import me.adamoflynn.dynalarm.MainActivity;
import me.adamoflynn.dynalarm.R;
import me.adamoflynn.dynalarm.services.AccelerometerService;
import me.adamoflynn.dynalarm.services.AlarmSound;

/**
 * Created by Adam on 20/03/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

	private final long SNOOZE_TIME = 60000;

	@Override
	public void onReceive(Context context, Intent intent) {
		/*Toast.makeText(context, "Alarm!", Toast.LENGTH_LONG).show();
		Uri alarmUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);

		long[] vib = {600,600,600, 200, 200, 900, 200};*/

		Intent intentToActivity = new Intent(context, MainActivity.class);
		intentToActivity.putExtra("isAlarmRinging", true);
		intentToActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 101, intentToActivity, 0);

		Notification not = new Notification.Builder(context)
		  .setContentTitle("Alarm! Wake Up!")
			.setContentText("Click here to go to Alarm.")
			.setContentIntent(pendingIntent)
			.setSmallIcon(R.drawable.ic_alarm_white_48dp)
			.setAutoCancel(false)
		//	.setSound(alarmUri).setVibrate(vib)
			.build();

		//not.defaults |= Notification.DEFAULT_VIBRATE;

		Intent startAlarmSound = new Intent(context, AlarmSound.class);
		context.startService(startAlarmSound);
		/*Intent goToAccel = new Intent(context, AccelerometerService.class);
		context.stopService(goToAccel);

		cancelAlarms(context);
	*/

		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, not);

		//Log.d("End of alarm recevier"," should be cancel/called");
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
