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
	private String mPairName;
	private String mOrderId;
	private long mTaskId;
	String orderValue = "";

	OrderAlarmTask(String pairName, String orderId, long taskId) {
		assistTradeApi = TradeControl.getInstance().assistTradeApi;
		dbControl = DBControl.getInstance();
		mPairName = pairName.toUpperCase().replace('_', '-');
		mOrderId = orderId;
		mTaskId = taskId;
	}

	@Override
	public boolean check() {
		if (!assistTradeApi.isKeysInstalled()) {
			return false;
		}
		if (!assistTradeApi.info.isSuccess()) {
			if (!assistTradeApi.info.runMethod()) {
				assistTradeApi.info.runMethod();
			}
		}
		assistTradeApi.activeOrders.setPair(mPairName);
		boolean result = assistTradeApi.activeOrders.runMethod();
		if (!result) {
			String errMessage = assistTradeApi.activeOrders.getErrorMessage();
			if (errMessage.equals("no orders")) {
				return true;
			} else {
				return false;
			}
		} else {
			while (assistTradeApi.activeOrders.hasNext()) {
				assistTradeApi.activeOrders.switchNext();
				if (assistTradeApi.activeOrders.getCurrentPosition().equals(
						mOrderId)) {
					if (orderValue.length() == 0) {
						setOrderValue();
					}
					return false;
				}
			}
		}
		return true;
	}

	private void setOrderValue() {
		String[] pairNameParts = mPairName.split("-");
		double amount = Double.parseDouble(assistTradeApi.activeOrders
				.getCurrentAmount());
		double rate = Double.parseDouble(assistTradeApi.activeOrders
				.getCurrentRate());
		if (assistTradeApi.activeOrders.getCurrentType().equals("buy")) {
			if (assistTradeApi.info.isSuccess()) {
				assistTradeApi.info.setCurrentPair(mPairName);
				double fee = assistTradeApi.info.getCurrentFee() / 100;
				double doubleValue = amount * (1 - fee);
				orderValue = TradeApi.formatDouble(doubleValue, 8);
			} else {
				orderValue = TradeApi.formatDouble(amount, 8) + " -fee";
			}
			orderValue += " " + pairNameParts[0];
		} else {
			if (assistTradeApi.info.isSuccess()) {
				assistTradeApi.info.setCurrentPair(mPairName);
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
			Uri alarmSound = RingtoneManager
					.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					App.context)
					.setSmallIcon(R.drawable.ic_stat_notif)
					.setContentTitle(
							String.format(
									App.context
											.getString(R.string.order_alarm_notify_title),
									mPairName))
					.setContentText(
							String.format(
									App.context
											.getString(R.string.order_alarm_notify_content),
									orderValue)).setSound(alarmSound);
			Intent resultIntent = new Intent(App.context, TickerActivity.class);
			resultIntent.putExtra(TickerFragment.INTENT_VALUE, mPairName);

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
