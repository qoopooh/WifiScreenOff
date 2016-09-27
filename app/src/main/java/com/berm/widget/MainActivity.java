package com.berm.widget;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RemoteViews;
import android.widget.Spinner;
import android.widget.TextView;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.berm.widget.Const.DOZE_MINUTES;
import static com.berm.widget.Const.DOZE_SELECTED_INDEX;
import static com.berm.widget.Const.SHARED_PREFER_TAG;
import static com.berm.widget.Const.WIDGET_ENABLED;

public class MainActivity extends Activity {
    private static final String TAG = "MainActivity";

    @BindView(R.id.checkBox) CheckBox enableCheckBox;
    @BindView(R.id.spinner) Spinner dozeSpinner;
    @BindView(R.id.version) TextView version;
    private SharedPreferences mSharedPref;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mSharedPref = getSharedPreferences(SHARED_PREFER_TAG, Context.MODE_PRIVATE);
        enableCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                log("onCheckedChanged " + b);
                dozeSpinner.setEnabled(b);

                saveEnabledState(b);
                WifiScreenOffWidget.setEnabled(b);
                updateAllWidgets(MainActivity.this, R.layout.main, WifiScreenOffWidget.class, b);
            }
        });

        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.doze_array, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        dozeSpinner.setAdapter(adapter);
        dozeSpinner.setSelection(getDozeSelection());
        dozeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                Resources res = getResources();
                int[] values = res.getIntArray(R.array.doze_values);
                log("onItemSelected " + values[i]);
                setDozeInterval(i, values[i]);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
            }
        });

        String v = getString(R.string.app_version, BuildConfig.VERSION_NAME);
        if (BuildConfig.DEBUG)
            v += " ( " + BuildConfig.VERSION_CODE + " )";
        version.setText(v);
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
        return mSharedPref.getBoolean(WIDGET_ENABLED, false);
    }

    /**
     * Save widget enable state on SharedPreferences
     * @param enabled widget enable state
     */
    private void saveEnabledState(boolean enabled) {
        mSharedPref.edit()
                .putBoolean(WIDGET_ENABLED, enabled)
                .apply();
    }

    /**
     * Set doze interval value
     * @param index selected
     * @param value in minute
     */
    private void setDozeInterval(int index, int value) {
        mSharedPref.edit()
                .putInt(DOZE_SELECTED_INDEX, index)
                .putInt(DOZE_MINUTES, value)
                .apply();
    }

    /**
     * Get last doze selection
     * @return position
     */
    private int getDozeSelection() {
        return mSharedPref.getInt(DOZE_SELECTED_INDEX, 0);
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
