package co.yishun.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

/**
 * Created on 2015/10/29.
 */
public class OMVideoView extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "PlaySurfaceView";
    private SurfaceHolder mHolder;
    private OMVideoPlayer mVideoPlayer;
    private boolean surfaceCreate = false;
    private boolean holderSet = false;

    public OMVideoView(Context context) {
        super(context);
        init();
    }

    public OMVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OMVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OMVideoView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        Log.d(TAG, "init");
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void setPlayListener(OMVideoPlayer.PlayListener listener) {
        mVideoPlayer = new OMVideoPlayer(getContext());
        Log.d(TAG, "set player");
        mVideoPlayer.setListener(listener);
        if (!holderSet && surfaceCreate) {
            mVideoPlayer.setDisplay(mHolder);
        }
    }

    public void setVideoRes(Uri videoUri) {
        mVideoPlayer.setVideoRes(videoUri);
    }

    public void start() {
        mVideoPlayer.start();
    }

    public void pause() {
        mVideoPlayer.pause();
    }

    public boolean isPlaying() {
        return mVideoPlayer.isPlaying();
    }

    public void reset() {
        mVideoPlayer.reset();
    }

    public void release() {
        mVideoPlayer.release();
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surface create");
        surfaceCreate = true;
        if (mVideoPlayer != null) {
            mVideoPlayer.setDisplay(holder);
            holderSet = true;
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surface change");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surface destroy");
        surfaceCreate = false;
        if (mVideoPlayer != null) {
            mVideoPlayer.setDisplay(null);
            holderSet = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

}
