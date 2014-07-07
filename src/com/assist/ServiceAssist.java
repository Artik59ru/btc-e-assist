package com.assist;

import java.util.ArrayList;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

public class ServiceAssist extends Service {
	public static final String TASK_EXTRA_NAME = "task";
	public static final String TASK_ADD_ARRAY = "taskAddArray";
	public static final String TASK_ADD = "taskAdd";
	public static final String TASK_FOR_DELETE = "taskDelete";

	public static volatile SimpleAlarmTask globalTask;
	public static volatile ArrayList<CheckableTask> globalTaskList;
	public static volatile ArrayList<Long> globalDeleteList;

	public static long sleepValue = 60 * 1000;

	private static final long serverCacheFrequency = 2 * 1000;
	private MainCycle mainCycle;
	public static int notificationId = 0;

	@Override
	public IBinder onBind(Intent arg0) {
		return null;
	}

	@Override
	public void onCreate() {
		try {
			if (mainCycle == null) {
				mainCycle = new MainCycle();
			}
		} catch (Exception e) {
			stopSelf();
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if (mainCycle == null) {
			mainCycle = new MainCycle();
		}
		if (intent != null) {
			String command = intent.getStringExtra(TASK_EXTRA_NAME);
			if (command != null) {
				if (command.equals(TASK_ADD)) {
					mainCycle.addTask(globalTask);
				} else if (command.equals(TASK_ADD_ARRAY)) {
					mainCycle.addTaskList(globalTaskList);
				} else if (command.equals(TASK_FOR_DELETE)) {
					mainCycle.deleteTasks(globalDeleteList);
				}
			}
		}
		if (!mainCycle.isAlive()) {
			mainCycle.start();
			Intent notificationIntent = new Intent(this, MainActivity.class);
			PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
					notificationIntent, 0);
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
					this)
					.setSmallIcon(R.drawable.ic_stat_notif)
					.setContentTitle(
							String.format(getString(R.string.app_name),
									getString(R.string.app_name)))
					.setContentText(getString(R.string.foreground_content));
			mBuilder.setContentIntent(pendingIntent);
			if (notificationId == 0) {
				notificationId++;
			}
			startForeground(notificationId, mBuilder.build());
			notificationId++;
		}
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
	}

	private class MainCycle extends Thread implements Runnable {
		volatile ArrayList<CheckableTask> taskList = new ArrayList<CheckableTask>();
		boolean updated = false;

		synchronized void addTask(CheckableTask task) {
			if (task != null) {
				taskList.add(task);
				updated = true;
			}
		}

		@SuppressWarnings("unchecked")
		synchronized void addTaskList(ArrayList<CheckableTask> inputList) {
			if (inputList != null) {
				taskList = (ArrayList<CheckableTask>) inputList.clone();
				updated = true;
			}
		}

		synchronized void deleteTasks(ArrayList<Long> taskIds) {
			for (CheckableTask task : taskList) {
				for (long taskId : taskIds) {
					long id = task.getId();
					if (id == taskId) {
						if (taskList.remove(task)) {
							updated = true;
						}
					}
				}
			}
		}

		@Override
		public void run() {
			while (taskList.size() > 0) {
				for (CheckableTask task : taskList) {
					if (updated) {
						updated = false;
						break;
					}
					if (task.check()) {
						if (!task.doIfPositive()) {
							task.doIfPositiveActionFail();
						}
						if (task.isDeleteIfPositive()) {
							taskList.remove(task);
						}
					} else {
						if (!task.doIfNegative()) {
							task.doIfNegativeActionFail();
						}
						if (task.isDeleteIfNegative()) {
							taskList.remove(task);
						}
					}
					if (updated) {
						updated = false;
						break;
					}
					try {
						Thread.sleep(serverCacheFrequency);
					} catch (InterruptedException e) {
					}
				}
				try {
					Thread.sleep(sleepValue);
				} catch (InterruptedException e) {
				}
			}
			stopSelf();
		}
	}
}