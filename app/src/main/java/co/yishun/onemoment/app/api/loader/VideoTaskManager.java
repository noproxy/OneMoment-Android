package co.yishun.onemoment.app.api.loader;

import android.content.Context;
import android.os.AsyncTask;
import android.support.annotation.Nullable;

import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.api.model.TagVideo;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoTaskManager {
    public static final OkHttpClient httpClient = new OkHttpClient();
    private static final String TAG = "VideoTaskManager";
    private static VideoTaskManager instance;
    private List<AsyncTask> asyncTaskList  = new ArrayList<>();

    public static VideoTaskManager getInstance() {
        synchronized (VideoTaskManager.class) {
            if (instance == null) {
                instance = new VideoTaskManager();
            }
        }
        return instance;
    }

    public void addTask(AsyncTask task) {
        asyncTaskList.add(task);
    }

    public void removeTask(AsyncTask task) {
        asyncTaskList.remove(task);
    }

    public void quit() {
        for (AsyncTask asyncTask : asyncTaskList) {
            asyncTask.cancel(false);
        }
        asyncTaskList.clear();
    }
}
