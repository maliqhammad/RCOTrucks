package com.rco.rcotrucks.adapters;

import android.content.Context;
import android.opengl.Visibility;
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

public class MalfunctioListAdapter extends BaseAdapter {
    private static final String TAG = MalfunctioListAdapter.class.getSimpleName();
    private ArrayList<EldEvent> items;
    ArrayList<EldEvent> malfunctionsListActive;
    private Context ctx;
    MalfunctionInterface malfunctionInterface;

    public MalfunctioListAdapter(Context ctx, ArrayList<EldEvent> eldEvents, ArrayList<EldEvent> malfunctionsListActive, MalfunctionInterface malfunctionInterface) {
        this.ctx = ctx;
        this.items = eldEvents;
        this.malfunctionsListActive = malfunctionsListActive;
        this.malfunctionInterface = malfunctionInterface;
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
//        v = mInflator.inflate(R.layout.listitem_diagnostic, null, false);
        v = mInflator.inflate(R.layout.item_diagnoctic_malfunction, null, false);

        boolean isLogged = eldEvent.EventCode != null && eldEvent.EventCode.equalsIgnoreCase(BusinessRules.EventCode.ELD_MALFUNCTION_LOGGED.getValue());

//        April 18, 2022    -   Auto = 1 And Manual = 2
        boolean isManual = eldEvent.RecordOrigin != null && eldEvent.RecordOrigin.equalsIgnoreCase("2") ? true : false;
//        boolean isManual = eldEvent.RecordOrigin != null && eldEvent.RecordOrigin.equalsIgnoreCase("MANUAL") ? true : false;

        TextView line1, line2, clearLink;

        line1 = v.findViewById(R.id.line1);
        ImageView warningSign = v.findViewById(R.id.warning_icon);
        line2 = v.findViewById(R.id.line2);
        clearLink = v.findViewById(R.id.clear_button);

        line1.setText("ID: " + eldEvent.SequenceId + ", USER: " + eldEvent.EldUsername + ", " + (isLogged ? "LOGGED" : "CLEARED") + ", " + (isManual ? "MANUAL" : "AUTO"));

//        April 18, 2022    -   Should we show the engine parameters here also?
//        line2.setText(eldEvent.CreationDate.substring(0, 19) + "\n" + eldEvent.MalfunctionDiagnosticDescp + "\nMiles: " + eldEvent.VehicleMiles + ", Hours: " + eldEvent.EngineHours);
        line2.setText(eldEvent.CreationDate.substring(0, 19) + "\n" + eldEvent.MalfunctionDiagnosticDescp);

        clearLink.setVisibility(View.INVISIBLE);
        if (isActiveEvent(eldEvent)) {
            if (isLogged) {
                clearLink.setVisibility(View.VISIBLE);
            }
        }
        clearLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                    April 15, 2022    -   Need to clear the particular logged event
                malfunctionInterface.onItemClick(eldEvent);
            }
        });

        warningSign.setVisibility(View.VISIBLE);
        if (isLogged) {
            Picasso.with(ctx).load(R.drawable.warning_malfunction_icon).into(warningSign);
        } else {
            Picasso.with(ctx).load(R.drawable.tick_icon).into(warningSign);
        }

        return v;
    }

    public interface MalfunctionInterface {
        void onItemClick(EldEvent eldEvent);
    }

    boolean isActiveEvent(EldEvent eldEvent) {
        for (int i = 0; i < malfunctionsListActive.size(); i++) {
            if (eldEvent.SequenceId.equalsIgnoreCase(malfunctionsListActive.get(i).SequenceId)) {
                return true;
            }
        }
        return false;
    }
}
