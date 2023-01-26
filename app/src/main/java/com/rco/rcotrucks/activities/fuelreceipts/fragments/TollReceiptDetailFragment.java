package com.rco.rcotrucks.activities.fuelreceipts.fragments;

import android.content.DialogInterface;
import android.os.Bundle;

import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.FuelReceiptDtlAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.utils.UiHelperFuelReceiptDtl;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.fragments.BaseFragment;
import com.rco.rcotrucks.interfaces.ReceiptListener;
import com.rco.rcotrucks.interfaces.TextWatcherListener;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.util.List;

public class TollReceiptDetailFragment  extends BaseFragment implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, View.OnFocusChangeListener, AdapterView.OnItemSelectedListener {

    private static String TAG = TollReceiptDetailFragment.class.getSimpleName();
    public static final int REQUEST_IMAGE_CAPTURE = 2, REQUESTCODE_PICK_PHOTO = 3;

    public String objectId = null, objectType = null, recordId = null;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager layoutManager;

    private ImageView deleteFuelReceipt, saveFuelReceipt;

    private BusHelperFuelReceipts busRules = BusHelperFuelReceipts.instance();
    private BusHelperRmsCoding.RmsRecords identRmsRecords;

    List<ListItemCodingDataGroup> listItems;
    View mView;
    ReceiptListener receiptListener;

    public TollReceiptDetailFragment() {
    }

    public TollReceiptDetailFragment(ReceiptListener receiptListener) {
        this.receiptListener=receiptListener;
    }

    void getIntentData() {
        Log.d(TAG, "getIntentData: ");
        identRmsRecords = (BusHelperRmsCoding.RmsRecords) getArguments().getSerializable(ReceiptFragment.EXTRA_MESSAGE_FUEL_RECEIPT_IDENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_toll_receipt_detail, container, false);

        mView = view;
        Log.d(TAG, "onCreateView: view: " + mView);

        setIds(view);
        getIntentData();
        initialize();
        setListener();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(TAG, "onResume: ");
        UiHelperFuelReceiptDtl.instance().runRefreshTask(getActivity(), this, identRmsRecords.getIdRecord(),
                identRmsRecords.getObjectId(), identRmsRecords.getObjectType());
    }


    void setIds(View view) {
        Log.d(TAG, "setIds: ");
//        title = view.findViewById(R.id.tv_title);
        recyclerView = view.findViewById(R.id.recyclerView);
        deleteFuelReceipt = view.findViewById(R.id.delete_fuel_receipt);
        saveFuelReceipt = view.findViewById(R.id.save_fuel_receipt);
    }

    void initialize() {
        Log.d(TAG, "initialize: ");
    }

    void setListener() {
        Log.d(TAG, "setListener: ");
        deleteFuelReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UiUtils.showBooleanDialog(getContext(), "Delete",
                        R.drawable.ic_circlered, "Yes", "No", "Do you want to delete this receipt?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // If it is a new record (idRmsRecords < 0) and user hits delete, no need to update database or RMS -- should be same as "Cancel" for a new record.  We could hide Delete button for new records as an alternative.
                                if (identRmsRecords.getIdRecord() >= 0) {
                                    BusHelperFuelReceipts busHelperFuelReceipts = new BusHelperFuelReceipts(RecordRulesHelper.getDb());
                                    busHelperFuelReceipts.runSaveFuelReceiptDtlTask(getActivity(),
                                            identRmsRecords.getIdRecord(), BusHelperFuelReceipts.getListFuelReceiptDetail(),
                                            BusHelperFuelReceipts.getListExtra(), null, true);
//                                    We should reload this lists for fuel here
                                    receiptListener.receiptUpdate();
                                } else cancel();
                            }
                        }
                );
            }
        });

        saveFuelReceipt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!validate()) {
                } else {
                    busRules.runSaveFuelReceiptDtlTask(getActivity(),
                            identRmsRecords.getIdRecord(), BusHelperFuelReceipts.getListFuelReceiptDetail(),
                            BusHelperFuelReceipts.getListExtra(),
                            busRules.getFuelReceiptExtraPostProcessor(), false);
                    receiptListener.receiptUpdate();
                }
            }
        });
    }

    public void loadContentView() {
        Log.d(TAG, "loadContentView: ");
        loadRecyclerView();
    }

    private void loadRecyclerView() {
        Log.d(TAG, "loadRecyclerView: ");
        // Todo: implement searchbox
        listItems = busRules.getListFuelReceiptDetail();
        Log.d(TAG, "loadRecyclerView() start, listItems: size: " + listItems.size());
        Log.d(TAG, "loadRecyclerView: recyclerView: " + recyclerView);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        // recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        if (layoutManager == null) layoutManager = new LinearLayoutManager(getContext());
        recyclerView.setLayoutManager(layoutManager);

        DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                layoutManager.getOrientation());

        Log.d(TAG, "loadRecyclerView: " + mDividerItemDecoration.toString());
        recyclerView.addItemDecoration(mDividerItemDecoration);

        mAdapter = new FuelReceiptDtlAdapter(getContext(), identRmsRecords.getIdRecord(), listItems,
                new TextWatcherListener() {
                    @Override
                    public void afterTextChanged(View view, String value, int position) {
                        afterTextChange(view);
                    }
                },
                this, this, this, this);
        recyclerView.setAdapter(mAdapter);
        Log.d(TAG, "loadRecyclerView() case: initializing recyclerView, end of block, after recyclerView.setAdapter");
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        Log.d(TAG, "onClick() I'm clicked. Id: " + v.getId() + ", Class: " + v.getClass().getSimpleName());
        UiUtils.closeKeyboard(v);
    }

    public void cancel() {
        Log.d(TAG, "cancel: ");
//        busRules.getListFuelReceiptDetail().clear(); // or should it be set to null?
        if (BusHelperFuelReceipts.getListFuelReceiptDetail() != null) {
            BusHelperFuelReceipts.getListFuelReceiptDetail().clear(); // or should it be set to null?
        }

        getActivity().finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        Log.d(TAG, "onCheckedChanged: ");
        ListItemCodingDataGroup.onCheckedChangeListener.onCheckedChanged(buttonView, isChecked);
    }

    /**
     * Simple focus change listener, appropriate for EditText fields that map directly to a
     * single codingfield.  Any field combining codingfields into a single EditText field
     * would require a listener with an appropriate parser and have a ListItemCodingDataGroup
     * object in the View's tag.
     *
     * @param v
     * @param hasFocus
     */
    @Override
    public void onFocusChange(View v, boolean hasFocus) {
        Log.d(TAG, "onFocusChange: ");
        ListItemCodingDataGroup.onFocusChangeListener.onFocusChange(v, hasFocus);
    }

    // region: Nested Classes / Interfaces


    // endregion: Nested Classes / Interfaces

    boolean validate() {
        Log.d(TAG, "validate: ");
        boolean valid = true;

        if (BusHelperFuelReceipts.getListFuelReceiptDetail() != null) {
            for (int i = 0; i < BusHelperFuelReceipts.getListFuelReceiptDetail().size(); i++) {
                ListItemCodingDataGroup listItemCodingDataGroup = BusHelperFuelReceipts.getListFuelReceiptDetail().get(i);
                Log.d(TAG, "validate: position: " + i + " label: " + listItemCodingDataGroup.getLabel() + " combinedValue: "
                        + listItemCodingDataGroup.getCombinedValue());
                if (i == 1 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: total amount of sale: ");
                    Toast.makeText(getActivity(), "Please enter total amount of sale", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 2 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: Gallons: ");
                    Toast.makeText(getActivity(), "Please enter Gallons", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 4 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: sales tax: ");
                    Toast.makeText(getActivity(), "Please enter sales tax", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 5 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: fuel type: ");
                    Toast.makeText(getActivity(), "Please enter fuel type", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 6 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: vendor name: ");
                    Toast.makeText(getActivity(), "Please enter vendor name", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 7 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: vendor state: ");
                    Toast.makeText(getActivity(), "Please enter vendor state", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 8 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: date: ");
                    Toast.makeText(getActivity(), "Please enter date", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

        } else {
            return false;
        }
        return valid;
    }

    //    public void onFocusChange(View v, boolean hasFocus) {
    public void afterTextChange(View v) {
        Log.d(TAG, "afterTextChange: ");
        try {
            String label = null;
            Object objTag = v.getTag();
            if (objTag != null && objTag instanceof AdapterUtils.ILabeled)
                label = ((AdapterUtils.ILabeled) objTag).getLabel();

            if (!(v instanceof EditText)) {
            } else {
                String val = null;

                if (v instanceof EditText)
                    val = ((EditText) v).getText().toString();
                else
                    Log.d(TAG, "afterTextChange() ***** Assertion Error.  Expecting view of EditText type, but found: "
                            + v.getClass().getSimpleName());

                BusHelperRmsCoding.CodingDataRow codingDataRow;

                if (objTag instanceof BusHelperRmsCoding.CodingDataRow) {

                    BusHelperRmsCoding.CodingDataRow itemCoding = (BusHelperRmsCoding.CodingDataRow) objTag;

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
            }
        } catch (Throwable e) {
            Log.d(TAG, "afterTextChange: throwable: " + e.getMessage());
            e.printStackTrace();
        }
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        Log.d(TAG, "onItemSelected: ");
        String value = parent.getItemAtPosition(position).toString();
        Object tag = parent.getTag();

        if (tag instanceof ListItemCodingDataGroup) {
            ListItemCodingDataGroup item = (ListItemCodingDataGroup) tag;
            Log.d(TAG, "onItemSelected() **** Case tag instance of "
                    + "ListItemCodingDataGroup, value=" + value);
            item.updateCombinedValue(value);
        } else {
            Log.d(TAG, "onItemSelected() **** Warning - tag is not instance of "
                    + "ListItemCodingDataGroup, tag.getClass().getSimpleName()=" + (tag != null ? tag.getClass().getSimpleName() : "(NULL)")
                    + " . No action taken. value=" + value);
        }
    }


    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        Log.d(TAG, "onNothingSelected: ");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(TAG, "onDestroyView: ");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "onDestroy: ");
    }


}