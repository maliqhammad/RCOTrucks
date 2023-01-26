package com.rco.rcotrucks.businesslogic.rms.recordcommon;

/*
                "CREATE TABLE rmsrecords (" +
                        "Id			                INTEGER NOT NULL PRIMARY KEY, " +
                        "IdRecordType               INTEGER, " +
                        "ObjectType                 TEXT, " +
                        "ObjectId                   TEXT, " +
                        "RecordId			        TEXT, " +
                        "MobileRecordId			    TEXT, " +
                        "MasterBarcode              TEXT, " +  // may not need this.  Used to relate details to header rec.
                        "RmsTimestamp		        INTEGER, " + // May need index to help syncing
                        "EfileContent               BLOB, " +
                        "IdRmsRecordsLink           INTEGER," +
                        "IsValid                    INTEGER DEFAULT(0), " +
                        "sent				        INTEGER DEFAULT (" + Cadp.SYNC_STATUS_PENDING_UPDATE + "), " + // may need index to help syncing -- changing meaning of this to "SyncStatus"  0 means pending upsync, 1 means sync completed, 2 means marked for deletion.
                        "IsEfileContentSent         INTEGER DEFAULT (" + Cadp.SYNC_STATUS_PENDING_UPDATE + "), " + // may need index to help syncing -- changing meaning of this to "SyncStatus"  0 means pending upsync, 1 means sync completed, 2 means marked for deletion.
                        "LocalSysTime		        INTEGER " + // May need index to help syncing
                        ");",

     */

import android.database.Cursor;
import android.util.Log;

import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.StringUtils;

public class RecRepairWork {
    private static String TAG = RecRepairWork.class.getSimpleName();
    private String tablename;
    private long idRecord = -1L;
    private long idRecordType = -1L;
    private String recordType;
    private String objectType;
    private String objectId;
    private String mobileRecordId;
    private int sentSyncStatus;
    private int sentSyncStatusBeforeRepair;
    private boolean isRepaired = false;
    private boolean isFound = false;
//    private IRmsRecordCommon.IRecordCommonHelper recordHelperCommon;

    public RecRepairWork(String tablename) {
        this.tablename = tablename;
    }

//    public IRmsRecordCommon.IRecordCommonHelper getRecordHelperCommon() {
//        return recordHelperCommon;
//    }


    public void setFound(boolean found) {
        isFound = found;
    }

    public boolean isFound() {
        return isFound;
    }

//    /**
//     * **** Note hard to make this generic because assumes knowledge of Sql Table columns if done efficiently.  If we
//     * enforce common table column names and use less efficient access by name instead of position could work,
//     * but maintainable?  Other possibility is to
//     * completely encapsulate the fetching of records in some kind of iterator, but worth the hassle? -RAN 5/24/2021
//     * @param mobileRecordId
//     * @param objectId
//     * @param objectType
//     * @param isRepairConflict
//     * @param recordType
//     * @param ixIteration
//     * @param mobileRecordIdPrefix
//     * @return
//     * @throws Exception
//     */
//    public RecRepairWork findRepairRecordDisabled(
//            String mobileRecordId, String objectId, String objectType,
//            boolean isRepairConflict, String recordType, int ixIteration, String mobileRecordIdPrefix,
//            RecordCommonHelper recHelper // just to get it to compile while refactoring. -RAN 6/3
//    ) throws Exception {
//        String strThis = "findRepairRecordDisabled(), ";
//
//        Log.d(TAG, strThis + "Start. mobileRecordId=" + mobileRecordId
//                + ", objectId=" + objectId + ", objectType=" + objectType + ", isRepairConflict=" + isRepairConflict);
//
//
//        Cursor cursor = null;
//        int ixRow = 0;
//        String objId = null;
//        String objType = null;
//
//        clearInit(getTablename());
//
////        DatabaseHelper db = getRecordHelperCommon().getDb();
//        DatabaseHelper db = RecordRulesHelper.getDb(); // Just to make it compile while refactoring. -RAN 6/3/2021
//        try {
//            // Slightly convoluted logic to use most efficient or effective query available.
//            if (!StringUtils.isNullOrWhitespaces(mobileRecordId) && !StringUtils.isNullOrWhitespaces(objectId)
//                    && !StringUtils.isNullOrWhitespaces(objectType))
////                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectId, ObjectType, IdRecordType, sent FROM rmsrecords"
////                  cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectId, ObjectType, IdRecordType, sent FROM " + getTablename()
//                  cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectId, ObjectType, IdRecordType, sent FROM " + getTablename()
//                    + "\n WHERE MobileRecordId = ? OR (ObjectId = ? AND ObjectType = ?)", new String[]{mobileRecordId, objectId, objectType});
////                cursor = db.getQuery(rmsRecordCommonWork.getSqlSearchByMobRecIdObjIdObjType(), new String[]{mobileRecordId, objectId, objectType});
//            else if (!StringUtils.isNullOrWhitespaces(objectId)
//                    && !StringUtils.isNullOrWhitespaces(objectType))
////                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM rmsrecords"
//                  cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM " + getTablename()
//                        + "\n WHERE (ObjectId = ? AND ObjectType = ?)", new String[]{objectId, objectType});
////                cursor = db.getQuery(rmsRecordCommonWork.getSqlSearchByObjIdObjType(), new String[]{objectId, objectType});
//            else if (!StringUtils.isNullOrWhitespaces(mobileRecordId))
////                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM rmsrecords"
//                cursor = db.getQuery("SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM " + getTablename()
//                        + "\n WHERE MobileRecordId = ?", new String[]{mobileRecordId});
////                cursor = db.getQuery(rmsRecordCommonWork.getSqlSearchByMobRecId(), new String[]{mobileRecordId});
//            else
//                Log.d(TAG, strThis + "**** Probable design error - all search parameters are blank. mobileRecordId=" + mobileRecordId
//                        + ", objectId=" + objectId + ", objectType=" + objectType);
//
//            if (cursor != null) {
//                int icount = cursor.getCount();
//                Log.d(TAG, strThis + "After query, icount=" + icount);
//
//                if (icount > 0) {
//                    isFound = true;
//
//                    while (cursor.moveToNext()) {
//                        int ix = 0;
//                        setIdRecord(cursor.getLong(ix++));
//                        setMobileRecordId(cursor.getString(ix++));
//                        setObjectId(cursor.getString(ix++));
//                        setObjectType(cursor.getString(ix++));
//                        setIdRecordType(cursor.getInt(ix++));
//                        setSentSyncStatus(cursor.getInt(ix++));
//                        setSentSyncStatusBeforeRepair(getSentSyncStatus());
//
//                        Log.d(TAG, strThis + "Case: ixRow=" + ixRow + ", found an rmsrecord row by MobileRecordId=" + mobileRecordId
//                                + " or (ObjectId=" + objectId + " and ObjectType=" + objectType + ")"
//                                + ", this=" + this.toString());
//
//                        if (!StringUtils.isNullOrWhitespaces(objectId) && !StringUtils.isNullOrWhitespaces(objectType)) {
//                            //  Always pick the record if it matches the ObjectId, ObjectType
//                            // and if a conflict detected, update its MobileRecordId.
//                            // Because of unique indexes, a multiple-record conflict case implies one record will have non-null objectId, objectType.
////                            if (objectId.equals(rmsRecordCommonWork.objectId) && objectType.equals(rmsRecordCommonWork.objectType)) {
//                            if (objectId.equals(getObjectId()) && objectType.equals(getObjectType())) {
//                                Log.d(TAG, strThis + "Case: ixRow=" + ixRow + ", found matching ObjectId, ObjectType"
//                                        + ", MobileRecordId=" + mobileRecordId
//                                        + " ObjectId=" + objectId + ", ObjectType=" + objectType
//                                        + ", rmsRecordCommonWork=" + this + (icount > 1 ? ", picking this record to resolve conflict." : ""));
//                                break;
//                            }
//                        }
//                        ixRow++;
//                    }
//
//                    cursor.close();
//
//                    boolean isMobileRecordIdConflict
//                            = (!StringUtils.isNullOrWhitespaces(mobileRecordId) && !mobileRecordId.equals(getMobileRecordId()));
//
//                    // If more than one record found or the mobileRecordIds don't match, some repair work needed.
////                    if (icount > 1 || (!StringUtils.isNullOrWhitespaces(mobileRecordId) && !mobileRecordId.equals(getMobileRecordId()))) {
//                    if (icount > 1 || isMobileRecordIdConflict) {
//                        Log.d(TAG, strThis + "Case: A conflict was found.  Analyze it and repair it if 'isRepairConflict' is true.");
//
//                        if (icount > 2)
//                            Log.d(TAG, "\n" + strThis + "****** Major database design error! More than two records found (" + ixRow
//                                    + "), should be violation of unique indexes.\n");
//
//                        if (icount > 1) {
//                            /*
//                                Repair Rules for two records found.
//                                1.  One record should be located by ObjectId, ObjectType.  That is
//                                    the preferred record whose values were stored in this RecRepairWork object during the loop.  By inference, there
//                                    must be another record found with the searched MobileRecordId (typically from the downsynced JSON).
//                                    Give the preferred record a new MobileRecordId and set status to upsync again to update the remote server,
//                                    update or insert the MobileRecordId (and codingfield for CodingData type records) and return the Id of the preferred record.
//                             */
//                            Log.d(TAG, strThis + "****** Design/Data Error. Located more than one rmsrecord row by MobileRecordId=" + mobileRecordId
//                                    + " or (ObjectId=" + objectId + " and ObjectType=" + objectType + ")" + ", icount=" + icount
//                                    + ", resolved by picking matching ObjectId, ObjectType.  Will now update the chosen record with a new MobileRecordId.");
//
//                            if (StringUtils.isNullOrEmpty(mobileRecordIdPrefix))
//                                mobileRecordIdPrefix = Rms.getMobileRecordId(RecordCommonHelper.getMobileRecordIdPrefix(recordType));
//
//                            if (ixIteration < 0)
//                                setMobileRecordId(mobileRecordIdPrefix);
//                            else
//                                setMobileRecordId(mobileRecordIdPrefix + "." + ixIteration);
//                        }
//                        else if (isMobileRecordIdConflict) {
//                            // By previous logic, isMobileRecordIdConflict == true requires a non-blank passed mobileRecordId parameter.
//                            Log.d(TAG, strThis + "No multiple record conflict found, but MobileRecordId mismatch found, mobileRecordId=" + mobileRecordId
//                                    + ", rmsRecordCommonWork.mobileRecordId=" + getMobileRecordId() + ".  Will update chosen record"
//                                    + " with the searched MobileRecordId: " + mobileRecordId); // i.e. setting the local record MobileRecordId to same as server.
//
//                            setMobileRecordId(mobileRecordId);
//                        }
//
//                        if (isRepairConflict) {
//                            Log.d(TAG, strThis + "Will now repair conflict by updating the chosen record with new MobileRecordId and setting sent to SYNC_STATUS_PENDING_UPDATE if not DELETE so the change will be upsynced."
////                                + " The original sent value will be returned and is: " + recRepairWork.getSyncStatusBeforeRepair()); // Why should we return the original sent value if we are setting the db value to Cadp.SYNC_STATUS_PENDING_UPDATE.
//                                + " The original sent value will be returned and is: " + getSentSyncStatusBeforeRepair()); //  If the record was SYNC_STATUS_DELETE, the calling routine wants to know to delete it rather than upsync it.
//
//
//                            try {
////                                stmtRmsRecordsUpdateMobileRecordIdAndSent.bindString(0, rmsRecordCommonWork.mobileRecordId);
////                                stmtRmsRecordsUpdateMobileRecordIdAndSent.bindString(0, rmsRecordCommonWork.getMobileRecordId());
////                                stmtRmsRecordsUpdateMobileRecordIdAndSent.bindLong(1, Cadp.SYNC_STATUS_PENDING_UPDATE);
////                                stmtRmsRecordsUpdateMobileRecordIdAndSent.bindLong(2, rmsRecordCommonWork.getIdTable());
////                                stmtRmsRecordsUpdateMobileRecordIdAndSent.executeUpdateDelete();
////                                insertOrUpdateCodingDataNoId(rmsRecordCommonWork.idRmsRecords, CMID_MOBILE_RECORDID, rmsRecordCommonWork.mobileRecordId);
////                                insertOrUpdateCodingDataNoId(rmsRecordCommonWork.getIdTable(), CMID_MOBILE_RECORDID, rmsRecordCommonWork.getMobileRecordId());
//                                if (getSentSyncStatus() != Cadp.SYNC_STATUS_MARKED_FOR_DELETE) setSentSyncStatus(Cadp.SYNC_STATUS_PENDING_UPDATE);
//
////                                if (getSentSyncStatusBeforeRepair() != Cadp.SYNC_STATUS_MARKED_FOR_DELETE)
////                                getRecordHelperCommon().updateSentStatusAndMobRecIdFromUpsyncResponse(getIdRecord(), getMobileRecordId(), getSentSyncStatus());
//                                recHelper.updateSentStatusAndMobRecIdFromUpsyncResponse(getIdRecord(), getMobileRecordId(), getSentSyncStatus());
//
//                                setRepaired(true);
//                                Log.d(TAG, strThis + "Conflict record successfully updated with new MobileRecordId, this=" + this);
//                            } catch (Throwable e) {
//                                Log.d(TAG, strThis + "***** Unable to update MobileRecordId of conflict record, this=" + this);
//                            }
//                        }
//                        else
//                            Log.d(TAG, strThis + "***** Warning, non-standard configuration.  Repair mode is disabled, record was not updated, this=" + this + "\n");
//                    } else
//                        Log.d(TAG, strThis + "Case: No conflict was found. returning found (or not found) record information. this=" + this + "\n");
//
//                }
//                else
//                    Log.d(TAG, strThis + "****** Cannot locate any " + getTablename() + " row by MobileRecordId=" + mobileRecordId
//                        + " or (ObjectId=" + objectId + " and ObjectType=" + objectType + ")" + ", icount=" + icount + ".");
//            }
//        } catch (Throwable e) {
//            Log.d(TAG, strThis + "**** Error. ", e);
//            throw e;
//        }
//        finally {
//            if (cursor != null && !cursor.isClosed()) cursor.close();
//        }
//
//        return this;
//    }


    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public long getIdRecord() {
        return idRecord;
    }

    public void setIdRecord(long idRecord) {
        this.idRecord = idRecord;
    }

//    public long getIdRecordType() {
//        return idRecordType;
//    }

    public void setIdRecordType(long idRecordType) {
        this.idRecordType = idRecordType;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        Log.d(TAG, "testingObject: setObjectType: previous: "+this.objectType);
        this.objectType = objectType;
        Log.d(TAG, "testingObject: setObjectType: new: "+this.objectType);
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        Log.d(TAG, "testingObject: setObjectId: previous: "+this.objectId + " setObjectType: old: " + this.objectType);
        this.objectId = objectId;
        Log.d(TAG, "testingObject: setObjectId: new: "+this.objectId + " setObjectType: new: " + this.objectType);
    }

    public String getMobileRecordId() {
        return mobileRecordId;
    }

    public void setMobileRecordId(String mobileRecordId) {
        this.mobileRecordId = mobileRecordId;
    }


    public int getSentSyncStatus() {
        return sentSyncStatus;
    }

    public void setSentSyncStatus(int sentSyncStatus) {
        this.sentSyncStatus = sentSyncStatus;
    }

    public int getSentSyncStatusBeforeRepair() {
        return sentSyncStatusBeforeRepair;
    }

    public void setSentSyncStatusBeforeRepair(int sentSyncStatusBeforeRepair) {
        this.sentSyncStatusBeforeRepair = sentSyncStatusBeforeRepair;
    }

    public boolean isRepaired() {
        return isRepaired;
    }

    public void setRepaired(boolean repaired) {
        isRepaired = repaired;
    }

    public void clearInit(String tablename) {
        this.idRecordType = -1;
        this.idRecord = -1L;
        this.isRepaired = false;
        this.mobileRecordId = null;
        this.objectId = null;
        this.objectType = null;
        this.sentSyncStatusBeforeRepair = -1;
        this.tablename = tablename;
        this.isFound = false;
    }

    //    public abstract IRmsRecordCommon.IRecordDeleter getRecordDeleter();
//    public abstract void updateSentStatusAndMobRecIdFromUpsyncResponse(long idTable, String mobileRecordId, int sentSyncStatus);
//    public abstract int updateRmsRecordStatusObjIdObjType(long idRecord, boolean isValid,
//                                                  int sentSyncStatus,
//                                                  String objectIdOptional, String objectTypeOptional, boolean isUpdateBlankObjectIdObjectType);

    public String toString() {
        return "{"
                + "tablename=" + tablename
                + ", idRecord=" + this.idRecord
                + ", idRecordType=" + this.idRecordType
                + ", recordType=" + this.recordType
                + ", objectType=" + objectType
                + ", objectId=" + this.objectId
                + ", mobileRecordId=" + this.mobileRecordId
                + ", isRepaired=" + this.isRepaired
                + ", isFound=" + isFound
                + ", sentSyncStatus=" + this.sentSyncStatus
                + ", sentSyncStatusBeforeRepair=" + this.sentSyncStatusBeforeRepair
//                + ", objectIdLinked=" + this.objectIdLinked + ", objectTypeLinked=" + this.objectTypeLinked
//                + ", getExtra() is " + (getExtra() == null ? "null" : "not null")
                + "}";
    }

}
