package com.btc_e_assist;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.text.InputType;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends ActionBarActivity {
	private TradeControl tControl = TradeControl.getInstance();
	private PrefControl pControl = PrefControl.getInstance();
	private static final long PAIRS_LIST_AGE_SECONDS = 300;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		startService(new Intent(this, ServiceAssist.class));

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
		PreferenceManager.setDefaultValues(MainActivity.this,
				R.xml.preferences, false);

		Thread initThread = new Thread(new Runnable() {
			@Override
			public void run() {
				pControl.setAllPreferences();
				long delta = System.currentTimeMillis() / 1000
						- pControl.getPairsListUpdatedSeconds();
				if (delta > PAIRS_LIST_AGE_SECONDS) {
					pControl.updatePairsList();
				}
				if (!tControl.tradeApi.info.isSuccess()) {
					tControl.tradeApi.info.runMethod();
				}
			}
		});
		initThread.start();
		showPasswordDialog();
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

	private void showPasswordDialog() {
		if (pControl == null) {
			return;
		}
		final long currentId = pControl.getCurrentProfileId();
		if (currentId != PrefControl.EMPTY_LONG
				&& !tControl.tradeApi.isKeysInstalled()) {
			DBControl dbControl = DBControl.getInstance();
			Cursor profilesData = dbControl.getProfilesDataWithId(currentId);
			if (profilesData == null) {
				return;
			}
			profilesData.moveToNext();
			final String nameFromCursor = profilesData.getString(profilesData
					.getColumnIndex(DBControl.PROFILES_NAME_NAME));
			final String keyFromCursor = profilesData.getString(profilesData
					.getColumnIndex(DBControl.PROFILES_NAME_KEY));
			final String secretFromCursor = profilesData.getString(profilesData
					.getColumnIndex(DBControl.PROFILES_NAME_SECRET));
			int isEncoded = profilesData.getInt(profilesData
					.getColumnIndex(DBControl.PROFILES_NAME_IS_ENCODED));
			if (isEncoded == 0) {
				try {
					tControl.tradeApi.setKeys(keyFromCursor, secretFromCursor);
				} catch (Exception e) {
					Toast.makeText(this, R.string.wrong_api_key_or_secret,
							Toast.LENGTH_SHORT).show();
				}
			} else {
				AlertDialog.Builder passDialog = new AlertDialog.Builder(this);
				passDialog.setTitle(this
						.getString(R.string.decryption_dialog_title));
				passDialog.setMessage(String.format(
						getString(R.string.decryption_dialog_message),
						nameFromCursor));
				final EditText passEditText = new EditText(this);
				passEditText.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				passEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);
				passDialog.setView(passEditText);
				passDialog.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									tControl.tradeApi.setKeys(keyFromCursor,
											secretFromCursor, passEditText
													.getText().toString());
									Toast.makeText(MainActivity.this,
											R.string.accepted_dialog_message,
											Toast.LENGTH_SHORT).show();
								} catch (Exception e) {
									Toast.makeText(MainActivity.this,
											R.string.wrong_password,
											Toast.LENGTH_SHORT).show();
									showPasswordDialog();
								}
							}
						});
				passDialog.show();
			}
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