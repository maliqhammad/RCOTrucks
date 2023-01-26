package com.rco.rcotrucks.custom_image;

//import androidx.annotation.NonNull;
//import androidx.appcompat.app.AppCompatActivity;
//import androidx.camera.core.ImageCapture;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CaptureRequest;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.provider.MediaStore;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.TextureView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.BaseActivity;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.SystemClock;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.TextureView;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

//import androidx.annotation.NonNull;
//import androidx.camera.core.ImageCapture;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Random;


public class CustomImageRecorder extends BaseActivity {

//    private static final String TAG = CustomImageRecorder.class.getSimpleName();
//
//    FrameLayout frameLayout;
//    ConstraintLayout rootConstraintLayout;
//    ImageView redBorderAreaIV;
//
//
//    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
//    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;
//
//    TextureView mTextureView;
//    TextureView.SurfaceTextureListener mSurfaceTextureView = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
//
//            Log.d(TAG, "onSurfaceTextureAvailable: width: " + width);
//            Log.d(TAG, "onSurfaceTextureAvailable: height: " + height);
////            Toast.makeText(CustomImageRecorder.this, "TextureView is now available.", Toast.LENGTH_SHORT).show();
//
//            setupBackCamera(width, height);
//            connectCamera();
//        }
//
//        @Override
//        public void onSurfaceTextureSizeChanged(@NonNull SurfaceTexture surface, int width, int height) {
//
//        }
//
//        @Override
//        public boolean onSurfaceTextureDestroyed(@NonNull SurfaceTexture surface) {
//            return false;
//        }
//
//        @Override
//        public void onSurfaceTextureUpdated(@NonNull SurfaceTexture surface) {
//
//        }
//    };
//
//    CameraDevice mCameraDevice;
//    CameraDevice.StateCallback mCameraDeviceStateCallBack = new CameraDevice.StateCallback() {
//        @Override
//        public void onOpened(@NonNull CameraDevice camera) {
//
//            Log.d(TAG, "onOpened: ");
//            mCameraDevice = camera;
//
//
//            if (mIsRecording) {
//                Log.d(TAG, "onOpened: mIsRecording: " + mIsRecording);
////                try {
//////                    createVideoFileName();
//////                    createImageFileName();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//                startRecord();
//
//                mMediaRecorder.start();
//
////                mChronometer.setBase(SystemClock.elapsedRealtime());
////                mChronometer.setVisibility(View.VISIBLE);
////                mChronometer.start();
//
////                mChronometer.stop();
////                mChronometer.setVisibility(View.INVISIBLE);
////
////                mIsRecording = false;
////                save.setVisibility(View.VISIBLE);
////                rotateCamera.setVisibility(View.VISIBLE);
////                startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomImageRecorder.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
////
////                mMediaRecorder.stop();
////                mMediaRecorder.reset();
////                startPreview();
//
//            } else {
//                startPreview();
//            }
//        }
//
//        @Override
//        public void onDisconnected(@NonNull CameraDevice camera) {
//
//            Log.d(TAG, "onDisconnected: ");
//            camera.close();
//            mCameraDevice = null;
//        }
//
//        @Override
//        public void onError(@NonNull CameraDevice camera, int error) {
//            Log.d(TAG, "onError: error: " + error);
//            camera.close();
//            mCameraDevice = null;
//        }
//    };
//
//    String mCameraId = "";
//    private Size mPreviewSize;
//    private CaptureRequest.Builder mCaptureRequestBuilder;
//    HandlerThread mBackgroundHandlerThread;
//    Handler mBackgroundHandler;
//    private static SparseIntArray ORIENTATIONS = new SparseIntArray();
//
//    static {
//
//        ORIENTATIONS.append(Surface.ROTATION_0, 0);
//        ORIENTATIONS.append(Surface.ROTATION_90, 90);
//        ORIENTATIONS.append(Surface.ROTATION_180, 180);
//        ORIENTATIONS.append(Surface.ROTATION_270, 270);
//    }
//
//    private static class CompareSizeByArea implements Comparator<Size> {
//
//        @Override
//        public int compare(Size lhs, Size rhs) {
//            return Long.signum((long) (lhs.getWidth() * lhs.getHeight()) -
//                    (long) (rhs.getWidth() * rhs.getHeight()));
//        }
//    }
//
//    TextView cancel, save;
//    ImageView rotateCamera, startRecordingVideo;
//    boolean mIsRecording = false, isFrontCameraSelect = false;
//
//    private File mVideoFolder, videoFile;
//    private String mVideoFileName;
//
//    private int mTotalRotation;
//    private Size mVideoSize;
//    private MediaRecorder mMediaRecorder;
//    MediaPlayer mediaPlayer;
//
//
//    String lastSavedVideoUri = "";
//    ImageCapture imageCapture;
//
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_custom_image_recorder);
//
//        setIds();
//        initialize();
//        setListener();
//
//        createVideoFolder();
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//
//        startBackgroundThread();
//        Log.d(TAG, "onResume: mTextureView.isAvailable(): " + mTextureView.isAvailable());
//        if (mTextureView.isAvailable()) {
//
//            setupBackCamera(mTextureView.getWidth(), mTextureView.getHeight());
//            connectCamera();
//        } else {
//
//            mTextureView.setSurfaceTextureListener(mSurfaceTextureView);
//        }
//    }
//
//
//    void setIds() {
//
//        cancel = findViewById(R.id.cancel);
//        save = findViewById(R.id.save);
//        redBorderAreaIV = findViewById(R.id.redBorderAreaIV);
//        startRecordingVideo = findViewById(R.id.redCircle);
//        rotateCamera = findViewById(R.id.rotateCamera);
//        mTextureView = findViewById(R.id.textureView);
////        mChronometer = findViewById(R.id.chronometer);
////        selectedVideoResponseTitle = findViewById(R.id.selectedVideoResponseTitle);
//
//        frameLayout = findViewById(R.id.frameLayout);
//        rootConstraintLayout = findViewById(R.id.rootConstraintLayout);
//    }
//
//    void initialize() {
//
//        mMediaRecorder = new MediaRecorder();
//        mediaPlayer = new MediaPlayer();
//        mMediaRecorder.setOrientationHint(90);
//    }
//
//    void setListener() {
//
//        startRecordingVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: startRecordingVideo");
////                checkWriteStoragePermission();
//                try {
//                    createImageFileName();
//                } catch (IOException e) {
//                    Log.d(TAG, "onClick: IOException: " + e.getMessage());
//                    e.printStackTrace();
//                }
//
//                Log.d(TAG, "onClick: mVideoFileName: " + mVideoFileName);
//                if (mVideoFileName != null) {
////                    takePhoto();
////                    Bitmap bitmap = screenShot(mTextureView); //  Returning black screen
////                    Bitmap bitmap = screenShot(rootConstraintLayout); //    Returning screenshot of the phone, but texture view is still black
////                    Bitmap bitmap = screenShot(frameLayout); //    Returning frame view area s.s but its area is black
//                    Bitmap bitmap = screenShot(redBorderAreaIV); //    Returning image view area s.s but its area is black
//                    saveScreenShot(bitmap);
//                }
//
//            }
//        });
//
////        startRecordingVideo.setOnClickListener(new View.OnClickListener() {
////            @Override
////            public void onClick(View v) {
////                Log.d(TAG, "onClick: startRecordingVideo");
////
////                if (mIsRecording) {
////                    Log.d(TAG, "onClick: mIsRecording: " + mIsRecording);
////
//////                    mChronometer.stop();
//////                    mChronometer.setVisibility(View.INVISIBLE);
////
////                    mIsRecording = false;
////                    save.setVisibility(View.VISIBLE);
////                    rotateCamera.setVisibility(View.VISIBLE);
////                    startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomImageRecorder.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
////
////                    mMediaRecorder.stop();
////                    mMediaRecorder.reset();
//////                    startPreview();
////
////                    MediaScannerConnection.scanFile(CustomImageRecorder.this, new String[]{videoFile.getAbsolutePath()}, null,
////                            (path, uri) -> {
////
////                                lastSavedVideoUri = uri.toString();
////                                Log.d(TAG, "onClick: videoUri: " + lastSavedVideoUri);
////
////                            });
////
////                } else {
////
////                    checkWriteStoragePermission();
////                }
////
////            }
////        });
//
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: save");
//
//                if (lastSavedVideoUri.isEmpty()) {
//
//                    Toast.makeText(CustomImageRecorder.this, "Please capture a video first", Toast.LENGTH_SHORT).show();
//                } else {
//
//                    Intent resultIntent = new Intent();
//                    resultIntent.putExtra("lastSavedVideoUri", lastSavedVideoUri);
//                    resultIntent.putExtra("mVideoFileName", mVideoFileName);
////                    setResult(AttachVideoResponse.REQUEST_VIDEO_CAPTURE, resultIntent);
//                    setResult(111, resultIntent);
//                    finish();
//                }
//            }
//        });
//
//
//        cancel.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Intent resultIntent = new Intent();
//                resultIntent.putExtra("lastSavedVideoUri", "");
////                setResult(AttachVideoResponse.REQUEST_VIDEO_CAPTURE, resultIntent);
//                setResult(111, resultIntent);
//                finish();
//            }
//        });
//
//        rotateCamera.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                Log.d(TAG, "onClick: isFrontCameraSelect: " + isFrontCameraSelect);
//                if (isFrontCameraSelect) {
//
//                    closeCamera();
//                    stopBackgroundThread();
//
//                    startBackgroundThread();
//                    Log.d(TAG, "onResume: mTextureView.isAvailable(): " + mTextureView.isAvailable());
//                    if (mTextureView.isAvailable()) {
//
//                        setupBackCamera(mTextureView.getWidth(), mTextureView.getHeight());
//                        connectCamera();
//                    } else {
//
//                        mTextureView.setSurfaceTextureListener(mSurfaceTextureView);
//                    }
//                } else {
//
//                    closeCamera();
//                    stopBackgroundThread();
//
//                    startBackgroundThread();
//                    Log.d(TAG, "onResume: mTextureView.isAvailable(): " + mTextureView.isAvailable());
//                    if (mTextureView.isAvailable()) {
//
//                        setupFrontCamera(mTextureView.getWidth(), mTextureView.getHeight());
//                        connectCamera();
//                    } else {
//
//                        mTextureView.setSurfaceTextureListener(mSurfaceTextureView);
//                    }
//                }
//            }
//        });
//
//
//    }
//
//
//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == REQUEST_CAMERA_PERMISSION_RESULT) {
//            if (grantResults[0] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getApplicationContext(),
//                        "Application will not run without camera services", Toast.LENGTH_SHORT).show();
//            }
//            if (grantResults[1] != PackageManager.PERMISSION_GRANTED) {
//                Toast.makeText(getApplicationContext(),
//                        "Application will not have audio on record", Toast.LENGTH_SHORT).show();
//            }
//        }
//
//        if (requestCode == REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT) {
//            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//
//                mIsRecording = true;
//                save.setVisibility(View.GONE);
//                rotateCamera.setVisibility(View.GONE);
//                startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomImageRecorder.this, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
//
////                try {
//////                    createVideoFileName();
////                    createImageFileName();
////                } catch (IOException e) {
////                    e.printStackTrace();
////                }
//
//                Toast.makeText(this,
//                        "Permission successfully granted!", Toast.LENGTH_SHORT).show();
//            } else {
//                Toast.makeText(this,
//                        "App needs to save video to run", Toast.LENGTH_SHORT).show();
//            }
//        }
//    }
//
//    public void setupFrontCamera(int width, int height) {
//        isFrontCameraSelect = true;
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//
//        try {
//            for (String cameraId : cameraManager.getCameraIdList()) {
//
//                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
//                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
//
//                    StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                    int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
//                    mTotalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
//                    boolean swapRotation = mTotalRotation == 90 || mTotalRotation == 270;
//                    int rotatedWidth = width;
//                    int rotatedHeight = height;
//                    if (swapRotation) {
//                        rotatedWidth = height;
//                        rotatedHeight = width;
//                    }
//                    mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
//                    mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
//                    mCameraId = cameraId;
//                    return;
//                }
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    public void setupBackCamera(int width, int height) {
//        isFrontCameraSelect = false;
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//
//        try {
//            for (String cameraId : cameraManager.getCameraIdList()) {
//
//                CameraCharacteristics cameraCharacteristics = cameraManager.getCameraCharacteristics(cameraId);
//                if (cameraCharacteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
//
//                    continue;
//                }
//                StreamConfigurationMap map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
//                int deviceOrientation = getWindowManager().getDefaultDisplay().getRotation();
//                int totalRotation = sensorToDeviceRotation(cameraCharacteristics, deviceOrientation);
//                boolean swapRotation = totalRotation == 90 || totalRotation == 270;
//                int rotatedWidth = width;
//                int rotatedHeight = height;
//                if (swapRotation) {
//                    rotatedWidth = height;
//                    rotatedHeight = width;
//                }
//                mPreviewSize = chooseOptimalSize(map.getOutputSizes(SurfaceTexture.class), rotatedWidth, rotatedHeight);
//                mVideoSize = chooseOptimalSize(map.getOutputSizes(MediaRecorder.class), rotatedWidth, rotatedHeight);
//                mCameraId = cameraId;
//                return;
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//
//    }
//
//    private void connectCamera() {
//        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
//        try {
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//                if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) ==
//                        PackageManager.PERMISSION_GRANTED) {
//                    cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
//                } else {
//                    if (shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA)) {
//                        Toast.makeText(this,
//                                "Video app required access to camera", Toast.LENGTH_SHORT).show();
//                    }
//                    requestPermissions(new String[]{android.Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO
//                    }, REQUEST_CAMERA_PERMISSION_RESULT);
//                }
//
//            } else {
//                cameraManager.openCamera(mCameraId, mCameraDeviceStateCallBack, mBackgroundHandler);
//            }
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    @Override
//    protected void onPause() {
//        closeCamera();
//        stopBackgroundThread();
//        super.onPause();
//    }
//
//    void closeCamera() {
//
//        if (mCameraDevice != null) {
//
//            mCameraDevice.close();
//            mCameraDevice = null;
//        }
//    }
//
//    public void startBackgroundThread() {
//
//        mBackgroundHandlerThread = new HandlerThread("Camera2CustomImageRecorder");
//        mBackgroundHandlerThread.start();
//        mBackgroundHandler = new Handler(mBackgroundHandlerThread.getLooper());
//    }
//
//    public void stopBackgroundThread() {
//
//        mBackgroundHandlerThread.quitSafely();
//        try {
//            mBackgroundHandlerThread.join();
//            mBackgroundHandlerThread = null;
//            mBackgroundHandler = null;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private static int sensorToDeviceRotation(CameraCharacteristics cameraCharacteristics, int deviceOrientation) {
//        int sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
//        deviceOrientation = ORIENTATIONS.get(deviceOrientation);
//        return (sensorOrientation + deviceOrientation + 360) % 360;
//    }
//
////    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
////        List<Size> bigEnough = new ArrayList<Size>();
////        for (Size option : choices) {
////            if (option.getHeight() == option.getWidth() * height / width &&
////                    option.getWidth() >= width && option.getHeight() >= height) {
////                bigEnough.add(option);
////            }
////        }
////        if (bigEnough.size() > 0) {
////            return Collections.min(bigEnough, new CompareSizeByArea());
////        } else {
////            return choices[0];
////        }
////    }
//
//    private static Size chooseOptimalSize(Size[] choices, int width, int height) {
//        Size bigEnough = null;
//        int minAreaDiff = Integer.MAX_VALUE;
//        for (Size option : choices) {
//            int diff = (width * height) - (option.getWidth() * option.getHeight());
//            if (diff >= 0 && diff < minAreaDiff &&
//                    option.getWidth() <= width &&
//                    option.getHeight() <= height) {
//                minAreaDiff = diff;
//                bigEnough = option;
//            }
//        }
//        if (bigEnough != null) {
//            return bigEnough;
//        } else {
//            Arrays.sort(choices, new CustomImageRecorder.CompareSizeByArea());
//            return choices[0];
//        }
//
//    }
//
//    private void startPreview() {
//        SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//        surfaceTexture.setDefaultBufferSize(mPreviewSize.getWidth(), mPreviewSize.getHeight());
//        Surface previewSurface = new Surface(surfaceTexture);
//
//        try {
//            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
//            mCaptureRequestBuilder.addTarget(previewSurface);
//
//            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface),
//                    new CameraCaptureSession.StateCallback() {
//                        @Override
//                        public void onConfigured(CameraCaptureSession session) {
//                            Log.d(TAG, "onConfigured: startPreview");
//
//                            try {
//                                session.setRepeatingRequest(mCaptureRequestBuilder.build(),
//                                        null, mBackgroundHandler);
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConfigureFailed(CameraCaptureSession session) {
//                            Log.d(TAG, "onConfigureFailed: startPreview");
//                            Toast.makeText(CustomImageRecorder.this, "Unable to setup camera preview.", Toast.LENGTH_SHORT).show();
//
//                        }
//                    }, null);
//        } catch (CameraAccessException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void createVideoFolder() {
//
//        File movieFile = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
////        File movieFile = CameraHelper.getOutputMediaFile(CameraHelper.MEDIA_TYPE_VIDEO);
//        mVideoFolder = new File(movieFile, "RCOTrucks");
//        if (!mVideoFolder.exists()) {
//            Log.d(TAG, "createVideoFolder: mVideoFolder.exists(): " + mVideoFolder.exists());
//            mVideoFolder.mkdirs();
//        }
//    }
//
//    private File createVideoFileName() throws IOException {
//        String timestamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
//        String prepend = "VIDEO_" + timestamp + "_";
//        videoFile = File.createTempFile(prepend, ".mp4", mVideoFolder);
//        mVideoFileName = videoFile.getAbsolutePath();
//        return videoFile;
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
//    private void checkWriteStoragePermission() {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//
//                mIsRecording = true;
//                save.setVisibility(View.GONE);
//                rotateCamera.setVisibility(View.GONE);
//                startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomImageRecorder.this, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
//
//                try {
//                    createVideoFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//
//                startRecord();
//                mMediaRecorder.start();
//
////                mChronometer.setBase(SystemClock.elapsedRealtime());
////                mChronometer.setVisibility(View.VISIBLE);
////                mChronometer.start();
//
//            } else {
//                if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                    Toast.makeText(this, "app needs to be able to save videos", Toast.LENGTH_SHORT).show();
//                }
//                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT);
//            }
//        } else {
//
//            mIsRecording = true;
//            save.setVisibility(View.GONE);
//            rotateCamera.setVisibility(View.GONE);
//            startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomImageRecorder.this, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
/////storage/emulated/0/Movies/RCOTrucks/IMAGE_20220715_145912_7066721091513858609.png
////            try {
//////                createVideoFileName();
////                createImageFileName();
////            } catch (IOException e) {
////                e.printStackTrace();
////            }
//
//            startRecord();
//            mMediaRecorder.start();
//
////            mChronometer.setBase(SystemClock.elapsedRealtime());
////            mChronometer.setVisibility(View.VISIBLE);
////            mChronometer.start();
//        }
//    }
//
//    private void setupMediaRecorder() {
//
//
//        mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE);
////        mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
//        mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
//        mMediaRecorder.setOutputFile(mVideoFileName);
//        mMediaRecorder.setVideoEncodingBitRate(1000000);
//        mMediaRecorder.setVideoFrameRate(30);
//        mMediaRecorder.setVideoSize(mVideoSize.getWidth(), mVideoSize.getHeight());
//        mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
////        mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
//        mMediaRecorder.setOrientationHint(90);
////        mMediaRecorder.setOrientationHint(mTotalRotation);
//        try {
//            mMediaRecorder.prepare();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    private void startRecord() {
//
//        try {
//
//            setupMediaRecorder();
//
//            SurfaceTexture surfaceTexture = mTextureView.getSurfaceTexture();
//            surfaceTexture.setDefaultBufferSize(mTextureView.getWidth(), mTextureView.getHeight());
//            Surface previewSurface = new Surface(surfaceTexture);
//            Surface recordSurface = mMediaRecorder.getSurface();
//            mCaptureRequestBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_RECORD);
//            mCaptureRequestBuilder.addTarget(previewSurface);
//            mCaptureRequestBuilder.addTarget(recordSurface);
//
//            Log.d(TAG, "startRecord: previewSurface: " + previewSurface);
//            Log.d(TAG, "startRecord: recordSurface: " + recordSurface);
//
//            mCameraDevice.createCaptureSession(Arrays.asList(previewSurface, recordSurface),
//                    new CameraCaptureSession.StateCallback() {
//                        @Override
//                        public void onConfigured(CameraCaptureSession session) {
//                            try {
//                                session.setRepeatingRequest(mCaptureRequestBuilder.build(), null, null);
//                            } catch (CameraAccessException e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onConfigureFailed(CameraCaptureSession session) {
//                            Log.d(TAG, "onConfigureFailed: startRecord");
//                        }
//                    }, null);
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    void takePhoto() {
//        Log.d(TAG, "takePhoto: ");
//
////        frameLayout.setDrawingCacheEnabled(true);
////        frameLayout.setDrawingCacheQuality(FrameLayout.DRAWING_CACHE_QUALITY_HIGH);
////        frameLayout.buildDrawingCache();
////
//////        Bitmap bitmap = frameLayout.getDrawingCache();
////
////        Bitmap bitmap = Bitmap.createBitmap(frameLayout.getDrawingCache());
////        Log.d(TAG, "takePhoto: bitmap: " + bitmap);
////        frameLayout.setDrawingCacheEnabled(false);
//
//
//        mTextureView.setDrawingCacheEnabled(true);
//        mTextureView.setDrawingCacheQuality(FrameLayout.DRAWING_CACHE_QUALITY_HIGH);
//        mTextureView.buildDrawingCache();
//
////        Bitmap bitmap = frameLayout.getDrawingCache();
//
//        Bitmap bitmap = Bitmap.createBitmap(mTextureView.getDrawingCache());
//        Log.d(TAG, "takePhoto: bitmap: " + bitmap);
//        mTextureView.setDrawingCacheEnabled(false);
//
//
//        try {
////            File rootFile = new File(Environment.getExternalStorageDirectory().toString() + "/MYCAMERAOVERLAY");
////            rootFile.mkdirs();
////            Random generator = new Random();
////            int n = 10000;
////            n = generator.nextInt(n);
////            String fname = "Image-" + n + ".png";
////
////            File resultingfile = new File(rootFile, fname);
//
////            if (resultingfile.exists()) resultingfile.delete();
//            try {
//                FileOutputStream fileOutputStream = new FileOutputStream(mVideoFileName);
//                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutputStream);
//                fileOutputStream.flush();
//                fileOutputStream.close();
//                MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Screen", "screen");
//
////                u.setDrawingCacheEnabled(true);
////                ScrollView z = (ScrollView) findViewById(R.id.scroll);
////                int totalHeight = z.getChildAt(0).getHeight();
////                int totalWidth = z.getChildAt(0).getWidth();
////                u.layout(0, 0, totalWidth, totalHeight);
////                u.buildDrawingCache(true);
////                Bitmap b = Bitmap.createBitmap(u.getDrawingCache());
////                u.setDrawingCacheEnabled(false);
//
//                //Save bitmap
////                String extr = Environment.getExternalStorageDirectory().toString() +   File.separator + "Folder";
////                String fileName = new SimpleDateFormat("yyyyMMddhhmm'_report.jpg'").format(new Date());
////                File myPath = new File(extr, fileName);
////                FileOutputStream fos = null;
////                try {
//////                    fos = new FileOutputStream(myPath);
//////                    b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
//////                    fos.flush();
//////                    fos.close();
////                    MediaStore.Images.Media.insertImage(getContentResolver(), b, "Screen", "screen");
////                }catch (FileNotFoundException e) {
////                    // TODO Auto-generated catch block
////                    e.printStackTrace();
////                } catch (Exception e) {
////                    // TODO Auto-generated catch block
////                    e.printStackTrace();
////                }
////
//
//            } catch (FileNotFoundException e) {
//                Log.d(TAG, "takePhoto: FileNotFoundException: " + e);
//            }
//        } catch (IOException e) {
//            Log.d(TAG, "takePhoto: IOException: " + e);
//        }
////        isSaved = true;
//    }
//
//
//    android.hardware.Camera.ShutterCallback cameraShutterCallback = new android.hardware.Camera.ShutterCallback() {
//        @Override
//        public void onShutter() {
//            Log.d(TAG, "onShutter: ");
//            // TODO Auto-generated method stub
//        }
//    };
//
//    android.hardware.Camera.PictureCallback cameraPictureCallbackRaw = new android.hardware.Camera.PictureCallback() {
//        @Override
//        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
//            Log.d(TAG, "onPictureTaken: ");
//            // TODO Auto-generated method stub
//        }
//    };
//
//    android.hardware.Camera.PictureCallback cameraPictureCallbackJpeg = new android.hardware.Camera.PictureCallback() {
//        @Override
//        public void onPictureTaken(byte[] data, android.hardware.Camera camera) {
//            Log.d(TAG, "onPictureTaken: ");
//            // TODO Auto-generated method stub
//            Bitmap cameraBitmap = BitmapFactory.decodeByteArray
//                    (data, 0, data.length);
//
//            int wid = cameraBitmap.getWidth();
//            int hgt = cameraBitmap.getHeight();
//
//            //  Toast.makeText(getApplicationContext(), wid+""+hgt, Toast.LENGTH_SHORT).show();
//            Bitmap newImage = Bitmap.createBitmap
//                    (wid, hgt, Bitmap.Config.ARGB_8888);
//
//            Canvas canvas = new Canvas(newImage);
//
//            canvas.drawBitmap(cameraBitmap, 0f, 0f, null);
//
//            camera.startPreview();
//
//            newImage.recycle();
//            newImage = null;
//
//            Intent intent = new Intent();
//            intent.setAction(Intent.ACTION_VIEW);
//
//            startActivity(intent);
//
//        }
//    };
//
//    private static Bitmap takeScreenshot() {
//        Bitmap screen;
//        Bitmap resizedBitmap = null;
////        try {
////            Process sh = Runtime.getRuntime().exec("su", null, null);
////
////            OutputStream os = sh.getOutputStream();
////            Log.d("SCREENSHOT ", "PATH: " + ActFileUtil.getPathToScreenshotDirOnDevice());
////            os.write(("/system/bin/screencap -p " + ActFileUtil.getPathToScreenshotDirOnDevice()).getBytes("ASCII"));
////            os.flush();
////            os.close();
////            sh.waitFor();
////            Log.d(TAG, "procScreenshot: SCREENSHOT TAKEN");
////            screen = BitmapFactory.decodeFile(ActFileUtil.getPathToScreenshotDirOnDevice());
////            if(screen != null) {
////                resizedBitmap = Bitmap.createScaledBitmap(screen, screen.getWidth() / 4, screen.getHeight() / 4, true);
////            }
////
////
////        } catch (IOException | NullPointerException | InterruptedException e) {
////            e.printStackTrace();
////        }
//
//        return resizedBitmap;
//    }
//
//    public Bitmap screenShot(View view) {
//        Log.d(TAG, "screenShot: ");
//        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(),
//                view.getHeight(), Bitmap.Config.ARGB_8888);
//        Canvas canvas = new Canvas(bitmap);
//        view.draw(canvas);
//        Log.d(TAG, "screenShot: bitmap: "+bitmap);
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