package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Carlos on 2015/8/8.
 */
public class Video extends ApiModel {
    public String mimeType = "video/mp4";
    public String hash;
    @SerializedName("fsize")
    public long fileSize;
    public String key;
    public long putTime;
}
