package co.yishun.onemoment.app.ui;

import android.animation.ObjectAnimator;
import android.graphics.Rect;
import android.os.Build;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.Scene;
import com.transitionseverywhere.TransitionInflater;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import org.androidannotations.annotations.AfterInject;
import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.WorldAPI;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.loader.VideoTaskManager;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.WorldVideoAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.WorldVideosController_;
import co.yishun.onemoment.app.ui.view.GridSpacingItemDecoration;
import co.yishun.onemoment.app.ui.view.RadioCornerImageView;

/**
 * Created by Jinge on 2016/1/25.
 */
@EActivity(R.layout.activity_world_videos)
public class WorldVideosActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<WorldVideo> {
    private static final String TAG = "WorldVideosActivity";
    @Extra String thumbnail;
    @Extra String worldName;
    @Extra int videosNum;
    @Extra String worldId;
    @Extra boolean forWorld;
    /**
     * imageRect contains the original position of the {@link #transImage}.
     * And {@link #imageCorner} is the original corner of the {@link #transImage}.
     * Set imageRect and add flag {@link android.content.Intent#FLAG_ACTIVITY_NO_ANIMATION} to apply an transition.
     * If no transition needed when start this activity, set imageRect to null.
     */
    @Extra Rect imageRect;
    @Extra int imageCorner;

    @ViewById AppBarLayout appBar;
    @ViewById Toolbar toolbar;
    @ViewById SuperRecyclerView recyclerView;
    @ViewById CollapsingToolbarLayout collapsingToolbarLayout;
    @ViewById ImageView videoImageView;
    @ViewById FrameLayout transitionFrameLayout;
    @ViewById RadioCornerImageView transImage;

    private int statusBarHeight;
    private int collapsedTitleColor;
    private int collapsedSubTitleColor;
    private int expendedTitleColor;
    private int expendedSubTitleColor;
    private WorldVideoAdapter adapter;
    private boolean needTransition;


    @AfterInject void checkExtra() {
        needTransition = imageRect != null;
    }

    @UiThread(delay = 100) void setupTransition() {
        ViewGroup sceneRoot = transitionFrameLayout;
        LogUtil.d(TAG, imageRect.toString());
        Scene scene = Scene.getSceneForLayout(sceneRoot, R.layout.scene_world_videos_end, this);

        TransitionSet set = (TransitionSet) TransitionInflater.from(this)
                .inflateTransition(R.transition.activity_world_videos_transition);
        TransitionManager.go(scene, set);

        transImage = (RadioCornerImageView) findViewById(R.id.transImage);
        Picasso.with(this).load(thumbnail).into(transImage);

        ObjectAnimator appbarAnimator = ObjectAnimator.ofFloat(appBar, "alpha", 0, 1).setDuration(200);
        appbarAnimator.setStartDelay(400);
        appbarAnimator.start();

        ObjectAnimator recyclerAnimator = ObjectAnimator.ofFloat(recyclerView, "alpha", 0, 1).setDuration(200);
        recyclerAnimator.setStartDelay(400);
        recyclerAnimator.start();
    }

    @AfterViews void setupViews() {
        if (needTransition) {
            appBar.setAlpha(0);
            recyclerView.setAlpha(0);

            Picasso.with(this).load(thumbnail).into(transImage);
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) transImage.getLayoutParams();
            params.topMargin = imageRect.top;
            params.leftMargin = imageRect.left;
            params.width = imageRect.right - imageRect.left;
            params.height = imageRect.bottom - imageRect.top;
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                statusBarHeight = getResources().getDimensionPixelSize(resourceId);
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    params.topMargin -= statusBarHeight;
                }
            }
            transImage.setLayoutParams(params);

            transImage.setCorner(imageCorner);
            setupTransition();
        }

        expendedTitleColor = getResources().getColor(R.color.colorPrimary);
        expendedSubTitleColor = getResources().getColor(R.color.colorPrimary);
        collapsedTitleColor = getResources().getColor(R.color.textColorPrimary);
        collapsedSubTitleColor = getResources().getColor(R.color.textColorPrimaryDark);

        setupToolbar();
        appBar.addOnOffsetChangedListener(new OffsetChangeListener());

        int spanCount = 3;
        int spacing = (int) getResources().getDimension(R.dimen.video_grid_divider);
        GridLayoutManager manager = new GridLayoutManager(this, spanCount);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));

        adapter = new WorldVideoAdapter(this, this);
        recyclerView.setAdapter(adapter);
    }

    void setupToolbar() {
        setSupportActionBar(toolbar);
        final ActionBar ab = getSupportActionBar();
        assert ab != null;
        toolbar.setTitleTextColor(expendedTitleColor);
        toolbar.setSubtitleTextColor(expendedSubTitleColor);
        String num = String.valueOf(videosNum);
        SpannableString ss = new SpannableString(num + getString(R.string.fragment_world_suffix_people_count));
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, num.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        toolbar.setSubtitle(ss);
        ab.setTitle(worldName);
        ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override protected void onResume() {
        super.onResume();
        WorldVideosController_.getInstance_(this).setup(adapter, recyclerView, worldId, worldName,
                thumbnail, forWorld, videoImageView);
    }

    @Override protected void onPause() {
        super.onPause();
        VideoTaskManager.getInstance().quit();
    }


    @Override public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            NavUtils.navigateUpFromSameTask(this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Click(R.id.worldAdd) void addVideo(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        ShootActivity_.intent(this).transitionX(location[0] + view.getWidth() / 2)
                .transitionY(location[1] + view.getHeight() / 2)
                .forWorld(true).worldId(worldId).worldName(worldName).start();
    }

    @Click(R.id.worldShare) @Background void shareWorld(View view) {
        WorldAPI worldAPI = OneMomentV3.createAdapter().create(WorldAPI.class);
        ShareInfo shareInfo = worldAPI.shareWorld(worldName);
        ShareActivity_.intent(this).shareInfo(shareInfo).shareType(ShareActivity.TYPE_SHARE_WORLD).start();
    }

    void videoImageClick(View v) {
//        PlayActivity_.intent(this).worldTag(tag).type(PlayActivity.TYPE_WORLD).start();
    }

    @Override public void onClick(View view, WorldVideo item) {
//        PlayActivity_.intent(this).oneVideo(item).worldTag(tag).type(PlayActivity.TYPE_VIDEO).start();
    }

    @Override public void setPageInfo() {
        mPageName = "WorldVideosActivity";
    }

    void changeTitleColor(float fraction) {
        int startInt = expendedTitleColor;
        int startA = (startInt >> 24) & 0xff;
        int startR = (startInt >> 16) & 0xff;
        int startG = (startInt >> 8) & 0xff;
        int startB = startInt & 0xff;

        int endInt = collapsedTitleColor;
        int endA = (endInt >> 24) & 0xff;
        int endR = (endInt >> 16) & 0xff;
        int endG = (endInt >> 8) & 0xff;
        int endB = endInt & 0xff;

        toolbar.setTitleTextColor((startA + (int) (fraction * (endA - startA))) << 24 |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB)))));


        startInt = expendedSubTitleColor;
        startA = (startInt >> 24) & 0xff;
        startR = (startInt >> 16) & 0xff;
        startG = (startInt >> 8) & 0xff;
        startB = startInt & 0xff;

        endInt = collapsedSubTitleColor;
        endA = (endInt >> 24) & 0xff;
        endR = (endInt >> 16) & 0xff;
        endG = (endInt >> 8) & 0xff;
        endB = endInt & 0xff;

        toolbar.setSubtitleTextColor((startA + (int) (fraction * (endA - startA))) << 24 |
                ((startR + (int) (fraction * (endR - startR))) << 16) |
                ((startG + (int) (fraction * (endG - startG))) << 8) |
                ((startB + (int) (fraction * (endB - startB)))));
    }

    private class OffsetChangeListener implements AppBarLayout.OnOffsetChangedListener {

        @Override public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            int insetTop = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? 0 : statusBarHeight;
            float fraction = Math.abs(verticalOffset) / (float) (appBarLayout.getHeight() -
                    ViewCompat.getMinimumHeight(collapsingToolbarLayout) - insetTop);
            changeTitleColor(fraction);
        }
    }
}
