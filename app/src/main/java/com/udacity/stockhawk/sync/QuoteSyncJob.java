package com.udacity.stockhawk.sync;

import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import com.udacity.stockhawk.data.Contract;
import com.udacity.stockhawk.data.PrefUtils;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import timber.log.Timber;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;
import yahoofinance.histquotes.HistoricalQuote;
import yahoofinance.histquotes.Interval;
import yahoofinance.quotes.stock.StockQuote;

public final class QuoteSyncJob {

    private static final int ONE_OFF_ID = 2;
    public static final String ACTION_DATA_UPDATED = "com.udacity.stockhawk.ACTION_DATA_UPDATED";
    private static final int PERIOD = 300000;
    private static final int INITIAL_BACKOFF = 10000;
    private static final int PERIODIC_ID = 1;
    private static final int YEARS_OF_HISTORY = 2;

    private QuoteSyncJob() {
    }

    //查询股票列表
    static void getQuotes(Context context) {

        Timber.d("Running sync job");

        Calendar from = Calendar.getInstance();
        Calendar to = Calendar.getInstance();
       //
        // from.add(Calendar.YEAR, -YEARS_OF_HISTORY);

        try {

            Set<String> stockPref = PrefUtils.getStocks(context);
            Set<String> stockCopy = new HashSet<>();
            stockCopy.addAll(stockPref);
            String[] stockArray = stockPref.toArray(new String[stockPref.size()]);

            Timber.d(stockCopy.toString());

            if (stockArray.length == 0) {
                return;
            }

            Map<String, Stock> quotes = YahooFinance.get(stockArray);
            Iterator<String> iterator = stockCopy.iterator();

            Timber.d(quotes.toString());

            ArrayList<ContentValues> quoteCVs = new ArrayList<>();

            while (iterator.hasNext()) {
                String symbol = iterator.next();
                Stock stock = quotes.get(symbol);


                StockQuote quote ;
                float change;
                float price;
                float dayLowest;
                float dayHighest;
                float percentChange;
                String stockName;
                String exchangeName;

                try {
                    quote = stock.getQuote();
                    price = quote.getPrice().floatValue();

                    BigDecimal temp = quote.getDayLow();
                    //This is done because lowest or highest of the day is unknown
                    //and quote.getDayLow() returns null.
                    if (temp == null) {
                        dayLowest = -1;
                        dayHighest = -1;
                    } else {
                        dayLowest = temp.floatValue();
                        dayHighest = quote.getDayHigh().floatValue();
                    }

                    change = quote.getChange().floatValue();
                    percentChange = quote.getChangeInPercent().floatValue();
                    stockName = stock.getName();
                    exchangeName = stock.getStockExchange();
                } catch (NullPointerException exception) {
                    Timber.e(exception, "Incorrect stock symbol entered : " + symbol);
                    PrefUtils.removeStock(context, symbol);
//                    if (PrefUtils.getStocks(context).size() == 0) {
//                        setStockStatus(context, STOCK_STATUS_EMPTY);
//                    } else {
//                        setStockStatus(context, STOCK_STATUS_INVALID);
//                    }
//                    invalidFlag = true;
                    continue;
                }


                from = Calendar.getInstance();
                from.add(Calendar.MONTH, -9);
                String monthHistory = getHistory(stock, from, to, Interval.MONTHLY);


                from = Calendar.getInstance();
                from.add(Calendar.WEEK_OF_YEAR, -9);
                String weekHistory = getHistory(stock, from, to, Interval.WEEKLY);

                from = Calendar.getInstance();
                from.add(Calendar.DAY_OF_YEAR, -9);
                String dayHistory = getHistory(stock, from, to, Interval.DAILY);

//                Log.e("hxt-month",monthHistory);
//
//                Log.e("hxt-week",weekHistory);
//
//                Log.e("hxt-day",dayHistory);
                ContentValues quoteCV = new ContentValues();
                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);
                quoteCV.put(Contract.Quote.COLUMN_MONTH_HISTORY, monthHistory);
                quoteCV.put(Contract.Quote.COLUMN_DAY_HISTORY, dayHistory);
                quoteCV.put(Contract.Quote.COLUMN_WEEK_HISTORY, weekHistory);
                quoteCV.put(Contract.Quote.COLUMN_DAY_HIGHEST, dayHighest);
                quoteCV.put(Contract.Quote.COLUMN_DAY_LOWEST, dayLowest);
                quoteCV.put(Contract.Quote.COLUMN_STOCK_NAME, stockName);
                quoteCV.put(Contract.Quote.COLUMN_STOCK_EXCHANGE, exchangeName);
//
//                // WARNING! Don't request historical data for a stock that doesn't exist!
//                // The request will hang forever X_x
//                List<HistoricalQuote> history = stock.getHistory(from, to, Interval.WEEKLY);
//
//                StringBuilder historyBuilder = new StringBuilder();
//
//                for (HistoricalQuote it : history) {
//                    historyBuilder.append(it.getDate().getTimeInMillis());
//                    historyBuilder.append(", ");
//                    historyBuilder.append(it.getClose());
//                    historyBuilder.append("\n");
//                }
//
//                ContentValues quoteCV = new ContentValues();
//                quoteCV.put(Contract.Quote.COLUMN_SYMBOL, symbol);
//                quoteCV.put(Contract.Quote.COLUMN_PRICE, price);
//                quoteCV.put(Contract.Quote.COLUMN_PERCENTAGE_CHANGE, percentChange);
//                quoteCV.put(Contract.Quote.COLUMN_ABSOLUTE_CHANGE, change);


//                quoteCV.put(Contract.Quote.COLUMN_HISTORY, historyBuilder.toString());

                quoteCVs.add(quoteCV);

            }

            context.getContentResolver()
                    .bulkInsert(
                            Contract.Quote.URI,
                            quoteCVs.toArray(new ContentValues[quoteCVs.size()]));

            Intent dataUpdatedIntent = new Intent(ACTION_DATA_UPDATED);
            context.sendBroadcast(dataUpdatedIntent);

        } catch (Exception exception) {
            Timber.e(exception, "Error fetching stock quotes");
        }
    }

    private static String getHistory(Stock stock, Calendar from, Calendar to, Interval interval) throws IOException {

        List<HistoricalQuote> history = new ArrayList<>();

        //At times, query over 5-7 days history returns very less data at times.
        //hence performing iterative queries until 5 days of data is received.

        if (interval.equals(Interval.DAILY)) {
            while (history.size() < 5) {
                history = stock.getHistory(from, to, interval);
                from.add(Calendar.DAY_OF_YEAR, -1);
            }
        } else {
            history = stock.getHistory(from, to, interval);
        }

        StringBuilder historyBuilder = new StringBuilder();
        for (HistoricalQuote it : history) {
            historyBuilder.append(it.getDate().getTimeInMillis());
            historyBuilder.append(":");
            historyBuilder.append(it.getClose());
            historyBuilder.append("$");
        }
        return historyBuilder.toString();
    }


    //开启一个调度器
    private static void schedulePeriodic(Context context) {
        Timber.d("Scheduling a periodic task");


        JobInfo.Builder builder = new JobInfo.Builder(PERIODIC_ID, new ComponentName(context, QuoteJobService.class));


        builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                .setPeriodic(PERIOD)
                .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


        JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

        scheduler.schedule(builder.build());
    }


    public static synchronized void initialize(final Context context) {

        schedulePeriodic(context);
        syncImmediately(context);

    }


    //及时同步
    public static synchronized void syncImmediately(Context context) {

        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnectedOrConnecting()) {
            Intent nowIntent = new Intent(context, QuoteIntentService.class);
            context.startService(nowIntent);
        } else {
            //开启一个延迟任务，等有网了之后执行
            JobInfo.Builder builder = new JobInfo.Builder(ONE_OFF_ID, new ComponentName(context, QuoteJobService.class));


            builder.setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setBackoffCriteria(INITIAL_BACKOFF, JobInfo.BACKOFF_POLICY_EXPONENTIAL);


            JobScheduler scheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);

            scheduler.schedule(builder.build());


        }
    }




}
