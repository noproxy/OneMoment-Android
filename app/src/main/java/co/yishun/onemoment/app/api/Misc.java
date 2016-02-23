package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import co.yishun.onemoment.app.api.model.Domain;
import co.yishun.onemoment.app.api.model.SplashCover;
import co.yishun.onemoment.app.api.model.UploadToken;
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
    UploadToken getUploadToken(@Query("filename") @Nullable String filename);

    @GET("/misc/upload_token")
    UploadToken getUploadToken(@Query("filename") @Nullable String filename, @Query("type") String type);

    @GET("/misc/resource_domain")
    Domain getResourceDomain(@Query("type") @Nullable @DomainType String type);

    @GET("/misc/share_text")
    void getShareText(@Query("type") @Nullable @ShareType String type);

    @GET("/misc/share_image")
    void getShareImage(@Query("type") @Nullable @ShareType String type);

    @FormUrlEncoded
    @POST("/misc/delete_video")
    void deleteVideo(@Field("filename") @Nullable String filename);

    @FormUrlEncoded
    @POST("misc/delete_tag/{account_id}")
    void deleteTag(@Path("account_id") @NonNull String userId);

    @GET("/misc/app/cover/default")
    SplashCover getSplashCover();

    @StringDef({"friends", "long_video", "world"})
    @interface ShareType {
    }

    @StringDef({"video", "tag"})
    @interface DomainType {
    }
}
