package com.rco.rcotrucks.activities.drive.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.drive.PlaceModel;

import java.util.List;

public class RecentSearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<PlaceModel> placeModels;
    private IOnClickPlace iOnClickPlace;

    public RecentSearchAdapter(List<PlaceModel> placeModels, IOnClickPlace iOnClickPlace) {
        this.placeModels = placeModels;
        this.iOnClickPlace = iOnClickPlace;
    }

    @Override
    public int getItemViewType(int position) {
        return placeModels.get(position).getCreated_time() != null ? 1 : 0;
    }

    @Override
    public int getItemCount() {
        return placeModels.size();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new TitleViewHolderHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_title_search, parent, false));
        }
        return new RecentSearchViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_recent_search, parent, false));
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        switch (holder.getItemViewType()) {
            case 0:
                TitleViewHolderHolder viewHolderTitle = (TitleViewHolderHolder) holder;
                viewHolderTitle.bindData(placeModels.get(position));
                break;

            case 1:
                RecentSearchViewHolder viewHolderPlace = (RecentSearchViewHolder) holder;
                viewHolderPlace.bindData(placeModels.get(position));
                break;
        }
    }

    class TitleViewHolderHolder extends RecyclerView.ViewHolder {
        private TextView title;

        public TitleViewHolderHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
        }

        public void bindData(PlaceModel placeModel) {
            if (placeModel.getTitle() != null) {
                title.setText(placeModel.getTitle());
            }
        }
    }

    class RecentSearchViewHolder extends RecyclerView.ViewHolder {
        private TextView title, address;

        public RecentSearchViewHolder(View itemView) {
            super(itemView);
            title = (TextView) itemView.findViewById(R.id.title);
            address = (TextView) itemView.findViewById(R.id.address);
        }

        public void bindData(PlaceModel placeModel) {
            if (placeModel.getTitle() != null) {
                title.setText(placeModel.getTitle());
            }
            if (placeModel.getAddress() != null) {
                address.setText(placeModel.getAddress());
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iOnClickPlace.onGOClicked(placeModel);
                }
            });
        }
    }
}

