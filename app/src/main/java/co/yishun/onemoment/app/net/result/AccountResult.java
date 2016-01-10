package co.yishun.onemoment.app.net.result;

import java.io.Serializable;

/**
 * Created by Carlos on 2/15/15.
 *
 * User data of version 1.x
 * User for migration.
 */
public class AccountResult implements Serializable {

    public static class Data implements Serializable {
        private String _id;
        private String phone;
        private boolean available;
        private String signup_ua;
        private int signup_time;
        private String signup_ip;
        private String signin_ua;
        private int signin_time;
        private String signin_ip;
        private String nickname;
        private String email;
        private boolean email_validated;
        private String introduction;
        private String avatar_url;
        private String weibo_uid;
        private String gender;
        private String location;

        public Data() {
        }

        public String getArea() {
            return location;
        }

        public String getGender() {
            return gender;
        }

        public String get_id() {
            return _id;
        }

        public String getPhone() {
            return phone;
        }

        public boolean isAvailable() {
            return available;
        }

        public String getSignup_ua() {
            return signup_ua;
        }

        public int getSignup_time() {
            return signup_time;
        }

        public String getSignup_ip() {
            return signup_ip;
        }

        public String getSignin_ua() {
            return signin_ua;
        }

        public int getSignin_time() {
            return signin_time;
        }

        public String getSignin_ip() {
            return signin_ip;
        }

        public String getNickname() {
            return nickname;
        }

        public String getEmail() {
            return email;
        }

        public boolean isEmail_validated() {
            return email_validated;
        }

        public String getIntroduction() {
            return introduction;
        }

        public String getAvatar_url() {
            return avatar_url;
        }

        public String getWeibo_uid() {
            return weibo_uid;
        }
    }
}
