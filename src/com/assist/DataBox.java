package com.assist;

import java.util.ArrayList;
import java.util.HashMap;

public class DataBox {
	public volatile ArrayList<HashMap<String, Object>> data1;
	public volatile ArrayList<ArrayList<HashMap<String, Object>>> data2;
	public volatile String time;

	public DataBox() {
		data1 = new ArrayList<HashMap<String, Object>>();
		data2 = new ArrayList<ArrayList<HashMap<String, Object>>>();
		time = "xx:xx";
	}
}
