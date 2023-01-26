package com.rco.rcotrucks.activities.forms.driverviolations;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Base64;
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
import com.rco.rcotrucks.utils.ImageUtils;
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
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_SIGNATURE;

public class DVFieldsActivity extends AppCompatActivity {
    private static String TAG = "DVFieldsActivity";
    public static final int REQUESTCODE_SIGNATURE = 1;
    TextView btnPreview, btnSign;

    List<FormField> dvFormList = new ArrayList<>();
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
        setContentView(R.layout.activity_der_fields);
        recyclerView = findViewById(R.id.rv_der);
        btnPreview = findViewById(R.id.btn_preview);
        btnSign = findViewById(R.id.btn_sign);
        tvTitle = findViewById(R.id.tv_title);
        btnCancel = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.textViewSave);

        progress = findViewById(R.id.progress);

        evaluationType = getIntent().getStringExtra(EXTRA_EVALUATION_TYPE);
        if (getIntent().getSerializableExtra(EXTRA_EVALUATION) != null)
            evaluationForm = (Evaluation) getIntent().getSerializableExtra(EXTRA_EVALUATION);
        tvTitle.setText(getString(R.string.forms_title_driver_violation));

        setData();
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(DVFieldsActivity.this, EvaluationPreviewActivity.class);
                previewIntent.putExtra(EXTRA_PREVIEW, (Serializable) dvFormList);
                previewIntent.putExtra(EXTRA_PREVIEW_URL, "file:///android_asset/evaluationform/driverviolation/driverViolations.html");
                startActivity(previewIntent);
            }
        });
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evaluationForm != null && !evaluationForm.objectType.isEmpty() && !evaluationForm.LobjectId.isEmpty()) {
                    Intent intent = new Intent(DVFieldsActivity.this, SignActivity.class);
                    intent.putExtra(Cadp.EXTRA_OBJECT_ID, evaluationForm.LobjectId);
                    intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, evaluationForm.objectType);
                    intent.putExtra(EXTRA_LASTACTIVITY, "FORM_ACTIVITY_DV");
                    startActivityForResult(intent, REQUESTCODE_SIGNATURE);
                } else {
                    UiUtils.showToast(DVFieldsActivity.this, "You must add a new Form First");
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
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_first_name), "firstName", evaluationForm.Driver_First_Name));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_maiddle_name), "middleName", evaluationForm.middleName));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_name), "lastName", evaluationForm.Driver_Last_Name));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_maiddle_security_number), "socialSercurityNumber", evaluationForm.socialSercurityNumber));
        summaryItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_employmentDate), "employmentDate", evaluationForm.employmentDate));

        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_terminalCity), "terminalCity", evaluationForm.terminalCity));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_terminalState), "terminalState", evaluationForm.terminalState));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_license_number), "driverLicenseNumber", evaluationForm.Driver_License_Number));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_drivers_license_state), "driverLicenseState", evaluationForm.Driver_License_State));

        summaryItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_driverLicenseExpirationDate), "driverLicenseExpirationDate", evaluationForm.driverLicenseExpirationDate));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_violationsthisyear), "violationsthisyear", evaluationForm.violationsthisyear));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_carrier_name), "carrierName", evaluationForm.carrierName));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_carrierAddress), "carrierAddress", evaluationForm.carrierAddress));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_reviewedBy), "reviewedBy", evaluationForm.reviewedBy));
        summaryItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_reviewedDate), "reviewedDate", evaluationForm.reviewedDate));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_title), "title", evaluationForm.title));
        dvFormList.addAll(summaryItem);
        listFields.add(new Forms(getString(R.string.field_title_certification_of_violation),summaryItem)) ;

        List<FormField> violationItem = new ArrayList<>();
        violationItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_violation_date), "dateTime_0", evaluationForm.violationDate));
        violationItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_violationOffense), "violationOffense_0", evaluationForm.violationOffense));
        violationItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_violationLocation), "violationLocation_0", evaluationForm.violationLocation));
        violationItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_violationtypeofvehicle), "violationtypeofvehicle_0", evaluationForm.violationtypeofvehicle));
        dvFormList.addAll(violationItem);
        listFields.add(new Forms(getString(R.string.field_title_violation_desc),violationItem)) ;



        FormFieldsAdapter adapter = new FormFieldsAdapter(listFields);
        StickyHeaderLayoutManager layoutManager = new StickyHeaderLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private Evaluation generateEvaluationForm() {

        Evaluation evaluation = new Evaluation();

        evaluation.LobjectId = "";
        evaluation.objectType = "";
        evaluation.mobileRecordId = "DriverViolation";
        evaluation.ItemType = evaluationType;
        evaluation.dateTime = DateUtils.getCurrentDay();
        evaluation.Creation_Date = DateUtils.getCurrentDay();


        for (FormField field : dvFormList) {
            switch (field.getScriptKey()) {
                case "firstName":
                    evaluation.Driver_First_Name = field.getValue();
                    break;
                case "lastName":
                    evaluation.Driver_Last_Name = field.getValue();
                    break;
                case "middleName":
                    evaluation.middleName = field.getValue();
                    break;
                case "socialSercurityNumber":
                    evaluation.socialSercurityNumber = field.getValue();
                    break;
                case "employmentDate":
                    evaluation.employmentDate = field.getValue();
                    break;
                case "terminalCity":
                    evaluation.terminalCity = field.getValue();
                    break;
                case "terminalState":
                    evaluation.terminalState = field.getValue();
                    break;
                case "driverLicenseNumber":
                    evaluation.Driver_License_Number = field.getValue();
                    break;
                case "driverLicenseState":
                    evaluation.Driver_License_State = field.getValue();
                    break;
                case "driverLicenseExpirationDate":
                    evaluation.driverLicenseExpirationDate = field.getValue();
                    break;
                case "violationsthisyear":
                    evaluation.violationsthisyear = field.getValue();
                    break;
                case "carrierName":
                    evaluation.carrierName = field.getValue();
                    break;
                case "carrierAddress":
                    evaluation.carrierAddress = field.getValue();
                    break;
                case "reviewedBy":
                    evaluation.reviewedBy = field.getValue();
                    break;
                case "reviewedDate":
                    evaluation.reviewedDate = field.getValue();
                    break;
                case "title":
                    evaluation.title = field.getValue();
                    break;
                case "dateTime_0":
                    evaluation.violationDate = field.getValue();
                    break;
                case "violationOffense_0":
                    evaluation.violationOffense = field.getValue();
                    break;
                case "violationLocation_0":
                    evaluation.violationLocation = field.getValue();
                    break;
                case "violationtypeofvehicle_0":
                    evaluation.violationtypeofvehicle = field.getValue();
                    break;
            }
        }
        return evaluation;
    }
    private void genSignatureBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return;
        String imagedata = Base64.encodeToString(ImageUtils.getPngBytesFromBitmap(bitmap), Base64.DEFAULT);
        FormField formField1 = new FormField(Cadp.HTML_ELEM_TYPE_BITMAP, "signature", "signature_of_driver", imagedata);
        FormField formField2 = new FormField(Cadp.HTML_ELEM_TYPE_BITMAP, "signature", "signature_of_reviewer", imagedata);
        dvFormList.add(formField1);
        dvFormList.add(formField2);
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
                String jsonReturn = RmsHelperForms.setANewEvaluationDVForms(evaluationList);
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // what does this do?  Should it be at end?  Leave it out?
        Log.e(TAG, "onActivityResult() requestCode=" + requestCode + ", resultCode=" + resultCode
                + ", RESULT_CANCELED=" + RESULT_CANCELED + ", RESULT_OK=" + RESULT_OK
                + ", data==null?" + (data == null));

        if (resultCode == RESULT_OK && data != null) {
            byte[] byteArray = data.getByteArrayExtra(EXTRA_SIGNATURE);
            signatureBitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);
            genSignatureBitmap(signatureBitmap);
        }
    }
}
