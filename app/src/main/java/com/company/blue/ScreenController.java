package com.company.blue;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Shide on 26/8/17.
 */

public class ScreenController {
    private static final String TAG = "ScreenControllerClass";
    public static ScreenController mInstance = null;
    private static List<Screen> openedScreens = new ArrayList<Screen>();
    private FragmentManager mFragmentManager;

    public enum Screen {
        MENU,
        Bluetooth,
        Map
    }

    public static ScreenController getInstance() {
        if (mInstance == null) {
            mInstance = new ScreenController();
        }
        return mInstance;
    }

    public static Screen getLastScreen() {
        return openedScreens.get(openedScreens.size() - 1);
    }

    public void openScreen(Screen screen){

        Log.i(TAG, "Opening normal screen");
        mFragmentManager = Shared.activity.getFragmentManager();
        Fragment fragment = getFragment(screen);
        if(fragment!= null){
            FragmentTransaction fragmentTransaction = mFragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container,fragment, "CURRENT_FRAG");
            fragmentTransaction.commit();
            mFragmentManager.executePendingTransactions();
        }
        openedScreens.add(screen);
        Log.i(TAG," Open Screen queue: " + openedScreens.toString());

    }
    public boolean onBack() {
        Log.i(TAG, "onBack pressed");
        Log.i(TAG,"Screen queue: " + openedScreens.toString());
        if (openedScreens.size() > 0) {
            openedScreens.remove(openedScreens.size() - 1);
            if (openedScreens.size() == 0) {
                return true;
            }
            Screen screen = openedScreens.get(openedScreens.size() - 1);
            openedScreens.remove(openedScreens.size() - 1);
            /*if(screen.equals(Screen.MFH)){
                openScreen(Screen.MFH, Shared.destination);
            }
            else{
                openScreen(screen);
            }*/
            Log.i(TAG,"Post queue: " + openedScreens.toString());
            return false;
        }
        return true;
    }
    public Fragment getFragment(Screen screen){
        Fragment frag = null;
        switch(screen){
            case MENU:
                frag = new MainMenuFragment();
                break;
            case Bluetooth:
                //frag = new MyCustomMap();
                break;
            case Map:
                //frag = new TestFrag();
                break;
        }
        return frag;
    }
}
