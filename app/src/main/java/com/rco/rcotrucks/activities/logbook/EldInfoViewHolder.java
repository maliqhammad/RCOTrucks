package com.rco.rcotrucks.activities.logbook;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import com.rco.rcotrucks.R;

public class EldInfoViewHolder extends RecyclerView.ViewHolder {
    private TextView eldInfoLabel;
    private Context context;

    public EldInfoViewHolder(View v) {
        super(v);
        eldInfoLabel = (TextView) v.findViewById(R.id.info_data);
        context = v.getContext();
    }

    public void setData(String eldLabel, int position) {
        eldInfoLabel.setText(eldLabel);

        boolean isEldData = ((position / LogBookFragment.GRID_SPAN_COUNT) % 2) == 0;
        if (isEldData) {
//            eldInfoLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_eld_color));
            eldInfoLabel.setBackground(context.getResources().getDrawable(R.drawable.light_gray_and_dark_gray_background));
        } else {
            eldInfoLabel.setBackgroundColor(ContextCompat.getColor(context, R.color.white_and_black));
        }
    }
}
