package com.rco.rcotrucks.activities.forms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.forms.driverapplication.DriverApplicationActivity;
import com.rco.rcotrucks.activities.forms.drivermedicalform.DMActivity;
import com.rco.rcotrucks.activities.forms.driverviolations.DriverViolationsActivity;
import com.rco.rcotrucks.activities.forms.employmentrecord.DERActivity;
import com.rco.rcotrucks.activities.forms.spe.SkillPerformanceActivity;

public class FormsFragment extends Fragment {
    private static final String TAG = FormsFragment.class.getSimpleName();
    ConstraintLayout driverMedicalLayout, skillPerformanceLayout, employmentRecordLayout,
            applicationLayout, violationLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ((MainMenuActivity) getActivity()).setActionBarTitle(getString(R.string.forms_title));

        setIds(view);
        setListeners();
    }

    void setIds(View view) {
        driverMedicalLayout = view.findViewById(R.id.tv_driver_medical_layout);
        skillPerformanceLayout = view.findViewById(R.id.tv_skill_performance_layout);
        employmentRecordLayout = view.findViewById(R.id.tv_employment_rec_layout);
        applicationLayout = view.findViewById(R.id.tv_application_layout);
        violationLayout = view.findViewById(R.id.tv_driver_violation_layout);
    }

    void setListeners() {
        driverMedicalLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity(DMActivity.class);
            }
        });

        skillPerformanceLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity(SkillPerformanceActivity.class);
            }
        });

        employmentRecordLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity(DERActivity.class);
            }
        });

        applicationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity(DriverApplicationActivity.class);
            }
        });

        violationLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openNextActivity(DriverViolationsActivity.class);
            }
        });
    }

    void openNextActivity(Class<?> activityClass) {
        Intent intent = new Intent(requireActivity(), activityClass);
        startActivity(intent);
    }
}