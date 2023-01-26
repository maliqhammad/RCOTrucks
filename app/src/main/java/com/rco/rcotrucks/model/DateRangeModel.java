package com.rco.rcotrucks.model;

public class DateRangeModel {

    boolean isSelected=false;
    String date;

    public DateRangeModel() {
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
