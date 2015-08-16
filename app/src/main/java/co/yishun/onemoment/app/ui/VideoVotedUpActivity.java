package co.yishun.onemoment.app.ui;

import android.annotation.SuppressLint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.github.ppamorim.dragger.DraggerActivity;
import com.github.ppamorim.dragger.DraggerView;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.api.SdkVersionHelper;
import org.lucasr.twowayview.widget.DividerItemDecoration;
import org.lucasr.twowayview.widget.TwoWayView;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.VideoLikeAdapter;

/**
 * Created by yyz on 7/20/15.
 */
//TODO handle not extend BaseActivity
@EActivity(R.layout.activity_video_voted_up)
public class VideoVotedUpActivity extends DraggerActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<Video> {
    @ViewById Toolbar toolbar;
    @ViewById TwoWayView twoWayView;
    @ViewById DraggerView draggerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(0, 0);
        super.onCreate(savedInstanceState);
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

    @Override
    protected void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    @SuppressLint("NewApi")
    @AfterViews
    void setupTwoWayView() {
        twoWayView.setHasFixedSize(true);

        final Drawable divider;
        if (SdkVersionHelper.getSdkInt() > 21) {
            divider = getResources().getDrawable(R.drawable.divider, null);
        } else {
            //noinspection deprecation
            divider = getResources().getDrawable(R.drawable.divider);
        }
        twoWayView.addItemDecoration(new DividerItemDecoration(divider));

        twoWayView.setAdapter(new VideoLikeAdapter(this, this, twoWayView));
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
    public void onClick(View view, Video item) {

    }
}

