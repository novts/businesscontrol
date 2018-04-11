package com.tmsoftstudio.businesscontrol;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.WakefulBroadcastReceiver;

public class BootReceiver extends WakefulBroadcastReceiver {

    private SharedPreferences mSettings;

    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        mSettings = context.getSharedPreferences("APP_PREFERENCES", Context.MODE_PRIVATE);

        if(mSettings.contains("APP_PREFERENCES_START_ALARM")) {
            Boolean start = mSettings.getBoolean("APP_PREFERENCES_START_ALARM", false);
            if (start == true) {
                Intent service = new Intent(context, StartAlarmIntentService.class);
                service.setAction(StartAlarmIntentService.ACTION_START_ALARM_BOOT);
                long interval=15L*1000*60;
                service.putExtra(StartAlarmIntentService.EXTRA_PARAM_INTERVAL, interval);
                startWakefulService(context, service);
            }
        }
    }
}
