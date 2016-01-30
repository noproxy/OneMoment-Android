package co.yishun.onemoment.app.ui;

import android.animation.ObjectAnimator;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageSwitcher;

import com.afollestad.materialdialogs.MaterialDialog;
import com.transitionseverywhere.Scene;
import com.transitionseverywhere.TransitionManager;
import com.transitionseverywhere.TransitionSet;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.Extra;
import org.androidannotations.annotations.UiThread;

import java.io.File;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.api.modelv4.WorldProvider;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.view.PageIndicatorDot;
import co.yishun.onemoment.app.ui.view.shoot.CameraGLSurfaceView;
import co.yishun.onemoment.app.ui.view.shoot.IShootView;
import co.yishun.onemoment.app.ui.view.shoot.filter.FilterManager;
import co.yishun.onemoment.app.video.VideoCommand;
import co.yishun.onemoment.app.video.VideoConvert;
import io.codetail.animation.SupportAnimator;
import io.codetail.animation.ViewAnimationUtils;
import me.toxz.circularprogressview.library.CircularProgressView;

/**
 * Created by Carlos on 2015/10/4.
 */
@EActivity(R.layout.activity_shoot)
public class ShootActivity extends BaseActivity implements Callback, Consumer<File>, IShootView.SecurityExceptionHandler {
    private static final String TAG = "ShootActivity";
    IShootView shootView;
    //    @ViewById unable by AndroidAnnotation because the smooth fake layout causes that it cannot find the really View, we must findViewById after transition animation
    ImageSwitcher recordFlashSwitch;
    //    @ViewById unable by AndroidAnnotation because the smooth fake layout causes that it cannot find the really View, we must findViewById after transition animation
    ImageSwitcher cameraSwitch;
    PageIndicatorDot pageIndicatorDot;

    @Extra int transitionX;
    @Extra int transitionY;

    // forwarding to MomentCreateActivity
    @Extra boolean forWorld = false;
    @Extra boolean forToday = false;
    @Extra WorldProvider world;

    private ViewGroup sceneRoot;
    @Nullable private CameraGLSurfaceView mCameraGLSurfaceView;
    private boolean flashOn = false;

    @Override public void setPageInfo() {
        mPageName = "ShootActivity";
    }

    @Override protected void onResume() {
        if (mCameraGLSurfaceView != null) {
            mCameraGLSurfaceView.onResume();
        }
        super.onResume();
    }

    @UiThread() @AfterViews void preTransition() {
        sceneRoot = (ViewGroup) findViewById(R.id.linearLayout);
        sceneRoot.setVisibility(View.INVISIBLE);
        sceneRoot.post(() -> {
            // before lollipop, the topMargin start below statusBar
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
                if (resourceId > 0) {
                    int result = getResources().getDimensionPixelSize(resourceId);
                    transitionY -= result;
                }
            }
            int finalRadius = (int) Math.hypot(Math.max(transitionX, sceneRoot.getWidth() - transitionX),
                    Math.max(transitionY, sceneRoot.getHeight() - transitionY));
            SupportAnimator animator = ViewAnimationUtils.createCircularReveal(sceneRoot, transitionX, transitionY, 0, finalRadius);
            LogUtil.d(TAG, transitionX + " " + transitionY + " " + finalRadius);
            animator.setDuration(350);
            animator.start();
            sceneRoot.setVisibility(View.VISIBLE);
        });

    }

    @UiThread(delay = 250) @AfterViews void sceneTransition() {
        Scene scene = Scene.getSceneForLayout(sceneRoot, R.layout.scene_activity_shoot, this);
        TransitionSet set = new TransitionSet();
        set.setOrdering(TransitionSet.ORDERING_TOGETHER);
        set.setDuration(50);
        TransitionManager.go(scene, set);
        afterTransition();
    }

    void afterTransition() {
        shootView = (IShootView) findViewById(R.id.shootView);
        shootView.setSecurityExceptionHandler(this);
        if (shootView instanceof CameraGLSurfaceView) {
            mCameraGLSurfaceView = ((CameraGLSurfaceView) shootView);
            pageIndicatorDot = ((PageIndicatorDot) findViewById(R.id.pageIndicator));
            pageIndicatorDot.setNum(FilterManager.FilterType.values().length);

            mCameraGLSurfaceView.setFilterListener(index -> pageIndicatorDot.setCurrent(index));
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

    @Override protected void onPause() {
        if (mCameraGLSurfaceView != null) {
            mCameraGLSurfaceView.onPause();
        }
        super.onPause();
        this.finish();
    }

    @Override protected void onDestroy() {
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
        LogUtil.i(TAG, "flashlight switch, from " + flashOn + " to " + !flashOn);
        flashOn = !flashOn;
        shootView.setFlashlightOn(flashOn);
        recordFlashSwitch.setDisplayedChild(flashOn ? 1 : 0);
    }

    @Override public void call() {
        LogUtil.i(TAG, "start record callback");
    }

    @Override public void accept(File file) {
        LogUtil.i(TAG, "accept: " + file);
        if (shootView instanceof CameraGLSurfaceView)
            delayStart(file);
        else {
            showProgress();
            delayAccept(file);
        }
    }

    @UiThread(delay = 800) void delayAccept(File file) {
        File newFile = FileUtil.getVideoCacheFile(this);
        new VideoConvert(this).setFiles(file, newFile).setListener(new VideoCommand.VideoCommandListener() {
            @Override public void onSuccess(VideoCommand.VideoCommandType type) {
                file.delete();
                delayStart(newFile);
                hideProgress();
            }

            @Override public void onFail(VideoCommand.VideoCommandType type) {
                hideProgress();
            }
        }).start();
    }

    @UiThread(delay = 200) void delayStart(File file) {
        TagCreateActivity_.intent(this).forWorld(forWorld).forToday(forToday).world(world).videoPath(file.getPath()).start();
        this.finish();
    }

    @Override public void onHandler(SecurityException e) {
        LogUtil.e(TAG, "", e);
        MaterialDialog.Builder builder = new MaterialDialog.Builder(this).positiveText(R.string.activity_shoot_permission_error_ok).content(R.string.activity_shoot_permission_error_msg).title(R.string.activity_shoot_permission_error_title).cancelable(false);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            builder.negativeText(R.string.activity_shoot_permission_error_settings);
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override public void onPositive(MaterialDialog dialog) {
                    ShootActivity.this.finish();
                }

                @Override public void onNegative(MaterialDialog dialog) {
                    try {
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:" + getPackageName()));
                            intent.addCategory(Intent.CATEGORY_DEFAULT);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(intent);
                        }
                    } catch (Exception ignore) {
                        ignore.printStackTrace();
                    }
                }
            });
        } else {
            builder.callback(new MaterialDialog.ButtonCallback() {
                @Override public void onPositive(MaterialDialog dialog) {
                    ShootActivity.this.finish();
                }
            });
        }
        builder.show();
        //TODO add help btn to guide user to how enable permission for three-party rom
    }
}