package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.api.model.TagVideo;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoTaskManager {
    private static final String TAG = "VideoTaskManager";
    private static VideoTaskManager instance;
    private List<AsyncTask> asyncTaskList;
    private Context mContext;

    public static VideoTaskManager getInstance() {
        synchronized (VideoTaskManager.class) {
            if (instance == null) {
                instance = new VideoTaskManager();
            }
        }
        return instance;
    }

    public void init(Context context) {
        mContext = context;
        asyncTaskList = new ArrayList<>();
    }

    public VideoDownload addDownloadTask(VideoDownload task, TagVideo... params) {
        if (asyncTaskList.contains(task)) {
//            if (task.getStatus() != AsyncTask.Status.PENDING) {
            task.cancel(true);
            removeTask(task);
            task = new VideoDownload(mContext);
            addTask(task);
//            }
        }
        if (task == null || task.getStatus() == AsyncTask.Status.FINISHED) {
            task = new VideoDownload(mContext);
            addTask(task);
        }
        task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params[0]);
        return task;
    }

    public void addTask(AsyncTask task) {
        asyncTaskList.add(task);
    }

    public void removeTask(AsyncTask task) {
        asyncTaskList.remove(task);
    }

    public void quit() {
        if (mContext != null) {
            mContext = null;
            for (AsyncTask asyncTask : asyncTaskList) {
                asyncTask.cancel(true);
            }
            asyncTaskList.clear();
        }
    }
}
