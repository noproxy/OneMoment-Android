package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.util.Matrix;
import com.j256.ormlite.dao.Dao;
import com.qiniu.android.storage.UploadManager;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.MomentLock;
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.play.PlayMomentFragment;
import co.yishun.onemoment.app.ui.play.PlayMomentFragment_;
import co.yishun.onemoment.app.ui.share.ShareActivity;
import co.yishun.onemoment.app.ui.share.ShareActivity_;
import co.yishun.onemoment.app.video.VideoCommand;
import co.yishun.onemoment.app.video.VideoConcat;

@EActivity(R.layout.activity_play_moment)
public class PlayMomentActivity extends BaseActivity {

    private static final String TAG = "PlayMomentActivity";
    @Extra String startDate;
    @Extra String endDate;

    @ViewById Toolbar toolbar;
    @ViewById FrameLayout containerFrameLayout;

    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;

    private PlayMomentFragment playMomentFragment;
    private List<Moment> playingMoments;
    private File videoCacheFile;

    @AfterViews void setUpViews() {
    }

    @AfterViews void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        LogUtil.i("setupToolbar", "set home as up true");
    }

    @Override protected void onResume() {
        super.onResume();
        try {
            playingMoments = new ArrayList<>();
            List<Moment> momentInDatabase = momentDao.queryBuilder().where().eq("owner", AccountManager.getAccountId(this)).and()
                    .between("time", startDate, endDate).query();
            for (Moment m : momentInDatabase) {
                if (m.getFile().length() > 0) {
                    playingMoments.add(m);
                    MomentLock.lockMoment(this, m);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        playMomentFragment = PlayMomentFragment_.builder().startDate(startDate).endDate(endDate).build();
        getSupportFragmentManager().beginTransaction().replace(R.id.containerFrameLayout, playMomentFragment).commit();
    }

    @Override protected void onPause() {
        super.onPause();
        try {
            for (Moment m : playingMoments) {
                MomentLock.unlockMomentIfLocked(this, m);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_play_moment, menu);
        return true;
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.activity_play_moment_share) {
            //TODO add share moment here
            shareClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Background void shareClick() {
        showProgress();
        concatSelectedVideos();
        if (videoCacheFile == null) {
            //TODO check append videos failed
            return;
        }
        uploadAndShare();
    }

    void concatSelectedVideos() {
        List<File> files = new ArrayList<>();
        Collections.sort(playingMoments);
        for (Moment moment : playingMoments) {
            files.add(new File(moment.getPath()));
        }
        videoCacheFile = FileUtil.getCacheFile(this, Constants.LONG_VIDEO_PREFIX + AccountManager.getUserInfo(this)._id
                + Constants.URL_HYPHEN + playingMoments.size() + Constants.URL_HYPHEN
                + Util.unixTimeStamp() + Constants.VIDEO_FILE_SUFFIX);

        List<File> filesNeedTrans = new ArrayList<>();
        try {
            for (File f : files) {
                Movie movie = MovieCreator.build(f.getPath());
                for (Track t : movie.getTracks()) {
                    if (!t.getTrackMetaData().getMatrix().equals(Matrix.ROTATE_0)) {
                        filesNeedTrans.add(f);
                        break;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        new VideoConcat(this)
                .setTransFile(filesNeedTrans)
                .setConcatFile(files, videoCacheFile)
                .setListener(new VideoCommand.VideoCommandListener() {
                    @Override public void onSuccess(VideoCommand.VideoCommandType type) {
                        switch (type) {
                            case COMMAND_TRANSPOSE:
                                LogUtil.d(TAG, "onTransSuccess: ");
                                break;
                            case COMMAND_FORMAT:
                                LogUtil.d(TAG, "onFormatSuccess: ");
                                break;
                            case COMMAND_CONCAT:
                                LogUtil.d(TAG, "onConcatSuccess: ");
                                afterConcat();
                                break;
                        }
                    }

                    @Override public void onFail(VideoCommand.VideoCommandType type) {
                        LogUtil.d(TAG, "onFail: ");
                    }
                }).start();
    }

    void afterConcat() {
        if (videoCacheFile == null) {
            //TODO check append videos failed
            return;
        }
        uploadAndShare();
    }

    @SupposeBackground void uploadAndShare() {
        showProgress();
        UploadManager uploadManager = new UploadManager();
        LogUtil.d(TAG, "upload " + videoCacheFile.getName());
        UploadToken token = OneMomentV3.createAdapter().create(Misc.class).getUploadToken(videoCacheFile.getName());
        if (token.code <= 0) {
            LogUtil.e(TAG, "get upload token error: " + token.msg);
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        uploadManager.put(videoCacheFile, videoCacheFile.getName(), token.token,
                (s, responseInfo, jsonObject) -> {
                    LogUtil.i(TAG, responseInfo.toString());
                    if (responseInfo.isOK()) {
                        LogUtil.d(TAG, "loaded " + responseInfo.path);
                        LogUtil.i(TAG, "profile upload ok");
                    } else {
                        LogUtil.e(TAG, "profile upload error: " + responseInfo.error);
                    }
                    latch.countDown();
                }, null
        );
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Gson gson = new Gson();
        JsonArray allTagArray = new JsonArray();
        for (Moment m : playingMoments) {
            JsonArray momentTags = new JsonArray();
            for (OMLocalVideoTag tag : RealmHelper.getTags(m.getTime())) {
                JsonObject element = new JsonObject();
                String[] position = tag.getTagPosition().split(" ");
                element.addProperty("name", tag.getTagText());
                element.addProperty("x", Float.valueOf(position[0]));
                element.addProperty("y", Float.valueOf(position[1]));
                momentTags.add(element);
            }
            allTagArray.add(momentTags);
        }
        String tags = gson.toJson(allTagArray);
        LogUtil.d(TAG, tags);

        Account account = OneMomentV3.createAdapter().create(Account.class);
        ShareInfo shareInfo = account.share(videoCacheFile.getName(), AccountManager.getUserInfo(this)._id, tags);

        videoCacheFile.delete();
        hideProgress();

        ShareActivity_.intent(this).shareInfo(shareInfo).shareType(ShareActivity.TYPE_SHARE_MOMENT).start();
    }

    @Override public void setPageInfo() {
        mPageName = "PlayMomentActivity";
    }
}
