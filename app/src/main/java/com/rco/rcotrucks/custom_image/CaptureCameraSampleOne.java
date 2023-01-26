package com.rco.rcotrucks.custom_image;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Camera;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.rco.rcotrucks.R;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

public class CaptureCameraSampleOne extends Activity implements SurfaceHolder.Callback {
    private android.hardware.Camera camera = null;
    private SurfaceView cameraSurfaceView = null;
    private SurfaceHolder cameraSurfaceHolder = null;
    private boolean previewing = false;
    RelativeLayout relativeLayout;


    private ImageButton btnCapture = null;
    private ImageButton btnsave = null;
    private ImageButton btnshare = null;
    private boolean isSaved = false;
    private boolean isCaptured = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_capture_camera_sample_one);

        relativeLayout = (RelativeLayout) findViewById(R.id.containerImg);
        relativeLayout.setDrawingCacheEnabled(true);
        cameraSurfaceView = (SurfaceView)
                findViewById(R.id.surfaceView1);
        //  cameraSurfaceView.setLayoutParams(new FrameLayout.LayoutParams(640, 480));
        cameraSurfaceHolder = cameraSurfaceView.getHolder();
        cameraSurfaceHolder.addCallback(this);
//    cameraSurfaceHolder.setType(SurfaceHolder.
        //                                               SURFACE_TYPE_PUSH_BUFFERS);


        btnCapture = findViewById(R.id.capturebtn);
        btnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                camera.takePicture(cameraShutterCallback,
                        cameraPictureCallbackRaw,
                        cameraPictureCallbackJpeg);
                isCaptured = true;
            }
        });
        btnsave = findViewById(R.id.savebtn);
        btnsave.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                FrameLayout frm = (FrameLayout) findViewById(R.id.frameLayout1);
                frm.setDrawingCacheEnabled(true);
                frm.buildDrawingCache();
                Bitmap bitmap = frm.getDrawingCache();
                try {
                    File rootFile = new File(Environment.getExternalStorageDirectory().toString() + "/MYCAMERAOVERLAY");
                    rootFile.mkdirs();
                    Random generator = new Random();
                    int n = 10000;
                    n = generator.nextInt(n);
                    String fname = "Image-" + n + ".png";

                    File resultingfile = new File(rootFile, fname);

                    if (resultingfile.exists()) resultingfile.delete();
                    try {
                        FileOutputStream Fout = new FileOutputStream(resultingfile);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, Fout);
                        Fout.flush();
                        Fout.close();

                    } catch (FileNotFoundException e) {
                        Log.d("In Saving File", e + "");
                    }
                } catch (IOException e) {
                    Log.d("In Saving File", e + "");
                }
                isSaved = true;
            }
        });
        btnshare = (ImageButton) findViewById(R.id.sharebtn);
        btnshare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if ((isSaved) && (isCaptured)) {
                    // TODO sharing what ever we saved
                    // take the path


                }

            }
        });
    }


    android.hardware.Camera.ShutterCallback cameraShutterCallback = new android.hardware.Camera.ShutterCallback() {
        @Override
        public void onShutter() {
            // TODO Auto-generated method stub
        }
    };

    android.hardware.Camera.PictureCallback cameraPictureCallbackRaw = new android.hardware.Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
            // TODO Auto-generated method stub
        }
    };

    android.hardware.Camera.PictureCallback cameraPictureCallbackJpeg = new android.hardware.Camera.PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
            // TODO Auto-generated method stub
            Bitmap cameraBitmap = BitmapFactory.decodeByteArray
                    (data, 0, data.length);

            int wid = cameraBitmap.getWidth();
            int hgt = cameraBitmap.getHeight();

            //  Toast.makeText(getApplicationContext(), wid+""+hgt, Toast.LENGTH_SHORT).show();
            Bitmap newImage = Bitmap.createBitmap
                    (wid, hgt, Bitmap.Config.ARGB_8888);

            Canvas canvas = new Canvas(newImage);

            canvas.drawBitmap(cameraBitmap, 0f, 0f, null);

            camera.startPreview();

            newImage.recycle();
            newImage = null;

            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);

            startActivity(intent);

        }
    };

    @Override
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
        // TODO Auto-generated method stub

        if (previewing) {
            camera.stopPreview();
            previewing = false;
        }
        try {
            android.hardware.Camera.Parameters parameters = camera.getParameters();
            parameters.setPreviewSize(640, 480);
            parameters.setPictureSize(640, 480);
            if (this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                camera.setDisplayOrientation(-90);

            }

            // parameters.setRotation(90);
            camera.setParameters(parameters);

            camera.setPreviewDisplay(cameraSurfaceHolder);
            camera.startPreview();
            previewing = true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        try {
            camera = android.hardware.Camera.open();
        } catch (RuntimeException e) {
            Toast.makeText(getApplicationContext(), "Device camera  is not working properly, please try after sometime.", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        // TODO Auto-generated method stub
        try {


            camera.stopPreview();
            camera.release();
            camera = null;
            previewing = false;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}