package com.rco.rcotrucks.activities.dvir;

import android.util.Log;

import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.HttpClient;
import com.rco.rcotrucks.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class RmsHelperDvir extends Rms {
    private static final String TAG = "RmsHelperDvir";

    public static String setTruckDvirs(Map<String, String> mapDvirHeader, List<BusHelperRmsCoding.RmsRecordCoding> dvirDtls) throws Exception {
//        Jan 25, 2022  -   I think this method is somehow affecting our new method to send pending pre trip entries
//        so commented for now

        Log.d(TAG, "pretrip: csv driv: setTruckDvirs: ");
        String strThis = "setTruckDvirs(), ";

        StringBuilder sbufPostBody = new StringBuilder();
        String sysTime = String.valueOf(System.currentTimeMillis());

        String strFieldValue = mapDvirHeader.get(Crms.KEY_OBJECT_ID);
//        final String operation = StringUtils.isNullOrWhitespaces(strFieldValue) ? "I" : "N"; // N means no update
        final String operation = StringUtils.isNullOrWhitespaces(strFieldValue) ? "O" : "N"; // N means no update

//        strFieldValue = mapDvirHeader.get("MobileRecordId");
//        final String mobileRecordId = StringUtils.isNullOrWhitespaces(strFieldValue)
//                ? Rms.getMobileRecordId("TruckDVIRHeaderAndroid", Rms.getDeviceId(), Rms.getUsername(), true, sysTime)
//                : strFieldValue;
        List<Map<String, String>> listHeader = new ArrayList<>();
        listHeader.add(mapDvirHeader);

        final String detailMobileRecordIdTemplate = getMobileRecordId(BusHelperDvir.MOBILERECORDID_PREFIX_DVIR_DTL,
                getDeviceId(), getUsername(), true, sysTime);

        // DVIR Header CSV row.
        sbufPostBody.append(serializeListAsCsvPost(listHeader,
                new IPostParserList<List<Map<String, String>>>() {
                    public String parse(final List<Map<String, String>> listH, int ix) {
                        Map<String, String> h = listH.get(ix);

                        String row = serializeItemAsCsvPostLine(new String[] {
                                operation,
                                "H",
                                h.get(Crms.KEY_OBJECT_ID),
                                h.get(Crms.KEY_OBJECT_TYPE),
                                "", // mobileRecordId,           // MobileRecordId
                                "", 						                        // Functional Group Name
                                getOrgName(),					                        // Org name
                                getOrgNumber(),					                        // Org number
                                h.get("Driver First Name"),
                                h.get("Driver Last Name"),
                                h.get("DriverRecordId"),
                                h.get("DateTime")
                        });

                        return row;
                    }
                }));

        sbufPostBody.append(CsvRowNeedle);

        // DVIR Detail CSV rows.
        sbufPostBody.append(serializeListAsCsvPost(dvirDtls,
                new IPostParserList<List<BusHelperRmsCoding.RmsRecordCoding>>() {
                    public String parse(final List<BusHelperRmsCoding.RmsRecordCoding> listD, int ix) {
                        BusHelperRmsCoding.RmsRecordCoding rmsRecordDetail = listD.get(ix);

                        Map<String, String> d = rmsRecordDetail.getMapCoding();

                        final String objectId = d.get(Crms.KEY_OBJECT_ID);
                        final String objectType = d.get(Crms.KEY_OBJECT_TYPE);
                        Log.d(TAG, "pretrip: parse: objectId: "+objectId+" objectType: "+objectType);
//                        final String operation = StringUtils.isNullOrWhitespaces(objectId) ? "I" : "O";
                        int syncStatus = rmsRecordDetail.getSentSyncStatus();

                        Log.d(TAG, "pretrip: parse: syncStatus: "+syncStatus);
                        String operation = "O";  // If we always down sync after up sync, could use the logic to detect existing or new.
                        if (syncStatus == Cadp.SYNC_STATUS_MARKED_FOR_DELETE) operation = "D";

                        String strFieldValue = d.get(Crms.MOBILERECORDID);
                        final String mobileRecordId = StringUtils.isNullOrWhitespaces(strFieldValue)
                                ? detailMobileRecordIdTemplate + "." + ix
                                : strFieldValue;
                        strFieldValue = d.get(Crms.DRIVER_RECORDID);
                        if (StringUtils.isNullOrWhitespaces(strFieldValue)) strFieldValue = BusinessRules.instance().getAuthenticatedUser().getRecordId();
                        final String driverRecordId = strFieldValue;

                        Log.d(TAG, "pretrip: parse: driverRecordId: "+driverRecordId);
                        String row = serializeItemAsCsvPostLine(new String[] {
                                operation,
                                "D",
                                objectId,
                                objectType,
                                mobileRecordId,                                     // MobileRecordId
                                "", 					                            // Functional Group Name
                                getOrgName(),					                        // Org name
                                getOrgNumber(),					                        // Org number
                                d.get(Crms.DATETIME),
                                d.get(Crms.LOCATION),
                                d.get(Crms.DRIVER_FIRST_NAME),
                                d.get(Crms.DRIVER_LAST_NAME),
                                driverRecordId,
                                d.get(Crms.VEHICLE_LICENSE_NUMBER),
                                d.get(Crms.AIR_COMPRESSOR),
                                d.get(Crms.AIR_LINES),
                                d.get(Crms.BATTERY),
                                d.get(Crms.BRAKE_ACCESSORIES),
                                d.get(Crms.BRAKES),
                                d.get(Crms.CARBURETOR),
                                d.get(Crms.CLUTCH),
                                d.get(Crms.DEFROSTER),
                                d.get(Crms.DRIVE_LINE),
                                d.get(Crms.FIFTH_WHEEL),
                                d.get(Crms.FRONT_AXLE),
                                d.get(Crms.FUEL_TANKS),
                                d.get(Crms.HEATER),
                                d.get(Crms.HORN),
                                d.get(Crms.LIGHTS),
                                d.get(Crms.MIRRORS),
                                d.get(Crms.OIL_PRESSURE),
                                d.get(Crms.ON_BOARD_RECORDER),
                                d.get(Crms.RADIATOR),
                                d.get(Crms.REAR_END),
                                d.get(Crms.REFLECTORS),
                                d.get(Crms.SAFETY_EQUIPMENT),
                                d.get(Crms.SPRINGS),
                                d.get(Crms.STARTER),
                                d.get(Crms.STEERING),
                                d.get(Crms.TACHOGRAPH),
                                d.get(Crms.TIRES),
                                d.get(Crms.TRANSMISSION),
                                d.get(Crms.WHEELS),
                                d.get(Crms.WINDOWS),
                                d.get(Crms.WINDSHIELD_WIPERS),
                                d.get(Crms.OTHER),
                                d.get(Crms.TRAILER1_NUMBER),
                                d.get(Crms.TRAILER1_BRAKE_CONNECTIONS),
                                d.get(Crms.TRAILER1_BRAKES),
                                d.get(Crms.TRAILER1_COUPLING_KING_PIN),
                                d.get(Crms.TRAILER1_COUPLING_CHAINS),
                                d.get(Crms.TRAILER1_DOORS),
                                d.get(Crms.TRAILER1_HITCH),
                                d.get(Crms.TRAILER1_LANDING_GEAR),
                                d.get(Crms.TRAILER1_LIGHTS_ALL),
                                d.get(Crms.TRAILER1_ROOF),
                                d.get(Crms.TRAILER1_SPRINGS),
                                d.get(Crms.TRAILER1_TARPAULIN),
                                d.get(Crms.TRAILER1_TIRES),
                                d.get(Crms.TRAILER1_WHEELS),
                                d.get(Crms.TRAILER1_OTHER),
                                d.get(Crms.TRAILER2_NUMBER),
                                d.get(Crms.TRAILER2_BRAKE_CONNECTIONS),
                                d.get(Crms.TRAILER2_BRAKES),
                                d.get(Crms.TRAILER2_COUPLING_KING_PIN),
                                d.get(Crms.TRAILER2_COUPLING_CHAINS),
                                d.get(Crms.TRAILER2_DOORS),
                                d.get(Crms.TRAILER2_HITCH),
                                d.get(Crms.TRAILER2_LANDING_GEAR),
                                d.get(Crms.TRAILER2_LIGHTS_ALL),
                                d.get(Crms.TRAILER2_OTHER),
                                d.get(Crms.TRAILER2_ROOF),
                                d.get(Crms.TRAILER2_SPRINGS),
                                d.get(Crms.TRAILER2_TARPAULIN),
                                d.get(Crms.TRAILER2_TIRES),
                                d.get(Crms.TRAILER2_WHEELS),
                                d.get(Crms.REMARKS),
                                d.get(Crms.CONDITION_VEHICLE_SATISFACTORY),
                                d.get(Crms.DRIVERS_SIGNATURE_VEHICLE_SATISFACTORY),
                                d.get(Crms.ABOVE_DEFECTS_CORRECTED),
                                d.get(Crms.ABOVE_DEFECTS_NO_CORRECTIONS_NEEDED),
                                d.get(Crms.MECHANICS_SIGNATURE),
                                d.get(Crms.MECHANICS_SIGNATURE_DATE),
                                d.get(Crms.DRIVERS_SIGNATURE_NO_CORRECTIONS_NEEDED),
                                d.get(Crms.DRIVERS_SIGNATURE_NO_CORRECTIONS_NEEDED_DATE),
                                d.get(Crms.TRUCK_NUMBER),
                                d.get(Crms.LOGISTICS_CARRIER),
                                d.get(Crms.ADDRESS),
                                d.get(Crms.ODOMETER),
                                d.get(Crms.MECHANIC_FIRST_NAME),
                                d.get(Crms.MECHANIC_LAST_NAME),
                                d.get(Crms.MECHANIC_RECORDID),
                                d.get(Crms.TRAILER1_REEFER_HOS),
                                d.get(Crms.TRAILER2_REEFER_HOS),
                                d.get(Crms.REGISTRATION),
                                d.get(Crms.INSURANCE),
                        });

                        Log.d(TAG, "pretrip: parse: row: "+row);
                        return row;
                    }
                }));

        String postBody = sbufPostBody.toString();
        Log.d(BusinessRules.TAG, "pretrip: csv driv: "+strThis + "postBody=" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();

        Log.d(TAG, "pretrip: setTruckDvirs: url: "+getUrl()+"/Image2000/rest/shipservice/setTruckDvirs/" +
                getUsername() + "/" + getPassword());
        Log.d(TAG, "pretrip: setTruckDvirs: filename: setTruckDvirsAndroid." + dateStr + ".txt");

        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTruckDvirs/" +
                getUsername() + "/" + getPassword(), "text/plain", "setTruckDvirsAndroid." + dateStr + ".txt", postBody.getBytes(), false);
        Log.d(TAG, "pretrip: setTruckDvirs: response: "+response);


        return response;
    }

    public static String setSignatures(List<BusHelperRmsCoding.RmsRecordCoding> listHeader) throws Exception {
        String strThis = "setSignatures";

        StringBuilder sbufPostBody = new StringBuilder();
//        String sysTime = String.valueOf(System.currentTimeMillis());

//        setOperation(getVal(arstrColVals, ix++));
//        String flag = getVal(arstrColVals, ix++);
//        setObjectId(getVal(arstrColVals, ix++));
//        setObjectType(getVal(arstrColVals, ix++), holderObjectType);
//        setMobileRecordId(getVal(arstrColVals, ix++));
//        setFuncGroupName(getVal(arstrColVals, ix++));
//        setOrgName(getVal(arstrColVals, ix++));
//        setOrgNumber(getVal(arstrColVals, ix++));
//        documentTitle = getVal(arstrColVals, ix++);
//        issuingAuthority = getVal(arstrColVals, ix++);
//        expirationDate = getVal(arstrColVals, ix++);
//        signatureDate = getVal(arstrColVals, ix++);
//        signatureName = getVal(arstrColVals, ix++);
//        description = getVal(arstrColVals, ix++);
//        itemType = getVal(arstrColVals, ix++);
//        documentType = getVal(arstrColVals, ix++);
//        documentDate = getVal(arstrColVals, ix++);
//        reviewedBy = getVal(arstrColVals, ix++);
//        reviewedDate = getVal(arstrColVals, ix++);
//        parentObjectId = getVal(arstrColVals, ix++);
//        parentObjectType = getVal(arstrColVals, ix++);
        sbufPostBody.append(serializeListAsCsvPost(listHeader,
                new IPostParserList<List<BusHelperRmsCoding.RmsRecordCoding>>() {
                    public String parse(final List<BusHelperRmsCoding.RmsRecordCoding> listH, int ix) {
                        BusHelperRmsCoding.RmsRecordCoding rec = listH.get(ix);

                        Map<String, String> h = rec.getMapCoding();
                        String strFieldValue = h.get(Crms.KEY_OBJECT_ID);
                        String objectIdParent = h.get(Crms.PARENT_OBJECTID);
                        String objectTypeParent = h.get(Crms.PARENT_OBJECTTYPE);
                        if (StringUtils.isNullOrWhitespaces(objectIdParent) || StringUtils.isNullOrWhitespaces(objectTypeParent)) {
                            objectIdParent = rec.getObjectIdLinked();
                            objectTypeParent = rec.getObjectTypeLinked();
                        }

                        if (StringUtils.isNullOrWhitespaces(objectIdParent) || StringUtils.isNullOrWhitespaces(objectTypeParent)) {
                            Log.d(TAG, "setSignatures(), ***** Design error.  objectIdParent=" + objectIdParent
                                 + ", objectTypeParent=" + objectTypeParent + ", both should be non-empty.");
                        }

                        String operation = StringUtils.isNullOrWhitespaces(strFieldValue) ? "O" : "N"; // N means no update

                        String mobileRecordId = h.get(Crms.MOBILERECORDID);
                        if (StringUtils.isNullOrWhitespaces(mobileRecordId)) {
                            Log.d(TAG, "\n setSignatures() IPostParserList.parse() ****** Design error mobileRecordId is blank. h=" + h + "\n");
                        }

                        Log.d(TAG, "DOCUMENT_TITLE: parse: row: "+h.get(Crms.DOCUMENT_TITLE));
                        String row = serializeItemAsCsvPostLine(new String[] {
                                operation,
                                "H",
                                h.get(Crms.KEY_OBJECT_ID),
                                h.get(Crms.KEY_OBJECT_TYPE),
//                                "", // mobileRecordId,           // MobileRecordId
                                h.get(Crms.MOBILERECORDID),
                                "", 						                        // Functional Group Name
                                getOrgName(),					                        // Org name
                                getOrgNumber(),					                        // Org number
                                h.get(Crms.DOCUMENT_TITLE),
                                h.get(Crms.ISSUING_AUTHORITY),
                                h.get(Crms.EXPIRATION_DATE),
                                h.get(Crms.SIGNATURE_DATE),
                                h.get(Crms.SIGNATURE_NAME),
                                h.get(Crms.DESCRIPTION),
                                h.get(Crms.ITEMTYPE),
                                h.get(Crms.DOCUMENT_TYPE),
                                h.get(Crms.DOCUMENT_DATE),
                                h.get(Crms.REVIEWED_BY),
                                h.get(Crms.REVIEWED_DATE),
//                                h.get(Crms.PARENT_OBJECTID),
//                                h.get(Crms.PARENT_OBJECTTYPE),
                                objectIdParent,
                                objectTypeParent,
                        });

                        return row;
                    }
                }));

        sbufPostBody.append(CsvRowNeedle); // necessary?

        String postBody = sbufPostBody.toString();
        Log.d(TAG, strThis + "postBody=" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();

        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/formsservice/setSignatures/" +
                getUsername() + "/" + getPassword(), "text/plain", "setSignaturesAndroid." + dateStr + ".txt", postBody.getBytes(), false);

        return response;
    }

}
