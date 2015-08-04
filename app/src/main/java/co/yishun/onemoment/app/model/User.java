package co.yishun.onemoment.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

/**
 * Bean contains user info.
 * <p>
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
    public String[] createdWorld;
    public String weiboNickname;

    @SerializedName("signin_name")
    public String signInTime;
    public String phone;
    public String phoneVerified;
    public String weixinNickname;
    public String nickname;
    public String likedWorlds;
    @SerializedName("signin_ip")
    public String signInIP;
    @SerializedName("signup_ua")
    public String signUpUserAgent;
    public String introduction;
    public String gender;
    public String weiboUid;
    public String weixinUid;
    @SerializedName("signin_ua")
    public String signInUserAgent;
    @SerializedName("signup_ip")
    public String signUpIP;
    @SerializedName("signup_time")
    public String signUpTime;
    public String avatarUrl;
    public String location;
    public String _id;
    public String joinedWorld;

    @Override public String toString() {
        return "User{" +
                "available=" + available +
                ", createdWorld=" + Arrays.toString(createdWorld) +
                ", weiboNickname='" + weiboNickname + '\'' +
                ", signInTime='" + signInTime + '\'' +
                ", phone='" + phone + '\'' +
                ", phoneVerified='" + phoneVerified + '\'' +
                ", weixinNickname='" + weixinNickname + '\'' +
                ", nickname='" + nickname + '\'' +
                ", likedWorlds='" + likedWorlds + '\'' +
                ", signInIP='" + signInIP + '\'' +
                ", signUpUserAgent='" + signUpUserAgent + '\'' +
                ", introduction='" + introduction + '\'' +
                ", gender='" + gender + '\'' +
                ", weiboUid='" + weiboUid + '\'' +
                ", weixinUid='" + weixinUid + '\'' +
                ", signInUserAgent='" + signInUserAgent + '\'' +
                ", signUpIP='" + signUpIP + '\'' +
                ", signUpTime='" + signUpTime + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", location='" + location + '\'' +
                ", _id='" + _id + '\'' +
                ", joinedWorld='" + joinedWorld + '\'' +
                '}';
    }
}
