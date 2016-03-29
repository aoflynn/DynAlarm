package me.adamoflynn.dynalarm;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import io.realm.Realm;
import me.adamoflynn.dynalarm.adapters.ViewPagerAdapter;

public class MainActivity extends AppCompatActivity {

	private Toolbar tb;
	private TabLayout tabLayout;
	private ViewPager viewPager;
	private Realm db;

	private int[] tabsIcons = {
		R.drawable.ic_alarm_white_48dp,
		R.drawable.ic_timeline_white_48dp,
		R.drawable.ic_settings_white_48dp
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		db = Realm.getDefaultInstance();

		tb = (Toolbar) findViewById(R.id.toolbar);
		setSupportActionBar(tb);

		getSupportActionBar().hide();

		viewPager = (ViewPager) findViewById(R.id.viewpager);
		setupViewPager(viewPager);

		tabLayout = (TabLayout) findViewById(R.id.tabs);
		tabLayout.setupWithViewPager(viewPager);
		setupTabIcons();

	}

	private void setupViewPager(ViewPager viewPager) {
		ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
		adapter.addFragment(new AlarmFragment());
		adapter.addFragment(new AnalysisFragment());
		adapter.addFragment(new SettingsFragment());
		viewPager.setAdapter(adapter);
	}

	private void setupTabIcons(){
		tabLayout.getTabAt(0).setIcon(tabsIcons[0]);
		tabLayout.getTabAt(1).setIcon(tabsIcons[1]);
		tabLayout.getTabAt(2).setIcon(tabsIcons[2]);
	}


	@Override
	protected void onResume() {
		super.onResume();

	}

	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();

		if (db != null) {
			db.close();
			db = null;
		}
	}

}

