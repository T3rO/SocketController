package com.tninteractive.socketcontroller.adddevice;

import android.support.annotation.NonNull;

import com.tninteractive.socketcontroller.data.DataRepository;
import com.tninteractive.socketcontroller.data.DataSource;
import com.tninteractive.socketcontroller.data.SocketDevice;

/**
 * Created by trist on 12/26/19.
 */

public class AddDevicePresenter implements AddDeviceContract.Presenter {

    private final AddDeviceContract.View mAddDeviceView;

    private final DataRepository mRepository;

    public AddDevicePresenter(@NonNull AddDeviceContract.View addDeviceView, @NonNull DataRepository repository){
        mAddDeviceView = addDeviceView;
        mRepository = repository;

        mAddDeviceView.setPresenter(this);
    }

    @Override
    public void start() {

    }

    @Override
    public void stop() {

    }

    @Override
    public void addDevice(String name, String ip) {
        final SocketDevice newDevice = new SocketDevice(name, ip);
        mRepository.saveDevice(newDevice, new DataSource.SaveDeviceCallback() {
            @Override
            public void onDeviceSaved(int id) {
                newDevice.getOneTimeTimer().setDeviceId(id);
                mRepository.saveOneTimeTimerForDevice(newDevice);
            }
        });
    }
}
