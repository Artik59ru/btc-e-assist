package com.btc_e_assist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.btc_e_assist.R;

public class ChatFragment extends ListFragment {
	private Context mContext;
	private CustomAdapter adapter;
	private String currentFragmentName = "";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		currentFragmentName = getTag();
		HtmlCutter.setLanguage(Locale.getDefault().getLanguage());
		new SecondThread().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_chat, null);
		ArrayList<String> redNicks = new ArrayList<String>();
		redNicks.add("admin");
		redNicks.add("support");
		redNicks.add("system");
		redNicks.add("penek");
		adapter = new CustomAdapter(mContext, HtmlCutter.chatData, redNicks);
		setListAdapter(adapter);
		CommonHelper.makeToastUpdating(mContext, currentFragmentName);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.chat_actions, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			CommonHelper.makeToastUpdating(mContext, currentFragmentName);
			new SecondThread().execute();
			return true;
		case R.id.action_set_russian:
			HtmlCutter.setLanguage(HtmlCutter.LANG_RU);
			new SecondThread().execute();
			return true;
		case R.id.action_set_english:
			HtmlCutter.setLanguage(HtmlCutter.LANG_EN);
			new SecondThread().execute();
			return true;
		case R.id.action_set_chinese:
			HtmlCutter.setLanguage(HtmlCutter.LANG_CN);
			new SecondThread().execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class SecondThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return HtmlCutter.runGettingChatData();
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

	@SuppressLint("DefaultLocale")
	private class CustomAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		private ArrayList<HashMap<String, Object>> mData;
		private ArrayList<String> mRedNicks;
		private String[] mFrom = { "nickname", "message" };
		private int[] mTo = { R.id.itemChatNickname, R.id.itemChatMessage };
		private int redColor;
		private int blackColor;

		CustomAdapter(Context context, ArrayList<HashMap<String, Object>> data,
				ArrayList<String> redNicks) {
			mInflater = (LayoutInflater) context
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			mData = data;
			mRedNicks = redNicks;
			redColor = getResources().getColor(R.color.Red2);
			blackColor = getResources().getColor(R.color.ChatText);
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
			TextView nicknameView = null;
			TextView messageView = null;
			HashMap<String, Object> map = mData.get(position);
			if (map.get("checked") != null) {
				view = mInflater.inflate(R.layout.item_chat_checked_mark,
						parent, false);
				return view;
			}
			if (view == null) {
				view = mInflater
						.inflate(R.layout.item_chat_list, parent, false);
				nicknameView = (TextView) view.findViewById(mTo[0]);
			} else {
				nicknameView = (TextView) view.findViewById(mTo[0]);
				if (nicknameView == null) {
					view = mInflater.inflate(R.layout.item_chat_list, parent,
							false);
					nicknameView = (TextView) view.findViewById(mTo[0]);
				}
			}
			messageView = (TextView) view.findViewById(mTo[1]);

			String nickname = (String) map.get("nickname");
			if (mRedNicks.contains(nickname.toLowerCase())) {
				nicknameView.setTextColor(redColor);
				nicknameView.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.chat_admin, 0, 0, 0);
				messageView.setTextColor(redColor);
				messageView.setTypeface(Typeface.DEFAULT_BOLD);

			} else {
				nicknameView.setTextColor(blackColor);
				nicknameView.setCompoundDrawablesWithIntrinsicBounds(
						R.drawable.chat_user, 0, 0, 0);
				messageView.setTextColor(blackColor);
				messageView.setTypeface(CommonHelper.fontRobotoLight);
			}
			nicknameView.setText((String) map.get(mFrom[0]));
			messageView.setText((String) map.get(mFrom[1]));
			return view;
		}

		@Override
		public boolean isEnabled(int position) {
			return false;
		}
	}
}
