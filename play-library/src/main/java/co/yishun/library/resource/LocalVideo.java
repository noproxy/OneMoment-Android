package co.yishun.library.resource;

import android.net.Uri;

import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/3/15.
 */
public class LocalVideo implements VideoResource {
    private VideoResource mVideoResource;
    private Uri mVideoUri;

    public LocalVideo(VideoResource videoResource, String path) {
        mVideoResource = videoResource;
        mVideoUri = Uri.parse(path);
    }

    @Override
    public Uri getVideoUri() {
        return mVideoUri;
    }

    @Override
    public Uri getAvatarUri() {
        return mVideoResource.getAvatarUri();
    }

    @Override
    public List<VideoTag> getVideoTags() {
        return mVideoResource.getVideoTags();
    }
}
