package co.yishun.onemoment.app.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.afollestad.materialdialogs.Theme;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.util.Matrix;
import com.j256.ormlite.dao.Dao;
import com.qiniu.android.storage.UploadManager;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.SupposeBackground;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.BackgroundExecutor;

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

import co.yishun.library.momentcalendar.DayView;
import co.yishun.library.momentcalendar.MomentCalendar;
import co.yishun.library.momentcalendar.MomentMonthView;
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
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.model.OMLocalVideoTag;
import co.yishun.onemoment.app.data.realm.RealmHelper;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.util.GsonFactory;
import co.yishun.onemoment.app.video.VideoCommand;
import co.yishun.onemoment.app.video.VideoConcat;

@EActivity(R.layout.activity_share_export)
public class ShareExportActivity extends BaseActivity implements MomentMonthView.MonthAdapter, DayView.OnMomentSelectedListener {

    public static final String ID_CANCEL = "wocao233";
    private static final String TAG = "ShareExportActivity";
    @ViewById
    Toolbar toolbar;
    @ViewById
    MomentCalendar momentCalendar;
    @ViewById
    TextView shareText;
    @ViewById
    TextView exportText;
    @ViewById
    TextView selectAllText;
    @ViewById
    TextView clearText;
    @ViewById
    TextView selectedText;
    @OrmLiteDao(helper = MomentDatabaseHelper.class)
    Dao<Moment, Integer> momentDao;
    private List<Moment> allMoments;
    private List<Moment> selectedMoments;
    private File videoCacheFile;
    private volatile boolean concatExport = true;
    private MaterialDialog concatProgress;
    private int totalTask = 1;
    private int completeTask = 0;
    /**
     * flag to show whether the export or share is in progress.
     */
    private boolean isWorking = false;
    /**
     * flag to show whether the task should be canceled.
     */
    private boolean canceled = false;
    private MaterialDialog mUploadProgressDialog;
    private MaterialDialog mCancelDialog;
    private volatile CountDownLatch mCancelDialogLatch;
    private volatile boolean uploading = false;

    @AfterViews
    void setupViews() {
        momentCalendar.setAdapter(this);
        DayView.setOnMomentSelectedListener(this);
        DayView.setMultiSelection(true);
        try {
            List<Moment> momentInDatabase = momentDao.queryBuilder().where().eq("owner", AccountManager.getAccountId(this)).query();
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

    @AfterViews
    void setAppbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_share_export_title);
        ab.setHomeAsUpIndicator(R.drawable.ic_action_back_close);
    }

    @UiThread
    void showConcatProgress() {
        hideProgress();
        concatProgress = new MaterialDialog.Builder(this).progress(false, 100, false).theme(Theme.LIGHT)
                .cancelListener(dialog -> {
                    showCancelDialog();
                }).content(getString(R.string.activity_share_export_progress_concatenating)).build();
        concatProgress.show();
    }

    @UiThread
    void updateConcatProgress() {
        concatProgress.setProgress((int) (completeTask * 100.0f / totalTask));
    }

    @UiThread
    void hideConcatProgress() {
        if (concatProgress != null) {
            concatProgress.hide();
        }
    }

    @Click(R.id.shareText)
    @Background(id = ID_CANCEL)
    void shareTextClicked() {
        canceled = false;
        if (selectedMoments.size() == 0) {
            showSnackMsg(R.string.activity_share_export_no_moment_select);
            return;
        }
        showProgress();
        concatExport = false;
        concatSelectedVideos();
    }

    @UiThread
    void showCancelDialog() {
        if (mCancelDialog == null) {
            mCancelDialog = new MaterialDialog.Builder(this).theme(Theme.LIGHT).content(R.string.activity_share_export_abort)
                    .positiveText(R.string.activity_share_export_positive).negativeText(R.string.activity_share_export_negative)
                    .cancelable(true).onPositive((dialog, which) ->
                    {
                        BackgroundExecutor.cancelAll(ID_CANCEL, true);
                        canceled = true;
                        mCancelDialogLatch.countDown();
                    }).onNegative((dialog1, which1) -> {
                        if (concatProgress != null && concatProgress.getCurrentProgress() < 100) {
                            concatProgress.show();
                        } else if (uploading) {
                            showUploadProgress(R.string.activity_share_export_progress_uploading);
                        }
                        mCancelDialogLatch.countDown();
                    }).build();
        }
        mCancelDialog.show();
        mCancelDialogLatch = new CountDownLatch(1);
    }

    @UiThread
    void hideCancelDialog() {
        if (mCancelDialog != null) {
            mCancelDialog.hide();
            if (mCancelDialogLatch != null) {
                mCancelDialogLatch.countDown();
                mCancelDialogLatch = null;
            }
        }
    }

    @Click(R.id.exportText)
    @Background(id = ID_CANCEL)
    void exportTextClicked() {
        canceled = false;
        if (selectedMoments.size() == 0) {
            showSnackMsg(R.string.activity_share_export_no_moment_select);
            return;
        }
        showProgress();
        concatExport = true;
        concatSelectedVideos();
    }

    @Click(R.id.selectAllText)
    void selectAllTextClicked() {
        selectedMoments.clear();
        selectedMoments.addAll(allMoments);
        setAllSelect(true);
        updateSelectedText();
    }

    @UiThread
    void showUploadProgress(@StringRes int msgRes) {
        uploading = true;
        if (mUploadProgressDialog == null)
            mUploadProgressDialog = new MaterialDialog.Builder(this).theme(Theme.LIGHT).
                    content(getString(msgRes))
                    .cancelListener(dialog -> {
                        showCancelDialog();
                    })
                    .progress(true, 0).build();
        mUploadProgressDialog.setContent(getString(msgRes));
        mUploadProgressDialog.show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUploadProgressDialog != null) {
            mUploadProgressDialog.dismiss();
        }

        if (concatProgress != null) {
            concatProgress.dismiss();
        }

    }

    @UiThread
    void hideUploadProgress() {
        uploading = false;
        if (mUploadProgressDialog != null) {
            mUploadProgressDialog.hide();
        }
    }

    @Click(R.id.clearText)
    void clearTextClicked() {
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
                        if (dayView.isEnabled())
                            dayView.setSelected(select);
                    }
                }
            }
        }
    }


    /**
     *
     * @param select the dayview state that will be changed
     * @param begin  the begin dayview number
     * @param end    the end dayview number
     */
    void setSomeSelectClicked(boolean select,int begin,int end){
        if (begin>end){
            int tem = end;
            end = begin;
            begin = tem;
        }
        MomentMonthView monthView = momentCalendar.getCurrentMonthView();

        if(select == true){
            for (int dayIndex = begin+1; dayIndex < end; dayIndex++) {
                if (monthView.getChildAt(dayIndex) instanceof DayView) {
                    DayView dayView = (DayView) monthView.getChildAt(dayIndex);
                    selectedMoments.add((Moment) dayView.getTag());
                    LogUtil.d("ShareExportActivity", "isEnabled=" + "monthIndex 0" + dayIndex + dayView.isEnabled());
                    if (dayView.isEnabled())
                        dayView.setSelected(true);
                }
            }
        }
    }



    void updateSelectedText() {
        String content = String.format(getResources().getString(R.string.activity_share_export_selected), selectedMoments.size(), allMoments.size());
        SpannableString ss = new SpannableString(content);
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorSecondary)), content.indexOf(" "), content.indexOf("/"), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        selectedText.setText(ss);
    }

    @Override
    public void onBindView(Calendar calendar, DayView dayView) {
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
            Picasso.with(this).load(new File(moment.getThumbPath())).into(dayView, new Callback() {
                @Override
                public void onSuccess() {
                    dayView.overrideTextColorResource(R.color.colorPrimary);
                }

                @Override
                public void onError() {
                    dayView.removeOverrideTextColor();
                }
            });
            if (selectedMoments.contains(moment))
                dayView.setSelected(true);
            else
                dayView.setSelected(false);
            LogUtil.i(TAG, "moment found: " + moment.getTime());
        } else {
            dayView.setEnabled(false);
        }
    }

    @Override
    public void onSelected(DayView dayView) {
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
        if (canceled) return;
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
        videoCacheFile = FileUtil.getCacheFile(this, Constants.LONG_VIDEO_PREFIX + AccountManager.getUserInfo(this)._id + Constants.URL_HYPHEN + selectedMoments.size() + Constants.URL_HYPHEN + Util.unixTimeStamp() + Constants.VIDEO_FILE_SUFFIX);

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

        totalTask = (int) ((filesNeedTrans.size() + files.size()) / 0.9f);
        completeTask = 0;
        showConcatProgress();

        new VideoConcat(this).setTransFile(filesNeedTrans).setConcatFile(files, videoCacheFile).setListener(new VideoCommand.VideoCommandListener() {
            @Override
            public void onSuccess(VideoCommand.VideoCommandType type) {
                if (canceled) return;
                switch (type) {
                    case COMMAND_TRANSPOSE:
                        LogUtil.d(TAG, "onTransSuccess: ");
                        completeTask++;
                        break;
                    case COMMAND_FORMAT:
                        LogUtil.d(TAG, "onFormatSuccess: ");
                        completeTask++;
                        break;
                    case COMMAND_CONCAT:
                        LogUtil.d(TAG, "onConcatSuccess: ");
                        completeTask = totalTask;
                        afterConcat();
                        break;
                }
                updateConcatProgress();
            }

            @Override
            public void onFail(VideoCommand.VideoCommandType type) {
                LogUtil.d(TAG, "onFail: ");
            }
        }).start();
    }

    @Background(id = ID_CANCEL)
    void afterConcat() {
        hideConcatProgress();
        if (canceled) return;
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
            hideCancelDialog();

            Uri uri = addImageGallery(outFile);
            exportOKMsg(uri);
        } else
            uploadAndShare();
    }

    @UiThread
    void exportOKMsg(Uri uri) {
        Snackbar.make(getSnackbarAnchorWithView(null),
                R.string.activity_share_export_export_success, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.activity_share_export_export_action_open, v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(uri);
                    try {
                        startActivity(intent);
                    } catch (Exception ignored) {
                        showSnackMsg(R.string.activity_share_export_export_action_open_error);
                    }
                }).show();
    }

    Uri addImageGallery(File file) {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DATA, file.getAbsolutePath());
        values.put(MediaStore.Images.Media.MIME_TYPE, "video/mp4");
        return getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    @SupposeBackground
    void uploadAndShare() {
        if (canceled) return;
        if (mCancelDialogLatch != null)
            try {
                mCancelDialogLatch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                return;
            }
        showUploadProgress(R.string.activity_share_export_progress_uploading);
        UploadManager uploadManager = new UploadManager();
        LogUtil.d(TAG, "upload " + videoCacheFile.getName());
        UploadToken token = OneMomentV3.createAdapter().create(Misc.class).getUploadToken(videoCacheFile.getName());
        if (token.code <= 0) {
            LogUtil.e(TAG, "get upload token error: " + token.msg);
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        uploadManager.put(videoCacheFile, videoCacheFile.getName(), token.token, (s, responseInfo, jsonObject) -> {
            LogUtil.i(TAG, responseInfo.toString());
            if (responseInfo.isOK()) {
                LogUtil.d(TAG, "loaded " + responseInfo.path);
                LogUtil.i(TAG, "profile upload ok");
            } else {
                LogUtil.e(TAG, "profile upload error: " + responseInfo.error);
            }
            latch.countDown();
        }, null);
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Gson gson = GsonFactory.newNormalGson();
        JsonArray allTagArray = new JsonArray();
        for (Moment m : selectedMoments) {
            JsonArray momentTags = new JsonArray();
            for (OMLocalVideoTag tag : RealmHelper.getTags(m.getTime())) {
                JsonObject element = new JsonObject();
                String[] position = tag.getTagPosition().split(" ");
                element.addProperty("name", tag.getTagText());
                element.addProperty("x", Float.valueOf(position[0]) * 100f);
                element.addProperty("y", Float.valueOf(position[1]) * 100f);
                momentTags.add(element);
            }
            allTagArray.add(momentTags);
        }
        String tags = gson.toJson(allTagArray);
        LogUtil.d(TAG, tags);

        Account account = OneMomentV3.createAdapter().create(Account.class);
        ShareInfo shareInfo = account.share(videoCacheFile.getName(), AccountManager.getUserInfo(this)._id, tags);

        videoCacheFile.delete();
        hideUploadProgress();

        share(shareInfo);
    }

    @UiThread
    void share(ShareInfo shareInfo) {
        if (canceled) return;
        ShareActivity.showShareChooseDialog(this, shareInfo, 0);
    }

    @Override
    public void setPageInfo() {
        mIsPage = true;
        mPageName = "ShareExportActivity";
    }
}
