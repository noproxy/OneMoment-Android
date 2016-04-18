package co.yishun.onemoment.app.ui.play;

import android.app.Activity;
import android.view.WindowManager;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import co.yishun.library.VideoPlayerView;
import co.yishun.library.resource.NetworkVideo;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.APIV4;
import co.yishun.onemoment.app.api.authentication.OneMomentV4;
import co.yishun.onemoment.app.api.loader.VideoTask;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;
import co.yishun.onemoment.app.api.modelv4.WorldProvider;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.api.modelv4.WorldVideoListWithErrorV4;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_world)
public class PlayWorldFragment extends PlayFragment implements VideoPlayerView.VideoPlayViewListener,
        VideoTask.OnVideoListener {

    private static final String TAG = "PlayWorldFragment";
    private final Object OffsetLock = new Object();
    @FragmentArg
    WorldProvider world;
    @PlayType
    @FragmentArg
    int playType;
    @ViewById
    TextView voteCountTextView;
    @ViewById
    TextView usernameTextView;
    private APIV4 mApiV4 = OneMomentV4.createAdapter().create(APIV4.class);
    private List<VideoProvider> videoProviders = new ArrayList<>();
    //    private int order;
    private Integer offset = 0;
    private boolean mReady = false;
    private boolean moreAsking = true;
    private int playViewRequestIndex = 0;
    private boolean avatarAdded = false;

    @Background
    void getData() {
        synchronized (OffsetLock) {
            LogUtil.d(TAG, "offset lock");
            // not background to ensure offset access only by one thread
            WorldVideoListWithErrorV4<WorldVideo> videos = playType == PlayType.TYPE_WORLD ?
                    mApiV4.getWorldVideos(world.getId(), AccountManager.getUserInfo(mContext)._id, offset, 6) :
                    mApiV4.getTodayVideos(world.getName(), offset, 6);
            if (videos.size() == 0) {
                if (offset == 0) {
                    onLoadError(R.string.fragment_play_world_video_null);
                }
                return;//TODO will OOM if this world contains so many many videos
            }
            LogUtil.d(TAG, "offset add: " + videos.size());
            offset += videos.size();

            for (VideoProvider oneVideo : videos) {
                if (mContext == null) {
                    return;
                }
                addVideo(oneVideo);
            }
        }
        getData();
    }

    @AfterViews
    void setupView() {
        videoPlayView.setWithAvatar(true);
        videoPlayView.setVideoPlayListener(this);
        getData();
    }

    @Override
    public void onVideoLoad(VideoProvider video) {
        LogUtil.d(TAG, "get video : " + video.getFilename());
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);

        int index = videoProviders.indexOf(video);
        if (index >= 0 && index == playViewRequestIndex)
            cacheVideoToPlayView(video, videoFile);
        if (index == 0) {
            onLoad();
            refreshUserInfo(0);
        }
        if (index != playViewRequestIndex) {
            LogUtil.e(TAG, "not equals index, wocao");
        }
    }

    @UiThread
    void addVideo(VideoProvider video) {
        videoProviders.add(video);
        LogUtil.d(TAG, "submit a video task: " + video);
        new VideoTask(mContext, video, VideoTask.TYPE_VIDEO)
                .setVideoListener(this).start();
        if (playType != PlayType.TYPE_WORLD || !avatarAdded) {
            videoPlayView.addAvatarUrl(video.getAvatarUrl());
            avatarAdded = true;
        }
    }

    void cacheVideoToPlayView(VideoProvider video, File videoFile) {
        LogUtil.d(TAG, "cacheVideoToPlayView: " + video + ", " + videoFile);
        List<VideoTag> tags = new LinkedList<>();
        for (int i = 0; i < video.getTags().size(); i++) {
            tags.add(new BaseVideoTag(video.getTags().get(i).name, video.getTags().get(i).x, video.getTags().get(i).y));
        }
        NetworkVideo videoResource = new NetworkVideo(tags, videoFile.getPath());
        videoPlayView.addVideoResource(videoResource);
        playViewRequestIndex = -1;
    }

    @UiThread
    void refreshUserInfo(int index) {
//        if (videoProviders.get(index).liked) {
//            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall_Inverse);
//            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_orange, 0, 0, 0);
//        } else {
//            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall);
//            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_gray, 0, 0, 0);
//        }
//        voteCountTextView.setText(videoProviders.get(index).likeNum + "");
        usernameTextView.setText(videoProviders.get(index).getNickname());
    }

//    @Click(R.id.voteCountTextView)
//    @Background void voteClick() {
//        voteIndex = videoPlayView.getCurrentIndex();
//        videoProviders.get(voteIndex).liked = !videoProviders.get(voteIndex).liked;
//        videoProviders.get(voteIndex).likeNum += videoProviders.get(voteIndex).liked ? 1 : -1;
//        refreshUserInfo(videoPlayView.getCurrentIndex());
//        if (videoProviders.get(voteIndex).liked) {
//            mApiV4.likeVideo(videoProviders.get(voteIndex)._id, AccountManager.getUserInfo(mContext)._id);
//        } else {
//            mApiV4.unlikeVideo(videoProviders.get(voteIndex)._id, AccountManager.getUserInfo(mContext)._id);
//        }
//    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }

    @Override
    public void videoChangeTo(int index) {
        if (index == 0) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }
        refreshUserInfo(index);
    }

    @Override
    public boolean loadMore(int index) {
        if (index == videoProviders.size()) {
            return false;
        } else {
            VideoProvider video = videoProviders.get(index);
            File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
            if (videoFile.length() > 0) {
                cacheVideoToPlayView(video, videoFile);
            } else {
                playViewRequestIndex = index;
            }
            return true;
        }
    }


}
