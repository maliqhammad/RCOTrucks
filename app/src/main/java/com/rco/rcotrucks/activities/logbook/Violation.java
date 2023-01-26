package com.rco.rcotrucks.activities.logbook;

public class Violation {
    private String violationID;
    private String violationDesc;

    public Violation(String violationID, String violationDesc) {
        this.violationID = violationID;
        this.violationDesc = violationDesc;
    }

    public String getViolationID() {
        return violationID;
    }

    public void setViolationID(String violationID) {
        this.violationID = violationID;
    }

    public String getViolationDesc() {
        return violationDesc;
    }

    public void setViolationDesc(String violationDesc) {
        this.violationDesc = violationDesc;
    }
}
