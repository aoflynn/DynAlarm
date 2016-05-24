package me.adamoflynn.dynalarm.receivers;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Matchers;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowApplication;
import org.robolectric.shadows.ShadowLog;
import org.robolectric.shadows.ShadowNotification;
import org.robolectric.shadows.ShadowNotificationManager;


import java.util.List;

import io.realm.Realm;
import io.realm.RealmConfiguration;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.internal.RealmCore;
import me.adamoflynn.dynalarm.BuildConfig;
import me.adamoflynn.dynalarm.services.AlarmSound;

/**
 * Created by Adam on 29/03/2016.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest({Realm.class, RealmConfiguration.class, RealmQuery.class, RealmResults.class, RealmCore.class})
public class AlarmReceiverTest {

	private NotificationManager notificationManager;
	private Notification not;

	@Rule
	public PowerMockRule rule = new PowerMockRule();
	Realm mockRealm;


	static {
		ShadowLog.stream = System.out;
	}

	@Before
	public void setup() throws Exception {
		notificationManager = (NotificationManager) ShadowApplication.getInstance().
				getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);

		PowerMockito.mockStatic(Realm.class);
		PowerMockito.mockStatic(RealmConfiguration.class);
		PowerMockito.mockStatic(RealmCore.class);

		final Realm mockRealm = PowerMockito.mock(Realm.class);
		final RealmConfiguration mockRealmConfiguration = PowerMockito.mock(RealmConfiguration.class);

		PowerMockito.doNothing().when(RealmCore.class);
		RealmCore.loadLibrary();


		PowerMockito.whenNew(RealmConfiguration.class).withAnyArguments().thenReturn(mockRealmConfiguration);
		PowerMockito.when(Realm.getDefaultInstance()).thenReturn(mockRealm);
		PowerMockito.when(Realm.getInstance(Matchers.any(RealmConfiguration.class))).thenReturn(mockRealm);

		this.mockRealm = mockRealm;
	}

	@Test
	public void testBroadcastReceiverRegistered() {
		List<ShadowApplication.Wrapper> registeredReceivers = ShadowApplication.getInstance().getRegisteredReceivers();

		Assert.assertFalse(registeredReceivers.isEmpty());

		boolean receiverFound = false;
		for (ShadowApplication.Wrapper wrapper : registeredReceivers) {
			if (!receiverFound)
				receiverFound = AlarmReceiver.class.getSimpleName().equals(
						wrapper.broadcastReceiver.getClass().getSimpleName());
		}

		Assert.assertTrue(receiverFound); //will be false if not found
	}


	@Test
	public void testNotificationBroadcasted(){
		ShadowNotificationManager manager = Shadows.shadowOf(notificationManager);

		notificationManager.notify(0, not);
		Assert.assertEquals(1, manager.size());

		List<Notification> notification = manager.getAllNotifications();

		Log.d("SIZE", notification.toString());

		Assert.assertNotNull("Expected Notification Value", notification.get(0));

		ShadowNotification shadowNotification = Shadows.shadowOf(notification.get(0));
		Assert.assertNotNull(shadowNotification);

		Assert.assertEquals("Alarm! Wake up!", shadowNotification.getLatestEventInfo().getContentTitle());

		Assert.assertEquals("Click here to go to Alarm.", shadowNotification.getLatestEventInfo().getContentText());

	}

	@Test
	public void testAlarmServiceStarted(){
		Intent intent = new Intent(ShadowApplication.getInstance().getApplicationContext(), AlarmSound.class);

		Log.d("Intent", intent.getComponent().getClassName());
		Assert.assertEquals(AlarmSound.class.getName(), intent.getComponent().getClassName());
	}

	class AlarmSoundMock extends AlarmSound {
		@Override
		public void onCreate() {
			super.onCreate();
		}
	}

}
