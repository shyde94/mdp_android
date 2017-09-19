package com.company.blue;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.io.IOException;

/**
 * Created by Shide on 29/8/17.
 */

public class MapContainerFrag extends Fragment {
    final public String TAG = "MapContainerFragClass";
    private BoardView mBoardView;

    Button mForward, mReverse, mTurnLeft, mTurnRight, mManualUpdate, mExplore, mGo;
    ToggleButton mAutoUpdate, mStartLock;
    private ProgressBar mProgressBar;
    private TextView mStatus;
    private Handler mHandler;

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

        mProgressBar.setVisibility(View.INVISIBLE);

        //For now, assume that robot faces N at the start. Need to know where robot is facing!!!
        //Forward is just forward, but need to know how to shift current position. Need variable called direction.
        mForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //By right, if bluetooth isnt connected, map shouldn't display that robot has moved forward
                mBoardView.moveForward();
                try {
                    Shared.btController.write("MOVE FORWARD");
                    Log.i(TAG, "command: move forward");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mReverse.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mBoardView.moveBackward();
                try {
                    Shared.btController.write("MOVE BACKWARD");
                    Log.i(TAG, "command: move backward");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mTurnLeft.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mBoardView.moveLeftward();
                try {
                    Shared.btController.write("TURN LEFT");
                    Log.i(TAG, "command: turn left");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mTurnRight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mBoardView.moveRightward();
                try {
                    Shared.btController.write("TURN RIGHT");
                    Log.i(TAG, "command: turn right");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mExplore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Shared.btController.write("EXPLOREEEEE");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mGo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Shared.btController.write("GOOOOOOOO");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        mAutoUpdate.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //toggled on, call for auto update.
                    Log.i(TAG, "AutoUpdateChecked");
                    autoUpdateMechanism();
                }
                else {
                    //Cancel autoupdate? KIV.
                    mHandler.removeCallbacks(periodicUpdate);
                }
            }
        });

        mStartLock.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(b){
                    //toggled on, can set start position
                    Log.i(TAG, "Set start");
                    mBoardView.setStartLock(1);

                }
                else {
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
                    e.printStackTrace();
                    Toast.makeText(Shared.context, "Failed to update", Toast.LENGTH_LONG).show();
                    mProgressBar.setVisibility(View.INVISIBLE);
                }
            }
        });


        Log.i(TAG, "test");
        return view;
    }

    public void hideProgressBar(){
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
                    Shared.btController.write("Update" + count[0]);
                    mHandler.postDelayed(this,2000);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        mHandler.post(periodicUpdate);
    }
}
