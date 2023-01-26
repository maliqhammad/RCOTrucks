package com.rco.rcotrucks.activities.logbook;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.logbook.adapter.CertificationAdapter;
import com.rco.rcotrucks.activities.logbook.model.GenericModel;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.SessionManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class Certifications extends BaseActivity {

    private static final String TAG = Certifications.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    ImageView backIcon, addIcon, clearSearch, refreshIcon, cancelSearch;
    TextView title, date;
    EditText searchET;

    RecyclerView certificationsRecyclerView;
    CertificationAdapter certificationsAdapter;
    private List<EldEvent> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_certifications);

        setIds();
        initialize();
        setListeners();

        setUpCertificationRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        getCertificationList();
    }

    void setIds() {
        backIcon = findViewById(R.id.back);
        title = findViewById(R.id.title);
        addIcon = findViewById(R.id.add);
        date = findViewById(R.id.date);
        cancelSearch = findViewById(R.id.cancelSearch);
        clearSearch = findViewById(R.id.iv_clear_search);
        searchET = findViewById(R.id.et_search);
        certificationsRecyclerView = findViewById(R.id.certificationsRecyclerView);
        refreshIcon = findViewById(R.id.refreshIcon);
    }

    void initialize() {
        sessionManagement = new SessionManagement(Certifications.this);
        progressDialog = new ProgressDialog(Certifications.this);

        title.setText("Certifications");
        addIcon.setVisibility(View.GONE);
        date.setVisibility(View.GONE);
        cancelSearch.setVisibility(View.GONE);
        list = new ArrayList<>();
    }

    void setListeners() {

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchET.setText("");
            }
        });


        refreshIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });


        searchET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                Log.d(TAG, "beforeTextChanged: ");
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                Log.d(TAG, "onTextChanged: ");
//                String searchedString = searchET.getText().toString().toLowerCase(Locale.getDefault());
//                Log.d(TAG, "onTextChanged: searchedString: "+searchedString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: ");
            }
        });


    }

    void setUpCertificationRecyclerView() {
        Log.d(TAG, "setUpRecyclerViewLinearLayoutForChat: ");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(Certifications.this);
        certificationsRecyclerView.setLayoutManager(linearLayoutManager);

        certificationsAdapter = new CertificationAdapter(list, Certifications.this);
        certificationsRecyclerView.setAdapter(certificationsAdapter);
    }

    void getCertificationList() {
        Log.d(TAG, "getDutyEldEventList: ");
//        EventType 4 is A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS
        list.clear();
        String whereClause = "eventType='" + BusinessRules.EventType.A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS.getValue()
                + "'  AND eldUsername='" + sessionManagement.getKeyLogin() + "' ORDER BY id DESC";
        list = rules.getAllEldEventItems(whereClause);
        Log.d(TAG, "getDutyEldEventList: list: " + list.size());
        setUpCertificationRecyclerView();
    }



}