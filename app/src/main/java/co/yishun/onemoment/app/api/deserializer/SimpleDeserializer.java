package co.yishun.onemoment.app.api.deserializer;

import android.support.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;

import java.lang.reflect.Type;

import co.yishun.onemoment.app.api.model.ApiModel;

/**
 * This deserializer just return ApiModel contains code and msg.
 * <p>
 * Created by Carlos on 2015/8/8.
 */
public class SimpleDeserializer extends ApiModelDeserializer<ApiModel> {
    @NonNull
    @Override
    protected ApiModel success(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new ApiModel();
    }

    @NonNull
    @Override
    protected ApiModel fail(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
        return new ApiModel();
    }
}
