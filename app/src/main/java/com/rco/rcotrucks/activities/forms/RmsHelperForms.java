package com.rco.rcotrucks.activities.forms;

import android.util.Log;

import com.rco.rcotrucks.businesslogic.rms.Evaluation;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.HttpClient;

import java.util.List;

public class RmsHelperForms extends Rms {
    private static final String TAG = "RmsHelperForms";

    public static String setANewEvaluationSPEForms(final List<Evaluation> evaluationsList) throws Exception {
        String strThis = "setANewEvaluationForms ";

        //StringBuilder sbufPostBody = new StringBuilder();
        String postBody = serializeListAsCsvPost(evaluationsList,
                new IPostParserList<List<Evaluation>>() {
                    public String parse(List<Evaluation> evaluationList, int ix) {

                        Evaluation evaluation = evaluationList.get(ix);
                        String row = serializeItemAsCsvPostLine(new String[]{
                                "O",
                                "H",
                                evaluation.LobjectId,
                                evaluation.objectType,
                                getMobileRecordId(evaluation.mobileRecordId),                                     // MobileRecordId
                                "",                                                // Functional Group Name
                                getOrgName(),                                            // Org name
                                getOrgNumber(),                                            // Org number
                                evaluation.Form_Date,
                                evaluation.Application_Type_Unilateral,
                                evaluation.Application_Type_Joint,
                                evaluation.Driver_First_Name,
                                evaluation.Driver_Last_Name,
                                evaluation.Driver_Maiden_Name,//middleName
                                evaluation.Driver_Maiden_Name,
                                evaluation.Date_of_Birth,
                                evaluation.Sex,
                                evaluation.Driver_Address,
                                evaluation.Driver_City,
                                evaluation.Driver_State,
                                evaluation.Driver_Zipcode,
                                evaluation.Driver_Home_Phone_Number,
                                evaluation.Driver_License_Number,
                                evaluation.Driver_License_State,
                                evaluation.Driver_Mobile_Phone_Number,
                                evaluation.Description_of_impairment_or_amputation,
                                evaluation.Type_of_prosthesis_worn,
                                evaluation.States_of_operation,
                                evaluation.Type_of_cargo,
                                evaluation.Average_period_of_driving_time,
                                evaluation.Type_of_operation,
                                evaluation.Number_of_years_driving_vehicle_type,
                                evaluation.Number_of_years_driving_all_vehicle_types,
                                evaluation.Vehicle_Type,
                                evaluation.Vehicle_Seating_Capacity,
                                evaluation.Vehicle_Make,
                                evaluation.Vehicle_Model,
                                evaluation.Vehicle_Year,
                                evaluation.Vehicle_Transmission_Type,
                                evaluation.Vehicle_Number_of_Forward_Speeds,
                                evaluation.Vehicle_Rear_Axle_Speed,
                                evaluation.Vehicle_Type_Brake_System,
                                evaluation.Vehicle_Type_Steering_System,
                                evaluation.Number_of_Trailers,
                                evaluation.Description_of_trailers,
                                evaluation.Vehicle_Modifications,
                                evaluation.Driver_Signature_Date,
                                evaluation.Instructor_First_Name,
                                evaluation.Instructor_Last_Name,
                                evaluation.Instructor_Driver_License_Number,
                                evaluation.Location

                        });

                        return row;
                    }
                });

        Log.e("Form", "" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();
        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/formsservice/setDriverSkillPerformanceEvaluations/" +
                Rms.getUsername() + "/" + Rms.getPassword(), "text/plain", "setDriverSkillPerformanceEvaluations." + dateStr + ".txt", postBody.getBytes());

        return response;
    }

    public static String setANewEvaluationDERForms(final List<Evaluation> evaluationsList) throws Exception {

        String postBody = serializeListAsCsvPost(evaluationsList,
                new IPostParserList<List<Evaluation>>() {
                    public String parse(List<Evaluation> evaluationList, int ix) {

                        Evaluation evaluation = evaluationList.get(ix);
                        String row = serializeItemAsCsvPostLine(new String[]{
                                "O",
                                "H",
                                evaluation.LobjectId,
                                evaluation.objectType,
                                getMobileRecordId(evaluation.mobileRecordId),                                     // MobileRecordId
                                "",                                                // Functional Group Name
                                getOrgName(),                                            // Org name
                                getOrgNumber(),                                            // Org number
                                evaluation.lastEmployerName,
                                evaluation.lastEmployerAddress,
                                evaluation.lastEmployerCity,
                                evaluation.lastEmployerState,
                                evaluation.lastEmployerZipcode,
                                evaluation.lastEmployerTelephoneNumber,
                                evaluation.lastEmployerReasonsforleaving,
                                evaluation.secondLastEmployerName,
                                evaluation.secondLastEmployerAddress,
                                evaluation.secondLastEmployerCity,
                                evaluation.secondLastEmployerState,
                                evaluation.secondLastEmployerZipcode,
                                evaluation.secondLastEmployerTelephoneNumber,
                                evaluation.secondLastEmployerReasonsforleaving,
                                evaluation.thirdLastEmployerName,
                                evaluation.thirdLastEmployerAddress,
                                evaluation.thirdLastEmployerCity,
                                evaluation.thirdLastEmployerState,
                                evaluation.thirdLastEmployerZipcode,
                                evaluation.thirdLastEmployerTelephoneNumber,
                                evaluation.thirdLastEmployerReasonsforleaving,
                                evaluation.Creation_Date,
                                evaluation.Driver_First_Name,
                                evaluation.Driver_Last_Name,
                                evaluation.Driver_Mobile_Phone_Number,
                                evaluation.Driver_License_Number,
                                evaluation.positionHeld1,
                                evaluation.positionHeld2,
                                evaluation.positionHeld3,
                                evaluation.lastEmployerFromDate,
                                evaluation.lastEmployerToDate,
                                evaluation.secondLastEmployerFromDate,
                                evaluation.secondLastEmployerToDate,
                                evaluation.thirdLastEmployerFromDate,
                                evaluation.thirdLastEmployerToDate

                        });

                        return row;
                    }
                });

        Log.e("Form", "" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();
        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/formsservice/setDriverEmploymentRecords/" +
                Rms.getUsername() + "/" + Rms.getPassword(), "text/plain", "setDriverEmploymentRecords." + dateStr + ".txt", postBody.getBytes());

        return response;
    }

    public static String setANewEvaluationDMForms(final List<Evaluation> evaluationsList) throws Exception {

        String postBody = serializeListAsCsvPost(evaluationsList,
                new IPostParserList<List<Evaluation>>() {
                    public String parse(List<Evaluation> evaluationList, int ix) {

                        Evaluation evaluation = evaluationList.get(ix);
                        String row = serializeItemAsCsvPostLine(new String[]{
                                "O",
                                "H",
                                evaluation.LobjectId,
                                evaluation.objectType,
                                getMobileRecordId(evaluation.mobileRecordId),                                     // MobileRecordId
                                "",                                                // Functional Group Name
                                getOrgName(),                                            // Org name
                                getOrgNumber(),                                            // Org number
                                evaluation.Creation_Date,
                                evaluation.carrierName,
                                evaluation.doctorsName,
                                evaluation.sPEApplicantName,
                                evaluation.vehicleTypeStraightTruck,
                                evaluation.vehicleTypeTruckTrailerover10klbs,
                                evaluation.vehicleTypeTrucklessthan10klbsandhazardousmaterials,
                                evaluation.vehicleTypeTruckover10klbs,
                                evaluation.vehicleTypeMotorHome10klbs,
                                evaluation.vehicleTypeTractorTrailer,
                                evaluation.vehicleTypePassengerVehicle,
                                evaluation.vehicleTypePassengerSeatingCapacity,
                                evaluation.vehicleTypePassengerMotorCoach,
                                evaluation.vehicleTypePassengerBus,
                                evaluation.vehicleTypePassengerVan,
                                evaluation.vehicleTypeshortrelaydrives,
                                evaluation.vehicleTypelongrelaydrives,
                                evaluation.vehicleTypestraightthrough,
                                evaluation.vehicleTypenightsawayfromhome,
                                evaluation.vehicleTypesleeperteamdrives,
                                evaluation.vehicleTypenumberofnightsawayfromhome,
                                evaluation.vehicleTypeclimbinginandoutoftruck,
                                evaluation.environmentalFactorsabruptduty,
                                evaluation.environmentalFactorssleepdeprivation,
                                evaluation.environmentalFactorsunbalancedwork,
                                evaluation.environmentalFactorstemperature,
                                evaluation.environmentalFactorslongtrips,
                                evaluation.environmentalFactorsshortnotice,
                                evaluation.environmentalFactorstightdelivery,
                                evaluation.environmentalFactorsdelayenroute,
                                evaluation.environmentalFactorsothers,
                                evaluation.physicalDemandGearShifting,
                                evaluation.physicalDemandNumberspeedtransmission,
                                evaluation.physicalDemandsemiautomatic,
                                evaluation.physicalDemandfullyautomatic,
                                evaluation.physicalDemandsteeringwheelcontrol,
                                evaluation.physicalDemandbrakeacceleratoroperation,
                                evaluation.physicalDemandvarioustasks,
                                evaluation.physicalDemandbackingandparking,
                                evaluation.physicalDemandvehicleinspections,
                                evaluation.physicalDemandcargohandling,
                                evaluation.physicalDemandcoupling,
                                evaluation.physicalDemandchangingtires,
                                evaluation.physicalDemandvehiclemodifications,
                                evaluation.physicalDemandvehiclemodnotes,
                                evaluation.muscleStrengthyesno,
                                evaluation.muscleStrengthrightupperextremity,
                                evaluation.muscleStrengthleftupperextremity,
                                evaluation.muscleStrengthrightlowerextremity,
                                evaluation.muscleStrengthleftlowerextremity,
                                evaluation.mobilityyesno,
                                evaluation.mobilityrightupperextremity,
                                evaluation.mobilityleftupperextremity,
                                evaluation.mobilityrightlowerextremity,
                                evaluation.mobilityleftlowerextremity,
                                evaluation.mobilitytrunk,
                                evaluation.stabilityyesno,
                                evaluation.stabilityrightupperextremity,
                                evaluation.stabilityleftupperextremity,
                                evaluation.stabilityrightlowerextremity,
                                evaluation.stabilityleftlowerextremity,
                                evaluation.stabilitytrunk,
                                evaluation.impairmenthand,
                                evaluation.impairmentupperlimb,
                                evaluation.amputationhand,
                                evaluation.amputationpartial,
                                evaluation.amputationfull,
                                evaluation.amputationupperlimb,
                                evaluation.powergriprightyesno,
                                evaluation.powergripleftyesno,
                                evaluation.surgicalreconstructionyesno,
                                evaluation.hasupperimpairment,
                                evaluation.haslowerlimbimpairment,
                                evaluation.hasrightimpairment,
                                evaluation.hasleftimpairment,
                                evaluation.hasupperamputation,
                                evaluation.haslowerlimbamputation,
                                evaluation.hasrightamputation,
                                evaluation.hasleftamputation,
                                evaluation.appropriateprosthesisyesno,
                                evaluation.appropriateterminaldeviceyesno,
                                evaluation.prosthesisfitsyesno,
                                evaluation.useprostheticproficientlyyesno,
                                evaluation.abilitytopowergraspyesno,
                                evaluation.prostheticrecommendations,
                                evaluation.prostheticclinicaldescription,
                                evaluation.medicalconditionsinterferewithtasksyesno,
                                evaluation.medicalconditionsinterferewithtasksexplanation,
                                evaluation.medicalfindingsandevaluation,
                                evaluation.physicianlastname,
                                evaluation.physicianfirstname,
                                evaluation.physicianmiddlename,
                                evaluation.physicianaddress,
                                evaluation.physiciancity,
                                evaluation.physicianstate,
                                evaluation.physicianzipcode,
                                evaluation.physiciantelephonenumber,
                                evaluation.physicianalternatenumber,
                                evaluation.physiatrist,
                                evaluation.orthopedicsurgeon,
                                evaluation.boardCertifiedyesno,
                                evaluation.boardEligibleyesno,
                                evaluation.physiciandate,
                                evaluation.Driver_First_Name,
                                evaluation.Driver_Last_Name,
                                evaluation.Driver_Mobile_Phone_Number,
                                evaluation.Driver_License_Number,
                                evaluation.vehicleTypeslocaldeliveries,
                                evaluation.physicalDemandmountingsnowchains

                        });

                        return row;
                    }
                });

        Log.e("Form", "" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();
        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/formsservice/setDriverMedicalForms/" +
                Rms.getUsername() + "/" + Rms.getPassword(), "text/plain", "setDriverMedicalForms." + dateStr + ".txt", postBody.getBytes());

        return response;
    }

    public static String setANewEvaluationDAForms(final List<Evaluation> evaluationsList) throws Exception {

        StringBuilder sbufPostBody = new StringBuilder();
        String header = serializeListAsCsvPost(evaluationsList,
                new IPostParserList<List<Evaluation>>() {
                    public String parse(List<Evaluation> evaluationList, int ix) {

                        Evaluation evaluation = evaluationList.get(ix);
                        String row = serializeItemAsCsvPostLine(new String[]{
                                "O",
                                "H",
                                evaluation.LobjectId,
                                evaluation.objectType,
                                getMobileRecordId(evaluation.mobileRecordId),
                                "",                                                // Functional Group Name
                                getOrgName(),                                            // Org name
                                getOrgNumber(),
                                evaluation.companyName,
                                evaluation.companyAddress,
                                evaluation.companyCity,
                                evaluation.companyState,
                                evaluation.companyZipcode,
                                evaluation.firstName,
                                evaluation.lastName,
                                evaluation.middleName,
                                evaluation.licenseNumber_0,
                                evaluation.nameBirthDate,
                                evaluation.socialSercurityNumber,

                                evaluation.nameAddress,
                                evaluation.nameCity,
                                evaluation.nameState,
                                evaluation.nameZip,
                                evaluation.nameAddresshowLong,

                                evaluation.pastThreeYearsAddress2,
                                evaluation.pastThreeYearsCity2,
                                evaluation.pastThreeYearsState2,
                                evaluation.pastThreeYearsZipCode2,
                                evaluation.pastThreeYearsHowLong2,

                                evaluation.pastThreeYearsAddress3,
                                evaluation.pastThreeYearsCity3,
                                evaluation.pastThreeYearsState3,
                                evaluation.pastThreeYearsZipCode3,
                                evaluation.pastThreeYearsHowLong3,

                                evaluation.driverLicensePermitDenied,
                                evaluation.driverLicensePermitRevokedorSuspended,
                                evaluation.driverLicensePermitNotes,
                                evaluation.Creation_Date
                        });

                        return row;
                    }
                });
        sbufPostBody.append(header);
        sbufPostBody.append(CsvRowNeedle);

        String body = serializeListAsCsvPost(evaluationsList,
                new IPostParserList<List<Evaluation>>() {
                    public String parse(List<Evaluation> evaluationList, int ix) {

                        Evaluation evaluation = evaluationList.get(ix);
                        String row = serializeItemAsCsvPostLine(new String[]{
                                "O",
                                "H",
                                evaluation.LobjectId,
                                evaluation.objectType,
                                getMobileRecordId(evaluation.mobileRecordId),
                                "",                                                // Functional Group Name
                                getOrgName(),                                            // Org name
                                getOrgNumber(),
                                evaluation.pastThreeYearsAddress2,
                                evaluation.pastThreeYearsCity2,
                                evaluation.pastThreeYearsState2,
                                evaluation.pastThreeYearsZipCode2,
                                evaluation.pastThreeYearsHowLong2,

                                evaluation.pastThreeYearsAddress3,
                                evaluation.pastThreeYearsCity3,
                                evaluation.pastThreeYearsState3,
                                evaluation.pastThreeYearsZipCode3,
                                evaluation.pastThreeYearsHowLong3,
                                evaluation.driverName_0,
                                evaluation.state_0,
                                evaluation.licenseNumber_0,
                                evaluation.licenseType_0,
                                evaluation.expirationDate_0,
                                evaluation.classofEquipment_0,
                                evaluation.typeofEquipment_0,
                                evaluation.dateFrom_0,
                                evaluation.dateTo_0,
                                evaluation.approximateNumberofMiles_0,

                                evaluation.accidentDate_0,
                                evaluation.natureofAccident_0,
                                evaluation.fatalities_0,
                                evaluation.injuries_0,

                                evaluation.convictionLocation_0,
                                evaluation.convictionDate_0,
                                evaluation.charge_0,
                                evaluation.penalty_0,
                                evaluation.licenseType_0,
                                evaluation.Creation_Time
                        });

                        return row;
                    }
                });
        sbufPostBody.append(body);
        String postBody = sbufPostBody.toString();
        Log.e("Form", "" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();
        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/formsservice/setDriverApplications/" +
                Rms.getUsername() + "/" + Rms.getPassword(), "text/plain", "setDriverApplications." + dateStr + ".txt", postBody.getBytes());

        return response;
    }

    public static String setANewEvaluationDVForms(final List<Evaluation> evaluationsList) throws Exception {

        StringBuilder sbufPostBody = new StringBuilder();
        String header = serializeListAsCsvPost(evaluationsList,
                new IPostParserList<List<Evaluation>>() {
                    public String parse(List<Evaluation> evaluationList, int ix) {

                        Evaluation evaluation = evaluationList.get(ix);
                        String row = serializeItemAsCsvPostLine(new String[]{
                                "O",
                                "H",
                                evaluation.LobjectId,
                                evaluation.objectType,
                                getMobileRecordId(evaluation.mobileRecordId),
                                "",                                                // Functional Group Name
                                getOrgName(),                                            // Org name
                                getOrgNumber(),
                                evaluation.Creation_Date,
                                evaluation.Driver_First_Name,
                                evaluation.middleName,
                                evaluation.Driver_Last_Name,
                                evaluation.socialSercurityNumber,
                                evaluation.employmentDate,
                                evaluation.terminalCity,
                                evaluation.terminalState,
                                evaluation.Driver_License_Number,
                                evaluation.Driver_License_State,
                                evaluation.driverLicenseExpirationDate,

                                evaluation.violationsthisyear,
                                evaluation.carrierName,
                                evaluation.carrierAddress,
                                evaluation.reviewedBy,
                                evaluation.reviewedDate,

                                evaluation.title
                        });

                        return row;
                    }
                });
        sbufPostBody.append(header);
        sbufPostBody.append(CsvRowNeedle);

        String body = serializeListAsCsvPost(evaluationsList,
                new IPostParserList<List<Evaluation>>() {
                    public String parse(List<Evaluation> evaluationList, int ix) {

                        Evaluation evaluation = evaluationList.get(ix);
                        String row = serializeItemAsCsvPostLine(new String[]{
                                "O",
                                "H",
                                evaluation.LobjectId,
                                evaluation.objectType,
                                getMobileRecordId(evaluation.mobileRecordId),
                                "",                                                // Functional Group Name
                                getOrgName(),                                            // Org name
                                getOrgNumber(),
                                evaluation.violationDate,
                                evaluation.violationOffense,
                                evaluation.violationLocation,
                                evaluation.violationtypeofvehicle,
                                "","","","","","","","","","","","",""
                        });

                        return row;
                    }
                });
        sbufPostBody.append(body);
        String postBody = sbufPostBody.toString();
        Log.e("Form", "" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();
        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/formsservice/setDriverViolations/" +
                Rms.getUsername() + "/" + Rms.getPassword(), "text/plain", "setDriverViolations." + dateStr + ".txt", postBody.getBytes());

        return response;
    }

}
