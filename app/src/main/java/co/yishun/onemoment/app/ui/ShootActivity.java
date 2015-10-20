package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageSwitcher;

import com.transitionseverywhere.Fade;
import com.transitionseverywhere.Scene;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.UiThread;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.view.shoot.IShootView;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;

/**
 * Created by Carlos on 2015/10/4.
 */
@EActivity(R.layout.activity_shoot)
public class ShootActivity extends BaseActivity {
    private ViewGroup sceneRoot;
    IShootView shootView;
    @ViewById
    ImageSwitcher recordFlashSwitch;
    @ViewById
    ImageSwitcher cameraSwitch;
    private boolean flashOn = false;

    @Nullable
    @Override
    public View getSnackbarAnchorWithView(@Nullable View view) {
        return null;
    }

    @Override
    protected void onResume() {
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
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.setDuration(500);
        animator.start();



    }

    @UiThread(delay = 50)
    @AfterViews
    void sceneTransition() {
        Scene scene = Scene.getSceneForLayout(sceneRoot, R.layout.scene_activity_shoot, this);
        TransitionSet set = new TransitionSet();

        Fade fadeIn = new Fade(Fade.IN);
        fadeIn.addTarget(R.id.shootView);
        fadeIn.setStartDelay(350);
        set.addTransition(fadeIn);

        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
        set.setDuration(800);
        TransitionManager.go(scene, set);

        afterTransition();
    }

    void afterTransition() {
        shootView = (IShootView) findViewById(R.id.shootView);
        setControllerBtn();

        recordFlashSwitch.getCurrentView().setOnClickListener(this::flashlightBtnClicked);
        recordFlashSwitch.getNextView().setOnClickListener(this::flashlightBtnClicked);
        cameraSwitch.getCurrentView().setOnClickListener(this::cameraSwitchBtnClicked);
        cameraSwitch.getNextView().setOnClickListener(this::cameraSwitchBtnClicked);
    }

    @Override
    protected void onPause() {
        super.onPause();
        this.finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (shootView != null)
            shootView.releaseCamera();
    }

    private void setControllerBtn() {
        flashOn = false;
        cameraSwitch.setEnabled(shootView.isFrontCameraAvailable());
        recordFlashSwitch.setEnabled(shootView.isFlashlightAvailable());
        cameraSwitch.setDisplayedChild(0);
        recordFlashSwitch.setDisplayedChild(0);
    }

    private void cameraSwitchBtnClicked(View view) {
        shootView.setBackCameraOn(!shootView.isBackCamera());
        setControllerBtn();
    }

    private void flashlightBtnClicked(View view) {
        flashOn = !flashOn;
        shootView.setFlashlightOn(flashOn);
        recordFlashSwitch.setDisplayedChild(flashOn ? 1 : 0);
    }
}
