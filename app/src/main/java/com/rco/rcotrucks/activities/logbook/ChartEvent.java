package com.rco.rcotrucks.activities.logbook;

public class ChartEvent {

    private int startTimeInMinutes;
    private int endTimeInMinutes;
    private int periodInMinutes;
    private String eventCode;
    private String previousEventCode;

    public int getStartTimeInMinutes() {
        return startTimeInMinutes;
    }

    public void setStartTimeInMinutes(int startTimeInMinutes) {
        this.startTimeInMinutes = startTimeInMinutes;
    }

    public int getEndTimeInMinutes() {
        return endTimeInMinutes;
    }

    public void setEndTimeInMinutes(int endTimeInMinutes) {
        this.endTimeInMinutes = endTimeInMinutes;
    }

    public int getPeriodInMinutes() {
        return periodInMinutes;
    }

    public void setPeriodInMinutes(int periodInMinutes) {
        this.periodInMinutes = periodInMinutes;
    }

    public String getEventCode() {
        return eventCode;
    }

    public void setEventCode(String eventCode) {
        this.eventCode = eventCode;
    }

    public String getPreviousEventCode() {
        return previousEventCode;
    }

    public void setPreviousEventCode(String previousEventCode) {
        this.previousEventCode = previousEventCode;
    }
}
