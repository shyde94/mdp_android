package com.company.blue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Shide on 12/9/17.
 */

public class BluetoothClass {

    private String TAG = "BluetoothClass";

    public static BluetoothClass mInstance = null;
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;

    private Thread connectionThread, listenThread;
    private BluetoothSocket mmSocket, connectedSocket;
    private BluetoothDevice mmDevice;
    private OutputStream outputStream;
    private InputStream inStream;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private byte[] mmBuffer; // mmBuffer store for the stream
    private BroadcastReceiver mReceiver;
    private String incoming;



    public static BluetoothClass getInstance() {
        if (mInstance == null) {
            mInstance = new BluetoothClass();
        }
        return mInstance;
    }

    public void setmReceiver(BroadcastReceiver mReceiver) {
        this.mReceiver = mReceiver;
    }

    public Thread getConnectionThread() {
        return connectionThread;
    }

    public void setConnectionThread(Thread connectionThread) {
        this.connectionThread = connectionThread;
    }

    public Thread getListenThread() {
        return listenThread;
    }

    public void setListenThread(Thread listenThread) {
        this.listenThread = listenThread;
    }

    public BluetoothSocket getMmSocket() {
        return mmSocket;
    }

    public void setMmSocket(BluetoothSocket mmSocket) {
        this.mmSocket = mmSocket;
    }

    public BluetoothSocket getConnectedSocket() {
        return connectedSocket;
    }

    public void setConnectedSocket(BluetoothSocket connectedSocket) {
        this.connectedSocket = connectedSocket;
    }

    public BluetoothDevice getMmDevice() {
        return mmDevice;
    }

    public void setMmDevice(BluetoothDevice mmDevice) {
        this.mmDevice = mmDevice;
    }

    public OutputStream getOutputStream() {
        return outputStream;
    }

    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public InputStream getInStream() {
        return inStream;
    }

    public void setInStream(InputStream inStream) {
        this.inStream = inStream;
    }

    public BluetoothAdapter getmBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    public byte[] getMmBuffer() {
        return mmBuffer;
    }

    public void setMmBuffer(byte[] mmBuffer) {
        this.mmBuffer = mmBuffer;
    }

    public void init(){
        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // the registerReceiver pairs up with startdiscovery i guess
        Shared.activity.registerReceiver(mReceiver, filter);
    }

    public boolean onBt() {
        if(mBluetoothAdapter==null){
            Log.i(TAG, "Device does not support bluetooth");
            return false;
        }
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            Shared.activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
            return true;
        }
        return false;
    }
    public boolean makeDiscoverable(){
        if (!mBluetoothAdapter.isDiscovering()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            Shared.activity.startActivityForResult(enableBtIntent, REQUEST_DISCOVERABLE_BT);
            Log.i(TAG, "Making discoverable");
            return true;
        }
        return false;
    }

    public boolean offBt(){
        if(mBluetoothAdapter != null){
            mBluetoothAdapter.disable();
            return true;
        }
        return false;

    }

    public boolean discover(){
        if(mBluetoothAdapter!= null){
            if (mBluetoothAdapter.isDiscovering()) {
                mBluetoothAdapter.cancelDiscovery();
            }
            mBluetoothAdapter.startDiscovery();
            return true;
        }
        return false;
    }

    public BroadcastReceiver getmReceiver() {
        return mReceiver;
    }

    /*private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.i(TAG,"Action:" + action);
            Log.i(TAG,"Here");
            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
                Log.i(TAG, "action finish");
                //bluetoothProgress.setVisibility(View.INVISIBLE);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG,"action started");
                //discovery starts, show progress dialog

            }
        }
    };*/

    public void ConnectToDevice (final BluetoothDevice device) {
        final boolean[] status = {false};
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
                    status[0] = true;
                } catch (IOException e) {
                    Log.d(TAG, "Cannot connect");
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
                        incoming = readMessage;


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

    public void write(String s) throws IOException {
        Log.d(TAG, "Last send: "+s);
        if(outputStream != null){
            outputStream.write((s+"|").getBytes());
        }
        else{
            Log.i(TAG, "Bluetooth not connected");
        }

    }
}
