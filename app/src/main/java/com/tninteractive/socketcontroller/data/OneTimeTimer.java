package com.tninteractive.socketcontroller.data;

import com.tninteractive.socketcontroller.util.TimeUtil;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Ignore;

import static androidx.room.ForeignKey.CASCADE;

@Entity(foreignKeys = {@ForeignKey(
        onDelete = CASCADE,
        entity = SocketDevice.class,parentColumns = "id",childColumns = "device_id")},
        primaryKeys = {"device_id"},
        tableName = "one_time_timer")
public class OneTimeTimer {

    @ColumnInfo(name = "device_id")
    private int deviceId;

    @Ignore
    private boolean enabled;

    @ColumnInfo(name = "timer_time")
    private int timerTime;

    private int action;

    @Ignore
    private boolean armedOnDevice;

    @Ignore
    private Date executionDate;

    public OneTimeTimer(int deviceId, int timerTime, int action) {
        this(deviceId, false, timerTime, action);
    }

    public OneTimeTimer(int deviceId, boolean enabled, int timerTime, int action) {
        this.deviceId = deviceId;
        this.enabled = enabled;
        this.timerTime = timerTime;
        this.action = action;
        this.armedOnDevice = false;
    }

    public void calculateAndSetExecutionDate(){
        if(timerTime <= 0){
            System.out.println("timer timerTime not set");
            return;
        }

        Date now = Calendar.getInstance().getTime();

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.add(Calendar.MINUTE, timerTime);

        executionDate = calendar.getTime();
    }

    public boolean isSet(){
        return armedOnDevice || (timerTime > 0 && action >= 0);
    }

    public boolean isLoaded(){
        return timerTime > 0 && action >= 0;
    }

    public String getShortInfoText(){
        return getActionString() + " in " + getFormattedTimerTime();
    }

    public String getFormattedTimerTime(){
        if(armedOnDevice){
            return getFormattedTimerTimeFromExecutionDate();
        }
        return getFormattedTimerTimeFromMinutes();
    }

    private String getFormattedTimerTimeFromMinutes(){
        int time = timerTime;

        int days = time / (24 * 60);
        time = time % (24 * 60);

        int hours = time / 60;
        time = time % 60;

        return getFormattedTimerTimeString(days, hours, time);
    }

    private String getFormattedTimerTimeFromExecutionDate(){
        Date now = Calendar.getInstance().getTime();
        long[] elapsedTimeValues = TimeUtil.getElapsedTimeValues(now, executionDate);

        return getFormattedTimerTimeString((int)elapsedTimeValues[0],
                (int)elapsedTimeValues[1], (int)elapsedTimeValues[2]);
    }

    private String getFormattedTimerTimeString(int days, int hours, int minutes){
        NumberFormat formatter = new DecimalFormat("00");

        String timerTimeString = "";
        if(days > 0){
            timerTimeString += formatter.format(days) + ":";
        }
        timerTimeString += formatter.format(hours) + ":" + formatter.format(minutes);

        return timerTimeString;
    }

    public int getMinutesUntilExecution(){
        if(!armedOnDevice || executionDate == null){
            return timerTime;
        }

        Date now = Calendar.getInstance().getTime();

        long diff = executionDate.getTime() - now.getTime();

        int minutes = (int)Math.ceil(diff / 60000.0);

        return minutes;
    }

    public String getActionString(){
        switch (action){
            case 0:
                return "Off";
            case 1:
                return "On";
            default:
                return "";
        }
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public int getTimerTime() {
        return timerTime;
    }

    public void setTimerTime(int timerTime) {
        this.timerTime = timerTime;
    }

    public boolean isArmedOnDevice() {
        return armedOnDevice;
    }

    public void setArmedOnDevice(boolean armedOnDevice) {
        this.armedOnDevice = armedOnDevice;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public Date getExecutionDate() {
        return executionDate;
    }

    public void setExecutionDate(Date executionDate) {
        this.executionDate = executionDate;
    }
}
