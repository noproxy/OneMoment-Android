package co.yishun.onemoment.app.api;

import android.support.annotation.Nullable;

import co.yishun.onemoment.app.api.modelv4.HybrdData;
import co.yishun.onemoment.app.api.modelv4.ListWithErrorV4;
import co.yishun.onemoment.app.api.modelv4.ShareInfo;
import co.yishun.onemoment.app.api.modelv4.UploadToken;
import co.yishun.onemoment.app.api.modelv4.World;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.api.modelv4.WorldVideoListWithErrorV4;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by Jinge on 2016/1/21.
 */
public interface APIV4 {
    @GET("/hybrd/update")
    HybrdData getHybrdData(@Query("name") String name);

    @POST("/world")
    @FormUrlEncoded
    World createWorld(
            @Field("name") String worldName,
            @Field("account_id") String userId);

    @POST("/world/delete")
    @FormUrlEncoded
    World deleteWorld(@Field("world_id") String worldId,
                      @Field("account_id") String userId);

    @POST("/world/video")
    @FormUrlEncoded
    WorldVideo createWorldVideo(
            @Field("world_id") String worldId,
            @Field("filename") String filename,
            @Field("account_id") String userId,
            @Field("tags") String tags);

    @GET("/world/videos")
    WorldVideoListWithErrorV4<WorldVideo> getWorldVideos(
            @Query("world_id") String worldId,
            @Query("account_id") String userId,
            @Query("order") int order,
            @Query("limit") int limit);

    @POST("/world/today")
    @FormUrlEncoded
    WorldVideo createTodayVideo(
            @Field("filename") String filename,
            @Field("account_id") String userId,
            @Field("tags") String tags);

    @GET("/world/todays")
    ListWithErrorV4<World> getTodayWorlds(
            @Query("ranking") int ranking,
            @Query("limit") int limit);

    @GET("/world/today")
    WorldVideoListWithErrorV4<WorldVideo> getTodayVideos(
            @Query("name") String name,
            @Query("offset") int offset,
            @Query("limit") int limit);

    @POST("/world/share_today")
    @FormUrlEncoded
    ShareInfo shareToday(
            @Field("name") String name,
            @Field("account_id") String userId);

    @POST("/world/share")
    @FormUrlEncoded
    ShareInfo shareWorld(
            @Field("world_id") String worldId,
            @Field("account_id") String userId);

    @GET("/misc/upload_token")
    UploadToken getUploadToken(
            @Query("filename") @Nullable String filename);

    @GET("/misc/upload_token")
    UploadToken getUploadToken(
            @Query("filename") @Nullable String filename,
            @Query("type") String type);
}
