package com.udacity.stockhawk.utils;

import android.content.Context;

import com.udacity.stockhawk.StockHawkApp;

/**
 * Created by huxiaotian on 2017/4/12.
 */

public class UiUtil {

    private static  Context ctx  = StockHawkApp.app;

    public static String getString(int resId){
        return ctx.getResources().getString(resId);
    }
}
