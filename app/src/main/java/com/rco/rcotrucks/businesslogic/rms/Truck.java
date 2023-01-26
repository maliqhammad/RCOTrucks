package com.rco.rcotrucks.businesslogic.rms;

import com.rco.rcotrucks.utils.StringUtils;

public class Truck {
    public String RecordId;
    public String MobileRecordId;
    public String RmsCodingTimestamp;
    public String RmsTimestamp;
    public String OrganizationName;
    public String OrganizationNumber;
    public String ContainerTypeRecordId;
    public String UniqueKey1;
    public String Deployment;
    public String IsaLocation;
    public String CustomerRecordId;
    public String ManufacturerSerialNumber;
    public String Overdue;
    public String StoreName;
    public String StoreNumber;
    public String StoreType;
    public String DateLoaded;
    public String DateUnloaded;
    public String TimeLoaded;
    public String TimeUnloaded;
    public String FunctionalGroupName;
    public String Managers;
    public String FunctionalGroupObjectId;
    public String DateDoorClosed;
    public String TimeDoorClosed;
    public String DateTime;
    public String AlertCount;
    public String VendorName;
    public String Tracking;
    public String ItemType;
    public String Humidity;
    public String GPSDailySampleRate;
    public String GPSTripSampleRate;
    public String TruckGPSServices;
    public String FromLatitude;
    public String FromLongitude;
    public String ToLatitude;
    public String ToLongitude;
    public String DepartureDate;
    public String DepartureTime;
    public String EstimatedTimeEnroute;
    public String EstimatedTimeOfArrival;
    public String ActualTimeEnroute;
    public String ActualTimeOfArrival;
    public String TravelDangerAlertTime;
    public String TravelWarningAlertTime;
    public String OverdueTimeLimit;
    public String SalesOrderNumber;
    public String FromAddress;
    public String FromCity;
    public String FromState;
    public String FromZipcode;
    public String ToAddress;
    public String ToCity;
    public String ToState;
    public String ToZipcode;
    public String VIN;
    public String VehicleLicenseNumber;
    public String Make;
    public String Mack;
    public String Model;
    public String Year;
    public String LastWorked;
    public String Country;
    public String StateRegion;
    public String City;
    public String Company;
    public String FirstName;
    public String LastName;
    public String MobilePhone;
    public String Latitude;
    public String Longitude;
    public String Active;
    public String Speed;
    public String Temperature1;
    public String Temperature2;
    public String DoorStatus;
    public String Heading;
    public String TruckNumber;
    public String Odometer;
    public String EngineIdle;
    public String RPM;
    public String EngineHours;
    public String OdometerSetup;
    public String Shock;
    public String TirePressures;
    public String EngineStatus;
    public String HOSViolation;
    public String SpeedViolations;
    public String SmogViolation;
    public String LastMaintenanceDate;
    public String LastAnnualInspectionDate;
    public String Status;
    public String CustomerName;
    public String CustomerNumber;
    public String FuelCode;
    public String FuelType;
    public String Diesel;
    public String DOTNumber;
    public String DOTExpirationDate;
    public String IFTADecal;
    public String IFTAFuelPermits;
    public String IFTATripPermits;
    public String ReportingPeriod;

    public Integer getReportingPeriod() {
        if (StringUtils.isNullOrWhitespaces(ReportingPeriod))
            return null;

        return new Integer(ReportingPeriod);
    }

    @Override
    public String toString() {
        return "TruckNumber: " + TruckNumber;
    }
}
