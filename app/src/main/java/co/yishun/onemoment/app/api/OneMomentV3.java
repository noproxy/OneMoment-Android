package co.yishun.onemoment.app.api;

import com.google.common.base.Charsets;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.config.Constants;
import retrofit.RestAdapter;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * One Moment Server Api version 3.0
 * <p>
 * Created by Carlos on 2015/8/4.
 */
public class OneMomentV3 {
    public static final String API_BASE_URL = Constants.API_V3_URL;

    public static RestAdapter createAdapter() {
        return new RestAdapter.Builder().setEndpoint(API_BASE_URL)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .setClient(new OneMomentClient())
                .setConverter(new OneMomentConverter())
                .build();
    }

    private static class OneMomentConverter implements Converter {
        private final Gson mGson;

        public OneMomentConverter() {
            mGson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        }


        @Override public Object fromBody(TypedInput body, Type type) throws ConversionException {
            String json = OneMomentEncoding.decode(body);
            return mGson.fromJson(json, type);
        }

        @Override public TypedOutput toBody(Object object) {
            // will be encoded in OneMomentClient, so don't encode here
            return new JsonTypedOutput(mGson.toJson(object).getBytes(Charsets.UTF_8), Charsets.UTF_8.name());
        }

        private static class JsonTypedOutput implements TypedOutput {
            private final byte[] jsonBytes;
            private final String mimeType;

            JsonTypedOutput(byte[] jsonBytes, String encode) {
                this.jsonBytes = jsonBytes;
                this.mimeType = "application/json; charset=" + encode;
            }

            @Override public String fileName() {
                return null;
            }

            @Override public String mimeType() {
                return mimeType;
            }

            @Override public long length() {
                return jsonBytes.length;
            }

            @Override public void writeTo(OutputStream out) throws IOException {
                out.write(jsonBytes);
            }
        }


    }
}
