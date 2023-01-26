package com.rco.rcotrucks.activities.logbook.model;

import java.util.ArrayList;

public class GenericModel {

    int id, index, speed;
    double straightDistance;
    String date = "", certificationDetail = "", checkedDetail = "", latitude = "", longitude = "", previousLatitude,
            previousLongitude;
    ArrayList<GenericModel> list;

    public GenericModel() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getCertificationDetail() {
        return certificationDetail;
    }

    public void setCertificationDetail(String certificationDetail) {
        this.certificationDetail = certificationDetail;
    }

    public String getCheckedDetail() {
        return checkedDetail;
    }

    public void setCheckedDetail(String checkedDetail) {
        this.checkedDetail = checkedDetail;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public int getSpeed() {
        return speed;
    }

    public void setSpeed(int speed) {
        this.speed = speed;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public ArrayList<GenericModel> getList() {
        return list;
    }

    public void setList(ArrayList<GenericModel> list) {
        this.list = list;
    }

    public double getStraightDistance() {
        return straightDistance;
    }

    public void setStraightDistance(double straightDistance) {
        this.straightDistance = straightDistance;
    }

    public String getPreviousLatitude() {
        return previousLatitude;
    }

    public void setPreviousLatitude(String previousLatitude) {
        this.previousLatitude = previousLatitude;
    }

    public String getPreviousLongitude() {
        return previousLongitude;
    }

    public void setPreviousLongitude(String previousLongitude) {
        this.previousLongitude = previousLongitude;
    }
}
