package com.btc_e_assist;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.ResourceCursorAdapter;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * aliasColumnName - column name for row, where contents alias
 */
class CheckBoxCursorAdapter extends ResourceCursorAdapter {
    public ArrayList<Long> checkData;
    String[] mFrom;
    int[] mTo;
    String mAliasColumnName;
    String[] mAliases;
    String[] mAliasesValues;
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
        checkData = new ArrayList<Long>();
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        if (cursor != null) {
            int count = mTo.length;
            int currentIndex;
            int aliasIndex = cursor.getColumnIndex(mAliasColumnName);
            int idIndex = cursor.getColumnIndex(DBControl.ACTIONS_NAME_ID);
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
                final long idIndexContent = cursor.getLong(idIndex);
                boolean isContains = checkData.contains(idIndexContent);
                if (isContains) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
                checkBox.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View arg0) {
                        if (checkData.contains(idIndexContent)) {
                            checkData.remove((Object) idIndexContent);
                        } else {
                            checkData.add(idIndexContent);
                        }
                    }
                });
            }
        }
    }
}