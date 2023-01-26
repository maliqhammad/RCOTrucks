package com.rco.rcotrucks.businesslogic.rms.recordcommon;

import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.JsonUtils;
import com.rco.rcotrucks.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.Map;

public abstract class RecordCommonHelperTableRec extends RecordCommonHelper implements IRmsRecordCommon.IRecordCommonHelperRecTable {
    public static final String TAG = RecordCommonHelperTableRec.class.getSimpleName();

    public RecordCommonHelperTableRec(DatabaseHelper db, String tablename) {
        super(db);
        this.tablename = tablename;
    }


    public String tablename;

    public String getTableName() {
        return tablename;
    }


    /**
     * Todo: convert this from CodingData copy to table record implementation.
     *
     * @param mapRecWorkComboByRecordType
     * @param maxTimestampKey
     * @param filterFields
     * @param filterValues
     * @param isInsert
     * @throws Exception
     */
    public static void downSyncTableRecData(
            DatabaseHelper db, BusinessRules rules, Map<String,
            RecRepairWorkRecTable.RecWorkCombo> mapRecWorkComboByRecordType,
            String maxTimestampKey, String filterFields, String filterValues,
            boolean isInsert) throws Exception {

        String strThis = "downSyncTableRecData(), ";

        String[] arRecordTypes = new String[mapRecWorkComboByRecordType.keySet().size()];
        int i = 0;
        for (RecRepairWorkRecTable.RecWorkCombo workCombo : mapRecWorkComboByRecordType.values()) {
            arRecordTypes[i++] = workCombo.getRecWork().getRecordType();
        }

        String rmsRecordTypesUrlEncoded = Rms.urlEncodeCommaDelimited(arRecordTypes).toString();
        String filterFieldsUrlEncoded = URLEncoder.encode(filterFields, "UTF-8");
        String filterValuesUrlEncoded = URLEncoder.encode(filterValues, "UTF-8");

        String maxTimestamp = rules.getSetting(maxTimestampKey);
        if (StringUtils.isNullOrEmpty(maxTimestamp)) maxTimestamp = "0";

        Log.d(TAG, strThis + "Start. rmsRecordTypesUrlEncoded=" + rmsRecordTypesUrlEncoded
                + ", arRecordTypes=" + StringUtils.dumpArray(arRecordTypes)
                + ", maxTimestamp=" + maxTimestamp
                + ", filterFields=" + filterFields + ", filterValues=" + filterValues
                + ", filterFieldsUrlEncoded=" + filterFieldsUrlEncoded + ", filterValuesUrlEncoded=" + filterValuesUrlEncoded
                + ", isInsert=" + isInsert);


        // Todo: batch processing if response has more than 5000 records.
        // Note: setting param "isReturnRawFields to false causes normal JSON map style to be returned.
        String response = Rms.getRecordsUpdatedXFilteredRaw(rmsRecordTypesUrlEncoded, -5000,
                maxTimestamp, "+", "+", "+", filterFieldsUrlEncoded, "%2C",
                filterValuesUrlEncoded, "%2C%3B",
                "+", false, false);

        Log.d(TAG, strThis + "response=" + response);

        if (response != null && response.length() > 0) {
            JSONArray jaCodingData = new JSONArray(response);
            // Todo: comment out this logging after troubleshooting. -RAN 6/6/2021
            Log.d(TAG, strThis + "jaCodingData.length()=" + jaCodingData.length());

            if (jaCodingData != null && jaCodingData.length() > 0) {
                maxTimestamp = syncUpdateDbRecords(db, jaCodingData, false, isInsert, mapRecWorkComboByRecordType); // ------------------>
                rules.setSetting(maxTimestampKey, maxTimestamp);
            } else
                Log.d(TAG, strThis + "No data to process.  jaCodingData.length()=" + jaCodingData.length());

        } else
            Log.d(TAG, strThis + "***** Warning.  Empty response calling Rms.getRecordsUpdatedXFilteredRaw()"
                    + " for record types: " + rmsRecordTypesUrlEncoded);

        Log.d(TAG, strThis + "End. maxTimestamp=" + maxTimestamp);

    }

    /**
     * This method (and methods it calls) are responsible for updating the local database records
     * from a JSON string fetched from the Image2000 RMS Server -- i.e. a "down sync".  Also
     * this code is responsible for fetching any efile content for efile type records.
     * Certain record types are "one-way", meaning they only originate on the mobile device or
     * RMS exclusively.  One-way mobile-originating records, such as Truck DVIRs
     * should only need down-syncing when
     * the app is installed or a new database version is detected, meaning a full fetch of
     * all the records or possibly a recent subset.  However, because a user may use multiple
     * devices such as a tablet and phone, few record types really qualify as one-way.
     * These methods are designed to be RecordType-agnostic, and should be able to down-syn
     * multiple record types into the generic rmsrecords and codingdata tables.
     *
     * @param jaCodingData
     * @param isAlreadyInTransaction
     * @throws JSONException
     */
    public static String syncUpdateDbRecords(DatabaseHelper db,
                                             JSONArray jaCodingData, boolean isAlreadyInTransaction, boolean isInsert,
                                             Map<String, RecRepairWorkRecTable.RecWorkCombo> mapRecWorkComboByRecordType) throws Exception {

        if (!isAlreadyInTransaction)
            return executeTransactionSyncUpdateDbRecords(db, jaCodingData, isInsert, mapRecWorkComboByRecordType);
        else
            return updateDbRecordsFromJsonMapFormat(jaCodingData, isInsert, mapRecWorkComboByRecordType);

    }

    public static String executeTransactionSyncUpdateDbRecords(DatabaseHelper db,
                                                               JSONArray jaCodingData, boolean isInsert, Map<String, RecRepairWorkRecTable.RecWorkCombo> mapRecWorkComboByRecordType) {
        Log.d(TAG, "executeTransactionSyncUpdateDbRecords() Start.");
        String maxTimestamp = null;

        try {
            db.beginTransaction();
            maxTimestamp = updateDbRecordsFromJsonMapFormat(jaCodingData, isInsert, mapRecWorkComboByRecordType);
            db.setTransactionSuccessful(); // This commits the transaction if there were no exceptions
        } catch (Exception e) {
            Log.w("Exception:", e);
        } finally {
            db.endTransaction();
        }
        Log.d(TAG, "executeTransactionSyncUpdateDbRecords() End.");
        return maxTimestamp;
    }

    /**
     * This is the down sync logic for dedicated record tables using a JSON array from getRecordsUpdatedXFiltered
     * that is NOT in the "raw" format, retrieved from the RMS server.  Currently not supporting CodingData table type of records,
     * only dedicated table record types.
     *
     * @param jaRecordData
     * @param isInsert
     * @param mapRecWorkComboByRecordType
     * @return
     * @throws Exception
     */
    public static String updateDbRecordsFromJsonMapFormat(
            JSONArray jaRecordData, boolean isInsert,
            Map<String, RecRepairWorkRecTable.RecWorkCombo> mapRecWorkComboByRecordType) throws Exception {

        String strThis = "updateDbRecordsFromJsonMapFormat(), ";

        Log.d(TAG, strThis + "Start.  jaRecordData.length()=" + jaRecordData.length()
                + ", isInsert=" + isInsert);

        BusinessRules rules = BusinessRules.instance();

//        recRepairWork.clearInit(recRepairWork.getTablename());

        long LrmsTimestamp = -1L;
        BusHelperRmsCoding.MaxTimeStamp maxTimeStamp = new BusHelperRmsCoding.MaxTimeStamp();

//        boolean isNewRecord = false;
//        boolean isValid = true; // We assume (for now) records coming from the RMS Server were validated before upsyncing.
//        int sentSyncStatus = Cadp.SYNC_STATUS_SENT;
        byte[] arEfileContent = null;
        String login = rules.getAuthenticatedUser().getLogin();
        String pwd = rules.getAuthenticatedUser().getPassword();

        int ixNewIds = 0;
//        int iRecordCount = 0; // for debugging.
        String mobileRecordId = null;
        BusHelperRmsCoding.RmsRecordType rtype = null;
        String objectTypeLast = null;

        // If no mobileRecordId coming from server, we'll invent one now.  The record type probably will never change
        // in this loop, but it is not out of the question to get more than one record type in theory, if we can handle it.  But
        // We only have one recWork for now which ties us to one record type.
//        BusHelperRmsCoding.RmsRecordType rmsRecordType = BusinessRules.getMapRecordTypeInfoFromObjectType().get(recWork.getObjectType());
        String mobileRecordIdPrefix = null;
        RmsRecTableRec recWork = null;
        RecRepairWork recRepairWork = null;
        RecordCommonHelperTableRec recHelper = null;

        for (int i = 0; i < jaRecordData.length(); i++) {

            JSONObject row = jaRecordData.getJSONObject(i);
            JsonUtils.JsonRecFiltXWrapper recjson = new JsonUtils.JsonRecFiltXWrapper(row);
            if (StringUtils.isNullOrWhitespaces(recjson.objectType)) {
                Log.d(TAG, strThis + "**** Major data corruption error, no ObjectType found in JSON recjson=" + recjson);
                continue;
            }

            if (objectTypeLast == null || !objectTypeLast.equals(recjson.objectType)) {

                rtype = rules.getMapRecordTypeInfoFromObjectType().get(recjson.objectType);
                mobileRecordIdPrefix = RecordCommonHelper.getMobileRecordIdPrefix(rtype.recordTypeName);

                RecRepairWorkRecTable.RecWorkCombo recRepairWorkCombo = mapRecWorkComboByRecordType.get(rtype.recordTypeName);

                if (recRepairWorkCombo == null)
                    throw new Exception(TAG + ", " + strThis + "**** Design error - JSON object objectType: " + recjson.objectType
                            + " is not in mapRecWork keyset=" + mapRecWorkComboByRecordType.keySet() + ". i=" + i + ", recjson=" + recjson);


                recWork = recRepairWorkCombo.getRecWork();

                if (!isInsert)
                    recRepairWork = new RecRepairWork(recWork.getTableName());

                recHelper = recRepairWorkCombo.getRecordHelper();

                objectTypeLast = recjson.objectType;
            }

            recWork.initFromJsonRecFiltX(recjson); // <------------------------------- Initialize record from JSON

            Log.d(TAG, strThis + "After recWork.initFromJsonRecFiltX(recjson), recWork=" + recWork);

//            else mobileRecordId = recWork.getMobileRecordId();

            if (rtype.isEfileType) {
                arEfileContent = Rms.getEfileBytes(recWork.getObjectType(), recWork.getObjectId(), "-1", login, pwd);
                recWork.setEfileContent(arEfileContent);
            }

//            LrmsTimestamp = (!StringUtils.isNullOrWhitespaces(recWork.getRmsTimestamp()) ? Long.parseLong(value) : -1L);
            maxTimeStamp.updateMaxTimestamp(recWork.getRmsTimestamp(), Long.parseLong(recWork.getObjectId()), recWork.getObjectType());

            if (!isInsert) {
                recRepairWork = recHelper.findRepairRecord(recRepairWork, recWork.getMobileRecordId(), recWork.getObjectId(),
                        recWork.getObjectType(), true, rtype.recordTypeName,
                        i, mobileRecordIdPrefix);

                Log.d(TAG, strThis + "recRepairWork=" + StringUtils.memberValuesToString(recRepairWork));

                Log.d(TAG, "testingObject: updateDbRecordsFromJsonMapFormat: recRepairWork.isRepaired: "+recRepairWork.isRepaired());
                if (recRepairWork.isRepaired()) {
                    String objectId = recRepairWork.getObjectId();
                    String objectType = recRepairWork.getObjectType();
                    Log.d(TAG, "testingObject: updateDbRecordsFromJsonMapFormat: previous: objectId: " + recWork.getObjectId() + " objectType: " + recWork.getObjectType());
                    Log.d(TAG, "testingObject: updateDbRecordsFromJsonMapFormat: new: objectId: " + objectId + " objectType: " + objectType);
                    recWork.setObjectId(recRepairWork.getObjectId());
                    recWork.setObjectType(recRepairWork.getObjectType());
                    recWork.setMobileRecordId(recRepairWork.getMobileRecordId());
                    recWork.setSentSyncStatus(recRepairWork.getSentSyncStatus());
                } else if (StringUtils.isNullOrWhitespaces(recWork.getMobileRecordId()))
                    recWork.setMobileRecordId(Rms.getMobileRecordId(mobileRecordIdPrefix) + "." + ixNewIds++);


                    Log.d(TAG, "testingObject: updateDbRecordsFromJsonMapFormat: isFound: "+recRepairWork.isFound());
                if (recRepairWork.isFound()) {
                    Log.d(TAG, "testingObject: updateDbRecordsFromJsonMapFormat: getIdRecord: "+recRepairWork.getIdRecord());
                    Log.d(TAG, "testingObject: updateDbRecordsFromJsonMapFormat: objectId: "+recRepairWork.getObjectId());
                    Log.d(TAG, "testingObject: updateDbRecordsFromJsonMapFormat: objectType: "+recRepairWork.getObjectType());
                    recWork.setIdRecord(recRepairWork.getIdRecord());

                    // Even if record is marked for delete, it is okay to update it.  It should be deleted on server and local db at next upsync.
                    // Todo: logic for updating based on LocalSysTime, only update if not older?
                    recHelper.updateRecord(recWork, true);
                } else {
                    Log.d(TAG, "testingObject: updateDbRecordsFromJsonMapFormat: ");
                    recHelper.insertRecord(recWork);
                }
            } else {
                if (StringUtils.isNullOrWhitespaces(recWork.getMobileRecordId()))
                    recWork.setMobileRecordId(Rms.getMobileRecordId(mobileRecordIdPrefix) + "." + ixNewIds++);

                recHelper.insertRecord(recWork);
            }
        }

        return maxTimeStamp.getMaxtimestampCsv();
    }

    private static final String SQL_DELETE_RECORD = "delete from [tablename] where id = ?";

    public SQLiteStatement getStmtDeleteRmsRecord(String tablename) {
        if (stmtDeleteRmsRecord == null) {
            String sql = SQL_DELETE_RECORD.replace("[tablename]", tablename);
            stmtDeleteRmsRecord = getDb().compileStatement(sql);
        }

        return stmtDeleteRmsRecord;
    }

    private SQLiteStatement stmtDeleteRmsRecord;

    /**
     * Todo: move this to superclass?
     *
     * @param idRecord
     * @param txc
     * @return
     */
    public int deleteRecord(long idRecord, DatabaseHelper.TxControl txc) {
        // Not worrying about multithread access at this time.
        SQLiteStatement stmt = getStmtDeleteRmsRecord(getTableName());
        stmt.bindLong(0, idRecord);

//        int iUpdated = stmtDeleteRmsRecord.executeUpdateDelete();

        int iUpdated = RecordCommonHelper.deleteTableRecordStatic(getDb(), stmt, idRecord, txc);

        return iUpdated;
    }

    /**
     * public abstract int updateRmsRecordStatusObjIdObjType(long idRecord, boolean isValid,
     * int sentSyncStatus,
     * String objectIdOptional, String objectTypeOptional, boolean isUpdateBlankObjectIdObjectType);
     *
     * @param idRecord
     * @param isValid
     * @param sentSyncStatus
     * @param objectIdOptional
     * @param objectTypeOptional
     * @param isUpdateBlankObjectIdObjectType
     * @return
     */
    public int updateRmsRecordStatusObjIdObjType(
            long idRecord, boolean isValid, int sentSyncStatus, String objectIdOptional, String objectTypeOptional, boolean isUpdateBlankObjectIdObjectType) {
        String strThis = "testingObject: updateRmsRecordStatusObjIdObjType(), ";
        Log.d(TAG, strThis + "Start. idRecord=" + idRecord + ", isValid=" + isValid
//            + ", isSent=" + isSent
                + ", sentSyncStatus=" + sentSyncStatus
                + ", objectIdOptional=" + objectIdOptional + ", objectTypeOptional=" + objectTypeOptional);

        if (idRecord < 0)
            throw new AssertionError(TAG + "testingObject: .updateRmsRecordForCoding() **** Design error.  idRmsRecords is uninitialized.");

        // Todo: possible optimization if not new record, only need/want to change timestamps, Sent bit?
        // "UPDATE RmsRecCommon SET LocalSysTime = ?, IsValid = ?, Sent = ? WHERE Id = ?"
        SQLiteStatement stmt = null;

        Log.d(TAG, "testingObject: updateRmsRecordStatusObjIdObjType: objectIdOptional: "+objectIdOptional+" objectTypeOptional: "+objectTypeOptional
                +" isUpdateBlankObjectIdObjectType: "+isUpdateBlankObjectIdObjectType);
        if ((StringUtils.isNullOrWhitespaces(objectIdOptional) || StringUtils.isNullOrWhitespaces(objectTypeOptional))
                && !isUpdateBlankObjectIdObjectType)
            stmt = getStmtRmsRecordsUpdateStatus();
        else
            stmt = getStmtRmsRecordsUpdateStatusObjIdObjType();

        Log.d(TAG, "testingObject: updateRmsRecordStatusObjIdObjType: stmt: "+stmt);
        int ix = 1;
        stmt.clearBindings();
//        stmt.bindLong(ix++, System.currentTimeMillis()); // Let's not update local timestamp unless codingfields changed, to help merge conflicts.
        stmt.bindLong(ix++, isValid ? 1 : 0);
        stmt.bindLong(ix++, sentSyncStatus);

        if (stmt.equals(getStmtRmsRecordsUpdateStatusObjIdObjType())) {
            stmt.bindString(ix++, objectIdOptional);
            stmt.bindString(ix++, objectTypeOptional);
        }

        Log.d(TAG, "testingObject: updateRmsRecordStatusObjIdObjType: idRecord: "+idRecord);
        stmt.bindLong(ix++, idRecord);

        int iUpdated = stmt.executeUpdateDelete();

        Log.d(TAG, strThis + "End. iUpdated=" + iUpdated + ", idRecord=" + idRecord + ", isValid=" + isValid
//                + ", isSent=" + isSent
                + ", sentSyncStatus=" + sentSyncStatus
                + ", objectIdOptional=" + objectIdOptional + ", objectTypeOptional=" + objectTypeOptional);

        return iUpdated;
    }

    //    public abstract SQLiteStatement getStmtRmsRecordsUpdateMobileRecordIdAndSent();
    private static final String SQL_RMS_RECORDS_UPDATE_MOBILERECORDID_SENT = "UPDATE [tablename] SET MobileRecordId = ?, sent = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateMobileRecordIdAndSent; // not thread safe.

    /**
     * Technically, should make this synchronized, but a work object should not be thread-shared.
     *
     * @return
     */
    public SQLiteStatement getStmtRmsRecordsUpdateMobileRecordIdAndSent() {
        if (stmtRmsRecordsUpdateMobileRecordIdAndSent == null) {
            String sql = SQL_RMS_RECORDS_UPDATE_MOBILERECORDID_SENT.replace("[tablename]", getTableName());
            stmtRmsRecordsUpdateMobileRecordIdAndSent
                    = getDb().compileStatement(sql);
        }
        return stmtRmsRecordsUpdateMobileRecordIdAndSent;
    }


    //    public abstract SQLiteStatement getStmtRmsRecordsUpdateStatus();
    //    private static final String SQL_RMS_RECORDS_UPDATE_STATUS = "UPDATE iftaevent SET LocalSysTime = ?, IsValid = ?, Sent = ? WHERE Id = ?";
    private static final String SQL_RMS_RECORDS_UPDATE_STATUS = "UPDATE [tablename] SET IsValid = ?, Sent = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateStatus; // not thread safe.

    private SQLiteStatement getStmtRmsRecordsUpdateStatus() {
        if (stmtRmsRecordsUpdateStatus == null) {
            String sql = SQL_RMS_RECORDS_UPDATE_STATUS.replace("[tablename]", getTableName());
            Log.d(TAG, "testingObject: getStmtRmsRecordsUpdateStatus: sql: "+sql);
            stmtRmsRecordsUpdateStatus
                    = getDb().compileStatement(sql);
        }
        return stmtRmsRecordsUpdateStatus;
    }

    //    private static final String SQL_RMS_RECORDS_UPDATE_STATUS_OBJIDTYPE = "UPDATE RmsRecords SET LocalSysTime = ?, IsValid = ?, Sent = ?, ObjectId = ?, ObjectType = ? WHERE Id = ?";
    private static final String SQL_RMS_RECORDS_UPDATE_STATUS_OBJIDTYPE = "UPDATE [tablename] SET IsValid = ?, Sent = ?, ObjectId = ?, ObjectType = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateStatusObjIdObjType; // not thread safe.

    private SQLiteStatement getStmtRmsRecordsUpdateStatusObjIdObjType() {
        if (stmtRmsRecordsUpdateStatusObjIdObjType == null) {
            String sql = SQL_RMS_RECORDS_UPDATE_STATUS_OBJIDTYPE.replace("[tablename]", getTableName());
            stmtRmsRecordsUpdateStatusObjIdObjType
                    = getDb().compileStatement(sql);
        }
        return stmtRmsRecordsUpdateStatusObjIdObjType;
    }

    public void updateSentStatusAndMobRecIdFromUpsyncResponse(long idTable, String mobileRecordId, int sentSyncStatus) {
        SQLiteStatement stmt = getStmtRmsRecordsUpdateMobileRecordIdAndSent();
        stmt.bindString(0, mobileRecordId);
        stmt.bindLong(1, sentSyncStatus);
        stmt.bindLong(2, idTable);
        stmt.executeUpdateDelete();
    }


}
