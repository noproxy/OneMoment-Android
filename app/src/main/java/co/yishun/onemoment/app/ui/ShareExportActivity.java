package co.yishun.onemoment.app.ui;

import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.widget.TextView;

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
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.ViewById;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import co.yishun.library.calendarlibrary.DayView;
import co.yishun.library.calendarlibrary.MomentCalendar;
import co.yishun.library.calendarlibrary.MomentMonthView;
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
import co.yishun.onemoment.app.ui.share.ShareFragment;
import co.yishun.onemoment.app.ui.share.ShareFragment_;

@EActivity(R.layout.activity_share_export)
public class ShareExportActivity extends BaseActivity
        implements MomentMonthView.MonthAdapter, DayView.OnMomentSelectedListener {

    private static final String TAG = "ShareExportActivity";
    @ViewById Toolbar toolbar;
    @ViewById MomentCalendar momentCalendar;
    @ViewById TextView shareText;
    @ViewById TextView exportText;
    @ViewById TextView selectAllText;
    @ViewById TextView clearText;
    @ViewById TextView selectedText;

    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;

    private List<Moment> allMoments;
    private List<Moment> selectedMoments;
    private File videoCacheFile;

    @AfterViews void setupViews() {
        momentCalendar.setAdapter(this);
        DayView.setOnMomentSelectedListener(this);
        DayView.setMultiSelection(true);
        try {
            allMoments = momentDao.queryBuilder().where().eq("owner", AccountManager.getAccountId(this)).query();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        selectedMoments = new LinkedList<>();
        updateSelectedText();
    }

    @AfterViews void setAppbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_share_export_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_action_back_close);
    }

    @Click(R.id.shareText) @Background void shareTextClicked() {
        if (selectedMoments.size() == 0) {
            showSnackMsg("Select at least one to share");
            return;
        }
        showProgress();
        appendSelectedVideos();
        if (videoCacheFile == null) {
            //TODO check append videos failed
            return;
        }
        uploadAndShare();
    }

    @Click(R.id.exportText) void exportTextClicked() {
        if (selectedMoments.size() == 0) {
            showSnackMsg("Select at least one to share");
            return;
        }
        showProgress();
        appendSelectedVideos();
        if (videoCacheFile == null) {
            //TODO check append videos failed
            return;
        }
        File outFile = FileUtil.getExportVideoFile();
        Log.d(TAG, "out : " + outFile.getPath());
        Log.d(TAG, "origin : " + videoCacheFile.getPath());
        FileUtil.copyFile(videoCacheFile, outFile);
        videoCacheFile.delete();
        hideProgress();
        showSnackMsg("Export success");
    }

    @Click(R.id.selectAllText) void selectAllTextClicked() {
        selectedMoments.clear();
        selectedMoments.addAll(allMoments);
        setAllSelect(true);
        updateSelectedText();
    }

    @Click(R.id.clearText) void clearTextClicked() {
        selectedMoments.clear();
        momentCalendar.getAdapter().notifyDataSetChanged();
        setAllSelect(false);
        updateSelectedText();
    }

    /**
     * invalidate() and notifyDataSetChanged() don't work.
     */
    void setAllSelect(boolean select) {
        for (int monthIndex = 0; monthIndex < momentCalendar.getChildCount(); monthIndex++) {
            if (momentCalendar.getChildAt(monthIndex) instanceof MomentMonthView) {
                MomentMonthView monthView = (MomentMonthView) momentCalendar.getChildAt(monthIndex);
                for (int dayIndex = 0; dayIndex < monthView.getChildCount(); dayIndex++) {
                    if (monthView.getChildAt(dayIndex) instanceof DayView) {
                        DayView dayView = (DayView) monthView.getChildAt(dayIndex);
                        if (dayView.isEnabled()) dayView.setSelected(select);
                    }
                }
            }
        }
    }

    void updateSelectedText() {
        String content = String.format(getResources().getString(R.string.activity_share_export_selected),
                selectedMoments.size(), allMoments.size());
        SpannableString ss = new SpannableString(content);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSecondary)), content.indexOf(" "),
                content.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        selectedText.setText(ss);
    }

    @Override public void onBindView(Calendar calendar, DayView dayView) {
        String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(calendar.getTime());
        try {
            Moment moment = momentDao.queryBuilder().where().eq("time", time).queryForFirst();
            for (Moment m : allMoments) {
                if (TextUtils.equals(m.getTime(), time)) {
                    moment = m;
                    break;
                }
            }
            if (moment != null) {
                dayView.setEnabled(true);
                dayView.setTag(moment);
                Picasso.with(this).load(new File(moment.getThumbPath())).into(dayView);
                if (selectedMoments.contains(moment))
                    dayView.setSelected(true);
                else dayView.setSelected(false);
                Log.i(TAG, "moment found: " + moment.getTime());
            } else {
                dayView.setEnabled(false);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override public void onSelected(DayView dayView) {
        Moment moment = (Moment) dayView.getTag();
        if (moment != null) {
            if (selectedMoments.contains(moment)) {
                selectedMoments.remove(moment);
            } else {
                selectedMoments.add(moment);
            }
            updateSelectedText();
        }
    }

    @Override public void onBackPressed() {
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(ShareFragment.TAG);
        if (fragment != null) {
            getSupportFragmentManager().beginTransaction().remove(fragment).commit();
        } else {
            super.onBackPressed();
        }
    }

    void appendSelectedVideos() {
        List<String> paths = new ArrayList<>();
        Collections.sort(selectedMoments);
        try {
            for (Moment moment : selectedMoments) {
                paths.add(moment.getPath());
                MomentLock.lockMoment(this, moment);
            }
        } catch (Exception e) {
            e.printStackTrace();
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

            videoCacheFile = FileUtil.getCacheFile(this, Constants.LONG_VIDEO_PREFIX + AccountManager.getUserInfo(this)._id
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
        try {
            for (Moment moment : selectedMoments) {
                MomentLock.unlockMomentIfLocked(this, moment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SupposeBackground void uploadAndShare() {
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
        ShareInfo shareInfo = account.share(videoCacheFile.getName(), AccountManager.getUserInfo(this)._id, tags);

        videoCacheFile.delete();
        hideProgress();

        ShareFragment shareFragment = ShareFragment_.builder()
                .shareInfo(shareInfo).build();
        getSupportFragmentManager().beginTransaction()
                .add(android.R.id.content, shareFragment, ShareFragment.TAG).commit();
    }

    @Override public void setPageInfo() {
        mIsPage = true;
        mPageName = "ShareExportActivity";
    }
}
