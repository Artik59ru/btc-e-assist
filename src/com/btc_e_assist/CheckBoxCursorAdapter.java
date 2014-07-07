package com.btc_e_assist;

import java.util.ArrayList;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * aliasColumnName - column name for row, where content alias
 */
class CheckBoxCursorAdapter extends ResourceCursorAdapter {
	public ArrayList<Integer> checkData;
	String[] mFrom;
	int[] mTo;
	String[] mAliases;
	String[] mAliasesValues;
	String mAliasColumnName;
	String aliasIndexContent;
	int mCheckBoxId;

	public CheckBoxCursorAdapter(Context context, int layout, Cursor c,
			String[] from, int[] to, String aliasColumnName, String[] aliases,
			String[] aliasesValues, int checkBoxId, int flags) {
		super(context, layout, c, flags);
		mFrom = from;
		mTo = to;
		mAliasColumnName = aliasColumnName;
		mAliases = aliases;
		mAliasesValues = aliasesValues;
		mCheckBoxId = checkBoxId;
		checkData = new ArrayList<Integer>();
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		if (cursor != null) {
			final int mPosition = cursor.getPosition();
			int count = mTo.length;
			int currentIndex;
			int aliasIndex = cursor.getColumnIndex(mAliasColumnName);
			aliasIndexContent = cursor.getString(aliasIndex);
			for (int i = 0; i < count; i++) {
				currentIndex = cursor.getColumnIndex(mFrom[i]);
				TextView v = (TextView) view.findViewById(mTo[i]);
				if (v != null) {
					if (currentIndex == aliasIndex) {
						int countAliases = mAliases.length;
						for (int y = 0; y < countAliases; y++) {
							if (mAliases[y].equals(aliasIndexContent)) {
								v.setText(mAliasesValues[y]);
								break;
							}
						}
						continue;
					}
					v.setText(cursor.getString(currentIndex));
				}
			}
			final CheckBox checkBox = (CheckBox) view.findViewById(mCheckBoxId);
			if (checkBox != null) {
				boolean isContains = checkData.contains(mPosition);
				if (isContains) {
					checkBox.setChecked(true);
				} else {
					checkBox.setChecked(false);
				}
				checkBox.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View arg0) {
						if (checkData.contains(mPosition)) {
							checkData.remove((Object) mPosition);
						} else {
							checkData.add(mPosition);
						}
					}
				});
			}
		}
	}
}