package me.adamoflynn.dynalarm.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.R;
import me.adamoflynn.dynalarm.model.Sleep;

/**
 * Created by Adam on 18/05/2016.
 */
public class SleepAdapter extends RealmBaseAdapter<Sleep> implements ListAdapter {

	public TextView date;
	public TextView time;
	public ProgressBar sleepDesired;
	public TextView sleepPercent;
	private final DateFormat format = new SimpleDateFormat("HH:mm");
	private final DateFormat formatGMT = new SimpleDateFormat("HH:mm");
	private final DateFormat dateFormat = new SimpleDateFormat("E MMM dd", Locale.ENGLISH);
	private SharedPreferences mSettings = PreferenceManager.getDefaultSharedPreferences(context);


	public static class ViewHolder {
		TextView date;
		TextView time;
		TextView duration;
		TextView sleepPercent;
		ProgressBar sleepDesired;
	}


	public SleepAdapter(Context context, int resId, RealmResults<Sleep> realmResults, boolean automaticUpdate) {
		super(context, realmResults, automaticUpdate);
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.sleep_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.date = (TextView) convertView.findViewById(R.id.date);
			viewHolder.time = (TextView) convertView.findViewById(R.id.time);
			viewHolder.sleepPercent = (TextView) convertView.findViewById(R.id.percent);
			viewHolder.sleepDesired = (ProgressBar) convertView.findViewById(R.id.sleepDesired);
			viewHolder.duration = (TextView) convertView.findViewById(R.id.duration);
			convertView.setTag(viewHolder);
			//convertView.setLongClickable(true);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Sleep s = realmResults.get(position);

		long start = s.getStartTime();
		long end = s.getEndTime();
		long lengthOfsleep = end - start;

		formatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
		viewHolder.time.setText(formatGMT.format(new Date(lengthOfsleep)) + " hrs");
		viewHolder.duration.setText(format.format(new Date(start)) + " to " + format.format(new Date(end)));
		viewHolder.date.setText(dateFormat.format(s.getDate()));

		String desired = mSettings.getString("desiredSleep", "07:00");

		try {
			formatGMT.setTimeZone(TimeZone.getTimeZone("GMT"));
			Date timeSelected = formatGMT.parse(desired);
			long desiredTime = timeSelected.getTime();
			double percent = ((double) lengthOfsleep/desiredTime) * 100;
			int progress = (int) Math.round(percent);
			viewHolder.sleepDesired.setMax(100);
			viewHolder.sleepDesired.setProgress(progress);
			if(progress >= 100) viewHolder.sleepPercent.setText("100%");
			else viewHolder.sleepPercent.setText(Integer.toString(progress)+"%");
		} catch (ParseException e) {
			e.printStackTrace();
		}

		return convertView;
	}


	public RealmResults<Sleep> getRealmResults() {
		return realmResults;
	}

}
