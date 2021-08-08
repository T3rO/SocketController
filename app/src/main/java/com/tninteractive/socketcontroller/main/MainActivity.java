package com.tninteractive.socketcontroller.main;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.tninteractive.socketcontroller.adddevice.AddDeviceFragment;
import com.tninteractive.socketcontroller.data.DataRepository;
import com.tninteractive.socketcontroller.R;
import com.tninteractive.socketcontroller.SocketControllerApp;
import com.tninteractive.socketcontroller.data.SocketDevice;
import com.tninteractive.socketcontroller.options.OptionsActivity;
import com.tninteractive.socketcontroller.socketdevicedetail.SocketDeviceDetailActivity;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements MainContract.View,DeviceListAdapter.DevicesRecyclerViewListener, AddDeviceFragment.AddSocketListener {

    private RecyclerView devicesRecyclerView;
    private DeviceListAdapter mDeviceListAdapter;

    private FloatingActionButton fab;

    // private List<SocketDevice> devices = new ArrayList<>();

    private MainContract.Presenter mMainPresenter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        DataRepository repository = ((SocketControllerApp)getApplication()).getRepository();

        devicesRecyclerView = findViewById(R.id.devicesRecyclerView);
        devicesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mDeviceListAdapter = new DeviceListAdapter(this, new ArrayList<SocketDevice>());
        devicesRecyclerView.setAdapter(mDeviceListAdapter);

        Toolbar myToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(myToolbar);

        mMainPresenter = new MainPresenter(this, repository);

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddDeviceFragment addDeviceFragment = AddDeviceFragment.newInstance();

                fab.setVisibility(View.GONE);

                FragmentManager fragmentManager = getSupportFragmentManager();
                FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                fragmentTransaction.replace(R.id.addSocketFragmentContainer, addDeviceFragment);
                fragmentTransaction.addToBackStack(null);
                fragmentTransaction.commit();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();

        mMainPresenter.start();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mMainPresenter.stop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.main_options:
                Intent intent = new Intent(this, OptionsActivity.class);
                startActivity(intent);
                break;
            case R.id.main_refresh:
                mMainPresenter.refreshDeviceList();
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDeviceRowClicked(SocketDevice device) {
        mMainPresenter.openDeviceDetail(device);
    }

    @Override
    public void onSwitchPowerButtonClicked(SocketDevice device, boolean power) {
        mMainPresenter.setPowerForDevice(device, power);
    }

    @Override
    public void fragmentAttached() {
        mMainPresenter.stop();
    }

    @Override
    public void fragmentDetached(){
        fab.setVisibility(View.VISIBLE);

        mMainPresenter.start();
    }

    @Override
    public void previousFragment() {
        getSupportFragmentManager().popBackStack();
    }

    @Override
    public void setPresenter(MainContract.Presenter presenter) {
        mMainPresenter = presenter;
    }

    @Override
    public void showDevices(List<SocketDevice> devices) {
        mDeviceListAdapter.replaceData(devices);
    }

    @Override
    public void showDeviceDetail(int deviceId) {
        Intent intent = new Intent(this, SocketDeviceDetailActivity.class);
        intent.putExtra("DeviceId", deviceId);
        startActivity(intent);
    }

    @Override
    public void refreshDeviceList() {
        mDeviceListAdapter.notifyDataSetChanged();
    }

    @Override
    public Context getViewContext() {
        return getApplicationContext();
    }
}
