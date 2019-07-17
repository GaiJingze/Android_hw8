package com.bytedance.camera.demo;

import android.content.Context;
import android.graphics.Rect;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_IMAGE;
import static com.bytedance.camera.demo.utils.Utils.MEDIA_TYPE_VIDEO;
import static com.bytedance.camera.demo.utils.Utils.getOutputMediaFile;

public class CustomCameraActivity extends AppCompatActivity {

    private SurfaceView mSurfaceView;
    private Camera mCamera=null;

    private int CAMERA_TYPE = Camera.CameraInfo.CAMERA_FACING_BACK;

    private boolean isRecording = false;
    private int tt;
    private int rotationDegree = 0;
    private boolean focus=false;

    Button btn;
    Camera.AutoFocusCallback mAutoFocusCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_custom_camera);
        tt=Camera.CameraInfo.CAMERA_FACING_BACK;
        mSurfaceView = findViewById(R.id.img);
        mAutoFocusCallback= new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean success, Camera camera) {

            }
        };

            mSurfaceView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
               int areaX = (int) (event.getX() / mSurfaceView.getWidth() * 2000) - 1000; // 获取映射区域的X坐标
               int areaY = (int) (event.getY() / mSurfaceView.getWidth() * 2000) - 1000; // 获取映射区域的Y坐标
               // 创建Rect区域
               Rect focusArea = new Rect();
               focusArea.left = Math.max(areaX - 100, -1000); // 取最大或最小值，避免范围溢出屏幕坐标
               focusArea.top = Math.max(areaY - 100, -1000);
               focusArea.right = Math.min(areaX + 100, 1000);
               focusArea.bottom = Math.min(areaY + 100, 1000);
               // 创建Camera.Area
               Camera.Area cameraArea = new Camera.Area(focusArea, 1000);
               List<Camera.Area> meteringAreas = new ArrayList<Camera.Area>();
               List<Camera.Area> focusAreas = new ArrayList<Camera.Area>();
               Camera.Parameters mParameters = mCamera.getParameters();
               if (mParameters.getMaxNumMeteringAreas() > 0) {
                   meteringAreas.add(cameraArea);
                   focusAreas.add(cameraArea);
               }
               mParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO); // 设置对焦模式
               mParameters.setFocusAreas(focusAreas); // 设置对焦区域
               mParameters.setMeteringAreas(meteringAreas); // 设置测光区域
               try {
                   mCamera.cancelAutoFocus(); // 每次对焦前，需要先取消对焦
                   mCamera.setParameters(mParameters); // 设置相机参数
                   mCamera.autoFocus(mAutoFocusCallback); // 开启对焦
               } catch (Exception e) {
               }
               return false;
            }
        });



        SurfaceHolder surfaceHolder = mSurfaceView.getHolder();
        surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surfaceHolder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mCamera = (Camera) getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
                startPreview();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                mCamera.stopPreview();
                mCamera.release();
                mCamera=null;
            }
        });
        mSurfaceView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            }
        });

        findViewById(R.id.btn_picture).setOnClickListener(v -> {
            mCamera.takePicture(null,null,mPicture);
            Toast.makeText(CustomCameraActivity.this,"succeed",Toast.LENGTH_SHORT).show();
        });
        btn=findViewById(R.id.btn_record);
        findViewById(R.id.btn_record).setOnClickListener(v -> {
            if (isRecording) {
                releaseMediaRecorder();
            } else {
                prepareVideoRecorder();
            }
        });

        findViewById(R.id.btn_facing).setOnClickListener(v -> {
            releaseCameraAndPreview();
            if(tt== Camera.CameraInfo.CAMERA_FACING_BACK)
            {
                mCamera=getCamera(Camera.CameraInfo.CAMERA_FACING_FRONT);
            }
            else
            {
                mCamera=getCamera(Camera.CameraInfo.CAMERA_FACING_BACK);
            }
            startPreview();
        });

        findViewById(R.id.btn_zoom).setOnClickListener(v -> {
            if(focus)
            {
                focus=false;
                Toast.makeText(CustomCameraActivity.this,"close",Toast.LENGTH_SHORT).show();
                Camera.Parameters parameter = mCamera.getParameters();
                parameter.setFocusMode(Camera.Parameters.FOCUS_MODE_EDOF);
                mCamera.setParameters(parameter);
            }
            else
                {
                focus=true;
                Toast.makeText(CustomCameraActivity.this,"open",Toast.LENGTH_SHORT).show();
                Camera.Parameters parameter = mCamera.getParameters();
                parameter.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
                mCamera.setParameters(parameter);
            }
        });

    }


    public Camera getCamera(int position) {
        CAMERA_TYPE = position;
        if (mCamera != null) {
            releaseCameraAndPreview();
        }
        Camera cam = Camera.open(position);
        rotationDegree = getCameraDisplayOrientation(position);
        cam.setDisplayOrientation(rotationDegree);
        tt=position;
        Camera.Parameters parameter = cam.getParameters();
        size=getOptimalPreviewSize(parameter.getSupportedPreviewSizes(),mSurfaceView.getWidth(),mSurfaceView.getHeight());
        parameter.setPreviewSize(size.width,size.height);
        cam.setParameters(parameter);
        focus=false;
        return cam;
    }


    private static final int DEGREE_90 = 90;
    private static final int DEGREE_180 = 180;
    private static final int DEGREE_270 = 270;
    private static final int DEGREE_360 = 360;

    private int getCameraDisplayOrientation(int cameraId) {
        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);
        int rotation = getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = DEGREE_90;
                break;
            case Surface.ROTATION_180:
                degrees = DEGREE_180;
                break;
            case Surface.ROTATION_270:
                degrees = DEGREE_270;
                break;
            default:
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % DEGREE_360;
            result = (DEGREE_360 - result) % DEGREE_360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + DEGREE_360) % DEGREE_360;
        }
        return result;
    }


    private void releaseCameraAndPreview() {
        mCamera.setPreviewCallback(null);
        mCamera.stopPreview();
        mCamera.release();
        mCamera = null;
    }

    Camera.Size size;

    private void startPreview() {
        try {
            mCamera.setPreviewDisplay(mSurfaceView.getHolder());
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    private MediaRecorder mMediaRecorder;

    private void prepareVideoRecorder() {
        isRecording=true;
        mMediaRecorder = new MediaRecorder();
        mCamera.unlock();
        mMediaRecorder.setCamera(mCamera);
        btn.setText("STOP");
    }


    private void releaseMediaRecorder() {
        btn.setText("START");
        isRecording = false;
        mMediaRecorder.stop();
        mMediaRecorder.reset();
        mMediaRecorder.release();
        mMediaRecorder = null;
        mCamera.lock();
    }


    private Camera.PictureCallback mPicture = (data, camera) -> {
        File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
        if (pictureFile == null) {
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            fos.write(data);
            fos.close();
        } catch (IOException e) {
            Log.d("mPicture", "Error accessing file: " + e.getMessage());
        }
        mCamera.startPreview();
    };


    private Camera.Size getOptimalPreviewSize(List<Camera.Size> sizes, int w, int h) {
        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) h / w;

        if (sizes == null) return null;

        Camera.Size optimalSize = null;
        double minDiff = Double.MAX_VALUE;

        int targetHeight = Math.min(w, h);

        for (Camera.Size size : sizes) {
            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE) continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizes) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

}
