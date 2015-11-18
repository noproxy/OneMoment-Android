package co.yishun.onemoment.app.ui.play;

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

import co.yishun.library.OnemomentPlayerView;
import co.yishun.library.resource.BaseVideoResource;
import co.yishun.library.resource.LocalVideo;
import co.yishun.library.resource.TaggedVideo;
import co.yishun.library.resource.VideoResource;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.loader.VideoDownloadTask;
import co.yishun.onemoment.app.api.loader.VideoTaskManager;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_tag_video)
public class PlayTagVideoFragment extends BaseFragment {
    @FragmentArg
    TagVideo oneVideo;
    @ViewById
    ImageView avatar;
    @ViewById
    TextView usernameTextView;
    @ViewById
    OnemomentPlayerView videoPlayView;
    @ViewById
    TextView voteCountTextView;
    private World mWorld = OneMomentV3.createAdapter().create(World.class);

    @AfterViews
    void setup() {
        VideoTaskManager.getInstance().init(this.getActivity());
        Log.d("oneVideo", oneVideo.toString());
        Picasso.with(this.getActivity()).load(oneVideo.avatar).into(avatar);

        usernameTextView.setText(oneVideo.nickname);

        videoPlayView.setSinglePlay(true);

        File fileSynced = FileUtil.getWorldVideoStoreFile(this.getActivity(), oneVideo);
        if (fileSynced.exists()) {
            addVideo(oneVideo, fileSynced);
        } else {
            VideoDownloadTask task = VideoTaskManager.getInstance().addDownloadTask(null, oneVideo);
//            task.setListener(this::addVideo);
        }

        refreshUserInfo();
    }

    @UiThread
    void addVideo(Video tagVideo, File fileSynced) {
        VideoResource videoResource = new LocalVideo(new BaseVideoResource(), fileSynced.getPath());
        List<VideoTag> tags = new LinkedList<VideoTag>();
        for (int i = 0; i < tagVideo.tags.size(); i++) {
            tags.add(new BaseVideoTag(tagVideo.tags.get(i).name, tagVideo.tags.get(i).x / 100f, tagVideo.tags.get(i).y / 100f));
        }
        videoResource = new TaggedVideo(videoResource, tags);
        videoPlayView.addVideoResource(videoResource);
    }

    @Click(R.id.videoPlayView)
    void videoClick() {
        if (videoPlayView.isPlaying()) {
            videoPlayView.pause();
        } else {
            videoPlayView.start();
        }
    }

    @Click(R.id.voteCountTextView)
    @Background
    void voteClick() {
        ApiModel model;
        if (oneVideo.liked) {
            model = mWorld.unlikeVideo(oneVideo._id, AccountHelper.getUserInfo(this.getActivity())._id);
        } else {
            model = mWorld.likeVideo(oneVideo._id, AccountHelper.getUserInfo(this.getActivity())._id);
        }
        if (model.code == 1) {
            oneVideo.liked = !oneVideo.liked;
            if (oneVideo.liked) {
                oneVideo.likeNum++;
            } else {
                oneVideo.likeNum--;
            }
            refreshUserInfo();
        }
    }

    @UiThread
    void refreshUserInfo() {
        if (oneVideo.liked) {
            voteCountTextView.setTextAppearance(this.getActivity(), R.style.TextAppearance_PlaySmall_Inverse);
            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_orange, 0, 0, 0);
        } else {
            voteCountTextView.setTextAppearance(this.getActivity(), R.style.TextAppearance_PlaySmall);
            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_gray, 0, 0, 0);
        }
        voteCountTextView.setText(oneVideo.likeNum + "");
    }

    @Override
    public void onPause() {
        super.onPause();
        if (videoPlayView != null) {
            videoPlayView.pause();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        VideoTaskManager.getInstance().quit();
        if (videoPlayView != null) {
            videoPlayView.stop();
        }
    }
}
