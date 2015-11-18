package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;

/**
 * Created by Jinge on 2015/11/18.
 */
public class VideoImageTask extends AsyncTask<Video, Integer, Boolean> {
    private static final String TAG = "VideoImageTask";
    private Context context;
    private File large;
    private File small;
    private WeakReference<VideoTask> videoTaskReference;

    public VideoImageTask(Context context) {
        this.context = context;
    }

    public VideoImageTask(Context context, VideoTask videoTask) {
        this.context = context;
        this.videoTaskReference = new WeakReference<>(videoTask);
    }

    @Override
    protected Boolean doInBackground(Video... videos) {
        final Video video = videos[0];
        Log.d(TAG, "start image " + video.fileName);
        File videoFile = FileUtil.getWorldVideoStoreFile(context, video);
        large = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.LARGE_THUMB);
        small = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.MICRO_THUMB);
        try {
            if (!large.exists()) {
                Log.d(TAG, "create large " + video.fileName);
                VideoUtil.createLargeThumbImage(context, video, videoFile.getPath());
            }
            if (!small.exists()) {
                Log.d(TAG, "create small " + video.fileName);
                VideoUtil.createThumbImage(context, video, videoFile.getPath());
            }
            return large.exists() && small.exists();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            Log.d(TAG, "stop image");
            if (videoTaskReference.get() != null) {
                videoTaskReference.get().getImage(large, small);
            }
        } else {
            Log.e(TAG, "error");
        }
    }
}
