package com.btc_e_assist;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;
import com.btc_e_assist.R;

public class FinancesActivity extends ActionBarActivity {
	private ActionBar actionBar;
	private static int previousPosition = -1;
	private static final String BUNDLE_POSITION_NAME = "position";
	private Bundle mSavedState;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		CommonHelper.showPasswordDialog(this);
		mSavedState = savedState;
		setContentView(R.layout.activity_standard_fragment);
		actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		SpinnerAdapter mSpinnerAdapter = ArrayAdapter.createFromResource(this,
				R.array.finances_tabs_names, R.layout.item_actionbar_spinner);
		ActionBar.OnNavigationListener mOnNavigationListener = new OnNavigationListener() {
			String[] tabNames = getResources().getStringArray(
					R.array.finances_tabs_names);

			@Override
			public boolean onNavigationItemSelected(int position, long rowId) {
				if (mSavedState != null) {
					int savedPosition = mSavedState.getInt(
							BUNDLE_POSITION_NAME, Integer.MIN_VALUE);
					if (savedPosition != Integer.MIN_VALUE) {
						mSavedState.putInt(BUNDLE_POSITION_NAME,
								Integer.MIN_VALUE);
						actionBar.setSelectedNavigationItem(savedPosition);
						return false;
					} else {
						if (previousPosition == position) {
							return false;
						}
					}
				}
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				switch (position) {
				case 0:
					ft.replace(R.id.standardLayout, new BalanceFragment(),
							tabNames[position]);
					break;
				case 1:
					ft.replace(R.id.standardLayout, new MyTradeFragment(),
							tabNames[position]);
					break;
				case 2:
					ft.replace(R.id.standardLayout, new MyTransFragment(),
							tabNames[position]);
					break;
				}
				ft.commit();
				previousPosition = position;
				return true;
			}
		};
		actionBar.setListNavigationCallbacks(mSpinnerAdapter,
				mOnNavigationListener);
	}

	@Override
	protected void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putInt(BUNDLE_POSITION_NAME,
				actionBar.getSelectedNavigationIndex());
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.standard_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}
}
