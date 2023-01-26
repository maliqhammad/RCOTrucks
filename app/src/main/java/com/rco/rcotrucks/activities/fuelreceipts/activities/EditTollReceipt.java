package com.rco.rcotrucks.activities.fuelreceipts.activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.FuelReceiptListFragment;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperTollReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.utils.UiHelperFuelReceiptDtl;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.dialog.CurrencyAndUMDialog;
import com.rco.rcotrucks.model.RoadTollModel;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.SessionManagement;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class EditTollReceipt extends BaseActivity {

    private static final String TAG = EditTollReceipt.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    TextView title, amountTitle, gallonsTitle;
    ImageView backIcon, clearDateIcon, clearAmountIcon, clearVendorNameIcon, fuelReceiptImage, imageViewCameraIcon;
    EditText dateET, amountET, vendorNameET;
    String dateValue = "", amountValue = "", vendorName = "", tollRoadValue = "", stateValue = "";
    TextView save;
    SearchableSpinner tollRoadSpinner, stateSpinner;

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
    private BusHelperTollReceipts busRules = BusHelperTollReceipts.instance();
    boolean isStateInitialAssigned = false, isRoadInitialAssigned = false;
    List<ListItemCodingDataGroup> listItems;
    ReceiptModel receiptModel;
    TollReceiptModel tollReceiptModel;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_toll_receipt);

        setIds();
        getIntentData();
        initialize();
        initializeCalendar();
        setListeners();

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

        dateET = findViewById(R.id.date);
        amountET = findViewById(R.id.amount);
        vendorNameET = findViewById(R.id.vendorName);

        tollRoadSpinner = findViewById(R.id.tollRoadSpinner);
        stateSpinner = findViewById(R.id.stateSpinner);

        calendarLayout = findViewById(R.id.calendarLayout);
        calendarView = findViewById(R.id.calendarView);
    }

    void getIntentData() {
        if (getIntent().getExtras().getSerializable("receiptModel") != null) {
            receiptModel = (ReceiptModel) getIntent().getExtras().getSerializable("receiptModel");
            Log.d(TAG, "save: getIntentData: road: name: " + receiptModel);

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
            Log.d(TAG, "save: getIntentData: mobileRecordId: " + receiptModel.getTollReceiptMobileRecordId()
                    + " and: " + tollReceiptModel.getTollReceiptMobileRecordId());
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

            stateValue = receiptModel.getVendorState();
            tollRoadValue = receiptModel.getTollReceiptRoadName();
            dateET.setText(receiptModel.getTollReceiptDateTime());
            amountET.setText(receiptModel.getTollReceiptAmount());
            vendorNameET.setText(receiptModel.getVendorName());
        }
    }

    void initialize() {
        sessionManagement = new SessionManagement(EditTollReceipt.this);
        progressDialog = new ProgressDialog(EditTollReceipt.this);
        title.setText("Edit Toll Receipt");
        user = BusinessRules.instance().getAuthenticatedUser();
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

                if (!validate()) {
                } else {

                    tollReceiptModel.setTollReceiptDateTime(dateValue);// TODO add time with it too Nov 25, 2022
                    tollReceiptModel.setTollReceiptVendorName(vendorName);
                    tollReceiptModel.setTollReceiptVendorState(stateValue);
                    tollReceiptModel.setTollReceiptAmount(amountValue);
                    tollReceiptModel.setTollReceiptRoadName(tollRoadValue);

                    Log.d(TAG, "save: onClick: whenEdit: userRecordId: " + tollReceiptModel.getTollReceiptUserRecordId());
//                    if (tollReceiptModel.getTollReceiptMobileRecordId() != null
//                            && tollReceiptModel.getTollReceiptMobileRecordId().isEmpty()) {
                    rules.updateTollReceiptEntry(tollReceiptModel);
                    finish();
//                    }

                }
            }
        });


        clearDateIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateET.setText("");
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

        dateET.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarLayout.setVisibility(View.VISIBLE);
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

                dateET.setText(selectedDate);

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
                pickImage(EditTollReceipt.this);
            }
        });

    }

    boolean validate() {

        boolean valid = true;

        dateValue = dateET.getText().toString();
        amountValue = amountET.getText().toString();
        vendorName = vendorNameET.getText().toString();
        tollRoadValue = tollRoadSpinner.getSelectedItem().toString();
        stateValue = stateSpinner.getSelectedItem().toString();

        Log.d(TAG, "save: validate: date: " + dateValue);
        Log.d(TAG, "save: validate: amount: " + amountValue);
        Log.d(TAG, "save: validate: vendor: " + vendorName);
        Log.d(TAG, "save: validate: road: " + tollRoadValue);
        Log.d(TAG, "save: validate: state: " + stateValue);
        Log.d(TAG, "save: validate: identRmsRecords: " + identRmsRecords);

        if (dateValue.isEmpty()) {
            dateET.setError("Please enter date");
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
        ArrayList<String> activeStateList = rules.getActiveStateList(EditTollReceipt.this);
        Log.d(TAG, "setStateSpinnerAdapter: activeStateList: size: " + activeStateList.size());
        for (int i = 0; i < activeStateList.size(); i++) {
            Log.d(TAG, "setStateSpinnerAdapter: activeStateList: value: " + activeStateList.get(i));
        }

        stateSpinner.setAdapter(new ArrayAdapter<>(EditTollReceipt.this, android.R.layout.simple_spinner_dropdown_item, rules.getActiveStateList(EditTollReceipt.this)));
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isStateInitialAssigned) {
                    isStateInitialAssigned = true;
                }
//                String selectedState = ((TextView) adapterView.getChildAt(0)).getText().toString();
                String selectedState = stateSpinner.getSelectedItem().toString();


                ArrayList<RoadTollModel> selectedStateRoadsList = rules.getRoadsList(selectedState, EditTollReceipt.this);
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
        stateSpinner.setSelection(getSelectedStatePosition());

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

        dateET.setText(selectedDate);
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
        currencyAndUMDialog = new CurrencyAndUMDialog(EditTollReceipt.this,
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
                    loadProfile(EditTollReceipt.this, uri.toString(), fuelReceiptImage, false);
                } catch (IOException e) {
                    Log.d(TAG, "onActivityResult: IOException: " + e.getMessage());
                }
            }
        }
    }

    int getSelectedStatePosition() {
        ArrayList<String> activeStateList = rules.getActiveStateList(EditTollReceipt.this);
        for (int i = 0; i < activeStateList.size(); i++) {
            if (tollReceiptModel.getTollReceiptVendorState().equalsIgnoreCase(activeStateList.get(i))) {
                return i;
            }
        }
        return 0;
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

        tollRoadSpinner.setAdapter(new ArrayAdapter<>(EditTollReceipt.this, android.R.layout.simple_spinner_dropdown_item, roadsList));
        tollRoadSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (!isRoadInitialAssigned) {
                    isRoadInitialAssigned = true;
                } else {
//                    ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
                    vendorNameET.setText(selectedStateRoadsList.get(i).getVendorName());
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                hideSoftKeyboard(getContext(), billerName);
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        tollRoadSpinner.setTitle("Search Toll Roads");
        tollRoadSpinner.setSelection(getSelectedRoadPosition());
    }

    int getSelectedRoadPosition() {
        ArrayList<RoadTollModel> selectedStateRoadsList = rules.getRoadsList(tollReceiptModel.getTollReceiptVendorState(), EditTollReceipt.this);
        for (int i = 0; i < selectedStateRoadsList.size(); i++) {
            if (tollReceiptModel.getTollReceiptRoadName().equalsIgnoreCase(selectedStateRoadsList.get(i).getRoadName())) {
                vendorNameET.setText(tollReceiptModel.getTollReceiptVendorName());
                return i;
            }
        }
        return 0;
    }


}