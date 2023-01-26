package com.rco.rcotrucks.utils.route;

/**
 * Async Task to access the Google Direction API and return the routing data
 * which is then parsed and converting to a route overlay using some classes created by Hesham Saeed.
 * Requires an instance of the map activity and the application's current context for the progress dialog.
 */

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.ArrayList;
import java.util.List;


public abstract class AbstractRouting extends AsyncTask<Void, Void, List<Route>> {
//public abstract class AbstractRouting{
    protected List<RoutingListener> alisteners;

//    private static final String TAG = "SearchRelevant: AbtRout";
    private static final String TAG = AbstractRouting.class.getSimpleName();
    protected static final String DIRECTIONS_API_URL = "https://maps.googleapis.com/maps/api/directions/json?";

    /* Private member variable that will hold the RouteException instance created in the background thread */
    private RouteException mException = null;

    public enum TravelMode {
        BIKING("bicycling"),
        DRIVING("driving"),
        WALKING("walking"),
        TRANSIT("transit");

        protected String sValue;

        TravelMode(String sValue) {
            this.sValue = sValue;
        }

        protected String getValue() {
            return sValue;
        }
    }

    public enum AvoidKind {
        TOLLS(1, "tolls"),
        HIGHWAYS(1 << 1, "highways"),
        FERRIES(1 << 2, "ferries");

        private final String sRequestParam;
        private final int sBitValue;

        AvoidKind(int bit, String param) {
            this.sBitValue = bit;
            this.sRequestParam = param;
        }

        protected int getBitValue() {
            return sBitValue;
        }

        protected static String getRequestParam(int bit) {
            Log.d(TAG, "getRequestParam: ");
            StringBuilder ret = new StringBuilder();
            for (AvoidKind kind : AvoidKind.values()) {
                if ((bit & kind.sBitValue) == kind.sBitValue) {
                    ret.append(kind.sRequestParam).append('|');
                }
            }
            Log.d(TAG, "getRequestParam: ret: " + ret);
            return ret.toString();
        }
    }

    protected AbstractRouting(RoutingListener listener) {
        Log.d(TAG, "AbstractRouting: ");
        this.alisteners = new ArrayList<RoutingListener>();
        registerListener(listener);
    }

    public void registerListener(RoutingListener mListener) {
        Log.d(TAG, "registerListener: ");
        if (mListener != null) {
            alisteners.add(mListener);
        }
    }

    protected void dispatchOnStart() {
        Log.d(TAG, "dispatchOnStart: ");
        for (RoutingListener mListener : alisteners) {
            mListener.onRoutingStart();
        }
    }

    protected void dispatchOnFailure(RouteException exception) {
        Log.d(TAG, "dispatchOnFailure: exception: " + exception.getMessage());
        for (RoutingListener mListener : alisteners) {
            mListener.onRoutingFailure(exception);
        }
    }

    protected void dispatchOnSuccess(List<Route> route, int shortestRouteIndex, String routeName) {
        Log.d(TAG, "dispatchOnSuccess: route: "+route);
        Log.d(TAG, "dispatchOnSuccess: shortestRouteIndex: "+shortestRouteIndex);
        for (RoutingListener mListener : alisteners) {
            mListener.onRoutingSuccess(route, shortestRouteIndex, routeName);
        }
    }

    private void dispatchOnCancelled() {
        Log.d(TAG, "dispatchOnCancelled: ");
        for (RoutingListener mListener : alisteners) {
            mListener.onRoutingCancelled();
        }
    }

    /**
     * Performs the call to the google maps API to acquire routing data and
     * deserializes it to a format the map can display.
     *
     * @return an array list containing the routes
     */
    @Override
    protected List<Route> doInBackground(Void... voids) {
//        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Log.d(TAG, "doInBackground: ");
        List<Route> result = new ArrayList<Route>();
        Log.d(TAG, "doInBackground: result: "+result);
        try {
            String url = constructURL();
            Log.d(TAG, "abstractRouting: URL: " + url);
            result = new GoogleParser(url).parse();
        } catch (RouteException e) {
            Log.d(TAG, "doInBackground: RouteException: "+e.getMessage());
            mException = e;
        }
        return result;
    }

    protected abstract String constructURL();
    protected abstract String getRouteId();

    @Override
    protected void onPreExecute() {
        Log.d(TAG, "onPreExecute: ");
        dispatchOnStart();
    }

    @Override
    protected void onPostExecute(List<Route> result) {
        Log.d(TAG, "onPostExecute: result: "+result);
        if (!result.isEmpty()) {
            int shortestRouteIndex = 0;
            int minDistance = Integer.MAX_VALUE;
            String routeId = getRouteId();

            for (int i = 0; i < result.size(); i++) {
                PolylineOptions mOptions = new PolylineOptions();
                Route route = result.get(i);

                Log.d(TAG, "onPostExecute: shortestRoute: length: "+route.getLength());
                Log.d(TAG, "onPostExecute: shortestRoute: distance: value: "+route.getDistanceValue());
                Log.d(TAG, "onPostExecute: shortestRoute: distance: text: "+route.getDistanceText());
                // Find the shortest route index
                if (route.getLength() < minDistance) {
                    Log.d(TAG, "onPostExecute: shortestRoute: index: "+i+" :route.length: "+route.getLength());
                    shortestRouteIndex = i;
                    minDistance = route.getLength();
                }

                for (LatLng point : route.getPoints()) {
                    mOptions.add(point);
                }
                result.get(i).setPolyOptions(mOptions);
            }
            dispatchOnSuccess(result, shortestRouteIndex, routeId);
        } else {
            dispatchOnFailure(mException);
        }
    }

    // end onPostExecute method

    @Override
    protected void onCancelled() {
        Log.d(TAG, "onCancelled: ");
        dispatchOnCancelled();
    }

}
