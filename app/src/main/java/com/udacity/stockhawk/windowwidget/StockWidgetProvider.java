package com.udacity.stockhawk.windowwidget;

import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.RemoteViews;

import com.udacity.stockhawk.R;
import com.udacity.stockhawk.sync.QuoteSyncJob;
import com.udacity.stockhawk.ui.MainActivity;
import com.udacity.stockhawk.ui.StockDetailActivity;

/**
 * Created by huxiaotian on 2017/4/12.
 */

public class StockWidgetProvider extends AppWidgetProvider {
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_stock_list);

            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context,0,intent,0);
            remoteViews.setOnClickPendingIntent(R.id.widget,pendingIntent);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH){
                remoteViews.setRemoteAdapter(R.id.widget_list,new Intent(context,StockWidgetServices.class));
            }else {
                remoteViews.setRemoteAdapter(0,R.id.widget_list,new Intent(context,StockWidgetServices.class));
            }

            Intent clickIntent = new Intent(context, StockDetailActivity.class);
            PendingIntent clickPendinIntent = TaskStackBuilder.create(context).addNextIntentWithParentStack(clickIntent)
                                                .getPendingIntent(0,PendingIntent.FLAG_UPDATE_CURRENT);

            remoteViews.setPendingIntentTemplate(R.id.widget_list,clickPendinIntent);
            remoteViews.setEmptyView(R.id.widget_list,R.id.widget_empty);
            remoteViews.setInt(R.id.widget_list,"setBackgroundResource",R.color.material_gray);
            remoteViews.setInt(R.id.widget_content,"setBackgroundResource",R.color.material_gray);
            remoteViews.setContentDescription(R.id.widget_list,context.getResources().getString(R.string.widget_cd));
            appWidgetManager.updateAppWidget(appWidgetId,remoteViews);
        }

        super.onUpdate(context,appWidgetManager,appWidgetIds);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        if(QuoteSyncJob.ACTION_DATA_UPDATED.equals(intent.getAction())){
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(new ComponentName(context, getClass()));
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds,R.id.widget_list);
        }
    }
}
