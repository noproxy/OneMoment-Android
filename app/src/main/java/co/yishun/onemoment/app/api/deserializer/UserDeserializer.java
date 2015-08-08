package co.yishun.onemoment.app.api.deserializer;

import android.support.annotation.NonNull;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;

import co.yishun.onemoment.app.api.model.User;

/**
 * Created by Carlos on 2015/8/8.
 */
public class UserDeserializer extends ApiModelDeserializer<User> {


    @NonNull
    @Override
    protected User success(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        JsonElement jsonElement = json.getAsJsonObject().get("account");
        return new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(jsonElement, typeOfT);
    }

    @NonNull
    @Override
    protected User fail(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new User();
    }
}
