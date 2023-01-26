package com.rco.rcotrucks.activities.fuelreceipts.fragments;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.BaseCoordinatorFragment;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.utils.UiHelperFuelReceiptList;
import com.rco.rcotrucks.activities.fuelreceipts.activities.CreateTollReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.activities.FuelReceiptDtlActivity;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.CalendarAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.FuelReceiptListAdapter;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.dialog.DateFilterDialog;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class FuelReceiptListFragment extends BaseCoordinatorFragment implements View.OnClickListener {

    private static final String TAG = FuelReceiptListFragment.class.getSimpleName();
    public static final String EXTRA_MESSAGE_FUEL_RECEIPT_IDENT = "com.rco.rcotrucks.fuelreceiptident";
    private RecyclerView recyclerView;
    private FuelReceiptListAdapter mAdapter;

    ConstraintLayout btnBluetoothOn, dateFilterLayout;
    ImageView addButton, searchClear, refreshBtn, filterBtn, cancelSearch;
    EditText searchBox;
    LinearLayout bottomBar;
    TextView cancelDateFilter, removeDateFilter;

    MaterialDatePicker.Builder<Pair<Long, Long>> materialDateBuilder;
    MaterialDatePicker<Pair<Long, Long>> materialDatePicker;
    long today = 0, month = 0;

    RecyclerView calendarRecyclerView;

    Calendar mCalendar;
    DateFilterDialog dateFilterDialog;
    String rangeStartDate = "", rangeEndDate = "";


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setActionBarTitle();
        setIds(view);
        initialize();
        setListeners();

//        setUpDateFilterRecyclerView();
    }


    void setActionBarTitle() {
        Log.d(TAG, "setActionBarTitle: ");
        ((MainMenuActivity) getActivity()).setActionBarTitle(getString(R.string.fuel_receipts_title));
    }

    void setIds(View view) {
        Log.d(TAG, "setIds: ");
        btnBluetoothOn = getActivity().findViewById(R.id.btn_bluetooth_on);
        addButton = (ImageView) getActivity().findViewById(R.id.toolbar_add_button);
        calendarRecyclerView = getActivity().findViewById(R.id.date_filter_list_recyclerview);
        searchClear = view.findViewById(R.id.iv_clear_search);
        refreshBtn = view.findViewById(R.id.refresh_btn);
//        filterBtn = view.findViewById(R.id.filter_btn);

        cancelSearch = view.findViewById(R.id.cancelSearch);
        searchBox = view.findViewById(R.id.et_search);
        filterBtn = view.findViewById(R.id.filter_btn);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view_list);

//        searchET = ((EditText) view.findViewById(R.id.et_search));
        bottomBar = view.findViewById(R.id.bottom_bar);

        dateFilterLayout = getActivity().findViewById(R.id.date_filter_layout);
        cancelDateFilter = getActivity().findViewById(R.id.cancel_date_filter);
        removeDateFilter = getActivity().findViewById(R.id.remove_date_filter);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        btnBluetoothOn.setVisibility(View.GONE);
        addButton.setVisibility(View.VISIBLE);
        addButton.setOnClickListener(this);

        materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker();
        materialDateBuilder.setTitleText("SELECT A DATE");
        materialDatePicker = materialDateBuilder.build();

//        July 25, 2022 -   Roy said, remove it in mail July 23, 2022
        bottomBar.setVisibility(View.GONE);
        filterBtn.setVisibility(View.VISIBLE);
        cancelSearch.setVisibility(View.GONE);

        setFilterIcon(false);

        today = MaterialDatePicker.todayInUtcMilliseconds(); // Select current date
        month = MaterialDatePicker.thisMonthInUtcMilliseconds(); // Select this month first date
        mCalendar = Calendar.getInstance();
    }

    void setListeners() {
        Log.d(TAG, "setListeners: ");
        searchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchBox.setText("");
            }
        });

        searchBox.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: ");
//                loadRecyclerView("", "");
                String searchedString = searchBox.getText().toString().toLowerCase(Locale.getDefault());
                mAdapter.search(searchedString);
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: ");
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: ");

            }
        });


        refreshBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                UiHelperFuelReceiptList.instance().runRefreshTask(FuelReceiptListFragment.this, "", "");
            }
        });


        filterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDateFilterDialog();
            }
        });

        cancelDateFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDateFilterLayout(false);
            }
        });

        removeDateFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showDateFilterLayout(false);
                setFilterIcon(false);
                UiHelperFuelReceiptList.instance().runRefreshTask(FuelReceiptListFragment.this, "", "");
            }
        });


    }

    @Override
    public void onResume() {
        super.onResume();
        UiHelperFuelReceiptList.instance().runRefreshTask(FuelReceiptListFragment.this, "", "");
        UiUtils.closeKeyboard(searchBox);
    }

    @Override
    protected int getContentLayoutId() {
         return R.layout.activity_fuel_receipt_list;
    }

    protected int getToolbarId() {
        return R.layout.activity_toolbar_list_add;
    }

    @Override
    protected int getSecondaryToolbarId() {
//        return R.layout.search_bar;
        return R.layout.include_layout_search_generic;
    }

    protected int getBottomLayoutId() {
        return R.layout.bottom_bar_refresh_loading;
    }


    public void loadContentView() {
        loadRecyclerView("", "");
    }


    private void loadRecyclerView(String rangeStartDate, String rangeEndDate) {
        String strThis = "loadRecyclerView(), ";
        Log.d(TAG, strThis + "Start.");

        String searchText = searchBox.getText().toString();

//        List<FuelReceiptListAdapter.ListItemFuelReceipt> listFuelReceiptItems = busRules.getListFuelReceipts();
        List<FuelReceiptListAdapter.ListItemFuelReceipt> listFuelReceiptItems = BusHelperFuelReceipts.getListFuelReceipts();
        Log.d(TAG, "loadRecyclerView: listFuelReceiptItems: "+listFuelReceiptItems);
//        public static class ListItemFuelReceipt implements AdapterUtils.IAdapterItem<DateUtils.IDateConverter, ListItemFuelReceipt> {


//        Log.d(TAG, "loadRecyclerView() searchText=" + searchText
//                + ", listFuelReceiptItems.size()=" + (listFuelReceiptItems != null ? listFuelReceiptItems.size() : "(NULL)")
//                + ", listFuelReceiptItems: " + listFuelReceiptItems);

//        else
//            List<FuelReceiptListAdapter.ListItemFuelReceipt> listItems =  AdapterUtils.getItems(listFuelReceiptItems, searchText);

//        Log.d(TAG, "loadRecyclerView() after filter/sort, listItems.size()=" + listItems.size() + ", listItems: " + listItems);

        if (mAdapter == null) {
            Log.d(TAG, strThis + "Case: mAdapter == null, loading new adapter.");

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
//        recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            LayoutManager layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);
            List<FuelReceiptListAdapter.ListItemFuelReceipt> listItems = new ArrayList<>();

//            listItems =  AdapterUtils.getItems(listFuelReceiptItems, searchText);
            AdapterUtils.filterItems(listFuelReceiptItems, listItems, searchText);

//            Log.d(TAG, "loadRecyclerView() after filter/sort, searchText=" + searchText
//                    + ", listFuelReceiptItems.size()="
//                    + (listFuelReceiptItems != null ? listFuelReceiptItems.size() : "Null")
//                    + ", listItems.size()=" + (listItems != null ? listItems.size() : "Null"));

            // specify an adapter
            mAdapter = new FuelReceiptListAdapter(getActivity(), listItems, this);
            recyclerView.setAdapter(mAdapter);//        initActionbar();
        } else {
            Log.d(TAG, strThis + "Case: mAdapter != null, filtering items.");

            AdapterUtils.filterItems(listFuelReceiptItems, mAdapter.getmDataset(), searchText);

            Log.d(TAG, "loadRecyclerView() after filter/sort, mAdapter.getmDataset().size()="
                    + mAdapter.getmDataset().size() + ", mAdapter.getmDataset(): " + mAdapter.getmDataset());

            mAdapter.notifyDataSetChanged();
        }

        Log.d(TAG, strThis + "End.");
    }


    @Override
    public void onClick(View view) {

        openPreviousFunctionality(view);
//        openNextActivity(view);
    }

    void openPreviousFunctionality(View view) {
        Log.d(TAG, "openPreviousFunctionality: ");
//        List <BusinessRules.DbTableColumnInfo> listCols = busRules.getDbTableColumnInfo("TruckDVIRDetail");
//        for (BusinessRules.DbTableColumnInfo info : listCols)
//            Log.d(TAG, "onCLick() info: " + info.columnIndex + " " + info.name);
        Intent intent = new Intent(getActivity(), FuelReceiptDtlActivity.class);
//        Rms.RmsObjectIdType objectIdType = (Rms.RmsObjectIdType) v.getTag();
        Log.d(TAG, "onClick: TAG: " + view.getTag());
        Object message = view.getTag();
//        Long idRmsRecords = -1L;
        FuelReceiptListAdapter.ListItemFuelReceipt item = null;

        if (message != null && message instanceof FuelReceiptListAdapter.ListItemFuelReceipt)
//            idRmsRecords = (Long) v.getTag();
            item = (FuelReceiptListAdapter.ListItemFuelReceipt) message;
//        else
//            Log.d(TAG, "onClick() **** Unexpected view tag of type: "
//                    + (message != null ? message.getClass().getCanonicalName() : "(NULL)"));


        BusHelperRmsCoding.RmsRecords recordIdent = null;

        if (item != null)
            recordIdent
                    = new BusHelperRmsCoding.RmsRecords(item.getIdRmsRecords(), item.getObjectId(), item.getObjectType(), null, -1);
        else recordIdent = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);
//        Bundle message = new Bundle();
//        message.putString("objectid", objectIdType.getObjectId());
//        message.putString("objecttype", objectIdType.getObjectType());
//        message.putString(FuelReceiptListAdapter.KEY_IDRMSRECORDS, idRmsRecords);
        intent.putExtra(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, recordIdent);
        startActivity(intent);
    }


    void openNextActivity(View view) {

        Intent intent = new Intent(getActivity(), CreateTollReceipt.class);
        Object message = view.getTag();

        FuelReceiptListAdapter.ListItemFuelReceipt item = null;

        if (message != null && message instanceof FuelReceiptListAdapter.ListItemFuelReceipt)
            item = (FuelReceiptListAdapter.ListItemFuelReceipt) message;

        BusHelperRmsCoding.RmsRecords recordIdent = null;
        if (item != null) {
            recordIdent = new BusHelperRmsCoding.RmsRecords(item.getIdRmsRecords(),
                    item.getObjectId(), item.getObjectType(), null, -1);
        } else {
            recordIdent = new BusHelperRmsCoding.RmsRecords(-1L, null,
                    null, null, -1);
        }

        intent.putExtra(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, recordIdent);
        startActivity(intent);
    }

    void setFilterIcon(boolean isFilterApplied) {
        if (isFilterApplied) {
            filterBtn.setImageResource(R.drawable.filter_icon_remove);
        } else {
            filterBtn.setImageResource(R.drawable.filter_icon);
        }
    }


    void showDateFilterLayout(boolean showFilter) {
        if (showFilter) {
            dateFilterLayout.setVisibility(View.VISIBLE);
        } else {
            dateFilterLayout.setVisibility(View.GONE);
        }
    }

    String getStartRange(long startingTimeStamp) {
        return DateUtils.getDateTime(startingTimeStamp, DateUtils.FORMAT_DATE_YYYY_MM_DD) + " 00:00:00.000";
    }

    String getEndRange(long endingTimeStamp) {
        return DateUtils.getDateTime(endingTimeStamp, DateUtils.FORMAT_DATE_YYYY_MM_DD) + " 23:59:59.000";
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

    void applyDateFilter(String selectedDateValue) {

        if (selectedDateValue.equalsIgnoreCase("Today")) {
            rangeStartDate = getStartRange(today);
            rangeEndDate = getEndRange(today);
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

        UiHelperFuelReceiptList.instance().runRefreshTask(FuelReceiptListFragment.this, rangeStartDate, rangeEndDate);
    }

}
