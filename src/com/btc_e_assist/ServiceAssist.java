package com.btc_e_assist;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class ServiceAssist extends Service {
	private static final String TASK_EXTRA_NAME = "tsk";
	private static final int TASK_ADD = 1;
	private static final int TASK_ADD_ARRAY = 2;
	private static final int TASK_ID_FOR_DELETE = -1;
	private static final int ARRAY_ID_FOR_DELETE = 0;
	private static volatile CheckableTask mTask;
	private static volatile ArrayList<CheckableTask> mTaskList;
	private static volatile ArrayList<Long> mDeleteList;
	private static volatile long mDeleteId;

	private static ServiceCycle serviceCycle;
	private static int notificationId = 1;

	public static synchronized int getNewNotificationId() {
		return notificationId++;
	}

	public static synchronized int getQueueSize() {
		if (serviceCycle != null) {
			return serviceCycle.getQueueSize();
		} else {
			return 0;
		}
	}

	public static synchronized void setTask(CheckableTask task) {
		mTask = task;
		Intent intent = new Intent(App.context, ServiceAssist.class);
		intent.putExtra(TASK_EXTRA_NAME, TASK_ADD);
		App.context.startService(intent);
	}

	public static synchronized void setTaskList(
			ArrayList<CheckableTask> taskList) {
		mTaskList = taskList;
		Intent intent = new Intent(App.context, ServiceAssist.class);
		intent.putExtra(TASK_EXTRA_NAME, TASK_ADD_ARRAY);
		App.context.startService(intent);
	}

	public static synchronized void setDeleteTask(long deleteId) {
		mDeleteId = deleteId;
		Intent intent = new Intent(App.context, ServiceAssist.class);
		intent.putExtra(TASK_EXTRA_NAME, TASK_ID_FOR_DELETE);
		App.context.startService(intent);
	}

	public static synchronized void setDeleteList(ArrayList<Long> deleteList) {
		mDeleteList = deleteList;
		Intent intent = new Intent(App.context, ServiceAssist.class);
		intent.putExtra(TASK_EXTRA_NAME, ARRAY_ID_FOR_DELETE);
		App.context.startService(intent);
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		try {
			if (serviceCycle == null) {
				serviceCycle = new ServiceCycle(this);
			}
		} catch (Exception e) {
			stopSelf();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (serviceCycle == null) {
			serviceCycle = new ServiceCycle(this);
		}
		if (serviceCycle.getState() == Thread.State.TERMINATED) {
			serviceCycle = new ServiceCycle(this);
		}
		if (intent != null) {
			int command = intent
					.getIntExtra(TASK_EXTRA_NAME, Integer.MIN_VALUE);
			switch (command) {
			case TASK_ID_FOR_DELETE:
				serviceCycle.deleteTask(mDeleteId);
				break;
			case ARRAY_ID_FOR_DELETE:
				serviceCycle.deleteTasks(mDeleteList);
				break;
			case TASK_ADD:
				serviceCycle.addTask(mTask);
				break;
			case TASK_ADD_ARRAY:
				serviceCycle.addTaskList(mTaskList);
			}
		}
		if (!serviceCycle.isAlive()
				&& serviceCycle.getState() != Thread.State.TERMINATED) {
			serviceCycle.start();
			Intent notificationIntent = new Intent(this, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this).setSmallIcon(R.drawable.ic_stat_notif)
					.setContentTitle(getString(R.string.app_name))
					.setContentText(getString(R.string.foreground_content));
			mBuilder.setContentIntent(pendingIntent);
			startForeground(notificationId, mBuilder.build());
			notificationId++;
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
	}
}