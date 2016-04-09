package co.yishun.onemoment.app.api.loader;

import android.content.Context;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;

/**
 * Created by Jinge on 2015/11/18.
 */
public class VideoImageTask extends LoaderTask {
    private static final String TAG = "VideoImageTask";
    private Context context;
    private File large;
    private File small;
    private boolean getLarge;
    private boolean getSmall;
    private WeakReference<VideoTask> videoTaskReference;

    public VideoImageTask(Context context, VideoTask videoTask) {
        this.context = context;
        this.videoTaskReference = new WeakReference<>(videoTask);
    }

    @Override
    protected Boolean doInBackground(VideoProvider... videos) {
        final VideoProvider video = videos[0];
        LogUtil.d(TAG, "start image " + video.getFilename() + " " + this.toString());
        File videoFile = FileUtil.getWorldVideoStoreFile(context, video);
        large = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.LARGE_THUMB);
        small = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.MICRO_THUMB);
        try {
            // there is some reason lead to thumb created fail, it depends. So try 3 times, make it less error
            if (videoFile.length() == 0) {
                LogUtil.e(TAG, "video not found " + videoFile.getName());
                return false;
            }
            for (int i = 0; i < 3; i++) {
                if (small.length() > 0) break;
                VideoUtil.createThumbs(videoFile.getPath(), large, small);
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
            LogUtil.d(TAG, "stop image");
            if (videoTaskReference.get() != null) {
                videoTaskReference.get().getImage(large, small);
            }
        } else {
            onCancelled(false);
            LogUtil.e(TAG, "error");
        }
        VideoTaskManager.getInstance().removeTask(this);
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        LogUtil.d(TAG, "cancel image " + result + " " + this.toString());
        if (result == null || !result) {
            if (large != null && !getLarge && large.exists()) large.delete();
        }
        VideoTaskManager.getInstance().removeTask(this);
    }
}
