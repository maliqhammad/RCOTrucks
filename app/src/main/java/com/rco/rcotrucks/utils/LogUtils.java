package com.rco.rcotrucks.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

class LogUtils {
    private static String TAG = "LogUtils";

    public static synchronized StringBuilder getLogs() {
        StringBuilder sbuf = null;

        try {
            Process process = Runtime.getRuntime().exec("logcat -d");
            BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()));

            sbuf = new StringBuilder();
            String line = "";
            while ((line = bufferedReader.readLine()) != null) {
                sbuf.append(line);
            }
//            TextView tv = (TextView)findViewById(R.id.textView1);
//            tv.setText(log.toString());
        } catch (Throwable e) {
            Log.d(TAG, "getLogs() **** Error: " + e);
            e.printStackTrace();
        }

        return sbuf;
    }

    public static synchronized void clearLogs() {
        try {
            Runtime.getRuntime().exec(new String[]{"logcat", "-c"});
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
