package co.yishun.library.resource;

import android.net.Uri;

import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/3/15.
 */
public class LocalAvatar implements VideoResource {
    private VideoResource mVideoResource;
    private Uri mAvatarUri;

    public LocalAvatar(VideoResource videoResource, String path) {
        mVideoResource = videoResource;
        mAvatarUri = Uri.parse(path);
    }

    @Override
    public Uri getVideoUri() {
        return mVideoResource.getVideoUri();
    }

    @Override
    public Uri getAvatarUri() {
        return mAvatarUri;
    }

    @Override
    public List<VideoTag> getVideoTags() {
        return mVideoResource.getVideoTags();
    }
}
