package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

import android.support.annotation.StringDef;

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
    public
    @Type
    String type;
    public Domain domain;
    // pick from Palette
    public int color;

    @Override
    public int compareTo(Object another) {
        return Integer.parseInt(((WorldTag) another).ranking) - Integer.parseInt(ranking);
        // Collections.sort is in ascending natural order. So this value being positive means
        // this one will be placed later. So flip.
    }

    @Override
    public boolean equals(Object another) {
        return another instanceof WorldTag && ((WorldTag) another).ranking.equals(ranking);
    }

    @StringDef({"public", "private"})
    public @interface Type {
    }
}
