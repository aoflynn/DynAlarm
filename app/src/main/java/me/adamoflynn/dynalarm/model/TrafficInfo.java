package me.adamoflynn.dynalarm.model;

import java.util.Date;

/**
 * Created by Adam on 02/04/2016.
 */
public class TrafficInfo {

	private int lengthInMeters;
	private int travelTime;
	private int travelDelay;
	private Date departureTime;
	private Date arrivalTime;
	private int travelTimeNoTraffic;
	private int historicTravelTime;
	private int liveIncidentsTravelTime;

	public TrafficInfo(){ }

	public TrafficInfo(int lengthInMeters, int travelTime, int travelDelay, Date departureTime, Date arrivalTime){
		this.setLengthInMeters(lengthInMeters);
		this.setTravelTime(travelTime);
		this.setTravelDelay(travelDelay);
		this.setDepartureTime(departureTime);
		this.setArrivalTime(arrivalTime);
	}

	public TrafficInfo(int lengthInMeters, int travelTime, int travelDelay, Date departureTime, Date arrivalTime, int travelTimeNoTraffic, int historicTravelTime, int liveIncidentsTravelTime){
		this.setLengthInMeters(lengthInMeters);
		this.setTravelTime(travelTime);
		this.setTravelDelay(travelDelay);
		this.setDepartureTime(departureTime);
		this.setArrivalTime(arrivalTime);
		this.setTravelTimeNoTraffic(travelTimeNoTraffic);
		this.setHistoricTravelTime(historicTravelTime);
		this.setLiveIncidentsTravelTime(liveIncidentsTravelTime);
	}

	public int getLengthInMeters() {
		return lengthInMeters;
	}

	public void setLengthInMeters(int lengthInMeters) {
		this.lengthInMeters = lengthInMeters;
	}

	public int getTravelTime() {
		return travelTime;
	}

	public void setTravelTime(int travelTime) {
		this.travelTime = travelTime;
	}

	public int getTravelDelay() {
		return travelDelay;
	}

	public void setTravelDelay(int travelDelay) {
		this.travelDelay = travelDelay;
	}

	public Date getDepartureTime() {
		return departureTime;
	}

	public void setDepartureTime(Date departureTime) {
		this.departureTime = departureTime;
	}

	public Date getArrivalTime() {
		return arrivalTime;
	}

	public void setArrivalTime(Date arrivalTime) {
		this.arrivalTime = arrivalTime;
	}

	public int getTravelTimeNoTraffic() {
		return travelTimeNoTraffic;
	}

	public void setTravelTimeNoTraffic(int travelTimeNoTraffic) {
		this.travelTimeNoTraffic = travelTimeNoTraffic;
	}

	public int getHistoricTravelTime() {
		return historicTravelTime;
	}

	public void setHistoricTravelTime(int historicTravelTime) {
		this.historicTravelTime = historicTravelTime;
	}

	public int getLiveIncidentsTravelTime() {
		return liveIncidentsTravelTime;
	}

	public void setLiveIncidentsTravelTime(int liveIncidentsTravelTime) {
		this.liveIncidentsTravelTime = liveIncidentsTravelTime;
	}

	public String toString(){
		return "Travel length in KM: " + this.lengthInMeters/1000.00
				+ "\nTravel Time in Mins: " + this.travelTime/60.00
				+ "\nDelay in Mins: " + this.travelDelay/60.00
				+ "\nDeparture Time: " + this.departureTime
				+ "\nArrival Time: " + this.arrivalTime
				+ "\nNo Traffic Time: " + this.travelTimeNoTraffic/60.00
				+ "\nHistoric Travel Time: " + this.historicTravelTime/60.00
				+ "\nLive Incidents Time: " + this.liveIncidentsTravelTime/60.00;
	}
}