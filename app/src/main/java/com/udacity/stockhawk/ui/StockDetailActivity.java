package com.udacity.stockhawk.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.BinderThread;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.LimitLine;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.Utils;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.custom.MyMarkerView;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.DateUtil;
import com.udacity.stockhawk.utils.UiUtil;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import rx.Observable;
import rx.functions.Action1;
import timber.log.Timber;
import yahoofinance.histquotes.HistoricalQuote;


/**
 * Created by huxiaotian on 2017/3/21.
 */

public class StockDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private static final int PAGE_LIMIT = 2;
    private static int LOADER_ID = 0;

    public Map<Integer,String> fragmentTags = new HashMap<>();
    public Boolean dataLoaded = false;


    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.toolbar) public Toolbar toolbar;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stock_name) public TextView tvStockName;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stock_exchange) public TextView tvStockExchange;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.stock_price) public TextView tvStockPrice;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.day_highest) public TextView tvDayHighest;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.day_lowest) public TextView tvDayLowest;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.absolute_change) public TextView tvAbsoluteChange;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.viewpager) public ViewPager viewPager;
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.tabs) public TabLayout tabLayout;
    private Uri stockUri;

    private LineData lineData;
    private LineDataSet lineDataSet;
    private List<Entry> entries;
    private DecimalFormat dollarFormat;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        supportFinishAfterTransition();
        Log.e("hxt","12312313");
        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle("");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        stockUri = getIntent().getData();

        initViewPager();

        getSupportLoaderManager().initLoader(LOADER_ID,null,this);

//        String symbol = getIntent().getStringExtra("symbol");
//        ContentResolver contentResolver = getContentResolver();
//
//        Cursor cursor = contentResolver.query(Contract.Quote.URI,
//                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
//                "symbol='" + symbol + "'",
//                null, null);
//        cursor.moveToFirst();
//        int nameColumnIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
//        String name = cursor.getString(nameColumnIndex);
//
//        cursor.close();
//
//        Timber.e(name);
//        String[] split = name.split("\n");
//        List<String> strings = Arrays.asList(split);
//        Collections.reverse(strings);
//        entries = new ArrayList<>();
//
//        Observable.from(strings)
//                .subscribe(new Action1<String>() {
//                    @Override
//                    public void call(String s) {
//                        String[] split1 = s.split(", ");
//                        float x = DateUtil.formatDayFloat(split1[0]);
//                        Float y = Float.valueOf(split1[1]);
//                        Log.e("hxt", x + "----" + y);
//
//                        entries.add(new Entry(x
//                                ,y, getResources().getDrawable(R.drawable.star)));
//
//
//                    }
//                });


//        lineDataSet = new LineDataSet(entries, "Label");
//        lineDataSet.setColor(Color.RED);
//
//        lineDataSet.setDrawValues(false);
//
//        lineData = new LineData(lineDataSet);


        // mChart.setData(data);

//        setData(40, 150);
//
//        mChart.animateX(2500);
//        //mChart.invalidate();
//
//        // get the legend (only possible after setting data)
//        Legend l = mChart.getLegend();
//
//        // modify the legend ...
//        l.setForm(Legend.LegendForm.LINE);


    }

    private void initViewPager() {
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
        Bundle bundle = new Bundle();
        StockDetailFragment monththFragment = new StockDetailFragment();
        bundle.putString(UiUtil.getString(R.string.frgament_date_type_key),UiUtil.getString(R.string.month));
        monththFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(monththFragment,UiUtil.getString(R.string.months_fragment_title));

        bundle = new Bundle();
        StockDetailFragment weekFragment = new StockDetailFragment();
        bundle.putString(UiUtil.getString(R.string.frgament_date_type_key),UiUtil.getString(R.string.week));
        weekFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(weekFragment,UiUtil.getString(R.string.weeks_fragment_title));

        bundle = new Bundle();
        StockDetailFragment dayFragment = new StockDetailFragment();
        bundle.putString(UiUtil.getString(R.string.frgament_date_type_key),UiUtil.getString(R.string.day));
        dayFragment.setArguments(bundle);
        viewPagerAdapter.addFragment(dayFragment,UiUtil.getString(R.string.days_fragment_title));

        viewPager.setAdapter(viewPagerAdapter);
        viewPager.setOffscreenPageLimit(PAGE_LIMIT);

        tabLayout.setupWithViewPager(viewPager);

    }



 //not use
//    private void setData(int count, float range) {
//
//        ArrayList<Entry> values = new ArrayList<Entry>();
//
////        for (int i = 0; i < count; i++) {
////
////            float val = (float) (Math.random() * range) + 3;
////            values.add(new Entry(i, val, getResources().getDrawable(R.drawable.star)));
////        }
//
//
//
//
//        // create a dataset and give it a type
//        LineDataSet  set1 = new LineDataSet(entries
//                , "DataSet 1");
//
//        //set1.setd(false);
//
//        // set the line to be drawn like this "- - - - - -"
//        set1.enableDashedLine(10f, 5f, 0f);
//        set1.enableDashedHighlightLine(10f, 5f, 0f);
//        set1.setColor(Color.BLACK);
//        set1.setCircleColor(Color.BLACK);
//        set1.setLineWidth(1f);
//        set1.setCircleRadius(3f);
//        set1.setDrawCircleHole(false);
//        set1.setValueTextSize(9f);
//        set1.setDrawFilled(true);
//        set1.setFormLineWidth(1f);
//        set1.setFormLineDashEffect(new DashPathEffect(new float[]{10f, 5f}, 0f));
//        set1.setFormSize(15.f);
//
//        if (Utils.getSDKInt() >= 18) {
//            // fill drawable only supported on api level 18 and above
//            Drawable drawable = ContextCompat.getDrawable(this, R.drawable.fade_red);
//            set1.setFillDrawable(drawable);
//        } else {
//            set1.setFillColor(Color.BLACK);
//        }
//
//        ArrayList<ILineDataSet> dataSets = new ArrayList<ILineDataSet>();
//        dataSets.add(set1); // add the datasets
//
//        // create a data object with the datasets
//        LineData data = new LineData(dataSets);
//
//        // set data
//        mChart.setData(data);
//
//    }










    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(stockUri != null){
            return new CursorLoader(
                    this,
                    stockUri,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if(data.moveToFirst()){
            String stockExchage = data.getString(Contract.Quote.POSITION_EXCHANGE);
            String stockName = data.getString(Contract.Quote.POSITION_NAME);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            float sotckAbsoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            float stockLowest = data.getFloat(Contract.Quote.POSITION_LOWEST);
            float stockHighest = data.getFloat(Contract.Quote.POSITION_HIGHEST);

            getWindow().getDecorView().setContentDescription(
                    String.format(getString(R.string.detail_activity_cd),stockName));

            NumberFormat dollarFormat = NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);

            tvStockExchange.setText(stockExchage);
            tvStockName.setText(stockName);
            tvStockPrice.setText(dollarFormat.format(stockPrice));
            tvStockPrice.setContentDescription(String.format(getString(R.string.stock_price_cd),tvStockPrice.getText()));
            tvAbsoluteChange.setText(dollarFormat.format(sotckAbsoluteChange));

            if(stockHighest != -1){
                tvDayHighest.setText(dollarFormat.format(stockHighest));
                tvDayHighest.setContentDescription(String.format(getString(R.string.day_highest_cd),tvDayHighest.getText()));
                tvDayLowest.setText(dollarFormat.format(stockLowest));
                tvDayLowest.setContentDescription(String.format(getString(R.string.day_lowest_cd),tvDayLowest.getText()));
            }else{
                tvDayHighest.setVisibility(View.GONE);
                tvDayLowest.setVisibility(View.GONE);
            }

            if(sotckAbsoluteChange >0){ //zhang
                tvAbsoluteChange.setBackgroundResource(R.drawable.percent_change_pill_red);
                tvAbsoluteChange.setContentDescription(String.format(getString(R.string.stock_increment_cd),tvAbsoluteChange.getText()));

            }else {
                tvAbsoluteChange.setBackgroundResource(R.drawable.percent_change_pill_green);
                tvAbsoluteChange.setContentDescription(String.format(getString(R.string.stock_decrement_cd),tvAbsoluteChange.getText()));
            }

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
            dataLoaded = false;
    }


    public class ViewPagerAdapter  extends FragmentPagerAdapter{
        private final List<Fragment> fragmentList = new ArrayList<>();
        private final List<String> fragmentTitleList = new ArrayList<>();

        public ViewPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return fragmentList.get(position);
        }

        @Override
        public int getCount() {
            return fragmentList.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragmentTitleList.get(position);
        }

        public void addFragment(Fragment fragment,String title){
            fragmentList.add(fragment);
            fragmentTitleList.add(title);
        }
    }
}
