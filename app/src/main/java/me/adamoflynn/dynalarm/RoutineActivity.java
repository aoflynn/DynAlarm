package me.adamoflynn.dynalarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.adapters.RoutineAdapter;
import me.adamoflynn.dynalarm.model.Routine;
import me.adamoflynn.dynalarm.utils.RoutineOnItemSelectedListener;

public class RoutineActivity extends AppCompatActivity {
	private Realm realm;
	private int routineID;
	private RealmResults<Routine> routines = null;
	private RoutineAdapter routineAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routine);

		// Get the newest ID from db
		routineID = Application.routineID.incrementAndGet();
		realm = Realm.getDefaultInstance();


		// Back arrow implementation
		Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
		tb.setTitle("Routines");
		setSupportActionBar(tb);

		if(getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
			upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
			getSupportActionBar().setHomeAsUpIndicator(upArrow);
			tb.setTitleTextColor(Color.WHITE);
		}

		// Simulate back press
		tb.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		// Get all routines in db and set the adapter to this RoutineAdapter
		routines = realm.where(Routine.class).findAll();
		routineAdapter = new RoutineAdapter(this, R.id.listView, routines, true);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(routineAdapter);

		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		Button save = (Button) findViewById(R.id.done);

		// Action button in bottom right of screen allows users to add data
		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				buildAndShowInputDialog();
			}
		});

		// Show dialog to edit and delete routines
		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //list is my listView
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			                               final int pos, long id) {
				showDeleteDialog(routines.get(pos).getName(), pos, routines.get(pos).getDesc());
				return false;
			}
		});


		// Send required data back to alarm fragment
		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("routineData", routineAdapter.getCheckedRoutines());
				setResult(1, intent);
				finish();
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


	// Prompt for user
	private void buildAndShowInputDialog()  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(RoutineActivity.this);
		builder.setTitle("Create A Routine");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.routine_add_item, null);

		// Display UI fields in dialog
		final EditText name = (EditText) dialogView.findViewById(R.id.name);
		final Spinner desc = (Spinner) dialogView.findViewById(R.id.desc);

		// Read string array values from string.xml file
		final ArrayAdapter<CharSequence> times = ArrayAdapter.createFromResource(this, R.array.routine_time_list, R.layout.support_simple_spinner_dropdown_item );
		times.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		desc.setAdapter(times);
		desc.setOnItemSelectedListener(new RoutineOnItemSelectedListener());

		builder.setView(dialogView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {

				// ADd item, update the size of the boolean array in the adapter that is tracking which routine is being selected
				addToDoItem(name.getText().toString(), desc.getSelectedItem().toString().substring(0, 2));
				routines = realm.where(Routine.class).findAll();
				routineAdapter.updateArraySize(routines.size());
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
							addToDoItem(name.getText().toString(), desc.getSelectedItem().toString().substring(0, 2));
							routines = realm.where(Routine.class).findAll();
							routineAdapter.updateArraySize(routines.size());
							return true;
						}
						return false;
					}
				});
	}

	// Dialog to edit or delete
	private void showDeleteDialog(final String routineName, final int routinePos, final String routineDesc) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(RoutineActivity.this);

		builder.setTitle("Edit/Delete A Routine");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.routine_delete_item, null);
		final TextView routine = (TextView) dialogView.findViewById(R.id.locationName);
		routine.setText(routineName);

		builder.setView(dialogView);

		builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), "Deleting...", Toast.LENGTH_LONG).show();

				// DB work
				Routine ro = routines.get(routinePos);
				realm.beginTransaction();
				ro.removeFromRealm();
				realm.commitTransaction();

				// Necessary update sent to adapter so it handles checkboxes
				routineAdapter.updateRealmResults(routines = realm.where(Routine.class).findAll());
				routines = realm.where(Routine.class).findAll();
				routineAdapter.updateArraySize(routines.size());
			}
		});

		builder.setNeutralButton("Edit", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Routine ro = routines.get(routinePos);
				showEditDialog(ro, routineName, routineDesc);
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

	private void showEditDialog(final Routine routine, String nameText, String descText) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(RoutineActivity.this);
		builder.setTitle("Edit A Routine");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.routine_add_item, null);

		// Get names and spinner values from routine
		final EditText name = (EditText) dialogView.findViewById(R.id.name);
		name.setText(nameText);
		final Spinner spinner = (Spinner) dialogView.findViewById(R.id.desc);
		final ArrayAdapter<CharSequence> times = ArrayAdapter.createFromResource(this, R.array.routine_time_list, R.layout.support_simple_spinner_dropdown_item );
		times.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinner.setAdapter(times);
		spinner.setSelection(times.getPosition(descText + " minutes"));
		Log.d("Position of edit ", Integer.toString(times.getPosition(descText + " minutes")));
		spinner.setOnItemSelectedListener(new RoutineOnItemSelectedListener());

		builder.setView(dialogView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				editToDoItem(routine, name.getText().toString(), spinner.getSelectedItem().toString().substring(0, 2) );
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
							editToDoItem(routine, name.getText().toString(), spinner.getSelectedItem().toString().substring(0, 2));
							routines = realm.where(Routine.class).findAll();
							routineAdapter.updateArraySize(routines.size());
							return true;
						}
						return false;
					}
				});
	}


	// Add new routine to DB
	private void addToDoItem(String name, String desc) {
		if (name == null || name.length() == 0) {
			Toast.makeText(this, "Empty Routine!", Toast.LENGTH_SHORT).show();
			return;
		}
		Routine routine = new Routine(routineID, name, desc);
		realm.beginTransaction();
		realm.copyToRealm(routine);
		realm.commitTransaction();
		routineID++;
	}

	// Edit existing routine to database

	private void editToDoItem(Routine routine, String name, String desc) {
		if (name == null || name.length() == 0) {
			Toast.makeText(this, "Empty Routine!", Toast.LENGTH_SHORT).show();
			return;
		}
		realm.beginTransaction();
		routine.setName(name);
		routine.setDesc(desc);
		realm.commitTransaction();
		Log.d("Updating", name);
	}

}