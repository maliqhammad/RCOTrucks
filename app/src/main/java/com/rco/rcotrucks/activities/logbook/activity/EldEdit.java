package com.rco.rcotrucks.activities.logbook.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.logbook.Certifications;
import com.rco.rcotrucks.activities.logbook.adapter.CertificationAdapter;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.SessionManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class EldEdit extends BaseActivity {

    private static final String TAG = EldEdit.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    ImageView backIcon, clearCMVPowerUnitNumberIcon, clearTrailerNumberIcon, clearShippingInfoIcon, clearCommentsIcon;
    EditText cmvPowerUnitNumber, trailerNumber, shippingInfo, comments;
    String cmvPowerUnitNumberValue = "", trailerNumberValue = "", shippingInfoValue = "", commentsValue = "";
    TextView date, events;
    Button save;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eld_edit);
        Log.d(TAG, "onCreate: ");

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

        cmvPowerUnitNumber = findViewById(R.id.cmvPowerUnitNumber);
        trailerNumber = findViewById(R.id.trailerNumber);
        shippingInfo = findViewById(R.id.shippingInfo);
        comments = findViewById(R.id.comments);

        clearCMVPowerUnitNumberIcon = findViewById(R.id.clearCMVPowerUnitNumberIcon);
        clearTrailerNumberIcon = findViewById(R.id.clearTrailerNumberIcon);
        clearShippingInfoIcon = findViewById(R.id.clearShippingInfoIcon);
        clearCommentsIcon = findViewById(R.id.clearCommentsIcon);

        events = findViewById(R.id.events);
    }

    void initialize() {
        sessionManagement = new SessionManagement(EldEdit.this);
        progressDialog = new ProgressDialog(EldEdit.this);

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

        clearCMVPowerUnitNumberIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cmvPowerUnitNumber.setText("");
            }
        });

        clearTrailerNumberIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                trailerNumber.setText("");
            }
        });

        clearShippingInfoIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                shippingInfo.setText("");
            }
        });

        clearCommentsIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comments.setText("");
            }
        });

        events.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(EldEdit.this, "Events...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    boolean validate() {

        boolean valid = true;

        cmvPowerUnitNumberValue = cmvPowerUnitNumber.getText().toString();
        trailerNumberValue = trailerNumber.getText().toString();
        shippingInfoValue = shippingInfo.getText().toString();
        commentsValue = comments.getText().toString();

        if (cmvPowerUnitNumberValue.isEmpty()) {
            cmvPowerUnitNumber.setError("Please enter CMV Power Unit Number");
            valid = false;
        }

        if (trailerNumberValue.isEmpty()) {
            trailerNumber.setError("Please enter trailer number");
            valid = false;
        }

        if (shippingInfoValue.isEmpty()) {
            shippingInfo.setError("Please enter shipping info");
            valid = false;
        }

        if (commentsValue.isEmpty()) {
            comments.setError("Please enter comments");
            valid = false;
        }

        return valid;
    }


}