package com.rco.rcotrucks.activities;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.GlideApp;

public class GaugesFragment extends Fragment {

    private static final String TAG = GaugesFragment.class.getSimpleName();

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_gauges, container, false);
        ImageView gaugesDrivingBar = view.findViewById(R.id.gauges_driving_bar);
        ImageView gaugesBreakBar = view.findViewById(R.id.gauges_break_bar);
        ImageView gaugesShiftBar = view.findViewById(R.id.gauges_shift_bar);
        ImageView gaugesCycleBar = view.findViewById(R.id.gauges_cycle_bar);


        GlideApp.with(getContext())
                .load(getWidgetBitmapUpdate(getContext(), 70))
                .into(gaugesDrivingBar);

        GlideApp.with(getContext())
                .load(getWidgetBitmapUpdate(getContext(), 150))
                .into(gaugesBreakBar);

        GlideApp.with(getContext())
                .load(getWidgetBitmapUpdate(getContext(), 200))
                .into(gaugesShiftBar);

        GlideApp.with(getContext())
                .load(getWidgetBitmapUpdate(getContext(), 215))
                .into(gaugesCycleBar);


        return view;
    }

    //    Percentage values varies from 0 to 270
    private Bitmap getWidgetBitmapUpdate(Context context, int percentage) {
        Log.d(TAG, "getWidgetBitmap: ");
        int width = 400;
        int height = 400;
        int stroke = 30;
        int padding = 5;
        float density = context.getResources().getDisplayMetrics().density;

        //Paint for arc stroke.
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG | Paint.DITHER_FLAG | Paint.ANTI_ALIAS_FLAG);
        paint.setStrokeWidth(stroke);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeCap(Paint.Cap.ROUND);
        //paint.setStrokeJoin(Paint.Join.ROUND);
        //paint.setPathEffect(new CornerPathEffect(10) );
        //Paint for text values.
        Paint mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTextPaint.setTextSize((int) (context.getResources().getDimension(R.dimen.rco_trucks_xx_large_text_size) / density));
        mTextPaint.setColor(getResources().getColor(R.color.side_panel_remaining_progress_color));
        mTextPaint.setTextAlign(Paint.Align.CENTER);

        final RectF arc = new RectF();
        arc.set((stroke / 2) + padding, (stroke / 2) + padding, width - padding - (stroke / 2), height - padding - (stroke / 2));

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        //First draw full arc as background.
        paint.setColor(Color.argb(75, 255, 255, 255));
        canvas.drawArc(arc, 135, 270, false, paint);
        //Then draw arc progress with actual value.
        paint.setColor(getResources().getColor(R.color.side_panel_active_progress_color));

        canvas.drawArc(arc, 135, percentage, false, paint);

//Draw text value.
//        canvas.drawText(percentage + "%", bitmap.getWidth() / 2, (bitmap.getHeight() - mTextPaint.ascent()) / 2, mTextPaint);
//Draw widget title.
//        mTextPaint.setTextSize((int) (context.getResources().getDimension(R.dimen.rco_trucks_xxx_large_text_size) / density));
//        canvas.drawText("Driving", bitmap.getWidth() / 2, bitmap.getHeight()-(stroke+padding), mTextPaint);

        return bitmap;
    }

}