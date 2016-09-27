package com.berm.widget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.os.SystemClock;
import android.util.Log;

import static com.berm.widget.Const.DOZE_MINUTES;
import static com.berm.widget.Const.SHARED_PREFER_TAG;
import static com.berm.widget.ScreenReceiver.alarmIntent;

/**
 * Receive alarm
 * Created by berm on 9/27/16.
 */
public class AlarmReceiver extends BroadcastReceiver {
    public static final String ACTION_WIFI_ON = "com.berm.widget.ACTION_WIFI_ON";
    public static final String ACTION_WIFI_OFF = "com.berm.widget.ACTION_WIFI_OFF";
    private static final String TAG = "AlarmReceiver";
    private SharedPreferences mSharedPref;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            log("No action");
            return;
        }
        log("onReceive: " + action);
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        mSharedPref = context.getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE);
        if (action.equals(ACTION_WIFI_ON)) {
            wifiManager.setWifiEnabled(true);
            setAlarmManager(context, true);
            log("setWifiEnabled(true)");
        } else if (action.equals(ACTION_WIFI_OFF)) {
            wifiManager.setWifiEnabled(false);
            setAlarmManager(context, false);
            log("setWifiEnabled(false)");
        }
    }

    /**
     * Clear doze counter
     * @param context context
     * @param on wifi
     */
    private void setAlarmManager(Context context, boolean on) {
        int dozeMinutes = mSharedPref.getInt(DOZE_MINUTES, -1);
        if (dozeMinutes < 0) {
            log("No doze minutes");
            return;
        }

        AlarmManager alarmMgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.setAction(on ? AlarmReceiver.ACTION_WIFI_OFF : AlarmReceiver.ACTION_WIFI_ON);
        alarmIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
        alarmMgr.set(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() +
                        (on ? 20 : dozeMinutes * 14) * 1000, alarmIntent);
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
