package com.rco.rcotrucks.businesslogic.rms;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteCursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.IRmsRecordCommon;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecRepairWork;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordCommonHelper;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RmsRecCommon;
import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.HttpClient;
import com.rco.rcotrucks.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.Serializable;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static com.rco.rcotrucks.businesslogic.rms.Rms.getMobileRecordId;
import static com.rco.rcotrucks.businesslogic.rms.Rms.serializeListAsCsvPost;

import java.text.SimpleDateFormat;

/**
 * This class extends BusinessRules purely to have access to static db and lock members.   Eventually this
 * code can be migrated into BusinessRules.
 */
public class BusHelperRmsCoding extends RecordCommonHelper {
    private static String TAG = "BusHelperRmsCoding";

    private BusinessRules rules;
    private static BusHelperRmsCoding instance;
    public static final ReentrantLock lock = new ReentrantLock();

    private static final String SQL_CODINGDATA_INSERT_UPDATE_NO_ID = "INSERT OR REPLACE INTO codingdata (IdRmsRecords, CodingMasterId, Value) VALUES (?,?,?)";
    private SQLiteStatement stmtCodingDataInsertUpdateNoId; // not thread safe.

    private static final String SQL_CODINGDATA_INSERT = "INSERT INTO codingdata (IdRmsRecords, CodingMasterId, Value) VALUES (?,?,?)";
    private SQLiteStatement stmtCodingDataInsert; // not thread safe.

    private static final String SQL_CODINGDATA_UPDATE = "UPDATE codingdata SET Value = ? WHERE Id = ?";
    private SQLiteStatement stmtCodingDataUpdate; // not thread safe.

    private static final String SQL_CODINGDATA_DELETE_BY_ID = "DELETE FROM codingdata WHERE Id = ?";
    private SQLiteStatement stmtCodingDataDeleteById; // not thread safe.

    private static final String SQL_CODINGDATA_DELETE_BY_IDREC_CMID = "DELETE FROM codingdata WHERE IdRmsRecords = ? AND CodingMasterId = ?";
    private SQLiteStatement stmtCodingDataDeleteByIdrecCmid; // not thread safe.

    private static final String SQL_RMS_RECORDS_UPDATE_FOR_CODING = "UPDATE RmsRecords SET RecordId = ?, MobileRecordId = ?, MasterBarCode = ?, RmsTimestamp = ?, eFileContent = ?, IsValid = ?, Sent = ?, IsEfileContentSent = ?, LocalSysTime = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateForCoding; // not thread safe.

    private static final String SQL_RMS_RECORDS_UPDATE_FOR_CODING_AND_OBJECTIDTYPE = "UPDATE RmsRecords SET ObjectId = ?, ObjectType = ?, RecordId = ?, MobileRecordId = ?, MasterBarCode = ?, RmsTimestamp = ?, eFileContent = ?, IsValid = ?, Sent = ?, IsEfileContentSent = ?, LocalSysTime = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateForCodingAndObjectIdType; // not thread safe.

    private static final String SQL_RMS_RECORDS_UPDATE_STATUS = "UPDATE RmsRecords SET LocalSysTime = ?, IsValid = ?, Sent = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateStatus; // not thread safe.

    private static final String SQL_RMS_RECORDS_UPDATE_STATUS_OBJIDTYPE = "UPDATE RmsRecords SET LocalSysTime = ?, IsValid = ?, Sent = ?, ObjectId = ?, ObjectType = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateStatusObjIdObjType; // not thread safe.

    private static final String SQL_RMS_RECORDS_UPDATE_EFILE_CONTENT_STATUS = "UPDATE RmsRecords SET LocalSysTime = ?, IsValid = ?, IsEfileContentSent = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateEfileContentStatus; // not thread safe.

    private static final String SQL_RMS_RECORDS_UPDATE_EFILE_CONTENT = "UPDATE RmsRecords SET EfileContent = ?, LocalSysTime = ?, IsEfileContentSent = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateEfileContent; // not thread safe.

    private static final String SQL_RMS_RECORDS_UPDATE_IS_EFILE_SENT = "UPDATE RmsRecords SET IsEfileContentSent = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateIsEfileSent; // not thread safe.

    private static final String SQL_RMS_RECORDS_UPDATE_MOBILERECORDID_SENT = "UPDATE RmsRecords SET MobileRecordId = ?, sent = ? WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsUpdateMobileRecordIdAndSent; // not thread safe.
    /**
     * "CREATE TABLE rmsrecords (" +
     * "Id			                INTEGER NOT NULL PRIMARY KEY, " +
     * "IdRecordType               TEXT, " +
     * "ObjectType                 TEXT, " +
     * "ObjectId                   TEXT, " +
     * "RecordId			        TEXT, " +
     * "MobileRecordId			    TEXT, " +
     * "MasterBarcode              TEXT, " +  // may not need this.  Used to relate details to header rec.
     * "RmsTimestamp		        INTEGER, " + // May need index to help syncing
     * "EfileContent              BLOB, " +
     * "IdRmsRecordsLink           INTEGER," +
     * "IsValid                    INTEGER DEFAULT(0), " +
     * "sent				        INTEGER DEFAULT (0), " + // may need index to help syncing
     * "LocalSysTime		        INTEGER " + // May need index to help syncing
     * ");",
     */
    private static final String SQL_RMS_RECORDS_INSERT
            = "INSERT INTO RmsRecords (IdRecordType, ObjectType, ObjectId, RecordId, MobileRecordId," +
            " MasterBarcode, RmsTimestamp, EfileContent, IdRmsRecordsLink, IsValid, sent, LocalSysTime)" +
            " VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

    private SQLiteStatement stmtRmsRecordsInsert; // not thread safe.

    //    public long createNewRmsRecordMinimal(long idRecordType, String mobileRecordId, String objectType,
//                                          String objectId, boolean isValidated, boolean isSent) {
    private static final String SQL_RMS_RECORDS_INSERT_MINIMAL
            = "INSERT INTO RmsRecords (IdRecordType, MobileRecordId, ObjectType, ObjectId," +
            " IsValid, sent, IsEfileContentSent, LocalSysTime)" +
            " VALUES(?,?,?,?,?,?,?,?)";

    private SQLiteStatement stmtRmsRecordsInsertMinimal; // not thread safe.

    private static String SQL_RMSRECORDS_FIND_ID_BY_OID_OTYPE = "SELECT Id, ObjectId, ObjectType, MobileRecordId FROM RmsRecords WHERE ObjectId = ? AND ObjectType = ?";
    private static String SQL_RMSRECORDS_FIND_ID_BY_MOB_REC_ID = "SELECT Id, ObjectId, ObjectType, MobileRecordId FROM RmsRecords WHERE MobileRecordId = ?";

    private static String SQL_CODINGDATA_DELETE_BY_RMSRECORDSID = "DELETE FROM codingdata WHERE IdRmsRecords = ?";
    private SQLiteStatement stmtCodingDataDeleteByRmsRecordsId; // not thread safe.

    private static String SQL_RMSRECORDS_DELETE_BY_ID = "DELETE FROM rmsrecords WHERE Id = ?";
    private SQLiteStatement stmtRmsRecordsDeleteById; // not thread safe.

//    private Map<String, String> mapCodingDataColumnByDataType;

    public static String CMID_RECORDID;
    public static String CMID_MOBILE_RECORDID;
    public static String CMID_MASTER_BARCODE;
    public static String CMID_RMS_TIMESTAMP;

    public BusHelperRmsCoding(DatabaseHelper db) {
        super(db);
        String strThis = "BusHelperRmsCoding() constructor, ";
        Log.d(TAG, strThis + "start.");

        rules = BusinessRules.instance();
//        this.db = BusinessRules.getDb();

        // pre-compile statements -- not thread safe objects I assume.
        stmtCodingDataInsertUpdateNoId = db.compileStatement(SQL_CODINGDATA_INSERT_UPDATE_NO_ID);
        stmtCodingDataInsert = db.compileStatement(SQL_CODINGDATA_INSERT);
        stmtRmsRecordsUpdateForCoding = db.compileStatement(SQL_RMS_RECORDS_UPDATE_FOR_CODING);
        stmtRmsRecordsUpdateForCodingAndObjectIdType = db.compileStatement(SQL_RMS_RECORDS_UPDATE_FOR_CODING_AND_OBJECTIDTYPE);
        stmtRmsRecordsUpdateStatus = db.compileStatement(SQL_RMS_RECORDS_UPDATE_STATUS);
        stmtRmsRecordsUpdateStatusObjIdObjType = db.compileStatement(SQL_RMS_RECORDS_UPDATE_STATUS_OBJIDTYPE);
        stmtRmsRecordsUpdateEfileContentStatus = db.compileStatement(SQL_RMS_RECORDS_UPDATE_EFILE_CONTENT_STATUS);
        stmtRmsRecordsUpdateIsEfileSent = db.compileStatement(SQL_RMS_RECORDS_UPDATE_IS_EFILE_SENT);
        stmtRmsRecordsInsert = db.compileStatement(SQL_RMS_RECORDS_INSERT);
        stmtRmsRecordsInsertMinimal = db.compileStatement(SQL_RMS_RECORDS_INSERT_MINIMAL);
        stmtCodingDataUpdate = db.compileStatement(SQL_CODINGDATA_UPDATE);
        stmtCodingDataDeleteByRmsRecordsId = db.compileStatement(SQL_CODINGDATA_DELETE_BY_RMSRECORDSID);
        stmtRmsRecordsUpdateEfileContent = db.compileStatement(SQL_RMS_RECORDS_UPDATE_EFILE_CONTENT);
        stmtCodingDataDeleteById = db.compileStatement(SQL_CODINGDATA_DELETE_BY_ID);
        stmtCodingDataDeleteByIdrecCmid = db.compileStatement(SQL_CODINGDATA_DELETE_BY_IDREC_CMID);
        stmtRmsRecordsUpdateMobileRecordIdAndSent = db.compileStatement(SQL_RMS_RECORDS_UPDATE_MOBILERECORDID_SENT);
        stmtRmsRecordsDeleteById = db.compileStatement(SQL_RMSRECORDS_DELETE_BY_ID);

//        instance = this;

        Log.d(TAG, strThis + "End. CMID_MASTER_BARCODE=" + CMID_MASTER_BARCODE + ", CMID_MOBILE_RECORDID=" + CMID_MOBILE_RECORDID
                + ", CMID_RECORDID=" + CMID_RECORDID + ", CMID_RMS_TIMESTAMP=" + CMID_RMS_TIMESTAMP);
    }

    /**
     * "CREATE TABLE recordtypes (" +
     * "Id			                INTEGER NOT NULL PRIMARY KEY, " +
     * "RecordType                 TEXT," +
     * "ObjectType                 TEXT," +
     * "IsEfile                    INTEGER," +
     * "Category                   TEXT," +
     * "IdCategory                 INTEGER" +
     * ");",
     *
     * @return JSON example: [["Signature","NRT353","true","Electronic","2"]]
     */
    public boolean initRecordTypeInfo(String rmsRecordTypesUrlEncoded, boolean isAlreadyInTransaction) {
        Log.d(TAG, "initRecordTypeInfo() Start. isAlreadyInTransaction=" + isAlreadyInTransaction
                + ", RMS_RECORD_TYPES=" + StringUtils.dumpArray(BusinessRules.RMS_RECORD_TYPES));

        Log.d(TAG, "initRecordTypeInfo() rmsRecordTypesUrlEncoded=" + rmsRecordTypesUrlEncoded);

        boolean isSuccess = false;
        SQLiteStatement statement = null;

        try {
            String strResponse = Rms.getRecordTypeInfo(rmsRecordTypesUrlEncoded);
            Log.d(TAG, "initRecordTypeInfo() strResponse=" + strResponse);

            JSONArray jaResultSet = new JSONArray(strResponse);
            Log.d(TAG, "initRecordTypeInfo() jaResultSet.length()=" + jaResultSet.length());

            if (!isAlreadyInTransaction) getDb().beginTransaction();

            getDb().delete("recordtypes");

            Log.d(TAG, "initRecordTypeInfo() after DELETE FROM recordtypes.");

            String sql = "INSERT INTO recordtypes (RecordType, ObjectType, IsEfile, Category, IdCategory) VALUES (?,?,?,?,?)";
            statement = getDb().compileStatement(sql); // Potentially initialize in constructor or some higher level.

//            JSONArray arRecordTypes = jaResultSet.names();
            // [["Signature","NRT353","true","Electronic","2"]]

            for (int i = 0; i < jaResultSet.length(); i++) {
                JSONArray jaRecordType = jaResultSet.getJSONArray(i);
                String rectype = (String) jaRecordType.get(0);
                String objectType = (String) jaRecordType.get(1);
                boolean bIsEfile = StringUtils.isTrue((String) jaRecordType.get(2));
                String category = (String) jaRecordType.get(3);
                String idCategory = (String) jaRecordType.get(4);

                statement.clearBindings();
                statement.bindString(1, rectype);
                statement.bindString(2, objectType);
                statement.bindLong(3, bIsEfile ? 1 : 0);
                statement.bindString(4, category);
                statement.bindString(5, idCategory); // should be okay, will convert to INTEGER.

                statement.executeInsert();
                Log.d(TAG, "initRecordTypeInfo() after executeInsert"
                        + ", rectype=" + rectype
                        + ", objectType=" + objectType
                );
            }

            if (!isAlreadyInTransaction) getDb().setTransactionSuccessful();

            isSuccess = true;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (statement != null)
                statement.close(); // probably not necessary?

            if (!isAlreadyInTransaction) getDb().endTransaction();
        }

        Log.d(TAG, "initRecordTypeInfo() End.");
        return isSuccess;
    }

    public Cursor getRmsRecordCursor(String idRmsRecords, String recordType) {
        String strSql =
                "select cd.Id, cd.Value, cds.CodingFieldName, cds.DisplayName, cds.DataType DataTypeSetup, dt.DataTypeName, cds.ViewType, " +
                        "cm.CodingMasterId, cds.EditMode, cds.IsRequired, cds.CombineType, cds.DisplayPosition \n" +
                        "    from codingdatasetup cds  \n" +
                        "               left outer join codingmasterlookup cm on cm.CodingFieldName = cds.CodingFieldName \n" +
                        "               left outer join codingdata cd on cd.CodingMasterId = cm.CodingMasterId and cd.IdRmsRecords = ? \n" +
                        "               left outer join rmsdatatypes dt on dt.TypeId = cm.DataType \n" +
                        "               left outer join rmsrecords rr on rr.id = cd.IdRmsRecords \n" +
//            "               left outer join recordtypes rt on rt.ObjectType = rr.ObjectType \n" +
                        "    where cds.RecordType = ?" +
                        "    order by cds.DisplayPosition";

        Log.d(TAG, "getRmsRecordCursor() idRmsRecords=" + idRmsRecords + ", recordType=" + recordType + ", strSql=\n" + strSql);

        Cursor cursor = getDb().getQuery(strSql, new String[]{String.valueOf(idRmsRecords), recordType});


        return cursor;
    }

    public Cursor getRmsRecordCursorUpdateForFuel(String idRmsRecords, String recordType) {
        String strSql =
                "select cd.Id, cd.Value, cds.CodingFieldName, cds.DisplayName, cds.DataType DataTypeSetup, dt.DataTypeName, cds.ViewType, " +
                        "cm.CodingMasterId, cds.EditMode, cds.IsRequired, cds.CombineType, cds.DisplayPosition \n" +
                        "    from codingdatasetup cds  \n" +
                        "               left outer join codingmasterlookup cm on cm.CodingFieldName = cds.CodingFieldName \n" +
                        "               left outer join codingdata cd on cd.CodingMasterId = cm.CodingMasterId and cd.IdRmsRecords = ? \n" +
                        "               left outer join rmsdatatypes dt on dt.TypeId = cm.DataType \n" +
                        "               left outer join rmsrecords rr on rr.id = cd.IdRmsRecords \n" +
//            "               left outer join recordtypes rt on rt.ObjectType = rr.ObjectType \n" +
                        "    where cds.RecordType = ?" +
                        "    order by cds.DisplayPosition";

        Log.d(TAG, "getRmsRecordCursor() idRmsRecords=" + idRmsRecords + ", recordType=" + recordType + ", strSql=\n" + strSql);

        Cursor cursor = getDb().getQuery(strSql, new String[]{String.valueOf(idRmsRecords), recordType});

        cursor.moveToFirst();
//        String recordId = c.getString(0);
//        String firstName = c.getString(1);
//        String lastName = c.getString(2);
//        String employeeId = c.getString(3);
//        String itemType = c.getString(4);
//        String truckNumber = c.getString(5);
//        String trailerNumber = c.getString(6);
//        c.moveToFirst();

        int counter=0;
        while (!cursor.isAfterLast()) {
            Log.d(TAG, "getRmsRecordCursorUpdateForFuel: counter: "+counter);
            Log.d(TAG, "getRmsRecordCursorUpdateForFuel: "+cursor.getString(counter));
            counter++;
            cursor.moveToNext();
        }



        return cursor;
    }

    public Bitmap getEfileBitmapByIdRmsRecords(String idRmsRecords) {
        String sql = "SELECT r.Id, r.eFileContent FROM rmsrecords r WHERE r.Id = ?";
        String[] arParams = new String[]{idRmsRecords};
        RmsRecords rmsRecordId = getEfileBitmap(sql, arParams, null);
        Bitmap bitmap = null;
        if (rmsRecordId != null) bitmap = (Bitmap) rmsRecordId.getExtra();
        return bitmap;
    }

    /**
     * cursor from select SQL that returns id, EfileContent of rmsrecords row.
     *
     * @param sql
     * @param arParams
     * @param rmsRecordId RmsRecCommon tbl Id column of row with bitmap.
     * @return should return null if no record found.  May return null bitmap.
     */
    public RmsRecords getEfileBitmap(
            String sql, String[] arParams, RmsRecords rmsRecordId) {

        String strThis = "getEfileBitmap(), ";
        Log.d(TAG, strThis + "Start. sql=\n" + sql + "\n, arParams=" + StringUtils.dumpArray(arParams));

        Cursor cur = null;
        Bitmap bmp = null;
        byte[] arEfileContent = null;
        long idRmsRecords = -1L;
        RmsRecords rmsRecId = null;

        try {

            cur = getDb().getQuery(sql, arParams);

            int iRowCount = 0;

            while (cur.moveToNext()) {
                idRmsRecords = cur.getLong(0);
                arEfileContent = cur.getBlob(1);
                iRowCount++;
                Log.d(TAG, strThis + "Case: moved to next cursor row. idRmsRecords=" + idRmsRecords
                        + ", iRowCount=" + iRowCount);
            }

            Log.d(TAG, strThis + "iRowCount=" + iRowCount);

            if (BuildConfig.DEBUG && iRowCount > 1) {
                Log.e(TAG, strThis + " getEfileBitmapByIdLink() Assertion failed: iRowCount <= 1"
                        + ", sql=" + sql + ", arParams=" + StringUtils.dumpArray(arParams));
//                throw new AssertionError(TAG + " getEfileBitmapByIdLink() Assertion failed: iRowCount <= 1");
            }

            if (idRmsRecords >= 0) {
                if (rmsRecordId == null)
                    rmsRecId = new RmsRecords(idRmsRecords);
                else {
                    rmsRecId = rmsRecordId;
                    rmsRecId.clear();
//                    rmsRecId.idRmsRecords = idRmsRecords;
                    rmsRecId.setIdRecord(idRmsRecords);
                }


                if (arEfileContent != null && arEfileContent.length > 0) {
                    bmp = BitmapFactory.decodeByteArray(arEfileContent, 0, arEfileContent.length);
                    Log.d(TAG, strThis + "Case: found non-empty efile content. Converting to bitmap. arEfileContent.length="
                            + arEfileContent.length + ", bmp.getByteCount()=" + bmp.getByteCount()
                            + ", bmp.getWidth()=" + bmp.getWidth() + ", bmp.getHeight()=" + bmp.getHeight());
                    rmsRecId.setExtra(bmp);
                }
            }
        } catch (Throwable e) {
            Log.d(TAG, strThis + "**** Error. ", e);
        } finally {
            if (cur != null) cur.close();
        }

        return rmsRecId;
    }

    public RmsRecords getEfileBitmapByParent(
            String tagDebug,
            String idRecordTypeEfile, long idRmsRecordsParent, String codingMasterIdParentObjectid, String objIdParent,
            String codingMasterIdParentObjectType, String objTypeParent,
            String codingMasterIdFilterField, String filterValue, RmsRecords rmsRecordId) {

        String strThis = "getEfileBitmapByParent(), ";

        RmsRecords rmsRecId = null;

        if (idRmsRecordsParent >= 0) {
            rmsRecId = getEfileBitmapByIdLink(idRecordTypeEfile, String.valueOf(idRmsRecordsParent), codingMasterIdFilterField, filterValue, rmsRecordId);

            if (rmsRecId == null)
                Log.d(TAG, strThis + "Case: tagDebug=" + tagDebug + ", idRecordTypeEfile=" + idRecordTypeEfile + ", codingMasterIdFilterField=" + codingMasterIdFilterField
                        + ", filterValue=" + filterValue + ", did not find signature record by rmsRecords link to: " + idRmsRecordsParent);
            else
                Log.d(TAG, strThis + "Case: tagDebug=" + tagDebug + ",idRecordTypeEfile=" + idRecordTypeEfile + ", codingMasterIdFilterField=" + codingMasterIdFilterField
                        + ", filterValue=" + filterValue + ", found signature record by rmsRecords link to: " + idRmsRecordsParent);
        }

        if (rmsRecId == null && objIdParent != null && objTypeParent != null) {
            rmsRecId = getEfileBitmapByParentObjIdObjType(idRecordTypeEfile, codingMasterIdParentObjectid, objIdParent,
                    codingMasterIdParentObjectType, objTypeParent, codingMasterIdFilterField, filterValue, rmsRecordId);
            if (rmsRecId == null)
                Log.d(TAG, strThis + "Case: tagDebug=" + tagDebug + ", idRecordTypeEfile=" + idRecordTypeEfile + ", codingMasterIdFilterField=" + codingMasterIdFilterField
                        + ", filterValue=" + filterValue + ", did not find signature record by parent objectId/objectType: " + objIdParent + "/" + objTypeParent);
            else
                Log.d(TAG, strThis + "Case: tagDebug=" + tagDebug + ", idRecordTypeEfile=" + idRecordTypeEfile + ", codingMasterIdFilterField=" + codingMasterIdFilterField
                        + ", filterValue=" + filterValue + ", found signature record by parent objectId/objectType: " + objIdParent + "/" + objTypeParent);
        }

        return rmsRecId;
    }

    public RmsRecords getEfileBitmapByIdLink(
            String idRecordTypeEfile, String idRmsRecordsLink, String codingMasterIdFilterField,
            String filterValue, RmsRecords rmsRecordId) {

        String sql = "SELECT r.Id, r.EfileContent FROM rmsrecords r "
                + "INNER JOIN codingdata cd"
                + " ON cd.IdRmsRecords = r.Id AND cd.codingMasterid = ?"
                + " AND cd.Value = ?"
                + " WHERE r.IdRmsRecordsLink = ? AND r.IdRecordType = ?"
                + " ORDER BY r.LocalSysTime"; // added order by to help conflict resolution if multiple Efiles are associated with a parent.

        String[] arParams = new String[]{codingMasterIdFilterField, filterValue, idRmsRecordsLink,
                idRecordTypeEfile};

        return getEfileBitmap(sql, arParams, rmsRecordId);
    }

    public RmsRecords getEfileBitmapByParentObjIdObjType(
            String idRecordTypeEfile, String codingMasterIdParentObjectid, String objIdParent,
            String codingMasterIdParentObjectType, String objTypeParent,
            String codingMasterIdFilterField, String filterValue, RmsRecords rmsRecordId) {

        String sql = "SELECT r.Id, r.eFileContent FROM rmsrecords r INNER JOIN codingdata cdpid" + "\r\n"
                + " ON cdpid.IdRmsRecords = r.Id AND cdpid.codingMasterid = ?" + "\r\n"
                + " AND cdpid.Value = ?" + "\r\n"// parent objectid join
                + " INNER JOIN codingdata cdptyp" + "\r\n"
                + " ON cdptyp.IdRmsRecords = r.Id AND cdptyp.codingMasterid = ?" + "\r\n"
                + " AND cdptyp.Value = ?" + "\r\n"// parent objecttype join
                + " INNER JOIN codingdata cdf" + "\r\n"
                + " ON cdf.IdRmsRecords = r.Id AND cdf.codingMasterid = ?" + "\r\n"
                + " AND cdf.Value = ?" + "\r\n"// filter field join (typically mechanic or driver)
                + " WHERE r.IdRecordType = ?"
                + " ORDER BY r.LocalSysTime"; // added order by to help conflict resolution if multiple Efiles are associated with a parent.

        String[] arParams = new String[]{
                codingMasterIdParentObjectid, objIdParent,
                codingMasterIdParentObjectType, objTypeParent,
                codingMasterIdFilterField, filterValue,
                idRecordTypeEfile};

        return getEfileBitmap(sql, arParams, rmsRecordId);
    }

    public final Cursor getNewRmsRecordCursor(String recordType) {
        String strSql =
                "select null cdId, null cdValue, cds.CodingFieldName, cds.DisplayName, cds.DataType DataTypeSetup, dt.DataTypeName DataTypeRms, cds.ViewType, " +
                        "cm.CodingMasterId, cds.EditMode, cds.IsRequired, cds.CombineType, cds.DisplayPosition \n" +
                        "  from codingdatasetup cds \n" +
                        "  left outer join codingmasterlookup cm on cm.CodingFieldName = cds.CodingFieldName  \n" +
                        "  left outer join rmsdatatypes dt on dt.TypeId = cm.DataType \n" +
                        "  where cds.RecordType = ? \n" + // example: 'Truck DVIR Detail'
                        "   order by cds.DisplayPosition";

        Log.d(TAG, "getNewRmsRecordSql() recordType=" + recordType + ", strSql=\n" + strSql);
        Cursor cursor = getDb().getQuery(strSql, new String[]{recordType});
        Log.d(TAG, "getNewRmsRecordCursor: cursor: count: "+cursor.getCount());
        return cursor;
    }

    public final Cursor getNewRmsRecordCursorUpdate(String recordType) {
        String strSql =
                "select null cdId, null cdValue, cds.CodingFieldName, cds.DisplayName, cds.DataType DataTypeSetup, dt.DataTypeName DataTypeRms, cds.ViewType, " +
                        "cm.CodingMasterId, cds.EditMode, cds.IsRequired, cds.CombineType, cds.DisplayPosition \n" +
                        "  from codingdatasetup cds \n" +
                        "  left outer join codingmasterlookup cm on cm.CodingFieldName = cds.CodingFieldName  \n" +
                        "  left outer join rmsdatatypes dt on dt.TypeId = cm.DataType \n" +
                        "  where cds.RecordType = ? \n" + // example: 'Truck DVIR Detail'
                        "   order by cds.DisplayPosition";

        Log.d(TAG, "getNewRmsRecordSql() recordType=" + recordType + ", strSql=\n" + strSql);
        Cursor cursor = getDb().getQuery(strSql, new String[]{recordType});

        return cursor;
    }

    public boolean upsyncEfileContent(int batchSize) throws Exception {
        Log.d(TAG, "upsyncEfileContent: batchSize: " + batchSize);
        String strThis = "upsyncEfileContent(, ";
        // 1.  get batch of pending FuelReceipt records to send to the RMS server.
//        List<BusHelperRmsCoding.RmsRecords> list = getPendingEfileContentIds(batchSize);
        List<IRmsRecordCommon.IRmsRecCommon> list = getPendingEfileContentIds(batchSize);
        Log.d(TAG, "upsyncEfileContent: list: " + list);
        if ((list == null || list.size() == 0)) {
            Log.d(TAG, "upsyncEfileContent: list: size: " + list.size());
            return true;
        }

        // 2.  Send pending records to RMS Server
        sendPendingEfileContent(list);

        return false;
    }

    /**
     * Note: this may need to be abstracted to support "record" tables instead of RmsRecCommon.  Or
     * use a model where common codingfields / flags are put in RmsRecCommon and linked to a
     * specific "record" table with columns for specific codingfields.
     * Also move abstracted methods to a new RecordGenericHelper class.
     *
     * @param list
     * @throws Exception
     */
//    public void sendPendingEfileContent(List<RmsRecCommon> list) throws Exception {
    public void sendPendingEfileContent(List<IRmsRecordCommon.IRmsRecCommon> list) throws Exception {
        String strThis = "sendPendingEfileContent(), ";
        Log.d(TAG, strThis + "Start. list.size()" + (list != null ? list.size() : "(NULL)"));

        if (list == null || list.size() == 0) {
            Log.d(TAG, strThis + "case: returning without processing, empty or null list=" + list);
            return;
        }

        HttpClient.HttpReturnInfo response = null;

        String strSql = "SELECT EfileContent FROM rmsrecords WHERE Id = ?";
        String[] arParams = new String[1];
//        Cursor cur = null;
        byte[] arBytes = null;

//        for (RmsRecCommon rmsRecordId : list) {
        for (IRmsRecordCommon.IRmsRecCommon rmsRecordId : list) {
            try {

//                arParams[0] = String.valueOf(rmsRecordId.getIdTable());
//
//                cur = db.getQuery(strSql, arParams); // ------------>
//
//                if (cur.getCount() > 1) Log.d(TAG, strThis
//                        + "**** Major design error. Multiple rmsrecords returned for unique Id:" + rmsRecordId.getIdTable()
//                    + ", rmsRecordId=" + rmsRecordId);
//
//                while (cur.moveToNext())
//                    arBytes = cur.getBlob(0);
//
//                cur.close();
//                arBytes = rmsRecordId.getEfileContent();
                arParams[0] = String.valueOf(rmsRecordId.getIdRecord());
                Log.d(TAG, strThis + "sendPendingEfileContent: rmsRecordId.getIdRecord: " + rmsRecordId.getIdRecord());
                Log.d(TAG, strThis + "sendPendingEfileContent: arParams: " + arParams);
                arBytes = fetchEfileContent(strSql, arParams);
                // Even if bytes are null, we want to send because user may be deleting content.
//                String ext = BusHelperFuelReceipts.getFileExtByRecordType(rmsRecordId.recordType);
                String ext = BusHelperFuelReceipts.getFileExtByRecordType(rmsRecordId.getRecordType());
                String uploadFileName = "setRecordContent_android_signature_" + rules.getAuthenticatedUser().getClientLogon()
                        + "_" + DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z_FOR_NAME)
                        + "." + ext;

                if (arBytes == null || arBytes.length == 0) {
                    Log.d(TAG, strThis + "Case: EfileContent is empty, deleting file version.");
                    response = Rms.deleteFileVersion(rmsRecordId.getObjectId(), rmsRecordId.getObjectType(), "-1");
                    Log.d(TAG, strThis + "Called deleteFileVersion for rmsRecordId=" + rmsRecordId + ", arBytes.length="
                            + (arBytes != null ? arBytes.length : "(NULL)") + ", response=" + response);
                } else {
                    Log.d(TAG, strThis + "Case: EfileContent is not empty, setting record content arBytes.length=" + arBytes.length);
                    response = Rms.setRecordContent(uploadFileName,
                            rmsRecordId.getObjectId(), rmsRecordId.getObjectType(), "-1", "+", "+", arBytes); // -------------------->
                    Log.d(TAG, strThis + "Called setRecordContent for rmsRecordId=" + rmsRecordId + ", arBytes.length="
                            + (arBytes != null ? arBytes.length : "(NULL)") + ", response=" + response);

                }

                if (response.responseCode < 300) {
                    updateRmsRecordsIsEfileSent(rmsRecordId.getIdRecord(), true);
//                    rmsRecordId.updateRmsRecordsIsEfileSent(true);
                    Log.d(TAG, strThis + "updated IsEfileSent to true for rmsrecords row with Id: " + rmsRecordId.getIdRecord());
                } else
                    Log.d(TAG, strThis + "Due to response code: " + response.responseCode
                            + ", did NOT update IsEfileSent to true for rmsrecords row with Id: " + rmsRecordId.getIdRecord());

            } catch (Exception e) {
                Log.d(TAG, strThis + "**** Error processing rmsRecordId=" + rmsRecordId + e, e);
            } finally {
//                if (cur != null && !cur.isClosed()) try {
//                    cur.close();
//                }catch (Throwable e2){
//                    Log.d(TAG, strThis + "**** Error trying to close cursor", e2);
//                }
            }
        }

        Log.d(TAG, strThis + "End. list.size=" + list.size());
    }

    /**
     * initCodingMasterLookup() must be called some time prior to this method.
     */
    public static void initConvenienceMembers() {
        // Initialize some convenience variables and lookup maps.
//        mapCodingDataColumnByDataType = rules.getMapCodingDataColumnByDataTypeLookup();
        Map<String, String> mapCodingCodingMasterIdByName = BusinessRules.getMapCodingMasterIdByName();

        CMID_MASTER_BARCODE = mapCodingCodingMasterIdByName.get("Master Barcode");
        CMID_MOBILE_RECORDID = mapCodingCodingMasterIdByName.get("MobileRecordId");
        CMID_RECORDID = mapCodingCodingMasterIdByName.get("RecordId");
        CMID_RMS_TIMESTAMP = mapCodingCodingMasterIdByName.get("RMS Timestamp");
        Log.d(TAG, "initConvenienceMembers(), " + "CMID_RECORDID=" + CMID_RECORDID + ", CMID_MOBILE_RECORDID=" + CMID_MOBILE_RECORDID
                + ", CMID_MASTER_BARCODE=" + CMID_MASTER_BARCODE + ", CMID_RMS_TIMESTAMP=" + CMID_RMS_TIMESTAMP);
    }

    private String dataTypeBoolean;

    public final String getDataTypeBoolean() {
        if (dataTypeBoolean == null)
//            dataTypeBoolean = getMapDataTypeIdFromDataTypeName().get("Boolean");
            dataTypeBoolean = BusinessRules.getMapDataTypeByName().get(Crms.D_BOOLEAN).typeId;

        return dataTypeBoolean;
    }


    /**
     * @return
     */
    public static synchronized BusHelperRmsCoding instance() {
        Log.d(TAG, "Start. instance(), instance is null ? " + (instance == null));
        if (instance == null) {
            Log.d(TAG, "instance is null, assigning new instance.");
            instance = new BusHelperRmsCoding(RecordRulesHelper.getDb());
        } else Log.d(TAG, "instance is not null, returning it.");

        Log.d(TAG, "End. instance(), instance is null ? " + (instance == null));
        return instance;
    }

    public void addCodingDataSetup(String recordType, String[][] arCodingDataSetup, boolean isAlreadyInTransaction) {
        ContentValues values;
        try {

            if (!isAlreadyInTransaction) getDb().beginTransaction();

            for (int i = 0; i < arCodingDataSetup.length; i++) {
                String[] arRow = arCodingDataSetup[i];
                values = new ContentValues();

                /**
                 * arCodingDataSetup is an array of rows of template codingfield descriptors for the Truck DVIR Detail record.
                 * The columns are as followes:
                 * 0.  Codingfield Rms Name
                 * 1.  Codingfield Rms datatype name.
                 * 2.  Android display type -- can be any code that helps the software display the field, such as a RecyclerView view type integer.
                 * 3.  (Optional if subsequent columns are missing.  Can be blank.)  Android field display name -- sometimes slightly different from the Rms codingfield name.  Defaults to Codingfield name.
                 * 4.  (Optional) EditMode numeric constant - see DvirDtlAdapter EDIT_MODE_.. constants. Can be used by UI code or ignored.
                 * 5.  (Optional) IsRequired - set to 1 if required, blank or 0 if not. Can be used by UI code or ignored.
                 * 6.  (Optional CombineType - set to a Cadp.COMBINE_TYPE_.. value that can be used by UI code to create a composite value from multiple codingfields.
                 * 7.  (Optional) Android display position in a list, sortable.  Defaults to the index of the row in this array.
                 * 8.  (Optional) column relative position in setter CSV file.
                 values.put("RecordType", "Truck DVIR Detail");
                 values.put("CodingFieldName", arRow[0]);
                 */

//                "RecordType                 TEXT, " +
//                        "CodingFieldName            TEXT, " +
//                        "DisplayName                TEXT, " +
//                        "DisplayPosition            REAL, " +
//                        "DataType                   TEXT, " +
//                        "ViewType                   INTEGER, " +
//                        "EditMode                   INTEGER DEFAULT(" + Cadp.EDIT_MODE_EDITABLE + "), " + // see Cadp.EDIT_MODE_... -- may not be used.
//                        "IsRequired                 INTEGER DEFAULT(0), " + // 1=required, 0 not required.  Interpreted by usage, such as user entry, validation, etc.
//                        "CombineType                INTEGER DEFAULT(" + Cadp.COMBINE_TYPE_NONE + "), " +
//                        "SetterCsvColumnSort        INTEGER, " + // for sorting codingfields in a setter CSV file -- can be a relative column position.  May not be used.
//                        "PRIMARY KEY(RecordType,CodingFieldName)" +

                values.put("RecordType", recordType);
                values.put("CodingFieldName", arRow[0]);

                values.put("DataType", arRow[1]);
                values.put("ViewType", arRow[2]);

                if (arRow.length > 3 && arRow[3] != null
                        && arRow[3].length() > 0) {
                    values.put("DisplayName", arRow[3]);
                } else
                    values.put("DisplayName", arRow[0]); // default DisplayName to Codingfield name.

                if (arRow.length > 4 && !StringUtils.isNullOrEmpty(arRow[4]))
                    values.put("EditMode", arRow[4]);

                if (arRow.length > 5 && !StringUtils.isNullOrEmpty(arRow[5]))
                    values.put("IsRequired", arRow[5]);

                if (arRow.length > 6 && !StringUtils.isNullOrEmpty(arRow[6]))
                    values.put("CombineType", arRow[6]);

                if (arRow.length > 7 && !StringUtils.isNullOrEmpty(arRow[7]))
                    values.put("SetterCsvColumnSort", arRow[7]);

                // For now, the order in the initialization array is used as the order in a displayed list.
                values.put("DisplayPosition", i);

                getDb().insert("codingdatasetup", values);
            }

            if (!isAlreadyInTransaction) getDb().setTransactionSuccessful();
        } catch (Throwable e) {
            Log.d(TAG, "addCodingDataSetup() **** Error.", e);
        } finally {
            if (!isAlreadyInTransaction) getDb().endTransaction();
        }
    }

    /**
     * Is there any point in putting this array into database? Do the constant strings take up memory anyway?
     * Maybe move them to strings resource?  Or initialize them from server. rmsdatatypes tbl is useful in some queries.
     * Although this is hard-coded because the datatypes have been unchanged for decades, there is an RMS table
     * holding them that could be synced down.
     */
    public void initRmsDatatypes(boolean isAlreadyInTransaction) {
        String[][] arDatatypes = new String[][]{
                {"1", Crms.D_BOOLEAN, Crms.COL_NUMBERVAL},
                {"2", Crms.D_DATE, Crms.COL_STRING},
                {"3", Crms.D_STRING, Crms.COL_STRING},
                {"4", Crms.D_DECIMAL, Crms.COL_NUMBERVAL},
                {"5", Crms.D_NUMERIC, Crms.COL_NUMBERVAL},
                {"7", Crms.D_SIGNATURE, Crms.COL_SIGNATURE},
                {"8", Crms.D_LIST, Crms.COL_STRING},
                {"9", Crms.D_GROUP, Crms.COL_STRING},
                {"10", Crms.D_HOLD, Crms.COL_NUMBERVAL},
                {"11", Crms.D_RETENTION, Crms.COL_STRING},
                {"12", Crms.D_RETENTION_FORMULA, Crms.COL_STRING},
                {"13", Crms.D_TRIGGER, Crms.COL_STRING},
                {"14", Crms.D_LINK, Crms.COL_STRING},
                {"15", Crms.D_WORKFLOW, Crms.COL_STRING},
                {"16", Crms.D_AUTO, Crms.COL_NUMBERVAL},
                {"21", Crms.D_DATETIME, Crms.COL_STRING}
        };

        ContentValues values;
        try {

            if (!isAlreadyInTransaction) getDb().beginTransaction();

            getDb().delete("rmsdatatypes");

            for (String[] arRow : arDatatypes) {
                values = new ContentValues();

                values.put("TypeId", arRow[0]);
                values.put("DataTypeName", arRow[1]);
                values.put("CodingDataColumn", arRow[2]);

                getDb().insert("rmsdatatypes", values);
            }

            if (!isAlreadyInTransaction) getDb().endTransaction();

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
//            if (statement != null) statement.close(); // probably not necessary?
            if (!isAlreadyInTransaction) getDb().endTransaction();
        }
    }

    public boolean loadDatabaseWithRmsCodingSetupData(String rmsRecordTypesUrlEncoded) {
        String strThis = "loadDatabaseWithRmsCodingSetupData(), ";
        Log.d(TAG, strThis + "Start. rmsRecordTypesUrlEncoded=" + rmsRecordTypesUrlEncoded);

        boolean isAllSuccess = true;

        try {
            getDb().beginTransaction();

            initRmsDatatypes(true);
            initRecordTypeInfo(rmsRecordTypesUrlEncoded, true);
            Log.d(TAG, strThis + "**** Debugging. BusinessRules.getMapRecordTypeInfoFromObjectType()=" + BusinessRules.getMapRecordTypeInfoFromObjectType());
            // Todo: make these calls consistent in terms of error handling.
            isAllSuccess = isAllSuccess && initCodingMasterLookup(rmsRecordTypesUrlEncoded, true); // --------------------->

//            initTruckDvirDetailSetup(true);

            getDb().setTransactionSuccessful(); // This commits the transaction if there were no exceptions

//            initRmsConvenienceMembers();
        } catch (Throwable throwable) {
            isAllSuccess = false;
            Log.w("Throwable:", throwable.getMessage());
        } finally {
            getDb().endTransaction();
        }

        Log.d(TAG, strThis + "End. rmsRecordTypesUrlEncoded=" + rmsRecordTypesUrlEncoded);
        return isAllSuccess;
    }

    /**
     * This is called during down-syncing.
     * "CREATE TABLE codingmasterlookup (" +
     * "codingMasterId             TEXT PRIMARY KEY," +
     * "CodingFieldName            TEXT, " +
     * ");",
     */
    public boolean initCodingMasterLookup(String recordDisplayTypesUrlEncoded, boolean isAlreadyInTransaction) {
        Log.d(TAG, "initCodingMasterLookup() Start.");
        boolean isSuccess = false;
        SQLiteStatement statement = null;

        try {
            String strResponse = Rms.getCodingfieldMasterInfo(recordDisplayTypesUrlEncoded, null);
            Log.d(TAG, "initCodingMasterLookup() strResponse=" + strResponse);

            JSONArray jaResultSet = new JSONArray(strResponse);
            Log.d(TAG, "initCodingMasterLookup() jaResultSet.length()=" + jaResultSet.length());

            if (!isAlreadyInTransaction) getDb().beginTransaction();

            getDb().delete("codingmasterlookup");

            Log.d(TAG, "initCodingMasterLookup() after DELETE FROM codingmasterlookup.");

            String sql = "INSERT INTO codingmasterlookup (CodingMasterId, CodingFieldName, DataType) VALUES (?,?,?)";
            statement = getDb().compileStatement(sql);

            for (int i = 0; i < jaResultSet.length(); i++) {
                JSONArray jaRow = jaResultSet.getJSONArray(i);

                statement.clearBindings();
                statement.bindString(1, jaRow.getString(0));
                statement.bindString(2, jaRow.getString(1));
                statement.bindString(3, jaRow.getString(2));
                statement.executeInsert();
                Log.d(TAG, "initCodingMasterLookup() after executeInsert"
                        + ", jaRow.getString(0)=" + jaRow.getString(0)
                        + ", jaRow.getString(1)=" + jaRow.getString(1)
                        + ", jaRow.getString(2)=" + jaRow.getString(2)
                );
            }

            if (!isAlreadyInTransaction) getDb().setTransactionSuccessful();
            isSuccess = true;

        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (statement != null) statement.close(); // probably not necessary?
            if (!isAlreadyInTransaction) getDb().endTransaction();
        }

        Log.d(TAG, "initCodingMasterLookup() End.");
        return isSuccess;
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
    public String syncUpdateDbRecords(JSONArray jaCodingData, boolean isAlreadyInTransaction, boolean isInsert, RecRepairWork recRepairWork) throws Exception {
        if (!isAlreadyInTransaction)
            return executeTransactionSyncUpdateDbRecords(jaCodingData, isInsert, recRepairWork);
        else return updateDbRecordsFromJsonRawFormat(jaCodingData, isInsert, recRepairWork);

    }

    public String executeTransactionSyncUpdateDbRecords(JSONArray jaCodingData, boolean isInsert, RecRepairWork recRepairWork) {
        Log.d(TAG, "executeTransactionSyncUpdateDbRecords() Start.");
        String maxTimestamp = null;

        try {
            getDb().beginTransaction();
            maxTimestamp = updateDbRecordsFromJsonRawFormat(jaCodingData, isInsert, recRepairWork);
            getDb().setTransactionSuccessful(); // This commits the transaction if there were no exceptions
        } catch (Exception e) {
            Log.w("Exception:", e);
        } finally {
            getDb().endTransaction();
        }
        Log.d(TAG, "executeTransactionSyncUpdateDbRecords() End.");
        return maxTimestamp;
    }

    public static int iDebugCount = 0;

    /**
     * This is the down sync logic for the rmsrecords and codingdata tables using a JSON array
     * of JSON arrays of CodingData rows retrieved from the RMS server.
     *
     * @param jaCodingData
     * @param isInsert      - reserved for optimization when we know we are doing a full down sync on empty tables.
     * @param recRepairWork
     * @throws JSONException
     */
    public String updateDbRecordsFromJsonRawFormat(JSONArray jaCodingData, boolean isInsert, RecRepairWork recRepairWork) throws Exception {
        String strThis = "updateDbRecordsFromJsonRawFormat(), ";

        Log.d(TAG, strThis + "Start.  jaCodingData.length()=" + jaCodingData.length()
                + ", isInsert=" + isInsert + ", iDebugCount=" + iDebugCount);

        String objectType = null;
        String objectId = null;
        boolean isUpdateObjectIdType = true;
        long LidRmsRecords = -1;
        String mobileRecordIdPrefix = null;
        String recordId = null;
        String mobileRecordId = null;
        String masterBarcode = null;
        long rmsTimestamp = -1L;
        long LmaxRmsTimestamp = 0;
//        String strMaxTimestampParts;
//        String strMaxTimestampWork;
//        String rmsTimestampPartsLast = null;
        MaxTimeStamp maxTimeStamp = new MaxTimeStamp();

        boolean isNewRecord = false;
        boolean isValid = true; // We assume (for now) records coming from the RMS Server were validated before upsyncing.
//        boolean isSent = true;  // We assume (for now) records coming from the RMS Server don't need upsyncing unless they have a new MobileRecordId, etc.
        int sentSyncStatus = Cadp.SYNC_STATUS_SENT;
        boolean isSkipMobileRecordId = false; // If a record repair was done, a new MobileRecordId was created and so we need to ignore using the codingfield value we receive.
        RmsRecordType rtype = null;
        byte[] arEfileContent = null;
        String login = rules.getAuthenticatedUser().getLogin();
        String pwd = rules.getAuthenticatedUser().getPassword();
//        RmsRecords recRepairWork = new RmsRecords();
//        RecRepairWork recRepairWork = new RecRepairWork(tablenameRec);
//        long LidRecordType = -1;
        boolean isEfile;

        int ixNewIds = 0;
        int iRecordCount = 0; // for debugging.
        int iCodingFieldCount = 0;
        String value = null;
        Log.d(TAG, strThis + "rules.getMapRecordTypeInfoFromObjectType()=" + rules.getMapRecordTypeInfoFromObjectType());

        for (int i = 0; i < jaCodingData.length(); i++) {

            JSONArray arRow = jaCodingData.getJSONArray(i);

            Log.d(TAG, strThis + "i=" + i + ", arRow=" + arRow.toString()
                    + ", LidRmsRecords=" + LidRmsRecords
                    + ", objectId=" + objectId + ", objectType=" + objectType + ", recordId=" + recordId
                    + ", mobileRecordId=" + mobileRecordId + ", masterBarcode=" + masterBarcode
                    + ", rmsTimestamp=" + rmsTimestamp);

            if (i == 0) {
                Log.d(TAG, strThis + "Skipping first row, should be column headers. i=" + i + ", arRow=" + arRow.toString());
                continue; // first row is column names.
            }
//            else Log.d(TAG, strThis + "Processing row " + i + ", arRow=" + arRow);


            String oid = arRow.getString(0);
            String otype = arRow.getString(1);
            String cmid = arRow.getString(2);
            String dtype = arRow.getString(3);
            String numVal = arRow.getString(4);
            String strVal = arRow.getString(5);
            iCodingFieldCount++;

//            String cdValueColumn = mapCodingDataColumnByDataType.get(dtype);
            String cdValueColumn = BusinessRules.getMapDataTypeById().get(dtype).codingDataColumn;

            if ("String".equals(cdValueColumn)) value = strVal;
            else value = numVal;

            if ((!oid.equals(objectId) || !otype.equals(objectType))) {
                // if objectId is null, we are just beginning and do not have a complete record for updating the rmsrecords table.
                if (objectId != null) {
                    // We have come to the end of a complete record and will update it prior to starting a new record.
                    iCodingFieldCount = 0;

                    if (StringUtils.isNullOrWhitespaces(mobileRecordId)) {
                        mobileRecordId = Rms.getMobileRecordId(mobileRecordIdPrefix) + "." + ixNewIds++;
                        Log.d(TAG, strThis + "Case: End of complete record, mobileRecordId was blank,"
//                                + " assigning a new one.  mobileRecordId=" + mobileRecordId
//                                + ", and did not find existing record by oid=" + oid + ", otype=" + otype
                                + ", recordType: " + rtype.recordTypeName + ", recRepairWork=" + recRepairWork
                                + ", created new mobileRecordId=" + mobileRecordId + " and inserting it as codingfield.");

//                        Log.d(TAG, strThis + "Case: since did not find a MobileRecordId in record JSON, new one was created.  Storing now as codingfield.");
                        insertCodingDataRow(LidRmsRecords, CMID_MOBILE_RECORDID, mobileRecordId);
//                        isSent = false;
                        sentSyncStatus = Cadp.SYNC_STATUS_SENT;
                    } else
                        Log.d(TAG, strThis + "Case: End of complete record, mobileRecordId was NOT blank"
                                + ", mobileRecordId=" + mobileRecordId + ", recordType: " + rtype.recordTypeName + ", recRepairWork=" + recRepairWork);
                    // if eFile type, call RMS to get content file.

                    if (rtype.isEfileType)
                        arEfileContent = Rms.getEfileBytes(objectType, objectId, "-1", login, pwd);

                    // update prior rmsrecord row using current IdRmsRecords.  If record was created on RMS Server,
                    // it will/should not have a mobileRecordId yet, we'll give it one now.

                    try {
                        updateRmsRecordCommon(LidRmsRecords, objectId, objectType, isUpdateObjectIdType, recordId, mobileRecordId,
//                                masterBarcode, rmsTimestamp, arEfileContent, isValid, isSent, true, isNewRecord);
                                masterBarcode, rmsTimestamp, arEfileContent, isValid, sentSyncStatus, true, isNewRecord);
                    } catch (SQLiteConstraintException e) {
                        Log.d(TAG, "updateDbRecordsFromJsonRawFormat: SQLiteConstraintException: exception: " + e.getMessage());
//                        Log.e(TAG, strThis + "**** Data integrity error, probable cause: insert mode and"
//                            + " duplicate MobileRecordIds on server, LidRmsRecords="
//                            + LidRmsRecords + ", objectId=" + objectId + ", objectType=" + objectType + ", recordId=" + recordId
//                            + ", recRepairWork=" + recRepairWork
//                            + " . Will try to update again with new MobileRecordId");

                        try {
                            mobileRecordId = Rms.getMobileRecordId(mobileRecordIdPrefix) + "." + ixNewIds++;
//                            isSent = false;
                            sentSyncStatus = Cadp.SYNC_STATUS_PENDING_UPDATE;
                            updateRmsRecordCommon(LidRmsRecords, objectId, objectType, isUpdateObjectIdType, recordId, mobileRecordId,
//                                    masterBarcode, rmsTimestamp, arEfileContent, isValid, isSent, true, isNewRecord);
                                    masterBarcode, rmsTimestamp, arEfileContent, isValid, sentSyncStatus, true, isNewRecord);

                            insertOrUpdateCodingDataNoId(LidRmsRecords, CMID_MOBILE_RECORDID, mobileRecordId);

                        } catch (Exception exception) {
                            Log.d(TAG, "updateDbRecordsFromJsonRawFormat: SQLiteConstraintException: exception: " + exception.getMessage());
                            Log.e(TAG, strThis + "**** Data integrity error trying new MobileRecordId, unknown cause, LidRmsRecords="
                                    + LidRmsRecords + ", objectId=" + objectId + ", objectType=" + objectType + ", recordId=" + recordId
                                    + ", recRepairWork=" + recRepairWork);
                            exception.printStackTrace();
                        }
//                        e.printStackTrace();
                    } finally {
                    }

                    iRecordCount++;
                }

                // Now we can start processing new record, update current objectiId, objectType.

                objectId = oid;
                objectType = otype;
                isUpdateObjectIdType = false;
                rtype = rules.getMapRecordTypeInfoFromObjectType().get(objectType);
//                LidRecordType = rtype.id;
                recordId = null;
                masterBarcode = null;
                rmsTimestamp = -1L;
                isSkipMobileRecordId = false;
                mobileRecordIdPrefix = RecordCommonHelper.getMobileRecordIdPrefix(rtype.recordTypeName);
//                isSent = true;
                sentSyncStatus = Cadp.SYNC_STATUS_SENT;
                // Our objective is to either find an existing record to update, or create a minimal new one for setting the IdRmsRecords in the codingdata
                // and update the rmsrecord with codingfield or ObjectId info after all the codingfields are processed.  If we know we're starting with
                // a clean database, isInsert should be true and we don't need to look for existing records.  We may need to resolve duplicate
                // MobileRecordId situations.

                String mobRecId = null;
                mobileRecordId = null;
                LidRmsRecords = -1L;

                if (!isInsert) {
                    // Not a forced "insert" mode, so scan ahead for MobileRecordId.  Logic causes this line to be arrived at before storing first codingdata row
                    // and whenever a new record is started and not in forced insert mode.
                    // Todo: potential optimization - try to locate existing record by otype, oid before scan ahead for mobilerecordid.
                    mobRecId = scanForMobileRecordId(i, objectType, objectId, cmid, value, jaCodingData);

                    if (!StringUtils.isNullOrWhitespaces(mobRecId)) {
                        Log.d(TAG, strThis + "Case: we found a MobileRecordId in the JSON codingdata. mobRecId=" + mobRecId + " recordType: " + rtype.recordTypeName
                                + ". Proceeding to search for existing record by MobileRrecordId.");
                        mobileRecordId = mobRecId;

//                        recRepairWork = findRmsRecordByMobRecId(mobRecId, recRepairWork);
//                        recRepairWork = recRepairWork.findRepairRecord(mobRecId, objectId, objectType, true,
//                                rtype.recordTypeName, ixNewIds, mobileRecordIdPrefix);
                        recRepairWork = findRepairRecord(recRepairWork, mobRecId, objectId, objectType, true,
                                rtype.recordTypeName, ixNewIds, mobileRecordIdPrefix);

                        if (recRepairWork.isRepaired()) {
//                            mobileRecordId = recRepairWork.mobileRecordId;
                            mobileRecordId = recRepairWork.getMobileRecordId();
                            isSkipMobileRecordId = true;
//                            isSent = false;
                            sentSyncStatus = Cadp.SYNC_STATUS_PENDING_UPDATE;
                        }

//                        LidRmsRecords = recRepairWork.idRmsRecords;
                        LidRmsRecords = recRepairWork.getIdRecord();

                        if (LidRmsRecords < 0)
                            Log.d(TAG, strThis + "Case: could not locate existing record by mobRecId=" + mobRecId + " found MobileRecordId in JSON, recordType: " + rtype.recordTypeName
                                    + ", assigned mobileRecordId=" + mobileRecordId + ", after searching rmsrecords for mobileRecordId, LidRmsRecords="
                                    + LidRmsRecords);
                        else {
                            Log.d(TAG, strThis + "Case: found existing record by mobRecId=" + mobRecId + " found MobileRecordId in JSON, recordType: " + rtype.recordTypeName
                                    + ", assigned mobileRecordId=" + mobileRecordId + ", after searching rmsrecords for mobileRecordId, LidRmsRecords="
                                    + LidRmsRecords);

                            // Validations -- (are these validations obsolete now that we have findRepairRecord()? -RAN 5/27/2021
                            if ((recRepairWork.getObjectType() != null && !otype.equals(recRepairWork.getObjectType()))
                                    || (recRepairWork.getObjectId() != null && !oid.equals(recRepairWork.getObjectId())))
                                Log.d(TAG, strThis + "********** Design error - record on server with: mobRecId=" + mobRecId + ", objectId=" + objectId
                                        + ", objectType=" + objectType + " does not have same ObjectId, ObjectType as local database record with same MobileRecordId, recRepairWork=" + recRepairWork);

                            if (recRepairWork.getObjectType() == null || recRepairWork.getObjectId() == null) {
                                Log.d(TAG, strThis + "record on server with: mobRecId=" + mobRecId + ", objectId=" + objectId
                                        + ", objectType=" + objectType + " has blank ObjectId or ObjectType in local database record with same MobileRecordId, recRepairWork=" + recRepairWork
                                        + ". Will update the ObjectId, ObjectType columns.");
                                isUpdateObjectIdType = true;
                            }
                        }
                    } else {
                        Log.d(TAG, strThis + "Case: **** Error - scan ahead encountered record JSON with no MobileRecordId. Starting row at i=" + i + ", starting arRow=" + arRow);
                        Log.d(TAG, strThis + "Case: no MobileRecordId found in the JSON codingdata. mobRecId=" + mobRecId + " recordType: " + rtype.recordTypeName);
                    }

//                    if (StringUtils.isNullOrWhitespaces(mobRecId)) {
                    if (LidRmsRecords < 0) {
                        Log.d(TAG, strThis + "Case: have not identified an existing record yet by MobileRecordId. mobRecId=" + mobRecId + " recordType: " + rtype.recordTypeName
                                + ".  Proceeding to search by ObjectId, ObjectType, objectId=" + objectId + ", objectType=" + objectType);
                        // In case of bad data on server where record has missing MobileRecordId or insert case where we don't look, search for the record by objectId, objectType.
                        // Todo: Could this be a duplicate search if findRepairRecord was done and search found nothing? -RAN 5/27/2021
                        recRepairWork = findRmsRecordByObjIdObjType(objectId, objectType, recRepairWork);

//                        if (recRepairWork.idRmsRecords >= 0) {
                        if (recRepairWork.getIdRecord() >= 0) {
                            Log.d(TAG, strThis + "Case: ***** Warning.  Suspect server data corrupt.  mobRecId=" + mobRecId
                                    + ", but found existing record by objectId=" + objectId + ", objectType=" + objectType
                                    + ", recordType: " + rtype.recordTypeName + ", recRepairWork=" + recRepairWork
                                    + ", mobileRecordId=" + mobileRecordId);

//                            if (mobileRecordId != null && !mobileRecordId.equals(recRepairWork.mobileRecordId)) {
                            if (mobileRecordId != null && !mobileRecordId.equals(recRepairWork.getMobileRecordId())) {
                                Log.d(TAG, strThis + "Case: ***** Warning.  Suspect server data or local data corrupt.  Could not find record by mobRecId=" + mobRecId
                                        + ", but found existing record by objectId=" + objectId + ", objectType=" + objectType
//                                        + " and it has a different MobileRecordId=" + recRepairWork.mobileRecordId
                                        + " and it has a different MobileRecordId=" + recRepairWork.getMobileRecordId()
                                        + ". Will use the one from the server.  recordType: " + rtype.recordTypeName + ", recRepairWork=" + recRepairWork
                                        + ", mobileRecordId=" + mobileRecordId);
                            }

//                            LidRmsRecords = recRepairWork.idRmsRecords;
                            LidRmsRecords = recRepairWork.getIdRecord();

                            if (StringUtils.isNullOrWhitespaces(mobileRecordId)) {
                                // Todo: Why do we assume this is a valid mobileRecordId?  What if recRepairWork did not find a record?  Is that Okay to assign a null?
                                mobileRecordId = recRepairWork.getMobileRecordId();
//                                isSent = false; // If the server didn't have a MobileRecordId, we should update it.
                                sentSyncStatus = Cadp.SYNC_STATUS_PENDING_UPDATE;
                            }

                        } else {
//                            mobileRecordId = null;
                            Log.d(TAG, strThis + "Case: ***** Note:  Server data may have been created by another device.  mobRecId=" + mobRecId
                                    + ", and did not find existing record by objectId=" + objectId + ", objectType=" + objectType
                                    + ", recordType: " + rtype.recordTypeName + ", recRepairWork=" + recRepairWork
                                    + ", mobileRecordId=" + mobileRecordId);
                        }

//                        mobRecId = null;
//                        Log.d(TAG, strThis + "Case: mobRecId=" + mobRecId + ", did not find or look for a MobileRecordId in JSON, recordType: " + rtype.recordTypeName
//                                + ", will create new mobileRecordId later, mobileRecordId=" + mobileRecordId);
                    }
                }


                if (LidRmsRecords < 0) {
                    // Create minimal new record to be updated after codingfields are processed.
                    if (mobileRecordId != null)
                        Log.d(TAG, strThis + "***** Assertion error.  Case: LidRmsRecords < 0, isInsert="
                                + isInsert + ", expected mobileRecordId to be null but =" + mobileRecordId
                                + ", objectId=" + objectId + ", objectType=" + objectType);

                    LidRmsRecords = createNewRmsRecordMinimal(rtype.id, mobileRecordId, objectType, objectId, true, true, true);
                    isNewRecord = true;

                    Log.d(TAG, strThis + "Case: did not find existing record to update either because isInsert is true or searches by MobileRecord and ObjectId, ObjectType"
                            + " came up empty."
                            + " isInsert=" + isInsert + ", recordType: " + rtype.recordTypeName
                            + ", created new record with LidRmsRecords=" + LidRmsRecords + ", mobileRecordId=" + mobileRecordId + ", mobRecId=" + mobRecId
                            + ", objectType=" + objectType + ", objectId=" + objectId);
                    // if scan for mobileRecordId codingfield didn't find one,
                    // then we created we end up in this block creating a new record with the new or unmatched MobilRecordId.
                    // We now must store a new MobileRecordId as a codingfield in the codingdata tbl
                    // since it won't happen automatically because MobileRecordId not found on Server.
//                    if (mobRecId == null) {
//                        Log.d(TAG, strThis + "Case: since did not find a MobileRecordId in record JSON, new one was created.  Storing now as codingfield.");
//                        insertCodingDataRow(LidRmsRecords, CMID_MOBILE_RECORDID, mobileRecordId);
//                    }
                } else {
                    Log.d(TAG, strThis + "Case: found existing record to update, recordType: " + rtype.recordTypeName
                            + ", isInsert=" + isInsert + ", LidRmsRecords=" + LidRmsRecords + ", mobileRecordId=" + mobileRecordId + ", mobRecId=" + mobRecId
                            + ", oid=" + oid + ", otype=" + otype + ", objectId=" + objectId + ", objectType=" + objectType + ", recRepairWork=" + recRepairWork);

                    if (BuildConfig.DEBUG && isInsert == true) {
                        throw new AssertionError("Assertion failed that isInsert is false.");
                    }

                    isNewRecord = false;
                    // Delete existing codingdata so we can put in new.  Need to do this because blank strings or false booleans
                    // do not exist on RMS server, so we would need to detect their absence somehow and delete them or set them to blank or z 0.
                    // For now, will standardize on mirroring the RMS Server, blank strings or false booleans will have no row in codingdata table.
                    // This may not be the most efficient way to update existing records, depending on SQLite delete, inserts vs updates.  Updating
                    // would probably involve fetching the existing codingfields and checking off the ones that are represented in the JSON string.
                    // The ones not represented should be deleted from the database (or using a different model, set to blank for strings, 0 for numbers
                    // and false booleans).
                    deleteRecordCodingData(LidRmsRecords);
                }
            }

            Log.d(TAG, strThis + "CMID_RECORDID=" + CMID_RECORDID + ", CMID_MOBILE_RECORDID=" + CMID_MOBILE_RECORDID
                    + ", CMID_MASTER_BARCODE=" + CMID_MASTER_BARCODE + ", CMID_RMS_TIMESTAMP=" + CMID_RMS_TIMESTAMP
                    + ", isSkipMobileRecordId=" + isSkipMobileRecordId);

            if (CMID_RECORDID.equals(cmid)) recordId = value;
            else if (!isSkipMobileRecordId && CMID_MOBILE_RECORDID.equals(cmid))
                mobileRecordId = value;
            else if (CMID_MASTER_BARCODE.equals(cmid)) masterBarcode = value;
            else if (CMID_RMS_TIMESTAMP.equals(cmid)) {
                rmsTimestamp = (!StringUtils.isNullOrWhitespaces(value) ? Long.parseLong(value) : -1L);
                maxTimeStamp.updateMaxTimestamp(rmsTimestamp, Long.parseLong(objectId), objectType);
//
                Log.d(TAG, strThis + "Case: RmsTimestamp codingfield: iRecordCount=" + iRecordCount
                        + " iDebugCount=" + iDebugCount + ", value=" + value
                        + ", LmaxRmsTimestamp=" + LmaxRmsTimestamp +
                        ", rmsTimestamp=" + rmsTimestamp
                        + ", maxTimeStamp.getMaxtimestampCsv()=[" + maxTimeStamp.getMaxtimestampCsv() + "]"
//                        + ", rmsTimestampPartsLast=" + rmsTimestampPartsLast
                        + ", rtype=" + rtype);
            }

            // update or create record codingfield for current record;
            insertCodingDataRow(LidRmsRecords, cmid, value);
        }

        Log.d(TAG, strThis + "------- Finished looping over codingdata rows, " + iRecordCount + " records saved so far.  "
                + "Proceeding to process final record. LidRmsRecords=" + LidRmsRecords
                + ", iCodingFieldCount=" + iCodingFieldCount);

        if (objectId != null) {
            // We have come to the end of the final complete record.

//            isSent = true; // If coming from server, assume no need to sync up until modified.
            sentSyncStatus = Cadp.SYNC_STATUS_SENT; // If coming from server, assume no need to sync up until modified.
            if (StringUtils.isNullOrWhitespaces(mobileRecordId)) {
//                Log.d(TAG, strThis + "***** Design error.  mobileRecordId is blank. jaCodingData=" + jaCodingData.toString());
                mobileRecordId = Rms.getMobileRecordId(mobileRecordIdPrefix) + "." + ixNewIds++;
                Log.d(TAG, strThis + "Case: mobileRecordId=" + mobileRecordId
                        + ", and did not find existing record by objectId=" + objectId + ", objectType=" + objectType
                        + ", recordType: " + rtype.recordTypeName + ", recRepairWork=" + recRepairWork
                        + ", created new mobileRecordId=" + mobileRecordId);

                Log.d(TAG, strThis + "Case: since did not find a MobileRecordId in record JSON, new one was created.  Storing now as codingfield.");
                insertCodingDataRow(LidRmsRecords, CMID_MOBILE_RECORDID, mobileRecordId);
//                isSent = false; // We need to upsync this record for MobileRecordId
                sentSyncStatus = Cadp.SYNC_STATUS_PENDING_UPDATE; // We need to upsync this record for MobileRecordId
            }

            if (rtype.isEfileType)
                arEfileContent = Rms.getEfileBytes(objectType, objectId, "-1", login, pwd);

            Log.d(TAG, "updateDbRecordsFromJsonRawFormat: objectId: " + objectId + " objectType: " + objectType + " call: updateRmsRecordForCoding(): ");
            updateRmsRecordCommon(LidRmsRecords, objectId, objectType, isUpdateObjectIdType, recordId, mobileRecordId,
//                    masterBarcode, rmsTimestamp, arEfileContent, isValid, isSent, true, isNewRecord);
                    masterBarcode, rmsTimestamp, arEfileContent, isValid, sentSyncStatus, true, isNewRecord);

            ++iRecordCount;
        }

        iDebugCount++;

        Log.d(TAG, strThis + "End. iRecordCount=" + iRecordCount
                        + " iDebugCount=" + iDebugCount
//                + ", LmaxRmsTimestamp=" + LmaxRmsTimestamp
                        + ", rmsTimestamp=" + rmsTimestamp
//                + ", rmsTimestampPartsLast=" + rmsTimestampPartsLast
                        + ", maxTimeStamp.getMaxtimestampCsv()=[" + maxTimeStamp.getMaxtimestampCsv() + "]"
        );

//        return String.valueOf(LmaxRmsTimestamp);
        return maxTimeStamp.getMaxtimestampCsv();
    }

    public int deleteRmsRecordAndCodingData(long idRmsRecords, DatabaseHelper.TxControl txc) {
        int iUpdated = 0;

        if (txc.isUseTransaction()) getDb().beginTransaction();

        try {
            stmtRmsRecordsDeleteById.bindLong(1, idRmsRecords);
            iUpdated = stmtCodingDataDeleteById.executeUpdateDelete();
            iUpdated += deleteRecordCodingData(idRmsRecords);
            if (txc.isUseTransaction()) getDb().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (txc.isUseTransaction()) getDb().endTransaction();
        }

        return iUpdated;
    }

    public int deleteRecordCodingData(long idRmsRecords) {
        stmtCodingDataDeleteByRmsRecordsId.bindLong(1, idRmsRecords);
        int iUpdated = stmtCodingDataDeleteByRmsRecordsId.executeUpdateDelete();
        Log.d(TAG, "deleteRecordCodingData() Case: updating existing record from RMS Server.  Deleted " + iUpdated + " old record rows from codingdata tbl for record with idRmsRecords=" + idRmsRecords);
        return iUpdated;
    }


    /**
     * Todo: under development. -RAN 12/21/2020.
     *
     * @param jaCodingData
     * @param isInsert
     * @param recRepairWork
     * @throws JSONException
     */
    @Deprecated
    public void updateDbRecordsFromJsonMapFormat(JSONArray jaCodingData, boolean isInsert, RecRepairWork recRepairWork) throws JSONException {
        String strThis = "updateDbRecordsFromJsonRawFormat(), ";
        Log.d(TAG, strThis + "Start.  jaCodingData.length()=" + jaCodingData.length()
                + ", iDebugCount=" + iDebugCount);
        long Lid = -1;
        String objectType = null;
        String objectId = null;
        long LidRmsRecords = -1;
        String mobileRecordIdPrefix = null;
        String recordId = null;
        String mobileRecordId = null;
        String masterBarcode = null;
        long rmsTimestamp = -1L;
        boolean isNewRecord = false;
        final boolean isValid = true; // We assume (for now) records coming from the RMS Server were validated before upsyncing.
//        long LidRecordType = -1;
        RmsRecordType rtype = null;
        String[] arMobileRecordIdHolder = null;
        byte[] arEfileContent = null;
        String login = rules.getAuthenticatedUser().getLogin();
        String pwd = rules.getAuthenticatedUser().getPassword();
//        RmsRecords recRepairWork = new RmsRecords();
//        RecRepairWork recRepairWork = new RecRepairWork(tablenameRec);

        for (int i = 0; i < jaCodingData.length(); i++) {

            // Todo: replace arRow with JSONObject that is essentially a map of codingfield name/value pairs.
            // Todo: need inner loop iterating over JSONObject elements.  We can pull out the MobileRecordId first to find or create the record.
            JSONArray arRow = jaCodingData.getJSONArray(i);

//            Log.d(TAG, strThis + "i=" + i + ", arRow=" + arRow.toString()
//                    + ", objectId=" + objectId + ", objectType=" + objectType + ", recordId=" + recordId
//                    + ", " + ", mobileRecordId=" + mobileRecordId + ", masterBarcode=" + masterBarcode
//                    + ", rmsTimestamp=" + rmsTimestamp);

            if (i == 0) continue; // first row is column names.


            String oid = arRow.getString(0);
            String otype = arRow.getString(1);
            String cmid = arRow.getString(2);
            String dtype = arRow.getString(3);
            String numVal = arRow.getString(4);
            String strVal = arRow.getString(5);
            String value = null;


//            String cdValueColumn = mapCodingDataColumnByDataType.get(dtype);
            String cdValueColumn = BusinessRules.getMapDataTypeById().get(dtype).codingDataColumn;
            if ("String".equals(cdValueColumn)) value = strVal;
            else value = numVal;

            if ((!oid.equals(objectId) || !otype.equals(objectType))) {
                if (objectId != null) {
                    // We have come to the end of a complete record and have started a new record..

                    // if eFile type, call RMS to get content file.
                    if (rtype.isEfileType)
                        arEfileContent = Rms.getEfileBytes(objectType, objectId, "-1", login, pwd);

                    // update prior rmsrecord row using current IdRmsRecords.  If record was created on RMS Server,
                    // it will/should not have a mobileRecordId yet, we'll give it one now.

                    updateRmsRecordCommon(LidRmsRecords, objectId, objectType, true, recordId, mobileRecordId,
//                            masterBarcode, rmsTimestamp, arEfileContent, isValid, true, true, isNewRecord);
                            masterBarcode, rmsTimestamp, arEfileContent, isValid, Cadp.SYNC_STATUS_SENT, true, isNewRecord);
                }

                // update current objectiId, objectType.

                objectId = oid;
                objectType = otype;
                rtype = rules.getMapRecordTypeInfoFromObjectType().get(objectType);
//                LidRecordType = rtype.id;
                recordId = null;
//                mobileRecordId = null;
                masterBarcode = null;
                rmsTimestamp = -1L;
                mobileRecordIdPrefix = RecordCommonHelper.getMobileRecordIdPrefix(rtype.recordTypeName);

                // Scan ahead for MobileRecordId.  Logic causes this line to be arrived at before storing first codingdata row
                // and whenever a new record is encountered.
                // Todo: we won't need to scan ahead for MobileRecordId, can directly access from JSON object.
                mobileRecordId = scanForMobileRecordId(i, otype, oid, cmid, value, jaCodingData);
                arMobileRecordIdHolder = new String[]{mobileRecordId};

                // find or create current rmsrecord row, return current recordid.
                // We assume if coming from RMS Server it is a "validated" record and can be sent back as is.
                // Todo: should always we make this validated or not?  Theoretically, we might want to upsync new mobileRecordId.

                // alt code to find or create, more clear?
                LidRmsRecords = -1L;

                if (!StringUtils.isNullOrWhitespaces(mobileRecordId))
                    mobileRecordId = Rms.getMobileRecordId(mobileRecordIdPrefix);
                else {
//                    LidRmsRecords = findRmsRecordByMobRecId(mobileRecordId, recRepairWork).idRmsRecords;
                    LidRmsRecords = findRmsRecordByMobRecId(mobileRecordId, recRepairWork).getIdRecord();
                }

                if (LidRmsRecords < 0) {
                    LidRmsRecords = createNewRmsRecordMinimal(rtype.id, mobileRecordId, otype, oid, isValid, true, true);
                    isNewRecord = true;
                } else {
                    isNewRecord = false;
                    // delete existing codingfields because blank or false codingfields will be missing from down-synced record.
                    deleteRecordCodingData(LidRmsRecords);
                }

                // if scan for mobileRecordId codingfield didn't find one, then arMobileRecordIdHolder will have a newly generated one, which
                // we now must store as a codingfield since it won't happen automatically.
                if (StringUtils.isNullOrWhitespaces(mobileRecordId)) {
                    mobileRecordId = arMobileRecordIdHolder[0];
                    insertCodingDataRow(LidRmsRecords, CMID_MOBILE_RECORDID, mobileRecordId);
                }

            }

            if (CMID_RECORDID.equals(cmid)) recordId = value;
//            else if (CMID_MOBILE_RECORDID.equals(cmid)) mobileRecordId = value;
            else if (CMID_MASTER_BARCODE.equals(cmid)) masterBarcode = value;
            else if (CMID_RMS_TIMESTAMP.equals(cmid))
                rmsTimestamp = (!StringUtils.isNullOrWhitespaces(value) ? Long.parseLong(value) : -1L);

            // update or create record codingfield for current record;
            insertOrUpdateCodingDataNoId(LidRmsRecords, cmid, value);
        }

        if (objectId != null) {
            // We have come to the end of the final complete record.
            // mobileRecordId should have value, always created or found at beginning of each record.

            // if eFile type, call RMS to get content file.
            if (rtype.isEfileType)
                arEfileContent = Rms.getEfileBytes(objectType, objectId, "-1", login, pwd);

            updateRmsRecordCommon(LidRmsRecords, objectId, objectType, true, recordId, mobileRecordId,
//                    masterBarcode, rmsTimestamp, arEfileContent, isValid, true, true, isNewRecord);
                    masterBarcode, rmsTimestamp, arEfileContent, isValid, Cadp.SYNC_STATUS_SENT, true, isNewRecord);
        }

        Log.d(TAG, strThis + "End." + " iDebugCount=" + iDebugCount);
        iDebugCount++;
    }

    public long insertOrUpdateCodingDataNoId(long lidRmsRecords, String cmid, String value) {
        String strThis = "insertOrUpdateCodingDataNoId(), ";
        long Lid;
        if (!StringUtils.isNullOrWhitespaces(value)) {
            stmtCodingDataInsertUpdateNoId.clearBindings();
            stmtCodingDataInsertUpdateNoId.bindLong(1, lidRmsRecords);
            stmtCodingDataInsertUpdateNoId.bindString(2, cmid);
            stmtCodingDataInsertUpdateNoId.bindString(3, value);

            Lid = stmtCodingDataInsertUpdateNoId.executeInsert();
            Log.d(TAG, "Case value non-empty, inserted or updated row with Lid: " + Lid + ", LidRmsRecords=" + lidRmsRecords + ", cmid=" + cmid + ", value=" + value);
        } else {
            stmtCodingDataDeleteByRmsRecordsId.clearBindings();
            stmtCodingDataDeleteByRmsRecordsId.bindLong(1, lidRmsRecords);
            stmtCodingDataDeleteByRmsRecordsId.bindString(2, cmid);
            int iUpdated = stmtCodingDataDeleteByRmsRecordsId.executeUpdateDelete();
            Lid = -1L;
            Log.d(TAG, "Case: value is empty, deleted " + iUpdated + " rows with LidRmsRecords=" + lidRmsRecords + ", cmid=" + cmid + ", value=" + value);
        }

        return Lid;
    }

    public long insertCodingDataRow(long lidRmsRecords, String cmid, String value) {
        String strThis = "insertCodingDataRow(), ";
        long Lid;
        // By design, try to keep empty codingfields out of the table, like on the RMS server.
        if (!StringUtils.isNullOrWhitespaces(value)) {
            stmtCodingDataInsert.clearBindings();
            stmtCodingDataInsert.bindLong(1, lidRmsRecords);
            stmtCodingDataInsert.bindString(2, cmid);

            bindVal(stmtCodingDataInsert, value, 3);

            Lid = stmtCodingDataInsert.executeInsert();

            Log.d(TAG, strThis + " inserted row with Lid: " + Lid + ", LidRmsRecords=" + lidRmsRecords + ", cmid=" + cmid + ", value=" + value);
        } else {
            Lid = -1;
            Log.d(TAG, strThis + " **** empty value, skipping inserting row with LidRmsRecords=" + lidRmsRecords + ", cmid=" + cmid + ", value=" + value + ", Lid=" + Lid);
        }

        return Lid;
    }

    //    public RmsRecords saveRecord(String tablenameRec, long idRmsRecords, long idRecordType, String recordTypeName,
    public RecRepairWork saveRecord(String tablenameRec, long idRmsRecords, long idRecordType, String recordTypeName,
                                    String objectTypeRecord, String objectIdRecord, String recordId,
                                    String masterBarcode, long rmsTimestamp,
                                    byte[] arEfileContent, long idRmsRecordsLink,
                                    int loopIndexOptional,
                                    Map<String, String> mapCodingData, DatabaseHelper.TxControl txc, boolean isDeleteCoding) {

        String strThis = "saveRecord(), ";
        Log.d(TAG, strThis + "Start. idRmsRecords=" + idRmsRecords + ", objectIdRecord=" + objectIdRecord
                + ", objectTypeRecord=" + objectTypeRecord + ", idRecordType=" + idRecordType
                + ", recordTypeName=" + recordTypeName + ", loopIndexOptional=" + loopIndexOptional
                + "mapCodingData.size()=" + (mapCodingData != null ? mapCodingData.size() : "(NULL)")
                + ", txc=" + txc);

//        RmsRecords rmsRecordId = null;
        RecRepairWork rmsRecordId = null;

        if (idRmsRecords < 0) {
            String strIdRmsRecords = mapCodingData.get(Crms.KEY_ID_RMSRECORDS);
            if (strIdRmsRecords != null) idRmsRecords = Long.parseLong(strIdRmsRecords);
        }

        if (objectIdRecord == null)
            objectIdRecord = mapCodingData.get(Crms.KEY_OBJECT_ID);

        if (objectTypeRecord == null)
            objectTypeRecord = mapCodingData.get(Crms.KEY_OBJECT_TYPE);

        try {

            if (txc.isUseTransaction()) getDb().beginTransaction();

            if (idRmsRecords < 0) {
                // We'll look for the record first before creating a new one.
                if (!StringUtils.isNullOrWhitespaces(objectIdRecord)
                        && !StringUtils.isNullOrWhitespaces(objectTypeRecord)) {
//                    rmsRecordId = new RmsRecords();
                    rmsRecordId = new RecRepairWork("rmsrecords");
                    rmsRecordId = findRmsRecordByObjIdObjType(objectIdRecord, objectTypeRecord, rmsRecordId);
//                    if (rmsRecordId != null) idRmsRecords = rmsRecordId.idRmsRecords;
                    if (rmsRecordId != null) idRmsRecords = rmsRecordId.getIdRecord();
                }

                if (idRmsRecords < 0) {
                    // Must be a new record, we'll create one.
                    String mobileRecordId = getMobileRecordId(recordTypeName) + "." + loopIndexOptional;
                    idRmsRecords = createNewRmsRecord(idRecordType, objectTypeRecord,
                            objectIdRecord, recordId, mobileRecordId, masterBarcode, rmsTimestamp, arEfileContent, idRmsRecordsLink, true, false);
                }
            } else {
                if (isDeleteCoding) deleteRecordCodingData(idRmsRecords);
            }

            insertCodingData(idRmsRecords, mapCodingData, txc);

            if (txc.isUseTransaction()) getDb().setTransactionSuccessful();
        } catch (Throwable e) {
            Log.d(TAG, "saveRecord() **** Error. " + e, e);
        } finally {
            if (txc.isUseTransaction()) getDb().endTransaction();
        }

        return rmsRecordId;
    }

    public void insertCodingData(long idRmsRecords, Map<String, String> mapCodingData, DatabaseHelper.TxControl txc) {
        if (mapCodingData != null && mapCodingData.size() > 0) {
            try {
                if (txc.isUseTransaction()) getDb().beginTransaction();
                for (Map.Entry<String, String> entry : mapCodingData.entrySet()) {
                    String codingfieldName = entry.getKey();
                    String value = entry.getValue();
                    String codingmasterId = BusinessRules.getMapCodingMasterIdByName().get(codingfieldName);
                    insertCodingDataRow(idRmsRecords, codingmasterId, value);
                }
                if (txc.isUseTransaction()) getDb().setTransactionSuccessful();
//            } catch (Exception e) {
//                e.printStackTrace();
            } finally {
                if (txc.isUseTransaction()) getDb().endTransaction();
            }
        }
    }

    private String scanForMobileRecordId(int ixCurRec, String objectType, String objectId, String cmid,
                                         String strVal, JSONArray jaCodingData) throws JSONException {
        String strThis = "scanForMobileRecordId(), ";
        Log.d(TAG, strThis + "Start. ixCurRec=" + ixCurRec);

        JSONArray arRow = null; // jaCodingData.getJSONArray(ixCurRec);
        String oid = objectId;
        String otype = objectType;
        String mobileRecordId = null;

        do {
//            Log.d(TAG, strThis + "ixCurRec=" + ixCurRec + ", arRow=" + arRow.toString()
//                    + ", objectId=" + objectId + ", objectType=" + objectType + ", oid=" + oid + ", otype=" + otype
//                    + ", " + ", mobileRecordId=" + mobileRecordId );

            if (CMID_MOBILE_RECORDID.equals(cmid)) {
                mobileRecordId = strVal;
                break;
            }

            if (++ixCurRec >= jaCodingData.length()) break;

            arRow = jaCodingData.getJSONArray(ixCurRec);

            oid = arRow.getString(0);
            otype = arRow.getString(1);
            cmid = arRow.getString(2);
            strVal = arRow.getString(5);

        } while (oid.equals(objectId) && otype.equals(objectType));

        Log.d(TAG, strThis + "End. ixCurRec=" + ixCurRec + ", mobileRecordId=" + mobileRecordId);
        return mobileRecordId;
    }

    /**
     * By design, record was created previously if not exist during sync and we just need to
     * update a few columns.
     *
     * @param LidRmsRecords
     * @param mobileRecordId
     * @param rmsTimestamp
     * @return "CREATE TABLE rmsrecords (" +
     * "Id			                INTEGER NOT NULL PRIMARY KEY AUTOINCREMENT, " +
     * "ObjectType                 TEXT, " +
     * "ObjectId                   TEXT, " +
     * "RecordId			        TEXT, " +
     * "MobileRecordId			    TEXT, " +
     * "MasterBarcode              TEXT, " +
     * "RmsTimestamp		        TEXT, " + // May need index to help syncing
     * "LocalSysTime		        INTEGER, " + // May need index to help syncing
     * "sent				        BIT " + // may need index to help syncing
     * ");",
     */


    public int updateRmsRecordCommon(long LidRmsRecords,
                                     String objectId,
                                     String objectType,
                                     boolean isUpdateObjectIdType,
                                     String recordId,
                                     String mobileRecordId,
                                     String masterBarcode,
                                     long rmsTimestamp,
                                     byte[] arEfileContent,
                                     boolean isValid,
                                     int sentSyncStatus,
                                     boolean isEfileSent,
                                     boolean isNewRecord
    ) {
        Log.d(TAG, "updateRmsRecordForCoding() Start. LidRmsRecords=" + LidRmsRecords // + ", idRecordType=" + idRecordType
                + ", recordId=" + recordId
                + ", mobileRecordId=" + mobileRecordId + ", masterBarcode=" + masterBarcode
                + ", rmsTimestamp=" + rmsTimestamp + ", arEfileContent.length="
                + (arEfileContent != null ? arEfileContent.length : "(NULL)")
                + ", isNewRecord=" + isNewRecord);

        if (LidRmsRecords < 0)
            throw new AssertionError(TAG + ".updateRmsRecordForCoding() **** Design error.  LidRmsRecords is uninitialized.");
        if (mobileRecordId == null || mobileRecordId.length() == 0)
            throw new AssertionError(TAG + ".updateRmsRecordForCoding() **** Design error.  mobileRecordId is uninitialized.");
//        if (rmsTimestamp == null || rmsTimestamp.length() == 0) throw new AssertionError(TAG + ".updateRmsRecordForCoding() **** Design error.  rmsTimestamp is uninitialized.");

        int ix = 1;
        SQLiteStatement stmt = null;

        if (isUpdateObjectIdType && objectId != null && objectType != null) {
            stmt = stmtRmsRecordsUpdateForCodingAndObjectIdType;
            stmt.bindString(ix++, objectId);
            stmt.bindString(ix++, objectType);
        } else {
            stmt = stmtRmsRecordsUpdateForCoding;
        }
        // Todo: possible optimization if not new record, only need/want to change timestamps, Sent bit?
//        "UPDATE RmsRecCommon SET RecordId = ?, MobileRecordId = ?, MasterBarCode = ?, RmsTimestamp = ?,
//        eFileContent = ?, IsValid = ?, Sent = ?, LocalSysTime = ? WHERE Id = ?";
        stmt.clearBindings();
        bindString(stmt, ix++, recordId);
        bindString(stmt, ix++, mobileRecordId);
        bindString(stmt, ix++, masterBarcode);
        bindNonNegative(stmt, ix++, rmsTimestamp);
        bindBlob(stmt, ix++, arEfileContent);
        stmt.bindLong(ix++, isValid ? 1 : 0);
//        stmt.bindLong(ix++, isSent ? 1 : 0);
        stmt.bindLong(ix++, sentSyncStatus);
        stmt.bindLong(ix++, isEfileSent ? 1 : 0);
        stmt.bindLong(ix++, System.currentTimeMillis());
        stmt.bindLong(ix++, LidRmsRecords);

        int iUpdated = stmt.executeUpdateDelete();

        Log.d(TAG, "updateRmsRecordForCoding() End. iUpdated=" + iUpdated);
        return iUpdated;
    }

    public int updateRmsRecordsStatusObjIdObjType(long idRmsRecords, boolean isValid, int sentSyncStatus,
                                                  String objectIdOptional, String objectTypeOptional,
                                                  boolean isUpdateBlankObjectIdObjectType) {
        String strThis = "updateRmsRecordsStatus(), updateRmsRecordsStatusObjIdObjType: ";
        Log.d(TAG, strThis + "Start. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
//            + ", isSent=" + isSent
                + ", sentSyncStatus=" + sentSyncStatus
                + ", objectIdOptional=" + objectIdOptional + ", objectTypeOptional=" + objectTypeOptional);

        if (idRmsRecords < 0)
            throw new AssertionError(TAG + ".updateRmsRecordForCoding() **** Design error.  idRmsRecords is uninitialized.");

        // Todo: possible optimization if not new record, only need/want to change timestamps, Sent bit?
        // "UPDATE RmsRecCommon SET LocalSysTime = ?, IsValid = ?, Sent = ? WHERE Id = ?"
        SQLiteStatement stmt = null;

        if ((StringUtils.isNullOrWhitespaces(objectIdOptional) || StringUtils.isNullOrWhitespaces(objectTypeOptional))
                && !isUpdateBlankObjectIdObjectType)
            stmt = stmtRmsRecordsUpdateStatus;
        else
            stmt = stmtRmsRecordsUpdateStatusObjIdObjType;

        int ix = 1;
        stmt.clearBindings();
        stmt.bindLong(ix++, System.currentTimeMillis());
        stmt.bindLong(ix++, isValid ? 1 : 0);
        stmt.bindLong(ix++, sentSyncStatus);

        if (stmt.equals(stmtRmsRecordsUpdateStatusObjIdObjType)) {
            stmt.bindString(ix++, objectIdOptional);
            stmt.bindString(ix++, objectTypeOptional);
        }

        stmt.bindLong(ix++, idRmsRecords);

        int iUpdated = stmt.executeUpdateDelete();

        Log.d(TAG, strThis + "End. iUpdated=" + iUpdated + ", idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
//                + ", isSent=" + isSent
                + ", sentSyncStatus=" + sentSyncStatus
                + ", objectIdOptional=" + objectIdOptional + ", objectTypeOptional=" + objectTypeOptional);

        Log.d(TAG, "updateRmsRecordsStatusObjIdObjType: iUpdated: "+iUpdated);
        return iUpdated;
    }

    /**
     * private static final String SQL_RMS_RECORDS_UPDATE_EFILE_CONTENT = "UPDATE RmsRecCommon SET EfileContent = ?, LocalSysTime = ?, IsValid = ?, Sent = ? WHERE Id = ?";
     * private SQLiteStatement stmtRmsRecordsUpdateEfileContent; // not thread safe.
     *
     * @param idRmsRecords
     * @param arbyteEfileContent
     * @param isValid
     * @param syncStatusEfile    -- see Cadp.SYNC_STATUS... constants
     * @return
     */
    public int updateRmsRecordsEfileContent(long idRmsRecords, byte[] arbyteEfileContent, boolean isValid, int syncStatusEfile) {
        String strThis = "updateRmsRecordsEfileContent(), ";
        Log.d(TAG, strThis + "Start. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid + ", syncStatusEfile=" + syncStatusEfile);

        if (idRmsRecords < 0)
            throw new AssertionError(TAG + "." + strThis + " **** Design error.  idRmsRecords is uninitialized.");

        int ix = 1;
        this.stmtRmsRecordsUpdateEfileContent.clearBindings();
        bindBlob(stmtRmsRecordsUpdateEfileContent, ix++, arbyteEfileContent);
        this.stmtRmsRecordsUpdateEfileContent.bindLong(ix++, System.currentTimeMillis());
//        this.stmtRmsRecordsUpdateEfileContent.bindLong(ix++, isValid ? 1 : 0);
        this.stmtRmsRecordsUpdateEfileContent.bindLong(ix++, syncStatusEfile);
        this.stmtRmsRecordsUpdateEfileContent.bindLong(ix++, idRmsRecords);

        int iUpdated = this.stmtRmsRecordsUpdateEfileContent.executeUpdateDelete();

        Log.d(TAG, strThis + "End. iUpdated=" + iUpdated + ", idRmsRecords=" + idRmsRecords + ", isValid=" + isValid + ", syncStatusEfile=" + syncStatusEfile);

        return iUpdated;
    }

    public int updateRmsRecordsIsEfileSent(long idRmsRecords, boolean isEfileSent) {
        String strThis = "updateRmsRecordsEfileContent(), ";
        Log.d(TAG, strThis + "Start. idRmsRecords=" + idRmsRecords + ", isEfileSent=" + isEfileSent);

        if (idRmsRecords < 0)
            throw new AssertionError(TAG + "." + strThis + " **** Design error.  idRmsRecords is uninitialized.");

        int ix = 1;
        this.stmtRmsRecordsUpdateIsEfileSent.clearBindings();
        this.stmtRmsRecordsUpdateIsEfileSent.bindLong(ix++, isEfileSent ? Cadp.SYNC_STATUS_SENT : Cadp.SYNC_STATUS_PENDING_UPDATE);
        this.stmtRmsRecordsUpdateIsEfileSent.bindLong(ix++, idRmsRecords);

        int iUpdated = this.stmtRmsRecordsUpdateIsEfileSent.executeUpdateDelete();

        Log.d(TAG, strThis + "End. iUpdated=" + iUpdated + ", idRmsRecords=" + idRmsRecords + ", isEfileSent=" + isEfileSent);

        return iUpdated;
    }

    public int insertOrUpdateCodingDataRowFromItem(long LidRmsRecords, boolean isChangedOnly, int iUpdatedRunning, CodingDataRow item) {
        String strThis = "insertOrUpdateCodingDataRowFromItem(), ";
        Log.d(TAG, strThis + "Start. LidRmsRecords=" + LidRmsRecords + ", isChangedOnly=" + isChangedOnly
                + ", item.getLabel()=" + item.getLabel() + ", item.isChanged()=" + item.isChanged()
                + ", item.valueOrig=" + item.valueOrig + ", item.value=" + item.value
                + ", iUpdatedRunning=" + iUpdatedRunning);

        long idCodingData = item.getIdCodingData();
        Log.d(TAG, strThis + "insertOrUpdateCodingDataRowFromItem: idCodingData: " + idCodingData);

        if (isChangedOnly) {
            // Skip saving unchanged values.
            if (!item.isChanged()) {
                Log.d(TAG, strThis + "Case: isChangedOnly=" + isChangedOnly + " and value not changed, skipping storing item: " + item
                        + ", iUpdatedRunning=" + iUpdatedRunning);
                return iUpdatedRunning;
            } else
                Log.d(TAG, strThis + "Case: isChangedOnly=" + isChangedOnly + "and value changed, *** NOT *** skipping storing item: " + item);
        } else
            Log.d(TAG, strThis + "Case: isChangedOnly=" + isChangedOnly + ", will store item if not new blank value.");

        String value = item.getValue();

        if (idCodingData < 0) {
            Log.d(TAG, strThis + "Case: idCodingData (" + idCodingData + ") < 0, new codingdata row, " + item.displayName + " = " + value);
            if (!StringUtils.isNullOrWhitespaces(value)) {
                stmtCodingDataInsert.clearBindings();
                stmtCodingDataInsert.bindLong(1, LidRmsRecords);
                stmtCodingDataInsert.bindString(2, item.getCodingMasterId());

                if (value != null) stmtCodingDataInsert.bindString(3, value);
                else stmtCodingDataInsert.bindNull(3);

                idCodingData = stmtCodingDataInsert.executeInsert();
                item.setIdCodingData(idCodingData); // in case arListItems is reused.
                iUpdatedRunning++;
                Log.d(TAG, strThis + "Case: idCodingData (" + idCodingData + ") < 0 (new) and not blank value, inserted new codingdata row, " + item.displayName + " = " + value + ", iUpdatedRunning=" + iUpdatedRunning);
            } else {
                Log.d(TAG, strThis + "Case: idCodingData (" + idCodingData + ") < 0 (new) but value is empty, ***NOT*** inserting new codingdata row, " + item.displayName + " = " + value + ", iUpdatedRunning=" + iUpdatedRunning);
            }
        } else {
            if (!StringUtils.isNullOrWhitespaces(value)) {
                stmtCodingDataUpdate.clearBindings();
                stmtCodingDataUpdate.bindString(1, value);
                stmtCodingDataUpdate.bindLong(2, idCodingData);
                iUpdatedRunning += stmtCodingDataUpdate.executeUpdateDelete();
                Log.d(TAG, strThis + "Case: idCodingData (" + idCodingData + ") >= 0, value not blank, updated existing codingdata row, " + item.displayName + " = " + value + ", iUpdatedRunning=" + iUpdatedRunning);
            } else {
                stmtCodingDataDeleteById.bindLong(1, idCodingData);
                int iRowsAffected = stmtCodingDataDeleteById.executeUpdateDelete();
                iUpdatedRunning += iRowsAffected;
                Log.d(TAG, strThis + "Case: value is empty, idCodingData (" + idCodingData + ") >= 0, value is blank, deleted codingdata row, "
                        + item.displayName + " = " + value + ", iRowsAffected=" + iRowsAffected + ", iUpdatedRunning=" + iUpdatedRunning);
            }
        }

        Log.d(TAG, strThis + "End. LidRmsRecords=" + LidRmsRecords + ", isChangedOnly=" + isChangedOnly
                + ", iUpdatedRunning=" + iUpdatedRunning + ", item.getLabel()=" + item.getLabel());
        return iUpdatedRunning;
    }

    /**
     * @param idRecordType
     * @param objectType
     * @param objectId
     * @param mobileRecordIdPrefix
     * @param arIsNewRecord
     * @return
     */
    public long findOrCreateRmsRecordByMobRecId(String tablenameRec, long idRecordType, String[] arMobileRecordId, String objectType,
                                                String objectId, String mobileRecordIdPrefix, boolean isValidated,
                                                boolean isSent, boolean isEfileContentSent, boolean[] arIsNewRecord) {
        String strThis = "findOrCreateRmsRecordByMobRecId(), ";
        Log.d(TAG, strThis + "Start. idRecordType=" + idRecordType
                + ", arMobileRecordId=" + StringUtils.dumpArray(arMobileRecordId) + ", objectType=" + objectType
                + ", objectId=" + objectId + ", mobileRecordIdPrefix=" + mobileRecordIdPrefix
                + ", isValidated=" + isValidated + ", isSent=" + isSent + ", isEfileContentSent=" + isEfileContentSent);

        long idRecord = -1;
//        RmsRecords rmsRecordId = new RmsRecords();
        RecRepairWork rmsRecordId = new RecRepairWork("rmsrecords");

        if (arIsNewRecord != null) arIsNewRecord[0] = false;
        String mobileRecordId = null;

        if (arMobileRecordId != null && arMobileRecordId.length > 0)
            mobileRecordId = arMobileRecordId[0];

        rmsRecordId = findRmsRecordByMobRecId(mobileRecordId, rmsRecordId);
//        idRecord = rmsRecordId.idRmsRecords;
        idRecord = rmsRecordId.getIdRecord();

        if (idRecord < 0) {
            if (StringUtils.isNullOrWhitespaces(mobileRecordId)) {
                Log.d(TAG, strThis + "Case: mobileRecordId is blank. Probably either record created on server, or a new mobile record.");

                mobileRecordId = (mobileRecordIdPrefix != null ? Rms.getMobileRecordId(mobileRecordIdPrefix) : "");

                if (arMobileRecordId != null && arMobileRecordId.length > 0)
                    arMobileRecordId[0] = mobileRecordId;
            }

            idRecord = createNewRmsRecordMinimal(idRecordType, mobileRecordId, objectType, objectId, isValidated, isSent, isEfileContentSent);
        }

        Log.d(TAG, strThis + "End. idRecord=" + idRecord);

        return idRecord;
    }

    //    public RmsRecords findRmsRecordByMobRecId(String mobileRecordId, RmsRecords idRecord)
    public RecRepairWork findRmsRecordByMobRecId(String mobileRecordId, RecRepairWork idRecord) {
        String strThis = "findRmsRecordByMobRecId(), ";
        Log.d(TAG, strThis + "Start. mobileRecordId=" + mobileRecordId);

        idRecord = findRecordByUniqueKey(this.SQL_RMSRECORDS_FIND_ID_BY_MOB_REC_ID, new String[]{mobileRecordId}, idRecord);

//        if (idRecord.idRmsRecords < 0)
        if (idRecord.getIdRecord() < 0)
            Log.d(TAG, strThis + "Case: Can't find mobileRecordId. Probable cause: record created" +
                    " by another device, on RMS Server, app install or new db version. "
                    + " mobileRecordId=" + mobileRecordId + ", idRecord=" + idRecord);
        else
            Log.d(TAG, strThis + "Case: found record by"
                    + " mobileRecordId=" + mobileRecordId + ", idRecord=" + idRecord);

        Log.d(TAG, strThis + "End. idRecord=" + idRecord);
        return idRecord;
    }

    //    public RmsRecords findRmsRecordByObjIdObjType(String objectId, String objectType, RmsRecords idRecord)
    public RecRepairWork findRmsRecordByObjIdObjType(String objectId, String objectType, RecRepairWork idRecord) {
        String strThis = "findRmsRecordByObjIdObjType(), ";
        Log.d(TAG, strThis + "Start. objectId=" + objectId + ", objectType=" + objectType);

        idRecord = findRecordByUniqueKey(this.SQL_RMSRECORDS_FIND_ID_BY_OID_OTYPE, new String[]{objectId, objectType}, idRecord);

//        if (idRecord.idRmsRecords < 0)
        if (idRecord.getIdRecord() < 0)
            Log.d(TAG, strThis + "Case: Can't find record with SQL: " + this.SQL_RMSRECORDS_FIND_ID_BY_OID_OTYPE + " and objectId="
                    + objectId + ", objectType=" + objectType
                    + " . Probable cause: record created" +
                    " by another device, on RMS Server, app install or new db version. "
                    + " objectId=" + objectId + ", objectType=" + objectType);

        Log.d(TAG, strThis + "End. idRecord=" + idRecord);
        return idRecord;
    }

    //    public RmsRecords findRecordByUniqueKey(String sql, String[] arParams, RmsRecords idRecord)
    public RecRepairWork findRecordByUniqueKey(String sql, String[] arParams, RecRepairWork idRecord) {
        String strThis = "findRecordByUniqueKey(), ";
        Log.d(TAG, strThis + "Start. sql=" + sql);
        Log.d(TAG, strThis + "arParams=" + StringUtils.dumpArray(arParams));
//        idRecord.idRmsRecords = -1;
//        idRecord.mobileRecordId = null;
//        idRecord.objectId = null;
//        idRecord.objectType = null;
        idRecord.clearInit(idRecord.getTablename());
        Cursor cur = null;

        try {
            cur = getDb().getQuery(sql, arParams);

            if (cur.moveToFirst()) {
//                    idRecord.idRmsRecords = cur.getLong(0);
//                    idRecord.objectId = cur.getString(1);
//                    idRecord.objectType = cur.getString(2);
//                    idRecord.mobileRecordId = cur.getString(3);
                idRecord.setIdRecord(cur.getLong(0));
                idRecord.setObjectId(cur.getString(1));
                Log.d(TAG, "findRecordByUniqueKey: objectType: " + cur.getString(2));
                idRecord.setObjectType(cur.getString(2));
                idRecord.setMobileRecordId(cur.getString(3));

                Log.d(TAG, strThis + "Case: search found idRecord=" + idRecord);

                if (cur.moveToNext()) {
                    // This should be prevented by unique index on rmsrecords MobileRecordId columns,
                    // but making sure.
                    String strError = "**** Error - more than one record found with arParams=" + StringUtils.dumpArray(arParams);
                    Log.d(TAG, strThis + strError);
                    do {
                        Log.d(TAG, strThis + "Error. rmsrecord with id: " + cur.getLong(0)
                                + " shares arParams: " + StringUtils.dumpArray(arParams) + " with rmsrecord with id: " + idRecord);
                    } while (cur.moveToNext());

                    cur.close();

                    throw new Exception(strThis + strError);
                }
            } else
                Log.d(TAG, strThis + "Case: Can't find " + StringUtils.dumpArray(arParams) + ". Possible cause: record created" +
                        " by another device, on RMS Server, app install or new db version. ");

            cur.close();


        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }

        Log.d(TAG, strThis + "End. idRecord=" + idRecord);

        return idRecord;
    }

    public long createNewRmsRecordMinimal(long idRecordType, String mobileRecordId, String objectType,
                                          String objectId, boolean isValidated, boolean isSent, boolean isEfileContentSent) {

        String strThis = "createNewRmsRecordMinimal(), ";
        Log.d(TAG, strThis + "Start. idRecordType=" + idRecordType + ", mobileRecordId="
                + mobileRecordId + ", objectType=" + objectType
                + ", objectId=" + objectId + ", isValidated=" + isValidated + ", isSent=" + isSent);

        long idRmsRecords = -1;
        int ix = 1;

        this.stmtRmsRecordsInsertMinimal.bindLong(ix++, idRecordType);
        bindString(stmtRmsRecordsInsertMinimal, ix++, mobileRecordId);
        bindString(stmtRmsRecordsInsertMinimal, ix++, objectType);
        bindString(stmtRmsRecordsInsertMinimal, ix++, objectId);
        this.stmtRmsRecordsInsertMinimal.bindLong(ix++, (isValidated ? 1 : 0));
        this.stmtRmsRecordsInsertMinimal.bindLong(ix++, (isSent ? Cadp.SYNC_STATUS_SENT : Cadp.SYNC_STATUS_PENDING_UPDATE));
        this.stmtRmsRecordsInsertMinimal.bindLong(ix++, (isEfileContentSent ? Cadp.SYNC_STATUS_SENT : Cadp.SYNC_STATUS_PENDING_UPDATE));
        this.stmtRmsRecordsInsertMinimal.bindLong(ix++, System.currentTimeMillis());

        idRmsRecords = stmtRmsRecordsInsertMinimal.executeInsert();

        Log.d(TAG, strThis + "End. idRmsRecords=" + idRmsRecords);

        return idRmsRecords;
    }

    /**
     * @param idRecordType
     * @param mobileRecordId
     * @param objectType
     * @param objectId
     * @param isValidated
     * @param isSent
     * @return
     */
    public long createNewRmsRecord(
            long idRecordType,
            String objectType, String objectId, String recordId, String mobileRecordId,
            String masterBarcode, long rmsTimestamp,
            byte[] arEfileContent, long idRmsRecordsLink,
            boolean isValidated, boolean isSent) {
        String strThis = "createNewRmsRecord(), ";
        Log.d(TAG, strThis + "Start. idRecordType=" + idRecordType + ", mobileRecordId="
                + mobileRecordId + ", objectType=" + objectType
                + ", objectId=" + objectId + ", rmsTimestamp=" + rmsTimestamp + ", idRmsRecordsLink=" + idRmsRecordsLink
                + ", isValidated=" + isValidated + ", isSent=" + isSent + ", arEfileContent.length="
                + (arEfileContent != null ? arEfileContent.length : "(NULL"));

        long idRecord = -1;
        int ix = 1;

        this.stmtRmsRecordsInsert.bindLong(ix++, idRecordType);
        bindString(stmtRmsRecordsInsert, ix++, objectType);
        bindString(stmtRmsRecordsInsert, ix++, objectId);
        bindString(stmtRmsRecordsInsert, ix++, recordId);
        bindString(stmtRmsRecordsInsert, ix++, mobileRecordId);
        bindString(stmtRmsRecordsInsert, ix++, masterBarcode);
        bindNonNegative(stmtRmsRecordsInsert, ix++, rmsTimestamp);
        bindBlob(stmtRmsRecordsInsert, ix++, arEfileContent);
        bindNonNegative(stmtRmsRecordsInsert, ix++, idRmsRecordsLink);
        this.stmtRmsRecordsInsert.bindLong(ix++, (isValidated ? 1 : 0));
        this.stmtRmsRecordsInsert.bindLong(ix++, (isSent ? 1 : 0));
        this.stmtRmsRecordsInsert.bindLong(ix++, System.currentTimeMillis());

        idRecord = stmtRmsRecordsInsert.executeInsert();

        Log.d(TAG, strThis + "End. idRecord=" + idRecord);

        return idRecord;
    }

    private String getSqlInClauseListCmids(String[] arCodingfieldNames) {
        if (arCodingfieldNames == null || arCodingfieldNames.length == 0) return null;

        StringBuilder sbuf = new StringBuilder(arCodingfieldNames[0]);
        Map<String, String> map = rules.getMapCodingMasterIdByName();

        for (int i = 1; i < arCodingfieldNames.length; i++)
            sbuf.append("," + map.get(arCodingfieldNames[i]));

        return sbuf.toString();
    }

    //    public List<RmsRecCommon> getRmsRecordIdsFromDbFiltered(
    public List<IRmsRecordCommon.IRmsRecCommon> getRmsRecordIdsFromDbFiltered(
            String[] arRecordTypes, String[] arIdRmsRecords,
            String greaterThanLocalSysTime, String isValid, String syncStatusCsv, String efileContentSyncStatusCsv,
            int maxRecords,
            boolean isObjectIdInfoExists, boolean isEfileContentExists, boolean isIncludeEfileContent) {
        String strThis = "getRmsRecordsFromDbFiltered(), ";
        Log.d(TAG, strThis + "Start. arRecordTypes: " + StringUtils.dumpArray(arRecordTypes)
                + ", arIdRmsRecords=" + StringUtils.dumpArray(arIdRmsRecords)
                + ", greaterThanLocalSysTime=" + greaterThanLocalSysTime
                + ", isValid=" + isValid
//                + ", isSent=" + isSent
//                + ", isEfileContentSent=" + isEfileContentSent
                + ", syncStatusCsv=" + syncStatusCsv
                + ", efileContentSyncStatusCsv=" + efileContentSyncStatusCsv
                + ", maxRecords=" + maxRecords
                + ", isObjectIdInfoExists=" + isObjectIdInfoExists);

        StringBuilder sbufSql = new StringBuilder("SELECT r.Id, r.IdRecordType, rt.RecordType, r.ObjectType, r.ObjectId, r.MobileRecordId");

        if (isIncludeEfileContent) sbufSql.append(", r.EfileContent");

        sbufSql.append("\n FROM rmsrecords r");

        sbufSql.append("\n LEFT OUTER JOIN recordtypes rt ON rt.Id = r.IdRecordType");

        String conj = "WHERE";

        if (isEfileContentExists) {
            sbufSql.append("\n ").append(conj).append(" r.EfileContent IS NOT NULL");
            conj = "AND";
        }


        if (arRecordTypes != null && arRecordTypes.length > 0) {
            String[] arRecordTypeIds = new String[arRecordTypes.length];

            for (int i = 0; i < arRecordTypes.length; i++)
                arRecordTypeIds[i]
                        = String.valueOf(rules.getMapRecordTypeInfoFromRecordTypeName().get(arRecordTypes[i]).id);

            sbufSql.append("\n ").append(conj).append(" r.IdRecordType in('").append(getSqlInClauseListCmids(arRecordTypeIds)).append("')");
            conj = "AND";
        }

        if (arIdRmsRecords != null && arIdRmsRecords.length > 0) {
            sbufSql.append("\n ").append(conj).append(" r.Id in('").append(getSqlInClauseListCmids(arIdRmsRecords)).append("')");
            conj = "AND";
        }


        if (greaterThanLocalSysTime != null) {
            sbufSql.append("\n ").append(conj).append(" r.LocalSysTime > ").append(greaterThanLocalSysTime);
            conj = "AND";
        }

        if (isValid != null) {
            sbufSql.append("\n ").append(conj).append(" r.isValid = ").append(isValid);
            conj = "AND";
        }

//        if (isSent != null) {
//            if ("0".equals(isSent))
//                sbufSql.append(" " + conj + " (r.sent IS NULL OR r.sent = " + isSent + ")");
//            else
//                sbufSql.append(" " + conj + " r.sent = " + isSent);
//
//            conj = "AND";
//        }

        if (syncStatusCsv != null) {
            sbufSql.append("\n " + conj + " r.sent IN (" + syncStatusCsv + ")");
            conj = "AND";
        }

//        if (isEfileContentSent != null) {
//            if ("0".equals(isEfileContentSent))
//                sbufSql.append(" " + conj + " (r.IsEfileContentSent IS NULL OR r.IsEfileContentSent = " + isEfileContentSent + ")");
//            else
//                sbufSql.append(" " + conj + " r.IsEfileContentSent = " + isEfileContentSent);
//
//            conj = "AND";
//        }

        if (efileContentSyncStatusCsv != null) {
            sbufSql.append("\n " + conj + " r.IsEfileContentSent IN (" + efileContentSyncStatusCsv + ")");

            conj = "AND";
        }

        if (isObjectIdInfoExists) {

            sbufSql.append("\n " + conj + " r.ObjectId IS NOT NULL AND r.ObjectType IS NOT NULL");
            conj = "AND";
        }

        String sql = sbufSql.toString();

        Log.d(TAG, strThis + " sql=\n" + sql + "\n");

//        List<RmsRecCommon> listReturn = getRmsRecordIdsFromDb(maxRecords, sql, null, false);
        List<IRmsRecordCommon.IRmsRecCommon> listReturn = getRmsRecordIdsFromDb(maxRecords, sql, null, false);

        Log.d(TAG, strThis + "End. listReturn.size()=" + (listReturn != null ? listReturn.size() : "(NULL)"));

        return listReturn;
    }

    /**
     * @param arRecordTypes
     * @param arIdRmsRecords
     * @param arCodingFieldNames
     * @param greaterThanLocalSysTime
     * @param isValid
     * @param syncStatusCsv
     * @param maxRecords
     * @param isIncludeLinkedParentCols
     * @param isParentObjectIdInfoExists
     * @return
     */
    public List<RmsRecordCoding> getRmsRecordsFromDbFiltered(
            String[] arRecordTypes, String[] arIdRmsRecords, String[] arCodingFieldNames,
            String greaterThanLocalSysTime, String isValid, String syncStatusCsv,// String isEfileContentSent,
            int maxRecords,
            boolean isIncludeLinkedParentCols, boolean isParentObjectIdInfoExists) {
        String strThis = "getRmsRecordsFromDbFiltered(), ";
        Log.d(TAG, strThis + "Start. arRecordTypes: " + StringUtils.dumpArray(arRecordTypes)
                + ", arIdRmsRecords=" + StringUtils.dumpArray(arIdRmsRecords)
                + ", arCodingFieldNames=" + StringUtils.dumpArray(arCodingFieldNames)
                + ", greaterThanLocalSysTime=" + greaterThanLocalSysTime
                + ", isValid=" + isValid + ", syncStatusCsv=" + syncStatusCsv + ", maxRecords=" + maxRecords);

        StringBuilder sbufSql = new StringBuilder(
                "SELECT cd.IdRmsRecords, cd.Value, r.ObjectType, r.ObjectId, cm.CodingFieldName, cm.DataType, r.sent syncstatus");

        if (isIncludeLinkedParentCols)
            sbufSql.append(", rlink.id idRmsRecordsLinked, rlink.ObjectId ParentObjectId, rlink.ObjectType ParentObjectType");

        sbufSql.append("\n FROM codingdata cd INNER JOIN rmsrecords r ON r.id = cd.IdRmsRecords"
                + "\n INNER JOIN codingmasterlookup cm on cm.CodingMasterId = cd.CodingMasterId");

        if (isIncludeLinkedParentCols)
            sbufSql.append("\n LEFT OUTER JOIN rmsrecords rlink ON rlink.Id = r.IdRmsRecordsLink");

        String conj = "WHERE";

        if (arRecordTypes != null && arRecordTypes.length > 0) {
            String[] arRecordTypeIds = new String[arRecordTypes.length];
            for (int i = 0; i < arRecordTypes.length; i++)
                arRecordTypeIds[i]
                        = String.valueOf(rules.getMapRecordTypeInfoFromRecordTypeName().get(arRecordTypes[i]).id);
            sbufSql.append("\n ").append(conj).append(" r.IdRecordType in('").append(getSqlInClauseListCmids(arRecordTypeIds)).append("')");
            conj = "AND";
        }

        if (arIdRmsRecords != null && arIdRmsRecords.length > 0) {
            sbufSql.append("\n ").append(conj).append(" cd.IdRmsRecords in('").append(getSqlInClauseListCmids(arIdRmsRecords)).append("')");
            conj = "AND";
        }

        if (arCodingFieldNames != null && arCodingFieldNames.length > 0) {
            String[] arCmids = new String[arCodingFieldNames.length];
            for (int i = 0; i < arCmids.length; i++)
                arCmids[i] = rules.getMapCodingMasterIdByName().get(arCodingFieldNames[i]);
            sbufSql.append("\n ").append(conj).append(" cd.CodingMasterId in('").append(getSqlInClauseListCmids(arCmids)).append("')");
            conj = "AND";
        }

        if (greaterThanLocalSysTime != null) {
            sbufSql.append("\n ").append(conj).append(" r.LocalSysTime > ").append(greaterThanLocalSysTime);
            conj = "AND";
        }

        if (isValid != null) {
            sbufSql.append("\n ").append(conj).append(" r.isValid = ").append(isValid);
            conj = "AND";
        }

        if (syncStatusCsv != null) {
            sbufSql.append(" " + conj + " r.sent IN (" + syncStatusCsv + ")");
            conj = "AND";
        }

        if (isParentObjectIdInfoExists) {
            String cmidObjectIdParent = BusinessRules.getMapCodingMasterIdByName().get(Crms.PARENT_OBJECTID);

            sbufSql.append("\n " + conj + " (EXISTS (SELECT * FROM rmsrecords r2 WHERE r2.Id = r.IdRmsRecordsLink AND r2.ObjectId >= 0)"
                    + "\n     OR (EXISTS (SELECT * FROM codingdata cd2 WHERE cd2.IdRmsRecords = r.Id AND cd2.CodingMasterId = " + cmidObjectIdParent + ")  )"
                    + "\n     )");
            conj = "AND";
        }

        String sql = sbufSql.toString();

        Log.d(TAG, strThis + " sql=\n" + sql + "\n");

        List<RmsRecordCoding> listReturn = getRmsRecordsFromDb(maxRecords, sql, null, isIncludeLinkedParentCols);

        Log.d(TAG, strThis + "End. listReturn.size()=" + listReturn.size());

        return listReturn;
    }

    public List<RmsRecordCoding> getRmsRecordsFromDb(int maxRecords, String sql, String[] arSqlParameters, boolean isIncludeLinkedParentInfo) {
        String strThis = "getRmsRecordsFromDb, ";

        List<RmsRecordCoding> listReturn = new ArrayList<>();

        long LidRecordLast = -1;
        Map<String, String> mapRec = null;

        String mobileRecordIdLast = null;
        String objectIdLast = null;
        String objectTypeLast = null;
        int syncStatusLast = -99;

        int iRecordCount = 0;
        Cursor cur = null;

        long LidLinked = -1L;
        String objectIdLinked = null;
        String objectTypeLinked = null;

        try {
            cur = getDb().getQuery(sql, arSqlParameters);
//            SELECT cd.IdRmsRecords, cd.Value, r.ObjectType, r.ObjectId, cm.CodingFieldName, cm.DataType,
//            rlink.id idRmsRecordsLinked, rlink.ObjectId ParentObjectId, rlink.ObjectType ParentObjectType");

            while (cur.moveToNext()) {
                long LidRecord = cur.getLong(0);

                if (LidRecord != LidRecordLast) {
                    // A new record has been started, but first store the last record that is now complete.
                    if (LidRecordLast != -1) {
                        Log.d(TAG, strThis + "Before starting new rec, storing codingdata record mapRec+" + mapRec);
                        addCodingDataMapToList(listReturn, mapRec, LidRecordLast, mobileRecordIdLast, objectIdLast, objectTypeLast,
                                LidLinked, objectIdLinked, objectTypeLinked, syncStatusLast);
                        iRecordCount++;
                        if (iRecordCount >= maxRecords) {
                            mapRec = null;
                            break;
                        }
                    }

                    // Initializing new record record-level parameters.
                    mapRec = new HashMap<>();
                    objectTypeLast = cur.getString(2);
                    objectIdLast = cur.getString(3);
//                    objectTypeLast = objectType;
//                    objectIdLast = objectId;
//                    syncStatusLast = syncStatus;
                    syncStatusLast = cur.getInt(6);
                    LidRecordLast = LidRecord;
                    mobileRecordIdLast = null;

                    if (isIncludeLinkedParentInfo) {
                        LidLinked = cur.getLong(7);
                        objectIdLinked = cur.getString(8);
                        objectTypeLinked = cur.getString(9);
                    }

                    // Store special record meta fields in case they are useful.
                    mapRec.put(Crms.KEY_ID_RMSRECORDS, String.valueOf(LidRecord));
//                    mapRec.put(Crms.KEY_OBJECT_TYPE, objectType);
//                    mapRec.put(Crms.KEY_OBJECT_ID, objectId);
                    mapRec.put(Crms.KEY_OBJECT_TYPE, objectTypeLast);
                    mapRec.put(Crms.KEY_OBJECT_ID, objectIdLast);
                }

                String value = cur.getString(1);
//                String objectType = cur.getString(2);
//                String objectId = cur.getString(3);
                String codingFieldName = cur.getString(4);
//                String syncStatus = cur.getString(6);
                if (codingFieldName.equals(Crms.MOBILERECORDID)) mobileRecordIdLast = value;

                Log.d(TAG, strThis + "Storing codingdata field in map, codingFieldName=" + codingFieldName
                        + ", value=" + value);

                mapRec.put(codingFieldName, value);
            }
        } catch (Throwable e) {
            Log.d(TAG, strThis + "**** Error. " + e, e);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }
        // Put last map into list.
        Log.d(TAG, strThis + "Storing final codingdata record mapRec+" + mapRec);

        if (mapRec != null) {
            addCodingDataMapToList(listReturn, mapRec, LidRecordLast, mobileRecordIdLast, objectIdLast, objectTypeLast,
                    LidLinked, objectIdLinked, objectTypeLinked, syncStatusLast);
            iRecordCount++;
        }

        Log.d(TAG, strThis + "End. iRecordCount=" + iRecordCount + ", listReturn.size()=" + listReturn.size());
        return listReturn;
    }

    //    public List<RmsRecCommon> getRmsRecordIdsFromDb(int maxRecords, String sql, String[] arSqlParameters, boolean isIncludeEfileContent) {
    public List<IRmsRecordCommon.IRmsRecCommon> getRmsRecordIdsFromDb(int maxRecords, String sql, String[] arSqlParameters, boolean isIncludeEfileContent) {
        String strThis = "getRmsRecordIdsFromDb, ";

//        List<RmsRecCommon> listReturn = null;
        List<IRmsRecordCommon.IRmsRecCommon> listReturn = null;

        SQLiteCursor cur = null;
        int iRecordCount = 0;

        try {
            cur = (SQLiteCursor) getDb().getQuery(sql, arSqlParameters);

            if (cur.getCount() > 0)
                listReturn = new ArrayList<>();

            while (cur.moveToNext()) {
                int ix = 0;

//                RmsRecCommon rmsRecordId = new RmsRecCommon();
                IRmsRecordCommon.IRmsRecCommon rmsRecordId = new RmsRecords();

//                rmsRecordId.idRmsRecords = cur.getLong(ix++);
//                rmsRecordId.idRecordType = cur.getInt(ix++);
//                rmsRecordId.recordType = cur.getString(ix++);
//                rmsRecordId.objectType = cur.getString(ix++);
//                rmsRecordId.objectId = cur.getString(ix++);
//                rmsRecordId.mobileRecordId = cur.getString(ix++);


                rmsRecordId.setIdRecord(cur.getLong(ix++));
//                June 21, 2022 -   it should be a setIdRecordType instead of setIdRecord
//                rmsRecordId.setIdRecord(cur.getInt(ix++));
                rmsRecordId.setIdRecordType(cur.getInt(ix++));
                rmsRecordId.setRecordType(cur.getString(ix++));
                rmsRecordId.setObjectType(cur.getString(ix++));
                rmsRecordId.setObjectId(cur.getString(ix++));
                rmsRecordId.setMobileRecordId(cur.getString(ix++));

                if (isIncludeEfileContent)
                    rmsRecordId.setExtra(cur.getBlob(ix++));

                listReturn.add(rmsRecordId);

                iRecordCount++;
                if (maxRecords >= 0 && iRecordCount > maxRecords) break;
            }
        } catch (Throwable e) {
            Log.d(TAG, strThis + "**** Error. " + e, e);
        } finally {
            if (cur != null && !cur.isClosed()) cur.close();
        }

        Log.d(TAG, strThis + "End. iRecordCount=" + iRecordCount + ", listReturn.size()=" + (listReturn != null ? listReturn.size() : "(NULL)"));
        return listReturn;
    }

    public List<IRmsRecordCommon.IRmsRecCommon> getPendingEfileContentIds(int maxBatchCount) {
        String strThis = "getPendingEfileContentIds(), ";
        Log.d(TAG, strThis + "Start. maxBatchCount=" + maxBatchCount);

        BusHelperRmsCoding.lock.lock(); // Todo: need to review all the locking.
//        List<BusHelperRmsCoding.RmsRecords> list = null;
        List<IRmsRecordCommon.IRmsRecCommon> list = null;

        try {
            list = getRmsRecordIdsFromDbFiltered(null, null,
                    null, null, null,
                    Cadp.SYNC_STATUS_PENDING_UPDATE + "," + Cadp.SYNC_STATUS_MARKED_FOR_DELETE,
                    maxBatchCount,
                    true, false, false);

            // Todo: upsync list.
        } finally {
            BusHelperRmsCoding.lock.unlock();
        }

        Log.d(TAG, strThis + "End. list.size()=" + (list != null ? list.size() : "(NULL)"));
        return list;
    }

//    /**
//     *
//     * @param arRecordTypes
//     * @param arIdRmsRecords
//     * @param arCodingFieldNames
//     * @param greaterThanLocalSysTime
//     * @param isValid
//     * @param isSent
//     * @param maxRecords
//     * @return
//     */
//    public List<RmsRecordCoding> getPendingSignaturesFromDb(String[] arRecordTypes, String[] arIdRmsRecords, String[] arCodingFieldNames,
//                                                         String greaterThanLocalSysTime, String isValid, String isSent, int maxRecords)
//    {
//        String strThis = "getRmsRecordsFromDbFiltered(), ";
//        Log.d(TAG, strThis + "Start. arRecordTypes: " + StringUtils.dumpArray(arRecordTypes)
//                + ", arIdRmsRecords=" + StringUtils.dumpArray(arIdRmsRecords)
//                + ", arCodingFieldNames=" + StringUtils.dumpArray(arCodingFieldNames)
//                + ", greaterThanLocalSysTime=" + greaterThanLocalSysTime
//                + ", isValid=" + isValid + ", isSent=" + isSent + ", maxRecords=" + maxRecords);
//
//        List<RmsRecordCoding> listReturn = new ArrayList<>();
//
//        StringBuilder sbufSql = new StringBuilder("SELECT cd.IdRmsRecords," +
//                " cd.Value, r.ObjectType, r.ObjectId, cm.CodingFieldName, cm.DataType" + "\n"
//                + " FROM codingdata cd INNER JOIN rmsrecords r ON r.id = cd.IdRmsRecords" + "\n"
//                + " INNER JOIN codingmasterlookup cm on cm.CodingMasterId = cd.CodingMasterId");
//
//        String conj = "WHERE";
//
//        if (arRecordTypes != null && arRecordTypes.length > 0)
//        {
//            String[] arRecordTypeIds = new String[arRecordTypes.length];
//            for (int i = 0; i < arRecordTypes.length; i++) arRecordTypeIds[i]
//                    = String.valueOf(rules.getMapRecordTypeInfoFromRecordTypeName().get(arRecordTypes[i]).id);
//            sbufSql.append("\n ").append(conj).append(" IdRecordType in('").append(getSqlInClauseListCmids(arRecordTypeIds)).append("')");
//            conj = "AND";
//        }
//
//        if (arIdRmsRecords != null && arIdRmsRecords.length > 0) {
//            sbufSql.append("\n ").append(conj).append(" IdRmsRecords in('").append(getSqlInClauseListCmids(arIdRmsRecords)).append("')");
//            conj = "AND";
//        }
//
//        if (arCodingFieldNames != null && arCodingFieldNames.length > 0) {
//            String[] arCmids = new String[arCodingFieldNames.length];
//            for (int i = 0; i < arCmids.length; i++) arCmids[i] = rules.getMapCodingMasterIdByName().get(arCodingFieldNames[i]);
//            sbufSql.append("\n ").append(conj).append(" CodingMasterId in('").append(getSqlInClauseListCmids(arCmids)).append("')");
//            conj = "AND";
//        }
//
//        if (greaterThanLocalSysTime != null) {
//            sbufSql.append("\n ").append(conj).append(" r.LocalSysTime > ").append(greaterThanLocalSysTime);
//            conj = "AND";
//        }
//
//        if (isValid != null) {
//            sbufSql.append("\n ").append(conj).append(" r.isValid = ").append(isValid);
//            conj = "AND";
//        }
//
//        if (isSent != null) {
//            sbufSql.append(" " + conj + " r.sent = " + isSent);
//            conj = "AND";
//        }
//
//        Log.d(TAG, "getRmsRecordsFromDb() sbufSql=" + sbufSql);
//        long LidRecordLast = -1;
//        Map <String, String> mapRec = null;
//
//        String objectIdLast = null;
//        String objectTypeLast = null;
//        String mobileRecordIdLast = null;
//        int iRecordCount = 0;
//        Cursor cur = null;
//
//        try {
//            cur = db.getQuery(sbufSql.toString());
//
//            while (cur.moveToNext()) {
//                long LidRecord = cur.getLong(0);
//                String value = cur.getString(1);
//                String objectType = cur.getString(2);
//                String objectId = cur.getString(3);
//                String codingFieldName = cur.getString(4);
//
//                if (LidRecord != LidRecordLast) {
//                    if (LidRecordLast != -1) {
//                        Log.d(TAG, strThis + "Before starting new rec, storing codingdata record mapRec+" + mapRec);
//                        addCodingDataMapToList(listReturn, mapRec, LidRecordLast, mobileRecordIdLast, objectIdLast, objectTypeLast,
//                                -1L, null, null);
//                        iRecordCount++;
//                        if (iRecordCount >= maxRecords) {
//                            mapRec = null;
//                            break;
//                        }
//                    }
//
//                    objectTypeLast = objectType;
//                    objectIdLast = objectId;
//                    LidRecordLast = LidRecord;
//                    mobileRecordIdLast = null;
//
//                    mapRec = new HashMap<>();
//                    // Store special record meta fields in case they are useful.
//                    mapRec.put(Crms.KEY_ID_RMSRECORDS, String.valueOf(LidRecord));
//                    mapRec.put(Crms.KEY_OBJECT_TYPE, objectType);
//                    mapRec.put(Crms.KEY_OBJECT_ID, objectId);
//                }
//
//                if (codingFieldName.equals(Crms.MOBILERECORDID)) mobileRecordIdLast = value;
//
//                Log.d(TAG, strThis + "Storing codingdata field in map, codingFieldName=" + codingFieldName
//                        + ", value=" + value);
//
//                mapRec.put(codingFieldName, value);
//            }
//        } catch (Throwable e)
//        {
//            Log.d(TAG, strThis + "**** Error. " + e, e);
//        } finally {
//            if (cur != null && !cur.isClosed()) cur.close();
//        }
//        // Put last map into list.
//        Log.d(TAG, strThis + "Storing final codingdata record mapRec+" + mapRec);
//
//        if (mapRec != null)
//        {
//            addCodingDataMapToList(listReturn, mapRec, LidRecordLast, mobileRecordIdLast, objectIdLast, objectTypeLast,
//                    -1L, null, null);
//            iRecordCount++;
//        }
//
//        Log.d(TAG, strThis + "End. iRecordCount=" + iRecordCount + ", listReturn.size()=" + listReturn.size());
//
//        return listReturn;
//    }

    private void addCodingDataMapToList(List<RmsRecordCoding> listReturn, Map<String, String> mapRec,
                                        long idRmsRecordsLast, String mobileRecordIdLast,
                                        String objectIdLast, String objectTypeLast, long idRmsRecordsLinked,
                                        String objectIdLinked, String objectTypeLinked, int syncStatus) {
        if (objectTypeLast != null) {
            mapRec.put("[objecttype]", objectTypeLast);
        }

        if (objectIdLast != null) {
            mapRec.put("[objectid]", objectIdLast);
        }

        RmsRecordCoding rec = new RmsRecordCoding(idRmsRecordsLast, objectIdLast, objectTypeLast, mobileRecordIdLast, mapRec, syncStatus);
        rec.setLinked(idRmsRecordsLinked, objectIdLinked, objectTypeLinked);

        listReturn.add(rec);
    }

    public List<String[]> getRmsRecordForDisplay(String idRmsRecords) {
        List<String[]> listReturn = new ArrayList<>();

        String strSql = "select cd.*, cds.DisplayName, cds.DisplayPosition from CodingData cd\n" +
                "inner join codingmasterlookup cm on cm.CodingMasterId = cd.CodingMasterId\n" +
                "inner join rmsrecords rr on rr.id = cd.IdRmsRecords\n" +
                "inner join recordtypes rt on rt.ObjectType = rr.ObjectType\n" +
                "inner join codingdatasetup cds on  cds.CodingFieldName = cm.CodingFieldName and cds.RecordType = rt.RecordType\n" +
                "where cd.IdRmsRecords = ?\n" +
                "order by cds.DisplayPosition";

        String[] arParams = new String[]{idRmsRecords};

        Log.d(TAG, "getRmsRecordsFromDb() strSql=" + strSql + ", arParams=" + StringUtils.dumpArray(arParams));

        Cursor cur = null;

        try {
            cur = getDb().getQuery(strSql, arParams);

            while (cur.moveToNext()) {
                String id = cur.getString(0);
                String idRecord = cur.getString(1);
                String codingMasterId = cur.getString(2);
                String value = cur.getString(3);
                String displayName = cur.getString(4);

                listReturn.add(new String[]{id, idRecord, codingMasterId, value, displayName});
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (cur != null) cur.close();
        }
        return listReturn;
    }

    //    public void updateFromUpsyncResponse(IRmsRecordCommon.IRmsRecCommon rmsRecordIdWork, int sentSyncStatus) {
    public void updateSentStatusAndMobRecIdFromUpsyncResponse(long idTable, String mobileRecordId, int sentSyncStatus) {
        stmtRmsRecordsUpdateMobileRecordIdAndSent.bindString(0, mobileRecordId);
//        stmtRmsRecordsUpdateMobileRecordIdAndSent.bindLong(1, Cadp.SYNC_STATUS_PENDING_UPDATE); // This looks wrong. Should be sentSyncStatus? -RAN 5/23/2021
        stmtRmsRecordsUpdateMobileRecordIdAndSent.bindLong(1, sentSyncStatus); // I think this is the fix. -RAN 5/23/2021
        stmtRmsRecordsUpdateMobileRecordIdAndSent.bindLong(2, idTable);
        stmtRmsRecordsUpdateMobileRecordIdAndSent.executeUpdateDelete();
        insertOrUpdateCodingDataNoId(idTable, CMID_MOBILE_RECORDID, mobileRecordId);
    }

    @Override
    public int updateRmsRecordStatusObjIdObjType(long idRecord, boolean isValid, int sentSyncStatus, String objectIdOptional, String objectTypeOptional, boolean isUpdateBlankObjectIdObjectType) {
        Log.d(TAG, "testingObject: updateRmsRecordStatusObjIdObjType: with no implementation and returning 0 for iUpdated");

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

        Log.d(TAG, "testingObject: updateRmsRecordStatusObjIdObjType: objectIdOptional: " + objectIdOptional + " objectTypeOptional: " + objectTypeOptional
                + " isUpdateBlankObjectIdObjectType: " + isUpdateBlankObjectIdObjectType);
        if ((StringUtils.isNullOrWhitespaces(objectIdOptional) || StringUtils.isNullOrWhitespaces(objectTypeOptional))
                && !isUpdateBlankObjectIdObjectType) {
            Log.d(TAG, "updateRmsRecordStatusObjIdObjType: if: ");
            stmt = getStmtRmsRecordsUpdateStatus();
        } else {
            Log.d(TAG, "updateRmsRecordStatusObjIdObjType: else: ");
            stmt = getStmtRmsRecordsUpdateStatusObjIdObjType();
        }

        Log.d(TAG, "testingObject: updateRmsRecordStatusObjIdObjType: stmt: " + stmt);
        int ix = 1;
        stmt.clearBindings();
//        June 22, 2022 -   uncommented current time milliseconds because the query was using the time
        stmt.bindLong(ix++, System.currentTimeMillis()); // Let's not update local timestamp unless codingfields changed, to help merge conflicts.
        stmt.bindLong(ix++, isValid ? 1 : 0);
        stmt.bindLong(ix++, sentSyncStatus);


        if (stmt.equals(getStmtRmsRecordsUpdateStatusObjIdObjType())) {
            stmt.bindString(ix++, objectIdOptional);
            stmt.bindString(ix++, objectTypeOptional);
        }

        Log.d(TAG, "testingObject: updateRmsRecordStatusObjIdObjType: idRecord: " + idRecord);
        stmt.bindLong(ix++, idRecord);

        int iUpdated = stmt.executeUpdateDelete();

        Log.d(TAG, strThis + "End. iUpdated=" + iUpdated + ", idRecord=" + idRecord + ", isValid=" + isValid
//                + ", isSent=" + isSent
                + ", sentSyncStatus=" + sentSyncStatus
                + ", objectIdOptional=" + objectIdOptional + ", objectTypeOptional=" + objectTypeOptional);

        return iUpdated;
    }


    private SQLiteStatement getStmtRmsRecordsUpdateStatus() {
        Log.d(TAG, "testingObject: getStmtRmsRecordsUpdateStatus: ");
        if (stmtRmsRecordsUpdateStatus == null) {
            String sql = SQL_RMS_RECORDS_UPDATE_STATUS.replace("[tablename]", getTableName());
            Log.d(TAG, "testingObject: getStmtRmsRecordsUpdateStatus: sql: " + sql);
            stmtRmsRecordsUpdateStatus
                    = getDb().compileStatement(sql);
        }
        Log.d(TAG, "testingObject: getStmtRmsRecordsUpdateStatus: stmtRmsRecordsUpdateStatus; " + stmtRmsRecordsUpdateStatus);
        return stmtRmsRecordsUpdateStatus;
    }

    private SQLiteStatement getStmtRmsRecordsUpdateStatusObjIdObjType() {
        Log.d(TAG, "testingObject: getStmtRmsRecordsUpdateStatusObjIdObjType: ");
        if (stmtRmsRecordsUpdateStatusObjIdObjType == null) {
            String sql = SQL_RMS_RECORDS_UPDATE_STATUS_OBJIDTYPE.replace("[tablename]", getTableName());
            Log.d(TAG, "testingObject: getStmtRmsRecordsUpdateStatusObjIdObjType: sql: " + sql);
            stmtRmsRecordsUpdateStatusObjIdObjType
                    = getDb().compileStatement(sql);
        }
        Log.d(TAG, "testingObject: getStmtRmsRecordsUpdateStatusObjIdObjType: stmtRmsRecordsUpdateStatusObjIdObjType; " + stmtRmsRecordsUpdateStatusObjIdObjType);
        return stmtRmsRecordsUpdateStatusObjIdObjType;
    }


    public String tablename = "rmsrecords";

    public String getTableName() {
        return tablename;
    }

    @Override
    public int deleteRecord(long idRecord, DatabaseHelper.TxControl txc) {
        return deleteRmsRecordAndCodingData(idRecord, txc);
    }

    /**
     * Sync down multiple record types to the codingdata data model in the database.
     *
     * @param arRecordTypes
     * @param maxTimestampKey
     * @param filterFields
     * @param filterValues
     * @param isInsert
     * @throws Exception
     */
    public void downSyncRmsCodingData(String[] arRecordTypes,
                                      String maxTimestampKey, String filterFields, String filterValues,
                                      boolean isInsert) throws Exception {

        String rmsRecordTypesUrlEncoded = Rms.urlEncodeCommaDelimited(arRecordTypes).toString();

        String maxTimestamp = rules.getSetting(maxTimestampKey);
//        if (StringUtils.isNullOrEmpty(maxTimestamp)) maxTimestamp = "+";
        if (StringUtils.isNullOrEmpty(maxTimestamp)) maxTimestamp = "0";

        Log.d(TAG, "downSyncRmsCodingData(), Start. rmsRecordTypesUrlEncoded=" + rmsRecordTypesUrlEncoded
                + ", arRecordTypes=" + StringUtils.dumpArray(arRecordTypes)
                + ", maxTimestamp=" + maxTimestamp
                + ", filterFields=" + filterFields + ", filterValues=" + filterValues
                + ", isInsert=" + isInsert);


        // Todo: batch processing if response has more than 5000 records.

        String response = Rms.getRecordsUpdatedXFilteredRaw(rmsRecordTypesUrlEncoded, -5000,
                maxTimestamp, "+", "+", "+", filterFields, "%2C",
                filterValues, "%2C%3B",
                "+", true, false);

        if (response != null && response.length() > 0) {
            JSONArray jaCodingData = new JSONArray(response);
            Log.d(TAG, "downSyncRmsCodingData(), jaCodingData.length()=" + jaCodingData.length());

            if (jaCodingData != null && jaCodingData.length() > 1) {
                RecRepairWork recRepairWork = new RecRepairWork("rmsrecords");
                maxTimestamp = syncUpdateDbRecords(jaCodingData, false, isInsert, recRepairWork); // ------------------>
                rules.setSetting(maxTimestampKey, maxTimestamp);
            } else
                Log.d(TAG, "downSyncRmsCodingData(), skipping processing of data because 1 or less rows returned from server, "
                        + ", first row is just column labels.  jaCodingData.length()=" + jaCodingData.length());

        } else
            Log.d(TAG, "downSyncRmsCodingData() ***** Warning.  Empty response calling Rms.getRecordsUpdatedXFilteredRaw()"
                    + " for record types: " + rmsRecordTypesUrlEncoded);

        Log.d(TAG, "downSyncRmsCodingData(), End. maxTimestamp=" + maxTimestamp);

    }

//    @Override
//    public int insertRecord(RmsRecCommon rec) {
//        // Todo: implement this.
//        throw new RuntimeException(TAG + " insertRecord(), **** Design error.  This method not supported at this time.");
////        return 0;
//    }
//
//    @Override
//    public int updateRecord(RmsRecCommon rec) {
//        throw new RuntimeException(TAG + " insertRecord(), **** Design error.  This method not supported at this time.");
////        return 0;
//    }

    //    public <K extends IRmsRecordCommon.IRmsRecCommon> void processUpsyncResponseJsonRecord(
//            List<K> listUpSyncRecords, int ixList,
//            K rmsRecordCommonWork,
//            JSONObject jsonDetailRec) throws Exception {

//    public int updateRmsRecordForResponse(String objectId, String objectType, boolean isValid,
//                                          RecRepairWork recRepairWork
//                                        )
//    {
//        String strThis = "updateRmsRecordForResponse(), ";
//
//        int iUpdated;// Do we really want to mark a repaired record as needing upsync at this point?  Might get upsynced again
//        // in the next batch -- okay?  Probably okay, and best, assuming we don't have some endless cycle bug.
//        int syncStatusRepair = (recRepairWork.isRepaired() ? Cadp.SYNC_STATUS_PENDING_UPDATE : Cadp.SYNC_STATUS_SENT);
//
//        if (StringUtils.isNullOrWhitespaces(recRepairWork.getObjectId()) || StringUtils.isNullOrWhitespaces(recRepairWork.getObjectType())) {
//            iUpdated = recRepairWork.updateRmsRecordStatusObjIdObjType(recRepairWork.getIdTable(), isValid, syncStatusRepair, objectId, objectType, false);
//        } else {
//            iUpdated = recRepairWork.updateRmsRecordStatusObjIdObjType(recRepairWork.getIdTable(), isValid, syncStatusRepair, null, null, false);
//        }
//
//        if (iUpdated > 0)
//            Log.d(TAG, strThis + "Updated record: " + recRepairWork + " status to " + syncStatusRepair + ", valid.");
//        else
//            Log.d(TAG, strThis + "***** Error. Failed to update record: " + recRepairWork + " status to " + syncStatusRepair + ", valid.");
//
//        return iUpdated;
//    }
    // region Nested Classes / Interfaces

    // ================================== Nested Classes ================================

    /**
     *
     */
    public static class CodingDataRow
            implements
            AdapterUtils.IMatchable, AdapterUtils.ILabeled {
        private static final String TAG = "CodingDataRow";
        private String valueOrig;
        private String value;
        private Bitmap bitmap;
        long idRmsRecordsBitmap;
        private String displayValue;
        private String dataTypeName;
        private String displayName;
        private String codingfieldName;
        private long idCodingData;
        private long idRmsRecords;
        private String codingMasterId;
        private int editMode;

        DateUtils.IDateConverter dateConverterToLocal;
        DateUtils.IDateConverter dateConverterFromLocal;

        public void init(long idCodingData, long idRmsRecords, String codingMasterId, String codingfieldName, String displayName, String value,
                         String valueOrig, Bitmap bitmap, long idRmsRecordsBitmap, String dataTypeName, int editMode,
                         DateUtils.IDateConverter dateConverterToLocal, DateUtils.IDateConverter dateConverterFromLocal) {
            this.idCodingData = idCodingData;
            this.idRmsRecords = idRmsRecords;
            this.codingMasterId = codingMasterId;
            this.codingfieldName = codingfieldName;
            this.displayName = displayName;
            this.dataTypeName = dataTypeName;
            this.editMode = editMode;
            this.dateConverterToLocal = dateConverterToLocal;
            this.dateConverterFromLocal = dateConverterFromLocal;
            this.valueOrig = valueOrig;
            this.bitmap = bitmap;
            this.idRmsRecordsBitmap = idRmsRecordsBitmap;

            setValue(value);
        }

        public boolean isMatch(String pattern) {
            boolean isMatch = StringUtils.isSearchMatchEquiv(this.displayName, pattern, true);
            if (!isMatch)
                isMatch = StringUtils.isSearchMatchEquiv(this.value, pattern, true);

            return isMatch;
        }

        @Override
        public String toString() {
            return "displayName=" + displayName + ", displayValue=" + displayValue + ", " + StringUtils.memberValuesToString(this);
        }

        public void setValue(String value) {
            this.value = value;

            if (!StringUtils.isNullOrWhitespaces(dataTypeName) && dataTypeName.startsWith("Date")
                    && dateConverterToLocal != null) {

                if (!StringUtils.isNullOrWhitespaces(value))
                    this.displayValue = dateConverterToLocal.convert(value, dataTypeName.equals("DateTime"));

                Log.d(TAG, "setValue() converted date to local date. value=" + value
                        + ", displayValue=" + displayValue + ", dataTypeName=" + dataTypeName
                        + ", codingfieldName=" + codingfieldName + ", idCodingData=" + idCodingData
                        + ", dateConverterToLocal=" + dateConverterToLocal);
            } else {
                this.displayValue = value;
                Log.d(TAG, "setValue() no date conversion needed. value="
                        + value + ", displayValue=" + displayValue + ", dataTypeName="
                        + dataTypeName
                        + ", codingfieldName=" + codingfieldName + ", idCodingData=" + idCodingData
                        + ", dateConverterToLocal=" + dateConverterToLocal);
            }
        }

        public String getValue() {
            return value;
        }

        public boolean isChanged() {
            return !StringUtils.isEquiv(value, valueOrig, false);
        }

        public String getDisplayValue() {
            return displayValue;
        }

        public void updateValueFromDisplay(String displayValue) {
            this.displayValue = displayValue;
            if (!StringUtils.isNullOrWhitespaces(dataTypeName) && dataTypeName.startsWith("Date")
                    && dateConverterFromLocal != null) {

                Log.d(TAG, "updateValueFromDisplay: displayValue: " + displayValue + " value: " + value);
                if (!StringUtils.isNullOrWhitespaces(displayValue)) {
//                    this.value = dateConverterFromLocal.convert(value, true);
                    Log.d(TAG, "updateValueFromDisplay: dateConverterFromLocal: " + dateConverterFromLocal);

                    Log.d(TAG, "updateValueFromDisplay: value: " + value + " displayValue: " + displayValue);
//                    July 21, 2022 -
                    Log.d(TAG, "updateValueFromDisplay: value: " + displayValue.contains("/"));
                    if (displayValue.contains("/")) {
                        String selectedDate = "";
                        if (displayValue.contains(" ")) {
                            String[] splitValue = displayValue.split(" ");
                            Log.d(TAG, "updateValueFromDisplay: splitValue: " + splitValue);
                            if (splitValue != null) {
                                Log.d(TAG, "updateValueFromDisplay: splitValue: length: " + splitValue.length);
                                if (splitValue.length > 0) {
                                    selectedDate = splitValue[0].trim();
                                }
                            } else {
                                selectedDate = displayValue;
                            }
                        } else {
                            selectedDate = displayValue;
                        }
                        Log.d(TAG, "updateValueFromDisplay: selectedDate: " + selectedDate);

                        if (!selectedDate.isEmpty()) {
                            Date formattedDate = null;
                            String finalDate = "";
                            SimpleDateFormat simpleDateFormatTwo = new SimpleDateFormat("MM/dd/yyyy");
                            try {
                                formattedDate = simpleDateFormatTwo.parse(selectedDate);
                                Log.d(TAG, "updateValueFromDisplay: formattedDate: " + formattedDate);
                            } catch (ParseException parseException) {
                                Log.d(TAG, "from exception: updateValueFromDisplay: parseException: " + parseException.getMessage());
                            }

                            Log.d(TAG, "updateValueFromDisplay: formattedDate: " + formattedDate);
                            if (formattedDate != null) {

                                DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
                                finalDate = dateFormat.format(formattedDate);
                                this.value = finalDate;
                            }
                        }
                    }


//                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(FORMAT_DATE_MM_DD_YYYY_HH_MM_SS_SSS);
//                    SimpleDateFormat simpleDateFormat = new SimpleDateFormat(DateUtils.FORMAT_DATE_MM_DD_YYYY);
//                    DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//
//                    Date date = null;
//                    try {
//                        date = simpleDateFormat.parse(displayValue);
//                        if (date == null) {
//                            SimpleDateFormat simpleDateFormatTwo = new SimpleDateFormat(FORMAT_DATE_TIME_MILLIS);
//                            Date dateTwo = simpleDateFormatTwo.parse(displayValue);
//                            Log.d(TAG, "updateValueFromDisplay: dateTwo: " + dateTwo);
//                        }
//                    } catch (ParseException parseException) {
//                        Log.d(TAG, "from exception: updateValueFromDisplay: parseException: " + parseException.getMessage());
//                        parseException.printStackTrace();
//                        SimpleDateFormat simpleDateFormatTwo = new SimpleDateFormat(FORMAT_DATE_TIME_MILLIS);
//                        try {
//                            date = simpleDateFormatTwo.parse(displayValue);
//                        } catch (ParseException e) {
//                            Log.d(TAG, "from exception: updateValueFromDisplay: parseException: " + parseException.getMessage());
//                            e.printStackTrace();
//                        }
//                        Log.d(TAG, "from exception: updateValueFromDisplay: dateTwo: " + date);
//
//                    }
//
////                    this.value = dateConverterFromLocal.convert(value, true);
//                    if (date != null) {
////                        this.value = displayValue;
//                        String strDate = dateFormat.format(date);
//                        this.value = strDate;
//                    }
                    Log.d(TAG, "if: updateValueFromDisplay: this.value: " + this.value);
                }

                Log.d(TAG, "updateValueFromDisplay() converted displayed date: " + displayValue + " to database date. value=" + value);
            } else {
                this.value = StringUtils.isNullOrWhitespaces(displayValue) ? null : displayValue;
                Log.d(TAG, "else: updateValueFromDisplay() set value=" + value);
            }
        }


        public String getDataTypeName() {
            return dataTypeName;
        }

        public String getCodingMasterId() {
            return codingMasterId;
        }


        public void setIdCodingData(long idCodingData) {
            this.idCodingData = idCodingData;
        }

        public long getIdCodingData() {
            return idCodingData;
        }

        public String getCodingfieldName() {
            return codingfieldName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public long getIdRmsRecords() {
            return idRmsRecords;
        }

        public long getIdRmsRecordsBitmap() {
            return idRmsRecordsBitmap;
        }

        public boolean isReadOnly() {
            if (editMode == Cadp.EDIT_MODE_READONLY) return true;
            else return false;
        }

        @Override
        public String getLabel() {
            return getDisplayName();
        }

        public void setBitmap(Bitmap bitmap) {
            this.bitmap = bitmap;
        }

        public Bitmap getBitmap() {
            return bitmap;
        }
    }

    public static class RmsRecordType {
        public int id;
        public String recordTypeName;
        public String objectType;
        public boolean isEfileType;
        public String category;
        public int idCategory;
    }

    public static class RmsRecordCoding extends RmsRecords {
        private Map<String, String> mapCoding;

        public RmsRecordCoding(long idRmsRecords, String objectId, String objectType, String mobileRecordId, Map<String, String> mapCoding, int syncStatus) {
            super(idRmsRecords, objectId, objectType, mobileRecordId, syncStatus);
            this.mapCoding = mapCoding;
        }

//        public RmsRecordCoding() {
//        }

        public Map<String, String> getMapCoding() {
            return mapCoding;
        }

        public String get(String codingfieldName) {
            return mapCoding.get(codingfieldName);
        }

        public String toString() {
            return super.toString() + ", mapCoding: " + mapCoding;
        }
    }

    public static class RmsDataType implements Serializable {
        public String typeId;
        public String dataTypeName;
        public String codingDataColumn;

        public RmsDataType(String typeId, String dataTypeName, String codingDataColumn) {
            this.typeId = typeId;
            this.dataTypeName = dataTypeName;
            this.codingDataColumn = codingDataColumn;
        }
    }

    public static class MaxTimeStamp {
        private long rmsTimestamp = -1L;
        private long objectId = -1L;
        private String objectType;

        public void updateMaxTimestamp(long rmsTimestamp, long objectId, String objectType) {
            if (rmsTimestamp > this.rmsTimestamp)
                setMaxTimestamp(rmsTimestamp, objectId, objectType);
            else if (rmsTimestamp >= this.rmsTimestamp && objectId > this.objectId)
                setMaxTimestamp(rmsTimestamp, objectId, objectType);
            else if (rmsTimestamp >= this.rmsTimestamp && objectId >= this.objectId) {
                if (this.objectType == null || (objectType != null && this.objectType.compareTo(objectType) < 0))
                    setMaxTimestamp(rmsTimestamp, objectId, objectType);
            }
        }

        public void setMaxTimestamp(long rmsTimestamp, long objectId, String objectType) {
            this.rmsTimestamp = rmsTimestamp;
            this.objectId = objectId;
            this.objectType = objectType;
        }

        public String getMaxtimestampCsv() {
            StringBuilder sbuf = new StringBuilder();
            if (rmsTimestamp >= 0) sbuf.append(rmsTimestamp);
            if (objectId >= 0) sbuf.append(",").append(objectId);
            if (objectType != null) sbuf.append(",").append(objectType);
            return sbuf.toString();
        }
    }

    public interface IExtraListPostProcessor<K> {
        public List<K> process(List<K> listItems, List<K> listExtra);
    }

    public interface IResponseUpdateable {
        public long getDbRecordId();

        public String getObjectType();

        public long getObjectId();

        public String getMobileRecordId();

    }

    public static class RmsRecords extends RmsRecCommon    // implements com.rco.rcotrucks.businesslogic.rms.recordcommon.IRmsRecordCommon.IRmsRecCommon, Serializable {
    {
        public RmsRecords() {
            super();
        }

        public RmsRecords(long idRmsRecords) {
            super(idRmsRecords); //this.idTable = idRmsRecords;
        }

//        public RmsRecords(String mobileRecordId) {
//            super(mobileRecordId); // this.mobileRecordId = mobileRecordId;
//        }

//        public RmsRecords(String objectId, String objectType)
//        {
////            this.objectId = objectId;
////            this.objectType = objectType;
//            super(objectId, objectType);
//        }

        public RmsRecords(long idRmsRecords, String objectId, String objectType, String mobileRecordId, int syncStatusBeforeRepair) {
//            this(idRmsRecords, objectId, objectType, mobileRecordId, -1L);
//            this.syncStatusBeforeRepair = syncStatusBeforeRepair;
//            super(idRmsRecords, objectId, objectType, mobileRecordId, syncStatusBeforeRepair);
            super(idRmsRecords);
            setObjectId(objectId);
            setObjectType(objectType);
            setMobileRecordId(mobileRecordId);
            setSentSyncStatus(syncStatusBeforeRepair);
        }

//        public RmsRecords(long idRmsRecords, String objectId, String objectType, String mobileRecordId, long idRecordType) {
////            this(idRmsRecords, objectId, objectType, mobileRecordId, idRecordType, null,
////                    false, null, -1L, null, null, -1);
//            super(idRmsRecords, objectId, objectType, mobileRecordId, idRecordType, null,
//                    false, null, -1L, null,
//                    null, -1, -1L);
//        }

//        public RmsRecords(long idRmsRecords, String objectId, String objectType, String mobileRecordId,
//                          long idRecordType, String recordType, boolean isRepaired, Object extra,
//                          long idRmsRecordsLinked, String objectIdLinked, String objectTypeLinked,
//                          int syncStatusBeforeRepair, long mobileTimestamp) {
//
//            super(idRmsRecords, objectId, objectType, mobileRecordId, idRecordType, recordType, isRepaired, extra,
//                    idRmsRecordsLinked, objectIdLinked, objectTypeLinked, syncStatusBeforeRepair, mobileTimestamp);
//        }

        @Override
        public void clear() {
        }

        public int deleteRecord(long idRecord, DatabaseHelper.TxControl txc) {
            return BusHelperRmsCoding.instance().deleteRmsRecordAndCodingData(idRecord, txc);
        }


//        @Override
//        public int updateRmsRecordsStatus(long idTable, boolean isValid, int syncStatus, String objectId, String objectType, boolean isUpdateBlankObjectIdObjectType) {
//            return BusHelperRmsCoding.instance().updateRmsRecordsStatusObjIdObjType(idTable, isValid, syncStatus, objectId, objectType, isUpdateBlankObjectIdObjectType);
//        }


//        @Override
//        public int updateRecordCommon(String objectId, String objectType, boolean isUpdateObjectIdType,
//                                      String recordId, String mobileRecordId, String masterBarcode,
//                                      long rmsTimestamp, byte[] arEfileContent, boolean isValid,
//                                      int sentSyncStatus, boolean isEfileSent, boolean isNewRecord) {
//            return BusHelperRmsCoding.instance().updateRmsRecordCommon(getIdRecord(), objectId, objectType, isUpdateObjectIdType, recordId, mobileRecordId,
//                    masterBarcode, rmsTimestamp, arEfileContent, isValid, sentSyncStatus, isEfileSent, isNewRecord);
//        }
//
//        @Override
//        public int updateRmsRecordsEfileContent(byte[] arbyteEfileContent, boolean isValid, int syncStatusEfile) {
//            return BusHelperRmsCoding.instance().updateRmsRecordsEfileContent(getIdRecord(), arbyteEfileContent, isValid, syncStatusEfile);
//        }

//        @Override
//        public byte[] getEfileContent() {
//            String strThis = "getEfileContent(), ";
//            String strSql = "SELECT EfileContent FROM rmsrecords WHERE Id = ?";
//            String[] arParams = new String[1];
//            Cursor cur = null;
//            byte[] arBytes = null;
//
//            arParams[0] = String.valueOf(getIdRecord());
//
//            try {
//                cur = getDb().getQuery(strSql, arParams); // ------------>
//
//                if (cur.getCount() > 1) Log.d(TAG, strThis
//                        + "**** Major design error. Multiple rmsrecords returned for unique Id:" + getIdRecord()
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

//        @Override
//        public byte[] fetchEfileContent() {
//            String strSql = "SELECT EfileContent FROM " + getTableName() + " WHERE Id = ?";
//            String[] arParams = new String[]{String.valueOf(getIdRecord())};
//
//            return BusHelperRmsCoding.instance().fetchEfileContent(strSql, arParams);
//        }

//        @Override
//        public int updateRmsRecordsIsEfileSent(boolean isEfileSent) {
//            return BusHelperRmsCoding.instance().updateRmsRecordsIsEfileSent(getIdRecord(), isEfileSent);
//        }

//        protected DatabaseHelper getDb() {
//            return null;
//        }

//        public String toString() {
//            return "{idRmsRecords=" + this.idTable + ", mobileRecordId="
//                + this.mobileRecordId + ", objectType=" + objectType + ", objectId=" + this.objectId
//                + ", idRecordType=" + this.idRecordType + ", recordType=" + this.recordType
//                    + ", isRepaired=" + this.isRepaired + ", syncStatusBeforeRepair=" + this.syncStatusBeforeRepair
//                    + ", objectIdLinked=" + this.objectIdLinked + ", objectTypeLinked=" + this.objectTypeLinked
//                + ", getExtra() is " + (getExtra() == null ? "null" : "not null") + "}";
//        }
//
//        public void setIdRmsRecordsLinked(long idRmsRecordsLinked) {
//            this.idRmsRecordsLinked = idRmsRecordsLinked;
//        }
    }

    // endregion Nested Classes / Interfaces

}

/* ----------------------------------------------- Database Inspector queries ---------------------------------------
--------- Dump record ------------
select rt.RecordType, r.Id rId, cd.Id cdId, cd.IdRmsRecords, r.IdRmsRecordsLink, cd.CodingMasterId, cm.CodingFieldName, cd.Value from
codingdata cd inner join codingmasterlookup cm on cm.CodingMasterId = cd.CodingMasterId
inner join rmsrecords r on r.Id = cd.IdRmsRecords
inner join recordtypes rt on rt.Id = r.IdRecordType
where r.Id = ?
order by rt.RecordType, r.Id, cm.CodingFieldName

----------------- get Pending records --------------------------------
SELECT cd.IdRmsRecords, cd.Value, r.ObjectType, r.ObjectId, cm.CodingFieldName, cm.DataType
     FROM codingdata cd INNER JOIN rmsrecords r ON r.id = cd.IdRmsRecords
     INNER JOIN codingmasterlookup cm on cm.CodingMasterId = cd.CodingMasterId
     WHERE IdRecordType in('1') -- replace '1' with IdRecordType list for the record type of interest.
     AND r.isValid = 1 AND r.sent = 0

----------------- get Pending records and parent objectid --------------------------------
SELECT cd.IdRmsRecords, cd.Value, r.ObjectType, r.ObjectId, cm.CodingFieldName, cm.DataType,
    rlink.ObjectId ParentObjectId, rlink.ObjectType ParentObjectType
     FROM codingdata cd INNER JOIN rmsrecords r ON r.id = cd.IdRmsRecords
     LEFT OUTER JOIN rmsrecords rlink ON rlink.Id = r.IdRmsRecordsLink
     INNER JOIN codingmasterlookup cm on cm.CodingMasterId = cd.CodingMasterId
     WHERE r.IdRecordType in('3')
     AND r.isValid = 1 AND r.sent = 0

----------------------- get pending records with non-blank ParentObjectId codingfield or linked parent ObjectId ---------------
SELECT cd.IdRmsRecords, cd.Value, r.ObjectType, r.ObjectId, cm.CodingFieldName, cm.DataType, (SELECT ObjectId FROM rmsrecords rr WHERE rr.Id = r.IdRmsRecordsLink) ParentObjectId
     FROM codingdata cd INNER JOIN rmsrecords r ON r.id = cd.IdRmsRecords
     INNER JOIN codingmasterlookup cm on cm.CodingMasterId = cd.CodingMasterId
     WHERE r.IdRecordType in('3')
     AND r.isValid = 1 AND r.sent = 0
     AND (EXISTS (SELECT * FROM rmsrecords r2 WHERE r2.Id = r.IdRmsRecordsLink AND r2.ObjectId >= 0)
     OR (EXISTS (SELECT * FROM codingdata cd2 WHERE cd2.IdRmsRecords = r.Id AND cd2.CodingMasterId = 24314)  ) -- use CodingMasterId of ParentObjectId codingfield.
     )
----------------- get Pending efile content records --------------------------------
SELECT cd.IdRmsRecords, cd.Value, r.ObjectType, r.ObjectId, cm.CodingFieldName, cm.DataType
     FROM codingdata cd INNER JOIN rmsrecords r ON r.id = cd.IdRmsRecords
     INNER JOIN codingmasterlookup cm on cm.CodingMasterId = cd.CodingMasterId
     WHERE IdRecordType in('1') AND  r.IsEfileContentSent = 0 -- replace '1' with IdRecordType list for the record type of interest.

----------------------- Gen DVIR Report code -----------------------------------------
drop table temp1

create table temp1 (a TEXT, b TEXT, c TEXT, d TEXT)

insert or replace into temp1 (a,b,c) values  ('hidden','operation',null),
           ('hidden','objectId',null),
           ('hidden','objectType',null),
           ('hidden','d_objectId',null),
           ('hidden','d_objectType',null),
           ('hidden','mobileRecordId',null),
           ('hidden','functionalGroupName',null),
           ('hidden','d_f_name',null),
           ('hidden','d_l_name',null),
           ('hidden','location',null),
           ('hidden','d_id',null),
           ('hidden','license_number',null),
               ('text','carrier',null),
               ('text','address',null),
               ('text','date',null),
               ('text','time',null),
               ('text','truckTractorNumber',null),
               ('text','odometer',null),
                       ('checkbox','airCompressor',null),
                       ('checkbox','airLines',null),
                       ('checkbox','battery',null),
                       ('checkbox','brakeAccessories',null),
                       ('checkbox','brakes',null),
                       ('checkbox','carburetor',null),
                       ('checkbox','clutch',null),
                       ('checkbox','defroster',null),
                       ('checkbox','driveLine',null),
                       ('checkbox','fifthWheel',null),
                       ('checkbox','registration',null),
                       ('checkbox','insurance',null),
                       ('checkbox','frontAxle',null),
                       ('checkbox','fuelTanks',null),
                       ('checkbox','horn',null),
                       ('checkbox','heater',null),
                       ('checkbox','lights',null),
                       ('checkbox','mirrors',null),
                       ('checkbox','oilPressure',null),
                       ('checkbox','onBoardRecorder',null),
                       ('checkbox','radiator',null),
                       ('checkbox','rearEnd',null),
                       ('checkbox','reflectors',null),
                       ('checkbox','safetyEquipment',null),
                       ('checkbox','springs',null),
                       ('checkbox','starter',null),
                       ('checkbox','steering',null),
                       ('checkbox','tachograph',null),
                       ('checkbox','tires',null),
                       ('checkbox','transmission',null),
                       ('checkbox','wheels',null),
                       ('checkbox','windows',null),
                       ('checkbox','windShieldWipers',null),
                       ('checkbox','others',null),
                   ('text','trailer1Numbers',null),
                   ('text','trailer1ReeferHOS',null),
                       ('checkbox','trailer1BreakeConnections',null),
                       ('checkbox','trailer1Breakes',null),
                       ('checkbox','trailer1CouplingPin',null),
                       ('checkbox','trailer1CouplingChains',null),
                       ('checkbox','trailer1Doors',null),
                       ('checkbox','trailer1Hitch',null),
                       ('checkbox','trailer1LandingGear',null),
                       ('checkbox','trailer1LightsAll',null),
                       ('checkbox','trailer1Roof',null),
                       ('checkbox','trailer1Springs',null),
                       ('checkbox','trailer1Tarpaulin',null),
                       ('checkbox','trailer1Tires',null),
                       ('checkbox','trailer1Wheels',null),
                       ('checkbox','trailer1Other',null),
                   ('text','trailer2Numbers',null),
                   ('text','trailer2ReeferHOS',null),
                       ('checkbox','trailer2BreakeConnections',null),
                       ('checkbox','trailer2Breakes',null),
                       ('checkbox','trailer2CouplingPin',null),
                       ('checkbox','trailer2CouplingChains',null),
                       ('checkbox','trailer2Doors',null),
                       ('checkbox','trailer2Hitch',null),
                       ('checkbox','trailer2LandingGear',null),
                       ('checkbox','trailer2LightsAll',null),
                       ('checkbox','trailer2Roof',null),
                       ('checkbox','trailer2Springs',null),
                       ('checkbox','trailer2Tarpaulin',null),
                       ('checkbox','trailer2Tires',null),
                       ('checkbox','trailer2Wheels',null),
                       ('checkbox','trailer2Other',null),
            ('textarea','remarks',null),
               ('checkbox','conditionVehicleIsSatisfactory',null),
               ('text','driversSignatureVehicleSatisfactory',null),
               ('checkbox','aboveDefectsCorrected',null),
               ('checkbox','aboveDefectsNoCorrectionNeeded',null),
                ('signature', 'signature_of_mechanic',null),
                   ('text','mechanicsSignatureDate',null),
                ('signature','mechanicsSignature',null),
                ('text','mechanic_name',null),
                ('signature', 'signature_of_driver',null),
                   ('text','driversSignatureNoCorrectionNeededDate',null);

select t.b, (select c.CodingFieldName from codingmasterlookup c where c.CodingFieldName like '%' || t.b || '%')
from temp1 t

**** not the best, spaces mess up -- update temp1 set c =  (select c.CodingFieldName from codingmasterlookup c where c.CodingFieldName like '%' || temp1.b || '%')

select cc cco, substr(cc, 1, instr(cc, ' ') - 1) ||  substr(cc, instr(cc, ' ') + 1 ) cc
from (
select cc cco, substr(cc, 1, instr(cc, ' ') - 1) ||  substr(cc, instr(cc, ' ') + 1 ) cc
from (
select cc cco, substr(cc, 1, instr(cc, ' ') - 1) ||  substr(cc, instr(cc, ' ') + 1 ) cc
from (
select cc cco, substr(cc, 1, instr(cc, ' ') - 1) ||  substr(cc, instr(cc, ' ') + 1 ) cc
from (
select codingfieldname, substr(codingfieldname, 1, instr(codingfieldname, ' ') - 1) ||  substr(codingfieldname, instr(codingfieldname, ' ') + 1 ) cc
from codingmasterlookup
)
)
)
)

---------------------------------------------- Create temp2 work table mapping DVIR report html element ids to codingdatafields
drop table temp2

create table temp2 as
select * from  temp1 t left outer join
(
select substr(cc, 1, instr(cc, ' ') - 1) ||  substr(cc, instr(cc, ' ') + 1 ) cc, codingfieldname
from (
select substr(cc, 1, instr(cc, ' ') - 1) ||  substr(cc, instr(cc, ' ') + 1 ) cc, codingfieldname
from (
select substr(cc, 1, instr(cc, ' ') - 1) ||  substr(cc, instr(cc, ' ') + 1 ) cc, codingfieldname
from (
select substr(cc, 1, instr(cc, ' ') - 1) ||  substr(cc, instr(cc, ' ') + 1 ) cc, codingfieldname
from (
select substr(codingfieldname, 1, instr(codingfieldname, ' ') - 1) ||  substr(codingfieldname, instr(codingfieldname, ' ') + 1 ) cc, codingfieldname
from codingmasterlookup c
)
)
)
)
) tbl on t.b like tbl.cc

--------------- Generate Array[][] initialization for html/codingfield map
select '{"' || ifnull(a,'') || '","' || ifnull(b,'') || '","' || ifnull(codingfieldname,'') || '"},' from temp2 order by b

 */