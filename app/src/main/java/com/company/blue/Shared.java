package com.company.blue;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Handler;

/**
 * Created by Shide on 26/8/17.
 */

public class Shared {
    public static Context context;
    public static Activity activity;
    public static ScreenController SC = new ScreenController();
    public static BluetoothClass btController = new BluetoothClass();
    public static Handler mHandler;
    public static SensorManager sMgr;
    public static Sensor mAccelerometer;
    public static Sensor mMagnetometer;
    public static Boolean PickUpMessages = false;
}
