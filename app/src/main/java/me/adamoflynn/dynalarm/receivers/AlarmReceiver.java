package me.adamoflynn.dynalarm.receivers;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

import me.adamoflynn.dynalarm.MainActivity;
import me.adamoflynn.dynalarm.R;
import me.adamoflynn.dynalarm.services.AlarmSound;

/**
 * Created by Adam on 20/03/2016.
 */
public class AlarmReceiver extends BroadcastReceiver {

	private final long SNOOZE_TIME = 60000;

	@Override
	public void onReceive(Context context, Intent intent) {

		Intent intentToActivity = new Intent(context, MainActivity.class);
		intentToActivity.putExtra("isAlarmRinging", true);
		intentToActivity.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent pendingIntent = PendingIntent.getActivity(context, 101, intentToActivity, PendingIntent.FLAG_UPDATE_CURRENT);

		Notification not = new Notification.Builder(context)
		  .setContentTitle("Alarm! Wake Up!")
			.setContentText("Click here to go to Alarm.")
			.setContentIntent(pendingIntent)
			.setSmallIcon(R.drawable.ic_alarm_white_48dp)
			.setAutoCancel(false)
			.build();


		Intent startAlarmSound = new Intent(context, AlarmSound.class);
		context.startService(startAlarmSound);


		NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		notificationManager.notify(0, not);
	}

}
