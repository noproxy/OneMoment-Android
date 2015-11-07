package co.yishun.onemoment.app.ui;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.squareup.picasso.Picasso;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.ChangeImageTransform;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Scene;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.ui.adapter.AbstractRecyclerViewAdapter;
import co.yishun.onemoment.app.ui.adapter.TagAdapter;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.controller.TagController_;
import co.yishun.onemoment.app.ui.view.RoundRectImageView;

/**
 * Created by Carlos on 2015/8/17.
 */

@EActivity(R.layout.activity_tag)
public class TagActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<TagVideo> {
    public static final int FROM_WORLD_FRAGMENT = 0;
    public static final int FROM_SEARCH_ACTIVITY = 1;
    @Extra
    int top;
    @Extra
    int from;
    @Extra
    WorldTag tag;
    @ViewById
    CoordinatorLayout coordinatorLayout;

    Toolbar toolbar;
    SuperRecyclerView recyclerView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView videoImageView;
    SwipeRefreshLayout swipeRefreshLayout;
    ImageView addImageView;
    private boolean transitionOver = false;
    private TagAdapter tagAdapter;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    void setLayout() {
        swipeRefreshLayout = ((SwipeRefreshLayout) coordinatorLayout.findViewById(R.id.ptr_layout));
        toolbar = ((Toolbar) coordinatorLayout.findViewById(R.id.toolbar));
        videoImageView = ((ImageView) coordinatorLayout.findViewById(R.id.videoImageView));
        recyclerView = ((SuperRecyclerView) coordinatorLayout.findViewById(R.id.recyclerView));
        collapsingToolbarLayout = ((CollapsingToolbarLayout) coordinatorLayout.findViewById(R.id.collapsingToolbarLayout));
    }

    @AfterViews
    void preTransition() {
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
            if (resourceId > 0) {
                int result = getResources().getDimensionPixelSize(resourceId);
                top -= result;

            }
        }
        params.topMargin += top;
        videoImageView.setLayoutParams(params);

        Picasso.with(this).load(tag.domain + tag.thumbnail).into(videoImageView);
        collapsingToolbarLayout.setTitle("");
        collapsingToolbarLayout.setTitleEnabled(false);
    }

    @UiThread(delay = 100)
    @AfterViews
    void sceneTransition() {
        ViewGroup sceneRoot =coordinatorLayout;
        Scene scene = Scene.getSceneForLayout(sceneRoot, R.layout.scene_activity_tag, this);

        ObjectAnimator animator = ObjectAnimator.ofInt(sceneRoot, "backgroundColor",
                0x00ffffff, getResources().getColor(R.color.colorPrimary)).setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();

        TransitionSet set = new TransitionSet();

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.addTarget(R.id.videoImageView);
        changeBounds.addTarget(R.id.collapsingToolbarLayout);
        changeBounds.addTarget(R.id.appBar);
        changeBounds.addTarget(R.id.videoFrame);
        changeBounds.setDuration(500);
        changeBounds.setInterpolator(new DecelerateInterpolator(2.0f));
        set.addTransition(changeBounds);

        ChangeImageTransform changeImageTransform = new ChangeImageTransform();
        changeImageTransform.addTarget(R.id.videoImageView);
        changeImageTransform.setDuration(500);
        set.addTransition(changeImageTransform);

        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.addTarget(R.id.recyclerView);
        fadeIn.addTarget(R.id.toolbar);
        fadeIn.addTarget(R.id.addImageView);
        fadeIn.addTarget(R.id.videoMask);
        fadeIn.addTarget(R.id.collapsingToolbarLayout);
        fadeIn.setStartDelay(500);
        set.addTransition(fadeIn);

        Slide slide = new Slide(Gravity.BOTTOM);
        slide.addTarget(R.id.recyclerView);
        slide.setDuration(500);
        set.addTransition(slide);

        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
        set.setDuration(800);

        float oldRadiusRate = ((RoundRectImageView) sceneRoot.findViewById(R.id.videoImageView)).getRoundRate();
        TransitionManager.go(scene, set);
        sceneRoot.setFitsSystemWindows(true);
        float newRadiusRate = ((RoundRectImageView) sceneRoot.findViewById(R.id.videoImageView)).getRoundRate();
        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(sceneRoot.findViewById(R.id.videoImageView),
                "roundRate", oldRadiusRate, newRadiusRate).setDuration(500);
        radiusAnimator.setEvaluator(new FloatEvaluator());
        radiusAnimator.setInterpolator(new AccelerateInterpolator());
        radiusAnimator.start();

        afterTransition();

        videoImageView = ((ImageView) findViewById(R.id.videoImageView));
        swipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.ptr_layout));
        recyclerView = ((SuperRecyclerView) findViewById(R.id.recyclerView));
        addImageView = (ImageView) findViewById(R.id.addImageView);
        addImageView.setOnClickListener(this::addImageClicked);

        Picasso.with(this).load(tag.domain + tag.thumbnail).into(videoImageView);
        videoImageView.setOnClickListener(this::videoImageClick);

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        tagAdapter = new TagAdapter(this, this);
        recyclerView.setAdapter(tagAdapter);
        TagController_.getInstance_(this).setUp(tagAdapter, recyclerView, tag);

        transitionOver = true;
    }

    @UiThread(delay = 600)
    void afterTransition() {
        toolbar = ((Toolbar) findViewById(R.id.toolbar));
        collapsingToolbarLayout = ((CollapsingToolbarLayout) findViewById(R.id.collapsingToolbarLayout));
        setupToolbar(this, toolbar);
        collapsingToolbarLayout.setContentScrimColor(getResources().getColor(R.color.colorPrimary));
        collapsingToolbarLayout.setTitleEnabled(true);
        collapsingToolbarLayout.setTitle(tag.name);
        collapsingToolbarLayout.setCollapsedTitleTextColor(getResources().getColor(R.color.textColorPrimary));
        collapsingToolbarLayout.setExpandedTitleColor(getResources().getColor(R.color.textColorPrimaryInverse));
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (transitionOver) {
            tagAdapter.clear();
            TagController_.getInstance_(this).setUp(tagAdapter, recyclerView, tag);
        }
    }

    @CallSuper
    protected ActionBar setupToolbar(AppCompatActivity activity, Toolbar toolbar) {
        if (toolbar == null)
            throw new UnsupportedOperationException("You need bind Toolbar instance to" +
                    " toolbar in onCreateView(LayoutInflater, ViewGroup, Bundle");
        activity.setSupportActionBar(toolbar);

        final ActionBar ab = activity.getSupportActionBar();
        assert ab != null;
        ab.setDisplayHomeAsUpEnabled(true);
        Log.i("setupToolbar", "set home as up true");
        return ab;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    void addImageClicked(View view) {
        int[] location = new int[2];
        view.getLocationOnScreen(location);
        ShootActivity_.intent(this).transitionX(location[0] + view.getWidth() / 2)
                .transitionY(location[1] + view.getHeight() / 2).worldTag(tag).forWorld(true).start();
    }

    void videoImageClick(View v) {
        PlayActivity_.intent(this).worldTag(tag).type(PlayActivity.TYPE_WORLD).start();
    }

    @Override
    public void onClick(View view, TagVideo item) {
        PlayActivity_.intent(this).oneVideo(item).worldTag(tag).type(PlayActivity.TYPE_VIDEO).start();
    }
}
