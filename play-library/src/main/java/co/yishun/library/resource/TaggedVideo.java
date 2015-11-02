package co.yishun.library.resource;

import android.net.Uri;

import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/3/15.
 */
public class TaggedVideo implements VideoResource {
    private VideoResource mVideoResource;
    private List<VideoTag> mTags;

    public TaggedVideo(VideoResource videoResource, List<VideoTag> tags) {
        mVideoResource = videoResource;
        mTags = tags;
    }

    @Override
    public Uri getVideoUri() {
        return mVideoResource.getVideoUri();
    }

    @Override
    public Uri getAvatarUri() {
        return mVideoResource.getAvatarUri();
    }

    @Override
    public List<VideoTag> getVideoTags() {
        return mTags;
    }
}
