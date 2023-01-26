package com.rco.rcotrucks.activities.fuelreceipts.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Selection;
import android.text.TextWatcher;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.FuelReceiptListFragment;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperTollReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.utils.RmsHelperTollReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.utils.UiHelperFuelReceiptDtl;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.dialog.CurrencyAndUMDialog;
import com.rco.rcotrucks.model.RoadTollModel;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.DecimalDigitsInputFilter;
import com.rco.rcotrucks.utils.SessionManagement;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class CreateTollReceipt extends BaseActivity {

    private static final String TAG = CreateTollReceipt.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    TextView title, amountTitle, gallonsTitle;
    ImageView backIcon, clearDateIcon, clearAmountIcon, clearVendorNameIcon, fuelReceiptImage, imageViewCameraIcon;
    EditText amountET, vendorNameET;
    String dateValue = "", amountValue = "", vendorName = "", tollRoadValue = "", stateValue = "";
    TextView save, dateTV;
    SearchableSpinner tollRoadSpinner, stateSpinner;

    int yearSelected, monthSelected, daySelected;
    java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getDefault());
    Date date;
    int day, month, year, dayOfWeek, dayOfMonth, dayOfYear;
    CalendarView calendarView;
    ConstraintLayout calendarLayout;
    CurrencyAndUMDialog currencyAndUMDialog;
    String encodedImageData = "";
    Uri uri;
    boolean isStateInitialAssigned = false;
    User user;

    int selectedYear, selectedMonth, selectedDayOfMonth;
    private Calendar initialCalendar;
    private SimpleDateFormat initialDateFormat;
    private String initialDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_fuel_receipt);

        setIds();
        setDecimalDigitsInputFilter();
        initialize();
//        Dec 05, 2022  -   replaced with android default material date picker
//        initializeCalendar();
        setInitialDate();

        setListeners();
//        Dec 01, 2022  -   its behaving strange on spinners so add it in to do list
//        setOnTouchListener();
        setStateSpinnerAdapter();
    }


    void setIds() {
        fuelReceiptImage = findViewById(R.id.fuelReceiptImage);
        backIcon = findViewById(R.id.btn_back);
        save = findViewById(R.id.textViewSave);

        title = findViewById(R.id.tv_title);
        amountTitle = findViewById(R.id.amountTitle);
        gallonsTitle = findViewById(R.id.gallonsTitle);

        imageViewCameraIcon = findViewById(R.id.imageViewCameraIcon);
        clearDateIcon = findViewById(R.id.clearDateIcon);
        clearAmountIcon = findViewById(R.id.clearAmountIcon);
        clearVendorNameIcon = findViewById(R.id.clearVendorNameIcon);

        dateTV = findViewById(R.id.date);
        amountET = findViewById(R.id.amount);
        vendorNameET = findViewById(R.id.vendorName);

        tollRoadSpinner = findViewById(R.id.tollRoadSpinner);
        stateSpinner = findViewById(R.id.stateSpinner);

        calendarLayout = findViewById(R.id.calendarLayout);
        calendarView = findViewById(R.id.calendarView);
    }

    //    Dec 08, 2022  -   We applied restriction of value to only 2 decimal points at max it on amount only in too
    void setDecimalDigitsInputFilter() {
        amountET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
    }

    void initialize() {
        sessionManagement = new SessionManagement(CreateTollReceipt.this);
        progressDialog = new ProgressDialog(CreateTollReceipt.this);
        title.setText("Toll Receipt");
        user = BusinessRules.instance().getAuthenticatedUser();

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        setTruckNumber();
        setOdometer();
    }

    public void setInitialDate() {

        initialCalendar = Calendar. getInstance();
        initialDateFormat = new SimpleDateFormat("MM/dd/yyyy");
        initialDate = initialDateFormat.format(initialCalendar.getTime());
        dateTV.setText(initialDate);
    }

    void setTruckNumber() {
        String truckNumber = rules.getCurrentTruckNumberFromEld();
        Log.d(TAG, "setTruckNumber: truckNumber: " + truckNumber);
        if (truckNumber != null) {
//            vendorNameET.setText(truckNumber);
        }
    }

    void setOdometer() {
        String date = rules.getBleParameter("Odometer");
        Log.d(TAG, "setOdometer: date: " + date);
        if (date != null) {
//            dateTV.setText(date);
        }
    }

    void setListeners() {

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(CreateTollReceipt.this, amountET);
                if (!validate()) {
                } else {
                    addNewFuelEntry();
                }
            }
        });

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
                Log.d(TAG, "onClick: dateTV: ");
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

        imageViewCameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(CreateTollReceipt.this);
            }
        });

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
                    hideSoftKeyboard(CreateTollReceipt.this, amountET);
                }
            }
        });

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
//        Log.d(TAG, "save: validate: identRmsRecords: " + identRmsRecords);

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
            Toast.makeText(this, "Please enter road toll", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (stateValue.isEmpty()) {
            Toast.makeText(this, "Please enter state", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }


    void setStateSpinnerAdapter() {
        Log.d(TAG, "setStateSpinnerAdapter: ");
        ArrayList<String> activeStateList = rules.getActiveStateList(CreateTollReceipt.this);
        Log.d(TAG, "setStateSpinnerAdapter: activeStateList: size: " + activeStateList.size());
        for (int i = 0; i < activeStateList.size(); i++) {
            Log.d(TAG, "setStateSpinnerAdapter: activeStateList: value: " + activeStateList.get(i));
        }

//        stateSpinner.setAdapter(new ArrayAdapter<>(CreateTollReceipt.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.states_provinces_abbreviation)));
        stateSpinner.setAdapter(new ArrayAdapter<>(CreateTollReceipt.this, android.R.layout.simple_spinner_dropdown_item, rules.getActiveStateList(CreateTollReceipt.this)));
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                hideSoftKeyboard(CreateTollReceipt.this, amountET);
                if (!isStateInitialAssigned) {
                    isStateInitialAssigned = true;
                }

//                Dec 04, 2022  -   we are having an crash while trying to get first index value
//                String selectedState = ((TextView) adapterView.getChildAt(0)).getText().toString();
                String selectedState = stateSpinner.getSelectedItem().toString();
                Log.d(TAG, "onItemSelected: stateSpinner: title: selectedState: " + selectedState);
                if (selectedState.isEmpty()) {
                    return;
                }

                ArrayList<RoadTollModel> selectedStateRoadsList = rules.getRoadsList(selectedState, CreateTollReceipt.this);
                Log.d(TAG, "onItemSelected: selectedStateRoadsList: size: " + selectedStateRoadsList.size());
                setStateRoads(selectedStateRoadsList);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                hideSoftKeyboard(getContext(), billerName);
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        stateSpinner.setTitle("Search State");
    }


    void initializeCalendar() {

//        calendar = java.util.Calendar.getInstance(TimeZone.getDefault());
//        date = calendar.getTime();
//        day = calendar.get(java.util.Calendar.DATE);
//        month = calendar.get(java.util.Calendar.MONTH);
//        year = calendar.get(java.util.Calendar.YEAR);
//        dayOfWeek = calendar.get(java.util.Calendar.DAY_OF_WEEK);
//        dayOfMonth = calendar.get(java.util.Calendar.DAY_OF_MONTH);
//        dayOfYear = calendar.get(java.util.Calendar.DAY_OF_YEAR);
//        Log.d(TAG, "initializeCalendar: year: " + getYear() + " month: " + getMonth() + " day: " + getDay());
//
//
//        yearSelected = getYear();
//        monthSelected = (getMonth() + 1);
//        daySelected = getDay();
//
////        String selectedDate = yearSelected + "-" + monthSelected + "-" + daySelected;
//        String selectedDate = monthSelected + "/" + daySelected + "/" + yearSelected;
//        Log.d(TAG, "onSelectedDayChange: selectedDate: " + selectedDate);

//        dateTV.setText(selectedDate);
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
        currencyAndUMDialog = new CurrencyAndUMDialog(CreateTollReceipt.this,
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == REQUEST_CODE_FOR_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                uri = data.getParcelableExtra("path");
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    encodedImageData = getEncoded64ImageStringFromBitmap(bitmap);
                    Log.d(TAG, "onActivityResult: encodedImageData: " + encodedImageData);

                    fuelReceiptImage.setVisibility(View.VISIBLE);
                    loadProfile(CreateTollReceipt.this, uri.toString(), fuelReceiptImage, false);
                } catch (IOException e) {
                    Log.d(TAG, "onActivityResult: IOException: " + e.getMessage());
                }
            }
        }
    }

    String getSelectedStateIndex() {
        String[] stateArray = getResources().getStringArray(R.array.states_provinces_abbreviation);
        for (int i = 0; i < getResources().getStringArray(R.array.states_provinces_abbreviation).length; i++) {
            if (rules.getCurrentState(CreateTollReceipt.this).equalsIgnoreCase(stateArray[i])) {
                return stateArray[i];
            }
        }
        return "";
    }

    void setStateRoads(ArrayList<RoadTollModel> selectedStateRoadsList) {
        ArrayList<String> roadsList = new ArrayList<>();
        for (int i = 0; i < selectedStateRoadsList.size(); i++) {
            roadsList.add(selectedStateRoadsList.get(i).getRoadName());
        }
        setRoadSpinnerAdapter(roadsList, selectedStateRoadsList);
    }

    void setRoadSpinnerAdapter(ArrayList<String> roadsList, ArrayList<RoadTollModel> selectedStateRoadsList) {

        if (selectedStateRoadsList.size() == 0) {
            vendorNameET.setText("");
        }

        tollRoadSpinner.setAdapter(new ArrayAdapter<>(CreateTollReceipt.this, android.R.layout.simple_spinner_dropdown_item, roadsList));
        tollRoadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                hideSoftKeyboard(CreateTollReceipt.this, amountET);
//                Dec 04, 2022  -   adapterView.getChildAt(0)) is creating a crash
//                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                vendorNameET.setText(selectedStateRoadsList.get(i).getVendorName());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                hideSoftKeyboard(getContext(), billerName);
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        tollRoadSpinner.setTitle("Search Toll Roads");
    }


    void addNewFuelEntry() {
        Log.d(TAG, "fuelReceipt: addNewFuelEntry: ");
        Intent intent = new Intent();

        int newlyAddedEntryId = rules.insertTollReceipt(parseDataModel(), false);
        intent.putExtra("value", newlyAddedEntryId);
        Log.d(TAG, "tollReceipt: addNewTollEntry: newlyAddedEntryId: " + newlyAddedEntryId);
        setResult(1, intent);

        finish();
    }

    TollReceiptModel parseDataModel() {

        TollReceiptModel tollReceiptModel = new TollReceiptModel();
        String mobileRecordId = Rms.getMobileRecordId("TollReceipt");
        tollReceiptModel.setTollReceiptDateTime(dateValue);// TODO add time with it too Nov 25, 2022
        tollReceiptModel.setTollReceiptFirstName(user.getFirstName());
        tollReceiptModel.setTollReceiptLastName(user.getLastName());
        tollReceiptModel.setTollReceiptCompany(user.getCompany());
        tollReceiptModel.setTollReceiptTruckNumber(user.getTruckNumber());
        tollReceiptModel.setTollReceiptDotNumber("");
        tollReceiptModel.setTollReceiptVehicleLicenseNumber("");
        tollReceiptModel.setTollReceiptVendorName(vendorName);
        tollReceiptModel.setTollReceiptVendorState(stateValue);
        tollReceiptModel.setTollReceiptVendorCountry("US");
        tollReceiptModel.setTollReceiptAmount(amountValue);
        tollReceiptModel.setTollReceiptUserRecordId(user.getRecordId());
        tollReceiptModel.setTollReceiptMobileRecordId(mobileRecordId);
        tollReceiptModel.setTollReceiptRoadName(tollRoadValue);
        return tollReceiptModel;
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateTollReceipt.this,
                dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);
        datePickerDialog.show();
    }

}