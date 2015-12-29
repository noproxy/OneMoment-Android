package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jinge on 2015/12/29.
 */
public class SplashCover extends ApiModel{
    String url;
    @SerializedName("update_ts")
    long updateTime;
    int stay;
}
