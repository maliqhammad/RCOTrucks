package com.rco.rcotrucks.activities.fuelreceipts.utils;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.activities.CreateTollReceipt;
import com.rco.rcotrucks.activities.fuelreceipts.activities.FuelReceiptDtlActivity;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.FuelReceiptListAdapter;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.SyncTask;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.IRmsRecordCommon;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecRepairWork;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordCommonHelper;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.ImageUtils;
import com.rco.rcotrucks.utils.MathUtils;
import com.rco.rcotrucks.utils.StringUtils;

import java.lang.ref.WeakReference;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class extends BusinessRules purely to have access to static db and lock members.   Eventually this
 * code can be migrated into BusinessRules.
 */

public class BusHelperFuelReceipts extends BusHelperRmsCoding {
    private static final String TAG = "BusHelperFuelReceipts";

    public static final String MOBILERECORDID_PREFIX_FUEL_RECEIPT_DTL = "FuelReceipt";
    public static final int UPSYNC_MAX_BATCH_SIZE = 20;
    public static BusHelperFuelReceipts instance;
    public static int MAX_FUEL_RECEIPT_BATCH_COUNT;
    private BusinessRules rules;
    public BusHelperRmsCoding rulesRmsCoding;
    //    private String rmsRecordTypesUrlEncoded;
    private static long idRecordTypeFuelReceiptDetail = -1L;
    private static String objectTypeFuelReceiptDetail;
    private User user;
    //    private String odometer;
    private SQLiteStatement stmtRmsRecordsUpdateIsValidByMobid; // not thread safe.

    public static Context getContext() throws Exception {
        return context;
    }

    public static void setContext(Context context) {
        BusHelperFuelReceipts.context = context;
    }

    private static Context context;

    public BusHelperFuelReceipts(DatabaseHelper db) {
        super(db);
        rules = BusinessRules.instance();
        rulesRmsCoding = BusHelperRmsCoding.instance();
        user = BusinessRules.instance().getAuthenticatedUser();
//        odometer = BusinessRules.instance().getOdometer();
//        rmsRecordTypesUrlEncoded = urlEncodeCommaDelimited(RMS_CODINGDATA_RECORD_TYPES).toString();
    }

    public String getOdometer() {
        return rules.getOdometer();
    }

    public static synchronized BusHelperFuelReceipts instance() {
        // Todo: conditionally instantiate instance.
        if (instance == null) instance = new BusHelperFuelReceipts(RecordRulesHelper.getDb());

        return instance;
    }

//    private static String lockFuelReceiptWaitMessage;
//    private long FuelReceiptLockMillis;
//    public static ReentrantLock lockFuelReceiptData = new ReentrantLock();
//
//    public static boolean tryLockFuelReceiptData(Context ctx, String waitMessage, long maxMillisBeforeCancel) {
//        boolean ret = false;
//        try
//    }

    public String getOrgName() {
        return Rms.getOrgName();
    }

    /**
     * arCodingDataSetupTruckFuelReceiptDetail is an array of rows of template codingfield descriptors for the Truck FuelReceipt Detail record.
     * The columns are as followes:
     * 0.  Codingfield Rms Name
     * 1.  Codingfield Rms datatype name.
     * 2.  Android display type -- can be any code that helps the software display the field, such as a RecyclerView view type integer.
     * 3.  (Optional if subsequent columns are missing.  Can be blank.)  Android field display name -- sometimes slightly different from the Rms codingfield name.  Defaults to Codingfield name.
     * 4.  (Optional) EditMode numeric constant - see FuelReceiptDtlAdapter EDIT_MODE_.. constants. Can be used by UI code or ignored.
     * 5.  (Optional) IsRequired - set to 1 if required, blank or 0 if not. Can be used by UI code or ignored.
     * 6.  (Optional CombineType - set to a Cadp.COMBINE_TYPE_.. value that can be used by UI code to create a composite value from multiple codingfields.  If it is null, then it will be combined with the next non-null row to make a displayed item.
     * 7.  (Optional) Android display position in a list, sortable.  Defaults to the index of the row in this array.
     * 8.  (Optional) column relative position in setter CSV file.  May not be used at this time.
     * <p>
     * Note: items with null "display type" are typically grouped and displayed with the first following non-null "display type".
     */
    public boolean initFuelReceiptCodingSetup(boolean isAlreadyInTransaction) {
        String strThis = "initFuelReceiptCodingSetup(), ";
        Log.d(TAG, strThis + "Start. isAlreadyInTransaction=" + isAlreadyInTransaction);
        boolean isSuccess = true;
//        dateTime = getVal(arstrColVals, ix++);
//        salesTax = getVal(arstrColVals, ix++);
//        refund = getVal(arstrColVals, ix++);
//        firstName = getVal(arstrColVals, ix++);
//        lastName = getVal(arstrColVals, ix++);
//        driverLicenseNumber = getVal(arstrColVals, ix++);
//        company = getVal(arstrColVals, ix++);
//        fuelCode = getVal(arstrColVals, ix++);
//        fuelType = getVal(arstrColVals, ix++);
//        truckNumber = getVal(arstrColVals, ix++);
//        dOTNumber = getVal(arstrColVals, ix++);
//        odometer = getVal(arstrColVals, ix++);
//        vehicleLicenseNumber = getVal(arstrColVals, ix++);
//        vendorName = getVal(arstrColVals, ix++);
//        vendorAddress = getVal(arstrColVals, ix++);
//        vendorState = getVal(arstrColVals, ix++);
//        vendorCountry = getVal(arstrColVals, ix++);
//        purchasersName = getVal(arstrColVals, ix++);
//        priceperGallon = getVal(arstrColVals, ix++);
//        numberofGallonsPurchased = getVal(arstrColVals, ix++);
//        totalAmountofSaleinUSD = getVal(arstrColVals, ix++);

        String[][] arCodingDataSetup = new String[][]{

                {Crms.TOTAL_AMOUNT_OF_SALE_IN_USD, Crms.D_DECIMAL, String.valueOf(Cadp.VIEWTYPE_HDR_FLD)},
                {Crms.NUMBER_OF_GALLONS_PURCHASED, Crms.D_DECIMAL, String.valueOf(Cadp.VIEWTYPE_HDR_FLD), "Gallons"},
                {Crms.ODOMETER, Crms.D_NUMERIC, String.valueOf(Cadp.VIEWTYPE_HDR_FLD)},
                {Crms.SALES_TAX, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_HDR_FLD)},
                {Crms.FUEL_CODE, Crms.D_STRING, null},
                {Crms.FUEL_TYPE, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_HDR_SPIN_HID)},
                {Crms.FUEL_TYPE, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_HDR_SPIN_HID)},
                {Crms.VENDOR_NAME, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_HDR_FLD)},
                {Crms.VENDOR_STATE, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_HDR_SPIN_HID)},
                {Crms.DATETIME, Crms.D_DATE, String.valueOf(Cadp.VIEWTYPE_HDR_FLD), "Date"},
                {Crms.REFUND, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.FIRST_NAME, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.LAST_NAME, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.DRIVER_LICENSE_NUMBER, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.COMPANY, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.TRUCK_NUMBER, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.DOT_NUMBER, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.VEHICLE_LICENSE_NUMBER, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.VENDOR_ADDRESS, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.VENDOR_COUNTRY, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.PURCHASERS_NAME, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.PRICE_PER_GALLON, Crms.D_DECIMAL, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
//                {Crms.RECORDID, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.USER_RECORD_ID, Crms.D_STRING, String.valueOf(Cadp.VIEWTYPE_EXTRA)},
                {Crms.TOTAL_AMOUNT, Crms.D_NUMERIC, String.valueOf(Cadp.VIEWTYPE_HDR_FLD)},
        };

        rulesRmsCoding.addCodingDataSetup(Crms.R_FUEL_RECEIPT, arCodingDataSetup, isAlreadyInTransaction);
        Log.d(TAG, strThis + "End.");

        return isSuccess;
    }

    public static void initRmsConvenienceMembers() {
        BusHelperRmsCoding.RmsRecordType rtype = BusinessRules.getMapRecordTypeInfoFromRecordTypeName().get(Crms.R_FUEL_RECEIPT);
        if (rtype == null)
            return;
        idRecordTypeFuelReceiptDetail = rtype.id;
        objectTypeFuelReceiptDetail = rtype.objectType;
        Log.d(TAG, "initRmsConvenienceMembers() done.");
    }


    public String urlEncodeCommaDelimited(String[] arItems) {
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


    private static List<FuelReceiptListAdapter.ListItemFuelReceipt> listFuelReceipts = null;

    public static List<FuelReceiptListAdapter.ListItemFuelReceipt> getListFuelReceipts() {
        return listFuelReceipts;
    }


    private static List<ListItemCodingDataGroup> listFuelReceiptDetail = null;

    public static List<ListItemCodingDataGroup> getListFuelReceiptDetail() {
        return listFuelReceiptDetail;
    }

    private static List<ListItemCodingDataGroup> listExtra;

    public static List<ListItemCodingDataGroup> getListExtra() {
        return listExtra;
    }


    /**
     * @throws Exception
     * @deprecated
     */
    @Deprecated
    public void syncFuelReceiptItems() throws Exception {

        String response = Rms.getRecordsUpdatedXFiltered(Crms.R_FUEL_RECEIPT, -5000,
                Crms.DRIVER_FIRST_NAME + "," + Crms.DRIVER_LAST_NAME, "%2C",
                user.getFirstName() + "," + user.getLastName(), "%2C%3B",
                Crms.DATETIME + "," + Crms.TRUCK_NUMBER + "," + Crms.TRAILER1_NUMBER + "," + Crms.TRAILER2_NUMBER + "," + Crms.RECORDID);


        Log.d(TAG, "syncFuelReceiptItems: response: " + response);
        if (listFuelReceipts == null)
            listFuelReceipts = new ArrayList();
        else
            listFuelReceipts.clear();

        DateUtils.IDateConverter dateConverter = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_TIME_MILLIS,
                DateUtils.FORMAT_DATE_MM_DD_YY, DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS);

        listFuelReceipts = AdapterUtils.syncItems(response, listFuelReceipts, FuelReceiptListAdapter.ListItemFuelReceipt.class, dateConverter);

        if (listFuelReceipts != null)
            Collections.sort(listFuelReceipts);
    }

    public void loadFuelReceiptItems(String startDate, String endDate) throws Exception {
        Log.d(TAG, "loadFuelReceiptItems() Start.");

        DateUtils.IDateConverter dateConverter = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_TIME_MILLIS,
                DateUtils.FORMAT_DATE_MM_DD_YY, DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS);

        List<String> listParams = new ArrayList<>();

//        String strSql = FuelReceiptListAdapter.getItemListSql(idRecordTypeFuelReceiptDetail, listParams);
        String strSql = "";
        if (!startDate.isEmpty() && !endDate.isEmpty()) {
            strSql = FuelReceiptListAdapter.getItemListSqlInDateRange(idRecordTypeFuelReceiptDetail, listParams
                    , startDate, endDate);
        } else {
            strSql = FuelReceiptListAdapter.getItemListSql(idRecordTypeFuelReceiptDetail, listParams);
        }

        Log.d(TAG, "loadFuelReceiptItems: strSql: " + strSql);

        String[] arParams = (String[]) listParams.toArray(new String[listParams.size()]);

        Log.d(TAG, "loadFuelReceiptItems() strSql=\n" + strSql + "\n listParams=" + listParams
                + ", arParams=" + StringUtils.dumpArray(arParams));

        Cursor cursor = null;

        if (listFuelReceipts == null) {
            Log.d(TAG, "loadFuelReceiptItems() initializing listFuelReceipts.");
            listFuelReceipts = new ArrayList<>();
        } else {
            Log.d(TAG, "loadFuelReceiptItems() clearing listFuelReceipts.");
            listFuelReceipts.clear();
        }

        try {
            cursor = getDb().getQuery(strSql, arParams);

            listFuelReceipts = AdapterUtils.loadItemsFromDatabase(cursor, listFuelReceipts, FuelReceiptListAdapter.ListItemFuelReceipt.class, dateConverter);
        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }

//        if (listFuelReceipts != null)
//            Collections.sort(listFuelReceipts);

        Log.d(TAG, "loadFuelReceiptItems() End.");
    }

    public void loadFuelReceiptDtlItems(Context ctx, long idRmsRecordsFuelReceiptDtl, String objectIdFuelReceiptDetail, String objectTypeFuelReceiptDetail) throws Exception {
        String strThis = "loadFuelReceiptDtlItems(), ";

        Log.d(TAG, strThis + "Start. idRmsRecordsFuelReceiptDtl=" + idRmsRecordsFuelReceiptDtl);

        String strSql = null;
        Cursor cursor = null;


        try {

            if (listFuelReceiptDetail == null) {
                Log.d(TAG, strThis + "initializing listFuelReceiptDetail.");
                listFuelReceiptDetail = new ArrayList<>();
            } else {
                Log.d(TAG, strThis + "clearing listFuelReceiptDetail.");
                listFuelReceiptDetail.clear();
            }

            if (listExtra == null) {
                Log.d(TAG, strThis + "initializing listExtra.");
                listExtra = new ArrayList<>();
            } else {
                Log.d(TAG, strThis + "clearing listExtra.");
                listExtra.clear();
            }

            List<String> listParams = new ArrayList<>();
            String[] arParams = null;
            Bitmap bitmap = null;

            if (idRmsRecordsFuelReceiptDtl >= 0) {
                // Case: fetch FuelReceipt Detail codingfields for the selected FuelReceipt identified by id: idRmsRecordsFuelReceiptDtl.
//                strSql = BusHelperRmsCoding.getRmsRecordSql(String.valueOf(idRmsRecordsFuelReceiptDtl),
//                        Crms.R_FUEL_RECEIPT, listParams);
//                arParams = (String[]) listParams.toArray();
                Log.d(TAG, strThis + "Case: idRmsRecordsFuelReceiptDtl=" + idRmsRecordsFuelReceiptDtl + ", >= 0, loading existing record.");
                bitmap = rulesRmsCoding.getEfileBitmapByIdRmsRecords(String.valueOf(idRmsRecordsFuelReceiptDtl));
//                        "\n arParams=" + StringUtils.dumpArray(arParams));
//                cursor = db.getQuery(strSql, new String[]{String.valueOf(idRmsRecordsFuelReceiptDtl), Crms.R_FUEL_RECEIPT});
                cursor = rulesRmsCoding.getRmsRecordCursor(String.valueOf(idRmsRecordsFuelReceiptDtl), Crms.R_FUEL_RECEIPT);
//                rulesRmsCoding.getRmsRecordCursorUpdateForFuel(String.valueOf(idRmsRecordsFuelReceiptDtl), Crms.R_FUEL_RECEIPT);


            } else {
                // Case: fetch FuelReceipt Detail codingfield template for entering a new FuelReceipt.
//                strSql = getNewRmsRecordSql();
                Log.d(TAG, strThis + "Case: idRmsRecordsFuelReceiptDtl < 0, loading new record detail.");
//                cursor = db.getQuery(strSql, new String[]{Crms.R_FUEL_RECEIPT});
                cursor = rulesRmsCoding.getNewRmsRecordCursor(Crms.R_FUEL_RECEIPT);
//                rulesRmsCoding.getNewRmsRecordCursorUpdate(Crms.R_FUEL_RECEIPT);
            }

            Log.d(TAG, "loadFuelReceiptDtlItems: cursor: count: " + cursor.getCount());


            listFuelReceiptDetail = loadFuelReceiptDtlItemsFromDatabase(ctx, idRmsRecordsFuelReceiptDtl, objectIdFuelReceiptDetail,
                    objectTypeFuelReceiptDetail, bitmap, cursor, listFuelReceiptDetail, listExtra);

        } finally {
            if (cursor != null && !cursor.isClosed()) cursor.close();
        }

        Log.d(TAG, strThis + "End. listFuelReceiptDetail.size()=" + (listFuelReceiptDetail != null ? listFuelReceiptDetail.size() : "(NULL)"));
    }

//    private static String[] FROM_DATE_TIME_FORMAT_LIST = {DateUtils.FORMAT_DATE_TIME_MILLIS, DateUtils.FORMAT_ISO_SSS_Z};

    /**
     * This is the workhorse method that creates the Detail list for the recycler view presented
     * in the {recordtype}DtlActivity screen.  It has various exception logic for initializing special items
     * such as compound views that have multiple views such as name and signature image, etc.
     * It relies heavily on the information from the codingdatasetup for the Detail record type
     * to drive the logic.  See corresponding init{recordtype}DetailSetup() for the specific field configuration data.  For
     * example, the "CombineType" column assists in combining several codingfields into a single
     * RecyclerView item.
     *
     * @param idRmsRecordsDtl
     * @param objectIdDtl
     * @param objectTypeDtl
     * @param cursor
     * @param listItems
     * @return
     * @throws Exception
     */
    public List<ListItemCodingDataGroup> loadFuelReceiptDtlItemsFromDatabase(
            Context ctx, long idRmsRecordsDtl, String objectIdDtl, String objectTypeDtl, Bitmap bitmapReceipt,
            Cursor cursor, List<ListItemCodingDataGroup> listItems, List<ListItemCodingDataGroup> listExtra) {
        String strThis = "loadFuelReceiptDtlItemsFromDatabase(), ";
        Log.d(TAG, strThis + "Start. idRmsRecordsDtl=" + idRmsRecordsDtl + ", objectIdDtl=" + objectIdDtl
                + ", objectTypeDtl=" + objectTypeDtl
                + ", bitmapReceipt.getByteCount()=" + (bitmapReceipt != null ? bitmapReceipt.getByteCount() : "(NULL)")
                + ", listItems.size()=" + (listItems != null ? listItems.size() : "(NULL)")
                + ", listExtra.size()=" + (listExtra != null ? listExtra.size() : "(NULL)")
                + ", cursor.getColumnNames()=" + StringUtils.dumpArray(cursor.getColumnNames()));

        // Todo: these date converters can be made more permanent I think, so long as no thread conflicts. -RAN
        DateUtils.IDateConverter dateConverterToLocal = new DateUtils.DateConverterParser(DateUtils.FROM_DATE_TIME_FORMAT_LIST,
                DateUtils.FORMAT_DATE_MM_DD_YYYY, DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_AMPM);

        DateUtils.IDateConverter dateConverterFromLocalDate = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_MM_DD_YYYY,
                DateUtils.FORMAT_ISO_SSS_Z, DateUtils.FORMAT_ISO_SSS_Z);
//                DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_SS_SSS, DateUtils.FORMAT_DATE_TIME_MILLIS);

        DateUtils.IDateConverter dateConverterFromLocalDateTime = new DateUtils.DateConverterParser(DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_AMPM,
//                DateUtils.FORMAT_DATE_TIME_MILLIS, DateUtils.FORMAT_DATE_TIME_MILLIS);
                DateUtils.FORMAT_ISO_SSS_Z, DateUtils.FORMAT_ISO_SSS_Z);

        DateUtils.IDateConverter dateConvFromLocal = null;
        DateUtils.IDateConverter dateConvToLocal = null;
        String idRecordTypeSignature = String.valueOf(BusinessRules.getMapRecordTypeInfoFromRecordTypeName().get(Crms.R_SIGNATURE).id);
        String strIdRmsRecordsDtl = String.valueOf(idRmsRecordsDtl);
        String cmidDocumentTitle = BusinessRules.getMapCodingMasterIdByName().get(Crms.DOCUMENT_TITLE);
        Log.d(TAG, strThis + "DOCUMENT_TITLE: loadFuelReceiptDtlItemsFromDatabase: cmidDocumentTitle: " + cmidDocumentTitle);
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
        ArrayAdapter<CharSequence> spinnerAdapter = null;
//        listItems.clear();

        List<BusHelperRmsCoding.CodingDataRow> listCodingRowItems = null;

        // First item is a special record-level image of the receipt.
        ListItemCodingDataGroup itemReceiptImage = new ListItemCodingDataGroup();
        AdapterUtils.BitmapItem bitmapItem = new AdapterUtils.BitmapItem(bitmapReceipt,
                Cadp.BITMAP_CLASS_RECORD_CONTENT, Cadp.BITMAP_TYPE_NONE, idRmsRecordsDtl);
        arBitmaps = new AdapterUtils.BitmapItem[]{bitmapItem};

        itemReceiptImage.init(idRmsRecordsDtl, objectIdDtl, objectTypeDtl, null,
                arBitmaps, Cadp.COMBINE_TYPE_NONE,
                null, null, null,
                Cadp.VIEWTYPE_PIC, Cadp.EDIT_MODE_EDITABLE, false, null);

        listItems.add(itemReceiptImage);

        int irow = 0;
        while (cursor.moveToNext()) {
            bitmap = null;
            arBitmaps = null;
            spinnerAdapter = null;
            int ixSelectedSpinnerItem = 0;
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

//            Nov 24, 2022  -   We don't want to show two fields like in usd and in normal amount so added this check
            if (codingFieldName.equalsIgnoreCase("Amount")) {
                continue;
            }

            Log.d(TAG, strThis + "Case: top of loop, codingFieldName=" + codingFieldName + ", viewType=" + viewType + ", dataTypeName=" + dataTypeName + ", codingvalue=" + codingvalue);

            if (Crms.DATETIME.equals(codingFieldName) || Crms.DATE.equals(codingFieldName)) {
                // Conversion to local date format occurs in item.init().  It might be more appropriate here?
                Log.d(TAG, strThis + "Case: date type codingFieldName=" + codingFieldName + ", displayName=" + displayName + ", dataTypeName=" + dataTypeName + ", codingvalue=" + codingvalue);
                // Todo: get logged in user's address?
                if (StringUtils.isNullOrWhitespaces(codingvalue)) {
                    Log.d(TAG, strThis + "Case: blank date date codingvalue, initializing with current local datetime. Not considering timezone at this point.");
//                    July 21, 2022 -
                    codingvalue = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_TIME_MILLIS);
//                    codingvalue = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_MM_DD_YYYY);
                    Log.d(TAG, strThis + "Case: blank date date codingvalue initialized with current datetime: " + codingvalue);
                }
            } else if (Crms.ODOMETER.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue))
                    codingvalue = getOdometer();
            } else if (Crms.TRUCK_NUMBER.equals(codingFieldName)) {
                if (StringUtils.isNullOrWhitespaces(codingvalue))
                    codingvalue = user.getTruckNumber();
            } else if (Crms.FIRST_NAME.equals(codingFieldName) && StringUtils.isNullOrWhitespaces(codingvalue)) {
                codingvalue = user.getFirstName();
            } else if (Crms.LAST_NAME.equals(codingFieldName) && StringUtils.isNullOrWhitespaces(codingvalue)) {
                codingvalue = user.getLastName();
            } else if (Crms.COMPANY.equals(codingFieldName) && StringUtils.isNullOrWhitespaces(codingvalue)) {
                codingvalue = user.getCompany();
            } else if (Crms.DRIVER_LICENSE_NUMBER.equals(codingFieldName) && StringUtils.isNullOrWhitespaces(codingvalue)) {
                codingvalue = user.getDriversLicenseNumber();
            } else if (Crms.PURCHASERS_NAME.equals(codingFieldName) && StringUtils.isNullOrWhitespaces(codingvalue)) {
                codingvalue = user.getFirstLastName();
            }  else if (Crms.USER_RECORD_ID.equals(codingFieldName) && StringUtils.isNullOrWhitespaces(codingvalue)) {
                codingvalue = user.getRecordId();
                Log.d(TAG, "loadFuelReceiptDtlItemsFromDatabase: codingvalue: "+codingvalue);
            } else if (Crms.FUEL_TYPE.equals(codingFieldName)) {
//                combineType = Cadp.COMBINE_TYPE_FIRST_LAST_NAME; // Should work for Fuel code, type.
//                spinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.fuel_types, android.R.layout.simple_spinner_item);
//                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            } else if (Crms.VENDOR_STATE.equals(codingFieldName)) {
                combineType = Cadp.COMBINE_TYPE_CODE_OF_SELECTION; // Should work for Fuel code, type.
//                Nov 24, 2022  -   Updated states name without abbreviations
//                spinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.states_provinces, android.R.layout.simple_spinner_item);
                spinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.states_provinces_name, android.R.layout.simple_spinner_item);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            } else if (Crms.VENDOR_COUNTRY.equals(codingFieldName)) {
                combineType = Cadp.COMBINE_TYPE_FIRST_LAST_NAME; // Should work for Fuel code, type.
                spinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.country, android.R.layout.simple_spinner_item);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            }
//            else if (Crms.TOTAL_AMOUNT.equals(codingFieldName)) {
//                spinnerAdapter = ArrayAdapter.createFromResource(ctx, R.array.country, android.R.layout.simple_spinner_item);
//                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

//            }
//            else if (Crms.TOTAL_AMOUNT_OF_SALE_IN_USD.equals(codingFieldName)) {
//                long idRmsRecordsBitmap = -1;
//
//                if (idRmsRecordsDtl >= 0) {
//                    rmsRecordId = BusHelperRmsCoding.getEfileBitmapByParent(codingFieldName, idRecordTypeSignature,
//                            idRmsRecordsDtl, cmidParentObjId, objectIdDtl, cmidParentObjType,
//                            objectTypeDtl, cmidDocumentTitle, Crms.TOTAL_AMOUNT_OF_SALE_IN_USD, rmsRecordId);
//
//                    if (rmsRecordId != null) {
//                        idRmsRecordsBitmap =rmsRecordId.idRmsRecords;
//                        bitmap = (Bitmap) rmsRecordId.getExtra();
//                        };
//
//                }
//
//                // even if bitmap is null, create the holder so user can create new signature and store it here.
//                arBitmaps = new AdapterUtils.BitmapItem[] {
//                        new AdapterUtils.BitmapItem(bitmap, Cadp.BITMAP_CLASS_SIGNATURE, Cadp.BITMAP_TYPE_MECHANIC_SIGNATURE,
//                                idRmsRecordsBitmap)
//                };
//
//                Log.d(TAG, strThis + "Case: " + codingFieldName
//                        + ", bitmap is " + (bitmap == null ? "NULL" : "NOT NULL") + ", idRmsRecordsBitmap=" + idRmsRecordsBitmap
//                        + ", idRmsRecordsDtl=" + idRmsRecordsDtl + ", objectIdDtl=" + objectIdDtl + ", objectTypeDtl=" + objectTypeDtl
//                        + ", arBitmaps[0]=" + (arBitmaps != null ? arBitmaps[0] : "(NULL)"));
//            }


            if (Crms.D_DATE.equals(dataTypeName)) {
                dateConvFromLocal = dateConverterFromLocalDate;
                dateConvToLocal = dateConverterToLocal;
            } else if (Crms.D_DATETIME.equals(dataTypeName)) {
                dateConvFromLocal = dateConverterFromLocalDateTime;
                dateConvToLocal = dateConverterToLocal;
            } else {
                dateConvFromLocal = null;
                dateConvToLocal = null;
            }

            BusHelperRmsCoding.CodingDataRow item = new BusHelperRmsCoding.CodingDataRow();

            Log.d(TAG, strThis + "irow=" + irow + ", idCodingData=" + idCodingData
                    + ", codingFieldName=" + codingFieldName
                    + ", displayName=" + displayName + ", codingvalue=" + codingvalue + ", viewType=" + viewType + ", "
                    + " dataTypeName=" + dataTypeName + ", iEditMode=" + iEditMode + ", isRequired=" + isRequired
                    + ", dateConvToLocal=" + dateConvToLocal + ", dateConvFromLocal=" + dateConvFromLocal);

            item.init(idCodingData, idRmsRecordsDtl, codingMasterId, codingFieldName, displayName, codingvalue, codingvalueOrig,
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

                listItem.init(idRmsRecordsDtl, objectIdDtl, objectTypeDtl,
                        listCodingRowItems, arBitmaps, combineType, displayName,
                        null, null, viewType, iEditMode, isRequired, spinnerAdapter);

                if (viewType != Cadp.VIEWTYPE_EXTRA) {
                    listItems.add(listItem);
                    Log.d(TAG, strThis + "Case: added listItem with list of codingfield row items to listItems, viewType >= 0, viewType=" + viewType + ", irow=" + irow
                            + ", listItem.getLabel()=" + listItem.getLabel() + ", listItem.getCombinedValue()=" + listItem.getCombinedValue()
                            + ", listCodingRowItems.size()=" + listCodingRowItems.size() + ", listItems.size()=" + listItems.size());
                } else {
                    listExtra.add(listItem);
                    Log.d(TAG, strThis + "Case: added listItem with list of codingfield row items to listExtra, viewType >= 0, viewType=" + viewType + ", irow=" + irow
                            + ", listItem.getLabel()=" + listItem.getLabel() + ", listItem.getCombinedValue()=" + listItem.getCombinedValue()
                            + ", listCodingRowItems.size()=" + listCodingRowItems.size() + ", listItems.size()=" + listItems.size());
                }

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

    /**
     * cursor from select SQL that returns id, EfileContent of rmsrecords row.
     *
     * @param sql
     * @param arParams
     * @return should return null if no record found.  May return null bitmap.
     */
    public BusHelperRmsCoding.RmsRecords getEfileBitmap(
            String sql, String[] arParams, BusHelperRmsCoding.RmsRecords rmsRecordId) {

        String strThis = "getEfileBitmap(), ";
        Log.d(TAG, strThis + "Start. sql=\n" + sql + "\n, arParams=" + StringUtils.dumpArray(arParams));

        Cursor cur = null;
        Bitmap bmp = null;
        byte[] arEfileContent = null;
        long idRmsRecords = -1L;
        BusHelperRmsCoding.RmsRecords rmsRecId = null;

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
                    rmsRecId = new BusHelperRmsCoding.RmsRecords(idRmsRecords);
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

    public void runSaveFuelReceiptDtlTask(final FuelReceiptDtlActivity activity, final long idRmsRecords,
                                          final List<ListItemCodingDataGroup> listItems, final List<ListItemCodingDataGroup> listExtra,
                                          final BusHelperRmsCoding.IExtraListPostProcessor extraListPostProcessor,
                                          final boolean isMarkForDelete) {
        final String strThis = "runSaveFuelReceiptDtlTask(): saveFuelReceiptDetailToDb(): ";

        Log.d(TAG, strThis + " Start.");
        final WeakReference<FuelReceiptDtlActivity> activityWeakReference = new WeakReference<>(activity);

        SyncTask task = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, strThis + ".task.loadScreen() start.");
                // Don't need to close activity here any more because launching FuelReceipt Report
                // in parallel, and return from that will finish the activity. -RAN 2/5/21
//                FuelReceiptDtlActivity activity = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
//                if (activity != null && !activity.isFinishing())
//                {
//                    Log.d(TAG, strThis + ".task.loadScreen() calling activity.finish().");
//                    activity.finish();
//                }
                Log.d(TAG, strThis + ".task.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                try {

                    Log.d(TAG, "executeSyncItems: runSaveFuelReceiptDtlTask() task.executeSyncItems() Start. about to run BusHelperRmsCoding.saveFuelReceiptDetailToDb().");

                    // Todo: validate whether items being saved are valid for sending to RMS Server (esp. required fields).
                    boolean isValid = true;

//                    Log.d(TAG, "executeSyncItems: listItems: "+listItems.size());
                    for (ListItemCodingDataGroup item : listItems) {

                        Log.d(TAG, "executeSyncItems: bitmap: " + item.getArBitmapItems());
                        if (item.isRequired() && StringUtils.isNullOrWhitespaces(item.getCombinedValue())) {
                            isValid = false;
                            break;
                        }
                    }

                    if (!isMarkForDelete) {
                        if (extraListPostProcessor != null)
                            extraListPostProcessor.process(listItems, listExtra);

                        Log.d(TAG, "executeSyncItems: saveFuelReceipt: ");
                        saveFuelReceiptDetailToDb(idRmsRecords, listItems, listExtra, isValid);
                    } else
                        rulesRmsCoding.updateRmsRecordsStatusObjIdObjType(idRmsRecords,
                                isValid, Cadp.SYNC_STATUS_MARKED_FOR_DELETE, null, null, false);

                    Log.d(TAG, "runSaveFuelReceiptDtlTask() task.executeSyncItems() Start about to run UiHelperFuelReceiptDtl.runSyncUpdateRmsFuelReceiptsTask().");

                    // Todo: for now, we will try an upsync after every save operation.  Need to integrate
                    //  with larger strategy.  Need concurrency control.
//                    if (!isMarkForDelete)
                    UiHelperFuelReceiptDtl.instance().runSyncUpdateRmsFuelReceiptsTask(UPSYNC_MAX_BATCH_SIZE);

                    Log.d(TAG, "runSaveFuelReceiptDtlTask() task.executeSyncItems() End.");
                } catch (Throwable e) {
                    Log.d(TAG, "runSaveFuelReceiptDtlTask() task.executeSyncItems() **** Exception.", e);
                }
            }
        }, "Refreshing FuelReceipt usage data...");

//        Log.d(TAG, "runSaveFuelReceiptDtlTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "runSaveFuelReceiptDtlTask() starting task, serial to prevent conflict with refresh FuelReceipt detail task.");
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    public void runSaveFuelReceiptDtlTask(final Activity activity, final long idRmsRecords,
                                          final List<ListItemCodingDataGroup> listItems, final List<ListItemCodingDataGroup> listExtra,
                                          final BusHelperRmsCoding.IExtraListPostProcessor extraListPostProcessor,
                                          final boolean isMarkForDelete) {
        final String strThis = "runSaveFuelReceiptDtlTask(): saveFuelReceiptDetailToDb(): ";

        Log.d(TAG, strThis + " Start.");
        final WeakReference<Activity> activityWeakReference = new WeakReference<>(activity);

        SyncTask task = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, strThis + ".task.loadScreen() start.");
                // Don't need to close activity here any more because launching FuelReceipt Report
                // in parallel, and return from that will finish the activity. -RAN 2/5/21
//                FuelReceiptDtlActivity activity = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
//                if (activity != null && !activity.isFinishing())
//                {
//                    Log.d(TAG, strThis + ".task.loadScreen() calling activity.finish().");
//                    activity.finish();
//                }
                Log.d(TAG, strThis + ".task.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                try {

                    Log.d(TAG, "updateRmsRecordsStatusObjIdObjType: executeSyncItems: runSaveFuelReceiptDtlTask() task.executeSyncItems() Start. about to run BusHelperRmsCoding.saveFuelReceiptDetailToDb().");

                    // Todo: validate whether items being saved are valid for sending to RMS Server (esp. required fields).
                    boolean isValid = true;

//                    Log.d(TAG, "executeSyncItems: listItems: "+listItems.size());
                    for (ListItemCodingDataGroup item : listItems) {

                        Log.d(TAG, "updateRmsRecordsStatusObjIdObjType: executeSyncItems: bitmap: " + item.getArBitmapItems());
                        if (item.isRequired() && StringUtils.isNullOrWhitespaces(item.getCombinedValue())) {
                            isValid = false;
                            break;
                        }
                    }

                    if (!isMarkForDelete) {
                        if (extraListPostProcessor != null)
                            extraListPostProcessor.process(listItems, listExtra);

                        Log.d(TAG, "updateRmsRecordsStatusObjIdObjType: executeSyncItems: saveFuelReceipt: ");
                        saveFuelReceiptDetailToDb(idRmsRecords, listItems, listExtra, isValid);
                    } else
                        rulesRmsCoding.updateRmsRecordsStatusObjIdObjType(idRmsRecords,
                                isValid, Cadp.SYNC_STATUS_MARKED_FOR_DELETE, null, null, false);

                    Log.d(TAG, "updateRmsRecordsStatusObjIdObjType: runSaveFuelReceiptDtlTask() task.executeSyncItems() Start about to run UiHelperFuelReceiptDtl.runSyncUpdateRmsFuelReceiptsTask().");

                    // Todo: for now, we will try an upsync after every save operation.  Need to integrate
                    //  with larger strategy.  Need concurrency control.
//                    if (!isMarkForDelete)
                    UiHelperFuelReceiptDtl.instance().runSyncUpdateRmsFuelReceiptsTask(UPSYNC_MAX_BATCH_SIZE);

                    Log.d(TAG, "updateRmsRecordsStatusObjIdObjType: runSaveFuelReceiptDtlTask() task.executeSyncItems() End.");
                } catch (Throwable e) {
                    Log.d(TAG, "updateRmsRecordsStatusObjIdObjType: runSaveFuelReceiptDtlTask() task.executeSyncItems() **** Exception.", e);
                }
            }
        }, "Refreshing FuelReceipt usage data...");

//        Log.d(TAG, "runSaveFuelReceiptDtlTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "updateRmsRecordsStatusObjIdObjType: runSaveFuelReceiptDtlTask() starting task, serial to prevent conflict with refresh FuelReceipt detail task.");
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    public void runSaveFuelReceiptDtlTaskUpdate(final CreateTollReceipt activity, final long idRmsRecords,
                                                final List<ListItemCodingDataGroup> listItems, final List<ListItemCodingDataGroup> listExtra,
                                                final BusHelperRmsCoding.IExtraListPostProcessor extraListPostProcessor,
                                                final boolean isMarkForDelete) {
        final String strThis = "runSaveFuelReceiptDtlTask()";

        Log.d(TAG, strThis + " Start.");
        final WeakReference<CreateTollReceipt> activityWeakReference = new WeakReference<>(activity);

        SyncTask task = new SyncTask(activity, new SyncTask.IRefreshTaskMethods() {
            @Override
            public void loadScreen() {
                Log.d(TAG, strThis + ".task.loadScreen() start.");
                // Don't need to close activity here any more because launching FuelReceipt Report
                // in parallel, and return from that will finish the activity. -RAN 2/5/21
//                FuelReceiptDtlActivity activity = activityWeakReference.get();
                // Todo: maybe need notify if adapter already attached, optimization.
//                if (activity != null && !activity.isFinishing())
//                {
//                    Log.d(TAG, strThis + ".task.loadScreen() calling activity.finish().");
//                    activity.finish();
//                }
                Log.d(TAG, strThis + ".task.loadScreen() end.");
            }

            @Override
            public void executeSyncItems() throws Exception {
                try {

                    Log.d(TAG, "runSaveFuelReceiptDtlTask() task.executeSyncItems() Start. about to run BusHelperRmsCoding.saveFuelReceiptDetailToDb().");

                    // Todo: validate whether items being saved are valid for sending to RMS Server (esp. required fields).
                    boolean isValid = true;

                    for (ListItemCodingDataGroup item : listItems) {

                        if (item.isRequired() && StringUtils.isNullOrWhitespaces(item.getCombinedValue())) {
                            isValid = false;
                            break;
                        }
                    }

                    Log.d(TAG, "saveFuelReceiptDetailToDb(): executeSyncItems: isMarkForDelete: " + isMarkForDelete);
                    if (!isMarkForDelete) {
                        if (extraListPostProcessor != null)
                            extraListPostProcessor.process(listItems, listExtra);

                        saveFuelReceiptDetailToDb(idRmsRecords, listItems, listExtra, isValid);
                    } else
                        rulesRmsCoding.updateRmsRecordsStatusObjIdObjType(idRmsRecords,
                                isValid, Cadp.SYNC_STATUS_MARKED_FOR_DELETE, null, null, false);

                    Log.d(TAG, "runSaveFuelReceiptDtlTask() task.executeSyncItems() Start about to run UiHelperFuelReceiptDtl.runSyncUpdateRmsFuelReceiptsTask().");

                    // Todo: for now, we will try an upsync after every save operation.  Need to integrate
                    //  with larger strategy.  Need concurrency control.
//                    if (!isMarkForDelete)
                    UiHelperFuelReceiptDtl.instance().runSyncUpdateRmsFuelReceiptsTask(UPSYNC_MAX_BATCH_SIZE);

                    Log.d(TAG, "runSaveFuelReceiptDtlTask() task.executeSyncItems() End.");
                } catch (Throwable e) {
                    Log.d(TAG, "runSaveFuelReceiptDtlTask() task.executeSyncItems() **** Exception.", e);
                }
            }
        }, "Refreshing FuelReceipt usage data...");

//        Log.d(TAG, "runSaveFuelReceiptDtlTask() starting task, parallel as a design default in case other AsyncTasks are running in background.");
//        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        Log.d(TAG, "runSaveFuelReceiptDtlTask() starting task, serial to prevent conflict with refresh FuelReceipt detail task.");
        task.executeOnExecutor(AsyncTask.SERIAL_EXECUTOR);
    }


    public void upsyncFuelReceiptGroup(int batchSize, int maxBatches) {

        // 1.  send pending FuelReceipt records to the RMS server.
        try {
            for (int i = 0; i < maxBatches; i++) {
                if (upsyncFuelReceipts(batchSize)) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2.  send pending efile content to the server.
        try {
            for (int i = 0; i < maxBatches; i++) {
                if (upsyncEfileContent(batchSize)) break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean upsyncFuelReceipts(int maxBatchSize) throws Exception {
        String strThis = "upsyncFuelReceipts(), ";

        Log.d(TAG, strThis + "Start. maxBatchSize=" + maxBatchSize);

        // 1.  get batch of pending FuelReceipt records to send to the RMS server.
        List<BusHelperRmsCoding.RmsRecordCoding> list = getPendingFuelReceipts(maxBatchSize);
        Log.d(TAG, strThis + "After getPendingFuelReceipts(), list.size()=" + list.size());

        if ((list == null || list.size() == 0))
            return true;

        // 2.  Send pending records to RMS Server
        String returnJson = sendPendingFuelReceipts(list);
        Log.d(TAG, strThis + "After sendPendingFuelReceipts(), returnJson.length()=" + (returnJson != null ? returnJson.length() : "(NULL)"));
        // 3.  Mark successfully sent FuelReceipt records as sent based on returned response.

        if (!StringUtils.isNullOrWhitespaces(returnJson)) {
//            BusHelperRmsCoding.RmsRecordCoding rmsRecordIdWork = new BusHelperRmsCoding.RmsRecordCoding();
            RecRepairWork rmsRecordIdWork = new RecRepairWork("rmsrecords");

            String errorMessage = updateRecordsFromUpsyncResponse(returnJson, list, false, rmsRecordIdWork);
            if (errorMessage != null)
                Log.d(TAG, strThis + "**** Error while updating records after sending pending signatures: " + errorMessage);

        } else
            Log.d(TAG, strThis + "Case: empty returnJson, no update of records from response done.");

        return false;
    }

    public boolean upsyncEfileContent(int batchSize) throws Exception {
        String strThis = "upsyncEfileContent(, ";
        // 1.  get batch of pending FuelReceipt records to send to the RMS server.
//        List<BusHelperRmsCoding.RmsRecords> list = getPendingEfileContentIds(batchSize);
        List<IRmsRecordCommon.IRmsRecCommon> list = getPendingEfileContentIds(batchSize);

        if ((list == null || list.size() == 0)) {
            Log.d(TAG, "upsyncEfileContent: list: " + list);
            return true;
        }

        // 2.  Send pending records to RMS Server
        sendPendingEfileContent(list);

        return false;
    }

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
        Log.d(TAG, "DOCUMENT_TITLE: createNewSignatureRecord: documentTitle: " + documentTitle);
        mapCoding.put(Crms.DOCUMENT_TITLE, documentTitle);
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

    public void saveFuelReceiptDetailToDb(long idRmsRecords, List<ListItemCodingDataGroup> listItems,
                                          List<ListItemCodingDataGroup> listExtra, boolean isValid) {
        String strThis = "saveFuelReceiptDetailToDb: ";

        Log.d(TAG, strThis + "Start. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
                + ", listItems.size()=" + listItems.size());

        String mobileRecordId = null;
        boolean isSaveChangedOnly = true;
        boolean isNewRecord = false;
        int iUpdatedCoding = 0;

        try {
            Log.d(TAG, strThis + "Starting database transaction.");
            getDb().beginTransaction();  // --------------------->
            Log.d(TAG, strThis + "Database transaction started.");

            // Make new rmsrecords tbl entry if this is a new FuelReceipt.
            Log.d(TAG, "saveFuelReceiptDetailToDb: idRmsRecords: " + idRmsRecords);
            if (idRmsRecords < 0) {
                mobileRecordId = Rms.getMobileRecordId(RecordCommonHelper.getMobileRecordIdPrefix(Crms.R_FUEL_RECEIPT));

                idRmsRecords = rulesRmsCoding.createNewRmsRecordMinimal(idRecordTypeFuelReceiptDetail,
                        mobileRecordId, objectTypeFuelReceiptDetail, null,
                        isValid, false, true);
                Log.d(TAG, strThis + "Case: idRmsRecords < 0, new record, created one with idRmsRecords=" + idRmsRecords
                        + ", mobileRecordId=" + mobileRecordId);
                isSaveChangedOnly = false;
                isNewRecord = true;
            }

            Log.d(TAG, "saveFuelReceiptDetailToDb: isNewRecord: " + isNewRecord);

            // update codingdata tbl with new codingfields if any are set by user.
            // non-null mobileRecordId is only passed if new rmsrecords row is created.  Otherwise,
            // we assume that the MobileRecordId is already present in the codingdata table for the record
            // from a previous save.
            Log.d(TAG, strThis + "About to save listItems, listItems.size()=" + listItems.size() + ", iUpdatedCoding=" + iUpdatedCoding);
            iUpdatedCoding = updateOrInsertFuelReceiptDtlRecord(idRmsRecords, listItems, mobileRecordId, isSaveChangedOnly, true);

            Log.d(TAG, strThis + "About to save listExtra, listExtra.size()=" + listExtra.size() + ", iUpdatedCoding=" + iUpdatedCoding);
            iUpdatedCoding += updateOrInsertFuelReceiptDtlRecord(idRmsRecords, listExtra, null, isSaveChangedOnly, true);

            Log.d(TAG, "saveFuelReceiptDetailToDb: if (!isNewRecord && iUpdatedCoding > 0): " + (!isNewRecord && iUpdatedCoding > 0));
            if (!isNewRecord && iUpdatedCoding > 0)
                rulesRmsCoding.updateRmsRecordsStatusObjIdObjType(idRmsRecords,
                        isValid, Cadp.SYNC_STATUS_PENDING_UPDATE, null, null, false);

            Log.d(TAG, strThis + "Setting database transaction successful. iUpdatedCoding=" + iUpdatedCoding);
            getDb().setTransactionSuccessful(); // --------------------->
            Log.d(TAG, strThis + "Set database transaction successful.");
        }
//        catch (Throwable e) {
//            Log.d(TAG, strThis + "**** Error during database transaction. May be rolled back.", e);
//        }
        finally {
            Log.d(TAG, strThis + "Ending database transaction.");
            getDb().endTransaction();  // --------------------->
            Log.d(TAG, strThis + "Ended database transaction.");
        }

        Log.d(TAG, strThis + "End. idRmsRecords=" + idRmsRecords + ", isValid=" + isValid
                + ", iUpdatedCoding=" + iUpdatedCoding + ", isNewRecord=" + isNewRecord
                + ", listItems.size()=" + listItems.size());
    }

    void printFuelReceiptValues(List<ListItemCodingDataGroup> arListItems) {
        for (int i = 0; i < arListItems.size(); i++) {
            Log.d(TAG, "printFuelReceiptValues: Label: " + arListItems.get(i).getLabel() + " Value: " + arListItems.get(i).getCombinedValue());
        }
    }

    public int updateOrInsertFuelReceiptDtlRecord(long LidRmsRecordsFuelReceiptDtl, List<ListItemCodingDataGroup> arListItems,
                                                  String mobileRecordId,
                                                  boolean isChangedOnly, boolean isAlreadyInTransaction) {
        String strThis = "executeSyncItems: updateOrInsertFuelReceiptDtlRecord:(), ";
        Log.d(TAG, strThis + "Start. LidRmsRecordsFuelReceiptDtl=" + LidRmsRecordsFuelReceiptDtl + ", arListItems.size()=" + arListItems.size()
                + ", mobileRecordId=" + mobileRecordId);

        printFuelReceiptValues(arListItems);
        int iUpdatedRunning = 0;

        try {
            if (!isAlreadyInTransaction) getDb().beginTransaction();

            if (!StringUtils.isNullOrWhitespaces(mobileRecordId)) {
                Log.d(TAG, strThis + "Case mobileRecordId not null, =" + mobileRecordId + ". inserting MobilileRecordId codingfield."
                        + " rulesRmsCoding.CMID_MOBILE_RECORDID=" + rulesRmsCoding.CMID_MOBILE_RECORDID);

                long LidCodingData = rulesRmsCoding.insertCodingDataRow(LidRmsRecordsFuelReceiptDtl, rulesRmsCoding.CMID_MOBILE_RECORDID, mobileRecordId);
                iUpdatedRunning++;
            } else
                Log.d(TAG, strThis + "Case mobileRecordId is null, not storing it as a codingfield.");

            int ix = 0;

            for (ListItemCodingDataGroup item : arListItems) {
                List<BusHelperRmsCoding.CodingDataRow> listItemCodingDataRowList = item.getListCodingdataRows();
                Log.d(TAG, "validate: index: item.getLabel()=" + item.getLabel() + " item.getCombinedValue()=" + item.getCombinedValue());
                Log.d(TAG, strThis + "Updating ListItemCodingDataGroup " + ix++ + ", item.getLabel()=" + item.getLabel() + " item.getCombinedValue()=" + item.getCombinedValue());
                iUpdatedRunning += updateOrInsertFuelReceiptDtlItem(LidRmsRecordsFuelReceiptDtl, listItemCodingDataRowList, isChangedOnly);

                // save only if non-null.
                Log.d(TAG, strThis + "inside: for loop: item: arBitmapItem: " + item.getArBitmapItems());
                if (item.getArBitmapItems() != null && item.getArBitmapItems().length > 0) {
                    Log.d(TAG, strThis + "updateOrInsertFuelReceiptDtlRecord: ArBitmapItems: size: " + item.getArBitmapItems().length);
                    Log.d(TAG, strThis + "Case: item.getArBitmapItems() != null, length: " + item.getArBitmapItems().length);

                    for (AdapterUtils.BitmapItem bitmapItem : item.getArBitmapItems()) {
                        Log.d(TAG, strThis + "Case: bitmapItem=" + bitmapItem);
                        if (bitmapItem.isModified) {
                            Log.d(TAG, strThis + "Case: bitmapItem.isModified=" + bitmapItem.isModified);
                            Bitmap bitmap = bitmapItem.bitmap;
                            byte[] arEfileContent = null;

                            if (bitmap != null) {
                                Log.d(TAG, strThis + "Case: bitmap != null.");
                                arEfileContent = ImageUtils.getPngBytesFromBitmap(bitmap);
                            } else
                                Log.d(TAG, strThis + "Case: bitmap is null.");

                            if (bitmapItem.idRmsRecords >= 0) {
                                Log.d(TAG, strThis + "Case: updating existing Signature record, bitmapItem.idRmsRecords=" + bitmapItem.idRmsRecords);
//                                rulesRmsCoding.updateRmsRecordsEfileContent(bitmapItem.idRmsRecords, arEfileContent, true, false);
//                                saveSignatureToDb(bitmapItem.idRmsRecords, arEfileContent, isAlreadyInTransaction);
                                rulesRmsCoding.updateRmsRecordsEfileContent(bitmapItem.idRmsRecords, arEfileContent, true, Cadp.SYNC_STATUS_PENDING_UPDATE);
                            } else {
//                                June 23, 2022 -   We should use idRms that we are getting as an parameter
                                Log.d(TAG, strThis + "Case: ***** Assertion error, bitmapItem.idRmsRecords < 0. Should have id of main record." +
                                        "LidRmsRecordsFuelReceiptDtl: " + LidRmsRecordsFuelReceiptDtl);
                                rulesRmsCoding.updateRmsRecordsEfileContent(LidRmsRecordsFuelReceiptDtl, arEfileContent, true, Cadp.SYNC_STATUS_PENDING_UPDATE);
                            }
                        } else
                            Log.d(TAG, strThis + "Case: bitmap item was not updated, no action taken. bitmapItem=" + bitmapItem);
                    }
                } else
                    Log.d(TAG, strThis + "Case: bitmap array is " + (item.getArBitmapItems() == null ? "(NULL)" : "length:" + item.getArBitmapItems().length) + " for this FuelReceipt Detail item.");
            }

            if (!isAlreadyInTransaction) getDb().setTransactionSuccessful();
//        } catch (Exception e) {
//            e.printStackTrace();
        } finally {
            if (!isAlreadyInTransaction) getDb().endTransaction();
        }


        Log.d(TAG, strThis + "End. LidRmsRecordsFuelReceiptDtl=" + LidRmsRecordsFuelReceiptDtl + ", arListItems.size()=" + arListItems.size()
                + ", mobileRecordId=" + mobileRecordId + ", iUpdatedRunning=" + iUpdatedRunning);

        return iUpdatedRunning;
    }

    public int updateOrInsertFuelReceiptDtlItem(long LidRmsRecords, List<BusHelperRmsCoding.CodingDataRow> arListCdRowItems,
                                                boolean isChangedOnly) {
        String strThis = "updateOrInsertFuelReceiptDtlItem(), ";
        Log.d(TAG, strThis + "Start. LidRmsRecords=" + LidRmsRecords
                + ", arListCdRowItems.size()=" + (arListCdRowItems != null ? arListCdRowItems.size() : ("NULL)")));

        int iUpdated = 0;

        if (arListCdRowItems != null) {
            for (BusHelperRmsCoding.CodingDataRow item : arListCdRowItems) {

                iUpdated += rulesRmsCoding.insertOrUpdateCodingDataRowFromItem(LidRmsRecords, isChangedOnly, iUpdated, item);

            }
        } else Log.d(TAG, strThis + "arListCdRowItems is null, nothing to save.");

        Log.d(TAG, strThis + "End. LidRmsRecords=" + LidRmsRecords
                + ", arListCdRowItems.size()=" + (arListCdRowItems != null ? arListCdRowItems.size() : "(NULL)"));

        return iUpdated;
    }

    public List<BusHelperRmsCoding.RmsRecordCoding> getPendingFuelReceipts(int maxBatchCount) {
        String strThis = "getPendingFuelReceipts(), ";
        Log.d(TAG, strThis + "Start.");

        BusHelperRmsCoding.lock.lock();
        List<BusHelperRmsCoding.RmsRecordCoding> list = null;

        try {
            list = rulesRmsCoding.getRmsRecordsFromDbFiltered(new String[]{Crms.R_FUEL_RECEIPT}, null,
                    null, null, "1", Cadp.SYNC_STATUS_PENDING_UPDATE + "," + Cadp.SYNC_STATUS_MARKED_FOR_DELETE,
                    maxBatchCount, false, false);
        } finally {
            BusHelperRmsCoding.lock.unlock();
        }

        Log.d(TAG, strThis + "End. list.size()=" + (list != null ? list.size() : "(NULL)"));
        return list;
    }

    public String sendPendingFuelReceipts(List<BusHelperRmsCoding.RmsRecordCoding> list) throws Exception {
        String strThis = "sendPendingFuelReceipts(), ";
        Log.d(TAG, strThis + "Start. list.size()" + (list != null ? list.size() : "(NULL)"));

        if (list == null || list.size() == 0) {
            Log.d(TAG, strThis + "case: returning without processing, empty or null list=" + list);
            return null;
        }

        String jsonReturn = null;

        jsonReturn = RmsHelperFuelReceipts.setFuelReceipts(list);

        Log.d(TAG, strThis + "End. jsonReturn=" + jsonReturn);
        return jsonReturn;
    }

    public List<BusHelperRmsCoding.RmsRecordCoding> getPendingSignatures(int maxBatchCount) {
        String strThis = "getPendingSignatures(), ";
        Log.d(TAG, strThis + "Start. maxBatchCount=" + maxBatchCount);

        BusHelperRmsCoding.lock.lock(); // Todo: need to review all the locking.
        List<BusHelperRmsCoding.RmsRecordCoding> list = null;

        try {
            list = rulesRmsCoding.getRmsRecordsFromDbFiltered(new String[]{Crms.R_SIGNATURE}, null,
                    null, null, "1", "0", maxBatchCount,
                    true, true);

            // Todo: upsync list.
        } finally {
            BusHelperRmsCoding.lock.unlock();
        }

        Log.d(TAG, strThis + "End. list.size()=" + (list != null ? list.size() : "(NULL)"));
        return list;
    }


//    public List<BusHelperRmsCoding.RmsRecords> getPendingEfileContentIds(int maxBatchCount) {
//    public List<IRmsRecordCommon.IRmsRecCommon> getPendingEfileContentIds(int maxBatchCount) {
//        String strThis = "getPendingEfileContentIds(), ";
//        Log.d(TAG, strThis + "Start. maxBatchCount=" + maxBatchCount);
//
//        BusHelperRmsCoding.lock.lock(); // Todo: need to review all the locking.
////        List<BusHelperRmsCoding.RmsRecords> list = null;
//        List<IRmsRecordCommon.IRmsRecCommon> list = null;
//
//        try {
//            list = rulesRmsCoding.getRmsRecordIdsFromDbFiltered(null, null,
//                    null,null, null,
//                    Cadp.SYNC_STATUS_PENDING_UPDATE + "," + Cadp.SYNC_STATUS_MARKED_FOR_DELETE,
//                    maxBatchCount,
//                    true, false, false);
//
//            // Todo: upsync list.
//        } finally {
//            BusHelperRmsCoding.lock.unlock();
//        }
//
//        Log.d(TAG, strThis + "End. list.size()=" + (list != null ? list.size() : "(NULL)"));
//        return list;
//    }

    public static String getFileExtByRecordType(String recordType) {
        Log.d(TAG, "getFileExtByRecordType: recordType: " + recordType);
        String ext = null;

        if (Crms.R_TRUCK_DVIR_DETAIL.equals(recordType)) ext = "pdf";
        else if (Crms.R_SIGNATURE.equals(recordType)) ext = "png";
        else if (Crms.R_FUEL_RECEIPT.equals(recordType)) ext = "png";
        else ext = "bin";

        Log.d(TAG, "getFileExtByRecordType: ext: " + ext);
        return ext;
    }

    private FuelReceiptExtraPostProcessor fuelReceiptExtraPostProcessor;

    public FuelReceiptExtraPostProcessor getFuelReceiptExtraPostProcessor() {
        if (fuelReceiptExtraPostProcessor == null)
            fuelReceiptExtraPostProcessor = new FuelReceiptExtraPostProcessor();
        return fuelReceiptExtraPostProcessor;
    }

//    @Override
//    public int deleteRecord(long idRecord, DatabaseHelper.TxControl txc) {
//        // Todo: implement.
//        return 0;
//    }
//
//    @Override
//    public int insertRecord(RmsRecCommon rec) {
//        // Todo: implement.
//        return 0;
//    }
//
//    @Override
//    public int updateRecord(RmsRecCommon rec) {
//        // Todo: implement.
//        return 0;
//    }


    // region: Nested Classes / Interfaces

    // ====================================================== Nested Classes / Interfaces ======================================

    public static class FuelReceiptExtraPostProcessor implements BusHelperRmsCoding.IExtraListPostProcessor<ListItemCodingDataGroup> {
        @Override
        public List<ListItemCodingDataGroup> process(List<ListItemCodingDataGroup> listItems, List<ListItemCodingDataGroup> listExtra) {
            // Calculate and set the Price per Gallon codingfield in listExtra.
            double dblSaleAmount = 0;
            double dblAmount = 0;
            double dblSalesTax = 0;
            double dblGallons = 0;
            double dblPricePerGallon = 0;

            for (ListItemCodingDataGroup item : listItems) {
                if (item.getListCodingdataRows() != null) {
                    for (BusHelperRmsCoding.CodingDataRow codingrow : item.getListCodingdataRows()) {
                        if (Crms.TOTAL_AMOUNT_OF_SALE_IN_USD.equals(codingrow.getCodingfieldName())) {
                            dblSaleAmount = MathUtils.getDoubleValue(codingrow.getValue());
                        } else if (Crms.NUMBER_OF_GALLONS_PURCHASED.equals(codingrow.getCodingfieldName())) {
                            dblGallons = MathUtils.getDoubleValue(codingrow.getValue());
                        } else if (Crms.SALES_TAX.equals(codingrow.getCodingfieldName())) {
                            dblSalesTax = MathUtils.getDoubleValue(codingrow.getValue());
                        } else if (Crms.TOTAL_AMOUNT.equals(codingrow.getCodingfieldName())) {
                            dblAmount = MathUtils.getDoubleValue(codingrow.getValue());
                        }
                    }
                }
            }

//            Nov 23, 2022  -   For now we are using new parameter amount instead of amount in usd
//            if (dblGallons != 0 && dblSaleAmount != 0) {
//                dblPricePerGallon = (dblSaleAmount - dblSalesTax) / dblGallons;
            if (dblGallons != 0 && dblAmount != 0) {
                dblPricePerGallon = (dblAmount - dblSalesTax) / dblGallons;

                for (ListItemCodingDataGroup item : listExtra) {
                    if (item.getListCodingdataRows() != null) {
                        for (BusHelperRmsCoding.CodingDataRow codingrow : item.getListCodingdataRows()) {
                            if (Crms.PRICE_PER_GALLON.equals(codingrow.getCodingfieldName())) {
                                codingrow.setValue(String.valueOf(dblPricePerGallon));
                            }
                        }
                    }
                }
            }

            return listExtra;
        }

    }

    // endregion: Nested Classes / Interfaces
}
