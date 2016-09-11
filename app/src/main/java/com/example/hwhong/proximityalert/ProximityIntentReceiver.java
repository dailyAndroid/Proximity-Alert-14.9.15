package com.example.hwhong.proximityalert;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.widget.Toast;

/**
 * Created by hwhong on 9/11/16.
 */
public class ProximityIntentReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String key = LocationManager.KEY_PROXIMITY_ENTERING;

        Boolean entering = intent.getBooleanExtra(key, false);

        if(entering) {
            Toast.makeText(context, "Entering", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(context, "Leaving", Toast.LENGTH_SHORT).show();
        }
    }
}
