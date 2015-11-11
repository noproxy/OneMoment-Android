package co.yishun.onemoment.app.api.loader;

import android.content.AsyncTaskLoader;
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

import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.VideoUtil;

/**
 * Created by Jinge on 2015/11/9.
 */
public class VideoLoader extends AsyncTaskLoader<Boolean> {
    private static final String TAG = "VideoLoader";
    private VideoTask mTask;
    private String largeThumbImage;
    private String thumbImage;
    private Context mContext;
    private boolean isCancelled;

    public VideoLoader(Context context, VideoTask task) {
        super(context);
        mContext = context;
        mTask = task;
    }

    @Override
    protected void onStartLoading() {
        super.onStartLoading();
        Log.d(TAG, "start");
    }

    @Override
    public Boolean loadInBackground() {
        Log.d(TAG, "back start");
        try {
            // if video exists
            File fileSynced = FileUtil.getWorldVideoStoreFile(mContext, mTask.getTagVideo());
            Log.d(TAG, "loader start");
            if (fileSynced.exists()) {
                Log.d(TAG, "file exist");
                // check whether thumbnail exists
                File large = FileUtil.getThumbnailStoreFile(mContext, mTask.getTagVideo(), FileUtil.Type.LARGE_THUMB);
                File small = FileUtil.getThumbnailStoreFile(mContext, mTask.getTagVideo(), FileUtil.Type.MICRO_THUMB);
                boolean re = true;
                try {
                    if (large.exists()) {
                        largeThumbImage = large.getPath();
                    } else {
                        largeThumbImage = VideoUtil.createLargeThumbImage(mContext, mTask.getTagVideo(), fileSynced.getPath());
                    }
                    if (small.exists()) {
                        thumbImage = small.getPath();
                    } else {
                        thumbImage = VideoUtil.createThumbImage(mContext, mTask.getTagVideo(), fileSynced.getPath());
                    }
                } catch (IOException e) {
                    // create thumbnail failed, video file may be damaged, redownload
                    Log.e(TAG, "IOException when create thumbImage of the old video");
                    e.printStackTrace();
                    if (!fileSynced.delete()) {
                        return false;
                    }
                    re = false;
                }
                if (re) return true;
            }
            Log.d(TAG, "file not exist");

            OkHttpClient httpClient = new OkHttpClient();
            Call call = httpClient.newCall(new Request.Builder().url(mTask.getTagVideo().domain + mTask.getTagVideo().fileName).get().build());
            Response response = call.execute();

            if (response.code() == 200) {
                InputStream input = null;
                FileOutputStream output = null;
                try {
                    input = response.body().byteStream();
                    output = new FileOutputStream(fileSynced);
                    long fileLength = response.body().contentLength();

                    byte data[] = new byte[4096];
                    long total = 0;
                    int count;
                    while ((count = input.read(data)) != -1) {
                        // allow canceling
                        if (isCancelled) {
                            input.close();
                            return false;
                        }
                        total += count;
                        // publishing the progress....
//                        if (fileLength > 0) // only if total length is known
//                            publishProgress((int) (total * 100 / fileLength));
                        output.write(data, 0, count);
                    }
                    largeThumbImage = VideoUtil.createLargeThumbImage(mContext, mTask.getTagVideo(), fileSynced.getPath());
                    thumbImage = VideoUtil.createThumbImage(mContext, mTask.getTagVideo(), fileSynced.getPath());

                    return total == fileLength;
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
            } else {
                return false;
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }

    public VideoTask getTask() {
        return mTask;
    }

    public void setTask(VideoTask mTask) {
        this.mTask = mTask;
    }

    public String getLargeThumbImage() {
        return largeThumbImage;
    }

    public String getThumbImage() {
        return thumbImage;
    }

    @Override
    protected boolean onCancelLoad() {
        isCancelled = true;
        return super.onCancelLoad();
    }
}

