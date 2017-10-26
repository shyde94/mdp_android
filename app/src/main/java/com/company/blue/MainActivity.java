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
                    Log.i(TAG,"message: " + message);
                    switch(msg.what){
                        //Case 0 - robot status update
                        case 0:
                            MCFrag.setStatus(message);
                            break;
                        //Case 1 - robot position
                        case 1:
                            //split string;
                            int direction = 0;
                            String[] strContents = message.split(",");
                            int x = Integer.parseInt(strContents[1]);
                            int y = Integer.parseInt(strContents[0]);
                            String str = strContents[2];
                            if(str.contains("NORTH"))direction = 0;
                            else if(str.contains("EAST")) direction =3;
                            else if(str.contains("SOUTH")) direction =2;
                            else if(str.contains("WEST")) direction =1;

                            MCFrag.getmBoardView().getCurPos().setxCoord(x);
                            MCFrag.getmBoardView().getCurPos().setyCoord(y);
                            MCFrag.getmBoardView().setDirection(direction);
                            MCFrag.getmBoardView().refreshMap();

                            break;
                        //Case 2 - map info
                        case 2:
                            Log.i(TAG,"Message: " + message);
                            String[] tempArray = message.split(",");
                            MCFrag.getmBoardView().setExploredOrNot(tempArray[0]);
                            MCFrag.getmBoardView().setObstacleOrNot(tempArray[1]);
                            MCFrag.getmBoardView().refreshMap();
                            MCFrag.hideProgressBar();
                            MCFrag.printMapDescriptorText("Obstacle: " + tempArray[1]);
                            MCFrag.printMapDescriptorText2("Returned: " + BluetoothClass.BinTohex(tempArray[0]) + BluetoothClass.BinTohex(tempArray[1]));
                            Log.i(TAG,"Refreshing map");

                            break;
                    }
                }catch(ClassCastException e){
                    e.printStackTrace();
                }
            }
            if(Frag instanceof BluetoothFrag){
                    BluetoothFrag btFrag = (BluetoothFrag) Frag;
                switch(msg.what){
                    case 0:
                        message =(String)msg.obj;
                        btFrag.setStatus(message);
                        break;
                    case 1:
                        message =(String)msg.obj;
                        btFrag.setStatus(message);
                    default:
                        Log.i(TAG,"Message error");
                }


            }

        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"On destroy");
        Shared.activity.unregisterReceiver(Shared.btController.getmReceiver());
    }
}
