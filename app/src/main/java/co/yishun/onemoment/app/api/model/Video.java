package co.yishun.onemoment.app.api.model;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Carlos on 2015/8/9.
 */
public class Video extends ApiModel implements QiniuKeyProvider {
    @SerializedName("filename")
    public String fileName;
    public String accountId;
    public @Type String type;
    public List<VideoTag> tags;
    public Domain domain;

    @Override
    public String getName() {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    @StringDef({"public", "private"})
    public @interface Type {
    }
}
