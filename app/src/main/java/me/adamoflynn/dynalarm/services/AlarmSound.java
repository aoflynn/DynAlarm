package me.adamoflynn.dynalarm.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;



public class AlarmSound extends Service {

	private Vibrator vibrator;
	private MediaPlayer mediaPlayer;
	private PowerManager.WakeLock wakeLock;
	private final long[] vibPattern = {600, 600, 600};
	private boolean isPlaying, isVibrateOn = false;
	private SharedPreferences prefs;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate(){

		// Get our settings and our required system services
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);


		// Acquire a wake lock so phone comes and stays awake when waking up user
		PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "RINGTONE");
		wakeLock.acquire();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		// Get vibration settings from preferences
		boolean isVibrateEnabled = prefs.getBoolean("Vibration", true);
		if(isVibrateEnabled){
			vibrator.vibrate(vibPattern, 0);
			isVibrateOn = true;
		}

		// Get ringtone from prefs
		String ringtone = prefs.getString("alarmTone", "Default alarm sound");
		Uri alarmNoise = Uri.parse(ringtone);

		try {
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setDataSource(this, alarmNoise);
			mediaPlayer.setLooping(true);
			mediaPlayer.prepare();
			mediaPlayer.start();
			isPlaying = true;
			Log.d("ALARM SOUND", " playing service..");
		} catch (IOException e) {
			e.printStackTrace();
		}


		// return this so service can restart itself if killed
		return START_STICKY;
	}


	// RElease all the required media streams and stop playing music and vibrating
	@Override
	public void onDestroy(){
		if(isPlaying){
			mediaPlayer.stop();
			mediaPlayer.release();
			if(isVibrateOn){
				vibrator.cancel();
			}
			Log.d("ALARM WAKE LOCK -before", " released...");

			// Release wake lock so phone can go to sleep again if required
			wakeLock.release();
			Log.d("ALARM WAKE LOCK -after", wakeLock.toString());
			wakeLock = null;
		}
		Log.v("Alarm","Stopping ringtone service...");
	}
}
