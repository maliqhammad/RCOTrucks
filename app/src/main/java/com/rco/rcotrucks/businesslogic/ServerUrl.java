package com.rco.rcotrucks.businesslogic;

public class ServerUrl {
    private String url;
    private boolean selected;

    public ServerUrl(String url, boolean selected) {
        this.url = url;
        this.selected = selected;
    }

    public String getUrl() {
        return url;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean value) {
        this.selected = value;
    }
}
