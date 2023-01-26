package com.rco.rcotrucks.activities.forms;

import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.forms.spe.SPEFieldsActivity;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Evaluation;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_EVALUATION_TYPE;

abstract public class EvaluationActivity extends AppCompatActivity {
    public BusinessRules businessRules = BusinessRules.instance();

    public RecyclerView spRecyclerView;
    private ImageView btnBack, clearSearch, btnShare;
    public ImageView btnPlus;
    public EditText etSearch;
    public TextView tvTitle, save;
    public RadioGroup filterRadiogroup;

    public EvaluationAdapter spAdapter;
    protected List<Evaluation> evaluationForms;
    protected String evaluationType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_skill_performance);
        spRecyclerView = findViewById(R.id.rv_sp);
        btnBack = findViewById(R.id.btn_back);
        btnPlus = findViewById(R.id.btn_plus);
        etSearch = findViewById(R.id.et_search);
        clearSearch = findViewById(R.id.iv_clear_search);
        filterRadiogroup = findViewById(R.id.filter_radiogroup);
        tvTitle = findViewById(R.id.tv_title);
        save = findViewById(R.id.textViewSave);
        btnShare = findViewById(R.id.btn_share);

        save.setVisibility(View.GONE);
        filterRadiogroup.setVisibility(View.VISIBLE);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        filterRadiogroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.by_name:
                        filterByName(evaluationForms);
                        break;
                    case R.id.by_date:
                        filterByDate(evaluationForms);
                        break;
                }
            }
        });
        etSearch.addTextChangedListener(new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence query, int start, int before, int count) {

                List<Evaluation> filteredList = new ArrayList<>();
                query = query.toString().toLowerCase();
                if (evaluationForms != null) {
                    for (int i = 0; i < evaluationForms.size(); i++) {

                        final String text = evaluationForms.get(i).Driver_First_Name.toLowerCase();
                        if (text.contains(query)) {
                            filteredList.add(evaluationForms.get(i));
                        }
                    }
                    spAdapter.filterList(filteredList);
                }
            }
        });

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
            }
        });

        btnShare.setOnClickListener(new View.OnClickListener() {
                                        @Override
                                        public void onClick(View view) {
                                            String subject = evaluationType + DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_MM_DD_YYYY);
                                            String body = getBodyEmail(evaluationForms);
                                            Intent emailIntent = new Intent(Intent.ACTION_SEND);
                                            emailIntent.setData(Uri.parse("mailto:"));
                                            emailIntent.setType("message/rfc822");
                                            if (!StringUtils.isNullOrWhitespaces(subject))
                                                emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
                                            if (!StringUtils.isNullOrWhitespaces(body))
                                                emailIntent.putExtra(Intent.EXTRA_TEXT, body);
                                            ComponentName componentName = emailIntent.resolveActivity(getPackageManager());
                                            if (componentName != null) {
                                                emailIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);
                                                startActivity(emailIntent);
                                            }
                                        }
                                    }
        );

    }

    public String getBodyEmail(List<Evaluation> evaluationForms) {
        String body = null;
        String evaTitle = "";
        for (int i = 0; i < evaluationForms.size(); i++) {
            if (evaluationForms.get(i).Driver_First_Name != null) {
                if (evaluationForms.get(i).Driver_First_Name != "") {
                    evaTitle = evaluationForms.get(i).Driver_First_Name;
                } else {
                    evaTitle = "no Title";
                }
            } else {
                evaTitle = "no Title";
            }
            evaTitle += " " + DateUtils.convertDateTime(evaluationForms.get(i).Creation_Date, DateUtils.FORMAT_DATE_TIME_MILLIS, DateUtils.FORMAT_DATE_MM_DD_YYYY);
            body += evaTitle + "\n";
        }
        return body;
    }


    public void filterByName(List<Evaluation> evaluationForms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            evaluationForms.sort(new Comparator<Evaluation>() {
                @Override
                public int compare(Evaluation o1, Evaluation o2) {
                    return o1.Driver_First_Name.compareTo(o2.Driver_First_Name);
                }
            });
        }
        spAdapter.filterList(evaluationForms);
    }

    public void filterByDate(List<Evaluation> evaluationForms) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            evaluationForms.sort(new Comparator<Evaluation>() {
                @Override
                public int compare(Evaluation o1, Evaluation o2) {
                    return o2.Creation_Date.compareTo(o1.Creation_Date);
                }
            });
        }
        spAdapter.filterList(evaluationForms);
    }


    public void setupEvaluationView(final String evaluationType) {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this, RecyclerView.VERTICAL, false);
        spRecyclerView.setLayoutManager(linearLayoutManager);
        try {
            evaluationForms = businessRules.getEvaluationList(evaluationType);
            spAdapter = new EvaluationAdapter(evaluationForms, evaluationType);
            spRecyclerView.setAdapter(spAdapter);
        } catch (Exception e) {

            e.printStackTrace();
        }
    }
}