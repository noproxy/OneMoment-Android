package co.yishun.onemoment.app.ui.view.shoot;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.Toast;

import java.io.File;

import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;
import co.yishun.onemoment.app.ui.view.shoot.filter.FilterManager.FilterType;
import co.yishun.onemoment.app.ui.view.shoot.video.EncoderConfig;

/**
 * Created by Carlos on 2015/10/13.
 */
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


    public CameraGLSurfaceView(Context context) {
        super(context);
        init();
    }

    public CameraGLSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;

        if (preferExternal && Environment.MEDIA_MOUNTED.equals(
                Environment.getExternalStorageState())) {
            appCacheDir = getExternalDirectory(context);
        }

        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }

        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            Log.d(FileUtil.class.getName(),
                    "Can't define system cache directory! use " + cacheDirPath);
            appCacheDir = new File(cacheDirPath);
        }

        return appCacheDir;
    }

    private static File getExternalDirectory(Context context) {

        File cacheDir = context.getExternalCacheDir();
        if (cacheDir != null && !cacheDir.exists()) {
            if (!cacheDir.mkdirs()) {
                Log.d(FileUtil.class.getName(), "无法创建SDCard cache");
                return null;
            }

            //try {
            //    new File(cacheDir, ".nomedia").createNewFile();
            //} catch (IOException e) {
            //    Log.d(FileUtil.class.getName(), "无法创建 .nomedia 文件");
            //}
        }

        return cacheDir;
    }

    private void init() {
        initHandler();

        setEGLContextClientVersion(2);

        mCameraRenderer = new CameraRecordRender(getContext(), mBackgroundHandler);
        setRenderer(mCameraRenderer);
        setRenderMode(RENDERMODE_WHEN_DIRTY);

        initFlashlightAndCamera();


        this.setOnClickListener(v -> {
            mCameraRenderer.nextFilter();
            Toast.makeText(getContext(), "clicked", Toast.LENGTH_SHORT).show();
        });
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
        Log.e(TAG, "front camera enable: " + mHasFrontCamera);
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
        Log.i(TAG, "release camera:" + camera);
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
        Log.i(TAG, "send msg: STOP");
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
                Log.e(TAG, "exception when set flash on! ", e);
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
            Log.i(TAG, "locked to setup camera");
            innerReleaseCamera();
            camera = Camera.open(mIsBackCamera ? mCameraId.back : mCameraId.front);
            final Camera.Parameters parameters = camera.getParameters();
            mSize = CameraUtil.getOptimalPreviewSize(parameters.getSupportedPreviewSizes(), Constants.VIDEO_WIDTH, Constants.VIDEO_HEIGHT);
            int[] fps = CameraUtil.getOptimalPreviewFpsRange(parameters.getSupportedPreviewFpsRange(), Constants.VIDEO_FPS);

            parameters.setPreviewSize(mSize.width, mSize.height);
            parameters.setPreviewFpsRange(fps[0], fps[1]);
            camera.setParameters(parameters);
            camera.setDisplayOrientation(90);
            Log.i(TAG, "setCamera, w: " + mSize.width + " h: " + mSize.height);


            try {
                camera.setPreviewTexture(surfaceTexture);
                mCameraRenderer.setCameraPreviewSize(mSize.width, mSize.height);

                camera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
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

    @Override
    public boolean isBackCamera() {
        return mIsBackCamera;
    }

    @Override
    public void record(Callback recordStartCallback, Consumer<File> recordEndConsumer) {
        Handler uiHandler = new Handler(Looper.getMainLooper());
        File file = new File(getCacheDirectory(getContext(), true), "video-" + System.currentTimeMillis() + ".mp4");
        Log.i(TAG, file.toString());
        queueEvent(() -> mCameraRenderer.setEncoderConfig(new EncoderConfig(file, 480, 480, 1024 * 1024)));
        queueEvent(() -> {
            mCameraRenderer.setRecordingEnabled(true);
            uiHandler.post(recordStartCallback::call);
            postDelayed(() -> {
                mCameraRenderer.setRecordingEnabled(false);
                uiHandler.post(() -> recordEndConsumer.accept(file));
            }, 1200);
        });
    }

    public void changeFilter(FilterType filterType) {
        mCameraRenderer.changeFilter(filterType);
    }

    public class CameraHandler extends Handler {
        public static final int START = 1001;
        public static final int STOP = 1002;
        public static final int RESUME = 1003;

        public CameraHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case CameraHandler.START:
                    mSurfaceTexture = (SurfaceTexture) msg.obj;
                    if (mSurfaceTexture != null) {
                        mSurfaceTexture.setOnFrameAvailableListener(CameraGLSurfaceView.this);
                        setupCamera(mSurfaceTexture);
                    } else {
                        Log.e(TAG, "surfaceTexture Null!!");
                    }
                    break;
                case CameraHandler.RESUME:
                    mSurfaceTexture.setOnFrameAvailableListener(CameraGLSurfaceView.this);
                    setupCamera(mSurfaceTexture);
                    break;
                case CameraHandler.STOP:
                    innerReleaseCamera();
                    break;
                default:
                    break;
            }
        }
    }
}
