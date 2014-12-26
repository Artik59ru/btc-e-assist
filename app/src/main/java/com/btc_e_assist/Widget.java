package com.btc_e_assist;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.view.View;
import android.widget.RemoteViews;

import com.assist.TradeApi;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Widget extends AppWidgetProvider {
    private static final int[] cellsNamesIds = {R.id.widgetName1,
            R.id.widgetName2, R.id.widgetName3, R.id.widgetName4,
            R.id.widgetName5, R.id.widgetName6, R.id.widgetName7,
            R.id.widgetName8, R.id.widgetName9, R.id.widgetName10};
    private static final int[] cellsValuesIds = {R.id.widgetValue1,
            R.id.widgetValue2, R.id.widgetValue3, R.id.widgetValue4,
            R.id.widgetValue5, R.id.widgetValue6, R.id.widgetValue7,
            R.id.widgetValue8, R.id.widgetValue9, R.id.widgetValue10};
    private static TradeApi tradeApi = new TradeApi();
    private static AppWidgetManager mainWidgetManager;
    private static int mainWidgetId;
    private static RemoteViews mainWidgetView;
    private static PrefControl mainPControl;

    public static void updateWidget() {
        String[] configPairs = mainPControl
                .getWidgetPairsSettings(mainWidgetId);
        int transparencyValue = mainPControl
                .getWidgetTransparencySettings(mainWidgetId);
        mainWidgetView.setInt(R.id.widgetImageView, "setAlpha",
                transparencyValue);
        mainWidgetView.setInt(R.id.widgetLayoutName1, "setBackgroundColor",
                Color.argb(transparencyValue, 72, 61, 139));
        mainWidgetView.setInt(R.id.widgetLayoutName2, "setBackgroundColor",
                Color.argb(transparencyValue, 72, 61, 139));
        if (configPairs.length == 0 || configPairs[0].equals(PrefControl.EMPTY)) {
            return;
        }
        mainWidgetView.setViewVisibility(R.id.widgetProgressBar, View.VISIBLE);
        mainWidgetManager.updateAppWidget(mainWidgetId, mainWidgetView);
        new SecondThread().execute(configPairs);
    }

    // return array as {[pair],[value],[pair],[value],...[date]},last element is
    // date
    private static ArrayList<String> getPrices(String[] pairsList) {
        ArrayList<String> resultList = new ArrayList<String>();
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
                while (tradeApi.ticker.hasNextPair()) {
                    tradeApi.ticker.switchNextPair();
                    resultList.add(tradeApi.ticker.getCurrentPairName());
                    resultList.add(tradeApi.ticker.getCurrentLast());
                }
                long timestamp = Long.parseLong(tradeApi.ticker
                        .getCurrentUpdated()) * 1000;
                DateFormat formatter = DateFormat.getTimeInstance();
                resultList.add(formatter.format(new Date(timestamp)));
            }
        } catch (Exception e) {
        }
        return resultList;
    }

    @Override
    public void onEnabled(Context context) {
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        mainWidgetManager = appWidgetManager;

        if (mainPControl == null) {
            mainPControl = PrefControl.getInstance();
        }
        for (int i : appWidgetIds) {
            mainWidgetId = i;
            Intent updateIntent = new Intent(context, Widget.class);
            updateIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,
                    appWidgetIds);
            PendingIntent pIntent = PendingIntent.getBroadcast(context, i,
                    updateIntent, 0);
            mainWidgetView = new RemoteViews(context.getPackageName(),
                    R.layout.widget);
            mainWidgetView.setOnClickPendingIntent(R.id.widgetButton, pIntent);
            updateWidget();
            appWidgetManager.updateAppWidget(i, mainWidgetView);
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

    private static class SecondThread extends
            AsyncTask<String[], Void, ArrayList<String>> {
        @Override
        protected ArrayList<String> doInBackground(String[]... arg) {
            return getPrices(arg[0]);
        }

        @Override
        protected void onPostExecute(ArrayList<String> dataList) {
            int i = 0;
            boolean isPref = false;
            boolean sw = true;
            if (dataList.size() == 0) {
                dataList = mainPControl.getWidgetContent(mainWidgetId);
                isPref = true;
            }
            for (Iterator<String> it = dataList.iterator(); it.hasNext(); ) {
                String buffer = it.next();
                if (!it.hasNext()) {
                    mainWidgetView.setTextViewText(R.id.widgetTime, buffer);
                    break;
                }
                if (sw) {
                    mainWidgetView.setTextViewText(cellsNamesIds[i], buffer);
                } else {
                    mainWidgetView.setTextViewText(cellsValuesIds[i], buffer);
                    if (i < 9) {
                        i++;
                    }
                }
                sw = sw ? false : true;
            }
            mainWidgetView.setViewVisibility(R.id.widgetProgressBar, View.GONE);
            mainWidgetManager.updateAppWidget(mainWidgetId, mainWidgetView);
            if (!isPref) {
                mainPControl.setWidgetContent(dataList, mainWidgetId);
            }
        }
    }
}
