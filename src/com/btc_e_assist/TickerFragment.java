package com.btc_e_assist;

import java.text.DecimalFormat;
import java.text.FieldPosition;
import java.text.Format;
import java.text.ParsePosition;
import java.util.ArrayList;
import java.util.HashMap;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.TradeApi.TradeApi;
import com.androidplot.Plot;
import com.androidplot.ui.AnchorPosition;
import com.androidplot.ui.SizeLayoutType;
import com.androidplot.ui.SizeMetrics;
import com.androidplot.ui.XLayoutStyle;
import com.androidplot.ui.YLayoutStyle;
import com.androidplot.ui.widget.TextLabelWidget;
import com.androidplot.util.PixelUtils;
import com.androidplot.xy.LineAndPointFormatter;
import com.androidplot.xy.XYPlot;
import com.androidplot.xy.XYSeries;
import com.androidplot.xy.XYStepMode;

public class TickerFragment extends Fragment {
	public static final String INTENT_VALUE = "pair";
	private static final long MIN_ALLOWED_LAST_UPDATE_AGE = 200;

	private static final DecimalFormat LOW_FORMAT = new DecimalFormat(
			"#.######");
	private static final DecimalFormat MED_FORMAT = new DecimalFormat("#.###");
	private static final DecimalFormat HIGH_FORMAT = new DecimalFormat("#");

	private Resources resources;
	private Context mContext;
	private PrefControl pControl;
	private TradeControl tradeControl;

	private ArrayList<String> leftList = new ArrayList<String>();
	private ArrayList<String> rightList = new ArrayList<String>();
	private ArrayAdapter<String> adapterLeft;
	private ArrayAdapter<String> adapterRight;

	public static Spinner spinnerLeft;
	public static Spinner spinnerRight;
	private TextView lastView;
	private TextView minView;
	private TextView maxView;
	private TextView volView;
	private XYPlot plot;
	private TextLabelWidget titleWidget;

	private String[] mainPairList;
	private static volatile DataBox tickerBox = new DataBox();
	private String currentFragmentName = "";

	private static int spinnerLeftSavedPosition = 0;
	private static int spinnerRightSavedPosition = 0;

	private TickerDataThread tickerDataThread;
	private GraphThread graphThread;

	private long lastUpdateCalled = 0;

	private boolean isNormalLand = false;

	@SuppressLint("DefaultLocale")
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		mContext = activity;
		currentFragmentName = getTag();
		resources = getResources();
		tradeControl = TradeControl.getInstance();
		pControl = PrefControl.getInstance();
		mainPairList = pControl.getPairsList();
		String pairFromIntent = activity.getIntent().getStringExtra(
				INTENT_VALUE);
		if (pairFromIntent != null) {
			pairFromIntent = pairFromIntent.replace('_', '-').toUpperCase();
			int[] positions = CommonHelper.getPositionsForPair(mainPairList,
					pairFromIntent);
			spinnerLeftSavedPosition = positions[0];
			spinnerRightSavedPosition = positions[1];

		}
		CommonHelper.showPasswordDialog(mContext);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		setHasOptionsMenu(true);
		View rootView = inflater.inflate(R.layout.fragment_ticker, null);
		spinnerLeft = (Spinner) rootView.findViewById(R.id.tickerSpinnerLeft);
		spinnerRight = (Spinner) rootView.findViewById(R.id.tickerSpinnerRight);
		lastView = (TextView) rootView.findViewById(R.id.tickerLastValue);
		minView = (TextView) rootView.findViewById(R.id.tickerMinValue);
		if (minView != null) {
			maxView = (TextView) rootView.findViewById(R.id.tickerMaxValue);
		} else {
			isNormalLand = true;
		}
		volView = (TextView) rootView.findViewById(R.id.tickerVolumeValue);
		plot = (XYPlot) rootView.findViewById(R.id.mySimpleXYPlot);

		if (tickerBox.data1.size() > 0) {
			HashMap<String, Object> map = tickerBox.data1.get(0);
			lastView.setText(map.get("last").toString());
			if (!isNormalLand) {
				minView.setText(map.get("low").toString());
				maxView.setText(map.get("high").toString());
			}
			volView.setText(map.get("volume").toString());
		}

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
				updateTicker();
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
				updateTicker();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
			}

		});
		spinnerRight.setAdapter(adapterRight);

		Button buyButton = (Button) rootView.findViewById(R.id.tickerBuyButton);
		buyButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String pair = getCurrentPairName();
				if (pair != null) {
					Intent intent = new Intent(mContext, TradeActivity.class);
					intent.putExtra(TradeActivity.INTENT_VALUE_PAIR, pair);
					intent.putExtra(TradeActivity.INTENT_VALUE_TYPE, "buy");
					startActivity(intent);
				}
			}
		});
		Button sellButton = (Button) rootView
				.findViewById(R.id.tickerSellButton);
		sellButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				String pair = getCurrentPairName();
				if (pair != null) {
					Intent intent = new Intent(mContext, TradeActivity.class);
					intent.putExtra(TradeActivity.INTENT_VALUE_PAIR, pair);
					intent.putExtra(TradeActivity.INTENT_VALUE_TYPE, "sell");
					startActivity(intent);
				}
			}
		});
		return rootView;
	}

	@SuppressWarnings("serial")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		LineAndPointFormatter formatter = new LineAndPointFormatter();
		formatter.configure(mContext, R.xml.line_formatter);
		plot.getGraphWidget()
				.setSize(
						new SizeMetrics(0, SizeLayoutType.FILL, 0,
								SizeLayoutType.FILL));
		plot.getGraphWidget().getBackgroundPaint()
				.setColor(resources.getColor(R.color.Gray2));
		plot.getGraphWidget().getGridBackgroundPaint()
				.setColor(resources.getColor(R.color.Gray2));
		plot.getGraphWidget().getDomainLabelPaint()
				.setColor(resources.getColor(R.color.GraphText));
		plot.getGraphWidget().getRangeLabelPaint()
				.setColor(resources.getColor(R.color.GraphText));

		plot.getGraphWidget().getDomainOriginLinePaint()
				.setColor(resources.getColor(R.color.GraphText));
		plot.getGraphWidget().getRangeOriginLinePaint()
				.setColor(resources.getColor(R.color.GraphText));
		plot.getGraphWidget().setDomainLabelVerticalOffset(1);
		plot.getGraphWidget().setDomainLabelHorizontalOffset(4);
		plot.getGraphWidget().setRangeLabelVerticalOffset(-2);

		plot.setBorderStyle(Plot.BorderStyle.NONE, null, null);
		plot.setPlotMargins(0, 0, 0, 0);
		plot.setPlotPadding(0, 0, 0, 0);

		plot.getLayoutManager().remove(plot.getLegendWidget());
		titleWidget = new TextLabelWidget(plot.getLayoutManager(),
				new SizeMetrics(PixelUtils.dpToPix(100),
						SizeLayoutType.ABSOLUTE, PixelUtils.dpToPix(200),
						SizeLayoutType.ABSOLUTE));
		titleWidget.getLabelPaint().setTextSize(30);
		titleWidget.getLabelPaint().setTypeface(
				Typeface.create("", Typeface.BOLD));
		titleWidget.position(0, XLayoutStyle.RELATIVE_TO_CENTER, 10,
				YLayoutStyle.ABSOLUTE_FROM_TOP, AnchorPosition.TOP_MIDDLE);
		titleWidget.pack();
		plot.setTitleWidget(titleWidget);

		plot.setDomainStep(XYStepMode.INCREMENT_BY_VAL, 1);
		plot.setDomainValueFormat(new Format() {
			@Override
			public StringBuffer format(Object arg0, StringBuffer arg1,
					FieldPosition arg2) {
				int index = ((Number) arg0).intValue();
				if (index >= HtmlCutter.chartTimeData.size() || index < 0) {
					return new StringBuffer("");
				} else {
					return new StringBuffer(HtmlCutter.chartTimeData.get(index));
				}
			}

			@Override
			public Object parseObject(String arg0, ParsePosition arg1) {
				return null;
			}

		});
		plot.setTicksPerDomainLabel(4);
		plot.setTicksPerRangeLabel(2);
		plot.getGraphWidget().setDomainLabelOrientation(-90);
		plot.addSeries(new MyDynamicSeries(), formatter);
	}

	@Override
	public void onPause() {
		super.onPause();
		if (spinnerLeft != null && spinnerRight != null) {
			spinnerLeftSavedPosition = spinnerLeft.getSelectedItemPosition();
			spinnerRightSavedPosition = spinnerRight.getSelectedItemPosition();
		}
		cancelUpdating();
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

	public static String getCurrentPairName() {
		if (spinnerLeft != null && spinnerRight != null) {
			StringBuilder currentPair = new StringBuilder();
			currentPair.append(spinnerLeft.getSelectedItem());
			currentPair.append("-");
			currentPair.append(spinnerRight.getSelectedItem());
			return currentPair.toString();
		} else {
			return null;
		}
	}

	private void updateTicker() {
		cancelUpdating();
		String currentPair = getCurrentPairName();
		if (currentPair != null) {
			tickerDataThread = new TickerDataThread();
			tickerDataThread.execute(currentPair);
			graphThread = new GraphThread();
			graphThread.execute(currentPair);
			long delta = System.currentTimeMillis() - lastUpdateCalled;
			if (delta > MIN_ALLOWED_LAST_UPDATE_AGE) {
				CommonHelper.makeToastUpdating(mContext, currentFragmentName);
			}
			lastUpdateCalled = System.currentTimeMillis();
		}
	}

	private void cancelUpdating() {
		if (tickerDataThread != null) {
			tickerDataThread.cancel(false);
		}
		if (graphThread != null) {
			graphThread.cancel(false);
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_refresh:
			updateTicker();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private class TickerDataThread extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... arg0) {
			return tradeControl.loadTickerData(arg0);

		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isAdded()) {
				return;
			}
			if (result.booleanValue() && tradeControl.setTickerData(tickerBox)) {
				HashMap<String, Object> map = tickerBox.data1.get(0);
				String last = (String) map.get("last");
				double lastDouble = Double.parseDouble(last);
				if (lastDouble < 1) {
					plot.setRangeValueFormat(LOW_FORMAT);
				} else if (lastDouble < 100) {
					plot.setRangeValueFormat(MED_FORMAT);
				} else {
					plot.setRangeValueFormat(HIGH_FORMAT);
				}
				lastView.setText(last);
				if (!isNormalLand) {
					minView.setText((String) map.get("low"));
					maxView.setText((String) map.get("high"));
				}
				volView.setText((String) map.get("volume"));
				CommonHelper.makeToastUpdated(mContext, currentFragmentName);
			} else {
				CommonHelper.makeToastErrorConnection(mContext,
						currentFragmentName);
			}
		}
	}

	private class GraphThread extends AsyncTask<String, Void, Boolean> {

		@Override
		protected Boolean doInBackground(String... arg0) {
			return HtmlCutter.loadChartData(arg0[0]);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (!isAdded()) {
				return;
			}
			if (result.booleanValue() && HtmlCutter.setChartData()) {
				StringBuilder resultText = new StringBuilder();
				double firstPrice = HtmlCutter.chartPriceData.get(0);
				double lastPrice = HtmlCutter.chartPriceData
						.get(HtmlCutter.chartPriceData.size() - 1);
				double percent = (lastPrice / firstPrice - 1) * 100;
				if (percent >= 0) {
					titleWidget.getLabelPaint().setColor(
							resources.getColor(R.color.Green2));
				} else {
					titleWidget.getLabelPaint().setColor(
							resources.getColor(R.color.Red2));
				}
				resultText.append(TradeApi.formatDouble(percent, 2));
				resultText.append('%');
				titleWidget.setText(resultText.toString());
				plot.redraw();
			} else {
				Toast.makeText(mContext, R.string.graph_connection_error,
						Toast.LENGTH_SHORT).show();
			}
		}
	}

	private class MyDynamicSeries implements XYSeries {

		public MyDynamicSeries() {

		}

		@Override
		public String getTitle() {
			return "";
		}

		@Override
		public int size() {
			return HtmlCutter.chartPriceData.size();
		}

		@Override
		public Number getX(int index) {
			return index;
		}

		@Override
		public Number getY(int index) {
			return HtmlCutter.chartPriceData.get(index);
		}
	}
}
