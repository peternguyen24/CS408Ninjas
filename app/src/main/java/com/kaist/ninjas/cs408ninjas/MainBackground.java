package com.kaist.ninjas.cs408ninjas;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.util.Log;
import android.util.Range;
import android.util.Size;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Timer;
import java.util.TimerTask;

public class MainBackground extends Service {

    private BroadcastReceiver backgroundReceiver;

    private CameraDevice mCameraDevice;
    private CaptureRequest.Builder captureRequestBuilder;
    private ImageReader.OnImageAvailableListener readerListener;
    private CameraCaptureSession.CaptureCallback captureListener;
    private ImageReader reader;

    private HandlerThread backgroundThread;
    private Handler backgroundHandler;

    private static final String[] CAMERA_PERMISSIONS = {
            Manifest.permission.CAMERA
    };

    private CameraDevice.StateCallback stateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(CameraDevice camera) {
            MainBackground.this.mCameraDevice = camera;
            Log.i("CAM STATE", "ASSIGN CAM");
            captureCamera();
        }

        @Override
        public void onDisconnected(CameraDevice camera) {
            Log.i("CAM STATE", "DISCONNECTED");
        }

        @Override
        public void onError(CameraDevice camera, int error) {
            Log.i("CAM STATE", "ERROR OCCURS");
        }
    };


    public MainBackground() {
        super();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        startBackgroundThread();
        receiverInit();
        bindNotification();
        cameraInit();
    }

    private void receiverInit() {
        this.backgroundReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                MainBackground.this.stopSelf();
            }
        };
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SEND);
        this.registerReceiver(this.backgroundReceiver, intentFilter);
    }

    private void cameraInit() {
        CameraManager myCameraManage = (CameraManager) getSystemService(CAMERA_SERVICE);

        readerListener = new ImageReader.OnImageAvailableListener() {
            @Override
            public void onImageAvailable(ImageReader reader) {
                Log.i("BEFORE DRAWING", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));

//                 String inputDir = getCacheDir().getAbsolutePath();  // use the cache directory for i/o
//                 String outputDir = getCacheDir().getAbsolutePath();
//                 FrameProcessor.test(inputDir,outputDir);

                final Image image;
                image = reader.acquireLatestImage();
//                ByteBuffer buffer = image.getPlanes()[0].getBuffer();
//                byte[] bytes = new byte[buffer.capacity()];
//                buffer.get(bytes);
               image.close();
//                Log.i("BEFORE DRAWING", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));
//                final Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        Matrix matrix = new Matrix();
//                        matrix.postRotate(90);
//                        Bitmap newBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
//                        BitmapDrawable bd = ((BitmapDrawable)imageView.getDrawable());
//                        if (bd != null) {
//                            bd.getBitmap().recycle();
//                        }
//                        imageView.setImageBitmap(newBitmap);
//                        bitmap.recycle();
//                        Log.i("AFTER DRAWING", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));
//                    }
//                });
            }
        };

        try {
            if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                Log.e("PERMISSION ERROR","NEED ACQUIRE PERMISSION CAMERA");
            } else {
                Log.e("PERMISSION ERROR","ALREADY ACQUIRED PERMISSION CAMERA");
            }
            String camId = getCamera(myCameraManage);
            if (camId == null) {
                Log.e("CAMERA PROBLEM","No Cam ID");
            }
            myCameraManage.openCamera(camId, stateCallback, backgroundHandler);
        } catch (CameraAccessException e) {
            e.printStackTrace();
        }

    }

    private void captureCamera() {
        Log.i("IMAGE CAPTURED START", ""+ new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS").format(Calendar.getInstance().getTime()));

        CameraManager cameraManager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(mCameraDevice.getId());
            Size[] jpegSizes = null;
            if (characteristics != null) {
                jpegSizes = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP).getOutputSizes(ImageFormat.JPEG);
            }
            int width = 640;
            int height = 480;
            if (jpegSizes != null && 0 < jpegSizes.length) {
                width = 640;
                height = 480;
            }

            reader = ImageReader.newInstance(width, height, ImageFormat.JPEG, 20);

            reader.setOnImageAvailableListener(readerListener, backgroundHandler);


            final CaptureRequest.Builder captureBuilder = mCameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE);
            captureBuilder.set(CaptureRequest.EDGE_MODE,
                    CaptureRequest.EDGE_MODE_OFF);
            captureBuilder.set(
                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE,
                    CaptureRequest.LENS_OPTICAL_STABILIZATION_MODE_ON);
            captureBuilder.set(
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE,
                    CaptureRequest.COLOR_CORRECTION_ABERRATION_MODE_OFF);
            captureBuilder.set(CaptureRequest.NOISE_REDUCTION_MODE,
                    CaptureRequest.NOISE_REDUCTION_MODE_OFF);
            captureBuilder.set(CaptureRequest.CONTROL_AF_TRIGGER,
                    CaptureRequest.CONTROL_AF_TRIGGER_CANCEL);
            captureBuilder.set(CaptureRequest.CONTROL_AE_TARGET_FPS_RANGE, new Range<>(20, 20));

            captureBuilder.set(CaptureRequest.CONTROL_AE_LOCK, true);
            captureBuilder.set(CaptureRequest.CONTROL_AWB_LOCK, true);
            captureBuilder.addTarget(reader.getSurface());
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO);


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

    private void bindNotification() {
        Intent broadcastIntent = new Intent(Intent.ACTION_SEND);
        PendingIntent pendingIntent =
                PendingIntent.getBroadcast(this, 0, broadcastIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification noti = new Notification.Builder(getApplicationContext())
                .setSmallIcon(R.drawable.ic_launcher_background)
                .setContentTitle("MAIN BG")
                .setContentText("Time now is " + Calendar.getInstance().getTime().toString())
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setOngoing(true)
                .build();
        NotificationManager notificationManager =
                (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(100, noti);
        }
    }

    private void cameraFinish() {
        mCameraDevice.close();
    }



    @Override
    public void onDestroy() {
        this.unregisterReceiver(this.backgroundReceiver);
        cameraFinish();
        stopBackgroundThread();
        super.onDestroy();
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

    @Override
    public IBinder onBind(Intent intent) {
        // Never bind this service
        return null;
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


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS:
                {
//                    String inputDir = getCacheDir().getAbsolutePath();  // use the cache directory for i/o
//                    String outputDir = getCacheDir().getAbsolutePath();
//                    intervalTask.doOnce(inputDir,outputDir);
                } break;
                default:
                {
                    super.onManagerConnected(status);
                } break;
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        if (!OpenCVLoader.initDebug()) {
            Log.d("OpenCV", "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0, this, mLoaderCallback);
        } else {
            Log.d("OpenCV", "OpenCV library found inside package. Using it!");
            mLoaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        return super.onStartCommand(intent, flags, startId);
    }
}
