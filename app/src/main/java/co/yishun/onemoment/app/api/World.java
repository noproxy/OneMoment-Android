package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Carlos on 2015/8/8.
 */
public interface World {
    @GET("/world/banners")
    void getBanners(@Query("limit") int bannerNumLimit);

    @FormUrlEncoded
    @POST("/world/like/video/{video_id}")
    void likeVideo(@Path("video_like") @NonNull String videoId, @NonNull String userId);

    @FormUrlEncoded
    @POST("/world/unlike/video/{video_id}")
    void unlikeVideo(@Path("video_like") @NonNull String videoId, @NonNull String userId);

    @GET("/world/tags")
    void getTagList(@Query("limit") int limit, @Query("type") @Nullable String type, @Query("ranking") @Nullable String ranking);

    @GET("/world/videos/liked")
    void getLikedVideos(@Query("account_id") @NonNull String userId, @Query("offset") int offset, @Query("limit") int limit);

    @GET("/world/tags/joined")
    void getJoinedTags(@Query("account_id") @NonNull String userId, @Query("type") @NonNull String type, @Query("offset") int offset, @Query("limit") int limit);

}
