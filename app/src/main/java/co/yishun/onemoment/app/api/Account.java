package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import co.yishun.onemoment.app.model.User;
import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Carlos on 2015/8/4.
 */
public interface Account {
    @GET("/account/check_nickname")
    boolean isNicknameExist(@NonNull @Field("nickname") String nickname);

    @POST("/account/signup")
    void signUpByPhone(@Field("phone") @NonNull String phone,
                       @Field("password") @NonNull String password);

    @POST("/account/signin")
    void signInByPhone(@Field("phone") @NonNull String phone,
                       @Field("password") @NonNull String password);


    @POST("/account/bind_weibo/{account_id}")
    void bindWeibo(@NonNull @Path("account_id") String userId,
                   @NonNull @Field("weibo_uid") String weiboUid);

    @POST("/account/unbind_weibo/{account_id}")
    void unbindWeibo(@NonNull @Path("account_id") String userId,
                     @NonNull @Field("weibo_uid") String weiboUid);


    @POST("/account/weibo_signup")
    void signUpByWeibo(@Field("uid") @NonNull String weiboUid,
                       @Field("introduction") @Nullable String introduction,
                       @Field("gender") @Nullable String gender,
                       @Field("avatar_url") @Nullable String avatarUrl,
                       @Field("location") @Nullable String location
    );

    @POST("/account/send_verify_sms")
    void sendVerifySms(@Field("phone") @NonNull String phone,
                       @Field("String") @Nullable String signUpOrResetPassword);

    @POST("/account/verify_phone")
    void verifyPhone(@Field("phone") @NonNull String phone,
                     @Field("verify_code") @NonNull String verifyCode);

    @GET("/account/account/{account_id}")
    User getUserInfo(@Path("account_id") @NonNull String userId);

    @GET("/account/weibo/{weibo_id}")
    void getUserInfoByWeiboUid(@Path("weibo_id") @NonNull String weiboUid);

    @GET("/account/videos/{account_id}")
    void getVideoList(@Path("account_id") @NonNull String userId);

    @GET("/account/tag/download/{account_id}")
    void getTagUrl(@Path("account_id") @NonNull String accountUid);

    @POST("/account/reset_password")
    void resetPassword(@Field("phone") @NonNull String phone, @Field("password") @NonNull String newPassword);
}

