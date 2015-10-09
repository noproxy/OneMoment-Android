package co.yishun.onemoment.app.ui.view.shoot;

/**
 * Created by Carlos on 2015/10/9.
 */
public interface IShootView {
    void releaseCamera();

    void setFlashlightOn(boolean isOn);

    void setBackCameraOn(boolean isBack);

    boolean isFlashlightAvailable();

    boolean isFrontCameraAvailable();

    boolean isBackCamera();
}
