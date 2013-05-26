package com.albertarmea.handsfreeactions;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.CompoundButton;
import android.widget.Switch;

public class SettingsActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        setupUi();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    private void setupUi() {
        Switch masterSwitch = (Switch) findViewById(R.id.master_switch);
        // Set the masterSwitch's initial state
        masterSwitch.setChecked(isServiceRunning());
        // Set the masterSwitch action
        masterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    startService(new Intent(SettingsActivity.this, RemapperService.class));
                } else {
                    stopService(new Intent(SettingsActivity.this, RemapperService.class));
                }
            }
        });
    }

    private boolean isServiceRunning() {
        // TODO: Use a constant time method of checking if the service is running
        // Linearly search running services for the RemapperService
        // Android does not provide an API to check if a service is running
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (RemapperService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
