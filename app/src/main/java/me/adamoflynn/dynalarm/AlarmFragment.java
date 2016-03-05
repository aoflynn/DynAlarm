package me.adamoflynn.dynalarm;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class AlarmFragment extends Fragment implements View.OnClickListener {


	public AlarmFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.fragment_alarm, container, false);

		Button b = (Button) v.findViewById(R.id.accelButton);
		b.setOnClickListener(this);

		return v;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (getArguments() != null) {

		}
	}

	public void onClick(View v){
		switch(v.getId()){
			case R.id.accelButton:
				Intent intent = new Intent(getActivity(), AccelerometerActivity.class);
				getActivity().startActivity(intent);
				break;
		}

	}



}
