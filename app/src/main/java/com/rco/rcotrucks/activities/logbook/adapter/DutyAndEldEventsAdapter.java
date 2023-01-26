package com.rco.rcotrucks.activities.logbook.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.logbook.model.GenericModel;
import com.rco.rcotrucks.businesslogic.BusinessRules;
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.businesslogic.transferrods.EldEventItem;
import com.rco.rcotrucks.utils.DateUtils;

import java.util.ArrayList;
import java.util.List;

public class DutyAndEldEventsAdapter extends RecyclerView.Adapter<DutyAndEldEventsAdapter.MyViewHolder> {

    private static final String TAG = DutyAndEldEventsAdapter.class.getSimpleName();
    ArrayList<EldEvent> list;
    Context context;
//    SelectionListener selectionListener;

    //    public DutyAndEldEventsAdapter(ArrayList<EldEventItem> list, Context context, SelectionListener selectionListener) {
    public DutyAndEldEventsAdapter(ArrayList<EldEvent> list, Context context) {

        this.list = list;
        this.context = context;
    }

    @NonNull
    @Override
    public DutyAndEldEventsAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_duty_and_eld_events, parent, false);
        return new DutyAndEldEventsAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final DutyAndEldEventsAdapter.MyViewHolder holder, final int position) {
        final EldEvent eldEvent = list.get(getItemViewType(position));

//        boolean isLogged = eldEvent.EventCode != null && eldEvent.EventCode.equalsIgnoreCase(BusinessRules.EventCode.DIAGNOSTIC_EVENT_LOGGED.getValue());
//        boolean isManual = eldEvent.RecordOrigin != null && eldEvent.RecordOrigin.equalsIgnoreCase("2") ? true : false;

        String engineHours = eldEvent.EngineHours;
        if (eldEvent.EngineHours == null) {
            engineHours = "0";
        }

        Log.d(TAG, "onBindViewHolder: date: "+DateUtils.getDateFromDateAndTime(eldEvent.CreationDate));
        holder.userRelevantDetail.setText("ID: " + eldEvent.SequenceId + ", USER: " + eldEvent.EldUsername + ", Time: " + eldEvent.CreationDate.substring(10, 19));
        holder.eventRelevantDetail.setText("Certification Recertification of records: (" + eldEvent.EventCode + ")\n" +
                "Event: " + eldEvent.EventCodeDescription + "\n" +
                "Engine Hours:" + engineHours + " Distance: \n" + eldEvent.LocalizationDescription);


//        Log.d(TAG, "onBindViewHolder: recordId: " + eldEvent.RecordId);
//        Log.d(TAG, "onBindViewHolder: objectId: " + eldEvent.objectId);
//        Log.d(TAG, "onBindViewHolder: objectType: " + eldEvent.objectType);
//        Log.d(TAG, "onBindViewHolder: trucklogsobjectId: " + eldEvent.trucklogsobjectId);
//        Log.d(TAG, "onBindViewHolder: trucklogsobjectType: " + eldEvent.trucklogsobjectType);
//        Log.d(TAG, "onBindViewHolder: eldlogsobjectId: " + eldEvent.eldlogsobjectId);
//        Log.d(TAG, "onBindViewHolder: eldlogsobjectType: " + eldEvent.eldlogsobjectType);
//        Log.d(TAG, "onBindViewHolder: MobileRecordId: " + eldEvent.MobileRecordId);
//        Log.d(TAG, "onBindViewHolder: RmsCodingTimestamp: " + eldEvent.RmsCodingTimestamp);
//        Log.d(TAG, "onBindViewHolder: RmsTimestamp: " + eldEvent.RmsTimestamp);
//        Log.d(TAG, "onBindViewHolder: Id: " + eldEvent.Id);
//        Log.d(TAG, "onBindViewHolder: EventType: " + eldEvent.EventType);
//        Log.d(TAG, "onBindViewHolder: Detail: " + eldEvent.Detail);
//        Log.d(TAG, "onBindViewHolder: OrganizationName: " + eldEvent.OrganizationName);
//        Log.d(TAG, "onBindViewHolder: OrganizationNumber: " + eldEvent.OrganizationNumber);
//        Log.d(TAG, "onBindViewHolder: EldUsername: " + eldEvent.EldUsername);
//        Log.d(TAG, "onBindViewHolder: EventCode: " + eldEvent.EventCode);
//        Log.d(TAG, "onBindViewHolder: RecordStatus: " + eldEvent.RecordStatus);
//        Log.d(TAG, "onBindViewHolder: RecordOrigin: " + eldEvent.RecordOrigin);
//        Log.d(TAG, "onBindViewHolder: TruckNumber: " + eldEvent.TruckNumber);
//        Log.d(TAG, "onBindViewHolder: Vin: " + eldEvent.Vin);
//        Log.d(TAG, "onBindViewHolder: LocalizationDescription: " + eldEvent.LocalizationDescription);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.LatitudeString);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.LongitudeString);
//        Log.d(TAG, "onBindViewHolder: DstSinceLastValidCoords: " + eldEvent.DstSinceLastValidCoords);
//        Log.d(TAG, "onBindViewHolder: VehicleMiles: " + eldEvent.VehicleMiles);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.EngineHours);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.OrderNumberCmv);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.OrderNumberUser);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.SequenceId);
//        Log.d(TAG, "onBindViewHolder: EventCodeDescription: " + eldEvent.EventCodeDescription);
//        Log.d(TAG, "onBindViewHolder: DiagnosticIndicator: " + eldEvent.DiagnosticIndicator);
//        Log.d(TAG, "onBindViewHolder: MalfunctionIndicator: " + eldEvent.MalfunctionIndicator);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.Annotation);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.RecordOriginId);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.CheckData);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.CheckSum);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.MalfunctionDiagnosticCode);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.MalfunctionDiagnosticDescp);
//        Log.d(TAG, "onBindViewHolder: DriverLastName: " + eldEvent.DriverLastName);
//        Log.d(TAG, "onBindViewHolder: DriverFirstName: " + eldEvent.DriverFirstName);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.DriverRecordId);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.EditReason);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.EventSeconds);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.ShiftStart);
//        Log.d(TAG, "onBindViewHolder: CreationDate: " + eldEvent.CreationDate);
//        Log.d(TAG, "onBindViewHolder: : " + eldEvent.Sent);
//        Log.d(TAG, "onBindViewHolder: Odometer: " + eldEvent.Odometer);
//        Log.d(TAG, "onBindViewHolder: : ");
//        Log.d(TAG, "onBindViewHolder: : ");
//        Log.d(TAG, "onBindViewHolder: : ");

        holder.openDetailIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        holder.informationIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });

        holder.dutyAndELDEventsItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return list.size();
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView informationIcon, openDetailIcon;
        TextView userRelevantDetail, eventRelevantDetail;
        ConstraintLayout dutyAndELDEventsItemLayout;

        public MyViewHolder(View itemView) {
            super(itemView);

            userRelevantDetail = itemView.findViewById(R.id.userRelevantDetail);
            eventRelevantDetail = itemView.findViewById(R.id.eventRelevantDetail);
            informationIcon = itemView.findViewById(R.id.informationIcon);
            openDetailIcon = itemView.findViewById(R.id.openDetailIcon);
            dutyAndELDEventsItemLayout = itemView.findViewById(R.id.dutyAndELDEventsItemLayout);
        }
    }

//    public interface SelectionListener {
//        void edit(GenericModel dataModel);
//
//        void delete(int position);
//    }

}
