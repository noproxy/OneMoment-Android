package co.yishun.onemoment.app.util;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Created by Carlos on 2016/2/19.
 */
public class GsonFactory {

    public static Gson newGson() {
        return newNamingGson();
    }

    public static Gson newNamingGson() {
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    public static Gson newNormalGson() {
        return new Gson();
    }
}
