package co.yishun.library;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import java.util.LinkedList;
import java.util.List;

import co.yishun.library.resource.VideoResource;

/**
 * OnemomentPlayerView
 *
 * @author ZhihaoJun
 */
public class OnemomentPlayerView extends RelativeLayout implements OnemomentPlaySurfaceView.PlayOneListener {
    public final static String VIDEO_TAG_VIEW_TAG = "video_tag";

    private OnemomentPlaySurfaceView mPlaySurface;
    private AvatarRecyclerView mAvatarView;
    private ImageView mPlayBtn;
    private PlayTagContainer mTagContainer;
    private List<VideoResource> mVideoResources = new LinkedList<VideoResource>();
    private OnVideoChangeListener mVideoChangeListener;
    private int mPreparedIndex = 0;
    private int mCompletionIndex = 0;
    private boolean mShowPlayBtn = true;
    private boolean mAutoplay = false;
    private boolean mShowTags = true;
    private boolean mSinglePlay = true;

    public OnemomentPlayerView(Context context) {
        super(context);
        init(context, null, 0);
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
        if (attrs != null) {
            TypedArray ta = context.getTheme().obtainStyledAttributes(attrs, R.styleable.OnemomentPlayerView, 0, 0);

            try {
                mShowPlayBtn = ta.getBoolean(R.styleable.OnemomentPlayerView_opv_showPlayButton, true);
                mAutoplay = ta.getBoolean(R.styleable.OnemomentPlayerView_opv_autoplay, false);
                mShowTags = ta.getBoolean(R.styleable.OnemomentPlayerView_opv_showTags, true);
            } finally {
                ta.recycle();
            }
        }
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            inflater.inflate(R.layout.onemoment_player_view, this);
        }

        // get views
        mPlaySurface = (OnemomentPlaySurfaceView) findViewById(R.id.om_video_surface);
        mPlayBtn = (ImageView) findViewById(R.id.om_play_btn);
        mTagContainer = (PlayTagContainer) findViewById(R.id.om_tags_container);
        mAvatarView = (AvatarRecyclerView) findViewById(R.id.om_avatar_recycler_view);

        mPlaySurface.setOneListener(this);

        mCompletionIndex = -1;
    }

    public boolean isPlaying() {
        return mPlaySurface.isPlaying();
    }

    public void prepare() {
        mPlaySurface.setVideoResource(mVideoResources.get(0));
        if (mVideoResources.size() == 1) {
            mPlaySurface.setNextVideoResource(mVideoResources.get(0));
            mPreparedIndex = 0;
        } else {
            mPlaySurface.setNextVideoResource(mVideoResources.get(1));
            mPreparedIndex = 1;
        }
        mPlaySurface.fistPrepare();
    }

    public void start() {
        mPlaySurface.start();
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.INVISIBLE);
        }
    }

    public void pause() {
        mPlaySurface.pause();
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
    }

    public void stop() {
        mPlaySurface.stop();
    }

    public void reset() {
        if (mShowPlayBtn) {
            mPlayBtn.setVisibility(View.VISIBLE);
        }
        if (mVideoResources.size() >= 1) {
            if (mVideoChangeListener != null) {
                mVideoChangeListener.videoChangeTo((mCompletionIndex + 1) % mVideoResources.size());
            }
            mPlaySurface.setVideoResource(mVideoResources.get(0));
            mPreparedIndex = 0;
            mPlaySurface.fistPrepare();
        }
        if (mVideoResources.size() >= 2) {
            mPlaySurface.setNextVideoResource(mVideoResources.get(1));
            mPreparedIndex = 1;
            mPlaySurface.nextPrepare();
        }
    }

    public void addVideoResource(VideoResource videoResource) {
        Log.i("[OPV]", "add resource " + videoResource);
        mVideoResources.add(videoResource);
        if (mVideoResources.size() == 1) {
            if (mVideoChangeListener != null) {
                mVideoChangeListener.videoChangeTo((mCompletionIndex + 1) % mVideoResources.size());
            }
            mPlaySurface.setVideoResource(mVideoResources.get(0));
            mPreparedIndex = 0;
            mTagContainer.setVideoTags(mVideoResources.get(0).getVideoTags());
            mPlaySurface.fistPrepare();
        }
        if (mVideoResources.size() == 2) {
            mPlaySurface.setNextVideoResource(mVideoResources.get(1));
            mPreparedIndex = 1;
            mPlaySurface.nextPrepare();
        }
    }

    public void addAvatarUrl(String url) {
        mAvatarView.addAvatar(url);
    }

    public void setShowPlayBtn(boolean mShowPlayBtn) {
        this.mShowPlayBtn = mShowPlayBtn;
        invalidate();
        requestLayout();
    }

    public void setVideoChangeListener(OnVideoChangeListener videoChangeListener) {
        this.mVideoChangeListener = videoChangeListener;
    }

    public boolean isAutoplay() {
        return mAutoplay;
    }

    public void setAutoplay(boolean mAutoplay) {
        this.mAutoplay = mAutoplay;
    }

    public boolean isShowTags() {
        return mTagContainer.isShowTags();
    }

    public void setShowTags(boolean showTags) {
        mTagContainer.setShowTags(showTags);
    }

    public int getCurrentIndex() {
        return (mCompletionIndex + 1) % mVideoResources.size();
    }

    public void setSinglePlay(boolean singlePlay) {
        this.mSinglePlay = singlePlay;
        if (singlePlay) {
            mAvatarView.setVisibility(GONE);
        }
    }

    @Override
    public void onOneCompletion() {
        mCompletionIndex++;
        mCompletionIndex %= mVideoResources.size();

        if (mSinglePlay || mVideoResources.size() == 1) {
            reset();
        } else {
            mAvatarView.scrollToNext();
            if (mVideoChangeListener != null) {
                mVideoChangeListener.videoChangeTo((mCompletionIndex + 1) % mVideoResources.size());
            }
            mTagContainer.setVideoTags(mVideoResources.get((mCompletionIndex + 1) % mVideoResources.size()).getVideoTags());
            if (mCompletionIndex == mVideoResources.size() - 2) {
                mPlaySurface.setNextVideoResource(null);
                return;
            }
            if (mCompletionIndex == mVideoResources.size() - 1) {
                reset();
                return;
            }
            mPreparedIndex++;
            mPreparedIndex %= mVideoResources.size();
            mPlaySurface.setNextVideoResource(mVideoResources.get(mPreparedIndex));
        }
    }

    public interface OnVideoChangeListener {
        void videoChangeTo(int index);
    }
}
