package com.rco.rcotrucks.custom_image;

//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.ImageCapture;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.BaseActivity;
import com.rco.rcotrucks.utils.ImagePickerActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import static android.hardware.camera2.CameraMetadata.LENS_FACING_BACK;

public class CaptureReceiptPhoto extends BaseActivity {

    private static final String TAG = CaptureReceiptPhoto.class.getSimpleName();

    ConstraintLayout captureImageLayout, rootConstraintLayout;
    FrameLayout frameLayout;
    ImageView rotateCamera, redBorderAreaIV;
    TextView cancel;
    TextureView mTextureView;

    private File mVideoFolder, videoFile;
    private String mVideoFileName;

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Integer mCameraLensFacingDirection;
    private static final int REQUEST_CAMERA_PERMISSION = 200;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_capture_receipt_photo);

//        setIds();
//        initialize();
//        setListener();
//
//        createImageFolder();
    }

//    void setIds() {
//
//        cancel = findViewById(R.id.cancel);
//        rotateCamera = findViewById(R.id.rotateCamera);
//        redBorderAreaIV = findViewById(R.id.redBorderAreaIV);
//        mTextureView = findViewById(R.id.textureView);
//        frameLayout = findViewById(R.id.frameLayout);
//        rootConstraintLayout = findViewById(R.id.rootConstraintLayout);
//        captureImageLayout = findViewById(R.id.captureImageLayout);
//    }
//
//    void initialize() {
//    }
//
//    void setListener() {
//
//        captureImageLayout.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: startRecordingVideo");
//                try {
//                    createImageFileName();
//                } catch (IOException e) {
//                    Log.d(TAG, "onClick: IOException: " + e.getMessage());
//                    e.printStackTrace();
//                }
//
//                Log.d(TAG, "onClick: mVideoFileName: " + mVideoFileName);
//                if (mVideoFileName != null) {
//                    takePicture();
//                }
//
//            }
//        });
//
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("lastSavedVideoUri", "");
//                setResult(ImagePickerActivity.REQUEST_PHOTO_CAPTURE, resultIntent);
//                finish();
//            }
//        });
//
//        rotateCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                switchCamera();
//            }
//        });
//
//
//    }
//
//    private void createImageFolder() {
//        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
//        mVideoFolder = new File(movieFile, "RCOTrucks");
//        if (!mVideoFolder.exists()) {
//            Log.d(TAG, "createVideoFolder: mVideoFolder.exists(): " + mVideoFolder.exists());
//            mVideoFolder.mkdirs();
//        }
//    }
//
//    private File createImageFileName() throws IOException {
//        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String prepend = "IMAGE_" + timestamp + "_";
//        videoFile = File.createTempFile(prepend, ".png", mVideoFolder);
//        mVideoFileName = videoFile.getAbsolutePath();
//        Log.d(TAG, "createImageFileName: mVideoFileName: " + mVideoFileName);
//        return videoFile;
//    }
//
//
//    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
//            openCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
//            // Transform you image captured size according to the surface width and height
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
//            return false;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
//            // do nothing
//        }
//    };
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void switchCamera() {
//        if (mCameraLensFacingDirection == LENS_FACING_BACK) {
//            mCameraLensFacingDirection = CameraMetadata.LENS_FACING_FRONT;
//            closeCamera();
//            reopenCamera();
//
//        } else if (mCameraLensFacingDirection == CameraMetadata.LENS_FACING_FRONT) {
//            mCameraLensFacingDirection = LENS_FACING_BACK;
//            closeCamera();
//            reopenCamera();
//        }
//    }
//
//    private void reopenCamera() {
//        if (mTextureView.isAvailable()) {
//            openCamera();
//        } else {
//            mTextureView.setSurfaceTextureListener(textureListener);
//        }
//    }
//
//    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onOpened(CameraDevice camera) {
//            cameraDevice = camera;
//            createCameraPreview();
//        }
//
//        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//        @Override
//        public void onDisconnected(CameraDevice camera) {
//            cameraDevice.close();
//        }
//
//        @Override
//        public void onError(CameraDevice camera, int error) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//    };
//
//    protected void startBackgroundThread() {
//        mBackgroundThread = new HandlerThread("Camera Background");
//        mBackgroundThread.start();
//        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
//    }
//
//    protected void stopBackgroundThread() {
//        mBackgroundThread.quitSafely();
//        try {
//            mBackgroundThread.join();
//            mBackgroundThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            // do nothing
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    void takePicture() {
//        if (null == cameraDevice) {
//            return;
//        }
//        try {
//            int width = 640;
//            int height = 480;
//            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
//            List<Surface> outputSurfaces = new ArrayList<>(2);
//            outputSurfaces.add(reader.getSurface());
////            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));
//            outputSurfaces.add(new Surface(mTextureView.getSurfaceTexture()));
//            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//
//            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraDevice.getId());
//            int rotation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);
//
////            final File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpeg");
//            final File file = videoFile;
//            if (file.exists()) {
//                file.delete();
//            }
//            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
//                @Override
//                public void onImageAvailable(ImageReader reader) {
//                    try (Image image = reader.acquireLatestImage()) {
//                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                        byte[] bytes = new byte[buffer.capacity()];
//                        buffer.get(bytes);
//                        save(bytes);
//                    } catch (IOException e) {
//                        // do nothing
//                    }
//                }
//
//                private void save(byte[] bytes) throws IOException {
//                    OutputStream output = null;
//                    try {
//                        if (!file.exists()) {
//                            output = new FileOutputStream(file);
//                            output.write(bytes);
//                        }
//                    } finally {
//                        if (null != output) {
//                            output.close();
//                        }
//                    }
//                }
//            };
//            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
//            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
//                @Override
//                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
//                    super.onCaptureCompleted(session, request, result);
//
//                    Log.d(TAG, "onCaptureCompleted: " + file.getAbsolutePath());
////                    redBorderAreaIV.setImageURI(Uri.parse(new File(file.getAbsolutePath()).toString()));
////
////                    Bitmap frameLayoutBitmap = screenShot(rootConstraintLayout);
////                    saveScreenShot(frameLayoutBitmap);
//
////                    Rect rectf = new Rect();
//
////For coordinates location relative to the parent
////                    frameLayout.getLocalVisibleRect(rectf);
////For coordinates location relative to the screen/display
////                    frameLayout.getGlobalVisibleRect(rectf);
//
////                    Log.d(TAG, "WIDTH        :" + String.valueOf(rectf.width()));
////                    Log.d(TAG, "HEIGHT       :" + String.valueOf(rectf.height()));
////                    Log.d(TAG, "left         :" + String.valueOf(rectf.left));
////                    Log.d(TAG, "right        :" + String.valueOf(rectf.right));
////                    Log.d(TAG, "top          :" + String.valueOf(rectf.top));
////                    Log.d(TAG, "bottom       :" + String.valueOf(rectf.bottom));
//
//
//                    Intent intent = new Intent();
//                    intent.putExtra(""+ImagePickerActivity.REQUEST_PHOTO_CAPTURE, file.getAbsolutePath());
//                    setResult(RESULT_OK, intent);
//                    finish();
//                }
//            };
//            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(CameraCaptureSession session) {
//                    try {
//                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
//                    } catch (CameraAccessException e) {
//                        // do nothing
//                    }
//                }
//
//                @Override
//                public void onConfigureFailed(CameraCaptureSession session) {
//
//                }
//            }, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            // do nothing
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    protected void createCameraPreview() {
//        try {
//            SurfaceTexture texture = mTextureView.getSurfaceTexture();
//            assert texture != null;
//            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
//            Surface surface = new Surface(texture);
//            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            captureRequestBuilder.addTarget(surface);
//            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
//                @Override
//                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    if (null == cameraDevice) {
//                        return;
//                    }
//                    cameraCaptureSessions = cameraCaptureSession;
//                    updatePreview();
//                }
//
//                @Override
//                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
//                    // do nothing
//                }
//            }, null);
//        } catch (CameraAccessException e) {
//            // do nothing
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    void openCamera() {
//        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            String cameraId = null;
//            if (mCameraLensFacingDirection != null) {
//                int size = manager.getCameraIdList().length;
//                for (int i = 0; i < size; i++) {
//                    if (mCameraLensFacingDirection == manager.getCameraCharacteristics(manager.getCameraIdList()[i]).get(CameraCharacteristics.LENS_FACING)) {
//                        cameraId = manager.getCameraIdList()[i];
//                    }
//                }
//            } else {
//                cameraId = manager.getCameraIdList()[0];
//                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
//                mCameraLensFacingDirection = Objects.requireNonNull(characteristics.get(CameraCharacteristics.LENS_FACING));
//                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                assert map != null;
//                imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
//            }
//
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
//                ActivityCompat.requestPermissions(CaptureReceiptPhoto.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
//                return;
//            }
//            assert cameraId != null;
//            manager.openCamera(cameraId, stateCallback, null);
//        } catch (CameraAccessException e) {
//            // do nothing
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    protected void updatePreview() {
//        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
//        try {
//            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
//        } catch (CameraAccessException e) {
//            // do nothing
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    private void closeCamera() {
//        if (null != cameraDevice) {
//            cameraDevice.close();
//            cameraDevice = null;
//        }
//        if (null != imageReader) {
//            imageReader.close();
//            imageReader = null;
//        }
//    }
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CAMERA_PERMISSION) {
//            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                finish();
//            }
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onResume() {
//        super.onResume();
//        startBackgroundThread();
//        if (mTextureView.isAvailable()) {
//            openCamera();
//        } else {
//            mTextureView.setSurfaceTextureListener(textureListener);
//        }
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @Override
//    protected void onPause() {
//        closeCamera();
//        stopBackgroundThread();
//        super.onPause();
//    }
//
//    public Bitmap screenShot(View view) {
//        Log.d(TAG, "screenShot: ");
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
//                view.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);
//        Log.d(TAG, "screenShot: bitmap: " + bitmap);
//        return bitmap;
//    }
//
//
//    public void saveScreenShot(Bitmap bitmap) {
//        Log.d(TAG, "saveScreenShot: ");
//        try {
//            try {
//                FileOutputStream fileOutputStream = new FileOutputStream(mVideoFileName);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
//                fileOutputStream.flush();
//                fileOutputStream.close();
//                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Screen", "screen");
//                Log.d(TAG, "saveScreenShot: done: i think: ");
//
//            } catch (FileNotFoundException e) {
//                Log.d(TAG, "saveScreenShot: FileNotFoundException: " + e);
//            }
//        } catch (IOException e) {
//            Log.d(TAG, "saveScreenShot: IOException: " + e);
//        }
//    }

}