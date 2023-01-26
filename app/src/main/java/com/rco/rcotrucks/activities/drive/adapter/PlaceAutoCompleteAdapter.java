package com.rco.rcotrucks.activities.drive.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.drive.PlaceModel;

import java.util.List;

public class PlaceAutoCompleteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = PlaceAutoCompleteAdapter.class.getSimpleName();
    private List<PlaceModel> placeModels;

    private IOnClickPlace iOnClickPlace;

    public PlaceAutoCompleteAdapter(List<PlaceModel> placeModels, IOnClickPlace iOnClickPlace) {
        this.placeModels = placeModels;
        this.iOnClickPlace = iOnClickPlace;
    }

    @Override
    public RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_route, parent, false);
        return new RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        RouteViewHolder detailViewHolder = (RouteViewHolder) holder;
        detailViewHolder.setData(placeModels.get(position));
    }

    @Override
    public int getItemCount() {
        return placeModels.size();
    }

    public class RouteViewHolder extends RecyclerView.ViewHolder {
        private TextView address, time, fastRoute, miles;

        public RouteViewHolder(View v) {
            super(v);
            address = (TextView) v.findViewById(R.id.address);
            time = (TextView) v.findViewById(R.id.time);
            fastRoute = (TextView) v.findViewById(R.id.fast_route);
            miles = (TextView) v.findViewById(R.id.miles);
        }

        public void setData(PlaceModel placeModel) {

            if (placeModel.getTitle() != null) {
                fastRoute.setText(placeModel.getTitle());
            }

            if (placeModel.getAddress() != null) {
                address.setText(placeModel.getAddress());
            }

            if (placeModel.getTime() != null) {
                time.setText(placeModel.getTime());
            }

//            Log.d(TAG, "setData: ");
//            if (placeModel.getMiles() != null) {
            Log.d(TAG, "setData: miles: "+placeModel.getMiles());
                miles.setText(placeModel.getMiles());
//            } else {
//                miles.setVisibility(View.GONE);
//            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    iOnClickPlace.onGOClicked(placeModel);
                }
            });
        }
    }
}
