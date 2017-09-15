package com.company.blue;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ToggleButton;

import java.io.IOException;

/**
 * Created by Shide on 29/8/17.
 */

public class MapContainerFrag extends Fragment {
    final public String TAG = "MapContainerFragClass";
    private BoardView mBoardView;
    Button mForward, mReverse, mTurnLeft, mTurnRight;
    ToggleButton mManualUpdate, mAutoUpdate;

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


        //For now, assume that robot faces N at the start. Need to know where robot is facing!!!
        //Forward is just forward, but need to know how to shift current position. Need variable called direction.
        mForward.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

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
            }
        });

        mTurnRight.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                mBoardView.moveRightward();
            }
        });


        Log.i(TAG, "test");
        return view;
    }
}
