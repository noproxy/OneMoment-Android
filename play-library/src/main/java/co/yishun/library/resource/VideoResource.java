package co.yishun.library.resource;

import android.net.Uri;

import java.util.List;

import co.yishun.library.tag.VideoTag;

/**
 * Video Resource interface
 * @author ZhihaoJun
 */
public interface VideoResource {
    public Uri getVideoUri();
    public Uri getAvatarUri();
    public List<VideoTag> getVideoTags();
}
