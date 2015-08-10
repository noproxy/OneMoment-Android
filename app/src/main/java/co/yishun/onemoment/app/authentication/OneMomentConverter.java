package co.yishun.onemoment.app.authentication;

import android.util.Log;

import com.google.common.base.Charsets;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.Link;
import co.yishun.onemoment.app.api.model.Moment;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.WorldTag;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Convert between body and ApiModel Object.
 * <p>
 * Created by Carlos on 2015/8/8.
 */
public class OneMomentConverter implements Converter {
    private static final String TAG = "OneMomentConverter";
    private final Gson mGson;
    private JsonParser mJsonParser = new JsonParser();

    public OneMomentConverter() {
        // we should custom TypeAdapter to fit ApiModel structure
        mGson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override
    /**
     * Decode the encrypted body, then use Gson convert to Object
     */
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        String json = OneMomentEncoding.decode(body);
        int code;
        String msg;
        JsonElement jsonElement = mJsonParser.parse(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        code = jsonObject.get("code").getAsInt();
        msg = jsonObject.get("msg").getAsString();


        ApiModel model = null;
        List<? extends ApiModel> models = null;

        Type rawType;
        try {
            rawType = ((ParameterizedType) type).getRawType();
        } catch (Exception e) {
            // ignored, throw when it is not a List.
            rawType = type;
        }


        if (code == 1) {
            if (rawType == User.class) {
                JsonObject data = jsonObject.get("data").getAsJsonObject();
                model = mGson.fromJson(data.get("account"), type);
            } else if (rawType == Link.class) {
                JsonObject data = jsonObject.get("data").getAsJsonObject();
                models = mGson.fromJson(data, type);
            } else if (rawType == Video.class) {
                JsonObject data = jsonObject.get("data").getAsJsonObject();
                model = mGson.fromJson(data.get("video"), type);
            } else if (rawType == List.class) {
                Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (genericType == Banner.class) {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    models = mGson.fromJson(data.get("banners").getAsJsonArray(), type);
                } else if (genericType == Moment.class || genericType == Video.class) {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    models = mGson.fromJson(data.get("videos").getAsJsonArray(), type);
                } else if (genericType == WorldTag.class) {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    models = mGson.fromJson(data.get("tags").getAsJsonArray(), type);
                } else {
                    models = new ArrayList<>();
                    Log.e(TAG, "unknown generic type, json: " + json);
                }
            }
        } else {
            if (rawType == User.class) {
                model = new User();
            } else if (rawType == Link.class) {
                model = new Link();
            } else if (rawType == Video.class) {
                model = new Video();
            } else if (rawType == List.class) {
                models = new ArrayList<>(0);
            }
        }
        if (models != null) {
            return models;
        }
        if (model == null) {
            model = new ApiModel();
        }
        model.msg = msg;
        model.code = code;
        return model;
    }

    /**
     * Just convert Object to json but not encrypted, do it later in Client.
     */
    @Override
    public TypedOutput toBody(Object object) {
        // will be encoded in OneMomentClient, so don't encode here
        String json = mGson.toJson(object);
        Log.i(TAG, object + ", " + json);
        return new JsonTypedOutput(json.getBytes(Charsets.UTF_8), Charsets.UTF_8.name());
    }

    /**
     * Copied from retrofit.converter.JsonTypedOutput.
     */
    private static class JsonTypedOutput implements TypedOutput {
        private final byte[] jsonBytes;
        private final String mimeType;

        JsonTypedOutput(byte[] jsonBytes, String encode) {
            this.jsonBytes = jsonBytes;
            this.mimeType = "application/json; charset=" + encode;
        }

        @Override
        public String fileName() {
            return null;
        }

        @Override
        public String mimeType() {
            return mimeType;
        }

        @Override
        public long length() {
            return jsonBytes.length;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            out.write(jsonBytes);
        }
    }
}