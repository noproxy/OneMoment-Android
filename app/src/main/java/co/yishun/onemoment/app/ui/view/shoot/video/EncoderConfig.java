package co.yishun.onemoment.app.ui.view.shoot.video;

/**
 * Created by relex on 15/6/2.
 */

import android.annotation.TargetApi;
import android.opengl.EGLContext;

/**
 * Encoder configuration. <p> Object is immutable, which means we can safely pass it between threads
 * without explicit synchronization (and don't need to worry about it getting tweaked out from under
 * us). <p> TODO: make frame rate and iframe interval configurable?  Maybe use builder pattern with
 * reasonable defaults for those and bit rate.
 */
@TargetApi(17)
public class EncoderConfig {
    public final String mOutputFilePath;
    public final int mWidth;
    public final int mHeight;
    public final int mBitRate;
    EGLContext mEglContext;

    public EncoderConfig(String outputFilePath, int width, int height, int bitRate) {
        mOutputFilePath = outputFilePath;
        mWidth = width;
        mHeight = height;
        mBitRate = bitRate;
    }

    public void updateEglContext(EGLContext context) {
        mEglContext = context;
    }

    //@Override public String toString() {
    //    return "EncoderConfig: " + mWidth + "x" + mHeight + " @" + mBitRate +
    //            " to '" + mOutputFilePath.toString() + "' ctxt=" + mEglContext;
    //}
}

