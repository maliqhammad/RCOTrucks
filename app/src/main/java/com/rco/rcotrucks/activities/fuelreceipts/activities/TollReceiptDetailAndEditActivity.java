package com.rco.rcotrucks.activities.fuelreceipts.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.Intent;
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
import com.rco.rcotrucks.activities.fuelreceipts.fragments.ReceiptFragment;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.TollReceiptDetail;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.interfaces.CRUDInterface;
import com.rco.rcotrucks.model.RoadTollModel;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.DecimalDigitsInputFilter;
import com.rco.rcotrucks.utils.SessionManagement;
import com.rco.rcotrucks.utils.UiUtils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class TollReceiptDetailAndEditActivity extends BaseActivity {

    private static final String TAG = TollReceiptDetail.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    ImageView clearDateIcon, clearAmountIcon, clearVendorNameIcon, back;
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
    public BusinessRules businessRules = BusinessRules.instance();
    User user;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toll_receipt_detail_and_edit);

        setIds();
        setDecimalDigitsInputFilter();
        setIdsFromActivity();
        getIntentData();
        initialize();
        setInitialDate();
        setListeners();

        setStateSpinnerAdapter();
    }

    void setIds() {

        back = findViewById(R.id.back);
        clearDateIcon = findViewById(R.id.clearDateIcon);
        clearAmountIcon = findViewById(R.id.clearAmountIcon);
        clearVendorNameIcon = findViewById(R.id.clearVendorNameIcon);

        dateTV = findViewById(R.id.date);
        amountET = findViewById(R.id.amount);
        amountET.setFilters(new InputFilter[] {new DecimalDigitsInputFilter(2)});
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

    void setIdsFromActivity() {
        title = findViewById(R.id.title);
        title.setText("Fuel Receipt");
        delete = findViewById(R.id.delete);
        save = findViewById(R.id.save);
    }

    void getIntentData() {
        receiptModel = (ReceiptModel) getIntent().getExtras().getSerializable("dataModel");
        if (receiptModel != null) {
            convertDataModel();
        }
    }

    void initialize() {
        user = BusinessRules.instance().getAuthenticatedUser();

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

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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


        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (receiptModel.getTollReceiptObjectId() == 0) {
                    deleteFromTollReceiptDB(receiptModel.getId());
                } else {
//            id, objectId, objectType
                    deleteRecord(receiptModel.getId(), "" + receiptModel.getTollReceiptObjectId(), receiptModel.getTollReceiptObjectType(), false);
                    deleteFromTollReceiptDB(receiptModel.getId());
                }
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard(TollReceiptDetailAndEditActivity.this, amountET);
                if (!validate()) {
                } else {

                    tollReceiptModel.setTollReceiptDateTime(dateValue);// TODO add time with it too Nov 25, 2022
                    tollReceiptModel.setTollReceiptVendorName(vendorName);
                    tollReceiptModel.setTollReceiptVendorState(stateValue);
                    tollReceiptModel.setTollReceiptAmount(amountValue);
                    tollReceiptModel.setTollReceiptRoadName(tollRoadValue);
                    Log.d(TAG, "save: onClick: whenEdit: userRecordId: " + tollReceiptModel.getTollReceiptUserRecordId());
                    rules.updateTollReceiptEntry(tollReceiptModel);
                    finish();
                }
            }
        });

        amountET.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                Log.d(TAG, "onFocusChange: hasFocus: " + hasFocus);
                if (!hasFocus) {
                        hideSoftKeyboard(TollReceiptDetailAndEditActivity.this, amountET);
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
        ArrayList<String> activeStateList = rules.getActiveStateList(TollReceiptDetailAndEditActivity.this);
        Log.d(TAG, "setStateSpinnerAdapter: activeStateList: size: " + activeStateList.size());
        for (int i = 0; i < activeStateList.size(); i++) {
            Log.d(TAG, "setStateSpinnerAdapter: activeStateList: value: " + activeStateList.get(i));
        }

        stateSpinner.setAdapter(new ArrayAdapter<>(TollReceiptDetailAndEditActivity.this, android.R.layout.simple_spinner_dropdown_item, rules.getActiveStateList(TollReceiptDetailAndEditActivity.this)));
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

                    ArrayList<RoadTollModel> selectedStateRoadsList = rules.getRoadsList(selectedState, TollReceiptDetailAndEditActivity.this);
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
        ArrayList<String> activeStateList = rules.getActiveStateList(TollReceiptDetailAndEditActivity.this);
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
        tollRoadSpinner.setAdapter(new ArrayAdapter<>(TollReceiptDetailAndEditActivity.this, android.R.layout.simple_spinner_dropdown_item, roadsList));
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
        ArrayList<RoadTollModel> selectedStateRoadsList = rules.getRoadsList(tollReceiptModel.getTollReceiptVendorState(), TollReceiptDetailAndEditActivity.this);
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
            Toast.makeText(TollReceiptDetailAndEditActivity.this, "Please enter road toll", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (stateValue.isEmpty()) {
            Toast.makeText(TollReceiptDetailAndEditActivity.this, "Please enter state", Toast.LENGTH_SHORT).show();
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
        DatePickerDialog datePickerDialog = new DatePickerDialog(TollReceiptDetailAndEditActivity.this,
                dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);
        datePickerDialog.show();
    }

    void deleteFromTollReceiptDB(String recordId) {
        int isDeleted = businessRules.deleteTollReceiptItem(recordId);
        Log.d(TAG, "onErrorResponse: isDeleted: " + isDeleted);
        if (isDeleted == 1) {
            finish();
        }
    }

    void deleteRecord(String id, String objectId, String objectType, boolean isFuelReceipt) {
        String usernamePasswordObjectIdObjectTypeCombine = user.getLogin() + "/" + user.getPassword() + "/"
                + objectId + "/" + objectType;
        Log.d(TAG, "deleteRecord: usernamePasswordObjectIdObjectTypeCombine: " + usernamePasswordObjectIdObjectTypeCombine);

        String deleteAPI = Rms.APIToDeleteRecord + usernamePasswordObjectIdObjectTypeCombine;

        RequestQueue requestQueue = Volley.newRequestQueue(TollReceiptDetailAndEditActivity.this);
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
                try {
                    String responseBody = new String(error.networkResponse.data, "utf-8");
                    Log.d(TAG, "onErrorResponse: responseBody: " + responseBody);

//                    Log.d(TAG, "onErrorResponse: id: " + receiptModel.getId());
//                    deleteFromDB(receiptModel.getId());

//                    JSONObject data = new JSONObject(responseBody);
//                    JSONArray errors = data.getJSONArray("errors");
//                    JSONObject jsonMessage = errors.getJSONObject(0);
//                    String message = jsonMessage.getString("message");
//                    Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
//                } catch (JSONException e) {
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



}