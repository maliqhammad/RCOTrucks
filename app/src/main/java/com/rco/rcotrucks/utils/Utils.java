package com.rco.rcotrucks.utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import static android.content.Context.CONNECTIVITY_SERVICE;

public class Utils {
    public static final String TAG = "Utils";

    public static String getDeviceId(Context context) {
        String androidId = Settings.System.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "getDeviceId: deviceId: "+androidId);
        return androidId;
    }

    public static void exitApplication(Activity activity) {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP); //***Change Here***

        activity.startActivity(intent);
        activity.finish();
        System.exit(0);
    }

    public static boolean isConnectedNetworkTypeWifi(Context ctx) {
        String connectedNetworkMethod = getConnectedNetworkMethod(ctx);

        if (connectedNetworkMethod == null)
            return false;

        return connectedNetworkMethod.equalsIgnoreCase("WiFi");
    }

    public static String getConnectedNetworkMethod(Context ctx) {
        try {
            ConnectivityManager mgr = (ConnectivityManager) ctx.getSystemService(CONNECTIVITY_SERVICE);

            if (mgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE).isConnectedOrConnecting())
                return "Mobile";

            if (mgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnectedOrConnecting())
                return "WiFi";
        } catch (Throwable throwable) {

        }

        return null;
    }

    public static ArrayList<String> readRawTextFile(Context ctx, int resId) {
        InputStream inputStream = ctx.getResources().openRawResource(resId);
        InputStreamReader inputreader = new InputStreamReader(inputStream);
        BufferedReader buffreader = new BufferedReader(inputreader);
        ArrayList<String> result = new ArrayList();
        String line;

        try {
            while ((line = buffreader.readLine()) != null) {
                result.add(line);
            }
        } catch (IOException e) {
            return null;
        }

        return result;
    }

    public static boolean IS_DEBUG_BACKGROUND_TASK_CONFLICT = false;
    public static boolean IS_DEBUG_STRICT_MODE = true;
    public static boolean IS_DEBUG_INIT_DVIR_DB_SETUP = false;
    public static boolean IS_DEBUG_DISABLE_DOWNSYNC_DVIR_GROUP_AT_LOGIN = false;


    public static boolean isDebug(Context context, boolean isDebugTarget, String toastwarning) {
        boolean bRet = isDebugTarget && BuildUtils.IS_DEBUG;
        if (bRet && context != null && toastwarning != null)
            UiUtils.showToast(context, toastwarning);
        Log.d(TAG, "isDebug(), ******** Is: " + toastwarning + "? " + bRet + "\n");
        return bRet;
    }

    public static double convertKmsToMiles(double kms) {
        return (0.621371192 * kms);
    }


    public static int convertKmsToRoundedMiles(double kms) {
        return MathUtils.round((0.621371192 * kms),0);
    }

    public static float convertMetersToMiles(double meters) {
        return (float) (0.000621371192 * meters);
    }

    public static float calculateDistance(LatLng latLngA, LatLng latLngB) {
        Location locationA = new Location("point A");

        locationA.setLatitude(latLngA.latitude);
        locationA.setLongitude(latLngA.longitude);

        Location locationB = new Location("point B");

        locationB.setLatitude(latLngB.latitude);
        locationB.setLongitude(latLngB.longitude);

        return locationA.distanceTo(locationB);
    }

    public static String convertDistanceInMiles(double distanceInMeters) {

        if (distanceInMeters < 300) {
            return convertMetersToFeet(distanceInMeters) + " ft";
        } else {
            return MathUtils.roundTo1DecimalCases("" + convertMetersToMiles(distanceInMeters)) + " mi";
        }
    }

    public static String getStationMiles(int meters) {

        if (meters < 300) {
            return convertMetersToFeet(meters) + " ft";
        } else {
            return MathUtils.round(convertMetersToMiles(meters), 0) + " mi";
        }
    }

    public static int convertMetersToFeet(double meters) {
        return (int) (meters * 3.28084);
    }

    public static LatLng convertLocationToLatLng(Location loc) {
        return new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    public static boolean areLocationServicesEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
