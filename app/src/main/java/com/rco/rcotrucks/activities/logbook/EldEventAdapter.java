package com.rco.rcotrucks.activities.logbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;

import java.util.List;

public class EldEventAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<LogBookELDEvent> eldEventItems;

    public EldEventAdapter(List<LogBookELDEvent> eldEventItems) {
        this.eldEventItems = eldEventItems;
    }

    @Override
    public EldEventViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eld_event, parent, false);
        return new EldEventViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EldEventViewHolder eldEventViewHolder = (EldEventViewHolder) holder;
        eldEventViewHolder.setData(eldEventItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return eldEventItems.size();
    }

}
