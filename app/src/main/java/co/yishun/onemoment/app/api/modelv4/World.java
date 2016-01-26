package co.yishun.onemoment.app.api.modelv4;

import java.util.List;

/**
 * Created by Jinge on 2016/1/20.
 */
public class World extends ApiModel {
    public String _id;
    public int createTime;
    public String thumbnail;
    public boolean available;
    public String name;
    public int videoNum;

    public String accountId;
    public int updateTime;
    public int order;
    public String type;
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
