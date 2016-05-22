package me.adamoflynn.dynalarm.model;

import io.realm.RealmObject;

/**
 * Created by Adam on 08/03/2016.
 */
public class Settings extends RealmObject {
	private int wake_timeframe;
	private String wake_tone;
	private Boolean vibration;

	public Settings(){
	}

	public int getWake_timeframe() {
		return wake_timeframe;
	}

	public void setWake_timeframe(int wake_timeframe) {
		this.wake_timeframe = wake_timeframe;
	}

	public String getWake_tone() {
		return wake_tone;
	}

	public void setWake_tone(String wake_tone) {
		this.wake_tone = wake_tone;
	}

	public Boolean getVibration() {
		return vibration;
	}

	public void setVibration(Boolean vibration) {
		this.vibration = vibration;
	}
}
