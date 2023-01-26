package com.rco.rcotrucks.activities.drive.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.model.MapAssetModel;

import java.util.List;

public class MapAssetsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final String TAG = MapAssetsAdapter.class.getSimpleName();
    private List<MapAssetModel> placeModels;
    Context context;

    public MapAssetsAdapter(Context context, List<MapAssetModel> placeModels) {
        this.context = context;
        this.placeModels = placeModels;
    }

    @Override
    public MapAssetsAdapter.RouteViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_map_assets, parent, false);
        return new MapAssetsAdapter.RouteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MapAssetsAdapter.RouteViewHolder detailViewHolder = (MapAssetsAdapter.RouteViewHolder) holder;
        detailViewHolder.setData(placeModels.get(position));
    }

    @Override
    public int getItemCount() {
        return placeModels.size();
    }

    public class RouteViewHolder extends RecyclerView.ViewHolder {
        ConstraintLayout itemLayout;
        ImageView mapAssetImage;
        TextView mapAssetTitle;
        Switch mapAssetSwitch;

        public RouteViewHolder(View v) {
            super(v);
            itemLayout = v.findViewById(R.id.item_map_asset);
            mapAssetImage = v.findViewById(R.id.map_asset_image);
            mapAssetTitle = v.findViewById(R.id.map_asset_title);
            mapAssetSwitch = v.findViewById(R.id.map_asset_switch);
        }

        public void setData(MapAssetModel placeModel) {

            mapAssetTitle.setText(placeModel.getName());
            setMapAssetIcon(placeModel, mapAssetImage);

            if (placeModel.isAssetSelected()) {
                mapAssetSwitch.setChecked(true);
            } else {
                mapAssetSwitch.setChecked(false);
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }


    void setMapAssetIcon(MapAssetModel placeModel, ImageView mapAssetIV) {
        if (placeModel.getName().equalsIgnoreCase("fuel")) {
            Glide.with(context).load(R.drawable.map_fuel).into(mapAssetIV);
        } else if (placeModel.getName().equalsIgnoreCase("ta")) {
            Glide.with(context).load(R.drawable.ic_ta).into(mapAssetIV);
        }  else if (placeModel.getName().equalsIgnoreCase("pilot")) {
            Glide.with(context).load(R.drawable.ic_pilot).into(mapAssetIV);
        }  else if (placeModel.getName().equalsIgnoreCase("loves")) {
            Glide.with(context).load(R.drawable.ic_love_pol).into(mapAssetIV);
        }  else if (placeModel.getName().equalsIgnoreCase("reefer")) {
            Glide.with(context).load(R.drawable.map_reefer).into(mapAssetIV);
        }   else if (placeModel.getName().equalsIgnoreCase("Carrier")) {
            Glide.with(context).load(R.drawable.map_reefercarrier).into(mapAssetIV);
        }   else if (placeModel.getName().equalsIgnoreCase("Thermo King")) {
            Glide.with(context).load(R.drawable.map_reeferthermoking).into(mapAssetIV);
        } else if (placeModel.getName().equalsIgnoreCase("cat scale")) {
            Glide.with(context).load(R.drawable.map_catscale).into(mapAssetIV);
        } else if (placeModel.getName().equalsIgnoreCase("rest areas")) {
            Glide.with(context).load(R.drawable.map_restarea).into(mapAssetIV);
        } else if (placeModel.getName().equalsIgnoreCase("truck stop")) {
            Glide.with(context).load(R.drawable.ic_baseline_stop_24).into(mapAssetIV);
        } else if (placeModel.getName().equalsIgnoreCase("truck wash")) {
            Glide.with(context).load(R.drawable.map_truckwash).into(mapAssetIV);
        } else if (placeModel.getName().equalsIgnoreCase("walmart")) {
            Glide.with(context).load(R.drawable.map_walmart).into(mapAssetIV);
        } else if (placeModel.getName().equalsIgnoreCase("Independent")) {
            Glide.with(context).load(R.drawable.map_walmart).into(mapAssetIV);
        }

    }
}

