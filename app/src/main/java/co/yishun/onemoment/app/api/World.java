package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.List;

import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.WorldTag;
import retrofit.http.Field;
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
    List<Banner> getBanners(@Query("limit") int bannerNumLimit);

    @FormUrlEncoded
    @POST("/world/like/video/{video_id}")
    ApiModel likeVideo(@Path("video_like") @NonNull String videoId, @NonNull String userId);

    @FormUrlEncoded
    @POST("/world/unlike/video/{video_id}")
    ApiModel unlikeVideo(@Path("video_like") @NonNull String videoId, @NonNull String userId);

    @GET("/world/tags")
    List<WorldTag> getWorldTagList(@Query("limit") int limit, @Query("type") @Nullable String type, @Query("ranking") @Nullable String ranking);

    @GET("/world/videos/liked")
    List<Video> getLikedVideos(@Query("account_id") @NonNull String userId, @Query("offset") int offset, @Query("limit") int limit);

    @GET("/world/tags/joined")
    List<WorldTag> getJoinedWorldTags(@Query("account_id") @NonNull String userId, @Query("type") @NonNull String type, @Query("offset") int offset, @Query("limit") int limit);

    @GET("/world/videos")
    List<Video> getVideoOfTag(@Query("tag_name") @NonNull String tagName, @Query("offset") int offset, @Query("limit") int limit
            , @Query("account_id") @Nullable String userId, @Query("seed") @Nullable String seed, @Query("type") @Nullable String type);

    @GET("/world/tag/suggest")
    List<WorldTag> getSuggestedTagName(@Query("words") @NonNull String tagName);

    @POST("/world/video")
    @FormUrlEncoded
    Video addVideoToWorld(@Field("account_id") @NonNull String userId, @Field("type") @NonNull String type,
                          @Field("filename") @NonNull String fileName, @Field("tags") @NonNull List<Video.VideoTag> tags
    );
}
