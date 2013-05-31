package com.albertarmea.handsfreeactions;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;

/**
 * Created by aarmea on 5/24/13.
 */
public class RemapperService extends Service {
    public static final String TAG = "RemapperService";

    public LogcatReader.OnLogReceiveListener listener = new LogcatReader.OnLogReceiveListener() {
        @Override
        public void onLogReceive(Date time, String message, String fullMessage) {
            // TODO: Parse the message for AT+???? signals, implement triggered activity
            Log.i(TAG, String.format("Received Bluetooth AT signal %s", message));
            actions.act(message);
        }
    };

    private LogcatReader bluetoothMonitor = null;
    private PhoneActions actions = new PhoneActions();

    @Override
    public void onCreate() {
        super.onCreate();
        fixPermissions();
        Toast.makeText(this, "RemapperService created", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);

        bluetoothMonitor = new LogcatReader("main", "Bluetooth AT recv", 1000);
        bluetoothMonitor.setOnLogReceiveListener(listener);
        bluetoothMonitor.start();

        Log.i(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothMonitor.stop();
        Log.i(TAG, "Service stopped");
        Toast.makeText(this, "RemapperService destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void fixPermissions() {
        // Get the READ_LOGS permission, which requires root as of Jelly Bean
        // From http://stackoverflow.com/questions/11461650/read-logs-permission-on-jelly-bean-api-16
        String pname = getPackageName();
        String[] CMDLINE_GRANTPERMS = { "su", "-c", null };
        if (getPackageManager().checkPermission(android.Manifest.permission.READ_LOGS, pname) != 0) {
            Log.d(TAG, "we do not have the READ_LOGS permission!");
            if (android.os.Build.VERSION.SDK_INT >= 16) {
                Log.d(TAG, "Working around JellyBeans 'feature'...");
                try {
                    // format the commandline parameter
                    CMDLINE_GRANTPERMS[2] = String.format("pm grant %s android.permission.READ_LOGS", pname);
                    java.lang.Process p = Runtime.getRuntime().exec(CMDLINE_GRANTPERMS);
                    int res = p.waitFor();
                    Log.d(TAG, "exec returned: " + res);
                    if (res != 0)
                        throw new Exception("failed to become root");
                } catch (Exception e) {
                    Log.d(TAG, "exec(): " + e);
                    Toast.makeText(this, "Failed to obtain READ_LOGS permission", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
}
