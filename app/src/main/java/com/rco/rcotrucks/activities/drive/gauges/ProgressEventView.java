package com.rco.rcotrucks.activities.drive.gauges;

import android.app.Activity;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.AsyncTask;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.activities.MainMenuActivity;
import com.rco.rcotrucks.activities.drive.DriveFragmentBase;
import com.rco.rcotrucks.utils.DateUtils;
import com.rco.rcotrucks.utils.StringUtils;

public class ProgressEventView extends RelativeLayout {

    private static final String TAG = ProgressEventView.class.getSimpleName();

    public enum ProgressType {DRIVING, BREAKS, SHIFT, CYCLE}

    public static final int DAY_HOURS = 60 * 24;
    public static final int BREAKS_MAX_HOURS = 60 * 8;
    private static final int MAX_MILES = 800;//if you have more than 850 miles you are speeding (average is 650 miles per day)
    private static final int MAX_DRIVING_FOR_SINGLE_SHIFT = 60 * 11;//driving past 11 hours in a single shift
    private static final int MAX_DRIVING_WITHOUT_OFF_DUTY = 60 * 8;//after going off duty for shift and into sleeper berth and then
    //starting to drive without taking 8 hours in sleeper or off duty
    private static final int MAX_SHIFT_ON_DUTY = 60 * 14;//On duty for more than 14 hours
    private static final int MAX_DRIVING_AFTER_BREAKS = 60 * 8;//driving past 8 hours without taking a 30 minute break
    private static final int MAX_CYCLE_TIME = 60 * 112;//Cycle Time
    private static final int MAX_DRIVING_BEFORE_RESTING = 60 * 34;//driving before the 34 hours of resting after ending a cycle
    private int limit = 0;

    Activity mActivity;
    private TextView txtEventTitle, txtMaxLimit;
    private ProgressBar progressView;
    private ProgressType progressEventType;

    public ProgressEventView(Context context) {
        super(context);
    }

    //    public ProgressEventView(Context context, @Nullable AttributeSet attrs, Activity activity) {
    public ProgressEventView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        initView(context, attrs);
//        initView(context, attrs, activity);
    }

    private void initView(Context context, AttributeSet attrs) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View container = inflater.inflate(R.layout.layout_progress_event, this, true);

        txtEventTitle = container.findViewById(R.id.txt_event_title);
        txtMaxLimit = container.findViewById(R.id.txt_max_limit);
        progressView = container.findViewById(R.id.progress_view);

//        if (mapAutoUpdateTask == null) {
//            mapAutoUpdateTask = new ProgressEventView.MapAutoUpdateTask();
//            mapAutoUpdateTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
//        }

    }

    public void init(String title, int limit, ProgressType progressType) {
        this.limit = limit;
        txtEventTitle.setText(title);
        txtMaxLimit.setText(limit + " hrs");
        this.progressEventType = progressType;
    }

    private void setEventProgress(String title, int progress) {
        txtEventTitle.setText(title);
        progressView.setProgress(progress);
    }

    public void handleEventProgress(long timeInMinutes) {
        if (progressEventType == null)
            return;

        String eventHour = DateUtils.convertMinutesToHours(timeInMinutes);
        switch (progressEventType) {
            case DRIVING:
                int drivingProgress = (int) ((timeInMinutes * 100) / MAX_DRIVING_FOR_SINGLE_SHIFT);
                setEventProgress(getContext().getString(R.string.text_drive, eventHour), drivingProgress);
                if (timeInMinutes > MAX_DRIVING_FOR_SINGLE_SHIFT) {
                    setWarningProgress();
                }
                break;
            case BREAKS:
                int breaksProgress = (int) ((timeInMinutes * 100) / MAX_DRIVING_AFTER_BREAKS);
                setEventProgress(getContext().getString(R.string.text_break, eventHour), breaksProgress);
                if (timeInMinutes > MAX_DRIVING_AFTER_BREAKS) {
                    setWarningProgress();
                }
                break;
            case SHIFT:
                int shiftProgress = (int) ((timeInMinutes * 100) / MAX_SHIFT_ON_DUTY);
                setEventProgress(getContext().getString(R.string.text_shift, eventHour), shiftProgress);
                if (timeInMinutes > MAX_SHIFT_ON_DUTY) {
                    setWarningProgress();
                }
                break;
            case CYCLE:
                Log.d(TAG, "handleEventProgress: timeInMinutes: " + timeInMinutes + " eventHour: " + eventHour);
//                int cycleProgress = (int) ((timeInMinutes * 100) / MAX_CYCLE_TIME);
                int cycleProgress;
                Log.d(TAG, "handleEventProgress: limit: " + limit);
                if (limit > 0) {
                    cycleProgress = (int) ((timeInMinutes * 100) / (limit * 60));
                } else {
                    cycleProgress = (int) ((timeInMinutes * 100) / MAX_CYCLE_TIME);
                }
                Log.d(TAG, "handleEventProgress: cycleProgress: " + cycleProgress);
                setEventProgress(getContext().getString(R.string.text_cycle, eventHour), cycleProgress);

//                if (timeInMinutes > MAX_DRIVING_BEFORE_RESTING) {
//                    setWarningProgress();
//                }
                if (timeInMinutes > MAX_CYCLE_TIME) {
                    setWarningProgress();
                }
                break;
        }
    }

    public void setWarningProgress() {
        progressView.setProgressTintList(ColorStateList.valueOf(ContextCompat.getColor(getContext(),
                android.R.color.holo_red_light)));
    }

    public void setLimit(int limit) {
        this.limit = limit;
        txtMaxLimit.setText(limit + " hrs");
    }

}
