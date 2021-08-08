package com.tninteractive.socketcontroller.data;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by trist on 12/27/19.
 */

public interface DataSource {

    interface LoadDevicesCallback {

        void onDevicesLoaded(List<SocketDevice> devices);

        void onDataNotAvailable();
    }

    interface LoadDeviceCallback {

        void onDeviceLoaded(SocketDevice device);

        void onDataNotAvailable();
    }

    interface SaveDeviceCallback{
        void onDeviceSaved(int id);
    }

    interface LoadOneTimeTimerCallback{

        void onOneTimeTimerLoaded(OneTimeTimer timer);

        void onDataNotAvailable();
    }

    void getDevices(@NonNull LoadDevicesCallback callback);

    void getDevice(int id, @NonNull LoadDeviceCallback callback);

    void saveDevice(@NonNull SocketDevice device);

    void saveDevice(@NonNull SocketDevice device, @NonNull SaveDeviceCallback callback);

    void deleteDevice(@NonNull int deviceId);

    void getOneTimeTimer(int deviceId, @NonNull LoadOneTimeTimerCallback callback);

    void saveOneTimeTimerForDevice(@NonNull SocketDevice device);


}
