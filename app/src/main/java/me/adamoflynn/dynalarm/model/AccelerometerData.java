package me.adamoflynn.dynalarm.model;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Adam on 29/02/2016.
 */
public class AccelerometerData extends RealmObject {

	@PrimaryKey
	private long timestamp;
	private int sleepId;
	private int amtMotion;
	private float maxAccel;
	private float minAccel;
	private String sleepState;

	public AccelerometerData(){}

	public AccelerometerData(long timestamp, int sleepId, int amtMotion, float maxAccel, float minAccel, String sleepState){
		this.setTimestamp(timestamp);
		this.setSleepId(sleepId);
		this.setAmtMotion(amtMotion);
		this.setMaxAccel(maxAccel);
		this.setMinAccel(minAccel);
		this.setSleepState(sleepState);
	}


	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public int getSleepId() {
		return sleepId;
	}

	public void setSleepId(int sleepId) {
		this.sleepId = sleepId;
	}

	public int getAmtMotion() {
		return amtMotion;
	}

	public void setAmtMotion(int amtMotion) {
		this.amtMotion = amtMotion;
	}

	public float getMaxAccel() {
		return maxAccel;
	}

	public void setMaxAccel(float maxAccel) {
		this.maxAccel = maxAccel;
	}

	public float getMinAccel() {
		return minAccel;
	}

	public void setMinAccel(float minAccel) {
		this.minAccel = minAccel;
	}

	public String getSleepState() {
		return sleepState;
	}

	public void setSleepState(String sleepState) {
		this.sleepState = sleepState;
	}

}
