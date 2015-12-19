package co.yishun.onemoment.app.ui.play;

import android.content.Context;
import android.util.Log;
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
import java.io.IOException;
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
import co.yishun.onemoment.app.api.loader.VideoTaskManager;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_world)
public class PlayWorldFragment extends BaseFragment implements OnemomentPlayerView.OnVideoChangeListener,
        VideoTask.OnVideoListener {
    private static final String TAG = "platworld";
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
    private Context mContext;
    private boolean mReady = false;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Background
    void getData() {
        List<TagVideo> videos = mWorld.getVideoOfTag(worldTag.name, offset, 10, AccountManager.getUserInfo(mContext)._id, seed);
        if (videos.size() == 0) {
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

    @AfterViews
    void setupView() {
        videoPlayView.setWithAvatar(true);
        videoPlayView.setVideoChangeListener(this);
        getData();
    }

    @Override
    public void onVideoLoad(Video video) {
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
        videoPlayView.setToLocal(video.domain + video.fileName, videoFile.getPath());
        if (!mReady) {
            backgroundGetThumb(video, videoFile.getPath());
        }
    }

    @UiThread
    void getThumb(File large) {
        Log.d(TAG, "set preview");
        if (large.length() == 0) {
            Log.e(TAG, "file error");
        }
        videoPlayView.setPreview(large);
    }

    @UiThread
    void addVideo(TagVideo video) {
        File videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);
        List<VideoTag> tags = new LinkedList<>();
        for (int i = 0; i < video.tags.size(); i++) {
            tags.add(new BaseVideoTag(video.tags.get(i).name, video.tags.get(i).x, video.tags.get(i).y));
        }
        NetworkVideo videoResource = new NetworkVideo(video.domain + video.fileName, tags);
        if (videoFile.length() > 0) {
            videoResource.setPath(videoFile.getPath());
            if (!mReady) {
                backgroundGetThumb(video, videoFile.getPath());
            }
        } else {
            new VideoTask(mContext, video, VideoTask.TYPE_VIDEO)
                    .setVideoListener(this).start();
        }

        videoPlayView.addVideoResource(videoResource);
        tagVideos.add(video);
        videoPlayView.addAvatarUrl((video).avatar);
    }

    @Background
    void backgroundGetThumb(Video video, String path) {
        mReady = true;
        File thumb = FileUtil.getThumbnailStoreFile(mContext, video, FileUtil.Type.LARGE_THUMB);
        try {
            for (int i = 0; i < 3; i++) {
                if (thumb.length() > 0) break;
                VideoUtil.createLargeThumbImage(mContext, video, path);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        getThumb(thumb);
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
        tagVideos.get(voteIndex).liked = !tagVideos.get(voteIndex).liked;
        tagVideos.get(voteIndex).likeNum += tagVideos.get(voteIndex).liked ? 1 : -1;
        refreshUserInfo(videoPlayView.getCurrentIndex());
        if (tagVideos.get(voteIndex).liked) {
            mWorld.likeVideo(tagVideos.get(voteIndex)._id, AccountManager.getUserInfo(mContext)._id);
        } else {
            mWorld.unlikeVideo(tagVideos.get(voteIndex)._id, AccountManager.getUserInfo(mContext)._id);
        }
    }

    @UiThread
    void refreshUserInfo(int index) {
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
