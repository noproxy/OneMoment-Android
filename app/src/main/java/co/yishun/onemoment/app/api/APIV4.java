package co.yishun.onemoment.app.api;

import co.yishun.onemoment.app.api.modelv4.HybrdData;
import co.yishun.onemoment.app.api.modelv4.ListWithErrorV4;
import co.yishun.onemoment.app.api.modelv4.TodayWorld;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import retrofit.http.Field;
import retrofit.http.FormUrlEncoded;
import retrofit.http.GET;
import retrofit.http.POST;
import retrofit.http.Query;

/**
 * Created by Jinge on 2016/1/21.
 */
public interface APIV4 {
    @GET("/hybrd/update") HybrdData getHybrdData(@Query("name") String name);

    @POST("/world") @FormUrlEncoded co.yishun.onemoment.app.api.modelv4.World createWorld(
            @Field("name") String worldName,
            @Field("account_id") String userId);

    @POST("/world/video") @FormUrlEncoded WorldVideo createWorldVideo(
            @Field("world_id") String worldId,
            @Field("filename") String filename,
            @Field("account_id") String userId, @Field("tags") String tags);

    @GET("/world/videos") ListWithErrorV4<WorldVideo> getWorldVideos(
            @Query("world_id") String worldId,
            @Query("account_id") String userId,
            @Query("order") int order,
            @Query("limit") int limit);

    @POST("/world/today") @FormUrlEncoded WorldVideo createTodayVideo(
            @Field("filename") String filename,
            @Field("account_id") String userId);

    @GET("/world/todays") ListWithErrorV4<TodayWorld> getTodayWorlds(
            @Query("ranking") int ranking,
            @Query("limit") int limit);

    @GET("world/today") WorldVideo getTodayVideo(
            @Query("name") String name,
            @Query("offset") int offset,
            @Query("limit") int limit);
}
