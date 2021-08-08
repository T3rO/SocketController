package com.tninteractive.socketcontroller.socketdevicedetail;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.tninteractive.socketcontroller.R;
import com.tninteractive.socketcontroller.SocketControllerApp;
import com.tninteractive.socketcontroller.adddevice.AddDeviceFragment;
import com.tninteractive.socketcontroller.data.SocketDevice;
import com.tninteractive.socketcontroller.options.OptionsActivity;

public class SocketDeviceDetailActivity extends AppCompatActivity implements
        SocketDeviceDetailContract.View,
        AddDeviceFragment.AddSocketListener{

    private TextView noConnectionTextView;

    private TextView powerTextView;
    private Switch powerSwitch;

    private TextView timerTextView;
    private TextView timerInfoTextView;
    private Switch timerSwitch;
    private Spinner timerActionSpinner;
    private EditText timerTimeEditText;
    private Button setTimerButton;
    private TextView setTimerTurnTextView;
    private TextView setTimerInTextView;

    private SocketDeviceDetailContract.Presenter mPresenter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_socket_device_detail);

        Intent intent = getIntent();
        int deviceId = intent.getIntExtra("DeviceId",0);

        Toolbar myToolbar = findViewById(R.id.socket_device_detail_toolbar);
        setSupportActionBar(myToolbar);

        noConnectionTextView = findViewById(R.id.device_detail_no_connection_text_view);

        powerTextView = findViewById(R.id.device_detail_power_text_view);
        powerSwitch = findViewById(R.id.socketDevicePowerSwitch);

        timerTextView = findViewById(R.id.socketDeviceDetailTimerTextView);
        timerInfoTextView = findViewById(R.id.socketDeviceDetailTimerTimeTextView);
        timerSwitch = findViewById(R.id.socketDeviceDetailTimerSwitch);
        timerActionSpinner = findViewById(R.id.socketDeviceDetailTimerActionSpinner);
        timerTimeEditText = findViewById(R.id.socketDeviceDetailTimerTimeEditText);
        setTimerButton = findViewById(R.id.socketDeviceDetailSetTimerButton);
        setTimerTurnTextView = findViewById(R.id.socketDeviceDetailSetTimerTurnTextView);
        setTimerInTextView = findViewById(R.id.socketDeviceDetailSetTimerInTextView);

        String[] timerActions = {"On", "Off"};
        ArrayAdapter<String> timerActionAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, timerActions);

        timerActionSpinner.setAdapter(timerActionAdapter);

        timerActionSpinner.setSelection(1);

        showPowerUI(false);
        showTimerUI(false);
        showSetTimerUI(false);

        mPresenter = new SocketDeviceDetailPresenter(this,
                ((SocketControllerApp)getApplication()).getRepository(),
                deviceId);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.device_detail_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.device_detail_refresh:
                mPresenter.refresh();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mPresenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mPresenter.stop();
    }

    @Override
    public void fragmentAttached() {

    }

    @Override
    public void fragmentDetached(){}

    public void previousFragment(){
        getSupportFragmentManager().popBackStack();
    }


    public void onClickPowerSwitch(View view){
        mPresenter.setPowerForDevice(powerSwitch.isChecked());
    }

    public void onClickTimerSwitch(View view){
        System.out.println("timer switch on click");
        mPresenter.enableOneTimeTimer(timerSwitch.isChecked());
    }

    public void onClickSetTimerButton(View view){
        timerTimeEditText.clearFocus();
        setTimer();
    }

    public void onClickBackButton(View view){
        finish();
    }

    public void onClickDeleteButton(View view){
        mPresenter.deleteDevice();
        finish();
    }

    public void onClickEditButton(View view){

    }

    @Override
    public void showDeviceInfo(String deviceName, String deviceIp) {
        getSupportActionBar().setTitle(deviceName);
        getSupportActionBar().setSubtitle(deviceIp);
    }

    @Override
    public void showDeviceStatus(SocketDevice device) {
        switch (device.getConnectionStatus()){
            case SocketDevice.CONNECTION_STATUS_CONNECTING:
                showPowerUI(false);
                showTimerUI(false);
                showSetTimerUI(false);
                showNoConnectionUI(false);

                break;
            case SocketDevice.CONNECTION_STATUS_CONNECTED:
                showPowerUI(true);
                showNoConnectionUI(false);

                powerSwitch.setChecked(device.isPowerOn());
                break;
            case SocketDevice.CONNECTION_STATUS_NO_CONNECTION:
                showPowerUI(false);
                showTimerUI(false);
                showSetTimerUI(false);
                showNoConnectionUI(true);

                break;
        }

    }

    @Override
    public void showTimer(boolean timerLoaded, boolean timerEnabled, boolean timerSet,
                          boolean power, String timerTime) {

        showTimerUI(timerLoaded);
        if(timerLoaded){
            timerSwitch.setChecked(timerEnabled);
            showSetTimerUI(timerEnabled);
            if(timerSet){
                String timerActionString = power ? "On" : "Off";
                timerInfoTextView.setText("Turn " + timerActionString + " in " + timerTime);
            }else{
                timerInfoTextView.setText("Timer not set");
            }
        }else{
            showSetTimerUI(false);
        }
    }

    @Override
    public void showInvalidTimerMessage(String message) {
        timerInfoTextView.setText(message);
    }

    @Override
    public void showTimerNotSetMessage() {
        timerInfoTextView.setText(R.string.timer_not_set_message);
    }

    @Override
    public void showErrorSettingTimerMessage() {
        timerInfoTextView.setText("Error");
    }

    private void showNoConnectionUI(boolean show){
        int visibility = show ? View.VISIBLE : View.GONE;
        noConnectionTextView.setVisibility(visibility);
    }

    private void showPowerUI(boolean show){
        int visibility = show ? View.VISIBLE : View.GONE;
        powerTextView.setVisibility(visibility);
        powerSwitch.setVisibility(visibility);
    }

    private void showTimerUI(boolean show){
        int visibility = show ? View.VISIBLE : View.GONE;
        timerTextView.setVisibility(visibility);
        timerSwitch.setVisibility(visibility);
        timerInfoTextView.setVisibility(visibility);
    }

    private void showSetTimerUI(boolean show){
        if(show){
            timerActionSpinner.setVisibility(View.VISIBLE);
            timerTimeEditText.setVisibility(View.VISIBLE);
            setTimerButton.setVisibility(View.VISIBLE);
            setTimerTurnTextView.setVisibility(View.VISIBLE);
            setTimerInTextView.setVisibility(View.VISIBLE);
        }else{
            timerActionSpinner.setVisibility(View.GONE);
            timerTimeEditText.setVisibility(View.GONE);
            setTimerButton.setVisibility(View.GONE);
            setTimerTurnTextView.setVisibility(View.GONE);
            setTimerInTextView.setVisibility(View.GONE);
        }
    }

    private void setTimer(){
        boolean power = timerActionSpinner.getSelectedItemPosition() == 0;
        mPresenter.setOneTimeTimer(power, timerTimeEditText.getText().toString());
    }



    @Override
    public void setPresenter(SocketDeviceDetailContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public Context getViewContext() {
        return getApplicationContext();
    }
}
