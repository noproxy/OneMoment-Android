package co.yishun.onemoment.app.ui.view.shoot;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.TextureView;

import java.io.File;
import java.io.IOException;

import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;

/**
 * Created by Carlos on 2015/10/6.
 */
public class ShootView extends TextureView implements IShootView, MediaRecorder.OnInfoListener {
    private static final String TAG = "ShootView";
    MediaRecorder mRecorder;
    private PackageManager packageManager = getContext().getPackageManager();
    private Camera camera;
    private Camera.Size mSize;
    private boolean needPreview = false;
    private boolean mHasFrontCamera;
    private CameraId mCameraId;
    private boolean mHasFlash;
    private boolean mIsBackCamera = true;
    private Consumer<File> mRecordEndConsumer;
    private Callback mRecordStartCallback;
    private File mFile = new File(CameraGLSurfaceView.getCacheDirectory(getContext(), true), System.currentTimeMillis() + "test.mp4");
    private HandlerThread mHandlerThread;
    private RecordHandler mBackgroundHandler;

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

        initHandler();
        initCamera();
        initFlash();
    }

    private void initHandler() {
        mHandlerThread = new HandlerThread("RecordHandlerThread");
        mHandlerThread.start();
        mBackgroundHandler = new RecordHandler(mHandlerThread.getLooper());
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
    public void switchCamera(boolean isBack) {
        mBackgroundHandler.sendMessage(mBackgroundHandler.obtainMessage(RecordHandler.PREPARE, isBack));
    }

    private void innerSwitchCamera(boolean isBack) {
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
//            prepare();
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

        innerSwitchCamera(true);// load camera first
        this.setSurfaceTextureListener(new SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
                try {
                    if (needPreview) {
                        Log.d(TAG, "camera set texture");
                        camera.setPreviewTexture(getSurfaceTexture());
                        camera.startPreview();
                        applyTransform();
                        Log.d(TAG, "camera set texture finish");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.d(TAG, "camera set texture fail");
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

    private void prepare() {
        CamcorderProfile profile = getProfile();
        profile.videoFrameRate = 30;
        profile.audioCodec = MediaRecorder.AudioEncoder.AAC;
        profile.videoCodec = MediaRecorder.VideoEncoder.H264;
        profile.fileFormat = MediaRecorder.OutputFormat.MPEG_4;

        mRecorder = new MediaRecorder();
        Log.d(TAG, "camera unlock");
        camera.unlock();
        mRecorder.setCamera(camera);
        mRecorder.setAudioSource(MediaRecorder.AudioSource.DEFAULT);
        mRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
        mRecorder.setOutputFile(mFile.getPath());//TODO set output file
        mRecorder.setProfile(profile);
        mRecorder.setOrientationHint(isBackCamera() ? 90 : 270);
        mRecorder.setMaxDuration(1200);
        mRecorder.setOnInfoListener(this);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(TAG, "MediaRecorder prepare exception", e);
        }
    }

    private void onRecordStart() {
        if (mRecordStartCallback != null) {
            mRecordStartCallback.call();
        }
    }

    private void onRecordEnd(File file) {
        if (mRecordEndConsumer != null) {
            mRecordEndConsumer.accept(file);
        }
    }

    @Override
    public void record(Callback recordStartCallback, Consumer<File> recordEndConsumer) {
        mRecordStartCallback = recordStartCallback;
        mRecordEndConsumer = recordEndConsumer;
        mBackgroundHandler.sendEmptyMessage(RecordHandler.START);
    }

    @Override
    public void onInfo(MediaRecorder mr, int what, int extra) {
        switch (what) {
            case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
                onRecordEnd(mFile);
                break;
        }
    }

    private CamcorderProfile getProfile() {
        CamcorderProfile profile = null;
        int[] allProfiles = {
                CamcorderProfile.QUALITY_480P,
                CamcorderProfile.QUALITY_TIME_LAPSE_480P,

                CamcorderProfile.QUALITY_720P,
                CamcorderProfile.QUALITY_TIME_LAPSE_720P,
                CamcorderProfile.QUALITY_LOW,
                CamcorderProfile.QUALITY_TIME_LAPSE_LOW,
                CamcorderProfile.QUALITY_QCIF,
                CamcorderProfile.QUALITY_TIME_LAPSE_QCIF,
                CamcorderProfile.QUALITY_CIF,
                CamcorderProfile.QUALITY_TIME_LAPSE_CIF,

                CamcorderProfile.QUALITY_HIGH,
                CamcorderProfile.QUALITY_TIME_LAPSE_HIGH,
                CamcorderProfile.QUALITY_1080P,
                CamcorderProfile.QUALITY_TIME_LAPSE_1080P,

        };
        for (int oneProfile : allProfiles) {
            if (CamcorderProfile.hasProfile(oneProfile)) {
                profile = CamcorderProfile.get(oneProfile);
                break;
            }
        }
        if (profile == null) throw new IllegalArgumentException("no profile at all!!");
        return profile;
    }

    public class RecordHandler extends Handler {
        public static final int PREPARE = 1001;
        public static final int START = 1002;
        public static final int STOP = 1003;

        public RecordHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(final Message msg) {
            switch (msg.what) {
                case RecordHandler.PREPARE:
                    boolean isBack = (boolean) msg.obj;
                    innerSwitchCamera(isBack);
                    break;
                case RecordHandler.START:
                    mRecorder.start();
                    onRecordStart();
                    break;
                case RecordHandler.STOP:
                    mRecorder.release();

                    this.removeCallbacksAndMessages(null);
                    if (!mHandlerThread.isInterrupted()) {
                        try {
                            mHandlerThread.quit();
                            mHandlerThread.interrupt();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }
}
