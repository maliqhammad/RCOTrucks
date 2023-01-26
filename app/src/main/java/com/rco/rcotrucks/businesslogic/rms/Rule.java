package com.rco.rcotrucks.businesslogic.rms;

public class Rule {
    public int Id;
    public String objectId;
    public String objectType;
    public String RecordId;
    public String Name;
    public String Hours;
    public String Days;
    public String HoursPerDay;
    public String ItemType;

    @Override
    public String toString() {
        return "Name: " + Name + ", Hours: " + Hours + ", Days: " + Days + ", HoursPerDay: " + HoursPerDay + ", ItemType: " + ItemType;
    }
}
