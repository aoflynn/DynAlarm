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

public class LocationAdapter extends RealmBaseAdapter<Location> implements ListAdapter {

	// Get UI
	public static class ViewHolder {
		TextView location;
		TextView address;
	}

	// Constructor to pass data and the application context
	public LocationAdapter(Context context, RealmResults realmResults, boolean automaticUpdate) {
		super(context, realmResults, automaticUpdate);
	}

	// This method populates the list view with every object in Realm Results list
	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			// Use my specific location item layout
			convertView = inflater.inflate(R.layout.location_item, parent, false);
			// Get a new viewholder so we can update UI
			viewHolder = new ViewHolder();
			// Get references to the UI from our custom layout above
			viewHolder.location = (TextView) convertView.findViewById(R.id.location);
			viewHolder.address = (TextView) convertView.findViewById(R.id.address);
			convertView.setTag(viewHolder);
			// Allow for the list entries to be long clickable, this is how I edit and delete locations
			convertView.setLongClickable(true);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// Get location object from database results and set the relevant data to the UI
		final Location loc = realmResults.get(position);
		viewHolder.location.setText(loc.getLocation());
		viewHolder.address.setText(loc.getAddress());

		return convertView;
	}

	public RealmResults<Location> getRealmResults() {
		return realmResults;
	}

}
