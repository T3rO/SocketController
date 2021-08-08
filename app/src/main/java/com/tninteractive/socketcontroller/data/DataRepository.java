package com.tninteractive.socketcontroller.data;

import android.content.Context;
import android.util.ArrayMap;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;

/**
 * Created by trist on 12/25/19.
 */

public class DataRepository implements DataSource, SocketDeviceStatusSource{

    private final String GET_STATUS_URL = "http://<ip>/cm?cmnd=Status";
    private final String SWITCH_POWER_URL = "http://<ip>/cm?cmnd=Power%20<power>";
    private final String SET_TIMER_URL = "http://<ip>/cm?cmnd=Timer<timerId>%20<payload>";

    private static DataRepository sInstance;

    private DataSource mLocalDataSource;

    private Map<Integer, SocketDevice> mCachedDevices;

    private DataRepository(@NonNull DataSource localDataSource){
        mLocalDataSource = localDataSource;
    }

    public static DataRepository getInstance(@NonNull DataSource localDataSource) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(localDataSource);
                }
            }
        }
        return sInstance;
    }

    @Override
    public void getDevices(@NonNull final LoadDevicesCallback callback) {
        mLocalDataSource.getDevices(new LoadDevicesCallback() {
            @Override
            public void onDevicesLoaded(List<SocketDevice> devices) {
                refreshCache(devices);
                callback.onDevicesLoaded(new ArrayList<>(mCachedDevices.values()));
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    public List<SocketDevice> getDevicesAsList(){
        ArrayList<SocketDevice> devices = new ArrayList<>();
        if(mCachedDevices != null){
            devices.addAll(mCachedDevices.values());
        }
        return devices;
    }

    @Override
    public void getDevice(@NonNull final int id, @NonNull final LoadDeviceCallback callback) {

        if(mCachedDevices != null){
            SocketDevice cachedDevice = mCachedDevices.get(id);

            if(cachedDevice != null){
                callback.onDeviceLoaded(mCachedDevices.get(id));
                return;
            }
        }

        mLocalDataSource.getDevice(id, new LoadDeviceCallback() {
            @Override
            public void onDeviceLoaded(SocketDevice device) {
                if (mCachedDevices == null) {
                    mCachedDevices = new LinkedHashMap<>();
                }
                mCachedDevices.put(device.getId(), device);
                callback.onDeviceLoaded(device);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    @Override
    public void saveDevice(SocketDevice device) {
        mLocalDataSource.saveDevice(device);

        if (mCachedDevices == null) {
            mCachedDevices = new LinkedHashMap<>();
        }
        mCachedDevices.put(device.getId(), device);
    }

    @Override
    public void saveDevice(@NonNull SocketDevice device, @NonNull SaveDeviceCallback callback) {
        mLocalDataSource.saveDevice(device, callback);

        if (mCachedDevices == null) {
            mCachedDevices = new LinkedHashMap<>();
        }
        mCachedDevices.put(device.getId(), device);
    }

    @Override
    public void deleteDevice(int deviceId) {
        mLocalDataSource.deleteDevice(deviceId);
        mCachedDevices.remove(deviceId);
    }

    @Override
    public void getOneTimeTimer(int deviceId, @NonNull final LoadOneTimeTimerCallback callback){
        if(mCachedDevices == null){
            System.out.println("Devices not loaded");
            callback.onDataNotAvailable();
            return;
        }
        final SocketDevice device = mCachedDevices.get(deviceId);
        if(device == null){
            System.out.println("Device not loaded");
            callback.onDataNotAvailable();
            return;
        }

        if(device.getOneTimeTimer().isLoaded()){
            callback.onOneTimeTimerLoaded(device.getOneTimeTimer());
        }else {
            mLocalDataSource.getOneTimeTimer(deviceId, new LoadOneTimeTimerCallback() {
                @Override
                public void onOneTimeTimerLoaded(OneTimeTimer timer) {
                    OneTimeTimer deviceTimer = device.getOneTimeTimer();

                    deviceTimer.setTimerTime(timer.getTimerTime());
                    deviceTimer.setAction(timer.getAction());

                    callback.onOneTimeTimerLoaded(deviceTimer);
                }

                @Override
                public void onDataNotAvailable() {
                    callback.onDataNotAvailable();
                }
            });
        }
    }

    @Override
    public void saveOneTimeTimerForDevice(@NonNull SocketDevice device) {
        mLocalDataSource.saveOneTimeTimerForDevice(device);
    }

    private void refreshCache(List<SocketDevice> devices) {
        if (mCachedDevices == null) {
            mCachedDevices = new LinkedHashMap<>();
        }
        mCachedDevices.clear();
        for (SocketDevice d : devices) {
            mCachedDevices.put(d.getId(), d);
        }
    }

    @Override
    public void loadDevicesStatus(@NonNull Context context, @NonNull final LoadDeviceStatusCallback callback) {
        if(mCachedDevices == null){
            return;
        }
        for (final SocketDevice device : mCachedDevices.values()){
            loadDeviceStatus(context, callback, device);
        }
    }

    @Override
    public void loadDeviceStatus(@NonNull Context context,
                                 @NonNull final LoadDeviceStatusCallback callback,
                                 @NonNull final SocketDevice device) {

        getPowerStatus(context, new GetDeviceStatusCallback() {
            @Override
            public void onDeviceStatusReceived(boolean power) {
                device.setConnectionStatus(SocketDevice.CONNECTION_STATUS_CONNECTED);
                device.setPowerOn(power);

                callback.onDeviceStatusLoaded();
            }

            @Override
            public void onDeviceNotAvailable() {
                device.setConnectionStatus(SocketDevice.CONNECTION_STATUS_NO_CONNECTION);
                callback.onDeviceStatusLoaded();
            }
        }, device.getIp());
    }

    @Override
    public void setPowerForDevice(Context context, final SetPowerForDeviceCallback callback,
                                  SocketDevice device, final boolean power) {
        RequestQueue queue = Volley.newRequestQueue(context);
        String url =getSwitchPowerURL(device.getIp(), power);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        try{
                            JSONObject responseJS = new JSONObject(response);

                            String powerStatusString = responseJS.getString("POWER");
                            int powerStatusCode = getPowerStatusCode(powerStatusString);

                            callback.onPowerForDeviceSet(powerStatusCode == 1);


                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                        // Display the first 500 characters of the response string.
                        if(response.length() > 500){
                            //textView.setText("Response is: "+ response.substring(0,500));
                        } else{
                            //textView.setText("Response is: "+ response);

                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                callback.onFailedToSetPowerForDevice();
            }
        });

        queue.add(stringRequest);
    }

    private void getPowerStatus(@NonNull Context context,
                                @NonNull final GetDeviceStatusCallback callback,
                                String deviceIp){
        RequestQueue queue = Volley.newRequestQueue(context);
        String url = getStatusURL(deviceIp);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println(response);
                        try{
                            JSONObject responseJS = new JSONObject(response);

                            int powerStatusCode = responseJS.getJSONObject("Status").getInt("Power");

                            callback.onDeviceStatusReceived(powerStatusCode == 1);

                        }catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error response get power status");
                error.printStackTrace();
                callback.onDeviceNotAvailable();
            }
        });

        queue.add(stringRequest);
    }

    public void loadOneTimeTimerStatus(@NonNull Context context, @NonNull final SocketDevice device,
                                       @NonNull final LoadOneTimeTimerStatusCallback callback){

        getTimerStatus(context, device.getIp(), 16, new SetTimerOnDeviceCallback() {
            @Override
            public void onTimerSet(JSONObject response) {

                oneTimeTimerOnDeviceSet(response, new SetOneTimeTimerOnDeviceCallback() {
                    @Override
                    public void onTimerSet(boolean armed, int action, Date executionDate) {
                        device.getOneTimeTimer().setEnabled(armed);
                        device.getOneTimeTimer().setArmedOnDevice(armed);
                        device.getOneTimeTimer().setAction(action);
                        device.getOneTimeTimer().setExecutionDate(executionDate);


                        callback.onOneTimeTimerStatusLoaded();
                    }

                    @Override
                    public void onFailedToSetTimer() {
                        callback.onOneTimeTimerStatusLoaded();
                    }
                });


            }

            @Override
            public void onFailedToSetTimer() {
                callback.onOneTimeTimerStatusLoaded();
            }
        });

    }

    private void getTimerStatus(@NonNull Context context, String deviceIp, final int timerId,
                                @NonNull final SetTimerOnDeviceCallback callback){

        JSONObject payload = new JSONObject();
        String url = getSetTimerURL(deviceIp, timerId, payload);

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("timer status response: " + response);

                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            JSONObject timerJSON = responseJSON.getJSONObject("Timer" + timerId);
                            callback.onTimerSet(timerJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                            callback.onFailedToSetTimer();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error response get timer");
                callback.onFailedToSetTimer();
            }
        });

        queue.add(stringRequest);
    }

    public void setOneTimeTimerOnDevice(@NonNull Context context,
                                        String deviceIp,
                                        boolean arm,
                                        int timeHours,
                                        int timeMinutes,
                                        int day,
                                        int action,
                                        final SetOneTimeTimerOnDeviceCallback callback){

        List<Integer> days = new ArrayList<>();
        days.add(day);
        setTimerOnDevice(context, deviceIp, 16, arm, 0, timeHours, timeMinutes,
                0, days, false, 16, action, new SetTimerOnDeviceCallback() {
                    @Override
                    public void onTimerSet(JSONObject response) {
                        oneTimeTimerOnDeviceSet(response, callback);
                    }

                    @Override
                    public void onFailedToSetTimer() {
                        callback.onFailedToSetTimer();
                    }
                });
    }

    public void disableOneTimeTimerOnDevice(@NonNull Context context,
                                            String deviceIp,
                                            final SetOneTimeTimerOnDeviceCallback callback){

        setTimerOnDevice(context, deviceIp, 16, false, new SetTimerOnDeviceCallback() {
            @Override
            public void onTimerSet(JSONObject response) {
                oneTimeTimerOnDeviceSet(response, callback);
            }

            @Override
            public void onFailedToSetTimer() {
                callback.onFailedToSetTimer();
            }
        });
    }

    private void oneTimeTimerOnDeviceSet(JSONObject response, SetOneTimeTimerOnDeviceCallback callback){
        try {
            boolean armed = response.getInt("Arm") == 1;
            int action = response.getInt("Action");

            String timeString = response.getString("Time");
            int hour = Integer.parseInt(timeString.substring(0,2));
            int minute = Integer.parseInt(timeString.substring(3,5));

            String daysString = response.getString("Days");
            int day = -1;
            for (int i = 0; i < 7; i++) {
                if(daysString.charAt(i) != '-' && daysString.charAt(i) != '0'){
                    day = i+1;
                    break;
                }
            }

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.DAY_OF_WEEK, day);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);

            Date date = cal.getTime();

            callback.onTimerSet(armed, action, date);

        } catch (JSONException e) {
            callback.onFailedToSetTimer();
            e.printStackTrace();
        }
    }


    private void setTimerOnDevice(@NonNull Context context,
                                  String deviceIp,
                                  final int timerId,
                                  boolean arm,
                                  final SetTimerOnDeviceCallback callback){

        JSONObject payload = new JSONObject();
        try {
            payload.put("Arm", arm ? 1 : 0);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getSetTimerURL(deviceIp, timerId, payload);

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("timer response: " + response);

                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            JSONObject timerJSON = responseJSON.getJSONObject("Timer" + timerId);
                            callback.onTimerSet(timerJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error response set timer");
                callback.onFailedToSetTimer();
            }
        });

        queue.add(stringRequest);
    }


    private void setTimerOnDevice(@NonNull Context context,
                                  String deviceIp,
                                  final int timerId,
                                  boolean arm,
                                  int mode,
                                  int timeHours,
                                  int timeMinutes,
                                  int window,
                                  List<Integer> days,
                                  boolean repeat,
                                  int output,
                                  int action,
                                  final SetTimerOnDeviceCallback callback){

        String timeHoursString = getFormattedTimeString(timeHours);
        String timeMinutesString = getFormattedTimeString(timeMinutes);
        String timeString = timeHoursString + ":" + timeMinutesString;
        String daysMask = "";
        for (int i = 1; i <= 7; i++) {
            if(days.contains(i)){
                daysMask += 1;
            }
            daysMask += 0;
        }

        JSONObject payload = new JSONObject();
        try {
            payload.put("Arm", arm ? 1 : 0);
            payload.put("Mode", mode);
            payload.put("Time", timeString);
            payload.put("Window", window);
            payload.put("Days", daysMask);
            payload.put("Repeat", repeat ? 1 : 0);
            payload.put("Output", output);
            payload.put("Action", action);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        String url = getSetTimerURL(deviceIp, timerId, payload);

        RequestQueue queue = Volley.newRequestQueue(context);

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        System.out.println("timer response: " + response);

                        try {
                            JSONObject responseJSON = new JSONObject(response);
                            JSONObject timerJSON = responseJSON.getJSONObject("Timer" + timerId);
                            callback.onTimerSet(timerJSON);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                System.out.println("error response set timer");
                callback.onFailedToSetTimer();
            }
        });

        queue.add(stringRequest);
    }



    private String getFormattedTimeString(int time){
        if(time < 10){
            return "0" + time;
        }
        return "" + time;
    }

    private String getStatusURL(String ip){
        return GET_STATUS_URL.replace("<ip>", ip);
    }

    private String getSwitchPowerURL(String ip, boolean power){
        String powerString = power ? "On" : "Off";
        return SWITCH_POWER_URL.replace("<ip>", ip).replace("<power>", powerString);
    }

    private String getSetTimerURL(String ip, int timerId, JSONObject payload){
        return SET_TIMER_URL.replace("<ip>", ip)
                .replace("<timerId>", "" + timerId)
                .replace("<payload>", payload.toString());
    }

    private int getPowerStatusCode(String codeString){
        if(codeString.equals("OFF")){
            return 0;
        } else if( codeString.equals("ON")){
            return 1;
        }
        return 0;
    }
}
