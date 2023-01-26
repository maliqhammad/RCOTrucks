package com.rco.rcotrucks.activities.drive;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;
import com.rco.rcotrucks.activities.drive.direction.PathFoundListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class PlaceDetailApi extends AsyncTask<Void, Void, StringBuilder> {

    private static final String TAG = PlaceDetailApi.class.getSimpleName();
    private final String placeID;
    private final String PLACEDetail_API_BASE = "https://maps.googleapis.com/maps/api/place/details";
    private final String OUT_JSON = "/json";
    private final String KEY = "?key=AIzaSyCkXY-OOuAIGFiHisd0EAaQ5m92OmG-qHg";

    private final PathFoundListener pathFoundListener;

    public PlaceDetailApi(String placeID, PathFoundListener pathFoundListener) {
        Log.d(TAG, "SearchRelevant: PlaceDetailApi: placeID: " + placeID);
        this.placeID = placeID;
        this.pathFoundListener = pathFoundListener;
    }

    @Override
    protected void onProgressUpdate(Void... values) {
        super.onProgressUpdate(values);
        Log.d(TAG, "SearchRelevant: onProgressUpdate: ");
    }

    @Override
    protected StringBuilder doInBackground(Void... voids) {
        Log.d(TAG, "SearchRelevant: doInBackground: ");
        return placeDetail(placeID);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        Log.d(TAG, "SearchRelevant: onPreExecute: ");
    }

    @Override
    protected void onPostExecute(StringBuilder strings) {
        Log.d(TAG, "SearchRelevant: onPostExecute: ");
        if (strings != null) {

            try {

                // Create a JSON object hierarchy from the results
                JSONObject jsonObj = new JSONObject(strings.toString());

                JSONObject resultJsonObject = jsonObj.getJSONObject("result");
                JSONObject geometryJsonObject = resultJsonObject.getJSONObject("geometry");
                JSONObject locationJsonObject = geometryJsonObject.getJSONObject("location");
                double lat = locationJsonObject.getDouble("lat");
                double lon = locationJsonObject.getDouble("lng");
                String firstPhoto = "";
                if (resultJsonObject.has("photos")) {

                    Log.d(TAG, "onPostExecute: photos: " + resultJsonObject.getJSONArray("photos"));
                    JSONArray photos = resultJsonObject.getJSONArray("photos");
                    firstPhoto = photos.getJSONObject(0).getString("photo_reference");
                }
                Log.d(TAG, "onPostExecute: pathFoundListener: " + pathFoundListener);
                if (pathFoundListener != null) {
                    pathFoundListener.onPathsFound(new LatLng(lat, lon), firstPhoto);
                }

            } catch (JSONException e) {

                Log.d(TAG, "SearchRelevant: onPostExecute: JSONException: " + e.getMessage());
            }
        }
    }

    public StringBuilder placeDetail(String placeID) {
        Log.d(TAG, "SearchRelevant: placeDetail: ");
        HttpURLConnection conn = null;
        StringBuilder jsonResults = new StringBuilder();
        try {
            Log.d(TAG, "placeDetail: try: ");
            StringBuilder sb = new StringBuilder(PLACEDetail_API_BASE + OUT_JSON);
            sb.append(KEY);


            sb.append("&placeid=" + placeID);

            URL url = new URL(sb.toString());
            System.out.println("PLACE DETAIL URL: " + url);
            conn = (HttpURLConnection) url.openConnection();
            InputStreamReader in = new InputStreamReader(conn.getInputStream());

            // Load the results into a StringBuilder
            int read;
            char[] buff = new char[1024];
            while ((read = in.read(buff)) != -1) {
                jsonResults.append(buff, 0, read);
            }
        } catch (MalformedURLException e) {
            Log.d(TAG, "SearchRelevant: placeDetail: MalformedURLException: " + e.getMessage());
            return null;
        } catch (IOException e) {
            Log.d(TAG, "SearchRelevant: placeDetail: IOException: " + e.getMessage());
            return null;
        } finally {
            Log.d(TAG, "SearchRelevant: placeDetail: finally: ");
            if (conn != null) {
                conn.disconnect();
            }
        }
        return jsonResults;
    }
}
