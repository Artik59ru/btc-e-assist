package com.btc_e_assist;

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
import android.widget.TextView;

public class TradesFragment extends Fragment {
	private Context mContext;
	private TradeControl tradeControl;
	private static CustomExpandAdapter adapter;
	private ExpandableListView tradesList;
	private static volatile DataBox dataBox = new DataBox();
	private TextView noData;
	private String currentFragmentName = "";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		currentFragmentName = getTag();
		tradeControl = TradeControl.getInstance();
		new SecondThread().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_expandable_list,
				container, false);
		mContext = inflater.getContext();
		noData = (TextView) rootView
				.findViewById(R.id.standardFragmentExpNoData);
		String[] groupFrom = { "amount", "name0", "name1" };
		int[] groupTo = { R.id.itemTradesGroupAmount,
				R.id.itemTradesGroupName1, R.id.itemTradesGroupName2 };

		String[] childFrom = { "rate", "total", "date" };
		int[] childTo = { R.id.itemTradesChildPriceValue,
				R.id.itemTradesChildTotalValue, R.id.itemTradesChildTimeValue };

		String[] aliases = { "bid", "ask" };
		int[] drawIds = { R.drawable.buy_icon, R.drawable.sell_icon };
		adapter = new CustomExpandAdapter(mContext, dataBox.data1, groupFrom,
				groupTo, dataBox.data2, childFrom, childTo,
				R.layout.item_trades_group_list,
				R.layout.item_trades_child_list, R.id.itemTradesGroupAmount, 0,
				R.id.itemTradesChildViewDivider, aliases, drawIds);
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
		if (noData != null) {
			if (dataBox.data1.size() == 0) {
				noData.setVisibility(View.VISIBLE);
			} else {
				noData.setVisibility(View.GONE);
			}
		}
	}

	private class SecondThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			String pairName = TickerFragment.getCurrentPairName();
			if (pairName != null) {
				return tradeControl.getTradesData(pairName, dataBox);
			} else {
				return false;
			}
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
