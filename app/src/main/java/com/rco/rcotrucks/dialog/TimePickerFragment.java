package com.rco.rcotrucks.dialog;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.icu.util.Calendar;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.DialogFragment;

public class TimePickerFragment extends DialogFragment
        implements TimePickerDialog.OnTimeSetListener {

    SelectionListener selectionListener;

    public TimePickerFragment() {
    }

    public TimePickerFragment(SelectionListener selectionListener) {
        this.selectionListener = selectionListener;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        selectionListener.setTime(view, hourOfDay, minute);
    }

    public interface SelectionListener {
        void setTime(TimePicker view, int hourOfDay, int minute);
    }
}
