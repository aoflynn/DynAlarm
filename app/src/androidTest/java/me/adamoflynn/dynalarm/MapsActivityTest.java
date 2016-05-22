package me.adamoflynn.dynalarm;

import android.support.test.espresso.contrib.PickerActions;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.widget.TimePicker;

import org.hamcrest.Matchers;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Adam on 04/05/2016.
 */
@RunWith(AndroidJUnit4.class)
public class MapsActivityTest {

	private final DateFormat hh = new SimpleDateFormat("HH:mm");

	@Rule
	public ActivityTestRule<MapsActivity> mActivityRule =
			new ActivityTestRule<>(MapsActivity.class);

	@Test
	public void timeShouldBeCurrentTime(){
		onView(withId(R.id.arriveAt)).check(matches(withText(hh.format(Calendar.getInstance().getTime()))));
	}

	@Test
	public void clickTimeShouldGetPicker(){
		onView(withId(R.id.arriveAt)).perform(click());

		onView(isAssignableFrom(TimePicker.class)).perform(PickerActions.setTime(12, 36));
		// Confirm the time
		onView(withId(android.R.id.button1)).perform(click());
		// Check if the date result is displayed.
		onView(withId(R.id.arriveAt)).check(matches(Matchers.allOf(withText("12:36"),
				ViewMatchers.isDisplayed())));
	}
}
