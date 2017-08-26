package com.company.blue;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class BluetoothFrag extends Fragment {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;
    private ProgressBar bluetoothProgress;

    //huangkai
    private ListView nearbyDevicesList;
    private ArrayList<String> nearbyDevices = new ArrayList<String>();
    private ArrayAdapter<String> nearbyDevicesAdapter;
    IntentFilter filter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.bluetooth_frag, container, false);

        final TextView out= view.findViewById(R.id.out);
        final Button button1 = view.findViewById(R.id.button1);
        final Button button2 = view.findViewById(R.id.button2);
        final Button button3 = view.findViewById(R.id.button3);
        final Button button4 = view.findViewById(R.id.button4);

        //huangkai
        bluetoothProgress =  view.findViewById(R.id.bluetooth_progress);
        bluetoothProgress.setVisibility(View.INVISIBLE);
        nearbyDevicesList =  view.findViewById(R.id.devices_list);


        // Register for broadcasts when a device is discovered.
        //final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(mReceiver, filter);

        final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (mBluetoothAdapter == null) {
            out.append("device not supported");
        }
        //turn on
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
            }
        });
        //discoverable
        button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (!mBluetoothAdapter.isDiscovering()) {
                    //out.append("MAKING YOUR DEVICE DISCOVERABLE");
                    Toast.makeText(Shared.context, "MAKING YOUR DEVICE DISCOVERABLE",
                            Toast.LENGTH_LONG);

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(enableBtIntent, REQUEST_DISCOVERABLE_BT);

                }
            }
        });
        //turn off
        button3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mBluetoothAdapter.disable();
                //out.append("TURN_OFF BLUETOOTH");
                Toast.makeText(Shared.context, "TURNING_OFF BLUETOOTH", Toast.LENGTH_LONG);

            }
        });

        //turn off
        button4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }
                final IntentFilter filter = new IntentFilter();
                filter.addAction(BluetoothDevice.ACTION_FOUND);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
                filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                // the registerReceiver pairs up with startdiscovery i guess
                Shared.activity.registerReceiver(mReceiver, filter);

                mBluetoothAdapter.startDiscovery();

            }
        });
        return view;
    }
    // Create a BroadcastReceiver for ACTION_FOUND. This is just a container
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        // this method does something
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
                bluetoothProgress.setVisibility(View.INVISIBLE);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                //discovery starts, show progress dialog
                nearbyDevices.clear();
                nearbyDevicesList.setAdapter(null);
                bluetoothProgress.setVisibility(View.VISIBLE);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                nearbyDevices.add(device.getName() + "\n" + device.getAddress());
                nearbyDevicesAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, nearbyDevices);
                nearbyDevicesList.setAdapter(nearbyDevicesAdapter);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        Shared.activity.unregisterReceiver(mReceiver);
    }
}
