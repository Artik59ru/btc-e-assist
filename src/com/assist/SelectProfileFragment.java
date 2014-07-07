package com.assist;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.ResourceCursorAdapter;
import android.text.InputType;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class SelectProfileFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	public static final int LOADER_ID = 0;
	private static final int CONTEXT_MENU_ID = 0;

	private String nameFromCursor, keyFromCursor, secretFromCursor;

	private Context mContext;
	private DBControl dbControl;
	private TradeControl tradeControl;
	private PrefControl pControl;
	private CustomAdapter cursorAdapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		tradeControl = TradeControl.getInstance(mContext);
		pControl = PrefControl.getInstance();
		dbControl = DBControl.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.standard_list_layout, null);
		cursorAdapter = new CustomAdapter(mContext,
				R.layout.item_select_profile, null, 0);
		setListAdapter(cursorAdapter);
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null,
				this);
		getActivity().getSupportLoaderManager().getLoader(LOADER_ID)
				.forceLoad();
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		registerForContextMenu(getListView());
	}

	@Override
	public void onListItemClick(ListView list, View view, int position, long id) {
		int isEncoded;
		Cursor profilesData = dbControl.getProfilesDataWithId(id);
		if (profilesData == null) {
			return;
		}
		profilesData.moveToFirst();
		nameFromCursor = profilesData.getString(profilesData
				.getColumnIndex(DBControl.PROFILES_NAME_NAME));
		keyFromCursor = profilesData.getString(profilesData
				.getColumnIndex(DBControl.PROFILES_NAME_KEY));
		secretFromCursor = profilesData.getString(profilesData
				.getColumnIndex(DBControl.PROFILES_NAME_SECRET));
		isEncoded = profilesData.getInt(profilesData
				.getColumnIndex(DBControl.PROFILES_NAME_IS_ENCODED));
		if (isEncoded == 1) {
			showPassDialog(id);
		} else if (isEncoded == 0) {
			try {
				tradeControl.tradeApi.setKeys(keyFromCursor, secretFromCursor);
				pControl.setCurrentProfileId(id);
				getActivity().getSupportLoaderManager().getLoader(LOADER_ID)
						.forceLoad();
				Toast.makeText(mContext, R.string.profile_is_enabled,
						Toast.LENGTH_SHORT).show();
			} catch (Exception e) {
			}
		}
	}

	private void showPassDialog(final long id) {
		AlertDialog.Builder passDialog = new AlertDialog.Builder(mContext);
		passDialog.setTitle(getString(R.string.decryption_dialog_title));
		passDialog.setMessage(String.format(
				getString(R.string.decryption_dialog_message), nameFromCursor));
		final EditText passEditText = new EditText(mContext);
		passEditText.setInputType(InputType.TYPE_CLASS_TEXT
				| InputType.TYPE_TEXT_VARIATION_PASSWORD);
		passDialog.setView(passEditText);
		passDialog.setPositiveButton("OK",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int which) {
						try {
							tradeControl.tradeApi.setKeys(keyFromCursor,
									secretFromCursor, passEditText.getText()
											.toString());
							pControl.setCurrentProfileId(id);
							getActivity().getSupportLoaderManager()
									.getLoader(LOADER_ID).forceLoad();
							Toast.makeText(mContext,
									R.string.accepted_dialog_message,
									Toast.LENGTH_SHORT).show();
						} catch (Exception e) {
							Toast.makeText(mContext, R.string.wrong_password,
									Toast.LENGTH_SHORT).show();
						}
					}
				});
		passDialog.show();
	}

	public void onCreateContextMenu(ContextMenu menu, View view,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, view, menuInfo);
		menu.add(0, CONTEXT_MENU_ID, 0, getString(R.string.delete));
	}

	public boolean onContextItemSelected(MenuItem item) {
		if (item.getItemId() == CONTEXT_MENU_ID) {
			AdapterContextMenuInfo contMenuAdapter = (AdapterContextMenuInfo) item
					.getMenuInfo();
			dbControl.deleteProfile(contMenuAdapter.id);
			if (contMenuAdapter.id == pControl.getCurrentProfileId()) {
				pControl.deleteCurrentProfileId();
				tradeControl.tradeApi.resetInstalledKeys();
			}
			getActivity().getSupportLoaderManager().getLoader(LOADER_ID)
					.forceLoad();
			return true;
		}
		return super.onContextItemSelected(item);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new MyCursorLoader(mContext, dbControl);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor crs) {
		cursorAdapter.swapCursor(crs);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		cursorAdapter.swapCursor(null);
	}

	private static class MyCursorLoader extends CursorLoader {

		DBControl mDBControl;

		public MyCursorLoader(Context context, DBControl db) {
			super(context);
			mDBControl = db;
		}

		@Override
		public Cursor loadInBackground() {
			return mDBControl.getProfilesData();
		}
	}

	private class CustomAdapter extends ResourceCursorAdapter {
		public CustomAdapter(Context context, int layout, Cursor c, int flags) {
			super(context, layout, c, flags);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			if (cursor != null) {
				int _idIndex = cursor
						.getColumnIndex(DBControl.PROFILES_NAME_ID);
				int nameIndex = cursor
						.getColumnIndex(DBControl.PROFILES_NAME_NAME);
				int isEncodedIndex = cursor
						.getColumnIndex(DBControl.PROFILES_NAME_IS_ENCODED);
				TextView nameView = (TextView) view
						.findViewById(R.id.itemSelectProfileName);
				CheckedTextView checkView = (CheckedTextView) view
						.findViewById(R.id.itemSelectProfileChecked);
				if (cursor.getInt(_idIndex) == pControl.getCurrentProfileId()) {
					checkView.setChecked(true);
				} else {
					checkView.setChecked(false);
				}
				int isEncoded = cursor.getInt(isEncodedIndex);
				if (isEncoded == 1) {
					Drawable img = mContext.getResources().getDrawable(
							R.drawable.secure);
					nameView.setCompoundDrawablesWithIntrinsicBounds(img, null,
							null, null);
				} else {
					Drawable img = mContext.getResources().getDrawable(
							R.drawable.not_secure);
					nameView.setCompoundDrawablesWithIntrinsicBounds(img, null,
							null, null);
				}
				nameView.setText(cursor.getString(nameIndex));
			}
		}
	}
}
