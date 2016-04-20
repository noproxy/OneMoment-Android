package co.yishun.onemoment.app.ui.play;

import android.content.Context;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.SurfaceHolder;

import org.androidannotations.api.BackgroundExecutor;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by Jinge on 2016/3/1.
 */
@SuppressWarnings("unused")
public class TurnsPlayer implements MediaPlayer.OnCompletionListener, MediaPlayer.OnErrorListener {

    private static final String TAG = "TurnsPlayer";
    private final Context mContext;
    private MediaPlayer mPrimaryPlayer;
    private MediaPlayer mSecondaryPlayer;
    private boolean pendingStart = false;
    private PlayListener mProvider = null;
    private SurfaceHolder mSurfaceHolder;
    private Handler mHandler;
    private Map<MediaPlayer, Uri> mUriMap = new WeakHashMap<>();

    public TurnsPlayer(Context context) {
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void setProvider(PlayListener provider) {
        this.mProvider = provider;
    }

    public void setDisplay(SurfaceHolder sh) {
        mSurfaceHolder = sh;
    }

    public void start() {
        if (mPrimaryPlayer != null) {
            // resume
            mPrimaryPlayer.start();
        } else if (mSecondaryPlayer != null) {
            mPrimaryPlayer = mSecondaryPlayer;
            mSecondaryPlayer = null;
            mPrimaryPlayer.start();
        } else {
            pendingStart = true;
        }
    }

    private void prepareInner() {
        if (mProvider == null) {
            throw new NoPlayProviderException();
        }

        Uri uri = mProvider.onMoreAsked();
        if (uri != null) {
            mHandler.post(() -> {
                MediaPlayer mediaPlayer = new MediaPlayer();
                try {
                    mediaPlayer.setDataSource(mContext, uri);
                    mediaPlayer.setOnPreparedListener(this::onPrepared);
                    mediaPlayer.setOnCompletionListener(this);
                    mediaPlayer.setOnErrorListener(this);
                    mediaPlayer.setScreenOnWhilePlaying(true);
                    mediaPlayer.setDisplay(mSurfaceHolder);

                    mUriMap.put(mediaPlayer, uri);

                    mediaPlayer.prepareAsync();

                } catch (IllegalArgumentException | IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            Log.e(TAG, "null uri");
        }
    }

    public void prepare() {
        BackgroundExecutor.execute(this::prepareInner);
    }

    private void onPrepared(MediaPlayer mp) {
        if (pendingStart) {
            pendingStart = false;
            if (mPrimaryPlayer != null) {
                mPrimaryPlayer.release();
            }
            mPrimaryPlayer = mp;

            Uri uri = mUriMap.get(mp);
            mProvider.onOneStart(uri);
            mPrimaryPlayer.start();
        } else {
            mSecondaryPlayer = mp;
        }
    }

    public void pause() throws IllegalStateException {
        if (mPrimaryPlayer != null) {
            mPrimaryPlayer.pause();
        }
    }

    public boolean isPlaying() {
        return mPrimaryPlayer != null && mPrimaryPlayer.isPlaying();
    }

    public void release() {
        if (mPrimaryPlayer != null) {
            mPrimaryPlayer.release();
        }

        if (mSecondaryPlayer != null) {
            mSecondaryPlayer.release();
        }
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPrimaryPlayer = null;
        Uri uri = mUriMap.get(mp);
        mProvider.onOneCompletion(uri);
        mUriMap.remove(mp);
        mp.release();
        start();
        prepare();
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    public interface PlayListener {
        /**
         * Called when one of the video complete. Will be called in UI thread.
         */
        void onOneCompletion(Uri uri);

        /**
         * Will be called in UI thread.
         */
        void onOneStart(Uri uri);

        /**
         * Called when need more video uri to play. Will be called in background thread.
         *
         * @return Uri of the video, null if no more videos to play.
         */
        Uri onMoreAsked();
    }

    public static class NoPlayProviderException extends IllegalArgumentException {
        public NoPlayProviderException() {
            super("You must PlayListener first");
        }
    }
}
