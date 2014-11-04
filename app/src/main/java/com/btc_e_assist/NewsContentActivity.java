package com.btc_e_assist;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class NewsContentActivity extends ActionBarActivity {
	private int id;
	private TextView textContent;
	private TextView textLabel;
	private NewsTextThread newsTextThread;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_news_content);
		getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		textContent = (TextView) findViewById(R.id.newsContentData);
		textContent.setMovementMethod(new ScrollingMovementMethod());
		textLabel = (TextView) findViewById(R.id.newsContentLabel);
		textLabel.setTypeface(CommonHelper.fontRobotoLight);
		Intent intent = getIntent();
		id = intent.getIntExtra(NewsFragment.NEWS_CONTENT_ID_NAME, 0);
		textLabel.setText((String) HtmlCutter.newsData.get(id).get("date"));
		Button buttonNext = (Button) findViewById(R.id.newsContentNextButton);
		buttonNext.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (id < HtmlCutter.newsData.size() - 1) {
					id++;
					update();
					textLabel.setText((String) HtmlCutter.newsData.get(id).get(
							"date"));
				}

			}
		});
		Button buttonPrev = (Button) findViewById(R.id.newsContentPrevButton);
		buttonPrev.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (id > 0) {
					id--;
					update();
					textLabel.setText((String) HtmlCutter.newsData.get(id).get(
							"date"));
				}

			}
		});
		update();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.news_actions, menu);
		return super.onCreateOptionsMenu(menu);
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

	private void update() {
		if (newsTextThread != null) {
			newsTextThread.cancel(false);
		}
		newsTextThread = new NewsTextThread();
		newsTextThread.execute();
		CommonHelper.makeToastUpdating(this,
				getString(R.string.title_activity_news_content));
	}

	class NewsTextThread extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... arg0) {
			return HtmlCutter.runGettingNewsContent(id);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result.booleanValue()) {
				textContent.setText(HtmlCutter.news);
				CommonHelper.makeToastUpdated(NewsContentActivity.this,
						getString(R.string.title_activity_news_content));
			} else {
				CommonHelper.makeToastErrorConnection(NewsContentActivity.this,
						getString(R.string.title_activity_news_content));
			}
		}
	}
}