package co.yishun.library;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.util.Log;
import android.view.SurfaceHolder;

import java.io.IOException;

/**
 * Created by Jinge on 2016/3/1.
 */
public class OMVideoPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "OMVideoPlayer";
    private final Context mContext;
    private SurfaceHolder mHolder;

    private MediaPlayer mMediaPlayer;
    private MediaPlayer mNextPlayer;
    private Uri mVideoUri;
    private PlayListener mPlayListener;

    private State mMediaState;
    private State mNextState;

    private boolean mStartInFuture = false;

    public OMVideoPlayer(Context context) {
        mContext = context;
    }

    public void setListener(PlayListener listener) {
        mPlayListener = listener;
    }

    public void prepareNew() {
        if (mVideoUri == null && (mMediaPlayer == null || mNextPlayer == null)) {
            mVideoUri = mPlayListener.onMoreAsked();
        }
        if (mVideoUri != null) {
            try {
                Log.d(TAG, "create new " + mVideoUri.toString());
                MediaPlayer newPlayer = new MediaPlayer();
                newPlayer.setDataSource(mContext, mVideoUri);
                newPlayer.setOnPreparedListener(this::newPrepared);
                newPlayer.prepareAsync();
                newPlayer.setOnCompletionListener(this);
                newPlayer.setOnErrorListener(this);
                newPlayer.setScreenOnWhilePlaying(true);
                if (mMediaPlayer == null) {
                    mMediaPlayer = newPlayer;
                    mMediaState = State.PREPARING;
                    Log.d(TAG, "new is media player " + mMediaPlayer);
                } else {
                    mNextPlayer = newPlayer;
                    mNextState = State.PREPARING;
                    Log.d(TAG, "new is next player " + mNextPlayer);
                }
                mVideoUri = null;
            } catch (IOException e) {
                Log.e(TAG, mVideoUri.toString());
                e.printStackTrace();
            }
        }
    }

    public void setVideoRes(Uri videoUri) {
        mVideoUri = videoUri;
        prepareNew();
    }

    public void start() {
        Log.d(TAG, "main player state : " + mMediaState);
        if (mHolder == null) Log.e(TAG, "SurfaceHolder is null");
        if (mMediaState == State.PREPARED || mMediaState == State.PAUSE) {
            Log.d(TAG, "start");
            changeTo(mMediaPlayer);
            mMediaPlayer.start();
            mMediaState = State.STARTED;
        } else if (mMediaState == State.PREPARING) {
            mStartInFuture = true;
        }
    }

    public void pause() {
        if (mMediaState == State.STARTED) {
            mMediaPlayer.pause();
            mMediaState = State.PAUSE;
        }
    }

    public void reset() {
        Log.d(TAG, "reset");
        release();
        mMediaPlayer = null;
        mNextPlayer = null;
        mMediaState = State.IDLE;
    }

    public void release() {
        Log.d(TAG, "release");
        if (mMediaPlayer != null) {
            mMediaPlayer.release();
        }
        if (mNextPlayer != null) {
            mNextPlayer.release();
        }
    }

    void changeTo(MediaPlayer mp) {
        Log.d(TAG, "move to new");
        if (mMediaPlayer != mp) {
            if (mMediaPlayer != null)
                mMediaPlayer.release();
            mMediaPlayer = mp;
        }
        mMediaPlayer.setDisplay(mHolder);
        if (mMediaState != State.STARTED) {
            mMediaPlayer.start();
            mMediaPlayer.pause();
        }
        if (mNextPlayer == mp) {
            mNextPlayer = null;
            mNextState = State.IDLE;
        }
    }

    public boolean isPlaying() {
        return mMediaState == State.STARTED;
    }

    public void setDisplay(SurfaceHolder holder) {
        Log.d(TAG, "set holder");
        mHolder = holder;
        if (mMediaPlayer != null && mMediaState != State.STARTED) {
            mMediaPlayer.setDisplay(holder);
            mMediaPlayer.start();
            mMediaPlayer.pause();
        }
    }

    void newPrepared(MediaPlayer mp) {
        Log.d(TAG, "new prepared  " + mMediaPlayer + "  " + mNextPlayer + "  " + mp);
        if (mp == mMediaPlayer) {
            mMediaState = State.PREPARED;
            Log.d(TAG, "move to media player");
            changeTo(mp);
            if (mStartInFuture) {
                mMediaPlayer.start();
                mMediaState = State.STARTED;
                mStartInFuture = false;
            }

            if (mNextPlayer != null && mNextState == State.PREPARED) {
                mMediaPlayer.setNextMediaPlayer(mNextPlayer);
            } else {
                prepareNew();
            }
        } else {
            mNextState = State.PREPARED;
            if (mMediaPlayer == null) {
                Log.d(TAG, "move to next player");
                changeTo(mNextPlayer);
            } else {
                mMediaPlayer.setNextMediaPlayer(mNextPlayer);
            }
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        Log.e(TAG, "complete");
        mMediaState = State.COMPLETED;

        if (mNextPlayer != null && mNextState == State.PREPARED) {
            mMediaState = State.STARTED;
            changeTo(mNextPlayer);
            prepareNew();
        } else {
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        mPlayListener.onOneCompletion();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        Log.e(TAG, what + " " + extra);
        Log.e(TAG, mp + "  ");
        return true;
    }

//    @Override
//    public void surfaceCreated(SurfaceHolder holder) {
//        Log.d(TAG, "surface create");
//        mHolderCreated = true;
//        if (mMediaPlayer != null) {
//            mMediaPlayer.setDisplay(holder);
//        }
//    }
//
//    @Override
//    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
//        Log.d(TAG, "surface change");
//    }
//
//    @Override
//    public void surfaceDestroyed(SurfaceHolder holder) {
//        Log.d(TAG, "surface destroy");
//        mHolderCreated = false;
//        reset();
//    }

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

        Uri onMoreAsked();
    }
}
