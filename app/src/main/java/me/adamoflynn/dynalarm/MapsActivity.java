package me.adamoflynn.dynalarm;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import me.adamoflynn.dynalarm.services.TrafficService;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener {

  private GoogleMap mMap;
	private LatLng to, from;
	private TextView toText, fromText;
	private Button fetch;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_maps);

		toText = (TextView)findViewById(R.id.to);
	  fromText = (TextView)findViewById(R.id.from);
	  fetch = (Button)findViewById(R.id.fetchData);
	  fetch.setOnClickListener(this);


    SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
            .findFragmentById(R.id.map);
    mapFragment.getMapAsync(this);
	}




  @Override
  public void onMapReady(GoogleMap googleMap) {
    mMap = googleMap;

    // Add a marker in Sydney and move the camera
    /* LatLng sydney = new LatLng(53.703831, -6.302308);
    mMap.addMarker(new MarkerOptions().position(sydney).title("Home"));
    mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));*/

    /*if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
		    == PackageManager.PERMISSION_GRANTED) {
	    //mMap.setMyLocationEnabled(true);
    } else {*/
	    from = new LatLng(53.703831, -6.302308);
	    mMap.addMarker(new MarkerOptions().position(from).title("Home"));
	    mMap.moveCamera(CameraUpdateFactory.newLatLng(from));
	    mMap.setOnMapLongClickListener(this);
	    fromText.setText(from.toString());
    //}
  }

	@Override
	public void onMapLongClick(LatLng point) {
		to = point;
		toText.setText(to.toString());
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()){
			case R.id.fetchData:
				String fromA = Double.toString(from.latitude) + "," + Double.toString(from.longitude);
				String fromB = Double.toString(to.latitude) + "," + Double.toString(to.longitude);
				new TrafficService().execute(fromA, fromB);
				break;
		}
	}
}
