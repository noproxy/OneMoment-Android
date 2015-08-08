package co.yishun.onemoment.app.authentication;

import com.google.common.base.Charsets;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Type;

import co.yishun.onemoment.app.api.deserializer.SimpleDeserializer;
import co.yishun.onemoment.app.api.model.User;
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

    public OneMomentConverter() {
        // we should custom TypeAdapter to fit ApiModel structure
        mGson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .registerTypeAdapter(User.class, new SimpleDeserializer<>()).create();
    }

    @Override
    /**
     * Decode the encrypted body, then use Gson convert to Object
     */
    public Object fromBody(TypedInput body, Type type) throws ConversionException {
        String json = OneMomentEncoding.decode(body);
        return mGson.fromJson(json, type);
    }

    /**
     * Just convert Object to json but not encrypted, do it later in Client.
     */
    @Override
    public TypedOutput toBody(Object object) {
        // will be encoded in OneMomentClient, so don't encode here
        return new JsonTypedOutput(mGson.toJson(object).getBytes(Charsets.UTF_8), Charsets.UTF_8.name());
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