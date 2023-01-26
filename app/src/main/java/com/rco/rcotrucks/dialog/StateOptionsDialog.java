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

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.drive.NightModeReceiver;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.utils.SessionManagement;

public class StateOptionsDialog extends Dialog {

    private static final String TAG = StateOptionsDialog.class.getSimpleName();
    ProgressDialog progressDialog;
    SessionManagement sessionManagement;
    public Context context;
    public Dialog dialog;
    TextView offDuty, sleeper, driving, onDuty, cancel;
    View drivingView;
    StateOptionsInterface stateOptionsInterface;
    private BusinessRules rules = BusinessRules.instance();

    public StateOptionsDialog(@NonNull Context context, StateOptionsInterface stateOptionsInterface) {
        super(context);
        this.context = context;
        this.stateOptionsInterface = stateOptionsInterface;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_layout_state_options);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        Log.d(TAG, "onCreate: ");

        setIds();
        initialize();
        setListener();
    }

    void setIds() {
        Log.d(TAG, "setIds: ");
        offDuty = findViewById(R.id.offDuty);
        sleeper = findViewById(R.id.sleeper);
        driving = findViewById(R.id.driving);
        onDuty = findViewById(R.id.onDuty);
        drivingView = findViewById(R.id.drivingView);
        cancel = findViewById(R.id.cancel);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        progressDialog = new ProgressDialog(context);
        sessionManagement = new SessionManagement(context);
//        if (!rules.isDebugMode()) {
//            driving.setVisibility(View.GONE);
//            drivingView.setVisibility(View.GONE);
//        }
    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        offDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateOptionsInterface.onSelection("Off Duty");
            }
        });

        sleeper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateOptionsInterface.onSelection("Sleeper");
            }
        });
        driving.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateOptionsInterface.onSelection("Driving");
            }
        });
        onDuty.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stateOptionsInterface.onSelection("On Duty");
            }
        });
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }


    public interface StateOptionsInterface {
        void onSelection(String eventStatus);
    }

}


