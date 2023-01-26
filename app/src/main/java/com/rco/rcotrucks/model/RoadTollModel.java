package com.rco.rcotrucks.model;

public class RoadTollModel {

//    	"Cash Tolls": "$1.50 bridge only",
//                "Latitude": "33.1813775",
//                "Length": "4.9",
//                "Longitude": "-87.6132381",
//                "Northern or Eastern Terminus": "US 82 - Northport",
//                "Notes": "Cash or Freedom Pass",
//                "RMS Timestamp": "1669062876127",
//                "RecordId": "2070836",
//                "Road Name": "Joe Mallisham Parkway",
//                "Southern or Western Terminus": "I-20/I-59 - Tuscaloosa",
//                "State": "AL",
//                "Vendor Name": "Tuscaloosa By-Pass"

    String cashTolls = "", latitude = "", length = "", longitude = "", northernOrEasternTerminus = "",
            notes = "", rmsTimestamp = "", recordId = "", roadName = "", southernOrWesternTerminus = "",
            state = "", vendorName = "";

    public RoadTollModel() {
    }

    public String getCashTolls() {
        return cashTolls;
    }

    public void setCashTolls(String cashTolls) {
        this.cashTolls = cashTolls;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLength() {
        return length;
    }

    public void setLength(String length) {
        this.length = length;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }


    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getRmsTimestamp() {
        return rmsTimestamp;
    }

    public void setRmsTimestamp(String rmsTimestamp) {
        this.rmsTimestamp = rmsTimestamp;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getRoadName() {
        return roadName;
    }

    public void setRoadName(String roadName) {
        this.roadName = roadName;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getNorthernOrEasternTerminus() {
        return northernOrEasternTerminus;
    }

    public void setNorthernOrEasternTerminus(String northernOrEasternTerminus) {
        this.northernOrEasternTerminus = northernOrEasternTerminus;
    }

    public String getSouthernOrWesternTerminus() {
        return southernOrWesternTerminus;
    }

    public void setSouthernOrWesternTerminus(String southernOrWesternTerminus) {
        this.southernOrWesternTerminus = southernOrWesternTerminus;
    }
}
