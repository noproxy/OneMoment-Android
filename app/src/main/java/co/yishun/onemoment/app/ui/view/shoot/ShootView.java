package co.yishun.onemoment.app.ui.view.shoot;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.AudioManager;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import java.io.IOException;

import co.yishun.onemoment.app.config.Constants;

/**
 * Created by Carlos on 2015/10/6.
 */
public class ShootView extends TextureView implements IShootView {
    private static final String TAG = "ShootView";
    Camera camera;
    PackageManager packageManager = getContext().getPackageManager();
    CameraSwitchController mCameraSwitchController;
    FlashlightController mFlashlightController;
    private Camera.Size mSize;
    private boolean needPreview = false;
    private boolean mHasFrontCamera;
    private int FRONT_ID = -1;
    private int BACK_ID = -1;
    private boolean mHasFlash;

    public ShootView(Context context) {
        super(context);
        init();
    }

    public ShootView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ShootView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ShootView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    @Override
    public void setFlashlightController(FlashlightController controller) {
        mFlashlightController = controller;
    }

    @Override
    public void setCameraSwitchController(CameraSwitchController controller) {
        mCameraSwitchController = controller;
    }

    private void init() {
        Log.i(TAG, "ShootView init");
        ((AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_SYSTEM, true);
        initCamera();
        initFlash();
    }

    private void applyTransform() {
        Matrix mat = CameraUtil.calculatePreviewMatrix(this, mSize);
        this.setTransform(mat);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        int size = Math.min(
                getMeasuredWidth(),
                getMeasuredHeight()
        );

        Log.i(TAG, "size: " + size);
        setMeasuredDimension(size, size);
    }

    public void releaseCamera() {
        if (camera != null) {
            camera.stopPreview();
//            camera.unlock();
            camera.release();
        }
    }

    private void startPreview() throws IOException {
        Log.i(TAG, "ShootView preview");
        if (isAvailable()) {
            camera.setPreviewTexture(this.getSurfaceTexture());
            camera.startPreview();
            applyTransform();
        } else {
            Log.i(TAG, "not available");
            needPreview = true;
        }
    }

    private void initCamera() {
        mHasFrontCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        findCameraId();
        if (mHasFrontCamera && FRONT_ID == -1) {
            Log.e(TAG, "has front camera, but not found");
            mHasFrontCamera = false;
        }
        setCurrentCamera(false);
        this.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                try {
                    if (needPreview) {
                        camera.setPreviewTexture(getSurfaceTexture());
                        camera.startPreview();
                        applyTransform();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

            }

            @Override
            public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
                return false;
            }

            @Override
            public void onSurfaceTextureUpdated(SurfaceTexture surface) {

            }
        });
    }

    private void findCameraId() {
        // Find the total number of cameras available
        int mNumberOfCameras = Camera.getNumberOfCameras();
        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                BACK_ID = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                FRONT_ID = i;
            }
        }
    }

    @Override
    public void setCurrentCamera(boolean isFront) {
        releaseCamera();
        camera = Camera.open(isFront ? FRONT_ID : BACK_ID);
        final Camera.Parameters parameters = camera.getParameters();
        mSize = CameraUtil.getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT);
        int[] fps = CameraUtil.getOptimalPreviewFpsRange(parameters.getSupportedPreviewFpsRange(), Constants.VIDEO_FPS);

        parameters.setPreviewSize(mSize.width, mSize.height);
        parameters.setPreviewFpsRange(fps[0], fps[1]);
        camera.setParameters(parameters);
        camera.setDisplayOrientation(90);
        Log.i(TAG, "setCamera, w: " + mSize.width + " h: " + mSize.height);

        try {
            startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initFlash() {
        mHasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }


    /**
     * @return whether success
     */
    protected boolean setFlashlight(boolean isOpen) {
        if (mCameraSwitchController.isFrontCamera() || !mHasFlash)
            return !isOpen;
        else try {
            Camera.Parameters p = camera.getParameters();
            p.setFlashMode(isOpen ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            return true;
        } catch (Exception e) {
            Log.e(TAG, "exception when set flash on! ", e);
            try {
                return isOpen == camera.getParameters().getFlashMode().equals(Camera.Parameters.FLASH_MODE_TORCH);
            } catch (Exception e1) {
                Log.e(TAG, "exception when get flash mode", e);
            }
            return !isOpen;
        }
    }

}
