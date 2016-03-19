package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.qiniu.android.storage.UploadManager;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterTextChange;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.EditorAction;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;
import org.solovyev.android.views.llm.LinearLayoutManager;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;

import co.yishun.library.OMVideoPlayer;
import co.yishun.library.OMVideoView;
import co.yishun.library.TagContainer;
import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.api.APIV4;
import co.yishun.onemoment.app.api.authentication.OneMomentV4;
import co.yishun.onemoment.app.api.model.VideoTag;
import co.yishun.onemoment.app.api.modelv4.UploadToken;
import co.yishun.onemoment.app.api.modelv4.World;
import co.yishun.onemoment.app.api.modelv4.WorldProvider;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.data.realm.RealmHelper;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagSearchAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.TagSearchController_;
import co.yishun.onemoment.app.ui.hybrd.BaseWebFragment;
import co.yishun.onemoment.app.ui.view.VideoTypeView;
import co.yishun.onemoment.app.util.GsonFactory;

import static co.yishun.onemoment.app.LogUtil.d;
import static co.yishun.onemoment.app.LogUtil.e;
import static co.yishun.onemoment.app.LogUtil.i;
import static co.yishun.onemoment.app.ui.MainActivity.Navigation.Diary;
import static co.yishun.onemoment.app.ui.MainActivity.Navigation.Discovery;
import static co.yishun.onemoment.app.ui.MainActivity.Navigation.World;

/**
 * Created by Carlos on 2015/11/2.
 */
@EActivity(R.layout.activity_tag_create)
public class TagCreateActivity extends BaseActivity
        implements AbstractRecyclerViewAdapter.OnItemClickListener<String> {
    private static final String TAG = "TagCreateActivity";

    private static final int REQUEST_SELECT_WORLD = 1;
    //    @ViewById
//    OMVideoView videoView;
    @ViewById
    FrameLayout videoViewContainer;
    @ViewById
    VideoTypeView videoTypeView;
    @ViewById
    Toolbar toolbar;
    @ViewById
    EditText queryText;
    @ViewById
    ImageView addView;
    @Extra
    boolean forDiary;
    @Extra
    boolean forToday = false;
    @Extra
    boolean forWorld = false;
    @Extra
    WorldProvider world;
    /**
     * Just for read extra. if need read to do something, be careful that {@link
     * #nextBtnClicked(View)} will move file to new place.
     */
    @Extra
    String videoPath;
    @Extra
    boolean isPrivate;
    @ViewById
    TagContainer tagContainer;
    //    @ViewById
//    ImageView momentPreviewImageView;
    @ViewById
    FrameLayout searchFrame;
    @ViewById
    RecyclerView recyclerView;
    @ViewById
    Button nextBtn;
    TagSearchAdapter adapter;
    @OrmLiteDao(helper = MomentDatabaseHelper.class)
    Dao<Moment, Integer> momentDao;
    private boolean searching = false;
    private LocationClient locationClient;
    private Moment momentToSave;
    /**
     * flag that whether video path has been set.
     */
    private boolean played = false;
    private OMVideoView mVideoView;

    @AfterInject
    void checkExtra() {
        if (world == null) {
            world = new World();
        }
    }

    @AfterViews
    void setupViews() {
        setupToolbar();
        setPreviewImage();

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new TagSearchAdapter(this, this);
        recyclerView.setAdapter(adapter);

        videoTypeView.setWorldCheck(forWorld, world.getName());
        videoTypeView.setTodayCheck(forToday);
        videoTypeView.setDiaryCheck(forDiary);

        nextBtn.setEnabled(forDiary || forWorld || forToday);

        mVideoView = new OMVideoView(this);
        videoViewContainer.addView(mVideoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
    }

    private void setPreviewImage() {
        // alert! fromFile will move video file
        momentToSave = new Moment.MomentBuilder(this).fromFile(new File(videoPath)).build();
        videoPath = momentToSave.getPath();
        try {
            File largeThumb =
                    FileUtil.getThumbnailStoreFile(this, momentToSave, FileUtil.Type.LARGE_THUMB);
            File smallThumb =
                    FileUtil.getThumbnailStoreFile(this, momentToSave, FileUtil.Type.MICRO_THUMB);
            VideoUtil.createThumbs(videoPath, largeThumb, smallThumb);
            momentToSave.setLargeThumbPath(largeThumb.getPath());
            momentToSave.setThumbPath(smallThumb.getPath());
//            Picasso.with(this).load(largeThumb).into(momentPreviewImageView);
        } catch (IOException e) {
            e(TAG, "create thumb failed");
            e.printStackTrace();
        }
    }

    void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_moment_create_title_text);
        i("setupToolbar", "set home as up true");
    }

    @Override
    protected void onResume() {
        super.onResume();

        // we create video view every time onResume, because on some devices video view is unable to be reused.
        if (mVideoView == null) {
            mVideoView = new OMVideoView(this);
            videoViewContainer.addView(mVideoView, new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        }
        setVideo();
    }

    void setVideo() {
        LogUtil.i(TAG, "set video: " + videoPath);
        if (videoPath == null) return;
        mVideoView.setPlayListener(new OMVideoPlayer.PlayListener() {
            @Override
            public void onOneCompletion() {
                mVideoView.reset();
                mVideoView.setVideoRes(Uri.fromFile(new File(videoPath)));
            }

            @Override
            public Uri onMoreAsked() {
                return null;
            }
        });
        mVideoView.setVideoRes(Uri.fromFile(new File(videoPath)));
        mVideoView.start();
    }

    @Click(R.id.tagContainer)
    void replay() {
        if (!mVideoView.isPlaying()) {
            mVideoView.start();
        } else {
            mVideoView.pause();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.reset();
            mVideoView.release();
            mVideoView = null;
        }
    }

    @Click(R.id.worldTextView)
    void selectWorld() {
        PersonalWorldActivity_.intent(this).startForResult(REQUEST_SELECT_WORLD);
    }

    @Click(R.id.todayTextView)
    void todayTextViewClick() {
        forToday = !forToday;
        videoTypeView.setTodayCheck(forToday);
        nextBtn.setEnabled(forDiary || forWorld || forToday);
    }

    @Click(R.id.diaryTextView)
    void diaryTextViewClick() {
        forDiary = !forDiary;
        videoTypeView.setDiaryCheck(forDiary);
        nextBtn.setEnabled(forDiary || forWorld || forToday);
    }

    @Click(R.id.worldClearView)
    void clearWorld() {
        if (forWorld) {
            forWorld = false;
            videoTypeView.setWorldCheck(false, null);
        }
        nextBtn.setEnabled(forDiary || forWorld || forToday);
    }

    void setupSearch() {
        searching = true;
        queryText.setVisibility(View.VISIBLE);
        queryText.requestFocus();
        queryText.setText("");
        addView.setImageResource(R.drawable.ic_action_add);
        recyclerView.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.GONE);
        searchFrame.setVisibility(View.VISIBLE);
        Animation queryTextAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_query_in);
        queryText.startAnimation(queryTextAnim);
        Animation recyclerAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_content_in);
        recyclerView.startAnimation(recyclerAnim);
        showKeyboard();

        List<String> defaultTag = new ArrayList<>();
        if (locationClient.getLastKnownLocation() == null ||
                TextUtils.isEmpty(formatLocation(locationClient.getLastKnownLocation()))) {
            defaultTag.add(AccountManager.getUserInfo(this).location);
        } else {
            defaultTag.add(formatLocation(locationClient.getLastKnownLocation()));
        }
        defaultTag.add(new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date()));
        if (forWorld) {
            defaultTag.add(world.getName());
        }
        adapter.addFixedItems(defaultTag);
        locationClient.start();
    }

    void recoverSearch() {
        searching = false;
        Animation queryTextAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_query_out);
        queryText.startAnimation(queryTextAnim);
        Animation recyclerAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_content_out);
        recyclerView.startAnimation(recyclerAnim);
        Animation frameAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_frame_out);
        searchFrame.startAnimation(frameAnim);
        Animation nextAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_next_in);
        nextBtn.startAnimation(nextAnim);
        viewChange();
        hideKeyboard();
        locationClient.stop();
    }

    @UiThread(delay = 200)
    void viewChange() {
        searchFrame.setVisibility(View.GONE);
        queryText.setVisibility(View.GONE);
        addView.setImageResource(R.drawable.ic_action_add_tag);
        recyclerView.setVisibility(View.GONE);
        nextBtn.setVisibility(View.VISIBLE);
    }

    @OnActivityResult(REQUEST_SELECT_WORLD)
    void onSelectWorld(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            forWorld = true;
            world.setId(data.getStringExtra(PersonalWorldActivity.KEY_ID));
            world.setName(data.getStringExtra(PersonalWorldActivity.KEY_NAME));
            videoTypeView.setWorldCheck(true, world.getName());
        }
        nextBtn.setEnabled(forDiary || forWorld || forToday);
    }

    boolean addTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            showSnackMsg(R.string.activity_tag_create_tag_empty_error);
            return true;
        } else if (tagContainer.getVideoTags().size() == 3) {
            showSnackMsg(R.string.activity_tag_create_tag_number_error);
            return false;
        }
        VideoTag videoTag = new VideoTag();
        videoTag.name = tag;
        videoTag.setX(50);
        videoTag.setY(50);
        videoTag.type = "words";
        tagContainer.addTag(videoTag);
        return true;
    }

    @Click
    void nextBtnClicked(View view) {
        BaseWebFragment.invalidateWeb();
        if (forDiary) {
            saveToMoment();
        }
        if (forWorld || forToday) {
            upload();
        }

        // world > diary > today
        if (forWorld) {
            MainActivity.setNextNavigationTo(World);
        } else if (forDiary) {
            MainActivity.setNextNavigationTo(Diary);
        } else if (forToday) {
            MainActivity.setNextNavigationTo(Discovery);
        }

    }

    void saveToMoment() {
        final Moment moment = momentToSave;

        String time =
                new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(Calendar.getInstance().getTime());
        List<Moment> result;
        Where<Moment, Integer> w = momentDao.queryBuilder().where();
        try {
            result = w.and(w.eq("time", time), w.eq("owner", AccountManager.getUserInfo(this)._id)).query();

            i(TAG, "delete old today moment: " + Arrays.toString(result.toArray()));

            if (1 == momentDao.create(moment)) {
                i(TAG, "new moment: " + moment);

                RealmHelper.removeTags(moment.getTime());
                for (co.yishun.library.tag.VideoTag tag : tagContainer.getVideoTags()) {
                    RealmHelper.addTodayTag(tag.getText(), tag.getX() / 100f, tag.getY() / 100f);
                }

                momentDao.delete(result);
                SyncManager.syncNow(this);

                for (Moment mToDe : result) {
                    FileUtil.getThumbnailStoreFile(this, mToDe, FileUtil.Type.LARGE_THUMB).delete();
                    FileUtil.getThumbnailStoreFile(this, mToDe, FileUtil.Type.MICRO_THUMB).delete();
                    mToDe.getFile().delete();
                }

                //TODO send broadcast whenever local moment changed, those lines code copy from MomentSyncImpl
                String timestamp = moment.getUnixTimeStamp();
                Intent intent = new Intent(SyncManager.SYNC_BROADCAST_ACTION_LOCAL_UPDATE);
                intent.putExtra(SyncManager.SYNC_BROADCAST_EXTRA_LOCAL_UPDATE_TIMESTAMP, timestamp);
                LogUtil.i(TAG, "create new moment, send a broadcast. timestamp: " + timestamp);
                this.sendBroadcast(intent);

                showSnackMsg(R.string.activity_tag_create_moment_success);
                delayFinish();
                return;
            }
        } catch (SQLException e) {
            LogUtil.e(TAG, "failed to save moment", e);
            e.printStackTrace();
        }
        showSnackMsg(R.string.activity_tag_create_moment_fail);
        //TODO need any longer? Moment.unlock();
    }

    /**
     * Upload the video file to qiniu, if this video is for a world
     */
    @Background
    void upload() {
        showProgress();
        WorldVideo video = new WorldVideo();
        video.filename = Constants.WORLD_VIDEO_PREFIX + AccountManager.getUserInfo(this)._id +
                Constants.URL_HYPHEN + Util.unixTimeStamp() + Constants.VIDEO_FILE_SUFFIX;
        File tmp = new File(videoPath);
        File videoFile = FileUtil.getWorldVideoStoreFile(this, video);
        FileUtil.copyFile(tmp, videoFile);//TODO this video may be used by diary, but also a cache, need to delete when it is cache.
        videoPath = videoFile.getPath();

        UploadManager uploadManager = new UploadManager();
        d(TAG, "upload " + videoFile.getName());
        UploadToken token =
                OneMomentV4.createAdapter().create(APIV4.class).getUploadToken(videoFile.getName());
        if (!token.isSuccess()) {
            e(TAG, "get upload token error: " + token.msg);
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        uploadManager.put(videoFile, videoFile.getName(), token.token,
                (s, responseInfo, jsonObject) -> {
                    i(TAG, responseInfo.toString());
                    if (responseInfo.isOK()) {
                        d(TAG, "loaded " + responseInfo.path);
                        i(TAG, "profile upload ok");
                    } else {
                        e(TAG, "profile upload error: " + responseInfo.error);
                    }
                    latch.countDown();
                }, null
        );
        try {
            latch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        Gson gson = GsonFactory.newNormalGson();
        JsonArray tagArray = gson.toJsonTree(tagContainer.getVideoTags()).getAsJsonArray();
        for (JsonElement element : tagArray) {
            element.getAsJsonObject().remove("code");
            element.getAsJsonObject().remove("errorCode");
        }
        String tags = gson.toJson(tagArray);
        d(TAG, tags);

        APIV4 apiv4 = OneMomentV4.createAdapter().create(APIV4.class);
        if (forWorld) {
            WorldVideo worldVideo =
                    apiv4.createWorldVideo(world.getId(), videoFile.getName(), AccountManager.getUserInfo(this)._id, tags);
        }
        if (forToday) {
            WorldVideo todayVideo =
                    apiv4.createTodayVideo(videoFile.getName(), AccountManager.getUserInfo(this)._id, tags);
        }

        hideProgress();
        this.finish();
    }

    /**
     * RecyclerView onClick item event for tag expandable edit text
     */
    @Override
    public void onClick(View view, String item) {
        if (addTag(item)) {
            recoverSearch();
        }
    }

    @Click
    void searchFrameClicked(View view) {
        recoverSearch();
    }

    @Click(R.id.addView)
    void addViewClicked(View view) {
        if (searching) {
            addTag(queryText.getText().toString());
            recoverSearch();
        } else
            setupSearch();
    }

    @Override
    public void onBackPressed() {
        if (searching)
            recoverSearch();
        else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @AfterTextChange(R.id.queryText)
    @EditorAction(R.id.queryText)
    void search() {
        if ("".equals(queryText.getText().toString())) {
            return;
        }
        TagSearchController_.getInstance_(this).setUp(adapter, recyclerView, queryText.getText().toString());
    }

    @AfterInject
    void setupLocation() {
        locationClient = new LocationClient(getApplicationContext());
        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
        option.setCoorType("bd09ll");
        option.setIsNeedAddress(true);//need address info
        option.setOpenGps(true);
        option.setIsNeedLocationDescribe(false);
        option.SetIgnoreCacheException(false);
        option.setEnableSimulateGps(false);
        locationClient.setLocOption(option);
        locationClient.registerLocationListener(bdLocation -> {
            if (TextUtils.isEmpty(formatLocation(bdLocation))) {
                return;
            }
            addItem(0, formatLocation(bdLocation));
        });
    }

    String formatLocation(BDLocation location) {
        if (location == null) {
            return null;
        }
        String province = location.getProvince();
        String city = location.getCity();
        if (province == null || city == null || province.equals("") || city.equals("")) {
            return null;
        }
        if (province.endsWith("省")) {
            province = province.substring(0, province.lastIndexOf("省"));
        }
        if (city.endsWith("市")) {
            city = city.substring(0, city.lastIndexOf("市"));
        }
        return province + " " + city;
    }

    @UiThread
    void addItem(int position, String item) {
        if (position < 0) {
            adapter.add(item);
        } else {
            adapter.replaceItem(position, item);
        }
    }

    @UiThread(delay = 500)
    void delayFinish() {
        this.finish();
    }

    @NonNull
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return super.getSnackbarAnchorWithView(tagContainer);
    }

    @Override
    public void setPageInfo() {
        mPageName = "TagCreateActivity";
    }

    void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(queryText, 0);
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(queryText.getWindowToken(), 0);
    }

}
