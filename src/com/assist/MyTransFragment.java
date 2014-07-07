package com.assist;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class MyTransFragment extends Fragment {
	private Context mContext;
	private TradeControl tradeControl;
	private static CustomExpandAdapter adapter;
	private ExpandableListView tradesList;
	private volatile DataBox dataBox = new DataBox();
	private RelativeLayout layout;
	private TextView noData;
	private String currentFragmentName = "";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		currentFragmentName = getTag();
		tradeControl = TradeControl.getInstance(mContext);
		new SecondThread().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_expandable_list,
				container, false);
		layout = (RelativeLayout) rootView
				.findViewById(R.id.standardExpandableFragment);
		noData = (TextView) rootView
				.findViewById(R.id.standardFragmentExpNoData);
		String[] groupFrom = { "amount", "name" };
		int[] groupTo = { R.id.itemMyTransGroupAmount,
				R.id.itemMyTransGroupName };

		String[] childFrom = { "desc", "status", "date" };
		int[] childTo = { R.id.itemMyTransChildDescValue,
				R.id.itemMyTransChildStatusValue,
				R.id.itemMyTransChildTimeValue };

		String[] aliases = { "in", "out" };
		int[] drawIds = { R.drawable.buy_icon, R.drawable.sell_icon };
		adapter = new CustomExpandAdapter(mContext, dataBox.data1, groupFrom,
				groupTo, dataBox.data2, childFrom, childTo,
				R.layout.item_mytrans_group_list,
				R.layout.item_mytrans_child_list, R.id.itemMyTransGroupAmount,
				0, R.id.itemMyTransChildViewDivider, aliases, drawIds);
		tradesList = (ExpandableListView) rootView
				.findViewById(R.id.standardFragmentExpList);
		tradesList.setAdapter(adapter);
		checkNoData();
		CommonHelper.makeToastUpdating(mContext, currentFragmentName);
		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			CommonHelper.makeToastUpdating(mContext, currentFragmentName);
			new SecondThread().execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	void checkNoData() {
		if (layout != null && noData != null) {
			if (dataBox.data1.size() == 0) {
				noData.setVisibility(View.VISIBLE);
				layout.setBackgroundColor(getResources().getColor(R.color.Gray));
			} else {
				noData.setVisibility(View.GONE);
				layout.setBackgroundColor(getResources().getColor(
						android.R.color.white));
			}
		}
	}

	private class SecondThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return tradeControl.getTransHistoryData(dataBox);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isAdded()) {
				return;
			}
			if (result.booleanValue()) {
				if (adapter != null) {
					adapter.notifyDataSetChanged();
					checkNoData();
					CommonHelper
							.makeToastUpdated(mContext, currentFragmentName);
				}
			} else {
				CommonHelper.makeToastErrorConnection(mContext,
						currentFragmentName);
			}
		}
	}
}
