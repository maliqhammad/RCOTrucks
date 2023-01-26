package com.rco.rcotrucks.businesslogic.rms;

import java.util.ArrayList;

public class TruckEldHeader {
    public String Id;
    public String objectId;
    public String objectType;
    public String RecordId;
    public String MobileRecordId;
    public String BarCode;
    public String CreationDatetime;
    public String CreationDate;
    public String CreationTime;
    public String MasterBarcode;
    public String RmsTimestamp;
    public String RmsCodingTimestamp;
    public String FunctionalGroupName;
    public String FunctionalGroupObjectId;
    public String CreatorFirstName;
    public String CreatorLastName;
    public String CreatorRecordId;
    public String OrganizationName;
    public String OrganizationNumber;
    public String VehicleLicenseNumber;
    public String CycleStartDateTime;
    public String Rule;
    public String DriverName;
    public String DriverRecordId;
    public String DriverId;
    public String CoDriverName;
    public String CoDriverRecordId;
    public String CoDriverId;
    public String TruckLogHeaderRecordId;
    public String TruckNumber;
    public String Trailer1Number;
    public String Trailer2Number;
    public boolean Sent;

    private ArrayList<TruckEldDetail> details;

    public ArrayList<TruckEldDetail> getTruckEldDetails() {
        return details;
    }

    public void addDetail(TruckEldDetail d) {
        if (details == null)
            details = new ArrayList();

        details.add(d);
    }
}
