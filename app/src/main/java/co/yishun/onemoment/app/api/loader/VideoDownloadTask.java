package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.okhttp.Call;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.data.FileUtil;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoDownloadTask extends AsyncTask<Video, Integer, Boolean> {
    private static final String TAG = "VideoDownloadTask";
    private String largeThumbImage;
    private String thumbImage;
    private Context mContext;
    private WeakReference<ImageView> mTargetImageView;
    private VideoDownloadListener mListener;

    public VideoDownloadTask(Context context) {
        mContext = context;
    }

    public VideoDownloadTask(Context context, VideoDownloadListener listener) {
        mContext = context;
        mListener = listener;
    }

    public void setListener(VideoDownloadListener listener) {
        mListener = listener;
    }

    public void setImageView(ImageView imageView) {
        if (mTargetImageView != null) {
            mTargetImageView.clear();
        }
        mTargetImageView = new WeakReference<>(imageView);
    }

    @Override
    protected Boolean doInBackground(Video... videos) {
        final Video video = videos[0];
        Log.d(TAG, "start " + video.fileName);
        // if video exists
        File fileSynced = FileUtil.getWorldVideoStoreFile(mContext, video);
        OkHttpClient httpClient = VideoTaskManager.httpClient;
        Call call = httpClient.newCall(new Request.Builder().url(video.domain + video.fileName).get().build());
        Response response = null;
        InputStream input = null;
        FileOutputStream output = null;
        try {
            response = call.execute();
            Log.d(TAG, "start net " + video.fileName);
            if (response.code() == 200) {
                input = response.body().byteStream();
                output = new FileOutputStream(fileSynced);
                long fileLength = response.body().contentLength();

                byte data[] = new byte[4096];
                long total = 0;
                int count;
                Log.d(TAG, "start while " + video.fileName);
                while ((count = input.read(data)) != -1) {
                    if (isCancelled()) {
                        input.close();
                        if (fileSynced.exists()) {
                            fileSynced.delete();
                        }
                        return false;
                    }
                    total += count;
                    output.write(data, 0, count);
                }
                Log.d(TAG, "end while " + video.fileName);
                return total == fileLength;

            } else {
                return false;
            }
        } catch (IOException ignore) {
            if (fileSynced.exists()) {
                fileSynced.delete();
            }
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

    }

    public interface VideoDownloadListener {
        void onDownloadOver(Video tagVideo, File fileSynced);
    }

}