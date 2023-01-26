package com.rco.rcotrucks.activities.forms.driverapplication;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.forms.EvaluationPreviewActivity;
import com.rco.rcotrucks.activities.forms.FormField;
import com.rco.rcotrucks.activities.forms.FormFieldsAdapter;
import com.rco.rcotrucks.activities.forms.Forms;
import com.rco.rcotrucks.activities.forms.RmsHelperForms;
import com.rco.rcotrucks.activities.logbook.sign.SignActivity;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.Evaluation;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.UiUtils;

import org.json.JSONArray;
import org.zakariya.stickyheaders.StickyHeaderLayoutManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_EVALUATION;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_EVALUATION_TYPE;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_LASTACTIVITY;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_PREVIEW;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_PREVIEW_URL;

public class DAFieldsActivity extends AppCompatActivity {
    private static String TAG = "DAFieldsActivity";
    public static final int REQUESTCODE_SIGNATURE = 1;
    TextView btnPreview, btnSign;

    List<FormField> darFormList = new ArrayList<>();
    RecyclerView recyclerView;
    private Bitmap signatureBitmap;
    private TextView tvTitle, btnSave;
    private ImageView btnCancel;

    ProgressBar progress;
    String evaluationType = "";

    private Evaluation evaluationForm = new Evaluation();
    private BusinessRules businessRules = BusinessRules.instance();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_da_fields);
        recyclerView = findViewById(R.id.rv_dar);
        btnPreview = findViewById(R.id.btn_preview);
        btnSign = findViewById(R.id.btn_sign);
        tvTitle = findViewById(R.id.tv_title);
        btnCancel = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.textViewSave);

        progress = findViewById(R.id.progress);

        evaluationType = getIntent().getStringExtra(EXTRA_EVALUATION_TYPE);
        if (getIntent().getSerializableExtra(EXTRA_EVALUATION) != null)
            evaluationForm = (Evaluation) getIntent().getSerializableExtra(EXTRA_EVALUATION);
        tvTitle.setText(getString(R.string.forms_title_driver_application));

        setData();
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(DAFieldsActivity.this, EvaluationPreviewActivity.class);
                previewIntent.putExtra(EXTRA_PREVIEW, (Serializable) darFormList);
                previewIntent.putExtra(EXTRA_PREVIEW_URL, "file:///android_asset/evaluationform/driverapplication/driverApplication.html");
                startActivity(previewIntent);
            }
        });
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evaluationForm != null && !evaluationForm.objectType.isEmpty() && !evaluationForm.LobjectId.isEmpty()) {
                    Intent intent = new Intent(DAFieldsActivity.this, SignActivity.class);
                    intent.putExtra(Cadp.EXTRA_OBJECT_ID, evaluationForm.LobjectId);
                    intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, evaluationForm.objectType);
                    intent.putExtra(EXTRA_LASTACTIVITY, "FORM_ACTIVITY_DA");
                    startActivityForResult(intent, REQUESTCODE_SIGNATURE);
                } else {
                    UiUtils.showToast(DAFieldsActivity.this, "You must add a new Form First");
                }
            }
        });
        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new HttpCreateEvaluationForm().execute();
            }
        });
    }

    private void setData() {
        List<Forms> listFields = new ArrayList<>() ;
        List<FormField> summaryItem = new ArrayList<>();
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_company_name), "company", evaluationForm.companyName));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_address), "companyAddress", evaluationForm.companyAddress));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_city), "companyCity", evaluationForm.companyCity));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_state), "companyState", evaluationForm.companyState));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_zipcode), "companyZipcode", evaluationForm.companyZipcode));

        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_first_name), "firstName", evaluationForm.firstName));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_name), "lastName", evaluationForm.lastName));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_maiden_name), "maidenNameIfAny", evaluationForm.maidenName));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_maiddle_name), "middleName", evaluationForm.middleName));

        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_address), "address1", evaluationForm.nameAddress));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_city), "city", evaluationForm.nameCity));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_state), "state", evaluationForm.nameState));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_zipcode), "zip", evaluationForm.nameZip));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_how_long), "driverAddress1HowLong", evaluationForm.nameAddresshowLong));
        summaryItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_date), "dateofBirth", evaluationForm.nameBirthDate));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_maiddle_security_number), "socialSercurityNumber", evaluationForm.socialSercurityNumber));
        darFormList.addAll(summaryItem);
        listFields.add(new Forms(getString(R.string.field_application_for_employment),summaryItem)) ;

        List<FormField> addressItem = new ArrayList<>();
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_address), "driverAddress2", evaluationForm.pastThreeYearsAddress2));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_city), "driverCity2", evaluationForm.pastThreeYearsCity2));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_state), "driverState2", evaluationForm.pastThreeYearsState2));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_zipcode), "driverZipcode2", evaluationForm.pastThreeYearsZipCode2));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_how_long), "driverAddress2HowLong", evaluationForm.pastThreeYearsHowLong2));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_address), "driverAddress3", evaluationForm.pastThreeYearsAddress3));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_city), "driverCity3", evaluationForm.pastThreeYearsCity3));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_state), "driverState3", evaluationForm.pastThreeYearsState3));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_zipcode), "driverZipcode3", evaluationForm.pastThreeYearsZipCode3));
        addressItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_how_long), "driverAddress3HowLong", evaluationForm.pastThreeYearsHowLong3));
        darFormList.addAll(addressItem);
        listFields.add(new Forms(getString(R.string.field_address_for_the_past_three_years),addressItem)) ;

        List<FormField> experienceAndQualificationsItem = new ArrayList<>();
        experienceAndQualificationsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_qualification_name), "driverName_0", evaluationForm.driverName_0));
        experienceAndQualificationsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_qualification_state), "state_0", evaluationForm.state_0));
        experienceAndQualificationsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_qualification_license_number), "licenseNumber_0", evaluationForm.licenseNumber_0));
        experienceAndQualificationsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_qualification_license_type), "licenseType_0", evaluationForm.licenseType_0));
        experienceAndQualificationsItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_qualification_experience_date), "expirationDate_0", evaluationForm.expirationDate_0));
        darFormList.addAll(experienceAndQualificationsItem);
        listFields.add(new Forms(getString(R.string.field_driving_experience_and_qualifications),experienceAndQualificationsItem)) ;

        List<FormField> drivingExperienceItem = new ArrayList<>();
        drivingExperienceItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driving_experience_class), "classofEquipment_0", evaluationForm.classofEquipment_0));
        drivingExperienceItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driving_experience_equipment), "typeofEquipment_0", evaluationForm.typeofEquipment_0));
        drivingExperienceItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_driving_experience_date_from), "dateFrom_0", evaluationForm.dateFrom_0));
        drivingExperienceItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_driving_experience_date_to), "dateTo_0", evaluationForm.dateTo_0));
        drivingExperienceItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driving_experience_miles), "approximateNumberofMiles_0", evaluationForm.approximateNumberofMiles_0));
        darFormList.addAll(drivingExperienceItem);
        listFields.add(new Forms(getString(R.string.field_driving_experience),drivingExperienceItem)) ;

        List<FormField> accidentRecordItem = new ArrayList<>();
        accidentRecordItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_accident_dates), "accidentDate_0", evaluationForm.accidentDate_0));
        accidentRecordItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_accident_nature_of_accident), "natureofAccident_0", evaluationForm.natureofAccident_0));
        accidentRecordItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_accident_fatalities), "fatalities_0", evaluationForm.fatalities_0));
        accidentRecordItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_accident_injuries), "injuries_0", evaluationForm.injuries_0));
        darFormList.addAll(accidentRecordItem);
        listFields.add(new Forms(getString(R.string.field_accident_record_for_past_three_years),accidentRecordItem)) ;

        List<FormField> traffiConvictionsItem = new ArrayList<>();
        traffiConvictionsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_traffic_convictions_locations), "convictionLocation_0", evaluationForm.convictionLocation_0));
        traffiConvictionsItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_traffic_convictions_dates), "convictionDate_0", evaluationForm.convictionDate_0));
        traffiConvictionsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_traffic_convictions_charge), "charge_0", evaluationForm.charge_0));
        traffiConvictionsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_traffic_convictions_penalty), "penalty_0", evaluationForm.penalty_0));
        traffiConvictionsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_traffic_convictions_question_a), "deniedalicenseYes", evaluationForm.deniedalicenseYes));
        traffiConvictionsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_traffic_convictions_question_b), "deniedalicenseNo", evaluationForm.deniedalicenseNo));
        traffiConvictionsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_traffic_convictions_question_a), "driverLicensePermitDenied", evaluationForm.driverLicensePermitDenied));
        traffiConvictionsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_traffic_convictions_question_b), "driverLicensePermitRevokedorSuspended", evaluationForm.driverLicensePermitRevokedorSuspended));
        traffiConvictionsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_traffic_convictions_question_details), "driverLicensePermitNotes", evaluationForm.driverLicensePermitNotes));

        darFormList.addAll(traffiConvictionsItem);
        listFields.add(new Forms(getString(R.string.field_traffic_convictions),traffiConvictionsItem)) ;


        FormFieldsAdapter adapter = new FormFieldsAdapter(listFields);
        StickyHeaderLayoutManager layoutManager = new StickyHeaderLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private Evaluation generateEvaluationForm() {

        Evaluation evaluation = new Evaluation();

        evaluation.LobjectId = "";
        evaluation.objectType = "";
        evaluation.mobileRecordId = "DriverApplication";
        evaluation.ItemType = evaluationType;
        evaluation.Form_Date = DateUtils.getCurrentDay();
        evaluation.Creation_Date = DateUtils.getCurrentDay();


        for (FormField field : darFormList) {
            switch (field.getScriptKey()) {
                case "company":
                    evaluation.companyName = field.getValue();
                    break;
                case "companyAddress":
                    evaluation.companyAddress = field.getValue();
                    break;
                case "companyCity":
                    evaluation.companyCity = field.getValue();
                    break;
                case "companyState":
                    evaluation.companyState = field.getValue();
                    break;
                case "companyZipcode":
                    evaluation.companyZipcode = field.getValue();
                    break;
                case "firstName":
                    evaluation.firstName = field.getValue();
                    break;
                case "lastName":
                    evaluation.lastName = field.getValue();
                    break;
                case "maidenNameIfAny":
                    evaluation.maidenName = field.getValue();
                    break;
                case "middleName":
                    evaluation.middleName = field.getValue();
                    break;

                case "address1":
                    evaluation.nameAddress = field.getValue();
                    break;
                case "city":
                    evaluation.nameCity = field.getValue();
                    break;
                case "state":
                    evaluation.nameState = field.getValue();
                    break;
                case "zip":
                    evaluation.nameZip = field.getValue();
                    break;
                case "driverAddress1HowLong":
                    evaluation.nameAddresshowLong = field.getValue();
                    break;
                case "dateofBirth":
                    evaluation.nameBirthDate = field.getValue();
                    break;
                case "driverAddress2":
                    evaluation.pastThreeYearsAddress2 = field.getValue();
                    break;
                case "driverCity2":
                    evaluation.pastThreeYearsCity2 = field.getValue();
                    break;
                case "driverState2":
                    evaluation.pastThreeYearsState2 = field.getValue();
                    break;
                case "driverZipcode2":
                    evaluation.pastThreeYearsZipCode2 = field.getValue();
                    break;
                case "driverAddress2HowLong":
                    evaluation.pastThreeYearsHowLong2 = field.getValue();
                    break;
                case "driverAddress3":
                    evaluation.pastThreeYearsAddress3 = field.getValue();
                    break;
                case "driverCity3":
                    evaluation.pastThreeYearsCity3 = field.getValue();
                    break;
                case "driverState3":
                    evaluation.pastThreeYearsState3 = field.getValue();
                    break;
                case "driverZipcode3":
                    evaluation.pastThreeYearsZipCode3 = field.getValue();
                    break;
                case "driverAddress3HowLong":
                    evaluation.pastThreeYearsHowLong3 = field.getValue();
                    break;
                case "driverName_0":
                    evaluation.driverName_0 = field.getValue();
                    break;
                case "state_0":
                    evaluation.state_0 = field.getValue();
                    break;
                case "licenseNumber_0":
                    evaluation.licenseNumber_0 = field.getValue();
                    break;
                case "licenseType_0":
                    evaluation.licenseType_0 = field.getValue();
                    break;
                case "expirationDate_0":
                    evaluation.expirationDate_0 = field.getValue();
                    break;
                case "classofEquipment_0":
                    evaluation.classofEquipment_0 = field.getValue();
                    break;
                case "typeofEquipment_0":
                    evaluation.typeofEquipment_0 = field.getValue();
                    break;
                case "dateFrom_0":
                    evaluation.dateFrom_0 = field.getValue();
                    break;
                case "dateTo_0":
                    evaluation.dateTo_0 = field.getValue();
                    break;
                case "approximateNumberofMiles_0":
                    evaluation.approximateNumberofMiles_0 = field.getValue();
                    break;
                case "accidentDate_0":
                    evaluation.accidentDate_0 = field.getValue();
                    break;
                case "natureofAccident_0":
                    evaluation.natureofAccident_0 = field.getValue();
                    break;
                case "fatalities_0":
                    evaluation.fatalities_0 = field.getValue();
                    break;
                case "injuries_0":
                    evaluation.injuries_0 = field.getValue();
                    break;
                case "convictionLocation_0":
                    evaluation.convictionLocation_0 = field.getValue();
                    break;
                case "convictionDate_0":
                    evaluation.convictionDate_0 = field.getValue();
                    break;
                case "charge_0":
                    evaluation.charge_0 = field.getValue();
                    break;
                case "penalty_0":
                    evaluation.penalty_0 = field.getValue();
                    break;
                case "deniedalicenseYes":
                    evaluation.deniedalicenseYes = field.getValue();
                    break;
                case "deniedalicenseNo":
                    evaluation.deniedalicenseNo = field.getValue();
                    break;
                case "driverLicensePermitDenied":
                    evaluation.driverLicensePermitDenied = field.getValue();
                    break;
                case "driverLicensePermitRevokedorSuspended":
                    evaluation.driverLicensePermitRevokedorSuspended = field.getValue();
                    break;
                case "driverLicensePermitNotes":
                    evaluation.driverLicensePermitNotes = field.getValue();
                    break;
            }
        }
        return evaluation;
    }

    public class HttpCreateEvaluationForm extends AsyncTask<String, Void, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {

            ArrayList<Evaluation> evaluationList = new ArrayList<>();
            Evaluation evaluationForm = generateEvaluationForm();
            evaluationList.add(evaluationForm);
            try {
                String jsonReturn = RmsHelperForms.setANewEvaluationDAForms(evaluationList);
                Log.e("Response", "" + jsonReturn);


                if (jsonReturn == null || jsonReturn.equalsIgnoreCase("[]"))
                    return null;
                JSONArray response = new JSONArray(jsonReturn);

                int lObjectId = response.getJSONObject(0).getInt("LobjectId");
                String objectType = response.getJSONObject(0).getString("objectType");

                evaluationForm.LobjectId = "" + lObjectId;
                evaluationForm.objectType = "" + objectType;

                businessRules.insertEvaluation(evaluationForm);

                return jsonReturn;
            } catch (Exception e) {
                e.printStackTrace();
            }

            return "";
        }

        @Override
        protected void onPostExecute(String response) {
            super.onPostExecute(response);
            progress.setVisibility(View.GONE);
            finish();
        }
    }
}
