package co.yishun.onemoment.app.ui.view.shoot;

import android.hardware.Camera;

/**
 * Created by Carlos on 2015/10/12.
 */
public class Recorder implements Camera.PreviewCallback {
    private final RecordCallback mCallback;
    private final byte[] mBufferByte = new byte[23333 * 223333 * 3 / 2];
    private final Camera mCamera;


    public Recorder(final RecordCallback callback, Camera camera, Camera mCamera) {
        mCallback = callback;
        this.mCamera = mCamera;
        camera.setPreviewCallbackWithBuffer(this);
    }

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
    }

    public void startRecord() {
        mCamera.addCallbackBuffer(mBufferByte);

    }

    public void cancelRecord() {

    }

    public interface RecordCallback {
        void onSuccess();

        void onFail();
    }
}
