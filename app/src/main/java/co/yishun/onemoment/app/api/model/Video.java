package co.yishun.onemoment.app.api.model;

import android.support.annotation.StringDef;

import com.google.gson.annotations.SerializedName;

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
        return fileName;
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

    @Override
    public String toString() {
        return "Video{" +
                "fileName='" + fileName + '\'' +
                ", accountId='" + accountId + '\'' +
                ", type='" + type + '\'' +
                ", tags=" + tags +
                ", domain=" + domain +
                '}';
    }

    @StringDef({"public", "private"})
    public @interface Type {
    }
}
