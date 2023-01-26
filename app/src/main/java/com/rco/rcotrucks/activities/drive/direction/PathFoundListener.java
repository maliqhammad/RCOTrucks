package com.rco.rcotrucks.activities.drive.direction;

import com.google.android.gms.maps.model.LatLng;

public interface PathFoundListener {
  void onPathsFound(LatLng endLocation, String photo);
}