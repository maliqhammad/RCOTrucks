package com.rco.rcotrucks.activities.fuelreceipts.activities;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.adapter.FuelReceiptDtlAdapter;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.CameraDialogFragment;
import com.rco.rcotrucks.activities.fuelreceipts.fragments.FuelReceiptListFragment;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.activities.fuelreceipts.utils.UiHelperFuelReceiptDtl;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.recordcommon.RecordRulesHelper;
import com.rco.rcotrucks.interfaces.TextWatcherListener;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public class TollReceiptDetailActivity extends BaseActivity implements CompoundButton.OnCheckedChangeListener,
        View.OnClickListener, View.OnFocusChangeListener, AdapterView.OnItemSelectedListener,
        CameraDialogFragment.ICameraDialogListener {

    private static String TAG = TollReceiptDetailActivity.class.getSimpleName();
    public static final int REQUEST_IMAGE_CAPTURE = 2, REQUESTCODE_PICK_PHOTO = 3;

    LinearLayout bottomBar;
    public String objectId = null, objectType = null, recordId = null;
    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private LinearLayoutManager layoutManager;

    private ImageView textViewFuelReceiptDtlCancel, imageViewCamera;
    private TextView textViewFuelReceiptDtlSave, textViewFuelReceiptDtlDelete, title;

    private BusHelperFuelReceipts busRules = BusHelperFuelReceipts.instance();
    private BusHelperRmsCoding.RmsRecords identRmsRecords;
    private String[] arCameraChoices;

    Uri uri;
    List<ListItemCodingDataGroup> listItems;

    void getIntentData() {
        Intent intent = getIntent();
        Serializable message = intent.getSerializableExtra(FuelReceiptListFragment.EXTRA_MESSAGE_FUEL_RECEIPT_IDENT);
        if (message != null && message instanceof BusHelperRmsCoding.RmsRecords)
            identRmsRecords = (BusHelperRmsCoding.RmsRecords) message;
        else {
            identRmsRecords = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_toll_receipt_detail);

        getIntentData();
        setIds();
        initialize();
        setListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Date,  Gallons, Amount, Sales Tax, Vendor, State, Fuel Type, Odometer
        UiHelperFuelReceiptDtl.instance().runRefreshTask(this, identRmsRecords.getIdRecord(),
                identRmsRecords.getObjectId(), identRmsRecords.getObjectType());
    }

    void setIds() {
        textViewFuelReceiptDtlCancel = findViewById(R.id.btn_back);
        bottomBar = findViewById(R.id.layoutFuelReceiptDtlBotBar);
        title = findViewById(R.id.tv_title);
        textViewFuelReceiptDtlSave = findViewById(R.id.textViewSave);
        textViewFuelReceiptDtlDelete = findViewById(R.id.textViewDelete);
        imageViewCamera = findViewById(R.id.imageViewCameraIcon);
    }

    void initialize() {
//        July 26, 2022 -   Roy told to remove it on e-mail - date of e-mail is July 23, 2022    -
        title.setText("Toll Receipt");
        arCameraChoices = getResources().getStringArray(R.array.camera_choices);
        bottomBar.setVisibility(View.GONE);
    }

    void setListener() {
        textViewFuelReceiptDtlCancel.setOnClickListener(this);
        textViewFuelReceiptDtlDelete.setOnClickListener(this);
        imageViewCamera.setOnClickListener(this);

        textViewFuelReceiptDtlSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: now validate: ");
                if (!validate()) {

                } else {

                    busRules.runSaveFuelReceiptDtlTask(TollReceiptDetailActivity.this,
                            identRmsRecords.getIdRecord(), BusHelperFuelReceipts.getListFuelReceiptDetail(),
                            BusHelperFuelReceipts.getListExtra(),
                            busRules.getFuelReceiptExtraPostProcessor(), false);
                    finish();
                }
            }
        });
    }

    public void loadContentView() {
        loadRecyclerView();
    }

    private void loadRecyclerView() {
        // Todo: implement searchbox
        listItems = busRules.getListFuelReceiptDetail();


        Log.d(TAG, "loadRecyclerView() start, listItems: size: " + listItems.size());
        for (int i = 0; i < listItems.size(); i++) {
            Log.d(TAG, "loadRecyclerView: item: "+listItems.get(i).getLabel());
        }

        if (recyclerView == null) {
            Log.d(TAG, "loadRecyclerView() recyclerView is null, initializing recyclerView.");
            recyclerView = (RecyclerView) findViewById(R.id.recyclerView);

            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            // recyclerView.setHasFixedSize(true);

            // use a linear layout manager
            if (layoutManager == null) layoutManager = new LinearLayoutManager(this);
            recyclerView.setLayoutManager(layoutManager);

            DividerItemDecoration mDividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                    layoutManager.getOrientation());

            Log.d(TAG, "loadRecyclerView: " + mDividerItemDecoration.toString());
            recyclerView.addItemDecoration(mDividerItemDecoration);

            mAdapter = new FuelReceiptDtlAdapter(TollReceiptDetailActivity.this, identRmsRecords.getIdRecord(), listItems,
                    new TextWatcherListener() {
                        @Override
                        public void afterTextChanged(View view, String value, int position) {
                            afterTextChange(view);
                        }
                    },
                    this, this, this, this);
            recyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onClick(View v) {
        UiUtils.closeKeyboard(v);

        if (v == imageViewCamera) {
//            pickImage(TollReceiptDetailActivity.this);
        } else {
            if (v == textViewFuelReceiptDtlDelete) {
                // Todo: Delete this receipt. (mark for delete)
                UiUtils.showBooleanDialog(TollReceiptDetailActivity.this, "Delete",
                        R.drawable.ic_circlered, "Yes", "No", "Do you want to delete this receipt?",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // If it is a new record (idRmsRecords < 0) and user hits delete, no need to update database or RMS -- should be same as "Cancel" for a new record.  We could hide Delete button for new records as an alternative.
                                if (identRmsRecords.getIdRecord() >= 0) {
//                                    BusHelperFuelReceipts.instance().runSaveFuelReceiptDtlTask(TollReceiptDetailActivity.this,
//                                            identRmsRecords.getIdRecord(), busRules.getListFuelReceiptDetail(),
//                                            busRules.getListExtra(), null, true);
                                    // Get a new helper object because database update operations might use compiled statements, not thread safe.
                                    BusHelperFuelReceipts busHelperFuelReceipts = new BusHelperFuelReceipts(RecordRulesHelper.getDb());
                                    busHelperFuelReceipts.runSaveFuelReceiptDtlTask(TollReceiptDetailActivity.this,
                                            identRmsRecords.getIdRecord(), BusHelperFuelReceipts.getListFuelReceiptDetail(),
                                            BusHelperFuelReceipts.getListExtra(), null, true);
                                    finish();
                                } else cancel();
                            }
                        }
                );
            } else if (v == textViewFuelReceiptDtlCancel) {
                cancel();
            }

        }
    }

    public void cancel() {
        if (BusHelperFuelReceipts.getListFuelReceiptDetail() != null) {
            BusHelperFuelReceipts.getListFuelReceiptDetail().clear(); // or should it be set to null?
        }

        finish();
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
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
        ListItemCodingDataGroup.onFocusChangeListener.onFocusChange(v, hasFocus);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.d(TAG, "onActivityResult: ");
        if (requestCode == REQUEST_CODE_FOR_IMAGE) {
            if (resultCode == Activity.RESULT_OK) {
                uri = data.getParcelableExtra("path");
                Log.d(TAG, "onActivityResult: uri: " + uri);
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
                    setListItemPhoto(bitmap);

                } catch (IOException e) {
                    Log.d(TAG, "onActivityResult: IOException: " + e.getMessage());
                    try {
                        Bitmap mImageBitmap = MediaStore.Images.Media.getBitmap(getApplicationContext().getContentResolver(), Uri.fromFile(new File(uri.toString())));
                        setListItemPhoto(mImageBitmap);
                    } catch (IOException ioException) {
                        Log.d(TAG, "onActivityResult: IOException: inner: " + ioException.getMessage());
                    }

                }
            }
        } else {
            mAdapter.notifyDataSetChanged();
        }
    }

    public void setListItemPhoto(Bitmap bitmap) {
        Log.d(TAG, "setListItemPhoto: bitmap: " + bitmap + " BusHelperFuelReceipts.getListFuelReceiptDetail(): " + BusHelperFuelReceipts.getListFuelReceiptDetail());
//        if (busRules.getListFuelReceiptDetail() != null) {
        if (BusHelperFuelReceipts.getListFuelReceiptDetail() != null) {
            // First item on list is always the Fuel Receipt picture.
//            ListItemCodingDataGroup item = busRules.getListFuelReceiptDetail().get(0);
            ListItemCodingDataGroup item = BusHelperFuelReceipts.getListFuelReceiptDetail().get(0);
            Log.d(TAG, "setListItemPhoto: item: " + item);
            AdapterUtils.BitmapItem[] arBitmaps = item.getArBitmapItems();

            Log.d(TAG, "setListItemPhoto: arBitmaps: " + arBitmaps);
            if (arBitmaps != null && arBitmaps.length > 0) {
                Log.d(TAG, "setListItemPhoto: arBitmaps.length: " + arBitmaps.length);
                arBitmaps[0].setBitmap(bitmap);

                Log.d(TAG, "setListItemPhoto: bitmap at 0th index: " + arBitmaps[0].bitmap);
                Log.d(TAG, "setListItemPhoto: mAdapter: " + mAdapter);
                if (mAdapter == null) {
                    loadRecyclerView();
                } else {
                    mAdapter.notifyItemChanged(0);
                }
            }
        }
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
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

    }

    @Override
    public void onDialogSelectCameraAction(DialogFragment dialog, int which) {
        Log.d(TAG, "onDialogSelectCameraAction() I'm clicked. which=" + which);

        String cameraChoice = arCameraChoices[which];

        if (getResources().getString(R.string.item_take).equals(cameraChoice)) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, REQUEST_IMAGE_CAPTURE);
            } else
                Log.d(TAG, "onDialogSelectCameraAction() ***** Error. intent.resolveActivity(getPackageManager()) is null. cameraChoice=" + cameraChoice + ", which=" + which);
        } else if (getResources().getString(R.string.item_select).equals(cameraChoice)) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent.createChooser(intent, "Pick Photo"), REQUESTCODE_PICK_PHOTO);
            } else
                Log.d(TAG, "onDialogSelectCameraAction() ***** Error. intent.resolveActivity(getPackageManager()) is null. cameraChoice=" + cameraChoice + ", which=" + which);

        } else if (getResources().getString(R.string.item_cancel).equals(cameraChoice)) {
            // Do nothing.
        } else Log.d(TAG, "Dialog onClick(), unrecognized choice, which=" + which);

        dialog.dismiss();
    }

    public void startPhotoPicker(View view) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(intent, REQUESTCODE_PICK_PHOTO);
        }
    }
    // region: Nested Classes / Interfaces


    // endregion: Nested Classes / Interfaces

    boolean validate() {
        boolean valid = true;

        if (BusHelperFuelReceipts.getListFuelReceiptDetail() != null) {
            for (int i = 0; i < BusHelperFuelReceipts.getListFuelReceiptDetail().size(); i++) {
                ListItemCodingDataGroup listItemCodingDataGroup = BusHelperFuelReceipts.getListFuelReceiptDetail().get(i);
                Log.d(TAG, "validate: position: " + i + " label: " + listItemCodingDataGroup.getLabel() + " combinedValue: "
                        + listItemCodingDataGroup.getCombinedValue());
                if (i == 1 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: total amount of sale: ");
                    Toast.makeText(this, "Please enter total amount of sale", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 2 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: Gallons: ");
                    Toast.makeText(this, "Please enter Gallons", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 4 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: sales tax: ");
                    Toast.makeText(this, "Please enter sales tax", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 5 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: fuel type: ");
                    Toast.makeText(this, "Please enter fuel type", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 6 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: vendor name: ");
                    Toast.makeText(this, "Please enter vendor name", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 7 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: vendor state: ");
                    Toast.makeText(this, "Please enter vendor state", Toast.LENGTH_SHORT).show();
                    return false;
                } else if (i == 8 && listItemCodingDataGroup.getCombinedValue().isEmpty()) {
                    Log.d(TAG, "validate: date: ");
                    Toast.makeText(this, "Please enter date", Toast.LENGTH_SHORT).show();
                    return false;
                }
            }

        } else {
            return false;
        }
        return valid;
    }

    public void afterTextChange(View v) {
//        Log.d(TAG, "afterTextChange: ");
//        Integer intIxListItems = (Integer) v.getTag();
        try {
//            Log.d(TAG, "onFocusChange() Start. hasFocus=" + hasFocus // + ", intIxListItems=" + intIxListItems
//                    + ", v.getClass().getName()=" + v.getClass().getName() + ", v.getId()=" + v.getId());
            String label = null;
            Object objTag = v.getTag();
//            Log.d(TAG, "afterTextChange: objTag: " + objTag);
            if (objTag != null && objTag instanceof AdapterUtils.ILabeled)
                label = ((AdapterUtils.ILabeled) objTag).getLabel();
//            Log.d(TAG, "afterTextChange: label: " + label);

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

}
