package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;

import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created by Jinge on 2015/11/18.
 */
public class VideoTask {
    public static final int TYPE_VIDEO_IMAGE = 0;
    public static final int TYPE_VIDEO_ONLY = 1;
    private static final String TAG = "VideoTask";

    private Context context;
    private Video video;
    private int type;
    private String largeThumbImage;
    private String thumbImage;
    private OnLoadedListener loadedListener;
    private OnImageListener imageListener;


    public VideoTask(Context context, Video video, int type) {
        this.video = video;
        this.context = context;
        this.type = type;
    }

    private void createWorker() {
        File videoFile = FileUtil.getWorldVideoStoreFile(context, video);
        if (videoFile.exists()) {
            Log.d(TAG, "video file exist " + video.fileName);
            loadedListener.onVideoLoad(video);
            if (type == TYPE_VIDEO_ONLY) {
                return;
            }
            // check whether thumbnail exists
            File large = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.LARGE_THUMB);
            File small = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.MICRO_THUMB);
            if (large.exists() && small.exists()) {
                imageListener.onImageCreate(large, small);
            } else {
                VideoImageTask imageTask = new VideoImageTask(context);
                imageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, video);
            }
        } else {
            VideoDownloadTask downloadTask = new VideoDownloadTask(context, null);
            downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, video);
        }
    }

    public interface OnLoadedListener {
        void onVideoLoad(Video video);
    }

    public interface OnImageListener {
        void onImageCreate(File large, File small);
    }
}
