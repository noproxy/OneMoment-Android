package co.yishun.onemoment.app.ui.play;

import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import co.yishun.library.VideoPlayerView;
import co.yishun.library.resource.NetworkVideo;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.WorldAPI;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.loader.VideoTask;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_tag_video)
public class PlayTagVideoFragment extends PlayFragment implements VideoPlayerView.VideoPlayViewListener {
    @FragmentArg
    VideoProvider video;

    @ViewById
    ImageView avatar;
    @ViewById
    TextView usernameTextView;
    @ViewById
    TextView voteCountTextView;

    private WorldAPI mWorldAPI = OneMomentV3.createAdapter().create(WorldAPI.class);

    private int playViewRequestIndex = 0;

    @AfterViews
    void setup() {
        Picasso.with(mContext).load(video.getAvatarUrl()).into(avatar);

        usernameTextView.setText(video.getNickname());

        videoPlayView.setWithAvatar(false);
        videoPlayView.setVideoPlayListener(this);

        new VideoTask(mContext, video, VideoTask.TYPE_VIDEO)
                .setVideoListener(this::addVideo).start();

//        refreshUserInfo();
    }

    @UiThread
    void addVideo(VideoProvider video) {
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
        if (playViewRequestIndex == 0)
            cacheVideoToPlayView(video, videoFile);
        videoPlayView.addAvatarUrl(video.getAvatarUrl());
        onLoad();
    }

    void cacheVideoToPlayView(VideoProvider video, File videoFile) {
        List<VideoTag> tags = new LinkedList<>();
        for (int i = 0; i < video.getTags().size(); i++) {
            tags.add(new BaseVideoTag(video.getTags().get(i).name, video.getTags().get(i).x, video.getTags().get(i).y));
        }
        NetworkVideo videoResource = new NetworkVideo(tags, videoFile.getPath());
        videoPlayView.addVideoResource(videoResource);
        playViewRequestIndex = -1;
    }

//    @Click(R.id.voteCountTextView)
//    @Background void voteClick() {
//        video.liked = !video.liked;
//        video.likeNum += video.liked ? 1 : -1;
//        refreshUserInfo();
//        if (video.liked) {
//            mWorldAPI.likeVideo(video._id, AccountManager.getUserInfo(mContext)._id);
//        } else {
//            mWorldAPI.unlikeVideo(video._id, AccountManager.getUserInfo(mContext)._id);
//        }
//    }
//
//    @UiThread void refreshUserInfo() {
//        if (video.liked) {
//            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall_Inverse);
//            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_orange, 0, 0, 0);
//        } else {
//            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall);
//            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_gray, 0, 0, 0);
//        }
//        voteCountTextView.setText(video.likeNum + "");
//    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }

    @Override
    public void videoChangeTo(int index) {

    }

    @Override
    public boolean loadMore(int index) {
        if (index >= 1) {
            return false;
        } else {
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
