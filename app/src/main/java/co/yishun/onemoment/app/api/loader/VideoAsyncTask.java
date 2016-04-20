package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.os.AsyncTask;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.model.VideoTag;
import co.yishun.onemoment.app.api.modelv4.VideoProvider;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.function.Consumer;

/**
 * Created by carlos on 4/18/16.
 */
public class VideoAsyncTask extends AsyncTask<VideoProvider, Integer, Boolean> {
    private static final String TAG = "VideoAsyncTask";
    private Context mContext;
    private File cacheVideoFile;
    private File videoFile;
    private WeakReference<Consumer<VideoProvider>> consumerWeakReference
            = new WeakReference<>(null);
    private VideoProvider mVideo;

    public VideoAsyncTask(Context context, Consumer<VideoProvider> consumer) {
        this.mContext = context;
        consumerWeakReference = new WeakReference<>(consumer);
    }

    public static boolean isInvalid(VideoProvider provider) {
        return provider instanceof DummyVideo && !((DummyVideo) provider).isEnd;
    }

    public static boolean isEnd(VideoProvider provider) {
        return provider instanceof DummyVideo && ((DummyVideo) provider).isEnd;
    }

    public static VideoAsyncTask endTask(Consumer<VideoProvider> consumer) {
        return new VideoAsyncTask(null, consumer);
    }

    @Override
    protected Boolean doInBackground(VideoProvider... params) {
        // if context null, this is dummy end task.
        if (mContext == null)
            return false;

        final VideoProvider video = params[0];
        mVideo = video;

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
        VideoProvider re = mVideo;


        if (result) {
            LogUtil.i(TAG, "stop video" + " " + this.toString());
            if (cacheVideoFile.length() > 0)
                cacheVideoFile.renameTo(videoFile);
        } else if (mContext != null) {
            re = DummyVideo.newInvalidVideo();
            LogUtil.e(TAG, "error video " + result + " " + this.toString());
        } else {
            // if context null, this is dummy end task.
            re = DummyVideo.newEndVideo();
        }

        Consumer<VideoProvider> consumer = consumerWeakReference.get();
        if (consumer != null) {
            consumer.accept(re);
        }

        if (cacheVideoFile != null && cacheVideoFile.exists()) {
            cacheVideoFile.delete();
        }
    }

    @Override
    protected void onCancelled(Boolean result) {
        super.onCancelled(result);
        LogUtil.d(TAG, "cancel video " + result + " " + this.toString());
        if (cacheVideoFile != null && cacheVideoFile.exists() && (result == null || !result)) {
            cacheVideoFile.delete();
        }
    }

    private static class DummyVideo implements VideoProvider {
        private boolean isEnd = false;

        private DummyVideo(boolean isEnd) {
            this.isEnd = isEnd;
        }

        private static DummyVideo newEndVideo() {
            return new DummyVideo(true);
        }

        private static DummyVideo newInvalidVideo() {
            return new DummyVideo(false);
        }

        @Override
        public String toString() {
            return super.toString() + ", isEnd: " + isEnd;
        }

        @Override
        public String getFilename() {
            return null;
        }

        @Override
        public String getDownloadUrl() {
            return null;
        }

        @Override
        public List<VideoTag> getTags() {
            return null;
        }

        @Override
        public String getAvatarUrl() {
            return null;
        }

        @Override
        public String getNickname() {
            return null;
        }

        @Override
        public String getKey() {
            return this.toString();
        }
    }
}
