package com.tninteractive.socketcontroller.options;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

public class OptionsPresenter implements OptionsContract.Presenter {

    public static final String[] AUTO_REFRESH_RATES = {"5 sec", "10 sec", "30 sec", "1 min", "2 min", "5 min", "10 min"};
    public static final int[] AUTO_REFRESH_RATES_IN_SECONDS = {5, 10, 30, 60, 120, 300, 600};

    public static final String AUTO_REFRESH_KEY = "AutoRefresh";
    public static final String AUTO_REFRESH_RATE_ID_KEY = "AutoRefreshRateId";

    public static final boolean AUTO_REFRESH_DEFAULT_VALUE = true;
    public static final int REFRESH_RATE_ID_DEFAULT_VALUE = 2;


    private OptionsContract.View mView;

    private SharedPreferences sharedPrefs;

    public OptionsPresenter(@NonNull OptionsContract.View view) {
        mView = view;

        mView.setPresenter(this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mView.getViewContext());
    }

    @Override
    public void start() {
        loadSettings();
    }

    @Override
    public void stop() {

    }

    @Override
    public void setAutoRefresh(boolean autoRefresh) {
        sharedPrefs.edit().putBoolean(AUTO_REFRESH_KEY, autoRefresh).apply();

        mView.showAutoRefresh(autoRefresh);
    }

    @Override
    public void setAutoRefreshRate(int refreshRateId) {
        sharedPrefs.edit().putInt(AUTO_REFRESH_RATE_ID_KEY, refreshRateId)
                .apply();
    }

    private void loadSettings(){
        boolean autoRefresh = sharedPrefs.getBoolean(AUTO_REFRESH_KEY, AUTO_REFRESH_DEFAULT_VALUE);
        int autoRefreshRate = sharedPrefs.getInt(AUTO_REFRESH_RATE_ID_KEY, REFRESH_RATE_ID_DEFAULT_VALUE);

        mView.showRefreshOptions(autoRefresh, autoRefreshRate);
    }
}
