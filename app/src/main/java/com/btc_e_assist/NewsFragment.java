package com.btc_e_assist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
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

import java.util.Locale;

public class NewsFragment extends ListFragment {
    public static final String NEWS_CONTENT_ID_NAME = "newsId";
    private Context mContext;
    private SimpleAdapter adapter;
    private String currentFragmentName = "";
    private NewsHeadlinesThread newsHeadlinesThread;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        currentFragmentName = getTag();
        HtmlCutter.setLanguage(Locale.getDefault().getLanguage());
        update();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        View rootView = inflater.inflate(R.layout.standard_list_layout, null);
        String[] from = {"date", "text"};
        int[] to = {R.id.itemNewsDate, R.id.itemNewsHeadline};
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
                update();
                return true;
            case R.id.action_set_russian:
                HtmlCutter.setLanguage(HtmlCutter.LANG_RU);
                update();
                return true;
            case R.id.action_set_english:
                HtmlCutter.setLanguage(HtmlCutter.LANG_EN);
                update();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onListItemClick(ListView list, View view, int position, long id) {
        Intent intent = new Intent(mContext, NewsContentActivity.class);
        intent.putExtra(NEWS_CONTENT_ID_NAME, position);
        startActivity(intent);
    }

    private void update() {
        if (newsHeadlinesThread != null) {
            newsHeadlinesThread.cancel(false);
        }
        newsHeadlinesThread = new NewsHeadlinesThread();
        newsHeadlinesThread.execute();
        CommonHelper.makeToastUpdating(mContext, currentFragmentName);
    }

    private class NewsHeadlinesThread extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            return HtmlCutter.loadNewsList();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!isAdded()) {
                return;
            }
            if (result.booleanValue() && HtmlCutter.setNewsList()) {
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
}
