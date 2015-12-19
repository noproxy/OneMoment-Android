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
public class VideoImageTask extends LoaderTask {
    private static final String TAG = "VideoImageTask";
    private Context context;
    private File large;

    private boolean getLarge;

    private WeakReference<VideoTask> videoTaskReference;

    public VideoImageTask(Context context, VideoTask videoTask) {
        this.context = context;
        this.videoTaskReference = new WeakReference<>(videoTask);
    }

    @Override
    protected Boolean doInBackground(Video... videos) {
        final Video video = videos[0];
        Log.d(TAG, "start image " + video.fileName + " " + this.toString());
        File videoFile = FileUtil.getWorldVideoStoreFile(context, video);
        large = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.LARGE_THUMB);

        try {
            // there is some reason lead to thumb created fail, it depends. So try 3 times, make it less error
            for (int i = 0; i < 3; i++) {
                if (large.length() == 0) {
                    Log.d(TAG, "create large " + video.fileName + " " + this.toString());
                    VideoUtil.createLargeThumbImage(context, video, videoFile.getPath());
                    getLarge = true;
                }
                if (large.length() > 0) break;
            }
            //maybe the video file is error, download again.
            if (large.length() == 0){
                videoTaskReference.get().startForce();
                return false;
            }
            return large.exists();
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
                videoTaskReference.get().getImage(large);
            }
        } else {
            onCancelled(false);
            Log.e(TAG, "error");
        }
        VideoTaskManager.getInstance().removeTask(this);
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        Log.d(TAG, "cancel image " + result + " " + this.toString());
        if (result == null || !result) {
            if (large != null && !getLarge && large.exists()) large.delete();
        }
        VideoTaskManager.getInstance().removeTask(this);
    }
}
