package com.albertarmea.handsfreeactions;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by aarmea on 5/24/13.
 */
public class RemapperService extends Service {
    public static final String TAG = "RemapperService";

    public LogcatReader.OnLogReceiveListener listener = new LogcatReader.OnLogReceiveListener() {
        @Override
        public void onLogReceive(Date time, String message, String fullMessage) {
            Log.i(TAG, String.format("Received Bluetooth AT signal %s", message));
            act(message);
        }
    };

    private LogcatReader bluetoothMonitor = null;
    private Date lastBLDN = null;
    private long lastBLDNdelay = 5000;
    private BroadcastReceiver phoneReceiver = null;

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

        registerPhone();

        Log.i(TAG, "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterPhone();
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

    private boolean callCausedByBLDN() {
        // if (lastBLDN == null) return false;
        long lastBLDNage = Math.abs((new Date()).getTime() - lastBLDN.getTime());
        Log.d(TAG, String.format("Call came %d ms after the last BLDN (tolerance %d ms)", lastBLDNage, lastBLDNdelay));
        return lastBLDNage < lastBLDNdelay;
    }

    private void registerPhone() {
        phoneReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d(TAG, "Detected outgoing call");
                if (callCausedByBLDN()) {
                    Log.d(TAG, "Call caused by BLDN, aborting");
                    this.setResultData(null);
                    this.abortBroadcast();
                } else {
                    Log.d(TAG, "Ignoring last BLDN because it is too old");
                }
            }
        };
        IntentFilter filter = new IntentFilter("android.intent.action.NEW_OUTGOING_CALL");
        filter.setPriority(Integer.MAX_VALUE);
        registerReceiver(phoneReceiver, filter);
    }

    private void unregisterPhone() {
        unregisterReceiver(phoneReceiver);
        phoneReceiver = null;
    }

    private void act(String command) {
        BasicAction action = defaultActions.get(command);
        if (action != null) {
            Log.d(TAG, String.format("Handling Bluetooth command %s", command));
            action.act();
        } else {
            Log.d(TAG, String.format("Unknown Bluetooth command %s", command));
        }
    }

    private abstract class BasicAction {
        public abstract void act();
    }

    private final Map<String, BasicAction> defaultActions = new HashMap<String, BasicAction>() {{
        // BLDN: "Bluetooth last dialled number" - redial
        put("AT+BLDN", new BasicAction() {
            @Override
            public void act() {
                lastBLDN = new Date();
                // TODO: (wait? and) open the Google Now voice search activity
            }
        });

        // TODO: handle more commands
    }};
}
