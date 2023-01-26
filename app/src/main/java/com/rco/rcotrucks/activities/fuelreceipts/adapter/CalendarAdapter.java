package com.rco.rcotrucks.activities.fuelreceipts.adapter;

import com.rco.rcotrucks.R;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class CalendarAdapter extends RecyclerView.Adapter<CalendarAdapter.MyViewHolder> {

    private static final String TAG = CalendarAdapter.class.getSimpleName();
    private String list[];
    Context context;
    CalendarInterface calendarInterface;

    public CalendarAdapter(String[] list, Context context, CalendarInterface calendarInterface) {
        this.list = list;
        this.context = context;
        this.calendarInterface = calendarInterface;

    }

    @NonNull
    @Override
    public CalendarAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_calendar, parent, false);
        return new CalendarAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CalendarAdapter.MyViewHolder holder, @SuppressLint("RecyclerView") final int position) {

        holder.itemList.setText(list[position]);
        holder.itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendarInterface.onListItemClicked(position, list[position]);
            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list.length;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ConstraintLayout itemLayout;
        TextView itemList;

        public MyViewHolder(View itemView) {
            super(itemView);

            itemLayout = itemView.findViewById(R.id.itemLayout);
            itemList = itemView.findViewById(R.id.itemList);
        }
    }

    public interface CalendarInterface {
        public void onListItemClicked(int position, String selectedDateValue);
    }
}