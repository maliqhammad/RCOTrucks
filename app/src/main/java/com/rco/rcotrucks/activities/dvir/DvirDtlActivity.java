package com.rco.rcotrucks.activities.dvir;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.SignatureActivity;
import com.rco.rcotrucks.activities.dvir.activities.PreTripReport;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.interfaces.TextWatcherListener;
import com.rco.rcotrucks.model.PretripModel;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import static com.rco.rcotrucks.adapters.Cadp.EXTRA_LASTACTIVITY;

public class DvirDtlActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener, View.OnClickListener {
    public static final int REQUESTCODE_SIGNATURE = 1;
    public static final int REQUESTCODE_PREVIEW_REPORT = 2;
    public static final int REQUESTCODE_REPORT_AND_SAVE = 3;

    public EditText etSearch;
    private static String TAG = DvirDtlActivity.class.getSimpleName();
    public String objectId = null;
    public String objectType = null;
    public String recordId = null;
    private RecyclerView recyclerView;
    private DvirDtlAdapter mAdapter;
    private LinearLayoutManager layoutManager;
    //    private BusinessRules busRules = BusinessRules.instance();
    private ImageView textViewDvirDtlCancel, clearSearch, cancelSearch, filterIcon;
    private TextView textViewDvirDtlSave;
    private TextView textViewDvirDtlPreview;
    //    private Long idRmsRecords;
    private BusHelperDvir busRules = BusHelperDvir.instance();
    //    private BusHelperRmsCoding.RmsRecords identRmsRecords;
    private BusHelperRmsCoding.RmsRecords identRmsRecords;
    String mSignatureType = "", mSignatureName = "";
    //    private ListItemCodingDataGroup.OnCheckedChangeListener onCheckedChangeListener;
//    private ListItemCodingDataGroup.OnFocusChangeListener onFocusChangeListener;
    boolean isReeferHosRequired = false;
    protected BusinessRules rules = BusinessRules.instance();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getIntentData();
        setContentView(R.layout.activity_dvir_dtl);

        setIds();
        initialize();
        setListener();

        runUIHelperDvirRefreshTask();
    }


    void getIntentData() {
//        Log.d(TAG, "getIntentData: ");
        Intent intent = getIntent();
        Serializable message = intent.getSerializableExtra(DvirListFragment.EXTRA_MESSAGE_DVIR_RMS_DVIR_IDENT);
//        Log.d(TAG, "getIntentData: message class:" + message.getClass().getName() + ", message=" + StringUtils.memberValuesToString(message));
//        UiUtils.showToast(this, "message class: " + message.getClass().getName() + ", message: " + message);


        if (message != null && message instanceof BusHelperRmsCoding.RmsRecords) {
            identRmsRecords = (BusHelperRmsCoding.RmsRecords) message;
        } else {
//            Log.d(TAG, "getIntentData:  **** Unexpected intent extra of type: "
//                    + (message != null ? message.getClass().getCanonicalName() : "(NULL)"));
//            identRmsRecords = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);

            identRmsRecords = new BusHelperRmsCoding.RmsRecords();
        }
        Log.d(TAG, "getIntentData: " + identRmsRecords);
//
//        Log.d(TAG, "getIntentData: identRmsRecords={" + StringUtils.memberValuesToString(identRmsRecords) + "}");
    }

    void setIds() {
        Log.d(TAG, "setIds: ");
        textViewDvirDtlCancel = findViewById(R.id.btn_back);
        etSearch = findViewById(R.id.et_search);
        textViewDvirDtlSave = findViewById(R.id.textViewSave);
        textViewDvirDtlPreview = findViewById(R.id.textViewPreview);
        cancelSearch = findViewById(R.id.cancelSearch);
        filterIcon = findViewById(R.id.filter_btn);
        clearSearch = findViewById(R.id.iv_clear_search);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
//        textViewDvirDtlSave.setFocusable(true);
//        textViewDvirDtlSave.setFocusableInTouchMode(true);
//        textViewDvirDtlSave.setOnFocusChangeListener(this);
        cancelSearch.setVisibility(View.GONE);
        filterIcon.setVisibility(View.GONE);
    }

    void setListener() {
        Log.d(TAG, "setListener: ");

        clearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etSearch.setText("");
            }
        });

        textViewDvirDtlCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //                busRules.getListDvirDetail().clear(); // or should it be set to null?
                BusHelperDvir.getListDvirDetail().clear(); // or should it be set to null?
                finish();
            }
        });

        textViewDvirDtlSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate()) {

                } else {
//                    Jan 17, 2022  -   Commented so I can see validate function plus we can assign user populated values into our model class
//                    Current Working Code
                    BusHelperDvir helperDvir = new BusHelperDvir();
                    rmsId = helperDvir.runSaveDvirDtlTaskSync(identRmsRecords.getIdRecord(), BusHelperDvir.getListDvirDetail());
//                    startDvirReportActivity(REQUESTCODE_REPORT_AND_SAVE, false);
                    PretripModel pretripModel = getPopulatePreTripModelFromList(BusHelperDvir.getListDvirDetail());
                    startDvirReportActivity(pretripModel, true);

//                    Jan 17, 2022  -
                    populatePreTripModelFromList(BusHelperDvir.getListDvirDetail());

                    finish();
                }
            }
        });

        textViewDvirDtlPreview.setOnClickListener(this);
    }

    void runUIHelperDvirRefreshTask() {
        UiHelperDvirDtl.instance().runRefreshTask(this, identRmsRecords.getIdRecord(), identRmsRecords.getObjectId(), identRmsRecords.getObjectType());
    }


    public void loadContentView() {
        loadRecyclerView();
    }

    private void loadRecyclerView() {
        // Todo: implement searchbox

        try {
            List<ListItemCodingDataGroup> listItems = BusHelperDvir.getListDvirDetail();
            //        List<ListItemCodingDataGroup> listItems = busRules.getListDvirDetail();

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

            if (recyclerView == null) {
//                Log.d(TAG, "loadRecyclerView() recyclerView is null, initializing recyclerView.");
                recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

                // use this setting to improve performance if you know that changes
                // in content do not change the layout size of the RecyclerView
                //        recyclerView.setHasFixedSize(true);

                // use a linear layout manager
                if (layoutManager == null) layoutManager = new LinearLayoutManager(this);
                recyclerView.setLayoutManager(layoutManager);

                DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                        layoutManager.getOrientation());
                //            mDividerItemDecoration.setDrawable();

                recyclerView.addItemDecoration(mDividerItemDecoration);

                mAdapter = new DvirDtlAdapter(this, identRmsRecords.getIdRecord(), listItems,
                        new TextWatcherListener() {
                            @Override
                            public void afterTextChanged(View view, String value, int position) {
                                if (view.hasFocus()) {
                                    afterTextChange(view);
                                }
                            }
                        },
//                        this, this, null, new DvirDtlAdapter.SignatureSelectionListener() {
                        this, this, new DvirDtlAdapter.SignatureSelectionListener() {
                    //                        this, this, this, new DvirDtlAdapter.SignatureSelectionListener() {
                    @Override
                    public void drawSignature(String signatureName, String label, View view) {
//                        Log.d(TAG, "drawSignature: label: " + label);
                        mSignatureName = signatureName;
                        openDrawSignatureActivity(label, view);
                    }
                });
                recyclerView.setAdapter(mAdapter);
//                Log.d(TAG, "loadRecyclerView() case: initializing recyclerView, end of block, after recyclerView.setAdapter");
            } else {
//                Log.d(TAG, "loadRecyclerView() case: not initializing recyclerView, calling mAdapter.notifyDataSetChanged().");
                mAdapter.notifyDataSetChanged();
            }
//            Log.d(TAG, "loadRecyclerView() end.");
        } catch (Throwable e) {
//            Log.d(TAG, "loadRecyclerView() **** Error." + Log.getStackTraceString(e));
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


        Intent intent = new Intent(this, SignatureActivity.class);
        Object message = view.getTag();
//        Log.d(TAG, "openDrawSignatureActivity: view: " + view + " message: " + message);
        ListItemCodingDataGroup item = null;

        if (message != null && message instanceof Integer) {
            int position = (Integer) ((Integer) message).intValue();
//            Log.d(TAG, "onClick() Case: message instanceof Integer. position=" + position);
//            UiUtils.showToast(this, TAG + " message instanceof Integer. position=" + position);
            intent.putExtra(Cadp.EXTRA_MESSAGE, position);
        } else
//            Log.d(TAG, "onClick() Case: message instanceof " + message.getClass().getCanonicalName());

//            Log.d(TAG, "onClick() Case: v instanceof ImageView.");

//        UiUtils.showToast(this, TAG + " v instanceof ImageView, launching SignatureActivity.");

            objectId = identRmsRecords.getObjectId();
        objectType = identRmsRecords.getObjectType();
        Log.d(TAG, "onClick: objectId: " + objectId + " objectType: " + objectType + " ");

        intent.putExtra(Cadp.EXTRA_OBJECT_ID, objectId);
        intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, objectType);
        intent.putExtra(Cadp.EXTRA_SIGNATURE, mSignatureName);

        Log.d(TAG, "onCreate: objectId: openDrawSignatureActivity: mSignatureType: " + mSignatureType);
        intent.putExtra(Cadp.EXTRA_SIGNATURE_TYPE, mSignatureType);
        intent.putExtra(EXTRA_LASTACTIVITY, "DvirDltActivity");

        startActivityForResult(intent, REQUESTCODE_SIGNATURE);
    }

    long rmsId;

    @Override
    public void onClick(View v) {
        try {
//            Log.d(TAG, "onClick() I'm clicked. Id: " + v.getId() + ", Class: " + v.getClass().getSimpleName());
//
//            Log.d(TAG, "textViewDvirDtlSave.setOnClickListener.onClick() v: " + v
//                    + ", DvirDtlActivity.this=" + DvirDtlActivity.this);

            UiUtils.closeKeyboard(v);

//            if (v instanceof ImageView) {
//                Intent intent = new Intent(this, SignatureActivity.class);
//                Object message = v.getTag();
//                ListItemCodingDataGroup item = null;
//
//                if (message != null && message instanceof Integer) {
//                    int position = (Integer) ((Integer) message).intValue();
//                    Log.d(TAG, "onClick() Case: message instanceof Integer. position=" + position);
//                    UiUtils.showToast(this, TAG + " message instanceof Integer. position=" + position);
//                    intent.putExtra(Cadp.EXTRA_MESSAGE, position);
//                } else
//                    Log.d(TAG, "onClick() Case: message instanceof " + message.getClass().getCanonicalName());
//
//                Log.d(TAG, "onClick() Case: v instanceof ImageView.");
//
//                UiUtils.showToast(this, TAG + " v instanceof ImageView, launching SignatureActivity.");
//
//                objectId = identRmsRecords.getObjectId();
//                objectType = identRmsRecords.getObjectType();
//                Log.d(TAG, "onClick: objectId: " + objectId + " objectType: " + objectType);
//
//                intent.putExtra(Cadp.EXTRA_OBJECT_ID, objectId);
//                intent.putExtra(Cadp.EXTRA_OBJECT_TYPE, objectType);
////                intent.putExtra(Cadp.EXTRA_SIGNATURE, signa);
////                intent.putExtra(Cadp.EXTRA_SIGNATURE_TYPE, objectType);
//                intent.putExtra(EXTRA_LASTACTIVITY, "DvirDltActivity");
//
//                startActivityForResult(intent, REQUESTCODE_SIGNATURE);
//            } else {
            if (v == textViewDvirDtlPreview) {
//                startDvirReportActivity(REQUESTCODE_PREVIEW_REPORT, true);
                Log.d(TAG, "onClick: preview");


                BusHelperDvir helperDvir = new BusHelperDvir();
                rmsId = helperDvir.runSaveDvirDtlTaskSync(identRmsRecords.getIdRecord(), BusHelperDvir.getListDvirDetail());
                PretripModel pretripModel = getPopulatePreTripModelFromList(BusHelperDvir.getListDvirDetail());

                startDvirReportActivity(pretripModel, false);
            }
//            else if (v == textViewDvirDtlSave) {
//                Log.d(TAG, "savePreTrip: onClick: save pre trip check: ");
//                //                BusHelperDvir.instance().runSaveDvirDtlTask(DvirDtlActivity.this,
//                //                        identRmsRecords.getIdRecord(), busRules.getListDvirDetail());
//                // Get a new business dir helper because save SQL typically not thread safe.
//                BusHelperDvir helperDvir = new BusHelperDvir();
//                //                BusHelperDvir.instance().runSaveDvirDtlTask(DvirDtlActivity.this,
//                //                        identRmsRecords.getIdRecord(), BusHelperDvir.getListDvirDetail());
//                Log.d(TAG, "savePreTrip: onClick: runSaveDvirDtlTask: ");
//
////                June 24, 2022 -   We should save it synchronous to get rms id from the local database so we can use
////                it in set the content for the dvir detail on server
////                helperDvir.runSaveDvirDtlTask(DvirDtlActivity.this,
////                        identRmsRecords.getIdRecord(), BusHelperDvir.getListDvirDetail());
//
//                rmsId = helperDvir.runSaveDvirDtlTaskSync(identRmsRecords.getIdRecord(), BusHelperDvir.getListDvirDetail());
//
//
//                Log.d(TAG, "savePreTrip: onClick: runSaveDvirDtlTask: complete now call startDvirReportActivity");
//                startDvirReportActivity(REQUESTCODE_REPORT_AND_SAVE, false);
//            }
//            }
        } catch (Throwable e) {
            Log.d(TAG, "onClick() **** Error." + Log.getStackTraceString(e));
        }
    }

    public void startDvirReportActivity(int requestCode, boolean isPreviewOnly) {
        Intent intent = new Intent(this, DvirReportActivity.class);
//        intent.putExtra("rmsId", "" + identRmsRecords.getIdRecord());
        Log.d(TAG, "startDvirReportActivity: rmsId: " + rmsId);
        intent.putExtra("rmsId", "" + rmsId);
        intent.putExtra("isPreviewOnly", "" + isPreviewOnly);
        startActivityForResult(intent, requestCode);
    }

    //        Jan 23, 2022  -   Added new Activity
    public void startDvirReportActivity(PretripModel dataModel, boolean isEditable) {
        Log.d(TAG, "startDvirReportActivity: ");
        Intent intent = new Intent(DvirDtlActivity.this, PreTripReport.class);
        intent.putExtra("dataModel", dataModel);
        intent.putExtra("isEditable", isEditable);
        startActivity(intent);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data); // what does this do?  Should it be at end?  Leave it out?
//        Log.d(TAG, "SignatureCheck: onActivityResult() requestCode=" + requestCode + ", resultCode=" + resultCode
//                + ", RESULT_CANCELED=" + RESULT_CANCELED + ", RESULT_OK=" + RESULT_OK
//                + ", data==null?" + (data == null));

//          By design, report activity only returns here if user saved DVIR form in previous return.  There may be better
//          ways to flow, such as launching Report from DvirDtlActivity on save with flag telling it to return to DvirListActivity.
//          recyclerView.invalidate();
        if (requestCode == REQUESTCODE_REPORT_AND_SAVE) {
            // Todo: figure out when safe to clear DVIR detail list items on save action.  Locking?  Synchronizing?
            finish();
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }


    void onCreateOLDCode() {
        String strThis = "onCreate(), ";

        try {

//        onCheckedChangeListener = new ListItemCodingDataGroup.OnCheckedChangeListener();
//        onFocusChangeListener = new ListItemCodingDataGroup.OnFocusChangeListener();

            // Get the Intent that started this activity and extract the string
            Intent intent = getIntent();
            Serializable message = intent.getSerializableExtra(DvirListFragment.EXTRA_MESSAGE_DVIR_RMS_DVIR_IDENT);
            Log.d(TAG, strThis + " message class:" + message.getClass().getName() + ", message=" + StringUtils.memberValuesToString(message));
//        UiUtils.showToast(this, "message class: " + message.getClass().getName() + ", message: " + message);

            if (message != null && message instanceof BusHelperRmsCoding.RmsRecords)
                identRmsRecords = (BusHelperRmsCoding.RmsRecords) message;
            else {
                Log.d(TAG, strThis + " **** Unexpected intent extra of type: "
                        + (message != null ? message.getClass().getCanonicalName() : "(NULL)"));

//            identRmsRecords = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);
                identRmsRecords = new BusHelperRmsCoding.RmsRecords();
            }

            Log.d(TAG, strThis + " identRmsRecords={" + StringUtils.memberValuesToString(identRmsRecords) + "}");


            setContentView(R.layout.activity_dvir_dtl);
//        recyclerView = findViewById(R.id.recyclerView);
            textViewDvirDtlCancel = findViewById(R.id.btn_back);

            textViewDvirDtlCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //                busRules.getListDvirDetail().clear(); // or should it be set to null?
                    BusHelperDvir.getListDvirDetail().clear(); // or should it be set to null?
                    finish();
                }
            });
            etSearch = findViewById(R.id.et_search);
//            textViewDvirDtlSave = findViewById(R.id.textViewSave);
//            textViewDvirDtlSave.setFocusable(true);
//            textViewDvirDtlSave.setFocusableInTouchMode(true);
//            textViewDvirDtlSave.setOnFocusChangeListener(this);

//        textViewDvirDtlSave.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                BusHelperDvir.instance().runSaveDvirDtlTask(DvirDtlActivity.this,
//                        identRmsRecords.idRmsRecords, busRules.getListDvirDetail());
//            }
//        });

//            textViewDvirDtlSave.setOnClickListener(this);

            textViewDvirDtlPreview = findViewById(R.id.textViewPreview);
            textViewDvirDtlPreview.setOnClickListener(this);

            UiHelperDvirDtl.instance().runRefreshTask(
                    this, identRmsRecords.getIdRecord(), identRmsRecords.getObjectId(), identRmsRecords.getObjectType());
        } catch (Throwable e) {
            e.printStackTrace();
            Log.d(TAG, strThis + "**** Error. " + Log.getStackTraceString(e));
        }
    }

    boolean validate() {
        boolean valid = true;

        if (BusHelperDvir.getListDvirDetail() != null) {
            for (int i = 0; i < BusHelperDvir.getListDvirDetail().size(); i++) {
                ListItemCodingDataGroup listItemCodingDataGroup = BusHelperDvir.getListDvirDetail().get(i);
//                Log.d(TAG, "validate: position: " + i + " label: " + listItemCodingDataGroup.getLabel() + " combinedValue: "
//                        + listItemCodingDataGroup.getCombinedValue());

                if (i == 2 && (listItemCodingDataGroup.getCombinedValue() == null || listItemCodingDataGroup.getCombinedValue().isEmpty())) {
                    Log.d(TAG, "validate: total amount of sale: ");
                    Toast.makeText(this, "Please enter odometer", Toast.LENGTH_LONG).show();
                    focusRecyclerViewItem(0);
                    return false;
                }
                if (i == 4 && (listItemCodingDataGroup.getCombinedValue() == null || listItemCodingDataGroup.getCombinedValue().isEmpty())) {
                    Log.d(TAG, "validate: total amount of sale: ");
                    Toast.makeText(this, "Please enter truck/trailer number", Toast.LENGTH_LONG).show();
                    focusRecyclerViewItem(0);
                    return false;
                } else if (i == 37 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: Gallons: ");
                    Toast.makeText(this, "Please enter Trailer 1 Number", Toast.LENGTH_SHORT).show();
                    isReeferHosRequired = true;
                    focusRecyclerViewItem(42);
                    return false;
                } else if (isReeferHosRequired && (i == 38 && listItemCodingDataGroup.getCombinedValue().isEmpty())) {
                    Log.d(TAG, "validate: sales tax: ");
                    Toast.makeText(this, "Please enter Reefer Hos for trailer 1", Toast.LENGTH_SHORT).show();
                    focusRecyclerViewItem(38);
                    return false;
                } else if (i == 76 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: fuel type: ");
                    Toast.makeText(this, "Please enter Mechanic Name", Toast.LENGTH_SHORT).show();
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
     * <p>
     * //     * @param view
     * //     * @param hasFocus
     */
//    @Override
//    public void onFocusChange(View view, boolean hasFocus) {
//        Log.d(TAG, "onFocusChange: view: " + view + " hasFocus: " + hasFocus);
//        try {
//            ListItemCodingDataGroup.onFocusChangeListener.onFocusChange(view, hasFocus);
//            isFocusChangedFromTrailerNumber(view);
//        } catch (Throwable e) {
//            e.printStackTrace();
//        }
//    }

    //    public void onFocusChange(View v, boolean hasFocus) {
    public void afterTextChange(View v) {
        Log.d(TAG, "afterTextChange: ");
//        Integer intIxListItems = (Integer) v.getTag();
        try {
//            Log.d(TAG, "onFocusChange() Start. hasFocus=" + hasFocus // + ", intIxListItems=" + intIxListItems
//                    + ", v.getClass().getName()=" + v.getClass().getName() + ", v.getId()=" + v.getId());
            String label = null;
            Object objTag = v.getTag();
            Log.d(TAG, "afterTextChange: objTag: " + objTag);
            if (objTag != null && objTag instanceof AdapterUtils.ILabeled)
                label = ((AdapterUtils.ILabeled) objTag).getLabel();
            Log.d(TAG, "afterTextChange: label: " + label);

            if (!(v instanceof EditText)) {
                Log.d(TAG, "afterTextChange() not EditText case: " + label);

//                if (hasFocus) {
//                    Log.d(TAG, "afterTextChange() EditText has focus case: hasFocus=" + hasFocus // + ", intIxListItems=" + intIxListItems
//                    );
//                    UiUtils.closeKeyboard(v);
//                }
            } else {
                String val = null;

                if (v instanceof EditText)
                    val = ((EditText) v).getText().toString();
                else
                    Log.d(TAG, "afterTextChange() ***** Assertion Error.  Expecting view of EditText type, but found: "
                            + v.getClass().getSimpleName());

//                if (!hasFocus) {
                // When an EditText loses focus, we want to store the value.
//                    Log.d(TAG, "afterTextChange() EditText case: hasFocus=" + hasFocus + ", label=" + label + ", val=" + val);
                BusHelperRmsCoding.CodingDataRow codingDataRow;

                if (objTag instanceof BusHelperRmsCoding.CodingDataRow) {
                    Log.d(TAG, "afterTextChange: if: ");

                    BusHelperRmsCoding.CodingDataRow itemCoding = (BusHelperRmsCoding.CodingDataRow) objTag;
//                        BusHelperRmsCoding.CodingDataRow itemCoding = busRules.getListFuelReceiptDetail().get(0)
//                                .getListCodingdataRows().get(0);

                    Log.d(TAG, "afterTextChange() if: case: EditText, objTag is CodingDataRow, val=" + val + ", itemCoding=" + itemCoding);

                    if (!StringUtils.isEquiv(val, itemCoding.getDisplayValue(), false)) // check for equiv not really necessary -- easier on GC? -RAN
                    {
                        Log.d(TAG, "afterTextChange() case: EditText, objTag is CodingDataRow,"
                                + " val is different from itemCoding.getDisplayValue(), val="
                                + val + ", itemCoding.getDisplayValue()g="
                                + itemCoding.getDisplayValue() + ", updating itemCoding.");

                        itemCoding.updateValueFromDisplay(val);
                    } else
                        Log.d(TAG, "afterTextChange() case: EditText, objTag is CodingDataRow, val"
                                + " is same as itemCoding.getDisplayValue(), val=" + val
                                + ", itemCoding.getDisplayValue()=" + itemCoding.getDisplayValue()
                                + ", NOT updating itemCoding.");

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

    PretripModel getPopulatePreTripModelFromList(List<ListItemCodingDataGroup> listItems) {
        Log.d(TAG, "printList: ");
        PretripModel pretripModel = new PretripModel();
        for (int i = 0; i < listItems.size(); i++) {
            Log.d(TAG, "printList: position: " + i + " label: " + listItems.get(i).getLabel() + " combinedValue: " + listItems.get(i).getCombinedValue());
            String userPopulatedValue = listItems.get(i).getCombinedValue();
            setPreTripAttributeValue(i, userPopulatedValue, pretripModel);
        }
        Log.d(TAG, "printList: pretripModel: " + pretripModel);

        return pretripModel;
    }


    void populatePreTripModelFromList(List<ListItemCodingDataGroup> listItems) {
        Log.d(TAG, "printList: ");
        PretripModel pretripModel = new PretripModel();
        for (int i = 0; i < listItems.size(); i++) {
            Log.d(TAG, "printList: position: " + i + " label: " + listItems.get(i).getLabel() + " combinedValue: " + listItems.get(i).getCombinedValue());
            String userPopulatedValue = listItems.get(i).getCombinedValue();
            setPreTripAttributeValue(i, userPopulatedValue, pretripModel);
        }
        Log.d(TAG, "printList: pretripModel: " + pretripModel);

        addNewPreTripEntry(pretripModel);
    }

    void addNewPreTripEntry(PretripModel pretripModel) {
        Log.d(TAG, "addNewPreTripEntry: ");
        int newlyAddedEntryId = rules.insertPretrip(pretripModel, false);
        Log.d(TAG, "addNewPreTripEntry: newlyAddedEntryId: " + newlyAddedEntryId);
//        finish();
    }

    void setPreTripAttributeValue(int position, String userPopulatedValueForThisPosition, PretripModel pretripModel) {
        if (position == 0) {
            pretripModel.setOrganizationName(userPopulatedValueForThisPosition);
        } else if (position == 1) {
            pretripModel.setAddress(userPopulatedValueForThisPosition);
        } else if (position == 2) {
            pretripModel.setOdometer(userPopulatedValueForThisPosition);
        } else if (position == 3) {
            pretripModel.setDateTime(userPopulatedValueForThisPosition);
        } else if (position == 4) {
            pretripModel.setTruckNumber(userPopulatedValueForThisPosition);
        } else if (position == 5) {
            pretripModel.setAirCompressor(userPopulatedValueForThisPosition);
        } else if (position == 6) {
            pretripModel.setAirLines(userPopulatedValueForThisPosition);
        } else if (position == 7) {
            pretripModel.setBattery(userPopulatedValueForThisPosition);
        } else if (position == 8) {
            pretripModel.setBrakeAccessories(userPopulatedValueForThisPosition);
        } else if (position == 9) {
            pretripModel.setBrakes(userPopulatedValueForThisPosition);
        } else if (position == 10) {
            pretripModel.setCarburetor(userPopulatedValueForThisPosition);
        } else if (position == 11) {
            pretripModel.setClutch(userPopulatedValueForThisPosition);
        } else if (position == 12) {
            pretripModel.setDefroster(userPopulatedValueForThisPosition);
        } else if (position == 13) {
            pretripModel.setDriveLine(userPopulatedValueForThisPosition);
        } else if (position == 14) {
            pretripModel.setFifthWheel(userPopulatedValueForThisPosition);
        } else if (position == 15) {
            pretripModel.setFrontalAxle(userPopulatedValueForThisPosition);
        } else if (position == 16) {
            pretripModel.setFuelTanks(userPopulatedValueForThisPosition);
        } else if (position == 17) {
            pretripModel.setHeater(userPopulatedValueForThisPosition);
        } else if (position == 18) {
            pretripModel.setHorn(userPopulatedValueForThisPosition);
        } else if (position == 19) {
            pretripModel.setLights(userPopulatedValueForThisPosition);
        } else if (position == 20) {
            pretripModel.setMirrors(userPopulatedValueForThisPosition);
        } else if (position == 21) {
            pretripModel.setOilPressure(userPopulatedValueForThisPosition);
        } else if (position == 22) {
            pretripModel.setOnBoardRecorder(userPopulatedValueForThisPosition);
        } else if (position == 23) {
            pretripModel.setRadiator(userPopulatedValueForThisPosition);
        } else if (position == 24) {
            pretripModel.setRearEnd(userPopulatedValueForThisPosition);
        } else if (position == 25) {
            pretripModel.setReflectors(userPopulatedValueForThisPosition);
        } else if (position == 26) {
            pretripModel.setSafetyEquipment(userPopulatedValueForThisPosition);
        } else if (position == 27) {
            pretripModel.setSprings(userPopulatedValueForThisPosition);
        } else if (position == 28) {
            pretripModel.setStarter(userPopulatedValueForThisPosition);
        } else if (position == 29) {
            pretripModel.setSteering(userPopulatedValueForThisPosition);
        } else if (position == 30) {
            pretripModel.setTachograph(userPopulatedValueForThisPosition);
        } else if (position == 31) {
            pretripModel.setTires(userPopulatedValueForThisPosition);
        } else if (position == 32) {
            pretripModel.setTransmission(userPopulatedValueForThisPosition);
        } else if (position == 33) {
            pretripModel.setWheels(userPopulatedValueForThisPosition);
        } else if (position == 34) {
            pretripModel.setWindows(userPopulatedValueForThisPosition);
        } else if (position == 35) {
            pretripModel.setWindShieldWipers(userPopulatedValueForThisPosition);
        } else if (position == 36) {
            pretripModel.setOthers(userPopulatedValueForThisPosition);
        } else if (position == 37) {
            pretripModel.setTrailer1(userPopulatedValueForThisPosition);
        } else if (position == 38) {
            pretripModel.setTrailer1ReeferHOS(userPopulatedValueForThisPosition);
        } else if (position == 39) {
            pretripModel.setTrailer1BreakConnections(userPopulatedValueForThisPosition);
        } else if (position == 40) {
            pretripModel.setTrailer1Breaks(userPopulatedValueForThisPosition);
        } else if (position == 41) {
            pretripModel.setTrailer1CouplingPin(userPopulatedValueForThisPosition);
        } else if (position == 42) {
            pretripModel.setTrailer1CouplingChains(userPopulatedValueForThisPosition);
        } else if (position == 43) {
            pretripModel.setTrailer1Doors(userPopulatedValueForThisPosition);
        } else if (position == 44) {
            pretripModel.setTrailer1Hitch(userPopulatedValueForThisPosition);
        } else if (position == 45) {
            pretripModel.setTrailer1LandingGear(userPopulatedValueForThisPosition);
        } else if (position == 46) {
            pretripModel.setTrailer1LightsAll(userPopulatedValueForThisPosition);
        } else if (position == 47) {
            pretripModel.setTrailer1Roof(userPopulatedValueForThisPosition);
        } else if (position == 48) {
            pretripModel.setSprings(userPopulatedValueForThisPosition);
        } else if (position == 49) {
            pretripModel.setTrailer1Tarpaulin(userPopulatedValueForThisPosition);
        } else if (position == 50) {
            pretripModel.setTrailer1Tires(userPopulatedValueForThisPosition);
        } else if (position == 51) {
            pretripModel.setTrailer1Wheels(userPopulatedValueForThisPosition);
        } else if (position == 52) {
            pretripModel.setTrailer1Others(userPopulatedValueForThisPosition);
        } else if (position == 53) {
            pretripModel.setTrailer2(userPopulatedValueForThisPosition);
        } else if (position == 54) {
            pretripModel.setTrailer2ReeferHOS(userPopulatedValueForThisPosition);
        } else if (position == 55) {
            pretripModel.setTrailer2BreakConnections(userPopulatedValueForThisPosition);
        } else if (position == 56) {
            pretripModel.setTrailer2Breaks(userPopulatedValueForThisPosition);
        } else if (position == 57) {
            pretripModel.setTrailer2CouplingPin(userPopulatedValueForThisPosition);
        } else if (position == 58) {
            pretripModel.setTrailer2CouplingChains(userPopulatedValueForThisPosition);
        } else if (position == 59) {
            pretripModel.setTrailer2Doors(userPopulatedValueForThisPosition);
        } else if (position == 60) {
            pretripModel.setTrailer2Hitch(userPopulatedValueForThisPosition);
        } else if (position == 61) {
            pretripModel.setTrailer2LandingGear(userPopulatedValueForThisPosition);
        } else if (position == 62) {
            pretripModel.setTrailer2LightsAll(userPopulatedValueForThisPosition);
        } else if (position == 63) {
            pretripModel.setTrailer2Roof(userPopulatedValueForThisPosition);
        } else if (position == 64) {
            pretripModel.setSprings(userPopulatedValueForThisPosition);
        } else if (position == 65) {
            pretripModel.setTrailer2Tarpaulin(userPopulatedValueForThisPosition);
        } else if (position == 66) {
            pretripModel.setTrailer2Tires(userPopulatedValueForThisPosition);
        } else if (position == 67) {
            pretripModel.setTrailer2Wheels(userPopulatedValueForThisPosition);
        } else if (position == 68) {
            pretripModel.setTrailer2Others(userPopulatedValueForThisPosition);
        } else if (position == 69) {
            pretripModel.setRemarks(userPopulatedValueForThisPosition);
        } else if (position == 70) {
            pretripModel.setConditionVehicleIsSatisfactory(userPopulatedValueForThisPosition);
        } else if (position == 71) {
            pretripModel.setRegistration(userPopulatedValueForThisPosition);
        } else if (position == 72) {
            pretripModel.setInsurance(userPopulatedValueForThisPosition);
        } else if (position == 73) {
            pretripModel.setDriverNameAndSignature(userPopulatedValueForThisPosition);
        } else if (position == 74) {
            pretripModel.setAboveDefectsCorrected(userPopulatedValueForThisPosition);
        } else if (position == 75) {
            pretripModel.setAboveDefectsNoCorrectionNeeded(userPopulatedValueForThisPosition);
        } else if (position == 76) {
            pretripModel.setMechanicNameAndSignature(userPopulatedValueForThisPosition);
        }
    }

}
