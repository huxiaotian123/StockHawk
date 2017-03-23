package com.udacity.stockhawk.ui;

import android.content.ContentResolver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.data.Contract;

import butterknife.BindView;
import butterknife.ButterKnife;
import timber.log.Timber;

import static com.udacity.stockhawk.data.Contract.PATH_QUOTE_WITH_SYMBOL;


/**
 * Created by huxiaotian on 2017/3/21.
 */

public class StockDetailActivity extends AppCompatActivity {
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.error)
    TextView error;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock_detail);
        ButterKnife.bind(this);

        String symbol = getIntent().getStringExtra("symbol");
        Timber.e("1212","21313");

        if(TextUtils.isEmpty(symbol)){
            error.setText(getString(R.string.error_no_get_stock));
            return;
        }


        ContentResolver contentResolver = getContentResolver();

        Cursor cursor = contentResolver.query(Contract.Quote.URI,
                Contract.Quote.QUOTE_COLUMNS.toArray(new String[]{}),
                "symbol='" + symbol+"'",
                null, null);
        cursor.moveToFirst();
        int columnCount = cursor.getColumnCount();

        Timber.e(columnCount+"");

        int nameColumnIndex = cursor.getColumnIndex(Contract.Quote.COLUMN_HISTORY);
        String name = cursor.getString(nameColumnIndex);
        Timber.e(name+"-----");

//        float percentageChange = cursor.getFloat(Contract.Quote.POSITION_PERCENTAGE_CHANGE);

//        Timber.e(percentageChange+"");
        //query.toString();


    }



}
