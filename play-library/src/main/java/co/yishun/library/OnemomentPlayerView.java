package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.media.MediaPlayer;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import co.yishun.library.resource.VideoResource;
import co.yishun.library.tag.VideoTag;

/**
 * OnemomentPlayerView
 *
 * @author ZhihaoJun
 */
public class OnemomentPlayerView extends RelativeLayout
        implements SurfaceHolder.Callback, MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener {
    private SurfaceHolder mVideoSurfaceHolder;
    private SurfaceView mVideo;
    private ImageView mPlayBtn;
    private VideoTagsContainer mTagsContainer;
    private LinearLayout mHeadsContainer;
    private List<VideoResource> mVideoResources = new LinkedList<VideoResource>();
    private MediaPlayer mActiveMediaPlayer;
    private int mVideoIndex = 0;
    private boolean mShowPlayBtn = true;
    private boolean mAutoplay = false;
    private boolean mShowTags = true;

    public OnemomentPlayerView(Context context) {
        super(context);
        init(context, null, 0);
//        mActiveMediaPlayer.setDataSource();
//        mActiveMediaPlayer.setNextMediaPlayer();
    }

    public OnemomentPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public OnemomentPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void init(Context context, AttributeSet attrs, int defStyle) {
        mActiveMediaPlayer = new MediaPlayer();
        if (attrs != null) {
            TypedArray ta = context.getTheme().obtainStyledAttributes(
                    attrs,
                    R.styleable.OnemomentPlayerView,
                    0, 0);

            try {
                mShowPlayBtn = ta.getBoolean(R.styleable.OnemomentPlayerView_showPlayButton, true);
                mAutoplay = ta.getBoolean(R.styleable.OnemomentPlayerView_autoplay, false);
                mShowTags = ta.getBoolean(R.styleable.OnemomentPlayerView_showTags, true);
            } finally {
                ta.recycle();
            }
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.inflate(R.layout.onemoment_player_view, this);
        }

        // get views
        mVideo = (SurfaceView) findViewById(R.id.om_video_surface);
        mPlayBtn = (ImageView) findViewById(R.id.om_play_btn);
        mTagsContainer = (VideoTagsContainer) findViewById(R.id.om_tags_container);
        mHeadsContainer = (LinearLayout) findViewById(R.id.om_heads_container);

        // add listeners
        mVideoSurfaceHolder = mVideo.getHolder();
        mVideoSurfaceHolder.addCallback(this);
        mActiveMediaPlayer.setOnCompletionListener(this);
    }

    public boolean isPlaying() {
        return mActiveMediaPlayer.isPlaying();
    }

    public void start() {
        if (!mActiveMediaPlayer.isPlaying()) {
            mActiveMediaPlayer.start();
        }
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void pause() {
        if (mActiveMediaPlayer.isPlaying()) {
            mActiveMediaPlayer.pause();
        }
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }

    public void stop() {
        if (mActiveMediaPlayer.isPlaying()) {
            mActiveMediaPlayer.stop();
            mActiveMediaPlayer.release();
        }
    }

    public void addVideoResource(VideoResource videoResource) {
        Log.i("[OPV]", "add resource " + videoResource);
        mVideoResources.add(videoResource);
    }

    public void prepare() throws IOException {
        if (mVideoResources.size() <= 0)
            return;
        VideoResource first = mVideoResources.get(0);
        mTagsContainer.setVideoTags(first.getVideoTags());
        mActiveMediaPlayer.setDataSource(getContext(), first.getVideoUri());
        mActiveMediaPlayer.prepare();
    }

    public void setShowPlayBtn(boolean mShowPlayBtn) {
        this.mShowPlayBtn = mShowPlayBtn;
        invalidate();
        requestLayout();
    }

    public boolean isAutoplay() {
        return mAutoplay;
    }

    public void setAutoplay(boolean mAutoplay) {
        this.mAutoplay = mAutoplay;
    }

    public boolean isShowTags() {
        return mTagsContainer.isShowTags();
    }

    public void addVideoTag(VideoTag tag) {
        mTagsContainer.addTag(tag);
    }

    public void setShowTags(boolean showTags) {
        mTagsContainer.setShowTags(showTags);
    }

    public boolean isTagEditable() {
        return mTagsContainer.isEditable();
    }

    public void setTagEditable(boolean mTagEditable) {
        mTagsContainer.setEditable(mTagEditable);
    }

    public int getVideoIndex() {
        return this.mVideoIndex;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Log.i("[OPV]", "SurfaceCreated");
        mActiveMediaPlayer.setDisplay(surfaceHolder);
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        Log.i("[OPV]", "SurfaceChanged");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        Log.i("[OPV]", "SurfaceDestroyed");
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        Log.i("[OPV]", "mp error" + what + "");
        Log.i("[OPV]", "mp error" + extra + "");
        return false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.i("[OPV]", "mp complete");
        if (mVideoIndex < mVideoResources.size() - 1) {
            mVideoIndex++;
        } else {
            mVideoIndex = 0;
        }

        Log.i("[OPV]", "next video index: " + mVideoIndex);
        VideoResource vr = mVideoResources.get(mVideoIndex);
        mActiveMediaPlayer.reset();
        try {
            mActiveMediaPlayer.setDataSource(getContext(), vr.getVideoUri());
        } catch (IOException e) {
            Log.i("[OPV]", "media player set data source failed");
            e.printStackTrace();
        }
        try {
            mActiveMediaPlayer.prepare();
        } catch (IOException e) {
            Log.i("[OPV]", "media player prepare failed");
            e.printStackTrace();
        }

        mTagsContainer.setVideoTags(mVideoResources.get(mVideoIndex).getVideoTags());
        mTagsContainer.showTags();

        if (mVideoIndex != 0) {
            mActiveMediaPlayer.start();
        }

        if (mShowPlayBtn && mVideoIndex == 0) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }

    public void setOnTouchListener(View.OnTouchListener listener) {
        mVideo.setOnTouchListener(listener);
    }
}
