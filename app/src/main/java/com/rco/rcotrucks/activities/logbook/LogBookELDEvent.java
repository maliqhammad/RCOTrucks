package com.rco.rcotrucks.activities.logbook;

public class LogBookELDEvent{

    private int id;
    private String time;
    private String location;
    private String odometer;
    private String engHours;
    private String eventTypeDescription;
    private String origin;
    private String eventType;
    private String eventCode;
    private String annotation;
    private String shiftstart;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getOdometer() {
        return odometer;
    }

    public void setOdometer(String odometer) {
        this.odometer = odometer;
    }

    public String getEngHours() {
        return engHours;
    }

    public void setEngHours(String engHours) {
        this.engHours = engHours;
    }

    public String getEventTypeDescription() {
        return eventTypeDescription;
    }

    public void setEventTypeDescription(String eventTypeDescription) {
        this.eventTypeDescription = eventTypeDescription;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getAnnotation() {
        return annotation;
    }

    public void setAnnotation(String annotation) {
        this.annotation = annotation;
    }

    public String getShiftstart() {
        return shiftstart;
    }

    public void setShiftstart(String shiftstart) {
        this.shiftstart = shiftstart;
    }
}