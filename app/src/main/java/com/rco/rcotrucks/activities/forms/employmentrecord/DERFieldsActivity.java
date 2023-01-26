package com.rco.rcotrucks.activities.forms.employmentrecord;

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

public class DERFieldsActivity extends AppCompatActivity {
    private static String TAG = "FormFieldDriverEmploymentRecordActivity";
    public static final int REQUESTCODE_SIGNATURE = 1;
    TextView btnPreview, btnSign;

    List<FormField> derFormList = new ArrayList<>();
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
//        tvTitle.setText(getString(R.string.forms_title_der));
        tvTitle.setText(getString(R.string.forms_title_employment_record));
        setData();
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(DERFieldsActivity.this, EvaluationPreviewActivity.class);
                previewIntent.putExtra(EXTRA_PREVIEW, (Serializable) derFormList);
                previewIntent.putExtra(EXTRA_PREVIEW_URL, "file:///android_asset/evaluationform/driveremploymentrecord/driverEmployment.html");
                startActivity(previewIntent);
            }
        });
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evaluationForm != null && !evaluationForm.objectType.isEmpty() && !evaluationForm.LobjectId.isEmpty()) {
                    Intent intent = new Intent(DERFieldsActivity.this, SignActivity.class);
                    intent.putExtra(Cadp.EXTRA_OBJECT_ID, evaluationForm.LobjectId);
                    intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, evaluationForm.objectType);
                    intent.putExtra(EXTRA_LASTACTIVITY, "FORM_ACTIVITY_DER");
                    startActivityForResult(intent, REQUESTCODE_SIGNATURE);
                } else {
                    UiUtils.showToast(DERFieldsActivity.this, "You must add a new Form First");
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
                //https://www.rcofox.com/Image2000/rest/formsservice/setDriverSkillPerformanceEvaluations/austin/austin
                new DERFieldsActivity.HttpCreateEvaluationForm().execute();
            }
        });
    }

    private Evaluation generateEvaluationForm() {

        Evaluation evaluation = new Evaluation();

        evaluation.LobjectId = "";
        evaluation.objectType = "";
        evaluation.mobileRecordId = "DriverEmploymentRecord";
        evaluation.ItemType = evaluationType;
        evaluation.Creation_Date = DateUtils.getCurrentDay();


        for (FormField field : derFormList) {
            switch (field.getScriptKey()) {
                case "lastEmployerName":
                    evaluation.lastEmployerName = field.getValue();
                    break;
                case "lastEmployerAddress":
                    evaluation.lastEmployerAddress = field.getValue();
                    break;
                case "lastEmployerCity":
                    evaluation.lastEmployerCity = field.getValue();
                    break;
                case "lastEmployerState":
                    evaluation.lastEmployerState = field.getValue();
                    break;
                case "lastEmployerZipcode":
                    evaluation.lastEmployerZipcode = field.getValue();
                    break;
                case "lastEmployerTelephoneNumber":
                    evaluation.lastEmployerTelephoneNumber = field.getValue();
                    break;
                case "lastEmployerReasonsforleaving":
                    evaluation.lastEmployerReasonsforleaving = field.getValue();
                    break;
                case "secondLastEmployerName":
                    evaluation.secondLastEmployerName = field.getValue();
                    break;
                case "secondLastEmployerAddress":
                    evaluation.secondLastEmployerAddress = field.getValue();
                    break;
                case "secondLastEmployerCity":
                    evaluation.secondLastEmployerCity = field.getValue();
                    break;
                case "secondLastEmployerState":
                    evaluation.secondLastEmployerState = field.getValue();
                    break;
                case "secondLastEmployerZipcode":
                    evaluation.secondLastEmployerZipcode = field.getValue();
                    break;
                case "secondLastEmployerTelephoneNumber":
                    evaluation.secondLastEmployerTelephoneNumber = field.getValue();
                    break;
                case "secondLastEmployerReasonsforleaving":
                    evaluation.secondLastEmployerReasonsforleaving = field.getValue();
                    break;
                case "thirdLastEmployerName":
                    evaluation.thirdLastEmployerName = field.getValue();
                    break;
                case "thirdLastEmployerAddress":
                    evaluation.thirdLastEmployerAddress = field.getValue();
                    break;
                case "thirdLastEmployerCity":
                    evaluation.thirdLastEmployerCity = field.getValue();
                    break;
                case "thirdLastEmployerState":
                    evaluation.thirdLastEmployerState = field.getValue();
                    break;
                case "thirdLastEmployerZipcode":
                    evaluation.thirdLastEmployerZipcode = field.getValue();
                    break;
                case "thirdLastEmployerTelephoneNumber":
                    evaluation.thirdLastEmployerTelephoneNumber = field.getValue();
                    break;
                case "thirdLastEmployerReasonsforleaving":
                    evaluation.thirdLastEmployerReasonsforleaving = field.getValue();
                    break;
                case "driverFirstName":
                    evaluation.Driver_First_Name = field.getValue();
                    break;
                case "driverLastName":
                    evaluation.Driver_Last_Name = field.getValue();
                    break;
                case "driverMobilePhoneNumber":
                    evaluation.Driver_Mobile_Phone_Number = field.getValue();
                    break;
                case "driverLicenseNumber":
                    evaluation.Driver_License_Number = field.getValue();
                    break;
                case "positionHeld1":
                    evaluation.positionHeld1 = field.getValue();
                    break;
                case "positionHeld2":
                    evaluation.positionHeld2 = field.getValue();
                    break;
                case "positionHeld3":
                    evaluation.positionHeld3 = field.getValue();
                    break;
                case "lastEmployerFromDate":
                    evaluation.lastEmployerFromDate = field.getValue();
                    break;
                case "lastEmployerToDate":
                    evaluation.lastEmployerToDate = field.getValue();
                    break;
                case "secondLastEmployerFromDate":
                    evaluation.secondLastEmployerFromDate = field.getValue();
                    break;
                case "secondLastEmployerToDate":
                    evaluation.secondLastEmployerToDate = field.getValue();
                    break;
                case "thirdLastEmployerFromDate":
                    evaluation.thirdLastEmployerFromDate = field.getValue();
                    break;
                case "thirdLastEmployerToDate":
                    evaluation.thirdLastEmployerToDate = field.getValue();
                    break;
            }
        }
        return evaluation;
    }

    private void setData() {
        List<Forms> listFields = new ArrayList<>() ;
        List<FormField> lastEmployerItem = new ArrayList<>();
        lastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_employer_name), "lastEmployerName", evaluationForm.lastEmployerName));
        lastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_employer_address), "lastEmployerAddress", evaluationForm.lastEmployerAddress));
        lastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_employer_city), "lastEmployerCity", evaluationForm.lastEmployerCity));
        lastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_employer_state), "lastEmployerState", evaluationForm.lastEmployerState));
        lastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_employer_zip_code), "lastEmployerZipcode", evaluationForm.lastEmployerZipcode));
        lastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_employer_telephone_number), "lastEmployerTelephoneNumber", evaluationForm.lastEmployerTelephoneNumber));
        lastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_employer_reason_for_leaving), "lastEmployerReasonsforleaving", evaluationForm.lastEmployerReasonsforleaving));
        lastEmployerItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_last_employer_from_date), "lastEmployerFromDate", evaluationForm.lastEmployerFromDate));
        lastEmployerItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_last_employer_to_date), "lastEmployerToDate", evaluationForm.lastEmployerToDate));
        lastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_position_held_1), "positionHeld1", evaluationForm.positionHeld1));

        derFormList.addAll(lastEmployerItem);
        listFields.add(new Forms(getString(R.string.field_title_last_employer),lastEmployerItem)) ;

        List<FormField> secondLastEmployerItem = new ArrayList<>();
        secondLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_second_last_employer_name), "secondLastEmployerName", evaluationForm.secondLastEmployerName));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_second_last_employer_address), "secondLastEmployerAddress", evaluationForm.secondLastEmployerAddress));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_second_last_employer_city), "secondLastEmployerCity", evaluationForm.secondLastEmployerCity));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_second_last_employer_state), "secondLastEmployerState", evaluationForm.secondLastEmployerState));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_second_last_employer_zip_code), "secondLastEmployerZipcode", evaluationForm.secondLastEmployerZipcode));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_second_last_employer_telephone_number), "secondLastEmployerTelephoneNumber", evaluationForm.secondLastEmployerTelephoneNumber));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_second_last_employer_reason_for_leaving), "secondLastEmployerReasonsforleaving", evaluationForm.secondLastEmployerReasonsforleaving));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_last_employer_from_date), "secondLastEmployerFromDate", evaluationForm.secondLastEmployerFromDate));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_last_employer_to_date), "secondLastEmployerToDate", evaluationForm.secondLastEmployerToDate));
        secondLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_position_held_2), "positionHeld2", evaluationForm.positionHeld2));

        derFormList.addAll(secondLastEmployerItem);
        listFields.add(new Forms(getString(R.string.field_title_second_last_employer),secondLastEmployerItem)) ;

        List<FormField> thirdLastEmployerItem = new ArrayList<>();
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_third_last_employer_name), "thirdLastEmployerName", evaluationForm.thirdLastEmployerName));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_third_last_employer_address), "thirdLastEmployerAddress", evaluationForm.thirdLastEmployerAddress));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_third_last_employer_city), "thirdLastEmployerCity", evaluationForm.thirdLastEmployerCity));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_third_last_employer_state), "thirdLastEmployerState", evaluationForm.thirdLastEmployerState));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_third_last_employer_zip_code), "thirdLastEmployerZipcode", evaluationForm.thirdLastEmployerZipcode));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_third_last_employer_telephone_number), "thirdLastEmployerTelephoneNumber", evaluationForm.thirdLastEmployerTelephoneNumber));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_third_last_employer_reason_for_leaving), "thirdLastEmployerReasonsforleaving", evaluationForm.thirdLastEmployerReasonsforleaving));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_last_employer_from_date), "thirdLastEmployerFromDate", evaluationForm.thirdLastEmployerFromDate));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_last_employer_to_date), "thirdLastEmployerToDate", evaluationForm.thirdLastEmployerToDate));
        thirdLastEmployerItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_position_held_3), "positionHeld3", evaluationForm.positionHeld3));

        derFormList.addAll(thirdLastEmployerItem);
        listFields.add(new Forms(getString(R.string.field_title_third_last_employer),thirdLastEmployerItem)) ;

        List<FormField> driverItem = new ArrayList<>();
        driverItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_first_name), "driverFirstName", evaluationForm.Driver_First_Name));
        driverItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_last_name), "driverLastName", evaluationForm.Driver_Last_Name));
        driverItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_mobile_number), "driverMobilePhoneNumber", evaluationForm.Driver_Mobile_Phone_Number));
        driverItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_license_number), "driverLicenseNumber", evaluationForm.Driver_License_Number));

        derFormList.addAll(driverItem);
        listFields.add(new Forms(getString(R.string.field_title_driver),driverItem)) ;

        FormFieldsAdapter adapter = new FormFieldsAdapter(listFields);
        StickyHeaderLayoutManager layoutManager = new StickyHeaderLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void genSignatureBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return;
        String imagedata = Base64.encodeToString(ImageUtils.getPngBytesFromBitmap(bitmap), Base64.DEFAULT);
        FormField fField1 = new FormField(Cadp.HTML_ELEM_TYPE_BITMAP, "signature", "signature_of_driver", imagedata);
        derFormList.add(fField1);
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
                String jsonReturn = RmsHelperForms.setANewEvaluationDERForms(evaluationList);
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