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

import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.SessionManagement;

public class DialogEldCheck extends Dialog {

    private static final String TAG = DialogEldCheck.class.getSimpleName();
    public Context context;
    public Dialog dialog;
    TextView cancel;

    public DialogEldCheck(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_dialog_eld_check);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d(TAG, "onCreate: ");

        setIds();
        initialize();
        setListener();
    }

    void setIds() {
        Log.d(TAG, "setIds: ");
        cancel = findViewById(R.id.cancel);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
    }

    void setListener() {
        Log.d(TAG, "setListener: ");
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

}

