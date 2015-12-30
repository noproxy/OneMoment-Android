package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.util.Matrix;
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
import java.io.IOException;
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
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.share.ShareActivity;
import co.yishun.onemoment.app.ui.share.ShareActivity_;
import co.yishun.onemoment.app.video.VideoCommand;
import co.yishun.onemoment.app.video.VideoConcat;

import co.yishun.onemoment.app.data.MomentLock;

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
    private boolean concatExport = true;

    @AfterViews void setupViews() {
        momentCalendar.setAdapter(this);
        DayView.setOnMomentSelectedListener(this);
        DayView.setMultiSelection(true);
        try {
            List<Moment> momentInDatabase = momentDao.queryBuilder().where()
                    .eq("owner", AccountManager.getAccountId(this)).query();
            allMoments = new ArrayList<>();
            for (Moment moment : momentInDatabase) {
                if (moment.getFile().length() > 0) {
                    allMoments.add(moment);
                }
            }
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
        concatExport = false;
        concatSelectedVideos();
    }

    @Click(R.id.exportText) @Background void exportTextClicked() {
        if (selectedMoments.size() == 0) {
            showSnackMsg("Select at least one to share");
            return;
        }
        showProgress();
        concatExport = true;
        concatSelectedVideos();
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

        Moment moment = null;
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
            LogUtil.i(TAG, "moment found: " + moment.getTime());
        } else {
            dayView.setEnabled(false);
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

    void concatSelectedVideos() {
        List<File> files = new ArrayList<>();
        Collections.sort(selectedMoments);
        try {
            for (Moment moment : selectedMoments) {
                files.add(new File(moment.getPath()));
                MomentLock.lockMoment(this, moment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        videoCacheFile = FileUtil.getCacheFile(this, Constants.LONG_VIDEO_PREFIX + AccountManager.getUserInfo(this)._id
                + Constants.URL_HYPHEN + selectedMoments.size() + Constants.URL_HYPHEN
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

    @Background void afterConcat() {
        try {
            for (Moment moment : selectedMoments) {
                MomentLock.unlockMomentIfLocked(this, moment);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (videoCacheFile == null) {
            //TODO check append videos failed
            return;
        }
        if (concatExport) {
            File outFile = FileUtil.getExportVideoFile();
            LogUtil.d(TAG, "out : " + outFile.getPath());
            LogUtil.d(TAG, "origin : " + videoCacheFile.getPath());
            FileUtil.copyFile(videoCacheFile, outFile);
            videoCacheFile.delete();
            hideProgress();
            showSnackMsg("Export success");
        } else
            uploadAndShare();
    }

    @SupposeBackground void uploadAndShare() {
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
        LogUtil.d(TAG, tags);

        Account account = OneMomentV3.createAdapter().create(Account.class);
        ShareInfo shareInfo = account.share(videoCacheFile.getName(), AccountManager.getUserInfo(this)._id, tags);

        videoCacheFile.delete();
        hideProgress();

        ShareActivity_.intent(this).shareInfo(shareInfo).shareType(ShareActivity.TYPE_SHARE_MOMENT).start();
    }

    @Override public void setPageInfo() {
        mIsPage = true;
        mPageName = "ShareExportActivity";
    }
}
