package com.rco.rcotrucks.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.drive.DriveFragmentBase;
import com.rco.rcotrucks.activities.drive.MapFragment;
import com.rco.rcotrucks.activities.drive.NightModeReceiver;
import com.rco.rcotrucks.geofence.NotificationHelper;

import java.util.List;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = GeofenceBroadcastReceiver.class.getSimpleName();
    String requestId = "";

    public GeofenceBroadcastReceiver() {
    }

    public void onReceive(Context context, Intent intent) {
        String currentSegment = "";
        if (intent != null) {

            currentSegment = intent.getStringExtra("currentSegment");
            Log.d(TAG, "onReceive: currentSegment: " + currentSegment + " ");
        }

        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);

        if (geofencingEvent.hasError()) {
            String errorMessage = GeofenceStatusCodes
                    .getStatusCodeString(geofencingEvent.getErrorCode());
            Log.e(TAG, errorMessage);
            return;
        }

//        May 20, 2022  -   Added notifications for geofence testing
//        NotificationHelper notificationHelper = new NotificationHelper(context);

//         Get the transition type.
        List<Geofence> geofenceList = geofencingEvent.getTriggeringGeofences();
        Log.d(TAG, "compareRequestId: onReceive: geofenceList: size: " + geofenceList.size());
        for (Geofence geofence : geofenceList) {
            Log.d(TAG, "compareRequestId: onReceive: gettingRequestId: " + geofence.getRequestId());
            String[] splitRequestId = geofence.getRequestId().split("_");

            if (splitRequestId == null)
                return;

            if (splitRequestId.length < 3) {
                return;
            }

            requestId = geofence.getRequestId();

            String segment = splitRequestId[0].trim();
            String speedIndex = splitRequestId[1].trim();
            String speed = splitRequestId[2].trim();


            if (currentSegment != null) {
                if (currentSegment.equalsIgnoreCase(segment)) {
                    Log.d(TAG, "onReceive: yeah its equal: currentSegment: " + currentSegment);
                }
            }

        }

//        if (geofenceList.size()>0) {
//
//            String[] splitRequestId = geofenceList.get(0).getRequestId().split("_");
//
//            if (splitRequestId == null)
//                return;
//
//            if (splitRequestId.length < 3) {
//                return;
//            }
//
//            requestId = geofenceList.get(0).getRequestId();
//            Log.d(TAG, "compareRequestId: onReceive: gettingRequestId: " + requestId);
//
//            String segment = splitRequestId[0].trim();
//            String speedIndex = splitRequestId[1].trim();
//            String speed = splitRequestId[2].trim();
//        }


        int transitionType = geofencingEvent.getGeofenceTransition();
        switch (transitionType) {
            case Geofence.GEOFENCE_TRANSITION_ENTER:
                sendDataBack(requestId + "_ENTER");

//                May 20, 2022  -   Added notifications for geofence testing
//                Toast.makeText(context, "GEOFENCE_TRANSITION_ENTER", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_ENTER", "", MainMenuActivity.class);
                break;
//            case Geofence.GEOFENCE_TRANSITION_DWELL:
//                Toast.makeText(context, "GEOFENCE_TRANSITION_DWELL", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_DWELL", "", MainMenuActivity.class);
//                break;
            case Geofence.GEOFENCE_TRANSITION_EXIT:
                sendDataBack(requestId + "_EXIT");

//                May 20, 2022  -   Added notifications for geofence testing
//                Toast.makeText(context, "GEOFENCE_TRANSITION_EXIT", Toast.LENGTH_SHORT).show();
//                notificationHelper.sendHighPriorityNotification("GEOFENCE_TRANSITION_EXIT", "", MainMenuActivity.class);
                break;

        }

    }

    void sendDataBack(String requestIdAndEventType) {
        if (DriveFragmentBase.getInstace() != null)
            DriveFragmentBase.getInstace().eventFromGeofence(requestIdAndEventType);
    }


    public interface GeofenceEvent {
        void onGeofenceEnter();

        void onGeofenceExit();
    }

}

