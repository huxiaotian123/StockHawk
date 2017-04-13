package com.udacity.stockhawk.utils;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Created by huxiaotian on 2017/4/10.
 */

public class DateUtil {

    public static float formatDayFloat(String time){
        Long l = Long.valueOf(time);

        Date date = new Date(Long.valueOf(time));
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String format = simpleDateFormat.format(date);
        Log.e("hxt",format+"~~~~~~~~~~~~~~");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(date);

        return l/(1000*60*24*7);
    }
}
