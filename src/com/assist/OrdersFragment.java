package com.assist;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class OrdersFragment extends Fragment {
	private Context mContext;
	private TradeControl tradeControl;
	private static CustomExpandAdapter adapter;
	private static ExpandableListView ordersList;
	private static volatile DataBox dataBox = new DataBox();
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
		String[] groupFrom = { "rate", "name0", "name1" };
		int[] groupTo = { R.id.itemOrdersGroupPrice, R.id.itemOrdersGroupName1,
				R.id.itemOrdersGroupName2 };

		String[] childFrom = { "amount", "total", "date", "status" };
		int[] childTo = { R.id.itemOrdersChildAmountValue,
				R.id.itemOrdersChildTotalValue, R.id.itemOrdersChildTimeValue,
				R.id.itemOrdersChildStatusValue };

		String[] aliases = { "buy", "sell" };
		int[] drawIds = { R.drawable.buy_icon, R.drawable.sell_icon };
		adapter = new CustomExpandAdapter(mContext, dataBox.data1, groupFrom,
				groupTo, dataBox.data2, childFrom, childTo,
				R.layout.item_orders_group_list,
				R.layout.item_orders_child_list, R.id.itemOrdersGroupPrice,
				R.id.itemOrdersGroupCheckBox, R.id.itemOrdersChildViewDivider,
				aliases, drawIds);
		ordersList = (ExpandableListView) rootView
				.findViewById(R.id.standardFragmentExpList);
		ordersList.setAdapter(adapter);
		checkNoData();
		CommonHelper.makeToastUpdating(mContext, currentFragmentName);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.orders_actions, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			CommonHelper.makeToastUpdating(mContext, currentFragmentName);
			new SecondThread().execute();
			return true;
		case R.id.action_cancel:
			CommonHelper.makeToastUpdating(mContext, currentFragmentName);
			new CancelOrdersThread().execute();
			return true;
		case R.id.action_select_all:
			selectAll();
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

	private void selectAll() {
		int count = dataBox.data1.size();
		int sizeChecks = adapter.checkData.size();
		adapter.checkData.clear();
		if (sizeChecks != count) {
			for (int i = 0; i < count; i++) {
				adapter.checkData.add(i);
			}
		}
		adapter.notifyDataSetChanged();
	}

	private class SecondThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return tradeControl.getOrdersData(dataBox);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isAdded()) {
				return;
			}
			if (result.booleanValue()) {
				if (adapter != null) {
					checkNoData();
					adapter.notifyDataSetChanged();
					CommonHelper
							.makeToastUpdated(mContext, currentFragmentName);
				}
			} else {
				CommonHelper.makeToastErrorConnection(mContext,
						currentFragmentName);
			}
		}
	}

	private class CancelOrdersThread extends AsyncTask<Void, Void, Boolean> {
		ArrayList<Integer> notCancelledOrders = new ArrayList<Integer>();

		@Override
		protected Boolean doInBackground(Void... arg0) {
			if (dataBox.data1.size() == 0 || dataBox.data2.size() == 0) {
				return true;
			}
			for (Integer position : adapter.checkData) {
				String orderId = (String) dataBox.data1.get(position).get("id");
				if (orderId != null) {
					tradeControl.tradeApi.cancelOrder.setOrder_id(orderId);
					if (!tradeControl.tradeApi.cancelOrder.runMethod()) {
						if (!tradeControl.tradeApi.cancelOrder.runMethod()) {
							notCancelledOrders.add(position);
						}
					}
				}
			}
			return tradeControl.getOrdersData(dataBox);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			adapter.checkData.clear();
			if (!isAdded()) {
				return;
			}
			for (Integer order : notCancelledOrders) {
				Toast.makeText(
						mContext,
						String.format(getString(R.string.order_not_cancelled),
								order + 1), Toast.LENGTH_SHORT).show();
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
