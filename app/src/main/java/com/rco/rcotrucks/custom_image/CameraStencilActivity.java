package com.rco.rcotrucks.custom_image;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.rco.rcotrucks.R;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
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
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import static android.hardware.camera2.CameraMetadata.LENS_FACING_BACK;

@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class CameraStencilActivity extends AppCompatActivity {

    private static final String TAG = CameraStencilActivity.class.getSimpleName();
    //    @BindView(R.id.textture)
    TextureView textture;
    //    @BindView(R.id.stencil)
//    ImageView stencil;
    //    @BindView(R.id.stencil_description)
    TextView stencilDescription;
    ImageButton capture_button;

    protected CameraDevice cameraDevice;
    protected CameraCaptureSession cameraCaptureSessions;
    protected CaptureRequest.Builder captureRequestBuilder;
    private Size imageDimension;
    private ImageReader imageReader;
    //    private StencilItem stencilItem;
    private static final int REQUEST_CAMERA_PERMISSION = 200;
    public static final int STENCIL_CAMERA_REQUEST_CODE = 10;
    public static final String KEY_CAPTURED_STENCIL_DATA = "KEY_CAPTURED_STENCIL_DATA";
    private Handler mBackgroundHandler;
    private HandlerThread mBackgroundThread;
    private Integer mCameraLensFacingDirection;

//    public static void startForResult(Fragment fragment, StencilItem stencilItem) {
//        Intent intent = new Intent(fragment.getContext(), CameraStencilActivity.class);
//        intent.putExtra("stencil", stencilItem);
//        fragment.startActivityForResult(intent, STENCIL_CAMERA_REQUEST_CODE);
//    }

    //    @OnClick(R.id.btn_rotate)
    void onRotateClick() {
        switchCamera();
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_stencil);

        textture = findViewById(R.id.view_finder);
//        stencil = findViewById(R.id.stencil);
        capture_button = findViewById(R.id.capture_button);


//        ButterKnife.bind(this);
//        stencilItem = getIntent().getParcelableExtra("stencil");
//        Glide.with(this).load(stencilItem.getStencil().getUrl()).into(stencil);
        textture.setSurfaceTextureListener(textureListener);
//        stencilDescription.setText(stencilItem.getStencil().getUnderlineText());

        capture_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePicture();
            }
        });

//        onRotateClick();
    }

    TextureView.SurfaceTextureListener textureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            openCamera();
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            // Transform you image captured size according to the surface width and height
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
            // do nothing
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void switchCamera() {
        if (mCameraLensFacingDirection == LENS_FACING_BACK) {
            mCameraLensFacingDirection = CameraMetadata.LENS_FACING_FRONT;
            closeCamera();
            reopenCamera();

        } else if (mCameraLensFacingDirection == CameraMetadata.LENS_FACING_FRONT) {
            mCameraLensFacingDirection = LENS_FACING_BACK;
            closeCamera();
            reopenCamera();
        }
    }

    private void reopenCamera() {
        if (textture.isAvailable()) {
            openCamera();
        } else {
            textture.setSurfaceTextureListener(textureListener);
        }
    }

    private final CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onOpened(CameraDevice camera) {
            cameraDevice = camera;
            createCameraPreview();
        }

        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @Override
        public void onDisconnected(CameraDevice camera) {
            cameraDevice.close();
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            cameraDevice.close();
            cameraDevice = null;
        }
    };

    protected void startBackgroundThread() {
        mBackgroundThread = new HandlerThread("Camera Background");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    protected void stopBackgroundThread() {
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            // do nothing
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
//    @OnClick(R.id.btn_takepicture)
    void takePicture() {
        if (null == cameraDevice) {
            return;
        }
        try {
            int width = 640;
            int height = 480;
            ImageReader reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 1);
            List<Surface> outputSurfaces = new ArrayList<>(2);
            outputSurfaces.add(reader.getSurface());
            outputSurfaces.add(new Surface(textture.getSurfaceTexture()));
            final CaptureRequest.Builder captureBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);

            CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
            CameraCharacteristics cameraCharacteristics = manager.getCameraCharacteristics(cameraDevice.getId());
            int rotation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, rotation);

            final File file = new File(Environment.getExternalStorageDirectory() + "/" + System.currentTimeMillis() + ".jpeg");
            if (file.exists()) {
                file.delete();
            }
            ImageReader.OnImageAvailableListener readerListener = new ImageReader.OnImageAvailableListener() {
                @Override
                public void onImageAvailable(ImageReader reader) {
                    try (Image image = reader.acquireLatestImage()) {
                        ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                        byte[] bytes = new byte[buffer.capacity()];
                        buffer.get(bytes);
                        save(bytes);
                    } catch (IOException e) {
                        // do nothing
                    }
                }

                private void save(byte[] bytes) throws IOException {
                    OutputStream output = null;
                    try {
                        if (!file.exists()) {
                            output = new FileOutputStream(file);
                            output.write(bytes);
                        }
                    } finally {
                        if (null != output) {
                            output.close();
                        }
                    }
                }
            };
            reader.setOnImageAvailableListener(readerListener, mBackgroundHandler);
            final CameraCaptureSession.CaptureCallback captureListener = new CameraCaptureSession.CaptureCallback() {
                @Override
                public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                    super.onCaptureCompleted(session, request, result);

                    Log.d(TAG, "onCaptureCompleted: " + file.getAbsolutePath());
//                    Intent intent = new Intent();
//                    intent.putExtra(KEY_CAPTURED_STENCIL_DATA, new CapturedStencilData(file.getAbsolutePath(), stencilItem));
//                    setResult(RESULT_OK, intent);
//                    finish();
                }
            };
            cameraDevice.createCaptureSession(outputSurfaces, new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.capture(captureBuilder.build(), captureListener, mBackgroundHandler);
                    } catch (CameraAccessException e) {
                        // do nothing
                    }
                }

                @Override
                public void onConfigureFailed(CameraCaptureSession session) {

                }
            }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            // do nothing
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void createCameraPreview() {
        try {
            SurfaceTexture texture = textture.getSurfaceTexture();
            assert texture != null;
            texture.setDefaultBufferSize(imageDimension.getWidth(), imageDimension.getHeight());
            Surface surface = new Surface(texture);
            captureRequestBuilder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            captureRequestBuilder.addTarget(surface);
            cameraDevice.createCaptureSession(Collections.singletonList(surface), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    if (null == cameraDevice) {
                        return;
                    }
                    cameraCaptureSessions = cameraCaptureSession;
                    updatePreview();
                }

                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    // do nothing
                }
            }, null);
        } catch (CameraAccessException e) {
            // do nothing
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void openCamera() {
        CameraManager manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            String cameraId = null;
            if (mCameraLensFacingDirection != null) {
                int size = manager.getCameraIdList().length;
                for (int i = 0; i < size; i++) {
                    if (mCameraLensFacingDirection == manager.getCameraCharacteristics(manager.getCameraIdList()[i]).get(CameraCharacteristics.LENS_FACING)) {
                        cameraId = manager.getCameraIdList()[i];
                    }
                }
            } else {
                cameraId = manager.getCameraIdList()[0];
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                mCameraLensFacingDirection = Objects.requireNonNull(characteristics.get(CameraCharacteristics.LENS_FACING));
                StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
                assert map != null;
                imageDimension = map.getOutputSizes(SurfaceTexture.class)[0];
            }

            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(CameraStencilActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CAMERA_PERMISSION);
                return;
            }
            assert cameraId != null;
            manager.openCamera(cameraId, stateCallback, null);
        } catch (CameraAccessException e) {
            // do nothing
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    protected void updatePreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            cameraCaptureSessions.setRepeatingRequest(captureRequestBuilder.build(), null, mBackgroundHandler);
        } catch (CameraAccessException e) {
            // do nothing
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void closeCamera() {
        if (null != cameraDevice) {
            cameraDevice.close();
            cameraDevice = null;
        }
        if (null != imageReader) {
            imageReader.close();
            imageReader = null;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
                finish();
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();
        startBackgroundThread();
        if (textture.isAvailable()) {
            openCamera();
        } else {
            textture.setSurfaceTextureListener(textureListener);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onPause() {
        closeCamera();
        stopBackgroundThread();
        super.onPause();
    }

}