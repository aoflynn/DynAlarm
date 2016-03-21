package me.adamoflynn.dynalarm;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.YAxis;

import java.util.ArrayList;
import java.util.Calendar;

import io.realm.Realm;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.model.AccelerometerData;

public class AnalysisFragment extends Fragment implements View.OnClickListener {

	private ArrayList<Entry> entries;
	private ArrayList<String> labels;
	private Realm realm;
	private Number newestData;
	private int lastId;
	private Button previous, next;
	private LineChart chart;
	private LineDataSet dataSet;

	public AnalysisFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_analysis, container, false);
		chart = (LineChart) v.findViewById(R.id.chart);;
		realm = Realm.getDefaultInstance();
		newestData = realm.where(AccelerometerData.class).max("sleepId");
		lastId = newestData.intValue();
		getData(lastId);
		initializeChart(v);
		initializeButtons(v);
		return v;
	}

	public void initializeChart(View v){
		dataSet = new LineDataSet(entries, "Movements");

		dataSet.setDrawCubic(true);
		dataSet.setDrawFilled(true);
		dataSet.setFillColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
		dataSet.setFillAlpha(255);
		chart.setTouchEnabled(true);
		chart.setDragEnabled(true);
		chart.setScaleEnabled(true);
		chart.setPinchZoom(true);
		chart.setDrawGridBackground(false);

		YAxis leftAxis = chart.getAxisLeft();
		leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
		leftAxis.setAxisMaxValue(220f);
		leftAxis.setAxisMinValue(0f);

		LineData data = new LineData(labels, dataSet);
		chart.setData(data);
		//data.notifyDataChanged();
		chart.notifyDataSetChanged();
		chart.invalidate();
	}

	public void getData(int sleepId){
		Log.d("Sleep id", Integer.toString(sleepId));
		int i = 0;
		entries = new ArrayList<>();
		labels = new ArrayList<>();
		RealmResults<AccelerometerData> results = realm.where(AccelerometerData.class)
				.equalTo("sleepId", sleepId).findAll();

		for (AccelerometerData a: results) {
			entries.add(new Entry(a.getAmtMotion(), i++));
			labels.add(java.text.DateFormat.getTimeInstance().format(a.getTimestamp()));
		}
		lastId = sleepId;
		Log.d("Sleep size", Integer.toString(entries.size()));
	}

	public void initializeButtons(View v){
		previous = (Button)v.findViewById(R.id.previous);
		previous.setOnClickListener(this);
		next = (Button)v.findViewById(R.id.next);
		next.setOnClickListener(this);
	}

	public void onClick(View v){
		switch(v.getId()){
			case R.id.previous:
				Log.d("State: ", " previous sleep cycle...");
				getData(lastId - 1);
				initializeChart(v);
				break;
			case R.id.next:
				if(lastId == newestData.intValue()) {
					Toast.makeText(getActivity(), "Already at Newest Sleep!", Toast.LENGTH_SHORT).show();
					break;
				}
				else{
					getData(lastId + 1);
					initializeChart(v);
					Log.d("State: ", " next sleep cycle...");
				}
		}
	}
}
