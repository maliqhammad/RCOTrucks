package com.rco.rcotrucks.activities.ifta;


import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;

import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RmsRecTableRec;
import com.rco.rcotrucks.utils.JsonUtils;
import com.rco.rcotrucks.utils.StringUtils;

public class IftaEvent extends RmsRecTableRec {
    public static final String TAG = IftaEvent.class.getSimpleName();
    public static final String TABLE_NAME = "iftaevent";

    SQLiteStatement stmtUpdateDb;

    public String dateTime;
    public String jurisdictionId;
    public double odometer;
    public String truckNumber;
    public String dotNumber;
    public String firstName;
    public String lastName;
    public String employeeId;
    public String company;
    public String state;
    public String country;
    public String vehicleLicenseNumber;
    public String iftaYesOrNo;
    public String tripPermitYesOrNo;
    public double miles;
    public String fuelCode;
    public String fuelType;
    public String isIftaTaxExemptRoadYesNo;
    public String status;
    public double odometerStart;
    public double longitude;
    public double latitude;
//    public int sent;
//    public int syncErrorCount;

    public IftaEvent() {
        super(TABLE_NAME, Crms.R_IFTA_EVENT);
    }

//    public IftaEvent(String tablename, String recordType, String objectType) {
//        super(tablename, recordType, objectType);
//    }

//    private BusHelperIfta rulesIfta;
//    private DatabaseHelper db;

//    public IftaEvent() {
//    }

    public void initFromCursor(Cursor cur) {
        int col = 0;
//        "SELECT id, idRecordType, ObjectId, ObjectType, RecordId, rmsTimestamp, MobileRecordId, " +
//                "dateTime, jurisdictionId, odometer, truckNumber, " +
//                "dotNumber, firstName, lastName, employeeId, company, state, country, " +
//                "vehicleLicenseNumber, iftaYesorNo, tripPermitYesOrNo, miles, fuelCode, fuelType, " +
//                "status, odometerStart, sent, syncErrorCount, IftaTaxExemptRoadYesOrNo FROM iftaEvent WHERE status = '" + IFTA_EVENT_STATUS_ACTIVE + "'";

        setIdRecord(cur.getLong(col++));
        setIdRecordType(cur.getInt(col++));
        setObjectId(cur.getString(col++));
        setObjectType(cur.getString(col++));
        setRecordId(cur.getString(col++));
        setRmsTimestamp(cur.getLong(col++));
        setMobileRecordId(cur.getString(col++));
        dateTime = cur.getString(col++);
        jurisdictionId = cur.getString(col++);
        odometer = cur.getInt(col++);
        truckNumber = cur.getString(col++);
        dotNumber = cur.getString(col++);
        firstName = cur.getString(col++);
        lastName = cur.getString(col++);
        employeeId = cur.getString(col++);
        company = cur.getString(col++);
        state = cur.getString(col++);
        country = cur.getString(col++);
        vehicleLicenseNumber = cur.getString(col++);
        iftaYesOrNo = cur.getString(col++);
        tripPermitYesOrNo = cur.getString(col++);
        miles = cur.getDouble(col++);
        fuelCode = cur.getString(col++);
        fuelType = cur.getString(col++);
        isIftaTaxExemptRoadYesNo = cur.getString(col++);
        status = cur.getString(col++);
        odometerStart = cur.getInt(col++);
        longitude = cur.getDouble(col++);
        latitude = cur.getDouble(col++);
        setSentSyncStatus(cur.getInt(col++));
        setSyncErrorCount(cur.getInt(col++));
        setLocalSysTime(cur.getLong(col++));
    }

    @Override
    public void initFromJsonRecFiltX(JsonUtils.JsonRecFiltXWrapper jrec) {
        int col = 0;
        setObjectId(jrec.objectId);
        setObjectType(jrec.objectType);
        setRecordId(jrec.getCoding(Crms.RECORDID));
        setRmsTimestamp(Long.parseLong(jrec.getCoding(Crms.RMS_TIMESTAMP, "-1L")));
        setMobileRecordId(jrec.getCoding(Crms.MOBILERECORDID));
        dateTime = jrec.getCoding(Crms.DATETIME);
        jurisdictionId = jrec.getCoding(Crms.JURISDICTION_ID);
        odometer = Double.parseDouble(jrec.getCoding(Crms.ODOMETER, "0"));
        truckNumber = jrec.getCoding(Crms.TRUCK_NUMBER);
        dotNumber = jrec.getCoding(Crms.DOT_NUMBER);
        firstName = jrec.getCoding(Crms.FIRST_NAME);
        lastName = jrec.getCoding(Crms.LAST_NAME);
        employeeId = jrec.getCoding(Crms.EMPLOYEE_ID);
        company = jrec.getCoding(Crms.COMPANY);
        state = jrec.getCoding(Crms.STATE);
        country = jrec.getCoding(Crms.COUNTRY);
        vehicleLicenseNumber = jrec.getCoding(Crms.VEHICLE_LICENSE_NUMBER);
        iftaYesOrNo = jrec.getCoding(Crms.IFTA_YES_OR_NO);
        tripPermitYesOrNo = jrec.getCoding(Crms.TRIP_PERMIT_YES_OR_NO);
        isIftaTaxExemptRoadYesNo = jrec.getCoding(Crms.IFTA_TAX_EXEMPT_ROAD_YES_OR_NO);
        String strMiles = jrec.getCoding(Crms.MILES);
        miles = (strMiles != null ? Double.parseDouble(strMiles) : 0.0);
        fuelCode = jrec.getCoding(Crms.FUEL_CODE);
        fuelType = jrec.getCoding(Crms.FUEL_TYPE);
        status = jrec.getCoding(Crms.STATUS);
        odometerStart = Double.parseDouble(jrec.getCoding(Crms.ODOMETER_START, "0"));
        longitude = Double.parseDouble(jrec.getCoding(Crms.LONGITUDE, "0"));
        latitude = Double.parseDouble(jrec.getCoding(Crms.LATITUDE, "0"));
        // Todo: should we initialize status, error count, etc.?  Or done by logic of other routines.
        setSentSyncStatus(Cadp.SYNC_STATUS_SENT);
        setSyncErrorCount(0);
        // Todo: in general should call a validation method to set isValid.
        setIsValid(true);
        setLocalSysTime(Long.parseLong(jrec.getCoding(Crms.MOBILETIMESTAMP, "-1")));
    }

    private void clearIftaFields() {
        int col = 0;
        dateTime = null;
        jurisdictionId = null;
        odometer = -1;
        truckNumber = null;
        dotNumber = null;
        firstName = null;
        lastName = null;
        employeeId = null;
        company = null;
        state = null;
        country = null;
        vehicleLicenseNumber = null;
        iftaYesOrNo = null;
        tripPermitYesOrNo = null;
        miles = 0.0;
        fuelCode = null;
        fuelType = null;
        status = null;
        odometerStart = -1;
        // Todo: should we initialize status, error count, etc.?  Or done by logic of other routines.
//        setSentSyncStatus(Cadp.SYNC_STATUS_SENT);
//        setSyncErrorCount(0);
        // Todo: in general should call a validation method to set isValid.
//        setIsValid(true);
//        setLocalSysTime(-1L);
    }

    @Override
    public void clear() {
        super.clearCommon();
        clearIftaFields();
    }

    @Override
    public byte[] getEfileContent() {
        throw new RuntimeException(TAG + " getEfileContent() is not implemented for this class " + this.getClass().getName());
    }

    @Override
    public String toString() {
        return super.toString() + ", " + StringUtils.memberValuesToString(this);
    }
}
