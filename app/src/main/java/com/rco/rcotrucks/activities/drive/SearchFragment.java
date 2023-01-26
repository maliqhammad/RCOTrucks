package com.rco.rcotrucks.activities.drive;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.maps.model.LatLng;
import com.orhanobut.hawk.Hawk;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.drive.adapter.IOnClickPlace;
import com.rco.rcotrucks.activities.drive.adapter.OnSetRoute;
import com.rco.rcotrucks.activities.drive.adapter.PlaceAutoCompleteAdapter;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.FIFOQueue;
import com.rco.rcotrucks.utils.MathUtils;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.utils.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import static com.rco.rcotrucks.utils.Constants.KEY_SEARCHED_LIST;

public class SearchFragment extends Fragment {

    private static final String TAG = SearchFragment.class.getSimpleName();
    private static final int SAVED_LIST_SIZE = 50;
    private static final int DISPALYED_LIST_SIZE = 5;

    //    private AutoCompleteTextView addressSearch;
    private EditText addressSearch;
    private RecyclerView addressRv;
    private TextView tvMoreHistory;
    private ImageView ivClearSearch, cancelSearch;
    private LatLng myLocation;
    private OnSetRoute iSetRoute;
    boolean isTablet=false;
    boolean isFragmentDestroyed = false;


    public static SearchFragment newInstance(LatLng myLocation) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putParcelable("MyLocation", myLocation);
        fragment.setArguments(args);
        return fragment;
    }

    public void setISetRoute(OnSetRoute onSetRoute) {
//        Log.d(TAG, "setISetRoute: ");
        iSetRoute = onSetRoute;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView: ");
        // Inflate the layout for this fragment
        isTablet = false;
        try {
            isTablet = getResources().getBoolean(R.bool.isTablet);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }


        if (isTablet) {
            return inflater.inflate(R.layout.fragment_search_tablet, container, false);
        } else {
            return inflater.inflate(R.layout.fragment_search, container, false);
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        try {
        Log.d(TAG, "onViewCreated: isFragmentDestroyed: " + isFragmentDestroyed);
        if (isFragmentDestroyed) {
            return;
        }
        super.onViewCreated(view, savedInstanceState);
        InputMethodManager imgr = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imgr.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);

        myLocation = getArguments().getParcelable("MyLocation");
        addressSearch = view.findViewById(R.id.et_search);
        addressRv = view.findViewById(R.id.address_rv);
        tvMoreHistory = view.findViewById(R.id.tv_more);
        cancelSearch = view.findViewById(R.id.cancelSearch);
        ivClearSearch = view.findViewById(R.id.iv_clear_search);
        addressSearch.requestFocus();

//            connectionMonitor = ((MainMenuActivity) getActivity()).connectionMonitor;
        initialize();
        setupRoute();

        loadLastCurrentSearch();

        tvMoreHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    UiUtils.closeKeyboard(addressSearch);
                    RecentSearchFragment recentSearchFragment = new RecentSearchFragment();
                    recentSearchFragment.setISetRoute(new OnSetRoute() {
                        @Override
                        public void setARoute(String placeId, String address) {
                            if (iSetRoute != null) {
                                iSetRoute.setARoute(placeId, address);
                            }
                        }

//                            @Override
//                            public void closeSearch() {
//                                if (iSetRoute != null) {
//                                    iSetRoute.closeSearch();
//                                }
//                            }
                    });

                    if (getActivity() != null && !getActivity().isFinishing())
                        getActivity().getSupportFragmentManager().beginTransaction().add(R.id.search_container, recentSearchFragment).commit();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        });

        if (!isTablet) {
            cancelSearch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addressSearch.setText("");
                    UiUtils.closeKeyboard(addressSearch);
                    getActivity().onBackPressed();
                }
            });
        }


        ivClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: ivClearSearch: ");
                addressSearch.setText("");
                fillPlaceList(new ArrayList<>());

            }
        });
//        } catch (Throwable throwable) {
//            throwable.printStackTrace();
//        }
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: isFragmentDestroyed: "+isFragmentDestroyed);
        UiUtils.showSoftKeyboard(getActivity());
    }

    void initialize() {
//        progressDialog = new ProgressDialog(getContext());
        isFragmentDestroyed = false;
    }


    private List<PlaceModel> placeModels;
    private PlaceAutoCompleteAdapter mAdapter;

    @SuppressLint("ClickableViewAccessibility")
    private void setupRoute() {
        try {
            addressSearch.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    Log.d(TAG, "addressSearch: onTouch: ");
                    try {
                        addressSearch.setCursorVisible(true);
                    } catch (Throwable throwable) {
                        throwable.printStackTrace();
                    }
                    return false;
                }
            });
            addressSearch.addTextChangedListener(textWatcher);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            Log.d(TAG, "setupRoute: throwable: " + throwable.getMessage());
        }
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int startNum, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "afterTextChanged: fillPlaceList: s: " + addressSearch.getText().toString());

            try {
//                if (connectionMonitor.isConnected()) {
                if (UiUtils.isOnline(getActivity(), getResources().getString(R.string.no_internet_connection))) {
                    if (s.toString().length() > 1) {
                        addressSearch.removeTextChangedListener(this);

                        fillPlaceList(new ArrayList<>());
                        new NearbySearchApi(s.toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                        Aug 29, 2022  -   Remove this method call from this place and added after we got the results from NearbySearchApi
//                        new QueryAutoComplete(s.toString()).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);


//                      Aug 29, 2022    -   After getting and adding all the results we should sort them as per distance
//                        Such that closest location should be at the top of the list

                    }
                }
//        May 23, 2022  -   isOnline function is enough to call if there is no internet then it will shown a message
//        "No Internet connection"
//                else {
//                    UiUtils.showToast(getActivity(), getString(R.string.error_lost_Connection));
//                    //UiUtils.showExclamationDialog(getActivity(), "Connection", getString(R.string.error_lost_Connection));
//                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    };

    void sortOutResults() {
        Log.d(TAG, "sortOutResults: placeModels: " + placeModels.size());

        if (placeModels != null) {

//            Arrays.sort(placeModels, new Comparator<PlaceModel>() {
//                @Override
//                public int compare(PlaceModel o1, PlaceModel o2) {
//                    if (o1 == null && o2 == null) {
//                        return 0;
//                    }
//                    if (o1 == null) {
//                        return 1;
//                    }
//                    if (o2 == null) {
//                        return -1;
//                    }
//                    return o1.getMiles().compareTo(o2.getMiles());
//                }});

            Collections.sort(placeModels, new Comparator<PlaceModel>() {
                public int compare(PlaceModel obj1, PlaceModel obj2) {
                    // ## Ascending order
                    if (obj1.getMiles() != null && obj2.getMiles() != null) {
                        return obj1.getMiles().compareToIgnoreCase(obj2.getMiles()); // To compare string values
                    }
                    // return Integer.valueOf(obj1.empId).compareTo(Integer.valueOf(obj2.empId)); // To compare integer values

                    // ## Descending order
//                     return obj2.getMiles().compareToIgnoreCase(obj1.getMiles()); // To compare string values
                    // return Integer.valueOf(obj2.empId).compareTo(Integer.valueOf(obj1.empId)); // To compare integer values

                    return -1;
                }
            });
        }

        Log.d(TAG, "sortOutResults: collection sorted: now notify adapter: ");
        mAdapter.notifyDataSetChanged();
    }

    private void saveLastInSearch(PlaceModel placeModel) {
        Log.d(TAG, "saveLastInSearch: placeId: placeModel: " + placeModel);
        FIFOQueue<PlaceModel> fifoQueue = new FIFOQueue<>(SAVED_LIST_SIZE);

        List<PlaceModel> placeModels = Hawk.get(KEY_SEARCHED_LIST, new ArrayList<>());
        fifoQueue.addAll(placeModels);

        StringBuilder placeIdsBuilder = new StringBuilder();
        for (PlaceModel model : placeModels) {
            placeIdsBuilder.append(model.getPlaceId()).append("-");
        }

        if (!placeIdsBuilder.toString().contains(placeModel.getPlaceId()) || fifoQueue.isEmpty()) {
            placeModel.setCreated_time(DateUtils.getCreatedDate());
            fifoQueue.add(placeModel);
        }

        Hawk.put(KEY_SEARCHED_LIST, fifoQueue);
    }

    private void loadLastCurrentSearch() {
        Log.d(TAG, "loadLastCurrentSearch: ");
        List<PlaceModel> placeModels = Hawk.get(KEY_SEARCHED_LIST, new ArrayList<>());
        Log.d(TAG, "loadLastCurrentSearch: placesModel: "+placeModels.size());
        if (placeModels.size() > 5) {
//            Log.d(TAG, "loadLastCurrentSearch: size: " + placeModels.size());
            placeModels = placeModels.subList(0, DISPALYED_LIST_SIZE);
            tvMoreHistory.setVisibility(View.VISIBLE);
        }

        Collections.reverse(placeModels);
        fillPlaceList(placeModels);
    }

    private void fillPlaceList(List<PlaceModel> list) {
//        Log.d(TAG, "fillPlaceList: list: size: " + list.size());

        if (list.size() > 0 && !addressSearch.getText().toString().isEmpty()) {
//            Log.d(TAG, "fillPlaceList: hide progress dialog");
        }

        placeModels = list;
        mAdapter = new PlaceAutoCompleteAdapter(placeModels, new IOnClickPlace() {
            @Override
            public void onGOClicked(PlaceModel placeModel) {
//                Log.d(TAG, "fillPlaceList: onGOClicked: " + placeModel);
                try {
                    saveLastInSearch(placeModel);

                    final String placeId = placeModel.getPlaceId();
//                    Log.d(TAG, "fillPlaceList: onGOClicked: placeId: " + placeId);
                    if (iSetRoute != null) {
                        iSetRoute.setARoute(placeId, placeModel.getAddress());

                        UiUtils.closeKeyboard(addressSearch);
                    }
                } catch (Throwable throwable) {
                    Log.d(TAG, "onGOClicked: throwable: " + throwable.getMessage());
                    throwable.printStackTrace();
                }
            }
        });

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), RecyclerView.VERTICAL, false);
        addressRv.setLayoutManager(linearLayoutManager);
        addressRv.setAdapter(mAdapter);
    }

    private class NearbySearchApi extends AsyncTask<Void, Void, StringBuilder> {
        private final String s;

        public NearbySearchApi(String s) {
//            Log.d(TAG, "NearbySearchApi: s: " + s);
            this.s = s;
        }

        @Override
        protected StringBuilder doInBackground(Void... voids) {
//            Log.d(TAG, "NearbySearchApi: doInBackground: ");
//            progressDialog.setMessage("Loading...");
//            progressDialog.show();
            return search(s);
        }

        @Override
        protected void onPostExecute(StringBuilder strings) {
//            Log.d(TAG, "NearbySearchApi: onPostExecute: strings: " + strings);
//            progressDialog.dismiss();
            try {
//                Nov 15, 2022  -   app crashed if we don't check that getActivity is null or not
                if (getActivity() != null && getActivity().isFinishing())
                    return;

                if (strings != null) {
                    // Create a JSON object hierarchy from the results
                    JSONObject jsonObj = new JSONObject(strings.toString());
                    JSONArray resultJsonArray = jsonObj.getJSONArray("results");
                    Log.d(TAG, "onPostExecute: resultJsonArray: " + resultJsonArray.length());


                    for (int i = 0; i < resultJsonArray.length(); i++) {
                        System.out.println(resultJsonArray.getJSONObject(i).getString("name"));
                        System.out.println(resultJsonArray.getJSONObject(i).getString("vicinity"));
                        System.out.println("============================================================");

                        PlaceModel placeModel = new PlaceModel();

                        placeModel.setAddress(resultJsonArray.getJSONObject(i).getString("name"));
                        placeModel.setTitle(resultJsonArray.getJSONObject(i).getString("vicinity"));

                        JSONObject geometryJsonObject = resultJsonArray.getJSONObject(i).getJSONObject("geometry");
                        JSONObject locationJsonObject = geometryJsonObject.getJSONObject("location");
                        double lat = locationJsonObject.getDouble("lat");
                        double lon = locationJsonObject.getDouble("lng");

                        double distanceMeters = Utils.calculateDistance(myLocation, new LatLng(lat, lon));
                        placeModel.setMeters(distanceMeters);

                        float miles = Utils.convertMetersToMiles(distanceMeters);
                        placeModel.setMiles(MathUtils.roundTo1DecimalCases("" + miles) + " miles");

//                        Log.d(TAG, "onPostExecute: distanceMeters: " + distanceMeters + " miles: " + miles + " name: " + resultJsonArray.getJSONObject(i).getString("name"));
                        placeModel.setPlaceId(resultJsonArray.getJSONObject(i).getString("place_id"));
                        if (placeModel.getPlaceId() != null)
                            placeModels.add(placeModel);
                    }


                    mAdapter.notifyDataSetChanged();

//                        Aug 29, 2022  -   call QueryAutoComplete after we got the results from NearbySearchApi
                    new QueryAutoComplete(s).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//                    Aug 29, 2022  -   I believe Don't need this function any more
//                    sortOutResults();
                }
            } catch (JSONException e) {
                Log.d(TAG, "onPostExecute: NearbySearchApi: JSONException: " + e.getMessage());
                Log.e("error", "" + e.getMessage());
            } catch (Throwable throwable) {
                Log.d(TAG, "onPostExecute: throwable: " + throwable.getMessage());
            }
        }

        String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        String TYPE_NEARCBYSEARCH = "/nearbysearch";
        String OUT_JSON = "/json";
        String KEY = "?key=" + getString(R.string.google_maps_key);

        public StringBuilder search(String input) {
//            Log.d(TAG, "search: NearbySearchApi: input: " + input);
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_NEARCBYSEARCH + OUT_JSON);
                sb.append(KEY);

                if (myLocation != null) {
                    sb.append("&location=" + myLocation.latitude + "," + myLocation.longitude);
                }

                sb.append("&components=country:usa");
                //sb.append("&radius=9999");
                sb.append("&rankby=distance");

                sb.append("&query=" + URLEncoder.encode(input, "utf8"));
                sb.append("&name=" + URLEncoder.encode(input, "utf8"));

                URL url = new URL(sb.toString());
                System.out.println("URL: " + url);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.d(TAG, "search: MalformedURLException: " + e.getMessage());
                return null;
            } catch (IOException e) {
                Log.d(TAG, "search: IOException: " + e.getMessage());
                return null;
            } finally {
                Log.d(TAG, "search: ");
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return jsonResults;
        }
    }

    private class QueryAutoComplete extends AsyncTask<Void, Void, StringBuilder> {
        private final String s;

        public QueryAutoComplete(String s) {
//            Log.d(TAG, "QueryAutoComplete: ");
            this.s = s;
        }

        @Override
        protected StringBuilder doInBackground(Void... voids) {
//            Log.d(TAG, "QueryAutoComplete: doInBackground: ");
//            progressDialog.setMessage("Loading...");
//            progressDialog.show();
            return autocomplete(s);
        }

        @Override
        protected void onPostExecute(StringBuilder strings) {
//            Log.d(TAG, "QueryAutoComplete: onPostExecute: strings: " + strings);

            Log.d(TAG, "onPostExecute: isFragmentDestroyed: " + isFragmentDestroyed);
            if (isFragmentDestroyed) {
                return;
            }
            if (strings != null) {
                try {
//                    progressDialog.dismiss();
                    if (placeModels == null)
                        placeModels = new ArrayList<>();
                    // Create a JSON object hierarchy from the results
                    JSONObject jsonObj = new JSONObject(strings.toString());
                    JSONArray resultJsonArray = jsonObj.getJSONArray("predictions");


                    for (int i = 0; i < resultJsonArray.length(); i++) {

                        PlaceModel placeModel = new PlaceModel();

                        JSONObject placeJsonObject = resultJsonArray.getJSONObject(i);
                        if (placeJsonObject.has("place_id")) {

                            String placeId = resultJsonArray.getJSONObject(i).getString("place_id");
                            placeModel.setPlaceId(placeId);
                        }
                        if (placeJsonObject.has("description")) {

                            String description = placeJsonObject.getString("description");
//                            Log.d(TAG, "onPostExecute: description: " + description);
                            placeModel.setTitle(description);
                        }
                        if (placeJsonObject.has("structured_formatting")) {
                            JSONObject structuredFormatting = resultJsonArray.getJSONObject(i).getJSONObject("structured_formatting");
                            if (structuredFormatting.has("main_text")) {

                                String mainText = structuredFormatting.getString("main_text");
                                placeModel.setAddress(mainText);
                            }
                        }

//                        Aug 11, 2022  -   The location coordinates for nearby places is coming so don't need to add these
//                        because they are far places as per distances
                        if (placeModel.getPlaceId() != null)
                            placeModels.add(placeModel);
                    }
                    mAdapter.notifyDataSetChanged();

                } catch (JSONException e) {
                    Log.e("error", "" + e.getMessage());
                    Log.d(TAG, "QueryAutoComplete: onPostExecute: JSONException: " + e.getMessage());
                }
            }

            addressSearch.addTextChangedListener(textWatcher);
        }


        String PLACES_API_BASE = "https://maps.googleapis.com/maps/api/place";
        String TYPE_NEARCBYSEARCH = "/queryautocomplete";
        String OUT_JSON = "/json";
        String KEY = "?key=" + getString(R.string.google_maps_key);

        public StringBuilder autocomplete(String input) {
            Log.d(TAG, "QueryAutoComplete: autocomplete: ");
            HttpURLConnection conn = null;
            StringBuilder jsonResults = new StringBuilder();
            try {
                StringBuilder sb = new StringBuilder(PLACES_API_BASE + TYPE_NEARCBYSEARCH + OUT_JSON);
                sb.append(KEY);

                if (myLocation != null) {
                    sb.append("&location=" + myLocation.latitude + "," + myLocation.longitude);
                }

                sb.append("&components=country:usa");
                sb.append("&types=address");

                double radius = 100;
                if (input.length() > 4)
                    radius = Math.pow(10, Math.min(input.length() - 2, 6));

                sb.append("&radius=" + radius);

                sb.append("&input=" + URLEncoder.encode(input, "utf8"));

                URL url = new URL(sb.toString());
                System.out.println("URL: " + url);
                conn = (HttpURLConnection) url.openConnection();
                InputStreamReader in = new InputStreamReader(conn.getInputStream());

                // Load the results into a StringBuilder
                int read;
                char[] buff = new char[1024];
                while ((read = in.read(buff)) != -1) {
                    jsonResults.append(buff, 0, read);
                }
            } catch (MalformedURLException e) {
                Log.d(TAG, "QueryAutoComplete: autocomplete: MalformedURLException: " + e.getMessage());
                return null;
            } catch (IOException e) {
                Log.d(TAG, "QueryAutoComplete: autocomplete: IOException: " + e.getMessage());
                return null;
            } finally {
                Log.d(TAG, "QueryAutoComplete: autocomplete: finally: con: " + conn);
                if (conn != null) {
                    conn.disconnect();
                }
            }
            return jsonResults;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isFragmentDestroyed = true;
        Log.d(TAG, "onDestroy: isFragmentDestroyed: " + isFragmentDestroyed);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        isFragmentDestroyed = true;
        Log.d(TAG, "onDestroyView: isFragmentDestroyed: " + isFragmentDestroyed);
    }
}