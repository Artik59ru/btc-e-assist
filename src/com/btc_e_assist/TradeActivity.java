package com.btc_e_assist;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;
import com.TradeApi.Trade;
import com.TradeApi.TradeApi;
import com.btc_e_assist.R;

public class TradeActivity extends ActionBarActivity {
	public static final String INTENT_VALUE_PAIR = "name";
	public static final String INTENT_VALUE_TYPE = "type";

	private static final double STANDARD_FEE = 0.02;
	private static final long DEPTH_REUSE_LIMIT_MILLIS = 10000;
	private String currentPair = "";
	private String currentLeftName = "";
	private String currentRightName = "";
	private String myFundsResourceString = "";
	private String myBalance = "";
	private boolean isBuy = true;
	private SimpleAdapter adapter;
	private TradeControl tradeControl;
	private volatile DataBox depthBuyBox = new DataBox();
	private volatile DataBox depthSellBox = new DataBox();

	private TextView myFundsView;
	private EditText amountEdit;
	private EditText priceEdit;
	private TextView totalView;
	private TextView feeView;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		currentPair = intent.getStringExtra(INTENT_VALUE_PAIR);

		String[] names = currentPair.split("-");
		currentLeftName = names[0];
		currentRightName = names[1];
		myFundsResourceString = getString(R.string.my_funds);
		String type = intent.getStringExtra(INTENT_VALUE_TYPE);

		if (type.equals("sell")) {
			isBuy = false;
		}
		setContentView(R.layout.activity_trade);
		tradeControl = TradeControl.getInstance(this);
		ActionBar actionBar = getSupportActionBar();
		actionBar.setDisplayHomeAsUpEnabled(true);
		actionBar.setTitle(currentPair);
		final ViewGroup editsGroup = (ViewGroup) findViewById(R.id.tradeTopLayout);
		myFundsView = (TextView) findViewById(R.id.tradeMyFundsName);
		myFundsView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (priceEdit != null && amountEdit != null
						&& myBalance.length() != 0) {
					if (isBuy) {
						if (depthBuyBox.data1.size() == 0) {
							return;
						}
						Double myBalanceDouble = Double.parseDouble(myBalance);
						Double priceDouble;
						if (priceEdit.getText().toString().length() == 0) {
							String priceStr = (String) depthBuyBox.data1.get(0)
									.get("price");
							priceEdit.setText(priceStr);
							priceDouble = Double.parseDouble(priceStr);

							String resultAmount = String.valueOf(TradeApi
									.formatDouble(
											myBalanceDouble / priceDouble, 8));
							amountEdit.setText(resultAmount);
						} else {
							priceDouble = Double.valueOf(priceEdit.getText()
									.toString());
							String resultAmount = String.valueOf(TradeApi
									.formatDouble(
											myBalanceDouble / priceDouble, 8));
							amountEdit.setText(resultAmount);
						}
					} else {
						if (depthSellBox.data1.size() == 0) {
							return;
						}
						amountEdit.setText(myBalance);
					}
				}
			}
		});
		totalView = (TextView) findViewById(R.id.tradeTotalValue);
		feeView = (TextView) findViewById(R.id.tradeFeeValue);
		amountEdit = (EditText) findViewById(R.id.tradeAmountEdit);
		priceEdit = (EditText) findViewById(R.id.tradePriceEdit);
		amountEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				updateTotalFee(true, s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}

		});
		priceEdit.addTextChangedListener(new TextWatcher() {
			@Override
			public void afterTextChanged(Editable s) {
				updateTotalFee(false, s.toString());
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1,
					int arg2, int arg3) {
			}

			@Override
			public void onTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
			}

		});

		Button leftButtonView = (Button) findViewById(R.id.tradeLeftButton);
		if (isBuy) {
			leftButtonView.setBackgroundResource(R.drawable.buy_selector);
			leftButtonView.setText(R.string.buy_button);
		} else {
			leftButtonView.setBackgroundResource(R.drawable.sell_selector);
			leftButtonView.setText(R.string.sell_button);
		}
		leftButtonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isBuy) {
					Toast.makeText(TradeActivity.this, R.string.try_to_buy,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(TradeActivity.this, R.string.try_to_sell,
							Toast.LENGTH_SHORT).show();
				}
				new SimpleTradeThread().execute();
				CommonHelper.clearAllEdits(editsGroup);
			}
		});
		Button rightButtonView = (Button) findViewById(R.id.tradeRightButton);
		if (isBuy) {
			rightButtonView.setText(R.string.just_buy_button);
			rightButtonView.setTextColor(getResources().getColor(
					R.color.Green70));
		} else {
			rightButtonView.setText(R.string.just_sell_button);
			rightButtonView
					.setTextColor(getResources().getColor(R.color.Red70));
		}
		rightButtonView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				if (isBuy) {
					Toast.makeText(TradeActivity.this, R.string.try_to_buy,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(TradeActivity.this, R.string.try_to_sell,
							Toast.LENGTH_SHORT).show();
				}
				new ExtendedTradeThread().execute();
				CommonHelper.clearAllEdits(editsGroup);
			}
		});

		ListView listView = (ListView) findViewById(R.id.tradeDepthList);
		String[] from = { "price", "amount", "total" };
		int[] to = { R.id.itemDepthPrice, R.id.itemDepthAmount,
				R.id.itemDepthTotal };
		if (isBuy) {
			adapter = new SimpleAdapter(this, depthBuyBox.data1,
					R.layout.item_depth_list, from, to);
		} else {
			adapter = new SimpleAdapter(this, depthSellBox.data1,
					R.layout.item_depth_list, from, to);
		}
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> adapter, View view,
					int position, long id) {
				String price;
				try {
					if (isBuy) {
						if (depthBuyBox.data1.size() > 0) {
							price = (String) depthBuyBox.data1.get(position)
									.get("price");
							priceEdit.setText(price);
						}
					} else {
						if (depthSellBox.data1.size() > 0) {
							price = (String) depthSellBox.data1.get(position)
									.get("price");
							priceEdit.setText(price);
						}
					}
				} catch (Exception e) {
				}
			}
		});
		TextView amountDepthView = (TextView) findViewById(R.id.tradeDepthAmountName);
		TextView totalDepthView = (TextView) findViewById(R.id.tradeDepthTotalName);
		amountDepthView.append(" " + currentLeftName);
		totalDepthView.append(" " + currentRightName);
		updateInfo();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.standard_actions, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/**
	 * 
	 * @param type
	 *            If this method amount - true, if price then false.
	 * 
	 */
	void updateTotalFee(boolean type, String content) {
		if (priceEdit != null && amountEdit != null) {
			try {
				double amount;
				double price;
				if (type) {
					amount = Double.parseDouble(content);
					price = Double.parseDouble(priceEdit.getText().toString());
				} else {
					amount = Double
							.parseDouble(amountEdit.getText().toString());
					price = Double.parseDouble(content);
				}
				double total = price * amount;
				StringBuilder buffer = new StringBuilder(
						String.valueOf(TradeApi.formatDouble(total, 8)));
				buffer.append(" ");
				if (isBuy) {
					buffer.append(currentRightName);
				} else {
					buffer.append(currentLeftName);
				}
				totalView.setText(buffer);
				buffer.delete(0, buffer.length());

				if (tradeControl.tradeApi.info.isSuccess()) {
					tradeControl.tradeApi.info.setCurrentPair(currentPair);
					double fee = tradeControl.tradeApi.info.getCurrentFee();
					buffer.append(String.valueOf(TradeApi.formatDouble(total
							* (fee/10), 8)));
				} else {
					buffer.append('~');
					buffer.append(String.valueOf(TradeApi.formatDouble(total
							* STANDARD_FEE, 8)));
				}
				buffer.append(" ");
				if (isBuy) {
					buffer.append(currentRightName);
				} else {
					buffer.append(currentLeftName);
				}
				feeView.setText(buffer);
			} catch (Exception e) {
			}
		}
	}

	private void updateInfo() {
		CommonHelper.makeToastUpdating(this, currentPair);
		new DepthThread().execute();
		new MyFundsThread().execute();
	}

	private void updateMyFunds() {
		StringBuilder str = new StringBuilder(String.format(
				myFundsResourceString, myBalance));
		str.append(" ");
		if (isBuy) {
			str.append(currentRightName);
		} else {
			str.append(currentLeftName);
		}
		myFundsView.setText(str);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			updateInfo();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class DepthThread extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... arg0) {
			if (isBuy) {
				return tradeControl.getDepthData(currentPair, isBuy,
						depthBuyBox);
			} else {
				return tradeControl.getDepthData(currentPair, isBuy,
						depthSellBox);
			}
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result.booleanValue()) {
				if (adapter != null) {
					adapter.notifyDataSetChanged();
					CommonHelper.makeToastUpdated(TradeActivity.this,
							currentPair);
				}
			} else {
				CommonHelper.makeToastErrorConnection(TradeActivity.this,
						currentPair);
			}
		}
	}

	private class MyFundsThread extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... arg0) {
			if (!tradeControl.tradeApi.getInfo.runMethod()) {
				if (!tradeControl.tradeApi.getInfo.runMethod()) {
					return false;
				}
			}
			if (isBuy) {
				myBalance = tradeControl.tradeApi.getInfo
						.getBalance(currentRightName);
			} else {
				myBalance = tradeControl.tradeApi.getInfo
						.getBalance(currentLeftName);
			}
			return true;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (result.booleanValue()) {
				updateMyFunds();
			}
		}
	}

	private class SimpleTradeThread extends AsyncTask<Void, Void, Void> {
		Trade result;
		String errorMessage;

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				String rate = priceEdit.getText().toString();
				String amount = amountEdit.getText().toString();
				result = tradeControl.tradeApi.extendedTrade(currentPair,
						isBuy, rate, amount);
			} catch (Exception e) {
				errorMessage = e.getMessage();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void arg) {
			super.onPostExecute(arg);
			if (errorMessage != null) {
				if (errorMessage
						.equals(tradeControl.tradeApi.EXCEPTION_INVALID_AMOUNT)) {
					Toast.makeText(TradeActivity.this, R.string.invalid_amount,
							Toast.LENGTH_SHORT).show();
				} else if (errorMessage
						.equals(tradeControl.tradeApi.EXCEPTION_TOO_LOW_AMOUNT)) {
					Toast.makeText(TradeActivity.this, R.string.too_low_amount,
							Toast.LENGTH_SHORT).show();
				} else if (errorMessage
						.equals(tradeControl.tradeApi.EXCEPTION_INVALID_RATE)) {
					Toast.makeText(TradeActivity.this, R.string.invalid_price,
							Toast.LENGTH_SHORT).show();
				} else if (errorMessage
						.equals(tradeControl.tradeApi.EXCEPTION_TOO_LOW_RATE)) {
					Toast.makeText(TradeActivity.this, R.string.too_low_price,
							Toast.LENGTH_SHORT).show();
				} else if (errorMessage
						.equals(tradeControl.tradeApi.EXCEPTION_TOO_HIGH_RATE)) {
					Toast.makeText(TradeActivity.this, R.string.too_high_price,
							Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(TradeActivity.this, R.string.error,
							Toast.LENGTH_SHORT).show();
				}
				return;
			}
			if (result.isSuccess()) {
				StringBuilder str = new StringBuilder();
				str.append(getString(R.string.trade_result));
				str.append("\n");
				str.append(getString(R.string.received));
				str.append(result.getReceived());
				str.append(" ");
				str.append(currentLeftName);
				str.append("\n");
				str.append(getString(R.string.remains));
				str.append(result.getRemains());
				str.append(" ");
				str.append(currentLeftName);
				myBalance = result.getBalance(currentRightName);
				updateMyFunds();
				Toast.makeText(TradeActivity.this, str, Toast.LENGTH_LONG)
						.show();
			} else {
				if (result.getErrorMessage().length() == 0) {
					CommonHelper.makeToastErrorConnection(TradeActivity.this,
							currentPair);
				} else {
					Toast.makeText(TradeActivity.this,
							result.getErrorMessage(), Toast.LENGTH_SHORT)
							.show();
				}
			}
		}
	}

	private class ExtendedTradeThread extends AsyncTask<Void, Void, String> {
		@Override
		protected String doInBackground(Void... arg0) {
			double result;
			try {
				if (isBuy) {
					result = tradeControl.tradeApi.tryMaximumBuy(currentPair,
							DEPTH_REUSE_LIMIT_MILLIS);
				} else {
					result = tradeControl.tradeApi.tryMaximumSell(currentPair);
				}
			} catch (Exception e) {
				try {
					if (isBuy) {
						result = tradeControl.tradeApi.tryMaximumBuy(
								currentPair, DEPTH_REUSE_LIMIT_MILLIS);
					} else {
						result = tradeControl.tradeApi
								.tryMaximumSell(currentPair);
					}
				} catch (Exception ex) {
					return null;
				}
			}
			return TradeApi.formatDouble(result, 8);
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			if (result != null) {
				myBalance = result;
				updateMyFunds();
				CommonHelper.makeToastUpdated(TradeActivity.this, currentPair);
			} else {
				CommonHelper.makeToastErrorConnection(TradeActivity.this,
						currentPair);
			}
		}
	}
}
