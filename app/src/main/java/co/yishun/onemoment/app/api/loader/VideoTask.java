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
    public static final int TYPE_IAMGE_ONLY = 2;
    private static final String TAG = "VideoTask";

    private Context context;
    private Video video;
    private int type;
    private OnVideoListener videoListener;
    private OnImageListener imageListener;
    private VideoDownloadTask downloadTask;
    private VideoImageTask imageTask;

    public VideoTask(Context context, Video video, int type) {
        this.video = video;
        this.context = context;
        this.type = type;
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
        File videoFile = FileUtil.getWorldVideoStoreFile(context, video);
        //the file exist, but size is 0.
        if (videoFile.length() != 0) {
            Log.d(TAG, "video file exist " + video.fileName);
            getVideo(video);
        } else {
            Log.d(TAG, "video file not exist " + video.fileName);
            downloadTask = new VideoDownloadTask(context, this);
            VideoTaskManager.getInstance().executeTask(downloadTask, video);
        }
        return this;
    }

    public VideoTask startForce() {
        //the file exists, but is error.
        File videoFile = FileUtil.getWorldVideoStoreFile(context, video);
        File large = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.LARGE_THUMB);
        videoFile.delete();
        large.delete();

        return start();
    }

    public void cancel() {
        Log.d(TAG, "cancel task");
        if (downloadTask != null) {
            downloadTask.cancel(true);
        }
        if (imageTask != null) {
            imageTask.cancel(true);
        }
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

        if (large.length() != 0) {
            getImage(large);
        } else {
            imageTask = new VideoImageTask(context, this);
            VideoTaskManager.getInstance().executeTask(imageTask, video);
//            imageTask.executeOnExecutor(VideoTaskManager.executor, video);
        }
    }

    void getImage(File large) {
        Log.d(TAG, "get image");
        if (imageListener != null) {
            imageListener.onImageCreate(large);
        } else {
            Log.e(TAG, "image listener null");
        }
    }

    public interface OnVideoListener {
        void onVideoLoad(Video video);
    }

    public interface OnImageListener {
        void onImageCreate(File large);
    }
}
