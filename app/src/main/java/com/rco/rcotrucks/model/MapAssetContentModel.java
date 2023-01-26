package com.rco.rcotrucks.model;

public class MapAssetContentModel {

    int markerImageId;
    String address = "", category = "", chain = "", name = "", recordId = "", state = "", symbolName = "",
            symbolRecordId = "";
    double latitude, longitude;

    public MapAssetContentModel() {
    }

    public int getMarkerImageId() {
        return markerImageId;
    }

    public void setMarkerImageId(int markerImageId) {
        this.markerImageId = markerImageId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getChain() {
        return chain;
    }

    public void setChain(String chain) {
        this.chain = chain;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getSymbolName() {
        return symbolName;
    }

    public void setSymbolName(String symbolName) {
        this.symbolName = symbolName;
    }

    public String getSymbolRecordId() {
        return symbolRecordId;
    }

    public void setSymbolRecordId(String symbolRecordId) {
        this.symbolRecordId = symbolRecordId;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }
}
