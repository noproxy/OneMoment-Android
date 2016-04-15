package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

import co.yishun.onemoment.app.api.model.VideoTag;

/**
 * Created by Jinge on 2016/1/23.
 */
public class WorldVideo extends ApiModel implements VideoProvider {
    public String _id;
    public String accountId;
    public int createTime;
    public String worldId;
    public int order;
    public boolean available;
    public List<VideoTag> tags;
    public String downloadUrl;
    public String filename;
    public String avatarUrl;
    public String nickname;

    @Override
    public String getFilename() {
        return filename;
    }

    @Override
    public String getDownloadUrl() {
        return downloadUrl;
    }

    @Override
    public List<VideoTag> getTags() {
        return tags;
    }

    @Override
    public String getAvatarUrl() {
        return avatarUrl;
    }

    @Override
    public String getNickname() {
        return nickname;
    }

    @Override
    public String getKey() {
        return filename;
    }

    @Override
    public String toString() {
        return "WorldVideo{" +
                "_id='" + _id + '\'' +
                ", accountId='" + accountId + '\'' +
                ", createTime=" + createTime +
                ", worldId='" + worldId + '\'' +
                ", order=" + order +
                ", available=" + available +
                ", tags=" + tags +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", filename='" + filename + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", nickname='" + nickname + '\'' +
                '}';
    }
}
