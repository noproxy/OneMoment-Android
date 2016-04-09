package co.yishun.onemoment.app.api.modelv4;

import java.io.Serializable;

/**
 * Created by Jinge on 2016/1/29.
 */
public interface ShareInfoProvider extends Serializable {
    String getImageUrl();

    String getLink();

    String getTitle();
}
