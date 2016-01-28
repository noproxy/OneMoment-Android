package co.yishun.onemoment.app.ui.play;

import android.view.WindowManager;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
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
public class PlayWorldFragment extends PlayFragment implements VideoPlayerView.OnVideoChangeListener,
        VideoTask.OnVideoListener {
    private static final String TAG = "platworld";
    @FragmentArg WorldProvider world;
    @FragmentArg boolean forWorld;

    @ViewById TextView voteCountTextView;
    @ViewById TextView usernameTextView;

    private APIV4 mApiV4 = OneMomentV4.createAdapter().create(APIV4.class);
    private List<VideoProvider> tagVideos = new ArrayList<>();
    private int order;
    private int offset = 0;
    private boolean mReady = false;

    @Background void getData() {
        WorldVideoListWithErrorV4<WorldVideo> videos = forWorld ?
                mApiV4.getWorldVideos(world.getId(), AccountManager.getUserInfo(mContext)._id, order, 6) :
                mApiV4.getTodayVideos(world.getName(), offset, 6);
        if (videos.size() == 0) {
            if (offset == 0) {
                onLoadError(R.string.fragment_play_world_video_null);
            }
            return;
        }
        offset += videos.size();
        order = videos.world.order;

        for (VideoProvider oneVideo : videos) {
            if (mContext == null) {
                return;
            }
            addVideo(oneVideo);
        }
        getData();
    }

    @AfterViews void setupView() {
        videoPlayView.setWithAvatar(true);
        videoPlayView.setVideoChangeListener(this);
        getData();
    }

    @Override
    public void onVideoLoad(VideoProvider video) {
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
        videoPlayView.setToLocal(video.getDownloadUrl(), videoFile.getPath());
        if (!mReady) {
            onLoad();
            mReady = true;
        }
    }

    @UiThread void addVideo(VideoProvider video) {
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
        List<VideoTag> tags = new LinkedList<>();
        for (int i = 0; i < video.getTags().size(); i++) {
            tags.add(new BaseVideoTag(video.getTags().get(i).name, video.getTags().get(i).x, video.getTags().get(i).y));
        }
        NetworkVideo videoResource = new NetworkVideo(video.getDownloadUrl(), tags);
        if (videoFile.length() > 0) {
            videoResource.setPath(videoFile.getPath());
            if (!mReady) {
                onLoad();
                mReady = true;
            }
        } else {
            new VideoTask(mContext, video, VideoTask.TYPE_VIDEO)
                    .setVideoListener(this).start();
        }

        videoPlayView.addVideoResource(videoResource);
        tagVideos.add(video);
        videoPlayView.addAvatarUrl(video.getAvatarUrl());
    }

//    @Click(R.id.voteCountTextView)
//    @Background void voteClick() {
//        voteIndex = videoPlayView.getCurrentIndex();
//        tagVideos.get(voteIndex).liked = !tagVideos.get(voteIndex).liked;
//        tagVideos.get(voteIndex).likeNum += tagVideos.get(voteIndex).liked ? 1 : -1;
//        refreshUserInfo(videoPlayView.getCurrentIndex());
//        if (tagVideos.get(voteIndex).liked) {
//            mApiV4.likeVideo(tagVideos.get(voteIndex)._id, AccountManager.getUserInfo(mContext)._id);
//        } else {
//            mApiV4.unlikeVideo(tagVideos.get(voteIndex)._id, AccountManager.getUserInfo(mContext)._id);
//        }
//    }

    @UiThread void refreshUserInfo(int index) {
//        if (tagVideos.get(index).liked) {
//            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall_Inverse);
//            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_orange, 0, 0, 0);
//        } else {
//            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall);
//            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_gray, 0, 0, 0);
//        }
//        voteCountTextView.setText(tagVideos.get(index).likeNum + "");
        usernameTextView.setText(tagVideos.get(index).getNickname());
    }


    @Override
    public void setPageInfo() {
        mIsPage = false;
    }

    @Override
    public void videoChangeTo(int index) {
        if (index == 0) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        refreshUserInfo(index);
    }

}
