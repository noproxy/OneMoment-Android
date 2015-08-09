package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Carlos on 2015/8/8.
 */
public class Moment extends ApiModel {
    public String mimeType = "video/mp4";
    public String hash;
    @SerializedName("fsize")
    public long fileSize;
    public String key;
    public long putTime;

    // When be created by Gson into a List, auto set code 1
    public Moment() {
        this.code = 1;
        this.msg = "";
    }
}
