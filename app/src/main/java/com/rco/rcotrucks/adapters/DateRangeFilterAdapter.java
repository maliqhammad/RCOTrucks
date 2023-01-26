package com.rco.rcotrucks.adapters;

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
import com.rco.rcotrucks.model.DateRangeModel;

import java.util.List;

public class DateRangeFilterAdapter extends RecyclerView.Adapter<DateRangeFilterAdapter.MyViewHolder> {

    private static final String TAG = DateRangeFilterAdapter.class.getSimpleName();
    private List<DateRangeModel> list;
    Context context;
    DateRangeFilterAdapter.DateRangeInterface dateRangeInterface;
    int lastSelectedFilterPosition = -1;

    public DateRangeFilterAdapter(java.util.List<DateRangeModel> list, Context context, DateRangeFilterAdapter.DateRangeInterface dateRangeInterface) {
        this.list = list;
        this.context = context;
        this.dateRangeInterface = dateRangeInterface;
    }

    @NonNull
    @Override
    public DateRangeFilterAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_filter_date_range, parent, false);
        return new DateRangeFilterAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DateRangeFilterAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {
        DateRangeModel dateModel = list.get(position);
        holder.date.setText(dateModel.getDate());

        if (dateModel.isSelected()) {
            holder.tickIcon.setVisibility(View.VISIBLE);
            lastSelectedFilterPosition = position;
        } else {
            holder.tickIcon.setVisibility(View.GONE);
        }

        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setSelectionOnNewFilter(position);
                dateRangeInterface.onListItemClicked(position);
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

        ConstraintLayout itemLayout;
        TextView date;
        ImageView tickIcon;

        public MyViewHolder(View itemView) {
            super(itemView);

            itemLayout = itemView.findViewById(R.id.item_layout);
            date = itemView.findViewById(R.id.date);
            tickIcon = itemView.findViewById(R.id.tick_icon);
        }
    }

    public interface DateRangeInterface {
        public void onListItemClicked(int position);
    }

    void setSelectionOnNewFilter(int position) {
        Log.d(TAG, "setSelectionOnNewFilter: ");
        for (int i = 0; i < list.size(); i++) {
            if (i == position) {
                list.get(i).setSelected(true);
            } else {
                list.get(i).setSelected(false);
            }
        }
        notifyDataSetChanged();
    }
}