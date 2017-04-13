package com.udacity.stockhawk.windowwidget;

import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.os.StrictMode;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class StockWidgetServices extends RemoteViewsService {
    public StockWidgetServices() {
    }


    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewFactory();
    }

    public class ListRemoteViewFactory implements RemoteViewsFactory{
        private Cursor data = null;

        @Override
        public void onCreate() {

        }

        @Override
        public void onDestroy() {
            if(data != null){
                data.close();
                data = null;
            }

        }


        @Override
        public void onDataSetChanged() {
            if(data != null){
                data.close();
            }

            long identity = Binder.clearCallingIdentity();
            data = getContentResolver().query(Contract.Quote.URI,
                    Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                    null,
                    null,
                    Contract.Quote.COLUMN_SYMBOL);

            Binder.restoreCallingIdentity(identity);

        }

        @Override
        public int getCount() {
            return data == null ?0:data.getCount();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if(position == AdapterView.INVALID_POSITION ||data ==null
                    ||!data.moveToPosition(position)){
                return  null;
            }

            RemoteViews remoteViews = new RemoteViews(getPackageName(),
                    R.layout.list_item_quote);

            String stockSymbol = data.getString(Contract.Quote.POSITION_SYMBOL);
            Float stockPrice = data.getFloat(Contract.Quote.POSITION_PRICE);
            Float absoluteChange = data.getFloat(Contract.Quote.POSITION_ABSOLUTE_CHANGE);
            int backgroundDrawable;

            DecimalFormat dollarFormat = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            DecimalFormat dollarFormatWithPlus = (DecimalFormat) NumberFormat.getCurrencyInstance(Locale.US);
            dollarFormatWithPlus.setPositivePrefix("+");
            dollarFormatWithPlus.setMaximumFractionDigits(2);
            dollarFormat.setMaximumFractionDigits(2);
            dollarFormat.setMinimumFractionDigits(2);
            dollarFormatWithPlus.setMinimumFractionDigits(2);

            if (absoluteChange > 0) {
                backgroundDrawable = R.drawable.percent_change_pill_red;
            } else {
                backgroundDrawable = R.drawable.percent_change_pill_green;
            }

            remoteViews.setTextViewText(R.id.symbol, stockSymbol);
            remoteViews.setTextViewText(R.id.price, dollarFormat.format(stockPrice));
            remoteViews.setTextViewText(R.id.change, dollarFormatWithPlus.format(absoluteChange));
            remoteViews.setInt(R.id.change, "setBackgroundResource", backgroundDrawable);
            remoteViews.setInt(R.id.list_item_quote, "setBackgroundResource", R.color.material_grey_850);

            final Intent fillInIntent = new Intent();
            Uri stockUri = Contract.Quote.makeUriForStock(stockSymbol);
            fillInIntent.setData(stockUri);
            remoteViews.setOnClickFillInIntent(R.id.list_item_quote, fillInIntent);
            return remoteViews;
//            return null;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return data.moveToPosition(position)?data.getLong(Contract.Quote.POSITION_ID):position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}
