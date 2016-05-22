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

		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);

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

		return START_STICKY;
	}

	@Override
	public void onDestroy(){
		if(isPlaying){
			mediaPlayer.stop();
			mediaPlayer.release();
			if(isVibrateOn){
				vibrator.cancel();
			}
			Log.d("ALARM WAKE LOCK -before", " released...");
			wakeLock.release();
			Log.d("ALARM WAKE LOCK -after", wakeLock.toString());
			wakeLock = null;
		}

		Log.v("Alarm","Stopping ringtone service...");
	}
}
