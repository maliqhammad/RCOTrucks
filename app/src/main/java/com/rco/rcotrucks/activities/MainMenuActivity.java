package com.rco.rcotrucks.activities;

import android.Manifest;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.junrar.Junrar;
import com.github.junrar.exception.RarException;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.orhanobut.hawk.Hawk;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.dashboard.DashboardFragment;
import com.rco.rcotrucks.activities.drive.DriveFragmentBase;
import com.rco.rcotrucks.activities.drive.DriveDiagnosticListFragment;
import com.rco.rcotrucks.activities.drive.DriveFragmentPhone;
import com.rco.rcotrucks.activities.drive.DriveFragmentTablet;
import com.rco.rcotrucks.activities.drive.DriveMalfunctionListFragment;
import com.rco.rcotrucks.activities.dvir.DvirListFragment;
import com.rco.rcotrucks.activities.forms.FormsFragment;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.FuelReceiptListFragment;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.ReceiptFragment;
import com.rco.rcotrucks.activities.fuelreceipts.model.FuelReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.ifta.BusHelperIfta;
import com.rco.rcotrucks.activities.ifta.IftaFragment;
import com.rco.rcotrucks.activities.logbook.LogBookFragments;
import com.rco.rcotrucks.activities.pretrip.PreTripListFragment;
import com.rco.rcotrucks.activities.settings.SettingsFragment;
import com.rco.rcotrucks.activities.streaming.CameraStreamingFragment;
import com.rco.rcotrucks.activities.truck.TruckFragment;
import com.rco.rcotrucks.adapters.MainNavigationAdapter;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.TruckEldContentLine;
import com.rco.rcotrucks.businesslogic.TruckLogContentLine;
import com.rco.rcotrucks.businesslogic.Tuple;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.TrailerLog;
import com.rco.rcotrucks.businesslogic.rms.TruckEldDetail;
import com.rco.rcotrucks.businesslogic.rms.TruckEldHeader;
import com.rco.rcotrucks.businesslogic.rms.TruckLogDetail;
import com.rco.rcotrucks.businesslogic.rms.TruckLogHeader;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.dialog.DialogEldCheck;
import com.rco.rcotrucks.dialog.SynStatusDialog;
import com.rco.rcotrucks.fragments.FeedBack;
import com.rco.rcotrucks.fragments.Help;
import com.rco.rcotrucks.interfaces.ListenFromActivity;
import com.rco.rcotrucks.model.PretripModel;
import com.rco.rcotrucks.utils.ConnectionMonitor;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.SessionManagement;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.TimerUtils;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;
import com.rco.rcotrucks.utils.zip.Decompress;
import com.rco.rcotrucks.utils.zip.DecompressFast;
import com.rco.rcotrucks.utils.zip.FileDownloadService;
import com.rco.rcotrucks.utils.zip.UnZip;
import com.rco.rcotrucks.utils.zip.UnZipFileUtils;
import com.wwdablu.soumya.wzip.WZip;
import com.wwdablu.soumya.wzip.WZipCallback;
//import com.wwdablu.soumya.wzip.WZip;
//import com.wwdablu.soumya.wzip.WZipCallback;

import net.lingala.zip4j.progress.ProgressMonitor;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

public class MainMenuActivity extends AppCompatActivity implements View.OnClickListener, SensorEventListener, ConnectionMonitor.Network {

    private static final String TAG = MainMenuActivity.class.getSimpleName();
    SessionManagement sessionManagement;

    public BusinessRules rules = BusinessRules.instance();
    private SensorManager sensorManager;
    private DrawerLayout drawerLayout;
    NavigationView navigationView;
    View headerView;
    private RecyclerView navigationRecyclerView;

    ConstraintLayout clLogin, logoutLayout, loadingPanel, syncReceiptLayout;
    public ImageView icMenu, toolbarAddButton, toolbarAddButtonPretrip, disconnectELD, drawerIconInsideNavigation;
    private TextView mainTitle, btnMalfunction, btnDiagnostics, signedInUsername, deleteFromAppbar, leftSideMiddlePointHeadingTitle, saveFromAppbar,
            displayCurrentTime, syncReceiptFromMobile, editPretrip, deleteMultipleEntries;
//    , emailPretrip

    public ConnectionMonitor connectionMonitor;
    private SyncRecordsTask syncRecords;
    private EldLocalDataProcessor eldProcessor;
    private EldBleListener eldBleListener;
    private EldTruckLogLocalDataProcessor truckLogProcessor;
    User user;
    public static ArrayList<String> logDataArrayList = new ArrayList<>();
    //2022.08.16 we need to be able to turn on / off logging all the data
    boolean logDataAllTheTime = true;

    int corePoolSize = 60, maximumPoolSize = 80, keepAliveTime = 10;
    BlockingQueue<Runnable> workQueue;
    public static Executor threadPoolExecutor;
    boolean recordDiagosticAndMalfuntions = false, isEldNotConnectedDialogAppeared = false, isTablet = false, isLoadingShown = false;
    EditText hideKeyboardET;
    public ListenFromActivity activityListener;
    String currentDisplayTime = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isTablet = getResources().getBoolean(R.bool.isTablet);
        setContentView(R.layout.drawer_layout);
        Log.d(TAG, "onCreate: ");

        keepsScreenOn();
        initializeHawk();
        setupConnectionMonitor();
        setIds();
        initialize();
        initUserInfo();
        setListeners();
//        syncRecordsAndEldProcessor();
        initEldActionBarFlags();

        NightModeAutoUpdateTask nightModeAutoUpdateTask = new NightModeAutoUpdateTask();
        nightModeAutoUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        updateTimeOnEachSecond();
    }


    @Override
    public void onResume() {
        try {
            super.onResume();

            rules.setDeviceId(this);
//             rules.loadDynamicDashboard(this, (LinearLayout) findViewById(R.id.activity_dashboard), currentOrientation);

            List<Tuple<Integer, Integer>> navigationMenu = rules.getNavigationMenus();
            setupNavigationView(navigationMenu);
        } catch (Throwable throwable) {
            if (throwable != null) throwable.printStackTrace();
        }
    }


    void keepsScreenOn() {
        Log.d(TAG, "keepsScreenOn: ");
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    void initializeHawk() {
        Log.d(TAG, "initializeHawk: ");
        Hawk.init(this).build();
    }

    void setupConnectionMonitor() {
        Log.d(TAG, "setupConnectionMonitor: ");
        connectionMonitor = new ConnectionMonitor(this, (ConnectivityManager) getSystemService(CONNECTIVITY_SERVICE));
        connectionMonitor.registerNetworkCallback();
    }


    void setIds() {
        Log.d(TAG, "setIds: ");
        drawerLayout = findViewById(R.id.drawerLayout);
        navigationView = findViewById(R.id.navigationView);
        headerView = navigationView.getHeaderView(0);
        drawerIconInsideNavigation = headerView.findViewById(R.id.drawerIconInsideNavigation);
        signedInUsername = headerView.findViewById(R.id.signedInUsername);
        navigationRecyclerView = headerView.findViewById(R.id.navigationRecyclerview);
        logoutLayout = headerView.findViewById(R.id.logoutLayout);


        icMenu = findViewById(R.id.ic_menu);
        clLogin = findViewById(R.id.cl_login);
        mainTitle = findViewById(R.id.main_title);
        toolbarAddButton = findViewById(R.id.toolbar_add_button);
        toolbarAddButtonPretrip = findViewById(R.id.toolbar_add_button_pretrip);
        disconnectELD = findViewById(R.id.disconnect_eld);
        btnMalfunction = findViewById(R.id.btn_malfunction);
        btnDiagnostics = findViewById(R.id.btn_diagnostics);
        loadingPanel = findViewById(R.id.loading_panel);
        syncReceiptLayout = findViewById(R.id.sync_receipt_layout);
        deleteMultipleEntries = findViewById(R.id.delete_multiple_receipts);
        deleteFromAppbar = findViewById(R.id.main_app_bar_delete);
        leftSideMiddlePointHeadingTitle = findViewById(R.id.left_side_title);
        saveFromAppbar = findViewById(R.id.main_app_bar_save);
        syncReceiptFromMobile = findViewById(R.id.sync_mobile);
        editPretrip = findViewById(R.id.edit_pretrip);
//        emailPretrip = findViewById(R.id.email_pretrip);


        hideKeyboardET = findViewById(R.id.hideKeyboardET);
        displayCurrentTime = findViewById(R.id.displayCurrentTime);
    }

    void initialize() {
        Log.d(TAG, "initialize: isDeviceTablet: " + getResources().getBoolean(R.bool.isTablet));

        workQueue = new LinkedBlockingQueue<Runnable>(maximumPoolSize);
        threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);
        sessionManagement = new SessionManagement(MainMenuActivity.this);

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        syncRecords = new SyncRecordsTask();

//        May 26, 2022  -   We moved the ble connection in a different thread
//        eldBleListener = new EldBleListener();

//        Sep 14, 2022  -   First check if we have the bluetooth permission or not
//        Log.d(TAG, "initialize: checkBlueToothPermissionsGranted: " + checkBlueToothPermissionsGranted());
        if (checkBlueToothPermissionsGranted()) {
            syncRecordsAndEldProcessor();
            checkLocationPermission();
        } else {
            invokeBluetoothPermissions();
        }


//        May 05, 2022  -    We should start the eld processor and truck log processor only after the syn is done
//        eldProcessor = new EldLocalDataProcessor();
//        truckLogProcessor = new EldTruckLogLocalDataProcessor();


        rules.setLastCtx(this);
//        isTablet = getResources().getBoolean(R.bool.isTablet);
    }

    void setListeners() {
        Log.d(TAG, "setListeners: ");
        disconnectELD.setOnClickListener(this);
        sensorManager.registerListener(this, sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);

        drawerIconInsideNavigation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openOrCloseDrawer();
                UiUtils.closeKeyboard(hideKeyboardET);
            }
        });

        logoutLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (sessionManagement.isLoggedIn()) {
                    sessionManagement.logoutUser();

                    rules.logoutDriver();
                    Intent intent = new Intent(MainMenuActivity.this, LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            }
        });

    }

    void syncRecordsAndEldProcessor() {
//        rules.setLastCtx(this);
//        Log.d(TAG, "syncRecordsAndEldProcessor: ");
//        syncRecords.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        eldProcessor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        truckLogProcessor.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "syncRecordsAndEldProcessor: syncRecords: " + syncRecords);
        Log.d(TAG, "syncRecordsAndEldProcessor: threadPoolExecutor: " + threadPoolExecutor);
        syncRecords.executeOnExecutor(threadPoolExecutor);

//        May 26, 2022  -   We moved the ble connection in a different thread
        eldBleListener = new EldBleListener();
        eldBleListener.executeOnExecutor(threadPoolExecutor);

//        May 05, 2022  -   We should run it after sync completes
//        eldProcessor.executeOnExecutor(threadPoolExecutor);
//        truckLogProcessor.executeOnExecutor(threadPoolExecutor);
    }

    public static final int BLUETOOTH_PERMISSION_REQUEST_CODE = 1234;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.d(TAG, "LP: onRequestPermissionsResult: requestCode: " + requestCode);

        if (requestCode == BusinessRules.MY_PERMISSIONS_REQUEST_LOCATION) {
//            Log.d(TAG, "onRequestPermissionsResult: ACCESS_COARSE_LOCATION permission granted");
            setMainFragment();
        }


        if (requestCode == BLUETOOTH_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0) {
                boolean isBluetoothPermissionGranted = true;
                for (int i = 0; i < grantResults.length; i++) {
//                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
//                        Log.d(TAG, "onRequestPermissionsResult: Please enable permission");
                        isBluetoothPermissionGranted = false;
                    }
                }

                Log.d(TAG, "onRequestPermissionsResult: isBluetoothPermissionGranted: " + isBluetoothPermissionGranted);
                if (isBluetoothPermissionGranted) {
                    syncRecordsAndEldProcessor();
                    checkLocationPermission();
                } else {
                    checkLocationPermission();
                }
            }
        }

    }

    void setMainFragment() {
//        Dec 12, 2022  -   We are setting the app bar for drive screen when ever we load drive screen
        setAppBarForReceipts("Drive");

        if (!rules.existsPendingSyncItem()) {
            if (rules.existsUserRight("Mobile-DisplayTrucks"))
                loadMainFragment(new TruckFragment());
            else {
                Log.d(TAG, "setMainFragment: DriveFragmentTablet: ");
                loadMainFragment(getResources().getBoolean(R.bool.isTablet) ? new DriveFragmentTablet() : new DriveFragmentPhone());
            }
        }
    }


    @Override
    public void onNetworkLost() {
        Log.d(TAG, "IC: ConnectionMonitor: onNetworkLost: ");
        if (rules.getPhoneLastBestLocation(MainMenuActivity.this) != null) {
            addDataInLogData("onNetworkLost: coordinates: " + rules.getPhoneLastBestLocation(MainMenuActivity.this).getLatitude() + ", " + rules.getPhoneLastBestLocation(MainMenuActivity.this).getLongitude());
        }

//        May 23, 2022  -   This function is enough to call if there is no internet then it will shown a message
//        "No Internet connection"
//        UiUtils.isOnline(MainMenuActivity.this,  getResources().getString(R.string.no_internet_connection));
//            Snackbar snackBar = Snackbar.make(findViewById(R.id.content_frame), R.string.error_lost_Connection, Snackbar.LENGTH_LONG);
//            snackBar.getView().setBackgroundColor(ContextCompat.getColor(this, R.color.colorAccent));
//            snackBar.show();

        if (activityListener != null) {
            activityListener.onNetworkLost();
        }
    }

    @Override
    public void onNetworkAvailable() {
        Log.d(TAG, "IC: ConnectionMonitor: onNetworkAvailable: ");

        if (activityListener != null) {
            activityListener.onNetworkAvailable();
        }


        if (rules.getPhoneLastBestLocation(MainMenuActivity.this) != null) {
            addDataInLogData("onNetworkAvailable: coordinates: " + rules.getPhoneLastBestLocation(MainMenuActivity.this).getLatitude() + ", " + rules.getPhoneLastBestLocation(MainMenuActivity.this).getLongitude());
        }
    }

    public void onClick(View view) {
        Log.d(TAG, "nav: onClick: ");
//        try {
        int resId;

        if (view instanceof ImageView) {
            Object tag = view.getTag();
            Log.d(TAG, "onClick: tag: display: " + view.getDisplay());
            resId = tag != null ? (Integer) tag : view.getId();
        } else resId = view.getId();

        clearToolBar();

        switch (resId) {
//                case R.drawable.icon_driversettings:
            case R.drawable.icon_drive_new:
                setAppBarForReceipts("Drive");
//                boolean isTablet = false;
//                isTablet = getResources().getBoolean(R.bool.isTablet);
                Log.d(TAG, "onClick: DriveFragmentTablet: ");
                loadMainFragment(isTablet ? new DriveFragmentTablet() : new DriveFragmentPhone());
                break;

            case R.drawable.icon_driversettings_new:
                setAppBarForReceipts("Settings");
                loadMainFragment(new SettingsFragment());
                break;

            case R.drawable.icon_forms_new:
                loadMainFragment(new FormsFragment());
                break;

            case R.drawable.icon_dvir_new:
                setAppBarForReceipts("Pretrip");
                loadMainFragment(new DvirListFragment());
                break;

            case R.drawable.ic_baseline_checklist_24:
                setAppBarForReceipts("PreTrip Update");
                loadMainFragment(new PreTripListFragment());
                break;

            case R.drawable.icon_fuel_new:
                loadMainFragment(new FuelReceiptListFragment());
                break;

            case R.drawable.ic_baseline_receipt:
                setAppBarForReceipts("Receipts");
                loadMainFragment(new ReceiptFragment());
                break;

            case R.drawable.ic_ifta:
                loadMainFragment(new IftaFragment());
                break;

            case R.drawable.icon_logbook_new:
                setAppBarForReceipts("Logbook");
                loadMainFragment(new LogBookFragments());
                break;

            case R.drawable.icon_gauge:
                loadMainFragment(new DashboardFragment());
                break;

            case R.drawable.icon_truck:
                loadMainFragment(new TruckFragment());
                break;

            case R.drawable.camera:
                loadMainFragment(new CameraStreamingFragment());
                break;

            case R.drawable.icon_sync_new:
                openSyncStatus();
                break;

            case R.id.disconnect_eld:
//                    April 12, 2022    -   Today we should show the isConnected popup
                if (rules.isBleConnected()) showDisconnectEldPopup();
                else showDeviceConnectedPopup();

                if (!rules.isBleConnected()) {
                    rules.setLastCtx(MainMenuActivity.this);
                    rules.startBleListener();
                    updateEldActionBarFlags();
                }
                break;

            case R.drawable.ic_baseline_message_24:
                setAppBarForReceipts("FeedBack");
                loadMainFragment(new FeedBack());
                break;

            case R.drawable.ic_baseline_help_24:
                setAppBarForReceipts("Help");
                loadMainFragment(new Help());
                break;
        }

//            April 21, 2022    -
//            mainToolbar.setBackgroundColor(getResources().getColor(R.color.black));
//        } catch (Throwable throwable) {
//            if (throwable != null)
//                throwable.printStackTrace();
//        }
    }

    @Override
    protected void onDestroy() {
        try {
            super.onDestroy();

            connectionMonitor.unregisterNetworkCallback();

        } catch (Throwable throwable) {
            if (throwable != null) throwable.printStackTrace();
        }
    }

    @Override
    public void onBackPressed() {
//        Log.d(TAG, "onBackPressed: ");
        try {
            if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
                getSupportFragmentManager().popBackStack();
            } else if (getSupportFragmentManager().getFragments().size() > 0) {
                Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);

                if (fragment instanceof DriveFragmentBase) {
                    ((DriveFragmentBase) fragment).handleBackPressed();
                } else {
                    Utils.exitApplication(this);
                }
            } else Utils.exitApplication(this);
        } catch (Throwable throwable) {
            Utils.exitApplication(this);

            if (throwable != null) throwable.printStackTrace();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        Log.d(TAG, "onSensorChanged: ");
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float accelerometerX = event.values[0];
            float accelerometerY = event.values[1];
            float accelerometerZ = event.values[2];

            rules.setLastKnownAccelerometerValues(accelerometerX, accelerometerY, accelerometerZ);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
//        Log.d(TAG, "onAccuracyChanged: ");
    }

    //region Sync Tasks

    public class EldLocalDataProcessor extends AsyncTask<String, Integer, Integer> {
        //      March 15, 2022  -   We change syncIntervalSecs from 5 seconds to 1 seconds
//        "5 seconds is a lot for speed, like in 5 seconds a car can reach up to 100 miles"
        private int syncIntervalSecs = 1;
        private Throwable throwable;


        @Override
        protected void onPreExecute() {
//            Log.d(TAG, "Comparison: EldLocalDataProcessor: onPreExecute: EldLocalDataProcessor: ");
            addDataInLogData("EldLocalDataProcessor: onPreExecute: ");
        }

        @Override
        protected Integer doInBackground(String... strings) {
//            Log.d(TAG, "Comparison: EldLocalDataProcessor: doInBackground: BreakEndedCheck: ");
//            Log.d(TAG, "TestingDriving: EldLocalDataProcessor: doInBackground: ");
            addDataInLogData("EldLocalDataProcessor: doInBackground: ");
//            try {
            Integer reportingPeriod = null;
            long intervalCounter = 0;

            do {
//                May 26, 2022  -   We created a seprate a thread that for listener ble connection
//                startBleListenerIfNotConnected();

                // Generic default events collection - Every 5secs

//                        Log.d(TAG, "TestingDriving: EldLocalDataProcessor: doInBackground: isBleConnected: " + rules.isBleConnected());
                if (rules.isBleConnected()) {

//                            Log.d(TAG, "TestingDriving: EldLocalDataProcessor: doInBackground: odometer: " + rules.getBleParameter("Odometer"));
                    rules.setOdometer(rules.getBleParameter("Odometer"));
                }

//                Log.d(TAG, "doInBackground: rules.isDriverSkillLevelBeginner: " + (rules.isDriverSkillLevelBeginner()));
//                Log.d(TAG, "doInBackground: rules.isDriverSkillLevelBeginner: && " + (rules.isDriverSkillLevelBeginner() && rules.isDriverLoggedIn()));
                if (rules.needToRecordContinuousDriving())
                    rules.recordContinuousDrivingEvent(MainMenuActivity.this, rules.isDriverInDutyStatus());

//                        March 21, 2022    -   Moved record Driving event to the end
//                        if (!rules.isCurrentEldStatusDriving() && rules.isVehicleMoving()) {
//                            Log.d(TAG, "doInBackground: dutyEventCode: " + rules.getDutyEventCode());
////                            March 17, 2022    -   Make sure we are not recording multiple driving events
//                            if (rules.getDutyEventCode() != BusinessRules.EventCode.DRIVING) {
//                                Log.d(TAG, "TestingDriving: doInBackground: setEldEventToDriving: " + rules.getDutyEventCode());
//                                rules.setEldEventToDriving(MainMenuActivity.this);
//                                rules.setDrivingStatus("Driving");
//                            }
//                        }

                if (rules.isCurrentEldStatusDriving() && rules.isVehicleSpeed0()) {
                    rules.countEldIdleTime();

                    if (rules.isVehicleStoppedForLongerThan5Mins()) publishProgress(2);

                    if (!rules.isDriverLoggedIn() && rules.isVehicleSpeedGraterThan0() && !wasLoginRequiredPopupTriggered)
                        publishProgress(3);

//                            March 18, 2022    -   Why we need to record here OnDuty or Off Duty
//                            if (rules.isDriverLoggedIn() && rules.isDriverSkillLevelBeginner()) {
//                                if (rules.isEngineOn()) {
////                                    March 15, 2022    -   Should check if the previous state is On Duty or not
//                                    if (rules.getDutyEventCode() != BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
//                                        rules.setEldEventToOnDuty(MainMenuActivity.this);
//                                    }
//                                } else {
////                                    March 15, 2022    -   Should check if the previous state is Off Duty or not
//                                    if (rules.getDutyEventCode() != BusinessRules.EventCode.OFF_DUTY) {
//                                        rules.setEldEventToOffDuty(MainMenuActivity.this);
//                                    }
//                                }
//                            }
                }

//                        April 12, 2022    -   We should reset the idle time counter if we are not driving or if
//                        speed!=0
//                        else if (!rules.isVehicleSpeed0())
//                            rules.resetEldIdelTimeCounter();
                if (!rules.isCurrentEldStatusDriving() || !rules.isVehicleSpeed0()) {
                    rules.resetEldIdelTimeCounter();
                }

                boolean forcefullyReloadStatus = false;
                if (rules.existsVehiclePowerSwitchEvent()) {
                    addDataInLogData("EldBleListener: rules.existsVehiclePowerSwitchEvent(): " + rules.existsVehiclePowerSwitchEvent());
                    rules.persistVehiclePowerSwitchEvent();

//                  March 21, 2022    -   Before recording Off Duty or On Duty according to Driver skill
//                  we should record power up or shut down event

                    boolean isPowerOn = rules.recordVehiclePowerSwitchEvent(MainMenuActivity.this, rules.isDriverInDutyStatus());
                    addDataInLogData("EldBleListener: isPowerOn: " + isPowerOn);

//                  March 31, 2022    -   We should check if the isEngineOn is not null
//                  This should be changed!
//                  July 18, 2022 -   We should record automatic onDuty or automatic OffDuty only when we are logged in
                    if (rules.isDriverSkillLevelBeginner() && rules.isDriverLoggedIn()) {
//                  July 14, 2022 -   We should force A reset for the duty event duty code
//                  Actually We experience onDuty event after power up even
                        rules.resetDutyEventCode();
                        if (rules.isEngineOn()) {
//                  March 15, 2022    -   Should check if the previous state is On Duty or not
                            if (rules.getDutyEventCode() != BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
                                rules.setEldEventToOnDuty(MainMenuActivity.this);
                                if (activityListener != null) {
                                    activityListener.notifyShiftChanged();
                                }
                                forcefullyReloadStatus = true;
                            }
                        } else {
//                  March 15, 2022    -   Should check if the previous state is Off Duty or not
                            if (rules.getDutyEventCode() != BusinessRules.EventCode.OFF_DUTY) {
//                                Log.d(TAG, "parse: hours: doInBackground: calculateCurrentDrivingHours: ");
                                rules.calculateCurrentDrivingHours();
                                rules.setEldEventToOffDuty(MainMenuActivity.this);
                                forcefullyReloadStatus = true;
                            }
                        }
                    }
                }

//                June 23, 2022 -   We need to make sure that the driving status is properly updated
                if (forcefullyReloadStatus) {
                    if (activityListener != null) {
//                        June 27, 2022 -   We should forcefully reload the duty event
                        rules.resetDutyEventCode();
                        activityListener.updateBottomDrivingStatus();
                        forcefullyReloadStatus = false;
                    }
                }

//                        March 21, 2022    -   Moved Recording Driving event here
//                        To ensure the sequence
                boolean isVehicleMoving = rules.isVehicleMoving();
//                String username = sessionManagement.getKeyLogin();
//                if (username != null && (username.equalsIgnoreCase("naeem"))) {
//                    isVehicleMoving = true;
//                }
                if (!rules.isCurrentEldStatusDriving() && isVehicleMoving) {

//                    Aug 02, 2022  -   Added just to double check that we did not recorded a previous driving
//                    (There is a strange situation that driving is being called after 10 milliseconds)
                    rules.resetDutyEventCode();
//                            Log.d(TAG, "TestingDriving: doInBackground: dutyEventCode: " + rules.getDutyEventCode());
//                            March 17, 2022    -   Make sure we are not recording multiple driving events
                    if (rules.getDutyEventCode() != BusinessRules.EventCode.DRIVING) {
//                                Log.d(TAG, "TestingDriving: doInBackground: setEldEventToDriving: " + rules.getDutyEventCode());
//                                Log.d(TAG, "TestingDriving: doInBackground: isEldTesting: "+rules.isEldTesting());

//                            March 31, 2022    -   We should check if truck log exists for today and if not then we should create them
                        if (!rules.existsTruckLogRecords()) {
                            rules.createTruckLogRecords(MainMenuActivity.this);
                        }

//                        April 20, 2022    -   Check here if we are in break then we should close the break state
                        rules.setBreakStarted(false);
//                        Log.d(TAG, "doInBackground: BreakEndedCheck: rules.isBreakStarted: " + rules.isBreakStarted);
                        BusinessRules.EventCode mostRecentBreakEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.BREAK, sessionManagement);
//                        Log.d(TAG, "BreakEndedCheck: doInBackground: mostRecentBreakEventCode: isBreakStarted: " + (mostRecentBreakEventCode == BusinessRules.EventCode.ON_BREAK_STARTED));
//                        Log.d(TAG, "BreakEndedCheck: mostRecentBreakEventCode: " + mostRecentBreakEventCode);

//                        TO DO April 21, 2022  -   Why detecting Engine Moving - when its not moving
//                        Log.d(TAG, "BreakEndedCheck: doInBackground: rules.isEngineOn(): " + rules.isEngineOn());
                        if ((mostRecentBreakEventCode == BusinessRules.EventCode.ON_BREAK_STARTED) && rules.isEngineOn()) {
//                        if ((mostRecentBreakEventCode == BusinessRules.EventCode.ON_BREAK_STARTED)) {
//                            Log.d(TAG, "BreakEndedCheck: doInBackground: setEldEventTo Break Ended By Driving");
                            rules.setEldEvent(MainMenuActivity.this, BusinessRules.EventCode.ON_BREAK_ENDED, "Break Ended By Driving", "Break Ended By Driving");
                        }


//                        May 05, 2022  -   We should show these annotations values only for testing
                        String annotation = "";
                        if (rules.isEldTesting()) {
                            annotation = "AnnotationMainActivity";
                        }

                        rules.setEldEventToDriving(MainMenuActivity.this, annotation);
                        addDataInLogData("recordDriving: ");
                        rules.setDrivingStatus("Driving");

                        if (activityListener != null) {
//                        Aug 02, 2022 -   We should forcefully reload the duty event (Driving was not reloaded fast enough)
                            rules.resetDutyEventCode();
                            activityListener.updateBottomDrivingStatus();
                        }

                    }


                }

                String vin = rules.getBleParameter("VIN");
//                        Log.d(TAG, "DeviceAndELD: OnBleData: value: "+value.get);

//                        Log.d(TAG, "TestingDriving: DeviceAndELD: doInBackground: latitude: " + rules.getBleParameter("Latitude") + ", " + rules.getBleParameter("Longitude"));

//                Log.d(TAG, "generateAndStoreNewEldAuthenticationValue: doInBackground: vin: " + vin);
//                Log.d(TAG, "generateAndStoreNewEldAuthenticationValue: doInBackground: existsVinEldEventForToday: " + rules.existsVinEldEventForToday());
                if (!StringUtils.isNullOrWhitespaces(vin) && !rules.existsVinEldEventForToday()) {

//                            Removed throwable exception
                    rules.recordVinEldEvent(MainMenuActivity.this, vin);
                    rules.generateAndStoreNewEldAuthenticationValue(vin);
                }

                // Malfunction & Diagnostics events collection (if any) - Every 5secs
                if (rules.isDebugMode()) {
                    recordDiagosticAndMalfuntions = true;
                }

//                        April 18, 2022    -   Maybe we should increase intervalCounter value
                if (rules.isBleConnected() && intervalCounter > 3 && recordDiagosticAndMalfuntions) {
//                    July 18, 2022 -   We should record diagostic only when we are logged in
                    if (rules.isDriverLoggedIn()) {

                        rules.recordEngineSynchronizationDiagnostic(MainMenuActivity.this);
                        rules.recordPositionDiagnostic(MainMenuActivity.this);
                    }


                    if (false) { // After each RMS data transfer
                        rules.recordDataTransferDiagnostic(MainMenuActivity.this);
                        rules.recordDataTransferMalfunction(MainMenuActivity.this);
                    }

                    rules.recordEngineSynchronizationMalfunction(MainMenuActivity.this);
                    rules.recordPositionMalfunction(MainMenuActivity.this);
                }

//  April 15, 2022  -   We should record the timing malfunction even if the ble is not connected
                if (recordDiagosticAndMalfuntions) {
                    rules.recordTimingComplianceMalfunction(MainMenuActivity.this);
                }

                publishProgress(0);

                BusinessRules.logDebug("ELD Local Data Processor looped " + intervalCounter);

                publishProgress(1);
                try {
                    Thread.sleep(syncIntervalSecs * 1000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
                intervalCounter++;

            } while (true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            Log.d(TAG, "Comparison: EldLocalDataProcessor: onProgressUpdate: ");
            try {
                if (values[0] == 2) {
                    showEldIdleState();
                    return;
                }

                if (values[0] == 3) {
//                    Log.d(TAG, "onProgressUpdate: ");
                    showEldDrivingAndLoggedInPopup();
                    return;
                }

                if (values[0] == -1) {
                    if (throwable != null) {
                        throwable.printStackTrace();

                        UiUtils.showExclamationDialog(MainMenuActivity.this, "Sync Error", throwable.toString() + "\n" + StringUtils.dumpArray(throwable.getStackTrace()) + "\n" + rules.getBleParametersAsStr());
                    }
                }

                updateEldActionBarFlags();
            } catch (Throwable throwable) {
                addDataInLogData("EldLocalDataProcessor: onProgressUpdate: outsideCatch: throwable: " + throwable.getMessage());
                Log.d(TAG, "onProgressUpdate: throwable: " + throwable.getMessage());
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
//            Log.d(TAG, "Comparison: EldLocalDataProcessor: onPostExecute: ");
            try {

                /*a.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (throwable != null)
                            UiUtils.showToast(a, t.toString() + "\\" + StringUtils.dumpArray(t.getStackTrace()));
                    }
                });*/

            } catch (Throwable throwable) {
//                logDataArrayList.add("onPostExecute: onProgressUpdate: outsideCatch: throwable: " + throwable.getMessage() + "\n" +
//                        DateUtils.getNowYyyyMmDdHhmmss());
                addDataInLogData("onPostExecute: onProgressUpdate: outsideCatch: throwable: " + throwable.getMessage());

                if (throwable != null) throwable.printStackTrace();
            }
        }
    }


    public class SyncRecordsTask extends AsyncTask<String, Integer, Integer> {
        private BusHelperIfta rulesIfta = new BusHelperIfta(RecordRulesHelper.getDb());
        private int syncIntervalSecs = 1;
        private Throwable throwable;

        private boolean isSyncingUp = false;
        private boolean isSyncingDown = false;
        private boolean isSyncDownWithErrors = false;

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "SyncRecordsTask: onPreExecute: ");
            try {
                isSyncingUp = true;
                isSyncingDown = true;
                showLoadingLayout(true);
            } catch (Throwable throwable) {
                showLoadingLayout(false);
                if (throwable != null) throwable.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Log.d(TAG, "SyncRecordsTask: doInBackground: ");
            long intervalCounter = 0;
            int progressCounter = 1;
            do {
//                Log.d(TAG, "SyncRecordsTask: doInBackground: do: started: ");
                try {
                    boolean isFirstIteration = intervalCounter == 0;

                    //#region Sync Up
                    isSyncingUp = true;
                    //boolean isTimeToSyncUp = intervalCounter % (rules.isProductionMode() ? 15 * 60 : 2 * 60) == 0; // Every 15 mins for prod, every 2 min for debug
                    boolean isTimeToSyncUp = intervalCounter % 60 == 0;
//                    boolean isTimeToSyncUp = intervalCounter % rules.getDriverReportingPeriod() == 0;

                    if (isFirstIteration || isTimeToSyncUp) {
                        syncEldEvents();
                        syncTruckLogs();
                        syncTruckElds();
//                        May 26, 2022  -   Sync trailer logs
                        syncTrailerLogs();


//                        Jan 12, 2022  Sync Pretrip
                        Log.d(TAG, "doInBackground: save: SyncRecordsTask: ");
                        sendPendingPreTripEntriesBeforeSync();  // this function will upload all the pending prerips


//                        syncTollReceipts();
//STARTS
//      Dec 15, 2022    -   We are now using the sync from fuel receipts to sync all the records not in MainMenuActivity
//                        Nov 25, 2022  -   Sync newly added toll receipts (Upload)
                        syncTollReceipts();
//                        Dec 05, 2022  -   Sync newly added fuel receipts (Upload)
                        syncFuelReceipts();
//ENDS


//                        Jan 24, 2022  -   syncCommonRecords was adding extra entries too so for Now for DVIR I have added
//                        some new sendPendingPreTripEntriesBeforeSync
//                      Record types implemented with generalized record code (DVIR, Fuel Receipt, IFTA Event)
                        progressCounter = rules.syncCommonRecords(progressCounter, isSyncingDown(), isSyncingUp());
//                      publishProgress(progressCounter);
                    }

                    rulesIfta.runCheckIftaEventTask(MainMenuActivity.this); // IFTA Events sync up
                    isSyncingUp = false;

                    //#endregion

                    //#region Sync Down

                    if (isFirstIteration) {
                        isSyncingDown = true;

                        progressCounter = 1;  // Todo: Fernando - why is progressCounter initialized here?  Don't we want to include the upsync work load? -RAN 11/19/2021

                        rules.loadUserTrucksTrailers();

//                        Log.d(TAG, "SyncRecordsTask: doInBackground: " + rules.getPhoneLastBestLocation(MainMenuActivity.this));
//                        if (rules.getPhoneLastBestLocation(MainMenuActivity.this) != null) {
//                            Location phoneLastBestLocation = rules.getPhoneLastBestLocation(MainMenuActivity.this);
//                            Log.d(TAG, "SyncRecordsTask: doInBackground: phoneLastBestLocation: " + phoneLastBestLocation.getLatitude() + "," + phoneLastBestLocation.getLongitude());
//                            String locationDescription = rules.getLocalizationStr(MainMenuActivity.this,
//                                    "" + phoneLastBestLocation.getLatitude(), "" + phoneLastBestLocation.getLongitude());
//                            Log.d(TAG, "SyncRecordsTask: doInBackground: locationDescription: " + locationDescription);
//                        }
                        rules.loadCities(MainMenuActivity.this);

                        if (rules.existsPendingSyncItem()) publishProgress(progressCounter += 5);

                        rules.syncDriver(rules.getAuthenticatedUser().getRmsUserId());

                        if (rules.existsPendingSyncItem()) publishProgress(progressCounter += 5);

                        rules.syncTrucks();
                        rules.syncTrailers();

/*      Dec 15, 2022    -   We are now using the sync from fuel receipts to sync all the records not in MainMenuActivity     */

//                        jan 12, 2022  -   get saved record into our local db from server
                        rules.syncPretrip();

//                        Nov 28, 2022  -   get saved record into our local db from server
                        rules.syncTollRoadReceipt();
//                        Dec 05, 2022  -   As a replica of toll receipts - working on fuel receipts - get saved record from server into our local db
                        rules.syncFuelReceipt();

                        if (rules.existsPendingSyncItem()) publishProgress(progressCounter += 5);

                        rules.syncRules();
                        rules.syncSettings();

                        if (rules.existsPendingSyncItem()) publishProgress(progressCounter += 5);


                        rules.syncTruckEldEvents();
                        rules.syncTruckLogs();
                        rules.syncTruckElds();
                        rules.syncTrailerLogs();

                        if (rules.existsPendingSyncItem()) publishProgress(progressCounter += 5);


//                        Nov 10, 2022 -   Previously we were using 4 different files to get each station type but now
//                        we have a single file so there is no need to call each method
//                        (separate so each call will cost us time so in just a single call we are getting all the content)
                        rules.syncAssets(MainMenuActivity.this);
//                        Nov 11, 2022 -   previous methods removed from business rules (refactoring)
//                        Nov 10, 2022 -       syncAssets() is adding all the required content for each type
//                        rules.syncPilotAssets();
//                        rules.syncPilotAssetsUpdate();

                        if (rules.existsPendingSyncItem()) publishProgress(progressCounter += 5);

//                        Nov 11, 2022 -   previous methods removed from business rules (refactoring)
//                        Nov 10, 2022 -       syncAssets() is adding all the required content for each type
//                        rules.syncTaAssets();
//                        rules.syncTaAssetsUpdate(MainMenuActivity.this);
                        rules.syncRoadsList(MainMenuActivity.this);

                        if (rules.existsPendingSyncItem()) publishProgress(progressCounter += 5);

//                        Nov 11, 2022 -   previous methods removed from business rules (refactoring)
//                        Nov 10, 2022 -       syncAssets() is adding all the required content for each type
//                        rules.syncLoveAssets();
//                        rules.syncLoveAssetsUpdate(MainMenuActivity.this);

                        if (rules.existsPendingSyncItem()) publishProgress(progressCounter += 5);

//                        Nov 11, 2022 -   previous methods removed from business rules (refactoring)
//                        Nov 10, 2022 -       syncAssets() is adding all the required content for each type
//                        rules.syncRestAreasAssets();
//                        rules.syncRestAreasAssetsUpdate(MainMenuActivity.this);

                        publishProgress(progressCounter += 5);

                        // Forms call

                        if (false) {
                            // TODO: Please check in RMS if there is new data for the sync items below, and only if there is clear the given table and resync, otherwise make your code instead to load the data from the table into memory

                            rules.clearEvaluationTable();

                            rules.syncEvaluations(Crms.FORM_Medical_Form_Signature);
                            publishProgress(progressCounter += 5);

                            rules.syncEvaluations(Crms.FORM_Driver_Skill_Performance_Evaluation_Form_Signature);
                            publishProgress(progressCounter += 5);

                            rules.syncEvaluations(Crms.FORM_Driver_Employment_Record_Form_Signature);
                            publishProgress(progressCounter += 5);

                            rules.syncEvaluations(Crms.FORM_Driver_Application_Header_Signature);
                            publishProgress(progressCounter += 5);

                            rules.syncEvaluations(Crms.FORM_Driver_Violations_Header_Signature);
                            publishProgress(progressCounter += 5);


                        }

                        publishProgress(100);

                        isSyncingDown = false;
                        isSyncDownWithErrors = false;
                    }

                    //#endregion

//                    Log.d(TAG, "SyncRecordsTask: doInBackground: do started: ended: ");
                    Thread.sleep(syncIntervalSecs * 1000);
                    intervalCounter++;
                } catch (Throwable throwable) {
                    if (throwable != null) throwable.printStackTrace();

                    showLoadingLayout(false);
                    publishProgress(-1);

                    return BusinessRules.OK;
                }
            } while (true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "SyncRecordsTask: onProgressUpdate: ");
            try {
                int percent = values[0];
//                Log.d(TAG, "onProgressUpdate: percent: " + percent);
                if (percent == -1) {
                    showLoadingLayout(false);

//                    Sep 02, 2022  -   Added just to test STARTS
                    if (!UiUtils.isOnline(MainMenuActivity.this)) {
                        if (!isEldDriveFragmentLoaded()) {
                            Log.d(TAG, "onProgressUpdate: DriveFragmentTablet: ");
                            loadMainFragment(getResources().getBoolean(R.bool.isTablet) ? new DriveFragmentTablet() : new DriveFragmentPhone());

//                            Sep 05, 2022  -   We should record the events also when we don't have internet connection
                            truckLogProcessor = new EldTruckLogLocalDataProcessor();
                            truckLogProcessor.executeOnExecutor(threadPoolExecutor);
                        }
                    }
//                    Sep 02, 2022  -   Added just to test ENDS
//                    UiUtils.showToast(MainMenuActivity.this, "Sync error " + throwable.toString() + " - " + StringUtils.dumpArray(throwable.getStackTrace()));


                } else if (percent == -2) {
                    showLoadingLayout(false);
                    isSyncDownWithErrors = true;
                    UiUtils.showToast(MainMenuActivity.this, "Sync complete with errors");
                } else if (percent < 100) {
                    UiUtils.showToast(MainMenuActivity.this, "Syncing (" + percent + "%), please wait...");
                } else {
//                    Log.d(TAG, "onProgressUpdate: activityListener: " + activityListener);
                    if (activityListener != null) {
                        activityListener.notifySyncComplete();
                    }
                    recordDiagosticAndMalfuntions = true;
                    showLoadingLayout(false);
                    UiUtils.showToast(MainMenuActivity.this, "Sync complete");

//                    May 05, 2022  -   We should EldLocalDataProcessor and EldTruckLogLocalDataProcessor only after the syn completes
//                    eldProcessor = new EldLocalDataProcessor();
//                    eldProcessor.executeOnExecutor(threadPoolExecutor);

                    truckLogProcessor = new EldTruckLogLocalDataProcessor();
                    truckLogProcessor.executeOnExecutor(threadPoolExecutor);
                    if (!isEldDriveFragmentLoaded()) {
                        Log.d(TAG, "onProgressUpdate: DriveFragmentTablet: ");
                        loadMainFragment(getResources().getBoolean(R.bool.isTablet) ? new DriveFragmentTablet() : new DriveFragmentPhone());
                    }

                    Log.d(TAG, "unzip: onProgressUpdate: progress finished 100%");
//                    Hidden for now Jan 11, 2022
//                    Jan 05, 2022  -   we will initiate unzipping only when other sync finished fully (100%)
//                    initiateUnZipping();
                }
            } catch (Throwable throwable) {
                Log.d(TAG, "onProgressUpdate: throwable: " + throwable.getMessage());
                showLoadingLayout(false);
//                Log.d(TAG, "SyncRecordsTask: onProgressUpdate: throwable: " + throwable.getMessage());
//                if (throwable != null)
//                    throwable.printStackTrace();
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "SyncRecordsTask: onPostExecute: result: " + result.toString());
            Log.d(TAG, "SyncRecordsTask: onPostExecute: isSyncingUp: " + isSyncingUp);
        }

        // Helpers
        public boolean isSyncing() {
            Log.d(TAG, "SyncRecordsTask: isSyncing: ");
            return isSyncingUp() || isSyncingDown();
        }

        public boolean isSyncingUp() {
            Log.d(TAG, "SyncRecordsTask: isSyncingUp: ");
            return isSyncingUp;
        }

        public boolean isSyncingDown() {
            Log.d(TAG, "SyncRecordsTask: isSyncingDown: ");
            return isSyncingDown;
        }

        public boolean isSyncDownWithErrors() {
            Log.d(TAG, "SyncRecordsTask: isSyncDownWithErrors: ");
            return isSyncDownWithErrors;
        }
    }

    private void syncEldEvents() {
        Log.d(TAG, "syncEldEvents: ");
        BusinessRules.logDebug("syncEldEvents");

        ArrayList<EldEvent> result = rules.getPendingEldEvents();

        if (result == null) return;

        for (EldEvent e : result) {
            try {
                Rms.setTruckEldEvent(e); // TODO: Check later to take the I from the Operator field and replace with a O
                rules.setEldEventToSent(e);
            } catch (Throwable t2) {
                if (t2 != null) t2.printStackTrace();

                BusinessRules.logDebug("Error while syncing up to RMS setTruckEldEvent: " + e.toString());
            }
        }
    }

    private void syncTruckLogs() {
//        Log.d(TAG, "syncTruckLogs: ");
        BusinessRules.logDebug("syncTruckLogs");

        ArrayList<TruckLogHeader> result = rules.getPendingTruckLogEntries();

        // Header and Detail sync

        if (result != null) for (TruckLogHeader e : result) {
            try {
                rules.sendPendingTruckLogEntries(e);
                rules.setTruckLogHeaderEntryToSent(e);

                ArrayList<TruckLogDetail> details = e.getTruckLogDetails();

                if (details != null && details.size() > 0)
                    rules.setTruckLogDetailEntryToSent(details);
            } catch (Throwable throwable) {

                Log.d(TAG, "syncTruckLogs: throwable: " + throwable.getMessage());
                if (throwable != null) throwable.printStackTrace();

                BusinessRules.logDebug("Error while syncing up to RMS setTruckEldEvent: " + e.toString());
            }
        }

        // Content lines

        try {
            if (rules.isDebugMode() || rules.isWiFiConnection(MainMenuActivity.this)) {
                int groupTries = 0;

                while (groupTries < 2) {
                    ArrayList<TruckLogContentLine> contentLines = rules.getPendingTruckLogContentLines();

//                    Log.d(TAG, "syncTruckLogs: contentLines: " + contentLines);
                    if (contentLines != null) {

//                        Log.d(TAG, "syncTruckLogs: contentLines: size: " + contentLines.size());
                    }
                    if (contentLines != null && contentLines.size() > 0) try {
                        Rms.appendRecordContentToTruckLog(contentLines);
                        rules.setContentLinesToSent(contentLines);
                    } catch (Throwable throwable) {
                        Log.d(TAG, "syncTruckLogs: throwable: " + throwable.getMessage());
                        if (throwable != null) throwable.printStackTrace();

                        BusinessRules.logDebug("Error while syncing content lines up to RMS");
                    }

                    groupTries++;
                }
            }
        } catch (Throwable throwable) {
            if (throwable != null) throwable.printStackTrace();

            BusinessRules.logDebug("Error while syncing content lines up to RMS: " + throwable.toString());
        }
    }


    private void syncTrailerLogs() {
//        Log.d(TAG, "syncTrailerLogs: ");
//        Log.d(TAG, "syncTruckLogs: ");
        BusinessRules.logDebug("syncTrailerLogs: ");

        ArrayList<TrailerLog> result = rules.getPendingTrailerLogEntries();

        // Header and Detail sync

        if (result != null) for (TrailerLog e : result) {
            try {
                rules.sendPendingTrailerLogEntries(e);
                rules.setTrailerLogEntryToSent(e);
            } catch (Throwable throwable) {

                Log.d(TAG, "syncTrailerLogs: throwable: " + throwable.getMessage());
                if (throwable != null) throwable.printStackTrace();

                BusinessRules.logDebug("syncTrailerLogs: Error while syncing up to RMS setTrailerEldEvent: " + e.toString());
            }
        }
    }

    private void sendPendingPreTripEntriesBeforeSync() {
        Log.d(TAG, "save: sendPendingPreTripEntriesBeforeSync: ");
        BusinessRules.logDebug("sendPendingPreTripEntriesBeforeSync: ");
        ArrayList<PretripModel> result = rules.getPendingPretripEntries();
        if (result != null) Log.d(TAG, "save: sendPendingPreTripEntriesBeforeSync: result: " + result.size());
        for (PretripModel pretripModelEntry : result) {
            try {
                Log.d(TAG, "save: sendPendingPreTripEntriesBeforeSync: sendPendingPretriptEntries: called: ");
                rules.sendPendingPretripEntries(pretripModelEntry);

                Log.d(TAG, "save: sendPendingPreTripEntriesBeforeSync: setPretrip: called: ");
            } catch (Throwable throwable) {
                Log.d(TAG, "save: sendPendingPreTripEntriesBeforeSync: throwable: " + throwable.getMessage());
                BusinessRules.logDebug("sendPendingPreTripEntriesBeforeSync: Error while syncing up to RMS setTrailerEldEvent: " + pretripModelEntry.toString());
            }
        }
    }


    private void syncTollReceipts() {
        Log.d(TAG, "save: syncTollReceipts: ");
        BusinessRules.logDebug("syncTollReceiptLogs: ");
        ArrayList<TollReceiptModel> result = rules.getPendingTollReceiptEntries();
        if (result != null) Log.d(TAG, "save: syncTollReceipts: result: " + result.size());

        for (TollReceiptModel tollReceiptModelEntry : result) {
            try {
                Log.d(TAG, "save: syncTollReceipts: sendPendingTollReceiptEntries: called: ");
                rules.sendPendingTollReceiptEntries(tollReceiptModelEntry);
                Log.d(TAG, "save: syncTollReceipts: setTollReceiptEntryToSent: called: ");
//                    Nov 25, 2022  -
//                    rules.setTollReceiptEntryToSent(tollReceiptModelEntry);
            } catch (Throwable throwable) {
                Log.d(TAG, "save: syncToll: throwable: " + throwable.getMessage());
                BusinessRules.logDebug("syncTollReceipts: Error while syncing up to RMS setTrailerEldEvent: " + tollReceiptModelEntry.toString());
            }
        }
    }

    private void syncFuelReceipts() {
        Log.d(TAG, "save: syncFuelReceipts: ");
        BusinessRules.logDebug("syncFuelReceipts: ");
        ArrayList<FuelReceiptModel> result = rules.getPendingFuelReceiptEntries();
        if (result != null) Log.d(TAG, "save: syncTollReceipts: result: " + result.size());
        for (FuelReceiptModel fuelReceiptModelEntry : result) {
            try {
                Log.d(TAG, "save: syncTollReceipts: sendPendingTollReceiptEntries: called: ");
                rules.sendPendingFuelReceiptEntries(fuelReceiptModelEntry);
                Log.d(TAG, "save: syncTollReceipts: setTollReceiptEntryToSent: called: ");
//                    Nov 25, 2022  -
//                    rules.setTollReceiptEntryToSent(tollReceiptModelEntry);
            } catch (Throwable throwable) {
                Log.d(TAG, "save: syncToll: throwable: " + throwable.getMessage());
                BusinessRules.logDebug("syncTollReceipts: Error while syncing up to RMS setTrailerEldEvent: " + fuelReceiptModelEntry.toString());
            }
        }
    }


    private void syncTruckElds() {
//        Log.d(TAG, "syncTruckElds: ");
        BusinessRules.logDebug("syncTruckElds");

        ArrayList<TruckEldHeader> result = rules.getPendingTruckEldEntries();

        // Header and Detail sync

        if (result != null) for (TruckEldHeader e : result) {
            try {
                rules.sendPendingTruckEldEntries(e);
                rules.setTruckEldHeaderEntryToSent(e);

                ArrayList<TruckEldDetail> details = e.getTruckEldDetails();

                if (details != null && details.size() > 0)
                    rules.setTruckEldDetailEntryToSent(details);
            } catch (Throwable throwable) {
                if (throwable != null) throwable.printStackTrace();

                BusinessRules.logDebug("Error while syncing up to RMS setTruckEldEvent: " + e.toString());
            }
        }

        // Content lines

        try {
            int groupTries = 0;

            while (groupTries < 2) {
                ArrayList<TruckEldContentLine> contentLines = rules.getPendingTruckEldContentLines();

                if (contentLines != null && contentLines.size() > 0) try {
                    Rms.appendRecordContentToTruckEld(contentLines);
                    rules.setEldContentLinesToSent(contentLines);
                } catch (Throwable throwable) {
                    if (throwable != null) throwable.printStackTrace();

                    BusinessRules.logDebug("Error while syncing content lines up to RMS");
                }

                groupTries++;
            }
        } catch (Throwable throwable) {
            if (throwable != null) throwable.printStackTrace();

            BusinessRules.logDebug("Error while syncing content lines up to RMS: " + throwable.toString());
        }
    }

    //endregion

    //region Helpers

    private TimerUtils eldIdleTimer;
    private boolean isEldIdleStateOpen = false;
    private boolean wasLoginRequiredPopupTriggered = false;
    private boolean isActionBarTitleSet = false;

    private boolean isEldDriveFragmentLoaded() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        List<Fragment> fragments = fragmentManager.getFragments();

        if (fragments == null) return false;

        for (Fragment f : fragments)
            if (f instanceof DriveFragmentTablet || f instanceof DriveFragmentPhone) return true;

        return false;
    }

    public void showDeviceConnectedPopup() {
//        Log.d(TAG, "runThreadForFewSeconds: showDeviceConnectedPopup: DialogManager.d: " + DialogManager.d);
//        if (DialogManager.d != null && DialogManager.d.isShowing())
//            return;

        DialogManager.d = UiUtils.showCustomLayoutDialog(MainMenuActivity.this, R.layout.dialog_automaticbleconnect);
//        Log.d(TAG, "runThreadForFewSeconds: showDeviceConnectedPopup: isShowing: " + DialogManager.d.isShowing());

//        Log.d(TAG, "runThreadForFewSeconds: showDeviceConnectedPopup: showCustomLayoutDialog: " + DialogManager.d.isShowing());
        DialogManager.d.findViewById(R.id.bt_notification_yes).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setEldEvent(eventCode, notificationSubTitle, etAnnotation);
                //rules.setLastCtx(DriveActivity2.this);

                rules.setLastCtx(MainMenuActivity.this);
                if (!rules.isBleConnected()) rules.startBleListener();

//                April 12, 2022    -   We should be able to set the device connected to yes especially in
//                the case when the device is not working
                rules.forceBleToConnectedState();
                updateEldActionBarFlags();
                if (activityListener != null) {
                    activityListener.notifyBleConnection();
                }

                if (DialogManager.d != null) DialogManager.d.dismiss();
            }
        });

        DialogManager.d.findViewById(R.id.bt_notification_no).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rules.forceBleToDisconnectedState();
                updateEldActionBarFlags();

                if (DialogManager.d != null) DialogManager.d.dismiss();
            }
        });
    }

    private void showDisconnectEldPopup() {
        if (DialogManager.d != null && DialogManager.d.isShowing()) return;

        DialogManager.d = UiUtils.showBooleanDialog(this, "ELD", android.R.drawable.ic_dialog_info, "Yes", "No", "Disconnect from the ELD device?", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    rules.forceBleToDisconnectedState();
                } catch (Throwable throwable) {
                    if (throwable != null) throwable.printStackTrace();
                }
            }
        });
    }

    private void showEldDrivingAndLoggedInPopup() {
        if (DialogManager.d != null && DialogManager.d.isShowing()) return;

        wasLoginRequiredPopupTriggered = true;

//        Log.d(TAG, "showEldDrivingAndLoggedInPopup: testingDriving: eventCode: " + BusinessRules.EventCode.ON_DUTY_NOT_DRIVING);
        rules.setEldEvent(MainMenuActivity.this, BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, BusinessRules.EventCode.ON_DUTY_NOT_DRIVING, BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.ASSUMED_FROM_UNIDENTIFIED_DRIVER_PROFILE, "On Duty Not Driving", "Driving", "", "");

//        Log.d("onProgressUpdate:", "showEldDrivingAndLoggedInPopup: ");
        DialogManager.d = UiUtils.showExclamationDialog(MainMenuActivity.this, "Login Required", "Please stop and Login!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface d, int which) {
                if (d != null) d.dismiss();
            }
        });
    }

    private void showEldIdleState() {

        if (DialogManager.d != null && DialogManager.d.isShowing()) return;

        DialogManager.d = UiUtils.showCustomLayoutDialog(MainMenuActivity.this, R.layout.dialog_continuedriving);

        DialogManager.d.findViewById(R.id.bt_notification_driving).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    eldIdleTimer.stopTimer();
                    rules.resetEldIdelTimeCounter();

                    if (DialogManager.d != null) DialogManager.d.dismiss();
                } catch (Throwable throwable) {
                    if (throwable != null) throwable.printStackTrace();
                }
            }
        });

        DialogManager.d.findViewById(R.id.bt_notification_change).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    eldIdleTimer.stopTimer();
                    rules.resetEldIdelTimeCounter();

                    if (DialogManager.d != null) DialogManager.d.dismiss();

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    List<Fragment> fragments = fragmentManager.getFragments();

                    if (fragments != null) for (Fragment f : fragments) {
                        if (f instanceof DriveFragmentBase)
                            if (!((DriveFragmentBase) f).isStatusDialogShown())
                                ((DriveFragmentBase) f).setupStatusPopup();
                    }
                } catch (Throwable throwable) {
                    if (throwable != null) throwable.printStackTrace();
                }
            }
        });

        isEldIdleStateOpen = true;

        eldIdleTimer = new TimerUtils();
        eldIdleTimer.startTimer(60000, 1000, new Runnable() {
            public void run() {
                if (DialogManager.d != null) DialogManager.d.dismiss();

//                March 18, 2022    -   Added extra check
                if (rules.getDutyEventCode() != BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
                    Log.d(TAG, "TestingDriving: run: setEldEvent: ON_DUTY_NOT_DRIVING");
                    rules.setEldEvent(MainMenuActivity.this, BusinessRules.EventCode.ON_DUTY_NOT_DRIVING, "On Duty", "MainMenuActivity: eldIdleTimer");
                    rules.resetEldIdelTimeCounter();
                    eldIdleTimer.stopTimer();
                }
            }
        });
    }

    private void loadMainFragment(Fragment fragment) {
        Log.d(TAG, "onClick: loadMainFragment: fragment: ");
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.content_frame, fragment).commit();
    }

    public void updateEldActionBarFlags() {
        Log.d(TAG, "bleConnection: updateEldActionBarFlags: ");
        int redColor = getResources().getColor(R.color.red);
        int whiteColor = getResources().getColor(R.color.white);

        Log.d(TAG, "bleConnection: updateEldActionBarFlags: isBleConnected: " + rules.isBleConnected());
        if (rules.isBleConnected()) {
            disconnectELD.setImageResource(R.drawable.ic_eld_connect);

            Log.d(TAG, "bleConnection: updateEldActionBarFlags: rules.isDriverLoggedIn(): " + rules.isDriverLoggedIn());
            if (rules.isDriverLoggedIn()) {
                disconnectELD.setVisibility(View.GONE);

                btnMalfunction.setVisibility(View.VISIBLE);
                btnDiagnostics.setVisibility(View.VISIBLE);

                Log.d(TAG, "bleConnection: updateEldActionBarFlags: rules.existsActiveMalfunction(): " + rules.existsActiveMalfunction());

//              Dec 23, 2022    -   We should use the existing malfunction and diagnostic for the last 7 days
//                btnMalfunction.setTextColor(rules.existsActiveMalfunction() ? redColor : whiteColor);
//                btnDiagnostics.setTextColor(rules.existsActiveDiagnostic() ? redColor : whiteColor);
                boolean isMalfunction = rules.existsLatestMalfunctions();
                boolean isDiagnostic = rules.existsLatestDiagnostics();
                btnMalfunction.setTextColor(isMalfunction ? redColor : whiteColor);
                btnDiagnostics.setTextColor(isDiagnostic ? redColor : whiteColor);


            } else {
                disconnectELD.setVisibility(View.GONE);
                btnMalfunction.setVisibility(View.VISIBLE);
                btnDiagnostics.setVisibility(View.GONE);

                Log.d(TAG, "bleConnection: updateEldActionBarFlags: else: rules.existsActiveMalfunction(): " + rules.existsActiveMalfunction());
//              Dec 23, 2022    -   We should use the existing malfunction and diagnostic for the last 7 days
//                btnMalfunction.setTextColor(rules.existsActiveMalfunction() ? redColor : whiteColor);
                boolean isMalfunction = rules.existsLatestMalfunctions();
                btnMalfunction.setTextColor(isMalfunction ? redColor : whiteColor);
            }

//                June 15, 2022 -   The automatic event when DM is shown
            if (activityListener != null) {
                activityListener.notifyBleConnection();
            }
        } else {
//            Log.d(TAG, "updateEldActionBarFlags: isBleConnected: else: ");
            disconnectELD.setImageResource(R.drawable.ic_eld_disconnect);

            disconnectELD.setVisibility(View.VISIBLE);
            btnMalfunction.setVisibility(View.GONE);
            btnDiagnostics.setVisibility(View.GONE);

//            Log.d(TAG, "updateEldActionBarFlags: disconnectELD: " + disconnectELD.getVisibility());
            BusinessRules.EventCode mostRecentDutyEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, sessionManagement);
//            Aug 10, 2022  -   Roy told in the email on Aug 10, show the icon in red if we are not connected to internet
            boolean showRedAllTheTimeWhenELDNotConnected = true;
            if (mostRecentDutyEventCode == BusinessRules.EventCode.DRIVING || (showRedAllTheTimeWhenELDNotConnected && rules.isDriverLoggedIn())) {
                disconnectELD.setColorFilter(ContextCompat.getColor(MainMenuActivity.this, R.color.red));
            } else {
                disconnectELD.setColorFilter(ContextCompat.getColor(MainMenuActivity.this, R.color.white));
            }
        }
    }

    private void initEldActionBarFlags() {
//        Log.d(TAG, "initEldActionBarFlags: ");
        btnDiagnostics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!rules.existsLatestDiagnostics()) {
                        UiUtils.showToast(MainMenuActivity.this, "No diagnostic recorded for the last 7 days.");
                        return;
                    }

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().add(R.id.content_frame, new DriveDiagnosticListFragment()).addToBackStack("DiagnosticList").commit();
                } catch (Throwable throwable) {
                    Log.d(TAG, "onClick: Throwable: " + throwable.getMessage());
                    if (throwable != null) throwable.printStackTrace();
                }
            }
        });

        btnMalfunction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (!rules.existsLatestMalfunctions()) {
                        UiUtils.showToast(MainMenuActivity.this, "No malfunction recorded for the last 7 days.");
                        return;
                    }

                    FragmentManager fragmentManager = getSupportFragmentManager();
                    fragmentManager.beginTransaction().add(R.id.content_frame, new DriveMalfunctionListFragment()).addToBackStack("MalfunctionList").commit();
                } catch (Throwable throwable) {
                    if (throwable != null) throwable.printStackTrace();
                }
            }
        });
    }


    private void initUserInfo() {
//        Log.d(TAG, "initUserInfo: ");
        user = rules.getAuthenticatedUser();
        if (user != null) {
            signedInUsername.setText(user.getFirstName());

        }
    }

    void checkLocationPermission() {
        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            setMainFragment();
        } else {
            invokeLocationPermissions();
        }
    }

    void invokeLocationPermissions() {
        Log.d(TAG, "LP: invokeLocationPermissions: ");
//        Sep 21, 2022  -   Suggestion (Unnecessary; SDK_INT is always >= 24)
//        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
//            rules.invokeLocationPermissions(this);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, rules.MY_PERMISSIONS_REQUEST_LOCATION);
        rules.setLastCtx(this);
    }


    public void setActionBarTitle(String value) {
        isActionBarTitleSet = !StringUtils.isNullOrWhitespaces(value) && !value.contains("mph");

        if (isActionBarTitleSet) {
            disconnectELD.setVisibility(View.INVISIBLE);
            btnMalfunction.setVisibility(View.GONE);
            btnDiagnostics.setVisibility(View.GONE);
            findViewById(R.id.login).setVisibility(View.GONE);
        }

        mainTitle.setText(value == null ? "" : value);
    }

    public void clearActionBarTitle() {
        isActionBarTitleSet = false;

        disconnectELD.setVisibility(View.INVISIBLE);
        mainTitle.setText("");

        updateEldActionBarFlags();
    }

    private void clearToolBar() {
        mainTitle.setText("");
        toolbarAddButton.setVisibility(View.GONE);
    }

    public void hideMoreIcon() {
        clLogin.setVisibility(View.GONE);
    }

    private void setupNavigationView(List<Tuple<Integer, Integer>> menus) {

        icMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    UiUtils.closeKeyboard(hideKeyboardET);
                    if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawers();
                    else drawerLayout.openDrawer(GravityCompat.START);
                } catch (Throwable throwable) {
                    Log.d(TAG, "onClick: throwable: " + throwable.getMessage());
                }

            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        navigationRecyclerView.setLayoutManager(linearLayoutManager);

        MainNavigationAdapter navigationAdapter = new MainNavigationAdapter(menus, new MainNavigationAdapter.ClickListener() {
            @Override
            public void onItemClick(View view) {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) drawerLayout.closeDrawers();

//                Sep 20, 2022  -   Unable to open menu options when running offline so comment it out for now
//                if (syncRecords.isSyncingDown()) {
//                    UiUtils.showToast(MainMenuActivity.this, "Still synchronizing data, please wait...");
//                    return;
//                } else if (syncRecords.isSyncDownWithErrors()) {
//                    UiUtils.showToast(MainMenuActivity.this, "Sync unable to complete due to errors");
//                    return;
//                }

                onClick(view);
            }
        });

        navigationRecyclerView.setAdapter(navigationAdapter);
    }

    public void openDriverFragment() {
        Log.d(TAG, "openDriverFragment: DriveFragmentTablet: ");
        if (!isEldDriveFragmentLoaded())
            loadMainFragment(getResources().getBoolean(R.bool.isTablet) ? new DriveFragmentTablet() : new DriveFragmentPhone());
    }

    //endregion
    void openOrCloseDrawer() {

        if (drawerLayout.isDrawerVisible(GravityCompat.START)) {

            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
    }


    public class EldTruckLogLocalDataProcessor extends AsyncTask<String, Integer, Integer> {
        //11.03.2022 ad to sync the truck log detail data
        private int syncIntervalSecs = 1;
        private Throwable throwable;
        private TruckEldDetail truckEldDetail;

        @Override
        protected void onPreExecute() {
//            Log.d(TAG, "Comparison: EldTruckLogLocalDataProcessor: onPreExecute: EldTruckLogLocalDataProcessor: ");
            Log.d(TAG, "EldTruckLogLocalDataProcessor: onPreExecute: ");

            if (user != null) {
                truckEldDetail = rules.getMostRecentEldDetail();
                if (truckEldDetail != null) {
//                    Log.d(TAG, "EldTruckLogLocalDataProcessor: onPreExecute: truckEldDetail: " + truckEldDetail.OdometerStart);
                }
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
//            Log.d(TAG, "Comparison: EldTruckLogLocalDataProcessor: doInBackground: ");
            try {
                Integer reportingPeriod = null;
                long intervalCounter = 0;

                do {

                    if (truckEldDetail == null) {
                        truckEldDetail = rules.getMostRecentEldDetail();
                    } else {
//                        Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: truckEldDetail: " + truckEldDetail.MobileRecordId);
                        rules.updateOdometerForTruckEldDetail(truckEldDetail, rules.getOdometer(), rules.getEngineHours());
                    }

//                    Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: odometer: " + rules.getOdometer());

                    try {
                        // Generic default events collection - Every 1secs

                        publishProgress(0);

                        // Collection of offline driving parameters (content lines) - Every reportingPeriod secs

                        reportingPeriod = rules.isDriverLoggedIn() && rules.existsReportingPeriod() ? rules.getDriverReportingPeriod() : 900; // If Reporting Period is null than collect samples only every 15mins

                        // 11.03.2022 boolean isTimeToCollectSample = intervalCounter % (reportingPeriod / syncIntervalSecs) == 0;
                        boolean isTimeToCollectSample = intervalCounter % reportingPeriod == 0;

//                        Log.d(TAG, "EldTruckLogLocalDataProcessor doInBackground: intervalCounter: " + intervalCounter + "reportingPeriod :" + reportingPeriod + "syncIntervalSecs: " + syncIntervalSecs);

                        Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: rules.isCurrentEldStatusDriving(): " + rules.isCurrentEldStatusDriving());
                        Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: isTimeToCollectSample: " + isTimeToCollectSample);
                        if (/*rules.isVehicleMoving()*/rules.isCurrentEldStatusDriving() && isTimeToCollectSample) {
//                        if (/*rules.isVehicleMoving()*/isTimeToCollectSample) {
                            // Truck Log content lines

                            try {
                                String[] objectIdType = rules.getTruckLogEntryForToday();
                                Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: objectIdType: " + objectIdType);

                                String truckLogDetailMobileRecordId = null;
                                if (objectIdType != null) {
                                    Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: objectIdType.length: " + objectIdType.length);
                                    for (int i = 0; i < objectIdType.length; i++) {
                                        if (i == 3) {
                                            truckLogDetailMobileRecordId = objectIdType[i];
                                        }
                                        Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: index: " + i + " And value: " + objectIdType[i]);
                                    }
                                }

                                if (objectIdType == null || objectIdType[0] == null || objectIdType[1] == null) {
                                    objectIdType = rules.findObjectIdTypeByMobileRecordId(objectIdType);

                                    if (objectIdType != null && !StringUtils.isNullOrWhitespaces(objectIdType[0]))
                                        rules.updateTruckLogEntryObjectIdType("detail", objectIdType[4], objectIdType[0], objectIdType[1]);
                                    Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: objectIdType: " + objectIdType);
                                }

                                if (objectIdType != null) {
                                    String objectId = objectIdType[0];
                                    String objectType = objectIdType[1];

//                                    Sep 05, 2022  -
                                    if (objectId != null && objectType != null) {
                                        rules.recordTruckLogDrivingParameters(objectId, objectType, "trucklog", MainMenuActivity.this, "Driving", "");
                                    }
                                } else {
                                    rules.recordTruckLogDrivingParameters("", "", "trucklog", MainMenuActivity.this, "Driving", truckLogDetailMobileRecordId);
                                }
                            } catch (Throwable throwable) {
                                if (throwable != null) throwable.printStackTrace();
                                Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: throwable: " + throwable.getMessage());
                                BusinessRules.logDebug("Error while recording truck log content lines: " + throwable.toString());
                            }
                        }
                        Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: ");

                        // Sleep & iterate

                        BusinessRules.logDebug("ELD Local Data Processor looped " + intervalCounter);

                        publishProgress(1);
                        Thread.sleep(syncIntervalSecs * 1000);
                        intervalCounter++;
                    } catch (Throwable throwable) {
                        Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: ");
                        if (throwable != null) throwable.printStackTrace();

                        publishProgress(-1);
                        return null;
                    }
                } while (true);
            } catch (Throwable throwable) {
                Log.d(TAG, "EldTruckLogLocalDataProcessor: doInBackground: throwable: " + throwable.getMessage());
                if (throwable != null) throwable.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            Log.d(TAG, "Comparison: EldTruckLogLocalDataProcessor: onProgressUpdate: ");
        }

        @Override
        protected void onPostExecute(Integer result) {
//            Log.d(TAG, "Comparison: EldTruckLogLocalDataProcessor: onPostExecute: ");
        }
    }

    void showToastMessage(String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainMenuActivity.this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }


    void showLoadingLayout(boolean showLoading) {
        Log.d(TAG, "unzip: showLoadingLayout: showLoading: " + showLoading);
//        April 26, 2022    -   We should perform the layout relevant changes only in UIThread
//        Otherwise we sometimes stuck with crash
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                isLoadingShown = showLoading;
                if (showLoading) {
                    loadingPanel.setVisibility(View.VISIBLE);
                } else {
                    loadingPanel.setVisibility(View.GONE);
                }
            }
        });
    }

    void openSyncStatus() {

        SynStatusDialog synStatusDialog = new SynStatusDialog(MainMenuActivity.this, new SynStatusDialog.PickupInterface() {
            @Override
            public void onYesPressed() {
                Toast.makeText(MainMenuActivity.this, "Yes", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelPressed() {
                Toast.makeText(MainMenuActivity.this, "No", Toast.LENGTH_SHORT).show();
            }
        });
        synStatusDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        synStatusDialog.show();
    }

    void addDataInLogData(String data) {
        if (logDataArrayList.size() > 2000) {
            // 2022.08.16 we should clear the data if the log has more than 2000 lines
            logDataArrayList = new ArrayList<>();
        }
        Log.d(TAG, "addDataInLogData: data: " + data);
        logDataArrayList.add(data + "\n" + DateUtils.getNowYyyyMmDdHhmmss());
    }

    void addDataInLogData(String data, boolean checkIsAlreadyAdded) {

        boolean isNewRecord = true;
        if (checkIsAlreadyAdded) {

            for (int i = 0; i < logDataArrayList.size(); i++) {
                if (logDataArrayList.get(i).contains(data)) {
                    isNewRecord = false;
                }
            }
        }

        if (isNewRecord) {
            logDataArrayList.add(data + "\n" + DateUtils.getNowYyyyMmDdHhmmss());
        }
    }


    //region Sync Tasks
    public class EldBleListener extends AsyncTask<String, Integer, Integer> {
        //      March 15, 2022  -   We change syncIntervalSecs from 5 seconds to 1 seconds
//        "5 seconds is a lot for speed, like in 5 seconds a car can reach up to 100 miles"
        private int syncIntervalSecs = 1;

        @Override
        protected void onPreExecute() {
//            Log.d(TAG, "EldBleListener: onPreExecute: ");
            addDataInLogData("EldBleListener: onPreExecute: ");
            rules.setLastCtx(MainMenuActivity.this);
        }

        @Override
        protected Integer doInBackground(String... strings) {
//            Log.d(TAG, "EldBleListener: doInBackground: ");

            do {
//                Log.d(TAG, "EldBleListener: doInBackground: do: ");
//                July 26, 2022 -   We should save the line just when the eld is not connected
                if (!rules.isBleConnected() || logDataAllTheTime) {
                    addDataInLogData("EldBleListener: doInBackground: do: ");
                }
                startBleListenerIfNotConnected();

                Log.d(TAG, "EldBleListener: doInBackground: rules.isBleConnected(): " + rules.isBleConnected());
                if (rules.isBleConnected()) {
//                    showToastMessage("Ble Connected.");
                    startEldProcessor();
                    syncIntervalSecs = 2;

                } else {
                    syncIntervalSecs = 1;
//                    May 30, 2022  -   Added below check so to show dialog only when sync completes.
                    if (recordDiagosticAndMalfuntions) {
//                        July 18, 2022 -   We commented(removed) this because roy said:
//                        "Please remove the dialog No ELD detected since the driver can see the red icon at the top and this is an extra button to press."
//                        showEldDialogNotAppeared();
                    }
                }

                try {
                    Thread.sleep(syncIntervalSecs * 1000);
                } catch (InterruptedException interruptedException) {
                    addDataInLogData("EldBleListener: doInBackground: InterruptedException: " + interruptedException.getMessage());
                    interruptedException.printStackTrace();
                }

            } while (true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
//            Log.d(TAG, "EldBleListener: onProgressUpdate: ");
        }

        @Override
        protected void onPostExecute(Integer result) {
//            Log.d(TAG, "EldBleListener: onPostExecute: ");
        }

        // Helpers

        private void startBleListenerIfNotConnected() {
//            Log.d(TAG, "EldBleListener: startBleListenerIfNotConnected: ");
//            Log.d(TAG, "initialize: ");

            if (!rules.isBleConnected()) {
//                Dec 20, 2022  -   Here in the implementation setting Last Ctx is important - it was commented so I believe that's why its not connecting with ELD because
//                we have done any changes here
//                I just uncomment it
                rules.setLastCtx(MainMenuActivity.this);
                rules.startBleListener();


//                Log.d(TAG, "EldBleListener: startBleListenerIfNotConnected: Connecting: " + DateUtils.getNowYyyyMmDdHhmmss());
                addDataInLogData("EldBleListener: startBleListenerIfNotConnected: Connecting:  ");

                try {
//                    Log.d(TAG, "EldBleListener: startBleListenerIfNotConnected: try: sleep 3 seconds");
//                    May 26, 2022  -   We change time to check is ble connected
//                    Thread.sleep(5 * 1000);
                    addDataInLogData("EldBleListener: startBleListenerIfNotConnected: sleep 3 seconds: ");
                    Thread.sleep(3 * 1000);
                } catch (InterruptedException interruptedException) {
//                    Log.d(TAG, "EldBleListener: startBleListenerIfNotConnected: interruptedException: with 3 seconds: but interruptedException: " + interruptedException.getMessage());
                    addDataInLogData("EldBleListener: startBleListenerIfNotConnected: InterruptedException: interruptedException: with 3 seconds: " + interruptedException.getMessage());

                }

                if (rules.isBleConnected()) {
//                    Log.d(TAG, "EldBleListener: startBleListenerIfNotConnected: isBleConnected: " + rules.isBleConnected());
                    addDataInLogData("EldBleListener: startBleListenerIfNotConnected: Connected: isBleConnected: " + rules.isBleConnected());

                } else {
                    try {
//                        Log.d(TAG, "EldBleListener: startBleListenerIfNotConnected: sleep 5 seconds");
//                    May 26, 2022  -   We change time to check is ble connected
//                        Thread.sleep(20 * 1000);
                        addDataInLogData("EldBleListener: startBleListenerIfNotConnected: sleep 5 seconds: ");
                        Thread.sleep(5 * 1000);
                    } catch (InterruptedException interruptedException) {
                        interruptedException.printStackTrace();
//                        Log.d(TAG, "EldBleListener: startBleListenerIfNotConnected: interruptedException: with 5 seconds: but interruptedException: " + interruptedException.getMessage());
                        addDataInLogData("EldBleListener: startBleListenerIfNotConnected: interruptedException: with 5 seconds: " + interruptedException.getMessage());
                    }
                }
            }
        }
    }

    void startEldProcessor() {
//        Log.d(TAG, "startEldProcessor: ");
//                July 26, 2022 -   We should save the line just when the eld is not connected
        if (!rules.isBleConnected() || logDataAllTheTime) {
            addDataInLogData("EldBleListener: startEldProcessor: ");
        }

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                // Stuff that updates the UI
//                July 26, 2022 -   We should save the line just when the eld is not connected
                if (!rules.isBleConnected() || logDataAllTheTime) {
                    addDataInLogData("EldBleListener: eldProcessor: " + eldProcessor);
                }
                if (eldProcessor == null) {
                    eldProcessor = new EldLocalDataProcessor();
                    eldProcessor.executeOnExecutor(threadPoolExecutor);
                }
            }
        });
    }

    //    May 30, 2022  -   We are showing this dialog only once when app starts and eld is not connected
    void showEldDialogNotAppeared() {
//        Log.d(TAG, "showEldDialogNotAppeared: ");
//        boolean isTablet = getResources().getBoolean(R.bool.isTablet);

        if (!isEldNotConnectedDialogAppeared) {
            isEldNotConnectedDialogAppeared = true;

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    DialogEldCheck dialogEldCheck = new DialogEldCheck(MainMenuActivity.this);
                    dialogEldCheck.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                    dialogEldCheck.show();
                }
            });
        }
    }

    public void setActivityListener(ListenFromActivity activityListener) {
//        Log.d(TAG, "setActivityListener: activityListener: " + activityListener);
        this.activityListener = activityListener;
    }

    boolean checkBlueToothPermissionsGranted() {
        Log.d(TAG, "checkBlueToothPermissionsGranted: ");
//        new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_SCAN,
//                Manifest.permission.BLUETOOTH_ADMIN};

        if (Build.VERSION.SDK_INT >= 31) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        } else {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) == PackageManager.PERMISSION_GRANTED && checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) == PackageManager.PERMISSION_GRANTED) {
                return true;
            }
        }

        return false;
    }

    public void invokeBluetoothPermissions() {
        Log.d(TAG, "invokeBluetoothPermissions: ");
        if (Build.VERSION.SDK_INT >= 31) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN, Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        } else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.BLUETOOTH, Manifest.permission.BLUETOOTH_ADMIN}, BLUETOOTH_PERMISSION_REQUEST_CODE);
        }
    }


    //region Tasks
    public class NightModeAutoUpdateTask extends AsyncTask<String, String, Integer> {
        private int syncIntervalSecsMapAutoUpdateTask = 1;
        private long intervalCounter = 1;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            Log.d(TAG, "NightModeAutoUpdateTask: onPreExecute: ");
            handleDayOrNightView();
        }

        @Override
        protected Integer doInBackground(String... params) {
//            Log.d(TAG, "NightModeAutoUpdateTask: doInBackground: params: " + params);
            do {

//            June 03, 2022 -   We should check if we should enable night mode or wait every 3 mints
//                boolean isTimeToUpdateDistances = intervalCounter == 0 || (intervalCounter % 180 == 0);
                boolean isTimeToUpdateDistances = (intervalCounter % 180 == 0);
//                Log.d(TAG, "handleDayOrNightView: NightModeAutoUpdateTask: doInBackground: isTimeToUpdateDistances: " + isTimeToUpdateDistances + " intervalCounter: " + intervalCounter);
                if (isTimeToUpdateDistances) {
                    handleDayOrNightView();
                }
                try {
                    Thread.sleep(syncIntervalSecsMapAutoUpdateTask * 1000);
                } catch (InterruptedException interruptedException) {
                    Log.d(TAG, "NightModeAutoUpdateTask: doInBackground: interruptedException: " + interruptedException.getMessage());
                }
                intervalCounter++;
            } while (true);

        }
    }


    private void handleDayOrNightView() {
//        Log.d(TAG, "NightModeAutoUpdateTask: handleDayOrNightView: ");
        if (rules.getPhoneLastBestLocation(MainMenuActivity.this) != null) {
            boolean isNight = rules.isItNight(new LatLng(rules.getPhoneLastBestLocation(MainMenuActivity.this).getLatitude(), rules.getPhoneLastBestLocation(MainMenuActivity.this).getLongitude()));
//            Log.d(TAG, "NightModeAutoUpdateTask: handleDayOrNightView: isNight: " + isNight);
//            Oct 31, 2022  -   We should apply either day or night theme when we previously have a different theme
//            For example right now its day and we got isNight flag as false that's mean don't apply day mode right away
//            instead we should wait until we got isNight
//            Log.d(TAG, "NightModeAutoUpdateTask: handleDayOrNightView: session: " + sessionManagement.getKeyIsNight());
//            Log.d(TAG, "NightModeAutoUpdateTask: handleDayOrNightView: comparison: " + (sessionManagement.getKeyIsNight() != isNight));
            if (sessionManagement.getKeyIsNight() != isNight) {
                sessionManagement.setKeyIsNight(isNight);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UiUtils.applyDarkTheme(isNight);
                    }
                });
            }
        }
    }

    public void setAppBarForReceipts(String screenType) {
        if (isTablet) {
            setAppBarForReceiptsForTablet(screenType);
        } else {
            setAppBarForReceiptsForMobile(screenType);
        }
    }

    void setAppBarForReceiptsForTablet(String screenType) {
        if (screenType.equalsIgnoreCase("Drive")) {
            leftSideMiddlePointHeadingTitle.setText("Drive");
            mainTitle.setText("");
            syncReceiptLayout.setVisibility(View.GONE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.GONE);
            toolbarAddButton.setVisibility(View.GONE);
            toolbarAddButtonPretrip.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            clLogin.setVisibility(View.VISIBLE);
            displayCurrentTime.setVisibility(View.VISIBLE);
            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
        } else if (screenType.equalsIgnoreCase("Logbook")) {
            leftSideMiddlePointHeadingTitle.setText("Logbook");
            mainTitle.setText("");

            syncReceiptLayout.setVisibility(View.GONE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.GONE);
            displayCurrentTime.setVisibility(View.GONE);
            toolbarAddButton.setVisibility(View.GONE);
            toolbarAddButtonPretrip.setVisibility(View.GONE);
            clLogin.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
        } else if (screenType.equalsIgnoreCase("PreTrip Update")) {
//            mainTitle.setText("");
//            syncReceiptLayout.setVisibility(View.GONE);
//            deleteFromAppbar.setVisibility(View.GONE);
//            saveFromAppbar.setVisibility(View.GONE);
//            toolbarAddButton.setVisibility(View.GONE);
//            clLogin.setVisibility(View.GONE);
//            editPretrip.setVisibility(View.VISIBLE);
////            emailPretrip.setVisibility(View.VISIBLE);
//
//            toolbarAddButtonPretrip.setVisibility(View.VISIBLE);
//            displayCurrentTime.setVisibility(View.VISIBLE);
//            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
//            leftSideMiddlePointHeadingTitle.setText("Pretrip");

            leftSideMiddlePointHeadingTitle.setText("Pretrip");
            mainTitle.setText("");
            syncReceiptLayout.setVisibility(View.VISIBLE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.VISIBLE);
            displayCurrentTime.setVisibility(View.VISIBLE);
            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
            clLogin.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            toolbarAddButton.setVisibility(View.GONE);
            toolbarAddButtonPretrip.setVisibility(View.GONE);

        } else if (screenType.equalsIgnoreCase("Receipts")) {
            leftSideMiddlePointHeadingTitle.setText("Receipts");
            mainTitle.setText("");
            syncReceiptLayout.setVisibility(View.VISIBLE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.VISIBLE);
            displayCurrentTime.setVisibility(View.VISIBLE);
            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
            clLogin.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            toolbarAddButton.setVisibility(View.GONE);
            toolbarAddButtonPretrip.setVisibility(View.GONE);
        } else if (screenType.equalsIgnoreCase("Settings")) {
            leftSideMiddlePointHeadingTitle.setText("Settings");
            mainTitle.setText("");

            syncReceiptLayout.setVisibility(View.GONE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.GONE);
            toolbarAddButton.setVisibility(View.GONE);
            toolbarAddButtonPretrip.setVisibility(View.GONE);
            clLogin.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            displayCurrentTime.setVisibility(View.VISIBLE);
            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
        }
    }

    void setAppBarForReceiptsForMobile(String screenType) {

        if (screenType.equalsIgnoreCase("Drive")) {
            leftSideMiddlePointHeadingTitle.setText(getResources().getString(R.string.drive));
            mainTitle.setText("");
            syncReceiptFromMobile.setVisibility(View.GONE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.GONE);
            toolbarAddButton.setVisibility(View.GONE);
            toolbarAddButtonPretrip.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            clLogin.setVisibility(View.VISIBLE);
            displayCurrentTime.setVisibility(View.VISIBLE);
            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
        } else if (screenType.equalsIgnoreCase("Logbook")) {
            leftSideMiddlePointHeadingTitle.setText(getResources().getString(R.string.logbook));
            mainTitle.setText("");

            syncReceiptFromMobile.setVisibility(View.GONE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.GONE);
            displayCurrentTime.setVisibility(View.GONE);
            toolbarAddButton.setVisibility(View.GONE);
            toolbarAddButtonPretrip.setVisibility(View.GONE);
            clLogin.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
        } else if (screenType.equalsIgnoreCase("Pretrip")) {
            leftSideMiddlePointHeadingTitle.setText(getResources().getString(R.string.pretrip));
            mainTitle.setText("");
            syncReceiptFromMobile.setVisibility(View.GONE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.GONE);
            toolbarAddButton.setVisibility(View.VISIBLE);
            clLogin.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            toolbarAddButtonPretrip.setVisibility(View.GONE);
            displayCurrentTime.setVisibility(View.VISIBLE);

            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);


        } else if (screenType.equalsIgnoreCase("Receipts")) {
            leftSideMiddlePointHeadingTitle.setText(getResources().getString(R.string.receipts));
            mainTitle.setText("");
            syncReceiptFromMobile.setVisibility(View.VISIBLE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.GONE);
            toolbarAddButton.setVisibility(View.GONE);
            clLogin.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            toolbarAddButtonPretrip.setVisibility(View.GONE);
            displayCurrentTime.setVisibility(View.VISIBLE);

            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);

        } else if (screenType.equalsIgnoreCase("Settings")) {
            leftSideMiddlePointHeadingTitle.setText(getResources().getString(R.string.settings));
            mainTitle.setText("");

            syncReceiptFromMobile.setVisibility(View.GONE);
            deleteFromAppbar.setVisibility(View.GONE);
            saveFromAppbar.setVisibility(View.GONE);
            toolbarAddButton.setVisibility(View.GONE);
            toolbarAddButtonPretrip.setVisibility(View.GONE);
            clLogin.setVisibility(View.GONE);
            editPretrip.setVisibility(View.GONE);
//            emailPretrip.setVisibility(View.GONE);

            displayCurrentTime.setVisibility(View.VISIBLE);
            leftSideMiddlePointHeadingTitle.setVisibility(View.VISIBLE);
        }


    }

    public void updateTimeOnEachSecond() {
        Log.d(TAG, "updateTimeOnEachSecond: ");
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                currentDisplayTime = getTime(System.currentTimeMillis());
                Log.d(TAG, "updateTimeOnEachSecond: run: currentDisplayTime: " + currentDisplayTime);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        displayCurrentTime.setText(currentDisplayTime);
                        Log.d(TAG, "updateTimeOnEachSecond: run: displayTime: " + currentDisplayTime);
                    }
                });

            }
        }, 0, 10000);
    }

    public String getTime(long timeStamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timeStamp);

//        DEC/13/2022 , 10:00 pm/am
//        String date = DateFormat.format("hh:mm:a", calendar).toString();
//        String date = DateFormat.format("hh:mm a", calendar).toString();           // Like 01:56 AM
//        Dec 14, 2022  -   Format updated
        String date = DateFormat.format("h:mm a", calendar).toString();     // Like  1:56 AM
        return date;
    }

    void initiateUnZipping() {
        Log.d(TAG, "initiateUnZipping: unzip: ");
//
//        Jan 04, 2022  -   Added this function so we can perform all the unzipping here
//        performUnZipping();

//
//        unzip("/storage/emulated/0/Documents/RCOTrucks/offline_tiles.zip",
//                "/storage/emulated/0/Documents/RCOTrucks/FilteredTilesWhichWeUnzipped");

//        String destinationFolderPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTiles";
//        String destinationFolderPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTilesEleven";
//        String destinationFolderPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTilesTen";

        String destinationFolderPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTiles";
        File dir = new File(destinationFolderPath);
//        Jan 05, 2022  -
//        If dir does not exists that means we should also perform unzipping because its never done before(if it had done already then we should have this destination folder already)
        Log.d(TAG, "unzip: copyAsset: dir isExists: " + dir.exists());
        if (!dir.exists()) {
            dir.mkdirs();

            Toast.makeText(this, "The process might take 30 minutes for the first time.", Toast.LENGTH_SHORT).show();
            ExtractZippedAssets extractZippedAssets = new ExtractZippedAssets();
            extractZippedAssets.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        } else {

//            TODO Makes sure we extracted all the files
            showLoadingLayout(false);

//            Then we should check that the destination folder is the right one
            Log.d(TAG, "unzip: initiateUnZipping: dir: length: " + dir.length());
//            File offlineTilesFile = new File(destinationFolderPath + "/offline_tiles");
            File offlineTilesFile = new File(destinationFolderPath);
            File[] files = offlineTilesFile.listFiles();
            Log.d("unzip: Files", "Size: " + files.length);
            for (int i = 0; i < files.length; i++) {
                Log.d("unzip: Files", "FileName:" + files[i].getName());
            }
        }
    }


    public class ExtractZippedAssets extends AsyncTask<String, Integer, Integer> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "unzip: ExtractZippedAssets: onPreExecute: ");
            try {
                showLoadingLayout(true);
            } catch (Throwable throwable) {
                showLoadingLayout(false);
                Log.d(TAG, "unzip: ExtractZippedAssets: onPreExecute: throwable: " + throwable);
            }
        }

        @Override
        protected Integer doInBackground(String... strings) {
            Log.d(TAG, "unzip: ExtractZippedAssets: doInBackground: ");
//                Jan 05, 2022 -    We should only initiate unzipping if we don't have the offline tiles already
//                So we gonna check initially that do we have the unzipped files if yes then do nothing else we should start unzipping process
//            Jan 24, 2022  -   Commented for now
//            callUnZipping();

//            Jan 09, 2022  -   Lets see if this works okay
//            unzip();
            Log.d(TAG, "unzip: ExtractZippedAssets: doInBackground: run only once");
            return 1;
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "unzip: ExtractZippedAssets: onProgressUpdate: ");
            int percent = values[0];
            Log.d(TAG, "unzip: ExtractZippedAssets: onProgressUpdate: percent: " + percent);
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "unzip: ExtractZippedAssets: onPostExecute: result: " + result.toString());
            showLoadingLayout(false);
        }
    }

    void callUnZipping() {
        Log.d(TAG, "unzip: callUnZipping: ");
//        unzip("/storage/emulated/0/Documents/RCOTrucks/offline_tiles.zip",
//                "/storage/emulated/0/Documents/RCOTrucks/FilteredTilesWhichWeUnzipped");
//        String zipFilePath = "/storage/emulated/0/Documents/RCOTruck/tiles.zip";

//        String zipFilePath = "/storage/emulated/0/Documents/RCOTruck/eleven.zip";
//        String unzipAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTilesEleven";
        String zipFilePath = "/storage/emulated/0/Documents/RCOTruck/tiles.zip";
        String unzipAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTiles";

//        String unzipAtLocationPath = "/storage/emulated/0/Documents/RCOTruck";

//        String zipFilePath = "/storage/emulated/0/Documents/RCOTruck/ten.zip";
//        String unzipAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTilesTen";


//        String zipFile = Environment.getExternalStorageDirectory() + "/the_raven.zip"; //your zip file location
//        String unzipLocation = Environment.getExternalStorageDirectory() + "/unzippedtestNew/"; // unzip location

//        Seems not working
//        DecompressFast df= new DecompressFast(zipFilePath, unzipAtLocationPath);
//        df.unzip();

//        Jan 10, 2022  -   Best till this point
        unzip(zipFilePath, unzipAtLocationPath);

//        callUnZippingLibrary(zipFilePath, unzipAtLocationPath);

//        zipFilePath = "/storage/emulated/0/Documents/RCOTruck/eleven.rar";
//        callUnRarLibrary(zipFilePath, unzipAtLocationPath);
    }

    public void unzip() {

//        String unzipAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTilesEleven";
//        String zipFilePath = "/storage/emulated/0/Documents/RCOTruck/eleven.zip";
//        String unzipAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/Eleven/";
        String zipFilePath = "/storage/emulated/0/Documents/RCOTruck/tiles.zip";
        String unzipAtLocationPath = "/storage/emulated/0/Documents/RCOTruck/OfflineTiles/";

        try {
            FileInputStream inputStream = new FileInputStream(zipFilePath);
            ZipInputStream zipStream = new ZipInputStream(inputStream);
            ZipEntry zEntry = null;
            while ((zEntry = zipStream.getNextEntry()) != null) {
                Log.d("Unzip", "Unzipping " + zEntry.getName() + " at "
                        + unzipAtLocationPath);

                if (zEntry.isDirectory()) {
                    hanldeDirectory(zEntry.getName(), unzipAtLocationPath);
                } else {
                    FileOutputStream fout = new FileOutputStream(
                            unzipAtLocationPath + "/" + zEntry.getName());
                    BufferedOutputStream bufout = new BufferedOutputStream(fout);
                    byte[] buffer = new byte[1024];
                    int read = 0;
                    while ((read = zipStream.read(buffer)) != -1) {
                        bufout.write(buffer, 0, read);
                    }

                    zipStream.closeEntry();
                    bufout.close();
                    fout.close();
                }
            }
            zipStream.close();
            Log.d("Unzip", "Unzipping complete. path :  " + unzipAtLocationPath);
        } catch (Exception e) {
            Log.d("Unzip", "Unzipping failed");
            e.printStackTrace();
        }

    }

    public void hanldeDirectory(String dir, String destination) {
        Log.d(TAG, "hanldeDirectory: Unzip: dir: " + dir + " destination: " + destination);
        File f = new File(destination + dir);
        if (!f.isDirectory()) {
            f.mkdirs();
        }
    }

    private void unzip(String zipFilePath, String unzipAtLocation) {
        Log.d(TAG, "unzip: zipFilePath: " + zipFilePath + " unzipAtLocation: " + unzipAtLocation);
        File archive = new File(zipFilePath);
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
//                Log.d(TAG, "unzip: entry: " + entry + " MACOSX contains: " + entry.toString().contains("MACOSX"));
                if (!entry.toString().contains("MACOSX")) {
                    unzipEntry(zipfile, entry, unzipAtLocation);
                }
            }
        } catch (Exception e) {
            Log.e("unzip: zip", "Unzip exception", e);
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {
//        Log.d(TAG, "unzip: unzipEntry: entry: " + entry.isDirectory());
        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

//        Log.d(TAG, "unzip: Extracting: " + entry);
//        InputStream zin = zipfile.getInputStream(entry);
//                            InputStream inputStream = getActivity().getAssets().open(String.format("tiles/%d_%d_%d.png", zoom, x, y));
//        InputStream zin = zipfile.getInputStream(entry);
        InputStream zin = zipfile.getInputStream(entry);
        BufferedInputStream inputStream = new BufferedInputStream(zin);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

//        Log.d(TAG, "unzip: unzipEntry: inputStream: " + inputStream);
//        Log.d(TAG, "unzip: unzipEntry: outputStream: " + outputStream);


        if (!isLoadingShown) {
            showLoadingLayout(true);
        }

        try {
            try {

//                Working Code 1
//                for (int c = inputStream.read(); c != -1; c = inputStream.read()) {
//                    outputStream.write(c);
//                }

//                Working Code 2
//                byte[] buffer = new byte[1024];
//                int read = 0;
//                while ((read = inputStream.read(buffer)) != -1) {
//                    outputStream.write(buffer, 0, read);
//                }

//                Working Code 3
                byte[] buffer = new byte[1024];
                int read;
                while ((read = inputStream.read(buffer,0,1024)) != -1) {
                    outputStream.write(buffer, 0, read);
                }


            } finally {
                Log.d(TAG, "unzip: unzipEntry: close: outputStream");
                outputStream.close();
            }
        } finally {
            Log.d(TAG, "unzip: unzipEntry: close: outputStream, inputStream");
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {
        Log.d(TAG, "unzip: createDir: ");
        if (dir.exists()) {
            return;
        }
        Log.v(TAG, "unzip: createDir: " + dir.getName());
        if (!dir.mkdirs()) {
            Log.d(TAG, "unzip: createDir: Can not create dir " + dir);
            throw new RuntimeException("Can not create dir " + dir);
        }
    }


    void performUnZipping() {
        Log.d(TAG, "performUnZipping: ");
//            unzip(getAssets().open("tiles.zip"),getFilesDir().getAbsolutePath());
//            unzip(getAssets().open("tiles.zip"),getFilesDir().getAbsolutePath());
//            Uri path = Uri.parse("assets/tiles.zip");
//            String newPath = path.toString();
//            unzip(newPath,getFilesDir().getAbsolutePath());
//            unzip("file:///android_assets/FILENAME",getFilesDir().getAbsolutePath());

//        unzip("file:///assets/zip/tiles.zip", getFilesDir().getAbsolutePath());

//        downloadFile();
//        Decompress.unzipFromAssets(MainMenuActivity.this, "file:///assets/zip/tiles.zip", getFilesDir().getAbsolutePath());

        File file = Environment.getExternalStorageDirectory();
//        Decompress.unzipFromAssets(MainMenuActivity.this, "zip/tiles.zip", file.getAbsolutePath()+"/rcoTrucks");
//        Decompress.unzipFromAssets(MainMenuActivity.this, "filtered_zip/filtered_tiles.zip", file.getAbsolutePath()+"/rcoTrucks");
//        getAssets().open("tiles.zip").


        String filePath = "";
        try {
//            filePath = "file:///android_asset/filtered_zip/filtered_tiles.zip";
            filePath = "file:///assets/filtered_zip/filtered_tiles.zip";
//            filePath = "file:///android_asset/filtered_zip/filtered_tiles.zip";
//            int isAssetsAvailable=getAssets().open("filtered_zip/filtered_tiles.zip").available();

//            String assetsPath="filtered_zip/filtered_tiles.zip";
            String assetsPath = "filtered_tiles.zip";
            int isAssetsAvailable = getAssets().open(assetsPath).available();
            Log.d(TAG, "zip: onCreate: isAssetsAvailable: " + isAssetsAvailable);
        } catch (IOException e) {
            Log.d(TAG, "zip: onCreate: " + e);
            e.printStackTrace();
        }

        Log.d(TAG, "zip: onCreate: second: ");
        try {
//            filePath = "file:///android_asset/filtered_zip/filtered_tiles.zip";
//            filePath = "file:///android_asset/filtered_zip/filtered_tiles.zip";
            filePath = "file:///assets/filtered_tiles.zip";


            ZipFile zip = new ZipFile(filePath);
            Log.d(TAG, "zip: onCreate: zip: " + zip);
        } catch (IOException e) {
            Log.d(TAG, "zip: filePath: onCreate: " + e);
        }


        String zippedFileDirectoryPath = "/storage/emulated/0/Documents/RCOTrucks/offline_tiles.zip";
//        UnZip unzipTask = new UnZip("filtered_zip/filtered_tiles.zip", Environment.getExternalStorageDirectory() + "/" + "RCOTrucks/");
//        UnZip unzipTask = new UnZip(filePath, Environment.getExternalStorageDirectory() + "/" + "RCOTrucks/");
        UnZip unzipTask = new UnZip(zippedFileDirectoryPath, "/storage/emulated/0/Documents/RCOTrucks/UnzippedTiles");
        unzipTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

//        startDownloading();
    }


    void copyFile(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] buffer = new byte[1024];
        int read;
//        while(read=inputStream)
    }


    void startDownloading() {
        Log.d(TAG, "startDownloading: ");
//        String serverFilePath = "http://www.colorado.edu/conflict/peace/download/peace_problem.ZIP";
        String serverFilePath = "https://drive.google.com/file/d/165W-11FrK0DLhZRY2itGXMcXI5WkmiF3/view?usp=share_link";

        String path = UnZipFileUtils.getDataDir(MainMenuActivity.this).getAbsolutePath();

        String fileName = "sample_download";
        File file = new File(path, fileName);

        String localPath = file.getAbsolutePath();
        String unzipPath = UnZipFileUtils.getDataDir(MainMenuActivity.this, "ExtractLoc").getAbsolutePath();
        Log.d(TAG, "startDownloading: unzipPath: " + unzipPath);

        FileDownloadService.DownloadRequest downloadRequest = new FileDownloadService.DownloadRequest(serverFilePath, localPath);
        downloadRequest.setRequiresUnzip(true);
        downloadRequest.setDeleteZipAfterExtract(false);
        downloadRequest.setUnzipAtFilePath(unzipPath);


        FileDownloadService.OnDownloadStatusListener listener = new FileDownloadService.OnDownloadStatusListener() {

            @Override
            public void onDownloadStarted() {
                Log.d(TAG, "startDownloading: onDownloadStarted: ");
            }

            @Override
            public void onDownloadCompleted() {
                Log.d(TAG, "startDownloading: onDownloadCompleted: ");
            }

            @Override
            public void onDownloadFailed() {
                Log.d(TAG, "startDownloading: onDownloadFailed: ");
            }

            @Override
            public void onDownloadProgress(int progress) {
                Log.d(TAG, "startDownloading: onDownloadProgress: ");
            }
        };

        FileDownloadService.FileDownloader downloader = FileDownloadService.FileDownloader.getInstance(downloadRequest, listener);
        downloader.download(MainMenuActivity.this);
    }

    void callUnZippingLibrary(String zipFilePath, String unzipAtLocation) {
        Log.d(TAG, "callUnZippingLibrary: ");
        File archive = new File(zipFilePath);

        File outputFile = new File(unzipAtLocation);
        if (!outputFile.exists()) {
            outputFile.mkdir();
        }
        Log.d(TAG, "callUnZippingLibrary: archive: " + archive + " outputFile: " + outputFile + " unzipAtLocation: " + unzipAtLocation);


        try {
//            Jan 09, 2022  -   Its extracting files properly but we don't know anything about progress
//            new net.lingala.zip4j.ZipFile(zipFilePath).extractAll(unzipAtLocation);


            net.lingala.zip4j.ZipFile zipFile = new net.lingala.zip4j.ZipFile(archive);
            zipFile.extractAll(unzipAtLocation);
            ProgressMonitor progressMonitor = zipFile.getProgressMonitor();

            zipFile.setRunInThread(true);
//            zipFile.addFolder(new File("/some/folder"));

            while (!progressMonitor.getState().equals(ProgressMonitor.State.READY)) {
                Log.d(TAG, "callUnZippingLibrary: Percentage done: " + progressMonitor.getPercentDone());
                Log.d(TAG, "callUnZippingLibrary: Current file: " + progressMonitor.getFileName());
                Log.d(TAG, "callUnZippingLibrary: Current task: " + progressMonitor.getCurrentTask());

                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Log.d(TAG, "callUnZippingLibrary: InterruptException: " + e);
                }
            }
            if (progressMonitor.getResult().equals(ProgressMonitor.Result.SUCCESS)) {
                Log.d(TAG, "callUnZippingLibrary: Successfully added folder to zip");
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.ERROR)) {
                Log.d(TAG, "callUnZippingLibrary: Error occurred. Error message: " + progressMonitor.getException().getMessage());
            } else if (progressMonitor.getResult().equals(ProgressMonitor.Result.CANCELLED)) {
                Log.d(TAG, "callUnZippingLibrary: Task cancelled");
            }
            Log.d(TAG, "callUnZippingLibrary: " + progressMonitor.getResult());
//
        } catch (IOException e) {
            Log.d(TAG, "callUnZippingLibrary: ioException: " + e);
        }


//        WZip wZip = new WZip();
//        wZip.unzip(archive,
//                outputFile,
//                "unZipTiles",
//                new WZipCallback() {
//                    @Override
//                    public void onStarted(String identifier) {
//                        Log.d(TAG, "callUnZippingLibrary: initiateTilesUnzipping: onStarted: identifier: " + identifier);
//                    }
//
//                    @Override
//                    public void onZipCompleted(File zipFile, String identifier) {
//                        Log.d(TAG, "callUnZippingLibrary: initiateTilesUnzipping: onZipCompleted: zipFile: " + zipFile.getAbsolutePath() + " identifier: " + identifier);
//                    }
//
//                    @Override
//                    public void onUnzipCompleted(String identifier) {
//                        Log.d(TAG, "callUnZippingLibrary: initiateTilesUnzipping: onUnzipCompleted: identifier: " + identifier);
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable, String identifier) {
//                        Log.d(TAG, "callUnZippingLibrary: initiateTilesUnzipping: onError: throwable: " + throwable.getMessage() + " identifier: " + identifier);
//                    }
//                });
    }

    void callUnRarLibrary(String zipFilePath, String unzipAtLocation) {
        Log.d(TAG, "callUnZippingLibrary: ");
        File archive = new File(zipFilePath);

        File outputFile = new File(unzipAtLocation);
        if (!outputFile.exists()) {
            outputFile.mkdir();
        }
        Log.d(TAG, "callUnZippingLibrary: archive: " + archive + " outputFile: " + outputFile + " unzipAtLocation: " + unzipAtLocation);


        try {
            Log.d(TAG, "try: callUnRarLibrary: archive: "+archive);
//            final File rar = new File(zipFilePath);
//            final File destinationFolder = new File(unzipAtLocation);
            Junrar.extract(archive, outputFile);

        } catch (IOException e) {
            Log.d(TAG, "callUnZippingLibrary: ioException: " + e);
        } catch (RarException e) {
            Log.d(TAG, "callUnZippingLibrary: RarException: " + e);
        }

    }


}
