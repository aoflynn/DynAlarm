package me.adamoflynn.dynalarm.utils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * Created by Adam on 30/04/2016.
 */
public final class Utils {

	public static boolean isMyServiceRunning(Class<?> serviceClass, Context context) {
		ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

}
