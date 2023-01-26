package com.rco.rcotrucks.activities.drive;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.maps.android.PolyUtil;
import com.google.maps.android.SphericalUtil;
import com.google.maps.android.clustering.ClusterManager;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.drive.direction.NavigationUtils;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.model.MapAssetContentModel;
import com.rco.rcotrucks.model.ClusterForMarkers;
import com.rco.rcotrucks.utils.MarkerClusterRenderer;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;
import com.rco.rcotrucks.utils.route.Route;
import com.rco.rcotrucks.utils.route.Segment;

import java.util.ArrayList;
import java.util.List;

public class MapFragment extends SupportMapFragment {

    private static final String TAG = MapFragment.class.getSimpleName();
    private BusinessRules rules = BusinessRules.instance();
    private boolean isMapInit = true, isNight = false;
    private Activity activity;
    private boolean isPointsDisplayed = true, isDestinationInstructionAlreadyGiven = false;
    private List<Marker> segmentMarkerList = new ArrayList<Marker>();
    private List<Marker> routePointsMarkerList = new ArrayList<Marker>();
    private LatLng myLocation, initialMapRouteLatLng;
    private int lastSavedIndex = -1;
    private int lastSavedIndexForBearing = 0;
    private float NAVIGATION_ZOOM_LEVEL = 15.0F;
    private Marker mPositionMarker;
    private String destinationAddress = "";
    private Route displayedRoute;
    private GoogleMap googleMap;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach: ");
        this.activity = activity;
    }

    public void onMapReady(GoogleMap map) {
        Log.d(TAG, "onMapReady: ");
//        this.map = map;
        this.googleMap = map;

        if (map != null) {
            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            map.setMyLocationEnabled(true);
            enableZoomControls(true);
            map.getUiSettings().setMyLocationButtonEnabled(false);
            map.getUiSettings().setMapToolbarEnabled(true);
            setOnCameraMoveListener();

            if (isMapInit())
                map.setOnMyLocationChangeListener(myLocationChangeListener);
            else {
                rules.logDebug("Re-creating map fragment");
            }
        }


    }

    int previousZoomValue = 0;

    void setOnCameraMoveListener() {

        googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
            @Override
            public void onCameraMove() {

                CameraPosition cameraPosition = googleMap.getCameraPosition();
                int currentZoomValue = (int) Math.round(cameraPosition.zoom);
//                Log.d(TAG, "IC: onCameraMove: currentZoomValue: "+currentZoomValue);
                if (previousZoomValue == 0) {
                    previousZoomValue = currentZoomValue;
                    Log.d(TAG, "onCameraMove: cameraPosition: zoom: 0: " + cameraPosition.zoom);
                } else {
                    if (previousZoomValue != currentZoomValue) {
                        previousZoomValue = currentZoomValue;
                        Log.d(TAG, "onCameraMove: cameraPosition: zoom: more: " + currentZoomValue
                                + " location: coordinates: " + cameraPosition.target + " downSpeed: " + UiUtils.getNetworkSpeed(false, getContext())
                                + " upSpeed: " + UiUtils.getNetworkSpeed(true, getContext()));
//                        MainMenuActivity.logDataArrayList.add("setOnCameraMoveListener: zoom Level: " + currentZoomValue +
//                                " location: coordinates: " + cameraPosition.target + " downSpeed: " + UiUtils.getNetworkSpeed(false, getContext())
//                                + " upSpeed: " + UiUtils.getNetworkSpeed(true, getContext()));
                    }
                }
//                if(cameraPosition.zoom > 18.0) {
//                    googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
//                } else {
//                    googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
//                }
            }
        });
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public LatLng getMyLastLocation() {
        Log.d(TAG, "getMyLastLocation: ");
        return myLocation;
    }

    public void setMyLastLocation(LatLng myLocation) {
//        Log.d(TAG, "setMyLastLocation: ");
        this.myLocation = myLocation;
    }

    private boolean isFirstMapLoad = true;
    private LoadDataOnLocationChange loadDataOnLocationChange;
    private UpdateLocation updateLocation;

    public void setOnLocationUpdatedEvent(UpdateLocation updateLocation) {
        Log.d(TAG, "setOnLocationUpdatedEvent: ");
        this.updateLocation = updateLocation;
    }


    private GoogleMap.OnMyLocationChangeListener myLocationChangeListener = new GoogleMap.OnMyLocationChangeListener() {
        @Override
        public void onMyLocationChange(Location location) {
            try {
//                Log.d(TAG, "onMyLocationChange: updateLocation: " + updateLocation);
                if (updateLocation != null) {
                    if (location != null && location.hasSpeed()) {
                        Log.d(TAG, "onMyLocationChange: currentSpeed:");
                        updateLocation.setCurrentSpeed(location.getSpeed());
                        Log.d(TAG, "onMyLocationChange: currentSpeed: " + location.getSpeed());
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Log.d(TAG, "onMyLocationChange: currentSpeed: meterPerSecond" + location.getSpeedAccuracyMetersPerSecond());
                        }
                    }
                }

                if (loadDataOnLocationChange != null)
                    loadDataOnLocationChange.OnLocationChange(location, googleMap);
//                    loadDataOnLocationChange.OnLocationChange(location, map);

                if (!isFirstMapLoad)
                    return;

                LatLng loc = new LatLng(location.getLatitude(), location.getLongitude());

                if (loc == null && rules.isDebugMode())
                    loc = new LatLng(37.2970523, -121.9574969); // It's coordinates are used for emulator purposes

                myLocation = loc;


//                if (map != null) {
//                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 10));
                if (googleMap != null) {
//                    Sep 19, 2022  -   Comment out this animate camera because it was overriding another call to animate camera as per mobile state when offline or online
//                    googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(loc, 10));
                    if (updateLocation != null)
                        updateLocation.OnLocationUpdated(loc);

                    setMapNightMode(rules.isItNight(myLocation));
                    //setMapNightMode(rules.isItNight());
                }

                isFirstMapLoad = false;
            } catch (Throwable throwable) {
                Log.d(TAG, "onMyLocationChange: throwable: " + throwable.getMessage());
                throwable.printStackTrace();
            }
        }
    };

    public boolean isMapInit() {
        Log.d(TAG, "isMapInit: ");
        return isMapInit;
    }

    public void setMapNightMode(boolean value) {
        Log.d(TAG, "setMapNightMode: ");
//        map.setMapStyle(value ? MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_in_night) : MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_in_day));
        googleMap.setMapStyle(value ? MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_in_night) : MapStyleOptions.loadRawResourceStyle(activity, R.raw.map_in_day));
//        July 01, 2022 -   We should disable showing the buildings
        googleMap.setBuildingsEnabled(false);
    }


    public void setSegmentAndPinPointsOnSelectedRoute() {
        Log.d(TAG, "rerouting: setSegmentAndPinPointsOnSelectedRoute: isPointsDisplayed: " + isPointsDisplayed);

        Log.d(TAG, "routePoints: size: points: " + displayedRoute.getPoints().size());
        Log.d(TAG, "routePoints: size: segments: " + displayedRoute.getSegments().size());
        if (isPointsDisplayed) {

            addGreenPinMarkersForSegmentEndPoints();
//            addOrangeMarkerForSelectedRoutePoints();
            isPointsDisplayed = false;
        }
    }

    public void addGreenPinMarkersForSegmentEndPoints() {
        Log.d(TAG, "rerouting: addGreenPinMarkersForSegmentEndPoints: ");
        Drawable locationPinDrawable = getResources().getDrawable(R.drawable.ic_baseline_push_pin_24);
        BitmapDescriptor locationPinMarkerIcon = getMarkerIconFromDrawable(locationPinDrawable);
        for (int i = 0; i < displayedRoute.getSegments().size(); i++) {
            Log.d(TAG, "SegmentPoints: position: " + i + " and startPoints: " + displayedRoute.getSegments().get(i).startPoint() + " and endPoints: " + displayedRoute.getSegments().get(i).endPoint());

            MarkerOptions options = new MarkerOptions();
            options.position(displayedRoute.getSegments().get(i).endPoint());
            options.icon(locationPinMarkerIcon);
            options.title(destinationAddress);
            Marker marker = googleMap.addMarker(options);
            segmentMarkerList.add(marker);
        }
    }

    public void addOrangeMarkerForSelectedRoutePoints() {
        Log.d(TAG, "rerouting: addOrangeMarkerForSelectedRoutePoints: ");

        Drawable circleDrawable = getResources().getDrawable(R.drawable.circle_orange);
        BitmapDescriptor markerIcon = getMarkerIconFromDrawable(circleDrawable);

        for (int i = 0; i < displayedRoute.getPoints().size(); i++) {

//            Log.d(TAG, "addOrangeMarkerForSelectedRoutePoints: position: " + i + " and points: " + displayedRoute.getPoints().get(i));
            MarkerOptions options = new MarkerOptions();
            options.position(displayedRoute.getPoints().get(i));
            options.icon(markerIcon);
            options.title(destinationAddress);
            Marker marker = googleMap.addMarker(options);
            routePointsMarkerList.add(marker);
        }
    }

    public void addBlueMarkerForMyVisitedPosition(LatLng loc) {
        Log.d(TAG, "findNearestPointFromRoute: marker: addBlueMarkerForMyVisitedPosition: loc: " + loc);

        if (loc != null && getActivity() != null) {
            Drawable locationPinDrawable = getActivity().getResources().getDrawable(R.drawable.circle_blue);
            BitmapDescriptor locationPinMarkerIcon = getMarkerIconFromDrawable(locationPinDrawable);

            MarkerOptions options = new MarkerOptions();
            options.position(loc);
            options.icon(locationPinMarkerIcon);
            options.title("");
            googleMap.addMarker(options);
        }
    }

    int counter = 0;

    public void addSelectedAssetsMarkers(MapAssetContentModel mapAssetContentModel, Drawable drawable) {
        Log.d(TAG, "addSelectedAssetsMarkers: counter: " + counter++);
        BitmapDescriptor locationPinMarkerIcon = getMarkerIconFromDrawable(drawable);

        MarkerOptions options = new MarkerOptions();
        options.position(new LatLng(mapAssetContentModel.getLatitude(), mapAssetContentModel.getLongitude()));
        options.icon(locationPinMarkerIcon);
        options.title("");
        googleMap.addMarker(options);
    }

    public void addSelectedAssetsMarkers(List<MapAssetContentModel> mapAssetContentList, Drawable drawable) {

        for (int i = 0; i < mapAssetContentList.size(); i++) {
//            Drawable locationPinDrawable = getActivity().getResources().getDrawable(R.drawable.map_asset_marker_fuel_loves);
//            Drawable locationPinDrawable = getActivity().getResources().getDrawable(drawable);
            BitmapDescriptor locationPinMarkerIcon = getMarkerIconFromDrawable(drawable);

            MarkerOptions options = new MarkerOptions();
            options.position(new LatLng(mapAssetContentList.get(i).getLatitude(), mapAssetContentList.get(i).getLongitude()));
            options.icon(locationPinMarkerIcon);
            options.title("");
            googleMap.addMarker(options);
        }
    }

    public void addSelectedAssetsMarkers(List<MapAssetContentModel> mapAssetContentList) {
        Log.d(TAG, "saveMapAsset: addSelectedAssetsMarkers: mapAssetContentList: size: " + mapAssetContentList.size());
        for (int i = 0; i < mapAssetContentList.size(); i++) {
//            Log.d(TAG, "saveMapAsset: addSelectedAssetsMarkers: index: " + i + " called to add marker: " + mapAssetContentList.get(i).getLatitude() + "," + mapAssetContentList.get(i).getLongitude() + " " + mapAssetContentList.get(i).getSymbolName());
            BitmapDescriptor locationPinMarkerIcon = null;

            try {
                locationPinMarkerIcon = getMarkerIconFromDrawable(getResources().getDrawable(mapAssetContentList.get(i).getMarkerImageId()));
                MarkerOptions options = new MarkerOptions();
                options.position(new LatLng(mapAssetContentList.get(i).getLatitude(), mapAssetContentList.get(i).getLongitude()));
                options.icon(locationPinMarkerIcon);
                options.title("");
                googleMap.addMarker(options);
            } catch (Exception exception) {
                Log.d(TAG, "saveMapAsset: addSelectedAssetsMarkers: exception: " + exception.getMessage());
            }

        }
    }

    public void addSelectedAssetsMarkersUpdate(List<MapAssetContentModel> mapAssetContentList) {
        Log.d(TAG, "saveMapAsset: addSelectedAssetsMarkers: mapAssetContentList: size: " + mapAssetContentList.size());

        ClusterManager<ClusterForMarkers> clusterManager = new ClusterManager<ClusterForMarkers>(getContext(), googleMap);
        MarkerClusterRenderer markerClusterRenderer = new MarkerClusterRenderer(getActivity(), googleMap, clusterManager);
        markerClusterRenderer.setMinClusterSize(1);
        clusterManager.setRenderer(markerClusterRenderer);

        googleMap.setOnCameraIdleListener(clusterManager);
        googleMap.setOnMarkerClickListener(clusterManager);
        clusterManager.setAnimation(false);
        clusterManager.clearItems();


        removePreviousMarkers();
        for (int i = 0; i < mapAssetContentList.size(); i++) {
            Log.d(TAG, "saveMapAsset: addSelectedAssetsMarkers: index: " + i + " called to add marker: " + mapAssetContentList.get(i).getLatitude() + "," + mapAssetContentList.get(i).getLongitude() + " " + mapAssetContentList.get(i).getSymbolName());
            BitmapDescriptor locationPinMarkerIcon = null;

            try {
                locationPinMarkerIcon = getMarkerIconFromDrawable(getResources().getDrawable(mapAssetContentList.get(i).getMarkerImageId()));
                LatLng markerPositionLatLng = new LatLng(mapAssetContentList.get(i).getLatitude(), mapAssetContentList.get(i).getLongitude());
                String markerPositionTitle = mapAssetContentList.get(i).getName();

                clusterManager.addItem(new ClusterForMarkers(markerPositionLatLng, locationPinMarkerIcon, markerPositionTitle));
            } catch (Exception exception) {
                Log.d(TAG, "saveMapAsset: addSelectedAssetsMarkers: exception: " + exception.getMessage());
            }
        }
    }

    void removePreviousMarkers() {

    }

    public void addWhiteMarkerForMyVisitedPosition(LatLng loc) {
        Log.d(TAG, "addWhiteMarkerForMyVisitedPosition: ");
//        Toast.makeText(activity, "addBlueMarker:W " + loc.latitude + ", " + loc.longitude, Toast.LENGTH_SHORT).show();
        Drawable locationPinDrawable = getResources().getDrawable(R.drawable.circle_white);
//        Drawable locationPinDrawable = getResources().getDrawable(R.drawable.circle_blue);
        BitmapDescriptor locationPinMarkerIcon = getMarkerIconFromDrawable(locationPinDrawable);

        MarkerOptions options = new MarkerOptions();
        options.position(loc);
        options.icon(locationPinMarkerIcon);
        options.title("");
        googleMap.addMarker(options);

//        adjustCamera(loc);
    }

    public MarkerOptions returnMarkerOption(LatLng loc) {
        Log.d(TAG, "addWhiteMarkerForMyVisitedPosition: ");
//        Toast.makeText(activity, "addBlueMarker:W " + loc.latitude + ", " + loc.longitude, Toast.LENGTH_SHORT).show();
        Drawable locationPinDrawable = getResources().getDrawable(R.drawable.circle_black);
//        Drawable locationPinDrawable = getResources().getDrawable(R.drawable.circle_blue);
        BitmapDescriptor locationPinMarkerIcon = getMarkerIconFromDrawable(locationPinDrawable);

        MarkerOptions options = new MarkerOptions();
        options.position(loc);
        options.icon(locationPinMarkerIcon);
        options.title("");
//        googleMap.addMarker(options);

//        adjustCamera(loc);

        return options;
    }

    void adjustCamera(LatLng loc) {
        Log.d(TAG, "DriveFragmentBase: IC: adjustCamera: loc: " + loc);
//        getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(loc)
                .tilt(0).zoom(NAVIGATION_ZOOM_LEVEL).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//            }
//        });
    }


    void adjustCamera(LatLng latLng, float zoomLevel) {
        Log.d(TAG, "adjustCamera: location: " + latLng + " zoomLevel: " + zoomLevel);
//         getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
        CameraPosition cameraPosition = new CameraPosition.Builder().target(latLng)
                .tilt(0).zoom(zoomLevel).build();
        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
//            }
//        });
    }


    public BitmapDescriptor getMarkerIconFromDrawable(Drawable drawable) {
        Canvas canvas = new Canvas();
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
        drawable.draw(canvas);
        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }


    public void removePreviousExpectedRouteMarkers() {
        Log.d(TAG, "rerouting: removePreviousExpectedRouteMarkers: ");
//        Log.d(TAG, "rerouting: removePreviousExpectedRouteMarkers: segmentMarkerList: size: " + segmentMarkerList.size());
//        Log.d(TAG, "rerouting: removePreviousExpectedRouteMarkers: routePointsMarkerList: size: " + routePointsMarkerList.size());
        for (int i = 0; i < segmentMarkerList.size(); i++) {

            segmentMarkerList.get(i).remove();
        }

        for (int i = 0; i < routePointsMarkerList.size(); i++) {

            routePointsMarkerList.get(i).remove();
        }

        isPointsDisplayed = true;
        if (segmentMarkerList != null) {
            segmentMarkerList.clear();
        }

        if (routePointsMarkerList != null) {
            routePointsMarkerList.clear();
        }
    }

    public void removeAllIncludedMarkers() {
        Log.d(TAG, "rerouting: removeAllIncludedMarkers: ");

        isPointsDisplayed = true;
        if (segmentMarkerList != null) {
            segmentMarkerList.clear();
        }

        if (routePointsMarkerList != null) {
            Log.d(TAG, "removeAllIncludedMarkers: routePointsMarkerList cleared");
            routePointsMarkerList.clear();
        }

        if (googleMap != null) {
            googleMap.clear();
        }
    }


    public Boolean isSameAsInitialLocation(LatLng latLng) {

        Log.d(TAG, "isSameAsInitialLocation: initialMapRouteLatLng: " + initialMapRouteLatLng);
        if (initialMapRouteLatLng != null
                && initialMapRouteLatLng.longitude == latLng.longitude
                && initialMapRouteLatLng.latitude == latLng.latitude) {

            Log.d(TAG, "isSameAsInitialLocation SAME LOCATION AS INITIAL LOCATION DETECTED");
            if (rules.isDebugMode()) {
                Toast.makeText(getContext(), "SAME LOCATION AS INITIAL LOCATION DETECTED", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return false;
    }

    public void removeVisitedPoint() {
        for (int i = 0; i < displayedRoute.getPoints().size(); i++) {
            List<LatLng> polyline = new ArrayList<>();
            polyline.add(displayedRoute.getPoints().get(i));

            if (displayedRoute.getLastVisitedPoint() != null) {
                if (PolyUtil.isLocationOnPath(displayedRoute.getLastVisitedPoint(), polyline,
                        true, 10)) {
                    if (routePointsMarkerList != null && i < routePointsMarkerList.size()) {
                        Log.d(TAG, "removeVisitedPoint: index: " + i + " size: " + routePointsMarkerList.size());
                        Marker visitedMarker = routePointsMarkerList.get(i);
                        visitedMarker.remove();
                    }
                }
            }
        }
    }


    public int getSelectedStepNearRoutePoints(LatLng currentPositionOnTheRoute) {
        Log.d(TAG, "getSelectedStepNearRoutePoints: lastSavedIndex: " + lastSavedIndex);

        int counter = lastSavedIndex;
        if (counter < 0)
            counter = 0;

        double maxDistance = 1000;
        for (int i = counter; i < displayedRoute.getPoints().size(); i++) {

            List<LatLng> polyline = new ArrayList<>();
            polyline.add(displayedRoute.getPoints().get(i));
            if (i < (displayedRoute.getPoints().size() - 1)) {
                polyline.add(displayedRoute.getPoints().get(i + 1));
            }


            LatLng startPoint = displayedRoute.getPoints().get(i);
            double straightDistance = SphericalUtil.computeDistanceBetween(startPoint, currentPositionOnTheRoute);
            Log.d(TAG, "getSelectedStepNearRoutePoints: straightDistance: " + straightDistance +
                    " startPoint: " + startPoint + " currentPositionOnTheRoute: " + currentPositionOnTheRoute + " maxDistance: " + maxDistance);

            if (straightDistance < maxDistance) {
                maxDistance = straightDistance;
                lastSavedIndex = i;
            } else {
                if (lastSavedIndex >= displayedRoute.getPoints().size()) {
                    lastSavedIndex = displayedRoute.getPoints().size() - 1;
                }
                return lastSavedIndex;
            }

            if (straightDistance < 10) {
                if (i >= (displayedRoute.getPoints().size())) {
                    lastSavedIndex = i;
                    return lastSavedIndex;
                } else {
                    if (i <= (displayedRoute.getPoints().size() - 2)) {
                        lastSavedIndex = (i + 2);
                        Log.d(TAG, "getSelectedStepNearRoutePoints: indexValueForI: " + i + " lastSavedIndex: " + lastSavedIndex);
                        return lastSavedIndex;
                    }

                }
            }

//            if (PolyUtil.isLocationOnPath(displayedRoute.getLastVisitedPoint(), polyline,
//            if (PolyUtil.isLocationOnPath(getMyLastLocation(), polyline,
//                    true, 5)) {


//            Log.d(TAG, "getSelectedStepNearRoutePoints: findWithin tolerance: 3: yes: lastSavedIndex: " + lastSavedIndex);
//            if (i >= (displayedRoute.getPoints().size())) {
//                lastSavedIndex = i;
//                return lastSavedIndex;
//            } else {
//                if (i <= (displayedRoute.getPoints().size() - 2)) {
//                    lastSavedIndex = (i + 2);
//                    Log.d(TAG, "getSelectedStepNearRoutePoints: indexValueForI: " + i + " lastSavedIndex: " + lastSavedIndex);
//                    return lastSavedIndex;
//                }

//                    else if (i <= (displayedRoute.getPoints().size() - 1)) {
//                        lastSavedIndex = (i + 1);
//                        Log.d(TAG, "getSelectedStepNearRoutePoints: by (i + 1) And lastSavedIndex: " + lastSavedIndex);
//                        routeLastSavedIndex = i;
//                        return lastSavedIndex;
//                    }
//            }
//            }
        }

//        Log.d(TAG, "getSelectedStepNearRoutePoints: lastSavedIndex: not found within tolerance of 5 meters");
//        if (lastSavedIndex < (displayedRoute.getPoints().size())) {
//            lastSavedIndex++;
//            Log.d(TAG, "getSelectedStepNearRoutePoints: (lastSavedIndex < (displayedRoute.getPoints().size())): lastSavedIndex: " + lastSavedIndex);
//            return lastSavedIndex;
//        } else {
//            Log.d(TAG, "getSelectedStepNearRoutePoints: lastSavedIndex: " + lastSavedIndex);
        return lastSavedIndex;
//        }
    }


    public int getBearingIndexPointForTheCurrentLocation(LatLng currentPositionOnTheRoute) {
        Log.d(TAG, "getBearingIndexPointForTheCurrentLocation: lastSavedIndexForBearing: " + lastSavedIndexForBearing);

        int counter = lastSavedIndexForBearing;

        double maxDistance = 1000;
        for (int i = counter; i < displayedRoute.getPoints().size(); i++) {

            List<LatLng> polyline = new ArrayList<>();
            polyline.add(displayedRoute.getPoints().get(i));
            if (i < (displayedRoute.getPoints().size() - 1)) {
                polyline.add(displayedRoute.getPoints().get(i + 1));
            }


            LatLng startPoint = displayedRoute.getPoints().get(i);
            double straightDistance = SphericalUtil.computeDistanceBetween(startPoint, currentPositionOnTheRoute);
//            Log.d(TAG, "getBearingIndexPointForTheCurrentLocation: ForLoop: index: " + i);
//            Log.d(TAG, "getBearingIndexPointForTheCurrentLocation: straightDistance: " + straightDistance);
//            Log.d(TAG, "getBearingIndexPointForTheCurrentLocation: startPoint: " + startPoint);
//            Log.d(TAG, "getBearingIndexPointForTheCurrentLocation: currentPositionOnTheRoute: " + currentPositionOnTheRoute);
//            Log.d(TAG, "getBearingIndexPointForTheCurrentLocation: maxDistance: " + maxDistance);

            if (straightDistance < maxDistance) {
                maxDistance = straightDistance;
                lastSavedIndexForBearing = i;
                Log.d(TAG, "getBearingIndexPointForTheCurrentLocation: if: lastSavedIndexForBearing: " + lastSavedIndexForBearing);
            } else {
                if (lastSavedIndexForBearing >= displayedRoute.getPoints().size()) {
                    lastSavedIndexForBearing = displayedRoute.getPoints().size() - 1;
                }
                Log.d(TAG, "getBearingIndexPointForTheCurrentLocation: else: lastSavedIndexForBearing: " + lastSavedIndexForBearing);
                lastSavedIndexForBearing++;
                return lastSavedIndexForBearing;
            }
        }

        return lastSavedIndexForBearing;
    }

    public boolean isSegmentEnds() {
        Log.d(TAG, "isSegmentEnds: ");

        boolean isSegmentEnded = false;
//        Log.d(TAG, "segmentEnds: segments: size: " + displayedRoute.getSegments().size());
        for (int x = 0; x < displayedRoute.getSegments().size(); x++) {

//            segment polyline will only contains the end points of a segment
            List<LatLng> segmentPolyline = new ArrayList<>();
            int selectedSegmentSize = displayedRoute.getSegments().get(x).getSegmentPoints().size();

            if (selectedSegmentSize > 0) {

                segmentPolyline.add(displayedRoute.getSegments().get(x).getSegmentPoints().get(selectedSegmentSize - 1));
            }

            if (displayedRoute.getLastVisitedPoint() != null) {
                if (PolyUtil.isLocationOnPath(displayedRoute.getLastVisitedPoint(),
                        segmentPolyline, true, 10)) {
                    isSegmentEnded = true;
                    Log.d(TAG, "segmentEnds: tolerance: 15: matched: segment: " + x + " and point: " + displayedRoute.getSegments().get(x).getSegmentPoints().get(selectedSegmentSize - 1));
                }
            }

        }

        return isSegmentEnded;
    }

    public void getCurrentRouteSegment() {
        Log.d(TAG, "getCurrentRouteSegment: ");

        for (int x = 0; x < displayedRoute.getSegments().size(); x++) {

            List<LatLng> segmentPolyline = displayedRoute.getSegments().get(x).getSegmentPoints();
            int selectedSegmentSize = segmentPolyline.size();
            if (displayedRoute.getLastVisitedPoint() != null) {
                if (PolyUtil.isLocationOnPath(displayedRoute.getLastVisitedPoint(),
                        segmentPolyline, true, 15)) {
                    Log.d(TAG, "getCurrentRouteSegment: tolerance: 15: matched: segment: " + x);
                    if (selectedSegmentSize > 0) {

                        LatLng lastPoint = displayedRoute.getSegments().get(x).getSegmentPoints().get(selectedSegmentSize - 1);
//                    getMaxSpeedForCurrentSegment(displayedRoute.getLastVisitedPoint(), lastPoint);
                    }
                }
            }
        }
    }


    public float bearing(LatLng begin, LatLng end) {
//        Log.d(TAG, "bearing: ");

        double latitude1 = Math.toRadians(begin.latitude);
        double latitude2 = Math.toRadians(end.latitude);
        double longDiff = Math.toRadians(end.longitude - begin.longitude);

        double longitude1 = Math.toRadians(begin.longitude);
        double longitude2 = Math.toRadians(end.longitude);

        double long2Diff = longitude2 - longitude1;
//        Log.d(TAG, "bearing: compare: longDiff: " + longDiff);
//        Log.d(TAG, "bearing: compare: longDiff: 2: " + long2Diff);
//        Log.d(TAG, "bearing: compare: ");

        double y = Math.sin(longDiff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(longDiff);


//        Log.d(TAG, "bearing: afterCalculation: " + (float) (Math.toDegrees(Math.atan2(y, x))));
        return (float) (Math.toDegrees(Math.atan2(y, x)));
    }

    public float bearingUpdate(LatLng begin, LatLng end) {
        Log.d(TAG, "bearing: ");

        double latitude1 = Math.toRadians(begin.latitude);
        double latitude2 = Math.toRadians(end.latitude);

//        THIS LINE
//        double longDiff = Math.toRadians(end.longitude - begin.longitude);

//        Is the same as:

        double longitude1 = Math.toRadians(begin.longitude);
        double longitude2 = Math.toRadians(end.longitude);

        double long2Diff = longitude2 - longitude1;
        Log.d(TAG, "bearing: longDiff: 2: " + long2Diff);
//????

        double y = Math.sin(long2Diff) * Math.cos(latitude2);
        double x = Math.cos(latitude1) * Math.sin(latitude2) - Math.sin(latitude1) * Math.cos(latitude2) * Math.cos(long2Diff);

//        Log.d(TAG, "bearing: afterCalculation: " + (float) (Math.toDegrees(Math.atan2(y, x))));
        return (float) (Math.toDegrees(Math.atan2(y, x)));
    }

    public List<Polyline> clearPolyline(List<Polyline> polylines) {
        Log.d(TAG, "dismissNavigation: clearPolyline: ");
        if (polylines != null && polylines.size() > 0) {
            for (Polyline poly : polylines) {
                poly.remove();
            }
        }

        return new ArrayList<>();
    }

    public interface LoadDataOnLocationChange {
        void OnLocationChange(Location loc, GoogleMap m);
    }

    public interface UpdateLocation {
        void OnLocationUpdated(LatLng loc);

        void setCurrentSpeed(float speed);
    }

    public void setDestinationInstructionAlreadyGiven(boolean isDestinationInstructionAlreadyGiven) {
        this.isDestinationInstructionAlreadyGiven = isDestinationInstructionAlreadyGiven;
    }

    public boolean isDestinationInstructionAlreadyGiven() {
        return isDestinationInstructionAlreadyGiven;
    }

    public boolean isPointsDisplayed() {
        return isPointsDisplayed;
    }

    public void setDestinationAddress(String destinationAddress) {
        this.destinationAddress = destinationAddress;
    }

    public String getDestinationAddress() {
        return destinationAddress;
    }

    public void setDisplayedRoute(Route displayedRoute) {
        Log.d(TAG, "setDisplayedRoute: displayedRoute: " + displayedRoute);
        this.displayedRoute = displayedRoute;
    }

    public Route generatedRoute(LatLng currentLatLng) {
        Log.d(TAG, "handleNavigation: generatedRoute: ");
        int size = displayedRoute.getSegments().size();

        displayedRoute.initVisitedPoint();

        List<LatLng> currentVisitedPoints = new ArrayList<>();
        int currentStep = -1;
//        for (int i = 0; i < size; i++) {
        for (int i = lastVisitedSegment; i < size; i++) {
            Segment stepPoints = displayedRoute.getSegments().get(i);
            if (PolyUtil.isLocationOnPath(currentLatLng, stepPoints.getSegmentPoints(), true, 15/*10 meter Tolerance*/)) {
                Log.d(TAG, "generatedRoute: findNearestPointFromSegment: ");

                if (lastVisitedSegment != i) {
                    NavigationUtils.lastVisitedSegmentPoint = 0;
                    Log.d(TAG, "findNearestPointFromRoute: resetting last visited segment: " + NavigationUtils.lastVisitedSegmentPoint);
                }
                lastVisitedSegment = i;

                LatLng nearestLocation = NavigationUtils.findNearestPointFromSegment(currentLatLng, displayedRoute.getSegments().get(i).getSegmentPoints());
                if (nearestLocation == null) {
                    Log.d(TAG, "generatedRoute: nearestLocation: is null" + nearestLocation);
//                    April 08, 2022    -   What should we do if nearest location is null, Should we skip?
                    continue;
                }

                int currentPointInStep = -1;
                for (int j = 0; j < stepPoints.getSegmentPoints().size() - 1; j++) {

                    LatLng currentLocation = stepPoints.getSegmentPoints().get(j);
                    LatLng nextLocation = stepPoints.getSegmentPoints().get(j + 1);
                    List<LatLng> visitedPointInSegment = new ArrayList<>();
                    visitedPointInSegment.add(currentLocation);
                    visitedPointInSegment.add(nextLocation);

//                    Previous value 5, changed it for testing
                    if (PolyUtil.isLocationOnPath(nearestLocation, visitedPointInSegment, true, 15)) {
                        Log.d(TAG, "generatedRoute: currentPointInStep: " + currentPointInStep + " and index: " + j);
                        currentPointInStep = j;
                    }
                }

                if (currentPointInStep > 0)
                    currentVisitedPoints.addAll(stepPoints.getSegmentPoints().subList(0, currentPointInStep));

                currentVisitedPoints.add(nearestLocation);

                displayedRoute.setCurrentStep(i);
                LatLng lastLatLng = stepPoints.getSegmentPoints().get(stepPoints.getSegmentPoints().size() - 1);
                double distanceInMeters = SphericalUtil.computeDistanceBetween(currentLatLng, lastLatLng);
                stepPoints.setDistanceText(Utils.convertDistanceInMiles(distanceInMeters));
                stepPoints.setCurrentRemainingDistance(distanceInMeters);

                currentStep = i;
            }
        }
        displayedRoute.addVisitedPoint(displayedRoute.getPoints().get(0));
        if (currentStep > 0)
            for (int i = 0; i < currentStep; i++) {
                Segment stepPoints = displayedRoute.getSegments().get(i);
                displayedRoute.addVisitedPoints(stepPoints.getSegmentPoints());
            }
        for (int i = 0; i < currentVisitedPoints.size(); i++) {
            if (!displayedRoute.getVisitedPoints().contains(currentVisitedPoints.get(i)))
                displayedRoute.addVisitedPoint(currentVisitedPoints.get(i));
        }
        Log.d(TAG, "generatedRoute: displayedRoute: visitedPoints: size: " + displayedRoute.getVisitedPoints().size());

//        addBlueMarkerForMyVisitedPosition(displayedRoute.getLastVisitedPoint());
        return displayedRoute;
    }

    public int lastVisitedSegment = 0;

    public LatLng findNearestPointFromRoute(LatLng currentLatLng) {
//        Log.d(TAG, "getSelectedStepNearRoutePoints: findNearestPointFromRoute: lastVisitedSegment: " + lastVisitedSegment);
//        Log.d(TAG, "GRD: point: currentLatLng: " + currentLatLng);
        int size = displayedRoute.getSegments().size();

        boolean isFar = true;
        for (int i = lastVisitedSegment; i < size; i++) {
            Segment stepPoints = displayedRoute.getSegments().get(i);
//            Log.d(TAG, "findNearestPointFromRoute: ");
            if (PolyUtil.isLocationOnPath(currentLatLng, stepPoints.getSegmentPoints(), true, 15/*10 meter Tolerance*/)) {
                if (lastVisitedSegment != i) {
                    NavigationUtils.lastVisitedSegmentPoint = 0;
//                    Log.d(TAG, "findNearestPointFromRoute: resetting last visited segment: " + NavigationUtils.lastVisitedSegmentPoint);
                }
                lastVisitedSegment = i;
//                Log.d(TAG, "findNearestPointFromRoute: findNearestPointFromSegment: ");
                LatLng nearestLocation = NavigationUtils.findNearestPointFromSegment(currentLatLng, displayedRoute.getSegments().get(i).getSegmentPoints());
                isFar = false;
//                addBlueMarkerForMyVisitedPosition(nearestLocation);
                return nearestLocation;
            }
        }

//        if (isFar) {
//            addWhiteMarkerForMyVisitedPosition(currentLatLng);
//        }

        LatLng currentPointCoordinates = displayedRoute.getLastVisitedPoint();
        if (currentPointCoordinates == null) {
            currentPointCoordinates = getMyLastLocation();
        }

        return currentPointCoordinates;
    }

    public void resetLastVisitedSegment() {
        lastVisitedSegment = 0;
    }


    public LatLng getBearingPoint() {
        Log.d(TAG, "setting: displayedRoute: getBearingPoint: lastVisitedSegment: " + lastVisitedSegment);
        int pointIndex = NavigationUtils.lastVisitedSegmentPoint + 2;
        Log.d(TAG, "getBearingPoint: pointIndex: " + pointIndex);
        Segment currentSegment = null;
        if (lastVisitedSegment < displayedRoute.getSegments().size()) {
            currentSegment = displayedRoute.getSegments().get(lastVisitedSegment);
        } else {
//            April 08, 2022    -   Crash occurred when we went over the destination after reaching to destination point
//            So we should return the current location as bearing
            return getMyLastLocation();
        }
        Log.d(TAG, "getBearingPoint: currentSegment: " + currentSegment);
        if (pointIndex >= currentSegment.getSegmentPoints().size()) {
            pointIndex = currentSegment.getSegmentPoints().size() - 1;
        }
        Log.d(TAG, "getBearingPoint: pointIndex: " + pointIndex);
        LatLng point = displayedRoute.getSegments().get(lastVisitedSegment).getSegmentPoints().get(pointIndex);
        return point;
    }

    public int getCurrentRouteSegmentPosition(LatLng currentLatLng) {
        Log.d(TAG, "maxSpeed: getCurrentRouteSegment: currentLatLng: " + currentLatLng);

        if (displayedRoute != null) {
            for (int x = 0; x < displayedRoute.getSegments().size(); x++) {
                Log.d(TAG, "maxSpeed: getCurrentRouteSegment: size: " + displayedRoute.getSegments().size());
                List<LatLng> segmentPolyline = displayedRoute.getSegments().get(x).getSegmentPoints();
                int selectedSegmentSize = segmentPolyline.size();

                Log.d(TAG, "maxSpeed: getCurrentRouteSegmentPosition: lastVisitedPoint: " + displayedRoute.getLastVisitedPoint());
                if (displayedRoute.getLastVisitedPoint() != null) {
                    if (PolyUtil.isLocationOnPath(currentLatLng,
                            segmentPolyline, true, 15)) {
                        Log.d(TAG, "maxSpeed: getCurrentRouteSegment: tolerance: 5: matched: segment: " + x);
                        if (selectedSegmentSize > 0) {
                            return x;
                        }
                    }
                }
            }
        }

        Log.d(TAG, "onLocationChanged: destination: getCurrentRouteSegmentPosition: return 0: ");
        return 0;
    }


    public void setInitialMapRouteLatLng(LatLng initialMapRouteLatLng) {
        this.initialMapRouteLatLng = initialMapRouteLatLng;
    }

    public LatLng displayedArea(Segment currentStep) {
        float rotation = bearing(currentStep.startPoint(), currentStep.endPoint());

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int orientation = getResources().getConfiguration().orientation;

        Point targetPoint = new Point(metrics.widthPixels / 2, metrics.heightPixels);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            targetPoint = new Point(metrics.heightPixels, metrics.widthPixels / 2);
        }
        Log.d(TAG, "displayedArea: targetPointX: " + targetPoint.x + " and targetPointY: " + targetPoint.y);

        LatLng centerLatLong = googleMap.getProjection().fromScreenLocation(targetPoint);

        double fromCenterToTarget = SphericalUtil.computeDistanceBetween(centerLatLong, googleMap.getCameraPosition().target);

        Log.e("point", "distance : " + fromCenterToTarget / 2);
        Log.d(TAG, "displayedArea: fromCenterToTarget: " + (fromCenterToTarget));
        Log.d(TAG, "displayedArea: fromCenterToTarget/2: " + (fromCenterToTarget / 2));

        LatLng latLngForTarget = SphericalUtil.computeOffset(displayedRoute.getLastVisitedPoint(), fromCenterToTarget / 1.8, rotation);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            latLngForTarget = SphericalUtil.computeOffset(displayedRoute.getLastVisitedPoint(), fromCenterToTarget / 1.4, rotation);
        }

        return latLngForTarget;
    }

    public LatLng displayedArea(Segment currentStep, LatLng currentPosition) {
        float rotation = bearing(currentStep.startPoint(), currentStep.endPoint());

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int orientation = getResources().getConfiguration().orientation;

        Point targetPoint = new Point(metrics.widthPixels / 2, metrics.heightPixels);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            targetPoint = new Point(metrics.heightPixels, metrics.widthPixels / 2);
        }
        Log.d(TAG, "displayedArea: targetPointX: " + targetPoint.x + " and targetPointY: " + targetPoint.y);

        LatLng centerLatLong = googleMap.getProjection().fromScreenLocation(targetPoint);

        double fromCenterToTarget = SphericalUtil.computeDistanceBetween(centerLatLong, googleMap.getCameraPosition().target);

        Log.e("point", "distance : " + fromCenterToTarget / 2);
        Log.d(TAG, "displayedArea: fromCenterToTarget: " + (fromCenterToTarget));
        Log.d(TAG, "displayedArea: fromCenterToTarget/2: " + (fromCenterToTarget / 2));

        LatLng currentPointCoordinates = displayedRoute.getLastVisitedPoint();
        currentPointCoordinates = currentPosition;
        if (currentPointCoordinates == null) {
            currentPointCoordinates = getMyLastLocation();
        }

        LatLng latLngForTarget = SphericalUtil.computeOffset(currentPointCoordinates, fromCenterToTarget / 1.8, rotation);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            latLngForTarget = SphericalUtil.computeOffset(currentPointCoordinates, fromCenterToTarget / 1.4, rotation);
        }

        return latLngForTarget;
    }

    public LatLng getNewLatLngFromOffset(LatLng currentPosition, int offset) {
        return currentPosition;
//        April 08, 2022    -   Trying to move the position to the bottom but maps starts jumping
//        Point centerCoordinate = googleMap.getProjection().toScreenLocation(currentPosition);
//        centerCoordinate.y = centerCoordinate.y + offset;
//        LatLng newLatLng = googleMap.getProjection().fromScreenLocation(centerCoordinate);
//        return newLatLng;
    }

    public LatLng displayedArea(LatLng startPoint, LatLng endPoint, LatLng currentPosition) {
        float rotation = bearing(startPoint, endPoint);

        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);

        int orientation = getResources().getConfiguration().orientation;

        Point targetPoint = new Point(metrics.widthPixels / 2, metrics.heightPixels);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            targetPoint = new Point(metrics.heightPixels, metrics.widthPixels / 2);
        }

        LatLng centerLatLong = googleMap.getProjection().fromScreenLocation(targetPoint);
        double fromCenterToTarget = SphericalUtil.computeDistanceBetween(centerLatLong,
                googleMap.getCameraPosition().target);

//        1.2, 1.4, 1.6, 1.8
//        The lower the value of divider from center to target, the closer it is from bottom of the screen
        LatLng currentPointCoordinates = displayedRoute.getLastVisitedPoint();
        currentPointCoordinates = currentPosition;
        if (currentPointCoordinates == null) {
            currentPointCoordinates = getMyLastLocation();
        }

        LatLng latLngForTarget = SphericalUtil.computeOffset(currentPointCoordinates,
                fromCenterToTarget / 1.9, rotation);
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            latLngForTarget = SphericalUtil.computeOffset(currentPointCoordinates, fromCenterToTarget / 1.4, rotation);
        }

        return latLngForTarget;
    }

    public void enableZoomControls(boolean show) {
        googleMap.getUiSettings().setZoomControlsEnabled(show);
    }

    //            Oct 10, 2022  -   We enable compass here
    public void enableCompassControls(boolean show) {
        googleMap.getUiSettings().setCompassEnabled(show);

    }

    public void enableLocationButton(boolean show) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
//        googleMap.setMyLocationEnabled(show);
        googleMap.getUiSettings().setMyLocationButtonEnabled(show);
    }

    public void enableLocationPointer(boolean visibility) {
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        googleMap.setMyLocationEnabled(visibility);

    }

    private void handleNightView() {
        if (getMyLastLocation() != null) {
            isNight = rules.isItNight(getMyLastLocation());
            googleMap.setMapStyle(isNight ? MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_in_night) : MapStyleOptions.loadRawResourceStyle(getActivity(), R.raw.map_in_day));
//        July 01, 2022 -   We should disable showing the buildings
            googleMap.setBuildingsEnabled(false);
/*
            if (isNight) {
                // tvSpeed.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
                // tvTxtMph.setTextColor(ContextCompat.getColor(requireActivity(), R.color.white));
            }*/
        }
    }

    public void setNight(boolean isNight) {
        this.isNight = isNight;
    }

    public int calculateZoomLevel(float displayedArea, FrameLayout mapContainer, Activity activity) {
        return calculateZoomLevel(displayedArea, null, null, mapContainer, activity);
    }

    private int calculateZoomLevel(float displayedArea, LatLng statPoint, LatLng nextPoint, FrameLayout mapContainer, Activity activity) {
        if (statPoint != null && nextPoint != null) {
            double distance = SphericalUtil.computeDistanceBetween(statPoint, nextPoint) * 5;

//            Circle circle = googleMap.addCircle(new CircleOptions().center(nextPoint).radius(10).fillColor(Color.RED));
//            circle.setVisible(true);
            Log.e("Zoom", "" + distance);

            if (distance > displayedArea) {
                displayedArea = (float) distance;
            } else if (distance < 50) {
                return 19;
            }
        }

        DisplayMetrics metrics = activity.getResources().getDisplayMetrics();
        float screenWidth = mapContainer.getWidth() / metrics.scaledDensity;
        double equatorLength = 40075004; // in meters
        double metersPerPixel = equatorLength / 256;
        int zoomLevel = 1;

        while ((metersPerPixel * screenWidth) > displayedArea) {
            metersPerPixel /= 2;
            ++zoomLevel;
        }

        Log.e("Zoom", "" + zoomLevel);
        return zoomLevel;
    }

    public float getCurrentZoomValue() {
        return googleMap.getCameraPosition().zoom;
    }


}
