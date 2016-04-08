package me.adamoflynn.dynalarm.utils;

import android.view.View;
import android.widget.AdapterView;
import android.widget.Toast;

/**
 * Created by Adam on 08/04/2016.
 */
public class RoutineOnItemSelectedListener implements AdapterView.OnItemSelectedListener {

	@Override
	public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
		Toast.makeText(parent.getContext(), "OnItemSelected " + parent.getItemAtPosition(position).toString(), Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onNothingSelected(AdapterView<?> parent) {

	}
}
