package com.berm.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

/**
 * Screen on / off receiver
 */
public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";
    private static final String WIFI_ENABLED = "wifiEnabled";

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean enabled = WifiScreenOffWidget.isEnabled();
        log("onReceive " + enabled + ": " + intent.getAction());

        if (!enabled)
            return;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            saveWifiState(context);
            wifiManager.setWifiEnabled(false);
            log("Disable wifi");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && isWifiOn(context)) {
            wifiManager.setWifiEnabled(true);
            log("Enable wifi");
        }
    }

    /**
     * Check wifi state
     * @param context context
     */
    private void saveWifiState(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        boolean on = (wifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED);
        context.getSharedPreferences(WifiScreenOffWidget.TAG, Context.MODE_PRIVATE)
                .edit()
                .putBoolean(WIFI_ENABLED, on)
                .apply();
    }

    /**
     * Check wifi state
     * @return true if it's ON
     * @param context context
     */
    private boolean isWifiOn(Context context) {
        return context.getSharedPreferences(WifiScreenOffWidget.TAG, Context.MODE_PRIVATE)
                .getBoolean(WIFI_ENABLED, false);
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
