package com.rco.rcotrucks.custom_image;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
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
import androidx.core.content.ContextCompat;

import com.rco.rcotrucks.R;
import com.rco.rcotrucks.utils.BaseActivity;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

public class CustomVideoRecorder extends BaseActivity {

    private static final String TAG = CustomVideoRecorder.class.getSimpleName();



    private static final int REQUEST_CAMERA_PERMISSION_RESULT = 0;
    private static final int REQUEST_WRITE_EXTERNAL_STORAGE_PERMISSION_RESULT = 1;

//    TextureView mTextureView;
//    TextureView.SurfaceTextureListener mSurfaceTextureView = new TextureView.SurfaceTextureListener() {
//        @Override
//        public void onSurfaceTextureAvailable(@NonNull SurfaceTexture surface, int width, int height) {
//
//            Log.d(TAG, "onSurfaceTextureAvailable: width: " + width);
//            Log.d(TAG, "onSurfaceTextureAvailable: height: " + height);
////            Toast.makeText(CustomVideoRecorder.this, "TextureView is now available.", Toast.LENGTH_SHORT).show();
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
//                try {
//                    createVideoFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
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
////                startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomVideoRecorder.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
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
//
//    String lastSavedVideoUri = "";
//    ImageCapture imageCapture;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_custom_video_recorder);
//
//        setIds();
//        initialize();
//        setListener();
//
//        createVideoFolder();
//
//        Camera.PictureCallback mPictureCallback = new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//
////                BitmapFactory.Options options = new BitmapFactory.Options();
////                mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length, options);
//            }
//        };
//
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
//        startRecordingVideo = findViewById(R.id.redCircle);
//        rotateCamera = findViewById(R.id.rotateCamera);
//        mTextureView = findViewById(R.id.textureView);
////        mChronometer = findViewById(R.id.chronometer);
////        selectedVideoResponseTitle = findViewById(R.id.selectedVideoResponseTitle);
//    }
//
//    void initialize() {
//
//        mMediaRecorder = new MediaRecorder();
//        mMediaRecorder.setOrientationHint(90);
//    }
//
//    void setListener() {
//
//        startRecordingVideo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: startRecordingVideo");
//
//                if (mIsRecording) {
//                    Log.d(TAG, "onClick: mIsRecording: " + mIsRecording);
//
////                    mChronometer.stop();
////                    mChronometer.setVisibility(View.INVISIBLE);
//
//                    mIsRecording = false;
//                    save.setVisibility(View.VISIBLE);
//                    rotateCamera.setVisibility(View.VISIBLE);
//                    startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomVideoRecorder.this, R.color.white), android.graphics.PorterDuff.Mode.MULTIPLY);
//
//                    mMediaRecorder.stop();
//                    mMediaRecorder.reset();
////                    startPreview();
//
//                    MediaScannerConnection.scanFile(CustomVideoRecorder.this, new String[]{videoFile.getAbsolutePath()}, null,
//                            (path, uri) -> {
//
//                                lastSavedVideoUri = uri.toString();
//                                Log.d(TAG, "onClick: videoUri: " + lastSavedVideoUri);
//
//                            });
//
//                } else {
//
//                    checkWriteStoragePermission();
//                }
//
//            }
//        });
//
//        save.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Log.d(TAG, "onClick: save");
//
//                if (lastSavedVideoUri.isEmpty()) {
//
//                    Toast.makeText(CustomVideoRecorder.this, "Please capture a video first", Toast.LENGTH_SHORT).show();
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
//                startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomVideoRecorder.this, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
//
//                try {
//                    createVideoFileName();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
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
//        mBackgroundHandlerThread = new HandlerThread("Camera2CustomVideoRecorder");
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
//            Arrays.sort(choices, new CompareSizeByArea());
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
//                            Toast.makeText(CustomVideoRecorder.this, "Unable to setup camera preview.", Toast.LENGTH_SHORT).show();
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
//        mVideoFolder = new File(movieFile, "RealRVideos");
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
//    private void checkWriteStoragePermission() {
//
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
//                    == PackageManager.PERMISSION_GRANTED) {
//
//                mIsRecording = true;
//                save.setVisibility(View.GONE);
//                rotateCamera.setVisibility(View.GONE);
//                startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomVideoRecorder.this, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
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
//            startRecordingVideo.setColorFilter(ContextCompat.getColor(CustomVideoRecorder.this, R.color.black), android.graphics.PorterDuff.Mode.MULTIPLY);
//
//            try {
//                createVideoFileName();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
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






}