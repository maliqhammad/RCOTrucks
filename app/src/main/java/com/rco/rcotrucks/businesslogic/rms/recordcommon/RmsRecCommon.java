package com.rco.rcotrucks.businesslogic.rms.recordcommon;

import android.util.Log;

import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.utils.StringUtils;

import java.io.Serializable;

import static com.rco.rcotrucks.businesslogic.rms.Rms.getMobileRecordId;
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

public abstract class RmsRecCommon implements IRmsRecordCommon.IRmsRecCommon, Serializable {
    private static String TAG = RmsRecCommon.class.getSimpleName();
    private long idRecord = -1L;
    private long idRecordType = -1L;
    private String objectType;
    private String objectId;
    private String recordId;
    private long rmsTimestamp;
    private String mobileRecordId;
    private String masterBarcode; // not needed for Header-only RMS record types, can be omitted from local db table.
    private byte[] efileContent;
    private long idRmsRecordsLinked;
    private String objectIdLinked;
    private String objectTypeLinked;
    private boolean isValid;
    private int sentSyncStatus;
    private int isEfileContentSent;
    private long localSysTime;
    // ------- work members
    private String recordType;
    private Object extra;
    private int syncErrorCount;
//    private String mobileRecordIdPrefix;

    private String tablename;

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public String getTableName() {
        return tablename;
    }

    public long getIdRecord() {
        return idRecord;
    }

    public void setIdRecord(long idRecord) {
        this.idRecord = idRecord;
    }

    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        Log.d(TAG, "testingObject: setObjectId: previous: " + this.objectId + " setObjectType: old: " + this.objectType);
        this.objectId = objectId;
        Log.d(TAG, "testingObject: setObjectId: new: " + this.objectId + " setObjectType: new: " + this.objectType);
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        Log.d(TAG, "testingObject: setObjectType: previous: " + this.objectType);
        this.objectType = objectType;
        Log.d(TAG, "testingObject: setObjectType: new: " + this.objectType);
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getMobileRecordId() {
        return mobileRecordId;
    }

    public void setMobileRecordId(String mobileRecordId) {
        this.mobileRecordId = mobileRecordId;
    }

    public long getIdRecordType() {
        if (idRecordType < 0 && !StringUtils.isNullOrWhitespaces(objectType)) {
            BusHelperRmsCoding.RmsRecordType rtype = BusinessRules.getMapRecordTypeInfoFromObjectType().get(objectType);
            if (rtype != null) idRecordType = rtype.id;
        }
        return idRecordType;
    }

    public void setIdRecordType(long idRecordType) {
        this.idRecordType = idRecordType;
    }

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public boolean isValid() {
        return isValid;
    }

    public void setIsValid(boolean isValid) {
        this.isValid = isValid;
    }

    @Override
    public int getSentSyncStatus() {
        return sentSyncStatus;
    }

    @Override
    public void setSentSyncStatus(int sentSyncStatus) {
        this.sentSyncStatus = sentSyncStatus;
    }

//    public int getIsSent() { return isSent; }
//
//    public void setIsSent(int isSent) { this.isSent = isSent; }

    public String getMasterBarcode() {
        return masterBarcode;
    }

    public void setMasterBarcode(String masterBarcode) {
        this.masterBarcode = masterBarcode;
    }


    public long getRmsTimestamp() {
        return rmsTimestamp;
    }

    public void setRmsTimestamp(long rmsTimestamp) {
        this.rmsTimestamp = rmsTimestamp;
    }

    public long getLocalSysTime() {
        return localSysTime;
    }

    public void setLocalSysTime(long localSysTime) {
        this.localSysTime = localSysTime;
    }

//    public void setSyncStatusBeforeRepair(int syncStatusBeforeRepair) {
//        this.syncStatusBeforeRepair = syncStatusBeforeRepair;
//    }
//
//    public int getSyncStatusBeforeRepair() { return syncStatusBeforeRepair; }

    public RmsRecCommon() {
    }

    public RmsRecCommon(long idRmsRecords) {
        this.idRecord = idRmsRecords;
    }
//
//    public RmsRecCommon(String mobileRecordId) {
//        this.mobileRecordId = mobileRecordId;
//    }
//
//    public RmsRecCommon(String objectId, String objectType)
//    {
//        this.objectId = objectId;
//        this.objectType = objectType;
//    }

//    public RmsRecCommon(long idRmsRecords, String objectId, String objectType, String mobileRecordId, int syncStatusBeforeRepair) {
//        this(idRmsRecords, objectId, objectType, mobileRecordId, -1L);
//        this.syncStatusBeforeRepair = syncStatusBeforeRepair;
//    }

    //    public RmsRecCommon(long idRecord, String objectId, String objectType, String mobileRecordId, long idRecordType) {
//        this(idRecord, objectId, objectType, mobileRecordId, idRecordType, null,
//                false, null, -1L, null, null, -1, -1L);
//    }
//
//    public RmsRecCommon(long idRecord, String objectId, String objectType, String mobileRecordId,
//                        long idRecordType, String recordType, boolean isRepaired, Object extra,
//                        long idRmsRecordsLinked, String objectIdLinked, String objectTypeLinked, int syncStatusBeforeRepair,
//                        long mobileTimestamp) {
//
//        init(idRecord, objectId, objectType, mobileRecordId, idRecordType, recordType, extra,
//                idRmsRecordsLinked, objectIdLinked, objectTypeLinked, syncStatusBeforeRepair, mobileTimestamp);
//    }
/*
    private long idRecord = -1L;
    private String objectType;
    private String objectId;
    private String recordId;
    private long rmsTimestamp;
    private String mobileRecordId;
    private String masterBarcode; // not needed for Header-only RMS record types, can be omitted from local db table.
    private byte[] efileContent;
    private long idRmsRecordsLinked;
    private String objectIdLinked;
    private String objectTypeLinked;
    private boolean isValid;
    private int sentSyncStatus;
    private int isEfileContentSent;
    private long localSysTime;
    // ------- work members
    private long idRecordType = -1L;
    private String recordType;
    private Object extra;
    private int syncErrorCount;
 */
    public void initCommon(String tablename, long idRecord, long idRecordType, String objectType, String objectId, String recordId, long rmsTimestamp, String mobileRecordId,
                           String masterBarcode, byte[] efileContent,
                           long idRmsRecordsLinked, String objectIdLinked, String objectTypeLinked, boolean isValid, int sentSyncStatus, int isEfileContentSent,
                           long localSysTime,
                           String recordType, Object extra, int syncErrorCount
    ) {
        this.tablename = tablename;
        this.idRecord = idRecord;
        this.idRecordType = idRecordType;
        this.objectType = objectType;
        this.objectId = objectId;
        this.recordId = recordId;
        this.rmsTimestamp = rmsTimestamp;
        this.mobileRecordId = mobileRecordId;
        this.masterBarcode = masterBarcode;
        this.efileContent = efileContent;
        this.idRmsRecordsLinked = idRmsRecordsLinked;
        this.objectIdLinked = objectIdLinked;
        this.objectTypeLinked = objectTypeLinked;
        this.isValid = isValid;
        this.sentSyncStatus = sentSyncStatus;
        this.isEfileContentSent = isEfileContentSent;
        this.localSysTime = localSysTime;
        this.recordType = recordType;
        this.extra = extra;
        this.syncErrorCount = syncErrorCount;
    }

    /**
     * This clears most members but retains member values establishing the record identity (e.g. the table name, record type).
     * The concept is a reusable record, for example for looping.
     */
    public void clearCommon() {
        initCommon(getTableName(), getIdRecord(), getIdRecordType(), getObjectType(), null, null, -1L,
                null, null, null, -1L,
                null, null, true, Cadp.SYNC_STATUS_PENDING_UPDATE, 0, -1L,
                getRecordType(), null, 0);
    }

    public void setExtra(Object extra) {
        this.extra = extra;
    }

    public Object getExtra() {
        return extra;
    }

    public void setLinked(long idRmsRecordsLinked, String objectIdLinked, String objectTypeLinked) {
        this.idRmsRecordsLinked = idRmsRecordsLinked;
        this.objectIdLinked = objectIdLinked;
        this.objectTypeLinked = objectTypeLinked;
    }

    public void setIdRmsRecordsLinked(long idRmsRecordsLinked) {
        this.idRmsRecordsLinked = idRmsRecordsLinked;
    }

    public long getIdRmsRecordsLinked() {
        return idRmsRecordsLinked;
    }

    public String getObjectIdLinked() {
        return objectIdLinked;
    }

    public String getObjectTypeLinked() {
        return objectTypeLinked;
    }


    public int getSyncErrorCount() {
        return syncErrorCount;
    }

    public void setSyncErrorCount(int syncErrorCount) {
        this.syncErrorCount = syncErrorCount;
    }

//    @Override
//    public String getMobileRecordIdPrefix() {
//        if (mobileRecordIdPrefix == null) mobileRecordIdPrefix = RecordCommonHelper.getMobileRecordIdPrefix(getRecordType());
//        return mobileRecordIdPrefix;
//    }

//    @Override
//    public abstract int deleteRmsRecord(DatabaseHelper.TxControl txc);
//        {
//            return BusHelperRmsCoding.instance().deleteRmsRecordAndCodingData(this.getIdTable(), txc);
//        }

//    @Override
//    public abstract int updateRmsRecordsStatus(long idRmsRecords, boolean isValid,
//                                               int sentSyncStatus,
//                                               String objectIdOptional, String objectTypeOptional, boolean isUpdateBlankObjectIdObjectType);
//        {
//            return BusHelperRmsCoding.instance().updateRmsRecordsStatus(this.getIdTable(), isValid, syncStatus, objectId, objectType, isUpdateBlankObjectIdObjectType);
//        }


//        {
//            BusHelperRmsCoding.instance().updateFromUpsyncResponse(this, sentSyncStatus);
//        }

//    @Override
//    public abstract int updateRecordCommon(String objectId, String objectType, boolean isUpdateObjectIdType,
//                                           String recordId, String mobileRecordId, String masterBarcode,
//                                           long rmsTimestamp, byte[] arEfileContent, boolean isValid,
//                                           int sentSyncStatus, boolean isEfileSent, boolean isNewRecord);
//        {
//            return BusHelperRmsCoding.instance().updateRmsRecordCommon(idTable, objectId, objectType, isUpdateObjectIdType, recordId, mobileRecordId,
//                    masterBarcode, rmsTimestamp, arEfileContent, isValid, isSent, isEfileSent, isNewRecord);
//        }

//    @Override
//    public abstract int updateRmsRecordsEfileContent(byte[] arbyteEfileContent, boolean isValid, int syncStatusEfile);
//        {
//            return BusHelperRmsCoding.instance().updateRmsRecordsEfileContent(getIdTable(), arbyteEfileContent, isValid, syncStatusEfile);
//        }

    @Override
    public byte[] getEfileContent() {
        return efileContent;
    }

    public void setEfileContent(byte[] efileContent) {
        this.efileContent = efileContent;
    }


    public int getIsEfileContentSent() {
        return isEfileContentSent;
    }

    public void setIsEfileContentSent(int isEfileContentSent) {
        this.isEfileContentSent = isEfileContentSent;
    }


//    public abstract byte[] fetchEfileContent();
//        {
//            String strThis = "getEfileContent(), ";
//            String strSql = "SELECT EfileContent FROM rmsrecords WHERE Id = ?";
//            String[] arParams = new String[1];
//            Cursor cur = null;
//            byte[] arBytes = null;
//
//            arParams[0] = String.valueOf(getIdTable());
//
//            try {
//                cur = db.getQuery(strSql, arParams); // ------------>
//
//                if (cur.getCount() > 1) Log.d(TAG, strThis
//                        + "**** Major design error. Multiple rmsrecords returned for unique Id:" + getIdTable()
//                        + ", this=" + this);
//
//                while (cur.moveToNext())
//                    arBytes = cur.getBlob(0);
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            } finally {
//                if (cur != null && !cur.isClosed()) cur.close();
//            }
//
//            return arBytes;
//        }

//    @Override
//    public abstract int updateRmsRecordsIsEfileSent(boolean isEfileSent);

//    public abstract int saveRecordToDb(DatabaseHelper.TxControl txc);


//        {
//            return BusHelperRmsCoding.instance().updateRmsRecordsIsEfileSent(getIdTable(), isEfileSent);
//        }

//    /**
//     * Utility method
//     * @param tablename
//     * @return
//     */
//    public String getSqlSearchByMobRecIdObjIdObjType(String tablename) {
//        return "SELECT Id, MobileRecordId, ObjectId, ObjectType, IdRecordType, sent FROM " + tablename
//                + "\n WHERE MobileRecordId = ? OR (ObjectId = ? AND ObjectType = ?)";
//    }
//
//    /**
//     * Utility method
//     * @param tablename
//     * @return
//     */
//    public String getSqlSearchByObjIdObjType(String tablename) {
//        return "SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM " + tablename
//                + "\n WHERE (ObjectId = ? AND ObjectType = ?)";
//    }
//
//    /**
//     * Utility method
//     * @param tablename
//     * @return
//     */
//    public String getSqlSearchByMobRecId(String tablename) {
//        return "SELECT Id, MobileRecordId, ObjectID, ObjectType, IdRecordType, sent FROM " + tablename
//                + "\n WHERE MobileRecordId = ?";
//    }

    public String toString() {
        return "{tablename=" + tablename + ", idRecord=" + this.idRecord + ", mobileRecordId="
                + this.mobileRecordId + ", objectType=" + objectType + ", objectId=" + this.objectId
                + ", idRecordType=" + this.idRecordType + ", recordType=" + this.recordType
//                + ", isRepaired=" + this.isRepaired
                + ", sentSyncStatus=" + this.sentSyncStatus
                + ", objectIdLinked=" + this.objectIdLinked + ", objectTypeLinked=" + this.objectTypeLinked
                + ", getExtra() is " + (getExtra() == null ? "null" : "not null") + "}";
    }
}
