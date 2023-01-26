package com.rco.rcotrucks.activities.drive;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.GlideApp;

public class DriveFragmentTablet extends DriveFragmentBase {
    private static final String TAG = DriveFragmentTablet.class.getSimpleName();
    private boolean isGaugesPanelOpen = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_drive_tablet, container, false);
        Log.d(TAG, "onCreateView: ");
        return view;
    }

    @Override
    void initFragment(View view) {
        Log.d(TAG, "initFragment: ");
        closeOpenPanelBtn.setVisibility(View.VISIBLE);
    }

    @Override
    public void onClick(View v) {
        super.onClick(v);

        switch (v.getId()) {
            case R.id.btn_closeopen_panel:
                gaugesContainer.setVisibility(isGaugesPanelOpen ? View.GONE : View.VISIBLE);
//                Oct 19, 2022  -   Layout updated so added new icons to close or open side panel
//                closeOpenPanelBtn.setImageDrawable(getResources().getDrawable(isGaugesPanelOpen ? R.drawable.collapse_panel2 : R.drawable.collapse_panel1));
                closeOpenPanelBtn.setImageDrawable(getResources().getDrawable(isGaugesPanelOpen ? R.drawable.ic_baseline_arrow_right_24 : R.drawable.ic_baseline_arrow_left_24));
                isGaugesPanelOpen = !isGaugesPanelOpen;
                break;
        }
    }
}
