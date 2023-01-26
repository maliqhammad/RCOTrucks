package com.rco.rcotrucks.businesslogic;

import com.google.android.gms.maps.model.LatLng;

public class City {
    public String City;
    public String State;
    public Double Lat;
    public Double Lon;

    public City(String city, String state, Double lat, Double lon) {
        City = city;
        State = state;
        Lat = lat;
        Lon = lon;
    }

    public LatLng getLatLng() {
        return new LatLng(Lat, Lon);
    }
}
