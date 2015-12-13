package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.googlecode.mp4parser.BasicContainer;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.AppendTrack;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.Account;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.play.PlayMomentFragment;
import co.yishun.onemoment.app.ui.play.PlayMomentFragment_;
import co.yishun.onemoment.app.ui.share.ShareFragment;
import co.yishun.onemoment.app.ui.share.ShareFragment_;

@EActivity(R.layout.activity_play_moment)
public class PlayMomentActivity extends BaseActivity {

    private static final String TAG = "PlayMomentActivity";
    @Extra String startDate;
    @Extra String endDate;

    @ViewById Toolbar toolbar;
    @ViewById FrameLayout containerFrameLayout;

    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;

    private PlayMomentFragment playMomentFragment;
    private List<Moment> selectedMoments;
    private File videoCacheFile;

    @AfterViews void setUpViews() {
        playMomentFragment = PlayMomentFragment_.builder().startDate(startDate).endDate(endDate).build();
        getSupportFragmentManager().beginTransaction().replace(R.id.containerFrameLayout, playMomentFragment).commit();
    }

    @AfterViews void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        Log.i("setupToolbar", "set home as up true");
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

    @Background void shareClick(){
        appendSelectedVideos();
        if (videoCacheFile == null) {
            //TODO check append videos failed
            return ;
        }
        uploadAndShare();
    }

    void appendSelectedVideos() {
        try {
            selectedMoments = momentDao.queryBuilder().where().between("time", startDate, endDate).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        List<String> paths = new ArrayList<>();
        Collections.sort(selectedMoments);
        for (Moment moment : selectedMoments) {
            paths.add(moment.getPath());
        }
        try {
            int count = paths.size();
            Movie[] inMovies = new Movie[count];
            for (int i = 0; i < count; i++) {
                inMovies[i] = MovieCreator.build(paths.get(i));
            }
            List<Track> videoTracks = new LinkedList<Track>();
            List<Track> audioTracks = new LinkedList<Track>();
            for (Movie m : inMovies) {
                for (Track t : m.getTracks()) {
                    if (t.getHandler().equals("soun")) {
                        audioTracks.add(t);
                    }
                    if (t.getHandler().equals("vide")) {
                        videoTracks.add(t);
                    }
                }
            }

            Movie result = new Movie();

            if (audioTracks.size() > 0) {
                result.addTrack(new AppendTrack(audioTracks
                        .toArray(new Track[audioTracks.size()])));
            }
            if (videoTracks.size() > 0) {
                result.addTrack(new AppendTrack(videoTracks
                        .toArray(new Track[videoTracks.size()])));
            }

            BasicContainer out = (BasicContainer) new DefaultMp4Builder()
                    .build(result);

            videoCacheFile = FileUtil.getCacheFile(this, Constants.LONG_VIDEO_PREFIX
                    + AccountHelper.getUserInfo(this)._id
                    + Constants.URL_HYPHEN + count + Constants.URL_HYPHEN
                    + Util.unixTimeStamp() + Constants.VIDEO_FILE_SUFFIX);
            FileChannel fc = new RandomAccessFile(videoCacheFile, "rw").getChannel();
            out.writeContainer(fc);
            fc.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @SupposeBackground void uploadAndShare() {
        showProgress();
        UploadManager uploadManager = new UploadManager();
        Log.d(TAG, "upload " + videoCacheFile.getName());
        UploadToken token = OneMomentV3.createAdapter().create(Misc.class).getUploadToken(videoCacheFile.getName());
        if (token.code <= 0) {
            Log.e(TAG, "get upload token error: " + token.msg);
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        uploadManager.put(videoCacheFile, videoCacheFile.getName(), token.token,
                (s, responseInfo, jsonObject) -> {
                    Log.i(TAG, responseInfo.toString());
                    if (responseInfo.isOK()) {
                        Log.d(TAG, "loaded " + responseInfo.path);
                        Log.i(TAG, "profile upload ok");
                    } else {
                        Log.e(TAG, "profile upload error: " + responseInfo.error);
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
        for (Moment m : selectedMoments) {
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
        Log.d(TAG, tags);

        Account account = OneMomentV3.createAdapter().create(Account.class);
        ShareInfo shareInfo = account.share(videoCacheFile.getName(), AccountHelper.getUserInfo(this)._id, tags);

        videoCacheFile.delete();
        hideProgress();

        ShareFragment shareFragment = ShareFragment_.builder()
                .shareInfo(shareInfo).build();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, shareFragment, ShareFragment.TAG).commit();
    }

    @Nullable @Override public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @Override public void setPageInfo() {
        mPageName = "PlayMomentActivity";
    }
}
