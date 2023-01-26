package com.rco.rcotrucks.activities.drive;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.BaseCoordinatorFragment;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.adapters.MalfunctioListAdapter;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;

import java.util.ArrayList;

public class DriveMalfunctionListFragment extends BaseCoordinatorFragment implements AdapterView.OnItemClickListener {
    private static final String TAG = DriveMalfunctionListFragment.class.getSimpleName();
    private BusinessRules rules = BusinessRules.instance();
    private ArrayList<EldEvent> malfunctions;
    private ArrayList<EldEvent> malfunctionsActive;
    private MalfunctioListAdapter adapter;
    private ListView listView;
    private EditText searchBox;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        try {
            super.onViewCreated(view, savedInstanceState);

            ((MainMenuActivity) getActivity()).setActionBarTitle(getString(R.string.malfunctionslist_title));

            searchBox = getActivity().findViewById(R.id.et_search);

            ImageView searchClear = getActivity().findViewById(R.id.iv_clear_search);
            searchClear.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    searchBox.setText("");
                }
            });

            searchBox.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    loadListView();
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                }
            });

            TextView btnMalfunctionClose = getActivity().findViewById(R.id.btn_malfunction_close);
            btnMalfunctionClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        btnMalfunctionClose.setEnabled(false);
                        btnMalfunctionClose.setTextColor(getResources().getColor(R.color.gray));

                        ((MainMenuActivity) getActivity()).clearActionBarTitle();
                        ((MainMenuActivity) getActivity()).findViewById(R.id.login).setVisibility(View.VISIBLE);

                        getFragmentManager().beginTransaction().remove(DriveMalfunctionListFragment.this).commit();
                    } catch (Throwable throwable) {
                        if (throwable != null)
                            throwable.printStackTrace();
                    }
                }
            });

            listView = view.findViewById(R.id.list);
            loadListView();
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();

            getActivity().getSupportFragmentManager().popBackStack();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadListView();
    }

    @Override
    protected int getContentLayoutId() {
        return R.layout.activity_diagnostics;
    }

    @Override
    protected int getSecondaryToolbarId() {
        return R.layout.search_bar;
    }

//    @Override
//    protected int getToolbarId() {
//        return R.layout.activity_toolbar;
//    }

    @Override
    protected boolean isShowHomeButton() {
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    private void loadListView() {
        String searchText = searchBox.getText().toString();
        malfunctions = rules.getLatestMalfunctions(searchText);
        malfunctionsActive = rules.getLatestMalfunctionActive();
        adapter = new MalfunctioListAdapter(getActivity(), malfunctions, malfunctionsActive,
                new MalfunctioListAdapter.MalfunctionInterface() {
                    @Override
                    public void onItemClick(EldEvent eldEvent) {
                        Log.d(TAG, "onItemClick: ");

                        showDialog_With_Listener("Malfunction", "Are you sure, you want to clear this malfunction event.",
                                "Yes", "No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i) {

                                        if (i == Dialog.BUTTON_POSITIVE) {

                                            rules.recordDiagnosticOrMalfunctionEvent(getActivity(),
                                                    BusinessRules.EventCode.ELD_MALFUNCTION_CLEARED,
                                                    getMalfunctionEventCodeFromCode(eldEvent.MalfunctionDiagnosticCode),
                                                    eldEvent.MalfunctionDiagnosticDescp, true);
                                            loadListView();
                                        }
                                    }
                                });
                    }
                });

        if (listView != null) {
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(this);
        }
    }


}
