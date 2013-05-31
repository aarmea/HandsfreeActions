package com.albertarmea.handsfreeactions;

import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by aarmea on 5/31/13.
 */
public class PhoneActions {
    public static final String TAG = "PhoneActions";

    public void act(String command) {
        BasicAction action = defaultActions.get(command);
        if (action != null) {
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
                // TODO: implement!
            }
        });

        // TODO: handle more commands
    }};

    private static void hangUpCall() {
        // TODO: implement!
    }
}
