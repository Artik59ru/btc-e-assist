package com.btc_e_assist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class OrdersFragment extends Fragment {
	private Context mContext;
	private TradeControl tradeControl;
	private DBControl dbControl;
	private static CustomAdapter adapter;
	private static ExpandableListView ordersList;
	private ArrayList<String> alarmOrdersIds = new ArrayList<String>();
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
		dbControl = DBControl.getInstance();
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
		dbControl.getOrderAlarmData(alarmOrdersIds);
		adapter = new CustomAdapter(mContext, dataBox.data1, groupFrom,
				groupTo, dataBox.data2, childFrom, childTo,
				R.layout.item_orders_group_list,
				R.layout.item_orders_child_list, R.id.itemOrdersGroupPrice,
				R.id.itemOrdersGroupCheckBox, R.id.itemOrdersChildViewDivider,
				aliases, drawIds, alarmOrdersIds);
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
					long taskId = dbControl.getOrderAlarmDBId(orderId);
					dbControl.deleteOrderAlarm(orderId);
					ServiceAssist.setDeleteTask(taskId);
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

	private class CustomAdapter extends CustomExpandAdapter {
		public ArrayList<Integer> checkData;
		private int mCheckBoxId;
		private ArrayList<String> mAlarmOrdersIds;

		public CustomAdapter(Context context,
				List<? extends Map<String, ?>> groupData, String[] groupFrom,
				int[] groupTo,
				List<? extends List<? extends Map<String, ?>>> childData,
				String[] childFrom, int[] childTo, int groupLayout,
				int childLayout, int imageId, int checkBoxId, int dividerId,
				String[] aliases, int[] aliasesDrawIds,
				ArrayList<String> alarmOrdersIds) {
			super(context, groupData, groupFrom, groupTo, childData, childFrom,
					childTo, groupLayout, childLayout, imageId, dividerId,
					aliases, aliasesDrawIds);
			mCheckBoxId = checkBoxId;
			mAlarmOrdersIds = alarmOrdersIds;
			checkData = new ArrayList<Integer>();
		}

		@Override
		public View getGroupView(int groupPosition, boolean isExpanded,
				View convertView, ViewGroup parent) {
			final int mPosition = groupPosition;
			final ExpandableListView mParent = (ExpandableListView) parent;
			View view = convertView;
			if (view == null) {
				view = mInflater.inflate(mGroupLayout, parent, false);
			}
			Map<String, ?> map = mGroupData.get(groupPosition);
			String currentAlias = (String) map.get("alias");
			final String currentPairName = (String) map.get("name0") + '-'
					+ (String) map.get("name1");
			int count = mGroupTo.length;
			for (int i = 0; i < count; i++) {
				TextView v = (TextView) view.findViewById(mGroupTo[i]);
				if (v != null) {
					if (currentAlias != null) {
						if (mGroupTo[i] == mImageId) {
							int countAliases = mAliases.length;
							for (int y = 0; y < countAliases; y++) {
								if (mAliases[y].equals(currentAlias)) {
									v.setCompoundDrawablesWithIntrinsicBounds(
											mAliasesDrawIds[y], 0, 0, 0);
									break;
								}
							}
						}
					}
					v.setText((String) map.get(mGroupFrom[i]));
				}

				if (mAlarmOrdersIds != null) {
					final ImageView bellIcon = (ImageView) view
							.findViewById(R.id.itemOrdersGroupBell);
					if (bellIcon != null) {
						final String orderId = (String) map.get("id");
						if (mAlarmOrdersIds.contains(orderId)) {
							bellIcon.setImageResource(R.drawable.orderalarm);
						} else {
							bellIcon.setImageResource(R.drawable.ordernotalarm);
						}
						bellIcon.setOnClickListener(new OnClickListener() {
							@Override
							public void onClick(View v) {
								if (alarmOrdersIds.contains(orderId)) {
									alarmOrdersIds.remove(orderId);
									long taskId = dbControl
											.getOrderAlarmDBId(orderId);
									dbControl.deleteOrderAlarm(orderId);
									ServiceAssist.setDeleteTask(taskId);
									bellIcon.setImageResource(R.drawable.ordernotalarm);
								} else {
									alarmOrdersIds.add(orderId);
									dbControl.addOrderAlarm(orderId);
									long taskId = dbControl
											.getOrderAlarmDBId(orderId);
									CheckableTask task = new OrderAlarmTask(
											currentPairName, orderId, taskId);
									ServiceAssist.setTask(task);
									bellIcon.setImageResource(R.drawable.orderalarm);
								}
							}
						});
					}
				}
			}

			final CheckBox checkBox = (CheckBox) view.findViewById(mCheckBoxId);
			if (checkBox != null) {
				boolean isContains = checkData.contains((Object) mPosition);
				if (isContains) {
					checkBox.setChecked(true);
				} else {
					checkBox.setChecked(false);
				}
				checkBox.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (checkData.contains(mPosition)) {
							checkData.remove(mPosition);
						} else {
							checkData.add(mPosition);
						}
					}
				});
				view.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (mParent.isGroupExpanded(mPosition)) {
							mParent.collapseGroup(mPosition);
						} else {
							mParent.expandGroup(mPosition);
						}
					}
				});
			}
			return view;
		}

	}
}
