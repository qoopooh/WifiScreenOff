package com.berm.widget;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import static com.berm.widget.Const.DOZE_COUNT;
import static com.berm.widget.Const.DOZE_MINUTES;
import static com.berm.widget.Const.SHARED_PREFER_TAG;
import static com.berm.widget.Const.DEVICE_WIFI_ENABLED;

/**
 * Screen on / off receiver
 */
public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";
    private WifiManager mWifiManager;
    private SharedPreferences mSharedPref;

    @Override
    public void onReceive(Context context, Intent intent) {
        boolean enabled = WifiScreenOffWidget.isEnabled();
        log("onReceive " + enabled + ": " + intent.getAction());

        if(Build.VERSION.SDK_INT >= 20) {
            checkBattery(context);
        }

        if (!enabled)
            return;

        mWifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mSharedPref = context.getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            saveWifiState(context);
            setAlarmManager(context);
            mWifiManager.setWifiEnabled(false);
            log("Disable wifi");
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON) && isWifiOn(context)) {
            resetAlarmManager(context);
            mWifiManager.setWifiEnabled(true);
            log("Enable wifi");
        }
    }

    /**
     * Check battery level
     * @param context ctx
     */
    @TargetApi(21)
    private void checkBattery(Context context) {
        BatteryManager batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
        int level = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
        String message = "Battery: " + level;
        log(message);

        if (BuildConfig.DEBUG) {
            Toast.makeText(context, message, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Stop alarm manager
     * @param context ctx
     */
    private void resetAlarmManager(Context context) {

    }

    /**
     * Clear doze counter
     * @param context context
     */
    private void setAlarmManager(Context context) {
        int dozeMinutes = mSharedPref.getInt(DOZE_MINUTES, -1);
        if (dozeMinutes < 0) {
            resetAlarmManager(context);
            return;
        }
        mSharedPref.edit()
                .putInt(DOZE_COUNT, 0)
                .apply();
    }

    /**
     * Check wifi state
     * @param context context
     */
    private void saveWifiState(Context context) {
        boolean on = (mWifiManager.getWifiState() != WifiManager.WIFI_STATE_DISABLED);
        mSharedPref.edit()
                .putBoolean(DEVICE_WIFI_ENABLED, on)
                .apply();
    }

    /**
     * Check wifi state
     * @return true if it's ON
     * @param context context
     */
    private boolean isWifiOn(Context context) {
        return mSharedPref.getBoolean(DEVICE_WIFI_ENABLED, false);
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
