package com.rco.rcotrucks.activities.drive.direction;

import android.graphics.Point;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.material.navigation.NavigationView;
import com.google.maps.android.PolyUtil;

import java.util.List;

public class NavigationUtils {

    private static final String TAG = NavigationUtils.class.getSimpleName();

    public static final float EARTH_RADIUS_M = 6371 * 1000;

    public static double distanceBetweenPoints(LatLng startLatLng, LatLng endLatLng) {
        double startLatRad = Math.toRadians(startLatLng.latitude);
        double endLatRad = Math.toRadians(endLatLng.latitude);
        double deltaLat = Math.toRadians((endLatLng.latitude - startLatLng.latitude));
        double deltaLong = Math.toRadians(endLatLng.longitude - startLatLng.longitude);

        double temp = (Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                Math.cos(startLatRad) * Math.cos(endLatRad) *
                        Math.sin(deltaLong / 2) * Math.sin(deltaLong / 2));

        double c = (2 * Math.atan2(Math.sqrt(temp), Math.sqrt(1 - temp)));

        return EARTH_RADIUS_M * c; //distance in meters
    }

    public static int lastVisitedSegmentPoint = 0;

    public static LatLng findNearestPointFromSegment(LatLng currentPosition, List<LatLng> target) {
        double distance = -1;
        LatLng minimumDistancePoint = currentPosition;
        LatLng closestPoint = null;
        Double shortestDistance = 0.0;

        if (currentPosition == null || target == null) {
            return minimumDistancePoint;
        }

        int counter = lastVisitedSegmentPoint;
        for (int i = counter; i < (target.size() - 1); i++) {
            LatLng point = target.get(i);
            LatLng nextPoint = target.get(i + 1);

            int segmentPoint = i + 1;
            if (segmentPoint >= target.size()) {
                segmentPoint = 0;
            }

            double currentDistance = PolyUtil.distanceToLine(currentPosition, point, target.get(segmentPoint));
            if (distance == -1 || currentDistance < distance) {
                distance = currentDistance;
                minimumDistancePoint = findNearestPoint(currentPosition, point, target.get(segmentPoint));
//                findShortestDistance(float startX, float startY, float endX, float endY, float originX, float originY)
                shortestDistance = findShortestDistance(point.latitude, point.longitude, nextPoint.latitude, nextPoint.longitude,
                        currentPosition.latitude, currentPosition.longitude);

                closestPoint = findClosestPoint(point.latitude, point.longitude, nextPoint.latitude, nextPoint.longitude,
                        currentPosition.latitude, currentPosition.longitude);

//                Log.d(TAG, "findNearestPoint: shortestDistance: " + shortestDistance);
//                Log.d(TAG, "findNearestPoint: closestPoint: " + closestPoint);
                lastVisitedSegmentPoint = i;
            }
        }

        Log.d(TAG, "findNearestPointFromSegment: target: size: "+target.size());
        Log.d(TAG, "findNearestPointFromSegment: counter: "+counter);
        Log.d(TAG, "findNearestPoint: closestPoint: " + closestPoint + " , minimumDistancePoint: " + minimumDistancePoint + " shortestDistance: " + shortestDistance);
//        return minimumDistancePoint;
        return closestPoint;
    }

    /**
     * Based on `distanceToLine` method from
     * https://github.com/googlemaps/android-maps-utils/blob/master/library/src/com/google/maps/android/PolyUtil.java
     */
    private static LatLng findNearestPoint(final LatLng currentPosition, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return start;
        }

        final double s0lat = Math.toRadians(currentPosition.latitude);
        final double s0lng = Math.toRadians(currentPosition.longitude);
        final double s1lat = Math.toRadians(start.latitude);
        final double s1lng = Math.toRadians(start.longitude);
        final double s2lat = Math.toRadians(end.latitude);
        final double s2lng = Math.toRadians(end.longitude);

        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);


        if (u <= 0) {
            return start;
        }
        if (u >= 1) {
            return end;
        }

        return new LatLng(start.latitude + (u * (end.latitude - start.latitude)),
                start.longitude + (u * (end.longitude - start.longitude)));
    }

    private static LatLng findNearestPointUpdate(final LatLng currentPosition, final LatLng start, final LatLng end) {
        if (start.equals(end)) {
            return start;
        }

        final double s0lat = Math.toRadians(currentPosition.latitude);
        final double s0lng = Math.toRadians(currentPosition.longitude);
        final double s1lat = Math.toRadians(start.latitude);
        final double s1lng = Math.toRadians(start.longitude);
        final double s2lat = Math.toRadians(end.latitude);
        final double s2lng = Math.toRadians(end.longitude);

        double s2s1lat = s2lat - s1lat;
        double s2s1lng = s2lng - s1lng;
        final double u = ((s0lat - s1lat) * s2s1lat + (s0lng - s1lng) * s2s1lng)
                / (s2s1lat * s2s1lat + s2s1lng * s2s1lng);

        if (u <= 0) {
            return start;
        }
        if (u >= 1) {
            return end;
        }

        return new LatLng(start.latitude + (u * (end.latitude - start.latitude)),
                start.longitude + (u * (end.longitude - start.longitude)));
    }

    private static double findShortestDistance(double startX, double startY, double endX, double endY, double originX, double originY) {

        double px = endX - startX;
        double py = endY - startY;
        double temp = (px * px) + (py * py);
        double u = ((originX - startX) * px + (originY - startY) * py) / (temp);
        if (u > 1) {
            u = 1;
        } else if (u < 0) {
            u = 0;
        }
        double x = startX + u * px;
        double y = startY + u * py;

        double dx = x - originX;
        double dy = y - originY;
        double dist = Math.sqrt(dx * dx + dy * dy);
        return dist;

    }

    private static LatLng findClosestPoint(double startX, double startY, double endX, double endY, double originX, double originY) {
        double px = endX - startX;
        double py = endY - startY;
        double temp = (px * px) + (py * py);
        double u = ((originX - startX) * px + (originY - startY) * py) / (temp);
        if (u > 1) {
            u = 1;
        } else if (u < 0) {
            u = 0;
        }
        double x = startX + u * px;
        double y = startY + u * py;

        return new LatLng(x, y);
    }
}
