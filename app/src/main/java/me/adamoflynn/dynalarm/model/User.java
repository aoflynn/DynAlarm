package me.adamoflynn.dynalarm.model;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Adam on 20/02/2016.
 */

public class User extends RealmObject {
    @PrimaryKey
    private int id;
    private RealmList<Location> savedLocations;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public RealmList<Location> getSavedLocations() {
        return savedLocations;
    }

    public void setSavedLocations(RealmList<Location> savedLocations) {
        this.savedLocations = savedLocations;
    }
}
