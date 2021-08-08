package com.tninteractive.socketcontroller.adddevice;

import com.tninteractive.socketcontroller.BasePresenter;
import com.tninteractive.socketcontroller.BaseView;

/**
 * Created by trist on 12/26/19.
 */

public interface AddDeviceContract {

    interface View extends BaseView<Presenter> {


    }

    interface Presenter extends BasePresenter {

        void addDevice(String name, String ip);

    }
}
