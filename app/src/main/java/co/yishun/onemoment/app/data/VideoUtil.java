package co.yishun.onemoment.app.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import co.yishun.onemoment.app.api.model.QiniuKeyProvider;

/**
 * Created by Carlos on 2015/8/22.
 */
public class VideoUtil {
    private static final String TAG = "VideoUtil";

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
        Log.i(TAG, "create thumb image: " + thumbFile.getPath());
        if (thumbFile.exists()) thumbFile.delete();
        FileOutputStream fOut = new FileOutputStream(thumbFile);
        Bitmap bitmap = ThumbnailUtils.createVideoThumbnail(videoPath, kind);
        bitmap.compress(Bitmap.CompressFormat.PNG, 85, fOut);
        fOut.flush();
        fOut.close();
        return thumbFile.getPath();
    }


}
