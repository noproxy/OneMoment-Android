package co.yishun.onemoment.app.ui.play;

import android.content.Context;
import android.net.Uri;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.FragmentArg;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import co.yishun.library.AvatarRecyclerView;
import co.yishun.library.TagContainer;
import co.yishun.library.tag.BaseVideoTag;
import co.yishun.library.tag.VideoTag;
import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.APIV4;
import co.yishun.onemoment.app.api.authentication.OneMomentV4;
import co.yishun.onemoment.app.api.loader.VideoAsyncTask;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;
import co.yishun.onemoment.app.api.modelv4.WorldProvider;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.api.modelv4.WorldVideoListWithErrorV4;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.function.Consumer;
import co.yishun.onemoment.app.ui.common.BaseFragment;
import co.yishun.onemoment.app.util.SerialExecutor;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

import static co.yishun.onemoment.app.api.loader.VideoAsyncTask.endTask;
import static co.yishun.onemoment.app.api.loader.VideoAsyncTask.isEnd;
import static co.yishun.onemoment.app.api.loader.VideoAsyncTask.isInvalid;

/**
 * Created on 2015/10/28.
 */
@EFragment(R.layout.fragment_play_videos)
public class PlayVideosFragment extends BaseFragment implements Consumer<VideoProvider>, View.OnClickListener {

    private static final String TAG = "PlayWorldFragment";
    private final Object OffsetLock = new Object();
    @FragmentArg
    WorldProvider world;
    @PlayType
    @FragmentArg
    int playType;
    @ViewById
    SurfaceView surfaceView;
    @ViewById
    TextView usernameTextView;
    @ViewById
    MaterialProgressBar progressBar;
    @ViewById
    View playBtnContainer;
    boolean avatarAdded = false;
    @ViewById
    AvatarRecyclerView avatarView;
    @ViewById
    View playBtn;
    @ViewById
    TagContainer tagsContainer;
    List<VideoTag> tags = new LinkedList<>();
    private SerialExecutor mExecutor = new SerialExecutor();
    private APIV4 mApiV4 = OneMomentV4.createAdapter().create(APIV4.class);
    private BlockingQueue<VideoProvider> mQueue = new LinkedBlockingQueue<>();
    private LinkedHashMap<Uri, VideoProvider> mVideoMap = new LinkedHashMap<>();
    private Integer offset = 0;
    private TurnsPlayer mPlayer;
    private boolean loaded = false;

    @UiThread
    void showProgress() {
        progressBar.setVisibility(View.VISIBLE);
    }

    @UiThread
    void hideProgress() {
        progressBar.setVisibility(View.INVISIBLE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mExecutor.clear();
    }

    @AfterViews
    void setViews() {
        playBtnContainer.setOnClickListener(this);
        mPlayer = new TurnsPlayer(surfaceView.getContext());
        surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                mPlayer.setDisplay(holder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
        mPlayer.setProvider(new TurnsPlayer.PlayListener() {
            @Override
            public void onOneCompletion(Uri uri) {
                if (avatarView != null) {
                    avatarView.scrollToNext();
                }
            }

            @Override
            public void onOneStart(Uri uri) {
                String name = mVideoMap.get(uri).getNickname();
                updateUsername(name);

                tags.clear();
                VideoProvider video = mVideoMap.get(uri);
                for (int i = 0; i < video.getTags().size(); i++) {
                    tags.add(new BaseVideoTag(video.getTags().get(i).name,
                            video.getTags().get(i).x, video.getTags().get(i).y));
                }
                tagsContainer.setVideoTags(tags);
            }

            @Override
            public Uri onMoreAsked() {
                try {
                    if (mQueue.isEmpty()) {
                        showProgress();
                    }


                    VideoProvider videoProvider = mQueue.take();
                    LogUtil.d(TAG, "videoProvider: " + videoProvider);

                    if (isEnd(videoProvider)) {
                        LogUtil.d(TAG, "end video");
                        hideProgress();
                        mExecutor.clear();
                        mQueue.clear();

                        onEnd();
                        mPlayer.release();

                        boolean nameSet = false;
                        for (VideoProvider video : mVideoMap.values()) {
                            mQueue.offer(video);
                            if (!nameSet) {
                                nameSet = true;
                                updateUsername(video.getNickname());
                            }
                        }

                        mPlayer = new TurnsPlayer(surfaceView.getContext());
                        mPlayer.setDisplay(surfaceView.getHolder());
                        mPlayer.setProvider(this);
                        mPlayer.prepare();
                        return null;
                    } else if (isInvalid(videoProvider)) {
                        LogUtil.d(TAG, "invalid video");
                        onSkip();
                        return onMoreAsked();
                    } else {
                        LogUtil.d(TAG, "normal video");
                        hideProgress();
                        return Uri.fromFile(FileUtil.getWorldVideoStoreFile(surfaceView.getContext(), videoProvider));
                    }
                } catch (InterruptedException ignore) {
                }
                return null;
            }
        });
    }

    @UiThread
    void onSkip() {
        avatarView.scrollToNext();
    }

    @UiThread
    void onEnd() {
        playBtn.setVisibility(View.VISIBLE);
        avatarView.scrollToZero();
    }

    @Background
    @AfterViews
    void getData() {
        Context context = getContext();
        if (context != null) {
            synchronized (OffsetLock) {
                LogUtil.d(TAG, "offset lock");
                // not background to ensure offset access only by one thread
                WorldVideoListWithErrorV4<WorldVideo> videos = playType ==
                        PlayType.TYPE_WORLD ?
                        mApiV4.getWorldVideos(world.getId(), AccountManager.getAccountId(context),
                                offset, 6) : mApiV4.getTodayVideos(world.getName(), offset, 6);
                if (videos.size() == 0) {
                    if (offset == 0) {
//                    onLoadError(R.string.fragment_play_world_video_null);
                    } else {
                        endTask(this).executeOnExecutor(mExecutor);
                    }
                    return;//TODO will OOM if this world contains so many many videos
                } else {
                    LogUtil.d(TAG, "offset add: " + videos.size());
                    offset += videos.size();

                    for (VideoProvider oneVideo : videos) {
                        new VideoAsyncTask(context, this).executeOnExecutor(mExecutor, oneVideo);
                        addAvatar(oneVideo);
                    }
                }
            }
            getData();
        }
    }

    @UiThread
    void updateUsername(String name) {
        usernameTextView.setText(name);
    }

    @UiThread
    void addAvatar(VideoProvider video) {
        if (playType != PlayType.TYPE_WORLD || !avatarAdded) {
            avatarView.addAvatar(video.getAvatarUrl());
            avatarAdded = true;
        }
    }

    @Override
    public void setPageInfo() {
        mIsPage = false;
    }

    @Override
    public void accept(VideoProvider video) {
        LogUtil.d(TAG, "offer: " + video);

        Uri uri = Uri.fromFile(FileUtil.getWorldVideoStoreFile(surfaceView.getContext(), video));
        mVideoMap.put(uri, video);
        mQueue.offer(video);

        if (!isInvalid(video) && !loaded) {
            loaded = true;
            mPlayer.prepare();
            mPlayer.start();
        }


    }

    @Override
    public void onClick(View v) {
        // pause, resume or replay
        if (mPlayer != null) {
            if (mPlayer.isPlaying()) {
                mPlayer.pause();
                playBtn.setVisibility(View.VISIBLE);
            } else {
                mPlayer.start();
                playBtn.setVisibility(View.INVISIBLE);
            }
        }
    }
}
