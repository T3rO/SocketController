package com.tninteractive.socketcontroller.data;

import android.content.Context;

import org.json.JSONObject;

import java.util.Date;

import androidx.annotation.NonNull;

/**
 * Created by trist on 12/27/19.
 */

public interface SocketDeviceStatusSource {

    interface LoadDeviceStatusCallback{

        void onDeviceStatusLoaded();
    }

    interface GetDeviceStatusCallback{

        void onDeviceStatusReceived(boolean power);

        void onDeviceNotAvailable();
    }

    interface SetPowerForDeviceCallback{

        void onPowerForDeviceSet(boolean power);

        void onFailedToSetPowerForDevice();
    }

    interface LoadOneTimeTimerStatusCallback {

        void onOneTimeTimerStatusLoaded();
    }

    interface SetTimerOnDeviceCallback{

        void onTimerSet(JSONObject response);

        void onFailedToSetTimer();

    }

    interface SetOneTimeTimerOnDeviceCallback{

        void onTimerSet(boolean armed, int action, Date executionDate);

        void onFailedToSetTimer();
    }

    void loadDevicesStatus(@NonNull Context context, @NonNull LoadDeviceStatusCallback callback);

    void loadDeviceStatus(@NonNull Context context, @NonNull LoadDeviceStatusCallback callback,
                          @NonNull SocketDevice device);

    void setPowerForDevice(@NonNull Context context,
                           @NonNull SetPowerForDeviceCallback callback,
                           @NonNull SocketDevice device,
                           boolean power);

    void loadOneTimeTimerStatus(@NonNull Context context, @NonNull SocketDevice device,
                                @NonNull LoadOneTimeTimerStatusCallback callback);
}
