package com.udacity.stockhawk.ui;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.util.Pair;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.utils.CustomMarkerView;
import com.udacity.stockhawk.utils.Parser;
import com.udacity.stockhawk.utils.UiUtil;
import com.udacity.stockhawk.utils.XAxisDateFormatter;
import com.udacity.stockhawk.utils.YAxisPriceFormatter;


import java.util.List;

import butterknife.BindColor;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by huxiaotian on 2017/4/12.
 */

public class StockDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    public String fragmentDataType;
    public String dataFormat;
    public int dataColumnPosition;
    public Uri stockUri;
    public Boolean isChartDescriptionAnnounced=false;
    public String historyData;
    public int LOADER_ID;
    public String fragmentTitle;

    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.chart)
    public LineChart mChart;
    @BindColor(R.color.white)
    public int white;




    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        stockUri = getActivity().getIntent().getData();

        if(savedInstanceState == null){
            fragmentDataType = getArguments().getString(UiUtil.getString(R.string.frgament_date_type_key));
            if(fragmentDataType.equals(UiUtil.getString(R.string.month))){
                dataColumnPosition = Contract.Quote.POSITION_MONTH_HISTORY;
                dataFormat="MMM";
                LOADER_ID = 100;
            }else if(fragmentDataType.equals(UiUtil.getString(R.string.week))){
                dataColumnPosition = Contract.Quote.POSITION_WEEK_HISTORY;
                dataFormat="dd";
                LOADER_ID = 200;
            }else {
                dataColumnPosition = Contract.Quote.POSITION_DAY_HISTORY;
                dataFormat = "dd";
                LOADER_ID = 300;
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_detail,container,false);
        ButterKnife.bind(this,view);
        if(historyData != null){
            setUpLineChart();
        }
        return view;
    }

//    @Override
//    public void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
//    }

    @Override
    public void onResume() {
        super.onResume();
        getLoaderManager().initLoader(LOADER_ID,null,this);
    }



    private void setUpLineChart() {
        Pair<Float,List<Entry>> result = Parser.getFormattedStockHistory(historyData);
        Float referenceTime = result.first;
        List<Entry> dataPairs = result.second;
        Log.e("hxt",dataPairs.toString());
        LineDataSet dataSet = new LineDataSet(dataPairs,"");
        dataSet.setColor(white);
        dataSet.setLineWidth(2f);
        dataSet.setDrawHighlightIndicators(false);
        dataSet.setCircleColor(white);
        dataSet.setHighLightColor(white);
        dataSet.setDrawValues(false);

        LineData lineData = new LineData(dataSet);
        mChart.setData(lineData);

        XAxis xAxis = mChart.getXAxis();
        xAxis.setValueFormatter(new XAxisDateFormatter(dataFormat,referenceTime));
        xAxis.setDrawGridLines(false);
        xAxis.setAxisLineColor(white);
        xAxis.setAxisLineWidth(1.5f);
        xAxis.setTextColor(white);
        xAxis.setTextSize(12f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);

        YAxis yAxis = mChart.getAxisLeft();
        yAxis.setValueFormatter(new YAxisPriceFormatter());
        yAxis.setDrawGridLines(false);
        yAxis.setAxisLineColor(white);
        yAxis.setAxisLineWidth(1.5f);
        yAxis.setTextColor(white);
        yAxis.setTextSize(12f);

        CustomMarkerView customMarkerView = new CustomMarkerView(getContext(),
                R.layout.marker_view,getLastButOneData(dataPairs),referenceTime);


        Legend legend = mChart.getLegend();
        legend.setEnabled(false);

        mChart.setMarker(customMarkerView);

        mChart.setDragEnabled(false);
        mChart.setScaleEnabled(false);
        mChart.setDragDecelerationEnabled(false);
        mChart.setPinchZoom(false);
        mChart.setDoubleTapToZoomEnabled(false);

        Description desc = new Description();
        desc.setText(" ");

        mChart.setDescription(desc);
        mChart.setExtraOffsets(10,0,0,10);
        mChart.animateX(1500, Easing.EasingOption.Linear);
    }

    private Entry getLastButOneData(List<Entry> dataPairs) {
        if(dataPairs.size() >2){
            return  dataPairs.get(dataPairs.size()-2);
        }else {
            return dataPairs.get(dataPairs.size()-1);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if(stockUri != null){
            return new CursorLoader(
                    getContext(),
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
        if(data.moveToFirst() && historyData == null){
            historyData = data.getString(dataColumnPosition);
            setUpLineChart();
//            getActivity().supportStartPostponedEnterTransition();
        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
