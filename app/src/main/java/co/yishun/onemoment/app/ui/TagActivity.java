package co.yishun.onemoment.app.ui;

import android.animation.ArgbEvaluator;
import android.animation.FloatEvaluator;
import android.animation.ObjectAnimator;
import android.content.pm.LabeledIntent;
import android.graphics.Color;
import android.support.annotation.CallSuper;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import com.transitionseverywhere.ChangeBounds;
import com.transitionseverywhere.ChangeClipBounds;
import com.transitionseverywhere.ChangeImageTransform;
import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Scene;
import com.transitionseverywhere.Slide;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterInject;
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
import co.yishun.onemoment.app.ui.view.SquareCircleImageView;

/**
 * Created by Carlos on 2015/8/17.
 */

@EActivity(R.layout.activity_tag)
public class TagActivity extends BaseActivity implements AbstractRecyclerViewAdapter.OnItemClickListener<TagVideo> {
    public static final int FROM_WORLD_FRAGMENT = 0;
    public static final int FROM_SEARCH_ACTIVITY = 1;
    @Extra int top;
    @Extra int from;
    @Extra
    WorldTag tag;
    @ViewById
    CoordinatorLayout coordinatorLayout;

    Toolbar toolbar;
    SuperRecyclerView recyclerView;
    CollapsingToolbarLayout collapsingToolbarLayout;
    ImageView videoImageView;
    SwipeRefreshLayout swipeRefreshLayout;

    @LayoutRes()
    String a = "";

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    void setLayout(){
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
            setLayout();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoImageView.getLayoutParams();
            params.topMargin += top;
        }
        else if (from==FROM_SEARCH_ACTIVITY) {
            coordinatorLayout.addView(LayoutInflater.from(this).inflate(
                    R.layout.scene_activity_tag_search_smooth, coordinatorLayout, false));
            setLayout();
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) videoImageView.getLayoutParams();
            params.topMargin += top;
        }
//        videoImageView.setBackgroundColor(tag.color);

        Picasso.with(this).load(tag.domain + tag.thumbnail).into(videoImageView);
        collapsingToolbarLayout.setTitle("");
        collapsingToolbarLayout.setTitleEnabled(false);
    }

    @UiThread(delay = 100)
    @AfterViews
    void sceneTransition() {
        if (from == FROM_WORLD_FRAGMENT) {
            sceneTransitionWorld();
        }
        else if (from == FROM_SEARCH_ACTIVITY) {
            sceneTransitionSearch();
        }

        afterTransition();

        videoImageView = ((ImageView) findViewById(R.id.videoImageView));
        swipeRefreshLayout = ((SwipeRefreshLayout) findViewById(R.id.ptr_layout));
        recyclerView = ((SuperRecyclerView) findViewById(R.id.recyclerView));

//        videoImageView.setBackgroundColor(tag.color);
        Picasso.with(this).load(tag.domain + tag.thumbnail).into(videoImageView);

        GridLayoutManager manager = new GridLayoutManager(this, 3);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);

        TagAdapter adapter = new TagAdapter(this, this);
        recyclerView.setAdapter(adapter);
        TagController_.getInstance_(this).setUp(adapter, recyclerView, tag);
    }

    void sceneTransitionWorld() {
        ViewGroup sceneRoot = (ViewGroup) findViewById(R.id.coordinatorLayout);
        ObjectAnimator animator = ObjectAnimator.ofInt(sceneRoot, "backgroundColor",
                0x00ffffff, getResources().getColor(R.color.colorPrimary)).setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();

        Scene scene = Scene.getSceneForLayout(sceneRoot, R.layout.scene_activity_tag, this);
        TransitionSet set = new TransitionSet();

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.addTarget(R.id.videoImageView);
        changeBounds.addTarget(R.id.collapsingToolbarLayout);
//        changeBounds.addTarget(R.id.itemLayout);
        changeBounds.addTarget(R.id.appBar);
        changeBounds.addTarget(R.id.videoFrame);
        changeBounds.setDuration(500);
        set.addTransition(changeBounds);

        ChangeImageTransform changeImageTransform = new ChangeImageTransform();
        changeImageTransform.addTarget(R.id.videoImageView);
        changeImageTransform.setDuration(500);
        set.addTransition(changeImageTransform);
        set.setInterpolator(new DecelerateInterpolator());

        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.addTarget(R.id.recyclerView);
        fadeIn.addTarget(R.id.toolbar);
        fadeIn.addTarget(R.id.collapsingToolbarLayout);
        fadeIn.setStartDelay(500);
        set.addTransition(fadeIn);

        Slide slide = new Slide(Gravity.BOTTOM);
        slide.addTarget(R.id.recyclerView);
        slide.setDuration(500);
        set.addTransition(slide);

        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
        set.setDuration(800);
        TransitionManager.go(scene, set);

        ((SquareCircleImageView)sceneRoot.findViewById(R.id.videoImageView)).setRadiusScale(3.0f);
    }

    void sceneTransitionSearch() {
        ViewGroup sceneRoot = (ViewGroup) findViewById(R.id.coordinatorLayout);
        ObjectAnimator animator = ObjectAnimator.ofInt(sceneRoot, "backgroundColor",
                0x00ffffff, getResources().getColor(R.color.colorPrimary)).setDuration(500);
        animator.setEvaluator(new ArgbEvaluator());
        animator.start();

        Scene scene = Scene.getSceneForLayout(sceneRoot, R.layout.scene_activity_tag, this);
        TransitionSet set = new TransitionSet();

        ChangeBounds changeBounds = new ChangeBounds();
        changeBounds.addTarget(R.id.videoImageView);
        changeBounds.addTarget(R.id.collapsingToolbarLayout);
        changeBounds.addTarget(R.id.appBar);
        changeBounds.addTarget(R.id.videoFrame);
        changeBounds.setDuration(500);
        set.addTransition(changeBounds);

        ChangeClipBounds clipBounds = new ChangeClipBounds();
        clipBounds.addTarget(R.id.videoImageView);
        set.addTransition(clipBounds);

        ChangeImageTransform changeImageTransform = new ChangeImageTransform();
        changeImageTransform.addTarget(R.id.videoImageView);
        changeImageTransform.setDuration(500);
        set.addTransition(changeImageTransform);
        set.setInterpolator(new DecelerateInterpolator());

        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.addTarget(R.id.recyclerView);
        fadeIn.addTarget(R.id.toolbar);
        fadeIn.addTarget(R.id.collapsingToolbarLayout);
        fadeIn.setStartDelay(500);
        set.addTransition(fadeIn);

        Slide slide = new Slide(Gravity.BOTTOM);
        slide.addTarget(R.id.recyclerView);
        slide.setDuration(500);
        set.addTransition(slide);

        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
        set.setDuration(800);
        TransitionManager.go(scene, set);

        ObjectAnimator radiusAnimator = ObjectAnimator.ofFloat(sceneRoot.findViewById(R.id.videoImageView),
                "radiusScale", 1.0f, 1.414f).setDuration(700);
        radiusAnimator.setEvaluator(new FloatEvaluator());
        radiusAnimator.start();
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
    public void onClick(View view, TagVideo item) {

    }
}
