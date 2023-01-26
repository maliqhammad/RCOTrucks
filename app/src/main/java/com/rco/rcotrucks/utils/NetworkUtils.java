package com.rco.rcotrucks.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkUtils {
    public static boolean isConnected(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        return nInfo != null && nInfo.isAvailable() && nInfo.isConnected();
    }

    public static boolean isConnectionWiFi(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

//        if (nInfo != null) {

            return nInfo.getType() == ConnectivityManager.TYPE_WIFI;
//        }
//        return false;
    }

    public static boolean isConnectionMobile(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        return nInfo.getType() == ConnectivityManager.TYPE_MOBILE;
    }

    public static boolean isConnectionBluetooth(Context ctx) {
        ConnectivityManager cm = (ConnectivityManager) ctx.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo nInfo = cm.getActiveNetworkInfo();

        return nInfo.getType() == ConnectivityManager.TYPE_BLUETOOTH;
    }
}
