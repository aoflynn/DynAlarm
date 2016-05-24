package me.adamoflynn.dynalarm.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import me.adamoflynn.dynalarm.MainActivity;
import me.adamoflynn.dynalarm.R;
import me.adamoflynn.dynalarm.services.AlarmSound;

/**
 * This alarm receiver, when triggerd by the base alarm, will start the alarm sound service which
 * will wake the user up with noise and vibrations
 */
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {

		// Set message in notification to give user a reason why they were woken up.
		String reasonForWaking = intent.getStringExtra("MESSAGE");

		// Create a content intent that will go to the main activity, i.e. the alarm fragment.
		// when a user clicks the notification. The flags specify to clear the activity stack
		// so users don't get into a loop when pressed the back button
		Intent intentToActivity = new Intent(context, MainActivity.class);
		intentToActivity.putExtra("isAlarmRinging", true);
		intentToActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 101, intentToActivity, PendingIntent.FLAG_UPDATE_CURRENT);

		// Build a notification to display
		Notification not = new Notification.Builder(context)
		  .setContentTitle("DynAlarm")
			.setContentText("Alarm! Wake Up!")
			.setContentInfo(reasonForWaking + "\nClick here to go to Alarm.")
			.setContentIntent(pendingIntent)
			.setSmallIcon(R.drawable.ic_alarm_white_48dp)
			.setAutoCancel(false)
			.build();


		// Start the alarm sound service
		Intent startAlarmSound = new Intent(context, AlarmSound.class);
		context.startService(startAlarmSound);

		// Display the notification
		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, not);
	}

}
