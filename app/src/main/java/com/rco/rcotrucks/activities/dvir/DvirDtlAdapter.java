package com.rco.rcotrucks.activities.dvir;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.interfaces.TextWatcherListener;
import com.rco.rcotrucks.views.ViewHolderCodingDataGroup;

import java.util.Calendar;
import java.util.List;

public class DvirDtlAdapter extends RecyclerView.Adapter<ViewHolderCodingDataGroup> {
    public static final String TAG = "DvirDtlAdapter";

    private Activity ctx;
    private List<ListItemCodingDataGroup> mDataset;
    private TextWatcher textWatcher;
    private View.OnClickListener onClickListener;
    private CompoundButton.OnCheckedChangeListener listenerCheckBoxSwitch;
//    private View.OnFocusChangeListener onFocusChangeListener;
    private long idRmsRecords;
    SignatureSelectionListener signatureSelectionListener;
    TextWatcherListener textWatcherListener;


    /**
     * @param context
     * @param idRmsRecords
     * @param myDataset              //     * @param textWatcher
     * @param listenerCheckBoxSwitch
     * @param imageClickListener
//     * @param onFocusChangeListener
     */

//    public DvirDtlAdapter(Activity context, long idRmsRecords, List<ListItemCodingDataGroup> myDataset,
//                          TextWatcher textWatcher, CompoundButton.OnCheckedChangeListener listenerCheckBoxSwitch,
//                          View.OnClickListener imageClickListener, View.OnFocusChangeListener onFocusChangeListener,
//                          SignatureSelectionListener signatureSelectionListener) {
    public DvirDtlAdapter(Activity context, long idRmsRecords, List<ListItemCodingDataGroup> myDataset,
                          TextWatcherListener textWatcherListener, CompoundButton.OnCheckedChangeListener listenerCheckBoxSwitch,
                          View.OnClickListener imageClickListener,
                          SignatureSelectionListener signatureSelectionListener) {
//                          View.OnClickListener imageClickListener, View.OnFocusChangeListener onFocusChangeListener,
        ctx = context;
        this.idRmsRecords = idRmsRecords;
        mDataset = myDataset;
        // default listeners for recycler item views.  These can be ignored and custom listeners used.
        this.textWatcher = textWatcher;
        this.listenerCheckBoxSwitch = listenerCheckBoxSwitch;
        this.onClickListener = imageClickListener;
//        this.onFocusChangeListener = onFocusChangeListener;
        this.signatureSelectionListener = signatureSelectionListener;
        this.textWatcherListener = textWatcherListener;

        for (int i = 0; i < mDataset.size(); i++) {
            Log.d(TAG, "loadRecyclerView: label: title: " + mDataset.get(i).getLabel());
        }

    }


    public void filterList(List<ListItemCodingDataGroup> filteredNames) {
        this.mDataset = filteredNames;
        notifyDataSetChanged();
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ViewHolderCodingDataGroup onCreateViewHolder(ViewGroup parent, int viewType) {
//        Log.d(TAG, "ViewHolderCodingDataGroup.onCreateViewHolder() Start.  parent: " + parent.toString()
//                + ", viewType=" + viewType);
        // create a new view
        View v = null;

        switch (viewType) {
            case Cadp.VIEWTYPE_HDR:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr, parent, false);
                break;

            case Cadp.VIEWTYPE_FLD:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_fld, parent, false);
                break;

            case Cadp.VIEWTYPE_LBL_FLD:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_lbl_fld, parent, false);
                break;

            case Cadp.VIEWTYPE_HDR_FLD:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr_fld, parent, false);
                break;

            case Cadp.VIEWTYPE_LBL_CHK:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_lbl_chk, parent, false);
                break;

            case Cadp.VIEWTYPE_HDR_CMT:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr_cmt, parent, false);
                break;

            case Cadp.VIEWTYPE_HDR_FLD_SIG:
                v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.listitem_hdr_fld_sig, parent, false);
                break;

            default:
                Log.d(TAG, "onCreateViewHolder() **** Error.  Unknown viewType=" + viewType);
                break;
        }
//        ViewHolderCodingDataGroup vh = new HeaderItemViewHolder(v, listener);
        ViewHolderCodingDataGroup vh = null;

        if (v != null) {
//            vh = new ItemViewHolder(v, viewType,
//                    null, // new ItemTextWatcher(),
//                    listenerCheckBoxSwitch, onClickListener, onFocusChangeListener, signatureSelectionListener);
            vh = new ItemViewHolder(ctx, v, viewType,
                    textWatcherListener, // new ItemTextWatcher(),
                    listenerCheckBoxSwitch, onClickListener, signatureSelectionListener);
//                    listenerCheckBoxSwitch, onClickListener, onFocusChangeListener, signatureSelectionListener);
        }

        // Todo: set onclick listeners;

//        Log.d(TAG, "ViewHolderCodingDataGroup.onCreateViewHolder() End.  parent: " + (parent != null ? parent.toString() : "(NULL)")
//                + ", viewType=" + viewType + ", vh.toString()=" + (vh != null ? vh.toString() : "(NULL)"));

        return vh;
    }

    @Override
    public int getItemViewType(int position) {
        return mDataset.get(position).getViewType();
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolderCodingDataGroup holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
//        Log.d(TAG, "onBindViewHolder() position=" + position);
        ListItemCodingDataGroup item = mDataset.get(position);
//        Log.d(TAG, "onBindViewHolder() position=" + position + ", item=" + item);
//        Log.d(TAG, "onBindViewHolder: position: " + position + " label: " + item.getLabel());
        holder.setItem(position, item);

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
//        Log.d(TAG, "getItemCount() mDataset.size()=" + mDataset.size());
        return mDataset.size();
    }

    // region Nested Classes / Interfaces

    // ======================================== Nested Classes ============================

    public static class ItemViewHolder extends ViewHolderCodingDataGroup {
        Context context;
        public View view;
        public int viewType;
        TextView vLabel;
        EditText eValue;
        Switch checkBoxSwitch;
        ImageView imgSig;
        TextView vIsRequired;
        //        String dataTypeName;
        CompoundButton.OnCheckedChangeListener listenerCheckBox;
        View.OnClickListener imageClickListener;
        //        ItemTextWatcher textWatcher;
//        View.OnFocusChangeListener onFocusChangeListener;
        ListItemCodingDataGroup item;
        SignatureSelectionListener signatureSelectionListener;
        TextWatcherListener textWatcherListener;
        String selectedDate = "", mHour = "", mMinutes = "", mTimeZone = "";

        //        public ItemViewHolder(View v, int viewType,
//                              ItemTextWatcher textWatcher,
//                              CompoundButton.OnCheckedChangeListener listenerCheckBox,
//                              View.OnClickListener imageClickListener,
//                              View.OnFocusChangeListener onFocusChangeListener,
//                              SignatureSelectionListener signatureSelectionListener) {

//        View.OnFocusChangeListener onFocusChangeListener,
        public ItemViewHolder(Context context, View v, int viewType,
                              TextWatcherListener textWatcherListener,
                              CompoundButton.OnCheckedChangeListener listenerCheckBox,
                              View.OnClickListener imageClickListener,
                              SignatureSelectionListener signatureSelectionListener) {
            super(v); // This is critical for RecyclerView to get the view later.
            this.context = context;
            view = v;
            this.viewType = viewType;
//            this.textWatcher = textWatcher;
            this.listenerCheckBox = listenerCheckBox;
            this.imageClickListener = imageClickListener;
//            this.onFocusChangeListener = onFocusChangeListener;
            this.signatureSelectionListener = signatureSelectionListener;
            this.textWatcherListener = textWatcherListener;

            vLabel = v.findViewById(R.id.textViewLbl);
            eValue = v.findViewById(R.id.textViewFldOne);
            checkBoxSwitch = v.findViewById(R.id.switchChk);
            imgSig = v.findViewById(R.id.imageViewOne);
            vIsRequired = v.findViewById(R.id.textViewIcon);

//            if (eValue != null && textWatcher != null) eValue.addTextChangedListener(textWatcher);
            if (checkBoxSwitch != null && listenerCheckBox != null)
                checkBoxSwitch.setOnCheckedChangeListener(listenerCheckBox);
            if (imgSig != null && imageClickListener != null)
                imgSig.setOnClickListener(imageClickListener);
            if (eValue != null) {
//                eValue.setOnFocusChangeListener(onFocusChangeListener);
                // we could add textWatcher to eValue here, but that takes place also in setItem to prevent
                // being called when eValue.setText() is called, and we don't want to risk adding more than one.
            }
//            displayType = "edittext";
        }

        public void setItem(int position, ListItemCodingDataGroup item) {
            String strThis = "ItemViewHolder.setItem(), ";
            this.item = item;

//            dataTypeName = item.getDataTypeName();

//            Log.d(TAG, strThis + "Start. item=" + item);

            if (vLabel != null) {
//                Log.d(TAG, "setItem: vLabel: " + item.getLabel() + " combinedValue: " + item.getCombinedValue());
                vLabel.setText(item.getLabel());
            }

//            Log.d(TAG, "setItem: ");
            if (eValue != null) {
//                eValue.removeTextChangedListener(textWatcher);
//                Log.d(TAG, "setItem: eValue: " + item.getLabel() + " combinedValue: " + item.getCombinedValue());
                String combinedValue = item.getCombinedValue();
                eValue.setText(combinedValue);
//                Log.d(TAG, strThis + "After setting EditText, combinedValue=" + combinedValue + ", eValue.getText().toString()=" + eValue.getText().toString() + ", item=" + item);
//                textWatcher.setItem(item);
//                eValue.addTextChangedListener(textWatcher);
//                eValue.setTag(new Integer(position));
                if (item.getCombineType() == Cadp.COMBINE_TYPE_NONE)
                    eValue.setTag(item.getListCodingdataRows().get(0));
                else
                    eValue.setTag(item);

                Log.d(TAG, "setItem: crash search: 1st if");
                if (item.getEditMode() != Cadp.EDIT_MODE_READONLY && item.getEditMode() != Cadp.EDIT_MODE_LOOKUP) {
                    Log.d(TAG, "setItem: crash search: inside 1st if");
//                    Log.d(TAG, strThis + "Case: Not read-only or lookup, editable EditText.  Setting onFocusChangeListener and optional hint.");
//                    if (eValue.isFocusable()) {
//                        eValue.setOnFocusChangeListener(onFocusChangeListener);
//                    }


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
//                    Log.d(TAG, strThis + "Case: read-only or lookup, item.getEditMode()=" + item.getEditMode());
                    eValue.setInputType(InputType.TYPE_NULL); // We'll see if this makes it readonly.
                }

                Log.d(TAG, "setItem: crash search: 2nd if");
                eValue.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        textWatcherListener.afterTextChanged(eValue, s.toString(), position);
                    }
                });
                Log.d(TAG, "setItem: crash search: after 2nd if");
            }

            Log.d(TAG, "setItem: crash search: 3rd if");
            if (checkBoxSwitch != null) {
                Log.d(TAG, "setItem: crash search: inside 3rd if");
                checkBoxSwitch.setOnCheckedChangeListener(null);

                // **** Restoring original logic -- the revised logic always set item to true.
//                if (item.getCombinedValue()=="1"){
                checkBoxSwitch.setChecked(Cadp.SQLITE_VAL_TRUE.equals(item.getCombinedValue()));
//                }else
//                checkBoxSwitch.setChecked(true);

                checkBoxSwitch.setOnCheckedChangeListener(this.listenerCheckBox);
//                checkBoxSwitch.setTag(new Integer(position));
                checkBoxSwitch.setTag(item.getListCodingdataRows().get(0));
            }
            Log.d(TAG, "setItem: crash search: after 3rd if");

            if (vIsRequired != null) {
                if (item.isRequired()) vIsRequired.setVisibility(View.VISIBLE);
                else vIsRequired.setVisibility(View.INVISIBLE);
            }
//            if (imgSig != null) imgSig.setTag(new Integer(position));
            if (imgSig != null) {
//                Log.d(TAG, "setItem: position: " + position);
                imgSig.setTag(position);
                if (item.getArBitmapItems() != null) {
                    for (AdapterUtils.BitmapItem bitmapItem : item.getArBitmapItems()) {
                        // For now don't set null bitmap, allow default image.
                        if (bitmapItem != null && bitmapItem.bitmapClass == Cadp.BITMAP_CLASS_SIGNATURE) {
                            imgSig.setImageBitmap(bitmapItem.bitmap);
                            imgSig.setScaleType(ImageView.ScaleType.FIT_CENTER);
//                        imgSig.setScaleX(1.f/16.f);
//                        imgSig.setScaleY(1.f/16.f);
                            break;
                        }
                    }
                }
            }

            if (imgSig != null) {
                imgSig.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String signatureName = eValue.getText().toString();
                        Log.d(TAG, "onClick: signatureName: " + signatureName);
                        if (signatureName.isEmpty()) {
                            eValue.setError("Please enter signature name");
                        } else {
                            Log.d(TAG, "onClick: ");
                            signatureSelectionListener.drawSignature(signatureName, item.getLabel(), imgSig);
                        }
                    }
                });
            }

//            if (eValue!=null) {
//                eValue.set
//            }

//            Log.d(TAG, "setItem: label: "+item.getLabel());
            if (item.getLabel().contains("Odometer")) {
                eValue.setHint("Enter Odometer");
                eValue.setInputType(InputType.TYPE_CLASS_NUMBER);
            }

            if (item.getLabel().contains("TRUCK/TRACTOR")) {
                eValue.setHint("Enter TRUCK/TRACTOR Number");
            }

            if (item.getLabel().equalsIgnoreCase("TRAILER 1")) {
                eValue.setHint("Enter TRAILER 1 Number");
            }

            if (item.getLabel().equalsIgnoreCase("TRAILER 2")) {
                eValue.setHint("Enter TRAILER 2 Number");
                vIsRequired.setVisibility(View.INVISIBLE);
            }

            if (item.getLabel().contains("Reefer HOS")) {
                eValue.setHint("Enter Reefer HOS.");
                eValue.setInputType(InputType.TYPE_CLASS_NUMBER);

//                July 07, 2022 -   We set isRequired * keyword to always invisible and if required we will show
//                the toast messages
                vIsRequired.setVisibility(View.INVISIBLE);
//                if (position == 38) {
//                    vIsRequired.setVisibility(View.VISIBLE);
//                } else {
//                    vIsRequired.setVisibility(View.GONE);
//                }
            }

            Log.d(TAG, "setItem: crash search: nth if");
            if (item.getLabel().contains("Remarks")) {
                eValue.setHint("Enter Remarks");
            }

            if (item.getLabel().contains("Mechanic Name and Signature")) {
                eValue.setHint("Enter Mechanic Name");
            }

//            if (eValue != null) {
//                eValue.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        if (item.getLabel().equalsIgnoreCase("DateTime")) {
//                            Log.d(TAG, "onClick: editDetail");
//
//                            Calendar calendar = Calendar.getInstance();
//                            final int selectedYear = calendar.get(Calendar.YEAR);
//                            final int selectedMonth = calendar.get(Calendar.MONTH);
//                            final int selectedDayOfMonth = calendar.get(Calendar.DAY_OF_WEEK);
//
//                            DatePickerDialog.OnDateSetListener dateSetListener = new DatePickerDialog.OnDateSetListener() {
//
//                                @Override
//                                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
//                                    selectedDate = String.format("%02d/%02d/%02d", (monthOfYear + 1), dayOfMonth, year);
//
//                                    Calendar mcurrentTime = Calendar.getInstance();
//                                    int hour = mcurrentTime.get(Calendar.HOUR_OF_DAY);
//                                    int minute = mcurrentTime.get(Calendar.MINUTE);
//                                    TimePickerDialog mTimePicker;
//                                    mTimePicker = new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
//                                        @Override
//                                        public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
//                                            Log.d(TAG, "onTimeSet: view: " + view.toString() + " hourOfDay: " + hourOfDay + " minute: " + minute);
//
//                                            int tempHour;
//                                            if (hourOfDay >= 12) {
//                                                tempHour = (hourOfDay % 12);
//                                                mTimeZone = "PM";
//                                            } else {
//                                                tempHour = hourOfDay;
//                                                mTimeZone = "AM";
//                                            }
//
//                                            if (tempHour < 10) {
//                                                mHour = "0" + tempHour;
//                                            } else {
//                                                mHour = "" + tempHour;
//                                            }
//
//                                            if (minute < 10) {
//                                                mMinutes = "0" + minute;
//                                            } else {
//                                                mMinutes = "" + minute;
//                                            }
//
//                                            selectedDate = selectedDate + " " + (mHour + ":" + mMinutes + " " + mTimeZone);
//                                            Log.d(TAG, "onTimeSet: selectedDate: " + selectedDate);
//                                            eValue.setText(selectedDate);
//                                        }
//                                    }, hour, minute, false);//Yes 24 hour time
//                                    mTimePicker.setTitle("Select Time");
//                                    mTimePicker.show();
//
//                                    eValue.setText(selectedDate);
//                                }
//                            };
//                            DatePickerDialog datePickerDialog = new DatePickerDialog(itemView.getContext(),
//                                    dateSetListener, selectedYear, selectedMonth, selectedDayOfMonth);
//                            datePickerDialog.show();
//                        }
//                    }
//                });
//            }

            Log.d(TAG, "setItem: crash search: nth+1 if");
            setViewType(item.getViewType());
        }

    }

    public static class ItemTextWatcher implements TextWatcher {
        private BusHelperRmsCoding.CodingDataRow item;

        public void setItem(BusHelperRmsCoding.CodingDataRow item) {
            this.item = item;
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            item.updateValueFromDisplay(s.toString());
            Log.d(TAG, "fieldCheck: onTextChanged: item: string: " + s.toString());
        }

        @Override
        public void afterTextChanged(Editable s) {
            Log.d(TAG, "fieldCheck: afterTextChanged: s: " + s);
            item.updateValueFromDisplay(s.toString());
        }
    }

    // endregion Nested Classes / Interfaces

    public interface SignatureSelectionListener {
        void drawSignature(String signatureName, String label, View view);
    }

}