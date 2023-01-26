package com.rco.rcotrucks.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;

import com.rco.rcotrucks.utils.UiUtils;

import java.io.ByteArrayOutputStream;
/**
 * This class supports displaying an initial bitmap, and if the user clears it,
 * drawing a signature.  The drawing can then be saved as a bitmap with reduced resolution.
 * If the user clears the initial bitmap, a new one is not created until a save operation is done.
 */
public class SignatureImageView extends AppCompatImageView {
    private static String TAG = "SignatureImageView";
    private Paint paint;
    //        private Bitmap bitmap;
//        private Canvas canvas;
    private Path path;

    public SignatureImageView(Context context) {
        super(context);
        Log.d(TAG, "SignatureImageView() constructor1");
    }

    public SignatureImageView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "SignatureImageView() constructor2");
    }

    public SignatureImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        Log.d(TAG, "SignatureImageView() constructor3");
    }


    public void init(Paint paint, Path path, Bitmap bitmap) {
        Log.d(TAG, "init() Start. getDrawable()==null?" + (getDrawable() == null));
        Log.d(TAG, "init: path: "+path);
        this.paint = paint;
        this.path = path;
        this.setImageBitmap(bitmap);
        this.setScaleType(ScaleType.FIT_CENTER);
//            bitmap = Bitmap.createBitmap(820, 480, Bitmap.Config.ARGB_4444);
//            bitmap = Bitmap.createBitmap(100, 100, Bitmap.Config.ARGB_4444);
//
//            canvas = new Canvas(bitmap);

        this.setBackgroundColor(Color.WHITE);
        Log.d(TAG, "init() End. getDrawable()==null?" + (getDrawable() == null));
    }

    public void clear() {
        if (path != null) path.reset();
        setImageBitmap(null); // may be redundant with setting drawable to null.
        setImageDrawable(null);
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        boolean retval = true;

        if (this.getDrawable() != null && this.getDrawable() instanceof BitmapDrawable
                && ((BitmapDrawable) getDrawable()).getBitmap() != null) {
            Log.d(TAG, "onTouchEvent() case: getDrawable() != null.  getDrawable() is instance of: "
                    + this.getDrawable().getClass().getCanonicalName());
            UiUtils.showToast(getContext(), "Please clear signature before replacing.");
            return retval;
        }

        float x = event.getX();
        float y = event.getY();
        Log.d(TAG, "onTouchEvent() case: getDrawable() == null or getDrawable()).getBitmap() == null.  getDrawable() is instance of: "
                + (getDrawable() != null ? this.getDrawable().getClass().getCanonicalName() : "(NULL)") + ", event.getAction()=" + event.getAction()
                + ", MotionEvent.ACTION_DOWN=" + MotionEvent.ACTION_DOWN + ", MotionEvent.ACTION_MOVE="
                + ", x=" + x + ", y=" + y);


        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(x, y);
                retval = true;
                break;
            case MotionEvent.ACTION_MOVE:
                path.lineTo(x, y);
                invalidate();
                retval = false;
                break;
            default:
                retval = false;
        }
        return retval;
    }

    public byte[] getSignatureByte() {
        Bitmap bitmap = getNewSignatureBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
        return stream.toByteArray();
    }

    public Bitmap getNewSignatureBitmap() {
//        if (getDrawable() != null) {
//            Log.d(TAG, "getNewSignatureBitmap()  getDrawable() is instance of: "
//                + this.getDrawable().getClass().getCanonicalName());
//            if (getDrawable() instanceof BitmapDrawable)
//            {
//                String filename = "drawablebitmap.png";
//
//                Bitmap bitmap = ((BitmapDrawable) getDrawable()).getBitmap();
//                Log.d(TAG, "getNewSignatureBitmap()  **** Debugging - saving getDrawable() bitmap to " + filename);
//                ImageUtils.saveBitmapToPngFile(this.getContext(), bitmap, "Debug", filename);
//            }
//        } else
//            Log.d(TAG, "getNewSignatureBitmap()  getDrawable() is null.");

        Log.d(TAG, "getNewSignatureBitmap: path: ");
        if (path == null || path.isEmpty()) {
            Log.d(TAG, "getNewSignatureBitmap() case: path is null or empty.  Returning null.");
            return null;
        } else {
            Bitmap bitmap = SignatureImageView.getBitmapFromView(this);
            Log.d(TAG, "getNewSignatureBitmap() case: path is not empty " + bitmap + " .  Returning bitmap, bitmap.getByteCount()=" + bitmap.getByteCount());
            return bitmap;
        }
    }

    public static Bitmap getBitmapFromView(View view) {
        int iscale = 8;
        float fscale = 1.0f / iscale;
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth() / iscale, view.getHeight() / iscale, Bitmap.Config.RGB_565);
        Canvas canvas = new Canvas(bitmap);
        canvas.scale(fscale, fscale);
        view.draw(canvas);
        return bitmap;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        Log.d(TAG, "onDraw() start.");
        canvas.drawPath(path, paint);
        Log.d(TAG, "onDraw() End.");
    }
}
