package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

import co.yishun.onemoment.app.api.model.QiniuKeyProvider;
import co.yishun.onemoment.app.api.model.VideoTag;

/**
 * Created by Jinge on 2016/1/27.
 *
 * Provide video common information. Since the difference among different version of ApiModels.
 *
 */
public interface VideoProvider extends QiniuKeyProvider {
    String getFilename();
    String getDownloadUrl();
    List<VideoTag> getTags();
    String getAvatarUrl();
}
