package com.btc_e_assist;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;

public class ServiceCycle extends Thread {
	private static volatile long frequencyValue = 60 * 1000;
	private static final long serverCacheFrequency = 2 * 1000;
	private Context mContext;
	private volatile ArrayList<CheckableTask> taskList = new ArrayList<CheckableTask>();
	private volatile boolean updated = false;

	public ServiceCycle(Context context) {
		mContext = context;
	}

	public static void setFrequency(long value) {
		if (value > 0) {
			frequencyValue = value;
		}
	}

	public synchronized int getQueueSize() {
		return taskList.size();
	}

	public synchronized void addTask(CheckableTask task) {
		if (task != null) {
			updated = true;
			interrupt();
			taskList.add(task);
		}
	}

	public synchronized void addTaskList(ArrayList<CheckableTask> inputList) {
		if (inputList != null) {
			updated = true;
			interrupt();
			for (CheckableTask t : inputList) {
				taskList.add(t);
			}
		}
	}

	public synchronized void deleteTask(long taskId) {
		updated = true;
		interrupt();
		for (CheckableTask task : taskList) {
			if (task.getId() == taskId) {
				taskList.remove(task);
				return;
			}
		}
	}

	public synchronized void deleteTasks(ArrayList<Long> taskIds) {
		if (taskIds != null) {
			updated = true;
			interrupt();
			for (long removeId : taskIds) {
				for (CheckableTask task : taskList) {
					if (task.getId() == removeId) {
						taskList.remove(task);
						break;
					}
				}
			}
		}
	}

	@Override
	public void run() {
		CheckableTask currentTask;
		while (getQueueSize() > 0) {
			for (int i = 0; i < getQueueSize(); i++) {
				if (updated) {
					break;
				}
				try {
					currentTask = taskList.get(i);
				} catch (Exception e) {
					break;
				}
				try {
					if (currentTask.check()) {
						if (!currentTask.doIfPositive()) {
							currentTask.doIfPositiveActionFail();
						}
						if (currentTask.isDeleteIfPositive()) {
							taskList.remove(currentTask);
							i--;
						}
					} else {
						if (!currentTask.doIfNegative()) {
							currentTask.doIfNegativeActionFail();
						}
						if (currentTask.isDeleteIfNegative()) {
							taskList.remove(currentTask);
							i--;
						}
					}
				} catch (Exception e) {
					continue;
				}
				if (updated) {
					break;
				}
				try {
					Thread.sleep(serverCacheFrequency);
				} catch (InterruptedException e) {
					break;
				}
			}
			if (updated) {
				updated = false;
				continue;
			}
			if (getQueueSize() == 0) {
				break;
			}
			try {
				Thread.sleep(frequencyValue);
			} catch (InterruptedException e) {
			}
		}
		if (mContext != null) {
			mContext.stopService(new Intent(mContext, ServiceAssist.class));
		}
		return;
	}
}
