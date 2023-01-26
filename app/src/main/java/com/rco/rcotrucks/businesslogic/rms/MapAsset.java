package com.rco.rcotrucks.businesslogic.rms;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.rco.rcotrucks.R;

public class MapAsset {
    public String RecordId;
    public String MobileRecordId;
    public String RmsCodingTimestamp;
    public String RmsTimestamp;
    public String DateTime;
    public String SystemTimeMillis;
    public String Type;
    public String Name;
    public String Number;
    public String Location;
    public String Latitude;
    public String Longitude;
    public String Elevation;
    public String Heading;
    public String Velocity;
    public String Acceleration;
    public String Pitch;
    public String Roll;
    public String Yaw;
    public String Area;
    public String Volume;
    public String Weight;
    public String Temperature;
    public String AssetOwner;
    public String Status;
    public String Category;
    public String Unit;
    public String Hours;
    public String EstimatedTimeEnroute;
    public String EstimatedTimeOfArrival;
    public String SymbolName;
    public String SymbolRecordId;
    public String Chain;
    public String Address;
    public String State;



    public LatLng getLatLng() {
        double lat = Double.parseDouble(Latitude);
        double lon = Double.parseDouble(Longitude);

        return new LatLng(lat, lon);
    }

    public BitmapDescriptor getIcon() {
        if (AssetOwner != null && AssetOwner.toLowerCase().indexOf("ta") != -1)
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_ta);

        if (AssetOwner != null && AssetOwner.toLowerCase().indexOf("pilot") != -1)
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_pilot);

        if (AssetOwner != null && AssetOwner.toLowerCase().indexOf("loves") != -1)
            return BitmapDescriptorFactory.fromResource(R.drawable.ic_love_pol);

        return BitmapDescriptorFactory.fromResource(R.drawable.ic_myloc4);
    }

    public String getLabel() {
        if (AssetOwner != null && AssetOwner.toLowerCase().indexOf("ta") != -1)
            return "TA";

        if (AssetOwner != null && AssetOwner.toLowerCase().indexOf("pilot") != -1)
            return "Pilot";

        return "";
    }
}
