package com.btc_e_assist;

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
import android.widget.TextView;
import android.widget.Toast;

public class OrdersFragment extends Fragment {
	private Context mContext;
	private TradeControl tradeControl;
	private static CustomExpandAdapter adapter;
	private static ExpandableListView ordersList;
	private static volatile DataBox dataBox = new DataBox();
	private TextView noData;
	private String currentFragmentName = "";
	private SecondThread secondThread;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		currentFragmentName = getTag();
		tradeControl = TradeControl.getInstance();
		update();
		CommonHelper.showPasswordDialog(mContext);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_expandable_list,
				container, false);
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
		return rootView;
	}

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		if (dataBox.data1.size() == 0) {
			menu.setGroupVisible(R.id.action_group_orders_delete, false);
		} else {
			menu.setGroupVisible(R.id.action_group_orders_delete, true);
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.orders_actions, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			update();
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
		if (noData != null) {
			if (dataBox.data1.size() == 0) {
				noData.setVisibility(View.VISIBLE);
			} else {
				noData.setVisibility(View.GONE);
			}
			getActivity().supportInvalidateOptionsMenu();
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

	private void update() {
		if (!tradeControl.tradeApi.isKeysInstalled()) {
			CommonHelper.makeToastNoKeys(mContext);
		} else {
			if (secondThread != null) {
				secondThread.cancel(false);
			}
			secondThread = new SecondThread();
			secondThread.execute();
			CommonHelper.makeToastUpdating(mContext, currentFragmentName);
		}
	}

	private class SecondThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return tradeControl.loadOrdersData();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isAdded()) {
				return;
			}
			if (result.booleanValue() && tradeControl.setOrdersData(dataBox)) {
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
			boolean result = tradeControl.loadOrdersData();
			if (!result || dataBox.data1.size() == 0
					|| dataBox.data2.size() == 0) {
				result = tradeControl.loadOrdersData();
			}
			return result;
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
			if (result.booleanValue() && tradeControl.setOrdersData(dataBox)) {
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
