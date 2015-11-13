package co.yishun.onemoment.app.api.model;

import com.google.gson.annotations.SerializedName;

import java.util.Arrays;

import co.yishun.onemoment.app.api.Account;

/**
 * Bean contains user info.
 * <p>
 * Created by Carlos on 2015/8/4.
 */
public class User extends ApiModel {
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
    public long signInTime;
    public String phone;
    public boolean phoneVerified;
    public String weixinNickname;
    public String nickname;
    public String[] likedWorlds;
    @SerializedName("signin_ip")
    public String signInIP;
    @SerializedName("signup_ua")
    public String signUpUserAgent;
    //    public String introduction; TODO ?
    public Account.Gender gender;
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
    public String[] joinedWorld;
    public String[] likedWorldVideos;

    public User() {
        super(1, "");

    }

    public User(int code, String msg) {
        super(code, msg);
    }


    @Override
    public String toString() {
        return "User{" +
                "available=" + available +
                ", createdWorld=" + Arrays.toString(createdWorld) +
                ", weiboNickname='" + weiboNickname + '\'' +
                ", signInTime=" + signInTime +
                ", phone='" + phone + '\'' +
                ", phoneVerified=" + phoneVerified +
                ", weixinNickname='" + weixinNickname + '\'' +
                ", nickname='" + nickname + '\'' +
                ", likedWorlds=" + Arrays.toString(likedWorlds) +
                ", signInIP='" + signInIP + '\'' +
                ", signUpUserAgent='" + signUpUserAgent + '\'' +
                ", gender='" + gender + '\'' +
                ", weiboUid='" + weiboUid + '\'' +
                ", weixinUid='" + weixinUid + '\'' +
                ", signInUserAgent='" + signInUserAgent + '\'' +
                ", signUpIP='" + signUpIP + '\'' +
                ", signUpTime='" + signUpTime + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", location='" + location + '\'' +
                ", _id='" + _id + '\'' +
                ", joinedWorld=" + Arrays.toString(joinedWorld) +
                '}';
    }
}
