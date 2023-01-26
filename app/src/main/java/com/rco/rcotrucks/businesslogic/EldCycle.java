package com.rco.rcotrucks.businesslogic;

import java.util.ArrayList;

public class EldCycle {
    public String Id;
    public String Operation;
    public String Flag;
    public String objectId;
    public String ObjectType;
    public String MobileRecordId;
    public String FunctionalGroupName;
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
    public String RuleDrivingDays;
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

    private ArrayList<EldEvent> eldEvents;

    public int countEldEvents() {
        if (eldEvents == null)
            return 0;

        return eldEvents.size();
    }

    public ArrayList<EldEvent> getEldEvents() {
        return eldEvents;
    }

    public EldEvent getFirstEldEvent() {
        if (eldEvents == null || eldEvents.size() == 0)
            return null;

        return eldEvents.get(0);
    }

    public void addEldEvent(EldEvent e) {
        if (eldEvents == null)
            eldEvents = new ArrayList();

        eldEvents.add(e);
    }

    @Override
    public String toString() {
        EldEvent eldEvent = getFirstEldEvent();
        String eldEventStr = eldEvent != null ? eldEvent.toString() : "";
        String eldEventsCount = eldEvents != null ? eldEvents.size() + "" : "0";

        return "Id: " + Id + ", StartDate: " + StartDate + ", Active: " + Active + ", EldEvents: " + eldEventsCount + ", First event: " + eldEventStr;
    }
}
