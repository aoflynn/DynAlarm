package me.adamoflynn.dynalarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.adapters.LocationAdapter;
import me.adamoflynn.dynalarm.model.Location;


public class LocationActivity  extends AppCompatActivity {
	private Realm realm;
	private RealmResults<Location> locations = null;
	private LocationAdapter locationAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_location);
		realm = Realm.getDefaultInstance();


		// Set up back arrow on toolbar
		Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
		tb.setTitle("Locations");
		setSupportActionBar(tb);

		if(getSupportActionBar() != null){
			// UI updates ot arrow
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
			upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
			getSupportActionBar().setHomeAsUpIndicator(upArrow);
			tb.setTitleTextColor(Color.WHITE);
		}

		// When arrow is hit, simulate back press.
		tb.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		// Get all locations in DB and pass them to the LocationAdapter to populate the list view
		locations = realm.where(Location.class).findAll();
		locationAdapter = new LocationAdapter(this, locations, true);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(locationAdapter);

		// On long click listener implementation
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //list is my listView
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			                               final int pos, long id) {
				showDeleteDialog(locations.get(pos).getLocation(), pos, locations.get(pos).getAddress());
				return false;
			}
		});

	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}


	// Close DB
	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (realm != null) {
			realm.close();
			realm = null;
		}
	}


	// Create Alert to prompt user to delete or edit location
	private void showDeleteDialog(final String locationName, final int locationPos, final String locationAddr) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);

		builder.setTitle("Edit/Delete A Location");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.location_delete_item, null);
		final TextView name = (TextView) dialogView.findViewById(R.id.locationName);
		final TextView addr = (TextView) dialogView.findViewById(R.id.address);
		name.setText(locationName);
		addr.setText(locationAddr);

		builder.setView(dialogView);

		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), "Deleting...", Toast.LENGTH_LONG).show();

				// DB work
				Location location = locations.get(locationPos);
				realm.beginTransaction();
				location.removeFromRealm();
				realm.commitTransaction();

				//routines = realm.where(Routine.class).findAll();
			}
		});

		builder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Location loc = locations.get(locationPos);
				showEditDialog(loc, locationName, locationAddr);
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		builder.show();
	}


	// Dialog to allow user to edit a location and save it to the db
	private void showEditDialog(final Location location, String locationName, String locationAddr) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(LocationActivity.this);
		builder.setTitle("Edit A Location");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.map_add_location, null);

		final EditText name = (EditText) dialogView.findViewById(R.id.locationName);
		final TextView address = (TextView) dialogView.findViewById(R.id.address);
		name.setText(locationName);
		address.setText(locationAddr);

		builder.setView(dialogView);
		builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				editLocation(location, name.getText().toString(), address.getText().toString());
			}
		});

		builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		final AlertDialog dialog = builder.show();

		name.setOnEditorActionListener(
				new EditText.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE ||
								(event.getAction() == KeyEvent.ACTION_DOWN &&
										event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
							dialog.dismiss();
							editLocation(location, name.getText().toString(), address.getText().toString());
							//locations = realm.where(Location.class).findAll();
							return true;
						}
						return false;
					}
				});
	}

	// Method to update the specified entry in the database

	private void editLocation(Location location, String name, String addr) {
		if (name == null || name.length() == 0) {
			Toast.makeText(this, "Empty Location!", Toast.LENGTH_SHORT).show();
			return;
		}
		realm.beginTransaction();
		location.setLocation(name);
		location.setAddress(addr);
		realm.commitTransaction();
		Log.d("Updating", name);
	}
}
