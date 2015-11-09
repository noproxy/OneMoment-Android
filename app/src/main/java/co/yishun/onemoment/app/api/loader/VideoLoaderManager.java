package co.yishun.onemoment.app.api.loader;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.os.Bundle;

import com.squareup.picasso.Picasso;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import co.yishun.onemoment.app.api.model.TagVideo;

/**
 * Created by Jinge on 2015/11/9.
 */
public class VideoLoaderManager implements LoaderManager.LoaderCallbacks<Boolean> {
    LoaderManager manager;
    Queue<VideoLoaderContent> t = new ConcurrentLinkedQueue<>();
    Context mContext;

    VideoLoaderManager() {

    }

    private void create() {
    }

    @Override
    public Loader<Boolean> onCreateLoader(int id, Bundle args) {
        return new VideoTaskLoader(mContext);
    }

    @Override
    public void onLoadFinished(Loader<Boolean> loader, Boolean data) {
        VideoTaskLoader l = (VideoTaskLoader)loader;
        Picasso.with(mContext).load(l.tagVideo.avatar).into(l.target.get());
    }

    @Override
    public void onLoaderReset(Loader<Boolean> loader) {

    }

}
