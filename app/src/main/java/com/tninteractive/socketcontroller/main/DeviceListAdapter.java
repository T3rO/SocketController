package com.tninteractive.socketcontroller.main;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.tninteractive.socketcontroller.R;
import com.tninteractive.socketcontroller.data.SocketDevice;

import java.util.List;

/**
 * Created by trist on 12/24/19.
 */

public class DeviceListAdapter extends RecyclerView.Adapter<DeviceListAdapter.DevicesViewHolder> {

    public interface DevicesRecyclerViewListener{
        void onDeviceRowClicked(SocketDevice device);
        void onSwitchPowerButtonClicked(SocketDevice device, boolean power);
    }

    private DevicesRecyclerViewListener mListener;

    private List<SocketDevice> mDevices;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public class DevicesViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView nameTextView;
        public TextView ipTextView;
        public TextView timerTextView;
        public TextView connectionStatusTextView;
        public Switch powerSwitch;
        public ViewGroup container;


        public DevicesViewHolder(View v) {
            super(v);

            nameTextView = v.findViewById(R.id.deviceRowDeviceName);
            ipTextView = v.findViewById(R.id.deviceRowDeviceIp);
            timerTextView = v.findViewById(R.id.deviceRowTimerTextView);
            connectionStatusTextView = v.findViewById(R.id.deviceRowConnectionStatusTextView);
            powerSwitch = v.findViewById(R.id.deviceRowPowerSwitch);
            container = v.findViewById(R.id.deviceRowRoot);
        }

        @Override
        public void onClick(View view) {
            SocketDevice device = mDevices.get(this.getAdapterPosition());
            mListener.onDeviceRowClicked(device);
        }
    }

    public DeviceListAdapter(DevicesRecyclerViewListener listener, List<SocketDevice> devices) {
        mListener = listener;
        mDevices = devices;
    }

    public void replaceData(List<SocketDevice> devices){
        mDevices = devices;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public DevicesViewHolder onCreateViewHolder(ViewGroup parent,
                                                int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_device, parent, false);

        DevicesViewHolder vh = new DevicesViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(final DevicesViewHolder holder, final int position) {
        holder.nameTextView.setText(mDevices.get(position).getName());
        holder.ipTextView.setText(mDevices.get(position).getIp());
        switch (mDevices.get(position).getConnectionStatus()){
            case SocketDevice.CONNECTION_STATUS_CONNECTING:
                holder.connectionStatusTextView.setText(R.string.device_row_connection_status_connecting);
                holder.timerTextView.setVisibility(View.GONE);
                holder.powerSwitch.setVisibility(View.GONE);
                break;
            case SocketDevice.CONNECTION_STATUS_CONNECTED:
                holder.connectionStatusTextView.setText("");

                if(mDevices.get(position).getOneTimeTimer().isArmedOnDevice()){
                    holder.timerTextView.setVisibility(View.VISIBLE);
                    holder.timerTextView.setText(mDevices.get(position).getOneTimeTimer().getShortInfoText());
                }else{
                    holder.timerTextView.setVisibility(View.GONE);
                }

                holder.powerSwitch.setVisibility(View.VISIBLE);
                holder.powerSwitch.setChecked(mDevices.get(position).isPowerOn());
                break;
            case SocketDevice.CONNECTION_STATUS_NO_CONNECTION:
                holder.connectionStatusTextView.setText(R.string.device_row_connection_status_no_connection);
                holder.timerTextView.setVisibility(View.GONE);
                holder.powerSwitch.setVisibility(View.GONE);
                break;
        }
        holder.powerSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onSwitchPowerButtonClicked(mDevices.get(position), holder.powerSwitch.isChecked());
            }
        });
        holder.container.setOnClickListener(holder);
    }

    @Override
    public int getItemCount() {
        return mDevices.size();
    }
}
