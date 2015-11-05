package co.yishun.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

import co.yishun.library.resource.VideoResource;

/**
 * Created on 2015/10/29.
 */
public class OnemomentPlaySurfaceView extends SurfaceView implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener, MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    private static final String TAG = "[OPO]";
    private SurfaceHolder mHolder;
    private MediaPlayer mMediaPlayer;
    private MediaPlayer mNextPlayer;
    private VideoResource mVideoResource;
    private VideoResource mNextVideoResource;
    private boolean mHolderCreated = false;
    private boolean mHolderDestroyed = false;
    private boolean mMediaError = false;
    private boolean mStop;
    private PlayOneListener mOneListener;

    public OnemomentPlaySurfaceView(Context context) {
        super(context);
        init();
    }

    public OnemomentPlaySurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OnemomentPlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OnemomentPlaySurfaceView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    public void fistPrepare() {
        if (mVideoResource == null) {
            return;
        }
        try {
            Log.d(TAG, "first " + mVideoResource);
            mMediaPlayer = new MediaPlayer();
            if (mHolderCreated) {
                Log.d(TAG, "first media " + mMediaPlayer);
                mMediaPlayer.setDisplay(mHolder);
            } else {
                Log.d(TAG, "creating ");
            }
            mMediaPlayer.setDataSource(getContext(), mVideoResource.getVideoUri());
            mMediaPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void nextPrepare() {
        if (mNextVideoResource == null) {
            mNextPlayer = null;
            return;
        }
        try {
            Log.d(TAG, "create next");
            mNextPlayer = new MediaPlayer();
            mNextPlayer.setDataSource(getContext(), mNextVideoResource.getVideoUri());
            mNextPlayer.prepareAsync();
            mNextPlayer.setOnPreparedListener(this);
            mNextPlayer.setOnCompletionListener(this);
            mNextPlayer.setOnErrorListener(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (mMediaPlayer != null && !mMediaPlayer.isPlaying()) {
            mMediaPlayer.start();
        }
    }

    public void pause() {
        if (mMediaPlayer.isPlaying()) {
            mMediaPlayer.pause();
        }
    }

    public void stop() {
        mStop = true;
    }

    public void reset() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mNextPlayer != null) {
            mNextPlayer.release();
        }
        mMediaPlayer = null;
        mNextPlayer = null;
        fistPrepare();
    }

    public boolean isPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    public void setVideoResource(VideoResource videoResource) {
        mVideoResource = videoResource;
    }

    public void setNextVideoResource(VideoResource videoResource) {
        mNextVideoResource = videoResource;
    }

    public void setOneListener(PlayOneListener oneListener) {
        mOneListener = oneListener;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
//        mp.seekTo(0);
        Log.d(TAG, "set next");
        mMediaPlayer.setNextMediaPlayer(mNextPlayer);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "complete");
        mp.release();
        if (mNextPlayer == null || mp == mNextPlayer) {
            Log.d(TAG, "end");
            if (mOneListener != null) {
                mOneListener.onOneCompletion();
            }
        } else {
            mMediaPlayer = null;
            Log.d(TAG, "to next");
            mMediaPlayer = mNextPlayer;
            if (mHolderDestroyed) {
                mMediaPlayer.release();
                return;
            }
            mMediaPlayer.setDisplay(mHolder);
            if (mOneListener != null) {
                mOneListener.onOneCompletion();
            }
            nextPrepare();
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, what + " " + extra);
        mMediaError = true;
        reset();
        nextPrepare();
        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(TAG, "surface create");
        mHolderCreated = true;
        if (mMediaPlayer != null) {
            mMediaPlayer.setDisplay(holder);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(TAG, "surface change");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(TAG, "surface destroy");
        mHolderDestroyed = true;
        mHolderCreated = false;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    public interface PlayOneListener {
        void onOneCompletion();
    }
}
