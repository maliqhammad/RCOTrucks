package com.rco.rcotrucks.businesslogic;

public class DutyEvent {
    private String name;
    private boolean selected;

    public DutyEvent(String name, boolean selected) {
        this.name = name;
        this.selected = selected;
    }

    public String getName() {
        return name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean value) {
        this.selected = value;
    }
}
