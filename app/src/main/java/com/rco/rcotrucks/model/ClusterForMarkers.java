package com.rco.rcotrucks.model;

import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterItem;
import com.rco.rcotrucks.R;

public class ClusterForMarkers implements ClusterItem {
    public final LatLng position;
    public final BitmapDescriptor markerIcon;
    public final String title;

    public ClusterForMarkers(LatLng position, BitmapDescriptor markerIcon, String title) {
        this.position = position;
        this.markerIcon = markerIcon;
        this.title = title;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public String getSnippet() {
        return null;
    }
}
