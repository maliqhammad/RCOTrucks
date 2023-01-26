package com.rco.rcotrucks.activities.forms.spe;

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
import com.rco.rcotrucks.businesslogic.rms.Crms;
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

public class SPEFieldsActivity extends AppCompatActivity {

    private static String TAG = "FormFieldsActivity";
    public static final int REQUESTCODE_SIGNATURE = 1;
    TextView btnPreview, btnSign;

    List<FormField> formFieldList = new ArrayList<>();
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
        setContentView(R.layout.activity_form_fields);

        recyclerView = findViewById(R.id.rv_sp);
        btnPreview = findViewById(R.id.btn_preview);
        btnSign = findViewById(R.id.btn_sign);
        tvTitle = findViewById(R.id.tv_title);
        btnCancel = findViewById(R.id.btn_back);
        btnSave = findViewById(R.id.textViewSave);

        progress = findViewById(R.id.progress);

        evaluationType = getIntent().getStringExtra(EXTRA_EVALUATION_TYPE);
        if (getIntent().getSerializableExtra(EXTRA_EVALUATION) != null)
            evaluationForm = (Evaluation) getIntent().getSerializableExtra(EXTRA_EVALUATION);
//        tvTitle.setText(getString(R.string.forms_title_spe));
        tvTitle.setText(getString(R.string.forms_title_skill_performance_just));
        setData();

        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(SPEFieldsActivity.this, EvaluationPreviewActivity.class);
                previewIntent.putExtra(EXTRA_PREVIEW, (Serializable) formFieldList);
                previewIntent.putExtra(EXTRA_PREVIEW_URL, "file:///android_asset/evaluationform/driverskillperformance/driverSkillPerformance.html");
                startActivity(previewIntent);
            }
        });

        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evaluationForm != null && !evaluationForm.objectType.isEmpty() && !evaluationForm.LobjectId.isEmpty()) {
                    Intent intent = new Intent(SPEFieldsActivity.this, SignActivity.class);
                    intent.putExtra(Cadp.EXTRA_OBJECT_ID, evaluationForm.LobjectId);
                    intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, evaluationForm.objectType);
                    intent.putExtra(EXTRA_LASTACTIVITY, "FORM_ACTIVITY");
                    startActivityForResult(intent, REQUESTCODE_SIGNATURE);
                } else {
                    UiUtils.showToast(SPEFieldsActivity.this, "You must add a new Form First");
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
                new HttpCreateEvaluationForm().execute();
            }
        });
    }

    private Evaluation generateEvaluationForm() {

        Evaluation evaluation = new Evaluation();

        evaluation.LobjectId = "";
        evaluation.objectType = "";
        evaluation.mobileRecordId = "DriverSkillEvaluation";
        evaluation.ItemType = evaluationType;
        evaluation.Form_Date = DateUtils.getCurrentDay();
        evaluation.Creation_Date = DateUtils.getCurrentDay();

        for (FormField field : formFieldList) {
            switch (field.getScriptKey()) {
                case "applicationTypeUnilateral":
                    evaluation.Application_Type_Unilateral = field.getValue();
//                    evaluation.Application_Type_Unilateral = "Unilateral";
                    break;
                case "applicationTypeJoint":
                    evaluation.Application_Type_Joint = field.getValue();
                    break;
                case "lastName":
                    evaluation.Driver_Last_Name = field.getValue();
                    break;
                case "firstName":
                    evaluation.Driver_First_Name = field.getValue();
                    break;
                case "driverMaidenName":
                    evaluation.Driver_Maiden_Name = field.getValue();
                    break;
                case "dateofBirth":
                    evaluation.Date_of_Birth = field.getValue();
                    break;
                case "sex":
                    evaluation.Sex = field.getValue();
                    break;
                case "address1":
                    evaluation.Driver_Address = field.getValue();
                    break;
                case "state":
                    evaluation.Driver_State = field.getValue();
                    break;
                case "city":
                    evaluation.Driver_City = field.getValue();
                    break;
                case "zip":
                    evaluation.Driver_Zipcode = field.getValue();
                    break;
                case "homePhone":
                    evaluation.Driver_Home_Phone_Number = field.getValue();
                    break;
                case "mobilePhone":
                    evaluation.Driver_Mobile_Phone_Number = field.getValue();
                    break;
                case "driverLicenseNumber":
                    evaluation.Driver_License_Number = field.getValue();
                    break;
                case "driverLicenseState":
                    evaluation.Driver_License_State = field.getValue();
                    break;
                case "descriptionofimpairmentoramputation":
                    evaluation.Description_of_impairment_or_amputation = field.getValue();
                    break;
                case "typeofprosthesisworn":
                    evaluation.Type_of_prosthesis_worn = field.getValue();
                    break;
                case "statesofoperation":
                    evaluation.States_of_operation = field.getValue();
                    break;
                case "typeofcargo":
                    evaluation.Type_of_cargo = field.getValue();
                    break;
                case "averageperiodofdrivingtime":
                    evaluation.Average_period_of_driving_time = field.getValue();
                    break;
                case "typeofoperation":
                    evaluation.Type_of_operation = field.getValue();
                    break;
                case "numberofyearsdrivingvehicletype":
                    evaluation.Number_of_years_driving_vehicle_type = field.getValue();
                    break;
                case "numberofyearsdrivingallvehicletypes":
                    evaluation.Number_of_years_driving_all_vehicle_types = field.getValue();
                    break;
                case "vehicleType":
                    evaluation.Vehicle_Type = field.getValue();
                    break;
                case "vehicleSeatingCapacity":
                    evaluation.Vehicle_Seating_Capacity = field.getValue();
                    break;
                case "vehicleMake":
                    evaluation.Vehicle_Make = field.getValue();
                    break;
                case "vehicleModel":
                    evaluation.Vehicle_Model = field.getValue();
                    break;
                case "vehicleYear":
                    evaluation.Vehicle_Year = field.getValue();
                    break;
                case "vehicleTransmissionType":
                    evaluation.Vehicle_Transmission_Type = field.getValue();
                    break;
                case "vehicle_numberof_forward_speeds":
                    evaluation.Vehicle_Number_of_Forward_Speeds = field.getValue();
                    break;
                case "vehicleRearAxleSpeed":
                    evaluation.Vehicle_Rear_Axle_Speed = field.getValue();
                    break;
                case "vehicleTypeBrakeSystem":
                    evaluation.Vehicle_Type_Brake_System = field.getValue();
                    break;
                case "vehicleTypeSteeringSystem":
                    evaluation.Vehicle_Type_Steering_System = field.getValue();
                    break;
                case "numberofTrailers":
                    evaluation.Number_of_Trailers = field.getValue();
                    break;
                case "descriptionoftrailers":
                    evaluation.Description_of_trailers = field.getValue();
                    break;
                case "vehicleModifications":
                    evaluation.Vehicle_Modifications = field.getValue();
                    break;
                case "instructor_first_name":
                    evaluation.Instructor_First_Name = field.getValue();
                    break;
                case "instructor_last_name":
                    evaluation.Instructor_Last_Name = field.getValue();
                    break;
                case "instructor_driver_license_num":
                    evaluation.Instructor_Driver_License_Number = field.getValue();
                    break;
                case "instructor_location":
                    evaluation.Location = field.getValue();
                    break;
            }
        }
        return evaluation;
    }

    private void setData() {
        List<Forms> listFields = new ArrayList<>();
        List<FormField> applicationTypesItem = new ArrayList<>();
//        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_unilateral), "applicationTypeUnilateral", evaluationForm.Application_Type_Unilateral));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_unilateral), "applicationTypeUnilateral", evaluationForm.Application_Type_Unilateral));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_joint), "applicationTypeJoint", evaluationForm.Application_Type_Joint));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_last_name), "lastName", evaluationForm.Driver_Last_Name));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_first_name), "firstName", evaluationForm.Driver_First_Name));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_maiden_name), "driverMaidenName", evaluationForm.Driver_Maiden_Name));
        applicationTypesItem.add(new FormField(Cadp.HTML_FORMAT_DATE, "dateofBirth", "dateofBirth", evaluationForm.Date_of_Birth));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_sex), "sex", evaluationForm.Sex));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_address), "address1", evaluationForm.Driver_Address));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_state), "state", evaluationForm.Driver_State));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_city), "city", evaluationForm.Driver_City));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_zipcode), "zip", evaluationForm.Driver_Zipcode));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_home_phone), "homePhone", evaluationForm.Driver_Home_Phone_Number));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_cell), "mobilePhone", evaluationForm.Driver_Mobile_Phone_Number));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_drivers_license), "driverLicenseNumber", evaluationForm.Driver_License_Number));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_drivers_license_state), "driverLicenseState", evaluationForm.Driver_License_State));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_description_of_impairment), "descriptionofimpairmentoramputation", evaluationForm.Description_of_impairment_or_amputation));
        applicationTypesItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_type_of_prosthesis_worn), "typeofprosthesisworn", evaluationForm.Type_of_prosthesis_worn));

        listFields.add(new Forms(getString(R.string.field_title_app_type), applicationTypesItem));
        formFieldList.addAll(applicationTypesItem);

        List<FormField> itemsDataOp = new ArrayList<>();
        itemsDataOp.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_states_of_operation), "statesofoperation", evaluationForm.States_of_operation));
        itemsDataOp.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_type_of_cargo), "typeofcargo", evaluationForm.Type_of_cargo));
        itemsDataOp.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_average_period_of_driving_time), "averageperiodofdrivingtime", evaluationForm.Average_period_of_driving_time));
        itemsDataOp.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_type_of_operation), "typeofoperation", evaluationForm.Type_of_operation));
        itemsDataOp.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_num_of_years_experience), "numberofyearsdrivingvehicletype", evaluationForm.Number_of_years_driving_vehicle_type));
        itemsDataOp.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_num_of_years_driving), "numberofyearsdrivingallvehicletypes", evaluationForm.Number_of_years_driving_all_vehicle_types));

        listFields.add(new Forms(getString(R.string.field_title_desc_of_op), itemsDataOp));
        formFieldList.addAll(itemsDataOp);

        List<FormField> itemsDataVeh = new ArrayList<>();
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_vehicle_type), "vehicleType", evaluationForm.Vehicle_Type));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_bus_seating_capacity), "vehicleSeatingCapacity", evaluationForm.Vehicle_Seating_Capacity));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_make), "vehicleMake", evaluationForm.Vehicle_Make));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_year), "vehicleModel", evaluationForm.Vehicle_Model));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_year), "vehicleYear", evaluationForm.Vehicle_Year));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_transmission_type), "vehicleTransmissionType", evaluationForm.Vehicle_Transmission_Type));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_num_of_forward_speeds), "vehicle_numberof_forward_speeds", evaluationForm.Vehicle_Number_of_Forward_Speeds));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_rear_axle_speed), "vehicleRearAxleSpeed", evaluationForm.Vehicle_Rear_Axle_Speed));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_type_brake_system), "vehicleTypeBrakeSystem", evaluationForm.Vehicle_Type_Brake_System));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_steering), "vehicleTypeSteeringSystem", evaluationForm.Vehicle_Type_Steering_System));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_semitrailers), "numberofTrailers", evaluationForm.Number_of_Trailers));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_description_of_trailers), "descriptionoftrailers", evaluationForm.Description_of_trailers));
        itemsDataVeh.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_description_of_vehicle_modifications), "vehicleModifications", evaluationForm.Vehicle_Modifications));

        listFields.add(new Forms(getString(R.string.field_title_desc_of_veh), itemsDataVeh));
        formFieldList.addAll(itemsDataVeh);

        List<FormField> itemsInstructor = new ArrayList<>();
        itemsInstructor.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_instructor_first_name), "instructor_first_name", evaluationForm.Instructor_First_Name));
        itemsInstructor.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_instructor_last_name), "instructor_last_name", evaluationForm.Driver_Last_Name));
        itemsInstructor.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_instructor_driver_license_num), "instructor_driver_license_num", evaluationForm.Instructor_Driver_License_Number));
        itemsInstructor.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_instructor_location), "instructor_location", evaluationForm.Location));

        listFields.add(new Forms(getString(R.string.field_title_instructor), itemsInstructor));
        formFieldList.addAll(itemsInstructor);


        FormFieldsAdapter adapter = new FormFieldsAdapter(listFields);
        StickyHeaderLayoutManager layoutManager = new StickyHeaderLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void genSignatureBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return;
        String imagedata = Base64.encodeToString(ImageUtils.getPngBytesFromBitmap(bitmap), Base64.DEFAULT);
        FormField formField1 = new FormField(Cadp.HTML_ELEM_TYPE_BITMAP, "signature", "signature_of_driver", imagedata);
        FormField formField2 = new FormField(Cadp.HTML_ELEM_TYPE_BITMAP, "signature", "signature_of_driver2", imagedata);
        formFieldList.add(formField1);
        formFieldList.add(formField2);
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
                String jsonReturn = RmsHelperForms.setANewEvaluationSPEForms(evaluationList);
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