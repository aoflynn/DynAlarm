package me.adamoflynn.dynalarm.utils;

import android.util.Log;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * Created by Adam on 15/04/2016.
 */
public class DatabaseMigrator  implements RealmMigration {
	@Override
	public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {

		RealmSchema schema = realm.getSchema();

		/** Realm DB migration
		 *  Version 0 -> 1
		 *  Adding address field to table Location
		 */

		if(oldVersion == 0){
			Log.d("Migrating", "...");
			schema.get("Location").addField("address", String.class);
			oldVersion++;
		}
	}
}
