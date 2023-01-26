package com.rco.rcotrucks.activities.logbook.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.dialog.StateOptionsDialog;
import com.rco.rcotrucks.dialog.TimePickerFragment;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.SessionManagement;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

public class AddNewEvent extends AppCompatActivity {

    private static final String TAG = AddNewEvent.class.getSimpleName();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    ImageView backIcon, clearLocationIcon, clearStartTimeIcon, clearEndTimeIcon, clearStateOptionsIcon, clearCommentsIcon;
    EditText location, comment, startTime, endTime, stateOptions;
    String locationValue = "", startTimeValue = "", endTimeValue = "", stateOptionValue = "", commentsValue = "";
    TextView date, events;
    Button save;
    StateOptionsDialog stateOptionsDialog;
    boolean isStartTimeSelected = false;
    com.wdullaer.materialdatetimepicker.time.TimePickerDialog startTimePicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_event);

        setIds();
        initialize();
        setListeners();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

    }

    void setIds() {
        backIcon = findViewById(R.id.back);
        date = findViewById(R.id.title);
        save = findViewById(R.id.save);

        location = findViewById(R.id.location);
        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        stateOptions = findViewById(R.id.stateOptions);
        comment = findViewById(R.id.comments);

        clearLocationIcon = findViewById(R.id.clearLocationIcon);
        clearStartTimeIcon = findViewById(R.id.clearStartTimeIcon);
        clearEndTimeIcon = findViewById(R.id.clearEndTimeIcon);
        clearStateOptionsIcon = findViewById(R.id.clearStateOptionsIcon);
        clearCommentsIcon = findViewById(R.id.clearCommentsIcon);

        events = findViewById(R.id.events);
    }

    void initialize() {
        sessionManagement = new SessionManagement(AddNewEvent.this);
        progressDialog = new ProgressDialog(AddNewEvent.this);

        date.setText("05/04/2022");
        save.setVisibility(View.VISIBLE);

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

        clearLocationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                location.setText("");
            }
        });

        clearStartTimeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startTime.setText("");
            }
        });

        clearEndTimeIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                endTime.setText("");
            }
        });

        startTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartTimeSelected = true;
                showTimePickerDialog();
            }
        });

        endTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isStartTimeSelected = false;
                showTimePickerDialog();
            }
        });

        stateOptions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openStateOptionsDialog();
            }
        });

        clearCommentsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comment.setText("");
            }
        });

    }

    boolean validate() {

        boolean valid = true;

        locationValue = location.getText().toString();
        startTimeValue = startTime.getText().toString();
        endTimeValue = endTime.getText().toString();
        stateOptionValue = stateOptions.getText().toString();
        commentsValue = comment.getText().toString();

        if (locationValue.isEmpty()) {
            location.setError("Please enter location");
            valid = false;
        }

        if (startTimeValue.isEmpty()) {
            startTime.setError("Please enter start time");
            valid = false;
        } else {
            startTime.setError(null);
        }

        if (endTimeValue.isEmpty()) {
            endTime.setError("Please enter end time");
            valid = false;
        } else {
            endTime.setError(null);
        }

        if (stateOptionValue.isEmpty()) {
            stateOptions.setError("Please enter state option");
            valid = false;
        } else {
            stateOptions.setError(null);
        }

        if (commentsValue.isEmpty()) {
            comment.setError("Please enter comments");
            valid = false;
        }

        return valid;
    }


    public void openStateOptionsDialog() {
        stateOptionsDialog = new StateOptionsDialog(AddNewEvent.this,
                new StateOptionsDialog.StateOptionsInterface() {
                    @Override
                    public void onSelection(String eventStatus) {
                        stateOptions.setText(eventStatus);
                        stateOptionsDialog.dismiss();
                    }
                });
        stateOptionsDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        stateOptionsDialog.show();
    }

    public void openTimeSelectionDialog() {

        // Create a new instance of DatePickerDialog and return it

        startTimePicker = com.wdullaer.materialdatetimepicker.time.TimePickerDialog
                .newInstance(new com.wdullaer.materialdatetimepicker.time.TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(com.wdullaer.materialdatetimepicker.time.TimePickerDialog view, int hourOfDay, int minute, int second) {

                        Log.d(TAG, "onTimeSet: hourOfDay: " + hourOfDay);
                        Log.d(TAG, "onTimeSet: minute: " + minute);
                        Log.d(TAG, "onTimeSet: second: " + second);

                        if (hourOfDay > 12) {

                            int startHour;
                            startHour = hourOfDay % 12;

//                            if (minute > 9) {
//
//                                eventStartTimeET.setText(startHour + ":" + minute + " PM");
//                            } else {
//
//                                eventStartTimeET.setText(startHour + ":0" + minute + " PM");
//                            }


                        } else {

//                            if (minute > 9) {
//
//                                eventStartTimeET.setText(hourOfDay + ":" + minute + " AM");
//                            } else {
//
//                                eventStartTimeET.setText(hourOfDay + ":0" + minute + " AM");
//                            }

                        }


                    }
                }, false);

        startTimePicker.show(new FragmentActivity().getSupportFragmentManager(), "Datepickerdialog");
    }


    public void showTimePickerDialog() {
        DialogFragment newFragment = new TimePickerFragment(new TimePickerFragment.SelectionListener() {
            @Override
            public void setTime(TimePicker view, int hourOfDay, int minute) {
                Log.d(TAG, "setTime: ");
                if (isStartTimeSelected) {
                    startTime.setText(hourOfDay + ":" + minute);
                } else {
                    endTime.setText(hourOfDay + ":" + minute);
                }
            }
        });
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }
}