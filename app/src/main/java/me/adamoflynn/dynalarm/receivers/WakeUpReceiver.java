package me.adamoflynn.dynalarm.receivers;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.Date;

import me.adamoflynn.dynalarm.services.TrafficService;

/**
 * Created by Adam on 12/04/2016.
 */
public class WakeUpReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(Context context, Intent intent) {
		String from = intent.getStringExtra("from");
		String to = intent.getStringExtra("to");
		String time = intent.getStringExtra("time");
		Intent trafficIntent = new Intent(context, TrafficService.class);

		trafficIntent.putExtra("from", from);
		trafficIntent.putExtra("to", to);
		trafficIntent.putExtra("time", time);
		context.startService(trafficIntent);

		Log.d("Traffic", "started service at" + new Date(System.currentTimeMillis()));
	}
}
