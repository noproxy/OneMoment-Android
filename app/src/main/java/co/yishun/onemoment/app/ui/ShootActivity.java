package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageSwitcher;
import android.widget.Toast;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import java.io.File;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.view.shoot.CameraGLSurfaceView;
import co.yishun.onemoment.app.ui.view.shoot.IShootView;

/**
 * Created by Carlos on 2015/10/4.
 */
@EActivity(R.layout.activity_shoot)
public class ShootActivity extends BaseActivity implements Callback, Consumer<File> {
    IShootView shootView;
    @ViewById ImageSwitcher recordFlashSwitch;
    @ViewById ImageSwitcher cameraSwitch;
    private @Nullable CameraGLSurfaceView mCameraGLSurfaceView;
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

    @AfterViews
    void setShootView() {
        shootView = (IShootView) findViewById(R.id.shootView);
        if (shootView instanceof CameraGLSurfaceView) {
            mCameraGLSurfaceView = ((CameraGLSurfaceView) shootView);
        }
        setControllerBtn();

        recordFlashSwitch.getCurrentView().setOnClickListener(this::flashlightBtnClicked);
        recordFlashSwitch.getNextView().setOnClickListener(this::flashlightBtnClicked);
        cameraSwitch.getCurrentView().setOnClickListener(this::cameraSwitchBtnClicked);
        cameraSwitch.getNextView().setOnClickListener(this::cameraSwitchBtnClicked);
    }

    @Click
    void shootBtnClicked() {
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
