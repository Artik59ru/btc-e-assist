package com.btc_e_assist;

import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

public class WidgetHalfConfigActivity extends ActionBarActivity {
    Intent result;
    int widgetId;
    PrefControl pControl;
    ArrayAdapter<String> spinnerAdapter;
    String[] pairs;
    Spinner cell1;
    Spinner cell2;
    Spinner cell3;
    Spinner cell4;
    SeekBar seekBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        result = getIntent();
        Bundle extras = result.getExtras();
        widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
        }
        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
        result = new Intent();
        result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_CANCELED, result);

        setContentView(R.layout.activity_widget_half_config);

        pControl = PrefControl.getInstance();
        seekBar = (SeekBar) findViewById(R.id.widgetHalfConfigSeekBar);
        seekBar.setMax(255);
        cell1 = (Spinner) findViewById(R.id.widgetHalfConfigName1);
        cell2 = (Spinner) findViewById(R.id.widgetHalfConfigName2);
        cell3 = (Spinner) findViewById(R.id.widgetHalfConfigName3);
        cell4 = (Spinner) findViewById(R.id.widgetHalfConfigName4);
        refreshCells();
        if (!pControl.isSavedPairsList()) {
            Toast.makeText(this, getString(R.string.pair_list_not_available),
                    Toast.LENGTH_LONG).show();
            new SecondThread().execute();
        }
    }

    public void onClick(View v) {
        String[] pairsList = {cell1.getSelectedItem().toString(),
                cell2.getSelectedItem().toString(),
                cell3.getSelectedItem().toString(),
                cell4.getSelectedItem().toString()};
        int seekValue = 255 - seekBar.getProgress();
        pControl.setWidgetSettings(pairsList, seekValue, widgetId);
        setResult(RESULT_OK, result);
        WidgetHalf.updateWidget();// FIX, otherwise android bug with onUpdate
        // BEFORE
        // config activity
        finish();
    }

    void refreshCells() {
        pairs = pControl.getPairsList();
        spinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, pairs);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cell1.setAdapter(spinnerAdapter);
        cell2.setAdapter(spinnerAdapter);
        cell3.setAdapter(spinnerAdapter);
        cell4.setAdapter(spinnerAdapter);
        cell1.setSelection(0);
        cell2.setSelection(1);
        cell3.setSelection(2);
        cell4.setSelection(3);
    }

    class SecondThread extends AsyncTask<Void, Void, Boolean> {
        @Override
        protected Boolean doInBackground(Void... arg) {
            return pControl.updatePairsList();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                CommonHelper.makeToastUpdated(WidgetHalfConfigActivity.this,
                        getTitle().toString());
                refreshCells();
            } else {
                CommonHelper.makeToastErrorConnection(
                        WidgetHalfConfigActivity.this, getTitle().toString());
            }
        }
    }
}
