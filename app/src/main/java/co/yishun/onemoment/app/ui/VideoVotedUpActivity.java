package co.yishun.onemoment.app.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.FrameLayout;

import com.malinskiy.superrecyclerview.SuperRecyclerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.loader.VideoTaskManager;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagAdapter;
import co.yishun.onemoment.app.ui.adapter.VideoLikeAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.VotedUpController_;
import co.yishun.onemoment.app.ui.play.PlayTagVideoFragment;
import co.yishun.onemoment.app.ui.play.PlayTagVideoFragment_;
import co.yishun.onemoment.app.ui.view.GridSpacingItemDecoration;

/**
 * Created by yyz on 7/20/15.
 */
//TODO handle not extend BaseActivity
@EActivity(R.layout.activity_video_voted_up)
public class VideoVotedUpActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<TagVideo> {
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
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        VideoTaskManager.getInstance().init(this);
    }

    @AfterViews
    void setupToolbar() {
        setSupportActionBar(toolbar);

        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        ab.setTitle(R.string.video_vote_up_title);
        Log.i("setupToolbar", "set home as up true");
    }

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
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

//        AbstractRecyclerViewAdapter<TagVideo, VideoLikeAdapter.SimpleViewHolder> adapter = new VideoLikeAdapter(this, this, recyclerView);
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
        previewFragment = PlayTagVideoFragment_.builder().oneVideo(item).build();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, previewFragment).commit();
//        PlayActivity_.intent(this).type(PlayActivity.TYPE_VIDEO).oneVideo(item).start();
    }


}

