package co.yishun.onemoment.app.api.modelv4;

import co.yishun.onemoment.app.api.model.QiniuKeyProvider;

/**
 * Created by Jinge on 2016/1/27.
 *
 * Provide video common information. Since the difference among different version of ApiModels.
 *
 */
public interface VideoProvider extends QiniuKeyProvider {
    String getFilename();
    String getDownloadUrl();
}
