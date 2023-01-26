package com.rco.rcotrucks.activities.dashboard;


import android.app.Activity;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.hawk.Hawk;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.rms.TruckLogHeader;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;

import static com.rco.rcotrucks.utils.Constants.KEY_LAST_ODOMETER;
import static com.rco.rcotrucks.activities.drive.gauges.ProgressEventView.BREAKS_MAX_HOURS;

public class GaugesHandler {

    private static final String TAG = GaugesHandler.class.getSimpleName();
    //update gauges each 1mn
    private final static int GAUGE_DELAY = 1000 * 60;
    private final static int GAUGE_DELAY_RELOAD = 1000 * 5;
    protected BusinessRules rules = BusinessRules.instance();

    private long currentEventTimeInMinutes = 0, drivingTimeInMinutes = 0, cycleTimeInMinutes = 0;
    private long shiftTimeInMinutes = 0, timeBeforBreakinMinutes = 0, breakTimeInMinutes = -1;
    private long miles = 0;
    int maxCycleHours = 70;

    private String currentEventAbbreviation = "";
    private String currentEventTitle = "";
    private String currentEventCode = null;

    List<EldEvent> todayEventList = new ArrayList<>();

    private GaugesProgress mGaugesProgress;
    String lastEventDate = null;

    Handler mGaugesHandler = new Handler();
    Runnable mGaugesTask = new Runnable() {
        @Override
        public void run() {
            if (lastEventDate == null) {
                loadGauges();
            }
            updateAllGauges();
            mGaugesHandler.postDelayed(mGaugesTask, GAUGE_DELAY);
        }
    };

    public GaugesHandler(GaugesProgress gaugesProgress) {
        Log.d(TAG, "GaugesHandler: ");
        mGaugesProgress = gaugesProgress;
        loadGauges();
    }

    void startGaugesHandler() {
        Log.d(TAG, "startGaugesHandler: ");
        final Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.d(TAG, "run: startGaugesHandler: postDelayed: run: ");
                //your code here
                if (lastEventDate == null) {
                    loadGauges();
                }
            }
        }, 5000);
    }

    void loadGauges() {

        lastEventDate = rules.getLastShiftStartDate();
        Log.d(TAG, "GaugesHandler: lastEventDate: " + lastEventDate);
//        if (lastEventDate == null) {
//            startGaugesHandler();
//            return;
//        }

        if (!Hawk.contains(KEY_LAST_ODOMETER + "-" + rules.getLastLoggedInUsername() + "-" + lastEventDate)) {
            String savedOdometer = rules.getLastSavedOdometer(lastEventDate);
            if (savedOdometer != null) {
                Hawk.put(KEY_LAST_ODOMETER + "-" + rules.getLastLoggedInUsername() + "-" + lastEventDate, Double.parseDouble(savedOdometer));
            }
        }

        double odometer = Hawk.get(KEY_LAST_ODOMETER + "-" + rules.getLastLoggedInUsername() + "-" + lastEventDate, 0D);
        todayEventList = rules.getAllDutyEventAfterDate(lastEventDate);

        Log.d(TAG, "GaugesHandler: todayEventList: " + todayEventList);
//        if (todayEventList == null || todayEventList.size() == 0)
//            return;
        Log.d(TAG, "GaugesHandler: todayEventList: size: " + todayEventList.size());

        EldEvent currentEvent = null;
        if (todayEventList.size() > 0) {
            currentEvent = todayEventList.get(todayEventList.size() - 1);
        }
        // Calculate driving , shift and mile from DBB
        for (int i = 0; i < todayEventList.size(); i++) {
            EldEvent event = todayEventList.get(i);

            if (event == null || event.EventCode == null)
                continue;

//                May 31, 2022  -   We should use just the events that are a change in driver duty status
            if (!event.EventType.equals("1")) {
                continue;
            }

            Log.d(TAG, "GaugesHandler: todayEventList: size: " + todayEventList.size());
            Log.d(TAG, "GaugesHandler: event: MobileRecordId: " + event.MobileRecordId +
                    " recordId: " + event.RecordId);

            if ((i + 1) < todayEventList.size()) {
                EldEvent nextEvent = todayEventList.get(i + 1);
                long timeInSecond = nextEvent.getEventSecondsValue() - event.getEventSecondsValue();
                timeInSecond = DateUtils.eliminateSecondsFromValue(timeInSecond);
//                    Log.d(TAG, "GaugesHandler: timeInSecond: " + timeInSecond);

                if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                    drivingTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
//                        May 30, 2022  -   We should add the driving time to the cycle hours
//                        cycleTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
//                        Log.d(TAG, "GaugesHandler: DRIVING: shiftTimeInMinutes: " + shiftTimeInMinutes);
//                        Log.d(TAG, "GaugesHandler: DRIVING: cycleTimeInMinutes: " + cycleTimeInMinutes);
                    if (event.getOdometer() != null && !event.getOdometer().isEmpty())
//                            try {
                        if (!StringUtils.isNullOrWhitespaces(event.getOdometer())) {
                            double lastOdometer = Double.parseDouble(event.getOdometer());
//                                    Log.d(TAG, "GaugesHandler: lastOdometer: " + lastOdometer + " odometer: " + odometer);
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
//                        Log.d(TAG, "GaugesHandler: ON_DUTY_NOT_DRIVING: shiftTimeInMinutes: " + shiftTimeInMinutes);
                    shiftTimeInMinutes += DateUtils.convertToMinutes(timeInSecond);
                }
            } else {
//                    Log.d(TAG, "GaugesHandler: todayEventList: size: is not greater than (i+1)");

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
                }
            }
        }

        Log.d(TAG, "GaugesHandler: cycleTimeInMinutes: first: " + cycleTimeInMinutes);
        Log.d(TAG, "GaugesHandler: shiftTimeInMinutes: " + shiftTimeInMinutes);
        long currentTimeInSecond = DateUtils.getTimeInSecond();
        if (currentEvent != null) {
            currentEventTimeInMinutes = (long) (currentTimeInSecond - currentEvent.getEventSeconds()) / 60;
        } else {
            currentEventTimeInMinutes = 0;
        }

        // Calculate cycle time from DBB
        TruckLogHeader truckLogHeader = rules.getOpenTruckLogHeader();

        String hours = rules.getTruckLogHeaderDrivingRuleHours();
        if (hours != null) {
            maxCycleHours = Integer.parseInt(hours);
        }
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
            Log.d(TAG, "GaugesHandler:: startDate and Time: " + cycleStartDate + " " + truckLogHeader.StartTime);
            cycleDate = cycleStartDate + " " + truckLogHeader.StartTime;
            Log.d(TAG, "GaugesHandler:: cycleDate: " + cycleDate);
        }
        cycleTimeInMinutes += getDrivingTimeInCyclePeriod(cycleDate);


        if (currentEvent != null && currentEvent.EventCode != null && currentEvent.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
            timeBeforBreakinMinutes = BREAKS_MAX_HOURS - currentEventTimeInMinutes;
        } else {
            timeBeforBreakinMinutes = BREAKS_MAX_HOURS;
        }

        mGaugesProgress.updateCurrentEventTime(currentEventTimeInMinutes);
        mGaugesProgress.updateDrivingTime(drivingTimeInMinutes);
        mGaugesProgress.updateDrivingTimeBeforeBreak(timeBeforBreakinMinutes);
        mGaugesProgress.updateCurrentShiftTime(shiftTimeInMinutes);
        Log.d(TAG, "GaugesHandler: cycleTimeInMinutes: second: " + cycleTimeInMinutes);
        mGaugesProgress.updateCurrentCycleTime(cycleTimeInMinutes);
        mGaugesProgress.updateCurrentMiles(miles);


        if (currentEvent != null) {
            updateEventTitle(currentEvent.EventCode);
            currentEventCode = currentEvent.EventCode;
        } else {
            currentEventCode = BusinessRules.EventCode.NOT_SET.getValue();
        }
    }

    private void updateEventTitle(String eventCode) {
        try {
            switch (eventCode) {
                case "2":
                    currentEventTitle = "Sleeper Not Driving";
                    currentEventAbbreviation = "SL";
                    break;
                case "3":
                    currentEventTitle = "Driving";
                    currentEventAbbreviation = "D";
                    break;
                case "4":
                    currentEventTitle = "ON DUTY Not Driving";
                    currentEventAbbreviation = "ON";
                    break;
                default:
                    currentEventTitle = "OFF DUTY Not Driving";
                    currentEventAbbreviation = "OF";
                    break;
            }
        } catch (Throwable throwable) {

        }
    }

    private int getDrivingTimeInCyclePeriod(String date) {
        Log.d(TAG, "getDrivingTimeInCyclePeriod: date: " + date);
        List<EldEvent> eventList = rules.getAllDutyEventAfterDate(date);
        int driveTime = 0;

        Log.d(TAG, "getDrivingTimeInCyclePeriod: eventList: " + eventList.size());
        for (int i = 0; i < eventList.size(); i++) {
            EldEvent event = eventList.get(i);

            if (event == null || event.EventCode == null)
                continue;

//                May 31, 2022  -   We should use just the events that are a change in driver duty status
//            if (!event.EventType.equals(BusinessRules.EventType.A_CHANGE_IN_DRIVER_DUTY_STATUS)) {
            if (!event.EventType.equals("1")) {
                continue;
            }
            Log.d(TAG, "getDrivingTimeInCyclePeriod: index: " + i + " event.EventType: " + event.EventType
                    + " mobileRecordId: " + event.MobileRecordId);

            if (i + 1 < eventList.size()) {
                EldEvent nextEvent = eventList.get(i + 1);
                long timeInSecond = nextEvent.getEventSecondsValue() - event.getEventSecondsValue();
                timeInSecond = DateUtils.eliminateSecondsFromValue(timeInSecond);
                Log.d(TAG, "getDrivingTimeInCyclePeriod: timeInSeconds: difference: " + timeInSecond);

                if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                    driveTime += DateUtils.convertToMinutes(timeInSecond);
                    Log.d(TAG, "getDrivingTimeInCyclePeriod: event: mobileRecordId: " + event.MobileRecordId +
                            " recordId: " + event.RecordId);
                    Log.d(TAG, "getDrivingTimeInCyclePeriod: eventCode:Driving: driveTime: " + driveTime);
                }
            } else if (event.EventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                long timeInSecond = DateUtils.getTimeInSecond() - event.getEventSecondsValue();
                timeInSecond = DateUtils.eliminateSecondsFromValue(timeInSecond);
                driveTime += DateUtils.convertToMinutes(timeInSecond);
                Log.d(TAG, "else if driving: getDrivingTimeInCyclePeriod: event: mobileRecordId: " +
                        "" + event.MobileRecordId + " recordId: " + event.RecordId);
                Log.d(TAG, "else if driving: getDrivingTimeInCyclePeriod: eventCode:Driving: driveTime: "
                        + driveTime);
            }
        }

        Log.d(TAG, "getDrivingTimeInCyclePeriod: before returning driveTime: " + driveTime);
        return driveTime;
    }

    private void updateAllGauges() {
        try {
            if (currentEventCode != null) {
                currentEventTimeInMinutes += 1;
                mGaugesProgress.updateCurrentEventTime(currentEventTimeInMinutes);

                if (currentEventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                    drivingTimeInMinutes += 1;
                    mGaugesProgress.updateDrivingTime(drivingTimeInMinutes);

                    cycleTimeInMinutes += 1;
                    Log.d(TAG, "updateAllGauges: cycleTimeInMinutes: " + cycleTimeInMinutes);
                    mGaugesProgress.updateCurrentCycleTime(cycleTimeInMinutes);

                    shiftTimeInMinutes += 1;
                    mGaugesProgress.updateCurrentShiftTime(shiftTimeInMinutes);
                } else if (currentEventCode.equals(BusinessRules.EventCode.ON_DUTY_NOT_DRIVING.getValue())) {
                    shiftTimeInMinutes += 1;
                    mGaugesProgress.updateCurrentShiftTime(shiftTimeInMinutes);
                }
            }

            if (currentEventCode != null && currentEventCode.equals(BusinessRules.EventCode.DRIVING.getValue())) {
                if (timeBeforBreakinMinutes >= 0)
                    timeBeforBreakinMinutes = BREAKS_MAX_HOURS - currentEventTimeInMinutes;
                else
                    timeBeforBreakinMinutes = 0;

                mGaugesProgress.updateDrivingTimeBeforeBreak(timeBeforBreakinMinutes);
            }

            //       March 23, 2022 -    We are using ON_BREAK_STARTED, before it as ON_BREAK
            if (currentEventCode != null && currentEventCode.equals(BusinessRules.EventCode.ON_BREAK_STARTED.getValue())) {
                timeBeforBreakinMinutes = 0;

                breakTimeInMinutes += 1;
                mGaugesProgress.updateBreakTime(breakTimeInMinutes);
            }

            String lastEventDate = rules.getLastShiftStartDate();
            if (Hawk.contains(KEY_LAST_ODOMETER + "-" + rules.getLastLoggedInUsername() + "-" + lastEventDate)) {
                double odometer = Hawk.get(KEY_LAST_ODOMETER + "-" + rules.getLastLoggedInUsername() + "-" + lastEventDate, 0D);

                String savedOdometer = rules.getLastSavedOdometer(lastEventDate);
                double lastSavedOdometer = Double.valueOf(savedOdometer);
                if (lastSavedOdometer > odometer) {
                    miles = (long) (lastSavedOdometer - odometer);
                } else {
                    miles = 0;
                }
            }

        } catch (Throwable throwable) {

        }
    }

    public void updateGauges(String eventCode) {
        try {
            currentEventCode = eventCode;
            updateEventTitle(eventCode);
            breakTimeInMinutes = -1;
            startRepeatingGaugeTask();
        } catch (Throwable throwable) {

        }
    }

    public void startRepeatingGaugeTask() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                if (mGaugesHandler.hasCallbacks(mGaugesTask))
                    stopRepeatingGaugeTask();
            }
            mGaugesTask.run();
        } catch (Throwable throwable) {

        }
    }


    public void stopRepeatingGaugeTask() {
        mGaugesHandler.removeCallbacks(mGaugesTask);
    }

    public String getCurrentEventAbbreviation() {
        return currentEventAbbreviation;
    }

    public String getCurrentEventTitle() {
        return currentEventTitle;
    }

    public LatLng getNearestTa(Activity activity) {
        return rules.calculateNearestTaStation(activity);
    }

    public LatLng getNearestPilot(Activity activity) {
        return rules.calculateNearestPilotStation(activity);
    }

    public LatLng getNearestRestArea(Activity activity) {
        return rules.calculateNearestRestAreaStation(activity);
    }

    public LatLng getNearestLove(Activity activity) {
        return rules.calculateNearestLoveStation(activity);
    }

    public interface GaugesProgress {
        void updateCurrentEventTime(long time);

        void updateDrivingTime(long time);

        void updateCurrentShiftTime(long time);

        void updateCurrentMiles(long miles);

        void updateCurrentCycleTime(long time);

        void updateDrivingTimeBeforeBreak(long time);

        void updateBreakTime(long time);
    }

    public int getMaxCycleHours() {
        return maxCycleHours;
    }

    public String getCurrentEventCode() {
        return currentEventCode;
    }
}
