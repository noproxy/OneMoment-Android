package co.yishun.library.resource;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/3/15.
 */
public class NetworkVideo implements VideoResource {
    private String mUrl;
    private String mPath;
    private List<VideoTag> mTags;

    /**
     * If the video hasn't been downloaded, use this constructor.
     * When the video's download finish, call {@link #setPath(String)}
     *
     * @param url  url of the video
     * @param tags tags of the video
     */
    public NetworkVideo(String url, List<VideoTag> tags) {
        mUrl = url;
        mTags = tags;
    }

    /**
     * If the video has been downloaded, use this constructor.
     *
     * @param tags tags of the video
     * @param path path of the video
     */
    public NetworkVideo(List<VideoTag> tags, @NonNull String path) {
        mTags = tags;
        mPath = path;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setPath(String path) {
        this.mPath = path;
        Log.d("play", "set " + path);
    }

    @Override
    public Uri getVideoUri() {
        return TextUtils.isEmpty(mPath) ? Uri.parse(mUrl) : Uri.parse(mPath);
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public List<VideoTag> getVideoTags() {
        return mTags;
    }
}
