package me.adamoflynn.dynalarm.receivers;

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

	private final String WAKEUP = "me.adamoflynn.dynalarm.action.WAKEUP";
	private final String TRAFFIC = "me.adamoflynn.dynalarm.action.TRAFFIC";
	@Override
	public void onReceive(Context context, Intent intent) {
		String action = intent.getAction();
		if(action.equals(WAKEUP)){
			String id = intent.getStringExtra("id");
			int routineTime = intent.getIntExtra("routines", 0);
			Calendar wake_time = (Calendar) intent.getSerializableExtra("wake_time");

			Intent wakeUpIntent = new Intent(context, WakeUpService.class);
			wakeUpIntent.putExtra("id", id);
			wakeUpIntent.putExtra("routines", routineTime);
			wakeUpIntent.putExtra("wake_time", wake_time);
			startWakefulService(context, wakeUpIntent);
			Log.d("Wakeup", "started service at " + new Date(System.currentTimeMillis()) + " with ID" + id);
		}

		else if(action.equals(TRAFFIC)){
			String from = intent.getStringExtra("from");
			String to = intent.getStringExtra("to");
			String time = intent.getStringExtra("time");
			String id = intent.getStringExtra("id");
			Calendar wake_time = (Calendar) intent.getSerializableExtra("wake_time");
			int routineTime = intent.getIntExtra("routines", 0);

			Intent trafficIntent = new Intent(context, TrafficService.class);
			trafficIntent.putExtra("from", from);
			trafficIntent.putExtra("to", to);
			trafficIntent.putExtra("time", time);
			trafficIntent.putExtra("id", id);
			trafficIntent.putExtra("routines", routineTime);
			trafficIntent.putExtra("wake_time", wake_time);
			startWakefulService(context, trafficIntent);
			Log.d("Traffic", "started service at " + new Date(System.currentTimeMillis()) + " with ID" + id);
		}
	}
}
