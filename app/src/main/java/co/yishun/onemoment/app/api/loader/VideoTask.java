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
    private OnVideoListener videoListener;
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
            getVideo(video);
        } else {
            Log.d(TAG, "video file not exist " + video.fileName);
            VideoDownloadTask downloadTask = new VideoDownloadTask(context, this);
            downloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, video);
        }
    }

    public VideoTask setVideoListener(OnVideoListener listener) {
        videoListener = listener;
        return this;
    }

    public VideoTask setImageListener(OnImageListener listener) {
        imageListener = listener;
        return this;
    }

    public VideoTask start() {
        createWorker();
        return this;
    }

    void getVideo(Video video) {
        if (videoListener != null) {
            videoListener.onVideoLoad(video);
        } else {
            Log.e(TAG, "video listener null");
        }
        if (type == TYPE_VIDEO_ONLY) {
            return;
        }
        // check whether thumbnail exists
        Log.d(TAG, "try to get image");
        File large = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.LARGE_THUMB);
        File small = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.MICRO_THUMB);
        if (large.exists() && small.exists()) {
            getImage(large, small);
        } else {
            VideoImageTask imageTask = new VideoImageTask(context, this);
            imageTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, video);
        }
    }

    void getImage(File large, File small) {
        Log.d(TAG, "get image");
        if (imageListener != null) {
            imageListener.onImageCreate(large, small);
        } else {
            Log.e(TAG, "image listener null");
        }
    }

    public interface OnVideoListener {
        void onVideoLoad(Video video);
    }

    public interface OnImageListener {
        void onImageCreate(File large, File small);
    }
}
