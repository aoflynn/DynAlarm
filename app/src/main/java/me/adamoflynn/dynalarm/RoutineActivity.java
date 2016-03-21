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
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import io.realm.Realm;
import io.realm.RealmResults;
import me.adamoflynn.dynalarm.model.Routine;

public class RoutineActivity extends AppCompatActivity {
	private Realm realm;
	private int routineID;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_routine);
		routineID = Application.routineID.incrementAndGet();
		realm = Realm.getDefaultInstance();

		final RealmResults<Routine> routines = realm.where(Routine.class).findAll();
		final RoutineAdapter routineAdapter = new RoutineAdapter(this, R.id.listView, routines, true);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(routineAdapter);
		FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
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
				Toast.makeText(getApplicationContext(),"Deleting...", Toast.LENGTH_LONG).show();
				Routine ro = routines.get(pos);
				realm.beginTransaction();
				ro.removeFromRealm();
				realm.commitTransaction();
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

	@Override
	protected void onDestroy() {
		super.onDestroy();
		if (realm != null) {
			realm.close();
			realm = null;
		}
	}

	private void buildAndShowInputDialog() {
		final AlertDialog.Builder builder = new AlertDialog.Builder(RoutineActivity.this);
		builder.setTitle("Create A Routine");

		LayoutInflater li = LayoutInflater.from(this);
		View dialogView = li.inflate(R.layout.routine_add_item, null);
		final EditText name = (EditText) dialogView.findViewById(R.id.name);
		final EditText desc = (EditText) dialogView.findViewById(R.id.desc);

		builder.setView(dialogView);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				addToDoItem(name.getText().toString(), desc.getText().toString());
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
							addToDoItem(name.getText().toString(), desc.getText().toString());
							return true;
						}
						return false;
					}
				});
	}

	private void addToDoItem(String name, String desc) {
		if (name == null || name.length() == 0) {
			Toast
					.makeText(this, "Empty Routine!", Toast.LENGTH_SHORT)
					.show();
			return;
		}
		Routine routine = new Routine(routineID, name, desc);
		/*routine.setId(routineID);
		routine.setName(name);
		routine.setDesc(desc);*/
		realm.beginTransaction();
		realm.copyToRealm(routine);
		realm.commitTransaction();
		routineID++;
	}

}
