package co.yishun.onemoment.app.account.auth;

import java.io.Serializable;

/**
 * User's Info <p> Created by yyz on 6/3/15.
 */
public class UserInfo implements Serializable {
    public String id;
    public String name;
    public String location;
    public String description;
    public String gender;
    public String avatar_large;

    @Override
    public String toString() {
        return "UserInfo{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", description='" + description + '\'' +
                ", gender='" + gender + '\'' +
                ", avatar_large='" + avatar_large + '\'' +
                '}';
    }
}
