package com.btc_e_assist;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class CustomExpandAdapter extends BaseExpandableListAdapter {
	public ArrayList<Integer> checkData;
	protected LayoutInflater mInflater;
	protected List<? extends Map<String, ?>> mGroupData;
	protected List<? extends List<? extends Map<String, ?>>> mChildData;
	protected int mGroupLayout;
	protected int mChildLayout;
	protected int mImageId;
	protected int mDividerId;
	protected String[] mGroupFrom;
	protected String[] mChildFrom;
	protected String[] mAliases;
	protected int[] mGroupTo;
	protected int[] mChildTo;
	protected int[] mAliasesDrawIds;

	/**
	 * Adapter for populating ExpandableListView, with checkBoxes and
	 * aliases-Image function. groupData must have "alias" key with key from
	 * aliases array
	 * 
	 * @param context
	 * @param groupData
	 * @param groupFrom
	 * @param groupTo
	 * @param childData
	 * @param childFrom
	 * @param childTo
	 * @param groupLayout
	 * @param childLayout
	 * @param imageId
	 *            id for insert DrawableLeft
	 * @param aliases
	 *            Array of aliases
	 * @param aliasesDrawIds
	 *            relative for aliases images
	 */
	public CustomExpandAdapter(Context context,
			List<? extends Map<String, ?>> groupData, String[] groupFrom,
			int[] groupTo,
			List<? extends List<? extends Map<String, ?>>> childData,
			String[] childFrom, int[] childTo, int groupLayout,
			int childLayout, int imageId, int dividerId, String[] aliases,
			int[] aliasesDrawIds) {
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mGroupData = groupData;
		mChildData = childData;
		mGroupLayout = groupLayout;
		mChildLayout = childLayout;
		mImageId = imageId;
		mDividerId = dividerId;
		mGroupFrom = groupFrom;
		mChildFrom = childFrom;
		mAliases = aliases;
		mGroupTo = groupTo;
		mChildTo = childTo;
		mAliasesDrawIds = aliasesDrawIds;
		checkData = new ArrayList<Integer>();
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mInflater.inflate(mGroupLayout, parent, false);
		}
		Map<String, ?> map = mGroupData.get(groupPosition);
		String currentAlias = (String) map.get("alias");
		int count = mGroupTo.length;
		for (int i = 0; i < count; i++) {
			TextView v = (TextView) view.findViewById(mGroupTo[i]);
			if (v != null) {
				if (currentAlias != null) {
					if (mGroupTo[i] == mImageId) {
						int countAliases = mAliases.length;
						for (int y = 0; y < countAliases; y++) {
							if (mAliases[y].equals(currentAlias)) {
								v.setCompoundDrawablesWithIntrinsicBounds(
										mAliasesDrawIds[y], 0, 0, 0);
								break;
							}
						}
					}
				}
				v.setText((String) map.get(mGroupFrom[i]));
			}
		}
		return view;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		View view = convertView;
		if (view == null) {
			view = mInflater.inflate(mChildLayout, parent, false);
		}
		if (isLastChild) {
			if (groupPosition == getGroupCount() - 1) {
				view.findViewById(mDividerId).setVisibility(View.GONE);
			} else {
				view.findViewById(mDividerId).setVisibility(View.VISIBLE);
			}
		} else {
			view.findViewById(mDividerId).setVisibility(View.VISIBLE);
		}
		Map<String, ?> map = mChildData.get(groupPosition).get(childPosition);
		int count = mChildTo.length;
		for (int i = 0; i < count; i++) {
			TextView v = (TextView) view.findViewById(mChildTo[i]);
			if (v != null) {
				v.setText((String) map.get(mChildFrom[i]));
			}
		}
		return view;
	}

	@Override
	public Object getChild(int groupPosition, int childPosition) {
		return mChildData.get(groupPosition).get(childPosition);
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return mChildData.get(groupPosition).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return mGroupData.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return mGroupData.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}

}
