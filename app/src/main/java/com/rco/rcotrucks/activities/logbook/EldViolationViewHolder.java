package com.rco.rcotrucks.activities.logbook;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;

public class EldViolationViewHolder extends RecyclerView.ViewHolder {
    private TextView violationLabel, violationDescription;

    public EldViolationViewHolder(View v) {
        super(v);
        violationLabel = (TextView) v.findViewById(R.id.violation_label);
        violationDescription = (TextView) v.findViewById(R.id.violation_description);
    }

    public void setData(Violation violation) {
        violationLabel.setText(violation.getViolationID());
        violationDescription.setText(violation.getViolationDesc());
    }
}
