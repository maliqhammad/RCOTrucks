package com.rco.rcotrucks.activities.drive.gauges;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.location.Location;
import android.opengl.Visibility;
import android.os.AsyncTask;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.SphericalUtil;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.DialogManager;
import com.rco.rcotrucks.activities.dashboard.GaugesHandler;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.MapAsset;
import com.rco.rcotrucks.businesslogic.rms.RestArea;
import com.rco.rcotrucks.services.ServiceToCalculateDistance;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;
import com.rco.rcotrucks.utils.route.AbstractRouting;
import com.rco.rcotrucks.utils.route.Route;
import com.rco.rcotrucks.utils.route.RouteException;
import com.rco.rcotrucks.utils.route.Routing;
import com.rco.rcotrucks.utils.route.RoutingListener;

import java.util.Date;
import java.util.List;

public class GaugesEventView extends ConstraintLayout {

    private static final String TAG = GaugesEventView.class.getSimpleName();
    //    private static final String TAG = "GaugesEventView gauges";
    private final static int STATION_UPDATE_DISTANCE = 1610; // Update Station for each 1miles = 1610 meters
    protected BusinessRules rules;
//    protected BusinessRules rules = BusinessRules.instance();

    private TextView txtTa, txtPilot, txtRest, txtLove;
    private TextView milesTv;
    private ProgressEventView driveProgressView, cycleProgressView, shiftProgressView, breakProgressView;

    private ConstraintLayout breaksView;
    private TextView breakTime;
    private Button btBreakEvent;

    private GaugesHandler gaugesHandler;
    private CurrentEventTime mCurrentEventTime;

    private OnClickListener finishBreak;
    private Location myLastSavedLocation;
    Thread threadToCalculateDistance;

    private long drivingTimeInMinutes = 0, cycleTimeInMinutes = 0, shiftTimeInMinutes = 0, breakTimeInMinutes = -1;
    private static Date lastestTimeNearestLocationUpdated = null;
    private static String nearestPilot = null;
    private static String nearestLove = null;
    private static String nearestRest = null;
    private static String nearestTa = null;

    private static long milesToday, drivingSecs, breakSecs, shiftSecs, cycleSecs, drivingHoursLeft, breakHoursLeft, shiftHoursLeft, cycleHoursLeft, drivingHoursProgress,
            breakHoursProgress, shiftHoursProgress, cycleHoursProgress;
    Activity mActivity;
    GaugesEventView.MapAutoUpdateTask mapAutoUpdateTask;


    public GaugesEventView(Context context) {
        super(context);
        rules = BusinessRules.instance();
        Log.d(TAG, "GaugesEventView: Context context: ");
    }

    public GaugesEventView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        rules = BusinessRules.instance();
        Log.d(TAG, "GaugesEventView: Context context, @Nullable AttributeSet attrs");
        initView(context, attrs);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        Log.d(TAG, "onDetachedFromWindow: ");
        if (gaugesHandler != null)
            gaugesHandler.stopRepeatingGaugeTask();
    }

    private void initView(Context context, AttributeSet attrs) {
        Log.d(TAG, "DashboardFrag: initView: ");
//        try {
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.GaugesLayout, 0, 0);
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        String layout = a.getString(R.styleable.GaugesLayout_layoutType);
        View container;

        if (layout.equals("dashboard"))
            container = inflater.inflate(R.layout.layout_gauges_dashboard, this, true);
        else
            container = inflater.inflate(R.layout.layout_gauges_drive, this, true);

        txtTa = container.findViewById(R.id.txt_ta);
        txtPilot = container.findViewById(R.id.txt_pilot);
        txtRest = container.findViewById(R.id.txt_rest);
        txtLove = container.findViewById(R.id.txt_love);
        milesTv = container.findViewById(R.id.miles_tv);

        driveProgressView = container.findViewById(R.id.drives_view);
        cycleProgressView = container.findViewById(R.id.cycle_view);
        shiftProgressView = container.findViewById(R.id.shift_view);
        breakProgressView = container.findViewById(R.id.break_view);

        breaksView = container.findViewById(R.id.breaks_view);
        breakTime = container.findViewById(R.id.break_time);
        btBreakEvent = container.findViewById(R.id.bt_break_event);

        btBreakEvent.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    breaksView.setVisibility(View.GONE);

                    if (finishBreak != null)
                        finishBreak.onClick(v);
                } catch (Throwable throwable) {
                }
            }
        });


        initEventProgressView();
        reloadGaugeHandler();
        Log.d(TAG, "initView: handleEventProgress: getMaxCycleHours: " + gaugesHandler.getMaxCycleHours());
        cycleProgressView.setLimit(gaugesHandler.getMaxCycleHours());


        stopThreadWhichCalculateDistance();

        if (mapAutoUpdateTask == null) {
            mapAutoUpdateTask = new GaugesEventView.MapAutoUpdateTask();
            mapAutoUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }

    }

    public void setEventTimeListener(CurrentEventTime currentEventTime) {
        Log.d(TAG, "setEventTimeListener: ");
        mCurrentEventTime = currentEventTime;
    }

    public void setFinishBreakListener(OnClickListener finishBreak) {
        Log.d(TAG, "setFinishBreakListener: ");
        this.finishBreak = finishBreak;
    }

    public void startUpdateGauges(BusinessRules.EventCode eventCode) {
        Log.d(TAG, "startUpdateGauges: ");
        try {
            if (gaugesHandler != null) {
                gaugesHandler.updateGauges(eventCode.getValue());
                handleBreakEvent(eventCode);
            }
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    private void handleBreakEvent(BusinessRules.EventCode eventCode) {
        Log.d(TAG, "handleBreakEvent: ");
//       March 23, 2022 -    We are using ON_BREAK_STARTED, before it as ON_BREAK
        if (eventCode.equals(BusinessRules.EventCode.ON_BREAK_STARTED)) {
            breaksView.setVisibility(View.VISIBLE);
        } else {
            breaksView.setVisibility(View.GONE);
        }
    }

    public void reloadGaugeHandler() {
        Log.d(TAG, "reloadGaugeHandler: ");
        gaugesHandler = new GaugesHandler(new GaugesHandler.GaugesProgress() {
            @Override
            public void updateCurrentEventTime(long time) {
                if (mCurrentEventTime != null)
                    mCurrentEventTime.updateCurrentEventTime(time);
            }

            @Override
            public void updateDrivingTime(long time) {
                drivingTimeInMinutes = time;
                Log.d(TAG, "updateDrivingTimeBeforeBreak: time: " + time + " drivingTimeInMinutes: " + drivingTimeInMinutes);
                if (driveProgressView != null)
                    driveProgressView.handleEventProgress(time);
            }

            @Override
            public void updateCurrentShiftTime(long time) {
                shiftTimeInMinutes = time;
                Log.d(TAG, "updateDrivingTimeBeforeBreak: time: " + time + " shiftTimeInMinutes: " + shiftTimeInMinutes);
                if (shiftProgressView != null)
                    shiftProgressView.handleEventProgress(time);
            }

            @Override
            public void updateCurrentMiles(long miles) {
                if (milesTv != null)
                    milesTv.setText("Miles Today: " + miles);
            }

            @Override
            public void updateCurrentCycleTime(long time) {
                cycleTimeInMinutes = time;
                Log.d(TAG, "updateDrivingTimeBeforeBreak: time: " + time + " cycleTimeInMinutes: " + cycleTimeInMinutes);
                if (cycleProgressView != null) {
                    cycleProgressView.handleEventProgress(time);
                }
            }

            @Override
            public void updateDrivingTimeBeforeBreak(long time) {
                breakTimeInMinutes = time;
                Log.d(TAG, "updateDrivingTimeBeforeBreak: time: " + time + " breakTimeInMinutes: " + breakTimeInMinutes);
                if (breakProgressView != null)
                    breakProgressView.handleEventProgress(time);
            }

            @Override
            public void updateBreakTime(long time) {
                if (breakTime != null) {

                    Log.e("breakTime", "" + time);
                    breakTime.setText(DateUtils.calculateBreakTime(time));
                }
            }
        });

    }

    public String getEventTitle() {
        Log.d(TAG, "getEventTitle: ");
        return gaugesHandler != null ? gaugesHandler.getCurrentEventTitle() : "";
    }

    public String getEventAbbreviation() {
        Log.d(TAG, "getEventAbbreviation: ");
        return gaugesHandler != null ? gaugesHandler.getCurrentEventAbbreviation() : "";
    }

    private void initEventProgressView() {
        Log.d(TAG, "initEventProgressView: ");

        if (driveProgressView != null)
            driveProgressView.init("Driving: 0.0 hrs", 11, ProgressEventView.ProgressType.DRIVING);

        if (cycleProgressView != null) {
            String hours = rules.getTruckLogHeaderDrivingRuleHours();
            int maxCycleHours = 0;
            if (hours != null) {
                maxCycleHours = Integer.parseInt(hours);
            }
            Log.d(TAG, "initEventProgressView: gaugesHandler: " + gaugesHandler);
            if (maxCycleHours == 0) {
                maxCycleHours = 70;
            }
            cycleProgressView.init("Cycle: 0.0 hrs", maxCycleHours, ProgressEventView.ProgressType.CYCLE);
        }

        if (shiftProgressView != null)
            shiftProgressView.init("Shift: 0.0 hrs", 14, ProgressEventView.ProgressType.SHIFT);

        if (breakProgressView != null)
            breakProgressView.init("Beak: 0.0 hrs", 8, ProgressEventView.ProgressType.BREAKS);
    }

    public void updateStations(Location myLastLocation, ConstraintLayout loadingPanel, Activity activity) {
        Log.d(TAG, "updateStations: ");
        Log.d(TAG, "updateStations: mActivity: " + activity);
        mActivity = activity;

        if (gaugesHandler == null || myLastLocation == null)
            return;

        LatLng myLastLatLng = Utils.convertLocationToLatLng(myLastLocation);
        if (myLastSavedLocation != null) {
            LatLng myLastSavedLatLng = Utils.convertLocationToLatLng(myLastSavedLocation);
            if (SphericalUtil.computeDistanceBetween(myLastLatLng, myLastSavedLatLng) < STATION_UPDATE_DISTANCE)
                return;
            else {
                myLastSavedLocation = myLastLocation;
            }
        } else {
            myLastSavedLocation = myLastLocation;
        }


        threadToCalculateDistance = new Thread(new Runnable() {
            @Override
            public void run() {

                calculateGaugesValuesForDistance();
//
//                LatLng taLatLng = gaugesHandler.getNearestTa((Activity) getContext());
//                setStationDistance(myLastLatLng, taLatLng, txtTa, loadingPanel);
//                String nearestTASight = findStraightDistance(myLastLatLng, taLatLng);
//                setValuesToTextView(activity, nearestTASight, txtTa);
//
//                LatLng pilotLatLng = gaugesHandler.getNearestPilot((Activity) getContext());
//                String nearestPilotSight = findStraightDistance(myLastLatLng, pilotLatLng);
//                setValuesToTextView(activity, nearestPilotSight, txtPilot);
//
//                LatLng restAreaLatLng = gaugesHandler.getNearestRestArea((Activity) getContext());
//                String nearestRestAreaSight = findStraightDistance(myLastLatLng, restAreaLatLng);
//                setValuesToTextView(activity, nearestRestAreaSight, txtRest);
//
//                LatLng loveLatLng = gaugesHandler.getNearestLove((Activity) getContext());
//                String nearestLoveSight = findStraightDistance(myLastLatLng, loveLatLng);
//                setValuesToTextView(activity, nearestLoveSight, txtLove);
            }
        });
        threadToCalculateDistance.start();
    }

    private static MapAsset nearestPilotAsset = null, nearestLoveAsset = null,
            nearestTaAsset = null;
    private static RestArea nearestRestArea = null;

    private void calculateGaugesValuesForDistance() {
        Log.d(TAG, "ForDistance: calculateGaugesValuesForDistance: rules.isDriverLoggedIn(): " + rules.isDriverLoggedIn());


        if (myLastSavedLocation != null) {
            nearestPilotAsset = rules.getNearestPilotAsset(myLastSavedLocation);
            if (nearestPilotAsset != null) {
                calculateDistanceToAsset(new LatLng(myLastSavedLocation.getLatitude(), myLastSavedLocation.getLongitude()),
                        new LatLng(Double.parseDouble(nearestPilotAsset.Latitude),
                                Double.parseDouble(nearestPilotAsset.Longitude)),
                        1);
            }


            nearestLoveAsset = rules.getNearestLoveAsset(myLastSavedLocation);
            if (nearestLoveAsset != null) {
                calculateDistanceToAsset(new LatLng(myLastSavedLocation.getLatitude(), myLastSavedLocation.getLongitude()),
                        new LatLng(Double.parseDouble(nearestLoveAsset.Latitude),
                                Double.parseDouble(nearestLoveAsset.Longitude)),
                        2);
            }


            nearestRestArea = rules.getNearestRestAreaAsset(myLastSavedLocation);
            if (nearestRestArea != null) {
                calculateDistanceToAsset(new LatLng(myLastSavedLocation.getLatitude(), myLastSavedLocation.getLongitude()),
                        new LatLng(Double.parseDouble(nearestRestArea.Latitude),
                                Double.parseDouble(nearestRestArea.Longitude)),
                        3);
            }


            nearestTaAsset = rules.getNearestTaAsset(myLastSavedLocation);
            if (nearestTaAsset != null) {
                calculateDistanceToAsset(new LatLng(myLastSavedLocation.getLatitude(), myLastSavedLocation.getLongitude()),
                        new LatLng(Double.parseDouble(nearestTaAsset.Latitude),
                                Double.parseDouble(nearestTaAsset.Longitude)),
                        4);
            }

        }
    }

    private void calculateDistanceToAsset(LatLng myLocation, LatLng stationLocation, int assetType) {
        Log.d(TAG, "calculateDistanceToAsset: myLocation: " + myLocation + " stationLocation: " + stationLocation);
        if (myLocation == null || stationLocation == null)
            return;

        Routing routing = new Routing.Builder()
                .key(getContext().getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .alternativeRoutes(false)
                .waypoints(myLocation, stationLocation)
                .withListener(new RoutingListener() {
                    @Override
                    public void onRoutingFailure(RouteException e) {
                        Log.d(TAG, "calculateDistanceToAsset: onRoutingFailure: ");
                    }

                    @Override
                    public void onRoutingStart() {
                        Log.d(TAG, "calculateDistanceToAsset: onRoutingStart: ");
                    }

                    @Override
                    public void onRoutingSuccess(List<Route> routes, int shortestRouteIndex, String routeName) {
                        Log.d(TAG, "calculateDistanceToAsset: onRoutingSuccess: ");
                        try {
                            Route route = routes.get(shortestRouteIndex);

                            Log.d(TAG, "calculateDistanceToAsset: onRoutingSuccess: " + route.getDistanceText());
                            int distanceInMeters = route.getDistanceValue();
                            Log.d(TAG, "calculateDistanceToAsset: onRoutingSuccess: distance: " + distanceInMeters);

                            double distanceInMiles = Utils.convertKmsToMiles((distanceInMeters / 1000.0));
                            updateDistance((int) distanceInMiles, assetType);

                        } catch (Throwable throwable) {
                            Log.d(TAG, "calculateDistanceToAsset: onRoutingSuccess: throwable: " + throwable.getMessage());
                            if (throwable != null)
                                throwable.printStackTrace();
                        }
                    }

                    @Override
                    public void onRoutingCancelled() {
                        Log.d(TAG, "calculateDistanceToAsset: onRoutingCancelled: ");
                    }
                })
                .build();
        routing.execute();
    }

    void updateDistance(int distance, int assetType) {
        Log.d(TAG, "calculateDistanceToAsset: updateDistance: distance: " + distance + " assetType: " + assetType);

        String distanceInStringMiles = distance + " mi";

        mActivity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                final Handler handler = new Handler();
//                handler.postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
                        //your code here
                        if (assetType == 1) {
                            nearestPilot = "" + distanceInStringMiles;
                            txtPilot.setText(distanceInStringMiles);
                        } else if (assetType == 2) {
                            nearestLove = "" + distanceInStringMiles;
                            txtLove.setText(distanceInStringMiles);
                        } else if (assetType == 3) {
                            nearestRest = "" + distanceInStringMiles;
                            txtRest.setText(distanceInStringMiles);
                        } else if (assetType == 4) {
                            nearestTa = "" + distanceInStringMiles;
                            txtTa.setText(distanceInStringMiles);
                        }
//                    }
//                }, 200);
            }
        });



    }


    void setValuesToTextView(Activity activity, String distance, TextView distanceTV) {
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //your code here
                        distanceTV.setText(distance);
                    }
                }, 200);
            }
        });
    }


    private void setStationDistance(LatLng myLocation, LatLng stationLocation, final TextView stationDistance,
                                    ConstraintLayout loadingPanel) {
        Log.d(TAG, "setStationDistance: ");
        if (myLocation == null || stationLocation == null)
            return;
//        loadingPanel.setVisibility(VISIBLE);

        Routing routing = new Routing.Builder()
                .key(getContext().getString(R.string.google_maps_key))
                .travelMode(AbstractRouting.TravelMode.DRIVING)
                .alternativeRoutes(false)
                .waypoints(myLocation, stationLocation)
                .withListener(new RoutingListener() {
                    @Override
                    public void onRoutingFailure(RouteException e) {
                        Log.d(TAG, "onRoutingFailure: ");
                    }

                    @Override
                    public void onRoutingStart() {
                        Log.d(TAG, "onRoutingStart: ");
                    }

                    @Override
                    public void onRoutingSuccess(List<Route> routes, int shortestRouteIndex, String routeName) {
                        Log.d(TAG, "onRoutingSuccess: ");
                        try {
                            Route route = routes.get(shortestRouteIndex);

                            int distance = route.getDistanceValue();
                            stationDistance.setText(Utils.getStationMiles(distance));
//                            loadingPanel.setVisibility(GONE);
                        } catch (Throwable throwable) {
                            if (throwable != null)
                                throwable.printStackTrace();
                        }
                    }

                    @Override
                    public void onRoutingCancelled() {
                    }
                })
                .build();
        routing.execute();
    }

    private void findStraightDistance(LatLng myLocation, LatLng stationLocation, final TextView stationDistance,
                                      ConstraintLayout loadingPanel, boolean isLoadingPanelShown) {
        Log.d(TAG, "setStationDistance: ");
        if (myLocation == null || stationLocation == null)
            return;

        double distance;

        Location locationA = new Location("myLocation");
        locationA.setLatitude(myLocation.latitude);
        locationA.setLongitude(myLocation.longitude);

        Location locationB = new Location("stationLocation");
        locationB.setLatitude(stationLocation.latitude);
        locationB.setLongitude(stationLocation.longitude);

        Log.d(TAG, "findStraightDistance: distance: before: ");
        distance = locationA.distanceTo(locationB);
        Log.d(TAG, "findStraightDistance: distance: " + distance);
//        stationDistance.setText(Utils.getStationMiles((int) distance));
        Log.d(TAG, "findStraightDistance: distance: after: ");

        Log.d(TAG, "findStraightDistance: isLoadingPanelShown: " + isLoadingPanelShown);
        if (isLoadingPanelShown) {
            Log.d(TAG, "findStraightDistance: loadingPanel: " + (loadingPanel.getVisibility() == VISIBLE));
            loadingPanel.setVisibility(GONE);
        }
    }

    private String findStraightDistance(LatLng myLocation, LatLng stationLocation) {
        Log.d(TAG, "setStationDistance: ");
        if (myLocation == null || stationLocation == null)
            return "";

        double distance;

        Location locationA = new Location("myLocation");
        locationA.setLatitude(myLocation.latitude);
        locationA.setLongitude(myLocation.longitude);

        Location locationB = new Location("stationLocation");
        locationB.setLatitude(stationLocation.latitude);
        locationB.setLongitude(stationLocation.longitude);

        Log.d(TAG, "findStraightDistance: distance: before: ");
        distance = locationA.distanceTo(locationB);
        Log.d(TAG, "findStraightDistance: distance: " + distance);
//        stationDistance.setText(Utils.getStationMiles((int) distance));
        Log.d(TAG, "findStraightDistance: distance: after: " + (Utils.getStationMiles((int) distance)));

        return (Utils.getStationMiles((int) distance));
    }

    private void setStationDistanceUpdate(LatLng myLocation, LatLng stationLocation, final TextView stationDistance) {
        Log.d(TAG, "setStationDistance: ");
        if (myLocation == null || stationLocation == null)
            return;

        Log.d(TAG, "DashboardFrag: setStationDistanceUpdate: computeDistanceBetween: " + SphericalUtil.computeDistanceBetween(myLocation, stationLocation));

    }

    public interface CurrentEventTime {
        void updateCurrentEventTime(long time);
    }

    public void stopThreadWhichCalculateDistance() {
        Log.d(TAG, "GaugesHandler: stopThreadWhichCalculateDistance: threadToCalculateDistance: " + threadToCalculateDistance);
        if (threadToCalculateDistance != null) {
            Log.d(TAG, "stopThreadWhichCalculateDistance: findStraightDistance: distance: after: THREAD interrupt: ");
//            threadToCalculateDistance.stop();
            threadToCalculateDistance.interrupt();
        }
    }

    String drivingStatus = "";

    public class MapAutoUpdateTask extends AsyncTask<String, String, Integer> {
        private int syncIntervalSecsMapAutoUpdateTask = 1;
        private long intervalCounter = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
//            June 03, 2022 -   We should calculate the hours when the thread is started
            Log.d(TAG, "onPreExecute: calculateHours: ");
//                calculateHours();
//                calculateGaugesValuesForDistance();
//                updateGaugesParametersForDistance();
//            I think we don't need this because we are already setting up their values
            drivingStatus = rules.getDrivingStatus();
        }

        @Override
        protected Integer doInBackground(String... params) {
//            Log.d(TAG, "MapAutoUpdateTask: doInBackground: ");
            do {
//                String drivingSpeed = rules.getDrivingSpeed();

//            June 03, 2022 -   We should update the minutes by one
                boolean isTimeToUpdateGauges = intervalCounter == 0 || (intervalCounter % 60 == 0);
                boolean isTimeToUpdateDistances = intervalCounter == 0 || (intervalCounter % 60 == 0);

                Log.d(TAG, "doInBackground: isTimeToUpdateGauges: " + isTimeToUpdateGauges + " intervalCounter: " + intervalCounter);
                if (isTimeToUpdateGauges) {
//                    June 13, 2022 -   We only increase the driving when we are driving otherwise not
                    if (gaugesHandler.getCurrentEventCode().equalsIgnoreCase("3")) {
                        drivingTimeInMinutes = drivingTimeInMinutes + 1;
                        cycleTimeInMinutes = cycleTimeInMinutes + 1;
                        breakTimeInMinutes = breakTimeInMinutes - 1;
                    }

//                    June 13, 2022 -   We should increment shift only if we are driving or if we are onDuty
                    if (gaugesHandler.getCurrentEventCode().equalsIgnoreCase("3") ||
                            gaugesHandler.getCurrentEventCode().equalsIgnoreCase("4")) {
                        shiftTimeInMinutes = shiftTimeInMinutes + 1;
                    }

                    setDrivingLayout(drivingTimeInMinutes);
                    setBreakLayout(breakTimeInMinutes);
                    setShiftLayout(shiftTimeInMinutes);
                    setCycleLayout(cycleTimeInMinutes);
                }

                intervalCounter++;
                try {
                    Thread.sleep(syncIntervalSecsMapAutoUpdateTask * 1000);
                } catch (InterruptedException interruptedException) {
//                    MainMenuActivity.logDataArrayList.add("doInBackground: interruptedException: " + interruptedException);
//                    addLogGeofenceData("doInBackground: interruptedException: " + interruptedException);
                    Log.d(TAG, "MapAutoUpdateTask: doInBackground: interruptedException: " + interruptedException.getMessage());
                    interruptedException.printStackTrace();
                }

            } while (true);
        }
    }

    void setDrivingLayout(long timeInMinutes) {
        Log.d(TAG, "setDrivingLayout: mActivity: " + mActivity + " timeInMinutes: " + timeInMinutes);
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: ");
                    if (driveProgressView != null)
                        driveProgressView.handleEventProgress(timeInMinutes);
                }
            });
        }
    }

    void setBreakLayout(long timeInMinutes) {
        Log.d(TAG, "setBreakLayout: mActivity: " + mActivity + " timeInMinutes: " + timeInMinutes);
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: ");
                    if (breakProgressView != null)
                        breakProgressView.handleEventProgress(timeInMinutes);
                }
            });
        }

    }

    void setShiftLayout(long timeInMinutes) {
        Log.d(TAG, "setShiftLayout: mActivity: " + mActivity + " timeInMinutes: " + timeInMinutes);
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: ");
                    if (shiftProgressView != null)
                        shiftProgressView.handleEventProgress(timeInMinutes);
                }
            });
        }
    }

    void setCycleLayout(long timeInMinutes) {
        Log.d(TAG, "setCycleLayout: mActivity: " + mActivity + " timeInMinutes: " + timeInMinutes);
        if (mActivity != null) {
            mActivity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG, "run: ");
                    if (cycleProgressView != null) {
                        cycleProgressView.handleEventProgress(timeInMinutes);
                    }
                }
            });
        }


    }


}
