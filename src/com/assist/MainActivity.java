package com.assist;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

public class MainActivity extends ActionBarActivity {
	private TradeControl tradeControl;
	private PrefControl pControl;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(new Intent(this, ServiceAssist.class));
		tradeControl = TradeControl.getInstance(this);
		pControl = PrefControl.getInstance();

		Button button1_1 = (Button) findViewById(R.id.mainButton1_1);
		Button button1_2 = (Button) findViewById(R.id.mainButton1_2);
		Button button2_1 = (Button) findViewById(R.id.mainButton2_1);
		Button button2_2 = (Button) findViewById(R.id.mainButton2_2);
		Button buttonProfiles = (Button) findViewById(R.id.mainButtonProfiles);
		button1_1.setTypeface(CommonHelper.fontRobotoLight);
		button1_2.setTypeface(CommonHelper.fontRobotoLight);
		button2_1.setTypeface(CommonHelper.fontRobotoLight);
		button2_2.setTypeface(CommonHelper.fontRobotoLight);
		buttonProfiles.setTypeface(CommonHelper.fontRobotoLight);

		Configuration configuration = getResources().getConfiguration();
		if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
				&& (configuration.screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) > Configuration.SCREENLAYOUT_SIZE_NORMAL) {
			String string = getString(R.string.trades_main_button_land);
			button1_1.setText(Html.fromHtml(string));
			string = getString(R.string.myinfo_main_button_land);
			button1_2.setText(Html.fromHtml(string));
			string = getString(R.string.assist_main_button_land);
			button2_1.setText(Html.fromHtml(string));
			string = getString(R.string.chat_main_button_land);
			button2_2.setText(Html.fromHtml(string));
		}

		Thread initThread = new Thread(new Runnable() {
			@Override
			public void run() {
				PreferenceManager.setDefaultValues(MainActivity.this,
						R.xml.preferences, false);
				pControl.updatePairsList();
				pControl.setAllPreferences();
				tradeControl.tradeApi.info.runMethod();
			}
		});
		initThread.start();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Intent intent;
		switch (item.getItemId()) {
		case R.id.action_main_settings:
			intent = new Intent(this, SettingsActivity.class);
			startActivity(intent);
			return true;
		case R.id.action_main_about:
			intent = new Intent(this, AboutActivity.class);
			startActivity(intent);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	public void onButtonClicker(View view) {
		Intent intent;
		switch (view.getId()) {
		case R.id.mainButton1_1:
			intent = new Intent(this, TickerActivity.class);
			startActivity(intent);
			return;
		case R.id.mainButton1_2:
			intent = new Intent(this, FinancesActivity.class);
			startActivity(intent);
			return;
		case R.id.mainButton2_1:
			intent = new Intent(this, AssistActivity.class);
			startActivity(intent);
			return;
		case R.id.mainButton2_2:
			intent = new Intent(this, ChatActivity.class);
			startActivity(intent);
			return;
		case R.id.mainButtonProfiles:
			intent = new Intent(this, ProfileActivity.class);
			startActivity(intent);
			return;
		}
	}

}