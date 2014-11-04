package com.btc_e_assist;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;

public class DBControl {
    public static final String PREFERENCES_EMPTY = "EMPTY";

    private static final String BASE_NAME = "btceassistdb";
    private static final int BASE_VERSION = 1;
    private static final String PROFILES_TABLE_NAME = "profiles";
    private static final String ACTIONS_TABLE_NAME = "actions";

    public static final String PROFILES_NAME_ID = "_id";
    public static final String PROFILES_NAME_NAME = "name";
    public static final String PROFILES_NAME_KEY = "apikey";
    public static final String PROFILES_NAME_SECRET = "secretkey";
    public static final String PROFILES_NAME_IS_ENCODED = "is_encoded";

    public static final String ACTIONS_NAME_ID = "_id";
    public static final String ACTIONS_NAME_TYPE = "type";
    public static final String ACTIONS_NAME_VALUE_FLOAT = "value";
    public static final String ACTIONS_NAME_OPTION_1_FLOAT = "option1";
    public static final String ACTIONS_NAME_OPTION_2_INT = "option2";
    public static final String ACTIONS_NAME_OPTION_3_INT = "option3";
    public static final String ACTIONS_NAME_OPTION_4_TEXT = "option4";
    public static final String ACTIONS_NAME_OPTION_5_TEXT = "option5";
    public static final String ACTIONS_TYPE_ALARM = "alarm";
    public static final String ACTIONS_TYPE_ORDER_ALARM = "order_alarm";
    private static final String FIND_ID = "_id = ?";

    private static final String CREATE_PROFILES = "CREATE TABLE "
            + PROFILES_TABLE_NAME + "(" + PROFILES_NAME_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + PROFILES_NAME_NAME
            + " TEXT, " + PROFILES_NAME_KEY + " TEXT, " + PROFILES_NAME_SECRET
            + " TEXT," + PROFILES_NAME_IS_ENCODED + " TEXT" + ");";
    private static final String CREATE_ACTIONS = "CREATE TABLE "
            + ACTIONS_TABLE_NAME + "(" + ACTIONS_NAME_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT, " + ACTIONS_NAME_TYPE
            + " TEXT, " + ACTIONS_NAME_VALUE_FLOAT + " FLOAT, "
            + ACTIONS_NAME_OPTION_1_FLOAT + " FLOAT, "
            + ACTIONS_NAME_OPTION_2_INT + " INTEGER, "
            + ACTIONS_NAME_OPTION_3_INT + " INTEGER, "
            + ACTIONS_NAME_OPTION_4_TEXT + " TEXT,"
            + ACTIONS_NAME_OPTION_5_TEXT + " TEXT" + ");";
    private static final String[] createCommands = {CREATE_PROFILES,
            CREATE_ACTIONS};

    private static volatile DBControl mDBControl;
    private DBHelper mDBHelper;
    private SQLiteDatabase mDB;

    private DBControl(Context context) {
        mDBHelper = new DBHelper(context, BASE_NAME, null, BASE_VERSION);
        mDB = mDBHelper.getWritableDatabase();
    }

    /**
     * Get DBControl singleton instance with App.context
     *
     * @return null if context is null
     */
    public static synchronized DBControl getInstance() {
        if (mDBControl == null) {
            if (App.context == null) {
                return null;
            }
            mDBControl = new DBControl(App.context);
        }
        return mDBControl;
    }

    /**
     * Get DBControl singleton instance with your context
     *
     * @param context
     * @return null if context is null
     */
    public static synchronized DBControl getInstance(Context context) {
        if (mDBControl == null) {
            if (context == null) {
                return null;
            }
            mDBControl = new DBControl(context);
        }
        return mDBControl;
    }

    public synchronized void closeConnection() {
        if (mDBHelper != null)
            mDBHelper.close();
    }

    public synchronized void addProfile(String name, String key, String secret,
                                        boolean isEncoded) throws Exception {
        if (key.length() < 8 || secret.length() < 8) {
            throw new Exception("Invalid key");
        }
        ContentValues cV = new ContentValues();
        cV.put(PROFILES_NAME_NAME, name);
        cV.put(PROFILES_NAME_KEY, key);
        cV.put(PROFILES_NAME_SECRET, secret);
        if (isEncoded) {
            cV.put(PROFILES_NAME_IS_ENCODED, 1);
        } else {
            cV.put(PROFILES_NAME_IS_ENCODED, 0);
        }
        mDB.insert(PROFILES_TABLE_NAME, null, cV);
    }

    @SuppressLint("DefaultLocale")
    public synchronized void addAlarm(String pair, String option, double value) {
        ContentValues cV = new ContentValues();
        cV.put(ACTIONS_NAME_TYPE, ACTIONS_TYPE_ALARM);
        cV.put(ACTIONS_NAME_OPTION_4_TEXT, pair.toUpperCase().replace('_', '-'));
        cV.put(ACTIONS_NAME_OPTION_5_TEXT, option);
        cV.put(ACTIONS_NAME_VALUE_FLOAT, value);
        mDB.insert(ACTIONS_TABLE_NAME, null, cV);
    }

    public synchronized void addOrderAlarm(String orderId) {
        ContentValues cV = new ContentValues();
        cV.put(ACTIONS_NAME_TYPE, ACTIONS_TYPE_ORDER_ALARM);
        cV.put(ACTIONS_NAME_OPTION_4_TEXT, orderId);
        mDB.insert(ACTIONS_TABLE_NAME, null, cV);
    }

    public synchronized void deleteProfile(long id) {
        mDB.delete(PROFILES_TABLE_NAME, PROFILES_NAME_ID + " = " + id, null);
    }

    public synchronized void deleteAlarm(long id) {
        mDB.delete(ACTIONS_TABLE_NAME, ACTIONS_NAME_ID + " = " + id, null);
    }

    public synchronized void deleteOrderAlarm(String orderId) {
        mDB.delete(ACTIONS_TABLE_NAME, ACTIONS_NAME_OPTION_4_TEXT + " = "
                + orderId, null);
    }

    public synchronized Cursor getProfilesDataWithId(long row_id) {
        String[] queryParam = {String.valueOf(row_id)};
        String[] columns = {PROFILES_NAME_NAME, PROFILES_NAME_KEY,
                PROFILES_NAME_SECRET, PROFILES_NAME_IS_ENCODED};
        return mDB.query(PROFILES_TABLE_NAME, columns, FIND_ID, queryParam,
                null, null, null);
    }

    public synchronized Cursor getProfilesData() {
        return mDB.query(PROFILES_TABLE_NAME, null, null, null, null, null,
                null);
    }

    public synchronized Cursor getActionsData() {
        return mDB
                .query(ACTIONS_TABLE_NAME, null, null, null, null, null, null);
    }

    public synchronized Cursor getAlarmsData() {
        String[] columns = {ACTIONS_NAME_ID, ACTIONS_NAME_OPTION_4_TEXT,
                ACTIONS_NAME_OPTION_5_TEXT, ACTIONS_NAME_VALUE_FLOAT};
        return mDB.query(ACTIONS_TABLE_NAME, columns, ACTIONS_NAME_TYPE
                + " = '" + ACTIONS_TYPE_ALARM + "'", null, null, null, null);
    }

    /**
     * Return ID of OrderAlarm by OrderID or Long.MIN_VALUE if can't find
     *
     * @param orderId
     * @return
     */
    public synchronized long getOrderAlarmDBId(String orderId) {
        String[] columns = {ACTIONS_NAME_ID};
        Cursor cursor = mDB.query(ACTIONS_TABLE_NAME, columns,
                ACTIONS_NAME_TYPE + " = '" + ACTIONS_TYPE_ORDER_ALARM
                        + "' AND " + ACTIONS_NAME_OPTION_4_TEXT + " = '"
                        + orderId + "'", null, null, null, null);
        if (cursor.moveToFirst()) {
            int idIndex = cursor.getColumnIndex(ACTIONS_NAME_ID);
            return cursor.getLong(idIndex);
        } else {
            return Long.MIN_VALUE;
        }
    }

    public synchronized void getOrderAlarmData(ArrayList<String> inputList) {
        String[] columns = {ACTIONS_NAME_OPTION_4_TEXT};
        Cursor cursor = mDB.query(ACTIONS_TABLE_NAME, columns,
                ACTIONS_NAME_TYPE + " = '" + ACTIONS_TYPE_ORDER_ALARM + "'",
                null, null, null, null);
        if (cursor.moveToFirst()) {
            inputList.clear();
            int orderIdIndex = cursor
                    .getColumnIndex(ACTIONS_NAME_OPTION_4_TEXT);
            do {
                inputList.add(cursor.getString(orderIdIndex));
            } while (cursor.moveToNext());
        }
    }

    private class DBHelper extends SQLiteOpenHelper {

        public DBHelper(Context context, String name, CursorFactory factory,
                        int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            for (String s : createCommands) {
                db.execSQL(s);
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        }
    }
}
