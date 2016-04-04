package me.adamoflynn.dynalarm;

import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.model.Settings;
import me.adamoflynn.dynalarm.model.User;

public class SettingsFragment extends Fragment {

	private TextView timeframe, timeframe_val, vibration, tone, tone_val;
	private RadioButton vibRadio;
	private Realm db;

	public SettingsFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_settings, container, false);
		db = Realm.getDefaultInstance();
		initializeViews(v);
		populateViews();
		return v;
	}

	public void initializeViews(View v) {
		timeframe = (TextView) v.findViewById(R.id.timeframe);
		timeframe_val = (TextView) v.findViewById(R.id.timeframe_val);
		vibration = (TextView) v.findViewById(R.id.vibration);
		tone = (TextView) v.findViewById(R.id.tone);
		tone_val = (TextView) v.findViewById(R.id.tone_val);
		vibRadio = (RadioButton) v.findViewById(R.id.vibRadio);
	}

	public void populateViews(){
		Settings query = db.where(Settings.class).findFirst();
		timeframe_val.setText(Integer.toString(query.getWake_timeframe()));
		tone_val.setText(query.getWake_tone());
		vibRadio.setChecked(query.getVibration());
	}
}
