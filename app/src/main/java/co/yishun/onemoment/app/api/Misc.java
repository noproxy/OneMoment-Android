package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import retrofit.http.Field;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;

/**
 * Created by Carlos on 2015/8/8.
 */
public interface Misc {

    @GET("/misc/upload_token")
    void getUploadToken(@Field("filename") @Nullable String filename);

    @GET("/misc/resource_domain")
    void getReourceDomain(@Field("type") @Nullable String type);

    @GET("/misc/share_text")
    void getShareText(@Field("type") @Nullable String type);

    @GET("/misc/share_image")
    void getShareImage(@Field("type") @Nullable String type);

    @POST("/misc/delete_video")
    void deleteVideo(@Field("filename") @Nullable String filename);

    @POST("misc/delete_tag/{account_id}")
    void deleteTag(@Path("account_id") @NonNull String userId);
}
