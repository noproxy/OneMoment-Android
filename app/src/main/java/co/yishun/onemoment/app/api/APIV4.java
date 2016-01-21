package co.yishun.onemoment.app.api;

import co.yishun.onemoment.app.api.modelv4.HybrdData;
import retrofit.http.GET;
import retrofit.http.Query;

/**
 * Created by Jinge on 2016/1/21.
 */
public interface APIV4 {
    @GET("/hybrd/update") HybrdData getHybrdData(@Query("name") String name);
}
