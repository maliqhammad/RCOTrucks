package com.rco.rcotrucks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.model.MapAssetModel;
import com.rco.rcotrucks.model.MapAssetSubcategoriesModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MapAssetsExpandableAdapter extends BaseExpandableListAdapter {

    //Initializing variables
    private List<MapAssetModel> headerData;
    private HashMap<String, ArrayList<MapAssetSubcategoriesModel>> childData;
    private Context mContext;
    private LayoutInflater layoutInflater;

    // constructor
    public MapAssetsExpandableAdapter(Context mContext, List<MapAssetModel> headerData,
                                   HashMap<String, ArrayList<MapAssetSubcategoriesModel>> childData) {
        this.mContext = mContext;
        this.headerData = headerData;
        this.childData = childData;
        this.layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public int getGroupCount() {
        return this.headerData.size();
    }

    @Override
    public int getChildrenCount(int headPosition) {
        return this.childData.get(this.headerData.get(headPosition).getName()).size();
    }

    @Override
    public Object getGroup(int headPosition) {
        return this.headerData.get(headPosition);
    }

    @Override
    public Object getChild(int headPosition, int childPosition) {
        return this.childData.get(this.headerData.get(headPosition).getName())
                .get(childPosition);
    }

    @Override
    public long getGroupId(int headPosition) {
        return headPosition;
    }

    @Override
    public long getChildId(int headPosition, int childPosition) {
        return this.childData.get(this.headerData.get(headPosition))
                .get(childPosition).getId();
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int headPosition, boolean is_expanded, View view, ViewGroup headGroup) {
        // Heading of each group
//        ExpandableListView mExpandableListView = (ExpandableListView) headGroup;
//        mExpandableListView.expandGroup(headPosition);
        MapAssetModel mapAssetModel = (MapAssetModel) getGroup(headPosition);
        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_map_assets, null);
        }
        TextView headerTv = view.findViewById(R.id.map_asset_title);
        headerTv.setText(mapAssetModel.getName());

        ExpandableListView mExpandableListView = (ExpandableListView) headGroup;
        if (mapAssetModel.isAssetSelected()) {
//            mapAssetSwitch.setChecked(true);
            mExpandableListView.expandGroup(headPosition);
        } else {
            mExpandableListView.collapseGroup(headPosition);
//            mapAssetSwitch.setChecked(false);
        }


        return view;
    }

    @Override
    public View getChildView(int headPosition, int childPosition, boolean islastChild, View view, ViewGroup viewGroup) {

        MapAssetSubcategoriesModel child = (MapAssetSubcategoriesModel) getChild(headPosition, childPosition);

        if (view == null) {
            view = layoutInflater.inflate(R.layout.item_subcategory_map_asset, null);
        }

//        TextView childTv = (TextView) view.findViewById(R.id.prodcutname);
//        ImageView childImg = (ImageView) view.findViewById(R.id.productImg);
        TextView prodcutname = (TextView) view.findViewById(R.id.map_subcategory_asset_title);
//        TextView productprice = (TextView) view.findViewById(R.id.productprice);

        prodcutname.setText(child.getSubCategoryName());
//        childTv.setText(child.getProductTitle());
//        productprice.setText(child.getProductPrice());


//        childImg.setImageResource(child.getImage());
        return view;
    }

    @Override
    public boolean isChildSelectable(int headPosition, int childPosition) {
        return true;
    }
}

