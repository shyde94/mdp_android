package com.company.blue;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

// Huang kai from ZY group

public class MainActivity extends AppCompatActivity {
    public ScreenController screenController = new ScreenController();

    private static final String TAG = "MainActivityClass";

    public Activity activity = this;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Shared.activity = MainActivity.this;
        Shared.context = getApplicationContext();
        Shared.SC.openScreen(ScreenController.Screen.MENU);
    }
    /**
     * Method used when the Back button on phone is pressed
     */
    @Override
    public void onBackPressed() {
        if (ScreenController.getInstance().onBack()) {
            super.onBackPressed();
        }
    }
}