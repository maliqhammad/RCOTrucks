package com.rco.rcotrucks.activities.fuelreceipts.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Editable;
import android.text.InputFilter;
import android.text.Selection;
import android.text.TextWatcher;
import android.util.Log;
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

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.FuelReceiptListFragment;
import com.rco.rcotrucks.activities.fuelreceipts.model.FuelReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.logbook.activity.EldEdit;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.dialog.CurrencyAndUMDialog;
import com.rco.rcotrucks.dialog.DropDialog;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.DecimalDigitsInputFilter;
import com.rco.rcotrucks.utils.ImagePickerActivity;
import com.rco.rcotrucks.utils.SessionManagement;
import com.rco.rcotrucks.utils.StringUtils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import android.os.Bundle;

import com.rco.rcotrucks.R;

public class CreateFuelReceipt extends BaseActivity {

    private static final String TAG = CreateFuelReceipt.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    ImageView backIcon, clearAmountIcon, clearGallonsIcon, clearFuelTypeIcon, clearTruckNumberIcon, infoIcon, fuelReceiptImage, imageViewCameraIcon;
    EditText amountET, gallonsET, salesTaxET, vendorNameET, odometerET;
    String amountValue = "", gallonsValue = "", salesTaxValue = "", fuelTypeValue = "", fuelCodeValue = "",
            stateValue = "", dateValue = "", odometerValue = "", stopValue = "";
    int pricePerGallonsValue = 0;
    TextView dateTV, amountTitle, gallonsTitle, title, save;
    SearchableSpinner fuelCodeSpinner, stateSpinner, stopSpinner;

    int yearSelected, monthSelected, daySelected;
    java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getDefault());
    Date date;
    int day, month, year, dayOfWeek, dayOfMonth, dayOfYear;
    CalendarView calendarView;
    ConstraintLayout calendarLayout;
    private BusHelperRmsCoding.RmsRecords identRmsRecords;
    CurrencyAndUMDialog currencyAndUMDialog;
    String encodedImageData = "";
    Uri uri;
    private BusHelperFuelReceipts busRules = BusHelperFuelReceipts.instance();
    boolean isStateInitialAssigned = false;
    User user;

    int selectedYear, selectedMonth, selectedDayOfMonth;
    private Calendar initialCalendar;
    private SimpleDateFormat initialDateFormat;
    private String initialDate;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_fuel_receipt2);

        setIds();
        setDecimalDigitsInputFilter();
        initialize();
        setInitialDate();
//        initializeCalendar();
        setListeners();

        setFuelCodeSpinnerAdapter();
        setStateSpinnerAdapter();
        setTruckStopSpinnerAdapter();
    }

    void setIds() {
        fuelReceiptImage = findViewById(R.id.fuelReceiptImage);
        backIcon = findViewById(R.id.btn_back);
        title = findViewById(R.id.tv_title);
        save = findViewById(R.id.textViewSave);

        dateTV = findViewById(R.id.date);
        amountTitle = findViewById(R.id.amountTitle);
        gallonsTitle = findViewById(R.id.gallonsTitle);

        imageViewCameraIcon = findViewById(R.id.imageViewCameraIcon);
        clearAmountIcon = findViewById(R.id.clearAmountIcon);
        clearGallonsIcon = findViewById(R.id.clearGallonsIcon);
        clearFuelTypeIcon = findViewById(R.id.clearFuelTypeIcon);
        clearTruckNumberIcon = findViewById(R.id.clearTruckNumberIcon);

        amountET = findViewById(R.id.amount);
        amountET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
        gallonsET = findViewById(R.id.gallons);
        salesTaxET = findViewById(R.id.sales_tax);
        vendorNameET = findViewById(R.id.vendorName);
        odometerET = findViewById(R.id.odometer);

        fuelCodeSpinner = findViewById(R.id.fuelCodeSpinner);
        stateSpinner = findViewById(R.id.stateSpinner);
        stopSpinner = findViewById(R.id.truckStopSpinner);

        dateTV = findViewById(R.id.date);

        calendarLayout = findViewById(R.id.calendarLayout);
        calendarView = findViewById(R.id.calendarView);

        infoIcon = findViewById(R.id.infoIcon);

    }

    //    Dec 08, 2022  -   We applied restriction of value to only 2 decimal points at max it on amount and sales tax
    void setDecimalDigitsInputFilter() {
        amountET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
        salesTaxET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
    }

    void getIntentData() {
        Intent intent = getIntent();
        Serializable message = intent.getSerializableExtra(FuelReceiptListFragment.EXTRA_MESSAGE_FUEL_RECEIPT_IDENT);

        if (message != null && message instanceof BusHelperRmsCoding.RmsRecords)
            identRmsRecords = (BusHelperRmsCoding.RmsRecords) message;
        else {
            identRmsRecords = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);
        }
    }

    void initialize() {
        sessionManagement = new SessionManagement(CreateFuelReceipt.this);
        progressDialog = new ProgressDialog(CreateFuelReceipt.this);
        user = BusinessRules.instance().getAuthenticatedUser();

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        title.setText("Create Fuel Receipt");
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

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(CreateFuelReceipt.this, amountET);
                if (!validate()) {
                } else {
                    addNewFuelEntry();
                }
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

        infoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCurrencyAndUMDialog();
            }
        });

        imageViewCameraIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(CreateFuelReceipt.this);
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

    }

    boolean validate() {

        boolean valid = true;

        dateValue = dateTV.getText().toString();
        gallonsValue = gallonsET.getText().toString();
        amountValue = amountET.getText().toString().replace("$", "");
        pricePerGallonsValue = ((int) ((Float.parseFloat(amountValue)) / (Integer.parseInt(gallonsValue))));
        salesTaxValue = salesTaxET.getText().toString();
        stopValue = stopSpinner.getSelectedItem().toString();
        stateValue = stateSpinner.getSelectedItem().toString();
        fuelTypeValue = fuelCodeSpinner.getSelectedItem().toString();
        fuelCodeValue = getFuelCodeFromFuelType(fuelTypeValue);
        odometerValue = odometerET.getText().toString();

        if (dateValue.isEmpty()) {
            dateTV.setError("Please enter date");
            valid = false;
        }

        if (gallonsValue.isEmpty()) {
            gallonsET.setError("Please enter gallons");
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
            Toast.makeText(this, "Please enter truck stop", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (fuelTypeValue.isEmpty()) {
            Toast.makeText(this, "Please select fuel type", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (stateValue.isEmpty()) {
            Toast.makeText(this, "Please select state", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    void setStateSpinnerAdapter() {

//        stateSpinner.setAdapter(new ArrayAdapter<>(CreateFuelReceipt.this, android.R.layout.simple_spinner_dropdown_item, rules.getActiveStateList(CreateFuelReceipt.this)));
        stateSpinner.setAdapter(new ArrayAdapter<>(CreateFuelReceipt.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.ifta_states_name)));
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isStateInitialAssigned) {
                    isStateInitialAssigned = true;
//                    ((TextView) adapterView.getChildAt(0)).setText(getSelectedStateIndex());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        stateSpinner.setTitle("Search State");
    }

    void setFuelCodeSpinnerAdapter() {
        fuelCodeSpinner.setAdapter(new ArrayAdapter<>(CreateFuelReceipt.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.fuel_types_update)));
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
    }

    void setTruckStopSpinnerAdapter() {
        stopSpinner.setAdapter(new ArrayAdapter<>(CreateFuelReceipt.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.truck_stops_list)));
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
        dateTV.setText(initialDate);
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
        currencyAndUMDialog = new CurrencyAndUMDialog(CreateFuelReceipt.this,
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
                    loadProfile(CreateFuelReceipt.this, uri.toString(), fuelReceiptImage, false);
                } catch (IOException e) {
                    Log.d(TAG, "onActivityResult: IOException: " + e.getMessage());
                }
            }
        }
    }

    String getSelectedStateIndex() {
        String[] stateArray = getResources().getStringArray(R.array.states_provinces_abbreviation);
        for (int i = 0; i < getResources().getStringArray(R.array.states_provinces_abbreviation).length; i++) {
            if (rules.getCurrentState(CreateFuelReceipt.this).equalsIgnoreCase(stateArray[i])) {
                return stateArray[i];
            }
        }
        return "";
    }

    void addNewFuelEntry() {
        Log.d(TAG, "fuelReceipt: addNewFuelEntry: ");
        Intent intent = new Intent();

        int newlyAddedEntryId = rules.insertFuelReceipt(parseDataModel(), false);
        intent.putExtra("value", newlyAddedEntryId);
        Log.d(TAG, "fuelReceipt: addNewFuelEntry: newlyAddedEntryId: " + newlyAddedEntryId);
        setResult(1, intent);

        finish();
    }

    FuelReceiptModel parseDataModel() {
        Log.d(TAG, "fuelReceipt: parseDataModel: ");
        FuelReceiptModel fuelReceiptModel = new FuelReceiptModel();
        String mobileRecordId = Rms.getMobileRecordId("FuelReceipt");
        fuelReceiptModel.setFuelReceiptFirstName(user.getFirstName());
        fuelReceiptModel.setFuelReceiptLastName(user.getLastName());
        fuelReceiptModel.setFuelReceiptCompany(user.getCompany());
        fuelReceiptModel.setFuelReceiptTruckNumber(user.getTruckNumber());
        fuelReceiptModel.setFuelReceiptDOTNumber("");
        fuelReceiptModel.setDriverLicenseNumber(user.getDriversLicenseNumber());
        fuelReceiptModel.setFuelReceiptUserRecordId(user.getRecordId());
        fuelReceiptModel.setFuelReceiptMobileRecordId(mobileRecordId);

        fuelReceiptModel.setFuelReceiptDateTime(dateValue);// TODO add time with it too Nov 25, 2022
        fuelReceiptModel.setFuelReceiptGallons(gallonsValue);
        fuelReceiptModel.setFuelReceiptAmount(amountValue);
        fuelReceiptModel.setPricePerGallons("" + pricePerGallonsValue);
        fuelReceiptModel.setFuelReceiptSalesTax(salesTaxValue);
        fuelReceiptModel.setFuelReceiptTruckStop(stopValue);
        fuelReceiptModel.setFuelReceiptState(stateValue);
        fuelReceiptModel.setFuelReceiptCountry("US");
        fuelReceiptModel.setFuelReceiptFuelType(fuelTypeValue);
        fuelReceiptModel.setFuelReceiptFuelCode(fuelCodeValue);
        fuelReceiptModel.setFuelReceiptOdometer(odometerValue);
        fuelReceiptModel.setFuelReceiptRMSTimestamp("0");
        Log.d(TAG, "fuelReceipt: fuelReceiptModel: " + fuelReceiptModel);
        return fuelReceiptModel;
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(CreateFuelReceipt.this,
                dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);
        datePickerDialog.show();
    }


//        setOperation(getVal(arstrColVals, ix++));
//        String flag = getVal(arstrColVals, ix++);
//        setObjectId(getVal(arstrColVals, ix++));
//        setObjectType(getVal(arstrColVals, ix++), holderObjectType);
//        setMobileRecordId(getVal(arstrColVals, ix++));
//        setFuncGroupName(getVal(arstrColVals, ix++));
//        setOrgName(getVal(arstrColVals, ix++));
//        setOrgNumber(getVal(arstrColVals, ix++));
//        dateTime = getVal(arstrColVals, ix++);
//        salesTax = getVal(arstrColVals, ix++);
//        refund = getVal(arstrColVals, ix++);
//        firstName = getVal(arstrColVals, ix++);
//        lastName = getVal(arstrColVals, ix++);
//        driverLicenseNumber = getVal(arstrColVals, ix++);
//        company = getVal(arstrColVals, ix++);
//        fuelCode = getVal(arstrColVals, ix++);
//        fuelType = getVal(arstrColVals, ix++);
//        truckNumber = getVal(arstrColVals, ix++);
//        dOTNumber = getVal(arstrColVals, ix++);
//        odometer = getVal(arstrColVals, ix++);
//        vehicleLicenseNumber = getVal(arstrColVals, ix++);
//        vendorName = getVal(arstrColVals, ix++);
//        vendorAddress = getVal(arstrColVals, ix++);
//        vendorState = getVal(arstrColVals, ix++);
//        vendorCountry = getVal(arstrColVals, ix++);
//        purchasersName = getVal(arstrColVals, ix++);
//        priceperGallon = getVal(arstrColVals, ix++);
//        numberofGallonsPurchased = getVal(arstrColVals, ix++);
//        totalAmountofSaleinUSD = getVal(arstrColVals, ix++);
//        userRecordId = getVal(arstrColVals, ix++);
//        amount = getVal(arstrColVals, ix++);


}