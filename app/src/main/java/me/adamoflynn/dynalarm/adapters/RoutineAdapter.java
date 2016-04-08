package me.adamoflynn.dynalarm.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashSet;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.R;
import me.adamoflynn.dynalarm.model.Routine;

/**
 * Created by Adam on 21/03/2016.
 */
public class RoutineAdapter extends RealmBaseAdapter<Routine> implements ListAdapter {

	public TextView name;
	public TextView desc;
	public CheckBox checkBox;
	public Boolean[] checkedRoutines;
	public HashSet<Integer> checkedIds = new HashSet<>();

	public static class ViewHolder {
		TextView name;
		TextView desc;
		CheckBox checkBox;
	}


	public RoutineAdapter(Context context, int resId, RealmResults<Routine> realmResults, boolean automaticUpdate) {
		super(context, realmResults, automaticUpdate);
		checkedRoutines = new Boolean[getCount()];
		Log.d("Size", Integer.toString(checkedRoutines.length));
	}


	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(R.layout.routine_item, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(R.id.routine);
			viewHolder.desc = (TextView) convertView.findViewById(R.id.minutes);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(R.id.checkBox);
			convertView.setTag(viewHolder);
			convertView.setLongClickable(true);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		final Routine item = realmResults.get(position);
		viewHolder.name.setText(item.getName());
		viewHolder.desc.setText(item.getDesc() + " minutes");
		viewHolder.checkBox.setChecked(false);
		checkedRoutines[position] = false;
		viewHolder.checkBox.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				checkedRoutines[position] = ((CheckBox) v).isChecked();
				if (((CheckBox) v).isChecked() == true) {
					checkedIds.add(item.getId());
				} else checkedIds.remove(item.getId());
			}
		});

		return convertView;
	}

	public RealmResults<Routine> getRealmResults() {
		return realmResults;
	}

	public void updateArraySize(int size){
		checkedRoutines = new Boolean[size];
	}

	public HashSet getCheckedRoutines(){
		return checkedIds;
	}

}
