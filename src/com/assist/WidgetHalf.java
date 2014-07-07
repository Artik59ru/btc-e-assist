package com.assist;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.RemoteViews;

import com.TradeApi.TradeApi;

public class WidgetHalf extends AppWidgetProvider {
	private static TradeApi tradeApi = new TradeApi();

	private static AppWidgetManager mainWidgetManager;
	private static int mainWidgetId;
	private static RemoteViews mainWidgetView;
	private static PrefControl mainPControl;
	private static int[] cellsNamesIds = { R.id.widgetHalfName1,
			R.id.widgetHalfName2, R.id.widgetHalfName3, R.id.widgetHalfName4 };
	private static int[] cellsValuesIds = { R.id.widgetHalfValue1,
			R.id.widgetHalfValue2, R.id.widgetHalfValue3, R.id.widgetHalfValue4 };
	private static ArrayList<String> cacheList;

	@Override
	public void onEnabled(Context context) {
	}

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager,
			int[] appWidgetIds) {
		mainWidgetManager = appWidgetManager;

		if (mainPControl == null)
			mainPControl = PrefControl.getInstance();

		for (int i : appWidgetIds) {
			mainWidgetId = i;
			Intent updateIntent = new Intent(context, WidgetHalf.class);
			updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
			updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
					appWidgetIds);
			PendingIntent pIntent = PendingIntent.getBroadcast(context, i,
					updateIntent, 0);
			mainWidgetView = new RemoteViews(context.getPackageName(),
					R.layout.widget_half);
			mainWidgetView.setOnClickPendingIntent(R.id.widgetHalfButton,
					pIntent);
			updateWidget();
			appWidgetManager.updateAppWidget(i, mainWidgetView);
		}

	}

	public static void updateWidget() {
		String[] configPairs = mainPControl
				.getWidgetPairsSettings(mainWidgetId);
		int transparencyValue = mainPControl
				.getWidgetTransparencySettings(mainWidgetId);
		mainWidgetView.setInt(R.id.widgetHalfImageView, "setAlpha",
				transparencyValue);
		mainWidgetView.setInt(R.id.widgetHalfLayoutName1, "setBackgroundColor",
				Color.argb(transparencyValue, 72, 61, 139));
		mainWidgetView.setInt(R.id.widgetHalfLayoutName2, "setBackgroundColor",
				Color.argb(transparencyValue, 72, 61, 139));
		if (configPairs.length == 0) {
			return;
		}
		if (configPairs[0].equals(PrefControl.EMPTY)) {
			return;
		} else {
			mainWidgetView.setViewVisibility(R.id.widgetHalfProgressBar,
					View.VISIBLE);
			mainWidgetManager.updateAppWidget(mainWidgetId, mainWidgetView);
			new SecondThread().execute(configPairs);
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		if (mainPControl != null) {
			for (int i : appWidgetIds) {
				mainPControl.deleteWidgetSettings(i);
			}
		}
	}

	@Override
	public void onDisabled(Context context) {

	}

	static class SecondThread extends
			AsyncTask<String[], Void, ArrayList<String>> {
		@Override
		protected ArrayList<String> doInBackground(String[]... arg) {
			return getPrices(arg[0]);
		}

		@Override
		protected void onPostExecute(ArrayList<String> dataList) {
			int i = 0;
			boolean sw = true;
			if (dataList == null) {
				dataList = cacheList;
			}
			if (dataList != null) {
				for (Iterator<String> it = dataList.iterator(); it.hasNext();) {
					String buffer = it.next();
					if (!it.hasNext()) {
						mainWidgetView.setTextViewText(R.id.widgetHalfTime,
								buffer);
						break;
					}
					if (sw) {
						mainWidgetView
								.setTextViewText(cellsNamesIds[i], buffer);
					} else {
						mainWidgetView.setTextViewText(cellsValuesIds[i],
								buffer);
						if (i < 3) {
							i++;
						}
					}
					sw = sw ? false : true;
				}
				cacheList = dataList;
			}
			mainWidgetView.setViewVisibility(R.id.widgetHalfProgressBar,
					View.INVISIBLE);
			mainWidgetManager.updateAppWidget(mainWidgetId, mainWidgetView);
		}
	}

	// return array as {[pair],[value],[pair],[value],...[date]},last element is
	// date
	private static ArrayList<String> getPrices(String[] pairsList) {
		try {

			if (tradeApi == null) {
				tradeApi = new TradeApi();
			}

			tradeApi.ticker.resetParams();
			for (String s : pairsList) {
				tradeApi.ticker.addPair(s);
			}

			if (!tradeApi.ticker.runMethod()) {
				tradeApi.ticker.runMethod();
			}
			if (tradeApi.ticker.isSuccess()) {
				ArrayList<String> resultList = new ArrayList<String>();
				while (tradeApi.ticker.hasNextPair()) {
					tradeApi.ticker.switchNextPair();
					resultList.add(tradeApi.ticker.getCurrentPairName());
					resultList.add(tradeApi.ticker.getCurrentLast());
				}
				long timestamp = Long.parseLong(tradeApi.ticker
						.getCurrentUpdated()) * 1000;
				DateFormat formatter = DateFormat.getTimeInstance();
				resultList.add(formatter.format(new Date(timestamp)));
				return resultList;
			} else {
				return null;
			}
		} catch (Exception e) {
		}
		return null;
	}
}
