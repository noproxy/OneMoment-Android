package co.yishun.onemoment.app.ui;

import android.animation.ObjectAnimator;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageSwitcher;
import android.widget.Toast;

import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Scene;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.view.shoot.CameraGLSurfaceView;
import co.yishun.onemoment.app.ui.view.shoot.IShootView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

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
    private ViewGroup sceneRoot;
    private
    @Nullable
    CameraGLSurfaceView mCameraGLSurfaceView;
    private boolean flashOn = false;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @Override
    protected void onResume() {
        if (mCameraGLSurfaceView != null) {
            mCameraGLSurfaceView.onResume();
        }
        super.onResume();
    }

    @UiThread()
    @AfterViews
    void preTransition() {
        sceneRoot = (ViewGroup) findViewById(R.id.linearLayout);

        int cx = sceneRoot.getRight() - 132;
        int cy = sceneRoot.getBottom() - 132;
        int finalRadius = (int) Math.hypot(sceneRoot.getWidth(), sceneRoot.getHeight());

        SupportAnimator animator = ViewAnimationUtils.createCircularReveal(sceneRoot, cx, cy, 0, finalRadius);
        animator.setInterpolator(new AccelerateInterpolator());
        animator.setDuration(350);
        animator.start();

    }

    @UiThread(delay = 250)
    @AfterViews
    void sceneTransition() {
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
        }

        ObjectAnimator animator = ObjectAnimator.ofFloat(findViewById(R.id.maskImageView), "alpha", 1f, 0f).setDuration(350);
        animator.start();

        recordFlashSwitch = (ImageSwitcher) findViewById(R.id.recordFlashSwitch);
        cameraSwitch = (ImageSwitcher) findViewById(R.id.cameraSwitch);
        findViewById(R.id.shootBtn).setOnClickListener(this::shootBtnClicked);

        setControllerBtn();


        recordFlashSwitch.getCurrentView().setOnClickListener(this::flashlightBtnClicked);
        recordFlashSwitch.getNextView().setOnClickListener(this::flashlightBtnClicked);
        cameraSwitch.getCurrentView().setOnClickListener(this::cameraSwitchBtnClicked);
        cameraSwitch.getNextView().setOnClickListener(this::cameraSwitchBtnClicked);
    }

    //    @Click unable by AndroidAnnotation because the smooth fake layout causes that it cannot find the really View, we must findViewById after transition animation
    void shootBtnClicked(View v) {
        shootView.record(this, this);
    }

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
        Toast.makeText(ShootActivity.this, "start record", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void accept(File file) {
        Toast.makeText(ShootActivity.this, "success: " + file.getPath(), Toast.LENGTH_SHORT).show();
    }
}