package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.ApiMoment;
import co.yishun.onemoment.app.api.model.Link;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.api.model.User;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Carlos on 2015/8/4.
 */
public interface Account {
    @GET("/account/check_nickname")
    ApiModel isNicknameExist(
            @NonNull @Query("nickname") String nickname
    );

    @FormUrlEncoded
    @POST("/account/signup")
    User signUpByPhone(
            @Field("phone") @NonNull String phone,
            @Field("password") @NonNull String password,
            @Field("nickname") @NonNull String nickname,
            @Field("gender") @NonNull Gender gender,
            @Field("avatar_url") @Nullable String avatarUrl,
            @Field("location") @NonNull String location
    );

    @FormUrlEncoded
    @POST("/account/weibo_signup")
    User signUpByWeibo(
            @Field("uid") @NonNull String weiboUid,
            @Field("weibo_nickname") @NonNull String nickname,
            @Field("gender") @NonNull Gender gender,
            @Field("avatar_url") @NonNull String avatarUrl,
            @Field("location") @NonNull String location,
            @Field("nickname") @NonNull String momentName
    );

    @FormUrlEncoded
    @POST("/account/weixin_signup")
    User signUpByWeChat(
            @Field("uid") @NonNull String weixinUid,
            @Field("weixin_nickname") @NonNull String nickname,
            @Field("gender") @NonNull Gender gender,
            @Field("avatar_url") @NonNull String avatarUrl,
            @Field("location") @NonNull String location,
            @Field("nickname") @NonNull String momentName
    );

    @FormUrlEncoded
    @POST("/account/qq_signup")
    User signUpByQQ(
            @Field("uid") @NonNull String qqUid,
            @Field("qq_nickname") @NonNull String nickname,
            @Field("gender") @NonNull Gender gender,
            @Field("avatar_url") @NonNull String avatarUrl,
            @Field("location") @NonNull String location,
            @Field("nickname") @NonNull String momentName
    );

    @FormUrlEncoded
    @POST("/account/signin")
    User signInByPhone(
            @Field("phone") @NonNull String phone,
            @Field("password") @NonNull String password);

    @FormUrlEncoded
    @POST("/account/update/{account_id}")
    User updateInfo(
            @Path("account_id") @NonNull String userId,
            @Field("nickname") @Nullable String nickname,
            @Field("gender") @Nullable Gender gender,
            @Field("avatar_url") @Nullable String remoteUrlOrKey,
            @Field("location") @Nullable String location
    );

    @FormUrlEncoded
    @POST("/account/send_verify_sms")
    ApiModel sendVerifySms(
            @Field("phone") @NonNull String phone,
            @Field("check") @Nullable String signUpOrResetPassword
    );

    @FormUrlEncoded
    @POST("/account/verify_phone")
    ApiModel verifyPhone(
            @Field("phone") @NonNull String phone,
            @Field("verify_code") @NonNull String verifyCode
    );

    @GET("/account/account/{account_id}")
    User getUserInfo(
            @Path("account_id") @NonNull String userId
    );

    @GET("/account/weibo/{weibo_id}")
    User getUserInfoByWeiboUid(
            @Path("weibo_id") @NonNull String weiboId
    );

    @GET("/account/weixin/{weixin_id}")
    User getUserInfoByWeChatUid(
            @Path("weixin_id") @NonNull String weChatId
    );

    @GET("/account/qq/{qq_number}")
    User getUserInfoByQQUid(
            @Path("qq_number") @NonNull String qq
    );


    @GET("/account/videos/{account_id}")
    ListWithError<ApiMoment> getVideoList(
            @Path("account_id") @NonNull String userId
    );

    @GET("/account/tag/download/{account_id}")
    Link getTagUrl(
            @Path("account_id") @NonNull String accountUid
    );

    @FormUrlEncoded
    @POST("/account/reset_password")
    ApiModel resetPassword(
            @Field("phone") @NonNull String phone,
            @Field("password") @NonNull String newPassword
    );

    @FormUrlEncoded
    @POST("/account/bind_weibo/{account_id}")
    User bindWeibo(
            @NonNull @Path("account_id") String userId,
            @NonNull @Field("weibo_uid") String weiboUid,
            @NonNull @Field("weibo_nickname") String weiboNickname
    );

    @FormUrlEncoded
    @POST("/account/unbind_weibo/{account_id}")
    User unbindWeibo(@NonNull @Path("account_id") String userId,
                     @NonNull @Field("weibo_uid") String weiboUid);

    @FormUrlEncoded
    @POST("/account/share")
    ShareInfo share(
            @NonNull @Field("filename") String filename,
            @NonNull @Field("account_id") String userId,
            @Nullable @Field("tags") String tags);

    enum Gender {
        @SerializedName("f")
        FEMALE,
        @SerializedName("m")
        MALE,
        @SerializedName("n")
        OTHER;

        public static Gender format(String s) {
            switch (s) {
                case "f":
                    return FEMALE;
                case "m":
                    return MALE;
                default:
                    return OTHER;
            }
        }

        public static Gender format(int i) {
            switch (i) {
                case 0:
                    return FEMALE;
                case 1:
                    return MALE;
                default:
                    return OTHER;
            }
        }

        @Override
        public String toString() {
            switch (this) {
                case FEMALE:
                    return "f";
                case MALE:
                    return "m";
                default:
                    return "n";
            }
        }

        public int toInt() {
            switch (this) {
                case FEMALE:
                    return 0;
                case MALE:
                    return 1;
                default:
                    return 2;
            }
        }

    }
}

