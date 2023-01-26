package com.rco.rcotrucks.utils.route;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Async Task to access the Google Direction API and return the routing data.
 */
public class Routing extends AbstractRouting {

    //    private static final String TAG = Routing.class.getSimpleName();
    private static final String TAG = "SearchRelevant: Routing";
    private final TravelMode travelMode;
    private final boolean alternativeRoutes;
    private final List<LatLng> waypoints;
    private final int avoidKinds;
    private final boolean optimize;
    private final String language;
    private final String key;
    private final String routeId;


    private Routing(Builder builder) {
        super(builder.listener);
        Log.d(TAG, "Routing: ");
        this.travelMode = builder.travelMode;
        this.waypoints = builder.waypoints;
        this.avoidKinds = builder.avoidKinds;
        this.optimize = builder.optimize;
        this.alternativeRoutes = builder.alternativeRoutes;
        this.language = builder.language;
        this.key = builder.key;
        this.routeId = builder.routeId;
    }

    protected String getRouteId() {
        return this.routeId;
    }

    protected String constructURL() {
        Log.d(TAG, "constructURL: ");
        final StringBuilder stringBuilder = new StringBuilder(AbstractRouting.DIRECTIONS_API_URL);

        // origin
        final LatLng origin = waypoints.get(0);
        stringBuilder.append("origin=")
                .append(origin.latitude)
                .append(',')
                .append(origin.longitude);

        // destination
        final LatLng destination = waypoints.get(waypoints.size() - 1);

//        Sep 26, 2022  -   Some times it we get null from destination field so returning null
//        Previously app crashed when this happended like
//        Caused by: java.lang.NullPointerException: Attempt to read from field 'double com.google.android.gms.maps.model.LatLng.latitude' on a null object reference in method 'java.lang.String com.rco.rcotrucks.utils.route.Routing.constructURL()'
        if (destination == null) {
            return "null";
        }
        stringBuilder.append("&destination=")
                .append(destination.latitude)
                .append(',')
                .append(destination.longitude);

        // travel
        stringBuilder.append("&mode=").append(travelMode.getValue());

        // waypoints
        if (waypoints.size() > 2) {
            stringBuilder.append("&waypoints=");
            if (optimize)
                stringBuilder.append("optimize:true|");
            for (int i = 1; i < waypoints.size() - 1; i++) {
                final LatLng p = waypoints.get(i);
                stringBuilder.append("via:"); // we don't want to parse the resulting JSON for 'legs'.
                stringBuilder.append(p.latitude);
                stringBuilder.append(',');
                stringBuilder.append(p.longitude);
                stringBuilder.append('|');
            }
        }

        // avoid
        if (avoidKinds > 0) {
            stringBuilder.append("&avoid=");
            stringBuilder.append(AvoidKind.getRequestParam(avoidKinds));
        }

        if (alternativeRoutes) {
            stringBuilder.append("&alternatives=true");
        }

//        July 15, 2022 -   Adding traffic info for duration
        stringBuilder.append("&departure_time=now");

        // sensor
        stringBuilder.append("&sensor=true");

        // language
        if (language != null) {
            stringBuilder.append("&language=").append(language);
        }

        // API key
        if (key != null) {
            stringBuilder.append("&key=").append(key);
        }
        Log.e("test", "" + stringBuilder.toString());
        Log.d(TAG, "constructURL: " + stringBuilder.toString());
        return stringBuilder.toString();
    }

    public static class Builder {

        //        private static final String TAG = "Routing: Builder";
        private static final String TAG = "SearchRelevant: Builder";
        private TravelMode travelMode;
        private boolean alternativeRoutes;
        private List<LatLng> waypoints;
        private int avoidKinds;
        private RoutingListener listener;
        private boolean optimize;
        private String language;
        private String key;
        private String routeId;

        public Builder() {
            Log.d(TAG, "Builder: ");
            this.travelMode = TravelMode.DRIVING;
            this.alternativeRoutes = false;
            this.waypoints = new ArrayList<>();
            this.avoidKinds = 0;
            this.listener = null;
            this.optimize = false;
            this.language = null;
            this.key = null;
            this.routeId = null;
        }

        public Builder travelMode(TravelMode travelMode) {
            Log.d(TAG, "travelMode: travelMode: " + travelMode);
            this.travelMode = travelMode;
            return this;
        }

        public Builder routeId(String routeId) {
            Log.d(TAG, "routeId: routeId: " + routeId);
            this.routeId = routeId;
            return this;
        }

        public Builder alternativeRoutes(boolean alternativeRoutes) {
            Log.d(TAG, "alternativeRoutes: alternativeRoutes: " + alternativeRoutes);
            this.alternativeRoutes = alternativeRoutes;
            return this;
        }

        public Builder waypoints(LatLng... points) {
            Log.d(TAG, "waypoints: points: " + points);
            waypoints.clear();
            Collections.addAll(waypoints, points);
            return this;
        }

        public Builder waypoints(List<LatLng> waypoints) {
            this.waypoints = new ArrayList<>(waypoints);
            return this;
        }

        public Builder optimize(boolean optimize) {
            Log.d(TAG, "optimize: " + optimize);
            this.optimize = optimize;
            return this;
        }

        public Builder avoid(AvoidKind... avoids) {
            Log.d(TAG, "avoid: ");
            for (AvoidKind avoidKind : avoids) {
                this.avoidKinds |= avoidKind.getBitValue();
            }
            return this;
        }

        public Builder language(String language) {
            Log.d(TAG, "language: " + language);
            this.language = language;
            return this;
        }

        public Builder key(String key) {
            Log.d(TAG, "key: " + key);
            this.key = key;
            return this;
        }

        public Builder withListener(RoutingListener listener) {
            Log.d(TAG, "withListener: ");
            this.listener = listener;
            return this;
        }

        public Routing build() {
            Log.d(TAG, "build: ");
            if (this.waypoints.size() < 2) {
                Log.d(TAG, "build: Must supply at least two waypoints to route between.");
                throw new IllegalArgumentException("Must supply at least two waypoints to route between.");
            }
            if (this.waypoints.size() <= 2 && this.optimize) {
                Log.d(TAG, "build: You need at least three waypoints to enable optimize");
                throw new IllegalArgumentException("You need at least three waypoints to enable optimize");
            }

            for (int i = 0; i < this.waypoints.size(); i++) {
                Log.d(TAG, "build: waypoint: index: " + i + " value: " + this.waypoints.get(i));
            }
            return new Routing(this);
        }

    }

}
