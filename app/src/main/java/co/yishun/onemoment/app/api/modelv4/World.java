package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

/**
 * Created by Jinge on 2016/1/20.
 */
public class World extends ApiModel {
    public String _id;
    public String accountId;
    public String name;
    public int updateTime;
    public int createTime;
    public int videosNum;
    public int order;
    public String type;
    public boolean available;
    public List<InvitedUser> invited;

    public static class InvitedUser {
        public String _id;
        public boolean add;
        public boolean read;
        public boolean available;
        public boolean accepted;
        public int acceptTime;
    }
}
