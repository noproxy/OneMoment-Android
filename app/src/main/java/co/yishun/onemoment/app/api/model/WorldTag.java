package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Carlos on 2015/8/8.
 */
public class WorldTag extends ApiModel {
    public long ranking;
    @SerializedName("videos_num")
    public int videosCount;
    public String name;
    public String thumbnail;
    @SerializedName("like_num")
    public int likeCount;
    public String type;
}
