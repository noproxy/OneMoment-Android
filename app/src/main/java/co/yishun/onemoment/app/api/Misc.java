package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Carlos on 2015/8/8.
 */
public interface Misc {

    @GET("/misc/upload_token")
    void getUploadToken(@Query("filename") @Nullable String filename);

    @GET("/misc/resource_domain")
    void getReourceDomain(@Query("type") @Nullable String type);

    @GET("/misc/share_text")
    void getShareText(@Query("type") @Nullable String type);

    @GET("/misc/share_image")
    void getShareImage(@Query("type") @Nullable String type);

    @FormUrlEncoded
    @POST("/misc/delete_video")
    void deleteVideo(@Field("filename") @Nullable String filename);

    @FormUrlEncoded
    @POST("misc/delete_tag/{account_id}")
    void deleteTag(@Path("account_id") @NonNull String userId);
}
