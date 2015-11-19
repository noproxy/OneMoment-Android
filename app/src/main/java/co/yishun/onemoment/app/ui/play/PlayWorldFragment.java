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
import co.yishun.onemoment.app.api.loader.VideoTask;
import co.yishun.onemoment.app.api.loader.VideoTaskManager;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_world)
public class PlayWorldFragment extends BaseFragment implements OnemomentPlayerView.OnVideoChangeListener {
    @FragmentArg
    WorldTag worldTag;
    @ViewById
    OnemomentPlayerView videoPlayView;
    @ViewById
    TextView voteCountTextView;
    @ViewById
    TextView usernameTextView;
    private World mWorld = OneMomentV3.createAdapter().create(World.class);
    private List<TagVideo> tagVideos = new ArrayList<>();
    private int voteIndex;
    private Seed seed;
    private int offset = 0;

    @Background
    void getData() {
        List<TagVideo> videos = mWorld.getVideoOfTag(worldTag.name, offset, 10, AccountHelper.getUserInfo(this.getActivity())._id, seed);
        if (videos.size() == 0) {
            return;
        }
        offset += videos.size();
        seed = videos.get(0).seed;

        for (TagVideo oneVideo : videos) {
            if (this.getActivity() == null) {
                return;
            }

//            if (fileSynced.exists()) {
//                addVideo(oneVideo, fileSynced);
//            } else {
////                VideoDownloadTask task = VideoTaskManager.getInstance().addDownloadTask(null, oneVideo);
////                task.setListener(this::addVideo);
//            }
            new VideoTask(this.getActivity(), oneVideo, VideoTask.TYPE_VIDEO_ONLY)
                    .setVideoListener(this::addVideo).start();
        }
        getData();
    }

    @AfterViews
    void setupView() {
//        VideoTaskManager.getInstance().init(this.getActivity());

        videoPlayView.setSinglePlay(false);
        videoPlayView.setVideoChangeListener(this);
        getData();
    }

    @UiThread
    void addVideo(Video tagVideo) {
        File fileSynced = FileUtil.getWorldVideoStoreFile(this.getActivity(), tagVideo);
        VideoResource videoResource = new LocalVideo(new BaseVideoResource(), fileSynced.getPath());
        List<VideoTag> tags = new LinkedList<VideoTag>();
        for (int i = 0; i < tagVideo.tags.size(); i++) {
            tags.add(new BaseVideoTag(tagVideo.tags.get(i).name, tagVideo.tags.get(i).x / 100f, tagVideo.tags.get(i).y / 100f));
        }
        videoResource = new TaggedVideo(videoResource, tags);
        videoPlayView.addVideoResource(videoResource);
        tagVideos.add((TagVideo) tagVideo);
        videoPlayView.addAvatarUrl(((TagVideo) tagVideo).avatar);
    }

    @Click(R.id.videoPlayView)
    void videoClick() {
        if (videoPlayView.isPlaying()) {
            videoPlayView.pause();
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        } else {
            videoPlayView.start();
            getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Click(R.id.voteCountTextView)
    @Background
    void voteClick() {
        voteIndex = videoPlayView.getCurrentIndex();
        ApiModel model;
        if (tagVideos.get(voteIndex).liked) {
            model = mWorld.unlikeVideo(tagVideos.get(voteIndex)._id, AccountHelper.getUserInfo(this.getActivity())._id);
        } else {
            model = mWorld.likeVideo(tagVideos.get(voteIndex)._id, AccountHelper.getUserInfo(this.getActivity())._id);
        }
        if (model.code == 1) {
            tagVideos.get(voteIndex).liked = !tagVideos.get(voteIndex).liked;
            if (tagVideos.get(voteIndex).liked) {
                tagVideos.get(voteIndex).likeNum++;
            } else {
                tagVideos.get(voteIndex).likeNum--;
            }
            refreshUserInfo(videoPlayView.getCurrentIndex());
        }
    }

    @UiThread
    void refreshUserInfo(int index) {
        if (tagVideos.get(index).liked) {
            voteCountTextView.setTextAppearance(this.getActivity(), R.style.TextAppearance_PlaySmall_Inverse);
            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_orange, 0, 0, 0);
        } else {
            voteCountTextView.setTextAppearance(this.getActivity(), R.style.TextAppearance_PlaySmall);
            voteCountTextView.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_world_play_like_gray, 0, 0, 0);
        }
        voteCountTextView.setText(tagVideos.get(index).likeNum + "");
        usernameTextView.setText(tagVideos.get(index).nickname);
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

    @Override
    public void videoChangeTo(int index) {
        if (index == 0) {
            getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
        refreshUserInfo(index);
    }

}
