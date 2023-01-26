package com.rco.rcotrucks.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;

public class DrawView extends View {
    private Paint paint;
//        private Bitmap bitmap;
//        private Canvas canvas;
    private Path path;

    public DrawView(Context context) {
        super(context);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DrawView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }


    public void init(Paint paint, Path path)
    {
        this.paint = paint;
        this.path = path;

//            bitmap = Bitmap.createBitmap(820, 480, Bitmap.Config.ARGB_4444);
//            bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_4444);
//
//            canvas = new Canvas(bitmap);

        this.setBackgroundColor(Color.WHITE);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retval = true;

        float x = event.getX();
        float y = event.getY();

        switch (event.getAction())
        {
            case MotionEvent.ACTION_DOWN :
                path.moveTo(x, y);
                retval = true;
                break;
            case MotionEvent.ACTION_MOVE :
                path.lineTo(x, y);
                invalidate();
                retval = false;
                break;
            default :
                retval = false;
        }
        return retval;
    }

    public Bitmap getBitmap()
    {
        return getBitmapFromView(this);
    }

    public static Bitmap getBitmapFromView(View view)
    {
        int iscale = 8;
        float fscale = 1.0f/iscale;
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth() / iscale, view.getHeight() / iscale, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(fscale, fscale);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
//            super.onDraw(canvas);
        canvas.drawPath(path, paint);
    }
}
