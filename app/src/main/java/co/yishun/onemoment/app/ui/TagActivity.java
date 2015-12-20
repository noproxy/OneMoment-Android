package co.yishun.onemoment.app.ui;

import android.animation.ArgbEvaluator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
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

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Background;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.World;
import co.yishun.onemoment.app.api.authentication.OneMomentV3;
import co.yishun.onemoment.app.api.loader.VideoTaskManager;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.TagController_;
import co.yishun.onemoment.app.ui.share.ShareActivity;
import co.yishun.onemoment.app.ui.share.ShareActivity_;
import co.yishun.onemoment.app.ui.view.GridSpacingItemDecoration;

/**
 * Created by Carlos on 2015/8/17.
 */

@EActivity(R.layout.activity_tag)
public class TagActivity extends BaseActivity
        implements AbstractRecyclerViewAdapter.OnItemClickListener<TagVideo> {
    public static final int FROM_WORLD_FRAGMENT = 0;
    public static final int FROM_SEARCH_ACTIVITY = 1;
    private static final String TAG = "TagActivity";
    @Extra int top;
    @Extra int from;
    @Extra WorldTag tag;
    @Extra boolean isPrivate = false;
    @ViewById CoordinatorLayout coordinatorLayout;

    Toolbar toolbar;
    SuperRecyclerView recyclerView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView videoImageView;
    SwipeRefreshLayout swipeRefreshLayout;
    private boolean transitionOver = false;
    private TagAdapter tagAdapter;
    private int statusBarHeight;
    private int collapsedTitleColor;
    private int collapsedSubTitleColor;
    private int expendedTitleColor;
    private int expendedSubTitleColor;

    @Override
    public void setPageInfo() {
        mPageName = "TagActivity";
    }

    void setLayout() {
        swipeRefreshLayout = ((SwipeRefreshLayout) coordinatorLayout.findViewById(R.id.ptr_layout));
        toolbar = ((Toolbar) coordinatorLayout.findViewById(R.id.toolbar));
        videoImageView = ((ImageView) coordinatorLayout.findViewById(R.id.videoImageView));
        recyclerView = ((SuperRecyclerView) coordinatorLayout.findViewById(R.id.recyclerView));
    }

    @AfterViews void preTransition() {
        if (from == FROM_WORLD_FRAGMENT) {
            coordinatorLayout.addView(LayoutInflater.from(this).inflate(
                    R.layout.scene_activity_tag_world_smooth, coordinatorLayout, false));
        } else if (from == FROM_SEARCH_ACTIVITY) {
            coordinatorLayout.addView(LayoutInflater.from(this).inflate(
                    R.layout.scene_activity_tag_search_smooth, coordinatorLayout, false));
        }
        setLayout();
        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoImageView.getLayoutParams();
        // before lollipop, the topMargin start below statusBar
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = getResources().getDimensionPixelSize(resourceId);
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                top -= statusBarHeight;
            }
        }
        params.topMargin += top;
        videoImageView.setLayoutParams(params);

        Picasso.with(this).load(tag.domain + tag.thumbnail).into(videoImageView);
    }

    @UiThread(delay = 100)
    @AfterViews void sceneTransition() {
        ViewGroup sceneRoot = coordinatorLayout;
        Scene scene = Scene.getSceneForLayout(sceneRoot, R.layout.scene_activity_tag, this);

        ObjectAnimator animator = ObjectAnimator.ofInt(sceneRoot, "backgroundColor",
                0x00ffffff, getResources().getColor(R.color.colorPrimary)).setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();

        TransitionSet set = (TransitionSet) TransitionInflater.from(this).inflateTransition(R.transition.activity_tag_transition);
        TransitionManager.go(scene, set);

        afterTransition();

        videoImageView = ((ImageView) findViewById(R.id.videoImageView));
        swipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.ptr_layout));
        recyclerView = ((SuperRecyclerView) findViewById(R.id.recyclerView));
        findViewById(R.id.worldAdd).setOnClickListener(this::addVideo);
        findViewById(R.id.worldShare).setOnClickListener(this::shareWorld);

        Picasso.with(this).load(tag.domain + tag.thumbnail).into(videoImageView);
        videoImageView.setOnClickListener(this::videoImageClick);

        int spanCount = 3;
        int spacing = (int) getResources().getDimension(R.dimen.video_grid_divider);
        GridLayoutManager manager = new GridLayoutManager(this, spanCount);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(spanCount, spacing, false));

        tagAdapter = new TagAdapter(this, this);
        recyclerView.setAdapter(tagAdapter);
        TagController_.getInstance_(this).setUp(tagAdapter, recyclerView, tag, isPrivate);

        transitionOver = true;
    }

    @UiThread(delay = 600) void afterTransition() {
        AppBarLayout appbar = (AppBarLayout) findViewById(R.id.appBar);
        toolbar = ((Toolbar) findViewById(R.id.toolbar));
        collapsingToolbarLayout = ((CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout));
        expendedTitleColor = getResources().getColor(R.color.colorPrimary);
        expendedSubTitleColor = getResources().getColor(R.color.colorPrimary);
        collapsedTitleColor = getResources().getColor(R.color.textColorPrimary);
        collapsedSubTitleColor = getResources().getColor(R.color.textColorPrimaryDark);

        setupToolbar(this, toolbar);
        appbar.addOnOffsetChangedListener(new OffsetChangeListener());
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (transitionOver) {
            TagController_.getInstance_(this).setUp(tagAdapter, recyclerView, tag, isPrivate);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        VideoTaskManager.getInstance().quit();
    }

    @CallSuper
    protected ActionBar setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);

        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        toolbar.setTitle(tag.name);
        toolbar.setTitleTextColor(expendedTitleColor);
        toolbar.setSubtitleTextColor(expendedSubTitleColor);
        String num = String.valueOf(tag.videosCount);
        SpannableString ss = new SpannableString(num + "人加入");
        ss.setSpan(new ForegroundColorSpan(getResources().getColor(R.color.colorAccent)), 0, num.length() + 1, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        toolbar.setSubtitle(ss);
        ab.setDisplayHomeAsUpEnabled(true);
        Log.i("setupToolbar", "set home as up true");
        return ab;
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void addVideo(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        ShootActivity_.intent(this).transitionX(location[0] + view.getWidth() / 2)
                .transitionY(location[1] + view.getHeight() / 2).worldTag(tag).forWorld(true).start();
    }

    @Background void shareWorld(View view) {
        World world = OneMomentV3.createAdapter().create(World.class);
        ShareInfo shareInfo = world.shareWorld(tag.name);
        ShareActivity_.intent(this).shareInfo(shareInfo).shareType(ShareActivity.TYPE_SHARE_WORLD).start();
    }

    void videoImageClick(View v) {
        PlayActivity_.intent(this).worldTag(tag).type(PlayActivity.TYPE_WORLD).start();
    }

    @Override
    public void onClick(View view, TagVideo item) {
        PlayActivity_.intent(this).oneVideo(item).worldTag(tag).type(PlayActivity.TYPE_VIDEO).start();
    }

    private class OffsetChangeListener implements AppBarLayout.OnOffsetChangedListener {

        @Override
        public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
            int insetTop = Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP ? 0 : statusBarHeight;
            float fraction = Math.abs(verticalOffset) /
                    (float) (appBarLayout.getHeight() -
                            ViewCompat.getMinimumHeight(collapsingToolbarLayout) - insetTop);
            changeTitleColor(fraction);
        }
    }
}
