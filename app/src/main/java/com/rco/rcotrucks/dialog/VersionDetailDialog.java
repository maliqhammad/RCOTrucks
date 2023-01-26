package com.rco.rcotrucks.dialog;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.SessionManagement;

import java.util.Date;

public class VersionDetailDialog extends Dialog {

    private static final String TAG = VersionDetailDialog.class.getSimpleName();
    ProgressDialog progressDialog;
    SessionManagement sessionManagement;
    public Context context;
    public Dialog dialog;
    TextView versionName, cancel;
    String buildCode = "", buildDate = "", buildInfo = "";

    public VersionDetailDialog(@NonNull Context context) {
        super(context);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_dialog_version_detail);
        getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        setIds();
        initialize();
        setListener();
    }

    void setIds() {
        versionName = findViewById(R.id.versionName);
        cancel = findViewById(R.id.cancel);
    }

    void initialize() {

        progressDialog = new ProgressDialog(context);
        sessionManagement = new SessionManagement(context);

        buildCode = context.getResources().getString(R.string.build_code) + " " + BuildConfig.VERSION_CODE;
        Date apkDate = new Date(BuildConfig.TIMESTAMP);

//        Sep 21, 2022  -   Roy Recommended
//        Use 12 hour format and show AM or PM
//        Example Date: Sep 20, 2022 Time 06:12 AM PST
//        Log.d(TAG, "initialize: 2: " + apkDate.toGMTString());            //      21 Sep 2022 10:04:02 GMT
//        Log.d(TAG, "initialize: 3: " + apkDate.toLocaleString());         //      Sep 21, 2022 3:04:02 PM


        String[] splitAPKDateBySpace = apkDate.toLocaleString().split(" ");
        String[] splitGMTStringBySpace = apkDate.toGMTString().split(" ");
        String apkExactDate = "", apkTime = "";
        if (splitAPKDateBySpace.length > 2) {
            apkExactDate = splitAPKDateBySpace[0] + " " + splitAPKDateBySpace[1] + " " + splitAPKDateBySpace[2];
        }

        if (splitAPKDateBySpace.length > 4) {

            String[] splitAPKTimeByColon = splitAPKDateBySpace[3].split(":");
//            apkTime = splitAPKTimeByColon[0] + ":" + splitAPKTimeByColon[1] + " " + splitAPKDateBySpace[4];

            String[] splitGMTStringTimeByColon = splitGMTStringBySpace[3].split(":");
            apkTime = splitGMTStringTimeByColon[0] + ":" + splitGMTStringTimeByColon[1] + " " + splitAPKDateBySpace[4] + " " + splitGMTStringBySpace[4];
        }

        buildDate = context.getResources().getString(R.string.build_date);
        buildInfo = buildCode + "\n" + buildDate + " " + apkExactDate + " Time " + apkTime;
        versionName.setText(buildInfo);
    }

    void setListener() {
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }

}

