package com.rco.rcotrucks.activities.dvir.fragments;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.dvir.BusHelperDvir;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
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
import android.widget.ImageView;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.dvir.BusHelperDvir;
import com.rco.rcotrucks.activities.dvir.DvirListFragment;
import com.rco.rcotrucks.activities.dvir.DvirReportActivity;
import com.rco.rcotrucks.activities.dvir.UiHelperDvirDtl;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.ReceiptFragment;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.fragments.BaseFragment;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.DocumentUtils;
import com.rco.rcotrucks.utils.ImageUtils;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

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


public class DvirReport extends BaseFragment implements View.OnClickListener, IWriteResultCallbackWrapper {
    private static String TAG = DvirReport.class.getSimpleName();
    private WebView mWebView;
    private List<ListItemCodingDataGroup> listDvirDetail = null;
    private File fileAttachment;
    long rmsId, idRmsRecordId;
    String date = "", objectId = "", objectType = "", recordId = null, sortKey = "", trailerNumber = "", truckTractorNumber = "";
    Boolean isSent = true, isValid = true;
    TextView email, preview;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dvir_report, container, false);

        getIntentData();
        setIds(view);
        setIdsFromActivity();
        initialize();
        setListener();
        runUIHelperDvirRefreshTask();

        return view;
    }

    void getIntentData() {
        Log.d(TAG, "getIntentData: ");

        if (getArguments() != null) {
            Serializable message = getArguments().getSerializable("recordIdent");
            Log.d(TAG, "getIntentData: message: " + message);
            try {

                JSONObject jsonObject = new JSONObject(message.toString());
//                {date="10/17/2022 12:45:29", idRmsRecords=6, isSent=true, isValid=true, objectId="1136", objectType="NRT269", recordId=null,
//                sortKey="2022-10-17 12:45:29.000", trailerNumber="326, 328", truckTractorNumber="226"}
                date = jsonObject.getString("date");
                idRmsRecordId = jsonObject.getLong("idRmsRecords");
                objectId = jsonObject.getString("objectId");
                objectType = jsonObject.getString("objectType");
                recordId = jsonObject.getString("recordId");
                sortKey = jsonObject.getString("sortKey");
                trailerNumber = jsonObject.getString("trailerNumber");
                truckTractorNumber = jsonObject.getString("truckTractorNumber");
                isSent = jsonObject.getBoolean("isSent");
                isValid = jsonObject.getBoolean("isValid");

            } catch (JSONException jsonException) {
                Log.d(TAG, "getIntentData: jsonException: " + jsonException.toString());
            }
        }
    }

    void setIds(View view) {
        mWebView = view.findViewById(R.id.webview);
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
        UiHelperDvirDtl.instance().runRefreshTask(getActivity(), DvirReport.this, idRmsRecordId, objectId, objectType);
    }

    public void loadContentView() {
        listDvirDetail = BusHelperDvir.getListDvirDetail();
        injectJavascript(mWebView, listDvirDetail); // ------------------->
        mWebView.loadUrl("file:///android_asset/formdvir/dvirformpruned.html");  // -------------------->
        WebSettings webSettings = mWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
    }

    private static void injectJavascript(final WebView webView, final List<ListItemCodingDataGroup> listDvirDetail) {

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
                                         genValueSettingJavascript(sbuf, listDvirDetail);
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

    private static StringBuilder genValueSettingJavascript(StringBuilder sbuf, List<ListItemCodingDataGroup> listItems) {
        String strThis = "genValueSettingJavascript(), ";

        Map<String, List<String[]>> mapIds = getMapHtmlidByCodingfield();
        Map<String, String> mapCoding = new HashMap<>();
        Map<String, Bitmap> mapBitmaps = new HashMap<>();

        loadHtmlIdLookupMaps(listItems, mapCoding, mapBitmaps);

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

    private static void loadHtmlIdLookupMaps(List<ListItemCodingDataGroup> listItems,
                                             Map<String, String> mapCoding, Map<String, Bitmap> mapBitmaps) {

        String strThis = "loadHtmlIdLookupMaps(), ";

        Map<String, List<String[]>> mapIds = getMapHtmlidByCodingfield();

        String name;
        String value;

        int counter = 0;
        for (ListItemCodingDataGroup item : listItems) {
//            Log.d(TAG, "loadHtmlIdLookupMaps: counter: " + counter);
            counter++;
            List<BusHelperRmsCoding.CodingDataRow> listCoding = item.getListCodingdataRows();
//            Log.d(TAG, strThis + "Outer loop1: item= " + item + " name: " + item.getLabel() + " value: " + item.getCombinedValue());
            name = item.getLabel();
            value = item.getCombinedValue();
            loadMapValue(mapCoding, name, value);

//            Log.d(TAG, "loadHtmlIdLookupMaps: listCoding: " + listCoding.size());
            for (BusHelperRmsCoding.CodingDataRow itemCoding : listCoding) {
                name = itemCoding.getCodingfieldName();
                value = itemCoding.getDisplayValue();
                loadMapValue(mapCoding, name, value);
//                Log.d(TAG, strThis + "codingfield loop2a: codingfieldName=" + name
//                        + ", value=" + value + ", itemCoding=" + itemCoding);
            }

            AdapterUtils.BitmapItem[] arBitmapItems = item.getArBitmapItems();
            if (arBitmapItems != null) {
                Log.d(TAG, strThis + "bitmap: Case: arBitmapItems != null, arBitmapItems.length=" + arBitmapItems.length + ", item.getLabel()=" + item.getLabel());
                Log.d(TAG, "bitmap: loadHtmlIdLookupMaps: item: "+item);

                for (AdapterUtils.BitmapItem itemBitmap : arBitmapItems) {
                    Log.d(TAG, strThis + "bitmap: itemBitmap loop2b itemBitmap=" + itemBitmap);
                    Bitmap bitmap = itemBitmap.bitmap;
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

//            attachEmailContent();

//        if (isEmailClicked) {
//            attachEmailContent();
//        } else {
//            isPDFGenerated = true;
//        }

//        Log.d(TAG, "onWriteFinished: isPDFGenerated: " + isPDFGenerated);
//        Log.d(TAG, "onWriteFinished: isEmailClicked: " + isEmailClicked);


        try {
//            isEmailClicked = false;

//            String pdfFileName = "dvirreport.pdf";
//            fileAttachment = DocumentUtils.getLocalFileHandle(this, Cadp.FOLDER_RELATIVE_PATH_PRINTFILES, pdfFileName);
//
//            Log.d(TAG, "onWriteFinished: onclick() case: textViewEmail.  Before submitting print job, fileAttachment.getCanonicalPath()=" + fileAttachment.getCanonicalPath());
//
//            PrintDocumentAdapter.WriteResultCallback callback = PdfPrint.getWriteResultCallbackDelegate(DvirReportActivity.this);
//
//            fileAttachment = DocumentUtils.createWebPrintJob("printdvirreport", mWebView, fileAttachment, 200, callback);
//
//            Log.d(TAG, "onWriteFinished: onclick() after submitting print job, fileAttachment.getCanonicalPath()=" + fileAttachment.getCanonicalPath()
//                    + ", fileAttachment.length()=" + fileAttachment.length() + ", empty file expected until print job completes  see onWriteFinished().");

            BusHelperDvir helperDvir = new BusHelperDvir();
            String absolutePath = fileAttachment.getAbsolutePath();
            Log.d(TAG, "onWriteFinished: " + absolutePath);
            Path path = null;
            byte[] bytesArray = null;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                path = Paths.get(absolutePath);
                Log.d(TAG, "onWriteFinished: path: " + path);
                bytesArray = Files.readAllBytes(path);
                Log.d(TAG, "onWriteFinished: bytesArray: length: " + bytesArray.length);
            }

            if (path != null && bytesArray != null) {
                Log.d(TAG, "onWriteFinished: saveDvirDetailToDb: ");
                helperDvir.saveDvirDetailToDb(rmsId, listDvirDetail, true, bytesArray);
            }

        } catch (IOException e) {
            Log.d(TAG, "onWriteFinished: IOException: " + e.getMessage());
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

}