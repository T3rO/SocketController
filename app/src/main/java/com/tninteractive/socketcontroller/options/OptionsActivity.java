package com.tninteractive.socketcontroller.options;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import com.tninteractive.socketcontroller.R;

public class OptionsActivity extends AppCompatActivity implements OptionsContract.View {

    private OptionsContract.Presenter mPresenter;

    private Switch autoRefreshSwitch;
    private TextView refreshRateTextView;
    private Spinner refreshRateSpinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        autoRefreshSwitch = findViewById(R.id.optionsAutoRefreshSwitch);
        refreshRateTextView = findViewById(R.id.optionsRefreshRateTextView);
        refreshRateSpinner = findViewById(R.id.optionsRefreshRateSpinner);


        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, OptionsPresenter.AUTO_REFRESH_RATES);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        refreshRateSpinner.setAdapter(adapter);

        refreshRateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mPresenter.setAutoRefreshRate(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        Toolbar myToolbar = findViewById(R.id.options_toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setTitle("Options");

        mPresenter = new OptionsPresenter(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        mPresenter.start();
    }

    public void onClickAutoRefreshSwitch(View v){
        mPresenter.setAutoRefresh(autoRefreshSwitch.isChecked());
    }

    @Override
    public void showRefreshOptions(boolean autoRefresh, int refreshRateId) {
        showAutoRefresh(autoRefresh);
        showAutoRefreshRate(refreshRateId);
    }

    @Override
    public void showAutoRefresh(boolean autoRefresh) {
        if(autoRefreshSwitch.isChecked() != autoRefresh){
            autoRefreshSwitch.setChecked(autoRefresh);
        }

        if(autoRefresh){
            refreshRateTextView.setVisibility(View.VISIBLE);
            refreshRateSpinner.setVisibility(View.VISIBLE);
        }else{
            refreshRateTextView.setVisibility(View.GONE);
            refreshRateSpinner.setVisibility(View.GONE);
        }
    }

    @Override
    public void showAutoRefreshRate(int refreshRateId) {
        if(refreshRateSpinner.getSelectedItemPosition() != refreshRateId){
            refreshRateSpinner.setSelection(refreshRateId);
        }
    }

    @Override
    public void setPresenter(OptionsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public Context getViewContext() {
        return getApplicationContext();
    }
}
