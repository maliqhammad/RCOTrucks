package com.rco.rcotrucks.activities.dvir;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.DateValidatorPointForward;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.BaseCoordinatorFragment;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.dvir.fragments.DvirReport;
import com.rco.rcotrucks.activities.dvir.fragments.EditPretripFragment;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.DateRangeFilterAdapter;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.dialog.DateFilterDialog;
import com.rco.rcotrucks.model.DateRangeModel;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

//public class DvirListFragment extends BaseCoordinatorFragment implements View.OnClickListener {
public class DvirListFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = DvirListFragment.class.getSimpleName();
    public static final String EXTRA_MESSAGE_DVIR_RMS_DVIR_IDENT = "com.rco.rcotrucks.dvirident";
    private RecyclerView recyclerView;
    private EditText searchET;
    //    private RecyclerView.Adapter mAdapter;
    private DvirListAdapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    MaterialDatePicker materialDatePicker;
    MaterialDatePicker.Builder<Pair<Long, Long>> materialDateBuilder;
    Pair<Long, Long> selectDateRangeForToday;

    ConstraintLayout btnBluetoothOn, dateFilterLayout, dateRangeFilterLayout;
    ImageView refreshBtn, addButton, searchClear, cancelSearch, toolbarAddButtonTablet;
    TextView leftSideTitle, mainTitle, cancelSearchRangeFilter, editPretrip, noRecordFound, filterBtn, cancel, save;

    Calendar mCalendar;
    DateFilterDialog dateFilterDialog;
    String rangeStartDate = "", rangeEndDate = "";
    long today = 0, month = 0;
    boolean isTablet = false;

    private BusHelperRmsCoding.RmsRecords identRmsRecords;
    public static final int REQUESTCODE_PREVIEW_REPORT = 2;

    RecyclerView dateRangeRecyclerView;
    DateRangeFilterAdapter dateRangeFilterAdapter;
    List<DateRangeModel> dateRangeModelList;
    DvirListAdapter.ListItemDvir lastSelectedItem = null;
    Fragment lastOpenedFragment;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        isTablet = getResources().getBoolean(R.bool.isTablet);
        View view = inflater.inflate(R.layout.fragment_pretrip, container, false);

        setAppbarTitle();
        setIds(view);
        setWidgetsFromActivity();
        initialize();
        setListeners();

//        Dec 13, 2022  -
        setupDateRangeRecyclerView();
        populateDateRangeFilterList();
        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        initializeFromActivity();
        UiHelperDvirList.instance().runRefreshTask(this, "", "");
    }

    protected int getBottomLayoutId() {
        return R.layout.bottom_bar_refresh_loading;
    }

    public void loadContentView() {
        loadRecyclerView();
    }


    private void loadRecyclerView() {
        Log.d(TAG, "syncDvirItems: loadRecyclerView: ");
        String searchText = searchET.getText().toString();

//        List<DvirListAdapter.ListItemDvir> listDvirItems = busRules.getListDvirs();
        List<DvirListAdapter.ListItemDvir> listDvirItems = BusHelperDvir.getListDvirs();
        Log.d(TAG, "syncDvirItems: loadRecyclerView: searchText=" + searchText
                + ", listDvirItems.size()=" + listDvirItems.size() + ", listDvirItems: " + listDvirItems);

        for (int i = 0; i < listDvirItems.size(); i++) {
            Log.d(TAG, "syncDvirItems: loadRecyclerView: index: " + i + " value: " + listDvirItems.get(i));
        }

        if (listDvirItems == null) {
            return;
        }

//        Dec 15, 2022  -   If list is empty then don't show detail of any pretrip
        if (listDvirItems.size() == 0) {
            setNoRecordFound(0);
            removeYourFragment();
        } else {
            if (isTablet) {
                openFromTablet(listDvirItems.get(0));
            }
            setNoRecordFound(1);
        }


//        else
//            List<DvirListAdapter.ListItemFuelReceipt> listItems =  AdapterUtils.getItems(listDvirItems, searchText);

//        Log.d(TAG, "loadRecyclerView() after filter/sort, listItems.size()=" + listItems.size() + ", listItems: " + listItems);

        Log.d(TAG, "syncDvirItems: loadRecyclerView: mAdapter: " + mAdapter);
        if (mAdapter == null) {

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
//        recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            List<DvirListAdapter.ListItemDvir> listItems = new ArrayList<>();

//            listItems =  AdapterUtils.getItems(listDvirItems, searchText);
            List<DvirListAdapter.ListItemDvir> filteredListDvirItems = AdapterUtils.filterItems(listDvirItems, listItems, searchText);

            Log.d(TAG, "syncDvirItems: loadRecyclerView: after filter/sort, searchText=" + searchText
                    + ", listDvirItems.size()=" + listDvirItems.size() + ", listItems.size()="
                    + listItems.size() + ", listItems: " + listItems);

            // specify an adapter
            mAdapter = new DvirListAdapter(getActivity(), listItems, this, new DvirListAdapter.PretripInterface() {
                @Override
                public void onListItemClicked(int position, List<DvirListAdapter.ListItemDvir> myDataset) {
                    if (isTablet && filteredListDvirItems != null && filteredListDvirItems.size() > 0) {
                        openFromTablet(filteredListDvirItems.get(0));
                    }
                }

                @Override
                public void onNoRecordFound(boolean isNoRecordFound) {
                    Log.d(TAG, "onNoRecordFound: isNoRecordFound: " + isNoRecordFound);
                    if (isNoRecordFound) {
//                    Dec 14, 2022  -   We mean record is found so hide no record found text
                        setNoRecordFound(0);
                        removeYourFragment();
                    } else {
                        setNoRecordFound(1);
                    }
                }
            });

            recyclerView.setAdapter(mAdapter);//        initActionbar();
        } else {
            List<DvirListAdapter.ListItemDvir> filteredListDvirItems = AdapterUtils.filterItems(listDvirItems, mAdapter.getmDataset(), searchText);
            //        Dec 13, 2022  -   we wanted to show detail of first list if its a tablet
            if (isTablet && filteredListDvirItems != null && filteredListDvirItems.size() > 0) {
                openFromTablet(filteredListDvirItems.get(0));
            }

            Log.d(TAG, "syncDvirItems: loadRecyclerView: else: after filter/sort, mAdapter.getmDataset().size()="
                    + mAdapter.getmDataset().size() + ", mAdapter.getmDataset(): " + mAdapter.getmDataset());
            for (int i = 0; i < mAdapter.getmDataset().size(); i++) {
                Log.d(TAG, "loadRecyclerView: else: index: " + i + " value: " + mAdapter.getmDataset().get(i));
            }

            mAdapter.notifyDataSetChanged();
        }

    }

    private void openDetail(Fragment fragment, Bundle bundle) {
        Log.d(TAG, "onClick: loadMainFragment: ");
        lastOpenedFragment = fragment;
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.pretrip_frame_layout, fragment).commit();
    }

    @Override
    public void onClick(View view) {
        if (isTablet) {
            editPretrip.setVisibility(View.VISIBLE);
            cancel.setVisibility(View.GONE);
            save.setVisibility(View.GONE);
            openFromTablet(view);
        } else {
            openFromMobile(view);
        }
    }

    public void startDvirReportActivity(int requestCode, boolean isPreviewOnly) {
//        Intent intent = new Intent(this, DvirReportActivity.class);
//        intent.putExtra("rmsId", "" + rmsId);
//        intent.putExtra("isPreviewOnly", "" + isPreviewOnly);
//        startActivityForResult(intent, requestCode);

    }

    void runUIHelperDvirRefreshTask(DvirListAdapter.ListItemDvir item) {
//        identRmsRecords = (BusHelperRmsCoding.RmsRecords) item;
//        UiHelperDvirDtl.instance().runRefreshTask(
//                getContext(), identRmsRecords.getIdRecord(), identRmsRecords.getObjectId(), identRmsRecords.getObjectType());
//                getContext(), identRmsRecords.getIdRecord(), identRmsRecords.getObjectId(), identRmsRecords.getObjectType());
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

        recyclerView = view.findViewById(R.id.recycler_view_list);
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
            addButton = getActivity().findViewById(R.id.toolbar_add_button_pretrip);
        } else {
            addButton = getActivity().findViewById(R.id.toolbar_add_button);
        }

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

//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: cancel: lastSelectedItem: "+lastSelectedItem);
//                openFromTablet(lastSelectedItem);
//            }
//        });
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
                if (mAdapter != null) {
                    mAdapter.search(searchedString);
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

        UiHelperDvirList.instance().runRefreshTask(DvirListFragment.this, rangeStartDate, rangeEndDate);
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
        if (lastOpenedFragment != null) {
            transaction.remove(lastOpenedFragment);
            transaction.commit();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_CLOSE);
            lastOpenedFragment = null;
        }
    }

    public interface PretripInterface {
        public void onListItemClicked(int position, java.util.List<ReceiptModel> list);

        public void onNoRecordFound(boolean isNoRecordFound);
    }
}