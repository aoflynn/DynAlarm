package me.adamoflynn.dynalarm;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ListView;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import me.adamoflynn.dynalarm.adapters.RoutineAdapter;
import me.adamoflynn.dynalarm.adapters.SleepAdapter;
import me.adamoflynn.dynalarm.model.Routine;
import me.adamoflynn.dynalarm.model.Sleep;

public class SleepActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_sleep);
		Realm realm = Realm.getDefaultInstance();

		Toolbar tb = (Toolbar) findViewById(R.id.toolbar);
		tb.setTitle("Sleep Summaries");
		setSupportActionBar(tb);

		if(getSupportActionBar() != null){
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
			getSupportActionBar().setDisplayShowHomeEnabled(true);
			final Drawable upArrow = getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha);
			upArrow.setColorFilter(Color.parseColor("#FFFFFF"), PorterDuff.Mode.SRC_ATOP);
			getSupportActionBar().setHomeAsUpIndicator(upArrow);
			tb.setTitleTextColor(Color.WHITE);
		}

		tb.setNavigationOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				onBackPressed();
			}
		});

		RealmResults<Sleep> sleeps = realm.where(Sleep.class).findAll();
		sleeps.sort("startTime", Sort.DESCENDING);
		SleepAdapter sleepAdapter = new SleepAdapter(this, R.id.listView, sleeps, true);
		ListView listView = (ListView) findViewById(R.id.listView);
		listView.setAdapter(sleepAdapter);
	}
}
