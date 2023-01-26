package com.rco.rcotrucks.activities.logbook.activity;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.logbook.adapter.DutyAndEldEventsAdapter;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.SessionManagement;

import java.util.ArrayList;

public class DutyAndEldEvents extends BaseActivity {

    private static final String TAG = DutyAndEldEvents.class.getSimpleName();
    public BusinessRules rules = BusinessRules.instance();
    SessionManagement sessionManagement;
    ProgressDialog progressDialog;
    ImageView backIcon, addIcon, refreshIcon, createIcon;
    TextView title, date, test, duty, all, unidentified;
    ConstraintLayout dutyLayout, allLayout, unidentifiedLayout;

    RecyclerView dutyAndEldEventsRecyclerView;
    DutyAndEldEventsAdapter dutyAndEldEventsAdapter;
    private ArrayList<EldEvent> list;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_duty_and_eld_events);

        setIds();
        initialize();
        setListeners();

        setUpDutyAndEldEventsRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        setAllLayout();
    }

    void setIds() {
        backIcon = findViewById(R.id.back);
        title = findViewById(R.id.title);
        addIcon = findViewById(R.id.add);
        date = findViewById(R.id.date);
        dutyAndEldEventsRecyclerView = findViewById(R.id.dutyAndELDEventsRecyclerView);

        refreshIcon = findViewById(R.id.refreshIcon);
        createIcon = findViewById(R.id.createIcon);
        test = findViewById(R.id.test);
        duty = findViewById(R.id.duty);
        all = findViewById(R.id.all);
        unidentified = findViewById(R.id.unidentified);

        dutyLayout = findViewById(R.id.dutyLayout);
        allLayout = findViewById(R.id.allLayout);
        unidentifiedLayout = findViewById(R.id.unidentifiedLayout);
    }

    void initialize() {
        sessionManagement = new SessionManagement(DutyAndEldEvents.this);
        progressDialog = new ProgressDialog(DutyAndEldEvents.this);

        title.setText("Duty And Eld Events");
        addIcon.setVisibility(View.GONE);
        date.setVisibility(View.VISIBLE);
        list = new ArrayList<>();
    }

    void setListeners() {

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        addIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DutyAndEldEvents.this, AddNewEvent.class);
                startActivity(intent);
            }
        });

        refreshIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        createIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        test.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        dutyLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setDutyLayout();
            }
        });

        allLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAllLayout();
            }
        });

        unidentifiedLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setUnidentifiedLayout();
            }
        });

    }


    void setUpDutyAndEldEventsRecyclerView() {
        Log.d(TAG, "setUpRecyclerViewLinearLayoutForChat: ");
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(DutyAndEldEvents.this);
        dutyAndEldEventsRecyclerView.setLayoutManager(linearLayoutManager);

        Log.d(TAG, "setUpDutyAndEldEventsRecyclerView: list: size: " + list.size());
        dutyAndEldEventsAdapter = new DutyAndEldEventsAdapter(list, DutyAndEldEvents.this);
        dutyAndEldEventsRecyclerView.setAdapter(dutyAndEldEventsAdapter);
    }

    void setDutyLayout() {
        dutyLayout.setBackground(getResources().getDrawable(R.drawable.black_curved_background_two));
        allLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background_for_bottom_bar));
        unidentifiedLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background_for_bottom_bar));

        duty.setTextColor(getResources().getColor(R.color.white));
        all.setTextColor(getResources().getColor(R.color.black));
        unidentified.setTextColor(getResources().getColor(R.color.black));

        addIcon.setVisibility(View.VISIBLE);
        date.setVisibility(View.GONE);

        getDutyEldEventList();
    }

    void setAllLayout() {
        Log.d(TAG, "setAllLayout: ");
        dutyLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background_for_bottom_bar));
        allLayout.setBackground(getResources().getDrawable(R.drawable.black_curved_background_two));
        unidentifiedLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background_for_bottom_bar));

        duty.setTextColor(getResources().getColor(R.color.black));
        all.setTextColor(getResources().getColor(R.color.white));
        unidentified.setTextColor(getResources().getColor(R.color.black));

        addIcon.setVisibility(View.GONE);
        date.setVisibility(View.GONE);

        getAllEldEventList();
    }


    void setUnidentifiedLayout() {
        dutyLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background_for_bottom_bar));
        allLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background_for_bottom_bar));
        unidentifiedLayout.setBackground(getResources().getDrawable(R.drawable.black_curved_background_two));

        duty.setTextColor(getResources().getColor(R.color.black));
        all.setTextColor(getResources().getColor(R.color.black));
        unidentified.setTextColor(getResources().getColor(R.color.white));

        addIcon.setVisibility(View.GONE);
        date.setVisibility(View.GONE);

        getUnIdentifiedEldEventList();
    }

    void getDutyEldEventList() {
        Log.d(TAG, "getDutyEldEventList: ");
        list.clear();
        dutyAndEldEventsAdapter.notifyDataSetChanged();

        String eventTypes = "eventType='1'";
        String whereClause = "eventType='1' AND eldUsername='" + sessionManagement.getKeyLogin() + "' ORDER BY id DESC";

//        list = rules.getAllEldEventItems(sessionManagement.getKeyLogin(), eventTypes);
        list = rules.getAllEldEventItems(whereClause);
        Log.d(TAG, "getDutyEldEventList: list: " + list.size());
        setUpDutyAndEldEventsRecyclerView();
//        dutyAndEldEventsAdapter.notifyDataSetChanged();
    }

    void getAllEldEventList() {
        Log.d(TAG, "getAllEldEventList: ");
        list.clear();
        String whereClause = "eventType='1' " +
                "OR eventType='2' " +
                "OR eventType='3' " +
                "OR eventType='4' " +
                "OR eventType='5' " +
                "OR eventType='6' " +
                "OR eventType='7' AND eldUsername='" + sessionManagement.getKeyLogin() + "' ORDER BY id DESC";
        list = rules.getAllEldEventItems(whereClause);
        setUpDutyAndEldEventsRecyclerView();
    }

    void getUnIdentifiedEldEventList() {
        Log.d(TAG, "getUnIdentifiedEldEventList: ");
        list.clear();
        String whereClause = "eventType='1' " +
                "OR eventType='2' " +
                "OR eventType='3' " +
                "OR eventType='4' " +
                "OR eventType='5' " +
                "OR eventType='6' " +
                "OR eventType='7' AND eldUsername=' ' AND eventCode='"
                + BusinessRules.EventCode.DRIVING.getValue() + "' ORDER BY id DESC";

        list = rules.getAllEldEventItems(whereClause);
        Log.d(TAG, "getUnIdentifiedEldEventList: list: " + list.size());
        setUpDutyAndEldEventsRecyclerView();
    }


}