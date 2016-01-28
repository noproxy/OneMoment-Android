package co.yishun.onemoment.app.api.modelv4;

import java.io.Serializable;

/**
 * Created by Jinge on 2016/1/28.
 */
public interface WorldProvider  extends Serializable{
    String getName();
    String getId();
    int getVideosNum();
    String getThumb();
    void setName(String name);
    void setId(String id);
    void setThumb(String url);
}
