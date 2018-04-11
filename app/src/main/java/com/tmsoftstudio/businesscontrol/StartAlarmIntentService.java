package com.tmsoftstudio.businesscontrol;

import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;


public class StartAlarmIntentService extends IntentService {

    static final String ACTION_START_ALARM = "com.tmsoftstudio.businesscontrol.action.START_ALARM";
    static final String EXTRA_PARAM_INTERVAL = "com.tmsoftstudio.businesscontrol.extra.PARAM_INTERVAL";
    static final String ACTION_START_ALARM_BOOT = "com.tmsoftstudio.businesscontrol.action.START_ALARM_BOOT";


    public static void startActionAlarm(Context context, long param) {
        Intent intent = new Intent(context, StartAlarmIntentService.class);
        intent.setAction(ACTION_START_ALARM);
        intent.putExtra(EXTRA_PARAM_INTERVAL, param);
        context.startService(intent);
    }


    public StartAlarmIntentService() {
        super("StartAlarmIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_START_ALARM.equals(action)) {
                final long param = intent.getLongExtra(EXTRA_PARAM_INTERVAL, 15L*1000*60);
                if(param!=0){
                    startAlarm(param);
                    this.stopSelf();
                }
            }
            if (ACTION_START_ALARM_BOOT.equals(action)) {
                final long param = intent.getLongExtra(EXTRA_PARAM_INTERVAL, 15L*1000*60);
                if(param!=0){
                    startAlarm(param);
                    BootReceiver.completeWakefulIntent(intent);
                    this.stopSelf();
                }
            }

        }
    }



    public void startAlarm(long interval) {
        Intent intent = new Intent(getApplicationContext(), LocationReceiver.class);
        final PendingIntent pIntent = PendingIntent.getBroadcast(getApplicationContext(), LocationReceiver.REQUEST_CODE, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        long firstMillis = System.currentTimeMillis();
        AlarmManager alarm = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        //  alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, interval, pIntent);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, firstMillis, interval, pIntent);
    }

}
