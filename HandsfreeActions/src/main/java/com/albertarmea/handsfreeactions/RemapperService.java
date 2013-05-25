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
    String tag = "RemapperService";

    @Override
    public void onCreate() {
        super.onCreate();
        Toast.makeText(this, "RemapperService created", Toast.LENGTH_LONG).show();
        Log.i(tag, "Service created");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i(tag, "Service started");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toast.makeText(this, "RemapperService destroyed", Toast.LENGTH_LONG).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
