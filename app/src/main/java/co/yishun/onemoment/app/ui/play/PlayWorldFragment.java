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

import co.yishun.library.OnemomentPlayerView;
import co.yishun.library.resource.NetworkVideo;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.loader.VideoTask;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_world)
public class PlayWorldFragment extends PlayFragment implements OnemomentPlayerView.OnVideoChangeListener,
        VideoTask.OnVideoListener {
    private static final String TAG = "platworld";
    @FragmentArg WorldTag worldTag;
    @FragmentArg boolean isPrivate = false;

    @ViewById TextView voteCountTextView;
    @ViewById TextView usernameTextView;

    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private List<TagVideo> tagVideos = new ArrayList<>();
    private int voteIndex;
    private Seed seed;
    private int offset = 0;
    private boolean mReady = false;

    @Background void getData() {
        List<TagVideo> videos = isPrivate ?
                mWorld.getPrivateVideoOfTag(worldTag.name, offset,
                        10, AccountManager.getUserInfo(mContext)._id) :
                mWorld.getVideoOfTag(worldTag.name, offset,
                        10, AccountManager.getUserInfo(mContext)._id, seed);
        if (videos.size() == 0) {
            if (offset == 0) {
                onLoadError(R.string.fragment_play_world_video_null);
            }
            return;
        }
        offset += videos.size();
        seed = videos.get(0).seed;

        for (TagVideo oneVideo : videos) {
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
    public void onVideoLoad(Video video) {
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
        videoPlayView.setToLocal(video.domain + video.fileName, videoFile.getPath());
        if (!mReady) {
            onLoad();
            mReady = true;
        }
    }

    @UiThread void addVideo(TagVideo video) {
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
        List<VideoTag> tags = new LinkedList<>();
        for (int i = 0; i < video.tags.size(); i++) {
            tags.add(new BaseVideoTag(video.tags.get(i).name, video.tags.get(i).x, video.tags.get(i).y));
        }
        NetworkVideo videoResource = new NetworkVideo(video.domain + video.fileName, tags);
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
        videoPlayView.addAvatarUrl((video).avatar);
    }

    @Click(R.id.voteCountTextView)
    @Background void voteClick() {
        voteIndex = videoPlayView.getCurrentIndex();
        tagVideos.get(voteIndex).liked = !tagVideos.get(voteIndex).liked;
        tagVideos.get(voteIndex).likeNum += tagVideos.get(voteIndex).liked ? 1 : -1;
        refreshUserInfo(videoPlayView.getCurrentIndex());
        if (tagVideos.get(voteIndex).liked) {
            mWorld.likeVideo(tagVideos.get(voteIndex)._id, AccountManager.getUserInfo(mContext)._id);
        } else {
            mWorld.unlikeVideo(tagVideos.get(voteIndex)._id, AccountManager.getUserInfo(mContext)._id);
        }
    }

    @UiThread void refreshUserInfo(int index) {
        if (tagVideos.get(index).liked) {
            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall_Inverse);
            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_orange, 0, 0, 0);
        } else {
            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall);
            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_gray, 0, 0, 0);
        }
        voteCountTextView.setText(tagVideos.get(index).likeNum + "");
        usernameTextView.setText(tagVideos.get(index).nickname);
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
