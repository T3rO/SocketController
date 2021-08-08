package com.tninteractive.socketcontroller.main;

import android.content.Context;
import android.content.SharedPreferences;

import com.tninteractive.socketcontroller.BasePresenter;
import com.tninteractive.socketcontroller.BaseView;
import com.tninteractive.socketcontroller.data.SocketDevice;

import java.util.List;

/**
 * Created by trist on 12/26/19.
 */

public interface MainContract {

    interface View extends BaseView<Presenter> {

        void showDevices(List<SocketDevice> devices);

        void showDeviceDetail(int deviceIndex);

        void refreshDeviceList();

        Context getViewContext();

    }

    interface Presenter extends BasePresenter {

        void refreshDeviceList();

        void openDeviceDetail(SocketDevice device);

        void setPowerForDevice(SocketDevice device, boolean power);
    }
}
