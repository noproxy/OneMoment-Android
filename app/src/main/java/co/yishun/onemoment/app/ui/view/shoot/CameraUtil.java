package co.yishun.onemoment.app.ui.view.shoot;

import android.graphics.Matrix;
import android.hardware.Camera;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.TextureView;

import java.util.List;

/**
 * Created by Carlos on 2015/10/6.
 */
public class CameraUtil {

    private static final String TAG = "CameraUtil";

    /**
     * Iterate over supported camera preview sizes to see which one best fits the
     * dimensions of the given view while maintaining the aspect ratio. If none can,
     * be lenient with the aspect ratio.
     *
     * @param sizes        Supported camera preview sizes.
     * @param targetWidth  The width of the view.
     * @param targetHeight The height of the view.
     * @return Best match camera preview size to fit in the view.
     */
    public static Camera.Size getOptimalPreviewSize(@NonNull List<Camera.Size> sizes, int targetWidth, int targetHeight) {
        Camera.Size optimalSize = null;
//        double minHeightDiff = Double.MAX_VALUE;
//        double minWidthDiff = Double.MAX_VALUE;

        double ratio = ((double) targetWidth) / targetHeight;
        Log.i(TAG, "target height: " + targetHeight + ", target ratio: " + ratio);
        double minRatioDiff = Double.MAX_VALUE;

        for (Camera.Size size : sizes) {
            if (size.height < targetHeight || size.width < targetWidth) continue;
            //select whose ratio is the most close
            double sizeRatio = ((float) size.width) / size.height;
            double ratioDiff = Math.abs(sizeRatio - ratio);
            if (ratioDiff < minRatioDiff) {
                minRatioDiff = ratioDiff;
                optimalSize = size;
            } else if (ratioDiff == minRatioDiff && size.height > targetHeight && size.height < optimalSize.height) {
                //if ratio is the same, select whose height is the most close
                optimalSize = size;
            }
            Log.v("iter height", "width: " + size.width + ", height: " + size.height + ", ratio: " + sizeRatio);
        }
        Log.i("selected size", "width: " + optimalSize.width + ", height: " + optimalSize.height);
        return optimalSize;
    }

    public static int[] getOptimalPreviewFpsRange(List<int[]> ranges, int targetFps) {
        int[] optimalRange = new int[]{0, Integer.MAX_VALUE};
        int averageDiff = Integer.MAX_VALUE;// ignore decimal
        for (int[] range : ranges) {
            int a = (range[0] + range[1]) / 2;
            if (a - targetFps < averageDiff
                    || (a == averageDiff && range[1] - targetFps < optimalRange[1] - targetFps))
                averageDiff = a;
            optimalRange = range;
        }
        return optimalRange;
    }

    public static Matrix calculatePreviewMatrix(@NonNull final TextureView previewView, @NonNull final Camera.Size size) {
        Matrix mat = new Matrix();

        int viewWidth = previewView.getWidth();
        int viewHeight = previewView.getHeight();
        if (viewHeight != viewWidth)
            Log.w(TAG, "preview view width not equals height, width is " + viewWidth + ", height is " + viewHeight);
        else Log.v(TAG, "view width: " + viewWidth);

        //assert rotation == 90, so width will be height
        float a = ((float) viewWidth) / size.height;
        float b = ((float) viewHeight) / size.width;
        float scaleY = a / b;
//        float scaleX = 1;
        mat.setScale(1, scaleY);

        //move to center
        mat.postTranslate(0, -viewHeight * (scaleY - 1) / 2);
//        Toast.makeText(this, "scaleX " + scaleX + "; scaleY " + scaleY, Toast.LENGTH_LONG).show();
        return mat;
    }

    /**
     * @return a integer pair of back camera id and front camera id.
     */
    public static CameraId findCameraId() {
        int backId = -1, frontId = -1;
        // Find the total number of cameras available
        int mNumberOfCameras = Camera.getNumberOfCameras();
        // Find the ID of the back-facing ("default") camera
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int i = 0; i < mNumberOfCameras; i++) {
            Camera.getCameraInfo(i, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                backId = i;
            } else if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                frontId = i;
            }
        }
        return CameraId.create(backId, frontId);
    }
}
