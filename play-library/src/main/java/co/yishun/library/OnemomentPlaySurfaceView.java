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
public class OnemomentPlaySurfaceView extends SurfaceView implements SurfaceHolder.Callback,
                                                                     MediaPlayer.OnCompletionListener,
                                                                     MediaPlayer.OnErrorListener {
    private static final String TAG = "PlaySurfaceView";
    private SurfaceHolder mHolder;
    private MediaPlayer mMediaPlayer;
    private MediaPlayer mNextPlayer;
    private VideoResource mVideoResource;
    private VideoResource mNextVideoResource;
    private boolean mHolderCreated = false;
    private boolean mHolderDestroyed = false;
    private PlayListener mPlayListener;

    private State mMediaState;
    private State mNextState;

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

    public void prepareFirst() {
        if (mVideoResource == null) {
            return;
        }
        try {
            mMediaPlayer = new MediaPlayer();
            if (mHolderCreated) {
                Log.d(TAG, "first media " + mMediaPlayer);
                mMediaPlayer.setDisplay(mHolder);
            }
            mMediaPlayer.setDataSource(getContext(), mVideoResource.getVideoUri());
            mMediaState = State.PREPARING;
            mMediaPlayer.prepareAsync();
            mMediaPlayer.setOnPreparedListener(this::firstPrepared);
            mMediaPlayer.setScreenOnWhilePlaying(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
    }

    public void prepareNext() {
        if (mNextVideoResource == null) {
            mNextPlayer = null;
            return;
        }
        try {
            Log.d(TAG, "create next");
            mNextPlayer = new MediaPlayer();
            mNextPlayer.setDataSource(getContext(), mNextVideoResource.getVideoUri());
            mNextState = State.PREPARING;
            mNextPlayer.prepareAsync();
            mNextPlayer.setOnPreparedListener(this::nextPrepared);
            mNextPlayer.setOnCompletionListener(this);
            mNextPlayer.setOnErrorListener(this);
            mNextPlayer.setScreenOnWhilePlaying(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void start() {
        if (mMediaState == State.PREPARED || mMediaState == State.PAUSE) {
            Log.d(TAG, "start");
            mMediaPlayer.start();
            mMediaState = State.STARTED;
        }
    }

    public void pause() {
        if (mMediaState == State.STARTED) {
            mMediaPlayer.pause();
            mMediaState = State.PAUSE;
        }
    }

    public void reset() {
        release();
        mMediaPlayer = null;
        mNextPlayer = null;
        mMediaState = State.IDLE;
        prepareFirst();
    }

    void release() {
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mNextPlayer != null) {
            mNextPlayer.release();
        }
    }

    void moveToNext() {
        Log.d(TAG, "move to next");
        mMediaPlayer.release();
        mMediaPlayer = null;
        mMediaPlayer = mNextPlayer;
        mMediaPlayer.setDisplay(mHolder);
        mMediaState = State.PREPARED;
    }

    public boolean isPlaying() {
        return mMediaState == State.STARTED;
    }

    public void setVideoResource(VideoResource videoResource) {
        mVideoResource = videoResource;
    }

    public void setNextVideoResource(VideoResource videoResource) {
        mNextVideoResource = videoResource;
    }

    public void setOneListener(PlayListener oneListener) {
        mPlayListener = oneListener;
    }

    void firstPrepared(MediaPlayer mp) {
        mMediaState = State.PREPARED;
        Log.d(TAG, "first prepared");
        if (mPlayListener != null) {
            mPlayListener.onPrepared();
        }
    }

    void nextPrepared(MediaPlayer mp) {
        mNextState = State.PREPARED;
        Log.d(TAG, "next prepared");
        if (mMediaState == State.COMPLETED) {
            Log.d(TAG, "completed and start");
            moveToNext();
            if (mPlayListener != null) {
                mPlayListener.onOneCompletion();
                mPlayListener.onPrepared();
            }
            start();
            prepareNext();
        } else {
            Log.d(TAG, "set next");
            mMediaPlayer.setNextMediaPlayer(mNextPlayer);
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "complete");
        mMediaState = State.COMPLETED;
        if (mNextPlayer == null || mp == mNextPlayer) {
            Log.d(TAG, "end");
            if (mPlayListener != null) {
                mPlayListener.onOneCompletion();
            }
        } else {
            Log.d(TAG, "to next");
            if (mNextState == State.PREPARED) {
                moveToNext();
                mMediaState = State.STARTED;
                if (mPlayListener != null) {
                    mPlayListener.onOneCompletion();
                }
                prepareNext();
            } else {
                //if next media not prepared, wait.
                if (mPlayListener != null) {
                    mPlayListener.onPreparing();
                }
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, what + " " + extra);
        reset();
        prepareNext();
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
        mHolderCreated = false;
        reset();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, widthMeasureSpec);
    }

    enum State {
        IDLE,
        PREPARING,
        PREPARED,
        STARTED,
        PAUSE,
        COMPLETED
    }

    public interface PlayListener {
        void onPrepared();

        void onPreparing();

        void onOneCompletion();
    }
}
