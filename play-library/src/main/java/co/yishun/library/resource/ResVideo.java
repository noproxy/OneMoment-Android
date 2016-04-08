package co.yishun.library.resource;

import android.content.Context;
import android.net.Uri;

import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/1/15.
 */
public class ResVideo implements VideoResource {
    private VideoResource mVideoResource;
    private Uri mVideoUri;

    public ResVideo(VideoResource videoResource, Context context, int resId) {
        this.mVideoResource = videoResource;
        String uriString = "android.resource://" + context.getPackageName() + "/" + resId;
        this.mVideoUri = Uri.parse(uriString);
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
