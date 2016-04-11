package me.adamoflynn.dynalarm.services;

import android.app.IntentService;
import android.content.Intent;

/**
 * Created by Adam on 11/04/2016.
 */
public class WakeUpService extends IntentService {


	public WakeUpService(){
		super("TrafficService");
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}
}
