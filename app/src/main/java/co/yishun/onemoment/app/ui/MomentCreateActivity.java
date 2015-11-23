package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.VideoView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;
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
    private boolean isPrivate;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @Override
    public void setPageInfo() {
        
    }

    @AfterViews
    void addView() {
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        View child;
        if (forWorld) {
            child = new PermissionSwitch(this);
            ((PermissionSwitch)child).setOnCheckedChangeListener((buttonView, isChecked) -> {
                isPrivate = isChecked;
                ((PermissionSwitch)child).setChecked(isChecked);
            });
        } else {
            child = new MomentCountDateView(this);
            setCountTextView();
        }
        containerFrameLayout.addView(child, params);
    }

    private void setCountTextView() {
        int count = 110;//TODO get it from database and set it
    }

    @AfterViews
    void setupToolbar() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.activity_moment_create_title_text);
        Log.i("setupToolbar", "set home as up true");
    }

    @AfterViews
    void setVideo() {
        if (videoPath == null) return;
        videoView.setVideoPath(videoPath);
        videoView.start();
    }

    @Click
    void nextBtnClicked(View view) {
        TagCreateActivity_.intent(this).worldTag(worldTag).forWorld(forWorld).videoPath(videoPath).start();
    }

}
