package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.io.File;
import java.io.IOException;

import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;

/**
 * Created by Jinge on 2015/11/18.
 */
public class VideoImageTask extends AsyncTask<Video, Integer, Boolean> {
    private static final String TAG = "VideoImageTask";
    private Context context;
    private String largeThumbImage;
    private String thumbImage;

    public VideoImageTask(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(Video... videos) {
        final Video video = videos[0];
        File videoFile = FileUtil.getWorldVideoStoreFile(context, video);
        File large = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.LARGE_THUMB);
        File small = FileUtil.getThumbnailStoreFile(context, video, FileUtil.Type.MICRO_THUMB);
        try {
            if (large.exists()) {
                largeThumbImage = large.getPath();
                Log.d(TAG, "large exist " + largeThumbImage);
            } else {
                largeThumbImage = VideoUtil.createLargeThumbImage(context, video, videoFile.getPath());
                Log.d(TAG, "large not exist " + largeThumbImage);
            }
            if (small.exists()) {
                thumbImage = small.getPath();
                Log.d(TAG, "small exist " + thumbImage);
            } else {
                thumbImage = VideoUtil.createThumbImage(context, video, videoFile.getPath());
                Log.d(TAG, "small not exist " + thumbImage);
            }
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        super.onPostExecute(aBoolean);
    }
}
