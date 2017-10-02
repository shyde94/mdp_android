package com.company.blue;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
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
            if(Frag instanceof MapContainerFrag){
                try{
                    MapContainerFrag MCFrag =  (MapContainerFrag) Frag;
                    message =(String)msg.obj;
                    switch(msg.what){
                        //Case 0 - robot status update
                        case 0:
                            MCFrag.setStatus(message);
                            break;
                        //Case 1 - robot position
                        case 1:
                            //split string;
                            String[] strContents = message.split(",");
                            int x = Integer.parseInt(strContents[0]);
                            int y = Integer.parseInt(strContents[1]);
                            int direction = Integer.parseInt(strContents[2]);
                            MCFrag.getmBoardView().getCurPos().setxCoord(x);
                            MCFrag.getmBoardView().getCurPos().setyCoord(y);
                            MCFrag.getmBoardView().setDirection(direction);
                            MCFrag.getmBoardView().refreshMap();

                            break;
                        //Case 2 - map info
                        case 2:
                            Log.i(TAG,"Message: " + message);
                            MCFrag.getmBoardView().setRpiData(message);
                            MCFrag.getmBoardView().refreshMap();
                            MCFrag.hideProgressBar();
                            Log.i(TAG,"Refreshing map");

                            break;
                    }
                }catch(ClassCastException e){
                    e.printStackTrace();
                }
            }
            if(Frag instanceof BluetoothFrag){
                BluetoothFrag btFrag = (BluetoothFrag) Frag;
            }

        }
    };
}
