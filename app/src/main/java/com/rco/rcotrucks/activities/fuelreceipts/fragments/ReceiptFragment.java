package com.rco.rcotrucks.activities.fuelreceipts.fragments;

import static android.app.Activity.RESULT_OK;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.icu.util.Calendar;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.constraintlayout.widget.Guideline;
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
import android.widget.FrameLayout;
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
import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.BaseCoordinatorFragment;
import com.rco.rcotrucks.activities.DialogManager;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.drive.DriveFragmentBase;
import com.rco.rcotrucks.activities.drive.DriveFragmentPhone;
import com.rco.rcotrucks.activities.fuelreceipts.activities.CreateFuelReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.activities.CreateTollReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.activities.FuelReceiptDetailActivity;
import com.rco.rcotrucks.activities.fuelreceipts.activities.FuelReceiptDetailAndEditActivity;
import com.rco.rcotrucks.activities.fuelreceipts.activities.TollReceiptDetailActivity;
import com.rco.rcotrucks.activities.fuelreceipts.activities.TollReceiptDetailAndEditActivity;
import com.rco.rcotrucks.activities.fuelreceipts.activities.TollReceiptDtlActivity;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.TollReceiptAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.model.FuelReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.activities.FuelReceiptDtlActivity;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.FuelReceiptListAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.utils.UiHelperFuelReceiptList;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.ReceiptAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.activities.ifta.BusHelperIfta;
import com.rco.rcotrucks.adapters.DateRangeFilterAdapter;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.dialog.DateFilterDialog;
import com.rco.rcotrucks.interfaces.CRUDInterface;
import com.rco.rcotrucks.interfaces.ReceiptListener;
import com.rco.rcotrucks.model.DateRangeModel;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ReceiptFragment extends BaseCoordinatorFragment implements CRUDInterface {

    private static final String TAG = ReceiptFragment.class.getSimpleName();
    public static final String EXTRA_MESSAGE_FUEL_RECEIPT_IDENT = "com.rco.rcotrucks.fuelreceiptident";
    ProgressDialog progressDialog;
    ConstraintLayout fuelLayout, tollLayout, dateRangeFilterLayout;
    ImageView cancelSearch, refreshIcon, addIcon, informationIcon, cameraIcon, searchClear;
    TextView fuelTV, tollTV, sync, cancelSearchRangeFilter, homeAppBarLeftSideTitle, noRecordFound, filterIcon, deleteMultipleEntries;
    FrameLayout receiptFrameLayout;
    EditText searchET;

    RecyclerView fuelReceiptRecyclerView, tollReceiptRecyclerView, dateRangeRecyclerView;
    ReceiptAdapter receiptAdapter;
    TollReceiptAdapter tollReceiptAdapter;
    DateRangeFilterAdapter dateRangeFilterAdapter;
    List<ReceiptModel> filteredList;
    List<DateRangeModel> dateRangeModelList;

    Calendar mCalendar;
    DateFilterDialog dateFilterDialog;
    String selectedFilter = "", rangeStartDate = "", rangeEndDate = "", lastOpenFragment = "fuel";
    long today = 0, month = 0;

    boolean isTablet = false, isDatePeriodFilterApplied = false, isFuelReceiptListShown = true;
    MaterialDatePicker.Builder<Pair<Long, Long>> materialDateBuilder;
    MaterialDatePicker<Pair<Long, Long>> materialDatePicker;
    User user;
    public BusinessRules businessRules = BusinessRules.instance();
    Guideline guideline;
    Fragment lastOpenedFragment;
    int lastFuelReceiptHighLightedPosition = -1, lastTollReceiptHighLightedPosition = -1;
    boolean isFilterOpen = false;

    private ReceiptFragment.SyncRecordsTask syncRecords;
    public static Executor threadPoolExecutor;
    int corePoolSize = 60, maximumPoolSize = 80, keepAliveTime = 10;
    BlockingQueue<Runnable> workQueue;
    ConstraintLayout loadingPanel;
    boolean isFirstIteration = false;
    private final static int RESULT_OK = 1;

    ArrayList<ReceiptModel> multiDeleteReceiptsArrayList;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_receipt, container, false);
        isTablet = getResources().getBoolean(R.bool.isTablet);
        Log.d(TAG, "onCreateView: ");

        setIds(view);
        setIdsFromActivity();
        initialize();
        setDatePicker();
        setListeners();

        setUpFuelReceiptRecyclerView();
        setUpTollReceiptRecyclerView();

//        Dec 04, 2022  -
        setupDateRangeRecyclerView();
        populateDateRangeFilterList();
        return view;
    }


//            Log.d(TAG, "onCreate: previousYear: firstDay: " + DateUtils.getLastYearFirstDayInTimeStamp() + " lastDay: " + DateUtils.getLastYearLastDayInTimeStamp());
//


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "save: onResume: lastOpenFragment: " + lastOpenFragment);
        refreshScreen();
    }

    void refreshScreen() {
        if (lastOpenFragment.equalsIgnoreCase("fuel")) {
            Log.d(TAG, "onDeleteCalled: delete fuel receipt: ");
            lastFuelReceiptHighLightedPosition = 0;
            setFuelLayout();

//            UiHelperFuelReceiptList.instance().runRefreshTask(ReceiptFragment.this, "", "");
//            getFuelReceiptsList();
            getFuelReceiptsContent();
        } else {
            Log.d(TAG, "onDeleteCalled: delete toll receipt: ");
            lastTollReceiptHighLightedPosition = 0;
            setTollLayout();
            getTollReceiptsContent();
        }
    }

    void setIds(View view) {

        Log.d(TAG, "setIds: ");
        cancelSearch = view.findViewById(R.id.cancelSearch);
        fuelLayout = view.findViewById(R.id.fuel_layout_bottom_bar);
        tollLayout = view.findViewById(R.id.toll_layout_bottom_bar);
        filterIcon = view.findViewById(R.id.filter_icon);
        refreshIcon = view.findViewById(R.id.refresh_icon);
        addIcon = view.findViewById(R.id.add_icon);
        informationIcon = view.findViewById(R.id.info_icon);
        cameraIcon = view.findViewById(R.id.camera_icon);

        fuelTV = view.findViewById(R.id.tv_fuel);
        tollTV = view.findViewById(R.id.tv_toll);

        dateRangeFilterLayout = view.findViewById(R.id.date_range_filter_layout);
        dateRangeRecyclerView = view.findViewById(R.id.date_range_recycler_view);
        fuelReceiptRecyclerView = view.findViewById(R.id.fuel_receipt_recycler_view);
        tollReceiptRecyclerView = view.findViewById(R.id.toll_receipt_recycler_view);
        receiptFrameLayout = view.findViewById(R.id.receipt_frame_layout);

        searchClear = view.findViewById(R.id.iv_clear_search);
        searchET = view.findViewById(R.id.et_search);

        guideline = view.findViewById(R.id.guideline4);
        cancelSearchRangeFilter = view.findViewById(R.id.cancel_search_range_filter);
        noRecordFound = view.findViewById(R.id.noRecordFound);

        loadingPanel = view.findViewById(R.id.loading_panel);
    }

    void setIdsFromActivity() {
        sync = getActivity().findViewById(R.id.main_app_bar_sync);
        homeAppBarLeftSideTitle = getActivity().findViewById(R.id.left_side_title);
        homeAppBarLeftSideTitle.setVisibility(View.VISIBLE);
        homeAppBarLeftSideTitle.setText("Receipts");

        deleteMultipleEntries = getActivity().findViewById(R.id.delete_multiple_receipts);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        progressDialog = new ProgressDialog(getActivity());
        cancelSearch.setVisibility(View.GONE);

        filteredList = new ArrayList<>();
        dateRangeModelList = new ArrayList<>();

        today = MaterialDatePicker.todayInUtcMilliseconds(); // Select current date
        month = MaterialDatePicker.thisMonthInUtcMilliseconds(); // Select this month first date
        mCalendar = Calendar.getInstance();

        materialDateBuilder = MaterialDatePicker.Builder.dateRangePicker();
        materialDateBuilder.setTitleText("SELECT A DATE");
        materialDatePicker = materialDateBuilder.build();

        user = BusinessRules.instance().getAuthenticatedUser();

        multiDeleteReceiptsArrayList = new ArrayList<>();
    }

    void setListeners() {
        Log.d(TAG, "setListeners: ");

        searchClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchET.setText("");
            }
        });

        searchET.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: isFuelReceiptListShown: " + isFuelReceiptListShown);
                String searchedString = s.toString().toLowerCase();
                if (isFuelReceiptListShown) {
                    if (receiptAdapter == null) {
                        return;
                    }
                    receiptAdapter.search(searchedString);
                } else {
                    if (tollReceiptAdapter == null) {
                        return;
                    }
                    tollReceiptAdapter.search(searchedString);
                }
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: ");
            }

            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Log.d(TAG, "onTextChanged: ");

            }
        });


        filterIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Dec 04, 2022  -   Commented this method so for now I can use newly design layout as we have in ios
//                openDateFilterDialog();
//                if (isFuelReceiptListShown) {
//                    Toast.makeText(getContext(), "Working", Toast.LENGTH_SHORT).show();
//                } else {
                isFilterOpen = true;
                dateRangeFilterLayout.setVisibility(View.VISIBLE);
//                }
            }
        });

        sync.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                Dec 15, 2022  -   On Sync we should upload any pending entries - delete any pending entries - and fetch new record
                isFirstIteration = true;
//                syncRecords();
            }
        });


        refreshIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setUpFuelReceiptRecyclerView();
            }
        });

        fuelLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchET.setText("");
                lastOpenFragment = "fuel";
                setFuelLayout();
                getFuelReceiptsContent();
            }
        });

        tollLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchET.setText("");
                lastOpenFragment = "toll";
                setTollLayout();
                getTollReceiptsContent();
            }
        });

        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity();
            }
        });

        dateRangeFilterLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFilterOpen = false;
                dateRangeFilterLayout.setVisibility(View.GONE);
            }
        });

        cancelSearchRangeFilter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isFilterOpen = false;
                dateRangeFilterLayout.setVisibility(View.GONE);
            }
        });

        deleteMultipleEntries.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: deleteMultipleEntries: ");
//                multiDeleteReceiptsArrayList
                deleteSelectedReceipts();
            }
        });

    }

//    Jan 02, 2022  -   this function will delete multi selection entries
    void deleteSelectedReceipts() {
        Log.d(TAG, "deleteSelectedReceipts: deleteSelectedReceipts: size: " + multiDeleteReceiptsArrayList.size());

        for (int i = 0; i < multiDeleteReceiptsArrayList.size(); i++) {
            ReceiptModel receiptModelToDelete = multiDeleteReceiptsArrayList.get(i);

            if (isFuelReceiptListShown) {
                Log.d(TAG, "deleteSelectedReceipts: objectId: " + receiptModelToDelete.getFuelReceiptObjectId()
                        + " objectType: " + receiptModelToDelete.getFuelReceiptObjectType());
                if (receiptModelToDelete.getFuelReceiptObjectId().isEmpty() || receiptModelToDelete.getFuelReceiptObjectId() == null
                        || receiptModelToDelete.getFuelReceiptObjectId().equalsIgnoreCase("0")) {
                    deleteFromFuelReceiptDB(receiptModelToDelete.getId());
                } else {
                    deleteRecord(receiptModelToDelete.getId(), receiptModelToDelete.getFuelReceiptObjectId(), receiptModelToDelete.getFuelReceiptObjectType(), true);
                    deleteFromFuelReceiptDB(receiptModelToDelete.getId());
                }
            } else {

                Log.d(TAG, "deleteSelectedReceipts: objectId: " + receiptModelToDelete.getTollReceiptObjectId()
                        + " objectType: " + receiptModelToDelete.getTollReceiptObjectType());

                if (receiptModelToDelete.getTollReceiptObjectId() == 0) {
                    deleteFromTollReceiptDB(receiptModelToDelete.getId());
                } else {
                    deleteRecord(receiptModelToDelete.getId(), "" + receiptModelToDelete.getTollReceiptObjectId(), receiptModelToDelete.getTollReceiptObjectType(), false);
                    deleteFromTollReceiptDB(receiptModelToDelete.getId());
                }
            }
        }


        multiDeleteReceiptsArrayList.clear();
        refreshScreen();
    }

    void setUpFuelReceiptRecyclerView() {
        Log.d(TAG, "setUpRecyclerViewLinearLayoutForChat: ");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        fuelReceiptRecyclerView.setLayoutManager(linearLayoutManager);

        receiptAdapter = new ReceiptAdapter(filteredList, getContext(), new ReceiptAdapter.ReceiptInterface() {
            @Override
            public void onListItemClicked(int position, java.util.List<ReceiptModel> list) {
                Log.d(TAG, "onListItemClicked: truckNumber: " + list.get(position).getTollReceiptTruckNumber());
                lastFuelReceiptHighLightedPosition = position;
                setBackgroundOfSelectedItem(position, list);
                if (isTablet) {
                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
                    bundle.putParcelable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
                    openFuelReceiptDetail(bundle);
                } else {
                    openEditActivity(true, list.get(position));
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


            @Override
            public void onItemLongClick(int position, java.util.List<ReceiptModel> list, ArrayList<ReceiptModel> multiDeleteArrayList) {
                Log.d(TAG, "onItemLongClick: ");

                multiDeleteReceiptsArrayList = multiDeleteArrayList;
                if (multiDeleteArrayList.size() == 0) {
                    deleteMultipleEntries.setVisibility(View.GONE);
                } else {
                    deleteMultipleEntries.setVisibility(View.VISIBLE);
                }


//                lastFuelReceiptHighLightedPosition = position;
//                setBackgroundOfSelectedItemForMultipleSelection(position, list, multiDeleteArrayList);
                if (isTablet) {
                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
                    bundle.putParcelable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
                    openFuelReceiptDetail(bundle);
                }

            }
        });
        fuelReceiptRecyclerView.setAdapter(receiptAdapter);
    }

    void setUpTollReceiptRecyclerView() {
        Log.d(TAG, "setUpTollReceiptRecyclerView: ");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        tollReceiptRecyclerView.setLayoutManager(linearLayoutManager);

        tollReceiptAdapter = new TollReceiptAdapter(filteredList, getContext(), new TollReceiptAdapter.ReceiptInterface() {
            @Override
            public void onListItemClicked(int position, java.util.List<ReceiptModel> list) {
                Log.d(TAG, "onListItemClicked: truckNumber: " + list.get(position).getTollReceiptTruckNumber());
                lastTollReceiptHighLightedPosition = position;
                setBackgroundOfSelectedItem(position, list);

                if (isTablet) {
                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
                    bundle.putParcelable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
                    openTollReceiptDetail(bundle);
                } else {
                    openEditActivity(false, list.get(position));
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
            public void onItemLongClick(int position, List<ReceiptModel> list, ArrayList<ReceiptModel> multiDeleteArrayList) {
                multiDeleteReceiptsArrayList = multiDeleteArrayList;
                if (multiDeleteArrayList.size() == 0) {
                    deleteMultipleEntries.setVisibility(View.GONE);
                } else {
                    deleteMultipleEntries.setVisibility(View.VISIBLE);
                }

//                lastFuelReceiptHighLightedPosition = position;
//                setBackgroundOfSelectedItemForMultipleSelection(position, list, multiDeleteArrayList);
                if (isTablet) {
                    Bundle bundle = new Bundle();
//                    bundle.putSerializable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
                    bundle.putParcelable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, list.get(position));
                    openTollReceiptDetail(bundle);
                }
            }
        });
        tollReceiptRecyclerView.setAdapter(tollReceiptAdapter);
    }


    //    public ReceiptModel(String date, String name, String amount, String icon) {
    public void getFuelReceiptsList() {
        Log.d(TAG, "getFuelReceiptsList: ");
        filteredList.clear();
//        fuelReceiptList.add(new ReceiptModel("11/15/2022", "Pilot", "100", ""));
//        fuelReceiptList.add(new ReceiptModel("11/15/2022", "TA", "120", ""));
//        fuelReceiptList.add(new ReceiptModel("11/15/2022", "Love", "140", ""));

        List<FuelReceiptListAdapter.ListItemFuelReceipt> listFuelReceiptItems = BusHelperFuelReceipts.getListFuelReceipts();
        if (listFuelReceiptItems == null) {
            return;
        }
        Log.d(TAG, "getFuelReceiptsList: listFuelReceiptItems: size: " + listFuelReceiptItems.size());
//        Nov 21, 2022  -   We should show the message for no receipts if we don't find any
        if (listFuelReceiptItems.size() == 0) {
            if (isDatePeriodFilterApplied && !selectedFilter.isEmpty()) {
                Toast.makeText(getContext(), "No Receipts for " + selectedFilter, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "No Receipts", Toast.LENGTH_SHORT).show();
            }
        }

        for (int i = 0; i < listFuelReceiptItems.size(); i++) {
//            Log.d(TAG, "getFuelReceiptsList: index: " + i + " list: " + listFuelReceiptItems.get(i));
//            arMatchText=["02", "Gasoline", {null}, "12", "12"],
//            date="11/16/2022 00:00:00", fuelCode="02", fuelType="Gasoline", idRmsRecords=44,
//            isValid=true, numberOfGallons="12", objectId="2966", objectType="NRT523", recordId=null,
//            sortKey="2022-11-16 00:00:00.000", syncStatus=0, totalAmount="12", vendorCountry=null,
//            vendorName="12", vendorState="AL Alabama"

            ReceiptModel receiptModel = new ReceiptModel();
//            receiptModel.setArMatchText(listFuelReceiptItems.get(i).);
            receiptModel.setDate(listFuelReceiptItems.get(i).getDate());
            receiptModel.setFuelCode(listFuelReceiptItems.get(i).getFuelCode());
            receiptModel.setFuelType(listFuelReceiptItems.get(i).getFuelType());
            receiptModel.setIdRmsRecords(listFuelReceiptItems.get(i).getIdRmsRecords());
//            receiptModel.isValid(listFuelReceiptItems.get(i).);
            receiptModel.setNumberOfGallons(listFuelReceiptItems.get(i).getNumberOfGallons());
            receiptModel.setObjectId(listFuelReceiptItems.get(i).getObjectId());
            receiptModel.setObjectType(listFuelReceiptItems.get(i).getObjectType());
            receiptModel.setRecordId(listFuelReceiptItems.get(i).getRecordId());
            receiptModel.setSortKey(listFuelReceiptItems.get(i).getSortKey());
//            receiptModel.setSyncStatus(listFuelReceiptItems.get(i).get);
//            Nov 23, 2022  -
            receiptModel.setTotalAmountInUSD(listFuelReceiptItems.get(i).getTotalAmountInUSD());
            receiptModel.setTotalAmount(listFuelReceiptItems.get(i).getTotalAmount());
            receiptModel.setUserRecordId(listFuelReceiptItems.get(i).getUserRecordId());

            receiptModel.setVendorCountry(listFuelReceiptItems.get(i).getVendorCountry());
            receiptModel.setVendorName(listFuelReceiptItems.get(i).getVendorName());
            receiptModel.setVendorState(listFuelReceiptItems.get(i).getVendorState());
            receiptModel.setFuelReceipt(true);

//            to select first item and show it in light gray color
            if (i == 0) {
                receiptModel.setSelected(true);
            } else {
                receiptModel.setSelected(false);
            }
            filteredList.add(receiptModel);
        }

        receiptAdapter.populateFilterArrayList(filteredList);
        receiptAdapter.notifyDataSetChanged();
        openLatestFuelReceiptDetail();
    }

    void setFuelLayout() {
        Log.d(TAG, "setFuelLayout: ");

        checkFilterLayoutState();
        isFuelReceiptListShown = true;
        fuelLayout.setBackground(getResources().getDrawable(R.drawable.black_curved_background_two));
        tollLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background_for_bottom_bar));

        fuelTV.setTextColor(getResources().getColor(R.color.white));
        tollTV.setTextColor(getResources().getColor(R.color.black));

        fuelReceiptRecyclerView.setVisibility(View.VISIBLE);
        tollReceiptRecyclerView.setVisibility(View.GONE);
    }

    void setTollLayout() {
        Log.d(TAG, "setTollLayout: ");
        checkFilterLayoutState();
        isFuelReceiptListShown = false;
        tollLayout.setBackground(getResources().getDrawable(R.drawable.black_curved_background_two));
        fuelLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background_for_bottom_bar));

        tollTV.setTextColor(getResources().getColor(R.color.white));
        fuelTV.setTextColor(getResources().getColor(R.color.black));

        fuelReceiptRecyclerView.setVisibility(View.GONE);
        tollReceiptRecyclerView.setVisibility(View.VISIBLE);
    }

    void checkFilterLayoutState() {
//        Dec 08, 2022  -   When we switch tabs, I am resetting filter to All and its index from array list is 9th
        populateDateRangeFilterList();

        if (isFilterOpen) {
            isFilterOpen = false;
            dateRangeFilterLayout.setVisibility(View.GONE);
        }
    }

    private void loadFragment(Fragment fragment, Bundle bundle) {
        Log.d(TAG, "onClick: loadMainFragment: ");
        lastOpenedFragment = fragment;

        if (isTablet) {
            fragment.setArguments(bundle);
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            fragmentManager.beginTransaction().replace(R.id.receipt_frame_layout, fragment).commit();
        } else {
//            Open new activity respectively

        }
    }

    void openDateFilterDialog() {
        Log.d(TAG, "openDateFilterDialog: ");
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

    void setDatePicker() {
//        datePicker = MaterialDatePicker.Builder.dateRangePicker().setSelection(Pair.create(MaterialDatePicker.thisMonthInUtcMilliseconds(), MaterialDatePicker.todayInUtcMilliseconds())).build();
    }

    void applyDateFilter(String selectedDateValue) {
        selectedFilter = selectedDateValue;
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
        } else if (selectedDateValue.equalsIgnoreCase("Custom Range")) {
            materialDatePicker.show(getActivity().getSupportFragmentManager(), "Material_Range");
            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                @Override
                public void onPositiveButtonClick(Pair<Long, Long> selection) {
                    Log.d(TAG, "onPositiveButtonClick: first: " + selection.first);
                    Log.d(TAG, "onPositiveButtonClick: second: " + selection.second);
                    rangeStartDate = getStartRange(selection.first);
                    rangeEndDate = getEndRange(selection.second);
                }
            });
        } else if (selectedDateValue.equalsIgnoreCase("All")) {
            selectedFilter = "";
            rangeStartDate = "";
            rangeEndDate = "";
            setFilterIcon(false);
        }

        Log.d(TAG, "applyDateFilter: ");

        if (isFuelReceiptListShown) {
//            UiHelperFuelReceiptList.instance().runRefreshTask(ReceiptFragment.this, rangeStartDate, rangeEndDate);

            ArrayList<FuelReceiptModel> fuelReceiptsList = businessRules.getFuelReceiptForDateRange(rangeStartDate, rangeEndDate);
            if (fuelReceiptsList == null) {

                return;
            }
            Log.d(TAG, "applyDateFilter: tollReceiptsList: " + fuelReceiptsList.size());
//            for (int i = 0; i < fuelReceiptsList.size(); i++) {
//                Log.d(TAG, "applyDateFilter: index: " + i + " date: " + fuelReceiptsList.get(i).getTollReceiptDateTime());
//            }

        } else {
//            DateUtils.IDateConverter dateConverter = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_TIME_MILLIS,
//                    DateUtils.FORMAT_DATE_MM_DD_YY, DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS);
//            String rangeStartDateAndTime=dateConverter.convert(rangeStartDate,true);
//            String rangeEndDateAndTime=dateConverter.convert(rangeEndDate,true);
//            Log.d(TAG, "loadFuelReceiptItems: start: "+rangeStartDateAndTime+" end: "+rangeEndDateAndTime);


            ArrayList<TollReceiptModel> tollReceiptsList = businessRules.getTollReceiptForDateRange(rangeStartDate, rangeEndDate);
            if (tollReceiptsList == null) {

                return;
            }
            Log.d(TAG, "applyDateFilter: tollReceiptsList: " + tollReceiptsList.size());
//            for (int i = 0; i < tollReceiptsList.size(); i++) {
//                Log.d(TAG, "applyDateFilter: index: " + i + " date: " + tollReceiptsList.get(i).getTollReceiptDateTime());
//            }

        }
    }

    void setFilterIcon(boolean isFilterApplied) {
        Log.d(TAG, "setFilterIcon: ");
        isDatePeriodFilterApplied = isFilterApplied;
//        if (isFilterApplied) {
//            filterIcon.setImageResource(R.drawable.filter_icon_remove);
//        } else {
//            filterIcon.setImageResource(R.drawable.filter_icon);
//        }
    }

    String getStartRange(long startingTimeStamp) {
        return DateUtils.getDateTime(startingTimeStamp, DateUtils.FORMAT_DATE_YYYY_MM_DD) + " 00:00:00.000";
    }

    String getEndRange(long endingTimeStamp) {
        return DateUtils.getDateTime(endingTimeStamp, DateUtils.FORMAT_DATE_YYYY_MM_DD) + " 23:59:59.000";
    }

    void openFuelReceiptDetail(Bundle bundle) {
        loadFragment(new FuelReceiptDetail(this), bundle);
    }

    void openTollReceiptDetail(Bundle bundle) {
        loadFragment(new TollReceiptDetail(this), bundle);
    }

    void openNextActivity() {
        Log.d(TAG, "openNextActivity: ");
        Intent intent;
        if (isFuelReceiptListShown) {
//            intent = new Intent(getActivity(), FuelReceiptDtlActivity.class);
            intent = new Intent(getActivity(), CreateFuelReceipt.class);
        } else {
//            intent = new Intent(getActivity(), TollReceiptDetailActivity.class);
            intent = new Intent(getActivity(), CreateTollReceipt.class);
        }
        BusHelperRmsCoding.RmsRecords recordIdent = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);
        intent.putExtra(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, recordIdent);
        startActivityForResult(intent, 1);
    }


    int newlyAddedEntryId = -1;

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: requestCode: " + requestCode);

        if (requestCode == 1) {
//            if (resultCode == RESULT_OK) {

            Log.d(TAG, "onActivityResult: resultCode: " + resultCode);

            if (data != null) {
                if (isFuelReceiptListShown) {
                    newlyAddedEntryId = data.getIntExtra("value", -1);
                    Log.d(TAG, "onActivityResult: newEntryId: " + newlyAddedEntryId);
                    getFuelReceiptsContent();
                } else {
                    newlyAddedEntryId = data.getIntExtra("value", -1);
                    Log.d(TAG, "onActivityResult: newEntryId: " + newlyAddedEntryId);
                    getTollReceiptsContent();
                }


            }

//            }
        }
    }

    void openEditActivity(boolean isFuelReceipt, ReceiptModel receiptModel) {
        Log.d(TAG, "openNextActivity: ");
        Intent intent;
        if (isFuelReceipt) {
            intent = new Intent(getActivity(), FuelReceiptDetailAndEditActivity.class);
        } else {
            intent = new Intent(getActivity(), TollReceiptDetailAndEditActivity.class);
        }
        intent.putExtra("dataModel", receiptModel);
        startActivity(intent);
    }

    void getFuelReceiptsContent() {
        filteredList.clear();
        boolean newEntrySelection = false;

        ArrayList<FuelReceiptModel> fuelReceiptsList = businessRules.getFuelReceipt();
        Log.d(TAG, "getFuelReceiptsContent: fuelReceiptsList: " + fuelReceiptsList.size());

        setNoRecordFound(fuelReceiptsList.size());
        for (int i = 0; i < fuelReceiptsList.size(); i++) {

//            Log.d(TAG, "getFuelReceipt: getFuelReceiptsContent: id: " + fuelReceiptsList.get(i).getId());
            FuelReceiptModel fuelReceiptModel = fuelReceiptsList.get(i);
            ReceiptModel receiptModel = new ReceiptModel();
            receiptModel.setId(fuelReceiptModel.getId());
            if (!fuelReceiptModel.getFuelReceiptObjectId().isEmpty()) {
                receiptModel.setFuelReceiptObjectId(fuelReceiptModel.getFuelReceiptObjectId());
            }
            receiptModel.setFuelReceiptObjectType(fuelReceiptModel.getFuelReceiptObjectType());
            receiptModel.setFuelReceiptMobileRecordId(fuelReceiptModel.getFuelReceiptMobileRecordId());
//            Log.d(TAG, "getFuelReceiptsContent: mobileRecordId: " + fuelReceiptModel.getFuelReceiptMobileRecordId());
            receiptModel.setFuelReceiptOrganizationNumber(fuelReceiptModel.getFuelReceiptOrganizationName());
            receiptModel.setFuelReceiptCompany(fuelReceiptModel.getFuelReceiptCompany());
            receiptModel.setFuelReceiptState(fuelReceiptModel.getFuelReceiptState());
            receiptModel.setFuelReceiptBarCode(fuelReceiptModel.getFuelReceiptBarCode());
            receiptModel.setFuelReceiptFirstName(fuelReceiptModel.getFuelReceiptFirstName());
            receiptModel.setFuelReceiptTruckStop(fuelReceiptModel.getFuelReceiptTruckStop());
            receiptModel.setFuelReceiptDateTime(fuelReceiptModel.getFuelReceiptDateTime().replace(" 00:00:00.000", "").replace("00:00:00", "").replace(" .000", "").trim());

//            Log.d(TAG, "getFuelReceiptsContent: before: " + fuelReceiptModel.getFuelReceiptDateTime());
//            String dateTime = fuelReceiptModel.getFuelReceiptDateTime().replace(" 00:00:00.000", "").replace("00:00:00", "").replace(" .000", "").trim();
//            String formattedDateTime = " " + DateUtils.convertDateTime(dateTime, DateUtils.FORMAT_DATE_YYYY_MM_DD, DateUtils.FORMAT_DATE_MM_DD_YYYY);
//            receiptModel.setFuelReceiptDateTime(formattedDateTime);
//            Log.d(TAG, "getFuelReceiptsContent: dateTime: " + dateTime);

            String dateTime = receiptModel.getFuelReceiptDateTime();

            if (dateTime.contains("/")) {
                String[] splitDateTimeByDash = dateTime.split("/");
                int month = Integer.parseInt(splitDateTimeByDash[0]);
                int day = Integer.parseInt(splitDateTimeByDash[1]);
                int year = Integer.parseInt(splitDateTimeByDash[2]);
//                int year = Integer.parseInt(splitDateTimeByDash[0]);
//                int month = Integer.parseInt(splitDateTimeByDash[1]);
//                int day = Integer.parseInt(splitDateTimeByDash[2]);

//                receiptModel.setFuelReceiptDateTime(month + "/" + day + "/" + year);
//
//                long dateTimeInTimeStamp = DateUtils.convertToTimeStamp(year, month, day, 11, 20);
//                Log.d(TAG, "getFuelReceiptsList: dateTimeInTimeStamp: " + dateTimeInTimeStamp);
//                receiptModel.setDateTimeInTimeStamp(dateTimeInTimeStamp);
//                Log.d(TAG, "getFuelReceiptsList: receiptModel: timeStamp: " + receiptModel.getDateTimeInTimeStamp());
//            } else if (dateTime.contains("-")) {
//                String[] splitDateTimeByDash = dateTime.split("-");
//                int year = Integer.parseInt(splitDateTimeByDash[0]);
//                int month = Integer.parseInt(splitDateTimeByDash[1]);
//                int day = Integer.parseInt(splitDateTimeByDash[2]);
//
//                receiptModel.setFuelReceiptDateTime(month + "/" + day + "/" + year);

                long dateTimeInTimeStamp = DateUtils.convertToTimeStamp(year, month, day, 11, 20);
                Log.d(TAG, "getFuelReceiptsList: dateTimeInTimeStamp: " + dateTimeInTimeStamp);
                receiptModel.setDateTimeInTimeStamp(dateTimeInTimeStamp);
                Log.d(TAG, "getFuelReceiptsList: receiptModel: timeStamp: " + receiptModel.getDateTimeInTimeStamp());
            }
            Log.d(TAG, "getFuelReceiptsContent: format: " + receiptModel.getFuelReceiptDateTime());
            Log.d(TAG, "getFuelReceiptsContent: timeStamp: " + receiptModel.getDateTimeInTimeStamp());

            receiptModel.setFuelReceiptTruckNumber(fuelReceiptModel.getFuelReceiptTruckNumber());
            receiptModel.setFuelReceiptDOTNumber(fuelReceiptModel.getFuelReceiptDOTNumber());
            receiptModel.setFuelReceiptVehicleLicenseNumber(fuelReceiptModel.getFuelReceiptVehicleLicenseNumber());
            receiptModel.setFuelReceiptCreationTime(fuelReceiptModel.getFuelReceiptCreationTime());
            receiptModel.setFuelReceiptCreationDate(fuelReceiptModel.getFuelReceiptCreationDate());
            receiptModel.setFuelReceiptRMSTimestamp(fuelReceiptModel.getFuelReceiptRMSTimestamp());
            receiptModel.setFuelReceiptRMSCodingTimestamp(fuelReceiptModel.getFuelReceiptRMSCodingTimestamp());
            receiptModel.setFuelReceiptRecordId(fuelReceiptModel.getFuelReceiptRecordId());
            receiptModel.setFuelReceiptCountry(fuelReceiptModel.getFuelReceiptCountry());
            receiptModel.setFuelReceiptLastName(fuelReceiptModel.getFuelReceiptLastName());
            receiptModel.setFuelReceiptGallons(fuelReceiptModel.getFuelReceiptGallons());

            String amount = fuelReceiptModel.getFuelReceiptAmount().replace("$", "");
            receiptModel.setFuelReceiptAmount(UiUtils.getTwoDecimalValue(amount));

            String salesTax = fuelReceiptModel.getFuelReceiptSalesTax().replace("$", "");
            receiptModel.setFuelReceiptSalesTax(UiUtils.getTwoDecimalValue(salesTax));

//            receiptModel.setFuelReceiptSalesTax(fuelReceiptModel.getFuelReceiptSalesTax());
            receiptModel.setFuelReceiptTruckStop(fuelReceiptModel.getFuelReceiptTruckStop());
            receiptModel.setFuelReceiptFuelType(fuelReceiptModel.getFuelReceiptFuelType());
            receiptModel.setFuelReceiptOdometer(fuelReceiptModel.getFuelReceiptOdometer());
            receiptModel.setFuelReceiptUserRecordId(fuelReceiptModel.getFuelReceiptUserRecordId());
            receiptModel.setSelected(false);

            Log.d(TAG, "getFuelReceiptsContent: id: " + receiptModel.getId() +
                    " objectId: " + receiptModel.getFuelReceiptObjectId() +
                    " objectType: " + receiptModel.getFuelReceiptObjectType());

            if (fuelReceiptModel.getId().equalsIgnoreCase("" + newlyAddedEntryId)) {
                receiptModel.setSelected(true);
                newEntrySelection = true;
            }

            filteredList.add(receiptModel);
        }

        sortFuelListByDate();

        if (!newEntrySelection) {
            if (filteredList.size() > 0) {
                filteredList.get(0).setSelected(true);
                newlyAddedEntryId = -1;
            }
        }

        receiptAdapter.populateFilterArrayList(filteredList);
        receiptAdapter.notifyDataSetChanged();

        openLatestFuelReceiptDetail();
    }


    void getTollReceiptsContent() {
        filteredList.clear();
        boolean newEntrySelection = false;

        ArrayList<TollReceiptModel> tollReceiptsList = businessRules.getTollReceipt();
        setNoRecordFound(tollReceiptsList.size());

        for (int i = 0; i < tollReceiptsList.size(); i++) {

            TollReceiptModel tollReceiptModel = tollReceiptsList.get(i);
            ReceiptModel receiptModel = new ReceiptModel();
            receiptModel.setId(tollReceiptModel.getId());
            if (!tollReceiptModel.getTollReceiptObjectId().isEmpty()) {
                receiptModel.setTollReceiptObjectId(Integer.parseInt(tollReceiptModel.getTollReceiptObjectId()));
            }
            receiptModel.setTollReceiptObjectType(tollReceiptModel.getTollReceiptObjectType());
//            receiptModel.setTollReceiptICSVRow(Integer.parseInt()tollReceiptModel.getTollReceiptICSVRow());
            receiptModel.setTollReceiptMobileRecordId(tollReceiptModel.getTollReceiptMobileRecordId());
//            Log.d(TAG, "save: getIntentData: mobileRecordId: " + tollReceiptModel.getTollReceiptMobileRecordId()
//                    + " and: " + receiptModel.getTollReceiptMobileRecordId());
            receiptModel.setTollReceiptOrganizationNumber(tollReceiptModel.getTollReceiptOrganizationName());
            receiptModel.setTollReceiptCompany(tollReceiptModel.getTollReceiptCompany());
            receiptModel.setTollReceiptVendorState(tollReceiptModel.getTollReceiptVendorState());
            receiptModel.setTollReceiptBarCode(tollReceiptModel.getTollReceiptBarCode());

            String amount = tollReceiptModel.getTollReceiptAmount().replace("$", "");
            receiptModel.setTollReceiptAmount(UiUtils.getTwoDecimalValue(amount));
//            receiptModel.setTollReceiptAmount(tollReceiptModel.getTollReceiptAmount());

            receiptModel.setTollReceiptFirstName(tollReceiptModel.getTollReceiptFirstName());
            receiptModel.setTollReceiptVendorName(tollReceiptModel.getTollReceiptVendorName());
            receiptModel.setTollReceiptDateTime(tollReceiptModel.getTollReceiptDateTime().replace(" 00:00:00.000", "").replace("00:00:00", "").replace(" .000", "").trim());

            String dateTime = receiptModel.getTollReceiptDateTime();

            if (dateTime.contains("/")) {
                String[] splitDateTimeByDash = dateTime.split("/");
                int month = Integer.parseInt(splitDateTimeByDash[0]);
                int day = Integer.parseInt(splitDateTimeByDash[1]);
                int year = Integer.parseInt(splitDateTimeByDash[2]);
                long dateTimeInTimeStamp = DateUtils.convertToTimeStamp(year, month, day, 11, 20);
                Log.d(TAG, "getFuelReceiptsList: dateTimeInTimeStamp: " + dateTimeInTimeStamp);
                receiptModel.setDateTimeInTimeStamp(dateTimeInTimeStamp);
                Log.d(TAG, "getFuelReceiptsList: receiptModel: timeStamp: " + receiptModel.getDateTimeInTimeStamp());
            } else if (dateTime.contains("-")) {
                String[] splitDateTimeByDash = dateTime.split("-");
                int year = Integer.parseInt(splitDateTimeByDash[0]);
                int month = Integer.parseInt(splitDateTimeByDash[1]);
                int day = Integer.parseInt(splitDateTimeByDash[2]);
                long dateTimeInTimeStamp = DateUtils.convertToTimeStamp(year, month, day, 11, 20);
                Log.d(TAG, "getFuelReceiptsList: dateTimeInTimeStamp: " + dateTimeInTimeStamp);
                receiptModel.setDateTimeInTimeStamp(dateTimeInTimeStamp);
                Log.d(TAG, "getFuelReceiptsList: receiptModel: timeStamp: " + receiptModel.getDateTimeInTimeStamp());
            }

            receiptModel.setTollReceiptTruckNumber(tollReceiptModel.getTollReceiptTruckNumber());
            receiptModel.setTollReceiptDOTNumber(tollReceiptModel.getTollReceiptDotNumber());
            receiptModel.setTollReceiptVehicleLicenseNumber(tollReceiptModel.getTollReceiptVehicleLicenseNumber());
//            receiptModel.setTollReceiptObjectName(tollReceiptModel.getTollReceiptObjectName());
            receiptModel.setTollReceiptCreationTime(tollReceiptModel.getTollReceiptCreationTime());
            receiptModel.setTollReceiptCreationDate(tollReceiptModel.getTollReceiptCreationDate());
//            receiptModel.setTollReceiptYear(tollReceiptModel.getTollReceiptYear());
            receiptModel.setTollReceiptRMSTimestamp(tollReceiptModel.getTollReceiptRMSTimestamp());
            receiptModel.setTollReceiptRMSCodingTimestamp(tollReceiptModel.getTollReceiptRMSCodingTimestamp());
            receiptModel.setTollReceiptRecordId(tollReceiptModel.getTollReceiptRecordId());
            receiptModel.setTollReceiptVendorCountry(tollReceiptModel.getTollReceiptVendorCountry());
            receiptModel.setTollReceiptLastName(tollReceiptModel.getTollReceiptLastName());
//            receiptModel.setTollReceiptDay(tollReceiptModel.getTollReceiptDay());
            receiptModel.setTollReceiptRoadName(tollReceiptModel.getTollReceiptRoadName());
            receiptModel.setTollReceiptUserRecordId(tollReceiptModel.getTollReceiptUserRecordId());
            receiptModel.setSelected(false);

            if (tollReceiptModel.getId().equalsIgnoreCase("" + newlyAddedEntryId)) {
                receiptModel.setSelected(true);
                newEntrySelection = true;
            }
            filteredList.add(receiptModel);
        }

        sortTollListByDate();
//        if (filteredList.size() > 0) {
//            filteredList.get(0).setSelected(true);
//        }


        if (!newEntrySelection) {
            if (filteredList.size() > 0) {
                filteredList.get(0).setSelected(true);
                newlyAddedEntryId = -1;
            }
        }

        tollReceiptAdapter.populateFilterArrayList(filteredList);
        tollReceiptAdapter.notifyDataSetChanged();

        openLatestTollReceiptDetail();
    }

    void openLatestFuelReceiptDetail() {
        if (filteredList == null) {
            return;
        }

        if (filteredList.size() == 0) {
            removeYourFragment();
            return;
        }

        Bundle bundle = new Bundle();
//        bundle.putSerializable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, filteredList.get(0));
        bundle.putParcelable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, filteredList.get(0));
        openFuelReceiptDetail(bundle);
    }


    void openLatestTollReceiptDetail() {
        if (filteredList == null) {
            return;
        }

        if (filteredList.size() == 0) {
            removeYourFragment();
            return;
        }

        Bundle bundle = new Bundle();
//        bundle.putSerializable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, filteredList.get(0));
        bundle.putParcelable(EXTRA_MESSAGE_FUEL_RECEIPT_IDENT, filteredList.get(0));
        openTollReceiptDetail(bundle);
    }

    //    Its for FuelReceipts
    @Override
    public void onSaveCalled(FuelReceiptModel fuelReceiptModel) {
        Log.d(TAG, "save: onSaveCalled: from fuel receipts: ");
        //        Dec 05, 2022  -
        if (isFuelReceiptListShown) {

            Log.d(TAG, "save: onSaveCalled: filteredList: size: " + filteredList.size() + " lastFuelReceiptHighLightedPosition: " + lastFuelReceiptHighLightedPosition);
            filteredList.get(lastFuelReceiptHighLightedPosition).setFuelReceiptDateTime(fuelReceiptModel.getFuelReceiptDateTime());
            filteredList.get(lastFuelReceiptHighLightedPosition).setFuelReceiptGallons(fuelReceiptModel.getFuelReceiptGallons());
            filteredList.get(lastFuelReceiptHighLightedPosition).setFuelReceiptAmount(fuelReceiptModel.getFuelReceiptAmount());
            filteredList.get(lastFuelReceiptHighLightedPosition).setFuelReceiptSalesTax(fuelReceiptModel.getFuelReceiptSalesTax());
            filteredList.get(lastFuelReceiptHighLightedPosition).setFuelReceiptTruckStop(fuelReceiptModel.getFuelReceiptTruckStop());
            filteredList.get(lastFuelReceiptHighLightedPosition).setFuelReceiptState(fuelReceiptModel.getFuelReceiptState());
            filteredList.get(lastFuelReceiptHighLightedPosition).setFuelReceiptFuelType(fuelReceiptModel.getFuelReceiptFuelType());
            filteredList.get(lastFuelReceiptHighLightedPosition).setFuelReceiptOdometer(fuelReceiptModel.getFuelReceiptOdometer());
            receiptAdapter.notifyItemChanged(lastFuelReceiptHighLightedPosition);
        }
    }


    //    Its for TollReceipts
    @Override
    public void onSaveCalled(TollReceiptModel tollReceiptModel) {
//        Dec 05, 2022  -   Rather than refreshing the whole list - just update the item which got updated and keeps highlighted it
//        refreshScreen();

//        Dec 05, 2022  -
        if (isFuelReceiptListShown) {

        } else {
            if ((filteredList.size() < lastTollReceiptHighLightedPosition || lastTollReceiptHighLightedPosition < 0)) {
                return;
            }

            filteredList.get(lastTollReceiptHighLightedPosition).setTollReceiptDateTime(tollReceiptModel.getTollReceiptDateTime());
            filteredList.get(lastTollReceiptHighLightedPosition).setTollReceiptVendorName(tollReceiptModel.getTollReceiptVendorName());
            filteredList.get(lastTollReceiptHighLightedPosition).setTollReceiptVendorState(tollReceiptModel.getTollReceiptVendorState());
            filteredList.get(lastTollReceiptHighLightedPosition).setTollReceiptAmount(tollReceiptModel.getTollReceiptAmount());
            filteredList.get(lastTollReceiptHighLightedPosition).setTollReceiptRoadName(tollReceiptModel.getTollReceiptRoadName());
            tollReceiptAdapter.notifyItemChanged(lastTollReceiptHighLightedPosition);
        }
    }


//    Dec 02, 2022  -   Delete Mechanism
//    if we have a record which user wanted to delete and its object id and type
//    is null or empty that's mean this record is not uploaded on the server yet so what we need to do
//    simply delete it from our db and that's it
//    But if we have the record id and type then first we will delete it from server and check from its response
//    if record deleted successfully then gonna delete from local db too

//    TODO
//    What should we do in case when there is no internet and user mark an item to delete
//    and its on the server too?
//    use isMarkForDelete

    @Override
    public void onDeleteCalled(ReceiptModel receiptModel) {
        Log.d(TAG, "onDeleteCalled: receiptModel: " + receiptModel);
        Log.d(TAG, "onDeleteCalled: receiptModel: id: " + receiptModel.getId());
        Log.d(TAG, "onDeleteCalled: objectId: " + receiptModel.getTollReceiptObjectId());

        if (receiptModel.getTollReceiptObjectId() == 0) {
            deleteFromTollReceiptDB(receiptModel.getId());
        } else {
//            id, objectId, objectType
            deleteRecord(receiptModel.getId(), "" + receiptModel.getTollReceiptObjectId(), receiptModel.getTollReceiptObjectType(), false);
            deleteFromTollReceiptDB(receiptModel.getId());
        }
        refreshScreen();
    }

    //    Its for Fuel receipt
    @Override
    public void onDeleteCalled(FuelReceiptModel receiptModel) {
        Log.d(TAG, "onDeleteCalled: FuelReceiptModel: " + receiptModel);
        Log.d(TAG, "onDeleteCalled: FuelReceiptModel: id: " + receiptModel.getId());
        Log.d(TAG, "onDeleteCalled: FuelReceiptModel: objectId: " + receiptModel.getFuelReceiptObjectId());

        if (receiptModel.getFuelReceiptObjectId().isEmpty() || receiptModel.getFuelReceiptObjectId() == null
                || receiptModel.getFuelReceiptObjectId().equalsIgnoreCase("0")) {
            deleteFromFuelReceiptDB(receiptModel.getId());
        } else {
            deleteRecord(receiptModel.getId(), receiptModel.getFuelReceiptObjectId(), receiptModel.getFuelReceiptObjectType(), true);
            deleteFromFuelReceiptDB(receiptModel.getId());
        }
        refreshScreen();
    }

    public class MapAutoUpdateTask extends AsyncTask<String, String, Integer> {
        private int syncIntervalSecsMapAutoUpdateTask = 1;
        private long intervalCounter = 0;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            try {
//                businessRules.syncTollRoadReceipt();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        @Override
        protected Integer doInBackground(String... params) {
//            Log.d(TAG, "MapAutoUpdateTask: doInBackground: ");
            Log.d(TAG, "MapAutoUpdateTask: doInBackground: ");
            return 1;
        }

        @Override
        protected void onProgressUpdate(String... values) {
        }
    }

    void setBackgroundOfSelectedItem(int position, java.util.List<ReceiptModel> list) {
//        int position, java.util.List<ReceiptModel> list
        for (int i = 0; i < list.size(); i++) {
            if (i == position) {
                list.get(i).setSelected(true);
            } else {
                list.get(i).setSelected(false);
            }
        }
        if (isFuelReceiptListShown) {
            receiptAdapter.notifyDataSetChanged();
        } else {
            tollReceiptAdapter.notifyDataSetChanged();
        }
    }

    void setBackgroundOfSelectedItemForMultipleSelection(int position, java.util.List<ReceiptModel> list, ArrayList<ReceiptModel> multiDeleteArrayList) {
//        int position, java.util.List<ReceiptModel> list

        for (int i = 0; i < list.size(); i++) {
            list.get(i).setSelected(false);
        }

        for (int x = 0; x < multiDeleteArrayList.size(); x++) {
            for (int i = 0; i < list.size(); i++) {
                if (multiDeleteArrayList.get(x).getId().equalsIgnoreCase(list.get(i).getId())) {
                    if (i == position) {
                        list.get(i).setSelected(true);
                    }
                }
            }
        }
        if (isFuelReceiptListShown) {
            receiptAdapter.notifyDataSetChanged();
        } else {
            tollReceiptAdapter.notifyDataSetChanged();
        }
    }


    void deleteRecord(String id, String objectId, String objectType, boolean isFuelReceipt) {
        String usernamePasswordObjectIdObjectTypeCombine = user.getLogin() + "/" + user.getPassword() + "/"
                + objectId + "/" + objectType;
        Log.d(TAG, "deleteRecord: usernamePasswordObjectIdObjectTypeCombine: " + usernamePasswordObjectIdObjectTypeCombine);

        String deleteAPI = Rms.APIToDeleteRecord + usernamePasswordObjectIdObjectTypeCombine;

        RequestQueue requestQueue = Volley.newRequestQueue(getActivity());
        StringRequest stringRequest = new StringRequest(Request.Method.GET, deleteAPI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: response: " + response);
                if (response.contains("successfully delete")) {
//                    Delete it from local db now too
                    Log.d(TAG, "onResponse: id: " + id);
//                    if (isFuelReceipt) {
//                        deleteFromFuelReceiptDB(id);
//                    } else {
//                        deleteFromTollReceiptDB(id);
//                    }
                }
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
//                try {
//                    String responseBody = new String(error.networkResponse.data, "utf-8");
//                    Log.d(TAG, "onErrorResponse: responseBody: " + responseBody);

//                    Log.d(TAG, "onErrorResponse: id: " + receiptModel.getId());
//                    deleteFromDB(receiptModel.getId());

//                    JSONObject data = new JSONObject(responseBody);
//                    JSONArray errors = data.getJSONArray("errors");
//                    JSONObject jsonMessage = errors.getJSONObject(0);
//                    String message = jsonMessage.getString("message");
//                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
//                } catch (JSONException e) {
//                } catch (UnsupportedEncodingException unsupportedEncodingException) {
//                    Log.d(TAG, "onErrorResponse: unsupportedEncodingException: " +
//                            unsupportedEncodingException.getMessage());
//                }

            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(120), //After the set time elapses the request will timeout
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    void deleteFromTollReceiptDB(String recordId) {
        int isDeleted = businessRules.deleteTollReceiptItem(recordId);
        Log.d(TAG, "onErrorResponse: isDeleted: " + isDeleted);
        if (isDeleted == 1) {
            refreshScreen();
        }
    }

    void deleteFromFuelReceiptDB(String recordId) {
        int isDeleted = businessRules.deleteFuelReceiptItem(recordId);
        Log.d(TAG, "onErrorResponse: isDeleted: " + isDeleted);
        if (isDeleted == 1) {
            refreshScreen();
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

    void setupDateRangeRecyclerView() {
        Log.d(TAG, "setUpRecyclerViewLinearLayoutForChat: ");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getContext());
        dateRangeRecyclerView.setLayoutManager(linearLayoutManager);

        dateRangeFilterAdapter = new DateRangeFilterAdapter(dateRangeModelList, getContext(), new DateRangeFilterAdapter.DateRangeInterface() {
            @Override
            public void onListItemClicked(int position) {
                Log.d(TAG, "onListItemClicked: index: " + position);
                isFilterOpen = false;
                dateRangeFilterLayout.setVisibility(View.GONE);
                if (dateRangeModelList != null && position < dateRangeModelList.size()) {
                    Log.d(TAG, "onListItemClicked: selectedDate: " + dateRangeModelList.get(position).getDate());
                    filterSelectedDateRange(dateRangeModelList.get(position).getDate());
                }
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

    //    Dec 05, 2022    -   Through testing I have noticed that
//            this week starting timestamp is not correct AND
//            this month ending timestamp is not correct
    void filterSelectedDateRange(String dateFilter) {
        Log.d(TAG, "filterSelectedDateRange: dateFilter: " + dateFilter);
        long startDateInTimeStamp = 0, endDateInTimeStamp = 0;
        if (dateFilter.equalsIgnoreCase("Today")) {
            startDateInTimeStamp = DateUtils.getTodayDateTimeInTimeStamp(true);
            endDateInTimeStamp = DateUtils.getTodayDateTimeInTimeStamp(false);
        } else if (dateFilter.equalsIgnoreCase("Yesterday")) {
            startDateInTimeStamp = DateUtils.getYesterdayDateTimeInTimeStamp(true);
            endDateInTimeStamp = DateUtils.getYesterdayDateTimeInTimeStamp(false);
        } else if (dateFilter.equalsIgnoreCase("This Week")) {
//            startDateInTimeStamp = DateUtils.getStartOfThisWeekInMilli();
//            endDateInTimeStamp = DateUtils.getCurrentWeek(mCalendar, false);
//            endDateInTimeStamp = DateUtils.getThisWeekEndTimeInTimeStamp();

//            Dec 20, 2022  -   Start time is okay but not the end time
            startDateInTimeStamp = DateUtils.getThisWeekStartTimeInTimeStamp();
            endDateInTimeStamp = DateUtils.getCurrentWeekEndTimeInTimeStamp();
        } else if (dateFilter.equalsIgnoreCase("Last Week")) {

//            Dec 20, 2022  -   New and correct start and ending timestamp
            startDateInTimeStamp = DateUtils.getLastWeekStartTimeStamp();
            endDateInTimeStamp = DateUtils.getPreviousWeekEndTimeInTimeStamp();
        } else if (dateFilter.equalsIgnoreCase("This Month")) {

//            Dec 20, 2022  -   New and correct start and ending timestamp
            startDateInTimeStamp = DateUtils.getThisMonthStart();
            endDateInTimeStamp = DateUtils.getThisMonthEnd();
        } else if (dateFilter.equalsIgnoreCase("Last Month")) {

//            Dec 20, 2022  -   New and correct start and ending timestamp
            startDateInTimeStamp = DateUtils.getLastMonthStartTimeStamp();
            endDateInTimeStamp = DateUtils.getLastMonthEndTimeStamp();
        } else if (dateFilter.equalsIgnoreCase("This Year")) {

            startDateInTimeStamp = DateUtils.getThisYearStartTimeStamp();
            endDateInTimeStamp = DateUtils.getThisYearEndTimeStamp();
        } else if (dateFilter.equalsIgnoreCase("Last Year")) {

//            Dec 20, 2022  -   New and correct start and ending timestamp
            startDateInTimeStamp = DateUtils.getLastYearFirstDayInTimeStamp();
            endDateInTimeStamp = DateUtils.getLastYearLastDayInTimeStamp();
        } else if (dateFilter.equalsIgnoreCase("This Quarter")) {

//            Dec 20, 2022  -   New and correct start and ending timestamp
            startDateInTimeStamp = DateUtils.getCurrentQuarterStartTime().getTime();
            endDateInTimeStamp = DateUtils.getCurrentQuarterEndTime().getTime();
        } else if (dateFilter.equalsIgnoreCase("Last Quarter")) {
            startDateInTimeStamp = DateUtils.getFirstDayOfPreviousQuarter().getTime();
            endDateInTimeStamp = DateUtils.getLastDayOfPreviousQuarter().getTime();
        } else if (dateFilter.equalsIgnoreCase("Custom Range")) {
            materialDatePicker.show(getActivity().getSupportFragmentManager(), "Material_Range");
            materialDatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener<Pair<Long, Long>>() {
                @Override
                public void onPositiveButtonClick(Pair<Long, Long> selection) {
                    getDataBetween(selection.first, selection.second);
                }
            });
        } else if (dateFilter.equalsIgnoreCase("All")) {
            selectedFilter = "";
            rangeStartDate = "";
            rangeEndDate = "";
            setFilterIcon(false);
        }

        if (!dateFilter.equalsIgnoreCase("Custom Range")) {
            getDataBetween(startDateInTimeStamp, endDateInTimeStamp);
        }
    }

    void getDataBetween(long startDateInTimeStamp, long endDateInTimeStamp) {
        if (isFuelReceiptListShown) {
            if (receiptAdapter == null) {
                return;
            }
            receiptAdapter.searchBetweenDateRange(startDateInTimeStamp, endDateInTimeStamp);
        } else {
            if (tollReceiptAdapter == null) {
                return;
            }
            tollReceiptAdapter.searchBetweenDateRange(startDateInTimeStamp, endDateInTimeStamp);
        }

    }

    void sortFuelListByDate() {

        Collections.sort(filteredList, new Comparator<ReceiptModel>() {
            DateFormat formatWithSlash = new SimpleDateFormat("MM/dd/yyyy");
            DateFormat formatWithDash = new SimpleDateFormat("yyyy-MM-dd");

            @Override
            public int compare(ReceiptModel lhs, ReceiptModel rhs) {
                try {
                    String lhsValue = lhs.getFuelReceiptDateTime();
                    String rhsValue = rhs.getFuelReceiptDateTime();
                    Log.d(TAG, "compare: lhs: " + lhsValue + " rhs: " + rhsValue);
                    if (lhsValue != null && rhsValue != null) {
                        if (lhsValue.contains("-") && rhsValue.contains("-")) {
                            return formatWithDash.parse(rhs.getFuelReceiptDateTime()).compareTo(formatWithDash.parse(lhs.getFuelReceiptDateTime()));
                        } else if (lhsValue.contains("/") && rhsValue.contains("/")) {
                            return formatWithSlash.parse(rhs.getFuelReceiptDateTime()).compareTo(formatWithSlash.parse(lhs.getFuelReceiptDateTime()));
                        } else if (lhsValue.contains("-") && rhsValue.contains("/")) {
                            return formatWithSlash.parse(rhs.getFuelReceiptDateTime()).compareTo(formatWithDash.parse(lhs.getFuelReceiptDateTime()));
                        } else if (lhsValue.contains("/") && rhsValue.contains("-")) {
                            return formatWithDash.parse(rhs.getFuelReceiptDateTime()).compareTo(formatWithSlash.parse(lhs.getFuelReceiptDateTime()));
                        }
                    }
                    return -1;
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
    }


    void sortTollListByDate() {

        Collections.sort(filteredList, new Comparator<ReceiptModel>() {
            DateFormat formatWithSlash = new SimpleDateFormat("MM/dd/yyyy");
            DateFormat formatWithDash = new SimpleDateFormat("yyyy-MM-dd");

            @Override
            public int compare(ReceiptModel lhs, ReceiptModel rhs) {
                try {
                    String lhsValue = lhs.getTollReceiptDateTime();
                    String rhsValue = rhs.getTollReceiptDateTime();
                    Log.d(TAG, "compare: lhs: " + lhsValue + " rhs: " + rhsValue);
                    if (lhsValue != null && rhsValue != null) {
                        if (lhsValue.contains("-") && rhsValue.contains("-")) {
                            return formatWithDash.parse(rhs.getTollReceiptDateTime()).compareTo(formatWithDash.parse(lhs.getTollReceiptDateTime()));
                        } else if (lhsValue.contains("/") && rhsValue.contains("/")) {
                            return formatWithSlash.parse(rhs.getTollReceiptDateTime()).compareTo(formatWithSlash.parse(lhs.getTollReceiptDateTime()));
                        } else if (lhsValue.contains("-") && rhsValue.contains("/")) {
                            return formatWithSlash.parse(rhs.getTollReceiptDateTime()).compareTo(formatWithDash.parse(lhs.getTollReceiptDateTime()));
                        } else if (lhsValue.contains("/") && rhsValue.contains("-")) {
                            return formatWithDash.parse(rhs.getTollReceiptDateTime()).compareTo(formatWithSlash.parse(lhs.getTollReceiptDateTime()));
                        }
                    }
                    return -1;
                } catch (ParseException e) {
                    throw new IllegalArgumentException(e);
                }
            }
        });
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

    void syncRecords() {

        if (syncRecords == null) {
            businessRules.setLastCtx(getContext());
            workQueue = new LinkedBlockingQueue<Runnable>(maximumPoolSize);
            threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maximumPoolSize, keepAliveTime, TimeUnit.SECONDS, workQueue);

            syncRecords = new ReceiptFragment.SyncRecordsTask();
            Log.d(TAG, "syncRecordsAndEldProcessor: syncRecords: " + syncRecords);
            syncRecords.executeOnExecutor(threadPoolExecutor);
        }
    }

    public class SyncRecordsTask extends AsyncTask<String, Integer, Integer> {
        private int syncIntervalSecs = 1;
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
            Log.d(TAG, "SyncRecordsTask: doInBackground: syncTollRoadReceipt: ");
            long intervalCounter = 0;
            int progressCounter = 1;
            do {
//                Log.d(TAG, "SyncRecordsTask: doInBackground: do: started: ");
                try {
//                    boolean isFirstIteration = intervalCounter == 0;

                    //#region Sync Up
                    isSyncingUp = true;
                    //boolean isTimeToSyncUp = intervalCounter % (rules.isProductionMode() ? 15 * 60 : 2 * 60) == 0; // Every 15 mins for prod, every 2 min for debug
//                    boolean isTimeToSyncUp = intervalCounter % 60 == 0;
//                    boolean isTimeToSyncUp = intervalCounter % rules.getDriverReportingPeriod() == 0;

//                    if (isFirstIteration || isTimeToSyncUp) {
                    if (isFirstIteration) {
//                        Dec 05, 2022  -   Sync newly added fuel receipts (Upload)
                        syncFuelReceipts();
//                        Nov 25, 2022  -   Sync newly added toll receipts (Upload)
                        syncTollReceipts();
                    }

                    isSyncingUp = false;

                    //#endregion

                    //#region Sync Down

                    if (isFirstIteration) {
                        isSyncingDown = true;

//                        progressCounter = 1;  // Todo: Fernando - why is progressCounter initialized here?  Don't we want to include the upsync work load? -RAN 11/19/2021

//                        if (businessRules.existsPendingSyncItem())
//                            publishProgress(progressCounter += 5);
//                        if (businessRules.existsPendingSyncItem())
//                            publishProgress(progressCounter += 5);

//                        Dec 05, 2022  -   As a replica of toll receipts - working on fuel receipts - get saved record from server into our local db
                        businessRules.syncFuelReceipt("+");
//                        Nov 28, 2022  -   get saved record into our local db from server
                        businessRules.syncTollRoadReceipt("+");

//                        Log.d(TAG, "doInBackground: progressCounter: " + progressCounter);
//                        if (businessRules.existsPendingSyncItem())
//                            publishProgress(progressCounter += 5);
//                        if (businessRules.existsPendingSyncItem())
//                            publishProgress(progressCounter += 5);
//                        if (businessRules.existsPendingSyncItem())
//                            publishProgress(progressCounter += 5);
//                        if (businessRules.existsPendingSyncItem())
//                            publishProgress(progressCounter += 5);
//                        if (businessRules.existsPendingSyncItem())
//                            publishProgress(progressCounter += 5);
//                        if (businessRules.existsPendingSyncItem())
//                            publishProgress(progressCounter += 5);
//                        publishProgress(progressCounter += 5);
//                        Log.d(TAG, "doInBackground: progressCounter: " + progressCounter);
//                        if (false) {
//                            publishProgress(progressCounter += 5);
//                            publishProgress(progressCounter += 5);
//                            publishProgress(progressCounter += 5);
//                            publishProgress(progressCounter += 5);
//                            publishProgress(progressCounter += 5);
//                        }
//                        Log.d(TAG, "doInBackground: progressCounter: " + progressCounter);

                        publishProgress(100);

                        isFirstIteration = false;
                        isSyncingDown = false;
                        isSyncDownWithErrors = false;
                    }

                    //#endregion

//                    Log.d(TAG, "SyncRecordsTask: doInBackground: do started: ended: ");
                    Thread.sleep(syncIntervalSecs * 1000);
                    intervalCounter++;
                } catch (Throwable throwable) {
                    Log.d(TAG, "syncTollRoadReceipt: doInBackground: throwable: " + throwable.getMessage());
                    showLoadingLayout(false);
                    publishProgress(-1);

                    return BusinessRules.OK;
                }
            } while (true);
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            Log.d(TAG, "syncTollRoadReceipt: SyncRecordsTask: onProgressUpdate: ");
            try {
                int percent = values[0];
//                Log.d(TAG, "onProgressUpdate: percent: " + percent);
                if (percent == -1) {
                    showLoadingLayout(false);
                } else if (percent == -2) {
                    showLoadingLayout(false);
                    isSyncDownWithErrors = true;
                    UiUtils.showToast(getContext(), "Sync complete with errors");
                } else if (percent < 100) {
                    UiUtils.showToast(getContext(), "Syncing (" + percent + "%), please wait...");
                } else {
                    isFirstIteration = false;
                    showLoadingLayout(false);
                    UiUtils.showToast(getContext(), "Sync complete");

                    syncRecords.cancel(true);
                    syncRecords = null;
                    if (isFuelReceiptListShown) {
                        getFuelReceiptsContent();
                    } else {
                        getTollReceiptsContent();
                    }

                    //                        Means sync is done so now go and fetch new records of fuel and toll
                }
            } catch (Throwable throwable) {
                Log.d(TAG, "syncTollRoadReceipt: onProgressUpdate: throwable: " + throwable.getMessage());
                showLoadingLayout(false);
            }
        }

        @Override
        protected void onPostExecute(Integer result) {
            Log.d(TAG, "syncTollRoadReceipt: SyncRecordsTask: onPostExecute: result: " + result.toString());
            Log.d(TAG, "syncTollRoadReceipt: SyncRecordsTask: onPostExecute: isSyncingUp: " + isSyncingUp);
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

    private void syncTollReceipts() {
        Log.d(TAG, "save: syncTollReceipts: ");
        BusinessRules.logDebug("syncTollReceiptLogs: ");
        ArrayList<TollReceiptModel> result = businessRules.getPendingTollReceiptEntries();
        if (result != null) Log.d(TAG, "save: syncTollReceipts: result: " + result.size());
        for (TollReceiptModel tollReceiptModelEntry : result) {
            try {
                Log.d(TAG, "save: syncTollReceipts: sendPendingTollReceiptEntries: called: ");
                businessRules.sendPendingTollReceiptEntries(tollReceiptModelEntry);
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
        ArrayList<FuelReceiptModel> result = businessRules.getPendingFuelReceiptEntries();
        if (result != null) Log.d(TAG, "save: syncTollReceipts: result: " + result.size());
        for (FuelReceiptModel fuelReceiptModelEntry : result) {
            try {
                Log.d(TAG, "save: syncTollReceipts: sendPendingTollReceiptEntries: called: ");
                businessRules.sendPendingFuelReceiptEntries(fuelReceiptModelEntry);
                Log.d(TAG, "save: syncTollReceipts: setTollReceiptEntryToSent: called: ");
//                    Nov 25, 2022  -
//                    rules.setTollReceiptEntryToSent(tollReceiptModelEntry);
            } catch (Throwable throwable) {
                Log.d(TAG, "save: syncToll: throwable: " + throwable.getMessage());
                BusinessRules.logDebug("syncTollReceipts: Error while syncing up to RMS setTrailerEldEvent: " + fuelReceiptModelEntry.toString());
            }
        }
    }

    void showLoadingLayout(boolean showLoading) {
//        April 26, 2022    -   We should perform the layout relevant changes only in UIThread
//        Otherwise we sometimes stuck with crash
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (showLoading) {
                    loadingPanel.setVisibility(View.VISIBLE);
                } else {
                    loadingPanel.setVisibility(View.GONE);
                }
            }
        });
    }


}