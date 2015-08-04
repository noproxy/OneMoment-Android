package co.yishun.onemoment.app.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Carlos on 2015/8/4.
 */
public class User {
/*
{
    "msg": "get account successfully",
    "code": 1,
    "data": {

    }
}
 */

    public boolean available;
    public String[] created_world;
    @SerializedName("weibo_nickname")
    public String weiboNickname;
    @SerializedName("signin_time")
    public String SignInTime;
    public String phone;
    public String phone_verified;
    public String weixin_nickname;
    public String nickname;
    public String liked_worlds;
    public String signin_ip;
    public String signup_ua;
    public String introduction;
    public String gender;
    public String weibo_uid;
    public String weixin_uid;
    public String signin_ua;
    public String signup_ip;
    public String signup_time;
    public String avatar_url;
    public String location;
    public String _id;
    public String joined_world;
}
