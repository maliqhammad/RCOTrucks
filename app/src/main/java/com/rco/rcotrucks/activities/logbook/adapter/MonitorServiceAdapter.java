package com.rco.rcotrucks.activities.logbook.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.utils.DateUtils;

import java.util.List;

public class MonitorServiceAdapter extends RecyclerView.Adapter<MonitorServiceAdapter.MyViewHolder> {

    private static final String TAG = MonitorServiceAdapter.class.getSimpleName();
    List<EldEvent> list;
    Context context;
//    SelectionListener selectionListener;

    //    public MonitorServiceAdapter(List<EldEvent> list, Context context, SelectionListener selectionListener) {
    public MonitorServiceAdapter(List<EldEvent> list, Context context) {

        this.list = list;
        this.context = context;
//        this.selectionListener = selectionListener;
    }

    @NonNull
    @Override
    public MonitorServiceAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_monitor_service, parent, false);
        return new MonitorServiceAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final MonitorServiceAdapter.MyViewHolder holder, final int position) {
        final EldEvent dataModel = list.get(getItemViewType(position));

        Log.d(TAG, "onBindViewHolder: index: " + position + " CheckData: " + dataModel.CheckData + " CheckSum: " + dataModel.CheckSum);
        holder.date.setText(DateUtils.getMonthDayYearDateFromDateAndTime(dataModel.CreationDate));
        holder.checkedDetail.setText((position + 1) + ". Certified Date: " + DateUtils.removeMilliSecondsFromDateAndTime(dataModel.CreationDate)
                + " By: " + dataModel.EldUsername);

        holder.monitorServiceItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                selectionListener.edit(dataModel);
            }
        });
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

        TextView date, checkedDetail;
        ConstraintLayout monitorServiceItemLayout;

        public MyViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            checkedDetail = itemView.findViewById(R.id.checkedDetail);
            monitorServiceItemLayout = itemView.findViewById(R.id.monitorServiceItemLayout);
        }
    }

//    public interface SelectionListener {
//        void edit(EldEvent dataModel);
//
//        void delete(int position);
//    }
}