package co.yishun.onemoment.app.ui.play;

import android.content.Context;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.util.LinkedList;
import java.util.List;

import co.yishun.library.resource.NetworkVideo;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.loader.VideoTask;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_tag_video)
public class PlayTagVideoFragment extends PlayFragment {
    @FragmentArg TagVideo oneVideo;

    @ViewById ImageView avatar;
    @ViewById TextView usernameTextView;
    @ViewById TextView voteCountTextView;

    private World mWorld = OneMomentV3.createAdapter().create(World.class);

    @AfterViews void setup() {
        Log.d("oneVideo", oneVideo.toString());
        Picasso.with(mContext).load(oneVideo.avatar).into(avatar);

        usernameTextView.setText(oneVideo.nickname);

        videoPlayView.setWithAvatar(false);

        new VideoTask(mContext, oneVideo, VideoTask.TYPE_VIDEO)
                .setVideoListener(this::addVideo).start();

        refreshUserInfo();
    }

    @UiThread void addVideo(Video video) {
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
        List<VideoTag> tags = new LinkedList<>();
        for (int i = 0; i < video.tags.size(); i++) {
            tags.add(new BaseVideoTag(video.tags.get(i).name, video.tags.get(i).x, video.tags.get(i).y));
        }
        NetworkVideo videoResource = new NetworkVideo(tags, videoFile.getPath());
        videoPlayView.addVideoResource(videoResource);
        videoPlayView.setPreview(FileUtil.getThumbnailStoreFile(mContext, video, FileUtil.Type.LARGE_THUMB));
        videoPlayView.addAvatarUrl(((TagVideo) video).avatar);
        onLoad();
    }

    @Click(R.id.voteCountTextView)
    @Background void voteClick() {
        oneVideo.liked = !oneVideo.liked;
        oneVideo.likeNum += oneVideo.liked ? 1 : -1;
        refreshUserInfo();
        if (oneVideo.liked) {
            mWorld.likeVideo(oneVideo._id, AccountManager.getUserInfo(mContext)._id);
        } else {
            mWorld.unlikeVideo(oneVideo._id, AccountManager.getUserInfo(mContext)._id);
        }
    }

    @UiThread void refreshUserInfo() {
        if (oneVideo.liked) {
            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall_Inverse);
            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_orange, 0, 0, 0);
        } else {
            voteCountTextView.setTextAppearance(mContext, R.style.TextAppearance_PlaySmall);
            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_gray, 0, 0, 0);
        }
        voteCountTextView.setText(oneVideo.likeNum + "");
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }
}
