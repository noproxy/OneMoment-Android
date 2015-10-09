package co.yishun.onemoment.app.ui.view.shoot;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
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
    private PackageManager packageManager = getContext().getPackageManager();
    private Camera camera;
    private Camera.Size mSize;
    private boolean needPreview = false;
    private boolean mHasFrontCamera;
    private CameraId mCameraId;
    private boolean mHasFlash;
    private boolean mIsBackCamera = true;

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

    private void init() {
        Log.i(TAG, "ShootView init");
//        ((AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE)).setStreamMute(AudioManager.STREAM_SYSTEM, true);
        initCamera();
        initFlash();
    }

    @Override
    public boolean isBackCamera() {
        return mIsBackCamera;
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

    @Override
    public void setFlashlightOn(boolean isOn) {
        if (isFlashlightAvailable()) {
            try {
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(isOn ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(p);
            } catch (Exception e) {
                Log.e(TAG, "exception when set flash on! ", e);
            }
        }
    }

    @Override
    public void setBackCameraOn(boolean isBack) {
        releaseCamera();
        camera = Camera.open(isBack ? mCameraId.back : mCameraId.front);
        mIsBackCamera = isBack;
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

    @Override
    public boolean isFlashlightAvailable() {
        return mIsBackCamera && mHasFlash;
    }

    @Override
    public boolean isFrontCameraAvailable() {
        return mHasFrontCamera;
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
        mCameraId = CameraUtil.findCameraId();

        mHasFrontCamera = mHasFrontCamera && mCameraId.front != -1;
        Log.e(TAG, "front camera enable: " + mHasFrontCamera);

        setBackCameraOn(true);// load camera first
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


    private void initFlash() {
        mHasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
    }
}
