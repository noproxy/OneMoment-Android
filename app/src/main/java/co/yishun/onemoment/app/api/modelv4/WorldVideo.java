package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

import co.yishun.onemoment.app.api.model.VideoTag;

/**
 *
 * Created by Jinge on 2016/1/23.
 */
public class WorldVideo extends ApiModel {
    public String _id;
    public String accountId;
    public int createTime;
    public String worldId;
    public int order;
    public boolean available;
    public List<VideoTag> tags;
    public String downloadUrl;
}
