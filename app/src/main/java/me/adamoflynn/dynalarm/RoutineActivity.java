package me.adamoflynn.dynalarm;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
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
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import java.lang.reflect.Array;

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
		routineID = Application.routineID.incrementAndGet();
		realm = Realm.getDefaultInstance();

		routines = realm.where(Routine.class).findAll();
		routineAdapter = new RoutineAdapter(this, R.id.listView, routines, false);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(routineAdapter);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
		Button save = (Button) findViewById(R.id.save);

		fab.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				buildAndShowInputDialog();
			}
		});

		listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() { //list is my listView
			@Override
			public boolean onItemLongClick(AdapterView<?> arg0, View arg1,
			                               final int pos, long id) {
				showDeleteDialog(routines.get(pos).getName(), pos);
				return false;
			}
		});

		save.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Log.d("Stuff checked", routineAdapter.getCheckedRoutines().toString() );
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (realm != null) {
			realm.close();
			realm = null;
		}
	}

	private void buildAndShowInputDialog()  {
		final AlertDialog.Builder builder = new AlertDialog.Builder(RoutineActivity.this);
		builder.setTitle("Create A Routine");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.routine_add_item, null);

		final EditText name = (EditText) dialogView.findViewById(R.id.name);
		final Spinner desc = (Spinner) dialogView.findViewById(R.id.desc);
		final ArrayAdapter<CharSequence> times = ArrayAdapter.createFromResource(this, R.array.routine_time_list, R.layout.support_simple_spinner_dropdown_item );
		times.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		desc.setAdapter(times);
		desc.setOnItemSelectedListener(new RoutineOnItemSelectedListener());

		builder.setView(dialogView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
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

	private void showDeleteDialog(String routineName, final int routinePos) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(RoutineActivity.this);

		builder.setTitle("Delete A Routine");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.routine_delete_item, null);
		final TextView text = (TextView) dialogView.findViewById(R.id.text);
		final TextView routine = (TextView) dialogView.findViewById(R.id.routine);
		routine.setText(routineName);

		builder.setView(dialogView);

		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Toast.makeText(getApplicationContext(), "Deleting...", Toast.LENGTH_LONG).show();
				Routine ro = routines.get(routinePos);
				realm.beginTransaction();
				ro.removeFromRealm();
				realm.commitTransaction();
				routineAdapter.updateRealmResults(routines = realm.where(Routine.class).findAll());
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

		builder.show();
	}
}

