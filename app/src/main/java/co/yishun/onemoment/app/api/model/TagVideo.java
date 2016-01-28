package co.yishun.onemoment.app.api.model;

/**
 * Created by Carlos on 2015/8/17.
 */
public class TagVideo extends Video {
    public String _id;
    public boolean available;
    public boolean liked;
    public long timestamp;
    public boolean visible;
    public long createTime;
    public int likeNum;
    public String avatar;
    public String worldId;
    public String nickname;

    public Seed seed;

    @Override public String getAvatarUrl() {
        return avatar;
    }

    @Override public String getNickname() {
        return nickname;
    }
}
