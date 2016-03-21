package me.adamoflynn.dynalarm;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;

public class AnalysisFragment extends Fragment {

	private ArrayList<Entry> entries = new ArrayList<>();
	private ArrayList<String> labels = new ArrayList<>();

	public AnalysisFragment() {
		// Required empty public constructor
	}


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_analysis, container, false);
		initializeChart(v);
		return v;
	}

	public void initializeChart(View v){
		LineChart chart = (LineChart)v.findViewById(R.id.chart);
		LineDataSet dataSet = new LineDataSet(entries, "movement/time");


		dataSet.setDrawCubic(true);
		dataSet.setDrawFilled(true);
		dataSet.setFillColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
		dataSet.setFillAlpha(255);


		chart.setTouchEnabled(true);
		chart.setDragEnabled(true);
		chart.setScaleEnabled(true);
		chart.setPinchZoom(true);


		LineData data = new LineData(labels, dataSet);
		chart.setData(data);
		chart.invalidate();
	}

	public void getData(){

	}
}
