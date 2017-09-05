package com.company.blue;

/**
 * Created by Shide on 29/8/17.
 */

public class GridPoint {
    private int xCoord;
    private int yCoord;
    private int status;

    public GridPoint(int xCoord, int yCoord, int status) {
        this.xCoord = xCoord;
        this.yCoord = yCoord;
        this.status = status;
    }

    public int getxCoord() {
        return xCoord;
    }

    public void setxCoord(int xCoord) {
        this.xCoord = xCoord;
    }

    public int getyCoord() {
        return yCoord;
    }

    public void setyCoord(int yCoord) {
        this.yCoord = yCoord;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
