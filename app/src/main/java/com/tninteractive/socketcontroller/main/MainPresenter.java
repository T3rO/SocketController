package com.tninteractive.socketcontroller.main;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.tninteractive.socketcontroller.data.DataRepository;
import com.tninteractive.socketcontroller.data.DataSource;
import com.tninteractive.socketcontroller.data.SocketDevice;
import com.tninteractive.socketcontroller.data.SocketDeviceStatusSource;
import com.tninteractive.socketcontroller.options.OptionsPresenter;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by trist on 12/26/19.
 */

public class MainPresenter implements MainContract.Presenter{

    private final MainContract.View mMainView;

    private final DataRepository mRepository;

    private boolean autoCheckDevicesStatusEnabled;
    private int checkDevicesStatusRate;

    private SharedPreferences sharedPrefs;

    private boolean checkingDevicesStarted;
    private Handler checkDevicesHandler = new Handler();

    private boolean timerCountdownStarted;
    private Handler timerCountdownHandler = new Handler();

    public MainPresenter(@NonNull MainContract.View mainView, @NonNull DataRepository repository) {
        mMainView = mainView;
        mRepository = repository;

        mMainView.setPresenter(this);

        sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mMainView.getViewContext());
    }

    @Override
    public void start() {
        loadPreferences();
        loadDevices();
        if(autoCheckDevicesStatusEnabled){
            startAutoCheckingDevices();
        }
        startTimerCountdown();
    }

    @Override
    public void stop() {
        stopAutoCheckingDevices();
        stopTimerCountdown();
    }

    private void loadPreferences(){
        autoCheckDevicesStatusEnabled = sharedPrefs.getBoolean(OptionsPresenter.AUTO_REFRESH_KEY,
                OptionsPresenter.AUTO_REFRESH_DEFAULT_VALUE);
        checkDevicesStatusRate = OptionsPresenter.AUTO_REFRESH_RATES_IN_SECONDS[
                sharedPrefs.getInt(OptionsPresenter.AUTO_REFRESH_RATE_ID_KEY,
                        OptionsPresenter.REFRESH_RATE_ID_DEFAULT_VALUE)] ;
    }

    @Override
    public void refreshDeviceList() {
        stopAutoCheckingDevices();
        stopTimerCountdown();

        loadDevices();
        if(autoCheckDevicesStatusEnabled){
            startAutoCheckingDevices();
        }
        startTimerCountdown();
    }

    @Override
    public void openDeviceDetail(SocketDevice device) {
        mMainView.showDeviceDetail(device.getId());
    }

    @Override
    public void setPowerForDevice(final SocketDevice device, boolean power) {
        mRepository.setPowerForDevice(mMainView.getViewContext(), new SocketDeviceStatusSource.SetPowerForDeviceCallback() {
            @Override
            public void onPowerForDeviceSet(boolean power) {
                device.setPowerOn(power);
                device.setConnectionStatus(SocketDevice.CONNECTION_STATUS_CONNECTED);

                mMainView.refreshDeviceList();
            }

            @Override
            public void onFailedToSetPowerForDevice() {
                device.setConnectionStatus(SocketDevice.CONNECTION_STATUS_NO_CONNECTION);

                mMainView.refreshDeviceList();
            }
        }, device, power);
    }

    private void loadDevices(){
        mRepository.getDevices(new DataSource.LoadDevicesCallback() {
            @Override
            public void onDevicesLoaded(List<SocketDevice> devices) {
                mMainView.showDevices(devices);
                loadDevicesStatus();

                for (SocketDevice d : devices){
                    loadOneTimeTimerStatus(d);
                }
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    private void loadDevicesStatus(){
        mRepository.loadDevicesStatus(mMainView.getViewContext(),
                new SocketDeviceStatusSource.LoadDeviceStatusCallback() {
            @Override
            public void onDeviceStatusLoaded() {
                mMainView.refreshDeviceList();
            }
        });
    }

    private void loadDeviceStatus(SocketDevice device){
        mRepository.loadDeviceStatus(mMainView.getViewContext(),
                new SocketDeviceStatusSource.LoadDeviceStatusCallback() {
            @Override
            public void onDeviceStatusLoaded() {
                mMainView.refreshDeviceList();
            }
        }, device);
    }

    private void loadOneTimeTimerStatus(SocketDevice device){
        mRepository.loadOneTimeTimerStatus(mMainView.getViewContext(), device,
                new SocketDeviceStatusSource.LoadOneTimeTimerStatusCallback() {
            @Override
            public void onOneTimeTimerStatusLoaded() {
                mMainView.refreshDeviceList();
            }
        });
    }

    private Runnable autoCheckDevicesStatus = new Runnable() {
        @Override
        public void run() {
            if(checkingDevicesStarted) {
                loadDevicesStatus();

                doAutoCheckDevicesStatus();
            }
        }
    };

    private void stopAutoCheckingDevices() {
        checkingDevicesStarted = false;
        checkDevicesHandler.removeCallbacks(autoCheckDevicesStatus);
    }

    private void startAutoCheckingDevices() {
        if(checkingDevicesStarted){
            return;
        }
        checkingDevicesStarted = true;
        doAutoCheckDevicesStatus();
    }

    private void doAutoCheckDevicesStatus(){
        checkDevicesHandler.postDelayed(autoCheckDevicesStatus,
                checkDevicesStatusRate * 1000);
    }

    private Runnable timerCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            if(timerCountdownStarted) {

                boolean timersActive = false;
                for(SocketDevice device : mRepository.getDevicesAsList()){

                    if(device.getOneTimeTimer().isEnabled()){
                        timersActive = true;
                        if(device.getOneTimeTimer().getMinutesUntilExecution() <= 0){
                            loadOneTimeTimerStatus(device);
                            loadDeviceStatus(device);
                        }
                    }
                }

                if(timersActive){
                    mMainView.refreshDeviceList();
                }

                timerCountdown();
            }
        }
    };

    private void stopTimerCountdown() {
        timerCountdownStarted = false;
        timerCountdownHandler.removeCallbacks(timerCountdownRunnable);
    }

    private void startTimerCountdown() {
        if(timerCountdownStarted){
            return;
        }
        timerCountdownStarted = true;
        timerCountdown();
    }

    private void timerCountdown(){

        Date now = Calendar.getInstance().getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        int next = 60 - calendar.get(Calendar.SECOND) + 2;

        timerCountdownHandler.postDelayed(timerCountdownRunnable,
                next * 1000);
    }
}
