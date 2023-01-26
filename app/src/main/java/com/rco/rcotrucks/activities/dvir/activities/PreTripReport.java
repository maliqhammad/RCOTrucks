package com.rco.rcotrucks.activities.dvir.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.FileProvider;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.dvir.fragments.PreTripReportFragmentUpdate;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.model.PretripModel;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class PreTripReport extends BaseActivity {

    private static String TAG = PreTripReport.class.getSimpleName();
    TextView email;
    PretripModel intentDataModel;
    EditText organizationNameET, addressET, dateET, timeET, truckNumberET, odometerET, trailer1NumberET, trailer1ReeferHOSET,
            trailer2NumberET, trailer2ReeferHOSET, remarksET, driverNameET, mechanicNameET, driverSignatureDateET, mechanicSignatureDateET;

    CheckBox airCompressorCB, airLinesCB, batteryCB, brakeAccessoriesCB, brakesCB, carburetorCB, clutchCB, defrosterCB,
            driveLineCB, fifthWheelCB, registrationCB, insuranceCB, frontalAxleCB, fuelTanksCB, heaterCB, hornCB, lightsCB, mirrorsCB, oilPressureCB,
            onBoardRecorderCB, radiatorCB, rearEndCB, reflectorsCB, safetyEquipmentCB, springsCB, starterCB, steeringCB,
            tachographCB, tiresCB, transmissionCB, wheelsCB, windowsCB, windShieldWipersCB, othersCB,
            trailer1BreakConnectionsCB, trailer1BrakesCB, trailer1CouplingPinCB, trailer1CouplingChainsCB, trailer1DoorsCB,
            trailer1HitchCB, trailer1LandingGearCB, trailer1LightsAllCB, trailer1RoofCB, trailer1SpringsCB, trailer1TarpaulinCB,
            trailer1TiresCB, trailer1WheelsCB, trailer1OthersCB,
            trailer2BreakConnectionsCB, trailer2BrakesCB, trailer2CouplingPinCB, trailer2CouplingChainsCB, trailer2DoorsCB,
            trailer2HitchCB, trailer2LandingGearCB, trailer2LightsAllCB, trailer2RoofCB, trailer2SpringsCB,
            trailer2TarpaulinCB, trailer2TiresCB, trailer2WheelsCB, trailer2OthersCB,
            conditionVehicleIsSatisfactoryCB, aboveDefectsCorrectedCB, aboveDefectsNoCorrectionNeededCB, selectAll;

    ImageView backIcon, mechanicSignature, driverSignature;

    ConstraintLayout preTripConstraintLayout;
    ScrollView preTripScrollView;
    public static Bitmap bitScroll;

    String id = "", objectId = "", objectType = "", mobileRecordId = "", functionalGroupName = "", organizationName = "", organizationNumber = "",
            dateTime = "", latitude = "", longitude = "", firstName = "", lastName = "", recordId = "", vehicleLicenseNumber = "",
            airCompressor = "", airLines = "", battery = "", brakeAccessories = "", brakes = "", carburetor = "", clutch = "", defroster = "",
            driveLine = "", fifthWheel = "", frontalAxle = "", fuelTanks = "", heater = "", horn = "", lights = "", mirrors = "", oilPressure = "",
            onBoardRecorder = "", radiator = "", rearEnd = "", reflectors = "", safetyEquipment = "", springs = "", starter = "", steering = "",
            tachograph = "", tires = "", transmission = "", wheels = "", windows = "", windShieldWipers = "", others = "", trailer1 = "",
            trailer1BreakConnections = "", trailer1Breaks = "", trailer1CouplingPin = "", trailer1CouplingChains = "", trailer1Doors = "",
            trailer1Hitch = "", trailer1LandingGear = "", trailer1LightsAll = "", trailer1Roof = "", trailer1Springs = "", trailer1Tarpaulin = "",
            trailer1Tires = "", trailer1Wheels = "", trailer1Others = "", trailer2 = "", trailer2BreakConnections = "", trailer2Breaks = "",
            trailer2CouplingPin = "", trailer2CouplingChains = "", trailer2Doors = "", trailer2Hitch = "", trailer2LandingGear = "",
            trailer2LightsAll = "", trailer2Roof = "", trailer2Springs = "", trailer2Tarpaulin = "", trailer2Tires = "", trailer2Wheels = "",
            trailer2Others = "", remarks = "", conditionVehicleIsSatisfactory = "", driversSignatureVehicleSatisfactory = "", aboveDefectsCorrected = "",
            aboveDefectsNoCorrectionNeeded = "", mechanicSignatureDate = "", driversSignatureNoCorrectionNeeded = "",
            driversSignatureNoCorrectionNeededDate = "", truckNumber = "", carrier = "", address = "", odometer = "", mechanicFirstName = "",
            mechanicLastName = "", mechanicRecordId = "", trailer1ReeferHOS = "", trailer2ReeferHOS = "",
            registration = "", insurance = "", rmsTimestamp = "", sent = "", mechanicNameAndSignature = "", driverNameAndSignature = "",
            mechanicSignatureBitmap = "", driverSignatureBitmap = "", driverSignatureDate = "";

    boolean isEditable = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pre_trip_report);

        getIntentData();
        setIds();
        setIdsFromActivity();
        initialize();
        initializeFields();
        setListener();
    }

    void getIntentData() {
        Log.d(TAG, "getIntentData: ");

        if (getIntent() != null) {
            Intent intentBundle = getIntent();
            intentDataModel = (PretripModel) getIntent().getExtras().getSerializable("dataModel");
            isEditable = intentBundle.getExtras().getBoolean("isEditable", false);

            Log.d(TAG, "getIntentData: mechanicSignature: dateTime: " + intentDataModel.getDateTime());
            Log.d(TAG, "getIntentData: mechanicSignature: trailer1Springs: " + intentDataModel.getTrailer1Springs());
            Log.d(TAG, "getIntentData: mechanicSignature: trailer2Springs: " + intentDataModel.getTrailer2Springs());
            Log.d(TAG, "getIntentData: mechanicSignature: mechanicSignatureDate: " + intentDataModel.getMechanicsSignatureDate());
            Log.d(TAG, "getIntentData: mechanicSignature: driverSignatureDate: " + intentDataModel.getDriverSignatureDate());
        }
    }

    void setIds() {

        preTripConstraintLayout = findViewById(R.id.pre_trip_constraint_layout);
        preTripScrollView = findViewById(R.id.pre_trip_preview_scroll_view);

        organizationNameET = findViewById(R.id.carrier_et);
        addressET = findViewById(R.id.address_et);
        dateET = findViewById(R.id.date_et);
        timeET = findViewById(R.id.time_et);
        truckNumberET = findViewById(R.id.truck_no_et);
        odometerET = findViewById(R.id.odometer_et);
        trailer1NumberET = findViewById(R.id.trailer_no_et_one);
        trailer1ReeferHOSET = findViewById(R.id.reefer_hos_et_one);
        trailer2NumberET = findViewById(R.id.trailer_no_et_two);
        trailer2ReeferHOSET = findViewById(R.id.reefer_hos_et_two);
        remarksET = findViewById(R.id.remarks);
        driverNameET = findViewById(R.id.driver_name_et);
        mechanicNameET = findViewById(R.id.mechanic_name_et);
        driverSignatureDateET = findViewById(R.id.driver_signature_date);
        mechanicSignatureDateET = findViewById(R.id.mechanic_signature_date);

        selectAll = findViewById(R.id.select_all);

        airCompressorCB = findViewById(R.id.air_compressor_cb);
        airLinesCB = findViewById(R.id.air_lines_cb);
        batteryCB = findViewById(R.id.battery_cb);
        brakeAccessoriesCB = findViewById(R.id.brake_accessories_cb);
        brakesCB = findViewById(R.id.brakes_cb);
        carburetorCB = findViewById(R.id.carburetor_cb);
        clutchCB = findViewById(R.id.clutch_cb);
        defrosterCB = findViewById(R.id.defroster_cb);
        driveLineCB = findViewById(R.id.drive_line_cb);
        fifthWheelCB = findViewById(R.id.fifth_wheel_cb);
        registrationCB = findViewById(R.id.registration_cb);
        insuranceCB = findViewById(R.id.insurance_cb);
        frontalAxleCB = findViewById(R.id.front_axle_cb);
        fuelTanksCB = findViewById(R.id.fuel_tanks_cb);
        heaterCB = findViewById(R.id.heater_cb);
        hornCB = findViewById(R.id.horn_cb);
        lightsCB = findViewById(R.id.lights_head_cb);
        mirrorsCB = findViewById(R.id.mirrors_cb);
        oilPressureCB = findViewById(R.id.oil_pressure_cb);
        onBoardRecorderCB = findViewById(R.id.on_board_recorder_cb);
        radiatorCB = findViewById(R.id.radiator_cb);
        rearEndCB = findViewById(R.id.rear_end_cb);
        reflectorsCB = findViewById(R.id.reflector_cb);
        safetyEquipmentCB = findViewById(R.id.safety_equipment_cb);
        springsCB = findViewById(R.id.springs_cb);
        starterCB = findViewById(R.id.starter_cb);
        steeringCB = findViewById(R.id.steering_cb);
        tachographCB = findViewById(R.id.tachograph_cb);
        tiresCB = findViewById(R.id.tires_cb);
        transmissionCB = findViewById(R.id.transmission_cb);
        wheelsCB = findViewById(R.id.wheels_and_rims_cb);
        windowsCB = findViewById(R.id.windows_cb);
        windShieldWipersCB = findViewById(R.id.wind_shield_wipers_cb);
        othersCB = findViewById(R.id.other_cb);

        trailer1BreakConnectionsCB = findViewById(R.id.brake_connection_one_cb);
        trailer1BrakesCB = findViewById(R.id.brakes_one_cb);
        trailer1CouplingPinCB = findViewById(R.id.coupling_king_pin_one_cb);
        trailer1CouplingChainsCB = findViewById(R.id.coupling_chains_one_cb);
        trailer1DoorsCB = findViewById(R.id.doors_one_cb);
        trailer1HitchCB = findViewById(R.id.hitch_one_cb);
        trailer1LandingGearCB = findViewById(R.id.landing_gear_one_cb);
        trailer1LightsAllCB = findViewById(R.id.lights_all_one_cb);
        trailer1RoofCB = findViewById(R.id.roof_one_cb);
        trailer1SpringsCB = findViewById(R.id.spring_one_cb);
        trailer1TarpaulinCB = findViewById(R.id.tarpaulin_one_cb);
        trailer1TiresCB = findViewById(R.id.tires_one_cb);
        trailer1WheelsCB = findViewById(R.id.wheels_and_rims_one_cb);
        trailer1OthersCB = findViewById(R.id.other_one_cb);

        trailer2BreakConnectionsCB = findViewById(R.id.brake_connection_two_cb);
        trailer2BrakesCB = findViewById(R.id.brakes_two_cb);
        trailer2CouplingPinCB = findViewById(R.id.coupling_king_pin_two_cb);
        trailer2CouplingChainsCB = findViewById(R.id.coupling_chains_two_cb);
        trailer2DoorsCB = findViewById(R.id.doors_two_cb);
        trailer2HitchCB = findViewById(R.id.hitch_two_cb);
        trailer2LandingGearCB = findViewById(R.id.landing_gear_two_cb);
        trailer2LightsAllCB = findViewById(R.id.lights_all_two_cb);
        trailer2RoofCB = findViewById(R.id.roof_two_cb);
        trailer2SpringsCB = findViewById(R.id.spring_two_cb);
        trailer2TarpaulinCB = findViewById(R.id.tarpaulin_two_cb);
        trailer2TiresCB = findViewById(R.id.tires_two_cb);
        trailer2WheelsCB = findViewById(R.id.wheels_and_rims_two_cb);
        trailer2OthersCB = findViewById(R.id.other_two_cb);

        conditionVehicleIsSatisfactoryCB = findViewById(R.id.vehicleIsSatisfactory);
        aboveDefectsCorrectedCB = findViewById(R.id.aboveDefects);
        aboveDefectsNoCorrectionNeededCB = findViewById(R.id.aboveDefectsNotCorrected);

        mechanicSignature = findViewById(R.id.mechanic_signature_image);
        driverSignature = findViewById(R.id.driver_signature_image);
    }

    void setIdsFromActivity() {
        backIcon = findViewById(R.id.back_icon);
        email = findViewById(R.id.email);
    }


    void initialize() {
        if (email == null) {
            setIdsFromActivity();
        }
        email.setVisibility(View.VISIBLE);

        id = intentDataModel.getId();
        objectId = intentDataModel.getObjectId();
        objectType = intentDataModel.getObjectType();
        mobileRecordId = intentDataModel.getMobileRecordId();
        functionalGroupName = intentDataModel.getFunctionalGroupName();
        organizationName = intentDataModel.getOrganizationName();
        organizationNumber = intentDataModel.getOrganizationNumber();
        dateTime = intentDataModel.getDateTime();
        recordId = intentDataModel.getRecordId();
        vehicleLicenseNumber = intentDataModel.getVehicleLicenseNumber();
        airCompressor = intentDataModel.getAirCompressor();
        airLines = intentDataModel.getAirLines();
        battery = intentDataModel.getBattery();
        brakeAccessories = intentDataModel.getBrakeAccessories();
        brakes = intentDataModel.getBrakes();
        carburetor = intentDataModel.getCarburetor();
        clutch = intentDataModel.getClutch();
        defroster = intentDataModel.getDefroster();
        driveLine = intentDataModel.getDriveLine();
        fifthWheel = intentDataModel.getFifthWheel();
        frontalAxle = intentDataModel.getFrontalAxle();
        fuelTanks = intentDataModel.getFuelTanks();
        heater = intentDataModel.getHeater();
        horn = intentDataModel.getHorn();
        lights = intentDataModel.getLights();
        mirrors = intentDataModel.getMirrors();
        oilPressure = intentDataModel.getOilPressure();
        onBoardRecorder = intentDataModel.getOnBoardRecorder();
        radiator = intentDataModel.getRadiator();
        rearEnd = intentDataModel.getRearEnd();
        reflectors = intentDataModel.getReflectors();
        safetyEquipment = intentDataModel.getSafetyEquipment();
        springs = intentDataModel.getSprings();
        starter = intentDataModel.getStarter();
        steering = intentDataModel.getSteering();
        tachograph = intentDataModel.getTachograph();
        tires = intentDataModel.getTires();
        transmission = intentDataModel.getTransmission();
        wheels = intentDataModel.getWheels();
        windows = intentDataModel.getWindows();
        windShieldWipers = intentDataModel.getWindShieldWipers();
        others = intentDataModel.getOthers();
        trailer1 = intentDataModel.getTrailer1();
        trailer1BreakConnections = intentDataModel.getTrailer1BreakConnections();
        trailer1Breaks = intentDataModel.getTrailer1Breaks();
        trailer1CouplingPin = intentDataModel.getTrailer1CouplingPin();
        trailer1CouplingChains = intentDataModel.getTrailer1CouplingChains();
        trailer1Doors = intentDataModel.getTrailer1Doors();
        trailer1Hitch = intentDataModel.getTrailer1Hitch();
        trailer1LandingGear = intentDataModel.getTrailer1LandingGear();
        trailer1LightsAll = intentDataModel.getTrailer1LightsAll();
        trailer1Roof = intentDataModel.getTrailer1Roof();
        trailer1Springs = intentDataModel.getTrailer1Springs();
        trailer1Tarpaulin = intentDataModel.getTrailer1Tarpaulin();
        trailer1Tires = intentDataModel.getTrailer1Tires();
        trailer1Wheels = intentDataModel.getTrailer1Wheels();
        trailer1Others = intentDataModel.getTrailer1Others();
        trailer2 = intentDataModel.getTrailer2();
        trailer2BreakConnections = intentDataModel.getTrailer2BreakConnections();
        trailer2Breaks = intentDataModel.getTrailer2Breaks();
        trailer2CouplingPin = intentDataModel.getTrailer2CouplingPin();
        trailer2CouplingChains = intentDataModel.getTrailer2CouplingChains();
        trailer2Doors = intentDataModel.getTrailer2Doors();
        trailer2Hitch = intentDataModel.getTrailer2Hitch();
        trailer2LandingGear = intentDataModel.getTrailer2LandingGear();
        trailer2LightsAll = intentDataModel.getTrailer2LightsAll();
        trailer2Roof = intentDataModel.getTrailer2Roof();
        trailer2Springs = intentDataModel.getTrailer2Springs();
        trailer2Tarpaulin = intentDataModel.getTrailer1();
        trailer2Tires = intentDataModel.getTrailer2Tires();
        trailer2Wheels = intentDataModel.getTrailer2Wheels();
        trailer2Others = intentDataModel.getTrailer2Others();
        remarks = intentDataModel.getRemarks();
        conditionVehicleIsSatisfactory = intentDataModel.getConditionVehicleIsSatisfactory();
        driversSignatureVehicleSatisfactory = intentDataModel.getDriversSignatureVehicleSatisfactory();
        aboveDefectsCorrected = intentDataModel.getAboveDefectsCorrected();
        aboveDefectsNoCorrectionNeeded = intentDataModel.getAboveDefectsNoCorrectionNeeded();
        mechanicSignatureDate = intentDataModel.getMechanicsSignatureDate();
        driversSignatureNoCorrectionNeeded = intentDataModel.getDriversSignatureNoCorrectionNeeded();
        driversSignatureNoCorrectionNeededDate = intentDataModel.getDriversSignatureNoCorrectionNeededDate();
        truckNumber = intentDataModel.getTruckNumber();
        carrier = intentDataModel.getCarrier();
        address = intentDataModel.getAddress();
        odometer = intentDataModel.getOdometer();
        mechanicFirstName = intentDataModel.getMechanicFirstName();
        mechanicLastName = intentDataModel.getMechanicLastName();
        mechanicRecordId = intentDataModel.getMechanicRecordId();
        trailer1ReeferHOS = intentDataModel.getTrailer1ReeferHOS();
        trailer2ReeferHOS = intentDataModel.getTrailer2ReeferHOS();
        registration = intentDataModel.getRegistration();
        insurance = intentDataModel.getInsurance();
        rmsTimestamp = intentDataModel.getRmsTimestamp();
        sent = intentDataModel.getSent();
        mechanicNameAndSignature = intentDataModel.getMechanicNameAndSignature();
        driverNameAndSignature = intentDataModel.getDriverNameAndSignature();
        mechanicSignatureBitmap = intentDataModel.getMechanicSignatureBitmap();
        driverSignatureBitmap = intentDataModel.getDriverSignatureBitmap();
        driverSignatureDate = intentDataModel.getDriverSignatureDate();

    }

    void setListener() {

        backIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        selectAll.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Log.d(TAG, "onCheckedChanged: isChecked: " + isChecked);
                setCheckboxes(isChecked);
            }
        });

        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                generatePDFFile(true);
                generatePDF();
            }
        });
    }

    void initializeFields() {
        Log.d(TAG, "initializeFields: ");
        setEditTextFields();
        setCheckboxes();
        setMechanicSignatureImages();

        setDetailEditable(isEditable);
    }

    void setEditTextFields() {

        organizationNameET.setText(intentDataModel.getOrganizationName());
        addressET.setText(intentDataModel.getAddress());

        String dateFromDateTime = UiUtils.getDateAfterSplittingDateAndTimeBySpace(intentDataModel.getDateTime());
        String timeFromDateTime = UiUtils.getTimeAfterSplittingDateAndTimeBySpace(intentDataModel.getDateTime());
        Log.d(TAG, "setEditTextFields: dateFromDateTime: " + dateFromDateTime + " timeFromDateTime: " + timeFromDateTime);

        String formattedDate = "";
        if (dateFromDateTime.contains("-")) {
            formattedDate = DateUtils.convertDateTime(dateFromDateTime, DateUtils.FORMAT_DATE_YYYY_MM_DD, DateUtils.FORMAT_DATE_MM_DD_YYYY);
        } else {
            formattedDate = dateFromDateTime;
        }
        dateET.setText(formattedDate);

        String formattedTime = timeFromDateTime.replace("00:00:00.000", "");
        timeET.setText(formattedTime);


        truckNumberET.setText(intentDataModel.getTruckNumber());
        odometerET.setText(intentDataModel.getOdometer());
        trailer1NumberET.setText(intentDataModel.getTrailer1());
        trailer1ReeferHOSET.setText(intentDataModel.getTrailer1ReeferHOS());
        trailer2NumberET.setText(intentDataModel.getTrailer2());
        trailer2ReeferHOSET.setText(intentDataModel.getTrailer2ReeferHOS());
        remarksET.setText(intentDataModel.getRemarks());
        driverNameET.setText(intentDataModel.getDriverNameAndSignature());
        mechanicNameET.setText(intentDataModel.getMechanicNameAndSignature());

        driverSignatureDate = intentDataModel.getDriverSignatureDate();
        if (driverSignatureDate != null) {
            driverSignatureDate = driverSignatureDate.replace("00:00:00.000", "");
        }
//        driverSignatureDateET.setText(driverSignatureDate);
        driverSignatureDateET.setText(formattedDate);

        mechanicSignatureDate = intentDataModel.getMechanicsSignatureDate();
        if (mechanicSignatureDate != null) {
            mechanicSignatureDate = mechanicSignatureDate.replace("00:00:00.000", "");
        }
//        mechanicSignatureDateET.setText(mechanicSignatureDate);
        mechanicSignatureDateET.setText(formattedDate);
    }

    boolean checkValueIsTrue(String value) {
        if (value == null) {
            return false;
        } else if (value.equalsIgnoreCase("1")) {
            return true;
        } else if (value.equalsIgnoreCase("0")) {
            return false;
        }
        return false;
    }

    void setCheckboxes() {
//    airCompressorCB, airLinesCB, batteryCB, brakeAccessoriesCB, brakesCB, carburetorCB, clutchCB, defrosterCB,
//            driveLineCB, fifthWheelCB, registrationCB, insuranceCB, frontalAxleCB,
//            fuelTanksCB, heaterCB, hornCB, lightsCB, mirrorsCB, oilPressureCB,
        airCompressorCB.setChecked(checkValueIsTrue(intentDataModel.getAirCompressor()));
        airLinesCB.setChecked(checkValueIsTrue(intentDataModel.getAirLines()));
        batteryCB.setChecked(checkValueIsTrue(intentDataModel.getBattery()));
        brakeAccessoriesCB.setChecked(checkValueIsTrue(intentDataModel.getBrakeAccessories()));
        brakesCB.setChecked(checkValueIsTrue(intentDataModel.getBrakes()));
        carburetorCB.setChecked(checkValueIsTrue(intentDataModel.getCarburetor()));
        clutchCB.setChecked(checkValueIsTrue(intentDataModel.getClutch()));
        defrosterCB.setChecked(checkValueIsTrue(intentDataModel.getDefroster()));
        driveLineCB.setChecked(checkValueIsTrue(intentDataModel.getDriveLine()));
        fifthWheelCB.setChecked(checkValueIsTrue(intentDataModel.getFifthWheel()));
        registrationCB.setChecked(checkValueIsTrue(intentDataModel.getRegistration()));
        insuranceCB.setChecked(checkValueIsTrue(intentDataModel.getInsurance()));
        frontalAxleCB.setChecked(checkValueIsTrue(intentDataModel.getFrontalAxle()));
        fuelTanksCB.setChecked(checkValueIsTrue(intentDataModel.getFuelTanks()));
        heaterCB.setChecked(checkValueIsTrue(intentDataModel.getHeater()));
        hornCB.setChecked(checkValueIsTrue(intentDataModel.getHorn()));
        lightsCB.setChecked(checkValueIsTrue(intentDataModel.getLights()));
        mirrorsCB.setChecked(checkValueIsTrue(intentDataModel.getMirrors()));
        oilPressureCB.setChecked(checkValueIsTrue(intentDataModel.getOilPressure()));
        onBoardRecorderCB.setChecked(checkValueIsTrue(intentDataModel.getOnBoardRecorder()));
        radiatorCB.setChecked(checkValueIsTrue(intentDataModel.getRadiator()));
        rearEndCB.setChecked(checkValueIsTrue(intentDataModel.getRearEnd()));
        reflectorsCB.setChecked(checkValueIsTrue(intentDataModel.getReflectors()));
        safetyEquipmentCB.setChecked(checkValueIsTrue(intentDataModel.getSafetyEquipment()));
        springsCB.setChecked(checkValueIsTrue(intentDataModel.getSprings()));
        starterCB.setChecked(checkValueIsTrue(intentDataModel.getStarter()));
        steeringCB.setChecked(checkValueIsTrue(intentDataModel.getSteering()));
        tachographCB.setChecked(checkValueIsTrue(intentDataModel.getTachograph()));
        tiresCB.setChecked(checkValueIsTrue(intentDataModel.getTires()));
        transmissionCB.setChecked(checkValueIsTrue(intentDataModel.getTransmission()));
        wheelsCB.setChecked(checkValueIsTrue(intentDataModel.getWheels()));
        windowsCB.setChecked(checkValueIsTrue(intentDataModel.getWindows()));
        windShieldWipersCB.setChecked(checkValueIsTrue(intentDataModel.getWindShieldWipers()));
        othersCB.setChecked(checkValueIsTrue(intentDataModel.getOthers()));
        trailer1BreakConnectionsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1BreakConnections()));
        trailer1BrakesCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Breaks()));
        trailer1CouplingPinCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1CouplingPin()));
        trailer1CouplingChainsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1CouplingChains()));
        trailer1DoorsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Doors()));
        trailer1HitchCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Hitch()));
        trailer1LandingGearCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1LandingGear()));
        trailer1LightsAllCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1LightsAll()));
        trailer1RoofCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Roof()));
        trailer1SpringsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Springs()));
        trailer1TarpaulinCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Tarpaulin()));
        trailer1TiresCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Tires()));
        trailer1WheelsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Wheels()));
        trailer1OthersCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer1Others()));
        trailer2BreakConnectionsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2BreakConnections()));
        trailer2BrakesCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Breaks()));
        trailer2CouplingPinCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2CouplingPin()));
        trailer2CouplingChainsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2CouplingChains()));
        trailer2DoorsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Doors()));
        trailer2HitchCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Hitch()));
        trailer2LandingGearCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2LandingGear()));
        trailer2LightsAllCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2LightsAll()));
        trailer2RoofCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Roof()));
        trailer2SpringsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Springs()));
        trailer2TarpaulinCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Tarpaulin()));
        trailer2TiresCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Tires()));
        trailer2WheelsCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Wheels()));
        trailer2OthersCB.setChecked(checkValueIsTrue(intentDataModel.getTrailer2Others()));
        conditionVehicleIsSatisfactoryCB.setChecked(checkValueIsTrue(intentDataModel.getConditionVehicleIsSatisfactory()));
        aboveDefectsCorrectedCB.setChecked(checkValueIsTrue(intentDataModel.getAboveDefectsCorrected()));
        aboveDefectsNoCorrectionNeededCB.setChecked(checkValueIsTrue(intentDataModel.getAboveDefectsNoCorrectionNeeded()));
    }

    void setCheckboxes(boolean check) {
        Log.d(TAG, "setCheckboxes: check: " + check);
        airCompressorCB.setChecked(check);
        airLinesCB.setChecked(check);
        batteryCB.setChecked(check);
        brakeAccessoriesCB.setChecked(check);
        brakesCB.setChecked(check);
        carburetorCB.setChecked(check);
        clutchCB.setChecked(check);
        defrosterCB.setChecked(check);
        driveLineCB.setChecked(check);
        fifthWheelCB.setChecked(check);
        registrationCB.setChecked(check);
        insuranceCB.setChecked(check);
        frontalAxleCB.setChecked(check);
        fuelTanksCB.setChecked(check);
        heaterCB.setChecked(check);
        hornCB.setChecked(check);
        lightsCB.setChecked(check);
        mirrorsCB.setChecked(check);
        oilPressureCB.setChecked(check);
        onBoardRecorderCB.setChecked(check);
        radiatorCB.setChecked(check);
        rearEndCB.setChecked(check);
        reflectorsCB.setChecked(check);
        safetyEquipmentCB.setChecked(check);
        springsCB.setChecked(check);
        starterCB.setChecked(check);
        steeringCB.setChecked(check);
        tachographCB.setChecked(check);
        tiresCB.setChecked(check);
        transmissionCB.setChecked(check);
        wheelsCB.setChecked(check);
        windowsCB.setChecked(check);
        windShieldWipersCB.setChecked(check);
        othersCB.setChecked(check);
        trailer1BreakConnectionsCB.setChecked(check);
        trailer1BrakesCB.setChecked(check);
        trailer1CouplingPinCB.setChecked(check);
        trailer1CouplingChainsCB.setChecked(check);
        trailer1DoorsCB.setChecked(check);
        trailer1HitchCB.setChecked(check);
        trailer1LandingGearCB.setChecked(check);
        trailer1LightsAllCB.setChecked(check);
        trailer1RoofCB.setChecked(check);
        trailer1SpringsCB.setChecked(check);
        trailer1TarpaulinCB.setChecked(check);
        trailer1TiresCB.setChecked(check);
        trailer1WheelsCB.setChecked(check);
        trailer1OthersCB.setChecked(check);
        trailer2BreakConnectionsCB.setChecked(check);
        trailer2BrakesCB.setChecked(check);
        trailer2CouplingPinCB.setChecked(check);
        trailer2CouplingChainsCB.setChecked(check);
        trailer2DoorsCB.setChecked(check);
        trailer2HitchCB.setChecked(check);
        trailer2LandingGearCB.setChecked(check);
        trailer2LightsAllCB.setChecked(check);
        trailer2RoofCB.setChecked(check);
        trailer2SpringsCB.setChecked(check);
        trailer2TarpaulinCB.setChecked(check);
        trailer2TiresCB.setChecked(check);
        trailer2WheelsCB.setChecked(check);
        trailer2OthersCB.setChecked(check);
        conditionVehicleIsSatisfactoryCB.setChecked(check);
        aboveDefectsCorrectedCB.setChecked(check);
        aboveDefectsNoCorrectionNeededCB.setChecked(check);
    }

    void setMechanicSignatureImages() {
//        mechanicSignature, driverSignature
//        Picasso.with(getContext()).load(intentDataModel.getMechanicSignatureBitmap()).into(mechanicSignature);
//        Picasso.with(getContext()).load(intentDataModel.getDriverSignatureBitmap()).into(driverSignature);
    }


    void generatePDF() {
        preTripScrollView.post(new Runnable() {
            @Override
            public void run() {
                //   This is where width would be different than 0
                Log.d(TAG, "initializeFields: run: ");

                int viewHeight = preTripScrollView.getChildAt(0).getHeight();
                int viewWidth = preTripScrollView.getChildAt(0).getWidth();
                Log.d(TAG, "initializeFields: viewHeight: " + viewHeight + " viewWidth: " + viewWidth);

                if (viewHeight <= 0 || viewWidth <= 0) {
                    viewHeight = preTripConstraintLayout.getChildAt(0).getHeight();
                    viewWidth = preTripConstraintLayout.getChildAt(0).getWidth();

                    Log.d(TAG, "initializeFields: viewHeight: " + viewHeight + " viewWidth: " + viewWidth);

                    if (viewHeight <= 0 || viewWidth <= 0) {
                        return;
                    }
                }

                bitScroll = getBitmapFromView(preTripScrollView, preTripScrollView.getChildAt(0).getHeight(), preTripScrollView.getChildAt(0).getWidth());
//        bitScroll = getBitmapFromView(preTripScrollView, preTripScrollView.getHeight(), preTripScrollView.getWidth());
//        bitScroll = getBitmapFromView(preTripScrollView, 1000, 400);
//        bitScroll = getBitmapFromView(preTripScrollView, 1000, 400);
                try {
                    saveImage(bitScroll);
                } catch (IOException e) {
                    Log.d(TAG, "initializeFields: ioException: " + e);
                }
            }
        });
    }

    //create bitmap from the ScrollView
    private Bitmap getBitmapFromView(View view, int height, int width) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        return bitmap;
    }

    public void saveImage(Bitmap bitmap) throws IOException {

        WindowManager wm = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        float hight = displaymetrics.heightPixels;
        float width = displaymetrics.widthPixels;

        int convertHighet = (int) hight, convertWidth = (int) width;

//        Resources mResources = getResources();
//        Bitmap bitmap = BitmapFactory.decodeResource(mResources, R.drawable.screenshot);

        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();


        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#ffffff"));
        canvas.drawPaint(paint);


        bitmap = Bitmap.createScaledBitmap(bitmap, bitmap.getWidth(), bitmap.getHeight(), true);

        paint.setColor(Color.BLUE);
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);


        String directory_path = "";
        Log.d(TAG, "copyAsset: dirPath: Build.VERSION.SDK_INT >= Build.VERSION_CODES.R: " + (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            directory_path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/RCOTruck";
        } else {
            directory_path = Environment.getExternalStorageDirectory() + "/RCOTruck";
        }
        Log.d(TAG, "copyAsset: dirPath: destinationDirPatch: " + directory_path);

        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 40, bytes);
        File f = new File(directory_path);
        if (!f.exists()) {
            Log.d(TAG, "createPdf: file: " + (!f.exists()));
            f.mkdirs();
            Log.d(TAG, "createPdf: file.mkdir: " + f);
        }
        String targetPdf = directory_path + "/PretripReport.pdf";
        Log.d(TAG, "createPdf: target: " + targetPdf);

        File filePath = new File(targetPdf);
        try {
            document.writeTo(new FileOutputStream(filePath));
            Log.d(TAG, "saveImage: filePath added: Done");

            attachEmailContent(filePath);

        } catch (IOException e) {
            Log.d(TAG, "createPdf: error: " + e);
            Toast.makeText(PreTripReport.this, "Something wrong: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        document.close();

    }

    void attachEmailContent(File fileAttachment) {
        String[] arToEmails = null;
        String subject = "DVIR Report " + DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_MM_DD_YYYY);
        String body = subject;

        Log.d(TAG, "onWriteFinished() About to call sendEmail().");
        sendEmail(PreTripReport.this, arToEmails, subject, body, fileAttachment);
        Log.d(TAG, "onWriteFinished() End. After calling sendEmail()");
    }

    public static void sendEmail(Context ctx, String[] arToEmails, String subject, String body, File fileAttachment) {
        try {
            Log.d(TAG, "sendEmail() called with: ctx = [" + ctx + "], arToEmails = ["
                    + arToEmails + "], subject = [" + subject + "], body = "
                    + body + "], fileAttachment = [" + fileAttachment + "], fileAttachment.getCanonicalPath()=" + fileAttachment.getCanonicalPath()
                    + ", fileAttachment.getAbsolutePath()=" + fileAttachment.getAbsolutePath()
                    + ", fileAttachment.length()=" + fileAttachment.length() // + ", bytecount=" + bytecount
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri uriAttachment = null;
        if (fileAttachment != null)
            uriAttachment = FileProvider.getUriForFile(ctx, "com.rco.rcotrucks", fileAttachment);

        Log.d(TAG, "sendEmail(), uriAttachment=" + uriAttachment);

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this// set the type to 'email'
        emailIntent.setType("message/rfc822");// add email(s) here to whom you want to send email
        if (arToEmails != null && arToEmails.length > 0)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arToEmails);
        else Log.d(TAG, "sendEmail() arToEmails is null or empty, not setting in Intent.");
        if (!StringUtils.isNullOrWhitespaces(subject))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        else Log.d(TAG, "sendEmail() subject is null or empty, not setting in Intent.");
        if (!StringUtils.isNullOrWhitespaces(body))
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        else Log.d(TAG, "sendEmail() body is null or empty, not setting in Intent.");

        if (uriAttachment != null)
            emailIntent.putExtra(Intent.EXTRA_STREAM, uriAttachment);
        else Log.d(TAG, "sendEmail() uriAttachment is null, not setting in Intent.");

        ComponentName componentName = emailIntent.resolveActivity(ctx.getPackageManager());
        Log.d(TAG, "sendEmail() componentName=" + componentName);

        if (componentName != null) {
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

            ctx.startActivity(emailIntent);
        } else {
            Log.d(TAG, "sendEmail(), ****** Error.  No suitable apps could be found for emailing.");
            UiUtils.showToast(ctx, "No suitable apps could be found for emailing.");
        }
    }


    void setDetailEditable(boolean isEditable) {

        organizationNameET.setEnabled(isEditable);
        addressET.setEnabled(isEditable);
        dateET.setEnabled(isEditable);
        timeET.setEnabled(isEditable);
        truckNumberET.setEnabled(isEditable);
        odometerET.setEnabled(isEditable);
        trailer1NumberET.setEnabled(isEditable);
        trailer1ReeferHOSET.setEnabled(isEditable);
        trailer2NumberET.setEnabled(isEditable);
        trailer2ReeferHOSET.setEnabled(isEditable);
        remarksET.setEnabled(isEditable);
        driverNameET.setEnabled(isEditable);
        mechanicNameET.setEnabled(isEditable);
        driverSignatureDateET.setEnabled(isEditable);
        mechanicSignatureDateET.setEnabled(isEditable);

        organizationNameET.setClickable(isEditable);
        addressET.setClickable(isEditable);
        dateET.setClickable(isEditable);
        timeET.setClickable(isEditable);
        truckNumberET.setClickable(isEditable);
        odometerET.setClickable(isEditable);
        trailer1NumberET.setClickable(isEditable);
        trailer1ReeferHOSET.setClickable(isEditable);
        trailer2NumberET.setClickable(isEditable);
        trailer2ReeferHOSET.setClickable(isEditable);
        remarksET.setClickable(isEditable);
        driverNameET.setClickable(isEditable);
        mechanicNameET.setClickable(isEditable);
        driverSignatureDateET.setClickable(isEditable);
        mechanicSignatureDateET.setClickable(isEditable);

//        Jan 25, 2022  -    I have talked with Dragos and he suggested that we should not have this select all option in preview because by default
//        all the options in pre trip check creation are enabled so hiding it
        if (isEditable) {
//            selectAll.setVisibility(View.VISIBLE);
            selectAll.setVisibility(View.GONE);
        } else {
            selectAll.setVisibility(View.GONE);
        }

        airCompressorCB.setClickable(isEditable);
        airLinesCB.setClickable(isEditable);
        batteryCB.setClickable(isEditable);
        brakeAccessoriesCB.setClickable(isEditable);
        brakesCB.setClickable(isEditable);
        carburetorCB.setClickable(isEditable);
        clutchCB.setClickable(isEditable);
        defrosterCB.setClickable(isEditable);
        driveLineCB.setClickable(isEditable);
        fifthWheelCB.setClickable(isEditable);
        registrationCB.setClickable(isEditable);
        insuranceCB.setClickable(isEditable);
        frontalAxleCB.setClickable(isEditable);
        fuelTanksCB.setClickable(isEditable);
        heaterCB.setClickable(isEditable);
        hornCB.setClickable(isEditable);
        lightsCB.setClickable(isEditable);
        mirrorsCB.setClickable(isEditable);
        oilPressureCB.setClickable(isEditable);
        onBoardRecorderCB.setClickable(isEditable);
        radiatorCB.setClickable(isEditable);
        rearEndCB.setClickable(isEditable);
        reflectorsCB.setClickable(isEditable);
        safetyEquipmentCB.setClickable(isEditable);
        springsCB.setClickable(isEditable);
        starterCB.setClickable(isEditable);
        steeringCB.setClickable(isEditable);
        tachographCB.setClickable(isEditable);
        tiresCB.setClickable(isEditable);
        transmissionCB.setClickable(isEditable);
        wheelsCB.setClickable(isEditable);
        windowsCB.setClickable(isEditable);
        windShieldWipersCB.setClickable(isEditable);
        othersCB.setClickable(isEditable);
        trailer1BreakConnectionsCB.setClickable(isEditable);
        trailer1BrakesCB.setClickable(isEditable);
        trailer1CouplingPinCB.setClickable(isEditable);
        trailer1CouplingChainsCB.setClickable(isEditable);
        trailer1DoorsCB.setClickable(isEditable);
        trailer1HitchCB.setClickable(isEditable);
        trailer1LandingGearCB.setClickable(isEditable);
        trailer1LightsAllCB.setClickable(isEditable);
        trailer1RoofCB.setClickable(isEditable);
        trailer1SpringsCB.setClickable(isEditable);
        trailer1TarpaulinCB.setClickable(isEditable);
        trailer1TiresCB.setClickable(isEditable);
        trailer1WheelsCB.setClickable(isEditable);
        trailer1OthersCB.setClickable(isEditable);
        trailer2BreakConnectionsCB.setClickable(isEditable);
        trailer2BrakesCB.setClickable(isEditable);
        trailer2CouplingPinCB.setClickable(isEditable);
        trailer2CouplingChainsCB.setClickable(isEditable);
        trailer2DoorsCB.setClickable(isEditable);
        trailer2HitchCB.setClickable(isEditable);
        trailer2LandingGearCB.setClickable(isEditable);
        trailer2LightsAllCB.setClickable(isEditable);
        trailer2RoofCB.setClickable(isEditable);
        trailer2SpringsCB.setClickable(isEditable);
        trailer2TarpaulinCB.setClickable(isEditable);
        trailer2TiresCB.setClickable(isEditable);
        trailer2WheelsCB.setClickable(isEditable);
        trailer2OthersCB.setClickable(isEditable);
        conditionVehicleIsSatisfactoryCB.setClickable(isEditable);
        aboveDefectsCorrectedCB.setClickable(isEditable);
        aboveDefectsNoCorrectionNeededCB.setClickable(isEditable);

    }

}