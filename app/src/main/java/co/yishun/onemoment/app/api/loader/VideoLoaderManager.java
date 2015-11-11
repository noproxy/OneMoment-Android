package co.yishun.onemoment.app.api.loader;

import android.app.Activity;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.ui.common.BaseFragment;

/**
 * Created by Jinge on 2015/11/9.
 */
public class VideoLoaderManager implements LoaderManager.LoaderCallbacks<Boolean> {
    private static final String TAG = "VideoLoaderManager";
    LoaderManager manager;
    Queue<VideoTask> taskQueue = new ConcurrentLinkedQueue<>();
    Context mContext;
    private int taskCapacity = 4;
    private static VideoLoaderManager instance;

    public static VideoLoaderManager getInstance() {
        synchronized (VideoLoaderManager.class) {
            if (instance == null) {
                instance = new VideoLoaderManager();
            }
        }
        return instance;
    }

    public void init(Object host) {
        if (host instanceof Activity) {
            mContext = (Activity)host;
            manager = ((Activity)host).getLoaderManager();
        } else if (host instanceof Fragment) {
            mContext = ((BaseFragment)host).getActivity();
            manager = ((BaseFragment)host).getLoaderManager();
        }
    }

    public VideoTask createTask(TagVideo tagVideo, ImageView target) {
        VideoTask task = new VideoTask(tagVideo, target);
        taskQueue.add(task);
        createLoader();
        return task;
    }

    public void createLoader() {
        for (int i = 0; i < taskCapacity; i++) {
            if (manager.getLoader(i) == null || !manager.getLoader(i).isStarted()) {
                Log.d(TAG, "try to create " + i);
                manager.initLoader(i, null, this);
            }
        }
    }

    public void stop() {

    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "create loader " + id);
        VideoTask task = taskQueue.poll();
        VideoLoader loader = new VideoLoader(mContext, task);
        task.setLoader(loader);
        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
        VideoLoader l = (VideoLoader) loader;
        Log.d(TAG, "create finish " + l.getThumbImage());
        Picasso.with(mContext).load(new File(l.getThumbImage())).into(l.getTask().getTarget().get());
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {
        VideoLoader l = (VideoLoader) loader;
        l.setTask(taskQueue.poll());
        l.startLoading();
        Log.d(TAG, "create reset ");
    }

}
