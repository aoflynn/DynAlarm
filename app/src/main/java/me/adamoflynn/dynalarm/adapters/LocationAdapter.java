package me.adamoflynn.dynalarm.adapters;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.R;
import me.adamoflynn.dynalarm.model.Location;

/**
 * Created by Adam on 21/05/2016.
 */
public class LocationAdapter extends RealmBaseAdapter<Location> implements ListAdapter {

	public static class ViewHolder {
		TextView location;
		TextView address;
	}

	public LocationAdapter(Context context, RealmResults realmResults, boolean automaticUpdate) {
		super(context, realmResults, automaticUpdate);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.location_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.location = (TextView) convertView.findViewById(R.id.location);
			viewHolder.address = (TextView) convertView.findViewById(R.id.address);
			convertView.setTag(viewHolder);
			convertView.setLongClickable(true);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Location loc = realmResults.get(position);
		viewHolder.location.setText(loc.getLocation());
		viewHolder.address.setText(loc.getAddress());

		return convertView;
	}


	public RealmResults<Location> getRealmResults() {
		return realmResults;
	}

}
