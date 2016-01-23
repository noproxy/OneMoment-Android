package co.yishun.onemoment.app.api;

import java.util.List;

import co.yishun.onemoment.app.api.modelv4.HybrdData;
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

    @POST("/world") @FormUrlEncoded
    World createWorld(@Field("name") String worldName, @Field("account_id") String userId);

    @POST("/world/video") @FormUrlEncoded
    WorldVideo createVideo(@Field("world_id") String worldId, @Field("filename") String filename, @Field("account_id") String userId, @Field("tags") String tags);

    @POST("/world/videos") @FormUrlEncoded
    List<WorldVideo> getWorldVideos(@Field("world_id") String worldId, @Field("account_id") String userId, @Field("order") int order, @Field("limit") int limit);
}
