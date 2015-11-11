package co.yishun.onemoment.app.api.loader;

import android.content.Loader;
import android.widget.ImageView;

import java.lang.ref.WeakReference;

import co.yishun.onemoment.app.api.model.TagVideo;

/**
 * Created by Jinge on 2015/11/9.
 */
public class VideoTask {
    TagVideo tagVideo;
    WeakReference<ImageView> target;
    Loader loader;

    public VideoTask(TagVideo tagVideo, ImageView imageView) {
        this.tagVideo = tagVideo;
        this.target = new WeakReference<ImageView>(imageView);
    }

    public void setLoader(Loader loader) {
        this.loader = loader;
    }

    boolean isLoading() {
        loader.cancelLoad();
        return loader.isStarted();
    }

    public void cancel() {
        if (loader != null) {
            loader.cancelLoad();
        }
    }

    public TagVideo getTagVideo() {
        return tagVideo;
    }

    public void setTagVideo(TagVideo tagVideo) {
        this.tagVideo = tagVideo;
    }

    public WeakReference<ImageView> getTarget() {
        return target;
    }

    public void setTarget(WeakReference<ImageView> target) {
        this.target = target;
    }
}
