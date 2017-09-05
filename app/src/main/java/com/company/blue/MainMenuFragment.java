package com.company.blue;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import static com.company.blue.ScreenController.Screen.Bluetooth;
import static com.company.blue.ScreenController.Screen.Map;

/**
 * Created by Shide on 26/8/17.
 */

public class MainMenuFragment extends Fragment{
    Button mBluetoothConfig;
    Button mMapConfig;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.menu_fragment, container, false);
        mBluetoothConfig = view.findViewById(R.id.bluetooth_config);
        mMapConfig = view.findViewById(R.id.map_config);
        mBluetoothConfig.setText("Bluetooth");

        mBluetoothConfig.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Shared.SC.openScreen(Bluetooth);
                    }
                }
        );
        mMapConfig.setOnClickListener(
                new View.OnClickListener(){
                    @Override
                    public void onClick(View view) {
                        Shared.SC.openScreen(Map);
                    }
                }
        );
        return view;
    }
}
