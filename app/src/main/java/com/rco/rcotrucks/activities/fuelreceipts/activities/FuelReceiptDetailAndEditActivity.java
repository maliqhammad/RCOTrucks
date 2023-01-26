package com.rco.rcotrucks.activities.fuelreceipts.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.InputFilter;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CalendarView;
import android.widget.DatePicker;
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
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.FuelReceiptDetail;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.ReceiptFragment;
import com.rco.rcotrucks.activities.fuelreceipts.model.FuelReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.dialog.CurrencyAndUMDialog;
import com.rco.rcotrucks.interfaces.CRUDInterface;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.DecimalDigitsInputFilter;
import com.rco.rcotrucks.utils.SessionManagement;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class FuelReceiptDetailAndEditActivity extends BaseActivity {

    private static final String TAG = FuelReceiptDetail.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    TextView amountTitle, gallonsTitle, title;
    ImageView backIcon, clearAmountIcon, clearGallonsIcon, clearFuelTypeIcon, clearTruckNumberIcon,
            fuelReceiptImage, imageViewCameraIcon, odometerIcon, back;
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

    int selectedYear, selectedMonth, selectedDayOfMonth;
    private Calendar initialCalendar;
    private SimpleDateFormat initialDateFormat;
    public BusinessRules businessRules = BusinessRules.instance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fuel_receipt_detail_and_edit);

        setIds();
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
    }

    void setIds() {
        back = findViewById(R.id.back);
        fuelReceiptImage = findViewById(R.id.fuelReceiptImage);

        amountTitle = findViewById(R.id.amountTitle);
        gallonsTitle = findViewById(R.id.gallonsTitle);

        imageViewCameraIcon = findViewById(R.id.imageViewCameraIcon);
        clearAmountIcon = findViewById(R.id.clearAmountIcon);
        clearGallonsIcon = findViewById(R.id.clearGallonsIcon);
        clearFuelTypeIcon = findViewById(R.id.clearFuelTypeIcon);
        clearTruckNumberIcon = findViewById(R.id.clearTruckNumberIcon);

        amountET = findViewById(R.id.amount);
        gallonsET = findViewById(R.id.gallons);
        salesTaxET = findViewById(R.id.sales_tax);
        vendorNameET = findViewById(R.id.vendorName);
        odometerET = findViewById(R.id.odometer);
        odometerIcon = findViewById(R.id.odometerIcon);
        odometerTitle = findViewById(R.id.odometerTitle);

        fuelCodeSpinner = findViewById(R.id.fuelCodeSpinner);
        stateSpinner = findViewById(R.id.stateSpinner);
        stopSpinner = findViewById(R.id.truckStopSpinner);

        dateTV = findViewById(R.id.date);

        calendarLayout = findViewById(R.id.calendarLayout);
        calendarView = findViewById(R.id.calendarView);

//        infoIcon = findViewById(R.id.infoIcon);

    }

    //    Dec 08, 2022  -   We applied restriction of value to only 2 decimal points at max it on amount and sales tax
    void setDecimalDigitsInputFilter() {
        amountET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
        salesTaxET.setFilters(new InputFilter[]{new DecimalDigitsInputFilter(2)});
    }

    void setIdsFromActivity() {

        title = findViewById(R.id.title);
        title.setText("Fuel Receipt");
        delete = findViewById(R.id.delete);
        save = findViewById(R.id.save);
    }


    void getIntentData() {
        intentReceiptModel = (ReceiptModel) getIntent().getExtras().getSerializable("dataModel");
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
        user = BusinessRules.instance().getAuthenticatedUser();

        Calendar calendar = Calendar.getInstance();
        selectedYear = calendar.get(Calendar.YEAR);
        selectedMonth = calendar.get(Calendar.MONTH);
        selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

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

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(FuelReceiptDetailAndEditActivity.this, amountET);
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
                    finish();
                }
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                crudInterface.onDeleteCalled(intentFuelReceiptModel);
                if (intentFuelReceiptModel.getFuelReceiptObjectId().isEmpty() || intentFuelReceiptModel.getFuelReceiptObjectId() == null
                        || intentFuelReceiptModel.getFuelReceiptObjectId().equalsIgnoreCase("0")) {
                    deleteFromFuelReceiptDB(intentFuelReceiptModel.getId());
                } else {
                    deleteRecord(intentFuelReceiptModel.getId(), intentFuelReceiptModel.getFuelReceiptObjectId(), intentFuelReceiptModel.getFuelReceiptObjectType(), true);
                    deleteFromFuelReceiptDB(intentFuelReceiptModel.getId());
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
            Toast.makeText(FuelReceiptDetailAndEditActivity.this, "Please enter truck stop", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (fuelTypeValue.isEmpty()) {
            Toast.makeText(FuelReceiptDetailAndEditActivity.this, "Please select fuel type", Toast.LENGTH_SHORT).show();
            valid = false;
        } else if (stateValue.isEmpty()) {
            Toast.makeText(FuelReceiptDetailAndEditActivity.this, "Please select state", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        return valid;
    }

    void setStateSpinnerAdapter() {

//        stateSpinner.setAdapter(new ArrayAdapter<>(CreateFuelReceipt.this, android.R.layout.simple_spinner_dropdown_item, rules.getActiveStateList(CreateFuelReceipt.this)));
        stateSpinner.setAdapter(new ArrayAdapter<>(FuelReceiptDetailAndEditActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.ifta_states_name)));
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
        fuelCodeSpinner.setAdapter(new ArrayAdapter<>(FuelReceiptDetailAndEditActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.fuel_types_update)));
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
        stopSpinner.setAdapter(new ArrayAdapter<>(FuelReceiptDetailAndEditActivity.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.truck_stops_list)));
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
        currencyAndUMDialog = new CurrencyAndUMDialog(FuelReceiptDetailAndEditActivity.this,
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(FuelReceiptDetailAndEditActivity.this,
                dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);
        datePickerDialog.show();
    }

    void deleteRecord(String id, String objectId, String objectType, boolean isFuelReceipt) {
        String usernamePasswordObjectIdObjectTypeCombine = user.getLogin() + "/" + user.getPassword() + "/"
                + objectId + "/" + objectType;
        Log.d(TAG, "deleteRecord: usernamePasswordObjectIdObjectTypeCombine: " + usernamePasswordObjectIdObjectTypeCombine);

        String deleteAPI = Rms.APIToDeleteRecord + usernamePasswordObjectIdObjectTypeCombine;

        RequestQueue requestQueue = Volley.newRequestQueue(FuelReceiptDetailAndEditActivity.this);
        StringRequest stringRequest = new StringRequest(Request.Method.GET, deleteAPI, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: response: " + response);
                if (response.contains("successfully delete")) {
//                    Delete it from local db now too
                    Log.d(TAG, "onResponse: id: " + id);
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
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    Log.d(TAG, "onErrorResponse: responseBody: " + responseBody);
                } catch (UnsupportedEncodingException unsupportedEncodingException) {
                    Log.d(TAG, "onErrorResponse: unsupportedEncodingException: " +
                            unsupportedEncodingException.getMessage());
                }

            }
        });
        stringRequest.setRetryPolicy(new DefaultRetryPolicy((int) TimeUnit.SECONDS.toMillis(120), //After the set time elapses the request will timeout
                0, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        requestQueue.add(stringRequest);
    }

    void deleteFromFuelReceiptDB(String recordId) {
        int isDeleted = businessRules.deleteFuelReceiptItem(recordId);
        Log.d(TAG, "onErrorResponse: isDeleted: " + isDeleted);
        if (isDeleted == 1) {
            finish();
        }
    }


}