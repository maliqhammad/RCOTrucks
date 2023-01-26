package com.rco.rcotrucks.adapters;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.DialogManager;
import com.rco.rcotrucks.businesslogic.Tuple;

import java.util.List;

public class MainNavigationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private List<Tuple<Integer, Integer>> menus;
    private ClickListener clickListener;

    public MainNavigationAdapter(List<Tuple<Integer, Integer>> menus,ClickListener clickListener) {
        this.menus = menus;
        this.clickListener = clickListener;
    }

    @Override
    public NavigationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_navigation, parent, false);
        return new NavigationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        try {
            NavigationViewHolder eldEventViewHolder = (NavigationViewHolder) holder;
            eldEventViewHolder.setData(menus.get(position));
        } catch (Throwable throwable) {
            if (throwable != null)
                throwable.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return menus.size();
    }

    public class NavigationViewHolder extends RecyclerView.ViewHolder {
        private TextView text;
        private ImageView img;

        public NavigationViewHolder(View v) {
            super(v);

            text = (TextView) v.findViewById(R.id.title);
            img = (ImageView) v.findViewById(R.id.icon);
        }

        public void setData(Tuple<Integer, Integer> menu) {
            text.setText(menu.getElement0());
            img.setImageResource(menu.getElement1());
            img.setTag(menu.getElement1());
            img.setColorFilter(Color.WHITE);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    clickListener.onItemClick(img);
                }
            });
        }
    }

    public interface ClickListener {
        void onItemClick(View view);
    }
}
