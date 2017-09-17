package com.company.blue;

import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
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
    public static final String PREFS_NAME = "MyPrefsFile";


    //huangkai

    //Variables to control listview to select device to connect to
    private ListView nearbyDevicesList;
    private ArrayList<String> nearbyDevices = new ArrayList<String>();
    private ArrayAdapter<String> nearbyDevicesAdapter;




    private BluetoothClass btController = Shared.btController;

    private byte[] mmBuffer; // mmBuffer store for the stream
    private TextView mConnectionStatus;


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
        final Button persistentBtn = view.findViewById(R.id.persistent);
        final Button savePer_Btn = view.findViewById(R.id.save_per);
        final EditText persistentText = view.findViewById(R.id.persistent_send);
        final EditText sendText = view.findViewById(R.id.ck_send);
        mConnectionStatus = view.findViewById(R.id.connection_status);



        //huangkai
        bluetoothProgress =  view.findViewById(R.id.bluetooth_progress);
        bluetoothProgress.setVisibility(View.INVISIBLE);
        nearbyDevicesList =  view.findViewById(R.id.devices_list);


        final IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothDevice.ACTION_FOUND);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        // the registerReceiver pairs up with startdiscovery i guess
        Shared.activity.registerReceiver(mReceiver, filter);
        Shared.btController.setmReceiver(mReceiver);

        //turn on
        button1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if(btController.onBt()){
                    //Toast.makeText(getContext(),"Turning on bluetooth", Toast.LENGTH_LONG).show();
                    mConnectionStatus.setText("Bluetooth turned on");
                }
                else{
                    //Toast.makeText(getContext(), "Device does not support bluetooth", Toast.LENGTH_LONG).show();
                    mConnectionStatus.setText("Device does not support bluetooth");
                }
            }
        });

        //discoverable
        onDiscoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(btController.makeDiscoverable()){
                    mConnectionStatus.setText("Making your device discoverable");
                    Log.i(TAG, "Discovering");
                }else{
                    Log.i(TAG, "Not discovering");
                }
                /*if (!mBluetoothAdapter.isDiscovering()) {
                    //out.append("MAKING YOUR DEVICE DISCOVERABLE");
                    Toast.makeText(Shared.context, "MAKING YOUR DEVICE DISCOVERABLE",
                            Toast.LENGTH_LONG);

                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
                    startActivityForResult(enableBtIntent, REQUEST_DISCOVERABLE_BT);

                }*/
            }
        });
        //turn off
        offBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                if(btController.offBt()){
                    Log.i(TAG, "Turning bluetooth off");
                }
                else{
                    Log.i(TAG, "offBt() returned false");
                }

                /*mBluetoothAdapter.disable();
                //out.append("TURN_OFF BLUETOOTH");
                Toast.makeText(Shared.context, "TURNING_OFF BLUETOOTH", Toast.LENGTH_LONG);*/

            }
        });

        //turn off
        discoverBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                btController.discover();
                mConnectionStatus.setText("Discovering");
                /*if (mBluetoothAdapter.isDiscovering()) {
                    mBluetoothAdapter.cancelDiscovery();
                }

                mBluetoothAdapter.startDiscovery();*/

            }
        });

        persistentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                SharedPreferences settings = Shared.context.getSharedPreferences(PREFS_NAME, 0);
                String test = settings.getString("testing", "wrong");
                try {
                    Log.d(TAG, "in persistent button, just sent"+test);
                    Shared.btController.write(test);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });


        sendBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                String text = sendText.getText().toString();
                Log.d(TAG, "in send button just sent"+text);

                try {

                    btController.write(text);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    sendText.setText("");
                }   catch (NullPointerException e) {
                    e.printStackTrace();
                }
                // this method hide keyboard
                //removeFocus();
            }
        });

        savePer_Btn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){

                try {
                    String text = persistentText.getText().toString();
                    SharedPreferences settings = Shared.context.getSharedPreferences(PREFS_NAME, 0);

                    SharedPreferences.Editor editor = settings.edit();
                    editor.putString("testing", text);
                    editor.apply();
                }
                catch (NullPointerException e) {
                    e.printStackTrace();
                }
                try {
                    persistentText.setText("");
                }   catch (NullPointerException e) {
                    e.printStackTrace();
                }
                // this method hide keyboard
                //removeFocus();
            }
        });

        // explore button
        exploreBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View r){
                String command[] = {"IDK what to put"};
                try {
                    btController.write(Arrays.toString(command));
                } catch (IOException e) {
                    e.printStackTrace();
                }catch (NullPointerException e) {
                    e.printStackTrace();
                }
            }
        });

        nearbyDevicesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                btController.getmBluetoothAdapter().cancelDiscovery();
                String selected = (String) nearbyDevicesList.getItemAtPosition(position);
                String []deviceInfo = selected.split("\n");
                BluetoothDevice mBluetoothDevice = btController.getmBluetoothAdapter().getRemoteDevice(deviceInfo[1]);
                btController.ConnectToDevice(mBluetoothDevice);
                try{
                    if(btController.getConnectedSocket().isConnected()){
                        //Toast.makeText(Shared.context, "Connected to " + deviceInfo[1], Toast.LENGTH_LONG).show();
                        mConnectionStatus.setText("Connected to " + deviceInfo[1]);
                    }
                    else{
                        //Toast.makeText(Shared.context, "Unable to connect to device", Toast.LENGTH_LONG).show();
                        mConnectionStatus.setText("Unable to connect to device");
                    }
                }catch(NullPointerException e){
                    e.printStackTrace();
                    //Toast.makeText(Shared.context, "Connection error", Toast.LENGTH_LONG).show();
                    mConnectionStatus.setText("Connection error, try again");
                }



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
            Log.i(TAG,"Action:" + action);
            Log.i(TAG,"Here");

            if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                //discovery finishes, dismiss progress dialog
                Log.i(TAG, "action finish");
                bluetoothProgress.setVisibility(View.INVISIBLE);
            }
            else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action)) {
                Log.i(TAG,"action started");
                //discovery starts, show progress dialog
                nearbyDevices.clear();
                nearbyDevicesList.setAdapter(null);
                bluetoothProgress.setVisibility(View.VISIBLE);
            }
            else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                Log.i(TAG,"action found");
                //bluetooth device found
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                Log.i(TAG, "device:" + device.getName());
                nearbyDevices.add(device.getName() + "\n" + device.getAddress());
                nearbyDevicesAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, nearbyDevices);
                nearbyDevicesList.setAdapter(nearbyDevicesAdapter);
            }else{
                Log.i(TAG,"Action:"+action);
            }
        }
    };

}
