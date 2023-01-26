package com.rco.rcotrucks.activities.dvir;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Log;

import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.SyncTask;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.TruckLogDetail;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.IRmsRecordCommon;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecRepairWork;
//import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecRepairWorkRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordCommonHelper;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.utils.BuildUtils;
import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.ImageUtils;
import com.rco.rcotrucks.utils.StringUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rco.rcotrucks.businesslogic.rms.Rms.serializeListAsCsvPost;

/**
 * This class extends BusinessRules purely to have access to static db and lock members.   Eventually this
 * code can be migrated into BusinessRules.
 */

public class BusHelperDvir extends BusHelperRmsCoding {
    private static final String TAG = "BusHelperDvir";

    public static final String MOBILERECORDID_PREFIX_DVIR_DTL = "TruckDVIRDetail";
    public static final int DVIR_UPSYNC_MAX_BATCH_SIZE = 20;
    public static BusHelperDvir instance;
    public static int MAX_DVIR_BATCH_COUNT;
    public BusinessRules rules;
    public BusHelperRmsCoding rulesRmsCoding;

    private static long idRecordTypeDvirDetail = -1L;
    private static String objectTypeDvirDetail;
    private User user;
    //    private String odometer;
    private SQLiteStatement stmtRmsRecordsUpdateIsValidByMobid; // not thread safe.
    private TruckLogDetail truckLogDetail;


    public static Context getContext() throws Exception {
        return context;
    }

    public static void setContext(Context context) {
        BusHelperDvir.context = context;
    }

    private static Context context;

    public BusHelperDvir() {
        super(RecordRulesHelper.getDb());
        Log.d(TAG, "savePreTrip: BusHelperDvir: constructor starts: ");
        rules = BusinessRules.instance();
        rulesRmsCoding = BusHelperRmsCoding.instance();
        user = BusinessRules.instance().getAuthenticatedUser();
//        odometer = BusinessRules.instance().getOdometer();

//        July 08, 2022 -   We need to get the current trailer 1 and trailer 2 which are saved in trailer log for today
        truckLogDetail = rules.getCurrentTruckLOgDetail();


        Log.d(TAG, "savePreTrip: BusHelperDvir() Constructor end.");
    }

    public String getOdometer() {
        return rules.getOdometer();
    }

    public static synchronized BusHelperDvir instance() {
        // Todo: conditionally instantiate instance.
        if (instance == null) instance = new BusHelperDvir();

        return instance;
    }

//    private static String lockDvirWaitMessage;
//    private long dvirLockMillis;
//    public static ReentrantLock lockDvirData = new ReentrantLock();
//
//    public static boolean tryLockDvirData(Context ctx, String waitMessage, long maxMillisBeforeCancel) {
//        boolean ret = false;
//        try
//    }

    public String getOrgName() {
        return Rms.getOrgName();
    }

    /**
     * arCodingDataSetupTruckDvirDetail is an array of rows of template codingfield descriptors for the Truck DVIR Detail record.
     * The columns are as followes:
     * 0.  Codingfield Rms Name
     * 1.  Codingfield Rms datatype name.
     * 2.  Android display type -- can be any code that helps the software display the field, such as a RecyclerView view type integer.
     * 3.  (Optional if subsequent columns are missing.  Can be blank.)  Android field display name -- sometimes slightly different from the Rms codingfield name.  Defaults to Codingfield name.
     * 4.  (Optional) EditMode numeric constant - see DvirDtlAdapter EDIT_MODE_.. constants. Can be used by UI code or ignored.
     * 5.  (Optional) IsRequired - set to 1 if required, blank or 0 if not. Can be used by UI code or ignored.
     * 6.  (Optional CombineType - set to a Cadp.COMBINE_TYPE_.. value that can be used by UI code to create a composite value from multiple codingfields.  If it is null, then it will be combined with the next non-null row to make a displayed item.
     * 7.  (Optional) Android display position in a list, sortable.  Defaults to the index of the row in this array.
     * 8.  (Optional) column relative position in setter CSV file.  May not be used at this time.
     * <p>
     * Note: items with null "display type" are typically grouped and displayed with the first following non-null "display type".
     */
    public boolean initTruckDvirDetailSetup(boolean isAlreadyInTransaction) {
        boolean isSuccess = true;

        String[][] arCodingDataSetupTruckDvirDetail = new String[][]{
                {Crms.LOGISTICS_CARRIER, "String", String.valueOf(Cadp.VIEWTYPE_FLD), "", String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.ADDRESS, "String", String.valueOf(Cadp.VIEWTYPE_FLD), null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.ODOMETER, "Numeric", String.valueOf(Cadp.VIEWTYPE_LBL_FLD), null, String.valueOf(Cadp.EDIT_MODE_EDITABLE), "1"},
                {Crms.DATETIME, "DateTime", String.valueOf(Cadp.VIEWTYPE_FLD), null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.TRUCK_NUMBER, "String", String.valueOf(Cadp.VIEWTYPE_HDR_FLD), "TRUCK/TRACTOR", String.valueOf(Cadp.EDIT_MODE_EDITABLE), "1"},
                {Crms.AIR_COMPRESSOR, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.AIR_LINES, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.BATTERY, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.BRAKE_ACCESSORIES, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.BRAKES, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.CARBURETOR, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.CLUTCH, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.DEFROSTER, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.DRIVE_LINE, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.FIFTH_WHEEL, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.FRONT_AXLE, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.FUEL_TANKS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.HEATER, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.HORN, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.LIGHTS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.MIRRORS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.OIL_PRESSURE, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.ON_BOARD_RECORDER, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.RADIATOR, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.REAR_END, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.REFLECTORS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.SAFETY_EQUIPMENT, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.SPRINGS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.STARTER, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.STEERING, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TACHOGRAPH, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TIRES, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRANSMISSION, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.WHEELS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.WINDOWS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.WINDSHIELD_WIPERS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.OTHER, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_NUMBER, "String", String.valueOf(Cadp.VIEWTYPE_HDR_FLD), "TRAILER 1", String.valueOf(Cadp.EDIT_MODE_EDITABLE), "1"},
                {Crms.TRAILER1_REEFER_HOS, "String", String.valueOf(Cadp.VIEWTYPE_LBL_FLD), "Reefer HOS.", String.valueOf(Cadp.EDIT_MODE_EDITABLE), "1"},
                {Crms.TRAILER1_BRAKE_CONNECTIONS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_BRAKES, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_COUPLING_KING_PIN, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK), "Trailer1 Coupling Pin"},
                {Crms.TRAILER1_COUPLING_CHAINS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_DOORS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_HITCH, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_LANDING_GEAR, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_LIGHTS_ALL, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_ROOF, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_SPRINGS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_TARPAULIN, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_TIRES, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_WHEELS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER1_OTHER, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_NUMBER, "String", String.valueOf(Cadp.VIEWTYPE_HDR_FLD), "TRAILER 2", String.valueOf(Cadp.EDIT_MODE_EDITABLE), "1"},
                {Crms.TRAILER2_REEFER_HOS, "String", String.valueOf(Cadp.VIEWTYPE_LBL_FLD), "Reefer HOS.", String.valueOf(Cadp.EDIT_MODE_EDITABLE), "1"},
                {Crms.TRAILER2_BRAKE_CONNECTIONS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_BRAKES, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_COUPLING_KING_PIN, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK), "Trailer2 Coupling Pin"},
                {Crms.TRAILER2_COUPLING_CHAINS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_DOORS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_HITCH, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_LANDING_GEAR, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_LIGHTS_ALL, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_ROOF, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_SPRINGS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_TARPAULIN, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_TIRES, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_WHEELS, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.TRAILER2_OTHER, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.REMARKS, "String", String.valueOf(Cadp.VIEWTYPE_HDR_CMT)},
                {Crms.CONDITION_VEHICLE_SATISFACTORY, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.REGISTRATION, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.INSURANCE, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.DRIVER_FIRST_NAME, "String", null, null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.DRIVER_LAST_NAME, "String", null, null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.DRIVER_RECORDID, "String", null, null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.DRIVERS_SIGNATURE_NO_CORRECTIONS_NEEDED_DATE, "Date", null, null},
                {Crms.DRIVERS_SIGNATURE_VEHICLE_SATISFACTORY, "String", String.valueOf(Cadp.VIEWTYPE_HDR_FLD_SIG), "Drivers name and signature", String.valueOf(Cadp.EDIT_MODE_READONLY), "1", String.valueOf(Cadp.COMBINE_TYPE_FIRST_LAST_NAME)},
                {Crms.ABOVE_DEFECTS_CORRECTED, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
                {Crms.ABOVE_DEFECTS_NO_CORRECTIONS_NEEDED, "Boolean", String.valueOf(Cadp.VIEWTYPE_LBL_CHK)},
//            {Crms.DRIVERS_SIGNATURE_NO_CORRECTIONS_NEEDED, "String", String.valueOf(Cadp.VIEWTYPE_LBL_FLD)},
                // Todo: Mechanic items need special case, maybe special layout.  Set by lookup.
                {Crms.MECHANIC_FIRST_NAME, "String", null, null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.MECHANIC_LAST_NAME, "String", null, null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.MECHANICS_SIGNATURE_DATE, "Date", null, null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.MECHANIC_RECORDID, "Numeric", null, null, String.valueOf(Cadp.EDIT_MODE_READONLY)},
                {Crms.MECHANICS_SIGNATURE, "String", String.valueOf(Cadp.VIEWTYPE_HDR_FLD_SIG), "Mechanic Name and Signature", String.valueOf(Cadp.EDIT_MODE_EDITABLE), null, String.valueOf(Cadp.COMBINE_TYPE_FIRST_LAST_NAME)},
        };

        rulesRmsCoding.addCodingDataSetup(Crms.R_TRUCK_DVIR_DETAIL, arCodingDataSetupTruckDvirDetail, isAlreadyInTransaction);

        return isSuccess;
    }

    public static void initRmsConvenienceMembers() {
        BusHelperRmsCoding.RmsRecordType rtype = BusinessRules.getMapRecordTypeInfoFromRecordTypeName().get(Crms.R_TRUCK_DVIR_DETAIL);
        if (rtype == null)
            return;

        idRecordTypeDvirDetail = rtype.id;
        objectTypeDvirDetail = rtype.objectType;
        Log.d(TAG, "initRmsConvenienceMembers() done.");
    }


    private static List<DvirListAdapter.ListItemDvir> listDvirs = null;

    public static List<DvirListAdapter.ListItemDvir> getListDvirs() {
        return listDvirs;
    }


    private static List<ListItemCodingDataGroup> listDvirDetail = null;

    public static List<ListItemCodingDataGroup> getListDvirDetail() {
        return listDvirDetail;
    }

    /**
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public void syncDvirItems() throws Exception {

        String response = Rms.getRecordsUpdatedXFiltered(Crms.R_TRUCK_DVIR_DETAIL, -5000,
                Crms.DRIVER_FIRST_NAME + "," + Crms.DRIVER_LAST_NAME, "%2C",
                user.getFirstName() + "," + user.getLastName(), "%2C%3B",
                Crms.DATETIME + "," + Crms.TRUCK_NUMBER + "," + Crms.TRAILER1_NUMBER + "," + Crms.TRAILER2_NUMBER + "," + Crms.RECORDID);
        Log.d(TAG, "syncDvirItems: response: " + response);

        if (listDvirs == null)
            listDvirs = new ArrayList();
        else
            listDvirs.clear();

        DateUtils.IDateConverter dateConverter = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_TIME_MILLIS,
                DateUtils.FORMAT_DATE_MM_DD_YY, DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS);

        listDvirs = AdapterUtils.syncItems(response, listDvirs, DvirListAdapter.ListItemDvir.class, dateConverter);
        Log.d(TAG, "syncDvirItems: listDvirs: " + listDvirs.size());

        if (listDvirs != null)
            Collections.sort(listDvirs);
    }

    public void loadDvirItems(String startDate, String endDate) throws Exception {
        Log.d(TAG, "syncDvirItems: loadDvirItems() Start.");

        DateUtils.IDateConverter dateConverter = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_TIME_MILLIS,
                DateUtils.FORMAT_DATE_MM_DD_YY, DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS);

        List<String> listParams = new ArrayList<>();

//        String strSql = DvirListAdapter.getDvirListSql(idRecordTypeDvirDetail);
//        Log.d(TAG, "loadDvirItems() strSql=\n" + strSql);
        String strSql = "";
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            strSql = DvirListAdapter.getDvirListSql(idRecordTypeDvirDetail,
                    listParams, startDate, endDate);
        } else {
            strSql = DvirListAdapter.getDvirListSql(idRecordTypeDvirDetail);
        }
        String[] arParams = (String[]) listParams.toArray(new String[listParams.size()]);

        Log.d(TAG, "syncDvirItems: loadDvirItems(): strSql: " + strSql);

        Cursor cursor = null;

        if (listDvirs == null) {
            Log.d(TAG, "syncDvirItems: loadDvirItems() initializing listDivrs.");
            listDvirs = new ArrayList<>();
        } else {
            Log.d(TAG, "syncDvirItems: loadDvirItems() clearing listDivrs.");
            listDvirs.clear();
        }

        try {
            cursor = getDb().getQuery(strSql, arParams);
            listDvirs = AdapterUtils.loadItemsFromDatabase(cursor, listDvirs, DvirListAdapter.ListItemDvir.class, dateConverter);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }

//        if (listDvirs != null)
//            Collections.sort(listDvirs);

        Log.d(TAG, "syncDvirItems: loadDvirItems() End.");
    }

    public void loadDvirDtlItems(long idRmsRecordsDvirDtl, String objectIdDvirDetail, String objectTypeDvirDetail) throws Exception {
        String strThis = "syncDvirItems: syncDvirItems: loadDvirDtlItems(), ";

        Log.d(TAG, strThis + "Start. idRmsRecordsDvirDtl=" + idRmsRecordsDvirDtl);

        String strSql = null;
        Cursor cursor = null;


        try {

            if (listDvirDetail == null) {
                Log.d(TAG, strThis + "initializing listDvirDetail.");
                listDvirDetail = new ArrayList<>();
            } else {
                Log.d(TAG, strThis + "clearing listDvirDetail.");
                listDvirDetail.clear();
            }

            if (idRmsRecordsDvirDtl >= 0) {
                // Case: fetch DVIR Detail codingfields for the selected DVIR identified by id: idRmsRecordsDvirDtl.
//                List <String> listParams = new ArrayList<>();
//                strSql = BusHelperRmsCoding.getRmsRecordSql();
                Log.d(TAG, strThis + "syncDvirItems: Case: idRmsRecordsDvirDtl=" + idRmsRecordsDvirDtl + ", >= 0, loading existing record.");
//                cursor = db.getQuery(strSql, new String[]{String.valueOf(idRmsRecordsDvirDtl), Crms.R_TRUCK_DVIR_DETAIL});
                cursor = rulesRmsCoding.getRmsRecordCursor(String.valueOf(idRmsRecordsDvirDtl), Crms.R_TRUCK_DVIR_DETAIL);

            } else {
                // Case: fetch DVIR Detail codingfield template for entering a new DVIR.
//                strSql = getNewRmsRecordSql();
                Log.d(TAG, strThis + "syncDvirItems: Case: idRmsRecordsDvirDtl < 0, loading new record template.");
//                cursor = db.getQuery(strSql, new String[]{Crms.R_TRUCK_DVIR_DETAIL});
                cursor = rulesRmsCoding.getNewRmsRecordCursor(Crms.R_TRUCK_DVIR_DETAIL);
            }


            listDvirDetail = loadDvirDtlItemsFromDatabase(idRmsRecordsDvirDtl, objectIdDvirDetail,
                    objectTypeDvirDetail, cursor, listDvirDetail);

        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }

        Log.d(TAG, strThis + "syncDvirItems: End. listDvirDetail.size()=" + (listDvirDetail != null ? listDvirDetail.size() : "(NULL)"));
    }

    /**
     * This is the workhorse method that creates the Dvir Detail list for the recycler view presented
     * in the DvirDtlActivity screen.  It has various exception logic for initializing special items
     * such as compound views that have multiple views such as name and signature image, etc.
     * It relies heavily on the information from the codingdatasetup for the Truck DVIR Detail record type
     * to drive the logic.  See initTruckDvirDetailSetup() for the specific field configuration data.  For
     * example, the "CombineType" column assists in combining several codingfields into a single
     * RecyclerView item.
     *
     * @param idRmsRecordsDvirDtl
     * @param cursor
     * @param listItems
     * @return
     * @throws Exception
     */
    public List<ListItemCodingDataGroup> loadDvirDtlItemsFromDatabase(long idRmsRecordsDvirDtl, String objectIdDvirDtl, String objectTypeDvirDtl,
                                                                      Cursor cursor, List<ListItemCodingDataGroup> listItems) throws Exception {
        String strThis = "loadDvirDtlItemsFromDatabase(), ";

        Log.d(TAG, strThis + "Start. cursor.getColumnNames()=" + StringUtils.dumpArray(cursor.getColumnNames()));

        // Todo: these date converters can be made more permanent I think, so long as no thread conflicts. -RAN
        DateUtils.IDateConverter dateConverterToLocal = new DateUtils.DateConverterParser(DateUtils.FROM_DATE_TIME_FORMAT_LIST,
                DateUtils.FORMAT_DATE_MM_DD_YYYY, DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_AMPM);

        DateUtils.IDateConverter dateConverterFromLocalDate = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_MM_DD_YYYY,
                DateUtils.FORMAT_ISO_SSS_Z, DateUtils.FORMAT_ISO_SSS_Z);

        DateUtils.IDateConverter dateConverterFromLocalDateTime = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_AMPM,
//                DateUtils.FORMAT_DATE_TIME_MILLIS, DateUtils.FORMAT_DATE_TIME_MILLIS);
                DateUtils.FORMAT_ISO_SSS_Z, DateUtils.FORMAT_ISO_SSS_Z);

        DateUtils.IDateConverter dateConvFromLocal = null;
        DateUtils.IDateConverter dateConvToLocal = null;
        String idRecordTypeSignature = String.valueOf(BusinessRules.getMapRecordTypeInfoFromRecordTypeName().get(Crms.R_SIGNATURE).id);
        String strIdRmsRecordsDvirDtl = String.valueOf(idRmsRecordsDvirDtl);
        String cmidDocumentTitle = BusinessRules.getMapCodingMasterIdByName().get(Crms.DOCUMENT_TITLE);
        Log.d(TAG, "DOCUMENT_TITLE: loadDvirDtlItemsFromDatabase: cmidDocumentTitle: " + cmidDocumentTitle);
        String cmidParentObjId = BusinessRules.getMapCodingMasterIdByName().get(Crms.PARENT_OBJECTID);
        String cmidParentObjType = BusinessRules.getMapCodingMasterIdByName().get(Crms.PARENT_OBJECTTYPE);
        Bitmap bitmapMechanicSignature = null;
        Bitmap bitmapDriverSignature = null;
        String driverFirstName = null;
        String driverLastName = null;
        Bitmap bitmap;
        AdapterUtils.BitmapItem[] arBitmaps = null;
//        long idRmsRecordsBitmap = -1L;
        BusHelperRmsCoding.RmsRecords rmsRecordId = new BusHelperRmsCoding.RmsRecords();

        listItems.clear();

        boolean bIsBeginner = "beginner".equals(user.getSkillLevel().trim().toLowerCase());

        List<BusHelperRmsCoding.CodingDataRow> listCodingRowItems = null;

        int irow = 0;
        while (cursor.moveToNext()) {
            bitmap = null;
            arBitmaps = null;

//            idRmsRecordsBitmap = -1L;

            long idCodingData = cursor.isNull(0) ? -1L : cursor.getLong(0);
            String codingvalue = StringUtils.nvl(cursor.getString(1), "");
            String codingvalueOrig = codingvalue;
            String codingFieldName = cursor.getString(2);
            String displayName = cursor.getString(3);
            String dataTypeName = cursor.getString(4);  // This is the datatype from the codingdatasetup table, not the rmsdatatypes table.
            String dataTypeRms = cursor.getString(5);
            int viewType = cursor.getInt(6);
            String codingMasterId = cursor.getString(7);
            int iEditMode = cursor.getInt(8);
            boolean isRequired = (cursor.getInt(9) == 1 ? true : false);
            int combineType = cursor.getInt(10);

            Log.d(TAG, strThis + "Case: top of loop, codingFieldName=" + codingFieldName + ", viewType=" + viewType);

            if (Crms.DRIVER_FIRST_NAME.equals(codingFieldName)) {
                // We cache firstName until lastName encountered so they can be combined into single item to be displayed.
                if (StringUtils.isNullOrWhitespaces(codingvalue)) codingvalue = user.getFirstName();
                driverFirstName = codingvalue;
            } else if (Crms.DRIVER_LAST_NAME.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue))
                    codingvalue = user.getLastName();
                driverLastName = codingvalue;
            } else if (Crms.ADDRESS.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue)) {
                    codingvalue = StringUtils.getCompoundName(user.getAddress1(), ", ",
                            user.getAddress2(), ", ", user.getCity(), ", ", user.getState(), ", ",
                            user.getZipCode(), ", ", user.getCountry());
                }
            } else if (Crms.DATETIME.equals(codingFieldName) || Crms.DATE.equals(codingFieldName)) {
                // Conversion to local date format occurs in item.init().  It might be more appropriate here?
                Log.d(TAG, strThis + "Case: date type codingFieldName=" + codingFieldName + ", displayName=" + displayName + ", codingvalue=" + codingvalue);
                // Todo: get logged in user's address?
                if (StringUtils.isNullOrWhitespaces(codingvalue)) {
                    Log.d(TAG, strThis + "Case: blank date date codingvalue, initializing with current local datetime. Not considering timezone at this point.");
                    codingvalue = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_TIME_MILLIS);
                    Log.d(TAG, strThis + "Case: blank date date codingvalue initializeed with current datetime: " + codingvalue);
                }
            } else if (Crms.ODOMETER.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue))
                    codingvalue = getOdometer();
            } else if (Crms.LOGISTICS_CARRIER.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue))
                    codingvalue = getOrgName();
            } else if (Crms.TRUCK_NUMBER.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue))
                    codingvalue = user.getTruckNumber();
            } else if (Crms.TRAILER1_NUMBER.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue)) {
                    if (truckLogDetail != null) {
                        codingvalue = truckLogDetail.Trailer1Number;
                    }
                }
            } else if (Crms.TRAILER2_NUMBER.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue)) {
                    if (truckLogDetail != null) {
                        codingvalue = truckLogDetail.Trailer2Number;
                    }
                }
            } else if (Crms.MECHANICS_SIGNATURE.equals(codingFieldName)) {
                long idRmsRecordsBitmap = -1;

                if (idRmsRecordsDvirDtl >= 0) {
                    rmsRecordId = rulesRmsCoding.getEfileBitmapByParent(codingFieldName, idRecordTypeSignature,
                            idRmsRecordsDvirDtl, cmidParentObjId, objectIdDvirDtl, cmidParentObjType,
                            objectTypeDvirDtl, cmidDocumentTitle, Crms.V_MECHANIC_SIGNATURE, rmsRecordId);

                    if (rmsRecordId != null) {
                        idRmsRecordsBitmap = rmsRecordId.getIdRecord();
                        bitmap = (Bitmap) rmsRecordId.getExtra();
//                        idRmsRecordsBitmap = rmsRecordId.idRmsRecords;
                    }
                    ;

                }

                // even if bitmap is null, create the holder so user can create new signature and store it here.
                arBitmaps = new AdapterUtils.BitmapItem[]{
                        new AdapterUtils.BitmapItem(bitmap, Cadp.BITMAP_CLASS_SIGNATURE, Cadp.BITMAP_TYPE_MECHANIC_SIGNATURE,
                                idRmsRecordsBitmap)
                };

                Log.d(TAG, strThis + "Case: " + Crms.MECHANICS_SIGNATURE
                        + ", bitmap is " + (bitmap == null ? "NULL" : "NOT NULL") + ", idRmsRecordsBitmap=" + idRmsRecordsBitmap
                        + ", idRmsRecordsDvirDtl=" + idRmsRecordsDvirDtl + ", objectIdDvirDtl=" + objectIdDvirDtl + ", objectTypeDvirDtl=" + objectTypeDvirDtl
                        + ", arBitmaps[0]=" + (arBitmaps != null ? arBitmaps[0] : "(NULL)"));

                bitmapMechanicSignature = bitmap;
            } else if (Crms.DRIVERS_SIGNATURE_VEHICLE_SATISFACTORY.equals(codingFieldName)) {
                if (StringUtils.isNullOrEmpty(codingvalue))
                    codingvalue = StringUtils.getCompoundName(driverFirstName, " ", driverLastName);

                long idRmsRecordsBitmap = -1;

                if (idRmsRecordsDvirDtl >= 0) {
                    rmsRecordId = rulesRmsCoding.getEfileBitmapByParent(codingFieldName, idRecordTypeSignature,
                            idRmsRecordsDvirDtl, cmidParentObjId, objectIdDvirDtl, cmidParentObjType,
                            objectTypeDvirDtl, cmidDocumentTitle, Crms.V_DRIVER_SIGNATURE, rmsRecordId);

                    if (rmsRecordId != null) {
                        idRmsRecordsBitmap = rmsRecordId.getIdRecord();
                        bitmap = (Bitmap) rmsRecordId.getExtra();
//                        idRmsRecordsBitmap = rmsRecordId.idRmsRecords;
                    }
                }

                // even if bitmap is null, create the holder so user can create new signature and store it here.
                arBitmaps = new AdapterUtils.BitmapItem[]{
                        new AdapterUtils.BitmapItem(bitmap, Cadp.BITMAP_CLASS_SIGNATURE, Cadp.BITMAP_TYPE_DRIVER_SIGNATURE,
                                idRmsRecordsBitmap)
                };

                Log.d(TAG, strThis + "Case: " + Crms.DRIVERS_SIGNATURE_VEHICLE_SATISFACTORY
                        + ", bitmap is " + (bitmap == null ? "NULL" : "NOT NULL") + ", idRmsRecordsBitmap=" + idRmsRecordsBitmap
                        + ", idRmsRecordsDvirDtl=" + idRmsRecordsDvirDtl + ", objectIdDvirDtl=" + objectIdDvirDtl + ", objectTypeDvirDtl=" + objectTypeDvirDtl
                        + ", arBitmaps[0]=" + arBitmaps[0]);

                bitmapDriverSignature = bitmap;
            }


            if ("Date".equals(dataTypeName)) {
                dateConvFromLocal = dateConverterFromLocalDate;
                dateConvToLocal = dateConverterToLocal;
            } else if ("DateTime".equals(dataTypeName)) {
                dateConvFromLocal = dateConverterFromLocalDateTime;
                dateConvToLocal = dateConverterToLocal;
            } else {
                dateConvFromLocal = null;
                dateConvToLocal = null;
            }

            // For beginners, we default new DVIR Detail checkboxes (boolean RMS types) to "true".
            if (bIsBeginner && Crms.D_BOOLEAN.equals(dataTypeRms) && idRmsRecordsDvirDtl < 0)
                codingvalue = Cadp.SQLITE_VAL_TRUE;

            BusHelperRmsCoding.CodingDataRow item = new BusHelperRmsCoding.CodingDataRow();

            Log.d(TAG, strThis + "irow=" + irow + ", idCodingData=" + idCodingData
                    + ", codingFieldName=" + codingFieldName
                    + ", displayName=" + displayName + ", codingvalue=" + codingvalue + ", viewType=" + viewType + ", "
                    + " dataTypeName=" + dataTypeName + ", iEditMode=" + iEditMode + ", isRequired=" + isRequired
                    + ", dateConvToLocal=" + dateConvToLocal + ", dateConvFromLocal=" + dateConvFromLocal);

            item.init(idCodingData, idRmsRecordsDvirDtl, codingMasterId, codingFieldName, displayName, codingvalue, codingvalueOrig,
                    null, -1L, dataTypeName, iEditMode, dateConvToLocal, dateConvFromLocal);

            Log.d(TAG, strThis + "after item.init(), irow=" + irow + ",item.toString()=" + item.toString());

            if (viewType >= 0 && listCodingRowItems == null) listCodingRowItems = new ArrayList<>();

            listCodingRowItems.add(item);

            // When we get to a coding setup row item that has a valid viewType, we can add it to the
            // list of row items that includes any row items without valid viewType.  This allows us
            // to combine codingfields into a single adapter item, such as first name, last name.
            // The view holder can reference the list of codingfields that comprise it's codingvalue,
            // and individual coding setup rows can be updated if necessary by the gui, so that
            // when saving, we retain the 1:1 correspondence with a coding setup row and codingdata
            // row in the codingdata table. If the adapter just held a combined item, the saving
            // process would know how to update the individual codingfields that were combined without
            // highly custom code.
            if (viewType > 0) {
                ListItemCodingDataGroup listItem = new ListItemCodingDataGroup();

                Log.d(TAG, "loadDvirDtlItemsFromDatabase: ");

                listItem.init(idRmsRecordsDvirDtl, objectIdDvirDtl, objectTypeDvirDtl,
                        listCodingRowItems, arBitmaps, combineType, displayName,
                        null, null,
                        viewType, iEditMode, isRequired, null);

                listItems.add(listItem);

                Log.d(TAG, strThis + "Case: added listItem with list of codingfield row items to listItems, viewType >= 0, viewType=" + viewType + ", irow=" + irow
                        + ", listItem.getLabel()=" + listItem.getLabel() + ", listItem.getCombinedValue()=" + listItem.getCombinedValue()
                        + ", listCodingRowItems.size()=" + listCodingRowItems.size() + ", listItems.size()=" + listItems.size());

                listCodingRowItems = null;
            } else if (viewType == 0) {
                Log.d(TAG, strThis + "***** Encountered viewType == 0,  viewType=" + viewType + ", irow=" + irow
                        + ", item=" + item
                        + ", listCodingRowItems.size()=" + StringUtils.dumpArray(listCodingRowItems.toArray())
                        + ", stacktrace: " + StringUtils.dumpArray(Thread.currentThread().getStackTrace()));
            }

            irow++;
        }

        Log.d(TAG, strThis + "End.  result.size()=" + listItems.size());
        return listItems;
    }


    public void runSaveDvirDtlTask(final DvirDtlActivity activity, final long idRmsRecords,
                                   final List<ListItemCodingDataGroup> listItems) {
        Log.d(TAG, "savePreTrip: runSaveDvirDtlTask: ");
        final String strThis = "runSaveDvirDtlTask()";

        Log.d(TAG, strThis + " Start.");
        final WeakReference<DvirDtlActivity> activityWeakReference = new WeakReference<>(activity);

        SyncTask task = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, "savePreTrip: loadScreen: starts: ");
                Log.d(TAG, strThis + ".task.loadScreen() start.");
                // Don't need to close activity here any more because launching DVIR Report
                // in parallel, and return from that will finish the activity. -RAN 2/5/21
//                DvirDtlActivity activity = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
//                if (activity != null && !activity.isFinishing())
//                {
//                    Log.d(TAG, strThis + ".task.loadScreen() calling activity.finish().");
//                    activity.finish();
//                }
                Log.d(TAG, strThis + ".task.loadScreen() end.");
                Log.d(TAG, "savePreTrip: loadScreen: ends: ");
            }

            @Override
            public void executeSyncItems() throws Exception {
                try {
                    Log.d(TAG, "savePreTrip: executeSyncItems: ");
                    Log.d(TAG, "runSaveDvirDtlTask() task.executeSyncItems() Start. about to run BusHelperRmsCoding.saveDvirDetailToDb().");

                    // Todo: validate whether items being saved are valid for sending to RMS Server (esp. required fields).
                    boolean isValid = true;

                    Log.d(TAG, "savePreTrip: executeSyncItems: listItems: size: " + listItems.size());
                    for (ListItemCodingDataGroup item : listItems) {

                        if (item.isRequired() && StringUtils.isNullOrWhitespaces(item.getCombinedValue())) {
                            Log.d(TAG, "savePreTrip: executeSyncItems: item: isRequired: " + item.isRequired() + " combinedValue: " + item.getCombinedValue());
                            isValid = false;
                            break;
                        } else {
                            Log.d(TAG, "else: savePreTrip: executeSyncItems: item: isRequired: " + item.isRequired() + " combinedValue: " + item.getCombinedValue());
                        }
                    }
                    Log.d(TAG, "savePreTrip: executeSyncItems: isValid: " + isValid);
                    Log.d(TAG, "executeSyncItems: ");
                    saveDvirDetailToDb(idRmsRecords, listItems, isValid);

                    Log.d(TAG, "savePreTrip: executeSyncItems: saveDvirDetailToDb: saved I believe");
                    Log.d(TAG, "runSaveDvirDtlTask() task.executeSyncItems() Start about to run UiHelperDvirDtl.runSyncUpdateRmsDvirsTask().");

                    // Todo: for now, we will try an upsync after every save operation.  Need to integrate
                    //  with larger strategy.  Need concurrency control.  -- disabled this upsync as of 11/19/2021 -RAN
                    if (false) {
                        Log.d(TAG, "savePreTrip: if(false): if: runSaveDvirDtlTask() task.executeSyncItems(), for testing, upsyncing DVIRs after local save to db.");
                        UiHelperDvirDtl.instance().runSyncUpdateRmsDvirsTask(DVIR_UPSYNC_MAX_BATCH_SIZE);
                    } else {
                        Log.d(TAG, "savePreTrip: if(false): else: runSaveDvirDtlTask() task.executeSyncItems(), not upsyncing DVIRs after local save to db.  Upsync now part of SyncRecords background task.");
                    }

                    Log.d(TAG, "savePreTrip: runSaveDvirDtlTask() task.executeSyncItems() End.");
                } catch (Throwable e) {
                    Log.d(TAG, "savePreTrip: runSaveDvirDtlTask() task.executeSyncItems() **** Exception.", e);
                }
            }
        }, "Refreshing Dvir usage data...");

//        Log.d(TAG, "runSaveDvirDtlTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "savePreTrip: runSaveDvirDtlTask() starting task, serial to prevent conflict with refresh dvir detail task.");
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);

    }


    //June 24, 2022 -   We need a function that will create the
    public long runSaveDvirDtlTaskSync(final long idRmsRecords,
                                       final List<ListItemCodingDataGroup> listItems) {
        Log.d(TAG, "savePreTrip: runSaveDvirDtlTask: ");
        boolean isValid = true;

        Log.d(TAG, "savePreTrip: executeSyncItems: listItems: size: " + listItems.size());
//        June 30, 2022 -   We already added validation so not required any more
//        for (ListItemCodingDataGroup item : listItems) {
//
//            if (item.isRequired() && StringUtils.isNullOrWhitespaces(item.getCombinedValue())) {
//                Log.d(TAG, "savePreTrip: executeSyncItems: item: isRequired: " + item.isRequired() + " combinedValue: " + item.getCombinedValue());
//                isValid = false;
//                break;
//            } else {
//                Log.d(TAG, "else: savePreTrip: executeSyncItems: item: isRequired: " + item.isRequired() + " combinedValue: " + item.getCombinedValue());
//            }
//        }
        Log.d(TAG, "savePreTrip: executeSyncItems: isValid: " + isValid);
        Log.d(TAG, "executeSyncItems: ");
        long tempIdRmsRecords = saveDvirDetailToDb(idRmsRecords, listItems, isValid);

        return tempIdRmsRecords;
    }


    public void upsyncDvirGroup(int batchSize, int maxBatches) {
        Log.d(TAG, "csv driv: upsyncDvirGroup: ");
        // 1.  send pending DVIR records to the RMS server.
        try {
            for (int i = 0; i < maxBatches; i++) {
                if (upsyncDvirs(batchSize)) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2.  send pending Signature records to the RMS server.
        try {
            for (int i = 0; i < maxBatches; i++) {
                if (upsyncSignatures(batchSize)) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 3.  send pending efile content to the server.
        try {
            for (int i = 0; i < maxBatches; i++) {
                if (upsyncEfileContent(batchSize)) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean upsyncDvirs(int maxBatchSize) throws Exception {
        Log.d(TAG, "csv driv: upsyncDvirs: ");
        // 1.  get batch of pending DVIR records to send to the RMS server.
//        List<BusHelperRmsCoding.RmsRecordCoding> list = getPendingDvirs(maxBatchSize);
        List<BusHelperRmsCoding.RmsRecordCoding> list = getPendingDvirs(maxBatchSize);

        if ((list == null || list.size() == 0))
            return true;

        // 2.  Send pending records to RMS Server
        String returnJson = sendPendingDvirs(list);

        // 3.  Mark successfully sent DVIR records as sent based on returned response.

        if (!StringUtils.isNullOrWhitespaces(returnJson)) {
//            BusHelperRmsCoding.RmsRecordCoding rmsRecordIdWork = new BusHelperRmsCoding.RmsRecordCoding();
            RecRepairWork recRepairWork = new RecRepairWork("rmsrecords");
            updateRecordsFromUpsyncResponse(returnJson, list, true, recRepairWork);
        }

        return false;
    }

    public boolean upsyncSignatures(int maxBatchSize) throws Exception {
        String strThis = "upsyncSignatures, ";
        // 1.  get batch of pending DVIR records to send to the RMS server.
        List<BusHelperRmsCoding.RmsRecordCoding> list = getPendingSignatures(maxBatchSize);

        if ((list == null || list.size() == 0))
            return true;

        // 2.  Send pending records to RMS Server
        String returnJson = sendPendingSignatures(list);

        // 3.  Mark successfully sent DVIR records as sent based on returned response.

        if (!StringUtils.isNullOrWhitespaces(returnJson)) {
//            BusHelperRmsCoding.RmsRecordCoding rmsRecordIdWork = new BusHelperRmsCoding.RmsRecordCoding();
            RecRepairWork recRepairWork = new RecRepairWork("rmsrecords");

            String errorMessage = updateRecordsFromUpsyncResponse(returnJson, list, false, recRepairWork);

            if (errorMessage != null)
                Log.d(TAG, strThis + "**** Error while updating records after sending pending signatures: " + errorMessage);
        }

//        if (true) return true; // ****** debugging.
        return false;
    }

    /**
     * Todo: This is a candidate for refactoring, moving to general class like Bus
     *
     * @param batchSize
     * @return
     * @throws Exception
     */
    public boolean upsyncEfileContent(int batchSize) throws Exception {
        Log.d(TAG, "upsyncEfileContent: batchSize: " + batchSize);
        String strThis = "upsyncEfileContent(, ";
        // 1.  get batch of pending DVIR records to send to the RMS server.
//        List<BusHelperRmsCoding.RmsRecords> list = getPendingEfileContentIds(batchSize);
        List<IRmsRecordCommon.IRmsRecCommon> list = getPendingEfileContentIds(batchSize);
        Log.d(TAG, "upsyncEfileContent: list: " + list);
        if ((list == null || list.size() == 0)) {
            return true;
        }

        // 2.  Send pending records to RMS Server
        rulesRmsCoding.sendPendingEfileContent(list);

        return false;
    }


    /**
     * Todo: This is a candidate for refactoring, moving to general class like BusHelperRmsCoding.
     *
     * @param idRmsRecordsSignature
     * @param arbytesSignatureImage
     * @param txc                   Transaction control, determines whether to use transactions.  Can be disabled.
     */
    public int saveSignatureToDb(
            long idRmsRecordsSignature,
            byte[] arbytesSignatureImage, DatabaseHelper.TxControl txc, int syncStatus) {

        if (BuildConfig.DEBUG && idRmsRecordsSignature < 0) {
            throw new AssertionError("Assertion failed due to: idRmsRecordsSignature < 0");
        }

        int iUpdated = 0;

//        byte[] arbytesSignatureImage = ImageUtils.getPngBytesFromBitmap(bitmapSignature);
//        ContentValues values = new ContentValues();
//        values.put("EfileContent", arbytesSignatureImage);
//        // Todo: do we set sent to 0 here, or set it when we know parentObjectId, parentObjectType are set?
//        values.put("sent", "0");
//        values.put("isvalid", true);
        try {
            if (txc.isUseTransaction()) getDb().beginTransaction();
//            db.update("rmsrecords", values, "Id = " + idRmsRecordsSignature);
            // Update the RMS record row
            rulesRmsCoding.updateRmsRecordsEfileContent(idRmsRecordsSignature, arbytesSignatureImage, true, syncStatus);
            iUpdated++;
            // Update or insert any necessary codingfields.
            String signatureDate = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z);
            String cmidSignatureDate = BusinessRules.getMapCodingMasterIdByName().get(Crms.SIGNATURE_DATE);
            iUpdated += rulesRmsCoding.insertOrUpdateCodingDataNoId(idRmsRecordsSignature, cmidSignatureDate, signatureDate);
            if (txc.isUseTransaction()) getDb().setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (txc.isUseTransaction()) getDb().endTransaction();
        }

        return iUpdated;
    }

    /**
     * Todo: This is a candidate for refactoring, moving to general class like BusHelperRmsCoding.
     *
     * @param idRmsRecordsParent
     * @param objectIdParent
     * @param objectTypeParent
     * @param documentTitle
     * @param signatureName
     * @param documentType
     * @param itemType
     * @param documentDate
     * @param signatureDate
     * @param arbytesSignature
     * @param txc
     * @return
     */
    public BusHelperRmsCoding.RmsRecords createNewSignatureRecord(
            long idRmsRecordsParent, String objectIdParent, String objectTypeParent,
            String documentTitle, String signatureName, String documentType, String itemType, String documentDate,
            String signatureDate, byte[] arbytesSignature, DatabaseHelper.TxControl txc) {

        String strThis = "createNewSignatureRecord(), ";
        Log.d(TAG, strThis + "Start. idRmsRecordsParent=" + idRmsRecordsParent + ", objectIdParent=" + objectIdParent
                + ", documentTitle=" + documentTitle + ", signatureName=" + signatureName + ", documentType=" + documentType
                + ", itemType=" + itemType + ", documentDate=" + documentDate + ", signatureDate=" + signatureDate
                + ", arbytesSignature.length=" + (arbytesSignature != null ? arbytesSignature.length : "(NULL)")
                + ", txc=" + txc);

        BusHelperRmsCoding.RmsRecords rmsRecordId = null;
        // Create a new signature record.
//        byte[] arbytes = ImageUtils.getPngBytesFromBitmap(bitmapSignature);
//        ContentValues values = new ContentValues();
//        if (arbytes != null && arbytes.length > 0)
//            values.put("EfileContent", arbytes);
        // Todo: do we set sent to 0 here, or set it when we know parentObjectId, parentObjectType are set?
//        values.put("sent", "0");
//        values.put("IdRmsRecordsLink", idRmsRecordsParent);
//        db.insert("rmsrecords", values);
        String mobileRecordId = Rms.getMobileRecordId(Crms.R_SIGNATURE);

        Map<String, String> mapCoding = new HashMap<>();
        mapCoding.put(Crms.MOBILERECORDID, mobileRecordId);
        mapCoding.put(Crms.DOCUMENT_TITLE, documentTitle);
        Log.d(TAG, "DOCUMENT_TITLE: createNewSignatureRecord: documentTitle: " + documentTitle);
        mapCoding.put(Crms.SIGNATURE_NAME, signatureName);
        mapCoding.put(Crms.DOCUMENT_TYPE, documentType);
        mapCoding.put(Crms.ITEMTYPE, itemType);
        mapCoding.put(Crms.DOCUMENT_DATE, documentDate);
        mapCoding.put(Crms.SIGNATURE_DATE, signatureDate);
        if (!StringUtils.isNullOrEmpty(objectIdParent))
            mapCoding.put(Crms.PARENT_OBJECTID, objectIdParent);
        if (!StringUtils.isNullOrEmpty(objectTypeParent))
            mapCoding.put(Crms.PARENT_OBJECTTYPE, objectTypeParent);

        BusHelperRmsCoding.RmsRecordType rtype = BusinessRules.getMapRecordTypeInfoFromRecordTypeName().get(Crms.R_SIGNATURE);

        long idRmsRecords = rulesRmsCoding.createNewRmsRecord(rtype.id, null, null, null,
                mobileRecordId, null, -1L, arbytesSignature, idRmsRecordsParent, true, false);

        rulesRmsCoding.insertCodingData(idRmsRecords, mapCoding, txc);

        rmsRecordId = new BusHelperRmsCoding.RmsRecords(idRmsRecords, null, rtype.objectType, mobileRecordId, Cadp.SYNC_STATUS_PENDING_UPDATE);

        Log.d(TAG, strThis + "End. idRmsRecordsParent=" + idRmsRecordsParent + ", rmsRecordId=" + rmsRecordId);

        return rmsRecordId;
    }

    public long saveDvirDetailToDb(long idRmsRecords, List<ListItemCodingDataGroup> listItems, boolean isValid) {
        String strThis = "savePreTrip: saveDvirDetailToDb(), with three parameters";

        Log.d(TAG, strThis + "Start. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
                + ", listItems.size()=" + listItems.size());

        String mobileRecordId = null;
        boolean isSaveChangedOnly = true;
        boolean isNewRecord = false;
        int iUpdatedCoding = 0;
        DatabaseHelper.TxControl txc = new DatabaseHelper.TxControl(false, true);
        DatabaseHelper.TxControl txcCalls = new DatabaseHelper.TxControl(txc.isUseTransaction(), txc.isDisableTransactions());

        Log.d(TAG, strThis + "txc=" + txc + ", txcCalls=" + txcCalls);
        Log.d(TAG, "savePreTrip: txc=" + txc + ", txcCalls=" + txcCalls);

        try {
            Log.d(TAG, strThis + "Starting database transaction.");

            if (txc.isUseTransaction()) {
                getDb().beginTransaction();  // --------------------->
                Log.d(TAG, strThis + "Database transaction started.");
            }

            // Make new rmsrecords tbl entry if this is a new DVIR.
            if (idRmsRecords < 0) {
                mobileRecordId = Rms.getMobileRecordId(RecordCommonHelper.getMobileRecordIdPrefix(Crms.R_TRUCK_DVIR_DETAIL));

                idRmsRecords = rulesRmsCoding.createNewRmsRecordMinimal(idRecordTypeDvirDetail,
                        mobileRecordId, objectTypeDvirDetail, null,
                        isValid, false, true);
                Log.d(TAG, strThis + "Case: idRmsRecords < 0, new record, created one with idRmsRecords=" + idRmsRecords
                        + ", mobileRecordId=" + mobileRecordId);
                isSaveChangedOnly = false;
                isNewRecord = true;
            }


            // update codingdata tbl with new codingfields if any are set by user.
            // non-null mobileRecordId is only passed if new rmsrecords row is created.  Otherwise,
            // we assume that the MobileRecordId is already present in the codingdata table for the record
            // from a previous save.
            iUpdatedCoding = updateOrInsertDvirDtlRecord(idRmsRecords, listItems, mobileRecordId, isSaveChangedOnly, txcCalls);

            if (!isNewRecord && (iUpdatedCoding > 0))
                rulesRmsCoding.updateRmsRecordsStatusObjIdObjType(idRmsRecords,
                        isValid, Cadp.SYNC_STATUS_PENDING_UPDATE, null, null, false);

            Log.d(TAG, strThis + "Setting database transaction successful.");
            if (txc.isUseTransaction()) {
                getDb().setTransactionSuccessful(); // --------------------->
                Log.d(TAG, strThis + "Set database transaction successful.");
            }
        } catch (Throwable e) {
            Log.d(TAG, strThis + "**** Error during database transaction. May be rolled back.", e);
        } finally {
            if (txc.isUseTransaction()) {
                Log.d(TAG, strThis + "Ending database transaction.");
                getDb().endTransaction();  // --------------------->
                Log.d(TAG, strThis + "Ended database transaction.");
            }
        }

        Log.d(TAG, strThis + "End. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
                + ", iUpdatedCoding=" + iUpdatedCoding + ", isNewRecord=" + isNewRecord
                + ", listItems.size()=" + listItems.size());
        Log.d(TAG, "savePreTrip: End. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
                + ", iUpdatedCoding=" + iUpdatedCoding + ", isNewRecord=" + isNewRecord
                + ", listItems.size()=" + listItems.size());

        return idRmsRecords;
    }

    public void saveDvirDetailToDb(long idRmsRecords, List<ListItemCodingDataGroup> listItems, boolean isValid, byte[] arEFileContent) {
        String strThis = "savePreTrip: saveDvirDetailToDb(), with four parameters";

        Log.d(TAG, strThis + "Start. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
                + ", listItems.size()=" + listItems.size());

        String mobileRecordId = null;
        boolean isSaveChangedOnly = true;
        boolean isNewRecord = false;
        int iUpdatedCoding = 0;
        DatabaseHelper.TxControl txc = new DatabaseHelper.TxControl(false, true);
        DatabaseHelper.TxControl txcCalls = new DatabaseHelper.TxControl(txc.isUseTransaction(), txc.isDisableTransactions());

        Log.d(TAG, strThis + "txc=" + txc + ", txcCalls=" + txcCalls);
        Log.d(TAG, "savePreTrip: txc=" + txc + ", txcCalls=" + txcCalls);

        try {
            Log.d(TAG, strThis + "Starting database transaction.");

            if (txc.isUseTransaction()) {
                getDb().beginTransaction();  // --------------------->
                Log.d(TAG, strThis + "Database transaction started.");
            }

            // Make new rmsrecords tbl entry if this is a new DVIR.
            if (idRmsRecords < 0) {
                mobileRecordId = Rms.getMobileRecordId(RecordCommonHelper.getMobileRecordIdPrefix(Crms.R_TRUCK_DVIR_DETAIL));

                idRmsRecords = rulesRmsCoding.createNewRmsRecordMinimal(idRecordTypeDvirDetail,
                        mobileRecordId, objectTypeDvirDetail, null,
                        isValid, false, true);
                Log.d(TAG, strThis + "Case: idRmsRecords < 0, new record, created one with idRmsRecords=" + idRmsRecords
                        + ", mobileRecordId=" + mobileRecordId);
                isSaveChangedOnly = false;
                isNewRecord = true;
            }

            // update codingdata tbl with new codingfields if any are set by user.
            // non-null mobileRecordId is only passed if new rmsrecords row is created.  Otherwise,
            // we assume that the MobileRecordId is already present in the codingdata table for the record
            // from a previous save.

//            June 24, 2022 -
//            iUpdatedCoding = updateOrInsertDvirDtlRecord(idRmsRecords, listItems, mobileRecordId, isSaveChangedOnly, txcCalls);
            Log.d(TAG, strThis + " iUpdatedCoding:" + iUpdatedCoding);

//            updateRmsRecordsEfileContent()
            updateRmsRecordsEfileContent(idRmsRecords, arEFileContent, true, 0);

            if (!isNewRecord && (iUpdatedCoding > 0)) {

                Log.d(TAG, strThis + "when if (!isNewRecord && (iUpdatedCoding > 0)): ");
                rulesRmsCoding.updateRmsRecordsStatusObjIdObjType(idRmsRecords,
                        isValid, Cadp.SYNC_STATUS_PENDING_UPDATE, null, null, false);
            }

            Log.d(TAG, strThis + "Setting database transaction successful.");
            if (txc.isUseTransaction()) {
                getDb().setTransactionSuccessful(); // --------------------->
                Log.d(TAG, strThis + "Set database transaction successful.");
            }
        } catch (Throwable e) {
            Log.d(TAG, strThis + "**** Error during database transaction. May be rolled back.", e);
        } finally {
            if (txc.isUseTransaction()) {
                Log.d(TAG, strThis + "Ending database transaction.");
                getDb().endTransaction();  // --------------------->
                Log.d(TAG, strThis + "Ended database transaction.");
            }
        }

        Log.d(TAG, strThis + "End. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
                + ", iUpdatedCoding=" + iUpdatedCoding + ", isNewRecord=" + isNewRecord
                + ", listItems.size()=" + listItems.size());
        Log.d(TAG, "savePreTrip: End. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
                + ", iUpdatedCoding=" + iUpdatedCoding + ", isNewRecord=" + isNewRecord
                + ", listItems.size()=" + listItems.size());
    }

    public int updateOrInsertDvirDtlRecord(long LidRmsRecordsDvirDtl, List<ListItemCodingDataGroup> arListItems,
                                           String mobileRecordId,
                                           boolean isChangedOnly, DatabaseHelper.TxControl txc) {
        Log.d(TAG, "updateOrInsertDvirDtlRecord: ");
        String strThis = "updateOrInsertDvirDtlRecord: updateOrInsertDvirDtlRecord(), ";
        Log.d(TAG, strThis + "Start. LidRmsRecordsDvirDtl=" + LidRmsRecordsDvirDtl + ", arListItems.size()=" + arListItems.size()
                + ", mobileRecordId=" + mobileRecordId + ", txc=" + txc);

        int iUpdatedRunning = 0;

        try {
            Log.d(TAG, "updateOrInsertDvirDtlRecord: isUseTransaction: " + txc.isUseTransaction());
            if (txc.isUseTransaction()) getDb().beginTransaction();

            Log.d(TAG, "updateOrInsertDvirDtlRecord: mobileRecordId: " + mobileRecordId);
            if (!StringUtils.isNullOrWhitespaces(mobileRecordId)) {
                Log.d(TAG, strThis + "Case mobileRecordId not null, =" + mobileRecordId + ". inserting MobilileRecordId codingfield."
                        + " CMID_MOBILE_RECORDID=" + rulesRmsCoding.CMID_MOBILE_RECORDID);

                long LidCodingData = rulesRmsCoding.insertCodingDataRow(LidRmsRecordsDvirDtl, rulesRmsCoding.CMID_MOBILE_RECORDID, mobileRecordId);
                iUpdatedRunning++;
            } else {

                Log.d(TAG, strThis + "Case mobileRecordId is null, not storing it as a codingfield.");
            }

            int ix = 0;

            Log.d(TAG, "updateOrInsertDvirDtlRecord: arListItems: " + arListItems.size());
            for (ListItemCodingDataGroup item : arListItems) {
                List<BusHelperRmsCoding.CodingDataRow> listItemCodingDataRowList = item.getListCodingdataRows();
                Log.d(TAG, strThis + "Updating ListItemCodingDataGroup " + ix++ + ", item.getLabel()=" + item.getLabel());
                iUpdatedRunning += updateOrInsertDvirDtlItem(LidRmsRecordsDvirDtl, listItemCodingDataRowList, isChangedOnly);

                // Todo: save signature.  If existing signature record, save it if updated, even if null.  If new sig record,
                // save only if non-null.
                if (item.getArBitmapItems() != null && item.getArBitmapItems().length > 0) {
                    Log.d(TAG, strThis + "Case: item.getArBitmapItems() != null, length: " + item.getArBitmapItems().length);

                    for (AdapterUtils.BitmapItem bitmapItem : item.getArBitmapItems()) {
                        Log.d(TAG, strThis + "Case: bitmapItem=" + bitmapItem);
                        if (bitmapItem.isModified) {
                            Log.d(TAG, strThis + "Case: bitmapItem.isModified=" + bitmapItem.isModified);
                            // Todo: save signature bitmap to record
                            Bitmap bitmap = bitmapItem.bitmap;
                            byte[] arEfileContent = null;
                            if (bitmap != null) {
                                Log.d(TAG, strThis + "Case: bitmap != null.");
                                arEfileContent = ImageUtils.getPngBytesFromBitmap(bitmap);
                            } else
                                Log.d(TAG, strThis + "Case: bitmap is null.");

                            if (bitmapItem.idRmsRecords >= 0) {
                                Log.d(TAG, strThis + "Case: updating existing Signaturer record, bitmapItem.idRmsRecords=" + bitmapItem.idRmsRecords);
//                                updateRmsRecordsEfileContent(bitmapItem.idRmsRecords, arEfileContent, true, false);
                                saveSignatureToDb(bitmapItem.idRmsRecords, arEfileContent, txc, Cadp.SYNC_STATUS_PENDING_UPDATE);
                            } else {
                                Log.d(TAG, strThis + "Case: new efile record, bitmapItem.idRmsRecords < 0.");
                                if (bitmapItem.bitmapClass == Cadp.BITMAP_CLASS_SIGNATURE) {
                                    Log.d(TAG, strThis + "Case: bitmapClass is signature.");
                                    if (arEfileContent != null) {
                                        Log.d(TAG, strThis + "Case: arEfileContent != null.  Creating new Signature efile record with bitmap bytes as EfileContent");
//                                            saveSignatureToDb(bitmapItem.idRmsRecords, arEfileContent, isAlreadyInTransaction);
                                        String dateTime = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z);
                                        String docTitle = null;
                                        String signatureName = null;

                                        if (bitmapItem.bitmapType == Cadp.BITMAP_TYPE_DRIVER_SIGNATURE) {
                                            docTitle = Crms.V_DRIVER_SIGNATURE;
                                            signatureName = user.getFirstLastName();
                                        } else if (bitmapItem.bitmapType == Cadp.BITMAP_TYPE_MECHANIC_SIGNATURE) {
                                            docTitle = Crms.V_MECHANIC_SIGNATURE;
                                            signatureName = item.getCombinedValue();
                                        } else {
                                            Log.d(TAG, strThis + "**** Design error. Unhandled bitmapItem.bitmapType=" + bitmapItem.bitmapType);
                                            docTitle = "(unknown signature type)";
                                        }

//                                        June 28, 2022 -
//                                        createNewSignatureRecord(LidRmsRecordsDvirDtl, item.getObjectIdRmsRecords(), item.getObjectTypeRmsRecords(), docTitle,
//                                                user.getFirstLastName(), null, Crms.ITEMTYPE_TRUCK_DVIR_DETAIL_SIGNATURE, dateTime, dateTime,
//                                                arEfileContent, txc);


                                        Log.d(TAG, "updateOrInsertDvirDtlRecord: signatureName: " + signatureName);
                                        createNewSignatureRecord(LidRmsRecordsDvirDtl, item.getObjectIdRmsRecords(), item.getObjectTypeRmsRecords(), docTitle,
                                                signatureName, null, Crms.ITEMTYPE_TRUCK_DVIR_DETAIL_SIGNATURE, dateTime, dateTime,
                                                arEfileContent, txc);

                                    } else {
                                        // I think no point in creating new efile record for null bitmap -- wait until user creates one.
                                        Log.d(TAG, strThis + "Case: arEfileContent/bitmap is not an update but is null, therefore not creating new Signature record.");
                                    }
                                } else {
                                    Log.d(TAG, strThis + "**** Design error.  Unhandled bitmapClass, do not know how to create new efile record type."
                                            + " bitmapItem=" + bitmapItem);
                                    if (BuildUtils.IS_DEBUG) {
                                        throw new RuntimeException(TAG + strThis + "**** Design error.  Unhandled bitmapClass, do not know how to create new efile record type."
                                                + " bitmapItem=" + bitmapItem + ", DVIR Detail item=" + item);
                                    }
                                }
                            }
                        } else
                            Log.d(TAG, strThis + "Case: bitmap item was not updated, no action taken. bitmapItem=" + bitmapItem);
                    }
                } else
                    Log.d(TAG, strThis + "Case: bitmap array is " + (item.getArBitmapItems() == null ? "(NULL)" : "length:" + item.getArBitmapItems().length) + " for this DVIR Detail item.");
            }

            if (txc.isUseTransaction()) getDb().setTransactionSuccessful();
        } catch (Exception e) {
            Log.d(TAG, "updateOrInsertDvirDtlRecord: exception: " + e.getMessage());
            e.printStackTrace();
        } finally {
            Log.d(TAG, "updateOrInsertDvirDtlRecord: finally: ");
            if (txc.isUseTransaction()) getDb().endTransaction();
        }


        Log.d(TAG, strThis + "End. LidRmsRecordsDvirDtl=" + LidRmsRecordsDvirDtl + ", arListItems.size()=" + arListItems.size()
                + ", mobileRecordId=" + mobileRecordId + ", iUpdatedRunning=" + iUpdatedRunning);

        return iUpdatedRunning;
    }

    public int updateOrInsertDvirDtlItem(long LidRmsRecords, List<BusHelperRmsCoding.CodingDataRow> arListCdRowItems,
                                         boolean isChangedOnly) {
        String strThis = "updateOrInsertDvirDtlItem(), ";
        Log.d(TAG, strThis + "Start. LidRmsRecords=" + LidRmsRecords + ", arListCdRowItems.size()=" + arListCdRowItems.size());

        int iUpdated = 0;

        for (BusHelperRmsCoding.CodingDataRow item : arListCdRowItems) {
            iUpdated += rulesRmsCoding.insertOrUpdateCodingDataRowFromItem(LidRmsRecords, isChangedOnly, iUpdated, item);
        }

        Log.d(TAG, strThis + "End. LidRmsRecords=" + LidRmsRecords + ", arListCdRowItems.size()=" + arListCdRowItems.size());

        return iUpdated;
    }

    public List<BusHelperRmsCoding.RmsRecordCoding> getPendingDvirs(int maxBatchCount) {
        String strThis = "getPendingDvirs(), ";
        Log.d(TAG, strThis + "Start.");

        rulesRmsCoding.lock.lock();
        List<BusHelperRmsCoding.RmsRecordCoding> list = null;

        try {
            list = rulesRmsCoding.getRmsRecordsFromDbFiltered(new String[]{Crms.R_TRUCK_DVIR_DETAIL}, null,
                    null, null, "1", "0,2",
                    maxBatchCount, false, false);
        } finally {
            rulesRmsCoding.lock.unlock();
        }

        Log.d(TAG, strThis + "End. list.size()=" + (list != null ? list.size() : "(NULL)"));
        return list;
    }

    public String sendPendingDvirs(List<BusHelperRmsCoding.RmsRecordCoding> list) throws Exception {
        String strThis = "csv driv: sendPendingDvirs(), ";
        Log.d(TAG, strThis + "Start. list.size()" + (list != null ? list.size() : "(NULL)"));

        if (list == null || list.size() == 0) {
            Log.d(TAG, strThis + "case: returning without processing, empty or null list=" + list);
            return null;
        }

        String jsonReturn = null;


        Map<String, String> mapHeader = new HashMap<>();
        mapHeader.put(Crms.DRIVER_FIRST_NAME, user.getFirstName());
        mapHeader.put(Crms.DRIVER_LAST_NAME, user.getLastName());
        mapHeader.put(Crms.DRIVER_RECORDID, user.getRecordId());
        mapHeader.put(Crms.DATETIME, DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z));


        jsonReturn = RmsHelperDvir.setTruckDvirs(mapHeader, list);

        Log.d(TAG, strThis + "csv driv: End. jsonReturn=" + jsonReturn);
        return jsonReturn;
    }

    public List<BusHelperRmsCoding.RmsRecordCoding> getPendingSignatures(int maxBatchCount) {
        String strThis = "getPendingSignatures(), ";
        Log.d(TAG, strThis + "Start. maxBatchCount=" + maxBatchCount);

        rulesRmsCoding.lock.lock(); // Todo: need to review all the locking.
        List<BusHelperRmsCoding.RmsRecordCoding> list = null;

        try {
            list = rulesRmsCoding.getRmsRecordsFromDbFiltered(new String[]{Crms.R_SIGNATURE}, null,
                    null, null, "1", "0", maxBatchCount,
                    true, true);

            Log.d(TAG, "getPendingSignatures: Todo: upsync list. by RANDY");
            // Todo: upsync list.
        } finally {
            rulesRmsCoding.lock.unlock();
        }

        Log.d(TAG, "getPendingSignatures: " + strThis + "End. list.size()=" + (list != null ? list.size() : "(NULL)"));
        return list;
    }

    /**
     * This is a candidate for refactoring, moving to some general RmsHelper class.
     *
     * @param list
     * @return
     * @throws Exception
     */
    public String sendPendingSignatures(List<BusHelperRmsCoding.RmsRecordCoding> list) throws Exception {
        String strThis = "sendPendingSignatures(), ";
        Log.d(TAG, strThis + "Start. list.size()" + (list != null ? list.size() : "(NULL)"));

        if (list == null || list.size() == 0) {
            Log.d(TAG, strThis + "case: returning without processing, empty or null list=" + list);
            return null;
        }

        String jsonReturn = null;

        jsonReturn = RmsHelperDvir.setSignatures(list);

        Log.d(TAG, strThis + "End. jsonReturn=" + jsonReturn);

        return jsonReturn;
    }

//    public List<BusHelperRmsCoding.RmsRecords> getPendingEfileContentIds(int maxBatchCount) {
//    public List<IRmsRecordCommon.IRmsRecCommon> getPendingEfileContentIds(int maxBatchCount) {
//        String strThis = "getPendingEfileContentIds(), ";
//        Log.d(TAG, strThis + "Start. maxBatchCount=" + maxBatchCount);
//
//        rulesRmsCoding.lock.lock(); // Todo: need to review all the locking.
////        List<BusHelperRmsCoding.RmsRecords> list = null;
//        List<IRmsRecordCommon.IRmsRecCommon> list = null;
//
//        try {
//            list = rulesRmsCoding.getRmsRecordIdsFromDbFiltered(null, null,
//                    null,null, null,
//                    String.valueOf(Cadp.SYNC_STATUS_PENDING_UPDATE),
//                    maxBatchCount,
//                    true, false, false);
//
//            // Todo: upsync list.
//        } finally {
//            rulesRmsCoding.lock.unlock();
//        }
//
//        Log.d(TAG, strThis + "End. list.size()=" + (list != null ? list.size() : "(NULL)"));
//        return list;
//    }

    public static String getFileExtByRecordType(String recordType) {
        String ext = null;

        if (Crms.R_SIGNATURE.equals(recordType)) ext = "png";
        else ext = "bin";

        return ext;
    }

//    @Override
//    public int deleteRecord(long idRecord, DatabaseHelper.TxControl txc) {
//        // Todo:
//        return 0;
//    }

//    @Override
//    public int insertRecord(RmsRecCommon rec) {
//        return 0;
//    }
//
//    @Override
//    public int updateRecord(RmsRecCommon rec) {
//        return 0;
//    }
    /**
     * Somewhat complicated method to update a batch of records from the response of a batched upsync operation.
     * The complications are all the validations done to trap design errors that might arise
     * from batched upsyncs/updates or corrupt data.  Upsyncing records one at a time would be simpler, but in general, could
     * be much slower.
     *
     * @param jsonSetterResponse  - JSON response returned from a "set.." RMS call to update the RMS.
     * @param listUpSyncRecords - The original list of records going into the "set.." call.  It is passed
     *                          here for the purpose of helping validate the response and update
     *                          the status of the upsynced records.
     */
//    public <K extends BusHelperRmsCoding.RmsRecords> String updateRecordsFromUpsyncResponse (
//            String jsonSetterResponse, List <K> listUpSyncRecords, boolean isIgnoreHeaders) {
//        String strThis = "updateRecordsFromUpsyncResponse(), ";
//        String errorMessage = null;
//
//        lock.lock();
//        int ixJsonRecord = 0;
//
//        try {
//            JSONArray arjsonResponse = new JSONArray(jsonSetterResponse);
//
//            if (arjsonResponse != null && arjsonResponse.length() > 0) {
//                BusHelperRmsCoding.RmsRecords rmsRecordIdWork = new BusHelperRmsCoding.RmsRecords();
//
//                for (int j = 0; j < arjsonResponse.length(); j++) {
//                    JSONObject jsonResponse = arjsonResponse.getJSONObject(j);
//                    // Todo: make Crms. constants for response member names.
//                    if (!isIgnoreHeaders)
//                        processUpsyncResponseJsonRecord(listUpSyncRecords, ixJsonRecord++, rmsRecordIdWork, jsonResponse);
//
//                    JSONArray arjsonDvirDetailRecs = jsonResponse.getJSONArray("arDetailRecordDataMapped");
//                    if (arjsonDvirDetailRecs.length() != listUpSyncRecords.size())
//                        Log.d(TAG, strThis + "\n ****** Warning - different number of response records than sent records."
//                                + ", listUpSyncRecords.size()=" + listUpSyncRecords.size()
//                                + ", arjsonDvirDetailRecs.length()=" + arjsonDvirDetailRecs.length());
//
//                    for (int i = 0; i < arjsonDvirDetailRecs.length(); i++) {
//                        JSONObject jsonDetailRec = arjsonDvirDetailRecs.getJSONObject(i);
//                        processUpsyncResponseJsonRecord(listUpSyncRecords, ixJsonRecord++, rmsRecordIdWork, jsonDetailRec);
//                    }
//                }
//            }
//            else
////                Log.d(TAG, strThis + "**** Warning.  arjsonResponse was empty. jsonSetterResponse=" + jsonSetterResponse);
//                errorMessage = TAG + " " + strThis + "**** Warning.  arjsonResponse was empty. jsonSetterResponse=" + jsonSetterResponse;
//        }
//        catch(Throwable e) {
//            errorMessage = TAG + " " + strThis + "**** jsonSetterResponse **** Error parsing and processing upsync response." + e
//                    + "  Cannot update sync status of records. jsonSetterResponse=" + jsonSetterResponse;
//            Log.d(TAG, strThis + errorMessage, e);
//        } finally {
//            lock.unlock();
//        }
//
//        return errorMessage;
//    }

//    public <K extends BusHelperRmsCoding.RmsRecords> void processUpsyncResponseJsonRecord(List<K> listUpSyncRecords, int ixList, BusHelperRmsCoding.RmsRecords rmsRecordIdWork, JSONObject jsonDetailRec) throws JSONException {
//        String mobileRecordId = jsonDetailRec.optString("mobileRecordId");
//        String objectId = jsonDetailRec.getString("LobjectId");
//        String objectType = jsonDetailRec.getString("objectType");
//        String recordType = (objectType != null ? BusinessRules.getMapRecordTypeInfoFromObjectType().get(objectType).recordTypeName : "(unknown)");
//
//        String errorCode = jsonDetailRec.optString(Crms.REST_RESPONSE_MEMBER_ERROR_CODE);
//        String operation = jsonDetailRec.optString(Crms.REST_RESPONSE_MEMBER_OPERATION);
//
//        if (errorCode != null && !Crms.REST_RESPONSE_ERROR_CODE_SUCCESS.equals(errorCode)) {
//            processUpsyncResponseRecordError(jsonDetailRec, mobileRecordId,
//                    objectId, objectType, recordType, errorCode, operation, listUpSyncRecords, ixList, rmsRecordIdWork);
//        } else {
//            processUpsyncResponseRecordSuccess(jsonDetailRec, mobileRecordId,
//                    objectId, objectType, recordType, operation, listUpSyncRecords, ixList, rmsRecordIdWork);
//        }
//
//    }

//    /**
//     * @param jsonDetailRec
//     * @param mobileRecordId
//     * @param objectId
//     * @param objectType
//     * @param recordType
//     * @param listUpsyncRecords
//     * @param ixResponseRecord
//     */
//
//    public <K extends BusHelperRmsCoding.RmsRecords> void processUpsyncResponseRecordSuccess(
//            JSONObject jsonDetailRec, String mobileRecordId, String objectId, String objectType,
//            String recordType, List <K> listUpsyncRecords,
//            int ixResponseRecord, BusHelperRmsCoding.RmsRecords rmsRecordIdWork) {
//
//        String strThis = "processUpsyncResponseRecordSuccess(), ";
//
//        BusHelperRmsCoding.RmsRecords rmsRecordId = rulesRmsCoding.findRepairRecord(
//                mobileRecordId, objectId, objectType, true, recordType, ixResponseRecord, null, rmsRecordIdWork);
//
//        if (ixResponseRecord < listUpsyncRecords.size()) {
//            long idExpectedRecord = listUpsyncRecords.get(ixResponseRecord).idRmsRecords;
//
//            if (idExpectedRecord != rmsRecordId.idRmsRecords)
//                Log.d(TAG, strThis + "**** Warning. Unexpected record found to update status.  Possible repair done. idExpectedRecord="
//                        + idExpectedRecord + ", updated rmsrecord Id: " + rmsRecordId.idRmsRecords + "\n"
//                        + ", rmsRecordId=" + rmsRecordId + ", expectedRecord: " + listUpsyncRecords.get(ixResponseRecord));
//        }
//
//        if (rmsRecordId != null && rmsRecordId.idRmsRecords >= 0) {
//            int iUpdated = 0;
//
//            int syncStatus = (rmsRecordId.isRepaired() ? Cadp.SYNC_STATUS_PENDING_UPDATE : Cadp.SYNC_STATUS_SENT);
//
//            if (StringUtils.isNullOrWhitespaces(rmsRecordId.objectId) || StringUtils.isNullOrWhitespaces(rmsRecordId.objectType))
//                iUpdated = rulesRmsCoding.updateRmsRecordsStatus(rmsRecordId.idRmsRecords, true,
//                        syncStatus, objectId, objectType, false);
//            else
//                iUpdated = rulesRmsCoding.updateRmsRecordsStatus(rmsRecordId.idRmsRecords, true,
//                        syncStatus, null, null, false);
//
//            if (iUpdated > 0)
//                Log.d(TAG, strThis + "Updated record: " + rmsRecordId + " status to " + syncStatus + ", valid.");
//            else
//                Log.d(TAG, strThis + "***** Error. Failed to update record: " + rmsRecordId
//                        + " status to " + syncStatus + ", valid.");
//        }
//    }

//    public <K extends BusHelperRmsCoding.RmsRecords> void processUpsyncResponseRecordError (
//            JSONObject jsonDetailRec, String mobileRecordId, String objectId, String objectType,
//            String recordType, String errorCode, List <K> listUpsyncRecords, int ixResponseRecord,
//            BusHelperRmsCoding.RmsRecords rmsRecordIdWork) {
//
//        String strThis = "processUpsyncResponseRecordError(), ";
//
//        Log.d(TAG, strThis + "**** Sync error.  A " + recordType + " record was not updated on the RMS server" +
//                " for record with"
//                + " mobileRecordId=" + mobileRecordId + ", objectId=" + objectId
//                + ", objectType=" + objectType + ", recordType=" + recordType +
//                ", errorCode: " + errorCode
//                + ", jsonDetailRec: " + jsonDetailRec);
//
//        BusHelperRmsCoding.RmsRecords rmsRecordId = rulesRmsCoding.findRepairRecord(
//                mobileRecordId, objectId, objectType, true, recordType, ixResponseRecord, null, rmsRecordIdWork);
//
//        if (rmsRecordId != null && rmsRecordId.idRmsRecords >= 0) {
//            int iUpdated = rulesRmsCoding.updateRmsRecordsStatus(rmsRecordId.idRmsRecords,
//                    false, Cadp.SYNC_STATUS_SENT, null, null);
//            if (iUpdated > 0)
//                Log.d(TAG, strThis + "Updated record: " + rmsRecordId + " status to sent, invalid.");
//            else
//                Log.d(TAG, strThis + "***** Error. Failed to update record: " + rmsRecordId + " status to sent, invalid.");
//        }
//    }

//    /**
//     * Delete case at this time does not pertain to DVIR records as they do not have a delete function. -RAN 2/26/2021
//     * @param jsonDetailRec
//     * @param mobileRecordId
//     * @param objectId
//     * @param objectType
//     * @param recordType
//     * @param errorCode
//     * @param listUpsyncRecords
//     * @param ixResponseRecord
//     * @param rmsRecordIdWork
//     * @param <K>
//     */
//    public <K extends BusHelperRmsCoding.RmsRecords> void processUpsyncResponseRecordError(
//            JSONObject jsonDetailRec, String mobileRecordId, String objectId, String objectType,
//            String recordType, String errorCode, List<K> listUpsyncRecords, int ixResponseRecord,
//            BusHelperRmsCoding.RmsRecords rmsRecordIdWork) {
//
//        String strThis = "processUpsyncResponseRecordError(), ";
//
//        Log.d(TAG, strThis + "**** Sync error.  A " + recordType + " record was not updated on the RMS server" +
//                " for record with"
//                + " mobileRecordId=" + mobileRecordId + ", objectId=" + objectId
//                + ", objectType=" + objectType + ", recordType=" + recordType +
//                ", errorCode: " + errorCode
//                + ", jsonDetailRec: " + jsonDetailRec);
//
//        BusHelperRmsCoding.RmsRecords rmsRecordId = rulesRmsCoding.findRepairRecord(
//                mobileRecordId, objectId, objectType, true, recordType, ixResponseRecord, null, rmsRecordIdWork);
//
//        String operation = null;
//
//        try {
//            operation = jsonDetailRec.getString(Crms.REST_RESPONSE_MEMBER_OPERATION);
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
//
//        if (rmsRecordId != null && rmsRecordId.idRmsRecords >= 0) {
//            // Handle special record-not-found cases for DELETE and UPDATE operations.
//            if (rmsRecordId.objectId != null && rmsRecordId.objectType != null && Crms.REST_RESPONSE_ERROR_CODE_RECORD_NOT_FOUND.equals(errorCode)) {
//                if (Crms.REST_CSV_OPERATION_DELETE.equals(operation)) {
//                    if (rmsRecordId.getSyncStatusBeforeRepair() == Cadp.SYNC_STATUS_MARKED_FOR_DELETE) {
//                        int iUpdated = rulesRmsCoding.deleteRmsRecordAndCodingData(rmsRecordId.idRmsRecords,
//                                new DatabaseHelper.TxControl(false, false));
//                    }
//                }
//                else {
//                    // Special case - ObjectId, ObjectType not blank in local record but record not found on server and not a delete operation,
//                    // assume it was an update operation.
//                    // We will blank out the objectid, objecttype and try again next sync cycle.
//                    int iUpdated = rulesRmsCoding.updateRmsRecordsStatus(rmsRecordId.idRmsRecords,
//                            true, rmsRecordId.getSyncStatusBeforeRepair(), null, null, true);
//                }
//            }
//            else {
//                int iUpdated = rulesRmsCoding.updateRmsRecordsStatus(rmsRecordId.idRmsRecords,
//                        false, Cadp.SYNC_STATUS_SENT, null, null, false);
//                if (iUpdated > 0)
//                    Log.d(TAG, strThis + "Updated record: " + rmsRecordId + " status to sent, invalid.");
//            }
//        } else {
//            Log.d(TAG, strThis + "***** Design Error. Couldn't find and failed to update local db record, rmsRecordId={" + rmsRecordId
//                    + "}, " + " mobileRecordId=" + mobileRecordId + ", objectId=" + objectId
//                    + ", objectType=" + objectType + ", recordType=" + recordType + ", errorCode: " + errorCode);
//        }
//    }

}
