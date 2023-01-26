package com.rco.rcotrucks.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rco.rcotrucks.businesslogic.ServerUrl;

import java.util.ArrayList;
import com.rco.rcotrucks.R;

public class ServersListAdapter extends BaseAdapter {
    private ArrayList<ServerUrl> items;
    private Context ctx;

    public ServersListAdapter(Context ctx, ArrayList<ServerUrl> urls) {
        this.items = urls;
        this.ctx = ctx;
    }

    public int getCount() {
        return items == null ? 0 : items.size();
    }

    public Object getItem(int i) {
        return items.get(i);
    }

    public long getItemId(int i) {
        return i;
    }

    public View getView(int i, View v, ViewGroup viewGroup) {
        ServerUrl url = items.get(i);

        LayoutInflater mInflator = LayoutInflater.from(ctx);
        v = mInflator.inflate(R.layout.listitem_serverurl, null, false);

        try {
            TextView field1 = v.findViewById(R.id.tv_url);
            field1.setText(url.getUrl());
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        try {
            ImageView image = v.findViewById(R.id.icon);

            image.setVisibility(url.isSelected() ? View.VISIBLE : View.INVISIBLE);
            image.setImageResource(R.drawable.ic_check);
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }

        return v;
    }
}
