package com.tninteractive.socketcontroller;

import android.app.Application;

import com.tninteractive.socketcontroller.data.AppDatabase;
import com.tninteractive.socketcontroller.data.DataRepository;
import com.tninteractive.socketcontroller.data.LocalDataSource;
import com.tninteractive.socketcontroller.util.AppExecutors;

/**
 * Created by trist on 12/25/19.
 */

public class SocketControllerApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
    }

    public DataRepository getRepository(){
        AppDatabase db = AppDatabase.getInstance(this);
        return DataRepository.getInstance(LocalDataSource.getInstance(new AppExecutors(),
                db.socketDeviceDao(), db.oneTimeTimerDao()));
    }
}
