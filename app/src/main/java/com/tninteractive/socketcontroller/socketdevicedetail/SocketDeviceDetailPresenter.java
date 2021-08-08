package com.tninteractive.socketcontroller.socketdevicedetail;

import android.content.SharedPreferences;
import android.os.Handler;
import android.preference.PreferenceManager;

import com.tninteractive.socketcontroller.data.DataRepository;
import com.tninteractive.socketcontroller.data.DataSource;
import com.tninteractive.socketcontroller.data.OneTimeTimer;
import com.tninteractive.socketcontroller.data.SocketDevice;
import com.tninteractive.socketcontroller.data.SocketDeviceStatusSource;
import com.tninteractive.socketcontroller.options.OptionsPresenter;

import java.util.Calendar;
import java.util.Date;

import androidx.annotation.NonNull;

/**
 * Created by trist on 12/27/19.
 */

public class SocketDeviceDetailPresenter implements SocketDeviceDetailContract.Presenter {

    private final SocketDeviceDetailContract.View mView;

    private final DataRepository mRepository;

    private final int mDeviceId;

    private SocketDevice mDevice;

    private boolean autoCheckDevicesStatusEnabled;
    private int checkDevicesStatusRate;

    private boolean checkingDeviceStatusStarted;
    private Handler checkDeviceHandler = new Handler();

    private boolean timerCountdownStarted;
    private Handler timerCountdownHandler = new Handler();

    public SocketDeviceDetailPresenter(@NonNull SocketDeviceDetailContract.View view,
                                       @NonNull DataRepository repository,
                                       @NonNull int deviceId){
        mView = view;
        mRepository = repository;
        mDeviceId = deviceId;
    }

    @Override
    public void start() {
        loadPreferences();
        loadDevice();
        if(autoCheckDevicesStatusEnabled){
            startAutoCheckingDevice();
        }
    }

    @Override
    public void stop() {
        stopAutoCheckingDevice();
    }

    private void loadPreferences(){
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(mView.getViewContext());

        autoCheckDevicesStatusEnabled = sharedPrefs.getBoolean(OptionsPresenter.AUTO_REFRESH_KEY,
                OptionsPresenter.AUTO_REFRESH_DEFAULT_VALUE);
        checkDevicesStatusRate = OptionsPresenter.AUTO_REFRESH_RATES_IN_SECONDS[
                sharedPrefs.getInt(OptionsPresenter.AUTO_REFRESH_RATE_ID_KEY,
                        OptionsPresenter.REFRESH_RATE_ID_DEFAULT_VALUE)] ;
    }

    @Override
    public void refresh() {
        stopAutoCheckingDevice();

        loadDevice();
        if(autoCheckDevicesStatusEnabled){
            startAutoCheckingDevice();
        }
    }

    private void loadDevice(){
        mRepository.getDevice(mDeviceId, new DataSource.LoadDeviceCallback() {
            @Override
            public void onDeviceLoaded(SocketDevice device) {
                mDevice = device;
                mView.showDeviceInfo(device.getName(), device.getIp());

                loadDeviceStatus();
                loadOneTimeTimer();
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    private void loadOneTimeTimer(){
        mRepository.getOneTimeTimer(mDevice.getId(), new DataSource.LoadOneTimeTimerCallback() {
            @Override
            public void onOneTimeTimerLoaded(OneTimeTimer timer) {
                loadOneTimeTimerStatus();
            }

            @Override
            public void onDataNotAvailable() {

            }
        });
    }

    private void loadDeviceStatus(){
        mRepository.loadDeviceStatus(mView.getViewContext(), new SocketDeviceStatusSource.LoadDeviceStatusCallback() {
            @Override
            public void onDeviceStatusLoaded() {
                mView.showDeviceStatus(mDevice);
            }
        }, mDevice);
    }

    private void loadOneTimeTimerStatus(){
        mRepository.loadOneTimeTimerStatus(mView.getViewContext(), mDevice,
                new SocketDeviceStatusSource.LoadOneTimeTimerStatusCallback() {
                    @Override
                    public void onOneTimeTimerStatusLoaded() {
                        showOneTimeTimer();

                        if(mDevice.getOneTimeTimer().isArmedOnDevice()){
                            stopTimerCountdown();
                            startTimerCountdown();
                        }else{
                            stopTimerCountdown();
                        }
                    }
                });
    }

    @Override
    public void deleteDevice() {
        mRepository.deleteDevice(mDeviceId);
    }

    @Override
    public void setPowerForDevice(boolean power) {
        mRepository.setPowerForDevice(mView.getViewContext(), new SocketDeviceStatusSource.SetPowerForDeviceCallback() {
            @Override
            public void onPowerForDeviceSet(boolean power) {
                mDevice.setConnectionStatus(SocketDevice.CONNECTION_STATUS_CONNECTED);
                mDevice.setPowerOn(power);

                mView.showDeviceStatus(mDevice);
            }

            @Override
            public void onFailedToSetPowerForDevice() {
                mDevice.setConnectionStatus(SocketDevice.CONNECTION_STATUS_NO_CONNECTION);

                mView.showDeviceStatus(mDevice);
            }
        }, mDevice, power);
    }





    private void showOneTimeTimer(){
        OneTimeTimer timer = mDevice.getOneTimeTimer();
        boolean loaded = mDevice.getConnectionStatus() == SocketDevice.CONNECTION_STATUS_CONNECTED;
        mView.showTimer(loaded , timer.isEnabled(), timer.isSet(),
                timer.getAction() == 1, timer.getFormattedTimerTime());
    }

    @Override
    public void enableOneTimeTimer(boolean enable) {
        mDevice.getOneTimeTimer().setEnabled(enable);
        if(enable){
            startOneTimeTimer();
        }else{
            disableOneTimerTimerOnDevice();
        }

        showOneTimeTimer();
    }

    @Override
    public void setOneTimeTimer(boolean power, String timeInputString) {
        String[] timeInput = timeInputString.split(":");
        if(timeInput.length == 0){
            mView.showInvalidTimerMessage("Invalid input");
            System.out.println("Invalid time input");
            return;
        }

        int minutes = 0;
        int hours = 0;

        try{
            minutes = Integer.parseInt(timeInput[timeInput.length-1]);
            if(timeInput.length >= 2){
                hours = Integer.parseInt(timeInput[timeInput.length-2]);
            }
        }catch(NumberFormatException e){
            e.printStackTrace();
        }

        if(hours == 0 && minutes == 0){
            mView.showInvalidTimerMessage("Invalid input");
            System.out.println("Invalid time input");
            return;
        }

        int totalMinutes = hours * 60 + minutes;

        OneTimeTimer timer = mDevice.getOneTimeTimer();

        timer.setTimerTime(totalMinutes);
        timer.setAction(power ? 1 : 0);

        mRepository.saveOneTimeTimerForDevice(mDevice);

        if(timer.isEnabled()){
            startOneTimeTimer();
        }
    }

    private void startOneTimeTimer(){
        if(!mDevice.getOneTimeTimer().isSet()){
            System.out.println("Timer not set");
            return;
        }

        mDevice.getOneTimeTimer().calculateAndSetExecutionDate();

        setOneTimeTimerOnDevice();
    }

    private void setOneTimeTimerOnDevice(){
        OneTimeTimer timer = mDevice.getOneTimeTimer();

        if(timer.getExecutionDate() == null){
            System.out.println("timer execution date not set");
            return;
        }

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(timer.getExecutionDate());
        calendar.setFirstDayOfWeek(Calendar.SUNDAY);
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        mRepository.setOneTimeTimerOnDevice(mView.getViewContext(), mDevice.getIp(),
                timer.isEnabled(), hour, minute, day, timer.getAction(),
                new SocketDeviceStatusSource.SetOneTimeTimerOnDeviceCallback() {
                    @Override
                    public void onTimerSet(boolean armed, int action, Date executionDate) {
                        mDevice.getOneTimeTimer().setArmedOnDevice(armed);
                        mDevice.getOneTimeTimer().setEnabled(armed);
                        mDevice.getOneTimeTimer().setAction(action);
                        mDevice.getOneTimeTimer().setExecutionDate(executionDate);

                        showOneTimeTimer();

                        stopTimerCountdown();
                        startTimerCountdown();
                    }

                    @Override
                    public void onFailedToSetTimer() {
                        mView.showErrorSettingTimerMessage();
                    }
                });
    }

    private void disableOneTimerTimerOnDevice(){
        mRepository.disableOneTimeTimerOnDevice(mView.getViewContext(), mDevice.getIp(),
                new SocketDeviceStatusSource.SetOneTimeTimerOnDeviceCallback() {
                    @Override
                    public void onTimerSet(boolean armed, int action, Date executionDate) {
                        mDevice.getOneTimeTimer().setArmedOnDevice(armed);
                        mDevice.getOneTimeTimer().setEnabled(armed);

                        showOneTimeTimer();

                        stopTimerCountdown();
                    }

                    @Override
                    public void onFailedToSetTimer() {
                        mView.showErrorSettingTimerMessage();
                    }
                });
    }

    private Runnable autoCheckDeviceStatus = new Runnable() {
        @Override
        public void run() {
            loadDeviceStatus();

            if(checkingDeviceStatusStarted) {
                doAutoCheckDeviceStatus();
            }
        }
    };

    private void stopAutoCheckingDevice() {
        checkingDeviceStatusStarted = false;
        checkDeviceHandler.removeCallbacks(autoCheckDeviceStatus);
    }

    private void startAutoCheckingDevice() {
        if(checkingDeviceStatusStarted){
            return;
        }
        checkingDeviceStatusStarted = true;
        doAutoCheckDeviceStatus();
    }

    private void doAutoCheckDeviceStatus(){
        checkDeviceHandler.postDelayed(autoCheckDeviceStatus,
                checkDevicesStatusRate * 1000);
    }

    private Runnable timerCountdownRunnable = new Runnable() {
        @Override
        public void run() {
            if(timerCountdownStarted) {
                if(mDevice.getOneTimeTimer().getMinutesUntilExecution() <= 0){
                    loadOneTimeTimerStatus();
                    loadDeviceStatus();
                }else{
                    showOneTimeTimer();

                    timerCountdown();
                }
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
