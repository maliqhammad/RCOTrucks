package com.rco.rcotrucks.activities;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.BusinessRules;

import java.util.HashMap;

public abstract class BaseCoordinatorFragment extends AppBaseFragment {
    private static final String TAG = "BaseCoordinatorFragment";

    protected int getSecondaryToolbarId(){
        return 0;
    }

    protected int getContentLayoutId(){
        return 0;
    }

    protected int getBottomLayoutId() {
        return 0;
    }

    protected boolean isShowHomeButton() {
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_base_coordinator, container, false);

        try {
            ViewStub tabViewStub = view.findViewById(R.id.stub_tabbar);

            if (getSecondaryToolbarId() > 0) {
                tabViewStub.setLayoutResource(getSecondaryToolbarId());
                tabViewStub.inflate();
            }

            ViewStub contentViewStub = view.findViewById(R.id.stub_content);

            if (getContentLayoutId() > 0) {
                Log.d(TAG, "Calling contentViewStub.setLayoutResource(getContentLayoutId() because getContentLayoutId()="
                        + getContentLayoutId() + ", > 0");
                contentViewStub.setLayoutResource(getContentLayoutId());
                contentViewStub.inflate();
            } else Log.d(TAG, "Skipped contentViewStub.setLayoutResource(getContentLayoutId() because getContentLayoutId()="
                + getContentLayoutId() + ", not > 0");

            ViewStub stub_bottom_layout = view.findViewById(R.id.stub_fixed_bottom_layout);

            if (getBottomLayoutId() > 0) {
                stub_bottom_layout.setLayoutResource(getBottomLayoutId());
                stub_bottom_layout.inflate();
            }

           // onActivityCreated();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        return view;
    }


    //region Tags general activity management

    private HashMap<String, Object> tags = new HashMap<>();

    public Object getTag(String key) {
        return tags.get(key);
    }

    public void setTag(String key, Object value) {
        if (tags.containsKey(key))
            tags.remove(key);

        tags.put(key, value);
    }

    //endregion

//    DIAGNOSTIC_POWER_DATA_DIAGNOSTIC(1),                    // Power data diagnostic
//    DIAGNOSTIC_ENGINE_SYNCHRONIZATION(2),                   // Engine synchronization data diagnostic
//    DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS(3),           // Missing required data elements data diagnostic
//    DIAGNOSTIC_DATA_TRANSFER(4),                            // Data transfer data diagnostic
//    DIAGNOSTIC_UNIDENTIFIED_DRIVING_RECORDS(5),             // Unidentified driving records data diagnostic
//    DIAGNOSTIC_OTHER(6),                                    // Other
    public BusinessRules.EventCode getMalfunctionEventCodeFromCode(String malfunctionCode) {
        if (malfunctionCode.equalsIgnoreCase("1")) {
            return BusinessRules.EventCode.DIAGNOSTIC_POWER_DATA_DIAGNOSTIC;
        }

        if (malfunctionCode.equalsIgnoreCase("2")) {
            return BusinessRules.EventCode.DIAGNOSTIC_ENGINE_SYNCHRONIZATION;
        }

        if (malfunctionCode.equalsIgnoreCase("3")) {
            return BusinessRules.EventCode.DIAGNOSTIC_MISSING_REQUIRED_DATA_ELEMENTS;
        }

        if (malfunctionCode.equalsIgnoreCase("4")) {
            return BusinessRules.EventCode.DIAGNOSTIC_DATA_TRANSFER;
        }

        if (malfunctionCode.equalsIgnoreCase("5")) {
            return BusinessRules.EventCode.DIAGNOSTIC_UNIDENTIFIED_DRIVING_RECORDS;
        }

        if (malfunctionCode.equalsIgnoreCase("6")) {
            return BusinessRules.EventCode.DIAGNOSTIC_OTHER;
        }

        return BusinessRules.EventCode.NOT_SET;
    }


    public void showDialog_Without_Listener(final String title, final String message, final String buttonText) {

        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {
                AlertDialog.Builder adbb = new AlertDialog.Builder(getContext());
                adbb.setIcon(R.drawable.app_truck_icon);
                adbb.setTitle(title);
                if (message != null)
                    adbb.setMessage(message);
                adbb.setPositiveButton(buttonText, null);
                adbb.show();
            }
        });
    }

    public void showDialog_With_Listener(final String title, final String message, final String positiveBtnText, final String negativeBtnText, final DialogInterface.OnClickListener listener) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(new Runnable() {
            public void run() {

                AlertDialog.Builder adb = new AlertDialog.Builder(getContext());
                adb.setIcon(R.drawable.app_truck_icon);
                adb.setTitle(title);
                adb.setMessage(message);
                if (positiveBtnText != null && !positiveBtnText.equals("")) {
                    adb.setPositiveButton(positiveBtnText, listener);
                }
                if (negativeBtnText != null && !negativeBtnText.equals("")) {
                    adb.setNegativeButton(negativeBtnText, listener);
                }
                adb.show();
            }
        });
    }


}
