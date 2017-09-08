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

/**
 * Created by Shide on 29/8/17.
 */

public class MapContainerFrag extends Fragment {
    final public String TAG = "MapContainerFragClass";
    private BoardView mBoardView;
    Button mForward, mReverse, mTurnLeft, mTurnRight;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        Log.i(TAG, "Creating mapcontainer frag");
        ViewGroup view = (ViewGroup) inflater.inflate(R.layout.map_container_frag, container, false);

        mBoardView = BoardView.fromXml(getActivity().getApplicationContext(), view);
        FrameLayout frameLayout = view.findViewById(R.id.map_container);
        frameLayout.addView(mBoardView);
        frameLayout.setClipChildren(false);
        mBoardView.setBoard();

        mForward = view.findViewById(R.id.forward);
        mReverse = view.findViewById(R.id.reverse);
        mTurnLeft = view.findViewById(R.id.turn_left);
        mTurnRight = view.findViewById(R.id.turn_right);
        return view;
    }
}
