package co.yishun.onemoment.app.ui.view.shoot;

import java.io.File;

import co.yishun.onemoment.app.function.Callback;
import co.yishun.onemoment.app.function.Consumer;

/**
 * Created by Carlos on 2015/10/9.
 */
public interface IShootView {
    void releaseCamera();

    void setFlashlightOn(boolean isOn);

    void switchCamera(boolean isBack);

    boolean isFlashlightAvailable();

    boolean isFrontCameraAvailable();

    boolean isBackCamera();

    void record(Callback recordStartCallback, Consumer<File> recordEndConsumer);
}
