package com.rco.rcotrucks.activities.forms.employmentrecord;


import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.forms.EvaluationActivity;
import com.rco.rcotrucks.businesslogic.rms.Crms;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_EVALUATION_TYPE;


public class DERActivity extends EvaluationActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        evaluationType = Crms.FORM_Driver_Employment_Record_Form_Signature;

        tvTitle.setText(getString(R.string.forms_title_employment_record));
//        tvTitle.setText(getString(R.string.forms_title_der));

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DERActivity.this, DERFieldsActivity.class);
                intent.putExtra(EXTRA_EVALUATION_TYPE, Crms.FORM_Driver_Employment_Record_Form_Signature);
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