package com.rco.rcotrucks.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.SessionManagement;

public class SynStatusDialog extends Dialog {

    private static final String TAG = SynStatusDialog.class.getSimpleName();
    ProgressDialog progressDialog;
    SessionManagement sessionManagement;
    public Context context;
    public Dialog dialog;
    TextView yes, no;
    SynStatusDialog.PickupInterface pickupInterface;


    public SynStatusDialog(@NonNull Context context, SynStatusDialog.PickupInterface pickupInterface) {
        super(context);
        this.context = context;
        this.pickupInterface = pickupInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_layout_syn_status);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d(TAG, "onCreate: ");

        setIds();
        initialize();
        setListener();
    }

    void setIds() {
        Log.d(TAG, "setIds: ");
        yes = findViewById(R.id.authenticate);
        no = findViewById(R.id.cancel);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        progressDialog = new ProgressDialog(context);
        sessionManagement = new SessionManagement(context);
    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        yes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickupInterface.onYesPressed();
                dismiss();
            }
        });

        no.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickupInterface.onCancelPressed();
                dismiss();
            }
        });

    }

    public interface PickupInterface {
        void onYesPressed();

        void onCancelPressed();
    }

}

