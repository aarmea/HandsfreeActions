package com.albertarmea.handsfreeactions;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by aarmea on 5/24/13.
 */
public class RemapperService extends Service {
    public String tag = "RemapperService";
    public static boolean running = false;

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "RemapperService created", Toast.LENGTH_LONG).show();
        Log.i(tag, "Service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        super.onStartCommand(intent, flags, startId);
        Log.i(tag, "Service started");
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        running = false;
        Log.i(tag, "Service stopped");
        super.onDestroy();
        Toast.makeText(this, "RemapperService destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
