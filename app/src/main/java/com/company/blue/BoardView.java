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

import java.util.HashMap;

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


    //String length should be 300
    private String Temp = "110000001111011010101111100101010101011000010101001000100100110010101111101101001111000111111101111111111111010101111001100110000000010001001010111100100011100100100110100100111100011011110101010011011100011100001111011010110110001100010001110111101100010111011110011010111101101100000011111001000101";

    //Start, End, Current, Waypoint.
    //Algo to decide where robot is. Take position as center of 9 squares.
    //Based on position, identify which squares to alter? Okay.
    private int wayPointSet = 0;
    private GridPoint wayPoint;

    private GridPoint curPos = new GridPoint(1,1,0);

    private HashMap<GridPoint, SquareView> gpMap = new HashMap<>();
    private GridPoint[][] gpArray = new GridPoint[numRows][numCol];

    public GridPoint getCurPos() {
        return curPos;
    }

    public void setCurPos(GridPoint curPos) {
        this.curPos = curPos;
    }



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
        mScreenWidth = getResources().getDisplayMetrics().widthPixels - padding*2 - (int) (Shared.context.getResources().getDisplayMetrics().density * 20);

        Log.i(TAG, "mScreenHeight: " + mScreenHeight);
        Log.i(TAG, "mScreenWidth: " + mScreenWidth);

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
        Log.i(TAG,"sumMargin: " + sumMargin);

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

        String[] DataStringArray = segmentString(Temp, numRows, numCol);

        //This gives grid points their status.
        for(int i=0;i<numCol;i++){
            Log.i(TAG, "Adding column - " + i);
            GridPoint point = new GridPoint(i,row,0);

            //extract status from string using row and column.
            char x = DataStringArray[row].charAt(i);

            Log.i(TAG, "Setting status for coord: ("+i + ", " +row+")," + "status: " + x);
            point.setStatus(x);
            addSquareView(linearLayout,point);
        }

        addView(linearLayout, 0);
        linearLayout.setClipChildren(false);

    }

    //Used to initialise board. Need to add ways to update board without re-drawing entire board. lol.
    private void addSquareView(ViewGroup parent, GridPoint point) {
        Log.i(TAG, "Adding square view");
        final SquareView sV = SquareView.fromXml(getContext(), parent, point);

        gpArray[point.getyCoord()][point.getxCoord()] = point;
        gpMap.put(point, sV);

        sV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                //When square box is clicked. To set way point! How to refresh from here?? Hmmm....
                Toast.makeText(getContext(), sV.getPoint().getxCoord()+" "+sV.getPoint().getyCoord(), Toast.LENGTH_SHORT).show();
                //displayCurrentPosition(curPos);
            }
        });
        updateImage(sV);
        sV.setLayoutParams(mTileLayoutParams);
        parent.addView(sV);
        parent.setClipChildren(false);
    }



    //Returns array of strings, status at coord (x,y) is indicated by array[x].charAt(y);
    private String[] segmentString(String x, int rows, int col){

        //if string given somehow has less than 300 digits. discuss with dhaslie.
        if(x.length()<(rows*col)){
            String fill = "";
            for(int i=0;i<(rows*col)-x.length();i++){
                fill += "0";
            }
            x += fill;
        }
        String[] x_array = new String[rows]; //array of strings of size 2
        int start_pos = 0;
        int end_pos = start_pos + col;
        for(int i=0;i<rows;i++){
            //System.out.println("start pos: " + start_pos);
            String a = x.substring(start_pos,end_pos);
            //System.out.println("insert: "+a);
            x_array[i] = a;
            start_pos += col;
            end_pos = start_pos + col;
        }
        return x_array;

    }




    /*
        How to identify boxes that car occupies? eg car is at (x,y), boxes occupied:
        (x+1,y), (x-1,y), (x,y+1), (x,y-1), (x+1,y+1), (x+1,y-1), (x-1, y+1), (x-1,y-1), (x,y)
        */
    public void displayCurrentPosition(){
        int x = curPos.getxCoord();
        int y = curPos.getyCoord();
        GridPoint[] gpArray2 = new GridPoint[9];
        try{
            gpArray2[0] = gpArray[y][x];
            gpArray2[1] = gpArray[y][x+1];
            gpArray2[2] = gpArray[y][x-1];
            gpArray2[3] = gpArray[y+1][x];
            gpArray2[4] = gpArray[y-1][x];
            gpArray2[5] = gpArray[y+1][x+1];
            gpArray2[6] = gpArray[y-1][x+1];
            gpArray2[7]= gpArray[y-1][x-1];
            gpArray2[8] = gpArray[y+1][x-1];
        }catch (ArrayIndexOutOfBoundsException e){
            e.printStackTrace();
        }
        for(GridPoint tempGp: gpArray2){
            SquareView sV = gpMap.get(tempGp);
            if(sV!=null){
                Log.i(TAG,"displaying cur position");
                sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.blue_box,null));
            }
        }

    }

    public void moveForward(){
        //assume robot is facing north now, move foward 1 step, y := y+1
        //Should contain code to send bluetooth message to rpi to make robot move forward.
        int y = curPos.getyCoord();
        curPos.setyCoord(y+1);
        //Cannot just set board, must remove all the views first. hmmmm
        refreshMap();



    }

    //Use hash map to refresh map!
    public void refreshMap(){
        //Should contain code to get updated map from rpi and current position. change variable curPos in here!
        String[] stringArray = segmentString(Temp, numRows, numCol);
        for(int i=0;i<numRows;i++){
            for(int j=0;j<numCol;j++){
                GridPoint tempGp = gpArray[i][j];
                SquareView tempSv = gpMap.get(tempGp);
                tempSv.getPoint().setStatus(stringArray[i].charAt(j));
                updateImage(tempSv);

            }
        }
        displayCurrentPosition();
    }

    private void updateImage(SquareView sV){
        if(sV.getPoint().getStatus() == '0'){
            Log.i(TAG,"unexplored");
            sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.black_box,null));
        }
        else if(sV.getPoint().getStatus() == '1') {
            Log.i(TAG, "explored");
            sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.white_box,null));
        }
    }




}
