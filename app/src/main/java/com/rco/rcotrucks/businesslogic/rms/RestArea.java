package com.rco.rcotrucks.businesslogic.rms;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.rco.rcotrucks.R;

public class RestArea {
    public String RecordId;
    public String MobileRecordId;
    public String RmsCodingTimestamp;
    public String RmsTimestamp;
    public String OrganizationName;
    public String OrganizationNumber;
    public String State;
    public String Road;
    public String Section;
    public String Name;
    public String Direction;
    public String Description;
    public String Restrooms;
    public String Water;
    public String PicnicArea;
    public String Phone;
    public String HandicapAccess;
    public String RVDump;
    public String FoodVending;
    public String PetArea;
    public String CigaretteAshDump;
    public String Latitude;
    public String Longitude;
    public String MileMarker1;
    public String MileMarker2;
    public String Category;
    public String Chain;
    public String Address;

    public LatLng getLatLng() {
        double lat = Double.parseDouble(Latitude);
        double lon = Double.parseDouble(Longitude);

        return new LatLng(lat, lon);
    }

    public BitmapDescriptor getIcon() {
        return BitmapDescriptorFactory.fromResource(R.drawable.ic_restarea);
    }

    public String getLabel() {
        return "Rest Area";
    }
}
