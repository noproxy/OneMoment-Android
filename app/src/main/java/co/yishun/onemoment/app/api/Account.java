package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Link;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.api.model.Video;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Carlos on 2015/8/4.
 */
public interface Account {
    @GET("/account/check_nickname")
    ApiModel isNicknameExist(
            @NonNull @Field("nickname") String nickname
    );

    @POST("/account/signup")
    User signUpByPhone(
            @Field("phone") @NonNull String phone,
            @Field("password") @NonNull String password);

    @POST("/account/weibo_signup")
    User signUpByWeibo(
            @Field("uid") @NonNull String weiboUid,
            @Field("weibo_nickname") @NonNull String nickname,
            @Field("gender") @NonNull String gender,
            @Field("avatar_url") @NonNull String avatarUrl
    );

    @POST("/account/weixin_signup")
    User signUpByWeixin(
            @Field("uid") @NonNull String weixinUid,
            @Field("weixin_nickname") @NonNull String nickname,
            @Field("gender") @NonNull String gender,
            @Field("avatar_url") @NonNull String avatarUrl
    );

    @POST("/account/qq_signup")
    User signUpByQQ(
            @Field("qq_number") @NonNull String qqNumber,
            @Field("qq_nickname") @NonNull String nickname,
            @Field("gender") @NonNull String gender,
            @Field("avatar_url") @NonNull String avatarUrl
    );

    @POST("/account/signin")
    User signInByPhone(
            @Field("phone") @NonNull String phone,
            @Field("password") @NonNull String password);

    @POST("/account/update/{account_id}")
    User updateInfo(
            @Path("account_id") @NonNull String userId,
            @Field("weibo_nickname") @Nullable String nickname,
            @Field("gender") @Nullable String gender,
            @Field("avatar_url") @Nullable String avatarUrl,
            @Field("location") @Nullable String location
    );

    @POST("/account/send_verify_sms")
    ApiModel sendVerifySms(
            @Field("phone") @NonNull String phone,
            @Field("String") @Nullable String signUpOrResetPassword
    );

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

    @GET("/account/videos/{account_id}")
    List<Video> getVideoList(
            @Path("account_id") @NonNull String userId
    );

    @GET("/account/tag/download/{account_id}")
    Link getTagUrl(
            @Path("account_id") @NonNull String accountUid
    );

    @POST("/account/reset_password")
    ApiModel resetPassword(
            @Field("phone") @NonNull String phone,
            @Field("password") @NonNull String newPassword
    );

//    @POST("/account/bind_weibo/{account_id}")
//    void bindWeibo(
//            @NonNull @Path("account_id") String userId,
//            @NonNull @Field("weibo_uid") String weiboUid
//    );
//
//    @POST("/account/unbind_weibo/{account_id}")
//    void unbindWeibo(@NonNull @Path("account_id") String userId,
//                     @NonNull @Field("weibo_uid") String weiboUid);
}

