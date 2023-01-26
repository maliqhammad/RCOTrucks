package com.rco.rcotrucks.activities.ifta;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.util.Calendar;

public class IftaFragment extends Fragment {
    public static final String TAG = "IftaFragment";

    Button buttonTest;
    Button buttonInit;
    EditText editTextViewLat;
    EditText editTextViewLon;
    TextView textViewResults;
    TextView textViewLastIftaCheck;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ifta2, container, false);

        editTextViewLat = view.findViewById(R.id.editTextLatitude);
        editTextViewLon = view.findViewById(R.id.editTextLongitude);
        textViewResults = view.findViewById(R.id.textViewResults);
        buttonInit = view.findViewById(R.id.buttonInit);
        buttonTest = view.findViewById(R.id.buttonTest);
        textViewLastIftaCheck = view.findViewById(R.id.textViewLastIftaCheck);
        String now = DateUtils.getDateTime(BusHelperIfta.LsystimeLastIftaCheck, DateUtils.FORMAT_ISO_SSS_Z);
        textViewLastIftaCheck.setText(now);

        buttonInit.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
//                BusHelperIfta.initStateProvAbbrev(IftaActivity.this);
//                BusHelperIfta.parseStateProvinceXml(IftaActivity.this, null);
//                BusHelperIfta.initStateProvGeoData(IftaActivity.this);
                BusHelperIfta.initGeoCode(getActivity());
//                    BusHelperIfta.testAddress(IftaActivity.this, 45.3023, -71.0801, 10);
            }
        });


        buttonTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String now = DateUtils.getDateTime(BusHelperIfta.LsystimeLastIftaCheck, DateUtils.FORMAT_ISO_SSS_Z);
                textViewLastIftaCheck.setText(now);
//
//                double lat = Double.parseDouble(editTextViewLat.getText().toString());
//                double lon = Double.parseDouble(editTextViewLon.getText().toString());
//                double lonx = BusHelperIfta.lx(lon);
//                Log.d(TAG, "buttonTest.setOnClickListener.onClick() lat=" + lat + ", lon=" + lon + ", lonx=" + lonx);
//
//                if (BusHelperIfta.getListJur() == null) UiUtils.showToast(getActivity(), "Please initialize first with Init button.");
//                else {
//                    BusHelperIfta.testGeoCode(getActivity(), lat, lonx, BusHelperIfta.getListJur(), textViewResults);
//                }
            }
        });

        return view;
    }
}