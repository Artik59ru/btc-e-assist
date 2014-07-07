package com.btc_e_assist;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import com.btc_e_assist.R;

public class TickerActivity extends ActionBarActivity {
	private ActionBar actionBar;
	FragmentManager fragmentManager;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_standard_fragment);
		fragmentManager = getSupportFragmentManager();
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.ticker_tabs_names, R.layout.item_actionbar_spinner);
		ActionBar.OnNavigationListener mOnNavigationListener = new OnNavigationListener() {

			String[] tabNames = getResources().getStringArray(
					R.array.ticker_tabs_names);

			@Override
			public boolean onNavigationItemSelected(int position, long rowId) {
				FragmentTransaction ft = fragmentManager.beginTransaction();
				switch (position) {
				case 0:
					ft.replace(R.id.standardLayout, new TickerFragment(),
							tabNames[position]);
					break;
				case 1:
					ft.replace(R.id.standardLayout, new OrdersFragment(),
							tabNames[position]);
					break;
				case 2:
					ft.replace(R.id.standardLayout, new TradesFragment(),
							tabNames[position]);
					break;
				}
				ft.commit();
				return true;
			}
		};
		actionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.standard_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
