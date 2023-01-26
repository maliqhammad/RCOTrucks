package com.rco.rcotrucks.adapters;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

public class DiagnosticsListAdapter extends BaseAdapter {
    private static final String TAG = DiagnosticsListAdapter.class.getSimpleName();
    private ArrayList<EldEvent> items;
    ArrayList<EldEvent> diagnosticsListActive;
    private Context ctx;
    DiagnosticInterface diagnosticInterface;

    public DiagnosticsListAdapter(Context ctx, ArrayList<EldEvent> eldEvents, ArrayList<EldEvent> diagnosticsListActive, DiagnosticInterface diagnosticInterface) {
        this.ctx = ctx;
        this.items = eldEvents;
        this.diagnosticsListActive = diagnosticsListActive;
        this.diagnosticInterface = diagnosticInterface;
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
        final EldEvent eldEvent = items.get(i);
        final int index = i;

        LayoutInflater mInflator = LayoutInflater.from(ctx);
        v = mInflator.inflate(R.layout.item_diagnoctic_malfunction, null, false);
//        v = mInflator.inflate(R.layout.listitem_diagnostic, null, false);

        boolean isLogged = eldEvent.EventCode != null && eldEvent.EventCode.equalsIgnoreCase(BusinessRules.EventCode.DIAGNOSTIC_EVENT_LOGGED.getValue());

//        April 18, 2022    -   Auto = 1 And Manual = 2
        boolean isManual = eldEvent.RecordOrigin != null && eldEvent.RecordOrigin.equalsIgnoreCase("2") ? true : false;
//        boolean isManual = e.RecordOrigin != null && e.RecordOrigin.equalsIgnoreCase("MANUAL") ? true : false;

        TextView line1 = v.findViewById(R.id.line1);
        line1.setText("ID: " + eldEvent.SequenceId + ", USER: " + eldEvent.EldUsername + ", " + (isLogged ? "LOGGED" : "CLEARED") + ", " + (isManual ? "MANUAL" : "AUTO"));

        TextView line2 = v.findViewById(R.id.line2);
        line2.setText(eldEvent.CreationDate.substring(0, 19) + "\n" + eldEvent.MalfunctionDiagnosticDescp + ",\nMiles: " + eldEvent.VehicleMiles + ", Hours: " + eldEvent.EngineHours);

        TextView clearLink = v.findViewById(R.id.clear_button);
        clearLink.setVisibility(View.INVISIBLE);

        Log.d(TAG, "getView: position: " + index + " malfunctionsListActive.contains: " + isActiveEvent(eldEvent) + " isLogged: " + isLogged);
        if (isActiveEvent(eldEvent)) {
            if (isLogged) {
                clearLink.setVisibility(View.VISIBLE);
            }
        }
//            clearLink.setVisibility(isManual ? View.VISIBLE : View.INVISIBLE);
        clearLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    April 15, 2022    -   Need to clear the particular logged event
                diagnosticInterface.onItemClick(eldEvent);
            }
        });


        ImageView warningIcon = v.findViewById(R.id.warning_icon);
        warningIcon.setVisibility(View.VISIBLE);
        if (isLogged) {
            Picasso.with(ctx).load(R.drawable.ic_warning_sign).into(warningIcon);
        } else {
            Picasso.with(ctx).load(R.drawable.tick_icon).into(warningIcon);
        }
        return v;
    }

    public interface DiagnosticInterface {
        void onItemClick(EldEvent eldEvent);
    }

    boolean isActiveEvent(EldEvent eldEvent) {
        for (int i = 0; i < diagnosticsListActive.size(); i++) {
            if (eldEvent.SequenceId.equalsIgnoreCase(diagnosticsListActive.get(i).SequenceId)) {
                return true;
            }
        }
        return false;
    }

}
