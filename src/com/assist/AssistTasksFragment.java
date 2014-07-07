package com.assist;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class AssistTasksFragment extends ListFragment implements
		LoaderCallbacks<Cursor> {
	public static final int LOADER_ID = 1;

	private Context mContext;
	private DBControl dbControl;
	private CheckBoxCursorAdapter adapter;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		dbControl = DBControl.getInstance();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.standard_list_layout, null);
		String[] from = new String[] { DBControl.ACTIONS_NAME_OPTION_4_TEXT,
				DBControl.ACTIONS_NAME_OPTION_5_TEXT,
				DBControl.ACTIONS_NAME_VALUE_FLOAT };
		int[] to = new int[] { R.id.itemAssistTaskOption4,
				R.id.itemAssistTaskOption5, R.id.itemAssistTaskValue };
		String[] aliases = { AssistAlarmFragment.CONDITION_HIGHER,
				AssistAlarmFragment.CONDITION_LOWER };
		String[] aliasesValues = getResources().getStringArray(
				R.array.assist_alarm_conditions);
		adapter = new CheckBoxCursorAdapter(mContext,
				R.layout.item_assist_task_list, null, from, to,
				DBControl.ACTIONS_NAME_OPTION_5_TEXT, aliases, aliasesValues,
				R.id.itemAssistTaskCheckBox, 0);
		setListAdapter(adapter);
		getActivity().getSupportLoaderManager().initLoader(LOADER_ID, null,
				this);
		getActivity().getSupportLoaderManager().getLoader(LOADER_ID)
				.forceLoad();
		return rootView;
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		inflater.inflate(R.menu.assist_tasks_actions, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		ArrayList<Long> checkedRowIds = new ArrayList<Long>();
		ListView list = getListView();
		switch (item.getItemId()) {
		case R.id.action_cancel:
			ArrayList<Long> idList = new ArrayList<Long>();
			for (Integer position : adapter.checkData) {
				checkedRowIds.add(list.getItemIdAtPosition(position));
			}
			for (long rowId : checkedRowIds) {
				dbControl.deleteAlarm(rowId);
				idList.add(rowId);
			}
			ServiceAssist.globalDeleteList = idList;
			Intent serviceIntent = new Intent(mContext, ServiceAssist.class);
			serviceIntent.putExtra(ServiceAssist.TASK_EXTRA_NAME,
					ServiceAssist.TASK_FOR_DELETE);
			mContext.startService(serviceIntent);
			adapter.checkData.clear();
			getActivity().getSupportLoaderManager().getLoader(LOADER_ID)
					.forceLoad();
			return true;
		case R.id.action_select_all:
			selectAll();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void selectAll() {
		int count = adapter.getCount();
		int sizeChecks = adapter.checkData.size();
		adapter.checkData.clear();
		if (sizeChecks != count) {
			for (int i = 0; i < count; i++) {
				adapter.checkData.add(i);
			}
		}
		getActivity().getSupportLoaderManager().getLoader(LOADER_ID)
				.forceLoad();
	}

	@Override
	public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
		return new MyCursorLoader(mContext, dbControl);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor crs) {
		adapter.swapCursor(crs);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		adapter.swapCursor(null);
	}

	private static class MyCursorLoader extends CursorLoader {

		DBControl mDBControl;

		public MyCursorLoader(Context context, DBControl db) {
			super(context);
			mDBControl = db;
		}

		@Override
		public Cursor loadInBackground() {
			return mDBControl.getAlarmsData();
		}
	}
}
