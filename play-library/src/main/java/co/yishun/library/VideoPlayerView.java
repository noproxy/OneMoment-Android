package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import co.yishun.library.resource.NetworkVideo;
import co.yishun.library.resource.VideoResource;
import co.yishun.library.tag.VideoTag;

/**
 * OnemomentPlayerView
 *
 * @author ZhihaoJun
 */
public class VideoPlayerView extends RelativeLayout
        implements OMVideoPlayer.PlayListener {
    public final static String TAG = "VideoPlayerView";

    private PlaySurfaceView mPlaySurface;
    private AvatarRecyclerView mAvatarView;
    private ImageView mVideoPreview;
    private ImageView mPlayBtn;
    private ProgressBar mProgress;
    private TagContainer mTagContainer;
    private Queue<NetworkVideo> mResQueue = new LinkedBlockingQueue<>();
    private Queue<List<VideoTag>> mTagQueue = new LinkedBlockingQueue<>();
    private VideoPlayViewListener mPlayListener;

    private OMVideoPlayer mVideoPlayer;

    private int mPreparedIndex = 0;
    private int mCompletionIndex = 0;
    private boolean mShowPlayBtn = true;
    private boolean mAutoplay = false;
    private boolean mShowTags = true;
    private boolean mWithAvatar = false;
    private boolean mLoading = false;
    private boolean mMoreAsking = true;
    private int mCachedIndex = 0;
    private int mPlayingIndex = 0;
    private boolean mNoMoreVideo = false;

    public VideoPlayerView(Context context) {
        super(context);
        init(context, null, 0);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    public void init(Context context, AttributeSet attrs, int defStyle) {
        if (attrs != null) {
            TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.VideoPlayerView, 0, 0);

            try {
                mShowPlayBtn = ta.getBoolean(R.styleable.VideoPlayerView_opv_showPlayButton, true);
                mAutoplay = ta.getBoolean(R.styleable.VideoPlayerView_opv_autoplay, false);
                mShowTags = ta.getBoolean(R.styleable.VideoPlayerView_opv_showTags, true);
            } finally {
                ta.recycle();
            }
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.inflate(R.layout.onemoment_player_view, this);
        }

        // get views
        mPlaySurface = (PlaySurfaceView) findViewById(R.id.om_video_surface);
        mPlayBtn = (ImageView) findViewById(R.id.om_play_btn);
        mPlayBtn.setVisibility(INVISIBLE);
        mVideoPreview = (ImageView) findViewById(R.id.om_video_preview);
        mTagContainer = (TagContainer) findViewById(R.id.om_tags_container);
        mAvatarView = (AvatarRecyclerView) findViewById(R.id.om_avatar_recycler_view);
        mProgress = (ProgressBar) findViewById(R.id.om_progress);

        mVideoPlayer = new OMVideoPlayer(getContext());
        mPlaySurface.setPlayer(mVideoPlayer, this);

        mCompletionIndex = -1;
    }

    public boolean isPlaying() {
        return mVideoPlayer.isPlaying();
    }

    public void setPreview(File largeThumb) {
        Picasso.with(getContext()).load(largeThumb).into(mVideoPreview);
    }

    public void showLoading() {
        mProgress.setVisibility(VISIBLE);
        this.setEnabled(false);
    }

    public void hideLoading() {
        if (mProgress.getVisibility() == VISIBLE) {
            mProgress.setVisibility(INVISIBLE);
            mVideoPlayer.start();
            this.setEnabled(true);
        }
    }

    public void start() {
        Log.d(TAG, "start");
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.INVISIBLE);
            mVideoPreview.setVisibility(INVISIBLE);
        }

        mVideoPlayer.start();
    }

    public void pause() {
        mVideoPlayer.pause();
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }

    public void stop() {
        mVideoPlayer.release();
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }

    public void reset() {
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
        mVideoPreview.setVisibility(VISIBLE);

        Log.d(TAG, "tag size : " + mTagQueue.size());
        List<VideoTag> videoTags = mTagQueue.poll();
        if (videoTags != null) mTagContainer.setVideoTags(videoTags);

        mCompletionIndex = -1;
        mMoreAsking = true;
        mCachedIndex = 0;
        mVideoPlayer.reset();
        loadMore();
    }

    public void addVideoResource(NetworkVideo[] videoResources) {
        for (NetworkVideo videoRes : videoResources) {
            addVideoResource(videoRes);
        }
    }

    public void addVideoResource(NetworkVideo videoResource) {
        Log.i(TAG, "add resource " + videoResource);
        if (mMoreAsking) {
            mVideoPlayer.setVideoRes(videoResource.getVideoUri());
            mMoreAsking = false;
            hideLoading();
        } else {
            mResQueue.offer(videoResource);
        }
        if (mCachedIndex == 0) {
            mTagContainer.setVideoTags(videoResource.getVideoTags());
        } else {
            mTagQueue.offer(videoResource.getVideoTags());
        }
        mCachedIndex++;
    }

    public void addAvatarUrl(String url) {
        mAvatarView.addAvatar(url);
    }

    public void setShowPlayBtn(boolean mShowPlayBtn) {
        this.mShowPlayBtn = mShowPlayBtn;
        invalidate();
        requestLayout();
    }

    public void setVideoPlayListener(VideoPlayViewListener playListener) {
        this.mPlayListener = playListener;
    }

    public boolean isAutoplay() {
        return mAutoplay;
    }

    public void setAutoplay(boolean mAutoplay) {
        this.mAutoplay = mAutoplay;
    }

    public int getCurrentIndex() {
        return (mCompletionIndex + 1) % mResQueue.size();
    }

    public void setWithAvatar(boolean withAvatar) {
        this.mWithAvatar = withAvatar;
        if (!withAvatar) {
            mAvatarView.setVisibility(GONE);
        }
    }

    private void loadMore() {
        if (mPlayListener != null)
            for (int i = 0; i < 8; i++) {
                boolean result = mPlayListener.loadMore(mCachedIndex);
                if (!result) {
                    mNoMoreVideo = true;
                    break;
                }
            }
        else mNoMoreVideo = true;
        Log.d(TAG, "queue size : " + mResQueue.size() + "  " + mCachedIndex);
    }

    @Override
    public void onPrepared() {
        Log.d(TAG, "loaded");
        mLoading = false;
    }

    @Override
    public void onPreparing() {
        Log.d(TAG, "loading");
        mLoading = true;
    }

    @Override
    public void onOneCompletion() {
        mCompletionIndex++;

        if (mWithAvatar)
            mAvatarView.scrollToNext();

        if (mPlayListener != null) {
            mPlayListener.videoChangeTo((mCompletionIndex + 1) % mCachedIndex);
        }

        List<VideoTag> videoTags = mTagQueue.poll();
        if (videoTags != null) mTagContainer.setVideoTags(videoTags);

        if (mCompletionIndex == mCachedIndex - 1) {
            if (mNoMoreVideo) {
                reset();
            } else {
                if (mMoreAsking) {
                    showLoading();
                }
            }
        }
    }

    @Override
    public Uri onMoreAsked() {
        Log.d(TAG, "on more asked, queue size : " + mResQueue.size());
        VideoResource videoRes = mResQueue.poll();
        if (videoRes == null) {
            mMoreAsking = true;
            loadMore();
            return null;
        }
        return videoRes.getVideoUri();
    }

    public interface VideoPlayViewListener {
        void videoChangeTo(int index);

        boolean loadMore(int index);
    }
}
