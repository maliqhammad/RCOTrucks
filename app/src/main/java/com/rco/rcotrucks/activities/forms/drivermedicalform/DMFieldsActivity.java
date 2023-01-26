package com.rco.rcotrucks.activities.forms.drivermedicalform;

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
import java.util.Date;
import java.util.List;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_EVALUATION;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_EVALUATION_TYPE;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_LASTACTIVITY;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_PREVIEW;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_PREVIEW_URL;
import static com.rco.rcotrucks.adapters.Cadp.EXTRA_SIGNATURE;

public class DMFieldsActivity extends AppCompatActivity {
    private static String TAG = "FormFieldDriverMedicalActivity";
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
//        tvTitle.setText(getString(R.string.forms_title_dm));
        tvTitle.setText(getString(R.string.forms_title_driver_medical));
        setData();
        btnPreview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent previewIntent = new Intent(DMFieldsActivity.this, EvaluationPreviewActivity.class);
                previewIntent.putExtra(EXTRA_PREVIEW, (Serializable) derFormList);
                previewIntent.putExtra(EXTRA_PREVIEW_URL, "file:///android_asset/evaluationform/drivermedical/driverMedical.html");
                startActivity(previewIntent);
            }
        });
        btnSign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (evaluationForm != null && !evaluationForm.objectType.isEmpty() && !evaluationForm.LobjectId.isEmpty()) {
                    Intent intent = new Intent(DMFieldsActivity.this, SignActivity.class);
                    intent.putExtra(Cadp.EXTRA_OBJECT_ID, evaluationForm.LobjectId);
                    intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, evaluationForm.objectType);
                    intent.putExtra(EXTRA_LASTACTIVITY, "FORM_ACTIVITY_DM");
                    startActivityForResult(intent, REQUESTCODE_SIGNATURE);
                } else {
                    UiUtils.showToast(DMFieldsActivity.this, "You must add a new Form First");
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
                new DMFieldsActivity.HttpCreateEvaluationForm().execute();
            }
        });
    }

    private Evaluation generateEvaluationForm() {

        Evaluation evaluation = new Evaluation();

        evaluation.LobjectId = "";
        evaluation.objectType = "";
        evaluation.mobileRecordId = "DriverMedical";
        evaluation.ItemType = evaluationType;
        evaluation.Form_Date = DateUtils.getCurrentDay();
        evaluation.Creation_Date = DateUtils.getCurrentDay();
        evaluation.Creation_Time = String.valueOf(new Date().getTime());


        for (FormField field : derFormList) {
            switch (field.getScriptKey()) {
                case "carrierName":
                    evaluation.carrierName = field.getValue();
                    break;
                case "doctorsName":
                    evaluation.doctorsName = field.getValue();
                    break;
                case "sPEApplicantName":
                    evaluation.sPEApplicantName = field.getValue();
                    break;
                case "vehicleTypeStraightTruck":
                    evaluation.vehicleTypeStraightTruck = field.getValue();
                    break;
                case "vehicleTypeTruckTrailerover10klbs":
                    evaluation.vehicleTypeTruckTrailerover10klbs = field.getValue();
                    break;
                case "vehicleTypeTrucklessthan10klbsandhazardousmaterials":
                    evaluation.vehicleTypeTrucklessthan10klbsandhazardousmaterials = field.getValue();
                    break;
                case "vehicleTypeTruckover10klbs":
                    evaluation.vehicleTypeTruckover10klbs = field.getValue();
                    break;
                case "vehicleTypeMotorHome10klbs":
                    evaluation.vehicleTypeMotorHome10klbs = field.getValue();
                    break;
                case "vehicleTypeTractorTrailer":
                    evaluation.vehicleTypeTractorTrailer = field.getValue();
                    break;
                case "vehicleTypePassengerVehicle":
                    evaluation.vehicleTypePassengerVehicle = field.getValue();
                    break;
                case "vehicleTypePassengerSeatingCapacity":
                    evaluation.vehicleTypePassengerSeatingCapacity = field.getValue();
                    break;
                case "vehicleTypePassengerMotorCoach":
                    evaluation.vehicleTypePassengerMotorCoach = field.getValue();
                    break;
                case "vehicleTypePassengerBus":
                    evaluation.vehicleTypePassengerBus = field.getValue();
                    break;
                case "vehicleTypePassengerVan":
                    evaluation.vehicleTypePassengerVan = field.getValue();
                    break;
                case "vehicleTypeshortrelaydrives":
                    evaluation.vehicleTypeshortrelaydrives = field.getValue();
                    break;
                case "vehicleTypelongrelaydrives":
                    evaluation.vehicleTypelongrelaydrives = field.getValue();
                    break;
                case "vehicleTypestraightthrough":
                    evaluation.vehicleTypestraightthrough = field.getValue();
                    break;
                case "vehicleTypenightsawayfromhome":
                    evaluation.vehicleTypenightsawayfromhome = field.getValue();
                    break;
                case "vehicleTypesleeperteamdrives":
                    evaluation.vehicleTypesleeperteamdrives = field.getValue();
                    break;
                case "vehicleTypenumberofnightsawayfromhome":
                    evaluation.vehicleTypenumberofnightsawayfromhome = field.getValue();
                    break;
                case "vehicleTypeclimbinginandoutoftruck":
                    evaluation.vehicleTypeclimbinginandoutoftruck = field.getValue();
                    break;
                case "environmentalFactorsabruptduty":
                    evaluation.environmentalFactorsabruptduty = field.getValue();
                    break;
                case "environmentalFactorssleepdeprivation":
                    evaluation.environmentalFactorssleepdeprivation = field.getValue();
                    break;
                case "environmentalFactorsunbalancedwork":
                    evaluation.environmentalFactorsunbalancedwork = field.getValue();
                    break;
                case "environmentalFactorstemperature":
                    evaluation.environmentalFactorstemperature = field.getValue();
                    break;
                case "environmentalFactorslongtrips":
                    evaluation.environmentalFactorslongtrips = field.getValue();
                    break;
                case "environmentalFactorsshortnotice":
                    evaluation.environmentalFactorsshortnotice = field.getValue();
                    break;
                case "environmentalFactorstightdelivery":
                    evaluation.environmentalFactorstightdelivery = field.getValue();
                    break;
                case "environmentalFactorsdelayenroute":
                    evaluation.environmentalFactorsdelayenroute = field.getValue();
                    break;
                case "environmentalFactorsothers":
                    evaluation.environmentalFactorsothers = field.getValue();
                    break;
                case "physicalDemandGearShifting":
                    evaluation.physicalDemandGearShifting = field.getValue();
                    break;
                case "physicalDemandNumberspeedtransmission":
                    evaluation.physicalDemandNumberspeedtransmission = field.getValue();
                    break;
                case "physicalDemandsemiautomatic":
                    evaluation.physicalDemandsemiautomatic = field.getValue();
                    break;
                case "physicalDemandfullyautomatic":
                    evaluation.physicalDemandfullyautomatic = field.getValue();
                    break;
                case "physicalDemandsteeringwheelcontrol":
                    evaluation.physicalDemandsteeringwheelcontrol = field.getValue();
                    break;
                case "physicalDemandbrakeacceleratoroperation":
                    evaluation.physicalDemandbrakeacceleratoroperation = field.getValue();
                    break;
                case "physicalDemandvarioustasks":
                    evaluation.physicalDemandvarioustasks = field.getValue();
                    break;
                case "physicalDemandbackingandparking":
                    evaluation.physicalDemandbackingandparking = field.getValue();
                    break;
                case "physicalDemandvehicleinspections":
                    evaluation.physicalDemandvehicleinspections = field.getValue();
                    break;
                case "physicalDemandcargohandling":
                    evaluation.physicalDemandcargohandling = field.getValue();
                    break;
                case "physicalDemandcoupling":
                    evaluation.physicalDemandcoupling = field.getValue();
                    break;
                case "physicalDemandchangingtires":
                    evaluation.physicalDemandchangingtires = field.getValue();
                    break;
                case "physicalDemandvehiclemodifications":
                    evaluation.physicalDemandvehiclemodifications = field.getValue();
                    break;
                case "physicalDemandvehiclemodnotes":
                    evaluation.physicalDemandvehiclemodnotes = field.getValue();
                    break;
                case "muscleStrengthyesno":
                    evaluation.muscleStrengthyesno = field.getValue();
                    break;
                case "muscleStrengthrightupperextremity":
                    evaluation.muscleStrengthrightupperextremity = field.getValue();
                    break;
                case "muscleStrengthleftupperextremity":
                    evaluation.muscleStrengthleftupperextremity = field.getValue();
                    break;
                case "muscleStrengthrightlowerextremity":
                    evaluation.muscleStrengthrightlowerextremity = field.getValue();
                    break;
                case "muscleStrengthleftlowerextremity":
                    evaluation.muscleStrengthleftlowerextremity = field.getValue();
                    break;
                case "mobilityyesno":
                    evaluation.mobilityyesno = field.getValue();
                    break;
                case "mobilityrightupperextremity":
                    evaluation.mobilityrightupperextremity = field.getValue();
                    break;
                case "mobilityleftupperextremity":
                    evaluation.mobilityleftupperextremity = field.getValue();
                    break;
                case "mobilityrightlowerextremity":
                    evaluation.mobilityrightlowerextremity = field.getValue();
                    break;
                case "mobilityleftlowerextremity":
                    evaluation.mobilityleftlowerextremity = field.getValue();
                    break;
                case "mobilitytrunk":
                    evaluation.mobilitytrunk = field.getValue();
                    break;
                case "stabilityyesno":
                    evaluation.stabilityyesno = field.getValue();
                    break;
                case "stabilityrightupperextremity":
                    evaluation.stabilityrightupperextremity = field.getValue();
                    break;
                case "stabilityleftupperextremity":
                    evaluation.stabilityleftupperextremity = field.getValue();
                    break;
                case "stabilityrightlowerextremity":
                    evaluation.stabilityrightlowerextremity = field.getValue();
                    break;
                case "stabilityleftlowerextremity":
                    evaluation.stabilityleftlowerextremity = field.getValue();
                    break;
                case "stabilitytrunk":
                    evaluation.stabilitytrunk = field.getValue();
                    break;
                case "impairmenthand":
                    evaluation.impairmenthand = field.getValue();
                    break;
                case "impairmentupperlimb":
                    evaluation.impairmentupperlimb = field.getValue();
                    break;
                case "amputationhand":
                    evaluation.amputationhand = field.getValue();
                    break;
                case "amputationpartial":
                    evaluation.amputationpartial = field.getValue();
                    break;
                case "amputationfull":
                    evaluation.amputationfull = field.getValue();
                    break;
                case "amputationupperlimb":
                    evaluation.amputationupperlimb = field.getValue();
                    break;
                case "powergriprightyesno":
                    evaluation.powergriprightyesno = field.getValue();
                    break;
                case "powergripleftyesno":
                    evaluation.powergripleftyesno = field.getValue();
                    break;
                case "surgicalreconstructionyesno":
                    evaluation.surgicalreconstructionyesno = field.getValue();
                    break;
                case "hasupperimpairment":
                    evaluation.hasupperimpairment = field.getValue();
                    break;
                case "haslowerlimbimpairment":
                    evaluation.haslowerlimbimpairment = field.getValue();
                    break;
                case "hasrightimpairment":
                    evaluation.hasrightimpairment = field.getValue();
                    break;
                case "hasleftimpairment":
                    evaluation.hasleftimpairment = field.getValue();
                    break;
                case "hasupperamputation":
                    evaluation.hasupperamputation = field.getValue();
                    break;
                case "haslowerlimbamputation":
                    evaluation.haslowerlimbamputation = field.getValue();
                    break;
                case "hasrightamputation":
                    evaluation.hasrightamputation = field.getValue();
                    break;
                case "hasleftamputation":
                    evaluation.hasleftamputation = field.getValue();
                    break;
                case "appropriateprosthesisyesno":
                    evaluation.appropriateprosthesisyesno = field.getValue();
                    break;
                case "appropriateterminaldeviceyesno":
                    evaluation.appropriateterminaldeviceyesno = field.getValue();
                    break;
                case "prosthesisfitsyesno":
                    evaluation.prosthesisfitsyesno = field.getValue();
                    break;
                case "useprostheticproficientlyyesno":
                    evaluation.useprostheticproficientlyyesno = field.getValue();
                    break;
                case "abilitytopowergraspyesno":
                    evaluation.abilitytopowergraspyesno = field.getValue();
                    break;
                case "prostheticrecommendations":
                    evaluation.prostheticrecommendations = field.getValue();
                    break;
                case "prostheticclinicaldescription":
                    evaluation.prostheticclinicaldescription = field.getValue();
                    break;
                case "medicalconditionsinterferewithtasksyesno":
                    evaluation.medicalconditionsinterferewithtasksyesno = field.getValue();
                    break;
                case "medicalconditionsinterferewithtasksexplanation":
                    evaluation.medicalconditionsinterferewithtasksexplanation = field.getValue();
                    break;
                case "medicalfindingsandevaluation":
                    evaluation.medicalfindingsandevaluation = field.getValue();
                    break;
                case "physicianlastname":
                    evaluation.physicianlastname = field.getValue();
                    break;
                case "physicianfirstname":
                    evaluation.physicianfirstname = field.getValue();
                    break;
                case "physicianmiddlename":
                    evaluation.physicianmiddlename = field.getValue();
                    break;
                case "physicianaddress":
                    evaluation.physicianaddress = field.getValue();
                    break;
                case "physiciancity":
                    evaluation.physiciancity = field.getValue();
                    break;
                case "physicianstate":
                    evaluation.physicianstate = field.getValue();
                    break;
                case "physicianzipcode":
                    evaluation.physicianzipcode = field.getValue();
                    break;
                case "physiciantelephonenumber":
                    evaluation.physiciantelephonenumber = field.getValue();
                    break;
                case "physicianalternatenumber":
                    evaluation.physicianalternatenumber = field.getValue();
                    break;
                case "physiatrist":
                    evaluation.physiatrist = field.getValue();
                    break;
                case "orthopedicsurgeon":
                    evaluation.orthopedicsurgeon = field.getValue();
                    break;
                case "boardCertifiedyesno":
                    evaluation.boardCertifiedyesno = field.getValue();
                    break;
                case "boardEligibleyesno":
                    evaluation.boardEligibleyesno = field.getValue();
                    break;
                case "physiciandate":
                    evaluation.physiciandate = field.getValue();
                    break;
                case "vehicleTypeslocaldeliveries":
                    evaluation.vehicleTypeslocaldeliveries = field.getValue();
                    break;
                case "physicalDemandmountingsnowchains":
                    evaluation.physicalDemandmountingsnowchains = field.getValue();
                    break;
                case "driverLastName":
                    evaluation.Driver_Last_Name = field.getValue();
                    break;
                case "driverFirstName":
                    evaluation.Driver_First_Name = field.getValue();
                    break;
                case "driverMobilePhoneNumber":
                    evaluation.Driver_Mobile_Phone_Number = field.getValue();
                    break;
                case "driverLicenseNumber":
                    evaluation.Driver_License_Number = field.getValue();
                    break;

            }
        }
        return evaluation;
    }

    private void setData() {
        List<Forms> listFields = new ArrayList<>();
        List<FormField> summaryItem = new ArrayList<>();
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_carrier_name), "lastEmployerName", evaluationForm.carrierName));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_doctors_name), "lastEmployerAddress", evaluationForm.doctorsName));
        summaryItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_sPE_applicant_name), "lastEmployerCity", evaluationForm.sPEApplicantName));

        derFormList.addAll(summaryItem);
        listFields.add(new Forms(getString(R.string.field_title_summury), summaryItem));

        List<FormField> vehicleTypeItem = new ArrayList<>();
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeStraightTruck), "secondLastEmployerName", evaluationForm.vehicleTypeStraightTruck));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeTruckTrailerover10klbs), "secondLastEmployerAddress", evaluationForm.vehicleTypeTruckTrailerover10klbs));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeTrucklessthan10klbsandhazardousmaterials), "secondLastEmployerCity", evaluationForm.vehicleTypeTrucklessthan10klbsandhazardousmaterials));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeTruckover10klbs), "secondLastEmployerState", evaluationForm.vehicleTypeTruckover10klbs));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeMotorHome10klbs), "vehicleTypeMotorHome10klbs", evaluationForm.vehicleTypeMotorHome10klbs));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeTractorTrailer), "vehicleTypeTractorTrailer", evaluationForm.vehicleTypeTractorTrailer));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypePassengerVehicle), "vehicleTypePassengerVehicle", evaluationForm.vehicleTypePassengerVehicle));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_vehicleTypePassengerSeatingCapacity), "vehicleTypePassengerSeatingCapacity", evaluationForm.vehicleTypePassengerSeatingCapacity));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypePassengerMotorCoach), "vehicleTypePassengerMotorCoach", evaluationForm.vehicleTypePassengerMotorCoach));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypePassengerBus), "vehicleTypePassengerBus", evaluationForm.vehicleTypePassengerBus));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypePassengerVan), "vehicleTypePassengerVan", evaluationForm.vehicleTypePassengerVan));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeshortrelaydrives), "vehicleTypeshortrelaydrives", evaluationForm.vehicleTypeshortrelaydrives));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypelongrelaydrives), "vehicleTypelongrelaydrives", evaluationForm.vehicleTypelongrelaydrives));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypestraightthrough), "vehicleTypestraightthrough", evaluationForm.vehicleTypestraightthrough));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_vehicleTypenightsawayfromhome), "vehicleTypenightsawayfromhome", evaluationForm.vehicleTypenightsawayfromhome));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypesleeperteamdrives), "vehicleTypesleeperteamdrives", evaluationForm.vehicleTypesleeperteamdrives));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_vehicleTypenumberofnightsawayfromhome), "vehicleTypenumberofnightsawayfromhome", evaluationForm.vehicleTypenumberofnightsawayfromhome));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeclimbinginandoutoftruck), "vehicleTypeclimbinginandoutoftruck", evaluationForm.vehicleTypeclimbinginandoutoftruck));
        vehicleTypeItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_vehicleTypeslocaldeliveries), "vehicleTypeslocaldeliveries", evaluationForm.vehicleTypeslocaldeliveries));


        derFormList.addAll(vehicleTypeItem);
        listFields.add(new Forms(getString(R.string.field_title_vehicle_type), vehicleTypeItem));

        List<FormField> environmentalFactorsItem = new ArrayList<>();
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorsabruptduty), "environmentalFactorsabruptduty", evaluationForm.environmentalFactorsabruptduty));
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorssleepdeprivation), "environmentalFactorssleepdeprivation", evaluationForm.environmentalFactorssleepdeprivation));
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorsunbalancedwork), "environmentalFactorsunbalancedwork", evaluationForm.environmentalFactorsunbalancedwork));
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorstemperature), "environmentalFactorstemperature", evaluationForm.environmentalFactorstemperature));
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorslongtrips), "environmentalFactorslongtrips", evaluationForm.environmentalFactorslongtrips));
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorsshortnotice), "environmentalFactorsshortnotice", evaluationForm.environmentalFactorsshortnotice));
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorstightdelivery), "environmentalFactorstightdelivery", evaluationForm.environmentalFactorstightdelivery));
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorsdelayenroute), "environmentalFactorsdelayenroute", evaluationForm.environmentalFactorsdelayenroute));
        environmentalFactorsItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_environmentalFactorsothers), "environmentalFactorsothers", evaluationForm.environmentalFactorsothers));

        derFormList.addAll(environmentalFactorsItem);
        listFields.add(new Forms(getString(R.string.field_title_environmental_factors), environmentalFactorsItem));

        List<FormField> physicalDemandItem = new ArrayList<>();
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandGearShifting), "physicalDemandGearShifting", evaluationForm.physicalDemandGearShifting));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physicalDemandNumberspeedtransmission), "physicalDemandNumberspeedtransmission", evaluationForm.physicalDemandNumberspeedtransmission));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandsemiautomatic), "physicalDemandsemiautomatic", evaluationForm.physicalDemandsemiautomatic));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandfullyautomatic), "physicalDemandfullyautomatic", evaluationForm.physicalDemandfullyautomatic));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandsteeringwheelcontrol), "physicalDemandsteeringwheelcontrol", evaluationForm.physicalDemandsteeringwheelcontrol));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandbrakeacceleratoroperation), "physicalDemandbrakeacceleratoroperation", evaluationForm.physicalDemandbrakeacceleratoroperation));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandvarioustasks), "physicalDemandvarioustasks", evaluationForm.physicalDemandvarioustasks));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandbackingandparking), "physicalDemandbackingandparking", evaluationForm.physicalDemandbackingandparking));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandvehicleinspections), "physicalDemandvehicleinspections", evaluationForm.physicalDemandvehicleinspections));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandcargohandling), "physicalDemandcargohandling", evaluationForm.physicalDemandcargohandling));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandcoupling), "physicalDemandcoupling", evaluationForm.physicalDemandcoupling));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandchangingtires), "physicalDemandchangingtires", evaluationForm.physicalDemandchangingtires));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandvehiclemodifications), "physicalDemandvehiclemodifications", evaluationForm.physicalDemandvehiclemodifications));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandvehiclemodnotes), "physicalDemandvehiclemodnotes", evaluationForm.physicalDemandvehiclemodnotes));
        physicalDemandItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physicalDemandmountingsnowchains), "physicalDemandmountingsnowchains", evaluationForm.physicalDemandmountingsnowchains));

        derFormList.addAll(physicalDemandItem);
        listFields.add(new Forms(getString(R.string.field_title_physical_demand), physicalDemandItem));

        List<FormField> muscleStrengthItem = new ArrayList<>();
        muscleStrengthItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_muscleStrengthyesno), "muscleStrengthyesno", evaluationForm.muscleStrengthyesno));
        muscleStrengthItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_muscleStrengthrightupperextremity), "muscleStrengthrightupperextremity", evaluationForm.muscleStrengthrightupperextremity));
        muscleStrengthItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_muscleStrengthleftupperextremity), "muscleStrengthleftupperextremity", evaluationForm.muscleStrengthleftupperextremity));
        muscleStrengthItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_muscleStrengthrightlowerextremity), "muscleStrengthrightlowerextremity", evaluationForm.muscleStrengthrightlowerextremity));
        muscleStrengthItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_muscleStrengthleftlowerextremity), "muscleStrengthleftlowerextremity", evaluationForm.muscleStrengthleftlowerextremity));

        derFormList.addAll(muscleStrengthItem);
        listFields.add(new Forms(getString(R.string.field_title_muscle_strength), muscleStrengthItem));


        List<FormField> mobilityItem = new ArrayList<>();
        mobilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_mobilityyesno), "mobilityyesno", evaluationForm.mobilityyesno));
        mobilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_mobilityrightupperextremity), "mobilityrightupperextremity", evaluationForm.mobilityrightupperextremity));
        mobilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_mobilityleftupperextremity), "mobilityleftupperextremity", evaluationForm.mobilityleftupperextremity));
        mobilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_mobilityrightlowerextremity), "mobilityrightlowerextremity", evaluationForm.mobilityrightlowerextremity));
        mobilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_mobilityleftlowerextremity), "mobilityleftlowerextremity", evaluationForm.mobilityleftlowerextremity));
        mobilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_mobilitytrunk), "mobilitytrunk", evaluationForm.mobilitytrunk));

        derFormList.addAll(mobilityItem);
        listFields.add(new Forms(getString(R.string.field_title_mobility), mobilityItem));


        List<FormField> stabilityItem = new ArrayList<>();
        stabilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_stabilityyesno), "stabilityyesno", evaluationForm.stabilityyesno));
        stabilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_stabilityrightupperextremity), "stabilityrightupperextremity", evaluationForm.stabilityrightupperextremity));
        stabilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_stabilityleftupperextremity), "stabilityleftupperextremity", evaluationForm.stabilityleftupperextremity));
        stabilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_stabilityrightlowerextremity), "stabilityrightlowerextremity", evaluationForm.stabilityrightlowerextremity));
        stabilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_stabilityleftlowerextremity), "stabilityleftlowerextremity", evaluationForm.stabilityleftlowerextremity));
        stabilityItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_stabilitytrunk), "stabilitytrunk", evaluationForm.stabilitytrunk));

        derFormList.addAll(stabilityItem);
        listFields.add(new Forms(getString(R.string.field_title_stability), stabilityItem));


        List<FormField> partItem = new ArrayList<>();
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_impairmenthand), "impairmenthand", evaluationForm.impairmenthand));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_impairmentupperlimb), "impairmentupperlimb", evaluationForm.impairmentupperlimb));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_amputationhand), "amputationhand", evaluationForm.amputationhand));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_amputationpartial), "amputationpartial", evaluationForm.amputationpartial));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_amputationfull), "amputationfull", evaluationForm.amputationfull));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_amputationupperlimb), "amputationupperlimb", evaluationForm.amputationupperlimb));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_powergriprightyesno), "powergriprightyesno", evaluationForm.powergriprightyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_powergripleftyesno), "powergripleftyesno", evaluationForm.powergripleftyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_surgicalreconstructionyesno), "surgicalreconstructionyesno", evaluationForm.surgicalreconstructionyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_hasupperimpairment), "hasupperimpairment", evaluationForm.hasupperimpairment));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_haslowerlimbimpairment), "haslowerlimbimpairment", evaluationForm.haslowerlimbimpairment));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_hasrightimpairment), "hasrightimpairment", evaluationForm.hasrightimpairment));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_hasleftimpairment), "hasleftimpairment", evaluationForm.hasleftimpairment));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_hasupperamputation), "hasupperamputation", evaluationForm.hasupperamputation));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_haslowerlimbamputation), "haslowerlimbamputation", evaluationForm.haslowerlimbamputation));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_hasrightamputation), "hasrightamputation", evaluationForm.hasrightamputation));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_hasleftamputation), "hasleftamputation", evaluationForm.hasleftamputation));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_appropriateprosthesisyesno), "appropriateprosthesisyesno", evaluationForm.appropriateprosthesisyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_appropriateterminaldeviceyesno), "appropriateterminaldeviceyesno", evaluationForm.appropriateterminaldeviceyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_prosthesisfitsyesno), "prosthesisfitsyesno", evaluationForm.prosthesisfitsyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_useprostheticproficientlyyesno), "useprostheticproficientlyyesno", evaluationForm.useprostheticproficientlyyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_abilitytopowergraspyesno), "abilitytopowergraspyesno", evaluationForm.abilitytopowergraspyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_prostheticrecommendations), "prostheticrecommendations", evaluationForm.prostheticrecommendations));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_prostheticclinicaldescription), "prostheticclinicaldescription", evaluationForm.prostheticclinicaldescription));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_medicalconditionsinterferewithtasksyesno), "medicalconditionsinterferewithtasksyesno", evaluationForm.medicalconditionsinterferewithtasksyesno));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_medicalconditionsinterferewithtasksexplanation), "medicalconditionsinterferewithtasksexplanation", evaluationForm.medicalconditionsinterferewithtasksexplanation));
        partItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_medicalfindingsandevaluation), "medicalfindingsandevaluation", evaluationForm.medicalfindingsandevaluation));

        derFormList.addAll(partItem);
        listFields.add(new Forms(getString(R.string.field_title_part_three), partItem));


        List<FormField> physicianItem = new ArrayList<>();
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physicianlastname), "physicianlastname", evaluationForm.physicianlastname));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physicianfirstname), "physicianfirstname", evaluationForm.physicianfirstname));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physicianmiddlename), "physicianmiddlename", evaluationForm.physicianmiddlename));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physicianaddress), "physicianaddress", evaluationForm.physicianaddress));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physiciancity), "physiciancity", evaluationForm.physiciancity));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physicianstate), "physicianstate", evaluationForm.physicianstate));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physicianzipcode), "physicianzipcode", evaluationForm.physicianzipcode));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physiciantelephonenumber), "physiciantelephonenumber", evaluationForm.physiciantelephonenumber));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_physicianalternatenumber), "physicianalternatenumber", evaluationForm.physicianalternatenumber));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_physiatrist), "physiatrist", evaluationForm.physiatrist));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_orthopedicsurgeon), "orthopedicsurgeon", evaluationForm.orthopedicsurgeon));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_boardCertifiedyesno), "boardCertifiedyesno", evaluationForm.boardCertifiedyesno));
        physicianItem.add(new FormField(Cadp.HTML_ELEM_TYPE_CHECKBOX, getString(R.string.field_boardEligibleyesno), "boardEligibleyesno", evaluationForm.boardEligibleyesno));
        physicianItem.add(new FormField(Cadp.HTML_FORMAT_DATE, getString(R.string.field_physiciandate), "physiciandate", evaluationForm.physiciandate));
        derFormList.addAll(physicianItem);
        listFields.add(new Forms(getString(R.string.field_title_physician), physicianItem));

        List<FormField> driverItem = new ArrayList<>();
        driverItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_first_name), "driverFirstName", evaluationForm.Driver_First_Name));
        driverItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_last_name), "driverLastName", evaluationForm.Driver_Last_Name));
        driverItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_mobile_number), "driverMobilePhoneNumber", evaluationForm.Driver_Mobile_Phone_Number));
        driverItem.add(new FormField(Cadp.HTML_ELEM_TYPE_TEXT, getString(R.string.field_driver_license_number), "driverLicenseNumber", evaluationForm.Driver_License_Number));
        derFormList.addAll(driverItem);
        listFields.add(new Forms(getString(R.string.field_title_driver), driverItem));


        FormFieldsAdapter adapter = new FormFieldsAdapter(listFields);
        StickyHeaderLayoutManager layoutManager = new StickyHeaderLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    private void genSignatureBitmap(Bitmap bitmap) {
        if (bitmap == null)
            return;
        String imagedata = Base64.encodeToString(ImageUtils.getPngBytesFromBitmap(bitmap), Base64.DEFAULT);
        FormField fField1 = new FormField(Cadp.HTML_ELEM_TYPE_BITMAP, "signature", "signature_of_physician", imagedata);
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
                String jsonReturn = RmsHelperForms.setANewEvaluationDMForms(evaluationList);
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