package co.yishun.library.resource;

import android.net.Uri;

import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/3/15.
 */
public class NetworkVideo implements VideoResource {
    private VideoResource mVideoResource;

    public NetworkVideo(VideoResource videoResource, String url) {
        mVideoResource = videoResource;

    }

    @Override
    public Uri getVideoUri() {
        return null;
    }

    @Override
    public Uri getAvatarUri() {
        return null;
    }

    @Override
    public List<VideoTag> getVideoTags() {
        return null;
    }
}
