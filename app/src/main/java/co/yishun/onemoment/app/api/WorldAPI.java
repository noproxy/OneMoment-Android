package co.yishun.onemoment.app.api;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;

import java.util.List;

import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.VideoTag;
import co.yishun.onemoment.app.api.model.WorldTag;
import co.yishun.onemoment.app.util.GsonFactory;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Path;
import retrofit.http.Query;

/**
 * Created by Carlos on 2015/8/8. <p> Rename from World.
 */
public interface WorldAPI {
    @GET("/world/banners")
    ListWithError<Banner> getBanners(@Query("limit") Integer bannerNumLimit);

    @FormUrlEncoded
    @POST("/world/like/video/{video_id}")
    ApiModel likeVideo(@Path("video_id") @NonNull String videoId, @Field("account_id") @NonNull String userId);

    @FormUrlEncoded
    @POST("/world/unlike/video/{video_id}")
    ApiModel unlikeVideo(@Path("video_id") @NonNull String videoId, @Field("account_id") @NonNull String userId);

    @GET("/world/tags")
    ListWithError<WorldTag> getWorldTagList(@Query("limit") int limit, @Query("ranking") @Nullable String ranking, @Query("sort") @Nullable
    @Sort String sort);

    @GET("/world/videos/liked")
    ListWithError<TagVideo> getLikedVideos(@Query("account_id") @NonNull String userId, @Query("offset") int offset, @Query("limit") int limit);

    @GET("/world/tags/joined")
    ListWithError<WorldTag> getJoinedWorldTags(@Query("account_id") @NonNull String userId, @Query("type") @NonNull @WorldTag.Type String type, @Query("offset") int offset, @Query("limit") int limit);

    @GET("/world/videos")
    ListWithError<TagVideo> getVideoOfTag(@Query("tag_name") @NonNull String tagName, @Query("offset") int offset, @Query("limit") int limit
            , @Query("account_id") @Nullable String userId, @Query("seed") @Nullable Seed seed);

    @GET("/world/private_videos")
    ListWithError<TagVideo> getPrivateVideoOfTag(@Query("tag_name") @NonNull String tagName, @Query("offset") int offset, @Query("limit") int limit
            , @Query("account_id") @Nullable String userId);

    @GET("/world/tag/suggest")
    ListWithError<WorldTag> getSuggestedTagName(@Query("words") @NonNull String tagName);

    @POST("/world/video")
    @FormUrlEncoded
    Video addVideoToWorld(@Field("account_id") @NonNull String userId, @Field("type") @NonNull @Video.Type String type,
                          @Field("filename") @NonNull String fileName, @Field("tags") @NonNull String tags
    );

    @POST("/world/share")
    @FormUrlEncoded
    ShareInfo shareWorld(@Field("tag_name") String tagName);

    @StringDef({"recommend", "time"})
    @interface Sort {
    }

    class Util {
        public static String getTagsJson(@NonNull List<VideoTag> tags) {
            return GsonFactory.newNamingGson().toJson(tags);
        }
    }
}


