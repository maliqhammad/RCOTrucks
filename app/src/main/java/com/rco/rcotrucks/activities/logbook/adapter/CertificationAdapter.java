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
import com.rco.rcotrucks.businesslogic.EldEvent;
import com.rco.rcotrucks.utils.DateUtils;

import java.util.List;

public class CertificationAdapter extends RecyclerView.Adapter<CertificationAdapter.MyViewHolder> {

    private static final String TAG = CertificationAdapter.class.getSimpleName();
    List<EldEvent> list;
    Context context;
//    SelectionListener selectionListener;

    //    public CertificationAdapter(List<EldEvent> list, Context context, SelectionListener selectionListener) {
    public CertificationAdapter(List<EldEvent> list, Context context) {

        this.list = list;
        this.context = context;
//        this.selectionListener = selectionListener;
    }

    @NonNull
    @Override
    public CertificationAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_certification, parent, false);
        return new CertificationAdapter.MyViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final CertificationAdapter.MyViewHolder holder, final int position) {
        final EldEvent dataModel = list.get(getItemViewType(position));

        holder.date.setText(DateUtils.getMonthDayYearDateFromDateAndTime(dataModel.CreationDate));
        holder.certificationDetail.setText((position + 1) + ". Certify Date: " + DateUtils.removeMilliSecondsFromDateAndTime(dataModel.CreationDate));

        holder.certificationItemLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                selectionListener.edit(dataModel);
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

        TextView date, certificationDetail;
        ConstraintLayout certificationItemLayout;

        public MyViewHolder(View itemView) {
            super(itemView);

            date = itemView.findViewById(R.id.date);
            certificationDetail = itemView.findViewById(R.id.certificationDetail);
            certificationItemLayout = itemView.findViewById(R.id.certificationItemLayout);
        }
    }

//    public interface SelectionListener {
//        void edit(EldEvent dataModel);
//
//        void delete(int position);
//    }

}
