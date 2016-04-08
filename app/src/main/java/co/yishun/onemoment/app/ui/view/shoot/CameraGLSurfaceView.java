package co.yishun.onemoment.app.ui.view.shoot;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.view.MotionEvent;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;
import co.yishun.onemoment.app.ui.view.shoot.filter.FilterManager.FilterType;
import co.yishun.onemoment.app.ui.view.shoot.video.EncoderConfig;

import static co.yishun.onemoment.app.LogUtil.e;
import static co.yishun.onemoment.app.LogUtil.i;

/**
 * Created by Carlos on 2015/10/13.
 */
@TargetApi(18)
public class CameraGLSurfaceView extends SquareGLSurfaceView implements SurfaceTexture.OnFrameAvailableListener, IShootView {
    private static final String TAG = "CameraGLSurfaceView";
    private final Object mLock = new Object();
    protected boolean mHasFrontCamera;
    protected boolean mHasFlash;
    protected boolean mIsBackCamera = true;
    private HandlerThread mHandlerThread;
    private CameraRecordRender mCameraRenderer;
    private CameraHandler mBackgroundHandler;
    private Camera camera = null;
    private CameraId mCameraId;
    private Camera.Size mSize;
    private SurfaceTexture mSurfaceTexture;
    private File file;
    private Consumer<File> onEndListener;
    private float mPressedX;
    private float mPressedY;
    private float mMoveLimit;
    private float mSlideLimit;
    private boolean mMoved = false;
    private OnFilterChangeListener mFilterListener;

    private Camera.AutoFocusCallback myAutoFocusCallback = (success, camera1) -> {
        if (success)
            camera1.cancelAutoFocus();
    };
    private SecurityExceptionHandler mExceptionHandler;

    public CameraGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        initHandler();

        setEGLContextClientVersion(2);

        file = FileUtil.getVideoCacheFile(getContext());
        mCameraRenderer = new CameraRecordRender(getContext(), mBackgroundHandler, new EncoderConfig(file.getPath(), 480, 480, 1024 * 1024));
        setRenderer(mCameraRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        initFlashlightAndCamera();
        mMoveLimit = getResources().getDimension(R.dimen.camera_surface_view_move);
        mSlideLimit = getResources().getDimension(R.dimen.camera_surface_view_slide);
    }

    public void setFilterListener(OnFilterChangeListener listener) {
        mFilterListener = listener;
    }

    public void doTouchFocus(final Rect tfocusRect) {
        try {
            List<Camera.Area> focusList = new ArrayList<Camera.Area>();
            Camera.Area focusArea = new Camera.Area(tfocusRect, 1000);
            focusList.add(focusArea);

            Camera.Parameters param = camera.getParameters();
            param.setFocusAreas(focusList);
            param.setMeteringAreas(focusList);
            camera.setParameters(param);

            camera.autoFocus(myAutoFocusCallback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mPressedX = event.getX();
                mPressedY = event.getY();
                mMoved = false;
                return true;
            case MotionEvent.ACTION_MOVE:
                if (Math.abs(event.getX() - mPressedX) > mMoveLimit || Math.abs(event.getY() - mPressedY) > mMoveLimit)
                    mMoved = true;
                return true;
            case MotionEvent.ACTION_UP:
                if (!mMoved) {
                    float x = event.getX();
                    float y = event.getY();
                    Rect touchRect = new Rect((int) (x - 100), (int) (y - 100), (int) (x + 100), (int) (y + 100));
                    final Rect targetFocusRect = new Rect(touchRect.left * 2000 / this.getWidth() - 1000, touchRect.top * 2000 / this.getHeight() - 1000, touchRect.right * 2000 / this.getWidth() - 1000, touchRect.bottom * 2000 / this.getHeight() - 1000);
                    doTouchFocus(targetFocusRect);
                } else {
                    if (event.getX() - mPressedX > mSlideLimit) {
                        mCameraRenderer.preFilter();
                    } else if (event.getX() - mPressedX < -mSlideLimit) {
                        mCameraRenderer.nextFilter();
                    }

                    if (mFilterListener != null) {
                        mFilterListener.onFilterIndexChange(mCameraRenderer.getCurrentFilterIndex());
                    }
                }
                return true;
        }
        return false;
    }

    public void onDestroy() {
        mBackgroundHandler.removeCallbacksAndMessages(null);
        if (!mHandlerThread.isInterrupted()) {
            try {
                mHandlerThread.quit();
                mHandlerThread.interrupt();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void initHandler() {
        mHandlerThread = new HandlerThread("CameraHandlerThread");
        mHandlerThread.start();
        mBackgroundHandler = new CameraHandler(mHandlerThread.getLooper());
    }

    private void initFlashlightAndCamera() {
        PackageManager packageManager = getContext().getPackageManager();

        mHasFlash = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH);
        mHasFrontCamera = packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_FRONT);
        mCameraId = CameraUtil.findCameraId();
        mHasFrontCamera = mHasFrontCamera && mCameraId.front != -1;
        e(TAG, "front camera enable: " + mHasFrontCamera);
    }

    @Override
    public void onPause() {
        mBackgroundHandler.removeCallbacksAndMessages(null);
        releaseCamera();
        queueEvent(() -> {
            // 跨进程 清空 Renderer数据
            mCameraRenderer.notifyPausing();
        });

        super.onPause();
    }

    private void innerReleaseCamera() {
        i(TAG, "release camera:" + camera);
        if (camera != null) {
            synchronized (mLock) {
                try {
                    camera.setPreviewCallback(null);
                    camera.stopPreview();
                    camera.release();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    camera = null;
                }
            }
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        requestRender();
    }

    @Override
    public void releaseCamera() {
        i(TAG, "send msg: STOP");
        this.mBackgroundHandler.sendEmptyMessage(CameraHandler.STOP);
    }

    @Override
    public void setFlashlightOn(boolean isOn) {
        if (isFlashlightAvailable()) {
            try {
                Camera.Parameters p = camera.getParameters();
                p.setFlashMode(isOn ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
                camera.setParameters(p);
            } catch (Exception e) {
                LogUtil.e(TAG, "exception when set flash on! ", e);
            }
        }
    }

    @Override
    public void switchCamera(boolean isBack) {
        mIsBackCamera = isBack;
        mBackgroundHandler.sendEmptyMessage(CameraHandler.RESUME);
    }

    protected void setupCamera(SurfaceTexture surfaceTexture) {
        synchronized (mLock) {
            try {
                i(TAG, "locked to setup camera");
                innerReleaseCamera();
                camera = Camera.open(mIsBackCamera ? mCameraId.back : mCameraId.front);
                final Camera.Parameters parameters = camera.getParameters();
                mSize = CameraUtil.getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT);

                parameters.setPreviewSize(mSize.width, mSize.height);
                camera.setParameters(parameters);
                camera.setDisplayOrientation(90);
                i(TAG, "setCamera, w: " + mSize.width + " h: " + mSize.height);


                try {
                    camera.setPreviewTexture(surfaceTexture);
                    mCameraRenderer.setCameraPreviewSize(mSize.width, mSize.height);

                    camera.startPreview();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } catch (RuntimeException e) {
                throw new SecurityException("catch RuntimeException to exception handler", e);
            }
        }
    }

    @Override
    public boolean isFlashlightAvailable() {
        i(TAG, "mIsBackCamera: " + mIsBackCamera + ", mHasFlash " + mHasFlash);
        return mIsBackCamera && mHasFlash;
    }

    @Override
    public boolean isFrontCameraAvailable() {
        return mHasFrontCamera;
    }

    @Override
    public boolean isBackCamera() {
        return mIsBackCamera;
    }

    @Override
    public void record(Callback recordStartCallback, Consumer<File> recordEndConsumer) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        //        file = new File(getCacheDirectory(getContext(), true), "video-" + System.currentTimeMillis() + ".mp4");
        i(TAG, file.toString());
        //        queueEvent(() -> mCameraRenderer.setEncoderConfig(new EncoderConfig(file, 480, 480, 1024 * 1024)));
        queueEvent(() -> {
            mCameraRenderer.setRecordingEnabled(true);
            uiHandler.post(recordStartCallback::call);
            postDelayed(() -> {
                mCameraRenderer.setRecordingEnabled(false);
                onEndListener = recordEndConsumer;
            }, 1200);
        });
    }

    @Override
    public void setSecurityExceptionHandler(SecurityExceptionHandler exceptionHandler) {
        mExceptionHandler = exceptionHandler;
    }

    private void onHandler(SecurityException e) {
        if (mExceptionHandler != null) {
            mExceptionHandler.onHandler(e);
        } else {
            throw new SecurityException(e);
        }
    }

    private void onEnd() {
        if (onEndListener != null) {
            onEndListener.accept(file);
        }
    }

    public void changeFilter(FilterType filterType) {
        mCameraRenderer.changeFilter(filterType);
    }

    public interface OnFilterChangeListener {
        void onFilterIndexChange(int index);
    }

    public class CameraHandler extends Handler {
        public static final int START = 1001;
        public static final int END = 1004;
        public static final int STOP = 1002;
        public static final int RESUME = 1003;

        public CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            try {
                switch (msg.what) {
                    case CameraHandler.START:
                        mSurfaceTexture = (SurfaceTexture) msg.obj;
                        if (mSurfaceTexture != null) {
                            mSurfaceTexture.setOnFrameAvailableListener(CameraGLSurfaceView.this);
                            setupCamera(mSurfaceTexture);
                        } else {
                            e(TAG, "surfaceTexture Null!!");
                        }
                        break;
                    case CameraHandler.RESUME:
                        mSurfaceTexture.setOnFrameAvailableListener(CameraGLSurfaceView.this);
                        setupCamera(mSurfaceTexture);
                        break;
                    case CameraHandler.STOP:
                        innerReleaseCamera();
                        break;
                    case END:
                        onEnd();
                        break;
                    default:
                        break;
                }
            } catch (SecurityException e) {
                onHandler(e);
            }
        }
    }
}
