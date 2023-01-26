package com.rco.rcotrucks.activities.truck;

import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.drive.MapFragment;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.PairList;
import com.rco.rcotrucks.businesslogic.UiRules;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.UiUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class TruckFragment extends Fragment implements OnMapReadyCallback, RadioGroup.OnCheckedChangeListener {

    private static final String TAG = TruckFragment.class.getSimpleName();
    private FrameLayout mapContainer;
    private MapFragment mapFragment;
    private GoogleMap googleMap;
    private RadioGroup btnRadioGroup;
    protected RadioButton btnMap;
    protected BusinessRules rules = BusinessRules.instance();
    private TextView tvSpeed;
    private final static int speedUpdateDelay = 1000 * 30;
    Handler mSpeedHandler = new Handler();
    Runnable mSpeedTask = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "run: ");
            TruckFragment.UpdateSpeedView speedView = new TruckFragment.UpdateSpeedView();
            speedView.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            mSpeedHandler.postDelayed(mSpeedTask, speedUpdateDelay);
        }
    };

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        return inflater.inflate(R.layout.fragment_truck, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");
        intView(view);
    }

    private void intView(View view) {
        Log.d(TAG, "intView: ");
        ((MainMenuActivity) getActivity()).setActionBarTitle(getString(R.string.trucks_title));
        ((MainMenuActivity) getActivity()).hideMoreIcon();
        btnRadioGroup = view.findViewById(R.id.btn_radiogroup);
        mapContainer = view.findViewById(R.id.map_container);
        tvSpeed = view.findViewById(R.id.tv_speed);
        btnMap = view.findViewById(R.id.btn_map);

        loadMapView();
    }

    void startRepeatingTask() {
        Log.d(TAG, "startRepeatingTask: ");
        mSpeedTask.run();
    }


    //region Main container
    protected void loadMapView() {
        Log.d(TAG, "loadMapView: ");
        try {

            mapContainer.removeAllViews();

            mapFragment = new MapFragment();

            mapFragment.getMapAsync(this);
            getChildFragmentManager().beginTransaction().replace(R.id.map_container, mapFragment).commit();

        } catch (Throwable throwable) {
            UiUtils.showExclamationDialog(getActivity(), getString(R.string.error_map_connectivity_title), getString(R.string.error_map_connectivity),
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
        }
    }

    @Override
    public void onMapReady(final GoogleMap map) {
        Log.d(TAG, "onMapReady: ");
        try {

            mapFragment.onMapReady(map);
            googleMap = map;
            btnRadioGroup.setOnCheckedChangeListener(this);
            btnMap.setChecked(true);

            startRepeatingTask();

        } catch (Throwable throwable) {
            UiRules.showExclamationDataLoadErrorDialog(getActivity(), "Map", throwable);
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {
        switch (checkedId) {
            case R.id.btn_map:
                googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                break;
            case R.id.btn_hybrid:
                googleMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                break;
            case R.id.btn_sat:
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                break;

        }
    }

    public class UpdateSpeedView extends AsyncTask<String, String, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "onPreExecute: ");
        }

        @Override
        protected String doInBackground(String... params) {
            String response = "";
            Log.d(TAG, "doInBackground: ");
            try {
                Log.d(TAG, "doInBackground: response: " + response);
                response = rules.getTruckSpeedAndLocation();
            } catch (Exception e) {
                Log.d(TAG, "doInBackground: exception: " + e.getMessage());
                e.printStackTrace();
            }

            return response;
        }

        @Override
        protected void onPostExecute(String response) {
            Log.d(TAG, "onPostExecute: ");
            if (response != null) {
                JSONArray jsonArray = null;
                try {
                    jsonArray = new JSONArray(response);

                    JSONObject o = jsonArray.getJSONObject(0);
                    PairList fields = Rms.parseJsonCodingFields(o);

                    String speed = fields.getValue("Speed");
                    String truckNumber = fields.getValue("Truck Number");
                    String longitude = fields.getValue("Longitude");
                    String latitude = fields.getValue("Latitude");
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(latitude), Double.valueOf(longitude)), 14.0f));
                    googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(Double.valueOf(latitude), Double.valueOf(longitude)))
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_pol_truck2))
                            .title("Truck")
                            .snippet("Number: " + truckNumber + "\n" + "Speed :" + speed));

                    googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                        @Override
                        public View getInfoWindow(Marker arg0) {
                            return null;
                        }

                        @Override
                        public View getInfoContents(Marker marker) {

                            LinearLayout info = new LinearLayout(requireActivity());
                            info.setOrientation(LinearLayout.VERTICAL);

                            TextView title = new TextView(requireActivity());
                            title.setTextColor(Color.BLACK);
                            title.setGravity(Gravity.CENTER);
                            title.setTypeface(null, Typeface.BOLD);
                            title.setText(marker.getTitle());

                            TextView snippet = new TextView(requireActivity());
                            snippet.setTextColor(Color.GRAY);
                            snippet.setText(marker.getSnippet());

                            info.addView(title);
                            info.addView(snippet);

                            return info;
                        }
                    });
                    tvSpeed.setVisibility(View.VISIBLE);
                    Log.d(TAG, "onPostExecute: currentSpeed: tvSpeed: if: isVisible: " + tvSpeed.getVisibility());
                    tvSpeed.setText("" + speed);
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            } else {
                tvSpeed.setVisibility(View.GONE);
                Log.d(TAG, "onPostExecute: currentSpeed: tvSpeed: else: isVisible: " + tvSpeed.getVisibility());
            }
        }
    }
}
