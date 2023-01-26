package com.rco.rcotrucks.activities.logbook.sign;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.dvir.RmsHelperDvir;
import com.rco.rcotrucks.activities.forms.driverapplication.DAFieldsActivity;
import com.rco.rcotrucks.activities.forms.drivermedicalform.DMFieldsActivity;
import com.rco.rcotrucks.activities.forms.driverviolations.DVFieldsActivity;
import com.rco.rcotrucks.activities.forms.employmentrecord.DERFieldsActivity;
import com.rco.rcotrucks.activities.forms.spe.SPEFieldsActivity;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.businesslogic.rms.Rms;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.HttpClient;
import com.rco.rcotrucks.utils.UiUtils;
import com.rco.rcotrucks.views.SignatureImageView;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_SIGNATURE;

public class SignActivity extends AppCompatActivity {
    private static final String TAG = "SignActivity";

    private Paint paint;
    private SignatureImageView viewSignature;
    private Path path;
    Button buttonClear;
    private ImageView btnBack;
    private TextView textViewSave;
    private Bitmap bitmap = null;
    List<BusHelperRmsCoding.RmsRecordCoding> listHeader = new ArrayList<>();

    protected BusinessRules businessRules = BusinessRules.instance();
    private String userFullName = "";
    ProgressBar progress;
    private String activity = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        String strThis = "onCreate(), ";
        setContentView(R.layout.activity_signature_imageview_layout);

        Intent intent = getIntent();
        final String objectId = intent.getStringExtra(Cadp.EXTRA_OBJECT_ID);
        final String objectType = intent.getStringExtra(Cadp.EXTRA_OBJECT_TYPE);

        activity = intent.getStringExtra(Cadp.EXTRA_LASTACTIVITY);
        final Bitmap signatureBitmap = intent.getParcelableExtra(EXTRA_SIGNATURE);
        if (signatureBitmap != null) {
            viewSignature.setImageBitmap(signatureBitmap);
        }

        btnBack = findViewById(R.id.btn_back);
        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        textViewSave = findViewById(R.id.textViewSave);
        progress = findViewById(R.id.progress);
        viewSignature = (SignatureImageView) findViewById(R.id.viewSketchSheet);

        userFullName = businessRules.getAuthenticatedUser().getFullQualifiedName();

        textViewSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                bitmap = viewSignature.getNewSignatureBitmap();
                Log.d(TAG, "save: onClick: bitmap: " + bitmap + " isEmpty: " + bitmap.toString().isEmpty());
                if (bitmap != null) {
                    long idRmsRecords = -1;

                    String mobileRecordId = Rms.getMobileRecordId(Crms.R_SIGNATURE);

                    Map<String, String> mapCoding = new HashMap<>();

                    mapCoding.put(Crms.KEY_OBJECT_ID, "");
                    mapCoding.put(Crms.KEY_OBJECT_TYPE, "");
                    mapCoding.put(Crms.MOBILERECORDID, mobileRecordId);

                    Log.d(TAG, "DOCUMENT_TITLE: onClick: textViewSave: mapCoding.put(Crms.DOCUMENT_TITLE, DriverSignature)");
                    mapCoding.put(Crms.DOCUMENT_TITLE, "DriverSignature");
                    mapCoding.put(Crms.ISSUING_AUTHORITY, "");
                    mapCoding.put(Crms.EXPIRATION_DATE, "");
                    mapCoding.put(Crms.SIGNATURE_DATE, "");
                    mapCoding.put(Crms.SIGNATURE_NAME, userFullName);
                    mapCoding.put(Crms.DESCRIPTION, "");
                    String itemType = activity.equals("LOG_BOOK_ACTIVITY") ? "ELD Driver Signature" : "Driver Skill Performance Evaluation Form Signature";
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

                    new HttpPostSignature().execute();

                } else {
                    Log.d(TAG, "save: onClick:  textViewSave.setOnClickListener.onClick()  ***** Error. bitmapItemSignature is null, cannot set bitmap.");
                }
            }
        });


        Log.d(TAG, strThis + "Assigning relative layout.");

        Log.d(TAG, strThis + "Assigning clear button.");
        buttonClear = (Button) findViewById(R.id.buttonClearSketch);

        buttonClear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "buttonClear.onClick() Start. viewSignature.getDrawable()==null?" + (viewSignature.getDrawable() == null));
                viewSignature.clear();
                Log.d(TAG, "buttonClear.onClick() End. viewSignature.getDrawable()==null?" + (viewSignature.getDrawable() == null));
            }
        });

        Log.d(TAG, strThis + "Initializing paint and path objects.");
        paint = initPaint();
        path = new Path();

        Log.d(TAG, strThis + "Assigning viewSignature.");


        Log.d(TAG, strThis + "Initializing viewSignature.");

        viewSignature.init(paint, path, bitmap);

    }

    private Paint initPaint() {
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setColor(Color.BLUE);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(18);

        return paint;
    }

    public class HttpPostSignature extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            Log.d(TAG, "HttpPostSignature: onPreExecute: ");
            super.onPreExecute();
            progress.setVisibility(View.VISIBLE);
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
            progress.setVisibility(View.GONE);
            if (activity.equals("FORM_ACTIVITY")) {

                Intent intent = new Intent(SignActivity.this, SPEFieldsActivity.class);
                byte[] bitmapBytes = viewSignature.getSignatureByte();
                intent.putExtra(EXTRA_SIGNATURE, bitmapBytes);
                setResult(RESULT_OK, intent);
                finish();
            } else if (activity.equals("FORM_ACTIVITY_DER")) {

                Intent intent = new Intent(SignActivity.this, DERFieldsActivity.class);
                byte[] bitmapBytes = viewSignature.getSignatureByte();
                intent.putExtra(EXTRA_SIGNATURE, bitmapBytes);
                setResult(RESULT_OK, intent);
                finish();
            } else if (activity.equals("FORM_ACTIVITY_DM")) {

                Intent intent = new Intent(SignActivity.this, DMFieldsActivity.class);
                byte[] bitmapBytes = viewSignature.getSignatureByte();
                intent.putExtra(EXTRA_SIGNATURE, bitmapBytes);
                setResult(RESULT_OK, intent);
                finish();
            } else if (activity.equals("FORM_ACTIVITY_DV")) {
                Intent intent = new Intent(SignActivity.this, DVFieldsActivity.class);
                byte[] bitmapBytes = viewSignature.getSignatureByte();
                intent.putExtra(EXTRA_SIGNATURE, bitmapBytes);
                setResult(RESULT_OK, intent);
                finish();
            } else if (activity.equals("FORM_ACTIVITY_DA")) {
                Intent intent = new Intent(SignActivity.this, DAFieldsActivity.class);
                byte[] bitmapBytes = viewSignature.getSignatureByte();
                intent.putExtra(EXTRA_SIGNATURE, bitmapBytes);
                setResult(RESULT_OK, intent);
                finish();
            } else {
                UiUtils.showToast(SignActivity.this, "" + result);
            }
            finish();
        }
    }
}
