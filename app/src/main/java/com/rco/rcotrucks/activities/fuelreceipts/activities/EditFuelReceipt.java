package com.rco.rcotrucks.activities.fuelreceipts.activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.ProgressDialog;
import android.graphics.Color;
import android.os.Bundle;
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
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.SessionManagement;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.Date;
import java.util.TimeZone;

public class EditFuelReceipt extends BaseActivity {

    private static final String TAG = EditFuelReceipt.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    ImageView backIcon, clearAmountIcon, clearGallonsIcon, clearFuelTypeIcon, clearTruckNumberIcon;
    EditText amountET, gallonsET, salesTaxET, vendorNameET, dateET;
    String amountValue = "", gallonsValue = "", salesTaxValue = "", fuelCodeValue = "", truckNumberValue = "",
            stateValue = "", odometerDateValue = "";
    Button save;
    SearchableSpinner fuelCodeSpinner, stateSpinner;

    int yearSelected, monthSelected, daySelected;
    java.util.Calendar calendar = java.util.Calendar.getInstance(TimeZone.getDefault());
    Date date;
    int day, month, year, dayOfWeek, dayOfMonth, dayOfYear, totalIncomeValue = 0;
    CalendarView calendarView;
    ConstraintLayout calendarLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_fuel_receipt);

        setIds();
        initialize();
        initializeCalendar();
        setListeners();

        setFuelCodeSpinnerAdapter();
        setVehicleLicenseNumberSpinnerAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

    }

    void setIds() {
        backIcon = findViewById(R.id.btn_back);
        save = findViewById(R.id.save);

        clearAmountIcon = findViewById(R.id.clearAmountIcon);
        clearGallonsIcon = findViewById(R.id.clearGallonsIcon);
        clearFuelTypeIcon = findViewById(R.id.clearFuelTypeIcon);
        clearTruckNumberIcon = findViewById(R.id.clearTruckNumberIcon);

        amountET = findViewById(R.id.amount);
        gallonsET = findViewById(R.id.gallons);
        salesTaxET = findViewById(R.id.sales_tax);
        vendorNameET = findViewById(R.id.vendorName);

        fuelCodeSpinner = findViewById(R.id.fuelCodeSpinner);
        stateSpinner = findViewById(R.id.stateSpinner);

        dateET = findViewById(R.id.date);

        calendarLayout = findViewById(R.id.calendarLayout);
        calendarView = findViewById(R.id.calendarView);

    }

    void initialize() {
        sessionManagement = new SessionManagement(EditFuelReceipt.this);
        progressDialog = new ProgressDialog(EditFuelReceipt.this);
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
                if (validate()) {
                } else {

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

    }

    boolean validate() {

        boolean valid = true;

        amountValue = amountET.getText().toString();
        gallonsValue = gallonsET.getText().toString();
        salesTaxValue = salesTaxET.getText().toString();
        fuelCodeValue = fuelCodeSpinner.getSelectedItem().toString();
        truckNumberValue = vendorNameET.getText().toString();
        stateValue = stateSpinner.getSelectedItem().toString();
        odometerDateValue = dateET.getText().toString();


        if (amountValue.isEmpty()) {
            amountET.setError("Please enter amount");
            valid = false;
        }

        if (gallonsValue.isEmpty()) {
            gallonsET.setError("Please enter quantity");
            valid = false;
        }

        if (salesTaxValue.isEmpty()) {
            salesTaxET.setError("Please enter sales tax");
            valid = false;
        }
        if (fuelCodeValue.isEmpty()) {
            Toast.makeText(this, "Please enter sales tax", Toast.LENGTH_SHORT).show();
            valid = false;
        }

        if (stateValue.isEmpty()) {
            Toast.makeText(this, "Please enter state", Toast.LENGTH_SHORT).show();
            valid = false;
        }


        if (truckNumberValue.isEmpty()) {
            vendorNameET.setError("Please enter truck number");
            valid = false;
        }

        if (odometerDateValue.isEmpty()) {
            dateET.setError("Please enter odometer");
            valid = false;
        }

        return valid;
    }

    void setFuelCodeSpinnerAdapter() {

        fuelCodeSpinner.setAdapter(new ArrayAdapter<>(EditFuelReceipt.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.fuel_types_update)));
        fuelCodeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                hideSoftKeyboard(getContext(), billerName);
//                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
//                if (i == 0) {
//                    Toast.makeText(AddExpenseWizard.this, "Please select a category.", Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                hideSoftKeyboard(getContext(), billerName);
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        fuelCodeSpinner.setTitle("Search Fuel Code");
    }

    void setVehicleLicenseNumberSpinnerAdapter() {

        stateSpinner.setAdapter(new ArrayAdapter<>(EditFuelReceipt.this, android.R.layout.simple_spinner_dropdown_item, getResources().getStringArray(R.array.states_provinces)));
        stateSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                hideSoftKeyboard(getContext(), billerName);
//                ((TextView) adapterView.getChildAt(0)).setTextColor(Color.BLACK);
//                if (i == 0) {
//                    Toast.makeText(AddExpenseWizard.this, "Please select a category.", Toast.LENGTH_SHORT).show();
//                }
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

}