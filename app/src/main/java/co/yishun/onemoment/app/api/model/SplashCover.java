package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jinge on 2015/12/29.
 */
public class SplashCover extends ApiModel {
    public String url;
    @SerializedName("update_ts")
    public long updateTime;
    public int stay;
}
