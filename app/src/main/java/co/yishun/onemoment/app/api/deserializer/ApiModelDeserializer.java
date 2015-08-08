package co.yishun.onemoment.app.api.deserializer;

import android.support.annotation.NonNull;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.config.Constants;


/**
 * Created by Carlos on 2015/8/8.
 */
public abstract class ApiModelDeserializer<T extends ApiModel> implements JsonDeserializer<T> {
    private int code;
    private String msg;

    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        json = deserialize(json);
        if (code == Constants.CODE_SUCCESS) {
            return setModel(success(json, typeOfT, context));
        } else {
            return setModel(fail(json, typeOfT, context));
        }
    }

    @NonNull
    protected abstract T success(JsonElement json, Type typeOfT, JsonDeserializationContext context);

    @NonNull
    protected abstract T fail(JsonElement json, Type typeOfT, JsonDeserializationContext context);

    public JsonElement deserialize(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        code = jsonObject.get("code").getAsInt();
        msg = jsonObject.get("msg").getAsString();
        return jsonObject.get("data");
    }

    public T setModel(T model) {
        model.code = code;
        model.msg = msg;
        return model;
    }
}
