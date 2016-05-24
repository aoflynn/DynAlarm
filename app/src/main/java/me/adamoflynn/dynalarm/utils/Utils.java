package me.adamoflynn.dynalarm.utils;

import android.app.ActivityManager;
import android.content.Context;

/**
 * This method is used to check if the specified service is running currently.
 * Used extensively throughout my application
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
