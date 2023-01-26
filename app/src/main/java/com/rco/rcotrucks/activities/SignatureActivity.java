package com.rco.rcotrucks.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.dvir.BusHelperDvir;
import com.rco.rcotrucks.activities.dvir.RmsHelperDvir;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.HttpClient;
import com.rco.rcotrucks.views.SignatureImageView;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;

import org.json.JSONArray;

public class SignatureActivity extends AppCompatActivity {
    //    private static final String TAG = SignatureActivity.class.getSimpleName();
    private static final String TAG = "SignatureCheck";

    //    private RelativeLayout relativeLayout;
//    private ConstraintLayout layout;
    private LinearLayout layout;

    private Paint paint;
    //    private DrawView viewSignature;
    private SignatureImageView viewSignature;
    private Path path;
    //    private Bitmap bitmap;
//    private Canvas canvas;
    Button buttonClear;
    private ImageView textViewCancel;
    private TextView textViewSave;
    private long idRmsRecordsParent = -1L;
    //    private long idRmsRecordsSignature = -1L;
    private Bitmap bitmap = null;
    //    private BusHelperRmsCoding.CodingDataRow rowSignature = null;
    private AdapterUtils.BitmapItem bitmapItemSignature = null;
    private ListItemCodingDataGroup listItemSignature = null;

    List<BusHelperRmsCoding.RmsRecordCoding> listHeader = new ArrayList<>();
    String objectId, objectType, signatureName = "", signatureType = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        String strThis = "onCreate(), ";

        Intent intent = getIntent();
        Serializable message = intent.getSerializableExtra(Cadp.EXTRA_MESSAGE);
//
        initFromMessage(strThis, message); // ------------------------------->
//        Intent intent = getIntent();
        objectId = intent.getStringExtra(Cadp.EXTRA_OBJECT_ID);
        objectType = intent.getStringExtra(Cadp.EXTRA_OBJECT_TYPE);
        signatureName = intent.getStringExtra(Cadp.EXTRA_SIGNATURE);
        signatureType = intent.getStringExtra(Cadp.EXTRA_SIGNATURE_TYPE);
        Log.d(TAG, "onCreate: objectId: " + objectId + " objectType: " + objectType
                + " signatureName: " + signatureName + " signatureType: " + signatureType);

//        intent.putExtra(Cadp.EXTRA_OBJECT_ID, objectId);
//        intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, objectType);


//        setContentView(R.layout.activity_signature_layout);
        Log.d(TAG, strThis + "about to load layout.");
        setContentView(R.layout.activity_signature_imageview_layout);

        Log.d(TAG, strThis + "Assigning cancel button.");
        textViewCancel = findViewById(R.id.btn_back);

        textViewCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                path.reset(); // necessary?
//                Intent intent1 = new Intent();
                setResult(RESULT_CANCELED);
                finish();
            }
        });

        Log.d(TAG, strThis + "Assigning save button.");
        textViewSave = findViewById(R.id.textViewSave);

        textViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, " save signature ");

                Bitmap bitmap = viewSignature.getNewSignatureBitmap();
                saveSignature(bitmap);
                Log.d(TAG, " textViewSave.setOnClickListener.onClick()  bitmap==null?: " + (bitmap == null));

//                if (bitmap != null) {
                // **** save to local file for debugging.
//                    saveBitmapToPngFile(SignatureActivity.this, bitmap, "TestFolder", "signature" + System.currentTimeMillis() + ".png");

                // We could save to db at this point, but Dragos saves to list so can be canceled if list edit is canceled.
                Log.d(TAG, "onClick: bitmapItemSignature: " + bitmapItemSignature);
                Log.d(TAG, "onClick: bitmapItemSignature: getListCodingdataRows: " + listItemSignature.getListCodingdataRows().size());
                if (bitmapItemSignature != null) {
                    bitmapItemSignature.setBitmap(bitmap);

                    List<BusHelperRmsCoding.CodingDataRow> codingDataRows = listItemSignature.getListCodingdataRows();
                    for (BusHelperRmsCoding.CodingDataRow rowCoding : codingDataRows) {
                        if (rowCoding.getDataTypeName().startsWith("Date")) {
                            rowCoding.setValue(DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z));
                            Log.d(TAG, " textViewSave.setOnClickListener.onClick()  found a rowCoding of Date/DateTime type, "
                                    + "set value to current datetime. rowCoding.getDisplayValue()=" + rowCoding.getDisplayValue()
                                    + ", rowCoding=" + rowCoding);
                        }
                    }
                } else {
                    Log.d(TAG, " textViewSave.setOnClickListener.onClick()  ***** Error. bitmapItemSignature is null, cannot set bitmap.");
                }

//                UiUtils.showToast(SignatureActivity.this, "textViewSave.setOnClickListener.onClick(), I'm clicked, tried to save image.");
//                }

//                Intent intent1 = new Intent();
                setResult(RESULT_OK);
                finish();
            }
        });


        Log.d(TAG, strThis + "Assigning relative layout.");
//        relativeLayout = (RelativeLayout) findViewById(R.id.relativelayout1);
//        layout = (ConstraintLayout) findViewById(R.id.layoutSignature);
//        LinearLayout layout = (LinearLayout) findViewById(R.id.linearLayoutToolBarHeader);

        Log.d(TAG, strThis + "Assigning clear button.");
        buttonClear = (Button) findViewById(R.id.buttonClearSketch);

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "buttonClear.onClick() Start. viewSignature.getDrawable()==null?" + (viewSignature.getDrawable() == null));
//                path.reset();
//                viewSignature.setImageBitmap(null);
//                viewSignature.setImageDrawable(null);
//                viewSignature.invalidate();
                viewSignature.clear();
                Log.d(TAG, "buttonClear.onClick() End. viewSignature.getDrawable()==null?" + (viewSignature.getDrawable() == null));


            }
        });


//        view = new DrawView(this);
//
        Log.d(TAG, strThis + "Initializing paint and path objects.");
        paint = initPaint();
        path = new Path();

        Log.d(TAG, strThis + "Assigning viewSignature.");

//        viewSignature = (DrawView) findViewById(R.id.viewSketchSheet);
        viewSignature = (SignatureImageView) findViewById(R.id.viewSketchSheet);

        Log.d(TAG, strThis + "Initializing viewSignature.");

//        viewSignature.init(paint, path);
        viewSignature.init(paint, path, bitmap);

    }

    private void initFromMessage(String strThis, Serializable message) {
        if (message != null) {
            Log.d(TAG, strThis + "message class:" + message.getClass().getName() + ", message=" + StringUtils.memberValuesToString(message));
//            UiUtils.showToast(this, "message class: " + message.getClass().getName() + ", message: " + message);
//            BusHelperRmsCoding.RmsRecords rmsRecordId = (BusHelperRmsCoding.RmsRecords) message;
//            idRmsRecords = rmsRecordId.idRmsRecords;
            if (message instanceof Integer) {
                Log.d(TAG, strThis + "Case: message instanceof Integer, as expected.");
                Integer intPosition = ((Integer) message);
                int position = intPosition.intValue();
                Log.d(TAG, strThis + "Case: message instanceof Integer, as expected. position=" + position);
//                listItemSignature = BusHelperDvir.instance().getListDvirDetail().get(position);
                listItemSignature = BusHelperDvir.getListDvirDetail().get(position);
                Log.d(TAG, strThis + "Case: message instanceof Integer, retrieved  ListItemCodingDataGroup item at position=" + position
                        + ", listItemSignature=" + listItemSignature);
                idRmsRecordsParent = listItemSignature.getIdRmsRecords();

                Log.d(TAG, strThis + "Case: message instanceof Integer, idRmsRecords=" + idRmsRecordsParent);

                if (listItemSignature.getArBitmapItems() != null) {
                    Log.d(TAG, strThis + "Case: listItemSignature.getArBitmapItems() != null, "
                            + "listItemSignature.getArBitmapItems().length=" + listItemSignature.getArBitmapItems().length
                            + ", looking for bitmap.");
                    for (AdapterUtils.BitmapItem bitmapItem : listItemSignature.getArBitmapItems()) {
                        if (bitmapItem != null && bitmapItem.bitmapClass == Cadp.BITMAP_CLASS_SIGNATURE) {
                            bitmapItemSignature = bitmapItem; // by having an array of bitmaps we're allong for more than one image per list item.  Necessary?
                            bitmap = bitmapItem.bitmap;
                            Log.d(TAG, strThis + "Case: found signature BitmapItem. bitmap==null?" + (bitmap == null));
//                            idRmsRecordsSignature = bitmapItem.idRmsRecords;
                            break;
                        }
                    }

//                    if (rowSignature == null && listItemSignature.getListCodingdataRows().size() > 0)
//                        rowSignature = listItemSignature.getListCodingdataRows().get(0);
                } else
                    Log.d(TAG, strThis + "**** Warning. Case: listItemSignature.getArBitmapItems() == null, possible problem storing new bitmap.");

            }
        } else Log.d(TAG, "onCreate() Case: message from intent is null.");
    }

    private Paint initPaint() {
        Log.d(TAG, "initPaint: ");
        paint = new Paint();
        paint.setAntiAlias(true);
//        paint.setColor(Color.parseColor("#000000"));
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(18);

        return paint;
    }

    public static class LoadLinkedSignatureBitmapTask extends AsyncTask {
        private ImageView view;
        private long idRmsRecords;

        public LoadLinkedSignatureBitmapTask(ImageView view, long idRmsRecords) {
            this.view = view;
            this.idRmsRecords = idRmsRecords;
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            // Look for linked Signature record.  If found, load Bitmap into ImageView.

            return null;
        }
    }

    void saveSignature(Bitmap bitmap) {
//        bitmap = viewSignature.getNewSignatureBitmap();
//        Log.d(TAG, "save: onClick: bitmap: " + bitmap + " isEmpty: " + bitmap.toString().isEmpty());
//        if (bitmap != null) {
        long idRmsRecords = -1;

        String mobileRecordId = Rms.getMobileRecordId(Crms.R_SIGNATURE);

        Map<String, String> mapCoding = new HashMap<>();

        mapCoding.put(Crms.KEY_OBJECT_ID, "");
        mapCoding.put(Crms.KEY_OBJECT_TYPE, "");
        mapCoding.put(Crms.MOBILERECORDID, mobileRecordId);

        String documentTitle = "";
        if (signatureType.contains("Driver")) {
            documentTitle = "DriverSignature";
        } else {
            documentTitle = "MechanicSignature";
        }
        Log.d(TAG, "saveSignature: documentTitle: "+documentTitle);
        mapCoding.put(Crms.DOCUMENT_TITLE, documentTitle);
        mapCoding.put(Crms.ISSUING_AUTHORITY, "");
        mapCoding.put(Crms.EXPIRATION_DATE, "");
        mapCoding.put(Crms.SIGNATURE_DATE, "");
        Log.d(TAG, "saveSignature: signatureName: " + signatureName);
        mapCoding.put(Crms.SIGNATURE_NAME, signatureName);
//            mapCoding.put(Crms.SIGNATURE_NAME, "Naeem And Dragos");
        mapCoding.put(Crms.DESCRIPTION, "");
//            String itemType = activity.equals("LOG_BOOK_ACTIVITY") ? "ELD Driver Signature" : "Driver Skill Performance Evaluation Form Signature";
        String itemType = signatureType;
        Log.d(TAG, "saveSignature: itemType: " + itemType);
        mapCoding.put(Crms.ITEMTYPE, itemType);
        mapCoding.put(Crms.DOCUMENT_TYPE, "documenttype");
        mapCoding.put(Crms.DOCUMENT_DATE, DateUtils.getDateTime(new Date(), DateUtils.FORMAT_DATE_YYYY_MM_DD));
        mapCoding.put(Crms.REVIEWED_BY, "");
        mapCoding.put(Crms.REVIEWED_DATE, "");

        // mapCoding.put(Crms.KEY_OBJECT_ID, objectId);
        mapCoding.put(Crms.PARENT_OBJECTID, objectId);
        mapCoding.put(Crms.PARENT_OBJECTTYPE, objectType);


        int syncStatus = 0;

        BusHelperRmsCoding.RmsRecordCoding rec = new BusHelperRmsCoding.RmsRecordCoding(idRmsRecords, objectId, objectType, mobileRecordId, mapCoding, syncStatus);

        listHeader.add(rec);

        new SignatureActivity.HttpPostSignature().execute();

//        } else {
//            Log.d(TAG, "save: onClick:  textViewSave.setOnClickListener.onClick()  ***** Error. bitmapItemSignature is null, cannot set bitmap.");
//        }
    }


    public class HttpPostSignature extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "HttpPostSignature: onPreExecute: ");
            super.onPreExecute();
//            progress.setVisibility(View.VISIBLE);
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(TAG, "HttpPostSignature: doInBackground: ");
            String result = "";

            try {
                String json = RmsHelperDvir.setSignatures(listHeader);
                if (json == null || json.equalsIgnoreCase("[]")) {
                    Log.d(TAG, "HttpPostSignature: doInBackground: json is null or empty: so Error Upload Signature");
                    return "Error Upload Signature";
                }

                JSONArray response = new JSONArray(json);
                for (int i = 0; i < response.length(); i++) {

                    int lObjectId = response.getJSONObject(i).getInt("LobjectId");
                    String objectType = response.getJSONObject(i).getString("objectType");
                    Log.d(TAG, "HttpPostSignature: doInBackground: lObjectId: " + lObjectId + " objectTYpe: " + objectType);

                    String ext = BusHelperFuelReceipts.getFileExtByRecordType(Crms.R_SIGNATURE);
                    String uploadFileName = "setRecordContent_android_signature_" + BusinessRules.instance().getAuthenticatedUser().getClientLogon()
                            + "_" + DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_ISO_SSS_Z_FOR_NAME)
                            + "." + ext;

                    Log.d(TAG, "HttpPostSignature: doInBackground: uploadFileName: " + uploadFileName);

                    byte[] bitmapBytes = viewSignature.getSignatureByte();
                    if (bitmapBytes != null) {
                        HttpClient.HttpReturnInfo returnInfo = Rms.setRecordContent(uploadFileName,
                                "" + lObjectId, objectType, "-1", "+", "+", bitmapBytes);

                        if (returnInfo.responseCode < 300) {
                            result = "Signature Sent successfully";
                        } else {
                            result = "Error Upload Signature";
                        }
                    } else {
                        result = "Error Upload Signature";
                    }
                }
            } catch (Exception e) {
                Log.d(TAG, "HttpPostSignature: doInBackground: exception: " + e.getMessage());
                e.printStackTrace();
            }

            Log.d(TAG, "HttpPostSignature: doInBackground: return result: " + result);
            return result;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d(TAG, "HttpPostSignature: onPostExecute: ");

            setResult(RESULT_OK);
            finish();

//            onBackPressed();
//            Intent intent = new Intent(SignatureActivity.this, SPEFieldsActivity.class);
//            byte[] bitmapBytes = viewSignature.getSignatureByte();
//            intent.putExtra(EXTRA_SIGNATURE, bitmapBytes);
//            setResult(RESULT_OK, intent);
//            finish();
        }
    }
}