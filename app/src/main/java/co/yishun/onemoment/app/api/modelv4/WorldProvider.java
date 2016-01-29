package co.yishun.onemoment.app.api.modelv4;

import java.io.Serializable;

/**
 * Created by Jinge on 2016/1/28.
 */
public interface WorldProvider extends Serializable {
    String getName();

    void setName(String name);

    String getId();

    void setId(String id);

    int getVideosNum();

    String getThumb();

    void setThumb(String url);
}
