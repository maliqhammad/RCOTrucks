package com.rco.rcotrucks.activities.settings;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import com.rco.rcotrucks.BuildConfig;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.logbook.MonitorService;
import com.rco.rcotrucks.activities.logbook.dialog.ELDMonitorServiceDialog;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.TruckLogHeader;
import com.rco.rcotrucks.businesslogic.rms.User;
import com.rco.rcotrucks.dialog.CoDriverAuthentication;
import com.rco.rcotrucks.dialog.VersionDetailDialog;
import com.rco.rcotrucks.utils.UiUtils;
import com.toptoche.searchablespinnerlibrary.SearchableSpinner;

import java.util.ArrayList;

public class SettingsFragment extends Fragment {
    private static final String TAG = SettingsFragment.class.getSimpleName();
    BusinessRules businessRules = BusinessRules.instance();
    ConstraintLayout driverLayout, coDriverLayout, driverGroupLayout, eldGroupLayout, coDriverGroupLayout, mapControlsGroupLayout;
    ;
    TextView firstName, lastName, zipCode, state, exempt, eldName, eldId, carrier, truckVin, rules, odometer,
            driver, coDriver, driverSection, eldSection, codriverSection, mapControlSection;
    ImageView informationIcon;
    SearchableSpinner driverSpinner;
    Boolean isTablet = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
//        Dec 12, 2022  -   We are setting appbar widgets at MainMenuActivity so no need to write/override here
//        ((MainMenuActivity) getActivity()).setActionBarTitle(getString(R.string.settings_title));

        initView(view);
        fillSettingData();
        setListener();

        setDriverAdapter();
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    private void initView(View view) {
        firstName = view.findViewById(R.id.first_name);
        lastName = view.findViewById(R.id.last_name);
        zipCode = view.findViewById(R.id.zip_code);
        state = view.findViewById(R.id.state);
        exempt = view.findViewById(R.id.exempt);

        eldName = view.findViewById(R.id.eld_name);
        eldId = view.findViewById(R.id.eld_id);
        carrier = view.findViewById(R.id.carrier);
        truckVin = view.findViewById(R.id.truck_vin);
        rules = view.findViewById(R.id.rules);
        odometer = view.findViewById(R.id.odometer);

        driver = view.findViewById(R.id.driver);
        coDriver = view.findViewById(R.id.coDriver);
        driverLayout = view.findViewById(R.id.driverLayout);
        coDriverLayout = view.findViewById(R.id.coDriverLayout);
        informationIcon = view.findViewById(R.id.infoIcon);

        driverSpinner = view.findViewById(R.id.coDriverSpinner);

        isTablet = getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {

            driverGroupLayout = view.findViewById(R.id.driverGroupLayout);
            eldGroupLayout = view.findViewById(R.id.eldGroupLayout);
            coDriverGroupLayout = view.findViewById(R.id.coDriverLayout);
            mapControlsGroupLayout = view.findViewById(R.id.mapControlsLayout);

            driverSection = view.findViewById(R.id.driver_section);
            eldSection = view.findViewById(R.id.eld_section);
            codriverSection = view.findViewById(R.id.codriver_section);
            mapControlSection = view.findViewById(R.id.mapControls);
        }

    }

    private void fillSettingData() {
        User user = businessRules.getAuthenticatedUser();
        firstName.setText(user.getFirstName());
        lastName.setText(user.getLastName());
        zipCode.setText(user.getZipCode());
        state.setText(user.getState());

        String registrationName = businessRules.getSetting("database.ELD Registration Name");
        if (registrationName != null)
            eldName.setText(registrationName);

        String registrationID = businessRules.getSetting("database.ELD Registration Id");
        if (registrationID != null)
            eldId.setText(registrationID);

        TruckLogHeader truckLogHeader = businessRules.getOpenTruckLogHeader();
        if (truckLogHeader != null) {

            if (truckLogHeader.getTruckLogDetails().size() > 0 && truckLogHeader.getTruckLogDetails().get(0).Carrier != null) {
                carrier.setText(truckLogHeader.getTruckLogDetails().get(0).Carrier);
            }

            if (truckLogHeader.getTruckLogDetails().size() > 0 && truckLogHeader.getTruckLogDetails().get(0).VIN != null) {
                truckVin.setText(truckLogHeader.getTruckLogDetails().get(0).VIN);
            }

            if (businessRules.getBleParameter("Odometer") == null) {
                odometer.setText("Odometer");
            } else {
                odometer.setText("" + businessRules.getBleParameter("Odometer"));
            }

            if (truckLogHeader.Rule != null) {
                rules.setText(truckLogHeader.Rule);
            }
        }
    }

    void setListener() {

        driverLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setBottomBarForDriverLayout();
            }
        });

        coDriverLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: coDriverLayout: ");
                setBottomBarForCoDriverLayout();
                openCoAuthenticationDialog();
            }
        });

        coDriver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: coDriver: ");
                setBottomBarForCoDriverLayout();
                openCoAuthenticationDialog();
            }
        });

        informationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                VersionDetailDialog versionDetailDialog = new VersionDetailDialog(getContext());
                versionDetailDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                versionDetailDialog.show();
            }
        });

        if (isTablet) {

            driverSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
//                    driverSection.setBackground(getResources().getDrawable(R.drawable.background_curved_default_dark_mode));
                    driverGroupLayout.setVisibility(View.VISIBLE);
                    eldGroupLayout.setVisibility(View.GONE);
                    coDriverGroupLayout.setVisibility(View.GONE);
                    mapControlsGroupLayout.setVisibility(View.GONE);
                }
            });

            eldSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    eldGroupLayout.setVisibility(View.VISIBLE);
                    driverGroupLayout.setVisibility(View.GONE);
                    coDriverGroupLayout.setVisibility(View.GONE);
                    mapControlsGroupLayout.setVisibility(View.GONE);
                }
            });

            codriverSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    coDriverGroupLayout.setVisibility(View.VISIBLE);
                    driverGroupLayout.setVisibility(View.GONE);
                    eldGroupLayout.setVisibility(View.GONE);
                    mapControlsGroupLayout.setVisibility(View.GONE);
                }
            });

            mapControlSection.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mapControlsGroupLayout.setVisibility(View.VISIBLE);
                    coDriverGroupLayout.setVisibility(View.GONE);
                    driverGroupLayout.setVisibility(View.GONE);
                    eldGroupLayout.setVisibility(View.GONE);
                }
            });
        }

    }


    void setBottomBarForDriverLayout() {
        Log.d(TAG, "setBottomBarForDriverLayout: ");
        driverLayout.setBackground(getResources().getDrawable(R.drawable.black_curved_background_two));
        coDriverLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background));

        driver.setTextColor(getResources().getColor(R.color.white));
        coDriver.setTextColor(getResources().getColor(R.color.black));
    }

    void setBottomBarForCoDriverLayout() {
        Log.d(TAG, "setBottomBarForCoDriverLayout: ");
        driverLayout.setBackground(getResources().getDrawable(R.drawable.white_curved_background));
        coDriverLayout.setBackground(getResources().getDrawable(R.drawable.black_curved_background_two));

        driver.setTextColor(getResources().getColor(R.color.black));
        coDriver.setTextColor(getResources().getColor(R.color.white));
    }

    void openCoAuthenticationDialog() {
        Log.d(TAG, "openCoAuthenticationDialog: ");
        CoDriverAuthentication coDriverAuthentication = new CoDriverAuthentication(getContext(),
                new CoDriverAuthentication.CoDriverAuthenticationInterface() {
                    @Override
                    public void onAuthentication(String username, String password) {
                        Toast.makeText(getContext(), "Working...", Toast.LENGTH_SHORT).show();
                        setBottomBarForDriverLayout();
                    }

                    @Override
                    public void onCancelPressed() {
                        setBottomBarForDriverLayout();
                    }
                });
        coDriverAuthentication.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        coDriverAuthentication.show();
    }


    void setDriverAdapter() {

        driverSpinner.setAdapter(new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, getContext().getResources().getStringArray(R.array.driver_list)));
        driverSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
//                hideSoftKeyboard(getContext(), billerName);
                if (adapterView != null) {
                    ((TextView) adapterView.getChildAt(0)).setTextColor(getResources().getColor(R.color.black_and_white));
                }
//                if (i == 0) {
//                    Toast.makeText(AddExpenseWizard.this, "Please select a category.", Toast.LENGTH_SHORT).show();
//                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
//                hideSoftKeyboard(getContext(), billerName);
                Log.d(TAG, "onNothingSelected: ");
            }
        });
        driverSpinner.setTitle("Search Driver");
    }

}
