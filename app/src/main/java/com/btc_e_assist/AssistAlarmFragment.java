package com.btc_e_assist;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;

public class AssistAlarmFragment extends Fragment {
    public static final String CONDITION_HIGHER = "HIGHER";
    public static final String CONDITION_LOWER = "LOWER";
    public static final String CONDITION_UNDEFINED = "UNDEFINED";
    private static volatile DataBox tickerBox = new DataBox();
    private static int spinnerLeftSavedPosition = 0;
    private static int spinnerRightSavedPosition = 0;
    private Context mContext;
    private PrefControl pControl;
    private TradeControl tradeControl;
    private DBControl dbControl;
    private Spinner spinnerLeft;
    private Spinner spinnerRight;
    private Spinner spinnerCondition;
    private EditText priceEdit;
    private String[] mainPairList;
    private StringBuilder currentPair = new StringBuilder();
    private ArrayList<String> leftList = new ArrayList<String>();
    private ArrayList<String> rightList = new ArrayList<String>();
    private ArrayAdapter<String> adapterLeft;
    private ArrayAdapter<String> adapterRight;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = activity;
        tradeControl = TradeControl.getInstance();
        pControl = PrefControl.getInstance();
        dbControl = DBControl.getInstance();
        new SecondThread().execute();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_assist_alarm, null);
        TextView textLabel = (TextView) rootView
                .findViewById(R.id.assistAlarmTextLabel);
        TextView textTips = (TextView) rootView
                .findViewById(R.id.assistAlarmTips);
        textLabel.setTypeface(CommonHelper.fontRobotoThin);
        textTips.setTypeface(CommonHelper.fontRobotoLight);

        spinnerLeft = (Spinner) rootView
                .findViewById(R.id.assistAlarmSpinnerLeftPair);
        spinnerRight = (Spinner) rootView
                .findViewById(R.id.assistAlarmSpinnerRightPair);
        spinnerCondition = (Spinner) rootView
                .findViewById(R.id.assistAlarmSpinnerCondition);
        priceEdit = (EditText) rootView.findViewById(R.id.assistAlarmEditPrice);
        Button addButton = (Button) rootView
                .findViewById(R.id.assistAlarmButton);

        if (!pControl.isSavedPairsList()) {
            Toast.makeText(mContext, R.string.pair_list_not_available,
                    Toast.LENGTH_SHORT).show();
        }
        mainPairList = pControl.getPairsList();

        ArrayAdapter<CharSequence> conditionAdapter = ArrayAdapter
                .createFromResource(mContext, R.array.assist_alarm_conditions,
                        android.R.layout.simple_spinner_item);
        conditionAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCondition.setAdapter(conditionAdapter);

        CommonHelper.fillLeftList(mainPairList, leftList, adapterLeft);
        adapterLeft = new ArrayAdapter<String>(mContext,
                R.layout.item_pairname_spinner, leftList);
        adapterLeft
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLeft.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long rowId) {
                spinnerRight.setSelection(0);
                CommonHelper.fillRightList(mainPairList, rightList,
                        adapterRight, leftList.get(position));
                if (tickerBox.data1.size() != 0) {
                    updatePriceEdit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        spinnerLeft.setAdapter(adapterLeft);

        adapterRight = new ArrayAdapter<String>(mContext,
                R.layout.item_pairname_spinner, rightList);
        adapterRight
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerRight.setAdapter(adapterRight);
        spinnerRight.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapter, View view,
                                       int position, long rowId) {
                if (spinnerRightSavedPosition != 0) {
                    int temp = spinnerRightSavedPosition;
                    spinnerRightSavedPosition = 0;
                    spinnerRight.setSelection(temp);
                    return;
                }
                updatePriceEdit();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {
            }

        });
        final ViewGroup group = (ViewGroup) rootView
                .findViewById(R.id.assistAlarmEditsLayout);
        addButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View arg0) {
                try {
                    double price = Double.parseDouble(priceEdit.getText()
                            .toString());
                    StringBuilder pairName = new StringBuilder();
                    pairName.append(spinnerLeft.getSelectedItem());
                    pairName.append("-");
                    pairName.append(spinnerRight.getSelectedItem());
                    String condition;
                    switch (spinnerCondition.getSelectedItemPosition()) {
                        case 0:
                            condition = CONDITION_HIGHER;
                            break;
                        case 1:
                            condition = CONDITION_LOWER;
                            break;
                        default:
                            condition = CONDITION_UNDEFINED;
                    }
                    dbControl.addAlarm(pairName.toString(), condition, price);
                    AssistSynchronize.synchronizedWithDB();
                    CommonHelper.clearAllEdits(group);
                    Toast.makeText(mContext, R.string.alarm_added,
                            Toast.LENGTH_SHORT).show();
                } catch (Exception e) {
                    Toast.makeText(mContext, R.string.enter_price,
                            Toast.LENGTH_SHORT).show();
                }
            }
        });
        return rootView;
    }

    @Override
    public void onPause() {
        super.onPause();
        if (spinnerLeft != null && spinnerRight != null) {
            spinnerLeftSavedPosition = spinnerLeft.getSelectedItemPosition();
            spinnerRightSavedPosition = spinnerRight.getSelectedItemPosition();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (spinnerLeft != null && spinnerRight != null) {
            if (spinnerLeftSavedPosition != 0) {
                spinnerLeft.setSelection(spinnerLeftSavedPosition);
            }
        }
    }

    void updatePriceEdit() {
        if (spinnerLeft != null && spinnerRight != null) {
            currentPair.delete(0, currentPair.length());
            currentPair.append(spinnerLeft.getSelectedItem());
            currentPair.append("-");
            currentPair.append(spinnerRight.getSelectedItem());
            if (tickerBox.data1.size() > 0) {
                for (HashMap<String, Object> map : tickerBox.data1) {
                    if (map.get("name").equals(currentPair.toString())) {
                        String defaultHint = getString(R.string.price);
                        StringBuilder hint = new StringBuilder(defaultHint);
                        hint.append(" (");
                        hint.append(map.get("last"));
                        hint.append(")");
                        priceEdit.setHint(hint.toString());
                    }
                }
            }
        }
    }

    private class SecondThread extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... arg0) {
            return tradeControl.loadTickerData(pControl.getPairsList());
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            if (!isAdded()) {
                return;
            }
            if (result.booleanValue() && tradeControl.setTickerData(tickerBox)) {
                updatePriceEdit();
            }
        }
    }
}
