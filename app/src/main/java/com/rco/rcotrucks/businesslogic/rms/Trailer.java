package com.rco.rcotrucks.businesslogic.rms;

public class Trailer {
    public String RecordId;
    public String MobileRecordId;
    public String FunctionalGroupName;
    public String Managers;
    public String FunctionalGroupObjectId;
    public String RMSTimestamp;
    public String RMSCodingTimestamp;
    public String UniqueKey1;
    public String OrganizationName;
    public String OrganizationNumber;
    public String VIN;
    public String TrailerNumber;
    public String Make;
    public String Model;
    public String Year;
    public String LastWorked;
    public String Country;
    public String StateRegion;
    public String City;
    public String Company;
    public String ProcessorId;
    public String ProcessorInstalled;
    public String TruckNumber;
    public String Latitude;
    public String Longitude;
    public String Active;
    public String Speed;
    public String Temperature1;
    public String Temperature2;
    public String Humidity;
    public String DoorStatus;
    public String Heading;
    public String GeneratorHours;
    public String Miles;
    public String FuelRate;
    public String Shock;
    public String TirePressures;
    public String ItemType;
    public String Treads;
    public String LastMaintenanceDate;
    public String LastAnnualInspectionDate;
    public String Status;
    public String CustomerName;
    public String CustomerNumber;
    public String IsReefer;
    public String ReeferHOS;
    public String RecordReeferHOS;
    public String ReportingPeriod;
    public String HOSViolation;
    public String HasCamera;
    public String HasTracker;

    @Override
    public String toString() {
        return "TrailerNumber: " + TrailerNumber + ", TruckNumber: " + TruckNumber;
    }
}
