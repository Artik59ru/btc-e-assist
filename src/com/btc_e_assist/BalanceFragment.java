package com.btc_e_assist;

import java.util.List;
import java.util.Map;
import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SimpleAdapter;
import com.btc_e_assist.R;

public class BalanceFragment extends ListFragment {
	private Context mContext;
	private TradeControl tradeControl;
	private NotClickableAdapter adapter;
	private String currentFragmentName = "";
	private DataBox dataBox = new DataBox();

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
		View rootView = inflater.inflate(R.layout.balance_list_layout, null);
		String[] from = { "name", "value" };
		int[] to = { R.id.itemBalanceName, R.id.itemBalanceValue };
		adapter = new NotClickableAdapter(mContext, dataBox.data1,
				R.layout.item_balance_fragment, from, to);
		setListAdapter(adapter);
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

	private class SecondThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return tradeControl.getBalanceData(dataBox);
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
					CommonHelper
							.makeToastUpdated(mContext, currentFragmentName);
				}
			} else {
				CommonHelper.makeToastErrorConnection(mContext,
						currentFragmentName);
			}
		}
	}

	private class NotClickableAdapter extends SimpleAdapter {
		public NotClickableAdapter(Context context,
				List<? extends Map<String, ?>> data, int resource,
				String[] from, int[] to) {
			super(context, data, resource, from, to);
		}

		public boolean isEnabled(int position) {
			return false;
		}
	}
}
