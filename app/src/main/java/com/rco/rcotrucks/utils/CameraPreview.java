package com.rco.rcotrucks.utils;

import java.util.List;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.content.Context;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.View;

public class CameraPreview extends ViewGroup implements SurfaceHolder.Callback {
    @SuppressWarnings("unused")
    private final String APPTAG = "MyCameraAppNew";

    private SurfaceView surfaceView;
    private SurfaceHolder holder;
    private Size previewSize;
    private List<Size> supportedPreviewSizes;
    private Camera camera;
    private CameraPreviewCallback callback;

    private int width;
    private int height;

    public CameraPreview(Context ctx, CameraPreviewCallback callback) {
        super(ctx);

        this.callback = callback;

        surfaceView = new SurfaceView(ctx);
        addView(surfaceView);

        holder = surfaceView.getHolder();
        holder.addCallback(this);
        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    public void setSize(int w, int h) {
        this.width = w;
        this.height = h;
    }

    public void setCamera(Camera c) {
        this.camera = c;

        if (camera != null) {
            Camera.Parameters parameters = camera.getParameters();
            supportedPreviewSizes = parameters.getSupportedPreviewSizes();
            previewSize = getBestMatch(720, supportedPreviewSizes); // for width=640
            List<Size> picSizes = camera.getParameters().getSupportedPictureSizes();
            Size pSize = getBestMatch(1024, picSizes);

            if (previewSize != null)
                parameters.setPreviewSize(previewSize.width, previewSize.height);

            if (pSize != null)
                parameters.setPictureSize(pSize.width, pSize.height);

            camera.setParameters(parameters);
            requestLayout();
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        try {
            if (camera != null) {
                camera.setPreviewDisplay(holder);
                camera.startPreview();
            }
        } catch (Throwable throwable) {
            callback.onCameraError();
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        if (camera != null)
            camera.stopPreview();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        if (previewSize != null) {
            final View child = getChildAt(0);

            Log.d("com.rco.rcotrucks", "1>>> " + previewSize.width + " - " + previewSize.height);
            Log.d("com.rco.rcotrucks", "2>>> " + this.width + " - " + this.height);

            int w = 400;
            int h = this.width / w * this.height;
            int x = (r - l) / 2 - w / 2;
            int r1 = x + w;

            child.layout(0, 0, width, height);
        }
    }

    private Size getBestMatch(int w, List<Size> sizes) {
        Size newSize = null;
        int minDiff = 100000;
        int diff;

        for (Size size : sizes) {
            diff = Math.abs(w - size.width);

            if (diff < minDiff) {
                minDiff = diff;
                newSize = size;
            }
        }

        return newSize;
    }

    public interface CameraPreviewCallback {
        void onCameraError();
    }
}
