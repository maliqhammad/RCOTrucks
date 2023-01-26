package com.rco.rcotrucks.activities.logbook;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;

import java.util.List;

public class EldViolationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Violation> eldViolationItems;

    public EldViolationAdapter(List<Violation> eldViolationItems) {
        this.eldViolationItems = eldViolationItems;
    }

    @Override
    public EldViolationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eld_violation, parent, false);
        return new EldViolationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EldViolationViewHolder eldViolationViewHolder = (EldViolationViewHolder) holder;
        eldViolationViewHolder.setData(eldViolationItems.get(position));
    }

    @Override
    public int getItemCount() {
        return eldViolationItems.size();
    }

}
