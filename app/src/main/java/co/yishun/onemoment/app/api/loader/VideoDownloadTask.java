package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.util.Log;

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
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoDownloadTask extends LoaderTask {
    private static final String TAG = "VideoDownloadTask";
    private Context mContext;
    private File videoFile;
    private Video video;
    private WeakReference<VideoTask> videoTaskReference;

    public VideoDownloadTask(Context context) {
        mContext = context;
    }

    public VideoDownloadTask(Context context, VideoTask videoTask) {
        mContext = context;
        videoTaskReference = new WeakReference<>(videoTask);
    }

    @Override
    protected Boolean doInBackground(Video... videos) {
        video = videos[0];
        LogUtil.d(TAG, "start video " + video.fileName);
        videoFile = FileUtil.getWorldVideoStoreFile(mContext, video);

        OkHttpClient httpClient = VideoTaskManager.httpClient;
        Call call = httpClient.newCall(new Request.Builder().url(video.domain + video.fileName).get().build());
        Response response;
        InputStream input = null;
        FileOutputStream output = null;
        try {
            response = call.execute();
            LogUtil.d(TAG, "start net " + video.fileName + " " + this.toString());
            if (response.code() == 200) {
                input = response.body().byteStream();
                output = new FileOutputStream(videoFile);
                long fileLength = response.body().contentLength();
                if (fileLength == 0) {
                    LogUtil.e(TAG, "error file length " + this.toString());
                    return false;
                } else if (videoFile.length() == fileLength){
                    return true;
                }

                //OkHttp can't read more than 2048 bytes at a time.
                byte data[] = new byte[2048];
                long total = 0;
                int count;
                LogUtil.d(TAG, "start while " + fileLength + " " + video.fileName + " " + this.toString());
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        LogUtil.d(TAG, "cancel " + video.fileName + " " + this.toString());
                        input.close();
                        return false;
                    }
                    total += count;
                    output.write(data, 0, count);
                }
                LogUtil.d(TAG, "end while " + video.fileName + " " + videoFile.length() + " " + fileLength + " " + this.toString());

                return total == fileLength;
            } else {
                return false;
            }
        } catch (IOException ignore) {
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
            LogUtil.d(TAG, "stop video" + " " + this.toString());
            if (videoTaskReference.get() != null) {
                videoTaskReference.get().getVideo(video);
            }
        } else {
            LogUtil.d(TAG, "error video " + result + " " + this.toString());
            if (videoFile != null && videoFile.exists()) {
                videoFile.delete();
            }
        }
        VideoTaskManager.getInstance().removeTask(this);
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        LogUtil.d(TAG, "cancel video " + result + " " + this.toString());
        if (videoFile != null && videoFile.exists() && (result == null || !result)) {
            videoFile.delete();
        }
        VideoTaskManager.getInstance().removeTask(this);
    }
}