package com.rco.rcotrucks.activities.dashboard;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.activities.drive.gauges.GaugesEventView;

public class DashboardFragment extends Fragment {

    //    private static final String TAG = DashboardFragment.class.getSimpleName();
    private static final String TAG = "DashboardFrag: gauges:";
    private static final int ACCESS_FINE_LOCATION_CODE = 1001;

    protected BusinessRules rules = BusinessRules.instance();

    ConstraintLayout loadingPanel;
    TextView eventAbbreviation, eventName, eventTime, loadingFeedbackText;
    GaugesEventView gaugesEventView;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_dashboard, container, false);
        Log.d(TAG, "onCreateView: ");

        setIds(view);
        initialize();
        setListener();

        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated: ");

        if (!hasGPSPermission()) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_FINE_LOCATION_CODE);
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                final Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        //your code here
                        gaugesEventView.updateStations(rules.getPhoneLastBestLocation(getActivity()), loadingPanel, getActivity());
                    }
                }, 200);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(TAG, "onPause: GaugesHandler: ");
        gaugesEventView.stopThreadWhichCalculateDistance();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d(TAG, "onStop: ");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(TAG, "onStart: ");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored: ");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivityCreated: ");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }

    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        Log.d(TAG, "onAttach: ");
    }

    @Override
    public void onAttachFragment(@NonNull Fragment childFragment) {
        super.onAttachFragment(childFragment);
        Log.d(TAG, "onAttachFragment: ");
    }

    void setIds(View view) {
        gaugesEventView = view.findViewById(R.id.gauge_view);

        eventAbbreviation = view.findViewById(R.id.event_abbreviation);
        eventName = view.findViewById(R.id.event_name);
        eventTime = view.findViewById(R.id.event_time);
        loadingPanel = view.findViewById(R.id.loading_panel);
        loadingFeedbackText = view.findViewById(R.id.loading_feedback_text);
    }

    void initialize() {
        eventName.setText(gaugesEventView.getEventTitle());
        eventAbbreviation.setText(gaugesEventView.getEventAbbreviation());

        loadingFeedbackText.setText("Loading...");
        loadingPanel.setVisibility(View.GONE);
    }

    void setListener() {

        gaugesEventView.setEventTimeListener(new GaugesEventView.CurrentEventTime() {
            @Override
            public void updateCurrentEventTime(long time) {
                String formattedTime = DateUtils.convertMinutesToHours(time, "Hrs", "mn");

                eventTime.setText(formattedTime);
            }
        });

        gaugesEventView.setFinishBreakListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {

                    ((MainMenuActivity) getActivity()).openDriverFragment();
                } catch (Throwable throwable) {
                    if (throwable != null)
                        throwable.printStackTrace();
                }
            }
        });

        loadingPanel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingPanel.setVisibility(View.GONE);
            }
        });

    }

    private boolean hasGPSPermission() {
        Log.d(TAG, "hasGPSPermission: ");
        return ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d(TAG, "onRequestPermissionsResult: ");
        Log.e("Rest", "" + requestCode);
        if (requestCode == ACCESS_FINE_LOCATION_CODE) {
            gaugesEventView.updateStations(rules.getPhoneLastBestLocation(getActivity()), loadingPanel, getActivity());
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }


}
