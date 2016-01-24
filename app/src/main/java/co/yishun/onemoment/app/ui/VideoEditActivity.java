package co.yishun.onemoment.app.ui;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.VideoView;

import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OnActivityResult;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.Touch;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.view.VideoTypeView;
import retrofit.http.POST;

/**
 * Created by Carlos on 2015/10/29.
 */
@EActivity(R.layout.activity_moment_create)
public class VideoEditActivity extends BaseActivity {
    private static final String TAG = "VideoEditActivity";
    private static final int REQUEST_SELECT_WORLD = 1;
    @Extra String videoPath;
    @ViewById VideoView videoView;
    @ViewById Toolbar toolbar;
    @ViewById VideoTypeView videoTypeView;

    boolean forWorld = false;
    WorldTag worldTag;
    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;
    private boolean isPrivate;

    private boolean lifeCheck;
    private boolean diaryCheck;
    private boolean worldCheck;
    private String worldId;
    private String worldName;

    @Override
    public void setPageInfo() {
        mPageName = "VideoEditActivity";
    }

    @AfterViews void setupToolbar() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_moment_create_title_text);
        LogUtil.i("setupToolbar", "set home as up true");
    }

    @AfterViews void setVideo() {
        if (videoPath == null) return;
        videoView.setVideoPath(videoPath);
        videoView.seekTo(300);
        playVideo();
    }

    @UiThread(delay = 500) void playVideo() {
        videoView.seekTo(0);
        videoView.start();
    }

    @Touch(R.id.videoView) void videoClick(View view, MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
            videoView.seekTo(0);
            videoView.start();
        }
    }

    @Click(R.id.worldTextView) void selectWorld() {
        PersonalWorldActivity_.intent(this).startForResult(REQUEST_SELECT_WORLD);
    }

    @Click(R.id.lifeTextView) void lifeTextViewClick() {
        lifeCheck = !lifeCheck;
        videoTypeView.setLifeCheck(lifeCheck);
    }

    @Click(R.id.diaryTextView) void diaryTextViewClick() {
        diaryCheck = !diaryCheck;
        videoTypeView.setDiaryCheck(diaryCheck);
    }

    @Click(R.id.worldClearView) void clearWorld() {
        if (worldCheck) {
            worldCheck = false;
            worldName = null;
            videoTypeView.setWorldCheck(false, null);
        }
    }

    @Click void nextBtnClicked(View view) {
        TagCreateActivity_.intent(this).worldTag(worldTag).forWorld(forWorld).isPrivate(isPrivate).videoPath(videoPath).start();
        this.finish();
    }

    @OnActivityResult(REQUEST_SELECT_WORLD) void onSelectWorld(int resultCode, Intent data) {
        if (resultCode == RESULT_OK) {
            worldCheck = true;
            worldId = data.getStringExtra(PersonalWorldActivity.KEY_ID);
            worldName = data.getStringExtra(PersonalWorldActivity.KEY_NAME);
            videoTypeView.setWorldCheck(true, worldName);
        }
    }

    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.activity_video_edit_add_tag) {

        }
        return super.onOptionsItemSelected(item);
    }

    @Override public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_video_edit, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
