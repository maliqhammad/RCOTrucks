package com.rco.rcotrucks.businesslogic;

import android.app.Activity;
import android.util.Log;

import com.iosix.eldblelib.EldEngineStates;

import java.util.Date;

public class BleSimulator extends BleConnect {
    public static final int REQUEST_BASE = 100;
    public static final int REQUEST_BT_ENABLE = REQUEST_BASE + 1;

    private Activity a;
    private static IOnBleDataUpdate onBleDataUpdate;
    public static boolean isMalfunctionDiagnosticsSimulation = false;
    public static boolean isUnableToConnectToEldDeviceSimulation = false;

    private static int tripDistance = 0;
    private static int counter;

    public BleSimulator(Activity a, IOnBleDataUpdate onBleDataUpdate) {
        super();

        this.onBleDataUpdate = onBleDataUpdate;
        this.a = a;

        this.tripDistance = 0;

        if (isUnableToConnectToEldDeviceSimulation)
            onBleDataUpdate.OnBleStatus("No ELD found");
        else {
            onBleDataUpdate.OnBleStatus("Connected to Simulated ELD 123456789A");
            startEventsThread();
        }
    }

    private void startEventsThread() {
        Thread thread = new Thread() {
            @Override
            public void run() {
                try {
                    counter = 0;

                    while(true) {
                        //if (counter > 12 && counter < 20) return;

                        lastBleDataUpdate = new Date();

                        onBleDataUpdate.OnBleData("EngineHours: " + "11");
                        onBleDataUpdate.OnBleData("TripDistance: " + getNextTripDistance(counter));
                        onBleDataUpdate.OnBleData("Vin: " + getNextVinValue(counter));
                        onBleDataUpdate.OnBleData("EngineState: " + getNextEngineState(counter));
                        onBleDataUpdate.OnBleData("Latitude: " + getNextLatitude(counter));
                        onBleDataUpdate.OnBleData("Longitude: " + getNextLongitude(counter));
                        onBleDataUpdate.OnBleData("GpsSpeed: " + getNextGpsSpeed(counter));
                        onBleDataUpdate.OnBleData("Speed: " + getNextGpsSpeed(counter));
                        onBleDataUpdate.OnBleData("EngineSpeed: " + getNextGpsSpeed(counter));
                        onBleDataUpdate.OnBleData("Odometer: " + getNextOdometer(counter));
                        onBleDataUpdate.OnBleData("TripHours: " + getNextTripHours(counter));
                        onBleDataUpdate.OnBleData("RPM: " + getNextRpm(counter));

                        sleep(5000);
                        //Log.d(BusinessRules.TAG, "BLE Simulator roundtrip " + counter);
                        counter++;

                        if (false && counter == 15) { // Simulates disconnect in the middle of a already connected device
                            isUnableToConnectToEldDeviceSimulation = true;
                            onBleDataUpdate.OnBleStatus("No ELD found");
                        } else if (false && counter == 20) { // Simulates disconnect in the middle of a already connected device
                            isUnableToConnectToEldDeviceSimulation = false;
                            onBleDataUpdate.OnBleStatus("Connected to Simulated ELD 123456789A");
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
                }
            }
        };

        thread.start();
    }

    private double getNextRpm(int counter) {
        if (counter > 5 && counter < 10)
            return 0.0;

        return 10000.8;
    }

    private double getNextTripHours(int counter) {
        return counter * 0.5;
    }

    private Double getNextOdometer(int counter) {
        if (true && isMalfunctionDiagnosticsSimulation && counter >= 12 && counter < 62)
            return null; // To generate a malfunction event

        return 1000.6 + (counter * 2.1);
    }

    private double getNextTripDistance(int counter) {
        return tripDistance + (counter * 0.25);
    }

    private double getNextGpsSpeed(int counter) {
        if (counter < 5)
            return 0.0;
        else {
            if (counter > 21 && counter < 25)
                return 31.3;
            else if (counter > 26 && counter < 46)
                return 30.0;
            else if (counter > 46 && counter < 70)
                return 60.7 - (counter - 25.13);

            return 27.9;
        }
    }

    private double getNextLatitude(int counter) {
        return 37.2970523 + (counter * 0.01);
    }

    private double getNextLongitude(int counter) {
        return -121.9574969 + (counter * 0.01);
    }

    private EldEngineStates getNextEngineState(int counter) {
        if (counter < 5)
            return EldEngineStates.ENGINE_ON;
        else if (counter >= 5 && counter < 10)
            return EldEngineStates.ENGINE_ON;
        else if (counter >= 10 && counter < 15)
            return EldEngineStates.ENGINE_ON;
        else if (counter >= 15 && counter < 30)
            return EldEngineStates.ENGINE_ON;
        else if (counter >= 30 && counter < 60)
            return EldEngineStates.ENGINE_ON;

        return EldEngineStates.ENGINE_ON;
    }

    private String getNextVinValue(int counter) {
        if (false && isMalfunctionDiagnosticsSimulation && counter >= 12 && counter < 62)
            return null; // To generate a malfunction event

//        return "123456789";
        return "2A8HR54P18R747629";
        //return "";
    }

    public void enableDtcParameters() {
        String status = isUnableToConnectToEldDeviceSimulation ? "4: Not Connected to ELD" : "Connected to Simulated ELD 123456789A";
        //onBleDataUpdate.OnBleStatus("EnableDTCStatus (" + status + ")\n");
    }

    public void findAndConnectToBleDevice() {

    }

    public void resetBleDataCounters() {
        //tripDistance = (int) (counter * -0.25);
        //counter = 0;
    }
}
