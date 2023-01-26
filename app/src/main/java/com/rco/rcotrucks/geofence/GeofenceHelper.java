package com.rco.rcotrucks.geofence;

import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.maps.model.LatLng;
import com.rco.rcotrucks.receiver.GeofenceBroadcastReceiver;
import com.rco.rcotrucks.utils.Constants;

public class GeofenceHelper extends ContextWrapper {

    private static final String TAG = "GeofenceHelper";
    PendingIntent pendingIntent;
    Intent intent;
    int currentSegmentForIntent = -1;

    public GeofenceHelper(Context base) {
        super(base);
    }

    public GeofencingRequest getGeofencingRequest(Geofence geofence) {
        return new GeofencingRequest.Builder()
                .addGeofence(geofence)
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .build();
    }

    public Geofence getGeofence(String ID, double latitude, double longitude, int transitionTypes, float straightDistance) {
        Log.d(TAG, "generateCoordinates: getGeofence: straightDistance: " + straightDistance);
//        May 20, 2022  -   We should pass the distance between the previous point and current
        float geoRadiusInMeters = Constants.GEOFENCE_RADIUS_IN_METERS;
        if (straightDistance > 0 && straightDistance < geoRadiusInMeters) {
            geoRadiusInMeters = (straightDistance * 0.8f);

            Log.d(TAG, "generateCoordinates: getGeofence: straightDistance is greater than 0 and less than default geoRadiusInMeters: geoRadiusInMeters: " + geoRadiusInMeters);
        }


        return new Geofence.Builder()
                .setCircularRegion(latitude, longitude, geoRadiusInMeters)
                .setRequestId(ID)
                .setTransitionTypes(transitionTypes)
                .setLoiteringDelay(5000)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .build();
    }

    public PendingIntent getPendingIntent() {
        Log.d(TAG, "onReceive: getPendingIntent: pendingIntent: " + pendingIntent);
        if (pendingIntent != null) {
            Log.d(TAG, "onReceive: getPendingIntent: return pendingIntent ");
            return pendingIntent;
        }
        intent = new Intent(this, GeofenceBroadcastReceiver.class);
        Log.d(TAG, "onReceive: getPendingIntent: currentSegmentForIntent: " + currentSegmentForIntent);
        intent.putExtra("currentSegment", "" + currentSegmentForIntent);
        pendingIntent = PendingIntent.getBroadcast(this, 2607, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        return pendingIntent;
    }

    public String getErrorString(Exception e) {
        if (e instanceof ApiException) {
            ApiException apiException = (ApiException) e;
            switch (apiException.getStatusCode()) {
                case GeofenceStatusCodes
                        .GEOFENCE_NOT_AVAILABLE:
                    return "GEOFENCE_NOT_AVAILABLE";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_GEOFENCES:
                    return "GEOFENCE_TOO_MANY_GEOFENCES";
                case GeofenceStatusCodes
                        .GEOFENCE_TOO_MANY_PENDING_INTENTS:
                    return "GEOFENCE_TOO_MANY_PENDING_INTENTS";
            }
        }
        return e.getLocalizedMessage();
    }

    public void setCurrentSegment(int currentSegment) {
        currentSegmentForIntent = currentSegment;
        Log.d(TAG, "onReceive: currentSegmentForIntent: " + currentSegmentForIntent);
    }

}
