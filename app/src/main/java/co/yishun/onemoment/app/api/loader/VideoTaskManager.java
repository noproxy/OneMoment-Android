package co.yishun.onemoment.app.api.loader;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoTaskManager {
    public static final OkHttpClient httpClient = new OkHttpClient();
    private static final String TAG = "VideoTaskManager";
    private static VideoTaskManager instance;
    private List<AsyncTask> asyncTaskList = new ArrayList<>();

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
        Log.d(TAG, asyncTaskList.size() + "");
        if (asyncTaskList.size() >= 96) {
            Log.e(TAG, "size error");
        }
    }

    public void removeTask(AsyncTask task) {
        asyncTaskList.remove(task);
        Log.d(TAG, asyncTaskList.size() + "");
    }

    public void quit() {
        for (AsyncTask asyncTask : asyncTaskList) {
            asyncTask.cancel(false);
        }
        asyncTaskList.clear();
    }
}
