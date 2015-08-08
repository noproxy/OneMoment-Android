package co.yishun.onemoment.app.api.deserializer;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

import java.lang.reflect.Type;

import co.yishun.onemoment.app.api.model.ApiModel;


/**
 * Created by Carlos on 2015/8/8.
 */
public class SimpleDeserializer<T extends ApiModel> extends ApiModelDeserializer implements JsonDeserializer<T> {
    @Override
    public T deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        json = super.deserialize(json);
        final String child = childNodeName();
        if (child != null)
            json = json.getAsJsonObject().get(child);
        T model = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create().fromJson(json, typeOfT);
        return super.setModel(model);
    }

    /***
     * @return node name of target Object. Null if target is just hte JsonElement.
     */
    String childNodeName() {
        return null;
    }

}
