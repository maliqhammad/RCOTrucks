package com.rco.rcotrucks.businesslogic;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;

import androidx.core.app.ActivityCompat;

import android.util.Log;

import com.iosix.eldblelib.EldBleConnectionStateChangeCallback;
import com.iosix.eldblelib.EldBleDataCallback;
import com.iosix.eldblelib.EldBleError;
import com.iosix.eldblelib.EldBleScanCallback;
import com.iosix.eldblelib.EldBroadcast;
import com.iosix.eldblelib.EldBroadcastTypes;
import com.iosix.eldblelib.EldBufferRecord;
import com.iosix.eldblelib.EldCachedDataRecord;
import com.iosix.eldblelib.EldCachedNewTimeRecord;
import com.iosix.eldblelib.EldCachedPeriodicRecord;
import com.iosix.eldblelib.EldDataRecord;
import com.iosix.eldblelib.EldDriverBehaviorRecord;
import com.iosix.eldblelib.EldDtcCallback;
import com.iosix.eldblelib.EldEmissionsParametersRecord;
import com.iosix.eldblelib.EldEngineParametersRecord;
import com.iosix.eldblelib.EldFirmwareUpdateCallback;
import com.iosix.eldblelib.EldFuelRecord;
import com.iosix.eldblelib.EldLatestRecords;
import com.iosix.eldblelib.EldManager;
import com.iosix.eldblelib.EldParameterTypes;
import com.iosix.eldblelib.EldScanObject;
import com.iosix.eldblelib.EldTransmissionParametersRecord;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.utils.DateUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;


/***
 * 34.0218948
 * -118.498265
 */
public class BleConnect {
    private static final String TAG = BleConnect.class.getSimpleName();
    public static final int REQUEST_BASE = 100;
    public static final int REQUEST_BT_ENABLE = REQUEST_BASE + 1;

    private Activity a;

    private EldManager eldManager;

    private boolean reqDelinProgress = false;
    private boolean exit = false;
    private int updateSelection;
    private int startSeq;
    private int recCount = 0;
    private String mac;

    protected Date lastBleDataUpdate = null;

    private IOnBleDataUpdate onBleDataUpdate;

    protected BleConnect() {

    }

    public BleConnect(Activity a, IOnBleDataUpdate onBleDataUpdate) {
        this.onBleDataUpdate = onBleDataUpdate;
        this.a = a;

//        eldManager = EldManager.GetEldManager(this.a, "123456789A");
        eldManager = EldManager.GetEldManager(a, "123456789A");
    }

    public boolean isBluetoothNotEnabled() {
        EldBleError eldBleError = eldManager.ScanForElds(scanCallback);
        boolean isBluetoothNotEnabled = eldBleError == EldBleError.BLUETOOTH_NOT_ENABLED;
        return isBluetoothNotEnabled;
    }

    public boolean isBluetoothEnabled() {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        return mBluetoothAdapter.isEnabled();
    }



    //    Oct 04, 2022  -
//    This function is being called every 9 seconds so what happened if there is no  bluetooth than after every 9 seconds
//    we are generating a new dialog showing which keeps adding every 9 seconds if user did not respond or deny acces.
//    So we should not show dialog again if user gives his opinion already
    public void findAndConnectToBleDevice() {
        EldBleError eldBleError = eldManager.ScanForElds(scanCallback);
        boolean isBluetoothNotEnabled = eldBleError == EldBleError.BLUETOOTH_NOT_ENABLED;

        if (isBluetoothNotEnabled) {
            eldManager.EnableBluetooth(REQUEST_BT_ENABLE);
        }
    }

    public static boolean isBluetoothAvailable() {
        final BluetoothAdapter bluetoothAdapter =
                BluetoothAdapter.getDefaultAdapter();

        return (bluetoothAdapter != null &&
                bluetoothAdapter.isEnabled() &&
                bluetoothAdapter.getState() == BluetoothAdapter.STATE_ON);
    }

    public void scanForElds(int requestCode, int resultCode) {
        Log.d(TAG, "scanForElds: requestCode: " + requestCode + " resultCode: " + resultCode);
        if (requestCode == REQUEST_BT_ENABLE) {
//         Make sure the request was successful

            Log.d(TAG, "scanForElds: scanCallback: " + scanCallback);
            if (resultCode == Activity.RESULT_OK) {
                Log.d(TAG, "scanForElds: Activity.RESULT_OK");
                addDataInLogData("scanForElds: Bluetooth enabled - now scanning for ELD");
                onBleDataUpdate.OnBleStatus("Bluetooth enabled - now scanning for ELD\n");
                eldManager.ScanForElds(scanCallback);
            } else {
                Log.d(TAG, "scanForElds: scanForElds: Bluetooth enabled - now scanning for ELD");
                addDataInLogData("scanForElds: Bluetooth enabled - now scanning for ELD");
                onBleDataUpdate.OnBleStatus("Unable to enable bluetooth\n");
            }
        } else {
            Log.d(TAG, "scanForElds: scanForElds: " + "RequestCode: " + requestCode + ", ResultCode: " + resultCode);
            addDataInLogData("scanForElds: " + "RequestCode: " + requestCode + ", ResultCode: " + resultCode);
            onBleDataUpdate.OnBleStatus("RequestCode: " + requestCode + ", ResultCode: " + resultCode + "\n");
        }
    }

    public void rescanEldDeviceAndAutoConnect() {
        Log.d(TAG, "rescanEldDeviceAndAutoConnect: findAndConnectToBleDevice: ");
        findAndConnectToBleDevice();
    }

    public void disconnect() {
        EldBleError eldBleError = eldManager.DisconnectEld();
        onBleDataUpdate.OnBleStatus("Disconnected? " + (eldBleError == EldBleError.SUCCESS ? "Yes" : "Error: " + eldBleError.toString()));
    }

    public String getApiVersion() {
        return eldManager.GetApiVersion();
    }

    public void getFirmwareVersion() {
        final String Status = eldManager.CheckFirmwareUpdate();
        onBleDataUpdate.OnBleStatus("Current firmware: " + eldManager.GetFirmwareVersion() + " Available firmware: " + eldManager.CheckFirmwareUpdate() + "\r\n");
    }

    public void requestDebug() {
        EldBleError status = eldManager.RequestDebugData();
        onBleDataUpdate.OnBleStatus("Request Debug " + (status != EldBleError.SUCCESS ? "Failed" : "Succeeded") + "\n");
    }

    public void requestRecord() {
        final EldBleError status = eldManager.RequestRecord();
        onBleDataUpdate.OnBleStatus("ReqRecordStatus (" + status + ")\n");
    }

    public void enableFuelParameter() {
        final EldBleError status = eldManager.EnableAdditionalParameters(EldParameterTypes.FUEL_PARAMETERS);
        onBleDataUpdate.OnBleStatus("EnableFuelStatus (" + status + ")\n");
    }

    public void enableDtcParameters() {
        final EldBleError status = eldManager.EnableAdditionalParameters(EldParameterTypes.DIAGNOSTIC_PARAMETERS);
        //onBleDataUpdate.OnBleStatus("EnableDTCStatus (" + status + ")\n");

        eldManager.EnableDTCData(dtcCallback);
    }

    public void enableAllEldParameters() {
        EldBleError status0 = eldManager.EnableAdditionalParameters(EldParameterTypes.ENGINE_PARAMETERS);
        onBleDataUpdate.OnBleStatus("EnableEngineStatus (" + status0 + ")\n");

        EldBleError status1 = eldManager.EnableAdditionalParameters(EldParameterTypes.DRIVER_BEHAVIOR);
        onBleDataUpdate.OnBleStatus("EnableDriverStatus (" + status1 + ")\n");

        EldBleError status2 = eldManager.EnableAdditionalParameters(EldParameterTypes.TRANSMISSION_PARAMETERS);
        onBleDataUpdate.OnBleStatus("EnableTransmissionStatus (" + status2 + ")\n");

        EldBleError status3 = eldManager.EnableAdditionalParameters(EldParameterTypes.EMISSIONS_PARAMETERS);
        onBleDataUpdate.OnBleStatus("EnableEmissionsStatus (" + status3 + ")\n");

        EldBleError status4 = eldManager.EnableAdditionalParameters(EldParameterTypes.FUEL_PARAMETERS);
        onBleDataUpdate.OnBleStatus("EnableFuelStatus (" + status4 + ")\n");

        EldBleError status5 = eldManager.EnableAdditionalParameters(EldParameterTypes.DIAGNOSTIC_PARAMETERS);
        onBleDataUpdate.OnBleStatus("EnableDiagnosticsStatus (" + status5 + ")\n");
    }

    public void setPRecordingInterval(int pIntervalSecs) {
        EldBleError status = eldManager.SetRecordingInterval(pIntervalSecs * 1000);
        onBleDataUpdate.OnBleStatus("P60Status (" + status + ")\n");
    }

    /**
     * @param ctx application Context so method can access LOCATION_SERVICE
     * @return true if location services are enabled else false
     */
    public boolean areLocationServicesEnabled(Context ctx) {
        LocationManager locationManager = (LocationManager) ctx.getSystemService(Context.LOCATION_SERVICE);

        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isBleRequest(int requestCode) {
        return requestCode == REQUEST_BT_ENABLE;
    }

    public void requestBlePermissions() {
        ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
    }

    public boolean checkGrantResults(String[] permissions, int[] grantResults) {
        int granted = 0;

        if (grantResults.length > 0) {
            for (int i = 0; i < permissions.length; i++) {
                String permission = permissions[i];

                if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION) || permission.equals(Manifest.permission.ACCESS_COARSE_LOCATION))
                    if (grantResults[i] == PackageManager.PERMISSION_GRANTED)
                        granted++;
            }
        } else { // if cancelled
            return false;
        }

        return granted == 2;
    }

    public void setBleConnected() {
        onBleDataUpdate.OnBleStatus("Bluetooth enabled - now scanning for ELD\n");
    }

    //region Callbacks

    private EldDtcCallback dtcCallback = new EldDtcCallback() {
        @Override
        public void onDtcDetected(final String status, final String jsonString) {
            onBleDataUpdate.OnBleData("DTC: " + status);
            onBleDataUpdate.OnBleData("DTC Json: " + jsonString);
        }
    };

    private EldFirmwareUpdateCallback fwUpdateCallback = new EldFirmwareUpdateCallback() {
        @Override
        public void onUpdateNotification(final String status) {
            final String data = status;
            onBleDataUpdate.OnBleData(status + "\n");
        }
    };

    private EldBleDataCallback dataCallback = new EldBleDataCallback() {
        @Override
        public void OnDataRecord(final EldBroadcast dataRec, final EldBroadcastTypes RecordType) {
            lastBleDataUpdate = new Date();

            if (dataRec instanceof EldBufferRecord) {
                EldBufferRecord rec = (EldBufferRecord) dataRec;
                startSeq = ((EldBufferRecord) dataRec).getStartSeqNo();
                String broadcastStr = rec.getBroadcastString();

                //onBleDataUpdate.OnBleData("Buffer: start=" + rec.getStartSeqNo() + ", end=" + rec.getEndSeqNo() + ", rec type=" + rec.getRecType().toString() + ", broadcastStr=" + broadcastStr + "\n");
            } else if (RecordType == EldBroadcastTypes.ELD_DATA_RECORD) {
                EldDataRecord rec = (EldDataRecord) dataRec;

                onBleDataUpdate.OnBleData("EngineHours: " + rec.getEngineHours());
                onBleDataUpdate.OnBleData("TripDistance: " + rec.getTripDistance());
                onBleDataUpdate.OnBleData("Vin: " + rec.getVin());
                onBleDataUpdate.OnBleData("EngineState: " + rec.getEngineState());
                onBleDataUpdate.OnBleData("GpsSpeed: " + rec.getGpsSpeed());
                onBleDataUpdate.OnBleData("Latitude: " + rec.getLatitude());
                onBleDataUpdate.OnBleData("Longitude: " + rec.getLongitude());
                onBleDataUpdate.OnBleData("Speed: " + rec.getSpeed());
                onBleDataUpdate.OnBleData("Odometer: " + rec.getOdometer());
                onBleDataUpdate.OnBleData("TripDistance: " + rec.getTripDistance());
                onBleDataUpdate.OnBleData("TripHours: " + rec.getTripHours());
                onBleDataUpdate.OnBleData("RPM: " + rec.getRpm());

                String recStringRecord = "EngineHours: " + rec.getEngineHours() + " - " +
                        "TripDistance: " + rec.getTripDistance() + " - " +
                        "Vin: " + rec.getVin() + " - " +
                        "EngineState: " + rec.getEngineState() + " - " +
                        "GpsSpeed: " + rec.getGpsSpeed() + " - " +
                        "Latitude: " + rec.getLatitude() + " - " +
                        "Longitude: " + rec.getLongitude() + " - " +
                        "Speed: " + rec.getSpeed() + " - " +
                        "Odometer: " + rec.getOdometer() + " - " +
                        "TripDistance: " + rec.getTripDistance() + " - " +
                        "TripHours: " + rec.getTripHours() + " - " +
                        "RPM: " + rec.getRpm();
//                MainMenuActivity.logDataArrayList.add("EldBleDataCallback: rec: " + recStringRecord + "\n" +
//                        DateUtils.getNowYyyyMmDdHhmmss());
                addDataInLogData("EldBleDataCallback: rec: " + recStringRecord);


            } else if (RecordType == EldBroadcastTypes.ELD_CACHED_RECORD) {
                // Shows how to get to the specific record types created based on the broadcast info

                EldCachedDataRecord rec = (EldCachedDataRecord) dataRec;
                onBleDataUpdate.OnBleData(rec.getBroadcastString() + "\n");

                if (reqDelinProgress) {
                    // TESTING", "received " + recCount + " records

                    recCount++;

                    if (recCount == 10) {
                        eldManager.DeleteRecord(startSeq, startSeq + 9);    // TESTING delete " + startSeq + "-" + (startSeq + 9));
                        eldManager.RequestRecord(startSeq + 10);    // TESTING request + (startSeq + 10)
                    } else if (recCount == 11) {
                        // TESTING success!

                        reqDelinProgress = false;
                        recCount = 0;
                    }
                }

                if (rec instanceof EldCachedPeriodicRecord) {
                    onBleDataUpdate.OnBleData("Engine hours: " + rec.getEngineHours() + "\n");
                    onBleDataUpdate.OnBleData("Odometer: " + rec.getOdometer() + "\n");
                    onBleDataUpdate.OnBleData("RPM: " + rec.getRpm() + "\n");

                    ((EldCachedPeriodicRecord) (rec)).getUnixTime();
                    // mDataView.append("CACHED REC"+((EldCachedPeriodicRecord)(rec)).getBroadcastString());
                } else if (rec instanceof EldCachedNewTimeRecord) {
                    onBleDataUpdate.OnBleData("Engine hours: " + rec.getEngineHours() + "\n");
                    onBleDataUpdate.OnBleData("Odometer: " + rec.getOdometer() + "\n");
                    onBleDataUpdate.OnBleData("RPM: " + rec.getRpm() + "\n");

                    ((EldCachedNewTimeRecord) (rec)).getNewUnixTime();
                }
            } else if (RecordType == EldBroadcastTypes.ELD_DRIVER_BEHAVIOR_RECORD) {
                EldDriverBehaviorRecord rec = (EldDriverBehaviorRecord) dataRec;

                if (rec instanceof EldDriverBehaviorRecord) {
                    ((EldDriverBehaviorRecord) (rec)).getAbsStatus();
                    //((EldCachedPeriodicRecord)(rec)).getUnixTime();
                    // mStatusView.append("" + rec.getCruiseSetSpeed_kph() + " ");
                    // mStatusView.append("" + rec.getCruiseStatus() + " ");
                    // mStatusView.append("" + rec.getThrottlePosition_pct() + " ");
                    // mStatusView.append("" + rec.getAcceleratorPosition_pct() + " ");
                    // mStatusView.append("" + rec.getBrakePosition_pct() + " ");
                    // mStatusView.append("" + rec.getSeatBeltStatus() + " ");
                    // mStatusView.append("" + rec.getSteeringWheelAngle_deg() + " ");
                    // mStatusView.append("" + rec.getAbsStatus() + " ");
                    // mStatusView.append("" + rec.getTractionStatus() + " ");
                    // mStatusView.append("" + rec.getStabilityStaus() + " ");
                    // mStatusView.append("" + rec.getBrakeSystemPressure_kpa() + " ");
                    // mStatusView.append("\n");
                    // mDataView.append("CACHED REC"+((EldCachedPeriodicRecord)(rec)).getBroadcastString());
                }
            } else if (RecordType == EldBroadcastTypes.ELD_ENGINE_PARAMETERS_RECORD) {
                EldEngineParametersRecord rec = (EldEngineParametersRecord) dataRec;
                // mStatusView.append("" + rec.getOilPressure_kpa() + " ");
                // mStatusView.append("" + rec.getTurboBoost_kpa() + " ");
                // mStatusView.append("" + rec.getIntakePressure_kpa() + " ");
                // mStatusView.append("" + rec.getFuelPressure_kpa() + " ");
                // mStatusView.append("" + rec.getCrankCasePressure_kpa() + " ");
                // mStatusView.append("" + rec.getLoad_pct() + " ");
                // mStatusView.append("" + rec.getMassAirFlow_galPerSec() + " ");
                // mStatusView.append("" + rec.getTurboRpm() + " ");
                // mStatusView.append("" + rec.getIntakeTemp_c() + " ");
                // mStatusView.append("" + rec.getEngineCoolantTemp_c() + " ");
                // mStatusView.append("" + rec.getEngineOilTemp_c() + " ");
                // mStatusView.append("" + rec.getFuelTemp_c() + " ");
                // mStatusView.append("" + rec.getChargeCoolerTemp_c() + " ");
                // mStatusView.append("" + rec.getTorgue_Nm() + " ");
                // mStatusView.append("" + rec.getEngineOilLevel_pct() + " ");
                // mStatusView.append("" + rec.getEngineCoolandLevel_pct() + " ");
                // mStatusView.append("" + rec.getTripFuel_L() + " ");
                // mStatusView.append("" + rec.getDrivingFuelEconomy_LPerKm() + " ");
                // mStatusView.append("\n");
                // mDataView.append("Engine Rec was sent" + ((EldEngineParametersRecord) (rec)).getBroadcastString());
            } else if (RecordType == EldBroadcastTypes.ELD_EMISSIONS_PARAMETERS_RECORD) {
                EldEmissionsParametersRecord rec = (EldEmissionsParametersRecord) dataRec;
                // mStatusView.append("" + rec.getNOxInlet() + " ");
                // mStatusView.append("" + rec.getNOxOutlet() + " ");
                // mStatusView.append("" + rec.getAshLoad() + " ");
                // mStatusView.append("" + rec.getDpfSootLoad() + " ");
                // mStatusView.append("" + rec.getDpfRegenStatus() + " ");
                // mStatusView.append("" + rec.getDpfDifferentialPressure() + " ");
                // mStatusView.append("" + rec.getEgrValvePosition() + " ");
                // mStatusView.append("" + rec.getAfterTreatmentFuelPressure() + " ");
                // mStatusView.append("" + rec.getEngineExhaustTemperature() + " ");
                // mStatusView.append("" + rec.getExhaustTemperature1() + " ");
                // mStatusView.append("" + rec.getExhaustTemperature2() + " ");
                // mStatusView.append("" + rec.getExhaustTemperature3() + " ");
                // mStatusView.append("" + rec.getDefFluidLevel() + " ");
                // mStatusView.append("" + rec.getDefTankTemperature() + " ");
                // mStatusView.append("" + rec.getScrInducementFaultStatus() + " ");
                // mStatusView.append("\n");
            } else if (RecordType == EldBroadcastTypes.ELD_TRANSMISSION_PARAMETERS_RECORD) {
                EldTransmissionParametersRecord rec = (EldTransmissionParametersRecord) dataRec;
                // mStatusView.append("" + rec.getOutputShaftRpm() + " ");
                // mStatusView.append("" + rec.getGearStatus() + " ");
                // mStatusView.append("" + rec.getRequestGearStatus() + " ");
                // mStatusView.append("" + rec.getTransmissionOilTemp_c() + " ");
                // mStatusView.append("" + rec.getTorqueConverterLockupStatus() + " ");
                // mStatusView.append("" + rec.getTorqueConverterOilOutletTemp_c() + " ");
                // mStatusView.append("\n");
            } else if (RecordType == EldBroadcastTypes.ELD_FUEL_RECORD) {
                EldFuelRecord rec = (EldFuelRecord) dataRec;
                // mStatusView.append("" + rec.getFuelLevelPercent() + " ");
                // mStatusView.append("" + rec.getFuelIntegratedLiters() + " ");
                // mStatusView.append("" + rec.getTotalFuelConsumedLiters() + " ");
                // mStatusView.append("" + rec.getFuelRateLitersPerHours() + " ");
                // mStatusView.append("" + rec.getIdleFuelConsumedLiters() + " ");
                // mStatusView.append("" + rec.getIdleTimeHours() + " ");
                // mStatusView.append("" + rec.getStateHighRPM() + " ");
                // mStatusView.append("" + rec.getStateUnsteady() + " ");
                // mStatusView.append("" + rec.getStateEnginePower() + " ");
                // mStatusView.append("" + rec.getStateAccel() + " ");
                // mStatusView.append("" + rec.getStateEco() + " ");
                // mStatusView.append("" + rec.getStateAnticipate() + " ");
                // mStatusView.append("\n");
            }
        }
    };

    private EldBleConnectionStateChangeCallback connectionStateChangeCallback = new EldBleConnectionStateChangeCallback() {
        @Override
        public void onConnectionStateChange(final int newState) {
//            MainMenuActivity.logDataArrayList.add("EldBleConnectionStateChangeCallback: newState: " + newState + "\n" +
//                    DateUtils.getNowYyyyMmDdHhmmss());
            addDataInLogData("EldBleConnectionStateChangeCallback: newState: " + newState);
            if (newState == 0)
                onBleDataUpdate.OnBleStatus("New state of connection: " + Integer.toString(newState, 10) + "\n");

            //if (newState == 0)
            //eldManager.DisconnectEld(); // .closeBle();
        }
    };

    public EldBleScanCallback scanCallback = new EldBleScanCallback() {
        @Override
        public void onScanResult(EldScanObject device) {
//            MainMenuActivity.logDataArrayList.add("EldBleConnectionStateChangeCallback: device: " + device + "\n" +
//                    DateUtils.getNowYyyyMmDdHhmmss());;
            addDataInLogData("EldBleConnectionStateChangeCallback: device: " + device);
            String deviceId;

            if (device != null) {
                deviceId = device.getDeviceId();

                EldBleError result = eldManager.ConnectToELd(dataCallback, EnumSet.of(EldBroadcastTypes.ELD_BUFFER_RECORD,
                        EldBroadcastTypes.ELD_CACHED_RECORD, EldBroadcastTypes.ELD_DATA_RECORD), connectionStateChangeCallback);
//                MainMenuActivity.logDataArrayList.add("EldBleConnectionStateChangeCallback: result: " + result + "\n" +
//                        DateUtils.getNowYyyyMmDdHhmmss());
                addDataInLogData("EldBleConnectionStateChangeCallback: result: " + result);

                if (result != EldBleError.SUCCESS)
                    onBleDataUpdate.OnBleStatus("Connection failed");
                else
                    onBleDataUpdate.OnBleStatus("Now connected to ELD device " + deviceId);
            } else
                onBleDataUpdate.OnBleStatus("No ELD found");
        }

        @Override
        public void onScanResult(ArrayList deviceList) {
//            MainMenuActivity.logDataArrayList.add("EldBleScanCallback: onScanResult: deviceList: " + deviceList + "\n" +
//                    DateUtils.getNowYyyyMmDdHhmmss());
            addDataInLogData("EldBleScanCallback: onScanResult: deviceList: " + deviceList);
            if (deviceList != null) {
                EldScanObject so = (EldScanObject) deviceList.get(0);
                String deviceStr = so.getDeviceId();
                mac = deviceStr;

                EldBleError result = eldManager.ConnectToELd(dataCallback, EnumSet.of(EldBroadcastTypes.ELD_BUFFER_RECORD,
                        EldBroadcastTypes.ELD_CACHED_RECORD, EldBroadcastTypes.ELD_FUEL_RECORD, EldBroadcastTypes.ELD_DATA_RECORD,
                        EldBroadcastTypes.ELD_DRIVER_BEHAVIOR_RECORD, EldBroadcastTypes.ELD_EMISSIONS_PARAMETERS_RECORD,
                        EldBroadcastTypes.ELD_ENGINE_PARAMETERS_RECORD, EldBroadcastTypes.ELD_TRANSMISSION_PARAMETERS_RECORD),
                        connectionStateChangeCallback, deviceStr);
//                MainMenuActivity.logDataArrayList.add("EldBleScanCallback: onScanResult: result: " + result + "\n" +
//                        DateUtils.getNowYyyyMmDdHhmmss());;
                addDataInLogData("EldBleScanCallback: onScanResult: result: " + result);

                if (result != EldBleError.SUCCESS)
                    onBleDataUpdate.OnBleStatus("Connection failed");
                else
                    onBleDataUpdate.OnBleStatus("Now connected to ELD device " + deviceStr);
            } else
                onBleDataUpdate.OnBleStatus("No ELD found");
        }
    };

    //endregion

    private void requestDelSample() {
        // 1. request 10 records, for example from 1 to 10
        // 2. Wait until 10th record is received
        // 3. call deleteRecords method with parameters 1, 10
        // 4. request record 11 - In that case library does not return 11th record, need to request it few times or wait some time

        reqDelinProgress = true;
        recCount = 0;

        for (int i = startSeq; i < startSeq + 10; i++)
            Log.d("TESTING", "request " + i + " : " + eldManager.RequestRecord(i));
    }

    private void deleteRecordSample() {
        EldBleError status = eldManager.DeleteRecord(EldLatestRecords.getInstance().getLastBufferRecord().getStartSeqNo(),
                EldLatestRecords.getInstance().getLastBufferRecord().getStartSeqNo());

        onBleDataUpdate.OnBleStatus("DelRecordStatus (" + status + ")\n");
        //mEldManager.SetVin("12345678901234567");
    }

    public boolean isBleDataDeprecated() {
        Date nowLess30secs = new Date();
        nowLess30secs = DateUtils.addSecs(nowLess30secs, -30);

        return lastBleDataUpdate != null && nowLess30secs.after(lastBleDataUpdate);
    }

    public void resetBleDataCounters() {

    }

    void addDataInLogData(String data) {
//        MainMenuActivity.logDataArrayList.add(data + "\n" +
//                DateUtils.getNowYyyyMmDdHhmmss());
    }

}
