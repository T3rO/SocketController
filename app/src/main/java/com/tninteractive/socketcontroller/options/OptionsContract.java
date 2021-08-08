package com.tninteractive.socketcontroller.options;

import android.content.Context;
import android.content.SharedPreferences;

import com.tninteractive.socketcontroller.BasePresenter;
import com.tninteractive.socketcontroller.BaseView;

public interface OptionsContract {

    interface View extends BaseView<Presenter>{

        Context getViewContext();

        void showRefreshOptions(boolean autoRefresh, int refreshRateId);

        void showAutoRefresh(boolean autoRefresh);

        void showAutoRefreshRate(int refreshRateId);

    }

    interface Presenter extends BasePresenter{

        void setAutoRefresh(boolean autoRefresh);

        void setAutoRefreshRate(int refreshRateId);

    }

}
