package com.rco.rcotrucks.activities.fuelreceipts.adapter;

import android.app.DatePickerDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.interfaces.TextWatcherListener;

import java.util.Calendar;
import java.util.List;

public class FuelReceiptDtlAdapter extends RecyclerView.Adapter<FuelReceiptDtlAdapter.FuelReceiptDtlViewHolder> {
    //    public static final String TAG = FuelReceiptDtlAdapter.class.getSimpleName();
//    public static final String TAG = FuelReceiptDtlAdapter.class.getSimpleName();
    public static final String TAG = "FRDActivity: Adapter";

    private Context ctx;
    private List<ListItemCodingDataGroup> mDataset;
    //    private TextWatcher textWatcher;
    private CompoundButton.OnCheckedChangeListener listenerCheckBoxSwitch;
    private View.OnClickListener onClickListener;
    private View.OnFocusChangeListener onFocusChangeListener;
    private AdapterView.OnItemSelectedListener onItemSelectedListener;
    private long idRmsRecords;
    TextWatcherListener textWatcherListener;

    /**
     * Constructor.
     *
     * @param context
     * @param myDataset              //     * @param textWatcher
     * @param listenerCheckBoxSwitch
     * @param imageClickListener
     */
    public FuelReceiptDtlAdapter(Context context, long idRmsRecords, List<ListItemCodingDataGroup> myDataset,
                                 TextWatcherListener textWatcherListener, CompoundButton.OnCheckedChangeListener listenerCheckBoxSwitch,
                                 View.OnClickListener imageClickListener, View.OnFocusChangeListener onFocusChangeListener,
                                 AdapterView.OnItemSelectedListener onItemSelectedListener) {
        ctx = context;
        this.idRmsRecords = idRmsRecords;
        mDataset = myDataset;
        // default listeners for recycler item views.  These can be ignored and custom listeners used.
//        this.textWatcher = textWatcher;
        this.textWatcherListener = textWatcherListener;
        this.listenerCheckBoxSwitch = listenerCheckBoxSwitch;
        this.onClickListener = imageClickListener;
        this.onFocusChangeListener = onFocusChangeListener;
        this.onItemSelectedListener = onItemSelectedListener;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FuelReceiptDtlViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Log.d(TAG, "ViewHolderCodingDataGroup.onCreateViewHolder() Start.  parent: " + parent.toString()
                + ", viewType=" + viewType);
        // create a new view
        View v = null;

        switch (viewType) {
            case Cadp.VIEWTYPE_HDR:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr_update, parent, false);
                break;

            case Cadp.VIEWTYPE_FLD:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_fld_update, parent, false);
                break;

            case Cadp.VIEWTYPE_LBL_FLD:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_lbl_fld_update, parent, false);
                break;

            case Cadp.VIEWTYPE_HDR_FLD:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr_fld_update, parent, false);
                break;

            case Cadp.VIEWTYPE_HDR_FLD_SIG:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr_fld_sig_update, parent, false);
                break;

            case Cadp.VIEWTYPE_HDR_SPIN_HID:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr_spin_hid_update, parent, false);
                break;

            case Cadp.VIEWTYPE_LBL_CHK:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_lbl_chk_update, parent, false);
                break;

            case Cadp.VIEWTYPE_HDR_CMT:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr_cmt_update, parent, false);
                break;

            case Cadp.VIEWTYPE_PIC:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_picture, parent, false);
                break;

            default:
                Log.d(TAG, "onCreateViewHolder() **** Error.  Unknown viewType=" + viewType);
                break;
        }
//        ViewHolderCodingDataGroup vh = new HeaderItemViewHolder(v, listener);
        FuelReceiptDtlViewHolder vh = null;

        if (v != null) {
//            June 30, 2022 -
//            vh = new ItemViewHolder(v, viewType,
//                    new ItemTextWatcher(),
//                    listenerCheckBoxSwitch, onClickListener, onFocusChangeListener, onItemSelectedListener);
            vh = new ItemViewHolder(ctx, v, viewType,
                    textWatcherListener,
                    listenerCheckBoxSwitch, onClickListener, onFocusChangeListener, onItemSelectedListener);
        }

        // Todo: set onclick listeners;

        Log.d(TAG, "ViewHolderCodingDataGroup.onCreateViewHolder() End.  parent: " + (parent != null ? parent.toString() : "(NULL)")
                + ", viewType=" + viewType + ", vh.toString()=" + (vh != null ? vh.toString() : "(NULL)"));

        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getViewType();
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(FuelReceiptDtlViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        Log.d(TAG, "onBindViewHolder() position=" + position);
        ListItemCodingDataGroup item = mDataset.get(position);
        Log.d(TAG, "onBindViewHolder() position=" + position + ", item=" + item);
        holder.setItem(position, item);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        Log.d(TAG, "getItemCount() mDataset.size()=" + mDataset.size());
        return mDataset.size();
    }


//    public void setErrorFromAdapter(int position, ListItemCodingDataGroup item) {
//        setError(position,item);
//        ItemViewHolder itemViewHolder=new ItemViewHolder();
//    }

    // region Nested Classes / Interfaces

    // ======================================== Nested Classes ============================

    /**
     *
     */
    public static abstract class FuelReceiptDtlViewHolder extends RecyclerView.ViewHolder {
        private int viewType;

        public int getViewType() {
            return viewType;
        }

        public void setViewType(int viewType) {
            this.viewType = viewType;
        }

        public FuelReceiptDtlViewHolder(View itemView) {
            super(itemView);
        }

        public abstract void setItem(int ixItemList, ListItemCodingDataGroup listItemCodingDataRow);
    }

    /**
     * @param <T>
     */
    public interface IViewHolderItemSetter<T> {
        void setItem(T item);
    }

    public static class ItemViewHolder extends FuelReceiptDtlViewHolder {
        Context context;
        public View view;
        public int viewType;
        private TextView vLabel;
        private EditText eValue;
        private Switch checkBoxSwitch;
        private ImageView imgOne;
        private TextView vIsRequired;
        private Spinner spinner;
        //        String dataTypeName;
        private CompoundButton.OnCheckedChangeListener listenerCheckBox;
        private View.OnClickListener imageClickListener;
        //        ItemTextWatcher textWatcher;
        private View.OnFocusChangeListener onFocusChangeListener;
        private ListItemCodingDataGroup item;
        private AdapterView.OnItemSelectedListener onItemSelectedListener;
        TextWatcherListener textWatcherListener;
        String selectedDate = "";

//        public ItemViewHolder(View itemView) {
//            super(itemView);
//        }

        //        public ItemViewHolder(View v, int viewType,
//                              ItemTextWatcher textWatcher,
//                              CompoundButton.OnCheckedChangeListener listenerCheckBox,
//                              View.OnClickListener imageClickListener,
//                              View.OnFocusChangeListener onFocusChangeListener,
//                              AdapterView.OnItemSelectedListener onItemSelectedListener) {
        public ItemViewHolder(Context context, View v, int viewType,
                              TextWatcherListener textWatcherListener,
                              CompoundButton.OnCheckedChangeListener listenerCheckBox,
                              View.OnClickListener imageClickListener,
                              View.OnFocusChangeListener onFocusChangeListener,
                              AdapterView.OnItemSelectedListener onItemSelectedListener) {
            super(v); // This is critical for RecyclerView to get the view later.
            this.context = context;
            view = v;
            this.viewType = viewType;
            this.listenerCheckBox = listenerCheckBox;
            this.imageClickListener = imageClickListener;
            this.onFocusChangeListener = onFocusChangeListener;
            this.onItemSelectedListener = onItemSelectedListener;
            this.textWatcherListener = textWatcherListener;

            vLabel = v.findViewById(R.id.textViewLbl);
            eValue = v.findViewById(R.id.textViewFldOne);
            checkBoxSwitch = v.findViewById(R.id.switchChk);
            imgOne = v.findViewById(R.id.imageViewOne);
            vIsRequired = v.findViewById(R.id.textViewIcon);
            spinner = v.findViewById(R.id.spinner1);
//            if (eValue != null && textWatcher != null) eValue.addTextChangedListener(textWatcher);
            if (checkBoxSwitch != null && listenerCheckBox != null)
                checkBoxSwitch.setOnCheckedChangeListener(listenerCheckBox);
            if (imgOne != null && imageClickListener != null)
                imgOne.setOnClickListener(imageClickListener);
            if (eValue != null) {
//                eValue.setOnFocusChangeListener(onFocusChangeListener);
                // we could add textWatcher to eValue here, but that takes place also in setItem to prevent
                // being called when eValue.setText() is called, and we don't want to risk adding more than one.
            }

            if (spinner != null) {
                Log.d(TAG, "ItemViewHolder constructor, case: spinner != null, "
                        + "calling spinner.setOnItemSelectedListener(onItemSelectedListener), onItemSelectedListener class: "
                        + onItemSelectedListener.getClass().getSimpleName());
                spinner.setOnItemSelectedListener(onItemSelectedListener);
            }
//            displayType = "edittext";
        }

//        public void setError(int position, ListItemCodingDataGroup item) {
//
//        }

        public void setItem(int position, ListItemCodingDataGroup item) {
            Log.d(TAG, "setItem: position: " + position + " item: " + item.getLabel() + " value: " + item.getCombinedValue());
            String strThis = "ItemViewHolder.setItem(), ";
            this.item = item;

//            dataTypeName = item.getDataTypeName();

            Log.d(TAG, strThis + "Start. item=" + item);

            if (item.getArBitmapItems() != null && item.getArBitmapItems().length > 0) {
                Log.d(TAG, "setItem: image: length: " + item.getArBitmapItems().length);
                Log.d(TAG, "setItem: image: at zeroIndex: bitmap: " + item.getArBitmapItems()[0]);
            }

            if (vLabel != null) {
                vLabel.setText(item.getLabel());
            }

            if (eValue != null) {
//                eValue.removeTextChangedListener(textWatcher);
                String combinedValue = item.getCombinedValue();
                eValue.setText(combinedValue);
                Log.d(TAG, strThis + "After setting EditText, combinedValue=" + combinedValue + ", eValue.getText().toString()=" + eValue.getText().toString() + ", item=" + item);
//                textWatcher.setItem(item);
//                eValue.addTextChangedListener(textWatcher);
//                eValue.setTag(new Integer(position));
                if (item.getCombineType() == Cadp.COMBINE_TYPE_NONE)
                    eValue.setTag(item.getListCodingdataRows().get(0));
                else
                    eValue.setTag(item);

                if (item.getEditMode() != Cadp.EDIT_MODE_READONLY && item.getEditMode() != Cadp.EDIT_MODE_LOOKUP) {
                    Log.d(TAG, strThis + "Case: Not read-only or lookup, editable EditText.  Setting onFocusChangeListener and optional hint.");
                    eValue.setOnFocusChangeListener(onFocusChangeListener);

                    if ("Date".equals(item.getDataTypeName())) {
                        eValue.setInputType(InputType.TYPE_DATETIME_VARIATION_DATE);
                        eValue.setHint("mm/dd/yyyy");
                    } else if ("DateTime".equals(item.getDataTypeName())) {
                        eValue.setInputType(InputType.TYPE_DATETIME_VARIATION_NORMAL);
                        eValue.setHint("mm/dd/yyyy hh:mm:ss");
                    } else {
                        eValue.setInputType(InputType.TYPE_CLASS_TEXT);
                        eValue.setHint(null);
                    }
                } else {
                    Log.d(TAG, strThis + "Case: read-only or lookup, item.getEditMode()=" + item.getEditMode());
                    eValue.setInputType(InputType.TYPE_NULL); // We'll see if this makes it readonly.
                }

                eValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
//                            Log.d(TAG, strThis + "textWatcher: beforeTextChanged: ");
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
//                            Log.d(TAG, strThis + "textWatcher: onTextChanged: ");
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        validateTextWatcherText(eValue, item.getLabel(), s.toString(), position);
//                        void validateTextWatcherText(View view, String label, String textValue, int position) {
                    }
                });

                eValue.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (item.getLabel().equalsIgnoreCase("Date")) {
                            Log.d(TAG, "onClick: editDetail");

                            Calendar calendar = Calendar.getInstance();
                            final int selectedYear = calendar.get(Calendar.YEAR);
                            final int selectedMonth = calendar.get(Calendar.MONTH);
                            final int selectedDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);


                            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                    selectedDate = String.format("%02d/%02d/%02d", (monthOfYear + 1), dayOfMonth, year);

//                                    String currentTime = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_TIME_MILLIS);
//                                    String currentTime = DateUtils.getDateTime(System.currentTimeMillis(), DateUtils.FORMAT_DATE_TIME_MILLIS);
//                                    String[] splitCurrentDateTime = currentTime.split(" ");
//
//                                    String currentTimeInMilli = "";
//                                    if (splitCurrentDateTime != null) {
//                                        if (splitCurrentDateTime.length > 1) {
//                                            currentTimeInMilli = splitCurrentDateTime[1].trim();
//                                        }
//                                    }

//                                    eValue.setText(selectedDate + " " + currentTimeInMilli);
                                    eValue.setText(selectedDate);
                                    Log.d(TAG, "onDateSet: selectedDate: "+selectedDate+" now: call: afterTextChanged: ");
                                    textWatcherListener.afterTextChanged(eValue, selectedDate, position);
//                                    validateTextWatcherText(eValue, item.getLabel(), eValue.getText().toString(), position);
                                }
                            };
                            DatePickerDialog datePickerDialog = new DatePickerDialog(itemView.getContext(),
                                    dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);
                            datePickerDialog.show();

                        }


//                        Calendar mcurrentTime = Calendar.getInstance();
//                        int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
//                        int minute = mcurrentTime.get(Calendar.MINUTE);
//                        TimePickerDialog mTimePicker;
//                        mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
//                            @Override
//                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                                selectedDate = selectedDate + " " + (hourOfDay + ":" + minute);
//                                eValue.setText(selectedDate);
//                            }
//                        }, hour, minute, true);//Yes 24 hour time
//                        mTimePicker.setTitle("Select Time");
//                        mTimePicker.show();

                    }
                });

            }

            if (checkBoxSwitch != null) {
                checkBoxSwitch.setOnCheckedChangeListener(null);
                checkBoxSwitch.setChecked("1".equals(item.getCombinedValue()));
                checkBoxSwitch.setOnCheckedChangeListener(this.listenerCheckBox);
//                checkBoxSwitch.setTag(new Integer(position));
                checkBoxSwitch.setTag(item.getListCodingdataRows().get(0));
            }

//            if (vIsRequired != null) {
//                if (item.isRequired()) vIsRequired.setVisibility(View.VISIBLE);
//                else vIsRequired.setVisibility(View.INVISIBLE);
//            }
            if (item.getLabel().equalsIgnoreCase("Odometer")) {
                vIsRequired.setVisibility(View.GONE);
            }


//            if (imgSig != null) imgSig.setTag(new Integer(position));
            if (imgOne != null) {
                imgOne.setTag(position);
                Bitmap bitmap = null;

                if (item.getArBitmapItems() != null) {
                    for (AdapterUtils.BitmapItem bitmapItem : item.getArBitmapItems()) {
                        if (bitmapItem != null && (bitmapItem.bitmapClass == Cadp.BITMAP_CLASS_SIGNATURE
                                || bitmapItem.bitmapClass == Cadp.BITMAP_CLASS_RECORD_CONTENT)) {
//                        imgOne.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                        imgSig.setScaleX(1.f/16.f);
//                        imgSig.setScaleY(1.f/16.f);
                            bitmap = bitmapItem.bitmap;
                            if (bitmapItem.bitmapClass == Cadp.BITMAP_CLASS_RECORD_CONTENT)
                                imgOne.setAdjustViewBounds(true);
                            break;
                        } else {
                            Log.d(TAG, strThis + ", bitmapItem is null");
                        }
                    }
                } else Log.d(TAG, strThis + ", item.getArBitmapItems() is null");

                if (bitmap != null && bitmap.getByteCount() > 0) {
                    imgOne.setImageBitmap(bitmap);
                    imgOne.setVisibility(View.VISIBLE);
                    Log.d(TAG, strThis + "\" + bitmap byte count: " + bitmap.getByteCount()
                            + ", height: " + bitmap.getHeight() + ", width: " + bitmap.getWidth());
                } else {
                    imgOne.setImageBitmap(null);
                    imgOne.setVisibility(View.INVISIBLE);
                    imgOne.setAdjustViewBounds(false);
                }
            }

            if (spinner != null) {
                Log.d(TAG, strThis + "Case: spinner != null, setting tag and adapter.");
                int ix = 0;
                spinner.setTag(item);
                ArrayAdapter<String> adapter = item.getSpinnerAdapter();
                if (adapter != null) {
                    spinner.setAdapter(adapter);
                    spinner.setSelection(item.getIxSelectedSpinnerItem());
                } else
                    Log.d(TAG, strThis + "***** Assertion error - spinner view is not null, but item.getSpinnerAdapter() is null.");
            } else
                Log.d(TAG, strThis + "Case: spinner == null.");

            setViewType(item.getViewType());

            if (item.getLabel().contains("Total Amount of Sale in USD")) {
                vLabel.setText("Amount");
                eValue.setHint("Total Amount of Sale");
                eValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }

            if (item.getLabel().equalsIgnoreCase("Gallons")) {
                eValue.setHint("Gallons");
                eValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }

            Log.d(TAG, "setItem: label: " + item.getLabel());
            if (item.getLabel().contains("Odometer")) {
                eValue.setHint("Odometer");
                eValue.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            if (item.getLabel().contains("Vendor Name")) {
                vLabel.setText("Vendor");
                eValue.setHint("Vendor Name");
            }

            if (item.getLabel().equalsIgnoreCase("Vendor State")) {
                vLabel.setText("State");
                eValue.setHint("Vendor State");
            }

            if (item.getLabel().equalsIgnoreCase("Date")) {
                eValue.setHint("Date");
//                June 01, 2022 -   fuel receipt allover behaves differently,
                eValue.setInputType(InputType.TYPE_NULL);
                eValue.setFocusableInTouchMode(false);
                eValue.setClickable(true);
            }

            if (item.getLabel().equalsIgnoreCase("Sales Tax")) {
                eValue.setHint("Sales Tax");
                eValue.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL | InputType.TYPE_NUMBER_FLAG_SIGNED);
            }

            if (item.getLabel().contains("Fuel Type")) {
                eValue.setHint("Fuel Type");
            }


            if (item.getLabel().contains("Fuel Type")) {
                eValue.setHint("Fuel Type");
            }

        }

        void validateTextWatcherText(View view, String label, String textValue, int position) {
            Log.d(TAG, "validateTextWatcherText: label: " + label + " position: " + position + " textValue: " + textValue);
            textWatcherListener.afterTextChanged(view, textValue, position);
        }

    }

    public static class ItemTextWatcher implements TextWatcher {
        private BusHelperRmsCoding.CodingDataRow item;

        public void setItem(BusHelperRmsCoding.CodingDataRow item) {
            Log.d(TAG, "textWatcher: ItemTextWatcher: setItem: ");
            this.item = item;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            Log.d(TAG, "textWatcher: ItemTextWatcher: beforeTextChanged: ");
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            Log.d(TAG, "textWatcher: ItemTextWatcher: onTextChanged: ");
            item.updateValueFromDisplay(s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "textWatcher: ItemTextWatcher: afterTextChanged: ");
        }
    }

}