package co.yishun.onemoment.app.api.deserializer;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import co.yishun.onemoment.app.api.model.ApiModel;

/**
 * Deserialize json for ApiModel.
 * <p>
 * Created by Carlos on 2015/8/8.
 */
public class ApiModelDeserializer {
    private int code;
    private String msg;

    public JsonElement deserialize(JsonElement json) {
        JsonObject jsonObject = json.getAsJsonObject();
        code = jsonObject.get("code").getAsInt();
        msg = jsonObject.get("msg").getAsString();

        return jsonObject.get("data");
    }

    public <T extends ApiModel> T setModel(T model) {
        model.code = code;
        model.msg = msg;
        return model;
    }
}
