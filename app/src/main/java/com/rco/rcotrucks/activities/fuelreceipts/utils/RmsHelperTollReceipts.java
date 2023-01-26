package com.rco.rcotrucks.activities.fuelreceipts.utils;

import android.util.Log;

import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.HttpClient;
import com.rco.rcotrucks.utils.StringUtils;

import java.util.List;
import java.util.Map;

public class RmsHelperTollReceipts  extends Rms {
    private static final String TAG = RmsHelperTollReceipts.class.getSimpleName();

    /**
     * @param tollReceiptDtls
     * @return
     * @throws Exception
     */
    public static String setTollReceipts(List<BusHelperRmsCoding.RmsRecordCoding> tollReceiptDtls) throws Exception {
        String strThis = "setFuelReceipts, ";

        StringBuilder sbufPostBody = new StringBuilder();
        String sysTime = String.valueOf(System.currentTimeMillis());

        final String detailMobileRecordIdTemplate = getMobileRecordId(BusHelperFuelReceipts.MOBILERECORDID_PREFIX_FUEL_RECEIPT_DTL,
                getDeviceId(), getUsername(), true, sysTime);

        sbufPostBody.append(serializeListAsCsvPost(tollReceiptDtls,
                new IPostParserList<List<BusHelperRmsCoding.RmsRecordCoding>>() {
                    public String parse(final List<BusHelperRmsCoding.RmsRecordCoding> listD, int ix) {
                        BusHelperRmsCoding.RmsRecordCoding rec = listD.get(ix);

                        Map<String, String> d = rec.getMapCoding();

                        final String objectId = d.get(Crms.KEY_OBJECT_ID);
                        final String objectType = d.get(Crms.KEY_OBJECT_TYPE);
//                        final String operation = StringUtils.isNullOrWhitespaces(objectId) ? "I" : "O";
                        String operation = (rec.getSentSyncStatus() == Cadp.SYNC_STATUS_MARKED_FOR_DELETE ? "D" : "O");
                        String strFieldValue = d.get(Crms.MOBILERECORDID);
                        final String mobileRecordId = StringUtils.isNullOrWhitespaces(strFieldValue)
                                ? detailMobileRecordIdTemplate + "." + ix
                                : strFieldValue;
//                        strFieldValue = d.get(Crms.DRIVER_RECORDID);
//                        if (StringUtils.isNullOrWhitespaces(strFieldValue))
//                            strFieldValue = BusinessRules.instance().getAuthenticatedUser().getRecordId();
//                        final String driverRecordId = strFieldValue;

                        String row = serializeItemAsCsvPostLine(new String[] {
                                operation,
                                "H",
                                objectId,
                                objectType,
                                mobileRecordId,                                     // MobileRecordId
                                "", 					                            // Functional Group Name
                                getOrgName(),					                        // Org name
                                getOrgNumber(),					                        // Org number
                                d.get(Crms.DATETIME),
                                d.get(Crms.FIRST_NAME),
                                d.get(Crms.LAST_NAME),
                                d.get(Crms.COMPANY),
                                d.get(Crms.TRUCK_NUMBER),
                                d.get(Crms.DOT_NUMBER),
                                d.get(Crms.VEHICLE_LICENSE_NUMBER),
                                d.get(Crms.VENDOR_NAME),
                                d.get(Crms.VENDOR_STATE),
                                d.get(Crms.VENDOR_COUNTRY),
                                d.get(Crms.TOTAL_AMOUNT),
                                d.get(Crms.USER_RECORD_ID),
                        });

                        return row;
                    }
                }));

        String postBody = sbufPostBody.toString();
        Log.d(TAG, strThis + "postBody=" + postBody);
        String dateStr = DateUtils.getIso8601NowDateStr();

        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTollReceipts/" +
                getUsername() + "/" + getPassword(), "text/plain", "setTollReceiptsAndroid." + dateStr + ".txt", postBody.getBytes(), false);
        Log.d(TAG, "save: setTollReceipts: response: "+response);
        return response;
    }

}
