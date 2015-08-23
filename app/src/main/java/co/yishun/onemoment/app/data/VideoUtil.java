package co.yishun.onemoment.app.data;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static co.yishun.onemoment.app.data.FileUtil.getOutputMediaFile;

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
    public static String createLargeThumbImage(Context context, String videoPath) throws IOException {
        return createThumbImage(context, videoPath, MediaStore.Images.Thumbnails.FULL_SCREEN_KIND);
    }

    /**
     * Create micro thumb of a video file. If thumb exists, it will be deleted.
     *
     * @return path to the thumb
     * @throws IOException
     */
    public static String createThumbImage(Context context, String videoPath) throws IOException {
        return createThumbImage(context, videoPath, MediaStore.Images.Thumbnails.MICRO_KIND);
    }

    /**
     * Create target kind thumb of a video file. If thumb exists, it will be deleted.
     *
     * @return path to the thumb
     * @throws IOException
     */
    private static String createThumbImage(Context context, String videoPath, int kind) throws IOException {
        File thumbFile = getOutputMediaFile(context, kind == MediaStore.Images.Thumbnails.FULL_SCREEN_KIND ? FileUtil.Type.LARGE_THUMB : FileUtil.Type.MICRO_THUMB, new File(videoPath));
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
