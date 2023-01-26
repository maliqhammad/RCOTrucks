package com.rco.rcotrucks.activities.fuelreceipts.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.fuelreceipts.model.ReceiptModel;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TollReceiptAdapter extends RecyclerView.Adapter<TollReceiptAdapter.MyViewHolder> {

    private static final String TAG = TollReceiptAdapter.class.getSimpleName();
    private List<ReceiptModel> list;
    private List<ReceiptModel> mDataset;
    ArrayList<ReceiptModel> filterArrayList = new ArrayList<>();
    ArrayList<ReceiptModel> multiDeleteArrayList = new ArrayList<>();
    boolean isEnable = false;


    Context context;
    TollReceiptAdapter.ReceiptInterface receiptInterface;

    public TollReceiptAdapter(java.util.List<ReceiptModel> list, Context context, TollReceiptAdapter.ReceiptInterface receiptInterface) {
        this.list = list;
        mDataset = list;
        this.context = context;
        this.receiptInterface = receiptInterface;

        if (list != null)
            filterArrayList.addAll(list);
    }

    @NonNull
    @Override
    public TollReceiptAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
//        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_toll_receipt, parent, false);
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_toll_receipt_update, parent, false);
        return new TollReceiptAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final TollReceiptAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        ReceiptModel receiptModel = list.get(position);

        String filterDate = StringUtils.nvl(receiptModel.getTollReceiptDateTime(), "");
        filterDate = filterDate.replace("00:00:00", "").trim();
        holder.date.setText(filterDate);
//        String stateAndRoadName = UiUtils.returnStateAbbreviation(receiptModel.getTollReceiptVendorState()) + " \n" + receiptModel.getTollReceiptRoadName();
        String stateAndRoadName = receiptModel.getTollReceiptVendorState() + " \n" + receiptModel.getTollReceiptRoadName();
        holder.stationName.setText(stateAndRoadName);

        String amountAndQuantity = "";
        holder.stationIcon.setVisibility(View.GONE);

        amountAndQuantity = receiptModel.getTollReceiptAmount().replace("$", "");
        receiptModel.setTollReceiptAmount(UiUtils.getTwoDecimalValue(amountAndQuantity));
        amountAndQuantity = "$ " + receiptModel.getTollReceiptAmount();
        holder.amount.setText(amountAndQuantity);

        if (receiptModel.isSelected()) {
            holder.constraintLayout.setBackgroundResource(R.drawable.backround_receipt_curved_top_light_grey);
            holder.stationName.setTextColor(context.getResources().getColor(R.color.white));
            holder.amount.setTextColor(context.getResources().getColor(R.color.white));
            holder.openDetail.setColorFilter(context.getResources().getColor(R.color.white));
        } else {
            holder.constraintLayout.setBackgroundResource(R.drawable.dark_mode_black_and_white_curved_background);
            holder.stationName.setTextColor(context.getResources().getColor(R.color.black));
            holder.amount.setTextColor(context.getResources().getColor(R.color.black));
            holder.openDetail.setColorFilter(context.getResources().getColor(R.color.black));
        }

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnable) {
                    ClickItem(position, holder);
                } else {
                    receiptInterface.onListItemClicked(position, list);
                }
            }
        });

        holder.itemLayout.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isEnable = true;
                ClickItem(position, holder);
                return true;
            }
        });
    }


    private void ClickItem(int position, TollReceiptAdapter.MyViewHolder holder) {
        Log.d(TAG, "ClickItem: filterArrayList: " + filterArrayList.size());

        ReceiptModel receiptModel = list.get(holder.getAdapterPosition());
        if (holder.checkBox.getVisibility() == View.GONE) {
            holder.checkBox.setVisibility(View.VISIBLE);

            holder.constraintLayout.setBackgroundResource(R.drawable.backround_receipt_curved_top_light_grey);
            holder.stationName.setTextColor(context.getResources().getColor(R.color.white));
            holder.amount.setTextColor(context.getResources().getColor(R.color.white));
            holder.openDetail.setColorFilter(context.getResources().getColor(R.color.white));

            multiDeleteArrayList.add(receiptModel);
        } else {
            holder.checkBox.setVisibility(View.GONE);

            holder.constraintLayout.setBackgroundResource(R.drawable.dark_mode_black_and_white_curved_background);
            holder.stationName.setTextColor(context.getResources().getColor(R.color.black));
            holder.amount.setTextColor(context.getResources().getColor(R.color.black));
            holder.openDetail.setColorFilter(context.getResources().getColor(R.color.black));

            multiDeleteArrayList.remove(receiptModel);
        }

        if (multiDeleteArrayList.size() == 0) {
            isEnable = false;
        }
        receiptInterface.onItemLongClick(position, list, multiDeleteArrayList);

//        Jan 02, 2022  -   We can show selected items count using it if required
//        receiptModel.setText(String.valueOf(filterArrayList.size()));
    }


    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout itemLayout, constraintLayout;
        TextView date, stationName, amount;
        ImageView stationIcon, openDetail, checkBox;

        public MyViewHolder(View itemView) {
            super(itemView);

            itemLayout = itemView.findViewById(R.id.receipt_item);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            checkBox = itemView.findViewById(R.id.check_box);
            date = itemView.findViewById(R.id.text_view_date);
            stationName = itemView.findViewById(R.id.station_name);
            amount = itemView.findViewById(R.id.receipt_amount);
            stationIcon = itemView.findViewById(R.id.station_image);
            openDetail = itemView.findViewById(R.id.open_receipt_detail_icon);
        }
    }

    public List<ReceiptModel> getmDataset() {
        return mDataset;
    }

    public void search(String title) {

        Log.d(TAG, "searchFilter: searched Value is " + title + " lenght: " + title.length());
        title = title.toLowerCase(Locale.getDefault());
        list.clear();
        if (title.length() == 0) {

            list.addAll(filterArrayList);
        } else {
            Log.d(TAG, "search: else: filterArrayList: size: " + filterArrayList.size());
            for (ReceiptModel dataModel : filterArrayList) {
                Log.d(TAG, "search: vendor: name: " + dataModel.getVendorName() + " state: " + dataModel.getVendorState());
                if (dataModel == null) {
                    return;
                }

                if (dataModel.getTollReceiptVendorName().toLowerCase().contains(title)
                        || dataModel.getTollReceiptVendorState().toLowerCase().contains(title)
                        || dataModel.getTollReceiptAmount().toLowerCase().contains(title)
                        || dataModel.getTollReceiptDateTime().toLowerCase().contains(title)) {
                    Log.d(TAG, "searchFilter: title " + title + " and adding to product list");

                    list.add(dataModel);
                }
            }
        }

        if (list.size() == 0) {
            receiptInterface.onNoRecordFound(true);
        } else {
            receiptInterface.onNoRecordFound(false);
            receiptInterface.onListItemClicked(0, list);
        }

        Log.d(TAG, "searchFilter: ");
        notifyDataSetChanged();
    }


    public void searchBetweenDateRange(long startDateInTimeStamp, long endDateInTimeStamp) {
        Log.d(TAG, "ReceiptFragment: searchBetweenDateRange: startDateInTimeStamp: " + startDateInTimeStamp + " endDateInTimeStamp: " + endDateInTimeStamp);
        list.clear();
        if (startDateInTimeStamp == 0 && endDateInTimeStamp == 0) {
            list.addAll(filterArrayList);
        } else {
            Log.d(TAG, "ReceiptFragment: else: filterArrayList: size: " + filterArrayList.size());
            for (ReceiptModel dataModel : filterArrayList) {
                if (dataModel == null) {
                    return;
                }
                Log.d(TAG, "ReceiptFragment: timeStamp: " + dataModel.getDateTimeInTimeStamp());

                Log.d(TAG, "ReceiptFragment: searchBetweenDateRange: if: " + (startDateInTimeStamp <= dataModel.getDateTimeInTimeStamp() && dataModel.getDateTimeInTimeStamp() <= endDateInTimeStamp));

                if (startDateInTimeStamp <= dataModel.getDateTimeInTimeStamp() && dataModel.getDateTimeInTimeStamp() <= endDateInTimeStamp) {
                    list.add(dataModel);
                }
            }
        }

        if (list.size() == 0) {
            receiptInterface.onNoRecordFound(true);
        } else {
            receiptInterface.onNoRecordFound(false);
            receiptInterface.onListItemClicked(0, list);
        }

        Log.d(TAG, "searchFilter: ");
        notifyDataSetChanged();
    }


    public void populateFilterArrayList(List<ReceiptModel> fuelReceiptList) {
        filterArrayList.clear();
        filterArrayList.addAll(fuelReceiptList);
    }

    public interface ReceiptInterface {
        public void onListItemClicked(int position, java.util.List<ReceiptModel> list);

        public void onNoRecordFound(boolean isFound);

        public void onItemLongClick(int position, java.util.List<ReceiptModel> list, ArrayList<ReceiptModel> filterArrayList);
    }
}