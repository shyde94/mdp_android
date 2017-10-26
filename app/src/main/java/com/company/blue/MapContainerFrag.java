package com.company.blue;

import android.app.Fragment;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

/**
 * Created by Shide on 29/8/17.
 */

public class MapContainerFrag extends Fragment implements SensorEventListener {
    final public String TAG = "MapContainerFragClass";
    private BoardView mBoardView;

    Button mForward, mReverse, mTurnLeft, mTurnRight, mManualUpdate, mExplore, mGo, mSendWayPoint;
    ToggleButton mAutoUpdate, mStartLock;
    private ProgressBar mProgressBar;
    private ToggleButton motionBtn;
    private TextView mStatus;
    private Handler mHandler;
    private long lastUpdate;
    //hk
    private GridPoint mGridPoint;

    EditText mMsgOut;
    Button mMsgSend;

    private TextView mMapDescriptor;
    private TextView mMapDescriptor2;

    public BoardView getmBoardView() {
        return mBoardView;
    }

    Runnable periodicUpdate;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "Creating mapcontainer frag");
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.map_container_frag, container, false);


        /*Create map board. So everytime i want to update the map board i need to recreate this board? okay. Sounds
        pretty inefficient.
        - but all i need to do is change the colour on the map dynamically right. Lets see if that can be done
        - Everytime i call for an update...I can identify the coordinates of the string that comes in. (x,y) coordinate is actually x_array[col_number].charAt(row_number)

        */
        mBoardView = BoardView.fromXml(getActivity().getApplicationContext(), view);
        FrameLayout frameLayout = view.findViewById(R.id.map_container);
        frameLayout.addView(mBoardView);
        frameLayout.setClipChildren(false);
        mBoardView.setBoard();
        mBoardView.displayCurrentPosition();

        mForward = view.findViewById(R.id.forward);
        mReverse = view.findViewById(R.id.reverse);
        mTurnLeft = view.findViewById(R.id.turn_left);
        mTurnRight = view.findViewById(R.id.turn_right);
        mExplore = view.findViewById(R.id.explore);
        mGo = view.findViewById(R.id.go);
        mStatus = view.findViewById(R.id.status);
        mManualUpdate = view.findViewById(R.id.manual_update);
        mAutoUpdate = view.findViewById(R.id.auto_update);
        mStartLock = view.findViewById(R.id.set_start_lock);
        mProgressBar = view.findViewById(R.id.update_progress_bar);
        motionBtn = view.findViewById(R.id.toggleButton2);
        mMapDescriptor = view.findViewById(R.id.map_descriptor);
        mMapDescriptor2 = view.findViewById(R.id.map_descriptor2);
        mSendWayPoint = view.findViewById(R.id.sendwaypoint);

        mProgressBar.setVisibility(View.INVISIBLE);
        lastUpdate = System.currentTimeMillis();


        mMsgOut = view.findViewById(R.id.msgOut);
        mMsgSend = view.findViewById(R.id.sendMsg);


        //////////// sensors
        Shared.sMgr = (SensorManager) Shared.activity.getSystemService(Shared.context.SENSOR_SERVICE);
        if (Shared.sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
            Shared.mAccelerometer = Shared.sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        } else {
            Log.d(TAG, "sorry no sensor");
        }

        motionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (motionBtn.isChecked()) {

                    Shared.sMgr.registerListener(MapContainerFrag.this, Shared.mAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    Log.d(TAG, "registered sensor listener");

                } else {
                    Shared.sMgr.unregisterListener(MapContainerFrag.this);
                }
            }
        });
        ////////hk

        mSendWayPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //By right, if bluetooth isnt connected, map shouldn't display that robot has moved forward
                try {
                    mGridPoint = mBoardView.getWayPoint();
                    Toast.makeText(Shared.context, mGridPoint.getxCoord()+" "+mGridPoint.getyCoord(), Toast.LENGTH_SHORT).show();
                    Shared.btController.write("SET WP," + mGridPoint.getxCoord() + ":"+ mGridPoint.getyCoord());

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(Shared.context, "Message cannot be sent. Waypoint not set  ", Toast.LENGTH_SHORT).show();
                }
                Log.i(TAG, "command: move forward");
            }
        });



        ///////////

        //For now, assume that robot faces N at the start. Need to know where robot is facing!!!
        //Forward is just forward, but need to know how to shift current position. Need variable called direction.
        mForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //By right, if bluetooth isnt connected, map shouldn't display that robot has moved forward
                try {
                    mBoardView.moveForward();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i(TAG, "command: move forward");
            }
        });

        mReverse.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mBoardView.moveBackward();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "command: move backward");
            }
        });

        mTurnLeft.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mBoardView.moveLeftward();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "command: turn left");
            }
        });

        mTurnRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    mBoardView.moveRightward();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                Log.i(TAG, "command: turn right");
            }
        });

        mExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Shared.btController.write("EXPLORE");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Shared.btController.write("FASTEST PATH");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //toggled on, call for auto update.
                    Log.i(TAG, "AutoUpdateChecked");
                    autoUpdateMechanism();
                } else {
                    //Cancel autoupdate? KIV.
                    mHandler.removeCallbacks(periodicUpdate);
                }
            }
        });

        mStartLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    //toggled on, can set start position
                    Log.i(TAG, "Set start");
                    mBoardView.setStartLock(1);
                } else {
                    mBoardView.setStartLock(0);
                }
            }
        });

        mManualUpdate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mProgressBar.setVisibility(View.VISIBLE);
                try {
                    Log.i(TAG, "Updating manually");
                    //Used Update as example.
                    //TODO:  Lias with dhaslie first.
                    Shared.btController.write("Update");
                } catch (IOException e) {
                    Log.i(TAG, "Update problems");
                    e.printStackTrace();
                    Toast.makeText(Shared.context, "Failed to update", Toast.LENGTH_LONG).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


        mMsgSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String msg = mMsgOut.getText().toString();
                try {
                    Shared.btController.write(msg);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Log.i(TAG, "test");
        return view;
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        Sensor mySensor = sensorEvent.sensor;

        if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];


            long actualTime = System.currentTimeMillis();
            if (actualTime - lastUpdate < 1000) {
                return;
            }
            Log.d(TAG, "X is: " + String.valueOf(x));
            Log.d(TAG, "Y is: " + String.valueOf(y));

            lastUpdate = actualTime;
            //device not tilt, start to sense for motions
            // if (x > (-2) && x < (2) && y > (-2) && y < (2)) {
            //   motionSensor = true;
            //}

            // upon every motion sensed, print statement and turn motion sensor off to repeat the process
            if (true) {
                // Left Right Movement
                if (Math.abs(x) > Math.abs(y)) {
                    // right motion
                    if (x < -2) {
                        Log.d(TAG, "You tilt the device right");
                        try {
                            mBoardView.moveRightward();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (x > 2) {
                        Log.d(TAG, "You tilt the device left");
                        try {
                            mBoardView.moveLeftward();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {
                    if (y < -2) {
                        Log.d(TAG, "You tilt the device down");
                        try {
                            mBoardView.moveBackward();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (y > 2) {

                        Log.d(TAG, "You tilt the device up");
                        try {
                            mBoardView.moveForward();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }


            // if ((curTime - lastUpdate) > 100) {
            //     long diffTime = (curTime - lastUpdate);
            //     lastUpdate = curTime;

            //     float speed = Math.abs(x + y + z - last_x - last_y - last_z)/ diffTime * 10000;

            //     if (speed > SHAKE_THRESHOLD) {

            //     }

            //     last_x = x;
            //     last_y = y;
            //     last_z = z;
            // }


        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }


    public void hideProgressBar() {
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void autoUpdateMechanism() {
        mHandler = new Handler();
        final int[] count = {0};
        periodicUpdate = new Runnable() {
            @Override
            public void run() {
                try {
                    Log.i(TAG, "In AutoUpdateThread");
                    count[0]++;
                    Shared.btController.write("Update");
                    //Shared.btController.write("Update" + count[0]);
                    mHandler.postDelayed(this, 2000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mHandler.post(periodicUpdate);
    }

    public void setStatus(String status) {
        mStatus.setText("Current Status: " + " " + status);
    }

    public void printMapDescriptorText(String s) {
        mMapDescriptor.setText(s);

    }

    public void printMapDescriptorText2(String s){
        mMapDescriptor2.setText(s);
    }

    String[] segmentString(String x, int rows, int col) {

        //if string given somehow has less than 300 digits. discuss with dhaslie.
        if (x.length() < (rows * col)) {
            String fill = "";
            for (int i = 0; i < (rows * col) - x.length(); i++) {
                fill += "0";
            }
            x += fill;
            Log.i(TAG, "string used:" + x);
        }
        String[] x_array = new String[rows]; //array of strings of size 2
        int start_pos = 0;
        int end_pos = start_pos + col;
        for (int i = 0; i < rows; i++) {
            //System.out.println("start pos: " + start_pos);
            String a = x.substring(start_pos, end_pos);
            //System.out.println("insert: "+a);
            x_array[i] = a;
            start_pos += col;
            end_pos = start_pos + col;
        }
        return x_array;

    }

}