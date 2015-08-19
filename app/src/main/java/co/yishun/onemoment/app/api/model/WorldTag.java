package co.yishun.onemoment.app.api.model;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Carlos on 2015/8/8.
 */
public class WorldTag extends ApiModel {
    public String ranking;
    @SerializedName("videos_num")
    public int videosCount;
    public String name;
    public String thumbnail;
    @SerializedName("like_num")
    public int likeCount;
    public @Type String type;
    public Domain domain;


    @StringDef({"public", "private"})
    public @interface Type {
    }
}
