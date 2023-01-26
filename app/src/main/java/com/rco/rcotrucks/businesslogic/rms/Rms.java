package com.rco.rcotrucks.businesslogic.rms;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.dvir.BusHelperDvir;
import com.rco.rcotrucks.activities.fuelreceipts.model.FuelReceiptModel;
import com.rco.rcotrucks.activities.fuelreceipts.model.TollReceiptModel;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.TruckEldContentLine;
import com.rco.rcotrucks.businesslogic.TruckLogContentLine;
import com.rco.rcotrucks.businesslogic.EldCycle;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.Pair;
import com.rco.rcotrucks.businesslogic.PairList;
import com.rco.rcotrucks.model.PretripModel;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.HttpClient;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

public class Rms {
    private static String TAG = Rms.class.getSimpleName();

    private static String url;
    private static String username;
    private static long userId;
    private static String password;
    private static String deviceId;

    public static String orgName;
    public static String orgNumber;
    protected BusinessRules rules = BusinessRules.instance();


    //    BASE URL
    public static String BASEURL = "https://www.rcofox.com/Image2000/rest/recordservice/";

    //    DELETE API
    public static String APIToDeleteRecord = BASEURL + "deleteRecord/";


    //region RMS calls

    //    public static String emailEld(String fromEmail, String toEmail, String subject, String body, String fromName, String fromRecordId) throws Exception {
    public static String emailEld(String fromEmail, String toEmail, String subject, String body, String fromName, String fromRecordId, String fileName, String fileContent) throws Exception {
        // https://www.rcolion.com/Image2000/rest/shipservice/emailELD/nick/nick/dragos.bodnar@gmail.com/fmcsaeldsub@dot.gov/
        // TEST:%20ELD%20records%20from%20SKDE:AGF001/testlion-03-02-2021-1/nick%20nick/1253068/

        String filteredUrl = Rms.getUrl() + "/Image2000/rest/shipservice/emailELD/"
                + Rms.getUsername() + "/" + Rms.getPassword() + "/" +
                fromEmail + "/" + toEmail + "/" + subject + "/" + body + "/" + fromName + "/" + fromRecordId + "/";

//        filteredUrl = "https://www.rcofox.com/Image2000/rest/shipservice/emailELD/dragosi/dragosi/dragos.bodnar@gmail.com/fmcsaeldsub@dot.gov/TEST:%20ELD%20records%20from%20SKDE:AGF001/test/Bod%20Dragos/1909158/" ;
//        filteredUrl = "https://www.rcofox.com/Image2000/rest/shipservice/emailELD/naeem/naeem/naeem.bajwax@gmail.com/fmcsaeldsub@dot.gov/TEST: ELD records from SKDE:AGF001/TextAgain/Naeem Android/1923785/" ;

//        https://www.rcofox.com/Image2000/rest/shipservice/emailELD/dragosi/dragosi/dragos.bodnar@gmail.com/fmcsaeldsub@dot.gov/TEST:%20ELD%20records%20from%20SKDE:AGF001/test/Bod%20Dragos/1909158/
//        https://www.rcofox.com/Image2000/rest/shipservice/emailELD/naeem/naeem/naeem.bajwax@gmail.com/fmcsaeldsub@dot.gov/TEST: ELD records from SKDE:AGF001/TextAgain/Naeem Android/1923785/
        Log.d(TAG, "transferEldFileViaEmail: emailEld: filteredUrl: " + filteredUrl);
        Log.d(TAG, "transferEldFileViaEmail: emailEld: body: " + body);
        Log.d(TAG, "transferEldFileViaEmail: emailEld: fileContent: before: " + fileContent);
        fileContent = fileContent.replace("null", "");
        Log.d(TAG, "transferEldFileViaEmail: emailEld: fileContent: after: " + fileContent);

        String response = HttpClient.postFile(filteredUrl, "text/plain", fileName, fileContent.getBytes(), false);
        Log.d(TAG, "transferEldFileViaEmail: emailEld: response: " + response);

//        return HttpClient.postFile(Rms.getUrl() + "/Image2000/rest/shipservice/emailELD/"
//                + Rms.getUsername() + "/" + Rms.getPassword() + "/" +
//                fromEmail + "/" + toEmail + "/" + subject + "/" + body + "/" + fromName + "/" + fromRecordId);


//        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/shipservice/emailELD/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" +
//                fromEmail + "/" + toEmail + "/" + subject + "/" + body + "/" + fromName + "/" + fromRecordId);

        return response;
    }


    public static String transferEldViaWebService(String comment, Boolean isTesting, String fileName, String fileContent) throws Exception {

        String testing = "true";
        if (!isTesting) {
            testing = "false";
        }

        String filteredUrl = Rms.getUrl() + "/Image2000/rest/shipservice/submitEldToWeb/"
                + Rms.getUsername() + "/" + Rms.getPassword() + "/" +
                comment + "/" + testing;

        String response = HttpClient.postFile(filteredUrl, "text/plain", fileName, fileContent.getBytes(), false);

        return response;
    }

    public static String getCityLocator(String latitude, String longitude) throws Exception {
        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/shipservice/getCityLocator/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" + latitude + "/" + longitude);
    }

    public static String getRecordsUpdatedFiltered(String recordType, String fullDataLimit, String maxTimestamp, String filterCodingFieldName, String filterCodingFieldValue) throws Exception {
        return HttpClient.get(url);
    }

    public static String getRecordsUpdatedXFiltered(String recordDisplayType, int maxNumberFullDataRecords, String filterFields, String strFieldDelim, String filterValues, String strValueDelim, String includeFields) throws IOException {
        String url = Rms.getUrl() + "/Image2000/rest/recordservice/getRecordsUpdatedXFiltered/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" + recordDisplayType + "/" + maxNumberFullDataRecords + "/+/+/+/+/" + filterFields + "/" + strFieldDelim + "/" + filterValues + "/" + strValueDelim + "/+/" + includeFields + "+/+/";
        Log.d(TAG, "syncDvirItems: getRecordsUpdatedXFiltered: url: " + url);
        return HttpClient.get(url);

    }

    //    Rms.getRecordsUpdatedXFiltered("Toll Receipt", -5000,
//            "Organization+Name,UserRecordId", ",", orgName + "," + userRecordId, ",", "", timeStamp);
    public static String getRecordsUpdatedXFiltered(String recordDisplayType, int maxNumberFullDataRecords, String filterFields, String strFieldDelim, String filterValues, String strValueDelim, String includeFields, String timeStamp) throws IOException {
        Log.d(TAG, "syncPretrip: syncTollRoadReceipt: getRecordsUpdatedXFiltered: recordDisplayType: " + recordDisplayType
                + " maxNumberFullDataRecords: " + maxNumberFullDataRecords + " filterFields: " + filterFields + " strFieldDelim: " + strFieldDelim +
                " strValueDelim" + strValueDelim + " includeFields: " + includeFields + " timeStamp: " + timeStamp);
        String url = Rms.getUrl() + "/Image2000/rest/recordservice/getRecordsUpdatedXFiltered/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" + recordDisplayType + "/" + maxNumberFullDataRecords + "/" + timeStamp + "/+/+/+/" + filterFields + "/" + strFieldDelim + "/" + filterValues + "/" + strValueDelim + "/+/" + includeFields + "+/+/";
        Log.d(TAG, "syncPretrip: getRecordsUpdatedXFiltered: url: " + url);
        return HttpClient.get(url);

    }

    public static String getMapBoxSpeed(LatLng startLocation, LatLng destination) throws IOException {
        String url = "https://api.mapbox.com/directions/v5/mapbox/driving/" + startLocation.longitude + "," + startLocation.latitude + ";" + destination.longitude + "," + destination.latitude + "?annotations=maxspeed&overview=full&geometries=geojson&access_token=pk.eyJ1IjoiZHJhZ29zYmIiLCJhIjoiY2tiZzR4YXM1MGlrcjJxa2M4M2t4c3ZldCJ9.-orOgLEYWRv2Hlpy4YJ3xg";
        Log.d(TAG, "Green: segment: getMapBoxSpeed: url: " + url);
        Log.d(TAG, "maxSpeed: getMapBoxSpeed: url: " + url);

        return HttpClient.get(url);
    }

    public static String getMapBoxSpeed(LatLng startLocation, LatLng destination, int stepPosition) throws IOException {
        String url = "https://api.mapbox.com/directions/v5/mapbox/driving/" + startLocation.longitude + "," + startLocation.latitude + ";" + destination.longitude + "," + destination.latitude + "?annotations=maxspeed&overview=full&geometries=geojson&access_token=pk.eyJ1IjoiZHJhZ29zYmIiLCJhIjoiY2tiZzR4YXM1MGlrcjJxa2M4M2t4c3ZldCJ9.-orOgLEYWRv2Hlpy4YJ3xg";
        Log.d(TAG, "Green: segment: getMapBoxSpeed: url: " + url);
        Log.d(TAG, "maxSpeed: getMapBoxSpeed: url: " + url + " stepPosition: " + stepPosition);

        return HttpClient.get(url);
    }

    public static String getTruckLocation() throws IOException {
        String url = Rms.getUrl() + "/Image2000/rest/recordservice/getRecordsUpdatedXFiltered/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" + "Truck/-10000/0/+/+/+/+/,/+/,/+/Latitude,Longitude,Truck Number,Speed";
        Log.i("trackLoc", url);

        return HttpClient.get(url);
    }

    public static String getRecordsUpdatedXFilteredRaw(String recordDisplayType, int maxNumberFullDataRecords, String maxTimestamp, String fromDate, String toDate, String dateRangeField, String filterFields, String strFieldDelim, String filterValues, String strValueDelim, String includeFields, boolean isReturnRawFields, boolean isIncludeFieldName) throws IOException {
        String strUrl = Rms.getUrl() + "/Image2000/rest/recordservice/getRecordsUpdatedXFiltered/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" + recordDisplayType + "/" + maxNumberFullDataRecords + "/" + maxTimestamp + "/" + fromDate + "/" + toDate + "/" + dateRangeField + "/" + filterFields + "/" + strFieldDelim + "/" + filterValues + "/" + strValueDelim + "/+/" + includeFields + "/" + isReturnRawFields + "/" + isIncludeFieldName + "/";
        Log.d(TAG, "getRecordsUpdatedXFilteredRaw() strUrl=" + strUrl);
        return HttpClient.get(strUrl);
    }

    public static String getUserRights() throws IOException {
        String strUrl = Rms.getUrl() + "/Image2000/rest/securityservice/getUserRights/" + Rms.getUsername() + "/" + Rms.getPassword();
        Log.d(TAG, "getUserRights() strUrl=" + strUrl);
        return HttpClient.get(strUrl);
    }

    public static String getUserInfoFull(String username, String password) throws IOException {
        String strUrl = Rms.getUrl() + "/Image2000/rest/userservice/getUserInfo/" + username + "/" + password;
        Log.d(TAG, "getUserInfoFull() strUrl=" + strUrl);
        return HttpClient.get(strUrl);
    }

    public static String getRecordTypeInfo(String recordTypesOptional) throws IOException {
        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/getRecordTypeInfo/"
                + Rms.getUsername() + "/" + Rms.getPassword() + "/" + recordTypesOptional + "/true");
    }

    //https://www.rcofox.com/Image2000/rest/recordservice/getRecordsUpdatedXFiltered/austin/austin/Driver%20Skill%20Performance%20Evaluation/-100/0/+/+/+/Driver%20License%20Number/,/7896297/,/+/+
    public static String getEvaluationList(String filterFields, String evaluationMaxNumber, String maxTimestamp) throws IOException {
        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/getRecordsUpdatedXFiltered/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" + filterFields + "/" + evaluationMaxNumber + "/" + maxTimestamp + "/+/+/+/" + "Driver License Number" + "/,/+/+/");
    }

    public static Bitmap getEfileBitmap(String objectType, String objectId, String vindex, String login, String passwd) throws IOException {
        if (StringUtils.isNullOrEmpty(vindex)) vindex = "-1";
        Bitmap bmp = null;
        InputStream inputStream = null;

        try {

            HttpURLConnection conn = HttpClient.getUrlConnection(
                    "https://www.rcofox.com/Image2000/ImgServer?objType=" + objectType
                            + "&id=" + objectId + "&vindex=" + vindex + "&login=" + login + "&passwd=" + passwd);

            inputStream = conn.getInputStream();

            BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

            bmp = BitmapFactory.decodeStream(bufferedInputStream);
        } catch (Throwable e) {
            Log.d(TAG, "getEfileBitMap() **** Error", e);
        } finally {
            inputStream.close();
        }

        return bmp;
    }

    public static byte[] getEfileBytes(String objectType, String objectId, String vindex, String login, String passwd) {
        String strThis = "getEfileBytes(), ";
        Log.d(TAG, strThis + "Start. objectType=" + objectType + ", objectId=" + objectId
                + ", vindex=" + vindex);

        if (StringUtils.isNullOrEmpty(vindex)) vindex = "-1";
        HttpURLConnection conn = null;
        InputStream inputStream = null;
        BufferedInputStream bufferedInputStream = null;
        byte[] arRetBytes = null;

        try {

            conn = HttpClient.getUrlConnection(
                    "https://www.rcofox.com/Image2000/ImgServer?objType=" + objectType
                            + "&id=" + objectId + "&vindex=" + vindex + "&login=" + login + "&passwd=" + passwd);

            inputStream = conn.getInputStream();

            bufferedInputStream = new BufferedInputStream(inputStream);

            arRetBytes = streamToByteArray(bufferedInputStream);
        } catch (java.io.FileNotFoundException ef) {
            Log.d(TAG, strThis + "**** Warning, no efile found for objectType=" + objectType
                    + ", objectId=" + objectId + ", vindex=" + vindex);
        } catch (Throwable e) {
            Log.d(TAG, strThis + "**** Error. ", e);
        } finally {
            try {
                if (bufferedInputStream != null) bufferedInputStream.close();
                if (inputStream != null) inputStream.close();
            } catch (Throwable e2) {
                Log.e(TAG, strThis + "**** Error trying to close inputstream. " + e2, e2);
            }

            try {
                if (conn != null) conn.disconnect();
            } catch (Throwable e3) {
                Log.e(TAG, strThis + "**** Error trying to disconnect conn. " + e3, e3);
            }
        }

        Log.d(TAG, strThis + "End. objectType=" + objectType + ", objectId=" + objectId
                + ", vindex=" + vindex + ", arRetBytes.length="
                + (arRetBytes != null ? arRetBytes.length : "(NULL)"));

        return arRetBytes;
    }

    public static String smsMessageToFunctionalGroup(String msg, String functionalGroupName) throws IOException {
        return HttpClient.get2(getUrl() + "/Image2000/rest/smsservice/smsMessageToFunctionalGroup/" + Rms.getUsername() + "/" +
                Rms.getPassword() + "/" + URLEncoder.encode(msg, "UTF-8") + "/" + functionalGroupName);
    }

    public static String sendSmsMessageToUser(String message, User u) throws IOException {
        return sendSmsMessageToUser(getUrl(), Rms.getUsername(), Rms.getPassword(),
                String.valueOf(u.getLobjectId()), u.getRecordId(), message);
    }

    public static String sendSmsMessageToUser(String protocolHostUrl, String login, String pw, String userObjectId, String userRecordId, String message) throws IOException {
        return HttpClient.get2(protocolHostUrl + "/Image2000/rest/smsservice/smsMessageToUser/" + login + "/" +
                pw + "/" + userObjectId + "/" + userRecordId + "/" + URLEncoder.encode(message, "UTF-8"));
    }

    public static String deleteRecord(ObjectInfo o) throws Exception {
        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/deleteRecord/" +
                Rms.getUsername() + "/" + Rms.getPassword() + "/" + o.getLobjectIdStr() + "/" + o.getObjectType());
    }

    public static String deleteRecord(String username, String password, String objectId, String objectType) throws Exception {
        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/deleteRecord/" +
                username + "/" + password + "/" + objectId + "/" + objectType);
    }

    public static HttpClient.HttpReturnInfo setRecordContent(String uploadFileName,
                                                             String objectId, String objectType, String version, String key, String mode, byte[] arRecordContent) throws Exception {
        String strThis = "setRecordContent";

        Log.d(TAG, strThis + "objectId=" + objectId + ", objectType=" + objectType
                + ", version=" + version + ", key (hidden), mode=" + mode + ", arRecordContent.length="
                + (arRecordContent != null ? arRecordContent.length : "(NULL)"));

        String url = getUrl() + "/Image2000/rest/recordservice/setRecordContent/" +
                getUsername() + "/" + getPassword() + "/" + HttpClient.blankEncode(objectId) + "/" + HttpClient.blankEncode(objectType)
                + "/" + HttpClient.blankEncode(key) + "/" + HttpClient.blankEncode(mode) + "/" + HttpClient.blankEncode(version);

        HttpClient.HttpReturnInfo response = HttpClient.postFileBytes(url,
                "application/octet-stream", uploadFileName, arRecordContent, false, 10 * 60 * 1000);

        return response;
    }

    /**
     * @param objectId
     * @param objectType
     * @param version    if "-1", deletes all versions.
     * @return
     * @throws Exception
     */
    public static HttpClient.HttpReturnInfo deleteFileVersion(String objectId, String objectType, String version) throws Exception {
        return HttpClient.getWithInfo(Rms.getUrl() + "/Image2000/rest/recordservice/deleteFileVersion/" +
                Rms.getUsername() + "/" + Rms.getPassword() + "/" + objectId + "/" + objectType + "/" + version);
    }

    public static String setLogMessages(final String topic, final String msg, final String status, final String username, final String firstName, final String lastName) throws Exception {
        final String nowStr = DateUtils.getNowYyyyMmDdHhmmss().replace("T", " ").replace("Z", "");

        ArrayList<ObjectInfo> csv = new ArrayList<>();
        csv.add(new ObjectInfo());

        String postBody = serializeListAsCsvPost(csv,
                new IPostParser<ObjectInfo>() {
                    public String parse(final ObjectInfo o) {
                        String row = serializeItemAsCsvPostLine(new String[]{"I", "H", "", "",
                                getMobileRecordId("LogMessage"),       // MobileRecordId
                                "",                                                // Functional Group Name
                                orgName,                                            // Org name
                                orgNumber,                                            // Org number
                                nowStr,                                             // DateTime
                                topic,                                              // Topic
                                msg,                                                // Message
                                "log-message",                                      // ItemType
                                status,                                             // Status
                                "Android",                                          // Origin
                                username,                                           // Login
                                firstName,                                          // First Name
                                lastName                                            // Last Name
                        });

                        return row;
                    }
                });

        String dateStr = DateUtils.getIso8601NowDateStr();

        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/auditservice/setLogMessages/" +
                Rms.getUsername() + "/" + Rms.getPassword(), "text/plain", "setLogMessages." + dateStr + ".txt", postBody.getBytes());

        return response;
    }

    public static String setTruckEldEvent(EldEvent eldEvent) throws Exception {
        ArrayList<EldEvent> eldEvents = new ArrayList();
        eldEvents.add(eldEvent);

        return setTruckEldEvents(eldEvents);
    }

    public static String setTruckEldEvents(ArrayList<EldEvent> eldEvents) throws Exception {
        String postBody = serializeListAsCsvPost(eldEvents,
                new IPostParser<EldEvent>() {
                    public String parse(final EldEvent e) {

                        DecimalFormat decimalFormat = new DecimalFormat("#.###");
                        String eventSeconds = decimalFormat.format(e.EventSeconds);
                        Log.d(TAG, "parse: eventSeconds: " + eventSeconds);
//                        June 10, 2022 -
//                        "I"   =>  Forcefully And  "O"   =>  insert with search
//                        We should use 'O' because when sending flag to '0' Randi is checking the mobile record id
//                        String row = serializeItemAsCsvPostLine(new String[]{"I", "H", "", "",
                        String row = serializeItemAsCsvPostLine(new String[]{"O", "H", "", "",
//                                June 10, 2022 -   We should use the mobile record id from local database
//                                getMobileRecordId("ELDEvent"),             // MobileRecordId
                                e.MobileRecordId,             // MobileRecordId
                                "",                                                    // Functional Group Name
                                orgName,                                                // Org name
                                orgNumber,                                                // Org number
//                                July 18, 2022 -   We should use the eld username that was used initially when it was generated
//                                And not use the current user
//                                username,                                               // ELD Username
                                e.EldUsername,                                               // ELD Username
                                e.EventType,                                            // Event Type
                                e.EventCode,                                            // Event Code
                                e.RecordStatus,                                         // Record Status
                                e.RecordOrigin,                                         // Record Origin
                                e.CreationDate,                                         // DateTime
                                e.TruckNumber,                                          // Truck Number
                                e.Vin,                                                  // VIN
                                e.LocalizationDescription,                              // Localization Description
                                e.LatitudeString,                                       // Latitude String
                                e.LongitudeString,                                      // Longitude String
                                e.DstSinceLastValidCoords,                              // Distance Since Last Valid Coordinates
                                e.VehicleMiles,                                         // Vehicle Miles
                                e.EngineHours,                                          // Engine Hours
                                e.OrderNumberCmv,                                       // Order Number CMV
                                e.OrderNumberUser,                                      // Order Number User
                                e.SequenceId,                                           // Sequence Id
                                e.EventCodeDescription,                                 // Event Code Description
                                e.DiagnosticIndicator,                                  // Diagnostic Indicator
                                e.MalfunctionIndicator,                                 // Malfunction Indicator
                                e.Annotation,                                           // Annotation
                                e.RecordOriginId,                                       // RecordOriginId
                                e.CheckData,                                            // CheckData
                                e.CheckSum,                                             // CheckSum
                                e.MalfunctionDiagnosticCode,                            // Malfunction Diagnostic Code
                                e.MalfunctionDiagnosticDescp,                           // Malfunction Diagnostic Description
                                e.DriverLastName,                                       // Driver Last Name
                                e.DriverFirstName,                                      // Driver First Name
                                e.DriverRecordId,                                       // DriverRecordId
                                e.EditReason,                                           // Edit Reason
                                eventSeconds,                                           // Event Seconds
                                e.ShiftStart,                                           // Shift Start
                                e.getOdometer()                                         // Odometer
                        });

                        return row;
                    }
                });

        DecimalFormat decimalFormat = new DecimalFormat("#.##");

        String dateStr = DateUtils.getIso8601NowDateStr();

        Log.e(TAG, "ELD Event Post Body: " + postBody);

        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTruckELDEvents/" +
                Rms.getUsername() + "/" + Rms.getPassword(), "text/plain", "setTruckELDEvents." + dateStr + ".txt", postBody.getBytes());

        Log.e(TAG, "ELD Event Response: " + response);

        return response;
    }

    public static String getRecordCodingSetup(String recordDisplayTypes) throws IOException {
        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/getRecordCodingSetup/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" + recordDisplayTypes);
    }

    public static String getCodingfieldMasterInfo(String recordDisplayTypes, String orderByOptional) throws IOException {
        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/getCodingfieldMasterInfo/" + Rms.getUsername() + "/" + Rms.getPassword() + "/" + recordDisplayTypes + "/" + StringUtils.nvl(orderByOptional, "+"));
    }

    public static ArrayList<String[]> setTruckLogs(TruckLogHeader truckLogHeader) throws Exception {
        ArrayList<TruckLogHeader> truckLogHeaders = new ArrayList();
        truckLogHeaders.add(truckLogHeader);

        return setTruckLogs(truckLogHeaders);
    }

    public static ArrayList<String[]> setTrailerLogs(TrailerLog trailerLog) throws Exception {
        ArrayList<TrailerLog> trailerLogs = new ArrayList();
        trailerLogs.add(trailerLog);

        return setTrailerLogs(trailerLogs);
    }

    public static ArrayList<String[]> setPretrip(Map<String, String> mapHeader, PretripModel pretripModel) throws Exception {
        Log.d(TAG, "save: setPretrip: pretripModel: objectId: "+pretripModel.getObjectId());
        ArrayList<PretripModel> pretripModelList = new ArrayList();
        pretripModelList.add(pretripModel);

        return setPretrip(mapHeader, pretripModelList);
    }


    public static ArrayList<String[]> setTollReceipts(TollReceiptModel tollReceiptModel) throws Exception {
        ArrayList<TollReceiptModel> tollReceiptModelList = new ArrayList();
        tollReceiptModelList.add(tollReceiptModel);

        return setTollReceipts(tollReceiptModelList);
    }


    public static ArrayList<String[]> setFuelReceipts(FuelReceiptModel fuelReceiptModel) throws Exception {
        ArrayList<FuelReceiptModel> fuelReceiptModelList = new ArrayList();
        fuelReceiptModelList.add(fuelReceiptModel);

        return setFuelReceipts(fuelReceiptModelList);
    }

    //    July 05, 2022 -   We need a function to return driving hours from rule name
//    getDrivingHoursFromRule("Alaska 7 days 70 hours");
    public static String getDrivingHoursFromRule(String rule) {
        String[] splitRule = rule.split("days");

        if (splitRule == null)
            return null;

        if (splitRule.length < 2) {
            return null;
        }

        String[] splitHours = splitRule[1].split("hours");
        if (splitHours == null || splitHours.length < 1)
            return null;

        return (splitHours[0].trim());

    }

    public static ArrayList<String[]> setTruckLogs(ArrayList<TruckLogHeader> truckLogHeaders) throws Exception {
        Log.d(TAG, "setTruckLogs: ");
        final String headerMobileRecordId = getMobileRecordId("TruckLogHeader", deviceId, username);

        String postBody = serializeListAsCsvPost(truckLogHeaders,
                new IPostParser<TruckLogHeader>() {
                    public String parse(final TruckLogHeader l) {

//                        July 05, 2022 -
                        String hoursString = getDrivingHoursFromRule(l.Rule);
                        Double hoursDouble = 0.0, hoursRemaining = 0.0;
                        if (hoursString != null && !hoursString.isEmpty()) {
                            hoursDouble = Double.parseDouble(hoursString);
                        }
                        Log.d(TAG, "parse: hours: hoursDouble: " + hoursDouble);

                        ArrayList<TruckLogDetail> list = l.getTruckLogDetails();
                        Double totalDrivingHours = 0.0;
                        for (int i = 0; i < list.size(); i++) {
                            TruckLogDetail truckLogDetail = list.get(i);
                            if (truckLogDetail.DrivingHours != null && !truckLogDetail.DrivingHours.isEmpty()) {
                                totalDrivingHours = totalDrivingHours +
                                        Double.parseDouble(truckLogDetail.DrivingHours);
                            }
                        }
                        Log.d(TAG, "parse: hours: loop ended: totalDrivingHours: " + totalDrivingHours);
                        if (hoursDouble > 0) {
                            hoursRemaining = hoursDouble - totalDrivingHours;
                        }
                        Log.d(TAG, "parse: hoursRemaining: " + hoursRemaining);


                        String row = serializeItemAsCsvPostLine(new String[]{"O", "H", l.objectId, l.objectType,
                                l.MobileRecordId != null ? l.MobileRecordId : headerMobileRecordId,     // MobileRecordId
                                "",                                                // Functional Group Name
                                orgName,                                            // Org name
                                orgNumber,                                            // Org number
                                l.OffDutyHours,                                     // Off Duty Hours
                                l.SleeperHours,                                     // Sleeper Hours
//                                    l.DrivingHours,                                     // Driving Hours
                                "" + totalDrivingHours,                                     // Driving Hours
                                l.OnDutyHours,                                      // On Duty Hours
                                l.HomeOfficeRecordId,                               // HomeOfficeRecordId
                                l.HomeOfficeName,                                   // Home Office Name
                                l.HomeOfficePhone,                                  // Home Office Phone
                                l.FirstName,                                        // First Name
                                l.LastName,                                         // Last Name
                                l.UserRecordId,                                     // UserRecordId
                                l.Year,                                             // Year
                                l.Driver,                                           // Driver
                                l.DriverRecordId,                                   // DriverRecordId
                                l.CoDriver,                                         // Co Driver
                                l.CoDriverRecordId,                                 // CoDriverRecordId
                                l.Rule,                                             // Rule
                                l.RuleDrivingDate,                                  // Rule Driving Days
                                l.Active,                                           // Active
                                l.TotalDistance,                                    // Total Distance
                                l.SpeedViolations,                                  // Speed Violations
                                l.GeofenceViolations,                               // Geofence Violations
                                l.StartDate,                                        // Start Date
                                l.StartTime,                                        // Start Time
                                l.EndDate,                                          // End Date
                                l.EndTime,                                          // End Time
                                "trucklogheader",                                   // ItemType
                                l.TripName,                                         // Trip Name
                                l.OverdueTimeLimit,                                 // Overdue Time Limit
                                l.RouteHeaderRecordId,                              // RouteHeaderRecordId
//                                l.HoursRemaining,                                   // HoursRemaining
                                "" + hoursRemaining,                                   // HoursRemaining
                                l.Weight,                                           // Weight
                                l.Lot,                                              // Lot
                                l.DriverStatus                                      // Driver Status
                        });

                        int counter = 0;


                        String lines = serializeListAsCsvPost(l.getTruckLogDetails(), new IPostParser<TruckLogDetail>() {

                            public String parse(TruckLogDetail e) {

                                Log.d(TAG, "postBody: parse: endDateTime: " + e.EndDateTime);
                                return serializeItemAsCsvPostLine(new String[]{"O", "D", e.objectId, e.objectType,
                                        e.MobileRecordId != null ? e.MobileRecordId : getMobileRecordId("TruckLogDetail", deviceId, username),   // MobileRecordId
                                        "",                                                                     // FunctionalGroupName
                                        orgName,                                                                // organizationName
                                        orgNumber,                                                              // organizationNumber
                                        e.DrivingHours,                                                         // Driving Hours
                                        e.ShiftHours,                                                           // Shift Hours
                                        e.Time,                                                                 // Time
                                        e.ShiftReset,                                                           // Shift Reset
                                        e.CycleHours,                                                           // Cycle Hours
                                        e.CycleType,                                                            // Cycle Type
                                        e.CycleReset,                                                           // Cycle Reset
                                        e.TimeZone,                                                             // Time Zone
                                        e.TruckNumber,                                                          // Vehicle License Number
                                        e.TruckType,                                                            // Truck Type
                                        e.VIN,                                                                  // VIN
                                        e.LoadDescription,                                                      // Load Description
                                        e.EventDescription,                                                     // Event Description
                                        e.EventDate,                                                            // Event Date
                                        e.EventStart,                                                           // Event Start
                                        e.EventDuration,                                                        // Event Duration
                                        e.EventStatus,                                                          // Event Status
                                        e.EventLocation,                                                        // Event Location
                                        e.EventNotes,                                                           // Event Notes
                                        e.Carrier,                                                              // Carrier
                                        e.Inspector,                                                            // Inspector
                                        e.InspectionNotes,                                                      // Inspection Notes
                                        e.CreationDate,                                                         // Start Date
                                        e.StartTime,                                                            // Start Time
                                        e.EndDateTime, //incorrect date format                                                         // End Date
//                                        "" + DateUtils.getNowYyyyMmDdHhmmss(),                                                          // End Date
                                        e.EndTime,                                                              // End Time
                                        e.OffDuty,                                                              // OffDuty
                                        e.Sleeper,                                                              // Sleeper
                                        e.Driving,                                                              // Driving
                                        e.OnDuty,                                                               // OnDuty
                                        e.Driver,                                                               // Driver
                                        e.CoDriver,                                                             // Co Driver
                                        e.EquipmentInfoNumbers,                                                 // Equipment Info Numbers
                                        e.CarrierName,                                                          // Carrier Name
                                        e.CarrierAddress,                                                       // Carrier Address
                                        e.Terminal,                                                             // Terminal
                                        e.Rule,                                                                 // Rule
                                        e.TotalMilesthisCycle,                                                  // Total Miles this Cycle
                                        e.Latitudes,                                                            // Latitudes
                                        e.Longitudes,                                                           // Longitudes
                                        e.LocationsDescriptions,                                                // Locations Descriptions
                                        e.TotalMilesToday,                                                      // Total Miles Today
                                        e.CoDriverRecordId,                                                     // CoDriverRecordId
                                        e.DriverRecordId,                                                       // DriverRecordId
                                        e.TotalDistance,                                                        // Total Distance
                                        e.SpeedViolations,                                                      // Speed Violations
                                        e.GeofenceViolations,                                                   // Geofence Violations
                                        e.ActiveDriver,                                                         // Active Driver
                                        e.ShipmentInfo,                                                         // Shipment Info
                                        "trucklogdetail",                                                       // ItemType
                                        e.Overdue,                                                              // Overdue
                                        e.FromLatitude,                                                         // From Latitude
                                        e.FromLongitude,                                                        // From Longitude
                                        e.ToLatitude,                                                           // To Latitude
                                        e.ToLongitude,                                                          // To Longitude
                                        e.DepartureTime,                                                        // Departure Time
                                        e.DepartureDate,                                                        // Departure Date
                                        e.TripNumber,                                                           // Trip Number
                                        e.RouteHeaderRecordId,                                                  // RouteHeaderRecordId
                                        l.StartDate,                                                            // CycleStartDateTime
                                        e.CycleEndDateTime,                                                     // CycleEndDateTime
                                        e.TruckNumber,                                                          // Truck Number
                                        e.OdometerStart,                                                        // Odometer Start
                                        e.OdometerEnd,                                                          // Odometer End
                                        e.EngineHours,                                                          // Engine Hours
                                        e.OriginType,                                                           // OriginType
                                        e.OnDutyBreak,                                                          // OnDutyBreak
                                        e.Trailer1Number,                                                       // Trailer1 Number 10.03.2022 set the values
                                        e.Trailer2Number                                                        // Trailer2 Number
                                });
                            }
                        });

                        Log.d(TAG, "parse: postBody: line: " + lines);
                        return row + (!TextUtils.isNullOrWhitespaces(lines) ? CsvRowNeedle + lines : "");
                    }
                });

        Log.d(TAG, "postBody: >>>" + postBody);

        String json = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTruckLogs/" + Rms.getUsername() + "/" + Rms.getPassword(),
                "text/plain", "setTruckLogs.txt", postBody.getBytes());
        Log.d(TAG, "setTruckLogs: test: postBody: " + postBody.getBytes());
        // Return objectIds/types for first header/detail relationships

        Log.d(TAG, "setTruckLogs: test: json: " + json);
        Log.d(TAG, "setTruckLogs: test: json: equals(): " + json.equalsIgnoreCase("[]"));
        if (json == null || json.equalsIgnoreCase("[]"))
            return null;

        TruckLogHeader truckLogHeader = truckLogHeaders.get(0);

        JSONArray response = new JSONArray(json);
        JSONObject header = response.getJSONObject(0);

        int lObjectId = header.getInt("LobjectId");
        String objectType = header.getString("objectType");

        ArrayList<String[]> result = new ArrayList<>();
        result.add(new String[]

                {
                        "0", "header", Integer.toString(lObjectId), objectType
                });

        JSONArray arDetailRecordDataMapped = header.getJSONArray("arDetailRecordDataMapped");
        ArrayList<TruckLogDetail> truckLogDetails = truckLogHeader.getTruckLogDetails();

        for (
                int i = 0; i < arDetailRecordDataMapped.length(); i++) {
            JSONObject detail = arDetailRecordDataMapped.getJSONObject(i);
            TruckLogDetail truckLogDetail = truckLogDetails.get(i);

            lObjectId = detail.getInt("LobjectId");
            objectType = detail.getString("objectType");

            result.add(new String[]{"0", "detail", Integer.toString(lObjectId), objectType});
        }

        return result;
    }


    public static ArrayList<String[]> setTrailerLogs(ArrayList<TrailerLog> trailerLogs) throws
            Exception {
        Log.d(TAG, "setTrailerLogs: ");
        final String headerMobileRecordId = getMobileRecordId("TrailerLog", deviceId, username);

        String postBody = serializeListAsCsvPost(trailerLogs,
                new IPostParser<TrailerLog>() {
                    public String parse(final TrailerLog l) {
                        String row = serializeItemAsCsvPostLine(new String[]{"O", "H", l.objectId, l.objectType,
                                l.MobileRecordId != null ? l.MobileRecordId : headerMobileRecordId,     // MobileRecordId
                                "",                                                // Functional Group Name
                                orgName,                                            // Org name
                                orgNumber,                                            // Org number
                                l.DateTime,                                     // Off Duty Hours
                                l.TruckNumber,                                     // Sleeper Hours
                                l.TrailerNumber,                                     // Driving Hours
                                l.ParentObjectId,                                      // On Duty Hours
                                l.ParentObjectType,                               // HomeOfficeRecordId
                                l.Action,                                           // Home Office Name
                                l.DriverRecordId                                  // Home Office Phone

                        });

                        return row;
                    }
                });

        Log.d(TAG, "setTrailerLogs: postBody: >>>" + postBody);

        String json = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTrailerLogs/" + Rms.getUsername() + "/" + Rms.getPassword(),
                "text/plain", "setTrailerLogs.txt", postBody.getBytes());
        Log.d(TAG, "setTrailerLogs: test: postBody: " + postBody.getBytes());
        // Return objectIds/types for first header/detail relationships

        Log.d(TAG, "setTrailerLogs: test: json: " + json);
        Log.d(TAG, "setTrailerLogs: test: json: equals(): " + json.equalsIgnoreCase("[]"));
        if (json == null || json.equalsIgnoreCase("[]"))
            return null;

        JSONArray response = new JSONArray(json);
        JSONObject header = response.getJSONObject(0);

        int lObjectId = header.getInt("LobjectId");
        String objectType = header.getString("objectType");

        ArrayList<String[]> result = new ArrayList<>();
        result.add(new String[]{"0", "header", Integer.toString(lObjectId), objectType});
        return result;
    }


    public static ArrayList<String[]> setPretrip(Map<String, String> mapHeader, ArrayList<PretripModel> pretripModelList) {
        Log.d(TAG, "save: setPretrip: ");

        Log.d(TAG, "save: setPretrip: mapHeader: "+mapHeader);

        StringBuilder sbufPostBody = new StringBuilder();
        String strFieldValue = pretripModelList.get(0).getObjectId();
        final String operation = StringUtils.isNullOrWhitespaces(strFieldValue) ? "O" : "N"; // N means no update

        List<Map<String, String>> listHeader = new ArrayList<>();
        listHeader.add(mapHeader);

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

        final String headerMobileRecordId = getMobileRecordId("pretrip", deviceId, username);
        Log.d(TAG, "save: setPretrip: headerMobileRecordId: " + headerMobileRecordId);
        String postBody = serializeListAsCsvPost(pretripModelList,
                new IPostParser<PretripModel>() {
                    public String parse(final PretripModel pretripModel) {
                        String mobileRecordId = (pretripModel.getMobileRecordId() != null && !pretripModel.getMobileRecordId().isEmpty()) ? pretripModel.getMobileRecordId() : headerMobileRecordId;
                        String driverRecordId = BusinessRules.instance().getAuthenticatedUser().getRecordId();

                        return getItemAsCsvPostLine("O", mobileRecordId, driverRecordId, pretripModel);
                    }
                });

        sbufPostBody.append(postBody);


        Log.d(TAG, "save: setPretrip: postBody: >>>" + postBody);
        Log.d(TAG, "save: setPretrip: postBody: bytes: >>>" + postBody.getBytes());

        Log.d(TAG, "save: setPretrip: postBody: >>> sbufPostBody: " + sbufPostBody);


//        setPretrip: mapHeader: {Driver First Name=Naeem, DriverRecordId=1923785, Driver Last Name=Android, DateTime=2023-01-24T21:19:31.289+0500}
//        setPretrip: headerMobileRecordId: pretrip-Android_1dbf1272eac8411c-naeem-ON-16745771712930
//        setPretrip: postBody: >>>"O","D","","","pretrip-Android_1dbf1272eac8411c-naeem-ON-16745771712930","","Henry Avocado","15","01/24/2023 09:17 PM","","","","1923785","","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","322","1","1","1","1","1","1","1","1","1","","1","1","1","1","329","1","1","1","1","1","1","1","1","1","1","","1","1","1","Working","1","","1","1","","","1","","4411","Henry Avocado","","7777","","","","32","29","1","1"
//        setPretrip: postBody: >>> sbufPostBody: "O","H","","","","","Henry Avocado","15","Naeem","Android","1923785","2023-01-24T21:19:31.289+0500"
//        "O","D","","","pretrip-Android_1dbf1272eac8411c-naeem-ON-16745771712930","","Henry Avocado","15","01/24/2023 09:17 PM","","","","1923785","","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","322","1","1","1","1","1","1","1","1","1","","1","1","1","1","329","1","1","1","1","1","1","1","1","1","1","","1","1","1","Working","1","","1","1","","","1","","4411","Henry Avocado","","7777","","","","32","29","1","1"


//        "O","H","","","01/24/2023 12:00:00 AM","Truck Drivers","Henry Avocado","15","Usman","Zafar","2063763","2023-01-24 21:24:39"
//        "O","D","","","Pretrip-Windows_9QJ6JR2CNCMK008AU0168-usman-ON-202301242124389720","Truck Drivers","Henry Avocado","15","2023-01-24 21:23:00","","Usman","Zafar","2063763","","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","0","1","1","1","1","1","1","1","1","1","1","1","1","1","1","0","1","1","1","1","1","1","1","1","1","1","1","1","1","1","afsfasdasd","1","","1","1","","2023-01-24 00:00:00","","2023-01-24 00:00:00","3435","new","test address","43","mechanic naes","","","45","46","1","1"

//        "O","H","","","","","Henry Avocado","15","Naeem","Android","1923785","2023-01-24T21:19:31.289+0500"
//        "O","D","","","pretrip-Android_1dbf1272eac8411c-naeem-ON-16745771712930","","Henry Avocado","15","01/24/2023 09:17 PM","","","","1923785","","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","1","322","1","1","1","1","1","1","1","1","1","","1","1","1","1","329","1","1","1","1","1","1","1","1","1","1","","1","1","1","Working","1","","1","1","","","1","","4411","Henry Avocado","","7777","","","","32","29","1","1"

        String dateStr = DateUtils.getIso8601NowDateStr();

//        String json = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTrailerLogs/" + Rms.getUsername() + "/" + Rms.getPassword(),
        String json = null;
//        try {
        Log.d(TAG, "save: setPretrip: check: url: " + getUrl());
        Log.d(TAG, "save: setPretrip: check: username: " + Rms.getUsername());
        Log.d(TAG, "save: setPretrip: check: password: " + Rms.getPassword());
        try {
            json = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTruckDvirs/" + Rms.getUsername() + "/" + Rms.getPassword(),
                    "text/plain", "setTruckDvirsAndroid" + dateStr + ".txt", sbufPostBody.toString().getBytes());
//                    "text/plain", "setTruckDvirsAndroid" + dateStr + ".txt", postBody.getBytes());
        } catch (Exception exception) {
            Log.d(TAG, "save: exception:  " + exception.getMessage());
        }
        Log.d(TAG, "save: setPretrip: json: " + json);

//        } catch (Exception exception) {
//            Log.d(TAG, "save: setPretrip: jsonException: " + exception.getMessage());
//        }
//        Log.d(TAG, "save: setPretrip: test: postBody: " + postBody.getBytes());
        Log.d(TAG, "save: setPretrip: test: postBody: " + sbufPostBody.toString().getBytes());
        // Return objectIds/types for first header/detail relationships

        Log.d(TAG, "save: setPretrip: test: json: " + json);
        if (json == null || json.equalsIgnoreCase("[]"))
            return null;

        JSONArray response = null;
        JSONObject header = null;
        int lObjectId = 0;
        String objectType = "", mobileRecordId = "", rmsCodingTimestamp = "";

        try {
            response = new JSONArray(json);
            header = response.getJSONObject(0);
            JSONObject mapCodingInfo = header.getJSONObject("mapCodingInfo");
            lObjectId = header.getInt("LobjectId");
            objectType = header.getString("objectType");

            if (mapCodingInfo.has("MobileRecordId")) {
                mobileRecordId = mapCodingInfo.getString("MobileRecordId");
            }
            if (mapCodingInfo.has("RMS Coding Timestamp")) {
                rmsCodingTimestamp = mapCodingInfo.getString("RMS Coding Timestamp");
            }

        } catch (JSONException jsonException) {
            Log.d(TAG, "save: setPretrip: response: " + response);
        }
        ArrayList<String[]> result = new ArrayList<>();
        Log.d(TAG, "save: setPretrip: result: " + result);
        result.add(new String[]{"0", "header", Integer.toString(lObjectId), objectType, mobileRecordId, rmsCodingTimestamp});
        return result;

    }

    static String getItemAsCsvPostLine(String operation, String mobileRecordId, String driverRecordId, PretripModel preTripModel) {

        String row = serializeItemAsCsvPostLine(new String[]{
                operation,
                "D",
                preTripModel.getObjectId(),
                preTripModel.getObjectType(),
                mobileRecordId,                                     // MobileRecordId
                "",                                                // Functional Group Name
                getOrgName(),                                            // Org name
                getOrgNumber(),                                            // Org number
                DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z),//preTripModel.getDateTime(),
                "",//getd.get(Crms.LOCATION),
                "TestDriverFirstName",//preTripModel.getDriverNameAndSignature(),//.get(Crms.DRIVER_FIRST_NAME),
                "TestDriverLastName",//preTripModel.getDriverNameAndSignature(),//.get(Crms.DRIVER_LAST_NAME),
                driverRecordId,
                "",                                 //.get(Crms.VEHICLE_LICENSE_NUMBER),
                preTripModel.getAirCompressor(),    //.get(Crms.AIR_COMPRESSOR),
                preTripModel.getAirLines(),         //d.get(Crms.AIR_LINES),
                preTripModel.getBattery(),
                preTripModel.getBrakeAccessories(),
                preTripModel.getBrakes(),
                preTripModel.getCarburetor(),
                preTripModel.getClutch(),
                preTripModel.getDefroster(),
                preTripModel.getDriveLine(),
                preTripModel.getFifthWheel(),
                preTripModel.getFrontalAxle(),
                preTripModel.getFuelTanks(),
                preTripModel.getHeater(),

                preTripModel.getHorn(),
                preTripModel.getLights(),
                preTripModel.getMirrors(),
                preTripModel.getOilPressure(),
                preTripModel.getOnBoardRecorder(),
                preTripModel.getRadiator(),
                preTripModel.getRearEnd(),
                preTripModel.getReflectors(),
                preTripModel.getSafetyEquipment(),
                preTripModel.getSprings(),
                preTripModel.getStarter(),
                preTripModel.getSteering(),
                preTripModel.getTachograph(),
                preTripModel.getTires(),
                preTripModel.getTransmission(),
                preTripModel.getWheels(),
                preTripModel.getWindows(),
                preTripModel.getWindShieldWipers(),
                preTripModel.getOthers(),

                preTripModel.getTrailer1(),
                preTripModel.getTrailer1BreakConnections(),
                preTripModel.getTrailer1Breaks(),
                preTripModel.getTrailer1CouplingPin(),
                preTripModel.getTrailer1CouplingChains(),
                preTripModel.getTrailer1Doors(),
                preTripModel.getTrailer1Hitch(),
                preTripModel.getTrailer1LandingGear(),
                preTripModel.getTrailer1LightsAll(),
                preTripModel.getTrailer1Others(),//New Compare
                preTripModel.getTrailer1Roof(),
                preTripModel.getTrailer1Springs(),
                preTripModel.getTrailer1Tarpaulin(),
                preTripModel.getTrailer1Tires(),
                preTripModel.getTrailer1Wheels(),
//                preTripModel.getTrailer1Others(),

                preTripModel.getTrailer2(),
                preTripModel.getTrailer2BreakConnections(),
                preTripModel.getTrailer2Breaks(),
                preTripModel.getTrailer2CouplingPin(),
                preTripModel.getTrailer2CouplingChains(),
                preTripModel.getTrailer2Doors(),
                preTripModel.getTrailer2Hitch(),
                preTripModel.getTrailer2LandingGear(),
                preTripModel.getTrailer2LightsAll(),
                preTripModel.getTrailer2Others(),//New Compare
                preTripModel.getTrailer2Roof(),
                preTripModel.getTrailer2Springs(),
                preTripModel.getTrailer2Tarpaulin(),
                preTripModel.getTrailer2Tires(),
                preTripModel.getTrailer2Wheels(),
                preTripModel.getRemarks(),

                preTripModel.getConditionVehicleIsSatisfactory(),
                "",//New Compare
                preTripModel.getAboveDefectsCorrected(),
                preTripModel.getAboveDefectsNoCorrectionNeeded(),
                "",//New Compare//   ""     preTripModel.getd.get(Crms.MECHANICS_SIGNATURE),
                "",//New Compare//preTripModel.getMechanicsSignatureDate(),
//                preTripModel.getAboveDefectsNoCorrectionNeeded(),
                preTripModel.getDriversSignatureNoCorrectionNeededDate(),
                "",//New Compare//preTripModel.getDriversSignatureNoCorrectionNeededDate(),
                preTripModel.getTruckNumber(),
                preTripModel.getOrganizationName(),
                preTripModel.getAddress(),
                preTripModel.getOdometer(),
                "TestMechanicFirstName",//preTripModel.getMechanicFirstName(),
                "TestMechanicLastName",//preTripModel.getMechanicLastName(),
                preTripModel.getMechanicRecordId(),
                preTripModel.getTrailer1ReeferHOS(),
                preTripModel.getTrailer2ReeferHOS(),
                preTripModel.getRegistration(),
                preTripModel.getInsurance()
//                preTripModel.getDriversSignatureVehicleSatisfactory(),

//                preTripModel.getConditionVehicleIsSatisfactory(),
//                preTripModel.getDriversSignatureVehicleSatisfactory(),
//                preTripModel.getAboveDefectsCorrected(),
//                preTripModel.getAboveDefectsNoCorrectionNeeded(),
//                "",//   ""     preTripModel.getd.get(Crms.MECHANICS_SIGNATURE),
//                preTripModel.getMechanicsSignatureDate(),
//                preTripModel.getAboveDefectsNoCorrectionNeeded(),
//                preTripModel.getDriversSignatureNoCorrectionNeededDate(),
//                preTripModel.getTruckNumber(),
//                preTripModel.getOrganizationName(),
//                preTripModel.getAddress(),
//                preTripModel.getOdometer(),
//                preTripModel.getMechanicFirstName(),
//                preTripModel.getMechanicLastName(),
//                preTripModel.getMechanicRecordId(),
//                preTripModel.getTrailer1ReeferHOS(),
//                preTripModel.getTrailer2ReeferHOS(),
//                preTripModel.getRegistration(),
//                preTripModel.getInsurance(),
        });

        return row;
    }

    public static ArrayList<String[]> setTollReceipts(ArrayList<TollReceiptModel> tollReceiptModelList) {
        Log.d(TAG, "save: setTollReceipts: ");
        final String headerMobileRecordId = getMobileRecordId("tollreceipt", deviceId, username);
        Log.d(TAG, "save: setTollReceipts: headerMobileRecordId: " + headerMobileRecordId);

        String postBody = serializeListAsCsvPost(tollReceiptModelList,
                new IPostParser<TollReceiptModel>() {
                    public String parse(final TollReceiptModel l) {
                        String row = serializeItemAsCsvPostLine(new String[]{"O", "H",
                                "" + l.getTollReceiptObjectId(),
                                l.getTollReceiptObjectType(),
                                (l.getTollReceiptMobileRecordId() != null && !l.getTollReceiptMobileRecordId().isEmpty()) ? l.getTollReceiptMobileRecordId() : headerMobileRecordId,     // MobileRecordId
                                "",                                                // Functional Group Name
                                orgName,                                            // Org name
                                orgNumber,                                            // Org number
                                l.getTollReceiptDateTime(),                                     // DateTime
                                l.getTollReceiptFirstName(),                                     // FirstName
                                l.getTollReceiptLastName(),                                     // LastName
                                l.getTollReceiptCompany(),                                      // Company
                                l.getTollReceiptTruckNumber(),                               // TruckNumber
                                l.getTollReceiptDotNumber(),                                // DotNumber
                                l.getTollReceiptVehicleLicenseNumber(),                     // Vehicle License Number
                                l.getTollReceiptVendorName(),                               // Vendor Name
                                l.getTollReceiptVendorState(),                              // Vendor State
                                l.getTollReceiptVendorCountry(),                            // Vendor Country
                                l.getTollReceiptAmount(),                                   // Amount
                                l.getTollReceiptUserRecordId(),                             // User Record Id
                                l.getTollReceiptRoadName()                                  // RoadName
                        });

                        return row;
                    }
                });

        Log.d(TAG, "save: setTollReceipts: postBody: >>>" + postBody);
        Log.d(TAG, "save: setTollReceipts: postBody: bytes: >>>" + postBody.getBytes());

//        String json = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTrailerLogs/" + Rms.getUsername() + "/" + Rms.getPassword(),
        String json = null;
//        try {
        Log.d(TAG, "save: setTollReceipts: check: url: " + getUrl());
        Log.d(TAG, "save: setTollReceipts: check: username: " + Rms.getUsername());
        Log.d(TAG, "save: setTollReceipts: check: password: " + Rms.getPassword());
        try {
            json = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTollReceipts/" + Rms.getUsername() + "/" + Rms.getPassword(),
                    "text/plain", "setTollReceipts.txt", postBody.getBytes());
        } catch (Exception exception) {
            Log.d(TAG, "save: exception:  " + exception.getMessage());
        }
        Log.d(TAG, "save: setTollReceipts: json: " + json);

//        } catch (Exception exception) {
//            Log.d(TAG, "save: setTollReceipts: jsonException: " + exception.getMessage());
//        }
        Log.d(TAG, "save: setTollReceipts: test: postBody: " + postBody.getBytes());
        // Return objectIds/types for first header/detail relationships

        Log.d(TAG, "save: setTollReceipts: test: json: " + json);
        if (json == null || json.equalsIgnoreCase("[]"))
            return null;

        JSONArray response = null;
        JSONObject header = null;
        int lObjectId = 0;
        String objectType = "", mobileRecordId = "", rmsCodingTimestamp = "";

        try {
            response = new JSONArray(json);
            header = response.getJSONObject(0);
            JSONObject mapCodingInfo = header.getJSONObject("mapCodingInfo");
            lObjectId = header.getInt("LobjectId");
            objectType = header.getString("objectType");

            if (mapCodingInfo.has("MobileRecordId")) {
                mobileRecordId = mapCodingInfo.getString("MobileRecordId");
            }
            if (mapCodingInfo.has("RMS Coding Timestamp")) {
                rmsCodingTimestamp = mapCodingInfo.getString("RMS Coding Timestamp");
            }

        } catch (JSONException jsonException) {
            Log.d(TAG, "save: setTollReceipts: response: " + response);
        }
        ArrayList<String[]> result = new ArrayList<>();
        Log.d(TAG, "save: setTollReceipts: result: " + result);
        result.add(new String[]{"0", "header", Integer.toString(lObjectId), objectType, mobileRecordId, rmsCodingTimestamp});
        return result;
    }

    public static ArrayList<String[]> setFuelReceipts(ArrayList<FuelReceiptModel> fuelReceiptModelList) {
        Log.d(TAG, "save: setFuelReceipts: ");
        final String headerMobileRecordId = getMobileRecordId("fuelreceipt", deviceId, username);
        Log.d(TAG, "save: setFuelReceipts: headerMobileRecordId: " + headerMobileRecordId);

        String postBody = serializeListAsCsvPost(fuelReceiptModelList,
                new IPostParser<FuelReceiptModel>() {
                    public String parse(final FuelReceiptModel fuelReceiptModel) {
                        Log.d(TAG, "save: setFuelReceipts: parse: ");
//                        int pricePerGallonsValue = ((int) ((Float.parseFloat(fuelReceiptModel.getFuelReceiptAmount())) / (Integer.parseInt(fuelReceiptModel.getPricePerGallons()))));
//                        Log.d(TAG, "save: setFuelReceipts: parse: pricePerGallonsValue: "+pricePerGallonsValue);
//                        String purchaserName =fuelReceiptModel.getFuelReceiptFirstName()+" "+fuelReceiptModel.getFuelReceiptLastName();
//                        Log.d(TAG, "save: setFuelReceipts: parse: purchaserName: "+purchaserName);
//                        String fuelCode = getFuelCodeFromFuelType(fuelReceiptModel.getFuelReceiptFuelType());
//                        Log.d(TAG, "save: setFuelReceipts: parse: fuelCode: "+fuelCode);

                        String row = serializeItemAsCsvPostLine(new String[]{"O", "H",
                                "" + fuelReceiptModel.getFuelReceiptObjectId(),
                                fuelReceiptModel.getFuelReceiptObjectType(),
                                (fuelReceiptModel.getFuelReceiptMobileRecordId() != null && !fuelReceiptModel.getFuelReceiptMobileRecordId().isEmpty()) ? fuelReceiptModel.getFuelReceiptMobileRecordId() : headerMobileRecordId,     // MobileRecordId
                                "",                                                                 // Functional Group Name
                                orgName,                                                            // Org name
                                orgNumber,                                                          // Org number
                                fuelReceiptModel.getFuelReceiptDateTime(),                          // DateTime
                                fuelReceiptModel.getFuelReceiptSalesTax(),                          // Sales tax
                                fuelReceiptModel.getFuelReceiptRefund(),                            // Refund (load from model)
                                fuelReceiptModel.getFuelReceiptFirstName(),                         // Company
                                fuelReceiptModel.getFuelReceiptLastName(),                          // TruckNumber
                                fuelReceiptModel.getDriverLicenseNumber(),                          // Driver License Number (from user info)
                                fuelReceiptModel.getFuelReceiptCompany(),                           // Vendor Name
//                                fuelCode,                                                           // Fuel Code
                                "",                                                           // Fuel Code
                                fuelReceiptModel.getFuelReceiptFuelType(),                          // Vendor Country
                                fuelReceiptModel.getFuelReceiptTruckNumber(),                       // Amount
                                fuelReceiptModel.getFuelReceiptDOTNumber(),                         // User Record Id
                                fuelReceiptModel.getFuelReceiptOdometer(),                          // User Record Id
                                fuelReceiptModel.getFuelReceiptVehicleLicenseNumber(),              // User Record Id
                                fuelReceiptModel.getFuelReceiptTruckStop(),                        // Vendor Name
                                "",                                                                 // vendor address
                                fuelReceiptModel.getFuelReceiptState(),                             // User Record Id
                                fuelReceiptModel.getFuelReceiptCountry(),                           // User Record Id
//                                purchaserName, // Purchasers Name (user first nme = last name)
                                "", // Purchasers Name (user first nme = last name)
//                                ""+pricePerGallonsValue,                                          // Price Per Gallon (amount/gallons)
                                "",                                          // Price Per Gallon (amount/gallons)
                                fuelReceiptModel.getFuelReceiptGallons(),                           //
                                fuelReceiptModel.getFuelReceiptAmount(),                            // Amount USD (like we aee in canada then convert amount into to usd)
                                fuelReceiptModel.getFuelReceiptUserRecordId(),
                                fuelReceiptModel.getFuelReceiptAmount()                             //(like actual that user entered)
                        });
                        return row;
                    }
                });

        Log.d(TAG, "save: setFuelReceipts: postBody: >>>" + postBody);

        String json = null;
        try {
            json = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setFuelReceipts/" + Rms.getUsername() + "/" + Rms.getPassword(),
                    "text/plain", "setFuelReceipts.txt", postBody.getBytes());
        } catch (Exception exception) {
            Log.d(TAG, "save: exception:  " + exception.getMessage());
        }
        Log.d(TAG, "save: setFuelReceipts: json: " + json);
        if (json == null || json.equalsIgnoreCase("[]"))
            return null;

        JSONArray response = null;
        JSONObject header = null;
        int lObjectId = 0;
        String objectType = "", mobileRecordId = "", rmsCodingTimestamp = "";

        try {
            response = new JSONArray(json);
            header = response.getJSONObject(0);
            JSONObject mapCodingInfo = header.getJSONObject("mapCodingInfo");
            lObjectId = header.getInt("LobjectId");
            objectType = header.getString("objectType");

            if (mapCodingInfo.has("MobileRecordId")) {
                mobileRecordId = mapCodingInfo.getString("MobileRecordId");
            }
            if (mapCodingInfo.has("RMS Coding Timestamp")) {
                rmsCodingTimestamp = mapCodingInfo.getString("RMS Coding Timestamp");
            }

        } catch (JSONException jsonException) {
            Log.d(TAG, "save: setFuelReceipts: response: " + response);
        }
        ArrayList<String[]> result = new ArrayList<>();
        Log.d(TAG, "save: setFuelReceipts: result: " + result);
        result.add(new String[]{"0", "header", Integer.toString(lObjectId), objectType, mobileRecordId, rmsCodingTimestamp});
        return result;
    }


    public static ArrayList<String[]> setTruckElds(TruckEldHeader header) throws Exception {
        ArrayList<TruckEldHeader> headers = new ArrayList();
        headers.add(header);

        return setTruckElds(headers);
    }

    public static ArrayList<String[]> setTruckElds(ArrayList<TruckEldHeader> headers) throws Exception {
        final String headerMobileRecordId = getMobileRecordId("TruckEldHeader", deviceId, username);

        String postBody = serializeListAsCsvPost(headers,
                new IPostParser<TruckEldHeader>() {
                    public String parse(final TruckEldHeader l) {
                        String row = serializeItemAsCsvPostLine(new String[]{"O", "H", l.objectId, l.objectType,
                                l.MobileRecordId != null ? l.MobileRecordId : headerMobileRecordId,     // MobileRecordId
                                "",                                                // Functional Group Name
                                orgName,                                            // Org name
                                orgNumber,                                            // Org number
                                l.VehicleLicenseNumber,                                // Vehicle License Number
                                l.CycleStartDateTime,                                // CycleStartDateTime
                                l.Rule,                                                // Rule
                                l.DriverName,                                        // Driver Name
                                l.DriverRecordId,                                    // DriverRecordId
                                l.DriverId,                                            // DriverId
                                l.CoDriverName,                                        // CoDriverName
                                l.CoDriverRecordId,                                    // CoDriverRecordId
                                l.CoDriverId,                                        // CoDriverId
                                l.TruckLogHeaderRecordId                            // TruckLogHeaderRecordId
                        });

                        int counter = 0;

                        String lines = serializeListAsCsvPost(l.getTruckEldDetails(), new IPostParser<TruckEldDetail>() {
                            public String parse(TruckEldDetail e) {
                                return serializeItemAsCsvPostLine(new String[]{"O", "D", e.objectId, e.objectType,
                                        e.MobileRecordId != null ? e.MobileRecordId : getMobileRecordId("TruckEldDetail", deviceId, username),   // MobileRecordId
                                        "",                                                                     // FunctionalGroupName
                                        orgName,                                                                // organizationName
                                        orgNumber,                                                              // organizationNumber
                                        e.TwentyFourHourPeriodStartingTime,                                     // 24-Hour Period Starting Time
                                        e.CarrierName,                                                          // Carrier Name
                                        e.UsDotNumber,                                                          // USDOT Number
                                        e.DriverName,                                                           // Driver Name
                                        e.DriverId,                                                             // Driver Id
                                        e.DriverRecordId,                                                       // DriverRecordId
                                        "",                                                                     // Drivers License State
                                        e.CoDriverName,                                                         // Co-Driver Name
                                        e.CoDriverId,                                                           // Co-Driver Id
                                        e.CoDriverRecordId,                                                     // CoDriverRecordId
                                        e.CurrentLocation,                                                      // Current Location
                                        e.DataDiagnosticsIndicators,                                            // Data Diagnostic Indicators
                                        e.EldMalfunctionIndicators,                                             // ELD Malfunction Indicators
                                        e.EldRegistrationId,                                                    // ELD Registration Id
                                        e.UnidentifiedDriverRecords,                                            // Unidentified Driver Records
                                        e.ExemptDriverStatus,                                                   // Exempt Driver Status
                                        e.MilesToday,                                                           // Miles Today
                                        e.PrintDisplayDate,                                                     // Print-Display Status
                                        e.RecordDate,                                                           // Record Date
                                        e.ShippingId,                                                           // Shipping Id
                                        e.CurrentEngineHours,                                                   // Current Engine Hours
                                        e.EngineHoursStart,                                                     // Engine Hours Start
                                        e.EngineHoursEnd,                                                       // Engine Hours End
                                        e.CurrentOdometer,                                                      // Current Odometer
                                        e.OdometerStart,                                                        // Odometer Start
                                        e.OdometerEnd,                                                          // Odometer End
                                        e.TimeZone,                                                             // Time Zone
                                        e.TruckNumber,                                                          // Truck Number
                                        e.TruckVin,                                                             // Truck VIN
                                        e.TrailerNumber,                                                        // Trailer1 Number
                                        "",                                                                     // Trailer2 Number
                                        e.TruckLogDetailRecordId,                                               // TruckLogDetailRecordId
                                        e.OffDutyHours,                                                         // Off Duty Hours
                                        e.SleeperHours,                                                         // Sleeper Hours
                                        e.DrivingHours,                                                         // Driving Hours
                                        e.OnDutyHours,                                                          // On Duty Hours
                                        e.Status,                                                               // Status
                                        e.Comments,                                                             // Comments
                                        e.Rule,                                                                 // Rule
                                        e.CycleStartDateTime,                                                   // CycleStartDateTime
                                        e.VehicleLicenseNumber,                                                 // Vehicle License Number
                                        e.DataCheckValue,                                                       // Data Check Value
                                        e.CertifyDateTime,                                                      // Certify DateTime
                                        e.CmvPowerUnitNumber                                                    // CMV Power Unit Number
                                });
                            }
                        });

                        return row + (!TextUtils.isNullOrWhitespaces(lines) ? CsvRowNeedle + lines : "");
                    }
                });

        Log.d(TAG, ">>>" + postBody);

        String json = HttpClient.postFile(getUrl() + "/Image2000/rest/shipservice/setTruckELDs/" + Rms.getUsername() + "/" + Rms.getPassword(),
                "text/plain", "setTruckELDs.txt", postBody.getBytes());

        // Return objectIds/types for first header/detail relationships

        if (json == null || json.equalsIgnoreCase("[]"))
            return null;

        TruckEldHeader h = headers.get(0);

        JSONArray response = new JSONArray(json);
        JSONObject header = response.getJSONObject(0);

        int lObjectId = header.getInt("LobjectId");
        String objectType = header.getString("objectType");

        ArrayList<String[]> result = new ArrayList<>();
        result.add(new String[]{"0", "header", Integer.toString(lObjectId), objectType});

        JSONArray arDetailRecordDataMapped = header.getJSONArray("arDetailRecordDataMapped");
        ArrayList<TruckEldDetail> details = h.getTruckEldDetails();

        for (int i = 0; i < arDetailRecordDataMapped.length(); i++) {
            JSONObject detail = arDetailRecordDataMapped.getJSONObject(i);
            TruckEldDetail d = details.get(i);

            lObjectId = detail.getInt("LobjectId");
            objectType = detail.getString("objectType");

            result.add(new String[]{"0", "detail", Integer.toString(lObjectId), objectType});
        }

        return result;
    }

    public static String appendRecordContentToTruckLog(TruckLogContentLine csv) throws
            Exception {
        ArrayList<TruckLogContentLine> result = new ArrayList();
        result.add(csv);

        return appendRecordContentToTruckLog(result);
    }

    public static String appendRecordContentToTruckLog(ArrayList<TruckLogContentLine> csv) throws
            Exception {
        TruckLogContentLine contentLine = csv.get(0);
        String objectId = contentLine.ObjectId;
        String objectType = contentLine.ObjectType;

        String postBody = serializeListAsCsvPost(csv,
                new IPostParser<TruckLogContentLine>() {
                    public String parse(final TruckLogContentLine l) {
                        String row = serializeItemAsCsvPostLine(new String[]{
                                l.TruckNumber,                                      // TruckNumber
                                l.Date,                                             // date
                                l.Time,                                             // Time
                                l.Speed,                                            // Speed
                                l.Latitude,                                         // Latitude
                                l.Longitude,                                        // Longitude
                                l.AccelerationX,                                    // AccelerationX
                                l.AccelerationY,                                    // AccelerationY
                                l.AccelerationZ,                                    // AccelerationZ
                                l.EngineOn,                                         // Engine On
                                l.Odometer,                                         // Odometer
                                l.SpeedFromObd2OrJ1939,                             // speed from OBD2 or J1939
                                l.EngineHours,                                      // Engine Hours
                                l.Status,                                           // Status
                                l.ActiveTruckRouteHeaderRecordId,                    // Active Truck Route Header RecordId
                                l.ELDLatitude,                                      // Latitude from ELD
                                l.ELDLongitude,                                     // Longitude from ELD
                                l.Speeding,                                         // Speeding
                                l.Trailer1,                                         // Trailer1
                                l.Trailer2                                          // Trailer2
                        });

                        return row;
                    }
                }) + "\n";

        String dateStr = DateUtils.getIso8601NowDateStr();

        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/recordservice/appendRecordContent/" +
                Rms.getUsername() + "/" + Rms.getPassword() + "/" + objectId + "/" + objectType + "/+/0", "text/plain", "setRecordContent." + dateStr + ".csv", postBody.getBytes());
//                Rms.getUsername() + "/" + Rms.getPassword() + "/" + objectId + "/" + objectType + "/+/0", "text/plain", "setRecordContent." + dateStr + ".txt", postBody.getBytes());
        Log.d(TAG, "appendRecordContentToTruckLog: check txt to csv change: " + response);
        return response;
    }

    public static String appendRecordContentToTruckEld(ArrayList<TruckEldContentLine> csv) throws
            Exception {
        TruckEldContentLine contentLine = csv.get(0);
        String objectId = contentLine.ObjectId;
        String objectType = contentLine.ObjectType;

        String postBody = serializeListAsCsvPost(csv, new IPostParser<TruckEldContentLine>() {
            public String parse(final TruckEldContentLine l) {
                return l.Csvline;
            }
        }) + "\n";

        String dateStr = DateUtils.getIso8601NowDateStr();

        String response = HttpClient.postFile(getUrl() + "/Image2000/rest/recordservice/appendRecordContent/" +
                Rms.getUsername() + "/" + Rms.getPassword() + "/" + objectId + "/" + objectType + "/+/0", "text/plain", "setRecordContent." + dateStr + ".csv", postBody.getBytes());
//                Rms.getUsername() + "/" + Rms.getPassword() + "/" + objectId + "/" + objectType + "/+/0", "text/plain", "setRecordContent." + dateStr + ".txt", postBody.getBytes());
        Log.d(TAG, "appendRecordContentToTruckEld: check txt to csv change: " + response);

        return response;
    }

    public static String getRecordCoding(long lObjectId, String objectType) throws IOException {
        ObjectInfo o = new ObjectInfo();
        o.setObjectInfo(lObjectId, objectType);

        return getRecordCoding(o);
    }

    public static String getRecordCoding(ObjectInfo o) throws IOException {
        //https://www.rcofox.com/Image2000/rest/recordservice/getRecordCoding/austin/austin/6/NRT243

        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/getRecordCoding/" +
                Rms.getUsername() + "/" + Rms.getPassword() + "/" + o.getLobjectIdStr() + "/" + o.getObjectType());
    }

    public static String getDirectoryService(String displayType, String objectName) throws
            Exception {
        //https://www.rcofox.com/Image2000/rest/directoryservice/getDirectoryId/austin/austin/Database/Database

        return HttpClient.get(Rms.getUrl() + "/Image2000/rest/directoryservice/getDirectoryId/" +
                Rms.getUsername() + "/" + Rms.getPassword() + "/" + displayType + "/" + objectName);
    }

    public static void setRecordCodingFields(String lObjectId, String objectType, String
            codingName, String codingValue, boolean ignoreErrorsAndEmpties) throws Exception {
        if (StringUtils.isNullOrWhitespaces(codingValue))
            codingValue = " ";

        codingValue = clearSpecialChars(codingValue);

        if (ignoreErrorsAndEmpties) {
            try {
                if (StringUtils.isNullOrWhitespaces(codingValue))
                    return;

                String response = HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/setRecordCoding/" +
                        Rms.getUsername() + "/" + Rms.getPassword() + "/" + lObjectId + "/" + objectType + "/" + codingName + "/" + codingValue + "/");
            } catch (Throwable throwable) {
                Log.d(TAG, "Error in coding field (object: " + lObjectId + "," + objectType + "): " + codingName + ": " + codingValue);
            }
        } else {
            if (StringUtils.isNullOrWhitespaces(codingValue))
                return;

            String response = HttpClient.get(Rms.getUrl() + "/Image2000/rest/recordservice/setRecordCoding/" +
                    Rms.getUsername() + "/" + Rms.getPassword() + "/" + lObjectId + "/" + objectType + "/" + codingName + "/" + codingValue + "/");
        }
    }

    private static String clearSpecialChars(String value) {
        if (StringUtils.isNullOrWhitespaces(value))
            return "";

        return value.replace("/", "").replace("#", "").replace("\"", "''")
                .replace("\r", " ").replace("\n", " ");
    }

    public static String signELDDriverLogin(String eldUsername, String vin) throws Exception {
        // https://www.rcofox.com/Image2000/rest/shipservice/signELDDriverLogin/dra/dra/dra+RX9888567
        String response = HttpClient.get(Rms.getUrl() + "/Image2000/rest/shipservice/signELDDriverLogin/" +
                Rms.getUsername() + "/" + Rms.getPassword() + "/" + eldUsername + "+" + vin);

        Log.d(TAG, "generateAndStoreNewEldAuthenticationValue: signELDDriverLogin: url: " + Rms.getUrl() + "/Image2000/rest/shipservice/signELDDriverLogin/" +
                Rms.getUsername() + "/" + Rms.getPassword() + "/" + eldUsername + "+" + vin);
        Log.d(TAG, "generateAndStoreNewEldAuthenticationValue: signELDDriverLogin: response: " + response);
        return response;
    }

    //endregion

    //region MobileRecordID management

    private static long counter = 0;

    public static String getMobileRecordId(String methodPrefix) {
        return getMobileRecordId(methodPrefix, deviceId, username, Long.toString(Calendar.getInstance().getTimeInMillis()));
    }

    public static String getDefaultMobileRecordId(String methodPrefix) {
        return getMobileRecordId(methodPrefix, deviceId, username);
    }

    public static String getMobileRecordId(String methodPrefix, String deviceId, String
            username) {
        return getMobileRecordId(methodPrefix, deviceId, username, true);
    }

    public static String getMobileRecordId(String methodPrefix, String deviceId, String
            username, boolean onOff) {
        String uniqueId = Long.toString(new Date().getTime()) + counter++;
        return getMobileRecordId(methodPrefix, deviceId, username, true, uniqueId);
    }

    public static String getMobileRecordId(String methodPrefix, String deviceId, String
            username, String uniqueId) {
        return getMobileRecordId(methodPrefix, deviceId, username, true, uniqueId);
    }

    public static String getMobileRecordId(String methodPrefix, String deviceId, String
            username, boolean onOff, String uniqueId) {
        final String needle = "-";
        return methodPrefix + needle + "Android_" + deviceId + needle + username + needle + (onOff ? "ON" : "OFF") + needle + uniqueId;
    }

    //endregion

    //region URL

    public static String getUrl() {
        return url;
    }

    public static void setUrl(String value) {
        url = value;
    }

    public static String getUsername() {
        return Rms.username;
    }

    public static void setUsernamePasswordIdentifier(String username, String password,
                                                     long identifier) {
        Rms.username = username;
        Rms.password = password;

        Rms.userId = identifier;
    }

    public static long getUserId() {
        return userId;
    }

    public static String getPassword() {
        return password;
    }

    public static String getOrgName() {
        return orgName;
    }

    public static String getOrgNumber() {
        return orgNumber;
    }

    public static final String[] arHostServerNames = {"falcon", "fox", "lion", "localhost", "logserver1", "web01"};

    public static String getUrlHostServerName(String urlString) {
        String strUrlLc = urlString.toLowerCase();

        for (String host : arHostServerNames)
            if (strUrlLc.contains(host))
                return host;

        return null;
    }

    //endregion

    //region Communication helpers

    private final static String CsvFileName = "fields.txt";
    private final static char CsvColNeedle = ',';
    protected final static char CsvRowNeedle = '\n';
    private final static char CsvQuote = '"';

    private static String getRmsUrl(String servicename, String callname, String variable) {
        return getRmsUrl(servicename, callname, new String[]{variable});
    }

    private static String getRmsUrl(String servicename, String callname, String[] variables) {
        String tmpStr = url + "/Image2000/rest/" + servicename + "/" + callname + "/" +
                username + "/" + password;

        return getRmsUrl(servicename, callname, variables, username, password);
    }

    private static String getRmsUrl(String servicename, String callname, String[]
            variables, String username, String password) {
        String tmpStr = url + "/Image2000/rest/" + servicename + "/" + callname + "/" + username + "/" + password;
        return variables == null || variables.length <= 0 ? tmpStr : tmpStr + "/" + encodeVariables(variables);
    }

    private static String getRmsUrlTest(String servicename, String callname, String[] variables) {
        String tmpStr = url + "/Image2000/rest/" + servicename + "/" + callname + "/" + "ann" + "/" + "ann";
        return variables == null || variables.length <= 0 ? tmpStr : tmpStr + "/" + encodeVariables(variables);
    }

    public static <T> String serializeListAsCsvPost(List<T> list, IPostParser<T> p) {
        StringBuilder result = new StringBuilder();

        for (T item : list)
            result.append(p.parse(item)).append(CsvRowNeedle);

        return TextUtils.trimEnd(result.toString(), String.valueOf(CsvRowNeedle));
    }

    /**
     * Max flexibility serializer, gives parser access to entire list and index of iterator.
     * One use is to use index to further granulate mobileRecordId timestamp.
     *
     * @param list
     * @param p
     * @param <T>
     * @return
     */
    public static <T> String
    serializeListAsCsvPost(List<T> list, IPostParserList<List<T>> p) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < list.size(); i++)
            result.append(p.parse(list, i)).append(CsvRowNeedle);

        return TextUtils.trimEnd(result.toString(), String.valueOf(CsvRowNeedle));
    }

    public static String serializeItemAsCsvPostLine(String[] values) {
        StringBuilder result = new StringBuilder();

        for (String v : values) {
            String field = parseSpecialChars(v);
            result.append(CsvQuote).append(field != null ? field : "").append(CsvQuote).append(CsvColNeedle);
        }

        return TextUtils.trimEnd(result.toString(), String.valueOf(CsvColNeedle));
    }

    private static String parseSpecialChars(String content) {
        if (content == null)
            return null;

        return TextUtils.replace(TextUtils.replace(TextUtils.replace(content, "%", "\\x11"),
                "\"", "\\x22"), "\r\n", "\\x0A0D");
    }

    public interface IPostParser<T> {
        public String parse(T values);
    }

    public interface IPostParserList<T> {
        public String parse(T values, int position);
    }

    public static String urlEncodeCommaDelimited(String[] arItems) {
        StringBuilder sbuf = new StringBuilder();
        try {
            for (String type : arItems) {
                if (sbuf.length() > 0) sbuf.append(",");
                sbuf.append(URLEncoder.encode(type, "UTF-8"));
            }
        } catch (Throwable e) {
            Log.d(TAG, "urlEncodeCommaDelimited() **** Error. ", e);
        }

        return sbuf.toString();
    }

    //endregion Communication helpers

    //region DeviceID

    public static String getDeviceId() {
        return deviceId;
    }

    public static void setDeviceId(String deviceId) {
        Rms.deviceId = deviceId;
    }

    //endregion

    // region Sync helpers

    public static class RmsTimestamp {
        long rmsTimestamp;
        String objectId;
        String objectType;
    }

    // endregion Sync helpers

    //region Generic helpers

    private static byte[] toByteArray(int[] values) throws Exception {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);

        for (int i = 0; i < values.length; ++i)
            dos.writeInt(values[i]);

        return baos.toByteArray();
    }

    private static PairList toCodingInfo(JSONObject item) throws JSONException {
        PairList result = new PairList();
        JSONArray fields = item.getJSONArray("arCodingInfo");

        if (fields != null)
            for (int i = 0; i < fields.length(); i++) {
                JSONObject pair = fields.getJSONObject(i);

                String displayName = pair.getString("displayName");
                String value = pair.getString("value");

                result.add(new Pair(displayName, value));
            }

        return result;
    }

    private static String encodeVariables(String[] variables) {
        String variablesStr = "";

        if (variables != null && variables.length > 0)
            for (int i = 0; i < variables.length; i++)
                try {
                    String temp = variables[i];
                    if (temp == null || temp.trim().length() == 0) {
                        variablesStr += "%20/";
                    } else if (temp.trim().equals("+")) {
                        variablesStr += "+/";
                    } else {
                        variablesStr += URLEncoder.encode(temp.trim()) + "/";
                    }

//					BusinessRules.logVerbose("encoded"+variables[i]);
                } catch (Exception e) {
                    //Log.d(TAG,"cannot encode " + variables[i]);
                }

//		if (variablesStr.charAt(variablesStr.length()-1) == '/')
//		{
//			variablesStr = variablesStr.substring(0, variablesStr.length()-1);
//		}
        return variablesStr;
    }

    public static byte[] toByteArray(InputStream is) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int reads = is.read();

        while (reads != -1) {
            baos.write(reads);
            reads = is.read();
        }

        return baos.toByteArray();
    }

    /**
     * method converts {@link InputStream} Object into byte[] array.
     *
     * @param stream the {@link InputStream} Object.
     * @return the byte[] array representation of received {@link InputStream} Object.
     * @throws IOException if an error occurs.
     */
    public static byte[] streamToByteArray(InputStream stream) throws IOException {

        byte[] buffer = new byte[1024];
        ByteArrayOutputStream os = new ByteArrayOutputStream();

        int line = 0;
        // read bytes from stream, and store them in buffer
        while ((line = stream.read(buffer)) != -1) {
            // Writes bytes from byte array (buffer) into output stream.
            os.write(buffer, 0, line);
        }

        return os.toByteArray();
    }

    //endregion

    //region JSON helpers

    public static PairList parseJsonCodingFields(JSONObject item) throws JSONException {
        if (item == null)
            return null;

        if (item.has("mapCodingInfo")) {
            return parseMap(item);
        } else if (item.has("arCodingInfo")) {
            return parseAr(item);
        } else if (item.has("LobjectId") || item.has("objectId")) {
            PairList result = new PairList();
            parseCommon(item, result);

            return result;
        }

        throw new JSONException("Invalid JSON element! " + (item != null ? item.toString() : "null item"));
    }

    private static PairList parseAr(JSONObject item) throws JSONException {
        PairList result = new PairList();
        parseCommon(item, result);
        JSONArray fields = item.getJSONArray("arCodingInfo");

        if (fields == null)
            return result;

        for (int i = 0; i < fields.length(); i++) {
            JSONObject pair = fields.getJSONObject(i);

            String displayName = pair.getString("displayName");
            String value = pair.getString("value");

            result.add(new Pair(displayName, value));
        }

        return result;
    }

    @SuppressWarnings("unchecked")
    private static void parseCommon(JSONObject item, PairList result) throws JSONException {
        if (item == null || result == null)
            return;

        Iterator<String> keys = item.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            result.add(key, item.getString(key));
        }
    }

    @SuppressWarnings("unchecked")
    private static PairList parseMap(JSONObject item) throws JSONException {
        PairList result = new PairList();

        parseCommon(item, result);
        JSONObject fields = item.getJSONObject("mapCodingInfo");
        Map<String, String> map = new HashMap<String, String>();
        Iterator<String> keys = fields.keys();

        while (keys.hasNext()) {
            String key = keys.next();
            map.put(key, fields.getString(key));
        }

        result.setMap(map, false);
        return result;
    }

    public static String getFuelCodeFromFuelType(String selfFuelName) {
        if (selfFuelName.equalsIgnoreCase("Diesel")) {
            return "01";
        } else if (selfFuelName.equalsIgnoreCase("Gasoline")) {
            return "02";
        } else if (selfFuelName.equalsIgnoreCase("Ethanol")) {
            return "03";
        } else if (selfFuelName.equalsIgnoreCase("Propane")) {
            return "04";
        } else if (selfFuelName.equalsIgnoreCase("CNG")) {
            return "05";
        } else if (selfFuelName.equalsIgnoreCase("A-55")) {
            return "06";
        } else if (selfFuelName.equalsIgnoreCase("E-85")) {
            return "07";
        } else if (selfFuelName.equalsIgnoreCase("M-85")) {
            return "08";
        } else if (selfFuelName.equalsIgnoreCase("Gasohol")) {
            return "09";
        } else if (selfFuelName.equalsIgnoreCase("LNG")) {
            return "10";
        } else if (selfFuelName.equalsIgnoreCase("Methanol")) {
            return "11";
        } else if (selfFuelName.equalsIgnoreCase("Biodiesel")) {
            return "12";
        } else if (selfFuelName.equalsIgnoreCase("Electricity")) {
            return "13";
        } else if (selfFuelName.equalsIgnoreCase("Hydrogen")) {
            return "14";
        }
        return "";
    }


    //endregion
}
