package com.btc_e_assist;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.Typeface;
import android.text.InputType;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

public class CommonHelper {
	private static AlertDialog dialog;

	public static void showPasswordDialog(final Context context) {
		TradeControl tControl = TradeControl.getInstance();
		PrefControl pControl = PrefControl.getInstance();
		final long currentId = pControl.getCurrentProfileId();
		if (!tControl.tradeApi.isKeysInstalled()
				&& currentId != PrefControl.EMPTY_LONG) {
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
					tControl.assistTradeApi.setKeys(keyFromCursor,
							secretFromCursor);
				} catch (Exception e) {
					Toast.makeText(context, R.string.wrong_api_key_or_secret,
							Toast.LENGTH_SHORT).show();
				}
			} else {
				AlertDialog.Builder passDialog = new AlertDialog.Builder(
						context);
				passDialog.setTitle(context
						.getString(R.string.decryption_dialog_title));
				passDialog.setMessage(String.format(
						context.getString(R.string.decryption_dialog_message),
						nameFromCursor));
				final EditText passEditText = new EditText(context);
				passEditText.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				passEditText.setImeOptions(EditorInfo.IME_ACTION_DONE
						| EditorInfo.IME_FLAG_NO_EXTRACT_UI);
				passEditText
						.setOnEditorActionListener(new OnEditorActionListener() {
							@Override
							public boolean onEditorAction(TextView v,
									int actionId, KeyEvent event) {
								if (actionId == EditorInfo.IME_ACTION_DONE) {
									dialog.cancel();
									positiveDialogClick(context, keyFromCursor,
											secretFromCursor, passEditText
													.getText().toString());
								}
								return false;
							}
						});
				passDialog.setView(passEditText);
				passDialog.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								positiveDialogClick(context, keyFromCursor,
										secretFromCursor, passEditText
												.getText().toString());
							}
						});
				dialog = passDialog.create();
				dialog.show();
			}
		}
	}

	private static void positiveDialogClick(Context context, String key,
			String secret, String pass) {
		try {
			TradeControl tradeControl = TradeControl.getInstance();
			tradeControl.tradeApi.setKeys(key, secret, pass);
			tradeControl.assistTradeApi.setKeys(key, secret, pass);
			Toast.makeText(context, R.string.accepted_dialog_message,
					Toast.LENGTH_SHORT).show();
		} catch (Exception e) {
			Toast.makeText(context, R.string.wrong_password, Toast.LENGTH_SHORT)
					.show();
			showPasswordDialog(context);
		}
	}

	public static void clearAllEdits(ViewGroup group) {
		if (group != null) {
			int count = group.getChildCount();
			for (int i = 0; i < count; ++i) {
				View view = group.getChildAt(i);
				if (view instanceof EditText) {
					((EditText) view).setText("");
				}
			}
		}
	}

	public static void makeToastUpdating(Context context, String fragmentName) {
		Toast.makeText(context,
				fragmentName + ": " + context.getString(R.string.updating),
				Toast.LENGTH_SHORT).show();
	}

	public static void makeToastUpdated(Context context, String fragmentName) {
		Toast.makeText(context,
				fragmentName + ": " + context.getString(R.string.updated),
				Toast.LENGTH_SHORT).show();
	}

	public static void makeToastErrorConnection(Context context,
			String fragmentName) {
		Toast.makeText(
				context,
				fragmentName + ": "
						+ context.getString(R.string.error_connection),
				Toast.LENGTH_SHORT).show();
	}

	public static void makeToastNoKeys(Context context) {
		Toast.makeText(context, R.string.no_keys, Toast.LENGTH_SHORT).show();
	}

	public static void fillLeftList(String[] pairsList, List<String> namesList,
			ArrayAdapter<String> adapter) {
		String[] names;
		if (pairsList.length != 0) {
			namesList.clear();
			for (String s : pairsList) {
				names = s.split("-");
				if (!namesList.contains(names[0])) {
					namesList.add(names[0]);
				}
			}
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	public static void fillRightList(String[] pairsList,
			List<String> namesList, ArrayAdapter<String> adapter,
			String targetPair) {
		String[] componentsList;
		String targetName = targetPair.split("-")[0];
		if (targetPair.length() != 0) {
			namesList.clear();
			for (String s : pairsList) {
				componentsList = s.split("-");
				if (componentsList[0].equals(targetName)) {
					namesList.add(componentsList[1]);
				}
			}
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * Return position in spinners for target pair. If has troubles, return
	 * {0,0}
	 * 
	 * @param pairsList
	 * @param pair
	 * @return
	 */
	public static int[] getPositionsForPair(String[] pairsList, String pair) {
		int leftPosition = 0, rightPosition = 0;
		String[] inputNames = pair.split("-");
		String[] names;
		String prevName = "";
		int realCount = 0;
		int globalCount = 0;
		if (pairsList.length != 0 && inputNames.length == 2) {
			for (String s : pairsList) {
				names = s.split("-");
				if (!names[0].equals(prevName)) {
					realCount++;
					prevName = names[0];
				}
				if (names[0].equals(inputNames[0])) {
					leftPosition = realCount - 1;
					break;
				}
				globalCount++;
			}
			if (leftPosition >= 0) {
				realCount = 0;
				for (int i = globalCount; i < pairsList.length; i++) {
					names = pairsList[i].split("-");
					if (names[0].equals(inputNames[0])) {
						if (!names[1].equals(inputNames[1])) {
							realCount++;
						} else {
							break;
						}
					} else {
						break;
					}
				}
				rightPosition = realCount;
				int[] result = { leftPosition, rightPosition };
				return result;
			}
		}
		int[] result = { 0, 0 };
		return result;
	}

	public static final Typeface fontRobotoLight = Typeface.createFromAsset(
			App.context.getAssets(), "Roboto-Light.ttf");
	public static final Typeface fontRobotoThin = Typeface.createFromAsset(
			App.context.getAssets(), "Roboto-Thin.ttf");
}
