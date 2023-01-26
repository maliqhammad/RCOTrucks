package com.rco.rcotrucks.businesslogic.rms.recordcommon;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.JsonUtils;
import com.rco.rcotrucks.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.List;

// Todo: This class (or subclasses) may have to extend BusinessRules just to get access to db to avoid needing it in constructor.  Ugly.
// Or initialize all helper classes at startup by calling instance(db).  Also ugly.  Would be nice if BusinessRules.db had public accessor.

public abstract class RecordCommonHelper implements IRmsRecordCommon.IRecordCommonHelper {
    public static boolean isStartupSyncDone = false;
    public static boolean isOneTimeTableSetupDone = false;

    private static final String TAG = RecordCommonHelper.class.getSimpleName();
    private DatabaseHelper db;
    protected BusinessRules rules;

    public RecordCommonHelper(DatabaseHelper db) {
        rules = BusinessRules.instance();
        this.db = db;
    }

    public static final void bindNonblankVal(ContentValues values, String key, Object value) {
        if (value != null && value.toString().length() > 0) values.put(key, value.toString());
    }

    public static final void bindVal(SQLiteStatement stmt, String value, int index) {
        if (value == null) stmt.bindNull(index);
        else stmt.bindString(index, value);
    }

    public static void bindString(SQLiteStatement stmt, int ix, String value) {
        if (value == null) stmt.bindNull(ix);
        else stmt.bindString(ix, value);
    }

    public static void bindBlob(SQLiteStatement stmt, int ix, byte[] value) {
        if (value == null) stmt.bindNull(ix);
        else stmt.bindBlob(ix, value);
    }

    public static void bindNonNegative(SQLiteStatement stmt, int ix, long value) {
        if (value < 0) stmt.bindNull(ix);
        else stmt.bindLong(ix, value);
    }

    @Override
    public DatabaseHelper getDb() {
        return db;
    }

    /**
     * Convenience method.
     *
     * @param db
     * @param stmtRecordTableDeleteById
     * @param idRmsRecords
     * @param txc
     * @return
     */
    public static int deleteTableRecordStatic(DatabaseHelper db, SQLiteStatement stmtRecordTableDeleteById,
                                              long idRmsRecords, DatabaseHelper.TxControl txc) {
        int iUpdated = 0;

        if (txc != null && txc.isUseTransaction()) db.beginTransaction();

        try {
            stmtRecordTableDeleteById.bindLong(1, idRmsRecords);
            if (txc != null && txc.isUseTransaction()) db.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (txc != null && txc.isUseTransaction()) db.endTransaction();
        }

        return iUpdated;
    }

    /**
     * Somewhat complicated method to update a batch of records from the response of a batched upsync operation.
     * The complications are all the validations done to trap design errors that might arise
     * from batched upsyncs/updates or corrupt data.  Upsyncing records one at a time would be simpler, but in general, could
     * be much slower.
     *
     * @param jsonSetterResponse - JSON response returned from a "set.." RMS call to update the RMS.
     * @param listUpSyncRecords  - The original list of records going into the "set.." call.  It is passed
     *                           here for the purpose of helping validate the response and update
     * @param isIgnoreHeaders    - true if we are only interested in syncing the detail records, but a header record was required as a container on the server.
     */
    public <K extends IRmsRecordCommon.IRmsRecCommon> String updateRecordsFromUpsyncResponse(
            String jsonSetterResponse, List<K> listUpSyncRecords, boolean isIgnoreHeaders, RecRepairWork rmsRecordCommonWork) {
        String strThis = "upsyncFuelReceipts: updateRecordsFromUpsyncResponse(), ";
        Log.d(TAG, strThis + "Start. jsonSetterResponse.length()=" + (jsonSetterResponse != null ? jsonSetterResponse.length() : "(NULL)")
                + ", listUpSyncRecords.size()=" + listUpSyncRecords.size() + ", isIgnoreHeaders=" + isIgnoreHeaders);
        Log.d(TAG, strThis + "updateRecordsFromUpsyncResponse: arjsonResponse: "+jsonSetterResponse);

        String errorMessage = null;

        BusHelperRmsCoding.lock.lock();
        int ixJsonRecord = 0;

//        Nov 15, 2022  -
        if (jsonSetterResponse!=null && jsonSetterResponse.equalsIgnoreCase("Internal Server Error")) {
            Log.d(TAG, strThis + "updateRecordsFromUpsyncResponse: jsonSetterResponse: is Internal Server Error: jsonSetterResponse: "+jsonSetterResponse);
            errorMessage = TAG + " " + strThis + "**** Error. jsonSetterResponse=" + jsonSetterResponse;
        }

        try {
            JSONArray arjsonResponse = new JSONArray(jsonSetterResponse);
            Log.d(TAG, strThis + "updateRecordsFromUpsyncResponse: arjsonResponse: "+arjsonResponse);
            Log.d(TAG, strThis + "arjsonResponse=" + (arjsonResponse != null ? arjsonResponse.length() : "(NULL)"));

//            if ((arjsonResponse == null && listUpSyncRecords.size() > 0) || (arjsonResponse.length() != listUpSyncRecords.size()))
//                Log.d(TAG, strThis + "\n ****** Warning - Header type record case, different number of response records than sent records."
//                        + ", listUpSyncRecords.size()=" + listUpSyncRecords.size()
//                        + ", arjsonResponse.length()=" + (arjsonResponse != null ? arjsonResponse.length() : "(NULL)"));

            if (arjsonResponse != null && arjsonResponse.length() > 0) {
                Log.d(TAG, strThis + "Case: " + arjsonResponse.length() + " record responses found.");
                int iProcessedCount = 0;

                for (int j = 0; j < arjsonResponse.length(); j++) {
                    JSONObject jsonResponse = arjsonResponse.getJSONObject(j);

                    // Todo: make Crms. constants for response member names.
                    if (!isIgnoreHeaders) {
                        Log.d(TAG, strThis + "j=" + j + ", Case: not ignoring headers.");
                        processUpsyncResponseJsonRecord(listUpSyncRecords, ixJsonRecord++, rmsRecordCommonWork, jsonResponse);
                        iProcessedCount++;
                    }

                    JSONArray arjsonFuelReceiptDetailRecs = jsonResponse.getJSONArray("arDetailRecordDataMapped");

//                    if (arjsonFuelReceiptDetailRecs.length() + (isIgnoreHeaders ? 0 : 1) != listUpSyncRecords.size())
//                        Log.d(TAG, strThis + "\n ****** Warning - different number of response records than sent records."
//                                + ", listUpSyncRecords.size()=" + listUpSyncRecords.size()
//                                + ", arjsonFuelReceiptDetailRecs.length()=" + arjsonFuelReceiptDetailRecs.length());

                    if (arjsonFuelReceiptDetailRecs != null) {
//                        if (arjsonFuelReceiptDetailRecs.length() > 0)
//                            Log.d(TAG, strThis + "\n ****** Warning - found detail records in response for header record case."
//                                    + ", listUpSyncRecords.size()=" + listUpSyncRecords.size()
//                                + ", arjsonFuelReceiptDetailRecs.length()=" + arjsonFuelReceiptDetailRecs.length());
                        Log.d(TAG, strThis + "j=" + j + ", Case: arjsonFuelReceiptDetailRecs != null, "
                                + ", arjsonFuelReceiptDetailRecs.length()=" + arjsonFuelReceiptDetailRecs.length());

                        iProcessedCount += arjsonFuelReceiptDetailRecs.length();

                        for (int i = 0; i < arjsonFuelReceiptDetailRecs.length(); i++) {
                            JSONObject jsonDetailRec = arjsonFuelReceiptDetailRecs.getJSONObject(i);
//                            processUpsyncResponseJsonRecord(listUpSyncRecords, ixJsonRecord++, rmsRecordCommonWork, jsonDetailRec);
                            processUpsyncResponseJsonRecord(listUpSyncRecords, ixJsonRecord++, rmsRecordCommonWork, jsonDetailRec);
                        }
                    } else
                        Log.d(TAG, strThis + "j=" + j + ", Case: arjsonFuelReceiptDetailRecs is null.");
                }

                if (iProcessedCount != listUpSyncRecords.size())
                    Log.d(TAG, strThis + "\n ****** Assertion Warning - different number of response records processed than sent records."
                            + ", listUpSyncRecords.size()=" + listUpSyncRecords.size()
                            + ", iProcessedCount=" + iProcessedCount);
            } else
//                Log.d(TAG, strThis + "**** Warning.  arjsonResponse was empty. jsonSetterResponse=" + jsonSetterResponse);
                errorMessage = TAG + " " + strThis + "**** Warning.  arjsonResponse was empty. jsonSetterResponse=" + jsonSetterResponse;
        } catch (Throwable e) {
            errorMessage = TAG + " " + strThis + "**** jsonSetterResponse **** Error parsing and processing upsync response." + e
                    + "  Cannot update sync status of records. jsonSetterResponse=" + jsonSetterResponse;
            Log.d(TAG, strThis + errorMessage, e);
        } finally {
            BusHelperRmsCoding.lock.unlock();
        }

        Log.d(TAG, strThis + "End.");

        return errorMessage;
    }

    public int updateRmsRecordForResponse(String objectId, String objectType, boolean isValid,
                                          RecRepairWork recRepairWork) {
        Log.d(TAG, "testingObject: updateRmsRecordForResponse: objectId: " + objectId + " objectType: " + objectType + " recRepairWork: objectId: "
                + recRepairWork.getObjectId() + " recRepairWork: objectType: " + recRepairWork.getObjectType());
        String strThis = "updateRmsRecordForResponse(), ";

        int iUpdated;// Do we really want to mark a repaired record as needing upsync at this point?  Might get upsynced again
        // in the next batch -- okay?  Probably okay, and best, assuming we don't have some endless cycle bug.
        int syncStatusRepair = (recRepairWork.isRepaired() ? Cadp.SYNC_STATUS_PENDING_UPDATE : Cadp.SYNC_STATUS_SENT);

        Log.d(TAG, "testingObject: updateRmsRecordForResponse: syncStatusRepair: "+syncStatusRepair);

        String oldObjectId = recRepairWork.getObjectId();
        String oldObjectType = recRepairWork.getObjectType();
        if (!StringUtils.isNullOrWhitespaces(oldObjectId) && !StringUtils.isNullOrWhitespaces(oldObjectType) &&
                oldObjectId.equalsIgnoreCase(objectId) && oldObjectType.equalsIgnoreCase(objectType)) {
//            iUpdated = 1;
            iUpdated = updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(), isValid, syncStatusRepair, objectId, objectType, false);
        } else if (StringUtils.isNullOrWhitespaces(recRepairWork.getObjectId()) || StringUtils.isNullOrWhitespaces(recRepairWork.getObjectType())) {
//            iUpdated = recRepairWork.getRecordHelperCommon().updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(), isValid, syncStatusRepair, objectId, objectType, false);
            iUpdated = updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(), isValid, syncStatusRepair, objectId, objectType, false);
        } else {
//            iUpdated = recRepairWork.getRecordHelperCommon().updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(), isValid, syncStatusRepair, null, null, false);
            iUpdated = updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(), isValid, syncStatusRepair, null, null, false);
        }

        Log.d(TAG, "testingObject: updateRmsRecordForResponse: iUpdated: " + iUpdated);
        if (iUpdated > 0) {
            Log.d(TAG, strThis + "testingObject: Updated record: " + recRepairWork + " status to " + syncStatusRepair + ", valid.");
        } else {
            Log.d(TAG, strThis + "testingObject: ***** Error. Failed to update record: " + recRepairWork + " status to " + syncStatusRepair + ", valid.");
        }

        return iUpdated;
    }

    public <K extends IRmsRecordCommon.IRmsRecCommon> void processUpsyncResponseJsonRecord(
            List<K> listUpSyncRecords, int ixList,
            RecRepairWork recRepairWork,
            JSONObject jsonRec) throws Exception {

        String strThis = "processUpsyncResponseJsonRecord(), ";
        String mobileRecordId = jsonRec.optString("mobileRecordId");
        String objectId = jsonRec.getString("LobjectId");
        String objectType = jsonRec.getString("objectType");
        String recordType = (objectType != null ? BusinessRules.getMapRecordTypeInfoFromObjectType().get(objectType).recordTypeName : "(unknown)");

        Log.d(TAG, strThis + "Processing response for record with mobileRecordId=" + mobileRecordId + ", objectId=" + objectId
                + ", objectType=" + objectType + ", recordType=" + recordType);

        String errorCode = jsonRec.optString(Crms.REST_RESPONSE_MEMBER_ERROR_CODE);
        String operation = jsonRec.optString(Crms.REST_RESPONSE_MEMBER_OPERATION);
        Log.d(TAG, strThis + "errorCode=" + errorCode);
        Log.d(TAG, strThis + "operation=" + operation);

        if (!StringUtils.isNullOrWhitespaces(errorCode) && !Crms.REST_RESPONSE_ERROR_CODE_SUCCESS.equals(errorCode)) {
            Log.d(TAG, strThis + "Case: error condition detected from response.");
            processUpsyncResponseRecordError(jsonRec, mobileRecordId,
                    objectId, objectType, recordType, errorCode, operation, listUpSyncRecords, ixList, recRepairWork);
        } else {
            Log.d(TAG, strThis + "Case: successful upsync detected from response.");
            processUpsyncResponseRecordSuccess(jsonRec, mobileRecordId,
                    objectId, objectType, recordType, operation, listUpSyncRecords, ixList, recRepairWork);
        }
    }

    public <K extends IRmsRecordCommon.IRmsRecCommon> void processUpsyncResponseRecordError(
            JSONObject jsonDetailRec, String mobileRecordId, String objectId, String objectType,
            String recordType, String errorCode, String operation, List<K> listUpsyncRecords, int ixResponseRecord,
            RecRepairWork recRepairWork
    ) throws Exception {

        String strThis = "processUpsyncResponseRecordError(), ";
        String errorMessage = jsonDetailRec.optString(Crms.REST_RESPONSE_MEMBER_ERROR_MESSAGE);

        Log.d(TAG, strThis + "**** Sync error.  A " + recordType + " record " + operation + " operation caused an error on the RMS server" +
                " for record with"
                + " mobileRecordId=" + mobileRecordId + ", objectId=" + objectId
                + ", objectType=" + objectType + ", recordType=" + recordType +
                ", errorCode: " + errorCode + ", errorMessage=" + errorMessage
                + ", jsonDetailRec: " + jsonDetailRec);

//        IRmsRecordCommon.IRmsRecCommon recRepairWork = findRepairRecord(
//        recRepairWork = recRepairWork.findRepairRecord(
//                mobileRecordId, objectId, objectType, true, recordType, ixResponseRecord,
//                null);

        recRepairWork = findRepairRecord(recRepairWork,
                mobileRecordId, objectId, objectType, true, recordType, ixResponseRecord,
                null);

//        if (recRepairWork != null && recRepairWork.idRmsRecords >= 0) {
        if (recRepairWork != null && recRepairWork.getIdRecord() >= 0) {
            // Handle special record-not-found cases for DELETE and UPDATE operations.
//            if (recRepairWork.objectId != null && recRepairWork.objectType != null && Crms.REST_RESPONSE_ERROR_CODE_RECORD_NOT_FOUND.equals(errorCode)) {
            if (recRepairWork.getObjectId() != null && recRepairWork.getObjectType() != null && Crms.REST_RESPONSE_ERROR_CODE_RECORD_NOT_FOUND.equals(errorCode)) {
                if (Crms.REST_CSV_OPERATION_DELETE.equals(operation)) {
                    if (recRepairWork.getSentSyncStatusBeforeRepair() == Cadp.SYNC_STATUS_MARKED_FOR_DELETE) {
//                         int iUpdated = deleteRmsRecordAndCodingData(recRepairWork.idRmsRecords,
//                         int iUpdated = deleteRmsRecordAndCodingData(recRepairWork.getIdTable(),
//                                 new DatabaseHelper.TxControl(false, false));
//                         int iUpdated = recRepairWork.getRecordHelperCommon().deleteRecord(recRepairWork.getIdRecord(),
//                                 new DatabaseHelper.TxControl(false, false));
                        int iUpdated = deleteRecord(recRepairWork.getIdRecord(),
                                new DatabaseHelper.TxControl(false, false));
                    }
                } else {
                    // Special case - ObjectId, ObjectType not blank in local record but record not found on server and not a delete operation,
                    // assume it was an update operation.
                    // We will blank out the objectid, objecttype and try again next sync cycle.
//                    int iUpdated = updateRmsRecordsStatus(recRepairWork.idRmsRecords,
//                    int iUpdated = updateRmsRecordsStatus(recRepairWork.getIdTable(),
//                            true, recRepairWork.getSyncStatusBeforeRepair(), null, null, true);
//                     int iUpdated = recRepairWork.getRecordHelperCommon().updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(),
//                             true, recRepairWork.getSentSyncStatusBeforeRepair(), null, null, true);
                    int iUpdated = updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(),
                            true, recRepairWork.getSentSyncStatusBeforeRepair(), null, null, true);
                }
            } else {
//                int iUpdated = updateRmsRecordsStatus(recRepairWork.getIdTable(),
//                        false, Cadp.SYNC_STATUS_SENT, null, null, false);
//                int iUpdated = recRepairWork.getRecordHelperCommon().updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(),
//                        false, Cadp.SYNC_STATUS_SENT, null, null, false);
                int iUpdated = updateRmsRecordStatusObjIdObjType(recRepairWork.getIdRecord(),
                        false, Cadp.SYNC_STATUS_SENT, null, null, false);
                if (iUpdated > 0)
                    Log.d(TAG, strThis + "Updated record: " + recRepairWork + " status to sent, invalid.");
            }
        } else {
            Log.d(TAG, strThis + "***** Design Error. Couldn't find and failed to update local db record, recRepairWork={" + recRepairWork
                    + "}, " + " mobileRecordId=" + mobileRecordId + ", objectId=" + objectId
                    + ", objectType=" + objectType + ", recordType=" + recordType + ", errorCode: " + errorCode);
        }
    }

    /**
     * @param jsonDetailRec
     * @param mobileRecordId
     * @param objectId
     * @param objectType
     * @param recordType
     * @param operation
     * @param listUpsyncRecords
     * @param ixResponseRecord
     * @param recRepairWork
     */

    public <K extends IRmsRecordCommon.IRmsRecCommon> void processUpsyncResponseRecordSuccess(
            JSONObject jsonDetailRec, String mobileRecordId, String objectId, String objectType,
            String recordType, String operation, List<K> listUpsyncRecords, int ixResponseRecord,
            RecRepairWork recRepairWork
    ) throws Exception {

        String strThis = "processUpsyncResponseRecordSuccess(), ";

        Log.d(TAG, "processUpsyncResponseRecordSuccess() Start.  called with: jsonDetailRec = " + jsonDetailRec + ", mobileRecordId = "
                + mobileRecordId + ", objectId = " + objectId + ", objectType = " + objectType + ", recordType = "
                + recordType + ", operation=" + operation
                + ", listUpsyncRecords.get(ixResponseRecord) = " + listUpsyncRecords.get(ixResponseRecord)
                + ", ixResponseRecord = " + ixResponseRecord);

        boolean isValid = true;
        int iUpdated = 0;

//        RmsRecCommon recRepairWork = findRepairRecord(
//        K recRepairWork = findRepairRecord(
//        recRepairWork = recRepairWork.findRepairRecord(
//                mobileRecordId, objectId, objectType, true, recordType, ixResponseRecord,
//                null);
        recRepairWork = findRepairRecord(recRepairWork,
                mobileRecordId, objectId, objectType, true, recordType, ixResponseRecord,
                null);

        Log.d(TAG, strThis + "findRepairRecord returned: {" + recRepairWork + "}");

        if (ixResponseRecord < listUpsyncRecords.size()) {
            long idExpectedRecord = listUpsyncRecords.get(ixResponseRecord).getIdRecord();

//            if (idExpectedRecord != recRepairWork.idRmsRecords)
            if (recRepairWork != null && idExpectedRecord != recRepairWork.getIdRecord())
                Log.d(TAG, strThis + "**** Warning. Unexpected record found to update status.  Possible repair done. idExpectedRecord="
//                        + idExpectedRecord + ", updated rmsrecord Id: " + recRepairWork.idRmsRecords + "\n"
                        + idExpectedRecord + ", updated rmsrecord Id: " + recRepairWork.getIdRecord() + "\n"
                        + ", recRepairWork=" + recRepairWork + ", expectedRecord: " + listUpsyncRecords.get(ixResponseRecord));
            else
                Log.d(TAG, strThis + "**** Warning. ixResponseRecord < listUpsyncRecords.size().  Also Possible repair fail. idExpectedRecord="
                        + idExpectedRecord + ", listUpsyncRecords.size()=" + listUpsyncRecords.size() + "\n"
                        + idExpectedRecord + ", updated rmsrecord Id: " + recRepairWork.getIdRecord() + "\n"
                        + ", recRepairWork=" + recRepairWork + ", expectedRecord: " + listUpsyncRecords.get(ixResponseRecord));
        }

//        if (recRepairWork != null && recRepairWork.idRmsRecords >= 0) {
        if (recRepairWork != null && recRepairWork.getIdRecord() >= 0) {
            if (Crms.REST_CSV_OPERATION_DELETE.equals(operation)
                    || recRepairWork.getSentSyncStatusBeforeRepair() == Cadp.SYNC_STATUS_MARKED_FOR_DELETE) {
                // Try to delete the record if consistent.  If not, update it as failed.
                if (recRepairWork.getSentSyncStatusBeforeRepair() != Cadp.SYNC_STATUS_MARKED_FOR_DELETE) {
                    Log.d(TAG, strThis + "****** Assertion error.  Response operation is " + operation
                            + ", but record sent/sync status is not marked for deletion (should be: " + Cadp.SYNC_STATUS_MARKED_FOR_DELETE
                            + "), recRepairWork={" + recRepairWork + "}\n");

//                    iUpdated = updateRmsRecordForResponse(objectId, objectType, isValid, recRepairWork);
                    iUpdated = updateRmsRecordForResponse(objectId, objectType, isValid, recRepairWork);
                }
//                else iUpdated = deleteRmsRecordAndCodingData(recRepairWork.idRmsRecords,
//                else iUpdated = deleteRecord(recRepairWork.getIdRecord(),
//                        new DatabaseHelper.TxControl(false, false));
                else iUpdated = deleteRecord(recRepairWork.getIdRecord(),
                        new DatabaseHelper.TxControl(false, false));
            } else
//                iUpdated = updateRmsRecordForResponse(objectId, objectType, isValid, recRepairWork);
                iUpdated = updateRmsRecordForResponse(objectId, objectType, isValid, recRepairWork);
        }

        Log.d(TAG, strThis + "End. iUpdated=" + iUpdated);
    }

    public static final String getMobileRecordIdPrefix(String recordType) {
        return recordType.replace(" ", "");
    }

    public byte[] fetchEfileContent(String sql, String[] arParams) {
        String strThis = "fetchEfileContent(), ";
//            String strSql = "SELECT EfileContent FROM " + tablename + " WHERE Id = ?";
//            String[] arParams = new String[1];
        Cursor cur = null;
        byte[] arBytes = null;

//            arParams[0] = String.valueOf(idTable);

        try {
            cur = getDb().getQuery(sql, arParams); // ------------>

            if (cur.getCount() > 1) Log.d(TAG, strThis
                    + "**** Major design error. Multiple records returned for sql: " + sql
                    + "\r arParams=" + StringUtils.dumpArray(arParams) + ", this=" + this);

            while (cur.moveToNext())
                arBytes = cur.getBlob(0);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }

        return arBytes;
    }

    /**
     * **** Note hard to make this generic because assumes knowledge of Sql Table columns if done efficiently.  If we
     * enforce common table column names and use less efficient access by name instead of position could work,
     * but maintainable?  Other possibility is to
     * completely encapsulate the fetching of records in some kind of iterator, but worth the hassle? -RAN 5/24/2021
     *
     * @param mobileRecordId
     * @param objectId
     * @param objectType
     * @param isRepairConflict
     * @param recordType
     * @param ixIteration
     * @param mobileRecordIdPrefix
     * @return
     * @throws Exception
     */
    public RecRepairWork findRepairRecord(RecRepairWork recRepairWork,
                                          String mobileRecordId, String objectId, String objectType,
                                          boolean isRepairConflict, String recordType, int ixIteration, String mobileRecordIdPrefix) throws Exception {
        Log.d(TAG, "findRepairRecord: ");
        String strThis = "findRepairRecord(), ";

        Log.d(TAG, strThis + "Start. mobileRecordId=" + mobileRecordId
                + ", objectId=" + objectId + ", objectType=" + objectType + ", isRepairConflict=" + isRepairConflict);


        Cursor cursor = null;
        int ixRow = 0;
        String objId = null;
        String objType = null;

        recRepairWork.clearInit(recRepairWork.getTablename());
        Log.d(TAG, "findRepairRecord: recRepairWork.getTableName(): " + recRepairWork.getTablename());

        DatabaseHelper db = getDb();

        try {
            Log.d(TAG, "findRepairRecord: mobileRecordId: " + mobileRecordId + " objectId: " + objectId + " objectType: " + objectType);
            // Slightly convoluted logic to use most efficient or effective query available.
            if (!StringUtils.isNullOrWhitespaces(mobileRecordId) && !StringUtils.isNullOrWhitespaces(objectId)
                    && !StringUtils.isNullOrWhitespaces(objectType)) {
                Log.d(TAG, "findRepairRecord: mobileRecordId: objectId: objectType: when all these three are not null");
//                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectId, ObjectType, IdRecordType, sent FROM rmsrecords"
//                  cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectId, ObjectType, IdRecordType, sent FROM " + getTablename()
                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectId, ObjectType, IdRecordType, sent FROM " + recRepairWork.getTablename()
                        + "\n WHERE MobileRecordId = ? OR (ObjectId = ? AND ObjectType = ?)", new String[]{mobileRecordId, objectId, objectType});
//                cursor = db.getQuery(rmsRecordCommonWork.getSqlSearchByMobRecIdObjIdObjType(), new String[]{mobileRecordId, objectId, objectType});
            } else if (!StringUtils.isNullOrWhitespaces(objectId)
                    && !StringUtils.isNullOrWhitespaces(objectType))
//                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM rmsrecords"
                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM " + recRepairWork.getTablename()
                        + "\n WHERE (ObjectId = ? AND ObjectType = ?)", new String[]{objectId, objectType});
//                cursor = db.getQuery(rmsRecordCommonWork.getSqlSearchByObjIdObjType(), new String[]{objectId, objectType});
            else if (!StringUtils.isNullOrWhitespaces(mobileRecordId))
//                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM rmsrecords"
                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM " + recRepairWork.getTablename()
                        + "\n WHERE MobileRecordId = ?", new String[]{mobileRecordId});
//                cursor = db.getQuery(rmsRecordCommonWork.getSqlSearchByMobRecId(), new String[]{mobileRecordId});
            else
                Log.d(TAG, strThis + "**** Probable design error - all search parameters are blank. mobileRecordId=" + mobileRecordId
                        + ", objectId=" + objectId + ", objectType=" + objectType);

            Log.d(TAG, "findRepairRecord: cursor: " + cursor);
            if (cursor != null) {
                Log.d(TAG, "findRepairRecord: cursor: size: " + cursor.getCount());
                int icount = cursor.getCount();
                Log.d(TAG, strThis + "After query, icount=" + icount);

                if (icount > 0) {
                    recRepairWork.setFound(true);

                    while (cursor.moveToNext()) {
                        int ix = 0;
                        recRepairWork.setIdRecord(cursor.getLong(ix++));
                        recRepairWork.setMobileRecordId(cursor.getString(ix++));
                        recRepairWork.setObjectId(cursor.getString(ix++));
                        recRepairWork.setObjectType(cursor.getString(ix++));

                        Log.d(TAG, "testingObject: findRepairRecord: objectId: " + objectId + " objectType: " + objectType);

                        recRepairWork.setIdRecordType(cursor.getInt(ix++));
                        recRepairWork.setSentSyncStatus(cursor.getInt(ix++));
                        recRepairWork.setSentSyncStatusBeforeRepair(recRepairWork.getSentSyncStatus());

                        Log.d(TAG, strThis + "Case: ixRow=" + ixRow + ", found an rmsrecord row by MobileRecordId=" + mobileRecordId
                                + " or (ObjectId=" + objectId + " and ObjectType=" + objectType + ")"
                                + ", recRepairWork=" + recRepairWork);

                        if (!StringUtils.isNullOrWhitespaces(objectId) && !StringUtils.isNullOrWhitespaces(objectType)) {
                            //  Always pick the record if it matches the ObjectId, ObjectType
                            // and if a conflict detected, update its MobileRecordId.
                            // Because of unique indexes, a multiple-record conflict case implies one record will have non-null objectId, objectType.
//                            if (objectId.equals(rmsRecordCommonWork.objectId) && objectType.equals(rmsRecordCommonWork.objectType)) {
                            if (objectId.equals(recRepairWork.getObjectId()) && objectType.equals(recRepairWork.getObjectType())) {
                                Log.d(TAG, strThis + "Case: ixRow=" + ixRow + ", found matching ObjectId, ObjectType"
                                        + ", MobileRecordId=" + mobileRecordId
                                        + " ObjectId=" + objectId + ", ObjectType=" + objectType
                                        + ", recRepairWork=" + recRepairWork + (icount > 1 ? ", picking this record to resolve conflict." : ""));
                                break;
                            }
                        }
                        ixRow++;
                    }

                    cursor.close();

                    boolean isMobileRecordIdConflict
                            = (!StringUtils.isNullOrWhitespaces(mobileRecordId) && !mobileRecordId.equals(recRepairWork.getMobileRecordId()));

                    // If more than one record found or the mobileRecordIds don't match, some repair work needed.
//                    if (icount > 1 || (!StringUtils.isNullOrWhitespaces(mobileRecordId) && !mobileRecordId.equals(getMobileRecordId()))) {
                    if (icount > 1 || isMobileRecordIdConflict) {
                        Log.d(TAG, strThis + "Case: A conflict was found.  Analyze it and repair it if 'isRepairConflict' is true.");

                        if (icount > 2)
                            Log.d(TAG, "\n" + strThis + "****** Major database design error! More than two records found (" + ixRow
                                    + "), should be violation of unique indexes.\n");

                        if (icount > 1) {
                            /*
                                Repair Rules for two records found.
                                1.  One record should be located by ObjectId, ObjectType.  That is
                                    the preferred record whose values were stored in this RecRepairWork object during the loop.  By inference, there
                                    must be another record found with the searched MobileRecordId (typically from the downsynced JSON).
                                    Give the preferred record a new MobileRecordId and set status to upsync again to update the remote server,
                                    update or insert the MobileRecordId (and codingfield for CodingData type records) and return the Id of the preferred record.
                             */
                            Log.d(TAG, strThis + "****** Design/Data Error. Located more than one rmsrecord row by MobileRecordId=" + mobileRecordId
                                    + " or (ObjectId=" + objectId + " and ObjectType=" + objectType + ")" + ", icount=" + icount
                                    + ", resolved by picking matching ObjectId, ObjectType.  Will now update the chosen record with a new MobileRecordId.");

                            if (StringUtils.isNullOrEmpty(mobileRecordIdPrefix))
                                mobileRecordIdPrefix = Rms.getMobileRecordId(RecordCommonHelper.getMobileRecordIdPrefix(recordType));

                            if (ixIteration < 0)
                                recRepairWork.setMobileRecordId(mobileRecordIdPrefix);
                            else
                                recRepairWork.setMobileRecordId(mobileRecordIdPrefix + "." + ixIteration);
                        } else if (isMobileRecordIdConflict) {
                            // By previous logic, isMobileRecordIdConflict == true requires a non-blank passed mobileRecordId parameter.
                            Log.d(TAG, strThis + "No multiple record conflict found, but MobileRecordId mismatch found, mobileRecordId=" + mobileRecordId
                                    + ", recRepairWork.mobileRecordId=" + recRepairWork.getMobileRecordId() + ".  Will update chosen record"
                                    + " with the searched MobileRecordId: " + mobileRecordId); // i.e. setting the local record MobileRecordId to same as server.

                            recRepairWork.setMobileRecordId(mobileRecordId);
                        }

                        if (isRepairConflict) {
                            Log.d(TAG, strThis + "Will now repair conflict by updating the chosen record with new MobileRecordId and setting sent to SYNC_STATUS_PENDING_UPDATE if not DELETE so the change will be upsynced."
//                                + " The original sent value will be returned and is: " + recRepairWork.getSyncStatusBeforeRepair()); // Why should we return the original sent value if we are setting the db value to Cadp.SYNC_STATUS_PENDING_UPDATE.
                                    + " The original sent value will be returned and is: " + recRepairWork.getSentSyncStatusBeforeRepair()); //  If the record was SYNC_STATUS_DELETE, the calling routine wants to know to delete it rather than upsync it.


                            try {
//                                stmtRmsRecordsUpdateMobileRecordIdAndSent.bindString(0, rmsRecordCommonWork.mobileRecordId);
//                                stmtRmsRecordsUpdateMobileRecordIdAndSent.bindString(0, rmsRecordCommonWork.getMobileRecordId());
//                                stmtRmsRecordsUpdateMobileRecordIdAndSent.bindLong(1, Cadp.SYNC_STATUS_PENDING_UPDATE);
//                                stmtRmsRecordsUpdateMobileRecordIdAndSent.bindLong(2, rmsRecordCommonWork.getIdTable());
//                                stmtRmsRecordsUpdateMobileRecordIdAndSent.executeUpdateDelete();
//                                insertOrUpdateCodingDataNoId(rmsRecordCommonWork.idRmsRecords, CMID_MOBILE_RECORDID, rmsRecordCommonWork.mobileRecordId);
//                                insertOrUpdateCodingDataNoId(rmsRecordCommonWork.getIdTable(), CMID_MOBILE_RECORDID, rmsRecordCommonWork.getMobileRecordId());
                                if (recRepairWork.getSentSyncStatus() != Cadp.SYNC_STATUS_MARKED_FOR_DELETE)
                                    recRepairWork.setSentSyncStatus(Cadp.SYNC_STATUS_PENDING_UPDATE);

//                                if (getSentSyncStatusBeforeRepair() != Cadp.SYNC_STATUS_MARKED_FOR_DELETE)
                                updateSentStatusAndMobRecIdFromUpsyncResponse(recRepairWork.getIdRecord(), recRepairWork.getMobileRecordId(), recRepairWork.getSentSyncStatus());

                                recRepairWork.setRepaired(true);
                                Log.d(TAG, strThis + "Conflict record successfully updated with new MobileRecordId, recRepairWork=" + recRepairWork);
                            } catch (Throwable e) {
                                Log.d(TAG, strThis + "***** Unable to update MobileRecordId of conflict record, recRepairWork=" + recRepairWork);
                            }
                        } else
                            Log.d(TAG, strThis + "***** Warning, non-standard configuration.  Repair mode is disabled, record was not updated, this=" + this + "\n");
                    } else
                        Log.d(TAG, strThis + "Case: No conflict was found. returning found (or not found) record information. recRepairWork=" + recRepairWork + "\n");

                } else
                    Log.d(TAG, strThis + "****** Cannot locate any " + recRepairWork.getTablename() + " row by MobileRecordId=" + mobileRecordId
                            + " or (ObjectId=" + objectId + " and ObjectType=" + objectType + ")" + ", icount=" + icount + ".");
            }
        } catch (Throwable e) {
            Log.d(TAG, strThis + "**** Error. ", e);
            throw e;
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }

        return recRepairWork;
    }

}
