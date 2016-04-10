package me.adamoflynn.dynalarm.adapters;

import android.app.Fragment;
import android.app.FragmentManager;
import android.preference.PreferenceFragment;
import android.support.v13.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import me.adamoflynn.dynalarm.SettingsFragment;

/**
 * Created by Adam on 05/03/2016.
 */
public class ViewPagerAdapter extends FragmentPagerAdapter {
	private final List<Fragment> mFragmentList = new ArrayList<>();

	public ViewPagerAdapter(FragmentManager manager) {
		super(manager);
	}

	@Override
	public Fragment getItem(int position) {
		if(position == 2){
			return new SettingsFragment();
		}
		else return mFragmentList.get(position);
	}

	@Override
	public int getCount() {
		return mFragmentList.size();
	}

	public void addFragment(Fragment fragment) {
		mFragmentList.add(fragment);
	}
	@Override
	public CharSequence getPageTitle(int position) {
		return null;
	}
}
