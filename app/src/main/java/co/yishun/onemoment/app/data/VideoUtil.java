package co.yishun.onemoment.app.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.model.QiniuKeyProvider;

/**
 * Util to create thumbnail of a video.
 * <p>
 * Created by Carlos on 2015/8/22.
 */
public class VideoUtil {
    public static final int OPTIONS_RECYCLE_INPUT = 0x2;
    public static final int TARGET_SIZE_MICRO_THUMBNAIL = 192;
    private static final String TAG = "VideoUtil";
    private static final int OPTIONS_SCALE_UP = 0x1;

    /**
     * Create full screen thumb of a video file. If thumb exists, it will be deleted.
     *
     * @return path to the thumb
     * @throws IOException
     */
    public static String createLargeThumbImage(Context context, QiniuKeyProvider provider, String videoPath) throws IOException {
        return createThumbImage(context, videoPath, provider, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
    }

    /**
     * Create micro thumb of a video file. If thumb exists, it will be deleted.
     *
     * @return path to the thumb
     * @throws IOException
     */
    public static String createThumbImage(Context context, QiniuKeyProvider provider, String videoPath) throws IOException {
        return createThumbImage(context, videoPath, provider, MediaStore.Images.Thumbnails.MICRO_KIND);
    }

    /**
     * Create target kind thumb of a video file. If thumb exists, it will be deleted.
     *
     * @return path to the thumb
     * @throws IOException
     */
    private static String createThumbImage(Context context, String videoPath, QiniuKeyProvider provider, int kind) throws IOException {
        File thumbFile = FileUtil.getThumbnailStoreFile(context, provider, kind == MediaStore.Images.Thumbnails.FULL_SCREEN_KIND ? FileUtil.Type.LARGE_THUMB : FileUtil.Type.MICRO_THUMB);
        LogUtil.i(TAG, "create thumb image: " + thumbFile.getPath());
        if (thumbFile.exists()) thumbFile.delete();
        FileOutputStream fOut = new FileOutputStream(thumbFile);
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        } else {
            //TODO some video cannot get thumbnail
            LogUtil.e(TAG, "video: " + videoPath + ", create thumbnail failed.");
        }
        fOut.flush();
        fOut.close();
        return thumbFile.getPath();
    }

    public static void createThumbs(String videoPath, File large, File small) throws IOException {
        if (large.length() == 0) large.delete();
        if (small.length() == 0) small.delete();

        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        retriever.setDataSource(videoPath);
        FileOutputStream largeFOS = new FileOutputStream(large);
        Bitmap bitmap = retriever.getFrameAtTime(0);
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, largeFOS);
        } else {
            LogUtil.e(TAG, "video: " + videoPath + ", create thumbnail failed.");
        }
        largeFOS.flush();
        largeFOS.close();

        FileOutputStream smallFOS = new FileOutputStream(small);
        bitmap = extractThumbnail(bitmap,
                TARGET_SIZE_MICRO_THUMBNAIL,
                TARGET_SIZE_MICRO_THUMBNAIL,
                OPTIONS_RECYCLE_INPUT);
        if (bitmap != null) {
            bitmap.compress(Bitmap.CompressFormat.PNG, 85, smallFOS);
        } else {
            LogUtil.e(TAG, "video: " + videoPath + ", create thumbnail failed.");
        }
        smallFOS.flush();
        smallFOS.close();

        retriever.release();
    }

    /**
     * Creates a centered bitmap of the desired size.
     *
     * @param source  original bitmap source
     * @param width   targeted width
     * @param height  targeted height
     * @param options options used during thumbnail extraction
     */
    public static Bitmap extractThumbnail(
            Bitmap source, int width, int height, int options) {
        if (source == null) {
            return null;
        }

        float scale;
        if (source.getWidth() < source.getHeight()) {
            scale = width / (float) source.getWidth();
        } else {
            scale = height / (float) source.getHeight();
        }
        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        Bitmap thumbnail = transform(matrix, source, width, height,
                OPTIONS_SCALE_UP | options);
        return thumbnail;
    }

    /**
     * Transform source Bitmap to targeted width and height.
     */
    private static Bitmap transform(Matrix scaler,
                                    Bitmap source,
                                    int targetWidth,
                                    int targetHeight,
                                    int options) {
        boolean scaleUp = (options & OPTIONS_SCALE_UP) != 0;
        boolean recycle = (options & OPTIONS_RECYCLE_INPUT) != 0;

        int deltaX = source.getWidth() - targetWidth;
        int deltaY = source.getHeight() - targetHeight;
        if (!scaleUp && (deltaX < 0 || deltaY < 0)) {
            /*
            * In this case the bitmap is smaller, at least in one dimension,
            * than the target.  Transform it by placing as much of the image
            * as possible into the target and leaving the top/bottom or
            * left/right (or both) black.
            */
            Bitmap b2 = Bitmap.createBitmap(targetWidth, targetHeight,
                    Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(b2);

            int deltaXHalf = Math.max(0, deltaX / 2);
            int deltaYHalf = Math.max(0, deltaY / 2);
            Rect src = new Rect(
                    deltaXHalf,
                    deltaYHalf,
                    deltaXHalf + Math.min(targetWidth, source.getWidth()),
                    deltaYHalf + Math.min(targetHeight, source.getHeight()));
            int dstX = (targetWidth - src.width()) / 2;
            int dstY = (targetHeight - src.height()) / 2;
            Rect dst = new Rect(
                    dstX,
                    dstY,
                    targetWidth - dstX,
                    targetHeight - dstY);
            c.drawBitmap(source, src, dst, null);
            if (recycle) {
                source.recycle();
            }
            c.setBitmap(null);
            return b2;
        }
        float bitmapWidthF = source.getWidth();
        float bitmapHeightF = source.getHeight();

        float bitmapAspect = bitmapWidthF / bitmapHeightF;
        float viewAspect = (float) targetWidth / targetHeight;

        if (bitmapAspect > viewAspect) {
            float scale = targetHeight / bitmapHeightF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        } else {
            float scale = targetWidth / bitmapWidthF;
            if (scale < .9F || scale > 1F) {
                scaler.setScale(scale, scale);
            } else {
                scaler = null;
            }
        }

        Bitmap b1;
        if (scaler != null) {
            // this is used for minithumb and crop, so we want to filter here.
            b1 = Bitmap.createBitmap(source, 0, 0,
                    source.getWidth(), source.getHeight(), scaler, true);
        } else {
            b1 = source;
        }

        if (recycle && b1 != source) {
            source.recycle();
        }

        int dx1 = Math.max(0, b1.getWidth() - targetWidth);
        int dy1 = Math.max(0, b1.getHeight() - targetHeight);

        Bitmap b2 = Bitmap.createBitmap(
                b1,
                dx1 / 2,
                dy1 / 2,
                targetWidth,
                targetHeight);

        if (b2 != b1) {
            if (recycle || b1 != source) {
                b1.recycle();
            }
        }

        return b2;
    }

}
