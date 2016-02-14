package com.berm.widget;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;

/**
 * Starter service
 */
public class UpdateService extends Service {
    private static final String TAG = "UpdateService";

    private ScreenReceiver mScreenReceiver;

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

        mScreenReceiver = new ScreenReceiver();
        registerReceiver(mScreenReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        log("onDestroy");
        unregisterReceiver(mScreenReceiver);
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
