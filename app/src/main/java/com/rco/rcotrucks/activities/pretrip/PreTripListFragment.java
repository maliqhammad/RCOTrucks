package com.rco.rcotrucks.activities.pretrip;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.icu.util.Calendar;
import android.opengl.Visibility;
import android.os.Bundle;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.dvir.BusHelperDvir;
import com.rco.rcotrucks.activities.dvir.DvirDtlActivity;
import com.rco.rcotrucks.activities.dvir.DvirDtlAdapter;
import com.rco.rcotrucks.activities.dvir.DvirListAdapter;
import com.rco.rcotrucks.activities.dvir.DvirListFragment;
import com.rco.rcotrucks.activities.dvir.UiHelperDvirList;
import com.rco.rcotrucks.activities.dvir.activities.PreTripReport;
import com.rco.rcotrucks.activities.dvir.adapter.PreTripAdapter;
import com.rco.rcotrucks.activities.dvir.fragments.DvirReport;
import com.rco.rcotrucks.activities.dvir.fragments.EditPretripFragment;
import com.rco.rcotrucks.activities.dvir.fragments.PreTripReportFragment;
import com.rco.rcotrucks.activities.dvir.fragments.PreTripReportFragmentUpdate;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.FuelReceiptListAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.TollReceiptAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.DateRangeFilterAdapter;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.dialog.DateFilterDialog;
import com.rco.rcotrucks.model.DateRangeModel;
import com.rco.rcotrucks.model.PretripModel;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class PreTripListFragment extends Fragment {

    private static final String TAG = PreTripListFragment.class.getName();
    public BusinessRules businessRules = BusinessRules.instance();
    public static final String EXTRA_MESSAGE_DVIR_RMS_DVIR_IDENT = "com.rco.rcotrucks.dvirident";
    private EditText searchET;

    MaterialDatePicker materialDatePicker;
    MaterialDatePicker.Builder<Pair<Long, Long>> materialDateBuilder;

    ConstraintLayout btnBluetoothOn, dateRangeFilterLayout;
    ImageView refreshBtn, addButton, searchClear, cancelSearch;
    TextView leftSideTitle, mainTitle, cancelSearchRangeFilter, editPretrip, noRecordFound, filterBtn, cancel, save,
            sync, homeAppBarLeftSideTitle, deleteMultipleEntries;

    Calendar mCalendar;
    DateFilterDialog dateFilterDialog;
    String rangeStartDate = "", rangeEndDate = "";
    long today = 0, month = 0;
    boolean isTablet = false;

    RecyclerView dateRangeRecyclerView;
    DateRangeFilterAdapter dateRangeFilterAdapter;
    List<DateRangeModel> dateRangeModelList;
    DvirListAdapter.ListItemDvir lastSelectedItem = null;

    //    Jan 18, 2022  -
    RecyclerView preTripRecyclerView;
    private PreTripAdapter preTripAdapter;
    List<PretripModel> preTripList;
    int newlyAddedEntryId = -1;
    ArrayList<PretripModel> multiDeletePretripArrayList;
    User user;
    int lastHighLightedPosition = -1;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        checkDeviceType();
        View view = inflater.inflate(R.layout.fragment_pretrip_list, container, false);

        setAppbarTitle();
        setIds(view);
        setWidgetsFromActivity();
        initialize();
        setListeners();

//        Jan 18, 2022  -   settings recycler view
        setUpPreTripRecyclerView();

//        Dec 13, 2022  -
        setupDateRangeRecyclerView();
        populateDateRangeFilterList();

        return view;
    }

    void checkDeviceType() {
        isTablet = getResources().getBoolean(R.bool.isTablet);
    }

    @Override
    public void onResume() {
        super.onResume();
        initializeFromActivity();

//        Jan 18, 2022  -   getPreTripList
        getPreTripList();
    }

    void getPreTripList() {
        Log.d(TAG, "getPreTripList: ");
        preTripList.clear();
        boolean newEntrySelection = false;

        ArrayList<PretripModel> pretripList = businessRules.getPreTrips();
        Log.d(TAG, "getPreTripList: preTripList: " + pretripList);
        int preTripListSize = pretripList.size();
        setNoRecordFound(preTripListSize);

        Log.d(TAG, "getPreTripList: preTripListSize: " + preTripListSize);
        for (int i = 0; i < pretripList.size(); i++) {

            PretripModel pretripModel = pretripList.get(i);
            pretripModel.setSelected(false);
            if (pretripModel.getId().equalsIgnoreCase("" + newlyAddedEntryId)) {
                pretripModel.setSelected(true);
                newEntrySelection = true;
            }
            preTripList.add(pretripModel);
        }

        sortPreTripListByDate(preTripList);

        if (!newEntrySelection) {
            if (preTripList.size() > 0) {
                preTripList.get(0).setSelected(true);
                newlyAddedEntryId = -1;
            }
        }

        preTripAdapter.populateFilterArrayList(preTripList);
        preTripAdapter.notifyDataSetChanged();

        openLatestPreTripDetail();
    }


    void sortPreTripListByDate(List<PretripModel> preTripList) {
        Collections.sort(preTripList, new Comparator<PretripModel>() {
            DateFormat formatWithSlash = new SimpleDateFormat("MM/dd/yyyy");
            DateFormat formatWithDash = new SimpleDateFormat("yyyy-MM-dd");

            @Override
            public int compare(PretripModel lhs, PretripModel rhs) {
                try {
                    String lhsValue = lhs.getDateTime();
                    String rhsValue = rhs.getDateTime();
//                    Log.d(TAG, "compare: lhs: " + lhsValue + " rhs: " + rhsValue);
                    if (lhsValue != null && rhsValue != null) {
                        if (lhsValue.contains("-") && rhsValue.contains("-")) {
                            return formatWithDash.parse(rhs.getDateTime()).compareTo(formatWithDash.parse(lhs.getDateTime()));
                        } else if (lhsValue.contains("/") && rhsValue.contains("/")) {
                            return formatWithSlash.parse(rhs.getDateTime()).compareTo(formatWithSlash.parse(lhs.getDateTime()));
                        } else if (lhsValue.contains("-") && rhsValue.contains("/")) {
                            return formatWithSlash.parse(rhs.getDateTime()).compareTo(formatWithDash.parse(lhs.getDateTime()));
                        } else if (lhsValue.contains("/") && rhsValue.contains("-")) {
                            return formatWithDash.parse(rhs.getDateTime()).compareTo(formatWithSlash.parse(lhs.getDateTime()));
                        }
                    }
                    return -1;
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }

    void openLatestPreTripDetail() {
        Log.d(TAG, "openLatestPreTripDetail: ");

        if (preTripList.size() <= 0) {
            return;
        }

        PretripModel intentPreTripModel = preTripList.get(0);
        if (isTablet) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("dataModel", intentPreTripModel);
            bundle.putBoolean("isEditable", false);
            loadPreTripReportFragment(new PreTripReportFragmentUpdate(), bundle);
        } else {
            openPreTripReportActivity(intentPreTripModel);
        }
    }


    protected int getBottomLayoutId() {
        return R.layout.bottom_bar_refresh_loading;
    }

    private void openDetail(Fragment fragment, Bundle bundle) {
        Log.d(TAG, "onClick: loadMainFragment: ");
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.pretrip_frame_layout, fragment).commit();
    }

    void openFromMobile(View v) {
        Intent intent = new Intent(getActivity(), DvirDtlActivity.class);
        Object message = v.getTag();
        DvirListAdapter.ListItemDvir item = null;

        if (message != null && message instanceof DvirListAdapter.ListItemDvir)
            //            idRmsRecords = (Long) v.getTag();
            item = (DvirListAdapter.ListItemDvir) message;

        BusHelperRmsCoding.RmsRecords recordIdent = null;

        Log.d(TAG, "DvirDtlActivity: onClick(): item: " + item);
        if (item != null) {
            recordIdent = new BusHelperRmsCoding.RmsRecords(item.getIdRmsRecords(), item.getObjectId(), item.getObjectType(), null, -1);
            Log.d(TAG, "DvirDtlActivity: onClick(): getIdRmsRecords: " + item.getIdRmsRecords() +
                    " getObjectId: " + item.getObjectId() + "getObjectType: " + item.getObjectType());
        } else {
            recordIdent = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);
        }

        lastSelectedItem = item;
        intent.putExtra(EXTRA_MESSAGE_DVIR_RMS_DVIR_IDENT, recordIdent);
        startActivity(intent);
    }

    void openFromMobile(DvirListAdapter.ListItemDvir item) {
        lastSelectedItem = item;
        Intent intent = new Intent(getActivity(), DvirDtlActivity.class);
        BusHelperRmsCoding.RmsRecords recordIdent = null;

        Log.d(TAG, "DvirDtlActivity: onClick(): item: " + item);
        if (item != null) {
            recordIdent = new BusHelperRmsCoding.RmsRecords(item.getIdRmsRecords(), item.getObjectId(), item.getObjectType(), null, -1);
            Log.d(TAG, "DvirDtlActivity: onClick(): getIdRmsRecords: " + item.getIdRmsRecords() +
                    " getObjectId: " + item.getObjectId() + "getObjectType: " + item.getObjectType());
        } else {
            recordIdent = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);
        }
        intent.putExtra(EXTRA_MESSAGE_DVIR_RMS_DVIR_IDENT, recordIdent);
        startActivity(intent);
    }

    void openFromTablet(View view) {
//        Intent intent = new Intent(getActivity(), DvirDtlActivity.class);
        Log.d(TAG, "openFromTablet: view: " + view);
        Object message = view.getTag();
        DvirListAdapter.ListItemDvir item = null;

        if (message != null && message instanceof DvirListAdapter.ListItemDvir)
            //            idRmsRecords = (Long) v.getTag();
            item = (DvirListAdapter.ListItemDvir) message;

        Log.d(TAG, "openFromTablet: item: " + item);

        if (item == null) {
            return;
        }
        Log.d(TAG, "openFromTablet: idRmsRecord: " + item.getIdRmsRecords());

        lastSelectedItem = item;
        Bundle bundle = new Bundle();
        bundle.putString("recordIdent", "" + item);
        bundle.putString("isPreviewOnly", "" + true);
        openDetail(new DvirReport(), bundle);
    }

    void openFromTablet(DvirListAdapter.ListItemDvir item) {
        Log.d(TAG, "openFromTablet: ");
//        Intent intent = new Intent(getActivity(), DvirDtlActivity.class);
//        Log.d(TAG, "openFromTablet: view: " + view);
//        Object message = view.getTag();
//        DvirListAdapter.ListItemDvir item = null;
//
//        if (message != null && message instanceof DvirListAdapter.ListItemDvir)
//            //            idRmsRecords = (Long) v.getTag();
//            item = (DvirListAdapter.ListItemDvir) message;
//
//        Log.d(TAG, "openFromTablet: item: " + item);

        if (item == null) {
            return;
        }

        lastSelectedItem = item;
        Log.d(TAG, "openFromTablet: idRmsRecord: " + item.getIdRmsRecords());
        Bundle bundle = new Bundle();
        bundle.putString("recordIdent", "" + item);
        bundle.putString("isPreviewOnly", "" + true);
        openDetail(new DvirReport(), bundle);
    }

    void editPretripFragment(DvirListAdapter.ListItemDvir item) {
        Log.d(TAG, "DvirReport: idRmsRecord: item: " + item);
        if (item == null) {
            return;
        }
        Log.d(TAG, "DvirReport: idRmsRecord: " + item.getIdRmsRecords());

        BusHelperRmsCoding.RmsRecords recordIdent = null;

        Log.d(TAG, "DvirDtlActivity: onClick(): item: " + item);
        if (item != null) {
            recordIdent = new BusHelperRmsCoding.RmsRecords(item.getIdRmsRecords(), item.getObjectId(), item.getObjectType(), null, -1);
            Log.d(TAG, "DvirDtlActivity: onClick(): getIdRmsRecords: " + item.getIdRmsRecords() +
                    " getObjectId: " + item.getObjectId() + "getObjectType: " + item.getObjectType());
        } else {
            recordIdent = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);
        }
//        intent.putExtra(EXTRA_MESSAGE_DVIR_RMS_DVIR_IDENT, recordIdent);

        Log.d(TAG, "editPretripFragment: recordIdent: " + recordIdent);
        Bundle bundle = new Bundle();
//        bundle.putString("recordIdent", "" + recordIdent);
        bundle.putLong("idRecord", recordIdent.getIdRecord());
        bundle.putString("objectId", "" + recordIdent.getObjectId());
        bundle.putString("objectType", "" + recordIdent.getObjectType());
        openDetail(new EditPretripFragment(new EditPretripFragment.PreTripOptionsSelection() {
            @Override
            public void onCancelCalled() {
                Log.d(TAG, "onCancelCalled: ");
                editPretrip.setVisibility(View.VISIBLE);
                openFromTablet(lastSelectedItem);
            }

            @Override
            public void onDeleteCalled() {

            }
        }), bundle);
    }


    void setAppbarTitle() {
//        Dec 13, 2022  -   We are setting appbar at MainMenuActivity so no need to override here
//        ((MainMenuActivity) getActivity()).setActionBarTitle(getString(R.string.dvirs_title));
    }

    void setIds(View view) {

        searchET = view.findViewById(R.id.et_search);
//        filterBtn = view.findViewById(R.id.filter_btn);
        filterBtn = view.findViewById(R.id.filter_btn_bottom_bar);
        noRecordFound = view.findViewById(R.id.noRecordFound);

        refreshBtn = view.findViewById(R.id.refresh_btn);

        btnBluetoothOn = getActivity().findViewById(R.id.btn_bluetooth_on);
        leftSideTitle = getActivity().findViewById(R.id.left_side_title);
        mainTitle = getActivity().findViewById(R.id.main_title_for_pretrip);
        searchClear = view.findViewById(R.id.iv_clear_search);

//        bottomBar = view.findViewById(R.id.bottom_bar);
        cancelSearch = view.findViewById(R.id.cancelSearch);

        dateRangeFilterLayout = view.findViewById(R.id.date_range_filter_layout);
        dateRangeRecyclerView = view.findViewById(R.id.date_range_recycler_view);
        cancelSearchRangeFilter = view.findViewById(R.id.cancel_search_range_filter);

//        toolbarAddButtonTablet = getActivity().findViewById(R.id.toolbar_add_button_tablet);
//        toolbarAddButtonTablet.setVisibility(View.VISIBLE);
        if (isTablet) {
//            addButton = getActivity().findViewById(R.id.toolbar_add_button_pretrip);
            addButton = view.findViewById(R.id.add_icon);
        } else {
            addButton = getActivity().findViewById(R.id.toolbar_add_button);
        }

        preTripRecyclerView = view.findViewById(R.id.recycler_view_list);

    }

    void setWidgetsFromActivity() {
        if (isTablet) {
            setIdsFromActivity();
            initializeFromActivity();
            setListenerFromActivity();
        }
    }

    void setIdsFromActivity() {
        editPretrip = getActivity().findViewById(R.id.edit_pretrip);
//        emailPretrip = getActivity().findViewById(R.id.email_pretrip);
        cancel = getActivity().findViewById(R.id.main_app_bar_cancel);
        save = getActivity().findViewById(R.id.main_app_bar_save);

        sync = getActivity().findViewById(R.id.main_app_bar_sync);
        sync.setVisibility(View.VISIBLE);

//        Jan 19, 2022  -   Show Delete Option
        homeAppBarLeftSideTitle = getActivity().findViewById(R.id.left_side_title);
        homeAppBarLeftSideTitle.setVisibility(View.VISIBLE);
        homeAppBarLeftSideTitle.setText("Pretrip");
        deleteMultipleEntries = getActivity().findViewById(R.id.delete_multiple_receipts);


    }

    void initializeFromActivity() {
        editPretrip.setVisibility(View.VISIBLE);
//        cancel.setVisibility(View.GONE);
//        emailPretrip.setVisibility(View.GONE);

    }

    void setListenerFromActivity() {
        editPretrip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isTablet) {
                    editPretripFragment(lastSelectedItem);
//                    Toast.makeText(getContext(), "Working...", Toast.LENGTH_SHORT).show();
                } else {
                    openFromMobile(lastSelectedItem);
                }
            }
        });

    }

    void initialize() {

//        Dec 12, 2022  -   We are setting the widgets at MainMenuActivity so we don't need to set them here
//        if (isTablet) {
//            leftSideTitle.setVisibility(View.VISIBLE);
//            mainTitle.setVisibility(View.VISIBLE);
//            addButton.setVisibility(View.VISIBLE);
//            leftSideTitle.setText("Pretrips");
//            mainTitle.setText("");
//        }

        materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker();
        materialDateBuilder.setTitleText("SELECT A DATE");
//        materialDateBuilder.setTheme(R.style.ThemeOverlay_App_MaterialCalendar);
        materialDatePicker = materialDateBuilder.build();

        filterBtn.setVisibility(View.VISIBLE);
        btnBluetoothOn.setVisibility(View.GONE);
        addButton.setVisibility(View.VISIBLE);
        cancelSearch.setVisibility(View.GONE);
//        addButton.setOnClickListener(this);

//        July 25, 2022 -   Roy said, remove it in mail July 23, 2022 (As in Fuel receipt)
//        bottomBar.setVisibility(View.GONE);

        today = MaterialDatePicker.todayInUtcMilliseconds(); // Select current date
        month = MaterialDatePicker.thisMonthInUtcMilliseconds(); // Select this month first date
        mCalendar = Calendar.getInstance();

        dateRangeModelList = new ArrayList<>();

        preTripList = new ArrayList<>();
        multiDeletePretripArrayList = new ArrayList<>();
        user = BusinessRules.instance().getAuthenticatedUser();
    }


    void setListeners() {

        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openFromMobile(view);
            }
        });

        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                showMaterialDataPicker();
//                openDateFilterDialog();
                dateRangeFilterLayout.setVisibility(View.VISIBLE);
            }
        });

        materialDatePicker.addOnPositiveButtonClickListener(
                new MaterialPickerOnPositiveButtonClickListener() {
                    @SuppressLint("SetTextI18n")
                    @Override
                    public void onPositiveButtonClick(Object selection) {


                        // if the user clicks on the positive
                        // button that is ok button update the
                        // selected date
//                        mShowSelectedDateText.setText("Selected Date is : " + materialDatePicker.getHeaderText());
                        Log.d(TAG, "onPositiveButtonClick: Selected Date is : " + materialDatePicker.getHeaderText());
                        // in the above statement, getHeaderText
                        // is the selected date preview from the
                        // dialog
                    }
                });

        searchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchET.setText("");
            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
//                String searchedString = searchET.getText().toString().toLowerCase(Locale.getDefault());
//                mAdapter.search(searchedString);

                String searchedString = s.toString().toLowerCase();
                if (preTripAdapter != null) {
                    preTripAdapter.search(searchedString);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
        });

        dateRangeFilterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateRangeFilterLayout.setVisibility(View.GONE);
            }
        });

        cancelSearchRangeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateRangeFilterLayout.setVisibility(View.GONE);
            }
        });

        deleteMultipleEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: deleteMultipleEntries: ");
                deleteSelectedPreTrip();
            }
        });

    }

    void openDateFilterDialog() {
        dateFilterDialog = new DateFilterDialog(getContext(), new DateFilterDialog.DateFilterInterface() {
            @Override
            public void selectedDate(String selectedDate) {

                setFilterIcon(true);
                applyDateFilter(selectedDate);
                dateFilterDialog.dismiss();
            }

            @Override
            public void onCancelSelection() {
                dateFilterDialog.dismiss();
            }
        });
        dateFilterDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        dateFilterDialog.show();
    }

    void setFilterIcon(boolean isFilterApplied) {
//        if (isFilterApplied) {
//            filterBtn.setImageResource(R.drawable.filter_icon_remove);
//        } else {
//            filterBtn.setImageResource(R.drawable.filter_icon);
//        }
    }

    String getStartRange(long startingTimeStamp) {
        return DateUtils.getDateTime(startingTimeStamp, DateUtils.FORMAT_DATE_YYYY_MM_DD) + " 00:00:00.000";
    }

    String getEndRange(long endingTimeStamp) {
        return DateUtils.getDateTime(endingTimeStamp, DateUtils.FORMAT_DATE_YYYY_MM_DD) + " 23:59:59.000";
    }

    void applyDateFilter(String selectedDateValue) {

        if (selectedDateValue.equalsIgnoreCase("Today")) {
            rangeStartDate = getStartRange(today);
            rangeEndDate = getEndRange(today);
        } else if (selectedDateValue.equalsIgnoreCase("Yesterday")) {
            rangeStartDate = getStartRange(DateUtils.getYesterdayDateTimeInTimeStamp(true));
            rangeEndDate = getEndRange(DateUtils.getYesterdayDateTimeInTimeStamp(false));
        } else if (selectedDateValue.equalsIgnoreCase("This Week")) {
            rangeStartDate = getStartRange(DateUtils.getCurrentWeek(mCalendar, true));
            rangeEndDate = getEndRange(DateUtils.getCurrentWeek(mCalendar, false));
        } else if (selectedDateValue.equalsIgnoreCase("Last Week")) {
            rangeStartDate = getStartRange(DateUtils.getLastWeek(mCalendar, true));
            rangeEndDate = getEndRange(DateUtils.getLastWeek(mCalendar, false));
        } else if (selectedDateValue.equalsIgnoreCase("This Month")) {
            rangeStartDate = getStartRange(month);
            rangeEndDate = getEndRange(DateUtils.getThisMonth(mCalendar, false));
        } else if (selectedDateValue.equalsIgnoreCase("Last Month")) {
            rangeStartDate = getStartRange(DateUtils.getLastMonth(true));
            rangeEndDate = getEndRange(DateUtils.getLastMonth(false));
        } else if (selectedDateValue.equalsIgnoreCase("This Year")) {
            rangeStartDate = getStartRange(DateUtils.getThisYear(true));
            rangeEndDate = getEndRange(DateUtils.getThisYear(false));
        } else if (selectedDateValue.equalsIgnoreCase("This Quarter")) {
            rangeStartDate = getStartRange(DateUtils.getFirstDayOfQuarter().getTime());
            rangeEndDate = getEndRange(DateUtils.getLastDayOfQuarter().getTime());
        } else if (selectedDateValue.equalsIgnoreCase("Last Quarter")) {
            rangeStartDate = getStartRange(DateUtils.getFirstDayOfPreviousQuarter().getTime());
            rangeEndDate = getEndRange(DateUtils.getLastDayOfPreviousQuarter().getTime());
        } else if (selectedDateValue.equalsIgnoreCase("All")) {
            rangeStartDate = "";
            rangeEndDate = "";
            setFilterIcon(false);
        }

        UiHelperDvirList.instance().runRefreshTask(PreTripListFragment.this, rangeStartDate, rangeEndDate);
    }

    void setupDateRangeRecyclerView() {
        Log.d(TAG, "setUpRecyclerViewLinearLayoutForChat: ");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        dateRangeRecyclerView.setLayoutManager(linearLayoutManager);

        dateRangeFilterAdapter = new DateRangeFilterAdapter(dateRangeModelList, getContext(), new DateRangeFilterAdapter.DateRangeInterface() {
            @Override
            public void onListItemClicked(int position) {
                Log.d(TAG, "onListItemClicked: index: " + position);
                dateRangeFilterLayout.setVisibility(View.GONE);
                applyDateFilter(dateRangeModelList.get(position).getDate());
            }
        });
        dateRangeRecyclerView.setAdapter(dateRangeFilterAdapter);
    }

    void populateDateRangeFilterList() {
        dateRangeModelList.clear();
        String[] dateRange = getResources().getStringArray(R.array.date_range_filter);
        for (int i = 0; i < dateRange.length; i++) {
            DateRangeModel dateRangeModel = new DateRangeModel();
            String date = dateRange[i];
            dateRangeModel.setDate(date);
            if (date.equals("All")) {
                dateRangeModel.setSelected(true);
            } else {
                dateRangeModel.setSelected(false);
            }
            dateRangeModelList.add(dateRangeModel);
        }
        dateRangeFilterAdapter.notifyDataSetChanged();
    }

    void setNoRecordFound(int size) {
        Log.d(TAG, "onNoRecordFound: setNoRecordFound: size: " + size);
        if (size == 0) {
            showNoRecordFound(true);
        } else {
            showNoRecordFound(false);
        }
    }

    void showNoRecordFound(boolean show) {
        Log.d(TAG, "onNoRecordFound: showNoRecordFound: show: " + show);
        if (show) {
            noRecordFound.setVisibility(View.VISIBLE);
        } else {
            noRecordFound.setVisibility(View.GONE);
        }
    }

    public void removeYourFragment() {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
//        if (lastOpenedFragment != null) {
//            transaction.remove(lastOpenedFragment);
//            transaction.commit();
//            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
//            lastOpenedFragment = null;
//        }
    }

    void setUpPreTripRecyclerView() {
        Log.d(TAG, "setUpTollReceiptRecyclerView: ");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        preTripRecyclerView.setLayoutManager(linearLayoutManager);

        preTripAdapter = new PreTripAdapter(preTripList, getContext(), new PreTripAdapter.PretripInterface() {
            @Override
            public void onListItemClicked(int position, java.util.List<PretripModel> list) {
                Log.d(TAG, "onListItemClicked: truckNumber: " + list.get(position));
                lastHighLightedPosition = position;
                setBackgroundOfSelectedItem(position, list);

                if (isTablet) {
/*                  bundle.putString("idRmsRecords", list.get(position).getId());
                    bundle.putString("objectId", list.get(position).getObjectId());
                    bundle.putString("objectType", list.get(position).getObjectType());*/

                    Bundle bundle = new Bundle();
                    PretripModel intentPreTripModel = list.get(position);
                    bundle.putSerializable("dataModel", intentPreTripModel);
                    bundle.putBoolean("isEditable", false);
//                    loadPreTripReportFragment(new PreTripReportFragment(), bundle);
                    loadPreTripReportFragment(new PreTripReportFragmentUpdate(), bundle);
                } else {
                    openPreTripReportActivity(list.get(position));
                }
            }

            @Override
            public void onNoRecordFound(boolean isNoRecordFound) {
                if (isNoRecordFound) {
//                    Dec 14, 2022  -   We mean record is found so hide no record found text
                    setNoRecordFound(0);
                    removeYourFragment();
                } else {
                    setNoRecordFound(1);
                }
            }

            @Override
            public void onItemLongClick(int position, List<PretripModel> list, ArrayList<PretripModel> multiDeleteArrayList) {
                multiDeletePretripArrayList = multiDeleteArrayList;
                Log.d(TAG, "onItemLongClick: multiDeletePretripArrayList: " + multiDeletePretripArrayList.size());
                if (multiDeleteArrayList.size() == 0) {
                    deleteMultipleEntries.setVisibility(View.GONE);
                } else {
                    deleteMultipleEntries.setVisibility(View.VISIBLE);
                }
                Log.d(TAG, "onItemLongClick: isVisible: " + (deleteMultipleEntries.getVisibility() == View.VISIBLE));

                if (isTablet) {
//                    Bundle bundle = new Bundle();
//                    bundle.putParcelable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
//                    openTollReceiptDetail(bundle);
                }
            }
        });
        preTripRecyclerView.setAdapter(preTripAdapter);
    }

    //    Jan 02, 2022  -   this function will delete multi selection entries
    void deleteSelectedPreTrip() {
        Log.d(TAG, "deleteSelectedPreTrip: deleteSelectedPreTrip: size: " + multiDeletePretripArrayList.size());

        for (int i = 0; i < multiDeletePretripArrayList.size(); i++) {
            PretripModel preTripModelToDelete = multiDeletePretripArrayList.get(i);
            Log.d(TAG, "deleteSelectedPreTrip: objectId: " + preTripModelToDelete.getObjectId()
                    + " objectType: " + preTripModelToDelete.getObjectType());

            if (preTripModelToDelete.getObjectId().isEmpty()) {
                deleteFromPretripDB(preTripModelToDelete.getId());
            } else {
                deleteRecord(preTripModelToDelete.getId(), "" + preTripModelToDelete.getObjectId(), preTripModelToDelete.getObjectType());
                deleteFromPretripDB(preTripModelToDelete.getId());
            }
        }
    }

    void refreshScreen() {
        Log.d(TAG, "onDeleteCalled: delete toll receipt: ");
        getPreTripList();
    }

    void deleteFromPretripDB(String recordId) {
        int isDeleted = businessRules.deleteFuelReceiptItem(recordId);
        Log.d(TAG, "onErrorResponse: isDeleted: " + isDeleted);
        if (isDeleted == 1) {
            multiDeletePretripArrayList.clear();
            refreshScreen();
        }
    }

    void deleteRecord(String id, String objectId, String objectType) {
        String usernamePasswordObjectIdObjectTypeCombine = user.getLogin() + "/" + user.getPassword() + "/"
                + objectId + "/" + objectType;
        Log.d(TAG, "deleteRecord: usernamePasswordObjectIdObjectTypeCombine: " + usernamePasswordObjectIdObjectTypeCombine);

        String deleteAPI = Rms.APIToDeleteRecord + usernamePasswordObjectIdObjectTypeCombine;

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, deleteAPI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: response: " + response);
                refreshScreen();
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {

                if (error instanceof TimeoutError || error instanceof NoConnectionError) {
                    Log.d(TAG, "onErrorResponse: timeOutError: noConnectionError: ");
                } else if (error instanceof AuthFailureError) {
                    Log.d(TAG, "onErrorResponse: AuthFailureError: ");
                } else if (error instanceof ServerError) {
                    Log.d(TAG, "onErrorResponse: ServerError: ");
                } else if (error instanceof NetworkError) {
                    Log.d(TAG, "onErrorResponse: NetworkError: ");
                } else if (error instanceof ParseError) {
                    Log.d(TAG, "onErrorResponse: ParseError: ");
                }

                Log.d(TAG, "deleteRecord: onErrorResponse: error: " + error.getMessage());
            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(120), //After the set time elapses the request will timeout
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    void setBackgroundOfSelectedItem(int position, java.util.List<PretripModel> list) {
        for (int i = 0; i < list.size(); i++) {
            if (i == position) {
                list.get(i).setSelected(true);
            } else {
                list.get(i).setSelected(false);
            }
        }
        preTripAdapter.notifyDataSetChanged();
    }

    private void loadPreTripReportFragment(Fragment fragment, Bundle bundle) {
        Log.d(TAG, "onClick: loadMainFragment: ");
//        lastOpenedFragment = fragment;

        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.pretrip_frame_layout, fragment).commit();
    }

    private void openPreTripReportActivity(PretripModel pretripModel) {
        Log.d(TAG, "onClick: loadMainFragment: ");
        Intent intent = new Intent(getContext(), PreTripReport.class);
//        intent.putExtra("idRmsRecords", pretripModel.getId());
//        intent.putExtra("idRmsRecords", pretripModel.getRecordId());
//        intent.putExtra("objectId", pretripModel.getObjectId());
//        intent.putExtra("objectType", pretripModel.getObjectType());
        startActivity(intent);
    }


}