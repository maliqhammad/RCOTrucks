package com.rco.rcotrucks.utils;

import android.os.Handler;

import java.util.Timer;
import java.util.TimerTask;

public class TimerUtils {
    private final Handler handler = new Handler();
    private Timer timer;
    private TimerTask timerTask;

    public Timer startTimer(int delay, int period, final Runnable r) {
        timer = new Timer();

        timerTask = new TimerTask() {
            public void run() {
                handler.post(r);
            }
        };

        timer.schedule(timerTask, delay, period);
        return timer;
    }

    public void stopTimer() {
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }
}
