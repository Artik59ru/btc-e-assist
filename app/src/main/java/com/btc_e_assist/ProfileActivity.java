package com.btc_e_assist;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.OnNavigationListener;
import android.support.v7.app.ActionBarActivity;
import android.widget.ArrayAdapter;
import android.widget.SpinnerAdapter;

public class ProfileActivity extends ActionBarActivity {
    private static final String BUNDLE_POSITION_NAME = "position";
    private static int previousPosition = -1;
    private ActionBar actionBar;
    private Bundle mSavedState;

    @Override
    public void onCreate(Bundle savedState) {
        super.onCreate(savedState);
        mSavedState = savedState;
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
                        ft.replace(R.id.standardLayout, new AddProfileFragment(),
                                tabNames[position]);
                        break;
                    case 1:
                        ft.replace(R.id.standardLayout,
                                new SelectProfileFragment(), tabNames[position]);
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
}
