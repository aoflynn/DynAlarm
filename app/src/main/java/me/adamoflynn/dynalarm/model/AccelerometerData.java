package me.adamoflynn.dynalarm.model;

import java.util.Date;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Adam on 29/02/2016.
 */
public class AccelerometerData extends RealmObject {

	private Date date;
	private double xValue;
	private double yValue;
	private double zValue;

	public AccelerometerData(){}

	public AccelerometerData( Date date, double x, double y, double z){
		this.setDate(date);
		this.setxValue(x);
		this.setyValue(y);
		this.setzValue(z);
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public double getxValue() {
		return xValue;
	}

	public void setxValue(double xValue) {
		this.xValue = xValue;
	}

	public double getyValue() {
		return yValue;
	}

	public void setyValue(double yValue) {
		this.yValue = yValue;
	}

	public double getzValue() {
		return zValue;
	}

	public void setzValue(double zValue) {
		this.zValue = zValue;
	}
/**
	public String toString(){
		return "Time: " + date.getHours() + ":"+ date.getMinutes() + "." + date.getSeconds()
				+ " - X = " + xValue + " Y = " + yValue + " Z = " + zValue;
	}**/
}
