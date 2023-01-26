package com.rco.rcotrucks.activities.fuelreceipts.fragments;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

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
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.activities.CreateFuelReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.activities.CreateTollReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.model.FuelReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.dialog.CurrencyAndUMDialog;
import com.rco.rcotrucks.fragments.BaseFragment;
import com.rco.rcotrucks.interfaces.CRUDInterface;
import com.rco.rcotrucks.utils.DecimalDigitsInputFilter;
import com.rco.rcotrucks.utils.SessionManagement;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

public class FuelReceiptDetail extends BaseFragment {

    private static final String TAG = FuelReceiptDetail.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    TextView amountTitle, gallonsTitle, title;
    //    , infoIcon
    ImageView backIcon, clearAmountIcon, clearGallonsIcon, clearFuelTypeIcon, clearTruckNumberIcon,
            fuelReceiptImage, imageViewCameraIcon, odometerIcon;
    EditText amountET, gallonsET, salesTaxET, vendorNameET, odometerET;
    String amountValue = "", gallonsValue = "", salesTaxValue = "", fuelTypeValue = "",
            stateValue = "", dateValue = "", odometerValue = "", stopValue = "";
    TextView dateTV, delete, save, odometerTitle;
    SearchableSpinner fuelCodeSpinner, stateSpinner, stopSpinner;

    int yearSelected, monthSelected, daySelected;
    java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getDefault());
    Date date;
    int day, month, year, dayOfWeek, dayOfMonth, dayOfYear;
    CalendarView calendarView;
    ConstraintLayout calendarLayout;
    CurrencyAndUMDialog currencyAndUMDialog;
    boolean isStateInitialAssigned = false, isFuelTypeInitiallySelected = false, isTruckStopInitiallySelected = false;
    User user;
    ReceiptModel intentReceiptModel;
    FuelReceiptModel intentFuelReceiptModel;
    //    ReceiptModel receiptModel;
    CRUDInterface crudInterface;

    int selectedYear, selectedMonth, selectedDayOfMonth;
    private Calendar initialCalendar;
    private SimpleDateFormat initialDateFormat;
    private String initialDate;


    public FuelReceiptDetail() {
    }

    public FuelReceiptDetail(CRUDInterface crudInterface) {
        this.crudInterface = crudInterface;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_fuel_receipt_detail, container, false);

        setIds(view);
        setDecimalDigitsInputFilter();
        setIdsFromActivity();
        getIntentData();
        initialize();
        initializeCalendar();
        setInitialDate();
        setListeners();

        setFuelCodeSpinnerAdapter();
        setStateSpinnerAdapter();
        setTruckStopSpinnerAdapter();
        return view;
    }

    void setIds(View view) {
        fuelReceiptImage = view.findViewById(R.id.fuelReceiptImage);
//        backIcon = view.findViewById(R.id.btn_back);
//        title = view.findViewById(R.id.tv_title);
//        save = view.findViewById(R.id.textViewSave);

        amountTitle = view.findViewById(R.id.amountTitle);
        gallonsTitle = view.findViewById(R.id.gallonsTitle);

        imageViewCameraIcon = view.findViewById(R.id.imageViewCameraIcon);
        clearAmountIcon = view.findViewById(R.id.clearAmountIcon);
        clearGallonsIcon = view.findViewById(R.id.clearGallonsIcon);
        clearFuelTypeIcon = view.findViewById(R.id.clearFuelTypeIcon);
        clearTruckNumberIcon = view.findViewById(R.id.clearTruckNumberIcon);

        amountET = view.findViewById(R.id.amount);
        gallonsET = view.findViewById(R.id.gallons);
        salesTaxET = view.findViewById(R.id.sales_tax);
        vendorNameET = view.findViewById(R.id.vendorName);
        odometerET = view.findViewById(R.id.odometer);
        odometerIcon = view.findViewById(R.id.odometerIcon);
        odometerTitle = view.findViewById(R.id.odometerTitle);

        fuelCodeSpinner = view.findViewById(R.id.fuelCodeSpinner);
        stateSpinner = view.findViewById(R.id.stateSpinner);
        stopSpinner = view.findViewById(R.id.truckStopSpinner);

        dateTV = view.findViewById(R.id.date);

        calendarLayout = view.findViewById(R.id.calendarLayout);
        calendarView = view.findViewById(R.id.calendarView);

//        infoIcon = view.findViewById(R.id.infoIcon);

    }

    //    Dec 08, 2022  -   We applied restriction of value to only 2 decimal points at max it on amount and sales tax
    void setDecimalDigitsInputFilter() {
        amountET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
        salesTaxET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
    }

    void setIdsFromActivity() {

        title = getActivity().findViewById(R.id.main_title);
        delete = getActivity().findViewById(R.id.delete_btn);


        save = getActivity().findViewById(R.id.main_app_bar_save);

//        title.setText("Fuel Receipt");
        getActivity().findViewById(R.id.cl_login).setVisibility(View.GONE);
//        delete.setVisibility(View.VISIBLE);
//        save.setVisibility(View.VISIBLE);
    }


    void getIntentData() {
//        intentReceiptModel = (ReceiptModel) getArguments().getSerializable(ReceiptFragment.EXTRA_MESSAGE_FUEL_RECEIPT_IDENT);
        intentReceiptModel = (ReceiptModel) getArguments().getParcelable(ReceiptFragment.EXTRA_MESSAGE_FUEL_RECEIPT_IDENT);
        if (intentReceiptModel != null) {
            convertDataModel();
            Log.d(TAG, "getIntentData: dateTime: " + intentReceiptModel.getFuelReceiptDateTime());
            dateTV.setText(intentReceiptModel.getFuelReceiptDateTime());
            gallonsET.setText(intentReceiptModel.getFuelReceiptGallons());
            amountET.setText(intentReceiptModel.getFuelReceiptAmount());
            salesTaxET.setText(intentReceiptModel.getFuelReceiptSalesTax());
            odometerET.setText(intentReceiptModel.getFuelReceiptOdometer());
        }
    }

    void initialize() {
        sessionManagement = new SessionManagement(getContext());
        progressDialog = new ProgressDialog(getContext());
        user = BusinessRules.instance().getAuthenticatedUser();

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

//        odometerET.setVisibility(View.GONE);
//        odometerIcon.setVisibility(View.GONE);
//        odometerTitle.setVisibility(View.GONE);
        setTruckNumber();
        setOdometer();
    }

    void setTruckNumber() {
        String truckNumber = rules.getCurrentTruckNumberFromEld();
        Log.d(TAG, "setTruckNumber: truckNumber: " + truckNumber);
        if (truckNumber != null) {
//            vendorNameET.setText(truckNumber);
        }
    }

    void setOdometer() {
        String odometer = rules.getBleParameter("Odometer");
        Log.d(TAG, "setOdometer: odometer: " + odometer);
        if (odometer != null) {
            odometerET.setText(odometer);
        }
    }

    void setListeners() {

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(getContext(), amountET);
                if (!validate()) {
                } else {
                    Log.d(TAG, "save: onClick: validated: ");
                    intentFuelReceiptModel.setFuelReceiptDateTime(dateValue);// TODO add time with it too Nov 25, 2022
                    intentFuelReceiptModel.setFuelReceiptGallons(gallonsValue);
                    intentFuelReceiptModel.setFuelReceiptAmount(amountValue);
                    intentFuelReceiptModel.setFuelReceiptSalesTax(salesTaxValue);
                    intentFuelReceiptModel.setFuelReceiptTruckStop(stopValue);
                    intentFuelReceiptModel.setFuelReceiptState(stateValue);
                    intentFuelReceiptModel.setFuelReceiptFuelType(fuelTypeValue);
                    intentFuelReceiptModel.setFuelReceiptOdometer(odometerValue);
                    Log.d(TAG, "save: onClick: whenEdit: userRecordId: " + intentFuelReceiptModel.getFuelReceiptUserRecordId());
                    rules.updateFuelReceiptEntry(intentFuelReceiptModel);
                    crudInterface.onSaveCalled(intentFuelReceiptModel);
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                crudInterface.onDeleteCalled(intentFuelReceiptModel);
            }
        });

        clearAmountIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                amountET.setText("");
            }
        });

        clearGallonsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                gallonsET.setText("");
            }
        });

        clearFuelTypeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                salesTaxET.setText("");
            }
        });

        clearTruckNumberIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

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

//        infoIcon.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                openCurrencyAndUMDialog();
//            }
//        });


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

    }

    boolean validate() {

        boolean valid = true;

        dateValue = dateTV.getText().toString();
        gallonsValue = gallonsET.getText().toString();
        amountValue = amountET.getText().toString().replace("$", "");
        salesTaxValue = salesTaxET.getText().toString();
        stopValue = stopSpinner.getSelectedItem().toString();
        stateValue = stateSpinner.getSelectedItem().toString();
        fuelTypeValue = fuelCodeSpinner.getSelectedItem().toString();
        odometerValue = odometerET.getText().toString();

        if (dateValue.isEmpty()) {
            dateTV.setError("Please enter date");
            valid = false;
        }

        if (gallonsValue.isEmpty()) {
            gallonsET.setError("Please enter quantity");
            valid = false;
        }


        if (amountValue.isEmpty()) {
            amountET.setError("Please enter amount");
            valid = false;
        } else if (amountValue.equalsIgnoreCase("0.00")) {
            amountET.setError("Please enter valid amount");
            valid = false;
        }

        if (salesTaxValue.isEmpty()) {
            salesTaxET.setError("Please enter sales tax");
            valid = false;
        }

        if (stopValue.isEmpty()) {
            Toast.makeText(getContext(), "Please enter truck stop", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (fuelTypeValue.isEmpty()) {
            Toast.makeText(getContext(), "Please select fuel type", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (stateValue.isEmpty()) {
            Toast.makeText(getContext(), "Please select state", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    void setStateSpinnerAdapter() {

//        stateSpinner.setAdapter(new ArrayAdapter<>(CreateFuelReceipt.this, android.R.layout.simple_spinner_dropdown_item, rules.getActiveStateList(CreateFuelReceipt.this)));
        stateSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.ifta_states_name)));
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        stateSpinner.setTitle("Search State");
        if (!isStateInitialAssigned) {
            Log.d(TAG, "setStateSpinnerAdapter: call setSelection() ");
            isStateInitialAssigned = true;
            stateSpinner.setSelection(getSelectedStatePosition());
        }
    }

    void setFuelCodeSpinnerAdapter() {
        fuelCodeSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.fuel_types_update)));
        fuelCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                This is the place which actually creating crash man
//                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        fuelCodeSpinner.setTitle("Search Fuel Code");
        if (!isFuelTypeInitiallySelected) {
            Log.d(TAG, "setStateSpinnerAdapter: call setSelection() ");
            isFuelTypeInitiallySelected = true;
            fuelCodeSpinner.setSelection(getSelectedFuelTypePosition());
        }
    }

    void setTruckStopSpinnerAdapter() {
        stopSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.truck_stops_list)));
        stopSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        stopSpinner.setTitle("Search Truck Stops");
        if (!isTruckStopInitiallySelected) {
            Log.d(TAG, "setStateSpinnerAdapter: call setSelection() ");
            isTruckStopInitiallySelected = true;
            stopSpinner.setSelection(getSelectedTruckStopPosition());
        }
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

//        dateTV.setText(selectedDate);
    }

    public void setInitialDate() {

        initialCalendar = Calendar.getInstance();
        initialDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        initialDate = initialDateFormat.format(initialCalendar.getTime());
//        dateTV.setText(initialDate);
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

    public void openCurrencyAndUMDialog() {
        currencyAndUMDialog = new CurrencyAndUMDialog(getContext(),
                new CurrencyAndUMDialog.CurrencyOptionInterface() {
                    @Override
                    public void onUsAndGallonsSelection() {

                        amountTitle.setText(getResources().getString(R.string.amount));
                        gallonsTitle.setText(getResources().getString(R.string.gallons));
                        currencyAndUMDialog.dismiss();
                    }

                    @Override
                    public void onCanAndLitersSelection() {
                        amountTitle.setText(getResources().getString(R.string.amountInCans));
                        gallonsTitle.setText(getResources().getString(R.string.liters));
                        currencyAndUMDialog.dismiss();
                    }

                    @Override
                    public void onPesosAndLitersSelection() {
                        amountTitle.setText(getResources().getString(R.string.amountInPeso));
                        gallonsTitle.setText(getResources().getString(R.string.liters));
                        currencyAndUMDialog.dismiss();
                    }

                    @Override
                    public void onCancelSelection() {
                        currencyAndUMDialog.dismiss();
                    }
                });
        currencyAndUMDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        currencyAndUMDialog.show();
    }

    String getSelectedStateIndex() {
        String[] stateArray = getResources().getStringArray(R.array.states_provinces_abbreviation);
        for (int i = 0; i < getResources().getStringArray(R.array.states_provinces_abbreviation).length; i++) {
            if (rules.getCurrentState(getActivity()).equalsIgnoreCase(stateArray[i])) {
                return stateArray[i];
            }
        }
        return "";
    }

//    void addNewFuelEntry() {
////        rules.insertFuelReceipt(parseDataModel(), false);
//    }

//    FuelReceiptModel parseDataModel() {
//        FuelReceiptModel fuelReceiptModel = new FuelReceiptModel();
//        String mobileRecordId = Rms.getMobileRecordId("FuelReceipt");
//        fuelReceiptModel.setFuelReceiptFirstName(user.getFirstName());
//        fuelReceiptModel.setFuelReceiptLastName(user.getLastName());
//        fuelReceiptModel.setFuelReceiptCompany(user.getCompany());
//        fuelReceiptModel.setFuelReceiptTruckNumber(user.getTruckNumber());
//        fuelReceiptModel.setFuelReceiptDOTNumber("");
//        fuelReceiptModel.setFuelReceiptVehicleLicenseNumber("");
//        fuelReceiptModel.setFuelReceiptUserRecordId(user.getRecordId());
//        fuelReceiptModel.setFuelReceiptMobileRecordId(mobileRecordId);
//        fuelReceiptModel.setFuelReceiptDateTime(dateValue);// TODO add time with it too Nov 25, 2022
//        fuelReceiptModel.setFuelReceiptGallons(gallonsValue);
//        fuelReceiptModel.setFuelReceiptAmount(amountValue);
//        fuelReceiptModel.setFuelReceiptSalesTax(salesTaxValue);
//        fuelReceiptModel.setFuelReceiptTruckStop(stopValue);
//        fuelReceiptModel.setFuelReceiptState(stateValue);
//        fuelReceiptModel.setFuelReceiptCountry("US");
//        fuelReceiptModel.setFuelReceiptFuelType(fuelTypeValue);
//        fuelReceiptModel.setFuelReceiptOdometer(odometerValue);
//        Log.d(TAG, "save: onClick: fuelReceiptModel: " + fuelReceiptModel);
//        return fuelReceiptModel;
//    }

    void convertDataModel() {

        intentFuelReceiptModel = new FuelReceiptModel();
        Log.d(TAG, "convertDataModel: truckStop: " + intentFuelReceiptModel.getFuelReceiptTruckStop());
        Log.d(TAG, "convertDataModel: truckStop: " + intentReceiptModel.getFuelReceiptTruckStop());
        intentFuelReceiptModel.setId(intentReceiptModel.getId());
        intentFuelReceiptModel.setFuelReceiptObjectId(intentReceiptModel.getFuelReceiptObjectId());
        intentFuelReceiptModel.setFuelReceiptObjectType(intentReceiptModel.getFuelReceiptObjectType());
        intentFuelReceiptModel.setFuelReceiptICSVRow(intentReceiptModel.getFuelReceiptICSVRow());
        intentFuelReceiptModel.setFuelReceiptMobileRecordId(intentReceiptModel.getFuelReceiptMobileRecordId());
        intentFuelReceiptModel.setFuelReceiptOrganizationNumber(intentReceiptModel.getFuelReceiptOrganizationName());
        intentFuelReceiptModel.setFuelReceiptCompany(intentReceiptModel.getFuelReceiptCompany());
        intentFuelReceiptModel.setFuelReceiptState(intentReceiptModel.getFuelReceiptState());
        intentFuelReceiptModel.setFuelReceiptBarCode(intentReceiptModel.getFuelReceiptBarCode());
        intentFuelReceiptModel.setFuelReceiptAmount(intentReceiptModel.getFuelReceiptAmount());
        intentFuelReceiptModel.setFuelReceiptFirstName(intentReceiptModel.getFuelReceiptFirstName());
        intentFuelReceiptModel.setFuelReceiptTruckStop(intentReceiptModel.getFuelReceiptTruckStop());
        intentFuelReceiptModel.setFuelReceiptObjectName(intentReceiptModel.getFuelReceiptObjectName());
        intentFuelReceiptModel.setFuelReceiptCreationTime(intentReceiptModel.getFuelReceiptCreationTime());
        intentFuelReceiptModel.setFuelReceiptCreationDate(intentReceiptModel.getFuelReceiptCreationDate());
        intentFuelReceiptModel.setFuelReceiptYear(intentReceiptModel.getFuelReceiptYear());
        intentFuelReceiptModel.setFuelReceiptRMSTimestamp(intentReceiptModel.getFuelReceiptRMSTimestamp());
        intentFuelReceiptModel.setFuelReceiptRMSCodingTimestamp(intentReceiptModel.getFuelReceiptRMSCodingTimestamp());
        intentFuelReceiptModel.setFuelReceiptRecordId(intentReceiptModel.getFuelReceiptRecordId());
        intentFuelReceiptModel.setFuelReceiptCountry(intentReceiptModel.getFuelReceiptCountry());
        intentFuelReceiptModel.setFuelReceiptLastName(intentReceiptModel.getFuelReceiptLastName());
        intentFuelReceiptModel.setFuelReceiptUserRecordId(intentReceiptModel.getFuelReceiptUserRecordId());

        intentFuelReceiptModel.setFuelReceiptTruckNumber(intentReceiptModel.getFuelReceiptTruckNumber());
        intentFuelReceiptModel.setFuelReceiptDOTNumber(intentReceiptModel.getFuelReceiptDOTNumber());
        intentFuelReceiptModel.setFuelReceiptVehicleLicenseNumber(intentReceiptModel.getFuelReceiptVehicleLicenseNumber());

        intentFuelReceiptModel.setFuelReceiptDateTime(intentReceiptModel.getFuelReceiptDateTime());
        intentFuelReceiptModel.setFuelReceiptGallons(intentReceiptModel.getFuelReceiptGallons());
        intentFuelReceiptModel.setFuelReceiptAmount(intentReceiptModel.getFuelReceiptAmount());
        intentFuelReceiptModel.setFuelReceiptSalesTax(intentReceiptModel.getFuelReceiptSalesTax());
        intentFuelReceiptModel.setFuelReceiptOdometer(intentReceiptModel.getFuelReceiptOdometer());
        intentFuelReceiptModel.setFuelReceiptState(intentReceiptModel.getFuelReceiptState());
        intentFuelReceiptModel.setFuelReceiptFuelType(intentReceiptModel.getFuelReceiptFuelType());
        intentFuelReceiptModel.setFuelReceiptTruckStop(intentReceiptModel.getFuelReceiptTruckStop());
    }

    int getSelectedStatePosition() {
        Log.d(TAG, "getSelectedStatePosition: ");
        String[] statesList = getResources().getStringArray(R.array.ifta_states_name);
        for (int i = 0; i < statesList.length; i++) {
            if (intentFuelReceiptModel.getFuelReceiptState().equalsIgnoreCase(statesList[i])) {
                Log.d(TAG, "getSelectedStatePosition: index: " + i);
                return i;
            }
        }
        return 0;
    }

    int getSelectedFuelTypePosition() {
        Log.d(TAG, "getSelectedStatePosition: ");
        String[] fuelTypeList = getResources().getStringArray(R.array.fuel_types_update);
        for (int i = 0; i < fuelTypeList.length; i++) {
            if (intentFuelReceiptModel.getFuelReceiptFuelType().equalsIgnoreCase(fuelTypeList[i])) {
                Log.d(TAG, "getSelectedStatePosition: index: " + i);
                return i;
            }
        }
        return 0;
    }

    int getSelectedTruckStopPosition() {
        Log.d(TAG, "getSelectedStatePosition: ");
        String[] truckStopList = getResources().getStringArray(R.array.truck_stops_list);
        for (int i = 0; i < truckStopList.length; i++) {
            Log.d(TAG, "getSelectedTruckStopPosition: " + intentFuelReceiptModel.getFuelReceiptTruckStop());
            if (intentFuelReceiptModel.getFuelReceiptTruckStop() != null) {
                if (intentFuelReceiptModel.getFuelReceiptTruckStop().equalsIgnoreCase(truckStopList[i])) {
                    Log.d(TAG, "getSelectedStatePosition: index: " + i);
                    return i;
                }
            }
        }
        return 0;
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