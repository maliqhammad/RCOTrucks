package com.rco.rcotrucks.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.bumptech.glide.Glide;
import com.rco.rcotrucks.R;
import com.rco.rcotrucks.model.MapAssetModel;
import com.rco.rcotrucks.model.MapAssetSubcategoriesModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapAssetExpandableAdapter extends BaseExpandableListAdapter {

    private static final String TAG = MapAssetExpandableAdapter.class.getSimpleName();
    private List<MapAssetModel> headerData;
    private HashMap<String, ArrayList<MapAssetSubcategoriesModel>> childData;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public MapAssetExpandableAdapter(Context mContext, List<MapAssetModel> headerData,
                                     HashMap<String, ArrayList<MapAssetSubcategoriesModel>> childData) {
        Log.d(TAG, "MapAssetExpandableAdapter: ");
        this.mContext = mContext;
        this.headerData = headerData;
        this.childData = childData;
        Log.d(TAG, "MapAssetExpandableAdapter: childData: " + childData.size());
        this.layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getGroup(int headPosition) {
        Log.d(TAG, "getGroup: headPosition: " + headPosition);
        return this.headerData.get(headPosition);
    }

    @Override
    public long getGroupId(int headPosition) {
        Log.d(TAG, "getGroupId: headPosition: " + headPosition);
        return headPosition;
    }

    @Override
    public View getGroupView(int headPosition, boolean is_expanded, View view, ViewGroup headGroup) {
        Log.d(TAG, "getGroupView: headPosition: " + headPosition);
        if (headPosition < this.headerData.size()) {

            MapAssetModel mMapAssetModel = (MapAssetModel) getGroup(headPosition);
            if (view == null) {
                view = layoutInflater.inflate(R.layout.item_map_assets, null);
            }

            ConstraintLayout itemLayout = view.findViewById(R.id.item_map_asset);
            ImageView mapAssetImage = view.findViewById(R.id.map_asset_image);
            TextView mapAssetTitle = view.findViewById(R.id.map_asset_title);
            Switch mapAssetSwitch = view.findViewById(R.id.map_asset_switch);

            Log.d(TAG, "getGroupView: model: " + mMapAssetModel + " mapAssetTitle: " + mapAssetTitle);
            mapAssetTitle.setText(mMapAssetModel.getName());
            setSubCategoryMapAssetIcon(mMapAssetModel.getMapAssetId(), mapAssetImage);

            ExpandableListView mExpandableListView = (ExpandableListView) headGroup;
            if (mMapAssetModel.isAssetSelected()) {
                mapAssetSwitch.setChecked(true);
                mExpandableListView.expandGroup(headPosition);
            } else {
                mExpandableListView.collapseGroup(headPosition);
                mapAssetSwitch.setChecked(false);
            }

            mapAssetSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ExpandableListView mExpandableListView = (ExpandableListView) headGroup;
                    Log.d(TAG, "onClick: headGroup: " + headGroup.toString() + " headPosition: " + headPosition);
                    Log.d(TAG, "onClick: if: " + (mExpandableListView.isGroupExpanded(headPosition)));

                    if (mExpandableListView.isGroupExpanded(headPosition)) {
                        mExpandableListView.collapseGroup(headPosition);
                        mapAssetSwitch.setChecked(false);
                        headerData.get(headPosition).setAssetSelected(false);
                    } else {
                        mExpandableListView.expandGroup(headPosition);
                        mapAssetSwitch.setChecked(true);
                        headerData.get(headPosition).setAssetSelected(true);
                    }

                }
            });

        }

        return view;
    }

    @Override
    public int getGroupCount() {
        return this.headerData.size();
    }

    @Override
    public Object getChild(int headPosition, int childPosition) {
        Log.d(TAG, "getChild: headPosition: " + headPosition + " childPosition: " + childPosition);
        return this.childData.get(this.headerData.get(headPosition).getName()).get(childPosition);
    }

    @Override
    public long getChildId(int headPosition, int childPosition) {
        return childPosition;
    }


    @Override
    public View getChildView(int headPosition, int childPosition, boolean islastChild, View view, ViewGroup viewGroup) {
        Log.d(TAG, "getChildView: from getGroupView: headPosition: headPosition: " + headPosition + " childPosition: " + childPosition);
        MapAssetSubcategoriesModel child = (MapAssetSubcategoriesModel) getChild(headPosition, childPosition);


        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_subcategory_map_asset, null);
        }

        ImageView mapAssetImage = view.findViewById(R.id.map_subcategory_asset_image);
        TextView mapAssetTitle = view.findViewById(R.id.map_subcategory_asset_title);
        Switch mapAssetSwitch = view.findViewById(R.id.map_subcategory_asset_switch);

        if (mapAssetTitle != null) {
            mapAssetTitle.setText(child.getSubCategoryName());
            setSubCategoryMapAssetIcon(child.getMapAssetId(), mapAssetImage);

            if (child.isSelected()) {
                mapAssetSwitch.setChecked(true);
            } else {
                mapAssetSwitch.setChecked(false);
            }

            mapAssetSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    Log.d(TAG, "onCheckedChanged: ");
                    if (isChecked) {
                        child.setSelected(true);
                    } else {
                        child.setSelected(false);
                    }
                }
            });

        }
        return view;
    }

    @Override
    public int getChildrenCount(int headPosition) {
        Log.d(TAG, "getChildrenCount: headPosition: " + headPosition + " size: " + this.headerData.size());
        Log.d(TAG, "getChildrenCount: this.headerData.get(headPosition): " + this.headerData.get(headPosition));
        return this.childData.get(this.headerData.get(headPosition).getName()).size();
    }


    @Override
    public boolean isChildSelectable(int headPosition, int childPosition) {
        return true;
    }

    void setSubCategoryMapAssetIcon(int mapAsset, ImageView mapAssetIV) {
        Glide.with(mContext).load(mapAsset).into(mapAssetIV);
    }

    @Override
    public boolean hasStableIds() {
        Log.d(TAG, "hasStableIds: ");
        return false;
    }


}