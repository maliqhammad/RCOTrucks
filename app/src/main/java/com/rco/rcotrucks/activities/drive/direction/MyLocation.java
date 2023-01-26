package com.rco.rcotrucks.activities.drive.direction;


import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import static android.content.Context.LOCATION_SERVICE;

public class MyLocation {

    private static final String TAG = MyLocation.class.getSimpleName();
    private onLocationChanged listener;
    private static MyLocation myLocation;
    private static final float MIN_DISTANCE = 3;
    private static final int MIN_TIME = 1000;
    Context context;

    public static void init() {
        if (myLocation == null) {
            myLocation = new MyLocation();
        }
    }

    public static MyLocation getInstance() {
        return myLocation;
    }

    public interface onLocationChanged {
        void onLocationChanged(Location location);
    }

    public void initListener(onLocationChanged listener) {
        this.listener = listener;
    }

    public void requestLocation(Context context) {

        if (ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            return;
        LocationManager locationManager = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);

        String provider = locationManager.getBestProvider(criteria, true);
        //String provider = LocationManager.NETWORK_PROVIDER;
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, MIN_TIME, MIN_DISTANCE, new
                    LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {
                            Log.d(TAG, "onLocationChanged: " + location.getLatitude() + ", " + location.getLongitude());
                            if (location.getLatitude() != 0 && location.getLongitude() != 0) {

                                listener.onLocationChanged(location);
                            }
                        }

                        @Override
                        public void onStatusChanged(String provider, int status, Bundle extras) {
                            Log.d(TAG, "onStatusChanged: status: " + status);
                        }

                        @Override
                        public void onProviderEnabled(String provider) {
//                            Log.d(TAG, "onProviderEnabled: provider: " + provider);
                        }

                        @Override
                        public void onProviderDisabled(String provider) {
                            Log.d(TAG, "onProviderDisabled: provider: " + provider);
                        }
                    });
        }

    }

    String currentLatitude = "", currentLongitude = "";
    private FusedLocationProviderClient mFusedLocationProviderClient;
    LocationManager locationManager;
    LocationListener networkLocationListener;
    LocationListener gpsLocationListener;

    public void isLocationEnabled(Context context) {
        Log.d(TAG, "checkIsUserLocationPermissionGranted: ");
        this.context = context;
        if (!canGetLocation()) {

            showSettingsAlert();
            //DO SOMETHING USEFUL HERE. ALL GPS PROVIDERS ARE CURRENTLY ENABLED
        } else {
            Log.d(TAG, "enableLocationComponent: permission already granted");

//            getDeviceLocation();
        }
    }

    public void showSettingsAlert() {
        Log.d(TAG, "showSettingsAlert: ");

        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        // Setting Dialog Title
        alertDialog.setTitle("Location!");

        // Setting Dialog Message
        alertDialog.setMessage("GPS is disabled. To enable again go to settings.");

        // On pressing Settings button
        alertDialog.setPositiveButton("Settings",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        context.startActivity(intent);
                    }
                });

        alertDialog.show();
    }


    public boolean canGetLocation() {
        Log.d(TAG, "canGetLocation: ");

        boolean result = true;
        LocationManager lm = null;
        boolean gps_enabled = false;
        boolean network_enabled = false;
        if (lm == null)
            lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        // exceptions will be thrown if provider is not permitted.
        try {
            gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception ex) {

        }
        try {
            network_enabled = lm
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception ex) {
        }
        if (gps_enabled == false || network_enabled == false) {
            result = false;
        } else {
            result = true;
        }

        return result;
    }

    public void getDeviceLocation(Context context) {
        Log.d(TAG, "getDeviceLocation: Getting device current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }

        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

        final Task location = mFusedLocationProviderClient.getLastLocation();


        location.addOnCompleteListener(new OnCompleteListener() {
            @Override
            public void onComplete(@NonNull Task task) {
                if (task.isSuccessful()) {
                    Log.d(TAG, "onComplete: task.isSuccessful: " + task.isSuccessful());
                    Location currentLocation = (Location) location.getResult();

                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        if (currentLocation != null) {
                            addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 8);
                            currentLatitude = "" + currentLocation.getLatitude();
                            currentLongitude = "" + currentLocation.getLongitude();
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
                    Log.d(TAG, "getDeviceLocation: onComplete :Location Not Found (current location is null)");
                }

            }
        });

        if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {

            networkLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
//                    currentLatitude = "" + location.getLatitude();
//                    currentLongitude = "" + location.getLongitude();

                    if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                        Log.d(TAG, "NETWORK_PROVIDER: onLocationChanged: NETWORK: lat: " + location.getLatitude() + " longitude: " + location.getLongitude());
                        listener.onLocationChanged(location);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d(TAG, "onStatusChanged: NETWORK_PROVIDER: ");
                }

                @Override
                public void onProviderEnabled(String provider) {
//                    Log.d(TAG, "onProviderEnabled: NETWORK_PROVIDER: ");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.d(TAG, "onProviderDisabled: NETWORK_PROVIDER: ");
                }
            };

//            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, networkLocationListener);
            locationManager.requestLocationUpdates(1000, 0, getCriteria(), networkLocationListener, null);

        } else if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

            gpsLocationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
//                    currentLatitude = "" + location.getLatitude();
//                    currentLongitude = "" + location.getLongitude();
                    if (location.getLatitude() != 0 && location.getLongitude() != 0) {
                        Log.d(TAG, "GPS_PROVIDER: onLocationChanged: GPS: lat: " + location.getLatitude() + " longitude: " + location.getLongitude());
                        listener.onLocationChanged(location);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.d(TAG, "GPS_PROVIDER: onStatusChanged: ");
                }

                @Override
                public void onProviderEnabled(String provider) {
//                    Log.d(TAG, "GPS_PROVIDER: onProviderEnabled: ");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.d(TAG, "GPS_PROVIDER: onProviderDisabled: ");
                }
            };

//            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, gpsLocationListener);
            locationManager.requestLocationUpdates(1000, 0, getCriteria(), gpsLocationListener, null);
        }

    }

    private void stopLocationUpdatesForNetwork(LocationListener locationListener) {
        locationManager.removeUpdates(locationListener);
    }

    private void stopLocationUpdatesForGPS(LocationListener locationListener) {
        locationManager.removeUpdates(locationListener);
    }

    Criteria getCriteria() {
        Criteria criteria = new Criteria();
        criteria.setAccuracy(Criteria.ACCURACY_COARSE);
        criteria.setHorizontalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setVerticalAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setBearingAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setSpeedAccuracy(Criteria.ACCURACY_HIGH);
        criteria.setPowerRequirement(Criteria.POWER_HIGH);
        return criteria;
    }

}