package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Jinge on 2015/12/11.
 */
public class ShareInfo extends ApiModel {
    @SerializedName("image_url")
    public String imageUrl;
    public String link;
    public String title;
}
