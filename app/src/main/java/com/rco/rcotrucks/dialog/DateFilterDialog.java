package com.rco.rcotrucks.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.SessionManagement;

public class DateFilterDialog extends Dialog {

    private static final String TAG = DateFilterDialog.class.getSimpleName();
    ProgressDialog progressDialog;
    SessionManagement sessionManagement;
    public Context context;
    public Dialog dialog;
    TextView today, thisWeek, lastWeek, thisMonth, lastMonth, thisYear, thisQuarter, lastQuarter, all, customRange, cancel;
    DateFilterDialog.DateFilterInterface dateFilterInterface;
    ConstraintLayout dialogRootConstraintLayout;

    public DateFilterDialog(@NonNull Context context, DateFilterDialog.DateFilterInterface dateFilterInterface) {
        super(context);
        this.context = context;
        this.dateFilterInterface = dateFilterInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_date_filter);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d(TAG, "onCreate: ");

        setIds();
        initialize();
        setListener();
    }

    void setIds() {
        Log.d(TAG, "setIds: ");
        today = findViewById(R.id.today);
        thisWeek = findViewById(R.id.thisWeek);
        lastWeek = findViewById(R.id.lastWeek);
        thisMonth = findViewById(R.id.thisMonth);
        lastMonth = findViewById(R.id.lastMonth);
        thisYear = findViewById(R.id.thisYear);
        thisQuarter = findViewById(R.id.thisQuarter);
        lastQuarter = findViewById(R.id.lastQuarter);
        all = findViewById(R.id.all);
        customRange = findViewById(R.id.customRange);
        cancel = findViewById(R.id.cancel);
        dialogRootConstraintLayout = findViewById(R.id.dialogRootConstraintLayout);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        progressDialog = new ProgressDialog(context);
        sessionManagement = new SessionManagement(context);
    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("Today");
            }
        });

        thisWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("This Week");
            }
        });

        lastWeek.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("Last Week");
            }
        });

        thisMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("This Month");
            }
        });

        lastMonth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("Last Month");
            }
        });

        thisYear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("This Year");
            }
        });

        thisQuarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("This Quarter");
            }
        });

        lastQuarter.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("Last Quarter");
            }
        });

        all.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("All");
            }
        });

        customRange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.selectedDate("Custom Range");
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dateFilterInterface.onCancelSelection();
            }
        });

        dialogRootConstraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }


    public interface DateFilterInterface {
        void selectedDate(String selectedDate);

        void onCancelSelection();
    }

}


