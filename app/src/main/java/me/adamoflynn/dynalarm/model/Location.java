package me.adamoflynn.dynalarm.model;

/**
 * Created by Adam on 20/02/2016.
 */
public class Location {

    private int id;
    private String location;
    private double locLat;
    private double locLon;

    public Location(){}

    /**
     * Constructor to edit saved locations
     * @param id
     * @param location
     * @param locLat
     * @param locLon
     */
    public Location(int id, String location, double locLat, double locLon){
        this.id = id;
        this.location = location;
        this.locLat = locLat;
        this.locLon = locLon;
    }

    /**
     * Constructor to create saved locations
     * @param location
     * @param locLat
     * @param locLon
     */
    public Location(String location, double locLat, double locLon){
        this.location = location;
        this.locLat = locLat;
        this.locLon = locLon;
    }

    /*
       Setters
    */

    public void setId(int id) {
        this.id = id;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public void setLocLat(double locLat) {
        this.locLat = locLat;
    }

    public void setLocLon(double locLon) {
        this.locLon = locLon;
    }

    /*
       Getters
    */
    public int getId() {
        return id;
    }

    public String getLocation() {
        return location;
    }

    public double getLocLat() {
        return locLat;
    }

    public double getLocLon() {
        return locLon;
    }
}