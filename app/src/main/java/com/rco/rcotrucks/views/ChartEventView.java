package com.rco.rcotrucks.views;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import com.rco.rcotrucks.activities.logbook.ChartEvent;
import com.rco.rcotrucks.activities.logbook.LogBookELDEvent;
import com.rco.rcotrucks.utils.DateUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ChartEventView extends View {

    private static final String TAG = ChartEventView.class.getSimpleName();
    public static final int HOURS_BY_DAY = 24;
    public static final int EVENT_CODE_SECTION = 4;
    public static final int MINUTES_BY_HOUR = 60;

    public static final int MAX_ON_DUTY_HOUR = 11;
    public static final int MAX_DRIVING_HOUR = 8;
    public static final int MAX_ON_DUTY_HOURS_WITHOUT_SB = 14; //SB = SLEEPER BIRTH
    public static final int MIN_SLEEPER_BIRTH_HOUR = 7;
    public static final int MIN_SLEEPER_BIRTH_AFTER_OD = 10; //OD ON DUTY

    public static final List validateEventCode = Arrays.asList("1", "2", "3", "4");


    DecimalFormat decimalFormat = new DecimalFormat("#.##");
    private ArrayList<ChartEvent> chartEventData = new ArrayList<>();

    private String eldEventDate;

    private int ofTotalHours, slTotalHours, drTotalHours, onTotalHours;
    private int continuousSleeperBirthTime, continuousDrivingTime;

    private IEventPeriod iEventPeriod;

    private Boolean isOnDutyStarted = false;

    public ChartEventView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        drawBorder(canvas);

    }

    private void drawBorder(Canvas canvas) {
        if (canvas == null)
            return;

        Paint borderPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(2f);

        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();

        Paint chartBG = new Paint();
        chartBG.setColor(Color.rgb(129, 191, 220));
        chartBG.setStyle(Paint.Style.FILL);
        canvas.drawRect(0f, 0f, canvasWidth, canvasHeight / 4, chartBG);

        chartBG.setColor(Color.rgb(0, 124, 186));
        canvas.drawRect(0f, canvasHeight / 4, canvasWidth, canvasHeight / 2, chartBG);

        chartBG.setColor(Color.rgb(255, 175, 84));
        canvas.drawRect(0f, canvasHeight / 2, canvasWidth, 3 * canvasHeight / 4, chartBG);

        chartBG.setColor(Color.rgb(253, 216, 165));
        canvas.drawRect(0f, 3 * canvasHeight / 4, canvasWidth, canvasHeight, chartBG);

        canvas.drawLine(0, 0, 0, canvasHeight, borderPaint);
        canvas.drawLine(0, canvasHeight, canvasWidth, canvasHeight, borderPaint);
        canvas.drawLine(0, 0, canvasWidth, 0, borderPaint);
        canvas.drawLine(canvasWidth, 0, canvasWidth, canvasHeight, borderPaint);

        Paint sectionPaint = new Paint();
        borderPaint.setColor(Color.BLACK);
        borderPaint.setStrokeWidth(1f);

        canvas.drawLine(0, canvasHeight / 2, canvasWidth, canvasHeight / 2, sectionPaint);
        canvas.drawLine(0, canvasHeight / 4, canvasWidth, canvasHeight / 4, sectionPaint);
        canvas.drawLine(0, 3 * canvasHeight / 4, canvasWidth, 3 * canvasHeight / 4, sectionPaint);

        float oneHourWidth = (float) canvasWidth / HOURS_BY_DAY;
        float oneHourHeight = (float) canvasHeight / EVENT_CODE_SECTION;

        for (int pos = 1; pos <= HOURS_BY_DAY; pos++) {
            //draw vertical lines
            canvas.drawLine(pos * oneHourWidth, 0, pos * oneHourWidth, canvasHeight, borderPaint);

            for (int line = 0; line < EVENT_CODE_SECTION; line++) {
                float yLine = line * oneHourHeight;
                // draw middle hour
                canvas.drawLine(pos * oneHourWidth - oneHourWidth / 2, yLine + oneHourHeight / 4, pos * oneHourWidth - oneHourWidth / 2, yLine + oneHourHeight, borderPaint);

                // draw first quarter hour
                float firstQuarterHourStartX = pos * oneHourWidth - oneHourWidth / 2 - oneHourWidth / 4;
                canvas.drawLine(firstQuarterHourStartX, yLine + oneHourHeight / 2, firstQuarterHourStartX, yLine + oneHourHeight, borderPaint);

                // draw second quarter hour
                float secondQuarterHourStartX = pos * oneHourWidth - oneHourWidth / 2 + oneHourWidth / 4;
                canvas.drawLine(secondQuarterHourStartX, yLine + oneHourHeight / 2, secondQuarterHourStartX, yLine + oneHourHeight, borderPaint);
            }
        }

        drawTimeLines(canvas, canvasHeight, canvasWidth);

        // invalidate();
    }

    private void drawTimeLines(Canvas canvas, int canvasHeight, int canvasWidth) {
        if (chartEventData.size() == 0)
            return;

        ofTotalHours = 0;
        slTotalHours = 0;
        drTotalHours = 0;
        onTotalHours = 0;
        continuousSleeperBirthTime = 0;
        continuousDrivingTime = 0;

        Paint timePaint = new Paint();
        timePaint.setColor(Color.BLACK);
        timePaint.setStrokeWidth(2.5f);

        Paint circlePaint = new Paint();
        circlePaint.setColor(Color.RED);
        circlePaint.setStyle(Paint.Style.STROKE);
        circlePaint.setStrokeWidth(4f);


        Paint textPaint = new Paint();
        textPaint.setTextSize(30);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        textPaint.setColor(Color.RED);

        float oneHourHeight = canvasHeight / EVENT_CODE_SECTION;

        for (ChartEvent chartEvent : chartEventData) {
            if (chartEvent.getEventCode() != null) {
                int nbrMinutes = chartEvent.getStartTimeInMinutes();

                float xStartPoint = (nbrMinutes * canvasWidth) / (MINUTES_BY_HOUR * HOURS_BY_DAY);
                float yStartPoint = 0f;
                switch (chartEvent.getEventCode()) {
                    case "1":
                        yStartPoint = oneHourHeight / 2;
                        ofTotalHours += chartEvent.getPeriodInMinutes();
                        break;
                    case "2":
                        yStartPoint = oneHourHeight + oneHourHeight / 2;
                        slTotalHours += chartEvent.getPeriodInMinutes();
                        if (continuousSleeperBirthTime < chartEvent.getPeriodInMinutes()) {
                            continuousSleeperBirthTime = chartEvent.getPeriodInMinutes();
                        }
                        break;
                    case "3":
                        yStartPoint = 2 * oneHourHeight + oneHourHeight / 2;
                        drTotalHours += chartEvent.getPeriodInMinutes();
                        if (continuousDrivingTime < chartEvent.getPeriodInMinutes()) {
                            continuousDrivingTime = chartEvent.getPeriodInMinutes();
                        }
                        break;
                    case "4":
                        yStartPoint = 3 * oneHourHeight + oneHourHeight / 2;
                        onTotalHours += chartEvent.getPeriodInMinutes();
                        break;
                }

                canvas.drawCircle(xStartPoint, yStartPoint, 6, circlePaint);

                int endTimeInMinutes = chartEvent.getEndTimeInMinutes();
                float stopXPoint = (endTimeInMinutes * canvasWidth) / (MINUTES_BY_HOUR * HOURS_BY_DAY);
                canvas.drawLine(xStartPoint, yStartPoint, stopXPoint, yStartPoint, timePaint);

//                Aug 03, 2022  -   If the period time is zero, don't show 0 on graph
                String periodTime = getPeriodTime(chartEvent.getPeriodInMinutes());
                Log.d(TAG, "drawTimeLines: periodTime: " + periodTime);
                if (periodTime.equalsIgnoreCase("0")) {
                    periodTime = "";
                }
                canvas.drawText(periodTime, xStartPoint + ((stopXPoint - xStartPoint) / 2) - 30, yStartPoint - 15, textPaint);

                if (chartEvent.getPreviousEventCode() != null) {

                    float yEndPoint = 0f;
                    switch (chartEvent.getPreviousEventCode()) {
                        case "1":
                            yEndPoint = oneHourHeight / 2;
                            break;
                        case "2":
                            yEndPoint = oneHourHeight + oneHourHeight / 2;
                            break;
                        case "3":
                            yEndPoint = 2 * oneHourHeight + oneHourHeight / 2;
                            break;
                        case "4":
                            yEndPoint = 3 * oneHourHeight + oneHourHeight / 2;
                            break;
                    }

                    canvas.drawLine(xStartPoint, yStartPoint, xStartPoint, yEndPoint, timePaint);
                }
            }
        }

        if (iEventPeriod != null) {
            iEventPeriod.onUpdateOFEvent(getPeriodTime(ofTotalHours));
            iEventPeriod.onUpdateSLEvent(getPeriodTime(slTotalHours));
            iEventPeriod.onUpdateDREvent(getPeriodTime(drTotalHours));
            iEventPeriod.onUpdateONEvent(getPeriodTime(onTotalHours));

            if (isOnDutyStarted) {
                if (drTotalHours + onTotalHours > MAX_ON_DUTY_HOUR * MINUTES_BY_HOUR) {
                    iEventPeriod.isON11HoursViolation();

                }
                if (continuousDrivingTime >= MAX_DRIVING_HOUR * MINUTES_BY_HOUR) {
                    iEventPeriod.isON8HoursViolation();
                }

                if (drTotalHours + onTotalHours > MAX_ON_DUTY_HOURS_WITHOUT_SB * MINUTES_BY_HOUR) {
                    iEventPeriod.isON14hoursViolation();
                }
                if (drTotalHours + onTotalHours > MAX_ON_DUTY_HOUR * MINUTES_BY_HOUR) {
                    if (continuousSleeperBirthTime < MIN_SLEEPER_BIRTH_HOUR * MINUTES_BY_HOUR) {
                        iEventPeriod.isNotSB7ContinuousHoursViolation();
                    }
                    if (slTotalHours < MIN_SLEEPER_BIRTH_HOUR * MINUTES_BY_HOUR) {
                        iEventPeriod.isNotSB10TotalHoursViolation();
                    }
                }
            }
        }
        iEventPeriod = null;
    }

    public void setEldEventDate(String eldEventDate) {
        this.eldEventDate = eldEventDate;
    }

    public void setListOfEvents(LogBookELDEvent previousDayELDEvent, List<LogBookELDEvent> logBookELDEvents) {
        Log.d(TAG, "setListOfEvents: ");
        chartEventData = new ArrayList<>();
        isOnDutyStarted = false;

        List<LogBookELDEvent> logBookDistinctList = new ArrayList<>();
        for (int i = 0; i < logBookELDEvents.size(); i++) {
            Log.d(TAG, "setListOfEvents: logBookELDEvents.get(i).getEventType(): " + logBookELDEvents.get(i).getEventType());
            //Remove item have event type  != 1
            if (logBookELDEvents.get(i).getEventType().equals("1") && validateEventCode.contains(logBookELDEvents.get(i).getEventCode())) {

                logBookDistinctList.add(logBookELDEvents.get(i));
            }
        }

        if (previousDayELDEvent != null && logBookDistinctList.size() > 0) {
            ChartEvent firstChartEvent = generateChartEvent(previousDayELDEvent, logBookDistinctList.get(0), true);

            chartEventData.add(firstChartEvent);
        } else if (previousDayELDEvent != null) {
            ChartEvent firstChartEvent = generateChartEvent(previousDayELDEvent, null, true);
            Log.d(TAG, "setListOfEvents: firstChartEvent: " + firstChartEvent);
            chartEventData.add(firstChartEvent);
        }

        for (int i = 0; i < logBookDistinctList.size(); i++) {
            ChartEvent chartEvent;

            if (logBookDistinctList.get(i).getShiftstart().equals("1") && !isOnDutyStarted) {
                isOnDutyStarted = true;
            }

            if (i + 1 < logBookDistinctList.size()) {

                chartEvent = generateChartEvent(logBookDistinctList.get(i), logBookDistinctList.get(i + 1), false);
            } else {
                chartEvent = generateChartEvent(logBookDistinctList.get(i), null, false);
            }
            if (chartEventData.size() > 0)
                chartEvent.setPreviousEventCode(chartEventData.get(chartEventData.size() - 1).getEventCode());

            Log.d(TAG, "setListOfEvents: chartEvent: " + chartEvent);
            chartEventData.add(chartEvent);
        }
    }

    private ChartEvent generateChartEvent(LogBookELDEvent previousELDEvent, LogBookELDEvent currentEldEvent, boolean isPreviousEvent) {
        ChartEvent chartEvent = new ChartEvent();
        int endTimeInMinutes = MINUTES_BY_HOUR * HOURS_BY_DAY;

        if (eldEventDate != null && DateUtils.isToday(eldEventDate, DateUtils.FORMAT_DATE_YYYY_MM_DD)) {
            String times = DateUtils.getCurrentTime();
            endTimeInMinutes = getTimeInMinutes(times);
        }
        if (currentEldEvent != null) {
            String nextEventTime = currentEldEvent.getTime();
            endTimeInMinutes = getTimeInMinutes(nextEventTime);

            chartEvent.setEventCode(currentEldEvent.getEventCode());
        }

        int startTimeInMinutes = isPreviousEvent ? 0 : getTimeInMinutes(previousELDEvent.getTime());
        Log.d(TAG, "generateChartEvent: startTimeInMinutes: " + startTimeInMinutes);

        chartEvent.setStartTimeInMinutes(startTimeInMinutes);
        chartEvent.setEndTimeInMinutes(endTimeInMinutes);
        chartEvent.setPeriodInMinutes(endTimeInMinutes - startTimeInMinutes);
        chartEvent.setEventCode(previousELDEvent.getEventCode());

        Log.d(TAG, "generateChartEvent: chartEvent: " + chartEvent);
        return chartEvent;
    }

    private int getTimeInMinutes(String time) {
        if (time == null)
            return 0;
//        Log.d(TAG, "displayEventChart: getTimeInMinutes: time: " + time);
        int hours = (Integer.parseInt(time.substring(0, 2)));
//        June 16, 2022 -   We should round the minutes up if it is more than 7 minutes else we are rounding it down
//        int minutes = Integer.parseInt(time.substring(3, 5)) / 15;
        int mints = Integer.parseInt(time.substring(3, 5));
        int minutes = mints / 15;

        if (mints % 15 > 7) {
            minutes = minutes + 1;
        }
        if (minutes == 4) {
            hours = hours + 1;
            minutes = 0;
        }

        Log.d(TAG, "displayEventChart: getTimeInMinutes: hours: " + hours + " minutes: " + minutes + " multiplication: " + minutes * 15 + hours * MINUTES_BY_HOUR);
        return minutes * 15 + hours * MINUTES_BY_HOUR;
    }

    public void setPeriodsListener(IEventPeriod iEventPeriod) {
        this.iEventPeriod = iEventPeriod;
    }

    private String getPeriodTime(int timeInMinutes) {
        return decimalFormat.format((float) timeInMinutes / 60);
    }

    public interface IEventPeriod {
        void onUpdateOFEvent(String of);

        void onUpdateSLEvent(String sl);

        void onUpdateDREvent(String dr);

        void onUpdateONEvent(String on);

        void isON11HoursViolation();

        void isON8HoursViolation();

        void isON14hoursViolation();

        void isNotSB7ContinuousHoursViolation();

        void isNotSB10TotalHoursViolation();
    }
}

