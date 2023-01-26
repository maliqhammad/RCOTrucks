package com.rco.rcotrucks.activities.dvir;

import android.app.Activity;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.adapters.AdapterUtils;
import com.rco.rcotrucks.businesslogic.PairList;
import com.rco.rcotrucks.businesslogic.rms.Crms;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DvirListAdapter extends RecyclerView.Adapter<DvirListAdapter.ListItemViewHolder> {
    public static final String TAG = "DvirListAdapter";
    private Activity ctx;
    private List<ListItemDvir> mDataset;
    ArrayList<ListItemDvir> filterArrayList = new ArrayList<>();
    PretripInterface pretripInterface;

    public List<ListItemDvir> getmDataset() {
        return mDataset;
    }

    View.OnClickListener listener;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ListItemViewHolder extends RecyclerView.ViewHolder {
        public View view;
        TextView vDate, vTruckTractorNumber, vTrailerNumber;
        ImageView vIcon;
        ConstraintLayout pretripItem;


        public ListItemViewHolder(View v, View.OnClickListener listener) {
            super(v); // This is critical for RecyclerView to get the view later.
            view = v;
            if (listener != null) v.setOnClickListener(listener);
            vDate = v.findViewById(R.id.textview_date);
            vTruckTractorNumber = v.findViewById(R.id.textview_truck_tractor_number);
            vTrailerNumber = v.findViewById(R.id.textview_trailer_number);
            vIcon = v.findViewById(R.id.textViewIcon);
            pretripItem = v.findViewById(R.id.item_pretrip);
        }

        public void setItem(ListItemDvir dviritem) {
//            UiHelperDvirList.ListItemFuelReceipt item = (UiHelperDvirList.ListItemFuelReceipt) dviritem;
            Log.d(TAG, "setItem: recordId: " + dviritem.recordId + " idRmsRecords: " + dviritem.idRmsRecords);
            Log.d(TAG, "setItem: date: " + dviritem.getDate());

            String date = UiUtils.getDateAfterSplittingDateAndTimeBySpace(dviritem.getDate());
            String str = "Date: " + StringUtils.nvl(date, "");
            Log.d(TAG, "setItem: str: " + str);
            vDate.setText(str);

            str = "Truck Number: " + StringUtils.nvl(dviritem.getTruckTractorNumber(), "");
            vTruckTractorNumber.setText(str);
            str = "Trailer Number: " + StringUtils.nvl(dviritem.getTrailerNumber(), "");
            vTrailerNumber.setText(str);
            if (!dviritem.isValid) {
                ImageViewCompat.setImageTintList(vIcon, ColorStateList.valueOf(Color.RED));
            } else if (!dviritem.isSent) {
                ImageViewCompat.setImageTintList(vIcon, ColorStateList.valueOf(Color.GREEN));
            } else {
                ImageViewCompat.setImageTintList(vIcon, ColorStateList.valueOf(Color.BLACK));
            }

            view.setTag(dviritem);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public DvirListAdapter(Activity context, List<ListItemDvir> myDataset,
                           View.OnClickListener listener, PretripInterface pretripInterface) {
        ctx = context;
        mDataset = myDataset;
        this.listener = listener;
        this.pretripInterface = pretripInterface;

        if (mDataset != null)
            filterArrayList.addAll(mDataset);
    }


    /**
     * Create new views (invoked by the layout manager)
     *
     * @param parent
     * @param viewType - ignored because only one view type for list adapter, defaults to 0.
     * @return
     */
    @Override
    public DvirListAdapter.ListItemViewHolder onCreateViewHolder(ViewGroup parent,
                                                                 int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.listitem_dvir, parent, false);
        ListItemViewHolder vh = new ListItemViewHolder(v, listener);

        // Todo: set onclick listeners;

        return vh;
    }


    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        ListItemDvir item = mDataset.get(position);
        holder.setItem(item);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    public static String getDvirListSql(long idRecordTypeDvirDetail) {
        String strSql = "select cdd.IdRmsRecords, cdd.Value datetime, cdtk.Value trucknum, cdt1.value trailernum1," +
                " cdt2.value trailernum2, rr.ObjectType, rr.ObjectId, rr.IsValid, rr.sent\n" +
                "from CodingData cdd\n" +
                "inner join rmsrecords rr on rr.id = cdd.IdRmsRecords AND rr.IdRecordType = " + idRecordTypeDvirDetail + " \n" +
                "left outer join CodingData cdtk on cdtk.IdRmsRecords = cdd.IdRmsRecords\n" +
                "    and cdtk.CodingMasterId = (select CodingMasterId from codingmasterlookup where CodingFieldName = '" + Crms.TRUCK_NUMBER + "')\n" +
                "left outer join CodingData cdt1 on cdt1.IdRmsRecords = cdd.IdRmsRecords\n" +
                "    and cdt1.CodingMasterId = (select CodingMasterId from codingmasterlookup where CodingFieldName = '" + Crms.TRAILER1_NUMBER + "')\n" +
                "left outer join CodingData cdt2 on cdt2.IdRmsRecords = cdd.IdRmsRecords\n" +
                "    and cdt2.CodingMasterId = (select CodingMasterId from codingmasterlookup where CodingFieldName = '" + Crms.TRAILER2_NUMBER + "')    \n" +
                "where cdd.CodingMasterId = (select CodingMasterId from codingmasterlookup where CodingFieldName = '" + Crms.DATETIME + "')\n" +
                "order by datetime desc";

        return strSql;
    }


    public static String getDvirListSql(long idRecordTypeDvirDetail, List<String> listParams, String startDate, String endDate) {

        String strSql = "select cdd.IdRmsRecords, cdd.Value datetime, cdtk.Value trucknum, cdt1.value trailernum1," +
                " cdt2.value trailernum2, rr.ObjectType, rr.ObjectId, rr.IsValid, rr.sent\n" +
                "from CodingData cdd\n" +
                "inner join rmsrecords rr on rr.id = cdd.IdRmsRecords AND rr.IdRecordType = " + idRecordTypeDvirDetail + " \n" +
                "left outer join CodingData cdtk on cdtk.IdRmsRecords = cdd.IdRmsRecords\n" +
                "    and cdtk.CodingMasterId = (select CodingMasterId from codingmasterlookup where CodingFieldName = '" + Crms.TRUCK_NUMBER + "')\n" +
                "left outer join CodingData cdt1 on cdt1.IdRmsRecords = cdd.IdRmsRecords\n" +
                "    and cdt1.CodingMasterId = (select CodingMasterId from codingmasterlookup where CodingFieldName = '" + Crms.TRAILER1_NUMBER + "')\n" +
                "left outer join CodingData cdt2 on cdt2.IdRmsRecords = cdd.IdRmsRecords\n" +
                "    and cdt2.CodingMasterId = (select CodingMasterId from codingmasterlookup where CodingFieldName = '" + Crms.TRAILER2_NUMBER + "')    \n" +
                "where cdd.CodingMasterId = (select CodingMasterId from codingmasterlookup where CodingFieldName = '" + Crms.DATETIME + "')\n" +
//                "and datetime >= " + startDate + " " +
//                "and datetime <= " + endDate + " " +
                "and datetime >= ? " +
                "and datetime <= ? " +
                "order by datetime desc";

        listParams.add(startDate);
        listParams.add(endDate);

        return strSql;
    }


    public static class ListItemDvir implements AdapterUtils.IAdapterItem<DateUtils.IDateConverter, ListItemDvir> {
        private String date;
        private String truckTractorNumber;
        private String trailerNumber;
        private String sortKey;
        private String objectType;
        private String objectId;
        private String recordId;
        private boolean isValid;
        private boolean isSent;
        private Long idRmsRecords;
        private boolean isSelected;

        public String getDate() {
            return date;
        }

        public void init(Cursor cursor, DateUtils.IDateConverter dateConverter) {
            Log.d(TAG, "ListItemDvir.init(cur) initializing listDvirs.");

            idRmsRecords = cursor.getLong(0);
            date = cursor.getString(1); //pairlistRecordData.getValue("Date");
            truckTractorNumber = cursor.getString(2); // pairlistRecordData.getValue("Truck Number");
            String tn1 = cursor.getString(3);
            String tn2 = cursor.getString(4);
            trailerNumber = StringUtils.getCompoundName(tn1, ", ", tn2);
            objectType = cursor.getString(5);
            objectId = cursor.getString(6);
            isValid = cursor.getLong(7) == 1 ? true : false;
            isSent = cursor.getLong(8) == 1 ? true : false;
//            recordId = pairlistRecordData.getValue("RecordId");

            if (!StringUtils.isNullOrWhitespaces(date)) {
                sortKey = date;
                date = dateConverter.convert(date, true);
            }
        }

        @Override
        public void init(PairList pairlistRecordData, DateUtils.IDateConverter dateConverter) {
            date = pairlistRecordData.getValue("Date");

            if (!StringUtils.isNullOrWhitespaces(date)) {
                sortKey = date;
                date = dateConverter.convert(date, true);
            }

            truckTractorNumber = pairlistRecordData.getValue("Truck Number");
            String tn1 = pairlistRecordData.getValue("Trailer1 Number");
            String tn2 = pairlistRecordData.getValue("Trailer2 Number");
            trailerNumber = StringUtils.getCompoundName(tn1, ", ", tn2);
            objectId = pairlistRecordData.getValue("LobjectId");
            objectType = pairlistRecordData.getValue("objectType");
            recordId = pairlistRecordData.getValue("RecordId");
            isValid = "1".equals(pairlistRecordData.getValue("IsValid")) ? true : false;
            isSent = "1".equals(pairlistRecordData.getValue("sent")) ? true : false;
        }

        public String getTruckTractorNumber() {
            return truckTractorNumber;
        }

        public String getTrailerNumber() {
            return trailerNumber;
        }

        @Override
        public String getSortKey() {
            return sortKey;
        }

        @Override
        public boolean isMatch(String pattern) {
            boolean isMatch = StringUtils.isSearchMatchEquiv(date, pattern, true);

            if (!isMatch) {
                isMatch = StringUtils.isSearchMatchEquiv(truckTractorNumber, pattern, true);
                if (!isMatch)
                    isMatch = StringUtils.isSearchMatchEquiv(trailerNumber, pattern, true);
            }

            return isMatch;
        }

        @Override
        public String toString() {
//            return date + "," + truckTractorNumber + "," + trailerNumber;
            return StringUtils.memberValuesToString(this);
        }

        @Override
        public int compareTo(@NonNull ListItemDvir o) {
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


        @Override
        public String getLabel() {
            return getDate();
        }

//        @Override
//        public boolean isMatch(ListItemFuelReceipt o1, String searchText) {
//            return o1.isMatch(searchText);
//        }
    }

    public void search(String title) {
        title = title.toLowerCase(Locale.getDefault());
        mDataset.clear();
        if (title.length() == 0) {
            mDataset.addAll(filterArrayList);
        } else {
            for (ListItemDvir dataModel : filterArrayList) {

                Log.d(TAG, "search: trailerNumber: " + dataModel.getTrailerNumber()
                        + " label: " + dataModel.getLabel() + " truckTractorNumber: " + dataModel.getTruckTractorNumber());

                if (dataModel.getTrailerNumber().toLowerCase().contains(title)
                        || dataModel.getLabel().toLowerCase().contains(title)
                        || dataModel.getTruckTractorNumber().toLowerCase().contains(title)) {

                    mDataset.add(dataModel);
                }
            }
        }

        if (mDataset.size() == 0) {
            pretripInterface.onNoRecordFound(true);
        } else {
            pretripInterface.onNoRecordFound(false);
            pretripInterface.onListItemClicked(0, mDataset);
        }
        notifyDataSetChanged();
    }

    public void searchBetweenDateRange(long startDateInTimeStamp, long endDateInTimeStamp) {
        Log.d(TAG, "ReceiptFragment: searchBetweenDateRange: startDateInTimeStamp: " + startDateInTimeStamp + " endDateInTimeStamp: " + endDateInTimeStamp);
        mDataset.clear();
        if (startDateInTimeStamp == 0 && endDateInTimeStamp == 0) {
            mDataset.addAll(filterArrayList);
        } else {
            Log.d(TAG, "ReceiptFragment: else: filterArrayList: size: " + filterArrayList.size());
            for (ListItemDvir dataModel : filterArrayList) {
                if (dataModel == null) {
                    return;
                }
//                Log.d(TAG, "ReceiptFragment: timeStamp: " + dataModel.getDateTimeInTimeStamp());
//
//                Log.d(TAG, "ReceiptFragment: searchBetweenDateRange: if: " + (startDateInTimeStamp <= dataModel.getDateTimeInTimeStamp() && dataModel.getDateTimeInTimeStamp() <= endDateInTimeStamp));

//                if (startDateInTimeStamp <= dataModel.getDateTimeInTimeStamp() && dataModel.getDateTimeInTimeStamp() <= endDateInTimeStamp) {
//                    mDataset.add(dataModel);
//                }
            }
            if (mDataset.size() == 0) {
//                receiptInterface.onNoRecordFound();
            }
        }
        if (mDataset.size() == 0) {
            pretripInterface.onNoRecordFound(true);
        } else {
            pretripInterface.onNoRecordFound(false);
            pretripInterface.onListItemClicked(0, mDataset);
        }
        notifyDataSetChanged();
    }

    public interface PretripInterface {
        public void onListItemClicked(int position, List<ListItemDvir> myDataset);

        public void onNoRecordFound(boolean isNoRecordFound);
    }

}