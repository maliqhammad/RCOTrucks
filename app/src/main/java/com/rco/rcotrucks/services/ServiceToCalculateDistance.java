package com.rco.rcotrucks.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

public class ServiceToCalculateDistance extends Service {

    private static final String TAG = ServiceToCalculateDistance.class.getSimpleName();

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand: ");
        updateDeviceOfflineCommand();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "onBind: ");
        return null;
    }


    private void updateDeviceOfflineCommand() {
        Log.d(TAG, "updateDeviceOfflineCommand: ");
//        FirebaseFirestore.getInstance()
//                .collection("devices")
////                .document(device.getId())
//                .document("OS45Mj")
//                .update("offlineCommand", "OFFLINE")
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        Log.d(TAG, "setOfflineCommand: onSuccess: ");
//                        stopSelf();
//                        stopForeground(true);
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@androidx.annotation.NonNull Exception e) {
//                        Log.d(TAG, "setOfflineCommand: onFailure: ");
//                    }
//                });

    }

}

