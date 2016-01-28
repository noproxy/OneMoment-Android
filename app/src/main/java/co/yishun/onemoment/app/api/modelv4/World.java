package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

/**
 * Created by Jinge on 2016/1/20.
 */
public class World extends ApiModel implements WorldProvider {
    public String _id;
    public int createTime;
    public String thumbnail;
    public boolean available;
    public String name;
    public int videosNum;

    public int ranking;

    public String accountId;
    public int updateTime;
    public int order;
    public String type;
    public List<InvitedUser> invited;

    @Override public String getName() {
        return name;
    }

    @Override public void setName(String name) {
        this.name = name;
    }

    @Override public String getId() {
        return _id;
    }

    @Override public void setId(String id) {
        this._id = id;
    }

    @Override public int getVideosNum() {
        return videosNum;
    }

    @Override public String getThumb() {
        return thumbnail;
    }

    @Override public void setThumb(String url) {
        thumbnail = url;
    }

    public static class InvitedUser {
        public String _id;
        public boolean add;
        public boolean read;
        public boolean available;
        public boolean accepted;
        public int acceptTime;
    }
}
