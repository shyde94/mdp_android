package com.company.blue;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


/**
 * Created by Shide on 29/8/17.
 */

/*
* SquareView is 1 box on the grid. This will be used to populate the grid. Streams from rpi3 will be taken in to identify position.
* GridPoint objects in map will contain position? 1 gridpoint object mapped to 1 squareview object.
* This means that SquareView class should contain object that contains bit stream data. Or, some way to assign values to GridPoint object.
*
* */

class SquareView extends FrameLayout {


    private ImageView gridImage;
    private GridPoint point = new GridPoint();

    private final String TAG = "SquareViewClass";



    public SquareView (Context context, AttributeSet attrs){
        super(context, attrs);
    }


    public static SquareView fromXml(Context context, ViewGroup parent, GridPoint point){
        SquareView Sv = (SquareView) LayoutInflater.from(context).inflate(R.layout.square_view, parent, false);
        Sv.setPoint(point);
        return Sv;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        gridImage = findViewById(R.id.grid_box);
        /*Log.i(TAG, "point:" + point.getxCoord()+","+point.getyCoord() + "status: " + point.getStatus());
        if(point.getStatus() == 0){
            Log.i(TAG,"unexplored");
            gridImage.setImageDrawable(getResources().getDrawable(R.drawable.white_box,null));
        }
        else if(point.getStatus() == 1) {
            Log.i(TAG, "explored");
            gridImage.setImageDrawable(getResources().getDrawable(R.drawable.black_box,null));
        }*/
    }

    public GridPoint getPoint() {
        return point;
    }

    public void setPoint(GridPoint point) {
        this.point = point;
    }

    public ImageView getGridImage() {
        return gridImage;
    }


}
