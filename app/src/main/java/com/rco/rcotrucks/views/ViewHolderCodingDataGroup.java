package com.rco.rcotrucks.views;

import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.adapters.ListItemCodingDataGroup;

/**
 *
 */
public abstract class ViewHolderCodingDataGroup extends RecyclerView.ViewHolder {
    private int viewType;

    public int getViewType() {
        return viewType;
    }

    public void setViewType(int viewType) {
        this.viewType = viewType;
    }

    public ViewHolderCodingDataGroup(View itemView) {
        super(itemView);
    }

    public abstract void setItem(int ixItemList, ListItemCodingDataGroup listItemCodingDataRow);
}
