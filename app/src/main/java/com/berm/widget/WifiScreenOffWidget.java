package com.berm.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.widget.Toast;

/**
 * A widget for controlling wifi
 */
public class WifiScreenOffWidget extends AppWidgetProvider {
    private static final String TAG = "WifiScreenOffWidget";
    private static final String ENABLE_STATE = "EN";
    private static final String DISABLE_COUNT = "DisCount";
    private static final int NO_WARN_DISABLE_COUNT = 2;

    private static boolean mEnabled;
    private static boolean mWifiEnabled;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        saveWifiState(context);
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
                log("onReceive extras:" + extras.getString(AppWidgetManager.EXTRA_APPWIDGET_ID));
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
        } else {
            if (!isEnabled())
                return;
            WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

            if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
                saveWifiState(context);
                wifiManager.setWifiEnabled(false);
                log("Disable wifi");
            } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && isWifiOn()) {
                wifiManager.setWifiEnabled(true);
                log("Enable wifi");
            }
        }
    }

    /**
     * Check whether widget is enabled
     * @return enable state
     */
    public static boolean isEnabled() {
        return mEnabled;
    }

    /**
     * Check wifi state
     * @param context context
     */
    public static void saveWifiState(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mWifiEnabled = (wifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED);
    }

    /**
     * Check wifi state
     * @return true if it's ON
     */
    public static boolean isWifiOn() {
        return mWifiEnabled;
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
        context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit()
                .putBoolean(ENABLE_STATE, enabled)
                .commit();
    }

    /**
     * Get widget enable state on SharedPreferences
     * @param context context
     * @return widget enable state
     */
    private boolean getEnableState(Context context) {
        return context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
                .getBoolean(ENABLE_STATE, false);
    }

    /**
     * Set counter for disable warning
     * @param context ctx
     * @param count counter
     */
    private void setDisableCount(Context context, int count) {
        context.getSharedPreferences(TAG, Context.MODE_PRIVATE).edit()
                .putInt(DISABLE_COUNT, count)
                .commit();
    }

    /**
     * Get disable warning counter
     * @param context ctx
     * @return count
     */
    private int getDisableCount(Context context) {
        return context.getSharedPreferences(TAG, Context.MODE_PRIVATE)
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
