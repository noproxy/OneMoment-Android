package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.loader.VideoTaskManager;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.VotedUpController_;
import co.yishun.onemoment.app.ui.play.PlayTagVideoFragment;
import co.yishun.onemoment.app.ui.view.GridSpacingItemDecoration;

/**
 * Created by yyz on 7/20/15.
 */
@EActivity(R.layout.activity_video_voted_up)
public class VideoVotedUpActivity extends BaseActivity
        implements AbstractRecyclerViewAdapter.OnItemClickListener<TagVideo> {
    private static final String TAG = "VideoVotedUpActivity";
    @ViewById
    Toolbar toolbar;
    @ViewById
    SuperRecyclerView recyclerView;
    @ViewById
    FrameLayout containerFrameLayout;

    private PlayTagVideoFragment previewFragment;
    private boolean preview = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @AfterViews
    void setupToolbar() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.video_vote_up_title);
        LogUtil.i("setupToolbar", "set home as up true");
    }

    @Override
    public void setPageInfo() {
        mPageName = "VideoVotedUpActivity";
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoTaskManager.getInstance().quit();
    }

    @AfterViews
    void setView() {
        //TODO solve sliding conflict
        int spanCount = 3;
        int spacing = (int) getResources().getDimension(R.dimen.video_grid_divider);
        GridLayoutManager manager = new GridLayoutManager(this, spanCount);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));

        TagAdapter adapter = new TagAdapter(this, this);
        recyclerView.setAdapter(adapter);
        VotedUpController_.getInstance_(this).setUp(adapter, recyclerView);
    }
    //TODO not test


    @Override
    public void onBackPressed() {
        if (preview) {
            getSupportFragmentManager().beginTransaction().remove(previewFragment).commit();
            preview = false;
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            // android:launchMode="singleTop" solve navigationUp to a new Activity issue http://stackoverflow.com/questions/13293772/how-to-navigate-up-to-the-same-parent-state
            // but there is no dragger animation, so we should close manually
            // but draggerView.closeActivity(); has bug: https://github.com/ppamorim/Dragger/issues/27
            onBackPressed();//TODO handle back stack
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view, TagVideo item) {
        preview = true;
//        previewFragment = PlayTagVideoFragment_.builder().oneVideo(item).build();
//        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fragment_fade_in, R.anim.fragment_fade_out)
//                .replace(R.id.containerFrameLayout, previewFragment).commit();
    }


}

