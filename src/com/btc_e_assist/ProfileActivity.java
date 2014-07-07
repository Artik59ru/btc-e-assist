package com.btc_e_assist;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import com.btc_e_assist.R;

public class ProfileActivity extends ActionBarActivity {
	private ActionBar actionBar;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_standard_fragment);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.profile_tabs_names, R.layout.item_actionbar_spinner);
		ActionBar.OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
			String[] tabNames = getResources().getStringArray(
					R.array.profile_tabs_names);

			@Override
			public boolean onNavigationItemSelected(int position, long rowId) {
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				switch (position) {
				case 0:
					ft.replace(R.id.standardLayout, new AddProfileFragment(),
							tabNames[position]);
					break;
				case 1:
					ft.replace(R.id.standardLayout,
							new SelectProfileFragment(), tabNames[position]);
					break;
				}
				ft.commit();
				return true;
			}
		};
		actionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);
	}
}
