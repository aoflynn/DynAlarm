package me.adamoflynn.dynalarm.model;



import java.util.Date;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Adam on 08/03/2016.
 */
public class Sleep extends RealmObject {
	@PrimaryKey
	private int id;
	private long startTime;
	private long endTime;
	private Date date;
	private int quality;
	private RealmList<AccelerometerData> sleepData;

	public Sleep(){}

	public Sleep(int id, long startTime, long endTime, Date date, int quality, RealmList<AccelerometerData> sleepData){
		this.setId(id);
		this.setStartTime(startTime);
		this.setEndTime(endTime);
		this.setDate(date);
		this.setQuality(quality);
		this.setSleepData(sleepData);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public long getStartTime() {
		return startTime;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public long getEndTime() {
		return endTime;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public int getQuality() {
		return quality;
	}

	public void setQuality(int quality) {
		this.quality = quality;
	}

	public RealmList<AccelerometerData> getSleepData() {
		return sleepData;
	}

	public void setSleepData(RealmList<AccelerometerData> sleepData) {
		this.sleepData = sleepData;
	}
}
