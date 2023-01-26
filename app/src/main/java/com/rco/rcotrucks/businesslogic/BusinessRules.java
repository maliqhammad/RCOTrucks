package com.rco.rcotrucks.businesslogic;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Environment;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.drive.DriveFragmentBase;
import com.rco.rcotrucks.activities.dvir.BusHelperDvir;
import com.rco.rcotrucks.activities.fuelreceipts.model.FuelReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.ifta.BusHelperIfta;
import com.rco.rcotrucks.activities.logbook.LogBookELDEvent;
import com.rco.rcotrucks.activities.logbook.model.GenericModel;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Driver;
import com.rco.rcotrucks.businesslogic.rms.Evaluation;
import com.rco.rcotrucks.businesslogic.rms.MapAsset;
import com.rco.rcotrucks.businesslogic.rms.ObjectInfo;
import com.rco.rcotrucks.businesslogic.rms.RestArea;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.Rule;
import com.rco.rcotrucks.businesslogic.rms.SettingNode;
import com.rco.rcotrucks.businesslogic.rms.Trailer;
import com.rco.rcotrucks.businesslogic.rms.TrailerLog;
import com.rco.rcotrucks.businesslogic.rms.Truck;
import com.rco.rcotrucks.businesslogic.rms.TruckEldDetail;
import com.rco.rcotrucks.businesslogic.rms.TruckEldHeader;
import com.rco.rcotrucks.businesslogic.rms.TruckLogDetail;
import com.rco.rcotrucks.businesslogic.rms.TruckLogHeader;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecRepairWorkRecTable;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordCommonHelper;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordCommonHelperTableRec;
import com.rco.rcotrucks.businesslogic.transferrods.CmvEnginePowerUpAndShutDownActivityItem;
import com.rco.rcotrucks.businesslogic.transferrods.DriversCertificationRecertificationActionItem;
import com.rco.rcotrucks.businesslogic.transferrods.EldCmvItem;
import com.rco.rcotrucks.businesslogic.transferrods.EldEventAnnotationCommentItem;
import com.rco.rcotrucks.businesslogic.transferrods.EldEventItem;
import com.rco.rcotrucks.businesslogic.transferrods.EldLoginLogoutReportItem;
import com.rco.rcotrucks.businesslogic.transferrods.EldUserItem;
import com.rco.rcotrucks.businesslogic.transferrods.MalfunctionsAndDataDiagnosticEventItem;
import com.rco.rcotrucks.businesslogic.transferrods.UnidentifiedDriverProfileRecordsItem;
import com.rco.rcotrucks.model.PretripModel;
import com.rco.rcotrucks.model.RoadTollModel;
import com.rco.rcotrucks.utils.ArrayUtils;
import com.rco.rcotrucks.utils.BuildUtils;
import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.MathUtils;
import com.rco.rcotrucks.utils.NetworkUtils;
import com.rco.rcotrucks.utils.SessionManagement;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.TextUtils;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.Vector;
import java.util.concurrent.locks.ReentrantLock;

import ca.rmen.sunrisesunset.SunriseSunset;

import static com.rco.rcotrucks.utils.StringUtils.isDouble;
import static java.lang.Math.max;
import static java.lang.Math.sqrt;

public class BusinessRules {
    private static BusinessRules instance;
    private boolean isDebugMode = BuildUtils.IS_DEBUG;
    private boolean isSimulatedBleDevice = true;

    protected final ReentrantLock lock = new ReentrantLock();
    private int deviceGPSSpeed = 0;
    Double deviceGPSLatitude = 0.0, deviceGPSLongitude = 0.0, eldDrivingSpeed = 0.0;
    Long eldIdleTimeStamp = 0L;
    String currentTruckLogHeaderCreationDateTime = null, odometerSinceShiftStarted = null;
    public boolean isBreakStarted = false;
    String mLineDelimitator = "\r";
    //    String mLineDelimitator = "\n";
    int mFileDataSum = 0;
    int nearestLoveDistance = 0, nearestPilotDistance = 0, nearestTADistance = 0, nearestRestAreaDistance = 0;

    // 2022.08.19 we need to save if the cycle was started
    private boolean newCycleStarted = false;

    Map<Integer, ArrayList<GenericModel>> maxSpeedArrayHashMap = new HashMap<>();
    ArrayList<Integer> steps = new ArrayList<>();

    public boolean isBreakStarted() {
        return isBreakStarted;
    }

    public void setBreakStarted(boolean breakStarted) {
        isBreakStarted = breakStarted;
    }

    public static synchronized BusinessRules instance() {
        if (instance == null)
            instance = new BusinessRules();

        return instance;
    }

//    March 11, 2022
//    Getting Speed, Latitude and Longitude from Device GPS STARTS

    public int getDeviceGPSSpeed() {
        return deviceGPSSpeed;
    }

    public void setDeviceGPSSpeed(int deviceGPSSpeed) {
        this.deviceGPSSpeed = deviceGPSSpeed;
//        Log.d(TAG, "recordTruckLogDrivingParameters: speed: " + deviceGPSSpeed);
    }

    public Double getDeviceGPSLatitude() {
        return deviceGPSLatitude;
    }

    public void setDeviceGPSLatitude(Double deviceGPSLatitude) {
        this.deviceGPSLatitude = deviceGPSLatitude;
//        Log.d(TAG, "recordTruckLogDrivingParameters: deviceGPSLatitude: " + deviceGPSLatitude);
    }

    public Double getDeviceGPSLongitude() {
        return deviceGPSLongitude;
    }

    public void setDeviceGPSLongitude(Double deviceGPSLongitude) {
        this.deviceGPSLongitude = deviceGPSLongitude;
//        Log.d(TAG, "recordTruckLogDrivingParameters: deviceGPSLongitude: " + deviceGPSLongitude);
    }

//    Getting Speed, Latitude and Longitude from Device GPS ENDS


    //region Database management

    protected static DatabaseHelper db;
    private static Context lastCtx;

    public static void openDatabase() {
        db.open();
    }

    public void instatiateDatabase(Context ctx) {
//        Log.d(TAG, "instatiateDatabase() Start.");

        ArrayList<String> tables = new ArrayList<String>();

        tables.add("settings");
        tables.add("users");
        tables.add("credentials");
        tables.add("userrights");
        tables.add("logmessages");
        tables.add("eldevents");
        tables.add("contentlines");
        tables.add("rmsrecords");
        tables.add("recordtypes");
        tables.add("codingmasterlookup");
        tables.add("codingdatasetup");
        tables.add("codingdata");
        tables.add("rmsdatatypes");
        tables.add("trucks");
        tables.add("trailers");
        tables.add("truckeldheader");
        tables.add("truckelddetail");
        tables.add("trucklogheader");
        tables.add("trucklogdetail");
        tables.add("driver");
        tables.add(BusHelperIfta.TABLE_IFTA_EVENT);
        tables.add("rules");
        tables.add("restareas");

//        May 25, 2022
        tables.add("trailerlog");

//        jan 16, 2022  -   I think I forget to add all these tables so added now
        tables.add("fuelreceipt");
        tables.add("tollreceipt");
        tables.add("pretrip");

        db = new DatabaseHelper(ctx, "rcotrucks.db", 5, tables, new String[]{
                "CREATE TABLE settings (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "key					    TEXT NOT NULL, " +
                        "value					    TEXT" +
                        ");",
                "CREATE TABLE users (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "rmsUserId				    TEXT NOT NULL, " +
                        "recordId				    TEXT, " +
                        "firstName				    TEXT NOT NULL, " +
                        "lastName				    TEXT NOT NULL, " +
                        "employeeId				    TEXT, " +
                        "itemType				    TEXT, " +
                        "trucknumber			    TEXT, " +
                        "trailernumber			    TEXT, " +
                        "trailer2Number			    TEXT " +
                        ");",
                "CREATE TABLE credentials (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "rmsUserId				    TEXT NOT NULL, " +
                        "username				    TEXT NOT NULL, " +
                        "password				    TEXT NOT NULL" +
                        ");",
                "CREATE TABLE userrights (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "rmsUserId				    TEXT NOT NULL, " +
                        "userright				    TEXT" +
                        ");",
                "CREATE TABLE logmessages (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "topic				        TEXT, " +
                        "msg				        TEXT, " +
                        "status				        TEXT, " +
                        "username				    TEXT, " +
                        "firstname				    TEXT, " +
                        "lastname				    TEXT, " +
                        "sent				        BIT " +
                        ");",
                "CREATE TABLE eldevents (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			        TEXT, " +
                        "objectType				    TEXT, " +
                        "recordId				    TEXT, " +
                        "mobileRecordId		        TEXT, " +
                        "organizationName		    TEXT, " +
                        "organizationNumber		    TEXT, " +
                        "eldUsername		        TEXT, " +
                        "eventType		            TEXT, " +
                        "eventCode		            TEXT, " +
                        "recordStatus		        TEXT, " +
                        "recordOrigin		        TEXT, " +
                        "truckNumber		        TEXT, " +
                        "vin		                TEXT, " +
                        "localizationDescription	TEXT, " +
                        "latitudeString		        TEXT, " +
                        "longitudeString		    TEXT, " +
                        "dstSinceLastValidCoords    TEXT, " +
                        "vehicleMiles		        TEXT, " +
                        "engineHours		        TEXT, " +
                        "orderNumbercmv		        TEXT, " +
                        "orderNumberUser		    TEXT, " +
                        "sequenceId		            TEXT, " +
                        "eventCodeDescription		TEXT, " +
                        "diagnosticIndicator		TEXT, " +
                        "malfunctionIndicator		TEXT, " +
                        "annotation		            TEXT, " +
                        "recordOriginId		        TEXT, " +
                        "checkData		            TEXT, " +
                        "checkSum		            TEXT, " +
                        "malfunctionDiagnosticCode  TEXT, " +
                        "malfunctionDiagnosticDescp TEXT, " +
                        "driverFirstName		    TEXT, " +
                        "driverLastName		        TEXT, " +
                        "driverRecordId		        TEXT, " +
                        "shiftstarteldid		    INTEGER, " +
                        "editReason		            TEXT, " +
                        "eventseconds		        Double, " +
                        "shiftstart		            TEXT, " +
                        "senteldevent		        BIT, " +
                        "senttrucklogs			    BIT, " +
                        "senteldlogs		        BIT, " +
                        "creationdate		        DATETIME, " +
                        "trucklogsobjectId			TEXT, " +
                        "trucklogsobjectType		TEXT, " +
                        "eldlogsobjectId			TEXT, " +
                        "eldlogsobjectType		    TEXT, " +
                        "odometer       		    TEXT," +
                        "rmsTimestamp				TEXT " +
                        ");",
                "CREATE TABLE contentlines (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			        TEXT, " +
                        "objectType				    TEXT, " +
                        "driverRecordId		        TEXT, " +
                        "csvline   				    TEXT, " +
                        "truckNumber   			    TEXT, " +
                        "date   				    TEXT, " +
                        "time   				    TEXT, " +
                        "speed   				    TEXT, " +
                        "latitude   			    TEXT, " +
                        "longitude   			    TEXT, " +
                        "accelerationx   		    TEXT, " +
                        "accelerationy   		    TEXT, " +
                        "accelerationz   		    TEXT, " +
                        "engineon   			    TEXT, " +
                        "odometer   			    TEXT, " +
                        "speedfromobd2orj1939       TEXT, " +
                        "engineHours   			    TEXT, " +
                        "status   				    TEXT, " +
                        "activeTruckRouteHeaderRecordId   				TEXT, " +
                        "eldlatitude   				TEXT, " +
                        "eldlongitude   			TEXT, " +
                        "speeding   				TEXT, " +
                        "trailer1   				TEXT, " +
                        "trailer2   				TEXT, " +
                        "logType				    TEXT, " +
                        "sent         		        BIT, " +
                        "creationdate		        DATETIME, " +
                        "parentMobileRecordId		        TEXT" +
                        ");",
                "CREATE TABLE trucks (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "recordId				    TEXT NOT NULL, " +
                        "mobileRecordId				TEXT, " +
                        "vin				        TEXT, " +
                        "vehicleLicenseNumber		TEXT, " +
                        "truckNumber			        TEXT, " +
                        "RmsCodingTimestamp				TEXT, " +
                        "RmsTimestamp					TEXT, " +
                        "OrganizationName				TEXT, " +
                        "OrganizationNumber				TEXT, " +
                        "ContainerTypeRecordId			TEXT, " +
                        "UniqueKey1						TEXT, " +
                        "Deployment						TEXT, " +
                        "IsaLocation					TEXT, " +
                        "CustomerRecordId				TEXT, " +
                        "ManufacturerSerialNumber		TEXT, " +
                        "Overdue						TEXT, " +
                        "StoreName						TEXT, " +
                        "StoreNumber					TEXT, " +
                        "StoreType						TEXT, " +
                        "DateLoaded						TEXT, " +
                        "DateUnloaded					TEXT, " +
                        "TimeLoaded						TEXT, " +
                        "TimeUnloaded					TEXT, " +
                        "FunctionalGroupName			TEXT, " +
                        "Managers						TEXT, " +
                        "FunctionalGroupObjectId		TEXT, " +
                        "DateDoorClosed					TEXT, " +
                        "TimeDoorClosed					TEXT, " +
                        "DateTime						TEXT, " +
                        "AlertCount						TEXT, " +
                        "VendorName						TEXT, " +
                        "Tracking						TEXT, " +
                        "ItemType						TEXT, " +
                        "Humidity						TEXT, " +
                        "GPSDailySampleRate				TEXT, " +
                        "GPSTripSampleRate				TEXT, " +
                        "TruckGPSServices				TEXT, " +
                        "FromLatitude					TEXT, " +
                        "FromLongitude					TEXT, " +
                        "ToLatitude						TEXT, " +
                        "ToLongitude					TEXT, " +
                        "DepartureDate					TEXT, " +
                        "DepartureTime					TEXT, " +
                        "EstimatedTimeEnroute			TEXT, " +
                        "EstimatedTimeOfArrival			TEXT, " +
                        "ActualTimeEnroute				TEXT, " +
                        "ActualTimeOfArrival			TEXT, " +
                        "TravelDangerAlertTime			TEXT, " +
                        "TravelWarningAlertTime			TEXT, " +
                        "OverdueTimeLimit				TEXT, " +
                        "SalesOrderNumber				TEXT, " +
                        "FromAddress					TEXT, " +
                        "FromCity						TEXT, " +
                        "FromState						TEXT, " +
                        "FromZipcode					TEXT, " +
                        "ToAddress						TEXT, " +
                        "ToCity							TEXT, " +
                        "ToState						TEXT, " +
                        "ToZipcode						TEXT, " +
                        "Make							TEXT, " +
                        "Mack							TEXT, " +
                        "Model							TEXT, " +
                        "Year							TEXT, " +
                        "LastWorked						TEXT, " +
                        "Country						TEXT, " +
                        "StateRegion					TEXT, " +
                        "City							TEXT, " +
                        "Company						TEXT, " +
                        "FirstName						TEXT, " +
                        "LastName						TEXT, " +
                        "MobilePhone					TEXT, " +
                        "Latitude						TEXT, " +
                        "Longitude						TEXT, " +
                        "Active							TEXT, " +
                        "Speed							TEXT, " +
                        "Temperature1					TEXT, " +
                        "Temperature2					TEXT, " +
                        "DoorStatus						TEXT, " +
                        "Heading						TEXT, " +
                        "Odometer						TEXT, " +
                        "EngineIdle						TEXT, " +
                        "RPM							TEXT, " +
                        "EngineHours					TEXT, " +
                        "OdometerSetup					TEXT, " +
                        "Shock							TEXT, " +
                        "TirePressures					TEXT, " +
                        "EngineStatus					TEXT, " +
                        "HOSViolation					TEXT, " +
                        "SpeedViolations				TEXT, " +
                        "SmogViolation					TEXT, " +
                        "LastMaintenanceDate			TEXT, " +
                        "LastAnnualInspectionDate		TEXT, " +
                        "Status							TEXT, " +
                        "CustomerName					TEXT, " +
                        "CustomerNumber					TEXT, " +
                        "FuelCode						TEXT, " +
                        "FuelType						TEXT, " +
                        "Diesel							TEXT, " +
                        "DOTNumber						TEXT, " +
                        "DOTExpirationDate				TEXT, " +
                        "IFTADecal						TEXT, " +
                        "IFTAFuelPermits				TEXT, " +
                        "IFTATripPermits				TEXT" +
                        ");",
                "CREATE TABLE trailers (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "recordId				    TEXT NOT NULL, " +
                        "mobileRecordId				TEXT, " +
                        "vin				        TEXT, " +
                        "trailerNumber		        TEXT NOT NULL, " +
                        "truckNumber			    TEXT, " +

//July 6 2022 add missing fields in the Trailer table
                        "make			             TEXT, " +
                        "model			             TEXT, " +
                        "year			             TEXT, " +
                        "lastWorked			    TEXT, " +
                        "country			    TEXT, " +
                        "stateRegion			    TEXT, " +
                        "city			    TEXT, " +
                        "company			    TEXT, " +
                        "processorId			    TEXT, " +
                        "processorInstalled			    TEXT, " +
                        "latitude			    TEXT, " +
                        "longitude			    TEXT, " +
                        "active			    TEXT, " +
                        "speed			    TEXT, " +
                        "temperature1			    TEXT, " +
                        "temperature2			    TEXT, " +
                        "humidity			    TEXT, " +
                        "doorStatus			    TEXT, " +
                        "heading			    TEXT, " +
                        "miles			    TEXT, " +
                        "fuelRate			    TEXT, " +
                        "shock			    TEXT, " +
                        "tirePressures			    TEXT, " +
                        "itemType			    TEXT, " +
                        "treads			    TEXT, " +
                        "lastMaintenanceDate			    TEXT, " +
                        "lastAnnualInspectionDate			    TEXT, " +
                        "status			    TEXT, " +
                        "customerName			    TEXT, " +
                        "customerNumber			    TEXT, " +
                        "isReefer			    TEXT, " +
                        "reeferHOS			    TEXT, " +
                        "reportingPeriod			    TEXT, " +
                        "hOSViolation			    TEXT, " +
                        "hasCamera			    TEXT, " +
                        "hasTracker			    TEXT, " +
                        "recordReeferHOS			    TEXT" +
                        ");",

                "CREATE TABLE trucklogheader (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			        TEXT, " +
                        "objectType				    TEXT, " +
                        "creationDate				TEXT, " +
                        "creationDatetime			TEXT, " +
                        "creationTime				TEXT, " +
                        "recordId					TEXT, " +
                        "mobileRecordId				TEXT, " +
                        "rmsTimestamp				TEXT, " +
                        "rmsEfileTimestamp			TEXT, " +
                        "rmsCodingTimestamp			TEXT, " +
                        "FunctionalGroupName		TEXT, " +
                        "functionalGroupObjectId	TEXT, " +
                        "creatorFirstName			TEXT, " +
                        "creatorLastName			TEXT, " +
                        "creatorRecordId			TEXT, " +
                        "organizationName			TEXT, " +
                        "organizationNumber			TEXT, " +
                        "offDutyHours				TEXT, " +
                        "sleeperHours				TEXT, " +
                        "drivingHours				TEXT, " +
                        "onDutyHours				TEXT, " +
                        "homeOfficeRecordId			TEXT, " +
                        "homeOfficeName				TEXT, " +
                        "homeOfficePhone			TEXT, " +
                        "firstName					TEXT, " +
                        "lastName					TEXT, " +
                        "userRecordId				TEXT, " +
                        "year						TEXT, " +
                        "vehicleLicenseNumber		TEXT, " +
                        "driver						TEXT, " +
                        "driverRecordId				TEXT, " +
                        "coDriver					TEXT, " +
                        "coDriverRecordId			TEXT, " +
                        "rule						TEXT, " +
                        "ruleDrivingDate			TEXT, " +
                        "active						TEXT, " +
                        "totalDistance				TEXT, " +
                        "speedViolations			TEXT, " +
                        "geofenceViolations			TEXT, " +
                        "startDate					TEXT, " +
                        "startTime					TEXT, " +
                        "endDate					TEXT, " +
                        "endTime					TEXT, " +
                        "itemType					TEXT, " +
                        "tripName					TEXT, " +
                        "overdueTimeLimit			TEXT, " +
                        "routeHeaderRecordId		TEXT, " +
                        "hoursRemaining				TEXT, " +
                        "weight						TEXT, " +
                        "lot						TEXT, " +
                        "truckNumber				TEXT, " +
                        "trailer1Number				TEXT, " +
                        "trailer2Number				TEXT, " +
                        "driverStatus				TEXT, " +
                        "sleeper					TEXT, " +
                        "previousTractors			TEXT, " +
                        "barcode			        TEXT, " +
                        "sent   			        BIT" +
                        ");",
                "CREATE TABLE trucklogdetail (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			        TEXT, " +
                        "objectType				    TEXT, " +
                        "recordId				    TEXT, " +
                        "mobileRecordId				TEXT, " +
                        "cycleStartDate				TEXT, " +
                        "startDate		            TEXT, " +
                        "cycleStartDateTime			TEXT, " +
                        "driverRecordId			    TEXT, " +
                        "trucklogheaderid		    TEXT, " +
                        "headeRecordId			    TEXT, " +
                        "masterBarcode			    TEXT, " +
                        "creationDate			    TEXT, " +
                        "creationDatetime			TEXT, " +
                        "cCreationTime			    TEXT, " +
                        "rmsTimestamp			    TEXT, " +
                        "rmsCodingTimestamp			TEXT, " +
                        "rmsEfileTimestamp			TEXT, " +
                        "functionalGroupName		TEXT, " +
                        "functionalGroupObjectId    TEXT, " +
                        "cycleEndDate			    TEXT, " +
                        "cCycleEndTime			    TEXT, " +
                        "cCycleStarDate			    TEXT, " +
                        "cycleStartTime			    TEXT, " +
                        "creatorFirstName			TEXT, " +
                        "creatorLastName			TEXT, " +
                        "creatorRecordId			TEXT, " +
                        "organizationName			TEXT, " +
                        "organizationNumber			TEXT, " +
                        "drivingHours			    TEXT, " +
                        "shiftHours			        TEXT, " +
                        "time						TEXT, " +
                        "shiftReset			        TEXT, " +
                        "cycleHours			        TEXT, " +
                        "cycleType			        TEXT, " +
                        "cycleReset			        TEXT, " +
                        "timeZone			        TEXT, " +
                        "vehicleLicenseNumber		TEXT, " +
                        "truckType			        TEXT, " +
                        "vin            			TEXT, " +
                        "loadDescription			TEXT, " +
                        "eventDescription			TEXT, " +
                        "eventDate			        TEXT, " +
                        "eventStart			        TEXT, " +
                        "eventDuration			    TEXT, " +
                        "eventStatus			    TEXT, " +
                        "eventLocation			    TEXT, " +
                        "eventNotes			        TEXT, " +
                        "carrier					TEXT, " +
                        "inspector			        TEXT, " +
                        "inspectionNotes			TEXT, " +
                        "startTime			        TEXT, " +
                        "endDateTime			    TEXT, " +
                        "endTime					TEXT, " +
                        "offDuty					TEXT, " +
                        "sleeper					TEXT, " +
                        "driving					TEXT, " +
                        "onDuty						TEXT, " +
                        "driver						TEXT, " +
                        "coDriver			        TEXT, " +
                        "equipmentInfoNumbers		TEXT, " +
                        "carrierName			    TEXT, " +
                        "carrierAddress			    TEXT, " +
                        "terminal			        TEXT, " +
                        "rule						TEXT, " +
                        "totalMilesthisCycle		TEXT, " +
                        "latitudes			        TEXT, " +
                        "longitudes			        TEXT, " +
                        "locationsDescriptions		TEXT, " +
                        "totalMilesToday			TEXT, " +
                        "coDriverRecordId			TEXT, " +
                        "totalDistance			    TEXT, " +
                        "speedViolations			TEXT, " +
                        "geofenceViolations			TEXT, " +
                        "activeDriver			    TEXT, " +
                        "shipmentInfo			    TEXT, " +
                        "itemType			        TEXT, " +
                        "overdue					TEXT, " +
                        "fromLatitude			    TEXT, " +
                        "fromLongitude			    TEXT, " +
                        "toLatitude			        TEXT, " +
                        "toLongitude			    TEXT, " +
                        "departureTime			    TEXT, " +
                        "departureDate			    TEXT, " +
                        "tripNumber			        TEXT, " +
                        "routeHeaderRecordId		TEXT, " +
                        "cycleEndDateTime			TEXT, " +
                        "truckNumber			    TEXT, " +
                        "odometerStart			    TEXT, " +
                        "odometerEnd			    TEXT, " +
                        "engineHours			    TEXT, " +
                        "originType			        TEXT, " +
                        "onDutyBreak			    TEXT, " +
                        "previousTractors			TEXT, " +
                        "headerRecordId			    TEXT, " +
                        "trailer1Number					TEXT, " + // 10.03.2022
                        "trailer2Number					TEXT, " +
                        "sent   			        BIT" +
                        ");",
                "CREATE TABLE truckeldheader (" +
                        "id								INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId						TEXT, " +
                        "objectType						TEXT, " +
                        "creationDatetime			    TEXT, " +
                        "creationDate					TEXT, " +
                        "creationTime					TEXT, " +
                        "recordId						TEXT, " +
                        "masterBarcode					TEXT, " +
                        "rmsTimestamp					TEXT, " +
                        "rmsCodingTimestamp			    TEXT, " +
                        "functionalGroupName			TEXT, " +
                        "functionalGroupObjectId		TEXT, " +
                        "mobileRecordId					TEXT, " +
                        "creatorFirstName			    TEXT, " +
                        "creatorLastName			    TEXT, " +
                        "creatorRecordId			    TEXT, " +
                        "organizationName			    TEXT, " +
                        "organizationNumber			    TEXT, " +
                        "vehicleLicenseNumber			TEXT, " +
                        "cycleStartDateTime			    TEXT, " +
                        "rule							TEXT, " +
                        "driverName						TEXT, " +
                        "driverRecordId					TEXT, " +
                        "driverId						TEXT, " +
                        "coDriverName					TEXT, " +
                        "coDriverRecordId			    TEXT, " +
                        "coDriverId						TEXT, " +
                        "truckLogHeaderRecordId			TEXT, " +
                        "truckNumber					TEXT, " +
                        "trailer1Number					TEXT, " +
                        "trailer2Number					TEXT, " +
                        "sent   						BIT" +
                        ");",
                "CREATE TABLE truckelddetail (" +
                        "id								INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId						TEXT, " +
                        "objectType						TEXT, " +
                        "masterBarcode					TEXT, " +
                        "creationDatetime				TEXT, " +
                        "creationDate					TEXT, " +
                        "creationTime					TEXT, " +
                        "recordId						TEXT, " +
                        "rmsTimestamp					TEXT, " +
                        "rmsCodingTimestamp				TEXT, " +
                        "rmsEfileTimestamp				TEXT, " +
                        "functionalGroupName			TEXT, " +
                        "functionalGroupObjectId		TEXT, " +
                        "mobileRecordId					TEXT, " +
                        "organizationName				TEXT, " +
                        "organizationNumber				TEXT, " +
                        "twentyFourHourPeriodStartingTime	TEXT, " +
                        "carrierName					TEXT, " +
                        "usDotNumber					TEXT, " +
                        "driverName						TEXT, " +
                        "driverId						TEXT, " +
                        "driverRecordId					TEXT, " +
                        "coDriverName					TEXT, " +
                        "coDriverId						TEXT, " +
                        "coDriverRecordId				TEXT, " +
                        "currentLocation				TEXT, " +
                        "dataDiagnosticsIndicators		TEXT, " +
                        "eldMalfunctionIndicators		TEXT, " +
                        "eldManufacturerName			TEXT, " +
                        "eldRegistrationId				TEXT, " +
                        "unidentifiedDriverRecords		TEXT, " +
                        "exemptDriverStatus				TEXT, " +
                        "milesToday						TEXT, " +
                        "printDisplayDate				TEXT, " +
                        "recordDate						TEXT, " +
                        "shippingId						TEXT, " +
                        "currentEngineHours				TEXT, " +
                        "engineHoursStart				TEXT, " +
                        "engineHoursEnd					TEXT, " +
                        "currentOdometer				TEXT, " +
                        "odometerStart					TEXT, " +
                        "odometerEnd					TEXT, " +
                        "timeZone						TEXT, " +
                        "truckNumber					TEXT, " +
                        "truckVIN						TEXT, " +
                        "trailerNumber					TEXT, " +
                        "truckLogDetailRecordId			TEXT, " +
                        "offDutyHours					TEXT, " +
                        "sleeperHours					TEXT, " +
                        "drivingHours					TEXT, " +
                        "onDutyHours					TEXT, " +
                        "status							TEXT, " +
                        "comments						TEXT, " +
                        "rule							TEXT, " +
                        "cycleStartDateTime				TEXT, " +
                        "vehicleLicenseNumber			TEXT, " +
                        "dataCheckValue					TEXT, " +
                        "certifyDateTime				TEXT, " +
                        "cmvPowerUnitNumber				TEXT, " +
                        "sent   						BIT, " +
                        "truckeldheaderid				TEXT" +
                        ");",
                "CREATE TABLE driver (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "rcoObjectId			    TEXT, " +
                        "rcoObjectType				TEXT, " +
                        "rcoRecordId				TEXT, " +
                        "rcoTimestamp		        TEXT, " +
                        "rcoMobileRecordId		    TEXT, " +
                        "organizationName		    TEXT, " +
                        "organizationNumber		    TEXT, " +
                        "role		                TEXT, " +
                        "login		                TEXT, " +
                        "password		            TEXT, " +
                        "userGroupName		        TEXT, " +
                        "company		            TEXT, " +
                        "surname		            TEXT, " +
                        "firstname		            TEXT, " +
                        "address1		            TEXT, " +
                        "address2		            TEXT, " +
                        "city		                TEXT, " +
                        "state		                TEXT, " +
                        "zip		                TEXT, " +
                        "country		            TEXT, " +
                        "email		                TEXT, " +
                        "phone		                TEXT, " +
                        "itemType		            TEXT, " +
                        "latitude		            TEXT, " +
                        "longitude		            TEXT, " +
                        "driverLicenseNumber		TEXT, " +
                        "driverLicenseState		    TEXT, " +
                        "location		            TEXT, " +
                        "customerNumber		        TEXT, " +
                        "dateOfHire		            TEXT, " +
                        "dateOfBirth		        TEXT, " +
                        "userType		            TEXT, " +
                        "employeeNumber		        TEXT, " +
                        "status		                TEXT, " +
                        "sex		                TEXT, " +
                        "nationalIdentifier		    TEXT, " +
                        "contactName		        TEXT, " +
                        "middleName		            TEXT, " +
                        "driverLicenseClass		    TEXT, " +
                        "endorsements		        TEXT, " +
                        "driverLicenseExpirationDate TEXT, " +
                        "dOTExpirationDate		    TEXT, " +
                        "correctiveLensRequired		TEXT, " +
                        "category1Name		        TEXT, " +
                        "category1Value		        TEXT, " +
                        "category2Name		        TEXT, " +
                        "category2Value		        TEXT, " +
                        "category3Name		        TEXT, " +
                        "category3Value		        TEXT, " +
                        "category4Name		        TEXT, " +
                        "category4Value		        TEXT, " +
                        "category5Name		        TEXT, " +
                        "category6Name		        TEXT, " +
                        "category6Value		        TEXT, " +
                        "mobilePhoneNumber		    TEXT, " +
                        "homePhoneNumber		    TEXT, " +
                        "workPhoneNumber		    TEXT, " +
                        "notifyGroupName		    TEXT, " +
                        "rating		                TEXT, " +
                        "numberOfReviews		    TEXT, " +
                        "quota		                TEXT, " +
                        "managerName		        TEXT, " +
                        "managerRecordId		    TEXT, " +
                        "managerEmployeeId		    TEXT, " +
                        "territory		            TEXT, " +
                        "rmsUserId		            TEXT, " +
                        "exemptDriverStatus		    TEXT, " +
                        "skilllevel		            TEXT, " +
                        "reportingperiod            TEXT, " +
                        "trucknumber			    TEXT, " +
                        "trailernumber			    TEXT, " +
                        "trailer2Number			    TEXT " +

                        ");",
                "CREATE TABLE rules (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			        TEXT, " +
                        "objectType				    TEXT, " +
                        "recordId				    TEXT, " +
                        "name					    TEXT, " +
                        "hours					    TEXT, " +
                        "days					    TEXT, " +
                        "hoursperday			    TEXT, " +
                        "itemType				    TEXT" +
                        ");",
                "CREATE TABLE " + BusHelperIfta.TABLE_IFTA_EVENT + " (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "IdRecordType               INTEGER, " +
                        "ObjectId			        TEXT, " +
                        "ObjectType				    TEXT, " +
                        "RecordId				    TEXT, " +
                        "rmsTimestamp		        TEXT, " +
                        "MobileRecordId		        TEXT, " +
                        "dateTime		            TEXT, " +
                        "jurisdictionId		        TEXT, " +
                        "odometer		            REAL, " +
                        "truckNumber		        TEXT, " +
                        "dotNumber		            TEXT, " +
                        "firstName		            TEXT, " +
                        "lastName		            TEXT, " +
                        "employeeId		            TEXT, " +
                        "company		            TEXT, " +
                        "state		                TEXT, " +
                        "country		            TEXT, " +
                        "vehicleLicenseNumber		TEXT, " +
                        "iftaYesOrNo                TEXT, " +
                        "tripPermitYesOrNo          TEXT, " +
                        "miles		                REAL, " +
                        "fuelCode		            TEXT, " +
                        "fuelType		            TEXT, " +
                        "iftaTaxExemptRoadYesOrNo   TEXT, " +
                        "status                     TEXT, " +
                        "odometerStart              REAL, " +
                        "longitude                  REAL, " +
                        "latitude                   REAL, " +
                        "sent				        INTEGER DEFAULT (" + Cadp.SYNC_STATUS_PENDING_UPDATE + "), " + // may need index to help syncing -- changing meaning of this to "SyncStatus"  0 means pending upsync, 1 means sync completed, 2 means marked for deletion.
                        "syncErrorCount             INTEGER," +
                        "LocalSysTime		        INTEGER, " + // May need index to help syncing
                        "IsValid                    INTEGER DEFAULT(1) " +
                        ");",
                "DROP INDEX IF EXISTS iftaevent_objidtype;",
                "CREATE UNIQUE INDEX iftaevent_objidtype " +
                        "ON iftaevent(ObjectType, ObjectId);",
                "DROP INDEX IF EXISTS iftaevent_mobrecid;",
                "CREATE UNIQUE INDEX iftaevent_mobrecid " +
                        "ON iftaevent(MobileRecordId);",
                "CREATE TABLE recordtypes (" +
                        "Id			                INTEGER NOT NULL PRIMARY KEY, " +
                        "RecordType                 TEXT," +
                        "ObjectType                 TEXT," +
                        "IsEfile                    INTEGER," +
                        "Category                   TEXT," +
                        "IdCategory                 INTEGER" +
                        ");",
                "CREATE TABLE codingdatasetup (" +
                        "RecordType                 TEXT, " +
                        "CodingFieldName            TEXT, " +
                        "DisplayName                TEXT, " +
                        "DisplayPosition            REAL, " +
                        "DataType                   TEXT, " +
                        "ViewType                   INTEGER, " +
                        "EditMode                   INTEGER DEFAULT(" + Cadp.EDIT_MODE_EDITABLE + "), " + // see Cadp.EDIT_MODE_... -- may not be used.
                        "IsRequired                 INTEGER DEFAULT(0), " + // 1=required, 0 not required.  Interpreted by usage, such as user entry, validation, etc.
                        "CombineType                INTEGER DEFAULT(" + Cadp.COMBINE_TYPE_NONE + "), " +
                        "SetterCsvColumnSort        INTEGER, " + // for sorting codingfields in a setter CSV file -- can be a relative column position.  May not be used.
                        "PRIMARY KEY(RecordType,CodingFieldName)" +
                        ");",
                "CREATE TABLE codingmasterlookup (" +
                        "CodingMasterId             TEXT PRIMARY KEY," +
                        "CodingFieldName            TEXT, " +
                        "DataType                   TEXT " +
                        ");",
                "DROP INDEX IF EXISTS codingmasterlookup_codingfieldname;",
                "CREATE UNIQUE INDEX codingmasterlookup_codingfieldname " +
                        "ON codingmasterlookup(CodingFieldName);",
                "CREATE TABLE rmsrecords (" +
                        "Id			                INTEGER NOT NULL PRIMARY KEY, " +
                        "IdRecordType               INTEGER, " +
                        "ObjectType                 TEXT, " +
                        "ObjectId                   TEXT, " +
                        "RecordId			        TEXT, " +
                        "MobileRecordId			    TEXT, " +
                        "MasterBarcode              TEXT, " +  // may not need this.  Used to relate details to header rec.
                        "RmsTimestamp		        INTEGER, " + // May need index to help syncing
                        "EfileContent               BLOB, " +
                        "IdRmsRecordsLink           INTEGER," +
                        "IsValid                    INTEGER DEFAULT(0), " +
                        "sent				        INTEGER DEFAULT (" + Cadp.SYNC_STATUS_PENDING_UPDATE + "), " + // may need index to help syncing -- changing meaning of this to "SyncStatus"  0 means pending upsync, 1 means sync completed, 2 means marked for deletion.
                        "IsEfileContentSent         INTEGER DEFAULT (" + Cadp.SYNC_STATUS_PENDING_UPDATE + "), " + // may need index to help syncing -- changing meaning of this to "SyncStatus"  0 means pending upsync, 1 means sync completed, 2 means marked for deletion.
                        "LocalSysTime		        INTEGER " + // May need index to help syncing
                        ");",
                "DROP INDEX IF EXISTS rmsrecords_objidtype;",
                "CREATE UNIQUE INDEX rmsrecords_objidtype " +
                        "ON rmsrecords(ObjectType, ObjectId);",
                "DROP INDEX IF EXISTS rmsrecords_mobrecid;",
                "CREATE UNIQUE INDEX rmsrecords_mobrecid " +
                        "ON rmsrecords(MobileRecordId);",
                "DROP INDEX IF EXISTS rmsrecords_link;",
                "CREATE INDEX rmsrecords_link " +
                        "ON rmsrecords(IdRmsRecordsLink);",
                "CREATE TABLE codingdata (" +
                        "Id			                INTEGER NOT NULL PRIMARY KEY, " +
                        "IdRmsRecords               INTEGER, " +
                        "CodingMasterId             TEXT, " +
                        "Value                      TEXT " +
                        ");",
                "DROP INDEX IF EXISTS codingdata_idrmsrec_cmid;",
                "CREATE UNIQUE INDEX codingdata_idrmsrec_cmid " +
                        "ON codingdata(idRmsRecords, codingMasterId);",
                "DROP INDEX IF EXISTS codingdata_value;",
                "CREATE INDEX codingdata_value " +
                        "ON codingdata(value);",
                "CREATE TABLE rmsdatatypes (" +
                        "TypeId			                TEXT PRIMARY KEY, " +
                        "DataTypeName                   TEXT, " +
                        "CodingDataColumn               TEXT " +
                        ");",
                "CREATE TABLE evaluations (" +
                        "Id			                    INTEGER NOT NULL PRIMARY KEY, " +
                        "LobjectId			            INTEGER, " +
                        "objectType                     TEXT, " +
                        "csvDataFilePath                TEXT, " +
                        "iCsvRow                        INTEGER, " +
                        "mobileRecordId                 TEXT, " +
                        "Driver_Mobile_Phone_Number     TEXT, " +
                        "Vehicle_Modifications          TEXT, " +
                        "Instructor_Last_Name           TEXT, " +
                        "Creation_Time                  TEXT, " +
                        "Vehicle_Model                  TEXT, " +
                        "Organization_Name              TEXT, " +
                        "States_of_operation            TEXT, " +
                        "Type_of_operation              TEXT, " +
                        "Driver_License_Number          TEXT, " +
                        "Driver_Address                 TEXT, " +
                        "Driver_City                    TEXT, " +
                        "Description_of_trailers        TEXT, " +
                        "Creation_Date                  TEXT, " +
                        "Vehicle_Transmission_Type      TEXT, " +
                        "Vehicle_Type_Steering_System   TEXT, " +
                        "Application_Type_Unilateral    TEXT, " +
                        "Sex                            TEXT, " +
                        "RMS_Efile_Timestamp            TEXT, " +
                        "RecordId                       TEXT, " +
                        "Organization_Number            TEXT, " +
                        "Date_of_Birth                  TEXT, " +
                        "Driver_Maiden_Name             TEXT, " +
                        "Vehicle_Year                   TEXT, " +
                        "RMS_Coding_Timestamp           TEXT, " +
                        "Driver_State                   TEXT, " +
                        "Driver_Home_Phone_Number       TEXT, " +
                        "Average_period_of_driving_time TEXT, " +
                        "ObjectName                     TEXT, " +
                        "Form_Date                      TEXT, " +
                        "Driver_Last_Name               TEXT, " +
                        "BarCode                        TEXT, " +
                        "Vehicle_Type_Brake_System      TEXT, " +
                        "Driver_Zipcode                 TEXT, " +
                        "Vehicle_Type                   TEXT, " +
                        "Vehicle_Number_of_Forward_Speeds  TEXT, " +
                        "Application_Type_Joint         TEXT, " +
                        "Type_of_cargo                  TEXT, " +
                        "Instructor_Driver_License_Number  TEXT, " +
                        "Vehicle_Seating_Capacity       TEXT, " +
                        "Vehicle_Rear_Axle_Speed        TEXT, " +
                        "Driver_License_State           TEXT, " +
                        "Vehicle_Make                   TEXT, " +
                        "Driver_First_Name              TEXT, " +
                        "Location                       TEXT, " +
                        "Type_of_prosthesis_worn        TEXT, " +
                        "Instructor_First_Name          TEXT, " +
                        "Driver_Signature_Date          TEXT, " +
                        "Description_of_impairment_or_amputation   TEXT, " +
                        "Number_of_years_driving_vehicle_type      TEXT, " +
                        "Number_of_Trailers             TEXT, " +
                        "Number_of_years_driving_all_vehicle_types  TEXT, " +
                        "RMS_Timestamp  TEXT ," +
                        "ItemType       TEXT ," +
                        "lastEmployerName                 TEXT, " +
                        "lastEmployerAddress     TEXT, " +
                        "lastEmployerCity          TEXT, " +
                        "lastEmployerState           TEXT, " +
                        "lastEmployerZipcode                  TEXT, " +
                        "lastEmployerTelephoneNumber                  TEXT, " +
                        "lastEmployerReasonsforleaving              TEXT, " +
                        "secondLastEmployerName            TEXT, " +
                        "secondLastEmployerAddress              TEXT, " +
                        "secondLastEmployerCity          TEXT, " +
                        "secondLastEmployerState                 TEXT, " +
                        "secondLastEmployerZipcode                    TEXT, " +
                        "secondLastEmployerTelephoneNumber        TEXT, " +
                        "secondLastEmployerReasonsforleaving                  TEXT, " +
                        "thirdLastEmployerName      TEXT, " +
                        "thirdLastEmployerAddress   TEXT, " +
                        "thirdLastEmployerCity    TEXT, " +
                        "thirdLastEmployerState                            TEXT, " +
                        "thirdLastEmployerZipcode            TEXT, " +
                        "thirdLastEmployerTelephoneNumber                       TEXT, " +
                        "thirdLastEmployerReasonsforleaving            TEXT, " +
                        "positionHeld1       TEXT, " +
                        "positionHeld2 TEXT, " +
                        "positionHeld3                     TEXT, " +
                        "lastEmployerFromDate                      TEXT, " +
                        "lastEmployerToDate               TEXT, " +
                        "secondLastEmployerFromDate                        TEXT, " +
                        "secondLastEmployerToDate      TEXT, " +
                        "thirdLastEmployerFromDate                 TEXT, " +
                        "thirdLastEmployerToDate                   TEXT ," +
                        "carrierName                 TEXT, " +
                        "doctorsName     TEXT, " +
                        "sPEApplicantName          TEXT, " +
                        "vehicleTypeStraightTruck           TEXT, " +
                        "vehicleTypeTruckTrailerover10klbs                  TEXT, " +
                        "vehicleTypeTrucklessthan10klbsandhazardousmaterials                  TEXT, " +
                        "vehicleTypeTruckover10klbs              TEXT, " +
                        "vehicleTypeMotorHome10klbs            TEXT, " +
                        "vehicleTypeTractorTrailer              TEXT, " +
                        "vehicleTypePassengerVehicle          TEXT, " +
                        "vehicleTypePassengerSeatingCapacity                 TEXT, " +
                        "vehicleTypePassengerMotorCoach                    TEXT, " +
                        "vehicleTypePassengerBus        TEXT, " +
                        "vehicleTypePassengerVan                  TEXT, " +
                        "vehicleTypeshortrelaydrives      TEXT, " +
                        "vehicleTypelongrelaydrives   TEXT, " +
                        "vehicleTypestraightthrough    TEXT, " +
                        "vehicleTypenightsawayfromhome                            TEXT, " +
                        "vehicleTypesleeperteamdrives            TEXT, " +
                        "vehicleTypenumberofnightsawayfromhome                       TEXT, " +
                        "vehicleTypeclimbinginandoutoftruck            TEXT, " +
                        "environmentalFactorsabruptduty       TEXT, " +
                        "environmentalFactorssleepdeprivation TEXT, " +
                        "environmentalFactorsunbalancedwork                     TEXT, " +
                        "environmentalFactorstemperature                      TEXT, " +
                        "environmentalFactorslongtrips               TEXT, " +
                        "environmentalFactorsshortnotice                        TEXT, " +
                        "environmentalFactorstightdelivery      TEXT, " +
                        "environmentalFactorsdelayenroute                 TEXT, " +
                        "environmentalFactorsothers                   TEXT ," +
                        "physicalDemandGearShifting           TEXT, " +
                        "physicalDemandNumberspeedtransmission                  TEXT, " +
                        "physicalDemandsemiautomatic                  TEXT, " +
                        "physicalDemandfullyautomatic              TEXT, " +
                        "physicalDemandsteeringwheelcontrol            TEXT, " +
                        "physicalDemandbrakeacceleratoroperation              TEXT, " +
                        "physicalDemandvarioustasks          TEXT, " +
                        "physicalDemandbackingandparking                 TEXT, " +
                        "physicalDemandvehicleinspections                    TEXT, " +
                        "physicalDemandcargohandling        TEXT, " +
                        "physicalDemandcoupling                  TEXT, " +
                        "physicalDemandchangingtires      TEXT, " +
                        "physicalDemandvehiclemodifications   TEXT, " +
                        "physicalDemandvehiclemodnotes    TEXT, " +
                        "muscleStrengthyesno    TEXT, " +
                        "muscleStrengthrightupperextremity                            TEXT, " +
                        "muscleStrengthleftupperextremity            TEXT, " +
                        "muscleStrengthrightlowerextremity                       TEXT, " +
                        "muscleStrengthleftlowerextremity            TEXT, " +
                        "mobilityyesno       TEXT, " +
                        "mobilityrightupperextremity TEXT, " +
                        "mobilityleftupperextremity                     TEXT, " +
                        "mobilityrightlowerextremity                      TEXT, " +
                        "mobilityleftlowerextremity               TEXT, " +
                        "mobilitytrunk                        TEXT, " +
                        "stabilityyesno      TEXT, " +
                        "stabilityrightupperextremity                 TEXT, " +
                        "stabilityleftupperextremity                   TEXT, " +
                        "stabilityrightlowerextremity                 TEXT, " +
                        "stabilityleftlowerextremity     TEXT, " +
                        "stabilitytrunk          TEXT, " +
                        "impairmenthand           TEXT, " +
                        "impairmentupperlimb                  TEXT, " +
                        "amputationhand                  TEXT, " +
                        "amputationpartial              TEXT, " +
                        "amputationfull            TEXT, " +
                        "amputationupperlimb              TEXT, " +
                        "powergriprightyesno          TEXT, " +
                        "powergripleftyesno                 TEXT, " +
                        "surgicalreconstructionyesno                    TEXT, " +
                        "hasupperimpairment        TEXT, " +
                        "haslowerlimbimpairment                  TEXT, " +
                        "hasrightimpairment      TEXT, " +
                        "hasleftimpairment   TEXT, " +
                        "hasupperamputation    TEXT, " +
                        "haslowerlimbamputation                            TEXT, " +
                        "hasrightamputation            TEXT, " +
                        "hasleftamputation                       TEXT, " +
                        "appropriateprosthesisyesno            TEXT, " +
                        "appropriateterminaldeviceyesno       TEXT, " +
                        "prosthesisfitsyesno TEXT, " +
                        "useprostheticproficientlyyesno                     TEXT, " +
                        "abilitytopowergraspyesno                      TEXT, " +
                        "prostheticrecommendations               TEXT, " +
                        "prostheticclinicaldescription                        TEXT, " +
                        "medicalconditionsinterferewithtasksyesno      TEXT, " +
                        "medicalconditionsinterferewithtasksexplanation                 TEXT, " +
                        "medicalfindingsandevaluation                   TEXT, " +
                        "physicianlastname           TEXT, " +
                        "physicianfirstname                  TEXT, " +
                        "physicianmiddlename                  TEXT, " +
                        "physicianaddress              TEXT, " +
                        "physiciancity            TEXT, " +
                        "physicianstate              TEXT, " +
                        "physicianzipcode          TEXT, " +
                        "physiciantelephonenumber                 TEXT, " +
                        "physicianalternatenumber                    TEXT, " +
                        "physiatrist        TEXT, " +
                        "orthopedicsurgeon                  TEXT, " +
                        "boardCertifiedyesno      TEXT, " +
                        "boardEligibleyesno   TEXT, " +
                        "physiciandate    TEXT, " +
                        "vehicleTypeslocaldeliveries                            TEXT, " +
                        "physicalDemandmountingsnowchains            TEXT, " +
                        "companyName            TEXT, " +
                        "companyAddress            TEXT, " +
                        "companyCity            TEXT, " +
                        "companyState            TEXT, " +
                        "companyZipcode            TEXT, " +
                        "firstName            TEXT, " +
                        "lastName            TEXT, " +
                        "maidenName            TEXT, " +
                        "middleName            TEXT, " +
                        "nameAddress            TEXT, " +
                        "nameCity            TEXT, " +
                        "nameState            TEXT, " +
                        "nameZip            TEXT, " +
                        "nameAddresshowLong            TEXT, " +
                        "nameBirthDate            TEXT, " +
                        "socialSercurityNumber            TEXT, " +
                        "pastThreeYearsAddress2            TEXT, " +
                        "pastThreeYearsCity2            TEXT, " +
                        "pastThreeYearsState2            TEXT, " +
                        "pastThreeYearsZipCode2            TEXT, " +
                        "pastThreeYearsHowLong2            TEXT, " +
                        "pastThreeYearsAddress3            TEXT, " +
                        "pastThreeYearsCity3            TEXT, " +
                        "pastThreeYearsState3            TEXT, " +
                        "pastThreeYearsZipCode3            TEXT, " +
                        "pastThreeYearsHowLong3            TEXT, " +
                        "driverName_0            TEXT, " +
                        "state_0            TEXT, " +
                        "licenseNumber_0            TEXT, " +
                        "licenseType_0            TEXT, " +
                        "expirationDate_0            TEXT, " +
                        "classofEquipment_0            TEXT, " +
                        "typeofEquipment_0            TEXT, " +
                        "dateFrom_0            TEXT, " +
                        "dateTo_0            TEXT, " +
                        "approximateNumberofMiles_0            TEXT, " +
                        "accidentDate_0            TEXT, " +
                        "natureofAccident_0            TEXT, " +
                        "fatalities_0            TEXT, " +
                        "injuries_0            TEXT, " +
                        "convictionLocation_0            TEXT, " +
                        "convictionDate_0            TEXT, " +
                        "charge_0            TEXT, " +
                        "penalty_0            TEXT, " +
                        "deniedalicenseYes            TEXT, " +
                        "deniedalicenseNo            TEXT, " +
                        "driverLicensePermitDenied            TEXT, " +
                        "driverLicensePermitRevokedorSuspended            TEXT, " +
                        "driverLicensePermitNotes            TEXT, " +
                        "employmentDate            TEXT, " +
                        "terminalCity            TEXT, " +
                        "terminalState            TEXT, " +
                        "driverLicenseExpirationDate            TEXT, " +
                        "violationsthisyear            TEXT, " +
                        "carrierAddress            TEXT, " +
                        "reviewedBy            TEXT, " +
                        "reviewedDate            TEXT, " +
                        "title            TEXT, " +
                        "violationDate            TEXT, " +
                        "violationOffense            TEXT, " +
                        "violationLocation            TEXT, " +
                        "violationtypeofvehicle            TEXT " +
                        ");",
                "CREATE TABLE trailerlog (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			        TEXT, " +
                        "objectType				    TEXT, " +
                        "recordId				    TEXT, " +
                        "mobileRecordId		        TEXT, " +
                        "organizationName		    TEXT, " +
                        "organizationNumber		    TEXT, " +
                        "dateTime   		        TEXT, " +
                        "truckNumber		        TEXT, " +
                        "trailerNumber		        TEXT, " +
                        "parentObjectId		        TEXT, " +
                        "parentObjectType		    TEXT, " +
                        "action		                TEXT, " +
                        "driverRecordId		        TEXT," +
                        "sent   					BIT" +
                        ");",
                "CREATE TABLE tollreceipt (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			        TEXT, " +
                        "objectType				    TEXT, " +
                        "recordId				    TEXT, " +
                        "mobileRecordId		        TEXT, " +
                        "organizationName		    TEXT, " +
                        "organizationNumber		    TEXT, " +
                        "dateTime   		        TEXT, " +
                        "truckNumber		        TEXT, " +
                        "userRecordId		        TEXT," +
                        "firstName  		        TEXT," +
                        "lastName     		        TEXT," +
                        "company     		        TEXT," +
                        "dotNumber     		        TEXT," +
                        "vehicleLicenseNumber     	TEXT," +
                        "vendorName     	        TEXT," +
                        "vendorState         	    TEXT," +
                        "vendorCountry       	    TEXT," +
                        "amount                	    TEXT," +
                        "roadName                	TEXT," +
                        "rmsTimestamp               TEXT," +
                        "sent   					BIT" +
                        ");",
                "CREATE TABLE fuelreceipt (" +
                        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			        TEXT, " +
                        "objectType				    TEXT, " +
                        "recordId				    TEXT, " +
                        "mobileRecordId		        TEXT, " +
                        "organizationName		    TEXT, " +
                        "organizationNumber		    TEXT, " +
                        "dateTime   		        TEXT, " +
                        "truckNumber		        TEXT, " +
                        "userRecordId		        TEXT," +
                        "firstName  		        TEXT," +
                        "lastName     		        TEXT," +
                        "company     		        TEXT," +
                        "dotNumber     		        TEXT," +
                        "vehicleLicenseNumber     	TEXT," +
                        "gallons                	TEXT," +
                        "amount                	    TEXT," +
                        "salesTax                	TEXT," +
                        "truckStop       	        TEXT," +
                        "state         	            TEXT," +
                        "country       	            TEXT," +
                        "fuelType                	TEXT," +
                        "odometer                	TEXT," +
                        "rmsTimestamp               TEXT," +
                        "sent   					BIT" +
                        ");",

                "CREATE TABLE pretrip (" +
                        "id						                                INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "objectId			                                    TEXT, " +
                        "objectType				                                TEXT, " +
                        "mobileRecordId		                                    TEXT, " +
                        "functionalGroupName			                        TEXT, " +
                        "organizationName		                                TEXT, " +
                        "organizationNumber		                                TEXT, " +// Organization Id (Check)
                        "dateTime   		                                    TEXT, " +
                        "latitude   		                                    TEXT, " +
                        "longitude   		                                    TEXT, " +
                        "firstName  		                                    TEXT," +    //Driver First Name
                        "lastName     		                                    TEXT," +    // Driver Last name
                        "recordId     		                                    TEXT," +    // Driver Record Id
                        "vehicleLicenseNumber     	                            TEXT," +
                        "airCompressor     	                                    TEXT," +
                        "airLines           	                                TEXT," +
                        "battery           	                                    TEXT," +//Missing
                        "brakeAccessories           	                        TEXT," +
                        "brakes           	                                    TEXT," +
                        "carburetor           	                                TEXT," +
                        "clutch           	                                    TEXT," +
                        "defroster           	                                TEXT," +
                        "driveLine           	                                TEXT," +
                        "fifthWheel           	                                TEXT," +
                        "frontalAxle           	                                TEXT," +
                        "fuelTanks           	                                TEXT," +
                        "heater           	                                    TEXT," +
                        "horn           	                                    TEXT," +
                        "lights           	                                    TEXT," +
                        "mirrors           	                                    TEXT," +
                        "oilPressure           	                                TEXT," +
                        "onBoardRecorder           	                            TEXT," +
                        "radiator           	                                TEXT," +
                        "rearEnd           	                                    TEXT," +
                        "reflectors           	                                TEXT," +
                        "safetyEquipment           	                            TEXT," +
                        "springs           	                                    TEXT," +
                        "starter           	                                    TEXT," +
                        "steering           	                                TEXT," +
                        "tachograph           	                                TEXT," +
                        "tires           	                                    TEXT," +
                        "transmission           	                            TEXT," +
                        "wheels           	                                    TEXT," +
                        "windows           	                                    TEXT," +
                        "windShieldWipers           	                        TEXT," +
                        "others           	                                    TEXT," +

                        "trailer1   				                            TEXT, " +
                        "trailer1BreakConnections   		                    TEXT, " +
                        "trailer1Breaks   				                        TEXT, " +
                        "trailer1CouplingPin   				                    TEXT, " +
                        "trailer1CouplingChains   			                    TEXT, " +
                        "trailer1Doors   				                        TEXT, " +
                        "trailer1Hitch   				                        TEXT, " +
                        "trailer1LandingGear   				                    TEXT, " +
                        "trailer1LightsAll   				                    TEXT, " +
                        "trailer1Roof   				                        TEXT, " +
                        "trailer1Springs   				                        TEXT, " +
                        "trailer1Tarpaulin   				                    TEXT, " +
                        "trailer1Tires   				                        TEXT, " +
                        "trailer1Wheels   				                        TEXT, " +
                        "trailer1Others   				                        TEXT, " +

                        "trailer2   				                            TEXT, " +
                        "trailer2BreakConnections   				            TEXT, " +
                        "trailer2Breaks   				                        TEXT, " +
                        "trailer2CouplingPin   				                    TEXT, " +
                        "trailer2CouplingChains   				                TEXT, " +
                        "trailer2Doors   				                        TEXT, " +
                        "trailer2Hitch   			                        	TEXT, " +
                        "trailer2LandingGear   				                    TEXT, " +
                        "trailer2LightsAll   				                    TEXT, " +
                        "trailer2Roof   				                        TEXT, " +
                        "trailer2Springs   				                        TEXT, " +
                        "trailer2Tarpaulin   				                    TEXT, " +
                        "trailer2Tires   				                        TEXT, " +
                        "trailer2Wheels   				                        TEXT, " +
                        "trailer2Others   				                        TEXT, " +

                        "remarks		                                        TEXT, " +
                        "conditionVehicleIsSatisfactory		                    TEXT," +
                        "driversSignatureVehicleSatisfactory     		        TEXT," +
                        "aboveDefectsCorrected     		                        TEXT," +
                        "aboveDefectsNoCorrectionNeeded     		            TEXT," +
                        "mechanicsSignatureDate     		                    TEXT," +
                        "driversSignatureNoCorrectionNeeded     		        TEXT," +
                        "driversSignatureNoCorrectionNeededDate     		    TEXT," +
                        "truckTractorNumber     		                        TEXT," +
                        "carrier     		                                    TEXT," +
                        "address     		                                    TEXT," +
                        "odometer     		                                    TEXT," +
                        "mechanicFirstName     		                            TEXT," +
                        "mechanicLastName     		                            TEXT," +
                        "mechanicRecordId     		                            TEXT," +
                        "trailer1ReeferHOS     		                            TEXT," +
                        "trailer2ReeferHOS     		                            TEXT," +
                        "registration     		                                TEXT," +
                        "insurance     		                                    TEXT," +

                        "rmsTimestamp               TEXT," +
                        "sent   					BIT" +
                        ");"

        });

//        Log.d(TAG, "instatiateDatabase() End.");
    }

    public static void closeDatabase() {
        db.close();
    }

    public boolean isInitDbFromRmsSuccess() {
        String s = getSetting("isInitDbFromRmsSuccess");
        if (!"true".equals(s)) return false;
        else return true;
    }

    public void setInitDbFromRmsSuccess(boolean isInitDbFromRmsSuccess) {
        setSetting("isInitDbFromRmsSuccess", String.valueOf(isInitDbFromRmsSuccess));
    }

    /**
     * A one-time initialization of RMS data that should not change unless the design is changed or
     * a different server is logged into.  Changing servers probably requires a raw sync of all the RMS data.
     * For now we can call this from the down-sync code conditionally if it is the same login that
     * a new database update is installed.  It is okay to initialize "one-way" data tables, meaning
     * data that originates on the mobile and is never created or modified on the server.
     * Tables that need to be synced both ways can be here if they are also be included in a separate "sync"
     * module.  Convenience members will be initialized regardless of whether a new db is installed or not.
     */
    public boolean initRmsCodingDataModel() {
        String strThis = "initRmsCodingDataModel, ";
//        Log.d(TAG, strThis + "start. db.isNewVersion()=" + db.isNewVersion());

        boolean isAllSuccess = true;
        BusHelperRmsCoding rulesRmsCoding = BusHelperRmsCoding.instance();
//        Log.d(TAG, strThis + "Before processing, isInitDbFromRmsSuccess()=" + isInitDbFromRmsSuccess());

//        Log.d(TAG, strThis + " performing some RMS one-time initializations.");

        try {
            // Todo: success granularity so only repeat previously unsuccessful initializations.
            isAllSuccess = isAllSuccess && rulesRmsCoding.loadDatabaseWithRmsCodingSetupData(getRmsRecordTypesUrlEncoded()); // --------------------->

            isAllSuccess = isAllSuccess &&
                    initCodingDataSetup(false);

//            Log.d(TAG, strThis + " isAllSuccess=" + isAllSuccess);
        } catch (Throwable e) {
            isAllSuccess = false;
            Log.w(TAG, strThis + " **** Error. ", e);
        }

//        BusHelperRmsCoding.instance().initConvenienceMembers();
//        BusHelperDvir.initRmsConvenienceMembers();
//        BusHelperFuelReceipts.initRmsConvenienceMembers();

//        Log.d(TAG, strThis + " end. isAllSuccess=" + isAllSuccess);
        return isAllSuccess;
    }

    /**
     * Todo: Need to review logic and meaning of "one time init".
     */
    public void checkInitRmsCodingDataModel() {
        if (!RecordCommonHelper.isOneTimeTableSetupDone) {

            if (Utils.isDebug(null, Utils.IS_DEBUG_INIT_DVIR_DB_SETUP, "Init DVIR setup forced on for debugging.")
                    || !isInitDbFromRmsSuccess()) {

                // Todo: how often should we initialize the CodingData model?  Just after install or database version change?
                boolean isSuccess = initRmsCodingDataModel();  // one-time init for new database or per login with debug.

                RecordCommonHelper.isOneTimeTableSetupDone = isSuccess;
                setInitDbFromRmsSuccess(isSuccess); // --------------------->
            }

            BusHelperRmsCoding.instance().initConvenienceMembers();
            BusHelperDvir.initRmsConvenienceMembers();
            BusHelperFuelReceipts.initRmsConvenienceMembers();
        }
    }

    public boolean initCodingDataSetup(boolean isAlreadyInTransaction) {
        boolean isSuccess;

        try {
            if (!isAlreadyInTransaction) db.beginTransaction();

            db.delete("codingdatasetup");

            isSuccess = BusHelperDvir.instance().initTruckDvirDetailSetup(isAlreadyInTransaction);

            isSuccess = isSuccess && BusHelperFuelReceipts.instance().initFuelReceiptCodingSetup(isAlreadyInTransaction);

            if (!isAlreadyInTransaction) db.setTransactionSuccessful();
        } catch (Throwable e) {
            isSuccess = false;
            Log.d(TAG, "initCodingDataSetup() **** Error. " + e, e);
        } finally {
            if (!isAlreadyInTransaction) db.endTransaction();
        }

        return isSuccess;
    }

    //endregion Database management

    // region Lookup Maps

    //    private static Map <String, String> mapDataTypeIdFromDataTypeName;
//    private static Map <String, String> mapDataTypeNameFromDataTypeId;
    private static Map<String, BusHelperRmsCoding.RmsDataType> mapDataTypeByName;
    private static Map<String, BusHelperRmsCoding.RmsDataType> mapDataTypeById;
    private static Object lockInitDataTypes = new Object();

//    public static Map<String, String> getMapDataTypeIdFromDataTypeName() {
//        if (mapDataTypeIdFromDataTypeName == null) initDataTypeMaps();
//        return mapDataTypeIdFromDataTypeName;
//    }
//
//    public static Map<String, String> getMapDataTypeNameFromDataTypeId() {
//        if (mapDataTypeNameFromDataTypeId == null) initDataTypeMaps();
//        return mapDataTypeNameFromDataTypeId;
//    }

    public static Map<String, BusHelperRmsCoding.RmsDataType> getMapDataTypeByName() {
        if (mapDataTypeByName == null) initDataTypeMaps();
        return mapDataTypeByName;
    }

    public static Map<String, BusHelperRmsCoding.RmsDataType> getMapDataTypeById() {
        if (mapDataTypeById == null) initDataTypeMaps();
        return mapDataTypeById;
    }

    private static void initDataTypeMaps() {
        String strThis = "initDataTypeMaps(), ";
//        Log.d(TAG, strThis + "Start.");

        synchronized (lockInitDataTypes) {
//            if (mapDataTypeIdFromDataTypeName != null && mapDataTypeNameFromDataTypeId != null) return;
            if (mapDataTypeByName != null && mapDataTypeById != null) return;
            Cursor cur = null;
            try {
                cur = db.getQuery("SELECT TypeId, DataTypeName, CodingDataColumn FROM rmsdatatypes");
//            if (mapDataTypeIdFromDataTypeName == null) mapDataTypeIdFromDataTypeName = new HashMap<>();
//            if (mapDataTypeNameFromDataTypeId == null) mapDataTypeNameFromDataTypeId = new HashMap<>();
                if (mapDataTypeByName == null) mapDataTypeByName = new HashMap<>();
                if (mapDataTypeById == null) mapDataTypeById = new HashMap<>();

                while (cur.moveToNext()) {
                    String typeId = cur.getString(0);
                    String dataTypeName = cur.getString(1);
                    String codingDataColumn = cur.getString(2);

//                mapDataTypeIdFromDataTypeName.put(dataTypeName, typeId);
//                mapDataTypeNameFromDataTypeId.put(typeId, dataTypeName);
                    BusHelperRmsCoding.RmsDataType dataType
                            = new BusHelperRmsCoding.RmsDataType(typeId, dataTypeName, codingDataColumn);
                    mapDataTypeByName.put(dataTypeName, dataType);
                    mapDataTypeById.put(typeId, dataType);

                }
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (cur != null && !cur.isClosed()) cur.close();
            }
//            mapDataTypeIdFromDataTypeName = Collections.unmodifiableMap(mapDataTypeIdFromDataTypeName);
//            mapDataTypeNameFromDataTypeId = Collections.unmodifiableMap(mapDataTypeNameFromDataTypeId);
            mapDataTypeByName = Collections.unmodifiableMap(mapDataTypeByName);
            mapDataTypeById = Collections.unmodifiableMap(mapDataTypeById);
        }

        Log.d(TAG, strThis + "End. mapDataTypeByName=" + mapDataTypeByName
                + "\n mapDataTypeById=" + mapDataTypeById);
    }

    private static Map<String, BusHelperRmsCoding.RmsRecordType> mapRecordTypeInfoFromObjectType;
    private static Map<String, BusHelperRmsCoding.RmsRecordType> mapRecordTypeInfoFromRecordTypeName;
    private static Object lockInitRecordTypes = new Object();

    public static Map<String, BusHelperRmsCoding.RmsRecordType> getMapRecordTypeInfoFromObjectType() {
        if (mapRecordTypeInfoFromObjectType == null) initRecordTypeMaps();
        return mapRecordTypeInfoFromObjectType;
    }

    public static Map<String, BusHelperRmsCoding.RmsRecordType> getMapRecordTypeInfoFromRecordTypeName() {
        if (mapRecordTypeInfoFromRecordTypeName == null) initRecordTypeMaps();
        return mapRecordTypeInfoFromRecordTypeName;
    }

    private static void initRecordTypeMaps() {
        String strThis = "initRecordTypeMaps(), ";
//        Log.d(TAG, strThis + "Start.");

        synchronized (lockInitRecordTypes) {
            if (mapRecordTypeInfoFromObjectType != null && mapCodingMasterIdByName != null) return;
            Cursor cur = null;

            try {
                cur = db.getQuery("SELECT Id, RecordType, ObjectType, IsEfile, Category, IdCategory FROM recordtypes");
                if (mapRecordTypeInfoFromObjectType == null)
                    mapRecordTypeInfoFromObjectType = new HashMap<>();
                if (mapRecordTypeInfoFromRecordTypeName == null)
                    mapRecordTypeInfoFromRecordTypeName = new HashMap<>();

                while (cur.moveToNext()) {
                    BusHelperRmsCoding.RmsRecordType r = new BusHelperRmsCoding.RmsRecordType();
                    r.id = cur.getInt(0);
                    r.recordTypeName = cur.getString(1);
                    r.objectType = cur.getString(2);
                    r.isEfileType = cur.getInt(3) == 1 ? true : false;
                    r.category = cur.getString(4);
                    r.idCategory = cur.getInt(5);

                    mapRecordTypeInfoFromObjectType.put(r.objectType, r);
                    mapRecordTypeInfoFromRecordTypeName.put(r.recordTypeName, r);
                }
            } catch (Throwable e) {
                Log.d(TAG, "initRecordTypeMaps() **** Error.", e);
            } finally {
                if (cur != null && !cur.isClosed()) cur.close();
            }

            mapRecordTypeInfoFromObjectType = Collections.unmodifiableMap(mapRecordTypeInfoFromObjectType);
            mapRecordTypeInfoFromRecordTypeName = Collections.unmodifiableMap(mapRecordTypeInfoFromRecordTypeName);
        }

//        Log.d(TAG, strThis + "End. mapRecordTypeInfoFromObjectType=" + mapRecordTypeInfoFromObjectType
//                + "\n mapRecordTypeInfoFromRecordTypeName=" + mapRecordTypeInfoFromRecordTypeName);

    }

    private static Map<String, String> mapCodingDataColumnByDataTypeLookup = null;
    private static Object lockCodingDataColumnByDataType = new Object();

    /**
     * Small map that relates an RMS datatype number to one of two columns in the RMS CodingData table
     * to help know which column holds the value for a codingfield of that datatype.
     * "CREATE TABLE rmsdatatypes (" +
     * "typeid			                TEXT PRIMARY KEY, " +
     * "DataTypeName                   TEXT " +
     * "CodingDataColumn               TEXT " +
     * ");"
     *
     * @return
     */
//    public static final Map<String, String> getMapCodingDataColumnByDataTypeLookup() {
//        if (mapCodingDataColumnByDataTypeLookup == null) {
//            initCodingDataColumnLookup();
//        }
//
//        return mapCodingDataColumnByDataTypeLookup;
//    }
//
//    private static void initCodingDataColumnLookup() {
//        synchronized (lockCodingDataColumnByDataType) {
//            if (mapCodingDataColumnByDataTypeLookup != null) return;
//
//            Cursor cur = null;
//
//            try {
//                mapCodingDataColumnByDataTypeLookup = new HashMap<>();
//                cur = db.getQuery("SELECT typeid, CodingDataColumn FROM RmsDataTypes");
//
//                while (cur.moveToNext())
//                    mapCodingDataColumnByDataTypeLookup.put(cur.getString(0), cur.getString(1));
//
//                mapCodingDataColumnByDataTypeLookup = Collections.unmodifiableMap(mapCodingDataColumnByDataTypeLookup);
//
//            } catch (Throwable e) {
//                e.printStackTrace();
//            } finally {
//                if (cur != null)
//                    cur.close();
//            }
//        }
//    }

    private static Map<String, String> mapCodingMasterIdByName;
    private static Map<String, String> mapCodingFieldNameByMasterId;
    private static Object lockInitCodingMaster = new Object();

    public static final Map<String, String> getMapCodingFieldNameByMasterId() {
        if (mapCodingFieldNameByMasterId == null)
            initCodingMasterLookupMaps();

        return mapCodingFieldNameByMasterId;
    }

    public static final Map<String, String> getMapCodingMasterIdByName() {
        if (mapCodingMasterIdByName == null)
            initCodingMasterLookupMaps();

        return mapCodingMasterIdByName;
    }

    public static void initCodingMasterLookupMaps() {
        synchronized (lockInitCodingMaster) {
            if (mapCodingMasterIdByName != null && mapCodingFieldNameByMasterId != null) return;
            mapCodingFieldNameByMasterId = new HashMap<>();
            mapCodingMasterIdByName = new HashMap<>();
            Cursor cur = null;

            try {
                cur = db.getQuery("SELECT * FROM codingmasterlookup");

                while (cur.moveToNext()) {
                    String strCmid = cur.getString(0);
                    String strFieldName = cur.getString(1);
                    mapCodingFieldNameByMasterId.put(strCmid, strFieldName);
                    mapCodingMasterIdByName.put(strFieldName, strCmid);
                }

                mapCodingFieldNameByMasterId = Collections.unmodifiableMap(mapCodingFieldNameByMasterId);
                mapCodingMasterIdByName = Collections.unmodifiableMap(mapCodingMasterIdByName);
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {
                if (cur != null && !cur.isClosed())
                    cur.close();
            }
        }
    }

    // endregion Lookup Maps

    //region Device Management

    private String deviceId;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(Context ctx) {
        deviceId = Utils.getDeviceId(ctx);
        Rms.setDeviceId(deviceId);
    }

    public boolean isTablet(Activity a) {
        DisplayMetrics metrics = new DisplayMetrics();
        a.getWindowManager().getDefaultDisplay().getMetrics(metrics);

        float yInches = metrics.heightPixels / metrics.ydpi;
        float xInches = metrics.widthPixels / metrics.xdpi;
        double diagonalInches = sqrt(xInches * xInches + yInches * yInches);

        return diagonalInches >= 6.5;
    }

    public boolean isWiFiConnection(Activity a) throws Exception {
        return NetworkUtils.isConnectionWiFi(a);
    }

    //endregion

    //region Dashboard management

    public boolean loadDynamicDashboard(Activity a, LinearLayout dashboard, int orientation) {
        boolean canProceed = false;
        Vector<Tuple<Integer, Boolean>> icons = getAllowedDashboardIconsArray();

        if (icons != null && !icons.isEmpty()) {
            int colsPerRow = 3;

            if (orientation != Configuration.ORIENTATION_PORTRAIT && orientation != Configuration.ORIENTATION_SQUARE)
                colsPerRow = 4;

            int rows = icons.size() / colsPerRow + (icons.size() % colsPerRow > 0 ? 1 : 0);
            dashboard.removeAllViews();

            for (int i = 0; i < rows; i++)
                dashboard.addView(getRowIcons(a, colsPerRow, i, icons));

            setDashboardBackground(a, orientation);
            canProceed = true;
        }

        return canProceed;
    }

    private void setDashboardBackground(Activity a, int orientation) {
        try {
            View viewDashboard = a.findViewById(R.id.background);

            String strWallpaperName = TextUtils.isNullOrWhitespaces(BusinessRules.instance().getSetting("BACKGROUND_NAME")) ?
                    "" : BusinessRules.instance().getSetting("BACKGROUND_NAME");

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                if (strWallpaperName.compareToIgnoreCase("Aluminum") == 0)
                    viewDashboard.setBackgroundResource(R.mipmap.wallpaper_aluminum_landscape);
                else if (strWallpaperName.compareToIgnoreCase("Oldpaper") == 0)
                    viewDashboard.setBackgroundResource(R.mipmap.wallpaper_oldpaper_landscape);
                else if (strWallpaperName.compareToIgnoreCase("Wood") == 0)
                    viewDashboard.setBackgroundResource(R.mipmap.wallpaper_wood_landscape);
                else if (strWallpaperName.compareToIgnoreCase("") != 0) {
                    Bitmap screenWallPaper = Bitmap.createBitmap(BitmapFactory.decodeFile(strWallpaperName));
                    viewDashboard.setBackgroundDrawable(new BitmapDrawable(screenWallPaper));
                }
            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                if (strWallpaperName.compareToIgnoreCase("Aluminum") == 0)
                    viewDashboard.setBackgroundResource(R.mipmap.wallpaper_aluminum_portrait);
                else if (strWallpaperName.compareToIgnoreCase("Oldpaper") == 0)
                    viewDashboard.setBackgroundResource(R.mipmap.wallpaper_oldpaper_portrait);
                else if (strWallpaperName.compareToIgnoreCase("Wood") == 0)
                    viewDashboard.setBackgroundResource(R.mipmap.wallpaper_wood_portrait);
                else if (strWallpaperName.compareToIgnoreCase("") != 0) {
                    Bitmap screenWallPaper = Bitmap.createBitmap(BitmapFactory.decodeFile(strWallpaperName));
                    viewDashboard.setBackgroundDrawable(new BitmapDrawable(screenWallPaper));
                }
            }
        } catch (Exception ex) {
            // Depending on the device we may run out of memory for large wallpapers
        }
    }

    private LinearLayout getRowIcons(Activity a, int cols, int row, Vector<Tuple<Integer, Boolean>> resIds) {
        HashMap<Integer, Integer> titles = getDashboardTitlesArray();

        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        params1.gravity = Gravity.CENTER_VERTICAL;
        params1.weight = 1;

        LinearLayout line = new LinearLayout(a);
        line.setLayoutParams(params1);
        line.setOrientation(LinearLayout.HORIZONTAL);

        for (int i = 0; i < cols; i++) {
            int resIdIndex = (row * cols) + i;
            int resId = -1;

            if (resIdIndex < resIds.size()) // There is no more resources to show
                resId = resIds.get(resIdIndex).getElement0();

            LinearLayout.LayoutParams params2 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

            params2.gravity = Gravity.CENTER;
            params2.weight = 1;

            LayoutInflater mInflator = LayoutInflater.from(a);
            View item = mInflator.inflate(R.layout.dashboard_item, null, false);
            item.setLayoutParams(params2);

            if (resId != -1) {
                ImageView img = item.findViewById(R.id.img_icon);
                img.setOnClickListener((View.OnClickListener) a);
                img.setImageResource(resId);
                img.setTag(resId);

                TextView tv = item.findViewById(R.id.txt_label);
                tv.setVisibility(View.VISIBLE);
                tv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);

                try {
                    tv.setText(titles.get(resId));
                } catch (Exception e) {
                    //Logger.logDebug("resId = " + resId);
                }
            }

            line.addView(item);
        }

        return line;
    }

    private Vector<Tuple<Integer, Boolean>> getAllowedDashboardIconsArray() {
        Vector<Tuple<Integer, Boolean>> allIcons = getDashboardIconsArray();

        if (allIcons == null)
            return null;

        Vector<Tuple<Integer, Boolean>> result = new Vector<Tuple<Integer, Boolean>>();

        for (Tuple<Integer, Boolean> icon : allIcons)
            if (icon.getElement1() == true) // Has right to access
                result.add(icon);

        return result;
    }

    //    Nov 30, 2022  -   Instructions from Roy emails
//    On the Sidebar menu change Pretrip Check to Pretrip. Change Sync Status to Sync.
//    Please make the menu items { Drive, Logbook, Pretrip, Receipts, Settings, Sync }. (Order should be)
    public List<Tuple<Integer, Integer>> getNavigationMenus() {
        List<Tuple<Integer, Integer>> menus = new ArrayList<>();

        if (existsUserRight("Mobile-DisplayTrucks")) {
            menus.add(new Tuple<>(R.string.dashboard_title_trucks, R.drawable.icon_truck));
        } else {

            menus.add(new Tuple<>(R.string.dashboard_title_drive, R.drawable.icon_drive_new));
            if (existsUserRight("Mobile-DisplayLogbook")) {
                menus.add(new Tuple<>(R.string.dashboard_title_log_book, R.drawable.icon_logbook_new));
            }
            menus.add(new Tuple<>(R.string.dashboard_title_dvir, R.drawable.icon_dvir_new));
            menus.add(new Tuple<>(R.string.dashboard_title_dvir_update, R.drawable.ic_baseline_checklist_24));
            menus.add(new Tuple<>(R.string.dashboard_title_receipts, R.drawable.ic_baseline_receipt));
            menus.add(new Tuple<>(R.string.dashboard_title_settings, R.drawable.icon_driversettings_new));

            if (existsUserRight("Mobile-DisplaySyncStatus"))
                menus.add(new Tuple<>(R.string.dashboard_title_syncstatusyellow, R.drawable.icon_sync_new));
            if (existsUserRight("Mobile-DisplaySyncNow"))
                menus.add(new Tuple<>(R.string.dashboard_title_sync_now, R.drawable.icon_sync_new));
            if (existsUserRight("Mobile-Displaysettings"))
                menus.add(new Tuple<>(R.string.dashboard_title_settings, R.drawable.icon_forms_new));

//            Jan 13, 2022  -   Today we added two new features(fragments) feedback and help
            menus.add(new Tuple<>(R.string.dashboard_title_feedback, R.drawable.ic_baseline_message_24));
            menus.add(new Tuple<>(R.string.dashboard_title_help, R.drawable.ic_baseline_help_24));


            //  menus.add(new Tuple<>(R.string.dashboard_title_annual_inspection, R.drawable.ic_annual_inspection));
            //       menus.add(new Tuple<>(R.string.dashboard_title_bol, R.drawable.ic_bol));
            //       menus.add(new Tuple<>(R.string.dashboard_title_camerarear, R.drawable.ic_camerarear));
            //       menus.add(new Tuple<>(R.string.dashboard_title_delivery, R.drawable.ic_delivery));
//            Oct 31, 2022  -   Replaced "Drive" title with "Map"
//            Nove 04, 2022 -   Change back to "Drive" from "Map"
//            menus.add(new Tuple<>(R.string.dashboard_title_map, R.drawable.icon_drive_new));
//            July 12, 2022 -   Roy said comment it
//            menus.add(new Tuple<>(R.string.dashboard_title_forms, R.drawable.icon_forms));
//            Nov 29, 2022  -   We have added fuel receipt into receipts screen now
//            and now this new receipts screen holds info about both fuel and toll receipts so
//            thats why removing this previous fuel receipt class
//            menus.add(new Tuple<>(R.string.dashboard_title_fuel_receipts, R.drawable.icon_fuel_new));
//            June 16, 2022 -   We have gauges on tablet and mobile as well so don't need it - Roy
//            menus.add(new Tuple<>(R.string.dashboard_title_gauges, R.drawable.icon_gauge));
//            menus.add(new Tuple<>(R.string.dashboard_title_streaming, R.drawable.camera));
//            July 12, 2022 -   Roy said comment it
//            menus.add(new Tuple<>(R.string.dashboard_title_streaming, R.drawable.ic_baseline_videocam_24));
//            July 22, 2022 -   Roy said comment it
//            menus.add(new Tuple<>(R.string.dashboard_title_ifta, R.drawable.ic_ifta));
        }

        return menus;
    }

    @SuppressLint("UseSparseArrays")
    private HashMap<Integer, Integer> getDashboardTitlesArray() {
        HashMap<Integer, Integer> result = new HashMap<Integer, Integer>();

        result.put(R.drawable.ic_annual_inspection, R.string.dashboard_title_annual_inspection);
        result.put(R.drawable.ic_bol, R.string.dashboard_title_bol);
        result.put(R.drawable.ic_camerarear, R.string.dashboard_title_camerarear);

        result.put(R.drawable.ic_delivery, R.string.dashboard_title_delivery);
        result.put(R.drawable.ic_drivecmd, R.string.dashboard_title_drive);
        result.put(R.drawable.ic_dvir, R.string.dashboard_title_dvir);
        result.put(R.drawable.ic_ifta, R.string.dashboard_title_ifta);

        result.put(R.drawable.ic_usersettings, R.string.dashboard_title_driver_settings);
        result.put(R.drawable.ic_fuelreceipt, R.string.dashboard_title_fuel_receipts);
        result.put(R.drawable.ic_gauges, R.string.dashboard_title_gauges);

        result.put(R.drawable.syncstatusyellow, R.string.dashboard_title_syncstatusyellow);
        result.put(R.drawable.sync_now, R.string.dashboard_title_sync_now);
        result.put(R.drawable.settings, R.string.dashboard_title_settings);
        result.put(R.drawable.logbook, R.string.dashboard_title_log_book);

        return result;
    }

    private Vector<Tuple<Integer, Boolean>> getDashboardIconsArray() {
        if (savedIcons == null) {
            Vector<Tuple<Integer, Boolean>> result = new Vector<Tuple<Integer, Boolean>>();

            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_annual_inspection, true));
            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_bol, true));
            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_camerarear, true));

            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_delivery, true));
            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_drivecmd, true));
            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_dvir, true));
            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_ifta, true));

            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_usersettings, true));
            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_fuelreceipt, true));
            result.add(new Tuple<Integer, Boolean>(R.drawable.ic_gauges, true));

            result.add(new Tuple<Integer, Boolean>(R.drawable.syncstatusyellow, existsUserRight("Mobile-DisplaySyncStatus")));
            result.add(new Tuple<Integer, Boolean>(R.drawable.sync_now, existsUserRight("Mobile-DisplaySyncNow")));
            result.add(new Tuple<Integer, Boolean>(R.drawable.settings, existsUserRight("Mobile-Displaysettings")));
            result.add(new Tuple<Integer, Boolean>(R.drawable.logbook, existsUserRight("Mobile-DisplayLogbook")));

            savedIcons = result;
        }

        return savedIcons;
    }

    public LinearLayout findDashboardItemByTitle(LinearLayout dashboard, String dashboardItemTitle) {
        for (int i = 0; i < dashboard.getChildCount(); i++) {
            LinearLayout row = (LinearLayout) dashboard.getChildAt(i);

            for (int j = 0; j < row.getChildCount(); j++) {
                LinearLayout rowItem = (LinearLayout) row.getChildAt(j);
                TextView itemTitle = (TextView) rowItem.getChildAt(1);

                if (itemTitle.getText().toString().equalsIgnoreCase(dashboardItemTitle))
                    return rowItem;
            }
        }

        return null;
    }

    private static Vector<Tuple<Integer, Boolean>> savedIcons = null;

    //endregion

    //region Login management

    public boolean existsLoggedInUser() {
        return user != null;
    }

    public boolean existsLastLoggedInUsername() {
        String username = getLastLoggedInUsername();
        return username.length() > 0;
    }

    public String getLastLoggedInUsername() {
        String lastLoggedInUsername = getSetting("lastloggedinusername");

        if (lastLoggedInUsername == null)
            return "";

        return lastLoggedInUsername.trim();
    }

    public void setLastLoggedInUsername(String value) {
        if (value == null) {
//            Log.d(TAG, "setLastLoggedInUsername() >>>" + value);
            return;
        }

        setSetting("lastloggedinusername", value);
    }

    public String getLastLoggedInUserPassword() {
        String password = getSetting("lastloggedinuserpassword");

        if (password == null)
            return "";

        return password.trim();
    }

    public void setLastLoggedInUserPassword(String value) {
        setSetting("lastloggedinuserpassword", value);
    }

    public void clearLastLoggedInUserPassword() {
        setSetting("lastloggedinuserpassword", "");
    }

    public void switchRememberUserPassword() {
        boolean rememberPassword = rememberUserPassword();
        setSetting("rememberuserpassword", !rememberPassword ? "true" : "false");
    }

    public boolean rememberUserPassword() {
        try {
            String rememberPasswordStr = getSetting("rememberuserpassword");
            return rememberPasswordStr != null && rememberPasswordStr.equalsIgnoreCase("true");
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return false;
        }
    }

    public boolean showPassword() {
        String showPasswordStr = getSetting("showpassword");
        return showPasswordStr != null && showPasswordStr.equalsIgnoreCase("true");
    }

    public void switchShowPassword() {
        boolean showPassword = showPassword();
        setSetting("showpassword", !showPassword ? "true" : "false");
    }

    public void deleteRecord(ObjectInfo o) throws Exception {
        /*if (StringUtils.isNullOrWhitespaces(o.getObjectType()))
            o.setObjectType("NRT362");*/

        Rms.deleteRecord(o);
    }

    public String getDrivingStatus() {
//        March 15, 2022    -   We should use new way to get the most recent driving event
//        return user.getDrivingStatus();
        return getMostRecentDutyState();
    }

    public void setDrivingStatus(String v) {
        user.setDrivingStatus(v);
    }

    //endregion

    //region General
    public boolean isItNight(LatLng latLng) {
        TimeZone timeZone = TimeZone.getDefault();
        Calendar nowCalendar = Calendar.getInstance(timeZone);
        Calendar[] sunriseSunset = SunriseSunset.getSunriseSunset(nowCalendar, latLng.latitude, latLng.longitude);

        Calendar sunrise = sunriseSunset[0];
//        sunrise.add(Calendar.MINUTE, -30);
        sunrise.add(Calendar.MINUTE, 0);

        Calendar sunset = sunriseSunset[1];
//        sunset.add(Calendar.MINUTE, 30);
        sunset.add(Calendar.MINUTE, 0);

        return nowCalendar.getTime().after(sunset.getTime()) || nowCalendar.getTime().before(sunrise.getTime());
    }

    //endregion

    //region Logging, settings, preferences

    // Settings

    public String getSetting(String key) {
        Log.d(TAG, "getSetting: key: " + key);
        lock.lock();
        Cursor c = null;

        try {
            Log.d(TAG, "getSetting: ");
            if (!db.exists("SELECT * FROM settings WHERE key='" + key + "'")) {
                Log.d(TAG, "getSetting: return null: ");
                return null;
            }

            String result = null;
            c = db.getQuery("SELECT * FROM settings WHERE key='" + key + "'");
            Log.d(TAG, "getSetting: cursor: " + c);
            c.moveToFirst();

            while (!c.isAfterLast()) {
                result = c.getString(2);
                c.moveToNext();
            }

//            c.close();
            //Log.d(TAG, "getSetting() key=" + key + ", value: " + result);
            Log.d(TAG, "getSetting: result: " + result);
            return result;
        } finally {
            Log.d(TAG, "getSetting: finally block: ");
            if (c != null && !c.isClosed()) c.close();
            lock.unlock();
        }
    }

    public void setSetting(String key, String value) {
        lock.lock();
        Log.d(TAG, "setSetting: key: " + key + " value: " + value);
        try {
            boolean existsSetting = db.exists("SELECT * FROM settings WHERE key='" + key + "'");
            ContentValues values = new ContentValues();

            Log.d(TAG, "setSetting: existsSetting: " + existsSetting);
            int isInserted = -2;
            if (existsSetting) {
                values.put("value", value);

                Log.d(TAG, "setSetting: update:");
                isInserted = db.update("settings", values, "key='" + key + "'");
            } else {
                values.put("key", key);
                values.put("value", value);

                Log.d(TAG, "setSetting: insert: ");
                db.insert("settings", values);
            }
            Log.d(TAG, "setSetting: isInserted: " + isInserted);
        } finally {
            lock.unlock();
        }
    }

    public boolean isEldTesting() {
//      March 21, 2022    -   We should use the testing coding field from database settings
        String isTesting = getSetting("database.ELD Testing");
        if (isTesting != null && isTesting.equalsIgnoreCase("yes")) {
            return true;
        }
        return false;
    }

    public String getStreamingUrl() {
        String streamingUrl = getSetting("database.CameraURL");
        return streamingUrl;
    }

    //endregion

    //region Debug mode

    public boolean isProductionMode() {
        return !isDebugMode();
    }

    public boolean isDebugMode() {
        return isDebugMode;
    }

    public boolean isFernandosDebuggingDevices() {
        if (isFernandosDebuggingEmulators())
            return true;

        String deviceId = getDeviceId();

        return deviceId != null && deviceId.equalsIgnoreCase("8a06a7ad5c5cf438");
    }

    public boolean isFernandosDebuggingEmulators() {
        String deviceId = getDeviceId();

        return deviceId != null && (
                deviceId.equalsIgnoreCase("8a5699d3127dd36d") ||
                        deviceId.equalsIgnoreCase("45e1470d1988a7ee") ||
                        deviceId.equalsIgnoreCase("1e41fb89adc960fe") ||
                        deviceId.equalsIgnoreCase("5dd48530d9545271")
//                        deviceId.equalsIgnoreCase("fb680e65cab779aa")
        );
    }

    public String getFernandosDebuggingUserPass() {
        return "fernandoa";
    }

    public String getDebugUsername() {
        return "dee";
    }

    public String getDebugPassword() {
        return "123456789";
    }

    //endregion

    //region Server URLs

    private ArrayList<ServerUrl> serverUrls;

    public void setServerUrls() {
        serverUrls = new ArrayList();

        serverUrls.add(new ServerUrl("https://www.rcolion.com", true));
        serverUrls.add(new ServerUrl("https://www.rcofox.com", false));
        serverUrls.add(new ServerUrl("http://10.0.2.2:8080", false));
    }

    public ArrayList<ServerUrl> getServerUrls() {
        if (serverUrls == null)
            setServerUrls();

        return serverUrls;
    }

    public String getSelectedServerUrlStr() {
        if (serverUrls == null)
            setServerUrls();

        for (int i = 0; i < serverUrls.size(); i++) {
            ServerUrl url = serverUrls.get(i);
            if (url.isSelected())
                return url.getUrl();
        }

        return null;
    }

    public void setSelectedServerUrl(String serverNameMatch) {
        if (serverUrls == null)
            return;

        for (int i = 0; i < serverUrls.size(); i++) {
            ServerUrl url = serverUrls.get(i);
            url.setSelected(false);

            if (url.getUrl() != null && url.getUrl().toLowerCase().indexOf(serverNameMatch.toLowerCase()) != -1) {
                url.setSelected(true);
//                Log.d(TAG, "setSelectedServerUrl: url: " + url.getUrl());
                Rms.setUrl(url.getUrl());
//                Log.d(TAG, "setSelectedServerUrl() Rms.getUrl()=>>>" + Rms.getUrl());
            }
        }
    }

    public void setSelectedServerUrl(int index) {
        if (serverUrls == null)
            return;

        for (int i = 0; i < serverUrls.size(); i++) {
            ServerUrl url = serverUrls.get(i);
            url.setSelected(i == index);

            if (i == index) {
//                Log.d(TAG, "setSelectedServerUrl: url: " + url.getUrl());
                Rms.setUrl(url.getUrl());
            }
        }
    }

    //endregion

    //region Users management

    private List<User> users = new ArrayList<User>();
    private List<Driver> drivers = new ArrayList();

    private static User user;
    private String username;
    private String password;
    private boolean authenticatedUser = false;

    public boolean isAuthenticatedUser() {
        return authenticatedUser;
    }

    public void unsetAuthenticatedUser() {
        authenticatedUser = false;
    }

    public User authenticate(String username, String password) throws Exception {
        String response = Rms.getUserInfoFull(username, password);
        Log.d(TAG, "LoginUserTask: authenticate() response=" + response);

        if (response == null || response.trim().length() == 0)
            return null;

        JSONObject jsonObj = new JSONObject(response);
        PairList pairList = Rms.parseJsonCodingFields(jsonObj);

        User user = new User(pairList);

        user.setLogin(username);
        user.setPassword(password);

        Rms.orgName = pairList.getValue("Organization Name");
        Rms.orgNumber = pairList.getValue("Organization Number");

        user.setRecordId(pairList.getValue("RecordId"));
        user.setDriversLicenseState(pairList.getValue("Driver License State"));
        user.setDriversLicenseNumber(pairList.getValue("Driver License Number"));

        return user;
    }

    private void upsertDriverInfo(User user) {
        ContentValues cols = new ContentValues();

        cols.put("driverLicenseNumber", user.getDriversLicenseNumber());
        cols.put("driverLicenseState", user.getDriversLicenseState());
        cols.put("rcoRecordId", user.getRecordId());

        if (db.exists("SELECT * FROM driver WHERE rcoRecordId='" + user.getRecordId() + "'"))
            db.update("driver", cols, "rcoRecordId='" + user.getRecordId() + "'");
        else
            db.insert("driver", cols);
    }

    public ArrayList<String> getUserTruckAndTrailer(String rmsUserId) throws Exception {
        ArrayList<String> result = new ArrayList();

        User u = getUserFromDb(rmsUserId);

        if (u == null)
            return null;

        result.add(u.getTruckNumber());
        result.add(u.getTrailerNumber());

        return result;
    }

    public User authenticateOffline(String username, String password) throws Exception {
        String rmsUserId = getCredentialsUserId(username, password);
        Log.d(TAG, "authenticateOffline: rmsUserId: " + rmsUserId);
//        Sep 02, 2022  -
        User user = getUserFromUserDb(rmsUserId);
        Log.d(TAG, "authenticateOffline: getUserFromUserDb: user: " + user);
        if (user == null) {
            user = getUserFromDriverDb(rmsUserId);
//            Sep 05, 2022  -   We should set the login and password here because when IC is not available- we were not
//            getting any values for login(username) in DB queries
            user.setLogin(username);
            user.setPassword(password);
        }
        Log.d(TAG, "authenticateOffline: user: " + user);
        user.setRmsUserId(rmsUserId);

        return user;
    }

    public User getUser(String employeeId) {
        if (users == null)
            return null;

        for (User u : users)
            if (u != null && u.getEmployeeId() != null && u.getEmployeeId().equalsIgnoreCase(employeeId))
                return u;

        return null;
    }

    public void setAuthenticatedUser(User user, String username, String password) {
        this.authenticatedUser = true;
        this.username = username;
        this.password = password;
        this.user = user;

        Rms.setUsernamePasswordIdentifier(username, password, 1);
    }

    public User getAuthenticatedUser() {
        return user;
    }

    public ArrayList<String> getAuthenticatedUserTruckAndTrailer() throws Exception {
        User u = getAuthenticatedUser();

        if (u == null)
            return null;

        return getUserTruckAndTrailer(u.getRmsUserId());
    }

    public String getCurrentTruckNumberFromEld() {
        if (user != null) {
            return user.getTruckNumber();
        }
        return "";
    }

    public boolean updateTruckAndTrailersInformation(String truckNumber, String trailer1, String trailer2) {
        Log.d(TAG, "applyImplementation: updateTruckAndTrailersInformation: truckNumber: " + truckNumber);
        if (user != null) {
            user.setTruckNumber(truckNumber);
            user.setTrailerNumber(trailer1);
            user.setTrailerNumber2(trailer2);
            return true;
        }

        return false;
    }

    public ArrayList<User> syncStaff() throws Exception {
        ArrayList<User> result = new ArrayList<User>();

        String response = Rms.getRecordsUpdatedXFiltered("User", -5000,
                "Organization+Name,ItemType", ",", getOrgName().replace(" ", "+") + ",staff", ",",
                "RMS+User+Id,RecordId,First+Name,Last+Name,Employee+Id");

        if (response == null || response.trim().length() == 0)
            return null;

        JSONArray jsonArray = new JSONArray(response);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject o = jsonArray.getJSONObject(i);
            User u = new User(Rms.parseJsonCodingFields(o));

            u.setItemType("staff");
            result.add(u);
        }

        deleteUsersByItemType("staff");
        storeUsers(result);

        return result;
    }

    public void loadStaff() {
        ArrayList<User> result = new ArrayList();

        Cursor c = db.getQuery("SELECT rmsUserId, recordId, firstName, lastName, employeeId, itemType FROM users WHERE itemType='staff'");
        c.moveToFirst();

        while (!c.isAfterLast()) {
            String rmsUserId = c.getString(0);
            String recordId = c.getString(1);
            String firstName = c.getString(2);
            String lastName = c.getString(3);
            String employeeId = c.getString(4);
            String itemType = c.getString(5);

            User u = new User();

            u.setRmsUserId(rmsUserId);
            u.setRecordId(recordId);
            u.setFirstName(firstName);
            u.setLastName(lastName);
            u.setEmployeeId(employeeId);
            u.setItemType(itemType);

            result.add(u);
            c.moveToNext();
        }

        c.close();

        if (this.users == null)
            this.users = new ArrayList();

        this.users.addAll(result);
    }

    public void clearUsers() {
        users.clear();
        db.delete("users");
    }

    public boolean existsUserCredentials(String username) {
        return db.exists("SELECT * FROM credentials WHERE username='" + username + "'");
    }

    public boolean existsUserCredentials(String username, String password) {
        return db.exists("SELECT rmsUserId FROM credentials WHERE username='" + username + "' AND password='" + password + "'");
    }

    public boolean existsUser(String rmsUserId) {
        return db.exists("SELECT * FROM users WHERE rmsUserId='" + rmsUserId + "'");
    }

    public void deleteUser(String rmsUserId) {
        db.delete("users", "rmsUserId='" + rmsUserId + "'");
    }

    public void deleteUserCredentials(String username) {
        db.delete("credentials", "username='" + username + "'");
    }

    public void deleteUsersByItemType(String itemType) {
        db.delete("users", "itemType='" + itemType + "'");
    }

    public boolean existsUserRights(String rmsUserId) {
        return db.exists("SELECT * FROM userrights WHERE rmsUserId='" + rmsUserId + "' LIMIT 1");
    }

    public void deleteUserRights(String rmsUserId) {
        db.delete("userrights", "rmsUserId='" + rmsUserId + "'");
    }

    public int deleteTollReceiptItem(String entryId) {
        int isDeleted = db.delete("tollreceipt", "id='" + entryId + "'");
        Log.d(TAG, "ReceiptFragment: deleteTollReceiptItem: isDeleted: " + isDeleted);
        return isDeleted;
    }

    public int deleteFuelReceiptItem(String entryId) {
        int isDeleted = db.delete("fuelReceipt", "id='" + entryId + "'");
        Log.d(TAG, "ReceiptFragment: deleteFuelReceiptItem: isDeleted: " + isDeleted);
        return isDeleted;
    }

    public void storeUserCredentials(String userId, String username, String password) {
        if (existsUserCredentials(username))
            deleteUserCredentials(username);

        ContentValues cols = new ContentValues();

        cols.put("rmsUserId", userId);
        cols.put("username", username);
        cols.put("password", password);

        db.insert("credentials", cols);
    }

    public void storeUser(User u, String itemType) {
        if (existsUser(u.getRmsUserId()))
            return;

        ContentValues cols = new ContentValues();

        cols.put("rmsUserId", u.getRmsUserId());
        cols.put("recordId", u.getRecordId());
        cols.put("firstName", u.getFirstName());
        cols.put("lastName", u.getLastName());
        cols.put("employeeId", u.getEmployeeId());
        cols.put("itemType", itemType);

        db.insert("users", cols);
    }

    public void storeUsers(ArrayList<User> users) {
        if (users == null)
            return;

        this.users.addAll(users);

        for (User u : users) {
            ContentValues cols = new ContentValues();

            cols.put("rmsUserId", u.getRmsUserId());
            cols.put("recordId", u.getRecordId());
            cols.put("firstName", u.getFirstName());
            cols.put("lastName", u.getLastName());
            cols.put("employeeId", u.getEmployeeId());
            cols.put("itemType", u.getItemType());

            db.insert("users", cols);
        }
    }

    public String getCredentialsUserId(String username, String password) {
        Cursor c = db.getQuery("SELECT rmsUserId FROM credentials WHERE username='" + username + "' AND password='" + password + "'");

        if (c == null || c.getCount() == 0)
            return null;

        c.moveToFirst();

        String rmsUserId = c.getString(0);
        c.close();

        return rmsUserId;
    }

    public User getUserFromDb(String rmsUserId) throws Exception {
        Cursor c = db.getQuery("SELECT recordId, firstName, lastName, employeeId, itemType, trucknumber, trailernumber FROM users WHERE rmsUserId='" + rmsUserId + "'");

        if (c == null || c.getCount() == 0)
            return null;

        c.moveToFirst();

        String recordId = c.getString(0);
        String firstName = c.getString(1);
        String lastName = c.getString(2);
        String employeeId = c.getString(3);
        String itemType = c.getString(4);
        String truckNumber = c.getString(5);
        String trailerNumber = c.getString(6);

        c.close();

        User u = new User();

        u.setRecordId(recordId);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmployeeId(employeeId);
        u.setItemType(itemType);
        u.setTruckNumber(truckNumber);
        u.setTrailerNumber(trailerNumber);

        return u;
    }

    public User getUserFromUserDb(String rmsUserId) throws Exception {
        Cursor c = db.getQuery("SELECT recordId, firstName, lastName, employeeId, itemType, trucknumber, trailernumber FROM users WHERE rmsUserId='" + rmsUserId + "'");

        if (c == null || c.getCount() == 0)
            return null;

        c.moveToFirst();

        String recordId = c.getString(0);
        String firstName = c.getString(1);
        String lastName = c.getString(2);
        String employeeId = c.getString(3);
        String itemType = c.getString(4);
        String truckNumber = c.getString(5);
        String trailerNumber = c.getString(6);

        c.close();

        User u = new User();

        u.setRecordId(recordId);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmployeeId(employeeId);
        u.setItemType(itemType);
        u.setTruckNumber(truckNumber);
        u.setTrailerNumber(trailerNumber);

        return u;
    }

    public User getUserFromDriverDb(String rmsUserId) throws Exception {

        Cursor c = db.getQuery("SELECT rcoRecordId, firstname, surname, employeeNumber, itemType, skilllevel, reportingperiod FROM driver WHERE rmsUserId='" + rmsUserId + "'");

        if (c == null || c.getCount() == 0)
            return null;

        c.moveToFirst();

        String recordId = c.getString(0);
        String firstName = c.getString(1);
        String lastName = c.getString(2);
        String employeeId = c.getString(3);
        String itemType = c.getString(4);
        String skillLevel = c.getString(5);
        String reportingPeriod = c.getString(6);

        c.close();

        User u = new User();

        u.setRecordId(recordId);
        u.setFirstName(firstName);
        u.setLastName(lastName);
        u.setEmployeeId(employeeId);
        u.setItemType(itemType);
        u.setSkillLevel(skillLevel);
//        Sep 06, 2022  -   We need to select the reporting period from local DB in the case when we are reporting offline
        u.setReportingPeriod(reportingPeriod);

        return u;
    }

    public ArrayList<String> syncUserRights(String rmsUserId) throws Exception {
        String response = Rms.getUserRights();

        if (response == null || response.trim().length() == 0)
            return null;

        ArrayList<String> result = new ArrayList<String>();
        JSONArray jsonArray = new JSONArray(response);

        for (int i = 0; i < jsonArray.length(); i++)
            result.add(jsonArray.getString(i));

        return result;
    }

    public void loadUserTrucksTrailers() throws Exception {
        if (user.existsTruckNumber() || user.existsTrailerNumber())
            return;

        ArrayList<String> truckAndTrailer = getUserTruckAndTrailer(user.getRmsUserId());

        if (truckAndTrailer != null) {
            user.setTruckNumber(truckAndTrailer.get(0));
            user.setTrailerNumber(truckAndTrailer.get(1));
        }
    }

    public void syncDriver(String rmsUserId) throws Exception {
//        May 26, 2022  -   The sync is done just at the first install
        if (db.exists("SELECT * FROM driver WHERE rmsUserId='" + rmsUserId + "'"))
            return;

        drivers = new ArrayList<>();
        ArrayList<Driver> result = new ArrayList();

        String response = Rms.getRecordsUpdatedXFiltered("User", -5000,
                "Organization+Name,ItemType,RMS+User+Id", ",",
                Rms.getOrgName().replace(" ", "+") + "," + "driver," + rmsUserId, ",", "");

        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            clearTableEntries("driver", "rmsUserId='" + rmsUserId + "'");

            JSONObject o = jsonArray.getJSONObject(0);
            PairList codingFields = Rms.parseJsonCodingFields(o);

            Driver d = new Driver();

            d.setLobjectId(codingFields.getValue("LobjectId"));
            d.setObjectType(codingFields.getValue("objectType"));
            d.RecordId = codingFields.getValue("RecordId");
            d.MobileRecordId = codingFields.getValue("MobileRecordId");
            d.RmsCodingTimestamp = codingFields.getValue("RMS Coding Timestamp");
            d.RmsTimestamp = codingFields.getValue("RMS Timestamp");
            d.OrganizationName = codingFields.getValue("OrganizationName");
            d.OrganizationNumber = codingFields.getValue("OrganizationNumber");
            d.RmsUserId = codingFields.getValue("RMS User Id");
            d.MobilePhone = codingFields.getValue("MobilePhone");
            d.JobType = codingFields.getValue("Job Type");
            d.HelpUrl = codingFields.getValue("Help URL");
            d.DivisionNumber = codingFields.getValue("Division Number");
            d.ChartReference = codingFields.getValue("Chart Reference");
            d.FunctionalGroupObjectId = codingFields.getValue("FunctionalGroupObjectId");
            d.HomeUrl = codingFields.getValue("Home URL");
            d.CurrentUserIdentification = codingFields.getValue("Current User Identification");
            d.UserIdent = codingFields.getValue("UserIdent");
            d.SubOrganizationNumber = codingFields.getValue("SubOrganization Number");
            d.SubOrganizationName = codingFields.getValue("SubOrganization Name");
            d.FunctionalGroupName = codingFields.getValue("FunctionalGroupName");
            d.RmsEfileTimestamp = codingFields.getValue("RMS Efile Timestamp");
            d.CustomerName = codingFields.getValue("Customer Name");
            d.CustomerNumber = codingFields.getValue("Customer Number");
            d.Title = codingFields.getValue("Title");
            d.MobilePhoneNumber = codingFields.getValue("Mobile Phone Number");
            d.Carrier = codingFields.getValue("Carrier");
            d.CurrentPhone = codingFields.getValue("Current Phone");
            d.ClientName = codingFields.getValue("Client Name");
            d.ClientNumber = codingFields.getValue("Client Number");
            d.DirectoryView = codingFields.getValue("Directory View");
            d.SecurityCode = codingFields.getValue("Security Code");
            d.Description1 = codingFields.getValue("Description1");
            d.Url = codingFields.getValue("URL");
            d.UniqueKey1 = codingFields.getValue("Unique Key 1");
            d.Currency = codingFields.getValue("Currency");
            d.Rank = codingFields.getValue("Rank");
            d.Grade = codingFields.getValue("Grade");
            d.PinNumber = codingFields.getValue("Pin Number");
            d.ItemLabel = codingFields.getValue("ItemLabel");
            d.Rate = codingFields.getValue("Rate");
            d.BillRate = codingFields.getValue("Bill Rate");
            d.UserGroupRecordId = codingFields.getValue("UserGroupRecordId");
            d.DisplayName = codingFields.getValue("DisplayName");
            d.ShipToCompany = codingFields.getValue("Ship To Company");
            d.ShipToAddress = codingFields.getValue("Ship To Address");
            d.GPSLocationServices = codingFields.getValue("GPSLocationServices");
            d.MetricSystem = codingFields.getValue("Metric System");
            d.Notes = codingFields.getValue("Notes");
            d.Language = codingFields.getValue("Language");
            d.TruckRule = codingFields.getValue("Truck Rule");
            d.TruckCycleDay = codingFields.getValue("Truck Cycle Day");
            d.DriverDutyStatus = codingFields.getValue("Driver Duty Status");
            d.ExemptDriverStatus = codingFields.getValue("Exempt Driver Status");
            d.TruckShiftStartDate = codingFields.getValue("Truck Shift Start Date");
            d.TruckShiftStartTime = codingFields.getValue("Truck Shift Start Time");
            d.TruckShiftEndDate = codingFields.getValue("Truck Shift End Date");
            d.TruckShiftEndTime = codingFields.getValue("Truck Shift End Time");
            d.IsActive = codingFields.getValue("IsActive");
            d.Company = codingFields.getValue("Company");
            d.LastName = codingFields.getValue("Last Name");
            d.FirstName = codingFields.getValue("First Name");
            d.Address1 = codingFields.getValue("Address1");
            d.City = codingFields.getValue("City");
            d.State = codingFields.getValue("State");
            d.ZipCode = codingFields.getValue("ZipCode");
            d.Country = codingFields.getValue("Country");
            d.Email = codingFields.getValue("Email");
            d.Telephone = codingFields.getValue("Telephone");
            d.ItemType = codingFields.getValue("ItemType");
            d.Latitude = codingFields.getValue("Latitude");
            d.Longitude = codingFields.getValue("Longitude");
            d.DriverLicenseNumber = codingFields.getValue("Driver License Number");
            d.DriverLicenseState = codingFields.getValue("Driver License State");
            d.Location = codingFields.getValue("Location");
            d.DateOfHire = codingFields.getValue("Date of Hire");
            d.DateOfBirth = codingFields.getValue("Date of Birth");
            d.UserType = codingFields.getValue("UserType");
            d.EmployeeId = codingFields.getValue("Employee Id");
            d.Status = codingFields.getValue("Status");
            d.Sex = codingFields.getValue("Sex");
            d.NationalIdentifier = codingFields.getValue("National Identifier");
            d.DriverLicenseClass = codingFields.getValue("Driver License Class");
            d.Endorsements = codingFields.getValue("Endorsements");
            d.DriverLicenseExpirationDate = codingFields.getValue("Driver License Expiration Date");
            d.DotExpirationDate = codingFields.getValue("DOT Expiration Date");
            d.HomePhoneNumber = codingFields.getValue("Home Phone Number");
            d.WorkPhoneNumber = codingFields.getValue("Work Phone Number");
            d.Rating = codingFields.getValue("Rating");
            d.NumberOfReviews = codingFields.getValue("Number of Reviews");
            d.Quota = codingFields.getValue("Quota");
            d.Country = codingFields.getValue("Country");
            d.MobilePhone = codingFields.getValue("MobilePhone");
            d.Status = codingFields.getValue("Status");
            d.CustomerName = codingFields.getValue("Customer Name");
            d.Category1Name = codingFields.getValue("Category1 Name");
            d.Category1Value = codingFields.getValue("Category1 Value");
            d.Category2Name = codingFields.getValue("Category2 Name");
            d.Category2Value = codingFields.getValue("Category2 Value");
            d.Category3Name = codingFields.getValue("Category3 Name");
            d.Category3Value = codingFields.getValue("Category3 Value");
            d.Category4Name = codingFields.getValue("Category4 Name");
            d.Category4Value = codingFields.getValue("Category4 Value");
            d.Category5Name = codingFields.getValue("Category5 Name");
            d.Category5Value = codingFields.getValue("Category5 Value");
            d.SkillLevel = codingFields.getValue("Skill Level");
            d.ReportingPeriod = codingFields.getValue("Reporting Period");

            result.add(d);
            insertDriverEntry(d);

            drivers.addAll(result);
        }

        upsertDriverInfo(user);
    }

    private void insertDriverEntry(Driver d) {
        ContentValues values = new ContentValues();

        values.put("rcoObjectId", d.getLobjectIdStr());
        values.put("rcoObjectType", d.getObjectType());
        values.put("rcoRecordId", d.RecordId);
        values.put("rcoTimestamp", d.RmsTimestamp);
        values.put("rcoMobileRecordId", d.MobileRecordId);
        values.put("organizationName", d.OrganizationName);
        values.put("organizationNumber", d.OrganizationNumber);
        //values.put("role", d.Ro
        //values.put("login", d.);
        //password
        values.put("userGroupName", d.UserGroupName);
        values.put("company", d.Company);
        values.put("surname", d.LastName);
        values.put("firstname", d.FirstName);
        values.put("address1", d.Address1);
        values.put("address2", d.Address2);
        values.put("city", d.City);
        values.put("state", d.State);
        values.put("zip", d.ZipCode);
        values.put("country", d.Country);
        values.put("email", d.Email);
        values.put("phone", d.CurrentPhone);
        values.put("itemType", d.ItemType);
        values.put("latitude", d.Latitude);
        values.put("longitude", d.Longitude);
        values.put("driverLicenseNumber", d.DriverLicenseNumber);
        values.put("driverLicenseState", d.DriverLicenseState);
        values.put("location", d.Location);
        values.put("customerNumber", d.CustomerNumber);
        values.put("dateOfHire", d.DateOfHire);
        values.put("dateOfBirth", d.DateOfBirth);
        values.put("userType", d.UserType);
        values.put("employeeNumber", d.EmployeeId);
        values.put("status", d.Status);
        values.put("sex", d.Sex);
        values.put("nationalIdentifier", d.NationalIdentifier);
        values.put("contactName", d.ContactName);
        values.put("middleName", d.MiddleName);
        values.put("driverLicenseClass", d.DriverLicenseClass);
        values.put("endorsements", d.Endorsements);
        values.put("driverLicenseExpirationDate", d.DriverLicenseExpirationDate);
        values.put("dOTExpirationDate", d.DotExpirationDate);
        values.put("correctiveLensRequired", d.CorrectiveLensRequired);
        values.put("category1Name", d.Category1Name);
        values.put("category1Value", d.Category1Value);
        values.put("category2Name", d.Category2Name);
        values.put("category2Value", d.Category3Name);
        values.put("category3Name", d.Category3Name);
        values.put("category3Value", d.Category3Value);
        values.put("category4Name", d.Category4Name);
        values.put("category4Value", d.Category4Value);
        values.put("category5Name", d.Category5Name);
        values.put("category6Name", d.Category6Name);
        values.put("category6Value", d.Category6Value);
        values.put("mobilePhoneNumber", d.MobilePhoneNumber);
        values.put("homePhoneNumber", d.HomePhoneNumber);
        values.put("workPhoneNumber", d.WorkPhoneNumber);
        values.put("notifyGroupName", d.NotifyGroupName);
        values.put("rating", d.Rating);
        values.put("numberOfReviews", d.NumberOfReviews);
        values.put("quota", d.Quota);
        values.put("managerName", d.ManagerName);
        values.put("managerRecordId", d.ManagerRecordId);
        values.put("managerEmployeeId", d.ManagerEmployeeId);
        values.put("territory", d.Territory);
        values.put("rmsUserId", d.RmsUserId);
        values.put("exemptDriverStatus", d.ExemptDriverStatus);
        values.put("skilllevel", d.SkillLevel);
        values.put("reportingperiod", d.ReportingPeriod);

        db.insert("driver", values);
    }

    public void setUserRights(String rmsUserId, ArrayList<String> userRights) {
        if (userRights == null)
            return;

        this.userRights = userRights;

        for (String userRight : userRights) {
            ContentValues cols = new ContentValues();

            cols.put("rmsUserId", rmsUserId);
            cols.put("userright", userRight);

            db.insert("userrights", cols);
        }
    }

    public void loadUserRights(String rmsUserId) {
        ArrayList<String> userRightsLst = getUserRights(rmsUserId);
        this.userRights = userRightsLst;
    }

    public ArrayList<String> getUserRights(String rmsUserId) {
        ArrayList<String> result = new ArrayList();
        Cursor c = db.getQuery("SELECT userright FROM userrights WHERE rmsUserId='" + rmsUserId + "'");
        c.moveToFirst();

        while (!c.isAfterLast()) {
            String userRight = c.getString(0);
            result.add(userRight);
            c.moveToNext();
        }

        c.close();
        return result;
    }

    public void updatePowerUnitAndTrailerNumber(String powerUnitNumber, String trailerNumber) {
        if (user == null || user.getRmsUserId() == null)
            return;

        lock.lock();

        try {
            boolean existsSetting = db.exists("SELECT * FROM users WHERE rmsUserId='" + user.getRmsUserId() + "'");
            ContentValues values = new ContentValues();

            if (existsSetting) {
                powerUnitNumber = powerUnitNumber != null ? powerUnitNumber.replace("\r", "").replace("\n", "") : "";
                trailerNumber = trailerNumber != null ? trailerNumber.replace("\r", "").replace("\n", "") : "";

                values.put("trucknumber", powerUnitNumber);
                values.put("trailernumber", trailerNumber);

                db.update("users", values, "rmsUserId='" + user.getRmsUserId() + "'");

                user.setTruckNumber(powerUnitNumber);
                user.setTrailerNumber(trailerNumber);
            }
        } finally {
            lock.unlock();
        }
    }

    public void updatePowerUnitAndTrailerNumber(String powerUnitNumber, String rule, String trailerNumber, String trailerNumberTwo) {
        Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: updatePowerUnitAndTrailerNumber: user: " + user);
        if (user == null || user.getRmsUserId() == null)
            return;

        Log.d(TAG, "updatePowerUnitAndTrailerNumber: user.getRmsUserId(): " + user.getRmsUserId());
        Log.d(TAG, "updatePowerUnitAndTrailerNumber: truckNumber: " + user.getTruckNumber());
        Log.d(TAG, "updatePowerUnitAndTrailerNumber: trailer: " + user.getTrailerNumber());
        Log.d(TAG, "updatePowerUnitAndTrailerNumber: trailer2: " + user.getTrailerNumber2());

        lock.lock();


        try {
//            Sep 29, 2022  -   We should update truck and trailer values in driver table (not is user table)
//            boolean existsSetting = db.exists("SELECT * FROM users WHERE rmsUserId='" + user.getRmsUserId() + "'");
            boolean existsSetting = db.exists("SELECT * FROM driver WHERE rmsUserId='" + user.getRmsUserId() + "'");
            Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: updatePowerUnitAndTrailerNumber: existsSetting: " + existsSetting);

            powerUnitNumber = powerUnitNumber != null ? powerUnitNumber.replace("\r", "").replace("\n", "") : "";
            trailerNumber = trailerNumber != null ? trailerNumber.replace("\r", "").replace("\n", "") : "";
            trailerNumberTwo = trailerNumberTwo != null ? trailerNumberTwo.replace("\r", "").replace("\n", "") : "";

            Log.d(TAG, "updatePowerUnitAndTrailerNumber: powerUnitNumber: " + powerUnitNumber + " trailerNumber: " + trailerNumber + " trailerNumberTwo: " + trailerNumberTwo);

            if (existsSetting) {

                ContentValues values = new ContentValues();
                values.put("trucknumber", powerUnitNumber);
                values.put("trailernumber", trailerNumber);
//                July 08, 2022 -   Checked but in user table their is no column for trailer2 number
                values.put("trailer2Number", trailerNumberTwo);

//            Sep 29, 2022  -   We should update truck and trailer values in driver table (not is user table)
//                db.update("users", values, "rmsUserId='" + user.getRmsUserId() + "'");
                int isInserted = db.update("driver", values, "rmsUserId='" + user.getRmsUserId() + "'");
                Log.d(TAG, "updatePowerUnitAndTrailerNumber: isInserted: " + isInserted);
            }

            user.setTruckNumber(powerUnitNumber);
//                Aug 30, 2022  -   In case when cycle is also ended - on next login we are getting the rule type detail
//                from user and saving it
            user.setTruckRule(rule);
            user.setTrailerNumber(trailerNumber);
            user.setTrailerNumber2(trailerNumberTwo);

            Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: updatePowerUnitAndTrailerNumber: truckNumber: " + user.getTruckNumber() + " trailer: " + user.getTrailerNumber());

        } finally {
            lock.unlock();
        }
    }

    public boolean existsTruckNumber(String truckNumber) {
        if (truckNumber == null)
            return false;

        return db.exists("SELECT * FROM trucks WHERE truckNumber LIKE '" + truckNumber.trim().toUpperCase() + "'");
    }

    public boolean isTruckNumberUnknown(String truckNumber) {
        return truckNumber != null && truckNumber.equalsIgnoreCase("Unknown");
    }

    public boolean existsTrailerNumber(String trailerNumber) {
        if (trailerNumber == null)
            return false;

        return db.exists("SELECT * FROM trailers WHERE trailerNumber LIKE '" + trailerNumber.trim().toUpperCase() + "'");
    }

    public String[] getPowerUnitNumbers() {
        lock.lock();

        try {
            if (!db.exists("SELECT * FROM trucks"))
                return null;

            ArrayList<String> result = new ArrayList();
            result.add("Unknown");

            Cursor c = db.getRow("SELECT DISTINCT truckNumber FROM trucks ORDER BY truckNumber ASC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                String truckNumber = c.getString(col++);

                result.add(truckNumber);
                c.moveToNext();
                col = 0;
            }

            c.close();

            return ArrayUtils.toStringArray(result);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        } finally {
            lock.unlock();
        }
    }

    public String[] getTrailers(String truckNumber) {
        lock.lock();

        try {
            if (!db.exists("SELECT * FROM trailers"))
                return null;

            ArrayList<String> result = new ArrayList();
            result.add("Unknown");
            result.add("No Trailer");

            Cursor c = db.getRow("SELECT DISTINCT trailerNumber FROM trailers WHERE " +
                    "(truckNumber='" + truckNumber + "' OR truckNumber LIKE 'T" + truckNumber + "' OR truckNumber IS NULL) ORDER BY trailerNumber ASC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                String trailerNumber = c.getString(col++);

                result.add(trailerNumber);
                c.moveToNext();
                col = 0;
            }

            c.close();

            return ArrayUtils.toStringArray(result);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        } finally {
            lock.unlock();
        }
    }

    public String[] getTrailers() {
        lock.lock();

        try {
            if (!db.exists("SELECT * FROM trailers"))
                return null;

            ArrayList<String> result = new ArrayList();
            result.add("Unknown");
            result.add("No Trailer");

            Cursor c = db.getRow("SELECT DISTINCT trailerNumber FROM trailers ORDER BY trailerNumber ASC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                String trailerNumber = c.getString(col++);

                result.add(trailerNumber);
                c.moveToNext();
                col = 0;
            }

            c.close();

            return ArrayUtils.toStringArray(result);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        } finally {
            lock.unlock();
        }
    }

    public String[] getNearestArea(Activity a) {
        Tuple<Double, MapAsset> nearestTa = calculateNearestTaMiles(getPhoneLastBestLocation(a));
        Tuple<Double, MapAsset> nearestPilot = calculateNearestPilotMiles(getPhoneLastBestLocation(a));
        Tuple<Double, RestArea> nearestRest = calculateNearestRestAreaMiles(getPhoneLastBestLocation(a));

        boolean closetGasArea = nearestTa.getElement0() < nearestPilot.getElement0();
        Tuple<Double, MapAsset> gasArea = closetGasArea ? nearestTa : nearestPilot;

        if (gasArea.getElement0() > nearestRest.getElement0()) {
            return new String[]{nearestRest.getElement1().Latitude, nearestRest.getElement1().Longitude};
        } else {
            return new String[]{gasArea.getElement1().Latitude, gasArea.getElement1().Longitude};
        }
    }

//    public Integer getSpeedFromLocation(LatLng startLocation, LatLng destination) throws Exception {
//        try {
//
//            String response = Rms.getMapBoxSpeed(startLocation, destination);
//            Log.i("response", "" + response);
//            Log.d(TAG, "Green: segment: getSpeedFromLocation: response: " + response);
//
//            if (response == null || response.trim().length() == 0)
//                return null;
//
//            JSONObject jsonObject = new JSONObject(response);
//            JSONArray routeArray = jsonObject.getJSONArray("routes");
//
//            JSONObject routeJsonObject = routeArray.getJSONObject(0);
//
//            JSONArray legArray = routeJsonObject.getJSONArray("legs");
//            JSONObject legs = legArray.getJSONObject(0);
//
//            JSONObject annotation = legs.getJSONObject("annotation");
//            JSONArray maxSpeedArray = annotation.getJSONArray("maxspeed");
//            Log.d(TAG, "maxSpeed: getSpeedFromLocation: " + maxSpeedArray);
//
//            JSONObject maxSpeed = maxSpeedArray.getJSONObject(0);
//            if (maxSpeed.has("speed")) {
//                Log.d(TAG, "Green: segment: getSpeedFromLocation: maxSpeed: hasSpeed: " + maxSpeed.getInt("speed"));
//                return maxSpeed.getInt("speed");
//            } else {
//                Log.d(TAG, "Green: segment: getSpeedFromLocation: else: ");
//                if (maxSpeedArray.length() > 0) {
//                    for (int i = 0; i < maxSpeedArray.length(); i++) {
//                        if (maxSpeedArray.getJSONObject(i).has("speed")) {
//                            Log.d(TAG, "Green: segment: findMaxSpeedLimit: getSpeedFromLocation: " + maxSpeedArray.getJSONObject(i).getInt("speed"));
//                            return maxSpeedArray.getJSONObject(i).getInt("speed");
//                        }
//                    }
//                } else {
//                    Log.d(TAG, "Green: segment: getSpeedFromLocation: return null");
//                    return null;
//                }
//            }
//
//
//        } catch (IOException | JSONException e) {
//            e.printStackTrace();
//            Log.d(TAG, "Green: segment: findMaxSpeedLimit: getSpeedFromLocation: Exception: " + e.getMessage());
//        }
//
//        return null;
//    }

    public ArrayList<GenericModel> getSpeedFromLocationUpdate(LatLng startLocation, LatLng destination, int stepPosition) throws Exception {
        try {

            String response = Rms.getMapBoxSpeed(startLocation, destination, stepPosition);
//            Log.i("response", "" + response);
//            Log.d(TAG, "Green: segment: getSpeedFromLocation: response: " + response);

            if (response == null || response.trim().length() == 0)
                return null;

            JSONObject jsonObject = new JSONObject(response);
            JSONArray routeArray = jsonObject.getJSONArray("routes");

            JSONObject routeJsonObject = routeArray.getJSONObject(0);

            JSONArray legArray = routeJsonObject.getJSONArray("legs");
            JSONObject legs = legArray.getJSONObject(0);

            JSONObject annotation = legs.getJSONObject("annotation");


//          MAY 17, 2022
//          save new coordinates only if new value is a different value from previous value
            JSONArray maxSpeedArray = annotation.getJSONArray("maxspeed");
            int previousSpeed = 0;
            String previousLatitude = "", previousLongitude = "";
            ArrayList<GenericModel> maxSpeedAndCoordinatesArray = new ArrayList<>();
            for (int i = 0; i < maxSpeedArray.length(); i++) {
                JSONObject speedIndexObject = maxSpeedArray.getJSONObject(i);
                if (speedIndexObject.has("speed") && !speedIndexObject.isNull("speed")) {
                    int speed = speedIndexObject.getInt("speed");
                    if (speed != previousSpeed) {
                        previousSpeed = speed;

                        GenericModel genericModel = generateCoordinates(i, routeJsonObject, speed,
                                previousLatitude, previousLongitude);
                        if (genericModel != null) {
                            previousLatitude = genericModel.getLatitude();
                            previousLongitude = genericModel.getLongitude();

                            if (genericModel.getStraightDistance() < 25) {
                                if (maxSpeedAndCoordinatesArray.size() > 0) {
                                    GenericModel previousGenericModel = (maxSpeedAndCoordinatesArray.get(maxSpeedAndCoordinatesArray.size() - 1));
//                                    previousGenericModel.setStraightDistance(genericModel.getStraightDistance());
//                                    previousGenericModel.setSpeed(genericModel.getSpeed());
                                    maxSpeedAndCoordinatesArray.remove(previousGenericModel);
                                    maxSpeedAndCoordinatesArray.add(genericModel);
                                } else {
                                    maxSpeedAndCoordinatesArray.add(genericModel);
                                }
                            } else {
                                maxSpeedAndCoordinatesArray.add(genericModel);
                            }
                        }
                    }
                }
            }

//            Log.d(TAG, "printMaxSpeedArrayHashMap: getSpeedFromLocation: maxSpeedAndCoordinatesArray: size: " + maxSpeedAndCoordinatesArray.size() + " stepPosition: " + stepPosition);

            steps.add(stepPosition);
            maxSpeedArrayHashMap.put(stepPosition, maxSpeedAndCoordinatesArray);
//            Log.d(TAG, "printMaxSpeedArrayHashMap: getSpeedFromLocationUpdate: " + maxSpeedArrayHashMap.size() + " stepPosition: " + stepPosition);
            for (int i = 1; i < maxSpeedAndCoordinatesArray.size(); i++) {
                GenericModel genericModel = maxSpeedAndCoordinatesArray.get(i);
                generateGeofence(stepPosition, genericModel.getLatitude(),
                        genericModel.getLongitude(), genericModel.getSpeed());
            }

            return maxSpeedAndCoordinatesArray;

        } catch (IOException | JSONException e) {
            e.printStackTrace();
            Log.d(TAG, "printMaxSpeedArrayHashMap: Green: segment: findMaxSpeedLimit: getSpeedFromLocation: Exception: " + e.getMessage());
        }


        return null;
    }

    void generateGeofence(int stepPosition, String latitude, String longitude, int maxSpeed) {
//        Log.d(TAG, "printMaxSpeedArrayHashMap: generateGeofence: stepPosition: " + stepPosition + " latitude: " + latitude + " longitude: " + longitude + " maxSpeed: " + maxSpeed);

    }

    public void clearMaxSpeedArrayHashMap() {
        maxSpeedArrayHashMap.clear();
    }

    public void clearSteps() {
        steps.clear();
    }


    public Map<Integer, ArrayList<GenericModel>> getMaxSpeedArrayHashMap() {
        return maxSpeedArrayHashMap;
    }

    public void setMaxSpeedArrayHashMap(Map<Integer, ArrayList<GenericModel>> maxSpeedArrayHashMap) {
        this.maxSpeedArrayHashMap = maxSpeedArrayHashMap;
    }


    GenericModel generateCoordinates(int index, JSONObject jsonObject, int speed, String previousLatitude,
                                     String previousLongitude) {
//        Log.d(TAG, "generateCoordinates: ");
        JSONObject geometryJSONObject = null;
        try {
            geometryJSONObject = jsonObject.getJSONObject("geometry");
            JSONArray coordinatesArray;
            if (geometryJSONObject != null) {
                coordinatesArray = geometryJSONObject.getJSONArray("coordinates");
                JSONArray jsonArray = coordinatesArray.getJSONArray(index);

                GenericModel genericModel = new GenericModel();
                genericModel.setIndex(index);
                genericModel.setLatitude("" + jsonArray.get(1));
                genericModel.setLongitude("" + jsonArray.get(0));
                genericModel.setSpeed(speed);

                if (!previousLatitude.isEmpty() && !previousLongitude.isEmpty()) {

                    double latitude = Double.parseDouble(previousLatitude);
                    double longitude = Double.parseDouble(previousLongitude);

                    LatLng previousLocation = new LatLng(latitude, longitude);
                    LatLng currentLocation = new LatLng(Double.parseDouble(genericModel.getLatitude()),
                            Double.parseDouble(genericModel.getLongitude()));
                    double straightDistance = Utils.calculateDistance(previousLocation, currentLocation);
//                    Log.d(TAG, "generateCoordinates: straightDistance: " + straightDistance);

//                    May 25, 2022  -   We should add a limitation to the radius
                    if (straightDistance > 100) {
                        straightDistance = 100;
                    } else {
//                        Aug 09, 2022  -   if the distance is smaller than 20 meters,
//                        this means we are at a stop and we will enter on a segment with a different speed
                        if (straightDistance < 20) {
//                            Log.d(TAG, "generateCoordinates: if (straightDistance < 20): ");
//                            return null;

                        }
                    }
                    genericModel.setStraightDistance(straightDistance);
                }

                return genericModel;
            }
        } catch (JSONException jsonException) {
            Log.d(TAG, "generateCoordinates: jsonException: " + jsonException.getMessage());
            jsonException.printStackTrace();
        }

        return null;
    }

    public String getTruckSpeedAndLocation() throws Exception {
        try {

            return Rms.getTruckLocation();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void updateTruckDriverAndLocation(Activity a) throws Exception {
        String truckNumber = user.getTruckNumber();

        String response = Rms.getRecordsUpdatedXFiltered("Truck", -5000, "ItemType,Truck+Number",
                ",", "truck," + truckNumber, ",", "");

        if (response == null || response.trim().length() == 0)
            return;

        JSONArray jsonArray = new JSONArray(response);
        JSONObject o = jsonArray.getJSONObject(0);
        PairList fields = Rms.parseJsonCodingFields(o);

        String lobjectId = fields.getValue("LobjectId");
        String objectType = fields.getValue("objectType");

        Rms.setRecordCodingFields(lobjectId, objectType, "First+Name", user.getFirstName(), true);
        Rms.setRecordCodingFields(lobjectId, objectType, "Last+Name", user.getLastName(), true);

        String lat = "";
        String lon = "";

        try {
            Double[] latLon = getLastValidLatLonDistance();

            if (latLon == null || latLon.length == 0) {
                Location l = getPhoneLastBestLocation(a);

                if (l != null) {
                    lat = Double.toString(l.getLatitude());
                    lon = Double.toString(l.getLongitude());
                }
            } else {
                lat = Double.toString(latLon[0]);
                lon = Double.toString(latLon[1]);
            }
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        Rms.setRecordCodingFields(lobjectId, objectType, "Latitude", lat, true);
        Rms.setRecordCodingFields(lobjectId, objectType, "Longitude", lon, true);
    }

    //endregion

    //region Map management

    private ArrayList<City> cities;

//    Sep 12, 2022  -   Comment out this code as the city from raw folder contains incomplete list of cities
//    So now used new approach where we get the list for mexico - canada - usa states and then their cities list

//    public void loadCities(Activity activity) {
//        ArrayList<String> citiesList = Utils.readRawTextFile(activity, R.raw.cities);
//        cities = new ArrayList<>();
//
//        for (int i = 0; i < citiesList.size(); i++) {
//            String line = citiesList.get(i);
//            String[] cols = line.split(",");
//
//            String city = cols[0];
//            String state = cols[1];
//            Double lat = null;
//            Double lon = null;
//
//            try {
//                String latStr = cols[2];
//                lat = Double.parseDouble(latStr);
//            } catch (Throwable throwable) {
//                if (throwable != null)
//                    throwable.printStackTrace();
//            }
//
//            try {
//                String lonStr = cols[3];
//                lon = Double.parseDouble(lonStr);
//            } catch (Throwable throwable) {
//                if (throwable != null)
//                    throwable.printStackTrace();
//            }
//
//            cities.add(new City(city, state, lat, lon));
//        }
//    }

    //    Sep 12, 2022  -   loadCities previous method(above one) only have limited cities
    public void loadCities(Activity activity) {
        Log.d(TAG, "loadCities: ");
        cities = new ArrayList<>();
        String[] citiesArray = activity.getResources().getStringArray(R.array.cities_list);
        Log.d(TAG, "loadCities: citiesArray: " + citiesArray.length);
        for (int i = 0; i < citiesArray.length; i++) {
            try {
                JSONArray jsonArray = new JSONArray(BusinessRules.getJsonFromAssets(activity, citiesArray[i]));
                for (int x = 0; x < jsonArray.length(); x++) {
                    JSONObject cityJSONObject = jsonArray.getJSONObject(x);
                    cities.add(new City(cityJSONObject.getString("City"), cityJSONObject.getString("State"), cityJSONObject.getDouble("latitude"), cityJSONObject.getDouble("longitude")));
                }
            } catch (JSONException jsonException) {
                Log.d(TAG, "loadCities: jsonException: " + jsonException);
            }
        }
        Log.d(TAG, "loadCities: total cities: size: " + cities.size());
    }

    private ArrayList<MapAsset> mMapPilots;
    private ArrayList<MapAsset> mapTA;
    private ArrayList<MapAsset> mapLove;
    private ArrayList<RestArea> restAreas;
    private ArrayList<String> activeStatesList;
    private ArrayList<RoadTollModel> roadList;
    ArrayList<Marker> restMarker = new ArrayList<Marker>();
    ArrayList<Marker> gasMarker = new ArrayList<Marker>();

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION_BACKGROUND = 10002;
    public static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1234;

    private final static double AVERAGE_RADIUS_OF_EARTH_KM = 6371;
    private final static double MAX_KMS_FOR_MAP_ASSETS = 100;
    public final static double MAX_LOCATION_UPDATE = 3;
    public final static double DESTINATION_RANG = 30;

    private LocationManager locationManager = null;
    private Location currentBestLocation = null;
    private Location lastKnownFarLocation;
    private boolean isSelfPermissionAllowedForLocation = false;

    private double kmInMiles = 0.621371192;

    public boolean invokeLocationPermissions(Activity a) {
        if (ContextCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(a, Manifest.permission.ACCESS_FINE_LOCATION))
                ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);
            else
                ActivityCompat.requestPermissions(a, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, MY_PERMISSIONS_REQUEST_LOCATION);

            return false;
        }

        return true;
    }

    public void syncAssets(Context context) {
        Log.d(TAG, "calculateDistance: syncPilotAssetsUpdate: ");
        try {

            JSONArray jsonArray = new JSONArray(BusinessRules.getJsonFromAssets(context, "map_assets/map_assets_content.json"));

//        Log.d(TAG, "syncPilotAssetsUpdate: jsonArray: size: "+jsonArray.length());
            mMapPilots = new ArrayList<>();
            mapTA = new ArrayList<>();
            restAreas = new ArrayList<>();
            mapLove = new ArrayList<>();

            ArrayList<MapAsset> pilotResult = new ArrayList();
            ArrayList<MapAsset> taResult = new ArrayList();
            ArrayList<RestArea> restAreasResult = new ArrayList();
            ArrayList<MapAsset> loveResult = new ArrayList();

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.getString("Chain").contains("Pilot")) {
                    MapAsset a = new MapAsset();
//                a.Address = jsonObject.getString("Address");
                    a.Category = jsonObject.getString("Category");
                    a.Chain = jsonObject.getString("Chain");
                    a.Latitude = jsonObject.getString("Latitude");
                    a.Longitude = jsonObject.getString("Longitude");
                    a.Name = jsonObject.getString("Name");
                    a.RecordId = jsonObject.getString("RecordId");
//                a.State = jsonObject.getString("State");
                    pilotResult.add(a);
                } else if (jsonObject.getString("Chain").contains("TA")) {
                    MapAsset a = new MapAsset();
//                    a.Address = jsonObject.getString("Address");
                    a.Category = jsonObject.getString("Category");
                    a.Chain = jsonObject.getString("Chain");
                    a.Latitude = jsonObject.getString("Latitude");
                    a.Longitude = jsonObject.getString("Longitude");
                    a.Name = jsonObject.getString("Name");
                    a.RecordId = jsonObject.getString("RecordId");
//                    a.State = jsonObject.getString("State");
                    taResult.add(a);
                } else {

                    if (jsonObject.getString("Category").contains("Rest Areas")) {
                        RestArea a = new RestArea();
                        //                    a.Address = jsonObject.getString("Address");
                        a.Category = jsonObject.getString("Category");
                        a.Chain = jsonObject.getString("Chain");
                        a.Latitude = jsonObject.getString("Latitude");
                        a.Longitude = jsonObject.getString("Longitude");
                        a.Name = jsonObject.getString("Name");
                        a.RecordId = jsonObject.getString("RecordId");
                        //                    a.State = jsonObject.getString("State");
                        restAreasResult.add(a);
                    } else if (jsonObject.getString("Chain").contains("Loves")) {
                        MapAsset a = new MapAsset();
                        //                    a.Address = jsonObject.getString("Address");
                        a.Category = jsonObject.getString("Category");
                        a.Chain = jsonObject.getString("Chain");
                        a.Latitude = jsonObject.getString("Latitude");
                        a.Longitude = jsonObject.getString("Longitude");
                        a.Name = jsonObject.getString("Name");
                        a.RecordId = jsonObject.getString("RecordId");
                        //                    a.State = jsonObject.getString("State");
                        loveResult.add(a);
                    }
                }
            }


            mMapPilots.addAll(pilotResult);
            mapTA.addAll(taResult);
            restAreas.addAll(restAreasResult);
            mapLove.addAll(loveResult);

//        Log.d(TAG, "calculateDistance: syncPilotAssetsUpdate: mapPilots: " + mMapPilots.size());
//        Log.d(TAG, "calculateDistance: syncPilotAssetsUpdate: mapTA: " + mapTA.size());
//        Log.d(TAG, "calculateDistance: syncPilotAssetsUpdate: restAreas: " + restAreas.size());
//        Log.d(TAG, "calculateDistance: syncPilotAssetsUpdate: mapLove: " + mapLove.size());

        } catch (JSONException jsonException) {
            Log.d(TAG, "calculateDistance: syncAssets: jsonException: " + jsonException.getMessage());
        }
    }

    public ArrayList<RoadTollModel> getRoadsList(String state, Context context) {
//        Log.d(TAG, "CreateTollReceipt: getRoadsList: roadList: " + roadList);
        ArrayList<RoadTollModel> tempRoadList = new ArrayList<>();
        tempRoadList.add(new RoadTollModel());
        if (roadList == null && roadList.size() == 0) {
            syncRoadsList(context);
        }
//        Log.d(TAG, "CreateTollReceipt: getRoadsList: roadList: 2: " + roadList);
        for (int i = 0; i < roadList.size(); i++) {
            if (roadList.get(i).getState().equalsIgnoreCase(state)) {
                tempRoadList.add(roadList.get(i));
            }
        }
//        Log.d(TAG, "CreateTollReceipt: getRoadsList: roadList: 3: " + roadList);
        return tempRoadList;
    }


    public ArrayList<String> getActiveStateList(Context context) {
        Log.d(TAG, "CreateTollReceipt: getRoadsList: roadList: 1: " + activeStatesList);
        if (activeStatesList == null) {
            syncRoadsList(context);
        }
        if (activeStatesList.size() == 0) {
            syncRoadsList(context);
        }
        return activeStatesList;
    }


    public void syncRoadsList(Context context) {
        Log.d(TAG, "syncRoadsList: syncPilotAssetsUpdate: ");
        try {

            JSONArray jsonArray = new JSONArray(BusinessRules.getJsonFromAssets(context, "stations/toll_roads.json"));
            roadList = new ArrayList<>();
            activeStatesList = new ArrayList<>();

            Log.d(TAG, "syncRoadsList: jsonArray: size: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                String cashTolls = "", latitude = "", length = "", longitude = "", northernOrEasternTerminus = "",
                        notes = "", rmsTimestamp = "", recordId = "", roadName = "", southernOrWesternTerminus = "",
                        state = "", vendorName = "";

                if (jsonObject.has("Cash Tolls")) {
                    Log.d(TAG, "jsonValue : Cash Tolls: value: ");
                    cashTolls = jsonObject.getString("Cash Tolls");
                }
                if (jsonObject.has("Latitude")) {
                    Log.d(TAG, "jsonValue : latitude : value: ");
                    latitude = jsonObject.getString("Latitude");
                }
                if (jsonObject.has("Length")) {
                    Log.d(TAG, "jsonValue : Length : value: ");
                    length = jsonObject.getString("Length");
                }
                if (jsonObject.has("Longitude")) {
                    Log.d(TAG, "jsonValue : Longitude : value: ");
                    longitude = jsonObject.getString("Longitude");
                }
                if (jsonObject.has("Northern or Eastern Terminus")) {
                    Log.d(TAG, "jsonValue : Northern or Eastern Terminus : value: ");
                    northernOrEasternTerminus = jsonObject.getString("Northern or Eastern Terminus");
                }
                if (jsonObject.has("Notes")) {
                    Log.d(TAG, "jsonValue : Notes : value: ");
                    notes = jsonObject.getString("Notes");
                }
                if (jsonObject.has("RMS Timestamp")) {
                    Log.d(TAG, "jsonValue : RMS Timestamp : value: ");
                    rmsTimestamp = jsonObject.getString("RMS Timestamp");
                }
                if (jsonObject.has("RecordId")) {
                    Log.d(TAG, "jsonValue : RecordId : value: ");
                    recordId = jsonObject.getString("RecordId");
                }
                if (jsonObject.has("Road Name")) {
                    Log.d(TAG, "jsonValue : RoadName : value: ");
                    roadName = jsonObject.getString("Road Name");
                }
                if (jsonObject.has("Southern or Western Terminus")) {
                    Log.d(TAG, "jsonValue : Southern or Western Terminus : value: ");
                    southernOrWesternTerminus = jsonObject.getString("Southern or Western Terminus");
                }
                if (jsonObject.has("State")) {
                    Log.d(TAG, "jsonValue : State : value: ");
                    state = jsonObject.getString("State");
                    if (!isStateExists(state)) {
                        activeStatesList.add(state);
                    }
                }

                if (jsonObject.has("Vendor Name")) {
                    Log.d(TAG, "jsonValue : Vendor Name : value: ");
                    vendorName = jsonObject.getString("Vendor Name");
                }

//    	"Cash Tolls": "$1.50 bridge only",
//                "Latitude": "33.1813775",
//                "Length": "4.9",
//                "Longitude": "-87.6132381",
//                "Northern or Eastern Terminus": "US 82 - Northport",
//                "Notes": "Cash or Freedom Pass",
//                "RMS Timestamp": "1669062876127",
//                "RecordId": "2070836",
//                "Road Name": "Joe Mallisham Parkway",
//                "Southern or Western Terminus": "I-20/I-59 - Tuscaloosa",
//                "State": "AL",
//                "Vendor Name": "Tuscaloosa By-Pass"

                RoadTollModel roadTollModel = new RoadTollModel();
                roadTollModel.setCashTolls(cashTolls);
                roadTollModel.setLatitude(latitude);
                roadTollModel.setLength(length);
                roadTollModel.setLongitude(longitude);
                roadTollModel.setNorthernOrEasternTerminus(northernOrEasternTerminus);
                roadTollModel.setNotes(notes);
                roadTollModel.setRmsTimestamp(rmsTimestamp);
                roadTollModel.setRecordId(recordId);
                roadTollModel.setRoadName(roadName);
                roadTollModel.setSouthernOrWesternTerminus(southernOrWesternTerminus);
                roadTollModel.setState(state);
                roadTollModel.setVendorName(vendorName);
                roadList.add(roadTollModel);
            }
            Log.d(TAG, "syncRoadsList: syncRoadsList: roadList: " + roadList.size());
        } catch (JSONException jsonException) {
            Log.d(TAG, "syncRoadsList: jsonException: " + jsonException.getMessage());
        }

    }

    boolean isStateExists(String state) {
        if (activeStatesList == null) {
            activeStatesList = new ArrayList<>();
        }
        for (int i = 0; i < activeStatesList.size(); i++) {
            if (state.equalsIgnoreCase(activeStatesList.get(i))) {
                return true;
            }
        }
        return false;
    }

    public static String getJsonFromAssets(Context context, String fileName) {
        String jsonString;
        try {
            Log.d(TAG, "getJsonFromAssets: context: " + context + " filename: " + fileName);
            if (context == null) {
                return null;
            }
            InputStream is = context.getAssets().open(fileName);

            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }

        return jsonString;
    }

    public ArrayList<MapAsset> getMapLovesIn100MilesRange(Location myLocation) {
        if (mapLove == null)
            return null;

        ArrayList<MapAsset> result = new ArrayList();

        for (int i = 0; i < mapLove.size(); i++) {
            MapAsset m = mapLove.get(i);

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double distanceInKm = calculateDistanceInKm(myLocation, m);
            if (distanceInKm != null)
                if (distanceInKm * kmInMiles <= MAX_KMS_FOR_MAP_ASSETS)
                    result.add(m);
        }

        return result;
    }

    public ArrayList<MapAsset> getMapPilotsIn100MilesRange(Location myLocation) {
        if (mMapPilots == null)
            return null;

        ArrayList<MapAsset> result = new ArrayList();

        for (int i = 0; i < mMapPilots.size(); i++) {
            MapAsset m = mMapPilots.get(i);

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double distanceInKm = calculateDistanceInKm(myLocation, m);

            if (distanceInKm * kmInMiles <= MAX_KMS_FOR_MAP_ASSETS)
                result.add(m);
        }

        return result;
    }

    public ArrayList<MapAsset> getMapTAIn100MilesRange(Location myLocation) {
        if (mapTA == null)
            return null;

        ArrayList<MapAsset> result = new ArrayList();

        for (int i = 0; i < mapTA.size(); i++) {
            MapAsset m = mapTA.get(i);

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double distanceInKm = calculateDistanceInKm(myLocation, m);

            if (distanceInKm * kmInMiles <= MAX_KMS_FOR_MAP_ASSETS)
                result.add(m);
        }

        return result;
    }

    public ArrayList<RestArea> getRestAreasIn100MilesRange(Location myLocation) {
        if (restAreas == null)
            return null;

        ArrayList<RestArea> result = new ArrayList();

        for (int i = 0; i < restAreas.size(); i++) {
            RestArea m = restAreas.get(i);

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double lat = Double.parseDouble(m.Latitude);
            Double lon = Double.parseDouble(m.Longitude);

            Double distanceInKm = calculateDistanceInKm(myLocation.getLatitude(), myLocation.getLongitude(), lat, lon);

            if (distanceInKm * kmInMiles <= MAX_KMS_FOR_MAP_ASSETS)
                result.add(m);
        }

        return result;
    }

    public Location getPhoneLastBestLocation(Activity a) {
        try {
            if (locationManager == null)
                locationManager = (LocationManager) a.getSystemService(Context.LOCATION_SERVICE);

            if (!isSelfPermissionAllowedForLocation) {
                if (ActivityCompat.checkSelfPermission(a, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ActivityCompat.checkSelfPermission(a, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return null;
                } else
                    isSelfPermissionAllowedForLocation = true;
            }

            Location l = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

            if (l != null)
                return l;

            return locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    private Float lastKnownAccelerometerX, lastKnownAccelerometerY, lastKnownAccelerometerZ;

    public void setLastKnownAccelerometerValues(float x, float y, float z) {
        lastKnownAccelerometerX = x;
        lastKnownAccelerometerY = y;
        lastKnownAccelerometerZ = z;
    }

    public Double calculateDistanceInKm(Location myLocation, RestArea m) {
        Double lat = Double.parseDouble(m.Latitude);
        Double lon = Double.parseDouble(m.Longitude);

        return calculateDistanceInKm(myLocation.getLatitude(), myLocation.getLongitude(), lat, lon);
    }

    public Double calculateDistanceInKm(Location myLocation, MapAsset m) {
        Double lat = null;
        Double lon = null;

        if (isDouble(m.Latitude) && isDouble(m.Longitude)) {
            lat = Double.parseDouble(m.Latitude);
            lon = Double.parseDouble(m.Longitude);

            return calculateDistanceInKm(myLocation.getLatitude(), myLocation.getLongitude(), lat, lon);
        }

        return null;
    }

    //    Sep 12, 2022  -   Dragos recommended - It returns difference between coordinates much faster - means efficient
//    But we are already doing it Link shared https://stackoverflow.com/questions/27928/calculate-distance-between-two-latitude-longitude-points-haversine-formula
    public Double calculateDistanceInKm(double userLat, double userLng, double venueLat,
                                        double venueLng) {
        try {
            double latDistance = Math.toRadians(userLat - venueLat);
            double lngDistance = Math.toRadians(userLng - venueLng);

            double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                    + Math.cos(Math.toRadians(userLat)) * Math.cos(Math.toRadians(venueLat))
                    * Math.sin(lngDistance / 2) * Math.sin(lngDistance / 2);

            double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

            return AVERAGE_RADIUS_OF_EARTH_KM * c;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public boolean isNewLocation100MilesFar(Location l) {
        if (lastKnownFarLocation == null)
            return true;

        double lastLat = lastKnownFarLocation.getLatitude();
        double lastLon = lastKnownFarLocation.getLongitude();

        double newLat = l.getLatitude();
        double newLon = l.getLongitude();

        return calculateDistanceInKm(lastLat, lastLon, newLat, newLon) > (MAX_KMS_FOR_MAP_ASSETS / 4);
    }

    public boolean isMyLocationCloseToDestination(Location myLocation, LatLng destination) {
        if (myLocation == null)
            return false;

        Location endPoint = new Location("locationB");
        endPoint.setLatitude(destination.latitude);
        endPoint.setLongitude(destination.longitude);

        double distance = myLocation.distanceTo(endPoint);
        return distance <= DESTINATION_RANG;
    }

    public boolean isLocationUpdated(Location l) {
        if (lastKnownFarLocation == null)
            return true;

        Location startPoint = new Location("locationA");
        startPoint.setLatitude(lastKnownFarLocation.getLatitude());
        startPoint.setLongitude(lastKnownFarLocation.getLongitude());

        Location endPoint = new Location("locationB");
        endPoint.setLatitude(l.getLatitude());
        endPoint.setLongitude(l.getLongitude());

        double distance = startPoint.distanceTo(endPoint);
        return distance > MAX_LOCATION_UPDATE;
    }

    public void storeLastFarLocation(Location l) {
        lastKnownFarLocation = l;
    }

    public Location getLastFarLocation() {
        return lastKnownFarLocation;
    }

    public void loadMapAssetsAndRestAreasIn100MilesRange(Activity a, GoogleMap map,
                                                         boolean isRestStation, boolean isGasStation) {
//        Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: ");
        // Coord1 : 34.008303, -118.491227

        //map.clear();

        Location myLocation = getPhoneLastBestLocation(a);
//        Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: myLocation: " + myLocation);

        ArrayList<MapAsset> mapLoves = getMapLovesIn100MilesRange(myLocation);
        if (mapLoves != null) {
//            Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: mapLoves: " + mapLoves.size());
            for (int i = 0; i < mapLoves.size(); i++) {
                MapAsset m = mapLoves.get(i);

                map.addMarker(new MarkerOptions()
                        .position(m.getLatLng())
                        .icon(m.getIcon())
                        .title(m.Name)
                        .snippet(m.Number + ", " + m.Location));
            }
        }

        ArrayList<MapAsset> mapPilots = getMapPilotsIn100MilesRange(myLocation);
        if (mapPilots != null && isGasStation) {
//            Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: mapPilots: " + mapPilots.size());
            for (int i = 0; i < mapPilots.size(); i++) {
                MapAsset m = mapPilots.get(i);
                gasMarker.add(map.addMarker(new MarkerOptions()
                        .position(m.getLatLng())
                        .icon(m.getIcon())
                        .title(m.Name)
                        .snippet(m.Number + ", " + m.Location)));
            }
        }

//        Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: mapPilots: " + mapPilots.size());
        ArrayList<MapAsset> mapTA = getMapTAIn100MilesRange(myLocation);
        if (mapTA != null && isGasStation) {
//            Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: mapTA: " + mapTA.size());
            for (int i = 0; i < mapTA.size(); i++) {
                MapAsset m = mapTA.get(i);
                gasMarker.add(map.addMarker(new MarkerOptions()
                        .position(m.getLatLng())
                        .icon(m.getIcon())
                        .title(m.Name)
                        .snippet(m.Number + ", " + m.Location)));
            }
        }

        ArrayList<RestArea> restAreas = getRestAreasIn100MilesRange(myLocation);
        if (restAreas != null && isRestStation) {
//            Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: restAreas: " + restAreas.size());
            for (int i = 0; i < restAreas.size(); i++) {
                RestArea m = restAreas.get(i);
                restMarker.add(map.addMarker(new MarkerOptions()
                        .position(m.getLatLng())
                        .icon(m.getIcon())
                        .title(m.Name)
                        .snippet("Road: " + m.Road)));
            }
        }

        if (!isRestStation) {
            for (int i = 0; i < restMarker.size(); i++)
                restMarker.get(i).remove();
        }
        if (!isGasStation) {
            for (int i = 0; i < gasMarker.size(); i++)
                gasMarker.get(i).remove();
        }
    }

    public City getNearestCity(double myLat, double myLon) {
        if (cities == null)
            return null;

        City result = null;
        Double shortestDistanceInKm = null;

        for (int i = 0; i < cities.size(); i++) {
            City m = cities.get(i);

            if (m.Lat == null || m.Lon == null)
                continue;

            Double distanceInKm = calculateDistanceInKm(myLat, myLon, m.Lat, m.Lon);

            if (shortestDistanceInKm == null || distanceInKm < shortestDistanceInKm) {
                shortestDistanceInKm = distanceInKm;
                result = m;
            }
        }

        return result;
    }

    public HashMap<String, Object> getNearestLocationInfo(String latStr, String lonStr) {
        Log.d(TAG, "getNearestLocationInfo: latStr: " + latStr + " lonStr: " + lonStr);
        if (latStr == null || lonStr == null) {
            return null;
        }

        latStr = latStr.replace(",", ".");
        lonStr = lonStr.replace(",", ".");
        Log.d(TAG, "getNearestLocationInfo: ");

        double kmInMiles = 0.621371192;
        HashMap<String, Object> result = new HashMap();

        double lat = Double.parseDouble(latStr);
        double lon = Double.parseDouble(lonStr);

        City city = getNearestCity(lat, lon);

        if (city == null)
            return null;

        Double distanceInKm = calculateDistanceInKm(lat, lon, city.Lat, city.Lon);

        if (lat == 0 || lon == 0)
            distanceInKm = 0d;

        Integer distanceInMiles = (int) (distanceInKm * kmInMiles);
        String distanceInMilesStr = Integer.toString(distanceInMiles);

        if (distanceInKm == null)
            return null;

        LatLng latLng = city.getLatLng();
        double angle = calculateAngleBetweenGeoCoords(new LatLng(lat, lon), latLng);

        angle = angle * 100;

        if (angle < 0)
            angle = 360 + angle;

        String windRoseRepresentation = new WindRose().getWindRoseRepresentation(angle);

        result.put("Latitude", latLng.latitude);
        result.put("Longitude", latLng.longitude);
        result.put("Distance", distanceInKm);
        result.put("DistanceMiles", distanceInMilesStr);
        result.put("Angle", angle);
        result.put("WindRose", windRoseRepresentation);
        result.put("City", city.City);
        result.put("State", city.State);

        Log.d(TAG, "getNearestLocationInfo: result: " + result);
        return result;
    }

    public String getLocalizationStr(Activity a) {
        String latitude = getBleParameter("Latitude");
        String longitude = getBleParameter("Longitude");

        Location loc = getPhoneLastBestLocation(a);

        if (loc != null) {
            if (latitude == null || longitude == null) {
                double lat = loc.getLatitude();
                double lon = loc.getLongitude();

                latitude = Double.toString(lat);
                longitude = Double.toString(lon);

                if (latitude != null)
                    latitude = latitude.replace(",", ".");

                if (longitude != null)
                    longitude = longitude.replace(",", ".");
            }
        }

        return getLocalizationStr(a, latitude, longitude);
    }

    public String getLocalizationStr(Activity a, String lat, String lon) {
        Log.d(TAG, "getLocalizationStr: ");
        try {

//         March 16, 2022   -   Fixing the issue regarding latitude and longitude -1.000
            Double absoluteLatitude = 0.0, absoluteLongitude = 0.0;
            if (lat != null) {
                absoluteLatitude = Math.abs(Double.parseDouble(lat));
            }
            if (lon != null) {
                absoluteLongitude = Math.abs(Double.parseDouble(lon));
            }

//            if (StringUtils.isNullOrWhitespacesAny(new String[]{lat, lon}))
            if (StringUtils.isNullOrWhitespacesAny(new String[]{lat, lon}) || (absoluteLatitude <= 1 || absoluteLongitude <= 1)) {
                return null;
            }

            // Format: [Miles from nearest location], Wind Rose, Address. Ex: 4.75 mi WNW Los Angeles CA

            HashMap<String, Object> info = getNearestLocationInfo(lat, lon);
            Log.d(TAG, "getLocalizationStr: info: " + info);
            if (info == null)
                return null;

            String result = info.get("DistanceMiles").toString() + " miles " + info.get("WindRose").toString()
                    + " " + info.get("City").toString() + " " + info.get("State").toString();
            Log.d(TAG, "getLocalizationStr: result: " + result);

            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "getLocalizationStr: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }


    //    June 16, 2022 -   We need to get the current state based on the provided latitude and longitude
    public String getCurrentState(Activity a) {
        try {
            String lat = "", lon = "";
            Location l = getPhoneLastBestLocation(a);
            if (l != null) {
                lat = Double.toString(l.getLatitude());
                lon = Double.toString(l.getLongitude());
            }

//         March 16, 2022   -   Fixing the issue regarding latitude and longitude -1.000
            Double absoluteLatitude = 0.0, absoluteLongitude = 0.0;
            if (lat != null) {
                absoluteLatitude = Math.abs(Double.parseDouble(lat));
            }
            if (lon != null) {
                absoluteLongitude = Math.abs(Double.parseDouble(lon));
            }

//            if (StringUtils.isNullOrWhitespacesAny(new String[]{lat, lon}))
            if (StringUtils.isNullOrWhitespacesAny(new String[]{lat, lon}) || (absoluteLatitude <= 1 || absoluteLongitude <= 1)) {
                return null;
            }

            // Format: [Miles from nearest location], Wind Rose, Address. Ex: 4.75 mi WNW Los Angeles CA

            HashMap<String, Object> info = getNearestLocationInfo(lat, lon);

            if (info == null)
                return null;

            String result = info.get("State").toString();

            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public double calculateAngleBetweenGeoCoords(LatLng coords1, LatLng coords2) {
        double M_PI = 3.14159265358979323846264338327950288;

        double lat1 = coords1.latitude;
        double long1 = coords1.longitude;

        double lat2 = coords2.latitude;
        double long2 = coords2.longitude;

        double dy = lat2 - lat1;
        double dx = Math.cos(M_PI / 180 * lat1) * (long2 - long1);
        double angle = Math.atan2(dy, dx);

        return angle;
    }

    //endregion

    //region Driver login management

    private boolean isDriverLoggedIn = false;

    public boolean isDriverLoggedIn() {
//        Log.d(TAG, "isDriverLoggedIn: isDriverLoggedIn: " + isDriverLoggedIn);
        return this.isDriverLoggedIn;
    }

    public boolean isDriverInDutyStatus() {
        if (!isDriverLoggedIn())
            return false;

        return getLastEldEventCode() == null || getLastEldEventCode() == BusinessRules.EventCode.CLEARED_PC_YM_WT;
    }

    public boolean isDriverSkillLevelBeginner() {
        Log.d(TAG, "isDriverSkillLevelBeginner: user: " + user);
        String skillLevel = user.getSkillLevel();
        Log.d(TAG, "isDriverSkillLevelBeginner: skillLevel: " + skillLevel);
        if (StringUtils.isNullOrWhitespaces(skillLevel))
            return false;

        return skillLevel.equalsIgnoreCase("Beginner");
    }

    public boolean existsDriverTruckNumber() {
        return user != null && user.getTruckNumber() != null;
    }

    public void loginAuthenticatedUserAsDriver() {
        isDriverLoggedIn = true;
        setSetting("isdriverloggedin", "true");
    }

    public void logoutDriver() {
        isDriverLoggedIn = false;
        setSetting("isdriverloggedin", "false");
    }

    public boolean existsPreviousDriverLoginStatus() {
        String isDriverLoggedIn = getSetting("isdriverloggedin");

        if (isDriverLoggedIn == null)
            return false;

        return isDriverLoggedIn.equalsIgnoreCase("true");
    }

    private Driver getDriver(String rmsUserId) {
        Cursor c = db.getQuery("SELECT * FROM driver WHERE rmsUserId='" + rmsUserId + "'");
        c.moveToFirst();

        Driver result = new Driver();
        int counter = 0;

        if (!c.isAfterLast()) {
            counter++;
            result.setLobjectId(c.getString(counter++));
            result.setObjectType(c.getString(counter++));
            counter++;
            result.RmsTimestamp = c.getString(counter++);
            result.MobileRecordId = c.getString(counter++);
            result.OrganizationName = c.getString(counter++);
            result.OrganizationNumber = c.getString(counter++);
            counter++;
            result.CurrentUserIdentification = c.getString(counter++);
            counter++;
            result.UserGroupName = c.getString(counter++);
            result.Company = c.getString(counter++);
            result.LastName = c.getString(counter++);
            result.FirstName = c.getString(counter++);
            result.Address1 = c.getString(counter++);
            result.Address2 = c.getString(counter++);
            result.City = c.getString(counter++);
            result.State = c.getString(counter++);
            result.ZipCode = c.getString(counter++);
            result.Country = c.getString(counter++);
            result.Email = c.getString(counter++);
            result.CurrentPhone = c.getString(counter++);
            result.ItemType = c.getString(counter++);
            result.Latitude = c.getString(counter++);
            result.Longitude = c.getString(counter++);
            result.DriverLicenseNumber = c.getString(counter++);
            result.DriverLicenseState = c.getString(counter++);
            result.Location = c.getString(counter++);
            result.CustomerNumber = c.getString(counter++);
            result.DateOfHire = c.getString(counter++);
            result.DateOfBirth = c.getString(counter++);
            result.UserType = c.getString(counter++);
            result.EmployeeId = c.getString(counter++);
            result.Status = c.getString(counter++);
            result.Sex = c.getString(counter++);
            result.NationalIdentifier = c.getString(counter++);
            result.ContactName = c.getString(counter++);
            result.MiddleName = c.getString(counter++);
            result.DriverLicenseClass = c.getString(counter++);
            result.Endorsements = c.getString(counter++);
            result.DriverLicenseExpirationDate = c.getString(counter++);
            result.DotExpirationDate = c.getString(counter++);
            result.CorrectiveLensRequired = c.getString(counter++);
            result.Category1Name = c.getString(counter++);
            result.Category1Value = c.getString(counter++);
            result.Category2Name = c.getString(counter++);
            result.Category2Value = c.getString(counter++);
            result.Category3Name = c.getString(counter++);
            result.Category3Value = c.getString(counter++);
            result.Category4Name = c.getString(counter++);
            result.Category4Value = c.getString(counter++);
            result.Category5Name = c.getString(counter++);
            result.Category6Name = c.getString(counter++);
            result.Category6Value = c.getString(counter++);
            result.MobilePhoneNumber = c.getString(counter++);
            result.HomePhoneNumber = c.getString(counter++);
            result.WorkPhoneNumber = c.getString(counter++);
            result.NotifyGroupName = c.getString(counter++);
            result.Rating = c.getString(counter++);
            result.NumberOfReviews = c.getString(counter++);
            result.Quota = c.getString(counter++);
            result.ManagerName = c.getString(counter++);
            result.ManagerRecordId = c.getString(counter++);
            result.ManagerEmployeeId = c.getString(counter++);
            result.Territory = c.getString(counter++);
            result.RmsUserId = c.getString(counter++);
            result.ExemptDriverStatus = c.getString(counter++);
            result.SkillLevel = c.getString(counter++);
        }

        c.close();
        return result;
    }


    public List<Driver> getDriverList() {

//        Log.d(TAG, "getDriverList: drivers: size: " + drivers.size());
        return drivers;
    }

    //    private Driver getDriversList(String rmsUserId) {
    public Driver getDriversList() {
//        Cursor c = db.getQuery("SELECT * FROM driver WHERE rmsUserId='" + rmsUserId + "'");
        Cursor c = db.getQuery("SELECT * FROM driver");
//        Log.d(TAG, "getDriversList: cursor: " + c.getCount());
        c.moveToFirst();

        Driver result = new Driver();
        int counter = 0;

        if (!c.isAfterLast()) {
            counter++;
            result.setLobjectId(c.getString(counter++));
            result.setObjectType(c.getString(counter++));
            counter++;
            result.RmsTimestamp = c.getString(counter++);
            result.MobileRecordId = c.getString(counter++);
            result.OrganizationName = c.getString(counter++);
            result.OrganizationNumber = c.getString(counter++);
            counter++;
            result.CurrentUserIdentification = c.getString(counter++);
            counter++;
            result.UserGroupName = c.getString(counter++);
            result.Company = c.getString(counter++);
            result.LastName = c.getString(counter++);
            result.FirstName = c.getString(counter++);
            result.Address1 = c.getString(counter++);
            result.Address2 = c.getString(counter++);
            result.City = c.getString(counter++);
            result.State = c.getString(counter++);
            result.ZipCode = c.getString(counter++);
            result.Country = c.getString(counter++);
            result.Email = c.getString(counter++);
            result.CurrentPhone = c.getString(counter++);
            result.ItemType = c.getString(counter++);
            result.Latitude = c.getString(counter++);
            result.Longitude = c.getString(counter++);
            result.DriverLicenseNumber = c.getString(counter++);
            result.DriverLicenseState = c.getString(counter++);
            result.Location = c.getString(counter++);
            result.CustomerNumber = c.getString(counter++);
            result.DateOfHire = c.getString(counter++);
            result.DateOfBirth = c.getString(counter++);
            result.UserType = c.getString(counter++);
            result.EmployeeId = c.getString(counter++);
            result.Status = c.getString(counter++);
            result.Sex = c.getString(counter++);
            result.NationalIdentifier = c.getString(counter++);
            result.ContactName = c.getString(counter++);
            result.MiddleName = c.getString(counter++);
            result.DriverLicenseClass = c.getString(counter++);
            result.Endorsements = c.getString(counter++);
            result.DriverLicenseExpirationDate = c.getString(counter++);
            result.DotExpirationDate = c.getString(counter++);
            result.CorrectiveLensRequired = c.getString(counter++);
            result.Category1Name = c.getString(counter++);
            result.Category1Value = c.getString(counter++);
            result.Category2Name = c.getString(counter++);
            result.Category2Value = c.getString(counter++);
            result.Category3Name = c.getString(counter++);
            result.Category3Value = c.getString(counter++);
            result.Category4Name = c.getString(counter++);
            result.Category4Value = c.getString(counter++);
            result.Category5Name = c.getString(counter++);
            result.Category6Name = c.getString(counter++);
            result.Category6Value = c.getString(counter++);
            result.MobilePhoneNumber = c.getString(counter++);
            result.HomePhoneNumber = c.getString(counter++);
            result.WorkPhoneNumber = c.getString(counter++);
            result.NotifyGroupName = c.getString(counter++);
            result.Rating = c.getString(counter++);
            result.NumberOfReviews = c.getString(counter++);
            result.Quota = c.getString(counter++);
            result.ManagerName = c.getString(counter++);
            result.ManagerRecordId = c.getString(counter++);
            result.ManagerEmployeeId = c.getString(counter++);
            result.Territory = c.getString(counter++);
            result.RmsUserId = c.getString(counter++);
            result.ExemptDriverStatus = c.getString(counter++);
            result.SkillLevel = c.getString(counter++);
//            Log.d(TAG, "getDriversList: name: " + result.FirstName + " " + result.MiddleName + " " + result.LastName + " " + result.Company + " " + result.JobType + " " + result.UserType);
        }

        c.close();
        return result;
    }


    public String getDrivingSpeed() {
        String speed = getBleParameter("Speed");

        if (!StringUtils.isNullOrWhitespaces(speed))
            return speed;

//        April 29, 2022    -   We should not return GPS SPeed if the ble parameter is null
//        String gpsSpeed = getBleParameter("GpsSpeed");
//
//        if (!StringUtils.isNullOrWhitespaces(gpsSpeed))
//            return gpsSpeed;

        return "-1";
    }

    public boolean existsVinEldEventForToday() {
        lock.lock();

        try {
            return db.exists("SELECT * FROM eldevents WHERE driverRecordId='" + user.getRecordId() + "' AND " +
                    "eventType='" + EventType.VIN.value + "' AND eventCode='" + EventCode.VIN.value + "' AND creationdate LIKE '" + DateUtils.getNowYyyyMmDd() + " %'");
        } finally {
            lock.unlock();
        }
    }

    //    public void recordVinEldEvent(Activity a, String vin) throws Exception {
    public void recordVinEldEvent(Activity a, String vin) {
        EventCode oldEventCode = this.eventCode;

        setEldEvent(a,
                EventType.VIN,
                EventCode.VIN,
                EventRecordStatus.ACTIVE,
                EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD, "VIN", vin, "", "");

        this.eventCode = oldEventCode;
    }

    public String getLastKnownVin(String username) {
        lock.lock();

        try {
            Cursor c = db.getRow("SELECT annotation FROM eldevents WHERE eldUsername='" + username + "' AND EventType=" +
                    EventType.VIN.getValue() + " AND EventCode=" + EventCode.VIN.getValue() + " ORDER BY id DESC LIMIT 1");

            if (c == null)
                return null;

            String result = "";
            c.moveToFirst();

            if (!c.isAfterLast())
                result = c.getString(0);

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    public boolean existsReportingPeriod() {
        try {
            return getDriverReportingPeriod() != null;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return false;
        }
    }

    public Integer getDriverReportingPeriod() throws Exception {
        User u = getAuthenticatedUser();

        return u.getReportingPeriod();
    }

    //endregion

    //region ELD Events management

    //    March 17, 2022    -   Changed Min Speed Detection to 0 because its cording onDuty
    //    when the car stops at traffic lights
//    private double MIN_VEHICLE_MOVING_DETECTION_SPEED = 10;
    private double MIN_VEHICLE_MOVING_DETECTION_SPEED = 0;

    private EventCode eventCode;
    //    March 15, 2022
    private EventCode eventCodeDutyEvent = EventCode.NOT_SET;

    private BleConnect bleDevice;

    private Integer latestShiftStartEldId;

    private static HashMap<String, String> bleParameters = new HashMap<String, String>();

    public boolean isCurrentEldStatusDriving() {
//        March 17, 2022    -   Added check for eventCodeDutyEvent if its equal to Driving
        return (eventCode != null && eventCode == EventCode.DRIVING) || (eventCodeDutyEvent == EventCode.DRIVING);
    }


    public boolean isVehicleMoving() {
//        Log.d(TAG, "isVehicleMoving: drivingSpeed: " + getDrivingSpeed());
        if (getDrivingSpeed() == null)
            return false;

        try {
            double drivingCurrentSpeed = Double.parseDouble(getDrivingSpeed());
//            MainMenuActivity.logDataArrayList.add();
            addDataInLogData("drivingCurrentSpeed: " + drivingCurrentSpeed);

//            Log.d(TAG, "isVehicleMoving: eldDrivingSpeed: " + eldDrivingSpeed);
            if (eldDrivingSpeed > 0) {
//                March 18, 2022    -   Previous speed is greater than zero that's mean the vehicle is moving
                if (drivingCurrentSpeed == 0) {
                    Long currentTimeStamp = DateUtils.getTimestamp();
                    eldIdleTimeStamp = currentTimeStamp;
                } else {
                    eldIdleTimeStamp = 0L;
                }
                eldDrivingSpeed = drivingCurrentSpeed;
            } else {
//                Log.d(TAG, "isVehicleMoving: else: eldDrivingSpeed: " + eldDrivingSpeed);
                eldDrivingSpeed = drivingCurrentSpeed;

                if (eldDrivingSpeed > 0) {
                    // we should reset the timestamp. We got a positive speed
                    eldIdleTimeStamp = 0L;
//                    Log.d(TAG, "isVehicleMoving: else: if: eldDrivingSpeed > 0");
                    return true;
                }
                if (eldIdleTimeStamp == 0L) {
                    // we don’t have a timestamp, so we were not driving before
                    return false;
                }

                Long currentTimeStamp = DateUtils.getTimestamp();
                Long delta = currentTimeStamp - eldIdleTimeStamp;
//                Log.d(TAG, "isVehicleMoving: delta: " + delta);
//                Log.d(TAG, "isVehicleMoving: delta: isGreater: " + (delta > (5 * 60 * 1000)));
                if (delta > (5 * 60 * 1000)) {
//                    Log.d(TAG, "isVehicleMoving: before todo");
//                    Log.d(TAG, "isVehicleMoving: delta: before todo");
//                    Todo=>    Show a popup to ask about current driving state, is it still Driving or OnDuty
//                    Log.d(TAG, "isVehicleMoving: delta: after todo");
//                    To do=>    Show a popup to ask about current driving state, is it still Driving or OnDuty

                    return false;
                } else {
//                    Log.d(TAG, "isVehicleMoving: else: delta > (5 * 60 * 1000)");

                    return true;
                }
            }

            return drivingCurrentSpeed > MIN_VEHICLE_MOVING_DETECTION_SPEED;
        } catch (Throwable throwable) {
            Log.d(TAG, "isVehicleMoving: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();
        }

        return false;
    }

    public BleConnect getBleDevice() {
        return bleDevice;
    }

    boolean isDialogToEnableBlueToothAlreadyShown = false;

    public void startBleListener() {
        addDataInLogData("startBleListener: ");
        final Activity a = (Activity) lastCtx;
        Log.d(TAG, "startBleListener: lastCtx: " + a);
        isBleConnected = false;

        IOnBleDataUpdate onBleDataUpdate = new IOnBleDataUpdate() {
            @Override
            public void OnBleStatus(String d) {
                Log.d(TAG, "OnBleStatus: d: " + d);
                a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        if (false && isDebugMode() && isFernandosDebuggingDevices())
////                        if (false && isDebugMode())
//                            UiUtils.showToast(a, d);

                        addDataInLogData("startBleListener: OnBleStatus: d: " + d);
                        if (d == null || d.contains("No ELD found") || d.contains("Not connected") || d.contains("Connection failed") ||
                                d.contains("Unable to enable bluetooth") || d.contains("Disconnected? Yes") || d.contains("New state of connection: 0")) {

                            addDataInLogData("startBleListener: isBleConnected: " + isBleConnected);
//                            April 20, 2022    -   We need to inform the user when the eld device got disconnected
                            if (isBleConnected) {
                                if (d.contains("Disconnected? Yes") || d.contains("New state of connection: 0")) {


                                    UiUtils.showDialog_Without_Listener(a, "Notification",
                                            "ELD device disconnected.", "Close");
                                }
                            }

                            isBleConnected = false;
                            addDataInLogData("startBleListener: OnBleStatus: isBleConnected = false  ");
                        } else if (d.contains("Connected to") || d.contains("Now connected to ELD device ") || d.contains("Bluetooth enabled - now scanning for")) {

                            isBleConnected = true;
                            addDataInLogData("startBleListener: OnBleStatus: isBleConnected = true  ");
                        }
                    }
                });
            }

            @Override
            public void OnBleData(final String dStr) {
                Log.d(TAG, "OnBleData: dStr: " + dStr);
                try {
                    if (dStr == null || dStr.indexOf(": ") == -1)
                        return;

                    String[] dSplit = dStr.split(": ");

                    if (dSplit == null)
                        return;

                    final String d = dSplit[0].trim().toUpperCase();
                    final String value = dSplit.length < 2 || dSplit[1] == null || dSplit[1].equalsIgnoreCase("null") ? null : dSplit[1].trim();

                    if (bleParameters.containsKey(d))
                        bleParameters.remove(d);

                    bleParameters.put(d, value);

                    /*a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            UiUtils.showToast(a, ">>>" + dStr);
                        }
                    });*/
                } catch (final Throwable throwable) {
//                    MainMenuActivity.logDataArrayList.add("startBleListener: OnBleData: Throwable: " + throwable.getMessage() + "\n" +
//                            DateUtils.getNowYyyyMmDdHhmmss());
                    addDataInLogData("startBleListener: OnBleData: Throwable: " + throwable.getMessage());
                    if (throwable != null)
                        throwable.printStackTrace();

                    a.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (throwable != null)
                                UiUtils.showToast(a, throwable.toString() + "\\" + StringUtils.dumpArray(throwable.getStackTrace()));
                        }
                    });
                }
            }
        };

        bleDevice = isSimulatedBleDevice && isDebugMode() && isFernandosDebuggingEmulators() ?
                new BleSimulator(a, onBleDataUpdate) : new BleConnect(a, onBleDataUpdate);

        bleDevice.enableDtcParameters();

        Log.d(TAG, "startBleListener: findAndConnectToBleDevice: ");

//        Nov 23, 2022  -   We were getting a crash when "scanner already started with given callback"
//        because in isBluetoothNotEnabled() they were calling eldManager.ScanForElds(scanCallback) just to
//        see that do we have bluetooth enabled so I replaced that function with isBluetoothEnabled() to know if
//        bluetooth enabled - Its working fine
        if (bleDevice.isBluetoothNotEnabled() && !isDialogToEnableBlueToothAlreadyShown) {
//        if (!bleDevice.isBluetoothEnabled() && !isDialogToEnableBlueToothAlreadyShown) {
            bleDevice.findAndConnectToBleDevice();
            isDialogToEnableBlueToothAlreadyShown = true;
        }

    }

    public String getBleParameter(String key) {
        if (key == null)
            return null;

        key = key.toUpperCase();

        if (!bleParameters.containsKey(key))
            return null;


//        May 05, 2022  -   We need to check If the key is related with trip distance/odometer then we need to convert it into miles
        if (key.equalsIgnoreCase("TripDistance") || key.equalsIgnoreCase("Odometer")) {

            Double speedInMile = Utils.convertKmsToMiles(Double.parseDouble(bleParameters.get(key)));
            return "" + speedInMile;
        } else {
            return bleParameters.get(key);
        }
    }

    public HashMap<String, String> getBleParameters() {
        return bleParameters;
    }

    public String getBleParametersAsStr() {
        if (bleParameters == null)
            return null;

        StringBuilder result = new StringBuilder();
        Set<String> keys = bleParameters.keySet();

        for (String key : keys) {
            String v = bleParameters.get(key);

            result.append("(" + key + "," + v + "), ");
        }

        return result.toString();
    }

    private static boolean isBleConnected = false;
    private static boolean isBleConnectedByForce = false;

    public void forceBleToConnectedState() {
//        April 12, 2022    -   We need to be able to set the ble to connected forcefully especially in the case where ble is broken
//        isBleConnected = true;
        isBleConnectedByForce = true;
    }

    public boolean isBleConnected() {
        Log.d(TAG, "ELD: isBleConnected: isBleConnected: " + isBleConnected);
        Log.d(TAG, "ELD: isBleConnected: isBleConnectedByForce: " + isBleConnectedByForce);
        Log.d(TAG, "ELD: isBleConnected: bleDevice: " + bleDevice);
        if (bleDevice != null) {
            Log.d(TAG, "ELD: isBleConnected: bleDevice.isBleDataDeprecated(): " + bleDevice.isBleDataDeprecated());
        }
        return (isBleConnected || isBleConnectedByForce) && !bleDevice.isBleDataDeprecated();
    }

    public void forceBleToDisconnectedState() {
        try {
            getBleDevice().disconnect();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        isBleConnected = false;
    }

    public void setEldEventToOnDuty(Activity a) {
        this.eventCode = EventCode.ON_DUTY_NOT_DRIVING;
//        March 15, 2020 -  Saved the most recent event code
        this.eventCodeDutyEvent = EventCode.ON_DUTY_NOT_DRIVING;

        setEldEvent(a, EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, eventCode,
                BusinessRules.EventRecordStatus.ACTIVE, EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "On Duty", "setEldEventToOnDuty", "", "");

        setDrivingStatus("On Duty");
    }

    public void setEldEventToOnDuty(Activity a, String annotation) {
        this.eventCode = EventCode.ON_DUTY_NOT_DRIVING;
//        March 15, 2020 -  Saved the most recent event code
        this.eventCodeDutyEvent = EventCode.ON_DUTY_NOT_DRIVING;

        setEldEvent(a, EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, eventCode,
                BusinessRules.EventRecordStatus.ACTIVE, EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "On Duty", annotation, "", "");

        setDrivingStatus("On Duty");

    }

    public void setEldEventToOffDuty(Activity a) {
        this.eventCode = EventCode.OFF_DUTY;
//        March 15, 2020 -  Saved the most recent event code
        this.eventCodeDutyEvent = EventCode.OFF_DUTY;

        setEldEvent(a, EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, eventCode,
                BusinessRules.EventRecordStatus.ACTIVE, EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Off Duty", "", "", "");

        setDrivingStatus("Off Duty");
    }

    //    March 21, 2022    -
//    Added a new parameter in setEldEventToDriving annotation to check who is calling the function twice
//    Note- Later removed it
    public void setEldEventToDriving(Activity a, String annotation) {
//        Log.d(TAG, "setEldEventToDriving: " + a);

        if (!isEngineOn()) {
//          March 21, 2022    -   We need to make sure the engine is on
            return;
        }

        this.eventCode = EventCode.DRIVING;
//        March 15, 2020 -  Saved the most recent event code
        this.eventCodeDutyEvent = EventCode.DRIVING;

//        March 17, 2022    -   Change the Event Code Origin
//        setEldEvent(a, EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, eventCode,
//                BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Driving", "", "", "");
        setEldEvent(a, EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, eventCode,
                BusinessRules.EventRecordStatus.ACTIVE, EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD, "Driving", annotation, "", "");


//        July 18, 2022 -  We should make sure that we saved driving state
        this.eventCode = EventCode.DRIVING;
        this.eventCodeDutyEvent = EventCode.DRIVING;

        if (bleDevice != null)
            bleDevice.resetBleDataCounters();
    }

    public void setIndicationOfAuthorizedPersonalUseOfCmvOrYardMovesEldEvent(Activity
                                                                                     a, EventCode eventCode, String eventCodeDescription, String annotation) {
        setEldEvent(a,
                BusinessRules.EventType.A_CHANGE_IN_DRIVERS_INDICATION_OF_AUTHORIZED_PERSONAL_USE_OF_CMV_OR_YARD_MOVES,
                eventCode,
                BusinessRules.EventRecordStatus.ACTIVE,
                BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER,
                eventCodeDescription, annotation, "", "");
    }

    public boolean isIndicationOfAuthorizedPersonalUseOnDuty() {
        return indicationOfAuthorizedPersonalUseOfCmvOrYardMoves == null || indicationOfAuthorizedPersonalUseOfCmvOrYardMoves == EventCode.CLEARED_PC_YM_WT;
    }

    public void setDriverEldEvent(Activity a, EventCode eventCode, String
            eventCodeDescription, String annotation) {
        // 2022.03.22
        /*
        setEldEvent(a, BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, eventCode,
                BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, eventCodeDescription, annotation, "", "");
        */
        BusinessRules.EventType eventType = EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS;
        if (eventCode == EventCode.ON_BREAK_STARTED) {
            eventType = EventType.BREAK;
            eventCodeDescription = "Break Started";
//        April 29, 2022    -   if we are changing the duty status then we should idle the time set
            eldIdleTimeStamp = 0L;

        } else if (eventCode == EventCode.ON_BREAK_ENDED) {
            eventType = EventType.BREAK;
            eventCodeDescription = "Break Ended";
        } else if (eventCode == EventCode.SPEEDING_STARTED || eventCode == EventCode.SPEEDING_ENDED) {
            eventType = EventType.SPEEDING;
        }
        setEldEvent(a, eventType, eventCode,
                BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, eventCodeDescription, annotation, "", "");
    }

    public EldEvent generateCertifyEvent(Activity a, String eventCodeDescription) {
        EventCode eventCode = BusinessRules.EventCode.FIRST_CERTIFICATION_DAILY_RECORD;
        String annotation = null;
        EventType eventType = EventType.A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS;
        EventRecordStatus eventRecordStatus = EventRecordStatus.ACTIVE;
        EventCodeOrigin eventCodeOrigin = BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER;
        String malfunctionDiagnosticCode = "";
        String malfunctionDiagnosticDescp = "";

        String vin = getBleParameter("VIN");
        String speed = getBleParameter("Speed");

        String vehicleMiles = getBleParameter("TripDistance");
        String odometer = getBleParameter("Odometer");

        String engineHours = eventType == BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS ?
                getBleParameter("EngineHours") : getBleParameter("TripHours");

        String lat = getBleParameter("Latitude");
        String lon = getBleParameter("Longitude");

//         March 16, 2022   -   Fixing the issue regarding latitude and longitude -1.000
        Double absoluteLatitude = 0.0, absoluteLongitude = 0.0;
        if (lat != null) {
            absoluteLatitude = Math.abs(Double.parseDouble(lat));
        }
        if (lon != null) {
            absoluteLongitude = Math.abs(Double.parseDouble(lon));
        }

        if (StringUtils.isNullOrWhitespacesAny(new String[]{lat, lon}) || (absoluteLatitude <= 1 || absoluteLongitude <= 1)) {
            Location l = getPhoneLastBestLocation(a);
            if (l != null) {
                lat = Double.toString(l.getLatitude());
                lon = Double.toString(l.getLongitude());
            }
        }

        String localization = getLocalizationStr(a, lat, lon);
//        MainMenuActivity.logDataArrayList.add("generateCertifyEvent: localization: " + localization + "\n" +
//                DateUtils.getNowYyyyMmDdHhmmss());
        addDataInLogData("generateCertifyEvent: localization: " + localization);

        this.eventCode = eventCode;
//        March 15, 2020 -  Saved the most recent event code
        if (eventType == BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS) {
            this.eventCodeDutyEvent = eventCode;
        } else {
//            March 29, 2022    -   We should invalidate previous duty status
            this.eventCodeDutyEvent = EventCode.NOT_SET;
        }


        ContentValues cols = new ContentValues();
        EldEvent eldEvent = new EldEvent();
        eldEvent.Id = "" + getNextId();

        try {
            lat = MathUtils.roundTo2DecimalCases(lat);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        try {
            lon = MathUtils.roundTo2DecimalCases(lon);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        String lastDistanceMiles = "";

        try {
            HashMap<String, Object> result = getNearestLocationInfo(lat, lon);
            lastDistanceMiles = (String) result.get("DistanceMiles");


//            May 05, 2022  -   We should use the previous distance that we are calculating when we are checking the
//            current coordinates
            lastDistanceMiles = "" + previousDistance;

//            June 07, 2022 -   We need to make the difference between the current odometer and previous distance
            long delta = Long.parseLong(odometer) - previousDistance;
//            lastDistanceMiles = "" + previousDistance;
            lastDistanceMiles = "" + delta;

        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        Integer checkSum = eventDataCheckSum(eventType, eventCode, DateUtils.getNowYyyyMmDd(), DateUtils.getNowHhmmss(),
                vehicleMiles, engineHours, lat, lon, getOrderNumberCmv(), username);

        String checkDataValue = eventDataCheckStr(checkSum);
        String checkSumValue = Integer.toString(checkSum);

        String mobileRecordId = Rms.getMobileRecordId("ELDEvent");
        cols.put("mobileRecordId", mobileRecordId);
        eldEvent.MobileRecordId = mobileRecordId;

        String organizationName = getOrgName();
        cols.put("organizationName", organizationName);
        eldEvent.OrganizationName = organizationName;

        String organizationNumber = getOrgNumber();
        cols.put("organizationNumber", organizationNumber);
        eldEvent.OrganizationNumber = organizationNumber;

        String eldUsername = getLastLoggedInUsername();
        cols.put("eldUsername", eldUsername);
        eldEvent.EldUsername = eldUsername;

        cols.put("eventType", eventType.value);
        eldEvent.EventType = "" + eventType.value;

        cols.put("eventCode", eventCode.value);
        eldEvent.EventCode = "" + eventCode.value;

        cols.put("recordStatus", eventRecordStatus.value);
        eldEvent.RecordStatus = "" + eventRecordStatus.value;

        cols.put("recordOrigin", eventCodeOrigin.value);
        eldEvent.RecordOrigin = "" + eventCodeOrigin.value;

        cols.put("truckNumber", user.getTruckNumber());
        eldEvent.TruckNumber = user.getTruckNumber();

        cols.put("vin", vin);
        eldEvent.Vin = vin;

        cols.put("localizationDescription", localization);
        eldEvent.LocalizationDescription = localization;

        cols.put("latitudeString", lat);
        eldEvent.LatitudeString = lat;

        cols.put("longitudeString", lon);
        eldEvent.LongitudeString = lon;

        cols.put("dstSinceLastValidCoords", lastDistanceMiles);
        eldEvent.DstSinceLastValidCoords = lastDistanceMiles;

        cols.put("vehicleMiles", vehicleMiles);
        eldEvent.VehicleMiles = vehicleMiles;

        cols.put("engineHours", engineHours);
        eldEvent.EngineHours = engineHours;

        String orderNumbercmv = getOrderNumberCmv(); // TODO: Taken from the ELD Detail. Shipping info (where to go).
        cols.put("orderNumbercmv", orderNumbercmv);
        eldEvent.OrderNumberCmv = orderNumbercmv;

        String orderNumberUser = getOrderNumberUser(); // TODO: Taken from the ELD Detail. Shipping info (where to go).
        cols.put("orderNumberUser", orderNumberUser);
        eldEvent.OrderNumberUser = orderNumberUser;

//        int sequenceId = getNextSequenceId();
//        March 22, 2022    -   We should generate sequence id based on event type
//        because for custom event types, we should not generate sequence id
        int sequenceId = getNextSequenceIdForEventType(EventType.A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS);
        cols.put("sequenceId", sequenceId);
        eldEvent.SequenceId = "" + sequenceId;

        cols.put("eventCodeDescription", eventCodeDescription);
        eldEvent.EventCodeDescription = eventCodeDescription;

        String diagnostic = diagnosticIndicator ? "1" : "0";
        cols.put("diagnosticIndicator", diagnostic);
        eldEvent.DiagnosticIndicator = diagnostic;

        String malfunction = malfunctionIndicator ? "1" : "0";
        cols.put("malfunctionIndicator", malfunction);
        eldEvent.MalfunctionIndicator = malfunction;

        cols.put("annotation", annotation);
        eldEvent.Annotation = annotation;

        cols.put("recordOriginId", eventCodeOrigin.value);
        eldEvent.RecordOriginId = "" + eventCodeOrigin.value;

        cols.put("checkData", checkDataValue);
        eldEvent.CheckData = checkDataValue;

        cols.put("checkSum", checkSumValue);
        eldEvent.CheckSum = checkSumValue;

        cols.put("malfunctionDiagnosticCode", malfunctionDiagnosticCode);
        eldEvent.MalfunctionDiagnosticCode = malfunctionDiagnosticCode;

        cols.put("malfunctionDiagnosticDescp", malfunctionDiagnosticDescp);
        eldEvent.MalfunctionDiagnosticDescp = malfunctionDiagnosticDescp;

        cols.put("driverLastName", user != null ? user.getLastName() : "");
        eldEvent.DriverLastName = user != null ? user.getLastName() : "";

        cols.put("driverFirstName", user != null ? user.getFirstName() : "");
        eldEvent.DriverFirstName = user != null ? user.getFirstName() : "";

        cols.put("driverRecordId", user != null ? user.getRecordId() : "");
        eldEvent.DriverRecordId = user != null ? user.getRecordId() : "";

        cols.put("editReason", ""); // TODO: LogBook should allow to edit the ELD events by clonning it into a new db record and the cloned event should contain the reason for the edit, if present to pass here as the parameter
        eldEvent.EditReason = "";

        Double eventseconds = DateUtils.getTimestampInDouble();
        cols.put("eventseconds", eventseconds);
        eldEvent.EventSeconds = eventseconds;

        boolean isShiftStart = false;

        if (isShiftStart || latestShiftStartEldId == null)
            reCalculateNewShiftStartEldId();

        cols.put("shiftstart", isShiftStart ? "1" : "0");
        eldEvent.ShiftStart = isShiftStart ? "1" : "0";
//        Log.d(TAG, "generateCertifyEvent: ShiftStart: " + eldEvent.ShiftStart);

        cols.put("shiftstarteldid", latestShiftStartEldId);

        cols.put("senteldevent", "1");
        eldEvent.Sent = "1";

        String creationdate = DateUtils.getNowYyyyMmDdHhmmss();
        cols.put("creationdate", creationdate);
        eldEvent.CreationDate = creationdate;

        cols.put("odometer", odometer);
        eldEvent.Odometer = odometer;

        db.insert("eldevents", cols);
        return eldEvent;
    }

    public void setEldEvent(Activity a, EventType eventType, EventCode
            eventCode, EventRecordStatus eventRecordStatus,
                            EventCodeOrigin eventCodeOrigin, String eventCodeDescription, String
                                    annotation, String malfunctionDiagnosticCode, String malfunctionDiagnosticDescp) {
//        Log.d(TAG, "setEldEvent: recordVehiclePowerSwitchEvent: ");
        String vin = getBleParameter("VIN");
        String speed = getBleParameter("Speed");

//            May 02, 2022  -   We should not pass negative values while setting ELD

//        April 29, 2022    -   if we are changing the duty status then we should idle the time set
        if (eventType == EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS) {
            eldIdleTimeStamp = 0L;
        }

        String vehicleTripMiles = getBleParameter("TripDistance");
        if (vehicleTripMiles == null || vehicleTripMiles.isEmpty()) {
            vehicleTripMiles = "0";
        } else {

            int value = ((int) Double.parseDouble(vehicleTripMiles));
            if (value < 0) {
                value = 0;
            }
            vehicleTripMiles = "" + value;
        }

        String odometer = getBleParameter("Odometer");

        if (odometer == null || odometer.isEmpty()) {
            odometer = "0";
        } else {
            int odometerValue = ((int) Double.parseDouble(odometer));
            if (odometerValue < 0) {
                odometerValue = 0;
            }
            odometer = "" + odometerValue;
        }

//        Sep 23, 2022  -   For Duty events, we need to save trip distance and for the rest we need odometer
        String vehicleMiles = (eventType == BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS) ? vehicleTripMiles : odometer;


//        April 29, 2022    -   We need to make sure that the EngineHours and Trip to limits to only one digit
        String totalEngineHoursFromELD = getBleParameter("EngineHours");
        if (totalEngineHoursFromELD == null || totalEngineHoursFromELD.isEmpty()) {
            totalEngineHoursFromELD = "0.0";
        } else {
            int totalEngineHoursValue = ((int) Double.parseDouble(totalEngineHoursFromELD));
            if (totalEngineHoursValue < 0) {
                totalEngineHoursFromELD = "0.0";
            } else {
                totalEngineHoursFromELD = "" + round(Double.parseDouble(totalEngineHoursFromELD), 1);
            }
        }

        String tripHoursFromELD = getBleParameter("TripHours");
        if (tripHoursFromELD == null || tripHoursFromELD.isEmpty()) {
            tripHoursFromELD = "0.0";
        } else {
            int tripHoursValue = ((int) Double.parseDouble(tripHoursFromELD));
            if (tripHoursValue < 0) {
                tripHoursFromELD = "0.0";
            } else {
                tripHoursFromELD = "" + round(Double.parseDouble(tripHoursFromELD), 1);
            }
        }


//        Sep 23, 2022  -   For Duty status, we need to save trip hours and not total hours
//        String engineHours = eventType == BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS ? totalEngineHoursFromELD : tripHoursFromELD;
        String engineHours = eventType == BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS ? tripHoursFromELD : totalEngineHoursFromELD;

        String lat = getBleParameter("Latitude");
        String lon = getBleParameter("Longitude");

//         March 16, 2022   -   Fixing the issue regarding latitude and longitude -1.000
        Double absoluteLatitude = 0.0, absoluteLongitude = 0.0;
        if (lat != null) {
            absoluteLatitude = Math.abs(Double.parseDouble(lat));
        }
        if (lon != null) {
            absoluteLongitude = Math.abs(Double.parseDouble(lon));
        }

        if (StringUtils.isNullOrWhitespacesAny(new String[]{lat, lon}) || (absoluteLatitude <= 1 || absoluteLongitude <= 1)) {
            Location l = getPhoneLastBestLocation(a);

            if (l != null) {
                lat = Double.toString(l.getLatitude());
                lon = Double.toString(l.getLongitude());
            }
        }

        String localization = getLocalizationStr(a, lat, lon);
//        Log.d(TAG, "" + localization);
//        MainMenuActivity.logDataArrayList.add("setEldEvent: localization: " + localization + "\n" +
//                DateUtils.getNowYyyyMmDdHhmmss());
        addDataInLogData("setEldEvent: localization: " + localization + " latitude: " + lat + " longitude: " + lon);

//        Oct 26, 2022  -   We should not pass vehicleTripMiles
//        setEldEvent(a, eventType, eventCode, eventRecordStatus, eventCodeOrigin, vehicleTripMiles, odometer, vin, lat, lon, localization, engineHours, eventCodeDescription, annotation,
        setEldEvent(a, eventType, eventCode, eventRecordStatus, eventCodeOrigin, vehicleMiles, odometer, vin, lat, lon, localization, engineHours, eventCodeDescription, annotation,
                malfunctionDiagnosticCode, malfunctionDiagnosticDescp);
    }

    public static double round(double value, int places) {
        if (places < 0) throw new IllegalArgumentException();

        long factor = (long) Math.pow(10, places);
        value = value * factor;
        long tmp = Math.round(value);
        return (double) tmp / factor;
    }

    //    April 15, 2022    -   We need to reload the Diagnostic and Malfunction indicators to have the right value
    void reloadDiagnosticAndMalfunctionIndicators() {
        diagnosticIndicator = existsActiveDiagnostic();
        malfunctionIndicator = existsActiveMalfunction();
    }

    public void setEldEvent(Activity a, EventType eventType, EventCode
            eventCode, EventRecordStatus eventRecordStatus, EventCodeOrigin eventCodeOrigin,
                            String vehicleMiles, String odometer, String vin, String lat, String lon, String localization,
                            String engineHours, String eventCodeDescription, String annotation,
                            String malfunctionDiagnosticCode, String malfunctionDiagnosticDescp) {
//        Log.d(TAG, "setEldEvent: eventType: " + eventType + " eventCode: " + eventCode + " and reference: " + a);

        reloadDiagnosticAndMalfunctionIndicators();

        if (!malfunctionDiagnosticCode.equalsIgnoreCase("null")
                && malfunctionDiagnosticCode.equalsIgnoreCase("T")) {
//            Log.d(TAG, "MD: setEldEvent: malfunctionDiagnosticCode: " + malfunctionDiagnosticCode);
//            Log.d("myapp", Log.getStackTraceString(new Exception()));

//            String crashString="Name";
//            Integer.parseInt(crashString);
//            throw new AssertionError("Object cannot be null");
//            Log.d(TAG, "setEldEvent: crash: " + (1 / 0));
        }

        this.eventCode = eventCode;
//        March 15, 2020 -  Saved the most recent event code
        if (eventType == BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS) {
            this.eventCodeDutyEvent = eventCode;
        } else {
//            March 29, 2022    -   We should invalidate previous duty status
            this.eventCodeDutyEvent = EventCode.NOT_SET;
        }

//       March 23, 2022 -    We are using ON_BREAK_STARTED, before it as ON_BREAK
        if (eventCode == EventCode.ON_BREAK_STARTED)
            this.eventCodeDutyEvent = BusinessRules.EventCode.ON_DUTY_NOT_DRIVING;


        ContentValues cols = new ContentValues();

//        March 25, 2022-   For Break (STARTED or ENDED) event we should sent full latitude and longitude
//        July 07, 2022 -   Take out the try catch to detect if latitute or longitude generates a crash or not
//        try {
        if (eventCode != EventCode.ON_BREAK_STARTED && eventCode != EventCode.ON_BREAK_ENDED
                && eventCode != EventCode.SPEEDING_STARTED && eventCode != EventCode.SPEEDING_ENDED) {
            lat = MathUtils.roundTo2DecimalCases(lat);
        }
//        } catch (Throwable throwable) {
//            if (throwable != null)
//                throwable.printStackTrace();
//        }

//        try {
        if (eventCode != EventCode.ON_BREAK_STARTED && eventCode != EventCode.ON_BREAK_ENDED
                && eventCode != EventCode.SPEEDING_STARTED && eventCode != EventCode.SPEEDING_ENDED) {
            lon = MathUtils.roundTo2DecimalCases(lon);
        }
//        } catch (Throwable throwable) {
//            if (throwable != null)
//                throwable.printStackTrace();
//        }

        if (isPositionMalfunctionEvent())
            lat = lon = "E";
        else if (isPositionDiagnosticEvent())
            lat = lon = "X";

        String lastDistanceMiles = "";

//        try {

//            May 20, 2022  -   We should use the previous distance that we are calculating when we are checking the
//            current coordinates
        HashMap<String, Object> result = getNearestLocationInfo(lat, lon);
//        lastDistanceMiles = (String) result.get("DistanceMiles");

//        Log.d(TAG, "setEldEvent: check: previousDistance: " + previousDistance);
        if (previousDistance == null) {
            lastDistanceMiles = "0";
        } else {
//            June 07, 2022 -   We need to make the difference between the current odometer and previous distance
            long delta = Long.parseLong(odometer) - previousDistance;
//            lastDistanceMiles = "" + previousDistance;
            lastDistanceMiles = "" + delta;
        }
//        Log.d(TAG, "setEldEvent: check: lastDistanceMiles: " + lastDistanceMiles);


//        } catch (Throwable throwable) {
//            if (throwable != null)
//                throwable.printStackTrace();
//        }

        Integer checkSum = eventDataCheckSum(eventType, eventCode, DateUtils.getNowYyyyMmDd(), DateUtils.getNowHhmmss(),
                vehicleMiles, engineHours, lat, lon, getOrderNumberCmv(), username);

        String checkDataValue = eventDataCheckStr(checkSum);
        String checkSumValue = Integer.toString(checkSum);
//        Log.d(TAG, "setEldEvent: eventCode: " + eventCode);

        cols.put("mobileRecordId", Rms.getMobileRecordId("ELDEvent"));
        cols.put("organizationName", getOrgName());
        cols.put("organizationNumber", getOrgNumber());

//        April 29, 2022    -   If the user is not logged in then the automatic event should be unidentified
        if (isDriverLoggedIn()) {
            cols.put("eldUsername", getLastLoggedInUsername());
        } else {
            cols.put("eldUsername", "");
        }
        cols.put("eventType", eventType.value);
        cols.put("eventCode", eventCode.value);
        cols.put("recordStatus", eventRecordStatus.value);
        cols.put("recordOrigin", eventCodeOrigin.value);
        cols.put("truckNumber", user.getTruckNumber());
        cols.put("vin", vin);
        cols.put("localizationDescription", localization);
        cols.put("latitudeString", lat);
        cols.put("longitudeString", lon);
        cols.put("dstSinceLastValidCoords", lastDistanceMiles);
        cols.put("vehicleMiles", vehicleMiles);
        cols.put("engineHours", engineHours);
        cols.put("orderNumbercmv", getOrderNumberCmv()); // TODO: Taken from the ELD Detail. Shipping info (where to go).
        cols.put("orderNumberUser", getOrderNumberUser()); // TODO: Taken from the ELD Detail. Shipping info (where to go).

//        cols.put("sequenceId", getNextSequenceId());
//        March 22, 2022    -   We should generate sequence id based on event type
//        because for custom event types, we should not generate sequence id
//        cols.put("sequenceId", getNextSequenceIdForEventType(EventType.A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS));
        cols.put("sequenceId", getNextSequenceIdForEventType(eventType));

        cols.put("eventCodeDescription", eventCodeDescription);
        cols.put("diagnosticIndicator", diagnosticIndicator ? "1" : "0");
        cols.put("malfunctionIndicator", malfunctionIndicator ? "1" : "0");

//        Log.d(TAG, "MD: setEldEvent: malfunction: annotation: " + getEngineSynchronizationDiagnosticAnnotation());
        if (eventType == EventType.A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE
                && malfunctionDiagnosticCode.equalsIgnoreCase("2")) {
            cols.put("annotation", getEngineSynchronizationDiagnosticAnnotation());
        } else {
            cols.put("annotation", annotation);
        }

        cols.put("recordOriginId", eventCodeOrigin.value);
        cols.put("checkData", checkDataValue);
        cols.put("checkSum", checkSumValue);
        cols.put("malfunctionDiagnosticCode", malfunctionDiagnosticCode);
        cols.put("malfunctionDiagnosticDescp", malfunctionDiagnosticDescp);
        cols.put("driverLastName", user != null ? user.getLastName() : "");
        cols.put("driverFirstName", user != null ? user.getFirstName() : "");
        cols.put("driverRecordId", user != null ? user.getRecordId() : "");
        cols.put("editReason", ""); // TODO: LogBook should allow to edit the ELD events by clonning it into a new db record and the cloned event should contain the reason for the edit, if present to pass here as the parameter
        cols.put("eventseconds", DateUtils.getTimestampInDouble());

        boolean isShiftStart = false;
        EldEvent eldEvent = getLatestDutyOrDrivingIntermediateEvent();
        if (eldEvent == null)
            isShiftStart = true;
        else {
//            Log.d(TAG, "setEldEvent: eldEvent: eventCode: " + eldEvent.EventCode + " eventType: " + eldEvent.EventType + " creationDate: " + eldEvent.CreationDate);
//            May 30, 2022  -   Shift started should be first onDuty after a long(min 7 hours) sleeper
            Double currentTimeStamp = DateUtils.getTimestampInDouble();
            Double delta = currentTimeStamp - eldEvent.getEventSeconds();

//            Log.d(TAG, "setEldEvent: delta: " + delta);
//            June 13, 2022 -   We should use OffDuty as a sort of sleeper alternative
//            7 hours
            if (delta >= (7 * 60 * 60)) {
                isShiftStart = eventCode == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING && (isEldSleeperBerthStatus(eldEvent) || isEldOffDutyStatus(eldEvent));
                if (eventCode == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING && delta > (24 * 60 * 60)) {
//                    Log.d(TAG, "setEldEvent: eventCode: onDuty and delta is greater than 24 hours ");
                    isShiftStart = true;
                }
            } else {
//                Log.d(TAG, "setEldEvent: its not a shift started: time diffrence: " + delta);
            }
        }

//        Log.d(TAG, "generateCertifyEvent: getLatestEvent: " + eldEvent.RecordId);
//        Log.d(TAG, "generateCertifyEvent: latestEvent.EventCode: " + eldEvent.EventCode);
//        Log.d(TAG, "generateCertifyEvent: latestEvent.EventType: " + eldEvent.EventType);
//        Log.d(TAG, "generateCertifyEvent: eventCode: " + eventCode);
//        Log.d(TAG, "generateCertifyEvent: eventType: " + eventType);
        if (eldEvent == null)
            isShiftStart = true;
        else {
            if (eldEvent.EventCode.equalsIgnoreCase("1") && eldEvent.EventType.equalsIgnoreCase("5")
                    && eventCode == EventCode.ON_DUTY_NOT_DRIVING && eventType == EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS) {
//            June 01, 2022 -   if we are recording a OnDuty after login then we should set is Shift Start to yes
                isShiftStart = true;
            }
        }

        Log.d(TAG, "setEldEvent: isShiftStart before: " + isShiftStart);

        // 2022.08.19 we need to make sure that after we started a new cycle the onDuty has shiftStarted:1
        if (newCycleStarted && eventCode == EventCode.ON_DUTY_NOT_DRIVING && eventType == EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS) {
            Log.d(TAG, "setEldEvent: isShiftStart inside: " + isShiftStart);
            isShiftStart = true;
            // we need to rest the flag
            newCycleStarted = false;
        }

        Log.d(TAG, "setEldEvent: isShiftStart after: " + isShiftStart);

//        Log.d(TAG, "setEldEvent: generateCertifyEvent: isShiftStart: " + isShiftStart);
        if (isShiftStart || latestShiftStartEldId == null) {

            reCalculateNewShiftStartEldId();
        }

        cols.put("shiftstart", isShiftStart ? "1" : "0");
        cols.put("shiftstarteldid", latestShiftStartEldId);
        cols.put("senteldevent", "0");
        cols.put("creationdate", DateUtils.getNowYyyyMmDdHhmmss());

        // 2022.08.10 we should get the last valid odometer saved
        String odometerChecked = odometer;
        boolean getPrevOdometer = false;
        if ((odometer == null) || (odometer.isEmpty()) || (odometer.equalsIgnoreCase("0"))) {
            getPrevOdometer = true;
        }
        if (getPrevOdometer) {
            double odometerFromDB = getMostRecentOdometerFromDB();
            odometerChecked = "" + odometerFromDB;
        }
        // 22.08.10 we shoule check the odometer cols.put("odometer", odometer);
        cols.put("odometer", odometerChecked);

        Log.d(TAG, "setEldEvent: cols: "+cols);

        db.insert("eldevents", cols);
    }

    private BusinessRules.EventCode indicationOfAuthorizedPersonalUseOfCmvOrYardMoves;

    public void setIndicationOfAuthorizedPersonalUseOfCmvOrYardMoves(BusinessRules.EventCode v) {
        indicationOfAuthorizedPersonalUseOfCmvOrYardMoves = v;
    }

    public BusinessRules.EventCode getIndicationOfAuthorizedPersonalUseOfCmvOrYardMoves() {
        return indicationOfAuthorizedPersonalUseOfCmvOrYardMoves;
    }

    private String getOrderNumberCmv() {
        return "1";
    }

    private String getOrderNumberUser() {
        return "1";
    }

    public void reCalculateNewShiftStartEldId() {
        lock.lock();

        try {
            if (latestShiftStartEldId == null)
                latestShiftStartEldId = 1;

            Cursor c = db.getRow("SELECT id FROM eldevents ORDER BY id DESC LIMIT 1");

            if (c == null)
                return;

            c.moveToFirst();

            if (!c.isAfterLast())
                latestShiftStartEldId = c.getInt(0) + 1;

            c.close();
        } finally {
            lock.unlock();
        }
    }

    public int getNextSequenceIdForEventType(EventType eventType) {
        // 2022.03.22 for Break, Speeding and VIN we should not generate sequenceId
        if (eventType == EventType.BREAK) {
            return 0;
        }
        if (eventType == EventType.VIN) {
            return 0;
        }
        if (eventType == EventType.SPEEDING) {
            return 0;
        }
        try {
            int nextId = getNextSequenceId();
            int result = nextId - ((nextId / 65535) * 65535);

            //Log.d(TAG, "Next Sequence ID:" + result);
            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return -1;
        }
    }


    public int getNextSequenceId() {
        // 2022.03.22 we should get the max sequenceId from the local DB and increment that value
//        Cursor c = db.getRow("SELECT sequenceId FROM eldevents ORDER BY id DESC");
//        Cursor c = db.getRow("SELECT sequenceId FROM eldevents ORDER BY sequenceId DESC");
        Cursor c = db.getRow("SELECT sequenceId FROM eldevents ORDER BY sequenceId+0 DESC");

        if (c == null)
            return 0;

        c.moveToFirst();

        while (!c.isAfterLast()) {
            int id = c.getInt(0);
            return id + 1;
        }

        c.close();
        return 0;
    }

    public int getNextId() {
        Cursor c = db.getRow("SELECT id FROM eldevents ORDER BY id DESC");

        if (c == null)
            return 0;

        c.moveToFirst();

        while (!c.isAfterLast()) {
            int id = c.getInt(0);
            return id + 1;
        }

        c.close();
        return 0;
    }

    private String rmsRecordTypesUrlEncoded;

    public String getRmsRecordTypesUrlEncoded() {
        if (rmsRecordTypesUrlEncoded == null)
            rmsRecordTypesUrlEncoded = Rms.urlEncodeCommaDelimited(RMS_RECORD_TYPES).toString();

        return rmsRecordTypesUrlEncoded;
    }

    public enum EventType {
        NOT_SET(0),
        A_CHANGE_IN_DRIVER_DUTY_STATUS(1),
        AN_INTERMEDIATE_LOG(2),
        A_CHANGE_IN_DRIVERS_INDICATION_OF_AUTHORIZED_PERSONAL_USE_OF_CMV_OR_YARD_MOVES(3),
        A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS(4),
        A_DRIVER_LOGIN_LOGOUT_ACTIVITY(5),
        CMVS_ENGINE_POWER_UP_SHUT_DOWN_ACTIVITY(6),
        A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE(7),
        PARTIAL_A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE(71),

        IN_SERVICE_MONITOR(8),
        VIN(100),

        //      March 22, 2022  -   Define Break and Speeding custom events
        BREAK(200),
        SPEEDING(300);

        private final int value;

        EventType(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }
    }

    public enum EventCodeOrigin {
        AUTOMATICALLY_RECORDED_BY_ELD(1),
        EDITED_OR_ENTERED_BY_THE_DRIVER(2),
        EDIT_REQUESTED_BY_AN_AUTHENTICATED_USER_OTHER_THAN_THE_DRIVER(3),
        ASSUMED_FROM_UNIDENTIFIED_DRIVER_PROFILE(4);

        private final int value;

        EventCodeOrigin(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }
    }

    public enum EventRecordStatus {
        ACTIVE(1),
        INACTIVE_CHANGED(2),
        INACTIVE_CHANGE_REQUESTED(3),
        INACTIVE_CHANGE_REJECTED(4);

        private final int value;

        EventRecordStatus(final int newValue) {
            value = newValue;
        }

        public int getValue() {
            return value;
        }
    }

    public enum EventCode {
        // Tied to EventType A_CHANGE_IN_DRIVER_DUTY_STATUS

        //        March 15, 2020 -  Make a default value
        NOT_SET(0),

        OFF_DUTY(1),
        SLEEPER_BERTH(2),
        DRIVING(3),
        ON_DUTY_NOT_DRIVING(4),

        ON_BREAK(1),

        //        March 22, 2022    -   Define start and end custom events
        ON_BREAK_STARTED(201),
        ON_BREAK_ENDED(202),

        SPEEDING_STARTED(301),
        SPEEDING_ENDED(302),


        // Tied to EventType AN_INTERMEDIATE_LOG

        INTERMEDIATE_LOG_WITH_CONVENTIONAL_LOCATION_PRECISION(1),
        INTERMEDIATE_LOG_WITH_REDUCED_LOCATION_PRECISION(2),

        // Tied to EventType A_CHANGE_IN_DRIVERS_INDICATION_OF_AUTHORIZED_PERSONAL_USE_OF_CMV_OR_YARD_MOVES

        AUTHORIZED_PERSIONAL_USE_OF_CMV(1),
        YARD_MOVES(2),
        CLEARED_PC_YM_WT(0),

        // Tied to EventType A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS

        FIRST_CERTIFICATION_DAILY_RECORD(1),
        DRIVER_N_CERTIFICATION(2),

        // Tied to EventType A_DRIVER_LOGIN_LOGOUT_ACTIVITY

        LOGIN(1),
        LOGOUT(2),

        // Tied to EventType CMVS_ENGINE_POWER_UP_SHUT_DOWN_ACTIVITY

        POWERUP_CONVENTIONAL_PRECISION(1),
        POWERUP_REDUCED_PRECISION(2),
        SHUTDOWN_CONVENTIONAL_PRECISION(3),
        SHUTDOWN_REDUCED_PRECISION(4),

        // Tied to EventType A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE / PARTIAL_A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE

        ELD_MALFUNCTION_LOGGED(1),
        ELD_MALFUNCTION_CLEARED(2),
        DIAGNOSTIC_EVENT_LOGGED(3),
        DIAGNOSTIC_EVENT_CLEARED(4),

        VIN(100),
        IN_SERVICE_MONITOR_OK(81),
        IN_SERVICE_MONITOR_NOTOK(82),

        // Diagnostic Event Codes

        DIAGNOSTIC_POWER_DATA_DIAGNOSTIC(1),                    // Power data diagnostic
        DIAGNOSTIC_ENGINE_SYNCHRONIZATION(2),                   // Engine synchronization data diagnostic
        DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS(3),           // Missing required data elements data diagnostic
        DIAGNOSTIC_DATA_TRANSFER(4),                            // Data transfer data diagnostic
        DIAGNOSTIC_UNIDENTIFIED_DRIVING_RECORDS(5),             // Unidentified driving records data diagnostic
        DIAGNOSTIC_OTHER(6),                                    // Other

        // Malfunction Event Codes

        MALFUNCTION_POWER_COMPLIANCE("P"),                      // Power compliance
        MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE("E"),     // Engine synchronization compliance
        MALFUNCTION_TIMING_COMPLIANCE("T"),                     // Timing compliance
        MALFUNCTION_POSITION_COMPLIANCE("L"),                   // Positioning compliance
        MALFUNCTION_DATA_RECORDING_COMPLIANCE("R"),             // Data recording compliance
        MALFUNCTION_DATA_TANSFER_COMPLIANCE("S"),               // Data transfer compliance
        MALFUNCTION_OTHER("O");                                 // Other

        private final String value;

        EventCode(final int newValue) {
            value = Integer.toString(newValue);
        }

        EventCode(final String newValue) {
            value = newValue;
        }

        public String getValue() {
            return value;
        }

//        private int timeToDelivery;
//
//        PizzaStatus (int timeToDelivery) {
//            this.timeToDelivery = timeToDelivery;
//        }
    }

    public enum EventCodeDescription {
        DIAGNOSTIC_POWER_DATA_DIAGNOSTIC("Power data diagnostic"),
        DIAGNOSTIC_ENGINE_SYNCHRONIZATION("Engine synchronization data diagnostic"),
        DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS("Missing required data elements data diagnostic"),
        DIAGNOSTIC_DATA_TRANSFER("Data transfer data diagnostic"),
        DIAGNOSTIC_UNIDENTIFIED_DRIVING_RECORDS("Unidentified driving records data diagnostic"),
        DIAGNOSTIC_OTHER("Other"),

        ELD_MALFUNCTION_LOGGED_DESCRIPTION("Logged"),
        ELD_MALFUNCTION_CLEARED_DESCRIPTION("Cleared"),
        DIAGNOSTIC_EVENT_LOGGED_DESCRIPTION("Logged"),
        DIAGNOSTIC_EVENT_CLEARED_DESCRIPTION("Cleared"),

        MALFUNCTION_POWER_COMPLIANCE("Power compliance"),
        MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE("Engine synchronization compliance"),
        MALFUNCTION_TIMING_COMPLIANCE("Timing compliance"),
        MALFUNCTION_POSITION_COMPLIANCE("Positioning compliance"),
        MALFUNCTION_DATA_RECORDING_COMPLIANCE("Data recording compliance"),
        MALFUNCTION_DATA_TANSFER_COMPLIANCE("Data transfer compliance"),
        MALFUNCTION_OTHER("Other");

        private final String value;

        EventCodeDescription(final String newValue) {
            value = newValue;
        }

        public String getValue() {
            return value;
        }
    }

    public EventCode getLastEldEventCode() {
        return eventCode;
    }

    public ArrayList<EldEvent> getPendingEldEvents() {
        lock.lock();

        try {
            if (!db.exists("SELECT * FROM eldevents WHERE senteldevent='0' LIMIT 1"))
                return null;

            ArrayList<EldEvent> result = new ArrayList();

            Cursor c = db.getRow("SELECT id, organizationName, organizationNumber, eldUsername, eventType, " +
                    "eventCode, recordStatus, recordOrigin, truckNumber, vin, localizationDescription, latitudeString, " +
                    "longitudeString, dstSinceLastValidCoords, vehicleMiles, engineHours, orderNumbercmv, orderNumberUser, " +
                    "sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, recordOriginId, checkData, " +
                    "checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                    "editReason, eventseconds, shiftstart, creationdate, odometer, mobileRecordId FROM eldevents WHERE senteldevent='0' ORDER BY eventSeconds");
//            June 09, 2022 -   We need to send the events in the same sequence they are created
//            there is a problem with the local id, Because when we create the record the id is 0
//                    "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE senteldevent='0' ORDER BY id DESC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                EldEvent e = new EldEvent();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);
//                June 10, 2022 -   We should use the mobile record id that was sent while creating the record
                e.MobileRecordId = c.getString(col++);

                result.add(e);
                c.moveToNext();
                col = 0;
            }

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    public void setEldEventToSent(EldEvent e) {
        ArrayList<EldEvent> eldEvents = new ArrayList();
        eldEvents.add(e);

        setEldEventsToSent(eldEvents, "senteldevent");
    }

    public void setEldEventsToSent(ArrayList<EldEvent> eldEvents, String sentField) {
        if (eldEvents == null)
            return;

        lock.lock();

        try {
            for (int i = 0; i < eldEvents.size(); i++) {
                EldEvent e = eldEvents.get(i);

                ContentValues values = new ContentValues();

                values.put(sentField, "1");
                db.update("eldevents", values, "id=" + e.Id);
            }
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    public void updateEldEvent(EldEvent eldEvent, String objectId, String objectType) {
        if (eldEvent == null)
            return;

        lock.lock();

        try {

            ContentValues values = new ContentValues();

            values.put("senteldevent", "1");
            values.put("objectId", objectId);
            values.put("objectType", objectType);

            db.update("eldevents", values, "id=" + eldEvent.Id);
        } finally {
            lock.unlock();
        }
    }

    private boolean isEngineOn = false;

    public boolean existsVehiclePowerSwitchEvent() {
        Boolean isEngineOnStatus = isEngineOn();
        /* 2022.08.16
        Caused by java.lang.NullPointerException
Attempt to invoke virtual method 'boolean java.lang.Boolean.booleanValue()' on a null object reference
         */

//        if (!isEngineOnStatus)
// 2022.08.16 fix the crash with null exception        if (isEngineOnStatus == null)
        if (isEngineOn() == null)
            return false;

        return isEngineOn() != isEngineOn;
    }

    public void persistVehiclePowerSwitchEvent() {
        isEngineOn = isEngineOn();
    }

//    public void recordVehiclePowerSwitchEvent(Activity a, boolean isDriverInDutyStatus) {
//        EventCode oldEventCode = this.eventCode;
//
//        BusinessRules.EventCode powerEventCode = isEngineOn ?
//                BusinessRules.EventCode.POWERUP_CONVENTIONAL_PRECISION : EventCode.SHUTDOWN_CONVENTIONAL_PRECISION;
//
//        if (isDriverInDutyStatus)
//            powerEventCode = isEngineOn ?
//                    EventCode.POWERUP_REDUCED_PRECISION : EventCode.SHUTDOWN_REDUCED_PRECISION;
//
//        setEldEvent(a, BusinessRules.EventType.CMVS_ENGINE_POWER_UP_SHUT_DOWN_ACTIVITY, powerEventCode,
//                BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "", isEngineOn ? "Login" : "Logout", "", "");
//
//        this.eventCode = oldEventCode;
//    }

    public boolean recordVehiclePowerSwitchEvent(Activity a, boolean isDriverInDutyStatus) {
//        Log.d(TAG, "recordVehiclePowerSwitchEvent: isDriverInDutyStatus: " + isDriverInDutyStatus);
        EventCode oldEventCode = this.eventCode;

//        Log.d(TAG, "recordVehiclePowerSwitchEvent: eventCode: current: " + this.eventCode);
//        Log.d(TAG, "recordVehiclePowerSwitchEvent: oldEventCode: current: " + oldEventCode);
//        Log.d(TAG, "recordVehiclePowerSwitchEvent: isEngineOn: " + isEngineOn);

        BusinessRules.EventCode powerEventCode = isEngineOn ?
                BusinessRules.EventCode.POWERUP_CONVENTIONAL_PRECISION : EventCode.SHUTDOWN_CONVENTIONAL_PRECISION;

//        Log.d(TAG, "recordVehiclePowerSwitchEvent: powerEventCode: " + powerEventCode);

        // 15.03.2022 we should check if is not in "Duty" ( is personal coneyance or yard moves ...) then we should record reduced precision

//        Log.d(TAG, "recordVehiclePowerSwitchEvent: isDriverInDutyStatus: " + isDriverInDutyStatus);
        if (isDriverInDutyStatus)
            powerEventCode = isEngineOn ?
                    EventCode.POWERUP_REDUCED_PRECISION : EventCode.SHUTDOWN_REDUCED_PRECISION;

//        Log.d(TAG, "recordVehiclePowerSwitchEvent: powerEventCode: " + powerEventCode);
        //15.03.2022
        /* setEldEvent(a, BusinessRules.EventType.CMVS_ENGINE_POWER_UP_SHUT_DOWN_ACTIVITY, powerEventCode,
              BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "", isEngineOn ? "Login" : "Logout", "", "");     */

//                        May 05, 2022  -   We should use these annotations only for testing
        String annotation = "";
        if (isEldTesting()) {
//            annotation="DriveFragmentBase login";
            annotation = isEngineOn ? "PowerUp" : "ShutDown";
        }

        setEldEvent(a, BusinessRules.EventType.CMVS_ENGINE_POWER_UP_SHUT_DOWN_ACTIVITY, powerEventCode,
                BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD, isEngineOn ? "Power Up" : "Engine Off", annotation, "", "");
        this.eventCode = oldEventCode;

        return isEngineOn;
    }

    public Boolean isEngineOn() {
//        MArch 31, 2022    -   We should look just for the RPM
        String engineState = getBleParameter("EngineState");

        if (engineState != null && engineState.equalsIgnoreCase("ENGINE_ON"))
            return true;

        if (engineState != null && engineState.equalsIgnoreCase("ENGINE_OFF"))
            return false;

        String rpmStr = getBleParameter("RPM");

//        March 29, 2022    -   We should not return null but false
        if (rpmStr == null)
            return null;

        try {
            int rpm = Integer.parseInt(rpmStr);
            return rpm > 0;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return null;
    }

    private ArrayList<DutyEvent> dutyStatues;

    public ArrayList<DutyEvent> getDutyStatues(SessionManagement sessionManagement) {
        String drivingStatus = getDrivingStatus();
//        Log.d(TAG, "getDutyStatues: drivingStatus: " + drivingStatus);

        String currentDutyEvent = getMostRecentDutyState();

        dutyStatues = new ArrayList();
        if (!currentDutyEvent.equalsIgnoreCase("On Duty")) {

            dutyStatues.add(new DutyEvent("On Duty", drivingStatus != null && drivingStatus.equalsIgnoreCase("On Duty")));
        }

        if (!currentDutyEvent.equalsIgnoreCase("Off Duty")) {
            dutyStatues.add(new DutyEvent("Off Duty", drivingStatus != null && drivingStatus.equalsIgnoreCase("Off Duty")));
        }

        if (!currentDutyEvent.equalsIgnoreCase("Sleeper Berth") || !currentDutyEvent.equalsIgnoreCase("Sleeper")) {
            dutyStatues.add(new DutyEvent("Sleeper Berth", drivingStatus != null && drivingStatus.equalsIgnoreCase("Sleeper")));
        }

        if (!currentDutyEvent.equalsIgnoreCase("Driving")) {
            dutyStatues.add(new DutyEvent("Driving", drivingStatus != null && drivingStatus.equalsIgnoreCase("Driving")));
        }

        BusinessRules.EventCode mostRecentBreakEventCode = getMostRecentEventCodeForEventType(BusinessRules.EventType.BREAK, sessionManagement);
        if (mostRecentBreakEventCode != BusinessRules.EventCode.ON_BREAK_STARTED) {
            dutyStatues.add(new DutyEvent("Break", drivingStatus != null && drivingStatus.equalsIgnoreCase("Break")));
        }


        return dutyStatues;
    }

    private ArrayList<DutyEvent> dutySubStatues;

    public ArrayList<DutyEvent> getDriverDutySubStatuses() {
        dutySubStatues = new ArrayList();

        dutySubStatues.add(new DutySubStatus("Personal Conveyance (Off Duty)", indicationOfAuthorizedPersonalUseOfCmvOrYardMoves == EventCode.AUTHORIZED_PERSIONAL_USE_OF_CMV));
        dutySubStatues.add(new DutySubStatus("Yard Moves", indicationOfAuthorizedPersonalUseOfCmvOrYardMoves == EventCode.YARD_MOVES));
        dutySubStatues.add(new DutySubStatus("Work (On Duty)", indicationOfAuthorizedPersonalUseOfCmvOrYardMoves == null ||
                indicationOfAuthorizedPersonalUseOfCmvOrYardMoves == EventCode.CLEARED_PC_YM_WT));

        return dutySubStatues;
    }

    public boolean isDriverDutyStatusChangable() {
        if (user == null || StringUtils.isNullOrWhitespaces(user.getDrivingModeList()))
            return false;

        return user.getDrivingModeList().toLowerCase().contains("personal") ||
                user.getDrivingModeList().toLowerCase().contains("yard moves");
    }

    public void generateAndStoreNewEldAuthenticationValue(String vin) {
//        Log.d(TAG, "generateAndStoreNewEldAuthenticationValue: vin: " + vin);
        try {
            String eldAuthenticationValue = getEldAuthentcationValue(vin);
//            Log.d(TAG, "generateAndStoreNewEldAuthenticationValue: eldAuthenticationValue: " + eldAuthenticationValue);
            if (!StringUtils.isNullOrWhitespaces(eldAuthenticationValue)) {
                eldAuthenticationValue = eldAuthenticationValue.replace("\"", "");
                setSetting("ELD.Authentication.Value", eldAuthenticationValue);
            }
        } catch (Throwable throwable) {
            Log.d(TAG, "generateAndStoreNewEldAuthenticationValue: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    private String getEldAuthentcationValue(String vin) throws Exception {
        return Rms.signELDDriverLogin(user.getLogin(), vin);
    }

    public String getLatestGeneratedEldAuthenticationValue() {
        return getSetting("ELD.Authentication.Value");
    }

    //region ELD IDLE state

    private Date idleStateTiming = null;

    public boolean isVehicleSpeed0() {
        if (getDrivingSpeed() == null)
            return false;

        try {
            double drivingSpeed = Double.parseDouble(getDrivingSpeed());

            return drivingSpeed <= 0;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return false;
    }

    public boolean isVehicleSpeedGraterThan0() {
        if (getDrivingSpeed() == null)
            return false;

        try {
            double drivingSpeed = Double.parseDouble(getDrivingSpeed());

            return drivingSpeed > 0;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return false;
    }

    public boolean isVehicleStoppedForLongerThan5Mins() {
        if (idleStateTiming == null)
            return false;

        Date now = new Date();
        long diffInSecs = (now.getTime() - idleStateTiming.getTime()) / 1000;

        return diffInSecs > 300;
    }

    public void countEldIdleTime() {
        if (idleStateTiming == null)
            idleStateTiming = new Date();

        try {
            Date now = new Date();
            long diffInSecs = (now.getTime() - idleStateTiming.getTime()) / 1000;

            logDebug(">>> countEldIdleTime: " + diffInSecs);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    public void resetEldIdelTimeCounter() {
        idleStateTiming = null;
    }

    //endregion

    public void setEldEvent(Activity a, BusinessRules.EventCode eventCode, String
            annotationSubTitle, String annotationMsg) {
        if (eventCode == BusinessRules.EventCode.AUTHORIZED_PERSIONAL_USE_OF_CMV ||
                eventCode == BusinessRules.EventCode.YARD_MOVES ||
                eventCode == BusinessRules.EventCode.CLEARED_PC_YM_WT) {
            setIndicationOfAuthorizedPersonalUseOfCmvOrYardMoves(eventCode);
            setIndicationOfAuthorizedPersonalUseOfCmvOrYardMovesEldEvent(a, eventCode, annotationSubTitle, annotationMsg);
        } else {

//            Log.d(TAG, "setEldEvent: else: message: annotationSubTitle: " + annotationSubTitle);
//            Log.d(TAG, "setEldEvent: message: annotationMsg: " + annotationMsg);
//            Log.d(TAG, "setEldEvent: eventCode: else: " + eventCode);
            if (eventCode == BusinessRules.EventCode.DRIVING) {
//                setEldEventToDriving(a);
//                March 11, 2022
//                Fixed Double Driving Event
                setDrivingStatus("Driving");
            } else
                setDriverEldEvent(a, eventCode, annotationSubTitle, annotationMsg);

            if (eventCode == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
                setDrivingStatus("On Duty");
            } else if (eventCode == BusinessRules.EventCode.OFF_DUTY) {
                setDrivingStatus("Off Duty");
            } else if (eventCode == BusinessRules.EventCode.SLEEPER_BERTH) {
                setDrivingStatus("Sleeper Berth");
            } else if (eventCode == EventCode.ON_BREAK_STARTED) {
//       March 23, 2022 -    We are using ON_BREAK_STARTED, before it as ON_BREAK
                setDrivingStatus("Break");
            }

            recordTruckEldDrivingParameters(a, getDrivingStatus(), "Driver", annotationMsg);
        }
    }

    //endregion

    //region Transfer RODS

    //    public void transferEldFile(Activity a, EldTransferOption eldTransferOption, Tuple<String, String> eldTransferFile, String outputFileComment) throws Exception {
    public void transferEldFile(Activity activity, String
            eldTransferOption, Tuple<String, String> eldTransferFile, String outputFileComment) {
        String eldRegistrationId = getSetting("database.ELD Registration Id");
        String eldIdentifier = getSetting("database.ELD Registration Name");

        if (eldTransferOption.equalsIgnoreCase("Email")) {
//            April 20, 2022    -   We should use email Eld call for sending the file via email
//            transferEldFileViaEmail(a, eldTransferFile, outputFileComment, eldRegistrationId, eldIdentifier);
            transferEldFileViaEmail(user, eldTransferFile, outputFileComment, eldRegistrationId, eldIdentifier);
        } else if (eldTransferOption.equalsIgnoreCase("Web Service")) {
//            May 04, 2022    -   We should use transferEldViaWebService call for sending the file via Web Service
            transferEldViaWebService(eldTransferFile, outputFileComment);
        }
    }

    //    private void transferEldFileViaEmail(Activity a, Tuple<String, String> eldTransferFile, String outputFileComment, String eldRegistrationId, String eldIdentifier) throws Exception {
    private void transferEldFileViaEmail(Activity
                                                 a, Tuple<String, String> eldTransferFile, String outputFileComment, String
                                                 eldRegistrationId, String eldIdentifier) {
        Intent i = new Intent(Intent.ACTION_SEND);

        i.setType("message/rfc822");

//      March 21, 2022    -   We should use the testing coding field from database settings
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{isEldTesting() ? "dragos.bodnar@gmail.com" : "fmcsaeldsub@dot.gov"});
        i.putExtra(Intent.EXTRA_SUBJECT, (isEldTesting() ? "TEST: " : "") + "ELD records from " + eldRegistrationId + ":" + eldIdentifier);

//        i.putExtra(Intent.EXTRA_EMAIL, new String[]{isDebugMode() ? "fernando.ml.alves@gmail.com" : "fmcsaeldsub@dot.gov"});
//        i.putExtra(Intent.EXTRA_SUBJECT, (isDebugMode() ? "TEST: " : "") + "ELD records from " + eldRegistrationId + ":" + eldIdentifier);
        i.putExtra(Intent.EXTRA_TEXT, outputFileComment);

        File root = Environment.getExternalStorageDirectory();
        File file = new File(root, eldTransferFile.getElement0());

//        if (!file.exists() || !file.canRead())
//            throw new Exception("Cannot read or attachment not found error");

        Uri uri = Uri.parse("file://" + file);
        i.putExtra(Intent.EXTRA_STREAM, uri);

//        try {
        a.startActivity(Intent.createChooser(i, "Send ELD via email to FMCSA..."));
//        } catch (android.content.ActivityNotFoundException ex) {
//            Log.d(TAG, "transferEldFileViaEmail: ");
//            throw new Exception("There are no email clients installed. Please configure your email and try again");
//        }
    }

    private void transferEldFileViaEmail(User
                                                 u, Tuple<String, String> eldTransferFile, String outputFileComment, String
                                                 eldRegistrationId, String eldIdentifier) {
//        Log.d(TAG, "transferEldFileViaEmail: ");
        // TODO: eldTransferFile not in use?

        sendEmailUsingThread(user, eldTransferFile, outputFileComment, eldRegistrationId, eldIdentifier);
    }

    String toEmail = "";

    void sendEmailUsingThread(User u, Tuple<String, String> eldTransferFile, String
            outputFileComment, String eldRegistrationId, String eldIdentifier) {

//        return;
        String fromEmail = u.getEmail();

//      March 21, 2022    -   We should use the testing coding field from database settings
        toEmail = isEldTesting() ? "dragos.bodnar@gmail.com" : "fmcsaeldsub@dot.gov";
        toEmail = "fmcsaeldsub@dot.gov";
        String subject = (isEldTesting() ? "TEST: " : "") + "ELD records from " + eldRegistrationId + ":" + eldIdentifier;

//        String toEmail = isDebugMode() ? "fernando.ml.alves@gmail.com" : "fmcsaeldsub@dot.gov";
//        String subject = (isDebugMode() ? "TEST: " : "") + "ELD records from " + eldRegistrationId + ":" + eldIdentifier;

//        Log.d(TAG, "transferEldFileViaEmail: fromEmail: " + fromEmail + " toEmail: " + toEmail + " subject: " + subject
//                + " outputFileComment: " + outputFileComment + " getFirstLastName: " + u.getFirstLastName() + " getRecordId: " + u.getRecordId());
//        Log.d(TAG, "transferEldFileViaEmail: id: " + eldTransferFile.getElement0());
//        Log.d(TAG, "transferEldFileViaEmail: name: " + eldTransferFile.getElement1());

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    String responseString = Rms.emailEld(fromEmail, toEmail, subject, outputFileComment, u.getFirstLastName(), u.getRecordId(),
                            eldTransferFile.getElement0(), eldTransferFile.getElement1());
//                    Log.d(TAG, "transferEldFileViaEmail: So responseString: " + responseString);
                } catch (Exception exception) {
                    Log.d(TAG, "transferEldFileViaEmail: exception: " + exception.getMessage());
                    exception.printStackTrace();
                }
            }
        });

        thread.start();
    }


    void transferEldViaWebService(Tuple<String, String> eldTransferFile, String
            outputFileComment) {

//        return;
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {

//                    Log.d(TAG, "transferEldViaWebService: run: isEldTesting: " + isEldTesting());
                    String responseString = Rms.transferEldViaWebService(outputFileComment, isEldTesting(), eldTransferFile.getElement0(), eldTransferFile.getElement1());
//                    Log.d(TAG, "transferEldViaWebService: So responseString: " + responseString);
                } catch (Exception exception) {
                    Log.d(TAG, "transferEldViaWebService: exception: " + exception.getMessage());
                    exception.printStackTrace();
                }
            }
        });
        thread.start();

    }


    public Tuple<String, String> generateEldTransferFile(String outputFileComment, Activity
            activity) {
        Driver d = getDriver(user.getRmsUserId());

        String filename = generateEldTransferFileName(user.getLastName(), d.DriverLicenseNumber, username);
        String content = generateEldTransferFileContent(username, user, d, outputFileComment, activity);

        return new Tuple<>(filename, content);
    }

    private String generateEldTransferFileName(String driverLastName, String
            driverLicenseNumber, String username) {
//        Log.d(TAG, "generateEldTransferFileName: driverLastName: " + driverLastName + " driverLicenseNumber: " + driverLicenseNumber + " username: " + username);
        Integer digitsSum = MathUtils.sumDigits(driverLicenseNumber);

        return StringUtils.substrWithPadding(driverLastName, 5, '_') +
                StringUtils.substrLastDigits(driverLicenseNumber, 2) + // Last two digits
                (digitsSum > 99 ? StringUtils.substrLastDigits(Integer.toString(digitsSum), 2) : (digitsSum < 10 ? "0" + digitsSum : digitsSum + "")) +
                DateUtils.getNowMmDdYyStr("") +
                "-" +
                "RCOELD." + DateUtils.getNowMmDdYyStr("") + "." + username +
                ".csv";
    }

    //     5. Time Zone Offset from UTC
//    Offset in time between UTC time and the time standard in effect at the driver’s home terminal excluding the leading “-“ symbol. All time values submitted in the ELD output file should be converted to use this same time zone offset value.
//            Required: Yes
//     2-digit number between 4 and 11 with single digit number containing a leading 0.
//            */
    private String getUTCTimeZoneOffSet() {

//        TODO get UTC time offset like in pakistan it will be like UTC+5
//        return TimeZone.getTimeZone("UTC");

        TimeZone tz = TimeZone.getDefault();
        Date now = new Date();
//        Log.d(TAG, "getUTCTimeZoneOffSet: getOffSet: " + tz.getOffset(now.getTime()));

        int offsetFromUtcInSeconds = (Math.abs(tz.getOffset(now.getTime()) / 1000));
        int offsetFromUtcInMinutes = offsetFromUtcInSeconds / 60;
        int offsetFromUtcInHours = offsetFromUtcInMinutes / 60;
//        Log.d(TAG, "getUTCTimeZoneOffSet: getOffSet: hours: " + offsetFromUtcInHours);

        return "" + offsetFromUtcInHours;
    }

    //     /*
//     7.36 MultidayBasisUsed
//
//     Description : This data element refers to the multiday basis (7 or 8 days) used by the motor carrier to compute cumulative duty hours.
//     Purpose : Provides ability to apply the HOS rules accordingly. Source : Motor carrier.
//     Used in : ELD account profile; ELD outputs.
//     Data Type : Entered by the motor carrier during account creation process.
//     DataRange :7or8.
//     Data Length : 1 character.
//     Data Format : <Multiday basis used> as in <C>.
//     Disposition : Mandatory.
//     Examples : [7], [8].
//
//     */
    private String getMultiDayBasisUsed() {
        TruckLogHeader truckLogHeader = getOpenTruckLogHeader();
        if (truckLogHeader != null && truckLogHeader.Rule != null && truckLogHeader.Rule.contains("8 days")) {
            return "8";
        }

        return "7";
    }

    private String getShippingDocumentId() {
        TruckLogHeader truckLogHeader = getOpenTruckLogHeader();
        String mobileRecordId = "";

        if (truckLogHeader != null) {
//            Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: generateEldTransferFileContent: truckLogHeader: " + truckLogHeader.getTruckLogDetails().size());
            int lastIndex = (truckLogHeader.getTruckLogDetails().size() - 1);
            if (truckLogHeader.getTruckLogDetails().size() > 0 && truckLogHeader.getTruckLogDetails().get(lastIndex).MobileRecordId != null) {
                mobileRecordId = truckLogHeader.getTruckLogDetails().get(lastIndex).MobileRecordId;
            }
        }
//        Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: generateEldTransferFileContent: mobileRecordId: " + mobileRecordId);

        TruckEldDetail truckEldDetail = getEldDetailForTruckLogDetailMobileRecordId(mobileRecordId);
        if (truckEldDetail == null || truckEldDetail.ShippingId == null) {
            return "";
        }

        return truckEldDetail.ShippingId;
    }


    public TruckEldDetail getMostRecentEldDetail() {
        TruckLogHeader truckLogHeader = getOpenTruckLogHeader();
        String mobileRecordId = "";

        if (truckLogHeader != null) {
//            Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: generateEldTransferFileContent: truckLogHeader: " + truckLogHeader.getTruckLogDetails().size());
            int lastIndex = (truckLogHeader.getTruckLogDetails().size() - 1);
            if (truckLogHeader.getTruckLogDetails().size() > 0 && truckLogHeader.getTruckLogDetails().get(lastIndex).MobileRecordId != null) {
                mobileRecordId = truckLogHeader.getTruckLogDetails().get(lastIndex).MobileRecordId;
            }
        }
//        Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: generateEldTransferFileContent: mobileRecordId: " + mobileRecordId);

        TruckEldDetail truckEldDetail = getEldDetailForTruckLogDetailMobileRecordId(mobileRecordId);

        return truckEldDetail;
    }


    private String generateEldTransferFileContent(String username, User u, Driver d, String
            outputFileComment, Activity activity) {
        int LOGDAYS = 7;

        String eldRegistrationId = getSetting("database.ELD Registration Id");
        String eldIdentifier = getSetting("database.ELD Registration Name");

        Truck t = getTruck(u.getTruckNumber());

        String lastActiveRuleForDriver = getLatestRuleUsedByDriver(u.getRecordId());
        String multidayBasisUsed = getDriverMultidayBasisUsed(lastActiveRuleForDriver);
//        April 28, 2022    -
        multidayBasisUsed = getMultiDayBasisUsed();

//        April 28, 2022    -
        String timeZoneOffsetFromUtc = new SimpleDateFormat("HH").format(new Date());
        timeZoneOffsetFromUtc = getUTCTimeZoneOffSet();

//        April 28, 2022    -   TO DO
//        May 02, 2022      -   IMPLEMENTED
        String shippingDocumentNumber = getShippingDocumentId();


//        May 02, 2022  -   We will use 24 hours starting period "000000"
        String twentyFourHourPeriodStartingTime = "000000"; // TODO

        String authenticationValue = getLatestGeneratedEldAuthenticationValue();
//        Log.d(TAG, "generateEldTransferFileContent: authenticationValue: " + authenticationValue);
        if (authenticationValue == null) {
            authenticationValue = "";
        }

        ArrayList<EldUserItem> eldUserItems = getEldUserItems(username, LOGDAYS);
        ArrayList<EldCmvItem> cmvItems = getEldCmvItems(username, LOGDAYS);
        ArrayList<EldEventItem> eldEventItems = getEldEventItems(username, LOGDAYS);
        ArrayList<EldEventAnnotationCommentItem> eldEventAnnotationCommentItems = getEldEventAnnotationCommentItems(username, LOGDAYS);
        ArrayList<DriversCertificationRecertificationActionItem> driversCertificationRecertificationActionItems = getDriversCertificationRecertificationActionItems(username, LOGDAYS);
        ArrayList<MalfunctionsAndDataDiagnosticEventItem> malfunctionsAndDataDiagnosticEventItems = getMalfunctionsAndDataDiagnosticEventItems(username, LOGDAYS);
        ArrayList<EldLoginLogoutReportItem> eldLoginLogoutReportItems = getEldLoginLogoutReportItems(username, LOGDAYS);
        ArrayList<CmvEnginePowerUpAndShutDownActivityItem> cmvEnginePowerUpAndShutDownActivityItems = getCmvEnginePowerUpAndShutDownActivityItems(username, LOGDAYS);
        ArrayList<UnidentifiedDriverProfileRecordsItem> unidentifiedDriverProfileRecordsItems = getUnidentifiedDriverProfileRecordsItems(username, LOGDAYS);

//        April 28, 2022    -   We should use carrier name and dot number from database settings
        String carrierName = d.Carrier;
        carrierName = getSetting("database.ELD Carrier Name");

        String USDotNumber = t.DOTNumber;
        USDotNumber = getSetting("database.ELD USDotNumber");

        String exemptDriverStatus = d.ExemptDriverStatus;
        if (exemptDriverStatus == null || exemptDriverStatus.isEmpty()) {
            exemptDriverStatus = "0";
        } else {
            if (exemptDriverStatus.equalsIgnoreCase("Yes") || exemptDriverStatus.equalsIgnoreCase("true")) {
                exemptDriverStatus = "E";
            } else {
                exemptDriverStatus = "0";
            }
        }

//        April 28, 2022`-  from the current log header, we should get the truck log detail for today and from there we can get the truck number
//        which is the power unit number
        String truckNumber = u.getTruckNumber();
//        Log.d(TAG, "generateEldTransferFileContent: truckNumber: " + truckNumber);
        TruckLogHeader truckLogHeader = getOpenTruckLogHeader();

        if (truckLogHeader != null) {
//            Log.d(TAG, "generateEldTransferFileContent: truckLogHeader: " + truckLogHeader.getTruckLogDetails().size());
//            for (int i = 0; i < truckLogHeader.getTruckLogDetails().size(); i++) {
//                Log.d(TAG, "generateEldTransferFileContent: index: " + i + " Creation: " + truckLogHeader.getTruckLogDetails().get(i).CreationDate);
//            }

            if (truckLogHeader.getTruckLogDetails().size() > 0 && truckLogHeader.getTruckLogDetails().get(0).TruckNumber != null) {
                truckNumber = truckLogHeader.getTruckLogDetails().get(0).TruckNumber;
            }
        }
//        Log.d(TAG, "generateEldTransferFileContent: truckNumber: " + truckNumber);
//        TODO We should get the eld detail based on the mobile record id of the most recent truck log detail

//        April 29, 2022    -   We should use the latitude and longitude from device if the eld is not connected
        String latitude = d.Latitude;
        String longitude = d.Longitude;
        String odometer = t.Odometer;
        String engineHours = t.EngineHours;

//        May 02, 2022  -   When we send email we need to check if we are connected to eld if not then we need to send Character 'X' in latitude and longitude
//        And if we are connected and we also have a position malfunction then we should pass character 'E' in latitude and longitude
        if (isBleConnected()) {
            if (isPositionMalfunctionEvent()) {
                latitude = "E";
                longitude = "E";
            } else {
                latitude = getLastLatitude(activity);
                longitude = getLastLongitude(activity);
            }
        } else {
            latitude = "X";
            longitude = "X";
        }


//        April 29, 2022    -   We should use the last value from Odometer
        odometer = getBleParameter("Odometer");
        engineHours = getBleParameter("EngineHours");


        if (odometer == null || odometer.isEmpty()) {
            odometer = "0";
        }
        if (engineHours == null || engineHours.isEmpty()) {
            engineHours = "0";
        }

        String vin = t.VIN;
        if (vin == null || vin.isEmpty()) {
            vin = getBleParameter("VIN");
        }


        return generateEldTransferFileContent(u.getLastName(), u.getFirstName(), username, d.DriverLicenseState, d.DriverLicenseNumber,
                truckNumber, vin, u.getTrailerNumber(), USDotNumber, carrierName, multidayBasisUsed,
                twentyFourHourPeriodStartingTime, timeZoneOffsetFromUtc,
                shippingDocumentNumber, exemptDriverStatus, latitude, longitude, odometer, engineHours,
                eldRegistrationId, eldIdentifier, authenticationValue, outputFileComment,
                eldUserItems, cmvItems, eldEventItems, eldEventAnnotationCommentItems, driversCertificationRecertificationActionItems,
                malfunctionsAndDataDiagnosticEventItems, eldLoginLogoutReportItems, cmvEnginePowerUpAndShutDownActivityItems,
                unidentifiedDriverProfileRecordsItems);
    }

    private String generateEldTransferFileContent(String driverLastName, String
            driverFirstName, String username, String driverLicenseState, String driverLicenseNumber,
                                                  String cmvPowerUnitNumber, String cmvVin, String trailerNumber,
                                                  String carrierUsDotNumber, String carrierName, String multidayBasisUsed, String
                                                          twentyFourHourPeriodStartingTime,
                                                  String timeZoneOffsetFromUtc, String shippingDocumentNumber, String
                                                          exemptDriverConfiguration,
                                                  String currentLat, String currentLon, String currentTotalVehicleMiles, String
                                                          currentTotalEngineHours,
                                                  String eldRegistrationId, String eldIdentifier, String
                                                          eldAuthenticationValue, String outputFileComment,
                                                  ArrayList<EldUserItem> userItems,
                                                  ArrayList<EldCmvItem> cmvItems,
                                                  ArrayList<EldEventItem> eldEventItems,
                                                  ArrayList<EldEventAnnotationCommentItem> eldEventAnnotationCommentItems,
                                                  ArrayList<DriversCertificationRecertificationActionItem> driversCertificationRecertificationActionItems,
                                                  ArrayList<MalfunctionsAndDataDiagnosticEventItem> malfunctionsAndDataDiagnosticEventItems,
                                                  ArrayList<EldLoginLogoutReportItem> eldLoginLogoutReportItems,
                                                  ArrayList<CmvEnginePowerUpAndShutDownActivityItem> cmvEnginePowerUpAndShutDownActivityItems,
                                                  ArrayList<UnidentifiedDriverProfileRecordsItem> unidentifiedDriverProfileRecordsItems) {

//        April 26, 2022    -   We should makes sure that the mFileDataSum should start from 0
        mFileDataSum = 0;
        StringBuilder b = new StringBuilder("");
        /*
 4.8.2.1.1 Header Segment
 This segment must include the following data elements and format:
 ELD File Header Segment: <CR>
 */
        b.append("ELD File Header Segment:" + mLineDelimitator);

// <{Driver’s} Last Name>,<{Driver’s} First Name>,< ELD username{for the driver} >,< {Driver’s} Driver's License Issuing State>,<{Driver’s} Driver's License Number>,<Line Data Check Value> <CR>
        b = appendContent(b, driverLastName + "," + driverFirstName + "," + username + "," + driverLicenseState + "," + driverLicenseNumber);
//          April 27, 2022  -   Its printing just too many commas, and its not identical to data we are getting on ios so commented it

//         <{Co-Driver’s} Last Name>,<{Co-Driver’s} First Name>,<ELD username {for the co- driver} >,<Line Data Check Value> <CR>
        b = appendContent(b, "" + "," + "" + "," + "");

// <CMV Power Unit Number>,<CMV VIN>,<Trailer Number(s)>,<Line Data Check Value> <CR>
        b = appendContent(b, cmvPowerUnitNumber + "," + cmvVin + "," + trailerNumber);

// <Carrier's USDOT Number>,<Carrier Name>,<Multiday-basis Used>,<24-Hour Period Starting Time>,<Time Zone Offset from UTC>,<Line Data Check Value> <CR>
        b = appendContent(b, carrierUsDotNumber + "," + carrierName + "," + multidayBasisUsed + "," + twentyFourHourPeriodStartingTime + "," + timeZoneOffsetFromUtc);

//         <Shipping Document Number>,<Exempt Driver Configuration>,<Line Data Check Value> <CR>
        b = appendContent(b, shippingDocumentNumber + "," + exemptDriverConfiguration);


//<{Current} Date>,< {Current} Time>,< {Current} Latitude>,<{Current} Longitude>,< {Current} {Total} Vehicle Miles>,< {Current} {Total} Engine Hours>,<Line Data Check Value> <CR>
        b = appendContent(b, DateUtils.getDateFromDateAndTime(DateUtils.getNowYyyyMmDdHhmmss()) + "," + DateUtils.getTimeFromDateAndTime(DateUtils.getNowYyyyMmDdHhmmss())
                + "," + currentLat + "," + currentLon + "," + currentTotalVehicleMiles + "," + currentTotalEngineHours);

// <ELD Registration ID>,<ELD Identifier>,<ELD Authentication Value>,<Output File Comment>,<Line Data Check Value> <CR>
        b = appendContent(b, eldRegistrationId + "," + eldIdentifier + "," + eldAuthenticationValue + "," + outputFileComment);


        b.append("User List:" + mLineDelimitator);
//        4.8.2.1.2 User List
//        This segment must list all drivers and co-drivers with driving time records on the most recent CMV operated by the inspected driver and motor carrier’s support personnel who requested edits within the time period for which this file is generated. The list must be in chronological order with most recent user of the ELD on top, and include the driver being inspected, the co-driver, and the unidentified driver profile. This segment has a variable number of rows depending on the number of profiles with activity over the time period for which this file is generated. This section must start with the following title:
//        User List: <CR>
//       Each subsequent row must have the following data elements:
//     <{Assigned User} Order Number>,<{User’s} ELD Account Type>,<{User’s} Last Name>,<{User’s} First Name>,<Line Data Check Value> <CR>
        if (userItems != null)
            for (EldUserItem e : userItems)
                b = appendContent(b, e.AssignedUserOrderNumber + "," + e.UsersEldAccountType + "," + e.UsersLastName + "," + e.UsersFirstName);

        b.append("CMV List:" + mLineDelimitator);
//        4.8.2.1.3 CMV List
//        This segment must list each CMV that the current driver operated and that has been recorded on the driver’s ELD records within the time period for which this file is generated. The list must be rank ordered in accordance with the time of CMV operation with the most recent CMV being on top. This segment has a variable number of rows depending on the number of CMVs operated by the driver over the time period for which this file is generated. This section must start with the following title:
//        CMV List: <CR>
//        Each subsequent row must have the following data elements:
// <{Assigned CMV} Order Number>,<CMV Power Unit Number>,<CMV VIN>,<Line Data Check Value> <CR>
        if (cmvItems != null)
            for (EldCmvItem e : cmvItems)
                b = appendContent(b, e.AssignedCmvOrderNumber + "," + e.CmvPowerUnitNumber + "," + e.CmvVin);

        b.append("ELD Event List:" + mLineDelimitator);
//        4.8.2.1.4 ELD Event List for Driver’s Record of Duty Status
//        This segment must list ELD event records tagged with event types 1 (a change in duty status as described in section 4.5.1.1 of this appendix), 2 (an intermediate log as described in section 4.5.1.2), and 3 (a change in driver’s indication of conditions impacting driving time recording as described in section 4.5.1.3). The segment must list all event record status types and all event record origins for the driver, rank ordered with the most current log on top in accordance with the date and time fields of the record. This segment has a variable number of rows depending on the number of ELD events recorded for the driver over the time period for which this file is generated. This section must start with the following title:
//        ELD Event List: <CR>
//        Each subsequent row must have the following data elements:
// <Event Sequence ID Number>,<Event Record Status>,<Event Record Origin>,<Event Type>,
// <Event Code>,<{Event} Date>,<{Event} Time>,< {Accumulated} Vehicle Miles>,< {Elapsed} Engine Hours>,
// <{Event} Latitude>,<{Event} Longitude>,<Distance Since Last Valid Coordinates>, <{Corresponding CMV} Order Number>,
// <{ User} Order Number {for Record Originator}>,<Malfunction Indicator Status {for ELD}>,<Data Diagnostic Event Indicator Status {for Driver}>,
// <Event Data Check Value>,<Line Data Check Value> <CR>

//        TODO list -   Here in event list we should also have at around 10th parameter the "Personal" and "YM"

        if (eldEventItems != null)
            for (EldEventItem e : eldEventItems) {

                String accumulatedVehicleMiles = e.AccumulatedVehicleMiles;
                if (accumulatedVehicleMiles == null || accumulatedVehicleMiles.isEmpty()) {
                    accumulatedVehicleMiles = "0";
                }

                String distanceSinceLastValidCoordinates = e.DistanceSinceLastValidCoordinates;
                if (distanceSinceLastValidCoordinates != null && !distanceSinceLastValidCoordinates.isEmpty()) {
                    if (Integer.parseInt(distanceSinceLastValidCoordinates) > 6) {
                        distanceSinceLastValidCoordinates = "6";
                    }
                }

//                Log.d(TAG, "generateEldTransferFileContent: int parse: "+(Integer.parseInt(e.AccumulatedVehicleMiles)));
//                Log.d(TAG, "generateEldTransferFileContent: int and double parse: " + ((int) Double.parseDouble(accumulatedVehicleMiles)));
                b = appendContent(b, getHexaDecimalValue(Integer.parseInt(e.EventSequenceIdNumber)) + "," + e.EventRecordStatus + "," + e.EventRecordOrigin + "," + e.EventType + "," +
                        e.EventCode + "," + e.EventDate + "," + e.EventTime + "," + ((int) Double.parseDouble(accumulatedVehicleMiles)) + "," + e.ElapsedEngineHours + "," +
                        e.EventLatitude + "," + e.EventLongitude + "," + distanceSinceLastValidCoordinates + "," + e.CorrespondingCmvOrderNumber + "," +
                        e.UserOrderNumberForRecordOriginator + "," + e.MalfunctionIndicatorStatusForEld + "," + e.DataDiagnosticEventIndicatorStatusForDriver
                        + "," + getHexaDecimalValueUpTo2Digits(Integer.parseInt(e.EventCheckData)));
            }


        b.append("ELD Event Annotations or Comments:" + mLineDelimitator);
//        4.8.2.1.5 Event Annotations, Comments, and Driver’s Location Description
//        This segment must list only the elements of the ELD event list created in section 4.8.2.1.4 of this appendix that have an annotation, comment, or a manual entry of location description by the driver. This segment has a variable number of rows depending on the number of ELD events under section 4.8.2.1.4 that feature a comment, annotation, or manual location entry by the driver. This section must start with the following title:
//        ELD Event Annotations or Comments: <CR>
//        Each subsequent row must have the following data elements:
// <Event Sequence ID Number>,< ELD username {of the Record Originator} >,<{Event} Comment Text or Annotation>,<{Event} Date>,<{Event} Time>, <Driver’s Location Description>,<Line Data Check Value> <CR>
        if (eldEventAnnotationCommentItems != null)
            for (EldEventAnnotationCommentItem e : eldEventAnnotationCommentItems)
                b = appendContent(b, getHexaDecimalValue(Integer.parseInt(e.EventSequenceIDNumber)) + "," + e.EldUsernameOfTheRecordOriginator + "," + e.EventCommentTextOrAnnotation + "," +
                        e.EventDate + "," + e.EventTime + "," + e.DriversLocationDescription);

        b.append("Driver's Certification/Recertification Actions:" + mLineDelimitator);
//        4.8.2.1.6 ELD Event List for Driver’s Certification of Own Records
//        This segment must list ELD event records with event type 4 (driver’s certification of own records as described in section 4.5.1.4 of this appendix) for the inspected driver for the time period for which this file is generated. It must be rank ordered with the most current record on top. This segment has a variable number of rows depending on the number of certification and re-certification actions the authenticated driver may have executed on the ELD over the time period for which this file is generated. This section must start with the following title:
//        Driver’s Certification/Recertification Actions: <CR>
//        Each subsequent row must have the following data elements:
// <Event Sequence ID Number>,<Event Code>,<{Event} Date>,<{Event} Time>,<Date {of the certified record}>,<{Corresponding CMV} Order Number>,<Line Data Check Value> <CR>
        if (driversCertificationRecertificationActionItems != null)
            for (DriversCertificationRecertificationActionItem e : driversCertificationRecertificationActionItems)
                b = appendContent(b, e.EventSequenceIdNumber + "," + e.EventCode + "," + e.EventDate + "," + e.EventTime + "," +
                        e.DateOfTheCertifiedRecord + "," + e.CorrespondingCmvOrderNumber);

        String totalVehicleMiles = "0";
        String totalEngineHours = "0";

        b.append("Malfunctions and Data Diagnostic Events:" + mLineDelimitator);
//        4.8.2.1.7 Malfunction and Diagnostic Event Records
//        This segment must list all malfunctions that have occurred on this ELD during the time period for which this file is generated. It must list diagnostic event records related to the driver being inspected, rank ordered with the most current record on top. This segment has a variable number of rows depending on the number of ELD malfunctions and ELD diagnostic event records recorded and relevant to the inspected driver over the time period for which this file is generated. This section must start with the following title:
//        Malfunctions and Data Diagnostic Events: <CR>
//        Each subsequent row must have the following data elements:
// <Event Sequence ID Number>,<Event Code>,<Malfunction/Diagnostic Code>,<{Event} Date>,<{Event} Time>,<{Total} Vehicle Miles>,<{Total} Engine Hours> ,<{Corresponding CMV} Order Number>,<Line Data Check Value> <CR>
        if (malfunctionsAndDataDiagnosticEventItems != null)
            for (MalfunctionsAndDataDiagnosticEventItem e : malfunctionsAndDataDiagnosticEventItems) {

                totalVehicleMiles = e.TotalVehicleMiles;
                if (totalVehicleMiles == null || totalVehicleMiles.isEmpty()) {
                    totalVehicleMiles = "0";
                }
                totalEngineHours = e.TotalEngineHours;
                if (totalEngineHours == null || totalEngineHours.isEmpty()) {
                    totalEngineHours = "0";
                }

                b = appendContent(b, getHexaDecimalValue(Integer.parseInt(e.EventSequenceIdNumber)) + "," + e.EventCode + "," + e.MalfunctionDiagnosticCode + "," + e.EventDate + "," +
                        e.EventTime + "," + ((int) Double.parseDouble(totalVehicleMiles)) + "," + totalEngineHours + "," + e.CorrespondingCmvOrderNumber);
            }


        b.append("ELD Login/Logout Report:" + mLineDelimitator);
//        4.8.2.1.8 ELD Login/Logout Report
//        This segment must list the login and logout activity on the ELD (ELD events with event type 5 (A driver’s login/logout activity)) for the inspected driver for the time period for which this file is generated. It must be rank ordered with the most recent activity on top. This section must start with the following title:
//        ELD Login/Logout Report: <CR>
//        Each subsequent row must have the following data elements:
// <Event Sequence ID Number>,<Event Code>,<ELD username>,<{Event} Date>,<{Event} Time>,<{Total} Vehicle Miles>,<{Total} Engine Hours>,<Line Data Check Value> <CR>
        if (eldLoginLogoutReportItems != null)
            for (EldLoginLogoutReportItem e : eldLoginLogoutReportItems) {
                totalVehicleMiles = e.TotalVehicleMiles;
                if (totalVehicleMiles == null || totalVehicleMiles.isEmpty()) {
                    totalVehicleMiles = "0";
                }


                totalEngineHours = e.TotalEngineHours;
                if (totalEngineHours == null || totalEngineHours.isEmpty()) {
                    totalEngineHours = "0.0";
                } else {
                    totalEngineHours = "" + round(Double.parseDouble(totalEngineHours), 1);
                }
                b = appendContent(b, getHexaDecimalValue(Integer.parseInt(e.EventSequenceIdNumber)) + "," + e.EventCode + "," + e.Eldusername + "," + e.EventDate + "," +
                        e.EventTime + "," + ((int) Double.parseDouble(totalVehicleMiles)) + "," + totalEngineHours);
            }


        b.append("CMV Engine Power-Up and Shut Down Activity:" + mLineDelimitator);
//        4.8.2.1.9 CMV’s Engine Power-Up and Shut Down Activity
//        This segment must list the logs created when a CMV’s engine is powered up and shut down (ELD events with event type 6 (CMV’s engine power up/shut down)) for the time period for which this file is generated. It must be rank ordered with the latest activity on top. This section must start with the following title:
//        CMV Engine Power-Up and Shut Down Activity: <CR>
//        Each subsequent row must have the following data elements:
// <Event Sequence ID Number>,<Event Code>,<{Event} Date>,<{Event} Time>,<{Total} Vehicle Miles>,<{Total} Engine Hours>,<{Event} Latitude>,<{Event} Longitude>,<CMV Power Unit Number>,<CMV VIN>,<Trailer Number(s)>,<Shipping Document Number>,<Line Data Check Value> <CR>
        if (cmvEnginePowerUpAndShutDownActivityItems != null)
            for (CmvEnginePowerUpAndShutDownActivityItem e : cmvEnginePowerUpAndShutDownActivityItems) {
                totalVehicleMiles = e.TotalVehicleMiles;
                if (totalVehicleMiles == null || totalVehicleMiles.isEmpty()) {
                    totalVehicleMiles = "0";
                }
                totalEngineHours = e.TotalEngineHours;
                if (totalEngineHours == null || totalEngineHours.isEmpty()) {
                    totalEngineHours = "0";
                }
                b = appendContent(b, getHexaDecimalValue(Integer.parseInt(e.EventSequenceIdNumber)) + "," + e.EventCode + "," + e.EventDate + "," +
                        e.EventTime + "," + ((int) Double.parseDouble(totalVehicleMiles)) + "," + e.TotalEngineHours + "," + e.EventLatitude + "," +
                        e.EventLongitude + "," + e.CmvPowerUnitNumber + "," + e.CmvVin + "," + e.TrailerNumbers + "," + e.ShippingDocumentNumber);
            }


        b.append("Unidentified Driver Profile Records:" + mLineDelimitator);
//        4.8.2.1.10 ELD Event Log List for the Unidentified Driver Profile
//        This segment must list the ELD event records for the Unidentified Driver profile, rank ordered with most current log on top in accordance with the date and time fields of the logs. This segment has a variable number of rows depending on the number of Unidentified Driver ELD records recorded over the time period for which this file is generated. This section must start with the following title:
//        Unidentified Driver Profile Records: <CR>
//        Each subsequent row must have the following data elements:
// <Event Sequence ID Number>,<Event Record Status>,<Event Record Origin>,<Event Type>,<Event Code>,<{Event} Date>,<{Event} Time>,< {Accumulated} Vehicle Miles>,< {Elapsed} Engine Hours>,<{Event} Latitude>,<{Event} Longitude>,
// <Distance Since Last Valid Coordinates>, <{Corresponding CMV} Order Number>,<Malfunction Indicator Status {for ELD}>,<Event Data Check Value>,<Line Data Check Value> <CR>

        if (unidentifiedDriverProfileRecordsItems != null)
            for (UnidentifiedDriverProfileRecordsItem e : unidentifiedDriverProfileRecordsItems) {
                String accumulatedVehicleMiles = e.AccumulatedVehicleMiles;
                if (accumulatedVehicleMiles == null) {
                    accumulatedVehicleMiles = "0";
                }
                String distanceSinceLastValidCoordinates = e.DistanceSinceLastValidCoordinates;
                if (distanceSinceLastValidCoordinates != null && !distanceSinceLastValidCoordinates.isEmpty()) {
                    if (Integer.parseInt(distanceSinceLastValidCoordinates) > 6) {
                        distanceSinceLastValidCoordinates = "6";
                    }
                }
                b = appendContent(b, getHexaDecimalValue(Integer.parseInt(e.EventSequenceIdNumber)) + "," + e.EventRecordStatus + "," + e.EventRecordOrigin + "," + e.EventType + "," +
                        e.EventCode + "," + e.EventDate + "," + e.EventTime + "," + ((int) Double.parseDouble(accumulatedVehicleMiles)) + "," + e.ElapsedEngineHours + "," +
                        e.EventLatitude + "," + e.EventLongitude + "," + distanceSinceLastValidCoordinates + "," + e.CorrespondingCmvOrderNumber + "," +
                        e.MalfunctionIndicatorStatusForEld + "," + getHexaDecimalValueUpTo2Digits(Integer.parseInt(e.EventCheckData)));
            }


        b.append("End of File:" + mLineDelimitator);
//        4.8.2.1.11 File Data Check Value
//        This segment lists the file data check value as specified in section 4.4.5.3 of this appendix. This part includes a single line as follows:
// <File Data Check Value><CR>
        b.append(getHexaDecimalValue(fileDataCheck(mFileDataSum), 4));
//        TO DO - Instead of having lineDataCheck - we need to have fileDataCheck

//        April 28, 2022    -   We need to add <CR> (carriage return) at the end
        b.append(mLineDelimitator);

//        Log.d(TAG, "generateEldTransferFileContent: mFileDataSum: " + mFileDataSum + " fileDataCheck: " + fileDataCheck(mFileDataSum));
//        Log.d(TAG, "generateEldTransferFileContent: lineDataCheck: value: " + lineDataCheck(b.toString()) + " Hexa: value: " + getHexaDecimalValue(Integer.parseInt(lineDataCheck(b.toString()))));
//        Log.d(TAG, "generateEldTransferFileContent: filtered: " + b.toString().replace("null", ""));
        return b.toString();
    }

    String getHexaDecimalValue(int int_value) {
        String hex_value = int_value < 0
                ? "-" + Integer.toHexString(-int_value)
                : Integer.toHexString(int_value);
        return (hex_value.toUpperCase());
    }

    String getHexaDecimalValueUpTo2Digits(int int_value) {
        String hex_value = int_value < 0
                ? "-" + Integer.toHexString(-int_value)
                : Integer.toHexString(int_value);

        if (hex_value.length() == 1) {
            hex_value = "0" + hex_value;
        }
        return (hex_value.toUpperCase());
    }

    String getHexaDecimalValue(int int_value, int digits) {
        String hex_value = int_value < 0
                ? "-" + Integer.toHexString(-int_value)
                : Integer.toHexString(int_value);
        if (hex_value.length() == 3) {
            hex_value = "0" + hex_value;
        } else if (hex_value.length() == 2) {
            hex_value = "00" + hex_value;
        } else if (hex_value.length() == 1) {
            hex_value = "000" + hex_value;
        } else if (hex_value.length() == 0) {
            hex_value = "0000";
        }
        return (hex_value.toUpperCase());
    }


    private StringBuilder appendContent(StringBuilder b, String line) {
        line = line.replace("null", "");
        String lineDataCheckValue = lineDataCheck(line);

//        Log.d(TAG, "appendContent: line: " + line);
//        Log.d(TAG, "appendContent: lineDataCheckValue: " + lineDataCheckValue);
        if (line.equalsIgnoreCase(",,,")) {
            Log.d(TAG, "appendContent: lineDataCheckValue: " + lineDataCheckValue);
        }
        mFileDataSum = mFileDataSum + Integer.parseInt(lineDataCheckValue);
//        return b.append(line + "," + getEldEncodedContent(lineDataCheckValue) + "\r");

//        April 26, 2022    -   We need make sure that line data check, its a hexa decimal value with 2 digits
        String convertedHexaValue = getHexaDecimalValue(Integer.parseInt(getEldEncodedContent(lineDataCheckValue)));
        if (convertedHexaValue.length() < 2) {
            if (convertedHexaValue.length() == 0) {
                convertedHexaValue = "00";
            } else {
                convertedHexaValue = "0" + convertedHexaValue;
            }
        }
        return b.append(line + "," + convertedHexaValue.trim() + mLineDelimitator);
    }


    private String getEldEncodedContent(String value) {
        if (value == null)
            return value;

        return value.replace(",", ";").replace("\r", ";");
    }

    private String getDriverMultidayBasisUsed(String ruleName) {
        if (StringUtils.isNullOrWhitespaces(ruleName))
            return null;

        if (!db.exists("SELECT days FROM rules WHERE name='" + ruleName + "'"))
            return null;

        String result = null;
        Cursor c = db.getQuery("SELECT days FROM rules WHERE name='" + ruleName + "'");
        c.moveToFirst();

        if (!c.isAfterLast())
            result = c.getString(0);

        c.close();
        return result;
    }

    private String getLatestRuleUsedByDriver(String driverRecordId) {
        if (StringUtils.isNullOrWhitespaces(driverRecordId))
            return null;

        if (!db.exists("SELECT rule FROM truckeldheader WHERE driverRecordId='" + driverRecordId + "' ORDER BY id DESC LIMIT 1"))
            return null;

        String result = null;
        Cursor c = db.getQuery("SELECT rule FROM truckeldheader WHERE driverRecordId='" + driverRecordId + "' ORDER BY id DESC LIMIT 1");
        c.moveToFirst();

        if (!c.isAfterLast())
            result = c.getString(0);

        c.close();
        return result;
    }

    //endregion

    //region ELD Queries

    private ArrayList<CmvEnginePowerUpAndShutDownActivityItem> getCmvEnginePowerUpAndShutDownActivityItems
            (final String username, int days) {
        return getEldItems(
                "SELECT sequenceId, eventCode, creationdate, creationdate, vehicleMiles, engineHours, " +
                        "latitudeString, longitudeString, truckNumber, vin" +
                        " FROM eldevents WHERE eldUsername='" + username + "' AND eventType=" + EventType.CMVS_ENGINE_POWER_UP_SHUT_DOWN_ACTIVITY.getValue(),
                new ILoadEldItem<CmvEnginePowerUpAndShutDownActivityItem>() {
                    @Override
                    public CmvEnginePowerUpAndShutDownActivityItem OnEldItemCreation(Cursor c, int counter) {
                        CmvEnginePowerUpAndShutDownActivityItem item = new CmvEnginePowerUpAndShutDownActivityItem();

                        item.EventSequenceIdNumber = c.getString(counter++);
                        item.EventCode = c.getString(counter++);
//                        April 28, 2022    -   Why we need to set the username to event code
//                        item.EventCode = username;
//                        counter++;
//                        item.EventDate = c.getString(counter++);
//                        item.EventTime = c.getString(counter++);
                        item.EventDate = DateUtils.getDateFromDateAndTime(c.getString(counter++));
                        item.EventTime = DateUtils.getTimeFromDateAndTime(c.getString(counter++));
                        item.TotalVehicleMiles = c.getString(counter++);
                        item.TotalEngineHours = c.getString(counter++);
                        item.EventLatitude = c.getString(counter++);
                        item.EventLongitude = c.getString(counter++);
                        item.CmvPowerUnitNumber = c.getString(counter++);
                        item.CmvVin = c.getString(counter++);
                        item.TrailerNumbers = ""; // TODO: Query to get the trailers from the truckNumber via a LEFT JOIN
                        item.ShippingDocumentNumber = ""; // TODO

                        return item;
                    }
                }, days);
    }

    private ArrayList<UnidentifiedDriverProfileRecordsItem> getUnidentifiedDriverProfileRecordsItems
            (final String username, int days) {
        return getEldItems(
                "SELECT sequenceId, recordStatus, recordOrigin, eventType, eventCode, creationdate, creationdate, vehicleMiles, engineHours, " +
                        "latitudeString, longitudeString, dstSinceLastValidCoords, orderNumbercmv, malfunctionIndicator , checkData " +
                        " FROM eldevents WHERE (eldUsername IS NULL OR eldUsername='') AND " +
                        "(eventType=" + EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.getValue() + " OR eventType=" + EventType.AN_INTERMEDIATE_LOG.getValue() + ")",
                new ILoadEldItem<UnidentifiedDriverProfileRecordsItem>() {
                    @Override
                    public UnidentifiedDriverProfileRecordsItem OnEldItemCreation(Cursor c, int counter) {
                        UnidentifiedDriverProfileRecordsItem item = new UnidentifiedDriverProfileRecordsItem();

                        item.EventSequenceIdNumber = c.getString(counter++);
                        item.EventRecordStatus = c.getString(counter++);
                        item.EventRecordOrigin = c.getString(counter++);
                        item.EventType = c.getString(counter++);
                        item.EventCode = c.getString(counter++);
//                        item.EventDate = c.getString(counter++);
//                        item.EventTime = c.getString(counter++);
                        item.EventDate = DateUtils.getDateFromDateAndTime(c.getString(counter++));
                        item.EventTime = DateUtils.getTimeFromDateAndTime(c.getString(counter++));
                        item.AccumulatedVehicleMiles = c.getString(counter++);
                        item.ElapsedEngineHours = c.getString(counter++);
                        item.EventLatitude = c.getString(counter++);
                        item.EventLongitude = c.getString(counter++);
                        item.DistanceSinceLastValidCoordinates = c.getString(counter++);
                        item.CorrespondingCmvOrderNumber = c.getString(counter++);
                        item.MalfunctionIndicatorStatusForEld = c.getString(counter++);
//                        April 28, 2022    -   from documentation
                        item.EventCheckData = c.getString(counter++);

                        return item;
                    }
                }, days);
    }

    private ArrayList<EldLoginLogoutReportItem> getEldLoginLogoutReportItems(
            final String username, int days) {
        return getEldItems(
                "SELECT sequenceId, eventCode, eldUsername, creationdate, creationdate, vehicleMiles, engineHours FROM eldevents WHERE eldUsername='" + username + "' AND " +
                        "eventType=" + EventType.A_DRIVER_LOGIN_LOGOUT_ACTIVITY.getValue(),
                new ILoadEldItem<EldLoginLogoutReportItem>() {
                    @Override
                    public EldLoginLogoutReportItem OnEldItemCreation(Cursor c, int counter) {
                        EldLoginLogoutReportItem item = new EldLoginLogoutReportItem();

                        item.EventSequenceIdNumber = c.getString(counter++);
//                        April 21, 2022
//                        item.EventCode = username;
//                        counter++;
                        item.EventCode = c.getString(counter++);
                        item.Eldusername = c.getString(counter++);

//                        item.EventDate = c.getString(counter++);
//                        item.EventTime = c.getString(counter++);
                        item.EventDate = DateUtils.getDateFromDateAndTime(c.getString(counter++));
                        item.EventTime = DateUtils.getTimeFromDateAndTime(c.getString(counter++));
                        item.TotalVehicleMiles = c.getString(counter++);
                        item.TotalEngineHours = c.getString(counter++);

                        return item;
                    }
                }, days);
    }

    public EldEvent getMostRecentEldLoginLogout(String username) {
//        Log.d(TAG, "getMostRecentEldLoginLogout: ");

        String sqlString =
                "SELECT " +
                        "sequenceId, recordStatus, recordOrigin, eventType, eventCode, creationdate, " +
                        "vehicleMiles, engineHours, latitudeString, longitudeString, dstSinceLastValidCoords, " +
                        "orderNumbercmv, orderNumberUser, malfunctionIndicator, diagnosticIndicator, odometer " +
                        "FROM " +
                        "eldevents " +
                        "WHERE " +
                        "eldUsername='" + username + "' " +
                        "AND " +
                        "eventType=5" + " " +
                        "ORDER BY " +
                        "creationdate DESC " +
                        "LIMIT 1";


        Cursor cursor = db.getQuery(sqlString);
        if (cursor == null)
            return null;

        cursor.moveToFirst();
        EldEvent item = new EldEvent();
        if (cursor.getCount() > 0) {

            int counter = 0;
            item.SequenceId = cursor.getString(counter++);
            item.RecordStatus = cursor.getString(counter++);
            item.RecordOrigin = cursor.getString(counter++);
            item.EventType = cursor.getString(counter++);
            item.EventCode = cursor.getString(counter++);
            item.CreationDate = cursor.getString(counter++);
            item.VehicleMiles = cursor.getString(counter++);
            item.EngineHours = cursor.getString(counter++);
            item.LatitudeString = cursor.getString(counter++);
            item.LongitudeString = cursor.getString(counter++);
            item.DstSinceLastValidCoords = cursor.getString(counter++);
            item.OrderNumberCmv = cursor.getString(counter++);
            item.OrderNumberUser = cursor.getString(counter++);
            item.MalfunctionIndicator = cursor.getString(counter++);
            item.DiagnosticIndicator = cursor.getString(counter++);
            item.Odometer = cursor.getString(counter++);

            return item;
        }

        cursor.close();
        return null;
    }


    private ArrayList<MalfunctionsAndDataDiagnosticEventItem> getMalfunctionsAndDataDiagnosticEventItems
            (final String username, int days) {
        return getEldItems(
                "SELECT sequenceId, eventCode, malfunctionDiagnosticCode, creationdate, creationdate, vehicleMiles, engineHours, " +
                        "orderNumbercmv FROM eldevents WHERE eldUsername='" + username + "' AND eventType=" + EventType.A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE.getValue(),
                new ILoadEldItem<MalfunctionsAndDataDiagnosticEventItem>() {
                    @Override
                    public MalfunctionsAndDataDiagnosticEventItem OnEldItemCreation(Cursor c, int counter) {
                        MalfunctionsAndDataDiagnosticEventItem item = new MalfunctionsAndDataDiagnosticEventItem();

                        item.EventSequenceIdNumber = c.getString(counter++);
                        item.EventCode = c.getString(counter++);
//                        April 28, 2022    -   Why we need to set the username to event code
//                        item.EventCode = username;
//                        counter++;
                        item.MalfunctionDiagnosticCode = c.getString(counter++);

//                        item.EventDate = c.getString(counter++);
//                        item.EventTime = c.getString(counter++);
                        item.EventDate = DateUtils.getDateFromDateAndTime(c.getString(counter++));
                        item.EventTime = DateUtils.getTimeFromDateAndTime(c.getString(counter++));
                        item.TotalVehicleMiles = c.getString(counter++);
                        item.TotalEngineHours = c.getString(counter++);
                        item.CorrespondingCmvOrderNumber = c.getString(counter++);

                        return item;
                    }
                }, days);
    }

    private ArrayList<DriversCertificationRecertificationActionItem> getDriversCertificationRecertificationActionItems
            (final String username, int days) {

//        April 20, 2022    -   Check This "creationdate, creationdate, creationdate"
        return getEldItems(
                "SELECT sequenceId, eventCode, creationdate, creationdate, creationdate, orderNumbercmv FROM eldevents WHERE eldUsername='" + username + "' AND " +
                        "eventType=" + EventType.A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS.getValue(),
                new ILoadEldItem<DriversCertificationRecertificationActionItem>() {
                    @Override
                    public DriversCertificationRecertificationActionItem OnEldItemCreation(Cursor c, int counter) {
                        DriversCertificationRecertificationActionItem item = new DriversCertificationRecertificationActionItem();

                        item.EventSequenceIdNumber = c.getString(counter++);
                        item.EventCode = c.getString(counter++);
//                        counter++;
//                        item.EventDate = c.getString(counter++);
//                        item.EventTime = c.getString(counter++);
//                        item.DateOfTheCertifiedRecord = c.getString(counter++);
                        item.EventDate = DateUtils.getDateFromDateAndTime(c.getString(counter++));
                        item.EventTime = DateUtils.getTimeFromDateAndTime(c.getString(counter++));
                        item.DateOfTheCertifiedRecord = DateUtils.getDateFromDateAndTime(c.getString(counter++));
                        item.CorrespondingCmvOrderNumber = c.getString(counter++);

                        return item;
                    }
                }, days);
    }

    private ArrayList<EldEventAnnotationCommentItem> getEldEventAnnotationCommentItems(
            final String username, int days) {
        return getEldItems(
                "SELECT sequenceId, annotation, creationdate, creationdate, localizationDescription FROM eldevents WHERE eldUsername='" + username + "' AND " +
                        "(eventType=" + EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.getValue() + " OR eventType=" + EventType.AN_INTERMEDIATE_LOG.getValue() + " OR " +
                        "eventType=" + EventType.A_CHANGE_IN_DRIVERS_INDICATION_OF_AUTHORIZED_PERSONAL_USE_OF_CMV_OR_YARD_MOVES.getValue() + ") AND " +
                        "(annotation IS NOT NULL OR annotation<>'' OR localizationDescription IS NOT NULL OR localizationDescription<>'')",
                new ILoadEldItem<EldEventAnnotationCommentItem>() {
                    @Override
                    public EldEventAnnotationCommentItem OnEldItemCreation(Cursor c, int counter) {
                        EldEventAnnotationCommentItem item = new EldEventAnnotationCommentItem();

                        item.EventSequenceIDNumber = c.getString(counter++);
                        item.EldUsernameOfTheRecordOriginator = username;
                        item.EventCommentTextOrAnnotation = c.getString(counter++);

//                        item.EventDate = c.getString(counter++);
//                        item.EventTime = c.getString(counter++);
                        item.EventDate = DateUtils.getDateFromDateAndTime(c.getString(counter++));
                        item.EventTime = DateUtils.getTimeFromDateAndTime(c.getString(counter++));
                        item.DriversLocationDescription = c.getString(counter++);

                        return item;
                    }
                }, days);
    }

    private ArrayList<EldEventItem> getEldEventItems(String username, int days) {
        return getEldItems(
                "SELECT sequenceId, recordStatus, recordOrigin, eventType, eventCode, creationdate, creationdate, " +
                        "vehicleMiles, engineHours, latitudeString, longitudeString, dstSinceLastValidCoords, " +
                        "orderNumbercmv, orderNumberUser, malfunctionIndicator, diagnosticIndicator, checkData FROM eldevents WHERE eldUsername='" + username + "' AND " +
                        "(eventType=" + EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.getValue() + " OR eventType=" + EventType.AN_INTERMEDIATE_LOG.getValue() + " OR " +
                        "eventType=" + EventType.A_CHANGE_IN_DRIVERS_INDICATION_OF_AUTHORIZED_PERSONAL_USE_OF_CMV_OR_YARD_MOVES.getValue() + ")",
                new ILoadEldItem<EldEventItem>() {
                    @Override
                    public EldEventItem OnEldItemCreation(Cursor c, int counter) {
                        EldEventItem item = new EldEventItem();

                        item.EventSequenceIdNumber = c.getString(counter++);
                        item.EventRecordStatus = c.getString(counter++);
                        item.EventRecordOrigin = c.getString(counter++);
                        item.EventType = c.getString(counter++);
                        item.EventCode = c.getString(counter++);

                        item.EventDate = DateUtils.getDateFromDateAndTime(c.getString(counter++));
                        item.EventTime = DateUtils.getTimeFromDateAndTime(c.getString(counter++));
                        item.AccumulatedVehicleMiles = c.getString(counter++);
                        item.ElapsedEngineHours = c.getString(counter++);
                        item.EventLatitude = c.getString(counter++);
                        item.EventLongitude = c.getString(counter++);
                        item.DistanceSinceLastValidCoordinates = c.getString(counter++);
                        item.CorrespondingCmvOrderNumber = c.getString(counter++);
                        item.UserOrderNumberForRecordOriginator = c.getString(counter++);
                        item.MalfunctionIndicatorStatusForEld = c.getString(counter++);
                        item.DataDiagnosticEventIndicatorStatusForDriver = c.getString(counter++);
//                        April 27, 2022    -   from documentation
                        item.EventCheckData = c.getString(counter++);
                        // TODO: Event data check value needs to be specified further

//                        Log.d(TAG, "OnEldItemCreation: EventSequenceIdNumber: " + item.EventSequenceIdNumber);
//                        Log.d(TAG, "OnEldItemCreation: EventRecordStatus: " + item.EventRecordStatus);
//                        Log.d(TAG, "OnEldItemCreation: EventRecordOrigin: " + item.EventRecordOrigin);
//                        Log.d(TAG, "OnEldItemCreation: EventType: " + item.EventType);
//                        Log.d(TAG, "OnEldItemCreation: EventCode: " + item.EventCode);
//                        Log.d(TAG, "OnEldItemCreation: EventDate: " + item.EventDate);
//                        Log.d(TAG, "OnEldItemCreation: EventTime: " + item.EventTime);
//                        Log.d(TAG, "OnEldItemCreation: AccumulatedVehicleMiles: " + item.AccumulatedVehicleMiles);
//                        Log.d(TAG, "OnEldItemCreation: ElapsedEngineHours: " + item.ElapsedEngineHours);
//                        Log.d(TAG, "OnEldItemCreation: EventLatitude: " + item.EventLatitude);
//                        Log.d(TAG, "OnEldItemCreation: EventLongitude: " + item.EventLongitude);
//                        Log.d(TAG, "OnEldItemCreation: DistanceSinceLastValidCoordinates: " + item.DistanceSinceLastValidCoordinates);
//                        Log.d(TAG, "OnEldItemCreation: CorrespondingCmvOrderNumber: " + item.CorrespondingCmvOrderNumber);
//                        Log.d(TAG, "OnEldItemCreation: UserOrderNumberForRecordOriginator: " + item.UserOrderNumberForRecordOriginator);
//                        Log.d(TAG, "OnEldItemCreation: MalfunctionIndicatorStatusForEld: " + item.MalfunctionIndicatorStatusForEld);
//                        Log.d(TAG, "OnEldItemCreation: DataDiagnosticEventIndicatorStatusForDriver: " + item.DataDiagnosticEventIndicatorStatusForDriver);
//                        Log.d(TAG, "OnEldItemCreation: EventCheckData: " + item.EventCheckData);
//                        Log.d(TAG, "OnEldItemCreation: ");


                        return item;
                    }
                }, days);
    }


    private ArrayList<EldCmvItem> getEldCmvItems(String username, int days) {

        return getEldItems(
                "SELECT orderNumbercmv, truckNumber, vin FROM eldevents WHERE eldUsername='" + username +
                        "' AND eventType=" + EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.getValue() + " AND eventCode=" + EventCode.DRIVING.getValue(),
                new ILoadEldItem<EldCmvItem>() {
                    @Override
                    public EldCmvItem OnEldItemCreation(Cursor c, int counter) {
                        EldCmvItem item = new EldCmvItem();

                        item.AssignedCmvOrderNumber = c.getString(counter++);
                        item.CmvPowerUnitNumber = c.getString(counter++);
                        item.CmvVin = c.getString(counter++);

                        return item;
                    }
                }, days, "");
    }

    private ArrayList<EldUserItem> getEldUserItems(String username, int days) {
        String lastKnownVin = getLastKnownVin(username);

//        return getEldItems(
        return getEldUserUniqueItem(
                "SELECT orderNumberUser, driverFirstName, driverLastName FROM eldevents WHERE eldUsername='" + username +
                        "' AND vin='" + lastKnownVin + "' AND eventType=" + EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.getValue() + " AND eventCode=" + EventCode.DRIVING.getValue(),
                new ILoadEldItem<EldUserItem>() {
                    @Override
                    public EldUserItem OnEldItemCreation(Cursor c, int counter) {
                        EldUserItem item = new EldUserItem();

                        item.AssignedUserOrderNumber = c.getString(counter++);
                        item.UsersEldAccountType = "D"; // Indicating account type â€œDriverâ€, or â€œSâ€, indicating account type â€œmotor carrier's support personnelâ€ (i.e. non-driver); â€œUnidentified Driverâ€ account must be designated with type â€œDâ€ as well
                        item.UsersLastName = c.getString(counter++);
                        item.UsersFirstName = c.getString(counter++);

                        return item;
                    }
                }, days);
    }

    private <T> ArrayList<T> getEldItems(String sqlString, ILoadEldItem<T> itemsAssigner,
                                         int days) {
        ArrayList<T> result = new ArrayList();

        try {
            int hours = days * 24;
            Date d = DateUtils.addHours(new Date(), -hours);
            String creationDateStr = DateUtils.getYyyyMmDdStr(d);

            String endOfToday = DateUtils.getNowYyyyMmDd() + " 23:59:59";

            String sqlQuery = sqlString + " AND creationdate>='" + creationDateStr + " 00:00:00' AND creationdate<='" + endOfToday + "' ORDER BY sequenceId DESC";
//            Log.d(TAG, "getEldCmvItems: getEldItems: sqlQuery: " + sqlQuery);
            Cursor c = db.getQuery(sqlQuery);

            if (c == null)
                return null;

            c.moveToFirst();
            int counter = 0;

            while (!c.isAfterLast()) {
                result.add(itemsAssigner.OnEldItemCreation(c, counter));
                c.moveToNext();
            }

            c.close();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }

        return result;
    }

    private <
            T> ArrayList<T> getEldUserUniqueItem(String sqlString, ILoadEldItem<T> itemsAssigner,
                                                 int days) {
        ArrayList<T> result = new ArrayList();
        try {
            int hours = days * 24;
            Date d = DateUtils.addHours(new Date(), -hours);
            String creationDateStr = DateUtils.getYyyyMmDdStr(d);

            String endOfToday = DateUtils.getNowYyyyMmDd() + " 23:59:59";

            String sqlQuery = sqlString + " AND creationdate>='" + creationDateStr + " 00:00:00' AND creationdate<='" + endOfToday + "' ORDER BY sequenceId DESC";
            Cursor c = db.getQuery(sqlQuery);

            if (c == null)
                return null;

            c.moveToFirst();
            int counter = 0;

            ArrayList<String> userList = new ArrayList<>();
            while (!c.isAfterLast()) {
                T eldUserItem = itemsAssigner.OnEldItemCreation(c, counter);
                EldUserItem eldUserItem1 = (EldUserItem) eldUserItem;
                String fullName = eldUserItem1.UsersFirstName.trim() + eldUserItem1.UsersLastName.trim();
                if (!userList.contains(fullName)) {
                    userList.add(eldUserItem1.UsersFirstName.trim() + eldUserItem1.UsersLastName.trim());
                    result.add(itemsAssigner.OnEldItemCreation(c, counter));
                }
                c.moveToNext();
            }


            c.close();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }

        return result;
    }

    private <T> ArrayList<T> getEldItems(String sqlString, ILoadEldItem<T> itemsAssigner,
                                         int days, String uniqueField) {
        ArrayList<T> result = new ArrayList();

        try {
            int hours = days * 24;
            Date d = DateUtils.addHours(new Date(), -hours);
            String creationDateStr = DateUtils.getYyyyMmDdStr(d);

            String endOfToday = DateUtils.getNowYyyyMmDd() + " 23:59:59";

//            "SELECT orderNumbercmv, truckNumber, vin FROM eldevents WHERE eldUsername='" + username +
//                    "' AND eventType=" + EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.getValue() + " AND eventCode=" + EventCode.DRIVING.getValue(),


            String sqlQuery = sqlString + " AND creationdate>='" + creationDateStr + " 00:00:00' AND creationdate<='" + endOfToday + "' ORDER BY sequenceId DESC";
//            Log.d(TAG, "getEldCmvItems: getEldItems: sqlQuery: " + sqlQuery);
            Cursor c = db.getQuery(sqlQuery);

            if (c == null)
                return null;

            c.moveToFirst();
            int counter = 0;

            ArrayList<String> vinList = new ArrayList<>();
            while (!c.isAfterLast()) {
                T eldCmvItem = itemsAssigner.OnEldItemCreation(c, counter);
                EldCmvItem eldCmvItem1 = (EldCmvItem) eldCmvItem;
                if (!vinList.contains(eldCmvItem1.CmvVin)) {
                    vinList.add(eldCmvItem1.CmvVin);
                    result.add(itemsAssigner.OnEldItemCreation(c, counter));
                }
                c.moveToNext();
            }

            c.close();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }

        return result;
    }

    private ArrayList<EldEvent> getEldItemsForToday(String username) {
        ArrayList<EldEvent> result = new ArrayList();

        String todayStartStr = DateUtils.getNowYyyyMmDd() + " 00:00:00";
        String todayEndStr = DateUtils.getNowYyyyMmDd() + " 23:59:59";

        String sqlString =
                "SELECT " +
                        "sequenceId, recordStatus, recordOrigin, eventType, eventCode, creationdate, " +
                        "vehicleMiles, engineHours, latitudeString, longitudeString, dstSinceLastValidCoords, " +
                        "orderNumbercmv, orderNumberUser, malfunctionIndicator, diagnosticIndicator, odometer " +
                        "FROM " +
                        "eldevents " +
                        "WHERE " +
                        "eldUsername='" + username + "' " +
                        "AND (" +
                        "eventType=" + EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.getValue() + " " +
                        "OR " +
                        "eventType=" + EventType.AN_INTERMEDIATE_LOG.getValue() + " " +
                        "OR " +
                        "eventType=" + EventType.A_CHANGE_IN_DRIVERS_INDICATION_OF_AUTHORIZED_PERSONAL_USE_OF_CMV_OR_YARD_MOVES.getValue() +
                        ") " +
                        "AND " +
                        "creationdate>='" + todayStartStr + "' AND creationdate<='" + todayEndStr + "' " +
                        "ORDER BY " +
                        "sequenceId ASC";

        Cursor c = db.getQuery(sqlString);

        if (c == null)
            return null;

        c.moveToFirst();
        int counter;

        while (!c.isAfterLast()) {
            counter = 0;
            EldEvent item = new EldEvent();

            item.SequenceId = c.getString(counter++);
            item.RecordStatus = c.getString(counter++);
            item.RecordOrigin = c.getString(counter++);
            item.EventType = c.getString(counter++);
            item.EventCode = c.getString(counter++);
            item.CreationDate = c.getString(counter++);
            item.VehicleMiles = c.getString(counter++);
            item.EngineHours = c.getString(counter++);
            item.LatitudeString = c.getString(counter++);
            item.LongitudeString = c.getString(counter++);
            item.DstSinceLastValidCoords = c.getString(counter++);
            item.OrderNumberCmv = c.getString(counter++);
            item.OrderNumberUser = c.getString(counter++);
            item.MalfunctionIndicator = c.getString(counter++);
            item.DiagnosticIndicator = c.getString(counter++);
            item.Odometer = c.getString(counter++);

            result.add(item);
            c.moveToNext();
        }

        c.close();
        return result;
    }

    private EldEvent getLastEldItem(String username) {
        String sqlString =
                "SELECT " +
                        "sequenceId, recordStatus, recordOrigin, eventType, eventCode, creationdate, " +
                        "vehicleMiles, engineHours, latitudeString, longitudeString, dstSinceLastValidCoords, " +
                        "orderNumbercmv, orderNumberUser, malfunctionIndicator, diagnosticIndicator, odometer " +
                        "FROM " +
                        "eldevents " +
                        "WHERE " +
                        "eldUsername='" + username + "' " +
                        "AND (" +
                        "eventType=" + EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.getValue() + " " +
                        "OR " +
                        "eventType=" + EventType.AN_INTERMEDIATE_LOG.getValue() + " " +
                        "OR " +
                        "eventType=" + EventType.A_CHANGE_IN_DRIVERS_INDICATION_OF_AUTHORIZED_PERSONAL_USE_OF_CMV_OR_YARD_MOVES.getValue() +
                        ") " +
                        "ORDER BY " +
                        "sequenceId DESC " +
                        "LIMIT 1";

        Cursor c = db.getQuery(sqlString);

        if (c == null)
            return null;

        c.moveToFirst();
        int counter;
        EldEvent item = new EldEvent();

        if (!c.isAfterLast()) {
            counter = 0;

            item.SequenceId = c.getString(counter++);
            item.RecordStatus = c.getString(counter++);
            item.RecordOrigin = c.getString(counter++);
            item.EventType = c.getString(counter++);
            item.EventCode = c.getString(counter++);
            item.CreationDate = c.getString(counter++);
            item.VehicleMiles = c.getString(counter++);
            item.EngineHours = c.getString(counter++);
            item.LatitudeString = c.getString(counter++);
            item.LongitudeString = c.getString(counter++);
            item.DstSinceLastValidCoords = c.getString(counter++);
            item.OrderNumberCmv = c.getString(counter++);
            item.OrderNumberUser = c.getString(counter++);
            item.MalfunctionIndicator = c.getString(counter++);
            item.DiagnosticIndicator = c.getString(counter++);
            item.Odometer = c.getString(counter++);
        }

        c.close();
        return item;
    }

    public interface ILoadEldItem<T> {
        T OnEldItemCreation(Cursor c, int counter);
    }

    public enum EldTransferOption {
        Email, WebService;
    }

    //endregion

    //region Other ELD queries

    public boolean isOnDutyStartShiftEvent() {
        return isShiftStart();
    }

    public boolean isPositionMalfunctionEvent() {
        if (!existsActiveMalfunction())
            return false;

        return isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_POSITION_COMPLIANCE);
    }

    public boolean isPositionDiagnosticEvent() {
        if (!existsActiveDiagnostic())
            return false;

        return isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_POSITION_COMPLIANCE); // TODO: There is no concept of POSITION DIAGNOSTIC defined until now, check with Dragos task https://app.asana.com/0/229158745004238/1199908229538032
    }

    public String getEngineHoursForEventType(EventType eventType) {
        return eventType == EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS ? getBleParameter("TripEngineHours") : getBleParameter("EngineHours");
    }

    public String getVehiceMilesForEventType(EventType eventType) {
        return eventType == EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS ? getBleParameter("TripDistance") : getBleParameter("Odometer");
    }

    public boolean getMalfunctionIndicatorStatusForDate() {
        return existsActiveMalfunction();
    }

    public boolean getDiagnosticIndicatorStatusForDriverAndDate() {
        return existsActiveDiagnostic();
    }

    public void createELDEvent(Activity a, BusinessRules.EventCode eventCode, String
            annotationSubTitle, String annotationMsg) {
//        Log.d(TAG, "TestingDriving: createELDEvent: eventCode: " + eventCode);
        setEldEvent(a, eventCode, annotationSubTitle, annotationMsg);
    }

    //endregion

    //region Continuous Driving


    private int continousDrivingMins = 60;

    //    set continuous driving time to 60 in debug mode also
    private int continousDrivingMinsDebug = 60;

    public boolean needToRecordContinuousDriving() {
        Log.d(TAG, "needToRecordContinuousDriving: ");
        EldEvent e = getLatestDutyOrDrivingIntermediateEvent();
        Log.d(TAG, "needToRecordContinuousDriving: eldEvent: " + e);

        if (e == null || StringUtils.isNullOrWhitespaces(e.CreationDate))
            return false;

        if (e.EventType == null || (e.EventCode == null && StringUtils.isNullOrWhitespaces(e.EventCodeDescription)))
            return false;

        if (!e.EventType.equalsIgnoreCase(Integer.toString(EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS.value)) &&
                !e.EventType.equalsIgnoreCase(Integer.toString(EventType.AN_INTERMEDIATE_LOG.value)))
            return false;

        Log.d(TAG, "needToRecordContinuousDriving: eldEvent: " + e.EventCode);

        boolean isDrivingEvent = e.EventCode != null && e.EventCode.equalsIgnoreCase(EventCode.DRIVING.value);
        Log.d(TAG, "needToRecordContinuousDriving: isDrivingEvent: " + isDrivingEvent + " EventCodeDescription: " + e.EventCodeDescription);

        if (!isDrivingEvent) {
//            isDrivingEvent = !StringUtils.isNullOrWhitespaces(e.EventCodeDescription) && e.EventCodeDescription.equalsIgnoreCase("Driving");
//            Dec 23, 2022  -   Doing in old fashin will improve
            Log.d(TAG, "needToRecordContinuousDriving: idDrivingEvent: " + isDrivingEvent);
            if (!StringUtils.isNullOrWhitespaces(e.EventType) && !StringUtils.isNullOrWhitespaces(e.EventCode)) {
                Log.d(TAG, "needToRecordContinuousDriving: event type and code is not null or empty eventType: " + e.EventType);
                Log.d(TAG, "needToRecordContinuousDriving: event type and code is not null or empty eventCode: " + e.EventCode);
                if (e.EventType.equalsIgnoreCase("2")) {
                    isDrivingEvent = true;
                } else if (e.EventType.equalsIgnoreCase("1") && e.EventCode.equalsIgnoreCase("3")) {
                    isDrivingEvent = true;
                }
            }
        }
        Log.d(TAG, "needToRecordContinuousDriving: isDrivingEvent: " + isDrivingEvent);

        if (!isDrivingEvent) {
            Log.d(TAG, "needToRecordContinuousDriving: isDrivingEvent: from if: " + isDrivingEvent);
            return false;
        }

        try {
            SimpleDateFormat from = new SimpleDateFormat(DateUtils.FORMAT_DATE_TIME_SEC);
            Date d = from.parse(e.CreationDate);
            Date now = new Date();
            long deltaMins = (now.getTime() - d.getTime()) / 1000 / 60;
            Log.d(TAG, "needToRecordContinuousDriving: deltaMins: " + deltaMins);
            Log.d(TAG, "needToRecordContinuousDriving: isDebugModel: " + isDebugMode);
            Log.d(TAG, "needToRecordContinuousDriving: continousDrivingMinsDebug: " + continousDrivingMinsDebug);
            Log.d(TAG, "needToRecordContinuousDriving: continousDrivingMins: " + continousDrivingMins);
            Log.d(TAG, "needToRecordContinuousDriving: deltaMins>: " + (deltaMins > (isDebugMode() ? continousDrivingMinsDebug : continousDrivingMins)));

            return deltaMins > (isDebugMode() ? continousDrivingMinsDebug : continousDrivingMins);//TODO Dec 22, 2022   -   Intermediate_Driving Interval
        } catch (ParseException ex) {
            if (ex != null)
                ex.printStackTrace();
            Log.d(TAG, "needToRecordContinuousDriving: ParseException: " + ex.getMessage());
        }

        return false;
    }

    public void recordContinuousDrivingEvent(Activity a, boolean isDriverInDutyStatus) {
//        Log.d(TAG, "recordContinuousDrivingEvent: needToRecordContinuousDriving: isDriverInDutyStatus: " + isDriverInDutyStatus);
        EventCode oldEventCode = this.eventCode;

        setEldEvent(a,
                EventType.AN_INTERMEDIATE_LOG,
                isDriverInDutyStatus ? EventCode.INTERMEDIATE_LOG_WITH_CONVENTIONAL_LOCATION_PRECISION : EventCode.INTERMEDIATE_LOG_WITH_REDUCED_LOCATION_PRECISION,
                EventRecordStatus.ACTIVE,
                EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD, "Driving INTERMEDIATE", "Driving INTERMEDIATE", "", "");

        this.eventCode = oldEventCode;
//        March 15, 2020 -  Saved the most recent event code
        this.eventCodeDutyEvent = EventCode.DRIVING;

        if (bleDevice != null)
            bleDevice.resetBleDataCounters();
    }

    public EventCode getDutyEventCode() {
//        Log.d(TAG, "checkIsSleeperStarted: getDutyEventCode: " + this.eventCodeDutyEvent);
        //15.03.2022 we should return the most recent Duty Event Code
//        Log.d(TAG, "checkIsSleeperStarted: getDutyEventCode: if equals to NOTSET: " + (this.eventCodeDutyEvent == EventCode.NOT_SET));
        if (this.eventCodeDutyEvent == EventCode.NOT_SET) {
            // we should get it from the local DB
            String eventCode = getMostRecentEventCodeAsStringForEventType(EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS);
//            Log.d(TAG, "checkIsSleeperStarted: getDutyEventCode: returned: eventCode: " + eventCode);
            if (eventCode != null) {
                if (eventCode.equalsIgnoreCase("1")) {
                    this.eventCodeDutyEvent = EventCode.OFF_DUTY;
                } else if (eventCode.equalsIgnoreCase("2")) {
                    this.eventCodeDutyEvent = EventCode.SLEEPER_BERTH;
                } else if (eventCode.equalsIgnoreCase("3")) {
                    this.eventCodeDutyEvent = EventCode.DRIVING;
                } else if (eventCode.equalsIgnoreCase("4")) {
                    this.eventCodeDutyEvent = EventCode.ON_DUTY_NOT_DRIVING;
                }
            }
        }
//        Log.d(TAG, "getDutyEventCode: checkIsSleeperStarted: eventCodeDutyEvent: " + this.eventCodeDutyEvent);
        if (this.eventCodeDutyEvent == null) {
            this.eventCodeDutyEvent = EventCode.NOT_SET;
        }
//        Log.d(TAG, "checkIsSleeperStarted: getDutyEventCode: return eventCodeDutyEvent: " + this.eventCodeDutyEvent);
        return this.eventCodeDutyEvent;
    }

    public void resetDutyEventCode() {
        this.eventCodeDutyEvent = EventCode.NOT_SET;
    }


    //    March 22, 2022    -
    public String getMostRecentEventCodeAsStringForEventType(EventType eventType) {
        EldEvent mostRecentEvent = getMostRecentEventType(eventType, null);
        if (mostRecentEvent == null) {
            return null;
        }
        return mostRecentEvent.EventCode;
    }

    public EventCode getMostRecentEventCodeForEventType(EventType eventType, SessionManagement
            sessionManagement) {
        EldEvent mostRecentEvent = getMostRecentEventForType(eventType, null, sessionManagement);
        if (mostRecentEvent == null) {
            return null;
        }

        return getEventCodeFromString(mostRecentEvent.EventCode);
    }

    public static EventCode getEventCodeFromString(String displayString) {
//        Log.d(TAG, "getEventCodeFromString: displayString: " + displayString);
        for (EventCode type : EventCode.values()) {
//            Log.d(TAG, "getEventCodeFromString: type: " + type);
            if (type.getValue().equals(displayString)) {
//                Log.d(TAG, "getEventCodeFromString: type: selected: one: " + type);
                return type;
            }
        }

        return null; //not found
    }

    private EldEvent getMostRecentEventForType(EventType eventType, String
            username, SessionManagement sessionManagement) {
        // 14.03.2022 get the most recent event type for selected username
        if (username == null && user != null) {
            username = user.getLogin();
        }

        if (username == null) {
            username = sessionManagement.getKeyLogin();
        }


        ArrayList<EldEvent> result = new ArrayList();

//        String sqlString =
//                "SELECT " +
//                        "eventType, eventCode, eldUsername, id " +
//                        "FROM " +
//                        "eldevents " +
//                        "WHERE " +
//                        "eldUsername='" + username + "' " +
//                        "AND " +
//                        "eventType='" + eventType.getValue() + "' " +
//                        "ORDER BY " +
//                        "id DESC";

//        June 03, 2022 -   we change it today and order by "eventseconds" in DESC because the last 'id' is not giving the last duty event
        String sqlString =
                "SELECT " +
                        "eventType, eventCode, eldUsername, id, eventseconds, recordId " +
                        "FROM " +
                        "eldevents " +
                        "WHERE " +
                        "eldUsername='" + username + "' " +
                        "AND " +
                        "eventType='" + eventType.getValue() + "' " +
                        "ORDER BY " +
//                        "eventseconds DESC, id DESC";
//                        "eventseconds DESC";
                        "id DESC";


        if (db == null) {
            return null;
        }
        Cursor c = db.getQuery(sqlString);

        if (c == null)
            return null;

//        Log.d(TAG, "getMostRecentEventForType: cursor: size: " + c.getCount());
        c.moveToFirst();
        int counter;

        EldEvent item = new EldEvent();

        while (!c.isAfterLast()) {
            counter = 0;
            item.EventType = c.getString(counter++);
            item.EventCode = c.getString(counter++);
            item.EldUsername = c.getString(counter++);
            item.Id = c.getString(counter++);
            item.EventSeconds = c.getDouble(counter++);
            item.RecordId = c.getString(counter++);

//            Log.d(TAG, "getMostRecentEventForType: item.EventCode: "
//                    + getEventCodeFromString(item.EventCode) + " RecordId: " + item.RecordId);


            result.add(item);
            c.moveToNext();

            return item;
        }
        c.close();
        return null;
    }


    private String getMostRecentTimeStampForEvent(String username) {
        // 14.03.2022 get the most recent event type for selected username
//        Log.d(TAG, "getMostRecentTimeStampForEvent: username: " + username);
        if (username == null && user != null) {
            username = user.getLogin();
        }

//        Log.d(TAG, "getMostRecentTimeStampForEvent: username: " + username);
        if (username == null) {
            return null;
        }

        String sqlString =
                "SELECT " +
                        "rmsTimestamp, objectId, objectType " +
                        "FROM " +
                        "eldevents " +
                        "WHERE " +
                        "eldUsername='" + username + "' " +
                        "ORDER BY " +
                        "rmsTimestamp DESC";

//        Log.d(TAG, "getMostRecentTimeStampForEvent: sqlString: " + sqlString);
//        Log.d(TAG, "getMostRecentTimeStampForEvent: db: " + db);
        if (db == null) {
            return null;
        }
        Cursor c = db.getQuery(sqlString);

        if (c == null)
            return null;

//        Log.d(TAG, "getMostRecentTimeStampForEvent: cursor: size: " + c.getCount());
        c.moveToFirst();
        int counter;

        if (c.getCount() > 0) {
//            c.close();
            return c.getString(0) + "," + c.getString(1) + "," + c.getString(2);
        }

        return null;
    }

    private String getMostRecentTimeStampForPreTrip(String username) {
        // 14.03.2022 get the most recent event type for selected username
//        Log.d(TAG, "getMostRecentTimeStampForEvent: username: " + username);
        String recordId = null;
        if (username == null && user != null) {
            username = user.getLogin();
            recordId = user.getRecordId();
        }

//        Log.d(TAG, "getMostRecentTimeStampForEvent: username: " + username);
        if (recordId == null) {
            return null;
        }


        String sqlString =
                "SELECT " +
                        "rmsTimestamp, objectId, objectType " +
                        "FROM " +
                        "pretrip " +
                        "WHERE " +
                        "userRecordId='" + recordId + "' " +
                        "ORDER BY " +
                        "rmsTimestamp DESC";

//        Log.d(TAG, "getMostRecentTimeStampForEvent: sqlString: " + sqlString);
//        Log.d(TAG, "getMostRecentTimeStampForEvent: db: " + db);
        if (db == null) {
            return null;
        }
        Cursor c = db.getQuery(sqlString);

        if (c == null)
            return null;

//        Log.d(TAG, "getMostRecentTimeStampForEvent: cursor: size: " + c.getCount());
        c.moveToFirst();
        int counter;

        if (c.getCount() > 0) {
//            c.close();
            return c.getString(0) + "," + c.getString(1) + "," + c.getString(2);
        }

        return null;
    }


    private String getMostRecentTimeStampForTollReceipt(String username) {
        // 14.03.2022 get the most recent event type for selected username
//        Log.d(TAG, "getMostRecentTimeStampForEvent: username: " + username);
        String recordId = null;
        if (username == null && user != null) {
            username = user.getLogin();
            recordId = user.getRecordId();
        }

//        Log.d(TAG, "getMostRecentTimeStampForEvent: username: " + username);
        if (recordId == null) {
            return null;
        }


        String sqlString =
                "SELECT " +
                        "rmsTimestamp, objectId, objectType " +
                        "FROM " +
                        "tollreceipt " +
                        "WHERE " +
                        "userRecordId='" + recordId + "' " +
                        "ORDER BY " +
                        "rmsTimestamp DESC";

//        Log.d(TAG, "getMostRecentTimeStampForEvent: sqlString: " + sqlString);
//        Log.d(TAG, "getMostRecentTimeStampForEvent: db: " + db);
        if (db == null) {
            return null;
        }
        Cursor c = db.getQuery(sqlString);

        if (c == null)
            return null;

//        Log.d(TAG, "getMostRecentTimeStampForEvent: cursor: size: " + c.getCount());
        c.moveToFirst();
        int counter;

        if (c.getCount() > 0) {
//            c.close();
            return c.getString(0) + "," + c.getString(1) + "," + c.getString(2);
        }

        return null;
    }

    private String getMostRecentTimeStampForFuelReceipt(String username) {
        Log.d(TAG, "timeStamp: getMostRecentTimeStampForFuelReceipt: ");
        // 14.03.2022 get the most recent event type for selected username
//        Log.d(TAG, "getMostRecentTimeStampForEvent: username: " + username);
        String recordId = null;
        if (username == null && user != null) {
            username = user.getLogin();
            recordId = user.getRecordId();
        }

//        Log.d(TAG, "getMostRecentTimeStampForEvent: username: " + username);
        if (recordId == null) {
            return null;
        }


        String sqlString =
                "SELECT " +
                        "rmsTimestamp, objectId, objectType " +
                        "FROM " +
                        "fuelreceipt " +
                        "WHERE " +
                        "userRecordId='" + recordId + "' " +
                        "ORDER BY " +
                        "rmsTimestamp DESC";

        Log.d(TAG, "timeStamp: getMostRecentTimeStampForFuelReceipt: sqlString: " + sqlString + " db" + db);
        if (db == null) {
            return null;
        }
        Cursor c = db.getQuery(sqlString);
        Log.d(TAG, "timeStamp: getMostRecentTimeStampForFuelReceipt: cursor: " + c);

        if (c == null)
            return null;

        Log.d(TAG, "timeStamp: getMostRecentTimeStampForEvent: cursor: size: " + c.getCount());
        c.moveToFirst();
        int counter;

        if (c.getCount() > 0) {
//            c.close();
            String timeStamp = c.getString(0) + "," + c.getString(1) + "," + c.getString(2);
//            String timeStamp="1670395070609" + "," + "3159" + "," + c.getString(2);
            Log.d(TAG, "rmsTimestamp: timeStamp: getMostRecentTimeStampForFuelReceipt: timeStamp: " + timeStamp);
            return timeStamp;
        }

        return null;
    }


    public Double getMostRecentEventCreationTime(EventType eventType, String username) {
        // 14.03.2022 get the most recent event type for selected username
        if (username == null) {
            username = user.getLogin();
        }
        ArrayList<EldEvent> result = new ArrayList();

        String sqlString =
                "SELECT " +
                        "eventseconds " +
                        "FROM " +
                        "eldevents " +
                        "WHERE " +
                        "eldUsername='" + username + "' " +
                        "AND " +
                        "eventType='" + eventType.getValue() + "' " +
                        "ORDER BY " +
                        "eventseconds DESC";

        Cursor c = db.getQuery(sqlString);

        if (c == null)
            return 0.0;

        c.moveToFirst();
        int counter;

        EldEvent item = new EldEvent();

        while (!c.isAfterLast()) {
            counter = 0;
            item.EventSeconds = c.getDouble(counter++);

            result.add(item);
            c.moveToNext();

            return item.EventSeconds;
        }
        c.close();
        return 0.0;
    }

    public Double getMostRecentEventCreationTime(EventType eventType, String
            username, EventCode eventCode) {
        // 14.03.2022 get the most recent event type for selected username
        if (username == null) {
            username = user.getLogin();
        }
        ArrayList<EldEvent> result = new ArrayList();

        String sqlString =
                "SELECT " +
                        "eventseconds " +
                        "FROM " +
                        "eldevents " +
                        "WHERE " +
                        "eldUsername='" + username + "' " +
                        "AND " +
                        "eventType='" + eventType.getValue() + "' " + " AND eventCode='" + eventCode.getValue() + "' " +
                        "ORDER BY " +
                        "eventseconds DESC";

        Cursor c = db.getQuery(sqlString);

        if (c == null)
            return 0.0;

        c.moveToFirst();
        int counter;

        EldEvent item = new EldEvent();

        while (!c.isAfterLast()) {
            counter = 0;
            item.EventSeconds = c.getDouble(counter++);

            result.add(item);
            c.moveToNext();

            return item.EventSeconds;
        }
        c.close();
        return 0.0;
    }


    public String getMostRecentDutyState() {
//        Log.d(TAG, "getMostRecentDutyState: ");

//        March 24, 2022    -   We should not get the most recent status from the local database all the time
        EventCode dutyEventCode = getDutyEventCode();

        if (dutyEventCode == EventCode.OFF_DUTY) {
            return "Off Duty";
        } else if (dutyEventCode == EventCode.SLEEPER_BERTH) {
            return "Sleeper";
        } else if (dutyEventCode == EventCode.DRIVING) {
            return "Driving";
        } else if (dutyEventCode == EventCode.ON_DUTY_NOT_DRIVING) {
            return "On Duty";
        }

        return null;
    }

    private EldEvent getMostRecentEventType(EventType eventType, String username) {
        // 14.03.2022 get the most recent event type for selected username
        Log.d(TAG, "checkIsSleeperStarted: getMostRecentEventType: ");

        if (username == null) {
            if (user == null)
                return null;

            Log.d(TAG, "getMostRecentEventType: user: " + user);
            username = user.getLogin();
            if (username.isEmpty()) {

            }

        }


        ArrayList<EldEvent> result = new ArrayList();

        String sqlString =
                "SELECT " +
                        "eventType, eventCode, eldUsername, id, sequenceId " +
                        "FROM " +
                        "eldevents " +
                        "WHERE " +
                        "eldUsername='" + username + "' " +
                        "AND " +
                        "eventType='" + eventType.getValue() + "' " +
                        "ORDER BY " +
//                        "creationdate DESC, sequenceId DESC";
                        "eventSeconds DESC, sequenceId DESC";


        Log.d(TAG, "checkIsSleeperStarted: getMostRecentEventType: sqlString: " + sqlString);
        Cursor c = db.getQuery(sqlString);
        Log.d(TAG, "getMostRecentEventType: cursor: " + c);

        if (c == null)
            return null;

        Log.d(TAG, "checkIsSleeperStarted: getMostRecentEventType: cursor: length: " + c.getCount());

        c.moveToFirst();
        int counter;

        EldEvent item = new EldEvent();

        while (!c.isAfterLast()) {
            counter = 0;
            item.EventType = c.getString(counter++);
            item.EventCode = c.getString(counter++);
            item.EldUsername = c.getString(counter++);
            item.Id = c.getString(counter++);
            item.SequenceId = c.getString(counter++);
            Log.d(TAG, "getMostRecentEventType: EventCode: " + item.EventCode);
//            Log.d(TAG, "checkIsSleeperStarted: getMostRecentEventType: sequenceId: " + item.SequenceId);


            result.add(item);
            c.moveToNext();

//            Log.d(TAG, "checkIsSleeperStarted: getMostRecentEventType: item: " + item.Id + " eventCode: " + item.EventCode + " eventType: " + item.EventType + " SequenceId: " + item.SequenceId);
            return item;
        }
        c.close();
        return null;
    }

    private EldEvent getLatestDutyOrDrivingIntermediateEvent() {
        lock.lock();
        try {
//            June 01, 2022 -   We need to skip the custom events so we added the condition sequenceId!='0' (sequenceId is different than zero)
//            June 08, 2022 -   We should get just the duty events and intermediate events
            String sqlString =
                    "SELECT " +
                            "id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, recordOrigin, " +
                            "truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, " +
                            "vehicleMiles, engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, " +
                            "diagnosticIndicator, malfunctionIndicator, annotation, recordOriginId, checkData, checkSum, " +
                            "malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                            "editReason, eventseconds, shiftstart, creationdate, odometer, recordId " +
                            "FROM " +
                            "eldevents " +
                            "WHERE " +
                            "driverRecordId='" + user.getRecordId() + "' AND " +
//                            "sequenceId!='0' " +
                            "(eventType='1' OR " +
                            "eventType='2') " +
                            "ORDER BY " +
                            "id DESC " +
                            "LIMIT 1";

            if (!db.exists(sqlString))
                return null;

            Cursor c = db.getRow(sqlString);

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;
            EldEvent e = new EldEvent();

            if (!c.isAfterLast()) {
                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);
                e.RecordId = c.getString(col++);
//                Log.d(TAG, "generateCertifyEvent: getLatestEvent: recordId: " + e.RecordId);

            }


            c.close();
            return e;
        } finally {
            lock.unlock();
        }
    }

    //endregion

    //region Shift Start

    public boolean isShiftStart() {
        // TODO: Do not use. Function has a bug. If multiple on duty events, they will all be marked as shift start
        ArrayList<EldEvent> events = getAllDutyEventsSincePast14Hours();

        EldEvent sbEvent = findMostRecentSleeperBerthEvent(events);
        EldEvent odEvent = findMostRecentOnDutyStartShiftEvent(events);

        if (sbEvent == null && odEvent == null)
            return true;
        else if (sbEvent != null && odEvent != null) {
//            return sbEvent.getEventSecondsValue() > odEvent.getEventSecondsValue();
            return sbEvent.EventSeconds > odEvent.EventSeconds;
        } else if (sbEvent != null)
            return true;

        return false;
    }

    private ArrayList<EldEvent> getEldEvents(String username, String dateStr) {
        lock.lock();

        try {
            if (!db.exists("SELECT * FROM eldevents WHERE eldUsername='" + username + "' AND creationdate>='" + dateStr + "'"))
                return null;

            ArrayList<EldEvent> result = new ArrayList();

            Cursor c = db.getRow(
                    "SELECT " +
                            "id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, recordOrigin, " +
                            "truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                            "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, " +
                            "malfunctionIndicator, annotation, recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, " +
                            "malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                            "editReason, eventseconds, shiftstart, creationdate, odometer " +
                            "FROM " +
                            "eldevents " +
                            "WHERE " +
                            "eldUsername='" + username + "' AND creationdate>='" + dateStr + "' " +
                            "ORDER BY " +
                            "id ASC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                EldEvent e = new EldEvent();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);

                result.add(e);
                c.moveToNext();
                col = 0;
            }

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    private ArrayList<EldEvent> getEldEvents(String username, String dateStr, String eventType) {
        lock.lock();

        try {
            if (!db.exists("SELECT * FROM eldevents WHERE eldUsername='" + username + "' AND creationdate>='" + dateStr + "'"))
                return null;

            ArrayList<EldEvent> result = new ArrayList();

            Cursor c = db.getRow(
                    "SELECT " +
                            "id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, recordOrigin, " +
                            "truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                            "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, " +
                            "malfunctionIndicator, annotation, recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, " +
                            "malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                            "editReason, eventseconds, shiftstart, creationdate, odometer " +
                            "FROM " +
                            "eldevents " +
                            "WHERE " +
                            "eldUsername='" + username + "' AND eventType='" + eventType + "' " + " AND creationdate>='" + dateStr + "' " +
                            "ORDER BY " +
                            "id ASC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                EldEvent e = new EldEvent();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);

                result.add(e);
                c.moveToNext();
                col = 0;
            }

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    private ArrayList<EldEvent> getAllDutyEventsSincePast14Hours() {
        lock.lock();

        try {
            String creationDateSincePast14Hours = DateUtils.getYyyyMmDdHhmmssStr(DateUtils.addHours(new Date(), -14));

            if (!db.exists("SELECT * FROM eldevents WHERE creationdate>='" + creationDateSincePast14Hours + "'"))
                return null;

            ArrayList<EldEvent> result = new ArrayList();

            Cursor c = db.getRow("SELECT id, organizationName, organizationNumber, eldUsername, eventType, " +
                    "eventCode, recordStatus, recordOrigin, truckNumber, vin, localizationDescription, latitudeString, " +
                    "longitudeString, dstSinceLastValidCoords, vehicleMiles, engineHours, orderNumbercmv, orderNumberUser, " +
                    "sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, recordOriginId, checkData, " +
                    "checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                    "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE creationdate>='" + creationDateSincePast14Hours + "' ORDER BY id DESC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                EldEvent e = new EldEvent();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);

                result.add(e);
                c.moveToNext();
                col = 0;
            }

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    private ArrayList<EldEvent> getAllDutyEventsSincePast48Hours() {
        lock.lock();

        try {
            String creationDateSincePast48Hours = DateUtils.getYyyyMmDdHhmmssStr(DateUtils.addHours(new Date(), -48));

            if (!db.exists("SELECT * FROM eldevents WHERE eldUsername='" + username + "' AND creationdate>='" + creationDateSincePast48Hours + "'"))
                return null;

            ArrayList<EldEvent> result = new ArrayList();

            Cursor c = db.getRow(
                    "SELECT " +
                            "id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, recordOrigin, " +
                            "truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                            "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, " +
                            "malfunctionIndicator, annotation, recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, " +
                            "malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                            "editReason, eventseconds, shiftstart, creationdate, odometer " +
                            "FROM " +
                            "eldevents " +
                            "WHERE " +
                            "eldUsername='" + username + "' AND creationdate>='" + creationDateSincePast48Hours + "' " +
                            "ORDER BY " +
                            "id DESC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                EldEvent e = new EldEvent();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);

                result.add(e);

                if (StringUtils.equalsIgnoreCaseAny(e.ShiftStart, new String[]{"1", "true"}))
                    break;

                c.moveToNext();
                col = 0;
            }

            c.close();

            Collections.reverse(result);
            return result;
        } finally {
            lock.unlock();
        }
    }

    private EldEvent findMostRecentSleeperBerthEvent(ArrayList<EldEvent> events) {
        if (events == null)
            return null;

        EldEvent result = null;

        for (int i = 0; i < events.size(); i++) {
            EldEvent e = events.get(i);

            if (StringUtils.equalsIgnoreCase(e.EventType, "1") && StringUtils.equalsIgnoreCase(e.EventCode, "2")) {
                if (result == null || e.getIdAsLong() > result.getIdAsLong())
                    result = e;
            }
        }

        return result;
    }

    private EldEvent findMostRecentOnDutyStartShiftEvent(ArrayList<EldEvent> events) {
        if (events == null)
            return null;

        EldEvent result = null;

        for (int i = 0; i < events.size(); i++) {
            EldEvent e = events.get(i);

            if (StringUtils.equalsIgnoreCase(e.EventType, "1") && StringUtils.equalsIgnoreCase(e.EventCode, "4")) {
                if (result == null || e.getIdAsLong() > result.getIdAsLong())
                    result = e;
            }
        }

        return result;
    }

    //endregion

    //region CheckSum and CheckData

    HashMap<String, Integer> checkSumMapppingDic;

    private Integer getCharactersToSum(String checkSumStr) {
        if (checkSumMapppingDic == null)
            checkSumMapppingDic = buildCharsCheckSumMappingDictionary();

        Integer sum = 0;

        for (int i = 0; i < checkSumStr.length(); i++) {
            String chStr = checkSumStr.substring(i, i + 1);
            int c = checkSumMapppingDic.containsKey(chStr) ? checkSumMapppingDic.get(chStr) : 0;
            int delta = c;

            if (delta > 0)
                sum += delta;
            /*else
                Log.d(TAG, ">>> computeCheckSum: " + delta);*/
        }

        return sum;
    }

    private String eventDataCheckSumStr(EventType eventType, EventCode eventCode, String
            eventDate, String eventTime, String vehicleMiles, String engineHours,
                                        String eventLatitude, String eventLongitude, String cmvNumber, String eldUsername) {
        return Integer.toString(eventDataCheckSum(eventType, eventCode, eventDate, eventTime, vehicleMiles,
                engineHours, eventLatitude, eventLongitude, cmvNumber, eldUsername));
    }

    private Integer eventDataCheckSum(EventType eventType, EventCode eventCode, String
            eventDate, String eventTime, String vehicleMiles, String engineHours,
                                      String eventLatitude, String eventLongitude, String cmvNumber, String eldUsername) {
        eventTime = eventTime != null ? eventTime.replace(":", "") : null;
        vehicleMiles = vehicleMiles != null ? vehicleMiles.replace(",", ".") : null;
        engineHours = engineHours != null ? engineHours.replace(",", ".") : null;
        eventLatitude = eventLatitude != null ? eventLatitude.replace(",", ".") : null;
        eventLongitude = eventLongitude != null ? eventLongitude.replace(",", ".") : null;

        ArrayList<String> fields = ArrayUtils.toArray(Integer.toString(eventType.value), eventCode.value,
                eventDate, eventTime, vehicleMiles, engineHours, eventLatitude, eventLongitude, cmvNumber, eldUsername);

        Integer sum = 0;

        for (String field : fields)
            if (field != null)
                sum += getCharactersToSum(field);

        return sum;

        /* -(NSNumber*)eventDataCheckSum {
            NSArray * fields = [NSArray arrayWithObjects:
            EventType, EventCode, EventDate, EventTime, VehicleMilesAcumulated, EngineHoursElapsed, EventLatitude, EventLongitude, EventCMVNumber, ELDUsername, nil]
            ;

            NSInteger sum = 0;

            for (NSString * field in fields){
                id value = [self valueForKey:field];
                NSString * valueString = nil;

                if ([value isKindOfClass:[NSString class]]){
                    if ([field isEqualToString:EventTime]){
                        // we need to replace â€œ:â€ with â€œâ€ (empty string)
                        valueString = [NSString stringWithFormat:@ "%@", [self timeFormatted]];
                    } else{
                        valueString = (NSString *) value;
                    }
                } else if ([value isKindOfClass:[NSNumber class]]){
                    valueString = [NSString stringWithFormat:@ "%@", value];
                } else if ([value isKindOfClass:[NSDate class]]){
                    NSDate * date = (NSDate *) value;
                    if ([field isEqualToString:EventDate]){
                        // date format is MMddyy
                        valueString = [self dateFormatted];
                    } else{
                        valueString = [date rcoDateRMSToString:date];
                    }
                }

                NSNumber * sumField = [NSNumber numberWithInteger:0];

                if (valueString) {
                    sumField = [ELDEvent getCharactersToSum:valueString];
                }
                sum += [sumField integerValue];
            }

            return [NSNumber numberWithInteger:sum];
        } */
    }

    private String lineDataCheck(String checkSumStr) {
        return lineDataCheck(getCharactersToSum(checkSumStr));
    }

    private String lineDataCheck(Integer lineDataSum) {
        int bit8 = lineDataSum & 0xFF;

        bit8 = circularShift(bit8);
        bit8 = circularShift(bit8);
        bit8 = circularShift(bit8);

        bit8 = bit8 ^ 0x96;
        bit8 = bit8 & 0xFF;

        return Integer.toString(bit8); // stringWithFormat:@"%02X" ??
    }

    private String eventDataCheckStr(Integer eventDataSum) {
        return Integer.toString(eventDataCheck(eventDataSum));
    }

    private Integer eventDataCheck(Integer eventDataSum) {
        Integer bit8 = eventDataSum & 0xFF;

        bit8 = circularShift(bit8);
        bit8 = circularShift(bit8);
        bit8 = circularShift(bit8);

        bit8 = bit8 ^ 0xc3;
        bit8 = bit8 & 0xFF;

        return bit8;
    }

    private Integer fileDataCheck(Integer fileDataSum) {
        Integer bit81 = fileDataSum & 0xFF00;
        Integer bit82 = fileDataSum & 0x00FF;
        bit81 = bit81 >> 8;


        bit81 = circularShift(bit81);
        bit81 = circularShift(bit81);
        bit81 = circularShift(bit81);

        bit82 = circularShift(bit82);
        bit82 = circularShift(bit82);
        bit82 = circularShift(bit82);

        bit81 = bit81 << 8;
        bit81 = bit81 | bit82;

        bit81 = bit81 ^ 0x969c;

        Integer bit16 = bit81 & 0xFFFF;

        return bit16;
    }

    private int circularShift(int value) {
        int bit8 = value & 0xFF;
        int carrier = bit8 & 0x80;
        carrier = carrier >> 7;

        bit8 = bit8 << 1;
        bit8 = bit8 | carrier;
        bit8 = bit8 & 0xFF;

        //Log.d(TAG, ">>Circular shift: " + bit8);

        return bit8;
    }

    private HashMap<String, Integer> buildCharsCheckSumMappingDictionary() {
        HashMap<String, Integer> result = new HashMap();

        result.put("1", 1);
        result.put("2", 2);
        result.put("3", 3);
        result.put("4", 4);
        result.put("5", 5);
        result.put("6", 6);
        result.put("7", 7);
        result.put("8", 8);
        result.put("9", 9);
        result.put("A", 17);
        result.put("B", 18);
        result.put("C", 19);
        result.put("D", 20);
        result.put("E", 21);
        result.put("F", 22);
        result.put("G", 23);
        result.put("H", 24);
        result.put("I", 25);
        result.put("J", 26);
        result.put("K", 27);
        result.put("L", 28);
        result.put("M", 29);
        result.put("N", 30);
        result.put("O", 31);
        result.put("P", 32);
        result.put("Q", 33);
        result.put("R", 34);
        result.put("S", 35);
        result.put("T", 36);
        result.put("U", 37);
        result.put("V", 38);
        result.put("W", 39);
        result.put("X", 40);
        result.put("Y", 41);
        result.put("Z", 42);
        result.put("a", 49);
        result.put("b", 50);
        result.put("c", 51);
        result.put("d", 52);
        result.put("e", 53);
        result.put("f", 54);
        result.put("g", 55);
        result.put("h", 56);
        result.put("i", 57);
        result.put("j", 58);
        result.put("k", 59);
        result.put("l", 60);
        result.put("m", 61);
        result.put("n", 62);
        result.put("o", 63);
        result.put("p", 64);
        result.put("q", 65);
        result.put("r", 66);
        result.put("s", 67);
        result.put("t", 68);
        result.put("u", 69);
        result.put("v", 70);
        result.put("w", 71);
        result.put("x", 72);
        result.put("y", 73);
        result.put("z", 74);

        return result;
    }

    //endregion

    //region Malfunction & Diagnostic

    private long MAX_POSITION_DISTANCE = 6;

    private boolean diagnosticIndicator = false;
    private boolean malfunctionIndicator = false;

    private String lastEngineSynchronizationFailureDescription;

    private Long previousDistance;
    private Double previousLat, previousLon;

    public boolean isUnableToReadBleParameter(String key) {
        try {

            String value = getBleParameter(key);
//            Log.d(TAG, "isUnableToReadBleParameter: value: " + value + " For key " + key);
            return StringUtils.isNullOrWhitespaces(value) || value.equalsIgnoreCase("null");
        } catch (Throwable throwable) {
            Log.d(TAG, "isUnableToReadBleParameter: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();

            return true;
        }
    }

    public boolean isPositionFailing(Activity a) {
        if (StringUtils.isNullOrWhitespaces(odometer) || odometer.equalsIgnoreCase("null"))
            return false;

        Long currentDistance = Long.parseLong(odometer);

        if (previousDistance == null) {
            previousDistance = Long.parseLong(odometer);
            return false;
        }

        Location l = getPhoneLastBestLocation(a);

        if (l == null)
            return false;

        Double currentLat = l.getLatitude();
        Double currentLon = l.getLongitude();

        if (currentLat == null || currentLon == null)
            return false;

        if (previousLat == null || previousLon == null) {
            previousLat = currentLat;
            previousLon = currentLon;
            return false;
        }

        Double distance = (currentDistance - previousDistance) / 1000 * 0.621371192; // In miles

        if (distance > MAX_POSITION_DISTANCE) {
            Double deltaLat = currentLat - previousLat;
            Double deltaLon = currentLon - previousLon;

            if (deltaLat > 0 || deltaLon > 0) {
                previousLat = currentLat;
                previousLon = currentLon;
                saveLastValidLatLonDistance(previousLat, previousLon, (double) MAX_POSITION_DISTANCE);
                return false;
            }
        } else
            saveLastValidLatLonDistance(previousLat, previousLon, 0d);

        previousDistance = currentDistance;
        return false;
    }

    public boolean existsActiveDiagnostic() {

//        April 13, 2022    -   We should check the diagostic not the malfunction
//        if (isLastEldEventMalfunctionLogged(EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION)){
//            Log.d(TAG, "MD: existsActiveDiagnostic: DIAGNOSTIC_ENGINE_SYNCHRONIZATION: ");
//            return true;
//        }
        if (isLastEldEventDiagnosticLogged(EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION))
            return true;

        if (isLastEldEventDiagnosticLogged(EventCode.DIAGNOSTIC_POWER_DATA_DIAGNOSTIC)) {
            return true;
        }

        if (isLastEldEventDiagnosticLogged(EventCode.DIAGNOSTIC_DATA_TRANSFER)) {
            return true;
        }

//        April 13, 2022    -   Adding Missing Diagnostics
        if (isLastEldEventDiagnosticLogged(EventCode.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS)) {
            return true;
        }

        if (isLastEldEventDiagnosticLogged(EventCode.DIAGNOSTIC_UNIDENTIFIED_DRIVING_RECORDS)) {
            return true;
        }


        return false;
    }

    public boolean existsActiveMalfunction() {
        if (isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE)) {
            Log.d(TAG, "bleConnection: existsActiveMalfunction: MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE: ");
            return true;
        }

        if (isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_POWER_COMPLIANCE)) {
            Log.d(TAG, "bleConnection: existsActiveMalfunction: MALFUNCTION_POWER_COMPLIANCE: ");
            return true;
        }

        if (isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_DATA_TANSFER_COMPLIANCE)) {
            Log.d(TAG, "bleConnection: existsActiveMalfunction: MALFUNCTION_DATA_TANSFER_COMPLIANCE: ");
            return true;
        }

        if (isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_POSITION_COMPLIANCE)) {
            Log.d(TAG, "bleConnection: existsActiveMalfunction: MALFUNCTION_POSITION_COMPLIANCE: ");
            return true;
        }

//        April 13, 2022    -   Added missing Malfunctions
        if (isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_TIMING_COMPLIANCE)) {
            Log.d(TAG, "bleConnection: existsActiveMalfunction: MALFUNCTION_TIMING_COMPLIANCE: ");
            return true;
        }

        if (isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_DATA_RECORDING_COMPLIANCE)) {
            Log.d(TAG, "bleConnection: existsActiveMalfunction: MALFUNCTION_DATA_RECORDING_COMPLIANCE: ");
            return true;
        }

        if (isLastEldEventMalfunctionLogged(EventCode.MALFUNCTION_OTHER)) {
            Log.d(TAG, "bleConnection: existsActiveMalfunction: MALFUNCTION_OTHER: ");
            return true;
        }

        return false;
    }

    public void saveLastValidLatLonDistance(Double lat, Double lon, Double distance) {
        setSetting("lastvaliddrivercoords.lat", lat.toString());
        setSetting("lastvaliddrivercoords.lon", lon.toString());
        setSetting("lastvaliddrivercoords.distance", distance.toString());
    }

    public boolean existsSavedLastValidDriverCoords() {
        return getSetting("lastvaliddrivercoords.lat") == null || getSetting("lastvaliddrivercoords.lon") == null;
    }

    public void loadLastValidLatLonDistance(Activity a) {
        Double[] lastValidLatLonDistance = getLastValidLatLonDistance();

        if (lastValidLatLonDistance == null) {
            Location l = getPhoneLastBestLocation(a);

            if (l == null)
                try {
                    Double lat = l.getLatitude();
                    Double lon = l.getLongitude();
                    Double odometer = Double.parseDouble(getBleParameter("Odometer"));

                    saveLastValidLatLonDistance(lat, lon, odometer);
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();

                    return;
                }
        }

        lastValidLatLonDistance = getLastValidLatLonDistance();

        if (lastValidLatLonDistance == null)
            return;

        if (lastValidLatLonDistance[0] != null)
            previousLat = lastValidLatLonDistance[0];

        if (lastValidLatLonDistance[1] != null)
            previousLon = lastValidLatLonDistance[1];

        if (lastValidLatLonDistance[2] != null)
            previousDistance = (long) ((double) lastValidLatLonDistance[2]);

//        Log.d(TAG, "loadLastValidLatLonDistance: previousDistance: " + previousDistance);
    }

    private Double[] getLastValidLatLonDistance() {
        try {
            String latStr = getSetting("lastvaliddrivercoords.lat");
            String lonStr = getSetting("lastvaliddrivercoords.lon");
            String distanceStr = getSetting("lastvaliddrivercoords.distance");

            if (latStr == null || lonStr == null || distanceStr == null)
                return null;

            Double[] result = new Double[3];

            result[0] = Double.parseDouble(latStr);
            result[1] = Double.parseDouble(lonStr);
            result[2] = Double.parseDouble(distanceStr);

            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return null;
    }

    public boolean isDataTransferFailing() {
        // TODO: After making the call to RMS to transfer RODS to FMCSA using emailELD (or the call to use the web service functionality)
        // we are checking the response to know if it failed or was successfully. To be complete once LogBook it's implemented.

        // If transfer failed because of NO INTERNET CONNECTION then return false
        // If transfer failed because of timeout or server/svc error then return true

        return false;
    }

    public boolean isDiagnosticFailingForLongerThanMins(int mins, EventCode
            malfunctionDiagnosticCode) {
        String sqlString = "SELECT eventCode, creationdate FROM eldevents WHERE eventType='7' AND malfunctionDiagnosticCode='" +
                malfunctionDiagnosticCode.getValue() + "' ORDER BY id DESC LIMIT 1";

        boolean result = false;
        lock.lock();
        try {

//            April 15, 2022    -   We should not return false if their is no
//            if (!db.exists(sqlString))
//                return false;

            Cursor c = db.getRow(sqlString);

            if (c == null)
                return false;

            c.moveToFirst();
//            Log.d(TAG, "MD: isDiagnosticFailingForLongerThanMins: cursor: count: " + c.getCount());
            if (!c.isAfterLast()) {
                String eventCode = c.getString(0);
//                Log.d(TAG, "MD: isDiagnosticFailingForLongerThanMins: c.getLong: " + c.getLong(1));
//                Log.d(TAG, "MD: isDiagnosticFailingForLongerThanMins: c.getString: " + c.getString(1));
//                Date creationDate = DateFormat.parse(c.getString(1));
                Date creationDate = DateUtils.getDateDiff(c.getString(1));
//                Log.d(TAG, "MD: isDiagnosticFailingForLongerThanMins: creationDate: " + creationDate);
                Calendar cal = Calendar.getInstance();
                cal.add(Calendar.MINUTE, -mins);
                Date nowMinusMinutesDate = cal.getTime();

//                Log.d(TAG, "MD: isDiagnosticFailingForLongerThanMins: eventCode: " + eventCode + " creationDate: " + creationDate + " nowMinusMinutesDate: " + nowMinusMinutesDate);
                if (!StringUtils.isNullOrWhitespaces(eventCode) && eventCode.trim().equalsIgnoreCase(EventCode.DIAGNOSTIC_EVENT_LOGGED.value)) {
//                    Log.d(TAG, "MD: isDiagnosticFailingForLongerThanMins: nowMinusMinutesDate.after(creationDate): " + nowMinusMinutesDate.after(creationDate));
                    if (nowMinusMinutesDate.after(creationDate)) {
//                        Log.d(TAG, "MD: isDiagnosticFailingForLongerThanMins: return true as time difference is 30 mints");
                        result = true;
                    }
                }
            }

            c.close();
        } finally {
            lock.unlock();
        }

        return result;
    }


    public boolean isLastEldEventMalfunctionLogged(BusinessRules.EventCode eventCode) {
        return isLastEldEventLoggedCleared(true, true, eventCode);
    }

    public boolean isLastEldEventMalfunctionCleared(BusinessRules.EventCode eventCode) {
        return isLastEldEventLoggedCleared(false, true, eventCode);
    }

    public boolean isLastEldEventDiagnosticLogged(BusinessRules.EventCode eventCode) {
        return isLastEldEventLoggedCleared(true, false, eventCode);
    }

    public boolean isLastEldEventDiagnosticCleared(BusinessRules.EventCode eventCode) {
        return isLastEldEventLoggedCleared(false, false, eventCode);
    }

    public boolean isLastEldEventLoggedCleared(boolean isLoggedEvent,
                                               boolean isMalfunction, BusinessRules.EventCode malfunctionDiagnosticCode) {
        Log.d(TAG, "MD: isLastEldEventLoggedCleared: ");
        lock.lock();

        try {
            String eventCode;

            Log.d(TAG, "MD: isLastEldEventLoggedCleared: isLoggedEvent: " + isLoggedEvent +
                    " isMalfunction: " + isMalfunction);
            if (isMalfunction)
                eventCode = isLoggedEvent ? EventCode.ELD_MALFUNCTION_LOGGED.value : EventCode.ELD_MALFUNCTION_CLEARED.value;
            else
                eventCode = isLoggedEvent ? EventCode.DIAGNOSTIC_EVENT_LOGGED.value : EventCode.DIAGNOSTIC_EVENT_CLEARED.value;

            String sqlString = "SELECT eventCode, mobileRecordId FROM eldevents WHERE eventType='7' AND malfunctionDiagnosticCode='" +
                    malfunctionDiagnosticCode.getValue() + "' AND eldUsername='" + username + "' ORDER BY eventSeconds DESC LIMIT 1";
//                    malfunctionDiagnosticCode.getValue() + "' AND eldUsername='" + username + "' ORDER BY id DESC LIMIT 1";
            Log.d(TAG, "MD: isLastEldEventLoggedCleared: sqlString: " + sqlString);


//            April 14, 2022    -   We don't need this anymore- its breaking the logic
//            Log.d(TAG, "MD: isLastEldEventLoggedCleared: db.exists(sqlString): "+(db.exists(sqlString)));
//            if (!db.exists(sqlString)) {
//                Log.d(TAG, "MD: isLastEldEventLoggedCleared: dont exists so return false");
//                return false;
//            }

            Cursor cursor = db.getRow(sqlString);

//            April 14, 2022    -
//            if (c == null)
//                return false;

            Log.d(TAG, "MD: isLastEldEventLoggedCleared: " + malfunctionDiagnosticCode.getValue() + " cursor: " + cursor);
            if (cursor == null || (cursor.getCount() == 0)) {
                Log.d(TAG, "MD: isLastEldEventLoggedCleared: isLoggedEvent: " + isLoggedEvent);
//            April 14, 2022    -
                if (!isLoggedEvent) {
//                    This is first time we are searching "is logged event" so if their no previous logged event then
//                    we should simulate that the last event is cleared
                    Log.d(TAG, "MD: isLastEldEventLoggedCleared: return true 1");
                    return true;
                } else {
//                    This should never happened
                    Log.d(TAG, "MD: isLastEldEventLoggedCleared: return false 1");
                    return false;
                }
            }

            Log.d(TAG, "MD: isLastEldEventLoggedCleared: cursor: " + cursor.getCount());
            cursor.moveToFirst();

//            June 09, 2022 -   We only have one value as Limit 1 so we don't need isAfterLast check
//            Actually their is a strange situation that we are recording clear after clear diagnostic
//            if (!cursor.isAfterLast()) {
            String eventCodeStr = cursor.getString(0);
            String mobileRecordId = cursor.getString(1);
//                String mobileRecordId = cursor.getString(1);

//                + " mobileRecordId: " + mobileRecordId
            Log.d(TAG, "MD: isLastEldEventLoggedCleared: eventCode: " + eventCode + " eventCodeString: " + eventCodeStr + " mobileRecordId: " + mobileRecordId);
            if (!StringUtils.isNullOrWhitespaces(eventCodeStr) && eventCodeStr.trim().equalsIgnoreCase(eventCode)) {
                Log.d(TAG, "MD: isLastEldEventLoggedCleared: returning true 2");
                return true;
            }
//            }

            cursor.close();
        } finally {
            Log.d(TAG, "MD: isLastEldEventLoggedCleared: finally block: ");
            lock.unlock();
        }
        Log.d(TAG, "MD: isLastEldEventLoggedCleared: returning false 2");
        return false;
    }

    public boolean isTimingComplianceFailing() {
        try {
            Long ntpTime = DateUtils.getNtpTime();
            Long nowTicks = (new Date()).getTime();

            Long delta = ntpTime - nowTicks < 0 ? nowTicks - ntpTime : ntpTime - nowTicks;
            Long deltaMins = delta / 1000 / 60;
//            Log.d(TAG, "MD: isTimingComplianceFailing: deltaMins: " + deltaMins + " ntpTime: " + ntpTime +
//                    " nowTicks: " + nowTicks);

            return deltaMins >= 10;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return false;
    }

    //    April 15, 2022    -   We need to get the parameters that are failing
    public String getEngineSynchronizationDiagnosticAnnotation() {
//        Log.d(TAG, "MD: getEngineSynchronizationDiagnosticAnnotation: ");

        boolean isUnableToReadVin = isUnableToReadBleParameter("VIN");
        boolean isUnableToReadEngineHours = isUnableToReadBleParameter("EngineHours");
        boolean isUnableToReadTripHours = isUnableToReadBleParameter("TripHours");
        boolean isUnableToReadOdometer = isUnableToReadBleParameter("Odometer");
        boolean isUnableToReadTripDistance = isUnableToReadBleParameter("TripDistance");
        boolean isUnableToReadEngineSpeed = isUnableToReadBleParameter("RPM");

        String annotation = "";
        if (isUnableToReadVin) {
            annotation = "VIN";
        }
        if (isUnableToReadEngineHours) {
            annotation = annotation + " - EngineHours";
        }
        if (isUnableToReadTripHours) {
            annotation = annotation + " - TripHours";
        }
        if (isUnableToReadOdometer) {
            annotation = annotation + " - Odometer";
        }
        if (isUnableToReadTripDistance) {
            annotation = annotation + " - TripDistance";
        }
        if (isUnableToReadEngineSpeed) {
            annotation = annotation + " - EngineSpeed";
        }

        return annotation;
    }

    public boolean isEngineSynchronizationDiagnosticFailing() {

//        return false;
        boolean isUnableToReadVin = isUnableToReadBleParameter("VIN");
        boolean isUnableToReadEngineHours = isUnableToReadBleParameter("EngineHours");
        boolean isUnableToReadTripHours = isUnableToReadBleParameter("TripHours");
        boolean isUnableToReadOdometer = isUnableToReadBleParameter("Odometer");
        boolean isUnableToReadTripDistance = isUnableToReadBleParameter("TripDistance");
        boolean isUnableToReadEngineSpeed = isUnableToReadBleParameter("RPM");
//        Log.d(TAG, "MD: isEngineSynchronizationDiagnosticFailing: isUnableToReadVin: " + isUnableToReadVin);
//        Log.d(TAG, "MD: isEngineSynchronizationDiagnosticFailing: getBleParameter: " + getBleParameter("VIN"));
//        Log.d(TAG, "MD: isEngineSynchronizationDiagnosticFailing: isUnableToReadEngineHours: " + isUnableToReadEngineHours);
//        Log.d(TAG, "MD: isEngineSynchronizationDiagnosticFailing: isUnableToReadTripHours: " + isUnableToReadTripHours);
//        Log.d(TAG, "MD: isEngineSynchronizationDiagnosticFailing: isUnableToReadOdometer: " + isUnableToReadOdometer);
//        Log.d(TAG, "MD: isEngineSynchronizationDiagnosticFailing: isUnableToReadTripDistance: " + isUnableToReadTripDistance);
//        Log.d(TAG, "MD: isEngineSynchronizationDiagnosticFailing: isUnableToReadEngineSpeed: " + isUnableToReadEngineSpeed);

        if (isUnableToReadVin)
            lastEngineSynchronizationFailureDescription = "VIN";

        if (isUnableToReadEngineHours)
            lastEngineSynchronizationFailureDescription = "EngineHours";

        if (isUnableToReadTripHours)
            lastEngineSynchronizationFailureDescription = "TripHours";

        if (isUnableToReadOdometer)
            lastEngineSynchronizationFailureDescription = "Odometer";

        if (isUnableToReadTripDistance)
            lastEngineSynchronizationFailureDescription = "TripDistance";

        if (isUnableToReadEngineSpeed)
            lastEngineSynchronizationFailureDescription = "RPM";

        return isUnableToReadVin || isUnableToReadEngineHours || isUnableToReadTripHours ||
                isUnableToReadOdometer || isUnableToReadTripDistance || isUnableToReadEngineSpeed;
    }

    public ArrayList<EldEvent> getLatestMalfunctions(String searchText) {
        ArrayList<EldEvent> events = getLatestMalfunctions();

        if (events == null || events.size() == 0)
            return null;

        if (TextUtils.isNullOrWhitespaces(searchText))
            return events;

        ArrayList<EldEvent> result = new ArrayList();

        for (EldEvent a : events)
            if (isDiagnosticSearchMatch(a, searchText))
                result.add(a);

        return result;
    }

    public ArrayList<EldEvent> getLatestMalfunctions() {
//
        return getDiagnosticAndMalFunctionForDays(1, false);
//        ArrayList<EldEvent> result = new ArrayList();
//
//        EldEvent e = getLastEldEventEntry(EventCode.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE);
//
//        if (e != null)
//            result.add(e);
//
//        e = getLastEldEventEntry(EventCode.MALFUNCTION_DATA_TANSFER_COMPLIANCE);
//
//        if (e != null)
//            result.add(e);
//
//        e = getLastEldEventEntry(EventCode.MALFUNCTION_POWER_COMPLIANCE);
//
//        if (e != null)
//            result.add(e);
//
//        e = getLastEldEventEntry(EventCode.MALFUNCTION_POSITION_COMPLIANCE);
//
//        if (e != null)
//            result.add(e);
//
//
//        e = getLastEldEventEntry(EventCode.MALFUNCTION_TIMING_COMPLIANCE);
//
//        if (e != null)
//            result.add(e);
//
//
//        e = getLastEldEventEntry(EventCode.MALFUNCTION_DATA_RECORDING_COMPLIANCE);
//
//        if (e != null)
//            result.add(e);
//
//
//        e = getLastEldEventEntry(EventCode.MALFUNCTION_OTHER);
//
//        if (e != null)
//            result.add(e);
//
//        return result;
    }

    public ArrayList<EldEvent> getLatestDiagnostics(String searchText) {
        ArrayList<EldEvent> events = getLatestDiagnostics();

        if (events == null || events.size() == 0)
            return null;

        if (TextUtils.isNullOrWhitespaces(searchText))
            return events;

        ArrayList<EldEvent> result = new ArrayList();

        for (EldEvent a : events)
            if (isDiagnosticSearchMatch(a, searchText))
                result.add(a);

        return result;
    }

    public static boolean isDiagnosticSearchMatch(EldEvent e, String searchText) {
        if (TextUtils.isNullOrWhitespaces(searchText))
            return true;

        searchText = TextUtils.toLowerCase(searchText);
        Vector<String> searchFields = new Vector();

        searchFields.add(e.EventCode);
        searchFields.add(e.DriverFirstName);
        searchFields.add(e.DriverLastName);
        searchFields.add(e.EventCodeDescription);

        for (String searchField : searchFields)
            if (TextUtils.toLowerCase(searchField).indexOf(searchText) != -1)
                return true;

        return false;
    }

    public boolean existsLatestDiagnostics() {
        ArrayList<EldEvent> result = getLatestDiagnostics();
        return result != null && result.size() > 0;
    }

    public boolean existsLatestMalfunctions() {
        ArrayList<EldEvent> result = getLatestMalfunctions();
        return result != null && result.size() > 0;
    }

    public ArrayList<EldEvent> getLatestDiagnostics() {
//        April 15, 2022    -    we should return all the diagnostics for th last x days
        return getDiagnosticAndMalFunctionForDays(1, true);
/*        ArrayList<EldEvent> result = new ArrayList();

        EldEvent e = getLastEldEventEntry(EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_DATA_TRANSFER);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_POWER_DATA_DIAGNOSTIC);

        if (e != null)
            result.add(e);

//        This should not be here right?
//        e = getLastEldEventEntry(EventCode.MALFUNCTION_POSITION_COMPLIANCE);
//
//        if (e != null)
//            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_UNIDENTIFIED_DRIVING_RECORDS);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_OTHER);

        if (e != null)
            result.add(e);

        return result;*/
    }

    public ArrayList<EldEvent> getLatestDiagnosticsActive() {
        ArrayList<EldEvent> result = new ArrayList();

        EldEvent e = getLastEldEventEntry(EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_DATA_TRANSFER);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_POWER_DATA_DIAGNOSTIC);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_UNIDENTIFIED_DRIVING_RECORDS);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.DIAGNOSTIC_OTHER);

        if (e != null)
            result.add(e);

        return result;
    }

    public ArrayList<EldEvent> getLatestMalfunctionActive() {
        ArrayList<EldEvent> result = new ArrayList();

        EldEvent e = getLastEldEventEntry(EventCode.MALFUNCTION_POWER_COMPLIANCE);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.MALFUNCTION_TIMING_COMPLIANCE);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.MALFUNCTION_POSITION_COMPLIANCE);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.MALFUNCTION_DATA_RECORDING_COMPLIANCE);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.MALFUNCTION_DATA_TANSFER_COMPLIANCE);

        if (e != null)
            result.add(e);

        e = getLastEldEventEntry(EventCode.MALFUNCTION_OTHER);

        if (e != null)
            result.add(e);

        return result;
    }


    public String getLastEngineSynchronizationFailureDescription() {
        return lastEngineSynchronizationFailureDescription;
    }

    public void recordDiagnosticOrMalfunctionEvent(Activity a, EventCode eventCode, EventCode
            malDiagnosticEventCode, String malDiagnosticEventCodeDescription, Boolean
                                                           IsManuallyRecordedELDEvent) {
        recordDiagnosticOrMalfunctionEvent(a, eventCode, malDiagnosticEventCodeDescription, malDiagnosticEventCodeDescription,
                malDiagnosticEventCode.getValue(), malDiagnosticEventCodeDescription, IsManuallyRecordedELDEvent);
    }

    public void recordDiagnosticOrMalfunctionEvent(Activity a, EventCode eventCode, String
            eventCodeDescription, String annotation, String malfunctionDiagnosticCode, String
                                                           malfunctionDiagnosticDescp, Boolean IsManuallyRecordedELDEvent) {
//        Log.d(TAG, "MD: recordDiagnosticOrMalfunctionEvent: eventCodeDescription: " + eventCodeDescription);

//        April 14, 2022    -   We should add more information for debugging malfunction and diagnostic
        String eventCodeDescriptionFormatted = eventCodeDescription + "";
        if (eventCode == EventCode.DIAGNOSTIC_EVENT_CLEARED) {
            eventCodeDescriptionFormatted = eventCodeDescription + " " + "Cleared Diagnostic";
        } else if (eventCode == EventCode.DIAGNOSTIC_EVENT_LOGGED) {
            eventCodeDescriptionFormatted = eventCodeDescription + " " + "Logged Diagnostic";
        } else if (eventCode == EventCode.ELD_MALFUNCTION_CLEARED) {
            eventCodeDescriptionFormatted = eventCodeDescription + " " + "Cleared Malfunction";
        } else if (eventCode == EventCode.ELD_MALFUNCTION_LOGGED) {
            eventCodeDescriptionFormatted = eventCodeDescription + " " + "Logged Malfunction";
        }
//        Log.d(TAG, "MD: recordDiagnosticOrMalfunctionEvent: eventCodeDescriptionFormatted: " + eventCodeDescriptionFormatted);

//        April 18, 2022    -   We need to set the origin - entered by driver

        if (IsManuallyRecordedELDEvent) {
            setEldEvent(a,
                    EventType.A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE,
                    eventCode,
                    EventRecordStatus.ACTIVE,
                    EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER,
                    eventCodeDescriptionFormatted, annotation, malfunctionDiagnosticCode, malfunctionDiagnosticDescp);
        } else {
            setEldEvent(a,
                    EventType.A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE,
                    eventCode,
                    EventRecordStatus.ACTIVE,
                    EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD,
                    eventCodeDescriptionFormatted, annotation, malfunctionDiagnosticCode, malfunctionDiagnosticDescp);
        }
    }

    public void recordTimingComplianceMalfunction(Activity a) {
//        Log.d(TAG, "MD: recordTimingComplianceMalfunction: isTimingComplianceFailing: " + isTimingComplianceFailing());
        if (isTimingComplianceFailing()) {
//            April 14, 2022    -   We should search by Malfunction timing and not MalFunction Power
//            if (!isLastEldEventMalfunctionLogged(BusinessRules.EventCode.MALFUNCTION_POWER_COMPLIANCE))

//            Log.d(TAG, "MD: recordTimingComplianceMalfunction: isLastEldEventMalfunctionLogged: " + isLastEldEventMalfunctionLogged(BusinessRules.EventCode.MALFUNCTION_TIMING_COMPLIANCE));
            if (!isLastEldEventMalfunctionLogged(BusinessRules.EventCode.MALFUNCTION_TIMING_COMPLIANCE))
                recordDiagnosticOrMalfunctionEvent(a,
                        BusinessRules.EventCode.ELD_MALFUNCTION_LOGGED,
                        BusinessRules.EventCode.MALFUNCTION_TIMING_COMPLIANCE,
                        BusinessRules.EventCodeDescription.MALFUNCTION_TIMING_COMPLIANCE.getValue(),
                        false);

            return;
        }

//        Log.d(TAG, "MD: recordTimingComplianceMalfunction: if: MALFUNCTION_TIMING_COMPLIANCE: " + (isLastEldEventMalfunctionCleared(BusinessRules.EventCode.MALFUNCTION_TIMING_COMPLIANCE)));
        if (!isLastEldEventMalfunctionCleared(BusinessRules.EventCode.MALFUNCTION_TIMING_COMPLIANCE)) {
//            Log.d(TAG, "MD: recordTimingComplianceMalfunction: its not so record ELD_MALFUNCTION_CLEARED");
            recordDiagnosticOrMalfunctionEvent(a,
                    BusinessRules.EventCode.ELD_MALFUNCTION_CLEARED,
                    BusinessRules.EventCode.MALFUNCTION_TIMING_COMPLIANCE,
                    BusinessRules.EventCodeDescription.MALFUNCTION_TIMING_COMPLIANCE.getValue(),
                    false);
        }
    }

    public void recordPositionDiagnostic(Activity a) {
        if (isPositionFailing(a)) {
            if (!existsSavedLastValidDriverCoords())
                loadLastValidLatLonDistance(a);
            else {
//                April 12, 2022    -   We should use missing data elements for position diagostic
//                if (!isLastEldEventDiagnosticLogged(BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE))
//                    recordDiagnosticOrMalfunctionEvent(a,
//                            BusinessRules.EventCode.DIAGNOSTIC_EVENT_LOGGED,
//                            BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE,
//                            BusinessRules.EventCodeDescription.MALFUNCTION_POSITION_COMPLIANCE.getValue());
                if (!isLastEldEventDiagnosticLogged(EventCode.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS))
                    recordDiagnosticOrMalfunctionEvent(a,
                            BusinessRules.EventCode.DIAGNOSTIC_EVENT_LOGGED,
                            EventCode.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS,
                            EventCodeDescription.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS.getValue()
                                    + EventCodeDescription.DIAGNOSTIC_EVENT_LOGGED_DESCRIPTION.getValue(),
                            false);
            }

            return;
        }

//                April 12, 2022    -   We should use missing data elements for position diagostic
//        if (!isLastEldEventDiagnosticCleared(BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE))
//            recordDiagnosticOrMalfunctionEvent(a,
//                    BusinessRules.EventCode.DIAGNOSTIC_EVENT_CLEARED,
//                    BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE,
//                    BusinessRules.EventCodeDescription.MALFUNCTION_POSITION_COMPLIANCE.getValue());
        if (!isLastEldEventDiagnosticCleared(EventCode.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS))
            recordDiagnosticOrMalfunctionEvent(a,
                    BusinessRules.EventCode.DIAGNOSTIC_EVENT_CLEARED,
                    EventCode.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS,
                    EventCodeDescription.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS.getValue()
                            + EventCodeDescription.DIAGNOSTIC_EVENT_CLEARED_DESCRIPTION.getValue(),
                    false);

    }

    public void recordPositionMalfunction(Activity a) {
        if (isPositionFailing(a)) {
            if (isDiagnosticFailingForLongerThanMins(60, BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE))
                if (!isLastEldEventMalfunctionLogged(BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE))
                    recordDiagnosticOrMalfunctionEvent(a,
                            BusinessRules.EventCode.ELD_MALFUNCTION_LOGGED,
                            BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE,
                            BusinessRules.EventCodeDescription.MALFUNCTION_POSITION_COMPLIANCE.getValue(),
                            false);
        } else {
            if (!isLastEldEventMalfunctionCleared(BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE))
                recordDiagnosticOrMalfunctionEvent(a,
                        BusinessRules.EventCode.ELD_MALFUNCTION_CLEARED,
                        BusinessRules.EventCode.MALFUNCTION_POSITION_COMPLIANCE,
                        BusinessRules.EventCodeDescription.MALFUNCTION_POSITION_COMPLIANCE.getValue(),
                        false);
        }
    }

    public void recordEngineSynchronizationDiagnostic(Activity a) {
//        Log.d(TAG, "MD: recordEngineSynchronizationDiagnostic: isEngineSynchronizationDiagnosticFailing(): " + isEngineSynchronizationDiagnosticFailing());
        if (isEngineSynchronizationDiagnosticFailing()) {
//            Log.d(TAG, "MD: recordEngineSynchronizationDiagnostic: (!isLastEldEventDiagnosticLogged(BusinessRules.EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION)): " + (!isLastEldEventDiagnosticLogged(BusinessRules.EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION)));
            if (!isLastEldEventDiagnosticLogged(BusinessRules.EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION))
                recordDiagnosticOrMalfunctionEvent(a,
                        BusinessRules.EventCode.DIAGNOSTIC_EVENT_LOGGED,
                        BusinessRules.EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION,
                        BusinessRules.EventCodeDescription.DIAGNOSTIC_ENGINE_SYNCHRONIZATION.getValue(),
                        false);

//            April 13, 2022    -   Today temporary removed it
//            setEldEvent(a,
//                    BusinessRules.EventType.PARTIAL_A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE,
//                    BusinessRules.EventCode.IN_SERVICE_MONITOR_NOTOK,
//                    BusinessRules.EventRecordStatus.ACTIVE,
//                    BusinessRules.EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD,
//                    "Engine Synchronization Failure: " + getLastEngineSynchronizationFailureDescription(),
//                    BusinessRules.EventCodeDescription.DIAGNOSTIC_ENGINE_SYNCHRONIZATION.getValue(),
//                    "", "");

            return;
        }

        if (!isLastEldEventDiagnosticCleared(BusinessRules.EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION))
            recordDiagnosticOrMalfunctionEvent(a,
                    BusinessRules.EventCode.DIAGNOSTIC_EVENT_CLEARED,
                    BusinessRules.EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION,
                    BusinessRules.EventCodeDescription.DIAGNOSTIC_ENGINE_SYNCHRONIZATION.getValue(),
                    false);
    }

    public void recordEngineSynchronizationMalfunction(Activity a) {
        if (isEngineSynchronizationDiagnosticFailing()) {
            if (isDiagnosticFailingForLongerThanMins(60, BusinessRules.EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION))
                if (!isLastEldEventMalfunctionLogged(BusinessRules.EventCode.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE))
                    recordDiagnosticOrMalfunctionEvent(a,
                            BusinessRules.EventCode.ELD_MALFUNCTION_LOGGED,
                            BusinessRules.EventCode.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE,
                            BusinessRules.EventCodeDescription.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE.getValue(),
                            false);
        } else {
            if (!isLastEldEventMalfunctionCleared(BusinessRules.EventCode.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE))
                recordDiagnosticOrMalfunctionEvent(a,
                        BusinessRules.EventCode.ELD_MALFUNCTION_CLEARED,
                        BusinessRules.EventCode.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE,
                        BusinessRules.EventCodeDescription.MALFUNCTION_ENGINE_SYNCHRONIZATION_COMPLIANCE.getValue(),
                        false);
        }
    }

    public void recordDataTransferMalfunction(Activity a) {
        if (isDataTransferFailing()) {
            if (isDiagnosticFailingForLongerThanMins(BusinessRules.THREE_DAYS_IN_MINS, BusinessRules.EventCode.DIAGNOSTIC_DATA_TRANSFER))
                if (!isLastEldEventMalfunctionLogged(BusinessRules.EventCode.MALFUNCTION_DATA_TANSFER_COMPLIANCE))
                    recordDiagnosticOrMalfunctionEvent(a,
                            BusinessRules.EventCode.ELD_MALFUNCTION_LOGGED,
                            BusinessRules.EventCode.MALFUNCTION_DATA_TANSFER_COMPLIANCE,
                            BusinessRules.EventCodeDescription.MALFUNCTION_DATA_TANSFER_COMPLIANCE.getValue(),
                            false);
        } else {
            if (!isLastEldEventMalfunctionCleared(BusinessRules.EventCode.MALFUNCTION_DATA_TANSFER_COMPLIANCE))
                recordDiagnosticOrMalfunctionEvent(a,
                        BusinessRules.EventCode.ELD_MALFUNCTION_CLEARED,
                        BusinessRules.EventCode.MALFUNCTION_DATA_TANSFER_COMPLIANCE,
                        BusinessRules.EventCodeDescription.MALFUNCTION_DATA_TANSFER_COMPLIANCE.getValue(),
                        false);
        }
    }

    public void recordDataTransferDiagnostic(Activity a) {
        if (isDataTransferFailing()) {
            setEldEvent(a,
                    BusinessRules.EventType.IN_SERVICE_MONITOR,
                    BusinessRules.EventCode.IN_SERVICE_MONITOR_NOTOK,
                    BusinessRules.EventRecordStatus.ACTIVE,
                    BusinessRules.EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD,
                    "Data Transfer Not Completed Successfully",
                    BusinessRules.EventCodeDescription.DIAGNOSTIC_DATA_TRANSFER.getValue(),
                    "", "");

            if (!isLastEldEventDiagnosticLogged(BusinessRules.EventCode.DIAGNOSTIC_DATA_TRANSFER))
                recordDiagnosticOrMalfunctionEvent(a,
                        BusinessRules.EventCode.DIAGNOSTIC_EVENT_LOGGED,
                        BusinessRules.EventCode.DIAGNOSTIC_DATA_TRANSFER,
                        BusinessRules.EventCodeDescription.DIAGNOSTIC_DATA_TRANSFER.getValue(),
                        false);
            return;
        }

        // If data transfer is success then record success event, diagnostics and malfunction clear/end if applicable

//        April 13, 2022    -   We should record monitor OK if we have previously Monitor NotOK
//        String mostRecentInServiceMonitorEventCode = getMostRecentEventCodeAsStringForEventType(EventType.IN_SERVICE_MONITOR);
//        if (mostRecentInServiceMonitorEventCode != null &&
//                mostRecentInServiceMonitorEventCode.equalsIgnoreCase(EventCode.IN_SERVICE_MONITOR_NOTOK.getValue())) {

        setEldEvent(a,
                BusinessRules.EventType.IN_SERVICE_MONITOR,
                BusinessRules.EventCode.IN_SERVICE_MONITOR_OK,
                BusinessRules.EventRecordStatus.ACTIVE,
                BusinessRules.EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD,
                "Data Transfer Completed Successfully",
                BusinessRules.EventCodeDescription.DIAGNOSTIC_DATA_TRANSFER.getValue(),
                "", "");
//        }


        if (!isLastEldEventDiagnosticCleared(BusinessRules.EventCode.DIAGNOSTIC_DATA_TRANSFER))
            recordDiagnosticOrMalfunctionEvent(a,
                    BusinessRules.EventCode.DIAGNOSTIC_EVENT_CLEARED,
                    BusinessRules.EventCode.DIAGNOSTIC_DATA_TRANSFER,
                    BusinessRules.EventCodeDescription.DIAGNOSTIC_DATA_TRANSFER.getValue(),
                    false);
    }

    public void recordMalfunctionLogged(Activity a, String eventCodeDescription, String
            annotation, String malfunctionDiagnosticCode, String malfunctionDiagnosticDescp) {
        setEldEvent(a,
                EventType.A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE,
                EventCode.ELD_MALFUNCTION_LOGGED,
                EventRecordStatus.ACTIVE,
                EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD,
                eventCodeDescription, annotation, malfunctionDiagnosticCode, malfunctionDiagnosticDescp);
    }

    public void recordMalfunctionClear(Activity a, String eventCodeDescription, String
            annotation, String malfunctionDiagnosticCode, String malfunctionDiagnosticDescp) {
        setEldEvent(a,
                EventType.A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE,
                EventCode.ELD_MALFUNCTION_CLEARED,
                EventRecordStatus.ACTIVE,
                EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD,
                eventCodeDescription, annotation, malfunctionDiagnosticCode, malfunctionDiagnosticDescp);
    }

    public void setDiagnosticIndicatorOn(boolean v) {
        diagnosticIndicator = v;
    }

    public void setMalfunctionIndicatorOn(boolean v) {
        malfunctionIndicator = v;
    }

    //endregion

    //region Truck ELD Events management

    public EldEvent getLastEldEventEntry(BusinessRules.EventCode malfunctionDiagnosticCode) {
        EldEvent e = null;
        lock.lock();

        try {
            Cursor c = db.getQuery("SELECT id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, " +
                    "recordOrigin, truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                    "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, " +
                    "recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                    "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE eventType='7' AND malfunctionDiagnosticCode='" +
                    malfunctionDiagnosticCode.getValue() + "' ORDER BY id DESC LIMIT 1");
//                    malfunctionDiagnosticCode.getValue() + "' AND objectId IS NULL ORDER BY id DESC LIMIT 1");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            if (!c.isAfterLast()) {
                e = new EldEvent();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);

                if (StringUtils.isNullOrWhitespaces(e.TruckNumber))
                    e.TruckNumber = user.getTruckNumber();

                e.TruckNumber = e.TruckNumber != null ? e.TruckNumber.toUpperCase() : null;
            }

            c.close();
            return e;
        } catch (Throwable throwable) {
            return null;
        } finally {
            lock.unlock();
        }
    }


    public ArrayList<EldEvent> getDiagnosticAndMalFunctionForDays(int days,
                                                                  boolean isDiagnostic) {
        Log.d(TAG, "getDiagnosticAndMalFunctionForDays: ");
        EldEvent e = null;
        lock.lock();

        try {
            ArrayList<EldEvent> eventsResult = new ArrayList<>();
            String sqlString = null;

//            April 18, 2022    -   Why object Id should be null for both Diagnostic and Maldfunction
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            if (isDiagnostic) {

                sqlString = "SELECT id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, " +
                        "recordOrigin, truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                        "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, " +
                        "recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                        "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE eventType='7' AND (eventCode='3' OR eventCode='4') " +
                        "AND eldUsername='" + username + "' ORDER BY id DESC";
//                        "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE eventType='7' AND (eventCode='3' OR eventCode='4') AND objectId IS NULL ORDER BY id DESC";
            } else {

                sqlString = "SELECT id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, " +
                        "recordOrigin, truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                        "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, " +
                        "recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                        "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE eventType='7' AND (eventCode='1' OR eventCode='2') ORDER BY id DESC";
//                        "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE eventType='7' AND (eventCode='1' OR eventCode='2') AND objectId IS NULL ORDER BY id DESC";

            }
//            Log.d(TAG, "getDiagnosticAndMalFunctionForDays: sqlString: " + sqlString);

            Cursor cursor = db.getQuery(sqlString);
//            Log.d(TAG, "getDiagnosticAndMalFunctionForDays: cursor: " + cursor);
            if (cursor == null)
                return null;

            cursor.moveToFirst();
            int col = 0;
//            Log.d(TAG, "getDiagnosticAndMalFunctionForDays: c: count: " + cursor.getCount());


            while (!cursor.isAfterLast()) {

//                Log.d(TAG, "getDiagnosticAndMalFunctionForDays: c: position: " + cursor.getPosition());
                e = new EldEvent();

                e.Id = cursor.getString(col++);
                e.OrganizationName = cursor.getString(col++);
                e.OrganizationNumber = cursor.getString(col++);
                e.EldUsername = cursor.getString(col++);
                e.EventType = cursor.getString(col++);
                e.EventCode = cursor.getString(col++);
                e.RecordStatus = cursor.getString(col++);
                e.RecordOrigin = cursor.getString(col++);
                e.TruckNumber = cursor.getString(col++);
                e.Vin = cursor.getString(col++);
                e.LocalizationDescription = cursor.getString(col++);
                e.LatitudeString = cursor.getString(col++);
                e.LongitudeString = cursor.getString(col++);
                e.DstSinceLastValidCoords = cursor.getString(col++);
                e.VehicleMiles = cursor.getString(col++);
                e.EngineHours = cursor.getString(col++);
                e.OrderNumberCmv = cursor.getString(col++);
                e.OrderNumberUser = cursor.getString(col++);
                e.SequenceId = cursor.getString(col++);
                e.EventCodeDescription = cursor.getString(col++);
                e.DiagnosticIndicator = cursor.getString(col++);
                e.MalfunctionIndicator = cursor.getString(col++);
                e.Annotation = cursor.getString(col++);
                e.RecordOriginId = cursor.getString(col++);
                e.CheckData = cursor.getString(col++);
                e.CheckSum = cursor.getString(col++);
                e.MalfunctionDiagnosticCode = cursor.getString(col++);
                e.MalfunctionDiagnosticDescp = cursor.getString(col++);
                e.DriverLastName = cursor.getString(col++);
                e.DriverFirstName = cursor.getString(col++);
                e.DriverRecordId = cursor.getString(col++);
                e.EditReason = cursor.getString(col++);
                e.EventSeconds = cursor.getDouble(col++);
                e.ShiftStart = cursor.getString(col++);
                e.CreationDate = cursor.getString(col++);
                e.Odometer = cursor.getString(col++);

//                Log.d(TAG, "getDiagnosticAndMalFunctionForDays: last7Days: " + DateUtils.getCurrentDateTimeSevenDaysAgo("", DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS, -7));
                Date currentDateTimeSevenDaysAgo = DateUtils.getCurrentDateTimeSevenDaysAgo("", DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS, -7);

                Date myDate = dateFormat.parse(e.CreationDate);
//                Log.d(TAG, "getDiagnosticAndMalFunctionForDays: time: " + (myDate.getTime() > currentDateTimeSevenDaysAgo.getTime()));
                if (myDate.getTime() > currentDateTimeSevenDaysAgo.getTime()) {
                    eventsResult.add(e);
                }

                cursor.moveToNext();
                col = 0;
            }

            cursor.close();
            return eventsResult;
        } catch (Throwable throwable) {
            Log.d(TAG, "getDiagnosticAndMalFunctionForDays: throwable: " + throwable.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
    }

    public EldEvent getEldEvent(int id) {
        EldEvent e = null;
        lock.lock();

        try {
            Cursor c = db.getQuery("SELECT id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, " +
                    "recordOrigin, truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                    "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, " +
                    "recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                    "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE id=" + id);

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            if (!c.isAfterLast()) {
                e = new EldEvent();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);

                if (StringUtils.isNullOrWhitespaces(e.TruckNumber))
                    e.TruckNumber = user.getTruckNumber();

                e.TruckNumber = e.TruckNumber != null ? e.TruckNumber.toUpperCase() : null;
            }

            c.close();
            return e;
        } catch (Throwable throwable) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    public EldCycle getEldCycle(int id) {
        EldCycle e = null;
        lock.lock();

        try {
            Cursor c = db.getQuery("SELECT id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, " +
                    "recordOrigin, truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                    "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, " +
                    "recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                    "editReason, eventseconds, shiftstart, creationdate FROM eldevents WHERE id=" + id);

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            if (!c.isAfterLast()) {
                e = new EldCycle();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                col += 5;
                e.TruckNumber = c.getString(col++);
                col += 10;
                e.DriverStatus = c.getString(col++);
                col += 8;
                e.FirstName = c.getString(col++);
                e.LastName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                col += 3;
                e.StartDate = c.getString(col++);

                e.UserRecordId = e.DriverRecordId;
                e.Year = DateUtils.getYear(e.StartDate);
                e.Driver = e.LastName;
                e.TripName = e.LastName + ", " + e.FirstName + ", " + e.DriverRecordId + ", " + e.StartDate;
                e.Trailer1Number = user != null && StringUtils.equalsIgnoreCase(user.getRecordId(), e.DriverRecordId) ? user.getTrailerNumber() : "";
            }

            c.close();
            return e;
        } catch (Throwable throwable) {
            return null;
        } finally {
            lock.unlock();
        }
    }

    //endregion

    //region Truck Logs management

    public boolean existsOpenTruckLogHeader() {
        return db.exists("SELECT * FROM trucklogheader WHERE startDate IS NOT NULL AND endDate IS NULL AND active=1 AND driverRecordId='" + user.getRecordId() + "'");
    }

    public boolean existsTruckLogDetailForToday() {
        String yyyyMmDdDateStr = DateUtils.getNowYyyyMmDd();

        return db.exists("SELECT * FROM trucklogdetail WHERE driverRecordId='" +
                user.getRecordId() + "' AND startdate LIKE '" + yyyyMmDdDateStr + "%' AND endDateTime IS NULL");
    }

    public void syncTruckLogs() throws Exception {
//        May 26, 2022  -   The sync is done just at the first install
        String driverRecordId = user.getRecordId();
        if (db.exists("SELECT * FROM trucklogheader WHERE objectId IS NOT NULL AND driverRecordId='" + driverRecordId + "'")) {
            Log.d(TAG, "syncTruckLogs: db.exists: ");
            return;
        }

        truckLogs = new ArrayList<>();
        ArrayList<TruckLogHeader> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");
//        Log.d(TAG, "syncTruckLogs: orgName: " + orgName);

//        March 24, 2022    -   We should pass active coding field as "1"
//        The call should return only the active TruckLogHeaders
//        If there is no TruckLogHeader Active only then we create a new TruckLogHeader from the device
        String response = Rms.getRecordsUpdatedXFiltered("Truck+Log+Header", -5000,
                "Organization+Name,DriverRecordId,Active", ",", orgName + "," + driverRecordId + ",1", ",", "");
//        Log.d(TAG, "syncTruckLogs: response: " + response);


        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
//            Log.d(TAG, "syncTruckLogs: jsonArray: length: " + jsonArray.length());
            clearTableEntries("trucklogheader", "objectId NOT NULL AND driverRecordId='" + driverRecordId + "'");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                TruckLogHeader h = new TruckLogHeader();

                h.objectId = codingFields.getValue("LobjectId");
                h.objectType = codingFields.getValue("objectType");
                h.RecordId = codingFields.getValue("RecordId");
                h.MobileRecordId = codingFields.getValue("MobileRecordId");
                h.FirstName = codingFields.getValue("First Name");
                h.LastName = codingFields.getValue("Last Name");
                h.UserRecordId = codingFields.getValue("UserRecordId");
                h.DriverRecordId = codingFields.getValue("DriverRecordId");
                h.StartDate = codingFields.getValue("Start Date");
                h.BarCode = codingFields.getValue("BarCode");
                h.CreationDate = codingFields.getValue("Creation Date");
                h.CreationDatetime = codingFields.getValue("Creation Datetime");
                h.CreationTime = codingFields.getValue("Creation Time");
                h.RMSTimestamp = codingFields.getValue("RMS Timestamp");
                h.RMSEfileTimestamp = codingFields.getValue("RMS Efile Timestamp");
                h.RMSCodingTimestamp = codingFields.getValue("RMS Coding Timestamp");
                h.FunctionalGroupName = codingFields.getValue("FunctionalGroupName");
                h.FunctionalGroupObjectId = codingFields.getValue("FunctionalGroupObjectId");
                h.CreatorFirstName = codingFields.getValue("Creator First Name");
                h.CreatorLastName = codingFields.getValue("Creator Last Name");
                h.CreatorRecordId = codingFields.getValue("CreatorRecordId");
                h.OrganizationName = codingFields.getValue("Organization Name");
                h.OrganizationNumber = codingFields.getValue("Organization Number");
                h.OffDutyHours = codingFields.getValue("Off Duty Hours");
                h.SleeperHours = codingFields.getValue("Sleeper Hours");
                h.DrivingHours = codingFields.getValue("Driving Hours");
                h.OnDutyHours = codingFields.getValue("On Duty Hours");
                h.HomeOfficeRecordId = codingFields.getValue("HomeOfficeRecordId");
                h.HomeOfficeName = codingFields.getValue("Home Office Name");
                h.HomeOfficePhone = codingFields.getValue("Home Office Phone");
                h.Year = codingFields.getValue("Year");
                h.VehicleLicenseNumber = codingFields.getValue("Vehicle License Number");
                h.Driver = codingFields.getValue("Driver");
                h.CoDriver = codingFields.getValue("Co Driver");
                h.CoDriverRecordId = codingFields.getValue("CoDriverRecordId");
                h.Rule = codingFields.getValue("Rule");
                h.RuleDrivingDate = codingFields.getValue("Rule Driving Date");

                String active = codingFields.getValue("Active");
                h.Active = codingFields.getValue("Active");
                h.TotalDistance = codingFields.getValue("Total Distance");
                h.SpeedViolations = codingFields.getValue("Speed Violations");
                h.GeofenceViolations = codingFields.getValue("Geofence Violations");
                h.StartTime = codingFields.getValue("Start Time");
                String endDate = codingFields.getValue("End Date");
                String endTime = codingFields.getValue("End Time");
//                Log.d(TAG, "syncTruckLogs: active: " + active + " endDate: " + endDate + " endTime: " + endTime);
                h.EndDate = codingFields.getValue("End Date");
                h.EndTime = codingFields.getValue("End Time");
                h.ItemType = codingFields.getValue("ItemType");
                h.TripName = codingFields.getValue("Trip Name");
                h.OverdueTimeLimit = codingFields.getValue("Overdue Time Limit");
                h.RouteHeaderRecordId = codingFields.getValue("RouteHeaderRecordId");
                h.HoursRemaining = codingFields.getValue("HoursRemaining");
                h.Weight = codingFields.getValue("Weight");
                h.Lot = codingFields.getValue("Lot");
                h.TruckNumber = codingFields.getValue("Truck Number");
                h.Trailer1Number = codingFields.getValue("Trailer1 Number");
                h.Trailer2Number = codingFields.getValue("Trailer2 Number");
                h.DriverStatus = codingFields.getValue("Driver Status");
                h.PreviousTractors = codingFields.getValue("Previous Tractors");

                if (h.MobileRecordId != null && h.MobileRecordId.toLowerCase().indexOf("trucklogdetail") != -1)
                    continue;

                result.add(h);
                Integer truckLogHeaderId = insertTruckLogHeaderEntry(h, true);


                String response2 = Rms.getRecordsUpdatedXFiltered("Truck+Log+Detail", -5000,
                        "Organization+Name,Master+Barcode", ",", orgName + "," + h.BarCode, ",", "");

//                Log.d(TAG, "syncTruckLogs: response2: " + response2);
                if (response2 != null && response2.trim().length() > 0) {
                    jsonArray = new JSONArray(response2);
                    clearTableEntries("trucklogdetail", "MasterBarcode='" + h.BarCode + "'");

                    for (int j = 0; j < jsonArray.length(); j++) {
                        JSONObject o2 = jsonArray.getJSONObject(j);
                        PairList codingFields2 = Rms.parseJsonCodingFields(o2);

                        TruckLogDetail d = new TruckLogDetail();

                        d.objectId = codingFields2.getValue("LobjectId");
                        d.objectType = codingFields2.getValue("objectType");
                        d.RecordId = codingFields2.getValue("RecordId");
                        d.MobileRecordId = codingFields2.getValue("MobileRecordId");
                        d.CycleStartDate = codingFields2.getValue("CycleStartDate");
                        d.StartDate = codingFields2.getValue("Start Date");
                        d.CycleStartDateTime = codingFields2.getValue("CycleStartDateTime");
                        d.DriverRecordId = codingFields2.getValue("DriverRecordId");
                        d.CreationDate = codingFields2.getValue("Creation Date");
                        d.CreationDatetime = codingFields2.getValue("Creation Datetime");
                        d.CreationTime = codingFields2.getValue("Creation Time");
                        d.RMSTimestamp = codingFields2.getValue("RMS Timestamp");
                        d.RMSCodingTimestamp = codingFields2.getValue("RMS Coding Timestamp");
                        d.RMSEfileTimestamp = codingFields2.getValue("RMS Efile Timestamp");
                        d.FunctionalGroupName = codingFields2.getValue("FunctionalGroupName");
                        d.FunctionalGroupObjectId = codingFields2.getValue("FunctionalGroupObjectId");
                        d.CycleEndDate = codingFields2.getValue("Cycle End Date");
                        d.CycleEndTime = codingFields2.getValue("Cycle End Time");
                        d.CycleStarDate = codingFields2.getValue("Cycle Start Date");
                        d.CycleStartTime = codingFields2.getValue("Cycle Start Time");
                        d.CreatorFirstName = codingFields2.getValue("Creator First Name");
                        d.CreatorLastName = codingFields2.getValue("Creator Last Name");
                        d.CreatorRecordId = codingFields2.getValue("CreatorRecordId");
                        d.OrganizationName = codingFields2.getValue("Organization Name");
                        d.OrganizationNumber = codingFields2.getValue("Organization Number");
                        d.DrivingHours = codingFields2.getValue("Driving Hours");
                        d.ShiftHours = codingFields2.getValue("Shift Hours");
                        d.Time = codingFields2.getValue("Time");
                        d.ShiftReset = codingFields2.getValue("Shift Reset");
                        d.CycleHours = codingFields2.getValue("Cycle Hours");
                        d.CycleType = codingFields2.getValue("Cycle Type");
                        d.CycleReset = codingFields2.getValue("Cycle Reset");
                        d.TimeZone = codingFields2.getValue("Time Zone");
                        d.VehicleLicenseNumber = codingFields2.getValue("Vehicle License Number");
                        d.TruckType = codingFields2.getValue("Truck Type");
                        d.VIN = codingFields2.getValue("VIN");
                        d.LoadDescription = codingFields2.getValue("Load Description");
                        d.EventDescription = codingFields2.getValue("Event Description");
                        d.EventDate = codingFields2.getValue("Event Date");
                        d.EventStart = codingFields2.getValue("Event Start");
                        d.EventDuration = codingFields2.getValue("Event Duration");
                        d.EventStatus = codingFields2.getValue("Event Status");
                        d.EventLocation = codingFields2.getValue("Event Location");
                        d.EventNotes = codingFields2.getValue("Event Notes");
                        d.Carrier = codingFields2.getValue("Carrier");
                        d.Inspector = codingFields2.getValue("Inspector");
                        d.InspectionNotes = codingFields2.getValue("Inspection Notes");
                        d.StartDate = codingFields2.getValue("Start Date");
                        d.StartTime = codingFields2.getValue("Start Time");
                        d.EndDateTime = codingFields2.getValue("End DateTime");
//                        Log.d(TAG, "syncTruckLogs: EndDateTime: " + d.EndDateTime);

                        d.EndTime = codingFields2.getValue("End Time");
                        d.OffDuty = codingFields2.getValue("OffDuty");
                        d.Sleeper = codingFields2.getValue("Sleeper");
                        d.Driving = codingFields2.getValue("Driving");
                        d.OnDuty = codingFields2.getValue("OnDuty");
                        d.Driver = codingFields2.getValue("Driver");
                        d.CoDriver = codingFields2.getValue("Co Driver");
                        d.EquipmentInfoNumbers = codingFields2.getValue("Equipment Info Numbers");
                        d.CarrierName = codingFields2.getValue("Carrier Name");
                        d.CarrierAddress = codingFields2.getValue("Carrier Address");
                        d.Terminal = codingFields2.getValue("Terminal");
                        d.Rule = codingFields2.getValue("Rule");
                        d.TotalMilesthisCycle = codingFields2.getValue("Total Miles this Cycle");
                        d.Latitudes = codingFields2.getValue("Latitudes");
                        d.Longitudes = codingFields2.getValue("Longitudes");
                        d.LocationsDescriptions = codingFields2.getValue("Locations Descriptions");
                        d.TotalMilesToday = codingFields2.getValue("Total Miles Today");
                        d.CoDriverRecordId = codingFields2.getValue("CoDriverRecordId");
                        d.DriverRecordId = codingFields2.getValue("DriverRecordId");
                        d.TotalDistance = codingFields2.getValue("Total Distance");
                        d.SpeedViolations = codingFields2.getValue("Speed Violations");
                        d.GeofenceViolations = codingFields2.getValue("Geofence Violations");
                        d.ActiveDriver = codingFields2.getValue("Active Driver");
                        d.ShipmentInfo = codingFields2.getValue("Shipment Info");
                        d.ItemType = codingFields2.getValue("ItemType");
                        d.Overdue = codingFields2.getValue("Overdue");
                        d.FromLatitude = codingFields2.getValue("From Latitude");
                        d.FromLongitude = codingFields2.getValue("From Longitude");
                        d.ToLatitude = codingFields2.getValue("To Latitude");
                        d.ToLongitude = codingFields2.getValue("To Longitude");
                        d.DepartureTime = codingFields2.getValue("Departure Time");
                        d.DepartureDate = codingFields2.getValue("Departure Date");
                        d.TripNumber = codingFields2.getValue("Trip Number");
                        d.RouteHeaderRecordId = codingFields2.getValue("RouteHeaderRecordId");
                        d.CycleEndDateTime = codingFields2.getValue("CycleEndDateTime");
                        d.CycleStartDateTime = codingFields2.getValue("CycleStartDateTime");
                        d.TruckNumber = codingFields2.getValue("Truck Number");
                        d.OdometerStart = codingFields2.getValue("Odometer Start");
                        d.OdometerEnd = codingFields2.getValue("Odometer End");
                        d.EngineHours = codingFields2.getValue("Engine Hours");
                        d.OriginType = codingFields2.getValue("OriginType");
                        d.PreviousTractors = codingFields2.getValue("Previous Tractors");
                        d.Trailer1Number = codingFields2.getValue("Trailer1 Number");
                        d.Trailer2Number = codingFields2.getValue("Trailer2 Number");


                        d.MasterBarcode = h.BarCode;
                        d.TrucklogHeaderId = (truckLogHeaderId != null ? truckLogHeaderId : new Integer(i + 1)).toString();
                        d.HeaderRecordId = h.MobileRecordId;

                        h.addDetail(d);
                        insertTruckDetailEntry(d, true);
                    }
                }
            }

            truckLogs.addAll(result);
        }
    }


    public void syncTrailerLogs() throws Exception {
//        Log.d(TAG, "syncTruckLogs: ");
//        May 26, 2022  -   The sync is done just at the first install
        String driverRecordId = user.getRecordId();

//        Log.d(TAG, "syncTruckLogs: driverRecordId: " + driverRecordId);
//        Log.d(TAG, "syncTruckLogs: isRecordIdExists: " + db.exists("SELECT * FROM trucklogheader WHERE objectId IS NOT NULL AND driverRecordId='" + driverRecordId + "'"));
        if (db.exists("SELECT * FROM trailerlog WHERE objectId IS NOT NULL AND driverRecordId='" + driverRecordId + "'")) {
            return;
        }

        trailerLogs = new ArrayList<>();
        ArrayList<TrailerLog> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");
        String response = Rms.getRecordsUpdatedXFiltered("Trailer+Log+Detail", -5000,
                "Organization+Name,DriverRecordId", ",", orgName + "," + driverRecordId,
                ",", "");

        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            clearTableEntries("trailerlog", "objectId NOT NULL AND driverRecordId='" + driverRecordId + "'");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);
//                Log.d(TAG, "syncTrailerLogs: response: " + Rms.parseJsonCodingFields(o));

                TrailerLog h = new TrailerLog();

//                public String Id, objectId, objectType, BarCode, CreationDate, CreationDatetime, CreationTime,
//                        RecordId, MobileRecordId, OrganizationName, OrganizationNumber, DateTime, TruckNumber,
//                        TrailerNumber, ParentObjectId, ParentObjectType, Action, DriverRecordId;

//                "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                        "objectId			        TEXT, " +
//                        "objectType				    TEXT, " +
//                        "recordId				    TEXT, " +
//                        "mobileRecordId		        TEXT, " +
//                        "organizationName		    TEXT, " +
//                        "organizationNumber		    TEXT, " +
//                        "dateTime   		        TEXT, " +
//                        "truckNumber		        TEXT, " +
//                        "trailerNumber		        TEXT, " +
//                        "parentObjectId		        TEXT, " +
//                        "parentObjectType		    TEXT, " +
//                        "action		                TEXT, " +
//                        "driverRecordId		        TEXT" +


                h.objectId = codingFields.getValue("LobjectId");
                h.objectType = codingFields.getValue("objectType");
                h.RecordId = codingFields.getValue("RecordId");
                h.MobileRecordId = codingFields.getValue("MobileRecordId");
                h.DriverRecordId = codingFields.getValue("DriverRecordId");
                h.DateTime = codingFields.getValue("DateTime");
                h.BarCode = codingFields.getValue("BarCode");
                h.CreationDate = codingFields.getValue("Creation Date");
                h.CreationDatetime = codingFields.getValue("Creation Datetime");
                h.CreationTime = codingFields.getValue("Creation Time");

                h.OrganizationName = codingFields.getValue("Organization Name");
                h.TruckNumber = codingFields.getValue("Truck Number");
                h.TrailerNumber = codingFields.getValue("Trailer Number");
                h.ParentObjectId = codingFields.getValue("ParentObjectId");
                h.ParentObjectType = codingFields.getValue("ParentObjectType");
                h.Action = codingFields.getValue("Action");
                h.DriverRecordId = codingFields.getValue("DriverRecordId");

                if (h.MobileRecordId != null && h.MobileRecordId.toLowerCase().indexOf("trailerlog") != -1)
                    continue;

                result.add(h);
                Integer truckLogHeaderId = insertTrailerLogEntry(h, true);


            }

            trailerLogs.addAll(result);
        }
    }


    public String[] getTruckLogEntryForToday() {
        String userRecordId = user.getRecordId();
        String yyyyMmDdDateStr = DateUtils.getNowYyyyMmDd();

        String sqlString = "SELECT objectId, objectType, headerRecordId, mobileRecordId, id, headeRecordId FROM trucklogdetail WHERE " +
                "driverRecordId='" + userRecordId + "' AND startdate LIKE '" + yyyyMmDdDateStr + "%' AND endDateTime IS NULL";

        if (!db.exists(sqlString))
            return null;

        lock.lock();

        try {
            Cursor c = db.getQuery(sqlString);

            if (c == null)
                return null;

            c.moveToFirst();
            String[] result = new String[6];

            if (!c.isAfterLast()) {
                result[0] = c.getString(0);
                result[1] = c.getString(1);
                result[2] = c.getString(2);
                result[3] = c.getString(3);
                result[4] = c.getString(4);
                result[5] = c.getString(5);
            }

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    public TruckLogHeader getOpenTruckLogHeader() {
//        Cursor c = db.getQuery("SELECT * FROM trucklogheader WHERE driverRecordId='" + user.getRecordId() + "' AND active=1 ORDER BY creationdate DESC");
//        Cursor c = db.getQuery("SELECT * FROM trucklogheader WHERE startDate IS NOT NULL AND endDate IS NULL AND driverRecordId='" + user.getRecordId() + "' AND active=1");

//        Sep 15, 2022  -   App crashed when uninstalled app and run without internet
        if (user == null)
            return null;


        Cursor c = db.getQuery("SELECT * FROM trucklogheader WHERE startDate IS NOT NULL AND endDate IS NULL AND driverRecordId='"
                + user.getRecordId() + "' AND active=1 ORDER BY creationdate DESC");

//        Log.d(TAG, "getOpenTruckLogHeader: cursor: " + c);
        if (c == null)
            return null;

//        Log.d(TAG, "getOpenTruckLogHeader: count: " + c.getCount());
        c.moveToFirst();
        TruckLogHeader h = new TruckLogHeader();
        int couunter = 0;

        if (!c.isAfterLast()) {

            h.Id = Integer.toString(c.getInt(couunter++));
            h.objectId = c.getString(couunter++);
            h.objectType = c.getString(couunter++);
            h.CreationDate = c.getString(couunter++);
            h.CreationDatetime = c.getString(couunter++);
            h.CreationTime = c.getString(couunter++);
            h.RecordId = c.getString(couunter++);
            h.MobileRecordId = c.getString(couunter++);
            h.RMSTimestamp = c.getString(couunter++);
            h.RMSEfileTimestamp = c.getString(couunter++);
            h.RMSCodingTimestamp = c.getString(couunter++);
            h.FunctionalGroupName = c.getString(couunter++);
            h.FunctionalGroupObjectId = c.getString(couunter++);
            h.CreatorFirstName = c.getString(couunter++);
            h.CreatorLastName = c.getString(couunter++);
            h.CreatorRecordId = c.getString(couunter++);
            h.OrganizationName = c.getString(couunter++);
            h.OrganizationNumber = c.getString(couunter++);
            h.OffDutyHours = c.getString(couunter++);
            h.SleeperHours = c.getString(couunter++);
            h.DrivingHours = c.getString(couunter++);
            h.OnDutyHours = c.getString(couunter++);
            h.HomeOfficeRecordId = c.getString(couunter++);
            h.HomeOfficeName = c.getString(couunter++);
            h.HomeOfficePhone = c.getString(couunter++);
            h.FirstName = c.getString(couunter++);
            h.LastName = c.getString(couunter++);
            h.UserRecordId = c.getString(couunter++);
            h.Year = c.getString(couunter++);
            h.VehicleLicenseNumber = c.getString(couunter++);
            h.Driver = c.getString(couunter++);
            h.DriverRecordId = c.getString(couunter++);
            h.CoDriver = c.getString(couunter++);
            h.CoDriverRecordId = c.getString(couunter++);
            h.Rule = c.getString(couunter++);
            h.RuleDrivingDate = c.getString(couunter++);
            h.Active = c.getString(couunter++);
            h.TotalDistance = c.getString(couunter++);
            h.SpeedViolations = c.getString(couunter++);
            h.GeofenceViolations = c.getString(couunter++);
            h.StartDate = c.getString(couunter++);
            h.StartTime = c.getString(couunter++);
            h.EndDate = c.getString(couunter++);
            h.EndTime = c.getString(couunter++);
            h.ItemType = c.getString(couunter++);
            h.TripName = c.getString(couunter++);
            h.OverdueTimeLimit = c.getString(couunter++);
            h.RouteHeaderRecordId = c.getString(couunter++);
            h.HoursRemaining = c.getString(couunter++);
            h.Weight = c.getString(couunter++);
            h.Lot = c.getString(couunter++);
            h.TruckNumber = c.getString(couunter++);
            h.Trailer1Number = c.getString(couunter++);
            h.Trailer2Number = c.getString(couunter++);
            h.DriverStatus = c.getString(couunter++);
            h.SleeperHours = c.getString(couunter++);
            h.PreviousTractors = c.getString(couunter++);
            h.BarCode = c.getString(couunter++);
            h.Sent = c.getShort(couunter++) == 1;

//            Log.d(TAG, "getOpenTruckLogHeader: barCode: " + h.BarCode);
            Cursor c2 = db.getQuery("SELECT id FROM trucklogdetail WHERE trucklogheaderid=" + h.Id + "");

            if (c2 != null) {
                c2.moveToFirst();

                while (!c2.isAfterLast()) {
                    int detailId = c2.getInt(0);
//                    Log.d(TAG, "getOpenTruckLogHeader: cycle: detailId: " + detailId);
                    TruckLogDetail d = getTruckLogDetail(detailId);

                    h.addDetail(d);
                    c2.moveToNext();
                }

                c2.close();
            }

            return h;
        }

        return null;
    }


    //    July 19, 2022 -   We created this function to get the most recent driving cycle (Like is it ended or continued)
    public TruckLogHeader getMostRecentTruckLogHeader() {

//        Sep 15, 2022  -   App crashed when uninstalled app and run without internet
        if (user == null)
            return null;

        Cursor c = db.getQuery("SELECT * FROM trucklogheader WHERE driverRecordId='"
                + user.getRecordId() + "' ORDER BY creationDatetime DESC");

//        Log.d(TAG, "getOpenTruckLogHeader: cursor: " + c);
        if (c == null)
            return null;

//        Log.d(TAG, "getOpenTruckLogHeader: count: " + c.getCount());
        c.moveToFirst();
        TruckLogHeader h = new TruckLogHeader();
        int counter = 0;

        if (!c.isAfterLast()) {

            h.Id = Integer.toString(c.getInt(counter++));
            h.objectId = c.getString(counter++);
            h.objectType = c.getString(counter++);
            h.CreationDate = c.getString(counter++);
            h.CreationDatetime = c.getString(counter++);
            h.CreationTime = c.getString(counter++);
            h.RecordId = c.getString(counter++);
            h.MobileRecordId = c.getString(counter++);
            h.RMSTimestamp = c.getString(counter++);
            h.RMSEfileTimestamp = c.getString(counter++);
            h.RMSCodingTimestamp = c.getString(counter++);
            h.FunctionalGroupName = c.getString(counter++);
            h.FunctionalGroupObjectId = c.getString(counter++);
            h.CreatorFirstName = c.getString(counter++);
            h.CreatorLastName = c.getString(counter++);
            h.CreatorRecordId = c.getString(counter++);
            h.OrganizationName = c.getString(counter++);
            h.OrganizationNumber = c.getString(counter++);
            h.OffDutyHours = c.getString(counter++);
            h.SleeperHours = c.getString(counter++);
            h.DrivingHours = c.getString(counter++);
            h.OnDutyHours = c.getString(counter++);
            h.HomeOfficeRecordId = c.getString(counter++);
            h.HomeOfficeName = c.getString(counter++);
            h.HomeOfficePhone = c.getString(counter++);
            h.FirstName = c.getString(counter++);
            h.LastName = c.getString(counter++);
            h.UserRecordId = c.getString(counter++);
            h.Year = c.getString(counter++);
            h.VehicleLicenseNumber = c.getString(counter++);
            h.Driver = c.getString(counter++);
            h.DriverRecordId = c.getString(counter++);
            h.CoDriver = c.getString(counter++);
            h.CoDriverRecordId = c.getString(counter++);
            h.Rule = c.getString(counter++);
            h.RuleDrivingDate = c.getString(counter++);
            h.Active = c.getString(counter++);
            h.TotalDistance = c.getString(counter++);
            h.SpeedViolations = c.getString(counter++);
            h.GeofenceViolations = c.getString(counter++);
            h.StartDate = c.getString(counter++);
            h.StartTime = c.getString(counter++);
            h.EndDate = c.getString(counter++);
            h.EndTime = c.getString(counter++);
            h.ItemType = c.getString(counter++);
            h.TripName = c.getString(counter++);
            h.OverdueTimeLimit = c.getString(counter++);
            h.RouteHeaderRecordId = c.getString(counter++);
            h.HoursRemaining = c.getString(counter++);
            h.Weight = c.getString(counter++);
            h.Lot = c.getString(counter++);
            h.TruckNumber = c.getString(counter++);
            h.Trailer1Number = c.getString(counter++);
            h.Trailer2Number = c.getString(counter++);
            h.DriverStatus = c.getString(counter++);
            h.SleeperHours = c.getString(counter++);
            h.PreviousTractors = c.getString(counter++);
            h.BarCode = c.getString(counter++);
            h.Sent = c.getShort(counter++) == 1;

//            Log.d(TAG, "getOpenTruckLogHeader: barCode: " + h.BarCode);
            Cursor c2 = db.getQuery("SELECT id FROM trucklogdetail WHERE trucklogheaderid=" + h.Id + "");

            if (c2 != null) {
                c2.moveToFirst();

                while (!c2.isAfterLast()) {
                    int detailId = c2.getInt(0);
//                    Log.d(TAG, "getOpenTruckLogHeader: cycle: detailId: " + detailId);
                    TruckLogDetail d = getTruckLogDetail(detailId);

                    h.addDetail(d);
                    c2.moveToNext();
                }

                c2.close();
            }

            return h;
        }

        return null;
    }


    //    May 02, 2022  -   We need to get the eld based on the mobileRecordId on the truck log detail
    public TruckEldDetail getEldDetailForTruckLogDetailMobileRecordId(String
                                                                              truckLogDetailRecordId) {
//        Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: truckLogDetailRecordId: " + truckLogDetailRecordId);

        if (truckLogDetailRecordId == null || truckLogDetailRecordId.isEmpty()) {
            return null;
        }

//        Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: truckLogDetailRecordId: " + truckLogDetailRecordId);
        Cursor cursor = db.getQuery("SELECT * FROM truckelddetail WHERE truckLogDetailRecordId='" + truckLogDetailRecordId + "'");

//        Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: cursor: " + cursor);
        if (cursor == null)
            return null;

//        Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: cursor: size: " + cursor.getCount());
        cursor.moveToFirst();

        TruckEldDetail truckEldDetail = new TruckEldDetail();
        int counter = 0;
        if (!cursor.isAfterLast()) {
            truckEldDetail.Id = Integer.toString(cursor.getInt(counter++));
            truckEldDetail.objectId = cursor.getString(counter++);
            truckEldDetail.objectType = cursor.getString(counter++);
            truckEldDetail.MasterBarcode = cursor.getString(counter++);
            truckEldDetail.CreationDatetime = cursor.getString(counter++);
            truckEldDetail.CreationDate = cursor.getString(counter++);
            truckEldDetail.CreationTime = cursor.getString(counter++);
            truckEldDetail.RecordId = cursor.getString(counter++);
            truckEldDetail.RmsTimestamp = cursor.getString(counter++);
            truckEldDetail.RmsCodingTimestamp = cursor.getString(counter++);
            truckEldDetail.RmsEfileTimestamp = cursor.getString(counter++);
            truckEldDetail.FunctionalGroupName = cursor.getString(counter++);
            truckEldDetail.FunctionalGroupObjectId = cursor.getString(counter++);
            truckEldDetail.MobileRecordId = cursor.getString(counter++);
            truckEldDetail.OrganizationName = cursor.getString(counter++);
            truckEldDetail.OrganizationNumber = cursor.getString(counter++);
            truckEldDetail.TwentyFourHourPeriodStartingTime = cursor.getString(counter++);
            truckEldDetail.CarrierName = cursor.getString(counter++);
            truckEldDetail.UsDotNumber = cursor.getString(counter++);
            truckEldDetail.DriverName = cursor.getString(counter++);
            truckEldDetail.DriverId = cursor.getString(counter++);
            truckEldDetail.DriverRecordId = cursor.getString(counter++);
            truckEldDetail.CoDriverName = cursor.getString(counter++);
            truckEldDetail.CoDriverId = cursor.getString(counter++);
            truckEldDetail.CoDriverRecordId = cursor.getString(counter++);
            truckEldDetail.CurrentLocation = cursor.getString(counter++);
            truckEldDetail.DataDiagnosticsIndicators = cursor.getString(counter++);
            truckEldDetail.EldMalfunctionIndicators = cursor.getString(counter++);
            truckEldDetail.EldManufacturerName = cursor.getString(counter++);
            truckEldDetail.EldRegistrationId = cursor.getString(counter++);
            truckEldDetail.UnidentifiedDriverRecords = cursor.getString(counter++);
            truckEldDetail.ExemptDriverStatus = cursor.getString(counter++);
            truckEldDetail.MilesToday = cursor.getString(counter++);
            truckEldDetail.PrintDisplayDate = cursor.getString(counter++);
            truckEldDetail.RecordDate = cursor.getString(counter++);
            truckEldDetail.ShippingId = cursor.getString(counter++);
            truckEldDetail.CurrentEngineHours = cursor.getString(counter++);
            truckEldDetail.EngineHoursStart = cursor.getString(counter++);
            truckEldDetail.EngineHoursEnd = cursor.getString(counter++);
            truckEldDetail.CurrentOdometer = cursor.getString(counter++);
            truckEldDetail.OdometerStart = cursor.getString(counter++);
            truckEldDetail.OdometerEnd = cursor.getString(counter++);
            truckEldDetail.TimeZone = cursor.getString(counter++);
            truckEldDetail.TruckNumber = cursor.getString(counter++);
            truckEldDetail.TruckVin = cursor.getString(counter++);
            truckEldDetail.TrailerNumber = cursor.getString(counter++);
            truckEldDetail.TruckLogDetailRecordId = cursor.getString(counter++);
            truckEldDetail.OffDutyHours = cursor.getString(counter++);
            truckEldDetail.SleeperHours = cursor.getString(counter++);
            truckEldDetail.DrivingHours = cursor.getString(counter++);
            truckEldDetail.OnDutyHours = cursor.getString(counter++);
            truckEldDetail.Status = cursor.getString(counter++);
            truckEldDetail.Comments = cursor.getString(counter++);
            truckEldDetail.Rule = cursor.getString(counter++);
            truckEldDetail.CycleStartDateTime = cursor.getString(counter++);
            truckEldDetail.VehicleLicenseNumber = cursor.getString(counter++);
            truckEldDetail.DataCheckValue = cursor.getString(counter++);
            truckEldDetail.CertifyDateTime = cursor.getString(counter++);
            truckEldDetail.CmvPowerUnitNumber = cursor.getString(counter++);
            truckEldDetail.Sent = cursor.getShort(counter++) == 1;
            truckEldDetail.TruckEldHeaderId = cursor.getString(counter++);

//            Log.d(TAG, "getEldDetailForTruckLogDetailMobileRecordId: truckEldDetail: " + truckEldDetail.ShippingId);
            return truckEldDetail;
        }

        return null;
    }

    public TruckLogHeader getTruckLogHeader(int id) {
        Cursor c = db.getQuery("SELECT * FROM trucklogheader WHERE id=" + id);

        if (c == null)
            return null;

        c.moveToFirst();
        TruckLogHeader h = new TruckLogHeader();
        int counter = 0;

        if (!c.isAfterLast()) {
            h.Id = Integer.toString(c.getInt(counter++));
            h.objectId = c.getString(counter++);
            h.objectType = c.getString(counter++);
            h.CreationDate = c.getString(counter++);
            h.CreationDatetime = c.getString(counter++);
            h.CreationTime = c.getString(counter++);
            h.RecordId = c.getString(counter++);
            h.MobileRecordId = c.getString(counter++);
            h.RMSTimestamp = c.getString(counter++);
            h.RMSEfileTimestamp = c.getString(counter++);
            h.RMSCodingTimestamp = c.getString(counter++);
            h.FunctionalGroupName = c.getString(counter++);
            h.FunctionalGroupObjectId = c.getString(counter++);
            h.CreatorFirstName = c.getString(counter++);
            h.CreatorLastName = c.getString(counter++);
            h.CreatorRecordId = c.getString(counter++);
            h.OrganizationName = c.getString(counter++);
            h.OrganizationNumber = c.getString(counter++);
            h.OffDutyHours = c.getString(counter++);
            h.SleeperHours = c.getString(counter++);
            h.DrivingHours = c.getString(counter++);
            h.OnDutyHours = c.getString(counter++);
            h.HomeOfficeRecordId = c.getString(counter++);
            h.HomeOfficeName = c.getString(counter++);
            h.HomeOfficePhone = c.getString(counter++);
            h.FirstName = c.getString(counter++);
            h.LastName = c.getString(counter++);
            h.UserRecordId = c.getString(counter++);
            h.Year = c.getString(counter++);
            h.VehicleLicenseNumber = c.getString(counter++);
            h.Driver = c.getString(counter++);
            h.DriverRecordId = c.getString(counter++);
            h.CoDriver = c.getString(counter++);
            h.CoDriverRecordId = c.getString(counter++);
            h.Rule = c.getString(counter++);
            h.RuleDrivingDate = c.getString(counter++);
            h.Active = c.getString(counter++);
            h.TotalDistance = c.getString(counter++);
            h.SpeedViolations = c.getString(counter++);
            h.GeofenceViolations = c.getString(counter++);
            h.StartDate = c.getString(counter++);
            h.StartTime = c.getString(counter++);
            h.EndDate = c.getString(counter++);
            h.EndTime = c.getString(counter++);
            h.ItemType = c.getString(counter++);
            h.TripName = c.getString(counter++);
            h.OverdueTimeLimit = c.getString(counter++);
            h.RouteHeaderRecordId = c.getString(counter++);
            h.HoursRemaining = c.getString(counter++);
            h.Weight = c.getString(counter++);
            h.Lot = c.getString(counter++);
            h.TruckNumber = c.getString(counter++);
            h.Trailer1Number = c.getString(counter++);
            h.Trailer2Number = c.getString(counter++);
            h.DriverStatus = c.getString(counter++);
            h.SleeperHours = c.getString(counter++);
            h.PreviousTractors = c.getString(counter++);
            h.BarCode = c.getString(counter++);
            h.Sent = c.getShort(counter++) == 1;

            Cursor c2 = db.getQuery("SELECT id FROM trucklogdetail WHERE trucklogheaderid=" + id + "");

            if (c2 != null) {
                c2.moveToFirst();

                while (!c2.isAfterLast()) {
                    int detailId = c2.getInt(0);
//                    Log.d(TAG, "getTruckLogHeader: cycle: ");
                    TruckLogDetail d = getTruckLogDetail(detailId);

                    h.addDetail(d);
                    c2.moveToNext();
                }

                c2.close();
            }

            return h;
        }

        return null;
    }


    public TrailerLog getTrailerLog(int id) {
        Cursor c = db.getQuery("SELECT * FROM trailerlog WHERE id=" + id);

        if (c == null)
            return null;

        c.moveToFirst();
        TrailerLog h = new TrailerLog();
        int counter = 0;

//        "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                "objectId			        TEXT, " +
//                "objectType				    TEXT, " +
//                "recordId				    TEXT, " +
//                "mobileRecordId		        TEXT, " +
//                "organizationName		    TEXT, " +
//                "organizationNumber		    TEXT, " +
//                "dateTime   		        TEXT, " +
//                "truckNumber		        TEXT, " +
//                "trailerNumber		        TEXT, " +
//                "parentObjectId		        TEXT, " +
//                "parentObjectType		    TEXT, " +
//                "action		                TEXT, " +
//                "driverRecordId		        TEXT" +

        if (!c.isAfterLast()) {
            h.Id = Integer.toString(c.getInt(counter++));
            h.objectId = c.getString(counter++);
            h.objectType = c.getString(counter++);
            h.RecordId = c.getString(counter++);
            h.MobileRecordId = c.getString(counter++);
            h.OrganizationName = c.getString(counter++);
            h.OrganizationNumber = c.getString(counter++);
            h.DateTime = c.getString(counter++);
            h.TruckNumber = c.getString(counter++);
            h.TrailerNumber = c.getString(counter++);
            h.ParentObjectId = c.getString(counter++);
            h.ParentObjectType = c.getString(counter++);
            h.Action = c.getString(counter++);
            h.DriverRecordId = c.getString(counter++);

            return h;
        }

        return null;
    }


    public PretripModel getPretrip(int id) {
        Cursor c = db.getQuery("SELECT * FROM pretrip WHERE id=" + id);

        Log.d(TAG, "save: getPretrip: cursor: "+c);
        if (c == null)
            return null;


        c.moveToFirst();
        PretripModel pretripModel = new PretripModel();
        int counter = 0;

        Log.d(TAG, "save: getPretrip: count: "+c.getCount());

        if (!c.isAfterLast()) {

//            "id						                                INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "objectId			                                    TEXT, " +
//                    "objectType				                                TEXT, " +
//                    "mobileRecordId		                                    TEXT, " +
//                    "functionalGroupName			                        TEXT, " +
//                    "organizationName		                                TEXT, " +
//                    "organizationNumber		                                TEXT, " +// Organization Id (Check)
//                    "dateTime   		                                    TEXT, " +
//                    "latitude   		                                    TEXT, " +
//                    "longitude   		                                    TEXT, " +
//                    "firstName  		                                    TEXT," +    //Driver First Name
//                    "lastName     		                                    TEXT," +    // Driver Last name
//                    "recordId     		                                    TEXT," +    // Driver Record Id
//                    "vehicleLicenseNumber     	                            TEXT," +
//                    "airCompressor     	                                    TEXT," +
//                    "airLines           	                                TEXT," +
            pretripModel.setId(Integer.toString(c.getInt(counter++)));
            pretripModel.setObjectId(c.getString(counter++));
            pretripModel.setObjectType(c.getString(counter++));
            pretripModel.setMobileRecordId(c.getString(counter++));
            pretripModel.setFunctionalGroupName(c.getString(counter++));
            pretripModel.setOrganizationName(c.getString(counter++));
            pretripModel.setOrganizationNumber(c.getString(counter++));
            pretripModel.setDateTime(c.getString(counter++));
            pretripModel.setLatitude(c.getString(counter++));
            pretripModel.setLongitude(c.getString(counter++));
            pretripModel.setFirstName(c.getString(counter++));
            pretripModel.setLastName(c.getString(counter++));
            pretripModel.setRecordId(c.getString(counter++));
            pretripModel.setVehicleLicenseNumber(c.getString(counter++));
            pretripModel.setAirCompressor(c.getString(counter++));
            pretripModel.setAirLines(c.getString(counter++));
//                    "battery           	                                    TEXT," +
//                    "brakeAccessories           	                        TEXT," +
//                    "brakes           	                                    TEXT," +
//                    "carburetor           	                                TEXT," +
//                    "clutch           	                                    TEXT," +
//                    "defroster           	                                TEXT," +
//                    "driveLine           	                                TEXT," +
//                    "fifthWheel           	                                TEXT," +
//                    "frontalAxle           	                                TEXT," +
//                    "fuelTanks           	                                TEXT," +
//                    "heater           	                                    TEXT," +
//                    "horn           	                                    TEXT," +
//                    "lights           	                                    TEXT," +
//                    "mirrors           	                                    TEXT," +
//                    "oilPressure           	                                TEXT," +
//                    "onBoardRecorder           	                            TEXT," +
//                    "radiator           	                                TEXT," +
//                    "rearEnd           	                                    TEXT," +
//                    "reflectors           	                                TEXT," +
//                    "safetyEquipment           	                            TEXT," +
//                    "springs           	                                    TEXT," +
            pretripModel.setBattery(c.getString(counter++));
            pretripModel.setBrakeAccessories(c.getString(counter++));
            pretripModel.setBrakes(c.getString(counter++));
            pretripModel.setCarburetor(c.getString(counter++));
            pretripModel.setClutch(c.getString(counter++));
            pretripModel.setDefroster(c.getString(counter++));
            pretripModel.setDriveLine(c.getString(counter++));
            pretripModel.setFifthWheel(c.getString(counter++));
            pretripModel.setFrontalAxle(c.getString(counter++));
            pretripModel.setFuelTanks(c.getString(counter++));
            pretripModel.setHeater(c.getString(counter++));
            pretripModel.setHorn(c.getString(counter++));
            pretripModel.setLights(c.getString(counter++));
            pretripModel.setMirrors(c.getString(counter++));
            pretripModel.setOilPressure(c.getString(counter++));
            pretripModel.setOnBoardRecorder(c.getString(counter++));
            pretripModel.setRadiator(c.getString(counter++));
            pretripModel.setRearEnd(c.getString(counter++));
            pretripModel.setReflectors(c.getString(counter++));
            pretripModel.setSafetyEquipment(c.getString(counter++));
            pretripModel.setSprings(c.getString(counter++));
//                    "starter           	                                    TEXT," +
//                    "steering           	                                TEXT," +
//                    "tachograph           	                                TEXT," +
//                    "tires           	                                    TEXT," +
//                    "transmission           	                            TEXT," +
//                    "wheels           	                                    TEXT," +
//                    "windows           	                                    TEXT," +
//                    "windShieldWipers           	                        TEXT," +
//                    "others           	                                    TEXT," +
//
//                    "trailer1   				                            TEXT, " +
//                    "trailer1BreakConnections   		                    TEXT, " +
//                    "trailer1Breaks   				                        TEXT, " +
            pretripModel.setStarter(c.getString(counter++));
            pretripModel.setSteering(c.getString(counter++));
            pretripModel.setTachograph(c.getString(counter++));
            pretripModel.setTires(c.getString(counter++));
            pretripModel.setTransmission(c.getString(counter++));
            pretripModel.setWheels(c.getString(counter++));
            pretripModel.setWindows(c.getString(counter++));
            pretripModel.setWindShieldWipers(c.getString(counter++));
            pretripModel.setOthers(c.getString(counter++));
            pretripModel.setTrailer1(c.getString(counter++));
            pretripModel.setTrailer1BreakConnections(c.getString(counter++));
            pretripModel.setTrailer1Breaks(c.getString(counter++));

//                    "trailer1CouplingPin   				                    TEXT, " +
//                    "trailer1CouplingChains   			                    TEXT, " +
//                    "trailer1Doors   				                        TEXT, " +
//                    "trailer1Hitch   				                        TEXT, " +
//                    "trailer1LandingGear   				                    TEXT, " +
//                    "trailer1LightsAll   				                    TEXT, " +
//                    "trailer1Roof   				                        TEXT, " +
//                    "trailer1Springs   				                        TEXT, " +
//                    "trailer1Tarpaulin   				                    TEXT, " +
//                    "trailer1Tires   				                        TEXT, " +
//                    "trailer1Wheels   				                        TEXT, " +
//                    "trailer1Others   				                        TEXT, " +
//
//                    "trailer2   				                            TEXT, " +
//                    "trailer2BreakConnections   				            TEXT, " +
            pretripModel.setTrailer1CouplingPin(c.getString(counter++));
            pretripModel.setTrailer1CouplingChains(c.getString(counter++));
            pretripModel.setTrailer1Doors(c.getString(counter++));
            pretripModel.setTrailer1Hitch(c.getString(counter++));
            pretripModel.setTrailer1LandingGear(c.getString(counter++));
            pretripModel.setTrailer1LightsAll(c.getString(counter++));
            pretripModel.setTrailer1Roof(c.getString(counter++));
            pretripModel.setTrailer1Springs(c.getString(counter++));
            pretripModel.setTrailer1Tarpaulin(c.getString(counter++));
            pretripModel.setTrailer1Tires(c.getString(counter++));
            pretripModel.setTrailer1Wheels(c.getString(counter++));
            pretripModel.setTrailer1Others(c.getString(counter++));
            pretripModel.setTrailer2(c.getString(counter++));
            pretripModel.setTrailer2BreakConnections(c.getString(counter++));

//                    "trailer2Breaks   				                        TEXT, " +
//                    "trailer2CouplingPin   				                    TEXT, " +
//                    "trailer2CouplingChains   				                TEXT, " +
//                    "trailer2Doors   				                        TEXT, " +
//                    "trailer2Hitch   			                        	TEXT, " +
//                    "trailer2LandingGear   				                    TEXT, " +
//                    "trailer2LightsAll   				                    TEXT, " +
//                    "trailer2Roof   				                        TEXT, " +
//                    "trailer2Springs   				                        TEXT, " +
//                    "trailer2Tarpaulin   				                    TEXT, " +
//                    "trailer2Tires   				                        TEXT, " +
//                    "trailer2Wheels   				                        TEXT, " +
//                    "trailer2Others   				                        TEXT, " +
            pretripModel.setTrailer2Breaks(c.getString(counter++));
            pretripModel.setTrailer2CouplingPin(c.getString(counter++));
            pretripModel.setTrailer2CouplingChains(c.getString(counter++));
            pretripModel.setTrailer2Doors(c.getString(counter++));
            pretripModel.setTrailer2Hitch(c.getString(counter++));
            pretripModel.setTrailer2LandingGear(c.getString(counter++));
            pretripModel.setTrailer2LightsAll(c.getString(counter++));
            pretripModel.setTrailer2Roof(c.getString(counter++));
            pretripModel.setTrailer2Springs(c.getString(counter++));
            pretripModel.setTrailer2Tarpaulin(c.getString(counter++));
            pretripModel.setTrailer2Tires(c.getString(counter++));
            pretripModel.setTrailer2Wheels(c.getString(counter++));
            pretripModel.setTrailer2Others(c.getString(counter++));
//                    "remarks		                                        TEXT, " +
//                    "conditionVehicleIsSatisfactory		                    TEXT," +
//                    "driversSignatureVehicleSatisfactory     		        TEXT," +
//                    "aboveDefectsCorrected     		                        TEXT," +
//                    "aboveDefectsNoCorrectionNeeded     		            TEXT," +
//                    "mechanicsSignatureDate     		                    TEXT," +
//                    "driversSignatureNoCorrectionNeeded     		        TEXT," +
//                    "driversSignatureNoCorrectionNeededDate     		    TEXT," +
//                    "truckTractorNumber     		                        TEXT," +
//                    "carrier     		                                    TEXT," +
//                    "address     		                                    TEXT," +
//                    "odometer     		                                    TEXT," +
//                    "mechanicFirstName     		                            TEXT," +
//                    "mechanicLastName     		                            TEXT," +
//                    "mechanicRecordId     		                            TEXT," +
//                    "trailer1ReeferHOS     		                            TEXT," +
//                    "trailer2ReeferHOS     		                            TEXT," +
//                    "registration     		                                TEXT," +
//                    "insurance     		                                    TEXT," +
//                    "rmsTimestamp               TEXT," +
            pretripModel.setRemarks(c.getString(counter++));
            pretripModel.setConditionVehicleIsSatisfactory(c.getString(counter++));
            pretripModel.setDriversSignatureVehicleSatisfactory(c.getString(counter++));
            pretripModel.setAboveDefectsCorrected(c.getString(counter++));
            pretripModel.setAboveDefectsNoCorrectionNeeded(c.getString(counter++));
            pretripModel.setMechanicsSignatureDate(c.getString(counter++));
            pretripModel.setDriversSignatureNoCorrectionNeeded(c.getString(counter++));
            pretripModel.setDriversSignatureNoCorrectionNeededDate(c.getString(counter++));
            pretripModel.setTruckNumber(c.getString(counter++));
            pretripModel.setCarrier(c.getString(counter++));
            pretripModel.setAddress(c.getString(counter++));
            pretripModel.setOdometer(c.getString(counter++));
            pretripModel.setMechanicFirstName(c.getString(counter++));
            pretripModel.setMechanicLastName(c.getString(counter++));
            pretripModel.setMechanicRecordId(c.getString(counter++));
            pretripModel.setTrailer1ReeferHOS(c.getString(counter++));
            pretripModel.setTrailer2ReeferHOS(c.getString(counter++));
            pretripModel.setRegistration(c.getString(counter++));
            pretripModel.setInsurance(c.getString(counter++));
            pretripModel.setRmsTimestamp(c.getString(counter++));

            return pretripModel;
        }

        return null;
    }


    public TollReceiptModel getTollReceipt(int id) {
        Cursor c = db.getQuery("SELECT * FROM tollreceipt WHERE id=" + id);

        if (c == null)
            return null;

        c.moveToFirst();
        TollReceiptModel tollReceiptModel = new TollReceiptModel();
        int counter = 0;

        if (!c.isAfterLast()) {

//            "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "objectId			        TEXT, " +
//                    "objectType				    TEXT, " +
//                    "recordId				    TEXT, " +
//                    "mobileRecordId		        TEXT, " +
//                    "organizationName		    TEXT, " +
//                    "organizationNumber		    TEXT, " +
//                    "dateTime   		        TEXT, " +
//                    "truckNumber		        TEXT, " +
//                    "userRecordId		        TEXT," +
//                    "firstName  		        TEXT," +
//                    "lastName     		        TEXT," +
//                    "company     		        TEXT," +
//                    "dotNumber     		        TEXT," +
//                    "vehicleLicenseNumber     	TEXT," +
//                    "vendorName     	        TEXT," +
//                    "vendorState         	    TEXT," +
//                    "vendorCountry       	    TEXT," +
//                    "amount                	    TEXT," +
//                    "roadName                	TEXT," +
//                    "rmsTimestamp               TEXT," +
//                    "sent   					BIT" +

            tollReceiptModel.setId(Integer.toString(c.getInt(counter++)));
            tollReceiptModel.setTollReceiptObjectId(c.getString(counter++));
            tollReceiptModel.setTollReceiptObjectType(c.getString(counter++));
            tollReceiptModel.setTollReceiptRecordId(c.getString(counter++));
            tollReceiptModel.setTollReceiptMobileRecordId(c.getString(counter++));
            tollReceiptModel.setTollReceiptOrganizationName(c.getString(counter++));
            tollReceiptModel.setTollReceiptOrganizationNumber(c.getString(counter++));
            tollReceiptModel.setTollReceiptDateTime(c.getString(counter++));
            tollReceiptModel.setTollReceiptTruckNumber(c.getString(counter++));
            tollReceiptModel.setTollReceiptUserRecordId(c.getString(counter++));
            tollReceiptModel.setTollReceiptFirstName(c.getString(counter++));
            tollReceiptModel.setTollReceiptLastName(c.getString(counter++));
            tollReceiptModel.setTollReceiptCompany(c.getString(counter++));
            tollReceiptModel.setTollReceiptDotNumber(c.getString(counter++));
            tollReceiptModel.setTollReceiptVehicleLicenseNumber(c.getString(counter++));
            tollReceiptModel.setTollReceiptVendorName(c.getString(counter++));
            tollReceiptModel.setTollReceiptVendorState(c.getString(counter++));
            tollReceiptModel.setTollReceiptVendorCountry(c.getString(counter++));
            tollReceiptModel.setTollReceiptAmount(c.getString(counter++));
            tollReceiptModel.setTollReceiptRoadName(c.getString(counter++));

            return tollReceiptModel;
        }

        return null;
    }

    public FuelReceiptModel getFuelReceipt(int id) {
        Cursor c = db.getQuery("SELECT * FROM fuelreceipt WHERE id=" + id);

        if (c == null)
            return null;

        c.moveToFirst();
        FuelReceiptModel fuelReceiptModel = new FuelReceiptModel();
        int counter = 0;

        if (!c.isAfterLast()) {

//            "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
//                    "objectId			        TEXT, " +
//                    "objectType				    TEXT, " +
//                    "recordId				    TEXT, " +
//                    "mobileRecordId		        TEXT, " +
//                    "organizationName		    TEXT, " +
//                    "organizationNumber		    TEXT, " +
//                    "dateTime   		        TEXT, " +
//                    "truckNumber		        TEXT, " +
//                    "userRecordId		        TEXT," +
//                    "firstName  		        TEXT," +
//                    "lastName     		        TEXT," +
//                    "company     		        TEXT," +
//                    "dotNumber     		        TEXT," +
//                    "vehicleLicenseNumber     	TEXT," +
//                    "gallons                	TEXT," +
//                    "amount                	    TEXT," +
//                    "salesTax                	TEXT," +
//                    "truckStop       	        TEXT," +
//                    "state         	            TEXT," +
//                    "country       	            TEXT," +
//                    "fuelType                	TEXT," +
//                    "odometer                	TEXT," +
//                    "rmsTimestamp               TEXT," +
//                    "sent   					BIT" +
            fuelReceiptModel.setId(Integer.toString(c.getInt(counter++)));
            fuelReceiptModel.setFuelReceiptObjectId(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptObjectType(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptRecordId(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptMobileRecordId(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptOrganizationName(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptOrganizationNumber(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptDateTime(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptTruckNumber(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptUserRecordId(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptFirstName(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptLastName(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptCompany(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptDOTNumber(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptVehicleLicenseNumber(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptGallons(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptAmount(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptSalesTax(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptTruckStop(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptState(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptCountry(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptFuelType(c.getString(counter++));
            fuelReceiptModel.setFuelReceiptOdometer(c.getString(counter++));
//            fuelReceiptModel.setFuelReceiptRMSTimestamp(c.getString(counter++));

            return fuelReceiptModel;
        }

        return null;
    }


    public TruckLogHeader getTruckLogHeader(String truckLogHeaderMobileRecordId) {
        Cursor c = db.getQuery("SELECT * FROM trucklogheader WHERE mobileRecordId='" + truckLogHeaderMobileRecordId + "'");

        if (c == null)
            return null;

        c.moveToFirst();
        TruckLogHeader h = new TruckLogHeader();
        int counter = 0;

        if (!c.isAfterLast()) {
            h.Id = Integer.toString(c.getInt(counter++));
            h.objectId = c.getString(counter++);
            h.objectType = c.getString(counter++);
            h.CreationDate = c.getString(counter++);
            h.CreationDatetime = c.getString(counter++);
            h.CreationTime = c.getString(counter++);
            h.RecordId = c.getString(counter++);
            h.MobileRecordId = c.getString(counter++);
            h.RMSTimestamp = c.getString(counter++);
            h.RMSEfileTimestamp = c.getString(counter++);
            h.RMSCodingTimestamp = c.getString(counter++);
            h.FunctionalGroupName = c.getString(counter++);
            h.FunctionalGroupObjectId = c.getString(counter++);
            h.CreatorFirstName = c.getString(counter++);
            h.CreatorLastName = c.getString(counter++);
            h.CreatorRecordId = c.getString(counter++);
            h.OrganizationName = c.getString(counter++);
            h.OrganizationNumber = c.getString(counter++);
            h.OffDutyHours = c.getString(counter++);
            h.SleeperHours = c.getString(counter++);
            h.DrivingHours = c.getString(counter++);
            h.OnDutyHours = c.getString(counter++);
            h.HomeOfficeRecordId = c.getString(counter++);
            h.HomeOfficeName = c.getString(counter++);
            h.HomeOfficePhone = c.getString(counter++);
            h.FirstName = c.getString(counter++);
            h.LastName = c.getString(counter++);
            h.UserRecordId = c.getString(counter++);
            h.Year = c.getString(counter++);
            h.VehicleLicenseNumber = c.getString(counter++);
            h.Driver = c.getString(counter++);
            h.DriverRecordId = c.getString(counter++);
            h.CoDriver = c.getString(counter++);
            h.CoDriverRecordId = c.getString(counter++);
            h.Rule = c.getString(counter++);
            h.RuleDrivingDate = c.getString(counter++);
            h.Active = c.getString(counter++);
            h.TotalDistance = c.getString(counter++);
            h.SpeedViolations = c.getString(counter++);
            h.GeofenceViolations = c.getString(counter++);
            h.StartDate = c.getString(counter++);
            h.StartTime = c.getString(counter++);
            h.EndDate = c.getString(counter++);
            h.EndTime = c.getString(counter++);
            h.ItemType = c.getString(counter++);
            h.TripName = c.getString(counter++);
            h.OverdueTimeLimit = c.getString(counter++);
            h.RouteHeaderRecordId = c.getString(counter++);
            h.HoursRemaining = c.getString(counter++);
            h.Weight = c.getString(counter++);
            h.Lot = c.getString(counter++);
            h.TruckNumber = c.getString(counter++);
            h.Trailer1Number = c.getString(counter++);
            h.Trailer2Number = c.getString(counter++);
            h.DriverStatus = c.getString(counter++);
            h.SleeperHours = c.getString(counter++);
            h.PreviousTractors = c.getString(counter++);
            h.BarCode = c.getString(counter++);
            h.Sent = c.getShort(counter++) == 1;

            Cursor c2 = db.getQuery("SELECT id FROM trucklogdetail WHERE trucklogheaderid=" + h.Id + "");

            if (c2 != null) {
                c2.moveToFirst();

                while (!c2.isAfterLast()) {
                    int detailId = c2.getInt(0);
//                    Log.d(TAG, "getTruckLogHeader: cycle: ");
                    TruckLogDetail d = getTruckLogDetail(detailId);

                    h.addDetail(d);
                    c2.moveToNext();
                }

                c2.close();
            }

            return h;
        }

        return null;
    }

    public TruckLogDetail getTruckLogDetail(int id) {
        Cursor c = db.getQuery("SELECT * FROM trucklogdetail WHERE id=" + id);

        if (c == null)
            return null;

        c.moveToFirst();
        TruckLogDetail d = new TruckLogDetail();
        int counter = 0;

        if (!c.isAfterLast()) {

//            Log.d(TAG, "getTruckLogDetail: counter: " + counter++);
            d.Id = Integer.toString(c.getInt(counter++));
            d.objectId = c.getString(counter++);
            d.objectType = c.getString(counter++);
            d.RecordId = c.getString(counter++);
            d.MobileRecordId = c.getString(counter++);
            d.CycleStartDate = c.getString(counter++);
            d.StartDate = c.getString(counter++);
            d.CycleStartDateTime = c.getString(counter++);
            d.DriverRecordId = c.getString(counter++);
            d.TrucklogHeaderId = c.getString(counter++);
            d.HeaderRecordId = c.getString(counter++);
            d.MasterBarcode = c.getString(counter++);
            d.CreationDate = c.getString(counter++);
            d.CreationDatetime = c.getString(counter++);
            d.CreationTime = c.getString(counter++);
            d.RMSTimestamp = c.getString(counter++);
            d.RMSCodingTimestamp = c.getString(counter++);
            d.RMSEfileTimestamp = c.getString(counter++);
            d.FunctionalGroupName = c.getString(counter++);
            d.FunctionalGroupObjectId = c.getString(counter++);
            d.CycleEndDate = c.getString(counter++);
            d.CycleEndTime = c.getString(counter++);
            d.CycleStartDate = c.getString(counter++);
            d.CycleStartTime = c.getString(counter++);
            d.CreatorFirstName = c.getString(counter++);
            d.CreatorLastName = c.getString(counter++);
            d.CreatorRecordId = c.getString(counter++);
            d.OrganizationName = c.getString(counter++);
            d.OrganizationNumber = c.getString(counter++);
            d.DrivingHours = c.getString(counter++);
            d.ShiftHours = c.getString(counter++);
            d.Time = c.getString(counter++);
            d.ShiftReset = c.getString(counter++);
            d.CycleHours = c.getString(counter++);
            d.CycleType = c.getString(counter++);
            d.CycleReset = c.getString(counter++);
            d.TimeZone = c.getString(counter++);
            d.VehicleLicenseNumber = c.getString(counter++);
            d.TruckType = c.getString(counter++);
            d.VIN = c.getString(counter++);
            d.LoadDescription = c.getString(counter++);
            d.EventDescription = c.getString(counter++);
            d.EventDate = c.getString(counter++);
            d.EventStart = c.getString(counter++);
            d.EventDuration = c.getString(counter++);
            d.EventStatus = c.getString(counter++);
            d.EventLocation = c.getString(counter++);
            d.EventNotes = c.getString(counter++);
//            Fixing the end DateTime   //  Mar 9, 2022
            d.Carrier = c.getString(counter++);
            d.Carrier = getSetting("database.ELD Carrier Name");
            d.Inspector = c.getString(counter++);
            d.InspectionNotes = c.getString(counter++);
            d.StartTime = c.getString(counter++);
            d.EndDateTime = c.getString(counter++);
            d.EndTime = c.getString(counter++);
            d.OffDuty = c.getString(counter++);
            d.Sleeper = c.getString(counter++);
            d.Driving = c.getString(counter++);
            d.OnDuty = c.getString(counter++);
            d.Driver = c.getString(counter++);
            d.CoDriver = c.getString(counter++);
            d.EquipmentInfoNumbers = c.getString(counter++);
//            Fixing the Shifting Values   //  Mar 9, 2022
            d.CarrierName = c.getString(counter++);
            d.CarrierName = getSetting("database.ELD Carrier Name");
            d.CarrierAddress = c.getString(counter++);
            d.Terminal = c.getString(counter++);
            d.Rule = c.getString(counter++);
            d.TotalMilesthisCycle = c.getString(counter++);
            d.Latitudes = c.getString(counter++);
            d.Longitudes = c.getString(counter++);
            d.LocationsDescriptions = c.getString(counter++);
            d.TotalMilesToday = c.getString(counter++);
            d.CoDriverRecordId = c.getString(counter++);
            d.TotalDistance = c.getString(counter++);
            d.SpeedViolations = c.getString(counter++);
            d.GeofenceViolations = c.getString(counter++);
            d.ActiveDriver = c.getString(counter++);
            d.ShipmentInfo = c.getString(counter++);
            d.ItemType = c.getString(counter++);
            d.Overdue = c.getString(counter++);
            d.FromLatitude = c.getString(counter++);
            d.FromLongitude = c.getString(counter++);
            d.ToLatitude = c.getString(counter++);
            d.ToLongitude = c.getString(counter++);
            d.DepartureTime = c.getString(counter++);
            d.DepartureDate = c.getString(counter++);
            d.TripNumber = c.getString(counter++);
            d.RouteHeaderRecordId = c.getString(counter++);
            d.CycleEndDateTime = c.getString(counter++);
            d.TruckNumber = c.getString(counter++);
            d.OdometerStart = c.getString(counter++);
            d.OdometerEnd = c.getString(counter++);
            d.EngineHours = c.getString(counter++);
            d.OriginType = c.getString(counter++);
            d.OnDutyBreak = c.getString(counter++);
            d.PreviousTractors = c.getString(counter++);
            d.HeaderRecordId = c.getString(counter++);
            d.Trailer1Number = c.getString(counter++); // 10.03.2022
            d.Trailer2Number = c.getString(counter++);
            d.Sent = c.getShort(counter++) == 1;

            return d;
        }

        return null;
    }

    public TruckLogDetail getTruckLogDetailForToday(String truckLogHeaderMobileRecordId, String truckNumber) {
        lock.lock();

        try {
            String yyyyMmDdDateStr = DateUtils.getNowYyyyMmDd();
            if (truckNumber != null && truckNumber.isEmpty()) {
                truckNumber = user.getTruckNumber();
            }

            Log.d(TAG, "applyImplementation: getTruckLogDetailForToday: truckNumber: " + truckNumber);

            String sqlString = "";
            if (truckNumber != null && !truckNumber.isEmpty()) {
                sqlString = "SELECT id FROM trucklogdetail WHERE driverRecordId='" + user.getRecordId() +
                        "' AND startdate LIKE '" + yyyyMmDdDateStr + "%' AND endDateTime IS NULL AND headeRecordId='" + truckLogHeaderMobileRecordId
                        + "' AND truckNumber='" + truckNumber + "'";
            } else {
                sqlString = "SELECT id FROM trucklogdetail WHERE driverRecordId='" + user.getRecordId() +
                        "' AND startdate LIKE '" + yyyyMmDdDateStr + "%' AND endDateTime IS NULL AND headeRecordId='" + truckLogHeaderMobileRecordId + "'";
            }


            Cursor c = db.getQuery(sqlString);

            if (c == null)
                return null;

            c.moveToFirst();
            int id = -1;

            if (!c.isAfterLast())
                id = c.getInt(0);

            c.close();
//            Log.d(TAG, "getTruckLogDetailForToday: cycle: ");
            return getTruckLogDetail(id);
        } finally {
            lock.unlock();
        }
    }

    public String[] findObjectIdTypeByMobileRecordId(String[] objectIdType) {
        if (objectIdType == null || objectIdType[3] == null)
            return null;

        String mobileRecordId = objectIdType[3];
        String headerMobileRecordId = objectIdType[5];

        try {
            String json = Rms.getRecordsUpdatedXFiltered("Truck+Log+Detail", 1000, "MobileRecordId", ",", mobileRecordId, ",", "RecordId");

            if (json == null || json.equalsIgnoreCase("[]")) {
                json = Rms.getRecordsUpdatedXFiltered("Truck+Log+Detail", 1000, "MobileRecordId", ",", headerMobileRecordId, ",", "RecordId");

                if (json == null || json.equalsIgnoreCase("[]"))
                    return null;
            }

            JSONArray response = new JSONArray(json);
            JSONObject detail = response.getJSONObject(0);

            int lObjectId = detail.getInt("LobjectId");
            String objectType = detail.getString("objectType");
            String recordId = detail.getJSONObject("mapCodingInfo").getString("RecordId");

            objectIdType[0] = Integer.toString(lObjectId);
            objectIdType[1] = objectType;
            objectIdType[2] = recordId;

            return objectIdType;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return null;
    }

    public String createNewDriverTruckLogHeaderEntry() {
        Log.d(TAG, "Settings: createNewDriverTruckLogHeaderEntry: ");
        String mobileRecordId = Rms.getDefaultMobileRecordId("TruckLogHeader");
        Log.d(TAG, "Settings: createNewDriverTruckLogHeaderEntry: mobileRecordId: " + mobileRecordId);
        //2022.08.19 we need to mark that a new cycle was started
        newCycleStarted = true;
        ContentValues values = new ContentValues();

        values.put("creationDate", DateUtils.getNowYyyyMmDd());
        values.put("creationDatetime", DateUtils.getNowYyyyMmDdHhmmss());
        values.put("creationTime", DateUtils.getNowHhmmss());
        values.put("creatorFirstName", user.getFirstName());
        values.put("creatorLastName", user.getLastName());
        values.put("creatorRecordId", user.getRecordId());
        values.put("mobileRecordId", mobileRecordId);
        values.put("organizationName", Rms.getOrgName());
        values.put("organizationNumber", Rms.getOrgNumber());
        values.put("offDutyHours", "0");
        values.put("sleeperHours", "0");
        values.put("drivingHours", "0");
        values.put("onDutyHours", "0");
        values.put("firstName", user.getFirstName());
        values.put("lastName", user.getLastName());
        values.put("userRecordId", user.getRecordId());
        values.put("year", DateUtils.getNowYear());
        values.put("vehicleLicenseNumber", user.getTruckNumber());
        values.put("driver", user.getLastName());
        values.put("driverRecordId", user.getRecordId());
        values.put("active", "TRUE");
        values.put("totalDistance", "0");
        values.put("startDate", DateUtils.getNowYyyyMmDd());
        values.put("startTime", DateUtils.getNowHhmmss());
        values.put("itemType", "trucklogheader");
        values.put("tripName", user.getLastName() + ", " + user.getFirstName() + ", " + user.getRecordId() + ", " + DateUtils.getNowYyyyMmDdHhmmss());
        values.put("routeHeaderRecordId", "0");
        values.put("hoursRemaining", "0");
        values.put("weight", "0");
        values.put("truckNumber", user.getTruckNumber());
        values.put("trailer1Number", user.getTrailerNumber());
        values.put("driverStatus", user.getDrivingStatus());

        Log.d(TAG, "Settings: createNewDriverTruckLogHeaderEntry: rule: " + getActiveRuleName());
        values.put("rule", getActiveRuleName() != null ? getActiveRuleName() : "");
//        values.put("rule", user.getTruckRule());
        Log.d(TAG, "Settings: createNewDriverTruckLogHeaderEntry: rule: " + user.getTruckRule());
        values.put("previousTractors", "");
        values.put("active", "1");
        values.put("sent", "0");

        db.insert("trucklogheader", values);

        return mobileRecordId;
    }


    public String createNewTrailerLogEntry(String truckNumber, String trailerNumber, String
            action, String parentObjectId, String parentObjectType,
                                           String driverRecordId) {
//        Log.d(TAG, "createTrailerLog: createNewTrailerLogEntry: ");
        String mobileRecordId = Rms.getDefaultMobileRecordId("TrailerLog");

        ContentValues values = new ContentValues();
        values.put("dateTime", DateUtils.getNowYyyyMmDdHhmmss());
        values.put("mobileRecordId", mobileRecordId);
        values.put("organizationName", Rms.getOrgName());
        values.put("organizationNumber", Rms.getOrgNumber());
        values.put("trailerNumber", trailerNumber);
        values.put("truckNumber", truckNumber);
        values.put("action", action);
        values.put("parentObjectId", parentObjectId);
        values.put("parentObjectType", parentObjectType);
        values.put("driverRecordId", driverRecordId);
        values.put("sent", "0");

        db.insert("trailerlog", values);

        return mobileRecordId;
    }


    public String createNewDriverTruckLogDetailEntry(String truckLogHeaderMobileRecordId) {
//        Log.d(TAG, "createNewDriverTruckLogDetailEntry: ");
        String mobileRecordId = Rms.getDefaultMobileRecordId("TruckLogDetail");
        TruckLogHeader t = getTruckLogHeader(truckLogHeaderMobileRecordId);

        ContentValues values = new ContentValues();

        values.put("cycleStartDate", t.StartDate);
        values.put("startDate", DateUtils.getNowYyyyMmDd());
        values.put("cycleStartDateTime", t.StartDate);
        values.put("driverRecordId", user.getRecordId());
        values.put("trucklogheaderid", t.Id);
        values.put("headeRecordId", truckLogHeaderMobileRecordId);
        values.put("mobileRecordId", mobileRecordId);
        values.put("masterBarcode", "");
        values.put("creationDate", DateUtils.getNowYyyyMmDd());
        values.put("creationDatetime", DateUtils.getNowYyyyMmDdHhmmss());
        values.put("cCreationTime", DateUtils.getNowHhmmss());
        values.put("cycleEndDate", t.EndDate);
        values.put("cCycleEndTime", t.EndTime);
        values.put("cCycleStarDate", t.StartDate);
        values.put("cycleStartTime", t.StartTime);
        values.put("creatorFirstName", user.getFirstName());
        values.put("creatorLastName", user.getLastName());
        values.put("creatorRecordId", user.getRecordId());
        values.put("organizationName", Rms.getOrgName());
        values.put("organizationNumber", Rms.getOrgNumber());
        values.put("drivingHours", "0");
        values.put("shiftHours", "0");
        values.put("time", DateUtils.getNowHhmmss());
        values.put("shiftReset", "");
        values.put("cycleHours", "0");
        values.put("cycleType", "");
        values.put("cycleReset", "");
        values.put("timeZone", "");
        values.put("vehicleLicenseNumber", user.getTruckNumber());
        values.put("truckType", "");
        values.put("vin", user.getTruckNumber());
        values.put("loadDescription", "");
        values.put("eventDescription", "");
        values.put("eventDate", DateUtils.getNowYyyyMmDd());
        values.put("eventStart", "");
        values.put("eventDuration", "");
        values.put("eventStatus", "");
        values.put("eventLocation", "");
        values.put("eventNotes", "");
        values.put("carrier", "");
        values.put("inspector", "");
        values.put("inspectionNotes", "");
        values.put("startTime", DateUtils.getNowHhmmss());
        values.put("endTime", "");
        values.put("offDuty", "");
        values.put("sleeper", "");
        values.put("driving", "");
        values.put("onDuty", "");
        values.put("driver", user.getLastFirstName());
        values.put("coDriver", "");
        values.put("equipmentInfoNumbers", "");
        values.put("carrierName", "");
        values.put("carrierAddress", "");
        values.put("terminal", "");
        values.put("rule", getActiveRuleName() != null ? getActiveRuleName() : "");
        values.put("totalMilesthisCycle", "");
        values.put("latitudes", "");
        values.put("longitudes", "");
        values.put("locationsDescriptions", "");
        values.put("totalMilesToday", "");
        values.put("coDriverRecordId", "");
        values.put("totalDistance", "0");
        values.put("speedViolations", "0");
        values.put("geofenceViolations", "0");
        values.put("activeDriver", "");
        values.put("shipmentInfo", "");
        values.put("itemType", "trucklogdetail");
        values.put("overdue", "");
        values.put("fromLatitude", "0");
        values.put("fromLongitude", "0");
        values.put("toLatitude", "0");
        values.put("toLongitude", "0");
        values.put("departureTime", "");
        values.put("departureDate", "");
        values.put("tripNumber", "");
        values.put("routeHeaderRecordId", "0");
        values.put("cycleEndDateTime", "");
        values.put("truckNumber", user.getTruckNumber());
        values.put("odometerStart", "");
        values.put("odometerEnd", "");
        values.put("engineHours", "0");
        values.put("originType", "");
        values.put("onDutyBreak", "");
        values.put("previousTractors", "");
        values.put("headerRecordId", t.RecordId);
        values.put("trailer1Number", user.getTrailerNumber()); // 10.03.2022
        values.put("trailer2Number", user.getTrailerNumber2());
        values.put("rule", getActiveRuleName() != null ? getActiveRuleName() : "");
        values.put("sent", "0");

        db.insert("trucklogdetail", values);

        return mobileRecordId;
    }

    private EldEvent getPendingEldEntryInDate(String driverRecordId, String logEntryDate) {
        Integer eldId = getFirstPendingEldIdEntryInDate(driverRecordId, logEntryDate);

        if (eldId == null)
            return null;

        return getEldEvent(eldId);
    }

    private Integer getFirstPendingEldIdEntryInDate(String driverRecordId, String logEntryDate) {
        Cursor c = db.getQuery("SELECT id FROM eldevents WHERE (senttrucklogs IS NULL OR senttrucklogs='0') " +
                "AND driverRecordId='" + driverRecordId + "' AND creationdate LIKE '" + logEntryDate + "%' ORDER BY creationdate ASC LIMIT 1");

        if (c == null)
            return null;

        c.moveToFirst();

        if (!c.isAfterLast()) {
            int id = c.getInt(0);
            return id;
        }

        return null;
    }

    public ArrayList<TruckLogHeader> getPendingTruckLogEntries() {
        try {
            ArrayList<TruckLogHeader> result = new ArrayList();
            Cursor c = db.getQuery("SELECT id FROM trucklogheader WHERE startDate IS NOT NULL AND driverRecordId='" + user.getRecordId() + "' AND sent=0");

            if (c == null)
                return null;

            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                TruckLogHeader h = getTruckLogHeader(id);

                result.add(h);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public ArrayList<TrailerLog> getPendingTrailerLogEntries() {
//        Log.d(TAG, "syncTrailerLogs: getPendingTrailerLogEntries: ");
        try {
            ArrayList<TrailerLog> result = new ArrayList();
            Cursor c = db.getQuery("SELECT id FROM trailerlog WHERE dateTime IS NOT NULL AND driverRecordId='" + user.getRecordId() + "' AND sent=0");

            if (c == null)
                return null;

            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                TrailerLog h = getTrailerLog(id);

                result.add(h);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "syncTrailerLogs: getPendingTrailerLogEntries: throwable: " + throwable.getMessage());
            return null;
        }
    }

    public ArrayList<PretripModel> getPreTrips() {
        Log.d(TAG, "save: syncTrailerLogs: getPreTrips: getPendingTrailerLogEntries: userRecordId: " + user.getRecordId());
        try {
            ArrayList<PretripModel> result = new ArrayList();
            Cursor c = db.getQuery("SELECT id, mobileRecordId FROM pretrip WHERE dateTime IS NOT NULL");
            if (c == null)
                return null;

            Log.d(TAG, "save: getPreTrips: size: " + c.getCount());
            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                String mobileRecordId = c.getString(1);

                Log.d(TAG, "save: getPreTrips: id: " + id + " mobileRecordId: " + mobileRecordId);
                PretripModel pretripModel = getPretrip(id);
                Log.d(TAG, "save: getPreTrips: tollReceiptModel: id: " + pretripModel.getId()
                        + " mobileRecordId: " + pretripModel.getMobileRecordId());

                result.add(pretripModel);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "save: getTollReceipt: syncTrailerLogs: getPendingTrailerLogEntries: throwable: " + throwable.getMessage());
            return null;
        }
    }



    public ArrayList<TollReceiptModel> getTollReceipt() {
        Log.d(TAG, "save: syncTrailerLogs: getTollReceipt: getPendingTrailerLogEntries: userRecordId: " + user.getRecordId());
        try {
            ArrayList<TollReceiptModel> result = new ArrayList();
//            Cursor c = db.getQuery("SELECT id, mobileRecordId FROM tollreceipt WHERE dateTime IS NOT NULL ORDER BY id DESC");
//            Cursor c = db.getQuery("SELECT id FROM tollreceipt WHERE dateTime IS NOT NULL AND sent=0");
//            Cursor c = db.getQuery("SELECT id, mobileRecordId FROM tollreceipt WHERE dateTime IS NOT NULL ORDER BY dateTime DESC");
            Cursor c = db.getQuery("SELECT id, mobileRecordId FROM tollreceipt WHERE dateTime IS NOT NULL");
            Log.d(TAG, "save: getTollReceipt: getPendingTollReceiptEntries: cursor: " + c);
            if (c == null)
                return null;

            Log.d(TAG, "getTollReceipt: size: " + c.getCount());
            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                String mobileRecordId = c.getString(1);

                Log.d(TAG, "getTollReceipt: id: " + id + " mobileRecordId: " + mobileRecordId);
                TollReceiptModel tollReceiptModel = getTollReceipt(id);
                Log.d(TAG, "getTollReceipt: tollReceiptModel: id: " + tollReceiptModel.getId()
                        + " mobileRecordId: " + tollReceiptModel.getTollReceiptMobileRecordId());

                result.add(tollReceiptModel);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "save: getTollReceipt: syncTrailerLogs: getPendingTrailerLogEntries: throwable: " + throwable.getMessage());
            return null;
        }
    }

    public ArrayList<TollReceiptModel> getTollReceiptForDateRange(String startDate, String endDate) {
        Log.d(TAG, "ReceiptFragment: syncTrailerLogs: getTollReceipt: getPendingTrailerLogEntries: userRecordId: " + user.getRecordId());
        try {
            ArrayList<TollReceiptModel> result = new ArrayList();
//            db.getQuery("tollreceipt", null, "dateTime" + " BETWEEN ? AND ?", new String[] {
//                    startDate, endDate }, null, null, null, null);
            Log.d(TAG, "ReceiptFragment: startDate: " + startDate);
            Log.d(TAG, "ReceiptFragment: endDate: " + endDate);

//            String stringQuery = "SELECT * FROM Categories WHERE DateCreated BETWEEN '" + 2012 - 03 - 11
//            00:00:00 + "' AND '" + 2015 - 05 - 12 23:59:59 + "'";

//            Cursor c = db.getQuery("SELECT id, mobileRecordId FROM tollreceipt " +
//                    "WHERE dateTime IS NOT NULL " +
//                    "and datetime >= '" + startDate + "' " +
//                    "and datetime <=  '" + endDate + "' " +
//                    "ORDER BY id DESC");
            String stringQuery = "SELECT * FROM tollreceipt WHERE dateTime BETWEEN '" + startDate + "' AND '" + endDate + "'";
            Cursor c = db.getQuery(stringQuery);

//            "and datetime >= ? " + // -- DateTime
//                    "and datetime <= ? " + // -- DateTime


//            Cursor c = db.getQuery("SELECT id FROM tollreceipt WHERE dateTime IS NOT NULL AND sent=0");
            Log.d(TAG, "ReceiptFragment: getTollReceipt: getPendingTollReceiptEntries: cursor: " + c);
            if (c == null)
                return null;

            Log.d(TAG, "ReceiptFragment: getTollReceipt: size: " + c.getCount());
            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                String mobileRecordId = c.getString(1);

                Log.d(TAG, "ReceiptFragment: id: " + id + " mobileRecordId: " + mobileRecordId);
                TollReceiptModel tollReceiptModel = getTollReceipt(id);
                Log.d(TAG, "ReceiptFragment: tollReceiptModel: id: " + tollReceiptModel.getId()
                        + " mobileRecordId: " + tollReceiptModel.getTollReceiptMobileRecordId());

                result.add(tollReceiptModel);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "ReceiptFragment: syncTrailerLogs: getPendingTrailerLogEntries: throwable: " + throwable.getMessage());
            return null;
        }
    }


    public ArrayList<FuelReceiptModel> getFuelReceiptForDateRange(String startDate, String endDate) {
        Log.d(TAG, "getFuelReceiptForDateRange: ");
        try {
            ArrayList<FuelReceiptModel> result = new ArrayList();
            String stringQuery = "SELECT * FROM fuelreceipt WHERE dateTime BETWEEN '" + startDate + "' AND '" + endDate + "'";
            Cursor c = db.getQuery(stringQuery);

            if (c == null)
                return null;

            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                String mobileRecordId = c.getString(1);
                FuelReceiptModel fuelReceiptModel = getFuelReceipt(id);
                result.add(fuelReceiptModel);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "getFuelReceiptForDateRange: throwable: " + throwable.getMessage());
            return null;
        }
    }


    public ArrayList<FuelReceiptModel> getFuelReceipt() {
        Log.d(TAG, "save: syncTrailerLogs: getFuelReceipt: getPendingTrailerLogEntries: userRecordId: " + user.getRecordId());
        try {
            ArrayList<FuelReceiptModel> result = new ArrayList();
            Cursor c = db.getQuery("SELECT id, mobileRecordId FROM fuelreceipt WHERE dateTime IS NOT NULL ORDER BY dateTime DESC");
            if (c == null)
                return null;

            Log.d(TAG, "getFuelReceipt: size: " + c.getCount());
            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                String mobileRecordId = c.getString(1);

                Log.d(TAG, "getFuelReceipt: id: " + id + " mobileRecordId: " + mobileRecordId);
                FuelReceiptModel fuelReceiptModel = getFuelReceipt(id);
                Log.d(TAG, "getFuelReceipt: fuelReceiptModel: id: " + fuelReceiptModel.getId()
                        + " mobileRecordId: " + fuelReceiptModel.getFuelReceiptMobileRecordId());

                result.add(fuelReceiptModel);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "save: getFuelReceipt: syncTrailerLogs: getPendingTrailerLogEntries: throwable: " + throwable.getMessage());
            return null;
        }
    }


    public ArrayList<PretripModel> getPendingPretripEntries() {
        Log.d(TAG, "save: getPendingPretripEntries: getPendingTrailerLogEntries: userRecordId: " + user.getRecordId());
        try {
            ArrayList<PretripModel> result = new ArrayList();
//            Cursor c = db.getQuery("SELECT id FROM tollreceipt WHERE dateTime IS NOT NULL AND userRecordId='" + user.getRecordId() + "' AND sent=0");
            Cursor c = db.getQuery("SELECT id FROM pretrip WHERE dateTime IS NOT NULL AND sent=0");
            Log.d(TAG, "save: getPendingPretripEntries: cursor: " + c);
            if (c == null)
                return null;

            Log.d(TAG, "save: getPendingPretripEntries: count: "+c.getCount());

            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                Log.d(TAG, "save: getPendingPretripEntries: id: "+id);
                PretripModel pretripModel = getPretrip(id);
                Log.d(TAG, "save: getPendingPretripEntries: preTripModel: id: "+pretripModel.getId());

                result.add(pretripModel);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "save: syncTrailerLogs: getPendingPretripEntries: throwable: " + throwable.getMessage());
            return null;
        }
    }


    public ArrayList<TollReceiptModel> getPendingTollReceiptEntries() {
        Log.d(TAG, "save: syncTrailerLogs: getPendingTrailerLogEntries: userRecordId: " + user.getRecordId());
        try {
            ArrayList<TollReceiptModel> result = new ArrayList();
//            Cursor c = db.getQuery("SELECT id FROM tollreceipt WHERE dateTime IS NOT NULL AND userRecordId='" + user.getRecordId() + "' AND sent=0");
            Cursor c = db.getQuery("SELECT id FROM tollreceipt WHERE dateTime IS NOT NULL AND sent=0");
            Log.d(TAG, "save: getPendingTollReceiptEntries: cursor: " + c);
            if (c == null)
                return null;

            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                TollReceiptModel tollReceiptModel = getTollReceipt(id);

                result.add(tollReceiptModel);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "save: syncTrailerLogs: getPendingTrailerLogEntries: throwable: " + throwable.getMessage());
            return null;
        }
    }

    public ArrayList<FuelReceiptModel> getPendingFuelReceiptEntries() {
        Log.d(TAG, "save: syncTrailerLogs: getPendingTrailerLogEntries: userRecordId: " + user.getRecordId());
        try {
            ArrayList<FuelReceiptModel> result = new ArrayList();
            Cursor c = db.getQuery("SELECT id FROM fuelreceipt WHERE dateTime IS NOT NULL AND sent=0");
            Log.d(TAG, "save: getPendingFuelReceiptEntries: cursor: " + c);
            if (c == null)
                return null;

            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                FuelReceiptModel fuelReceiptModel = getFuelReceipt(id);

                result.add(fuelReceiptModel);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "save: syncTrailerLogs: getPendingFuelReceiptEntries: throwable: " + throwable.getMessage());
            return null;
        }
    }


    public void sendPendingTruckLogEntries(TruckLogHeader e) throws Exception {
        Log.d(TAG, "sendPendingTruckLogEntries: ");
        ArrayList<String[]> response = Rms.setTruckLogs(e);
        Log.d(TAG, "sendPendingTruckLogEntries: response: " + response);

        e.objectId = response.get(0)[2];
        e.objectType = response.get(0)[3];

        updateTruckLogEntryObjectIdType("header", e.Id, e.objectId, e.objectType);
        ArrayList<TruckLogDetail> details = e.getTruckLogDetails();

        if (details == null || details.size() == 0)
            return;

        for (int i = 0; i < details.size(); i++) {
            TruckLogDetail detail = details.get(i);

//            Sep 05, 2022  -
            if (detail.objectId == null) {
                updateContentLines(detail.MobileRecordId, response.get(i + 1)[2], response.get(i + 1)[3]);
            }

            detail.objectId = response.get(i + 1)[2];
            detail.objectType = response.get(i + 1)[3];

            if (StringUtils.isNullOrWhitespaces(detail.objectId) || detail.objectId.equalsIgnoreCase("-1")) // Some details may return errors although they update server-side. To workaround this just ignore those and allow for the up to go seek the objectId/type later, as it can do that
                continue;

            updateTruckLogEntryObjectIdType("detail", detail.Id, detail.objectId, detail.objectType);
        }
    }


    //    Sep 05, 2022  -
    public void updateContentLines(String parentMobileRecordId, String objectId, String
            objectType) {
        Log.d(TAG, "updateContentLines: ");
        ArrayList<String[]> result = new ArrayList();
        Cursor c = db.getQuery("SELECT * FROM contentlines WHERE (sent=0 OR sent IS NULL) AND parentMobileRecordId='" + parentMobileRecordId +
                "' ORDER BY creationdate ASC");

        if (c == null)
            return;

        Log.d(TAG, "updateContentLines: cursor: count: " + c.getCount());
        c.moveToFirst();
        int counter = 0;

        while (!c.isAfterLast()) {
            String id = c.getString(counter++);

            ContentValues values = new ContentValues();
            values.put("objectId", objectId);
            values.put("objectType", objectType);
            db.update("contentlines", values, "id=" + id);

            c.moveToNext();
            counter = 0;
        }
        c.close();
    }

    public void sendPendingTrailerLogEntries(TrailerLog e) throws Exception {
//        Log.d(TAG, "syncTrailerLogs: sendPendingTrailerLogEntries: ");
        ArrayList<String[]> response = Rms.setTrailerLogs(e);

        e.objectId = response.get(0)[2];
        e.objectType = response.get(0)[3];

        updateTruckLogEntryObjectIdType("header", e.Id, e.objectId, e.objectType);

    }

    public void sendPendingPretripEntries(PretripModel pretripModel) throws Exception {
        Log.d(TAG, "save: sendPendingPretripEntries: pretripModel: objectId: "+pretripModel.getObjectId());

        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put(Crms.DRIVER_FIRST_NAME, user.getFirstName());
        mapHeader.put(Crms.DRIVER_LAST_NAME, user.getLastName());
        mapHeader.put(Crms.DRIVER_RECORDID, user.getRecordId());
        mapHeader.put(Crms.DATETIME, DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z));

        ArrayList<String[]> response = Rms.setPretrip(mapHeader, pretripModel);

        pretripModel.setObjectId(response.get(0)[2]);
        pretripModel.setObjectType(response.get(0)[3]);
        pretripModel.setMobileRecordId(response.get(0)[4]);
        pretripModel.setRmsTimestamp(response.get(0)[5]);
        Log.d(TAG, "save: sendPendingEntries: response: " + response + " id: " + pretripModel.getId()
                + " objectId: " + pretripModel.getObjectId() + " objectType: " + pretripModel.getObjectType()
                + " mobileRecordId: " + pretripModel.getMobileRecordId() + " rmsTimestamp: " + pretripModel.getRmsTimestamp());
        updatePretripEntryObjectIdType(pretripModel);
    }

    public void sendPendingTollReceiptEntries(TollReceiptModel e) throws Exception {
//        Log.d(TAG, "syncTrailerLogs: sendPendingTrailerLogEntries: ");
        ArrayList<String[]> response = Rms.setTollReceipts(e);
        e.setTollReceiptObjectId(response.get(0)[2]);
        e.setTollReceiptObjectType(response.get(0)[3]);
        e.setTollReceiptMobileRecordId(response.get(0)[4]);
        e.setTollReceiptRMSCodingTimestamp(response.get(0)[5]);
        Log.d(TAG, "save: sendPendingTollReceiptEntries: response: " + response + " id: " + e.getId()
                + " objectId: " + e.getTollReceiptObjectId() + " objectType: " + e.getTollReceiptObjectType());
        updateTollReceiptEntryObjectIdType(e);
    }

    public void sendPendingFuelReceiptEntries(FuelReceiptModel fuelReceiptModel) throws Exception {
        ArrayList<String[]> response = Rms.setFuelReceipts(fuelReceiptModel);
        fuelReceiptModel.setFuelReceiptObjectId(response.get(0)[2]);
        fuelReceiptModel.setFuelReceiptObjectType(response.get(0)[3]);
        fuelReceiptModel.setFuelReceiptMobileRecordId(response.get(0)[4]);
        fuelReceiptModel.setFuelReceiptRMSCodingTimestamp(response.get(0)[5]);
        fuelReceiptModel.setFuelReceiptRMSTimestamp(response.get(0)[5]);
        Log.d(TAG, "save: sendPendingTollReceiptEntries: response: " + response + " id: " + fuelReceiptModel.getId()
                + " objectId: " + fuelReceiptModel.getFuelReceiptObjectId() + " objectType: " + fuelReceiptModel.getFuelReceiptObjectType());
        updateFuelReceiptEntryObjectIdType(fuelReceiptModel);
    }


    public void updateTruckLogEntryObjectIdType(String entryType, String entryId, String
            objectId, String objectType) {
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("objectId", objectId);
            values.put("objectType", objectType);

            db.update("trucklog" + entryType, values, "id=" + entryId);
        } finally {
            lock.unlock();
        }
    }

    public void updateTollReceiptEntryObjectIdType(String entryType, String entryId, String objectId, String objectType) {
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("objectId", objectId);
            values.put("objectType", objectType);
            values.put("sent", "1");

            Log.d(TAG, "save: updateTollReceiptEntryObjectIdType: values: " + values + " entryId: " + entryId);
            int isInserted = db.update("tollreceipt" + entryType, values, "id=" + entryId);
            Log.d(TAG, "save: updateTollReceiptEntryObjectIdType: isInserted: " + isInserted);


        } finally {
            lock.unlock();
        }
    }

    public void updatePretripEntryObjectIdType(PretripModel pretripModel) {
        Log.d(TAG, "updatePretripEntryObjectIdType: save: ");
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("objectId", pretripModel.getObjectId());
            values.put("objectType", pretripModel.getObjectType());
            values.put("mobileRecordId", pretripModel.getMobileRecordId());
            values.put("rmsTimestamp", pretripModel.getRmsTimestamp());
            values.put("sent", "1");

            int isInserted = db.update("pretrip", values, "id=" + pretripModel.getId());
            Log.d(TAG, "updatePretripEntryObjectIdType: save: isInserted: "+isInserted);
        } finally {
            lock.unlock();
        }
    }


    public void updateTollReceiptEntryObjectIdType(TollReceiptModel tollReceiptModel) {
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("objectId", tollReceiptModel.getTollReceiptObjectId());
            values.put("objectType", tollReceiptModel.getTollReceiptObjectType());
            values.put("mobileRecordId", tollReceiptModel.getTollReceiptMobileRecordId());
            values.put("rmsTimestamp", tollReceiptModel.getTollReceiptRMSCodingTimestamp());
            values.put("sent", "1");

            int isInserted = db.update("tollreceipt", values, "id=" + tollReceiptModel.getId());


        } finally {
            lock.unlock();
        }
    }


    public void updateFuelReceiptEntryObjectIdType(FuelReceiptModel fuelReceiptModel) {
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("objectId", fuelReceiptModel.getFuelReceiptObjectId());
            values.put("objectType", fuelReceiptModel.getFuelReceiptObjectType());
            values.put("mobileRecordId", fuelReceiptModel.getFuelReceiptMobileRecordId());
            values.put("rmsTimestamp", fuelReceiptModel.getFuelReceiptRMSCodingTimestamp());
            values.put("sent", "1");

            int isInserted = db.update("fuelreceipt", values, "id=" + fuelReceiptModel.getId());


        } finally {
            lock.unlock();
        }
    }


    public void updateTollReceiptEntry(TollReceiptModel tollReceiptModel) {
        Log.d(TAG, "save: updateTollReceiptEntry: ");
        lock.lock();

        try {
            ContentValues values = new ContentValues();

//            values.put("objectId", tollReceiptModel.getTollReceiptObjectId());
//            values.put("objectType", tollReceiptModel.getTollReceiptObjectType());
//            values.put("recordId", tollReceiptModel.getTollReceiptRecordId());
//            values.put("mobileRecordId", tollReceiptModel.getTollReceiptMobileRecordId());
//            values.put("organizationName", tollReceiptModel.getTollReceiptOrganizationName());
//            values.put("organizationNumber", tollReceiptModel.getTollReceiptOrganizationNumber());
            values.put("dateTime", tollReceiptModel.getTollReceiptDateTime());
            values.put("truckNumber", tollReceiptModel.getTollReceiptTruckNumber());
            values.put("userRecordId", tollReceiptModel.getTollReceiptUserRecordId());
            values.put("firstName", tollReceiptModel.getTollReceiptFirstName());
            values.put("lastName", tollReceiptModel.getTollReceiptLastName());
            values.put("company", tollReceiptModel.getTollReceiptCompany());
            values.put("dotNumber", tollReceiptModel.getTollReceiptDotNumber());
            values.put("vehicleLicenseNumber", tollReceiptModel.getTollReceiptVehicleLicenseNumber());
            values.put("vendorName", tollReceiptModel.getTollReceiptVendorName());
            values.put("vendorState", tollReceiptModel.getTollReceiptVendorState());
            values.put("vendorCountry", tollReceiptModel.getTollReceiptVendorCountry());
            values.put("amount", tollReceiptModel.getTollReceiptAmount());
            values.put("roadName", tollReceiptModel.getTollReceiptRoadName());
            values.put("sent", "0");

            Log.d(TAG, "save: updateTollReceiptEntry: values: " + values + " id: " + tollReceiptModel.getTollReceiptMobileRecordId());


//            int isInserted = db.update("tollreceipt", values,
//                    "id='" + tollReceiptModel.getId()+"'");

            int isInserted = db.update("tollreceipt", values,
                    "mobileRecordId='" + tollReceiptModel.getTollReceiptMobileRecordId() + "'");

//            int isTollReceiptUpdated = db.update("tollreceipt", values, "id=" + tollReceiptModel.getId());
//            int isInserted = db.update("tollreceipt", values,
//                    "mobileRecordId='" + tollReceiptModel.getTollReceiptMobileRecordId()+"'");

            Log.d(TAG, "save: updateTollReceiptEntry: isInserted: " + isInserted);


        } finally {
            lock.unlock();
        }
    }

    public void updateFuelReceiptEntry(FuelReceiptModel fuelReceiptModel) {
        Log.d(TAG, "save: updateTollReceiptEntry: ");
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("dateTime", fuelReceiptModel.getFuelReceiptDateTime());
            values.put("truckNumber", fuelReceiptModel.getFuelReceiptTruckNumber());
            values.put("userRecordId", fuelReceiptModel.getFuelReceiptUserRecordId());
            values.put("firstName", fuelReceiptModel.getFuelReceiptFirstName());
            values.put("lastName", fuelReceiptModel.getFuelReceiptLastName());
            values.put("company", fuelReceiptModel.getFuelReceiptCompany());
            values.put("dotNumber", fuelReceiptModel.getFuelReceiptDOTNumber());
            values.put("vehicleLicenseNumber", fuelReceiptModel.getFuelReceiptVehicleLicenseNumber());
            values.put("gallons", fuelReceiptModel.getFuelReceiptGallons());
            values.put("amount", fuelReceiptModel.getFuelReceiptAmount());
            values.put("salesTax", fuelReceiptModel.getFuelReceiptSalesTax());
            values.put("truckStop", fuelReceiptModel.getFuelReceiptTruckStop());
            values.put("state", fuelReceiptModel.getFuelReceiptState());
            values.put("country", fuelReceiptModel.getFuelReceiptCountry());
            values.put("fuelType", fuelReceiptModel.getFuelTypeFuelType());
            values.put("odometer", fuelReceiptModel.getFuelReceiptOdometer());

//        Dec 07, 2022  -   I think we should add rmsTimeStamp too here
            values.put("rmsTimestamp", "0");
            values.put("sent", "0");
            Log.d(TAG, "save: updateFuelReceiptEntry: values: " + values + " id: " + fuelReceiptModel.getFuelReceiptMobileRecordId());

            int isUpdated = db.update("fuelreceipt", values,
                    "mobileRecordId='" + fuelReceiptModel.getFuelReceiptMobileRecordId() + "'");
            Log.d(TAG, "save: updateFuelReceiptEntry: isUpdated: " + isUpdated);
        } finally {
            lock.unlock();
        }
    }


    private Integer insertTruckLogHeaderEntry(TruckLogHeader t, boolean isSent) {
        ContentValues values = new ContentValues();

        values.put("objectId", t.objectId);
        values.put("objectType", t.objectType);
        values.put("recordId", t.RecordId);
        values.put("mobileRecordId", t.MobileRecordId);
        values.put("firstName", t.FirstName);
        values.put("lastName", t.LastName);
        values.put("userRecordId", t.UserRecordId);
        values.put("driverRecordId", t.DriverRecordId);
        values.put("startDate", t.StartDate);
        values.put("barcode", t.BarCode);
        values.put("creationDate", t.CreationDate);
        values.put("creationDatetime", t.CreationDatetime);
        values.put("creationTime", t.CreationTime);
        values.put("rmsTimestamp", t.RMSTimestamp);
        values.put("rmsEfileTimestamp", t.RMSEfileTimestamp);
        values.put("rmsCodingTimestamp", t.RMSCodingTimestamp);
        values.put("FunctionalGroupName", t.FunctionalGroupName);
        values.put("functionalGroupObjectId", t.FunctionalGroupObjectId);
        values.put("creatorFirstName", t.CreatorFirstName);
        values.put("creatorLastName", t.CreatorLastName);
        values.put("creatorRecordId", t.CreatorRecordId);
        values.put("organizationName", t.OrganizationName);
        values.put("organizationNumber", t.OrganizationNumber);
        values.put("offDutyHours", t.OffDutyHours);
        values.put("sleeperHours", t.SleeperHours);
        values.put("drivingHours", t.DrivingHours);
        values.put("onDutyHours", t.OnDutyHours);
        values.put("homeOfficeRecordId", t.HomeOfficeRecordId);
        values.put("homeOfficeName", t.HomeOfficeName);
        values.put("homeOfficePhone", t.HomeOfficePhone);
        values.put("year", t.Year);
        values.put("vehicleLicenseNumber", t.VehicleLicenseNumber);
        values.put("driver", t.Driver);
        values.put("coDriver", t.CoDriver);
        values.put("coDriverRecordId", t.CoDriverRecordId);
        values.put("rule", t.Rule);
        values.put("ruleDrivingDate", t.RuleDrivingDate);
        values.put("active", t.Active);
        values.put("totalDistance", t.TotalDistance);
        values.put("speedViolations", t.SpeedViolations);
        values.put("geofenceViolations", t.GeofenceViolations);
        values.put("startDate", t.StartDate);
        values.put("startTime", t.StartTime);
        values.put("endDate", t.EndDate);
        values.put("endTime", t.EndTime);
        values.put("itemType", t.ItemType);
        values.put("tripName", t.TripName);
        values.put("overdueTimeLimit", t.OverdueTimeLimit);
        values.put("routeHeaderRecordId", t.RouteHeaderRecordId);
        values.put("hoursRemaining", t.HoursRemaining);
        values.put("weight", t.Weight);
        values.put("lot", t.Lot);
        values.put("truckNumber", t.TruckNumber);
        values.put("trailer1Number", t.Trailer1Number);
        values.put("trailer2Number", t.Trailer2Number);
        values.put("driverStatus", t.DriverStatus);
        values.put("sleeper", t.SleeperHours);
        values.put("previousTractors", t.PreviousTractors);
        values.put("sent", isSent ? "1" : "0");

        db.insert("trucklogheader", values);
        return getLastTableId("trucklogheader");
    }

    private void insertTruckDetailEntry(TruckLogDetail t, boolean isSent) {
//        Log.d(TAG, "insertTruckDetailEntry: cycle: ");
        ContentValues values = new ContentValues();

        values.put("objectId", t.objectId);
        values.put("objectType", t.objectType);
        values.put("recordId", t.RecordId);
        values.put("mobileRecordId", t.MobileRecordId);
        values.put("cycleStartDate", t.CycleStartDate);
        values.put("startDate", t.StartDate);
        values.put("cycleStartDateTime", t.CycleStartDateTime);
        values.put("driverRecordId", t.DriverRecordId);
        values.put("trucklogheaderid", t.TrucklogHeaderId);
        values.put("headerRecordId", t.HeaderRecordId);
        values.put("masterBarcode", t.MasterBarcode);
        values.put("creationDate", t.CreationDate);
        values.put("creationDatetime", t.CreationDatetime);
        values.put("cCreationTime", t.CreationTime);
        values.put("rmsTimestamp", t.RMSTimestamp);
        values.put("rmsCodingTimestamp", t.RMSCodingTimestamp);
        values.put("rmsEfileTimestamp", t.RMSEfileTimestamp);
        values.put("functionalGroupName", t.FunctionalGroupName);
        values.put("functionalGroupObjectId", t.FunctionalGroupObjectId);
        values.put("cycleEndDate", t.CycleEndDate);
        values.put("cCycleEndTime", t.CycleEndTime);
        values.put("cCycleStarDate", t.CycleStarDate);
        values.put("cycleStartTime", t.CycleStartTime);
        values.put("creatorFirstName", t.CreatorFirstName);
        values.put("creatorLastName", t.CreatorLastName);
        values.put("creatorRecordId", t.CreatorRecordId);
        values.put("organizationName", t.OrganizationName);
        values.put("organizationNumber", t.OrganizationNumber);
        values.put("drivingHours", t.DrivingHours);
        values.put("shiftHours", t.ShiftHours);
        values.put("time", t.Time);
        values.put("shiftReset", t.ShiftReset);
        values.put("cycleHours", t.CycleHours);
        values.put("cycleType", t.CycleType);
        values.put("cycleReset", t.CycleReset);
        values.put("timeZone", t.TimeZone);
        values.put("vehicleLicenseNumber", t.VehicleLicenseNumber);
        values.put("truckType", t.TruckType);
        values.put("vin", t.VIN);
        values.put("eventDescription", t.EventDescription);
        values.put("eventDate", t.EventDate);
        values.put("eventStart", t.EventStart);
        values.put("eventDuration", t.EventDuration);
        values.put("eventStatus", t.EventStatus);
        values.put("eventLocation", t.EventLocation);
        values.put("eventNotes", t.EventNotes);
        values.put("carrier", t.Carrier);
        values.put("inspector", t.Inspector);
        values.put("inspectionNotes", t.InspectionNotes);
        values.put("startTime", t.StartTime);
        values.put("endDateTime", t.EndDateTime);
//        Log.d(TAG, "insertTruckDetailEntry: cycle: " + t.EndDateTime);

        values.put("endTime", t.EndTime);
        values.put("offDuty", t.OffDuty);
        values.put("sleeper", t.Sleeper);
        values.put("driving", t.Driving);
        values.put("onDuty", t.OnDuty);
        values.put("driver", t.Driver);
        values.put("coDriver", t.CoDriver);
        values.put("equipmentInfoNumbers", t.EquipmentInfoNumbers);
        values.put("carrierName", t.CarrierName);
        values.put("carrierAddress", t.CarrierAddress);
        values.put("terminal", t.Terminal);
        values.put("rule", t.Rule);
        values.put("totalMilesthisCycle", t.TotalMilesthisCycle);
        values.put("latitudes", t.Latitudes);
        values.put("longitudes", t.Longitudes);
        values.put("locationsDescriptions", t.LocationsDescriptions);
        values.put("totalMilesToday", t.TotalMilesToday);
        values.put("coDriverRecordId", t.CoDriverRecordId);
        values.put("totalDistance", t.TotalDistance);
        values.put("speedViolations", t.SpeedViolations);
        values.put("geofenceViolations", t.GeofenceViolations);
        values.put("activeDriver", t.ActiveDriver);
        values.put("shipmentInfo", t.ShipmentInfo);
        values.put("itemType", t.ItemType);
        values.put("overdue", t.Overdue);
        values.put("fromLatitude", t.FromLatitude);
        values.put("fromLongitude", t.FromLongitude);
        values.put("toLatitude", t.ToLatitude);
        values.put("toLongitude", t.ToLongitude);
        values.put("departureTime", t.DepartureTime);
        values.put("departureDate", t.DepartureDate);
        values.put("tripNumber", t.TripNumber);
        values.put("routeHeaderRecordId", t.RouteHeaderRecordId);
        values.put("cycleEndDateTime", t.CycleEndDateTime);
        values.put("truckNumber", t.TruckNumber);
        values.put("odometerStart", t.OdometerStart);
        values.put("odometerEnd", t.OdometerEnd);
        values.put("engineHours", t.EngineHours);
        values.put("originType", t.OriginType);
        values.put("onDutyBreak", t.OnDutyBreak);
        values.put("previousTractors", t.PreviousTractors);
        values.put("headeRecordId", t.HeaderRecordId);
        values.put("trailer1Number", t.Trailer1Number); // 10.03.2022
        values.put("trailer2Number", t.Trailer2Number);
        values.put("sent", isSent ? "1" : "0");

        db.insert("trucklogdetail", values);
    }

    private Integer insertTrailerLogEntry(TrailerLog t, boolean isSent) {
        ContentValues values = new ContentValues();

        values.put("objectId", t.objectId);
        values.put("objectType", t.objectType);
        values.put("recordId", t.RecordId);
        values.put("mobileRecordId", t.MobileRecordId);
        values.put("driverRecordId", t.DriverRecordId);
        values.put("organizationName", t.OrganizationName);
        values.put("organizationNumber", t.OrganizationNumber);
        values.put("truckNumber", t.TruckNumber);
        values.put("trailerNumber", t.TrailerNumber);
        values.put("parentObjectId", t.ParentObjectId);
        values.put("parentObjectType", t.ParentObjectType);
        values.put("barcode", t.BarCode);
        values.put("creationDate", t.CreationDate);
        values.put("creationDatetime", t.CreationDatetime);
        values.put("creationTime", t.CreationTime);
        values.put("sent", isSent ? "1" : "0");

        db.insert("trailerlog", values);
        return getLastTableId("trailerlog");
    }

    public Integer insertPretrip(PretripModel pretripModel, boolean isSent) {
        ContentValues values = new ContentValues();

        values.put("objectId", pretripModel.getObjectId());
        values.put("objectType", pretripModel.getObjectType());
        values.put("functionalGroupName", pretripModel.getFunctionalGroupName());
        values.put("organizationName", pretripModel.getOrganizationName());
        values.put("organizationNumber", pretripModel.getOrganizationNumber());
        values.put("dateTime", pretripModel.getDateTime());
        values.put("latitude", pretripModel.getLatitude());
        values.put("longitude", pretripModel.getLongitude());
        values.put("firstName", pretripModel.getFirstName());
        values.put("lastName", pretripModel.getLastName());
        values.put("recordId", pretripModel.getRecordId());
        values.put("vehicleLicenseNumber", pretripModel.getVehicleLicenseNumber());
        values.put("airCompressor", pretripModel.getAirCompressor());
        values.put("airLines", pretripModel.getAirLines());
        values.put("battery", pretripModel.getBattery());
        values.put("brakeAccessories", pretripModel.getBrakeAccessories());
        values.put("brakes", pretripModel.getBrakes());
        values.put("carburetor", pretripModel.getCarburetor());
        values.put("clutch", pretripModel.getClutch());

        values.put("defroster", pretripModel.getDefroster());
        values.put("driveLine", pretripModel.getDriveLine());
        values.put("fifthWheel", pretripModel.getFifthWheel());
        values.put("frontalAxle", pretripModel.getFrontalAxle());
        values.put("fuelTanks", pretripModel.getFuelTanks());
        values.put("heater", pretripModel.getHeater());
        values.put("horn", pretripModel.getHorn());
        values.put("lights", pretripModel.getLights());
        values.put("mirrors", pretripModel.getMirrors());
        values.put("oilPressure", pretripModel.getOilPressure());
        values.put("onBoardRecorder", pretripModel.getOnBoardRecorder());
        values.put("radiator", pretripModel.getRadiator());
        values.put("rearEnd", pretripModel.getRearEnd());
        values.put("reflectors", pretripModel.getReflectors());
        values.put("safetyEquipment", pretripModel.getSafetyEquipment());
        values.put("springs", pretripModel.getSprings());
        values.put("starter", pretripModel.getStarter());
        values.put("steering", pretripModel.getSteering());
        values.put("tachograph", pretripModel.getTachograph());
        values.put("tires", pretripModel.getTires());
        values.put("transmission", pretripModel.getTransmission());
        values.put("wheels", pretripModel.getWheels());
        values.put("windows", pretripModel.getWindows());
        values.put("windShieldWipers", pretripModel.getWindShieldWipers());
        values.put("others", pretripModel.getOthers());

        values.put("trailer1", pretripModel.getTrailer1());
        values.put("trailer1BreakConnections", pretripModel.getTrailer1BreakConnections());
        values.put("trailer1Breaks", pretripModel.getTrailer1Breaks());
        values.put("trailer1CouplingPin", pretripModel.getTrailer1CouplingPin());
        values.put("trailer1CouplingChains", pretripModel.getTrailer1CouplingChains());
        values.put("trailer1Doors", pretripModel.getTrailer1Doors());
        values.put("trailer1Hitch", pretripModel.getTrailer1Hitch());
        values.put("trailer1LandingGear", pretripModel.getTrailer1LandingGear());
        values.put("trailer1LightsAll", pretripModel.getTrailer1LightsAll());
        values.put("trailer1Roof", pretripModel.getTrailer1Roof());
        values.put("trailer1Springs", pretripModel.getTrailer1Springs());
        values.put("trailer1Tarpaulin", pretripModel.getTrailer1Tarpaulin());
        values.put("trailer1Tires", pretripModel.getTrailer1Tires());
        values.put("trailer1Wheels", pretripModel.getTrailer1Wheels());
        values.put("trailer1Others", pretripModel.getTrailer1Others());

        values.put("trailer2", pretripModel.getTrailer2());
        values.put("trailer2BreakConnections", pretripModel.getTrailer2BreakConnections());
        values.put("trailer2Breaks", pretripModel.getTrailer2Breaks());
        values.put("trailer2CouplingPin", pretripModel.getTrailer2CouplingPin());
        values.put("trailer2CouplingChains", pretripModel.getTrailer2CouplingChains());
        values.put("trailer2Doors", pretripModel.getTrailer2Doors());
        values.put("trailer2Hitch", pretripModel.getTrailer2Hitch());
        values.put("trailer2LandingGear", pretripModel.getTrailer2LandingGear());
        values.put("trailer2LightsAll", pretripModel.getTrailer2LightsAll());
        values.put("trailer2Roof", pretripModel.getTrailer2Roof());
        values.put("trailer2Springs", pretripModel.getTrailer2Springs());
        values.put("trailer2Tarpaulin", pretripModel.getTrailer2Tarpaulin());
        values.put("trailer2Tires", pretripModel.getTrailer2Tires());
        values.put("trailer2Wheels", pretripModel.getTrailer2Wheels());
        values.put("trailer2Others", pretripModel.getTrailer2Others());

        values.put("remarks", pretripModel.getRemarks());
        values.put("conditionVehicleIsSatisfactory", pretripModel.getConditionVehicleIsSatisfactory());
        values.put("driversSignatureVehicleSatisfactory", pretripModel.getDriversSignatureVehicleSatisfactory());
        values.put("aboveDefectsCorrected", pretripModel.getAboveDefectsCorrected());
        values.put("aboveDefectsNoCorrectionNeeded", pretripModel.getAboveDefectsNoCorrectionNeeded());
        values.put("mechanicsSignatureDate", pretripModel.getMechanicsSignatureDate());
        values.put("driversSignatureNoCorrectionNeeded", pretripModel.getDriversSignatureNoCorrectionNeeded());
        values.put("driversSignatureNoCorrectionNeededDate", pretripModel.getDriversSignatureNoCorrectionNeededDate());
        values.put("carrier", pretripModel.getCarrier());
        values.put("address", pretripModel.getAddress());
        values.put("odometer", pretripModel.getOdometer());
        values.put("mechanicFirstName", pretripModel.getMechanicFirstName());
        values.put("mechanicLastName", pretripModel.getMechanicLastName());
        values.put("mechanicRecordId", pretripModel.getMechanicRecordId());
        values.put("trailer1ReeferHOS", pretripModel.getTrailer1ReeferHOS());
        values.put("trailer2ReeferHOS", pretripModel.getTrailer2ReeferHOS());
        values.put("registration", pretripModel.getRegistration());
        values.put("insurance", pretripModel.getInsurance());

//        Jan 19, 2022  -   I Think missing these were missing initially (Actually not missing but had a with a different name truckTractorNumber)
        values.put("truckTractorNumber", pretripModel.getTruckNumber());

        values.put("rmsTimestamp", pretripModel.getRmsTimestamp());
        values.put("sent", isSent ? "1" : "0");

        db.insert("pretrip", values);
        return getLastTableId("pretrip");
    }


    public Integer insertTollReceipt(TollReceiptModel tollReceiptModel, boolean isSent) {
        ContentValues values = new ContentValues();

        values.put("objectId", tollReceiptModel.getTollReceiptObjectId());
        values.put("objectType", tollReceiptModel.getTollReceiptObjectType());
        values.put("recordId", tollReceiptModel.getTollReceiptRecordId());
        values.put("mobileRecordId", tollReceiptModel.getTollReceiptMobileRecordId());
        values.put("organizationName", tollReceiptModel.getTollReceiptOrganizationName());
        values.put("organizationNumber", tollReceiptModel.getTollReceiptOrganizationNumber());
        values.put("dateTime", tollReceiptModel.getTollReceiptDateTime());
        values.put("truckNumber", tollReceiptModel.getTollReceiptTruckNumber());
        values.put("userRecordId", tollReceiptModel.getTollReceiptUserRecordId());
        values.put("firstName", tollReceiptModel.getTollReceiptFirstName());
        values.put("lastName", tollReceiptModel.getTollReceiptLastName());
        values.put("company", tollReceiptModel.getTollReceiptCompany());
        values.put("dotNumber", tollReceiptModel.getTollReceiptDotNumber());
        values.put("vehicleLicenseNumber", tollReceiptModel.getTollReceiptVehicleLicenseNumber());
        values.put("vendorName", tollReceiptModel.getTollReceiptVendorName());
        values.put("vendorState", tollReceiptModel.getTollReceiptVendorState());
        values.put("vendorCountry", tollReceiptModel.getTollReceiptVendorCountry());
        values.put("amount", tollReceiptModel.getTollReceiptAmount());
        values.put("roadName", tollReceiptModel.getTollReceiptRoadName());
        values.put("sent", isSent ? "1" : "0");

        db.insert("tollreceipt", values);
        return getLastTableId("tollreceipt");
    }


    public Integer insertFuelReceipt(FuelReceiptModel fuelReceiptModel, boolean isSent) {
        Log.d(TAG, "fuelReceipt: insertFuelReceipt: ");
        ContentValues values = new ContentValues();

        values.put("objectId", fuelReceiptModel.getFuelReceiptObjectId());
        values.put("objectType", fuelReceiptModel.getFuelReceiptObjectType());
        values.put("recordId", fuelReceiptModel.getFuelReceiptRecordId());
        values.put("mobileRecordId", fuelReceiptModel.getFuelReceiptMobileRecordId());
        values.put("organizationName", fuelReceiptModel.getFuelReceiptOrganizationName());
        values.put("organizationNumber", fuelReceiptModel.getFuelReceiptOrganizationNumber());
        values.put("dateTime", fuelReceiptModel.getFuelReceiptDateTime());
        values.put("truckNumber", fuelReceiptModel.getFuelReceiptTruckNumber());
        values.put("userRecordId", fuelReceiptModel.getFuelReceiptUserRecordId());
        values.put("firstName", fuelReceiptModel.getFuelReceiptFirstName());
        values.put("lastName", fuelReceiptModel.getFuelReceiptLastName());
        values.put("company", fuelReceiptModel.getFuelReceiptCompany());
        values.put("dotNumber", fuelReceiptModel.getFuelTypeDotNumber());
        values.put("vehicleLicenseNumber", fuelReceiptModel.getFuelReceiptVehicleLicenseNumber());
        values.put("gallons", fuelReceiptModel.getFuelReceiptGallons());
        values.put("amount", fuelReceiptModel.getFuelReceiptAmount());
        values.put("salesTax", fuelReceiptModel.getFuelReceiptSalesTax());
        values.put("truckStop", fuelReceiptModel.getFuelReceiptTruckStop());
        values.put("state", fuelReceiptModel.getFuelReceiptState());
        values.put("country", fuelReceiptModel.getFuelReceiptCountry());
        values.put("fuelType", fuelReceiptModel.getFuelTypeFuelType());
        values.put("odometer", fuelReceiptModel.getFuelReceiptOdometer());

//        Dec 07, 2022  -   I think we should add rmsTimeStamp too here
        values.put("rmsTimestamp", fuelReceiptModel.getFuelReceiptRMSTimestamp());

        values.put("sent", isSent ? "1" : "0");
        db.insert("fuelreceipt", values);

        Log.d(TAG, "fuelReceipt: insertFuelReceipt: values: " + values);
        return getLastTableId("fuelreceipt");
    }


    public void recordTruckLogDrivingParameters(String objectId, String objectType, String
            logType, Activity a, String drivingStatus, String parentMobileRecordId) {
        String driverRecordId = user.getRecordId();
        String truckNumber = user.getTruckNumber();
        String date = DateUtils.getNowYyyyMmDd();
        String time = DateUtils.getNowHhmmss();

//    March 11, 2022
//    Setting Speed, Latitude and Longitude from Device GPS STARTS
//        String speed = getBleParameter("Speed");
//        String latitude = getBleParameter("Latitude");
//        String longitude = getBleParameter("Longitude");

        String speed = "" + getDeviceGPSSpeed();
        String deviceLatitude = "" + getDeviceGPSLatitude();
        String deviceLongitude = "" + getDeviceGPSLongitude();
//        Log.d(TAG, "recordTruckLogDrivingParameters: check: speed: " + speed);
//        Log.d(TAG, "recordTruckLogDrivingParameters: check: deviceLatitude: " + deviceLatitude);
//        Log.d(TAG, "recordTruckLogDrivingParameters: check: deviceLongitude: " + deviceLongitude);


        String accelerationX = "";
        String accelerationY = "";
        String accelerationZ = "";
        String engineOn = speed != null && speed.trim().length() > 0 ? "1" : "0";
        String odometer = getBleParameter("Odometer");
        String speedFromObd2OrJ1939 = getBleParameter("Speed");
        String engineHours = getBleParameter("EngineHours");
        // 09.03.2022 this should be Driving State
        String status = getBleParameter("EngineState");
//        status = "Driving";
        status = drivingStatus;


        Location loc = getPhoneLastBestLocation(a);

        if (loc != null) {
            if (deviceLatitude == null || deviceLongitude == null || deviceLatitude.equalsIgnoreCase("-1") || deviceLongitude.equalsIgnoreCase("-1")) {
                double lat = loc.getLatitude();
                double lon = loc.getLongitude();

                deviceLatitude = Double.toString(lat);
                deviceLongitude = Double.toString(lon);

                if (deviceLatitude != null)
                    deviceLatitude = deviceLatitude.replace(",", ".");

                if (deviceLongitude != null)
                    deviceLongitude = deviceLongitude.replace(",", ".");
            }

            accelerationX = lastKnownAccelerometerX != null ? Float.toString(lastKnownAccelerometerX) : null;
            accelerationY = lastKnownAccelerometerY != null ? Float.toString(lastKnownAccelerometerY) : null;
            accelerationZ = lastKnownAccelerometerZ != null ? Float.toString(lastKnownAccelerometerZ) : null;

            if (accelerationX != null)
                accelerationX = accelerationX.replace(",", ".");

            if (accelerationY != null)
                accelerationY = accelerationY.replace(",", ".");

            if (accelerationZ != null)
                accelerationZ = accelerationZ.replace(",", ".");
        }

//        recordDrivingParameters(objectId, objectType, driverRecordId, "", truckNumber, date, time, speed, latitude, longitude, accelerationX,
//                accelerationY, accelerationZ, engineOn, odometer, speedFromObd2OrJ1939, engineHours, status, "", logType);
        // 09.03.2022 add the extra parameters

        String eldLatitude = getBleParameter("Latitude");
        String eldLongitude = getBleParameter("Longitude");

//         March 16, 2022   -   Fixing the issue regarding latitude and longitude -1.000
        Double absoluteLatitude = 0.0, absoluteLongitude = 0.0;
        if (eldLatitude != null) {
            absoluteLatitude = Math.abs(Double.parseDouble(eldLatitude));
            if ((absoluteLatitude <= 1)) {
                eldLatitude = "No ELD Latitude";
            }
        } else {
            eldLatitude = "No ELD Latitude";
        }
        if (eldLongitude != null) {
            absoluteLongitude = Math.abs(Double.parseDouble(eldLongitude));
            if ((absoluteLongitude <= 1)) {
                eldLongitude = "No ELD Longitude";
            }
        } else {
            eldLongitude = "No ELD Longitude";
        }


        String speeding = "not speeding";
//        March 24, 2022    -   We should get speeding from DriveFragmentBase
        if (DriveFragmentBase.isSpeeding) {
            speeding = "speeding";
        }

//        March 11, 2022
        String trailer1 = user.getTrailerNumber();
        String trailer2 = user.getTrailerNumber2();
//        Log.d(TAG, "recordTruckLogDrivingParameters: check: trailer1: " + trailer1);
//        Log.d(TAG, "recordTruckLogDrivingParameters: check: trailer2 " + trailer2);

        recordDrivingParameters(objectId, objectType, driverRecordId, "", truckNumber, date, time, speed, deviceLatitude, deviceLongitude, accelerationX,
                accelerationY, accelerationZ, engineOn, odometer, speedFromObd2OrJ1939, engineHours, status, "", eldLatitude, eldLongitude, speeding, trailer1, trailer2, logType, parentMobileRecordId);
    }

    public void recordTruckEldDrivingParameters(Activity a, String eventStatus, String
            origin, String annotation) {
        String[] objectIdType = getTruckEldEntryForToday();

        if (objectIdType == null)
            return;

        String objectId = objectIdType[0];
        String objectType = objectIdType[1];

        if (objectId == null || objectType == null)
            return;

        String logType = "truckeld";
        String time = DateUtils.getNowHhmmss();
        String odometer = getBleParameter("Odometer");
        String engineHours = getBleParameter("EngineHours");
        String driverRecordId = user.getRecordId();

        String lat = getBleParameter("Latitude");
        String lon = getBleParameter("Longitude");

//         March 16, 2022   -   Fixing the issue regarding latitude and longitude -1.000
        Double absoluteLatitude = 0.0, absoluteLongitude = 0.0;
        if (lat != null) {
            absoluteLatitude = Math.abs(Double.parseDouble(lat));
        }
        if (lon != null) {
            absoluteLongitude = Math.abs(Double.parseDouble(lon));
        }

        if (StringUtils.isNullOrWhitespacesAny(new String[]{lat, lon}) || (absoluteLatitude <= 1 || absoluteLongitude <= 1)) {
            Location l = getPhoneLastBestLocation(a);

            if (lat != null) {
                lat = Double.toString(l.getLatitude());
            }

            if (lon != null) {
                lon = Double.toString(l.getLongitude());
            }
        }

        String locationDescription = getLocalizationStr(a, lat, lon);

        odometer = odometer != null ? odometer : "";
        engineHours = engineHours != null ? engineHours : "";
        annotation = annotation != null ? annotation : "";

        String csv = "\"" + time + "\",\"" + locationDescription + "\",\"" + odometer + "\",\"" + engineHours + "\",\"" +
                eventStatus + "\",\"" + origin + "\",\"" + annotation + "\",\"" + driverRecordId + "\"";

//        recordDrivingParameters(objectId, objectType, driverRecordId, csv, "", "", time, "", "", "", "",
//                "", "", "", odometer, "", engineHours, eventStatus, "", logType);
        recordDrivingParameters(objectId, objectType, driverRecordId, csv, "", "", time, "", "", "", "",
                "", "", "", odometer, "", engineHours, eventStatus, "", "", "", "", "", "", logType, "");

    }

    // 09.03.2022 add missing columns
    //    private void recordDrivingParameters(String objectId, String objectType, String driverRecordId, String csvline, String truckNumber, String date, String time, String speed, String latitude, String longitude, String accelerationX, String accelerationY, String accelerationZ, String engineOn, String odometer, String speedFromObd2OrJ1939, String engineHours, String status, String activeTruckRouteHeaderRecordId, String logType) {
    private void recordDrivingParameters(String objectId, String objectType, String
            driverRecordId, String csvline, String truckNumber, String date, String time, String
                                                 speed, String latitude, String longitude, String accelerationX, String accelerationY, String
                                                 accelerationZ, String engineOn, String odometer, String speedFromObd2OrJ1939, String
                                                 engineHours, String status, String activeTruckRouteHeaderRecordId, String
                                                 ELDLatitude, String ELDLongitude, String speeding, String trailer1, String trailer2, String
                                                 logType, String parentMobileRecordId) {
        Log.d(TAG, "recordDrivingParameters: parentMobileRecordId: " + parentMobileRecordId);
//        Log.d(TAG, "recordDrivingParameters: eventCode: ");
        ContentValues values = new ContentValues();

        values.put("objectId", objectId);
        values.put("objectType", objectType);
        values.put("driverRecordId", driverRecordId);
        values.put("csvline", csvline);
        values.put("truckNumber", truckNumber);
        values.put("date", date);
        values.put("time", time);
        values.put("speed", speed);
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("accelerationX", accelerationX);
        values.put("accelerationY", accelerationY);
        values.put("accelerationZ", accelerationZ);
        values.put("engineOn", engineOn);
        values.put("odometer", odometer);
        values.put("speedFromObd2OrJ1939", speedFromObd2OrJ1939);
        values.put("engineHours", engineHours);
        values.put("status", status);
        values.put("activeTruckRouteHeaderRecordId", activeTruckRouteHeaderRecordId);

        // 09.03.2022 add missing columns
        values.put("eldlatitude", ELDLatitude);
        values.put("eldlongitude", ELDLongitude);
        values.put("speeding", speeding);
        values.put("trailer1", trailer1);
        values.put("trailer2", trailer2);

        values.put("logType", logType);
        values.put("parentMobileRecordId", parentMobileRecordId);
        values.put("sent", "0");

        db.insert("contentlines", values);
    }

    public void setTruckLogHeaderEntryToUnsent(String truckLogHeaderMobileRecordId) {
        TruckLogHeader h = getTruckLogHeader(truckLogHeaderMobileRecordId);
        setTruckLogHeaderEntryToUnsent(h);
    }

    public void setTruckLogHeaderEntryToUnsent(TruckLogHeader e) {
        setTruckLogHeaderEntryToSent(e, false);
    }

    public void setTruckLogHeaderEntryToSent(TruckLogHeader e) {
        setTruckLogHeaderEntryToSent(e, true);
    }

    public void setTrailerLogEntryToSent(TrailerLog trailerLog) {
//        Log.d(TAG, "syncTrailerLogs: setTrailerLogEntryToSent: ");
        setTrailerLogEntryToSent(trailerLog, true);
    }

    public void setTollReceiptEntryToSent(TollReceiptModel tollReceiptModel) {
        Log.d(TAG, "save: syncTrailerLogs: setTrailerLogEntryToSent: ");
        setTollReceiptEntryToSent(tollReceiptModel, true);
    }

    public void setTruckLogHeaderEntryToSent(TruckLogHeader e, boolean isSent) {
        ArrayList<TruckLogHeader> truckLogHeaders = new ArrayList();
        truckLogHeaders.add(e);

        setTruckLogHeaderEntryToSent(truckLogHeaders, isSent);
    }

    public void setTrailerLogEntryToSent(TrailerLog e, boolean isSent) {
        ArrayList<TrailerLog> trailerLog = new ArrayList();
        trailerLog.add(e);

        setTrailerLogEntryToSent(trailerLog, isSent);
    }

    public void setTollReceiptEntryToSent(TollReceiptModel tollReceiptModel, boolean isSent) {
        Log.d(TAG, "save: setTollReceiptEntryToSent: ");
        ArrayList<TollReceiptModel> tollReceiptLog = new ArrayList();
        tollReceiptLog.add(tollReceiptModel);

        setTollReceiptEntryToSent(tollReceiptLog, isSent);
    }

    public void setTruckLogHeaderEntryToSent(ArrayList<TruckLogHeader> truckLogHeaders,
                                             boolean isSent) {
        if (truckLogHeaders == null)
            return;

        lock.lock();

        try {
            for (int i = 0; i < truckLogHeaders.size(); i++) {
                TruckLogHeader e = truckLogHeaders.get(i);

                ContentValues values = new ContentValues();

                values.put("sent", isSent ? "1" : "0");
                db.update("trucklogheader", values, "id=" + e.Id);
            }
        } finally {
            lock.unlock();
        }
    }


    public void setTrailerLogEntryToSent(ArrayList<TrailerLog> trailerLogs, boolean isSent) {
        if (trailerLogs == null)
            return;

        lock.lock();

        try {
            for (int i = 0; i < trailerLogs.size(); i++) {
                TrailerLog e = trailerLogs.get(i);

                ContentValues values = new ContentValues();

                values.put("sent", isSent ? "1" : "0");
                db.update("trailerlog", values, "id=" + e.Id);
            }
        } finally {
            lock.unlock();
        }
    }

    public void setTollReceiptEntryToSent(ArrayList<TollReceiptModel> tollReceiptLogs, boolean isSent) {
        Log.d(TAG, "save: setTollReceiptEntryToSent: tollReceiptLogs: " + tollReceiptLogs);
        if (tollReceiptLogs == null)
            return;

        lock.lock();

        try {
            for (int i = 0; i < tollReceiptLogs.size(); i++) {
                TollReceiptModel tollReceiptModel = tollReceiptLogs.get(i);

                ContentValues values = new ContentValues();

                values.put("sent", isSent ? "1" : "0");
                int isTollReceiptUpdated = db.update("tollreceipt", values, "id=" + tollReceiptModel.getId());
                Log.d(TAG, "save: setTollReceiptEntryToSent: isTollReceiptUpdated: " + isTollReceiptUpdated);
            }
        } finally {
            lock.unlock();
        }
    }


    public void setContentLinesToSent(ArrayList<TruckLogContentLine> contentLines) {
        if (contentLines == null)
            return;

        lock.lock();

        try {
            for (int i = 0; i < contentLines.size(); i++) {
                TruckLogContentLine l = contentLines.get(i);

                ContentValues values = new ContentValues();

                values.put("sent", "1");
                db.update("contentlines", values, "id=" + l.Id);
            }
        } finally {
            lock.unlock();
        }
    }

    public void setEldContentLinesToSent(ArrayList<TruckEldContentLine> contentLines) {
        if (contentLines == null)
            return;

        lock.lock();

        try {
            for (int i = 0; i < contentLines.size(); i++) {
                TruckEldContentLine l = contentLines.get(i);

                ContentValues values = new ContentValues();

                values.put("sent", "1");
                db.update("contentlines", values, "id=" + l.Id);
            }
        } finally {
            lock.unlock();
        }
    }

    public void setTruckLogDetailEntryToSent(ArrayList<TruckLogDetail> truckLogDetails) {
//        Log.d(TAG, "setTruckLogDetailEntryToSent: ");
        if (truckLogDetails == null)
            return;

        lock.lock();


        try {
//            Log.d(TAG, "setTruckLogDetailEntryToSent: truckLogDetails: size: " + truckLogDetails.size());
            for (int i = 0; i < truckLogDetails.size(); i++) {
                TruckLogDetail e = truckLogDetails.get(i);

                ContentValues values = new ContentValues();
                values.put("sent", "1");
//                values.put("endDateTime", DateUtils.getNowYyyyMmDdHhmmss());

//                Log.d(TAG, "setTruckLogDetailEntryToSent: endDateTime: " + e.EndDateTime);
//                e.EndDateTime = DateUtils.getNowYyyyMmDdHhmmss();
//                Log.d(TAG, "setTruckLogDetailEntryToSent: endDateTime: " + e.EndDateTime);

//                Log.d(TAG, "setTruckLogDetailEntryToSent: id: " + e.Id);
                db.update("trucklogdetail", values, "id=" + e.Id);
            }
        } finally {
            lock.unlock();
        }
    }

    public ArrayList<String[]> getPendingTruckLogContentLinesGroups() {
        try {
            ArrayList<String[]> result = new ArrayList();
            Cursor c = db.getQuery("SELECT objectId, objectType FROM contentlines WHERE logType='trucklog' AND sent=0 GROUP BY objectId, objectType");

            if (c == null)
                return null;

            c.moveToFirst();
            int counter = 0;

            while (!c.isAfterLast()) {
                String objectId = c.getString(counter++);
                String objectType = c.getString(counter++);

                result.add(new String[]{objectId, objectType});

                c.moveToNext();
                counter = 0;
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public ArrayList<TruckLogContentLine> getPendingTruckLogContentLines() {
        try {
            ArrayList<String[]> groups = getPendingTruckLogContentLinesGroups();

            if (groups == null)
                return null;

            String objectId = groups.get(0)[0];
            String objectType = groups.get(0)[1];

            ArrayList<TruckLogContentLine> result = new ArrayList();

            Cursor c = db.getQuery("SELECT * FROM contentlines WHERE (sent=0 OR sent IS NULL) AND objectId='" + objectId +
                    "' AND objectType='" + objectType + "' ORDER BY creationdate ASC");

            if (c == null)
                return null;

            c.moveToFirst();
            int counter = 0;

            while (!c.isAfterLast()) {
                TruckLogContentLine contentLine = new TruckLogContentLine();
                counter = 0;

                contentLine.Id = Integer.toString(c.getInt(counter++));
                contentLine.ObjectId = c.getString(counter++);
                contentLine.ObjectType = c.getString(counter++);
                contentLine.DriverRecordId = c.getString(counter++);
                contentLine.Csvline = c.getString(counter++);
                contentLine.TruckNumber = c.getString(counter++);
                contentLine.Date = c.getString(counter++);
                contentLine.Time = c.getString(counter++);
                contentLine.Speed = c.getString(counter++);
                contentLine.Latitude = c.getString(counter++);
                contentLine.Longitude = c.getString(counter++);
                contentLine.AccelerationX = c.getString(counter++);
                contentLine.AccelerationY = c.getString(counter++);
                contentLine.AccelerationZ = c.getString(counter++);
                contentLine.EngineOn = c.getString(counter++);
                contentLine.Odometer = c.getString(counter++);
                contentLine.SpeedFromObd2OrJ1939 = c.getString(counter++);
                contentLine.EngineHours = c.getString(counter++);
                contentLine.Status = c.getString(counter++);
                contentLine.ActiveTruckRouteHeaderRecordId = c.getString(counter++);
                contentLine.ELDLatitude = c.getString(counter++);
                contentLine.ELDLongitude = c.getString(counter++);
                contentLine.Speeding = c.getString(counter++);
                contentLine.Trailer1 = c.getString(counter++);
                contentLine.Trailer2 = c.getString(counter++);
                contentLine.LogType = c.getString(counter++);
                contentLine.Sent = c.getShort(counter++) == 1;
                contentLine.CreationDate = c.getString(counter++);

                result.add(contentLine);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    private void setTruckLogHeaderToClosed(TruckLogHeader h) {
//        Log.d(TAG, "endCycle: setTruckLogHeaderToClosed: ");
        lock.lock();

        try {
//            March 21, 2022    -   We should reset the currentTruckLogHeaderCreationDateTime for the current log
            currentTruckLogHeaderCreationDateTime = null;
            ContentValues values = new ContentValues();

            values.put("endDate", DateUtils.getNowYyyyMmDd());
            values.put("endTime", DateUtils.getNowHhmmss());
            values.put("active", "0");
            values.put("sent", "0");

//            Log.d(TAG, "endCycle: setTruckLogHeaderToClosed: values: " + values);
            db.update("trucklogheader", values, "id=" + h.Id);
        } catch (Throwable throwable) {
            Log.d(TAG, "endCycle: setTruckLogHeaderToClosed: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();
        } finally {
            lock.unlock();
        }
    }


    //    May 04, 2022  -
    public void updateOdometerForTruckEldDetail(TruckEldDetail truckEldDetail, String
            odometer, String engineHours) {
//        Log.d(TAG, "EldTruckLogLocalDataProcessor: updateOdometerForTruckEldDetail: odometer: " + odometer + " engineHours: " + engineHours);
        if (truckEldDetail == null) {
            return;
        }

        lock.lock();

        try {
            boolean needsToSave = false;

            ContentValues values = new ContentValues();
            if (odometer != null && !odometer.isEmpty()) {

                needsToSave = true;
                if (truckEldDetail.OdometerStart == null || truckEldDetail.OdometerStart.isEmpty()) {
                    values.put("odometerStart", odometer);
                    truckEldDetail.OdometerStart = odometer;
                }

                values.put("odometerEnd", odometer);
                truckEldDetail.OdometerEnd = odometer;
            }

            if (engineHours != null && !engineHours.isEmpty()) {

                needsToSave = true;
                if (truckEldDetail.EngineHoursStart == null || truckEldDetail.EngineHoursStart.isEmpty()) {
                    values.put("engineHoursStart", engineHours);
                    truckEldDetail.EngineHoursStart = engineHours;
                }

                values.put("engineHoursEnd", engineHours);
                truckEldDetail.EngineHoursEnd = engineHours;
            }

            if (needsToSave) {
                values.put("sent", "0");
                db.update("truckelddetail", values, "id=" + truckEldDetail.Id);
//                Log.d(TAG, "EldTruckLogLocalDataProcessor: updateOdometerForTruckEldDetail: mobileRecordId: " + truckEldDetail.MobileRecordId + " TruckEldDetailId: " + truckEldDetail.Id);
            }
        } catch (Throwable throwable) {
            Log.d(TAG, "endCycle: setTruckLogHeaderToClosed: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    private void setTruckLogDetailEndDateTime(TruckLogDetail d) {
//        Log.d(TAG, "endCycle: setTruckLogDetailEndDateTime: TruckLogDetail: " + d);
        lock.lock();

        try {
            ContentValues values = new ContentValues();
            values.put("endDateTime", DateUtils.getNowYyyyMmDdHhmmss());
            values.put("endTime", DateUtils.getNowHhmmss());
            values.put("sent", "0");
//            values.put("active", "TRUE");


//            Log.d(TAG, "endCycle: setTruckLogDetailEndDateTime: values: " + values);
            db.update("trucklogdetail", values, "id=" + d.Id);
        } finally {
            lock.unlock();
        }
    }

    public void endDriverCurrentCycle() {
//        Log.d(TAG, "endCycle: endDriverCurrentCycle: ");
        TruckLogHeader h = getOpenTruckLogHeader();
//        Log.d(TAG, "endCycle: endDriverCurrentCycle: h: " + h);

        if (h == null)
            return;

        setTruckLogHeaderToClosed(h);

        ArrayList<TruckLogDetail> details = h.getTruckLogDetails();
//        Log.d(TAG, "endCycle: endDriverCurrentCycle: details: " + details.size());

        if (details == null)
            return;

        for (TruckLogDetail d : details)
            setTruckLogDetailEndDateTime(d);
    }
    //endregion

    //region Truck ELDs management
    private ArrayList<TruckEldHeader> truckElds;

    public boolean existsTruckEldHeaderEntry(String truckLogHeaderMobileRecordId) {
        return db.exists("SELECT * FROM truckeldheader WHERE truckLogHeaderRecordId='" + truckLogHeaderMobileRecordId + "'");
    }

    public boolean existsTruckEldDetailEntry(String truckLogDetailMobileRecordId, String truckNumber) {
        if (truckNumber != null && !truckNumber.isEmpty()) {
            return db.exists("SELECT * FROM truckelddetail WHERE truckLogDetailRecordId='"
                    + truckLogDetailMobileRecordId + "' AND truckNumber='" + truckNumber + "'");
        } else {
            return db.exists("SELECT * FROM truckelddetail WHERE truckLogDetailRecordId='" + truckLogDetailMobileRecordId + "'");
        }
    }

    public Truck getTruck(String truckNumber) {
        Cursor c = db.getQuery("SELECT * FROM trucks WHERE truckNumber='" + truckNumber + "'");
        c.moveToFirst();

        Truck result = new Truck();
        int counter = 0;

        if (!c.isAfterLast()) {
            counter++;
            result.RecordId = c.getString(counter++);
            result.MobileRecordId = c.getString(counter++);
            result.VIN = c.getString(counter++);
            result.VehicleLicenseNumber = c.getString(counter++);
            result.TruckNumber = c.getString(counter++);

            result.RmsCodingTimestamp = c.getString(counter++);
            result.RmsTimestamp = c.getString(counter++);
            result.OrganizationName = c.getString(counter++);
            result.OrganizationNumber = c.getString(counter++);
            result.ContainerTypeRecordId = c.getString(counter++);
            result.UniqueKey1 = c.getString(counter++);
            result.Deployment = c.getString(counter++);
            result.IsaLocation = c.getString(counter++);
            result.CustomerRecordId = c.getString(counter++);
            result.ManufacturerSerialNumber = c.getString(counter++);
            result.Overdue = c.getString(counter++);
            result.StoreName = c.getString(counter++);
            result.StoreNumber = c.getString(counter++);
            result.StoreType = c.getString(counter++);
            result.DateLoaded = c.getString(counter++);
            result.DateUnloaded = c.getString(counter++);
            result.TimeLoaded = c.getString(counter++);
            result.TimeUnloaded = c.getString(counter++);
            result.FunctionalGroupName = c.getString(counter++);
            result.Managers = c.getString(counter++);
            result.FunctionalGroupObjectId = c.getString(counter++);
            result.DateDoorClosed = c.getString(counter++);
            result.TimeDoorClosed = c.getString(counter++);
            result.DateTime = c.getString(counter++);
            result.AlertCount = c.getString(counter++);
            result.VendorName = c.getString(counter++);
            result.Tracking = c.getString(counter++);
            result.ItemType = c.getString(counter++);
            result.Humidity = c.getString(counter++);
            result.GPSDailySampleRate = c.getString(counter++);
            result.GPSTripSampleRate = c.getString(counter++);
            result.TruckGPSServices = c.getString(counter++);
            result.FromLatitude = c.getString(counter++);
            result.FromLongitude = c.getString(counter++);
            result.ToLatitude = c.getString(counter++);
            result.ToLongitude = c.getString(counter++);
            result.DepartureDate = c.getString(counter++);
            result.DepartureTime = c.getString(counter++);
            result.EstimatedTimeEnroute = c.getString(counter++);
            result.EstimatedTimeOfArrival = c.getString(counter++);
            result.ActualTimeEnroute = c.getString(counter++);
            result.ActualTimeOfArrival = c.getString(counter++);
            result.TravelDangerAlertTime = c.getString(counter++);
            result.TravelWarningAlertTime = c.getString(counter++);
            result.OverdueTimeLimit = c.getString(counter++);
            result.SalesOrderNumber = c.getString(counter++);
            result.FromAddress = c.getString(counter++);
            result.FromCity = c.getString(counter++);
            result.FromState = c.getString(counter++);
            result.FromZipcode = c.getString(counter++);
            result.ToAddress = c.getString(counter++);
            result.ToCity = c.getString(counter++);
            result.ToState = c.getString(counter++);
            result.ToZipcode = c.getString(counter++);
            result.Make = c.getString(counter++);
            result.Mack = c.getString(counter++);
            result.Model = c.getString(counter++);
            result.Year = c.getString(counter++);
            result.LastWorked = c.getString(counter++);
            result.Country = c.getString(counter++);
            result.StateRegion = c.getString(counter++);
            result.City = c.getString(counter++);
            result.Company = c.getString(counter++);
            result.FirstName = c.getString(counter++);
            result.LastName = c.getString(counter++);
            result.MobilePhone = c.getString(counter++);
            result.Latitude = c.getString(counter++);
            result.Longitude = c.getString(counter++);
            result.Active = c.getString(counter++);
            result.Speed = c.getString(counter++);
            result.Temperature1 = c.getString(counter++);
            result.Temperature2 = c.getString(counter++);
            result.DoorStatus = c.getString(counter++);
            result.Heading = c.getString(counter++);
            result.Odometer = c.getString(counter++);
            result.EngineIdle = c.getString(counter++);
            result.RPM = c.getString(counter++);
            result.EngineHours = c.getString(counter++);
            result.OdometerSetup = c.getString(counter++);
            result.Shock = c.getString(counter++);
            result.TirePressures = c.getString(counter++);
            result.EngineStatus = c.getString(counter++);
            result.HOSViolation = c.getString(counter++);
            result.SpeedViolations = c.getString(counter++);
            result.SmogViolation = c.getString(counter++);
            result.LastMaintenanceDate = c.getString(counter++);
            result.LastAnnualInspectionDate = c.getString(counter++);
            result.Status = c.getString(counter++);
            result.CustomerName = c.getString(counter++);
            result.CustomerNumber = c.getString(counter++);
            result.FuelCode = c.getString(counter++);
            result.FuelType = c.getString(counter++);
            result.Diesel = c.getString(counter++);
            result.DOTNumber = c.getString(counter++);
            result.DOTExpirationDate = c.getString(counter++);
            result.IFTADecal = c.getString(counter++);
            result.IFTAFuelPermits = c.getString(counter++);
            result.IFTATripPermits = c.getString(counter++);
        }

        c.close();
        return result;
    }

    public Trailer getTrailer(String trailerNumber) {

        if (trailerNumber == null || trailerNumber.isEmpty()) {
            return null;
        }

        Cursor c = db.getQuery("SELECT * FROM trailers WHERE trailerNumber='" + trailerNumber + "'");
        c.moveToFirst();

        Trailer result = new Trailer();
        int counter = 0;

        if (!c.isAfterLast()) {
            counter++;
            result.RecordId = c.getString(counter++);
            result.MobileRecordId = c.getString(counter++);
            result.VIN = c.getString(counter++);
            result.TrailerNumber = c.getString(counter++);
            result.TruckNumber = c.getString(counter++);

            result.Make = c.getString(counter++);
            result.Model = c.getString(counter++);
            result.Year = c.getString(counter++);
            result.LastWorked = c.getString(counter++);
            result.Country = c.getString(counter++);
            result.StateRegion = c.getString(counter++);
            result.City = c.getString(counter++);
            result.Company = c.getString(counter++);
            result.ProcessorId = c.getString(counter++);
            result.ProcessorInstalled = c.getString(counter++);
            result.Latitude = c.getString(counter++);
            result.Longitude = c.getString(counter++);
            result.Active = c.getString(counter++);
            result.Speed = c.getString(counter++);
            result.Temperature1 = c.getString(counter++);
            result.Temperature2 = c.getString(counter++);
            result.Humidity = c.getString(counter++);
            result.DoorStatus = c.getString(counter++);
            result.Heading = c.getString(counter++);
            result.Miles = c.getString(counter++);
            result.FuelRate = c.getString(counter++);
            result.Shock = c.getString(counter++);
            result.TirePressures = c.getString(counter++);
            result.ItemType = c.getString(counter++);
            result.Treads = c.getString(counter++);
            result.LastMaintenanceDate = c.getString(counter++);
            result.LastAnnualInspectionDate = c.getString(counter++);
            result.Status = c.getString(counter++);
            result.CustomerName = c.getString(counter++);
            result.CustomerNumber = c.getString(counter++);
            result.IsReefer = c.getString(counter++);
            result.ReeferHOS = c.getString(counter++);
            result.ReportingPeriod = c.getString(counter++);
            result.HOSViolation = c.getString(counter++);
            result.HasCamera = c.getString(counter++);
            result.HasTracker = c.getString(counter++);
            result.RecordReeferHOS = c.getString(counter++);
        }

        c.close();
        return result;
    }

    //      July 07, 2022   -   We should record reefer HOS only if it sets for trailer
    public boolean isReeferForTrailer(String trailerNumber) {

        if (trailerNumber == null || trailerNumber.isEmpty()) {
            return false;
        }
        Trailer trailer = getTrailer(trailerNumber);
        if (trailer == null) {
            return false;
        }

        if (trailer.RecordReeferHOS.isEmpty()) {
            return false;
        }

        if (trailer.RecordReeferHOS.toLowerCase().equalsIgnoreCase("yes")) {
            return true;
        }
        return false;
    }


    public TruckEldHeader getOpenTruckEldHeader() {
        Cursor c = db.getQuery("SELECT * FROM truckeldheader WHERE creationDate IS NOT NULL AND driverRecordId='" + user.getRecordId() + "' AND sent=0");

        if (c == null)
            return null;

        c.moveToFirst();
        TruckEldHeader h = new TruckEldHeader();
        int couunter = 0;

        if (!c.isAfterLast()) {
            h.Id = Integer.toString(c.getInt(couunter++));
            h.objectId = c.getString(couunter++);
            h.objectType = c.getString(couunter++);
            h.CreationDate = c.getString(couunter++);
            h.CreationDatetime = c.getString(couunter++);
            h.CreationTime = c.getString(couunter++);
            h.RecordId = c.getString(couunter++);
            h.MasterBarcode = c.getString(couunter++);
            h.RmsTimestamp = c.getString(couunter++);
            h.RmsCodingTimestamp = c.getString(couunter++);
            h.FunctionalGroupName = c.getString(couunter++);
            h.FunctionalGroupObjectId = c.getString(couunter++);
            h.MobileRecordId = c.getString(couunter++);
            h.CreatorFirstName = c.getString(couunter++);
            h.CreatorLastName = c.getString(couunter++);
            h.CreatorRecordId = c.getString(couunter++);
            h.OrganizationName = c.getString(couunter++);
            h.OrganizationNumber = c.getString(couunter++);
            h.VehicleLicenseNumber = c.getString(couunter++);
            h.CycleStartDateTime = c.getString(couunter++);
            h.Rule = c.getString(couunter++);
            h.DriverName = c.getString(couunter++);
            h.DriverRecordId = c.getString(couunter++);
            h.DriverId = c.getString(couunter++);
            h.CoDriverName = c.getString(couunter++);
            h.CoDriverRecordId = c.getString(couunter++);
            h.CoDriverId = c.getString(couunter++);
            h.TruckLogHeaderRecordId = c.getString(couunter++);
            h.TruckNumber = c.getString(couunter++);
            h.Trailer1Number = c.getString(couunter++);
            h.Trailer2Number = c.getString(couunter++);
            h.Sent = c.getShort(couunter++) == 1;

            Cursor c2 = db.getQuery("SELECT id FROM truckelddetail WHERE truckeldheaderid=" + h.Id + "");

            if (c2 != null) {
                c2.moveToFirst();

                while (!c2.isAfterLast()) {
                    int detailId = c2.getInt(0);
                    TruckEldDetail d = getTruckEldDetail(detailId);

                    h.addDetail(d);
                    c2.moveToNext();
                }

                c2.close();
            }

            return h;
        }

        return null;
    }

    public TruckEldHeader getTruckEldHeader(int id) {
        Cursor c = db.getQuery("SELECT * FROM truckeldheader WHERE id=" + id);

        if (c == null)
            return null;

        c.moveToFirst();
        TruckEldHeader h = new TruckEldHeader();
        int counter = 0;

        if (!c.isAfterLast()) {
            h.Id = Integer.toString(c.getInt(counter++));
            h.objectId = c.getString(counter++);
            h.objectType = c.getString(counter++);
            h.CreationDate = c.getString(counter++);
            h.CreationDatetime = c.getString(counter++);
            h.CreationTime = c.getString(counter++);
            h.RecordId = c.getString(counter++);
            h.MasterBarcode = c.getString(counter++);
            h.RmsTimestamp = c.getString(counter++);
            h.RmsCodingTimestamp = c.getString(counter++);
            h.FunctionalGroupName = c.getString(counter++);
            h.FunctionalGroupObjectId = c.getString(counter++);
            h.MobileRecordId = c.getString(counter++);
            h.CreatorFirstName = c.getString(counter++);
            h.CreatorLastName = c.getString(counter++);
            h.CreatorRecordId = c.getString(counter++);
            h.OrganizationName = c.getString(counter++);
            h.OrganizationNumber = c.getString(counter++);
            h.VehicleLicenseNumber = c.getString(counter++);
            h.CycleStartDateTime = c.getString(counter++);
            h.Rule = c.getString(counter++);
            h.DriverName = c.getString(counter++);
            h.DriverRecordId = c.getString(counter++);
            h.DriverId = c.getString(counter++);
            h.CoDriverName = c.getString(counter++);
            h.CoDriverRecordId = c.getString(counter++);
            h.CoDriverId = c.getString(counter++);
            h.TruckLogHeaderRecordId = c.getString(counter++);
            h.TruckNumber = c.getString(counter++);
            h.Trailer1Number = c.getString(counter++);
            h.Trailer2Number = c.getString(counter++);
            h.Sent = c.getShort(counter++) == 1;

            Cursor c2 = db.getQuery("SELECT id FROM truckelddetail WHERE truckEldHeaderId=" + h.Id + "");

            if (c2 != null) {
                c2.moveToFirst();

                while (!c2.isAfterLast()) {
                    int detailId = c2.getInt(0);
                    TruckEldDetail d = getTruckEldDetail(detailId);

                    h.addDetail(d);
                    c2.moveToNext();
                }

                c2.close();
            }

            return h;
        }

        return null;
    }

    public TruckEldHeader getTruckEldHeader(String truckLogHeaderMobileRecordId) {
        Cursor c = db.getQuery("SELECT id FROM truckeldheader WHERE truckLogHeaderRecordId='" + truckLogHeaderMobileRecordId + "'");

        if (c == null)
            return null;

        c.moveToFirst();
        int id = -1;

        if (!c.isAfterLast())
            id = c.getInt(0);

        c.close();
        return getTruckEldHeader(id);
    }

    public TruckEldDetail getLastTruckEldDetailByCreationDate(String recordDate) {
        Cursor c = db.getQuery("SELECT * FROM truckelddetail WHERE recordDate LIKE '" + recordDate + "%' ORDER BY Id DESC LIMIT 1");

        if (c == null)
            return null;

        c.moveToFirst();
        TruckEldDetail d = new TruckEldDetail();
        int counter = 0;

        if (!c.isAfterLast()) {
            d.Id = Integer.toString(c.getInt(counter++));
            d.objectId = c.getString(counter++);
            d.objectType = c.getString(counter++);
            d.MasterBarcode = c.getString(counter++);
            d.CreationDatetime = c.getString(counter++);
            d.CreationDate = c.getString(counter++);
            d.CreationTime = c.getString(counter++);
            d.RecordId = c.getString(counter++);
            d.RmsTimestamp = c.getString(counter++);
            d.RmsCodingTimestamp = c.getString(counter++);
            d.RmsEfileTimestamp = c.getString(counter++);
            d.FunctionalGroupName = c.getString(counter++);
            d.FunctionalGroupObjectId = c.getString(counter++);
            d.MobileRecordId = c.getString(counter++);
            d.OrganizationName = c.getString(counter++);
            d.OrganizationNumber = c.getString(counter++);
            d.TwentyFourHourPeriodStartingTime = c.getString(counter++);
            d.CarrierName = c.getString(counter++);
            d.UsDotNumber = c.getString(counter++);
            d.DriverName = c.getString(counter++);
            d.DriverId = c.getString(counter++);
            d.DriverRecordId = c.getString(counter++);
            d.CoDriverName = c.getString(counter++);
            d.CoDriverId = c.getString(counter++);
            d.CoDriverRecordId = c.getString(counter++);
            d.CurrentLocation = c.getString(counter++);
            d.DataDiagnosticsIndicators = c.getString(counter++);
            d.EldMalfunctionIndicators = c.getString(counter++);
            d.EldManufacturerName = c.getString(counter++);
            d.EldRegistrationId = c.getString(counter++);
            d.UnidentifiedDriverRecords = c.getString(counter++);
            d.ExemptDriverStatus = c.getString(counter++);
            d.MilesToday = c.getString(counter++);
            d.PrintDisplayDate = c.getString(counter++);
            d.RecordDate = c.getString(counter++);
            d.ShippingId = c.getString(counter++);
            d.CurrentEngineHours = c.getString(counter++);
            d.EngineHoursStart = c.getString(counter++);
            d.EngineHoursEnd = c.getString(counter++);
            d.CurrentOdometer = c.getString(counter++);
            d.OdometerStart = c.getString(counter++);
            d.OdometerEnd = c.getString(counter++);
            d.TimeZone = c.getString(counter++);
            d.TruckNumber = c.getString(counter++);
            d.TruckVin = c.getString(counter++);
            d.TrailerNumber = c.getString(counter++);
            d.TruckLogDetailRecordId = c.getString(counter++);
            d.OffDutyHours = c.getString(counter++);
            d.SleeperHours = c.getString(counter++);
            d.DrivingHours = c.getString(counter++);
            d.OnDutyHours = c.getString(counter++);
            d.Status = c.getString(counter++);
            d.Comments = c.getString(counter++);
            d.Rule = c.getString(counter++);
            d.CycleStartDateTime = c.getString(counter++);
            d.VehicleLicenseNumber = c.getString(counter++);
            d.DataCheckValue = c.getString(counter++);
            d.CertifyDateTime = c.getString(counter++);
            d.CmvPowerUnitNumber = c.getString(counter++);
            d.Sent = c.getShort(counter++) == 1;
            d.TruckEldHeaderId = c.getString(counter++);

            return d;
        }

        return null;
    }

    public String getLastShiftStartDate() {

        String lastDate = null;
//        June 08, 2022 -   we need to make sure that we are getting shift started from duty event , This is a double check
//        Cursor cursor = db.getQuery("SELECT creationdate FROM eldevents WHERE shiftstart = 1 ORDER BY creationDate DESC LIMIT 1");
//        Cursor cursor = db.getQuery("SELECT creationdate FROM eldevents WHERE shiftstart=1 AND eventType=1 AND eventCode=4 ORDER BY creationDate DESC LIMIT 1");
        Cursor cursor = db.getQuery("SELECT creationdate FROM eldevents WHERE shiftstart=1 AND eventType=1 AND eventCode=4 ORDER BY eventSeconds DESC LIMIT 1");
        Log.d(TAG, "calculateHours: getLastShiftStartDate: cursor: " + cursor);

        if (cursor != null) {
            Log.d(TAG, "calculateHours: getLastShiftStartDate: cursor: count: " + cursor.getCount());
        }

        if (cursor.moveToFirst()) {
            do {
                lastDate = cursor.getString(0);
                Log.d(TAG, "calculateHours: getLastShiftStartDate: lastDate: " + lastDate);
            } while (cursor.moveToNext());
        }
        cursor.close();

        Log.d(TAG, "calculateHours: getLastShiftStartDate: lastDate: " + lastDate + " isLastDate: " + (lastDate != null));
        if (lastDate != null) {
            Log.d(TAG, "calculateHours: getLastShiftStartDate: lastDate not null");

//
//            Nov 09, 2022  -   Format was wrong
//            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:sss");
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            Date date = null;
            try {
                date = format.parse(lastDate);
                Log.d(TAG, "calculateHours: getLastShiftStartDate: date: " + date);
            } catch (ParseException e) {
                Log.d(TAG, "calculateHours: getLastShiftStartDate: exception: " + e.getMessage());
            }
//            Nov 15, 2022  -   App crashed
            if (date == null) {
                return null;
            }

            Log.d(TAG, "calculateHours: getLastShiftStartDate: date: " + date);
            Long currentTimeStamp = DateUtils.getTimestamp();
            Long delta = currentTimeStamp - (date.getTime() / 1000);
            Log.d(TAG, "calculateHours: getLastShiftStartDate: currentTimeStamp: " + currentTimeStamp + " delta: " + delta);

            if (delta >= (24 * 60 * 60)) {
                Log.d(TAG, "calculateHours: getLastShiftStartDate: is Delta greater than 24 hours in seconds: true");
//                Nov 09, 2022  -   We commented this for making a better debugging between ios and android
//                lastDate = null;
            }
        }

        Log.d(TAG, "calculateHours: getLastShiftStartDate: before returning lastdate: " + lastDate);
        return lastDate;
    }

    public List<EldEvent> getAllDutyEventAfterDate(String creationdate) {
        Log.d(TAG, "calculateHours: getAllDutyEventAfterDate: creationdate: " + creationdate);
//        June 13, 2022 -   We should return null if the creation date is null
//        This might be caused by the fact that we have some days when we don't have activity
        if (creationdate == null || creationdate.isEmpty()) {
            return new ArrayList<>();
        }

        List<EldEvent> events = new ArrayList<>();
        Cursor cursor = db.getQuery("SELECT creationdate, eventseconds, odometer, eventCode, shiftstart, mobileRecordId, recordId, eventType, vin FROM eldevents WHERE creationdate >= '" + creationdate + "' AND eventType==1 ORDER BY creationDate");
        Log.d(TAG, "calculateHours: getAllDutyEventAfterDate: cursor: size: " + cursor.getCount() + " creationDate: " + creationdate);
        if (cursor.moveToFirst()) {
            do {
                EldEvent event = new EldEvent();
                event.CreationDate = cursor.getString(0);
                event.EventSeconds = cursor.getDouble(1);
                event.Odometer = cursor.getString(2);
                event.EventCode = cursor.getString(3);
                event.ShiftStart = cursor.getString(4);
                event.MobileRecordId = cursor.getString(5);
                event.RecordId = cursor.getString(6);
                event.EventType = cursor.getString(7);
                event.Vin = cursor.getString(8);
                events.add(event);
            } while (cursor.moveToNext());
        }
        cursor.close();

        return events;
    }

    public String getLastSavedOdometer(String creationdate) {
        List<EldEvent> events = new ArrayList<>();
        Cursor cursor = db.getQuery("SELECT odometer FROM eldevents WHERE creationdate >= '" + creationdate + "' ORDER BY creationDate DESC LIMIT 1");

        String odometer = null;
        if (cursor.moveToFirst()) {
            odometer = cursor.getString(0);
        }
        cursor.close();
        return odometer;
    }


    public double getOdometerSinceShiftStartedFromDB() {
        Cursor cursor = db.getQuery("SELECT odometer FROM eldevents WHERE shiftstart=1 AND eventType=1 AND eventCode=4 ORDER BY eventSeconds DESC LIMIT 1");

        String odometer = null;
        if (cursor.moveToFirst()) {
            odometer = cursor.getString(0);
        }
        cursor.close();

        if (odometer == null) {
            return -1;
        } else {
            return Double.parseDouble(odometer);
        }
    }

    public double getOdometerLastValueFromDBForTruck(String truckVIN) {
        String sqlQuery = "";
        if (truckVIN == null || truckVIN.isEmpty()) {
            sqlQuery = "SELECT odometer FROM eldevents WHERE eventType=1 ORDER BY eventSeconds DESC LIMIT 1";
        } else {
            sqlQuery = "SELECT odometer FROM eldevents WHERE eventType=1 AND vin=" + truckVIN + " ORDER BY eventSeconds DESC LIMIT 1";
        }
        Log.d(TAG, "getOdometerLastValueFromDBForTruck: sqlQuery: " + sqlQuery);
        Cursor cursor = db.getQuery(sqlQuery);

        String odometer = null;
        if (cursor.moveToFirst()) {
            odometer = cursor.getString(0);
        }
        cursor.close();

        if (odometer == null) {
            return -1;
        } else {
            return Double.parseDouble(odometer);
        }
    }

    //    Aug 09, 2022  -   Its returning the most recent odometer that is greater than 0
    public double getMostRecentOdometerFromDB() {
        Cursor cursor = db.getQuery("SELECT odometer FROM eldevents WHERE odometer!=0 ORDER BY eventSeconds DESC LIMIT 1");

        String odometer = null;
        if (cursor.moveToFirst()) {
            odometer = cursor.getString(0);
        }
        cursor.close();

        if (odometer == null) {
            return -1;
        } else {
            return Double.parseDouble(odometer);
        }
    }


    public HashMap<String, String> calculateDrivingMilesDeltaInfo() {
        String lastEventDate = getLastShiftStartDate();
        Log.d(TAG, "calculateDrivingMilesDeltaInfo: lastEventDate: " + lastEventDate);
        if (lastEventDate == null) {
            return null;
        }

//    Oct 28, 2022  -   we are getting all the duty events since shift started
        List<EldEvent> todayEventList = getAllDutyEventAfterDate(lastEventDate);
        Log.d(TAG, "calculateDrivingMilesDeltaInfo: todayEventList: " + todayEventList.size());
        double delta = 0;
        double startOdometer = 0;
        String lastVin = null;

        HashMap<String, String> todayEventsHashMap = new HashMap<>();

        for (int i = 0; i < todayEventList.size(); i++) {

            EldEvent eldEvent = todayEventList.get(i);
            EldEvent eldEventNext = null;
//    Oct 28, 2022  -   we are getting the next event so we can compare the odometers
            if ((i + 1) < todayEventList.size()) {
                eldEventNext = todayEventList.get(i + 1);
            }

            if (startOdometer == 0) {
                startOdometer = Double.parseDouble(eldEvent.Odometer);
            }
            Log.d(TAG, "calculateDrivingMilesDeltaInfo: eldEvent: " + eldEvent);

//    Oct 28, 2022  -   we changed the truck. We need to calculate delta and we need to "reset start odometer"
            if (eldEventNext != null
                    && eldEvent.Vin != null
                    && eldEventNext.Vin != null
                    && !(eldEvent.Vin.equals(eldEventNext.Vin))) {
                double odometer = Double.parseDouble(eldEvent.Odometer);
                delta += odometer - startOdometer;
                Log.d(TAG, "calculateDrivingMilesDeltaInfo: delta: " + delta);
                startOdometer = Double.parseDouble(eldEventNext.Odometer);
                Log.d(TAG, "calculateDrivingMilesDeltaInfo: startOdometer: " + startOdometer);
                lastVin = eldEventNext.Vin;
            }
        }

        todayEventsHashMap.put("delta", "" + delta);
        todayEventsHashMap.put("startOdometer", "" + startOdometer);
        if (lastVin != null) {
            todayEventsHashMap.put("lastVin", "" + lastVin);
        }


        return todayEventsHashMap;
    }

    //    public String getTruckLogHeaderDate() {
    public String getTruckLogHeaderDrivingRuleHours() {

        String hours = null;
        Cursor cursor = db.getQuery("SELECT hours FROM rules WHERE name = (SELECT rule FROM trucklogheader ORDER BY creationDate DESC LIMIT 1)");
        if (cursor.moveToFirst()) {
            hours = cursor.getString(0);
//            Log.d(TAG, "getTruckLogHeaderDrivingRuleHours: hours: " + hours);
        }

        cursor.close();
//        Log.d(TAG, "getTruckLogHeaderDrivingRuleHours: return hours: " + hours);
        return hours;
    }

    public String getStartShiftDate() {

        String createdDate = null;
        Cursor cursor = db.getQuery("SELECT creationdate FROM eldevents WHERE shiftstart =1 ORDER BY creationDate DESC LIMIT 1");
        if (cursor.moveToFirst()) {
            createdDate = cursor.getString(0);
        }

        cursor.close();
        return createdDate;
    }

    public List<LogBookELDEvent> getAllEldEventByDate(String CreationDate) {

        List<LogBookELDEvent> results = new ArrayList<>();
        Cursor cursor = db.getQuery("SELECT id, creationdate, localizationDescription, vehicleMiles, engineHours, eventCodeDescription," +
                " recordOrigin, eventType, eventCode, annotation, recordStatus, shiftstart FROM eldevents WHERE creationDate LIKE '" + CreationDate + "%' ORDER BY creationDate");

        if (cursor.moveToFirst()) {

            int counter = 0;
            do {
                LogBookELDEvent logBookELDEvent = new LogBookELDEvent();

                logBookELDEvent.setId(cursor.getInt(counter++));
                String creationDate = cursor.getString(counter++);

                String formattedTime = DateUtils.convertDateTime(creationDate, DateUtils.FORMAT_DATE_TIME_MILLIS, DateUtils.FORMAT_DATE_HH_MM);
                logBookELDEvent.setTime(formattedTime);

                logBookELDEvent.setLocation(cursor.getString(counter++));
                logBookELDEvent.setOdometer(cursor.getString(counter++));
                logBookELDEvent.setEngHours(cursor.getString(counter++));
                logBookELDEvent.setEventTypeDescription(cursor.getString(counter++));

                String origin = cursor.getString(counter++);
                String formattedOrigin = "";
                if (origin != null)
                    formattedOrigin = origin.equals("1") ? "Auto" : "Driver";

                logBookELDEvent.setOrigin(formattedOrigin);

                logBookELDEvent.setEventType(cursor.getString(counter++));

                String eventCode = cursor.getString(counter++);
                logBookELDEvent.setEventCode(eventCode);

                logBookELDEvent.setAnnotation(cursor.getString(counter++));
                String recordStatus = cursor.getString(counter++);

                logBookELDEvent.setShiftstart(cursor.getString(counter++));

                String availableEventCode = "1234567";
                if (recordStatus != null && recordStatus.equals("1")
                        && availableEventCode != null && availableEventCode.contains(eventCode))
                    results.add(logBookELDEvent);

                counter = 0;
            } while (cursor.moveToNext());
        }

        cursor.close();

        return results;
    }

    public int getCertifyNumber(String CreationDate) {
//        Log.d(TAG, "getCertifyNumber: CreationDate: " + CreationDate);

        if (CreationDate.isEmpty()) {
            return 0;
        }

        Cursor cursor = db.getQuery("SELECT Count(*) FROM eldevents WHERE eventCodeDescription LIKE 'cert%' AND creationDate LIKE '" + CreationDate + "%'");
        if (cursor == null)
            return 0;
        cursor.moveToFirst();

        int certifyCount = cursor.getInt(0);
        cursor.close();
        return certifyCount;
    }

    public EldEvent getLastCertifyEvent() {
        Cursor c = db.getQuery("SELECT id, organizationName, organizationNumber, eldUsername, eventType, " +
                "eventCode, recordStatus, recordOrigin, truckNumber, vin, localizationDescription, latitudeString, " +
                "longitudeString, dstSinceLastValidCoords, vehicleMiles, engineHours, orderNumbercmv, orderNumberUser, " +
                "sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, recordOriginId, checkData, " +
                "checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                "editReason, eventseconds, shiftstart, objectId, objectType, creationdate, odometer FROM eldevents WHERE eventCodeDescription LIKE 'cert%' ORDER BY creationDate DESC LIMIT 1");

        if (c == null)
            return null;

        c.moveToFirst();
        EldEvent e = new EldEvent();

        if (c.moveToFirst()) {
            int col = 0;

            e.Id = c.getString(col++);
            e.OrganizationName = c.getString(col++);
            e.OrganizationNumber = c.getString(col++);
            e.EldUsername = c.getString(col++);
            e.EventType = c.getString(col++);
            e.EventCode = c.getString(col++);
            e.RecordStatus = c.getString(col++);
            e.RecordOrigin = c.getString(col++);
            e.TruckNumber = c.getString(col++);
            e.Vin = c.getString(col++);
            e.LocalizationDescription = c.getString(col++);
            e.LatitudeString = c.getString(col++);
            e.LongitudeString = c.getString(col++);
            e.DstSinceLastValidCoords = c.getString(col++);
            e.VehicleMiles = c.getString(col++);
            e.EngineHours = c.getString(col++);
            e.OrderNumberCmv = c.getString(col++);
            e.OrderNumberUser = c.getString(col++);
            e.SequenceId = c.getString(col++);
            e.EventCodeDescription = c.getString(col++);
            e.DiagnosticIndicator = c.getString(col++);
            e.MalfunctionIndicator = c.getString(col++);
            e.Annotation = c.getString(col++);
            e.RecordOriginId = c.getString(col++);
            e.CheckData = c.getString(col++);
            e.CheckSum = c.getString(col++);
            e.MalfunctionDiagnosticCode = c.getString(col++);
            e.MalfunctionDiagnosticDescp = c.getString(col++);
            e.DriverLastName = c.getString(col++);
            e.DriverFirstName = c.getString(col++);
            e.DriverRecordId = c.getString(col++);
            e.EditReason = c.getString(col++);
            e.EventSeconds = c.getDouble(col++);
            e.ShiftStart = c.getString(col++);
            e.objectId = c.getString(col++);
            e.objectType = c.getString(col++);
            e.CreationDate = c.getString(col++);
            e.Odometer = c.getString(col++);
        }

        c.close();
        return e;
    }

    public LogBookELDEvent getPreviousEventByDate(String CreationDate) {
        LogBookELDEvent logBookELDEvent = null;

        Cursor cursor = db.getQuery("SELECT creationdate, localizationDescription, vehicleMiles, engineHours, eventCodeDescription, recordOrigin," +
                " eventType, eventCode FROM eldevents WHERE creationDate LIKE '" + CreationDate + "%' AND eventCode IN ('1', '2', '3', '4') AND eventType = '1' " +
                "ORDER BY creationDate DESC LIMIT 1");

        if (cursor.moveToFirst()) {
            int counter = 0;

            logBookELDEvent = new LogBookELDEvent();
            String creationDate = cursor.getString(counter++);

            String formattedTime = DateUtils.convertDateTime(creationDate, DateUtils.FORMAT_DATE_TIME_MILLIS, DateUtils.FORMAT_DATE_HH_MM);
            logBookELDEvent.setTime(formattedTime);

            logBookELDEvent.setLocation(cursor.getString(counter++));
            logBookELDEvent.setOdometer(cursor.getString(counter++));
            logBookELDEvent.setEngHours(cursor.getString(counter++));
            logBookELDEvent.setEventTypeDescription(cursor.getString(counter++));

            String origin = cursor.getString(counter++);
            String formattedOrigin = "";

            if (origin != null)
                formattedOrigin = origin.equals("1") ? "Auto" : "Driver";

            logBookELDEvent.setOrigin(formattedOrigin);

            logBookELDEvent.setEventType(cursor.getString(counter++));
            logBookELDEvent.setEventCode(cursor.getString(counter++));
        }

        cursor.close();
        return logBookELDEvent;
    }

    public List<String> getDriverInfo(String driverID) {

        List<String> results = new ArrayList<>();
        Cursor cursor = db.getQuery("SELECT driverLicenseNumber, driverLicenseState FROM driver WHERE rcoRecordId=" + driverID + " ORDER BY creationDate id LIMIT 1");

        int counter = 0;
        if (cursor.moveToFirst()) {
            do {
                results.add(cursor.getString(counter++));
                results.add(cursor.getString(counter++));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return results;
    }

    public List<String> getDriverTruckAndTrailerInfo(String rmsUserId) {

        List<String> results = new ArrayList<>();
        Cursor cursor = db.getQuery("SELECT trucknumber, trailernumber, trailer2Number FROM driver WHERE rmsUserId=" + rmsUserId);

        int counter = 0;
        if (cursor.moveToFirst()) {
            do {
                results.add(cursor.getString(counter++));
                results.add(cursor.getString(counter++));
                results.add(cursor.getString(counter++));
            } while (cursor.moveToNext());
        }

        cursor.close();
        return results;
    }


    public TruckEldDetail getTruckEldDetail(int id) {
        Cursor c = db.getQuery("SELECT * FROM truckelddetail WHERE id=" + id);

        if (c == null)
            return null;

        c.moveToFirst();
        TruckEldDetail d = new TruckEldDetail();
        int counter = 0;

        if (!c.isAfterLast()) {
            d.Id = Integer.toString(c.getInt(counter++));
            d.objectId = c.getString(counter++);
            d.objectType = c.getString(counter++);
            d.MasterBarcode = c.getString(counter++);
            d.CreationDatetime = c.getString(counter++);
            d.CreationDate = c.getString(counter++);
            d.CreationTime = c.getString(counter++);
            d.RecordId = c.getString(counter++);
            d.RmsTimestamp = c.getString(counter++);
            d.RmsCodingTimestamp = c.getString(counter++);
            d.RmsEfileTimestamp = c.getString(counter++);
            d.FunctionalGroupName = c.getString(counter++);
            d.FunctionalGroupObjectId = c.getString(counter++);
            d.MobileRecordId = c.getString(counter++);
            d.OrganizationName = c.getString(counter++);
            d.OrganizationNumber = c.getString(counter++);
            d.TwentyFourHourPeriodStartingTime = c.getString(counter++);
            d.CarrierName = c.getString(counter++);
            d.UsDotNumber = c.getString(counter++);
            d.DriverName = c.getString(counter++);
            d.DriverId = c.getString(counter++);
            d.DriverRecordId = c.getString(counter++);
            d.CoDriverName = c.getString(counter++);
            d.CoDriverId = c.getString(counter++);
            d.CoDriverRecordId = c.getString(counter++);
            d.CurrentLocation = c.getString(counter++);
            d.DataDiagnosticsIndicators = c.getString(counter++);
            d.EldMalfunctionIndicators = c.getString(counter++);
            d.EldManufacturerName = c.getString(counter++);
            d.EldRegistrationId = c.getString(counter++);
            d.UnidentifiedDriverRecords = c.getString(counter++);
            d.ExemptDriverStatus = c.getString(counter++);
            d.MilesToday = c.getString(counter++);
            d.PrintDisplayDate = c.getString(counter++);
            d.RecordDate = c.getString(counter++);
            d.ShippingId = c.getString(counter++);
            d.CurrentEngineHours = c.getString(counter++);
            d.EngineHoursStart = c.getString(counter++);
            d.EngineHoursEnd = c.getString(counter++);
            d.CurrentOdometer = c.getString(counter++);
            d.OdometerStart = c.getString(counter++);
            d.OdometerEnd = c.getString(counter++);
            d.TimeZone = c.getString(counter++);
            d.TruckNumber = c.getString(counter++);
            d.TruckVin = c.getString(counter++);
            d.TrailerNumber = c.getString(counter++);
            d.TruckLogDetailRecordId = c.getString(counter++);
            d.OffDutyHours = c.getString(counter++);
            d.SleeperHours = c.getString(counter++);
            d.DrivingHours = c.getString(counter++);
            d.OnDutyHours = c.getString(counter++);
            d.Status = c.getString(counter++);
            d.Comments = c.getString(counter++);
            d.Rule = c.getString(counter++);
            d.CycleStartDateTime = c.getString(counter++);
            d.VehicleLicenseNumber = c.getString(counter++);
            d.DataCheckValue = c.getString(counter++);
            d.CertifyDateTime = c.getString(counter++);
            d.CmvPowerUnitNumber = c.getString(counter++);
            d.Sent = c.getShort(counter++) == 1;
            d.TruckEldHeaderId = c.getString(counter++);

            return d;
        }

        return null;
    }

    public void createNewDriverTruckEldHeaderEntry(String truckLogHeaderRecordId) {
        String mobileRecordId = Rms.getDefaultMobileRecordId("TruckEldHeader");
        ContentValues values = new ContentValues();

        values.put("creationDate", DateUtils.getNowYyyyMmDd());
        values.put("creationDatetime", DateUtils.getNowYyyyMmDdHhmmss());
        values.put("creationTime", DateUtils.getNowHhmmss());
        values.put("organizationName", Rms.getOrgName());
        values.put("organizationNumber", Rms.getOrgNumber());
        values.put("truckLogHeaderRecordId", truckLogHeaderRecordId);
        values.put("driverName", user.getLastFirstName());
        values.put("driverRecordId", user.getRecordId());
        values.put("cycleStartDateTime", DateUtils.getNowYyyyMmDdHhmmss());
        values.put("vehicleLicenseNumber", user.getTruckNumber());
        values.put("mobileRecordId", mobileRecordId);
        values.put("creatorFirstName", user.getFirstName());
        values.put("creatorLastName", user.getLastName());
        values.put("creatorRecordId", user.getRecordId());
        values.put("truckNumber", user.getTruckNumber());
        values.put("trailer1Number", user.getTrailerNumber());
        values.put("rule", getActiveRuleName() != null ? getActiveRuleName() : "");
        values.put("sent", "0");

        if (getActiveRuleName() != null)
            values.put("rule", getActiveRuleName());

        db.insert("truckeldheader", values);
    }

    public void createNewDriverTruckEldDetailEntry(Activity a, String
            truckLogHeaderMobileRecordId, String truckLogDetailMobileRecordId, TruckLogDetail
                                                           truckLogDetail) {
        String mobileRecordId = Rms.getDefaultMobileRecordId("TruckEldDetail");
        String localizationStr = getLocalizationStr(a);

        TruckLogHeader t = getTruckLogHeader(truckLogHeaderMobileRecordId);
        TruckEldHeader truckEldHeader = getTruckEldHeader(truckLogHeaderMobileRecordId);

        ContentValues values = new ContentValues();

        values.put("creationDatetime", DateUtils.getNowYyyyMmDdHhmmss());
        values.put("creationDate", DateUtils.getNowYyyyMmDd());
        values.put("creationTime", DateUtils.getNowHhmmss());
        values.put("mobileRecordId", mobileRecordId);
        values.put("organizationName", Rms.getOrgName());
        values.put("organizationNumber", Rms.getOrgNumber());
        values.put("twentyFourHourPeriodStartingTime", DateUtils.getNowHhmmss());
        values.put("carrierName", getSetting("database.ELD Carrier Name"));
        values.put("usDotNumber", getSetting("database.ELD USDotNumber"));
        values.put("driverName", user.getLastName());
        values.put("driverId", "");
        values.put("driverRecordId", user.getRecordId());
        values.put("coDriverName", "");
        values.put("coDriverId", "");
        values.put("coDriverRecordId", "");
        values.put("currentLocation", localizationStr);
        values.put("dataDiagnosticsIndicators", "");
        values.put("eldMalfunctionIndicators", "");
        values.put("eldManufacturerName", getSetting("database.ELD Registration Name"));
        values.put("eldRegistrationId", getSetting("database.ELD Registration Id"));
        values.put("unidentifiedDriverRecords", "");
        values.put("exemptDriverStatus", "");
        values.put("milesToday", getBleParameter("TripDistance"));
        values.put("printDisplayDate", DateUtils.getNowYyyyMmDd());
        values.put("recordDate", DateUtils.getNowYyyyMmDd());
        values.put("shippingId", "");
        values.put("currentEngineHours", getBleParameter("EngineHours"));
        values.put("engineHoursStart", "");
        values.put("engineHoursEnd", "");
        values.put("currentOdometer", getBleParameter("Odometer"));
        values.put("odometerStart", "");
        values.put("odometerEnd", "");
        values.put("timeZone", "");
        values.put("truckNumber", user.getTruckNumber());
        values.put("truckVIN", user.getTruckNumber());
        values.put("trailerNumber", user.getTrailerNumber());
        values.put("truckLogDetailRecordId", truckLogDetailMobileRecordId);
        values.put("offDutyHours", "0");
        values.put("sleeperHours", "0");
        values.put("drivingHours", "0");
        values.put("onDutyHours", "0");
        values.put("status", getDrivingStatus());
        values.put("comments", "");
        values.put("rule", getActiveRuleName() != null ? getActiveRuleName() : "");
        values.put("cycleStartDateTime", t.CreationDatetime);
        values.put("vehicleLicenseNumber", user.getTruckNumber());
        values.put("dataCheckValue", "");
        values.put("certifyDateTime", "");
        values.put("cmvPowerUnitNumber", user.getTruckNumber());
        values.put("sent", "0");
        values.put("truckEldHeaderId", truckEldHeader.Id);

        db.insert("truckelddetail", values);
    }

    public void createNewDriverTruckEldDetailEntryOld(String
                                                              truckLogHeaderMobileRecordId, String truckLogDetailMobileRecordId, TruckLogDetail
                                                              truckLogDetail) {
        String mobileRecordId = Rms.getDefaultMobileRecordId("TruckEldDetail");

        TruckLogHeader t = getTruckLogHeader(truckLogHeaderMobileRecordId);
        TruckEldHeader truckEldHeader = getTruckEldHeader(truckLogHeaderMobileRecordId);

        ContentValues values = new ContentValues();

        values.put("creationDatetime", DateUtils.getNowYyyyMmDdHhmmss());
        values.put("creationDate", DateUtils.getNowYyyyMmDd());
        values.put("creationTime", DateUtils.getNowHhmmss());
        values.put("mobileRecordId", mobileRecordId);
        values.put("organizationName", Rms.getOrgName());
        values.put("organizationNumber", Rms.getOrgNumber());
        values.put("twentyFourHourPeriodStartingTime", DateUtils.getNowHhmmss());
        values.put("carrierName", "");

        values.put("driverName", user.getLastName());
        values.put("driverRecordId", user.getRecordId());
        values.put("vehicleLicenseNumber", user.getTruckNumber());
        values.put("truckLogDetailRecordId", truckLogDetailMobileRecordId);
        values.put("truckNumber", user.getTruckNumber());
        values.put("truckVIN", user.getTruckNumber());
        values.put("trailerNumber", user.getTrailerNumber());
        values.put("cycleStartDateTime", t.CreationDatetime);
        values.put("truckLogDetailRecordId", truckLogDetailMobileRecordId);
        values.put("offDutyHours", "0");
        values.put("sleeperHours", "0");
        values.put("drivingHours", "0");
        values.put("onDutyHours", "0");
        values.put("recordDate", DateUtils.getNowYyyyMmDd());
        values.put("status", getDrivingStatus());
        values.put("truckEldHeaderId", truckEldHeader.Id);
        values.put("rule", getActiveRuleName() != null ? getActiveRuleName() : "");
        values.put("sent", "0");

        db.insert("truckelddetail", values);
    }

    public ArrayList<TruckEldHeader> getPendingTruckEldEntries() {
        try {
            ArrayList<TruckEldHeader> result = getPendingTruckEldEntriesFromHeaders();

            if (result == null)
                result = new ArrayList();

            ArrayList<TruckEldHeader> result2 = getPendingTruckEldEntriesFromDetails();

            if (result2 == null || result2.size() == 0)
                return result;

            for (int i = 0; i < result2.size(); i++) {
                TruckEldHeader h = result2.get(i);

                if (!existsTruckEldEntry(h, result))
                    result.add(h);
            }

            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    private boolean existsTruckEldEntry(TruckEldHeader
                                                truckEldHeader, ArrayList<TruckEldHeader> entries) {
        if (entries == null || entries.size() == 0)
            return false;

        for (int i = 0; i < entries.size(); i++)
            if (truckEldHeader.Id == entries.get(i).Id)
                return true;

        return false;
    }

    private ArrayList<TruckEldHeader> getPendingTruckEldEntriesFromHeaders() {
        try {
            ArrayList<TruckEldHeader> result = new ArrayList();
            Cursor c = db.getQuery("SELECT id FROM truckeldheader WHERE creationDate IS NOT NULL AND driverRecordId='" + user.getRecordId() + "' AND sent=0");

            if (c == null)
                return null;

            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                TruckEldHeader h = getTruckEldHeader(id);

                result.add(h);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    private ArrayList<TruckEldHeader> getPendingTruckEldEntriesFromDetails() {
        try {
            ArrayList<TruckEldHeader> result = new ArrayList();
            Cursor c = db.getQuery("SELECT truckeldheaderid FROM truckelddetail WHERE creationDate IS NOT NULL AND driverRecordId='" + user.getRecordId() + "' AND sent=0");

            if (c == null)
                return null;

            c.moveToFirst();

            while (!c.isAfterLast()) {
                int id = c.getInt(0);
                TruckEldHeader h = getTruckEldHeader(id);

                result.add(h);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public ArrayList<String[]> getPendingTruckEldContentLinesGroups() {
        try {
            ArrayList<String[]> result = new ArrayList();
            Cursor c = db.getQuery("SELECT objectId, objectType FROM contentlines WHERE logType='truckeld' AND sent=0 GROUP BY objectId, objectType");

            if (c == null)
                return null;

            c.moveToFirst();
            int counter = 0;

            while (!c.isAfterLast()) {
                String objectId = c.getString(counter++);
                String objectType = c.getString(counter++);

                result.add(new String[]{objectId, objectType});

                c.moveToNext();
                counter = 0;
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public ArrayList<TruckEldContentLine> getPendingTruckEldContentLines() {
        try {
            ArrayList<String[]> groups = getPendingTruckEldContentLinesGroups();

            if (groups == null)
                return null;

            String objectId = groups.get(0)[0];
            String objectType = groups.get(0)[1];

            ArrayList<TruckEldContentLine> result = new ArrayList();

            Cursor c = db.getQuery("SELECT * FROM contentlines WHERE (sent=0 OR sent IS NULL) AND objectId='" + objectId +
                    "' AND objectType='" + objectType + "' ORDER BY creationdate ASC");

            if (c == null)
                return null;

            c.moveToFirst();
            int counter = 0;

            while (!c.isAfterLast()) {
                TruckEldContentLine contentLine = new TruckEldContentLine();
                counter = 0;

                contentLine.Id = Integer.toString(c.getInt(counter++));
                contentLine.ObjectId = c.getString(counter++);
                contentLine.ObjectType = c.getString(counter++);
                contentLine.DriverRecordId = c.getString(counter++);
                contentLine.Csvline = c.getString(counter++);
                counter++;
                counter++;
                contentLine.Time = c.getString(counter++);
                counter++;
                counter++;
                counter++;
                counter++;
                counter++;
                counter++;
                counter++;
                contentLine.Odometer = c.getString(counter++);
                counter++;
                contentLine.EngineHours = c.getString(counter++);
                contentLine.EventStatus = c.getString(counter++);
                counter++;
                //contentLine.LocationDescription = c.getString(counter++);
                //contentLine.Origin = c.getString(counter++);
                //contentLine.Annotation = c.getString(counter++);
                contentLine.LogType = c.getString(counter++);
                contentLine.CreationDate = c.getString(counter++);
                contentLine.Sent = c.getShort(counter++) == 1;

                result.add(contentLine);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            return null;
        }
    }

    public void sendPendingTruckEldEntries(TruckEldHeader e) throws Exception {
        ArrayList<String[]> response = Rms.setTruckElds(e);

        e.objectId = response.get(0)[2];
        e.objectType = response.get(0)[3];

        updateTruckEldEntryObjectIdType("header", e.Id, e.objectId, e.objectType);
        ArrayList<TruckEldDetail> details = e.getTruckEldDetails();

        if (details == null || details.size() == 0)
            return;

        for (int i = 0; i < details.size(); i++) {
            TruckEldDetail detail = details.get(i);

            detail.objectId = response.get(i + 1)[2];
            detail.objectType = response.get(i + 1)[3];

            updateTruckEldEntryObjectIdType("detail", detail.Id, detail.objectId, detail.objectType);
        }
    }

    public void setTruckEldHeaderEntryToSent(TruckEldHeader e) {
        ArrayList<TruckEldHeader> headers = new ArrayList();
        headers.add(e);

        setTruckEldHeaderEntryToSent(headers, true);
    }

    public void setTruckEldHeaderEntryToSent(ArrayList<TruckEldHeader> headers,
                                             boolean isSent) {
        if (headers == null)
            return;

        lock.lock();

        try {
            for (int i = 0; i < headers.size(); i++) {
                TruckEldHeader e = headers.get(i);

                ContentValues values = new ContentValues();

                values.put("sent", isSent ? "1" : "0");
                db.update("truckeldheader", values, "id=" + e.Id);
            }
        } finally {
            lock.unlock();
        }
    }

    public void setTruckEldDetailEntryToSent(ArrayList<TruckEldDetail> details) {
        if (details == null)
            return;

        lock.lock();

        try {
            for (int i = 0; i < details.size(); i++) {
                TruckEldDetail e = details.get(i);

                ContentValues values = new ContentValues();

                values.put("sent", "1");
                db.update("truckelddetail", values, "id=" + e.Id);
            }
        } finally {
            lock.unlock();
        }
    }

    public void syncTruckElds() throws Exception {
//        May 26, 2022  -   The sync is done just at the first install
        if (db.exists("SELECT * FROM truckeldheader WHERE objectId IS NOT NULL AND driverRecordId='" + user.getRecordId() + "'"))
            return;

        truckElds = new ArrayList<>();
        ArrayList<TruckEldHeader> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");

        String response = Rms.getRecordsUpdatedXFiltered("TruckELDHeader", -5000,
                "Organization+Name,DriverRecordId", ",", orgName + "," + user.getRecordId(), ",", "");

        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            clearTableEntries("truckeldheader", "objectId NOT NULL AND driverRecordId='" + user.getRecordId() + "'");

            for (int i = 0; i < jsonArray.length(); i++) {
                try {
                    logDebug(">>>Parsing truckeldheader record: " + i + " of " + jsonArray.length());

                    JSONObject o = jsonArray.getJSONObject(i);
                    PairList codingFields = Rms.parseJsonCodingFields(o);

                    TruckEldHeader h = new TruckEldHeader();

                    h.objectId = codingFields.getValue("LobjectId");
                    h.objectType = codingFields.getValue("objectType");
                    h.RecordId = codingFields.getValue("RecordId");
                    h.BarCode = codingFields.getValue("BarCode");
                    h.MobileRecordId = codingFields.getValue("MobileRecordId");
                    h.CreationDatetime = codingFields.getValue("Creation Datetime");
                    h.CreationDate = codingFields.getValue("Creation Date");
                    h.CreationTime = codingFields.getValue("Creation Time");
                    h.MasterBarcode = codingFields.getValue("Master Barcode");
                    h.RmsTimestamp = codingFields.getValue("RMS Timestamp");
                    h.RmsCodingTimestamp = codingFields.getValue("RMS Coding Timestamp");
                    h.FunctionalGroupName = codingFields.getValue("FunctionalGroupName");
                    h.FunctionalGroupObjectId = codingFields.getValue("FunctionalGroupObjectId");
                    h.CreatorFirstName = codingFields.getValue("Creator First Name");
                    h.CreatorLastName = codingFields.getValue("Creator Last Name");
                    h.CreatorRecordId = codingFields.getValue("CreatorRecordId");
                    h.OrganizationName = codingFields.getValue("Organization Name");
                    h.OrganizationNumber = codingFields.getValue("Organization Number");
                    h.VehicleLicenseNumber = codingFields.getValue("Vehicle License Number");
                    h.CycleStartDateTime = codingFields.getValue("CycleStartDateTime");
                    h.Rule = codingFields.getValue("Rule");
                    h.DriverName = codingFields.getValue("Driver Name");
                    h.DriverRecordId = codingFields.getValue("DriverRecordId");
                    h.DriverId = codingFields.getValue("DriverId");
                    h.CoDriverName = codingFields.getValue("Co-Driver Name");
                    h.CoDriverRecordId = codingFields.getValue("CoDriverRecordId");
                    h.CoDriverId = codingFields.getValue("Co-DriverId");
                    h.TruckLogHeaderRecordId = codingFields.getValue("TruckLogHeaderRecordId");
                    h.TruckNumber = codingFields.getValue("Truck Number");
                    h.Trailer1Number = codingFields.getValue("Trailer1 Number");
                    h.Trailer2Number = codingFields.getValue("Trailer2 Number");

                    result.add(h);
                    Integer truckEldHeaderId = insertTruckEldHeaderEntry(h, true);

                    String response2 = Rms.getRecordsUpdatedXFiltered("TruckELDDetail", -5000,
                            "Organization+Name,Master+Barcode", ",", orgName + "," + h.BarCode, ",", "");

                    if (response2 != null && response2.trim().length() > 0) {
                        JSONArray jsonArray2 = new JSONArray(response2);
                        clearTableEntries("truckelddetail", "MasterBarcode='" + h.BarCode + "'");

                        for (int j = 0; j < jsonArray2.length(); j++) {
                            JSONObject o2 = jsonArray2.getJSONObject(j);
                            PairList codingFields2 = Rms.parseJsonCodingFields(o2);

                            TruckEldDetail d = new TruckEldDetail();

                            d.objectId = codingFields2.getValue("LobjectId");
                            d.objectType = codingFields2.getValue("objectType");
                            d.RecordId = codingFields2.getValue("RecordId");
                            d.MobileRecordId = codingFields2.getValue("MobileRecordId");
                            d.MasterBarcode = codingFields2.getValue("Master Barcode");
                            d.CreationDatetime = codingFields2.getValue("Creation Datetime");
                            d.CreationDate = codingFields2.getValue("Creation Date");
                            d.CreationTime = codingFields2.getValue("Creation Time");
                            d.RmsTimestamp = codingFields2.getValue("RMS Timestamp");
                            d.RmsCodingTimestamp = codingFields2.getValue("RMS Coding Timestamp");
                            d.RmsEfileTimestamp = codingFields2.getValue("RMS Efile Timestamp");
                            d.FunctionalGroupName = codingFields2.getValue("FunctionalGroupName");
                            d.FunctionalGroupObjectId = codingFields2.getValue("FunctionalGroupObjectId");
                            d.OrganizationName = codingFields2.getValue("Organization Name");
                            d.OrganizationNumber = codingFields2.getValue("Organization Number");
                            d.TwentyFourHourPeriodStartingTime = codingFields2.getValue("24-Hour Period Starting Time");
                            d.CarrierName = codingFields2.getValue("Carrier Name");
                            d.UsDotNumber = codingFields2.getValue("USDOT Number");
                            d.DriverName = codingFields2.getValue("Driver Name");
                            d.DriverId = codingFields2.getValue("DriverId");
                            d.DriverRecordId = codingFields2.getValue("DriverRecordId");
                            d.CoDriverName = codingFields2.getValue("Co-Driver Name");
                            d.CoDriverId = codingFields2.getValue("Co-DriverId");
                            d.CoDriverRecordId = codingFields2.getValue("CoDriverRecordId");
                            d.CurrentLocation = codingFields2.getValue("Current Location");
                            d.DataDiagnosticsIndicators = codingFields2.getValue("Data Diagnostics Indicators");
                            d.EldMalfunctionIndicators = codingFields2.getValue("ELD Malfunction Indicators");
                            d.EldManufacturerName = codingFields2.getValue("ELD Manufacturer Name");
                            d.EldRegistrationId = codingFields2.getValue("ELD Registration Id");
                            d.UnidentifiedDriverRecords = codingFields2.getValue("Unidentified Driver Records");
                            d.ExemptDriverStatus = codingFields2.getValue("Exempt Driver Status");
                            d.MilesToday = codingFields2.getValue("Miles Today");
                            d.PrintDisplayDate = codingFields2.getValue("Print-Display Date");
                            d.RecordDate = codingFields2.getValue("Record Date");
                            d.ShippingId = codingFields2.getValue("Shipping Id");
                            d.CurrentEngineHours = codingFields2.getValue("Current Engine Hours");
                            d.EngineHoursStart = codingFields2.getValue("Engine Hours Start");
                            d.EngineHoursEnd = codingFields2.getValue("Engine Hours End");
                            d.CurrentOdometer = codingFields2.getValue("Current Odometer");
                            d.OdometerStart = codingFields2.getValue("Odometer Start");
                            d.OdometerEnd = codingFields2.getValue("Odometer End");
                            d.TimeZone = codingFields2.getValue("Time Zone");
                            d.TruckNumber = codingFields2.getValue("Truck Number");
                            d.TruckVin = codingFields2.getValue("Truck VIN");
                            d.TrailerNumber = codingFields2.getValue("Trailer Number");
                            d.TruckLogDetailRecordId = codingFields2.getValue("TruckLogDetailRecordId");
                            d.OffDutyHours = codingFields2.getValue("Off Duty Hours");
                            d.SleeperHours = codingFields2.getValue("Sleeper Hours");
                            d.DrivingHours = codingFields2.getValue("Driving Hours");
                            d.OnDutyHours = codingFields2.getValue("On Duty Hours");
                            d.Status = codingFields2.getValue("Status");
                            d.Comments = codingFields2.getValue("Comments");
                            d.Rule = codingFields2.getValue("Rule");
                            d.CycleStartDateTime = codingFields2.getValue("CycleStartDateTime");
                            d.VehicleLicenseNumber = codingFields2.getValue("Vehicle License Number");
                            d.DataCheckValue = codingFields2.getValue("Data Check Value");
                            d.CertifyDateTime = codingFields2.getValue("Certify DateTime");
                            d.CmvPowerUnitNumber = codingFields2.getValue("CMV Power Unit Number");
                            d.MasterBarcode = h.BarCode;
                            d.TruckEldHeaderId = truckEldHeaderId != null ? truckEldHeaderId.toString() : new Integer(i + 1).toString();

                            h.addDetail(d);
                            insertTruckEldDetailEntry(d, true);
                        }
                    }
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
                }
            }

            truckElds.addAll(result);
        }
    }

    private Integer insertTruckEldHeaderEntry(TruckEldHeader t, boolean isSent) {
        ContentValues values = new ContentValues();

        values.put("objectId", t.objectId);
        values.put("objectType", t.objectType);
        values.put("recordId", t.RecordId);
        values.put("mobileRecordId", t.MobileRecordId);
        values.put("creationDatetime", t.CreationDatetime);
        values.put("creationDate", t.CreationDate);
        values.put("creationTime", t.CreationTime);
        values.put("masterBarcode", t.MasterBarcode);
        values.put("rmsTimestamp", t.RmsTimestamp);
        values.put("rmsCodingTimestamp", t.RmsCodingTimestamp);
        values.put("functionalGroupName", t.FunctionalGroupName);
        values.put("functionalGroupObjectId", t.FunctionalGroupObjectId);
        values.put("creatorFirstName", t.CreatorFirstName);
        values.put("creatorLastName", t.CreatorLastName);
        values.put("creatorRecordId", t.CreatorRecordId);
        values.put("organizationName", t.OrganizationName);
        values.put("organizationNumber", t.OrganizationNumber);
        values.put("vehicleLicenseNumber", t.VehicleLicenseNumber);
        values.put("cycleStartDateTime", t.CycleStartDateTime);
        values.put("rule", t.Rule);
        values.put("driverName", t.DriverName);
        values.put("driverRecordId", t.DriverRecordId);
        values.put("driverId", t.DriverId);
        values.put("coDriverName", t.CoDriverName);
        values.put("coDriverRecordId", t.CoDriverRecordId);
        values.put("coDriverId", t.CoDriverId);
        values.put("truckLogHeaderRecordId", t.TruckLogHeaderRecordId);
        values.put("truckNumber", t.TruckNumber);
        values.put("trailer1Number", t.Trailer1Number);
        values.put("trailer2Number", t.Trailer2Number);
        values.put("sent", isSent ? "1" : "0");

        db.insert("truckeldheader", values);
        Integer id = getLastTableId("truckeldheader");

        return id;
    }

    private void insertTruckEldDetailEntry(TruckEldDetail t, boolean isSent) {
        ContentValues values = new ContentValues();

        values.put("objectId", t.objectId);
        values.put("objectType", t.objectType);
        values.put("recordId", t.RecordId);
        values.put("mobileRecordId", t.MobileRecordId);
        values.put("masterBarcode", t.MasterBarcode);
        values.put("creationDatetime", t.CreationDatetime);
        values.put("creationDate", t.CreationDate);
        values.put("creationTime", t.CreationTime);
        values.put("rmsTimestamp", t.RmsTimestamp);
        values.put("rmsCodingTimestamp", t.RmsCodingTimestamp);
        values.put("rmsEfileTimestamp", t.RmsEfileTimestamp);
        values.put("functionalGroupName", t.FunctionalGroupName);
        values.put("functionalGroupObjectId", t.FunctionalGroupObjectId);
        values.put("organizationName", t.OrganizationName);
        values.put("organizationNumber", t.OrganizationNumber);
        values.put("twentyFourHourPeriodStartingTime", t.TwentyFourHourPeriodStartingTime);
        values.put("carrierName", t.CarrierName);
        values.put("usDotNumber", t.UsDotNumber);
        values.put("driverName", t.DriverName);
        values.put("driverId", t.DriverId);
        values.put("driverRecordId", t.DriverRecordId);
        values.put("coDriverName", t.CoDriverName);
        values.put("coDriverId", t.CoDriverId);
        values.put("coDriverRecordId", t.CoDriverRecordId);
        values.put("currentLocation", t.CurrentLocation);
        values.put("dataDiagnosticsIndicators", t.DataDiagnosticsIndicators);
        values.put("eldMalfunctionIndicators", t.EldMalfunctionIndicators);
        values.put("eldManufacturerName", t.EldManufacturerName);
        values.put("eldRegistrationId", t.EldRegistrationId);
        values.put("unidentifiedDriverRecords", t.UnidentifiedDriverRecords);
        values.put("exemptDriverStatus", t.ExemptDriverStatus);
        values.put("milesToday", t.MilesToday);
        values.put("printDisplayDate", t.PrintDisplayDate);
        values.put("recordDate", t.RecordDate);
        values.put("shippingId", t.ShippingId);
        values.put("currentEngineHours", t.CurrentEngineHours);
        values.put("engineHoursStart", t.EngineHoursStart);
        values.put("engineHoursEnd", t.EngineHoursEnd);
        values.put("currentOdometer", t.CurrentOdometer);
        values.put("odometerStart", t.OdometerStart);
        values.put("odometerEnd", t.OdometerEnd);
        values.put("timeZone", t.TimeZone);
        values.put("truckNumber", t.TruckNumber);
        values.put("truckVIN", t.TruckVin);
        values.put("trailerNumber", t.TrailerNumber);
        values.put("truckLogDetailRecordId", t.TruckLogDetailRecordId);
        values.put("offDutyHours", t.OffDutyHours);
        values.put("sleeperHours", t.SleeperHours);
        values.put("drivingHours", t.DrivingHours);
        values.put("onDutyHours", t.OnDutyHours);
        values.put("status", t.Status);
        values.put("comments", t.Comments);
        values.put("rule", t.Rule);
        values.put("cycleStartDateTime", t.CycleStartDateTime);
        values.put("vehicleLicenseNumber", t.VehicleLicenseNumber);
        values.put("dataCheckValue", t.DataCheckValue);
        values.put("certifyDateTime", t.CertifyDateTime);
        values.put("cmvPowerUnitNumber", t.CmvPowerUnitNumber);
        values.put("truckeldheaderid", t.TruckEldHeaderId);
        values.put("sent", isSent ? "1" : "0");

        db.insert("truckelddetail", values);
    }

    public void updateTruckEldEntryObjectIdType(String entryType, String entryId, String
            objectId, String objectType) {
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("objectId", objectId);
            values.put("objectType", objectType);

            db.update("truckeld" + entryType, values, "id=" + entryId);
        } finally {
            lock.unlock();
        }
    }

    public String[] getTruckEldEntryForToday() {
        String userRecordId = user.getRecordId();
        String yyyyMmDdDateStr = DateUtils.getNowYyyyMmDd();

        String sqlString = "SELECT objectId, objectType FROM truckelddetail WHERE " +
                "driverRecordId='" + userRecordId + "' AND creationDate LIKE '" + yyyyMmDdDateStr + "%'";

        if (!db.exists(sqlString))
            return null;

        lock.lock();

        try {
            Cursor c = db.getQuery(sqlString);

            if (c == null)
                return null;

            c.moveToFirst();
            String[] result = new String[2];

            if (!c.isAfterLast()) {
                result[0] = c.getString(0);
                result[1] = c.getString(1);
            }

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    public Integer getLastTableId(String tableName) {
        lock.lock();

        try {
            Cursor c = db.getQuery("SELECT id FROM " + tableName + " ORDER BY id DESC LIMIT 1");

            if (c == null)
                return null;

            c.moveToFirst();
            Integer result = null;

            if (!c.isAfterLast())
                result = c.getInt(0);

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    //endregion

    //region Driver's Assumption of Unidentified Driver Logs

    public ArrayList<EldEvent> getUnidentifiedDriverLogs() {
        ArrayList<EldEvent> result = new ArrayList();
        Cursor c = db.getQuery("SELECT id FROM eldevents WHERE driverRecordId IS NULL OR driverRecordId=''");

        if (c == null)
            return null;

        c.moveToFirst();

        while (!c.isAfterLast()) {
            result.add(getEldEvent(c.getInt(0)));
            c.moveToNext();
        }

        c.close();
        return result;
    }

    public void assignUnidentifiedDriverEvent(EldEvent e, User u) {
        EldEvent clone = cloneEvent(e, u.getFirstName(), u.getLastName(), u.getRecordId());
        insertEldEventsEntry(clone);

        updateUnidentifiedDriverEventToEdited(e);
    }

    private EldEvent cloneEvent(EldEvent e, String driverFirstName, String
            driverLastName, String driverRecordId) {
        EldEvent result = new EldEvent(e);

        result.DriverFirstName = driverFirstName;
        result.DriverLastName = driverLastName;
        result.DriverRecordId = driverRecordId;
        result.RecordOrigin = EventCodeOrigin.ASSUMED_FROM_UNIDENTIFIED_DRIVER_PROFILE.toString();
        result.RecordStatus = EventRecordStatus.ACTIVE.toString();

        return result;
    }

    private void updateUnidentifiedDriverEventToEdited(EldEvent e) {
        if (e == null || StringUtils.isNullOrWhitespaces(e.Id))
            return;

        lock.lock();

        try {
            e.RecordOrigin = EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER.toString();
            ContentValues values = new ContentValues();

            values.put("recordOrigin", e.RecordOrigin);
            db.update("eldevents", values, "id=" + e.Id);
        } finally {
            lock.unlock();
        }
    }

    //endregion

    //region Start Toll Road

    public boolean isTollRoadEventStarted() {
        String isTollRoadEventStarted = getSetting("istollroadeventstarted");

        return isTollRoadEventStarted != null && isTollRoadEventStarted.equalsIgnoreCase("true");
    }

    public void setTollRoadEventToStarted(boolean value) {
        setSetting("istollroadeventstarted", value ? "true" : "false");
        BusHelperIfta.setIsIftaTaxExempt(value);
    }

    //endregion

    //region Sync items

    private ArrayList<Truck> trucks;
    private ArrayList<Trailer> trailers;
    private ArrayList<EldEvent> eldEvents;
    private ArrayList<TruckLogHeader> truckLogs;
    private ArrayList<TrailerLog> trailerLogs;
    private ArrayList<PretripModel> pretrips;
    private ArrayList<TollReceiptModel> tollReceipts;
    private ArrayList<FuelReceiptModel> fuelReceipts;
    private ArrayList<Rule> rules;

    public void syncTruckEldEvents() throws Exception {
//        Log.d(TAG, "syncTruckEldEvents: ");
        String timeStamp = "+";
//        May 26, 2022  -   The sync is done just at the first install
        boolean isExists = db.exists("SELECT * FROM eldevents WHERE objectId IS NOT NULL");
//        Log.d(TAG, "syncTruckEldEvents: isExists: " + isExists);
        if (isExists) {
//            return ;
            timeStamp = getMostRecentTimeStampForEvent(null);
            if (timeStamp == null) {
                timeStamp = "+";
            }
        }


        eldEvents = new ArrayList<>();
        ArrayList<EldEvent> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");
        String driverRecordId = user.getRecordId();

//        String response = Rms.getRecordsUpdatedXFiltered("ELD+Event", -5000,
//                "Organization+Name,DriverRecordId", ",", orgName + "," + driverRecordId, ",", "");


//        Log.d(TAG, "syncTruckEldEvents: response: timeStamp: " + timeStamp);
        String response = Rms.getRecordsUpdatedXFiltered("ELD+Event", -5000,
                "Organization+Name,DriverRecordId", ",", orgName + "," + driverRecordId, ",", "", timeStamp);
//        Log.d(TAG, "syncTruckEldEvents: response: " + response);
        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            if (timeStamp.equalsIgnoreCase("+")) {
//                June 08, 2022 -   We should clear the table only in case when we are doing a full sync
                clearTableEntries("eldevents", "objectId NOT NULL");
            }

//            Log.d(TAG, "syncTruckEldEvents: jsonArray: size: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                EldEvent a = new EldEvent();

                a.objectId = codingFields.getValue("LobjectId");
                a.objectType = codingFields.getValue("objectType");
                a.RecordId = codingFields.getValue("RecordId");
                a.MobileRecordId = codingFields.getValue("MobileRecordId");
                a.RmsCodingTimestamp = codingFields.getValue("RMS Coding Timestamp");
                a.RmsTimestamp = codingFields.getValue("RMS Timestamp");
                a.OrganizationName = codingFields.getValue("OrganizationName");
                a.OrganizationNumber = codingFields.getValue("OrganizationNumber");
                a.EldUsername = codingFields.getValue("ELD Username");
                a.EventType = codingFields.getValue("Event Type");
                a.EventCode = codingFields.getValue("Event Code");
                a.RecordStatus = codingFields.getValue("Record Status");
                a.RecordOrigin = codingFields.getValue("Record Origin");
                a.TruckNumber = codingFields.getValue("Truck Number");
                a.Vin = codingFields.getValue("VIN");
                a.LocalizationDescription = codingFields.getValue("Localization Description");
                a.LatitudeString = codingFields.getValue("Latitude String");
                a.LongitudeString = codingFields.getValue("Longitude String");
                a.DstSinceLastValidCoords = codingFields.getValue("Distance Since Last Valid Coordinates");
                a.VehicleMiles = codingFields.getValue("Vehicle Miles");
                a.EngineHours = codingFields.getValue("Engine Hours");
                a.OrderNumberCmv = codingFields.getValue("Order Number CMV");
                a.OrderNumberUser = codingFields.getValue("Order Number User");
                a.SequenceId = codingFields.getValue("Sequence Id");
                a.EventCodeDescription = codingFields.getValue("Event Code Description");
                a.DiagnosticIndicator = codingFields.getValue("Diagnostic Indicator");
                a.MalfunctionIndicator = codingFields.getValue("Malfunction Indicator");
                a.Annotation = codingFields.getValue("Annotation");
                a.RecordOriginId = codingFields.getValue("RecordOriginId");
                a.CheckData = codingFields.getValue("CheckData");
                a.CheckSum = codingFields.getValue("CheckSum");
                a.MalfunctionDiagnosticCode = codingFields.getValue("Malfunction Diagnostic Code");
                a.MalfunctionDiagnosticDescp = codingFields.getValue("Malfunction Diagnostic Description");
                a.DriverLastName = codingFields.getValue("Driver Last Name");
                a.DriverFirstName = codingFields.getValue("Driver First Name");
                a.DriverRecordId = codingFields.getValue("DriverRecordId");
                a.EditReason = codingFields.getValue("EditReason");

//                June 07, 2022 -   We are making sure that we never gets a null
                String eventSeconds = codingFields.getValue("Event Seconds");
                if (eventSeconds == null || eventSeconds.length() == 0) {
                    eventSeconds = "0.0";
                }
                a.EventSeconds = Double.parseDouble(eventSeconds);
                a.ShiftStart = codingFields.getValue("Shift Start");
                a.CreationDate = codingFields.getValue("DateTime");
                a.Odometer = codingFields.getValue("Odometer");

                result.add(a);
                insertEldEventsEntry(a);
            }

            eldEvents.addAll(result);
        }
    }

    public void syncTrucks() throws Exception {
//        May 26, 2022  -   The sync is done just at the first install
        if (db.exists("SELECT * FROM trucks"))
            return;

        trucks = new ArrayList<>();
        ArrayList<Truck> result = new ArrayList();

        String response = Rms.getRecordsUpdatedXFiltered("Truck", -5000,
                "Organization+Name", ",", Rms.getOrgName().replace(" ", "+"), ",", "");

        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            clearTableEntries("trucks");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                Truck a = new Truck();
                a.RecordId = codingFields.getValue("RecordId");
                a.MobileRecordId = codingFields.getValue("MobileRecordId");
                a.RmsCodingTimestamp = codingFields.getValue("RMS Coding Timestamp");
                a.RmsTimestamp = codingFields.getValue("RMS Timestamp");
                a.OrganizationName = codingFields.getValue("OrganizationName");
                a.OrganizationNumber = codingFields.getValue("OrganizationNumber");
                a.ContainerTypeRecordId = codingFields.getValue("ContainerTypeRecordId");
                a.UniqueKey1 = codingFields.getValue("Unique Key 1");
                a.Deployment = codingFields.getValue("Deployment");
                a.IsaLocation = codingFields.getValue("IsaLocation");
                a.CustomerRecordId = codingFields.getValue("CustomerRecordId");
                a.ManufacturerSerialNumber = codingFields.getValue("Manufacturer Serial Number");
                a.Overdue = codingFields.getValue("Overdue");
                a.StoreName = codingFields.getValue("Store Name");
                a.StoreNumber = codingFields.getValue("Store Number");
                a.StoreType = codingFields.getValue("Store Type");
                a.DateLoaded = codingFields.getValue("Date Loaded");
                a.DateUnloaded = codingFields.getValue("Date Unloaded");
                a.TimeLoaded = codingFields.getValue("Time Loaded");
                a.TimeUnloaded = codingFields.getValue("Time Unloaded");
                a.FunctionalGroupName = codingFields.getValue("FunctionalGroupName");
                a.Managers = codingFields.getValue("Managers");
                a.FunctionalGroupObjectId = codingFields.getValue("FunctionalGroupObjectId");
                a.DateDoorClosed = codingFields.getValue("Date Door Closed");
                a.TimeDoorClosed = codingFields.getValue("Time Door Closed");
                a.DateTime = codingFields.getValue("DateTime");
                a.AlertCount = codingFields.getValue("Alert Count");
                a.VendorName = codingFields.getValue("Vendor Name");
                a.Tracking = codingFields.getValue("Tracking");
                a.ItemType = codingFields.getValue("ItemType");
                a.Humidity = codingFields.getValue("Humidity");
                a.GPSDailySampleRate = codingFields.getValue("GPS Daily Sample Rate");
                a.GPSTripSampleRate = codingFields.getValue("GPS Trip Sample Rate");
                a.TruckGPSServices = codingFields.getValue("TruckGPSServices");
                a.FromLatitude = codingFields.getValue("From Latitude");
                a.FromLongitude = codingFields.getValue("From Longitude");
                a.ToLatitude = codingFields.getValue("To Latitude");
                a.ToLongitude = codingFields.getValue("To Longitude");
                a.DepartureDate = codingFields.getValue("Departure Date");
                a.DepartureTime = codingFields.getValue("Departure Time");
                a.EstimatedTimeEnroute = codingFields.getValue("Estimated Time Enroute");
                a.EstimatedTimeOfArrival = codingFields.getValue("Estimated Time of Arrival");
                a.ActualTimeEnroute = codingFields.getValue("Actual Time Enroute");
                a.ActualTimeOfArrival = codingFields.getValue("Actual Time of Arrival");
                a.TravelDangerAlertTime = codingFields.getValue("Travel Danger Alert Time");
                a.TravelWarningAlertTime = codingFields.getValue("Travel Warning Alert Time");
                a.OverdueTimeLimit = codingFields.getValue("Overdue Time Limit");
                a.SalesOrderNumber = codingFields.getValue("Sales Order Number");
                a.FromAddress = codingFields.getValue("From Address");
                a.FromCity = codingFields.getValue("From City");
                a.FromState = codingFields.getValue("From State");
                a.FromZipcode = codingFields.getValue("From Zipcode");
                a.ToAddress = codingFields.getValue("To Address");
                a.ToCity = codingFields.getValue("To City");
                a.ToState = codingFields.getValue("To State");
                a.ToZipcode = codingFields.getValue("To Zipcode");
                a.VIN = codingFields.getValue("VIN");
                a.VehicleLicenseNumber = codingFields.getValue("Vehicle License Number");
                a.Make = codingFields.getValue("Make");
                a.Mack = codingFields.getValue("Mack");
                a.Model = codingFields.getValue("Model");
                a.Year = codingFields.getValue("Year");
                a.LastWorked = codingFields.getValue("Last Worked");
                a.Country = codingFields.getValue("Country");
                a.StateRegion = codingFields.getValue("StateRegion");
                a.City = codingFields.getValue("City");
                a.Company = codingFields.getValue("Company");
                a.FirstName = codingFields.getValue("First Name");
                a.LastName = codingFields.getValue("Last Name");
                a.MobilePhone = codingFields.getValue("MobilePhone");
                a.Latitude = codingFields.getValue("Latitude");
                a.Longitude = codingFields.getValue("Longitude");
                a.Active = codingFields.getValue("Active");
                a.Speed = codingFields.getValue("Speed");
                a.Temperature1 = codingFields.getValue("Temperature1");
                a.Temperature2 = codingFields.getValue("Temperature2");
                a.DoorStatus = codingFields.getValue("Door Status");
                a.Heading = codingFields.getValue("Heading");
                a.TruckNumber = codingFields.getValue("Truck Number");
                a.Odometer = codingFields.getValue("Odometer");
                a.EngineIdle = codingFields.getValue("EngineIdle");
                a.RPM = codingFields.getValue("RPM");
                a.EngineHours = codingFields.getValue("EngineHours");
                a.OdometerSetup = codingFields.getValue("Odometer Setup");
                a.Shock = codingFields.getValue("Shock");
                a.TirePressures = codingFields.getValue("Tire Pressures");
                a.EngineStatus = codingFields.getValue("Engine Status");
                a.HOSViolation = codingFields.getValue("HOS Violation");
                a.SpeedViolations = codingFields.getValue("Speed Violations");
                a.SmogViolation = codingFields.getValue("Smog Violation");
                a.LastMaintenanceDate = codingFields.getValue("Last Maintenance Date");
                a.LastAnnualInspectionDate = codingFields.getValue("Last Annual Inspection Date");
                a.Status = codingFields.getValue("Status");
                a.CustomerName = codingFields.getValue("Customer Name");
                a.CustomerNumber = codingFields.getValue("Customer Number");
                a.FuelCode = codingFields.getValue("Fuel Code");
                a.FuelType = codingFields.getValue("Fuel Type");
                a.Diesel = codingFields.getValue("Diesel");
                a.DOTNumber = codingFields.getValue("DOT Number");
                a.DOTExpirationDate = codingFields.getValue("DOT Expiration Date");
                a.IFTADecal = codingFields.getValue("IFTA Decal");
                a.IFTAFuelPermits = codingFields.getValue("IFTA Fuel Permits");
                a.IFTATripPermits = codingFields.getValue("IFTA Trip Permits");
                a.ReportingPeriod = codingFields.getValue("Reporting Period");

                result.add(a);
                insertTrucksEntry(a);
            }

            trucks.addAll(result);
        }
    }

    private void loadTrucks() {
        ArrayList<Truck> result = new ArrayList();
        Cursor c = db.getQuery("SELECT * FROM trucks");
        c.moveToFirst();
        int counter = 0;

        while (!c.isAfterLast()) {
            Truck t = new Truck();

            counter = 1;
            t.RecordId = c.getString(counter++);
            t.MobileRecordId = c.getString(counter++);
            t.VIN = c.getString(counter++);
            t.VehicleLicenseNumber = c.getString(counter++);
            t.TruckNumber = c.getString(counter++);
            t.RmsCodingTimestamp = c.getString(counter++);
            t.RmsTimestamp = c.getString(counter++);
            t.OrganizationName = c.getString(counter++);
            t.OrganizationNumber = c.getString(counter++);
            t.ContainerTypeRecordId = c.getString(counter++);
            t.UniqueKey1 = c.getString(counter++);
            t.Deployment = c.getString(counter++);
            t.IsaLocation = c.getString(counter++);
            t.CustomerRecordId = c.getString(counter++);
            t.ManufacturerSerialNumber = c.getString(counter++);
            t.Overdue = c.getString(counter++);
            t.StoreName = c.getString(counter++);
            t.StoreNumber = c.getString(counter++);
            t.StoreType = c.getString(counter++);
            t.DateLoaded = c.getString(counter++);
            t.DateUnloaded = c.getString(counter++);
            t.TimeLoaded = c.getString(counter++);
            t.TimeUnloaded = c.getString(counter++);
            t.FunctionalGroupName = c.getString(counter++);
            t.Managers = c.getString(counter++);
            t.FunctionalGroupObjectId = c.getString(counter++);
            t.DateDoorClosed = c.getString(counter++);
            t.TimeDoorClosed = c.getString(counter++);
            t.DateTime = c.getString(counter++);
            t.AlertCount = c.getString(counter++);
            t.VendorName = c.getString(counter++);
            t.Tracking = c.getString(counter++);
            t.ItemType = c.getString(counter++);
            t.Humidity = c.getString(counter++);
            t.GPSDailySampleRate = c.getString(counter++);
            t.GPSTripSampleRate = c.getString(counter++);
            t.TruckGPSServices = c.getString(counter++);
            t.FromLatitude = c.getString(counter++);
            t.FromLongitude = c.getString(counter++);
            t.ToLatitude = c.getString(counter++);
            t.ToLongitude = c.getString(counter++);
            t.DepartureDate = c.getString(counter++);
            t.DepartureTime = c.getString(counter++);
            t.EstimatedTimeEnroute = c.getString(counter++);
            t.EstimatedTimeOfArrival = c.getString(counter++);
            t.ActualTimeEnroute = c.getString(counter++);
            t.ActualTimeOfArrival = c.getString(counter++);
            t.TravelDangerAlertTime = c.getString(counter++);
            t.TravelWarningAlertTime = c.getString(counter++);
            t.OverdueTimeLimit = c.getString(counter++);
            t.SalesOrderNumber = c.getString(counter++);
            t.FromAddress = c.getString(counter++);
            t.FromCity = c.getString(counter++);
            t.FromState = c.getString(counter++);
            t.FromZipcode = c.getString(counter++);
            t.ToAddress = c.getString(counter++);
            t.ToCity = c.getString(counter++);
            t.ToState = c.getString(counter++);
            t.ToZipcode = c.getString(counter++);
            t.Make = c.getString(counter++);
            t.Mack = c.getString(counter++);
            t.Model = c.getString(counter++);
            t.Year = c.getString(counter++);
            t.LastWorked = c.getString(counter++);
            t.Country = c.getString(counter++);
            t.StateRegion = c.getString(counter++);
            t.City = c.getString(counter++);
            t.Company = c.getString(counter++);
            t.FirstName = c.getString(counter++);
            t.LastName = c.getString(counter++);
            t.MobilePhone = c.getString(counter++);
            t.Latitude = c.getString(counter++);
            t.Longitude = c.getString(counter++);
            t.Active = c.getString(counter++);
            t.Speed = c.getString(counter++);
            t.Temperature1 = c.getString(counter++);
            t.Temperature2 = c.getString(counter++);
            t.DoorStatus = c.getString(counter++);
            t.Heading = c.getString(counter++);
            t.Odometer = c.getString(counter++);
            t.EngineIdle = c.getString(counter++);
            t.RPM = c.getString(counter++);
            t.EngineHours = c.getString(counter++);
            t.OdometerSetup = c.getString(counter++);
            t.Shock = c.getString(counter++);
            t.TirePressures = c.getString(counter++);
            t.EngineStatus = c.getString(counter++);
            t.HOSViolation = c.getString(counter++);
            t.SpeedViolations = c.getString(counter++);
            t.SmogViolation = c.getString(counter++);
            t.LastMaintenanceDate = c.getString(counter++);
            t.LastAnnualInspectionDate = c.getString(counter++);
            t.Status = c.getString(counter++);
            t.CustomerName = c.getString(counter++);
            t.CustomerNumber = c.getString(counter++);
            t.FuelCode = c.getString(counter++);
            t.FuelType = c.getString(counter++);
            t.Diesel = c.getString(counter++);
            t.DOTNumber = c.getString(counter++);
            t.DOTExpirationDate = c.getString(counter++);
            t.IFTADecal = c.getString(counter++);
            t.IFTAFuelPermits = c.getString(counter++);
            t.IFTATripPermits = c.getString(counter++);

            result.add(t);
            c.moveToNext();
        }

        c.close();
        trucks = result;
    }

    public void syncTrailers() throws Exception {
//        May 26, 2022  -   The sync is done just at the first install
        if (db.exists("SELECT * FROM trailers")) {
            loadTrailers();
            return;
        }

        trailers = new ArrayList<>();
        ArrayList<Trailer> result = new ArrayList();

        String response = Rms.getRecordsUpdatedXFiltered("Trailer", -5000,
                "Organization+Name", ",", Rms.getOrgName().replace(" ", "+"), ",", "");
//        Log.d(TAG, "syncTrailers: response: " + response);

        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            clearTableEntries("trailers");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                Trailer a = new Trailer();
                a.RecordId = codingFields.getValue("RecordId");
                a.MobileRecordId = codingFields.getValue("MobileRecordId");
                a.FunctionalGroupName = codingFields.getValue("");
                a.Managers = codingFields.getValue("Managers");
                a.FunctionalGroupObjectId = codingFields.getValue("FunctionalGroupObjectId");
                a.RMSTimestamp = codingFields.getValue("RMS Timestamp");
                a.RMSCodingTimestamp = codingFields.getValue("RMS Coding Timestamp");
                a.UniqueKey1 = codingFields.getValue("Unique Key 1");
                a.OrganizationName = codingFields.getValue("Organization Name");
                a.OrganizationNumber = codingFields.getValue("Organization Number");
                a.VIN = codingFields.getValue("VIN");
                a.TrailerNumber = codingFields.getValue("Trailer Number");
                a.Make = codingFields.getValue("Make");
                a.Model = codingFields.getValue("Model");
                a.Year = codingFields.getValue("Year");
                a.LastWorked = codingFields.getValue("Last Worked");
                a.Country = codingFields.getValue("Country");
                a.StateRegion = codingFields.getValue("StateRegion");
                a.City = codingFields.getValue("City");
                a.Company = codingFields.getValue("Company");
                a.ProcessorId = codingFields.getValue("ProcessorId");
                a.ProcessorInstalled = codingFields.getValue("ProcessorInstalled");
                a.TruckNumber = codingFields.getValue("Truck Number");
                a.Latitude = codingFields.getValue("Latitude");
                a.Longitude = codingFields.getValue("Longitude");
                a.Active = codingFields.getValue("Active");
                a.Speed = codingFields.getValue("Speed");
                a.Temperature1 = codingFields.getValue("Temperature1");
                a.Temperature2 = codingFields.getValue("Temperature2");
                a.Humidity = codingFields.getValue("Humidity");
                a.DoorStatus = codingFields.getValue("Door Status");
                a.Heading = codingFields.getValue("Heading");
                a.GeneratorHours = codingFields.getValue("Generator Hours");
                a.Miles = codingFields.getValue("Miles");
                a.FuelRate = codingFields.getValue("Fuel Rate");
                a.Shock = codingFields.getValue("Shock");
                a.TirePressures = codingFields.getValue("Tire Pressures");
                a.ItemType = codingFields.getValue("ItemType");
                a.Treads = codingFields.getValue("Treads");
                a.LastMaintenanceDate = codingFields.getValue("Last Maintenance Date");
                a.LastAnnualInspectionDate = codingFields.getValue("Last Annual Inspection Date");
                a.Status = codingFields.getValue("Status");
                a.CustomerName = codingFields.getValue("Customer Name");
                a.CustomerNumber = codingFields.getValue("Customer Number");
                a.IsReefer = codingFields.getValue("IsReefer");
                a.ReeferHOS = codingFields.getValue("Reefer HOS");
                a.RecordReeferHOS = codingFields.getValue("Record Reefer HOS");

                result.add(a);
//                Log.d(TAG, "syncTrailers: insertTrailersEntry: ");
                insertTrailersEntry(a);
            }

            trailers.addAll(result);
        }
    }

    public void syncPretrip() throws Exception {
        Log.d(TAG, "syncPretrip: ");
        String timeStamp = "+";
//        May 26, 2022  -   The sync is done just at the first install
        boolean isExists = db.exists("SELECT * FROM pretrip WHERE objectId IS NOT NULL");
//        Log.d(TAG, "syncTruckEldEvents: isExists: " + isExists);
//        if (isExists) {
////            return ;
//            timeStamp = getMostRecentTimeStampForPreTrip(null);
//            if (timeStamp == null) {
                timeStamp = "+";
//            }
//        }
        Log.d(TAG, "syncPretrip: timeStamp: syncPretrip: timeStamp: " + timeStamp);

        if (timeStamp.equalsIgnoreCase("+")) {
//                June 08, 2022 -   We should clear the table only in case when we are doing a full sync
            clearTableEntries("pretrip", "objectId NOT NULL");
        }



        pretrips = new ArrayList<>();
        ArrayList<PretripModel> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");
        String userRecordId = user.getRecordId();

        Log.d(TAG, "syncPretrip: response: timeStamp: " + timeStamp);
        String response = Rms.getRecordsUpdatedXFiltered("Truck+DVIR+Detail", -5000,
                "Organization+Name,DriverRecordId", ",", orgName + "," + userRecordId, ",", "", timeStamp);
        Log.d(TAG, "syncPretrip: response: " + response);
        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            if (timeStamp.equalsIgnoreCase("+")) {
//                June 08, 2022 -   We should clear the table only in case when we are doing a full sync
                clearTableEntries("pretrip", "objectId NOT NULL");
            }

            Log.d(TAG, "syncPretrip: jsonArray: size: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(jsonObject);

                String objectId = "", objectType = "", mobileRecordId = "", functionalGroupName = "", organizationName = "", organizationNumber = "",
                        dateTime = "", latitude = "", longitude = "", firstName = "", lastName = "", recordId = "", vehicleLicenseNumber = "",
                        airCompressor = "", airLines = "", battery = "", brakeAccessories = "", brakes = "", carburetor = "", clutch = "", defroster = "",
                        driveLine = "", fifthWheel = "", frontalAxle = "", fuelTanks = "", heater = "", horn = "", lights = "", mirrors = "", oilPressure = "",
                        onBoardRecorder = "", radiator = "", rearEnd = "", reflectors = "", safetyEquipment = "", springs = "", starter = "", steering = "",
                        tachograph = "", tires = "", transmission = "", wheels = "", windows = "", windShieldWipers = "", others = "", trailer1 = "",
                        trailer1BreakConnections = "", trailer1Breaks = "", trailer1CouplingPin = "", trailer1CouplingChains = "", trailer1Doors = "",
                        trailer1Hitch = "", trailer1LandingGear = "", trailer1LightsAll = "", trailer1Roof = "", trailer1Springs = "", trailer1Tarpaulin = "",
                        trailer1Tires = "", trailer1Wheels = "", trailer1Others = "", trailer2 = "", trailer2BreakConnections = "", trailer2Breaks = "",
                        trailer2CouplingPin = "", trailer2CouplingChains = "", trailer2Doors = "", trailer2Hitch = "", trailer2LandingGear = "",
                        trailer2LightsAll = "", trailer2Roof = "", trailer2Springs = "", trailer2Tarpaulin = "", trailer2Tires = "", trailer2Wheels = "",
                        trailer2Others = "", remarks = "", conditionVehicleIsSatisfactory = "", driversSignatureVehicleSatisfactory = "", aboveDefectsCorrected = "",
                        aboveDefectsNoCorrectionNeeded = "", mechanicsSignatureDate = "", driversSignatureNoCorrectionNeeded = "",
                        driversSignatureNoCorrectionNeededDate = "", truckTractorNumber = "", carrier = "", address = "", odometer = "", mechanicFirstName = "",
                        mechanicLastName = "", mechanicRecordId = "", trailer1ReeferHOS = "", trailer2ReeferHOS = "",
                        registration = "", insurance = "", truckNumber = "", rmsTimestamp = "", sent = "";


                objectId = codingFields.getValue("LobjectId");
                objectType = codingFields.getValue("objectType");
                mobileRecordId = codingFields.getValue("mobileRecordId");

                organizationName = codingFields.getValue("Organization Name");
                organizationNumber = codingFields.getValue("Organization Number");
                dateTime = codingFields.getValue("Date");
                recordId = codingFields.getValue("RecordId");
                airCompressor = codingFields.getValue("Air Compressor");
                airLines = codingFields.getValue("Air Lines");
                battery = codingFields.getValue("Battery");
                brakeAccessories = codingFields.getValue("Brake Accessories");
                brakes = codingFields.getValue("Brakes");
                carburetor = codingFields.getValue("Carburetor");
                clutch = codingFields.getValue("Clutch");
                defroster = codingFields.getValue("Defroster");
                driveLine = codingFields.getValue("Drive Line");
                fifthWheel = codingFields.getValue("Fifth Wheel");
                frontalAxle = codingFields.getValue("Front Axle");
                fuelTanks = codingFields.getValue("Fuel Tanks");
                heater = codingFields.getValue("Heater");
                horn = codingFields.getValue("Horn");
                lights = codingFields.getValue("Lights");
                mirrors = codingFields.getValue("Mirrors");
                oilPressure = codingFields.getValue("Oil Pressure");
                onBoardRecorder = codingFields.getValue("On Board Recorder");
                radiator = codingFields.getValue("Radiator");
                rearEnd = codingFields.getValue("Rear End");
                reflectors = codingFields.getValue("Reflectors");
                safetyEquipment = codingFields.getValue("");
                springs = codingFields.getValue("Springs");
                starter = codingFields.getValue("Starter");
                steering = codingFields.getValue("Steering");
                tachograph = codingFields.getValue("Tachograph");
                tires = codingFields.getValue("Tires");
                transmission = codingFields.getValue("Transmission");
                wheels = codingFields.getValue("Wheels");
                windows = codingFields.getValue("Windows");
                windShieldWipers = codingFields.getValue("Windshield Wipers");
                others = codingFields.getValue("Other");

                truckNumber = codingFields.getValue("Truck Number");

                Log.d(TAG, "syncPretrip: truckNumber: "+truckNumber);
                Log.d(TAG, "syncPretrip: others: "+others);
//
//                        "Trailer1 Other": "1",
//                        "Trailer1 Tarpaulin": "1",
//                        "Trailer1 Brakes": "1",
//                        "Trailer1 Wheels": "1",
//                        "Trailer1 Reefer HOS": "54654",
//                        "Trailer1 Number": "66666",
//                        "Trailer1 Landing Gear": "1",
//                        "Trailer1 Hitch": "1",
//                        "Trailer1 Lights - All": "1",
//                        "Trailer1 Tires": "1",
//                        "Trailer1 Coupling Chains": "1",
//                        "Trailer1 Brake Connections": "1",
//                        "Trailer1 Doors": "1",
//                        "Trailer1 Springs": "1",
//                        "Trailer1 Roof": "1",
//                        "Trailer1 Coupling (King) Pin": "1",

                trailer1 = codingFields.getValue("Trailer1 Number");
                Log.d(TAG, "syncPretrip: trailer1: "+trailer1);
                trailer1BreakConnections = codingFields.getValue("Trailer1 Brake Connections");
                trailer1Breaks = codingFields.getValue("Trailer1 Brakes");
                trailer1CouplingPin = codingFields.getValue("Trailer1 Coupling (King) Pin");
                trailer1CouplingChains = codingFields.getValue("Trailer1 Coupling Chains");
                trailer1Doors = codingFields.getValue("Trailer1 Doors");
                trailer1Hitch = codingFields.getValue("Trailer1 Hitch");
                trailer1LandingGear = codingFields.getValue("Trailer1 Landing Gear");
                trailer1LightsAll = codingFields.getValue("Trailer1 Lights - All");
                trailer1Roof = codingFields.getValue("Trailer1 Roof");
                trailer1Springs = codingFields.getValue("Trailer1 Springs");
                trailer1Tarpaulin = codingFields.getValue("Trailer1 Tarpaulin");
                trailer1Tires = codingFields.getValue("Trailer1 Tires");
                trailer1Wheels = codingFields.getValue("Trailer1 Wheels");
                trailer1Others = codingFields.getValue("Trailer1 Other");
                Log.d(TAG, "syncPretrip: Trailer1 others: "+trailer1Others);
//
//                "Trailer2 Brakes": "1",
//                        "Trailer2 Coupling Chains": "1",
//                        "Trailer2 Doors": "1",
//                        "Trailer2 Wheels": "1",
//                        "Trailer2 Coupling (King) Pin": "1",
//                        "Trailer2 Hitch": "1",
//                        "Trailer2 Other": "1",
//                        "Trailer2 Tarpaulin": "1",
//                        "Trailer2 R": "1",
//                        "Trailer2 Lights - All": "1",
//                        "Trailer2 Springs": "1",
//                        "Trailer2 Laoofnding Gear": "1",
//                        "Trailer2 Tires": "1",
//                        "Trailer2 Brake Connections": "1",
                trailer2 = codingFields.getValue("Trailer2 Number");
                Log.d(TAG, "syncPretrip: trailer2: "+trailer2);
                trailer2BreakConnections = codingFields.getValue("Trailer2 Brake Connections");
                trailer2Breaks = codingFields.getValue("Trailer2 Brakes");
                trailer2CouplingPin = codingFields.getValue("Trailer2 Coupling (King) Pin");
                trailer2CouplingChains = codingFields.getValue("Trailer2 Coupling Chains");
                trailer2Doors = codingFields.getValue("Trailer2 Doors");
                trailer2Hitch = codingFields.getValue("Trailer2 Hitch");
                trailer2LandingGear = codingFields.getValue("Trailer2 Laoofnding Gear");
                trailer2LightsAll = codingFields.getValue("Trailer2 Lights - All");
                trailer2Roof = codingFields.getValue("Trailer2 R");
                trailer2Springs = codingFields.getValue("Trailer2 Springs");
                trailer2Tarpaulin = codingFields.getValue("Trailer2 Tarpaulin");
                trailer2Tires = codingFields.getValue("Trailer2 Tires");
                trailer2Wheels = codingFields.getValue("Trailer2 Wheels");
                trailer2Others = codingFields.getValue("Trailer2 Other");
                Log.d(TAG, "syncPretrip: Trailer2 others: "+trailer2Others);

                conditionVehicleIsSatisfactory = codingFields.getValue("Condition Vehicle Satisfactory");
                driversSignatureVehicleSatisfactory = codingFields.getValue("Drivers Signature Vehicle Satisfactory");
                aboveDefectsCorrected = codingFields.getValue("Above Defects Corrected");
                aboveDefectsNoCorrectionNeeded = codingFields.getValue("Above Defects No Corrections Needed");
                mechanicsSignatureDate = codingFields.getValue("Mechanics Signature Date");
                driversSignatureNoCorrectionNeeded = codingFields.getValue("Drivers Signature No Corrections Needed");
                driversSignatureNoCorrectionNeededDate = codingFields.getValue("Drivers Signature No Corrections Needed Date");
                odometer = codingFields.getValue("Odometer");
                trailer1ReeferHOS = codingFields.getValue("Trailer1 Reefer HOS");
                trailer2ReeferHOS = codingFields.getValue("Trailer2 Reefer HOS");
                registration = codingFields.getValue("Registration");
                insurance = codingFields.getValue("Insurance");
                Log.d(TAG, "syncPretrip: insurance: "+insurance);

//                Jan 13, 2022  -   (TODO) Check if all these entries exists
                remarks = codingFields.getValue("Remarks");
                carrier = codingFields.getValue("Logistics Carrier");
                truckTractorNumber = codingFields.getValue("Truck Number");
                address = codingFields.getValue("Address");
                mechanicFirstName = codingFields.getValue("Mechanic First Name");
                mechanicLastName = codingFields.getValue("Mechanic Last Name");
                mechanicRecordId = codingFields.getValue("Mechanic RecordId");
                Log.d(TAG, "syncPretrip: mechanicRecordId: "+mechanicRecordId);

                PretripModel pretripModel = new PretripModel();
                pretripModel.setObjectId(objectId);
                pretripModel.setObjectType(objectType);
                pretripModel.setMobileRecordId(mobileRecordId);
                pretripModel.setFunctionalGroupName(functionalGroupName);
                pretripModel.setOrganizationName(organizationName);
                pretripModel.setOrganizationNumber(organizationNumber);
                pretripModel.setDateTime(dateTime);
                pretripModel.setLatitude(latitude);
                pretripModel.setLongitude(longitude);
                pretripModel.setFirstName(firstName);
                pretripModel.setLastName(lastName);
                pretripModel.setRecordId(recordId);
                pretripModel.setVehicleLicenseNumber(vehicleLicenseNumber);
                pretripModel.setAirCompressor(airCompressor);
                pretripModel.setAirLines(airLines);
                pretripModel.setBattery(battery);
                pretripModel.setBrakeAccessories(brakeAccessories);
                pretripModel.setBrakes(brakes);
                pretripModel.setCarburetor(carburetor);
                pretripModel.setClutch(clutch);
                pretripModel.setDefroster(defroster);
                pretripModel.setDriveLine(driveLine);
                pretripModel.setFifthWheel(fifthWheel);
                pretripModel.setFrontalAxle(frontalAxle);
                pretripModel.setFuelTanks(fuelTanks);
                pretripModel.setHeater(heater);
                pretripModel.setHorn(horn);
                pretripModel.setLights(lights);
                pretripModel.setMirrors(mirrors);
                pretripModel.setOilPressure(oilPressure);
                pretripModel.setOnBoardRecorder(onBoardRecorder);
                pretripModel.setRadiator(radiator);
                pretripModel.setRearEnd(rearEnd);
                pretripModel.setReflectors(reflectors);
                pretripModel.setSafetyEquipment(safetyEquipment);
                pretripModel.setSprings(springs);
                pretripModel.setStarter(starter);
                pretripModel.setSteering(steering);
                pretripModel.setTachograph(tachograph);
                pretripModel.setTires(tires);
                pretripModel.setTransmission(transmission);
                pretripModel.setWheels(wheels);
                pretripModel.setWindows(windows);
                pretripModel.setWindShieldWipers(windShieldWipers);
                pretripModel.setOthers(others);
                pretripModel.setTruckNumber(truckNumber);

                pretripModel.setTrailer1(trailer1);
                pretripModel.setTrailer1BreakConnections(trailer1BreakConnections);
                pretripModel.setTrailer1Breaks(trailer1Breaks);
                pretripModel.setTrailer1CouplingPin(trailer1CouplingPin);
                pretripModel.setTrailer1CouplingChains(trailer1CouplingChains);
                pretripModel.setTrailer1Doors(trailer1Doors);
                pretripModel.setTrailer1Hitch(trailer1Hitch);
                pretripModel.setTrailer1LandingGear(trailer1LandingGear);
                pretripModel.setTrailer1LightsAll(trailer1LightsAll);
                pretripModel.setTrailer1Roof(trailer1Roof);
                pretripModel.setTrailer1Springs(trailer1Springs);
                pretripModel.setTrailer1Tarpaulin(trailer1Tarpaulin);
                pretripModel.setTrailer1Tires(trailer1Tires);
                pretripModel.setTrailer1Wheels(trailer1Wheels);
                pretripModel.setTrailer1Others(trailer1Others);
                pretripModel.setTrailer2(trailer2);
                pretripModel.setTrailer2BreakConnections(trailer2BreakConnections);
                pretripModel.setTrailer2Breaks(trailer2Breaks);
                pretripModel.setTrailer2CouplingPin(trailer2CouplingPin);
                pretripModel.setTrailer2CouplingChains(trailer2CouplingChains);
                pretripModel.setTrailer2Doors(trailer2Doors);
                pretripModel.setTrailer2Hitch(trailer2Hitch);
                pretripModel.setTrailer2LandingGear(trailer2LandingGear);
                pretripModel.setTrailer2LightsAll(trailer2LightsAll);
                pretripModel.setTrailer2Roof(trailer2Roof);
                pretripModel.setTrailer2Springs(trailer2Springs);
                pretripModel.setTrailer2Tarpaulin(trailer2Tarpaulin);
                pretripModel.setTrailer2Tires(trailer2Tires);
                pretripModel.setTrailer2Wheels(trailer2Wheels);
                pretripModel.setTrailer2Others(trailer2Others);

                pretripModel.setRemarks(remarks);
                pretripModel.setConditionVehicleIsSatisfactory(conditionVehicleIsSatisfactory);
                pretripModel.setDriversSignatureVehicleSatisfactory(driversSignatureVehicleSatisfactory);
                pretripModel.setAboveDefectsCorrected(aboveDefectsCorrected);
                pretripModel.setAboveDefectsNoCorrectionNeeded(aboveDefectsNoCorrectionNeeded);
                pretripModel.setMechanicsSignatureDate(mechanicsSignatureDate);
                pretripModel.setDriversSignatureNoCorrectionNeeded(driversSignatureNoCorrectionNeeded);
                pretripModel.setDriversSignatureNoCorrectionNeededDate(driversSignatureNoCorrectionNeededDate);
                pretripModel.setTruckNumber(truckTractorNumber);
                pretripModel.setCarrier(carrier);
                pretripModel.setAddress(address);
                pretripModel.setOdometer(odometer);
                pretripModel.setMechanicFirstName(mechanicFirstName);
                pretripModel.setMechanicLastName(mechanicLastName);
                pretripModel.setMechanicRecordId(mechanicRecordId);

                pretripModel.setTrailer1ReeferHOS(trailer1ReeferHOS);
                pretripModel.setTrailer2ReeferHOS(trailer2ReeferHOS);
                pretripModel.setRegistration(registration);
                pretripModel.setInsurance(insurance);

                pretripModel.setRmsTimestamp(rmsTimestamp);
                pretripModel.setSent(sent);

                Log.d(TAG, "syncPretrip: everything set: now add: ");

                result.add(pretripModel);
                insertPretrip(pretripModel, true);
            }

            pretrips.addAll(result);
            Log.d(TAG, "syncPretrip: pretrips: size: "+pretrips.size());
        }
    }


    public void syncTollRoadReceipt() throws Exception {
        Log.d(TAG, "syncTollRoadReceipt: ");
        String timeStamp = "+";
//        May 26, 2022  -   The sync is done just at the first install
        boolean isExists = db.exists("SELECT * FROM tollreceipt WHERE objectId IS NOT NULL");
//        Log.d(TAG, "syncTruckEldEvents: isExists: " + isExists);
        if (isExists) {
//            return ;
            timeStamp = getMostRecentTimeStampForTollReceipt(null);
            if (timeStamp == null) {
                timeStamp = "+";
            }
        }
        Log.d(TAG, "timeStamp: syncTollRoadReceipt: timeStamp: " + timeStamp);


        tollReceipts = new ArrayList<>();
        ArrayList<TollReceiptModel> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");
        String userRecordId = user.getRecordId();

//        Log.d(TAG, "syncTruckEldEvents: response: timeStamp: " + timeStamp);
        String response = Rms.getRecordsUpdatedXFiltered("Toll Receipt", -5000,
                "Organization+Name,UserRecordId", ",", orgName + "," + userRecordId, ",", "", timeStamp);
//        Log.d(TAG, "syncTruckEldEvents: response: " + response);
        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            if (timeStamp.equalsIgnoreCase("+")) {
//                June 08, 2022 -   We should clear the table only in case when we are doing a full sync
                clearTableEntries("tollreceipt", "objectId NOT NULL");
            }

//            Log.d(TAG, "syncTruckEldEvents: jsonArray: size: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);


                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                TollReceiptModel a = new TollReceiptModel();
                a.setTollReceiptObjectId(codingFields.getValue("LobjectId"));
                a.setTollReceiptObjectType(codingFields.getValue("objectType"));
                a.setTollReceiptRecordId(codingFields.getValue("RecordId"));
                a.setTollReceiptMobileRecordId(codingFields.getValue("MobileRecordId"));
                a.setTollReceiptRMSCodingTimestamp(codingFields.getValue("RMS Coding Timestamp"));
                a.setTollReceiptOrganizationName(codingFields.getValue("Organization Name"));
                a.setTollReceiptCompany(codingFields.getValue("Company"));
                a.setTollReceiptVendorState(codingFields.getValue("Vendor State"));
                a.setTollReceiptBarCode(codingFields.getValue("BarCode"));
                a.setTollReceiptAmount(codingFields.getValue("Amount"));
                a.setTollReceiptFirstName(codingFields.getValue("First Name"));
                a.setTollReceiptCreationTime(codingFields.getValue("Creation Time"));
                a.setTollReceiptOrganizationNumber(codingFields.getValue("Organization Number"));
                a.setTollReceiptVendorName(codingFields.getValue("Vendor Name"));
                a.setTollReceiptCreationDate(codingFields.getValue("Creation Date"));
                a.setTollReceiptUserRecordId(codingFields.getValue("UserRecordId"));
                a.setTollReceiptRMSTimestamp(codingFields.getValue("RMS Timestamp"));
                a.setTollReceiptVendorCountry(codingFields.getValue("Vendor Country"));
                a.setTollReceiptLastName(codingFields.getValue("Last Name"));
                a.setTollReceiptRoadName(codingFields.getValue("Road Name"));
                a.setTollReceiptDateTime(codingFields.getValue("DateTime"));

                //                Dec 07, 2022  -   I am replacing "-" with "/" right at the point so when we get datetime with "-" format we can replace
//                it with "/" instead of many other places
//                String dateTime = codingFields.getValue("DateTime");
//                if (dateTime != null) {
//                    dateTime = dateTime.replace("-", "/");
//                    a.setTollReceiptDateTime(dateTime);
//                } else {
//                    a.setTollReceiptDateTime(dateTime);
//                }

                result.add(a);
                insertTollReceipt(a, true);
            }

            tollReceipts.addAll(result);
        }
    }

    public void syncFuelReceipt() throws Exception {
        Log.d(TAG, "syncFuelReceipt: ");
        String timeStamp = "+";
//        May 26, 2022  -   The sync is done just at the first install
        boolean isExists = db.exists("SELECT * FROM fuelreceipt WHERE objectId IS NOT NULL");
//        Log.d(TAG, "syncTruckEldEvents: isExists: " + isExists);
        if (isExists) {
//            return ;
            timeStamp = getMostRecentTimeStampForFuelReceipt(null);
            Log.d(TAG, "rmsTimestamp: syncFuelReceipt: timeStamp: " + timeStamp);
            if (timeStamp == null) {
                timeStamp = "+";
            }
        }

        fuelReceipts = new ArrayList<>();
        ArrayList<FuelReceiptModel> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");
        String userRecordId = user.getRecordId();

        String response = Rms.getRecordsUpdatedXFiltered("Fuel Receipt", -5000,
                "Organization+Name,UserRecordId", ",", orgName + "," + userRecordId, ",", "", timeStamp);
//        Log.d(TAG, "syncTruckEldEvents: response: " + response);
        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);

//            Dec 07, 2022  -   Fuel receipts keeps dublicating entries when open app each time so for now I am clearing its data when app starts
            if (timeStamp.equalsIgnoreCase("+")) {
//                June 08, 2022 -   We should clear the table only in case when we are doing a full sync
                clearTableEntries("fuelreceipt", "objectId NOT NULL");
            }

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                FuelReceiptModel fuelReceiptModel = new FuelReceiptModel();
                fuelReceiptModel.setFuelReceiptObjectId(codingFields.getValue("LobjectId"));
                fuelReceiptModel.setFuelReceiptObjectType(codingFields.getValue("objectType"));
                fuelReceiptModel.setFuelReceiptRecordId(codingFields.getValue("RecordId"));
                fuelReceiptModel.setFuelReceiptRMSCodingTimestamp(codingFields.getValue("RMS Coding Timestamp"));
                fuelReceiptModel.setFuelReceiptOrganizationName(codingFields.getValue("Organization Name"));
                fuelReceiptModel.setFuelReceiptCompany(codingFields.getValue("Company"));
                fuelReceiptModel.setFuelReceiptState(codingFields.getValue("Vendor State"));
                fuelReceiptModel.setFuelReceiptBarCode(codingFields.getValue("BarCode"));
                fuelReceiptModel.setFuelReceiptAmount(codingFields.getValue("Amount"));
                fuelReceiptModel.setFuelReceiptMobileRecordId(codingFields.getValue("MobileRecordId"));
                fuelReceiptModel.setFuelReceiptFirstName(codingFields.getValue("First Name"));
                fuelReceiptModel.setFuelReceiptCreationTime(codingFields.getValue("Creation Time"));
                fuelReceiptModel.setFuelReceiptOrganizationNumber(codingFields.getValue("Organization Number"));
                fuelReceiptModel.setFuelReceiptTruckStop(codingFields.getValue("Vendor Name"));
                fuelReceiptModel.setFuelReceiptDateTime(codingFields.getValue("DateTime"));

//                Dec 07, 2022  -   I am replacing "-" with "/" right at the point so when we get datetime with "-" format we can replace
//                it with "/" instead of many other places
//                String dateTime = codingFields.getValue("DateTime");
//                if (dateTime != null) {
//                    dateTime = dateTime.replace("-", "/");
//                    fuelReceiptModel.setFuelReceiptDateTime(dateTime);
//                } else {
//                    fuelReceiptModel.setFuelReceiptDateTime(dateTime);
//                }

                fuelReceiptModel.setFuelReceiptCreationDate(codingFields.getValue("Creation Date"));
                fuelReceiptModel.setFuelReceiptUserRecordId(codingFields.getValue("UserRecordId"));
                fuelReceiptModel.setFuelReceiptRMSTimestamp(codingFields.getValue("RMS Timestamp"));
                Log.d(TAG, "syncFuelReceipt: rmsTimeStamp: " + fuelReceiptModel.getFuelReceiptRMSTimestamp());
                fuelReceiptModel.setFuelReceiptCountry(codingFields.getValue("Vendor Country"));
                fuelReceiptModel.setFuelReceiptLastName(codingFields.getValue("Last Name"));
                fuelReceiptModel.setFuelReceiptFuelType(codingFields.getValue("Fuel Type"));
//                Dec 05, 2022  -   We don't have any parameter for price per gallons
//                fuelReceiptModel.setFuelReceiptPricePerGallons(codingFields.getValue("Price per Gallon"));

                fuelReceiptModel.setFuelReceiptAmount(codingFields.getValue("Total Amount of Sale in USD"));
                fuelReceiptModel.setFuelReceiptSalesTax(codingFields.getValue("Sales Tax"));
                fuelReceiptModel.setFuelReceiptGallons(codingFields.getValue("Number of Gallons Purchased"));

//                fuelReceiptModel.setMonth(codingFields.getValue("Month"));
//                fuelReceiptModel.setDay(codingFields.getValue("Day"));
//                fuelReceiptModel.setYear(codingFields.getValue("Year"));


//                Dec 05, 2022  -   we are not saving below values in our db
//                "Quarter": "3",
//                        "Driver License Number": "485745",
//                        "OwnerRecordId": "2063763",
//                        "Minutes": "00",
//                        "Month": "11",
//                        "Purchasers Name": "C_Sharp Usman",
//                        "Hour": "00",
//                        "Price per Gallon": "10",
//                        "Odometer": "0",
//                        "ObjectName": "2022-11-25 00:00, CO Colorado, 45120",
//                        "Year": "2022",
//                        "Fuel Code": "01",
//                        "Last Name": "Usman",
//                        "Day": "25",


                result.add(fuelReceiptModel);
                fuelReceipts.add(fuelReceiptModel);

                if (!isEntryAlreadyExists(fuelReceiptModel.getFuelReceiptObjectId())) {
                    insertFuelReceipt(fuelReceiptModel, true);
                }
            }

//            fuelReceipts.addAll(result);
        }
    }


    public void syncTollRoadReceipt(String timeStamp) throws Exception {
        Log.d(TAG, "syncTollRoadReceipt: ");
//        String timeStamp = "+";
//        May 26, 2022  -   The sync is done just at the first install
//        boolean isExists = db.exists("SELECT * FROM tollreceipt WHERE objectId IS NOT NULL");
//        Log.d(TAG, "syncTruckEldEvents: isExists: " + isExists);
//        if (isExists) {
////            return ;
//            timeStamp = getMostRecentTimeStampForTollReceipt(null);
//            if (timeStamp == null) {
//                timeStamp = "+";
//            }
//        }
        Log.d(TAG, "timeStamp: syncTollRoadReceipt: timeStamp: " + timeStamp);


        tollReceipts = new ArrayList<>();
        ArrayList<TollReceiptModel> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");
        String userRecordId = user.getRecordId();

//        Log.d(TAG, "syncTruckEldEvents: response: timeStamp: " + timeStamp);
        String response = Rms.getRecordsUpdatedXFiltered("Toll Receipt", -5000,
                "Organization+Name,UserRecordId", ",", orgName + "," + userRecordId, ",", "", timeStamp);
        Log.d(TAG, "syncTollRoadReceipt: response: " + response);
        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
//            if (timeStamp.equalsIgnoreCase("+")) {
//                June 08, 2022 -   We should clear the table only in case when we are doing a full sync
//                clearTableEntries("tollreceipt", "objectId NOT NULL");
            clearTableEntries("tollreceipt", "id NOT NULL");
//            }

//            Log.d(TAG, "syncTruckEldEvents: jsonArray: size: " + jsonArray.length());
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);


                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                TollReceiptModel a = new TollReceiptModel();
                a.setTollReceiptObjectId(codingFields.getValue("LobjectId"));
                a.setTollReceiptObjectType(codingFields.getValue("objectType"));
                a.setTollReceiptRecordId(codingFields.getValue("RecordId"));
                a.setTollReceiptMobileRecordId(codingFields.getValue("MobileRecordId"));
                a.setTollReceiptRMSCodingTimestamp(codingFields.getValue("RMS Coding Timestamp"));
                a.setTollReceiptOrganizationName(codingFields.getValue("Organization Name"));
                a.setTollReceiptCompany(codingFields.getValue("Company"));
                a.setTollReceiptVendorState(codingFields.getValue("Vendor State"));
                a.setTollReceiptBarCode(codingFields.getValue("BarCode"));
                a.setTollReceiptAmount(codingFields.getValue("Amount"));
                a.setTollReceiptFirstName(codingFields.getValue("First Name"));
                a.setTollReceiptCreationTime(codingFields.getValue("Creation Time"));
                a.setTollReceiptOrganizationNumber(codingFields.getValue("Organization Number"));
                a.setTollReceiptVendorName(codingFields.getValue("Vendor Name"));
                a.setTollReceiptCreationDate(codingFields.getValue("Creation Date"));
                a.setTollReceiptUserRecordId(codingFields.getValue("UserRecordId"));
                a.setTollReceiptRMSTimestamp(codingFields.getValue("RMS Timestamp"));
                a.setTollReceiptVendorCountry(codingFields.getValue("Vendor Country"));
                a.setTollReceiptLastName(codingFields.getValue("Last Name"));
                a.setTollReceiptRoadName(codingFields.getValue("Road Name"));
                a.setTollReceiptDateTime(codingFields.getValue("DateTime"));

                //                Dec 07, 2022  -   I am replacing "-" with "/" right at the point so when we get datetime with "-" format we can replace
//                it with "/" instead of many other places
//                String dateTime = codingFields.getValue("DateTime");
//                if (dateTime != null) {
//                    dateTime = dateTime.replace("-", "/");
//                    a.setTollReceiptDateTime(dateTime);
//                } else {
//                    a.setTollReceiptDateTime(dateTime);
//                }

                result.add(a);
                insertTollReceipt(a, true);
            }

            tollReceipts.addAll(result);
        }
    }

    public void syncFuelReceipt(String timeStamp) throws Exception {
        Log.d(TAG, "syncTollRoadReceipt: syncFuelReceipt: timestamp: " + timeStamp);
//        String timeStamp = "+";
//        May 26, 2022  -   The sync is done just at the first install
//        boolean isExists = db.exists("SELECT * FROM fuelreceipt WHERE objectId IS NOT NULL");
//        Log.d(TAG, "syncTruckEldEvents: isExists: " + isExists);
//        if (isExists) {
////            return ;
//            timeStamp = getMostRecentTimeStampForFuelReceipt(null);
//            Log.d(TAG, "rmsTimestamp: syncFuelReceipt: timeStamp: " + timeStamp);
//            if (timeStamp == null) {
//                timeStamp = "+";
//            }
//        }

        fuelReceipts = new ArrayList<>();
        ArrayList<FuelReceiptModel> result = new ArrayList();

        String orgName = Rms.getOrgName().replace(" ", "+");
        String userRecordId = user.getRecordId();

        String response = Rms.getRecordsUpdatedXFiltered("Fuel Receipt", -5000,
                "Organization+Name,UserRecordId", ",", orgName + "," + userRecordId, ",", "", timeStamp);
        Log.d(TAG, "syncTollRoadReceipt: syncFuelReceipt: response: " + response);
        if (response != null && response.trim().length() > 0) {


            JSONArray jsonArray = new JSONArray(response);

//            Dec 07, 2022  -   Fuel receipts keeps dublicating entries when open app each time so for now I am clearing its data when app starts
//            if (timeStamp.equalsIgnoreCase("+")) {
//                June 08, 2022 -   We should clear the table only in case when we are doing a full sync
//                clearTableEntries("fuelreceipt", "objectId NOT NULL");
//                clearTableEntries("fuelreceipt", "id NOT NULL");
            db.delete("fuelreceipt", null);
            fuelReceipts.clear();
//            }

            Log.d(TAG, "syncFuelReceipt: jsonArray: " + jsonArray);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                FuelReceiptModel fuelReceiptModel = new FuelReceiptModel();
                fuelReceiptModel.setFuelReceiptObjectId(codingFields.getValue("LobjectId"));
                fuelReceiptModel.setFuelReceiptObjectType(codingFields.getValue("objectType"));
                fuelReceiptModel.setFuelReceiptRecordId(codingFields.getValue("RecordId"));
                fuelReceiptModel.setFuelReceiptRMSCodingTimestamp(codingFields.getValue("RMS Coding Timestamp"));
                fuelReceiptModel.setFuelReceiptOrganizationName(codingFields.getValue("Organization Name"));
                fuelReceiptModel.setFuelReceiptCompany(codingFields.getValue("Company"));
                fuelReceiptModel.setFuelReceiptState(codingFields.getValue("Vendor State"));
                fuelReceiptModel.setFuelReceiptBarCode(codingFields.getValue("BarCode"));
                fuelReceiptModel.setFuelReceiptAmount(codingFields.getValue("Amount"));
                fuelReceiptModel.setFuelReceiptMobileRecordId(codingFields.getValue("MobileRecordId"));
                fuelReceiptModel.setFuelReceiptFirstName(codingFields.getValue("First Name"));
                fuelReceiptModel.setFuelReceiptCreationTime(codingFields.getValue("Creation Time"));
                fuelReceiptModel.setFuelReceiptOrganizationNumber(codingFields.getValue("Organization Number"));
                fuelReceiptModel.setFuelReceiptTruckStop(codingFields.getValue("Vendor Name"));
                fuelReceiptModel.setFuelReceiptDateTime(codingFields.getValue("DateTime"));

//                Dec 07, 2022  -   I am replacing "-" with "/" right at the point so when we get datetime with "-" format we can replace
//                it with "/" instead of many other places
//                String dateTime = codingFields.getValue("DateTime");
//                if (dateTime != null) {
//                    dateTime = dateTime.replace("-", "/");
//                    fuelReceiptModel.setFuelReceiptDateTime(dateTime);
//                } else {
//                    fuelReceiptModel.setFuelReceiptDateTime(dateTime);
//                }

                fuelReceiptModel.setFuelReceiptCreationDate(codingFields.getValue("Creation Date"));
                fuelReceiptModel.setFuelReceiptUserRecordId(codingFields.getValue("UserRecordId"));
                fuelReceiptModel.setFuelReceiptRMSTimestamp(codingFields.getValue("RMS Timestamp"));
                Log.d(TAG, "syncFuelReceipt: rmsTimeStamp: " + fuelReceiptModel.getFuelReceiptRMSTimestamp());
                fuelReceiptModel.setFuelReceiptCountry(codingFields.getValue("Vendor Country"));
                fuelReceiptModel.setFuelReceiptLastName(codingFields.getValue("Last Name"));
                fuelReceiptModel.setFuelReceiptFuelType(codingFields.getValue("Fuel Type"));
//                Dec 05, 2022  -   We don't have any parameter for price per gallons
//                fuelReceiptModel.setFuelReceiptPricePerGallons(codingFields.getValue("Price per Gallon"));

                fuelReceiptModel.setFuelReceiptAmount(codingFields.getValue("Total Amount of Sale in USD"));
                fuelReceiptModel.setFuelReceiptSalesTax(codingFields.getValue("Sales Tax"));
                fuelReceiptModel.setFuelReceiptGallons(codingFields.getValue("Number of Gallons Purchased"));

//                fuelReceiptModel.setMonth(codingFields.getValue("Month"));
//                fuelReceiptModel.setDay(codingFields.getValue("Day"));
//                fuelReceiptModel.setYear(codingFields.getValue("Year"));


//                Dec 05, 2022  -   we are not saving below values in our db
//                "Quarter": "3",
//                        "Driver License Number": "485745",
//                        "OwnerRecordId": "2063763",
//                        "Minutes": "00",
//                        "Month": "11",
//                        "Purchasers Name": "C_Sharp Usman",
//                        "Hour": "00",
//                        "Price per Gallon": "10",
//                        "Odometer": "0",
//                        "ObjectName": "2022-11-25 00:00, CO Colorado, 45120",
//                        "Year": "2022",
//                        "Fuel Code": "01",
//                        "Last Name": "Usman",
//                        "Day": "25",


                result.add(fuelReceiptModel);
//                fuelReceipts.add(fuelReceiptModel);

                if (!isEntryAlreadyExists(fuelReceiptModel.getFuelReceiptObjectId())) {
                    insertFuelReceipt(fuelReceiptModel, true);
                }
            }

            fuelReceipts.addAll(result);
            Log.d(TAG, "syncFuelReceipt: fuelreceipts: size: " + fuelReceipts.size());
        }
    }


    boolean isEntryAlreadyExists(String objectId) {

        for (int i = 0; i < fuelReceipts.size(); i++) {
            if (fuelReceipts.get(i).getFuelReceiptObjectId().equalsIgnoreCase(objectId)) {
                return true;
            }
        }

        return false;
    }


    private void loadTrailers() {
        ArrayList<Trailer> result = new ArrayList();
        Cursor c = db.getQuery("SELECT * FROM trailers");
        c.moveToFirst();
        int counter = 0;

        while (!c.isAfterLast()) {
            Trailer t = new Trailer();

            counter = 1;
            t.RecordId = c.getString(counter++);
            t.MobileRecordId = c.getString(counter++);
            t.VIN = c.getString(counter++);
            t.TrailerNumber = c.getString(counter++);
            t.TruckNumber = c.getString(counter++);

            result.add(t);
            c.moveToNext();
        }

        c.close();
        trailers = result;
    }

    public boolean existsPendingSyncItem() {
        Log.d(TAG, "existsPendingSyncItem: db: " + db);
//        Oct 31, 2022  -   A crash occurred here because .exists call on a null reference
//        Ask Dragos - is it okay to have this piece of code here
        if (db == null) {
            instatiateDatabase(getLastContext());
        }

        boolean existsPendingSyncItem = !db.exists("SELECT * FROM trucks") ||
                !db.exists("SELECT * FROM trailers") ||
                !db.exists("SELECT * FROM rules") ||
                !db.exists("SELECT * FROM eldevents") ||
                !db.exists("SELECT * FROM trucklogheader") ||
                !db.exists("SELECT * FROM truckeldheader") ||
                !db.exists("SELECT * FROM settings");
        Log.d(TAG, "existsPendingSyncItem: existsPendingSyncItem: " + existsPendingSyncItem);

        return existsPendingSyncItem;
    }

    public void clearSyncItems() {
        deleteUserRights(user.getRmsUserId());

        clearTableEntries("trucks");
        clearTableEntries("trailers");
        clearTableEntries("rules");
        clearTableEntries("settings");
    }

    private void clearTableEntries(String tableName) {
        db.delete(tableName);
    }

    private void clearTableEntries(String tableName, String whereClause) {
        db.delete(tableName, whereClause);
    }

    private void insertEldEventsEntry(EldEvent t) {
        ContentValues values = new ContentValues();

        values.put("objectId", t.objectId);
        values.put("objectType", t.objectType);
        values.put("recordId", t.RecordId);
        values.put("mobileRecordId", t.MobileRecordId);
        values.put("organizationName", t.OrganizationName);
        values.put("organizationNumber", t.OrganizationNumber);
        values.put("eldUsername", t.EldUsername);
        values.put("eventType", t.EventType);
        values.put("eventCode", t.EventCode);
        values.put("recordStatus", t.RecordStatus);
        values.put("recordOrigin", t.RecordOrigin);
        values.put("truckNumber", t.TruckNumber);
        values.put("vin", t.Vin);
        values.put("localizationDescription", t.LocalizationDescription);
        values.put("latitudeString", t.LatitudeString);
        values.put("longitudeString", t.LongitudeString);
        values.put("dstSinceLastValidCoords", t.DstSinceLastValidCoords);
        values.put("vehicleMiles", t.VehicleMiles);
        values.put("engineHours", t.EngineHours);
        values.put("orderNumbercmv", t.OrderNumberCmv);
        values.put("orderNumberUser", t.OrderNumberUser);
        values.put("sequenceId", t.SequenceId);
        values.put("eventCodeDescription", t.EventCodeDescription);
        values.put("diagnosticIndicator", t.DiagnosticIndicator);
        values.put("malfunctionIndicator", t.MalfunctionIndicator);
        values.put("annotation", t.Annotation);
        values.put("recordOriginId", t.RecordOriginId);
        values.put("checkData", t.CheckData);
        values.put("checkSum", t.CheckSum);
        values.put("malfunctionDiagnosticCode", t.MalfunctionDiagnosticCode);
        values.put("malfunctionDiagnosticDescp", t.MalfunctionDiagnosticDescp);
        values.put("driverLastName", t.DriverLastName);
        values.put("driverFirstName", t.DriverFirstName);
        values.put("driverRecordId", t.DriverRecordId);
        values.put("editReason", t.EditReason);
        values.put("eventseconds", t.EventSeconds);
        values.put("shiftstart", t.ShiftStart);
        values.put("senteldevent", "1");
        values.put("senttrucklogs", "1");
        values.put("senteldlogs", "1");
        values.put("creationdate", t.CreationDate);
        values.put("odometer", t.Odometer);
        values.put("rmsTimestamp", t.RmsTimestamp);

        db.insert("eldevents", values);
    }

    private void insertTrucksEntry(Truck t) {
        ContentValues values = new ContentValues();

        values.put("recordId", t.RecordId);
        values.put("mobileRecordId", t.MobileRecordId);
        values.put("vin", t.VIN);
        values.put("vehicleLicenseNumber", t.VehicleLicenseNumber);
        values.put("truckNumber", t.TruckNumber);

        values.put("RmsCodingTimestamp", t.RmsCodingTimestamp);
        values.put("RmsTimestamp", t.RmsTimestamp);
        values.put("OrganizationName", t.OrganizationName);
        values.put("OrganizationNumber", t.OrganizationNumber);
        values.put("ContainerTypeRecordId", t.ContainerTypeRecordId);
        values.put("UniqueKey1", t.UniqueKey1);
        values.put("Deployment", t.Deployment);
        values.put("IsaLocation", t.IsaLocation);
        values.put("CustomerRecordId", t.CustomerRecordId);
        values.put("ManufacturerSerialNumber", t.ManufacturerSerialNumber);
        values.put("Overdue", t.Overdue);
        values.put("StoreName", t.StoreName);
        values.put("StoreNumber", t.StoreNumber);
        values.put("StoreType", t.StoreType);
        values.put("DateLoaded", t.DateLoaded);
        values.put("DateUnloaded", t.DateUnloaded);
        values.put("TimeLoaded", t.TimeLoaded);
        values.put("TimeUnloaded", t.TimeUnloaded);
        values.put("FunctionalGroupName", t.FunctionalGroupName);
        values.put("Managers", t.Managers);
        values.put("FunctionalGroupObjectId", t.FunctionalGroupObjectId);
        values.put("DateDoorClosed", t.DateDoorClosed);
        values.put("TimeDoorClosed", t.TimeDoorClosed);
        values.put("DateTime", t.DateTime);
        values.put("AlertCount", t.AlertCount);
        values.put("VendorName", t.VendorName);
        values.put("Tracking", t.Tracking);
        values.put("ItemType", t.ItemType);
        values.put("Humidity", t.Humidity);
        values.put("GPSDailySampleRate", t.GPSDailySampleRate);
        values.put("GPSTripSampleRate", t.GPSTripSampleRate);
        values.put("TruckGPSServices", t.TruckGPSServices);
        values.put("FromLatitude", t.FromLatitude);
        values.put("FromLongitude", t.FromLongitude);
        values.put("ToLatitude", t.ToLatitude);
        values.put("ToLongitude", t.ToLongitude);
        values.put("DepartureDate", t.DepartureDate);
        values.put("DepartureTime", t.DepartureTime);
        values.put("EstimatedTimeEnroute", t.EstimatedTimeEnroute);
        values.put("EstimatedTimeOfArrival", t.EstimatedTimeOfArrival);
        values.put("ActualTimeEnroute", t.ActualTimeEnroute);
        values.put("ActualTimeOfArrival", t.ActualTimeOfArrival);
        values.put("TravelDangerAlertTime", t.TravelDangerAlertTime);
        values.put("TravelWarningAlertTime", t.TravelWarningAlertTime);
        values.put("OverdueTimeLimit", t.OverdueTimeLimit);
        values.put("SalesOrderNumber", t.SalesOrderNumber);
        values.put("FromAddress", t.FromAddress);
        values.put("FromCity", t.FromCity);
        values.put("FromState", t.FromState);
        values.put("FromZipcode", t.FromZipcode);
        values.put("ToAddress", t.ToAddress);
        values.put("ToCity", t.ToCity);
        values.put("ToState", t.ToState);
        values.put("ToZipcode", t.ToZipcode);
        values.put("Make", t.Make);
        values.put("Mack", t.Mack);
        values.put("Model", t.Model);
        values.put("Year", t.Year);
        values.put("LastWorked", t.LastWorked);
        values.put("Country", t.Country);
        values.put("StateRegion", t.StateRegion);
        values.put("City", t.City);
        values.put("Company", t.Company);
        values.put("FirstName", t.FirstName);
        values.put("LastName", t.LastName);
        values.put("MobilePhone", t.MobilePhone);
        values.put("Latitude", t.Latitude);
        values.put("Longitude", t.Longitude);
        values.put("Active", t.Active);
        values.put("Speed", t.Speed);
        values.put("Temperature1", t.Temperature1);
        values.put("Temperature2", t.Temperature2);
        values.put("DoorStatus", t.DoorStatus);
        values.put("Heading", t.Heading);
        values.put("Odometer", t.Odometer);
        values.put("EngineIdle", t.EngineIdle);
        values.put("RPM", t.RPM);
        values.put("EngineHours", t.EngineHours);
        values.put("OdometerSetup", t.OdometerSetup);
        values.put("Shock", t.Shock);
        values.put("TirePressures", t.TirePressures);
        values.put("EngineStatus", t.EngineStatus);
        values.put("HOSViolation", t.HOSViolation);
        values.put("SpeedViolations", t.SpeedViolations);
        values.put("SmogViolation", t.SmogViolation);
        values.put("LastMaintenanceDate", t.LastMaintenanceDate);
        values.put("LastAnnualInspectionDate", t.LastAnnualInspectionDate);
        values.put("Status", t.Status);
        values.put("CustomerName", t.CustomerName);
        values.put("CustomerNumber", t.CustomerNumber);
        values.put("FuelCode", t.FuelCode);
        values.put("FuelType", t.FuelType);
        values.put("Diesel", t.Diesel);
        values.put("DOTNumber", t.DOTNumber);
        values.put("DOTExpirationDate", t.DOTExpirationDate);
        values.put("IFTADecal", t.IFTADecal);
        values.put("IFTAFuelPermits", t.IFTAFuelPermits);
        values.put("IFTATripPermits", t.IFTATripPermits);

        db.insert("trucks", values);
    }

    private void insertTrailersEntry(Trailer t) {
        ContentValues values = new ContentValues();

        values.put("recordId", t.RecordId);
        values.put("mobileRecordId", t.MobileRecordId);
        values.put("vin", t.VIN);
        values.put("trailerNumber", t.TrailerNumber);
        values.put("truckNumber", t.TruckNumber);

        // July 06 2022
        values.put("make", t.Make);
        values.put("model", t.Model);
        values.put("year", t.Year);
        values.put("lastWorked", t.LastWorked);
        values.put("country", t.Country);
        values.put("stateRegion", t.StateRegion);
        values.put("city", t.City);
        values.put("company", t.Company);
        values.put("processorId", t.ProcessorId);
        values.put("processorInstalled", t.ProcessorInstalled);
        values.put("latitude", t.Latitude);
        values.put("longitude", t.Longitude);
        values.put("active", t.Active);
        values.put("speed", t.Speed);
        values.put("temperature1", t.Temperature1);
        values.put("temperature2", t.Temperature2);
        values.put("humidity", t.Humidity);
        values.put("doorStatus", t.DoorStatus);
        values.put("heading", t.Heading);
        values.put("miles", t.Miles);
        values.put("fuelRate", t.FuelRate);
        values.put("shock", t.Shock);
        values.put("tirePressures", t.TirePressures);
        values.put("itemType", t.ItemType);
        values.put("treads", t.Treads);
        values.put("lastMaintenanceDate", t.LastMaintenanceDate);
        values.put("lastAnnualInspectionDate", t.LastAnnualInspectionDate);
        values.put("status", t.Status);
        values.put("customerName", t.CustomerName);
        values.put("customerNumber", t.CustomerNumber);
        values.put("isReefer", t.IsReefer);
        values.put("reeferHOS", t.ReeferHOS);
        values.put("reportingPeriod", t.ReportingPeriod);
        values.put("hOSViolation", t.HOSViolation);
        values.put("hasCamera", t.HasCamera);
        values.put("hasTracker", t.HasTracker);
        values.put("recordReeferHOS", t.RecordReeferHOS);

//        Log.d(TAG, "syncTrailers: insertTrailersEntry: db.insert: t.TrailerNumber: " + t.TrailerNumber);
        db.insert("trailers", values);
    }

    public int syncCommonRecords(int progressCounter, boolean isDownSyncEnabled,
                                 boolean isUpsyncEnabled) throws Exception {
        long LstartMillis = System.currentTimeMillis();
        String strThis = "syncCommonRecords(), ";
//        Log.d(TAG, strThis + "Start. isDownSyncEnabled=" + isDownSyncEnabled + ", isUpsyncEnabled=" + isUpsyncEnabled);

        checkInitRmsCodingDataModel();  // Todo: This may no longer belong here.  Possibly move to login sequence.. Okay for now, uses one-time flag internally.

        if (isUpsyncEnabled) {
            upSyncRmsCodingData();
            upsyncTableRecData();
        }

        if (isDownSyncEnabled) {
            downSyncRmsCodingData();
            downSyncRmsTableRecData();
        }

        RecordCommonHelper.isStartupSyncDone = true;

        progressCounter += 15;

//        Log.d(TAG, strThis + "End. isStartupSyncDone=" + RecordCommonHelper.isStartupSyncDone
//                + ", elapsed millis: " + (System.currentTimeMillis() - LstartMillis));

        return progressCounter;
    }

    public void downSyncRmsCodingData() throws Exception {
        String strThis = "downSyncRmsCodingData()";
        long Lstart = System.currentTimeMillis();
//        Log.d(TAG, strThis + "Start.");

        BusHelperRmsCoding helperRmsCoding = new BusHelperRmsCoding(db);
        helperRmsCoding.downSyncRmsCodingData(
                RMS_CODINGDATA_RECORD_TYPES, Crms.SETTING_MAX_RMS_TIMESTAMP,
                Crms.OWNER_RECORDID, user.getRecordId(), db.isNewVersion());

//        Log.d(TAG, strThis + "End. Elapsed millis: " + (System.currentTimeMillis() - Lstart));
    }

    private long LmillisLastDvirUpsync = 0;
    private long LmillisLastFuelReceiptUpsync = 0;
    private Object lockUpSyncRmsCodingData = new Object();

    public void upSyncRmsCodingData() {
        String strThis = "upSyncRmsCodingData()";
        long Lstart;
        synchronized (lockUpSyncRmsCodingData) {
            Lstart = System.currentTimeMillis();
//            Log.d(TAG, strThis + "Start.");

//
//            if (System.currentTimeMillis() - LmillisLastDvirUpsync > 5 * 60 * 1000) {
//                BusHelperDvir helperDvir = new BusHelperDvir();
//                helperDvir.upsyncDvirGroup(20, 4);
//                LmillisLastDvirUpsync = System.currentTimeMillis();
//            } else {
//                Log.d(TAG, strThis + "**** Skipping upsync of DVIR group, not enough time elapsed.");
//            }

            if (System.currentTimeMillis() - LmillisLastFuelReceiptUpsync > 5 * 60 * 1000) {
                BusHelperFuelReceipts helperFuelReceipts = new BusHelperFuelReceipts(db);
                helperFuelReceipts.upsyncFuelReceiptGroup(20, 4);
                LmillisLastFuelReceiptUpsync = System.currentTimeMillis();
            } else {
                Log.d(TAG, strThis + "**** Skipping upsync of Fuel Receipts group, not enough time elapsed.");
            }
        }

//        Log.d(TAG, strThis + "End. Elapsed millis: " + (System.currentTimeMillis() - Lstart));
    }

    public void downSyncRmsTableRecData() throws Exception {
        String strThis = "downSyncRmsTableRecData()";
        long Lstart = System.currentTimeMillis();
//        Log.d(TAG, strThis + "Start.");

        Map<String, RecRepairWorkRecTable.RecWorkCombo> mapRecWorkComboByRecordType = new HashMap<>();
        BusHelperIfta busHelperIfta = new BusHelperIfta(db);
        RecRepairWorkRecTable.RecWorkCombo recWorkCombo = busHelperIfta.getRecWorkCombo();
        mapRecWorkComboByRecordType.put(recWorkCombo.getRecWork().getRecordType(), recWorkCombo);

        String filterFields = null;
        String filterValues = null;

        // Ifta Event sync special cases - to avoid overwriting the "active" Ifta Event record due
        // to collision between the "checkForIftaEvent" thread and occasional "sync now" threads,
        // after the initial startup sync is done, we only down-sync "final" Ifta Event records
        // which shouldn't change locally or on the server.  The issue is that a change to the
        // active Ifta Event could be up-synced, then another local change done before the downsync,
        // and the down-sync would overwrite the most recent update.  There are other ways to address
        // this collision with locks and rules to make it more general, and they may be applied later.
        // However, a prime objective is to never interfere with the "checkForIftaEvent" processing
        // which should be done in a timely way, such as every 30 seconds.  Locks could possibly cause
        // delays.

        if (RecordCommonHelper.isStartupSyncDone) {
//            filterFields = Crms.EMPLOYEE_ID + "," + Crms.STATUS;
//            filterValues = user.getEmployeeId() + "," + BusHelperIfta.IFTA_EVENT_STATUS_FINAL;
            // Skip subsequent downsyncs altogether for Ifta Events to prevent collision with Active Ifta Event.
        } else {
            filterFields = Crms.EMPLOYEE_ID;
            filterValues = user.getEmployeeId();
            RecordCommonHelperTableRec.downSyncTableRecData(db, this, mapRecWorkComboByRecordType, Crms.SETTING_MAX_IFTA_EVENT_TIMESTAMP,
                    filterFields, filterValues, db.isNewVersion());
        }

//        Log.d(TAG, strThis + "End. Elapsed millis: " + (System.currentTimeMillis() - Lstart));
    }

    public void upsyncTableRecData() {
        String strThis = " upsyncTableRecData()";
        long Lstart = System.currentTimeMillis();
//        Log.d(TAG, strThis + "Start.");

        BusHelperIfta helperIfta = new BusHelperIfta(db);
        try {
            helperIfta.upsyncIftaEvents(20);
        } catch (Exception e) {
            e.printStackTrace();
        }

//        Log.d(TAG, strThis + "End. Elapsed millis: " + (System.currentTimeMillis() - Lstart));
    }

    //endregion

    //region Rules

    private void loadRules() {
        ArrayList<Rule> result = new ArrayList();
        Cursor c = db.getQuery("SELECT * FROM rules");
        c.moveToFirst();
        int counter = 0;

        while (!c.isAfterLast()) {
            Rule r = new Rule();

            counter = 1;
            r.objectId = c.getString(counter++);
            r.objectType = c.getString(counter++);
            r.RecordId = c.getString(counter++);
            r.Name = c.getString(counter++);
            r.Hours = c.getString(counter++);
            r.Days = c.getString(counter++);
            r.HoursPerDay = c.getString(counter++);
            r.ItemType = c.getString(counter++);

            result.add(r);
            c.moveToNext();
        }

        c.close();
        rules = result;
    }

    public void syncRules() throws Exception {
//        May 26, 2022  -   The sync is done just at the first install
        if (db.exists("SELECT * FROM rules")) {
            loadRules();
            return;
        }

        rules = new ArrayList<>();
        ArrayList<Rule> result = new ArrayList();

        String response = Rms.getRecordsUpdatedXFiltered("Truck+Rule", -5000,
                "Organization+Name", ",", Rms.getOrgName().replace(" ", "+"), ",", "");

        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            clearTableEntries("rules");

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);

                Rule a = new Rule();
                a.objectId = codingFields.getValue("LobjectId");
                a.objectType = codingFields.getValue("objectType");
                a.RecordId = codingFields.getValue("RecordId");
                a.Name = codingFields.getValue("Name");
                a.Hours = codingFields.getValue("Hours");
                a.Days = codingFields.getValue("Days");
                a.HoursPerDay = codingFields.getValue("Hours Per Day");
                a.ItemType = codingFields.getValue("ItemType");

                result.add(a);
                insertRulesEntry(a);
            }

            rules.addAll(result);
        }
    }

    public ArrayList<String> getStateRuleNames() {
        ArrayList<Rule> rules = getStateRules();
        ArrayList<String> ruleNames = new ArrayList<>();

        if (rules == null)
            return null;

        for (Rule r : rules)
            ruleNames.add(r.Name);

        return ruleNames;
    }

    public ArrayList<Rule> getStateRules() {
        try {
            ArrayList<Rule> result = new ArrayList();
            Cursor c = db.getQuery("SELECT * FROM rules");

            if (c == null)
                return null;

            c.moveToFirst();

            while (!c.isAfterLast()) {
                Rule r = new Rule();

                r.Id = c.getInt(0);
                r.objectId = c.getString(1);
                r.objectType = c.getString(2);
                r.RecordId = c.getString(3);
                r.Name = c.getString(4);
                r.Hours = c.getString(5);
                r.Days = c.getString(6);
                r.HoursPerDay = c.getString(7);
                r.ItemType = c.getString(8);

                result.add(r);
                c.moveToNext();
            }

            c.close();
            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    private void insertRulesEntry(Rule t) {
        ContentValues values = new ContentValues();

        values.put("objectId", t.objectId);
        values.put("objectType", t.objectType);
        values.put("recordId", t.RecordId);
        values.put("name", t.Name);
        values.put("hours", t.Hours);
        values.put("days", t.Days);
        values.put("hoursperday", t.HoursPerDay);
        values.put("itemType", t.ItemType);

        db.insert("rules", values);
    }

    public Rule getActiveRule() {
        Integer activeRuleIndex = getActiveRuleIndex();

        if (activeRuleIndex == null)
            return null;

        if (rules == null || rules.size() <= activeRuleIndex)
            return null;

        return rules.get(activeRuleIndex);
    }

    public Integer getActiveRuleIndex(String[] ruleList) {
        String activeRuleName = getActiveRuleName();
        Log.d(TAG, "DriveFragmentBase: getActiveRuleIndex: activeRuleName: " + activeRuleName);

//        ArrayList<String> stateRuleNames = getStateRuleNames();
        String[] stateRuleNames = ruleList;

        for (int i = 0; i < stateRuleNames.length; i++) {
            String stateRuleName = stateRuleNames[i];

            if (stateRuleName.equalsIgnoreCase(activeRuleName))
                return i;
        }

        return null;
    }

    public Integer getActiveTruckNumberIndex(String[] truckNumberList, String activeTruckNumber) {
        Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: getActiveTruckNumberIndex: activeTruckNumber: " + activeTruckNumber);
        for (int i = 0; i < truckNumberList.length; i++) {
            Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: getActiveTruckNumberIndex: index: " + i + " value: " + truckNumberList[i]);
            String truckNumber = truckNumberList[i];
            if (truckNumber.equalsIgnoreCase(activeTruckNumber))
                return i;
        }
        return null;
    }

    public Integer getActiveRuleIndex() {
        String activeRuleName = getActiveRuleName();

//        ArrayList<String> stateRuleNames = getStateRuleNames();
        ArrayList<String> stateRuleNames = getStateRuleNames();

        for (int i = 0; i < stateRuleNames.size(); i++) {
            String stateRuleName = stateRuleNames.get(i);

            if (stateRuleName.equalsIgnoreCase(activeRuleName))
                return i;
        }

        return null;
    }

    public String getActiveRuleName() {
        return getSetting("lastactiverule");
    }

    public void setSelectedRule(String name) {
        setSetting("lastactiverule", name);
    }

    //endregion

    //region Evaluation

    public void clearEvaluationTable() {

        clearTableEntries("evaluations");
    }

    public void syncEvaluations(String filterValue) throws Exception {

        String maxTimestamp = "0";
        String response = Rms.getEvaluationList(filterValue, "-100", maxTimestamp);

        if (response != null && response.trim().length() > 0) {
            JSONArray jsonArray = new JSONArray(response);
            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject o = jsonArray.getJSONObject(i);
                PairList codingFields = Rms.parseJsonCodingFields(o);
                Evaluation evaluation = new Evaluation();
                evaluation.LobjectId = codingFields.getValue("LobjectId");
                evaluation.objectType = codingFields.getValue("objectType");
                evaluation.csvDataFilePath = codingFields.getValue("csvDataFilePath");
                evaluation.iCsvRow = codingFields.getValue("iCsvRow");
                evaluation.mobileRecordId = codingFields.getValue("mobileRecordId");
                evaluation.Driver_Mobile_Phone_Number = codingFields.getValue("Driver_Mobile_Phone_Number");
                evaluation.Vehicle_Modifications = codingFields.getValue("Vehicle Modifications");
                evaluation.Instructor_Last_Name = codingFields.getValue("Instructor Last Name");
                evaluation.Creation_Time = codingFields.getValue("Creation Time");
                evaluation.Vehicle_Model = codingFields.getValue("Vehicle Model");
                evaluation.Organization_Name = codingFields.getValue("Organization Name");

                evaluation.States_of_operation = codingFields.getValue("States of operation");
                evaluation.Type_of_operation = codingFields.getValue("Type of operation");
                evaluation.Driver_License_Number = codingFields.getValue("Driver License Number");
                evaluation.Driver_Address = codingFields.getValue("Driver Address");
                evaluation.Driver_City = codingFields.getValue("Driver City");
                evaluation.Description_of_trailers = codingFields.getValue("Description of trailers");
                evaluation.Creation_Date = codingFields.getValue("Creation Date");
                evaluation.Vehicle_Transmission_Type = codingFields.getValue("Vehicle Transmission Type");
                evaluation.Vehicle_Type_Steering_System = codingFields.getValue("Vehicle Type Steering System");
                evaluation.Application_Type_Unilateral = codingFields.getValue("Application Type Unilateral");
                evaluation.Sex = codingFields.getValue("Sex");
                evaluation.RMS_Efile_Timestamp = codingFields.getValue("RMS Efile Timestamp");
                evaluation.RecordId = codingFields.getValue("RecordId");
                evaluation.Organization_Number = codingFields.getValue("Organization Number");
                evaluation.Date_of_Birth = codingFields.getValue("Date of Birth");
                evaluation.Driver_Maiden_Name = codingFields.getValue("Driver Maiden Name");
                evaluation.Vehicle_Year = codingFields.getValue("Vehicle Year");
                evaluation.RMS_Coding_Timestamp = codingFields.getValue("RMS Coding Timestamp");
                evaluation.Driver_State = codingFields.getValue("Driver State");
                evaluation.Driver_Home_Phone_Number = codingFields.getValue("Driver Home Phone Number");
                evaluation.Average_period_of_driving_time = codingFields.getValue("Average period of driving time");
                evaluation.ObjectName = codingFields.getValue("ObjectName");
                evaluation.Form_Date = codingFields.getValue("Form Date");
                evaluation.Driver_Last_Name = codingFields.getValue("Driver Last Name");
                evaluation.BarCode = codingFields.getValue("BarCode");
                evaluation.Vehicle_Type_Brake_System = codingFields.getValue("Vehicle Type Brake System");
                evaluation.Driver_Zipcode = codingFields.getValue("Driver Zipcode");
                evaluation.Vehicle_Type = codingFields.getValue("Vehicle Type");
                evaluation.Vehicle_Number_of_Forward_Speeds = codingFields.getValue("Vehicle Number of Forward Speeds");
                evaluation.Application_Type_Joint = codingFields.getValue("Application Type Joint");
                evaluation.Type_of_cargo = codingFields.getValue("Type of cargo");
                evaluation.Instructor_Driver_License_Number = codingFields.getValue("Instructor Driver License Number");
                evaluation.Vehicle_Seating_Capacity = codingFields.getValue("Vehicle Seating Capacity");
                evaluation.Vehicle_Rear_Axle_Speed = codingFields.getValue("Vehicle Rear Axle Speed");
                evaluation.Driver_License_State = codingFields.getValue("Driver License State");
                evaluation.Vehicle_Make = codingFields.getValue("Vehicle Make");
                evaluation.Driver_First_Name = codingFields.getValue("Driver First Name");
                evaluation.Location = codingFields.getValue("Location");
                evaluation.BarCode = codingFields.getValue("BarCode");
                evaluation.Type_of_prosthesis_worn = codingFields.getValue("Type of prosthesis worn");
                evaluation.Instructor_First_Name = codingFields.getValue("Instructor First Name");
                evaluation.Driver_Signature_Date = codingFields.getValue("Driver Signature Date");
                evaluation.Description_of_impairment_or_amputation = codingFields.getValue("Description of impairment or amputation");
                evaluation.Number_of_years_driving_vehicle_type = codingFields.getValue("Number of years driving vehicle type");
                evaluation.Number_of_Trailers = codingFields.getValue("Number_of_Trailers");
                evaluation.Number_of_years_driving_all_vehicle_types = codingFields.getValue("Number of years driving all vehicle types");
                evaluation.RMS_Timestamp = codingFields.getValue("RMS Timestamp");
                evaluation.ItemType = filterValue;

                evaluation.lastEmployerName = codingFields.getValue("Last Employer Name");
                evaluation.lastEmployerAddress = codingFields.getValue("Last Employer Address");
                evaluation.lastEmployerCity = codingFields.getValue("Last Employer City");
                evaluation.lastEmployerState = codingFields.getValue("Last Employer State");
                evaluation.lastEmployerZipcode = codingFields.getValue("Last Employer Zipcode");
                evaluation.lastEmployerTelephoneNumber = codingFields.getValue("Last Employer Telephone Number");
                evaluation.lastEmployerReasonsforleaving = codingFields.getValue("Last Employer Reasons for leaving");
                evaluation.secondLastEmployerName = codingFields.getValue("Second Last Employer Name");
                evaluation.secondLastEmployerAddress = codingFields.getValue("Second Last Employer Address");
                evaluation.secondLastEmployerCity = codingFields.getValue("Second Last Employer City");
                evaluation.secondLastEmployerState = codingFields.getValue("Second Last Employer State");
                evaluation.secondLastEmployerZipcode = codingFields.getValue("Second Last Employer Zipcode");
                evaluation.secondLastEmployerTelephoneNumber = codingFields.getValue("Second Last Employer Telephone Number");
                evaluation.secondLastEmployerReasonsforleaving = codingFields.getValue("Second Last Employer Reasons for leaving");
                evaluation.thirdLastEmployerName = codingFields.getValue("Third Last Employer Name");
                evaluation.thirdLastEmployerAddress = codingFields.getValue("Third Last Employer Address");
                evaluation.thirdLastEmployerCity = codingFields.getValue("Third Last Employer City");
                evaluation.thirdLastEmployerState = codingFields.getValue("Third Last Employer State");
                evaluation.thirdLastEmployerZipcode = codingFields.getValue("Third Last Employer Zipcode");
                evaluation.thirdLastEmployerTelephoneNumber = codingFields.getValue("Third Last Employer Telephone Number");
                evaluation.thirdLastEmployerReasonsforleaving = codingFields.getValue("Third Last Employer Reasons for leaving");
                evaluation.positionHeld1 = codingFields.getValue("Position Held 1");
                evaluation.positionHeld2 = codingFields.getValue("Position Held 2");
                evaluation.positionHeld3 = codingFields.getValue("Position Held 2");
                evaluation.lastEmployerFromDate = codingFields.getValue("Last Employer From Date");
                evaluation.lastEmployerToDate = codingFields.getValue("Last Employer To Date");
                evaluation.secondLastEmployerFromDate = codingFields.getValue("Second Last Employer From Date");
                evaluation.secondLastEmployerToDate = codingFields.getValue("Second Last Employer To Date");
                evaluation.thirdLastEmployerFromDate = codingFields.getValue("Third Last Employer From Date");
                evaluation.thirdLastEmployerToDate = codingFields.getValue("Third Last Employer To Date");

                evaluation.carrierName = codingFields.getValue("Carrier Name");
                evaluation.doctorsName = codingFields.getValue("Doctors Name");
                evaluation.sPEApplicantName = codingFields.getValue("SPE Applicant Name");
                evaluation.vehicleTypeStraightTruck = codingFields.getValue("Vehicle Type Straight Truck");
                evaluation.vehicleTypeTruckTrailerover10klbs = codingFields.getValue("Vehicle Type Truck Trailer over 10k lbs");
                evaluation.vehicleTypeTrucklessthan10klbsandhazardousmaterials = codingFields.getValue("vehicle Type Truck less than 10k lbs and hazardous materials");
                evaluation.vehicleTypeTruckover10klbs = codingFields.getValue("Vehicle Type Truck over 10k lbs");
                evaluation.vehicleTypeMotorHome10klbs = codingFields.getValue("Vehicle Type Motor Home 10k lbs");
                evaluation.vehicleTypeTractorTrailer = codingFields.getValue("Vehicle Type Tractor Trailer");
                evaluation.vehicleTypePassengerVehicle = codingFields.getValue("Vehicle Type Passenger Vehicle");
                evaluation.vehicleTypePassengerSeatingCapacity = codingFields.getValue("Vehicle Type Passenger Seating Capacity");
                evaluation.vehicleTypePassengerMotorCoach = codingFields.getValue("Vehicle Type Passenger Motor Coach");
                evaluation.vehicleTypePassengerBus = codingFields.getValue("Vehicle Type Passenger Bus");
                evaluation.vehicleTypePassengerVan = codingFields.getValue("Vehicle Type Passenger Van");
                evaluation.vehicleTypeshortrelaydrives = codingFields.getValue("Vehicle Type short relay drives");
                evaluation.vehicleTypelongrelaydrives = codingFields.getValue("Vehicle Type long relay drives");
                evaluation.vehicleTypestraightthrough = codingFields.getValue("Vehicle Type straight through");
                evaluation.vehicleTypenightsawayfromhome = codingFields.getValue("Vehicle Type nights away from home");
                evaluation.vehicleTypesleeperteamdrives = codingFields.getValue("Vehicle Type sleeper team drives");
                evaluation.vehicleTypenumberofnightsawayfromhome = codingFields.getValue("Vehicle Type number of nights away from home");
                evaluation.vehicleTypeclimbinginandoutoftruck = codingFields.getValue("Vehicle Type climbing in and out of truck");
                evaluation.environmentalFactorsabruptduty = codingFields.getValue("Environmental Factors abrupt duty");
                evaluation.environmentalFactorssleepdeprivation = codingFields.getValue("Environmental Factors sleep deprivation");
                evaluation.environmentalFactorsunbalancedwork = codingFields.getValue("Environmental Factors unbalanced work");
                evaluation.environmentalFactorstemperature = codingFields.getValue("Environmental Factors temperaturee");
                evaluation.environmentalFactorslongtrips = codingFields.getValue("Environmental Factors long trips");
                evaluation.environmentalFactorsshortnotice = codingFields.getValue("Environmental Factors short notice");
                evaluation.environmentalFactorstightdelivery = codingFields.getValue("Environmental Factors tight delivery");
                evaluation.environmentalFactorsdelayenroute = codingFields.getValue("Environmental Factors delay en route");
                evaluation.environmentalFactorsothers = codingFields.getValue("Environmental Factors others");
                evaluation.physicalDemandGearShifting = codingFields.getValue("Physical Demand Gear Shifting");
                evaluation.physicalDemandNumberspeedtransmission = codingFields.getValue("Physical Demand Number speed transmission");
                evaluation.physicalDemandsemiautomatic = codingFields.getValue("Physical Demand semi automatic");
                evaluation.physicalDemandfullyautomatic = codingFields.getValue("Physical Demand fully automatic");
                evaluation.physicalDemandsteeringwheelcontrol = codingFields.getValue("Physical Demand steering wheel control");
                evaluation.physicalDemandbrakeacceleratoroperation = codingFields.getValue("Physical Demand brake accelerator operation");
                evaluation.physicalDemandvarioustasks = codingFields.getValue("Physical Demand various tasks");
                evaluation.physicalDemandbackingandparking = codingFields.getValue("Physical Demand backing and parking");
                evaluation.physicalDemandvehicleinspections = codingFields.getValue("Physical Demand vehicle inspections");
                evaluation.physicalDemandcargohandling = codingFields.getValue("Physical Demand cargo handling");
                evaluation.physicalDemandcoupling = codingFields.getValue("Physical Demand coupling");
                evaluation.physicalDemandchangingtires = codingFields.getValue("Physical Demand changing tires");
                evaluation.physicalDemandvehiclemodifications = codingFields.getValue("Physical Demand vehicle modifications");
                evaluation.physicalDemandvehiclemodnotes = codingFields.getValue("Physical Demand vehicle mod notes");
                evaluation.muscleStrengthyesno = codingFields.getValue("Muscle Strength yes-no");
                evaluation.muscleStrengthrightupperextremity = codingFields.getValue("Muscle Strength right upper extremity");
                evaluation.muscleStrengthleftupperextremity = codingFields.getValue("Muscle Strength left upper extremity");
                evaluation.muscleStrengthrightlowerextremity = codingFields.getValue("Muscle Strength right lower extremity");
                evaluation.muscleStrengthleftlowerextremity = codingFields.getValue("Muscle Strength left lower extremity");
                evaluation.mobilityyesno = codingFields.getValue("Mobility yes-no");
                evaluation.mobilityrightupperextremity = codingFields.getValue("Mobility right upper extremity");
                evaluation.mobilityleftupperextremity = codingFields.getValue("Mobility left upper extremity");
                evaluation.mobilityrightlowerextremity = codingFields.getValue("Mobility right lower extremity");
                evaluation.mobilityleftlowerextremity = codingFields.getValue("Mobility left lower extremity");
                evaluation.mobilitytrunk = codingFields.getValue("Mobility trunk");
                evaluation.stabilityyesno = codingFields.getValue("Stability yes-no");
                evaluation.stabilityrightupperextremity = codingFields.getValue("Stability right upper extremity");
                evaluation.stabilityleftupperextremity = codingFields.getValue("Stability left upper extremity");
                evaluation.stabilityrightlowerextremity = codingFields.getValue("Stability right lower extremity");
                evaluation.stabilityleftlowerextremity = codingFields.getValue("Stability left lower extremity");
                evaluation.stabilitytrunk = codingFields.getValue("Stability trunk");
                evaluation.impairmenthand = codingFields.getValue("Impairment hand");
                evaluation.impairmentupperlimb = codingFields.getValue("Impairment upper limb");
                evaluation.amputationhand = codingFields.getValue("Amputation hand");
                evaluation.amputationpartial = codingFields.getValue("Amputation partial");
                evaluation.amputationfull = codingFields.getValue("Amputation full");
                evaluation.amputationupperlimb = codingFields.getValue("Amputation upper limb");
                evaluation.powergriprightyesno = codingFields.getValue("Power grip right yes-no");
                evaluation.powergripleftyesno = codingFields.getValue("Power grip left yes-no");
                evaluation.surgicalreconstructionyesno = codingFields.getValue("Surgical reconstruction yes-no");
                evaluation.hasupperimpairment = codingFields.getValue("Has upper impairment");
                evaluation.haslowerlimbimpairment = codingFields.getValue("Has lower limb impairment");
                evaluation.hasrightimpairment = codingFields.getValue("Has right impairment");
                evaluation.hasleftimpairment = codingFields.getValue("Has left impairment");
                evaluation.hasupperamputation = codingFields.getValue("Has upper amputation");
                evaluation.haslowerlimbamputation = codingFields.getValue("Has lower limb amputation");
                evaluation.hasrightamputation = codingFields.getValue("Has right amputation");
                evaluation.hasleftamputation = codingFields.getValue("Has left amputation");
                evaluation.appropriateprosthesisyesno = codingFields.getValue("Appropriate prosthesis yes-no");
                evaluation.appropriateterminaldeviceyesno = codingFields.getValue("Appropriate terminal device yes-no");
                evaluation.prosthesisfitsyesno = codingFields.getValue("Prosthesis fits yes-no");
                evaluation.useprostheticproficientlyyesno = codingFields.getValue("Use prosthetic proficiently yes-no");
                evaluation.abilitytopowergraspyesno = codingFields.getValue("Ability to power grasp yes-no");
                evaluation.prostheticrecommendations = codingFields.getValue("Prosthetic recommendations");
                evaluation.prostheticclinicaldescription = codingFields.getValue("Prosthetic clinical description");
                evaluation.medicalconditionsinterferewithtasksyesno = codingFields.getValue("Medical conditions interfere with tasks yes-no");
                evaluation.medicalconditionsinterferewithtasksexplanation = codingFields.getValue("Medical conditions interfere with tasks explanation");
                evaluation.medicalfindingsandevaluation = codingFields.getValue("Medical findings and evaluation");
                evaluation.physicianlastname = codingFields.getValue("Physician last name");
                evaluation.physicianfirstname = codingFields.getValue("Physician first name");
                evaluation.physicianmiddlename = codingFields.getValue("Physician middle name");
                evaluation.physicianaddress = codingFields.getValue("Physician address");
                evaluation.physiciancity = codingFields.getValue("Physician city");
                evaluation.physicianstate = codingFields.getValue("Physician state");
                evaluation.physicianzipcode = codingFields.getValue("Physician zipcode");
                evaluation.physiciantelephonenumber = codingFields.getValue("Physician telephone number");
                evaluation.physicianalternatenumber = codingFields.getValue("Physician alternate number");
                evaluation.physiatrist = codingFields.getValue("Physiatrist");
                evaluation.orthopedicsurgeon = codingFields.getValue("Orthopedic surgeon");
                evaluation.boardCertifiedyesno = codingFields.getValue("Board Certified yes-no");
                evaluation.boardEligibleyesno = codingFields.getValue("Board Eligible yes-no");
                evaluation.physiciandate = codingFields.getValue("Physician date");
                evaluation.vehicleTypeslocaldeliveries = codingFields.getValue("Vehicle Types local deliveries");
                evaluation.physicalDemandmountingsnowchains = codingFields.getValue("Physical Demand mounting snow chains");

                evaluation.companyName = codingFields.getValue("Company");
                evaluation.companyAddress = codingFields.getValue("Company Address");
                evaluation.companyCity = codingFields.getValue("Company City");
                evaluation.companyState = codingFields.getValue("Company State");
                evaluation.companyZipcode = codingFields.getValue("Company Zipcode");
                evaluation.firstName = codingFields.getValue("Driver First Name");
                evaluation.lastName = codingFields.getValue("Driver Lirst Name");


                evaluation.employmentDate = codingFields.getValue("Employment Date");
                evaluation.terminalCity = codingFields.getValue("Terminal City");
                evaluation.terminalState = codingFields.getValue("Terminal State");
                evaluation.driverLicenseExpirationDate = codingFields.getValue("Driver License Expiration Date");
                evaluation.violationsthisyear = codingFields.getValue("Violations this year");
                evaluation.carrierAddress = codingFields.getValue("Carrier Address");
                evaluation.reviewedBy = codingFields.getValue("Reviewed By");
                evaluation.reviewedDate = codingFields.getValue("Reviewed Date");
                evaluation.title = codingFields.getValue("Title");

                evaluation.violationOffense = codingFields.getValue("Violation Offense");
                evaluation.violationDate = codingFields.getValue("DateTime");
                evaluation.violationLocation = codingFields.getValue("Violation Location");
                evaluation.violationtypeofvehicle = codingFields.getValue("violation type of vehicle");

                insertEvaluation(evaluation);
            }
        }
    }

    public void insertEvaluation(Evaluation ev) {
        ContentValues values = new ContentValues();

        values.put("LobjectId", ev.LobjectId);
        values.put("objectType", ev.objectType);
        values.put("csvDataFilePath", ev.csvDataFilePath);
        values.put("iCsvRow", ev.iCsvRow);
        values.put("mobileRecordId", ev.mobileRecordId);
        values.put("Driver_Mobile_Phone_Number", ev.Driver_Mobile_Phone_Number);
        values.put("Vehicle_Modifications", ev.Vehicle_Modifications);
        values.put("Instructor_Last_Name", ev.Instructor_Last_Name);
        values.put("Creation_Time", ev.Creation_Time);
        values.put("Vehicle_Model", ev.Vehicle_Model);
        values.put("Organization_Name", ev.Organization_Name);
        values.put("States_of_operation", ev.States_of_operation);
        values.put("Type_of_operation", ev.Type_of_operation);
        values.put("Driver_License_Number", ev.Driver_License_Number);
        values.put("Driver_Address", ev.Driver_Address);
        values.put("Driver_City", ev.Driver_City);
        values.put("Description_of_trailers", ev.Description_of_trailers);
        values.put("Creation_Date", ev.Creation_Date);
        values.put("Vehicle_Transmission_Type", ev.Vehicle_Transmission_Type);
        values.put("Vehicle_Type_Steering_System", ev.Vehicle_Type_Steering_System);
        values.put("Application_Type_Unilateral", ev.Application_Type_Unilateral);
        values.put("Sex", ev.Sex);
        values.put("RMS_Efile_Timestamp", ev.RMS_Efile_Timestamp);
        values.put("RecordId", ev.RecordId);
        values.put("Organization_Number", ev.Organization_Number);
        values.put("Date_of_Birth", ev.Date_of_Birth);
        values.put("Driver_Maiden_Name", ev.Driver_Maiden_Name);
        values.put("Vehicle_Year", ev.Vehicle_Year);
        values.put("RMS_Coding_Timestamp", ev.RMS_Coding_Timestamp);
        values.put("Driver_State", ev.Driver_State);
        values.put("Driver_Home_Phone_Number", ev.Driver_Home_Phone_Number);
        values.put("Average_period_of_driving_time", ev.Average_period_of_driving_time);
        values.put("ObjectName", ev.ObjectName);
        values.put("Form_Date", ev.Form_Date);
        values.put("Driver_Last_Name", ev.Driver_Last_Name);
        values.put("BarCode", ev.BarCode);
        values.put("Vehicle_Type_Brake_System", ev.Vehicle_Type_Brake_System);
        values.put("Driver_Zipcode", ev.Driver_Zipcode);
        values.put("Vehicle_Type", ev.Vehicle_Type);
        values.put("Vehicle_Number_of_Forward_Speeds", ev.Vehicle_Number_of_Forward_Speeds);
        values.put("Application_Type_Joint", ev.Application_Type_Joint);
        values.put("Type_of_cargo", ev.Type_of_cargo);
        values.put("Instructor_Driver_License_Number", ev.Instructor_Driver_License_Number);
        values.put("Vehicle_Seating_Capacity", ev.Vehicle_Seating_Capacity);
        values.put("Vehicle_Rear_Axle_Speed", ev.Vehicle_Rear_Axle_Speed);
        values.put("Driver_License_State", ev.Driver_License_State);
        values.put("Vehicle_Make", ev.Vehicle_Make);
        values.put("Driver_First_Name", ev.Driver_First_Name);
        values.put("Location", ev.Location);
        values.put("Type_of_prosthesis_worn", ev.Type_of_prosthesis_worn);
        values.put("Instructor_First_Name", ev.Instructor_First_Name);
        values.put("Driver_Signature_Date", ev.Driver_Signature_Date);
        values.put("Description_of_impairment_or_amputation", ev.Description_of_impairment_or_amputation);
        values.put("Number_of_years_driving_vehicle_type", ev.Number_of_years_driving_vehicle_type);
        values.put("Number_of_Trailers", ev.Number_of_Trailers);
        values.put("Number_of_years_driving_all_vehicle_types", ev.Number_of_years_driving_all_vehicle_types);
        values.put("RMS_Timestamp", ev.RMS_Timestamp);
        values.put("ItemType", ev.ItemType);

        values.put("lastEmployerName", ev.lastEmployerName);
        values.put("lastEmployerAddress", ev.lastEmployerAddress);
        values.put("lastEmployerCity", ev.lastEmployerCity);
        values.put("lastEmployerState", ev.lastEmployerState);
        values.put("lastEmployerZipcode", ev.lastEmployerZipcode);
        values.put("lastEmployerTelephoneNumber", ev.lastEmployerTelephoneNumber);
        values.put("lastEmployerReasonsforleaving", ev.lastEmployerReasonsforleaving);
        values.put("secondLastEmployerName", ev.secondLastEmployerName);
        values.put("secondLastEmployerAddress", ev.secondLastEmployerAddress);
        values.put("secondLastEmployerCity", ev.secondLastEmployerCity);
        values.put("secondLastEmployerState", ev.secondLastEmployerState);
        values.put("secondLastEmployerZipcode", ev.secondLastEmployerZipcode);
        values.put("secondLastEmployerTelephoneNumber", ev.secondLastEmployerTelephoneNumber);
        values.put("secondLastEmployerReasonsforleaving", ev.secondLastEmployerReasonsforleaving);
        values.put("thirdLastEmployerName", ev.thirdLastEmployerName);
        values.put("thirdLastEmployerAddress", ev.thirdLastEmployerAddress);
        values.put("thirdLastEmployerCity", ev.thirdLastEmployerCity);
        values.put("thirdLastEmployerState", ev.thirdLastEmployerState);
        values.put("thirdLastEmployerZipcode", ev.thirdLastEmployerZipcode);
        values.put("thirdLastEmployerTelephoneNumber", ev.thirdLastEmployerTelephoneNumber);
        values.put("thirdLastEmployerReasonsforleaving", ev.thirdLastEmployerReasonsforleaving);
        values.put("positionHeld1", ev.positionHeld1);
        values.put("positionHeld2", ev.positionHeld2);
        values.put("positionHeld3", ev.positionHeld3);
        values.put("lastEmployerFromDate", ev.lastEmployerFromDate);
        values.put("lastEmployerToDate", ev.lastEmployerToDate);
        values.put("secondLastEmployerFromDate", ev.secondLastEmployerFromDate);
        values.put("secondLastEmployerToDate", ev.secondLastEmployerToDate);
        values.put("thirdLastEmployerFromDate", ev.thirdLastEmployerFromDate);
        values.put("thirdLastEmployerToDate", ev.thirdLastEmployerToDate);


        values.put("sPEApplicantName", ev.sPEApplicantName);
        values.put("vehicleTypeStraightTruck", ev.vehicleTypeStraightTruck);
        values.put("vehicleTypeTruckTrailerover10klbs", ev.vehicleTypeTruckTrailerover10klbs);
        values.put("vehicleTypeTrucklessthan10klbsandhazardousmaterials", ev.vehicleTypeTrucklessthan10klbsandhazardousmaterials);
        values.put("vehicleTypeTruckover10klbs", ev.vehicleTypeTruckover10klbs);
        values.put("vehicleTypeMotorHome10klbs", ev.vehicleTypeMotorHome10klbs);
        values.put("vehicleTypeTractorTrailer", ev.vehicleTypeTractorTrailer);
        values.put("vehicleTypePassengerVehicle", ev.vehicleTypePassengerVehicle);
        values.put("vehicleTypePassengerSeatingCapacity", ev.vehicleTypePassengerSeatingCapacity);
        values.put("vehicleTypePassengerMotorCoach", ev.vehicleTypePassengerMotorCoach);
        values.put("vehicleTypePassengerBus", ev.vehicleTypePassengerBus);
        values.put("vehicleTypePassengerVan", ev.vehicleTypePassengerVan);
        values.put("vehicleTypeshortrelaydrives", ev.vehicleTypeshortrelaydrives);
        values.put("vehicleTypelongrelaydrives", ev.vehicleTypelongrelaydrives);
        values.put("vehicleTypestraightthrough", ev.vehicleTypestraightthrough);
        values.put("vehicleTypenightsawayfromhome", ev.vehicleTypenightsawayfromhome);
        values.put("vehicleTypesleeperteamdrives", ev.vehicleTypesleeperteamdrives);
        values.put("vehicleTypenumberofnightsawayfromhome", ev.vehicleTypenumberofnightsawayfromhome);
        values.put("vehicleTypeclimbinginandoutoftruck", ev.vehicleTypeclimbinginandoutoftruck);
        values.put("environmentalFactorsabruptduty", ev.environmentalFactorsabruptduty);
        values.put("environmentalFactorssleepdeprivation", ev.environmentalFactorssleepdeprivation);
        values.put("environmentalFactorsunbalancedwork", ev.environmentalFactorsunbalancedwork);
        values.put("environmentalFactorstemperature", ev.environmentalFactorstemperature);
        values.put("environmentalFactorslongtrips", ev.environmentalFactorslongtrips);
        values.put("environmentalFactorsshortnotice", ev.environmentalFactorsshortnotice);
        values.put("environmentalFactorstightdelivery", ev.environmentalFactorstightdelivery);
        values.put("environmentalFactorsdelayenroute", ev.environmentalFactorsdelayenroute);
        values.put("environmentalFactorsothers", ev.environmentalFactorsothers);
        values.put("physicalDemandGearShifting", ev.physicalDemandGearShifting);
        values.put("physicalDemandNumberspeedtransmission", ev.physicalDemandNumberspeedtransmission);
        values.put("physicalDemandsemiautomatic", ev.physicalDemandsemiautomatic);
        values.put("physicalDemandfullyautomatic", ev.physicalDemandfullyautomatic);
        values.put("physicalDemandsteeringwheelcontrol", ev.physicalDemandsteeringwheelcontrol);
        values.put("physicalDemandbrakeacceleratoroperation", ev.physicalDemandbrakeacceleratoroperation);
        values.put("physicalDemandvarioustasks", ev.physicalDemandvarioustasks);
        values.put("physicalDemandbackingandparking", ev.physicalDemandbackingandparking);
        values.put("physicalDemandvehicleinspections", ev.physicalDemandvehicleinspections);
        values.put("physicalDemandcargohandling", ev.physicalDemandcargohandling);
        values.put("physicalDemandcoupling", ev.physicalDemandcoupling);
        values.put("physicalDemandchangingtires", ev.physicalDemandchangingtires);
        values.put("physicalDemandvehiclemodifications", ev.physicalDemandvehiclemodifications);
        values.put("physicalDemandvehiclemodnotes", ev.physicalDemandvehiclemodnotes);
        values.put("muscleStrengthyesno", ev.muscleStrengthyesno);
        values.put("muscleStrengthrightupperextremity", ev.muscleStrengthrightupperextremity);
        values.put("muscleStrengthleftupperextremity", ev.muscleStrengthleftupperextremity);
        values.put("muscleStrengthrightlowerextremity", ev.muscleStrengthrightlowerextremity);
        values.put("muscleStrengthleftlowerextremity", ev.muscleStrengthleftlowerextremity);
        values.put("mobilityyesno", ev.mobilityyesno);
        values.put("mobilityrightupperextremity", ev.mobilityrightupperextremity);
        values.put("mobilityleftupperextremity", ev.mobilityleftupperextremity);
        values.put("mobilityrightlowerextremity", ev.mobilityrightlowerextremity);
        values.put("mobilityleftlowerextremity", ev.mobilityleftlowerextremity);
        values.put("stabilityyesno", ev.stabilityyesno);
        values.put("stabilityrightupperextremity", ev.stabilityrightupperextremity);
        values.put("stabilityleftupperextremity", ev.stabilityleftupperextremity);
        values.put("stabilityrightlowerextremity", ev.stabilityrightlowerextremity);
        values.put("stabilityleftlowerextremity", ev.stabilityleftlowerextremity);
        values.put("stabilitytrunk", ev.stabilitytrunk);
        values.put("impairmenthand", ev.impairmenthand);
        values.put("impairmentupperlimb", ev.impairmentupperlimb);
        values.put("amputationhand", ev.amputationhand);
        values.put("amputationpartial", ev.amputationpartial);
        values.put("amputationfull", ev.amputationfull);
        values.put("amputationupperlimb", ev.amputationupperlimb);
        values.put("powergriprightyesno", ev.powergriprightyesno);
        values.put("powergripleftyesno", ev.powergripleftyesno);
        values.put("surgicalreconstructionyesno", ev.surgicalreconstructionyesno);
        values.put("hasupperimpairment", ev.hasupperimpairment);
        values.put("haslowerlimbimpairment", ev.haslowerlimbimpairment);
        values.put("hasrightimpairment", ev.hasrightimpairment);
        values.put("hasleftimpairment", ev.hasleftimpairment);
        values.put("hasupperamputation", ev.hasupperamputation);
        values.put("haslowerlimbamputation", ev.haslowerlimbamputation);
        values.put("hasrightamputation", ev.hasrightamputation);
        values.put("appropriateprosthesisyesno", ev.appropriateprosthesisyesno);
        values.put("appropriateterminaldeviceyesno", ev.appropriateterminaldeviceyesno);
        values.put("prosthesisfitsyesno", ev.prosthesisfitsyesno);
        values.put("useprostheticproficientlyyesno", ev.useprostheticproficientlyyesno);
        values.put("abilitytopowergraspyesno", ev.abilitytopowergraspyesno);
        values.put("prostheticrecommendations", ev.prostheticrecommendations);
        values.put("prostheticclinicaldescription", ev.prostheticclinicaldescription);
        values.put("medicalconditionsinterferewithtasksyesno", ev.medicalconditionsinterferewithtasksyesno);
        values.put("medicalconditionsinterferewithtasksexplanation", ev.medicalconditionsinterferewithtasksexplanation);
        values.put("medicalfindingsandevaluation", ev.medicalfindingsandevaluation);
        values.put("physicianlastname", ev.physicianlastname);
        values.put("physicianfirstname", ev.physicianfirstname);
        values.put("physicianmiddlename", ev.physicianmiddlename);
        values.put("physiciancity", ev.physiciancity);
        values.put("physicianstate", ev.physicianstate);
        values.put("physicianzipcode", ev.physicianzipcode);
        values.put("physiciantelephonenumber", ev.physiciantelephonenumber);
        values.put("physiatrist", ev.physiatrist);
        values.put("orthopedicsurgeon", ev.orthopedicsurgeon);
        values.put("boardCertifiedyesno", ev.boardCertifiedyesno);
        values.put("boardEligibleyesno", ev.boardEligibleyesno);
        values.put("physiciandate", ev.physiciandate);
        values.put("vehicleTypeslocaldeliveries", ev.vehicleTypeslocaldeliveries);
        values.put("physicalDemandmountingsnowchains", ev.physicalDemandmountingsnowchains);

        values.put("companyName", ev.companyName);
        values.put("companyAddress", ev.companyAddress);
        values.put("companyCity", ev.companyCity);
        values.put("companyState", ev.companyState);
        values.put("companyZipcode", ev.companyZipcode);
        values.put("firstName", ev.firstName);
        values.put("lastName", ev.lastName);
        values.put("maidenName", ev.maidenName);
        values.put("middleName", ev.middleName);
        values.put("nameAddress", ev.nameAddress);
        values.put("nameCity", ev.nameCity);
        values.put("nameState", ev.nameState);
        values.put("nameZip", ev.nameZip);
        values.put("nameAddresshowLong", ev.nameAddresshowLong);
        values.put("nameBirthDate", ev.nameBirthDate);
        values.put("socialSercurityNumber", ev.socialSercurityNumber);
        values.put("pastThreeYearsAddress2", ev.pastThreeYearsAddress2);
        values.put("pastThreeYearsCity2", ev.pastThreeYearsCity2);
        values.put("pastThreeYearsState2", ev.pastThreeYearsState2);
        values.put("pastThreeYearsZipCode2", ev.pastThreeYearsZipCode2);
        values.put("pastThreeYearsHowLong2", ev.pastThreeYearsHowLong2);
        values.put("pastThreeYearsAddress3", ev.pastThreeYearsAddress3);
        values.put("pastThreeYearsCity3", ev.pastThreeYearsCity3);
        values.put("pastThreeYearsState3", ev.pastThreeYearsState3);
        values.put("pastThreeYearsZipCode3", ev.pastThreeYearsZipCode3);
        values.put("pastThreeYearsHowLong3", ev.pastThreeYearsHowLong3);
        values.put("driverName_0", ev.driverName_0);
        values.put("state_0", ev.state_0);
        values.put("licenseNumber_0", ev.licenseNumber_0);
        values.put("licenseType_0", ev.licenseType_0);
        values.put("expirationDate_0", ev.expirationDate_0);
        values.put("classofEquipment_0", ev.classofEquipment_0);
        values.put("typeofEquipment_0", ev.typeofEquipment_0);
        values.put("dateFrom_0", ev.dateFrom_0);
        values.put("dateTo_0", ev.dateTo_0);
        values.put("approximateNumberofMiles_0", ev.approximateNumberofMiles_0);
        values.put("accidentDate_0", ev.accidentDate_0);
        values.put("natureofAccident_0", ev.natureofAccident_0);
        values.put("fatalities_0", ev.fatalities_0);
        values.put("injuries_0", ev.injuries_0);
        values.put("convictionLocation_0", ev.convictionLocation_0);
        values.put("convictionDate_0", ev.convictionDate_0);
        values.put("charge_0", ev.charge_0);
        values.put("penalty_0", ev.penalty_0);
        values.put("deniedalicenseYes", ev.deniedalicenseYes);
        values.put("deniedalicenseNo", ev.deniedalicenseNo);
        values.put("driverLicensePermitDenied", ev.driverLicensePermitDenied);
        values.put("driverLicensePermitRevokedorSuspended", ev.driverLicensePermitRevokedorSuspended);
        values.put("driverLicensePermitNotes", ev.driverLicensePermitNotes);

        values.put("employmentDate", ev.employmentDate);
        values.put("terminalCity", ev.terminalCity);
        values.put("terminalState", ev.terminalState);
        values.put("driverLicenseExpirationDate", ev.driverLicenseExpirationDate);
        values.put("violationsthisyear", ev.violationsthisyear);
        values.put("carrierAddress", ev.carrierAddress);
        values.put("reviewedBy", ev.reviewedBy);
        values.put("reviewedDate", ev.reviewedDate);
        values.put("title", ev.title);
        values.put("violationDate", ev.dateTime);
        values.put("violationOffense", ev.violationOffense);
        values.put("violationLocation", ev.violationLocation);
        values.put("violationtypeofvehicle", ev.violationtypeofvehicle);

        db.insert("evaluations", values);
    }

    public List<Evaluation> getEvaluationList(String evaluationType) {
        List<Evaluation> evaluationList = new ArrayList();

        Cursor cursor = db.getQuery("SELECT LobjectId,Driver_First_Name, Driver_Last_Name, Creation_Date, objectType FROM evaluations WHERE ItemType = '" + evaluationType + "'");


        if (cursor != null && cursor.moveToFirst()) {

            int counter = 0;
            do {
                Evaluation evaluation = new Evaluation();

                evaluation.LobjectId = cursor.getString(counter++);
                String firstName = cursor.getString(counter++);
                evaluation.Driver_First_Name = firstName != null ? firstName : "";
                String lastName = cursor.getString(counter++);
                evaluation.Driver_Last_Name = lastName != null ? lastName : "";
                evaluation.Creation_Date = cursor.getString(counter++);
                evaluation.objectType = cursor.getString(counter++);
                evaluationList.add(evaluation);
                counter = 0;
            } while (cursor.moveToNext());
        }

        cursor.close();

        return evaluationList;
    }

    //endregion

    //region Settings

    public void syncSettings() throws Exception {
//        May 26, 2022  -   The sync is done just at the first install
//        Log.d(TAG, "syncSettings: ");

        Log.d(TAG, "syncSettings: isExists: " + db.exists("SELECT * FROM settings WHERE key LIKE 'database.%'"));
        if (db.exists("SELECT * FROM settings WHERE key LIKE 'database.%'"))
            return;

        String response = Rms.getDirectoryService("Database", "Database");
//        Log.d(TAG, "syncSettings: response: " + response);

        if (response == null || response.trim().length() == 0)
            return;

        JSONArray jsonArray = new JSONArray(response);
        JSONObject o = jsonArray.getJSONObject(0);

        PairList codingFields = Rms.parseJsonCodingFields(o);

        SettingNode node = new SettingNode();
        node.TreeId = codingFields.getValue("treeId");
        node.objectId = Long.valueOf(codingFields.getValue("objectId"));
        node.objectType = codingFields.getValue("objectType");
        node.name = codingFields.getValue("name");
        node.parentTreeId = codingFields.getValue("parentTreeId");
        node.isEncrypted = codingFields.getValue("isEncrypted");
        node.encryptMode = codingFields.getValue("encryptMode");
        node.scanCode = codingFields.getValue("scanCode");

        clearTableEntries("settings", "key LIKE 'database.%'");
        response = Rms.getRecordCoding(node.objectId, node.objectType);
        jsonArray = new JSONArray(response);

//        Log.d(TAG, "syncSettings: jsonArray: " + jsonArray);
        for (int i = 0; i < jsonArray.length(); i++) {
            o = jsonArray.getJSONObject(i);
            String displayName = o.getString("displayName");
            String value = o.getString("value");

            setSetting("database." + displayName, value);
        }
    }

    //endregion

    //region Others

    public static void logDebug(String value) {
//        Log.d(TAG, "logDebug() >>>" + value);
        //logEntry(value);
        //rotateLog();
    }

    public String getOrgName() {
        return Rms.getOrgName();
    }

    public String getOrgNumber() {
        return Rms.getOrgNumber();
    }

    public Context getLastContext() {
        return lastCtx;
    }

    public void setLastCtx(Context ctx) {
        lastCtx = ctx;
    }

    //endregion

    //region Gauges

    private String odometer = null;

    public String calculateNearestTaMilesStr(Location l) {
        Tuple<Double, MapAsset> tuple = calculateNearestTaMiles(l);
        if (tuple == null) {
            return null;
        }

        Double result = tuple.getElement0();

//        Log.d(TAG, "calculateNearestTaMilesStr: result: " + result.toString() + " location: " + l);
        return result == null ? "" : Math.round(result) + " Mi";
    }

    public MapAsset getNearestTaAsset(Location l) {
        Tuple<Double, MapAsset> tuple = calculateNearestTaMiles(l);
        if (tuple == null) {
            return null;
        }
        MapAsset result = tuple.getElement1();
        if (result == null) {
            return null;
        }
        return tuple.getElement1();
    }

    public MapAsset getNearestTaAsset(Location l, MapAsset previousMapAsset) {
        Tuple<Double, MapAsset> tuple = calculateNearestTaMiles(l, previousMapAsset);
        if (tuple == null) {
            return null;
        }
        setNearestTADistance(tuple.getElement0().intValue());
        MapAsset result = tuple.getElement1();
        if (result == null) {
            return null;
        }
        return tuple.getElement1();
    }


    public String calculateNearestTaMilesStr(Activity a) {
        Double result = calculateNearestTaMiles(a);
        return result == null ? "" : Math.round(result) + " Mi";
    }

    public String calculateNearestPilotMilesStr(Location l) {
        Tuple<Double, MapAsset> tuple = calculateNearestPilotMiles(l);
        if (tuple == null) {
            return null;
        }
        Double result = tuple.getElement0();
        return result == null ? "" : Math.round(result) + " Mi";
    }

    public MapAsset getNearestPilotAsset(Location l) {
        Tuple<Double, MapAsset> tuple = calculateNearestPilotMiles(l);
        if (tuple == null) {
            return null;
        }
        MapAsset result = tuple.getElement1();
        if (result == null) {
            return null;
        }
        return tuple.getElement1();
    }

    //    June 15, 2022 -   We need to get the closest asset to my location but it should ignore the one that
//    we are sending as parameter
    public MapAsset getNearestPilotAsset(Location l, MapAsset previousMapAsset) {
        Log.d(TAG, "calculateDistance: location: " + l + " previousMapAsset: " + previousMapAsset);
        Tuple<Double, MapAsset> tuple =
                calculateNearestPilotMiles(l, previousMapAsset);
        Log.d(TAG, "calculateDistance: getNearestPilotAsset: tuple: " + tuple);
        Log.d(TAG, "getNearestPilotAsset: nearestLoveDistance: " + tuple.getElement0());
        if (tuple == null) {
            return null;
        }
        setNearestPilotDistance(tuple.getElement0().intValue());
        MapAsset result = tuple.getElement1();
        Log.d(TAG, "calculateDistance: getNearestPilotAsset: result: " + result);
        if (result == null) {
            return null;
        }
        return tuple.getElement1();
    }

    public String calculateNearestPilotMilesStr(Activity a) {
        Double result = calculateNearestPilotMiles(a);
        return result == null ? "" : Math.round(result) + " Mi";
    }

    public String calculateNearestRestAreaMilesStr(Location l) {
        Tuple<Double, RestArea> tuple = calculateNearestRestAreaMiles(l);
        if (tuple == null) {
            return null;
        }

        Double result = tuple.getElement0();
        return result == null ? "" : Math.round(result) + " Mi";
    }


    public RestArea getNearestRestAreaAsset(Location l) {
        Tuple<Double, RestArea> tuple = calculateNearestRestAreaMiles(l);
        if (tuple == null) {
            return null;
        }
        RestArea result = tuple.getElement1();
        if (result == null) {
            return null;
        }
        return tuple.getElement1();
    }

    public RestArea getNearestRestAreaAsset(Location l, RestArea previousRestArea) {
        Tuple<Double, RestArea> tuple = calculateNearestRestAreaMiles(l, previousRestArea);
        if (tuple == null) {
            return null;
        }
        Log.d(TAG, "getNearestRestAreaAsset: nearestLoveDistance: " + tuple.getElement0());
        setNearestRestAreaDistance(tuple.getElement0().intValue());
        RestArea result = tuple.getElement1();
        if (result == null) {
            return null;
        }
        return tuple.getElement1();
    }

    public String calculateNearestRestAreaMilesStr(Activity a) {
        Double result = calculateNearestRestAreaMiles(a);
        return result == null ? "" : Math.round(result) + " Mi";
    }

    public String calculateNearestLoveMilesStr(Location l) {
        Tuple<Double, MapAsset> tuple = calculateNearestLoveMiles(l);
        if (tuple == null) {
            return null;
        }

        Double result = tuple.getElement0();
        return result == null ? "" : Math.round(result) + " Mi";
    }


    public MapAsset getNearestLoveAsset(Location l) {
        Tuple<Double, MapAsset> tuple = calculateNearestLoveMiles(l);
        if (tuple == null) {
            return null;
        }
        MapAsset result = tuple.getElement1();
        if (result == null) {
            return null;
        }
        return tuple.getElement1();
    }

    public MapAsset getNearestLoveAsset(Location l, MapAsset previousMapAsset) {
        Tuple<Double, MapAsset> tuple = calculateNearestLoveMiles(l, previousMapAsset);
        if (tuple == null) {
            return null;
        }
        setNearestLoveDistance(tuple.getElement0().intValue());
        MapAsset result = tuple.getElement1();
        if (result == null) {
            return null;
        }
        return tuple.getElement1();
    }


    public String calculateNearestLoveMilesStr(Activity a) {
        Double result = calculateNearestLoveMiles(a);
        return result == null ? "" : Math.round(result) + " Mi";
    }

    public LatLng calculateNearestTaStation(Activity a) {
        if (calculateNearestTaMiles(getPhoneLastBestLocation(a)) == null)
            return null;
        MapAsset result = calculateNearestTaMiles(getPhoneLastBestLocation(a)).getElement1();

        if (isDouble(result.Latitude) && isDouble(result.Longitude)) {
            double lat = Double.parseDouble(result.Latitude);
            double lon = Double.parseDouble(result.Longitude);
            return new LatLng(lat, lon);
        } else
            return null;
    }

    public LatLng calculateNearestPilotStation(Activity a) {
        if (calculateNearestPilotMiles(getPhoneLastBestLocation(a)) == null)
            return null;
        MapAsset result = calculateNearestPilotMiles(getPhoneLastBestLocation(a)).getElement1();

        if (isDouble(result.Latitude) && isDouble(result.Longitude)) {
            double lat = Double.parseDouble(result.Latitude);
            double lon = Double.parseDouble(result.Longitude);
            return new LatLng(lat, lon);
        } else
            return null;
    }

    public LatLng calculateNearestRestAreaStation(Activity a) {

        if (calculateNearestRestAreaMiles(getPhoneLastBestLocation(a)) == null)
            return null;
        RestArea result = calculateNearestRestAreaMiles(getPhoneLastBestLocation(a)).getElement1();

        if (isDouble(result.Latitude) && isDouble(result.Longitude)) {
            double lat = Double.parseDouble(result.Latitude);
            double lon = Double.parseDouble(result.Longitude);
            return new LatLng(lat, lon);
        } else
            return null;
    }

    public LatLng calculateNearestLoveStation(Activity a) {

        if (calculateNearestLoveMiles(getPhoneLastBestLocation(a)) == null)
            return null;
        MapAsset result = calculateNearestLoveMiles(getPhoneLastBestLocation(a)).getElement1();

        if (isDouble(result.Latitude) && isDouble(result.Longitude)) {
            double lat = Double.parseDouble(result.Latitude);
            double lon = Double.parseDouble(result.Longitude);
            return new LatLng(lat, lon);
        } else
            return null;
    }

    public void setOdometer(String v) {
        if (StringUtils.isNullOrWhitespaces(v)) {
            odometer = null;
            return;
        }

        try {
            Double d = Double.parseDouble(v);
            Long l = d.longValue();

            odometer = l.toString();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    public String getOdometer() {
        return odometer;
    }

    public String getEngineHours() {
        return getBleParameter("EngineHours");
    }

    public Double calculateNearestTaMiles(Activity a) {
        return calculateNearestTaMiles(getPhoneLastBestLocation(a)).getElement0();
    }

    public Double calculateNearestLoveMiles(Activity a) {
        return calculateNearestLoveMiles(getPhoneLastBestLocation(a)).getElement0();
    }

    public double calculateDistanceInMeter(Location myLocation, MapAsset m) {
        Double lat = null;
        Double lon = null;
        if (isDouble(m.Latitude) && isDouble(m.Longitude)) {
            lat = Double.parseDouble(m.Latitude);
            lon = Double.parseDouble(m.Longitude);
        }
        Location endPoint = new Location("locationB");
        endPoint.setLatitude(lat);
        endPoint.setLongitude(lon);

        return myLocation.distanceTo(endPoint);
    }

    public Tuple<Double, MapAsset> calculateNearestPilotMiles(Location myLocation) {
        if (mMapPilots == null || mMapPilots.size() == 0)
            return null;

        MapAsset nearestAsset = mMapPilots.get(0);
        Double nearestDistanceInKm = calculateDistanceInKm(myLocation, nearestAsset);

        for (int i = 1; i < mMapPilots.size(); i++) {
            MapAsset m = mMapPilots.get(i);

//            Nov 10, 2022  -   In previous file of pilot we required to check asset Owner but in new we don't
//            if (m.AssetOwner == null || m.AssetOwner.toLowerCase().indexOf("pilot") == -1)
//                continue;

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double newDistanceInKm = calculateDistanceInKm(myLocation, m);

            if (newDistanceInKm < nearestDistanceInKm) {
                nearestAsset = m;
                nearestDistanceInKm = newDistanceInKm;
            }
        }

//        Log.d(TAG, "calculateNearestPilotMiles: myLocation: " + myLocation.getLatitude() + ", "
//                + myLocation.getLongitude() + " and nearestAsset: " + nearestAsset.Latitude + ", " + nearestAsset.Longitude);
        return new Tuple<>(Utils.convertKmsToMiles(nearestDistanceInKm), nearestAsset);
    }

    //    June 15, 2022 -   We need to get the closest asset to my location but it should ignore the one that
//    we are sending as parameter
    public Tuple<Double, MapAsset> calculateNearestPilotMiles(Location myLocation, MapAsset
            previousMapAsset) {
        Log.d(TAG, "calculateDistance: calculateNearestPilotMiles: location: " + myLocation + " previousMapAsset: " + previousMapAsset);
        Log.d(TAG, "calculateDistance: calculateNearestPilotMiles: mapPilots: " + mMapPilots);
//        Nov 16, 2022  -   mMapPilots is becoming null most of the time so may we should sync
//        assets again if mMapPilots is null
//        if (mMapPilots == null || mMapPilots.size() == 0)
//            return null;
        if (mMapPilots == null || mMapPilots.size() == 0) {
            try {
                syncAssets(getLastContext());
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }


        MapAsset nearestAsset = mMapPilots.get(0);
        Log.d(TAG, "calculateDistance: nearestAsset: " + nearestAsset);
        Double nearestDistanceInKm = calculateDistanceInKm(myLocation, nearestAsset);
        Log.d(TAG, "calculateDistance: nearestDistanceInKm: " + nearestDistanceInKm);

        for (int i = 1; i < mMapPilots.size(); i++) {
            MapAsset m = mMapPilots.get(i);
//            Log.d(TAG, "calculateDistance: calculateNearest: PilotMiles: " + m.RecordId);
            if (previousMapAsset != null) {
//            June 15, 2022 -   If the asset is same then we should skip it
                if (previousMapAsset.Latitude == m.Latitude && previousMapAsset.Longitude == m.Longitude) {
                    Log.d(TAG, "calculateDistance: previousMapAsset: Latitude: " + previousMapAsset.Latitude + " previousMapAsset: Longitude: " + previousMapAsset.Longitude);
                    continue;
                }
            }
//            Nov 10, 2022  -   In previous file of pilot we required to check asset Owner but in new we don't
//            if (m.AssetOwner == null || m.AssetOwner.toLowerCase().indexOf("pilot") == -1)
//                continue;

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double newDistanceInKm = calculateDistanceInKm(myLocation, m);

            Log.d(TAG, "calculateNearestPilotMiles: newDistance: " + newDistanceInKm + " nearestDistance: " + nearestDistanceInKm + " if: " + (newDistanceInKm < nearestDistanceInKm));
            if (newDistanceInKm < nearestDistanceInKm) {
                nearestAsset = m;
                nearestDistanceInKm = newDistanceInKm;
                Log.d(TAG, "calculateDistance: calculateNearest: PilotMiles: " + nearestAsset.RecordId);
            }
        }

        Log.d(TAG, "calculateDistance: calculateNearestPilotMiles: myLocation: " + myLocation.getLatitude() + ", "
                + myLocation.getLongitude() + " and nearestAsset: " + nearestAsset.Latitude + ", " + nearestAsset.Longitude);
        return new Tuple<>(Utils.convertKmsToMiles(nearestDistanceInKm), nearestAsset);
    }

    public Tuple<Double, RestArea> calculateNearestRestAreaMiles(Location myLocation) {
        if (restAreas == null || restAreas.size() == 0)
            return null;

        RestArea nearestRestArea = restAreas.get(0);
        Double nearestDistanceInKm = calculateDistanceInKm(myLocation, nearestRestArea);

        for (int i = 1; i < restAreas.size(); i++) {
            RestArea m = restAreas.get(i);

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double newDistanceInKm = calculateDistanceInKm(myLocation, m);

            if (newDistanceInKm < nearestDistanceInKm) {
                nearestRestArea = m;
                nearestDistanceInKm = newDistanceInKm;
            }
        }

//        Log.d(TAG, "calculateNearestRestAreaMiles: myLocation: " + myLocation.getLatitude() + ", "
//                + myLocation.getLongitude() + " and nearestAsset: " + nearestRestArea.Latitude + ", " + nearestRestArea.Longitude);
        return new Tuple<>(Utils.convertKmsToMiles(nearestDistanceInKm), nearestRestArea);
    }

    public Tuple<Double, RestArea> calculateNearestRestAreaMiles(Location myLocation, RestArea
            previousRestArea) {
        if (restAreas == null || restAreas.size() == 0)
            return null;

        RestArea nearestRestArea = restAreas.get(0);
        Double nearestDistanceInKm = calculateDistanceInKm(myLocation, nearestRestArea);

        for (int i = 1; i < restAreas.size(); i++) {
            RestArea m = restAreas.get(i);
            if (previousRestArea != null) {
//            June 15, 2022 -   If the asset is same then we should skip it
                if (previousRestArea.Latitude == m.Latitude && previousRestArea.Longitude == m.Longitude) {
//                    Log.d(TAG, "calculateGaugesValuesForDistance: calculateNearestRestAreaMiles: previousMapAsset: Latitude: " + previousRestArea.Latitude + " previousMapAsset: Longitude: " + previousRestArea.Longitude);
                    continue;
                }
            }

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double newDistanceInKm = calculateDistanceInKm(myLocation, m);

            if (newDistanceInKm < nearestDistanceInKm) {
                nearestRestArea = m;
                nearestDistanceInKm = newDistanceInKm;
            }
        }

//        Log.d(TAG, "calculateNearestRestAreaMiles: myLocation: " + myLocation.getLatitude() + ", "
//                + myLocation.getLongitude() + " and nearestAsset: " + nearestRestArea.Latitude + ", " + nearestRestArea.Longitude);
        return new Tuple<>(Utils.convertKmsToMiles(nearestDistanceInKm), nearestRestArea);
    }

    public Tuple<Double, MapAsset> calculateNearestLoveMiles(Location myLocation) {
        if (mapLove == null || mapLove.size() == 0)
            return null;

        MapAsset nearestAsset = mapLove.get(0);
        Double nearestDistanceInKm = calculateDistanceInKm(myLocation, nearestAsset);
        for (int i = 1; i < mapLove.size(); i++) {
            MapAsset m = mapLove.get(i);

//            Nov 10, 2022  -   In previous file of love we required to check asset Owner but in new we don't
//            if (m.AssetOwner == null)
//                continue;

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double newDistanceInKm = calculateDistanceInKm(myLocation, m);

            if (newDistanceInKm != null && newDistanceInKm < nearestDistanceInKm) {
                nearestAsset = m;
                nearestDistanceInKm = newDistanceInKm;
            }
        }

//        Log.d(TAG, "calculateNearestLoveMiles: myLocation: " + myLocation.getLatitude() + ", "
//                + myLocation.getLongitude() + " and nearestAsset: " + nearestAsset.Latitude + ", " + nearestAsset.Longitude);
        return new Tuple<>(Utils.convertKmsToMiles(nearestDistanceInKm), nearestAsset);
    }

    public Tuple<Double, MapAsset> calculateNearestLoveMiles(Location myLocation, MapAsset
            previousMapAsset) {
        if (mapLove == null || mapLove.size() == 0)
            return null;

        MapAsset nearestAsset = mapLove.get(0);
        Double nearestDistanceInKm = calculateDistanceInKm(myLocation, nearestAsset);
        for (int i = 1; i < mapLove.size(); i++) {
            MapAsset m = mapLove.get(i);
            if (previousMapAsset != null) {
//            June 15, 2022 -   If the asset is same then we should skip it
                if (previousMapAsset.Latitude == m.Latitude && previousMapAsset.Longitude == m.Longitude) {
//                    Log.d(TAG, "calculateGaugesValuesForDistance: calculateNearestLoveMiles: previousMapAsset: Latitude: " + previousMapAsset.Latitude + " previousMapAsset: Longitude: " + previousMapAsset.Longitude);
                    continue;
                }
            }

//            Nov 10, 2022  -   In previous file of pilot we required to check asset Owner but in new we don't
//            if (m.AssetOwner == null)
//                continue;

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double newDistanceInKm = calculateDistanceInKm(myLocation, m);

            if (newDistanceInKm != null && newDistanceInKm < nearestDistanceInKm) {
                nearestAsset = m;
                nearestDistanceInKm = newDistanceInKm;
            }
        }

//        Log.d(TAG, "calculateNearestLoveMiles: myLocation: " + myLocation.getLatitude() + ", "
//                + myLocation.getLongitude() + " and nearestAsset: " + nearestAsset.Latitude + ", " + nearestAsset.Longitude);
        return new Tuple<>(Utils.convertKmsToMiles(nearestDistanceInKm), nearestAsset);
    }

    public Tuple<Double, MapAsset> calculateNearestTaMiles(Location myLocation) {
        if (mapTA == null || mapTA.size() == 0)
            return null;

        MapAsset nearestAsset = mapTA.get(0);
        Double nearestDistanceInKm = calculateDistanceInKm(myLocation, nearestAsset);
//        Log.d(TAG, "calculateNearestTaMiles: mapTA: " + mapTA.size() + " nearestAsset: "
//                + nearestAsset.Latitude + "," + nearestAsset.Longitude);

        for (int i = 1; i < mapTA.size(); i++) {
            MapAsset m = mapTA.get(i);

//            Nov 10, 2022  -   In previous file of ta we required to check asset Owner but in new we don't
//            if (m.AssetOwner == null || m.AssetOwner.toLowerCase().indexOf("ta") == -1)
//                continue;

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double newDistanceInKm = calculateDistanceInKm(myLocation, m);

            if (newDistanceInKm < nearestDistanceInKm) {
                nearestAsset = m;
                nearestDistanceInKm = newDistanceInKm;
            }
        }

//        Log.d(TAG, "calculateNearestTaMiles: myLocation: " + myLocation.getLatitude() + ", "
//                + myLocation.getLongitude() + " and nearestAsset: " + nearestAsset.Latitude + ", " + nearestAsset.Longitude);
        return new Tuple<>(Utils.convertKmsToMiles(nearestDistanceInKm), nearestAsset);
    }

    public Tuple<Double, MapAsset> calculateNearestTaMiles(Location myLocation, MapAsset
            previousMapAsset) {
        if (mapTA == null || mapTA.size() == 0)
            return null;

        MapAsset nearestAsset = mapTA.get(0);
        Double nearestDistanceInKm = calculateDistanceInKm(myLocation, nearestAsset);
//        Log.d(TAG, "calculateNearestTaMiles: mapTA: " + mapTA.size() + " nearestAsset: "
//                + nearestAsset.Latitude + "," + nearestAsset.Longitude);

        for (int i = 1; i < mapTA.size(); i++) {
            MapAsset m = mapTA.get(i);
            if (previousMapAsset != null) {
//            June 15, 2022 -   If the asset is same then we should skip it
                if (previousMapAsset.Latitude == m.Latitude && previousMapAsset.Longitude == m.Longitude) {
//                    Log.d(TAG, "calculateGaugesValuesForDistance: calculateNearestTaMiles: previousMapAsset: Latitude: " + previousMapAsset.Latitude + " previousMapAsset: Longitude: " + previousMapAsset.Longitude);
                    continue;
                }
            }

//            Nov 10, 2022  -   In previous file of ta we required to check asset Owner but in new we don't
//            if (m.AssetOwner == null || m.AssetOwner.toLowerCase().indexOf("ta") == -1)
//                continue;

            if (StringUtils.isNullOrWhitespaces(m.Latitude) || StringUtils.isNullOrWhitespaces(m.Longitude))
                continue;

            Double newDistanceInKm = calculateDistanceInKm(myLocation, m);

            if (newDistanceInKm < nearestDistanceInKm) {
                nearestAsset = m;
                nearestDistanceInKm = newDistanceInKm;
            }
        }

//        Log.d(TAG, "calculateNearestTaMiles: myLocation: " + myLocation.getLatitude() + ", "
//                + myLocation.getLongitude() + " and nearestAsset: " + nearestAsset.Latitude + ", " + nearestAsset.Longitude);
        return new Tuple<>(Utils.convertKmsToMiles(nearestDistanceInKm), nearestAsset);
    }

    public Double calculateNearestPilotMiles(Activity a) {
        return calculateNearestPilotMiles(getPhoneLastBestLocation(a)).getElement0();
    }

    public Double calculateNearestRestAreaMiles(Activity a) {
        return calculateNearestRestAreaMiles(getPhoneLastBestLocation(a)).getElement0();
    }

    public ArrayList<EldEvent> getDriverEldEventsAfterEldIdOrAll(String username, int eldId) {
        lock.lock();

        try {
            if (!db.exists("SELECT * FROM eldevents WHERE eldUsername='" + username + "' AND id>" + eldId + " ORDER BY id ASC"))
                return null;

            ArrayList<EldEvent> result = new ArrayList();

            Cursor c = db.getRow(
                    "SELECT " +
                            "id, organizationName, organizationNumber, eldUsername, eventType, " +
                            "eventCode, recordStatus, recordOrigin, truckNumber, vin, localizationDescription, latitudeString, " +
                            "longitudeString, dstSinceLastValidCoords, vehicleMiles, engineHours, orderNumbercmv, orderNumberUser, " +
                            "sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, recordOriginId, checkData, " +
                            "checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                            "editReason, eventseconds, shiftstart, creationdate, odometer " +
                            "FROM " +
                            "eldevents " +
                            "WHERE " +
                            "eldUsername='" + username + "' AND id>" + eldId + " " +
                            "ORDER BY id ASC");

            if (c == null)
                return null;

            c.moveToFirst();
            int col = 0;

            while (!c.isAfterLast()) {
                EldEvent e = new EldEvent();

                e.Id = c.getString(col++);
                e.OrganizationName = c.getString(col++);
                e.OrganizationNumber = c.getString(col++);
                e.EldUsername = c.getString(col++);
                e.EventType = c.getString(col++);
                e.EventCode = c.getString(col++);
                e.RecordStatus = c.getString(col++);
                e.RecordOrigin = c.getString(col++);
                e.TruckNumber = c.getString(col++);
                e.Vin = c.getString(col++);
                e.LocalizationDescription = c.getString(col++);
                e.LatitudeString = c.getString(col++);
                e.LongitudeString = c.getString(col++);
                e.DstSinceLastValidCoords = c.getString(col++);
                e.VehicleMiles = c.getString(col++);
                e.EngineHours = c.getString(col++);
                e.OrderNumberCmv = c.getString(col++);
                e.OrderNumberUser = c.getString(col++);
                e.SequenceId = c.getString(col++);
                e.EventCodeDescription = c.getString(col++);
                e.DiagnosticIndicator = c.getString(col++);
                e.MalfunctionIndicator = c.getString(col++);
                e.Annotation = c.getString(col++);
                e.RecordOriginId = c.getString(col++);
                e.CheckData = c.getString(col++);
                e.CheckSum = c.getString(col++);
                e.MalfunctionDiagnosticCode = c.getString(col++);
                e.MalfunctionDiagnosticDescp = c.getString(col++);
                e.DriverLastName = c.getString(col++);
                e.DriverFirstName = c.getString(col++);
                e.DriverRecordId = c.getString(col++);
                e.EditReason = c.getString(col++);
                e.EventSeconds = c.getDouble(col++);
                e.ShiftStart = c.getString(col++);
                e.CreationDate = c.getString(col++);
                e.Odometer = c.getString(col++);

                result.add(e);
                c.moveToNext();
                col = 0;
            }

            c.close();
            return result;
        } finally {
            lock.unlock();
        }
    }

    public ArrayList<EldEvent> getCurrentShiftEldEvents() {
        ArrayList<EldEvent> eldEvents = getAllDutyEventsSincePast48Hours();

        if (eldEvents == null)
            return null;

        EldEvent mostRecentSleeperBerth = findMostRecentSleeperBerthEvent(eldEvents);
        Long mostRecentSleeperBerthId = mostRecentSleeperBerth != null ? Long.valueOf(mostRecentSleeperBerth.Id) : -1;
        ArrayList<EldEvent> result = new ArrayList<EldEvent>();

        for (int i = 0; i < eldEvents.size(); i++) {
            EldEvent e = eldEvents.get(i);

            if (!StringUtils.isNullOrWhitespaces(e.Id) && e.getIdAsLong() > mostRecentSleeperBerthId)
                result.add(e);
        }

        //eldEvents = getDriverEldEventsAfterEldIdOrAll(username, Integer.parseInt(id));
        return eldEvents;
    }

    public Long getMaxDrivingHoursPerDay() {
        //Rule stateRules = getActiveRule();
        //return Long.parseLong(stateRules.HoursPerDay);
        return 11l;
    }

    public Long getMinSleeperBearthHoursPerDay() {
        return 7l;
    }

    public Long getMinBreakMinsPerEach8HrsDriveBlock() {
        return 30l;
    }

    public Long getMaxDrivingHoursUntilBreak() {
        return 8l;
    }

    public Long getShiftHours() {
        return 14l;
    }

    public Long getCycleHours() {
        return getCycleDays() * 24; // final int CYCLE_HOURS = (7 or 8 days) * 24;
    }

    public Long getCycleDays() {
        Rule stateRules = getActiveRule();

//        May 04, 2022  -   we should makes sure that state rules is not null
//        because its generating a crash
        String days = "0";
        if (stateRules == null) {
            days = getMultiDayBasisUsed();
        } else {
            days = stateRules.Days;
        }

        return Long.parseLong(days);
    }

    public Long getRemainingBreakMins(Long breakSecs) {
        if (breakSecs == null)
            return getMinBreakMinsPerEach8HrsDriveBlock();

        return ((getMinBreakMinsPerEach8HrsDriveBlock() * 60l) - breakSecs) / 60l;
    }

    public Long getRemainingDrivingHoursForToday(Long drivingSecs) {
        if (drivingSecs == null)
            return 0l;

        Long maxDrivingSecs = getMaxDrivingHoursPerDay() * 60 * 60;
        Double result = ((maxDrivingSecs - drivingSecs) / 60d / 60d);

        return result.longValue();
    }

    public Long getRemainingDrivingHoursUntilBreak(Long lastConsecutiveDrivingSecsSoFar) {
        if (lastConsecutiveDrivingSecsSoFar == null || lastConsecutiveDrivingSecsSoFar == 0)
            return getMaxDrivingHoursUntilBreak();

        Long maxDrivingSecs = getMaxDrivingHoursUntilBreak() * 60 * 60;
        Double result = ((maxDrivingSecs - lastConsecutiveDrivingSecsSoFar) / 60d / 60d);

        return result.longValue();
    }

    public Long getRemainingSleeperBearthHoursForToday(Long breakSecs) {
        if (breakSecs == null)
            return 0l;

        Long minSleeperBearthSecs = getMinSleeperBearthHoursPerDay() * 60 * 60;
        Double result = ((minSleeperBearthSecs - breakSecs) / 60d / 60d);

        return result.longValue();
    }

    public Long getRemainingShiftHoursForToday(Long shiftSecs) {
        if (shiftSecs == null)
            return 0l;

        Long totalShiftSecs = getShiftHours() * 60 * 60;
        Double result = ((totalShiftSecs - shiftSecs) / 60d / 60d);

        return result.longValue();
    }

    public Long getRemainingCycleHoursForToday(Long cycleSecs) {
        if (cycleSecs == null)
            return 0l;

        Long days = getCycleDays();
        Long totalCycleSecs = days * 24 * 60 * 60;
        Double result = ((totalCycleSecs - cycleSecs) / 60d / 60d);

        return result.longValue();
    }

    public Long calculateMilesToday(ArrayList<EldEvent> eldEvents) {
        try {
            Double result = 0d;

            if (eldEvents == null)
                return null;

            EldEvent previousEldEvent = null;

            for (int i = 0; i < eldEvents.size(); i++) {
                EldEvent e = eldEvents.get(i);

                boolean isPreviousEldEventDriving = previousEldEvent != null && previousEldEvent.EventCode != null &&
                        previousEldEvent.EventCode.equals("3") && previousEldEvent.EventType != null && previousEldEvent.EventType.equals("1");

                boolean isPreviousEldEventContinuousDriving = previousEldEvent != null && previousEldEvent.EventCode != null &&
                        (previousEldEvent.EventCode.equals("1") || previousEldEvent.EventCode.equals("2")) &&
                        previousEldEvent.EventType != null && previousEldEvent.EventType.equals("2");

                if (e.VehicleMiles != null && (isPreviousEldEventDriving || isPreviousEldEventContinuousDriving)) {
                    result += (Double.valueOf(e.VehicleMiles) - Double.valueOf(previousEldEvent.VehicleMiles));
                }

                previousEldEvent = e;
            }

            if (isDriverLoggedIn()) {
                String drivingStatus = getDrivingStatus();

                if (drivingStatus != null && drivingStatus.equalsIgnoreCase("Driving")) {
                    String vehicleMiles = getBleParameter("TripDistance");

                    Double lastRecordedVehicleMiles = 0d;

                    if (eldEvents != null && eldEvents.size() > 0)
                        lastRecordedVehicleMiles = Double.valueOf(eldEvents.get(eldEvents.size() - 1).VehicleMiles);

                    if (vehicleMiles != null)
                        result += Double.valueOf(vehicleMiles) - lastRecordedVehicleMiles;
                }
            }
            return result.longValue();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public Long calculateDrivingSecs(ArrayList<EldEvent> eldEvents) {
        try {
            if (eldEvents == null)
                return null;

            Long result = 0l;
            EldEvent previousEldEvent = null;

            for (int i = 0; i < eldEvents.size(); i++) {
                EldEvent e = eldEvents.get(i);

                boolean isPreviousEldEventDriving = previousEldEvent != null && previousEldEvent.EventCode != null &&
                        previousEldEvent.EventCode.equals("3") && previousEldEvent.EventType != null && previousEldEvent.EventType.equals("1");

                /*boolean isPreviousEldEventContinuousDriving = previousEldEvent != null && previousEldEvent.EventCode != null &&
                        (previousEldEvent.EventCode.equals("1") || previousEldEvent.EventCode.equals("2")) &&
                        previousEldEvent.EventType != null && previousEldEvent.EventType.equals("2");*/

                if (e.CreationDate != null && isPreviousEldEventDriving && previousEldEvent.CreationDate != null)
                    result += DateUtils.diffInSecs(DateUtils.parseDate(e.CreationDate), DateUtils.parseDate(previousEldEvent.CreationDate));

                previousEldEvent = e;
            }

            if (isDriverLoggedIn()) {
                String drivingStatus = getDrivingStatus();

                if (drivingStatus != null && drivingStatus.equalsIgnoreCase("Driving")) {
                    String latestCreationDateStr = eldEvents.get(eldEvents.size() - 1).CreationDate;
                    Date now = new Date();

                    result += DateUtils.diffInSecs(now, DateUtils.parseDate(latestCreationDateStr));
                }
            }

            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public Long calculateCurrentDrivingBlock(ArrayList<EldEvent> eldEvents) {
        try {

            if (!isDriverLoggedIn())
                return 0l;

            // We are assuming Break it's not the first event on a shift therefore there will always be a previous event

            if (eldEvents == null || eldEvents.size() == 0)
                return 0l;

            EldEvent lastEldEvent = eldEvents.get(eldEvents.size() - 1);
            String latestCreationDateStr = lastEldEvent.CreationDate;

            Long result = 0l;
            Long breakSecs = 0l;
            EldEvent ePlusOne = null;

            for (int i = eldEvents.size() - 1; i >= 0; i--) {
                EldEvent e = eldEvents.get(i);

                if (ePlusOne != null && e.CreationDate != null && ePlusOne.CreationDate != null) {
                    if (isEldDrivingStatus(e))
                        result += DateUtils.diffInSecs(DateUtils.parseDate(ePlusOne.CreationDate), DateUtils.parseDate(e.CreationDate));
                    else {
                        boolean isEldEventABreakStatus = isEldOffDutyStatus(ePlusOne) || isEldOnDutyStatus(ePlusOne) || isEldSleeperBerthStatus(ePlusOne);

                        if (isEldEventABreakStatus) {
                            breakSecs += DateUtils.diffInSecs(DateUtils.parseDate(ePlusOne.CreationDate), DateUtils.parseDate(e.CreationDate));

                            if (breakSecs >= 1800)
                                break;
                        }
                    }
                }

                ePlusOne = e;
            }

            if (StringUtils.equalsIgnoreCaseAny(getDrivingStatus(), new String[]{"Driving"}))
                result += DateUtils.diffInSecs(new Date(), DateUtils.parseDate(latestCreationDateStr));

            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public Long calculateBreakSecs(ArrayList<EldEvent> eldEvents) {
        // Get last break block to calculate this value (break, onduty, sleeperberth) + real time counter

        try {
            Long result = 0l; // We are assuming Break it's not the first event on a shift therefore there will always be a previous event
            EldEvent ePlusOne = null;

            for (int i = eldEvents.size() - 1; i >= 0; i--) {
                EldEvent e = eldEvents.get(i);

                if (ePlusOne != null && ePlusOne.CreationDate != null && e.CreationDate != null) {
                    boolean isDrivingEvent = isEldDrivingStatus(ePlusOne);

                    if (isDrivingEvent)
                        break;

                    boolean isEldEventABreakStatus = isEldOnDutyStatus(e) || isEldSleeperBerthStatus(e);

                    if (isEldEventABreakStatus)
                        result += DateUtils.diffInSecs(DateUtils.parseDate(ePlusOne.CreationDate), DateUtils.parseDate(e.CreationDate));
                }

                ePlusOne = e;
            }

            if (!isDriverLoggedIn())
                return result;

            if (!StringUtils.equalsIgnoreCaseAny(getDrivingStatus(), new String[]{"Break", "On Duty", "OnDuty", "Sleeper Berth", "SleeperBerth"}))
                return result;

            EldEvent latestEldEvent = eldEvents.get(eldEvents.size() - 1);

            if (!isEldDrivingStatus(latestEldEvent))
                result += DateUtils.diffInSecs(new Date(), DateUtils.parseDate(latestEldEvent.CreationDate));

            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public Long calculateSleeperBerthSecs(ArrayList<EldEvent> eldEvents) {
        try {
            if (!isDriverLoggedIn())
                return 0l;

            String drivingStatus = getDrivingStatus();

            if (StringUtils.equalsIgnoreCaseAny(drivingStatus, new String[]{"Sleeper Berth", "Sleeper Bearth"})) {
                if (eldEvents == null && eldEvents.size() == 0)
                    return 0l;

                String latestCreationDateStr = eldEvents.get(eldEvents.size() - 1).CreationDate;
                Date now = new Date();

                return DateUtils.diffInSecs(now, DateUtils.parseDate(latestCreationDateStr));
            }

            return 0l;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public Long calculateShiftSecs(ArrayList<EldEvent> eldEvents) {
        try {
            if (eldEvents == null || eldEvents.size() == 0)
                return null;

            Long result = 0l;
            EldEvent previousEldEvent = null;

            for (int i = 0; i < eldEvents.size(); i++) {
                EldEvent e = eldEvents.get(i);

                if (previousEldEvent != null && previousEldEvent.CreationDate != null && e.CreationDate != null) {
                    if (!isEldOffDutyStatus(previousEldEvent) && isEventTypeChangeInDriverDutyStatus(previousEldEvent))
                        result += DateUtils.diffInSecs(DateUtils.parseDate(e.CreationDate), DateUtils.parseDate(previousEldEvent.CreationDate));
//                    Log.d(TAG, ">>>calculateShiftSecs " + e.toString() + " - " + result);
                }

                previousEldEvent = e;
            }

            if (isDriverLoggedIn()) {
                String drivingStatus = getDrivingStatus();

                if (!StringUtils.isNullOrWhitespaces(drivingStatus) && !StringUtils.equalsIgnoreCaseAny(drivingStatus, new String[]{"Status", "OffDuty", "Off Duty"})) {
                    EldEvent latestEldEvent = eldEvents.get(eldEvents.size() - 1);
                    result += DateUtils.diffInSecs(new Date(), DateUtils.parseDate(latestEldEvent.CreationDate));
                }
            }

            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public Long calculateShiftSecsOld() {
        try {
            ArrayList<EldEvent> eldEvents = getCurrentShiftEldEvents();

            if (eldEvents == null || eldEvents.size() == 0)
                return null;

            Long result = 0l;
            EldEvent firstEldEvent = eldEvents.get(0);
            EldEvent latestEldEvent = null;

            if (eldEvents.size() > 0) {
                latestEldEvent = eldEvents.get(eldEvents.size() - 1);

                if (latestEldEvent != null &&
                        !StringUtils.isNullOrWhitespacesAny(new String[]{latestEldEvent.CreationDate, firstEldEvent.CreationDate}) &&
                        !isEldOffDutyStatus(latestEldEvent))
                    result += DateUtils.diffInSecs(DateUtils.parseDate(latestEldEvent.CreationDate), DateUtils.parseDate(firstEldEvent.CreationDate));
            }

            if (isDriverLoggedIn()) {
                String drivingStatus = getDrivingStatus();

                if (!StringUtils.isNullOrWhitespaces(drivingStatus) && !StringUtils.equalsIgnoreCaseAny(drivingStatus, new String[]{"Status"}))
                    result += DateUtils.diffInSecs(new Date(), DateUtils.parseDate(latestEldEvent.CreationDate));
            }

            return result;
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }


    public Long calculateCycleSecs() {
//        Log.d(TAG, "calculateCycleSecs: ");
        try {
//            March 21, 2022    -   Don't call getOpenTruckLogHeader each time the method rather saved its creationTime value
//            and used it
            if (currentTruckLogHeaderCreationDateTime == null) {
                TruckLogHeader h = getOpenTruckLogHeader();
//                Log.d(TAG, "calculateCycleSecs: truckLogHeader: " + h);
                currentTruckLogHeaderCreationDateTime = h.CreationDatetime;
//                Log.d(TAG, "calculateCycleSecs: CreationDatetime: " + currentTruckLogHeaderCreationDateTime);
            }
//            ArrayList<EldEvent> eldEvents = getEldEvents(username, currentTruckLogHeaderCreationDateTime);
            ArrayList<EldEvent> eldEvents = getEldEvents(username, currentTruckLogHeaderCreationDateTime, "1");

            if (eldEvents == null || eldEvents.size() == 0)
                return 0l;

            Long result = 0l;
            EldEvent previousEldEvent = null;

            for (int i = 0; i < eldEvents.size(); i++) {
                EldEvent e = eldEvents.get(i);

                if (previousEldEvent != null && previousEldEvent.CreationDate != null && e.CreationDate != null) {
                    if (isEldDrivingStatus(previousEldEvent))
                        result += DateUtils.diffInSecs(DateUtils.parseDate(e.CreationDate), DateUtils.parseDate(previousEldEvent.CreationDate));
                }

                previousEldEvent = e;
            }

            if (isDriverLoggedIn()) {
                String drivingStatus = getDrivingStatus();

                if (drivingStatus != null && drivingStatus.equalsIgnoreCase("Driving")) {
                    String latestCreationDateStr = eldEvents.get(eldEvents.size() - 1).CreationDate;
                    Date now = new Date();

                    result += DateUtils.diffInSecs(now, DateUtils.parseDate(latestCreationDateStr));
                }
            }

            return result;
        } catch (Throwable throwable) {
            Log.d(TAG, "calculateCycleSecs: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();

            return null;
        }
    }

    public boolean isCurrentEldStatusOffDuty() {
        return StringUtils.equalsIgnoreCaseAny(getDrivingStatus(), new String[]{"Off Duty", "OffDuty"});
    }

    public boolean isCurrentEldStatusOnDuty() {
        return StringUtils.equalsIgnoreCaseAny(getDrivingStatus(), new String[]{"On Duty", "OnDuty"});
    }

    public boolean isCurrentEldStatusBreak() {
        return StringUtils.equalsIgnoreCaseAny(getDrivingStatus(), new String[]{"Break"});
    }

    public boolean isCurrentEldStatusABreakStatus() {
        return isCurrentEldStatusBreak() || isCurrentEldStatusOnDuty() || isCurrentEldStatusOffDuty();
    }

    public boolean isCurrentEldStatusSleeperBerth() {
        return StringUtils.equalsIgnoreCaseAny(getDrivingStatus(), new String[]{"Sleeper Berth", "Sleeper Bearth"});
    }

    public boolean isEldSleeperBerthStatus(EldEvent e) {
        String eldStatus = getEldStatusStr(e);
        return StringUtils.equalsIgnoreCaseAny(eldStatus, new String[]{"Sleeper Berth", "Sleeper Bearth"});
    }

    public boolean isEldDrivingStatus(EldEvent e) {
        String eldStatus = getEldStatusStr(e);
        return StringUtils.equalsIgnoreCaseAny(eldStatus, new String[]{"Driving"});
    }

    public boolean isEldOffDutyStatus(EldEvent e) {
        String eldStatus = getEldStatusStr(e);
        return StringUtils.equalsIgnoreCaseAny(eldStatus, new String[]{"Off Duty", "OffDuty"});
    }

    public boolean isEldOnDutyStatus(EldEvent e) {
        String eldStatus = getEldStatusStr(e);
        return StringUtils.equalsIgnoreCaseAny(eldStatus, new String[]{"On Duty", "OnDuty"});
    }

    public boolean isEventTypeChangeInDriverDutyStatus(EldEvent e) {
        return StringUtils.equalsIgnoreCaseAny(e.EventType, new String[]{"1"});
    }

    public String getEldStatusStr(EldEvent e) {
        if (e == null)
            return null;

        String eventType = e.EventType;
        String eventCode = e.EventCode;

        if (StringUtils.equalsIgnoreCase(eventType, "1") && StringUtils.equalsIgnoreCase(eventCode, "1"))
            return "Off Duty";
        else if (StringUtils.equalsIgnoreCase(eventType, "1") && StringUtils.equalsIgnoreCase(eventCode, "2"))
            return "Sleeper Berth";
        else if (StringUtils.equalsIgnoreCase(eventType, "1") && StringUtils.equalsIgnoreCase(eventCode, "3"))
            return "Driving";
        else if (StringUtils.equalsIgnoreCase(eventType, "2") && StringUtils.equalsIgnoreCase(eventCode, "1"))
            return "Driving";
        else if (StringUtils.equalsIgnoreCase(eventType, "2") && StringUtils.equalsIgnoreCase(eventCode, "2"))
            return "Driving";
        else if (StringUtils.equalsIgnoreCase(eventType, "1") && StringUtils.equalsIgnoreCase(eventCode, "4"))
            return "On Duty";

        return null;
    }

    //endregion

    //region User rights

    private List<String> userRights = new ArrayList<String>();

    public boolean existsUserRight(String value) {
        if (userRights == null)
            return false;

        for (String userRight : userRights)
            if (userRight != null && userRight.toString().equalsIgnoreCase(value))
                return true;

        return false;
    }

    //endregion

    //region Constants

    public static final String TAG = "BusinessRules";

    public static final int OK = 1000;
    public static final int UNABLE_TO_INITIALIZE = -1000;
    public static final int UNABLE_TO_SYNC = -2000;
    public static final int ROLLBACK_ERROR = -3000;
    public static final int UNDEFINED = -1;

    public static final int THREE_DAYS_IN_MINS = 3 * 24 * 60;

    public static String[] RMS_CODINGDATA_RECORD_TYPES = {Crms.R_TRUCK_DVIR_HEADER, Crms.R_TRUCK_DVIR_DETAIL,
            Crms.R_SIGNATURE, Crms.R_FUEL_RECEIPT};

    public static String[] RMS_RECORD_TYPES = {Crms.R_TRUCK_DVIR_HEADER, Crms.R_TRUCK_DVIR_DETAIL,
            Crms.R_SIGNATURE, Crms.R_FUEL_RECEIPT, Crms.R_IFTA_EVENT};
    //endregion

    public void recordSpeedingELDEvent(Activity activity, boolean isSpeeding, int speedValue) {
//        Log.d(TAG, "recordSpeedingELDEvent: ");
        if (isSpeeding) {
            setEldEvent(activity,
                    EventType.SPEEDING,
                    EventCode.SPEEDING_STARTED,
                    EventRecordStatus.ACTIVE,
                    EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD, "SPEEDING STARTED", "" + speedValue, "", "");
        } else {
            setEldEvent(activity,
                    EventType.SPEEDING,
                    EventCode.SPEEDING_ENDED,
                    EventRecordStatus.ACTIVE,
                    EventCodeOrigin.AUTOMATICALLY_RECORDED_BY_ELD, "SPEEDING ENDED", "" + speedValue, "", "");
        }

    }

    //    March 30, 2022    -   It searches for an active truck log header and creates the necessary records
    public boolean existsTruckLogRecords() {

//        Log.d(TAG, "existsTruckLogRecords: ");
        String truckLogHeaderMobileRecordId;
        TruckLogHeader truckLogHeader = getOpenTruckLogHeader();
//        Log.d(TAG, "existsTruckLogRecords: truckLogHeader: " + truckLogHeader);

        if (truckLogHeader == null) {
//            Log.d(TAG, "existsTruckLogRecords: truckLogHeader is null so create a new one");
            return false;
        } else {
            truckLogHeaderMobileRecordId = truckLogHeader.MobileRecordId;
//            Log.d(TAG, "existsTruckLogRecords: truckLogHeaderMobileRecordId: " + truckLogHeaderMobileRecordId);
        }

//        Log.d(TAG, "existsTruckLogRecords: " + existsTruckEldHeaderEntry(truckLogHeaderMobileRecordId));

        TruckLogDetail truckLogDetail = getTruckLogDetailForToday(truckLogHeaderMobileRecordId, "");
//        Log.d(TAG, "existsTruckLogRecords: truckLogDetail: " + truckLogDetail);

        if (truckLogDetail == null) {
//            Log.d(TAG, "existsTruckLogRecords: truckLogDetail is null");


            return false;
        }

        return true;
    }

    //    July 19, 2022 -   We change its return type such that it returns true if we create a new truck log header
    public boolean createTruckLogRecords(Activity activity) {
        Log.d(TAG, "applyImplementation: createTruckLogRecords: ");
        boolean isNewTruckLogCreated = false;
//        Log.d(TAG, "existsTruckLogRecords: ");
        String truckLogHeaderMobileRecordId;
        TruckLogHeader truckLogHeader = getOpenTruckLogHeader();
//        Log.d(TAG, "existsTruckLogRecords: truckLogHeader: " + truckLogHeader);

        Log.d(TAG, "createTruckLogRecords: truckLogHeader: " + truckLogHeader);
        if (truckLogHeader == null) {
//            Log.d(TAG, "existsTruckLogRecords: truckLogHeader is null so create a new one");
            truckLogHeaderMobileRecordId = createNewDriverTruckLogHeaderEntry();
            isNewTruckLogCreated = true;
        } else {
            truckLogHeaderMobileRecordId = truckLogHeader.MobileRecordId;
//            Log.d(TAG, "existsTruckLogRecords: truckLogHeaderMobileRecordId: " + truckLogHeaderMobileRecordId);
        }

//        Log.d(TAG, "existsTruckLogRecords: " + existsTruckEldHeaderEntry(truckLogHeaderMobileRecordId));
        if (!existsTruckEldHeaderEntry(truckLogHeaderMobileRecordId))
            createNewDriverTruckEldHeaderEntry(truckLogHeaderMobileRecordId);

        String truckLogDetailMobileRecordId;
        TruckLogDetail truckLogDetail = getTruckLogDetailForToday(truckLogHeaderMobileRecordId, user.getTruckNumber());
//        Log.d(TAG, "existsTruckLogRecords: truckLogDetail: " + truckLogDetail);
        Log.d(TAG, "applyImplementation: createTruckLogRecords: truckLogDetail: " + truckLogDetail + " truckLogHeaderMobileRecordId: " + truckLogHeaderMobileRecordId + " truckNumber: " + user.getTruckNumber());

        if (truckLogDetail == null) {
//            Log.d(TAG, "existsTruckLogRecords: truckLogDetail is null");
            truckLogDetailMobileRecordId = createNewDriverTruckLogDetailEntry(truckLogHeaderMobileRecordId);
//            Log.d(TAG, "existsTruckLogRecords: truckLogDetail: truckLogDetailMobileRecordId: " + truckLogDetailMobileRecordId);
            setTruckLogHeaderEntryToUnsent(truckLogHeaderMobileRecordId);
            truckLogDetail = getTruckLogDetailForToday(truckLogHeaderMobileRecordId, user.getTruckNumber());
//            Log.d(TAG, "existsTruckLogRecords: truckLogDetail: " + truckLogDetail);
        } else {
//            Log.d(TAG, "existsTruckLogRecords: else: truckLogDetail is not null");
            truckLogDetailMobileRecordId = truckLogDetail.MobileRecordId;
            Log.d(TAG, "applyImplementation: createTruckLogRecords: truckLogDetailMobileRecordId: " + truckLogDetailMobileRecordId);
//            Log.d(TAG, "existsTruckLogRecords: else: truckLogDetailMobileRecordId: " + truckLogDetailMobileRecordId);
        }

        Log.d(TAG, "applyImplementation: existsTruckLogRecords: truckLogDetailMobileRecordId: " + truckLogDetailMobileRecordId);
        Log.d(TAG, "applyImplementation: existsTruckLogRecords: existsTruckEldDetailEntry: " + existsTruckEldDetailEntry(truckLogDetailMobileRecordId, user.getTruckNumber()));
        if (!existsTruckEldDetailEntry(truckLogDetailMobileRecordId, user.getTruckNumber()))
            createNewDriverTruckEldDetailEntry(activity, truckLogHeaderMobileRecordId, truckLogDetailMobileRecordId, truckLogDetail);

        Log.d(TAG, "applyImplementation: existsTruckLogRecords: existsVehiclePowerSwitchEvent: " + existsVehiclePowerSwitchEvent());

        return isNewTruckLogCreated;
    }

//    A_CHANGE_IN_DRIVER_DUTY_STATUS(1),
//    AN_INTERMEDIATE_LOG(2),
//    A_CHANGE_IN_DRIVERS_INDICATION_OF_AUTHORIZED_PERSONAL_USE_OF_CMV_OR_YARD_MOVES(3),
//    A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS(4),
//    A_DRIVER_LOGIN_LOGOUT_ACTIVITY(5),
//    CMVS_ENGINE_POWER_UP_SHUT_DOWN_ACTIVITY(6),
//    A_MALFUNCTION_OR_DATA_DIAGNOSTIC_DETECTION_OCCURRENCE(7),

    //    public ArrayList<EldEvent> getAllEldEventItems(String username, String eventTypes) {
    public ArrayList<EldEvent> getAllEldEventItems(String whereClause) {
        Log.d(TAG, "DutyAndEldEvents: getAllEldEventItems: ");
        EldEvent e = null;
        lock.lock();

        try {
            ArrayList<EldEvent> eventsResult = new ArrayList<>();
            String sqlString = null;

//            April 18, 2022    -   Why object Id should be null for both Diagnostic and Maldfunction
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

//            sqlString = "SELECT id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, " +
//                    "recordOrigin, truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
//                    "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, " +
//                    "recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
//                    "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE " + eventTypes +
//                    " AND eldUsername='" + username + "' ORDER BY id DESC";

            sqlString = "SELECT id, organizationName, organizationNumber, eldUsername, eventType, eventCode, recordStatus, " +
                    "recordOrigin, truckNumber, vin, localizationDescription, latitudeString, longitudeString, dstSinceLastValidCoords, vehicleMiles, " +
                    "engineHours, orderNumbercmv, orderNumberUser, sequenceId, eventCodeDescription, diagnosticIndicator, malfunctionIndicator, annotation, " +
                    "recordOriginId, checkData, checkSum, malfunctionDiagnosticCode, malfunctionDiagnosticDescp, driverLastName, driverFirstName, driverRecordId, " +
                    "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE " + whereClause;
//                        "editReason, eventseconds, shiftstart, creationdate, odometer FROM eldevents WHERE eventType='7' AND (eventCode='3' OR eventCode='4') AND objectId IS NULL ORDER BY id DESC";
//            Log.d(TAG, "getAllEldEventItems: sqlString: " + sqlString);

            Cursor cursor = db.getQuery(sqlString);
//            Log.d(TAG, "DutyAndEldEvents: getAllEldEventItems: cursor: " + cursor);
            if (cursor == null)
                return null;

            cursor.moveToFirst();
            int col = 0;
//            Log.d(TAG, "DutyAndEldEvents: getAllEldEventItems: c: count: " + cursor.getCount());


            while (!cursor.isAfterLast()) {

                //            Log.d(TAG, "DutyAndEldEvents: getAllEldEventItems: c: position: " + cursor.getPosition());
                e = new EldEvent();

                e.Id = cursor.getString(col++);
                e.OrganizationName = cursor.getString(col++);
                e.OrganizationNumber = cursor.getString(col++);
                e.EldUsername = cursor.getString(col++);
                e.EventType = cursor.getString(col++);
                e.EventCode = cursor.getString(col++);
                e.RecordStatus = cursor.getString(col++);
                e.RecordOrigin = cursor.getString(col++);
                e.TruckNumber = cursor.getString(col++);
                e.Vin = cursor.getString(col++);
                e.LocalizationDescription = cursor.getString(col++);
                e.LatitudeString = cursor.getString(col++);
                e.LongitudeString = cursor.getString(col++);
                e.DstSinceLastValidCoords = cursor.getString(col++);
                e.VehicleMiles = cursor.getString(col++);
                e.EngineHours = cursor.getString(col++);
                e.OrderNumberCmv = cursor.getString(col++);
                e.OrderNumberUser = cursor.getString(col++);
                e.SequenceId = cursor.getString(col++);
                e.EventCodeDescription = cursor.getString(col++);
                e.DiagnosticIndicator = cursor.getString(col++);
                e.MalfunctionIndicator = cursor.getString(col++);
                e.Annotation = cursor.getString(col++);
                e.RecordOriginId = cursor.getString(col++);
                e.CheckData = cursor.getString(col++);
                e.CheckSum = cursor.getString(col++);
                e.MalfunctionDiagnosticCode = cursor.getString(col++);
                e.MalfunctionDiagnosticDescp = cursor.getString(col++);
                e.DriverLastName = cursor.getString(col++);
                e.DriverFirstName = cursor.getString(col++);
                e.DriverRecordId = cursor.getString(col++);
                e.EditReason = cursor.getString(col++);
                e.EventSeconds = cursor.getDouble(col++);
                e.ShiftStart = cursor.getString(col++);
                e.CreationDate = cursor.getString(col++);
                e.Odometer = cursor.getString(col++);

                //            Log.d(TAG, "DutyAndEldEvents: getAllEldEventItems: last7Days: " + DateUtils.getCurrentDateTimeSevenDaysAgo("", DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS, -7));
                Date currentDateTimeSevenDaysAgo = DateUtils.getCurrentDateTimeSevenDaysAgo("", DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS, -7);

                Date myDate = dateFormat.parse(e.CreationDate);
                //            Log.d(TAG, "DutyAndEldEvents: getAllEldEventItems: time: " + (myDate.getTime() > currentDateTimeSevenDaysAgo.getTime()));
                if (myDate.getTime() > currentDateTimeSevenDaysAgo.getTime()) {
                    eventsResult.add(e);
                }

                cursor.moveToNext();
                col = 0;
            }

            cursor.close();
            return eventsResult;
        } catch (Throwable throwable) {
            Log.d(TAG, "DutyAndEldEvents: getAllEldEventItems: throwable: " + throwable.getMessage());
            return null;
        } finally {
            lock.unlock();
        }
    }

    String getLastLatitude(Activity activity) {

        String lat = getBleParameter("Latitude");

        Double absoluteLatitude = 0.0;
        if (lat != null) {
            absoluteLatitude = Math.abs(Double.parseDouble(lat));
        }

        if (StringUtils.isNullOrWhitespacesAny(new String[]{lat}) || (absoluteLatitude <= 1)) {
            Location location = getPhoneLastBestLocation(activity);

            if (location != null) {
                lat = Double.toString(location.getLatitude());
            }
        }

        return lat;
    }

    String getLastLongitude(Activity activity) {

        String lon = getBleParameter("Longitude");

        Double absoluteLongitude = 0.0;
        if (lon != null) {
            absoluteLongitude = Math.abs(Double.parseDouble(lon));
        }

        if (StringUtils.isNullOrWhitespacesAny(new String[]{lon}) || (absoluteLongitude <= 1)) {
            Location location = getPhoneLastBestLocation(activity);

            if (location != null) {
                lon = Double.toString(location.getLongitude());
            }
        }
        return lon;
    }

    void addDataInLogData(String data) {
        Log.d(TAG, "ELd: addDataInLogData: data: " + data);
        MainMenuActivity.logDataArrayList.add(data + "\n" +
                DateUtils.getNowYyyyMmDdHhmmss());
    }

    //    July 05, 2022 -   We need to update the current truck log detail driving hours
//    And we will use these values to update the total driving hours from truck log header
    public void calculateCurrentDrivingHours() {
        Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: ");

        String eventCode = getMostRecentEventCodeAsStringForEventType(EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS);
        Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: eventCode: " + eventCode);
        if (!eventCode.equalsIgnoreCase("3")) {
//            July 05, 2022 -   if the current state is not driving then we should not continue
//            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: return 1");
            return;
        }

        Double eventStartedTimeStamp = getMostRecentEventCreationTime(EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, null);
        Double startTime = eventStartedTimeStamp * 1000;
        Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: eventStartedTimeStamp: " + eventStartedTimeStamp + " startTime: " + startTime);

//        July 05, 2022
//        Todo =>   We need to fix the case when we start a new truck log detail after midnight
        Double duration = 0.0;
        if (eventStartedTimeStamp > 0) {
            duration = System.currentTimeMillis() - startTime;
        }
        Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: duration: " + duration);
        String truckLogHeaderMobileRecordId;
        TruckLogHeader truckLogHeader = getOpenTruckLogHeader();
        Log.d(TAG, "parse: hours: endCycle: onClick: truckLogHeader: " + truckLogHeader);

        if (truckLogHeader == null) {
//            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: return 2");
            return;
        } else {
            truckLogHeaderMobileRecordId = truckLogHeader.MobileRecordId;
        }


        TruckLogDetail truckLogDetail = getTruckLogDetailForToday(truckLogHeaderMobileRecordId, null);
        Log.d(TAG, "parse: hours: endCycle: onClick: truckLogDetail: " + truckLogDetail);

        if (truckLogDetail == null) {
//            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: return 3");
            return;
        }

        Double previousDrivingTime = 0.0;
        if (truckLogDetail.DrivingHours != null && !truckLogDetail.DrivingHours.isEmpty()) {
            previousDrivingTime = Double.parseDouble(truckLogDetail.DrivingHours);
        }

        Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: previousDrivingTime: " + previousDrivingTime);
        previousDrivingTime = previousDrivingTime + ((duration / 1000 / 60 / 60));
        truckLogDetail.DrivingHours = previousDrivingTime + "";
        Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: truckLogDetail.DrivingHours: " + truckLogDetail.DrivingHours);

//        July 05, 2022 -   Update value in database
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("drivingHours", previousDrivingTime + "");
            values.put("sent", "0");

//            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: previousDrivingTime: " + previousDrivingTime + "and update truck log detail");
            db.update("trucklogdetail", values, "id=" + truckLogDetail.Id);
        } catch (Throwable throwable) {
            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: throwable: " + throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            lock.unlock();
        }

        // July 06, 2022 Dragos - we need to mark the truck log header that needs to be uploaded
        lock.lock();

        try {
            ContentValues values = new ContentValues();

            values.put("sent", "0");
//            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: mark truck log header for uploading: " + truckLogHeader.Id);

            db.update("trucklogheader", values, "id=" + truckLogHeader.Id);
        } catch (Throwable throwable) {
            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: throwable2: " + throwable.getMessage());
            throwable.printStackTrace();
        } finally {
            lock.unlock();
        }
    }

    //    July 08, 2022 -   We need a function to return truck log detail for today
    public TruckLogDetail getCurrentTruckLOgDetail() {
        TruckLogHeader truckLogHeader = getOpenTruckLogHeader();
        Log.d(TAG, "parse: hours: endCycle: onClick: truckLogHeader: " + truckLogHeader);

        if (truckLogHeader == null) {
//            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: return 2");
            return null;
        }

        TruckLogDetail truckLogDetail = getTruckLogDetailForToday(truckLogHeader.MobileRecordId, "");
        Log.d(TAG, "parse: hours: endCycle: onClick: truckLogDetail: " + truckLogDetail);

        if (truckLogDetail == null) {
//            Log.d(TAG, "parse: hours: calculateCurrentDrivingHours: return 3");
            return null;
        }

        return truckLogDetail;
    }


    public int getNearestLoveDistance() {
        return nearestLoveDistance;
    }

    public void setNearestLoveDistance(int nearestLoveDistance) {
        this.nearestLoveDistance = nearestLoveDistance;
    }

    public int getNearestPilotDistance() {
        return nearestPilotDistance;
    }

    public void setNearestPilotDistance(int nearestPilotDistance) {
        this.nearestPilotDistance = nearestPilotDistance;
    }

    public int getNearestTADistance() {
        return nearestTADistance;
    }

    public void setNearestTADistance(int nearestTADistance) {
        this.nearestTADistance = nearestTADistance;
    }

    public int getNearestRestAreaDistance() {
        return nearestRestAreaDistance;
    }

    public void setNearestRestAreaDistance(int nearestRestAreaDistance) {
        this.nearestRestAreaDistance = nearestRestAreaDistance;
    }
}
