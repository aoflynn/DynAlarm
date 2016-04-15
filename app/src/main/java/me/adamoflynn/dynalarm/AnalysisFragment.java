package me.adamoflynn.dynalarm;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
//import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.YAxis;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Sleep;

public class AnalysisFragment extends Fragment implements View.OnClickListener {

	private ArrayList<Entry> entries;
	private ArrayList<Integer> motion;
	private ArrayList<String> labels;
	private ArrayList<Float> maxVar;
	private Realm realm;
	private Number newestData;
	private int lastId;
	private Button previous, next;
	private TextView date;
	private LineChart chart;
	private LineDataSet dataSet;
	private final DateFormat format = new SimpleDateFormat("HH:mm");
	private final DateFormat dateFormat = new SimpleDateFormat("EEE, MMM dd", Locale.ENGLISH);
	private ArrayList<Sleep> allSleepReq = new ArrayList<>();
	private int sleepIndex;

	public AnalysisFragment() {
		// Required empty public constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		// Inflate the layout for this fragment
		View v = inflater.inflate(R.layout.fragment_analysis, container, false);
		chart = (LineChart) v.findViewById(R.id.chart);
		realm = Realm.getDefaultInstance();

		newestData = realm.where(AccelerometerData.class).max("sleepId");
		lastId = newestData.intValue();
		Log.d("Newest ID", String.valueOf(newestData.intValue()));


		deleteBadDates();
		allSleepReq = getAllSleep();
		sleepIndex = allSleepReq.size() - 1;
		Log.d("Sler", Integer.toString(sleepIndex));
		getData(sleepIndex);
		initializeChart();
		initializeDate(v);
		initializeButtons(v);
		return v;
	}

	private ArrayList<Sleep> getAllSleep() {
		RealmResults<Sleep> allSleep = realm.where(Sleep.class).findAll();
		allSleep.sort("id");
		ArrayList<Sleep> sleepIds = new ArrayList<>();
		for (Sleep sleep: allSleep) {
			sleepIds.add(sleep);
			Log.d("Array", String.valueOf(sleep.getId()));
		}

		return sleepIds;
	}


	@Override
	public void onStop() {
		super.onStop();
		if (realm != null) {
			realm.close();
			realm = null;
		}
	}


	private void initializeChart(){
		dataSet = new LineDataSet(entries, "Movements");

		dataSet.setDrawCubic(false);
		dataSet.setDrawCircles(true);
		dataSet.setDrawFilled(true);
		dataSet.setFillColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
		dataSet.setFillAlpha(195);

		chart.setTouchEnabled(true);
		chart.setDragEnabled(true);
		chart.setPinchZoom(true);
		chart.setDrawGridBackground(false);
		chart.setAutoScaleMinMaxEnabled(true);
		chart.setBorderColor(Color.BLACK);
		chart.setNoDataTextDescription("No data for this sleep");

		YAxis leftAxis = chart.getAxisLeft();
		leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
		leftAxis.setDrawLabels(true); // no axis labels
		leftAxis.setStartAtZero(true);
		leftAxis.setDrawGridLines(false); // no grid lineschart.setData(data);

		YAxis rightAxis = chart.getAxisRight();
		rightAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
		rightAxis.setDrawLabels(false); // no axis labels
		rightAxis.setStartAtZero(true);
		rightAxis.setDrawGridLines(false);

		XAxis xAxis = chart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setDrawAxisLine(true);
		xAxis.setDrawGridLines(false);



		LineData data = new LineData(labels, dataSet);
		chart.setData(data);
		chart.notifyDataSetChanged();
		chart.invalidate();
	}

	// Initialize the view
	public void initializeDate(View v){
		TextView date = (TextView) v.findViewById(R.id.date);
		Sleep s = realm.where(Sleep.class).equalTo("id", allSleepReq.get(sleepIndex).getId()).findFirst();
		Date d = s.getDate();
		date.setText(dateFormat.format(d));
	}

	// Have to implement this method to change the date as fragments are weird
	public void changeText(){
		TextView date = (TextView) getView().findViewById(R.id.date);
		Sleep s = realm.where(Sleep.class).equalTo("id", allSleepReq.get(sleepIndex).getId()).findFirst();
		Date d = s.getDate();
		if(d == null) date.setText("No date");
		date.setText(dateFormat.format(d));
	}

	private void getData(int sleepId){
		int i = 0;
		Sleep sleep = allSleepReq.get(sleepId);
		Log.d("Data", String.valueOf(sleepId));
		Log.d("Data", sleep.toString());

		entries = new ArrayList<>();
		labels = new ArrayList<>();
		motion = new ArrayList<>();
		maxVar = new ArrayList<>();
		RealmResults<AccelerometerData> results = realm.where(AccelerometerData.class)
				.equalTo("sleepId", sleep.getId()).findAll();
		results.sort("timestamp");

		if(results.size() == 0){
			Log.d("Definitely something", " wrong with accelerometer data");
			return;
		}


		for (AccelerometerData a: results) {
			motion.add(a.getAmtMotion());
			maxVar.add(a.getMaxAccel());
			entries.add(new Entry(a.getAmtMotion(), i++));
			labels.add(format.format(a.getTimestamp()));
		}

		Log.d("Motion ", motion.toString());
		Log.d("Labels ", labels.toString());
		Log.d("Max Var", maxVar.toString());
		Log.d("Sleep size", Integer.toString(entries.size()));
	}

	private void initializeButtons(View v){
		previous = (Button)v.findViewById(R.id.previous);
		previous.setOnClickListener(this);

		next = (Button)v.findViewById(R.id.next);
		next.setOnClickListener(this);
	}

	public void onClick(View v){
		switch(v.getId()){
			case R.id.previous:
				if(sleepIndex == 0) {
					Toast.makeText(getActivity(), "This is the oldest sleep!", Toast.LENGTH_SHORT).show();
					break;
				}
				else {
					Log.d("State: ", " previous sleep cycle...");
					getData(sleepIndex - 1);
					sleepIndex--;
					initializeChart();
					changeText();
					break;
				}
			case R.id.next:
				newestData = realm.where(Sleep.class).max("id");
				if(sleepIndex == allSleepReq.size() - 1) {
					Log.d("Newest data", Integer.toString(newestData.intValue())) ;
					Toast.makeText(getActivity(), "Already at Newest Sleep!", Toast.LENGTH_SHORT).show();
					break;
				}
				else{
					getData(sleepIndex + 1);
					sleepIndex++;
					initializeChart();
					changeText();
					Log.d("State: ", " next sleep cycle...");
					break;
				}
			default:
					Log.d("State: ", " not set up.");
		}
	}

	private void deleteBadDates(){
		RealmResults<Sleep> sleeps = realm.where(Sleep.class).equalTo("endTime", 0).findAll();
		Log.d("dirty sleeps", String.valueOf(sleeps.size()));

		realm.beginTransaction();
		List<Sleep> merp = sleeps;
		if(sleeps.size() > 0){
			for (int i = 0; i < merp.size(); i++){
				RealmResults<AccelerometerData> accData = realm.where(AccelerometerData.class).equalTo("sleepId", merp.get(i).getId()).findAll();
				List<AccelerometerData> acc = accData;
				for (int j = 0; j < acc.size(); j++ ) {
					acc.get(j).removeFromRealm();
				}
				merp.get(i).removeFromRealm();
				//Log.d("Dirty", String.valueOf(merp.get(i)));
			}
		} else{
			Log.d("No dirty sleeps", "---");
		}

		realm.commitTransaction();
	}
}
