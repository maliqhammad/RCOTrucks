package com.rco.rcotrucks.activities.fuelreceipts.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.util.Pair;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.activities.CreateTollReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.activities.EditTollReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.activities.FuelReceiptDtlActivity;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.FuelReceiptListAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.ReceiptAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperTollReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.utils.UiHelperFuelReceiptDtl;
import com.rco.rcotrucks.activities.fuelreceipts.utils.UiHelperFuelReceiptList;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.dialog.CurrencyAndUMDialog;
import com.rco.rcotrucks.dialog.DateFilterDialog;
import com.rco.rcotrucks.fragments.BaseFragment;
import com.rco.rcotrucks.interfaces.CRUDInterface;
import com.rco.rcotrucks.interfaces.ReceiptListener;
import com.rco.rcotrucks.model.RoadTollModel;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.DecimalDigitsInputFilter;
import com.rco.rcotrucks.utils.SessionManagement;
import com.rco.rcotrucks.utils.UiUtils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TollReceiptDetail extends BaseFragment {

    private static final String TAG = TollReceiptDetail.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    ImageView clearDateIcon, clearAmountIcon, clearVendorNameIcon, editTollReceipt;
    EditText amountET, vendorNameET;
    TextView dateTV, title, save, delete;
    SearchableSpinner tollRoadSpinner, stateSpinner;
    ReceiptModel receiptModel;
    TollReceiptModel tollReceiptModel;

    java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getDefault());
    Date date;
    int day, month, year, dayOfWeek, dayOfMonth, dayOfYear;
    CalendarView calendarView;
    ConstraintLayout calendarLayout;
    int yearSelected, monthSelected, daySelected;

    boolean isStateInitialAssigned = false, isRoadInitialAssigned = false;
    String dateValue = "", amountValue = "", vendorName = "", tollRoadValue = "", stateValue = "";

    CRUDInterface crudInterface;

    int selectedYear, selectedMonth, selectedDayOfMonth;
    private Calendar initialCalendar;
    private SimpleDateFormat initialDateFormat;
    private String initialDate;

    public TollReceiptDetail() {
    }

    public TollReceiptDetail(CRUDInterface crudInterface) {
        this.crudInterface = crudInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toll_receipt_detail2, container, false);

        setIds(view);
        setDecimalDigitsInputFilter();
        setIdsFromActivity();
        getIntentData();
        initialize();
        setInitialDate();
        setListeners();

        setStateSpinnerAdapter();
        return view;
    }


    void setIds(View view) {

        clearDateIcon = view.findViewById(R.id.clearDateIcon);
        clearAmountIcon = view.findViewById(R.id.clearAmountIcon);
        clearVendorNameIcon = view.findViewById(R.id.clearVendorNameIcon);
        editTollReceipt = view.findViewById(R.id.edit_toll_receipt);

        dateTV = view.findViewById(R.id.date);
        amountET = view.findViewById(R.id.amount);
        amountET.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});
        vendorNameET = view.findViewById(R.id.vendorName);
        tollRoadSpinner = view.findViewById(R.id.tollRoadSpinner);
        stateSpinner = view.findViewById(R.id.stateSpinner);

        calendarLayout = view.findViewById(R.id.calendarLayout);
        calendarView = view.findViewById(R.id.calendarView);
    }

    //    Dec 08, 2022  -   We applied restriction of value to only 2 decimal points at max it on amount only in too
    void setDecimalDigitsInputFilter() {
        amountET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
    }

    void setIdsFromActivity() {
        title = getActivity().findViewById(R.id.main_title);
        delete = getActivity().findViewById(R.id.delete_btn);
        save = getActivity().findViewById(R.id.main_app_bar_save);

//        title.setText("Toll Receipt");
        getActivity().findViewById(R.id.cl_login).setVisibility(View.GONE);
//        delete.setVisibility(View.VISIBLE);
//        save.setVisibility(View.VISIBLE);
    }

    void getIntentData() {
//        receiptModel = (ReceiptModel) getArguments().getSerializable(ReceiptFragment.EXTRA_MESSAGE_FUEL_RECEIPT_IDENT);
        receiptModel = (ReceiptModel) getArguments().getParcelable(ReceiptFragment.EXTRA_MESSAGE_FUEL_RECEIPT_IDENT);
        if (receiptModel != null) {
            convertDataModel();
        }
    }

    void initialize() {
        sessionManagement = new SessionManagement(getContext());
        progressDialog = new ProgressDialog(getContext());

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    }

    public void setInitialDate() {

        initialCalendar = Calendar.getInstance();
        initialDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        initialDate = initialDateFormat.format(initialCalendar.getTime());
//        dateTV.setText(initialDate);
    }

    void setListeners() {

        clearDateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateTV.setText("");
            }
        });

        clearAmountIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountET.setText("");
            }
        });

        clearVendorNameIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                vendorNameET.setText("");
            }
        });

        dateTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                calendarLayout.setVisibility(View.VISIBLE);
                openDateSelectionDialog();
            }
        });

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView calendarView, int year, int month, int dayOfMonth) {
                yearSelected = year;
                monthSelected = month + 1;
                daySelected = dayOfMonth;

//                String selectedDate = monthSelected + "-" + daySelected + "-" + yearSelected;
                String selectedDate = yearSelected + "-" + monthSelected + "-" + daySelected;
                Log.d(TAG, "onSelectedDayChange: selectedDate: " + selectedDate);

                dateTV.setText(selectedDate);
                calendarLayout.setVisibility(View.GONE);
            }
        });

        calendarLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarLayout.setVisibility(View.GONE);
            }
        });

        editTollReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Log.d(TAG, "save: onClick: intent: " + receiptModel.getTollReceiptDateTime()
                        + ", " + receiptModel.getTollReceiptDateTime()
                        + ", " + receiptModel.getTollReceiptAmount()
                        + ", " + receiptModel.getTollReceiptVendorState()
                        + ", " + receiptModel.getTollReceiptRoadName()
                        + ", " + receiptModel.getTollReceiptVendorName()
                        + ", " + receiptModel.getTollReceiptMobileRecordId()
                        + ", userRecordId: " + receiptModel.getUserRecordId()
                );
                Intent intent = new Intent(getContext(), EditTollReceipt.class);
                intent.putExtra("receiptModel", receiptModel);
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crudInterface.onDeleteCalled(receiptModel);
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(getContext(), amountET);
                if (!validate()) {
                } else {

                    tollReceiptModel.setTollReceiptDateTime(dateValue);// TODO add time with it too Nov 25, 2022
                    tollReceiptModel.setTollReceiptVendorName(vendorName);
                    tollReceiptModel.setTollReceiptVendorState(stateValue);
                    tollReceiptModel.setTollReceiptAmount(amountValue);
                    tollReceiptModel.setTollReceiptRoadName(tollRoadValue);
                    Log.d(TAG, "save: onClick: whenEdit: userRecordId: " + tollReceiptModel.getTollReceiptUserRecordId());
                    rules.updateTollReceiptEntry(tollReceiptModel);
//                    finish();
//                    I think I should add a listener here so whenever there is a change in a entry I should update the list
                    crudInterface.onSaveCalled(tollReceiptModel);
                }
            }
        });

//        amountET.setRawInputType(Configuration.KEYBOARD_12KEY);
//        amountET.addTextChangedListener(new TextWatcher() {
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//                if (!charSequence.toString().matches("^\\$(\\d{1,3}(\\,\\d{3})*|(\\d+))(\\.\\d{2})?$")) {
//                    String userInput = "" + charSequence.toString().replaceAll("[^\\d]", "");
//                    StringBuilder cashAmountBuilder = new StringBuilder(userInput);
//
//                    while (cashAmountBuilder.length() > 3 && cashAmountBuilder.charAt(0) == '0') {
//                        cashAmountBuilder.deleteCharAt(0);
//                    }
//                    while (cashAmountBuilder.length() < 3) {
//                        cashAmountBuilder.insert(0, '0');
//                    }
//                    cashAmountBuilder.insert(cashAmountBuilder.length() - 2, '.');
//                    cashAmountBuilder.insert(0, '$');
////                    cashAmountBuilder.insert(0, "");
//
//                    amountET.setText(cashAmountBuilder.toString());
//                    // keeps the cursor always to the right
//                    Selection.setSelection(amountET.getText(), cashAmountBuilder.toString().length());
//                }
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//
//            }
//        });

        amountET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: hasFocus: " + hasFocus);
                if (!hasFocus) {
                    if (getContext() != null) {
                        hideSoftKeyboard(getContext(), amountET);
                    }
                }
            }
        });

    }

    void convertDataModel() {
        Log.d(TAG, "save: onClick: getIntentData: receiptModel: " + receiptModel.getTollReceiptDateTime()
                + ", " + receiptModel.getTollReceiptAmount()
                + ", " + receiptModel.getTollReceiptVendorState()
                + ", " + receiptModel.getTollReceiptRoadName()
                + ", " + receiptModel.getTollReceiptVendorName()
                + ", mobilerecordId: " + receiptModel.getTollReceiptMobileRecordId()
        );


        tollReceiptModel = new TollReceiptModel();
        tollReceiptModel.setTollReceiptObjectId("" + receiptModel.getTollReceiptObjectId());
        tollReceiptModel.setTollReceiptObjectType(receiptModel.getTollReceiptObjectType());
        tollReceiptModel.setTollReceiptICSVRow("" + receiptModel.getTollReceiptICSVRow());
        tollReceiptModel.setTollReceiptMobileRecordId(receiptModel.getTollReceiptMobileRecordId());
        tollReceiptModel.setTollReceiptOrganizationNumber(receiptModel.getTollReceiptOrganizationName());
        tollReceiptModel.setTollReceiptCompany(receiptModel.getTollReceiptCompany());
        tollReceiptModel.setTollReceiptVendorState(receiptModel.getTollReceiptVendorState());
        tollReceiptModel.setTollReceiptBarCode(receiptModel.getTollReceiptBarCode());
        tollReceiptModel.setTollReceiptAmount(receiptModel.getTollReceiptAmount());
        tollReceiptModel.setTollReceiptFirstName(receiptModel.getTollReceiptFirstName());
        tollReceiptModel.setTollReceiptVendorName(receiptModel.getTollReceiptVendorName());
        tollReceiptModel.setTollReceiptDateTime(receiptModel.getTollReceiptDateTime());
        tollReceiptModel.setTollReceiptObjectName(receiptModel.getTollReceiptObjectName());
        tollReceiptModel.setTollReceiptCreationTime(receiptModel.getTollReceiptCreationTime());
        tollReceiptModel.setTollReceiptCreationDate(receiptModel.getTollReceiptCreationDate());
        tollReceiptModel.setTollReceiptYear(receiptModel.getTollReceiptYear());
        tollReceiptModel.setTollReceiptRMSTimestamp(receiptModel.getTollReceiptRMSTimestamp());
        tollReceiptModel.setTollReceiptRMSCodingTimestamp(receiptModel.getTollReceiptRMSCodingTimestamp());
        tollReceiptModel.setTollReceiptRecordId(receiptModel.getTollReceiptRecordId());
        tollReceiptModel.setTollReceiptVendorCountry(receiptModel.getTollReceiptVendorCountry());
        tollReceiptModel.setTollReceiptLastName(receiptModel.getTollReceiptLastName());
        tollReceiptModel.setTollReceiptDay(receiptModel.getTollReceiptDay());
        tollReceiptModel.setTollReceiptRoadName(receiptModel.getTollReceiptRoadName());
        tollReceiptModel.setTollReceiptUserRecordId(receiptModel.getTollReceiptUserRecordId());

        tollReceiptModel.setTollReceiptTruckNumber(receiptModel.getTollReceiptTruckNumber());
        tollReceiptModel.setTollReceiptDotNumber(receiptModel.getTollReceiptDOTNumber());
        tollReceiptModel.setTollReceiptVehicleLicenseNumber(receiptModel.getTollReceiptVehicleLicenseNumber());
        Log.d(TAG, "save: getIntentData: while initialization: user: " + receiptModel.getUserRecordId());
        Log.d(TAG, "save: getIntentData: while initialization: " + tollReceiptModel.getTollReceiptUserRecordId());


        Log.d(TAG, "save: onClick: getIntentData: tollReceiptModel: "
                + tollReceiptModel.getTollReceiptDateTime()
                + ", " + tollReceiptModel.getTollReceiptAmount()
                + ", " + tollReceiptModel.getTollReceiptVendorState()
                + ", " + tollReceiptModel.getTollReceiptRoadName()
                + ", " + tollReceiptModel.getTollReceiptVendorName()
                + ", " + tollReceiptModel.getTollReceiptMobileRecordId()
        );

        dateTV.setText(receiptModel.getTollReceiptDateTime());
        amountET.setText(receiptModel.getTollReceiptAmount());
        vendorNameET.setText(receiptModel.getVendorName());

        stateValue = receiptModel.getVendorState();
        tollRoadValue = receiptModel.getTollReceiptRoadName();
    }

    void setStateSpinnerAdapter() {
        Log.d(TAG, "setStateSpinnerAdapter: ");
        ArrayList<String> activeStateList = rules.getActiveStateList(getContext());
        Log.d(TAG, "setStateSpinnerAdapter: activeStateList: size: " + activeStateList.size());
        for (int i = 0; i < activeStateList.size(); i++) {
            Log.d(TAG, "setStateSpinnerAdapter: activeStateList: value: " + activeStateList.get(i));
        }

        stateSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, rules.getActiveStateList(getContext())));
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                if (getContext() != null) {
//                    hideSoftKeyboard(getContext(), amountET);
                UiUtils.closeKeyboard(vendorNameET);
//                }
                Log.d(TAG, "onItemSelected: isStateInitialAssigned: " + isStateInitialAssigned);
//                Dec 04, 2022  -   This flag makes sure that we don't override setSelection for initial selection screen because its a detail screen
                if (isStateInitialAssigned) {
                    String selectedState = stateSpinner.getSelectedItem().toString();
                    Log.d(TAG, "onItemSelected: selectedState: " + selectedState);

                    ArrayList<RoadTollModel> selectedStateRoadsList = rules.getRoadsList(selectedState, getContext());
                    Log.d(TAG, "onItemSelected: selectedStateRoadsList: size: " + selectedStateRoadsList.size());
                    setStateRoads(selectedStateRoadsList);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        stateSpinner.setTitle("Search State");

        Log.d(TAG, "setStateSpinnerAdapter: isStateInitialAssigned: " + isStateInitialAssigned);
        if (!isStateInitialAssigned) {
            Log.d(TAG, "setStateSpinnerAdapter: call setSelection() ");
            isStateInitialAssigned = true;
            stateSpinner.setSelection(getSelectedStatePosition());
        }
    }

    int getSelectedStatePosition() {
        Log.d(TAG, "getSelectedStatePosition: ");
        ArrayList<String> activeStateList = rules.getActiveStateList(getContext());
        for (int i = 0; i < activeStateList.size(); i++) {
            if (tollReceiptModel.getTollReceiptVendorState().equalsIgnoreCase(activeStateList.get(i))) {
                Log.d(TAG, "getSelectedStatePosition: index: " + i);
                return i;
            }
        }
        return 0;
    }

    void setStateRoads(ArrayList<RoadTollModel> selectedStateRoadsList) {
        Log.d(TAG, "setStateRoads: ");
        ArrayList<String> roadsList = new ArrayList<>();
        for (int i = 0; i < selectedStateRoadsList.size(); i++) {
            Log.d(TAG, "setStateRoads: item: " + selectedStateRoadsList.get(i).getRoadName() + " index: " + i);
            roadsList.add(selectedStateRoadsList.get(i).getRoadName());
        }
        Log.d(TAG, "setStateRoads: roadList: size: " + roadsList.size());
        setRoadSpinnerAdapter(roadsList, selectedStateRoadsList);
    }

    void setRoadSpinnerAdapter(ArrayList<String> roadsList, ArrayList<RoadTollModel> selectedStateRoadsList) {
        if (selectedStateRoadsList.size() == 0) {
            vendorNameET.setText("");
        }
        Log.d(TAG, "setRoadSpinnerAdapter: roadsList: " + roadsList);
        tollRoadSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, roadsList));
        tollRoadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (isRoadInitialAssigned) {
                    vendorNameET.setText(selectedStateRoadsList.get(i).getVendorName());
                }
//                if (getContext() != null) {
//                    hideSoftKeyboard(getContext(), amountET);
                UiUtils.closeKeyboard(vendorNameET);
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                hideSoftKeyboard(getContext(), billerName);
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        tollRoadSpinner.setTitle("Search Toll Roads");
        if (!isRoadInitialAssigned) {
            Log.d(TAG, "setStateSpinnerAdapter: call setSelection() ");
            isRoadInitialAssigned = true;
            tollRoadSpinner.setSelection(getSelectedRoadPosition());
        }
    }

    int getSelectedRoadPosition() {
        Log.d(TAG, "getSelectedRoadPosition: ");
        ArrayList<RoadTollModel> selectedStateRoadsList = rules.getRoadsList(tollReceiptModel.getTollReceiptVendorState(), getContext());
        for (int i = 0; i < selectedStateRoadsList.size(); i++) {
            if (tollReceiptModel.getTollReceiptRoadName().equalsIgnoreCase(selectedStateRoadsList.get(i).getRoadName())) {
                vendorNameET.setText(tollReceiptModel.getTollReceiptVendorName());
                return i;
            }
        }
        return 0;
    }

    void initializeCalendar() {

        calendar = java.util.Calendar.getInstance(TimeZone.getDefault());
        date = calendar.getTime();
        day = calendar.get(java.util.Calendar.DATE);
        month = calendar.get(java.util.Calendar.MONTH);
        year = calendar.get(java.util.Calendar.YEAR);
        dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
        dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH);
        dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR);
        Log.d(TAG, "initializeCalendar: year: " + getYear() + " month: " + getMonth() + " day: " + getDay());


        yearSelected = getYear();
        monthSelected = (getMonth() + 1);
        daySelected = getDay();

        String selectedDate = yearSelected + "-" + monthSelected + "-" + daySelected;
        Log.d(TAG, "onSelectedDayChange: selectedDate: " + selectedDate);

        dateTV.setText(selectedDate);
    }

    public int getDay() {
        return day;
    }

    public int getMonth() {
        return month;
    }

    public int getYear() {
        return year;
    }


    boolean validate() {

        boolean valid = true;

        dateValue = dateTV.getText().toString();
        amountValue = amountET.getText().toString().replace("$", "");
        vendorName = vendorNameET.getText().toString();
        tollRoadValue = tollRoadSpinner.getSelectedItem().toString();
        stateValue = stateSpinner.getSelectedItem().toString();

        Log.d(TAG, "save: validate: date: " + dateValue);
        Log.d(TAG, "save: validate: amount: " + amountValue);
        Log.d(TAG, "save: validate: vendor: " + vendorName);
        Log.d(TAG, "save: validate: road: " + tollRoadValue);
        Log.d(TAG, "save: validate: state: " + stateValue);

        if (dateValue.isEmpty()) {
            dateTV.setError("Please enter date");
            valid = false;
        }

        if (amountValue.isEmpty()) {
            amountET.setError("Please enter amount");
            valid = false;
        }


        if (vendorName.isEmpty()) {
            vendorNameET.setError("Please enter sales tax");
            valid = false;
        }

        if (tollRoadValue.isEmpty()) {
            Toast.makeText(getContext(), "Please enter road toll", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (stateValue.isEmpty()) {
            Toast.makeText(getContext(), "Please enter state", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    void openDateSelectionDialog() {
        DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                String selectedDate = String.format("%02d/%02d/%02d", (monthOfYear + 1), dayOfMonth, year);
//             String currentTime = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_TIME_MILLIS);

                dateTV.setText(selectedDate);
                Log.d(TAG, "onDateSet: selectedDate: " + selectedDate + " now: call: afterTextChanged: ");
            }
        };
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(),
                dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);
        datePickerDialog.show();
    }

}