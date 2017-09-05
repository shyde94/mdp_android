package com.company.blue;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

/**
 * Created by Shide on 29/8/17.
 */

/*
* This contains all the squares. Each square is an object on its own that has its own xy coords and status.
* Contain bluetooth object as well? to read in bit stream to assign status of each grid square? Hmm...
* */
public class BoardView extends LinearLayout {

    final public String TAG = "BoardViewClass";
    final private int numRows = 20;
    final private int numCol = 15;

    private LayoutParams mRowLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    private LayoutParams mTileLayoutParams;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mSize;

    public BoardView(Context context) {
        this(context, null);
    }
    public BoardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        int margin = getResources().getDimensionPixelSize(R.dimen.margine_top);
        int padding = getResources().getDimensionPixelSize(R.dimen.board_padding);
        mScreenHeight = getResources().getDisplayMetrics().heightPixels - margin - padding*2;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels - padding*2;

    }

    public static BoardView fromXml(Context context, ViewGroup parent){
        Log.d("BoardView","fromXml");
        return (BoardView) LayoutInflater.from(context).inflate(R.layout.board_view, parent, false);
    }

    public void setBoard(){
        int singleMargin = getResources().getDimensionPixelSize(R.dimen.card_margin);
        float density = getResources().getDisplayMetrics().density;
        singleMargin = Math.max((int) (1 * density), (int) (singleMargin - 100 * 2 * density));
        int sumMargin = 0;
        for (int row = 0; row < numRows; row++) {
            sumMargin += singleMargin * 2;
        }
        //Programmatically calculate tile size based on screen size.
        int tilesHeight = (mScreenHeight - sumMargin) / numRows;
        int tilesWidth = (mScreenWidth - sumMargin) / numCol;
        mSize = Math.min(tilesHeight, tilesWidth);

        //set layoutparams of SquareGrid
        mTileLayoutParams = new LayoutParams(mSize, mSize);
        buildBoard();
    }

    private void buildBoard(){
        for(int row=0; row<numRows; row++){
            Log.i(TAG,"Adding row");
            addBoardRow(row);
        }
    }

    private void addBoardRow(int row) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);

        for(int i=0;i<numCol;i++){
            Log.i(TAG, "Adding column");
            GridPoint point = new GridPoint(i,row,0);
            addSquareView(linearLayout,point);
        }

        addView(linearLayout, 0);
        linearLayout.setClipChildren(false);

    }

    private void addSquareView(ViewGroup parent, GridPoint point) {
        Log.i(TAG, "Adding square view");
        final SquareView sV = SquareView.fromXml(getContext(), parent);
        sV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getContext(), sV.getPoint().getxCoord()+" "+sV.getPoint().getyCoord(), Toast.LENGTH_SHORT).show();
            }
        });
        sV.setLayoutParams(mTileLayoutParams);
        sV.setPoint(point);
        parent.addView(sV);
        parent.setClipChildren(false);
    }

}
