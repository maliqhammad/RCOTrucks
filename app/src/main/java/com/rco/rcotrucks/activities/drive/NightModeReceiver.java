package com.rco.rcotrucks.activities.drive;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;


import java.util.Calendar;
import java.util.TimeZone;

import ca.rmen.sunrisesunset.SunriseSunset;

public class NightModeReceiver extends BroadcastReceiver {
    private static int PENDING_INTENT_SWITCH_MODES = 2;
    private Event delegate;

    public final static String ACTION_SWITCH_NIGHT_MODE = "com.rco.rcotrucks.ACTION_SWITCH_NIGHT_MODE";
    public final static String ACTION_SWITCH_DAY_MODE = "com.rco.rcotrucks.ACTION_SWITCH_DAY_MODE";

    public NightModeReceiver(Event listener) {
        this.delegate = listener;
    }

    public static NightModeReceiver register(Context ctx, Event event) {
        NightModeReceiver receiver = new NightModeReceiver(event);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_SWITCH_NIGHT_MODE);
        filter.addAction(ACTION_SWITCH_DAY_MODE);
        ctx.registerReceiver(receiver, filter);
        return receiver;
    }

    public static void unregister(Context ctx, BroadcastReceiver receiver) {
        try {
            ctx.unregisterReceiver(receiver);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public static void schedule(Context context, Calendar[] sunriseSunset) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingDayIntent = getSwitchDayModeIntent(context);
        PendingIntent pendingNightIntent = getSwitchNightModeIntent(context);

        Calendar sunrise = sunriseSunset[0];
        sunrise.add(Calendar.MINUTE, -30);
        Log.e("isnight", "sunrise-->" + sunrise);

        Calendar sunset = sunriseSunset[1];
        sunset.add(Calendar.MINUTE, 30);

        Log.e("isnight", "sunset-->" + sunset);
        alarmManager.setExact(AlarmManager.RTC, sunrise.getTimeInMillis(), pendingDayIntent);
        alarmManager.setExact(AlarmManager.RTC, sunset.getTimeInMillis(), pendingNightIntent);
    }

    public static void cancel(Context context) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingDayIntent = getSwitchDayModeIntentForCancel(context);
        alarmManager.cancel(pendingDayIntent);

        PendingIntent pendingNightIntent = getSwitchNightModeIntentForCancel(context);
        alarmManager.cancel(pendingNightIntent);
    }

    private static PendingIntent getSwitchNightModeIntent(Context context) {
        Intent intent = new Intent(ACTION_SWITCH_NIGHT_MODE);
        return PendingIntent.getBroadcast(context, PENDING_INTENT_SWITCH_MODES, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getSwitchDayModeIntent(Context context) {
        Intent intent = new Intent(ACTION_SWITCH_DAY_MODE);
        return PendingIntent.getBroadcast(context, PENDING_INTENT_SWITCH_MODES, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static PendingIntent getSwitchDayModeIntentForCancel(Context context) {
        Intent intent = new Intent(ACTION_SWITCH_DAY_MODE);
//        Sep 14, 2022  -   Previously it was like this
//        return PendingIntent.getBroadcast(context, PENDING_INTENT_SWITCH_MODES, intent, 0);

//        But when i Update the target sdk from 29 to 33 its starts showing this error
//        IllegalArgumentException: com.rco.rcotrucks: Targeting S+ (version 31 and above) requires that one of FLAG_IMMUTABLE or FLAG_MUTABLE be specified when creating a PendingIntent.
//      Strongly consider using FLAG_IMMUTABLE, only use FLAG_MUTABLE if some functionality depends on the PendingIntent being mutable, e.g. if it needs to be used with inline replies or bubbles.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(context, PENDING_INTENT_SWITCH_MODES, intent,PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getBroadcast(context, PENDING_INTENT_SWITCH_MODES, intent, 0);
        }
    }

    private static PendingIntent getSwitchNightModeIntentForCancel(Context context) {
        Intent intent = new Intent(ACTION_SWITCH_NIGHT_MODE);
//        Sep 14, 2022  -   Previously it was like this
//        return PendingIntent.getBroadcast(context, PENDING_INTENT_SWITCH_MODES, intent, 0);
//        Replaced with
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return PendingIntent.getBroadcast(context, PENDING_INTENT_SWITCH_MODES, intent,PendingIntent.FLAG_IMMUTABLE);
        } else {
            return PendingIntent.getBroadcast(context, PENDING_INTENT_SWITCH_MODES, intent, 0);
        }

    }

    @SuppressWarnings("deprecation")
    private static void deprecatedSetAlarm(Context context, Calendar calendar, PendingIntent pendingIntent) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC, calendar.getTimeInMillis(), pendingIntent);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_SWITCH_NIGHT_MODE.equals(intent.getAction())) {
            delegate.onSwitchNightMode();
        } else if (ACTION_SWITCH_DAY_MODE.equals(intent.getAction())) {
            delegate.onSwitchDayMode();
        }
    }

    public interface Event {
        void onSwitchNightMode();

        void onSwitchDayMode();
    }
}
