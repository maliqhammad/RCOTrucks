package com.rco.rcotrucks.adapters;

import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.EditText;

import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.util.List;

/**
 * For now, the rules are that multi-codingfield values, such as "lastname, Firstname",
 * must be read-only.  Otherwise, parsers would need to be invoked.
 */
public class ListItemCodingDataGroup implements AdapterUtils.IMatchable, AdapterUtils.ILabeled {
    private static final String TAG = "ListItemCodingDataGroup";

    private List<BusHelperRmsCoding.CodingDataRow> listCodingdataRows;
    private String labelCombined;

    private String valueCombined;
    private String dataTypeName;

    private int viewType;
    private long idCodingData;
    private int editMode;

    private AdapterUtils.BitmapItem[] arBitmaps;

    private ArrayAdapter spinnerAdapter;

    public ArrayAdapter getSpinnerAdapter() {
        return spinnerAdapter;
    }

    int ixSelectedSpinnerItem = 0;

    public int getIxSelectedSpinnerItem() {
        return ixSelectedSpinnerItem;
    }

    private long idRmsRecords;

    public long getIdRmsRecords() {
        return idRmsRecords;
    }

    private String objectIdRmsRecords;

    public String getObjectIdRmsRecords() {
        return objectIdRmsRecords;
    }

    private String objectTypeRmsRecords;

    public String getObjectTypeRmsRecords() {
        return objectTypeRmsRecords;
    }

    private int combineType;

    private boolean isRequired;

    public void init(long idRmsRecords, String objectIdRmsRecords, String objectTypeRmsRecords,
                     List<BusHelperRmsCoding.CodingDataRow> listCodingdataRows,
                     AdapterUtils.BitmapItem[] arBitmaps,
                     int combineType,
                     String combinedLabelOptional,
                     String combinedValueOptional,
                     String dataTypeNameOptional,
                     int viewType,
                     int editMode,
                     boolean isRequired,
                     ArrayAdapter spinnerAdapter) {

        Log.d(TAG, "init() called with: idRmsRecords = [" + idRmsRecords + "], objectIdRmsRecords = [" + objectIdRmsRecords + "], objectTypeRmsRecords = [" + objectTypeRmsRecords + "], listCodingdataRows = [" + listCodingdataRows + "], arBitmaps = [" + arBitmaps + "], combineType = [" + combineType + "], combinedLabelOptional = [" + combinedLabelOptional + "], combinedValueOptional = [" + combinedValueOptional + "], dataTypeNameOptional = [" + dataTypeNameOptional + "], viewType = [" + viewType + "], editMode = [" + editMode + "], isRequired = [" + isRequired + "], spinnerAdapter = [" + spinnerAdapter + "]");
        this.idRmsRecords = idRmsRecords;
        this.objectIdRmsRecords = objectIdRmsRecords;
        this.objectTypeRmsRecords = objectTypeRmsRecords;
        this.listCodingdataRows = listCodingdataRows;
        this.arBitmaps = arBitmaps;
        this.combineType = combineType;
        this.dataTypeName = dataTypeNameOptional;
        this.viewType = viewType;
        this.editMode = editMode;
        this.isRequired = isRequired;
        this.spinnerAdapter = spinnerAdapter;
        labelCombined = combinedLabelOptional;
        valueCombined = combinedValueOptional;

        String vCombined = getCombinedValue();

//        if (spinnerAdapter != null) {
        if (spinnerAdapter != null && !StringUtils.isNullOrWhitespaces(vCombined)) {
            Log.d(TAG, "init() Case: labelCombined=" + labelCombined + ", spinnerAdapter not null, checking selections for match to vCombined=" + vCombined
                    + ", spinnerAdapter.getCount()=" + spinnerAdapter.getCount() + ", combineType=" + combineType);

            for (int i = 0; i < spinnerAdapter.getCount(); i++) {
                String selection = spinnerAdapter.getItem(i).toString();

                if (combineType == Cadp.COMBINE_TYPE_CODE_OF_SELECTION) {
                    int ix = selection.indexOf(" ");
                    if (ix >= 0) selection = selection.substring(0, ix);
                }

                if (StringUtils.isEquiv(vCombined, selection, true)) {
                    ixSelectedSpinnerItem = i;
                    Log.d(TAG, "init(), i=" + i + ", labelCombined=" + labelCombined + ",Case: spinner selection matches vCombined,"
                            + " set ixSelectedSpinnerItem=" + ixSelectedSpinnerItem
                            + ", selection=" + selection
                            + ", spinnerAdapter.getCount()=" + spinnerAdapter.getCount() + ", vCombined=" + vCombined);
                    break;
                } else
                    Log.d(TAG, "init(), i=" + i + ", labelCombined=" + labelCombined + ", Case: spinner selection does not match vCombined"
                            + ", selection=" + selection
                            + ", vCombined=" + vCombined);
            }
        } else
            Log.d(TAG, "init() Case: labelCombined=" + labelCombined + ", spinnerAdapter is null or vCombined has no value. vCombined=" + vCombined);


    }

//        public String getSortKey() {
//            return sortKey;
//        }

    public boolean isMatch(String pattern) {
        boolean isMatch = StringUtils.isSearchMatchEquiv(getLabel(), pattern, true);

        if (!isMatch)
            isMatch = StringUtils.isSearchMatchEquiv(getCombinedValue(), pattern, true);

        if (!isMatch && combineType <= Cadp.COMBINE_TYPE_NONE && listCodingdataRows != null) {
            for (BusHelperRmsCoding.CodingDataRow codingDataRow : listCodingdataRows) {
                isMatch = StringUtils.isSearchMatchEquiv(codingDataRow.getDisplayName(), pattern, true);

                if (!isMatch)
                    isMatch = StringUtils.isSearchMatchEquiv(codingDataRow.getDisplayValue(), pattern, true);

                if (isMatch) break;
            }
        }

        return isMatch;
    }

    @Override
    public String toString() {
        return StringUtils.memberValuesToString(this);
    }

//        public int compareTo(@NonNull ListItemCodingDataRow_obs o) {
//            return -getSortKey().compareTo(o.getSortKey()); // minus sign for sort descending.  Or could reverse objects.
//        }

//        public String getObjectType() {
//            return objectType;
//        }
//
//        public String getObjectId() {
//            return objectId;
//        }

    public List<BusHelperRmsCoding.CodingDataRow> getListCodingdataRows() {
        return listCodingdataRows;
    }

    public AdapterUtils.BitmapItem[] getArBitmapItems() {
        return arBitmaps;
    }

    public String getLabel() {
        if (!StringUtils.isNullOrWhitespaces(labelCombined)) return labelCombined;
        else if (listCodingdataRows != null && listCodingdataRows.size() > 0)
            return listCodingdataRows.get(listCodingdataRows.size() - 1).getDisplayName();
        else return "";
    }

    /**
     * If combine type is first-last name or last-first name, convention is that codingfield[0] holds the first name
     * and codingfield[1] holds the last name.
     *
     * @return
     */
    public String getCombinedValue() {
        String value = null;

        if (!StringUtils.isNullOrWhitespaces(valueCombined)) value = valueCombined;
        else if (listCodingdataRows != null && listCodingdataRows.size() > 0) {
            if (combineType <= Cadp.COMBINE_TYPE_NONE)
                value = listCodingdataRows.get(0).getDisplayValue();
            else if (combineType == Cadp.COMBINE_TYPE_FIRST_LAST_NAME && listCodingdataRows.size() > 1)
                value = StringUtils.getCompoundName(listCodingdataRows.get(0).getDisplayValue(), " ", listCodingdataRows.get(1).getDisplayValue());
            else if (combineType == Cadp.COMBINE_TYPE_LAST_FIRST_NAME && listCodingdataRows.size() > 1)
                value = StringUtils.getCompoundName(listCodingdataRows.get(1).getDisplayValue(), ", ", listCodingdataRows.get(0).getDisplayValue());
            else value = listCodingdataRows.get(0).getDisplayValue();
        }

        return value;
    }

    public void updateCombinedValue(String valueCombined) {
        String strThis = "updateCombinedValue(), ";
        Log.d(TAG, strThis + "Start. valueCombined=" + valueCombined
                + ", listCodingdataRows.size()=" + (listCodingdataRows != null ? listCodingdataRows.size() : "(NULL)")
                + ", combineType=" + combineType);

        if (getEditMode() == Cadp.EDIT_MODE_READONLY)
            Log.d(TAG, "**** Warning - unexpected update of readonly item. valueCombined=" + valueCombined
                    + ", this item=" + this + ", " + StringUtils.dumpArray(Thread.currentThread().getStackTrace()));

        // Todo: this optimization of not updating if no change may not be safe, depends on whether
        // underlying CodingDataRows (if any) are in sync with the valueCombined member.  Okay to comment out this case. -RAN
        if (StringUtils.isEquiv(valueCombined, this.valueCombined, false)) {
            Log.d(TAG, strThis + "**** Case: not updating " + this.getClass().getSimpleName() + " item with label: "
                    + getLabel() + " because valueCombined is same as current valueCombined=" + valueCombined
                    + ", this item=" + this);

            return;
        }

        this.valueCombined = valueCombined;

        if (listCodingdataRows != null && listCodingdataRows.size() > 0) {
            Nix nix = getEditableFieldCountAndHighestIndex(listCodingdataRows);

            Log.d(TAG, strThis + "After loop, nix:" + nix + ", listCodingdataRows.size()=" + listCodingdataRows.size());

            if (combineType == Cadp.COMBINE_TYPE_FIRST_LAST_NAME || combineType == Cadp.COMBINE_TYPE_CODE_OF_SELECTION) {
                updateCodingFirstLastName(valueCombined, nix, listCodingdataRows);
            } else {
                updateCodingOther(valueCombined, nix, listCodingdataRows);
            }
        } else Log.d(TAG, "**** no codingdata rows to update.");
    }

    public static void updateCodingOther(String valueCombined, Nix nix, List<BusHelperRmsCoding.CodingDataRow> listCodingdataRows) {
        String strThis = "updateCodingOther(), ";
        if (nix.n > 1)
            Log.d(TAG, strThis + "**** nix:" + nix + ", Warning, unhandled case, multiple codingfields with no parser.  Need parser to update multiple codingdata rows. valueCombined=" + valueCombined
                    + ", codingdata row items: " + StringUtils.dumpArray(listCodingdataRows.toArray()));

        BusHelperRmsCoding.CodingDataRow itemCoding = listCodingdataRows.get(nix.ix);

        itemCoding.updateValueFromDisplay(valueCombined);

        Log.d(TAG, strThis + "**** Case: nix:" + nix + ", listCodingdataRows.size()=" + listCodingdataRows.size()
                + ", multiple editable fields, updating one of them. valueCombined=" + valueCombined
                + ", itemCoding=" + itemCoding);
    }

    public static void updateCodingFirstLastName(String valueCombined, Nix nix, List<BusHelperRmsCoding.CodingDataRow> listCodingdataRows) {
        String strThis = "updateCodingFirstLastName(), ";
        // This combine type assumes the first two codingfields in the listCodingDataRows are the firstname and lastname, in that order.
        // We will try a crude parse to backfill those codingfields from the Mechanic Signature input - everything up to the first blank
        // is the first name, the rest is last name.

        String firstName = null;
        String lastName = null;

        if (valueCombined != null && valueCombined.length() > 0) {
            int ixblank = valueCombined.indexOf(" ");

            if (ixblank > 0) {
                firstName = valueCombined.substring(0, ixblank).trim();
                if (ixblank != valueCombined.length() - 1)
                    lastName = valueCombined.substring(ixblank + 1, valueCombined.length()).trim();
            } else firstName = valueCombined;
        }

        Log.d(TAG, strThis + "**** Case: COMBINE_TYPE_FIRST_LAST_NAME, nix:" + nix
                + ", listCodingdataRows.size()=" + listCodingdataRows.size()
                + ", valueCombined=" + valueCombined + ", firstName=" + firstName + ", lastName=" + lastName);

        if (listCodingdataRows.size() > 0)
            listCodingdataRows.get(0).updateValueFromDisplay(firstName);
        if (listCodingdataRows.size() > 1)
            listCodingdataRows.get(1).updateValueFromDisplay(lastName);
    }

    public static Nix getEditableFieldCountAndHighestIndex(List<BusHelperRmsCoding.CodingDataRow> listCodingdataRows) {
        String strThis = "getEditableFieldCountAndHighestIndex(), ";

        Nix nix = new Nix();
        BusHelperRmsCoding.CodingDataRow item;

        for (int i = 0; i < listCodingdataRows.size(); i++) {
            item = listCodingdataRows.get(i);
            if (!item.isReadOnly()) {
                nix.n++;
                nix.ix = i;
                Log.d(TAG, strThis + "**** Case: nix:" + nix + ", listCodingdataRows.size()=" + listCodingdataRows.size()
                        + ", found an editable CodingDataRow item"
                        + ", item=" + item);
            } else
                Log.d(TAG, strThis + "**** Case: nix:" + nix + ", listCodingdataRows.size()=" + listCodingdataRows.size()
                        + ", found an readonly CodingDataRow item"
                        + ", item=" + item);

        }

        // ix should now contain the index of the last editable codingfield on the list. n is the number of editable fields.

        return nix;
    }


    public int getViewType() {
        return viewType;
    }

    public void setIdCodingData(long idCodingData) {
        this.idCodingData = idCodingData;
    }

    public long getIdCodingData() {
        return idCodingData;
    }

    public int getCombineType() {
        return combineType;
    }

    public int getEditMode() {
        return editMode;
    }

    public boolean isRequired() {
        return isRequired;
    }

    public String getDataTypeName() {
        if (!StringUtils.isNullOrWhitespaces(dataTypeName)) return dataTypeName;
        if (listCodingdataRows != null && listCodingdataRows.size() == 1)
            return listCodingdataRows.get(0).getDataTypeName();
        else return null;
    }
//        @Override
//        public boolean isMatch(ListItemFuelReceipt o1, String searchText) {
//            return o1.isMatch(searchText);
//        }

    // ---------------------------------------- Nested Classes ----------------------------------------------

    /**
     * This class assumes the buttonView corresponds to a codingfield and has a BusHelperRmsCoding.CodingDataRow
     * item in its tag.  It updates the item whenever the CompoundButton has its checkmark changed.
     */
    public static class OnCheckedChangeListener implements CompoundButton.OnCheckedChangeListener {
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            try {
                Log.d(TAG, "onCheckChanged() Start. isChecked=" + isChecked);
                BusHelperRmsCoding.CodingDataRow itemCoding = (BusHelperRmsCoding.CodingDataRow) buttonView.getTag();
                Log.d(TAG, "onCheckChanged() itemCoding=" + itemCoding);
                UiUtils.closeKeyboard(buttonView);

                if (itemCoding != null) {
                    itemCoding.updateValueFromDisplay(isChecked ? "1" : "");
                }

                Log.d(TAG, "onCheckChanged() End. isChecked=" + isChecked);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    // hopefully reentrant listeners for general use.
    public static CompoundButton.OnCheckedChangeListener onCheckedChangeListener = new ListItemCodingDataGroup.OnCheckedChangeListener();
    public static View.OnFocusChangeListener onFocusChangeListener = new ListItemCodingDataGroup.OnFocusChangeListener();

    public static class OnFocusChangeListener implements View.OnFocusChangeListener {
        /**
         * Simple focus change listener, appropriate for EditText fields that map directly to a
         * single codingfield or a combined field.  Any field combining codingfields into a single EditText field
         * would require a listener with an appropriate parser and have a ListItemCodingDataGroup
         * object in the View's tag.
         *
         * @param v
         * @param hasFocus
         */
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
//        Integer intIxListItems = (Integer) v.getTag();
            try {
                Log.d(TAG, "onFocusChange() Start. hasFocus=" + hasFocus // + ", intIxListItems=" + intIxListItems
                        + ", v.getClass().getName()=" + v.getClass().getName() + ", v.getId()=" + v.getId());
                String label = null;
                Object objTag = v.getTag();
                if (objTag != null && objTag instanceof AdapterUtils.ILabeled)
                    label = ((AdapterUtils.ILabeled) objTag).getLabel();

                if (!(v instanceof EditText)) {
                    Log.d(TAG, "onFocusChange() not EditText case: hasFocus=" + hasFocus + ", label=" + label);

                    if (hasFocus) {
                        Log.d(TAG, "onFocusChange() EditText has focus case: hasFocus=" + hasFocus // + ", intIxListItems=" + intIxListItems
                        );
                        UiUtils.closeKeyboard(v);
                    }
                } else {
                    String val = null;

                    if (v instanceof EditText)
                        val = ((EditText) v).getText().toString();
                    else
                        Log.d(TAG, "onFocusChange() ***** Assertion Error.  Expecting view of EditText type, but found: "
                                + v.getClass().getSimpleName());

                    if (!hasFocus) {
                        // When an EditText loses focus, we want to store the value.
                        Log.d(TAG, "onFocusChange() EditText case: hasFocus=" + hasFocus + ", label=" + label + ", val=" + val);

                        if (objTag instanceof BusHelperRmsCoding.CodingDataRow) {

                            BusHelperRmsCoding.CodingDataRow itemCoding = (BusHelperRmsCoding.CodingDataRow) objTag;

                            Log.d(TAG, "onFocusChange() case: EditText, objTag is CodingDataRow, val=" + val + ", itemCoding=" + itemCoding);

                            if (!StringUtils.isEquiv(val, itemCoding.getDisplayValue(), false)) // check for equiv not really necessary -- easier on GC? -RAN
                            {
                                Log.d(TAG, "onFocusChange() case: EditText, objTag is CodingDataRow,"
                                        + " val is different from itemCoding.getDisplayValue(), val="
                                        + val + ", itemCoding.getDisplayValue()g="
                                        + itemCoding.getDisplayValue() + ", updating itemCoding.");

                                itemCoding.updateValueFromDisplay(val);
                            } else
                                Log.d(TAG, "onFocusChange() case: EditText, objTag is CodingDataRow, val"
                                        + " is same as itemCoding.getDisplayValue(), val=" + val
                                        + ", itemCoding.getDisplayValue()=" + itemCoding.getDisplayValue()
                                        + ", NOT updating itemCoding.");

                        } else if (objTag instanceof ListItemCodingDataGroup) {
                            ListItemCodingDataGroup item = (ListItemCodingDataGroup) objTag;
                            label = item.getLabel();

                            String itemVal = item.getCombinedValue();

                            Log.d(TAG, "onFocusChange() case: EditText, objTag is ListItemCodingDataGroup,"
                                    + " updating combined value, val=" + val + ", itemVal=" + itemVal
                                    + ", item=" + item);

                            if (!StringUtils.isEquiv(val, itemVal, false)) {
                                Log.d(TAG, "onFocusChange() case: EditText, objTag is ListItemCodingDataGroup, label=" + label
                                        + ", new val is different from old, updating.");
                                item.updateCombinedValue(val);
                            } else
                                Log.d(TAG, "onFocusChange() case: EditText, objTag is "
                                        + "ListItemCodingDataGroup, new val is same as old, updating, label=" + label + ".");
                        }
                    } else
                        Log.d(TAG, "onFocusChange() EditText case: field is gaining focus, hasFocus=" + hasFocus
                                + ", label=" + label + ", val=" + val + ", no need to update anything.");
                }

                Log.d(TAG, "onFocusChange() End. hasFocus=" + hasFocus + ", v.getClass().getName()=" + v.getClass().getName() + ", " + v.getId() + ", label=" + label
                );
            } catch (Throwable e) {
                e.printStackTrace();
            } finally {

            }
        }

    }

    public static class Nix {
        public int n = 0;
        public int ix = 0;

        public String toString() {
            return "n=" + n + ", ix=" + ix;
        }
    }
}
