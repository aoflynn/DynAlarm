package me.adamoflynn.dynalarm.services;

import android.app.Service;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
import java.net.URI;

/**
 * Created by Adam on 17/04/2016.
 */
public class AlarmSound extends Service {

	private Vibrator vibrator;
	private MediaPlayer mediaPlayer;
	private AudioManager audioManager;
	private PowerManager powerManager;
	private PowerManager.WakeLock wakeLock;
	private boolean isVibrateEnabled = true;
	private long[] vibPattern = {600, 600, 600};
	private Uri alarmNoise;
	private boolean isPlaying, isVibrateOn = false;

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate(){
		vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
		powerManager = (PowerManager) getSystemService(POWER_SERVICE);
		wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.ON_AFTER_RELEASE, "RINGTONE");
		wakeLock.acquire();
		Log.d("ALARM SOUND", " created service..");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		if(isVibrateEnabled){
			vibrator.vibrate(vibPattern, 0);
			isVibrateOn = true;
		}
		alarmNoise = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM);
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
			wakeLock.release();
			wakeLock = null;
		}

		Log.v("Alarm","Stopping ringtone service...");
	}
}
