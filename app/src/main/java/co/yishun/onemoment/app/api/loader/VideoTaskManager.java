package co.yishun.onemoment.app.api.loader;

import android.os.AsyncTask;
import android.util.Log;

import com.squareup.okhttp.OkHttpClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import co.yishun.onemoment.app.api.model.Video;
import java8.util.stream.StreamSupport;

/**
 * Created by Jinge on 2015/11/13.
 */
public class VideoTaskManager {
    public static final OkHttpClient httpClient = new OkHttpClient();
    private static final String TAG = "VideoTaskManager";

    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;
    private static final BlockingQueue<Runnable> poolQueue =
            new LinkedBlockingQueue<Runnable>(128);
    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "VideoTaskManager #" + mCount.getAndIncrement());
        }
    };
    public static final Executor executor = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, poolQueue, sThreadFactory);

    private static VideoTaskManager instance;
    private List<AsyncTask> asyncTaskList = new ArrayList<>();
    private List<TaskEntry<LoaderTask, Video[]>> taskMap = new ArrayList<>();

    public static VideoTaskManager getInstance() {
        synchronized (VideoTaskManager.class) {
            if (instance == null) {
                instance = new VideoTaskManager();
            }
        }
        return instance;
    }

    public void executeTask(LoaderTask task, Video... params) {
        asyncTaskList.add(task);
        Log.d(TAG, asyncTaskList.size() + "");

        if (poolQueue.size() >= 96) {
            Log.d(TAG, "pool size over");
//            taskMap.put(task, params);
            taskMap.add(new TaskEntry<>(task, params));
        } else {
            task.executeOnExecutor(executor, params);
        }
    }

    public void removeTask(LoaderTask task) {
        asyncTaskList.remove(task);
        Log.d(TAG, asyncTaskList.size() + "");
        for (int i = 0; i < taskMap.size(); ) {
            if (taskMap.get(i).key == task) {
                taskMap.remove(i);
                break;
            } else i++;
        }
        if (poolQueue.size() < 96) {
            Log.d(TAG, "pool size ok");
            if (taskMap.size() > 0) {
                TaskEntry<LoaderTask, Video[]> e = taskMap.get(0);
                taskMap.remove(0);
                executeTask(e.getKey(), e.getValue());
            }
        }
    }

//    public void addTask(AsyncTask task) {
//        asyncTaskList.add(task);
//        Log.d(TAG, asyncTaskList.size() + "");
//        if (asyncTaskList.size() >= 96) {
//            Log.e(TAG, "size error");
//        }
//    }
//
//    public void removeTask(AsyncTask task) {
//        asyncTaskList.remove(task);
//        Log.d(TAG, asyncTaskList.size() + "");
//    }

    public void quit() {
        for (AsyncTask asyncTask : asyncTaskList) {
            asyncTask.cancel(false);
        }
        asyncTaskList.clear();
    }

    class TaskEntry<K, V> {
        public K key;
        public V value;

        public TaskEntry(K key, V value) {
            this.key = key;
            this.value = value;
        }

        public boolean equals(Object object) {
            if (!(object instanceof TaskEntry)) {
                return false;
            }
            TaskEntry e = (TaskEntry) object;
            return value == e.getValue();
        }

        public K getKey() {
            return key;
        }

        public V getValue() {
            return value;
        }
    }
}
