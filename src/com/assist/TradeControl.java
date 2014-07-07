package com.assist;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
import android.text.InputType;
import android.widget.EditText;
import android.widget.Toast;

import com.TradeApi.TradeApi;

public class TradeControl {
	private static final int PRICE_LIMIT_FOR_DECIMAL_PLACES = 1;

	public static int tradesCount = 200;
	public static String tradeHistoryCount = "200";
	public static String transHistoryCount = "200";
	public static int depthCount = 60;

	private static TradeControl mTradeControl;
	private PrefControl pControl;
	public TradeApi tradeApi;

	private TradeControl(Context context) {
		tradeApi = new TradeApi();
		pControl = PrefControl.getInstance();
		setApiKeys(context);
		DBControl.getInstance();
	}

	public synchronized static TradeControl getInstance(Context context) {
		if (mTradeControl == null) {
			mTradeControl = new TradeControl(context);
		}
		return mTradeControl;
	}

	public void setApiKeys(final Context context) {
		if (pControl == null) {
			pControl = PrefControl.getInstance();
		}
		final long currentId = pControl.getCurrentProfileId();
		if (currentId != PrefControl.EMPTY_LONG) {
			DBControl dbControl = DBControl.getInstance();
			Cursor profilesData = dbControl.getProfilesDataWithId(currentId);
			if (profilesData == null) {
				return;
			}
			profilesData.moveToNext();
			final String nameFromCursor = profilesData.getString(profilesData
					.getColumnIndex(DBControl.PROFILES_NAME_NAME));
			final String keyFromCursor = profilesData.getString(profilesData
					.getColumnIndex(DBControl.PROFILES_NAME_KEY));
			final String secretFromCursor = profilesData.getString(profilesData
					.getColumnIndex(DBControl.PROFILES_NAME_SECRET));
			int isEncoded = profilesData.getInt(profilesData
					.getColumnIndex(DBControl.PROFILES_NAME_IS_ENCODED));
			if (isEncoded == 0) {
				try {
					tradeApi.setKeys(keyFromCursor, secretFromCursor);
				} catch (Exception e) {
				}
			} else {
				AlertDialog.Builder passDialog = new AlertDialog.Builder(
						context);
				passDialog.setTitle(context
						.getString(R.string.decryption_dialog_title));
				passDialog.setMessage(String.format(
						context.getString(R.string.decryption_dialog_message),
						nameFromCursor));
				final EditText passEditText = new EditText(context);
				passEditText.setInputType(InputType.TYPE_CLASS_TEXT
						| InputType.TYPE_TEXT_VARIATION_PASSWORD);
				passDialog.setView(passEditText);
				passDialog.setPositiveButton("OK",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int which) {
								try {
									tradeApi.setKeys(keyFromCursor,
											secretFromCursor, passEditText
													.getText().toString());
									pControl.setCurrentProfileId(currentId);
									Toast.makeText(context,
											R.string.accepted_dialog_message,
											Toast.LENGTH_SHORT).show();
								} catch (Exception e) {
									Toast.makeText(context,
											R.string.wrong_password,
											Toast.LENGTH_SHORT).show();
									setApiKeys(context);
								}
							}
						});
				passDialog.show();
			}
		}
	}

	/**
	 * Return false if has ANY troubles
	 * 
	 * @param pairsList
	 * @param inputTickerBox
	 * @return
	 */
	public boolean getTickerData(String[] pairsList, DataBox inputTickerBox) {
		try {
			if (pairsList.length == 0) {
				return false;
			}
			tradeApi.ticker.resetParams();
			for (String s : pairsList) {
				tradeApi.ticker.addPair(s);
			}
			if (!tradeApi.ticker.runMethod()) {
				if (!tradeApi.ticker.runMethod()) {
					return false;
				}
			}
			inputTickerBox.data1.clear();
			float last;
			while (tradeApi.ticker.hasNextPair()) {
				tradeApi.ticker.switchNextPair();
				HashMap<String, Object> itemMap = new HashMap<String, Object>();

				itemMap.put("name", tradeApi.ticker.getCurrentPairName());
				last = Float.parseFloat(tradeApi.ticker.getCurrentLast());
				if (last > PRICE_LIMIT_FOR_DECIMAL_PLACES) {
					tradeApi.ticker.setDecimalPlaces(2);
				}
				itemMap.put("last", tradeApi.ticker.getCurrentLast());
				itemMap.put("low", tradeApi.ticker.getCurrentLow());
				itemMap.put("high", tradeApi.ticker.getCurrentHigh());
				tradeApi.ticker.setDecimalPlaces(0);
				String[] parts = tradeApi.ticker.getCurrentPairName()
						.split("-");
				StringBuilder volume = new StringBuilder(
						tradeApi.ticker.getCurrentVolCur());
				volume.append(" ");
				volume.append(parts[0]);
				volume.append(" / ");
				volume.append(tradeApi.ticker.getCurrentVol());
				volume.append(" ");
				volume.append(parts[1]);
				itemMap.put("volume", volume.toString());
				tradeApi.ticker.setDefaultDecimalPlaces();
				inputTickerBox.data1.add(itemMap);
			}
			long timestamp = Long
					.parseLong(tradeApi.ticker.getCurrentUpdated()) * 1000;
			DateFormat formatter = DateFormat.getTimeInstance();
			inputTickerBox.time = formatter.format(new Date(timestamp));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return false if has ANY troubles
	 * 
	 * @param inputBox
	 */
	public boolean getBalanceData(DataBox inputBox) {
		long timestamp;
		DateFormat formatter;
		try {
			if (!tradeApi.getInfo.runMethod()) {
				if (!tradeApi.getInfo.runMethod()) {
					return false;
				}
			}
			inputBox.data1.clear();
			ArrayList<String> names = tradeApi.getInfo.getCurrencyList();
			for (String s : names) {
				HashMap<String, Object> map = new HashMap<String, Object>();
				map.put("name", s);
				map.put("value", tradeApi.getInfo.getBalance(s));
				inputBox.data1.add(map);
			}
			timestamp = tradeApi.getInfo.getLocalTimestamp();
			formatter = DateFormat.getTimeInstance();
			inputBox.time = formatter.format(new Date(timestamp));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return false if has ANY troubles
	 * 
	 * @param inputBox
	 */
	@SuppressLint("DefaultLocale")
	public boolean getOrdersData(DataBox inputBox) {
		long timestamp;
		DateFormat formatter;
		try {
			if (!tradeApi.activeOrders.runMethod()
					&& tradeApi.activeOrders.getErrorMessage().length() == 0) {
				if (!tradeApi.activeOrders.runMethod()
						&& tradeApi.activeOrders.getErrorMessage().length() == 0) {
					return false;
				}
			}
			inputBox.data1.clear();
			inputBox.data2.clear();
			while (tradeApi.activeOrders.hasNext()) {
				tradeApi.activeOrders.switchNext();
				HashMap<String, Object> itemGroupMap = new HashMap<String, Object>();
				HashMap<String, Object> itemChildMap = new HashMap<String, Object>();

				itemGroupMap.put("id",
						tradeApi.activeOrders.getCurrentPosition());

				if (tradeApi.activeOrders.getCurrentType().equals("sell")) {
					itemGroupMap.put("alias", "sell");
				} else {
					itemGroupMap.put("alias", "buy");
				}
				String rate = tradeApi.activeOrders.getCurrentRate();
				String names[] = tradeApi.activeOrders.getCurrentPair().split(
						"-");
				itemGroupMap.put("rate", rate);
				itemGroupMap.put("name0", names[0]);
				itemGroupMap.put("name1", names[1]);

				String amount = tradeApi.activeOrders.getCurrentAmount();
				itemChildMap.put("amount", amount);
				StringBuilder total = new StringBuilder();
				double rateDouble = Double.parseDouble(rate);
				double amountDouble = Double.parseDouble(amount);
				double totalDouble = rateDouble * amountDouble;
				total.append(TradeApi.formatDouble(totalDouble, 8));
				total.append(" ");
				total.append(names[1]);
				itemChildMap.put("total", total.toString());
				timestamp = Long.parseLong(tradeApi.activeOrders
						.getCurrentTimestamp_created()) * 1000;
				formatter = DateFormat.getDateTimeInstance();
				itemChildMap.put("date", formatter.format(new Date(timestamp)));
				int status = tradeApi.activeOrders.getCurrentStatus();
				if (status == 1) {
					itemChildMap
							.put("status", App.context
									.getString(R.string.partly_filled_order));
				} else {
					itemChildMap.put("status",
							App.context.getString(R.string.active_order));
				}
				ArrayList<HashMap<String, Object>> childArray = new ArrayList<HashMap<String, Object>>();
				childArray.add(itemChildMap);
				inputBox.data1.add(itemGroupMap);
				inputBox.data2.add(childArray);

				timestamp = tradeApi.activeOrders.getLocalTimestamp();
				formatter = DateFormat.getTimeInstance();
				inputBox.time = formatter.format(new Date(timestamp));
			}
			timestamp = tradeApi.activeOrders.getLocalTimestamp();
			formatter = DateFormat.getTimeInstance();
			inputBox.time = formatter.format(new Date(timestamp));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return false if has ANY troubles
	 * 
	 */
	@SuppressLint("DefaultLocale")
	public boolean getTradesData(String pairName, DataBox inputBox) {
		long timestamp;
		DateFormat formatter;
		try {
			tradeApi.trades.resetParams();
			tradeApi.trades.addPair(pairName);
			tradeApi.trades.setLimit(tradesCount);
			if (!tradeApi.trades.runMethod()) {
				if (!tradeApi.trades.runMethod()) {
					return false;
				}
			}
			inputBox.data1.clear();
			inputBox.data2.clear();
			tradeApi.trades.setCurrentPair(pairName);
			while (tradeApi.trades.hasNextTrade()) {
				tradeApi.trades.switchNextTrade();
				HashMap<String, Object> itemGroupMap = new HashMap<String, Object>();
				HashMap<String, Object> itemChildMap = new HashMap<String, Object>();

				itemGroupMap.put("id", tradeApi.trades.getCurrentTid());

				if (tradeApi.trades.getCurrentType().equals("ask")) {
					itemGroupMap.put("alias", "ask");
				} else {
					itemGroupMap.put("alias", "bid");
				}
				String amount = tradeApi.trades.getCurrentAmount();
				itemGroupMap.put("amount", amount);

				String rate = tradeApi.trades.getCurrentPrice();
				String names[] = tradeApi.trades.getCurrentPairName()
						.split("-");
				itemChildMap.put("rate", rate);
				itemGroupMap.put("name0", names[0]);
				itemGroupMap.put("name1", names[1]);
				StringBuilder total = new StringBuilder();
				double rateDouble = Double.parseDouble(rate);
				double amountDouble = Double.parseDouble(amount);
				double totalDouble = rateDouble * amountDouble;
				total.append(TradeApi.formatDouble(totalDouble, 8));
				total.append(" ");
				total.append(names[1]);
				itemChildMap.put("total", total.toString());
				timestamp = Long.parseLong(tradeApi.trades
						.getCurrentTimestamp()) * 1000;
				formatter = DateFormat.getDateTimeInstance();
				itemChildMap.put("date", formatter.format(new Date(timestamp)));
				ArrayList<HashMap<String, Object>> childArray = new ArrayList<HashMap<String, Object>>();
				childArray.add(itemChildMap);
				inputBox.data1.add(itemGroupMap);
				inputBox.data2.add(childArray);
			}
			timestamp = tradeApi.trades.getLocalTimestamp();
			formatter = DateFormat.getTimeInstance();
			inputBox.time = formatter.format(new Date(timestamp));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return false if has ANY troubles
	 * 
	 * @param inputBox
	 */
	public boolean getTradeHistoryData(DataBox inputBox) {
		long timestamp;
		DateFormat formatter;
		try {
			tradeApi.tradeHistory.setCount(tradeHistoryCount);
			if (!tradeApi.tradeHistory.runMethod()) {
				if (!tradeApi.tradeHistory.runMethod()) {
					return false;
				}
			}
			inputBox.data1.clear();
			inputBox.data2.clear();
			while (tradeApi.tradeHistory.hasNext()) {
				tradeApi.tradeHistory.switchNext();
				HashMap<String, Object> itemGroupMap = new HashMap<String, Object>();
				HashMap<String, Object> itemChildMap = new HashMap<String, Object>();

				itemGroupMap.put("id",
						tradeApi.tradeHistory.getCurrentOrder_id());

				if (tradeApi.tradeHistory.getCurrentType().equals("sell")) {
					itemGroupMap.put("alias", "sell");
				} else {
					itemGroupMap.put("alias", "buy");
				}
				String amount = tradeApi.tradeHistory.getCurrentAmount();
				itemGroupMap.put("amount", amount);

				String rate = tradeApi.tradeHistory.getCurrentRate();
				String names[] = tradeApi.tradeHistory.getCurrentPair().split(
						"-");
				itemChildMap.put("rate", rate);
				itemGroupMap.put("name0", names[0]);
				itemGroupMap.put("name1", names[1]);
				StringBuilder total = new StringBuilder();
				double rateDouble = Double.parseDouble(rate);
				double amountDouble = Double.parseDouble(amount);
				double totalDouble = rateDouble * amountDouble;
				total.append(TradeApi.formatDouble(totalDouble, 8));
				total.append(" ");
				total.append(names[1]);
				itemChildMap.put("total", total.toString());
				timestamp = Long.parseLong(tradeApi.tradeHistory
						.getCurrentTimestamp()) * 1000;
				formatter = DateFormat.getDateTimeInstance();
				itemChildMap.put("date", formatter.format(new Date(timestamp)));
				ArrayList<HashMap<String, Object>> childArray = new ArrayList<HashMap<String, Object>>();
				childArray.add(itemChildMap);
				inputBox.data1.add(itemGroupMap);
				inputBox.data2.add(childArray);
			}
			timestamp = tradeApi.tradeHistory.getLocalTimestamp();
			formatter = DateFormat.getTimeInstance();
			inputBox.time = formatter.format(new Date(timestamp));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return false if has ANY troubles
	 * 
	 * @param inputBox
	 */
	public boolean getTransHistoryData(DataBox inputBox) {
		long timestamp;
		DateFormat formatter;
		try {
			tradeApi.transHistory.setCount(transHistoryCount);
			if (!tradeApi.transHistory.runMethod()) {
				if (!tradeApi.transHistory.runMethod()) {
					return false;
				}
			}
			inputBox.data1.clear();
			inputBox.data2.clear();
			while (tradeApi.transHistory.hasNext()) {
				tradeApi.transHistory.switchNext();
				HashMap<String, Object> itemGroupMap = new HashMap<String, Object>();
				HashMap<String, Object> itemChildMap = new HashMap<String, Object>();
				itemGroupMap.put("id",
						tradeApi.transHistory.getCurrentPosition());
				int type = tradeApi.transHistory.getCurrentType();// 1+,2-,3?,4+,5-
				if (type == 1 || type == 4) {
					itemGroupMap.put("alias", "in");
				} else {
					itemGroupMap.put("alias", "out");
				}
				String amount = tradeApi.transHistory.getCurrentAmount();
				itemGroupMap.put("amount", amount);
				itemGroupMap.put("name",
						tradeApi.transHistory.getCurrentCurrency());
				timestamp = Long.parseLong(tradeApi.transHistory
						.getCurrentTimestamp()) * 1000;
				formatter = DateFormat.getDateTimeInstance();
				itemChildMap
						.put("desc", tradeApi.transHistory.getCurrentDesc());
				itemChildMap.put("date", formatter.format(new Date(timestamp)));
				int status = tradeApi.transHistory.getCurrentStatus();
				if (status == 2) {
					itemChildMap.put("status",
							App.context.getString(R.string.completed));
				} else {
					itemChildMap.put("status",
							App.context.getString(R.string.not_completed));
				}
				ArrayList<HashMap<String, Object>> childArray = new ArrayList<HashMap<String, Object>>();
				childArray.add(itemChildMap);
				inputBox.data1.add(itemGroupMap);
				inputBox.data2.add(childArray);
			}
			timestamp = tradeApi.transHistory.getLocalTimestamp();
			formatter = DateFormat.getTimeInstance();
			inputBox.time = formatter.format(new Date(timestamp));
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * Return false if has ANY troubles
	 */
	public boolean getDepthData(String pairName, boolean type, DataBox inputBox) {
		try {
			tradeApi.depth.resetParams();
			tradeApi.depth.addPair(pairName);
			tradeApi.depth.setLimit(depthCount);
			if (!tradeApi.depth.runMethod()) {
				if (!tradeApi.depth.runMethod()) {
					return false;
				}
			}
			inputBox.data1.clear();
			tradeApi.depth.setCurrentPair(pairName);
			if (!type) {
				while (tradeApi.depth.hasNextAsk()) {
					tradeApi.depth.switchNextAsk();
					HashMap<String, Object> itemMap = new HashMap<String, Object>();
					String price = tradeApi.depth.getCurrentAskPrice();
					String amount = tradeApi.depth.getCurrentAskAmount();
					itemMap.put("price", price);
					itemMap.put("amount", amount);
					double priceDouble = Double.parseDouble(price);
					double amountDouble = Double.parseDouble(amount);
					double totalDouble = priceDouble * amountDouble;
					itemMap.put("total", TradeApi.formatDouble(totalDouble, 8));
					inputBox.data1.add(itemMap);
				}
			} else {

				while (tradeApi.depth.hasNextBid()) {
					tradeApi.depth.switchNextBid();
					HashMap<String, Object> itemMap = new HashMap<String, Object>();
					String price = tradeApi.depth.getCurrentBidPrice();
					String amount = tradeApi.depth.getCurrentBidAmount();
					itemMap.put("price", price);
					itemMap.put("amount", amount);
					double priceDouble = Double.parseDouble(price);
					double amountDouble = Double.parseDouble(amount);
					double totalDouble = priceDouble * amountDouble;
					itemMap.put("total", TradeApi.formatDouble(totalDouble, 8));
					inputBox.data1.add(itemMap);
				}
			}
			long timestamp = tradeApi.depth.getLocalTimestamp();
			DateFormat formatter = DateFormat.getTimeInstance();
			inputBox.time = formatter.format(new Date(timestamp));
			return true;
		} catch (Exception e) {
			return false;
		}
	}
}
