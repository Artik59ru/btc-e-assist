package com.btc_e_assist;

import android.database.Cursor;

import java.util.ArrayList;

public class AssistSynchronize {
    private static Cursor cursor;
    private static long idFromDB;
    private static ArrayList<CheckableTask> bufferTaskList;

    private static int idIndex;
    private static int typeIndex;
    private static int valueIndex;
    private static int option1Index;
    private static int option2Index;
    private static int option3Index;
    private static int option4Index;
    private static int option5Index;

    public static synchronized void synchronizedWithDB() {
        ArrayList<Long> serviceTaskIds = ServiceAssist.getTaskIds();
        bufferTaskList = new ArrayList<CheckableTask>();
        cursor = DBControl.getInstance().getActionsData();
        if (cursor != null) {
            idIndex = cursor.getColumnIndex(DBControl.ACTIONS_NAME_ID);
            typeIndex = cursor.getColumnIndex(DBControl.ACTIONS_NAME_TYPE);
            valueIndex = cursor.getColumnIndex(DBControl.ACTIONS_NAME_VALUE_FLOAT);
            option1Index = cursor.getColumnIndex(DBControl.ACTIONS_NAME_OPTION_1_FLOAT);
            option2Index = cursor.getColumnIndex(DBControl.ACTIONS_NAME_OPTION_2_INT);
            option3Index = cursor.getColumnIndex(DBControl.ACTIONS_NAME_OPTION_3_INT);
            option4Index = cursor.getColumnIndex(DBControl.ACTIONS_NAME_OPTION_4_TEXT);
            option5Index = cursor.getColumnIndex(DBControl.ACTIONS_NAME_OPTION_5_TEXT);
            String type;
            while (cursor.moveToNext()) {
                idFromDB = cursor.getLong(idIndex);
                if (!serviceTaskIds.contains(idFromDB)) {
                    type = cursor.getString(typeIndex);
                    if (type.equals(DBControl.ACTIONS_TYPE_ALARM)) {
                        createSimpleAlarmTask();
                    } else if (type.equals(DBControl.ACTIONS_TYPE_ORDER_ALARM)) {
                        createOrderAlarmTask();
                    }
                }
            }
            ServiceAssist.setTaskList(bufferTaskList);
        }
    }

    private static void createSimpleAlarmTask() {
        String pair = cursor.getString(option4Index);
        String condition = cursor.getString(option5Index);
        Double value = cursor.getDouble(valueIndex);
        CheckableTask newTask = new SimpleAlarmTask(pair, value, condition, idFromDB);
        bufferTaskList.add(newTask);
    }

    private static void createOrderAlarmTask() {
        String orderId = cursor.getString(option4Index);
        CheckableTask newTask = new OrderAlarmTask(orderId, idFromDB);
        bufferTaskList.add(newTask);
    }

}

