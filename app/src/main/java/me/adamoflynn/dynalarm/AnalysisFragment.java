package me.adamoflynn.dynalarm;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.Context;
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
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.formatter.XAxisValueFormatter;
import com.github.mikephil.charting.utils.ViewPortHandler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.adamoflynn.dynalarm.model.AccelerometerData;
import me.adamoflynn.dynalarm.model.Sleep;
import me.adamoflynn.dynalarm.services.AccelerometerService;

public class AnalysisFragment extends Fragment implements View.OnClickListener {

	private ArrayList<Entry> entries;
	private ArrayList<Integer> motion, max_motion;
	private ArrayList<String> labels, sleep_stage;
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

		if(!isMyServiceRunning(AccelerometerService.class)){
			Log.d("Delete sleep", " no service running...");
			deleteBadDates();
		} else{
			Log.d("Delete sleep", " denied - accelerometer running...");
		}

		allSleepReq = getAllSleep();
		sleepIndex = allSleepReq.size() - 1;
		Log.d("Sler", Integer.toString(sleepIndex));
		getData(sleepIndex);
		initializeLineChart();
		initializeBarChart();
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
	public void onDestroy() {
		super.onDestroy();
		if (realm != null) {
			realm.close();
			realm = null;
		}
	}


	private void initializeLineChart(){
		dataSet = new LineDataSet(entries, "Movements");

		dataSet.setDrawCubic(true);
		dataSet.setDrawCircles(false);
		dataSet.setDrawFilled(false);
		dataSet.setDrawValues(false);
		dataSet.setHighlightEnabled(false);
		dataSet.setFillColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
		//dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
		dataSet.setFillAlpha(195);

		chart.setTouchEnabled(true);
		chart.setDragEnabled(true);
		chart.setPinchZoom(true);
		chart.setDrawGridBackground(false);
		chart.setAutoScaleMinMaxEnabled(true);
		chart.setBorderColor(Color.BLACK);
		chart.setNoDataTextDescription("No data for this sleep");
		chart.setDescription("");
		chart.setHardwareAccelerationEnabled(true);

		YAxis leftAxis = chart.getAxisLeft();
		leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
		leftAxis.setDrawLabels(true); // no axis labels
		leftAxis.setStartAtZero(true);
		leftAxis.setDrawGridLines(false); // no grid lineschart.setData(data);
		leftAxis.setTextColor(Color.WHITE);
		leftAxis.setAxisMaxValue(dataSet.getYMax());

		YAxis rightAxis = chart.getAxisRight();
		rightAxis.setEnabled(false);


		XAxis xAxis = chart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setDrawAxisLine(true);
		xAxis.setDrawGridLines(false);
		xAxis.setTextColor(Color.WHITE);



		LineData data = new LineData(labels, dataSet);
		chart.setData(data);
		chart.notifyDataSetChanged();
		chart.invalidate();
	}

	private void initializeBarChart() {
		dataSet = new LineDataSet(entries, "Movements");

		dataSet.setDrawCubic(true);
		dataSet.setDrawCircles(false);
		dataSet.setDrawFilled(false);
		dataSet.setDrawValues(false);
		dataSet.setHighlightEnabled(false);
		dataSet.setFillColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
		//dataSet.setColor(ContextCompat.getColor(getActivity(), R.color.colorAccent));
		dataSet.setFillAlpha(195);

		chart.setTouchEnabled(true);
		chart.setDragEnabled(true);
		chart.setPinchZoom(true);
		chart.setDrawGridBackground(false);
		chart.setAutoScaleMinMaxEnabled(true);
		chart.setBorderColor(Color.BLACK);
		chart.setNoDataTextDescription("No data for this sleep");
		chart.setDescription("");
		chart.setHardwareAccelerationEnabled(true);

		YAxis leftAxis = chart.getAxisLeft();
		leftAxis.removeAllLimitLines(); // reset all limit lines to avoid overlapping lines
		leftAxis.setDrawLabels(true); // no axis labels
		leftAxis.setStartAtZero(true);
		leftAxis.setDrawGridLines(false); // no grid lineschart.setData(data);
		leftAxis.setTextColor(Color.WHITE);
		leftAxis.setAxisMaxValue(dataSet.getYMax());

		YAxis rightAxis = chart.getAxisRight();
		rightAxis.setEnabled(false);


		XAxis xAxis = chart.getXAxis();
		xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
		xAxis.setDrawAxisLine(true);
		xAxis.setDrawGridLines(false);
		xAxis.setTextColor(Color.WHITE);



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

		if(results.size() == 0){
			Log.d("Sleep Id", String.valueOf(sleep.getId()) + " no accelerometer data for this date.");
			return;
		}
		results.sort("timestamp");

		if(results.size() == 0){
			Log.d("Definitely something", " wrong with accelerometer data");
			return;
		}

		if(checkDateAfter(sleep.getStartTime())){
			Log.d("SIZE before", Integer.toString(results.size()));
			Log.d("AFTER","true");
			int concatDataLength = results.size() / 5 + 1; // Get the required length of new array where 5 represents every 5 minutes we concatenate
			for (int j = 0, k = 0; j < concatDataLength; j++ ){
				labels.add(format.format(results.get(k).getTimestamp()));
				int amt = 0;
				int count = 0;
				for (; count < 5 && k < results.size(); k++ ){
					amt += results.get(k).getAmtMotion();
					count++;
				}
				motion.add(amt);
				entries.add(new Entry(amt + 30, i++));
			}

			Log.d("Motion ", motion.toString());
			Log.d("Labels ", labels.toString());
			Log.d("Sleep size", Integer.toString(entries.size()));
		}
		else {
			Log.d("BEFORE","true");
			for (AccelerometerData a: results) {
				motion.add(a.getAmtMotion());
				maxVar.add(a.getMaxAccel());
				entries.add(new Entry(a.getAmtMotion() + 30, i++));
				labels.add(format.format(a.getTimestamp()));
			}

			Log.d("Motion ", motion.toString());
			Log.d("Labels ", labels.toString());
			Log.d("Max Var", maxVar.toString());
			Log.d("Sleep size", Integer.toString(entries.size()));
		}

		int concatDataLength = results.size() / 15 + 1; // Get the required length of new array where 5 represents every 15 minutes we concatenate
		for (int j = 0, k = 0; j < concatDataLength; j++ ){
			int max = 0, amt = 0, count = 0;
			double avg = 0;
			for (; count < 15 && k < results.size(); k++ ){
				amt += results.get(k).getAmtMotion();
				if(max < results.get(k).getAmtMotion()) max = results.get(k).getAmtMotion();
				count++;
			}

			avg = amt / 15.0;
			Log.d("Avg", Double.toString(avg));
			Log.d("Amt", Integer.toString(amt));
			Log.d("Max", Integer.toString(max));

			/*motion.add(amt);
			entries.add(new Entry(amt + 30, i++));*/
		}

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
					initializeLineChart();
					changeText();
					break;
				}
			case R.id.next:
				//newestData = realm.where(Sleep.class).max("id");
				if(sleepIndex == allSleepReq.size() - 1) {
					//Log.d("Newest data", Integer.toString(newestData.intValue())) ;
					Toast.makeText(getActivity(), "Already at Newest Sleep!", Toast.LENGTH_SHORT).show();
					break;
				}
				else{
					getData(sleepIndex + 1);
					sleepIndex++;
					initializeLineChart();
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
			}
		} else{
			Log.d("No dirty sleeps", "---");
		}

		realm.commitTransaction();
	}

	private boolean isMyServiceRunning(Class<?> serviceClass) {
		ActivityManager manager = (ActivityManager) getActivity().getSystemService(Context.ACTIVITY_SERVICE);
		for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
			if (serviceClass.getName().equals(service.service.getClassName())) {
				return true;
			}
		}
		return false;
	}

	private boolean checkDateAfter(Long analysisDate){
		// Day I changed my analysis so want to read differently
		Calendar analysisChangeDate = new GregorianCalendar(2016, Calendar.APRIL, 26);
		analysisChangeDate.set(Calendar.HOUR, 12);
		Log.d("ANALYSIS", Long.toString(analysisChangeDate.getTimeInMillis()));
		Log.d("DATE", Long.toString(analysisDate));
		if (analysisDate > analysisChangeDate.getTimeInMillis()){
			return true;
		} else return false;
	}
}
