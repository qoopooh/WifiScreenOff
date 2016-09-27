package com.berm.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RemoteViews;
import android.widget.Spinner;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.berm.widget.Const.SHARED_PREFER_TAG;
import static com.berm.widget.Const.WIDGET_ENABLED;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.checkBox) CheckBox enableCheckBox;
    @BindView(R.id.spinner) Spinner dozeSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        enableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                log("onCheckedChanged " + b);
                saveEnabledState(b);
                WifiScreenOffWidget.setEnabled(b);
                updateAllWidgets(MainActivity.this, R.layout.main, WifiScreenOffWidget.class, b);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        log("onResume: " + isWidgetEnabled() + " / " + enableCheckBox.isChecked());
        if (isWidgetEnabled() != enableCheckBox.isChecked())
            enableCheckBox.setChecked(isWidgetEnabled());
    }

    /**
     * Update all widgets
     * @param context context
     * @param layoutResourceId layout
     * @param appWidgetClass widget
     * @param enable enable
     */
    public static void updateAllWidgets(final Context context,
                                        final int layoutResourceId,
                                        final Class<? extends AppWidgetProvider> appWidgetClass,
                                        boolean enable) {

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.main);
        remoteViews.setImageViewResource(R.id.image, enable ?
                R.drawable.sync_enabled : R.drawable.sync);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        final int[] appWidgetIds = manager.getAppWidgetIds(new ComponentName(context, appWidgetClass));

        for (int id : appWidgetIds) {
            log("updateAllWidgets " + id + ": " + remoteViews);
            manager.updateAppWidget(id, remoteViews);
        }
    }


    /**
     * Check widget state
     * @return true if it's ON
     */
    private boolean isWidgetEnabled() {
        return getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE)
                .getBoolean(WIDGET_ENABLED, false);
    }

    /**
     * Save widget enable state on SharedPreferences
     * @param enabled widget enable state
     */
    private void saveEnabledState(boolean enabled) {
        getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE).edit()
                .putBoolean(WIDGET_ENABLED, enabled)
                .apply();
    }

    /**
     * Log debug message
     * @param s information
     */
    private static void log(String s) {
        if (BuildConfig.DEBUG) {
            Log.i(TAG, s);
        }
    }
}
