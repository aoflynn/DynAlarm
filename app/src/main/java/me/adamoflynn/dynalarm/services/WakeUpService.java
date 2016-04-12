package me.adamoflynn.dynalarm.services;

import android.app.IntentService;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import me.adamoflynn.dynalarm.model.TrafficInfo;

/**
 * Created by Adam on 11/04/2016.
 */
public class WakeUpService extends IntentService {

	private boolean isRunning;
	private Context context;
	private Thread backgroundThread;
	private TrafficInfo trafficInfo;
	private String fromA, toB, time;


	public WakeUpService(String name) {
		super(name);
	}

	public WakeUpService(){
		super("WakeUpService");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(!this.isRunning) {
			this.isRunning = true;
			this.backgroundThread.start();
		}
		return START_STICKY;
	}

	@Override
	public void onCreate() {
		this.context = this;
	}


	@Override
	public void onDestroy() {
		this.isRunning = false;
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	protected void onHandleIntent(Intent intent) {

	}

}
