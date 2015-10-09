package co.yishun.onemoment.app.ui.view.shoot;

/**
 * Created by Carlos on 2015/10/9.
 */
public interface IShootView {
    void setCurrentCamera(boolean isFront);

    void releaseCamera();

    void setFlashlightController(FlashlightController controller);

    void setCameraSwitchController(CameraSwitchController controller);
}
