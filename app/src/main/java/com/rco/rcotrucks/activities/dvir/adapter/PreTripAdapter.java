package com.rco.rcotrucks.activities.dvir.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
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
import com.rco.rcotrucks.model.PretripModel;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;
import com.rco.rcotrucks.utils.UiUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class PreTripAdapter extends RecyclerView.Adapter<PreTripAdapter.MyViewHolder> {

    private static final String TAG = PreTripAdapter.class.getSimpleName();
    private List<PretripModel> list;
    private List<PretripModel> mDataset;
    ArrayList<PretripModel> filterArrayList = new ArrayList<>();
    ArrayList<PretripModel> multiDeleteArrayList = new ArrayList<>();
    boolean isEnable = false;

    Context context;
    PreTripAdapter.PretripInterface preTripInterface;

    public PreTripAdapter(java.util.List<PretripModel> list, Context context, PreTripAdapter.PretripInterface preTripInterface) {
        this.list = list;
        mDataset = list;
        this.context = context;
        this.preTripInterface = preTripInterface;

        if (list != null)
            filterArrayList.addAll(list);
    }

    @NonNull
    @Override
    public PreTripAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_pretrip, parent, false);
        return new PreTripAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final PreTripAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        PretripModel pretripModel = list.get(position);

        String dateFromDateTime = UiUtils.getDateAfterSplittingDateAndTimeBySpace(pretripModel.getDateTime());
        String formattedTime = DateUtils.convertDateTime(dateFromDateTime, DateUtils.FORMAT_DATE_YYYY_MM_DD, DateUtils.FORMAT_DATE_MM_DD_YYYY);
        String date = "Date: " + formattedTime;
        holder.date.setText(date);

        String truckTractorNumber = "Truck Number: " + pretripModel.getTruckNumber();
        String trailerOneAndTwo = "Trailer Number: " + pretripModel.getTrailer1() + ", " + pretripModel.getTrailer2();
        trailerOneAndTwo = trailerOneAndTwo.replace(", null", "");

        Log.d(TAG, "onBindViewHolder: truckTractorNumber: " + truckTractorNumber + " trailer: " + trailerOneAndTwo);
        holder.truckTractorNumber.setText(truckTractorNumber);
        holder.trailerNumber.setText(trailerOneAndTwo);

//        Jan 18, 2022  -   Commented for now
//        if (!pretripModel.isValid) {
//            ImageViewCompat.setImageTintList(holder.openDetail, ColorStateList.valueOf(Color.RED));
//        } else if (!pretripModel.getSent()) {
//            ImageViewCompat.setImageTintList(holder.openDetail, ColorStateList.valueOf(Color.GREEN));
//        } else {
//            ImageViewCompat.setImageTintList(holder.openDetail, ColorStateList.valueOf(Color.BLACK));
//        }

        Log.d(TAG, "onBindViewHolder: isSelected: " + pretripModel.getSelected()+" position: "+position);
        if (pretripModel.getSelected()) {
            holder.constraintLayout.setBackgroundResource(R.drawable.backround_receipt_curved_top_light_grey);
            holder.truckTractorNumber.setTextColor(context.getResources().getColor(R.color.white));
            holder.trailerNumber.setTextColor(context.getResources().getColor(R.color.white));
            holder.openDetail.setColorFilter(context.getResources().getColor(R.color.white));
        } else {
            holder.constraintLayout.setBackgroundResource(R.drawable.dark_mode_black_and_white_curved_background);
            holder.truckTractorNumber.setTextColor(context.getResources().getColor(R.color.black));
            holder.trailerNumber.setTextColor(context.getResources().getColor(R.color.black));
            holder.openDetail.setColorFilter(context.getResources().getColor(R.color.black));
        }

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isEnable) {
                    ClickItem(position, holder);
                } else {
                    preTripInterface.onListItemClicked(position, list);
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


    private void ClickItem(int position, PreTripAdapter.MyViewHolder holder) {
        Log.d(TAG, "ClickItem: filterArrayList: " + filterArrayList.size());

        PretripModel pretripModel = list.get(holder.getAdapterPosition());
        if (holder.checkBox.getVisibility() == View.GONE) {
            holder.checkBox.setVisibility(View.VISIBLE);

            holder.constraintLayout.setBackgroundResource(R.drawable.backround_receipt_curved_top_light_grey);
            holder.truckTractorNumber.setTextColor(context.getResources().getColor(R.color.white));
            holder.trailerNumber.setTextColor(context.getResources().getColor(R.color.white));
            holder.openDetail.setColorFilter(context.getResources().getColor(R.color.white));

            multiDeleteArrayList.add(pretripModel);
        } else {
            holder.checkBox.setVisibility(View.GONE);

            holder.constraintLayout.setBackgroundResource(R.drawable.dark_mode_black_and_white_curved_background);
            holder.truckTractorNumber.setTextColor(context.getResources().getColor(R.color.black));
            holder.trailerNumber.setTextColor(context.getResources().getColor(R.color.black));
            holder.openDetail.setColorFilter(context.getResources().getColor(R.color.black));

            multiDeleteArrayList.remove(pretripModel);
        }

        if (multiDeleteArrayList.size() == 0) {
            isEnable = false;
        }
        preTripInterface.onItemLongClick(position, list, multiDeleteArrayList);

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
        TextView date, truckTractorNumber, trailerNumber;
        ImageView openDetail, checkBox;

        public MyViewHolder(View itemView) {
            super(itemView);

            itemLayout = itemView.findViewById(R.id.item_pretrip);
            constraintLayout = itemView.findViewById(R.id.constraintLayout);
            checkBox = itemView.findViewById(R.id.check_box);

            date = itemView.findViewById(R.id.text_view_date);
            truckTractorNumber = itemView.findViewById(R.id.truck_tractor_number);
            trailerNumber = itemView.findViewById(R.id.trailer_number);
            openDetail = itemView.findViewById(R.id.open_detail_icon);
        }
    }

    public List<PretripModel> getmDataset() {
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
            for (PretripModel dataModel : filterArrayList) {
//                Log.d(TAG, "search: vendor: name: " + dataModel.getVendorName() + " state: " + dataModel.getVendorState());
                if (dataModel == null) {
                    return;
                }

                if (dataModel.getDateTime().toLowerCase().contains(title)
                        || dataModel.getTruckNumber().toLowerCase().contains(title)
                        || dataModel.getTrailer1().toLowerCase().contains(title)
                        || dataModel.getTrailer2().toLowerCase().contains(title)) {
                    Log.d(TAG, "searchFilter: title " + title + " and adding to product list");

                    list.add(dataModel);
                }
            }
        }

        if (list.size() == 0) {
            preTripInterface.onNoRecordFound(true);
        } else {
            preTripInterface.onNoRecordFound(false);
            preTripInterface.onListItemClicked(0, list);
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
            for (PretripModel dataModel : filterArrayList) {
                if (dataModel == null) {
                    return;
                }
                Log.d(TAG, "ReceiptFragment: timeStamp: " + dataModel.getDateTime());

                Log.d(TAG, "ReceiptFragment: searchBetweenDateRange: if: " + (startDateInTimeStamp <= dataModel.getDateTimeInTimeStamp() && dataModel.getDateTimeInTimeStamp() <= endDateInTimeStamp));

                if (startDateInTimeStamp <= dataModel.getDateTimeInTimeStamp() && dataModel.getDateTimeInTimeStamp() <= endDateInTimeStamp) {
                    list.add(dataModel);
                }
            }
        }

        if (list.size() == 0) {
            preTripInterface.onNoRecordFound(true);
        } else {
            preTripInterface.onNoRecordFound(false);
            preTripInterface.onListItemClicked(0, list);
        }

        Log.d(TAG, "searchFilter: ");
        notifyDataSetChanged();
    }


    public void populateFilterArrayList(List<PretripModel> preTripModelList) {
        filterArrayList.clear();
        filterArrayList.addAll(preTripModelList);
    }

    public interface PretripInterface {
        public void onListItemClicked(int position, java.util.List<PretripModel> list);

        public void onNoRecordFound(boolean isFound);

        public void onItemLongClick(int position, java.util.List<PretripModel> list, ArrayList<PretripModel> filterArrayList);
    }
}