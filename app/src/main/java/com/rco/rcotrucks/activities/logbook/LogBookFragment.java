package com.rco.rcotrucks.activities.logbook;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.TruckEldDetail;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.views.ChartEventView;
import com.rco.rcotrucks.views.GridDividerItemDecoration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class LogBookFragment extends Fragment {

    private static final String TAG = LogBookFragment.class.getSimpleName();
    public static final int GRID_SPAN_COUNT = 6;
    public static final String ARG_LOG_BOOK = "LOG_BOOK_DAY";
    private BusinessRules businessRules;

    public static LogBookFragment newInstance(String logBookDay) {
        Log.d(TAG, "newInstance: ");
        LogBookFragment fragment = new LogBookFragment();

        Bundle args = new Bundle();
        args.putString(ARG_LOG_BOOK, logBookDay);
        fragment.setArguments(args);

        return fragment;
    }

    private RecyclerView eldInfoRecyclerView, eldEventRecyclerview, eldViolationsRecyclerview;
    private TextView ofHours, slHours, drHours, onHours;
    private EldInfoAdapter eldInfoAdapter;
    private EldEventAdapter eldEventAdapter;
    private EldViolationAdapter eldViolationAdapter;
    private ChartEventView graphView;
    private List<String> eldInfoData = new ArrayList<>();
    private List<LogBookELDEvent> logBookELDEventData = new ArrayList<>();
    private List<Violation> violationList = new ArrayList<>();
    private String logBookDay;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_log_book, container, false);
        Log.d(TAG, "onCreateView: ");
        logBookDay = getArguments().getString(ARG_LOG_BOOK, "");
        businessRules = ((MainMenuActivity) getActivity()).rules;

        setIds(rootView);

        fillELDHeaderData();
        setupEldInfoView();

        fillEventData();
        setupEldEventView();
        setupEldViolationView();

        return rootView;
    }

    void setIds(View view) {

        eldInfoRecyclerView = (RecyclerView) view.findViewById(R.id.eld_info_recyclerview);
        eldEventRecyclerview = (RecyclerView) view.findViewById(R.id.eld_event_recyclerview);
        eldViolationsRecyclerview = (RecyclerView) view.findViewById(R.id.eld_violations_recyclerview);
        graphView = (ChartEventView) view.findViewById(R.id.graphView);

        ofHours = (TextView) view.findViewById(R.id.of_hours);
        slHours = (TextView) view.findViewById(R.id.sl_hours);
        drHours = (TextView) view.findViewById(R.id.dr_hours);
        onHours = (TextView) view.findViewById(R.id.on_hours);
    }

    private void setupEldInfoView() {
        Log.d(TAG, "setupEldInfoView: ");
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), GRID_SPAN_COUNT);

        Drawable divider = ContextCompat.getDrawable(getActivity(), R.drawable.shape_line_divider);
        eldInfoAdapter = new EldInfoAdapter(eldInfoData);
        eldInfoRecyclerView.setLayoutManager(gridLayoutManager);
        eldInfoRecyclerView.addItemDecoration(new GridDividerItemDecoration(divider, divider, GRID_SPAN_COUNT));
        eldInfoRecyclerView.setAdapter(eldInfoAdapter);
    }

    private void fillELDHeaderData() {
        Log.d(TAG, "fillELDHeaderData: ");
        eldInfoData = new ArrayList<>();

        Log.d(TAG, "fillELDHeaderData: logBookDay: "+logBookDay);
        TruckEldDetail eldDetailValue = businessRules.getLastTruckEldDetailByCreationDate(logBookDay);
        Log.d(TAG, "fillELDHeaderData: eldDetailValue: " + eldDetailValue);

        // region Header first Line Info
        eldInfoData.addAll(Arrays.asList(getResources().getStringArray(R.array.eld_header_titles_first_line)));

        if (eldDetailValue != null) {
            Log.d(TAG, "fillELDHeaderData: eldDetailValue: CreationDate: " + eldDetailValue.CreationDate);
            String formattedCreationDate = DateUtils.convertDateTime(eldDetailValue.CreationDate, DateUtils.FORMAT_DATE_TIME_MILLIS, DateUtils.FORMAT_DATE_DD_MMM_YY);
            eldInfoData.add(formattedCreationDate);
            eldInfoData.add(eldDetailValue.UsDotNumber);
            try {
                List<String> driverData = businessRules.getDriverInfo(eldDetailValue.DriverRecordId);

                eldInfoData.add(driverData.get(0));
                eldInfoData.add(driverData.get(1));
            } catch (Exception e) {
                Log.d(TAG, "fillELDHeaderData: exception: " + e.getMessage());
                eldInfoData.add(eldDetailValue.VehicleLicenseNumber);
                eldInfoData.add("");
            }

            eldInfoData.add(eldDetailValue.EldRegistrationId);
            eldInfoData.add(eldDetailValue.TrailerNumber);
//            eldInfoData.add();

        } else {
            for (int i = 0; i < GRID_SPAN_COUNT; i++) {
                eldInfoData.add("");
            }
        }
        //endregion

        // region Header second Line Info
        eldInfoData.addAll(Arrays.asList(getResources().getStringArray(R.array.eld_header_titles_second_line)));

        if (eldDetailValue != null) {
            eldInfoData.add(eldDetailValue.TimeZone);
            eldInfoData.add(eldDetailValue.DriverName);
            eldInfoData.add(eldDetailValue.CoDriverName);
            eldInfoData.add(eldDetailValue.EldManufacturerName);
            eldInfoData.add(eldDetailValue.ShippingId);
            if (eldDetailValue.DataDiagnosticsIndicators != null) {
                String formattedDataDiagnosticsIndicators = eldDetailValue.DataDiagnosticsIndicators.equals("O") ? "no" : "yes";
                eldInfoData.add(formattedDataDiagnosticsIndicators);
            } else {
                eldInfoData.add("");
            }
        } else {
            for (int i = 0; i < GRID_SPAN_COUNT; i++) {
                eldInfoData.add("");
            }
        }
        //endregion

        // region Header third Line Info
        eldInfoData.addAll(Arrays.asList(getResources().getStringArray(R.array.eld_header_titles_third_line)));

        if (eldDetailValue != null) {
            eldInfoData.add(eldDetailValue.TwentyFourHourPeriodStartingTime);
            eldInfoData.add(eldDetailValue.DriverId);
            eldInfoData.add(eldDetailValue.CoDriverId);
            eldInfoData.add(eldDetailValue.TruckNumber);
            eldInfoData.add(eldDetailValue.UnidentifiedDriverRecords);
            if (eldDetailValue.EldMalfunctionIndicators != null) {
                String formattedEldMalfunctionIndicators = eldDetailValue.EldMalfunctionIndicators.equals("O") ? "no" : "yes";
                eldInfoData.add(formattedEldMalfunctionIndicators);
            } else {
                eldInfoData.add("");
            }
        } else {
            for (int i = 0; i < GRID_SPAN_COUNT; i++) {
                eldInfoData.add("");
            }
        }
        //endregion

        // region Header fourth Line Info
        eldInfoData.addAll(Arrays.asList(getResources().getStringArray(R.array.eld_header_titles_fourth_line)));

        if (eldDetailValue != null) {
            eldInfoData.add(eldDetailValue.CarrierName);
            String startOdometerHours = eldDetailValue.OdometerStart != null && !eldDetailValue.OdometerStart.equals("0") ? eldDetailValue.OdometerStart : "0.0";
            String endOdometerHours = eldDetailValue.OdometerEnd != null && !eldDetailValue.OdometerEnd.equals("0") ? eldDetailValue.OdometerEnd : "0.0";
            eldInfoData.add(startOdometerHours + "-" + endOdometerHours);

            eldInfoData.add(eldDetailValue.MilesToday);
            eldInfoData.add(eldDetailValue.TruckVin);
            eldInfoData.add(eldDetailValue.ExemptDriverStatus);

            String startEngineHours = eldDetailValue.EngineHoursStart != null && !eldDetailValue.EngineHoursStart.equals("0") ? eldDetailValue.EngineHoursStart : "0.0";
            String endEngineHours = eldDetailValue.EngineHoursEnd != null && !eldDetailValue.EngineHoursEnd.equals("0") ? eldDetailValue.EngineHoursEnd : "0.0";
            eldInfoData.add(startEngineHours + "-" + endEngineHours);
        } else {
            for (int i = 0; i < GRID_SPAN_COUNT; i++) {
                eldInfoData.add("");
            }
        }
        //endregion

        // region Header fifth Line Info
        eldInfoData.addAll(Arrays.asList(getResources().getStringArray(R.array.eld_header_titles_fifth_line)));

        if (eldDetailValue != null) {
            eldInfoData.add(eldDetailValue.CurrentLocation);
            eldInfoData.add("");
            eldInfoData.add(eldDetailValue.Comments);
            eldInfoData.add("");

            String formattedCurrentDate = DateUtils.getDateTime(new Date(), DateUtils.FORMAT_DATE_DD_MMM_YY);
            eldInfoData.add(formattedCurrentDate);
            eldInfoData.add(eldDetailValue.Status);
        } else {
            for (int i = 0; i < GRID_SPAN_COUNT; i++) {
                eldInfoData.add("");
            }
        }
        //endregion

        Log.d(TAG, "fillELDHeaderData: eldInfoData: size: " + eldInfoData.size());
    }

    private void setupEldEventView() {
        Log.d(TAG, "setupEldEventView: ");
        Log.d(TAG, "setupEldEventView: eldEventRecyclerview: isVisible: " + eldEventRecyclerview.getVisibility());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        eldEventRecyclerview.setLayoutManager(linearLayoutManager);

        Log.d(TAG, "setupEldEventView: logBookELDEventData: " + logBookELDEventData.size());
        eldEventAdapter = new EldEventAdapter(logBookELDEventData);
        eldEventRecyclerview.setAdapter(eldEventAdapter);

//        Oct 04, 2022  -   Added this check because it was creating a black line right below of first row and this line sounds like
//        there is another row below it but its not the case
        if (logBookELDEventData.size() > 1) {
            DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(getContext(),
                    LinearLayoutManager.VERTICAL);
            dividerItemDecoration.setDrawable(getContext().getResources().getDrawable(R.drawable.shape_line_divider));
            eldEventRecyclerview.addItemDecoration(dividerItemDecoration);
        }

    }

    private void fillEventData() {
        Log.d(TAG, "fillEventData: ");
        logBookELDEventData = new ArrayList<>();
        String[] header = getResources().getStringArray(R.array.eld_event_header);
        LogBookELDEvent headerLogBookELDEvent = new LogBookELDEvent();
        headerLogBookELDEvent.setTime(header[0]);
        headerLogBookELDEvent.setLocation(header[1]);
        headerLogBookELDEvent.setOdometer(header[2]);
        headerLogBookELDEvent.setEngHours(header[3]);
        headerLogBookELDEvent.setEventTypeDescription(header[4]);
        headerLogBookELDEvent.setOrigin(header[5]);
        Log.d(TAG, "fillEventData: header: " + header[0] + " :" + header[1] + " :" + header[2]
                + " :" + header[3] + " :" + header[4] + " :" + header[5] + " :");
        Log.d(TAG, "fillEventData: headerLogBookELDEvent: " + headerLogBookELDEvent);

        logBookELDEventData.add(headerLogBookELDEvent);

        List<LogBookELDEvent> eldDetailValue = businessRules.getAllEldEventByDate(logBookDay);
        Log.d(TAG, "fillEventData: eldDetailValue: size: " + eldDetailValue);
        logBookELDEventData.addAll(eldDetailValue);

        displayEventChart(eldDetailValue);
    }

    private void displayEventChart(List<LogBookELDEvent> eldDetailValue) {
        Log.d(TAG, "displayEventChart: eldDetailValue: " + eldDetailValue);
        Log.d(TAG, "displayEventChart: logBookDay: " + logBookDay);
        String previousDay = DateUtils.getPreviousDay(logBookDay, DateUtils.FORMAT_DATE_YYYY_MM_DD);
        LogBookELDEvent previousDayELDEvent = businessRules.getPreviousEventByDate(previousDay);
        graphView.setEldEventDate(logBookDay);

        Log.d(TAG, "displayEventChart: previousDayELDEvent: " + previousDayELDEvent);
        if (previousDayELDEvent == null) {
            previousDayELDEvent = new LogBookELDEvent();
            previousDayELDEvent.setEventType("1");
            previousDayELDEvent.setEventCode("1");
            previousDayELDEvent.setTime("00:00");
        }
        Log.d(TAG, "displayEventChart: ");
        graphView.setListOfEvents(previousDayELDEvent, eldDetailValue);
        violationList.clear();
        graphView.setPeriodsListener(new ChartEventView.IEventPeriod() {
            @Override
            public void onUpdateOFEvent(String of) {
                Log.d(TAG, "onUpdateOFEvent: of: " + of);
                ofHours.setText(of);
            }

            @Override
            public void onUpdateSLEvent(String sl) {
                Log.d(TAG, "onUpdateSLEvent: sl: " + sl);
                slHours.setText(sl);
            }

            @Override
            public void onUpdateDREvent(String dr) {
                Log.d(TAG, "onUpdateDREvent: dr: " + dr);
                drHours.setText(dr);
            }

            @Override
            public void onUpdateONEvent(String on) {
                Log.d(TAG, "onUpdateONEvent: on: " + on);
                onHours.setText(on);
            }

            @Override
            public void isON11HoursViolation() {
                Log.d(TAG, "isON11HoursViolation: ");
                String violationID = getResources().getString(R.string.driving_beyond_11_id);
                String violationDesc = getResources().getString(R.string.driving_beyond_11_desc);
                setAViolation(new Violation(violationID, violationDesc));
            }

            @Override
            public void isON8HoursViolation() {
                Log.d(TAG, "isON8HoursViolation: ");
                String violationID = getResources().getString(R.string.driving_beyond_8_id);
                String violationDesc = getResources().getString(R.string.driving_beyond_8_desc);
                setAViolation(new Violation(violationID, violationDesc));
            }

            @Override
            public void isON14hoursViolation() {
                Log.d(TAG, "isON14hoursViolation: ");
                String violationID = getResources().getString(R.string.driving_beyond_14_id);
                String violationDesc = getResources().getString(R.string.driving_beyond_14_desc);
                setAViolation(new Violation(violationID, violationDesc));
            }

            @Override
            public void isNotSB7ContinuousHoursViolation() {
                Log.d(TAG, "isNotSB7ContinuousHoursViolation: ");
                String violationID = getResources().getString(R.string.sleeper_berth_7_id);
                String violationDesc = getResources().getString(R.string.sleeper_berth_7_desc);
                setAViolation(new Violation(violationID, violationDesc));
            }

            @Override
            public void isNotSB10TotalHoursViolation() {
                Log.d(TAG, "isNotSB10TotalHoursViolation: ");
                String violationID = getResources().getString(R.string.sleeper_berth_10_id);
                String violationDesc = getResources().getString(R.string.sleeper_berth_10_desc);
                setAViolation(new Violation(violationID, violationDesc));
            }
        });
    }

    private void setupEldViolationView() {
        Log.d(TAG, "setupEldViolationView: violationList: size: " + violationList.size());
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        eldViolationsRecyclerview.setLayoutManager(linearLayoutManager);
        eldViolationAdapter = new EldViolationAdapter(violationList);
        eldViolationsRecyclerview.setAdapter(eldViolationAdapter);
    }

    private void setAViolation(Violation violation) {
        Log.d(TAG, "setAViolation: ");
        violationList.add(violation);
        eldViolationAdapter.notifyDataSetChanged();
        Log.d(TAG, "setAViolation: violationList: size: " + violationList.size());
    }

}


