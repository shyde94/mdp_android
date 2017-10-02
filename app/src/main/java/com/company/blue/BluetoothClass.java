package com.company.blue;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Message;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

/**
 * Created by Shide on 12/9/17.
 */

//TODO Connectivity issues. Bluetooth should reconnect automatically if connection drops!
//TODO Bluetooth connection has issues after going to map configuration fragment. HMMMM

public class BluetoothClass {

    private String TAG = "BluetoothClass";

    public static BluetoothClass mInstance = null;
    private static final int REQUEST_ENABLE_BT = 0;
    private static final int REQUEST_DISCOVERABLE_BT = 0;

    private Thread connectionThread, listenThread, AcceptThread;
    private BluetoothSocket mmSocket, connectedSocket;
    private BluetoothDevice mmDevice;
    private BluetoothServerSocket btServerSocket;
    private OutputStream outputStream;
    private InputStream inStream;
    private final BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    private byte[] mmBuffer; // mmBuffer store for the stream
    private BroadcastReceiver mReceiver;
    private String Status;
    public static String incoming;




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
        if(mBluetoothAdapter.isEnabled()){
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

    public void manageConnectionRequests(){
        AcceptThread = new Thread(new Runnable() {
            @Override
            public void run() {
                Log.i(TAG,"In acceptThread");
                //UUID uuid = UUID.fromString("94f39d29-7d6d-437d-973b-fba39e49d4ee");
                 UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                 //UUID MY_UUID_INSECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
                try {
                    // MY_UUID is the app's UUID string, also used by the client code.
                    btServerSocket = mBluetoothAdapter.listenUsingRfcommWithServiceRecord("SD", uuid);

                } catch (IOException e) {
                    Log.i(TAG, "Socket's listen() method failed", e);
                }
                try {
                    Log.i(TAG,"In acceptThreadb");
                    listenForConnectionRequests(btServerSocket);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                listenForData();
            }
        });
        AcceptThread.start();
    }
    private void listenForConnectionRequests(BluetoothServerSocket btServerSocket) throws IOException {
        while(true){
            Log.i(TAG,"here");
            connectedSocket = btServerSocket.accept();
            Log.i(TAG, " listenForConnectionRequests");
            if (connectedSocket != null) {
                // A connection was accepted. Perform work associated with
                // the connection in a separate thread.
                Log.i(TAG, "Found connection. ");

                //Call handler here? perhaps.
                inStream = connectedSocket.getInputStream();
                outputStream = connectedSocket.getOutputStream();
                BluetoothDevice btDevice = connectedSocket.getRemoteDevice();
                String DeviceName = btDevice.getName();
                Message readMsg = Shared.mHandler.obtainMessage(
                        3, DeviceName);
                readMsg.sendToTarget();
                try {
                    Log.i(TAG, "Closing bluetoothServerSocket");
                    btServerSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                break;
            }

        }
        Log.i(TAG,"Exciting listenForConnectionRequests");
    }

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
                mmDevice = device;
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
                        //TODO the first instream always has some issues. Somehow.
                        numBytes = inStream.read(mmBuffer);
                        /*TODO Decide on some format with dhaslie? How to distinguish:
                        - Message indicating position of robot
                        - Message describing map
                        From here classify purpose of message, then set MessageConstant
                        do message processing here? hmmm okay based on status? then give message.what a number? okay.

                        */

                        String incomingMessage = new String(mmBuffer, 0, numBytes);
                        String msgToHandler = "";
                        int messageCode = 0;
                        // 0 - robot status
                        // 1 - robot position
                        // 2 - map info
                        //see if correct get
                        Log.d(TAG,incomingMessage);
                        JSONObject jsonObject = new JSONObject(incomingMessage);
                        JSONObject objMessage = jsonObject.getJSONObject("message");
                        String msgType = objMessage.getString("type");
                        if(msgType.equals("RobotStatus")){
                            msgToHandler = objMessage.getString("info");
                            messageCode = 0;
                        }
                        else if(msgType.equals("RobotPosition")){
                            JSONArray posDetails = objMessage.getJSONArray("info");
                            msgToHandler = posDetails.toString();
                            messageCode = 1;
                        }
                        else if(msgType.equals("MapInfo")){
                            msgToHandler = objMessage.getString("info");
                            messageCode = 2;
                        }
                        else{
                            msgToHandler = "Failed to Process message";
                        }
                        Message readMsg = Shared.mHandler.obtainMessage(messageCode, numBytes, -1, msgToHandler);
                        readMsg.sendToTarget();
                        incoming = incomingMessage;
                        Log.i(TAG, "Incoming: " + incoming);
                    } catch (IOException e) {
                        //If disconnected should reconnect back? yes. but how.
                        Log.d(TAG, "Input stream was disconnected", e);


                        break;
                    }catch (NullPointerException e) {
                        Log.d(TAG, "No input detected", e);
                        break;
                    } catch (JSONException e) {
                        e.printStackTrace();
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
