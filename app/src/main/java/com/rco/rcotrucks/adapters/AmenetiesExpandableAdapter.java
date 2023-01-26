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
import com.rco.rcotrucks.model.AmenitiesGenericModel;
import com.rco.rcotrucks.model.MapAssetModel;
import com.rco.rcotrucks.model.MapAssetSubcategoriesModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AmenetiesExpandableAdapter extends BaseExpandableListAdapter {

    private static final String TAG = MapAssetExpandableAdapter.class.getSimpleName();
    private List<AmenitiesGenericModel> headerData;
    private HashMap<String, ArrayList<MapAssetSubcategoriesModel>> childData;
    private Context mContext;
    private LayoutInflater layoutInflater;

    public AmenetiesExpandableAdapter(Context mContext, List<AmenitiesGenericModel> headerData,
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

            AmenitiesGenericModel mAmenitiesGenericModel = (AmenitiesGenericModel) getGroup(headPosition);
            if (view == null) {
                view = layoutInflater.inflate(R.layout.item_ameneties, null);
            }

            ConstraintLayout itemLayout = view.findViewById(R.id.item_map_asset);
            TextView amenityTitle = view.findViewById(R.id.amenity_title);
            ImageView amenityIcon = view.findViewById(R.id.amenity_image);

            amenityTitle.setText(mAmenitiesGenericModel.getName());
            int iconId = mContext.getResources().getIdentifier(mAmenitiesGenericModel.getImageUrl().toLowerCase(),"drawable", mContext.getPackageName());
            setIcon(iconId, amenityIcon);

            itemLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

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
            view = layoutInflater.inflate(R.layout.item_subcategories_ameneties, null);
        }

        ConstraintLayout constraintLayout = view.findViewById(R.id.item_subcategory_amenity);
        ImageView mapAssetImage = view.findViewById(R.id.subcategory_image);
        TextView mapAssetTitle = view.findViewById(R.id.subcategory_title);

        if (mapAssetTitle != null) {
            mapAssetTitle.setText(child.getSubCategoryName());
            setIcon(child.getMapAssetId(), mapAssetImage);
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

    void setIcon(int icon, ImageView imageView) {
        Glide.with(mContext).load(icon).into(imageView);
    }

    @Override
    public boolean hasStableIds() {
        Log.d(TAG, "hasStableIds: ");
        return false;
    }


}