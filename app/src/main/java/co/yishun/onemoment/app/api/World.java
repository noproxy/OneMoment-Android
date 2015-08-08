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
public interface World {
    @GET("/world/banners")
    void getBanners(@Field("limit") int bannerNumLimit);

    @POST("/world/like/video/{video_id}")
    void likeVideo(@Path("video_like") @NonNull String videoId, @NonNull String userId);

    @POST("/world/unlike/video/{video_id}")
    void unlikeVideo(@Path("video_like") @NonNull String videoId, @NonNull String userId);

    @GET("/world/tags")
    void getTagList(@Field("limit") int limit, @Field("type") @Nullable String type, @Field("ranking") @Nullable String ranking);

    @GET("/world/videos/liked")
    void getLikedVideos(@Field("account_id") @NonNull String userId, @Field("offset") int offset, @Field("limit") int limit);

    @GET("/world/tags/joined")
    void getJoinedTags(@Field("account_id") @NonNull String userId, @Field("type") @NonNull String type, @Field("offset") int offset, @Field("limit") int limit);

}
