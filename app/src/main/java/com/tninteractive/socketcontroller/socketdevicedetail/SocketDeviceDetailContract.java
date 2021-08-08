package com.tninteractive.socketcontroller.socketdevicedetail;

import android.content.Context;

import com.tninteractive.socketcontroller.BasePresenter;
import com.tninteractive.socketcontroller.BaseView;
import com.tninteractive.socketcontroller.data.SocketDevice;

/**
 * Created by trist on 12/27/19.
 */

public interface SocketDeviceDetailContract {

    interface View extends BaseView<Presenter>{
        void showDeviceInfo(String deviceName, String deviceIp);

        void showDeviceStatus(SocketDevice device);

        void showTimer(boolean timerLoaded, boolean timerEnabled, boolean timerSet,
                       boolean power, String timerTime);

        void showInvalidTimerMessage(String message);

        void showTimerNotSetMessage();

        void showErrorSettingTimerMessage();

        Context getViewContext();

    }

    interface Presenter extends BasePresenter{

        void refresh();

        void deleteDevice();

        void setPowerForDevice(boolean power);

        void enableOneTimeTimer(boolean enable);

        void setOneTimeTimer(boolean power, String timeInput);
    }
}
