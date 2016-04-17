package me.adamoflynn.dynalarm.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;

import java.util.Calendar;
import java.util.Date;

import me.adamoflynn.dynalarm.services.TrafficService;
import me.adamoflynn.dynalarm.services.WakeUpService;

/**
 * Created by Adam on 12/04/2016.
 */
public class WakeUpReceiver extends WakefulBroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String from = intent.getStringExtra("from");
		String to = intent.getStringExtra("to");
		String time = intent.getStringExtra("time");
		String id = intent.getStringExtra("id");
		Calendar wake_time = (Calendar) intent.getSerializableExtra("wake_time");
		Intent trafficIntent = new Intent(context, TrafficService.class);

		trafficIntent.putExtra("from", from);
		trafficIntent.putExtra("to", to);
		trafficIntent.putExtra("time", time);
		trafficIntent.putExtra("id", id);
		trafficIntent.putExtra("wake_time", wake_time);
		startWakefulService(context, trafficIntent);

		Log.d("Traffic", "started service at " + new Date(System.currentTimeMillis()) + " with ID" + id);
	}
}
