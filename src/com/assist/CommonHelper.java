package com.assist;

import java.util.List;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Toast;

public class CommonHelper {
	public static void clearAllEdits(ViewGroup group) {
		if (group != null) {
			int count = group.getChildCount();
			for (int i = 0; i < count; ++i) {
				View view = group.getChildAt(i);
				if (view instanceof EditText) {
					((EditText) view).setText("");
				}
			}
		}
	}

	public static void makeToastUpdating(Context context, String fragmentName) {
		Toast.makeText(context,
				fragmentName + " " + context.getString(R.string.updating),
				Toast.LENGTH_SHORT).show();
	}

	public static void makeToastUpdated(Context context, String fragmentName) {
		Toast.makeText(context,
				fragmentName + " " + context.getString(R.string.updated),
				Toast.LENGTH_SHORT).show();
	}

	public static void makeToastErrorConnection(Context context,
			String fragmentName) {
		Toast.makeText(context, R.string.error_connection, Toast.LENGTH_SHORT)
				.show();
	}

	public static void fillLeftList(String[] pairsList, List<String> namesList,
			ArrayAdapter<String> adapter) {
		String[] names;
		if (pairsList.length != 0) {
			namesList.clear();
			for (String s : pairsList) {
				names = s.split("-");
				if (!namesList.contains(names[0])) {
					namesList.add(names[0]);
				}
			}
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	public static void fillRightList(String[] pairsList,
			List<String> namesList, ArrayAdapter<String> adapter,
			String targetPair) {
		String[] componentsList;
		String targetName = targetPair.split("-")[0];
		if (targetPair.length() != 0) {
			namesList.clear();
			for (String s : pairsList) {
				componentsList = s.split("-");
				if (componentsList[0].equals(targetName)) {
					namesList.add(componentsList[1]);
				}
			}
			if (adapter != null) {
				adapter.notifyDataSetChanged();
			}
		}
	}

	/**
	 * Return position in spinners for target pair. If has troubles, return
	 * {0,0}
	 * 
	 * @param pairsList
	 * @param pair
	 * @return
	 */
	public static int[] getPositionsForPair(String[] pairsList, String pair) {
		int leftPosition = 0, rightPosition = 0;
		String[] inputNames = pair.split("-");
		String[] names;
		String prevName = "";
		int realCount = 0;
		int globalCount = 0;
		if (pairsList.length != 0 && inputNames.length == 2) {
			for (String s : pairsList) {
				names = s.split("-");
				if (!names[0].equals(prevName)) {
					realCount++;
					prevName = names[0];
				}
				if (names[0].equals(inputNames[0])) {
					leftPosition = realCount - 1;
					break;
				}
				globalCount++;
			}
			if (leftPosition >= 0) {
				realCount = 0;
				for (int i = globalCount; i < pairsList.length; i++) {
					names = pairsList[i].split("-");
					if (names[0].equals(inputNames[0])) {
						if (!names[1].equals(inputNames[1])) {
							realCount++;
						} else {
							break;
						}
					} else {
						break;
					}
				}
				rightPosition = realCount;
				int[] result = { leftPosition, rightPosition };
				return result;
			}
		}
		int[] result = { 0, 0 };
		return result;
	}

	public static Typeface fontRobotoLight = Typeface.createFromAsset(
			App.context.getAssets(), "Roboto-Light.ttf");
	public static Typeface fontRobotoThin = Typeface.createFromAsset(
			App.context.getAssets(), "Roboto-Thin.ttf");
}
