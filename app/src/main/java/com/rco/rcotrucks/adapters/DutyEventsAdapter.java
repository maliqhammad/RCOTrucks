package com.rco.rcotrucks.adapters;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.DutyEvent;

import java.util.ArrayList;

public class DutyEventsAdapter extends BaseAdapter {
    private ArrayList<DutyEvent> items;
    private Context ctx;
    ItemClickInterface itemClickInterface;

    public DutyEventsAdapter(Context ctx, ArrayList<DutyEvent> dutyStatuses, ItemClickInterface itemClickInterface) {
        this.items = dutyStatuses;
        this.ctx = ctx;
        this.itemClickInterface = itemClickInterface;
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
        final DutyEvent dutyEvent = items.get(i);
        final int index = i;

        LayoutInflater mInflator = LayoutInflater.from(ctx);
        v = mInflator.inflate(R.layout.listitem_serverurl, null, false);

//        try {
        TextView field1 = v.findViewById(R.id.tv_url);
        ImageView image = v.findViewById(R.id.icon);
        RelativeLayout itemLayout = v.findViewById(R.id.item_layout);


        field1.setText(dutyEvent.getName());

//        field1.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                itemClickInterface.selectedDutyEvent(dutyEvent.getName());
//            }
//        });
//        } catch (Throwable throwable) {
//            if (throwable != null)
//                throwable.printStackTrace();
//        }

//        try {

        Log.d("DutyEventsAdapter: ", "getView: " + dutyEvent.isSelected());
        image.setVisibility(dutyEvent.isSelected() ? View.VISIBLE : View.INVISIBLE);
        image.setImageResource(R.drawable.ic_check);

        itemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                itemClickInterface.selectedDutyEvent(dutyEvent.getName());
            }
        });

//        } catch (Throwable throwable) {
//            if (throwable != null)
//                throwable.printStackTrace();
//        }

        return v;
    }

    public interface ItemClickInterface {
        public void selectedDutyEvent(String dutyEvent);
    }

}
