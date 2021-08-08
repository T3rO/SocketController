package com.tninteractive.socketcontroller.data;

import com.tninteractive.socketcontroller.util.AppExecutors;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * Created by trist on 12/27/19.
 */

public class LocalDataSource implements DataSource{

    private static volatile LocalDataSource sInstance;

    private SocketDeviceDao mSocketDeviceDao;
    private OneTimeTimerDao mOneTimeTimerDao;

    private AppExecutors mAppExecutors;

    private LocalDataSource(@NonNull AppExecutors appExecutors,
                            @NonNull SocketDeviceDao socketDeviceDao,
                            @NonNull OneTimeTimerDao oneTimeTimerDao) {
        mAppExecutors = appExecutors;
        mSocketDeviceDao = socketDeviceDao;
        mOneTimeTimerDao = oneTimeTimerDao;
    }

    public static LocalDataSource getInstance(@NonNull AppExecutors appExecutors,
                                              @NonNull SocketDeviceDao socketDeviceDao,
                                              @NonNull OneTimeTimerDao oneTimeTimerDao) {
        if (sInstance == null) {
            synchronized (LocalDataSource.class) {
                if (sInstance == null) {
                    sInstance = new LocalDataSource(appExecutors, socketDeviceDao, oneTimeTimerDao);
                }
            }
        }
        return sInstance;
    }


    @Override
    public void getDevices(@NonNull final LoadDevicesCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final List<SocketDevice> devices = mSocketDeviceDao.getAll();
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (devices.isEmpty()) {
                            callback.onDataNotAvailable();
                        } else {
                            callback.onDevicesLoaded(devices);
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void getDevice(@NonNull final int id, @NonNull final LoadDeviceCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final SocketDevice device = mSocketDeviceDao.getById(id);
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (device != null) {
                            callback.onDeviceLoaded(device);
                        } else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveDevice(@NonNull final SocketDevice device) {
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                mSocketDeviceDao.insertAll(device);
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }

    @Override
    public void saveDevice(@NonNull final SocketDevice device, @NonNull final SaveDeviceCallback callback) {
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                long row = mSocketDeviceDao.insertAll(device)[0];
                callback.onDeviceSaved((int)row);
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }

    @Override
    public void deleteDevice(@NonNull final int deviceId) {
        Runnable deleteRunnable = new Runnable() {
            @Override
            public void run() {
                mSocketDeviceDao.deleteDeviceById(deviceId);
            }
        };

        mAppExecutors.diskIO().execute(deleteRunnable);
    }

    @Override
    public void getOneTimeTimer(final int deviceId, @NonNull final LoadOneTimeTimerCallback callback) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final OneTimeTimer timer = mOneTimeTimerDao.getOneTimeTimerByDeviceId(deviceId);
                mAppExecutors.mainThread().execute(new Runnable() {
                    @Override
                    public void run() {
                        if (timer != null) {
                            callback.onOneTimeTimerLoaded(timer);
                        } else {
                            callback.onDataNotAvailable();
                        }
                    }
                });
            }
        };

        mAppExecutors.diskIO().execute(runnable);
    }

    @Override
    public void saveOneTimeTimerForDevice(@NonNull final SocketDevice device) {
        Runnable saveRunnable = new Runnable() {
            @Override
            public void run() {
                mOneTimeTimerDao.insertAll(device.getOneTimeTimer());
            }
        };
        mAppExecutors.diskIO().execute(saveRunnable);
    }


}
