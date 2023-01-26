package com.rco.rcotrucks.activities.logbook;


import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.Tuple;

import java.util.List;

public class EldDetailAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Tuple<String, String>> eldDetails;

    public EldDetailAdapter(List<Tuple<String, String>> eldDetails) {
        this.eldDetails = eldDetails;
    }

    @Override
    public EldDetailViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_eld_event_detail, parent, false);
        return new EldDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EldDetailViewHolder detailViewHolder = (EldDetailViewHolder) holder;
        detailViewHolder.setData(eldDetails.get(position));
    }

    @Override
    public int getItemCount() {
        return eldDetails.size();
    }

    public class EldDetailViewHolder extends RecyclerView.ViewHolder {
        private TextView title, description;

        public EldDetailViewHolder(View v) {
            super(v);
            title = (TextView) v.findViewById(R.id.title);
            description = (TextView) v.findViewById(R.id.description);
        }

        public void setData(Tuple<String, String> eld) {
            title.setText(eld.getElement0());
            description.setText(eld.getElement1());

            if (getPosition() %2 ==0) {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.white));
            } else {
                itemView.setBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.bg_eld_color));
            }
        }
    }
}
