package com.rco.rcotrucks.activities.fuelreceipts.adapter;

import android.app.Activity;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.utils.BusHelperFuelReceipts;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.adapters.Cadp;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.PairList;
import com.rco.rcotrucks.businesslogic.rms.BusHelperRmsCoding;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class FuelReceiptListAdapter extends RecyclerView.Adapter<FuelReceiptListAdapter.ListItemViewHolder> {
    public static final String TAG = "FuelReceiptListAdapter";

    public Activity ctx;
    private List<ListItemFuelReceipt> mDataset;
    ArrayList<ListItemFuelReceipt> filterArrayList = new ArrayList<>();

    public List<ListItemFuelReceipt> getmDataset() {
        return mDataset;
    }

    View.OnClickListener listener;


    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ListItemViewHolder extends RecyclerView.ViewHolder {
        Activity activityContext;
        public View view;
        TextView vDate;
        TextView vFuelCodeType;
        TextView vVendorCountry;
        TextView vTotAmountTotGal;
        TextView vIcon;
        private BusHelperFuelReceipts busRules = BusHelperFuelReceipts.instance();


        public ListItemViewHolder(View v, View.OnClickListener listener, Activity activityContext) {
            super(v); // This is critical for RecyclerView to get the view later.
            view = v;
            this.activityContext=activityContext;
            if (listener != null) v.setOnClickListener(listener);
            vDate = v.findViewById(R.id.textViewLbl);
            vFuelCodeType = v.findViewById(R.id.textViewFldOne);
//            vVendorCountry = v.findViewById(R.id.textViewFldThree);
            vTotAmountTotGal = v.findViewById(R.id.textViewFldTwo);
            vIcon = v.findViewById(R.id.textViewIcon);
        }

        public void setItem(ListItemFuelReceipt listitem, int position) {
            String str = "Date: " + StringUtils.nvl(listitem.getDate(), "");
            str = str.replace("00:00:00", "").trim();
            vDate.setText(str);
//            vFuelCodeType.setText(listitem.getFuelCode() + " " + listitem.getFuelType()
//            July 15, 2022 -   Roy told to remove it
            vFuelCodeType.setText(listitem.getFuelType()
                    + ", " + listitem.getVendorName()
                    + ", " + listitem.getVendorState());
//            vVendorCountry.setText(listitem.getVendorCountry());
            vTotAmountTotGal.setText("$ " + listitem.getTotalAmountInUSD() + " USD  (" + listitem.getNumberOfGallons() + " GAL)");

            if (!listitem.isValid) {
//                view.setBackgroundColor(Color.rgb(255, 225, 225));
                vIcon.setTextColor(Color.RED);
            } else if (listitem.syncStatus == Cadp.SYNC_STATUS_PENDING_UPDATE) {
//                view.setBackgroundColor(Color.rgb(245, 255, 245));
                vIcon.setTextColor(Color.GREEN);
            } else if (listitem.syncStatus == Cadp.SYNC_STATUS_MARKED_FOR_DELETE) {
//                view.setBackgroundColor(Color.rgb(245, 255, 245));
                vIcon.setTextColor(Color.YELLOW);
            } else {
//                view.setBackgroundColor(Color.TRANSPARENT);
                vIcon.setTextColor(activityContext.getColor(R.color.black_and_white));
            }
//            view.setTag(new Rms.RmsObjectIdType(listitem.getObjectId(), listitem.getObjectType(), listitem.getRecordId()
//            ));
            view.setTag(listitem);
            getRecordIdentsIds(view.getTag());
        }
    }

    static void getRecordIdentsIds(Object message) {
//        Object message = view.getTag();
//        Long idRmsRecords = -1L;
        FuelReceiptListAdapter.ListItemFuelReceipt item = null;

        if (message != null && message instanceof FuelReceiptListAdapter.ListItemFuelReceipt) {
            item = (FuelReceiptListAdapter.ListItemFuelReceipt) message;
        }
        Log.d(TAG, "getRecordIdentsIds: item: " + item);
//        else
//            Log.d(TAG, "onClick() **** Unexpected view tag of type: "
//                    + (message != null ? message.getClass().getCanonicalName() : "(NULL)"));


        BusHelperRmsCoding.RmsRecords recordIdent = null;

        if (item != null)
            recordIdent
                    = new BusHelperRmsCoding.RmsRecords(item.getIdRmsRecords(), item.getObjectId(), item.getObjectType(), null, -1);
        else recordIdent = new BusHelperRmsCoding.RmsRecords(-1L, null, null, null, -1);

        Log.d(TAG, "getRecordIdentsIds: recordIdent: " + recordIdent);

    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public FuelReceiptListAdapter(Activity context, List<ListItemFuelReceipt> myDataset,
                                  View.OnClickListener listener) {
        ctx = context;
        mDataset = myDataset;
        this.listener = listener;

        if (mDataset != null)
            filterArrayList.addAll(mDataset);
    }

    // Create new views (invoked by the layout manager)
    @Override
    public FuelReceiptListAdapter.ListItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                                        int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_hdr_lbl_lbl_lbl_2x2, parent, false);
        ListItemViewHolder vh = new ListItemViewHolder(v, listener, ctx);

        // Todo: set onclick listeners;

        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        ListItemFuelReceipt item = mDataset.get(position);
        holder.setItem(item, position);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static String getItemListSql(long idRecordTypeFuelReceiptDetail, List<String> listParams) {
        Map<String, String> mapCoding = BusinessRules.getMapCodingMasterIdByName();
        String cmidFuelCode = mapCoding.get(Crms.FUEL_CODE);
        String cmidFuelType = mapCoding.get(Crms.FUEL_TYPE);
        String cmidVendCountry = mapCoding.get(Crms.VENDOR_COUNTRY);
        String cmidDateTime = mapCoding.get(Crms.DATETIME);
        String cmidTotSaleUsd = mapCoding.get(Crms.TOTAL_AMOUNT_OF_SALE_IN_USD);
        String cmidNumGallons = mapCoding.get(Crms.NUMBER_OF_GALLONS_PURCHASED);
        String cmidVendorState = mapCoding.get(Crms.VENDOR_STATE);
        String cmidVendorName = mapCoding.get(Crms.VENDOR_NAME);
        String cmidUserRecordId = mapCoding.get(Crms.USER_RECORD_ID);
        String cmidTotalAmount = mapCoding.get(Crms.TOTAL_AMOUNT);

//        July 05, 2022 -   Adding vendor state and name as we wanted to show them on adapter item as well
        String strSql = "select cdd.IdRmsRecords, cdd.Value datetime, cdfc.Value fuelcode, cdft.value fueltype," +
                " cdco.value country, cdta.value totalamount, cdng.value numgals, cdvs.value vendorstate, cdvn.value vendorname, cdusr.value userrecordId, cda.value amount, rr.ObjectType, rr.ObjectId, rr.IsValid, rr.sent\n" +
                "from CodingData cdd\n" +
                "inner join rmsrecords rr on rr.id = cdd.IdRmsRecords AND rr.IdRecordType = ? \n" + // -- IdRecordTypeFuelReceiptDetail
                "left outer join CodingData cdfc on cdfc.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdfc.CodingMasterId = ? \n" + // -- Fuel Code
                "left outer join CodingData cdft on cdft.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdft.CodingMasterId = ? \n" + // -- Fuel Type
                "left outer join CodingData cdco on cdco.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdco.CodingMasterId = ? \n" + // -- Vendor Country
                "left outer join CodingData cdta on cdta.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdta.CodingMasterId = ? \n" + // -- Tot Amt of Sale
                "left outer join CodingData cdng on cdng.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdng.CodingMasterId = ? \n" + // -- Num Gals
                "left outer join CodingData cdvs on cdvs.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdvs.CodingMasterId = ? \n" + // -- Vendor State
                "left outer join CodingData cdvn on cdvn.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdvn.CodingMasterId = ? \n" + // -- Vendor Name
                "left outer join CodingData cdusr on cdusr.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdusr.CodingMasterId = ? \n" + // -- UserRecordId
                "left outer join CodingData cda on cda.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cda.CodingMasterId = ? \n" + // -- Amount
                "where cdd.CodingMasterId = ? \n" + // -- DateTime
                "order by datetime desc";

        Log.d(TAG, "getItemListSql: fuelReceipt: sql: " + strSql);

        listParams.add(String.valueOf(idRecordTypeFuelReceiptDetail));
        listParams.add(cmidFuelCode);
        listParams.add(cmidFuelType);
        listParams.add(cmidVendCountry);
        listParams.add(cmidTotSaleUsd);
        listParams.add(cmidNumGallons);
        listParams.add(cmidVendorState);
        listParams.add(cmidVendorName);
        listParams.add(cmidUserRecordId);
        listParams.add(cmidTotalAmount);
        listParams.add(cmidDateTime);

        return strSql;
    }


    public static String getItemListSqlInDateRange(long idRecordTypeFuelReceiptDetail, List<String> listParams, String startDate, String endDate) {
        Map<String, String> mapCoding = BusinessRules.getMapCodingMasterIdByName();
        String cmidFuelCode = mapCoding.get(Crms.FUEL_CODE);
        String cmidFuelType = mapCoding.get(Crms.FUEL_TYPE);
        String cmidVendCountry = mapCoding.get(Crms.VENDOR_COUNTRY);
        String cmidDateTime = mapCoding.get(Crms.DATETIME);
        String cmidTotSaleUsd = mapCoding.get(Crms.TOTAL_AMOUNT_OF_SALE_IN_USD);
        String cmidNumGallons = mapCoding.get(Crms.NUMBER_OF_GALLONS_PURCHASED);
        String cmidVendorState = mapCoding.get(Crms.VENDOR_STATE);
        String cmidVendorName = mapCoding.get(Crms.VENDOR_NAME);
        String cmidUserRecordId = mapCoding.get(Crms.USER_RECORD_ID);
        String cmidTotalAmount = mapCoding.get(Crms.TOTAL_AMOUNT);


//        July 05, 2022 -   Adding vendor state and name as we wanted to show them on adapter item as well
        String strSql = "select cdd.IdRmsRecords, cdd.Value datetime, cdfc.Value fuelcode, cdft.value fueltype," +
                " cdco.value country, cdta.value totalamount, cdng.value numgals, cdvs.value vendorstate, cdvn.value vendorname, cdusr.value userrecordId, cda.value amount, rr.ObjectType, rr.ObjectId, rr.IsValid, rr.sent\n" +
                "from CodingData cdd\n" +
                "inner join rmsrecords rr on rr.id = cdd.IdRmsRecords AND rr.IdRecordType = ? \n" + // -- IdRecordTypeFuelReceiptDetail
                "left outer join CodingData cdfc on cdfc.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdfc.CodingMasterId = ? \n" + // -- Fuel Code
                "left outer join CodingData cdft on cdft.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdft.CodingMasterId = ? \n" + // -- Fuel Type
                "left outer join CodingData cdco on cdco.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdco.CodingMasterId = ? \n" + // -- Vendor Country
                "left outer join CodingData cdta on cdta.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdta.CodingMasterId = ? \n" + // -- Tot Amt of Sale
                "left outer join CodingData cdng on cdng.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdng.CodingMasterId = ? \n" + // -- Num Gals
                "left outer join CodingData cdvs on cdvs.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdvs.CodingMasterId = ? \n" + // -- Vendor State
                "left outer join CodingData cdvn on cdvn.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdvn.CodingMasterId = ? \n" + // -- Vendor Name
                "left outer join CodingData cdusr on cdusr.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cdusr.CodingMasterId = ? \n" + // -- UserRecordId
                "left outer join CodingData cda on cda.IdRmsRecords = cdd.IdRmsRecords \n" +
                "    and cda.CodingMasterId = ? \n" + // -- Amount
                "where cdd.CodingMasterId = ? \n" + // -- DateTime
                "and datetime >= ? " + // -- DateTime
                "and datetime <= ? " + // -- DateTime
                "order by datetime desc";

        Log.d(TAG, "getItemListSql: fuelReceipt: sql: " + strSql);

        listParams.add(String.valueOf(idRecordTypeFuelReceiptDetail));
        listParams.add(cmidFuelCode);
        listParams.add(cmidFuelType);
        listParams.add(cmidVendCountry);
        listParams.add(cmidTotSaleUsd);
        listParams.add(cmidNumGallons);
        listParams.add(cmidVendorState);
        listParams.add(cmidVendorName);
        listParams.add(cmidDateTime);
        listParams.add(startDate);
        listParams.add(cmidUserRecordId);
        listParams.add(cmidTotalAmount);
        listParams.add(endDate);

        return strSql;
    }

    public static class ListItemFuelReceipt implements AdapterUtils.IAdapterItem<DateUtils.IDateConverter, ListItemFuelReceipt> {
        private String date;

        private String vendorState;
        private String vendorName;
        private String fuelCode;
        private String fuelType;
        private String vendorCountry;
        private String totalAmountInUSD;
        private String totalAmount;
        private String userRecordId;
        private String numberOfGallons;
        private String sortKey;
        private String objectType;
        private String objectId;
        private String recordId;
        private boolean isValid;
        //        private boolean isSent;
        private int syncStatus;
        private Long idRmsRecords;
        private String[] arMatchText = new String[5];

        public String getDate() {
            return date;
        }

        public String getFuelCode() {
            return fuelCode;
        }

        public String getFuelType() {
            return fuelType;
        }

        public String getVendorCountry() {
            return vendorCountry;
        }

        public String getTotalAmountInUSD() {
            return totalAmountInUSD;
        }

        public void setTotalAmountInUSD(String totalAmountInUSD) {
            this.totalAmountInUSD = totalAmountInUSD;
        }

        public String getTotalAmount() {
            return totalAmount;
        }

        public void setTotalAmount(String totalAmount) {
            this.totalAmount = totalAmount;
        }

        public String getUserRecordId() {
            return userRecordId;
        }

        public void setUserRecordId(String userRecordId) {
            this.userRecordId = userRecordId;
        }

        public String getNumberOfGallons() {
            return numberOfGallons;
        }

        /*
        "select cdd.IdRmsRecords, cdd.Value datetime, cdfc.Value fuelcode, cdft.value fueltype," +
        " cdco.value country, cdta.value totalamount, cdng.value numgals, rr.ObjectType, rr.ObjectId, rr.IsValid, rr.sent\n" +
         */
        public void init(Cursor cursor, DateUtils.IDateConverter dateConverter) {
            Log.d(TAG, "ListItemFuelReceipt.init(cur) initializing a list item.");
            Log.d(TAG, "init: cursor: count: " + cursor.getCount());

//            String strSql = "select
//            cdd.IdRmsRecords, cdd.Value datetime, cdfc.Value fuelcode, cdft.value fueltype," +
//                    " cdco.value country, cdta.value totalamount, cdng.value numgals,
//                    cdvs.value vendorstate, rr.ObjectType, rr.ObjectId, rr.IsValid, rr.sent\n" +

            idRmsRecords = cursor.getLong(0);
            date = cursor.getString(1); //pairlistRecordData.getValue("Date");
            fuelCode = cursor.getString(2); // pairlistRecordData.getValue("Truck Number");
            fuelType = cursor.getString(3);
            vendorCountry = cursor.getString(4);
            totalAmountInUSD = cursor.getString(5);
            numberOfGallons = cursor.getString(6);
            vendorState = cursor.getString(7);
            vendorName = cursor.getString(8);
            Log.d(TAG, "init: vendorState: " + vendorState);
            objectType = cursor.getString(9);
            objectId = cursor.getString(10);
            isValid = cursor.getLong(11) == 1 ? true : false;
//            isSent = cursor.getLong(10) == 1 ? true : false;
            syncStatus = cursor.getInt(12);
//            recordId = pairlistRecordData.getValue("RecordId");
            Log.d(TAG, "init: idRmsRecords: " + idRmsRecords +
                    " date: " + date +
                    " fuelCode: " + fuelCode +
                    " fuelType: " + fuelType +
                    " vendorCountry: " + vendorCountry +
                    " totalAmount: " + totalAmountInUSD +
                    " numberOfGallons: " + numberOfGallons +
                    " vendorState: " + vendorState +
                    " vendorName: " + vendorName +
                    " objectType: " + objectType +
                    " objectId: " + objectId +
                    " isValid: " + isValid +
                    " syncStatus: " + syncStatus
            );

            if (!StringUtils.isNullOrWhitespaces(date)) {
                sortKey = date;
                date = dateConverter.convert(date, true);
            }

            initMatchText();
        }

        @Override
        public void init(PairList pairlistRecordData, DateUtils.IDateConverter dateConverter) {
            date = pairlistRecordData.getValue("Date");

            if (!StringUtils.isNullOrWhitespaces(date)) {
                sortKey = date;
                date = dateConverter.convert(date, true);
            }

            fuelCode = pairlistRecordData.getValue(Crms.FUEL_CODE);
            fuelType = pairlistRecordData.getValue(Crms.FUEL_TYPE);
            vendorCountry = pairlistRecordData.getValue(Crms.VENDOR_COUNTRY);
            totalAmountInUSD = pairlistRecordData.getValue(Crms.TOTAL_AMOUNT_OF_SALE_IN_USD);
            numberOfGallons = pairlistRecordData.getValue(Crms.NUMBER_OF_GALLONS_PURCHASED);

            objectId = pairlistRecordData.getValue("LobjectId");
            objectType = pairlistRecordData.getValue("objectType");
            recordId = pairlistRecordData.getValue("RecordId");
            isValid = "1".equals(pairlistRecordData.getValue("IsValid")) ? true : false;
//            isSent = "1".equals(pairlistRecordData.getValue("sent")) ? true : false;
            syncStatus = Integer.parseInt(pairlistRecordData.getValue("sent"));

            initMatchText();
        }

        private void initMatchText() {
            int ix = 0;
            arMatchText[ix++] = fuelCode;
            arMatchText[ix++] = fuelType;
            arMatchText[ix++] = vendorCountry;
            arMatchText[ix++] = totalAmountInUSD;
            arMatchText[ix++] = numberOfGallons;
        }

        @Override
        public String getSortKey() {
            return sortKey;
        }

        @Override
        public boolean isMatch(String pattern) {
            boolean isMatch = AdapterUtils.isMatch(arMatchText, pattern, true);
            return isMatch;
        }

        @Override
        public String toString() {
//            return date + "," + truckTractorNumber + "," + trailerNumber;
            return StringUtils.memberValuesToString(this);
        }

        @Override
        public int compareTo(@NonNull ListItemFuelReceipt o) {
            return -getSortKey().compareTo(o.getSortKey()); // minus sign for sort descending.  Or could reverse objects.
        }

        public String getObjectType() {
            return objectType;
        }

        public String getObjectId() {
            return objectId;
        }

        public String getRecordId() {
            return recordId;
        }

        public Long getIdRmsRecords() {
            return idRmsRecords;
        }

        public String getVendorState() {
            return vendorState;
        }

        public String getVendorName() {
            return vendorName;
        }

        @Override
        public String getLabel() {
            return getDate();
        }
    }

    public void search(String title) {

        Log.d(TAG, "searchFilter: searched Value is " + title);
        title = title.toLowerCase(Locale.getDefault());
        mDataset.clear();
        if (title.length() == 0) {

            mDataset.addAll(filterArrayList);
        } else {

            for (ListItemFuelReceipt dataModel : filterArrayList) {
                if (dataModel.getVendorName().toLowerCase().contains(title)
                        || dataModel.getVendorState().toLowerCase().contains(title)) {

                    Log.d(TAG, "searchFilter: title " + title + " and adding to product list");
                    mDataset.add(dataModel);
                }
            }
        }

        Log.d(TAG, "searchFilter: ");
        notifyDataSetChanged();
    }

}