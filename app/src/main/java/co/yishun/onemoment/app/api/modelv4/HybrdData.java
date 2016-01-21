package co.yishun.onemoment.app.api.modelv4;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jinge on 2016/1/21.
 */
public class HybrdData extends ApiModel {
    @SerializedName("update_time")
    public int updateTime;
    public String md5;
    public long length;
}
