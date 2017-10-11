package com.company.blue;

import android.content.Context;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.IOException;
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
    private int direction = 0;
    private int startLock = 0;

    private LayoutParams mRowLayoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
    private LayoutParams mTileLayoutParams;
    private int mScreenWidth;
    private int mScreenHeight;
    private int mSize;
    private GestureDetector.OnGestureListener gestureListener;

    //1 - explored/no obstacle, 0 - unexplored/obstacle
    //String length should be 300
    //String data used to display map coordinates.
    private String RpiData = "0";
    //Start, End, Current, Waypoint.
    //Algo to decide where robot is. Take position as center of 9 squares.
    //Based on position, identify which squares to alter? Okay.
    private int wayPointSet = 0;
    private GridPoint wayPoint;


    private int clickCount =0;
    //Need to include touch base function to enter robot start coordinates.
    private GridPoint curPos = new GridPoint(1,1,0); //Initial start position.
    private HashMap<GridPoint, SquareView> gpMap = new HashMap<>();
    private GridPoint[][] gpArray = new GridPoint[numRows][numCol];

    public GridPoint getCurPos() {
        return curPos;
    }

    public void setCurPos(GridPoint curPos) {
        this.curPos = curPos;
    }

    public String getRpiData() {
        return RpiData;
    }

    public void setRpiData(String rpiData) {
        RpiData = rpiData;
    }

    public int getStartLock() {
        return startLock;
    }

    public void setStartLock(int startLock) {
        this.startLock = startLock;
    }

    public BoardView(Context context) {
        this(context, null);
    }

    public int getDirection() {
        return direction;
    }

    public void setDirection(int direction) {
        this.direction = direction;
    }

    public BoardView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        setOrientation(LinearLayout.VERTICAL);
        setGravity(Gravity.CENTER);
        int margin = getResources().getDimensionPixelSize(R.dimen.margine_top);
        int padding = getResources().getDimensionPixelSize(R.dimen.board_padding);
        mScreenHeight = getResources().getDisplayMetrics().heightPixels - margin - padding*2;
        mScreenWidth = getResources().getDisplayMetrics().widthPixels - padding*2 - (int) (Shared.context.getResources().getDisplayMetrics().density * 20);

        //Log.i(TAG, "mScreenHeight: " + mScreenHeight);
        //Log.i(TAG, "mScreenWidth: " + mScreenWidth);

    }

    public static BoardView fromXml(Context context, ViewGroup parent){
        //Log.d("BoardView","fromXml");
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
        //Log.i(TAG,"sumMargin: " + sumMargin);

        //Programmatically calculate tile size based on screen size.
        int tilesHeight = (mScreenHeight - sumMargin) / numRows;
        int tilesWidth = (mScreenWidth - sumMargin) / numCol;
        mSize = Math.min(tilesHeight, tilesWidth);

        //set layoutparams of SquareGrid
        mTileLayoutParams = new LayoutParams(30, 30);
        buildBoard();
    }

    private void buildBoard(){
        for(int row=0; row<numRows; row++){
            //Log.i(TAG,"Adding row");
            addBoardRow(row);
        }
    }

    private void addBoardRow(int row) {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER);



        String[] DataStringArray = segmentString(RpiData, numRows, numCol);

        //This gives grid points their status.
        for(int i=0;i<numCol;i++){
            //Log.i(TAG, "Adding column - " + i);
            GridPoint point = new GridPoint(i,row,0);

            //extract status from string using row and column.
            char x = DataStringArray[row].charAt(i);

            //Log.i(TAG, "Setting status for coord: ("+i + ", " +row+")," + "status: " + x);
            point.setStatus(x);
            addSquareView(linearLayout,point);
        }

        //This line here controls where 0,0 starts from!
        addView(linearLayout, 0);
        linearLayout.setClipChildren(false);

    }

    //Used to initialise board. Need to add ways to update board without re-drawing entire board. lol.
    private void addSquareView(ViewGroup parent, GridPoint point) {
        //Log.i(TAG, "Adding square view");
        final SquareView sV = SquareView.fromXml(getContext(), parent, point);
        gpArray[point.getyCoord()][point.getxCoord()] = point;
        Log.i(TAG, "Inserting: gpArray["+point.getyCoord()+","+point.getxCoord()+"]"+" point: " +point.getxCoord()+"," + point.getyCoord() );
        gpMap.put(point, sV);

        sV.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                clickCount++;
                Handler mHandler = new Handler();
                mHandler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        if(clickCount==1){
                            //When square box is clicked. To set way point! How to refresh from here?? Hmmm....
                            Toast.makeText(getContext(), sV.getPoint().getxCoord()+" "+sV.getPoint().getyCoord(), Toast.LENGTH_SHORT).show();
                            Log.i(TAG, sV.getPoint().getxCoord() +", " + sV.getPoint().getyCoord());
                            //hk here send
                            Log.d(TAG,"hello here boardview waypoint");
                        }
                        if(clickCount==2 && startLock ==1){
                            Log.i(TAG, "DoubleClick");
                            Log.i(TAG, sV.getPoint().getxCoord() +", " + sV.getPoint().getyCoord());
                            //Set start point!
                            int x = sV.getPoint().getxCoord();
                            int y = sV.getPoint().getyCoord();
                            try {
                                Shared.btController.write("sp,"+x+","+y);
                                curPos.setxCoord(x);
                                curPos.setyCoord(y);
                                Toast.makeText(getContext(), "Start point set " + curPos.getxCoord() + ", " + curPos.getyCoord(), Toast.LENGTH_LONG).show();
                                refreshMap();
                            } catch (IOException e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Error sending message, start point not set", Toast.LENGTH_SHORT).show();
                            }


                            //Problem? Now when i move from that point A, coordinate at A gets changed to current position. Why is the
                            //gridpoint object being associated with another squareView object?

                        }
                        clickCount = 0;
                        Log.i(TAG,"Exit run");
                    }
                },500);

            }
        });


        //Set waypoint. Should check if box has obstacle or not.

        sV.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                Log.i(TAG, "long touch");
                if(wayPointSet==1){
                    //wayPointSet == 1 means there is an existing waypoint.
                    if(wayPoint == sV.getPoint()){
                        //make waypoint null.
                        try {
                            Shared.btController.write("rw");
                            wayPoint=null;
                            removeWayPoint();
                            refreshMap();
                            wayPointSet = 0;
                        } catch (IOException e) {
                            e.printStackTrace();
                            Toast.makeText(getContext(), "Message cannot be sent. Waypoint not removed", Toast.LENGTH_SHORT).show();
                        }
                    }
                    else{
                        Toast.makeText(getContext(), "A way point has already been set. Please unset waypoint first", Toast.LENGTH_SHORT).show();
                    }

                }
                else if(wayPointSet==0){
                    //Set waypoint here

                    try {
                        Shared.btController.write("SET WP," + sV.getPoint().getxCoord() + ":"+sV.getPoint().getyCoord());
                        wayPoint = sV.getPoint();
                        wayPointSet = 1;
                        refreshMap();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Message cannot be sent. Waypoint not set  ", Toast.LENGTH_SHORT).show();
                    }

                }
                return true;
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

    public void moveForward() throws IOException {
        //assume robot is facing north now, move foward 1 step, y := y+1
        //Should contain code to send bluetooth message to rpi to make robot move forward.
        int y = curPos.getyCoord();
        int x = curPos.getxCoord();
        Shared.btController.write("MOVE FORWARD");
        // facing north
        if (direction == 0) {
            if(y<18) {
                curPos.setyCoord(y+1);
                refreshMap();
            }
        }

        // facing west
        else if (direction == 1){
            if(x>1) {
                curPos.setxCoord(x - 1);
                refreshMap();
            }
        }
        // facing south
        else if (direction == 2){
            if(y>1) {
                curPos.setyCoord(y - 1);
                refreshMap();
            }
        }
        // facing east
        else if (direction == 3){
            if(x<13) {
                curPos.setxCoord(x + 1);
                refreshMap();
            }
        }


        //Cannot just set board, must remove all the views first. hmmmm

    }

    public void moveBackward() throws IOException {

        int y = curPos.getyCoord();
        int x = curPos.getxCoord();
        Shared.btController.write("MOVE BACKWARD");
        // facing north
        if (direction == 0) {

            if(y>1) {
                curPos.setyCoord(y - 1);
                refreshMap();
            }
        }

        // facing west
        else if (direction == 1){

            if(x<13) {
                curPos.setxCoord(x + 1);
                refreshMap();
            }
        }
        // facing south
        else if (direction == 2){
            if(y<18) {
                curPos.setyCoord(y+1);
                refreshMap();
            }
        }
        // facing east
        else if (direction == 3){
            if(x>1) {
                curPos.setxCoord(x - 1);
                refreshMap();
            }
        }
    }

    public void moveLeftward() throws IOException {

        Shared.btController.write("TURN LEFT");
        if (direction ==1) {
            direction = 2;
        }
        else if (direction == 2){
            direction = 3;
        }
        else if (direction == 3){
            direction = 0;
        }
        else if (direction == 0){
            direction = 1;
        }
        Log.d(TAG, String.valueOf(direction));
        refreshMap();

    }

    public void moveRightward() throws IOException {
        Shared.btController.write("TURN RIGHT");
        if (direction ==1) {
            direction = 0;
        }
        else if (direction == 2){
            direction = 1;
        }
        else if (direction == 3){
            direction = 2;
        }
        else if (direction == 0){
            direction = 3;
        }
        Log.d(TAG, String.valueOf(direction));
        refreshMap();
    }

    /*
        How to identify boxes that car occupies? eg car is at (x,y), boxes occupied:
        (x+1,y), (x,y+1), (x+1,y+1),(x,y)
        */
    public void displayCurrentPosition(){
        int x = curPos.getxCoord();
        int y = curPos.getyCoord();
        int counter =-1;
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
            if(sV!=null) {
                counter++;
                //Log.i(TAG, "displaying cur position");
                if (direction == 0 && counter == 3) {
                    sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.red_box, null));
                }
                else if (direction == 1 && counter == 2){
                    sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.red_box, null));
                }
                else if (direction == 2 && counter == 4){
                    sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.red_box, null));
                }
                else if (direction == 3 && counter == 1){
                    sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.red_box, null));
                }
                else {
                    sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.blue_box, null));
                }
            }
        }

    }

    public void displayWayPoint(){
        if(wayPoint != null){
            //Log.i(TAG, "displaying waypoint");
            int x = wayPoint.getxCoord();
            int y = wayPoint.getyCoord();

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
                // hk changed ==
                if(tempGp.getStatus() != '0'){
                    Toast.makeText(getContext(), "There is an obstacle here. Cannot set waypoint here", Toast.LENGTH_SHORT).show();
                    wayPointSet = 0;
                    wayPoint = null;
                    break;
                }
            }
            if(wayPointSet ==1){
                for(GridPoint tempGp: gpArray2){
                    SquareView sV = gpMap.get(tempGp);
                    if(sV!=null){
                        Log.i(TAG,"displaying waypoint");
                        sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.green_box,null));
                    }
                }

            }

        }else{
            Log.i(TAG, "No waypoint set");
        }

    }

    //Removes way point, sets image to white_box.png.
    public void removeWayPoint(){
        if(wayPoint != null){
            Log.i(TAG, "displaying waypoint");
            int x = wayPoint.getxCoord();
            int y = wayPoint.getyCoord();

            //Temporary array to hold gridpoint objects
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
                    sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.white_box,null));
                }
            }
        }else{
            Log.i(TAG, "Waypoint removed.");
        }

    }



    //Use hash map to refresh map!
    //Updates status of square
    //Updates robot position
    //Updates waypoint (if any)
    public void refreshMap(){
        Log.i(TAG,"refresh map");
        //Should contain code to get updated map from rpi and current position. change variable curPos in here!
        String[] stringArray = segmentString(RpiData, numRows, numCol);
        for(int i=0;i<numRows;i++){
            for(int j=0;j<numCol;j++){
                GridPoint tempGp = gpArray[i][j];
                //Log.i(TAG, "Array i,j: " + i + ","+j + "tempGp: " + tempGp.getxCoord() + ", " + tempGp.getyCoord());
                SquareView tempSv = gpMap.get(tempGp);
                tempSv.getPoint().setStatus(stringArray[i].charAt(j));
                updateImage(tempSv);
                //Log.i(TAG,"Updating " + tempSv.getPoint().getxCoord() + ", " + tempSv.getPoint().getyCoord());

            }
        }
        displayCurrentPosition();
        displayWayPoint();
        Log.i(TAG, "CurPos" + curPos.getxCoord() +","+curPos.getyCoord());
    }





    private void updateImage(SquareView sV){
        if(sV.getPoint().getStatus() == '0'){
            //Log.i(TAG,"unexplored");
            sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.black_box,null));
        }
        else if(sV.getPoint().getStatus() == '1') {
            //Log.i(TAG, "explored");
            sV.getGridImage().setImageDrawable(getResources().getDrawable(R.drawable.white_box,null));
        }
    }

    private void setStartPoint(){

    }



}
