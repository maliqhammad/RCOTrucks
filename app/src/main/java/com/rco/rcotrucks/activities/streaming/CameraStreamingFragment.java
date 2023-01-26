package com.rco.rcotrucks.activities.streaming;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

//import com.longdo.mjpegviewer.MjpegView;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.BusinessRules;

public class CameraStreamingFragment extends Fragment {
    private BusinessRules rules = BusinessRules.instance();
    private TextView streamingNotWorking;
//    private MjpegView viewer;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_camera_streaming, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);
            streamingNotWorking = view.findViewById(R.id.streaming_not_working);
//            viewer = (MjpegView) view.findViewById(R.id.mjpegview);

            String streamingUrl = rules.getStreamingUrl(); //https://sre-vision.com/video_feed
            if (streamingUrl == null || streamingUrl.isEmpty()) {
                streamingNotWorking.setVisibility(View.VISIBLE);
                return;
            }

//            viewer.setMode(MjpegView.MODE_BEST_FIT);
//            viewer.setAdjustHeight(true);
//            viewer.setSupportPinchZoomAndPan(false);
//            viewer.setUrl(streamingUrl);
//            viewer.startStream();
        } catch (Throwable throwable) {
            if (streamingNotWorking != null)
                streamingNotWorking.setVisibility(View.VISIBLE);
        }
    }
}

