package com.btc_e_assist;

import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class NewsFragment extends ListFragment {
	private Context mContext;
	private SimpleAdapter adapter;
	private String currentFragmentName = "";

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		currentFragmentName = getTag();
		HtmlCutter.setLanguage(Locale.getDefault().getLanguage());
		new NewsHeadlinesThread().execute();
		CommonHelper.makeToastUpdating(mContext, currentFragmentName);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.standard_list_layout, null);
		String[] from = { "date", "text" };
		int[] to = { R.id.itemNewsDate, R.id.itemNewsHeadline };
		adapter = new SimpleAdapter(mContext, HtmlCutter.newsData,
				R.layout.item_news_list, from, to);
		setListAdapter(adapter);
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.news_actions, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			CommonHelper.makeToastUpdating(mContext, currentFragmentName);
			new NewsHeadlinesThread().execute();
			return true;
		case R.id.action_set_russian:
			CommonHelper.makeToastUpdating(mContext, currentFragmentName);
			HtmlCutter.setLanguage(HtmlCutter.LANG_RU);
			new NewsHeadlinesThread().execute();
			return true;
		case R.id.action_set_english:
			CommonHelper.makeToastUpdating(mContext, currentFragmentName);
			HtmlCutter.setLanguage(HtmlCutter.LANG_EN);
			new NewsHeadlinesThread().execute();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		CommonHelper.makeToastUpdating(mContext, currentFragmentName);
		new NewsTextThread().execute(position);
	}

	private class NewsHeadlinesThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			return HtmlCutter.runGettingNewsList();
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

	class NewsTextThread extends AsyncTask<Integer, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Integer... arg0) {
			return HtmlCutter.runGettingNewsContent(arg0[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isAdded()) {
				return;
			}
			if (result.booleanValue() && HtmlCutter.news != null) {
				AlertDialog.Builder dialog = new AlertDialog.Builder(
						getActivity());
				dialog.setTitle(R.string.news);
				dialog.setMessage(HtmlCutter.news);
				dialog.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
							}
						});
				dialog.show();
			} else {
				CommonHelper.makeToastErrorConnection(mContext,
						currentFragmentName);
			}
		}
	}
}
