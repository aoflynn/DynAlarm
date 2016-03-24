package me.adamoflynn.dynalarm;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListAdapter;
import android.widget.TextView;

import io.realm.RealmBaseAdapter;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.model.Routine;

/**
 * Created by Adam on 21/03/2016.
 */
public class RoutineAdapter extends RealmBaseAdapter<Routine> implements ListAdapter {

	public TextView name;
	public TextView desc;
	public CheckBox checkBox;

	public static class ViewHolder {
		TextView name;
		TextView desc;
		CheckBox checkBox;
	}


	public RoutineAdapter(Context context, int resId, RealmResults<Routine> realmResults, boolean automaticUpdate) {
		super(context, realmResults, automaticUpdate);
	}


	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder viewHolder;
		if (convertView == null) {
			convertView = inflater.inflate(android.R.layout.simple_list_item_checked, parent, false);
			viewHolder = new ViewHolder();
			viewHolder.name = (TextView) convertView.findViewById(android.R.id.text1);
			viewHolder.desc = (TextView) convertView.findViewById(android.R.id.text2);
			viewHolder.checkBox = (CheckBox) convertView.findViewById(android.R.id.checkbox);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (ViewHolder) convertView.getTag();
		}

		Routine item = realmResults.get(position);
		viewHolder.name.setText(item.getName());
		viewHolder.name.setTextColor(Color.WHITE);
		//viewHolder.desc.setText(item.getDesc());
		//viewHolder.checkBox.setOnCheckedChangeListener(mListener);*/

		return convertView;
	}

	CompoundButton.OnCheckedChangeListener mListener = new CompoundButton.OnCheckedChangeListener() {
		public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
			checkBox.setChecked(isChecked); // get the tag so we know the row and store the status
		}
	};


	public RealmResults<Routine> getRealmResults() {
		return realmResults;
	}

}
