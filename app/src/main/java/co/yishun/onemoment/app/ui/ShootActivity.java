package co.yishun.onemoment.app.ui;

import android.animation.ObjectAnimator;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;

import com.transitionseverywhere.Scene;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.view.PageIndicatorDot;
import co.yishun.onemoment.app.ui.view.shoot.CameraGLSurfaceView;
import co.yishun.onemoment.app.ui.view.shoot.IShootView;
import co.yishun.onemoment.app.ui.view.shoot.filter.FilterManager;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import me.toxz.circularprogressview.library.CircularProgressView;

/**
 * Created by Carlos on 2015/10/4.
 */
@EActivity(R.layout.activity_shoot)
public class ShootActivity extends BaseActivity implements Callback, Consumer<File> {
    private static final String TAG = "ShootActivity";
    IShootView shootView;
    //    @ViewById unable by AndroidAnnotation because the smooth fake layout causes that it cannot find the really View, we must findViewById after transition animation
    ImageSwitcher recordFlashSwitch;
    //    @ViewById unable by AndroidAnnotation because the smooth fake layout causes that it cannot find the really View, we must findViewById after transition animation
    ImageSwitcher cameraSwitch;
    PageIndicatorDot pageIndicatorDot;

    @Extra
    int transitionX;
    @Extra
    int transitionY;

    // forwarding to MomentCreateActivity
    @Extra
    boolean forWorld = false;
    @Extra
    WorldTag worldTag;

    private ViewGroup sceneRoot;
    private
    @Nullable
    CameraGLSurfaceView mCameraGLSurfaceView;
    private boolean flashOn = false;
    private int currentFilter = 0;

    @Override
    public void setPageInfo() {
        mPageName = "ShootActivity";
    }

    @Override
    protected void onResume() {
        if (mCameraGLSurfaceView != null) {
            mCameraGLSurfaceView.onResume();
        }
        super.onResume();
    }

    @UiThread()
    @AfterViews void preTransition() {
        sceneRoot = (ViewGroup) findViewById(R.id.linearLayout);
        sceneRoot.setVisibility(View.INVISIBLE);
        sceneRoot.post(() -> {
            int finalRadius = (int) Math.hypot(sceneRoot.getWidth(), sceneRoot.getHeight());
            // before lollipop, the topMargin start below statusBar
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    int result = getResources().getDimensionPixelSize(resourceId);
                    transitionY -= result;
                }
            }
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(sceneRoot, transitionX, transitionY, 0, finalRadius);
            Log.d(TAG, transitionX + " " + transitionY + " " + finalRadius);
            animator.setDuration(350);
            animator.start();
            sceneRoot.setVisibility(View.VISIBLE);
        });

    }

    @UiThread(delay = 250)
    @AfterViews void sceneTransition() {
        Scene scene = Scene.getSceneForLayout(sceneRoot, R.layout.scene_activity_shoot, this);
        TransitionSet set = new TransitionSet();
        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
        set.setDuration(50);
        TransitionManager.go(scene, set);
        afterTransition();
    }

    void afterTransition() {
        shootView = (IShootView) findViewById(R.id.shootView);
        if (shootView instanceof CameraGLSurfaceView) {
            mCameraGLSurfaceView = ((CameraGLSurfaceView) shootView);
            pageIndicatorDot = ((PageIndicatorDot) findViewById(R.id.pageIndicator));
            pageIndicatorDot.setNum(FilterManager.FilterType.values().length);

            mCameraGLSurfaceView.setOnClickListener(v -> {
                Log.i(TAG, "click to set filter");
                currentFilter = (currentFilter + 1) % FilterManager.FilterType.values().length;
                mCameraGLSurfaceView.nextFilter();
                pageIndicatorDot.setCurrent(currentFilter);
            });
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(findViewById(R.id.maskImageView), "alpha", 1f, 0f).setDuration(350);
        animator.start();

        recordFlashSwitch = (ImageSwitcher) findViewById(R.id.recordFlashSwitch);
        cameraSwitch = (ImageSwitcher) findViewById(R.id.cameraSwitch);

        CircularProgressView shootBtn = ((CircularProgressView) findViewById(R.id.shootBtn));
        shootBtn.setOnStateListener(status -> {
            switch (status) {
                case START:
                    shootBtn.setDuration(1100);
                    shootView.record(this, this);
                    break;
            }
        });

        setControllerBtn();

        recordFlashSwitch.getCurrentView().setOnClickListener(this::flashlightBtnClicked);
        recordFlashSwitch.getNextView().setOnClickListener(this::flashlightBtnClicked);
        cameraSwitch.getCurrentView().setOnClickListener(this::cameraSwitchBtnClicked);
        cameraSwitch.getNextView().setOnClickListener(this::cameraSwitchBtnClicked);
    }

    //    @Click unable by AndroidAnnotation because the smooth fake layout causes that it cannot find the really View, we must findViewById after transition animation
    //    void shootBtnClicked(View v) {
    //        shootView.record(this, this);
    //    }

    @Override
    protected void onPause() {
        if (mCameraGLSurfaceView != null) {
            mCameraGLSurfaceView.onPause();
        }
        super.onPause();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        if (mCameraGLSurfaceView != null) {
            mCameraGLSurfaceView.onDestroy();
        }
        super.onDestroy();
    }

    private void setControllerBtn() {
        flashOn = false;
        cameraSwitch.setEnabled(shootView.isFrontCameraAvailable());
        recordFlashSwitch.setEnabled(shootView.isFlashlightAvailable());
        cameraSwitch.setDisplayedChild(0);
        recordFlashSwitch.setDisplayedChild(0);
    }

    private void cameraSwitchBtnClicked(View view) {
        shootView.switchCamera(!shootView.isBackCamera());
        setControllerBtn();
    }

    private void flashlightBtnClicked(View view) {
        Log.i(TAG, "flashlight switch, from " + flashOn + " to " + !flashOn);
        flashOn = !flashOn;
        shootView.setFlashlightOn(flashOn);
        recordFlashSwitch.setDisplayedChild(flashOn ? 1 : 0);
    }

    @Override
    public void call() {
        Log.i(TAG, "start record callback");
    }

    @Override
    public void accept(File file) {
        Log.i(TAG, "accept: " + file);
        delayStart(file);
    }

    @UiThread(delay = 200) void delayStart(File file) {
        MomentCreateActivity_.intent(this).videoPath(file.getPath()).forWorld(forWorld).worldTag(worldTag).start();
        this.finish();
    }
}