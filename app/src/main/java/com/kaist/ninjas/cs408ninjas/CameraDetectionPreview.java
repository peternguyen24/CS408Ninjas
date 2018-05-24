package com.kaist.ninjas.cs408ninjas;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.media.Image;
import android.media.ImageReader;
import android.os.Bundle;
import android.app.Activity;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.util.SparseIntArray;
import android.view.Surface;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.kaist.ninjas.cs408ninjas.detection.MotionDetector;
import com.kaist.ninjas.cs408ninjas.detection.HandDetector;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;

public class CameraDetectionPreview extends Activity {
    private static final SparseIntArray ORIENTATIONS = new SparseIntArray(4);

    static {
        ORIENTATIONS.append(Surface.ROTATION_0, 90);
        ORIENTATIONS.append(Surface.ROTATION_90, 0);
        ORIENTATIONS.append(Surface.ROTATION_180, 270);
        ORIENTATIONS.append(Surface.ROTATION_270, 180);
    }

    private CameraDevice mCameraDevice;
    private Button captureButton;
    private Button previewHistButton;

    private ImageView imageView;
    private CameraCaptureSession mCameraCaptureSession;
    private HandlerThread backgroundThread;
    private Handler backgroundHandler;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader.OnImageAvailableListener readerListener;
    private ImageReader.OnImageAvailableListener histPreviewReaderListener;
    private ImageReader reader;
    private Mat handHist = null;
    private boolean isDetecting;
    private boolean isGettingHist;


    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private CameraCaptureSession.CaptureCallback captureListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera_detection_preview);

        captureButton = findViewById(R.id.capture_button);
        previewHistButton = findViewById(R.id.preview_hist_button);
        imageView = findViewById(R.id.image_view);

        // for CAPTURE - detection
        readerListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                final Image image;
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                Log.i("BEFORE DRAWING", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));

//                Mat orig = Imgcodecs.imdecode(new MatOfByte(bytes), Imgcodecs.CV_LOAD_IMAGE_UNCHANGED);
//
//                Mat dst = FrameProcessor.resize(orig);
//
//                byte[] dstBytes = new byte[Math.toIntExact(dst.total()*dst.channels())];
//                dst.get(0, 0, dstBytes);
//
//                Log.i("BEFORE DRAWING", bytes.length + " " + dst.width() + " " + dstBytes.length);

                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                Bitmap bmp32 = bmp.copy(Bitmap.Config.ARGB_8888, true);

                Mat tmp = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
                Utils.bitmapToMat(bmp32, tmp);

//                FrameProcessor.convertColor(tmp, tmp);

                try {
                    Log.i("CAPTURE", "Try getting hand histogram");
                    if (handHist != null){

                        Log.i("CAPTURE", "Got histogram");
                        HandDetector.drawPalmCentroid(tmp, handHist);
                    }
                } catch (Exception ex){
                    Log.i("CAPTURE", "error getting historgram: ");
                    ex.printStackTrace();
                }

                Utils.matToBitmap(tmp, bmp);

                image.close();
                final Bitmap bitmap = bmp;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
                        BitmapDrawable bd = ((BitmapDrawable)imageView.getDrawable());
                        if (bd != null) {
                            bd.getBitmap().recycle();
                        }
                        imageView.setImageBitmap(newBitmap);
                        bitmap.recycle();
                        Log.i("AFTER DRAWING", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));
                    }
                });
            }
        };

        // for preview to get HISTOGRAM
        histPreviewReaderListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                final Image image;
                image = reader.acquireLatestImage();
                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
                byte[] bytes = new byte[buffer.capacity()];
                buffer.get(bytes);
                Log.i("PREVIEW HIST FRAME", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));
                Bitmap bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                image.close();
                final Bitmap bitmap = bmp;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Matrix matrix = new Matrix();
                        matrix.postRotate(90);
                        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

                        // Draw custom area for recording histogram
                        FrameProcessor.drawHistRects(newBitmap);

                        BitmapDrawable bd = ((BitmapDrawable)imageView.getDrawable());
                        if (bd != null) {
                            bd.getBitmap().recycle();
                        }
                        imageView.setImageBitmap(newBitmap);
                        bitmap.recycle();
                        Log.i("AFTER HIST FRAME", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));
                    }
                });
            }
        };

        captureListener = new CameraCaptureSession.CaptureCallback() {
            @Override
            public void onCaptureCompleted(CameraCaptureSession session, CaptureRequest request, TotalCaptureResult result) {
                super.onCaptureCompleted(session, request, result);
                Toast.makeText(CameraDetectionPreview.this, "SAVED IMAGE" ,Toast.LENGTH_SHORT).show();
                createCameraPreview();
            }
        };

        Log.i("AFTER ACQUIRING CAMERA", "***");

        captureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isGettingHist) {
                    return;
                }
                if (isDetecting) {
                    mCameraDevice.close();
                    stopBackgroundThread();
                    isDetecting = false;
                } else {
                    isDetecting = true;
                    startBackgroundThread();
                    CameraManager myCameraManage = (CameraManager) getSystemService(CAMERA_SERVICE);
                    try {
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraDetectionPreview.this, CAMERA_PERMISSIONS, 1);
                        }

                        myCameraManage.openCamera(getCamera(myCameraManage), stateCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        previewHistButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isDetecting) {
                    return;
                }
                if (isGettingHist) {
                    mCameraDevice.close();
                    stopBackgroundThread();

                    // Get the image for histogram here.
                    BitmapDrawable bd = ((BitmapDrawable)imageView.getDrawable());
                    Bitmap btm = null;
                    if (bd != null) {
                        btm = bd.getBitmap();
                    }
                    if (btm != null) {
                        try {
                            Bitmap bmp32 = btm.copy(Bitmap.Config.ARGB_8888, true);
                            Mat tmp = new Mat (btm.getWidth(), btm.getHeight(), CvType.CV_8UC1);
                            Utils.bitmapToMat(bmp32, tmp);
                            handHist = FrameProcessor.getHandHist(tmp);

                        } catch (Exception ex){
                            Log.i("GETTING HISTOGRAM", ex.toString());

                            ex.printStackTrace();
                        }
                    }

                    isGettingHist = false;
                } else {
                    isGettingHist = true;
                    startBackgroundThread();
                    CameraManager myCameraManage = (CameraManager) getSystemService(CAMERA_SERVICE);
                    try {
                        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(CameraDetectionPreview.this, CAMERA_PERMISSIONS, 1);
                        }

                        myCameraManage.openCamera(getCamera(myCameraManage), stateCallback, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    private void captureCamera() {
        Log.i("IMAGE CAPTURED START", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraDevice.getId());
            int sensorOrientation = 0;
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
                sensorOrientation = characteristics.get(CameraCharacteristics.SENSOR_ORIENTATION);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = 640;
                height = 480;
            }

            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 20);

            if (isDetecting) {
                reader.setOnImageAvailableListener(readerListener, backgroundHandler);
            } else if (isGettingHist) {
                reader.setOnImageAvailableListener(histPreviewReaderListener, backgroundHandler);
            }


            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
//            captureBuilder.set(CaptureRequest.EDGE_MODE,
//                    CaptureRequest.EDGE_MODE_OFF);
//            captureBuilder.set(
//                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
//                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
//            captureBuilder.set(
//                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
//                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF);
//            captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE,
//                    CaptureRequest.NOISE_REDUCTION_MODE_OFF);
//            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
//                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
//            captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(20, 20));
//
//            captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
//            captureBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);
            int rotation = getWindowManager().getDefaultDisplay().getRotation();
            int orientation = (ORIENTATIONS.get(rotation) + sensorOrientation + 270) % 360;
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, orientation);
            captureBuilder.addTarget(reader.getSurface());
//            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


            mCameraDevice.createCaptureSession(Arrays.asList(reader.getSurface()), new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(CameraCaptureSession session) {
                    try {
                        session.setRepeatingRequest(captureBuilder.build(), null, backgroundHandler);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }
                @Override
                public void onConfigureFailed(CameraCaptureSession session) {
                }
            }, backgroundHandler);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    protected void createCameraPreview() {
        captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);
        try {
            mCameraCaptureSession.setRepeatingRequest(captureRequestBuilder.build(), null, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.e("RESUME", "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void stopBackgroundThread() {
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void startBackgroundThread() {
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }

    private String getCamera(CameraManager manager) {
        try {
            for (String cameraId : manager.getCameraIdList()) {
                CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraId);
                int cOrientation = characteristics.get(CameraCharacteristics.LENS_FACING);
                if (cOrientation == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId;
                }
            }
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onStart() {
        super.onStart();
    }


    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            CameraDetectionPreview.this.mCameraDevice = camera;
            CameraDetectionPreview.this.captureCamera();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {

        }

        @Override
        public void onError(CameraDevice camera, int error) {

        }
    };


    // gesture detection
    private MotionDetector gestureDetector;
//    private Gesture initGesture;
//    private Point[] posBuffer;
//    private int maxIndex;
//    private int currentIndex;

}

