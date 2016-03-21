package me.adamoflynn.dynalarm;

import android.util.Log;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import java.util.concurrent.atomic.AtomicInteger;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import me.adamoflynn.dynalarm.model.Routine;
import me.adamoflynn.dynalarm.model.Settings;
import me.adamoflynn.dynalarm.model.Sleep;
import me.adamoflynn.dynalarm.model.User;

/**
 * Created by Adam on 06/03/2016.
 */
public class Application extends android.app.Application {

	public static AtomicInteger sleepIDValue;
	public static AtomicInteger routineID;
 // Set up Stetho Debugging and Set up Realm DB for application
	@Override
	public void onCreate() {
		super.onCreate();

		// Create an InitializerBuilder
		Stetho.initialize(
				Stetho.newInitializerBuilder(this)
						.enableDumpapp(Stetho.defaultDumperPluginsProvider(this))
						.enableWebKitInspector(RealmInspectorModulesProvider.builder(this).build())
						.build());

		RealmConfiguration config = new RealmConfiguration.Builder(this)
				.deleteRealmIfMigrationNeeded()
				.build();

		Realm.setDefaultConfiguration(config);

		Realm db = Realm.getInstance(config);

		if(db.isEmpty()){
			db.beginTransaction();
			User user = db.createObject(User.class);
			Settings userSettings = db.createObject(Settings.class);
			userSettings.setWake_timeframe(30);
			userSettings.setWake_tone("beep");
			userSettings.setVibration(true);
			user.setId(0);
			user.setUserSettings(userSettings);
			db.commitTransaction();
		}

		Number query = db.where(Sleep.class).max("id");
		Number query2 = db.where(Routine.class).max("id");
		if(query == null ){
			sleepIDValue = new AtomicInteger(0);
		}
		else if(query2 == null){
			routineID = new AtomicInteger(0);
		}
		else{
			sleepIDValue = new AtomicInteger(query.intValue());
			routineID = new AtomicInteger(query2.intValue());
		}

		Log.d("Value Sleep ", Integer.toString(sleepIDValue.intValue()));
		Log.d("Value Routine ", Integer.toString(routineID.intValue()));

	}
}
