package me.adamoflynn.dynalarm.adapters;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.HashSet;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.R;
import me.adamoflynn.dynalarm.model.Routine;

public class RoutineAdapter extends RealmBaseAdapter<Routine> implements ListAdapter {

	// checkRoutines and checkedIds keep track of which routines are selected
	public Boolean[] checkedRoutines;
	public HashSet<Integer> checkedIds = new HashSet<>();

	public static class ViewHolder {
		TextView name;
		TextView desc;
		CheckBox checkBox;
	}


	//	Constructor to pass data and the application context
	public RoutineAdapter(Context context, int resId, RealmResults<Routine> realmResults, boolean automaticUpdate) {
		super(context, realmResults, automaticUpdate);
		// Set up array of total number of objects in list/results
		checkedRoutines = new Boolean[getCount()];
		Log.d("Size", Integer.toString(checkedRoutines.length));
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			// Load in my custom routine layout
			convertView = inflater.inflate(R.layout.routine_item, parent, false);
			viewHolder = new ViewHolder();
			// Get references to the relevant layout aspects
			viewHolder.name = (TextView) convertView.findViewById(R.id.locationName);
			viewHolder.desc = (TextView) convertView.findViewById(R.id.minutes);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
			convertView.setTag(viewHolder);
			// Make them long clickable so I can edit and delete them
			convertView.setLongClickable(true);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		// Get object at position from DB results, and populate the UI view.
		final Routine item = realmResults.get(position);
		viewHolder.name.setText(item.getName());
		viewHolder.desc.setText(item.getDesc() + " minutes");

		// Initialise box and array position to false
		viewHolder.checkBox.setChecked(false);
		checkedRoutines[position] = false;

		// set up a click listener so I know when the box at the relevant position is ticked/unticked
		viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkedRoutines[position] = ((CheckBox) v).isChecked();
				if (((CheckBox) v).isChecked()) {
					checkedIds.add(item.getId());
				} else checkedIds.remove(item.getId());
			}
		});

		return convertView;
	}


	public RealmResults<Routine> getRealmResults() {
		return realmResults;
	}

	// Method that allows me to dynamically update array after list view is dynamically updated
	public void updateArraySize(int size){
		checkedRoutines = new Boolean[size];
	}

	// Return what routines the user wants to use in their sleep
	public HashSet getCheckedRoutines(){
		return checkedIds;
	}

}
