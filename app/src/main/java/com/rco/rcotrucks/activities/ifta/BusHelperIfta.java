package com.rco.rcotrucks.activities.ifta;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteStatement;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.util.ArrayMap;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.maps.model.LatLng;
import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecRepairWork;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecRepairWorkRecTable;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordCommonHelper;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordCommonHelperTableRec;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RmsRecCommon;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RmsRecTableRec;
import com.rco.rcotrucks.utils.DatabaseHelper;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.HttpClient;
import com.rco.rcotrucks.utils.StringUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
  *
 * Before or after copying the exported geojson file to assets/ifta_assets in this app,
 * if you rename the file extension to ".json" instead of ".geojson",
 * the structure pane will display the file contents hierarchically.
 * As of 11/3/2021, all the android maps are in: Randy's desktop machine, D:\Data\RCO\Doc\Trucking\IFTA\GeoData\StateBoundaries\cb_2018_us_state_5m.
 * The QGIS project file is: D:\Data\RCO\Doc\Trucking\IFTA\GeoData\BoundarisQgis_3.18.qgz
 *
 * Maps were created with QGIS3 application.
 *
 * To export a Shapefile (.shp) to JSON lat/lon data:
 *
 * 1. Start QGIS
 * 2. Use Browser pane to find the shapefile and
 * 3. In the Layers pane, right click on layer to export, select "Save Features As".
 *    A "Save Vector Layer As" dialog box opens.
 * 4. - In the "Save Vector Layer As" dialog box, make up a filename, (and layer name if ungrayed?).
 *    - For "CRS", choose "Default CRS: EPSG:4326 - WGS 84" to get Lat/Lon coordinates.
 *    - For "Geometry" probably "Automatic" is okay, or choose "Polygon".  Actually "Point" and "Line String" looks similar to "Polygon" in export data, so Automatic prob good.
 *    - I didn't check "Force multi-type" or "Include z-dimenson.
 *    - Other fields were left at defaults: "COORDINATE_PRECISION" "15" - Thats the number of places after decimal point. Probably only need 4 or 5?
 *    - Don't know what RFC7946 is, left it at "No".  Setting it to "Yes" actually removes one layer of array brackets in the data -- seems better.
 *    - Setting "WRITE_BBOX" to "yes" gives you the bounding box of coordinates -- pretty useful.
 *    - Probably want to uncheck "Add saved file to map", but useful to see what you did.
 * 5. Click "OK" button to convert.
 *
 *  * ---------------------
 *  * * Geo Data Notes: best layer export format using QGIS3 for using geojson data in this app is:
 *  *  * Format: GeoJSON
 *  *  * CRS: EPSG:4326 (others may be okay too)
 *  *  * Geometry: Polygon with Force multi-type
 *  *  * Layer Options:
 *  *  * Coordinate Precision: 6 should be okay for production, maybe 9 for mini-maps
 *  *  * RFC7946 NO
 *  *  * WRITE_BBOX YES
 * ---------------------
 * To resize a map in QGIS to make, for example, a test driving or walking map: select all the features (selection tool or open attribute table and select all), click edit button, click somewhere on map, then drag mouse to resize map.  Overlaying the "OpenStreetMap" is useful to for positioning and resizing for a particular region.
 * -----------------
 *
 * To simplify a Shapefile -- look under Vector | Geometry Tools | Simplify.  Then adjust "Tolerance" meters -- 100 meters seems maybe okay.  For 2011 Canada data, use snap-to-grid
 * 	to avoid gaps, and .02 deg might be okay.
 *
 * ---------------------
 * * Geo Data Notes: best layer export format using QGIS3 for using geojson data in this app is:
 *  * Format: GeoJSON
 *  * CRS: EPSG:4326 (others may be okay too)
 *  * Geometry: Polygon with Force multi-type
 *  * Layer Options:
 *  * Coordinate Precision: 6 should be okay for production, maybe 9 for mini-maps
 *  * RFC7946 NO
 *  * WRITE_BBOX YES
 * ----------------------------------
 */
public class BusHelperIfta extends RecordCommonHelperTableRec {
    public static String TAG = "BusHelperIfta";
    public static String KEY_IFTA_SETTINGS = "ifta_params";
    public static int EVENT_NONE = 0;
    public static int EVENT_ENTER_NON_IFTA_ROAD = 1;
    public static int EVENT_EXIT_NON_IFTA_ROAD = 2;
    public static final String TABLE_IFTA_EVENT = "iftaevent";
    public static final String IFTA_EVENT_STATUS_ACTIVE = "active";
    public static final String IFTA_EVENT_STATUS_FINAL = "final";
    public static final String NON_IFTA_JUR_ABBREV = "XX";
    public static final long IFTA_CHECK_PERIOD_MILLIS = 60500;
    public static final long IFTA_CHECK_PERIOD_MAX_ELAPSED_ALERT = 65000;
    public static final String USER_STATUS_DRIVING = "Driving";

    private static String[][] arStateProvAbbrev = null;  // Todo: add minLat, maxLat, minLon, maxLon elements.  Or use separate object.
    private static List<Jurisdiction> listJur;
    private static Map<String, Jurisdiction> mapJurs;
    private static BusHelperIfta rulesIfta;
    private int iDebugCount;
    private static int isIftaTaxExempt;
    private static final Object isIftaTaxExemptSync = new Object();
    private SimpleDateFormat dfmtDateTimeIsoZ = new SimpleDateFormat(DateUtils.FORMAT_ISO_SSS_Z); // not thread safe.
    private DateUtils.IDateConverterParser dateConverterToLocal = new DateUtils.DateConverterParser(DateUtils.FROM_DATE_TIME_FORMAT_LIST,
            DateUtils.FORMAT_DATE_MM_DD_YYYY, DateUtils.FORMAT_DATE_MM_DD_YYYY_HH_MM_AMPM);

    public static Lock lockIftaProcessing = new ReentrantLock();

    public BusHelperIfta(DatabaseHelper db) {
        super(db, IftaEvent.TABLE_NAME);
    }

    /**
     * Caution - use this carefully, not thread safe for SQL statement operations.  Use
     * a different BusHelperIfta object per thread.  Todo: checkout, checkin instance as possible optimization.
     *
     * @return
     */
    public static synchronized BusHelperIfta instance() {
        if (rulesIfta == null) rulesIfta = new BusHelperIfta(RecordRulesHelper.getDb());
        return rulesIfta;
    }

    public static String[][] getArStateProvAbbrev() {
        return arStateProvAbbrev;
    }

    public static List<Jurisdiction> getListJur() {
        return listJur;
    }

    public static Map<String, Jurisdiction> getMapJurs() {
        return mapJurs;
    }



    //

    /**
     * String pathnameCanadaData = "D:\\Data\\RCO\\Doc\\Trucking\\IFTA\\StateBoundaries\\canada_provinces\\canada_provinces\\canada_provinces_json.json";
     *
     * @param ctx
     */

    public static void initStateProvGeoData(Context ctx) {
        String strThis = "initStateProvGeoData(), ";
        initStateProvAbbrev(ctx);
        listJur = null;

        User user = BusinessRules.instance().getAuthenticatedUser();
        String loginLc = user.getLogin().toLowerCase();
        Log.d(TAG, strThis + "Start. loginLc=" + loginLc);

//        Aug, 01, 2022 -   Added another login roya2 here
        if (loginLc.startsWith("testroy") || loginLc.equals("roya") || loginLc.equals("roya2")) {
            Log.d(TAG, strThis + "Case: testroy geodata.");
            addJurisdictionGeoDataJson(ctx, "united_states_of_santa_monica_bbox_norfc_forcemulti.geojson",
                    "NAME", null);
        } else if (loginLc.startsWith("testrandy") || loginLc.startsWith("randya")) {
            Log.d(TAG, strThis + "Case: testrandy, randya united_states_of_montrose_bbox_norfc_forcemulti.geojson geodata.");
            addJurisdictionGeoDataJson(ctx, "united_states_of_montrose_bbox_norfc_forcemulti.geojson",
                    "NAME", null);
        } else if (loginLc.startsWith("testrandyw") || loginLc.startsWith("randyw")) {
            Log.d(TAG, strThis + "Case: testrandyw, randyw united_states_of_montrose_walking_bbox_norfc_forcemulti.json geodata.");
            addJurisdictionGeoDataJson(ctx, "united_states_of_montrose_walking_bbox_norfc_forcemulti.json",
                    "NAME", null);
        } else {
            Log.d(TAG, strThis + "Case: production geodata.");
            addJurisdictionGeoDataJson(ctx, "cb_2018_us_state_5m_genr_dougreduct_.01_30%_multipoly_cleanshore_geojson.json",
                    "NAME", null);

            addJurisdictionGeoDataJson(ctx, "gpr_000a11a_e_genr_dougreduct_.01_30%_cleanshore.json",
                    "PRENAME", null);
        }

        initMapJurisdictions();
    }

    public static void initStateProvAbbrev(Context ctx) {
        String strThis = "initIftaGeoData(), ";
        InputStream inputStream = null;
        arStateProvAbbrev = new String[62][4];

        try {
            Resources resources = ctx.getResources();
            AssetManager assetManager = resources.getAssets();
            String abbrevAssetPath = "ifta_assets/us-state_can-prov_abbrev.json";
            inputStream = assetManager.open(abbrevAssetPath);
            String strJson = HttpClient.convertStreamToStringNoClose(inputStream);
            inputStream.close();

//            JSONObject json = new JSONObject(strJson);
            JSONArray jsonArray = new JSONArray(strJson);

            int ix = 0;

            for (int icountry = 0; icountry < jsonArray.length(); icountry++) {
                JSONObject jsonCountry = jsonArray.getJSONObject(icountry);
                String country = jsonCountry.getString("name");

                if ("United States".equals(country)) {
                    JSONArray jsonArrayStates = jsonCountry.getJSONArray("states");
                    for (int istate = 0; istate < jsonArrayStates.length(); istate++) {
                        JSONObject jsonState = jsonArrayStates.getJSONObject(istate);
                        String abbrev = jsonState.getString("abbreviation");

                        arStateProvAbbrev[ix][0] = abbrev;
                        arStateProvAbbrev[ix][1] = jsonState.getString("name");
                        arStateProvAbbrev[ix][2] = "US";
                        if ("AK,HI,DC".contains(abbrev)) arStateProvAbbrev[ix][3] = "N";
                        else arStateProvAbbrev[ix][3] = "I";
                        ix++;
                    }
                } else if ("Canada".equals(country)) {
                    JSONArray jsonArrayStates = jsonCountry.getJSONArray("states");
                    for (int istate = 0; istate < jsonArrayStates.length(); istate++) {
                        JSONObject jsonState = jsonArrayStates.getJSONObject(istate);
                        String abbrev = jsonState.getString("abbreviation");

                        arStateProvAbbrev[ix][0] = abbrev;
                        arStateProvAbbrev[ix][1] = jsonState.getString("name");
                        arStateProvAbbrev[ix][2] = "CA";
                        if ("YT,NT,NU".contains(abbrev)) arStateProvAbbrev[ix][3] = "N";
                        else arStateProvAbbrev[ix][3] = "I";
                        ix++;
                    }
                }
            }

            Log.d(TAG, strThis + "arStateProvAbbrev" + StringUtils.dumpArray(arStateProvAbbrev));

        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static String[] lookupJurAbbrev(String jurisdictionName) {
        String[] abbrev = null;

        if (arStateProvAbbrev != null && arStateProvAbbrev.length > 0) {
            for (String[] arJur : arStateProvAbbrev) {
                if (arJur[1].equals(jurisdictionName)) {
//                    abbrev = arJur[0];
                    abbrev = arJur;
                    break;
                }
            }
        }

        return abbrev;
    }

    public static Jurisdiction findJurisdiction(double lat, double lonx, List<Jurisdiction> listJur, Jurisdiction currentJur) {
        String strThis = "findJurisdiction(), ";
        final boolean IS_LOG = false;
        final boolean IS_LOG_RESULT = true;

        long Lstart = System.currentTimeMillis();
        long Ldt = 0;

        if (IS_LOG_RESULT) Log.d(TAG, strThis + "Start. Ldt=" + Ldt + ", lat=" + lat + ", lonx=" + lonx + ", listJur.size()=" + listJur.size()
                + ", currentJur=" + currentJur);

        Jurisdiction jur = currentJur;
        boolean isInJur = false;
        // Check if we are still in the current jurisdiction;
        if (currentJur != null) {
            isInJur = isInJurisdiction(lat, lonx, jur);
            Ldt = System.currentTimeMillis() - Lstart;
            if (IS_LOG) Log.d(TAG, strThis + "Ldt=" + Ldt + ", after check jur: " + jur.name + ", isInJur=" + isInJur);
        }

        if (!isInJur) {
            for (Jurisdiction ju : listJur) {
                if (currentJur != null && currentJur.equals(ju)) continue;
                if (IS_LOG) Log.d(TAG, strThis + "ju=" + ju);
                isInJur = isInJurisdiction(lat, lonx, ju);
                Ldt = System.currentTimeMillis() - Lstart;
//                Log.d(TAG, strThis + "Ldt=" + Ldt + ", after check jur: " + ju.name + ", isInJur=" + isInJur);

                if (isInJur) {
                    jur = ju;
                    if (IS_LOG) Log.d(TAG, strThis + "Case: found containing jurisdiction, will break from jur loop.  Ldt=" + Ldt + ", after check jur: " + ju.name + ", isInJur=" + isInJur);
                    break;
                }
            }
        }

        Ldt = System.currentTimeMillis() - Lstart;

        if (IS_LOG_RESULT) Log.d(TAG, strThis + "End. Ldt=" + Ldt + ", jur: " + jur + ", isInJur=" + isInJur
                + ", lat=" + lat + ", lonx=" + lonx + ", listJur.size()=" + listJur.size()
                + ", currentJur=" + currentJur);

        return jur;
    }

    public static boolean isInJurisdiction(double lat, double lonx, Jurisdiction jur) {
        final boolean IS_LOG = false;
        final boolean IS_LOG_RESULT = false;
        String strThis = "isInJurisdiction(), ";
        if (IS_LOG) Log.d(TAG, strThis + "Start. lat=" + lat + ", lonx=" + lonx + ", jur=" + jur);
        // Check if in bounding rectangle, quick check to exclude.  Probably will be true most of time
        // for current jurisdiction even if crossing into other jurisdiction, so maybe not useful for that case.
        boolean isInJur = false;

        if (lat >= jur.latMin() && lat <= jur.latMax()
                && lonx >= jur.lonMinX() && lonx <= jur.lonMaxX()) {
            if (IS_LOG) Log.d(TAG, strThis + "Case: (lat,lonx)=(" + lat + "," + lonx + ","
                    + ") is in bounding box of jur:" + jur + ", proceeding to check polygons.");

            // Thorough check if still in current jurisdiction;

            for (int ipoly = 0; ipoly < jur.lonCoordinatesX.length; ipoly++) {
                int iRingCount = jur.lonCoordinatesX[ipoly].length;

               if (IS_LOG) Log.d(TAG, strThis + "ipoly=" + ipoly + ", ring count: " + iRingCount);

                // Typically we will just have a single polygon for a jurisdiction if we cleaned the data well, but we
                // will allow for multiple (they would typically represent islands off a coast or in a large lake) so
                // we could choose that model eventually.  Usually, we try to clean the data by extending the mainland
                // boundary around the islands and then deleting the island data.
                boolean isInPoly = false;

                for (int jring = 0; jring < iRingCount; jring++) {
                    if (IS_LOG) Log.d(TAG, strThis + "ipoly=" + ipoly + ", jring=" + jring + ", vertexCount: " + jur.latCoordinates[ipoly][jring].length);
                    // Linear rings represent polygons that may have  holes, where the first linear ring is the exterior
                    // polygon and subsequent rings represent holes whose interiors are not considered part of the polygon.
                    // A linear ring is what we think of as a simple polygon represented by a list of vertices (points)
                    // with the first and last vertex on the list being the same point in space.  We assume our linear rings
                    // are "simple", meaning the line segments between vertices never cross each other.
                    boolean isIn = inPolygon(lat, lonx, jur.latCoordinates[ipoly][jring],
                            jur.lonCoordinatesX[ipoly][jring]);

                    if (jring == 0) {
                        // First linear ring is exterior ring of polygon with holes.  If not in that, we're done.
                        isInPoly = isIn;
                        if (IS_LOG) Log.d(TAG, strThis + "Case: ipoly=" + ipoly + ", jring== 0, after inPolygon() call, isIn=" + isIn + ", isInPoly=" + isInPoly);
                    } else {
                        // Subsequent linear rings represent holes.  If we are in a hole, we are not in the polygon and we're done.
                        // Typically we will not have holes, but allowing for that case.  We will never execute this case if
                        // our test point is not in the exterior polygon so we don't bother anding or oring or value.  "!isIn"
                        // must be true for all holes, we break if that is not true for any one, meaning our test point is in a hole.
                        // We assume holes do not contain other non-holes, so we can terminate if we are in a hole.  We could allow
                        // for unlimited nesting of hole / non-hole probably using recursion and fancy data structures, but forget it.
                        isInPoly = !isIn;
                        if (IS_LOG) Log.d(TAG, strThis + "Case: ipoly=" + ipoly + " jring=" + jring + " (> 0), after inPolygon() call, isIn=" + isIn + ", isInPoly=" + isInPoly);
                    }

                    if (!isInPoly) {
                        if (IS_LOG) Log.d(TAG, strThis + "Case: ipoly=" + ipoly + ", jring=" + jring + ", !isInPoly=" + !isInPoly
                                + " after inPolygon() call, break from loop because detected not in polygon.");
                        break;
                    }
                }

                if (isInPoly) {
                    if (IS_LOG) Log.d(TAG, strThis + "Case: ipoly=" + ipoly + ", isInPoly=" + isInPoly
                            + " after inPolygon() call, we found a containing polygon, break from polygon loop because we must be in the jurisdiction.");
                    isInJur = isInPoly;
                    break;
                }
            }
        } else if (IS_LOG) Log.d(TAG, strThis + "(lat,lonx)=(" + lat + "," + lonx + "" +
                ") is not in bounding box of jurisdiction, no further checking done.  jur:" + jur);

        if (IS_LOG_RESULT) Log.d(TAG, strThis + "End.  isInJur=" + isInJur + ", (lat,lonx)=(" + lat + "," + lonx + "" +
                ") jur:" + jur);

        return isInJur;
    }

    public static void addJurisdictionGeoDataJson(Context ctx, String geoJasonAssetRelativePath, String jurNameProperty, String ignoreJursdictions) {
        String strThis = "addJurisdictionGeoDataJson(), ";
        Log.d(TAG, strThis + "Start.");

        InputStream inputStream = null;

        try {
            Resources resources = ctx.getResources();
            AssetManager assetManager = resources.getAssets();
//            String abbrevAssetPath = "ifta_assets/canada_prov_2011_bbox_simp02snap.json"; // "ifta_assets/canada_prov_bbox.json";
            String abbrevAssetPath = "ifta_assets/" + geoJasonAssetRelativePath; // "ifta_assets/canada_prov_bbox.json";
            inputStream = assetManager.open(abbrevAssetPath);
            String strJson = HttpClient.convertStreamToStringNoClose(inputStream);
            inputStream.close();

            JSONObject jsonFeatureCollection = new JSONObject(strJson);
            JSONArray jaBboxCountry = jsonFeatureCollection.optJSONArray("bbox");
            JSONArray jaFeatures = jsonFeatureCollection.getJSONArray("features");

            if (listJur == null)
                listJur = new ArrayList<>(jaFeatures.length());

            int ix = 0;

            for (int ijur = 0; ijur < jaFeatures.length(); ijur++) {

                JSONObject jsonFeature = jaFeatures.getJSONObject(ijur);
                JSONObject jsonProperties = jsonFeature.getJSONObject("properties");
                String jurName = jsonProperties.getString(jurNameProperty);

//                if ("Nunavut".equals(jurName)) continue;
                if (ignoreJursdictions != null && ignoreJursdictions.contains(jurName)) {
                    Log.d(TAG, strThis + "Skipping ignored jurisdiction: " + jurName);
                }

                Jurisdiction jur = new Jurisdiction();
                listJur.add(jur);

                jur.name = jurName;
                JSONArray jaBbox = jsonFeature.getJSONArray("bbox");
                jur.setBbox(getDoubleArrayFromJson(jaBbox));
                jur.initMinMaxForCalc();

                //                Log.d(TAG, strThis + "ijur=" + ijur + ", jur=" + jur);

                JSONObject jsonGeometry = jsonFeature.getJSONObject("geometry");
                jur.geometryType = jsonGeometry.getString("type"); // type is "Polygon" or "MultiPolygon"

                Log.d(TAG, strThis + "ijur=" + ijur + ", jur=" + jur);

                JSONArray jaPolygons = null;
                JSONArray jaLinearRings = null;

                int iPolygonCount = 0;

                // A linear ring is basically what we think of as a simple polygon.  However,
                // a geojson polygon can be an "exterior" polygon with "interior" polygon "holes".
                // So a polygon consists of an array of linear rings, where the first linear ring
                // is always the "exterior" polygon.  For states and provinces, there seem to never
                // be "interior" rings, so the number of linear rings is always 1.  This means
                // all territory inside a state polygon belongs to the state.  An example of an
                // interior ring might be the Vatican, which is not part of Italy.  So Italy would
                // have an exterior ring and an interior ring.  There is a peculiar piece of Kentucky,
                // but I think it is not an interior ring, rather it is a separate polygon.  In fact,
                // islands are represented by separate geojson polygons, for example with Alaska.
                // We check if the type of a geometry is "MultiPolygon" vs "Polygon" because a geojson file
                // that is formatted to "RFC 7946" standards uses mixed Polygons and MultiPolygons for its
                // features.  A feature that is just "Polygon" has one less depth to its Coordinates array because
                // it does not have a dimension for polygons, it assumes just 1.  If we export from the
                // QGIS3 program and do not choose "RFC 7946" standard, every feature is forced to MultiPolygon,
                // which is more uniform.  However, in this code, we try to accommodate both types of geojson
                // file formats, so we check each feature for its "Type" and if "Polygon", we know to skip
                // the polygons dimension when parsing the JSON Coordinates array.

                if ("MultiPolygon".equals(jur.geometryType)) {
                    jaPolygons = jsonGeometry.getJSONArray("coordinates");
                    iPolygonCount = jaPolygons.length();
                    Log.d(TAG, strThis + "Case: MultiPolygon case, obtained jaPolygons array with length: " + jaPolygons.length() + ", jur:" + jur.name);
                } else {
                    iPolygonCount = 1;
                    jaLinearRings = jsonGeometry.getJSONArray("coordinates");
                    Log.d(TAG, strThis + "Case: Single Polygon case, obtained jaLinearRings array with length: " + jaLinearRings.length() + ", jur:" + jur.name);
                }

                // For simplicity, we will allow for MultiPolygons in our vertices arrays, even though
                // we may edit the geo data so that states and provinces are always a single polygon
                // by eliminating islands by extending the mainland state/province boundary around them
                // and deleting the island polygons using QGIS3 or similar GIS program.

                jur.latCoordinates = new double[iPolygonCount][][];
                jur.lonCoordinatesX = new double[iPolygonCount][][];
                Log.d(TAG, strThis + "ijur=" + ijur + ", jur.name=" + jur.name
                        + ", iPolygonCount=" + iPolygonCount);

                for (int iPolygon = 0; iPolygon < iPolygonCount; iPolygon++) {
                    if ("MultiPolygon".equals(jur.geometryType)) {
                        jaLinearRings = jaPolygons.getJSONArray(iPolygon);

                        Log.d(TAG, strThis + "Case: MultiPolygon, obtained jaLinearRings array with length: " + jaLinearRings.length() + " from polygon[" + iPolygon + "], jur:" + jur.name);

                    } else {
                        // Single polygon, there is no "polygons" JSON array to parse, go straight to the linear rings.
                        // We should have already obtained the jaLinearRings array from the first json array.
                    }

//                    if (jaLinearRings.length() != 1)
//                        throw new Exception("**** Design error, expecting jaLinearRings.length() of 1, but "
//                                + " found jaLinearRings.length()=" + jaLinearRings.length() + ", jaLinearRings=" + jaLinearRings
//                            + ".  States and provinces should never have interior regions that don't belong to them.");

                    int iLinearRingCount = jaLinearRings.length();
                    jur.lonCoordinatesX[iPolygon] = new double[iLinearRingCount][];
                    jur.latCoordinates[iPolygon] = new double[iLinearRingCount][];
                    Log.d(TAG, strThis + "ijur=" + ijur + ", jur.name=" + jur.name
                            + ", iPolygonCount=" + iPolygonCount + ", iLinearRingCount=" + iLinearRingCount);


                    for (int iLinearRing = 0; iLinearRing < iLinearRingCount; iLinearRing++) {
                        JSONArray jaVertices = jaLinearRings.getJSONArray(iLinearRing);

                        jur.lonCoordinatesX[iPolygon][iLinearRing] = new double[jaVertices.length()];
                        jur.latCoordinates[iPolygon][iLinearRing] = new double[jaVertices.length()];

                        Log.d(TAG, strThis + "ijur=" + ijur + ", jur:" + jur.name
                                + ", iPolygon=" + iPolygon
//                                + ", jaUnks.length()=" + jaUnks.length()
//                                + ", iUnk=" + iUnk
                                + ", jaVertices.length()=" + jaVertices.length());

                        for (int iVertex = 0; iVertex < jaVertices.length(); iVertex++) {
                            JSONArray jaVertex = jaVertices.getJSONArray(iVertex);
//                            jur.lonCoordinates[iPolygon][iUnk][iVertex] = jaVertex.getDouble(0);
//                            jur.latCoordinates[iPolygon][iUnk][iVertex] = jaVertex.getDouble(1);
                            double lonx = lx(jaVertex.getDouble(0));
                            double lat = jaVertex.getDouble(1);
                            jur.lonCoordinatesX[iPolygon][iLinearRing][iVertex] = lonx;
                            jur.latCoordinates[iPolygon][iLinearRing][iVertex] = lat;
                            if (lat < jur.latmin) jur.latmin = lat;
                            else if (lat > jur.latmax) jur.latmax = lat;
                            if (lonx < jur.lonminx) jur.lonminx = lonx;
                            else if (lonx > jur.lonmaxx) jur.lonmaxx = lonx;
                        }
                    }
//                    } // end jaUnks Loop
                }
            }

            Log.d(TAG, strThis + "--------------------------- End of jurisdiction polygon ingest ---------------------");

            Log.d(TAG, strThis + "listJur.size()=" + listJur.size());

            for (Jurisdiction jur : listJur) {
                Log.d(TAG, strThis + "Dumping jur: " + jur.name + ", " + jur.lonCoordinatesX.length + " polygons, jur={" + jur + "}");
                double lonMin = 9999., lonMax = -9999., latMin = 9999., latMax = -9999.;
                for (int ipoly = 0; ipoly < jur.lonCoordinatesX.length; ipoly++) {
//                    Log.d(TAG, strThis + jur.name + " polygon:" + ipoly + ", rings:" + jur.lonCoordinates[ipoly].length);
                    Log.d(TAG, strThis + jur.name + " polygon:" + ipoly + ", rings:" + jur.lonCoordinatesX[ipoly].length);

                    for (int jring = 0; jring < jur.lonCoordinatesX[ipoly].length; jring++) {
                        Log.d(TAG, strThis + jur.name + " polygon:" + ipoly + ", ring:" + jring
                                + ", vertices:" + jur.lonCoordinatesX[ipoly][jring].length);
                        if (true) {
                            for (int kvert = 0; kvert < jur.lonCoordinatesX[ipoly][jring].length; kvert++) {
                                double lon = jur.lonCoordinatesX[ipoly][jring][kvert];
                                double lat = jur.latCoordinates[ipoly][jring][kvert];
                                if (false)
                                    Log.d(TAG, strThis + jur.name + "  polygon[" + ipoly + "] ring[" + jring + "] vertex[" + kvert + "] (" + lon + "," + lat + ")");
                                if (lon < lonMin) lonMin = lon;
                                if (lon > lonMax) lonMax = lon;
                                if (lat < latMin) latMin = lat;
                                if (lat > latMax) latMax = lat;
                            }
                        }
                    }
                }
                Log.d(TAG, strThis + "Summary for jur=" + jur + ", lonMin=" + lonMin + ", latMin=" + latMin + ", lonMax=" + lonMax + ", latMax=" + latMax);
            }

            Log.d(TAG, strThis + "End.");
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void parseStateProvinceXml(Context ctx, String assetPathOptional) {
        String strThis = "parseStateProvinceXml(), ";
        String assetPath = assetPathOptional;
        if (assetPath == null)
            assetPath = "ifta_assets/canada_plist.xml";

        InputStream inputStream = null;
        InputStreamReader inReader = null;

        try {
            Resources resources = ctx.getResources();
            AssetManager assetManager = resources.getAssets();

            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            inputStream = assetManager.open(assetPath);
            inReader = new InputStreamReader(inputStream);

            xpp.setInput(inReader);

            int eventType = xpp.getEventType();

            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_DOCUMENT) {
                    System.out.println("Start document");
                } else if (eventType == XmlPullParser.START_TAG) {
                    System.out.println("Start tag " + xpp.getName());
                } else if (eventType == XmlPullParser.END_TAG) {
                    System.out.println("End tag " + xpp.getName());
                } else if (eventType == XmlPullParser.TEXT) {
                    System.out.println("Text " + xpp.getText());
                }
                eventType = xpp.next();
            }

            inReader.close();
            System.out.println("End document");
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (inReader != null) inReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static double[] getDoubleArrayFromJson(JSONArray jsonArray) throws JSONException {
        double[] arDbl = new double[jsonArray.length()];

        for (int i = 0; i < jsonArray.length(); i++) arDbl[i] = jsonArray.getDouble(i);

        return arDbl;
    }

    public static void initGeoCode(final Context ctx) {

        Thread task = new Thread() {

            @Override
            public void run() {
//                reverseGeoCode(activity);
                initStateProvGeoData(ctx);
            }
        };

        task.start();
    }

    public static void initMapJurisdictions() {
        if (mapJurs == null) mapJurs = new ArrayMap<>();

        for (Jurisdiction jur : listJur) {
            String[] abbrev = lookupJurAbbrev(jur.name);
            if (abbrev != null) {
                jur.abbrev = abbrev[0];
                if ("I".equals(abbrev[3])) jur.isIftaJurisdiction = true;
                else jur.isIftaJurisdiction = false;

                mapJurs.put(abbrev[0], jur);
            } else {
                Log.d(TAG, "initMapJurisdictions()  ***** Warning.  Jurisdiction: " + jur.name
                        + " was not found with lookupJurAbbrev()." +
                        " Possible missing state/province abbreviation data.");
            }
        }
    }

    public static Jurisdiction currentJur = null;

    public static void testGeoCode(final Activity activity, final double lat, final double lonx,
                                   final List<Jurisdiction> listJurs,
                                   final TextView textViewResults) {

        String strThis = "testGeoCode(), ";
        Log.d(TAG, strThis + "Start. lat=" + lat + ", lonx=" + lonx + ", listJurs.size()=" + listJurs.size()
                + ", textViewResults==null?" + (textViewResults == null));

//        AsyncTask task = new AsyncTask <Void, Void, Jurisdiction> () {
//            @Override
//            protected Jurisdiction doInBackground(Void... voids) {
//                Log.d(TAG, "testGeoCode() doInBackground() Start.  currentJur=" + currentJur);
//                Jurisdiction jur = findJurisdiction(lat, lonx, listJurs, currentJur);
//                Log.d(TAG, "testGeoCode() doInBackground() End.  currentJur=" + currentJur);
//                return jur;
//            }
//
//            @Override
//            protected void onPostExecute(Jurisdiction jurisdiction) {
////                super.onPostExecute(jurisdiction);
//                currentJur = jurisdiction;
//                textViewResults.setText(jurisdiction.toString());
//            }
//
//            @Override
//            protected void onProgressUpdate(Void... values) {
//                super.onProgressUpdate(values);
//            }
//        };

        TestClass task = new TestClass(lat, lonx, listJurs, textViewResults);

        Log.d(TAG, strThis + "About to call task.execute().");

        task.execute();

    }

    public static void testAddress(final Context ctx, final double lat, final double lon, final int maxResults) {

        Thread task = new Thread() {

            @Override
            public void run() {
                getAddresses(ctx, lat, lon, maxResults);
            }
        };
        LatLng latLng = new LatLng(34.0, -111.0);

        com.google.maps.android.PolyUtil polyUtil;

        task.start();
    }

    public static List<Address> getAddresses(Context ctx, double lat, double lon, int maxResults) {
        String strThis = "reverseGeoCode(), ";
        Log.d(TAG, strThis + "Start. lat=" + lat + ", lon=" + lon + ", maxResults=" + maxResults);
        List<Address> listAddresses = null;

        try {
            Geocoder geocoder = new Geocoder(ctx);

            listAddresses = geocoder.getFromLocation(lat, lon, 10);

            for (Address address : listAddresses) {
                Log.d(TAG, strThis + " " + address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, strThis + "End. lat=" + lat + ", lon=" + lon + ", maxResults=" + maxResults
                + ", listAddresses.size()=" + (listAddresses != null ? listAddresses.size() : "(NULL)"));

        return listAddresses;
    }

    public static void reverseGeoCode(Activity a) {
        String strThis = "reverseGeoCode(), ";
        Log.d(TAG, strThis + "Start.");

        try {
            Geocoder geocoder = new Geocoder(a);
            Location loc = BusinessRules.instance().getPhoneLastBestLocation(a);
            Log.d(TAG, strThis + ", loc=" + loc);


            List<Address> listAddresses = geocoder.getFromLocation(loc.getLatitude(), loc.getLongitude(), 10);

            for (Address address : listAddresses) {
                Log.d(TAG, strThis + " " + address);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d(TAG, strThis + "End.");
    }

    /**
     * Longitude "de-modulus", converting all longitudes to the range 0 to 360 instead of -180 to 180
     * for North American region calculations.
     *
     * @param lon
     * @return
     */
    public static final double lx(double lon) {
        if (lon > 0.0) lon = lon - 360.;

        return lon;
    }

    /**
     * Globals which should be set before calling this function:
     * <p>
     * int    polyCorners  =  how many corners the polygon has
     * float  polyX[]      =  horizontal coordinates of corners
     * float  polyY[]      =  vertical coordinates of corners
     * float  x, y         =  point to be tested
     * <p>
     * (Globals are used in this example for purposes of speed.  Change as
     * desired.)
     * <p>
     * The function will return YES if the point x,y is inside the polygon, or
     * NO if it is not.  If the point is exactly on the edge of the polygon,
     * then the function may return YES or NO.
     * <p>
     * Note that division by zero is avoided because the division is protected
     * by the "if" clause which surrounds it.
     * <p>
     * NSInteger j = polygon.count - 1;
     * BOOL inside = NO;
     * double lat = point.latitude;
     * double lonx = point.longitude;
     * <p>
     * for (NSInteger i = 0; i < polygon.count; i++) {
     * NSArray *arri = [polygon objectAtIndex:i];
     * double lati = [[arri objectAtIndex:1] doubleValue];
     * double loni = [[arri objectAtIndex:0] doubleValue];
     * NSArray *arrj = [polygon objectAtIndex:j];
     * double latj = [[arrj objectAtIndex:1] doubleValue];
     * double lonj = [[arrj objectAtIndex:0] doubleValue];
     * <p>
     * if (((lati < lat && latj >= lat)
     * ||   (latj < lat && lati >= lat))
     * &&  (loni<=lonx || lonj<=lonx)) {
     * inside^=(loni+(lat-lati)/(latj-lati)*(lonj-loni)<lonx);
     * }
     * j=i;
     * }
     * <p>
     * return inside;
     * <p>
     * }
     *
     * @return
     */
    public static boolean inPolygon(double lat, double lonx, double[] arLat, double[] arLonx) {
        int iPolyCorners = arLonx.length - 1; // Assume the polygon does not cross itself.

        int j = iPolyCorners - 1;
        boolean inside = false;
        for (int i = 0; i < iPolyCorners; i++) {
            double lati = arLat[i];
            double loni = arLonx[i];
            double latj = arLat[j];
            double lonj = arLonx[j];

            if (((lati < lat && latj >= lat)
                    || (latj < lat && lati >= lat))
                    && (loni <= lonx || lonj <= lonx)) {
                inside ^= (loni + (lat - lati) / (latj - lati) * (lonj - loni) < lonx);
            }

            j = i;
        }

        return inside;
    }


    /**
     * Globals which should be set before calling this function:
     * <p>
     * int    polyCorners  =  how many corners the polygon has
     * float  polyX[]      =  horizontal coordinates of corners
     * float  polyY[]      =  vertical coordinates of corners
     * float  x, y         =  point to be tested
     * <p>
     * (Globals are used in this example for purposes of speed.  Change as
     * desired.)
     * <p>
     * The function will return YES if the point x,y is inside the polygon, or
     * NO if it is not.  If the point is exactly on the edge of the polygon,
     * then the function may return YES or NO.
     * <p>
     * Note that division by zero is avoided because the division is protected
     * by the "if" clause which surrounds it.
     *
     * @param polyX
     * @param polyY
     * @param x
     * @param y
     * @param polyCorners
     * @return
     */
    public static boolean pointInPolygon(float[] polyX, float[] polyY, float x, float y, int polyCorners) {

        int i, j = polyCorners - 1;
        boolean oddNodes = false;

        for (i = 0; i < polyCorners; i++) {
            if ((polyY[i] < y && polyY[j] >= y
                    || polyY[j] < y && polyY[i] >= y)
                    && (polyX[i] <= x || polyX[j] <= x)) {
                oddNodes ^= (polyX[i] + (y - polyY[i]) / (polyY[j] - polyY[i]) * (polyX[j] - polyX[i]) < x);
            }
            j = i;
        }

        return oddNodes;
    }

    public static long LsystimeLastIftaCheck = 0;

    /**
     * Design: always have an "active" IFTA Event in progress which is finalized when an "IFTA Event" state change is detected.
     * When a state change is detected, finalized the current "active" IFTA Event and start a new "active" IFTA Event.
     * State changes correspond to the following actions:
     * If change jurisdictions.
     * If was not on non-ifta road and enter non-ifta road.
     * If was on a non-ifta road and exit.
     * If change Fuel Type or Truck.
     */
    public void checkForIftaEvent(Location location) {
        String strThis = "checkForIftaEvent(), ";
        boolean isDriving = USER_STATUS_DRIVING.equals(rules.getDrivingStatus());
        boolean isDebugBuild = BuildConfig.BUILD_TYPE.equals("debug");

        Log.d(TAG, strThis + "Start. location=" + location + ", rules.getDrivingStatus()=" + rules.getDrivingStatus()
            + ", BuildConfig.BUILD_TYPE=" + BuildConfig.BUILD_TYPE + ", isDriving=" + isDriving
            + ", isDebugBuild=" + isDebugBuild);

        long LstartMillis = System.currentTimeMillis();

        if (location != null && (isDriving || isDebugBuild)) {
            Log.d(TAG, strThis + "**** Case: Location is not null and (isDriving=true or isDebugBuild=true), proceeding with checkForIftaEvent() processing.");
            BusinessRules busRules = BusinessRules.instance();
            User user = busRules.getAuthenticatedUser();

            String strOdometer = busRules.getOdometer();
            double dblOdometer = (strOdometer != null ? Double.parseDouble(strOdometer) : 0.0);
            String fuelCode = getFuelCode();
            String fuelType = getFuelType();

            Log.d(TAG, strThis + "user: " + user.getLastFirstName() + ", strOdometer=" + strOdometer + ", dblOdometer=" + dblOdometer
                    + ", fuelCode=" + fuelCode);

            Log.d(TAG, strThis + "strOdometer=" + strOdometer + ", dblOdometer=" + dblOdometer
            );

            IftaEvent iftaEventActive = getActiveIftaEventFromDb();

            if (iftaEventActive == null) {
                Log.d(TAG, strThis + "Case: iftaEventActive == null");
                initActiveIftaEvent(user, location, dblOdometer, fuelCode, fuelType);
            } else {
                Log.d(TAG, strThis + "Case: iftaEventActive != null, iftaEventActive=" + iftaEventActive);

                checkUpdateActiveIftaEvent(location, user, dblOdometer, fuelCode, fuelType, iftaEventActive);
            }
        } else {
            Log.d(TAG, strThis + "**** Case: Location is null or (isDriving=false and isDebugBuild=false), skipping checkForIftaEvent() processing."
                    + "  location=" + location + ", isDriving=" + isDriving + ", isDebugBuild=" + isDebugBuild);
        }
        Log.d(TAG, strThis + "End. Processing time millis:" + (System.currentTimeMillis() - LstartMillis)
                + ", location=" + location);
    }

    public void checkUpdateActiveIftaEvent(Location location, User user, double dblOdometer, String fuelCode, String fuelType, IftaEvent iftaEventActive) {
        String strThis = "checkUpdateActiveIftaEvent, ";
        String truckNumber = getTruckNumber();
        Log.d(TAG, strThis + "truckNumber=" + truckNumber);

        Jurisdiction jur = null;
        Jurisdiction jurActive = null;
        String jurAbbrev = null;
        String jurAbbrevActive = null;
        boolean isNewIftaEvent = false;


        jurActive = getMapJurs().get(iftaEventActive.jurisdictionId);
        // Assertion - jurActive may be null;
        jurAbbrevActive = (jurActive != null ? jurActive.abbrev : NON_IFTA_JUR_ABBREV);
        jurAbbrev = jurAbbrevActive;

        Log.d(TAG, strThis + "Case: iftaEventActive != null, before jur check, jurActive=" + jurActive
                + ", jurAbbrevActive=" + jurAbbrevActive + ", jurAbbrev=" + jurAbbrev + ", dblOdometer=" + dblOdometer
                + ", iftaEventActive.odometer=" + iftaEventActive.odometer);

//        if (dblOdometer - iftaEventActive.odometer > .25) {
            jur = findJurisdiction(location.getLatitude(), location.getLongitude(), listJur,
                    jurActive);

            jurAbbrev = (jur != null ? jur.abbrev : NON_IFTA_JUR_ABBREV);
//        }

        // Assertion - jur may be null here, but jurAbbrev should always be non-null.

        Log.d(TAG, strThis + "Case: iftaEventActive != null, after jur check, "
                + "jurAbbrevActive=" + jurAbbrevActive + ", jurAbbrev=" + jurAbbrev + ", dblOdometer=" + dblOdometer
                + ", iftaEventActive.odometer=" + iftaEventActive.odometer);
        StringBuilder sbufExplanation = new StringBuilder();
        long currentTimeMillis = System.currentTimeMillis();

        isNewIftaEvent = isNewIftaEvent(iftaEventActive, fuelCode, truckNumber, jur, jurAbbrev, jurAbbrevActive, currentTimeMillis, sbufExplanation);

        // Todo: need validation, warning of traveling with expired fuel trip permit or trip permit.

        Log.d(TAG, strThis + "Case: iftaEventActive != null, after odometer update, iftaEventActive=" + iftaEventActive);
        iftaEventActive.odometer = dblOdometer;
        iftaEventActive.miles = iftaEventActive.odometer - iftaEventActive.odometerStart;
        iftaEventActive.longitude = location.getLongitude();
        iftaEventActive.latitude = location.getLatitude();

        Log.d(TAG, strThis + "Case: isNewIftaEvent=" + isNewIftaEvent);

        if (isNewIftaEvent) {
            Log.d(TAG, strThis + "Case: new IFTA Event detected, explanation: " + sbufExplanation);

            IftaEvent iftaEventNew = getNewActiveIftaEvent(user, dblOdometer, location.getLongitude(),
                    location.getLatitude(),
                    jurAbbrev, fuelCode, fuelType, isIftaTaxExempt(),
                    isCurrentFuelTripPermit(jur), isIftaTravel(jur));

            // To run in background thread, use runNewIftaEventTask() instead of finalizeOldAndCreateNewIftaEvent().
//                runNewIftaEventTask(iftaEventActive, iftaEventNew);
            finalizeOldAndCreateNewIftaEvent(iftaEventActive, iftaEventNew, currentTimeMillis);
        } else {
            Log.d(TAG, strThis + "Case: no new IFTA Event detected, updating active event.");
            // update pending iftaEvent with latest odometer.

            // To run in background thread, use  runUpdateIftaEventOdometerMiles() instead of updateIftaOdometer().
            updateIftaOdometer(iftaEventActive.getIdRecord(), iftaEventActive.odometer,
                    iftaEventActive.miles, iftaEventActive.longitude, iftaEventActive.latitude, currentTimeMillis);
        }
    }

    public boolean isNewIftaEvent(IftaEvent iftaEventActive, String fuelCode, String truckNumber,
                                  Jurisdiction jur, String jurAbbrev, String jurAbbrevActive, long currentTimeMillis, StringBuilder sbuf) {
        String strThis = "isNewIftaEvent(), ";

        boolean isNewIftaEvent;

//        isNewIftaEvent = isNewQuarter(iftaEventActive.dateTime)
//                || (!fuelCode.equals(iftaEventActive.fuelCode))
//                || (!truckNumber.equals(iftaEventActive.truckNumber))
//                || (!jurAbbrev.equals(jurAbbrevActive));
//
//        if (!isNewIftaEvent) {
//            boolean isFuelTripPermitActive = ("yes".equals(iftaEventActive.tripPermitYesOrNo));
//            boolean isFuelTripPermit = isCurrentFuelTripPermit(jur); // This is time sensitive, can expire at any time.
//            isNewIftaEvent = (isFuelTripPermit != isFuelTripPermitActive);
//        }

        isNewIftaEvent = isTrue(isNewQuarter(iftaEventActive.dateTime, currentTimeMillis), "Quarter change, now=" + DateUtils.getNowIsoZ() + ", prev dateTime: " + iftaEventActive.dateTime, "", sbuf)
                || isTrue(!fuelCode.equals(iftaEventActive.fuelCode), "fuelCode change, new: " + fuelCode + " not equals prev: " + iftaEventActive.fuelCode, "or", sbuf)
                || isTrue(!truckNumber.equals(iftaEventActive.truckNumber), "Truck Number change, new: " + truckNumber + " not equals prev: " + iftaEventActive.truckNumber, "or", sbuf)
                || isTrue(!jurAbbrev.equals(jurAbbrevActive), "Jurisdiction change, new: " + jurAbbrev + " not equals prev: " + jurAbbrevActive, "or", sbuf);

        if (!isNewIftaEvent) {
            boolean isFuelTripPermitActive = ("yes".equals(iftaEventActive.tripPermitYesOrNo));
            boolean isFuelTripPermit = isCurrentFuelTripPermit(jur); // This is time sensitive, can expire at any time.
            isNewIftaEvent = isTrue(isFuelTripPermit != isFuelTripPermitActive, "Is Fuel Trip Permit change, new: " + isFuelTripPermit + " not equals prev: " + isFuelTripPermitActive, "or", sbuf);
        }

        Log.d(TAG, strThis + "After checking, isNewIftaEvent=" + isNewIftaEvent + " because: " + sbuf);

        return isNewIftaEvent;
    }

    public boolean isTrue(boolean condition, String conditionDescription, String conjOp, StringBuilder explanation) {
        if (explanation != null) {
            if (explanation.length() > 0) explanation.append(" ").append(conjOp).append(" ");
            if (!condition) explanation.append(" not [");
            explanation.append(conditionDescription);
            if (!condition) explanation.append("] ");
        }

        return condition;
    }

    public void initActiveIftaEvent(User user, Location location, double dblOdometer, String fuelCode, String fuelType) {
        String strThis = "initActiveIftaEvent(), ";
        Jurisdiction jur;
        String jurAbbrev;
        Log.d(TAG, strThis + "Case: iftaEventActive is null, location=" + location + ", dblOdometer=" + dblOdometer);

        jur = findJurisdiction(location.getLatitude(), location.getLongitude(), listJur,
                null);

        Log.d(TAG, strThis + "Case: iftaEventActive == null, jur=" + jur);

        jurAbbrev = NON_IFTA_JUR_ABBREV;
        if (jur != null) jurAbbrev = jur.abbrev;

        Log.d(TAG, strThis + "Case: iftaEventActive == null, jurAbbrev=" + jurAbbrev + ", jur=" + jur);

        IftaEvent iftaEventNew = getNewActiveIftaEvent(user, dblOdometer, location.getLongitude(), location.getLatitude(),
                jurAbbrev, fuelCode, fuelType, isIftaTaxExempt(),
                isCurrentFuelTripPermit(jur), isIftaTravel(jur));

        Log.d(TAG, strThis + "Case: iftaEventActive == null, iftaEventNew=" + iftaEventNew);

        // To run in background thread, use runNewIftaEventTask() instead of finalizeOldAndCreateNewIftaEvent().
//        runNewIftaEventTask(null, iftaEventNew);
        finalizeOldAndCreateNewIftaEvent(null, iftaEventNew, System.currentTimeMillis());
    }

    public boolean isNewQuarter(String dateTimePrevious, long currentTimeMillis) {
        boolean isNewQuarter = true;

//        try {
//            java.util.Date dActive = dfmtDateTimeIsoZ.parse(dateTimePrevious);

        if (!StringUtils.isNullOrWhitespaces(dateTimePrevious)) {
            java.util.Date dActive = dateConverterToLocal.parse(dateTimePrevious);

            if (dActive != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTime(dActive);
                int monthActive = cal.get(Calendar.MONTH);
                cal.clear();
                cal.setTimeInMillis(currentTimeMillis);
                int monthNow = cal.get(Calendar.MONTH);

                if (monthActive / 4 == monthNow / 4) isNewQuarter = false;
            }
        }
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
        return isNewQuarter;
    }

    /**
     *
     * @param iftaEventActive
     * @param iftaEventNew
     * @deprecated for now, thread safety must be carefully considered.
     */
    public void runNewIftaEventTask(IftaEvent iftaEventActive, IftaEvent iftaEventNew) {
//        BusHelperIfta busHelperIfta = new BusHelperIfta(db);
        NewIftaEventTask task = new NewIftaEventTask();
        task.execute(this, iftaEventActive, iftaEventNew);
    }

    /**
     *
     * @param idRecord
     * @param odometer
     * @param miles
     * @param longitude
     * @param latitude
     * @deprecated for now, thread safety must be carefully considered.
     */
    public void runUpdateIftaEventOdometerMiles(Long idRecord, Double odometer, Double miles, Double longitude, Double latitude) {
        UpdateIftaEventOdometerMilesTask task = new UpdateIftaEventOdometerMilesTask();
        task.execute(idRecord, odometer, miles, longitude, latitude);
    }

    public void runCheckIftaEventTask(Activity activity) {
        String strThis = "runCheckIftaEventTask(), ";
        boolean IS_LOG_CHECK_TIME = false;

        if (RecordCommonHelper.isStartupSyncDone) {
            if (isTimeToCheckForIftaEvent()) {
                try {
                    Location location = rules.getPhoneLastBestLocation(activity);
                    Log.d(TAG, strThis + "It's time to check for new Ifta Event.");
                    ThreadPoolExecutor exec = (ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR;
                    Log.d(TAG, strThis + "Executor stats: "
                        + "getCorePoolSize()=" + exec.getCorePoolSize()
                        + ", getMaximumPoolSize()=" + exec.getMaximumPoolSize()
                        + ", getPoolSize()=" + exec.getPoolSize()
                        + ", getActiveCount()=" + exec.getActiveCount()
                        + ", getCompletedTaskCount()=" + exec.getCompletedTaskCount()
                        + ", getTaskCount()=" + exec.getTaskCount()
                            + ", queue class: " + exec.getQueue().getClass().getName()
                    );

                    boolean IS_ASYNC_TASK_DESIGN = true;
                    Log.d(TAG, strThis + "IS_ASYNC_TASK_DESIGN=" + IS_ASYNC_TASK_DESIGN);

                    if (IS_ASYNC_TASK_DESIGN) {
                        Log.d(TAG, strThis + "Using AsyncTask task to check for ifta event. IS_ASYNC_TASK_DESIGN=" + IS_ASYNC_TASK_DESIGN);
                        CheckForIftaEventTask task = new CheckForIftaEventTask();
                        // Todo: Task should finish well before check time interval (typically 30 sec), but
                        // consider a lock to prevent starting if task already running.
                        Log.d(TAG, strThis + "About to execute CheckForIftaEventTask...");
                        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, this, location, activity.getApplicationContext());
                        Log.d(TAG, strThis + "After execute CheckForIftaEventTask. task.getStatus()=" + task.getStatus());
                    } else {
                        Log.d(TAG, strThis + "Using Thread task to check for ifta event. IS_ASYNC_TASK_DESIGN=" + IS_ASYNC_TASK_DESIGN);
                        CheckForIftaEventThread task = new CheckForIftaEventThread(this, location, activity.getApplicationContext());
                        Log.d(TAG, strThis + "About to execute CheckForIftaEventThread...");
                        task.start();
                        Log.d(TAG, strThis + "After execute CheckForIftaEventTask.start() task.isAlive()=" + task.isAlive()
                                + ", task.getId()=" + task.getId() + ", task.getState()=" + task.getState()
                                + ", task.getName()=" + task.getName());
                    }
                } catch (Throwable e) {
                    Log.d(TAG, strThis + "**** Error. " + e);
                    e.printStackTrace();
                }
            } else if (IS_LOG_CHECK_TIME) Log.d(TAG, strThis + "Skipping Ifta Event check because delay interval not passed.");
        } else Log.d(TAG, strThis + "Skipping Ifta Event check because startup sync not finished.");
    }

    public boolean isTimeToCheckForIftaEvent() {
        String strThis = "isTimeToCheckForIftaEvent(), ";
        boolean IS_LOG_CHECK_TIME = false;
        boolean bRet = false;

        long now = System.currentTimeMillis();

        long LelapsedMillisSinceLastRun = now - LsystimeLastIftaCheck;

        if (IS_LOG_CHECK_TIME) Log.d(TAG, strThis + "Start. LsystimeLastIftaCheck=" + LsystimeLastIftaCheck
                + ", now=" + now + ", LelapsedMillisSinceLastRun=" + LelapsedMillisSinceLastRun);

        if (LelapsedMillisSinceLastRun > IFTA_CHECK_PERIOD_MILLIS) {
            Log.d(TAG, strThis + "IFTA Event check interval passed, okay to run task to check for IFTA event. " +
                    "LsystimeLastIftaCheck=" + LsystimeLastIftaCheck
                    + ", now=" + now + ", LelapsedMillisSinceLastRun=" + LelapsedMillisSinceLastRun);

            bRet = true;
            LsystimeLastIftaCheck = now;

            if (LelapsedMillisSinceLastRun > IFTA_CHECK_PERIOD_MAX_ELAPSED_ALERT) {
                // Todo: log-to-server warning that IFTA checking is not happening often enough, excessive miles errors.

                Log.d(TAG, strThis + "***** Warning.  IFTA Event check interval exceeded safe limits, miles accuracy erroding. " +
                        "LsystimeLastIftaCheck=" + LsystimeLastIftaCheck
                        + ", now=" + now + ", LelapsedMillisSinceLastRun=" + LelapsedMillisSinceLastRun);
            }
        }

        return bRet;
    }

    public static IftaEvent getNewActiveIftaEvent(User user, double odometer, double longitude, double latitude, String currentJur, String fuelCode,
                                                  String fuelType, boolean isIftaTaxExempt,
                                                  boolean isFuelTripPermit, boolean isIftaJurisdiction) {
        BusinessRules rules = BusinessRules.instance();

        IftaEvent iftaEvent = new IftaEvent();

        iftaEvent.setRecordType(Crms.R_IFTA_EVENT);
        iftaEvent.initCommon(TABLE_IFTA_EVENT, -1, -1, null,
                null, null, -1L,
                Rms.getMobileRecordId(getMobileRecordIdPrefix(Crms.R_IFTA_EVENT)),
                null, null, -1L, null,
                null, true,
                Cadp.SYNC_STATUS_PENDING_UPDATE, -1, -1L,
                Crms.R_IFTA_EVENT, null, 0);


        iftaEvent.dateTime = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z);
        iftaEvent.lastName = user.getLastName();
        iftaEvent.firstName = rules.getAuthenticatedUser().getFirstName();
        iftaEvent.fuelCode = fuelCode; // Todo
        iftaEvent.fuelType = fuelType; // Todo
        iftaEvent.company = user.getCompany();
        iftaEvent.state = user.getState();
        iftaEvent.country = user.getCountry();
        iftaEvent.dotNumber = getDotNumber(); // Todo;
        iftaEvent.employeeId = user.getEmployeeId();
        iftaEvent.truckNumber = getTruckNumber(); // Todo check;
        iftaEvent.vehicleLicenseNumber = getVehicleLicenseNumber(); // Todo:
        iftaEvent.jurisdictionId = currentJur;
        iftaEvent.isIftaTaxExemptRoadYesNo = (isIftaTaxExempt ? "yes" : "no");
        iftaEvent.status = IFTA_EVENT_STATUS_ACTIVE;
        iftaEvent.tripPermitYesOrNo = (isFuelTripPermit ? "yes" : "no");
        iftaEvent.iftaYesOrNo = (isIftaJurisdiction ? "yes" : "no");
        iftaEvent.odometer = odometer;
        iftaEvent.odometerStart = odometer;
        iftaEvent.longitude = longitude;
        iftaEvent.latitude = latitude;
        iftaEvent.miles = 0.0;

        return iftaEvent;
    }


    public double getOdometer() {
        String strOdometer = rules.getOdometer();
        double dblOdometer = Double.parseDouble(strOdometer);
        return dblOdometer;
    }

    public static boolean isIftaTaxExempt() {
        synchronized (isIftaTaxExemptSync) {
            if (isIftaTaxExempt >= 0) return isIftaTaxExempt == 1 ? true : false;
            else {
                setIsIftaTaxExempt(BusinessRules.instance().isTollRoadEventStarted()); // one-time initialization after startup.
                return isIftaTaxExempt();  // Recursive, but should be safe.
            }
        }
    }

    public static void setIsIftaTaxExempt(boolean isIftaTaxExempt) {
        synchronized (isIftaTaxExemptSync) {
            BusHelperIfta.isIftaTaxExempt = (isIftaTaxExempt ? 1 : 0);
        }
    }


    public static boolean isIftaTravel(String jurisdiction) {
        Jurisdiction jur = mapJurs.get(jurisdiction);
        return jur != null && jur.isIftaJurisdiction;
    }

    public static boolean isIftaTravel(Jurisdiction jur) {
        return jur != null && jur.isIftaJurisdiction;
    }

    public static boolean isCurrentFuelTripPermit(Jurisdiction jur) {
        boolean bret = false;
        if (jur == null) return false;

        /*
        Todo: scan fuel trip permits for jurisdiction and date range. If in home state, ignore fuel permit.
        Todo: if truck has IFTA sticker, then no need to check for trip permit?
        if have current IFTA membership, return false;
        else
            if in home state, return false;
            else {
                fuelTripPermit = fineFuelTripPermit(jurisdictionAbbrev);
                if (fuelTripPermit == null) {
                    if (jurisdictionAbbrev != homeJurisdictionAbbrev) Violation!!!!!!!!!!!
                } else {
                if (fuelTripPermit.expirationDate < tomorrow) return true;
                else return false;
            }
        */

        return bret;
    }

    public static String getTruckNumber() {
        String truckNumber = BusinessRules.instance().getAuthenticatedUser().getTruckNumber(); // Todo
        if (StringUtils.isNullOrWhitespaces(truckNumber)) truckNumber = "n/a";
        return truckNumber;
    }

    public static String getFuelCode() {
        Log.d(TAG, "getFuelCode() ****** Todo.  implement this stub.");
        return "01"; // Todo
    }

    public static String getFuelType() {
        Log.d(TAG, "getFuelType() ****** Todo.  implement this stub.");
        return "Diesel"; // Todo
    }

    public static String getVehicleLicenseNumber() {
        Log.d(TAG, "getVehicleLicenseNumber() ****** Todo.  implement this stub.");
        return "vehiclelicensenumber"; // Todo
    }

    public static String getDotNumber() {
        return "dotnumber"; // Todo
    }

    public boolean upsyncIftaEvents(int maxBatchSize) throws Exception {
        String strThis = "upsyncIftaEvents(), ";
        Log.d(TAG, strThis + "Start. maxBatchSize=" + maxBatchSize);
        // 1.  get batch of pending DVIR records to send to the RMS server.
        List<IftaEvent> list = getUpsyncIftaEventsFromDb(maxBatchSize);

        Log.d(TAG, strThis + "list.size()=" + (list != null ? list.size() : "(NULL)"));

        if ((list == null || list.size() == 0))
            return true;

        // 2.  Send pending records to RMS Server
        String returnJson = RmsHelperIfta.setIftaEvents(list);

        Log.d(TAG, strThis + "returnJson.length()=" + (returnJson != null ? returnJson.length() : "(NULL)"));

        // 3.  Mark successfully sent DVIR records as sent based on returned response.

        if (!StringUtils.isNullOrWhitespaces(returnJson)) {
            Log.d(TAG, strThis + "Case: non-empty returnJson, proceeding to process.");

            RecRepairWork recordIdWork = new RecRepairWorkRecTable(TABLE_IFTA_EVENT);

            String errorMessage = updateRecordsFromUpsyncResponse(returnJson, list, false, recordIdWork);
            if (errorMessage != null)
                Log.d(TAG, strThis + "**** Error while updating records after sending pending signatures: " + errorMessage);

        } else Log.d(TAG, strThis + "Case: empty returnJson, nothing to process.");


        Log.d(TAG, strThis + "End.");

        return false;
    }

    @Override
    public RecRepairWorkRecTable.RecWorkCombo getRecWorkCombo() {
        RmsRecTableRec recWork = new IftaEvent();
        RecRepairWork recRepairWork = new RecRepairWork(getTableName());

        RecRepairWorkRecTable.RecWorkCombo recRepairWorkCombo
                = new RecRepairWorkRecTable.RecWorkCombo(recWork, this);
        return recRepairWorkCombo;
    }



    public static void main(String[] args) {
//        final String[] values = getResources().getStringArray(R.array.planets_array);
    }


//    private static final String SQL_CODINGDATA_INSERT_UPDATE_NO_ID = "INSERT OR REPLACE INTO codingdata (IdRmsRecords, CodingMasterId, Value) VALUES (?,?,?)";
//    private SQLiteStatement stmtCodingDataInsertUpdateNoId; // not thread safe.
//
//    private static final String SQL_CODINGDATA_INSERT = "INSERT INTO codingdata (IdRmsRecords, CodingMasterId, Value) VALUES (?,?,?)";
//    private SQLiteStatement stmtCodingDataInsert; // not thread safe.
//
//    private static final String SQL_UPDATE_IFTA_SENT = "UPDATE " + TABLE_IFTA_EVENT + " SET sent = ? WHERE Id = ?";
//    private SQLiteStatement stmtCodingDataUpdate; // not thread safe.
//
//    private static final String SQL_CODINGDATA_DELETE_BY_ID = "DELETE FROM codingdata WHERE Id = ?";
//    private SQLiteStatement stmtCodingDataDeleteById; // not thread safe.


    public static String SQL_GET_ACTIVE_IFTA_EVENT =
            "SELECT id, idRecordType, ObjectId, ObjectType, RecordId, rmsTimestamp, MobileRecordId, " +
                    "dateTime, jurisdictionId, odometer, truckNumber, " +
                    "dotNumber, firstName, lastName, employeeId, company, state, country, " +
                    "vehicleLicenseNumber, iftaYesorNo, tripPermitYesOrNo, miles, fuelCode, fuelType, IftaTaxExemptRoadYesOrNo, " +
                    "status, odometerStart, longitude, latitude, sent, syncErrorCount, LocalSysTime " +
                    "FROM iftaEvent WHERE status = '" + IFTA_EVENT_STATUS_ACTIVE + "' ORDER BY LocalSysTime DESC";

    private static String SQL_GET_UPSYNC_IFTA_EVENTS =
            "SELECT id, idRecordType, ObjectId, ObjectType, RecordId, rmsTimestamp, MobileRecordId, " +
                    "dateTime, jurisdictionId, odometer, truckNumber, " +
                    "dotNumber, firstName, lastName, employeeId, company, state, country, " +
                    "vehicleLicenseNumber, iftaYesorNo, tripPermitYesOrNo, miles, fuelCode, fuelType, IftaTaxExemptRoadYesOrNo, " +
                    "status, odometerStart, longitude, latitude, sent, syncErrorCount, LocalSysTime " +
                    "FROM iftaEvent WHERE sent = " + Cadp.SYNC_STATUS_PENDING_UPDATE;

    private static String SQL_UPDATE_IFTA_ODOMETER_MILES =
            "UPDATE " + TABLE_IFTA_EVENT + " SET dateTime = ?, odometer = ?, miles = ?, longitude= ?, latitude = ?, sent = ?, LocalSysTime = ? WHERE id = ?";

    private static SQLiteStatement stmtUpdateIftaOdometerMiles;

    public SQLiteStatement getStmtUpdateIftaOdometerMiles() {
        if (stmtUpdateIftaOdometerMiles == null)
            stmtUpdateIftaOdometerMiles = getDb().compileStatement(SQL_UPDATE_IFTA_ODOMETER_MILES);
        return stmtUpdateIftaOdometerMiles;
    }

    public int updateIftaOdometer(long idRecord, double odometer, double miles, double longitude, double latitude, long currentTimeMillis) {
        String strThis = "updateIftaOdometer(), ";
        Log.d(TAG, "Start. idRecord=" + idRecord + ", odometer=" + odometer + ", miles=" + miles);
        SQLiteStatement stmt = getStmtUpdateIftaOdometerMiles();
        String dateTime = DateUtils.getDateTime(currentTimeMillis, DateUtils.FORMAT_ISO_SSS_Z);
        int ix = 1;
        bindString(stmt, ix++, dateTime);
        stmt.bindDouble(ix++, odometer);
        stmt.bindDouble(ix++, miles);
        stmt.bindDouble(ix++, longitude);
        stmt.bindDouble(ix++, latitude);
        stmt.bindLong(ix++, Cadp.SYNC_STATUS_PENDING_UPDATE);
        stmt.bindLong(ix++, currentTimeMillis);
        stmt.bindLong(ix++, idRecord);
        int iUpdated = stmt.executeUpdateDelete();
        Log.d(TAG, "End.  iUpdated=" + iUpdated + ", idRecord=" + idRecord + ", odometer=" + odometer + ", miles=" + miles);
        return iUpdated;
    }
//    private static final String SQL_DELETE_IFTA_EVENT_RECORD = "delete from " + TABLE_IFTA_EVENT + " where id = ?";
//    private SQLiteStatement stmtDeleteRmsRecord;
//
//    /**
//     * Todo: move this to superclass?
//     * @param idRecord
//     * @param txc
//     * @return
//     */
//    public int deleteRecord(long idRecord, DatabaseHelper.TxControl txc) {
//        // Not worrying about multithread access at this time.
//        if (stmtDeleteRmsRecord == null) getDb().compileStatement(SQL_DELETE_IFTA_EVENT_RECORD);
//
//        stmtDeleteRmsRecord.bindLong(0, idRecord);
//
////        int iUpdated = stmtDeleteRmsRecord.executeUpdateDelete();
//
//        int iUpdated = RecordCommonHelper.deleteTableRecordStatic(getDb(), stmtDeleteRmsRecord, idRecord, txc);
//
//        return iUpdated;
//    }

    /*
                    "CREATE TABLE " + BusHelperIfta.TABLE_IFTA_EVENT + " (" +
                            "id						    INTEGER PRIMARY KEY AUTOINCREMENT, " +
                            "ObjectId			    TEXT, " +
                            "ObjectType				TEXT, " +
                            "RecordId				TEXT, " +
                            "rmsTimestamp		        TEXT, " +
                            "MobileRecordId		    TEXT, " +
                            "dateTime		            TEXT, " +
                            "jurisdictionId		        TEXT, " +
                            "odometer		            TEXT, " +
                            "truckNumber		        TEXT, " +
                            "dotNumber		            TEXT, " +
                            "firstName		            TEXT, " +
                            "lastName		            TEXT, " +
                            "employeeId		            TEXT, " +
                            "company		            TEXT, " +
                            "state		                TEXT, " +
                            "country		            TEXT, " +
                            "vehicleLicenseNumber		TEXT, " +
                            "iftaYesOrNo                TEXT, " +
                            "tripPermitYesOrNo          TEXT, " +
                            "miles		                TEXT, " +
                            "fuelCode		            TEXT, " +
                            "fuelType		            TEXT, " +
                            "status                     TEXT, " +
                            "odometerStart              TEXT, " +
                            "sent				        INTEGER DEFAULT (" + Cadp.SYNC_STATUS_PENDING_UPDATE + "), " + // may need index to help syncing -- changing meaning of this to "SyncStatus"  0 means pending upsync, 1 means sync completed, 2 means marked for deletion.
                            "syncErrorCount             INTEGER," +
                            "LocalSysTime		        INTEGER " + // May need index to help syncing

     */

    private static final String SQL_INSERT = "INSERT INTO " + TABLE_IFTA_EVENT + "(" +
            "IdRecordType," +
            "ObjectId, " +
            "ObjectType, " +
            "RecordId, " +
            "rmsTimestamp, " +
            "MobileRecordId, " +
            "dateTime, " +
            "jurisdictionId, " +
            "odometer, " +
            "truckNumber, " +
            "dotNumber, " +
            "firstName, " +
            "lastName, " +
            "employeeId, " +
            "company, " +
            "state, " +
            "country, " +
            "vehicleLicenseNumber, " +
            "iftaYesOrNo, " +
            "tripPermitYesOrNo, " +
            "miles, " +
            "fuelCode, " +
            "fuelType, " +
            "IftaTaxExemptRoadYesOrNo, " +
            "status, " +
            "odometerStart, " +
            "longitude, " +
            "latitude, " +
            "sent, " +
            "syncErrorCount," +
            "LocalSysTime" +
            ") VALUES (" +
            "?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?" +
            ")";

    private SQLiteStatement stmtInsert;

    public SQLiteStatement getStmtInsert() {
        if (stmtInsert == null) stmtInsert = getDb().compileStatement(SQL_INSERT);
        return stmtInsert;
    }

    @Override
    public long insertRecord(RmsRecCommon iftaRecord) {
        IftaEvent rec = (IftaEvent) iftaRecord;
        SQLiteStatement stmt = getStmtInsert();

        int ix = 1;
        stmt.bindLong(ix++, rec.getIdRecordType());
        bindString(stmt, ix++, rec.getObjectId());
        bindString(stmt, ix++, rec.getObjectType());
        bindString(stmt, ix++, rec.getRecordId());
        stmt.bindLong(ix++, rec.getRmsTimestamp());
        bindString(stmt, ix++, rec.getMobileRecordId());
        bindString(stmt, ix++, rec.dateTime);
        bindString(stmt, ix++, rec.jurisdictionId);
        stmt.bindDouble(ix++, rec.odometer);
        bindString(stmt, ix++, rec.truckNumber);
        bindString(stmt, ix++, rec.dotNumber);
        bindString(stmt, ix++, rec.firstName);
        bindString(stmt, ix++, rec.lastName);
        bindString(stmt, ix++, rec.employeeId);
        bindString(stmt, ix++, rec.company);
        bindString(stmt, ix++, rec.state);
        bindString(stmt, ix++, rec.country);
        bindString(stmt, ix++, rec.vehicleLicenseNumber);
        bindString(stmt, ix++, rec.iftaYesOrNo);
        bindString(stmt, ix++, rec.tripPermitYesOrNo);
        stmt.bindDouble(ix++, rec.miles);
        bindString(stmt, ix++, rec.fuelCode);
        bindString(stmt, ix++, rec.fuelType);
        bindString(stmt, ix++, rec.isIftaTaxExemptRoadYesNo);
        bindString(stmt, ix++, rec.status);
        stmt.bindDouble(ix++, rec.odometerStart);
        stmt.bindDouble(ix++, rec.longitude);
        stmt.bindDouble(ix++, rec.latitude);
        stmt.bindLong(ix++, rec.getSentSyncStatus());
        stmt.bindLong(ix++, rec.getSyncErrorCount());
        stmt.bindLong(ix++, System.currentTimeMillis());

        long Lupdated = stmt.executeInsert();

        return Lupdated;
    }

    private static final String SQL_UPDATE_BY_ID = "UPDATE " + TABLE_IFTA_EVENT +
            " SET " +
            "IdRecordType = ?, " +
            "ObjectId = ?, " +
            "ObjectType = ?, " +
            "RecordId = ?, " +
            "rmsTimestamp= ?, " +
            "MobileRecordId = ?, " +
            "dateTime = ?, " +
            "jurisdictionId= ?, " +
            "odometer = ?, " +
            "truckNumber= ?, " +
            "dotNumber = ?, " +
            "firstName = ?, " +
            "lastName = ?, " +
            "employeeId = ?, " +
            "company = ?, " +
            "state = ?, " +
            "country = ?, " +
            "vehicleLicenseNumber = ?, " +
            "iftaYesOrNo = ?, " +
            "tripPermitYesOrNo = ?, " +
            "miles = ?, " +
            "fuelCode = ?, " +
            "fuelType = ?, " +
            "IftaTaxExemptRoadYesOrNo = ?, " +
            "status = ?, " +
            "odometerStart = ?, " +
            "longitude = ?, " +
            "latitude = ?, " +
            "sent = ?, " +
            "syncErrorCount = ?, " +
            "LocalSysTime = ? " +
            " WHERE id = ?";

    private SQLiteStatement stmtUpdateIftaRecord;

    private SQLiteStatement getStmtUpdateIftaRecord() {
        if (stmtUpdateIftaRecord == null)
            stmtUpdateIftaRecord = getDb().compileStatement(SQL_UPDATE_BY_ID);
        return stmtUpdateIftaRecord;
    }

    private static final String SQL_UPDATE_BY_ID_SKIP_DIRTY = "UPDATE " + TABLE_IFTA_EVENT +
            " SET " +
            "IdRecordType = ?, " +
            "ObjectId = ?, " +
            "ObjectType = ?, " +
            "RecordId = ?, " +
            "rmsTimestamp= ?, " +
            "MobileRecordId = ?, " +
            "dateTime = ?, " +
            "jurisdictionId= ?, " +
            "odometer = ?, " +
            "truckNumber= ?, " +
            "dotNumber = ?, " +
            "firstName = ?, " +
            "lastName = ?, " +
            "employeeId = ?, " +
            "company = ?, " +
            "state = ?, " +
            "country = ?, " +
            "vehicleLicenseNumber = ?, " +
            "iftaYesOrNo = ?, " +
            "tripPermitYesOrNo = ?, " +
            "miles = ?, " +
            "fuelCode = ?, " +
            "fuelType = ?, " +
            "IftaTaxExemptRoadYesOrNo = ?, " +
            "status = ?, " +
            "odometerStart = ?, " +
            "longitude = ?, " +
            "latitude = ?, " +
            "sent = ?, " +
            "syncErrorCount = ?, " +
            "LocalSysTime = ? " +
            " WHERE id = ? AND sent <> " + Cadp.SYNC_STATUS_PENDING_UPDATE;

    private SQLiteStatement stmtUpdateIftaRecordSkipDirty;

    private SQLiteStatement getStmtUpdateIftaRecordSkipDirty() {
        if (stmtUpdateIftaRecordSkipDirty == null)
            stmtUpdateIftaRecordSkipDirty = getDb().compileStatement(SQL_UPDATE_BY_ID_SKIP_DIRTY);
        return stmtUpdateIftaRecordSkipDirty;
    }

    @Override
    public int updateRecord(RmsRecCommon iftaRecord, boolean isSkipDirty) {
        String strThis = "updateRecord(), ";
        Log.d(TAG, strThis + "Start.  isSkipDirty=" + isSkipDirty + ", iftaRecord=" + iftaRecord);

        IftaEvent rec = (IftaEvent) iftaRecord; // haven't found better design avoiding cast.

        SQLiteStatement stmt = null;

        if (isSkipDirty)
            stmt = getStmtUpdateIftaRecordSkipDirty();
        else
            stmt = getStmtUpdateIftaRecord();

        Log.d(TAG, strThis + "stmt==null?" + (stmt == null));

        int ix = 1;

        stmt.bindLong(ix++, rec.getIdRecordType());
        bindString(stmt, ix++, rec.getObjectId());
        bindString(stmt, ix++, rec.getObjectType());
        Log.d(TAG, strThis + "updateRecord: objectId: "+rec.getObjectId()+" objectType: "+rec.getObjectType());
        bindString(stmt, ix++, rec.getRecordId());
        stmt.bindLong(ix++, rec.getRmsTimestamp());
        bindString(stmt, ix++, rec.getMobileRecordId());
        bindString(stmt, ix++, rec.dateTime);
        bindString(stmt, ix++, rec.jurisdictionId);
        stmt.bindDouble(ix++, rec.odometer);
        bindString(stmt, ix++, rec.truckNumber);
        bindString(stmt, ix++, rec.dotNumber);
        bindString(stmt, ix++, rec.firstName);
        bindString(stmt, ix++, rec.lastName);
        bindString(stmt, ix++, rec.employeeId);
        bindString(stmt, ix++, rec.company);
        bindString(stmt, ix++, rec.state);
        bindString(stmt, ix++, rec.country);
        bindString(stmt, ix++, rec.vehicleLicenseNumber);
        bindString(stmt, ix++, rec.iftaYesOrNo);
        bindString(stmt, ix++, rec.tripPermitYesOrNo);
        stmt.bindDouble(ix++, rec.miles);
        bindString(stmt, ix++, rec.fuelCode);
        bindString(stmt, ix++, rec.fuelType);
        bindString(stmt, ix++, rec.isIftaTaxExemptRoadYesNo);
        bindString(stmt, ix++, rec.status);
        stmt.bindDouble(ix++, rec.odometerStart);
        stmt.bindDouble(ix++, rec.longitude);
        stmt.bindDouble(ix++, rec.latitude);
        stmt.bindLong(ix++, rec.getSentSyncStatus());
        stmt.bindLong(ix++, rec.getSyncErrorCount());
        stmt.bindLong(ix++, System.currentTimeMillis());
        stmt.bindLong(ix++, rec.getIdRecord());

        int iUpdated = stmt.executeUpdateDelete();
        Log.d(TAG, strThis + "End.  iUpdated=" + iUpdated + ", isSkipDirty=" + isSkipDirty + ", iftaRecord=" + iftaRecord);

        return iUpdated;
    }


    public IftaEvent getActiveIftaEventFromDb() {
        String strThis = "getActiveIftaEventFromDb(), ";

        IftaEvent iftaEvent = null;
        Cursor cur = null;

        try {
            cur = RecordRulesHelper.getDb().getQuery(SQL_GET_ACTIVE_IFTA_EVENT);

            if (cur.getCount() > 1) {
//                throw new Exception(strThis + "**** Error.  More than one active IFTA Event record in local db");
                Log.d(TAG, strThis + "**** Error.  More than one active IFTA Event record in local db");
            }

            if (cur.moveToFirst()) {
                iftaEvent = new IftaEvent();
                iftaEvent.initFromCursor(cur);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) cur.close();
        }

        return iftaEvent;
    }

    public List<IftaEvent> getUpsyncIftaEventsFromDb(int maxBatchSize) {
        String strThis = "getUpsyncIftaEventsFromDb(), ";

        IftaEvent iftaEvent = null;
        Cursor cur = null;
        List<IftaEvent> listRet = null;

        try {
//            cur = getDb().getQuery(SQL_GET_UPSYNC_IFTA_EVENTS, new String[]{
//                    String.valueOf(Cadp.SYNC_STATUS_PENDING_UPDATE)});
            cur = getDb().getQuery(SQL_GET_UPSYNC_IFTA_EVENTS);
            if (cur.getCount() > 0) listRet = new ArrayList();

            for (int i = 0; i < maxBatchSize && cur.moveToNext(); i++) {
                iftaEvent = new IftaEvent();
                iftaEvent.initFromCursor(cur);
                listRet.add(iftaEvent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) cur.close();
        }

        return listRet;
    }

    /**
     * Alternate, slower but easy to maintain way to insert or update an IftaEvent in the local database.
     *
     * @param iftaEvent
     * @return
     */
    public IftaEvent saveIftaEventToDb(IftaEvent iftaEvent) {
        String strThis = "saveIftaEventToDb(), ";

        Cursor cur = null;

        try {
            ContentValues values = new ContentValues();

//            valuePutNB(values, "id", iftaEvent.getIdTable());
            bindNonblankVal(values, "ObjectId", iftaEvent.getObjectId());
            bindNonblankVal(values, "ObjectType", iftaEvent.getObjectType());
            bindNonblankVal(values, "RecordId", iftaEvent.getRecordId());
            bindNonblankVal(values, "rmsTimestamp", iftaEvent.getRmsTimestamp());
            bindNonblankVal(values, "MobileRecordId", iftaEvent.getMobileRecordId());
            bindNonblankVal(values, "dateTime", iftaEvent.dateTime);
            bindNonblankVal(values, "jurisdictionId", iftaEvent.jurisdictionId);
            bindNonblankVal(values, "odometer", iftaEvent.odometer);
            bindNonblankVal(values, "truckNumber", iftaEvent.truckNumber);
            bindNonblankVal(values, "dotNumber", iftaEvent.dotNumber);
            bindNonblankVal(values, "firstName", iftaEvent.firstName);
            bindNonblankVal(values, "lastName", iftaEvent.lastName);
            bindNonblankVal(values, "employeeId", iftaEvent.employeeId);
            bindNonblankVal(values, "company", iftaEvent.company);
            bindNonblankVal(values, "state", iftaEvent.state);
            bindNonblankVal(values, "country", iftaEvent.country);
            bindNonblankVal(values, "vehicleLicenseNumber", iftaEvent.vehicleLicenseNumber);
            bindNonblankVal(values, "iftaYesOrNo", iftaEvent.iftaYesOrNo);
            bindNonblankVal(values, "tripPermitYesOrNo", iftaEvent.tripPermitYesOrNo);
            bindNonblankVal(values, "miles", iftaEvent.miles);
            bindNonblankVal(values, "fuelCode", iftaEvent.fuelCode);
            bindNonblankVal(values, "fuelType", iftaEvent.fuelType);
            bindNonblankVal(values, "IftaTaxExemptRoadYesOrNo", iftaEvent.isIftaTaxExemptRoadYesNo);
            bindNonblankVal(values, "status", iftaEvent.status);
            bindNonblankVal(values, "odometerStart", iftaEvent.odometerStart);
            bindNonblankVal(values, "longitude", iftaEvent.longitude);
            bindNonblankVal(values, "latitude", iftaEvent.latitude);
            bindNonblankVal(values, "sent", iftaEvent.getSentSyncStatus()); // may need index to help syncing -- changing meaning of this to "SyncStatus"  0 means pending upsync, 1 means sync completed, 2 means marked for deletion.
            bindNonblankVal(values, "syncErrorCount", iftaEvent.getSyncErrorCount());
            bindNonblankVal(values, "LocalSysTime", iftaEvent.getLocalSysTime());

            if (iftaEvent.getIdRecord() >= 0)
                getDb().update(TABLE_IFTA_EVENT, values, "id = ?", new String[]{String.valueOf(iftaEvent.getIdRecord())});
            else
                getDb().insert(TABLE_IFTA_EVENT, values);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cur != null) cur.close();
        }

        return iftaEvent;
    }

    /**
     * Todo: move this to superclass?
     *
     * @param idTable
     * @param isValid
     * @param syncStatus
     * @param objectIdOptional
     * @param objectTypeOptional
     * @param isUpdateBlankObjectIdObjectType
     * @return
     */
    public int updateRmsRecordsStatus(long idTable, boolean isValid, int syncStatus, String objectIdOptional,
                                      String objectTypeOptional, boolean isUpdateBlankObjectIdObjectType) {

        String strThis = "updateRmsRecordsStatus(), ";

        int iUpdated = 0;

        try {
            ContentValues values = new ContentValues();

            if (isUpdateBlankObjectIdObjectType || !StringUtils.isNullOrWhitespaces(objectIdOptional))
                bindNonblankVal(values, "ObjectId", objectIdOptional);
            if (isUpdateBlankObjectIdObjectType || !StringUtils.isNullOrWhitespaces(objectTypeOptional))
                bindNonblankVal(values, "ObjectType", objectTypeOptional);
            bindNonblankVal(values, "isValid", isValid);
            bindNonblankVal(values, "syncStatus", syncStatus);

            iUpdated = getDb().update(TABLE_IFTA_EVENT, values, "id = ?", new String[]{String.valueOf(idTable)});
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        }

        return iUpdated;
    }


    // region: Nested Interfaces / Classes

    // ======================================== Nested Interfaces / Classes =======================

    public static class Jurisdiction implements Comparable<Jurisdiction> {
        public boolean isIftaJurisdiction;
        public String name;
        public String abbrev;
        public String geometryType;
        public double[] bbox; // lonmin, latmin, lonmax, latmax

        public double lonminx;
        public double lonmaxx;
        public double latmin;
        public double latmax;

        double[][][] lonCoordinatesX; // [polygon][linear ring][vertex] Note: [linear ring] index indicates the rings making up the polygon.  The first ring is the exterior polygon, the inner rings are "holes".
        double[][][] latCoordinates;

        public final double lonMinX() {
            return lonminx;
        }

        public final double lonMaxX() {
            return lonmaxx;
        }

        public final double latMin() {
            return latmin;
        }

        public final double latMax() {
            return latmax;
        }

        public void setBbox(double[] bbox) {
//            lonminx = lx(bbox[0]);
//            lonmaxx = lx(bbox[2]);
//            if (lonminx > lonmaxx) {
//                double d = lonminx;
//                lonminx = lonmaxx;
//                lonmaxx = d;
//            }
//
//            latmin = bbox[1];
//            latmax = bbox[3];
            this.bbox = bbox;
        }

        public void initMinMaxForCalc() {
            lonminx = 99999.;
            lonmaxx = -99999.;
            latmin = 99999.;
            latmax = -99999.;
        }

        @Override
        public int hashCode() {
            if (name == null) return 0;
            return name.hashCode();
        }

        @Override
        public boolean equals(@Nullable Object obj) {
            if (obj == null) return false;
            else return compareTo((Jurisdiction) obj) == 0;
        }

        public String toString() {
            return "name=" + name + ", abbrev=" + abbrev + ", geometryType=" + geometryType + ", bbox="
                    + (bbox != null ? "[" + bbox[0] + "," + bbox[1] + "," + bbox[2] + "," + bbox[3] + "]" : "(NULL)")
                    + ", lonminx=" + lonminx + ", latmin=" + latmin + ", lonmaxx=" + lonmaxx + ", latmax=" + latmax
                    + ", lonCoordinates.length=" + (lonCoordinatesX != null ? lonCoordinatesX.length : "(NULL)");
        }

        @Override
        public int compareTo(@NonNull Jurisdiction o) {
            if (o == null) return 1;
            if (name != null) {
                if (o.name != null) return name.compareTo(o.name);
                else return 1;
            } else if (o.name != null) return -1;
            else
                throw new NullPointerException(TAG + "Jurisdiction.compareTo() **** Error.  Can't compare jurisdictions. " +
                        "this.name and o.name are both null. "
                        + "this.name=" + this.name + ", o.name=" + o.name);
        }
    }

    public static class IftaParams {
        public String dateTime;
        public String jurisdictionAbbrev;
        public double odometer;
        public double longitude;
        public double latitude;
        public boolean isTollRoad;

        public IftaParams() {
        }

        public IftaParams(String settingValue) {
            parseValue(settingValue);
        }

        public String getValue() {
            return dateTime + "," + jurisdictionAbbrev + "," + odometer + "," + longitude
                    + "," + latitude + "," + isTollRoad;
        }

        public void parseValue(String settingValue) {
            String[] ar = settingValue.split(",");
            int ix = 0;
            dateTime = ar[ix++];
            jurisdictionAbbrev = ar[ix++];
            String s = ar[ix++];
            if (!StringUtils.isNullOrWhitespaces(s)) odometer = Double.parseDouble(s);
            s = ar[ix++];
            if (!StringUtils.isNullOrWhitespaces(s)) longitude = Double.parseDouble(s);
            s = ar[ix++];
            if (!StringUtils.isNullOrWhitespaces(s)) latitude = Double.parseDouble(s);
            s = ar[ix++];
            if (!StringUtils.isNullOrWhitespaces(s)) isTollRoad = Boolean.parseBoolean(s);
        }
    }

    public static class TestClass extends AsyncTask<Void, Void, Jurisdiction> {
        private double lat;
        private double lonx;
        private List<Jurisdiction> listJurs;
        private TextView textViewResults;

        public TestClass(double lat, double lonx, List<Jurisdiction> listJurs, TextView textViewResults) {
            this.lat = lat;
            this.lonx = lonx;
            this.listJurs = listJurs;
            this.textViewResults = textViewResults;
            Log.d(TAG, "TestClass() constructor. "
                    + ", textViewResults==null?" + (textViewResults == null));
        }

        @Override
        protected Jurisdiction doInBackground(Void... voids) {
            Log.d(TAG, "TestClass doInBackground() Start.  currentJur=" + currentJur);
            Jurisdiction jur = findJurisdiction(lat, lonx, listJurs, currentJur);
            Log.d(TAG, "testGeoCode() doInBackground() End.  jur=" + jur + ", currentJur=" + currentJur);
            return jur;
        }

        @Override
        protected void onPostExecute(Jurisdiction jurisdiction) {
            super.onPostExecute(jurisdiction);
            Log.d(TAG, "TestClass onPostExecute Start. jurisdiction=" + jurisdiction
                    + ", textViewResults==null?" + (textViewResults == null));
            currentJur = jurisdiction;
            if (jurisdiction != null)
                this.textViewResults.setText(jurisdiction.toString());
            else Log.d(TAG, "TestClass.onPostExecute() jurisdiction is null.");
            Log.d(TAG, "testGeoCode() onPostExecute() End.");
        }
    }

    public static class NewIftaEventTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            String strThis = "NewIftaEventTask doInBackground(), ";

            Log.d(TAG, strThis + "Start.");
            BusHelperIfta busHelperIfta = (BusHelperIfta) params[0];
            IftaEvent iftaEventActive = (IftaEvent) params[1];
            IftaEvent iftaEventNew = (IftaEvent) params[2];

            busHelperIfta.finalizeOldAndCreateNewIftaEvent(iftaEventActive, iftaEventNew, System.currentTimeMillis());

            Log.d(TAG, strThis + "End.");

            return null;
        }
    }

    public void finalizeOldAndCreateNewIftaEvent(IftaEvent iftaEventActive, IftaEvent iftaEventNew, long currentTimeMillis) {
        String strThis = "finalizeOldAndCreateNewIftaEvent, ";
        Log.d(TAG, strThis + "Start.");
        String strDateTime = DateUtils.getDateTime(currentTimeMillis, DateUtils.FORMAT_ISO_SSS_Z);

        if (iftaEventActive != null) {
            iftaEventActive.status = IFTA_EVENT_STATUS_FINAL;
            iftaEventActive.dateTime = strDateTime;
            updateRecord(iftaEventActive, false);
            Log.d(TAG, strThis + "Updated iftaEventActive.");
        }

        Log.d(TAG, strThis + "iftaEventActive null, skipping update.");

        iftaEventNew.status = IFTA_EVENT_STATUS_ACTIVE;
        iftaEventNew.dateTime = strDateTime;

        insertRecord(iftaEventNew);
        Log.d(TAG, strThis + "End.  inserted iftaEventNew.");
    }

    public static class UpdateIftaEventOdometerMilesTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            String strThis = "UpdateIftaEventOdometerMilesTask doInBackground(), ";

            Log.d(TAG, strThis + "Start.");
            BusHelperIfta busHelperIfta = (BusHelperIfta) params[0];
            Long idRecord = (Long) params[1];
            Double odometer = (Double) params[2];
            Double miles = (Double) params[3];
            Double longitude = (Double) params[4];
            Double latitude = (Double) params[5];

            Log.d(TAG, strThis + "Start. idRecord=" + idRecord + ", odometer=" + odometer + ", miles=" + miles);

            busHelperIfta.updateIftaOdometer(idRecord.longValue(), odometer.doubleValue(),
                    miles.doubleValue(), longitude, latitude, System.currentTimeMillis());

            Log.d(TAG, strThis + "End.");

            return null;
        }
    }

    public static class DownSyncTableRecsTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            String strThis = "DownSyncTableRecsTask doInBackground(), ";

            Log.d(TAG, strThis + "Start.");
            BusinessRules rules = (BusinessRules) params[0];

            Log.d(TAG, strThis + "Start. ");

            try {
                // Todo: upsync table recs.
                rules.downSyncRmsTableRecData();
            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.d(TAG, strThis + "End.");

            return null;
        }
    }

    public static class CheckForIftaEventTask extends AsyncTask<Object, Void, Void> {

        @Override
        protected Void doInBackground(Object... params) {
            String strThis = "CheckForIftaEventTask doInBackground(), ";
            Log.d(TAG, strThis + "Start.");

            long LstartMillis = System.currentTimeMillis();

            try {
                Context ctx = (Context) params[2];

                if (getListJur() == null) initStateProvGeoData(ctx); // initGeoCode(ctx);

                Log.d(TAG, strThis + "About to lock lockIftaProcessing");
                lockIftaProcessing.lock();
                Log.d(TAG, strThis + "After lock lockIftaProcessing");
                BusHelperIfta rules = (BusHelperIfta) params[0];
                Location location = (Location) params[1];

                // Todo: upsync table recs.
                rules.checkForIftaEvent(location);
            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            } finally {
                Log.d(TAG, strThis + "About to unlock lockIftaProcessing");
                lockIftaProcessing.unlock();
            }

            Log.d(TAG, strThis + "End. execution elapsed milliseconds: " + (System.currentTimeMillis() - LstartMillis));

            return null;
        }
    }

    public static class CheckForIftaEventThread extends Thread {
        private BusHelperIfta rules;
        private Location location;
        Context ctx;

        public CheckForIftaEventThread(BusHelperIfta rules, Location location, Context ctx) {
            super("CheckForIftaEventThread");
            this.rules = rules;
            this.location = location;
            this.ctx = ctx;
        }

        @Override
        public void run() {
            String strThis = "CheckForIftaEventThread run(), ";
            Log.d(TAG, strThis + "Start.");

            long LstartMillis = System.currentTimeMillis();

            try {

                if (getListJur() == null) initStateProvGeoData(ctx); // initGeoCode(ctx);

                Log.d(TAG, strThis + "About to lock lockIftaProcessing");
                lockIftaProcessing.lock();
                Log.d(TAG, strThis + "After lock lockIftaProcessing");

                // Todo: upsync table recs.
                rules.checkForIftaEvent(location);
            } catch (Throwable throwable) {
                if (throwable != null)
                    throwable.printStackTrace();
            } finally {
                Log.d(TAG, strThis + "About to unlock lockIftaProcessing");
                lockIftaProcessing.unlock();
            }

            Log.d(TAG, strThis + "End. execution elapsed milliseconds: " + (System.currentTimeMillis() - LstartMillis));
        }
    }
    // endregion: Nested Interfaces / Classes
}
