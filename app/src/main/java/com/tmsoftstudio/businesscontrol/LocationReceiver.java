package com.tmsoftstudio.businesscontrol;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class LocationReceiver extends BroadcastReceiver {

    public static final int REQUEST_CODE = 1;

    public LocationReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent i = new Intent(context, LocationIntentService.class);
        context.startService(i);
    }
}
