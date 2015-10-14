package co.yishun.onemoment.app.ui;

import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageSwitcher;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.EActivity;
import org.androidannotations.annotations.ViewById;

import co.yishun.onemoment.app.R;
import co.yishun.onemoment.app.ui.common.BaseActivity;
import co.yishun.onemoment.app.ui.view.shoot.IShootView;

/**
 * Created by Carlos on 2015/10/4.
 */
@EActivity(R.layout.activity_shoot)
public class ShootActivity extends BaseActivity {
    IShootView shootView;
    @ViewById ImageSwitcher recordFlashSwitch;
    @ViewById ImageSwitcher cameraSwitch;
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

    @AfterViews
    void setShootView() {
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
        shootView.switchCamera(!shootView.isBackCamera());
        setControllerBtn();
    }

    private void flashlightBtnClicked(View view) {
        flashOn = !flashOn;
        shootView.setFlashlightOn(flashOn);
        recordFlashSwitch.setDisplayedChild(flashOn ? 1 : 0);
    }
}
