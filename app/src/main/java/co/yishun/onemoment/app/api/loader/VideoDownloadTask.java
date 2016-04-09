package co.yishun.onemoment.app.api.loader;

import android.content.Context;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoDownloadTask extends LoaderTask {
    private static final String TAG = "VideoDownloadTask";
    private Context mContext;
    private File cacheVideoFile;
    private File videoFile;
    private VideoProvider video;
    private WeakReference<VideoTask> videoTaskReference;

    public VideoDownloadTask(Context context) {
        mContext = context;
    }

    public VideoDownloadTask(Context context, VideoTask videoTask) {
        mContext = context;
        videoTaskReference = new WeakReference<>(videoTask);
    }

    @Override
    protected Boolean doInBackground(VideoProvider... videos) {
        video = videos[0];
        LogUtil.d(TAG, "start video " + this + "  " + video.getFilename() + "  " + video.getDownloadUrl());
        cacheVideoFile = FileUtil.getCacheFile(mContext, video.getFilename());
        videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);

        OkHttpClient httpClient = VideoTaskManager.httpClient;

        Call call = httpClient.newCall(new Request.Builder().url(video.getDownloadUrl()).get().build());
        Response response;
        InputStream input = null;
        FileOutputStream output = null;
        try {
            response = call.execute();
            LogUtil.d(TAG, "start net " + video.getFilename() + " " + this.toString());
            if (response.code() == 200) {
                input = response.body().byteStream();
                long fileLength = response.body().contentLength();

                if (fileLength == 0) {
                    LogUtil.e(TAG, "error file length " + this.toString());
                    return false;
                } else if (videoFile.length() == fileLength) {
                    return true;
                }

                output = new FileOutputStream(cacheVideoFile);
                //OkHttp can't read more than 2048 bytes at a time.
                byte data[] = new byte[2048];
                long total = 0;
                int count;
                LogUtil.d(TAG, "start while " + fileLength + " " + video.getFilename() + " " + this.toString());
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        LogUtil.d(TAG, "cancel " + video.getFilename() + " " + this.toString());
                        input.close();
                        return false;
                    }
                    total += count;
                    output.write(data, 0, count);
                }
                LogUtil.d(TAG, "end while " + video.getFilename() + " " + cacheVideoFile.length() + " " + fileLength + " " + this.toString());

                return total == fileLength;
            } else {
                LogUtil.e(TAG, "net error");
                return false;
            }
        } catch (IOException e) {
            LogUtil.e(TAG, this.toString() + "  io exception");
            e.printStackTrace();
            return false;
        } finally {
            try {
                if (output != null)
                    output.close();
                if (input != null)
                    input.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        if (result) {
            LogUtil.i(TAG, "stop video" + " " + this.toString());
            if (cacheVideoFile.length() > 0)
                cacheVideoFile.renameTo(videoFile);
            if (videoTaskReference.get() != null) {
                videoTaskReference.get().getVideo(video);
            }
        } else {
            LogUtil.e(TAG, "error video " + result + " " + this.toString());
        }
        if (cacheVideoFile != null && cacheVideoFile.exists()) {
            cacheVideoFile.delete();
        }
        VideoTaskManager.getInstance().removeTask(this);
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        LogUtil.d(TAG, "cancel video " + result + " " + this.toString());
        if (cacheVideoFile != null && cacheVideoFile.exists() && (result == null || !result)) {
            cacheVideoFile.delete();
        }
        VideoTaskManager.getInstance().removeTask(this);
    }
}