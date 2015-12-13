package co.yishun.onemoment.app.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.Where;
import com.qiniu.android.storage.UploadManager;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
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

import co.yishun.library.EditTagContainer;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.account.SyncManager;
import co.yishun.onemoment.app.api.Misc;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.VideoTag;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.data.VideoUtil;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagSearchAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.TagSearchController_;

/**
 * Created by Carlos on 2015/11/2.
 */
@EActivity(R.layout.activity_tag_create)
public class TagCreateActivity extends BaseActivity
        implements AbstractRecyclerViewAdapter.OnItemClickListener<String>,
                   TextView.OnEditorActionListener, TextWatcher {
    public static final int REQUEST_CODE_SEARCH = 1;
    private static final String TAG = "TagCreateActivity";
    @ViewById Toolbar toolbar;
    @ViewById EditText queryText;
    @ViewById ImageView addView;
    @Extra WorldTag worldTag;
    @Extra boolean forWorld = false;
    /**
     * Just for read extra. if need read to do something, be careful that {@link #nextBtnClicked(View)} will move file to new place.
     */
    @Extra String videoPath;
    @Extra boolean isPrivate;
    @ViewById EditTagContainer editTagContainer;
    @ViewById ImageView momentPreviewImageView;
    @ViewById FrameLayout searchFrame;
    @ViewById RecyclerView recyclerView;
    @ViewById Button nextBtn;

    TagSearchAdapter adapter;
    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;
    private boolean searching = false;
    private LocationClient locationClient;
    private float tagX;
    private float tagY;
    private Moment momentToSave;

    @NonNull @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return super.getSnackbarAnchorWithView(editTagContainer);
    }

    @Override
    public void setPageInfo() {
        mPageName = "TagCreateActivity";
    }

    @AfterViews void setupViews() {
        queryText.setVisibility(View.GONE);
        queryText.setOnEditorActionListener(this);
        queryText.addTextChangedListener(this);
        addView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        searchFrame.setVisibility(View.GONE);

        LinearLayoutManager manager = new LinearLayoutManager(this);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        adapter = new TagSearchAdapter(this, this);
        recyclerView.setAdapter(adapter);
        setPreviewImage();
    }

    private void setPreviewImage() {
        // alert! fromFile will move video file
        momentToSave = new Moment.MomentBuilder(this).fromFile(new File(videoPath)).build();
        videoPath = momentToSave.getPath();
        try {
            String largeThumbImage = VideoUtil.createLargeThumbImage(this, momentToSave, videoPath);
            momentToSave.setLargeThumbPath(largeThumbImage);
            Picasso.with(this).load(new File(largeThumbImage)).into(momentPreviewImageView);
        } catch (IOException e) {
            Log.e(TAG, "create thumb failed");
            e.printStackTrace();
        }
    }

    @AfterViews void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_moment_create_title_text);
        Log.i("setupToolbar", "set home as up true");
    }

    void setupSearch() {
        searching = true;
        queryText.setVisibility(View.VISIBLE);
        queryText.requestFocus();
        queryText.setText("");
        addView.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.VISIBLE);
        nextBtn.setVisibility(View.GONE);
        searchFrame.setVisibility(View.VISIBLE);
        Animation queryTextAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_query_in);
        queryText.startAnimation(queryTextAnim);
        Animation recyclerAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_content_in);
        recyclerView.startAnimation(recyclerAnim);
        Animation addAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_add_in);
        addView.startAnimation(addAnim);
        showKeyboard();

        List<String> defaultTag = new ArrayList<>();
        if (locationClient.getLastKnownLocation() == null) {
            defaultTag.add(AccountManager.getUserInfo(this).location);
        } else {
            defaultTag.add(formatLocation(locationClient.getLastKnownLocation()));
        }
        defaultTag.add(new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault()).format(new Date()));
        if (forWorld && worldTag != null && !"".equals(worldTag.name)) {
            defaultTag.add(worldTag.name);
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
        Animation addAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_add_out);
        addView.startAnimation(addAnim);
        Animation nextAnim = AnimationUtils.loadAnimation(this, R.anim.tag_create_next_in);
        nextBtn.startAnimation(nextAnim);
        viewChange();
        hideKeyboard();
        locationClient.stop();
    }

    @UiThread(delay = 200) void viewChange() {
        searchFrame.setVisibility(View.GONE);
        queryText.setVisibility(View.GONE);
        addView.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        nextBtn.setVisibility(View.VISIBLE);
    }

    @AfterViews void setEditTagContainer() {
        editTagContainer.setOnAddTagListener((x, y) -> {
            tagX = x;
            tagY = y;
            setupSearch();
        });
    }

    boolean addTag(String tag) {
        if (TextUtils.isEmpty(tag)) {
            showSnackMsg(R.string.activity_tag_create_tag_empty_error);
            return true;
        } else if (editTagContainer.getVideoTags().size() == 3) {
            showSnackMsg(R.string.activity_tag_create_tag_number_error);
            return false;
        }
        VideoTag videoTag = new VideoTag();
        videoTag.name = tag;
        videoTag.setX(tagX);
        videoTag.setY(tagY);
        videoTag.type = "words";
        editTagContainer.addTag(videoTag);
        return true;
    }

    @Click void nextBtnClicked(View view) {
        if (forWorld) {
            if (editTagContainer.getVideoTags().size() == 0) {
                showSnackMsg(R.string.activity_tag_create_no_tag_error);
            } else {
                upload();
            }
        } else {
            final Moment moment = momentToSave;
            try {
                String thumbImage = VideoUtil.createThumbImage(this, moment, videoPath);
                moment.setThumbPath(thumbImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

            String time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(Calendar.getInstance().getTime());
            List<Moment> result;
            Where<Moment, Integer> w = momentDao.queryBuilder().where();
            try {
                result = w.and(w.eq("time", time), w.eq("owner", AccountManager.getUserInfo(this)._id)).query();

                Log.i(TAG, "delete old today moment: " + Arrays.toString(result.toArray()));

                if (1 == momentDao.create(moment)) {
                    Log.i(TAG, "new moment: " + moment);
                    SyncManager.syncNow(this);

                    RealmHelper.removeTags(moment.getTime());
                    for (co.yishun.library.tag.VideoTag tag : editTagContainer.getVideoTags()) {
                        RealmHelper.addTodayTag(tag.getText(), tag.getX(), tag.getY());
                    }

                    momentDao.delete(result);
                    for (Moment mToDe : result) {
                        FileUtil.getThumbnailStoreFile(this, mToDe, FileUtil.Type.LARGE_THUMB).delete();
                        FileUtil.getThumbnailStoreFile(this, mToDe, FileUtil.Type.MICRO_THUMB).delete();
                        mToDe.getFile().delete();
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            this.finish();
            showSnackMsg(R.string.activity_tag_create_moment_success);
            //TODO need any longer? Moment.unlock();
        }
    }

    /**
     * Upload the video file to qiniu, if this video is for a world
     */
    @Background void upload() {
        showProgress();
        Video video = new Video();
        video.fileName = Constants.WORLD_VIDEO_PREFIX + AccountManager.getUserInfo(this)._id +
                Constants.URL_HYPHEN + Util.unixTimeStamp() + Constants.VIDEO_FILE_SUFFIX;
        File tmp = new File(videoPath);
        File videoFile = new File(FileUtil.getWorldVideoStoreFile(this, video).getPath() + Constants.VIDEO_FILE_SUFFIX);
        tmp.renameTo(videoFile);
        videoPath = videoFile.getPath();

        UploadManager uploadManager = new UploadManager();
        Log.d(TAG, "upload " + videoFile.getName());
        UploadToken token = OneMomentV3.createAdapter().create(Misc.class)
                .getUploadToken(videoFile.getName());
        if (token.code <= 0) {
            Log.e(TAG, "get upload token error: " + token.msg);
            return;
        }
        CountDownLatch latch = new CountDownLatch(1);
        uploadManager.put(videoFile, videoFile.getName(), token.token,
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
        JsonArray tagArray = gson.toJsonTree(editTagContainer.getVideoTags()).getAsJsonArray();
        for (JsonElement element : tagArray) {
            element.getAsJsonObject().remove("code");
            element.getAsJsonObject().remove("errorCode");
        }
        String tags = gson.toJson(tagArray);
        Log.d(TAG, tags);

        World world = OneMomentV3.createAdapter().create(World.class);
        Video uploadVideo = world.addVideoToWorld(AccountManager.getUserInfo(this)._id,
                isPrivate ? "private" : "public", videoFile.getName(), tags);
        if (uploadVideo.code == Constants.CODE_SUCCESS) {
            hideProgress();
            this.finish();
        }
    }

    @Override
    public void onClick(View view, String item) {
        if (addTag(item)) {
            recoverSearch();
        }
    }

    @Click void searchFrameClicked(View view) {
        recoverSearch();
    }

    @Click void addViewClicked(View view) {
        if (addTag(queryText.getText().toString())) {
            recoverSearch();
        }
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

    void search() {
        if ("".equals(queryText.getText().toString())) {
            return;
        }
        TagSearchController_.getInstance_(this).setUp(adapter, recyclerView, queryText.getText().toString());
    }

    @AfterInject void setupLocation() {
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
            if ("".equals(formatLocation(bdLocation))) {
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

    @UiThread void addItem(int position, String item) {
        if (position < 0) {
            adapter.add(item);
        } else {
            adapter.replaceItem(position, item);
        }
    }

    void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(queryText, 0);
    }

    void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(queryText.getWindowToken(), 0);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        search();
        return true;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        search();
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

}
