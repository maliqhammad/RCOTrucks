package com.rco.rcotrucks.activities.ifta;

import android.util.Log;

import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.HttpClient;
import com.rco.rcotrucks.utils.StringUtils;

import java.util.List;

public class RmsHelperIfta extends Rms {
    /**
     * 				setOperation(getVal(arstrColVals, ix++));
     * 				String flag = getVal(arstrColVals, ix++);
     * 				setObjectId(getVal(arstrColVals, ix++));
     * 				setObjectType(getVal(arstrColVals, ix++), holderObjectType);
     * 				setMobileRecordId(getVal(arstrColVals, ix++));
     * 				setFuncGroupName(getVal(arstrColVals, ix++));
     * 				setOrgName(getVal(arstrColVals, ix++));
     * 				setOrgNumber(getVal(arstrColVals, ix++));
     * 				dateTime = getVal(arstrColVals, ix++);
     * 				jurisdictionID = getVal(arstrColVals, ix++);
     * 				odometer = getVal(arstrColVals, ix++);
     * 				truckNumber = getVal(arstrColVals, ix++);
     * 				dOTNumber = getVal(arstrColVals, ix++);
     * 				firstName = getVal(arstrColVals, ix++);
     * 				lastName = getVal(arstrColVals, ix++);
     * 				employeeId = getVal(arstrColVals, ix++);
     * 				company = getVal(arstrColVals, ix++);
     * 				state = getVal(arstrColVals, ix++);
     * 				country = getVal(arstrColVals, ix++);
     * 				vehicleLicenseNumber = getVal(arstrColVals, ix++);
     * 				iFTAYesorNo = getVal(arstrColVals, ix++);
     * 				tripPermitYesorNo = getVal(arstrColVals, ix++);
     * 				miles = getVal(arstrColVals, ix++);
     * 				fuelCode = getVal(arstrColVals, ix++);
     * 				fuelType = getVal(arstrColVals, ix++);
     */

    private static final String TAG = "RmsHelperIfta";

    public static String setIftaEvents(List<IftaEvent> listHeaders) throws Exception {
        final String strThis = "setIftaEvents(), ";
        Log.d(TAG, strThis + "Start.");

        StringBuilder sbufPostBody = new StringBuilder();
        String sysTime = String.valueOf(System.currentTimeMillis());

//        final String operation = StringUtils.isNullOrWhitespaces(strFieldValue) ? "I" : "N"; // N means no update

//        strFieldValue = mapIftaHeader.get("MobileRecordId");
//        final String mobileRecordId = StringUtils.isNullOrWhitespaces(strFieldValue)
//                ? Rms.getMobileRecordId("TruckDVIRHeaderAndroid", Rms.getDeviceId(), Rms.getUsername(), true, sysTime)
//                : strFieldValue;

        final String mobileRecordIdTemplate = getMobileRecordId("IftaEvent",
                getDeviceId(), getUsername(), true, sysTime);

        // DVIR Header CSV row.
        sbufPostBody.append(serializeListAsCsvPost(listHeaders,
                new IPostParserList<List<IftaEvent>>() {
                    public String parse(final List<IftaEvent> listH, int ix) {
                        IftaEvent h = listH.get(ix);
                        String operation = "O"; // StringUtils.isNullOrWhitespaces(h.rcoObjectId) ? "I" : "U"; // I means force insert, U means force update, O means update or insert, D means delete, N means no update
                        if (h.getSentSyncStatus() == Cadp.SYNC_STATUS_MARKED_FOR_DELETE) operation = "D";

                        if (StringUtils.isNullOrWhitespaces(h.getMobileRecordId())) {
                            h.setMobileRecordId(mobileRecordIdTemplate + "." + ix);
                            if (!StringUtils.isNullOrWhitespaces(h.getObjectId()))
                                Log.d(TAG, strThis + "**** Error Assertion error, if " +
                                        "h.rcoMobileRecordId is null, also expect h.rcoObjectId to be null (never synced record)");
                        }

                        String row = serializeItemAsCsvPostLine(new String[] {
                                operation,
                                "H",
                                h.getObjectId(),
                                h.getObjectType(),
                                h.getMobileRecordId(), // mobileRecordId,           // MobileRecordId
                                "", 						                        // Functional Group Name
                                getOrgName(),					                        // Org name
                                getOrgNumber(),					                        // Org number
                                h.dateTime,
                                h.jurisdictionId,
                                String.valueOf(h.odometer),
                                h.truckNumber,
                                h.dotNumber,
                                h.firstName,
                                h.lastName,
                                h.employeeId,
                                h.company,
                                h.state,
                                h.country,
                                h.vehicleLicenseNumber,
                                h.iftaYesOrNo,
                                h.tripPermitYesOrNo,
                                String.valueOf(h.miles),
                                h.fuelCode,
                                h.fuelType,
                                h.isIftaTaxExemptRoadYesNo,
                                h.status,
                                String.valueOf(h.odometerStart),
                                String.valueOf(h.longitude),
                                String.valueOf(h.latitude),
                                String.valueOf(h.getLocalSysTime()),
                        });

                        return row;
                    }
                }));


        String postBody = sbufPostBody.toString();
        Log.d(BusinessRules.TAG, strThis + "postBody=" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();

        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setIftaEvents/" +
                getUsername() + "/" + getPassword(), "text/plain", "setIftaEventsAndroid." + dateStr + ".txt", postBody.getBytes(), false);

        Log.d(TAG, strThis + "End. response=" + response);

        return response;
    }

}
