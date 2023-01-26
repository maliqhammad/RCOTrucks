package com.rco.rcotrucks.model;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

public class MapAssetModel {

//    	"ImageName": "Map_Fuel",
//                "POIImageName": "POI_Map_Fuel",
//                "Value": "fuel",
//                "Name": "Truck Wash"
//    	"Subcategories": []

    int mapAssetId;
    private String name="", value="", imageName="",poiImageName="";
    ArrayList<MapAssetSubcategoriesModel> subcategory;
    boolean isAssetSelected = false, selectAllSelected = false, deselectAllSelected = false, isSwitchClicked=false;

    public MapAssetModel() {
    }

    public int getMapAssetId() {
        return mapAssetId;
    }

    public void setMapAssetId(int mapAssetId) {
        this.mapAssetId = mapAssetId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getPoiImageName() {
        return poiImageName;
    }

    public void setPoiImageName(String poiImageName) {
        this.poiImageName = poiImageName;
    }

    public ArrayList<MapAssetSubcategoriesModel> getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(ArrayList<MapAssetSubcategoriesModel> subcategory) {
        this.subcategory = subcategory;
    }

    public boolean isAssetSelected() {
        return isAssetSelected;
    }

    public void setAssetSelected(boolean assetSelected) {
        isAssetSelected = assetSelected;
    }

    public boolean isSelectAllSelected() {
        return selectAllSelected;
    }

    public void setSelectAllSelected(boolean selectAllSelected) {
        this.selectAllSelected = selectAllSelected;
    }

    public boolean isDeselectAllSelected() {
        return deselectAllSelected;
    }

    public void setDeselectAllSelected(boolean deselectAllSelected) {
        this.deselectAllSelected = deselectAllSelected;
    }

    public boolean isSwitchClicked() {
        return isSwitchClicked;
    }

    public void setSwitchClicked(boolean switchClicked) {
        isSwitchClicked = switchClicked;
    }
}
