package co.yishun.onemoment.app.ui;

import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.VideoView;

import com.j256.ormlite.dao.Dao;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.OrmLiteDao;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import java.sql.SQLException;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.data.compat.MomentDatabaseHelper;
import co.yishun.onemoment.app.data.model.Moment;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.view.MomentCountDateView;
import co.yishun.onemoment.app.ui.view.PermissionSwitch;

/**
 * Created by Carlos on 2015/10/29.
 */
@EActivity(R.layout.activity_moment_create)
public class MomentCreateActivity extends BaseActivity {
    private static final String TAG = "MomentCreateActivity";
    @Extra String videoPath;
    @ViewById VideoView videoView;
    @ViewById Toolbar toolbar;
    @Extra boolean forWorld = false;
    @ViewById FrameLayout containerFrameLayout;
    @Extra WorldTag worldTag;
    @OrmLiteDao(helper = MomentDatabaseHelper.class) Dao<Moment, Integer> momentDao;
    private boolean isPrivate;//TODO use it

    @Override
    public void setPageInfo() {
        mPageName = "MomentCreateActivity";
    }

    @AfterViews void addView() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        View child;
        if (forWorld) {
            child = new PermissionSwitch(this);
            ((PermissionSwitch) child).setOnCheckedChangeListener((buttonView, isChecked) -> {
                isPrivate = isChecked;
                ((PermissionSwitch) child).setChecked(isChecked);
            });
        } else {
            child = new MomentCountDateView(this);
            try {
                long count = momentDao.countOf();
                ((MomentCountDateView) child).setMomentCount((int) count + 1);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        containerFrameLayout.addView(child, params);
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

    @Click void nextBtnClicked(View view) {
        TagCreateActivity_.intent(this).worldTag(worldTag).forWorld(forWorld).isPrivate(isPrivate).videoPath(videoPath).start();
        this.finish();
    }

}
