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
import com.rco.rcotrucks.activities.logbook.adapter.MonitorServiceAdapter;
import com.rco.rcotrucks.activities.logbook.dialog.ELDMonitorServiceDialog;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.SessionManagement;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MonitorService extends BaseActivity {

    private static final String TAG = MonitorService.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    ImageView backIcon, addIcon, clearSearch, refreshIcon, deleteIcon, cancelSearch;
    TextView title, date, checkTV;
    EditText searchET;

    RecyclerView monitorServiceRecyclerView;
    MonitorServiceAdapter monitorServiceAdapter;
    private List<EldEvent> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monitor_service);
        Log.d(TAG, "onCreate: ");

        setIds();
        initialize();
        setListeners();

//        setUpMonitorServiceRecyclerView();
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
        clearSearch = findViewById(R.id.iv_clear_search);
        searchET = findViewById(R.id.et_search);
        monitorServiceRecyclerView = findViewById(R.id.monitorServiceRecyclerView);

        refreshIcon = findViewById(R.id.refreshIcon);
        checkTV = findViewById(R.id.check);
        deleteIcon = findViewById(R.id.deleteIcon);
        cancelSearch = findViewById(R.id.cancelSearch);
    }

    void initialize() {
        sessionManagement = new SessionManagement(MonitorService.this);
        progressDialog = new ProgressDialog(MonitorService.this);

        title.setText("Monitor Service");
        date.setVisibility(View.GONE);
        addIcon.setVisibility(View.GONE);
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

        checkTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ELDMonitorServiceDialog eldMonitorServiceDialog = new ELDMonitorServiceDialog(MonitorService.this);
                eldMonitorServiceDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                eldMonitorServiceDialog.show();
            }
        });

        deleteIcon.setOnClickListener(new View.OnClickListener() {
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
                Log.d(TAG, "onTextChanged: ");
//                String searchedString = searchET.getText().toString().toLowerCase(Locale.getDefault());
//                Log.d(TAG, "onTextChanged: searchedString: " + searchedString);
//                String searchedString= s.toString();
//                receiptAdapter.search(searchedString);
            }

            @Override
            public void afterTextChanged(Editable s) {
                Log.d(TAG, "afterTextChanged: ");
            }
        });

    }

    void setUpMonitorServiceRecyclerView() {
        Log.d(TAG, "setUpRecyclerViewLinearLayoutForChat: ");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MonitorService.this);
        monitorServiceRecyclerView.setLayoutManager(linearLayoutManager);

        monitorServiceAdapter = new MonitorServiceAdapter(list, MonitorService.this);
        monitorServiceRecyclerView.setAdapter(monitorServiceAdapter);
    }

    void getCertificationList() {
        Log.d(TAG, "getDutyEldEventList: ");
//        EventType 8 is A_DRIVERS_CERTIFICATION_RECERTIFICATION_OF_RECORDS
        list.clear();
        String whereClause = "eventType='" + BusinessRules.EventType.IN_SERVICE_MONITOR.getValue()
                + "' AND eldUsername='" + sessionManagement.getKeyLogin() + "' ORDER BY id DESC";
        list = rules.getAllEldEventItems(whereClause);
        Log.d(TAG, "getDutyEldEventList: list: " + list.size());
        setUpMonitorServiceRecyclerView();
    }


}