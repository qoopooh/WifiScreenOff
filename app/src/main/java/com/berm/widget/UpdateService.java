package com.berm.widget;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

import com.berm.widget.BuildConfig;

/**
 * Starter service
 */
public class UpdateService extends Service {
    private static final String TAG = "UpdateService";

    private WifiScreenOffWidget mScreenReceiver = new WifiScreenOffWidget();

    @Override
    public IBinder onBind(Intent intent) {
        log("onBind");
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        log("onCreate");
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        registerReceiver(mScreenReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mScreenReceiver);
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
