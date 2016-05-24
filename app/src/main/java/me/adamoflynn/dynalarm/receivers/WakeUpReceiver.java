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
 * This wake up receiver, when triggered by the timeframe alarm, will send data to either of the#
 * specified wake up services.
 */
public class WakeUpReceiver extends WakefulBroadcastReceiver {


	// Good practice, keep actions final
	public static final String WAKEUP = "me.adamoflynn.dynalarm.action.WAKEUP";
	public static final String TRAFFIC = "me.adamoflynn.dynalarm.action.TRAFFIC";


	// When triggered, the receiver will go to this method with data i.e. the intent
	@Override
	public void onReceive(Context context, Intent intent) {

		// Get action to see what service we must awaken
		String action = intent.getAction();

		// If its WAKEUP, we should go to the wake up service that only analyses accelerometer data
		if(action.equals(WAKEUP)){

			//Send on the required data
			String id = intent.getStringExtra("id");
			int routineTime = intent.getIntExtra("routines", 0);
			Calendar wake_time = (Calendar) intent.getSerializableExtra("wake_time");

			// Start the service, by using a wakeful service call. This means the phone will be
			// awoken if it was asleep.
			Intent wakeUpIntent = new Intent(context, WakeUpService.class);
			wakeUpIntent.putExtra("id", id);
			wakeUpIntent.putExtra("routines", routineTime);
			wakeUpIntent.putExtra("wake_time", wake_time);
			startWakefulService(context, wakeUpIntent);
			Log.d("Wakeup", "started service at " + new Date(System.currentTimeMillis()) + " with ID " + id);
		}

		// Otherwise, start the traffic service to analyse traffic and accelerometer data
		else if(action.equals(TRAFFIC)){

			// Send on the data
			String from = intent.getStringExtra("from");
			String to = intent.getStringExtra("to");
			String time = intent.getStringExtra("time");
			String id = intent.getStringExtra("id");
			Calendar wake_time = (Calendar) intent.getSerializableExtra("wake_time");
			int routineTime = intent.getIntExtra("routines", 0);

			// Start the service like above.
			Intent trafficIntent = new Intent(context, TrafficService.class);
			trafficIntent.putExtra("from", from);
			trafficIntent.putExtra("to", to);
			trafficIntent.putExtra("time", time);
			trafficIntent.putExtra("id", id);
			trafficIntent.putExtra("routines", routineTime);
			trafficIntent.putExtra("wake_time", wake_time);
			startWakefulService(context, trafficIntent);
			Log.d("Traffic", "started service at " + new Date(System.currentTimeMillis()) + " with ID " + id);
		}
	}
}
