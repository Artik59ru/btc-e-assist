package com.btc_e_assist;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;

import com.assist.TradeApi;

/**
 * Class is "task object", each alarm task from Database must have instance of
 * this class. Service has array of these objects and works with each of them.
 */
public class SimpleAlarmTask implements CheckableTask {
    private TradeApi assistTradeApi;
    private DBControl dbControl;

    private String mPair;
    private String mOption;
    private double mTarget;
    private long mTaskId;
    private String currentPrice = "";

    SimpleAlarmTask(String pair, double target, String option, long taskId) {
        assistTradeApi = TradeControl.getInstance().assistTradeApi;
        dbControl = DBControl.getInstance();
        mPair = pair;
        mTarget = target;
        mOption = option;
        mTaskId = taskId;
    }

    @Override
    public boolean check() {
        double difference = assistTradeApi.priceDifference(mPair, mTarget, 0);
        if (difference != Double.MIN_VALUE) {
            if (difference > 0
                    && mOption.equals(AssistAlarmFragment.CONDITION_LOWER)) {
                currentPrice = TradeApi.formatDouble(mTarget - difference, 8);
                return true;
            }
            if (difference < 0
                    && mOption.equals(AssistAlarmFragment.CONDITION_HIGHER)) {
                currentPrice = TradeApi.formatDouble(mTarget - difference, 8);
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean doIfPositive() {
        try {
            Uri alarmSound = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    App.context)
                    .setSmallIcon(R.drawable.ic_stat_alarm)
                    .setContentTitle(
                            String.format(
                                    App.context
                                            .getString(R.string.simple_alarm_notify_title),
                                    mPair))
                    .setContentText(
                            String.format(
                                    App.context
                                            .getString(R.string.simple_alarm_notify_content),
                                    mPair, currentPrice)).setSound(alarmSound);
            Intent resultIntent = new Intent(App.context, TickerActivity.class);
            resultIntent.putExtra(TickerFragment.INTENT_VALUE, mPair);

            TaskStackBuilder stackBuilder = TaskStackBuilder
                    .create(App.context);

            stackBuilder.addParentStack(TickerActivity.class);

            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                    0, PendingIntent.FLAG_UPDATE_CURRENT);
            mBuilder.setContentIntent(resultPendingIntent);
            NotificationManager mNotificationManager = (NotificationManager) App.context
                    .getSystemService(Context.NOTIFICATION_SERVICE);

            mNotificationManager.notify(ServiceAssist.getNewNotificationId(),
                    mBuilder.build());
            dbControl.deleteAlarm(mTaskId);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean doIfNegative() {
        return true;
    }

    @Override
    public boolean isDeleteIfPositive() {
        return true;
    }

    @Override
    public boolean isDeleteIfNegative() {
        return false;
    }

    @Override
    public void doIfPositiveActionFail() {
    }

    @Override
    public void doIfNegativeActionFail() {
    }

    @Override
    public long getId() {
        return mTaskId;
    }
}
