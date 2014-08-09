package com.btc_e_assist;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class BalanceFragment extends ListFragment {
	private Context mContext;
	private TradeControl tradeControl;
	private CustomAdapter adapter;
	private String currentFragmentName = "";
	private static volatile DataBox dataBox = new DataBox();
	private SecondThread secondThread;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		currentFragmentName = getTag();
		tradeControl = TradeControl.getInstance();
		update();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.balance_list_layout, null);
		String[] from = { "name", "value" };
		int[] to = { R.id.itemBalanceName, R.id.itemBalanceValue };
		adapter = new CustomAdapter(mContext, dataBox.data1,
				R.layout.item_balance_fragment, from, to);
		setListAdapter(adapter);
		return rootView;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			update();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
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
			return tradeControl.loadBalanceData();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isAdded()) {
				return;
			}
			if (result.booleanValue() && tradeControl.setBalanceData(dataBox)) {
				if (adapter != null) {
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

	private class CustomAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<HashMap<String, Object>> mData;
		private int mLayoutId;
		private String[] mFrom;
		private int[] mTo;
		private int firstColor;
		private int secondColor;

		CustomAdapter(Context context, ArrayList<HashMap<String, Object>> data,
				int id, String[] from, int[] to) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mData = data;
			mLayoutId = id;
			mFrom = from;
			mTo = to;
			Resources res = context.getResources();
			firstColor = res.getColor(R.color.Black70);
			secondColor = res.getColor(R.color.Red2);
		}

		@Override
		public int getCount() {
			return mData.size();
		}

		@Override
		public Object getItem(int position) {
			return mData.get(position);
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View view = convertView;
			HashMap<String, Object> map = mData.get(position);
			if (view == null) {
				view = mInflater.inflate(mLayoutId, parent, false);
			}
			for (int i = 0; i < mTo.length; i++) {
				TextView textView = (TextView) view.findViewById(mTo[i]);
				String textData = (String) map.get(mFrom[i]);
				try {
					Double value = Double.parseDouble(textData);
					if (value == 0) {
						textView.setTextColor(firstColor);
					} else {
						textView.setTextColor(secondColor);
					}
				} catch (Exception e) {
					textView.setTextColor(firstColor);
				}
				textView.setText(textData);
			}
			return view;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}
	}
}
