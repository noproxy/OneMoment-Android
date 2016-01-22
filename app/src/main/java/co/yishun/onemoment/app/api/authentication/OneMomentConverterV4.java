package co.yishun.onemoment.app.api.authentication;

import android.text.TextUtils;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.modelv4.ApiModel;
import co.yishun.onemoment.app.api.modelv4.HybrdData;
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
            json = CharStreams.toString(new InputStreamReader(body.in(), Charsets.UTF_8));
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
        ListWithError<? extends ApiModel> models = null;

        Type rawType;
        try {
            rawType = ((ParameterizedType) type).getRawType();
        } catch (Exception e) {
            // ignored, throw when it is not a List.
            rawType = type;
        }

        if (TextUtils.equals(error, "Ok")) {
            if (rawType == HybrdData.class) {
                JsonObject data = jsonObject.get("data").getAsJsonObject();

                model = mGson.fromJson(data, HybrdData.class);
                LogUtil.d(TAG, model.msg);
            }
        }

        if (model != null) {
            model.error = error;
            model.msg = msg;
            return model;
        }

        return null;
    }

    @Override public TypedOutput toBody(Object object) {
        String json = mGson.toJson(object);
        LogUtil.i(TAG, object + ", " + json);
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