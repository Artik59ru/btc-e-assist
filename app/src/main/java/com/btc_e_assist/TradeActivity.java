package com.btc_e_assist;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.assist.Trade;
import com.assist.TradeApi;

public class TradeActivity extends ActionBarActivity {
    public static final String INTENT_VALUE_PAIR = "name";
    public static final String INTENT_VALUE_TYPE = "type";

    private static final double STANDARD_FEE = 0.002;
    private static final long DEPTH_REUSE_LIMIT_MILLIS = 10000;
    private static final double AMOUNT_BUTTON_MULTIPLIER = 0.1;
    private static final double PRICE_BUTTON_MULTIPLIER = 0.01;
    private String currentPair = "";
    private String currentLeftName = "";
    private String currentRightName = "";
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
    private ViewGroup editsGroup;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        currentPair = intent.getStringExtra(INTENT_VALUE_PAIR);

        String[] names = currentPair.split("-");
        currentLeftName = names[0];
        currentRightName = names[1];
        String type = intent.getStringExtra(INTENT_VALUE_TYPE);

        if (type.equals("sell")) {
            isBuy = false;
        }
        setContentView(R.layout.activity_trade);
        tradeControl = TradeControl.getInstance();
        final PrefControl pControl = PrefControl.getInstance();
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setTitle(currentPair);
        editsGroup = (ViewGroup) findViewById(R.id.tradeTopLayout);
        ToggleButton switchButton = (ToggleButton) findViewById(R.id.tradeSwitchButton);
        switchButton.setChecked(pControl.getCurrentTradeSwitchState());
        switchButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                pControl.setTradeSwitchState(b);
            }
        });

        myFundsView = (TextView) findViewById(R.id.tradeMyFundsName);
        myFundsView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                amountEdit.setText(calculateMaxAmountToBuy());
            }
        });

        totalView = (TextView) findViewById(R.id.tradeTotalValue);
        feeView = (TextView) findViewById(R.id.tradeFeeValue);
        amountEdit = (EditText) findViewById(R.id.tradeAmountEdit);
        ImageButton amountClearButton = (ImageButton) findViewById(R.id.tradeAmountClearButton);
        amountClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                amountEdit.setText("");
            }
        });
        ImageButton amountMinusButton = (ImageButton) findViewById(R.id.tradeAmountMinusButton);
        amountMinusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                amountAction(false);
            }
        });

        ImageButton amountPlusButton = (ImageButton) findViewById(R.id.tradeAmountPlusButton);
        amountPlusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                amountAction(true);
            }
        });

        priceEdit = (EditText) findViewById(R.id.tradePriceEdit);
        ImageButton priceClearButton = (ImageButton) findViewById(R.id.tradePriceClearButton);
        priceClearButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                priceEdit.setText("");
            }
        });
        ImageButton priceMinusButton = (ImageButton) findViewById(R.id.tradePriceMinusButton);
        priceMinusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                priceAction(false);
            }
        });

        ImageButton pricePlusButton = (ImageButton) findViewById(R.id.tradePricePlusButton);
        pricePlusButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                priceAction(true);
            }
        });

        priceEdit.setOnEditorActionListener(new EditTextAction());
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
                if (pControl.getCurrentTradeSwitchState()) {
                    leftButtonAction();
                } else {
                    CommonHelper.makeToastButtonDisabled(TradeActivity.this);
                }
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
                if (pControl.getCurrentTradeSwitchState()) {
                    if (!tradeControl.tradeApi.isKeysInstalled()) {
                        CommonHelper.makeToastNoKeys(TradeActivity.this);
                    } else {
                        if (isBuy) {
                            Toast.makeText(TradeActivity.this, R.string.try_to_buy,
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TradeActivity.this,
                                    R.string.try_to_sell, Toast.LENGTH_SHORT)
                                    .show();
                        }
                        new ExtendedTradeThread().execute();
                    }
                } else {
                    CommonHelper.makeToastButtonDisabled(TradeActivity.this);
                }
            }
        });

        ListView listView = (ListView) findViewById(R.id.tradeDepthList);
        String[] from = {"price", "amount", "total"};
        int[] to = {R.id.itemDepthPrice, R.id.itemDepthAmount,
                R.id.itemDepthTotal};
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

    void leftButtonAction() {
        if (!tradeControl.tradeApi.isKeysInstalled()) {
            CommonHelper.makeToastNoKeys(this);
        } else {
            if (isBuy) {
                Toast.makeText(TradeActivity.this, R.string.try_to_buy,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(TradeActivity.this, R.string.try_to_sell,
                        Toast.LENGTH_SHORT).show();
            }
            new SimpleTradeThread().execute();
        }
    }

    /**
     * @param type If this method is amount - true, if it's price then false.
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
                        TradeApi.formatDouble(total, 8));
                buffer.append(" ");
                buffer.append(currentRightName);
                totalView.setText(buffer);
                buffer.delete(0, buffer.length());

                if (tradeControl.tradeApi.info.isSuccess()) {
                    tradeControl.tradeApi.info.setCurrentPair(currentPair);
                    double fee = tradeControl.tradeApi.info.getCurrentFee();
                    if (isBuy) {
                        buffer.append(TradeApi.formatDouble(amount
                                * (fee / 100), 8));
                    } else {
                        buffer.append(TradeApi.formatDouble(
                                total * (fee / 100), 8));
                    }
                } else {
                    buffer.append('~');
                    if (isBuy) {
                        buffer.append(TradeApi.formatDouble(amount
                                * STANDARD_FEE, 8));
                    } else {
                        buffer.append(TradeApi.formatDouble(total
                                * STANDARD_FEE, 8));
                    }
                }
                buffer.append(" ");
                if (isBuy) {
                    buffer.append(currentLeftName);
                } else {
                    buffer.append(currentRightName);
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
        StringBuilder str = new StringBuilder(myBalance);
        str.append(" ");
        if (isBuy) {
            str.append(currentRightName);
        } else {
            str.append(currentLeftName);
        }
        myFundsView.setText(str);
    }

    private String calculateMaxAmountToBuy() {
        if (priceEdit != null && amountEdit != null
                && myBalance.length() != 0) {
            if (isBuy) {
                if (depthBuyBox.data1.size() == 0) {
                    return "";
                }
                double myBalanceDouble = Double.parseDouble(myBalance);
                double priceDouble;
                if (priceEdit.getText().length() == 0) {
                    String priceStr = (String) depthBuyBox.data1.get(0)
                            .get("price");
                    priceEdit.setText(priceStr);
                    priceDouble = Double.parseDouble(priceStr);
                } else {
                    priceDouble = Double.valueOf(priceEdit.getText()
                            .toString());
                }
                double resultAmountDouble = myBalanceDouble
                        / priceDouble - 0.0000002;
                String resultAmount = TradeApi
                        .formatDouble(resultAmountDouble, 8);
                return resultAmount;
            } else {
                if (depthSellBox.data1.size() == 0) {
                    return "";
                }
                return myBalance;
            }
        }
        return "";
    }

    /**
     * @param type false is minus, true is plus
     */
    private void priceAction(boolean type) {
        if (priceEdit == null) {
            return;
        }
        try {
            double price;
            if (priceEdit.getText().length() == 0) {
                if (isBuy) {
                    if (depthBuyBox.data1.size() == 0) {
                        return;
                    }
                    price = Double.parseDouble((String) depthBuyBox.data1.get(0).get("price"));
                } else {
                    if (depthSellBox.data1.size() == 0) {
                        return;
                    }
                    price = Double.parseDouble((String) depthSellBox.data1.get(0).get("price"));
                }
            } else {
                price = Double.parseDouble(priceEdit.getText().toString());
            }
            if (type) {
                price *= 1 + PRICE_BUTTON_MULTIPLIER;
            } else {
                price *= 1 - PRICE_BUTTON_MULTIPLIER;
            }
            if (currentPair.length() != 0 && tradeControl.tradeApi.info.isSuccess()) {
                tradeControl.tradeApi.info.setCurrentPair(currentPair);
                int decPlaces = tradeControl.tradeApi.info.getCurrentDecimalPlaces();
                priceEdit.setText(TradeApi.formatDouble(price, decPlaces));
            } else {
                priceEdit.setText(TradeApi.formatDouble(price, 8));
            }
        } catch (Exception e) {
        }
    }

    /**
     * @param type false is minus, true is plus
     */
    private void amountAction(boolean type) {
        if (amountEdit == null) {
            return;
        }
        try {
            double amount;
            if (isBuy) {
                if (amountEdit.getText().length() == 0) {
                    try {
                        amount = Double.parseDouble(calculateMaxAmountToBuy());
                    } catch (Exception e) {
                        return;
                    }
                } else {
                    amount = Double.parseDouble(amountEdit.getText().toString());
                }
                if (type) {
                    amount *= 1 + AMOUNT_BUTTON_MULTIPLIER;
                } else {
                    amount *= 1 - AMOUNT_BUTTON_MULTIPLIER;
                }
            } else {
                if (amountEdit.getText().length() == 0) {
                    try {
                        amount = Double.parseDouble(calculateMaxAmountToBuy());
                        if (!type) {
                            amount *= 1 - AMOUNT_BUTTON_MULTIPLIER;
                        }
                    } catch (Exception e) {
                        return;
                    }
                } else {
                    double maxAmount = 0.0;
                    try {
                        maxAmount = Double.parseDouble(calculateMaxAmountToBuy());
                    } catch (Exception e) {
                    }
                    amount = Double.parseDouble(amountEdit.getText().toString());
                    if (maxAmount == 0.0) {
                        if (type) {
                            amount *= 1 + AMOUNT_BUTTON_MULTIPLIER;
                        } else {
                            amount *= 1 - AMOUNT_BUTTON_MULTIPLIER;
                        }
                    } else {
                        if (type) {
                            if (amount * (1 + AMOUNT_BUTTON_MULTIPLIER) >= maxAmount) {
                                amount = maxAmount;
                            } else {
                                amount *= 1 + AMOUNT_BUTTON_MULTIPLIER;
                            }
                        } else {
                            amount *= 1 - AMOUNT_BUTTON_MULTIPLIER;
                        }
                    }
                }
            }
            amountEdit.setText(TradeApi.formatDouble(amount, 8));
        } catch (Exception e) {
        }
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
            return tradeControl.loadDepthData(currentPair);
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (result.booleanValue()) {
                boolean isSet = false;
                if (isBuy) {
                    isSet = tradeControl.setDepthData(isBuy, depthBuyBox);
                } else {
                    isSet = tradeControl.setDepthData(isBuy, depthSellBox);
                }
                if (isSet) {
                    if (adapter != null) {
                        adapter.notifyDataSetChanged();
                        CommonHelper.makeToastUpdated(TradeActivity.this,
                                currentPair);
                    }
                    return;
                }
            }
            CommonHelper.makeToastErrorConnection(TradeActivity.this,
                    currentPair);
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
                if (!result.isSuccess()) {
                    result = tradeControl.tradeApi.extendedTrade(currentPair,
                            isBuy, rate, amount);
                }
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
                if (isBuy) {
                    str.append(getString(R.string.bought));
                } else {
                    str.append(getString(R.string.sold));
                }
                str.append(result.getReceived());
                str.append(" ");
                str.append(currentLeftName);
                str.append("\n");
                str.append(getString(R.string.remains));
                str.append(result.getRemains());
                str.append(" ");
                str.append(currentLeftName);
                if (isBuy) {
                    myBalance = result.getBalance(currentRightName);
                } else {
                    myBalance = result.getBalance(currentLeftName);
                }
                updateMyFunds();
                Toast.makeText(TradeActivity.this, str, Toast.LENGTH_LONG)
                        .show();
                CommonHelper.clearAllEdits(editsGroup);
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
                    if (ex.getMessage().equals(
                            tradeControl.tradeApi.EXCEPTION_TOO_LOW_AMOUNT)) {
                        return "too_low";
                    }
                    return null;
                }
            }
            return TradeApi.formatDouble(result, 8);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) {
                if (result.equals("too_low")) {
                    Toast.makeText(TradeActivity.this, R.string.too_low_amount,
                            Toast.LENGTH_SHORT).show();
                    return;
                }
                myBalance = result;
                updateMyFunds();
                CommonHelper.makeToastUpdated(TradeActivity.this, currentPair);
                CommonHelper.clearAllEdits(editsGroup);
            } else {
                CommonHelper.makeToastErrorConnection(TradeActivity.this,
                        currentPair);
            }
        }
    }

    private class EditTextAction implements OnEditorActionListener {

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                leftButtonAction();
            }
            return false;
        }

    }
}
