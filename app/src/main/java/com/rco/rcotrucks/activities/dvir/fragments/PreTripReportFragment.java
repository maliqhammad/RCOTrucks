package com.rco.rcotrucks.activities.dvir.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import android.print.IWriteResultCallbackWrapper;
import android.print.PageRange;
import android.print.PdfPrint;
import android.print.PrintDocumentAdapter;
import android.util.ArrayMap;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.dvir.BusHelperDvir;
import com.rco.rcotrucks.activities.dvir.UiHelperDvirDtl;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.fragments.BaseFragment;
import com.rco.rcotrucks.model.PretripModel;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.DocumentUtils;
import com.rco.rcotrucks.utils.ImageUtils;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PreTripReportFragment extends BaseFragment implements View.OnClickListener, IWriteResultCallbackWrapper {

    private static String TAG = PreTripReportFragment.class.getSimpleName();
    private WebView mWebView;
    private List<ListItemCodingDataGroup> listDvirDetail = null;
    private File fileAttachment;
    String objectId = "", objectType = "";
    TextView email, preview;
    long idRmsRecordId;
    PretripModel intentDataModel;
    EditText organizationNameET, addressET, dateET, timeET, truckNumberET, odometerET, trailer1NumberET, trailer1ReeferHOSET,
            trailer2NumberET, trailer2ReeferHOSET, remarksET, driverNameET, mechanicNameET, driverSignatureDateET, mechanicSignatureDateET;

    CheckBox airCompressorCB, airLinesCB, batteryCB, brakeAccessoriesCB, brakesCB, carburetorCB, clutchCB, defrosterCB,
            driveLineCB, fifthWheelCB, registrationCB, insuranceCB, frontalAxleCB, fuelTanksCB, heaterCB, hornCB, lightsCB, mirrorsCB, oilPressureCB,
            onBoardRecorderCB, radiatorCB, rearEndCB, reflectorsCB, safetyEquipmentCB, springsCB, starterCB, steeringCB,
            tachographCB, tiresCB, transmissionCB, wheelsCB, windowsCB, windShieldWipersCB, othersCB,
            trailer1BreakConnectionsCB, trailer1BrakesCB, trailer1CouplingPinCB, trailer1CouplingChainsCB, trailer1DoorsCB,
            trailer1HitchCB, trailer1LandingGearCB, trailer1LightsAllCB, trailer1RoofCB, trailer1SpringsCB, trailer1TarpaulinCB,
            trailer1TiresCB, trailer1WheelsCB, trailer1OthersCB,
            trailer2BreakConnectionsCB, trailer2BrakesCB, trailer2CouplingPinCB, trailer2CouplingChainsCB, trailer2DoorsCB,
            trailer2HitchCB, trailer2LandingGearCB, trailer2LightsAllCB, trailer2RoofCB, trailer2SpringsCB,
            trailer2TarpaulinCB, trailer2TiresCB, trailer2WheelsCB, trailer2OthersCB,
            conditionVehicleIsSatisfactoryCB, aboveDefectsCorrectedCB, aboveDefectsNoCorrectionNeededCB;

    ImageView mechanicSignature, driverSignature;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pre_trip_report, container, false);

        getIntentData();
        setIds(view);
        setIdsFromActivity();
        initialize();
        initializeFields();
        setListener();
        runUIHelperDvirRefreshTask();

        return view;
    }

    void getIntentData() {
        Log.d(TAG, "getIntentData: ");

        if (getArguments() != null) {
            Bundle intentBundle = getArguments();
            intentDataModel = (PretripModel) intentBundle.getSerializable("dataModel");
        }
    }

    void setIds(View view) {
        mWebView = view.findViewById(R.id.webview);

//        organizationNameET = view.findViewById(R.id.);
//        addressET = view.findViewById(R.id.);
//        dateET = view.findViewById(R.id.);
//        timeET = view.findViewById(R.id.);
//        truckNumberET = view.findViewById(R.id.);
//        odometerET = view.findViewById(R.id.);
//        trailer1NumberET = view.findViewById(R.id.);
//        trailer1ReeferHOSET = view.findViewById(R.id.);
//        trailer2NumberET = view.findViewById(R.id.);
//        trailer2ReeferHOSET = view.findViewById(R.id.);
//        remarksET = view.findViewById(R.id.);
//        driverNameET = view.findViewById(R.id.);
//        mechanicNameET = view.findViewById(R.id.);
//        driverSignatureDateET = view.findViewById(R.id.);
//        mechanicSignatureDateET = view.findViewById(R.id.);
//
//        airCompressorCB = view.findViewById(R.id.);
//        airLinesCB = view.findViewById(R.id.);
//        batteryCB = view.findViewById(R.id.);
//        brakeAccessoriesCB = view.findViewById(R.id.);
//        brakesCB = view.findViewById(R.id.);
//        carburetorCB = view.findViewById(R.id.);
//        clutchCB = view.findViewById(R.id.);
//        defrosterCB = view.findViewById(R.id.);
//        driveLineCB = view.findViewById(R.id.);
//        fifthWheelCB = view.findViewById(R.id.);
//        registrationCB = view.findViewById(R.id.);
//        insuranceCB = view.findViewById(R.id.);
//        frontalAxleCB = view.findViewById(R.id.);
//        fuelTanksCB = view.findViewById(R.id.);
//        heaterCB = view.findViewById(R.id.);
//        hornCB = view.findViewById(R.id.);
//        lightsCB = view.findViewById(R.id.);
//        mirrorsCB = view.findViewById(R.id.);
//        oilPressureCB = view.findViewById(R.id.);
//        onBoardRecorderCB = view.findViewById(R.id.);
//        radiatorCB = view.findViewById(R.id.);
//        rearEndCB = view.findViewById(R.id.);
//        reflectorsCB = view.findViewById(R.id.);
//        safetyEquipmentCB = view.findViewById(R.id.);
//        springsCB = view.findViewById(R.id.);
//        starterCB = view.findViewById(R.id.);
//        steeringCB = view.findViewById(R.id.);
//        tachographCB = view.findViewById(R.id.);
//        tiresCB = view.findViewById(R.id.);
//        transmissionCB = view.findViewById(R.id.);
//        wheelsCB = view.findViewById(R.id.);
//        windowsCB = view.findViewById(R.id.);
//        windShieldWipersCB = view.findViewById(R.id.);
//        othersCB = view.findViewById(R.id.);
//        trailer1BreakConnectionsCB = view.findViewById(R.id.);
//        trailer1BrakesCB = view.findViewById(R.id.);
//        trailer1CouplingPinCB = view.findViewById(R.id.);
//        trailer1CouplingChainsCB = view.findViewById(R.id.);
//        trailer1DoorsCB = view.findViewById(R.id.);
//        trailer1HitchCB = view.findViewById(R.id.);
//        trailer1LandingGearCB = view.findViewById(R.id.);
//        trailer1LightsAllCB = view.findViewById(R.id.);
//        trailer1RoofCB = view.findViewById(R.id.);
//        trailer1SpringsCB = view.findViewById(R.id.);
//        trailer1TarpaulinCB = view.findViewById(R.id.);
//        trailer1TiresCB = view.findViewById(R.id.);
//        trailer1WheelsCB = view.findViewById(R.id.);
//        trailer1OthersCB = view.findViewById(R.id.);
//
//        trailer2BreakConnectionsCB = view.findViewById(R.id.);
//        trailer2BrakesCB = view.findViewById(R.id.);
//        trailer2CouplingPinCB = view.findViewById(R.id.);
//        trailer2CouplingChainsCB = view.findViewById(R.id.);
//        trailer2DoorsCB = view.findViewById(R.id.);
//        trailer2HitchCB = view.findViewById(R.id.);
//        trailer2LandingGearCB = view.findViewById(R.id.);
//        trailer2LightsAllCB = view.findViewById(R.id.);
//        trailer2RoofCB = view.findViewById(R.id.);
//        trailer2SpringsCB = view.findViewById(R.id.);
//        trailer2TarpaulinCB = view.findViewById(R.id.);
//        trailer2TiresCB = view.findViewById(R.id.);
//        trailer2WheelsCB = view.findViewById(R.id.);
//        trailer2OthersCB = view.findViewById(R.id.);
//        conditionVehicleIsSatisfactoryCB = view.findViewById(R.id.);
//        aboveDefectsCorrectedCB = view.findViewById(R.id.);
//        aboveDefectsNoCorrectionNeededCB = view.findViewById(R.id.);
//
//        mechanicSignature = view.findViewById(R.id.);
//        driverSignature = view.findViewById(R.id.);

    }

    void setIdsFromActivity() {
        email = getActivity().findViewById(R.id.email_pretrip);
        preview = getActivity().findViewById(R.id.preview_pretrip);
    }


    void initialize() {
        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);

        if (email == null) {
            setIdsFromActivity();
        }
        email.setVisibility(View.VISIBLE);
        preview.setVisibility(View.GONE);
    }

    void setListener() {
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                generatePDFFile(true);
            }
        });
    }

    void runUIHelperDvirRefreshTask() {
        UiHelperDvirDtl.instance().runRefreshTask(getActivity(), PreTripReportFragment.this, idRmsRecordId, objectId, objectType);
    }

    public void loadContentView() {
        listDvirDetail = BusHelperDvir.getListDvirDetail();


        injectJavascript(mWebView, listDvirDetail, intentDataModel); // ------------------->
        mWebView.loadUrl("file:///android_asset/formdvir/dvirformpruned.html");  // -------------------->
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    private static void injectJavascript(final WebView webView, final List<ListItemCodingDataGroup> listDvirDetail, PretripModel intentDataModel) {

        webView.setWebViewClient(new WebViewClient() {
                                     @Override
                                     public void onPageFinished(WebView view, String url) {
                                         super.onPageFinished(view, url);
                                         Log.d(TAG, "onPageFinished: ");
                                         for (int i = 0; i < listDvirDetail.size(); i++) {
                                             Log.d(TAG, "onPageFinished: injectJavascript: onPageFinished: index: " + i + " label: " + listDvirDetail.get(i).getLabel()
                                                     + " combinedValue: " + listDvirDetail.get(i).getCombinedValue());
                                         }

                                         StringBuilder sbuf = new StringBuilder("javascript:(function() { var uselessvar;\n");
                                         genValueSettingJavascript(sbuf, listDvirDetail, intentDataModel);
                                         sbuf.append("})()");

//                                         Log.d(TAG, "onPageFinished: injectJavascript() sbuf=" + sbuf + "\n");
//                                         Log.d(TAG, "onPageFinished: injectJavascript() about to call webView.loadUrl()");

                                         webView.loadUrl(
//                        "javascript:(function() { "
//                                "var element = document.getElementById('hplogo');"
//                                + "uselessvar =document.getElementById('passwordfield').value='"+password+"'; " +
//                                + getValueSettingJavascript(listDvirDetail).toString()
//                                + "})()"
                                                 sbuf.toString()
                                         );
                                         Log.d(TAG, "onPageFinished: end: ");
                                     }
                                 }


        );

    }

    /**
     * This method builds a whole lot of Javascript statements that find an html element by id and set its value.  This allows
     * us to populate an HTML template displayed in a WebView object with values dynamically.
     * The source of our data is a list of "ListItemCodingDataGroup" items -- these are the items that correspond to a view (row)
     * in the RecyclerView presentation of a DVIR (detail).  However, one of these recycler views may be a composite
     * of several codingfield values (such as first and last name), and it may also display one or more images (so far only one image).
     * So each "ListItemCodingDataGroup" item contains lists of codingfields whose values get displayed (and sometimes updated by the user)
     * and also a list of bitmaps.  There is no real way to generalize the bitmaps since they do not correspond to a codingfield.
     * Therefore, the bitmap items have a "class" and "type" attribute to help with special case code.  So far, the classes include "signature"
     * and the types include "driver signature" and "mechanic signature".  We handle bitmaps with special case code.  Another
     * special case is when multiple codingfield values are combined into a single displayable value, such as "firstname lastname".
     * The ListItemCodingDataGroup item has a "combined value" attribute which can be used with special case code.
     * The basic structure is to loop over the ListItemCodingDataGroup items, with inner loops to loop over the codingfield list
     * and the bitmap list and transfer values to the HTML via generated Javascript that is injected with a WebClient object into the
     * WebView that will display the HTML.  A Map is pre-loaded with data that pairs a codingfieldname or pseudo name with the
     * html id of the corresponding element, and some type info as to what kind of element it is (checkbox, text, textarea, img, etc).  As
     * we loop over a list of html id keys (that we used to build the Map),
     * we use the Map to find the source codingfield or bitmap data that we want to stuff into the HTML element corresponding to the html id
     * and generate Javascript to set the element content/value.
     *
     * @param sbuf      work StringBuilder for building Javascript statements.
     * @param listItems
     * @return
     */

    private static StringBuilder genValueSettingJavascript(StringBuilder sbuf, List<ListItemCodingDataGroup> listItems, PretripModel intentDataModel) {
        String strThis = "genValueSettingJavascript(), ";

        Map<String, List<String[]>> mapIds = getMapHtmlidByCodingfield();
        Map<String, String> mapCoding = new HashMap<>();
        Map<String, Bitmap> mapBitmaps = new HashMap<>();

        loadHtmlIdLookupMaps(listItems, mapCoding, mapBitmaps, intentDataModel);

        for (String[] arId : arHtmlidCodingfieldRelations) {
            Log.d(TAG, strThis + "Loop.  arId=" + StringUtils.dumpArray(arId));

            String type = arId[0];
            String idHtml = arId[1];
            String lookupKey = arId[2];
            String format = null;
            if (arId.length > 3) format = arId[3];

            if (type.equals(Cadp.HTML_ELEM_TYPE_BITMAP)) {
                Log.d(TAG, strThis + "Case: type is Cadp.HTML_ID_TYPE_BITMAP.");
                Bitmap bitmap = mapBitmaps.get(lookupKey);
                if (bitmap != null) {
                    Log.d(TAG, strThis + "Case: type is Cadp.HTML_ID_TYPE_BITMAP, non-null bitmap.");
                    genSetBitmapValueJavascript(sbuf, idHtml, bitmap);
                } else
                    Log.d(TAG, strThis + ", **** Warning Case: type is Cadp.HTML_ID_TYPE_BITMAP,  but null bitmap. Skipping Javascript gen for idHtml: " + idHtml);
            } else {
                String value = mapCoding.get(lookupKey);

                if (type.equals(Cadp.HTML_ELEM_TYPE_CHECKBOX)) {
                    String checked = ("1".equals(value) ? "true" : "false");
                    sbuf.append("uselessvar = document.getElementById('").append(idHtml).append("').checked=").append(checked).append(";").append("\n");
                    Log.d(TAG, strThis + "Case: type is Cadp.HTML_ID_TYPE_CHECKBOX. Set checked attribute to: " + checked);
                } else {
                    if (!StringUtils.isNullOrEmpty(value)) {
                        if (Cadp.HTML_FORMAT_DATE.equals(format) && value != null && value.length() >= 10)
                            value = value.substring(0, 10);
                        else if (Cadp.HTML_FORMAT_TIME_FROM_DATE.equals(format) && value != null && value.length() > 11)
                            value = value.substring(11, 16);
                        else if (Cadp.HTML_FORMAT_AMPM_FROM_DATE.equals(format) && value != null && value.length() > 16)
                            value = value.substring(17);

                        if (type.equals(Cadp.HTML_ELEM_TYPE_SPAN))
                            sbuf.append("uselessvar = document.getElementById('").append(idHtml).append("').innerHTML='").append(value).append("';").append("\n");
                        else
                            sbuf.append("uselessvar = document.getElementById('").append(idHtml).append("').value='").append(value).append("';").append("\n");

                        Log.d(TAG, strThis + "Case: not bitmap, not checkbox (text, textarea, date, time, etc.). Set value=" + value);
                    }
                }
            }
        }

        return sbuf;
    }

    public static <K, V> void loadMapValue(Map<K, V> map, K key, V value) {
        Log.d(TAG, "loadMapValue: key: " + key + " value: " + value);
        if (key == null)
            Log.d(TAG, "loadMapValue(), Not loading value: [" + value + "] because key is null.");
        else map.put(key, value);
        if (value == null) Log.d(TAG, "loadMapValue(), **** Warning, loading null value for key");
    }

    static String getPreTripAttributeValue(String key, PretripModel intentDataModel) {
        Log.d(TAG, "getPreTripAttributeValue: key: " + key);
        if (key.equals("Logistics Carrier")) {
            return intentDataModel.getOrganizationName();
        } else if (key.equals("Address")) {
            return intentDataModel.getAddress();
        } else if (key.equals("Odometer")) {
            return intentDataModel.getOdometer();
        } else if (key.equals("DateTime")) {
            return intentDataModel.getDateTime();
        } else if (key.equals("TRUCK")) {
            return intentDataModel.getTruckNumber();
        } else if (key.equals("Air Compressor")) {
            return intentDataModel.getAirCompressor();
        } else if (key.equals("Air Lines")) {
            return intentDataModel.getAirLines();
        } else if (key.equals("Battery")) {
            return intentDataModel.getBattery();
        } else if (key.equals("Brake Accessories")) {
            return intentDataModel.getBrakeAccessories();
        } else if (key.equals("Brakes")) {
            return intentDataModel.getBrakes();
        } else if (key.equals("Carburetor")) {
            return intentDataModel.getCarburetor();
        } else if (key.equals("Clutch")) {
            return intentDataModel.getClutch();
        } else if (key.equals("Defroster")) {
            return intentDataModel.getDefroster();
        } else if (key.equals("Drive Line")) {
            return intentDataModel.getDriveLine();
        } else if (key.equals("Fifth Wheel")) {
            return intentDataModel.getFifthWheel();
        } else if (key.equals("Front Axle")) {
            return intentDataModel.getFrontalAxle();
        } else if (key.equals("Fuel Tanks")) {
            return intentDataModel.getFuelTanks();
        } else if (key.equals("Heater")) {
            return intentDataModel.getHeater();
        } else if (key.equals("Horn")) {
            return intentDataModel.getHorn();
        } else if (key.equals("Lights")) {
            return intentDataModel.getLights();
        } else if (key.equals("Mirrors")) {
            return intentDataModel.getMirrors();
        } else if (key.equals("Oil Pressure")) {
            return intentDataModel.getOilPressure();
        } else if (key.equals("On Board Recorder")) {
            return intentDataModel.getOnBoardRecorder();
        } else if (key.equals("Radiator")) {
            return intentDataModel.getRadiator();
        } else if (key.equals("Rear End")) {
            return intentDataModel.getRearEnd();
        } else if (key.equals("Reflectors")) {
            return intentDataModel.getReflectors();
        } else if (key.equals("Safety Equipment")) {
            return intentDataModel.getSafetyEquipment();
        } else if (key.equals("Springs")) {
            return intentDataModel.getSprings();
        } else if (key.equals("Starter")) {
            return intentDataModel.getStarter();
        } else if (key.equals("Steering")) {
            return intentDataModel.getSteering();
        } else if (key.equals("Tachograph")) {
            return intentDataModel.getTachograph();
        } else if (key.equals("Tires")) {
            return intentDataModel.getTires();
        } else if (key.equals("Transmission")) {
            return intentDataModel.getTransmission();
        } else if (key.equals("Wheels")) {
            return intentDataModel.getWheels();
        } else if (key.equals("Windows")) {
            return intentDataModel.getWheels();
        } else if (key.equals("Windshield Wipers")) {
            return intentDataModel.getWindShieldWipers();
        } else if (key.equals("Other")) {
            return intentDataModel.getOthers();
        } else if (key.equals("TRAILER 1")) {
            return intentDataModel.getTrailer1();
        } else if (key.equals("Reefer HOS")) {
            return intentDataModel.getTrailer1ReeferHOS();
        } else if (key.equals("Trailer1 Brake Connections")) {
            return intentDataModel.getTrailer1BreakConnections();
        } else if (key.equals("Trailer1 Brakes")) {
            return intentDataModel.getTrailer1Breaks();
        } else if (key.equals("Trailer1 Coupling Pin")) {
            return intentDataModel.getTrailer1CouplingPin();
        } else if (key.equals("Trailer1 Coupling Chains")) {
            return intentDataModel.getTrailer1CouplingChains();
        } else if (key.equals("Trailer1 Doors")) {
            return intentDataModel.getTrailer1Doors();
        } else if (key.equals("Trailer1 Hitch")) {
            return intentDataModel.getTrailer1Hitch();
        } else if (key.equals("Trailer1 Landing Gear")) {
            return intentDataModel.getTrailer1LandingGear();
        } else if (key.equals("Trailer1 Lights - All")) {
            return intentDataModel.getTrailer1LightsAll();
        } else if (key.equals("Trailer1 Roof")) {
            return intentDataModel.getTrailer1Roof();
        } else if (key.equals("Trailer1 Springs")) {
            return intentDataModel.getTrailer1Springs();
        } else if (key.equals("Trailer1 Tarpaulin")) {
            return intentDataModel.getTrailer1Tarpaulin();
        } else if (key.equals("Trailer1 Tires")) {
            return intentDataModel.getTrailer1Tires();
        } else if (key.equals("Trailer1 Wheels")) {
            return intentDataModel.getTrailer1Wheels();
        } else if (key.equals("Trailer1 Other")) {
            return intentDataModel.getTrailer1Others();
        } else if (key.equals("TRAILER 2")) {
            return intentDataModel.getTrailer2();
        } else if (key.equals("Reefer HOS")) {
            return intentDataModel.getTrailer2ReeferHOS();
        } else if (key.equals("Trailer2 Brake Connections")) {
            return intentDataModel.getTrailer2BreakConnections();
        } else if (key.equals("Trailer2 Brakes")) {
            return intentDataModel.getTrailer2Breaks();
        } else if (key.equals("Trailer2 Coupling Pin")) {
            return intentDataModel.getTrailer2CouplingPin();
        } else if (key.equals("Trailer2 Coupling Chains")) {
            return intentDataModel.getTrailer2CouplingChains();
        } else if (key.equals("Trailer2 Doors")) {
            return intentDataModel.getTrailer2Doors();
        } else if (key.equals("Trailer2 Hitch")) {
            return intentDataModel.getTrailer2Hitch();
        } else if (key.equals("Trailer2 Landing Gear")) {
            return intentDataModel.getTrailer2LandingGear();
        } else if (key.equals("Trailer2 Lights - All")) {
            return intentDataModel.getTrailer2LightsAll();
        } else if (key.equals("Trailer2 Roof")) {
            return intentDataModel.getTrailer2Roof();
        } else if (key.equals("Trailer2 Springs")) {
            return intentDataModel.getTrailer2Springs();
        } else if (key.equals("Trailer2 Tarpaulin")) {
            return intentDataModel.getTrailer2Tarpaulin();
        } else if (key.equals("Trailer2 Tires")) {
            return intentDataModel.getTrailer2Tires();
        } else if (key.equals("Trailer2 Wheels")) {
            return intentDataModel.getTrailer2Wheels();
        } else if (key.equals("Trailer2 Other")) {
            return intentDataModel.getTrailer2Others();
        } else if (key.equals("Remarks")) {
            return intentDataModel.getRemarks();
        } else if (key.equals("Condition Vehicle Satisfactory")) {
            return intentDataModel.getConditionVehicleIsSatisfactory();
        } else if (key.equals("Registration")) {
            return intentDataModel.getRegistration();
        } else if (key.equals("Insurance")) {
            return intentDataModel.getInsurance();
        } else if (key.equals("Drivers name and signature")) {
            return intentDataModel.getDriverNameAndSignature();
        } else if (key.equals("Above Defects Corrected")) {
            return intentDataModel.getAboveDefectsCorrected();
        } else if (key.equals("Above Defects No Corrections Needed")) {
            return intentDataModel.getAboveDefectsNoCorrectionNeeded();
        } else if (key.equals("Mechanic Name and Signature")) {
            return intentDataModel.getMechanicNameAndSignature();
        }

        return "";
    }

    private static void loadHtmlIdLookupMaps(List<ListItemCodingDataGroup> listItems,
                                             Map<String, String> mapCoding, Map<String, Bitmap> mapBitmaps, PretripModel intentDataModel) {

        String strThis = "loadHtmlIdLookupMaps(), ";

        Map<String, List<String[]>> mapIds = getMapHtmlidByCodingfield();

        String name;
        String value;

        int counter = 0;
        for (ListItemCodingDataGroup item : listItems) {
            Log.d(TAG, "loadHtmlIdLookupMaps: counter: " + counter);
            counter++;
            List<BusHelperRmsCoding.CodingDataRow> listCoding = item.getListCodingdataRows();
//            Log.d(TAG, strThis + "Outer loop1: item= " + item + " name: " + item.getLabel() + " value: " + item.getCombinedValue());
            Log.d(TAG, strThis + "Outer loop1: name: " + item.getLabel() + " value: " + item.getCombinedValue());
            name = item.getLabel();
//            value = item.getCombinedValue();
//            Jan 19, 2022  -
            value = getPreTripAttributeValue(name, intentDataModel);
            Log.d(TAG, "loadHtmlIdLookupMaps: returned: name: " + name + " value: " + value);

            loadMapValue(mapCoding, name, value);

//            Log.d(TAG, "loadHtmlIdLookupMaps: listCoding: " + listCoding.size());
//            for (BusHelperRmsCoding.CodingDataRow itemCoding : listCoding) {
//                name = itemCoding.getCodingfieldName();
//                value = itemCoding.getDisplayValue();
//                loadMapValue(mapCoding, name, value);
////                Log.d(TAG, strThis + "codingfield loop2a: codingfieldName=" + name
////                        + ", value=" + value + ", itemCoding=" + itemCoding);
//            }

            AdapterUtils.BitmapItem[] arBitmapItems = item.getArBitmapItems();
            if (arBitmapItems != null) {
                Log.d(TAG, strThis + "bitmap: Case: arBitmapItems != null, arBitmapItems.length=" + arBitmapItems.length + ", item.getLabel()=" + item.getLabel());
                Log.d(TAG, "bitmap: loadHtmlIdLookupMaps: item: " + item);

                for (AdapterUtils.BitmapItem itemBitmap : arBitmapItems) {
                    Log.d(TAG, strThis + "bitmap: itemBitmap loop2b itemBitmap=" + itemBitmap);
                    Bitmap bitmap = itemBitmap.bitmap;
                    Log.d(TAG, "bitmap: loadHtmlIdLookupMaps: bitmap: " + bitmap);
                    if (bitmap != null) {
                        mapBitmaps.put(String.valueOf(itemBitmap.bitmapType), bitmap);
                        Log.d(TAG, strThis + "bitmap: Case: stored non-null bitmap in mapBitmaps with key (itemBitmap.bitmapType): " + itemBitmap.bitmapType
                                + ", item=" + item + ", bitmap.getByteCount()=" + bitmap.getByteCount());
                    } else {
                        Log.d(TAG, strThis + "bitmap: ****Warning - itemBitmap with null bitmap, itemBitmap=" + itemBitmap
                                + ", item=" + item);
                    }

                }
            }
        }
    }

    private static void genSetBitmapValueJavascript(StringBuilder sbuf, Bitmap bitmap, String idLookupKey) {
        String strThis = "genSetBitmapValueJavascript(), ";
        Log.d(TAG, strThis + "Start. idLookupKey=" + idLookupKey);

        String imagedata = Base64.encodeToString(ImageUtils.getPngBytesFromBitmap(bitmap), Base64.DEFAULT);

        List<String[]> listIds = getMapHtmlidByCodingfield().get(idLookupKey);

        if (listIds != null) {
            for (String[] arId : listIds) {
                String id = arId[1];
//                    document.getElementById("img").src = "data:image/png;base64, iVBORw0KGgoAAAANSUhEUgAAAAUA
//                    AAAFCAYAAACNbyblAAAAHElEQVQI12P4//8/w38GIAXDIBKE0DHxgljNBAAO
//                    9TXL0Y4OHwAAAABJRU5ErkJggg=="
                sbuf.append("uselessvar = document.getElementById(\"").append(id).append("\").src = \"data:image/png;base64, ").append(imagedata).append("\";").append("\n");
                Log.d(TAG, strThis + "appended bitmap javascript to set image data for html id:" + id);
            }
        } else
            Log.d(TAG, strThis + "**** Error.  Nothing found for bitmap idLookupKey=" + idLookupKey);

        Log.d(TAG, strThis + "End. idLookupKey=" + idLookupKey);
    }

    private static void genSetBitmapValueJavascript(StringBuilder sbuf, String idHtml, Bitmap bitmap) {
        String strThis = "genSetBitmapValueJavascript(sbuf,idHtml,bitmap), ";
        Log.d(TAG, strThis + "Start. idHtml=" + idHtml);

        String imagedata = Base64.encodeToString(ImageUtils.getPngBytesFromBitmap(bitmap), Base64.DEFAULT);
        sbuf.append("uselessvar = document.getElementById(\"").append(idHtml).append("\").src = \"data:image/png;base64, ").append(imagedata).append("\";").append("\n");
        Log.d(TAG, strThis + "End. appended bitmap javascript to set image data for html idHtml:" + idHtml);
    }

    @Override
    public void onClick(View v) {
//        if (v == textViewClose) {
//            Log.d(TAG, "onclick() case: textViewClose. About to call finish()");
////            UiUtils.showToast(this, "Closing...");
////            onBackPressed();
//        } else if (v == textViewEmail) {
////            Log.d(TAG, "onClick: isPDFGenerated: " + isPDFGenerated);
////            Log.d(TAG, "onClick: isEmailClicked: " + isEmailClicked);
////            if (isPDFGenerated) {
////                attachEmailContent();
////            } else {
//            generatePDFFile(true);
////            }
//        }
    }


    public static Map<String, List<String[]>> mapHtmlidByCodingfield;

    public static Map<String, List<String[]>> getMapHtmlidByCodingfield() {
        if (mapHtmlidByCodingfield == null) {
            mapHtmlidByCodingfield = new ArrayMap<>();

            for (String[] arElement : arHtmlidCodingfieldRelations) {
                String key = arElement[2];

                List<String[]> item = mapHtmlidByCodingfield.get(key);

                if (item == null) {
                    item = new ArrayList<>();
                    mapHtmlidByCodingfield.put(key, item);
                }

                item.add(arElement);
            }
        }

        return mapHtmlidByCodingfield;
    }

    public static String[][] arHtmlidCodingfieldRelations = {
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "aboveDefectsCorrected", "Above Defects Corrected"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "aboveDefectsNoCorrectionNeeded", "Above Defects No Corrections Needed"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "address", "Address"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "airCompressor", "Air Compressor"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "airLines", "Air Lines"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "battery", "Battery"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "brakeAccessories", "Brake Accessories"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "brakes", "Brakes"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "carburetor", "Carburetor"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "carrier", Crms.LOGISTICS_CARRIER},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "clutch", "Clutch"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "conditionVehicleIsSatisfactory", "Condition Vehicle Satisfactory"},
//            {"hidden","d_f_name",""},
//            {"hidden","d_id",""},
//            {"hidden","d_l_name",""},
//            {"hidden","d_objectId",""},
//            {"hidden","d_objectType",""},
            {Cadp.HTML_ELEM_TYPE_TEXT, "date", "DateTime", Cadp.HTML_FORMAT_DATE},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "defroster", "Defroster"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "driveLine", "Drive Line"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "driverName", "Drivers Signature Vehicle Satisfactory"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "driversSignatureNoCorrectionNeededDate", Crms.DRIVERS_SIGNATURE_NO_CORRECTIONS_NEEDED_DATE},
//            {Cadp.HTML_ID_TYPE_TEXT,"driversSignatureVehicleSatisfactory","Drivers Signature Vehicle Satisfactory"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "fifthWheel", "Fifth Wheel"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "frontAxle", "Front Axle"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "fuelTanks", "Fuel Tanks"},
//            {"hidden","functionalGroupName","FunctionalGroupName"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "heater", "Heater"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "horn", "Horn"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "insurance", "Insurance"},
//            {"hidden","license_number","[user license]"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "lights", "Lights"},

//            {"hidden","location","Location"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "mechanicName", "Mechanics Signature"},
//            {Cadp.HTML_ID_TYPE_BITMAP,"mechanicsSignature",String.valueOf(Cadp.BITMAP_TYPE_MECHANIC_SIGNATURE)},
            {Cadp.HTML_ELEM_TYPE_TEXT, "mechanicsSignatureDate", Crms.MECHANICS_SIGNATURE_DATE},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "mirrors", "Mirrors"},
//            {"hidden","mobileRecordId","MobileRecordId"},
//            {"hidden","objectId",""},
//            {"hidden","objectType",""},
            {Cadp.HTML_ELEM_TYPE_TEXT, "odometer", "Odometer"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "oilPressure", "Oil Pressure"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "onBoardRecorder", "On Board Recorder"},
//            {"hidden","operation",""},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "others", Crms.OTHER},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "radiator", "Radiator"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "rearEnd", "Rear End"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "reflectors", "Reflectors"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "registration", "Registration"},
            {"textarea", "remarks", "Remarks"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "safetyEquipment", "Safety Equipment"},
            {Cadp.HTML_ELEM_TYPE_BITMAP, "signature_of_driver", String.valueOf(Cadp.BITMAP_TYPE_DRIVER_SIGNATURE)},
            {Cadp.HTML_ELEM_TYPE_BITMAP, "signature_of_mechanic", String.valueOf(Cadp.BITMAP_TYPE_MECHANIC_SIGNATURE)},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "springs", "Springs"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "starter", "Starter"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "steering", "Steering"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "tachograph", "Tachograph"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "time", "DateTime", Cadp.HTML_FORMAT_TIME_FROM_DATE},
            {Cadp.HTML_ELEM_TYPE_SPAN, "timeFormat", "DateTime", Cadp.HTML_FORMAT_AMPM_FROM_DATE},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "tires", "Tires"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1BreakeConnections", "Trailer1 Brake Connections"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Breakes", "Trailer1 Brakes"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1CouplingChains", "Trailer1 Coupling Chains"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1CouplingPin", "Trailer1 Coupling (King) Pin"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Doors", "Trailer1 Doors"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Hitch", "Trailer1 Hitch"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1LandingGear", "Trailer1 Landing Gear"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1LightsAll", "Trailer1 Lights - All"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "trailer1Numbers", "Trailer1 Number"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Other", "Trailer1 Other"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "trailer1ReeferHOS", "Trailer1 Reefer HOS"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Roof", "Trailer1 Roof"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Springs", "Trailer1 Springs"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Tarpaulin", "Trailer1 Tarpaulin"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Tires", "Trailer1 Tires"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer1Wheels", "Trailer1 Wheels"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2BreakeConnections", "Trailer2 Brake Connections"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Breakes", "Trailer2 Brakes"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2CouplingChains", "Trailer2 Coupling Chains"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2CouplingPin", "Trailer2 Coupling (King) Pin"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Doors", "Trailer2 Doors"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Hitch", "Trailer2 Hitch"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2LandingGear", "Trailer2 Landing Gear"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2LightsAll", "Trailer2 Lights - All"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "trailer2Numbers", "Trailer2 Number"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Other", "Trailer2 Other"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "trailer2ReeferHOS", "Trailer2 Reefer HOS"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Roof", "Trailer2 Roof"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Springs", "Trailer2 Springs"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Tarpaulin", "Trailer2 Tarpaulin"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Tires", "Trailer2 Tires"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "trailer2Wheels", "Trailer2 Wheels"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "transmission", "Transmission"},
            {Cadp.HTML_ELEM_TYPE_TEXT, "truckTractorNumber", "Truck Number"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "wheels", "Wheels"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "windShieldWipers", "Windshield Wipers"},
            {Cadp.HTML_ELEM_TYPE_CHECKBOX, "windows", "Windows"},
    };

    public static void sendEmail(Context ctx, String[] arToEmails, String subject, String body, File fileAttachment) {

        try {

//            long bytecount = countFileBytes(fileAttachment);

            Log.d(TAG, "sendEmail() called with: ctx = [" + ctx + "], arToEmails = ["
                            + arToEmails + "], subject = [" + subject + "], body = "
                            + body + "], fileAttachment = [" + fileAttachment + "], fileAttachment.getCanonicalPath()=" + fileAttachment.getCanonicalPath()
                            + ", fileAttachment.getAbsolutePath()=" + fileAttachment.getAbsolutePath()
                            + ", fileAttachment.length()=" + fileAttachment.length() // + ", bytecount=" + bytecount
//                    + ", fileAttachment.getFreeSpace()=" + fileAttachment.getFreeSpace() + ", fileAttachment.getUsableSpace()=" + fileAttachment.getUsableSpace()
//                    + ", fileAttachment.getTotalSpace()" + fileAttachment.getTotalSpace()
            );
        } catch (IOException e) {
            e.printStackTrace();
        }

        Uri uriAttachment = null;
        if (fileAttachment != null)
            uriAttachment = FileProvider.getUriForFile(ctx, "com.rco.rcotrucks", fileAttachment);

        Log.d(TAG, "sendEmail(), uriAttachment=" + uriAttachment);
        InputStream instream = null;

//        try {
//            instream = ctx.getContentResolver().openInputStream(uriAttachment);
//            long Lbytes = countFileBytes(instream);
//            Log.d(TAG, "sentEmail(), Lbytes=" + Lbytes);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        } finally {
//            if (instream != null) {
//                try {
//                    instream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }

        Intent emailIntent = new Intent(Intent.ACTION_SEND);
//        Intent emailIntent = new Intent(Intent.ACTION_SEND, uriAttachment);
        emailIntent.setData(Uri.parse("mailto:")); // only email apps should handle this// set the type to 'email'
//        emailIntent .setType("vnd.android.cursor.dir/email");
        emailIntent.setType("message/rfc822");// add email(s) here to whom you want to send email
//        emailIntent.setType("plain/text");// add email(s) here to whom you want to send email
//        String to[] = {toEmail};
        if (arToEmails != null && arToEmails.length > 0)
            emailIntent.putExtra(Intent.EXTRA_EMAIL, arToEmails);
        else Log.d(TAG, "sendEmail() arToEmails is null or empty, not setting in Intent.");
// add the fileAttachment
//        emailIntent .putExtra(Intent.EXTRA_STREAM, fileLocation);
// add mail subject
        if (!StringUtils.isNullOrWhitespaces(subject))
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        else Log.d(TAG, "sendEmail() subject is null or empty, not setting in Intent.");
//        emailIntent.setType("vnd.android.cursor.dir/email");
//        emailIntent.setPackage("com.google.android.gm");
// create mail service chooser
//        Uri uriAttachment = Uri.parse("file://" + attachmentFile);
//        Uri uriAttachment = Uri.fromFile(fileAttachment);
        if (!StringUtils.isNullOrWhitespaces(body))
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        else Log.d(TAG, "sendEmail() body is null or empty, not setting in Intent.");

        if (uriAttachment != null)
            emailIntent.putExtra(Intent.EXTRA_STREAM, uriAttachment);
        else Log.d(TAG, "sendEmail() uriAttachment is null, not setting in Intent.");

        ComponentName componentName = emailIntent.resolveActivity(ctx.getPackageManager());
        Log.d(TAG, "sendEmail() componentName=" + componentName);

        if (componentName != null) {
//        startActivity(Intent.createChooser(emailIntent , "Send email..."));
            emailIntent.setFlags(Intent.FLAG_ACTIVITY_FORWARD_RESULT);

            ctx.startActivity(emailIntent);
        } else {
            Log.d(TAG, "sendEmail(), ****** Error.  No suitable apps could be found for emailing.");
            UiUtils.showToast(ctx, "No suitable apps could be found for emailing.");
        }
    }

    public static long countFileBytes(File file) {
        Log.d(TAG, "countFileBytes() called with: file = [" + file + "]");
        FileInputStream fin = null;
        long count = 0;

        try {
            fin = new FileInputStream(file);
            count = countFileBytes(fin);
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                if (fin != null) fin.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return count;
    }

    public static long countFileBytes(InputStream inputStream) throws IOException {
        long count = 0;

        while (true) {
            int c = inputStream.read();
            Log.d(TAG, "countFileBytes() c=" + c);
            if (c == -1) break;
            count++;
        }

        return count;
    }

    @Override
    public void onWriteFinished(PageRange[] pages) {
        // The PdfPrint job is finished, the file should have content now.
        try {
            Log.d(TAG, "onWriteFinished() Start. After submitting print job, fileAttachment.getCanonicalPath()=" + fileAttachment.getCanonicalPath()
                    + ", fileAttachment.length()=" + fileAttachment.length() + ", fileAttachment should be non-empty.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onWriteFailed(CharSequence error) {

    }

    @Override
    public void onWriteCancelled() {

    }

    void attachEmailContent() {
        String[] arToEmails = null;
        String subject = "DVIR Report " + DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_MM_DD_YYYY);
        String body = subject;

        Log.d(TAG, "onWriteFinished() About to call sendEmail().");
        sendEmail(getContext(), arToEmails, subject, body, fileAttachment);
        Log.d(TAG, "onWriteFinished() End. After calling sendEmail()");
    }


    void generatePDFFile(boolean generateEmail) {
        Log.d(TAG, "generatePDFFile: ");
        try {
//            isEmailClicked = false;

            String pdfFileName = "dvirreport.pdf";
            fileAttachment = DocumentUtils.getLocalFileHandle(getContext(), Cadp.FOLDER_RELATIVE_PATH_PRINTFILES, pdfFileName);

            Log.d(TAG, "generatePDFFile: onclick() case: textViewEmail.  Before submitting print job, fileAttachment.getCanonicalPath()="
                    + fileAttachment.getCanonicalPath());

            PrintDocumentAdapter.WriteResultCallback callback = PdfPrint.getWriteResultCallbackDelegate(this);

            fileAttachment = DocumentUtils.createWebPrintJob("printdvirreport", mWebView, fileAttachment, 200, callback);

            Log.d(TAG, "generatePDFFile: onclick() after submitting print job, fileAttachment.getCanonicalPath()=" + fileAttachment.getCanonicalPath()
                    + ", fileAttachment.length()=" + fileAttachment.length() + ", empty file expected until print job completes  see onWriteFinished().");
            if (generateEmail) {
                attachEmailContent();
            }
        } catch (IOException e) {
            Log.d(TAG, "generatePDFFile: IOException: " + e.getMessage());
            e.printStackTrace();
        }
    }

//    @Override
//    public void onBackPressed() {
//        super.onBackPressed();
//        Log.d(TAG, "onBackPressed: isPreviewOnly: " + isPreviewOnly);
//        if (!isPreviewOnly) {
//            generatePDFFile(false);
//        }
//    }

    void initializeFields() {
        setEditTextFields();
        setCheckboxes();
        setMechanicSignatureImages();
    }

    void setEditTextFields() {

        organizationNameET.setText(intentDataModel.getOrganizationName());
        addressET.setText(intentDataModel.getAddress());
        dateET.setText(intentDataModel.getDateTime());
        timeET.setText(intentDataModel.getDateTime());
        truckNumberET.setText(intentDataModel.getTruckNumber());
        odometerET.setText(intentDataModel.getOdometer());
        trailer1NumberET.setText(intentDataModel.getTrailer1());
        trailer1ReeferHOSET.setText(intentDataModel.getTrailer1ReeferHOS());
        trailer2NumberET.setText(intentDataModel.getTrailer2());
        trailer2ReeferHOSET.setText(intentDataModel.getTrailer2ReeferHOS());
        remarksET.setText(intentDataModel.getRemarks());
        driverNameET.setText(intentDataModel.getDriverNameAndSignature());
        mechanicNameET.setText(intentDataModel.getMechanicNameAndSignature());
        driverSignatureDateET.setText(intentDataModel.getDriverSignatureDate());
        mechanicSignatureDateET.setText(intentDataModel.getMechanicsSignatureDate());
    }

    void setCheckboxes() {
//    airCompressorCB, airLinesCB, batteryCB, brakeAccessoriesCB, brakesCB, carburetorCB, clutchCB, defrosterCB,
        if (intentDataModel.getAirCompressor().equalsIgnoreCase("1")) {
            airCompressorCB.setEnabled(true);
        }
        if (intentDataModel.getAirLines().equalsIgnoreCase("1")) {
            airLinesCB.setEnabled(true);
        }
        if (intentDataModel.getBattery().equalsIgnoreCase("1")) {
            batteryCB.setEnabled(true);
        }
        if (intentDataModel.getBrakeAccessories().equalsIgnoreCase("1")) {
            brakeAccessoriesCB.setEnabled(true);
        }
        if (intentDataModel.getBrakes().equalsIgnoreCase("1")) {
            brakesCB.setEnabled(true);
        }
        if (intentDataModel.getCarburetor().equalsIgnoreCase("1")) {
            carburetorCB.setEnabled(true);
        }
        if (intentDataModel.getClutch().equalsIgnoreCase("1")) {
            clutchCB.setEnabled(true);
        }
        if (intentDataModel.getDefroster().equalsIgnoreCase("1")) {
            defrosterCB.setEnabled(true);
        }
//            driveLineCB, fifthWheelCB, registrationCB, insuranceCB, frontalAxleCB,
//            fuelTanksCB, heaterCB, hornCB, lightsCB, mirrorsCB, oilPressureCB,
        if (intentDataModel.getDriveLine().equalsIgnoreCase("1")) {
            driveLineCB.setEnabled(true);
        }
        if (intentDataModel.getFifthWheel().equalsIgnoreCase("1")) {
            fifthWheelCB.setEnabled(true);
        }
        if (intentDataModel.getRegistration().equalsIgnoreCase("1")) {
            registrationCB.setEnabled(true);
        }
        if (intentDataModel.getInsurance().equalsIgnoreCase("1")) {
            insuranceCB.setEnabled(true);
        }
        if (intentDataModel.getFrontalAxle().equalsIgnoreCase("1")) {
            frontalAxleCB.setEnabled(true);
        }
        if (intentDataModel.getFuelTanks().equalsIgnoreCase("1")) {
            fuelTanksCB.setEnabled(true);
        }
        if (intentDataModel.getHeater().equalsIgnoreCase("1")) {
            heaterCB.setEnabled(true);
        }
        if (intentDataModel.getHorn().equalsIgnoreCase("1")) {
            hornCB.setEnabled(true);
        }
        if (intentDataModel.getLights().equalsIgnoreCase("1")) {
            lightsCB.setEnabled(true);
        }
        if (intentDataModel.getMirrors().equalsIgnoreCase("1")) {
            mirrorsCB.setEnabled(true);
        }
        if (intentDataModel.getOilPressure().equalsIgnoreCase("1")) {
            oilPressureCB.setEnabled(true);
        }
//            onBoardRecorderCB, radiatorCB, rearEndCB, reflectorsCB, safetyEquipmentCB, springsCB, starterCB, steeringCB,
        if (intentDataModel.getOnBoardRecorder().equalsIgnoreCase("1")) {
            onBoardRecorderCB.setEnabled(true);
        }
        if (intentDataModel.getRadiator().equalsIgnoreCase("1")) {
            radiatorCB.setEnabled(true);
        }
        if (intentDataModel.getRearEnd().equalsIgnoreCase("1")) {
            rearEndCB.setEnabled(true);
        }
        if (intentDataModel.getReflectors().equalsIgnoreCase("1")) {
            reflectorsCB.setEnabled(true);
        }
        if (intentDataModel.getSafetyEquipment().equalsIgnoreCase("1")) {
            safetyEquipmentCB.setEnabled(true);
        }
        if (intentDataModel.getSprings().equalsIgnoreCase("1")) {
            springsCB.setEnabled(true);
        }
        if (intentDataModel.getStarter().equalsIgnoreCase("1")) {
            starterCB.setEnabled(true);
        }
        if (intentDataModel.getSteering().equalsIgnoreCase("1")) {
            steeringCB.setEnabled(true);
        }
        //            tachographCB, tiresCB, transmissionCB, wheelsCB, windowsCB, windShieldWipersCB, othersCB,
        if (intentDataModel.getTachograph().equalsIgnoreCase("1")) {
            tachographCB.setEnabled(true);
        }
        if (intentDataModel.getTires().equalsIgnoreCase("1")) {
            tiresCB.setEnabled(true);
        }
        if (intentDataModel.getTransmission().equalsIgnoreCase("1")) {
            transmissionCB.setEnabled(true);
        }
        if (intentDataModel.getWheels().equalsIgnoreCase("1")) {
            wheelsCB.setEnabled(true);
        }
        if (intentDataModel.getWindows().equalsIgnoreCase("1")) {
            windowsCB.setEnabled(true);
        }
        if (intentDataModel.getWindShieldWipers().equalsIgnoreCase("1")) {
            windShieldWipersCB.setEnabled(true);
        }
        if (intentDataModel.getOthers().equalsIgnoreCase("1")) {
            othersCB.setEnabled(true);
        }
//            trailer1BreakConnectionsCB, trailer1BrakesCB, trailer1CouplingPinCB, trailer1CouplingChainsCB, trailer1DoorsCB,
        if (intentDataModel.getTrailer1Breaks().equalsIgnoreCase("1")) {
            trailer1BreakConnectionsCB.setEnabled(true);
        }
        if (intentDataModel.getBrakes().equalsIgnoreCase("1")) {
            trailer1BrakesCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1CouplingPin().equalsIgnoreCase("1")) {
            trailer1CouplingPinCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1CouplingChains().equalsIgnoreCase("1")) {
            trailer1CouplingChainsCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1Doors().equalsIgnoreCase("1")) {
            trailer1DoorsCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1Hitch().equalsIgnoreCase("1")) {
            trailer1HitchCB.setEnabled(true);
        }
//            trailer1HitchCB, trailer1LandingGearCB, trailer1LightsAllCB, trailer1RoofCB, trailer1SpringsCB, trailer1TarpaulinCB,
//            trailer1TiresCB, trailer1WheelsCB, trailer1OthersCB,
        if (intentDataModel.getTrailer1LandingGear().equalsIgnoreCase("1")) {
            trailer1LandingGearCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1LightsAll().equalsIgnoreCase("1")) {
            trailer1LightsAllCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1Roof().equalsIgnoreCase("1")) {
            trailer1RoofCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1Springs().equalsIgnoreCase("1")) {
            trailer1SpringsCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1Tarpaulin().equalsIgnoreCase("1")) {
            trailer1TarpaulinCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1Tires().equalsIgnoreCase("1")) {
            trailer1TiresCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1Wheels().equalsIgnoreCase("1")) {
            trailer1WheelsCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer1Others().equalsIgnoreCase("1")) {
            trailer1OthersCB.setEnabled(true);
        }

//            trailer2BreakConnectionsCB, trailer2BrakesCB, trailer2CouplingPinCB, trailer2CouplingChainsCB, trailer2DoorsCB,
//            trailer2HitchCB, trailer2LandingGearCB, trailer2LightsAllCB, trailer2RoofCB, trailer2SpringsCB,
        if (intentDataModel.getTrailer2BreakConnections().equalsIgnoreCase("1")) {
            trailer2BreakConnectionsCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2Breaks().equalsIgnoreCase("1")) {
            trailer2BrakesCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2CouplingPin().equalsIgnoreCase("1")) {
            trailer2CouplingPinCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2CouplingChains().equalsIgnoreCase("1")) {
            trailer2CouplingChainsCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2Doors().equalsIgnoreCase("1")) {
            trailer2DoorsCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2Hitch().equalsIgnoreCase("1")) {
            trailer2HitchCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2LandingGear().equalsIgnoreCase("1")) {
            trailer2LandingGearCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2LightsAll().equalsIgnoreCase("1")) {
            trailer2LightsAllCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2Roof().equalsIgnoreCase("1")) {
            trailer2RoofCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2Springs().equalsIgnoreCase("1")) {
            trailer2SpringsCB.setEnabled(true);
        }

//            trailer2TarpaulinCB, trailer2TiresCB, trailer2WheelsCB, trailer2OthersCB,
//            conditionVehicleIsSatisfactoryCB, aboveDefectsCorrectedCB, aboveDefectsNoCorrectionNeededCB;

        if (intentDataModel.getTrailer2Tarpaulin().equalsIgnoreCase("1")) {
            trailer2TarpaulinCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2Tires().equalsIgnoreCase("1")) {
            trailer2TiresCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2Wheels().equalsIgnoreCase("1")) {
            trailer2WheelsCB.setEnabled(true);
        }
        if (intentDataModel.getTrailer2Others().equalsIgnoreCase("1")) {
            trailer2OthersCB.setEnabled(true);
        }
        if (intentDataModel.getConditionVehicleIsSatisfactory().equalsIgnoreCase("1")) {
            conditionVehicleIsSatisfactoryCB.setEnabled(true);
        }
        if (intentDataModel.getAboveDefectsCorrected().equalsIgnoreCase("1")) {
            aboveDefectsCorrectedCB.setEnabled(true);
        }
        if (intentDataModel.getAboveDefectsNoCorrectionNeeded().equalsIgnoreCase("1")) {
            aboveDefectsNoCorrectionNeededCB.setEnabled(true);
        }


    }

    void setMechanicSignatureImages() {
//        mechanicSignature, driverSignature
        Picasso.with(getContext()).load(intentDataModel.getMechanicSignatureBitmap()).into(mechanicSignature);
        Picasso.with(getContext()).load(intentDataModel.getDriverSignatureBitmap()).into(driverSignature);
    }


}