package co.yishun.onemoment.app.api.authentication;

import android.text.TextUtils;

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
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.api.modelv4.ApiModel;
import co.yishun.onemoment.app.api.modelv4.HybrdData;
import co.yishun.onemoment.app.api.modelv4.ListWithErrorV4;
import co.yishun.onemoment.app.api.modelv4.World;
import co.yishun.onemoment.app.api.modelv4.WorldVideo;
import co.yishun.onemoment.app.api.modelv4.WorldVideoListWithErrorV4;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by Jinge on 2016/1/21.
 */
public class OneMomentConverterV4 implements Converter {
    private static final String TAG = "OneMomentConverterV4";
    private final Gson mGson;
    private JsonParser mJsonParser = new JsonParser();

    public OneMomentConverterV4() {
        // we should custom TypeAdapter to fit ApiModel structure
        mGson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
    }

    @Override public Object fromBody(TypedInput body, Type type) throws ConversionException {
        String json = null;
        try {
            json = Util.toString(body.in(), "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        String error;
        String msg;

        JsonElement jsonElement = mJsonParser.parse(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        error = jsonObject.get("error").getAsString();
        msg = jsonObject.get("msg").getAsString();

        ApiModel model = null;
        ListWithErrorV4<? extends ApiModel> models = null;

        Type rawType;
        try {
            rawType = ((ParameterizedType) type).getRawType();
        } catch (Exception e) {
            // ignored, throw when it is not a List.
            rawType = type;
        }

        if (TextUtils.equals(error, "Ok")) {
            JsonObject data = jsonObject.getAsJsonObject("data");
            if (rawType == HybrdData.class) {
                model = mGson.fromJson(data, HybrdData.class);
            } else if (rawType == World.class) {
                model = mGson.fromJson(data.get("world"), World.class);
            } else if (rawType == WorldVideo.class) {
                model = mGson.fromJson(data.get("video"), WorldVideo.class);
            } else if (rawType == List.class || rawType == ListWithErrorV4.class || rawType == WorldVideoListWithErrorV4.class) {
                Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (genericType == WorldVideo.class) {
                    models = new WorldVideoListWithErrorV4<>(mGson.fromJson(data.get("videos"), type));
                    String key = "world";
                    if (!data.has(key)) key = "today";
                    ((WorldVideoListWithErrorV4) models).world = mGson.fromJson(data.get(key), World.class);
                } else if (genericType == World.class) {
                    models = new ListWithErrorV4<>(mGson.fromJson(data.get("worlds"), type));
                }
            }
        } else {
            if (rawType == HybrdData.class) {
                model = new HybrdData();
            } else if (rawType == World.class) {
                model = new World();
            } else if (rawType == WorldVideo.class) {
                model = new WorldVideo();
            } else if (rawType == List.class || rawType == ListWithErrorV4.class) {
                models = new ListWithErrorV4<>(new ArrayList<>(0));
            } else if (rawType == WorldVideoListWithErrorV4.class) {
                models = new WorldVideoListWithErrorV4<>(new ArrayList<>(0));
            }
        }

        if (models != null) {
            models.msg = msg;
            models.error = error;
            return models;
        }

        if (model == null) {
            model = new ApiModel();
        }
        model.error = error;
        model.msg = msg;
        return model;
    }

    @Override public TypedOutput toBody(Object object) {
        String json = mGson.toJson(object);
        LogUtil.i(TAG, object + ", " + json);
        return new JsonTypedOutput(json.getBytes(Charset.forName("UTF-8")), "UTF-8");
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
