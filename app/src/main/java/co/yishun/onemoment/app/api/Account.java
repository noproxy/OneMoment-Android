package co.yishun.onemoment.app.api;

import co.yishun.onemoment.app.model.User;
import retrofit.http.GET;
import retrofit.http.Path;

/**
 * Created by Carlos on 2015/8/4.
 */
public interface Account {


    @GET("/account/account/{account_id}") User getUser(@Path("account_id") String userId);


}
