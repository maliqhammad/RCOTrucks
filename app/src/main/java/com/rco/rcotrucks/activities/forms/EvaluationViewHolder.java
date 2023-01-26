package com.rco.rcotrucks.activities.forms;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.businesslogic.rms.Evaluation;
import com.rco.rcotrucks.utils.DateUtils;

public class EvaluationViewHolder extends RecyclerView.ViewHolder {
    private TextView skillLabel ;

    public EvaluationViewHolder(View v) {
        super(v);
        skillLabel = (TextView) v.findViewById(R.id.tv_title);
    }

    public void setData(final Evaluation evaluation,final String evaluationType) {

        String evaTitle = "";
        if(evaluation.Driver_First_Name != null && !evaluation.Driver_First_Name.isEmpty()){
            evaTitle = evaluation.Driver_First_Name;
        }else if(evaluation.lastEmployerName != null && !evaluation.lastEmployerName.isEmpty()){
            evaTitle = evaluation.lastEmployerName;
        }else if(evaluation.physicianfirstname != null && !evaluation.physicianfirstname.isEmpty()){
            evaTitle = evaluation.physicianfirstname;
        }else if(evaluation.companyName != null && !evaluation.companyName.isEmpty()){
            evaTitle = evaluation.companyName;
        }else if(evaluation.driverName_0 != null && !evaluation.driverName_0.isEmpty()){
            evaTitle = evaluation.driverName_0;
        }
        evaTitle += " "+ DateUtils.convertDateTime(evaluation.Creation_Date, DateUtils.FORMAT_DATE_TIME_MILLIS, DateUtils.FORMAT_DATE_MM_DD_YYYY);
        skillLabel.setText(evaTitle);

    }
}
