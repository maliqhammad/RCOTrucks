package com.rco.rcotrucks.activities.logbook;

import android.content.Context;
import android.content.Intent;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.adapters.Cadp;

public class EldEventViewHolder extends RecyclerView.ViewHolder {
    private ConstraintLayout eldEventLayout;
    private TextView time, location, odometer, engHours, eventType, origin;
    private Context context;

    public EldEventViewHolder(View v) {
        super(v);
        eldEventLayout = (ConstraintLayout) v.findViewById(R.id.eld_event_layout);
        time = (TextView) v.findViewById(R.id.time);
        location = (TextView) v.findViewById(R.id.location);
        odometer = (TextView) v.findViewById(R.id.odometer);
        engHours = (TextView) v.findViewById(R.id.eng_hours);
        eventType = (TextView) v.findViewById(R.id.event_type);
        origin = (TextView) v.findViewById(R.id.origin);
        context = v.getContext();
    }

    public void setData(final LogBookELDEvent logBookELDEvent, final int position) {

        time.setText(logBookELDEvent.getTime());
        String locationDescription = "";
        if (logBookELDEvent.getLocation() != null && !logBookELDEvent.getLocation().isEmpty())
            locationDescription = logBookELDEvent.getLocation();
        else
            locationDescription = logBookELDEvent.getAnnotation() != null && !logBookELDEvent.getAnnotation().isEmpty() ? "*" + logBookELDEvent.getAnnotation() : "";

        location.setText(locationDescription);
        odometer.setText(logBookELDEvent.getOdometer());
        engHours.setText(logBookELDEvent.getEngHours());
        eventType.setText(logBookELDEvent.getEventTypeDescription());
        origin.setText(logBookELDEvent.getOrigin());

        boolean isPairPosition = (position % 2) == 0;
        if (position == 0) {
            eldEventLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_header_event_color));
            setColor(context.getColor(R.color.black));
        } else if (isPairPosition) {
            eldEventLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.bg_eld_color));
            setColor(context.getColor(R.color.black_and_white));
        } else {
            eldEventLayout.setBackgroundColor(ContextCompat.getColor(context, R.color.white_and_black));
            setColor(context.getColor(R.color.black_and_white));
        }
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(position != 0){
                    Intent intent = new Intent(itemView.getContext(), EldEventDetailActivity.class);
                    intent.putExtra(Cadp.EXTRA_EVENT_ID, logBookELDEvent.getId());
                    itemView.getContext().startActivity(intent);
                }
            }
        });
    }

    void setColor(int color) {
        time.setTextColor(color);
        location.setTextColor(color);
        odometer.setTextColor(color);
        engHours.setTextColor(color);
        eventType.setTextColor(color);
        origin.setTextColor(color);
    }
}
