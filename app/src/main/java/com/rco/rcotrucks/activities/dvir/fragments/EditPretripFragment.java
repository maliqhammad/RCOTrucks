package com.rco.rcotrucks.activities.dvir.fragments;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_LASTACTIVITY;

import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.SignatureActivity;
import com.rco.rcotrucks.activities.dvir.BusHelperDvir;
import com.rco.rcotrucks.activities.dvir.DvirDtlActivity;
import com.rco.rcotrucks.activities.dvir.DvirDtlAdapter;
import com.rco.rcotrucks.activities.dvir.DvirListFragment;
import com.rco.rcotrucks.activities.dvir.DvirReportActivity;
import com.rco.rcotrucks.activities.dvir.UiHelperDvirDtl;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.interfaces.TextWatcherListener;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class EditPretripFragment extends Fragment implements CompoundButton.OnCheckedChangeListener, View.OnClickListener, View.OnFocusChangeListener {

    private static String TAG = EditPretripFragment.class.getSimpleName();
    private BusHelperDvir busHelperDvirRules = BusHelperDvir.instance();
    public static final int REQUEST_CODE_SIGNATURE = 1;
    public static final int REQUEST_CODE_PREVIEW_REPORT = 2;
    public static final int REQUEST_CODE_REPORT_AND_SAVE = 3;

    public EditText etSearch;
    public String objectId = null;
    public String objectType = null;
    public String recordId = null;
    private RecyclerView recyclerView;
    private DvirDtlAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    //    private ImageView textViewDvirDtlCancel, clearSearch, cancelSearch, filterIcon;
    private ImageView clearSearch, cancelSearch, filterIcon;
    //    private TextView textViewDvirDtlSave;
    private TextView textViewDvirDtlPreview, email, preview, cancel, save, edit;
    //    private BusHelperRmsCoding.RmsRecords identRmsRecords;
    boolean isReeferHosRequired = false;
    protected BusinessRules rules = BusinessRules.instance();
    String mSignatureType = "", mSignatureName = "";
    String intentObjectId = "", intentObjectType = "";
    Long intentIdRecord;
    PreTripOptionsSelection preTripOptionsSelection;

    public EditPretripFragment(PreTripOptionsSelection preTripOptionsSelection) {
        this.preTripOptionsSelection = preTripOptionsSelection;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_edit_pretrip, container, false);
        getIntentData();

        setIds(view);
        initialize();
        setListener();

        runUIHelperDvirRefreshTask();
        return view;
    }

    void getIntentData() {
        Log.d(TAG, "getIntentData: ");
//        Intent intent = new Intent(getActivity(), EditPretripFragment.class);
//        Serializable message = intent.getSerializableExtra(DvirListFragment.EXTRA_MESSAGE_DVIR_RMS_DVIR_IDENT);
//        if (message != null && message instanceof BusHelperRmsCoding.RmsRecords) {
//            identRmsRecords = (BusHelperRmsCoding.RmsRecords) message;
//        } else {
//            identRmsRecords = new BusHelperRmsCoding.RmsRecords();
//        }
//        Log.d(TAG, "getIntentData: " + identRmsRecords);

        if (getArguments() != null) {
//            Serializable message = getArguments().getSerializable("recordIdent");
//            Log.d(TAG, "getIntentData: message: " + message);
//            if (message != null && message instanceof BusHelperRmsCoding.RmsRecords) {
//                identRmsRecords = (BusHelperRmsCoding.RmsRecords) message;
//            } else {
//                identRmsRecords = new BusHelperRmsCoding.RmsRecords();
//            }
//
//            Log.d(TAG, "getIntentData: identRmsRecords: "+identRmsRecords);


            intentIdRecord = getArguments().getLong("idRecord");
            intentObjectId = getArguments().getString("objectId");
            intentObjectType = getArguments().getString("objectType");

            Log.d(TAG, "getIntentData: " + intentIdRecord + ", " + intentObjectId + ", " + intentObjectType);
        }
    }

    void setIds(View view) {
        Log.d(TAG, "setIds: ");
        cancel = getActivity().findViewById(R.id.main_app_bar_cancel);
        save = getActivity().findViewById(R.id.main_app_bar_save);

        etSearch = view.findViewById(R.id.et_search);
        textViewDvirDtlPreview = view.findViewById(R.id.textViewPreview);
        cancelSearch = view.findViewById(R.id.cancelSearch);
        filterIcon = view.findViewById(R.id.filter_btn);
        clearSearch = view.findViewById(R.id.iv_clear_search);
        email = getActivity().findViewById(R.id.email_pretrip);
        preview = getActivity().findViewById(R.id.preview_pretrip);
        edit = getActivity().findViewById(R.id.edit_pretrip);

        recyclerView = view.findViewById(R.id.recyclerView);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
        cancelSearch.setVisibility(View.GONE);
        filterIcon.setVisibility(View.GONE);
        email.setVisibility(View.GONE);
        preview.setVisibility(View.VISIBLE);
        edit.setVisibility(View.GONE);

        cancel.setVisibility(View.VISIBLE);
        save.setVisibility(View.VISIBLE);

    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
            }
        });

        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BusHelperDvir.getListDvirDetail().clear(); // or should it be set to null?
                save.setVisibility(View.GONE);
                cancel.setVisibility(View.GONE);
                preTripOptionsSelection.onCancelCalled();
            }
        });

        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate()) {

                } else {

                    save.setVisibility(View.GONE);
                    cancel.setVisibility(View.GONE);

                    BusHelperDvir helperDvir = new BusHelperDvir();
//                    rmsId = helperDvir.runSaveDvirDtlTaskSync(identRmsRecords.getIdRecord(), BusHelperDvir.getListDvirDetail());
                    rmsId = helperDvir.runSaveDvirDtlTaskSync(intentIdRecord, BusHelperDvir.getListDvirDetail());
                    startDvirReportActivity(REQUEST_CODE_REPORT_AND_SAVE, false);
//                    finish();
                }
            }
        });

        textViewDvirDtlPreview.setOnClickListener(this);
        preview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startDvirReportActivity(REQUEST_CODE_PREVIEW_REPORT, true);
            }
        });


    }

    void runUIHelperDvirRefreshTask() {

        UiHelperDvirDtl.instance().runRefreshTask(getActivity(), EditPretripFragment.this, intentIdRecord, intentObjectId, intentObjectType);
    }


    public void loadContentView() {
        loadRecyclerView();
    }

    private void loadRecyclerView() {
        // Todo: implement searchbox

        try {
            List<ListItemCodingDataGroup> listItems = BusHelperDvir.getListDvirDetail();
            //        List<ListItemCodingDataGroup> listItems = busRules.getListDvirDetail();

            for (int i = 0; i < listItems.size(); i++) {
                Log.d(TAG, "loadRecyclerView: label: " + listItems.get(i).getLabel());
            }

            Log.d(TAG, "loadRecyclerView: listItem: " + listItems.size());

            etSearch.addTextChangedListener(new TextWatcher() {

                public void afterTextChanged(Editable s) {
                }

                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                }

                public void onTextChanged(CharSequence query, int start, int before, int count) {

                    List<ListItemCodingDataGroup> filteredList = new ArrayList<>();
                    query = query.toString().toLowerCase();
                    if (listItems != null) {
                        for (int i = 0; i < listItems.size(); i++) {

                            final String text = listItems.get(i).getLabel().toLowerCase();
                            if (text.contains(query)) {
                                filteredList.add(listItems.get(i));
                            }
                        }
                        mAdapter.filterList(filteredList);
                    }
                }
            });

//            Log.d(TAG, "loadRecyclerView() start, listItems: " + listItems);

//            if (recyclerView == null) {
//                Log.d(TAG, "loadRecyclerView() recyclerView is null, initializing recyclerView.");

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            //        recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            if (layoutManager == null) layoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(layoutManager);

            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    layoutManager.getOrientation());
//              mDividerItemDecoration.setDrawable();

            recyclerView.addItemDecoration(mDividerItemDecoration);

//                mAdapter = new DvirDtlAdapter(getActivity(), identRmsRecords.getIdRecord(), listItems,
            mAdapter = new DvirDtlAdapter(getActivity(), intentIdRecord, listItems,
                    new TextWatcherListener() {
                        @Override
                        public void afterTextChanged(View view, String value, int position) {
                            if (view.hasFocus()) {
                                afterTextChange(view);
                            }
                        }
                    },
//                        this, this, this, new DvirDtlAdapter.SignatureSelectionListener() {
                    this, this, new DvirDtlAdapter.SignatureSelectionListener() {
                @Override
                public void drawSignature(String signatureName, String label, View view) {
//                        Log.d(TAG, "drawSignature: label: " + label);
                    mSignatureName = signatureName;
                    openDrawSignatureActivity(label, view);
                }
            });
            recyclerView.setAdapter(mAdapter);
//                Log.d(TAG, "loadRecyclerView() case: initializing recyclerView, end of block, after recyclerView.setAdapter");
//            } else {
//                Log.d(TAG, "loadRecyclerView() case: not initializing recyclerView, calling mAdapter.notifyDataSetChanged().");
            mAdapter.notifyDataSetChanged();
//            }
//            Log.d(TAG, "loadRecyclerView() end.");
        } catch (Throwable e) {
            Log.d(TAG, "loadRecyclerView() **** Error." + Log.getStackTraceString(e));
        }
    }

    void openDrawSignatureActivity(String label, View view) {
//        Log.d(TAG, "openDrawSignatureActivity: label: " + label);
//        Log.d(TAG, "openDrawSignatureActivity I'm clicked. Id: " + view.getId() + ", Class: " + view.getClass().getSimpleName());
        if (label.equalsIgnoreCase("Drivers name and signature")) {
            mSignatureType = "DriverSignature";
        } else if (label.equalsIgnoreCase("Mechanic Name and Signature")) {
            mSignatureType = "MechanicSignature";
        }


        Intent intent = new Intent(getContext(), SignatureActivity.class);
        Object message = view.getTag();
//        Log.d(TAG, "openDrawSignatureActivity: view: " + view + " message: " + message);
        ListItemCodingDataGroup item = null;

        if (message != null && message instanceof Integer) {
            int position = (Integer) ((Integer) message).intValue();
//            Log.d(TAG, "onClick() Case: message instanceof Integer. position=" + position);
//            UiUtils.showToast(this, TAG + " message instanceof Integer. position=" + position);
            intent.putExtra(Cadp.EXTRA_MESSAGE, position);
        }
//            Log.d(TAG, "onClick() Case: message instanceof " + message.getClass().getCanonicalName());

//            Log.d(TAG, "onClick() Case: v instanceof ImageView.");

//        UiUtils.showToast(this, TAG + " v instanceof ImageView, launching SignatureActivity.");

//        objectId = identRmsRecords.getObjectId();
//        objectType = identRmsRecords.getObjectType();
        objectId = intentObjectId;
        objectType = intentObjectType;
        Log.d(TAG, "onClick: objectId: " + objectId + " objectType: " + objectType + " ");

        intent.putExtra(Cadp.EXTRA_OBJECT_ID, objectId);
        intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, objectType);
        intent.putExtra(Cadp.EXTRA_SIGNATURE, mSignatureName);

        Log.d(TAG, "onCreate: objectId: openDrawSignatureActivity: mSignatureType: " + mSignatureType);
        intent.putExtra(Cadp.EXTRA_SIGNATURE_TYPE, mSignatureType);
        intent.putExtra(EXTRA_LASTACTIVITY, "DvirDltActivity");

        startActivityForResult(intent, REQUEST_CODE_SIGNATURE);
    }

    long rmsId;

    @Override
    public void onClick(View v) {
        try {
            UiUtils.closeKeyboard(v);
            if (v == textViewDvirDtlPreview) {
                startDvirReportActivity(REQUEST_CODE_PREVIEW_REPORT, true);
            }
        } catch (Throwable e) {
            Log.d(TAG, "onClick() **** Error." + Log.getStackTraceString(e));
        }
    }

    public void startDvirReportActivity(int requestCode, boolean isPreviewOnly) {
        Intent intent = new Intent(getActivity(), DvirReportActivity.class);
//        intent.putExtra("rmsId", "" + identRmsRecords.getIdRecord());
        Log.d(TAG, "startDvirReportActivity: rmsId: " + rmsId);
        intent.putExtra("rmsId", "" + rmsId);
        intent.putExtra("isPreviewOnly", "" + isPreviewOnly);
        startActivityForResult(intent, requestCode);
    }

//    @Override
//    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        Log.d(TAG, "onCheckChanged() Start. isChecked=" + isChecked);
//        Integer intIxListItems = (Integer) buttonView.getTag();
//        Log.d(TAG, "onCheckChanged() intIxListItems=" + intIxListItems);
//        UiUtils.closeKeyboard(buttonView);
//
//        if (intIxListItems != null) {
//            List <BusHelperRmsCoding.ListItemCodingDataRow_obs> listItemCodingDataRowList = busRules.getListDvirDetail2().get(intIxListItems.intValue());
//            BusHelperRmsCoding.ListItemCodingDataRow_obs item = listItemCodingDataRowList.get(listItemCodingDataRowList.size() - 1);
//            Log.d(TAG, "onCheckChanged() item=" + item);
//            item.setValue(isChecked ? "1" : "");
//        }
//        Log.d(TAG, "onCheckChanged() End. isChecked=" + isChecked);
//    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//        Log.d(TAG, "onCheckChanged() Start. isChecked=" + isChecked);
//        BusHelperRmsCoding.CodingDataRow itemCoding = (BusHelperRmsCoding.CodingDataRow) buttonView.getTag();
//        Log.d(TAG, "onCheckChanged() itemCoding=" + itemCoding);
//        UiUtils.closeKeyboard(buttonView);
//
//        if (itemCoding != null) {
//            itemCoding.updateValueFromDisplay(isChecked ? "1" : "");
//        }
//
//        Log.d(TAG, "onCheckChanged() End. isChecked=" + isChecked);
        try {
            ListItemCodingDataGroup.onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
        } catch (Throwable e) {
            Log.d(TAG, "onClick() **** Error." + Log.getStackTraceString(e));
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // what does this do?  Should it be at end?  Leave it out?
//        Log.d(TAG, "SignatureCheck: onActivityResult() requestCode=" + requestCode + ", resultCode=" + resultCode
//                + ", RESULT_CANCELED=" + RESULT_CANCELED + ", RESULT_OK=" + RESULT_OK
//                + ", data==null?" + (data == null));

//          By design, report activity only returns here if user saved DVIR form in previous return.  There may be better
//          ways to flow, such as launching Report from DvirDtlActivity on save with flag telling it to return to DvirListActivity.
//          recyclerView.invalidate();
        if (requestCode == REQUEST_CODE_REPORT_AND_SAVE) {
            // Todo: figure out when safe to clear DVIR detail list items on save action.  Locking?  Synchronizing?
//        Hide
//            finish();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    boolean validate() {
        boolean valid = true;

        if (BusHelperDvir.getListDvirDetail() != null) {
            for (int i = 0; i < BusHelperDvir.getListDvirDetail().size(); i++) {
                ListItemCodingDataGroup listItemCodingDataGroup = BusHelperDvir.getListDvirDetail().get(i);
                Log.d(TAG, "validate: position: " + i + " label: " + listItemCodingDataGroup.getLabel() + " combinedValue: "
                        + listItemCodingDataGroup.getCombinedValue());
                if (i == 2 && (listItemCodingDataGroup.getCombinedValue() == null || listItemCodingDataGroup.getCombinedValue().isEmpty())) {
                    Log.d(TAG, "validate: total amount of sale: ");
                    Toast.makeText(getContext(), "Please enter odometer", Toast.LENGTH_LONG).show();
                    focusRecyclerViewItem(0);
                    return false;
                }
                if (i == 4 && (listItemCodingDataGroup.getCombinedValue() == null || listItemCodingDataGroup.getCombinedValue().isEmpty())) {
                    Log.d(TAG, "validate: total amount of sale: ");
                    Toast.makeText(getContext(), "Please enter truck/trailer number", Toast.LENGTH_LONG).show();
                    focusRecyclerViewItem(0);
                    return false;
                } else if (i == 37 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: Gallons: ");
                    Toast.makeText(getContext(), "Please enter Trailer 1 Number", Toast.LENGTH_SHORT).show();
                    isReeferHosRequired = true;
                    focusRecyclerViewItem(42);
                    return false;
                } else if (isReeferHosRequired && (i == 38 && listItemCodingDataGroup.getCombinedValue().isEmpty())) {
                    Log.d(TAG, "validate: sales tax: ");
                    Toast.makeText(getContext(), "Please enter Reefer Hos for trailer 1", Toast.LENGTH_SHORT).show();
                    focusRecyclerViewItem(38);
                    return false;
                } else if (i == 76 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: fuel type: ");
                    Toast.makeText(getContext(), "Please enter Mechanic Name", Toast.LENGTH_SHORT).show();
                    focusRecyclerViewItem(76);
                    return false;
                }
            }

        } else {
            return false;
        }
        return valid;
    }

    void focusRecyclerViewItem(int position) {
//        recyclerView.setAdapter(adapter);// set adapter on recyclerview
        recyclerView.scrollToPosition(position); //use to focus the item with index
        mAdapter.notifyDataSetChanged();
    }


    /**
     * Simple focus change listener, appropriate for EditText fields that map directly to a
     * single codingfield.  Any field combining codingfields into a single EditText field
     * would require a listener with an appropriate parser and have a ListItemCodingDataGroup
     * object in the View's tag.
     *
     * @param view
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View view, boolean hasFocus) {
        Log.d(TAG, "onFocusChange: view: " + view + " hasFocus: " + hasFocus);
        try {
            ListItemCodingDataGroup.onFocusChangeListener.onFocusChange(view, hasFocus);
            isFocusChangedFromTrailerNumber(view);
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    public void afterTextChange(View v) {
        Log.d(TAG, "afterTextChange: ");
        try {
            String label = null;
            Object objTag = v.getTag();
            Log.d(TAG, "afterTextChange: objTag: " + objTag);
            if (objTag != null && objTag instanceof AdapterUtils.ILabeled)
                label = ((AdapterUtils.ILabeled) objTag).getLabel();
            Log.d(TAG, "afterTextChange: label: " + label);

            if (!(v instanceof EditText)) {
                Log.d(TAG, "afterTextChange() not EditText case: " + label);
            } else {
                String val = null;

                if (v instanceof EditText)
                    val = ((EditText) v).getText().toString();
                else
                    Log.d(TAG, "afterTextChange() ***** Assertion Error.  Expecting view of EditText type, but found: "
                            + v.getClass().getSimpleName());

                BusHelperRmsCoding.CodingDataRow codingDataRow;

                if (objTag instanceof BusHelperRmsCoding.CodingDataRow) {
                    Log.d(TAG, "afterTextChange: if: ");

                    BusHelperRmsCoding.CodingDataRow itemCoding = (BusHelperRmsCoding.CodingDataRow) objTag;

                    Log.d(TAG, "afterTextChange() if: case: EditText, objTag is CodingDataRow, val=" + val + ", itemCoding=" + itemCoding);

                    if (!StringUtils.isEquiv(val, itemCoding.getDisplayValue(), false)) {
                        itemCoding.updateValueFromDisplay(val);
                    }
                } else if (objTag instanceof ListItemCodingDataGroup) {
                    Log.d(TAG, "afterTextChange: else if: ");
                    ListItemCodingDataGroup item = (ListItemCodingDataGroup) objTag;
                    label = item.getLabel();

                    String itemVal = item.getCombinedValue();

                    Log.d(TAG, "afterTextChange() case: EditText, objTag is ListItemCodingDataGroup,"
                            + " updating combined value, val=" + val + ", itemVal=" + itemVal
                            + ", item=" + item);

                    if (!StringUtils.isEquiv(val, itemVal, false)) {
                        Log.d(TAG, "afterTextChange() case: EditText, objTag is ListItemCodingDataGroup, label=" + label
                                + ", new val is different from old, updating.");
                        item.updateCombinedValue(val);
                    } else
                        Log.d(TAG, "afterTextChange() case: EditText, objTag is "
                                + "ListItemCodingDataGroup, new val is same as old, updating, label=" + label + ".");
                }
//                } else
//                    Log.d(TAG, "afterTextChange() EditText case: field is gaining focus, hasFocus=" + hasFocus
//                            + ", label=" + label + ", val=" + val + ", no need to update anything.");
            }

//            Log.d(TAG, "afterTextChange() End. hasFocus=" + hasFocus + ", v.getClass().getName()=" + v.getClass().getName() + ", " + v.getId() + ", label=" + label
//            );
        } catch (Throwable e) {
            Log.d(TAG, "afterTextChange: throwable: " + e.getMessage());
            e.printStackTrace();
        }
    }

    boolean isFocusChangedFromTrailerNumber(View view) {
        Log.d(TAG, "isFocusChangedFromTrailerNumber: ");

        String label = null;
        Object objTag = view.getTag();
        Log.d(TAG, "isFocusChangedFromTrailerNumber: objTag: " + objTag);
        if (objTag != null && objTag instanceof AdapterUtils.ILabeled)
            label = ((AdapterUtils.ILabeled) objTag).getLabel();
        Log.d(TAG, "isFocusChangedFromTrailerNumber: label: " + label);

        if (label.equalsIgnoreCase("TRAILER 1")) {
            Log.d(TAG, "isFocusChangedFromTrailerNumber: now get its value and search from local database");

            if (!(view instanceof EditText)) {
                Log.d(TAG, "isFocusChangedFromTrailerNumber() not EditText case: " + label);
            } else {
                String val = null;

                if (view instanceof EditText)
                    val = ((EditText) view).getText().toString();
                else
                    Log.d(TAG, "isFocusChangedFromTrailerNumber() ***** Assertion Error.  Expecting view of EditText type, but found: "
                            + view.getClass().getSimpleName());

                if (objTag instanceof BusHelperRmsCoding.CodingDataRow) {
                    Log.d(TAG, "isFocusChangedFromTrailerNumber: if: ");

                    BusHelperRmsCoding.CodingDataRow itemCoding = (BusHelperRmsCoding.CodingDataRow) objTag;
                    Log.d(TAG, "isFocusChangedFromTrailerNumber: displayValue: " + itemCoding.getDisplayValue());

                    if (itemCoding.getDisplayValue().isEmpty()) {
                        isReeferHosRequired = false;
                    } else {
                        boolean isTrailerNumberAlreadyExist = rules.isReeferForTrailer(itemCoding.getDisplayValue());
                        Log.d(TAG, "isFocusChangedFromTrailerNumber: isTrailerNumberAlreadyExist: " + isTrailerNumberAlreadyExist);
                        if (isTrailerNumberAlreadyExist) {
                            isReeferHosRequired = true;
                        } else {
                            isReeferHosRequired = false;
                        }
                    }
                    Log.d(TAG, "isFocusChangedFromTrailerNumber: isReeferHosRequired: " + isReeferHosRequired);
                }
            }


        }
        return false;
    }

    public interface PreTripOptionsSelection {
        public void onCancelCalled();

        public void onDeleteCalled();
    }

}
