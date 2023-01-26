package com.rco.rcotrucks.activities.logbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;

import java.util.ArrayList;
import java.util.List;

public class EldInfoAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<String> eldInfoItems;

    public EldInfoAdapter(List<String> eldInfoItems) {
        this.eldInfoItems = eldInfoItems;
    }

    @Override
    public EldInfoViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eld_info, parent, false);
        return new EldInfoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EldInfoViewHolder eldInfoViewHolder = (EldInfoViewHolder) holder;
        eldInfoViewHolder.setData(eldInfoItems.get(position), position);
    }

    @Override
    public int getItemCount() {
        return eldInfoItems.size();
    }

}
