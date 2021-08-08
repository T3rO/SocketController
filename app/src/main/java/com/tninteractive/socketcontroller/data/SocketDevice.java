package com.tninteractive.socketcontroller.data;

import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

/**
 * Created by trist on 12/24/19.
 */

@Entity(tableName = "socket_device")
public class SocketDevice {

    public static final int CONNECTION_STATUS_CONNECTING = 0;
    public static final int CONNECTION_STATUS_CONNECTED = 1;
    public static final int CONNECTION_STATUS_NO_CONNECTION = 2;

    @PrimaryKey(autoGenerate = true)
    private int id;

    private String name;
    private String ip;

    @Ignore
    private int connectionStatus;

    @Ignore
    private boolean powerOn;

    @Ignore
    private OneTimeTimer oneTimeTimer;

    public SocketDevice(String name, String ip) {
        this.name = name;
        this.ip = ip;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }


    public int getConnectionStatus() {
        return connectionStatus;
    }

    public void setConnectionStatus(int connectionStatus) {
        this.connectionStatus = connectionStatus;
    }

    public boolean isPowerOn() {
        return powerOn;
    }

    public void setPowerOn(boolean powerOn) {
        this.powerOn = powerOn;
    }

    public OneTimeTimer getOneTimeTimer(){
        if(oneTimeTimer == null){
            oneTimeTimer = new OneTimeTimer(id,false, -1, -1);
        }
        return oneTimeTimer;
    }
}
