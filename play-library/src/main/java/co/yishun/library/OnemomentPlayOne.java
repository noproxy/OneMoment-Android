package co.yishun.library;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.security.MessageDigest;

import co.yishun.library.resource.VideoResource;

/**
 * Created on 2015/10/29.
 */
public class OnemomentPlayOne extends SurfaceView implements SurfaceHolder.Callback, MediaPlayer.OnCompletionListener {
    private SurfaceHolder mHolder;
    private MediaPlayer mMediaPlayer;
    private MediaPlayer nextPlayer;
    private VideoResource videoResource;
    private VideoResource nextVideoResource;
    public OnemomentPlayOne(Context context) {
        super(context);
        init();
    }

    public OnemomentPlayOne(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public OnemomentPlayOne(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public OnemomentPlayOne(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init();
    }

    private void init() {
        mMediaPlayer = new MediaPlayer();
        nextPlayer = new MediaPlayer();
        mHolder = getHolder();
        mHolder.addCallback(this);
        mMediaPlayer.setOnCompletionListener(this);
    }

    private void prepare() throws IOException {
        if (videoResource == null) {
            return;
        }
        mMediaPlayer.setDataSource(getContext(), videoResource.getVideoUri());
        mMediaPlayer.prepare();
        nextPlayer.setDataSource(getContext(), nextVideoResource.getVideoUri());
        nextPlayer.prepare();
    }

    public void setVideoResource(VideoResource videoResource) {
        this.videoResource = videoResource;
    }

    public void setNextVideoResource(VideoResource videoResource) {
        nextVideoResource = videoResource;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mMediaPlayer.setDisplay(holder);
        nextPlayer.setDisplay(holder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mMediaPlayer.reset();
        mMediaPlayer.setNextMediaPlayer(mMediaPlayer);
    }
}
