package me.adamoflynn.dynalarm.model;


import io.realm.RealmObject;

/**
 * Created by Adam on 08/03/2016.
 */
public class Routine extends RealmObject{
	private int id;
	private String name;
	private String desc;

	public Routine(){}

	public Routine(int id, String name, String desc){
		this.setId(id);
		this.setName(name);
		this.setDesc(desc);
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}
}
