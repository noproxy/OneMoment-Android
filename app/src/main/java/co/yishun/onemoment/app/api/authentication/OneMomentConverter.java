package co.yishun.onemoment.app.api.authentication;

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
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.api.model.ApiMoment;
import co.yishun.onemoment.app.api.model.Banner;
import co.yishun.onemoment.app.api.model.Domain;
import co.yishun.onemoment.app.api.model.Link;
import co.yishun.onemoment.app.api.model.ListWithError;
import co.yishun.onemoment.app.api.model.Seed;
import co.yishun.onemoment.app.api.model.ShareInfo;
import co.yishun.onemoment.app.api.model.SplashCover;
import co.yishun.onemoment.app.api.model.TagVideo;
import co.yishun.onemoment.app.api.model.UploadToken;
import co.yishun.onemoment.app.api.model.User;
import co.yishun.onemoment.app.api.model.Video;
import co.yishun.onemoment.app.api.model.WorldTag;
import java8.util.stream.StreamSupport;
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
     */ public Object fromBody(TypedInput body, Type type) throws ConversionException {
        String json = OneMomentEncoding.decode(body);
        int code;
        String msg;
        int errorCode = 1;

        JsonElement jsonElement = mJsonParser.parse(json);
        JsonObject jsonObject = jsonElement.getAsJsonObject();
        code = jsonObject.get("code").getAsInt();
        msg = jsonObject.get("msg").getAsString();
        try {
            errorCode = jsonObject.get("error_code").getAsInt();
        } catch (Exception e) {
            LogUtil.v(TAG, "no error code, catch. ");
        }


        ApiModel model = null;
        ListWithError<? extends ApiModel> models = null;

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
            } else if (rawType == Link.class | rawType == UploadToken.class | rawType == Domain.class |
                    rawType == ShareInfo.class | rawType == SplashCover.class) {
                JsonObject data = jsonObject.get("data").getAsJsonObject();
                model = mGson.fromJson(data, type);
            } else if (rawType == Video.class) {
                JsonObject data = jsonObject.get("data").getAsJsonObject();
                model = mGson.fromJson(data.get("video"), type);
            } else if (rawType == List.class || rawType == ListWithError.class) {
                Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
                if (genericType == Banner.class) {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    models = new ListWithError<>(mGson.fromJson(data.get("banners").getAsJsonArray(), type));
                } else if (genericType == ApiMoment.class) {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    models = new ListWithError<>(mGson.<List<ApiMoment>>fromJson(data.get("videos").getAsJsonArray(), type));
                } else if (genericType == Video.class) {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    List<Video> videos = mGson.fromJson(data.get("videos").getAsJsonArray(), type);
                    Domain domain = mGson.fromJson(data, Domain.class);
                    StreamSupport.stream(videos).filter(v -> v != null).forEach(video -> video.domain = domain);
                    models = new ListWithError<>(videos);
                } else if (genericType == WorldTag.class) {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    List<WorldTag> tags = mGson.fromJson(data.get("tags").getAsJsonArray(), type);
                    Domain domain = mGson.fromJson(data, Domain.class);
                    StreamSupport.stream(tags).filter(v -> v != null).forEach(tag -> tag.domain = domain);
                    models = new ListWithError<>(tags);
                } else if (genericType == TagVideo.class) {
                    JsonObject data = jsonObject.get("data").getAsJsonObject();
                    List<TagVideo> tags = mGson.fromJson(data.get("videos").getAsJsonArray(), type);
                    Domain domain = mGson.fromJson(data, Domain.class);
                    StreamSupport.stream(tags).filter(v -> v != null).forEach(tag -> tag.domain = domain);
                    Seed seed = mGson.fromJson(data, Seed.class);
                    StreamSupport.stream(tags).filter(v -> v != null).forEach(tag -> tag.seed = seed);

                    models = new ListWithError<>(tags);
                } else {
                    models = new ListWithError<>(new ArrayList<>());
                    LogUtil.e(TAG, "unknown generic type, json: " + json);
                }
            }
        } else {
            if (rawType == ShareInfo.class) {
                model = new ShareInfo();
            } else if (rawType == User.class) {
                model = new User();
            } else if (rawType == Link.class) {
                model = new Link();
            } else if (rawType == UploadToken.class) {
                model = new UploadToken();
            } else if (rawType == Domain.class) {
                model = new Domain();
            } else if (rawType == Video.class) {
                model = new Video();
            } else if (rawType == SplashCover.class) {
                model = new SplashCover();
            } else if (rawType == List.class || rawType == ListWithError.class) {
                models = new ListWithError<>(new ArrayList<>(0));
            }
        }
        if (models != null) {
            models.code = code;
            models.errorCode = errorCode;
            models.msg = msg;
            return models;
        }
        if (model == null) {
            model = new ApiModel();
        }
        model.msg = msg;
        model.code = code;
        model.errorCode = errorCode;
        return model;
    }

    /**
     * Just convert Object to json but not encrypted, do it later in Client.
     */
    @Override public TypedOutput toBody(Object object) {
        // will be encoded in OneMomentClient, so don't encode here
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