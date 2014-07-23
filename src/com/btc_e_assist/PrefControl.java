package com.btc_e_assist;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

public class PrefControl {
	private static volatile PrefControl mPrefControl;
	public static final String PREFERENCES_FILE_NAME = "btce_assist_preferences";
	public static final String PAIRS_STRING = "PAIRS_LIST";
	public static final String EMPTY = "EMPTY";
	public static final Long EMPTY_LONG = Long.MIN_VALUE;
	public static final String PAIRS_STRING_UPDATED_SECONDS = "PAIRS_LIST_UPDATED";
	public static final String WIDGET_PAIRS_SETTINGS_NAME = "WIDGET_PAIRS_SETTINGS:";
	public static final String WIDGET_CONTENT_NAME = "WIDGET_CONTENT:";
	public static final String WIDGET_TRANSPARENCY_SETTINGS_NAME = "WIDGET_TRANSPARENCY_SETTINGS:";
	public static final String PROFILE_CURRENT_ID = "CURRENT_PROFILE_DB_ID";
	private SharedPreferences preferences;

	private PrefControl() {
		preferences = App.context.getSharedPreferences(PREFERENCES_FILE_NAME,
				Context.MODE_PRIVATE);
	}

	public synchronized static PrefControl getInstance() {
		if (mPrefControl == null) {
			mPrefControl = new PrefControl();
		}
		return mPrefControl;
	}

	public synchronized SharedPreferences getSharedPreferences() {
		preferences = PreferenceManager
				.getDefaultSharedPreferences(App.context);
		return preferences;
	}

	public boolean isSavedPairsList() {
		if (preferences.getString(PAIRS_STRING, EMPTY).equals(EMPTY)) {
			return false;
		} else {
			return true;
		}
	}

	public synchronized boolean updatePairsList() {
		try {
			TradeControl tradeControl = TradeControl.getInstance();
			Editor editPref = preferences.edit();
			StringBuilder pairs = new StringBuilder();
			if (!tradeControl.tradeApi.info.runMethod()) {
				if (!tradeControl.tradeApi.info.runMethod()) {
					return false;
				}
			}
			for (String s : tradeControl.tradeApi.info.getPairsList()) {
				pairs.append(s);
				pairs.append(';');
			}
			editPref.putString(PAIRS_STRING, pairs.toString());
			editPref.putString(PAIRS_STRING_UPDATED_SECONDS,
					String.valueOf(tradeControl.tradeApi.info
							.getLocalTimestamp() / 1000));
			editPref.commit();
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Trying to get pairs from the preferences, otherwise return temporary list
	 */
	public String[] getPairsList() {
		String[] result = preferences.getString(PAIRS_STRING, EMPTY).split(";");
		if (result.length != 0) {
			if (result[0].split("-").length != 2) {
				String[] tempPairsList = { "BTC-USD", "BTC-EUR", "LTC-BTC",
						"LTC-USD", "LTC-EUR", "NMC-BTC", "NMC-USD", "NVC-BTC",
						"NVC-USD", "EUR-USD" };
				return tempPairsList;
			} else
				return result;
		}
		return null;
	}

	public long getPairsListUpdatedSeconds() {
		String time = preferences
				.getString(PAIRS_STRING_UPDATED_SECONDS, EMPTY);
		if (time.equals(EMPTY)) {
			return 0;
		} else {
			return Long.parseLong(time);
		}
	}

	/**
	 * Receive array of parameters, concatenate to string and save to
	 * preferences
	 */
	public synchronized void setWidgetSettings(String[] params,
			int transparencyValue, int widgetId) {
		Editor editPref = preferences.edit();
		StringBuilder pairs = new StringBuilder();
		for (String s : params) {
			pairs.append(s);
			pairs.append(';');
		}
		editPref.putString(WIDGET_PAIRS_SETTINGS_NAME + widgetId,
				pairs.toString());
		editPref.putString(WIDGET_TRANSPARENCY_SETTINGS_NAME + widgetId,
				String.valueOf(transparencyValue));
		editPref.commit();
	}

	/**
	 * Save content of widget to preferences
	 * 
	 * @param list
	 *            widget data
	 * @param widgetId
	 *            widget id
	 */
	public synchronized void setWidgetContent(ArrayList<String> list,
			int widgetId) {
		Editor editPref = preferences.edit();
		StringBuilder data = new StringBuilder();
		for (String s : list) {
			data.append(s);
			data.append(';');
		}
		editPref.putString(WIDGET_CONTENT_NAME + widgetId, data.toString());
		editPref.commit();
	}

	/**
	 * Return values, separated by ';'
	 */
	public String[] getWidgetPairsSettings(int widgetId) {
		return preferences.getString(
				WIDGET_PAIRS_SETTINGS_NAME + String.valueOf(widgetId), EMPTY)
				.split(";");
	}

	public ArrayList<String> getWidgetContent(int widgetId) {
		ArrayList<String> resultList = new ArrayList<String>();
		String stringFromPref = preferences.getString(WIDGET_CONTENT_NAME
				+ String.valueOf(widgetId), EMPTY);
		if (!stringFromPref.equals(EMPTY)) {
			for (String s : stringFromPref.split(";")) {
				resultList.add(s);
			}
		}
		return resultList;
	}

	public int getWidgetTransparencySettings(int widgetId) {
		int result;
		try {
			result = Integer.parseInt(preferences.getString(
					WIDGET_TRANSPARENCY_SETTINGS_NAME
							+ String.valueOf(widgetId), EMPTY));
		} catch (Exception e) {
			result = Integer.MIN_VALUE;
		}
		return result;
	}

	public synchronized void deleteWidgetSettings(int widgetId) {
		Editor editPref = preferences.edit();
		editPref.remove(WIDGET_PAIRS_SETTINGS_NAME + widgetId);
		editPref.remove(WIDGET_TRANSPARENCY_SETTINGS_NAME + widgetId);
		editPref.commit();
	}

	public synchronized void setCurrentProfileId(long id) {
		Editor editPref = preferences.edit();
		editPref.putLong(PROFILE_CURRENT_ID, id);
		editPref.commit();
	}

	public synchronized void deleteCurrentProfileId() {
		Editor editPref = preferences.edit();
		editPref.remove(PROFILE_CURRENT_ID);
		editPref.commit();
	}

	/**
	 * Return Long.MIN_VALUE if current profile id is not installed
	 * 
	 * @return
	 */
	public long getCurrentProfileId() {
		return preferences.getLong(PROFILE_CURRENT_ID, EMPTY_LONG);
	}

	public synchronized void setAllPreferences() {
		SharedPreferences sharedPreferences = PreferenceManager
				.getDefaultSharedPreferences(App.context);

		TradeControl.tradesCount = Integer.parseInt(sharedPreferences
				.getString("trades_transactions_count", "200"));
		TradeControl.tradeHistoryCount = sharedPreferences.getString(
				"trades_transactions_count", "200");
		TradeControl.transHistoryCount = sharedPreferences.getString(
				"trades_transactions_count", "200");

		TradeControl.depthCount = Integer.parseInt(sharedPreferences.getString(
				"depth_count", "60"));

		HtmlCutter.setNewsCount(Integer.parseInt(sharedPreferences.getString(
				"news_count", "10")));

		ServiceAssist.sleepValue = Long.parseLong(sharedPreferences.getString(
				"trades_transactions_count", "60000"));
	}
}
