package me.adamoflynn.dynalarm.model;


import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class User extends RealmObject {
  @PrimaryKey
  private int id;
	private Settings userSettings;
  private RealmList<Location> userLocations;
	private RealmList<Sleep> userSleepData;
	private RealmList<Routine> userRoutines;

	// This will be used for data analysis and sleep quality calculations in future releases
	private int age;
	private String gender;
	private int weight;
	private int height;

	public User(){

	}


	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Settings getUserSettings() {
		return userSettings;
	}

	public void setUserSettings(Settings userSettings) {
		this.userSettings = userSettings;
	}

	public RealmList<Location> getUserLocations() {
		return userLocations;
	}

	public void setUserLocations(RealmList<Location> userLocations) {
		this.userLocations = userLocations;
	}

	public RealmList<Sleep> getUserSleepData() {
		return userSleepData;
	}

	public void setUserSleepData(RealmList<Sleep> userSleepData) {
		this.userSleepData = userSleepData;
	}

	public RealmList<Routine> getUserRoutines() {
		return userRoutines;
	}

	public void setUserRoutines(RealmList<Routine> userRoutines) {
		this.userRoutines = userRoutines;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public String getGender() {
		return gender;
	}

	public void setGender(String gender) {
		this.gender = gender;
	}

	public int getWeight() {
		return weight;
	}

	public void setWeight(int weight) {
		this.weight = weight;
	}

	public int getHeight() {
		return height;
	}

	public void setHeight(int height) {
		this.height = height;
	}
}
