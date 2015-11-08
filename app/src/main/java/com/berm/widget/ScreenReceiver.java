package com.berm.widget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.util.Log;

import com.berm.widget.BuildConfig;

/**
 * Screen state changed listener
 */
public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        log("Widget:" + WifiScreenOffWidget.isEnabled() + " wifi:" + WifiScreenOffWidget.isWifiOn());
        if (!WifiScreenOffWidget.isEnabled())
            return;

        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            WifiScreenOffWidget.saveWifiState(context);
            wifiManager.setWifiEnabled(false);
            log("Disable wifi");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && WifiScreenOffWidget.isWifiOn()) {
            wifiManager.setWifiEnabled(true);
            log("Enable wifi");
        }
    }

    /**
     * Log debug information
     * @param s debug message
     */
    private void log(String s) {
        //if (BuildConfig.DEBUG)
            Log.i(TAG, s);
    }
}
