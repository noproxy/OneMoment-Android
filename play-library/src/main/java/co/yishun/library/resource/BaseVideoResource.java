package co.yishun.library.resource;

import android.net.Uri;

import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Created by jay on 10/1/15.
 */
public class BaseVideoResource implements VideoResource {

    public BaseVideoResource() {}

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
