package com.rco.rcotrucks.businesslogic.rms;

import java.util.ArrayList;

public class TruckLogHeader {
    public String Id;
    public String objectId;
    public String objectType;
    public String BarCode;
    public String CreationDate;
    public String CreationDatetime;
    public String CreationTime;
    public String RecordId;
    public String MobileRecordId;
    public String RMSTimestamp;
    public String RMSEfileTimestamp;
    public String RMSCodingTimestamp;
    public String FunctionalGroupName;
    public String FunctionalGroupObjectId;
    public String CreatorFirstName;
    public String CreatorLastName;
    public String CreatorRecordId;
    public String OrganizationName;
    public String OrganizationNumber;
    public String OffDutyHours;
    public String SleeperHours;
    public String DrivingHours;
    public String OnDutyHours;
    public String HomeOfficeRecordId;
    public String HomeOfficeName;
    public String HomeOfficePhone;
    public String FirstName;
    public String LastName;
    public String UserRecordId;
    public String Year;
    public String VehicleLicenseNumber;
    public String Driver;
    public String DriverRecordId;
    public String CoDriver;
    public String CoDriverRecordId;
    public String Rule;
    public String RuleDrivingDate;
    public String Active;
    public String TotalDistance;
    public String SpeedViolations;
    public String GeofenceViolations;
    public String StartDate;
    public String StartTime;
    public String EndDate;
    public String EndTime;
    public String ItemType;
    public String TripName;
    public String OverdueTimeLimit;
    public String RouteHeaderRecordId;
    public String HoursRemaining;
    public String Weight;
    public String Lot;
    public String TruckNumber;
    public String Trailer1Number;
    public String Trailer2Number;
    public String DriverStatus;
    public String PreviousTractors;
    public boolean Sent;

    private ArrayList<TruckLogDetail> truckLogDetails;

    public ArrayList<TruckLogDetail> getTruckLogDetails() {
        return truckLogDetails;
    }

    public void addDetail(TruckLogDetail d) {
        if (truckLogDetails == null)
            truckLogDetails = new ArrayList();

        truckLogDetails.add(d);
    }
}
