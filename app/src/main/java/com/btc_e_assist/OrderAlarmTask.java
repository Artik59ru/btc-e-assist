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

public class OrderAlarmTask implements CheckableTask {
    private TradeApi assistTradeApi;
    private DBControl dbControl;
    private String pairName;
    private String mOrderId;
    private long mTaskId;
    private String orderValue = "";
    private boolean isCancelled = false;

    OrderAlarmTask(String orderId, long taskId) {
        assistTradeApi = TradeControl.getInstance().assistTradeApi;
        dbControl = DBControl.getInstance();
        mOrderId = orderId;
        mTaskId = taskId;
    }

    @Override
    public boolean check() {
        if (!assistTradeApi.isKeysInstalled()) {
            return false;
        }
        if (!assistTradeApi.info.isSuccess()) {
            assistTradeApi.info.runMethod();
        }
        assistTradeApi.orderInfo.setOrder_id(mOrderId);
        if (!assistTradeApi.orderInfo.runMethod()) {
            assistTradeApi.orderInfo.runMethod();
        }
        if (assistTradeApi.orderInfo.isSuccess()) {
            assistTradeApi.orderInfo.switchNext();
            int status = assistTradeApi.orderInfo.getCurrentStatus();
            if (status == 2) {
                isCancelled = true;
                return true;
            }
            if (status == 1 || status == 3) {
                setOrderValue();
                return true;
            }
        }
        return false;
    }

    private void setOrderValue() {
        pairName = assistTradeApi.orderInfo.getCurrentPair();
        String[] pairNameParts = pairName.split("-");
        double amount = Double.parseDouble(assistTradeApi.orderInfo
                .getCurrentStart_amount()) - Double.parseDouble(assistTradeApi.orderInfo
                .getCurrentAmount());
        double rate = Double.parseDouble(assistTradeApi.orderInfo
                .getCurrentRate());
        if (assistTradeApi.orderInfo.getCurrentType().equals("buy")) {
            if (assistTradeApi.info.isSuccess()) {
                assistTradeApi.info.setCurrentPair(pairName);
                double fee = assistTradeApi.info.getCurrentFee() / 100;
                double doubleValue = amount * (1 - fee);
                orderValue = TradeApi.formatDouble(doubleValue, 8);
            } else {
                orderValue = TradeApi.formatDouble(amount, 8) + " -fee";
            }
            orderValue += " " + pairNameParts[0];
        } else {
            if (assistTradeApi.info.isSuccess()) {
                assistTradeApi.info.setCurrentPair(pairName);
                double fee = assistTradeApi.info.getCurrentFee() / 100;
                double doubleValue = amount * rate * (1 - fee);
                orderValue = TradeApi.formatDouble(doubleValue, 8);
            } else {
                double doubleValue = amount * rate;
                orderValue = TradeApi.formatDouble(doubleValue, 8) + " -fee";
            }
            orderValue += " " + pairNameParts[1];
        }
    }

    @Override
    public boolean doIfPositive() {
        try {
            if (isCancelled) {
                dbControl.deleteOrderAlarm(mOrderId);
                return true;
            }
            Uri alarmSound = RingtoneManager
                    .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
            NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                    App.context)
                    .setSmallIcon(R.drawable.ic_stat_alarm)
                    .setContentTitle(
                            String.format(
                                    App.context
                                            .getString(R.string.order_alarm_notify_title),
                                    pairName))
                    .setContentText(
                            String.format(
                                    App.context
                                            .getString(R.string.order_alarm_notify_content),
                                    orderValue)).setSound(alarmSound);
            Intent resultIntent = new Intent(App.context, TickerActivity.class);
            resultIntent.putExtra(TickerFragment.INTENT_VALUE, pairName);

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
            dbControl.deleteOrderAlarm(mOrderId);
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
