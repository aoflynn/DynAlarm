package me.adamoflynn.dynalarm.services;


import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import me.adamoflynn.dynalarm.model.TrafficInfo;


/**
 * Created by Adam on 02/04/2016.
 */

public class TrafficService extends AsyncTask<String, String, String> {

	private final String BASE_URL = "https://api.tomtom.com/routing/1/calculateRoute/";
	private final String API_KEY = "nmqjmepdy9ppbp8yekvrsaet";
	private final String END_URL = "?key="+ API_KEY + "&routeType=fastest&traffic=true";

	private final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Override
	protected String doInBackground(String... params) {

		InputStream in;
		String data = "";
		Log.d("Execute:", params[0] + params[1]);

		try {
			URL url = new URL(BASE_URL + params[0]+":"+params[1] + "/json" + END_URL);
			HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
			StringBuilder stringBuilder = new StringBuilder();
			String line;
			while ((line = bufferedReader.readLine()) != null) {
				stringBuilder.append(line).append("\n");
			}
			bufferedReader.close();
			urlConnection.disconnect();
			return stringBuilder.toString();

		} catch (Exception e){
			Log.d("Error", e.getMessage());
			return null;
		}

	}

	protected void onPostExecute(String response){
		if(response == null) {
			response = "Error...";
		}

		parseJSON(response);

	}

	public void parseJSON(String response) {

		try{

			JSONObject jsonObject = new JSONObject(response);
			JSONArray allRoutes = jsonObject.getJSONArray("routes");

			// Only need one route
			JSONObject route = allRoutes.getJSONObject(0);
			JSONObject summary = route.getJSONObject("summary");

			int lengthInMeters = summary.getInt("lengthInMeters");
			int travelTimeInSeconds = summary.getInt("travelTimeInSeconds");
			int travelDelayInSeconds = summary.getInt("trafficDelayInSeconds");
			Date dep = removeT(summary.getString("departureTime"));
			Date arr = removeT(summary.getString("arrivalTime"));

			TrafficInfo trafficInfo = new TrafficInfo(lengthInMeters, travelTimeInSeconds, travelDelayInSeconds, dep, arr);

			Log.d("Data", trafficInfo.toString());

		}catch (Exception e) {
			Log.d("Error", e.getMessage());
		}

	}

	public Date removeT(String time){
		time = time.replace('T',' ');
		Date d = null;
		try{
			d = df.parse(time);
		}catch (Exception e){
			Log.d("Error", e.getMessage());
		}
		return d;
	}
}
