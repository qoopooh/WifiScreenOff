package com.berm.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

import static com.berm.widget.Const.DISABLE_COUNT;
import static com.berm.widget.Const.NO_WARN_DISABLE_COUNT;
import static com.berm.widget.Const.SHARED_PREFER_TAG;
import static com.berm.widget.Const.WIDGET_ENABLED;

/**
 * A widget for controlling wifi
 */
public class WifiScreenOffWidget extends AppWidgetProvider {
    public static final String TAG = "WifiScreenOffWidget";

    private static boolean mEnabled;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        mEnabled = getEnableState(context);
        for ( int appWidgetId : appWidgetIds ) {
            updateSwitch(context, appWidgetManager, appWidgetId, mEnabled);
        }

        Intent i = new Intent(context, UpdateService.class);
        context.startService(i);

        log("onUpdate " + mEnabled);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        log("onReceive " + mEnabled + ": " + intent.getAction());

        if (intent.getAction()==null) {
            Bundle extras = intent.getExtras();
            if (extras != null) {
                log("onReceive extras:" + extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID));
                mEnabled = !mEnabled;
                saveEnabledState(context, mEnabled);
                showToast(context, mEnabled);

                RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
                remoteViews.setImageViewResource(R.id.image, mEnabled ?
                        R.drawable.sync_enabled : R.drawable.sync);
                ComponentName watchWidget = new ComponentName(context, WifiScreenOffWidget.class);
                AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
                appWidgetManager.updateAppWidget(watchWidget, remoteViews);
            }
        }
    }

    /**
     * Check widget enable
     * @return enabled
     */
    public static boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Set enable
     * @param enabled enabled
     */
    public static void setEnabled(boolean enabled) {
        mEnabled = enabled;
    }

    /**
     * Update switch view
     * @param context context
     * @param appWidgetManager widget manager
     * @param appWidgetId widget id
     * @param enabled widget enable status
     */
    private void updateSwitch(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
                              boolean enabled) {

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.main);
        if (enabled){
            views.setImageViewResource(R.id.image, R.drawable.sync_enabled);
        }

        Intent intent = new Intent(context, WifiScreenOffWidget.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        views.setOnClickPendingIntent(R.id.widget, pendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }

    /**
     * Show toast message on user clicked
     * @param context ctx
     * @param b enabled
     */
    private void showToast(Context context, boolean b) {
        if (b) {
            Toast.makeText(context, R.string.warning, Toast.LENGTH_LONG)
                    .show();
        } else {
            int count = getDisableCount(context);
            if (count < NO_WARN_DISABLE_COUNT) {
                setDisableCount(context, ++count);
                Toast.makeText(context, R.string.disable, Toast.LENGTH_SHORT)
                        .show();
            } else if (BuildConfig.DEBUG) {
                try {
                    PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
                    String message = "Disable " + pInfo.versionName + " (" + pInfo.versionCode + ")";
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Save widget enable state on SharedPreferences
     * @param context context
     * @param enabled widget enable state
     */
    private void saveEnabledState(Context context, boolean enabled) {
        context.getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE).edit()
                .putBoolean(WIDGET_ENABLED, enabled)
                .apply();
    }

    /**
     * Get widget enable state on SharedPreferences
     * @param context context
     * @return widget enable state
     */
    private boolean getEnableState(Context context) {
        return context.getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE)
                .getBoolean(WIDGET_ENABLED, true);
    }

    /**
     * Set counter for disable warning
     * @param context ctx
     * @param count counter
     */
    private void setDisableCount(Context context, int count) {
        context.getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE).edit()
                .putInt(DISABLE_COUNT, count)
                .apply();
    }

    /**
     * Get disable warning counter
     * @param context ctx
     * @return count
     */
    private int getDisableCount(Context context) {
        return context.getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE)
                .getInt(DISABLE_COUNT, 0);
    }

    /**
     * Log debug information
     * @param s debug message
     */
    private void log(String s) {
        if (BuildConfig.DEBUG)
            Log.i(TAG, s);
    }
}
