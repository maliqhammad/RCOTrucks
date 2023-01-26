package com.rco.rcotrucks.activities.forms;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.rms.Evaluation;

import java.util.List;

public class EvaluationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<Evaluation> formItems;
    private String evaluationType;

    public void filterList(List<Evaluation> filterdNames) {
        this.formItems = filterdNames;
        notifyDataSetChanged();
    }

    public EvaluationAdapter(List<Evaluation> eldViolationItems, String evaluationType) {
        this.formItems = eldViolationItems;
        this.evaluationType = evaluationType;
    }

    @Override
    public EvaluationViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_evaluation_form, parent, false);
        return new EvaluationViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        EvaluationViewHolder evaluationViewHolder = (EvaluationViewHolder) holder;
        evaluationViewHolder.setData(formItems.get(position), evaluationType);
    }

    @Override
    public int getItemCount() {
        return formItems.size();
    }

}
