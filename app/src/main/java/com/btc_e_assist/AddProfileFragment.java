package com.btc_e_assist;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.assist.TradeApi;

public class AddProfileFragment extends Fragment {
	private static final String apiKeysUrl = "https://btc-e.com/profile#api_keys";

	private Context mContext;
	private EditText name;
	private EditText key;
	private EditText secret;
	private EditText password;

	private DBControl dbControl;
	private TradeControl tradeControl;

	View rootView;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		tradeControl = TradeControl.getInstance();
		dbControl = DBControl.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.fragment_addprofile, container,
				false);
		TextView textLabel = (TextView) rootView
				.findViewById(R.id.addprofileTextLabel);
		TextView textTips = (TextView) rootView
				.findViewById(R.id.addprofileTips);
		Configuration configuration = getResources().getConfiguration();
		if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
				&& android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
			textLabel.setVisibility(View.GONE);
			textTips.setVisibility(View.GONE);
		} else {
			textLabel.setTypeface(CommonHelper.fontRobotoThin);
			textTips.setTypeface(CommonHelper.fontRobotoLight);
		}
		name = (EditText) rootView.findViewById(R.id.addprofileName);
		key = (EditText) rootView.findViewById(R.id.addprofileKey);
		secret = (EditText) rootView.findViewById(R.id.addprofileSecret);
		secret.setOnEditorActionListener(new EditTextAction());
		password = (EditText) rootView.findViewById(R.id.addprofilePassword);
		password.setOnEditorActionListener(new EditTextAction());
		Button button = (Button) rootView.findViewById(R.id.addProfileButton);
		button.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				addProfile();
			}
		});
		ImageView question1 = (ImageView) rootView
				.findViewById(R.id.addProfileQuestion1);
		ImageView question2 = (ImageView) rootView
				.findViewById(R.id.addProfileQuestion2);
		OnClickListener listener = new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Intent browserIntent = new Intent(Intent.ACTION_VIEW,
						Uri.parse(apiKeysUrl));
				startActivity(browserIntent);
			}
		};
		question1.setOnClickListener(listener);
		question2.setOnClickListener(listener);
		return rootView;
	}

	public void addProfile() {
		String nameText = name.getText().toString();
		if (nameText.length() == 0) {
			nameText = "API";
		}
		String keyText = key.getText().toString();
		String secretText = secret.getText().toString();
		String passwordText = password.getText().toString();

		try {
			if (passwordText.length() == 0) {
				dbControl.addProfile(nameText, keyText, secretText, false);
			} else {
				String encodedKey = tradeControl.tradeApi.encodeString(keyText,
						passwordText);
				String encodedSecret = tradeControl.tradeApi.encodeString(
						secretText, passwordText);
				dbControl.addProfile(nameText, encodedKey, encodedSecret, true);
			}
			Cursor profilesCursor = dbControl.getProfilesData();
			if (profilesCursor != null && profilesCursor.getCount() == 1) {
				profilesCursor.moveToFirst();
				long id = profilesCursor.getLong(profilesCursor
						.getColumnIndex(DBControl.PROFILES_NAME_ID));
				tradeControl.tradeApi.setKeys(keyText, secretText);
				PrefControl.getInstance().setCurrentProfileId(id);
				Toast.makeText(mContext, R.string.profile_is_enabled,
						Toast.LENGTH_SHORT).show();
			}
			new SecondThread().execute(nameText, keyText, secretText);
		} catch (Exception e) {
			Toast.makeText(mContext, R.string.wrong_api_key_or_secret,
					Toast.LENGTH_SHORT).show();
		}
		CommonHelper.clearAllEdits((ViewGroup) rootView
				.findViewById(R.id.addprofileLayout));
	}

	private class SecondThread extends AsyncTask<String, Void, Boolean> {
		String keyName;
		TradeApi tradeApiObject;

		@Override
		protected Boolean doInBackground(String... arg) {
			keyName = arg[0];
			try {
				tradeApiObject = new TradeApi(arg[1], arg[2]);
				return tradeApiObject.getInfo.runMethod();
			} catch (Exception e) {
			}
			return false;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (!isAdded()) {
				return;
			}
			String errorMessage = tradeApiObject.getInfo.getErrorMessage();
			StringBuilder message = new StringBuilder();
			if (!result.booleanValue()) {
				if (errorMessage.length() == 0) {
					return;
				} else {
					Toast.makeText(mContext, errorMessage, Toast.LENGTH_SHORT)
							.show();
				}
			} else {
				boolean info, trade;
				info = tradeApiObject.getInfo.isInfo();
				trade = tradeApiObject.getInfo.isTrade();
				message.append(String.format(
						getString(R.string.rights_message), keyName));
				message.append("\n");
				message.append(getString(R.string.trade));
				message.append(":");
				if (trade) {
					message.append(getString(R.string.yes));
				} else {
					message.append(getString(R.string.no));
				}
				message.append("\n");
				message.append(getString(R.string.info));
				message.append(":");
				if (info) {
					message.append(getString(R.string.yes));
				} else {
					message.append(getString(R.string.no));
				}
				Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
			}
		}
	}

	private class EditTextAction implements OnEditorActionListener {

		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {
				addProfile();
			}
			return false;
		}

	}
}
