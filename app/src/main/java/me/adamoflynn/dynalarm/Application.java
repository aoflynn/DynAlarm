package me.adamoflynn.dynalarm;

import com.facebook.stetho.Stetho;
import com.uphyca.stetho_realm.RealmInspectorModulesProvider;

import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Adam on 06/03/2016.
 */
public class Application extends android.app.Application {
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
				.name("default")
				.schemaVersion(1)
				.deleteRealmIfMigrationNeeded()
				.build();

		Realm.setDefaultConfiguration(config);
	}
}
