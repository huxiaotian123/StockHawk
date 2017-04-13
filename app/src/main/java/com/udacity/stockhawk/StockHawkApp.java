package com.udacity.stockhawk;

import android.app.Application;

import timber.log.Timber;

public class StockHawkApp extends Application {

    public static StockHawkApp app ;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        if (BuildConfig.DEBUG) {
            Timber.uprootAll();
            Timber.plant(new Timber.DebugTree());
        }
    }
}
