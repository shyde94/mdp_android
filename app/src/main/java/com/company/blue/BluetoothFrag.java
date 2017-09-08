package com.company.blue;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.UUID;


public class BluetoothFrag extends Fragment {
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;
    private ProgressBar bluetoothProgress;
    private static final String TAG = "Huangkai";


    //huangkai
    private ListView nearbyDevicesList;
    private ArrayList<String> nearbyDevices = new ArrayList<String>();
    private ArrayAdapter<String> nearbyDevicesAdapter;
    IntentFilter filter;
    private Thread connectionThread, listenThread;
    private BluetoothSocket mmSocket, connectedSocket;
    private BluetoothDevice mmDevice;
    private OutputStream outputStream;
    private InputStream inStream;
    final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private byte[] mmBuffer; // mmBuffer store for the stream

    @org.jetbrains.annotations.Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View view = inflater.inflate(R.layout.bluetooth_frag, container, false);

        final TextView out= view.findViewById(R.id.out);
        final Button button1 = view.findViewById(R.id.button1);
        final Button onDiscoverBtn = view.findViewById(R.id.button2);
        final Button offBtn = view.findViewById(R.id.button3);
        final Button discoverBtn = view.findViewById(R.id.button4);
        final Button sendBtn = view.findViewById(R.id.send);
        final Button exploreBtn = view.findViewById(R.id.explore);

        //huangkai
        bluetoothProgress =  view.findViewById(R.id.bluetooth_progress);
        bluetoothProgress.setVisibility(View.INVISIBLE);
        nearbyDevicesList =  view.findViewById(R.id.devices_list);


        // Register for broadcasts when a device is discovered.
        //final IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        //registerReceiver(mReceiver, filter);


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
        onDiscoverBtn.setOnClickListener(new View.OnClickListener() {
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
        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                mBluetoothAdapter.disable();
                //out.append("TURN_OFF BLUETOOTH");
                Toast.makeText(Shared.context, "TURNING_OFF BLUETOOTH", Toast.LENGTH_LONG);

            }
        });

        //turn off
        discoverBtn.setOnClickListener(new View.OnClickListener() {
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


        sendBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                EditText transmitEditText = (EditText)v.findViewById(R.id.ck_send);
                try {
                    write(transmitEditText.getText().toString());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                transmitEditText.setText("");
                // this method hide keyboard
                //removeFocus();
            }
        });

        // explore button
        exploreBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View r){
                String command[] = {"IDK what to put"};
                try {
                    write(Arrays.toString(command));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        nearbyDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                String selected = (String) nearbyDevicesList.getItemAtPosition(position);
                String []deviceInfo = selected.split("\n");
                Log.d(TAG,"hello huangkai2" + deviceInfo[1]);
                BluetoothDevice mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(deviceInfo[1]);
                ConnectToDevice(mBluetoothDevice);



                //Intent intent = new Intent(MainActivity.this, RobotActivity.class);
                //intent.putExtra("deviceAdd", deviceInfo[1]);
                //finish();
                //startActivity(intent);
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

    // Selecting a connection
    private void ConnectToDevice (final BluetoothDevice device) {
        connectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
                // Cancel discovery because it otherwise slows down the connection.
                try {
                    mmSocket = device.createRfcommSocketToServiceRecord(uuid);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                connectedSocket = mmSocket;
                mBluetoothAdapter.cancelDiscovery();
                try {
                    connectedSocket.connect();
                    outputStream = connectedSocket.getOutputStream();
                    inStream = connectedSocket.getInputStream();
                    Log.d(TAG, "connected");
                } catch (IOException e) {
                    Log.d(TAG, "bad");
                    e.printStackTrace();
                }

                //Being listening for data

                listenForData();

            }

            // Closes the client socket and causes the thread to finish.
            public void cancel() {
                try {
                    mmSocket.close();
                } catch (IOException e) {
                    Log.e(TAG, "Could not close the client socket", e);
                }
            }
        });
        connectionThread.start();
    }

    public void listenForData(){

        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                mmBuffer = new byte[1024];
                int numBytes; // bytes returned from read()
                // hk - handler and message constant are needed to do stuffs
                while (true) {
                    try {
                        // Read from the InputStream.
                        //while(!(inStream.available()>0)){};

                        Log.d(TAG,"prepare to receive");
                        numBytes = inStream.read(mmBuffer);

                        final String readMessage = new String(mmBuffer, 0, numBytes);
                        //see if correct get
                        Log.d(TAG,readMessage);
                        //readMsg.sendToTarget();

                        //*
                        //=========== a lot of other logic over here such as runOnUIThread
                        //*

                    } catch (IOException e) {
                        Log.d(TAG, "Input stream was disconnected", e);
                        break;
                    }catch (NullPointerException e) {
                        Log.d(TAG, "No input detected", e);
                        break;
                    }


                }
            }

        });
        listenThread.start();
    }


    // write method to outputstream
    public void write(String s) throws IOException {
        Log.d(TAG, "Last send: "+s);
        outputStream.write((s+"|").getBytes());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Shared.activity.unregisterReceiver(mReceiver);
    }


}
