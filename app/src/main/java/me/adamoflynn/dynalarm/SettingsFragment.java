package me.adamoflynn.dynalarm;

import android.os.Bundle;
import android.preference.PreferenceFragment;
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

public class SettingsFragment extends PreferenceFragment {

	private TextView timeframe, timeframe_val, vibration, tone, tone_val;
	private RadioButton vibRadio;
	private Realm db;

	public SettingsFragment() {
		// Required empty public constructor
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);


		// Load the preferences from an XML resource
		addPreferencesFromResource(R.xml.preferences);
		//getActivity().setTheme(R.style.preferencesTheme);
	}

}
