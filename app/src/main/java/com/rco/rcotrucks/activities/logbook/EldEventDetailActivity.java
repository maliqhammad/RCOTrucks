package com.rco.rcotrucks.activities.logbook;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.Tuple;

import java.util.ArrayList;
import java.util.List;

public class EldEventDetailActivity extends AppCompatActivity {
    private BusinessRules businessRules = BusinessRules.instance();
    ImageView back ;
    TextView save ,title;

    RecyclerView eldDetailRecyclerview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eld_detail);

        back = findViewById(R.id.btn_back);
        save = findViewById(R.id.textViewSave);
        title = findViewById(R.id.tv_title);
        save.setVisibility(View.GONE);
        title.setText("Event Detail");
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        eldDetailRecyclerview = findViewById(R.id.eld_detail_recyclerview);

        int eventID = getIntent().getIntExtra(Cadp.EXTRA_EVENT_ID, -1);
        EldEvent eldEvent = businessRules.getEldEvent(eventID);

        List<Tuple<String, String>> eldDetailList = new ArrayList<>();
        if (eldEvent.objectId != null && !eldEvent.objectId.isEmpty())
            eldDetailList.add(new Tuple<>("ObjectID", eldEvent.objectId));

        if (eldEvent.objectType != null && !eldEvent.objectType.isEmpty())
            eldDetailList.add(new Tuple<>("ObjectType", eldEvent.objectType));

        if (eldEvent.MobileRecordId != null && !eldEvent.MobileRecordId.isEmpty())
            eldDetailList.add(new Tuple<>("MobileRecordID", eldEvent.MobileRecordId));

        if (eldEvent.LocalizationDescription != null && !eldEvent.LocalizationDescription.isEmpty())
            eldDetailList.add(new Tuple<>("Localization", eldEvent.LocalizationDescription));

        if (eldEvent.EventCodeDescription != null && !eldEvent.EventCodeDescription.isEmpty())
            eldDetailList.add(new Tuple<>("Event Code", eldEvent.EventCodeDescription));

        if (eldEvent.Annotation != null && !eldEvent.Annotation.isEmpty())
            eldDetailList.add(new Tuple<>("Annotation", eldEvent.Annotation));

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        eldDetailRecyclerview.setLayoutManager(linearLayoutManager);
        EldDetailAdapter eldDetailAdapter = new EldDetailAdapter(eldDetailList);
        eldDetailRecyclerview.setAdapter(eldDetailAdapter);
    }
}