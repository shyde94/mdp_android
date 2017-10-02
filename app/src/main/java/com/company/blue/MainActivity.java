package com.company.blue;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;


public class MainActivity extends AppCompatActivity {
    //public ScreenController screenController = new ScreenController();
    //public BluetoothClass btController = new BluetoothClass();
    private static final String TAG = "MainActivityClass";
    public Activity activity = this;






    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Shared.activity = MainActivity.this;
        Shared.context = getApplicationContext();
        Shared.SC.openScreen(ScreenController.Screen.MENU);  //what this
        Shared.mHandler = mHandler;




    }
    /**
     * Method used when the Back button on phone is pressed
     */


    @Override
    public void onBackPressed() {

        if (ScreenController.getInstance().onBack()) {
            super.onBackPressed();
        }
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String message = "";
            FragmentManager fManager = Shared.activity.getFragmentManager();
            Fragment Frag = fManager.findFragmentById(R.id.fragment_container);
            switch(msg.what){
                case 0:
                    message =(String)msg.obj;
                    Log.i(TAG,"Message: " + message);

                    Log.i(TAG,"Refreshing map");
                    if(Frag instanceof MapContainerFrag){
                        try{
                            MapContainerFrag MCFrag =  (MapContainerFrag) Frag;
                            MCFrag.getmBoardView().setRpiData(message);
                            MCFrag.getmBoardView().refreshMap();
                            MCFrag.hideProgressBar();
                        }catch(ClassCastException e){
                            e.printStackTrace();
                        }
                    }
                    if(Frag instanceof BluetoothFrag){
                        BluetoothFrag btFrag = (BluetoothFrag) Frag;
                    }




            }
        }
    };
}
