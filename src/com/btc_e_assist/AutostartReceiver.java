package com.btc_e_assist;

import java.util.ArrayList;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

public class AutostartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Cursor cursor = DBControl.getInstance().getAlarmsData();
		if (cursor != null) {
			if (cursor.moveToFirst()) {
				int idIndex = cursor.getColumnIndex(DBControl.ACTIONS_NAME_ID);
				int pairNameIndex = cursor
						.getColumnIndex(DBControl.ACTIONS_NAME_OPTION_4_TEXT);
				int optionIndex = cursor
						.getColumnIndex(DBControl.ACTIONS_NAME_OPTION_5_TEXT);
				int targetIndex = cursor
						.getColumnIndex(DBControl.ACTIONS_NAME_VALUE_FLOAT);
				ArrayList<CheckableTask> list = new ArrayList<CheckableTask>();
				do {
					int id = cursor.getInt(idIndex);
					String name = cursor.getString(pairNameIndex);
					String option = cursor.getString(optionIndex);
					double target = cursor.getDouble(targetIndex);
					SimpleAlarmTask task = new SimpleAlarmTask(name, target,
							option, id);
					list.add(task);
				} while (cursor.moveToNext());
				if (list.size() > 0) {
					ServiceAssist.setTaskList(list);
				}
			}
		}
	}
}
