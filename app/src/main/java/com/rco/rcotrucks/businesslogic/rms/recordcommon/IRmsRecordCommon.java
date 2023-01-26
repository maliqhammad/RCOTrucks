package com.rco.rcotrucks.businesslogic.rms.recordcommon;

import android.database.Cursor;

import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.JsonUtils;

import java.util.List;

public class IRmsRecordCommon {

    // ==================================================== Nested Classes / Interfaces
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

    public static interface IRecordDeleter {
        public int deleteRecord (long idRecord, DatabaseHelper.TxControl txc);
    }

//    public static interface IRecRepairWork {
//        public String getTableName();
//        public long getIdRecord();
//        public void setIdRecord(long idRecord);
//        public String getObjectType();
//        public void setObjectType(String objectType);
//        public String getObjectId();
//        public void setObjectId(String objectId);
//        public String getRecordId();
//        public void setRecordId(String recordId);
//        public String getMobileRecordId();
//        public void setMobileRecordId(String mobileRecordId);
//    }

    public static interface IRmsRecIdCommon {
        public String getTableName();
        public long getIdRecord();
        public void setIdRecord(long idRmsRecords);
        public String getObjectType();
        public void setObjectType(String objectType);
        public String getObjectId();
        public void setObjectId(String objectId);
        public String getRecordId();
        public void setRecordId(String recordId);
        public String getMobileRecordId();
        public void setMobileRecordId(String mobileRecordId);
    }

    public static interface IRmsRecCommon extends IRmsRecIdCommon {
        public long getIdRecordType();
        public void setIdRecordType(long idRecordType);
        public String getRecordType();
        public void setRecordType(String recordType);
//        public void setSyncStatusBeforeRepair(int syncStatusBeforeRepair);
//        public int getSyncStatusBeforeRepair();
        public void clear();
        public void setExtra(Object extra);
        public Object getExtra();
        public void setLinked(long idRmsRecordsLinked, String objectIdLinked, String objectTypeLinked);
        public long getIdRmsRecordsLinked();
        public String getObjectIdLinked();
        public String getObjectTypeLinked();
        public boolean isValid();
        public void setIsValid(boolean isValid);
        public int getSentSyncStatus();
        public void setSentSyncStatus(int sentSyncStatus);
        public String getMasterBarcode();
        public void setMasterBarcode(String masterBarcode);
        public long getRmsTimestamp();
        public void setRmsTimestamp(long rmsTimestamp);
        public int getSyncErrorCount();
        public void setSyncErrorCount(int syncErrorCount);
//        public int updateRmsRecordsStatus(long idRmsRecords, boolean isValid,
//                                          int sentSyncStatus,
//                                          String objectIdOptional, String objectTypeOptional, boolean isUpdateBlankObjectIdObjectType);
//        public String getMobileRecordIdPrefix();

//        public void updateFromUpsyncResponse(int sentSyncStatus);
//        public int updateRecordCommon(
//                                            String objectId,
//                                            String objectType,
//                                            boolean isUpdateObjectIdType,
//                                            String recordId,
//                                            String mobileRecordId,
//                                            String masterBarcode,
//                                            long rmsTimestamp,
//                                            byte[] arEfileContent,
//                                            boolean isValid,
//                                            int sentSyncStatus,
//                                            boolean isEfileSent,
//                                            boolean isNewRecord);
//
//        public int updateRmsRecordsEfileContent(byte[] arbyteEfileContent, boolean isValid, int syncStatusEfile);

        public void setEfileContent(byte[] efileContent);
        public byte[] getEfileContent();

//        public byte[] fetchEfileContent();
//        public int updateRmsRecordsIsEfileSent(boolean isEfileSent);

//        public String getSqlSearchByMobRecIdObjIdObjType();
//        public String getSqlSearchByObjIdObjType();
//        public String getSqlSearchByMobRecId();
    }

    public static interface IRmsRecTableRec extends IRmsRecCommon {
        public void initFromCursor(Cursor cur);
        public void initFromJsonRecFiltX(JsonUtils.JsonRecFiltXWrapper jobj);
//        public int updateDb();
//        public int insertDb();
    }

    public static interface IRecordCommonHelperRecTable {

//        public String updateDbRecordsFromJsonMapFormat(
//                JSONArray jaRecordData, boolean isInsert,
//                Map<String, RecRepairWorkRecTable.RecWorkCombo> mapRecWorkCombo) throws Exception ;

        public long insertRecord(RmsRecCommon rec);
        public int updateRecord(RmsRecCommon rec, boolean isSkipDirty);
        public String getTableName();
        public RecRepairWorkRecTable.RecWorkCombo getRecWorkCombo ();
    }

    public static interface IRecordCommonHelper {
        public DatabaseHelper getDb();
        public byte[] fetchEfileContent(String sql, String[] arParams);
        public int deleteRecord(long idRecord, DatabaseHelper.TxControl txc);
        public <K extends IRmsRecCommon> String updateRecordsFromUpsyncResponse(
                String jsonSetterResponse, List<K> listUpSyncRecords, boolean isIgnoreHeaders, RecRepairWork rmsRecordCommonWork);
        public abstract void updateSentStatusAndMobRecIdFromUpsyncResponse(long idTable, String mobileRecordId, int sentSyncStatus);
        public abstract int updateRmsRecordStatusObjIdObjType(long idRecord, boolean isValid,
                                                              int sentSyncStatus,
                                                              String objectIdOptional, String objectTypeOptional, boolean isUpdateBlankObjectIdObjectType);
        public RecRepairWork findRepairRecord(RecRepairWork recRepairWork,
                String mobileRecordId, String objectId, String objectType,
                boolean isRepairConflict, String recordType, int ixIteration, String mobileRecordIdPrefix) throws Exception;

    }

}
