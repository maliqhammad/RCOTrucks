package com.rco.rcotrucks.model;

public class MapAssetSubcategoriesModel {

//    		"Name": "7 Eleven",
//                    "ImageName": "Map_7_eleven",
//                    "POIImageName": "POI_Map_7_eleven"

    int mapAssetId=0, markerId;
    long id;
    String subCategoryName = "", subCategoryImageName = "", subCategoryPOIImageName = "", parentType="";
    boolean isSelected = false;

    public MapAssetSubcategoriesModel() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getMapAssetId() {
        return mapAssetId;
    }

    public void setMapAssetId(int mapAssetId) {
        this.mapAssetId = mapAssetId;
    }

    public String getSubCategoryName() {
        return subCategoryName;
    }

    public void setSubCategoryName(String subCategoryName) {
        this.subCategoryName = subCategoryName;
    }

    public String getSubCategoryImageName() {
        return subCategoryImageName;
    }

    public void setSubCategoryImageName(String subCategoryImageName) {
        this.subCategoryImageName = subCategoryImageName;
    }

    public String getSubCategoryPOIImageName() {
        return subCategoryPOIImageName;
    }

    public void setSubCategoryPOIImageName(String subCategoryPOIImageName) {
        this.subCategoryPOIImageName = subCategoryPOIImageName;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public int getMarkerId() {
        return markerId;
    }

    public void setMarkerId(int markerId) {
        this.markerId = markerId;
    }

    public String getParentType() {
        return parentType;
    }

    public void setParentType(String parentType) {
        this.parentType = parentType;
    }
}
