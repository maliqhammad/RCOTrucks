package com.rco.rcotrucks.activities.logbook.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.SessionManagement;

public class ELDMonitorServiceDialog extends Dialog {

    private static final String TAG = ELDMonitorServiceDialog.class.getSimpleName();
    ProgressDialog progressDialog;
    SessionManagement sessionManagement;
    public Context context;
    public Dialog dialog;
    TextView saved, missed, cancel;

    public ELDMonitorServiceDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_eld_monitor_service_dialog);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setIds();
        initialize();
        setListener();
    }

    void setIds() {

        saved = findViewById(R.id.saved);
        missed = findViewById(R.id.missed);
        cancel = findViewById(R.id.cancel);
    }

    void initialize() {

        progressDialog = new ProgressDialog(context);
        sessionManagement = new SessionManagement(context);
    }

    void setListener() {

        saved.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        missed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

}

