package com.rco.rcotrucks.activities.drive;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.ConstraintSet;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.android.gms.common.util.IOUtils;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.orhanobut.hawk.Hawk;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.DialogManager;
import com.rco.rcotrucks.activities.LoginActivity;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.drive.adapter.DirectionAdapter;
import com.rco.rcotrucks.activities.drive.adapter.MapAssetsAdapter;
import com.rco.rcotrucks.activities.drive.adapter.OnSetRoute;
import com.rco.rcotrucks.activities.drive.direction.CustomInfoWindowGoogleMap;
import com.rco.rcotrucks.activities.drive.direction.DestinationDetailInfoWindow;
import com.rco.rcotrucks.activities.drive.direction.DirectionView;
import com.rco.rcotrucks.activities.drive.direction.MyLocation;
import com.rco.rcotrucks.activities.drive.direction.NavigationUtils;
import com.rco.rcotrucks.activities.drive.direction.PathFoundListener;
import com.rco.rcotrucks.activities.drive.direction.RouteSignInfoWindow;
import com.rco.rcotrucks.activities.logbook.model.GenericModel;
import com.rco.rcotrucks.activities.streaming.CameraStreamingFragment;
import com.rco.rcotrucks.adapters.AmenetiesExpandableAdapter;
import com.rco.rcotrucks.adapters.DutyEventsAdapter;
import com.rco.rcotrucks.adapters.MapAssetExpandableAdapter;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.DutyEvent;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.rms.MapAsset;
import com.rco.rcotrucks.businesslogic.rms.RestArea;
import com.rco.rcotrucks.businesslogic.rms.TruckLogDetail;
import com.rco.rcotrucks.businesslogic.rms.TruckLogHeader;
import com.rco.rcotrucks.dialog.DropDialog;
import com.rco.rcotrucks.dialog.PickupDialog;
import com.rco.rcotrucks.fragments.BaseFragment;
import com.rco.rcotrucks.interfaces.ListenFromActivity;
import com.rco.rcotrucks.model.AmenitiesGenericModel;
import com.rco.rcotrucks.model.AmenitiesModel;
import com.rco.rcotrucks.model.MapAssetContentModel;
import com.rco.rcotrucks.model.MapAssetModel;
import com.rco.rcotrucks.model.MapAssetSubcategoriesModel;
import com.rco.rcotrucks.receiver.GeofenceBroadcastReceiver;
//import com.rco.rcotrucks.utils.ConnectionMonitor;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.GlideApp;
import com.rco.rcotrucks.utils.ImageUtils;
import com.rco.rcotrucks.utils.SessionManagement;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.TTS;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;
import com.rco.rcotrucks.utils.route.AbstractRouting;
import com.rco.rcotrucks.utils.route.Route;
import com.rco.rcotrucks.utils.route.RouteException;
import com.rco.rcotrucks.utils.route.Routing;
import com.rco.rcotrucks.utils.route.RoutingListener;
import com.rco.rcotrucks.utils.route.Segment;
import com.rco.rcotrucks.utils.zip.UnZip;
import com.rco.rcotrucks.views.PopupWindowHelper;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;
//import com.wwdablu.soumya.wzip.WZip;
//import com.wwdablu.soumya.wzip.WZipCallback;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.file.Files;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static android.widget.RelativeLayout.RIGHT_OF;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.rco.rcotrucks.utils.Constants.KEY_LAST_ODOMETER;

public abstract class DriveFragmentBase extends BaseFragment implements OnMapReadyCallback, MyLocation.onLocationChanged, AdapterView.OnItemSelectedListener,
        PopupMenu.OnMenuItemClickListener, View.OnClickListener, PathFoundListener, NightModeReceiver.Event, ListenFromActivity {

    private static final String TAG = DriveFragmentBase.class.getSimpleName();
    SessionManagement sessionManagement;
    protected BusinessRules rules = BusinessRules.instance();

    private static final int[] COLORS = new int[]{R.color.route_color, R.color.gray, R.color.white, R.color.route_visited_color};

    private static final int ROUTE_DEFAULT_WIDTH = 8, ROUTE_PHONE_WIDTH = 30, ROUTE_TABLET_WIDTH = 30,
            ROUTE_SIGN_PHONE_WIDTH = 10, ROUTE_SIGN_TABLET_WIDTH = 10;
    private float NAVIGATION_ZOOM_LEVEL = 17.5F;
    boolean isRouteInitiallyLoaded = true, isSearchContainerOpened = false, isSearchRoutesShown = false;
    long systemBreakStartTime = 0, systemSleepStartTime = 0;
    Double startTime = 0.0, sleepTime = 0.0, endedCycleTime = 0.0, mTimeStampWhenEnteringOnASegment = 0.0,
            mTimeStampWhenEnteringOnARoute = 0.0;

    private FrameLayout mapContainer, searchContainer, streamingContainer;
    protected ConstraintLayout bottomBar, clLogin, speedCheckLayout, breakLayout, sleepLayout, gaugesContainer,
            cycleCounterLayout, loadingLayout, mapAssetConstraintLayout, yourSpeedLayout, yourSpeedInnerLayout,
            speedLimitLayout, amenitiesLayout;
    ProgressBar progressBar;
    private LinearLayout mphView, btnSearch;
    private TextView tvTxtMph, maxSpeedTV, txtLogin, btnWork, btnStatus, drop, pickup, breakTime, endBreak,
            sleeperTime, exitSleeper, cycleCounterTitle, cycleCounterTime, closeMapAsset, selectAllMapAsset,
            deselectAllMapAsset, saveMapAsset, loadingText, yourSpeed, speedLimit;
    protected ImageView closeOpenPanelBtn, btnRestStation, btnGasStation, btnCenterMap, btnShare, volumeIc,
            cameraIc, gaugesIcon, mapAssets, speechInstructionIcon;
    static int currentSegmentMaxSpeed = 0;

    private String destinationAddress, destinationPhoto;

    //    July 22, 2022 -   mIsFirstInstructionPlayed is created because we can play the instruction for the zeroth index of the route segment
    private boolean isBluetoothDeviceConnected = false, isLoginRequiredPopupOpen = false, isGasStation = true,
            isRestStation = true,
            isStatusDialogShown = false, isTablet = false, isBreakStarted = false, isSleeperStarted = false,
            isMaxSpeedAvailableForSegments = false, isAnyRouteDisplayedForSelection = false,
            animationInProgress = false, mIsFirstInstructionPlayed = false;


    private DirectionView directionView;
    private MapFragment mapFragment;
    private Route displayedRoute;
    private Marker endMarker;
    //    private ConnectionMonitor connectionMonitor;
    private NightModeReceiver nightModeReceiver = null;
    private GeofenceBroadcastReceiver broadcastReceiver = null;
    private GoogleMap googleMap;
    private Marker mPositionMarker;
    private List<Marker> directionMarkers = new ArrayList<>();
    private LatLng destinationLocation;
    LatLng currentPositionOnTheRoute = null;
    private List<Polyline> polylines;
    private List<Polyline> visitedPolylines;
    private Marker calloutMarker;

    abstract void initFragment(View view);

    Handler timerHandler = new Handler();
    Handler sleeperTimerHandler = new Handler();
    Handler endedCycleTimerHandler = new Handler();
    public static boolean isSpeeding = false;

    protected Activity mActivity;
    MapAutoUpdateTask mapAutoUpdateTask;
    UnZip unZip;
    Map<Integer, Integer> maxSpeedHashMap;
    Map<Integer, Integer> segmentsDurationMap;

    Marker bearingMarker = null;
    CameraPosition cameraPosition;
    Float bearingPrevious = 1000f;
    int screenWidthFromDisplayMetrics, screenHeightFromDisplayMetrics;

    int selectedRouteSegmentsSize = 0, thirtyFourHoursInSeconds = (34 * 60 * 60);

    Map<Integer, ArrayList<GenericModel>> maxSpeedArrayHashMap = new HashMap<>();

    private int BACKGROUND_LOCATION_ACCESS_REQUEST_CODE = 10002;
    List<String> geofenceRequestIdList = new ArrayList();
    int mCurrentSegmentPosition;
    private static DriveFragmentBase mainActivityRunningInstance;

    public static DriveFragmentBase getInstace() {
        return mainActivityRunningInstance;
    }

    Long previousTimeStamp = 0L;
    GoogleMap.CancelableCallback cancelableCallback;

    //    June 02, 2022 -   variables to calculates hours for left part of tablet
    private long currentEventTimeInMinutes = 0, drivingTimeInMinutes = 0, cycleTimeInMinutes = 0,
            shiftTimeInMinutes = 0, timeBeforBreakinMinutes = 0, breakTimeInMinutes = -1,
            distancePilot = -1, distanceLove = -1, distanceTA = -1, distanceRestArea = -1;
    int maxCycleHours = 70;

    private static Date lastestTimeNearestLocationUpdated = null;
    private static String nearestPilot = null, nearestLove = null, nearestRest = null, nearestTa = null;
    private static MapAsset nearestPilotAsset = null, nearestLoveAsset = null,
            nearestTaAsset = null;
    List<AmenitiesGenericModel> loveAmenitiesList, pilotAmenitiesList, tAAmenitiesList, restAreaAmenitiesList;
    List<AmenitiesGenericModel> sortedLoveAmenitiesList, sortedPilotAmenitiesList, sortedTAAmenitiesList,
            sortedRestAreaAmenitiesList;

    private static RestArea nearestRestArea = null;
    View mView;
    EditText forKeyboard;
    Bitmap mSmallMarker;
    //    Oct 28, 2022  -
    double odometerStartForCurrentTruck = -1, deltaShiftMiles = -1, miles = 0;
    //2022.08.16 we should have a flag that indicates if the cycle is ended or not. According to this flag we need to do some change in the Cycle logic
    boolean isCycleEnded = false, isICAvailable = false, isSpeechEnabled = false;
    ProgressDialog progressDialog;

    private MapAssetsAdapter mapAssetsAdapter;
    private List<MapAssetModel> mapAssetList;
    MapAssetModel mapAssetModel;

    MapAssetExpandableAdapter mapAssetExpandableAdapter;
    ExpandableListView expandableListView;
    List<MapAssetModel> headerDataList;
    HashMap<String, ArrayList<MapAssetSubcategoriesModel>> childDataList;
    MapAssetContentModel mapAssetContentModel;

    List<MapAssetSubcategoriesModel> mapAssetContentMarkerList;
    List<MapAssetContentModel> mapAssetContentList;

    Handler internetConnectionTimerHandler = new Handler();
    int icCounter = 0;
    TileOverlay tileOverlay;

    TruckLogDetail truckLogDetail = null;

    //        Oct 21, 2022  -   Gauges
    ImageView gaugesDrivingBar, gaugesBreakBar, gaugesShiftBar, gaugesCycleBar, disconnectELD;
    TextView drivingBarValue, breakBarValue, shiftBarValue, cycleBarValue;


    //        Oct 18, 2022  -   Gauges New Layout Bottom Bar (Love - TA - Pilot - Rest Area)
    TextView gaugesLoveStationDistance, gaugesPilotStationDistance, gaugesTAStationDistance, restAreaIcon, gaugesRestAreaStationDistance,
            btnMalfunction, btnDiagnostics, displayedCurrentTime;
    int drivingLastPercentage = 0, breakLastPercentage = 0, shiftLastPercentage = 0, cycleLastPercentage = 0;
    String loveRecordId = "", pilotRecordId = "", taRecordId = "", restAreaRecordId = "";
    String gaugesLastLoveStationDistance = "0 mi.", gaugesLastPilotStationDistance = "0 mi.", gaugesLastTAStationDistance = "0 mi.", gaugesLastRestAreaStationDistance = "0 mi.",
            drivingHoursLastPercentage = "0.0", breakHoursLastPercentage = "0.0", shiftHoursLastPercentage = "0.0", cycleHoursLastPercentage = "0.0";
    View idsView;

    //          Nov 07, 2022    -
    AmenetiesExpandableAdapter amenitiesExpandableAdapter;
    ExpandableListView amenitiesListView;
    List<AmenitiesGenericModel> amenitiesHeaderDataList;
    HashMap<String, ArrayList<MapAssetSubcategoriesModel>> amenitiesChildDataList;
    AmenitiesModel amenetiesModel;

    //    Dec 30, 2022  -
    SpeechRecognizer speechRecognizer;
    int SPEECH_PERMISSION_CODE = 666666;
    private TTS textToSpeech;

    //
//      Sep 13, 2022    -   When network lost
//        1-        Check if internet lost for last 30 seconds                                                              Done
//        2-        if no then wait for 30 second and if within this 30 seconds we got the internet ==>     Do Nothing      Done
//        3-        If the timer lasts more than 30 seconds then we should start applying custom tiles overlay
//                  maximum map zoom level = 4
//                  minimum map zoom level = 11
//                        For offline - custom tiles
    @Override
    public void onNetworkAvailable() {
        Log.d(TAG, "onNetworkAvailable: ");
        isICAvailable = true;
        closeOfflineMapTimer();
        removeTileOverlay();    //  Remove tile overlay if exists
        adjustCameraToUserLocation();

        if (calloutMarker != null) {
            setInfoWindowVisibility(true);
        }
        setSearchVisibility(true);
    }

    @Override
    public void onNetworkLost() {
        isICAvailable = false;
        startTimerForOfflineMap();
        adjustCameraToUserLocation();
        Log.d(TAG, "onNetworkLost: customInfoWindow: isNull: " + (customInfoWindow == null));
        setSearchVisibility(false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        isTablet = getResources().getBoolean(R.bool.isTablet);
        mView = view;
        mainActivityRunningInstance = this;

        handleDayOrNightView();

        initializeArrayList();
        setIds(view);
        setIdsFromActivity();
        initView(view);
        initialize();
        resetGaugesLayout();
        setControlsVisibility();
        setListener();
        updateGPSLocation();
        setupGauges();
        updateLoginViewAsPerMostRecentLoginLogoutEvent();
        updateDutyStatusForBottomBar();
        updateWorkStatusForBottomBar();
        checkIsBreakStarted(false);
        checkIsSleeperStarted();
        checkIsCycleEnded(0);

        ((MainMenuActivity) getActivity()).updateEldActionBarFlags();

        mSmallMarker = getSmallMarker();


        setGaugesWidgets(view);
        initiateCircularProgressBar();

//      Oct 03, 2022    -
//    checkAmenitiesImages("pilot","map_assets_pilot", "");
//    checkAmenitiesImages("rest_area","map_assets_rest_area", "");
//    checkAmenitiesImages("ta","map_assets_ta", "");

//        setStraightLineDistance();
    }

    void initiateCircularProgressBar() {
        updateCircularProgressBar(0, (11 * 60), gaugesDrivingBar, 1);
        updateCircularProgressBar(0, (8 * 60), gaugesBreakBar, 2);
        updateCircularProgressBar(0, (14 * 60), gaugesShiftBar, 3);
        updateCircularProgressBar(0, (maxCycleHours * 60), gaugesCycleBar, 4);
    }

    void updateCircularBarValue() {

        drivingBarValue.setText(drivingHoursLastPercentage);
        breakBarValue.setText(breakHoursLastPercentage);
        shiftBarValue.setText(shiftHoursLastPercentage);
        cycleBarValue.setText(cycleHoursLastPercentage);
    }

    void updateCircularProgressBar() {
        updateCircularProgressBar(gaugesDrivingBar, drivingLastPercentage);
        updateCircularProgressBar(gaugesBreakBar, breakLastPercentage);
        updateCircularProgressBar(gaugesShiftBar, shiftLastPercentage);
        updateCircularProgressBar(gaugesCycleBar, cycleLastPercentage);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mActivity != null) {
            if (!UiUtils.checkIsLocationEnabled(mActivity)) {
                if (!UiUtils.canGetLocation(mActivity)) {
                    UiUtils.showSettingsAlert(mActivity);
                }
            }
        }

//        runHandlerToCheckIsBleConnected();

//        Dec 04, 2022  -   sometimes when drive screen open automatically (like app was in sync and user click on receipts
//        - app went to receipt screen) and eventually when sync completes we reloading drive screen so the app bar keeps showing
//        the widget meant for receipts like delete, save - and title receipts so we calling this function so we can hide widgets meant
//        for receipt screen not drive screen
//        Dec 12, 2022  -   Comment it out actually a function in MainMenuActivity
//        ((MainMenuActivity) getActivity()).setAppBarForReceipts("Receipts");
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged: screen orientation changed: ");

        resetGaugesLayout();
        setGaugesWidgets(mView);

        updateCircularBarValue();
        updateCircularProgressBar();
        updateDistanceValues();
    }

    void resetGaugesLayout() {

        Log.d(TAG, "initialize: ");

        int orientation = getResources().getConfiguration().orientation;
        if (isTablet) {

            if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                Log.d(TAG, "initialize: ORIENTATION_LANDSCAPE: set to 40");
//                guideline.setGuidelinePercent(0.50F);
                ConstraintLayout mainLayout = idsView.findViewById(R.id.include_tablet_side_panel);
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.include_landscape_tablet_layout_for_side_panel, mainLayout, false);
                mainLayout.removeAllViews();
                mainLayout.addView(layout);


                ConstraintLayout mConstrainLayout = (ConstraintLayout) mView.findViewById(R.id.gauges_container);
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mConstrainLayout.getLayoutParams();
                lp.matchConstraintPercentWidth = (float) 0.26;
//                lp.matchConstraintPercentWidth = 1;
                mConstrainLayout.setLayoutParams(lp);

            } else if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                Log.d(TAG, "initialize: ORIENTATION_PORTRAIT: set to 65");

                ConstraintLayout mainLayout = idsView.findViewById(R.id.include_tablet_side_panel);
                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View layout = inflater.inflate(R.layout.include_tablet_layout_for_side_panel, mainLayout, false);
                mainLayout.removeAllViews();
                mainLayout.addView(layout);

                ConstraintLayout mConstrainLayout = (ConstraintLayout) mView.findViewById(R.id.gauges_container);
                ConstraintLayout.LayoutParams lp = (ConstraintLayout.LayoutParams) mConstrainLayout.getLayoutParams();
//                lp.matchConstraintPercentWidth = (float) 0.4;
                lp.matchConstraintPercentWidth = 4;
                mConstrainLayout.setLayoutParams(lp);
            }

        }
    }

    void updateDistanceValues() {
        if (isTablet) {
            ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).setText(gaugesLastPilotStationDistance);
            Log.d(TAG, "updateDistanceValuesOffline: updateDistanceValues: run: get: text: " + ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).getText());
            ((TextView) mView.findViewById(R.id.gauges_love_station_distance)).setText(gaugesLastLoveStationDistance);
            ((TextView) mView.findViewById(R.id.gauges_rest_area_station_distance)).setText(gaugesLastRestAreaStationDistance);
            ((TextView) mView.findViewById(R.id.gauges_ta_station_distance)).setText(gaugesLastTAStationDistance);
        }
    }

    public void eventFromGeofence(final String requestIdAndEventType) {
//        Log.d(TAG, "eventFromGeofence: requestIdAndEventType: " + requestIdAndEventType);
        addLogGeofenceData("DriveFragmentBase: eventFromGeofence: requestIdAndEventType: " + requestIdAndEventType);

        String[] splitRequestIdAndEventType = requestIdAndEventType.split("_");

        if (splitRequestIdAndEventType == null)
            return;

        if (splitRequestIdAndEventType.length < 4) {
            return;
        }

        String segment = splitRequestIdAndEventType[0].trim();
        int segmentInt = Integer.parseInt(segment);
        String speedIndex = splitRequestIdAndEventType[1].trim();
        String speed = splitRequestIdAndEventType[2].trim();
        String eventType = splitRequestIdAndEventType[3].trim();

        addLogGeofenceData("DriveFragmentBase: eventFromGeofence: mCurrentSegmentPosition: " + mCurrentSegmentPosition + " eventType: " + eventType);
//        Log.d(TAG, "eventFromGeofence: mCurrentSegmentPosition: " + mCurrentSegmentPosition + " eventType: " + eventType);
//        Log.d(TAG, "eventFromGeofence: isSegmentEqual: " + (segment.equalsIgnoreCase("" + mCurrentSegmentPosition)) + " speed: " + speed);

//        if (segment.equalsIgnoreCase("" + mCurrentSegmentPosition)) {

        if (eventType.equalsIgnoreCase("ENTER")) {
//            Log.d(TAG, "compareRequestId: eventFromGeofence: inside ENTER: ");
            addLogGeofenceData("DriveFragmentBase: eventFromGeofence: inside: geofenceEnter: eventType: " + eventType);
//            Update Max Speed
//            int speedInMile = (int) Math.round(Utils.convertKmsToMiles(speed));
            maxSpeedHashMap.put(Integer.parseInt(segment), Integer.parseInt(speed));
//            Log.d(TAG, "eventFromGeofence: maxSpeedHashMap: " + maxSpeedHashMap.get(Integer.parseInt(segment)) + " segment: " + segment);
//                maxSpeedHashMap.put(Integer.parseInt(segment), 100);

        } else if (eventType.equalsIgnoreCase("EXIT")) {
//            Log.d(TAG, "compareRequestId: eventFromGeofence: inside Exit: ");
            addLogGeofenceData("DriveFragmentBase: eventFromGeofence: inside: geofenceExit: eventType: " + eventType);
//            May 23, 2022  -   We should change the speed limit also when we exit the geofence especially
//            in the case when geofence radius is very small
//            The problem where not changing the speed was detected in a case where at the beginning we got a
//            small part of the route that has a different speed limit and the current segment was not
//            detected correctly.

//            if (mCurrentSegmentPosition == segmentInt) {
//                maxSpeedHashMap.put(Integer.parseInt(segment), Integer.parseInt(speed));
//                Log.d(TAG, "eventFromGeofence: maxSpeedHashMap: " + maxSpeedHashMap.get(Integer.parseInt(segment)) + " segment: " + segment);
//                ArrayList<String> arrayList = new ArrayList<>();
//                arrayList.add(requestIdAndEventType);
            String pendingIntentId = segment + "_" + speedIndex + "_" + speed;
//            Log.d(TAG, "compareRequestId: eventFromGeofence: pendingIntentId: " + pendingIntentId);
            addLogGeofenceData("DriveFragmentBase: pendingIntentId: pendingIntentId: " + pendingIntentId);

//            geofencingClient.removeGeofences(pendingIntentHashMap.get(pendingIntentId))
//                    .addOnSuccessListener(new OnSuccessListener<Void>() {
//                        @Override
//                        public void onSuccess(Void unused) {
//                            addLogGeofenceData("DriveFragmentBase: pendingIntentId: removeGeofences: onSuccess: Geofence Removed...");
//                            Log.d(TAG, "compareRequestId: removeGeofences: eventFromGeofence: onSuccess: geofencingClient: ");
//                        }
//                    })
//                    .addOnFailureListener(new OnFailureListener() {
//                        @Override
//                        public void onFailure(@NonNull Exception e) {
//                            addLogGeofenceData("DriveFragmentBase: pendingIntentId: removeGeofences: addOnFailureListener: Unable to removed Geofence... exception: " + e.getMessage());
//                            Log.d(TAG, "compareRequestId: removeGeofences: eventFromGeofence: onFailure: exception: " + e.getMessage());
//                        }
//                    });
//                removeGeofenceMonitoring();
//            }
        }
//        }

    }

    @Override
    public void onMapReady(final GoogleMap map) {
        Log.d(TAG, "setRecenterMapButton: onMapReady: ");
        rules.storeLastFarLocation(null);

        mapFragment.onMapReady(map);
        googleMap = map;
        Log.d(TAG, "setRecenterMapButton: onMapReady: googleMap: " + googleMap);
        googleMap.setIndoorEnabled(false);
//            July 14, 2022 -   we enabled google map to show the traffic
        googleMap.setTrafficEnabled(true);
//            Oct 10, 2022  -   We enable compass here
        mapFragment.enableCompassControls(true);
        rePositionCompassIcon();

//            July 20, 2022 -   Added this method so whenever a map is ready then focus the map on the user location
//            with our defined zoom level
//            moveCameraToMyPosition();
        setMapListeners();


//        Sep 22, 2022  -   Check if network is not available then apply the custom zoom tiles right away instead of waiting 30 seconds
//        Also restrict zoom in between 4 to 11.
        if (!UiUtils.isOnline(getContext())) {
            setMapZoomLevel(true);
            applyOfflineCustomTilesOverlay();
        }
        adjustCameraToUserLocation();

//        setStraightLineDistance();
    }

    //    Oct 10, 2022  -   Default position is top left corner and set it on right side
    void rePositionCompassIcon() {
        View compassButton = mapFragment.getView().findViewWithTag("GoogleMapCompass");//to access the compass button
        final float scale = getContext().getResources().getDisplayMetrics().density;

        compassButton.setBackgroundResource(R.drawable.white_oval_shade);
//        Oct 13, 2022  -   Added elevation as Roy recommended
        int elevationInPixels = (int) (14 * scale + 0.5f);
        compassButton.setElevation(elevationInPixels);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            compassButton.setOutlineAmbientShadowColor(getResources().getColor(R.color.black_and_white));
            compassButton.setOutlineSpotShadowColor(getResources().getColor(R.color.black_and_white));
        }


        if (isTablet) {
            int compassIconWidthAndHeightInPixels = (int) (50 * scale + 0.5f);
            compassButton.setMinimumWidth(compassIconWidthAndHeightInPixels);
            compassButton.setMinimumHeight(compassIconWidthAndHeightInPixels);
        } else {
            int compassIconWidthAndHeightInPixels = (int) (45 * scale + 0.5f);
            compassButton.setMinimumWidth(compassIconWidthAndHeightInPixels);
            compassButton.setMinimumHeight(compassIconWidthAndHeightInPixels);
        }

        RelativeLayout.LayoutParams relativeLayoutParams = (RelativeLayout.LayoutParams) compassButton.getLayoutParams();
        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        relativeLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_START, 0);

        if (isTablet) {
            int marginTop = (int) (18 * scale + 0.5f);
            int marginEnd = (int) (12 * scale + 0.5f);

            relativeLayoutParams.topMargin = marginTop;
            relativeLayoutParams.rightMargin = marginEnd;
        } else {

            int marginTop = (int) (85 * scale + 0.5f);
            int marginEnd = (int) (12 * scale + 0.5f);
            relativeLayoutParams.topMargin = marginTop;
            relativeLayoutParams.rightMargin = marginEnd;
        }
    }

    void setMapListeners() {
        googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(@NonNull LatLng latLng) {
                Log.d(TAG, "onMapClick: ");
                if (calloutMarker != null)
                    calloutMarker.showInfoWindow();

                if (isSearchContainerOpened) {
                    searchContainer.setVisibility(View.GONE);
                    isSearchContainerOpened = false;
                    if (isTablet) {
                        UiUtils.closeKeyboard(forKeyboard);
                    }
                }
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        UiUtils.showToast(getActivity(), "onActivityResult invoked");
        super.onActivityResult(requestCode, resultCode, data); // Check which request we're responding to

        rules.getBleDevice().scanForElds(requestCode, resultCode);
    }

    @Override
    public void onDestroy() {
        if (directionView != null) {
            directionView.onDestroy();
            hideDirectionView(true);
        }

        ((MainMenuActivity) getActivity()).setActionBarTitle("");
        NightModeReceiver.cancel(getActivity());
        getActivity().unregisterReceiver(nightModeReceiver);

        nightModeReceiver = null;

        // Hide drive screen icons from action bar on screen exit

//        Sep 30, 2022  -   When we click twice on Drive Option from menu, it automatically hides the login screen so handle this one
//        clLogin.setVisibility(View.GONE);


//        txtGps.setVisibility(View.GONE);

//        July 19, 2022 -   removing the handler when screen destroyed
        closeEndedCycleTimer();

        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login:
//                try {
//                    Log.d(TAG, "setEldEvent: eventCode: onClick: isDriverLoggedIn: " + !rules.isDriverLoggedIn());
                if (!rules.isDriverLoggedIn()) {
                    Log.d(TAG, "onClick: login: ");

//                        Aug 30, 2022  -
//                        First check is current cycle ended?
//                          If yes then show the the dialog for detail with rule spinner and don't show the other dialog after
//                          If no then first check do we have "trucklogdetail" of current driver for today
//                                      If yes then just show the second dialog with three buttons login - other and cancel
//                                      If no then just show the first dialog without rule spinner options

                    TruckLogHeader truckLogHeader = rules.getMostRecentTruckLogHeader();
                    if (truckLogHeader != null) {

                        if (truckLogHeader.EndDate != null && truckLogHeader.EndTime != null && !truckLogHeader.EndDate.isEmpty()
                                && !truckLogHeader.EndTime.isEmpty()) {
                            checkIsCycleEnded = true;
                        } else {
                            checkIsCycleEnded = false;
                        }

                        truckLogDetail = rules.getTruckLogDetailForToday(truckLogHeader.MobileRecordId, "");
                    }

                    Log.d(TAG, "onClick: login: checkIsCycleEnded: " + checkIsCycleEnded);
                    if (checkIsCycleEnded) {
                        showPowerUnitTrailerNumberPromptPopup(getActivity(), null, true);
                    } else {

                        if (truckLogDetail != null) {
                            isTruckLogDetailNull = false;
                            Log.d(TAG, "onClick: truckNumber: " + truckLogDetail.TruckNumber + " Trailer1: " + truckLogDetail.Trailer1Number + " Trailer2: " + truckLogDetail.Trailer2Number);
//                            showLoginScreen(null);  // For now show this for today build
//                            Sep 26, 2022  -   Roy told (When you login you have to show the truck and trailers every time)
                            showPowerUnitTrailerNumberPromptPopup(getActivity(), null, true);
                        } else {
                            isTruckLogDetailNull = true;
                            showPowerUnitTrailerNumberPromptPopup(getActivity(), null, true);
                        }
                    }


                } else {
                    String username = rules.getLastLoggedInUsername();

                    showDialog_With_Listener(getContext(), isTablet, "RCO Trucks", "Are you sure, you want to logout " + username + "?",
                            "Yes", "Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialogInterface, int i) {
                                    if (i == Dialog.BUTTON_POSITIVE) {
                                        String msg = "Missing Certify for: \n01-Oct-20, 30-Sep-20, 29-Sep-20, 28-Sep-20, 27-Sep-20, 26-Sep-20, 25-Sep-20";
                                        showDialog_With_Listener(getContext(), isTablet, "RCO Trucks", msg,
                                                "Yes", "", new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialogInterface, int i) {
                                                        if (i == Dialog.BUTTON_POSITIVE) {

//                                                                Log.d(TAG, "onClick: testingDriving: LOGOUT: " + BusinessRules.EventCode.LOGOUT);
                                                            rules.setEldEvent(getActivity(), BusinessRules.EventType.A_DRIVER_LOGIN_LOGOUT_ACTIVITY, BusinessRules.EventCode.LOGOUT,
                                                                    BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Logout", null, "", "");

                                                            rules.logoutDriver();
                                                            updateLoginView();

//                                                        April 18, 2022    -   When driver logged out then we should not show the "D" label
                                                            getActivity().findViewById(R.id.btn_diagnostics).setVisibility(View.GONE);

                                                        }
                                                    }
                                                });
                                    }
                                }
                            });
                }
                break;

            case R.id.btn_rest_station:
                showHideRestStation();
                break;

            case R.id.btn_gas_station:
                showHideGasStation();
                break;

            case R.id.btn_share:
                setupSharePopup();
                break;

            case R.id.iv_volume:
                handleVoice();
                break;

            case R.id.btn_center_map:
                adjustCameraToUserLocation();

//                Sep 22, 2022  -
//                try {
//                    if (displayedRoute != null) {
//                        Segment currentStep = getSelectedStep();
//
//                        CameraPosition cameraPosition = new CameraPosition.Builder()
//                                .target(mapFragment.displayedArea(currentStep, currentPositionOnTheRoute))
//                                .tilt(60)
//                                .zoom(NAVIGATION_ZOOM_LEVEL)
//                                .bearing(mapFragment.bearing(currentStep.startPoint(), currentStep.endPoint()))
//                                .build();
//
//                        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//                    }
//                } catch (Exception e) {
//
//                }
                break;

            case R.id.iv_camera:
                handleStreaming();
                break;
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.option_disconnect:
                try {
                    if (!rules.isDriverLoggedIn()) {
                        showPowerUnitTrailerNumberPromptPopup(getActivity(), null, true);
                    } else {
                        String username = rules.getLastLoggedInUsername();

                        UiUtils.showBooleanDialog(getActivity(), "Notification", android.R.drawable.ic_dialog_info,
                                "Yes", "Cancel", "Logout " + username + "?", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String msg = "Missing Certify for: \n01-Oct-20, 30-Sep-20, 29-Sep-20, 28-Sep-20, 27-Sep-20, 26-Sep-20, 25-Sep-20";

                                        UiUtils.showOkDialog(getActivity(), "Notification", android.R.drawable.ic_dialog_info,
                                                msg, true, new DialogInterface.OnClickListener() {
                                                    @Override
                                                    public void onClick(DialogInterface dialog, int which) {

                                                        rules.setEldEvent(getActivity(), BusinessRules.EventType.A_DRIVER_LOGIN_LOGOUT_ACTIVITY, BusinessRules.EventCode.LOGOUT,
                                                                BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Logout", null, "", "");

                                                        rules.logoutDriver();
                                                        updateLoginView();

                                                    }
                                                });
                                    }
                                });
                    }
                } catch (Throwable throwable) {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        UiUtils.showExclamationDialog(getActivity(), "Error", throwable.getMessage());
                    }
                }
                break;
        }

        return false;
    }

    @Override
    public void onPathsFound(LatLng endLocation, String photo) {
//        Log.d(TAG, "SearchRelevant: onPathsFound: ");
        try {

//            Log.d(TAG, "SearchRelevant: onPathsFound: ");
            destinationLocation = endLocation;
            destinationPhoto = photo;
            isMaxSpeedAvailableForSegments = false;
            if (mapFragment.getMyLastLocation() != null && destinationLocation != null) {
//                Log.d(TAG, "SearchRelevant: onPathsFound: mapFragment.getMyLastLocation() != null && destinationLocation != null");
                Routing routing = new Routing.Builder()
                        .key(getString(R.string.google_maps_key))
                        .travelMode(AbstractRouting.TravelMode.DRIVING)
                        .withListener(directionListener)
                        .alternativeRoutes(true)
                        .waypoints(mapFragment.getMyLastLocation(), destinationLocation)
                        .build();

                routing.execute();
            }
        } catch (Throwable throwable) {
            Log.d(TAG, "SearchRelevant: onPathsFound: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    LatLng previousLocation = null;
    LatLng mCurrentLocation = null;
    double previousDistanceToDestination = 0;
    boolean isDestinationReached = false;
    Location lastNotNullLocation = null;

    @Override
    public void onLocationChanged(Location loc) {
//        Log.d(TAG, "bearingPoint: projection: onLocationChanged: location: lat: " + loc.getLatitude() + ", long: " + loc.getLongitude());
        lastNotNullLocation = loc;
        mCurrentLocation = new LatLng(loc.getLatitude(), loc.getLongitude());

        setRecenterMapButton();
        int deviceGPSSpeed = 0;

        if (loc.hasSpeed()) {
            deviceGPSSpeed = (int) loc.getSpeed();
            rules.setDeviceGPSSpeed(deviceGPSSpeed);
        }

        rules.setDeviceGPSLatitude(loc.getLatitude());
        rules.setDeviceGPSLongitude(loc.getLongitude());

        if (displayedRoute != null && mapFragment != null) {
            currentPositionOnTheRoute = mapFragment.findNearestPointFromRoute(new LatLng(loc.getLatitude(), loc.getLongitude()));
//            mapFragment.addBlueMarkerForMyVisitedPosition(currentPositionOnTheRoute);
            int segmentPosition = mapFragment.getCurrentRouteSegmentPosition(new LatLng(loc.getLatitude(), loc.getLongitude()));
//            July 12, 2022 -   We need to save the timestamp when step changed for calculating the remaining duration of the step
//            Log.d(TAG, "onLocationChanged: comparison: segment positions: ");
            if (segmentPosition != mCurrentSegmentPosition) {
//                Log.d(TAG, "onLocationChanged: comparison: when: segmentPosition != mCurrentSegmentPosition: as segmentPosition: "
//                        + segmentPosition + " mCurrentSegmentPosition: " + mCurrentSegmentPosition);
//                July 22, 2022 -   Todo May be should add a condition that (segmentPosition > mCurrentSegmentPosition)
                mTimeStampWhenEnteringOnASegment = DateUtils.getTimestampInDouble();
//                Log.d(TAG, "onLocationChanged: comparison: segmentPosition: " + segmentPosition + " when segmentPosition != mCurrentSegmentPosition");
//                Aug 02, 2022  -   In the case when we are rerouting, there is a strange situation that the first message of the previous route played
                boolean playMessage = true;
                if (segmentPosition < mCurrentSegmentPosition) {
                    playMessage = false;
                }

                if (playMessage) {
                    directionView.playInstructionForStep((segmentPosition));
                }
            } else {
//                July 22, 2022 -   We need to play the first instruction message also when we start the route
//                Log.d(TAG, "else: onLocationChanged: segmentPosition: " + segmentPosition + " mIsFirstInstructionPlayed: " + mIsFirstInstructionPlayed);
                if (segmentPosition == 0 && !mIsFirstInstructionPlayed) {
                    directionView.playInstructionForStep((0));
                    mIsFirstInstructionPlayed = true;
//                    July 26, 2022 -   We need to initialize mTimeStampWhenEnteringOnASegment herd with the get timestamp here
//                    because it was giving a value 0.0
                    mTimeStampWhenEnteringOnASegment = DateUtils.getTimestampInDouble();
                }
            }
            mCurrentSegmentPosition = segmentPosition;

            currentSegmentMaxSpeed = getCurrentSegmentMaxSpeed(mCurrentSegmentPosition);
        }


        LatLng lastLatLong = new LatLng(loc.getLatitude(), loc.getLongitude());

//        May 26, 2022  -   We should check Geofences only when driving
//        if (displayedRoute != null && (rules.getLastEldEventCode() == BusinessRules.EventCode.DRIVING)) {
        if (displayedRoute != null) {
//            Log.d(TAG, "onLocationChanged: displayedRoute: " + displayedRoute +
//                    " mCurrentSegmentPosition: " + mCurrentSegmentPosition);

            GenericModel genericDataModel = getGeofencePointForSegment(mCurrentSegmentPosition);
//            Log.d(TAG, "onLocationChanged: genericDataModel: " + genericDataModel);
            if (genericDataModel != null) {

                int secondsDelay = 0;
                double radius = genericDataModel.getStraightDistance();
//                Log.d(TAG, "onLocationChanged: radius: " + radius);
                if (radius < 100) {
                    secondsDelay = 1;
                } else {
                    secondsDelay = 3;
                }

//                Log.d(TAG, "onLocationChanged: previousTimeStamp: " + previousTimeStamp);
                if (previousTimeStamp == 0) {
                    Long currentTimeStamp = DateUtils.getTimestamp();
                    previousTimeStamp = currentTimeStamp;
                } else {
                    Long currentTimeStamp = DateUtils.getTimestamp();
                    Long delta = currentTimeStamp - previousTimeStamp;

//                    Log.d(TAG, "CustomGeofence: onLocationChanged: delta: " + delta + " secondsDelay: " + secondsDelay + " " +
//                            "(delta >= secondsDelay): " + (delta >= secondsDelay));
                    if (delta >= secondsDelay) {

                        previousTimeStamp = currentTimeStamp;
                        boolean isLocationInGeofence = geofenceContainsPoint(genericDataModel, lastLatLong);
//                        Log.d(TAG, "onLocationChanged: isLocationInGeofence: "+isLocationInGeofence);
                        if (isLocationInGeofence) {
//                            Log.d(TAG, "CustomGeofence: onLocationChanged: isLocationInGeofence: " + isLocationInGeofence);
                            maxSpeedHashMap.put(mCurrentSegmentPosition, genericDataModel.getSpeed());
                            removeFirstGeofenceForSegment(mCurrentSegmentPosition);
                        }
                    }
                }
            }
        }

        mapFragment.setMyLastLocation(lastLatLong);

//            March 25, 2022    -   We should not load map assest here  -   It might block the interface
//            if (rules.isNewLocation100MilesFar(loc) && googleMap != null) {
//                Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: isNewLocation100MilesFar: before: ");
//                rules.loadMapAssetsAndRestAreasIn100MilesRange(getActivity(), googleMap, true, true);
//                Log.d(TAG, "loadMapAssetsAndRestAreasIn100MilesRange: isNewLocation100MilesFar: after: ");
//            }

//        Log.d(TAG, "onLocationChanged: isLocationUpdated: " + rules.isLocationUpdated(loc));

//        Log.d(TAG, "isReroutingFailing: outer: onLocationChanged: displayedRoute: " + displayedRoute
//                + " isRerouting: " + isRerouting + " isReroutingFailing: " + isReroutingFailing);

        if (rules.isLocationUpdated(loc)) {
            rules.storeLastFarLocation(loc);
//            Log.d(TAG, "isReroutingFailing: yes location changed: onLocationChanged: displayedRoute: " + displayedRoute + " isRerouting: " + isRerouting);
            int tolerance = 15;

            if (displayedRoute == null && isReroutingFailing) {
//                Log.d(TAG, "onLocationChanged: destination: isReroutingFailing: onLocationChanged: if (displayedRoute==null && isReroutingFailing)");
                handleNavigation(lastLatLong, tolerance);
            } else if (displayedRoute != null && mapFragment != null) {
                double distanceToDestination = SphericalUtil.computeDistanceBetween(lastLatLong, displayedRoute.getPoints().get(displayedRoute.getPoints().size() - 1));

                if (distanceToDestination < 10D && mCurrentSegmentPosition == (displayedRoute.getSegments().size() - 1)) {
//                    Log.d(TAG, "onLocationChanged: destination: onLocationChanged: destination: when destination is less than 10D");

                    if (!mapFragment.isDestinationInstructionAlreadyGiven()) {
                        isDestinationReached = true;
                        isMaxSpeedAvailableForSegments = false;
                        directionView.handleDestinationReached();
                        mapFragment.setDestinationInstructionAlreadyGiven(true);
//                        mIsFirstInstructionPlayed = false;
                    }
                } else {
                    if (isDestinationReached && distanceToDestination < 50D) {
//                        Log.d(TAG, "onLocationChanged: destination: do nothing.");
                        return;
                    }

//                    Log.d(TAG, "onLocationChanged: destination: when destination is far away");

//                    Log.d(TAG, "onLocationChanged: destination: previousDistanceToDestination: " + previousDistanceToDestination + " distanceToDestination: " + distanceToDestination);
                    if (mCurrentSegmentPosition != (displayedRoute.getSegments().size() - 1)) {
//                        Log.d(TAG, "onLocationChanged: destination: this is not the last segment: mCurrentSegmentPosition: " + mCurrentSegmentPosition
//                                + " segments: size: " + (displayedRoute.getSegments().size() - 1));
//                        July 29, 2022 -   This is the normal driving logic
                        mapFragment.setDestinationInstructionAlreadyGiven(false);
                        handleNavigation(lastLatLong, tolerance);
                    } else {

                        if (distanceToDestination > previousDistanceToDestination) {
                            tolerance = 100;
//                            Log.d(TAG, "onLocationChanged: destination: when distanceToDestination > previousDistanceToDestination ");
//                        July 29, 2022 -   We are on the last segment - we are passing by the destination with more than 50 meters than we should do a reroute
                            mapFragment.setDestinationInstructionAlreadyGiven(false);
                            handleNavigation(lastLatLong, tolerance);

                        } else {
                            if (distanceToDestination > 50D) {
//                                Log.d(TAG, "onLocationChanged: destination: (distanceToDestination > 50D)");
                                mapFragment.setDestinationInstructionAlreadyGiven(false);
                                handleNavigation(lastLatLong, tolerance);
                            } else {
//                            July 29, 2022 -   Do nothing we are on the destination area
//                                Log.d(TAG, "onLocationChanged: destination: Do nothing we are on the destination area");
                                tolerance = 50;
                                handleNavigation(lastLatLong, tolerance);
                            }
                        }
                    }

                }

                previousDistanceToDestination = distanceToDestination;

            } else if (googleMap != null) {
                if (!isAnyRouteDisplayedForSelection) {
//                    Sep 15, 2022  -   Don't animate the camera to user location when user is moving but have not selected any route - Roy mentioned in email received on sep 15
//                    Only forced and focused current location when user selected a route
                    Log.d(TAG, "adjustMapScreen: animateCamera: from onLocationChanged and displayedRoute: " + displayedRoute);
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lastLatLong, mapFragment.calculateZoomLevel(4000, mapContainer, mActivity)));
                }
            }


        }

        if (rules.isLocationUpdated(loc)) {
            updateGPSView();
        }

        previousLocation = new LatLng(loc.getLatitude(), loc.getLongitude());
    }

    void resetValues() {
        Log.d(TAG, "resetValues: ");

        directionView.setmPreviousInstructionPlayed("");
        isAnyRouteDisplayedForSelection = false;
        isDestinationReached = false;
        mIsFirstInstructionPlayed = false;

        hideDirectionView(true);
//            showSpeedLayout(false);
        mapFragment.enableLocationButton(false);
        mapFragment.enableZoomControls(true);
        mapFragment.removeAllIncludedMarkers();

        NavigationUtils.lastVisitedSegmentPoint = 0;
        mapFragment.lastVisitedSegment = 0;
        mTimeStampWhenEnteringOnASegment = 0.0;
        mTimeStampWhenEnteringOnARoute = 0.0;

        currentSegmentMaxSpeed = 0;
        bearingMarker = null;
        displayedRoute = null;
//        Log.d(TAG, "onLocationChanged: destination: resetValues: displayedRoute: " + displayedRoute);
        currentPositionOnTheRoute = null;
        mPositionMarker = null;

        maxSpeedHashMap.clear();
//        pendingIntentHashMap.clear();

        showSearchLayout(true);
        enableNavigationDrawerButton(true);
        mapFragment.enableLocationPointer(true);
    }

    void showSearchLayout(boolean value) {
//        Log.d(TAG, "showSearchLayout: value: " + value);
        if (value) {
            btnSearch.setVisibility(View.VISIBLE);
        } else {
            btnSearch.setVisibility(View.GONE);
        }
    }


    void resetValuesForNavigation() {
//        April 11, 2022    -   We should reset the values for rerouting/navigation
//        Log.d(TAG, "resetValuesForNavigation: ");
        NavigationUtils.lastVisitedSegmentPoint = 0;
        mapFragment.lastVisitedSegment = 0;
        isDestinationReached = false;

        currentSegmentMaxSpeed = 0;
        displayedRoute = null;
//        Log.d(TAG, "onLocationChanged: destination: resetValuesForNavigation: displayedRoute: " + displayedRoute);
        currentPositionOnTheRoute = null;

        maxSpeedHashMap.clear();

        if (bearingMarker != null) {
            bearingMarker.remove();
            bearingMarker = null;
        }
    }

    void enableNavigationDrawerButton(Boolean isEnabled) {
        ImageView navigationDrawerIcon = getActivity().findViewById(R.id.ic_menu);
        if (isEnabled) {
            navigationDrawerIcon.setClickable(true);
            navigationDrawerIcon.setEnabled(true);
            navigationDrawerIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.white)
                    , android.graphics.PorterDuff.Mode.SRC_IN);
        } else {
            navigationDrawerIcon.setClickable(false);
            navigationDrawerIcon.setEnabled(false);
            navigationDrawerIcon.setColorFilter(ContextCompat.getColor(getActivity(), R.color.dark_grey)
                    , android.graphics.PorterDuff.Mode.SRC_IN);
        }
    }

    //region Tasks
    public class MapAutoUpdateTask extends AsyncTask<String, String, Integer> {
        private int syncIntervalSecsMapAutoUpdateTask = 1;
        private long intervalCounter = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Log.d(TAG, "sortAmenity: MapAutoUpdateTask: onPreExecute: ");
//            June 03, 2022 -   We should calculate the hours when the thread is started
//            Log.d(TAG, "MapAutoUpdateTask: onPreExecute: calculateHours: ");
            calculateHours();
            calculateGaugesValuesForDistance();
            if (isTablet) {
                updateGaugesParametersForDistance();
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
//            Log.d(TAG, "MapAutoUpdateTask: doInBackground: ");
            Log.d(TAG, "MapAutoUpdateTask: doInBackground: ");
            do {
                String drivingSpeed = rules.getDrivingSpeed();

//            June 03, 2022 -   We should update the minutes by one
                boolean isTimeToUpdateGauges = intervalCounter == 0 || (intervalCounter % 60 == 0);
                boolean isTimeToUpdateDistances = intervalCounter == 0 || (intervalCounter % 60 == 0);

                if (!rules.isBleConnected()) {
                    isTimeToUpdateDistances = false;
                }

                if ((isTablet || (gauges_barvalue_driving_hours != null)) && isTimeToUpdateGauges) {
                    updateStatusAndGauges();
                }

//                Log.d(TAG, "updateDistance: doInBackground: isTimeToUpdateDistances: " + isTimeToUpdateDistances);
                if (isTimeToUpdateDistances) {
                    Log.d(TAG, "sortAmenity: doInBackground: isTimeToUpdateDistances: true");
                    calculateGaugesValuesForDistance();
                    if (isTablet) {
                        updateGaugesParametersForDistance();
                    } else {
//                        updateBottomValues();
                    }
                }

                updateViewForSpeed(Float.parseFloat(rules.getDrivingSpeed()));


                if (!StringUtils.isNullOrWhitespaces(drivingSpeed))
                    publishProgress("2:" + drivingSpeed);
                else
                    publishProgress("0:");

                try {
                    Thread.sleep(syncIntervalSecsMapAutoUpdateTask * 1000);
                } catch (InterruptedException interruptedException) {
//                    MainMenuActivity.logDataArrayList.add("doInBackground: interruptedException: " + interruptedException);
                    addLogGeofenceData("MapAutoUpdateTask: doInBackground: interruptedException: " + interruptedException);
//                    Log.d(TAG, "MapAutoUpdateTask: doInBackground: interruptedException: " + interruptedException.getMessage());
                    interruptedException.printStackTrace();
                }

            } while (true);

        }

        @Override
        protected void onProgressUpdate(String... values) {
//            try {
            if (values == null || values[0] == null)
                return;

            boolean isFirstIteration = intervalCounter == 0;
            boolean isTimeToUpdateGauges = isFirstIteration || (intervalCounter % 2 == 0);
            boolean isTimeToTriggerDrivingRelatedRules = isFirstIteration || (intervalCounter % 2 == 0);

//            Log.d(TAG, "MapAutoUpdateTask: onProgressUpdate: isTimeToUpdateGauges: " + isTimeToUpdateGauges);
//                if (isTimeToUpdateGauges)
//            Log.d(TAG, "tablet: onProgressUpdate: isTimeToUpdateGauges: " + isTimeToUpdateGauges);
            if (isTimeToUpdateGauges && isTablet)
//                updateGaugesParameters();

                if (isTimeToTriggerDrivingRelatedRules) {
                    if (values[0].startsWith("2:") && values[0].length() > 2) {
                        String drivingSpeedStr = values[0].substring(2);
                        double drivingSpeed = Double.valueOf(drivingSpeedStr);

                        int drivingSpeedInMiles = Utils.convertKmsToRoundedMiles(drivingSpeed);
//                            tvTxtMph.setText("" + drivingSpeedInMiles);

                        if (!rules.isDriverLoggedIn()) {
                            if (drivingSpeed > 0 && !isLoginRequiredPopupOpen && !hasOpenedDialogs()) {
                                isLoginRequiredPopupOpen = true;

//                            April 29, 2022    -   If we are not logged in, we should not record automatic duty event
//                            Log.d(TAG, "onProgressUpdate: setEldEvent: ");
                                if (rules.isDebugMode()) {
                                    rules.setEldEvent(getActivity(), BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, BusinessRules.EventCode.ON_DUTY_NOT_DRIVING,
                                            BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.ASSUMED_FROM_UNIDENTIFIED_DRIVER_PROFILE, "On Duty Not Driving", "", "", "");
                                }

                                if (getActivity() != null) {
                                    DialogManager.d = UiUtils.showExclamationDialog(getActivity(), "Login Required", "Please stop and Login!", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface d, int which) {
                                            isLoginRequiredPopupOpen = false;

                                            if (d != null)
                                                d.dismiss();

                                            showPowerUnitTrailerNumberPromptPopup(getActivity(), null, true);
                                        }
                                    });
                                }
                            }
                        }
                    }

                    if (rules.isDriverLoggedIn()) {
//                    Log.d(TAG, "MapAutoUpdateTask: onProgressUpdate: " + rules.isDriverLoggedIn());
                        String drivingStatus = rules.getDrivingStatus();

//                    Log.d(TAG, "MapAutoUpdateTask: onProgressUpdate: drivingStatus: " + drivingStatus);
                        if (drivingStatus != null) {
                            String statusRadioName = getStatusRadioName();
                            boolean isStatusRadioNameDriving = statusRadioName != null && statusRadioName.equalsIgnoreCase("Driving");

//                            if (!isStatusRadioNameDriving && drivingStatus.equalsIgnoreCase("Driving")) {
//                                Log.d(TAG, "onProgressUpdate: setEldEventToDriving: ");
//                                rules.setEldEventToDriving(getActivity());
//                            }


//      March 31, 2022  -   We should not record driving here because this thread is
//      called every one second and we only update labels here
//
//                        if (rules.getDutyEventCode() != BusinessRules.EventCode.DRIVING) {
//                            Log.d(TAG, "MapAutoUpdateTask: TestingDriving: MapAutoUpdateTask: setEldEventToDriving: " + rules.getDutyEventCode());
//                            rules.setEldEventToDriving(getActivity(), "AnnotationMapAutoUpdateTask");
//                        }

//                          March 15, 2022  -   We should update the bottom status because of the issue of Driving state
//                            updateStatusRadioName(drivingStatus, "1");
//                        Log.d(TAG, "MapAutoUpdateTask: onProgressUpdate: updateDutyStatusForBottomBar: ");
                            updateDutyStatusForBottomBar();
                        }
                    }
                }

            intervalCounter++;
        }
    }


    public class UpdateMaxSpeedViewUpdate extends AsyncTask<String, String, ArrayList<GenericModel>> {
        int stepPosition;
        LatLng myLocation;
        LatLng destination;

        public UpdateMaxSpeedViewUpdate(int stepPosition, LatLng myLocation, LatLng destination) {
            this.stepPosition = stepPosition;
            this.myLocation = myLocation;
            this.destination = destination;
        }

        @Override
        protected void onPreExecute() {
//            Log.d(TAG, "onPreExecute: UpdateMaxSpeedView: ");
        }

        @Override
        protected ArrayList<GenericModel> doInBackground(String... params) {

            try {

                addLogGeofenceData("DriveFragmentBase: UpdateMaxSpeedViewUpdate: getMaxSpeedForSegment: " + stepPosition
                        + " from latitude, longitude: " + myLocation.latitude
                        + "," + myLocation.longitude
                        + " toLatitude, toLongitude: " + destination.latitude
                        + "," + destination.longitude);
                return rules.getSpeedFromLocationUpdate(myLocation, destination, stepPosition);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<GenericModel> arrayList) {
            int speed = 0;

            if (arrayList != null) {

                if (arrayList.size() > 0) {
                    speed = arrayList.get(0).getSpeed();
                }
//                Log.d(TAG, "onPostExecute: maxSpeedHashMap: size: " + maxSpeedHashMap.size() + " stepPosition: " + stepPosition + " speed: " + speed);
                maxSpeedHashMap.put(stepPosition, speed);

//          May 20, 2022    -   We should set the initial speed limit for the segment to the first speed value
//            And if we have geofences on that segment then we will update speed for that segment then we update
//            speed value from that geofence
//            if (stepPosition < arrayList.size()) {
//                speed = arrayList.get(stepPosition).getSpeed();
//                Log.d(TAG, "maxSpeed: UpdateMaxSpeedView: onPostExecute: stepPosition: " + stepPosition + " speed: " + speed);
//                maxSpeedHashMap.put(stepPosition, speed);
//            } else {
//                maxSpeedHashMap.put(stepPosition, 0);
//            }

//                Log.d(TAG, "printMaxSpeedArrayHashMap: onPostExecute: selectedRouteSegmentsSize: "
//                        + selectedRouteSegmentsSize + " getMaxSpeedArrayHashMap: "
//                        + rules.getMaxSpeedArrayHashMap().keySet().size());

                if (selectedRouteSegmentsSize == rules.getMaxSpeedArrayHashMap().keySet().size()) {
//                printMaxSpeedArrayHashMap(stepPosition);
                    printMaxSpeedArrayHashMap();
                }

//            printMaxSpeedArrayHashMap();
            }

        }
    }

    public class UpdateTruckUserTask extends AsyncTask<String, String, Integer> {
        @Override
        protected void onPreExecute() {

        }

        @Override
        protected Integer doInBackground(String... params) {
            try {
                rules.updateTruckDriverAndLocation(getActivity());
                return BusinessRules.OK;
            } catch (Throwable throwable) {
                return BusinessRules.UNABLE_TO_SYNC;
            }
        }

        @Override
        protected void onProgressUpdate(String... values) {

        }

        @Override
        protected void onPostExecute(Integer result) {

        }
    }

    //endregion

    //region Popups

    private boolean isAnnotationPopupOpen = false;

    protected void showAnnotationPopup(final String annotationSubTitle, final BusinessRules.EventCode eventCode, final boolean isUpdateStatusRadioName) {
        DialogManager.d = UiUtils.showCustomLayoutDialog(getActivity(), R.layout.dialog_annotation);
        isAnnotationPopupOpen = true;

        TextView tv = DialogManager.d.findViewById(R.id.tv_annotation_subtitle);
        tv.setText(annotationSubTitle);

        EditText inputEditText = DialogManager.d.findViewById(R.id.et_annotation);
//        March 31, 2022    -   We should populate the initial annotation for input
        if (eventCode == BusinessRules.EventCode.ON_BREAK_STARTED) {
            inputEditText.setText("Break");
        } else if (eventCode == BusinessRules.EventCode.SLEEPER_BERTH) {
            inputEditText.setText("Sleeper");
        } else if (eventCode == BusinessRules.EventCode.OFF_DUTY) {
            inputEditText.setText("Off Duty");
        } else if (eventCode == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
//            July 08, 2022 -   We should prepopulate the onDuty status on the edittext also
            inputEditText.setText("On Duty");
        }


//        May 26, 2022  -   This is used for showing keyboard in Default Dialog
        InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        DialogManager.d.findViewById(R.id.bt_annotation_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    UiUtils.closeKeyboard(inputEditText);
//                    EditText et = DialogManager.d.findViewById(R.id.et_annotation);
                    TextView dialogTypeTV = DialogManager.d.findViewById(R.id.tv_annotation_subtitle);
//                    March 22, 2022    -   We are adding dynamic annotation instead of static "Break"
//                    final String etAnnotation = (eventCode == BusinessRules.EventCode.ON_BREAK ? "Break " : "") + et.getText().toString();
//                String etAnnotation = (eventCode == BusinessRules.EventCode.ON_BREAK_STARTED ? "Break " : "") + et.getText().toString();

                    String etAnnotation = inputEditText.getText().toString();
                    inputEditText.requestFocus();

//                    Log.d(TAG, "Break: onClick: break: etAnnotation: " + etAnnotation);
//                    Log.d(TAG, "Break: TestingDriving: onClick: break: showAnnotationPopup: " + eventCode);

//                    March 30, 2022    -   We need to check if their is an active truck log detail and if
//                    not then we need to create it
//                    existsTruckLogRecords();
//                    showPowerUnitTrailerNumberPromptPopup(getActivity(), null, false);


//              March 23, 2022  -
                    String dialogTypeTitle = dialogTypeTV.getText().toString();

//                    Log.d(TAG, "parse: hours: bt_annotation_save: calculateCurrentDrivingHours: dialogTypeTitle: " + dialogTypeTitle);
                    rules.calculateCurrentDrivingHours();

//                    Log.d(TAG, "onClick: break: etText: " + dialogTypeTitle);
                    if (dialogTypeTitle.equalsIgnoreCase("Break")) {
                        BusinessRules.EventCode mostRecentBreakEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.BREAK, sessionManagement);
//                        Log.d(TAG, "onClick: break: mostRecentBreakEventCode: " + mostRecentBreakEventCode);
                        if (mostRecentBreakEventCode == BusinessRules.EventCode.ON_BREAK_STARTED) {
//                            Log.d(TAG, "onClick: BusinessRules.EventCode.ON_BREAK_STARTED");
//                            Log.d(TAG, "onClick: break: set annotation Break Ended because last event was Break Started");
//                            // we should not record any break started
//                            etAnnotation = "Break Ended";
//                        rules.setEldEvent(getActivity(), BusinessRules.EventCode.ON_BREAK_ENDED, annotationSubTitle, etAnnotation);
//                            rules.setEldEvent(getActivity(), BusinessRules.EventCode.ON_BREAK_ENDED, "Break Ended", etAnnotation);
                        } else {
//                            Log.d(TAG, "onClick: break: else part");
                            // 2022.03.22 we should check to see if we need to record an on duty first
                            BusinessRules.EventCode mostRecentDutyEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, sessionManagement);
                            if (mostRecentDutyEventCode == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
//                                Log.d(TAG, "onClick: break: mostRecentDutyEventCode: if: " + mostRecentDutyEventCode);
                                // don't do anything we shoudl record just brealk started
                            } else {
//                                Log.d(TAG, "onClick: break: mostRecentDutyEventCode: else: ");
                                // we should record a on duty first and after that we should record break started
//                            rules.setEldEventToOnDuty(getActivity());
                                rules.setEldEventToOnDuty(getActivity(), etAnnotation);
                                resetOdometerStart();

                            }

//                        March 22, 2022
//                        rules.setEldEvent(getActivity(), eventCode, annotationSubTitle, etAnnotation);
                            etAnnotation = "Break Started";
//                        rules.setEldEvent(getActivity(), BusinessRules.EventCode.ON_BREAK_STARTED, annotationSubTitle, etAnnotation);
                            rules.setEldEvent(getActivity(), BusinessRules.EventCode.ON_BREAK_STARTED, "Break Started", etAnnotation);

                            String deviceLatitude = "" + rules.getDeviceGPSLatitude();
                            String deviceLongitude = "" + rules.getDeviceGPSLongitude();
//                            Log.d(TAG, "recordBreak: deviceLatitude: " + deviceLatitude + " deviceLongitude: " + deviceLongitude);


                            if (deviceLatitude.equalsIgnoreCase("0.0") || deviceLongitude.equalsIgnoreCase("0.0")) {
//                                Log.d(TAG, "recordTruckLogDrivingParameters: latitude/longitude is 0.0");
                                if (mActivity != null) {
                                    if (!UiUtils.checkIsLocationEnabled(mActivity)) {
                                        if (!UiUtils.canGetLocation(mActivity)) {
                                            UiUtils.showSettingsAlert(mActivity);
                                        }
                                    } else {
                                        getDeviceLocation(mActivity, dialogTypeTitle);
                                    }
                                }
                            } else {

                                recordBreakContent();
                                if (dialogTypeTitle.equalsIgnoreCase("Break")) {
//                                    Log.d(TAG, "onClick: BreakEndedCheck: bt_annotation_save: showBreakLayout:true");
                                    showBreakLayout(true);
                                }
                            }

                        }
                    } else if (dialogTypeTitle.equalsIgnoreCase("Sleeper")) {
//                            BusinessRules.EventCode mostRecentDutyEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS);
                        etAnnotation = "Sleeper";
                        rules.setEldEvent(getActivity(), eventCode, annotationSubTitle, etAnnotation);
                        if (dialogTypeTitle.equalsIgnoreCase("Sleeper")) {
                            showSleeperLayout(true);
                        }

                    } else {
//                        March 22, 2022
                        rules.setEldEvent(getActivity(), eventCode, annotationSubTitle, etAnnotation);

//                        Sep 07, 2022  -   Reload the shift info(gauges) when we manually change the status
                        reloadShiftInfo();
                        resetOdometerStart();
                    }


                    if (isUpdateStatusRadioName)
                        updateStatusRadioName(annotationSubTitle);

                    unselectBottomRadioButtons();
                    isAnnotationPopupOpen = false;
                    updateWorkStatusForBottomBar();

//                    if (annotationSubTitle.equalsIgnoreCase("Sleeper")) {
//                        showSleeperLayout(true);
//                    }
                    if (DialogManager.d != null)
                        DialogManager.d.dismiss();
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
//                    Log.d(TAG, "onClick: throwable: save: " + throwable.getMessage());

                    if (DialogManager.d != null)
                        DialogManager.d.dismiss();
                }
            }
        });

        DialogManager.d.findViewById(R.id.bt_annotation_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    unselectBottomRadioButtons();
                    isAnnotationPopupOpen = false;
                    UiUtils.closeKeyboard(inputEditText);

                    if (DialogManager.d != null)
                        DialogManager.d.dismiss();
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
                }
            }
        });
    }

    protected void showLoginScreen(final MenuItem item) {
        DialogManager.d = UiUtils.showCustomLayoutDialog(getActivity(), R.layout.dialog_login);

        TextView tv = DialogManager.d.findViewById(R.id.tv_login_loginas);
        tv.setText("Login as: " + rules.getLastLoggedInUsername());

        TextView selectedRules = DialogManager.d.findViewById(R.id.selected_rules);
        selectedRules.setText("" + rules.getActiveRuleName());

        ArrayList<String> stateRules = rules.getStateRuleNames();

        ArrayAdapter<String> adapter = new ArrayAdapter(getActivity(), android.R.layout.simple_spinner_item, stateRules);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        Spinner s = DialogManager.d.findViewById(R.id.sp_login_rules);
        s.setAdapter(adapter);

        Integer activeRuleIndex = rules.getActiveRuleIndex();

        if (activeRuleIndex != null)
            s.setSelection(activeRuleIndex);

        DialogManager.d.findViewById(R.id.bt_login_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    rules.loginAuthenticatedUserAsDriver();

                    rules.setLastCtx(getActivity());
//                    Aug 10, 2022  -   Calling updateEldActionBarFlags so the disconnect icon turned into red if not connected with the eld
                    if (!rules.isBleConnected()) {
                        rules.startBleListener();
                        ((MainMenuActivity) getActivity()).updateEldActionBarFlags();
                    }

                    updateLoginView();

                    Spinner spinner = DialogManager.d.findViewById(R.id.sp_login_rules);
                    String ruleName = spinner.getSelectedItem().toString();

                    if (DialogManager.d != null)
                        DialogManager.d.dismiss();

//                    Log.d(TAG, "onClick: ruleName: " + ruleName);
                    rules.setSelectedRule(ruleName);

                    String loginAnnotation = "";
                    if (rules.isEldTesting()) {
                        loginAnnotation = "Login";
                    }

                    rules.setEldEvent(getActivity(), BusinessRules.EventType.A_DRIVER_LOGIN_LOGOUT_ACTIVITY, BusinessRules.EventCode.LOGIN,
                            BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Login", loginAnnotation, "", "");

//                    Aug 09, 2022  -   We should reset the odometer because a new shift might have been started
                    resetOdometerStart();

                    String truckLogHeaderMobileRecordId;
                    TruckLogHeader truckLogHeader = rules.getOpenTruckLogHeader();
//                    Log.d(TAG, "endCycle: onClick: truckLogHeader: " + truckLogHeader);
                    boolean newCycleStarted = false;

                    if (truckLogHeader == null) {
                        truckLogHeaderMobileRecordId = rules.createNewDriverTruckLogHeaderEntry();
                        newCycleStarted = true;
                    } else
                        truckLogHeaderMobileRecordId = truckLogHeader.MobileRecordId;
//                    Log.d(TAG, "endCycle: onClick: truckLogHeaderMobileRecordId: " + truckLogHeaderMobileRecordId);
//                    Log.d(TAG, "endCycle: onClick: " + rules.existsTruckEldHeaderEntry(truckLogHeaderMobileRecordId));
                    if (!rules.existsTruckEldHeaderEntry(truckLogHeaderMobileRecordId))
                        rules.createNewDriverTruckEldHeaderEntry(truckLogHeaderMobileRecordId);

                    String truckLogDetailMobileRecordId;
                    TruckLogDetail truckLogDetail = rules.getTruckLogDetailForToday(truckLogHeaderMobileRecordId, powerUnitNumber);
//                    Log.d(TAG, "endCycle: onClick: truckLogDetail: " + truckLogDetail);

                    if (truckLogDetail == null) {
                        truckLogDetailMobileRecordId = rules.createNewDriverTruckLogDetailEntry(truckLogHeaderMobileRecordId);
                        rules.setTruckLogHeaderEntryToUnsent(truckLogHeaderMobileRecordId);
                        truckLogDetail = rules.getTruckLogDetailForToday(truckLogHeaderMobileRecordId, powerUnitNumber);
                    } else
                        truckLogDetailMobileRecordId = truckLogDetail.MobileRecordId;

//                    Log.d(TAG, "endCycle: onClick: truckLogDetailMobileRecordId: " + truckLogDetailMobileRecordId);
//                    Log.d(TAG, "endCycle: onClick: rules.existsTruckEldDetailEntry: " + rules.existsTruckEldDetailEntry(truckLogDetailMobileRecordId));
                    if (!rules.existsTruckEldDetailEntry(truckLogDetailMobileRecordId, powerUnitNumber))
                        rules.createNewDriverTruckEldDetailEntry(getActivity(), truckLogHeaderMobileRecordId, truckLogDetailMobileRecordId, truckLogDetail);

//                    March 31, 2022    -   Why should we record a power on event here
//                    Log.d(TAG, "endCycle: onClick: rules.existsVehiclePowerSwitchEvent: " + rules.existsVehiclePowerSwitchEvent());
//                    if (rules.existsVehiclePowerSwitchEvent())
//                        rules.recordVehiclePowerSwitchEvent(getActivity(), rules.isDriverInDutyStatus());

//                  March 15, 2022    -   Should check if the previous state is On Duty or not
                    if (rules.getDutyEventCode() != BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {

//                        Log.d(TAG, "TestingDriving: endCycle: onClick: rules.getDutyEventCode(): " + rules.getDutyEventCode());

//                        May 05, 2022  -   We should use these annotations only for testing
                        String annotation = "";
                        if (rules.isEldTesting()) {
                            annotation = "DriveFragmentBase login";
                        }
                        Log.d(TAG, "onClick: setEldEvent: isNewCycle: " + newCycleStarted);
                        rules.setEldEvent(getActivity(), BusinessRules.EventCode.ON_DUTY_NOT_DRIVING, "On Duty", annotation);

//                        June 14, 2022 -       We need to update the driving status to onDuty when we automatically record OnDuty after login
                        drivingStatus = "On Duty";
                        updateStatusRadioName("On Duty");
                        unselectBottomRadioButtons();
//                        Log.d(TAG, "onClick: drivingStatus: " + drivingStatus);
                    }

                    calculateHours();
                    // 2022.08.19 reload the gauges
                    updateBottomSheetGaugesValues();
                    // 2022.08.19 forcely reload the gauges
                    updateStatusAndGauges();

                    UpdateTruckUserTask updateTruckUserTask = new UpdateTruckUserTask();
                    updateTruckUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

                } catch (Throwable throwable) {
                    Log.d(TAG, "endCycle: onClick: throwable: " + throwable.getMessage());
                    if (throwable != null)
                        throwable.printStackTrace();
                }

//                July 19, 2022 -   Whenever user login, and previous truckLogHeader is null, it creates a new one so after the login we check we cycle ended or not
                checkIsCycleEnded(1);
            }
        });

        DialogManager.d.findViewById(R.id.bt_login_other).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

//                    March 22, 2022    -    Fixed the bug regarding event type set to Duty Status instead of Login/Logout
//                    rules.setEldEvent(getActivity(), BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, BusinessRules.EventCode.LOGOUT,
//                            BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Logout", "Logout", "", "");

                    rules.setEldEvent(getActivity(), BusinessRules.EventType.A_DRIVER_LOGIN_LOGOUT_ACTIVITY, BusinessRules.EventCode.LOGOUT,
                            BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Logout", "Logout", "", "");

                    rules.logoutDriver();
                    updateLoginView();


                    getActivity().finish();
                    getActivity().finish();
                    startActivity(new Intent(getActivity(), com.rco.rcotrucks.activities.LoginActivity.class));
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
                }
            }
        });

        DialogManager.d.findViewById(R.id.bt_login_cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (DialogManager.d != null)
                        DialogManager.d.dismiss();
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
                }
            }
        });
    }

    String[] trailerList, trailerTwoList;
    Button powerUnitTrailerSaveButton, powerUnitTrailerCancelButton;
    AutoCompleteTextView powerUnitSpinner, trailerNumberSpinner, trailerNumberTwoSpinner, ruleSpinner;
    ImageView powerUnitDropDown, trailerNumberDropDown, trailerNumberTwoDropDown, ruleDropDown;
    TextView ruleTitle;
    LinearLayout ruleSpinnerLayout;
    boolean checkIsCycleEnded = false, isTruckLogDetailNull = false;

    protected void showPowerUnitTrailerNumberPromptPopup(Activity activity, final MenuItem item, boolean showLoginAfter) {
        DialogManager.d = UiUtils.showCustomLayoutDialog(activity, R.layout.dialog_powerunittrailer2);

        setDialogIds();
        initializeDialog();
        setDialogListeners(activity, item, showLoginAfter);
//        When trucklogheader or cycle ended then show the pop with rule - Save then don't show the other login dialog
//        When trucklogheader is active then check
//                If there IS truck log detail for today (for the current driver and current date) then simple login screen (second login dialog with 3 buttons )
//                 else (there is no truck log detail for today (for the current driver and current date) then show the popup without rule)


        ArrayAdapter powerUnitsAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, rules.getPowerUnitNumbers());
        powerUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        powerUnitSpinner.setThreshold(1);
        powerUnitSpinner.setAdapter(powerUnitsAdapter);

        powerUnitDropDown.setClickable(true);
        powerUnitDropDown.bringToFront();
        powerUnitDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.closeKeyboard(powerUnitDropDown);

                ArrayAdapter powerUnitsAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, rules.getPowerUnitNumbers());
                powerUnitsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                powerUnitSpinner.setThreshold(1);
                powerUnitSpinner.setAdapter(powerUnitsAdapter);

                if (powerUnitSpinner.isPopupShowing()) {
                    powerUnitSpinner.dismissDropDown();
                } else {
                    powerUnitSpinner.showDropDown();
                }
            }
        });

//            Sep 27, 2022  -   if we have last selected truck number then find its index and set its selection
//            on truck number adapter
//              But I have scene that setSelection is not working for power unit spinner but setting its using setText is better option for now
        Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: truckLogDetail: " + truckLogDetail);
        if (truckLogDetail != null) {
//            Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: activeTruckNumber: " + truckLogDetail.TruckNumber);
//            Integer activeTruckNumberIndex = rules.getActiveTruckNumberIndex(rules.getPowerUnitNumbers(), truckLogDetail.TruckNumber);
//            Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: activeTruckNumberIndex: " + activeTruckNumberIndex + " list:size: " + rules.getPowerUnitNumbers().length);
//            if (activeTruckNumberIndex != null && powerUnitSpinner.length() > activeTruckNumberIndex) {
//                Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: setSelection: ");
//                powerUnitSpinner.setSelection(activeTruckNumberIndex);
            powerUnitSpinner.setText(truckLogDetail.TruckNumber);
//            }
        }


//            Aug 30, 2022  -   Set Rule Adapter STARTS

        ArrayAdapter ruleAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, getResources().getStringArray(R.array.rule_list));
        ruleAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ruleSpinner.setThreshold(1);
        ruleSpinner.setAdapter(ruleAdapter);

//            Aug 30, 2022  -   This is the last selected Rule option
//        Integer activeRuleIndex = rules.getActiveRuleIndex();
        Integer activeRuleIndex = rules.getActiveRuleIndex(getResources().getStringArray(R.array.rule_list));
        Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: activeRuleIndex: " + activeRuleIndex);
        Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: ruleSpinner: lenght: " + ruleSpinner.length());
        if (activeRuleIndex != null && ruleSpinner.length() > activeRuleIndex)
            ruleSpinner.setSelection(activeRuleIndex);


//            Aug 30, 2022  -   Set Rule Adapter ENDS

//            Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: rules.getTrailers(): " + rules.getTrailers());
        ArrayAdapter trailersAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, rules.getTrailers());
        trailersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        trailerNumberSpinner.setThreshold(1);
        trailerNumberSpinner.setAdapter(trailersAdapter);

        trailerTwoList = rules.getTrailers();
        trailerNumberDropDown.setClickable(true);
        trailerNumberTwoDropDown.setClickable(true);
        trailerNumberDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.closeKeyboard(trailerNumberDropDown);

//                Sep 29, 2022  -   Added this new code so on dropdown click we always have all the trailer list values
//                rather than just the filtered values as per the entered value like
//                if we selects 3 and on below list will show only the trailers which starts with digit 3
//                But in user enters 3 and click on drop down, we are gonna show all the trailer list values
                trailerList = rules.getTrailers();
//                    July 08, 2022 -   We need to remove the trailer that user selected in trailer 1
                String[] tempList = updateTrailerList(trailerList, trailerNumberTwoSpinner.getText().toString());
                if (tempList != null) {
                    trailerList = tempList;
                }


                ArrayAdapter trailersAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, trailerList);
                trailersAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                trailerNumberSpinner.setThreshold(1);
                trailerNumberSpinner.setAdapter(trailersAdapter);

                if (trailerNumberSpinner.isPopupShowing()) {
                    trailerNumberSpinner.dismissDropDown();
                } else {
                    trailerNumberSpinner.showDropDown();
                }


            }
        });

//        Sep 27, 2022  -
        if (truckLogDetail != null) {
            trailerNumberSpinner.setText(truckLogDetail.Trailer1Number);
        }


        ruleDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ");
                ruleSpinner.showDropDown();
            }
        });


        trailerNumberTwoDropDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.closeKeyboard(trailerNumberTwoDropDown);

                trailerTwoList = rules.getTrailers();
//                    July 08, 2022 -   We need to remove the trailer that user selected in trailer 1
                String[] tempList = updateTrailerList(trailerTwoList, trailerNumberSpinner.getText().toString());
                if (tempList != null) {
                    trailerTwoList = tempList;
                }

                ArrayAdapter trailersTwoAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, trailerTwoList);
                trailersTwoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                trailerNumberTwoSpinner.setThreshold(1);
                trailerNumberTwoSpinner.setAdapter(trailersTwoAdapter);

                if (trailerNumberTwoSpinner.isPopupShowing()) {
                    trailerNumberTwoSpinner.dismissDropDown();
                } else {
                    trailerNumberTwoSpinner.showDropDown();
                }
            }
        });
        if (truckLogDetail != null) {
            trailerNumberTwoSpinner.setText(truckLogDetail.Trailer2Number);
        }

        ArrayList<String> truckAndTrailer = null;
        try {
            truckAndTrailer = rules.getAuthenticatedUserTruckAndTrailer();
        } catch (
                Throwable throwable) {
            Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: throwable: " + throwable.getMessage());
            if (throwable != null)
                throwable.printStackTrace();
        }

//        if (truckAndTrailer != null && truckAndTrailer.size() > 0) {
//            powerUnitSpinner.setText(truckAndTrailer.get(0));
//        }

        if (truckAndTrailer != null && truckAndTrailer.size() > 1) {
            trailerNumberSpinner.setText(truckAndTrailer.get(1));
//                July 19, 2022 -   Inside validation we applied a filter that trailer 1 and 2 cannot have same values so commented out trailer because
//                otherwise both gets the save value which fails when validate
//                Apart from this, trailer 2 is optional so letting its value as empty so user decides if he wanted to add the value or not
//                trailerNumberTwoSpinner.setText(truckAndTrailer.get(1));
        }

        powerUnitSpinner.dismissDropDown();
        powerUnitTrailerSaveButton.requestFocus();
    }

    //    Aug 30, 2022  -
    void applyLogin() {
        Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: applyLogin: ");

        rules.loginAuthenticatedUserAsDriver();

        rules.setLastCtx(getActivity());
//                    Aug 10, 2022  -   Calling updateEldActionBarFlags so the disconnect icon turned into red if not connected with the eld
        if (!rules.isBleConnected()) {
            rules.startBleListener();
            ((MainMenuActivity) getActivity()).updateEldActionBarFlags();
        }

        updateLoginView();

        String loginAnnotation = "";
        if (rules.isEldTesting()) {
            loginAnnotation = "Login";
        }

        rules.setEldEvent(getActivity(), BusinessRules.EventType.A_DRIVER_LOGIN_LOGOUT_ACTIVITY, BusinessRules.EventCode.LOGIN,
                BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Login", loginAnnotation, "", "");

//                    Aug 09, 2022  -   We should reset the odometer because a new shift might have been started
        resetOdometerStart();

        String truckLogHeaderMobileRecordId;
        TruckLogHeader truckLogHeader = rules.getOpenTruckLogHeader();
//                    Log.d(TAG, "endCycle: onClick: truckLogHeader: " + truckLogHeader);
        boolean newCycleStarted = false;

        if (truckLogHeader == null) {
            truckLogHeaderMobileRecordId = rules.createNewDriverTruckLogHeaderEntry();
            newCycleStarted = true;
        } else
            truckLogHeaderMobileRecordId = truckLogHeader.MobileRecordId;
//                    Log.d(TAG, "endCycle: onClick: truckLogHeaderMobileRecordId: " + truckLogHeaderMobileRecordId);
//                    Log.d(TAG, "endCycle: onClick: " + rules.existsTruckEldHeaderEntry(truckLogHeaderMobileRecordId));
        if (!rules.existsTruckEldHeaderEntry(truckLogHeaderMobileRecordId))
            rules.createNewDriverTruckEldHeaderEntry(truckLogHeaderMobileRecordId);

        String truckLogDetailMobileRecordId;
        TruckLogDetail truckLogDetail = rules.getTruckLogDetailForToday(truckLogHeaderMobileRecordId, powerUnitNumber);
//                    Log.d(TAG, "endCycle: onClick: truckLogDetail: " + truckLogDetail);

        if (truckLogDetail == null) {
            truckLogDetailMobileRecordId = rules.createNewDriverTruckLogDetailEntry(truckLogHeaderMobileRecordId);
            rules.setTruckLogHeaderEntryToUnsent(truckLogHeaderMobileRecordId);
            truckLogDetail = rules.getTruckLogDetailForToday(truckLogHeaderMobileRecordId, powerUnitNumber);
        } else
            truckLogDetailMobileRecordId = truckLogDetail.MobileRecordId;

//                    Log.d(TAG, "endCycle: onClick: truckLogDetailMobileRecordId: " + truckLogDetailMobileRecordId);
//                    Log.d(TAG, "endCycle: onClick: rules.existsTruckEldDetailEntry: " + rules.existsTruckEldDetailEntry(truckLogDetailMobileRecordId));
        if (!rules.existsTruckEldDetailEntry(truckLogDetailMobileRecordId, powerUnitNumber))
            rules.createNewDriverTruckEldDetailEntry(getActivity(), truckLogHeaderMobileRecordId, truckLogDetailMobileRecordId, truckLogDetail);

//                    March 31, 2022    -   Why should we record a power on event here
//                    Log.d(TAG, "endCycle: onClick: rules.existsVehiclePowerSwitchEvent: " + rules.existsVehiclePowerSwitchEvent());
//                    if (rules.existsVehiclePowerSwitchEvent())
//                        rules.recordVehiclePowerSwitchEvent(getActivity(), rules.isDriverInDutyStatus());

//                  March 15, 2022    -   Should check if the previous state is On Duty or not
        if (rules.getDutyEventCode() != BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {

//                        Log.d(TAG, "TestingDriving: endCycle: onClick: rules.getDutyEventCode(): " + rules.getDutyEventCode());

//                        May 05, 2022  -   We should use these annotations only for testing
            String annotation = "";
            if (rules.isEldTesting()) {
                annotation = "DriveFragmentBase login";
            }
            Log.d(TAG, "showPowerUnitTrailerNumberPromptPopup: onClick: setEldEvent: isNewCycle: " + newCycleStarted);
            rules.setEldEvent(getActivity(), BusinessRules.EventCode.ON_DUTY_NOT_DRIVING, "On Duty", annotation);

//                        June 14, 2022 -       We need to update the driving status to onDuty when we automatically record OnDuty after login
            drivingStatus = "On Duty";
            updateStatusRadioName("On Duty");
            unselectBottomRadioButtons();
//                        Log.d(TAG, "onClick: drivingStatus: " + drivingStatus);
        }

        //
        calculateHours();
        // 2022.08.19 reload the gauges
        updateBottomSheetGaugesValues();
        // 2022.08.19 forcely reload the gauges
        updateStatusAndGauges();

        UpdateTruckUserTask updateTruckUserTask = new UpdateTruckUserTask();
        updateTruckUserTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

//                July 19, 2022 -   Whenever user login, and previous truckLogHeader is null, it creates a new one so after the login we check we cycle ended or not
        checkIsCycleEnded(1);


//    Dec 22, 2022  -   The D(Diagnostic) is only visible when driver logged in but its widget was not updated when user logged in so
//        makes sure that when apply login completes we should update the app bar for DM too
        ((MainMenuActivity) getActivity()).updateEldActionBarFlags();
    }

    void setDialogIds() {

        powerUnitTrailerSaveButton = DialogManager.d.findViewById(R.id.bt_powerunittrailer_save);
        powerUnitTrailerCancelButton = DialogManager.d.findViewById(R.id.bt_powerunittrailer_cancel);
        powerUnitSpinner = DialogManager.d.findViewById(R.id.powerunit_spinner);
        trailerNumberSpinner = DialogManager.d.findViewById(R.id.trailernumber_spinner);
        trailerNumberTwoSpinner = DialogManager.d.findViewById(R.id.trailernumber_two_spinner);

        powerUnitDropDown = DialogManager.d.findViewById(R.id.ic_powerunit_dropdown);
        trailerNumberDropDown = DialogManager.d.findViewById(R.id.ic_trailernumber_dropdown);
        trailerNumberTwoDropDown = DialogManager.d.findViewById(R.id.ic_trailernumber_two_dropdown);

        ruleTitle = DialogManager.d.findViewById(R.id.ruleTitle);
        ruleSpinnerLayout = DialogManager.d.findViewById(R.id.ruleSpinnerLayout);
        ruleSpinner = DialogManager.d.findViewById(R.id.rule_spinner);
        ruleDropDown = DialogManager.d.findViewById(R.id.ic_rule_dropdown);
    }

    void initializeDialog() {
        TruckLogHeader truckLogHeader = rules.getMostRecentTruckLogHeader();

//        Aug 30, 2022  -   We will only show the rule Spinner if the current cycle is ended and we will know it by getting
//        truckLogHeader detail
        if (truckLogHeader != null && truckLogHeader.EndDate != null && truckLogHeader.EndTime != null && !truckLogHeader.EndDate.isEmpty() && !truckLogHeader.EndTime.isEmpty()) {
            checkIsCycleEnded = true;
            ruleTitle.setVisibility(VISIBLE);
            ruleSpinnerLayout.setVisibility(VISIBLE);
            ruleSpinner.setVisibility(VISIBLE);
            ruleDropDown.setVisibility(VISIBLE);
        }

    }

    String powerUnitNumber, trailerNumber, trailerNumberTwo, selectedRule;
    boolean isActiveTruckAndTrailerNumbersSimilar = false;

    void setDialogListeners(Activity a, MenuItem item, boolean showLoginAfter) {

        powerUnitTrailerSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initializeLoginDialog();
                if (!validateLoginDialog()) {
                } else {
                    applyImplementation(item, showLoginAfter);
                }
            }
        });

        powerUnitTrailerCancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DialogManager.d != null)
                    DialogManager.d.dismiss();
            }
        });
    }

    void initializeLoginDialog() {
        powerUnitNumber = powerUnitSpinner.getText().toString();
        trailerNumber = trailerNumberSpinner.getText().toString();
        trailerNumberTwo = trailerNumberTwoSpinner.getText().toString();
        selectedRule = ruleSpinner.getText().toString();
    }

    boolean validateLoginDialog() {

        Log.d(TAG, "validateLoginDialog: powerUnitNumber: " + powerUnitNumber);
        Log.d(TAG, "validateLoginDialog: trailerNumber: 1: " + trailerNumber);
        Log.d(TAG, "validateLoginDialog: trailerNumber: 2: " + trailerNumberTwo);
        Log.d(TAG, "validateLoginDialog: truckNumber: " + rules.isTruckNumberUnknown(powerUnitNumber));

        if (StringUtils.isNullOrWhitespaces(powerUnitNumber) || rules.isTruckNumberUnknown(powerUnitNumber)) {
//            Nov 17, 2022  -   We should replace power unit number with truck number
//            UiUtils.showToast(getActivity(), "Please enter the Power Unit Number");
            UiUtils.showToast(getActivity(), "Please enter valid Truck Number");
            return false;
        } else if (!rules.existsTruckNumber(powerUnitNumber)) {
            UiUtils.showToast(getActivity(), "Invalid Truck Number, please enter a valid Truck Number");
            return false;
        } else if ((!trailerNumber.equalsIgnoreCase("No Trailer")
                && (!trailerNumberTwo.equalsIgnoreCase("No Trailer"))
                && trailerNumber.equalsIgnoreCase(trailerNumberTwo))) {
            Toast.makeText(getActivity(), "Trailer 1 and 2 cannot have same number", Toast.LENGTH_SHORT).show();
            return false;
        } else if (checkIsCycleEnded && (selectedRule.isEmpty() || selectedRule.equalsIgnoreCase("Unknown"))) {
            Toast.makeText(getActivity(), "Please select rule", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }


    //    Sep 29, 2022  -   Created this method as per code refactor
    void applyImplementation(MenuItem item, boolean showLoginAfter) {

        Log.d(TAG, "Settings: applyImplementation: call method to update active rule");
        rules.updatePowerUnitAndTrailerNumber(powerUnitNumber, selectedRule, trailerNumber, trailerNumberTwo);
        rules.setSelectedRule(selectedRule);
        Log.d(TAG, "Settings: applyImplementation: active rule set: ");

        Log.d(TAG, "Settings: applyImplementation: ");
//            Note:     powerUnitNumber is the selected truck number spinner
//            Don't know who name it like this
//            TODO replace powerUnitNumber with proper semantic name
        if ((truckLogDetail == null) || (truckLogDetail.TruckNumber != null && !truckLogDetail.TruckNumber.equalsIgnoreCase(powerUnitNumber))) {
//                    lets create a new truck log detail for today with new truck number and what ever
//                    the values we have for trailer 1 and 2
            Log.d(TAG, "Settings: applyImplementation: trucknumber is different");
            boolean isValuesUpdated = rules.updateTruckAndTrailersInformation(powerUnitNumber, trailerNumber, trailerNumberTwo);
            Log.d(TAG, "Settings: applyImplementation: isValueUpdated: " + isValuesUpdated);
            if (isValuesUpdated) {
                rules.createTruckLogRecords(getActivity());
                recordAndResetEventsAfterNewTruckLogRecordCreated();
            }
        } else if (truckLogDetail.Trailer1Number != null && !truckLogDetail.Trailer1Number.equalsIgnoreCase(trailerNumber)
                && truckLogDetail.Trailer2Number != null && !truckLogDetail.Trailer2Number.equalsIgnoreCase(trailerNumberTwo)) {
//                    we need to create two drops for last changes values and two pickups for each new selected value
//                    one for trailer 1 and one for trailer 2

            createTrailerLog(truckLogDetail.Trailer1Number, "drop", powerUnitNumber);
            createTrailerLog(truckLogDetail.Trailer2Number, "drop", powerUnitNumber);
            createTrailerLog(trailerNumber, "pickup", powerUnitNumber);
            createTrailerLog(trailerNumberTwo, "pickup", powerUnitNumber);
        } else if (truckLogDetail.Trailer1Number != null && !truckLogDetail.Trailer1Number.equalsIgnoreCase(trailerNumber)) {
//                      we need to create one drops for last selected value for trailer 1 and
//                      similarly one pickup for each new trailer selected value
            createTrailerLog(truckLogDetail.Trailer1Number, "drop", powerUnitNumber);
            createTrailerLog(trailerNumber, "pickup", powerUnitNumber);
        } else if (truckLogDetail.Trailer2Number != null && !truckLogDetail.Trailer2Number.equalsIgnoreCase(trailerNumberTwo)) {
//                      we need to create one drops for last selected value for trailer 2 and
//                      similarly one pickup for each new trailer 2 selected value
            createTrailerLog(truckLogDetail.Trailer2Number, "drop", powerUnitNumber);
            createTrailerLog(trailerNumberTwo, "pickup", powerUnitNumber);
        } else {
            isActiveTruckAndTrailerNumbersSimilar = true;
        }

//        Log.d(TAG, "Settings: applyImplementation: call method to update active rule");
//        rules.updatePowerUnitAndTrailerNumber(powerUnitNumber, selectedRule, trailerNumber, trailerNumberTwo);
//        rules.setSelectedRule(selectedRule);
//        Log.d(TAG, "Settings: applyImplementation: active rule set: ");

        if (DialogManager.d != null)
            DialogManager.d.dismiss();

//                    March 31, 2022    -   We should open the relevant dialog in case of onDuty
        Log.d(TAG, "Settings: applyImplementation: showPowerUnitTrailerNumberPromptPopup: onClick: showLoginAfter: " + showLoginAfter);
        boolean isNewTruckLogCreated = false;


        if (showLoginAfter) {
//                            Refine Comments
//                        Aug 30, 2022  -
//                        First check is current cycle ended?
//                          If yes then show the the dialog for detail with rule spinner and don't show the other dialog after
//                          If no then first check do we have "trucklogdetail" of current driver for today
//                                      If yes then just show the second dialog with three buttons login - other and cancel
//                                      If no then just show the first dialog without rule spinner options
//            if (checkIsCycleEnded || isTruckLogDetailNull || isActiveTruckAndTrailerNumbersSimilar) {
            applyLogin();
//            } else {
//                showLoginScreen(item);
//            }
        } else {
            isNewTruckLogCreated = rules.createTruckLogRecords(mActivity);
            Log.d(TAG, "Settings: applyImplementation: showPowerUnitTrailerNumberPromptPopup: onClick: isEldTesting: " + rules.isEldTesting() + " isNewTruckLogCreated: " + isNewTruckLogCreated);
            recordAndResetEventsAfterNewTruckLogRecordCreated();
        }

    }

    void recordAndResetEventsAfterNewTruckLogRecordCreated() {
        Log.d(TAG, "applyImplementation: recordAndResetEventsAfterNewTruckLogRecordCreated: ");
//          July 14, 2022 -   We should record the onDuty event only if already not on onDuty
//          (For drivers that are beginners we are recording OnDuty when they start the engine)
        if (rules.getDutyEventCode() != BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
            if (rules.isEldTesting()) {
                rules.setEldEventToOnDuty(mActivity, "DriveFragmentBase: On Duty");
            } else {
                rules.setEldEventToOnDuty(mActivity, "On Duty");
            }
            resetOdometerStart();
        }

        updateStatusRadioName("On Duty");
        unselectBottomRadioButtons();
    }

    void createNewTruckLogDetailForToday() {

    }

    String[] updateTrailerList(String[] trailerList, String selectedTrailerOne) {
//        Log.d(TAG, "updateTrailerList: selectedTrailerOne: " + selectedTrailerOne + " trailerTwoList: " + trailerTwoList);
        if (trailerList != null) {
//            Log.d(TAG, "updateTrailerList: trailerTwoList: length: " + trailerTwoList.length);
            for (int i = 0; i < trailerList.length; i++) {
//                Log.d(TAG, "updateTrailerList: selectedTrailerOne: " + trailerTwoList[i]);
                if (selectedTrailerOne.equalsIgnoreCase(trailerList[i])
                        && !selectedTrailerOne.equalsIgnoreCase("No Trailer")
                        && !selectedTrailerOne.equalsIgnoreCase("Unknown")) {

                    List<String> list = new ArrayList<String>(Arrays.asList(trailerList));
//                    Log.d(TAG, "updateTrailerList: list: " + list.size());
                    list.remove(selectedTrailerOne);
                    trailerList = list.toArray(new String[0]);
//                    Log.d(TAG, "updateTrailerList: trailerTwoList: " + trailerTwoList.length);

                    return trailerList;
                }
            }
        }
        return null;
    }

    private void setupSharePopup() {

        setSharePopupIds();
        initializeSharePopup();
        setListenerSharePopup();
    }

    ConstraintLayout popupShareLayout;
    ImageView closeSharePopup;
    TextView endCycleSharePopup, logDataSharePopup, violationsSharePopup, dropTrailerSharePopup,
            pickupTrailerSharePopup, btnExemptRoad, showHideGasStation, showHideRestStation;

    void setSharePopupIds() {
        popupShareLayout = mView.findViewById(R.id.popup_share_layout);
        btnExemptRoad = mView.findViewById(R.id.btn_exempt_road);
        showHideGasStation = mView.findViewById(R.id.btn_Hide_gas_station);
        showHideRestStation = mView.findViewById(R.id.btn_Hide_rest_areas);

        closeSharePopup = mView.findViewById(R.id.close_share_popup);
        endCycleSharePopup = mView.findViewById(R.id.btn_end_cycle);
        logDataSharePopup = mView.findViewById(R.id.btn_log_data);
        violationsSharePopup = mView.findViewById(R.id.btn_violations);
        dropTrailerSharePopup = mView.findViewById(R.id.dropTrailer);
        pickupTrailerSharePopup = mView.findViewById(R.id.pickupTrailer);
    }

    void initializeSharePopup() {
        setSharePopup(true);
        showHideGasStation.setText(isGasStation ? "Hide Gas Stations" : "Show Gas Stations");
        showHideRestStation.setText(isRestStation ? "Hide Rest Areas" : "Show Rest Areas");
        btnExemptRoad.setText(rules.isTollRoadEventStarted() ? "Stop Exempt Road" : "Start Exempt Road");
    }

    void setListenerSharePopup() {

        popupShareLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSharePopup(false);
            }
        });

        closeSharePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSharePopup(false);
            }
        });

        endCycleSharePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endCycle();
                setSharePopup(false);
            }
        });

        btnExemptRoad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    if (rules.isTollRoadEventStarted()) {
                        // BusHelperIfta.checkForIftaEvent(getActivity(), BusHelperIfta.EVENT_EXIT_NON_IFTA_ROAD);
                        rules.setTollRoadEventToStarted(false);
                        btnExemptRoad.setText("Start Exempt Road");
                    } else {
                        //  BusHelperIfta.checkForIftaEvent(getActivity(), BusHelperIfta.EVENT_ENTER_NON_IFTA_ROAD);
                        rules.setTollRoadEventToStarted(true);
                        btnExemptRoad.setText("Stop Exempt Road");
                    }
                } catch (Throwable throwable) {
                    if (throwable != null) {
                        throwable.printStackTrace();
                        UiUtils.showExclamationDialog(getActivity(), "Error", throwable.getMessage());
                    }
                }

                setSharePopup(false);
            }
        });

        logDataSharePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "sendLogData: onClick: ");

                String subject = "Log Data";
                String body = "";

                for (int i = 0; i < ((MainMenuActivity) getActivity()).logDataArrayList.size(); i++) {

                    body = body + ((MainMenuActivity) getActivity()).logDataArrayList.get(i);
                    body = body + "\n ---------------------------- \n ";
                }

//                Log.d(TAG, "sendLogData: onClick: onClick: sendLogData: body: " + body);

                if (((MainMenuActivity) getActivity()).logDataArrayList.size() > 0) {
//                    July 15, 2022 -   added new method to already include recipient email because in the previous code - recipient was empty
                    shareTextToEmail(getContext(), new String[]{"dragos.bodnar@gmail.com"}, subject, body);
                } else {
                    Toast.makeText(getActivity(), "No info to send.", Toast.LENGTH_SHORT).show();
                }
                setSharePopup(false);
            }
        });

        showHideGasStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideGasStation();
                setSharePopup(false);
            }
        });

        showHideRestStation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showHideRestStation();
                setSharePopup(false);
            }
        });


        violationsSharePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSharePopup(false);
                Toast.makeText(getActivity(), "Not implemented yet", Toast.LENGTH_SHORT).show();
            }
        });

        dropTrailerSharePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSharePopup(false);
                openDropDialog();
            }
        });

        pickupTrailerSharePopup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openPickupDialog();
            }
        });
    }

    void setSharePopup(boolean show) {
        if (show) {
            popupShareLayout.setVisibility(VISIBLE);
        } else {
            popupShareLayout.setVisibility(GONE);
        }
    }

    public static void shareTextToEmail(Context context, String[] email, String subject, String text) {
        Intent selectorIntent = new Intent(Intent.ACTION_SENDTO);
        selectorIntent.setData(Uri.parse("mailto:"));

        final Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, text);
        emailIntent.setSelector(selectorIntent);
        context.startActivity(emailIntent);
    }

    public void setupStatusPopup() {
        try {
            if (!rules.isIndicationOfAuthorizedPersonalUseOnDuty()) {
                unselectBottomRadioButtons();
                UiUtils.showToastLong(getActivity(), "Please first change to On Duty from the Work menu option");
                return;
            }

            if (!rules.isDriverLoggedIn()) {
                unselectBottomRadioButtons();
                UiUtils.showToast(getActivity(), "Please first login");
                return;
            }

            isStatusDialogShown = true;
            DialogManager.d = UiUtils.showCustomLayoutDialog(getActivity(), R.layout.dialog_selection);
            DutyEventsAdapter adapter = new DutyEventsAdapter(getActivity(), rules.getDutyStatues(sessionManagement),
                    new DutyEventsAdapter.ItemClickInterface() {
                        @Override
                        public void selectedDutyEvent(String dutyEvent) {
                            DialogManager.d.dismiss();
                            handleChangeInEventCode(dutyEvent);
                        }
                    });

            DialogManager.d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    Log.d(TAG, "onDismiss: ");
                    try {
                        isStatusDialogShown = false;
                        if (!isAnnotationPopupOpen)
                            unselectBottomRadioButtons();
                    } catch (Throwable throwable) {
                        if (throwable != null)
                            throwable.printStackTrace();
                    }
                }
            });

            Button btnCancel = DialogManager.d.findViewById(R.id.bt_cancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogManager.d.dismiss();
                    try {
                        unselectBottomRadioButtons();

                        if (DialogManager.d != null)
                            DialogManager.d.dismiss();
                    } catch (Throwable throwable) {
                        if (throwable != null) {
                            throwable.printStackTrace();
                            UiUtils.showExclamationDialog(getActivity(), "Error", throwable.getMessage());
                        }
                    }
                }
            });

            ListView selectionList = DialogManager.d.findViewById(R.id.lv_selection_list);

            if (selectionList != null) {
                selectionList.setAdapter(adapter);
//                selectionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                        try {
//                            if (DialogManager.d != null)
//                                DialogManager.d.dismiss();
//
//
//                            if (position == 0) {
//                                if (rules.getLastEldEventCode() == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING)
//                                    return;
//
////                    March 31, 2022    -   We should check if record already exists if no then open dialog for truck and trailor selection
//
//                                if (rules.existsTruckLogRecords()) {
//                                    showAnnotationPopup("On Duty", BusinessRules.EventCode.ON_DUTY_NOT_DRIVING, true);
//                                } else {
//                                    showPowerUnitTrailerNumberPromptPopup(getActivity(), null, false);
//                                }
//
//                            } else if (position == 1) {
//                                if (rules.getLastEldEventCode() == BusinessRules.EventCode.OFF_DUTY)
//                                    return;
//
//                                showAnnotationPopup("Off Duty", BusinessRules.EventCode.OFF_DUTY, true);
//                            } else if (position == 2) {
//                                if (rules.getLastEldEventCode() == BusinessRules.EventCode.SLEEPER_BERTH)
//                                    return;
//
//                                showAnnotationPopup("Sleeper", BusinessRules.EventCode.SLEEPER_BERTH, true);
//                            } else if (position == 3) {
//
////                                April 22, 2022    -   This should not be called anymore  because now we are showing the counter for break
////                                And its related with previous layout
//                                if (rules.getLastEldEventCode() == BusinessRules.EventCode.ON_BREAK_STARTED) {
////                                    Log.d(TAG, "onItemClick: EventCode: " + BusinessRules.EventCode.ON_BREAK_STARTED);
//                                    showAnnotationPopup("Break", BusinessRules.EventCode.ON_BREAK_ENDED, true);
//                                    return;
//                                }
//
//                                // 2022.03.22 showAnnotationPopup("Break", BusinessRules.EventCode.ON_BREAK, true);
//                                showAnnotationPopup("Break", BusinessRules.EventCode.ON_BREAK_STARTED, true);
//                            } else if (position == 4 && rules.isDebugMode()) {
////                            } else if (position == 4) {
//                                if (rules.getLastEldEventCode() == BusinessRules.EventCode.DRIVING)
//                                    return;
//
//                                UiUtils.showToast(getActivity(), "Driving...");
////                                Log.d(TAG, "TestingDriving: onItemClick: setEldEvent: Driving");
//                                rules.setEldEvent(getActivity(), BusinessRules.EventCode.DRIVING, "Driving", "Driving");
//                                updateStatusRadioName("Driving");
//                                unselectBottomRadioButtons();
//                            }
//                        } catch (Throwable throwable) {
//                            Log.d(TAG, "onItemClick: throwable: " + throwable.getMessage());
//                            if (throwable != null) {
//                                throwable.printStackTrace();
//                                UiUtils.showExclamationDialog(getActivity(), "Error", throwable.getMessage());
//                            }
//                        }
//                    }
//                });
            }
        } catch (Throwable throwable) {
//            Log.d(TAG, "setupStatusPopup: throwable: " + throwable.getMessage());
            if (throwable != null) {
                throwable.printStackTrace();
                UiUtils.showExclamationDialog(getActivity(), "Error", throwable.getMessage());
            }
        }
    }

    void handleChangeInEventCode(String eventCode) {
        Log.d(TAG, "handleChangeInEventCode: eventCode: " + eventCode);

        if (eventCode.equalsIgnoreCase("On Duty")) {
            if (rules.getLastEldEventCode() == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING)
                return;

//                    March 31, 2022    -   We should check if record already exists if no then open dialog for truck and trailor selection
            if (rules.existsTruckLogRecords()) {
                showAnnotationPopup("On Duty", BusinessRules.EventCode.ON_DUTY_NOT_DRIVING, true);
            } else {
                showPowerUnitTrailerNumberPromptPopup(getActivity(), null, false);
            }
        } else if (eventCode.equalsIgnoreCase("Off Duty")) {
            if (rules.getLastEldEventCode() == BusinessRules.EventCode.OFF_DUTY)
                return;

            showAnnotationPopup("Off Duty", BusinessRules.EventCode.OFF_DUTY, true);
        } else if (eventCode.equalsIgnoreCase("Sleeper") || eventCode.equalsIgnoreCase("Sleeper Berth")) {
            if (rules.getLastEldEventCode() == BusinessRules.EventCode.SLEEPER_BERTH)
                return;

            showAnnotationPopup("Sleeper", BusinessRules.EventCode.SLEEPER_BERTH, true);
        } else if (eventCode.equalsIgnoreCase("Break")) {

//                                April 22, 2022    -   This should not be called anymore  because now we are showing the counter for break
//                                And its related with previous layout
            if (rules.getLastEldEventCode() == BusinessRules.EventCode.ON_BREAK_STARTED) {
                showAnnotationPopup("Break", BusinessRules.EventCode.ON_BREAK_ENDED, true);
                return;
            }

            // 2022.03.22 showAnnotationPopup("Break", BusinessRules.EventCode.ON_BREAK, true);
            showAnnotationPopup("Break", BusinessRules.EventCode.ON_BREAK_STARTED, true);
        } else if (eventCode.equalsIgnoreCase("Driving") && rules.isDebugMode()) {
            if (rules.getLastEldEventCode() == BusinessRules.EventCode.DRIVING)
                return;

            UiUtils.showToast(getActivity(), "Driving...");
            rules.setEldEvent(getActivity(), BusinessRules.EventCode.DRIVING, "Driving", "Driving");
            updateStatusRadioName("Driving");
            unselectBottomRadioButtons();
        } else if (eventCode.equalsIgnoreCase("Personal Conveyance (Off Duty)")) {

            if (!rules.isDebugMode() && !rules.isDriverDutyStatusChangable()) {
                UiUtils.showToast(getActivity(), "Not enough permission to change work status");
                return;
            }

            if (rules.getDutyEventCode() == BusinessRules.EventCode.OFF_DUTY) {
                showAnnotationPopup("Authorized Personal Use Of CMV", BusinessRules.EventCode.AUTHORIZED_PERSIONAL_USE_OF_CMV, false);
            } else {
                Toast.makeText(mActivity, "Please change the driver status to Off Duty first.", Toast.LENGTH_LONG).show();
            }
            updateWorkStatusForBottomBar();
        } else if (eventCode.equalsIgnoreCase("Yard Moves")) {

            if (!rules.isDebugMode() && !rules.isDriverDutyStatusChangable()) {
                UiUtils.showToast(getActivity(), "Not enough permission to change work status");
                return;
            }

            if (rules.getDutyEventCode() == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
                showAnnotationPopup("Yard Moves", BusinessRules.EventCode.YARD_MOVES, false);
            } else {
                Toast.makeText(mActivity, "Please change the driver status to On Duty first.", Toast.LENGTH_LONG).show();
            }
            updateWorkStatusForBottomBar();
        } else if (eventCode.equalsIgnoreCase("Work (On Duty)")) {

            if (!rules.isDebugMode() && !rules.isDriverDutyStatusChangable()) {
                UiUtils.showToast(getActivity(), "Not enough permission to change work status");
                return;
            }

            if (rules.getDutyEventCode() == BusinessRules.EventCode.YARD_MOVES || rules.getDutyEventCode() == BusinessRules.EventCode.AUTHORIZED_PERSIONAL_USE_OF_CMV) {
                showAnnotationPopup("Clear Personal Use or Yard Moves", BusinessRules.EventCode.CLEARED_PC_YM_WT, false);
            } else {
                Toast.makeText(mActivity, "Current status is not personal conveyance or yard moves.", Toast.LENGTH_LONG).show();
            }
        }
    }

    protected void setupWorkPopup() {
        try {
            DialogManager.d = UiUtils.showCustomLayoutDialog(getActivity(), R.layout.dialog_selection);
            ((TextView) DialogManager.d.findViewById(R.id.tv_selection_title)).setText("Driver Duty Status");

            DialogManager.d.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    try {
                        if (!isAnnotationPopupOpen) {
                            unselectBottomRadioButtons();
                        }
                    } catch (Throwable throwable) {
                        if (throwable != null)
                            throwable.printStackTrace();
                    }
                }
            });

//            DutyEventsAdapter adapter = new DutyEventsAdapter(getActivity(), rules.getDriverDutySubStatuses());
            DutyEventsAdapter adapter = new DutyEventsAdapter(getActivity(), rules.getDriverDutySubStatuses(),
                    new DutyEventsAdapter.ItemClickInterface() {
                        @Override
                        public void selectedDutyEvent(String dutyEvent) {
                            DialogManager.d.dismiss();
                            handleChangeInEventCode(dutyEvent);
                        }
                    });

            Button btnCancel = DialogManager.d.findViewById(R.id.bt_cancel);
            btnCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogManager.d.dismiss();
                    try {
                        unselectBottomRadioButtons();

                        if (DialogManager.d != null)
                            DialogManager.d.dismiss();
                    } catch (Throwable throwable) {
                        if (throwable != null) {
                            throwable.printStackTrace();
                            UiUtils.showExclamationDialog(getActivity(), "Error", throwable.getMessage());
                        }
                    }
                }
            });

            ListView selectionList = DialogManager.d.findViewById(R.id.lv_selection_list);

            if (selectionList != null) {
                selectionList.setAdapter(adapter);
//                selectionList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
//                    @Override
//                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
////                        try {
////                        Log.d(TAG, "onItemClick: position: " + position);
//                        if (!rules.isDebugMode() && !rules.isDriverDutyStatusChangable()) {
//                            UiUtils.showToast(getActivity(), "Not enough permission to change work status");
//                            return;
//                        }
//
//                        if (DialogManager.d != null)
//                            DialogManager.d.dismiss();
//
//                        if (position == 0) {
////                                if (rules.getLastEldEventCode() == BusinessRules.EventCode.AUTHORIZED_PERSIONAL_USE_OF_CMV)
////                                    return;
//
////                            Log.d(TAG, "onItemClick: currentStatus: " + rules.getDutyEventCode());
//                            if (rules.getDutyEventCode() == BusinessRules.EventCode.OFF_DUTY) {
//                                showAnnotationPopup("Authorized Personal Use Of CMV", BusinessRules.EventCode.AUTHORIZED_PERSIONAL_USE_OF_CMV, false);
//                            } else {
//                                Toast.makeText(mActivity, "Please change the driver status to Off Duty first.", Toast.LENGTH_LONG).show();
//                            }
//                            updateWorkStatusForBottomBar();
//                        } else if (position == 1) {
////                                if (rules.getLastEldEventCode() == BusinessRules.EventCode.YARD_MOVES)
////                                    return;
//
////                            Log.d(TAG, "onItemClick: currentStatus: " + rules.getDutyEventCode());
//                            if (rules.getDutyEventCode() == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
//                                showAnnotationPopup("Yard Moves", BusinessRules.EventCode.YARD_MOVES, false);
//                            } else {
//                                Toast.makeText(mActivity, "Please change the driver status to On Duty first.", Toast.LENGTH_LONG).show();
//                            }
//
//                            updateWorkStatusForBottomBar();
//                        } else if (position == 2) {
////                                if (rules.getLastEldEventCode() == BusinessRules.EventCode.CLEARED_PC_YM_WT)
////                                    return;
//
////                            May 20, 2022  -   We should show the popup only on yard moves or personal conveyance
//                            if (rules.getDutyEventCode() == BusinessRules.EventCode.YARD_MOVES || rules.getDutyEventCode() == BusinessRules.EventCode.AUTHORIZED_PERSIONAL_USE_OF_CMV) {
//
//                                showAnnotationPopup("Clear Personal Use or Yard Moves", BusinessRules.EventCode.CLEARED_PC_YM_WT, false);
//                            } else {
//                                Toast.makeText(mActivity, "Current status is not personal conveyance or yard moves.", Toast.LENGTH_LONG).show();
//                            }
//
//                        }
//
//                    }
//                });
            }
        } catch (Throwable throwable) {

            if (throwable != null) {
                throwable.printStackTrace();
                UiUtils.showExclamationDialog(getActivity(), "Error", throwable.getMessage());
            }
        }
    }

    ConstraintLayout searchedRouteList;

    private void setupDirectionsPopup(List<Route> route) {
        isSearchRoutesShown = true;
//        Log.d(TAG, "onRoute: isSearchRoutesShown: " + isSearchRoutesShown);

//        Log.d(TAG, "onRoute: Routing: setupDirectionsPopup: ");
//        try {
        View popupView = LayoutInflater.from(getActivity()).inflate(R.layout.popup_directions, null);

        PopupWindowHelper popupWindowHelper = new PopupWindowHelper(popupView);
        popupWindowHelper.showFullAsPopUp(bottomBar, isTablet);
//            popupWindowHelper.showFullAsPopUp(gaug, isTablet);

        RecyclerView directionRecycleView = popupView.findViewById(R.id.directions_rv);
        TextView addressTextView = popupView.findViewById(R.id.address);
        TextView closeTextView = popupView.findViewById(R.id.txt_close);
        searchedRouteList = popupView.findViewById(R.id.searchedRouteList);

        addressTextView.setText("To " + destinationAddress);

        closeTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissNavigation();
                popupWindowHelper.dismiss();
                isSearchRoutesShown = false;
                enableMapControls(true);
            }
        });

        Collections.sort(route, (r1, r2) -> r1.getDistanceValue() - r2.getDistanceValue());

        DirectionAdapter directionAdapter = new DirectionAdapter(route, 0, new DirectionAdapter.IDirection() {
            @Override
            public void onRoute(int selectedPosition, boolean showAllRoutes) {
//                Log.d(TAG, "onRoute: showAllRoutes: " + showAllRoutes);
//                Log.d(TAG, "onRoute: isSearchRoutesShown: " + isSearchRoutesShown + " showAllRoutes: " + showAllRoutes);
                if (showAllRoutes) {

                    displayedRoute = null;
//                    Log.d(TAG, "onLocationChanged: destination: onRoute: showAllRoutes: true: displayedRoute: " + displayedRoute);
                    mapFragment.setDisplayedRoute(null);
                    if (calloutMarker != null)
                        calloutMarker.remove();
                    calloutMarker = null;
//                    Log.d(TAG, "onRoute: drawAllRoutes: called by onRoute: ");
                    drawAllRoutes(route, selectedPosition);

//                    Log.d(TAG, "onRoute: ");
//                        ArrayList<Integer> steps=new ArrayList<>();
//                        steps.add(rules.getMaxSpeedArrayHashMap().keySet().s)


                } else {
                    isSearchRoutesShown = false;
                    showSearchLayout(false);
                    enableNavigationDrawerButton(false);

//                    Log.d(TAG, "onRoute: drawAllRoutes: iAmSelected: ");
//                    Log.d(TAG, "onRoute: selectedPosition: points: " + route.get(selectedPosition).getPoints().get(0));

                    popupWindowHelper.dismiss();
                    mapFragment.enableZoomControls(false);
                    mapFragment.enableCompassControls(true);

//                    Log.d(TAG, "onRoute: setting: displayedRoute: " + route.get(selectedPosition));
                    displayedRoute = route.get(selectedPosition);
//                    Log.d(TAG, "onRoute: showAllRoutes: false: displayedRoute: " + displayedRoute);
//                        July 15, 2022 -   We need to get the duration with traffic for every segment
                    getETASegmentsForSelectedRoute();
                    mapFragment.setDisplayedRoute(displayedRoute);
                    if (route.get(selectedPosition).getSegments().size() > 0) {
                        mapFragment.setInitialMapRouteLatLng(route.get(selectedPosition).getSegments().get(0).startPoint());
                    }
//                        printAllSegmentsInstructions();

                    googleMap.setPadding(0, 0, 0, 0);
                    drawSelectedRoute();
                    mapFragment.enableLocationButton(false);
                    mapFragment.enableLocationPointer(false);
                    mapFragment.setSegmentAndPinPointsOnSelectedRoute();

//                    Log.d(TAG, "onRoute: UpdateMaxSpeedView: " + route.get(selectedPosition).getSegments().size());
                    maxSpeedHashMap.clear();
//                    Log.d(TAG, "onRoute: maxSpeed: onRoute: maxSpeedList: cleared: ");

                    selectedRouteSegmentsSize = route.get(selectedPosition).getSegments().size();
                    if (route.get(selectedPosition).getSegments().size() > 0) {
                        Log.d(TAG, "onRoute: segment: " + route.get(selectedPosition).getSegments().get(0));
                    }


//                            May 18, 2022  -   This Asyn Task "UpdateMaxSpeedViewUpdate" is called in drawSelectedRoute as well
//                            so making the calls duplicate so commented it from here
//                        for (int i = 0; i < route.get(selectedPosition).getSegments().size(); i++) {
//
//                            LatLng startPoint = route.get(selectedPosition).getSegments().get(i).startPoint();
//                            LatLng endPoint = route.get(selectedPosition).getSegments().get(i).endPoint();
//
//                            Log.d(TAG, "onRoute: UpdateMaxSpeedView: Green: segment: position: " + i + " onRoute: startPoint: " + startPoint
//                                    + " endPoint: " + endPoint);
//
////                            UpdateMaxSpeedView updateMaxSpeedView = new UpdateMaxSpeedView(i, startPoint, endPoint);
////                            updateMaxSpeedView.executeOnExecutor(MainMenuActivity.threadPoolExecutor);
////                            updateMaxSpeedView.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//
////                            UpdateMaxSpeedViewUpdate updateMaxSpeedView = new UpdateMaxSpeedViewUpdate(i, startPoint, endPoint);
////                            updateMaxSpeedView.executeOnExecutor(MainMenuActivity.threadPoolExecutor);
//
//                        }


                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        directionRecycleView.setLayoutManager(linearLayoutManager);
        directionRecycleView.setAdapter(directionAdapter);
//        } catch (Throwable throwable) {
//            Log.d(TAG, "Routing: setupDirectionsPopup: throwable: " + throwable.getMessage());
//        }
    }

    void showProgressDialog(boolean showDialog) {
        Log.d(TAG, "saveMapAsset: showProgressDialog: showDialog: " + showDialog);
        if (showDialog) {
            loadingLayout.setVisibility(VISIBLE);
        } else {
            loadingLayout.setVisibility(View.GONE);
        }
    }

    // July 25 2022
    void getETASegmentsForSelectedRoute() {
        segmentsDurationMap = new HashMap<>();
        for (int i = 0; i < displayedRoute.getSegments().size(); i++) {
            segmentsDurationMap.put(i, -1);
            Segment segment = displayedRoute.getSegments().get(i);
            int count = segment.getSegmentPoints().size();
            getSegmentETA(segment.getSegmentPoints().get(0), segment.getSegmentPoints().get(count - 1), "" + i);
        }
    }

    /*
    July 25 2022, we need to get the duration with traffic for each segment.
     */
    public void getCurrentSegmentETA() {
//        Log.d(TAG, "getCurrentSegmentETA: ");
        if (mapFragment == null && displayedRoute == null) {
            return;
        }
        LatLng startLocation = mapFragment.getMyLastLocation();
        LatLng endLocation = null;
        if (mCurrentSegmentPosition < displayedRoute.getSegments().size()) {
            Segment currentSegment = displayedRoute.getSegments().get(mCurrentSegmentPosition);
            endLocation = currentSegment.endPoint();
        }

        if (startLocation != null && endLocation != null) {
            Routing routing = new Routing.Builder()
                    .key(getString(R.string.google_maps_key))
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(directionListener)
                    .alternativeRoutes(false)
                    .waypoints(startLocation, endLocation)
                    .routeId("" + mCurrentSegmentPosition)
                    .build();
            routing.executeOnExecutor(MainMenuActivity.threadPoolExecutor);
        }
    }

    void printAllSegmentsInstructions() {
        for (int i = 0; i < displayedRoute.getSegments().size(); i++) {
            Log.d(TAG, "printAllSegmentsInstructions: " + displayedRoute.getSegments().get(i).getInstruction());
        }
    }

    //endregion

    //region Directions

    private RoutingListener directionListener = new RoutingListener() {
        @Override
        public void onRoutingFailure(RouteException e) {
//            Log.d(TAG, "Rerouting: onRoutingFailure: RouteException: " + e.getMessage() + " isRerouting: " + isRerouting);
        }

        @Override
        public void onRoutingStart() {
            Log.d(TAG, "Rerouting: onRoutingStart: ");
        }

        @Override
        public void onRoutingSuccess(List<Route> route, int shortestRouteIndex, String routeName) {
//            Log.d(TAG, "Rerouting: onRoutingSuccess: ");

//            Log.d(TAG, "onRoute: SearchRelevant: Rerouting: onRoutingSuccess: routeName: " + routeName);
            if (routeName != null && !routeName.equalsIgnoreCase("route")) {
                // 25.07.2022 we need to check if is the response for the "main route" or if is the response for a segment
//                Log.d(TAG, "onRoute: SearchRelevant: Rerouting: onRoutingSuccess: partial route: ");
                if (route.size() > 0) {
                    Route partialRoute = route.get(0);
//                    Log.d(TAG, "onRoute: SearchRelevant: Rerouting: onRoutingSuccess: segments: " + partialRoute.getSegments().size());
                    if (partialRoute.getSegments().size() > 0) {
//                        Log.d(TAG, "onRoute: SearchRelevant: Rerouting: onRoutingSuccess: duration: " + partialRoute.getDurationValue() + " " + partialRoute.getDurationWithTrafficValue());

                        if (segmentsDurationMap == null) {
                            segmentsDurationMap = new HashMap<>();
                        }
                        // we need to save the duration with traffic for the segment. We can have multiple calls to get the speed for a segment
//                        Log.d(TAG, "onRoutingSuccess: trafficValue: " + partialRoute.getDurationWithTrafficValue()
//                                + " routeName: " + routeName
//                                + " parsingToInt: " + Integer.parseInt(routeName));
//                        segmentsDurationMap.put(partialRoute.getDurationWithTrafficValue(), Integer.parseInt(routeName));
                        segmentsDurationMap.put(Integer.parseInt(routeName), partialRoute.getDurationWithTrafficValue());
//                        Log.d(TAG, "onRoutingSuccess: setting: SegmentsDurationMap: segmentsDurationMap: " + segmentsDurationMap);

                        // TODO should we update here the total duration?
//                        July 26, 2022 -   We made another call to get ETA for the current step
//                        Log.d(TAG, "setupBottomInstruction: onRoutingSuccess: checking if we made call for ETA: ");
                        if (Integer.parseInt(routeName) == mCurrentSegmentPosition) {
//                            Log.d(TAG, "setupBottomInstruction: onRoutingSuccess: when Integer.parseInt(routeName) == mCurrentSegmentPosition");
                            mTimeStampWhenEnteringOnASegment = DateUtils.getTimestampInDouble();
//                            Log.d(TAG, "setupBottomInstruction: onRoutingSuccess: mTimeStampWhenEnteringOnASegment: " + mTimeStampWhenEnteringOnASegment);
                        }

                        directionView.setSegmentsDurationMap(segmentsDurationMap);
                    }
                }
                return;
            } else if (routeName.equalsIgnoreCase("route")) {
                segmentsDurationMap = new HashMap<>();
//                Log.d(TAG, "onRoute: SearchRelevant: Rerouting: onRoutingSuccess: reset: ");
            }


//            if (connectionMonitor.isConnected()) {
            if (UiUtils.isOnline(getActivity(), getResources().getString(R.string.no_internet_connection))) {
                setupDirectionsPopup(route);
//                Log.d(TAG, "onRoutingSuccess: drawAllRoutes: called from");
//                April 13, 2022    -   As route is now sorted so we just need to send the 0 index if the array
//                drawAllRoutes(route, shortestRouteIndex);
                drawAllRoutes(route, 0);
            }
//        May 23, 2022  -   isOnline function is enough to call if there is no internet then it will shown a message
//        "No Internet connection"
//            else {
//                UiUtils.showToast(getActivity(), getString(R.string.error_lost_Connection));
//            }

            showProgressDialog(false);
        }

        @Override
        public void onRoutingCancelled() {
//            Log.d(TAG, "Rerouting: onRoutingCancelled: ");
            showProgressDialog(false);
        }
    };

    private RoutingListener routeListener = new RoutingListener() {
        @Override
        public void onRoutingFailure(RouteException e) {
            addLogGeneralData("makeARerouting: onRoutingFailure: RouteException: " + e.getMessage());
//            July 27, 2022 -   We are resetting isRerouting because we should enable another try for rerouting and
//            we should set the flag that rerouting failed
//            Log.d(TAG, "Rerouting: routeListener: onRoutingFailure: RouteException: " + e.getMessage()
//                    + " isRerouting: " + isRerouting);
            isRerouting = false;
            isReroutingFailing = true;
        }

        @Override
        public void onRoutingStart() {
            addLogGeneralData("makeARerouting: onRoutingStart: ");
//            Log.d(TAG, "Rerouting: routeListener: onRoutingStart: ");
        }


        @Override
        public void onRoutingSuccess(List<Route> route, int shortestRouteIndex, String routeName) {
//            Log.d(TAG, "Rerouting: routeListener: onRoutingSuccess: setting: displayedRoute: " + route.get(shortestRouteIndex));
            addLogGeneralData("makeARerouting: onRoutingSuccess: route.size(): " + route.size() + " shortestRouteIndex: " + shortestRouteIndex + " routeName: " + routeName);

            for (int i = 0; i < route.size(); i++) {
                Route tempRoute = route.get(i);

                addLogGeneralData("makeARerouting: onRoutingSuccess: tempRoute: points: size: " + tempRoute.getPoints().size());
                addLogGeneralData("makeARerouting: onRoutingSuccess: tempRoute: segments: size: " + tempRoute.getSegments().size());
                for (int x = 0; x < tempRoute.getSegments().size(); x++) {
                    Segment tempSegment = tempRoute.getSegments().get(x);
                    addLogGeneralData("makeARerouting: onRoutingSuccess: tempSegment: x: " + x
                            + " instruction: " + tempSegment.getInstruction()
                            + " first point: " + tempSegment.getSegmentPoints().get(0)
                            + " last point: " + tempSegment.getSegmentPoints().get((tempSegment.getSegmentPoints().size() - 1))
                    );
                }
                addLogGeneralData("makeARerouting: onRoutingSuccess: index: " + i + " name: " + route.get(i).getName() + " distanceValue: " + route.get(i).getDistanceValue());
                addLogGeneralData("makeARerouting: onRoutingSuccess: first index point: " + route.get(i).getPoints().get(0));
                addLogGeneralData("makeARerouting: onRoutingSuccess: last index point: " + route.get(i).getPoints().get((route.get(i).getPoints().size() - 1)));


            }
            displayedRoute = route.get(shortestRouteIndex);
            mapFragment.setDisplayedRoute(route.get(shortestRouteIndex));
            googleMap.setPadding(0, 0, 0, 0);
            drawSelectedRoute();
//            July 26, 2022 -   We should set the flag to false just to be sure that we are not rerouting again and again
            isRerouting = false;
            isReroutingFailing = false;

            mIsFirstInstructionPlayed = false;
        }

        @Override
        public void onRoutingCancelled() {
            addLogGeneralData("makeARerouting: onRoutingCancelled: ");
//            Log.d(TAG, "Rerouting: routeListener: onRoutingCancelled: ");
        }
    };

    CustomInfoWindowGoogleMap customInfoWindow;

    private void drawAllRoutes(List<Route> route, int selectedRoute) {
//        Log.d(TAG, "onRoute: Routing: drawAllRoutes: selectedRoute: " + selectedRoute);
        if (getActivity().isFinishing())
            return;

        if (endMarker != null)
            endMarker.remove();

        polylines = mapFragment.clearPolyline(polylines);

        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        //add route(s) to the map

//        Log.d(TAG, "drawAllRoutes: route: size: " + route.size());
        for (int i = 0; i < route.size(); i++) {
//            Log.d(TAG, "drawAllRoutes: route: distanceText: " + route.get(i).getDistanceText() + " index: " + i);

            PolylineOptions polyOptions = new PolylineOptions();

            if (i == selectedRoute) {
                polyOptions.color(ContextCompat.getColor(requireActivity(), COLORS[0]));
                polyOptions.width(ROUTE_TABLET_WIDTH);
            } else {
                polyOptions.color(ContextCompat.getColor(requireActivity(), COLORS[1]));
                polyOptions.width(ROUTE_DEFAULT_WIDTH);
            }

            polyOptions.addAll(route.get(i).getPoints());
            Polyline polyline = googleMap.addPolyline(polyOptions);
            polylines.add(polyline);

            builder.include(route.get(i).getPoints().get(0));
            int pointsSize = route.get(i).getPoints().size();
            builder.include(route.get(i).getPoints().get(pointsSize - 1));
        }

        MarkerOptions options = new MarkerOptions();
        // End marker
        options.position(destinationLocation);
        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_myloc4));
        options.title(destinationAddress);
        endMarker = googleMap.addMarker(options);

        DestinationDetailInfoWindow info = new DestinationDetailInfoWindow();
        info.setPhoto_reference(destinationPhoto);
        endMarker.setTag(info);
        customInfoWindow = new CustomInfoWindowGoogleMap(getActivity());
        googleMap.setInfoWindowAdapter(customInfoWindow);


//        googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//            @SuppressLint("PotentialBehaviorOverride")
//            @Nullable
//            @org.jetbrains.annotations.Nullable
//            @Override
//            public View getInfoContents(@NonNull @NotNull Marker marker) {
//                return null;
//            }
//
//            @Nullable
//            @org.jetbrains.annotations.Nullable
//            @Override
//            public View getInfoWindow(@NonNull @NotNull Marker marker) {
//                Log.d(TAG, "onNetwork: getInfoWindow: ");
////                if (marker.getTag() instanceof RouteSignInfoWindow) {
//                Log.d(TAG, "onNetwork: getInfoWindow: tag: instruction: " + ((RouteSignInfoWindow) marker.getTag()).getInstruction());
//                RouteSignInfoWindow routeSignInfoWindow = (RouteSignInfoWindow) marker.getTag();
////            July 15, 2022 -   App crashed when routeSignInfoWindow is null
//                if (routeSignInfoWindow == null) {
//                    return null;
//                }
//                View view = getLayoutInflater()
//                        .inflate(R.layout.layout_sign_route_left, null);
//
//
////            Aug 02, 2022    -   If we are not getting the manoeuvre info from google api then don't show the blue baloon
//                if (routeSignInfoWindow.getManoeuvreSign() == null || routeSignInfoWindow.getManoeuvreSign().isEmpty()) {
//                    return null;
//                }
//                if (routeSignInfoWindow.getManoeuvreSign().contains("right")) {
//                    view = getLayoutInflater()
//                            .inflate(R.layout.layout_sign_route_right, null);
//                }
//                mView = view;
//
//                TextView instruction = view.findViewById(R.id.instruction);
//                ImageView img = view.findViewById(R.id.pic);
//
//
////            if (segment.getIsRoundAbout().equalsIgnoreCase("true")) {
////                manoeuvreImg = ImageUtils.FetchDirectionImageForRoundAbout((int) segment.getTurnAngle());
////            } else {
////                manoeuvreImg = ImageUtils.FetchDirectionImage(segment.getManeuver());
////            }
//                String manoeuvreImg = ImageUtils.FetchDirectionImage(routeSignInfoWindow.getManoeuvreSign());
//                int manoeuvreResId = getResources().getIdentifier(manoeuvreImg, "drawable", getActivity().getPackageName());
//                img.setImageResource(manoeuvreResId);
//                Log.e("rotaion", "" + routeSignInfoWindow.getRotation());
//                // img.setRotation(45 + routeSignInfoWindow.getRotation());
////            instruction.setText(refineInstruction(routeSignInfoWindow.getInstruction()));
//                instruction.setText(refineInstruction(routeSignInfoWindow.getInstruction()));
//
//                Log.d(TAG, "getInfoWindow: hide: ");
//                Log.d(TAG, "getInfoWindow: setting its view ");
//                infoWindowAdapterView = view;
//
//                return view;
////                }
////                else {
////
////                    return null;
////                }
//
//            }
//        });


//        googleMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
//            @Override
//            public void onInfoWindowClick(@NonNull @NotNull Marker marker) {
//                Log.d(TAG, "onInfoWindowClick: marker: "+marker);
//                marker.hideInfoWindow();
//            }
//        });


        adjustMapScreen();
    }

    View infoWindowAdapterView;

    private String refineInstruction(String instruction) {
        Log.d(TAG, "refineInstruction: ");
        if (!instruction.contains("onto"))
            return instruction;

        /* 2022.07.26  we should not convert it to lowercase
        String formattedInstruction = instruction.toLowerCase().substring(instruction.indexOf("onto"));
         */
        String formattedInstruction = instruction.substring(instruction.indexOf("onto"));

        formattedInstruction = formattedInstruction.replace("onto ", "");
        if (formattedInstruction.contains("destination"))
            formattedInstruction = formattedInstruction.substring(0, formattedInstruction.indexOf("destination"));

        return formattedInstruction;
    }

    void adjustMapScreen() {
//        Log.d(TAG, "adjustMapScreen: ");
        isAnyRouteDisplayedForSelection = true;

        final ViewTreeObserver vto = searchedRouteList.getViewTreeObserver();
        if (vto.isAlive()) {
            vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    int viewWidth = searchedRouteList.getMeasuredWidth();
                    int viewHeight = searchedRouteList.getMeasuredHeight();
                    // handle viewWidth here...
//                    Log.d(TAG, "adjustMapScreen: onGlobalLayout: viewWidth: " + viewWidth + " - viewHeight: " + viewHeight);
                    LatLng middlePoint = new LatLng(((mapFragment.getMyLastLocation().latitude + destinationLocation.latitude) / 2),
                            ((mapFragment.getMyLastLocation().longitude + destinationLocation.longitude) / 2));
                    double straightDistance = Utils.calculateDistance(mapFragment.getMyLastLocation(), destinationLocation);
                    LatLngBounds latLngBounds = calculateBounds(middlePoint, (straightDistance / 2));

//                  CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(bounds, screenWidthFromDisplayMetrics, screenHeightFromDisplayMetrics, heightPadding);
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngBounds(latLngBounds, 30);
                    googleMap.animateCamera(cameraUpdate);
                    googleMap.setPadding(60, 30, 100, viewHeight);
                    enableMapControls(false);

                    if (vto.isAlive()) {

                        if (Build.VERSION.SDK_INT < 16) {
                            vto.removeGlobalOnLayoutListener(this);
                        } else {
                            vto.removeOnGlobalLayoutListener(this);
                        }
                    }
                }
            });
        }
    }


    private LatLngBounds calculateBounds(LatLng center, double radius) {
        return new LatLngBounds.Builder().
                include(SphericalUtil.computeOffset(center, radius, 0)).
                include(SphericalUtil.computeOffset(center, radius, 90)).
                include(SphericalUtil.computeOffset(center, radius, 180)).
                include(SphericalUtil.computeOffset(center, radius, 270)).build();
    }

    private void drawVisitedRoute(Route route) {
        if (getActivity().isFinishing())
            return;

//        Log.d(TAG, "onLocationChanged: destination: drawVisitedRoute: setting: displayedRoute: " + route);
        displayedRoute = route;
        mapFragment.setDisplayedRoute(route);
        visitedPolylines = mapFragment.clearPolyline(visitedPolylines);

        int polylineWidth = isTablet ? ROUTE_TABLET_WIDTH : ROUTE_PHONE_WIDTH;
        final RoundCap rc = new RoundCap();

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(ContextCompat.getColor(requireActivity(), COLORS[3]));
        polyOptions.width(polylineWidth);
        polyOptions.startCap(rc);
        polyOptions.zIndex(2);
        polyOptions.addAll(route.getVisitedPoints());
        Polyline polyline = googleMap.addPolyline(polyOptions);
        visitedPolylines.add(polyline);

        drawSignRoute(displayedRoute);
    }

    private void drawSelectedRoute() {
//        Toast.makeText(getContext(), "drawSelectedRoute:", Toast.LENGTH_SHORT).show();
//        try {
        if (getActivity().isFinishing())
            return;

        displayedRoute.addVisitedPoint(displayedRoute.getPoints().get(0));

//        Log.d(TAG, "drawSelectedRoute: ");

        visitedPolylines = mapFragment.clearPolyline(visitedPolylines);
        polylines = mapFragment.clearPolyline(polylines);

        int polylineWidth = isTablet ? ROUTE_TABLET_WIDTH : ROUTE_PHONE_WIDTH;

        PolylineOptions polyOptions = new PolylineOptions();
        polyOptions.color(ContextCompat.getColor(requireActivity(), COLORS[0]));
        polyOptions.width(polylineWidth);
        polyOptions.zIndex(1);
        polyOptions.addAll(displayedRoute.getPoints());

        Polyline polyline = googleMap.addPolyline(polyOptions);
        polylines.add(polyline);

        drawSignRoute(displayedRoute);
//            April 08, 2022    -   Trying to set the annotation place at bottom wih padding but the map starts
//            Jumping
//            googleMap.setPadding(0, 1200, 0, 0);


        for (int i = 0; i < displayedRoute.getSegments().size(); i++) {

//            Aug 09, 2022  -
            List<LatLng> segments = displayedRoute.getSegments().get(i).getSegmentPoints();
            LatLng startPoint = null, endPoint = null, startPoint1 = null, endPoint1 = null;

            startPoint = displayedRoute.getSegments().get(i).startPoint();
            endPoint = displayedRoute.getSegments().get(i).endPoint();
            if (segments != null && segments.size() > 5) {
                startPoint1 = segments.get(1);
                endPoint1 = segments.get((segments.size() - 2));
            }

//            Aug 09, 2022  -   In the case when we have long segments and many points -
//            we should use not the first the point but the average(middle) point between the first point and second point
//            And the same thing goes for end point
            if (startPoint1 != null && endPoint1 != null) {
                double latitude = ((startPoint.latitude + startPoint1.latitude) / 2);
                double longitude = ((startPoint.longitude + startPoint1.longitude) / 2);

                startPoint = new LatLng(latitude, longitude);

                latitude = ((endPoint.latitude + endPoint1.latitude) / 2);
                longitude = ((endPoint.longitude + endPoint1.longitude) / 2);

                endPoint = new LatLng(latitude, longitude);
            }


//            Log.d(TAG, "onRoute: UpdateMaxSpeedView: Green: segment: position: " + i + " onRoute: startPoint: " + startPoint
//                    + " endPoint: " + endPoint);

            UpdateMaxSpeedViewUpdate updateMaxSpeedView = new UpdateMaxSpeedViewUpdate(i, startPoint, endPoint);
            updateMaxSpeedView.executeOnExecutor(MainMenuActivity.threadPoolExecutor);
//                UpdateMaxSpeedView updateMaxSpeedView = new UpdateMaxSpeedView(i, startPoint, endPoint);
//                updateMaxSpeedView.executeOnExecutor(MainMenuActivity.threadPoolExecutor);
        }

        updateMyPositionUpdate();
    }


    void printMaxSpeedArrayHashMap() {
//        Log.d(TAG, "printMaxSpeedArrayHashMap: ");
//        Log.d(TAG, "printMaxSpeedArrayHashMap: rules.getMaxSpeedArrayHashMap().keySet().size(): "
//                + rules.getMaxSpeedArrayHashMap().keySet().size());

//        May 23, 2022  -   we need to make sure that we clear the previous geo fences
//        if (geofenceRequestIdList.size() > 0) {
//            if (geofencingClient != null) {
//        geofencingClient.removeGeofences(mPendingIntent)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        addLogGeofenceData("DriveFragmentBase: printMaxSpeedArrayHashMap: removeGeofences: onSuccess: Geofence Removed...");
//                        Log.d(TAG, "compareRequestId: removeGeofences: printMaxSpeedArrayHashMap: onSuccess: geofencingClient: ");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        addLogGeofenceData("DriveFragmentBase: printMaxSpeedArrayHashMap: removeGeofences: addOnFailureListener: Unable to removed Geofence... exception: " + e.getMessage());
//                        Log.d(TAG, "compareRequestId: removeGeofences: printMaxSpeedArrayHashMap: onFailure: exception: " + e.getMessage());
//                    }
//                });
//            }
//        }
        geofenceRequestIdList.clear();
        maxSpeedArrayHashMap.clear();

        for (int i = 0; i < rules.getMaxSpeedArrayHashMap().keySet().size(); i++) {
            ArrayList<GenericModel> list = rules.getMaxSpeedArrayHashMap().get(i);
            if (list != null) {
//                Log.d(TAG, "printMaxSpeedArrayHashMap: list: size: " + list.size());

                ArrayList<GenericModel> geofenceList = new ArrayList<>();
                for (int x = 0; x < list.size(); x++) {
                    if (x >= 1) {

                        geofenceList.add(list.get(x));
//                        Log.d(TAG, "printMaxSpeedArrayHashMap: we need to add geofence for this location:   " +
//                                "Latitude: " + list.get(x).getLatitude()
//                                + " Longitude" + list.get(x).getLongitude()
//                                + " speed: " + list.get(x).getSpeed());

//                        May 18, 2022  -   We ae creating geofenceRequestId by concatinating below three values
//                        stepPosition_speedIndex_Speed
                        String geofenceRequestId = i + "_" + x + "_" + list.get(x).getSpeed();
                        addLogGeofenceData("DriveFragmentBase: printMaxSpeedArrayHashMap: add for geofencing: geofenceRequestId: " + geofenceRequestId
                                + " segment: " + i
                                + " latitude, longitude: " + list.get(x).getLatitude()
                                + "," + list.get(x).getLongitude()
                                + " speed: " + list.get(x).getSpeed()
                                + " previousLatitude, previousLongitude: " + list.get(x).getPreviousLatitude()
                                + "," + list.get(x).getPreviousLongitude());
//                        Log.d(TAG, "printMaxSpeedArrayHashMap: geofenceRequestId: " + geofenceRequestId);

                        if (!geofenceRequestIdList.contains(geofenceRequestId)) {
//                            Log.d(TAG, "onReceive: printMaxSpeedArrayHashMap: " + geofenceRequestId);


//                            geofenceHelper.setCurrentSegment(mCurrentSegmentPosition);
                            addLogGeofenceData("DriveFragmentBase: printMaxSpeedArrayHashMap: add for geofencing: mCurrentSegmentPosition: " + mCurrentSegmentPosition + " radius: " + ((float) list.get(x).getStraightDistance()));
//                            Log.d(TAG, "compareRequestId: includeRequestId: " + geofenceRequestId);
                            geofenceRequestIdList.add(geofenceRequestId);


//<!--    May 25, 2022    -   We implement it by custom so don't need for now -->
//                            checkPermissionsBeforeAddingGeofence(geofenceRequestId,
//                                    Double.parseDouble(list.get(x).getLatitude()),
//                                    Double.parseDouble(list.get(x).getLongitude()),
//                                    ((float) list.get(x).getStraightDistance()));
                        } else {
                            addLogGeofenceData("geofence request id list contains " + geofenceRequestId);
                        }
                    }
//                    Log.d(TAG, "printMaxSpeedArrayHashMap: onRoute: stepIndex: " + i + " speedIndex: " + x +
//                            " Latitude: " + list.get(x).getLatitude()
//                            + " Longitude" + list.get(x).getLongitude()
//                            + " speed: " + list.get(x).getSpeed());
//                    Log.d(TAG, "printMaxSpeedArrayHashMap: geofenceRequestIdList: size: " + geofenceRequestIdList.size());
                }

                maxSpeedArrayHashMap.put(i, geofenceList);
//                Log.d(TAG, "printMaxSpeedArrayHashMap: ");
            }
        }
    }

    private void drawSignRoute(Route route) {
        int polylineDirectionWidth = isTablet ? ROUTE_SIGN_TABLET_WIDTH : ROUTE_SIGN_PHONE_WIDTH;

//        Log.d(TAG, "drawSignRoute: polylineDirectionWidth: " + polylineDirectionWidth);
        for (int i = 0; i < directionMarkers.size(); i++) {
            if (directionMarkers.get(i) != null)
                directionMarkers.get(i).remove();
        }

        for (int i = 0; i < route.getSegments().size() - 1; i++) {
            Segment currentStep = route.getSegments().get(i);
            Segment nextStep = route.getSegments().get(i + 1);

            float distanceInMeters = Utils.calculateDistance(currentStep.endPoint(), mapFragment.getMyLastLocation());
            Log.e("distance", "" + distanceInMeters);

//            Log.d(TAG, "drawSignRoute: ");
            if (i >= route.getCurrentStep() && distanceInMeters > 10) {
//                Log.d(TAG, "drawSignRoute: i> route.getCurrentStep() && distanceInMeters: ");
                List<LatLng> startPoint = generateFirstPolyPoint(currentStep);
                List<LatLng> endpoint = generateEndPolyStep(nextStep);

                Log.e("stepSize", "startPoint_____" + startPoint.size());
                Log.e("stepSize", "endpoint  ____" + endpoint.size());

                if (startPoint.size() < 2 || endpoint.size() < 2)
                    continue;

                float currentStepRotation = mapFragment.bearing(startPoint.get(startPoint.size() - 1), startPoint.get(startPoint.size() - 2));
                float nextStepRotation = mapFragment.bearing(endpoint.get(endpoint.size() - 2), endpoint.get(endpoint.size() - 1));

//                Aug 05, 2022  -
                float deltaAngle = currentStepRotation - nextStepRotation;
                currentStep.setTurnAngle(deltaAngle);

//                Log.d(TAG, "drawSignRoute: nextStep.getIsRoundAbout(): " + nextStep.getIsRoundAbout());
                if (nextStep.getIsRoundAbout().equalsIgnoreCase("true")) {

//                    Log.d(TAG, "drawSignRoute: deltaAngle: " + deltaAngle + " its interger value: " + ((int) deltaAngle));
                    String maneuver = ImageUtils.FetchDirectionImageForRoundAbout((int) deltaAngle);
//                    Log.d(TAG, "setInstructionView: drawSignRoute: roundAbout: " + nextStep.getIsRoundAbout() + " maneuver: " + maneuver);
                    nextStep.setManeuver(maneuver, "DFB when getIsRoundAbout true");

                    int resImage = getResources().getIdentifier(maneuver, "drawable", getContext().getPackageName());
//                    Log.d(TAG, "setInstructionView: drawSignRoute: resImage: " + resImage);
                    if (i == 0) {
                        directionView.setManeuverImage(resImage);
                    }
                } else {
//                    Log.d(TAG, "drawSignRoute: nextStep: " + nextStep);
//                    Log.d(TAG, "drawSignRoute: " + nextStep.getManeuver());
//                    Log.d(TAG, "drawSignRoute: " + getContext().getPackageName());

                    int resImage = getResources().getIdentifier(nextStep.getManeuver(), "drawable", getContext().getPackageName());
//                    Log.d(TAG, "else: setInstructionView: drawSignRoute: resImage: " + resImage);
                    if (i == 0) {
                        directionView.setManeuverImage(resImage);
                    }
                }


                Log.e("stepSize", "________________________________");

                PolylineOptions polyOptionsSign = new PolylineOptions();
                polyOptionsSign.color(ContextCompat.getColor(requireActivity(), COLORS[2]));
                polyOptionsSign.width(polylineDirectionWidth);
                polyOptionsSign.zIndex(2);
                polyOptionsSign.addAll(startPoint);
                polyOptionsSign.addAll(endpoint);
                Polyline directionPoly = googleMap.addPolyline(polyOptionsSign);
                visitedPolylines.add(directionPoly);

                Marker directionMarker = googleMap.addMarker(new MarkerOptions()
                        .anchor(0.5f, 0.5f)
                        .flat(true)
                        .rotation(nextStepRotation)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_direction_new))
                        .position(endpoint.get(endpoint.size() - 1)));

                if (PolyUtil.isLocationOnPath(route.getLastVisitedPoint(), currentStep.getSegmentPoints(), false, 10/*10 meter Tolerance*/)) {
                    calloutMarker = googleMap.addMarker(new MarkerOptions()
                            .anchor(0.5f, 0.5f)
                            .flat(true)
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.icon_dot))
                            .position(currentStep.endPoint()));

                    RouteSignInfoWindow routeSignInfoWindow = new RouteSignInfoWindow();
//                    Log.d(TAG, "drawSignRoute: nextStep: instruction: " + nextStep.getInstruction());
//                    Aug 08, 2022  -   Use Instruction short instead of instruction
//                    routeSignInfoWindow.setInstruction(nextStep.getInstruction());
                    routeSignInfoWindow.setInstruction(nextStep.getInstructionShort());

                    String nextStepManoeuvre = nextStep.getManeuver();

//                    Don't show the callout (blue info window) maneuver is null
//                    July 25, 2022 -   We need to fix/fixes regarding the maneuver during the call out
//                    Aug 01, 2022  -   Uncomment it out because
                    String nextStepInstruction = nextStep.getInstruction();
//                    Log.d(TAG, "drawSignRoute: nextStepInstruction: " + nextStepInstruction + " nextStepManoeuvre: " + nextStepManoeuvre);

//                    if (segment.getIsRoundAbout().equalsIgnoreCase("true")) {
//                        manoeuvreImg = ImageUtils.FetchDirectionImageForRoundAbout((int) segment.getTurnAngle());
//                    } else {
//                        manoeuvreImg = ImageUtils.FetchDirectionImage(segment.getManeuver());
//                    }

                    if (nextStepManoeuvre == null) {
                        if (nextStepInstruction != null) {
                            if (nextStepInstruction.toLowerCase().contains("continue")) {
                                nextStepManoeuvre = "straight";
                            } else {
                                nextStepManoeuvre = "";
                            }
                        }
                        routeSignInfoWindow.setManoeuvreSign(nextStepManoeuvre);
                    } else {

                        routeSignInfoWindow.setManoeuvreSign(nextStepManoeuvre);
                    }

                    routeSignInfoWindow.setRotation(currentStepRotation);
//                    Log.d(TAG, "drawSignRoute: " + destinationAddress + " routeSignInfoWindow: " + routeSignInfoWindow.getInstruction());
                    calloutMarker.setTag(routeSignInfoWindow);
//                    Log.d(TAG, "drawSignRoute: tag set done");
                    calloutMarker.showInfoWindow();
//                    Log.d(TAG, "drawSignRoute: showInfoWindow: done");

                    directionMarkers.add(calloutMarker);
//                    Log.d(TAG, "drawSignRoute: calloutMarker: "+calloutMarker);
                }

                directionMarkers.add(directionMarker);
            }
        }
//        Log.d(TAG, "drawSignRoute: loop end: ");

        handleShortRoute();

//        July 06, 2022 -   Previously currentPositionOnTheRoute is sending null value to setup instructions
//        and that's why we are unable to show the instructions when a route initially loaded
//        So now we are using getPhoneLastBestLocation method in case currentPositionOnTheRoute is null


        LatLng phoneLastBestLatLng = null;

        if (currentPositionOnTheRoute != null) {
            directionView.setupInstruction(currentPositionOnTheRoute, currentSegmentMaxSpeed);
        } else {
            Location phoneLastBestLocation = rules.getPhoneLastBestLocation(getActivity());
            phoneLastBestLatLng = new LatLng(phoneLastBestLocation.getLatitude(), phoneLastBestLocation.getLongitude());
            directionView.setupInstruction(phoneLastBestLatLng, currentSegmentMaxSpeed);
        }

//        July 26, 2022 -   We need to simulate isVehicleMoving for updating the duration and ETA
        boolean isVehicleMoving = rules.isVehicleMoving();
        String username = sessionManagement.getKeyLogin();
        if (username != null && (username.equalsIgnoreCase("naeem"))) {
            isVehicleMoving = true;
        }

//        July 15, 2022 -   We should save the timestamp when actually start driving(moving)
        if (mTimeStampWhenEnteringOnARoute == 0 && displayedRoute != null && isVehicleMoving) {
//        if (mTimeStampWhenEnteringOnARoute == 0 && displayedRoute != null) {
            mTimeStampWhenEnteringOnARoute = DateUtils.getTimestampInDouble();
        }

        directionView.setupBottomInstruction(displayedRoute, phoneLastBestLatLng, mTimeStampWhenEnteringOnASegment,
                mTimeStampWhenEnteringOnARoute,
                new DirectionView.OnHandleBottomView() {
                    @Override
                    public void iDismiss() {
//                        Log.d(TAG, "iDismiss: forSearch: ");
                        dismissNavigation();
                        rules.clearMaxSpeedArrayHashMap();
                        rules.clearSteps();

//                June 27, 2022 -   resetting zoom level back to 17.0F for initially load and
//                voice instruction field to empty strings again
//                        Todo what should we when we exit a route and their is no internet
                        resetMapControlsAndDirectionView();

                        geofenceRequestIdList.clear();
                        removeGeofenceMonitoring();
                    }

                    @Override
                    public void iDirection() {
//                        Log.d(TAG, "iDirection: forSearch: ");
                        dismissNavigation();
//                        hideDirectionView(false);

                        if (mapFragment.getMyLastLocation() != null && destinationLocation != null) {
                            Routing routing = new Routing.Builder()
                                    .key(getString(R.string.google_maps_key))
                                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                                    .withListener(directionListener)
                                    .alternativeRoutes(true)
                                    .waypoints(mapFragment.getMyLastLocation(), destinationLocation)
                                    .build();

                            routing.execute();
                        }
                    }

                    @Override
                    public void reloadCurrentSegment() {
//                        Log.d(TAG, "reloadCurrentSegment: ");
                        getCurrentSegmentETA();

                    }


                });
    }

    void resetMapControlsAndDirectionView() {
        Log.d(TAG, "resetFields: ");
        NAVIGATION_ZOOM_LEVEL = 17.5F;
        isRouteInitiallyLoaded = true;
        if (directionView != null) {
            directionView.setPreviousQuarterInstruction("");
        }
    }

    private void updateMyPositionUpdate() {
        Log.d(TAG, "handleNavigation: updateMyPositionUpdate: ");

        Segment currentSegmentStep = getSelectedStep();
        Segment nextSegmentStep = getNextStep();
        LatLng startPoint = currentSegmentStep.startPoint();
        LatLng bearingPoint = currentSegmentStep.endPoint();

        if (displayedRoute.getVisitedPoints().size() > 1) {
            startPoint = displayedRoute.getVisitedPoints().get(displayedRoute.getVisitedPoints().size() - 2);
//                bearingPoint = displayedRoute.getLastVisitedPoint();
        }

        mapFragment.removeVisitedPoint();

        bearingPoint = mapFragment.getBearingPoint();
//        Log.d(TAG, "updateMyPositionUpdate: bearingPoint: " + bearingPoint);

        LatLng calculatedPosition = currentPositionOnTheRoute;
        if (calculatedPosition == null) {
            calculatedPosition = mapFragment.getMyLastLocation();
        }

//        June 28, 2022 -
//        LatLng cameraCenter = mapFragment.getNewLatLngFromOffset(calculatedPosition, -500);
//        Log.d(TAG, "updateMyPositionUpdate: cameraCenter: " + cameraCenter);
//        LatLng cameraCenter = mapFragment.displayedArea(startPoint, bearingPoint, calculatedPosition);

        if (bearingMarker == null) {
            bearingMarker = googleMap.addMarker(mapFragment.returnMarkerOption(bearingPoint));
        } else {
//            Oct 19, 2022  -   Comment it out because app was giving an exception here that
//            Fatal Exception: api exception Not on the main thread (So running this piece of code on in uiThread)
//            bearingMarker.setPosition(bearingPoint);
            setBearingPoint(bearingPoint);
        }

        float bearing = mapFragment.bearing(calculatedPosition, bearingPoint);

//        Log.d(TAG, "updateMyPositionUpdate: bearing: " + bearing);
        if (bearingPrevious != 1000) {
            double delta = bearingPrevious - bearing;
//            Log.d(TAG, "updateMyPositionUpdate: delta: " + delta);
        }
        bearingPrevious = bearing;


        if (mPositionMarker == null) {

            mPositionMarker = googleMap.addMarker(new MarkerOptions()
//                    April 08, 2022    -   We should not use flat marker and should never rotate annotation marker
//                    .flat(true)
//                    .rotation(bearing)
                    .icon(bitmapDescriptorFromVector(getContext(), R.drawable.custom_navigation_icon))
//                    July 19, 2022 -   Added new icon from dragos ios - its smaller and is a image
//                    .icon(mIcon)
                    .position(calculatedPosition));
//                    .position(shadowTgt));
        } else {
//            mPositionMarker.setRotation(bearing);
//            Oct 24, 2022  -   App crashed with "Fatal Exception: Not on the main thread"
//            So setting this marker position using uiThread
            setMarkerPosition(calculatedPosition);
//            mPositionMarker.setPosition(shadowTgt);
        }


//        June 28, 2022 -   START

        Projection p = googleMap.getProjection();
        Point bottomRightPoint = p.toScreenLocation(p.getVisibleRegion().nearRight);
        Point center = new Point(bottomRightPoint.x / 2, bottomRightPoint.y / 2);

//        June 28, 2022 -   158 is the height that we calculated of direction view bottom bar
//        20 means 20 pixels above the direction view bottom bar
        int delta = bottomRightPoint.y - 158 - 20;
        if (isRouteInitiallyLoaded) {
            delta = ((bottomRightPoint.y * 2) / 3);
        }

//        Point offset = new Point(center.x, (center.y + 300));
        Point offset = new Point(center.x, (delta));


//        Log.d(TAG, "updateMyPositionUpdate: bottomRightPoint: " + bottomRightPoint
//                + " center: " + center
//                + " offset: " + offset
//                + " delta: " + delta);
        LatLng centerLoc = p.fromScreenLocation(center);
        LatLng offsetNewLoc = p.fromScreenLocation(offset);
        // this computed value only changes on zoom
        double offsetDistance = SphericalUtil.computeDistanceBetween(centerLoc, offsetNewLoc);

        double straightDistance = Utils.calculateDistance(centerLoc, offsetNewLoc);

//        Log.d(TAG, "updateMyPositionUpdate: centerLoc: " + centerLoc
//                + " offsetNewLoc: " + offsetNewLoc
//                + " offsetDistance: " + offsetDistance
//                + " straightDistance: " + straightDistance
//                + " delta: " + delta);

//        June 29, 2022 -   We should change the offset distance limit according the tilt that we set for the map
        int tilt = 90;
//        July 20, 2022-
        tilt = 60;
        if (offsetDistance > 200) {
            if (tilt == 0) {
                offsetDistance = 100;
            } else if (tilt == 45) {
                offsetDistance = 150;
            } else {
                offsetDistance = 200;
            }

        }


        //-----------------------------------------------
        LatLng positionTarget = null;
//        positionTarget = SphericalUtil.computeOffset(calculatedPosition, offsetDistance, bearing);


//        July 08, 2022 -   We should use adaptive zoom like when near to turn, zoom out and else zoom in
        int distance = directionView.getRemainingDistanceToNextStep();
//        Log.d(TAG, "Before: updateMyPositionUpdate: distance: " + distance + " NAVIGATION_ZOOM_LEVEL: " + NAVIGATION_ZOOM_LEVEL);
        double factor = 1;
        boolean changeZoom;

//        Log.d(TAG, "updateMyPositionUpdate: ");
//        July 12, 2022 -   We should allow auto zooming only if the next segment is long (500<distance)
//        TODO Should we change the value from 500 meters?
        if (nextSegmentStep != null) {
//            Log.d(TAG, "updateMyPositionUpdate: nextSegmentStep: " + nextSegmentStep.getLength());
        }
        if (nextSegmentStep != null && nextSegmentStep.getLength() > 500) {
            changeZoom = true;
        } else {
            changeZoom = false;
        }

//        July 20, 2022 -
        float zoomOut = 17.5F;
        zoomOut = 17.0F;
        float zoomIn = 18.5F;
        zoomIn = 17.5F;
//        Log.d(TAG, "updateMyPositionUpdate: changeZoom: " + changeZoom);
        int deltaTilt = 0;


        if (changeZoom) {
            if (distance > 150) {
                if (NAVIGATION_ZOOM_LEVEL == zoomOut) {
                    factor = 4;
                }
                NAVIGATION_ZOOM_LEVEL = zoomIn;
                deltaTilt = 25;
            } else {
                if (NAVIGATION_ZOOM_LEVEL == zoomIn) {
                    factor = 2;
                }
                NAVIGATION_ZOOM_LEVEL = zoomOut;
            }
        } else {
//            July 12, 2022 -   We should makes sure that the zoom stays maximum if we don't need to auto zoom
            factor = 1;
            NAVIGATION_ZOOM_LEVEL = zoomIn;
        }

//        Log.d(TAG, "After: updateMyPositionUpdate: distance: " + distance + " NAVIGATION_ZOOM_LEVEL: " + NAVIGATION_ZOOM_LEVEL);
        positionTarget = SphericalUtil.computeOffset(calculatedPosition, (offsetDistance / factor), bearing);

        CameraPosition.Builder cameraPosition = CameraPosition.builder();
        cameraPosition.zoom(NAVIGATION_ZOOM_LEVEL);
        cameraPosition.bearing((float) (bearing));
        cameraPosition.target(positionTarget);
        cameraPosition.tilt(tilt + deltaTilt);      // between 0 and 90
        CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cameraPosition.build());
//        Log.d(TAG, "updateMyPositionUpdate: shadowTgt: " + positionTarget);
//        googleMap.animateCamera(cu);

//        June 28, 2022 -   END


//        April 08, 2022    -   Trying to move camera first to bottom but it still jumps
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(cameraCenter));
//        cameraPosition = new CameraPosition.Builder()
//                .target(cameraCenter)
//                .tilt(0)   // press two fingers on the google map and move up or down, its tilt
////                    .zoom(googleMap.getCameraPosition().zoom)   //  I set it as driver wants, rather than a predefine value
//                .zoom(NAVIGATION_ZOOM_LEVEL)   //  Its current value is 18.0
//                .bearing(bearing)
//                .build();
//        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//        Log.d(TAG, "adjustMapScreen: animateCamera: from updateMyPositionUpdate: NAVIGATION_ZOOM_LEVEL: " + NAVIGATION_ZOOM_LEVEL);

//        if (isRouteInitiallyLoaded) {
//            NAVIGATION_ZOOM_LEVEL = 18.5F;
//            isRouteInitiallyLoaded = false;
//        }

//        Log.d(TAG, "updateMyPositionUpdate: cancelableCallback: " + cancelableCallback
//                + " animationInProgress: " + animationInProgress);
        if (!animationInProgress && cancelableCallback != null) {
            animationInProgress = true;
//            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition), cancelableCallback);
//            if (isRouteInitiallyLoaded) {
//                googleMap.moveCamera(cu);
//                animationInProgress = false;
//            } else {
            googleMap.animateCamera(cu, cancelableCallback);
//            }
        }

        if (isRouteInitiallyLoaded) {
            NAVIGATION_ZOOM_LEVEL = 18.5F;
            isRouteInitiallyLoaded = false;
        }

    }

    private Bitmap getSmallMarker() {
        int height = 400;
        int width = 400;
//        BitmapDrawable bitmapdraw = (BitmapDrawable) getResources().getDrawable(R.drawable.custom_navigation_icon);
//        Bitmap b = bitmapdraw.getBitmap();
        Bitmap b = getBitmapFromVectorDrawable(getContext(), R.drawable.custom_navigation_icon);
        Bitmap smallMarker = Bitmap.createScaledBitmap(b, width, height, false);
        return smallMarker;
    }

    private BitmapDescriptor bitmapDescriptorFromVector(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private Bitmap getBitmapFromVectorDrawable(Context context, int vectorResId) {
        Drawable vectorDrawable = ContextCompat.getDrawable(context, vectorResId);
        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        return bitmap;
    }

    private List<LatLng> generateFirstPolyPoint(Segment step) {
        int polylineMaxDistance = 8;
        polylineMaxDistance = 20;
        List<LatLng> startPoint = new ArrayList<>();

        try {
            int i = step.getSegmentPoints().size() - 1;

            while (polylineMaxDistance > 0 && i >= 0) {
                LatLng lastPoint = step.getSegmentPoints().get(i);
                startPoint.add(lastPoint);

                Log.e("Polyline1", "" + i);

                if (i > 0) {
                    LatLng secondLastPoint = step.getSegmentPoints().get(i - 1);
                    double distance = SphericalUtil.computeDistanceBetween(lastPoint, secondLastPoint);

                    float currentStepRotation = mapFragment.bearing(lastPoint, secondLastPoint);

                    if (distance > polylineMaxDistance) {
                        LatLng middlePoint = SphericalUtil.computeOffset(lastPoint, polylineMaxDistance, currentStepRotation);
                        startPoint.add(middlePoint);
                        polylineMaxDistance = 0;
                    } else {
                        polylineMaxDistance = (int) (polylineMaxDistance - distance);

                        if (polylineMaxDistance == 0 && startPoint.size() == 1) {

                            LatLng middlePoint = SphericalUtil.computeOffset(lastPoint, 4, currentStepRotation);
                            startPoint.add(middlePoint);
                        }
                    }
                } else
                    polylineMaxDistance = 0;

                i--;
            }
        } catch (Exception e) {
            Log.e("Polyline Error", e.getMessage());
        }

        Collections.reverse(startPoint);
        return startPoint;
    }

    private List<LatLng> generateEndPolyStep(Segment step) {
        int polylineMaxDistance = 8;
        polylineMaxDistance = 20;
        List<LatLng> endPoint = new ArrayList<>();

//        July 19, 2022 -   
//        Log.d(TAG, "drawSignRoute: generateEndPolyStep: step: " + step.getInstruction());
        if (step.getInstruction().contains("round") || step.getInstruction().contains("circle") || step.getInstruction().contains("Cir")) {
//            Log.d(TAG, "drawSignRoute: generateEndPolyStep: step contains round - so extending polylineMaxDistance to 100");
            polylineMaxDistance = 100;
        }

        try {
            int i = 0;

            while (polylineMaxDistance > 0 && i < step.getSegmentPoints().size()) {
                LatLng firstPoint = step.getSegmentPoints().get(i);
                endPoint.add(firstPoint);

                if (i < step.getSegmentPoints().size() - 1) {
                    LatLng secondFirstPoint = step.getSegmentPoints().get(i + 1);
                    double distance = SphericalUtil.computeDistanceBetween(firstPoint, secondFirstPoint);
                    float currentStepRotation = mapFragment.bearing(firstPoint, secondFirstPoint);

                    if (distance > polylineMaxDistance) {
                        LatLng middlePoint = SphericalUtil.computeOffset(firstPoint, polylineMaxDistance, currentStepRotation);
                        endPoint.add(middlePoint);
                        polylineMaxDistance = 0;
                    } else {
                        polylineMaxDistance = (int) (polylineMaxDistance - distance);

                        if (polylineMaxDistance == 0 && endPoint.size() == 1) {
                            LatLng middlePoint = SphericalUtil.computeOffset(firstPoint, 4, currentStepRotation);
                            endPoint.add(middlePoint);
                        }
                    }
                } else {
                    polylineMaxDistance = 0;
                    polylineMaxDistance -= 2;
                }

                i++;
            }
        } catch (Exception e) {
            Log.e("Polyline Error", e.getMessage());
        }

        return endPoint;
    }

    private void hideDirectionView(boolean hide) {
//        Log.d(TAG, "dismissNavigation: hideDirectionView: hide: " + hide);
        if (hide) {
            directionView.setVisibility(View.GONE);
            volumeIc.setVisibility(View.GONE);
//            btnCenterMap.setVisibility(View.GONE);
        } else {
            directionView.setVisibility(View.VISIBLE);
            volumeIc.setVisibility(View.VISIBLE);
//            btnCenterMap.setVisibility(View.VISIBLE);
        }
    }

    private void dismissNavigation() {

        resetValues();
        if (googleMap != null) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(mapFragment.getMyLastLocation())
                    .tilt(0)
                    .zoom(mapFragment.calculateZoomLevel(1000, mapContainer, mActivity))
                    .build();

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            polylines = mapFragment.clearPolyline(polylines);
            visitedPolylines = mapFragment.clearPolyline(visitedPolylines);

            destinationLocation = null;

            if (endMarker != null)
                endMarker.remove();

            for (int i = 0; i < directionMarkers.size(); i++) {
                if (directionMarkers.get(i) != null)
                    directionMarkers.get(i).remove();
            }
        }

//        displayedRoute = null;
        mapFragment.setDisplayedRoute(displayedRoute);
        if (calloutMarker != null)
            calloutMarker.remove();
        calloutMarker = null;

        if (mPositionMarker != null) {
            mPositionMarker.remove();
        }
    }

    private void handleVoice() {
        Log.d(TAG, "handleVoice: ");
        if (mapFragment != null && googleMap != null) {
            Log.d(TAG, "handleVoice: volumeIc: " + volumeIc.isSelected());
            boolean muteValue = !volumeIc.isSelected();
            Log.d(TAG, "handleVoice: muteValue: " + muteValue);
            setVolumeIcon(muteValue);
            directionView.setMuteVoice(muteValue);
            volumeIc.setSelected(muteValue);
        }
    }

    void setVolumeIcon(boolean muteValue) {
        if (muteValue) {
            Glide.with(getActivity())
                    .load(R.drawable.icon_volume_off)
                    .into(volumeIc);
        } else {
            Glide.with(getActivity())
                    .load(R.drawable.icon_volume_on)
                    .into(volumeIc);
        }
    }

    //endregion

    //region Short route

    private void handleShortRoute() {
//        Log.d(TAG, "handleShortRoute: displayedRoute: " + displayedRoute);
        if (displayedRoute != null) {
            hideDirectionView(false);
//            Log.d(TAG, "drawSignRoute: handleShortRoute: destinationAddress: " + destinationAddress);
            directionView.setRoute(displayedRoute, destinationAddress);
        } else {
            hideDirectionView(true);
        }
    }

    @Override
    public void onSwitchNightMode() {
        mapFragment.setNight(true);
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_in_night));
//        July 01, 2022 -   We should disable showing the buildings
        googleMap.setBuildingsEnabled(false);
    }

    @Override
    public void onSwitchDayMode() {
        mapFragment.setNight(false);
        googleMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_in_day));
//        July 01, 2022 -   We should disable showing the buildings
        googleMap.setBuildingsEnabled(false);
    }


    private void makeARerouting(LatLng currentLatLng) {
//        Log.d(TAG, "makeARerouting: currentLatLng: " + currentLatLng);
        addLogGeneralData(" makeARerouting: currentLatLng: " + currentLatLng);

        float bearing;
        if (displayedRoute == null) {
            bearing = mapFragment.bearing(previousLocation, currentLatLng);
        } else {
            bearing = mapFragment.bearing(displayedRoute.getPoints().get(0), currentLatLng);
        }


        resetValuesForNavigation();
//            July 26, 2022 -   We should set the flag to true just to be sure that we will try a reroute
        isReroutingFailing = false;


//        Sep 26, 2022  -   We commented put the six digit work around because we believe the problem was generated by passingLocation variable
//        String stringLat = String.format("%.6f", currentLatLng.latitude);
//        String stringLng = String.format("%.6f", currentLatLng.longitude);

//        LatLng tempLatLng = new LatLng(Double.parseDouble(stringLat), Double.parseDouble(stringLng));
//        LatLng passingLocation = SphericalUtil.computeOffset(currentLatLng, 1, bearing);
//        LatLng passingLocation = SphericalUtil.computeOffset(tempLatLng, 1, bearing);
//        addLogGeneralData(" makeARerouting: tempLatLng: " + tempLatLng + " passingLocation: " + passingLocation);

        addLogGeneralData(" makeARerouting: currentLatLng: " + currentLatLng);
        Routing routing = new Routing.Builder()
                .key(getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .withListener(routeListener)
                .alternativeRoutes(false)
//                Sep 26, 2022  -   We should not use passingLocation so if somehow this point is getting calculated
//                behind the current location (we are calculating based on the heading which might be in opposite
//                direction on the right direction)
//                .waypoints(currentLatLng, passingLocation, destinationLocation)
//                .waypoints(tempLatLng, passingLocation, destinationLocation)
                .waypoints(currentLatLng, destinationLocation)
                .build();

        routing.execute();
//        July 26, 2022 -   We should reset the flag only after we draw a new route -
//        Because there is a strange bug when rerouting that the cursor current position is not moving when navigating
//        isRerouting = false;
//        Log.d(TAG, "makeARerouting: reRouting: agian: ");
    }

    //endregion

    //region Helpers

    protected void loadMapView() {
        try {
            mapContainer.removeAllViews();

            mapFragment = new MapFragment();
            mapFragment.getMapAsync(this);
            getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();

            bottomBar.setVisibility(View.VISIBLE);
            closeOpenPanelBtn.setVisibility(View.GONE);
        } catch (Throwable throwable) {
            UiUtils.showExclamationDialog(getActivity(), getString(R.string.error_map_connectivity_title), getString(R.string.error_map_connectivity),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
        }
    }

    private void initView(View view) {
        //region View Instance

        mapContainer = view.findViewById(R.id.map_container);
        searchContainer = view.findViewById(R.id.search_container);
        streamingContainer = view.findViewById(R.id.streaming_container);
        gaugesContainer = view.findViewById(R.id.gauges_container);

        closeOpenPanelBtn = view.findViewById(R.id.btn_closeopen_panel);
        closeOpenPanelBtn.setVisibility(View.GONE);

        btnRestStation = view.findViewById(R.id.btn_rest_station);
        btnGasStation = view.findViewById(R.id.btn_gas_station);
        btnSearch = view.findViewById(R.id.btn_search);
        btnCenterMap = view.findViewById(R.id.btn_center_map);

        bottomBar = view.findViewById(R.id.bottom_bar);

        maxSpeedTV = view.findViewById(R.id.tv_speed);
        tvTxtMph = view.findViewById(R.id.tv_mph);

        mphView = view.findViewById(R.id.mph_view);

        btnStatus = view.findViewById(R.id.btn_status);
        btnWork = view.findViewById(R.id.btn_work);
//        btnRadioGroup = view.findViewById(R.id.btn_radiogroup);
        btnShare = view.findViewById(R.id.btn_share);

        directionView = view.findViewById(R.id.directionView);
        volumeIc = view.findViewById(R.id.iv_volume);
        cameraIc = view.findViewById(R.id.iv_camera);

        //endregion

        btnSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showSoftKeyboard();

                searchContainer.setVisibility(View.VISIBLE);
                isSearchContainerOpened = true;
                hideDirectionView(true);
                SearchFragment searchFragment = SearchFragment.newInstance(mapFragment.getMyLastLocation());

                searchFragment.setISetRoute(new OnSetRoute() {
                    @Override
                    public void setARoute(String placeId, String address) {
//                        Log.d(TAG, "SearchRelevant: setARoute: placeId: " + placeId);
//                        if (connectionMonitor.isConnected()) {
                        if (UiUtils.isOnline(getActivity(), getResources().getString(R.string.no_internet_connection))) {
                            if (!getActivity().isFinishing()) {
//                                Log.d(TAG, "SearchRelevant: setARoute: isLocationEnabled: " + UiUtils.checkIsLocationEnabled(getActivity()));
                                if (UiUtils.checkIsLocationEnabled(getActivity())) {
                                    loadingText.setText("Searching...");
                                    showProgressDialog(true);
                                    destinationAddress = address;
                                    mapFragment.setDestinationAddress(destinationAddress);
                                    searchContainer.setVisibility(View.GONE);
                                    getChildFragmentManager().popBackStackImmediate();

//                                    Log.d(TAG, "SearchRelevant: setARoute: address: " + destinationAddress);
//                                    new PlaceDetailApi(placeId, DriveFragmentBase.this:onPathsFound)
//                                    .executeOnExecutor(threadPoolExecutor);
//                                            .execute();
                                    findPlaceDetail(placeId);
                                } else {
//                                    Log.d(TAG, "setARoute: location is not enabled: ");
                                    UiUtils.showLocationSettingsAlert(getActivity());
                                }
                            }
                        }
//        May 23, 2022  -   isOnline function is enough to call if there is no internet then it will shown a message
//        "No Internet connection"
//                        else {
//                            Log.d(TAG, "setARoute: else: No Connection");
//                            UiUtils.showExclamationDialog(getActivity(), getString(R.string.connection), getString(R.string.error_lost_Connection));
//                        }
                    }

//                    @Override
//                    public void closeSearch() {
//                        searchContainer.setVisibility(View.GONE);
//                    }
                });

                getChildFragmentManager().beginTransaction().replace(R.id.search_container, searchFragment).commit();
            }
        });

        // btnLogin = view.findViewById(R.id.btn_login);
        // btnStartTollRoad = view.findViewById(R.id.btn_start_toll_road);

//        March 24, 2022    -   We should not have a recording here
//        We are recording events at the time they needs to be recorded so we think its better to commit it here
//        if (!rules.isCurrentEldStatusDriving() && rules.isVehicleMoving()) {
//            Log.d(TAG, "initView: setEldEventToDriving: ");
//            Log.d(TAG, "TestingDriving: initView: setEldEventToDriving: ");
//            rules.setEldEventToDriving(getActivity(), "AnnotationDriveFragmentBase2");
//        }


        loadMapView();

        closeOpenPanelBtn.setOnClickListener(this);
        btnRestStation.setOnClickListener(this);
        btnGasStation.setOnClickListener(this);
        btnShare.setOnClickListener(this);
        volumeIc.setOnClickListener(this);
        btnCenterMap.setOnClickListener(this);
        cameraIc.setOnClickListener(this);

        //btnRadioGroup.setOnCheckedChangeListener(this);

        Log.d(TAG, "MapAutoUpdateTask: initView: mapAutoUpdateTask: " + mapAutoUpdateTask);
//        if (mapAutoUpdateTask == null) {
//            mapAutoUpdateTask = new MapAutoUpdateTask();
//            mapAutoUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }
        initiateMapAutoUpdateTask();

        //region Activity

        ImageView addIcon = getActivity().findViewById(R.id.toolbar_add_button);
        addIcon.setVisibility(View.GONE);

        searchContainer.setVisibility(View.GONE);

//        btnBluetooth = getActivity().findViewById(R.id.btn_bluetooth);
//        btnBluetooth.setOnClickListener(this);
//        btnBluetoothOn = getActivity().findViewById(R.id.btn_bluetooth_on);

        if (rules.isBleConnected()) {
            isBluetoothDeviceConnected = true;
//            btnBluetooth.setVisibility(View.GONE);
//            btnBluetoothOn.setVisibility(View.VISIBLE);
        }

        clLogin = getActivity().findViewById(R.id.cl_login);
        clLogin.setVisibility(View.VISIBLE);

        txtLogin = getActivity().findViewById(R.id.login);
        txtLogin.setVisibility(View.VISIBLE);
        txtLogin.setOnClickListener(this);

//        txtGps = getActivity().findViewById(R.id.txt_gps);
//        txtGps.setVisibility(View.VISIBLE);

//        btnStatus = getActivity().findViewById(R.id.btn_status);
        btnStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isStatusDialogShown && !isSearchRoutesShown) {
                    setupStatusPopup();
                }
            }
        });

        btnWork = getActivity().findViewById(R.id.btn_work);
        btnWork.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupWorkPopup();
            }
        });

        //endregion

        updateLoginView();
        initFragment(view);
        updateGPSView();
    }

    void showSoftKeyboard() {
        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    private void updateLoginView() {
//        Log.d(TAG, "updateLoginView: isDriverLoggedIn: " + rules.isDriverLoggedIn());

        txtLogin.setText(rules.isDriverLoggedIn() ? "Logout" : "Login");
    }

    private void updateGPSLocation() {
//        Log.d(TAG, "updateGPSLocation: ");
        MyLocation.init();

        MyLocation.getInstance().initListener(this);
//        MyLocation.getInstance().requestLocation(getContext());
        MyLocation.getInstance().getDeviceLocation(getContext());
    }

    private void showHideRestStation() {
        if (mapFragment != null && googleMap != null)
            try {
                isRestStation = !isRestStation;
                btnRestStation.setImageResource(isRestStation ? R.drawable.restroom50 : R.drawable.restroom_off50);
                rules.loadMapAssetsAndRestAreasIn100MilesRange(getActivity(), googleMap, isRestStation, isGasStation);
            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            }
    }

    private void showHideGasStation() {
        if (mapFragment != null && googleMap != null)
            try {
                isGasStation = !isGasStation;
                btnGasStation.setImageResource(isGasStation ? R.drawable.gas50 : R.drawable.gas_off50);
                rules.loadMapAssetsAndRestAreasIn100MilesRange(getActivity(), googleMap, isRestStation, isGasStation);
            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            }
    }

    private void endCycle() {
//        Log.d(TAG, "endCycle: ");
        try {

//            Log.d(TAG, "endCycle: isDriverLoggedIn: " + rules.isDriverLoggedIn());
            if (!rules.isDriverLoggedIn()) {
                UiUtils.showToast(getActivity(), "Login first, to end cycle");
                return;
            }

//            July 19, 2022 -   Added a dialog that just to ensure user really wanted to end cycle
            showDialog_With_Listener(getContext(), isTablet, "RCO Trucks", "Are you sure, you want to end cycle?",
                    "Yes", "Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            if (i == Dialog.BUTTON_POSITIVE) {

                                rules.endDriverCurrentCycle();

//                    March 22, 2022    -    Fixed the bug regarding event type set to Duty Status instead of Login/Logout
//            rules.setEldEvent(getActivity(), BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, BusinessRules.EventCode.LOGOUT,
//                    BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Logout", null, "", "");

                                rules.setEldEvent(getActivity(), BusinessRules.EventType.A_DRIVER_LOGIN_LOGOUT_ACTIVITY, BusinessRules.EventCode.LOGOUT,
                                        BusinessRules.EventRecordStatus.ACTIVE, BusinessRules.EventCodeOrigin.EDITED_OR_ENTERED_BY_THE_DRIVER, "Logout", null, "", "");

                                rules.logoutDriver();
                                updateLoginView();


//                                July 19, 2022 -   Call end cycle counter when their is an end cycle called
                                checkIsCycleEnded(2);
//                                cycleCounterLayout.setVisibility(View.VISIBLE);
                            }
                        }
                    });
        } catch (Throwable t2) {
            Log.d(TAG, "endCycle: t2: " + t2.getMessage());
            if (t2 != null) {
                t2.printStackTrace();
                UiUtils.showExclamationDialog(getActivity(), "Error", t2.getMessage());
            }
        }
    }

    boolean isRerouting = false, isReroutingFailing = false;

    public void handleNavigation(LatLng currentLatLng, int tolerance) {
//        Log.d(TAG, "Rerouting: handleNavigation: ");
        boolean isLocationOnPath = false;


        if (displayedRoute == null) {
//            July 27, 2022 -   This is in the case of rerouting when internet is not available
//            Log.d(TAG, "onLocationChanged: destination: handleNavigation: isReroutingFailing: " + isReroutingFailing);
            if (!isReroutingFailing) {
//        July 27, 2022 -   If the displayed route is null and rerouting is not failing that means that
//        we actually search a route so there is no point in continuing the navigation
                return;
            }
        } else {
//            Log.d(TAG, "onLocationChanged: destination: handleNavigation: checking isLocationOnPath: ");
//            July 29, 2022 -   Added dynamic tolerance instead of static 15
//            isLocationOnPath = PolyUtil.isLocationOnPath(currentLatLng, displayedRoute.getPoints(), false, 15 /*15 meter Tolerance*/);
            isLocationOnPath = PolyUtil.isLocationOnPath(currentLatLng, displayedRoute.getPoints(), false, tolerance /*15 meter Tolerance*/);
//            TODO Sep 26, 2022 -   Makes sure if it is the right condition or it should be tolerance < 15 to assign isLocationOnPath a true value
            if (tolerance > 15) {
                addLogGeneralData("handleNavigation: tolerance: " + tolerance + " isLocationOnPath: " + isLocationOnPath);
                isLocationOnPath = true;
            }
        }

        mapFragment.enableLocationButton(false);
        Log.d(TAG, "onLocationChanged: destination: Rerouting: handleNavigation: isLocationOnPath: " + isLocationOnPath);

        if (isLocationOnPath) {

            mapFragment.setSegmentAndPinPointsOnSelectedRoute();
            Route route = mapFragment.generatedRoute(currentLatLng);
//            Log.d(TAG, "handleNavigation: route: " + route);
            if (!mapFragment.isSameAsInitialLocation(route.getLastVisitedPoint()))
                drawVisitedRoute(route);
        } else {
//            Log.d(TAG, "onLocationChanged: destination: Rerouting: handleNavigation: isRerouting: " + isRerouting);
            if (!isRerouting) {
                isRerouting = true;
//                Log.d(TAG, "onLocationChanged: destination: Rerouting: handleNavigation: ");
                mapFragment.removePreviousExpectedRouteMarkers();
                makeARerouting(currentLatLng);
            }
        }


//        Log.d(TAG, "after: rerouting: handleNavigation: displayedRoute: " + displayedRoute + " isRerouting: " + isRerouting);
        if (displayedRoute == null) {
//            July 27, 2022 -   When we lost the internet connection and we were rerouting -
//            we need to move the current position on the map
            if (isRerouting) {
//                Log.d(TAG, "handleNavigation: call mPositionMarker.setPosition(mCurrentLocation): " + mCurrentLocation);

                if (mPositionMarker != null) {
                    mPositionMarker.setPosition(mCurrentLocation);
                }

                CameraPosition.Builder cameraPosition = CameraPosition.builder();
                cameraPosition.zoom(NAVIGATION_ZOOM_LEVEL);
//            July 27, 2022 -   We need to calculate the bearing the previous position and the current position,
//            we are assuming that the driver is driving on the road
                float bearing = mapFragment.bearing(previousLocation, mCurrentLocation);
                cameraPosition.bearing((float) (bearing));
                cameraPosition.target(mCurrentLocation);
//            July 27, 2022 -   When we don't have the route, we don't care for the tilt
//                cameraPosition.tilt(tilt + deltaTilt);      // between 0 and 90
                CameraUpdate cu = CameraUpdateFactory.newCameraPosition(cameraPosition.build());
                if (!animationInProgress && cancelableCallback != null) {
                    animationInProgress = true;
                    googleMap.animateCamera(cu, cancelableCallback);
                }

            }
            return;
        }
        LatLng currentPointCoordinates = displayedRoute.getLastVisitedPoint();
        if (currentPointCoordinates == null) {
            currentPointCoordinates = currentPositionOnTheRoute;
        }

//        Log.d(TAG, "handleNavigation: currentPointCoordinates: " + currentPointCoordinates);
        if (!mapFragment.isSameAsInitialLocation(currentPointCoordinates)) {
//            Log.d(TAG, "handleNavigation: drawSelectedRoute: updateMyPositionUpdate: called: handleNavigationUpdate");
            updateMyPositionUpdate();
        }
    }


    private void callDirectionGoogleApi(LatLng currentLatLng) {
//        try {
//            Routing routing = new Routing.Builder()
//                    .key(getString(R.string.google_maps_key))
//                    .travelMode(AbstractRouting.TravelMode.DRIVING)
//                    .withListener(routeListener)
//                    .alternativeRoutes(false)
//                    .waypoints(currentLatLng, destinationLocation)
//                    .build();
//
//            routing.execute();
//        } catch (Throwable throwable) {
//        }
    }

    private Segment getSelectedStep() {
        for (int i = 0; i < displayedRoute.getSegments().size(); i++) {
            Segment stepPoints = displayedRoute.getSegments().get(i);

            if (displayedRoute.getLastVisitedPoint() != null) {
                if (PolyUtil.isLocationOnPath(displayedRoute.getLastVisitedPoint(), stepPoints.getSegmentPoints(), false, 5/*5 meter Tolerance*/)) {
                    return stepPoints;
                }
            }
        }

        if (displayedRoute.getSegments().size() > 0) {
            return displayedRoute.getSegments().get(0);
        }

        return null;
    }


    //    July 12, 2022 -   Based on the current segment position, we are returning next segment if their is any
    private Segment getNextStep() {

        int stepIndex = (mCurrentSegmentPosition + 1);
        if (stepIndex < displayedRoute.getSegments().size()) {
            return displayedRoute.getSegments().get(stepIndex);
        }
        return null;
    }

    private void handleStreaming() {
        try {
            boolean isStreamingViewVisible = streamingContainer.getVisibility() == View.VISIBLE;

            if (isStreamingViewVisible) {
                getChildFragmentManager().popBackStackImmediate();
            } else {
                getChildFragmentManager().beginTransaction().add(R.id.streaming_container, new CameraStreamingFragment()).commit();
            }

            cameraIc.setImageResource(isStreamingViewVisible ? R.drawable.camera : R.drawable.camera_active);
            streamingContainer.setVisibility(isStreamingViewVisible ? View.GONE : View.VISIBLE);
        } catch (Throwable throwable) {
        }
    }

    public void handleBackPressed() {
        try {
            if (directionView.isShown()) {
                dismissNavigation();
                displayedRoute = null;
//                Log.d(TAG, "onLocationChanged: destination: handleBackPressed: displayedRoute: " + displayedRoute);
                mapFragment.setDisplayedRoute(null);

                if (calloutMarker != null)
                    calloutMarker.remove();

                calloutMarker = null;

                if (mPositionMarker != null) {
                    mPositionMarker.remove();
                }
            } else if (searchContainer.isShown()) {
                searchContainer.setVisibility(View.GONE);
                getChildFragmentManager().popBackStackImmediate();
            } else if (streamingContainer.isShown()) {
                handleStreaming();
            } else {
                getActivity().finish();
            }
        } catch (Throwable throwable) {
        }
    }

    private void updateStatusRadioName(String name) {
        Log.d(TAG, "updateStatusRadioName: name: " + name);
        if (btnStatus == null) {
            btnStatus = mView.findViewById(R.id.btn_status);
        }

        updateBottomBarStatus(name);
    }

    private String getStatusRadioName() {
        return btnStatus.getText().toString();
    }

    private void unselectBottomRadioButtons() {
//        btnRadioGroup.clearCheck();
    }

    public boolean hasOpenedDialogs() {
        if (DialogManager.d != null && DialogManager.d.isShowing())
            return true;

        if (getActivity() == null)
            return false;

        List<Fragment> fragments = getActivity().getSupportFragmentManager().getFragments();

        if (fragments == null)
            return false;

        for (Fragment f : fragments)
            if (f instanceof DialogFragment)
                return true;

        return false;
    }

    private void updateGPSView() {
        try {
            boolean isLocationEnabled = Utils.areLocationServicesEnabled(getActivity());
            int redColor = getResources().getColor(R.color.red);
            int greenColor = getResources().getColor(R.color.green);

//            txtGps.setTextColor(isLocationEnabled ? greenColor : redColor);
        } catch (Throwable throwable) {
        }
    }

    private void calculateGaugesValuesForDistance() {
        Log.d(TAG, "sortAmenity: MapAutoUpdateTask: calculateDistance: ");
//        Log.d(TAG, "updateDistance: ForDistance: calculateGaugesValuesForDistance: rules.isDriverLoggedIn(): " + rules.isDriverLoggedIn());
//        if (!rules.isDriverLoggedIn())
//            return;

        rules.setLastCtx(getActivity());
        String drivingStatus = rules.getDrivingStatus();

//        June 15, 2022 -   We should calculate the distances all the time if the ble is connected
//        if (StringUtils.equalsIgnoreCaseAny(drivingStatus, new String[]{"OffDuty", "Off Duty"}))
//            return;


        Date nowMinus1Min = DateUtils.addSecs(new Date(), -60);

        if (lastestTimeNearestLocationUpdated == null || lastestTimeNearestLocationUpdated.before(nowMinus1Min)) {
            Location l = rules.getPhoneLastBestLocation(getActivity());

            if (l != null) {

                Log.d(TAG, "calculateDistance: distancePilot: " + distancePilot);
                if (distancePilot == -1) {
                    nearestPilotAsset = rules.getNearestPilotAsset(l, nearestPilotAsset);
                }

                Log.d(TAG, "calculateDistance: nearestPilotAsset: " + nearestPilotAsset);
                boolean useDrivingSpeedForCalculating = true;

                boolean isVehicleMoving = rules.isVehicleMoving();
//                Aug 11, 2022  -   enabled it for testing working fine with it
//                if (displayedRoute != null) {
//                    isVehicleMoving = true;
//                }

                if (nearestPilotAsset != null) {
                    Log.d(TAG, "calculateDistance: ");
//                    Do we need to find the nearest pilot etc again or we can deduct the miles we covered from the available distance
//                    Log.d(TAG, "updateDistance: calculateGaugesValuesForDistance: if: " + (distancePilot != -1 && !isVehicleMoving && useDrivingSpeedForCalculating));
//                    Log.d(TAG, "updateDistance: calculateGaugesValuesForDistance: rules.isVehicleMoving(): " + isVehicleMoving);
                    Log.d(TAG, "calculateDistance: distancePilot: " + distancePilot);
                    if (distancePilot != -1 && !isVehicleMoving && useDrivingSpeedForCalculating) {
//                        June 15, 2022 -   In the case that vehicle is not moving and we already calculated one
//                        distance then we should not do anything because this means that we are stopped
                    } else {
                        Log.d(TAG, "calculateDistance: calculateGaugesValuesForDistance: nearestLove: "
                                + new LatLng(Double.parseDouble(nearestPilotAsset.Latitude),
                                Double.parseDouble(nearestPilotAsset.Longitude)));
                        calculateDistanceToAsset(new LatLng(l.getLatitude(), l.getLongitude()),
                                new LatLng(Double.parseDouble(nearestPilotAsset.Latitude),
                                        Double.parseDouble(nearestPilotAsset.Longitude)),
                                1);
//                        Log.d(TAG, "updateDistance: calculateGaugesValuesForDistance: distancePilot: " + distancePilot);
                    }
                }

                Log.d(TAG, "calculateDistance: distanceLove: " + distanceLove);
                if (distanceLove == -1) {
                    nearestLoveAsset = rules.getNearestLoveAsset(l, nearestLoveAsset);
                    Log.d(TAG, "sortAmenity: calculateGaugesValuesForDistance: nearestLoveAsset: " + nearestLoveAsset);
                    if (nearestLoveAsset != null) {
                        checkAmenitiesImagesUpdate("love", "map_assets_love", "ameneties_images_info", nearestLoveAsset.RecordId);
                    }
                }
                if (nearestLoveAsset != null) {
                    if (distanceLove != -1 && !isVehicleMoving && useDrivingSpeedForCalculating) {
//                        June 15, 2022 -   In the case that vehicle is not moving and we already calculated one
//                        distance then we should not do anything because this means that we are stopped
                    } else {
                        Log.d(TAG, "calculateDistance: calculateGaugesValuesForDistance: nearestLove: "
                                + new LatLng(Double.parseDouble(nearestLoveAsset.Latitude),
                                Double.parseDouble(nearestLoveAsset.Longitude)));
                        calculateDistanceToAsset(new LatLng(l.getLatitude(), l.getLongitude()),
                                new LatLng(Double.parseDouble(nearestLoveAsset.Latitude),
                                        Double.parseDouble(nearestLoveAsset.Longitude)),
                                2);
                    }
                }

                Log.d(TAG, "calculateDistance: distanceRestArea: " + distanceRestArea);
                if (distanceRestArea == -1) {
                    nearestRestArea = rules.getNearestRestAreaAsset(l, nearestRestArea);
                }
                if (nearestRestArea != null) {
                    if (distanceRestArea != -1 && !isVehicleMoving && useDrivingSpeedForCalculating) {
//                        June 15, 2022 -   In the case that vehicle is not moving and we already calculated one
//                        distance then we should not do anything because this means that we are stopped
                    } else {
                        Log.d(TAG, "calculateDistance: calculateGaugesValuesForDistance: nearestLove: "
                                + new LatLng(Double.parseDouble(nearestRestArea.Latitude),
                                Double.parseDouble(nearestRestArea.Longitude)));
                        calculateDistanceToAsset(new LatLng(l.getLatitude(), l.getLongitude()),
                                new LatLng(Double.parseDouble(nearestRestArea.Latitude),
                                        Double.parseDouble(nearestRestArea.Longitude)),
                                3);
                    }


                }

                Log.d(TAG, "calculateDistance: distanceTA: " + distanceTA);
                if (distanceTA == -1) {
                    nearestTaAsset = rules.getNearestTaAsset(l, nearestTaAsset);
                }
                Log.d(TAG, "calculateDistance: nearestTaAsset: " + nearestTaAsset);
                if (nearestTaAsset != null) {
                    if (distanceTA != -1 && !isVehicleMoving && useDrivingSpeedForCalculating) {
//                        June 15, 2022 -   In the case that vehicle is not moving and we already calculated one
//                        distance then we should not do anything because this means that we are stopped
                    } else {
                        Log.d(TAG, "calculateDistance: calculateGaugesValuesForDistance: nearestLove: "
                                + new LatLng(Double.parseDouble(nearestTaAsset.Latitude),
                                Double.parseDouble(nearestTaAsset.Longitude)));
                        calculateDistanceToAsset(new LatLng(l.getLatitude(), l.getLongitude()),
                                new LatLng(Double.parseDouble(nearestTaAsset.Latitude),
                                        Double.parseDouble(nearestTaAsset.Longitude)),
                                4);
                    }
                }

//                Log.d(TAG, "updateDistance: calculateNearestTaMilesStr: nearestPilot: " + nearestPilot + " ");
//                Log.d(TAG, "updateDistance: calculateNearestTaMilesStr: nearestLove: " + nearestLove);
//                Log.d(TAG, "updateDistance: calculateNearestTaMilesStr: nearestRest: " + nearestRest);
//                Log.d(TAG, "updateDistance: calculateNearestTaMilesStr: nearestTA: " + nearestTa);

                lastestTimeNearestLocationUpdated = new Date();


            }
        }

//        Jan 10, 2022  -   Now we are setting the distance for offline when we don't have the internet connection
        if (!isICAvailable) {
            updateDistanceValuesOffline();
        }
    }

    private void calculateDistanceToAsset(LatLng myLocation, LatLng stationLocation, int assetType) {
//        Log.d(TAG, "updateDistance: nearestPilotAsset: calculateDistanceToAsset: myLocation: " + myLocation + " stationLocation: " + stationLocation);
        if (myLocation == null || stationLocation == null)
            return;

        Routing routing = new Routing.Builder()
//                .key(getContext().getString(R.string.google_maps_key))
                .key("AIzaSyCkXY-OOuAIGFiHisd0EAaQ5m92OmG-qHg")
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .alternativeRoutes(false)
                .waypoints(myLocation, stationLocation)
                .withListener(new RoutingListener() {
                    @Override
                    public void onRoutingFailure(RouteException e) {
                        Log.d(TAG, "calculateDistance: onRoutingFailure: ");
                    }

                    @Override
                    public void onRoutingStart() {
                        Log.d(TAG, "calculateDistance: onRoutingStart: ");
                    }

                    @Override
                    public void onRoutingSuccess(List<Route> routes, int shortestRouteIndex, String routeName) {
                        Log.d(TAG, "calculateDistance: onRoutingSuccess: assetType: " + assetType);
                        try {
                            Route route = routes.get(shortestRouteIndex);

                            Log.d(TAG, "calculateDistance: onRoutingSuccess: route: " + route.toString());
                            int distanceInMeters = route.getDistanceValue();
                            Log.d(TAG, "calculateDistance: onRoutingSuccess: distanceInMeters: " + distanceInMeters);
//                            Log.d(TAG, "calculateDistanceToAsset: onRoutingSuccess: distance: " + distanceInMeters);

//                            June 15, 2022 -   We should use a delta distance because of the gps accuracy
                            int delta = 100;

                            double distanceInMiles = Utils.convertKmsToMiles((distanceInMeters / 1000.0));
                            Log.d(TAG, "calculateDistance: onRoutingSuccess: ");
                            if (assetType == 1) {
                                Log.d(TAG, "calculateDistance: onRoutingSuccess: pilot: recordId: " + nearestPilotAsset.RecordId);
                                if (distancePilot == -1) {
                                    distancePilot = distanceInMeters;
//                                    Log.d(TAG, "onRoutingSuccess: distancePilot: " + distancePilot);
                                } else {
                                    if (distanceInMeters > (distancePilot + delta)) {
                                        distancePilot = -1;
                                    }
                                }
//                                Log.d(TAG, "updateDistance: onRoutingSuccess: distancePilot: " + distanceInMiles);
                            } else if (assetType == 2) {
                                Log.d(TAG, "calculateDistance: onRoutingSuccess: recordId: love: " + nearestLoveAsset.RecordId);
                                if (distanceLove == -1) {
                                    distanceLove = distanceInMeters;
                                } else {
                                    if (distanceInMeters > (distanceLove + delta)) {
                                        distanceLove = -1;
                                    }
                                }
                            } else if (assetType == 3) {
                                Log.d(TAG, "calculateDistance: onRoutingSuccess: recordId: rest area: " + nearestRestArea.RecordId);
                                if (distanceRestArea == -1) {
                                    distanceRestArea = distanceInMeters;
                                } else {
                                    if (distanceInMeters > (distanceRestArea + delta)) {
                                        distanceRestArea = -1;
                                    }
                                }
                            } else if (assetType == 4) {
                                Log.d(TAG, "calculateDistance: onRoutingSuccess: recordId: ta: " + nearestTaAsset.RecordId);
                                if (distanceTA == -1) {
                                    distanceTA = distanceInMeters;
                                } else {
                                    if (distanceInMeters > (distanceTA + delta)) {
                                        distanceTA = -1;
                                    }
                                }
                            }

//                            Log.d(TAG, "updateDistance: onRoutingSuccess: distanceInMiles: " + distanceInMiles);
                            if (isTablet) {
                                updateTabletDistance((int) distanceInMiles, assetType);
                            } else {
                                updatePhoneDistance((int) distanceInMiles, assetType);
                            }

                        } catch (Throwable throwable) {
                            Log.d(TAG, "calculateDistance: onRoutingSuccess: throwable: " + throwable.getMessage());
                            if (throwable != null)
                                throwable.printStackTrace();
                        }
                    }

                    @Override
                    public void onRoutingCancelled() {
                        Log.d(TAG, "calculateDistance: onRoutingCancelled: ");
                    }
                })
                .build();
        routing.execute();


    }

    private void updateGaugesParametersForDistance() {
        Log.d(TAG, "sortAmenity: updateGaugesParametersForDistance: ");
//        Log.d(TAG, "updateDistance: ForDistance: tablet: updateGaugesParameters: ");
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "MapAutoUpdateTask: updateGaugesParametersForDistance: run: ");

                    String loadingMessage = "Calculating...";
                    if (!rules.isBleConnected()) {
                        loadingMessage = "Waiting for ELD...";
                    }

//                    Log.d(TAG, "updateDistance: run: nearestPilot: " + nearestPilot + " nearestLove: " + nearestLove);
                    ((TextView) gaugesContainer.findViewById(R.id.gauges_pilot)).setText(nearestPilot != null ? nearestPilot + "" : loadingMessage);
                    ((TextView) gaugesContainer.findViewById(R.id.gauges_love)).setText(nearestLove != null ? nearestLove + "" : loadingMessage);
                    ((TextView) gaugesContainer.findViewById(R.id.gauges_rest)).setText(nearestRest != null ? nearestRest + "" : loadingMessage);
                    ((TextView) gaugesContainer.findViewById(R.id.gauges_ta)).setText(nearestTa != null ? nearestTa + "" : loadingMessage);

                    Log.d(TAG, "MapAutoUpdateTask: updateTabletDistance: updateGaugesParametersForDistance: run: isTablet: " + isTablet + " value: " + (nearestPilot != null ? nearestPilot + "" : loadingMessage));
                    if (isTablet) {
                        gaugesLastLoveStationDistance = (nearestLove != null ? nearestLove + "" : loadingMessage);
                        gaugesLastPilotStationDistance = (nearestPilot != null ? nearestPilot + "" : loadingMessage);
                        gaugesLastTAStationDistance = (nearestRest != null ? nearestRest + "" : loadingMessage);
                        gaugesLastRestAreaStationDistance = (nearestTa != null ? nearestTa + "" : loadingMessage);

                        if (isICAvailable) {
                            ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).setText(gaugesLastPilotStationDistance);
                            Log.d(TAG, "updateDistanceValuesOffline: updateGaugesParametersForDistance: run: get: text: " + ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).getText());
                            ((TextView) mView.findViewById(R.id.gauges_love_station_distance)).setText(gaugesLastLoveStationDistance);
                            ((TextView) mView.findViewById(R.id.gauges_rest_area_station_distance)).setText(gaugesLastRestAreaStationDistance);
                            ((TextView) mView.findViewById(R.id.gauges_ta_station_distance)).setText(gaugesLastTAStationDistance);
                        }
                    }
                }
            });
        }
    }

    private void setupGauges() {
        Log.d(TAG, "tablet: setupGauges: ");
        if (isTablet) {
            ConstraintLayout gaugesStatsView = getView().findViewById(R.id.gauges_statsview);
            gaugesStatsView.setVisibility(View.VISIBLE);

            ((TextView) gaugesStatsView.findViewById(R.id.gauges_drivinghours).findViewById(R.id.gauges_barname)).setText("Driving: ");
            ((TextView) gaugesStatsView.findViewById(R.id.gauges_breakhours).findViewById(R.id.gauges_barname)).setText("Break: ");
            ((TextView) gaugesStatsView.findViewById(R.id.gauges_shifthours).findViewById(R.id.gauges_barname)).setText("Shift: ");
            ((TextView) gaugesStatsView.findViewById(R.id.gauges_cyclehours).findViewById(R.id.gauges_barname)).setText("Cycle: ");
        }
    }

    private void setGaugeText(int id, long mints) {
//        Log.d(TAG, "tablet: calculateHours: setGaugeText: mints: " + mints + " DateUtils.formatHourStr" + DateUtils.formatHourStr(mints));
        TextView gauge = ((TextView) gaugesContainer.findViewById(id).findViewById(R.id.gauges_barvalue));
        if (gauge != null) {
//            updateGauge(secs != null ? DateUtils.formatSecsStr(secs) : "0", gauge);
            updateGauge(mints != 0 ? DateUtils.formatHourStr(mints) : "0", gauge);
        }
//        ((TextView) gaugesContainer.findViewById(id).findViewById(R.id.gauges_barvalue)).setText(secs != null ? DateUtils.formatSecsStr(secs) : "0");
    }

    private void setCircularGaugeText(TextView gaugeTextView, long mints, int type) {
//    private void setCircularGaugeText(TextView gaugeTextView, long mints) {
        if (gaugeTextView != null) {
            String status = mints != 0 ? DateUtils.formatHourStr(mints) : "0";
            status = status.replace("hrs", "");

            Log.d(TAG, "updateCircularBarValue: setCircularGaugeText: type: " + type + " status: " + status);
            if (type == 1) {
                drivingHoursLastPercentage = status;
            } else if (type == 2) {
                breakHoursLastPercentage = status;
            } else if (type == 3) {
                shiftHoursLastPercentage = status;
            } else if (type == 4) {
                cycleHoursLastPercentage = status;
            }
            updateGauge(status, gaugeTextView);
        }
    }

    private void setGaugeText(int id, double hours) {
//        Log.d(TAG, "MapAutoUpdateTask: doInBackground: setGaugeText: hours: ");
//        Log.d(TAG, "tablet: calculateHours: setGaugeText: hours: " + hours + " DateUtils.formatHourStr" + DateUtils.formatHourStr(hours));
        TextView gauge = ((TextView) gaugesContainer.findViewById(id).findViewById(R.id.gauges_barvalue));
        if (gauge != null) {
            updateGauge(hours + " hrs", gauge);
        }
    }

    private void setCircularGaugeText(TextView gaugeTextView, double hours) {
        if (gaugeTextView != null) {
            updateGauge(hours + "", gaugeTextView);
            drivingHoursLastPercentage = hours + "";
            Log.d(TAG, "updateCircularBarValue: setCircularGaugeText: drivingHoursLastPercentage: " + drivingHoursLastPercentage);
        }
    }


    private void setMilesTodayText(double miles) {
//        Log.d(TAG, "MapAutoUpdateTask: setMilesTodayText: miles: " + miles);
        TextView gauge = gaugesContainer.findViewById(R.id.gauges_milestoday);
        if (isCycleEnded) {
            // 2022.08.16 If the cycle is ended we should set the gauge to zero
            Log.d(TAG, "MapAutoUpdateTask: setMilesTodayText: isCycleEnded: ");
            miles = 0;
        }

        updateMilesToday(miles);
//        updateGauge("Miles Today: " + ((int) miles), gauge);
    }

    void updateMilesToday(double miles) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: miles: " + ("" + ((int) miles)));
                    int tempMiles = ((int) miles);
                    if (tempMiles < 0) {
                        tempMiles = 0;
                    }
                    ((TextView) mView.findViewById(R.id.gauges_miles_today)).setText(("" + tempMiles));
                }
            });
        }
    }


    void updateGauge(String status, TextView gauge) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    Log.d(TAG, "MapAutoUpdateTask: run: status: " + status);
                    gauge.setText(status);
                }
            });
        }
    }


//    private void setProgressBar(int id, Long progress, Long hoursLeft) {
//        setProgressBar(id, progress, hoursLeft, "hrs");
//    }
//
//    private void setProgressBar(int id, Long progress, Long hoursLeft, String unit) {
////        Log.d(TAG, "setProgressBar: progress: " + progress + " ");
//        int value = progress != null && progress > 0 ? progress.intValue() : 0;
//
//        ProgressBar bar = (ProgressBar) gaugesContainer.findViewById(id).findViewById(R.id.gauges_barprogress);
//
//        if (progress != null && progress < 0)
//            bar.getProgressDrawable().setTint(0xFFFF0000);
//
//        bar.setProgress(value);
//
//        TextView textView = (TextView) gaugesContainer.findViewById(id).findViewById(R.id.gauges_barhours);
//
//        if (hoursLeft != null) {
//            long hoursModule = hoursLeft >= 0 ? hoursLeft : hoursLeft * -1;
//            textView.setText(hoursModule + " " + unit);
//        } else
//            textView.setText("0 " + unit);
//
//        if (hoursLeft != null)
//            textView.setTextColor(hoursLeft > 0 ? 0xFF1C80CF : 0xFFFF0000);
//    }


    private void setProgressBarUpdate(int id, double progress, long maxValue) {
        Log.d(TAG, "setProgressBar: progress: " + progress + " maxValue: " + maxValue);


        ProgressBar bar = (ProgressBar) gaugesContainer.findViewById(id).findViewById(R.id.gauges_barprogress);
        if (progress > maxValue) {
            bar.getProgressDrawable().setTint(0xFFFF0000);
        } else if (progress == 0) {
            // 2022.08.17 we need to make sure that we have the white color
            bar.getProgressDrawable().setTint(0xFFFFFFFF);
        } else {
            // 2022.08.19 we need to make sure that we have the default blue color
            bar.setProgressTintList(ColorStateList.valueOf(0XFF3D7ED3));
        }


        TextView textView = (TextView) gaugesContainer.findViewById(id).findViewById(R.id.gauges_barhours);
//        textView.setText(maxValue + " hrs");
        textView.setText((maxValue / 60) + " hrs");

        bar.setMax((int) maxValue);
        bar.setProgress((int) progress);

        if (progress > maxValue) {
//            textView.setTextColor(Color.RED);
        }
    }

    private void updateCircularProgressBar(double progress, long maxValue, ImageView imageView, int type) {
        Log.d(TAG, "updateCircularProgressBar: setProgressBar: progress: " + progress + " maxValue: " + maxValue);

        int percentage = 0;
        if (progress > maxValue) {
            percentage = 270;
        } else if (progress == 0) {
            percentage = 0;
        } else {
            double progressInHours = (maxValue / 60);
            Log.d(TAG, "updateCircularProgressBar: progressInHours: " + progressInHours);
            double circularBarMaxValue = (270 / progressInHours);
            Log.d(TAG, "updateCircularProgressBar: circularBarMaxValue: " + circularBarMaxValue);
            percentage = (int) ((progress / 60) * circularBarMaxValue);

        }
        if (type == 1) {
            drivingLastPercentage = percentage;
        } else if (type == 2) {
            breakLastPercentage = percentage;
        } else if (type == 3) {
            shiftLastPercentage = percentage;
        } else if (type == 4) {
            cycleLastPercentage = percentage;
        }
        updateCircularProgressBar(imageView, percentage);
    }

    void updateCircularProgressBar(ImageView imageView, int percentage) {
        if (imageView == null) {
            return;
        }
        if (mActivity != null && !mActivity.isDestroyed()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "updateCircularProgressBar: percentage: " + percentage);
                    GlideApp.with(mActivity)
                            .load(getWidgetBitmapUpdate(mActivity, percentage))
                            .into(imageView);
                }
            });
        }
    }


    //endregion
    void findPlaceDetail(String placeId) {
//        Log.d(TAG, "SearchRelevant: findPlaceDetail: ");

        String finalUrl = "https://maps.googleapis.com/maps/api/place/details/json?key=AIzaSyCkXY-OOuAIGFiHisd0EAaQ5m92OmG-qHg&placeid=" + placeId;
        Log.d(TAG, "SearchRelevant: finalUrl: " + finalUrl);

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, finalUrl, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                try {
//                    Log.d(TAG, "SearchRelevant: onResponse: ");
                    JSONObject jsonObj = new JSONObject(response);

                    JSONObject resultJsonObject = jsonObj.getJSONObject("result");
                    JSONObject geometryJsonObject = resultJsonObject.getJSONObject("geometry");
                    JSONObject locationJsonObject = geometryJsonObject.getJSONObject("location");
                    double lat = locationJsonObject.getDouble("lat");
                    double lon = locationJsonObject.getDouble("lng");
                    String firstPhoto = "";
                    if (resultJsonObject.has("photos")) {

//                        Log.d(TAG, "SearchRelevant: onPostExecute: photos: " + resultJsonObject.getJSONArray("photos"));
                        JSONArray photos = resultJsonObject.getJSONArray("photos");
                        firstPhoto = photos.getJSONObject(0).getString("photo_reference");
                    }
                    onPathFound(new LatLng(lat, lon), firstPhoto);

                } catch (JSONException e) {
                    e.printStackTrace();
                    Log.d(TAG, "SearchRelevant: onResponse: JSONException: " + e.getMessage());
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                Log.d(TAG, "SearchRelevant: onErrorResponse: error: " + error);
                String body;

            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(120), //After the set time elapses the request will timeout
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    /*
    July 25 2022, we need to get the duration with traffic for each segment.
     */
    public void getSegmentETA(LatLng startLocation, LatLng endLocation, String segmentId) {
        if (startLocation != null && endLocation != null) {
            Routing routing = new Routing.Builder()
                    .key(getString(R.string.google_maps_key))
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(directionListener)
                    .alternativeRoutes(false)
                    .waypoints(startLocation, endLocation)
                    .routeId(segmentId)
                    .build();
            routing.executeOnExecutor(MainMenuActivity.threadPoolExecutor);
        }
    }

    public void onPathFound(LatLng endLocation, String photo) {
//        Log.d(TAG, "SearchRelevant: onPathsFound: ");
        destinationLocation = endLocation;
        destinationPhoto = photo;

//            Log.d(TAG, "SearchRelevant: onPathFound: googleKey: " + getString(R.string.google_maps_key));
//            Log.d(TAG, "SearchRelevant: onPathFound: directionListener: " + directionListener);
//            Log.d(TAG, "SearchRelevant: onPathFound: lastLocation: " + mapFragment.getMyLastLocation());
//            Log.d(TAG, "SearchRelevant: onPathFound: destinationLocation: " + destinationLocation);
        if (mapFragment.getMyLastLocation() != null && destinationLocation != null) {
//                Log.d(TAG, "SearchRelevant: onPathsFound: mapFragment.getMyLastLocation() != null && destinationLocation != null");
            Routing routing = new Routing.Builder()
                    .key(getString(R.string.google_maps_key))
                    .travelMode(AbstractRouting.TravelMode.DRIVING)
                    .withListener(directionListener)
                    .alternativeRoutes(true)
                    .waypoints(mapFragment.getMyLastLocation(), destinationLocation)
                    .routeId("Route")
                    .build();

//                routing.execute();
//                routing.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            routing.executeOnExecutor(MainMenuActivity.threadPoolExecutor);
        }
    }

    void setIds(View view) {
        Log.d(TAG, "updateDistanceValuesOffline: setIds: ");
        idsView = view;

        drop = view.findViewById(R.id.drop);
        pickup = view.findViewById(R.id.pickup);

        speedCheckLayout = view.findViewById(R.id.speedCheckLayout);
        breakLayout = view.findViewById(R.id.break_layout);
        sleepLayout = view.findViewById(R.id.sleep_layout);
        cycleCounterLayout = view.findViewById(R.id.cycle_counter_layout);

        breakTime = view.findViewById(R.id.break_time_until_now);
        sleeperTime = view.findViewById(R.id.sleep_time_until_now);

        endBreak = view.findViewById(R.id.end_break);
        exitSleeper = view.findViewById(R.id.end_sleep);

        cycleCounterTitle = view.findViewById(R.id.cycle_counter_title);
        cycleCounterTime = view.findViewById(R.id.cycle_counter_time_until_now);

        gaugesIcon = view.findViewById(R.id.btn_gagues);

        loadingLayout = view.findViewById(R.id.loading_layout);
        progressBar = view.findViewById(R.id.progressBar);
        loadingText = view.findViewById(R.id.loading_feedback_text);

        speechInstructionIcon = view.findViewById(R.id.btn_speech_instructions);
        mapAssets = view.findViewById(R.id.btn_map_assets);
        mapAssetConstraintLayout = view.findViewById(R.id.map_asset_constraint_layout);
        closeMapAsset = view.findViewById(R.id.close_map_asset);
        selectAllMapAsset = view.findViewById(R.id.select_all_map_asset);
        deselectAllMapAsset = view.findViewById(R.id.deselect_all_map_asset);
        saveMapAsset = view.findViewById(R.id.save_map_asset);

        expandableListView = view.findViewById(R.id.expandable_list_view);
        amenitiesListView = view.findViewById(R.id.expandable_list_view_for_ameneties);

        speedLimitLayout = view.findViewById(R.id.speed_limit_layout);
        yourSpeedLayout = view.findViewById(R.id.your_speed_layout);
        yourSpeedInnerLayout = view.findViewById(R.id.your_speed_inner_layout);
        speedLimit = view.findViewById(R.id.speed_limit);
        yourSpeed = view.findViewById(R.id.your_speed);


        Log.d(TAG, "updateDistanceValuesOffline: setIds: isTablet: " + isTablet);
        if (isTablet) {
            Log.d(TAG, "updateDistanceValuesOffline: setIds: isTablet: " + isTablet);
            forKeyboard = view.findViewById(R.id.forKeyboard);
            Log.d(TAG, "updateDistanceValuesOffline: setIds: isTablet: " + isTablet);
            amenitiesLayout = view.findViewById(R.id.amenities_layout);

//        Oct 18, 2022  -   Gauges New Layout Bottom Bar (Love - TA - Pilot - Rest Area)
            Log.d(TAG, "updateDistanceValuesOffline: setIds: gaugesPilotStationDistance: " + gaugesPilotStationDistance);
            gaugesPilotStationDistance = view.findViewById(R.id.gauges_pilot_station_distance);
            Log.d(TAG, "updateDistanceValuesOffline: setIds: gaugesPilotStationDistance: " + gaugesPilotStationDistance);

            gaugesLoveStationDistance = view.findViewById(R.id.gauges_love_station_distance);
            gaugesTAStationDistance = view.findViewById(R.id.gauges_ta_station_distance);
            gaugesRestAreaStationDistance = view.findViewById(R.id.gauges_rest_area_station_distance);

        }

        disconnectELD = getActivity().findViewById(R.id.disconnect_eld);
        btnMalfunction = getActivity().findViewById(R.id.btn_malfunction);
        btnDiagnostics = getActivity().findViewById(R.id.btn_diagnostics);

    }

    void setIdsFromActivity() {
        displayedCurrentTime = getActivity().findViewById(R.id.displayCurrentTime);
    }

    void setControlsVisibility() {

//        Sep 19, 2022  -   Dragos requested to hide cameraIc for now
        cameraIc.setVisibility(View.GONE);
        setRestStationsVisibility(false);
        setGasStationsVisibility(false);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        sessionManagement = new SessionManagement(getContext());
        maxSpeedHashMap = new HashMap<>();
//        pendingIntentHashMap = new HashMap<>();

//        connectionMonitor = ((MainMenuActivity) getActivity()).connectionMonitor;
        isTablet = getResources().getBoolean(R.bool.isTablet);
        nightModeReceiver = NightModeReceiver.register(getActivity(), this);
        screenWidthFromDisplayMetrics = getResources().getDisplayMetrics().widthPixels;
        screenHeightFromDisplayMetrics = getResources().getDisplayMetrics().heightPixels;

//        mIcon = BitmapDescriptorFactory.fromResource(R.drawable.current_location_marker);

//        geofencingClient = LocationServices.getGeofencingClient(getContext());
//        geofenceHelper = new GeofenceHelper(getContext());

//    July 28, 2022 -   We should change the duration of rest from 34 hours to 10 mints
        thirtyFourHoursInSeconds = (10 * 60);
//        Aug 01, 2022  -   changed to 2 for testing
        thirtyFourHoursInSeconds = (2 * 60);

        Log.d(TAG, "initialize: activityListener: setActivityListener: ");
        ((MainMenuActivity) getActivity()).setActivityListener(DriveFragmentBase.this);

//        Aug 03, 2022  -   Save the odometer value since shift started when drive fragment base initially loaded
        odometerStartForCurrentTruck = rules.getOdometerSinceShiftStartedFromDB();

        progressDialog = new ProgressDialog(getContext());
        mapAssetList = new ArrayList<>();
        headerDataList = new ArrayList<>();
        childDataList = new HashMap<>();
        mapAssetContentList = new ArrayList<>();
        mapAssetContentMarkerList = new ArrayList<>();


//        Nov 07, 2022  -
        amenitiesHeaderDataList = new ArrayList<>();
        amenitiesChildDataList = new HashMap<>();

//        Nov 15, 2022  -   We should check if IC is not available then hide this search layout
        if (!UiUtils.isOnline(getActivity())) {
            setSearchVisibility(false);
            isICAvailable = false;
        } else {
            isICAvailable = true;
        }

//        Dec 30, 2022  -
        textToSpeech = new TTS(getActivity(), Locale.US);
    }

    void initializeArrayList() {
        loveAmenitiesList = new ArrayList<>();
        pilotAmenitiesList = new ArrayList<>();
        tAAmenitiesList = new ArrayList<>();
        restAreaAmenitiesList = new ArrayList<>();
        sortedLoveAmenitiesList = new ArrayList<>();
        sortedPilotAmenitiesList = new ArrayList<>();
        sortedTAAmenitiesList = new ArrayList<>();
        sortedRestAreaAmenitiesList = new ArrayList<>();
    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        drop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: drop: isSearchRoutesShown: " + isSearchRoutesShown);
                if (!isSearchRoutesShown) {
                    openDropDialog();
                }
            }
        });

        pickup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: pickup: isSearchRoutesShown: " + isSearchRoutesShown);
//                Aug 04, 2022  -   So if search routes shown then on behind buttons like drop, pickup and status should not be clickable
                if (!isSearchRoutesShown) {
                    openPickupDialog();
                }
            }
        });

        endBreak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: endBreak: BreakEndedCheck: showBreakLayout:false");
                showBreakLayout(false);
            }
        });

        exitSleeper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Log.d(TAG, "onClick: exitSleeper:");

                showSleeperLayout(false);
            }
        });

        cancelableCallback = new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                animationInProgress = false;
            }

            @Override
            public void onCancel() {
                animationInProgress = false;
            }
        };

        speechInstructionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: speech: isSpeechEnabled: " + isSpeechEnabled);
                if (isSpeechEnabled) {
                    isSpeechEnabled = false;
                    speechInstructionIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_mic_24));
                    disableMic();
                } else {
                    isSpeechEnabled = true;
                    speechInstructionIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_mic_off_24));
                    enableMic();
                }
            }
        });

        if (!isTablet) {

            gaugesIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    addGaugesBottomSheet();
//                gaugesLayoutBS.setVisibility(View.VISIBLE);
                }
            });

        }

        loadingLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingLayout.setVisibility(View.GONE);
            }
        });

        mapAssets.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapAssetConstraintLayout.setVisibility(VISIBLE);

//                setupMapAssets();
                getMapAssetsCategoryAndSubCategories();
                setExpandableAdapterToMapAssetListView();
            }
        });

        closeMapAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mapAssetConstraintLayout.setVisibility(View.GONE);
            }
        });

        selectAllMapAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAllMapAssets(true);
            }
        });

        deselectAllMapAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkAllMapAssets(false);
            }
        });

        saveMapAsset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "saveMapAsset: onClick: ");
                setMapAssetWidgetsVisibility();

//                Sep 08, 2022  -   performing calculations in a thread because without it app kind of stuck while doing calculations
                GetMapAssets getMapAssets = new GetMapAssets();
                getMapAssets.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }
        });


        if (isTablet) {

            amenitiesLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    amenitiesLayout.setVisibility(GONE);
                }
            });

            mView.findViewById(R.id.lovesLayout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: amenity: lovesLayout: ");
//                    openDialogForLove("2065051");
                    if (nearestLoveAsset == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestLoveAsset.RecordId: " + nearestLoveAsset.RecordId);
                    openDialogForLove(nearestLoveAsset.RecordId);
                }
            });

            mView.findViewById(R.id.loves_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: amenity: loves_icon: ");
//                    openDialogForLove("2065051");
                    if (nearestLoveAsset == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestLoveAsset.RecordId: " + nearestLoveAsset.RecordId);
                    openDialogForLove(nearestLoveAsset.RecordId);
                }
            });

            mView.findViewById(R.id.first_icon_for_love).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: amenity: fuel_icon_for_love :");
//                    openDialogForLove("2065051");
                    if (nearestLoveAsset == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestLoveAsset.RecordId: " + nearestLoveAsset.RecordId);
                    openDialogForLove(nearestLoveAsset.RecordId);
                }
            });

            mView.findViewById(R.id.second_icon_for_love).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: amenity: eating_icon_for_love :");
//                    openDialogForLove("2065051");
                    if (nearestLoveAsset == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestLoveAsset.RecordId: " + nearestLoveAsset.RecordId);
                    openDialogForLove(nearestLoveAsset.RecordId);
                }
            });

            mView.findViewById(R.id.third_icon_for_love).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d(TAG, "onClick: amenity: more_icon_for_love :");
//                    openDialogForLove("2065051");
                    if (nearestLoveAsset == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestLoveAsset.RecordId: " + nearestLoveAsset.RecordId);
                    openDialogForLove(nearestLoveAsset.RecordId);
                }
            });


            mView.findViewById(R.id.pilot_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestPilotAsset == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestPilotAsset.RecordId: " + nearestPilotAsset.RecordId);
                    openDialogForPilot(nearestPilotAsset.RecordId);
                }
            });

            mView.findViewById(R.id.pilot_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestPilotAsset == null) {
                        return;
                    }

                    openDialogForPilot(nearestPilotAsset.RecordId);
                }
            });

            mView.findViewById(R.id.first_icon_for_pilot).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestPilotAsset == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestPilotAsset.RecordId: " + nearestPilotAsset.RecordId);
                    openDialogForPilot(nearestPilotAsset.RecordId);
                }
            });

            mView.findViewById(R.id.second_icon_for_pilot).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestPilotAsset == null) {
                        return;
                    }

                    openDialogForPilot(nearestPilotAsset.RecordId);
                }
            });

            mView.findViewById(R.id.third_icon_for_pilot).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestPilotAsset == null) {
                        return;
                    }

                    openDialogForPilot(nearestPilotAsset.RecordId);
                }
            });


            mView.findViewById(R.id.ta_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestTaAsset == null) {
                        return;
                    }

                    openDialogForTA(nearestTaAsset.RecordId);
                }
            });

            mView.findViewById(R.id.ta_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestTaAsset == null) {
                        return;
                    }

                    openDialogForTA(nearestTaAsset.RecordId);
                }
            });

            mView.findViewById(R.id.first_icon_for_ta).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestTaAsset == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestTAAsset.RecordId: " + nearestTaAsset.RecordId);
                    openDialogForTA(nearestTaAsset.RecordId);
                }
            });

            mView.findViewById(R.id.second_icon_for_ta).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestTaAsset == null) {
                        return;
                    }

                    openDialogForTA(nearestTaAsset.RecordId);
                }
            });

            mView.findViewById(R.id.third_icon_for_ta).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestTaAsset == null) {
                        return;
                    }

                    openDialogForTA(nearestTaAsset.RecordId);
                }
            });


            mView.findViewById(R.id.rest_area_layout).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestRestArea == null) {
                        return;
                    }

                    openDialogForRestArea(nearestRestArea.RecordId);
                }
            });

            mView.findViewById(R.id.rest_area_icon).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestRestArea == null) {
                        return;
                    }

                    openDialogForRestArea(nearestRestArea.RecordId);
                }
            });

            mView.findViewById(R.id.first_icon_for_rest_area).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestRestArea == null) {
                        return;
                    }

                    Log.d(TAG, "onClick: nearestRestAsset.RecordId: " + nearestRestArea.RecordId);
                    openDialogForRestArea(nearestRestArea.RecordId);
                }
            });

            mView.findViewById(R.id.second_icon_for_rest_area).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestRestArea == null) {
                        return;
                    }

                    openDialogForRestArea(nearestRestArea.RecordId);
                }
            });

            mView.findViewById(R.id.third_icon_for_rest_area).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (nearestRestArea == null) {
                        return;
                    }

                    openDialogForRestArea(nearestRestArea.RecordId);
                }
            });


            expandableListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    return true;
                }
            });


            amenitiesListView.setOnGroupClickListener(new ExpandableListView.OnGroupClickListener() {
                @Override
                public boolean onGroupClick(ExpandableListView parent, View v, int groupPosition, long id) {
                    return true;
                }
            });

        }


    }


    void setMapAssetWidgetsVisibility() {
//        Log.d(TAG, "saveMapAsset: showSelectedAssets: ");
        mapAssetConstraintLayout.setVisibility(View.GONE);
        loadingLayout.setVisibility(VISIBLE);
        loadingText.setText("Loading...");
    }


    public String getMapAssetsFromAssetsFolder() {
        String json = null;
        try {
            InputStream is = getActivity().getAssets().open("map_assets.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    //    Check if the current speed exceeds the maximum speed then change the background from black to red
//    And check that if the speed layout background is already in red then don't change it again and again
    void updateViewForSpeed(float speed) {
//        Log.d(TAG, "maxSpeed: updateViewForSpeed: speed: "+speed);

        int currentSpeed = Math.round(speed);
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    setCurrentSpeedVisibility(currentSpeed);
                    setMaxSpeedVisibility(currentSpeed);
                }
            });
        }
    }

    void updateBottomBarStatus(String status) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!btnStatus.getText().toString().equalsIgnoreCase(status)) {
                        addLogGeneralData("previousStatus: " + btnStatus.getText() + " newStatus: " + status);
                    }
                    Log.d(TAG, "run: status: " + status);
                    btnStatus.setText(status);
                }
            });
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof Activity) {
            mActivity = (Activity) context;
        }
    }

    //    March 24, 2022    -   Added Speeding Events for Speeding STARTED and Speeding ENDED
    void updateMaxSpeedAndItsLayoutBackground(int speed) {
//        Log.d(TAG, "updateMaxSpeedAndItsLayoutBackground: currentSpeed: " + speed + " maxSpeed: in KMs: " + currentSegmentMaxSpeed);
//        Log.d(TAG, "maxSpeed:  speedTest: setMaxSpeedVisibility: currentSpeed: " + speed);

        int tempCurrentSegmentMaxSpeed = 0;
        if (currentSegmentMaxSpeed > 0) {
            tempCurrentSegmentMaxSpeed = (int) Math.round(Utils.convertKmsToMiles(currentSegmentMaxSpeed));
//            Log.d(TAG, "maxSpeed: onLocationChanged: maxSpeed: inMiles: " + tempCurrentSegmentMaxSpeed);
        }
//        Log.d(TAG, "eventFromGeofence: updateMaxSpeedAndItsLayoutBackground: maxSpeed: " + tempCurrentSegmentMaxSpeed);
//        maxSpeedTV.setText(mCurrentSegmentPosition + "_" + tempCurrentSegmentMaxSpeed);
//        maxSpeedTV.setText("" + tempCurrentSegmentMaxSpeed);
        speedLimit.setText("" + tempCurrentSegmentMaxSpeed);

//        Sep 20, 2022  -   applied red font color if your speed > speed limit
        int deltaSpeed = 2;

        if (speed > (currentSegmentMaxSpeed + deltaSpeed)) {
//            yourSpeed.setTextColor(Objects.requireNonNull(getActivity()).getResources().getColor(R.color.red));
            yourSpeed.setTextColor(Color.RED);
            if (!isSpeeding) {
                isSpeeding = true;
                rules.recordSpeedingELDEvent(getActivity(), isSpeeding, speed);
            }
        } else if (speed <= (currentSegmentMaxSpeed + deltaSpeed)) {
//            yourSpeed.setTextColor(getResources().getColor(R.color.yellow_shade_two));
            yourSpeed.setTextColor(Color.YELLOW);
            if (isSpeeding) {
                isSpeeding = false;
                rules.recordSpeedingELDEvent(getActivity(), isSpeeding, speed);
            }
        }


    }

    void setCurrentSpeedVisibility(int speed) {
//        Log.d(TAG, "speedTest: setCurrentSpeedVisibility: current: speed: " + speed);
//        if (speed >= 0) {
//            if (mphView.getVisibility() == View.GONE) {
//                mphView.setVisibility(View.VISIBLE);
//            }
//        } else {
//            if (mphView.getVisibility() == View.VISIBLE) {
//                mphView.setVisibility(View.GONE);
//            }
//        }

        if (speed >= 0) {
            if (yourSpeedLayout.getVisibility() == View.GONE) {
                yourSpeedLayout.setVisibility(View.VISIBLE);
            }
        } else {
            if (yourSpeedLayout.getVisibility() == View.VISIBLE) {
                yourSpeedLayout.setVisibility(View.GONE);
            }
        }


        int speedInMile = (int) Math.round(Utils.convertKmsToMiles(speed));
//        tvTxtMph.setText("" + speedInMile);
        yourSpeed.setText("" + speedInMile);
    }

    void setMaxSpeedVisibility(int currentSpeed) {

//        Applied Updated Layout Below
//        if (currentSegmentMaxSpeed > 0) {
//            if (maxSpeedTV.getVisibility() == View.GONE) {
//                maxSpeedTV.setVisibility(View.VISIBLE);
//            }
//            updateMaxSpeedAndItsLayoutBackground(currentSpeed);
//        } else {
//            if (maxSpeedTV.getVisibility() == View.VISIBLE) {
//                speedCheckLayout.setBackground(mActivity.getResources().getDrawable(R.drawable.bg_black_boarder));
//                maxSpeedTV.setVisibility(View.GONE);
//            }
//        }

//        currentSegmentMaxSpeed=50;

//        Sep 16, 2022  -
        if (currentSegmentMaxSpeed > 0) {
            if (speedLimitLayout.getVisibility() == View.GONE) {
                speedLimitLayout.setVisibility(View.VISIBLE);
            }
            updateMaxSpeedAndItsLayoutBackground(currentSpeed);
        } else {
            if (speedLimitLayout.getVisibility() == View.VISIBLE) {
//                yourSpeedInnerLayout.setBackground(mActivity.getResources().getDrawable(R.drawable.curved_background_yellow));
                speedLimitLayout.setVisibility(View.GONE);
            }
        }
    }

    void updateLoginViewAsPerMostRecentLoginLogoutEvent() {

        try {
            EldEvent item = rules.getMostRecentEldLoginLogout(sessionManagement.getKeyLogin());
//            Log.d(TAG, "getMostRecentEldLoginLogout: item: eventCode: " + item.EventCode + " And creationDate: " + item.CreationDate);

            if (item.EventCode.equalsIgnoreCase("1")) {
                rules.loginAuthenticatedUserAsDriver();
                updateLoginView();
            }

        } catch (Throwable throwable) {
            Log.d(TAG, "updateLoginViewAsPerMostRecentLoginLogoutEvent: throwable: " + throwable.getMessage());
        }

    }

    String drivingStatus = "";

    public void updateDutyStatusForBottomBar() {
//        Log.d(TAG, "updateDutyStatusForBottomBar: ");
//          March 15, 2022  -   We should show the last duty status even driver is not logged in
        drivingStatus = rules.getDrivingStatus();
//        Log.d(TAG, "updateDutyStatusForBottomBar: drivingStatus: " + drivingStatus);
//        Log.d(TAG, "MapAutoUpdateTask: doInBackground: updateDutyStatusForBottomBar: drivingStatus: " + drivingStatus);
        drivingStatus = "Status";

        String drivingStatusMostRecent = rules.getMostRecentEventCodeAsStringForEventType(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS);
//        Log.d(TAG, "updateDutyStatusForBottomBar: drivingStatusMostRecent: " + drivingStatusMostRecent);
        if (drivingStatusMostRecent == null)
            return;

        if (drivingStatusMostRecent.equalsIgnoreCase("1")) {
            drivingStatus = "Off Duty";
        } else if (drivingStatusMostRecent.equalsIgnoreCase("2")) {
            drivingStatus = "Sleeper";
        } else if (drivingStatusMostRecent.equalsIgnoreCase("3")) {
            drivingStatus = "Driving";
        } else if (drivingStatusMostRecent.equalsIgnoreCase("4")) {
            drivingStatus = "On Duty";
        }

        if (drivingStatus != null)
            updateStatusRadioName(drivingStatus);
    }

    public void updateWorkStatusForBottomBar() {

        String workStatus = "";
        for (int i = 0; i < rules.getDriverDutySubStatuses().size(); i++) {
            DutyEvent dutyEvent = rules.getDriverDutySubStatuses().get(i);
            if (dutyEvent.isSelected()) {
                if (rules.getDriverDutySubStatuses().get(i).getName().contains("Personal Conveyance")) {
                    workStatus = "PC";
                } else if (rules.getDriverDutySubStatuses().get(i).getName().contains("Yard Moves")) {
                    workStatus = "YM";
                } else if (rules.getDriverDutySubStatuses().get(i).getName().contains("Work")) {
                    workStatus = "Work";
                }
            }
        }

        if (btnWork != null) {
            btnWork.setText(workStatus);
        }
    }

    void checkIsBreakStarted(boolean isForceBreakClose) {
//        Log.d(TAG, "checkIsBreakStarted: activityListener: isForceBreakClose: " + isForceBreakClose);
        BusinessRules.EventCode mostRecentBreakEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.BREAK, sessionManagement);
        BusinessRules.EventCode mostRecentDutyEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, sessionManagement);
//        Log.d(TAG, "checkIsBreakStarted: mostRecentBreakEventCode: " + mostRecentBreakEventCode);
//        Log.d(TAG, "checkIsBreakStarted: mostRecentDutyEventCode: " + mostRecentDutyEventCode);
        if (mostRecentBreakEventCode == BusinessRules.EventCode.ON_BREAK_STARTED) {
            if (mostRecentDutyEventCode == BusinessRules.EventCode.ON_DUTY_NOT_DRIVING) {
//                Log.d(TAG, "checkIsBreakStarted: BreakEndedCheck: showBreakLayout:true");
                showBreakLayout(true);
            }
        } else {
            if (isForceBreakClose) {
                closeBreakLayoutForcefully();
            }
//                April 22, 2022    -   What should we do with bad data
//                                        Should we force break ended?
        }
    }

    void checkIsSleeperStarted() {
//        Log.d(TAG, "checkIsBreakStarted: ");
        BusinessRules.EventCode mostRecentEventCode = rules.getDutyEventCode();
//        Log.d(TAG, "checkIsSleeperStarted: mostRecentEventCode: " + mostRecentEventCode);
        if (mostRecentEventCode == BusinessRules.EventCode.SLEEPER_BERTH) {
            showSleeperLayout(true);
        } else {
            closeSleeperLayoutForcefully();
        }
    }


    //    July 19, 2022 -
    void checkIsCycleEnded(int param) {

        TruckLogHeader truckLogHeader = rules.getMostRecentTruckLogHeader();
        Log.d(TAG, "endCycle: checkIsCycleEnded: truckLogHeader: " + truckLogHeader + "param: " + param);
        if (truckLogHeader != null && truckLogHeader.EndDate != null && truckLogHeader.EndTime != null && !truckLogHeader.EndDate.isEmpty() && !truckLogHeader.EndTime.isEmpty()) {
            Log.d(TAG, "endCycle: checkIsCycleEnded: truckLogHeader: " + truckLogHeader.EndDate);
//        July 19, 2022 -   This means the cycle is ended if the end date and time is not empty else the cycle still continues(live)
//            show counter
            String endDateTime = truckLogHeader.EndDate + " " + truckLogHeader.EndTime;
//            Log.d(TAG, "endCycle: checkIsCycleEnded: endDateTime: " + endDateTime + " RecordId: " + truckLogHeader.MobileRecordId);

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.FORMAT_DATE_TIME_SEC);
            Date endDateAndTime = null;
            isCycleEnded = true;
            try {
                endDateAndTime = simpleDateFormat.parse(endDateTime);
                Log.d(TAG, "endCycle: checkIsCycleEnded: endDateAndTime: " + endDateAndTime);
            } catch (ParseException parseException) {
                Log.d(TAG, "endCycle: checkIsCycleEnded: parseException: " + parseException.getMessage());
            }

            if (endDateAndTime != null) {
//            current timestamp - truck log header end date time
                Log.d(TAG, "endCycle: checkIsCycleEnded: DateUtils.getTimestampInDouble(): " + DateUtils.getTimestampInDouble() + " endDateAndTime.getTime(): " + endDateAndTime.getTime());
                double delta = DateUtils.getTimestampInDouble() - (endDateAndTime.getTime() / 1000);
//                Log.d(TAG, "endCycle: checkIsCycleEnded: delta: " + delta);

//                Log.d(TAG, "endCycle: checkIsCycleEnded: (34 * 60 * 60) - delta: " + (thirtyFourHoursInSeconds - delta));

//                July 19, 2022 -   We should show the counter only if the rest time is smaller than 34 hours
                if (delta < thirtyFourHoursInSeconds) {
                    //2022.08.17 cycleCounterLayout.setVisibility(View.VISIBLE);
                    updateCounterLayoutVisibility(View.VISIBLE);
                    startCycleEndedTimer(((double) endDateAndTime.getTime()));
                } else {
                    // 2022.08.17 cycleCounterLayout.setVisibility(View.GONE);
                    updateCounterLayoutVisibility(View.GONE);
                }
            }
        } else {
            Log.d(TAG, "else: endCycle: checkIsCycleEnded: " + cycleCounterLayout + " param: " + param);
            /* 2022.08.16 EXCEPTION calling "cycleCounterLayout.getVisibility()"
            Log.d(TAG, "else: endCycle: checkIsCycleEnded: " + (cycleCounterLayout.getVisibility() == VISIBLE) + " param: " + param);
            */
//            July 19, 2022 -   if the cycle counter is shown then we will set its visibility to gone
            isCycleEnded = false;
            /* 2022.08.16 EXCEPTION
            if (cycleCounterLayout.getVisibility() == VISIBLE) {
                cycleCounterLayout.setVisibility(View.GONE);
            }
            */
            updateCounterLayoutVisibility(View.GONE);
        }
        Log.d(TAG, "endCycle: checkIsCycleEnded: isCycleEnded: " + isCycleEnded + "param: " + param);
    }

    // 2022.08.17 we should set the the visibility in a UiThread
    void updateCounterLayoutVisibility(int visibility) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "updateCounterLayoutVisibility: cycleCounterLayout:" + cycleCounterLayout);
                    if (cycleCounterLayout != null) {
                        cycleCounterLayout.setVisibility(visibility);
                    }
                }
            });
        }
    }

    public boolean isStatusDialogShown() {
        return isStatusDialogShown;
    }

    void showBreakLayout(boolean visibility) {
//        Log.d(TAG, "BreakEndedCheck: showBreakLayout: visibility: " + visibility);
//        Log.d(TAG, "showBreakLayout: checkIsBreakStarted: showBreakLayout: ");
        if (visibility) {
            isBreakStarted = true;
            rules.setBreakStarted(true);
            breakLayout.setVisibility(View.VISIBLE);
            startTimer();
        } else {
            isBreakStarted = false;
            rules.setBreakStarted(false);
            breakLayout.setVisibility(View.GONE);
            closeTimer();
            systemBreakStartTime = 0;
            recordEndBreakEld();

//          March 25, 2022  -   When driver ends a break then we should update the bottom bar status to the last OnDutyStatus
            updateDutyStatusForBottomBar();
        }
    }

    void closeBreakLayoutForcefully() {
//        Log.d(TAG, "closeBreakLayoutForcefully: activityListener: ");
        isBreakStarted = false;
        rules.setBreakStarted(false);
        breakLayout.setVisibility(View.GONE);
        closeTimer();
        systemBreakStartTime = 0;

//          March 25, 2022  -   When driver ends a break then we should update the bottom bar status to the last OnDutyStatus
        updateDutyStatusForBottomBar();
    }

    void closeSleeperLayoutForcefully() {
//        Log.d(TAG, "checkIsSleeperStarted: closeSleeperLayoutForcefully: activityListener: ");

        isSleeperStarted = false;
        sleepLayout.setVisibility(View.GONE);
        closeSleeperTimer();
        systemSleepStartTime = 0;

        updateDutyStatusForBottomBar();
    }

    void hideBreakLayout() {

        isBreakStarted = false;
        rules.setBreakStarted(false);
        breakLayout.setVisibility(View.GONE);
        closeTimer();
        systemBreakStartTime = 0;

//          March 25, 2022  -   When driver ends a break then we should update the bottom bar status to the last OnDutyStatus
        updateDutyStatusForBottomBar();
    }


    void startTimer() {
//        Log.d(TAG, "timer: startTimer: ");
//        startTime = System.currentTimeMillis();
        BusinessRules.EventCode mostRecentBreakEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.BREAK, sessionManagement);
        if (mostRecentBreakEventCode == BusinessRules.EventCode.ON_BREAK_STARTED) {
            Double breakStartedTimeStamp = rules.getMostRecentEventCreationTime(BusinessRules.EventType.BREAK, null);
            startTime = breakStartedTimeStamp * 1000;
        }

        systemBreakStartTime = System.currentTimeMillis();
        timerHandler.postDelayed(timer, 0);
    }

    Runnable timer = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "timer: run: ");
            Double millis = System.currentTimeMillis() - startTime;
//            Log.d(TAG, "run: startTime: " + startTime);
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;
//            May 16, 2022  -   We should use % for mints as well
            minutes %= 60;
            seconds %= 60;
            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);

//            Log.d(TAG, "run: BreakEndedCheck: rules.isBreakStarted: " + rules.isBreakStarted);
            if (!rules.isBreakStarted) {
//                Log.d(TAG, "run: BreakEndedCheck: showBreakLayout:false");
//                April 22, 2022    -   We just need to close the UI counter for break as
//                ELD Break Ended event is already recorded by Driving in MainMenuActivity
//                showBreakLayout(false);
                hideBreakLayout();
            } else {
                breakTime.setText(formattedTime);
                timerHandler.postDelayed(this, 500);
            }
        }
    };

    void closeTimer() {
//        Log.d(TAG, "closeTimer: ");
        if (timerHandler != null) {
            timerHandler.removeCallbacks(timer);
        }
    }

    void recordEndBreakEld() {
        BusinessRules.EventCode mostRecentBreakEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.BREAK, sessionManagement);
        if (mostRecentBreakEventCode == BusinessRules.EventCode.ON_BREAK_STARTED) {
            rules.setEldEvent(getActivity(), BusinessRules.EventCode.ON_BREAK_ENDED, "Break Ended Manually", "Break Ended Manually");
        }
    }


    void showSleeperLayout(boolean visibility) {
        if (visibility) {
            isSleeperStarted = true;
            sleepLayout.setVisibility(View.VISIBLE);
            startSleeperTimer();
        } else {
            isSleeperStarted = false;
            sleepLayout.setVisibility(View.GONE);
            closeSleeperTimer();
            systemSleepStartTime = 0;

            rules.setEldEventToOnDuty(getActivity(), "Sleeper Ended");
            resetOdometerStart();
            drivingStatus = "On Duty";
            updateStatusRadioName("On Duty");
        }
    }


    void startSleeperTimer() {
//        Log.d(TAG, "BreakEndedCheck: timer: startSleeperTimer: ");

        BusinessRules.EventCode mostRecentBreakEventCode = rules.getMostRecentEventCodeForEventType(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, sessionManagement);
        if (mostRecentBreakEventCode == BusinessRules.EventCode.SLEEPER_BERTH) {
            Double breakStartedTimeStamp = rules.getMostRecentEventCreationTime(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS, null, BusinessRules.EventCode.SLEEPER_BERTH);
            sleepTime = breakStartedTimeStamp * 1000;
        }
        systemSleepStartTime = System.currentTimeMillis();
        sleeperTimerHandler.postDelayed(sleeperTimer, 0);
    }

    Runnable sleeperTimer = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "run: sleeperTimer: run: ");
            Double millis = System.currentTimeMillis() - sleepTime;
//            Log.d(TAG, "run: sleeperTimer: sleepTime: " + sleepTime);
            int seconds = (int) (millis / 1000);
            int minutes = seconds / 60;
            int hours = minutes / 60;

//            May 16, 2022  -   We should use % for mints as well
            minutes %= 60;
            seconds %= 60;

//            Log.d(TAG, "run: sleeperTimer: minutes: " + (seconds / 60) + " divide: by: " + (seconds / 3600));
//            Log.d(TAG, "run: sleeperTimer: hours: " + hours + " minutes: " + minutes + " seconds: " + seconds);
            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            sleeperTime.setText(formattedTime);
            sleeperTimerHandler.postDelayed(this, 500);
        }
    };

    void closeSleeperTimer() {
//        Log.d(TAG, "BreakEndedCheck: closeTimer: ");
        if (sleeperTimerHandler != null) {
            sleeperTimerHandler.removeCallbacks(sleeperTimer);
        }
    }

    void recordBreakContent() {

        String[] objectIdType = rules.getTruckLogEntryForToday();
        if (objectIdType == null || objectIdType[0] == null || objectIdType[1] == null) {
            objectIdType = rules.findObjectIdTypeByMobileRecordId(objectIdType);
/*                if (objectIdType != null && !StringUtils.isNullOrWhitespaces(objectIdType[0]))
                    rules.updateTruckLogEntryObjectIdType("detail", objectIdType[4], objectIdType[0], objectIdType[1]);*/
        }

        if (objectIdType != null) {
            String objectId = objectIdType[0];
            String objectType = objectIdType[1];

            if (objectId != null && objectType != null) {
                rules.recordTruckLogDrivingParameters(objectId, objectType, "trucklog", getActivity(), "Break Started", "");
            }
        }
    }

    private FusedLocationProviderClient mFusedLocationProviderClient;
    LocationManager locationManager;


    public void getDeviceLocation(Context context, String dialogTypeTitle) {
//        Log.d(TAG, "getDeviceLocation: Getting device current location");

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
        if (checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
//                    Log.d(TAG, "onComplete: task.isSuccessful: " + task.isSuccessful());
                    Location currentLocation = (Location) location.getResult();

                    Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                    List<Address> addresses = null;
                    try {
                        if (currentLocation != null) {
                            addresses = geocoder.getFromLocation(currentLocation.getLatitude(), currentLocation.getLongitude(), 8);
                            rules.setDeviceGPSLongitude(currentLocation.getLatitude());
                            rules.setDeviceGPSLongitude(currentLocation.getLongitude());
                            recordBreakContent();
                            if (dialogTypeTitle.equalsIgnoreCase("Break")) {
//                                Log.d(TAG, "onComplete: BreakEndedCheck: location: showBreakLayout:true");
                                showBreakLayout(true);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                } else {
//                    Log.d(TAG, "getDeviceLocation: onComplete :Location Not Found (current location is null)");
                }

            }
        });
    }

    int getCurrentSegmentMaxSpeed(int currentSegment) {
//        Log.d(TAG, "eventFromGeofence: maxSpeed: getCurrentSegmentMaxSpeed: currentSegmentPosition: " + currentSegment+" ");
//        Log.d(TAG, "eventFromGeofence: maxSpeed: getCurrentSegmentMaxSpeed: maxSpeedHashMap: size: " + maxSpeedHashMap.size());

//        if (currentSegment < maxSpeedHashMap.size()) {
        if (maxSpeedHashMap.get(currentSegment) != null) {
//            Log.d(TAG, "eventFromGeofence: maxSpeed: getCurrentSegmentMaxSpeed: currentMaxSpeed: " + maxSpeedHashMap.get(currentSegment));
            return maxSpeedHashMap.get(currentSegment);
        }
//        }
        return 0;
    }

    Handler handler = new Handler();
    Runnable runnable;
    int delay = 60 * 1000;

    void runHandlerToCheckIsBleConnected() {
        handler.postDelayed(runnable = new Runnable() {
            public void run() {
                //do something
//                Log.d(TAG, "run: after 60 seconds");
                if (!rules.isBleConnected())
                    ((MainMenuActivity) getActivity()).showDeviceConnectedPopup();
//                handler.postDelayed(runnable, delay);
                handler.removeCallbacks(runnable);
            }
        }, delay);
    }

    public void setRestStationsVisibility(boolean show) {
        if (show) {
            btnRestStation.setVisibility(View.VISIBLE);
        } else {
            btnRestStation.setVisibility(View.GONE);
        }
    }

    public void setGasStationsVisibility(boolean show) {
        if (show) {
            btnGasStation.setVisibility(View.VISIBLE);
        } else {
            btnGasStation.setVisibility(View.GONE);
        }
    }

    public void openPickupDialog() {
        PickupDialog pickupDialog = new PickupDialog(getContext(),
                new PickupDialog.PickupInterface() {
                    @Override
                    public void onYesPressed(String trailerNumber) {
//                        Log.d(TAG, "onYesPressed: ");
//                        Sep 29, 2022  -   TODO we need to pass the current truck number
                        createTrailerLog(trailerNumber, "pickup", null);
                    }

                    @Override
                    public void onCancelPressed() {
                        Log.d(TAG, "onCancelPressed: ");
                    }
                });
        pickupDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        pickupDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        pickupDialog.show();
    }

    public void openDropDialog() {
        DropDialog dropDialog = new DropDialog(getContext(),
                new DropDialog.DropInterface() {
                    @Override
                    public void onYesPressed(String trailerNumber) {
//                        Sep 29, 2022  -   TODO we need to pass the current truck number
                        createTrailerLog(trailerNumber, "drop", null);
                    }

                    @Override
                    public void onCancelPressed() {
                        Log.d(TAG, "onCancelPressed: ");
                    }
                });
        dropDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dropDialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dropDialog.show();
    }

    void createTrailerLog(String trailerNumber, String action, String truckNumber) {
//        Log.d(TAG, "createTrailerLog: ");
        TruckLogHeader truckLogHeader = rules.getOpenTruckLogHeader();
//        Log.d(TAG, "createTrailerLog: truckLogHeader: " + truckLogHeader);

        if (truckLogHeader == null)
//            TODO      Should we show some popup here?
            return;

        TruckLogDetail truckLogDetail = rules.getTruckLogDetailForToday(truckLogHeader.MobileRecordId, truckNumber);
//        Log.d(TAG, "createTrailerLog: truckLogDetail: " + truckLogDetail + " MobileRecordId: " + truckLogHeader.MobileRecordId);

        if (truckLogDetail == null) {
//            TODO      Should we show some popup here?
            return;
        }

        rules.createNewTrailerLogEntry(truckLogDetail.TruckNumber, trailerNumber, action, truckLogDetail.objectId, truckLogDetail.objectType,
                truckLogDetail.DriverRecordId);
    }


    void checkPermissionsBeforeAddingGeofence(String geofenceRequestId, double latitude, double longitude, float straightDistance) {
//        Log.d(TAG, "ABL: onReceive: checkPermissionsBeforeAddingGeofence: ");

        if (Build.VERSION.SDK_INT >= 29) {
            //We need background permission
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "ABL: checkPermissionsBeforeAddingGeofence: ACCESS_BACKGROUND_LOCATION: permission already granted: ");
                addGeofence(geofenceRequestId, latitude, longitude, straightDistance);
            } else {
//                Log.d(TAG, "ABL: checkPermissionsBeforeAddingGeofence: permission not granted: ");
                if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION)) {
//                    Log.d(TAG, "ABL: checkPermissionsBeforeAddingGeofence: permission not granted: shouldShowRequestPermissionRationale: ");
                    //We show a dialog and ask for permission
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                } else {
//                    Log.d(TAG, "ABL: checkPermissionsBeforeAddingGeofence: permission not granted: else: ");
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION}, BACKGROUND_LOCATION_ACCESS_REQUEST_CODE);
                }
            }

        } else {
            addGeofence(geofenceRequestId, latitude, longitude, straightDistance);
        }
    }


    private void addGeofence(String geofenceRequestId, double latitude, double longitude, float straightDistance) {
        Log.d(TAG, "onReceive: addGeofence: ");
        return;

//        Geofence geofence = geofenceHelper.getGeofence(geofenceRequestId, latitude, longitude,
//                Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_DWELL
//                        | Geofence.GEOFENCE_TRANSITION_EXIT, straightDistance);
//        GeofencingRequest geofencingRequest = geofenceHelper.getGeofencingRequest(geofence);
//        mPendingIntent = geofenceHelper.getPendingIntent();
//        pendingIntentHashMap.put(geofenceRequestId, mPendingIntent);

//        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//            Log.d(TAG, "addGeofence: geofencePermission not granted: ");
//            // TODO: Consider calling
//            //    ActivityCompat#requestPermissions
//            // here to request the missing permissions, and then overriding
//            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
//            //                                          int[] grantResults)
//            // to handle the case where the user grants the permission. See the documentation
//            // for ActivityCompat#requestPermissions for more details.
//            return;
//        }
//
//        geofencingClient.addGeofences(geofencingRequest, mPendingIntent)
//                .addOnSuccessListener(new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void aVoid) {
//                        addLogGeofenceData("DriveFragmentBase: addGeofence: onSuccess: Geofence Added...");
//                        Log.d(TAG, "onReceive: onSuccess: Geofence Added...");
//                    }
//                })
//                .addOnFailureListener(new OnFailureListener() {
//                    @Override
//                    public void onFailure(@NonNull Exception e) {
//                        addLogGeofenceData("DriveFragmentBase: addGeofence: onFailure: Failed while adding geofences... exception: " + e.getMessage());
//                        String errorMessage = geofenceHelper.getErrorString(e);
//                        Log.d(TAG, "onReceive: onFailure: " + errorMessage);
//                    }
//                });

    }

    public void removeGeofenceMonitoring() {

//        pendingIntentHashMap.get(""+geofenceRequestId);

//        geofencingClient.removeGeofences(mPendingIntent)
//                .addOnSuccessListener(getActivity(), new OnSuccessListener<Void>() {
//                    @Override
//                    public void onSuccess(Void unused) {
//                        // Geo fences removed
//                        Log.d(TAG, "compareRequestId: removeGeofences: onSuccess: unused: " + unused);
//                        addLogGeofenceData("DriveFragmentBase: removeGeofenceMonitoring: onSuccess: Geofence Removed...");
//                    }
//                }).addOnFailureListener(getActivity(), new OnFailureListener() {
//            @Override
//            public void onFailure(@NonNull Exception e) {
//                // Failed to remove geo fences
//                addLogGeofenceData("DriveFragmentBase: removeGeofenceMonitoring: onFailure: Unable to removed Geofence... exception: " + e.getMessage());
//                Log.d(TAG, "compareRequestId: removeGeofenceMonitoring: onFailure: exception: " + e.getMessage());
//            }
//        });

    }

    //  May 18, 2022    -   GEOFENCING
//    private PendingIntent getGeofencePendingIntent() {
//        if (geofencePendingIntent != null) {
//            return geofencePendingIntent;
//        }
//        Intent intent = new Intent(getActivity(), GeofenceBroadcastReceiver.class);
//
//        geofencePendingIntent = PendingIntent.getBroadcast(getActivity(), 0, intent, PendingIntent.
//                FLAG_UPDATE_CURRENT);
//        return geofencePendingIntent;
//    }

    void addLogGeofenceData(String geofenceData) {
//        MainMenuActivity.logDataArrayList.add(geofenceData + "\n" +
//                DateUtils.getNowYyyyMmDdHhmmss());
    }


    void addLogGeneralData(String logData) {
        MainMenuActivity.logDataArrayList.add("addLogGeneralData" + logData + "\n" +
                DateUtils.getNowYyyyMmDdHhmmss());
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        Log.d(TAG, "ABL: onRequestPermissionsResult: requestCode: " + requestCode);

        if (requestCode == BACKGROUND_LOCATION_ACCESS_REQUEST_CODE) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                Log.d(TAG, "ABL: onRequestPermissionsResult: ACCESS_BACKGROUND_LOCATION: permission already granted: ");
                printMaxSpeedArrayHashMap();
            } else {
//                Log.d(TAG, "ABL: onRequestPermissionsResult: Please give permission to access background ");
                Toast.makeText(mActivity, "Please access permission: ", Toast.LENGTH_SHORT).show();
            }
        }

        Log.d(TAG, "onRequestPermissionsResult: speech: requestCode: " + requestCode);
        if (requestCode == SPEECH_PERMISSION_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "onRequestPermissionsResult: speech: Speech Permission Granted");
                Toast.makeText(getContext(), "Speech Permission Granted", Toast.LENGTH_SHORT);

                enableMic();
            } else {
                Log.d(TAG, "onRequestPermissionsResult: speech: Speech Permission Denied");
                Toast.makeText(getContext(), "Speech Permission Denied", Toast.LENGTH_SHORT);
            }
        }

    }

    LatLng getFirstGeofencePointForSegment(int segment) {
        Log.d(TAG, "getFirstGeofencePointForSegment: ");
        ArrayList<GenericModel> geofenceList = maxSpeedArrayHashMap.get(segment);
        if (geofenceList == null && geofenceList.size() == 0) {
            return null;
        }

//        Log.d(TAG, "getFirstGeofencePointForSegment: geofenceList: " + geofenceList.size());
        GenericModel genericModel = geofenceList.get(0);
        return new LatLng(Double.parseDouble(genericModel.getLatitude()), Double.parseDouble(genericModel.getLongitude()));
    }

    GenericModel getGeofencePointForSegment(int segment) {
//        Log.d(TAG, "CustomGeofence: getGeofencePointForSegment: ");
        ArrayList<GenericModel> geofenceList = maxSpeedArrayHashMap.get(segment);
        if (geofenceList == null || geofenceList.size() == 0) {
            return null;
        }
//        Log.d(TAG, "CustomGeofence: getGeofencePointForSegment: geofenceList: " + geofenceList);
        return geofenceList.get(0);
    }

    boolean geofenceContainsPoint(GenericModel genericModel, LatLng currentLatLng) {
//        Log.d(TAG, "CustomGeofence: geofenceContainsPoint: ");
        if (genericModel == null || currentLatLng == null) {
            return false;
        }
        LatLng destinationLatLng = new LatLng(Double.parseDouble(genericModel.getLatitude()), Double.parseDouble(genericModel.getLongitude()));
        double straightDistance = Utils.calculateDistance(currentLatLng, destinationLatLng);
        double distance = genericModel.getStraightDistance();

//        Log.d(TAG, "CustomGeofence: geofenceContainsPoint: destinationLatLng: " + destinationLatLng + " straightDistance: "
//                + straightDistance + " distance: " + distance + " currentLatLng: " + currentLatLng);

        if (straightDistance <= distance) {
//            Log.d(TAG, "CustomGeofence: geofenceContainsPoint: if: (straightDistance <= distance): " + (straightDistance <= distance));
            return true;
        }

        return false;
    }

    void removeFirstGeofenceForSegment(int segment) {
//        Log.d(TAG, "CustomGeofence: removeFirstGeofenceForSegment: segment: " + segment);
        ArrayList<GenericModel> geofenceList = maxSpeedArrayHashMap.get(segment);
        if (geofenceList == null && geofenceList.size() == 0) {
            return;
        }
        geofenceList.remove(0);
        maxSpeedArrayHashMap.put(segment, geofenceList);

    }

    public void calculateHours() {
        Log.d(TAG, "sortAmenity: MapAutoUpdateTask: calculateHours: ");

        drivingTimeInMinutes = 0;
        breakTimeInMinutes = 0;
        shiftTimeInMinutes = 0;
        cycleTimeInMinutes = 0;
        String lastEventDate = rules.getLastShiftStartDate();
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: lastEventDate: " + lastEventDate);

        double odometer = Hawk.get(KEY_LAST_ODOMETER + "-" + rules.getLastLoggedInUsername() + "-" + lastEventDate, 0D);
        List<EldEvent> todayEventList = rules.getAllDutyEventAfterDate(lastEventDate);

//        if (todayEventList == null || todayEventList.size() == 0)
//            return;

//        June 13, 2022

        EldEvent currentEvent = null;
//        EldEvent currentEvent = todayEventList.get(todayEventList.size() - 1);

        if (todayEventList != null && todayEventList.size() > 0) {
            currentEvent = todayEventList.get(todayEventList.size() - 1);
        }


        Log.d(TAG, "MapAutoUpdateTask: calculateHours: beforeLoop: todayEventList: " + todayEventList.size());
        // Calculate driving , shift and mile from DBB
        double breakDuration = 0.0;
        for (int i = 0; i < todayEventList.size(); i++) {
            EldEvent event = todayEventList.get(i);

            if (event == null || event.EventCode == null)
                continue;

//                May 31, 2022  -   We should use just the events that are a change in driver duty status
            if (!event.EventType.equals("1")) {
                continue;
            }

            Log.d(TAG, "MapAutoUpdateTask: calculateHours: todayEventList: size: " + todayEventList.size());
            Log.d(TAG, "MapAutoUpdateTask: calculateHours: recordId: " + event.RecordId);

            if ((i + 1) < todayEventList.size()) {
                EldEvent nextEvent = todayEventList.get(i + 1);
                long timeInSecond = nextEvent.getEventSecondsValue() - event.getEventSecondsValue();
                timeInSecond = DateUtils.eliminateSecondsFromValue(timeInSecond);

                if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                    drivingTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
//                        May 30, 2022  -   We should add the driving time to the cycle hours
//                        cycleTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                    Log.d(TAG, "MapAutoUpdateTask: calculateHours: DRIVING: shiftTimeInMinutes: " + shiftTimeInMinutes);
                    Log.d(TAG, "MapAutoUpdateTask: calculateHours: DRIVING: cycleTimeInMinutes: " + cycleTimeInMinutes);
                    if (event.getOdometer() != null && !event.getOdometer().isEmpty())
//                            try {
                        if (!StringUtils.isNullOrWhitespaces(event.getOdometer())) {
                            double lastOdometer = Double.parseDouble(event.getOdometer());
                            Log.d(TAG, "MapAutoUpdateTask: calculateHours: lastOdometer: " + lastOdometer + " odometer: " + odometer);
                            if (odometer > 0 && lastOdometer > 0) {
                                miles = (long) (lastOdometer - odometer);
                            } else {
                                miles = 0;
                            }
                        }
//                            } catch (Throwable throwable) {
//
//                            }
                } else if (event.EventCode.equals(BusinessRules.EventCode.ON_DUTY_NOT_DRIVING.getValue())) {
                    Log.d(TAG, "MapAutoUpdateTask: calculateHours: ON_DUTY_NOT_DRIVING: shiftTimeInMinutes: " + shiftTimeInMinutes);
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                } else if (event.EventCode.equals(BusinessRules.EventCode.ON_BREAK_STARTED.getValue())) {

//                    June 10, 2022 -   We are interested in the breaks that lasted more than or equal 30 mints
//                    Todo =>   May be we should consider a valid break
//                        a sleeper >30 or a Off Duty>0
                    if (timeInSecond >= (30 * 60)) {
                        breakDuration = DateUtils.convertToMinutes(timeInSecond);
                    }
                }
            } else {
                Log.d(TAG, "MapAutoUpdateTask: calculateHours: todayEventList: size: is not greater than (i+1)");

                if (event.EventCode.equals(BusinessRules.EventCode.ON_DUTY_NOT_DRIVING.getValue())) {
                    long timeInSecond = DateUtils.getTimeInSecond() - event.getEventSecondsValue();
                    timeInSecond = DateUtils.eliminateSecondsFromValue(timeInSecond);

                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                } else if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                    long timeInSecond = DateUtils.getTimeInSecond() - event.getEventSecondsValue();
                    timeInSecond = DateUtils.eliminateSecondsFromValue(timeInSecond);
                    drivingTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
//                        May 30, 2022  -   We should add the driving time to the cycle hours
//                        cycleTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                } else if (event.EventCode.equals(BusinessRules.EventCode.ON_BREAK_STARTED.getValue())) {

//                    June 10, 2022 -   We are interested in the breaks that lasted more than or equal 30 mints
//                    Todo =>   May be we should consider a valid break
//                        a sleeper >30 or a Off Duty>0
                    long timeInSecond = DateUtils.getTimeInSecond() - event.getEventSecondsValue();
                    timeInSecond = DateUtils.eliminateSecondsFromValue(timeInSecond);
                    if (timeInSecond >= (30 * 60)) {
                        breakDuration = DateUtils.convertToMinutes(timeInSecond);
                    }

                }
            }
        }
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: after loop: ");
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: cycleTimeInMinutes: first: " + cycleTimeInMinutes);
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: shiftTimeInMinutes: " + shiftTimeInMinutes);
        long currentTimeInSecond = DateUtils.getTimeInSecond();

        if (currentEvent != null) {
            currentEventTimeInMinutes = (long) (currentTimeInSecond - currentEvent.getEventSeconds()) / 60;
        } else {
            currentEventTimeInMinutes = 0;
        }

        if (breakDuration >= 30) {
            if (drivingTimeInMinutes >= (3 * 60)) {
                breakTimeInMinutes = (11 * 60) - drivingTimeInMinutes;
            } else {
                breakTimeInMinutes = (8 * 60);
            }
        } else {
            breakTimeInMinutes = (8 * 60) - drivingTimeInMinutes;
        }

        // Calculate cycle time from DBB
        TruckLogHeader truckLogHeader = rules.getOpenTruckLogHeader();

        String hours = rules.getTruckLogHeaderDrivingRuleHours();
        if (truckLogHeader == null) {
            Log.d(TAG, "MapAutoUpdateTask: calculateHours: mobileRecordId: truckLogHeader: " + truckLogHeader);
        } else {
            Log.d(TAG, "MapAutoUpdateTask: calculateHours: mobileRecordId: truckLogHeader: mobileRecordId: " + truckLogHeader.MobileRecordId);
        }

        Log.d(TAG, "MapAutoUpdateTask: calculateHours: hours: " + hours);
        if (hours != null) {
            maxCycleHours = Integer.parseInt(hours);
        }
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: maxCycleHours: " + maxCycleHours);
        String cycleDate = DateUtils.getLastCycleDate(maxCycleHours, DateUtils.FORMAT_DATE_TIME_MILLIS);
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: splitCycleStartDate: " + cycleDate);

        if (truckLogHeader != null) {
//            Log.d(TAG, "calculateHours: StartDate: " + truckLogHeader.StartDate + " startTime: " + truckLogHeader.StartTime);
//                May 31, 2022  -   we should use start date and start time from truck log header
//                Here we are using it for cycle start date and time

            String[] splitCycleStartDate = truckLogHeader.StartDate.split(" ");

            if (splitCycleStartDate == null) {
                Log.d(TAG, "MapAutoUpdateTask: calculateHours: splitCycleStartDate: " + "1");
                return;
            }

            if (splitCycleStartDate.length < 1) {
                Log.d(TAG, "MapAutoUpdateTask: calculateHours: splitCycleStartDate: " + "2");
                return;
            }

            String cycleStartDate = splitCycleStartDate[0].trim();

//                String cycleStartDate = truckLogHeader.StartDate.replace(" 00:00:00.000 ", " ");
            Log.d(TAG, "MapAutoUpdateTask: calculateHours: startDate and Time: " + cycleStartDate + " " + truckLogHeader.StartTime);
            cycleDate = cycleStartDate + " " + truckLogHeader.StartTime;
            Log.d(TAG, "MapAutoUpdateTask: calculateHours: cycleDate: " + cycleDate);
        }

        Log.d(TAG, "MapAutoUpdateTask: calculateHours: cycleDate: " + cycleDate);
        cycleTimeInMinutes += getDrivingTimeInCyclePeriod(cycleDate);
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: cycleTimeInMinutes: " + cycleTimeInMinutes);


//        June 15, 2022 -   We should use breakTimeInMinutes
//        if (currentEvent != null && currentEvent.EventCode != null && currentEvent.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
//            timeBeforBreakinMinutes = BREAKS_MAX_HOURS - currentEventTimeInMinutes;
//        } else {
//            timeBeforBreakinMinutes = BREAKS_MAX_HOURS;
//        }
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: splitCycleStartDate: " + "checkIsCycleEnded");
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: splitCycleStartDate: " + "isTablet 0: " + isTablet);

        checkIsCycleEnded(3);
        Log.d(TAG, "MapAutoUpdateTask: calculateHours: splitCycleStartDate: " + "isTablet 1: " + isTablet);

        if (isTablet) {
            Log.d(TAG, "MapAutoUpdateTask: calculateHours: splitCycleStartDate: " + "isTablet");

            setGaugeText(R.id.gauges_drivinghours, (drivingTimeInMinutes));
            setGaugeText(R.id.gauges_breakhours, (breakTimeInMinutes));
            setGaugeText(R.id.gauges_shifthours, (shiftTimeInMinutes));
            setGaugeText(R.id.gauges_cyclehours, (cycleTimeInMinutes));

            setCircularGaugeText(drivingBarValue, (drivingTimeInMinutes), 1);
            setCircularGaugeText(breakBarValue, (breakTimeInMinutes), 2);
            setCircularGaugeText(shiftBarValue, (shiftTimeInMinutes), 3);
            setCircularGaugeText(cycleBarValue, (cycleTimeInMinutes), 4);


//            Aug 11, 2022  -   Fixed the problem regarding the progress bar that do not show the prohress when like less than 1
//            like in case 0.6 no progress was shown
            setProgressBarUpdate(R.id.gauges_drivinghours, drivingTimeInMinutes, (11 * 60));
            setProgressBarUpdate(R.id.gauges_breakhours, breakTimeInMinutes, (8 * 60));
            setProgressBarUpdate(R.id.gauges_shifthours, shiftTimeInMinutes, (14 * 60));
            setProgressBarUpdate(R.id.gauges_cyclehours, cycleTimeInMinutes, (maxCycleHours * 60));

//            Oct 21, 2022  -   I am adding the same values as above to circular progress bar
            updateCircularProgressBar(drivingTimeInMinutes, (11 * 60), gaugesDrivingBar, 1);
            updateCircularProgressBar(breakTimeInMinutes, (8 * 60), gaugesBreakBar, 2);
            updateCircularProgressBar(shiftTimeInMinutes, (14 * 60), gaugesShiftBar, 3);
            updateCircularProgressBar(cycleTimeInMinutes, (maxCycleHours * 60), gaugesCycleBar, 4);

        }

    }

    private int getDrivingTimeInCyclePeriod(String date) {

//        The date is not right, we should detect a date from most recent cycle

        Log.d(TAG, "MapAutoUpdateTask: calculateHours: getDrivingTimeInCyclePeriod: date: " + date);
        List<EldEvent> eventList = rules.getAllDutyEventAfterDate(date);
        int driveTime = 0;

        Log.d(TAG, "MapAutoUpdateTask: calculateHours: getDrivingTimeInCyclePeriod: eventList: " + eventList.size());
        for (int i = 0; i < eventList.size(); i++) {
            EldEvent event = eventList.get(i);

            if (event == null || event.EventCode == null)
                continue;

//                May 31, 2022  -   We should use just the events that are a change in driver duty status
//            if (!event.EventType.equals(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS)) {
            if (!event.EventType.equals("1")) {
                continue;
            }
            Log.d(TAG, "MapAutoUpdateTask: calculateHours: getDrivingTimeInCyclePeriod: index: " + i + " event.EventType: " + event.EventType
                    + " mobileRecordId: " + event.MobileRecordId);

            if (i + 1 < eventList.size()) {
                EldEvent nextEvent = eventList.get(i + 1);
                long timeInSecond = nextEvent.getEventSecondsValue() - event.getEventSecondsValue();
                Log.d(TAG, "MapAutoUpdateTask: calculateHours: getDrivingTimeInCyclePeriod: timeInSeconds: difference: " + timeInSecond);

                if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                    driveTime += DateUtils.convertToMinutes(timeInSecond);
                    Log.d(TAG, "MapAutoUpdateTask: calculateHours: getDrivingTimeInCyclePeriod: event: mobileRecordId: " + event.MobileRecordId +
                            " recordId: " + event.RecordId);
                    Log.d(TAG, "MapAutoUpdateTask: calculateHours: getDrivingTimeInCyclePeriod: eventCode:Driving: driveTime: " + driveTime);
                }
            } else if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                long timeInSecond = DateUtils.getTimeInSecond() - event.getEventSecondsValue();
                driveTime += DateUtils.convertToMinutes(timeInSecond);
                Log.d(TAG, "MapAutoUpdateTask: calculateHours: else if driving: getDrivingTimeInCyclePeriod: event: mobileRecordId: " +
                        "" + event.MobileRecordId + " recordId: " + event.RecordId);
                Log.d(TAG, "MapAutoUpdateTask: calculateHours: else if driving: getDrivingTimeInCyclePeriod: eventCode:Driving: driveTime: "
                        + driveTime);
            }
        }

        Log.d(TAG, "MapAutoUpdateTask: calculateHours: getDrivingTimeInCyclePeriod: before returning driveTime: " + driveTime);
        return driveTime;
    }

    @Override
    public void notifySyncComplete() {
        Log.d(TAG, "checkIsSleeperStarted: activityListener: notifySyncComplete: I think sync is complete: ");
        rules.resetDutyEventCode();
        checkIsBreakStarted(true);
        checkIsSleeperStarted();
        adjustCameraToUserLocation();
//        Oct 28, 2022  -   We need to reload miles info after the sync is done
        calculateDrivingMilesDelta();

//        Nov 10, 2022  -   We should call this right after we complete syn becuase previously
        mapAutoUpdateTask = null;
        initiateMapAutoUpdateTask();

        Log.d(TAG, "notifySyncComplete: initiateTilesUnzipping:");
//        Dec 30, 2022  -   Added this thread so we can decompress tiles once from assets folder into phones memory only once
        initiateTilesUnzipping();
    }

    void initiateMapAutoUpdateTask() {
        if (mapAutoUpdateTask == null) {
            mapAutoUpdateTask = new MapAutoUpdateTask();
            mapAutoUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    void initiateTilesUnzipping() {
        Log.d(TAG, "initiateTilesUnzipping: ");

        String zippedFileDirectoryPath = "/storage/emulated/0/Documents/RCOTrucks/offline_tiles.zip";
        String unzippedFileDirectoryPath = "/storage/emulated/0/Documents/RCOTrucks/weAreUnZippingZippedFiles";
        File zippedFile = new File(zippedFileDirectoryPath);
        File destinationFile = new File(unzippedFileDirectoryPath);
        Log.d(TAG, "initiateTilesUnzipping: destinationFile; isExists: " + destinationFile.exists());
        if (!destinationFile.exists()) {
            destinationFile.mkdir();
        }
        Log.d(TAG, "initiateTilesUnzipping: destinationFile; isExists: " + destinationFile.exists());
        Log.d(TAG, "initiateTilesUnzipping: zippedFile: " + zippedFile);
        if (zippedFile == null) {
            return;
        }

        unZippingCodeUpdate(unzippedFileDirectoryPath, zippedFile);

//
//        WZip wZip = new WZip();
//        wZip.unzip(zippedFile,
//                destinationFile,
//                "unZipTiles",
//                new WZipCallback() {
//                    @Override
//                    public void onStarted(String identifier) {
//                        Log.d(TAG, "initiateTilesUnzipping: onStarted: identifier: " + identifier);
//                    }
//
//                    @Override
//                    public void onZipCompleted(File zipFile, String identifier) {
//                        Log.d(TAG, "initiateTilesUnzipping: onZipCompleted: zipFile: " + zipFile.getAbsolutePath() + " identifier: " + identifier);
//                    }
//
//                    @Override
//                    public void onUnzipCompleted(String identifier) {
//                        Log.d(TAG, "initiateTilesUnzipping: onUnzipCompleted: identifier: " + identifier);
//                    }
//
//                    @Override
//                    public void onError(Throwable throwable, String identifier) {
//                        Log.d(TAG, "initiateTilesUnzipping: onError: throwable: " + throwable.getMessage() + " identifier: " + identifier);
//                    }
//                });
    }

    void unZippingCodeUpdate(String destinationDirectory, File zippedFile) {
        Log.d(TAG, "initiateTilesUnzipping: unZippingCodeUpdate: destinationDirectory: "+destinationDirectory+" zippedFile: "+zippedFile);
        byte[] buffer = new byte[1024];
//        ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFileName));
        ZipInputStream zis = null;
        try {
            zis = new ZipInputStream(new FileInputStream(zippedFile));
        } catch (FileNotFoundException e) {
            Log.d(TAG, "initiateTilesUnzipping: unZippingCodeUpdate: zis: " + zis);
        }

        ZipEntry zipEntry = null;
        try {
            zipEntry = zis.getNextEntry();
            while (zipEntry != null) {
                String filePath = destinationDirectory + File.separator + zipEntry.getName();
                Log.d(TAG, "initiateTilesUnzipping: Unzipping " + filePath);
                if (!zipEntry.isDirectory()) {
                    FileOutputStream fos = new FileOutputStream(filePath);
                    int len;
                    while ((len = zis.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    fos.close();
                } else {
                    File dir = new File(filePath);
                    dir.mkdir();
                }
                zis.closeEntry();
                zipEntry = zis.getNextEntry();
            }
            zis.closeEntry();
            zis.close();
            Log.d(TAG, "initiateTilesUnzipping: Unzipping complete");
        } catch (IOException e) {
            Log.d(TAG, "initiateTilesUnzipping: unZippingCodeUpdate: IOException: " + e);
        }

    }

//    public static void copyStream(InputStream in, OutputStream out) throws IOException {
//        byte[] buffer = new byte[1024];
//        int read;
//        while ((read = in.read(buffer)) != -1) {
//            out.write(buffer, 0, read);
//        }
//
//        File tempFile = File.createTempFile(prefix, suffix);
//        tempFile.deleteOnExit();
//        FileOutputStream out = new FileOutputStream(tempFile);
//        IOUtils.copy(in, out);
//        return tempFile;
//    }
//
//    void f(InputStream inputStream) {
//        BufferedReader r = new BufferedReader(new InputStreamReader(inputStream));
//        StringBuilder total = new StringBuilder();
//        String line;
//        while (true) {
//            try {
//                if (!((line = r.readLine()) != null)) break;
//                total.append(line).append('\n');
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//
//        try {
//            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(getContext().openFileOutput("file.txt", Context.MODE_PRIVATE));
//            outputStreamWriter.write(total.toString());
//            outputStreamWriter.close();
//        } catch (IOException e) {
//            Log.e("Exception", "File write failed: " + e.toString());
//        }
//    }
//
//    private File createFileFromInputStream(InputStream inputStream) {
//
//        try {
//            String unZipFilePath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "rcoTrucks" + File.separator + "offLineTiles";
//            Log.d(TAG, "initiateTilesUnzipping: createFileFromInputStream: unZipFilePath: " + unZipFilePath);
//
//            File f = new File(unZipFilePath);
//            OutputStream outputStream = new FileOutputStream(f);
//            byte buffer[] = new byte[1024];
//            int length = 0;
//
//            while ((length = inputStream.read(buffer)) > 0) {
//                outputStream.write(buffer, 0, length);
//            }
//
//            outputStream.close();
//            inputStream.close();
//
//            return f;
//        } catch (IOException e) {
//            //Logging exception
//            Log.d(TAG, "initiateTilesUnzipping: createFileFromInputStream: IOException: " + e);
//        }
//
//        return null;
//    }

    @Override
    public void notifyBleConnection() {
        Log.d(TAG, "activityListener: notifyBleConnection: I think ble is connected: ");
        if (isTablet) {
            updateGaugesParametersForDistance();
        } else {
            updateBottomValues(); //  For Mobile Bottom Sheet
        }

//        Dec 22, 2022 -   May this is not required
//    Dec 22, 2022  -   The D(Diagnostic) is only visible when driver logged in but I believe it was not showing
//        ((MainMenuActivity) getActivity()).updateEldActionBarFlags();
    }

    @Override
    public void updateBottomDrivingStatus() {
        Log.d(TAG, "updateBottomDrivingStatus: ");
        updateDutyStatusForBottomBar();
    }

    @Override
    public void notifyShiftChanged() {
        Log.d(TAG, "notifyShiftChanged: ");
        resetOdometerStart();
    }


    TextView gauges_milestoday, gauges_barvalue_driving_hours, gauges_barvalue_break_hours,
            gauges_barvalue_shift_hours, gauges_barvalue_cycle_hours,
            gauges_barhours_driving_hours, gauges_barhours_break_hours, gauges_barhours_shift_hours,
            gauges_barhours_cycle_hours, gauges_pilot, gauges_love, gauges_rest, gauges_ta;
    ProgressBar gauges_barprogress_driving_hours, gauges_barprogress_break_hours, gauges_barprogress_shift_hours, gauges_barprogress_cycle_hours;


    void addGaugesBottomSheet() {
        final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(getContext());
        final View view = LayoutInflater.from(getContext()).inflate(R.layout.gauges_bottomsheet, (ViewGroup) mView.getParent(), false);
        bottomSheetDialog.setContentView(view);
        bottomSheetDialog.show();

        Configuration configuration = getActivity().getResources().getConfiguration();
        if (configuration.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
            Display display = wm.getDefaultDisplay();
            DisplayMetrics metrics = new DisplayMetrics();
            display.getMetrics(metrics);
            int width = 300;
            bottomSheetDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            bottomSheetDialog.getWindow().setLayout(dpToPx(width), WindowManager.LayoutParams.MATCH_PARENT);
            bottomSheetDialog.getWindow().setGravity(RIGHT_OF);

            BottomSheetBehavior behavior = BottomSheetBehavior.from((View) view.getParent());
            behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            behavior.setPeekHeight(0);
        }

//        LinearLayout.LayoutParams  lllp=(LinearLayout.LayoutParams)MyButton.getLayoutParams();
//        lllp.gravity= Gravity.LEFT;
//        MyButton.setLayoutParams(lllp);


        setBottomSheetIds(bottomSheetDialog);
        initializeBottomSheetWidgets();
        calculateHoursForBottomSheet(bottomSheetDialog);
    }

    private static int dpToPx(int dp) {
        // https://developer.android.com/guide/practices/screens_support.html#dips-pels
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) ((dp * density) + 0.5f);
    }

    void setBottomSheetIds(BottomSheetDialog bottomSheetDialog) {
        Log.d(TAG, "setBottomSheetIds: ");

//        driving hours
        gauges_milestoday = bottomSheetDialog.findViewById(R.id.gauges_milestoday);

        gauges_barvalue_driving_hours = bottomSheetDialog.findViewById(R.id.gauges_barvalue_driving_hours);
        gauges_barvalue_break_hours = bottomSheetDialog.findViewById(R.id.gauges_barvalue_break_hours);
        gauges_barvalue_shift_hours = bottomSheetDialog.findViewById(R.id.gauges_barvalue_shift_hours);
        gauges_barvalue_cycle_hours = bottomSheetDialog.findViewById(R.id.gauges_barvalue_cycle_hours);

        gauges_barhours_driving_hours = bottomSheetDialog.findViewById(R.id.gauges_barhours_driving_hours);
        gauges_barhours_break_hours = bottomSheetDialog.findViewById(R.id.gauges_barhours_break_hours);
        gauges_barhours_shift_hours = bottomSheetDialog.findViewById(R.id.gauges_barhours_shift_hours);
        gauges_barhours_cycle_hours = bottomSheetDialog.findViewById(R.id.gauges_barhours_cycle_hours);

        gauges_barprogress_driving_hours = bottomSheetDialog.findViewById(R.id.gauges_barprogress_driving_hours);
        gauges_barprogress_break_hours = bottomSheetDialog.findViewById(R.id.gauges_barprogress_break_hours);
        gauges_barprogress_shift_hours = bottomSheetDialog.findViewById(R.id.gauges_barprogress_shift_hours);
        gauges_barprogress_cycle_hours = bottomSheetDialog.findViewById(R.id.gauges_barprogress_cycle_hours);

        gauges_pilot = bottomSheetDialog.findViewById(R.id.gauges_pilot);
        gauges_love = bottomSheetDialog.findViewById(R.id.gauges_love);
        gauges_rest = bottomSheetDialog.findViewById(R.id.gauges_rest);
        gauges_ta = bottomSheetDialog.findViewById(R.id.gauges_ta);
    }

    void initializeBottomSheetWidgets() {
        String loadingMessage = "Calculating...";
        if (!rules.isBleConnected()) {
            loadingMessage = "Waiting for ELD...";
        }
//        Log.d(TAG, "initializeBottomSheetWidgets: is BleConnected: " + rules.isBleConnected() + " loadingMessage: " + loadingMessage + " gauges_pilot: " + gauges_pilot);

        if (gauges_pilot != null) {
            Log.d(TAG, "updateGaugesParametersForDistance: initializeBottomSheetWidgets: loadingMessage: ");
            gauges_pilot.setText(loadingMessage);
            gauges_love.setText(loadingMessage);
            gauges_rest.setText(loadingMessage);
            gauges_ta.setText(loadingMessage);
        }

        if (isTablet) {

        }
    }

    public void calculateHoursForBottomSheet(BottomSheetDialog bottomSheetDialog) {
        Log.d(TAG, "calculateHoursForBottomSheet: ");
//        Log.d(TAG, "calculateHoursForBottomSheet: drivingTimeInMinutes: " + drivingTimeInMinutes);
//        Log.d(TAG, "calculateHoursForBottomSheet: breakTimeInMinutes: " + breakTimeInMinutes);
//        Log.d(TAG, "calculateHoursForBottomSheet: shiftTimeInMinutes: " + shiftTimeInMinutes);
//        Log.d(TAG, "calculateHoursForBottomSheet: cycleTimeInMinutes: " + cycleTimeInMinutes);

        drivingTimeInMinutes = 0;
        breakTimeInMinutes = 0;
        breakTimeInMinutes = 0;
        shiftTimeInMinutes = 0;
        cycleTimeInMinutes = 0;
        String lastEventDate = rules.getLastShiftStartDate();
//        Log.d(TAG, "calculateHoursForBottomSheet: lastEventDate: " + lastEventDate);

        double odometer = Hawk.get(KEY_LAST_ODOMETER + "-" + rules.getLastLoggedInUsername() + "-" + lastEventDate, 0D);
        List<EldEvent> todayEventList = rules.getAllDutyEventAfterDate(lastEventDate);

//        Log.d(TAG, "calculateHoursForBottomSheet: todayEventList: " + todayEventList);

//        if (todayEventList == null || todayEventList.size() == 0)
//            return;

//        June 13, 2022
        EldEvent currentEvent = null;
//        EldEvent currentEvent = todayEventList.get(todayEventList.size() - 1);

        if (todayEventList != null && todayEventList.size() > 0) {
            currentEvent = todayEventList.get(todayEventList.size() - 1);
        }


//        Log.d(TAG, "calculateHoursForBottomSheet: beforeLoop: todayEventList: " + todayEventList.size());
        // Calculate driving , shift and mile from DBB
        double breakDuration = 0.0;
        for (int i = 0; i < todayEventList.size(); i++) {
            EldEvent event = todayEventList.get(i);

            if (event == null || event.EventCode == null)
                continue;

//                May 31, 2022  -   We should use just the events that are a change in driver duty status
            if (!event.EventType.equals("1")) {
                continue;
            }

//            Log.d(TAG, "calculateHoursForBottomSheet: todayEventList: size: " + todayEventList.size());
//            Log.d(TAG, "calculateHoursForBottomSheet: event: MobileRecordId: " + event.MobileRecordId +
//                    " recordId: " + event.RecordId);

            if ((i + 1) < todayEventList.size()) {
                EldEvent nextEvent = todayEventList.get(i + 1);
                long timeInSecond = nextEvent.getEventSecondsValue() - event.getEventSecondsValue();
//                Log.d(TAG, "calculateHoursForBottomSheet: timeInSecond: " + timeInSecond);

                if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                    drivingTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
//                        May 30, 2022  -   We should add the driving time to the cycle hours
//                        cycleTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
//                    Log.d(TAG, "calculateHoursForBottomSheet: DRIVING: shiftTimeInMinutes: " + shiftTimeInMinutes);
//                    Log.d(TAG, "calculateHoursForBottomSheet: DRIVING: cycleTimeInMinutes: " + cycleTimeInMinutes);
                    if (event.getOdometer() != null && !event.getOdometer().isEmpty())

                        if (!StringUtils.isNullOrWhitespaces(event.getOdometer())) {
                            double lastOdometer = Double.parseDouble(event.getOdometer());
//                            Log.d(TAG, "calculateHoursForBottomSheet: lastOdometer: " + lastOdometer + " odometer: " + odometer);
                            if (odometer > 0 && lastOdometer > 0) {
                                miles = (long) (lastOdometer - odometer);
                            } else {
                                miles = 0;
                            }
                        }

                } else if (event.EventCode.equals(BusinessRules.EventCode.ON_DUTY_NOT_DRIVING.getValue())) {
//                    Log.d(TAG, "calculateHoursForBottomSheet: ON_DUTY_NOT_DRIVING: shiftTimeInMinutes: " + shiftTimeInMinutes);
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                } else if (event.EventCode.equals(BusinessRules.EventCode.ON_BREAK_STARTED.getValue())) {

//                    June 10, 2022 -   We are interested in the breaks that lasted more than or equal 30 mints
//                    Todo =>   May be we should consider a valid break
//                        a sleeper >30 or a Off Duty>0
                    if (timeInSecond >= (30 * 60)) {
                        breakDuration = DateUtils.convertToMinutes(timeInSecond);
                    }
                }

            } else {
//                Log.d(TAG, "calculateHoursForBottomSheet: todayEventList: size: is not greater than (i+1)");

                if (event.EventCode.equals(BusinessRules.EventCode.ON_DUTY_NOT_DRIVING.getValue())) {
                    long timeInSecond = DateUtils.getTimeInSecond() - event.getEventSecondsValue();
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                } else if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                    long timeInSecond = DateUtils.getTimeInSecond() - event.getEventSecondsValue();
                    drivingTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
//                        May 30, 2022  -   We should add the driving time to the cycle hours
//                        cycleTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                } else if (event.EventCode.equals(BusinessRules.EventCode.ON_BREAK_STARTED.getValue())) {

//                    June 10, 2022 -   We are interested in the breaks that lasted more than or equal 30 mints
//                    Todo =>   May be we should consider a valid break
//                        a sleeper >30 or a Off Duty>0
                    long timeInSecond = DateUtils.getTimeInSecond() - event.getEventSecondsValue();
                    if (timeInSecond >= (30 * 60)) {
                        breakDuration = DateUtils.convertToMinutes(timeInSecond);
                    }
                }
            }
        }

        long currentTimeInSecond = DateUtils.getTimeInSecond();
        if (currentEvent != null) {
            currentEventTimeInMinutes = (long) (currentTimeInSecond - currentEvent.getEventSeconds()) / 60;
        } else {
            currentEventTimeInMinutes = 0;
        }

        if (breakDuration >= 30) {
            if (drivingTimeInMinutes >= (3 * 60)) {
                breakTimeInMinutes = (11 * 60) - drivingTimeInMinutes;
            } else {
                breakTimeInMinutes = (8 * 60);
            }
        } else {
            breakTimeInMinutes = (8 * 60) - drivingTimeInMinutes;
        }

        // Calculate cycle time from DBB
        TruckLogHeader truckLogHeader = rules.getOpenTruckLogHeader();

        String hours = rules.getTruckLogHeaderDrivingRuleHours();
        if (hours != null) {
            maxCycleHours = Integer.parseInt(hours);
        }

//        Log.d(TAG, "calculateHoursForBottomSheet: StartDate: " + truckLogHeader.StartDate + " startTime: " + truckLogHeader.StartTime);
        String cycleDate = DateUtils.getLastCycleDate(maxCycleHours, DateUtils.FORMAT_DATE_TIME_MILLIS);
        if (truckLogHeader != null) {
//                May 31, 2022  -   we should use start date and start time from truck log header
//                Here we are using it for cycle start date and time

            String[] splitCycleStartDate = truckLogHeader.StartDate.split(" ");

            if (splitCycleStartDate == null)
                return;

            if (splitCycleStartDate.length < 1) {
                return;
            }

            String cycleStartDate = splitCycleStartDate[0].trim();

//                String cycleStartDate = truckLogHeader.StartDate.replace(" 00:00:00.000 ", " ");
//            Log.d(TAG, "calculateHoursForBottomSheet: startDate and Time: " + cycleStartDate + " " + truckLogHeader.StartTime);
            cycleDate = cycleStartDate + " " + truckLogHeader.StartTime;
//            Log.d(TAG, "calculateHoursForBottomSheet: cycleDate: " + cycleDate);
        }
        Log.d(TAG, "calculateHoursForBottomSheet: calculateHours: cycleDate: " + cycleDate);
        cycleTimeInMinutes += getDrivingTimeInCyclePeriod(cycleDate);

        // 2022.08.16 we should check if we need to reset the gauges if the cycle is ended
        resetGaugesForEndedCycle();

//        if (currentEvent.EventCode != null && currentEvent.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
//            timeBeforBreakinMinutes = BREAKS_MAX_HOURS - currentEventTimeInMinutes;
//        } else {
//            timeBeforBreakinMinutes = BREAKS_MAX_HOURS;
//        }
//        Log.d(TAG, "calculateHoursForBottomSheet: drivingTimeInMinutes: " + drivingTimeInMinutes);
//        Log.d(TAG, "calculateHoursForBottomSheet: breakTimeInMinutes: " + breakTimeInMinutes);
//        Log.d(TAG, "calculateHoursForBottomSheet: shiftTimeInMinutes: " + shiftTimeInMinutes);
        Log.d(TAG, "calculateHoursForBottomSheet: gauges_barvalue_driving_hours: " + gauges_barvalue_driving_hours);

        updateGauge(DateUtils.formatHourStr(drivingTimeInMinutes), gauges_barvalue_driving_hours);
        updateGauge(DateUtils.formatHourStr(breakTimeInMinutes), gauges_barvalue_break_hours);
        updateGauge(DateUtils.formatHourStr(shiftTimeInMinutes), gauges_barvalue_shift_hours);
        updateGauge(DateUtils.formatHourStr(cycleTimeInMinutes), gauges_barvalue_cycle_hours);

        setProgressBarUpdate(gauges_barprogress_driving_hours, gauges_barhours_driving_hours, drivingTimeInMinutes, 11 * 60);
        setProgressBarUpdate(gauges_barprogress_break_hours, gauges_barhours_break_hours, breakTimeInMinutes, 8 * 60);
        setProgressBarUpdate(gauges_barprogress_shift_hours, gauges_barhours_shift_hours, shiftTimeInMinutes, 14 * 60);
        setProgressBarUpdate(gauges_barprogress_cycle_hours, gauges_barhours_cycle_hours, cycleTimeInMinutes, maxCycleHours * 60);

//        Log.d(TAG, "calculateHoursForBottomSheet: nearestPilot: " + nearestPilot);
//        Log.d(TAG, "calculateHoursForBottomSheet: nearestLove: " + nearestLove);
//        Log.d(TAG, "calculateHoursForBottomSheet: nearestRest: " + nearestRest);
//        Log.d(TAG, "calculateHoursForBottomSheet: nearestTa: " + nearestTa);

        updateBottomValues();
//        gauges_pilot.setText(nearestPilot);
//        gauges_love.setText(nearestLove);
//        gauges_rest.setText(nearestRest);
//        gauges_ta.setText(nearestTa);
    }

    private void resetGaugesForEndedCycle() {
        // 2022.08.16 if the Cycle is ended we should set the gauges values to zero
        if (isCycleEnded) {
            Log.d(TAG, "resetGaugesForEndedCycle: ");
            drivingTimeInMinutes = 0;
            breakTimeInMinutes = 0;
            shiftTimeInMinutes = 0;
            cycleTimeInMinutes = 0;
            miles = 0;
        }
    }

    private void setProgressBarUpdate(ProgressBar bar, TextView textView, double progress, long maxValue) {
        Log.d(TAG, "setProgressBar: progress: " + progress + " maxValue: " + maxValue);

        if (progress > maxValue) {
            bar.getProgressDrawable().setTint(0xFFFF0000);
        }

        //2022.08.16 textView.setText(maxValue + " hrs");
        textView.setText(maxValue / 60 + " hrs");

        bar.setMax((int) maxValue);
        bar.setProgress((int) progress);
    }


    //    Oct 28, 2022  -
    void calculateDrivingMilesDelta() {
        HashMap<String, String> drivingInfo = rules.calculateDrivingMilesDeltaInfo();
        Log.d(TAG, "calculateDrivingMilesDelta: drivingInfo: " + drivingInfo);
        if (drivingInfo != null) {
            double delta = Double.parseDouble(drivingInfo.get("delta"));
            double startOdometer = Double.parseDouble(drivingInfo.get("startOdometer"));
            String lastVin = drivingInfo.get("lastVin");

//            lastVin = "1FUJGHDV0CLBP8834";
            deltaShiftMiles = delta;
            if (deltaShiftMiles > 0) {
                String currentTruckVin = rules.getBleParameter("VIN");
//                currentTruckVin = lastVin;
//                Log.d(TAG, "calculateDrivingMilesDelta: if condition: "+(startOdometer > 0 && currentTruckVin != null && lastVin != null && lastVin.equals(currentTruckVin)));
                if (startOdometer > 0 && currentTruckVin != null && lastVin != null && lastVin.equals(currentTruckVin)) {
                    odometerStartForCurrentTruck = startOdometer;
                }
            }
        }
    }

    // 2022.08.17 we need to split the content of the doInBackground
    void updateStatusAndGauges() {
        Log.d(TAG, "MapAutoUpdateTask: updateStatusAndGauges");

        if (isTablet || (gauges_barvalue_driving_hours != null)) {
//                    June 10, 2022 -   We need to decrease the break time only if we are in driving
//                    Log.d(TAG, "MapAutoUpdateTask: doInBackground: drivingStatus: " + drivingStatus);
            if (drivingStatus.equalsIgnoreCase("Driving")) {
//                        Log.d(TAG, "MapAutoUpdateTask: doInBackground: increase ");
                drivingTimeInMinutes = drivingTimeInMinutes + 1;
                cycleTimeInMinutes = cycleTimeInMinutes + 1;
                breakTimeInMinutes = breakTimeInMinutes - 1;
            }

//                    Log.d(TAG, "MapAutoUpdateTask: doInBackground: breakTimeInMinutes: " + breakTimeInMinutes);
//                    June 13, 2022 -   We should increment shift only if we are driving or if we are onDuty
            if (drivingStatus.equalsIgnoreCase("Driving") ||
                    drivingStatus.equalsIgnoreCase("On Duty")) {
//                        Log.d(TAG, "MapAutoUpdateTask: doInBackground: increase shift time by one ");
                shiftTimeInMinutes = shiftTimeInMinutes + 1;
            }

//                    Log.d(TAG, "MapAutoUpdateTask: doInBackground: isTablet: " + isTablet);
            if (isTablet) {
                double drivingTimeInHours = (drivingTimeInMinutes / 60.0);
//                        Log.d(TAG, "MapAutoUpdateTask: doInBackground: drivingTimeInHours: " + drivingTimeInHours);
                double drivingHours = Double.parseDouble(new DecimalFormat("##.#").format(drivingTimeInHours));
//                        Log.d(TAG, "MapAutoUpdateTask: doInBackground: drivingHours: " + drivingHours);

//                        Log.d(TAG, "doInBackground: check: miles: " + miles);

//                        Aug 03, 2022  -   We need to get the odometer when the shift started and we will make the difference using the current odometer
                miles = 0;
                double currentOdometerDouble = 0;
                String currentOdometerString = rules.getOdometer();
//                        Log.d(TAG, "doInBackground: check: currentOdometerString: " + currentOdometerString);
                if (currentOdometerString != null && !currentOdometerString.isEmpty()) {
                    currentOdometerDouble = Double.parseDouble(currentOdometerString);
                } else {
//                            Aug 09, 2022    -   If the ELD is not connected, we should load the most recent odometer that we saved
                    currentOdometerDouble = rules.getMostRecentOdometerFromDB();
//                            Log.d(TAG, "MapAutoUpdateTask: doInBackground: from getMostRecentOdometerFromDB currentOdometerDouble: " + currentOdometerDouble);

                }
//                        Log.d(TAG, "MapAutoUpdateTask: doInBackground: currentOdometerDouble: " + currentOdometerDouble);

//                        currentOdometerDouble = 400;

//                Oct 28, 2022  -
                if (deltaShiftMiles == -1) {
                    calculateDrivingMilesDelta();
                }

//                currentOdometerDouble = 112253 + 50;
                if (odometerStartForCurrentTruck == -1) {
                    Log.d(TAG, "updateStatusAndGauges: before: odometerStartForCurrentTruck: " + odometerStartForCurrentTruck);
                    odometerStartForCurrentTruck = rules.getOdometerSinceShiftStartedFromDB();
                    Log.d(TAG, "updateStatusAndGauges: after: odometerStartForCurrentTruck: " + odometerStartForCurrentTruck);
                }

                Log.d(TAG, "updateStatusAndGauges: odometerStartForCurrentTruck: " + odometerStartForCurrentTruck);
                if (currentOdometerDouble > 0) {
                    if (odometerStartForCurrentTruck > 0) {
                        miles = currentOdometerDouble - odometerStartForCurrentTruck;
                        Log.d(TAG, "updateStatusAndGauges: miles: " + miles);
                    }
                } else {
//                    Oct 27, 2022  -   We need to get the most recent odometer value
                    currentOdometerDouble = rules.getOdometerLastValueFromDBForTruck(null);
                    miles = currentOdometerDouble - odometerStartForCurrentTruck;
                }

                Log.d(TAG, "updateStatusAndGauges: miles: " + miles + " deltaShiftMiles: " + deltaShiftMiles);
//                Oct 28, 2022  -   We need to add the miles driven from other truck
                miles += deltaShiftMiles;

//                        Aug 04, 2022  -   Don't convert miles value from km to miles because they are already in miles
//                        setMilesTodayText(Utils.convertKmsToMiles(miles));

                // 2022.08.17 reset the values if is needed
                resetGaugesForEndedCycle();

                setMilesTodayText(miles);

                Log.d(TAG, "MapAutoUpdateTask: doInBackground: updateBottomSheetGaugesValues: one minute - 1 isCycleEnded: " + isCycleEnded + ": " + drivingTimeInMinutes + ": " + shiftTimeInMinutes + ": " + cycleTimeInMinutes);

                setGaugeText(R.id.gauges_drivinghours, drivingHours);
                setGaugeText(R.id.gauges_breakhours, breakTimeInMinutes);
                setGaugeText(R.id.gauges_shifthours, shiftTimeInMinutes);
                setGaugeText(R.id.gauges_cyclehours, cycleTimeInMinutes);

                setCircularGaugeText(drivingBarValue, drivingHours);
                setCircularGaugeText(breakBarValue, (breakTimeInMinutes), 2);
                setCircularGaugeText(shiftBarValue, (shiftTimeInMinutes), 3);
                setCircularGaugeText(cycleBarValue, (cycleTimeInMinutes), 4);


                if (mActivity != null && !mActivity.isDestroyed()) {
                    mActivity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
//                        Aug 11, 2022  -   Fixed the problem regarding the progress bar that do not show the prohress when like less than 1
//                        like in case 0.6 no progress was shown
                            setProgressBarUpdate(R.id.gauges_drivinghours, drivingTimeInMinutes, (11 * 60));
                            setProgressBarUpdate(R.id.gauges_breakhours, breakTimeInMinutes, (8 * 60));
                            setProgressBarUpdate(R.id.gauges_shifthours, shiftTimeInMinutes, (14 * 60));
                            setProgressBarUpdate(R.id.gauges_cyclehours, cycleTimeInMinutes, (maxCycleHours * 60));

//            Oct 21, 2022  -   I am adding the same values as above to circular progress bar
                            updateCircularProgressBar(drivingTimeInMinutes, (11 * 60), gaugesDrivingBar, 1);
                            updateCircularProgressBar(breakTimeInMinutes, (8 * 60), gaugesBreakBar, 2);
                            updateCircularProgressBar(shiftTimeInMinutes, (14 * 60), gaugesShiftBar, 3);
                            updateCircularProgressBar(cycleTimeInMinutes, (maxCycleHours * 60), gaugesCycleBar, 4);
                        }
                    });
                }


            } else {
                Log.d(TAG, "MapAutoUpdateTask: doInBackground: updateBottomSheetGaugesValues: one minute - 2");
                updateBottomSheetGaugesValues();
            }
        }
    }

    void updateBottomSheetGaugesValues() {
        Log.d(TAG, "updateBottomSheetGaugesValues: drivingTimeInMinutes: " + drivingTimeInMinutes);
//        Log.d(TAG, "updateBottomSheetGaugesValues: breakTimeInMinutes: " + breakTimeInMinutes);
//        Log.d(TAG, "updateBottomSheetGaugesValues: shiftTimeInMinutes: " + shiftTimeInMinutes);
//        Log.d(TAG, "updateBottomSheetGaugesValues: cycleTimeInMinutes: " + cycleTimeInMinutes);

        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "updateBottomSheetGaugesValues: run: gauges_barvalue_driving_hours: " + gauges_barvalue_driving_hours);
                    if (gauges_barvalue_driving_hours != null) {
                        // 2022.08.16 we should check if we need to reset the gauges if the cycle is ended
                        resetGaugesForEndedCycle();

                        updateGauge(DateUtils.formatHourStr(drivingTimeInMinutes), gauges_barvalue_driving_hours);
                        updateGauge(DateUtils.formatHourStr(breakTimeInMinutes), gauges_barvalue_break_hours);
                        updateGauge(DateUtils.formatHourStr(shiftTimeInMinutes), gauges_barvalue_shift_hours);
                        updateGauge(DateUtils.formatHourStr(cycleTimeInMinutes), gauges_barvalue_cycle_hours);

                        setProgressBarUpdate(gauges_barprogress_driving_hours, gauges_barhours_driving_hours, drivingTimeInMinutes, 11 * 60);
                        setProgressBarUpdate(gauges_barprogress_break_hours, gauges_barhours_break_hours, breakTimeInMinutes, 8 * 60);
                        setProgressBarUpdate(gauges_barprogress_shift_hours, gauges_barhours_shift_hours, shiftTimeInMinutes, 14 * 60);
                        setProgressBarUpdate(gauges_barprogress_cycle_hours, gauges_barhours_cycle_hours, cycleTimeInMinutes, maxCycleHours * 60);
                    }
                }
            });
        }
    }

    void setIcon(ImageView imageView, int icon) {
        Log.d(TAG, "sortAmenity: setIcon: icon: " + icon);
//        Nov 21, 2022  -   Crash was occurring when imageview becomes null
        Log.d(TAG, "setIcon: imageView: " + imageView);
        if (imageView == null) {
            return;
        }
        Log.d(TAG, "setIcon: imageView is not null");

        Picasso.with(mActivity).load(icon).fit().into(imageView);
    }

    void updateTabletDistance(int distance, int assetType) {
        Log.d(TAG, "MapAutoUpdateTask: updateGaugesParametersForDistance: updateDistance: calculateDistanceToAsset: distance: " + distance + " assetType: " + assetType + " isTablet: " + isTablet);

//        int iconId = getResources().getIdentifier("food", "drawable", getContext().getPackageName());
        setIcon(mView.findViewById(R.id.first_icon_for_love), R.drawable.food);
        setIcon(mView.findViewById(R.id.second_icon_for_love), R.drawable.cat_scales);
        setIcon(mView.findViewById(R.id.third_icon_for_love), R.drawable.private_showers);

        setIcon(mView.findViewById(R.id.first_icon_for_pilot), R.drawable.food);
        setIcon(mView.findViewById(R.id.second_icon_for_pilot), R.drawable.cat_scales);
//        setIcon(mView.findViewById(R.id.third_icon_for_pilot),R.drawable.private_showers);

        setIcon(mView.findViewById(R.id.first_icon_for_ta), R.drawable.food);
        setIcon(mView.findViewById(R.id.second_icon_for_ta), R.drawable.cat_scales);
        setIcon(mView.findViewById(R.id.third_icon_for_ta), R.drawable.private_showers);

        setIcon(mView.findViewById(R.id.first_icon_for_rest_area), R.drawable.restrooms);
        setIcon(mView.findViewById(R.id.second_icon_for_rest_area), R.drawable.water);
        setIcon(mView.findViewById(R.id.third_icon_for_rest_area), R.drawable.food_vending);

        String distanceInStringMiles = distance + " mi";
        if (assetType == 1) {
            nearestPilot = "" + distanceInStringMiles;
//            Log.d(TAG, "updateDistance: updateTabletDistance: nearestPilot: " + distanceInStringMiles);
            ((TextView) gaugesContainer.findViewById(R.id.gauges_pilot)).setText(distanceInStringMiles);
            Log.d(TAG, "MapAutoUpdateTask: updateTabletDistance: pilot: gaugesPilotStationDistance: " + gaugesPilotStationDistance + " distanceInStringMiles: " + distanceInStringMiles);
            if (isTablet) {
//                gaugesPilotStationDistance.setText(distanceInStringMiles);
//                applyDistance(gaugesPilotStationDistance, distanceInStringMiles);
                gaugesLastPilotStationDistance = distanceInStringMiles;
                ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).setText(distanceInStringMiles);
                Log.d(TAG, "updateDistanceValuesOffline: updateTabletDistance: run: get: text: " + ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).getText());
            }
        } else if (assetType == 2) {
            nearestLove = "" + distanceInStringMiles;
//            Log.d(TAG, "updateDistance: updateTabletDistance: nearestLove: " + distanceInStringMiles);
            ((TextView) gaugesContainer.findViewById(R.id.gauges_love)).setText(distanceInStringMiles);
            Log.d(TAG, "updateTabletDistance: pilot: gaugesLoveStationDistance: " + gaugesLoveStationDistance + " distanceInStringMiles: " + distanceInStringMiles);
            if (isTablet) {
                Log.d(TAG, "updateTabletDistance: setting love before: distanceInStringMiles: " + distanceInStringMiles + " gaugesLoveStationDistance.getText()" + gaugesLoveStationDistance.getText());
//                gaugesLoveStationDistance.setText(distanceInStringMiles);
//                applyDistance(gaugesLoveStationDistance, distanceInStringMiles);
                gaugesLastLoveStationDistance = distanceInStringMiles;
                ((TextView) mView.findViewById(R.id.gauges_love_station_distance)).setText(distanceInStringMiles);
                Log.d(TAG, "updateTabletDistance: setting love after " + gaugesLoveStationDistance.getText());
            }
        } else if (assetType == 3) {
            nearestRest = "" + distanceInStringMiles;
//            Log.d(TAG, "updateDistance: updateTabletDistance: nearestRest: " + distanceInStringMiles);
            ((TextView) gaugesContainer.findViewById(R.id.gauges_rest)).setText(distanceInStringMiles);
            Log.d(TAG, "MapAutoUpdateTask: gauges_rest_area_station_distance: rest area: distanceInStringMiles: " + distanceInStringMiles);
            if (isTablet) {
//                gaugesRestAreaStationDistance.setText(distanceInStringMiles);
//                applyDistance(gaugesRestAreaStationDistance, distanceInStringMiles);
                gaugesLastRestAreaStationDistance = distanceInStringMiles;
                ((TextView) mView.findViewById(R.id.gauges_rest_area_station_distance)).setText(distanceInStringMiles);
            }
        } else if (assetType == 4) {
            nearestTa = "" + distanceInStringMiles;
//            Log.d(TAG, "updateDistance: updateTabletDistance: nearestTa: " + distanceInStringMiles);
            ((TextView) gaugesContainer.findViewById(R.id.gauges_ta)).setText(distanceInStringMiles);
            Log.d(TAG, "MapAutoUpdateTask: gauges_ta_station_distance: pilot: distanceInStringMiles: " + distanceInStringMiles);
            if (isTablet) {
//                gaugesTAStationDistance.setText(distanceInStringMiles);
//                applyDistance(gaugesTAStationDistance, distanceInStringMiles);
                gaugesLastTAStationDistance = distanceInStringMiles;
                ((TextView) mView.findViewById(R.id.gauges_ta_station_distance)).setText(distanceInStringMiles);
            }
        }
    }

    void applyDistance(TextView textView, String distance) {
        Log.d(TAG, "IC: removeTileOverlay: ");

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                textView.setText(distance);
            }
        });

    }


    void updatePhoneDistance(int distance, int assetType) {
//        Log.d(TAG, "updatePhoneDistance: updateDistance: distance: " + distance + " assetType: " + assetType + " gauges_pilot: " + gauges_pilot);

        String distanceInStringMiles = distance + " mi";
        if (assetType == 1) {
            nearestPilot = "" + distanceInStringMiles;
            if (gauges_pilot != null) {
                Log.d(TAG, "updateGaugesParametersForDistance: updatePhoneDistance: distanceInStringMiles: " + distanceInStringMiles);
                gauges_pilot.setText(distanceInStringMiles);
//                gaugesLoveStationDistance.setText(distanceInStringMiles);
            }
        } else if (assetType == 2) {
            nearestLove = "" + distanceInStringMiles;
            if (gauges_love != null) {
                gauges_love.setText(distanceInStringMiles);
            }
        } else if (assetType == 3) {
            nearestRest = "" + distanceInStringMiles;
            if (gauges_rest != null) {
                gauges_rest.setText(distanceInStringMiles);
            }
        } else if (assetType == 4) {
            nearestTa = "" + distanceInStringMiles;
            if (gauges_ta != null) {
                gauges_ta.setText(distanceInStringMiles);
            }
        }

    }


    void updateBottomValues() {
//        Log.d(TAG, "updateBottomValues: nearestPilot: " + nearestPilot);
//        Log.d(TAG, "updateBottomValues: nearestLove: " + nearestLove);
//        Log.d(TAG, "updateBottomValues: nearestRest: " + nearestRest);
//        Log.d(TAG, "updateBottomValues: nearestTa: " + nearestTa);
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String loadingMessage = "Calculating...";
                    if (!rules.isBleConnected()) {
                        loadingMessage = "Waiting for ELD...";
                    }
//                    Log.d(TAG, "updateBottomValues: is BleConnected: " + rules.isBleConnected() + " loadingMessage: " + loadingMessage + " gauges_pilot: " + gauges_pilot);

                    if (gauges_pilot != null) {
                        if (nearestPilot == null) {
                            Log.d(TAG, "updateGaugesParametersForDistance: run: updateBottomValues: loadingMessage: ");
                            gauges_pilot.setText(loadingMessage);
                        } else {
                            Log.d(TAG, "updateGaugesParametersForDistance: run: updateBottomValues: nearestPilot: " + nearestPilot);
                            gauges_pilot.setText(nearestPilot);
                        }
                        if (nearestLove == null) {
                            gauges_love.setText(loadingMessage);
                        } else {
                            gauges_love.setText(nearestLove);
                        }
                        if (nearestRest == null) {
                            gauges_rest.setText(loadingMessage);
                        } else {
                            gauges_rest.setText(nearestRest);
                        }
                        if (nearestTa == null) {
                            gauges_ta.setText(loadingMessage);
                        } else {
                            gauges_ta.setText(nearestTa);
                        }
                    }
                }
            });
        }
    }


    void startCycleEndedTimer(Double endedCycleTime) {
//        Log.d(TAG, "endCycle: startCycleEndedTimer: timer: startSleeperTimer: ");

        this.endedCycleTime = endedCycleTime;
        endedCycleTimerHandler.postDelayed(endedCycleTimer, 0);
    }

    Runnable endedCycleTimer = new Runnable() {
        @Override
        public void run() {
//            Log.d(TAG, "endCycle: run: endedCycleTimer: run: ");
            Double millis = System.currentTimeMillis() - (endedCycleTime);
//            Log.d(TAG, "endCycle: run: endedCycleTimer: sleepTime: " + endedCycleTime + " millis: " + millis);
            int secondsPassed = (int) (millis / 1000);
            int seconds = (thirtyFourHoursInSeconds - secondsPassed);
            int minutes = seconds / 60;
            int hours = minutes / 60;

//            Log.d(TAG, "endCycle: run: endedCycleTimer: minutes: " + (seconds / 60) + " divide: by: " + (seconds / 3600));
//            Log.d(TAG, "endCycle: run: endedCycleTimer: hours: " + hours + " minutes: " + minutes + " seconds: " + seconds);

//            May 16, 2022  -   We should use % for mints as well
            minutes %= 60;
            seconds %= 60;

            String formattedTime = String.format("%02d:%02d:%02d", hours, minutes, seconds);
            cycleCounterTime.setText(formattedTime);

//            Aug 01, 2022  -
            if (hours == 0 && minutes == 0 && seconds == 0) {
                closeEndedCycleTimer();
                cycleCounterLayout.setVisibility(View.GONE);
                // 2022.08.16 force a reset after the cycle ended
                checkIsCycleEnded(5);
                // 2022.08.16 recalculate the hours
                calculateHours();
                // 2022.08.17 reload the gauges
                updateBottomSheetGaugesValues();
                // 2022.08.17 forcely reload the gauges
                updateStatusAndGauges();
            }

            endedCycleTimerHandler.postDelayed(this, 500);
        }
    };

    void closeEndedCycleTimer() {
//        Log.d(TAG, "endCycle: closeEndedCycleTimer: closeTimer: ");
        if (endedCycleTimerHandler != null) {
            endedCycleTimerHandler.removeCallbacks(endedCycleTimer);
        }
    }

    void moveCameraToMyPosition() {
        Log.d(TAG, "onMapReady: moveCameraToMyPosition: ");
        Location phoneLastBestLocation = rules.getPhoneLastBestLocation(getActivity());
        Log.d(TAG, "onMapReady: moveCameraToMyPosition: phoneLastBestLocation: " + phoneLastBestLocation);
        Log.d(TAG, "onMapReady: moveCameraToMyPosition: lastNotNullLocation: " + lastNotNullLocation);
        Log.d(TAG, "onMapReady: moveCameraToMyPosition: NAVIGATION_ZOOM_LEVEL: " + NAVIGATION_ZOOM_LEVEL);
        if (phoneLastBestLocation != null) {
            CameraPosition.Builder cameraPosition = new CameraPosition.Builder();
            cameraPosition.target(new LatLng(phoneLastBestLocation.getLatitude(), phoneLastBestLocation.getLongitude()))
                    .zoom(NAVIGATION_ZOOM_LEVEL)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition.build()));
        }
    }

    //    Aug 04, 2022  -   We need to reload the odometerStartForCurrentTruck after we record a duty event to detect a new shift started
    void resetOdometerStart() {
        odometerStartForCurrentTruck = -1;
    }


    private void getMapAssetsCategoryAndSubCategories() {

//    	"ImageName": "Map_Fuel",
//                "POIImageName": "POI_Map_Fuel",
//                "Value": "fuel",
//                "Name": "Truck Wash"
//    	"Subcategories": []

        mapAssetList.clear();
        headerDataList.clear();
        int subcategoryCounter = 0;
        try {
            if (getContext() == null) {
                return;
            }

            JSONArray jsonArray = new JSONArray(BusinessRules.getJsonFromAssets(getContext(), "map_assets/map_assets.json"));
            for (int i = 0; i < jsonArray.length(); i++) {
                String name = "", value = "", imageName = "", poiImageName = "";
                ArrayList<MapAssetSubcategoriesModel> subcategoriesList = new ArrayList<>();
                JSONObject jsonObject = jsonArray.getJSONObject(i);

                if (jsonObject.has("Value")) {
                    value = jsonObject.getString("Value");
                }

                if (jsonObject.has("Name")) {
                    name = jsonObject.getString("Name");
                }
                int categoryMapAssetId = 0;
                if (jsonObject.has("ImageName")) {
                    imageName = jsonObject.getString("ImageName");
                    if (imageName.isEmpty()) {
                        categoryMapAssetId = (R.drawable.ic_baseline_image_not_supported_24);
                    } else {
                        categoryMapAssetId = getResources().getIdentifier(imageName.toLowerCase(), "drawable", getContext().getPackageName());
                    }
                }

                if (jsonObject.has("POIImageName")) {
                    poiImageName = jsonObject.getString("POIImageName");
                }

                JSONArray subcategoriesJSONArray = jsonObject.getJSONArray("Subcategories");
                MapAssetSubcategoriesModel mapAssetSubcategoriesModel;
                for (int x = 0; x < subcategoriesJSONArray.length(); x++) {
                    mapAssetSubcategoriesModel = new MapAssetSubcategoriesModel();
                    mapAssetSubcategoriesModel.setId(subcategoryCounter++);
                    mapAssetSubcategoriesModel.setSelected(false);

                    JSONObject subcategoriesJSONObject = subcategoriesJSONArray.getJSONObject(x);
                    if (jsonObject.has("Name")) {
                        mapAssetSubcategoriesModel.setSubCategoryName(subcategoriesJSONObject.getString("Name"));
                    }

                    if (jsonObject.has("ImageName")) {
                        String subCategoryImageName = subcategoriesJSONObject.getString("ImageName");
                        mapAssetSubcategoriesModel.setSubCategoryImageName(subCategoryImageName);

                        int subCategoryMapAssetId = 0;
                        if (subCategoryImageName.isEmpty()) {
                            subCategoryMapAssetId = (R.drawable.ic_baseline_image_not_supported_24);
                        } else {
                            subCategoryMapAssetId = getResources().getIdentifier(subCategoryImageName.toLowerCase(), "drawable", getContext().getPackageName());
                        }
                        mapAssetSubcategoriesModel.setMapAssetId(subCategoryMapAssetId);
                    }

                    Log.d(TAG, "MapAssetExpandableAdapter: getMapAssetsCategoryAndSubCategories: id: " + mapAssetSubcategoriesModel.getId() + " name: " + mapAssetSubcategoriesModel.getSubCategoryName());
                    if (jsonObject.has("POIImageName")) {
                        mapAssetSubcategoriesModel.setSubCategoryPOIImageName(subcategoriesJSONObject.getString("POIImageName"));
                        int mapAssetId = getResources().getIdentifier(mapAssetSubcategoriesModel.getSubCategoryImageName().toLowerCase(), "drawable", getContext().getPackageName());
                        mapAssetSubcategoriesModel.setMapAssetId(mapAssetId);
                    }
                    subcategoriesList.add(mapAssetSubcategoriesModel);
                }
                subcategoriesList = sortSubMapAssetModelByASC(subcategoriesList);


                mapAssetModel = new MapAssetModel();
                mapAssetModel.setName(name);
                mapAssetModel.setValue(value);
                mapAssetModel.setImageName(imageName);
                mapAssetModel.setMapAssetId(categoryMapAssetId);
                mapAssetModel.setPoiImageName(poiImageName);
                mapAssetModel.setSubcategory(subcategoriesList);

                mapAssetList.add(mapAssetModel);
                headerDataList.add(mapAssetModel);
                childDataList.put(name, subcategoriesList);
//                childDataList.put(value, subcategoriesList);
                Log.d(TAG, "MapAssetExpandableAdapter: getMapAssetsCategoryAndSubCategories: name: " + name + " subcategoriesList: size: " + subcategoriesList.size());
            }
            sortMapAssetModelByASC();

        } catch (JSONException jsonException) {
            Log.d(TAG, "mapAssets: onClick: jsonException: " + jsonException);
        }
    }

    void sortMapAssetModelByASC() {
        Collections.sort(headerDataList, new Comparator<MapAssetModel>() {
            @Override
            public int compare(MapAssetModel o1, MapAssetModel o2) {
                return o1.getName().compareToIgnoreCase(o2.getName());
            }
        });
    }

    ArrayList<MapAssetSubcategoriesModel> sortSubMapAssetModelByASC(ArrayList<MapAssetSubcategoriesModel> subcategoriesList) {
        Collections.sort(subcategoriesList, new Comparator<MapAssetSubcategoriesModel>() {
            @Override
            public int compare(MapAssetSubcategoriesModel o1, MapAssetSubcategoriesModel o2) {
                return o1.getSubCategoryName().compareToIgnoreCase(o2.getSubCategoryName());
            }
        });
        return subcategoriesList;
    }


    void checkAllMapAssets(boolean selectAll) {

        for (int i = 0; i < headerDataList.size(); i++) {
            MapAssetModel mapAssetModel = headerDataList.get(i);
            if (selectAll) {
                mapAssetModel.setSelectAllSelected(true);
                mapAssetModel.setDeselectAllSelected(false);
                mapAssetModel.setAssetSelected(true);
                checkAllSubMapAssets(mapAssetModel, true);
            } else {
                mapAssetModel.setSelectAllSelected(false);
                mapAssetModel.setDeselectAllSelected(true);
                mapAssetModel.setAssetSelected(false);
                checkAllSubMapAssets(mapAssetModel, false);
            }
            mapAssetModel.setSwitchClicked(false);
        }

        mapAssetExpandableAdapter.notifyDataSetChanged();
    }


    void checkAllSubMapAssets(MapAssetModel mapAssetModel, boolean value) {
        ArrayList<MapAssetSubcategoriesModel> subChildList = childDataList.get(mapAssetModel.getName());
//        ArrayList<MapAssetSubcategoriesModel> subChildList = childDataList.get(mapAssetModel.getValue());
        for (int x = 0; x < subChildList.size(); x++) {
            subChildList.get(x).setSelected(value);
        }
    }


    void setExpandableAdapterToMapAssetListView() {
        //set adapter to list view

        Log.d(TAG, "setExpandableAdapterToMapAssetListView: " + headerDataList.size());
        mapAssetExpandableAdapter = new MapAssetExpandableAdapter(getContext(), headerDataList, childDataList);
        expandableListView.setAdapter(mapAssetExpandableAdapter);

//        mapAssetExpandableAdapter = new MapAssetsExpandableAdapter(getContext(), headerDataList, childDataList);
//        expandableListView.setAdapter(mapAssetExpandableAdapter);
    }


    void getSelectedAssets() {
        Log.d(TAG, "saveMapAsset: getSelectedAssets: headerDataList: size: " + headerDataList.size());
        for (int i = 0; i < headerDataList.size(); i++) {
            MapAssetModel mapAssetModel = headerDataList.get(i);
            if (mapAssetModel.isAssetSelected()) {
                Log.d(TAG, "saveMapAsset: getSelectedAssets: index: " + i);
                Log.d(TAG, "saveMapAsset: getSelectedAssets: name: " + mapAssetModel.getName());

                ArrayList<MapAssetSubcategoriesModel> subChildList = childDataList.get(mapAssetModel.getName());
//                ArrayList<MapAssetSubcategoriesModel> subChildList = childDataList.get(mapAssetModel.getValue());
                Log.d(TAG, "saveMapAsset: getSelectedAssets: subChildList: size: " + subChildList.size());

//                Sep 28, 2022  -   I was not including the map assets which don't have sub categories so added this approach
                if (subChildList.size() > 0) {
                    for (int x = 0; x < subChildList.size(); x++) {
//                        Log.d(TAG, "getSelectedAssets: sub: index: " + x);
                        if (subChildList.get(x).isSelected()) {
                            Log.d(TAG, "saveMapAsset: getSelectedAssets: sub: poiImageName: " + subChildList.get(x).getSubCategoryPOIImageName());
                            MapAssetSubcategoriesModel mapAssetSubcategoriesModel = subChildList.get(x);
                            int mapAssetId = getResources().getIdentifier(subChildList.get(x).getSubCategoryPOIImageName().toLowerCase(), "drawable", getContext().getPackageName());
                            mapAssetSubcategoriesModel.setMarkerId(mapAssetId);
//                            mapAssetSubcategoriesModel.setParentType(mapAssetModel.getName());
                            mapAssetSubcategoriesModel.setParentType(mapAssetModel.getValue());

                            mapAssetContentMarkerList.add(mapAssetSubcategoriesModel);
                        }
                    }
                } else {

                    MapAssetSubcategoriesModel mapAssetSubcategoriesModel = new MapAssetSubcategoriesModel();
                    mapAssetSubcategoriesModel.setSubCategoryName(mapAssetModel.getName());
                    mapAssetSubcategoriesModel.setSubCategoryImageName(mapAssetModel.getImageName());
                    mapAssetSubcategoriesModel.setSubCategoryPOIImageName(mapAssetModel.getPoiImageName());

                    int mapAssetId = getResources().getIdentifier(mapAssetModel.getPoiImageName().toLowerCase(), "drawable", getContext().getPackageName());
                    mapAssetSubcategoriesModel.setMarkerId(mapAssetId);
//                    mapAssetSubcategoriesModel.setParentType(mapAssetModel.getName());
                    mapAssetSubcategoriesModel.setParentType(mapAssetModel.getValue());

                    mapAssetContentMarkerList.add(mapAssetSubcategoriesModel);
                }

            }
        }

        Log.d(TAG, "getSelectedAssets: mapAssetContentMarkerList: size: " + mapAssetContentMarkerList.size());
    }

    private List<MapAssetContentModel> getInfoForSelectedAssets() {
        Log.d(TAG, "saveMapAsset: getInfoForSelectedAssets: ");
        Log.d(TAG, "getInfoForSelectedAssets: saveMapAsset: getMapAssetsFilteredContent: mapAssetContentMarkerList: size: " + mapAssetContentMarkerList.size());

        for (int j = 0; j < mapAssetContentMarkerList.size(); j++) {

            try {
                JSONArray jsonArray = new JSONArray(BusinessRules.getJsonFromAssets(getContext(), "map_assets/map_assets_content.json"));
                Log.d(TAG, "saveMapAsset: getInfoForSelectedAssets: jsonArray: size: " + jsonArray.length());
                for (int i = 0; i < jsonArray.length(); i++) {

                    JSONObject jsonObject = jsonArray.getJSONObject(i);
//                    Log.d(TAG, "getInfoForSelectedAssets: name: " + mapAssetContentMarkerList.get(j).getSubCategoryName() + " parentType: " + mapAssetContentMarkerList.get(j).getParentType());
//                    Log.d(TAG, "saveMapAsset: getInfoForSelectedAssets: category: " + jsonObject.getString("Category") + " condition: " + jsonObject.getString("Category").equalsIgnoreCase(mapAssetContentMarkerList.get(j).getParentType()));
//            Nov 03, 2022  -   Roy wanted to see all the map assets
//            (that's why removing filter to show map assets which are within CA)
                    if (
//                            jsonObject.has("State") &&
//                            jsonObject.getString("State").equalsIgnoreCase("CA") &&
                            jsonObject.getString("Category").equalsIgnoreCase(mapAssetContentMarkerList.get(j).getParentType()) &&
                                    jsonObject.getString("Chain").equalsIgnoreCase(mapAssetContentMarkerList.get(j).getSubCategoryName())) {
//                        if (jsonObject.getString("Chain").equalsIgnoreCase(mapAssetContentMarkerList.get(j).getSubCategoryName())) {
                        addInfoInMapAssetContentList(jsonObject, j);
                    }
                }
//                Log.d(TAG, "saveMapAsset: getMapAssetsFilteredContent: mapAssetContentList: size: " + mapAssetContentList.size());
            } catch (JSONException jsonException) {
                Log.d(TAG, "saveMapAsset: getInfoForSelectedAssets: jsonException: " + jsonException);
            }
        }

        Log.d(TAG, "saveMapAsset: getInfoForSelectedAssets: mapAssetContentList: size: " + mapAssetContentList.size());
        return mapAssetContentList;
    }

    void addInfoInMapAssetContentList(JSONObject jsonObject, int index) {

//        mapAssetContentList.clear();
        try {
            String address = "", category = "", chain = "", name = "", recordId = "", state = "", symbolName = "",
                    symbolRecordId = "";
            double latitude = 0.0, longitude = 0.0;

//                "Address": "9201 Grand Bay",
//                        "Category": "fuel",
//                        "Chain": "TA",
//                        "Latitude": 30.4985,
//                        "Longitude": -88.3336,
//                        "Name": "Grand Bay",
//                        "RecordId": "1950358",
//                        "State": "AL",
//                        "Symbol Name": "ta-pin",
//                        "SymbolRecordId": "1951519"


            if (jsonObject.has("Latitude")) {
                latitude = jsonObject.getLong("Latitude");
            }

            if (jsonObject.has("Longitude")) {
                longitude = jsonObject.getLong("Longitude");
            }

//            Log.d(TAG, "saveMapAsset: getMapAssetsFilteredContent: latitude: " + latitude + " longitude: " + longitude);
//            Nov 03, 2022  -   Roy wanted to see all the map assets
//            (that's why removing filter to show map assets which are only less than 1000)
//            float distanceInMeters = Utils.calculateDistance(new LatLng(latitude, longitude), mapFragment.getMyLastLocation());
//            float distanceInMiles = Utils.convertMetersToMiles(distanceInMeters);
//            if (distanceInMiles <= 100) {
            if (jsonObject.has("Address")) {
                address = jsonObject.getString("Address");
            }

            if (jsonObject.has("Category")) {
                category = jsonObject.getString("Category");
            }

            if (jsonObject.has("Chain")) {
                chain = jsonObject.getString("Chain");
            }

            if (jsonObject.has("Latitude")) {
                latitude = jsonObject.getDouble("Latitude");
            }

            if (jsonObject.has("Longitude")) {
                longitude = jsonObject.getDouble("Longitude");
            }

            if (jsonObject.has("Name")) {
                name = jsonObject.getString("Name");
            }

            if (jsonObject.has("RecordId")) {
                recordId = jsonObject.getString("RecordId");
            }

            if (jsonObject.has("State")) {
                state = jsonObject.getString("State");
            }

            if (jsonObject.has("Symbol Name")) {
                symbolName = jsonObject.getString("Symbol Name");
            }
            if (jsonObject.has("SymbolRecordId")) {
                symbolRecordId = jsonObject.getString("SymbolRecordId");
            }

            mapAssetContentModel = new MapAssetContentModel();
            mapAssetContentModel.setAddress(address);
            mapAssetContentModel.setCategory(category);
            mapAssetContentModel.setChain(chain);
            mapAssetContentModel.setLatitude(latitude);
            mapAssetContentModel.setLongitude(longitude);
            mapAssetContentModel.setName(name);
            mapAssetContentModel.setRecordId(recordId);
            mapAssetContentModel.setState(state);
            mapAssetContentModel.setSymbolName(symbolName);
            mapAssetContentModel.setSymbolRecordId(symbolRecordId);
            mapAssetContentModel.setMarkerImageId(mapAssetContentMarkerList.get(index).getMarkerId());

            mapAssetContentList.add(mapAssetContentModel);
//                            mapFragment.addSelectedAssetsMarkers(mapAssetContentModel, getActivity().getResources().getDrawable(R.drawable.poi_map_loves));
//            }

        } catch (JSONException jsonException) {
            Log.d(TAG, "saveMapAsset: getMapAssetsFilteredContent: jsonException: " + jsonException);
        }

    }


    //   Sep 07, 2022  -   Reload the shift info(gauges) when we manually change the status
    void reloadShiftInfo() {
        rules.resetDutyEventCode();
        updateBottomDrivingStatus();

        calculateHours();
        updateBottomSheetGaugesValues();
        updateStatusAndGauges();
    }

    public class GetMapAssets extends AsyncTask<String, String, List<MapAssetContentModel>> {

        public GetMapAssets() {
            Log.d(TAG, "saveMapAsset: GetMapAssets: ");
        }

        @Override
        protected void onPreExecute() {
            Log.d(TAG, "saveMapAsset: GetMapAssets: onPreExecute: ");
//            Nov 08, 2022  -   clear function also somehow effecting the offline tiles
//            So should only remove markers instead of calling clear which will remove markers but also somehow offline tiles
//            googleMap.clear();
            mapAssetContentMarkerList.clear();
            mapAssetContentList.clear();
        }

        @Override
        protected List<MapAssetContentModel> doInBackground(String... params) {
            Log.d(TAG, "saveMapAsset: GetMapAssets: doInBackground: ");
            getSelectedAssets();
            return getInfoForSelectedAssets();
        }

        @Override
        protected void onPostExecute(List<MapAssetContentModel> arrayList) {
            Log.d(TAG, "saveMapAsset: GetMapAssets: onPostExecute: arrayList: size: " + arrayList.size());
            if (arrayList != null) {
                if (arrayList.size() > 0) {
//                    showSelectedAssetsMarkers();
                    showMapAssetsUsingUIThread();
                }
            }
        }
    }


    void showMapAssetsUsingUIThread() {
        Log.d(TAG, "saveMapAsset: GetMapAssets: showMapAssetsUsingUIThread: ");
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showSelectedAssetsMarkers();
                }
            });
        }
    }

    void setBearingPoint(LatLng bearingPoint) {
        Log.d(TAG, "saveMapAsset: GetMapAssets: showMapAssetsUsingUIThread: ");
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    bearingMarker.setPosition(bearingPoint);
                }
            });
        }
    }

    void setMarkerPosition(LatLng calculatedPosition) {
        Log.d(TAG, "saveMapAsset: GetMapAssets: showMapAssetsUsingUIThread: ");
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPositionMarker.setPosition(calculatedPosition);
                }
            });
        }
    }


    void showSelectedAssetsMarkers() {
        Log.d(TAG, "GetMapAssets: saveMapAsset: showSelectedAssetsMarkers: mapAssetContentList: " + mapAssetContentList.size());
        showProgressDialog(false);
//        Oct 02, 2022  -   Below method call was working before making marker Cluster
//        mapFragment.addSelectedAssetsMarkers(mapAssetContentList);
//        Oct 02, 2022  -   addSelectedAssetsMarkersUpdate when making markers as cluster
        mapFragment.addSelectedAssetsMarkersUpdate(mapAssetContentList);
    }


    //    Nov 14, 2022  -   "calloutMarker" is responsible for showing sign boards(inside blue bubble)
//    We are only showing it when we are online else
//    (offline) we are hiding it because when we apply tiles it zoom outs so everything looks messy so
//    we are not showing blue bubble sign board when offline
    void setInfoWindowVisibility(boolean show) {
        Log.d(TAG, "IC: removeTileOverlay: ");
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (show) {
                        calloutMarker.showInfoWindow();
                    } else {
                        calloutMarker.hideInfoWindow();
                    }
                }
            });
        }

    }


    void startTimerForOfflineMap() {
        Log.d(TAG, "IC: startTimerForOfflineMap: ");
        internetConnectionTimerHandler.postDelayed(internetConnectionTimer, 0);
        icCounter = 0;
    }

    Runnable internetConnectionTimer = new Runnable() {
        @Override
        public void run() {
            icCounter++;
            Log.d(TAG, "IC: run: icCounter: " + icCounter);

            if (icCounter > 30) {
                closeOfflineMapTimer();
                setMapZoomLevel(true);
                applyOfflineCustomTilesOverlay();
            } else {
                internetConnectionTimerHandler.postDelayed(this, 1000);
            }
        }
    };

    void closeOfflineMapTimer() {
        Log.d(TAG, "IC: closeOfflineMapTimer: ");
        icCounter = 0;
        if (internetConnectionTimerHandler != null) {
            internetConnectionTimerHandler.removeCallbacks(internetConnectionTimer);
        }
    }

    void applyOfflineCustomTilesOverlay() {
        Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: ");

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {

                if (calloutMarker != null) {
                    setInfoWindowVisibility(false);
                }

                TileProvider tileProvider = new TileProvider() {
                    @Nullable
                    @org.jetbrains.annotations.Nullable
                    @Override
                    public Tile getTile(int x, int y, int zoom) {
                        Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: getTile: ");

                        if (!isICAvailable) {
                            try {
                                Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: getTile: x: " + x + " y: " + y + " zoom: " + zoom);
                                if (!checkTileExists(x, y, zoom)) {
                                    return null;
                                }
                                Log.d(TAG, "IC: getTile: applyOfflineCustomTilesOverlay: tileAvailable: ");


//                                LOAD FROM ASSETS
//                                Jan 05, 2022  -   Below two lines are used to generate an input stream from android assets folder
                                String filePath = String.format("tiles/%d/%d/%d.png", zoom, x, y);
//                                InputStream inputStream = getActivity().getAssets().open(filePath);

//                                LOAD FROM PHONE STORAGE (path to the unzipped tiles folder from phones memory)
//                                After testing its working fine that we are able to use these unzipped tiles (images) from phone's memory
//                                Jan 05, 2022  -   Below two lines wil get the required file path from phone storage
//                                String requiredTileImagePath = String.format("offline_tiles_unzipped/%d/%d/%d.png", zoom, x, y);
//                                String zippedFileDirectoryPath="/storage/emulated/0/Documents/RCOTrucks/"+requiredTileImagePath;
                                String requiredTileImagePath = String.format("OfflineTiles/filtered_tiles/%d/%d/%d.png", zoom, x, y);
                                String zippedFileDirectoryPath="/storage/emulated/0/Documents/RCOTruck/"+requiredTileImagePath;
//                                Now from file path we need to generate input stream
                                InputStream inputStream = new FileInputStream(zippedFileDirectoryPath);


                                Log.d(TAG, "IC: getTile: applyOfflineCustomTilesOverlay: inputStream: " + inputStream);
                                byte[] buffer = new byte[8192];
                                int bytesRead;
                                ByteArrayOutputStream output = new ByteArrayOutputStream();

                                while ((bytesRead = inputStream.read(buffer)) != -1) {
                                    output.write(buffer, 0, bytesRead);
                                }
                                byte file[] = output.toByteArray();

                                Tile result = new Tile(256, 256, file);
                                Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: getTile: before: result: x: " + x + " y: " + y + " zoom: " + zoom);
                                Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: getTile: before: result: " + result);
                                return result;
                            } catch (Exception e) {
                                Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: getTile: exception: " + e.getMessage() + " exception: " + e);
                            }
                        }
                        return null;
                    }

                    /*
                     * Check that the tile server supports the requested x, y and zoom.
                     * Complete this stub according to the tile range you support.
                     * If you support a limited range of tiles at different zoom levels, then you
                     * need to define the supported x, y range at each zoom level.
                     */
                    private boolean checkTileExists(int x, int y, int zoom) {
                        Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: checkTileExists: x: " + x + " y: " + y + " zoom: " + zoom);
                        int minZoom = 4;
                        int maxZoom = 11;

                        return (zoom >= minZoom && zoom <= maxZoom);
                    }
                };
                Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: tileProvider: " + tileProvider);
                if (tileProvider != null) {
                    tileOverlay = googleMap
                            .addTileOverlay(new TileOverlayOptions()
                                    .tileProvider(tileProvider));
                }
            }
        });
    }

    //    remove a tile overlay
    void removeTileOverlay() {
        Log.d(TAG, "IC: applyOfflineCustomTilesOverlay: removeTileOverlay: ");
        if (mActivity != null) {

            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (tileOverlay != null) {
                        tileOverlay.remove();
                    }
                    setMapZoomLevel(false);

//                Location lastLocation = rules.getPhoneLastBestLocation(getActivity());
//                Log.d(TAG, "IC: run: mCurrentLocation: " + mCurrentLocation);
//                mapFragment.adjustCamera(mCurrentLocation);
                }
            });
        }

    }


    void setMapZoomLevel(boolean isOffline) {
        if (isOffline) {
            googleMap.setMaxZoomPreference(11);
            googleMap.setMinZoomPreference(4);
        } else {
            googleMap.resetMinMaxZoomPreference();
        }
    }


    //    to force a refresh
    void reloadTileOverlay() {
        Log.d(TAG, "IC: reloadTileOverlay: ");
        if (tileOverlay != null) {
            tileOverlay.clearTileCache();
        }
    }

    void adjustCameraToUserLocation() {
        Log.d(TAG, "onNetworkAvailable: lastNotNullLocation: " + lastNotNullLocation);
        Log.d(TAG, "onNetworkAvailable: getPhoneLastBestLocation: " + rules.getPhoneLastBestLocation(getActivity()));
        Log.d(TAG, "onNetworkAvailable: getActivity(): " + getActivity());
        Log.d(TAG, "onNetworkAvailable: googleMap: " + googleMap);

//            Oct 25, 2022  -   The app was crashed saying Not on the Main Thread
        if (mActivity != null && !mActivity.isDestroyed()) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    //        SEP 27, 2022  -   Use updateMyPositionUpdate method to go back to user location in case of navigation else we are gonna use adjustCamera method from business rules
                    if (displayedRoute != null) {
                        updateMyPositionUpdate();
                    } else {
                        LatLng latLng = null;
                        if (lastNotNullLocation != null) {
                            latLng = new LatLng(lastNotNullLocation.getLatitude(), lastNotNullLocation.getLongitude());
                        } else {
                            if (rules.getPhoneLastBestLocation(getActivity()) != null) {
                                latLng = new LatLng(rules.getPhoneLastBestLocation(getActivity()).getLatitude(), rules.getPhoneLastBestLocation(getActivity()).getLongitude());
                            }
                        }

//        Sep 21, 2022  -   latLng is null in some cases like when app initially loaded and have not permission for location access
                        if (latLng != null) {
                            if (UiUtils.isOnline(getContext())) {
                                Log.d(TAG, "adjustCameraToUserLocation: online: ");
                                mapFragment.adjustCamera(latLng);
                            } else {
                                Log.d(TAG, "adjustCameraToUserLocation: offline: ");
                                mapFragment.adjustCamera(latLng, 11.0F);
                            }
                            btnCenterMap.setVisibility(View.GONE);
                        }
                    }

                }
            });
        }
    }


    //    Sep 19, 2022  -   Actually when we were showing a searched location -
//    the default icon for location and zoom controls
//    misplaced from there position and went quite left of screen
//    So we hide them when search a location and show them back when user close the searched route or exit a selected route
    void enableMapControls(boolean enableControl) {
        if (enableControl) {
            mapFragment.enableLocationButton(false);
            mapFragment.enableZoomControls(true);
            mapFragment.enableCompassControls(true);
            googleMap.setPadding(0, 0, 0, 0);
        } else {
            mapFragment.enableLocationButton(false);
            mapFragment.enableZoomControls(false);
            mapFragment.enableCompassControls(false);
        }
    }


    //    Sep 22, 2022  -   Check if we lose focus from user location on map while scrolling map only then show the recenter button etc don't show it
    void setRecenterMapButton() {
//        Log.d(TAG, "setRecenterMapButton: ");

//        if (displayedRoute == null) {
        if (googleMap != null) {
//            Log.d(TAG, "setRecenterMapButton: ");
            if (!googleMap.getProjection().getVisibleRegion().latLngBounds.contains(mCurrentLocation)) {
                Log.d(TAG, "projection: onLocationChanged: MyLocation is not visible ");
                if (btnCenterMap.getVisibility() == View.GONE) {
                    btnCenterMap.setVisibility(VISIBLE);
                }
            } else {
                if (btnCenterMap.getVisibility() == VISIBLE) {
                    btnCenterMap.setVisibility(View.GONE);
                }
            }
        }
//        } else {
//            if (btnCenterMap.getVisibility() == VISIBLE) {
//                btnCenterMap.setVisibility(View.GONE);
//            }
//        }
    }

    void setGaugesWidgets(View view) {
        Log.d(TAG, "setGaugesWidgets: ");

        gaugesDrivingBar = view.findViewById(R.id.gauges_driving_bar);
        gaugesBreakBar = view.findViewById(R.id.gauges_break_bar);
        gaugesShiftBar = view.findViewById(R.id.gauges_shift_bar);
        gaugesCycleBar = view.findViewById(R.id.gauges_cycle_bar);

        drivingBarValue = view.findViewById(R.id.driving_bar_value);
        breakBarValue = view.findViewById(R.id.break_bar_value);
        shiftBarValue = view.findViewById(R.id.shift_bar_value);
        cycleBarValue = view.findViewById(R.id.cycle_bar_value);
    }

    //    Percentage values varies from 0 to 270
    private Bitmap getWidgetBitmapUpdate(Context context, int percentage) {
        Log.d(TAG, "getWidgetBitmap: ");
        int width = 400;
        int height = 400;
        int stroke = 30;
        int padding = 5;
        float density = context.getResources().getDisplayMetrics().density;

        //Paint for arc stroke.
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        //paint.setStrokeJoin(Paint.Join.ROUND);
        //paint.setPathEffect(new CornerPathEffect(10) );
        //Paint for text values.
        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize((int) (context.getResources().getDimension(R.dimen.rco_trucks_xx_large_text_size) / density));
        mTextPaint.setColor(context.getResources().getColor(R.color.side_panel_remaining_progress_color));
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        final RectF arc = new RectF();
        arc.set((stroke / 2) + padding, (stroke / 2) + padding, width - padding - (stroke / 2), height - padding - (stroke / 2));

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //First draw full arc as background.
        paint.setColor(Color.argb(75, 255, 255, 255));
        canvas.drawArc(arc, 135, 270, false, paint);
        //Then draw arc progress with actual value.
        paint.setColor(context.getResources().getColor(R.color.side_panel_active_progress_color));

        canvas.drawArc(arc, 135, percentage, false, paint);

//Draw text value.
//        canvas.drawText(percentage + "%", bitmap.getWidth() / 2, (bitmap.getHeight() - mTextPaint.ascent()) / 2, mTextPaint);
//Draw widget title.
//        mTextPaint.setTextSize((int) (context.getResources().getDimension(R.dimen.rco_trucks_xxx_large_text_size) / density));
//        canvas.drawText("Driving", bitmap.getWidth() / 2, bitmap.getHeight()-(stroke+padding), mTextPaint);

        return bitmap;
    }

    private void handleDayOrNightView() {
        Log.d(TAG, "handleDayOrNightView: ");
        if (rules.getPhoneLastBestLocation(getActivity()) != null) {
            boolean isNight = rules.isItNight(new LatLng(rules.getPhoneLastBestLocation(getActivity()).getLatitude(), rules.getPhoneLastBestLocation(getActivity()).getLongitude()));
            Log.d(TAG, "handleDayOrNightView: isNight: " + isNight);
            UiUtils.applyDarkTheme(isNight);
        }
    }

    void openDialogForLove(String recordId) {
        Log.d(TAG, "amenity: openDialogForLove: ");
        amenitiesLayout.setVisibility(VISIBLE);
        checkAmenitiesImages("love", "map_assets_love", "ameneties_images_info", recordId);
    }

    void openDialogForPilot(String recordId) {
        amenitiesLayout.setVisibility(VISIBLE);
        checkAmenitiesImages("pilot", "map_assets_pilot", "ameneties_images_info", recordId);
    }

    void openDialogForTA(String recordId) {
        amenitiesLayout.setVisibility(VISIBLE);
        Log.d(TAG, "openDialogForTA: amenity: recordId: " + recordId);
        checkAmenitiesImages("ta", "map_assets_ta", "ameneties_images_info", recordId);
    }

    void openDialogForRestArea(String recordId) {
        amenitiesLayout.setVisibility(VISIBLE);
        checkAmenitiesImages("rest_area", "map_assets_rest_area", "ameneties_rest_area_images_info", recordId);
    }

    private void checkAmenitiesImages(String category, String fileName, String amenitiesImages, String recordId) {
        Log.d(TAG, "amenity: checkAmenitiesImages: category: " + category + " filename: " + fileName + " amenityImage: " + amenitiesImages + " recordId: " + recordId);
        try {
            JSONObject amenitiesJSONObject = new JSONObject(BusinessRules.getJsonFromAssets(getActivity(), "stations/" + amenitiesImages + ".json"));
            Log.d(TAG, "amenity: checkAmenitiesImages: amenitiesJSONObject: " + amenitiesJSONObject);

            getAmenities(category, fileName, amenitiesJSONObject, recordId);
            setExpandableAdapterToAmenitiesListView();
        } catch (JSONException jsonException) {
            Log.d(TAG, "amenity: checkAmenitiesImages: jsonException: " + jsonException);
        }
    }

    private void checkAmenitiesImagesUpdate(String category, String fileName, String amenitiesImages, String recordId) {
        Log.d(TAG, "sortAmenity: amenity: checkAmenitiesImages: category: " + category + " filename: " + fileName + " amenityImage: " + amenitiesImages + " recordId: " + recordId);
        try {
            String jsonAssets = BusinessRules.getJsonFromAssets(getActivity(), "stations/" + amenitiesImages + ".json");
            if (jsonAssets == null) {
                return;
            }

            JSONObject amenitiesJSONObject = new JSONObject(jsonAssets);
            Log.d(TAG, "sortAmenity: amenity: checkAmenitiesImages: amenitiesJSONObject: " + amenitiesJSONObject);
            Log.d(TAG, "sortAmenity: amenity: recordId: " + recordId);

            getAmenities(category, fileName, amenitiesJSONObject, recordId);
            sortAmenity();

        } catch (JSONException jsonException) {
            Log.d(TAG, "sortAmenity: amenity: checkAmenitiesImages: jsonException: " + jsonException);
        }
    }


    private void getAmenities(String category, String fileName, JSONObject amenitiesJSONObject, String recordId) {
        Log.d(TAG, "sortAmenity: amenity: getAmenities: ");
        try {
            JSONArray jsonArray = new JSONArray(BusinessRules.getJsonFromAssets(getContext(), "stations/" + category + "/" + fileName + ".json"));
            Log.d(TAG, "sortAmenity: amenity: getAmenities: jsonArray: " + jsonArray.length());
            for (int x = 0; x < jsonArray.length(); x++) {
                Log.d(TAG, "sortAmenity: amenity: getAmenities: x: " + x);
                JSONObject recordJSONObject = jsonArray.getJSONObject(x);
                if (recordJSONObject.has("RecordId") && recordJSONObject.get("RecordId").equals(recordId)) {
                    Log.d(TAG, "sortAmenity: amenity: getAmenities: recordJSONObject: " + recordJSONObject + " index: " + x);
//                    storeClickedAssetRecord(recordJSONObject, amenitiesJSONObject);
                    storeClickedAssetRecordUpdate(recordJSONObject, amenitiesJSONObject);
                    break;
                }
            }
        } catch (JSONException jsonException) {
            Log.d(TAG, "sortAmenity: amenity: getAmenities: jsonException: " + jsonException);
        }
    }

    void storeClickedAssetRecordUpdate(JSONObject jsonObject, JSONObject amenitiesJSONObject) {
        Log.d(TAG, "sortAmenity: storeClickedAssetRecordUpdate: ");
        if (amenitiesHeaderDataList == null) {
            amenitiesHeaderDataList = new ArrayList<>();
        }
        amenitiesHeaderDataList.clear();
        try {

            String amenitiesBusinessTransflo = "", amenitiesFoodRestaurants = "", amenitiesFuelBulkDEF = "",
                    amenitiesFuelBulkPropane = "", amenitiesFuelEVLevel2 = "", amenitiesFuelEVLevel3DCFC = "",
                    amenitiesFuelEVTeslaLevel2 = "", amenitiesFuelEVTeslaLevel3DCFC = "",
                    amenitiesFuelFastFillCNGAuto = "", amenitiesFuelFastFillCNGClass8 = "",
                    amenitiesFuelPropaneTankExchange = "", amenitiesFuelRFIDPumpStart = "", amenitiesOtherATM = "",
                    amenitiesOtherDogPark = "", amenitiesOtherLaundryFacility = "", amenitiesOtherPrivateShowers = "", amenitiesOtherVideoGaming = "", amenitiesOtherWiFiBasic = "", amenitiesOtherWiFiPremium = "", amenitiesParkingTruckParking = "", amenitiesRVRVDumpService = "", amenitiesRVRVFriendlyParking = "", amenitiesRVRVHookup = "", amenitiesServiceCommercialTruckOilChange = "", amenitiesServiceLightMechanical = "", amenitiesServiceSpeedcoOnSite = "", amenitiesServiceTireServices = "", amenitiesServiceTirePassInLane = "", amenitiesServiceTirePassInServiceCenter = "", amenitiesServiceTirePassMobile = "", amenitiesTruckCATScales = "", amenitiesRecordId = "", amenitiesBusinessTransfloImage = "", amenitiesFoodRestaurantsImage = "", amenitiesFuelBulkDEFImage = "", amenitiesFuelBulkPropaneImage = "", amenitiesFuelEVLevel2Image = "", amenitiesFuelEVLevel3DCFCImage = "", amenitiesFuelEVTeslaLevel2Image = "", amenitiesFuelEVTeslaLevel3DCFCImage = "", amenitiesFuelFastFillCNGAutoImage = "", amenitiesFuelFastFillCNGClass8Image = "", amenitiesFuelPropaneTankExchangeImage = "", amenitiesFuelRFIDPumpStartImage = "", amenitiesOtherATMImage = "", amenitiesOtherDogParkImage = "", amenitiesOtherLaundryFacilityImage = "", amenitiesOtherPrivateShowersImage = "", amenitiesOtherVideoGamingImage = "", amenitiesOtherWiFiBasicImage = "", amenitiesOtherWiFiPremiumImage = "", amenitiesParkingTruckParkingImage = "", amenitiesRVRVDumpServiceImage = "", amenitiesRVRVFriendlyParkingImage = "", amenitiesRVRVHookupImage = "", amenitiesServiceCommercialTruckOilChangeImage = "", amenitiesServiceLightMechanicalImage = "", amenitiesServiceSpeedcoOnSiteImage = "", amenitiesServiceTireServicesImage = "", amenitiesServiceTirePassInLaneImage = "", amenitiesServiceTirePassInServiceCenterImage = "", amenitiesServiceTirePassMobileImage = "", amenitiesTruckCATScalesImage = "", transflo = "", restaurants = "", bulkDEF = "", bulkPropane = "", EVLevel2 = "", EVLevel3DCFC = "", EVTeslaLevel2 = "", EVTeslaLevel3DCFC = "", fastFillCNGAuto = "", fastFillCNGClass8 = "", propaneTankExchange = "", RFIDPumpStart = "", ATM = "", dogPark = "", laundryFacility = "", privateShowers = "", videoGaming = "", WiFiBasic = "", WiFiPremium = "", truckParking = "", RVDumpService = "", RVFriendlyParking = "", RVHookup = "", commercialTruckOilChange = "", lightMechanical = "", speedCoOnSite = "", tireServices = "", tirePassInLane = "", tirePassInServiceCenter = "", tirePassMobile = "", CATScales = "";

            //            empty - n - N - No - no
            if (jsonObject.has("Amenities-Business Transflo")) {
                String keyValue = jsonObject.getString("Amenities-Business Transflo");

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    Log.d(TAG, "storeClickedAssetRecord: value: " + keyValue);
                    amenitiesBusinessTransflo = jsonObject.getString("Amenities-Business Transflo");
                    String key = getImageInfo("Amenities-Business Transflo");
                    transflo = key;
                    boolean isActive = false;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesBusinessTransfloImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesBusinessTransfloImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//            if (jsonObject.has("Amenities-Food Restaurants")) {
//                String keyValue = jsonObject.getString("Amenities-Food Restaurants").toString();
//
//                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
//                        && (!keyValue.equalsIgnoreCase("no"))) {
//
//                    amenitiesFoodRestaurants = jsonObject.getString("Amenities-Food Restaurants");
//                    String key = getImageInfo("Amenities-Food Restaurants");
//                    restaurants = key;
//                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
//                        amenitiesFoodRestaurantsImage = amenitiesJSONObject.getString(key);
//                    }
//                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
//                    amenitiesGenericModel.setName(key);
//                    amenitiesGenericModel.setImageUrl(amenitiesFoodRestaurantsImage);
//                    amenitiesHeaderDataList.add(amenitiesGenericModel);
//                }
//            }


            if (jsonObject.has("Amenities-Fuel Bulk DEF")) {
                String keyValue = jsonObject.getString("Amenities-Fuel Bulk DEF").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelBulkDEF = jsonObject.getString("Amenities-Fuel Bulk DEF");
                    String key = getImageInfo("Amenities-Fuel Bulk DEF");
                    bulkDEF = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelBulkDEFImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelBulkDEFImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Fuel Bulk Propane")) {
                String keyValue = jsonObject.getString("Amenities-Fuel Bulk Propane").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelBulkPropane = jsonObject.getString("Amenities-Fuel Bulk Propane");
                    String key = getImageInfo("Amenities-Fuel Bulk Propane");
                    bulkPropane = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelBulkPropaneImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelBulkPropaneImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Fuel EV Level 2")) {
                String keyValue = jsonObject.getString("Amenities-Fuel EV Level 2").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelEVLevel2 = jsonObject.getString("Amenities-Fuel EV Level 2");
                    String key = getImageInfo("Amenities-Fuel EV Level 2");
                    EVLevel2 = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelEVLevel2Image = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelEVLevel2Image);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Fuel EV Level 3 DCFC")) {
                String keyValue = jsonObject.getString("Amenities-Fuel EV Level 3 DCFC").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelEVLevel3DCFC = jsonObject.getString("Amenities-Fuel EV Level 3 DCFC");
                    String key = getImageInfo("Amenities-Fuel EV Level 3 DCFC");
                    EVLevel3DCFC = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelEVLevel3DCFCImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelEVLevel3DCFCImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Fuel EV Tesla Level 2")) {
                String keyValue = jsonObject.getString("Amenities-Fuel EV Tesla Level 2").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelEVTeslaLevel2 = jsonObject.getString("Amenities-Fuel EV Tesla Level 2");
                    String key = getImageInfo("Amenities-Fuel EV Tesla Level 2");
                    EVTeslaLevel2 = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelEVTeslaLevel2Image = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelEVTeslaLevel2Image);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Fuel EV Tesla Level 3 DCFC")) {
                String keyValue = jsonObject.getString("Amenities-Fuel EV Tesla Level 3 DCFC").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelEVTeslaLevel3DCFC = jsonObject.getString("Amenities-Fuel EV Tesla Level 3 DCFC");
                    String key = getImageInfo("Amenities-Fuel EV Tesla Level 3 DCFC");
                    EVTeslaLevel3DCFC = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelEVTeslaLevel3DCFCImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelEVTeslaLevel3DCFCImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);

                }
            }


            if (jsonObject.has("Amenities-Fuel Fast Fill CNG (Auto)")) {
                String keyValue = jsonObject.getString("Amenities-Fuel Fast Fill CNG (Auto)").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelFastFillCNGAuto = jsonObject.getString("Amenities-Fuel Fast Fill CNG (Auto)");
                    String key = getImageInfo("Amenities-Fuel Fast Fill CNG (Auto)");
                    fastFillCNGAuto = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelFastFillCNGAutoImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelFastFillCNGAutoImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Fuel Fast Fill CNG (Class 8)")) {
                String keyValue = jsonObject.getString("Amenities-Fuel Fast Fill CNG (Class 8)").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelFastFillCNGClass8 = jsonObject.getString("Amenities-Fuel Fast Fill CNG (Class 8)");
                    String key = getImageInfo("Amenities-Fuel Fast Fill CNG (Class 8)");
                    fastFillCNGClass8 = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelFastFillCNGClass8Image = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelFastFillCNGClass8Image);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Fuel Propane Tank Exchange")) {
                String keyValue = jsonObject.get("Amenities-Fuel Propane Tank Exchange").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelPropaneTankExchange = jsonObject.getString("Amenities-Fuel Propane Tank Exchange");
                    String key = getImageInfo("Amenities-Fuel Propane Tank Exchange");
                    propaneTankExchange = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelPropaneTankExchangeImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelPropaneTankExchangeImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Fuel RFID Pump Start")) {
                String keyValue = jsonObject.get("Amenities-Fuel RFID Pump Start").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesFuelRFIDPumpStart = jsonObject.getString("Amenities-Fuel RFID Pump Start");
                    String key = getImageInfo("Amenities-Fuel RFID Pump Start");
                    RFIDPumpStart = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesFuelRFIDPumpStartImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesFuelRFIDPumpStartImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Other ATM")) {
                String keyValue = jsonObject.get("Amenities-Other ATM").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesOtherATM = jsonObject.getString("Amenities-Other ATM");
                    String key = getImageInfo("Amenities-Other ATM");
                    ATM = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesOtherATMImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesOtherATMImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Other Dog Park")) {
                String keyValue = jsonObject.get("Amenities-Other Dog Park").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesOtherDogPark = jsonObject.getString("Amenities-Other Dog Park");
                    String key = getImageInfo("Amenities-Other Dog Park");
                    dogPark = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesOtherDogParkImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesOtherDogParkImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Other Laundry Facility")) {
                String keyValue = jsonObject.get("Amenities-Other Laundry Facility").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesOtherLaundryFacility = jsonObject.getString("Amenities-Other Laundry Facility");
                    String key = getImageInfo("Amenities-Other Laundry Facility");
                    laundryFacility = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesOtherLaundryFacilityImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesOtherLaundryFacilityImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Other Private Showers")) {
                String keyValue = jsonObject.get("Amenities-Other Private Showers").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesOtherPrivateShowers = jsonObject.getString("Amenities-Other Private Showers");
                    String key = getImageInfo("Amenities-Other Private Showers");
                    privateShowers = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesOtherPrivateShowersImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesOtherPrivateShowersImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Other Video Gaming")) {
                String keyValue = jsonObject.get("Amenities-Other Video Gaming").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesOtherVideoGaming = jsonObject.getString("Amenities-Other Video Gaming");
                    String key = getImageInfo("Amenities-Other Video Gaming");
                    videoGaming = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesOtherVideoGamingImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesOtherVideoGamingImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Other Wi-Fi - Basic")) {
                String keyValue = jsonObject.get("Amenities-Other Wi-Fi - Basic").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesOtherWiFiBasic = jsonObject.getString("Amenities-Other Wi-Fi - Basic");
                    String key = getImageInfo("Amenities-Other Wi-Fi - Basic");
                    WiFiBasic = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesOtherWiFiBasicImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesOtherWiFiBasicImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Other Wi-Fi - Premium")) {
                String keyValue = jsonObject.get("Amenities-Other Wi-Fi - Premium").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesOtherWiFiPremium = jsonObject.getString("Amenities-Other Wi-Fi - Premium");
                    String key = getImageInfo("Amenities-Other Wi-Fi - Premium");
                    WiFiPremium = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesOtherWiFiPremiumImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesOtherWiFiPremiumImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Parking Truck Parking")) {
                String keyValue = jsonObject.get("Amenities-Parking Truck Parking").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesParkingTruckParking = jsonObject.getString("Amenities-Parking Truck Parking");
                    String key = getImageInfo("Amenities-Parking Truck Parking");
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesParkingTruckParkingImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesParkingTruckParkingImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-RV RV Dump Service")) {
                String keyValue = jsonObject.get("Amenities-RV RV Dump Service").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesRVRVDumpService = jsonObject.getString("Amenities-RV RV Dump Service");
                    String key = getImageInfo("Amenities-RV RV Dump Service");
                    RVDumpService = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesRVRVDumpServiceImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesRVRVDumpServiceImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-RV RV Friendly Parking")) {
                String keyValue = jsonObject.get("Amenities-RV RV Friendly Parking").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesRVRVFriendlyParking = jsonObject.getString("Amenities-RV RV Friendly Parking");
                    String key = getImageInfo("Amenities-RV RV Friendly Parking");
                    RVFriendlyParking = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesRVRVFriendlyParkingImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesRVRVFriendlyParkingImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-RV RV Hookup")) {
                String keyValue = jsonObject.get("Amenities-RV RV Hookup").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesRVRVHookup = jsonObject.getString("Amenities-RV RV Hookup");
                    String key = getImageInfo("Amenities-RV RV Hookup");
                    RVHookup = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesRVRVHookupImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesRVRVHookupImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Service Commercial Truck Oil Change")) {
                String keyValue = jsonObject.get("Amenities-Service Commercial Truck Oil Change").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesServiceCommercialTruckOilChange = jsonObject.getString("Amenities-Service Commercial Truck Oil Change");
                    String key = getImageInfo("Amenities-Service Commercial Truck Oil Change");
                    commercialTruckOilChange = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesServiceCommercialTruckOilChangeImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesServiceCommercialTruckOilChangeImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Service Light Mechanical")) {
                String keyValue = jsonObject.get("Amenities-Service Light Mechanical").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesServiceLightMechanical = jsonObject.getString("Amenities-Service Light Mechanical");
                    String key = getImageInfo("Amenities-Service Light Mechanical");
                    lightMechanical = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesServiceLightMechanicalImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesServiceLightMechanicalImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Service Speedco On-Site")) {
                String keyValue = jsonObject.get("Amenities-Service Speedco On-Site").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesServiceSpeedcoOnSite = jsonObject.getString("Amenities-Service Speedco On-Site");
                    String key = getImageInfo("Amenities-Service Speedco On-Site");
                    speedCoOnSite = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesServiceSpeedcoOnSiteImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesServiceSpeedcoOnSiteImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Service Tire Services")) {
                String keyValue = jsonObject.get("Amenities-Service Tire Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesServiceTireServices = jsonObject.getString("Amenities-Service Tire Services");
                    String key = getImageInfo("Amenities-Service Tire Services");
                    tireServices = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesServiceTireServicesImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesServiceTireServicesImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Service TirePass In-Lane")) {
                String keyValue = jsonObject.get("Amenities-Service TirePass In-Lane").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesServiceTirePassInLane = jsonObject.getString("Amenities-Service TirePass In-Lane");
                    String key = getImageInfo("Amenities-Service TirePass In-Lane");
                    tirePassInLane = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesServiceTirePassInLaneImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesServiceTirePassInLaneImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Service TirePass In-Service Center")) {
                String keyValue = jsonObject.get("Amenities-Service TirePass In-Service Center").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesServiceTirePassInServiceCenter = jsonObject.getString("Amenities-Service TirePass In-Service Center");
                    String key = getImageInfo("Amenities-Service TirePass In-Service Center");
                    tirePassInServiceCenter = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesServiceTirePassInServiceCenterImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesServiceTirePassInServiceCenterImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Service TirePass Mobile")) {
                String keyValue = jsonObject.get("Amenities-Service TirePass Mobile").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesServiceTirePassMobile = jsonObject.getString("Amenities-Service TirePass Mobile");
                    String key = getImageInfo("Amenities-Service TirePass Mobile");
                    tirePassMobile = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesServiceTirePassMobileImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesServiceTirePassMobileImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Amenities-Truck CAT Scales")) {

                String keyValue = jsonObject.get("Amenities-Truck CAT Scales").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Truck CAT Scales");
                    String key = getImageInfo("Amenities-Truck CAT Scales");
                    CATScales = key;
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        amenitiesTruckCATScalesImage = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(amenitiesTruckCATScalesImage);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }

            //            TA HAVE SOME DIFFERENT ATTRIBUTES TOO Like These//
            //            "Amenities-Business Amazon Lockers": "no",
            if (jsonObject.has("Amenities-Business Amazon Lockers")) {
                String keyValue = jsonObject.get("Amenities-Business Amazon Lockers").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    String key = getImageInfo("Amenities-Business Amazon Lockers");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//            "Amenities-Business Check Cashing Services": "yes",
            if (jsonObject.has("Amenities-Business Check Cashing Services")) {
                String keyValue = jsonObject.get("Amenities-Business Check Cashing Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Business Check Cashing Services");
                    String key = getImageInfo("Amenities-Business Check Cashing Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //                    "Amenities-Business Permit Services": "yes",
            if (jsonObject.has("Amenities-Business Permit Services")) {
                String keyValue = jsonObject.get("Amenities-Business Permit Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Business Permit Services");
                    String key = getImageInfo("Amenities-Business Permit Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //                    "Amenities-Business TransFlo/Faxing Services": "no",
            if (jsonObject.has("Amenities-Business TransFlo/Faxing Services")) {
                String keyValue = jsonObject.get("Amenities-Business TransFlo/Faxing Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Business TransFlo/Faxing Services");
                    String key = getImageInfo("Amenities-Business TransFlo/Faxing Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//           "Amenities-Business Transflo Express Scanning":"yes",
            if (jsonObject.has("Amenities-Business Transflo Express Scanning")) {
                String keyValue = jsonObject.get("Amenities-Business Transflo Express Scanning").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Business Transflo Express Scanning");
                    String key = getImageInfo("Amenities-Business Transflo Express Scanning");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }
//////        "Amenities-Business Verizon Wireless":"no",
            if (jsonObject.has("Amenities-Business Verizon Wireless")) {
                String keyValue = jsonObject.get("Amenities-Business Verizon Wireless").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("");
                    String key = getImageInfo("Amenities-Business Verizon Wireless");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }

//        "Amenities-Business Western Union":"yes",
            if (jsonObject.has("Amenities-Business Western Union")) {
                String keyValue = jsonObject.get("Amenities-Business Western Union").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Business Western Union");
                    String key = getImageInfo("Amenities-Business Western Union");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fitness Fitness Room":"no",
            if (jsonObject.has("Amenities-Fitness Fitness Room")) {
                String keyValue = jsonObject.get("Amenities-Fitness Fitness Room").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Fitness Fitness Room");
                    String key = getImageInfo("Amenities-Fitness Fitness Room");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fitness StayFit Basketball Hoop":"no",
            if (jsonObject.has("Amenities-Fitness StayFit Basketball Hoop")) {
                String keyValue = jsonObject.get("Amenities-Fitness StayFit Basketball Hoop").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Fitness StayFit Basketball Hoop");
                    String key = getImageInfo("Amenities-Fitness StayFit Basketball Hoop");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fitness StayFit Fitness Room":"no",
            if (jsonObject.has("Amenities-Fitness StayFit Fitness Room")) {
                String keyValue = jsonObject.get("Amenities-Fitness StayFit Fitness Room").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Fitness StayFit Fitness Room");
                    String key = getImageInfo("Amenities-Fitness StayFit Fitness Room");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fitness StayFit Horseshoe Pit":"no",
            if (jsonObject.has("Amenities-Fitness StayFit Horseshoe Pit")) {
                String keyValue = jsonObject.get("Amenities-Fitness StayFit Horseshoe Pit").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Fitness StayFit Horseshoe Pit");
                    String key = getImageInfo("Amenities-Fitness StayFit Horseshoe Pit");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fitness StayFit Outdoor Fitness Room":"no",
            if (jsonObject.has("Amenities-Fitness StayFit Outdoor Fitness Room") && jsonObject.get("Amenities-Fitness StayFit Outdoor Fitness Room").equals("Y")) {
                String keyValue = jsonObject.get("Amenities-Fitness StayFit Outdoor Fitness Room").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Fitness StayFit Outdoor Fitness Room");
                    String key = getImageInfo("Amenities-Fitness StayFit Outdoor Fitness Room");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//////        "Amenities-Food Deli":"no",
            if (jsonObject.has("Amenities-Food Deli")) {
                String keyValue = jsonObject.get("Amenities-Fitness StayFit Fitness Room").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Food Deli");
                    String key = getImageInfo("Amenities-Food Deli");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//////        "Amenities-Food Dining Room is Open":"no",
            if (jsonObject.has("Amenities-Food Dining Room is Open")) {
                String keyValue = jsonObject.get("Amenities-Food Dining Room is Open").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Food Dining Room is Open");
                    String key = getImageInfo("Amenities-Food Dining Room is Open");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Food Restaurants":"Charleys",
            if (jsonObject.has("Amenities-Food Restaurants")) {
                String keyValue = jsonObject.get("Amenities-Food Restaurants").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Food Restaurants");
                    String key = getImageInfo("Amenities-Food Restaurants");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//////        "Amenities-Food Starbucks":"no",
            if (jsonObject.has("Amenities-Food Starbucks")) {
                String keyValue = jsonObject.get("Amenities-Food Starbucks").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Food Starbucks");
                    String key = getImageInfo("Amenities-Food Starbucks");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//////        "Amenities-Food Subway":"no",
            if (jsonObject.has("Amenities-Food Subway")) {
                String keyValue = jsonObject.get("Amenities-Food Subway").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//                    amenitiesTruckCATScales = jsonObject.getString("Amenities-Food Subway");
                    String key = getImageInfo("Amenities-Food Subway");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fuel Biodiesel Blend":
//        "yes",
            if (jsonObject.has("Amenities-Fuel Biodiesel Blend")) {
                String keyValue = jsonObject.get("Amenities-Fuel Biodiesel Blend").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

//amenitiesTruckCATScales = jsonObject.getString("Amenities-Fuel Biodiesel Blend");
                    String key = getImageInfo("Amenities-Fuel Biodiesel Blend");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fuel DEF - 2.5 Gallon Jugs":
//        "no",
            if (jsonObject.has("Amenities-Fuel DEF - 2.5 Gallon Jugs")) {
                String keyValue = jsonObject.get("Amenities-Fuel DEF - 2.5 Gallon Jugs").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Fuel DEF - 2.5 Gallon Jugs");
                    String key = getImageInfo("Amenities-Fuel DEF - 2.5 Gallon Jugs");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fuel Hydrogen":
//        "no",
            if (jsonObject.has("Amenities-Fuel Hydrogen")) {
                String keyValue = jsonObject.get("Amenities-Fuel Hydrogen").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Fuel Hydrogen");
                    String key = getImageInfo("Amenities-Fuel Hydrogen");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fuel Propane Fill Up Services":
//        "no",
            if (jsonObject.has("Amenities-Fuel Propane Fill Up Services")) {
                String keyValue = jsonObject.get("Amenities-Fuel Propane Fill Up Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Fuel Propane Fill Up Services");
                    String key = getImageInfo("Amenities-Fuel Propane Fill Up Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fuel Satellite Pumps":
//        "no",
            if (jsonObject.has("Amenities-Fuel Satellite Pumps")) {
                String keyValue = jsonObject.get("Amenities-Fuel Satellite Pumps").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Fuel Satellite Pumps");
                    String key = getImageInfo("Amenities-Fuel Satellite Pumps");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Fuel Tesla Charging Stations":
//        "no",
            if (jsonObject.has("Amenities-Fuel Tesla Charging Stations")) {
                String keyValue = jsonObject.get("Amenities-Fuel Tesla Charging Stations").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Fuel Tesla Charging Stations");
                    String key = getImageInfo("Amenities-Fuel Tesla Charging Stations");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//        "Amenities-Medical Drug Testing Services":
//        "no",
            if (jsonObject.has("Amenities-Medical Drug Testing Services")) {
                String keyValue = jsonObject.get("Amenities-Medical Drug Testing Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Medical Drug Testing Services");
                    String key = getImageInfo("Amenities-Medical Drug Testing Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//        "Amenities-Medical Medical Services":
//        "no",
            if (jsonObject.has("Amenities-Medical Medical Services")) {
                String keyValue = jsonObject.get("Amenities-Medical Medical Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Medical Medical Services");
                    String key = getImageInfo("Amenities-Medical Medical Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//        "Amenities-Other ATM":
//        "yes",
            if (jsonObject.has("Amenities-Other ATM")) {
                String keyValue = jsonObject.get("Amenities-Other ATM").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other ATM");
                    String key = getImageInfo("Amenities-Other ATM");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//        "Amenities-Other Barber Shop":
//        "no",
            if (jsonObject.has("Amenities-Other Barber Shop")) {
                String keyValue = jsonObject.get("Amenities-Other Barber Shop").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Barber Shop");
                    String key = getImageInfo("Amenities-Other Barber Shop");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//        "Amenities-Other Check Cashing":
//        "yes",
            if (jsonObject.has("Amenities-Other Check Cashing")) {
                String keyValue = jsonObject.get("Amenities-Other Check Cashing").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Check Cashing");
                    String key = getImageInfo("Amenities-Other Check Cashing");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }

//        "Amenities-Other Courtesy WiFi in Restaurant/Fast Food Area":
//        "no",
            if (jsonObject.has("Amenities-Other Courtesy WiFi in Restaurant/Fast Food Area")) {
                String keyValue = jsonObject.get("Amenities-Other Courtesy WiFi in Restaurant/Fast Food Area").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Courtesy WiFi in Restaurant/Fast Food Area");
                    String key = getImageInfo("Amenities-Other Courtesy WiFi in Restaurant/Fast Food Area");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//        "Amenities-Other DOT Inspections":
//        "no",
            if (jsonObject.has("Amenities-Other DOT Inspections")) {
                String keyValue = jsonObject.get("Amenities-Other DOT Inspections").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other DOT Inspections");
                    String key = getImageInfo("Amenities-Other DOT Inspections");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//        "Amenities-Other DOT Physicals":
//        "no",
            if (jsonObject.has("Amenities-Other DOT Physicals")) {
                String keyValue = jsonObject.get("Amenities-Other DOT Physicals").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other DOT Physicals");
                    String key = getImageInfo("Amenities-Other DOT Physicals");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//        "Amenities-Other Direct TV NFL Sunday Ticket":
//        "yes",
            if (jsonObject.has("Amenities-Other Direct TV NFL Sunday Ticket")) {
                String keyValue = jsonObject.get("Amenities-Other Direct TV NFL Sunday Ticket").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Direct TV NFL Sunday Ticket");
                    String key = getImageInfo("Amenities-Other Direct TV NFL Sunday Ticket");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


//////                    "Amenities-Other Dog Wash": "no",
            if (jsonObject.has("Amenities-Other Dog Wash")) {
                String keyValue = jsonObject.get("Amenities-Other Dog Wash").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Dog Wash");

                    String key = getImageInfo("Amenities-Other Dog Wash");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        //                    "Amenities-Other Driver Lounge": "yes",
            if (jsonObject.has("Amenities-Other Driver Lounge")) {
                String keyValue = jsonObject.get("Amenities-Other Driver Lounge").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Driver Lounge");
                    String key = getImageInfo("Amenities-Other Driver Lounge");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }

            }


            //                   "Amenities-Other Game Room": "yes",
            if (jsonObject.has("Amenities-Other Game Room")) {
                String keyValue = jsonObject.get("Amenities-Other Game Room").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Game Room");
                    String key = getImageInfo("Amenities-Other Game Room");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }

            }
            ////        //                   "Amenities-Other Goasis Travel Store": "no",
            if (jsonObject.has("Amenities-Other Goasis Travel Store")) {
                String keyValue = jsonObject.get("Amenities-Other Goasis Travel Store").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Goasis Travel Store");
                    String key = getImageInfo("Amenities-Other Goasis Travel Store");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }

                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }

            }


////                   "Amenities-Other Interstate Speedzone WiFi": "yes"
            if (jsonObject.has("Amenities-Other Interstate Speedzone WiFi")) {
                String keyValue = jsonObject.get("Amenities-Other Interstate Speedzone WiFi").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Interstate Speedzone WiFi");
                    String key = getImageInfo("Amenities-Other Interstate Speedzone WiFi");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }

            }


//        "Amenities-Other Laundry Facility":"yes"
            if (jsonObject.has("Amenities-Other Laundry Facility")) {
                String keyValue = jsonObject.get("Amenities-Other Laundry Facility").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Laundry Facility");
                    String key = getImageInfo("Amenities-Other Laundry Facility");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //        "Amenities-Other Lodging":"no"
            if (jsonObject.has("Amenities-Other Lodging")) {
                String keyValue = jsonObject.get("Amenities-Other Lodging").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Lodging");
                    String key = getImageInfo("Amenities-Other Lodging");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //        "Amenities-Other Ministry Services":"no"
            if (jsonObject.has("Amenities-Other Ministry Services")) {
                String keyValue = jsonObject.get("Amenities-Other Ministry Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Ministry Services");
                    String key = getImageInfo("Amenities-Other Ministry Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //        "Amenities-Other Theater Room":"no"
            if (jsonObject.has("Amenities-Other Theater Room") && jsonObject.get("Amenities-Other Theater Room").equals("Y")) {
                String keyValue = jsonObject.get("Amenities-Other Theater Room").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Theater Room");
                    String key = getImageInfo("Amenities-Other Theater Room");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //        "Amenities-Other Travel Store":"yes"
            if (jsonObject.has("Amenities-Other Travel Store") && jsonObject.get("Amenities-Other Travel Store").equals("Y")) {
                String keyValue = jsonObject.get("Amenities-Other Travel Store").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Other Travel Store");
                    String key = getImageInfo("Amenities-Other Travel Store");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //        "Amenities-Parking Handicap Parking":"yes"
            if (jsonObject.has("Amenities-Parking Handicap Parking")) {
                String keyValue = jsonObject.get("Amenities-Parking Handicap Parking").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Parking Handicap Parking");
                    String key = getImageInfo("Amenities-Parking Handicap Parking");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //        "Amenities-Parking Live Parking":"no"
            if (jsonObject.has("Amenities-Parking Live Parking")) {
                String keyValue = jsonObject.get("Amenities-Parking Live Parking").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Parking Live Parking");
                    String key = getImageInfo("Amenities-Parking Live Parking");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //        "Amenities-Parking Reserve-It Parking":"yes"
            if (jsonObject.has("Amenities-Parking Reserve-It Parking")) {
                String keyValue = jsonObject.get("Amenities-Parking Reserve-It Parking").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Parking Reserve-It Parking");
                    String key = getImageInfo("Amenities-Parking Reserve-It Parking");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Brake Svc":"no"
            if (jsonObject.has("Amenities-Service Brake Svc")) {
                String keyValue = jsonObject.get("Amenities-Service Brake Svc").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Brake Svc");
                    String key = getImageInfo("Amenities-Service Brake Svc");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service CB Shop":"no"
            if (jsonObject.has("Amenities-Service CB Shop")) {
                String keyValue = jsonObject.get("Amenities-Service CB Shop").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service CB Shop");
                    String key = getImageInfo("Amenities-Service CB Shop");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Electric Svc":"no"
            if (jsonObject.has("Amenities-Service Electric Svc")) {
                String keyValue = jsonObject.get("Amenities-Service Electric Svc").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Electric Svc");
                    String key = getImageInfo("Amenities-Service Electric Svc");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Engine Diagnostics":"no"
            if (jsonObject.has("Amenities-Service Engine Diagnostics")) {
                String keyValue = jsonObject.get("Amenities-Service Engine Diagnostics").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Engine Diagnostics");
                    String key = getImageInfo("Amenities-Service Engine Diagnostics");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }

            ////        "Amenities-Service Expert Truck Alignment":"no"
            if (jsonObject.has("Amenities-Service Expert Truck Alignment")) {
                String keyValue = jsonObject.get("Amenities-Service Expert Truck Alignment").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Expert Truck Alignment");
                    String key = getImageInfo("Amenities-Service Expert Truck Alignment");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Hub City Express":"no"
            if (jsonObject.has("Amenities-Service Hub City Express")) {
                String keyValue = jsonObject.get("Amenities-Service Hub City Express").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Hub City Express");
                    String key = getImageInfo("Amenities-Service Hub City Express");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service NG Certified Truck Service":"no"
            if (jsonObject.has("Amenities-Service NG Certified Truck Service")) {
                String keyValue = jsonObject.get("Amenities-Service NG Certified Truck Service").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service NG Certified Truck Service");
                    String key = getImageInfo("Amenities-Service NG Certified Truck Service");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service NorthStar Batteries":"no"
            if (jsonObject.has("Amenities-Service NorthStar Batteries")) {
                String keyValue = jsonObject.get("Amenities-Service NorthStar Batteries").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service NorthStar Batteries");
                    String key = getImageInfo("Amenities-Service NorthStar Batteries");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Oil Changes":"no"
            if (jsonObject.has("Amenities-Service Oil Changes")) {
                String keyValue = jsonObject.get("Amenities-Service Oil Changes").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Oil Changes");
                    String key = getImageInfo("Amenities-Service Oil Changes");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Other Lube Services":"no"
            if (jsonObject.has("Amenities-Service Other Lube Services")) {
                String keyValue = jsonObject.get("Amenities-Service Other Lube Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Other Lube Services");
                    String key = getImageInfo("Amenities-Service Other Lube Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Preventive Maintenance":"no"
            if (jsonObject.has("Amenities-Service Preventive Maintenance")) {
                String keyValue = jsonObject.get("Amenities-Service Preventive Maintenance").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Preventive Maintenance");
                    String key = getImageInfo("Amenities-Service Preventive Maintenance");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


////        "Amenities-Service Refrigerated Trailer Maintenance":"no"
            if (jsonObject.has("Amenities-Service Refrigerated Trailer Maintenance")) {
                String keyValue = jsonObject.get("Amenities-Service Refrigerated Trailer Maintenance").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Refrigerated Trailer Maintenance");
                    String key = getImageInfo("Amenities-Service Refrigerated Trailer Maintenance");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Roadside Breakdown Service":"no"
            if (jsonObject.has("Amenities-Service Roadside Breakdown Service")) {
                String keyValue = jsonObject.get("Amenities-Service Roadside Breakdown Service").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Roadside Breakdown Service");
                    String key = getImageInfo("Amenities-Service Roadside Breakdown Service");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Standard Services":"no"
            if (jsonObject.has("Amenities-Service Standard Services")) {
                String keyValue = jsonObject.get("Amenities-Service Standard Services").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Standard Services");
                    String key = getImageInfo("Amenities-Service Standard Services");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Tire Chains (Seasonal)":"no"
            if (jsonObject.has("Amenities-Service Tire Chains (Seasonal)")) {
                String keyValue = jsonObject.get("Amenities-Service Tire Chains (Seasonal)").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Tire Chains (Seasonal)");
                    String key = getImageInfo("Amenities-Service Tire Chains (Seasonal)");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Winterized Diesel Nov-March (Any Temperature)":"no"
            if (jsonObject.has("Amenities-Service Winterized Diesel Nov-March (Any Temperature)")) {
                String keyValue = jsonObject.get("Amenities-Service Winterized Diesel Nov-March (Any Temperature)").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Winterized Diesel Nov-March (Any Temperature)");
                    String key = getImageInfo("Amenities-Service Winterized Diesel Nov-March (Any Temperature)");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service Winterized Diesel Nov-March 30 degrees or below":"no"
            if (jsonObject.has("Amenities-Service Winterized Diesel Nov-March 30 degrees or below")) {
                String keyValue = jsonObject.get("Amenities-Service Winterized Diesel Nov-March 30 degrees or below").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service Winterized Diesel Nov-March 30 degrees or below");
                    String key = getImageInfo("Amenities-Service Winterized Diesel Nov-March 30 degrees or below");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            ////        "Amenities-Service eShop":"no"
            if (jsonObject.has("Amenities-Service eShop")) {
                String keyValue = jsonObject.get("Amenities-Service eShop").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Service eShop");
                    String key = getImageInfo("Amenities-Service eShop");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            //        "Amenities-Truck Truck Wash":"no"
            if (jsonObject.has("Amenities-Truck Truck Wash")) {
                String keyValue = jsonObject.get("Amenities-Truck Truck Wash").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Amenities-Truck Truck Wash");
                    String key = getImageInfo("Amenities-Truck Truck Wash");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }

            }


//        REST AREA
//        HAVE SOME
//        DIFFERENT ATTRIBUTES
//        TOO Like
//        These
//        "Rest Area Cigarette Ash Dump": "no",
//        "Rest Area Restrooms": "yes",
//        "Rest Area Road": "AK-1",
//        "Rest Area Section": "Alaska State Route AK-1 Rest Areas"

            if (jsonObject.has("Rest Area Cigarette Ash Dump")) {
                String keyValue = jsonObject.get("Rest Area Cigarette Ash Dump").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Rest Area Cigarette Ash Dump");
                    String key = getImageInfoRestArea("Rest Area Cigarette Ash Dump");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Rest Area Restrooms")) {
                String keyValue = jsonObject.get("Rest Area Restrooms").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Rest Area Restrooms");
                    String key = getImageInfoRestArea("Rest Area Restrooms");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Rest Area Road")) {
                String keyValue = jsonObject.get("Rest Area Road").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Rest Area Road");
                    String key = getImageInfoRestArea("Rest Area Road");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
//                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }

            }


            if (jsonObject.has("Rest Area Section")) {
                String keyValue = jsonObject.get("Rest Area Section").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    //amenitiesTruckCATScales = jsonObject.getString("Rest Area Section");
                    String key = getImageInfoRestArea("Rest Area Section");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
//                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Rest Area Direction")) {
                String keyValue = jsonObject.get("Rest Area Direction").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    String key = getImageInfoRestArea("Rest Area Direction");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Rest Area Food Vending")) {
                String keyValue = jsonObject.getString("Rest Area Food Vending");

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    String key = getImageInfoRestArea("Rest Area Food Vending");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Rest Area Handicap Access")) {
                String keyValue = jsonObject.get("Rest Area Handicap Access").toString();

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    String key = getImageInfoRestArea("Rest Area Handicap Access");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Rest Area Pet Area")) {
                String keyValue = jsonObject.getString("Rest Area Pet Area");

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    String key = getImageInfoRestArea("Rest Area Pet Area");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Rest Area RV Dump")) {
                String keyValue = jsonObject.getString("Rest Area RV Dump");

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    String key = getImageInfoRestArea("Rest Area RV Dump");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }


            if (jsonObject.has("Rest Area Water")) {
                String keyValue = jsonObject.getString("Rest Area Water");

                if ((!keyValue.isEmpty()) && (!keyValue.equalsIgnoreCase("n"))
                        && (!keyValue.equalsIgnoreCase("no"))) {

                    String key = getImageInfoRestArea("Rest Area Water");
                    String iconId = "";
                    if (!key.isEmpty() && amenitiesJSONObject.has(key)) {
                        iconId = amenitiesJSONObject.getString(key);
                    }
                    AmenitiesGenericModel amenitiesGenericModel = new AmenitiesGenericModel();
                    amenitiesGenericModel.setName(key);
                    amenitiesGenericModel.setImageUrl(iconId);
                    amenitiesHeaderDataList.add(amenitiesGenericModel);
                }
            }

            Log.d(TAG, "sortAmenity: storeClickedAssetRecordUpdate: addAll in loveAmenities: before: ");
            loveAmenitiesList.addAll(amenitiesHeaderDataList);
            Log.d(TAG, "sortAmenity: storeClickedAssetRecordUpdate: addAll in loveAmenities: after: ");

            if (jsonObject.has("RecordId") && jsonObject.getString("RecordId").equals("Y")) {
                amenitiesRecordId = jsonObject.getString("RecordId");
            }

            amenetiesModel = new AmenitiesModel();
            amenetiesModel.setAmenitiesBusinessTransflo(amenitiesBusinessTransflo);
            amenetiesModel.setTransflo(transflo);
            amenetiesModel.setAmenitiesBusinessTransfloImage(amenitiesBusinessTransfloImage);
            amenetiesModel.setAmenitiesFoodRestaurants(amenitiesFoodRestaurants);
            amenetiesModel.setRestaurants(restaurants);
            amenetiesModel.setAmenitiesFoodRestaurantsImage(amenitiesFoodRestaurantsImage);
            amenetiesModel.setAmenitiesFuelBulkDEF(amenitiesFuelBulkDEF);
            amenetiesModel.setBulkDEF(bulkDEF);
            amenetiesModel.setAmenitiesFuelBulkDEFImage(amenitiesFuelBulkDEFImage);
            amenetiesModel.setAmenitiesFuelBulkPropane(amenitiesFuelBulkPropane);
            amenetiesModel.setBulkPropane(bulkPropane);
            amenetiesModel.setAmenitiesFuelBulkPropaneImage(amenitiesFuelBulkPropaneImage);
            amenetiesModel.setAmenitiesFuelEVLevel2(amenitiesFuelEVLevel2);
            amenetiesModel.setEVLevel2(EVLevel2);
            amenetiesModel.setAmenitiesFuelEVLevel2Image(amenitiesFuelEVLevel2Image);
            amenetiesModel.setAmenitiesFuelEVLevel3DCFC(amenitiesFuelEVLevel3DCFC);
            amenetiesModel.setEVLevel3DCFC(EVLevel3DCFC);
            amenetiesModel.setAmenitiesFuelEVLevel3DCFCImage(amenitiesFuelEVLevel3DCFCImage);
            amenetiesModel.setAmenitiesFuelEVTeslaLevel2(amenitiesFuelEVTeslaLevel2);
            amenetiesModel.setEVTeslaLevel2(EVTeslaLevel2);
            amenetiesModel.setAmenitiesFuelEVTeslaLevel2Image(amenitiesFuelEVTeslaLevel2Image);
            amenetiesModel.setAmenitiesFuelEVTeslaLevel3DCFC(amenitiesFuelEVTeslaLevel3DCFC);
            amenetiesModel.setEVTeslaLevel3DCFC(EVTeslaLevel3DCFC);
            amenetiesModel.setAmenitiesFuelEVTeslaLevel3DCFCImage(amenitiesFuelEVTeslaLevel3DCFCImage);
            amenetiesModel.setAmenitiesFuelFastFillCNGAuto(amenitiesFuelFastFillCNGAuto);
            amenetiesModel.setFastFillCNGAuto(fastFillCNGAuto);
            amenetiesModel.setAmenitiesFuelFastFillCNGAutoImage(amenitiesFuelFastFillCNGAutoImage);
            amenetiesModel.setAmenitiesFuelFastFillCNGClass8(amenitiesFuelFastFillCNGClass8);
            amenetiesModel.setFastFillCNGClass8(fastFillCNGClass8);
            amenetiesModel.setAmenitiesFuelFastFillCNGClass8Image(amenitiesFuelFastFillCNGClass8Image);
            amenetiesModel.setAmenitiesFuelPropaneTankExchange(amenitiesFuelPropaneTankExchange);
            amenetiesModel.setPropaneTankExchange(propaneTankExchange);
            amenetiesModel.setAmenitiesFuelPropaneTankExchangeImage(amenitiesFuelPropaneTankExchangeImage);
            amenetiesModel.setAmenitiesFuelRFIDPumpStart(amenitiesFuelRFIDPumpStart);
            amenetiesModel.setRFIDPumpStart(RFIDPumpStart);
            amenetiesModel.setAmenitiesFuelRFIDPumpStartImage(amenitiesFuelRFIDPumpStartImage);
            amenetiesModel.setAmenitiesOtherATM(amenitiesOtherATM);
            amenetiesModel.setATM(ATM);
            amenetiesModel.setAmenitiesOtherATMImage(amenitiesOtherATMImage);
            amenetiesModel.setAmenitiesOtherDogPark(amenitiesOtherDogPark);
            amenetiesModel.setDogPark(dogPark);
            amenetiesModel.setAmenitiesOtherDogParkImage(amenitiesOtherDogParkImage);
            amenetiesModel.setAmenitiesOtherLaundryFacility(amenitiesOtherLaundryFacility);
            amenetiesModel.setLaundryFacility(laundryFacility);
            amenetiesModel.setAmenitiesOtherLaundryFacilityImage(amenitiesOtherLaundryFacilityImage);
            amenetiesModel.setAmenitiesOtherPrivateShowers(amenitiesOtherPrivateShowers);
            amenetiesModel.setPrivateShowers(privateShowers);
            amenetiesModel.setAmenitiesOtherPrivateShowersImage(amenitiesOtherPrivateShowersImage);
            amenetiesModel.setAmenitiesOtherVideoGaming(amenitiesOtherVideoGaming);
            amenetiesModel.setVideoGaming(videoGaming);
            amenetiesModel.setAmenitiesOtherVideoGamingImage(amenitiesOtherVideoGamingImage);
            amenetiesModel.setAmenitiesOtherWiFiBasic(amenitiesOtherWiFiBasic);
            amenetiesModel.setWiFiBasic(WiFiBasic);
            amenetiesModel.setAmenitiesOtherWiFiBasicImage(amenitiesOtherWiFiBasicImage);
            amenetiesModel.setAmenitiesOtherWiFiPremium(amenitiesOtherWiFiPremium);
            amenetiesModel.setWiFiPremium(WiFiPremium);
            amenetiesModel.setAmenitiesOtherWiFiPremiumImage(amenitiesOtherWiFiPremiumImage);
            amenetiesModel.setAmenitiesParkingTruckParking(amenitiesParkingTruckParking);
            amenetiesModel.setTruckParking(truckParking);
            amenetiesModel.setAmenitiesParkingTruckParkingImage(amenitiesParkingTruckParkingImage);
            amenetiesModel.setAmenitiesRVRVDumpService(amenitiesRVRVDumpService);
            amenetiesModel.setRVDumpService(RVDumpService);
            amenetiesModel.setAmenitiesRVRVDumpServiceImage(amenitiesRVRVDumpServiceImage);
            amenetiesModel.setAmenitiesRVRVFriendlyParking(amenitiesRVRVFriendlyParking);
            amenetiesModel.setRVFriendlyParking(RVFriendlyParking);
            amenetiesModel.setAmenitiesRVRVFriendlyParkingImage(amenitiesRVRVFriendlyParkingImage);
            amenetiesModel.setAmenitiesRVRVHookup(amenitiesRVRVHookup);
            amenetiesModel.setRVHookup(RVHookup);
            amenetiesModel.setAmenitiesRVRVHookupImage(amenitiesRVRVHookupImage);
            amenetiesModel.setAmenitiesServiceCommercialTruckOilChange(amenitiesServiceCommercialTruckOilChange);
            amenetiesModel.setCommercialTruckOilChange(commercialTruckOilChange);
            amenetiesModel.setAmenitiesServiceCommercialTruckOilChangeImage(amenitiesServiceCommercialTruckOilChangeImage);
            amenetiesModel.setAmenitiesServiceLightMechanical(amenitiesServiceLightMechanical);
            amenetiesModel.setLightMechanical(lightMechanical);
            amenetiesModel.setAmenitiesServiceLightMechanicalImage(amenitiesServiceLightMechanicalImage);
            amenetiesModel.setAmenitiesServiceSpeedcoOnSite(amenitiesServiceSpeedcoOnSite);
            amenetiesModel.setSpeedCoOnSite(speedCoOnSite);
            amenetiesModel.setAmenitiesServiceSpeedcoOnSiteImage(amenitiesServiceSpeedcoOnSiteImage);
            amenetiesModel.setAmenitiesServiceTireServices(amenitiesServiceTireServices);
            amenetiesModel.setTireServices(tireServices);
            amenetiesModel.setAmenitiesServiceTireServicesImage(amenitiesServiceTireServicesImage);
            amenetiesModel.setAmenitiesServiceTirePassInLane(amenitiesServiceTirePassInLane);
            amenetiesModel.setTirePassInLane(tirePassInLane);
            amenetiesModel.setAmenitiesServiceTirePassInLaneImage(amenitiesServiceTirePassInLaneImage);
            amenetiesModel.setAmenitiesServiceTirePassInServiceCenter(amenitiesServiceTirePassInServiceCenter);
            amenetiesModel.setTirePassInServiceCenter(tirePassInServiceCenter);
            amenetiesModel.setAmenitiesServiceTirePassInServiceCenterImage(amenitiesServiceTirePassInServiceCenterImage);
            amenetiesModel.setAmenitiesServiceTirePassMobile(amenitiesServiceTirePassMobile);
            amenetiesModel.setTirePassMobile(tirePassMobile);
            amenetiesModel.setAmenitiesServiceTirePassMobileImage(amenitiesServiceTirePassMobileImage);
            amenetiesModel.setAmenitiesTruckCATScales(amenitiesTruckCATScales);
            amenetiesModel.setCATScales(CATScales);
            amenetiesModel.setAmenitiesTruckCATScalesImage(amenitiesTruckCATScalesImage);
            amenetiesModel.setRecordId(amenitiesRecordId);
        } catch (
                JSONException jsonException) {
            Log.d(TAG, "saveMatchedRecord: jsonException: " + jsonException);
        }
    }

    String getImageInfo(String key) {
        String[] splitKey = key.split(" ", 2);
        Log.d(TAG, "getImageInfo: splitKey: " + splitKey.length);
        for (int i = 0; i < splitKey.length; i++) {
            Log.d(TAG, "getImageInfo: splitKey: value: " + splitKey[i]);
        }
        String imageKey = "";
        if (splitKey.length > 1) {
            imageKey = splitKey[1].trim();
        }
        return imageKey;
    }

    String getImageInfoRestArea(String key) {
        key = key.replace("Rest Area ", " ").trim();
        return key;
    }

    void setExpandableAdapterToAmenitiesListView() {
        Log.d(TAG, "amenity: setExpandableAdapterToAmenitiesListView: headerDataList: " + amenitiesHeaderDataList + " size: " + amenitiesHeaderDataList.size());
        Log.d(TAG, "amenity: setExpandableAdapterToAmenitiesListView: amenitiesChildDataList: " + amenitiesChildDataList + " size: " + amenitiesChildDataList.size());
        amenitiesExpandableAdapter = new AmenetiesExpandableAdapter(getContext(), amenitiesHeaderDataList, amenitiesChildDataList);
        amenitiesListView.setAdapter(amenitiesExpandableAdapter);
    }


    //    Nov 15, 2022  -   We are hiding search layout when IC is not available
    void setSearchVisibility(boolean show) {
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (show) {
                        if (isTablet) {
                            mView.findViewById(R.id.btn_search_CL).setVisibility(VISIBLE);
                            mView.findViewById(R.id.search_container).setVisibility(VISIBLE);
                        } else {
                            mView.findViewById(R.id.btn_search).setVisibility(VISIBLE);
                        }
                    } else {
                        if (isTablet) {
                            mView.findViewById(R.id.btn_search_CL).setVisibility(GONE);
                            mView.findViewById(R.id.search_container).setVisibility(GONE);
                        } else {
                            mView.findViewById(R.id.btn_search).setVisibility(GONE);
                        }
                    }
                }
            });
        }
    }

    void sortAmenity() {
        Log.d(TAG, "sortAmenity: ");
        sortedLoveAmenitiesList.clear();
        sortAmenitiesInASCOrder();
        populateInitialValuesToSortingAmenitiesArray(false);
        populateNewArrayRemainingValues(false);
        showNewList();
    }

    void sortAmenitiesInASCOrder() {
        Log.d(TAG, "sortAmenity: sortAmenitiesInASCOrder: loveAmenitiesList: size: " + loveAmenitiesList.size());
        Collections.sort(loveAmenitiesList, new Comparator<AmenitiesGenericModel>() {
            @Override
            public int compare(AmenitiesGenericModel o1, AmenitiesGenericModel o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
    }

    void populateInitialValuesToSortingAmenitiesArray(boolean isRestArea) {
        Log.d(TAG, "sortAmenity: populateInitialValuesToSortingAmenitiesArray: isRestArea: " + isRestArea);
        if (isRestArea) {

            for (int i = 0; i < loveAmenitiesList.size(); i++) {
//                Order should be Restrooms - Water - Food Vending
                if (loveAmenitiesList.get(i).getName().contains("Restrooms") && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
                    sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
                    continue;

                } else if (loveAmenitiesList.get(i).getName().contains("Water") && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
                    sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
                    continue;

                } else if (loveAmenitiesList.get(i).getName().contains("Food Vending") && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
                    sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
                    continue;
                }
            }
        } else {
//            for (int i = 0; i < loveAmenitiesList.size(); i++) {
//                if (loveAmenitiesList.get(i).getName().contains("Restaurants") && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
//                    sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
//                    continue;
//
//                } else if (loveAmenitiesList.get(i).getName().contains("CAT Scales") && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
//                    sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
//                    continue;
//
//                } else if (loveAmenitiesList.get(i).getName().contains("Private Showers") && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
//                    sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
//                    continue;
//                }

            checkIsKeyExisted("Restaurants");   // If this value is not added it will also add it
            checkIsKeyExisted("CAT Scales");
            checkIsKeyExisted("Private Showers");
        }
    }

    void populateNewArrayRemainingValues(boolean isRestArea) {
        Log.d(TAG, "sortAmenity: populateNewArrayRemainingValues: ");
        if (isRestArea) {
            for (int i = 0; i < loveAmenitiesList.size(); i++) {
                Log.d(TAG, "onClick: showList: " + loveAmenitiesList.get(i).getName());

                if (!loveAmenitiesList.get(i).getName().contains("Restrooms")
                        && !loveAmenitiesList.get(i).getName().contains("Water")
                        && !loveAmenitiesList.get(i).getName().contains("Food Vending")
                        && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
                    sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
                }
            }

        } else {
            for (int i = 0; i < loveAmenitiesList.size(); i++) {
                Log.d(TAG, "onClick: showList: " + loveAmenitiesList.get(i).getName());

                if (!loveAmenitiesList.get(i).getName().contains("Restaurants")
                        && !loveAmenitiesList.get(i).getName().contains("CAT Scales")
                        && !loveAmenitiesList.get(i).getName().contains("Private Showers")
                        && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
                    sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
                }
            }
        }
    }

    void checkIsKeyExisted(String value) {
        for (int i = 0; i < loveAmenitiesList.size(); i++) {
            if (loveAmenitiesList.get(i).getName().contains(value) && !checkIsAlreadyAdded(loveAmenitiesList.get(i).getName())) {
                sortedLoveAmenitiesList.add(loveAmenitiesList.get(i));
            }
        }
    }

    boolean checkIsAlreadyAdded(String value) {
        for (int i = 0; i < sortedLoveAmenitiesList.size(); i++) {
            if (sortedLoveAmenitiesList.get(i).getName().equalsIgnoreCase(value)) {
                return true;
            }
        }
        return false;
    }


    void showNewList() {

        Log.d(TAG, "sortAmenity: showNewList: sortedLoveAmenitiesList: size: " + sortedLoveAmenitiesList.size());
        for (int i = 0; i < sortedLoveAmenitiesList.size(); i++) {
            Log.d(TAG, "sortAmenity: onClick: index: " + i + " value: " + sortedLoveAmenitiesList.get(i).getName());
        }

//        if (sortedLoveAmenitiesList.size() >= 0) {
//            int iconId = getResources().getIdentifier(sortedLoveAmenitiesList.get(0).getImageUrl().toLowerCase(), "drawable", getContext().getPackageName());
//            setIcon(mView.findViewById(R.id.first_icon_for_love), iconId);
//        }
//        if (sortedLoveAmenitiesList.size() >= 1) {
//            int iconId = getResources().getIdentifier(sortedLoveAmenitiesList.get(1).getImageUrl().toLowerCase(), "drawable", getContext().getPackageName());
//            setIcon(mView.findViewById(R.id.second_icon_for_love), iconId);
//        }
//        if (sortedLoveAmenitiesList.size() >= 2) {
//            int iconId = getResources().getIdentifier(sortedLoveAmenitiesList.get(2).getImageUrl().toLowerCase(), "drawable", getContext().getPackageName());
//            setIcon(mView.findViewById(R.id.third_icon_for_love), iconId);
//        }
    }

    private void updateDistanceValuesOffline() {
        isTablet = getResources().getBoolean(R.bool.isTablet);
        Log.d(TAG, "updateDistanceValuesOffline: isTablet: " + isTablet + " isICAvailable: " + isICAvailable);
//        Log.d(TAG, "updateDistance: ForDistance: tablet: updateGaugesParameters: ");
        if (mActivity != null) {
            ((MainMenuActivity) getActivity()).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (isTablet) {
                        if (!isICAvailable) {
//                            previousCodeToUpdateStationDistances();
                            setStraightLineDistance();
                        }
                    }
                }
            });
        }
    }

    void setStraightLineDistance() {
        Log.d(TAG, "setStraightLineDistance: ");

        String straightLineLoveDistance = rules.getNearestLoveDistance() + " mi.";
        String straightLinePilotDistance = rules.getNearestPilotDistance() + " mi.";
        String straightLineTaDistance = rules.getNearestTADistance() + " mi.";
        String straightLineRestAreaDistance = rules.getNearestRestAreaDistance() + " mi.";

        Log.d(TAG, "setStraightLineDistance: love: " + straightLineLoveDistance);
        Log.d(TAG, "setStraightLineDistance: pilot: " + straightLinePilotDistance);
        Log.d(TAG, "setStraightLineDistance: ta: " + straightLineTaDistance);
        Log.d(TAG, "setStraightLineDistance: restArea: " + straightLineRestAreaDistance);

        gaugesLoveStationDistance.setText(straightLineLoveDistance);
        gaugesPilotStationDistance.setText(straightLinePilotDistance);
        gaugesTAStationDistance.setText(straightLineTaDistance);
        gaugesRestAreaStationDistance.setText(straightLineRestAreaDistance);


//        ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).setText(rules.getNearestPilotDistance() + " mi.");
//        ((TextView) mView.findViewById(R.id.gauges_love_station_distance)).setText(rules.getNearestLoveDistance() + " mi.");
//        ((TextView) mView.findViewById(R.id.gauges_rest_area_station_distance)).setText(rules.getNearestRestAreaDistance() + " mi.");
//        ((TextView) mView.findViewById(R.id.gauges_ta_station_distance)).setText(rules.getNearestTADistance() + " mi.");

//        TODO I dont know why but even crash don't occurr
//        Log.d(TAG, "updateDistanceValuesOffline: setStraightLineDistance: pilotStation: TextView: " + ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)));
//        Log.d(TAG, "updateDistanceValuesOffline: setStraightLineDistance: pilotStation: without: " + mView.findViewById(R.id.gauges_pilot_station_distance));
//        Log.d(TAG, "updateDistanceValuesOffline: setStraightLineDistance: pilotStation: gaugesPilotStationDistance: " + gaugesPilotStationDistance);
//        Log.d(TAG, "updateDistanceValuesOffline: setStraightLineDistance: pilotStation: straightLinePilotDistance: " + straightLinePilotDistance);
//        Log.d(TAG, "updateDistanceValuesOffline: setStraightLineDistance: pilotStation: straightLinePilotDistance: " + straightLinePilotDistance + " gaugesPilotStationDistance: " + gaugesPilotStationDistance);


//        ((TextView) mView.findViewById(R.id.gauges_love_station_distance)).setText(straightLineLoveDistance);
//        ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).setText(straightLinePilotDistance);
//        ((TextView) mView.findViewById(R.id.gauges_ta_station_distance)).setText(straightLineTaDistance);
//        ((TextView) mView.findViewById(R.id.gauges_rest_area_station_distance)).setText(straightLineRestAreaDistance);
//        Log.d(TAG, "updateDistanceValuesOffline: setStraightLineDistance: getText: " + gaugesPilotStationDistance);
//        gaugesPilotStationDistance.setText(straightLinePilotDistance);
//        Log.d(TAG, "updateDistanceValuesOffline: setStraightLineDistance: getText: " + gaugesPilotStationDistance.getText());
    }

    void previousCodeToUpdateStationDistances() {
//                            Log.d(TAG, "updateDistanceValuesOffline: run: love: " + rules.getNearestLoveDistance() + " mi.");
//                            Log.d(TAG, "updateDistanceValuesOffline: run: pilot: " + rules.getNearestPilotDistance() + " mi.");
//                            Log.d(TAG, "updateDistanceValuesOffline: run: ta: " + rules.getNearestTADistance() + " mi.");
//                            Log.d(TAG, "updateDistanceValuesOffline: run: rest: " + rules.getNearestRestAreaDistance() + " mi.");
//
//                            String pilotDistance = rules.getNearestPilotDistance() + " mi.";
//                            TextView pilotStation = mView.findViewById(R.id.gauges_pilot_station_distance);
//                            pilotStation.setText(pilotDistance);
////                            ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).setText(rules.getNearestPilotDistance() + " mi.");
//                            Log.d(TAG, "updateDistanceValuesOffline: run: get: text: " + ((TextView) mView.findViewById(R.id.gauges_pilot_station_distance)).getText());
////                            gaugesPilotStationDistance.setText(rules.getNearestPilotDistance() + " mi.");
//                            Log.d(TAG, "updateDistanceValuesOffline: run: pilotStation: text: " + pilotStation.getText());
//
//                            ((TextView) mView.findViewById(R.id.gauges_love_station_distance)).setText(rules.getNearestLoveDistance() + " mi.");
//                            ((TextView) mView.findViewById(R.id.gauges_rest_area_station_distance)).setText(rules.getNearestRestAreaDistance() + " mi.");
//                            ((TextView) mView.findViewById(R.id.gauges_ta_station_distance)).setText(rules.getNearestTADistance() + " mi.");
    }

    private void unzip(String zipFilePath, String unzipAtLocation) throws Exception {
        File archive = new File(zipFilePath);
        try {
            ZipFile zipfile = new ZipFile(archive);
            for (Enumeration e = zipfile.entries(); e.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) e.nextElement();
                unzipEntry(zipfile, entry, unzipAtLocation);
            }
        } catch (Exception e) {
            Log.e("Unzip zip", "Unzip exception", e);
        }
    }

    private void unzipEntry(ZipFile zipfile, ZipEntry entry, String outputDir) throws IOException {
        if (entry.isDirectory()) {
            createDir(new File(outputDir, entry.getName()));
            return;
        }

        File outputFile = new File(outputDir, entry.getName());
        if (!outputFile.getParentFile().exists()) {
            createDir(outputFile.getParentFile());
        }

        Log.v("ZIP E", "Extracting: " + entry);
        InputStream zin = zipfile.getInputStream(entry);
        BufferedInputStream inputStream = new BufferedInputStream(zin);
        BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));

        try {
            try {
                for (int c = inputStream.read(); c != -1; c = inputStream.read()) {
                    outputStream.write(c);
                }
            } finally {
                outputStream.close();
            }
        } finally {
            outputStream.close();
            inputStream.close();
        }
    }

    private void createDir(File dir) {
        if (dir.exists()) {
            return;
        }
        Log.v("ZIP E", "Creating dir " + dir.getName());
        if (!dir.mkdirs()) {

            throw new RuntimeException("Can not create dir " + dir);
        }
    }

    void enableMic() {
        Log.d(TAG, "enableMic: ");
        if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.RECORD_AUDIO}, SPEECH_PERMISSION_CODE);
        } else {
            if (speechRecognizer == null) {
                initializeSpeechRecognizer();
            }
//            Dev 30, 2022  -   disabled mic if already enabled
            disableMic();

            final Intent recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            speechRecognizer.startListening(recognizerIntent);

            setSpeechRecognizerListener();
        }
    }

    void disableMic() {
        Log.d(TAG, "disableMic: speech: speechRecognizer: " + speechRecognizer);
        if (speechRecognizer == null)
            return;
        speechRecognizer.stopListening();
    }

    void initializeSpeechRecognizer() {
        Log.d(TAG, "initializeSpeechRecognizer: speech: ");
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(getContext());
    }

    void setSpeechRecognizerListener() {
        Log.d(TAG, "setSpeechRecognizerListener: speech: ");

        speechRecognizer.setRecognitionListener(new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                Log.d(TAG, "speech: onReadyForSpeech: ");
            }

            @Override
            public void onBeginningOfSpeech() {
                Log.d(TAG, "speech: onBeginningOfSpeech: ");
            }

            @Override
            public void onRmsChanged(float rmsdB) {
                Log.d(TAG, "speech: onRmsChanged: ");
            }

            @Override
            public void onBufferReceived(byte[] buffer) {
                Log.d(TAG, "speech: onBufferReceived: ");
            }

            @Override
            public void onEndOfSpeech() {
                Log.d(TAG, "speech: onEndOfSpeech: ");
            }

            @Override
            public void onError(int error) {
                Log.d(TAG, "speech: onError: error: " + error);
            }

            @Override
            public void onResults(Bundle results) {
                Log.d(TAG, "onResults: speech: ");
                ArrayList<String> data = results.getStringArrayList(speechRecognizer.RESULTS_RECOGNITION);
                String speechString = data.get(0);
                Log.d(TAG, "onResults: speech: speechString: " + speechString);

//                        <item>What time is it?</item>
//        <item>What is the time now?</item>
//        <item>Give me the time?</item>

                if (speechString.contains("what time is it") || speechString.contains("what is the time now") ||
                        speechString.contains("give me the time")) {
                    String timeInInstructions = "The time is " + displayedCurrentTime.getText();
                    Log.d(TAG, "onResults: speech: timeInInstructions: " + timeInInstructions);
                    textToSpeech.speak(timeInInstructions);
                }

                isSpeechEnabled = false;
                speechInstructionIcon.setImageDrawable(getResources().getDrawable(R.drawable.ic_baseline_mic_24));
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                Log.d(TAG, "onPartialResults: speech: ");
            }

            @Override
            public void onEvent(int eventType, Bundle params) {
                Log.d(TAG, "onEvent: speech: ");
            }
        });
    }

//    private void muteVoice() {
//        Log.d(TAG, "handleVoice: muteVoice: stop test to speech: ");
//        if (tts != null && tts.isSpeaking()) {
//            tts.stop();
//        }
//    }
}