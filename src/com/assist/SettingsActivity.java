package com.assist;

import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceActivity;

public class SettingsActivity extends PreferenceActivity implements
		OnSharedPreferenceChangeListener {
	@SuppressWarnings("deprecation")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if (key.equals("trades_transactions_count")) {
			TradeControl.tradesCount = Integer.parseInt(sharedPreferences
					.getString(key, "200"));
			TradeControl.tradeHistoryCount = sharedPreferences.getString(key,
					"200");
			TradeControl.transHistoryCount = sharedPreferences.getString(key,
					"200");
		} else if (key.equals("depth_count")) {
			TradeControl.depthCount = Integer.parseInt(sharedPreferences
					.getString(key, "60"));
		} else if (key.equals("news_count")) {
			HtmlCutter.setNewsCount(Integer.parseInt(sharedPreferences
					.getString(key, "10")));
		} else if (key.equals("trades_transactions_count")) {
			ServiceAssist.sleepValue = Long.parseLong(sharedPreferences
					.getString(key, "60000"));
		}
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onResume() {
		super.onResume();
		getPreferenceScreen().getSharedPreferences()
				.registerOnSharedPreferenceChangeListener(this);
	}

	@SuppressWarnings("deprecation")
	@Override
	protected void onPause() {
		super.onPause();
		getPreferenceScreen().getSharedPreferences()
				.unregisterOnSharedPreferenceChangeListener(this);
	}
}
