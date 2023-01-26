package com.rco.rcotrucks.activities.forms.driverapplication;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.forms.EvaluationActivity;
import com.rco.rcotrucks.businesslogic.rms.Crms;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_EVALUATION_TYPE;

public class DriverApplicationActivity extends EvaluationActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        evaluationType = Crms.FORM_Driver_Application_Header_Signature;

        tvTitle.setText(getString(R.string.forms_title_driver_application));

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DriverApplicationActivity.this, DAFieldsActivity.class);
                intent.putExtra(EXTRA_EVALUATION_TYPE, Crms.FORM_Driver_Application_Header_Signature);
                startActivity(intent);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupEvaluationView(evaluationType);
    }
}