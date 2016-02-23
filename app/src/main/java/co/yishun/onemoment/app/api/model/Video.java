package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

import android.support.annotation.StringDef;

import java.util.List;

import co.yishun.onemoment.app.api.modelv4.VideoProvider;

/**
 * Created by Carlos on 2015/8/9.
 */
public class Video extends ApiModel implements VideoProvider {
    @SerializedName("filename")
    public String fileName;
    public String accountId;
    public
    @Type
    String type;
    public List<VideoTag> tags;
    public Domain domain;

    @Override
    public String getKey() {
        return fileName.substring(0, fileName.lastIndexOf("."));
    }

    @Override
    public String getFilename() {
        return fileName;
    }

    @Override
    public String getDownloadUrl() {
        return domain + fileName;
    }

    @Override
    public List<VideoTag> getTags() {
        return tags;
    }

    @Override
    public String getAvatarUrl() {
        return null;
    }

    @Override
    public String getNickname() {
        return null;
    }

    @StringDef({"public", "private"})
    public @interface Type {
    }
}
