package com.btc_e_assist;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;
import com.btc_e_assist.R;

public class WidgetConfigActivity extends Activity {
	Intent result;
	int widgetId;
	PrefControl pControl;
	ArrayAdapter<String> spinnerAdapter;
	String[] pairs;
	Spinner cell1;
	Spinner cell2;
	Spinner cell3;
	Spinner cell4;
	Spinner cell5;
	Spinner cell6;
	Spinner cell7;
	Spinner cell8;
	Spinner cell9;
	Spinner cell10;
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

		setContentView(R.layout.activity_widget_config);

		pControl = PrefControl.getInstance();
		seekBar = (SeekBar) findViewById(R.id.widgetConfigSeekBar);
		seekBar.setMax(255);
		cell1 = (Spinner) findViewById(R.id.widgetConfigName1);
		cell2 = (Spinner) findViewById(R.id.widgetConfigName2);
		cell3 = (Spinner) findViewById(R.id.widgetConfigName3);
		cell4 = (Spinner) findViewById(R.id.widgetConfigName4);
		cell5 = (Spinner) findViewById(R.id.widgetConfigName5);
		cell6 = (Spinner) findViewById(R.id.widgetConfigName6);
		cell7 = (Spinner) findViewById(R.id.widgetConfigName7);
		cell8 = (Spinner) findViewById(R.id.widgetConfigName8);
		cell9 = (Spinner) findViewById(R.id.widgetConfigName9);
		cell10 = (Spinner) findViewById(R.id.widgetConfigName10);
		refreshCells();
		if (!pControl.isSavedPairsList()) {
			Toast.makeText(this, getString(R.string.pair_list_not_available),
					Toast.LENGTH_LONG).show();
			new SecondThread().execute();
		}
	}

	public void onClick(View v) {
		String[] pairsList = { cell1.getSelectedItem().toString(),
				cell2.getSelectedItem().toString(),
				cell3.getSelectedItem().toString(),
				cell4.getSelectedItem().toString(),
				cell5.getSelectedItem().toString(),
				cell6.getSelectedItem().toString(),
				cell7.getSelectedItem().toString(),
				cell8.getSelectedItem().toString(),
				cell9.getSelectedItem().toString(),
				cell10.getSelectedItem().toString() };
		int seekValue = 255 - seekBar.getProgress();
		pControl.setWidgetSettings(pairsList, seekValue, widgetId);
		setResult(RESULT_OK, result);
		Widget.updateWidget();// FIX, otherwise android bug with onUpdate BEFORE
								// config activity
		finish();
	}

	private class SecondThread extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... arg) {
			return pControl.updatePairsList();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				CommonHelper.makeToastUpdated(WidgetConfigActivity.this,
						getTitle().toString());
				refreshCells();
			} else {
				CommonHelper.makeToastErrorConnection(
						WidgetConfigActivity.this, getTitle().toString());
			}
		}
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
		cell5.setAdapter(spinnerAdapter);
		cell6.setAdapter(spinnerAdapter);
		cell7.setAdapter(spinnerAdapter);
		cell8.setAdapter(spinnerAdapter);
		cell9.setAdapter(spinnerAdapter);
		cell10.setAdapter(spinnerAdapter);
		cell1.setSelection(0);
		cell2.setSelection(1);
		cell3.setSelection(2);
		cell4.setSelection(3);
		cell5.setSelection(4);
		cell6.setSelection(5);
		cell7.setSelection(6);
		cell8.setSelection(7);
		cell9.setSelection(8);
		cell10.setSelection(9);
	}
}
