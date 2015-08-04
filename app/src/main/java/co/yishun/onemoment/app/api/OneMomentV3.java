package co.yishun.onemoment.app.api;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Type;

import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.config.Constants;
import retrofit.RestAdapter;
import retrofit.converter.ConversionException;
import retrofit.converter.Converter;
import retrofit.converter.GsonConverter;
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

    public static class OneMomentConverter implements Converter {
        private final GsonConverter mGsonConverter;

        public OneMomentConverter() {
            Gson gson = new GsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
            this.mGsonConverter = new GsonConverter(gson);
        }


        @Override public Object fromBody(TypedInput body, Type type) throws ConversionException {
            //TODO encode
            return mGsonConverter.fromBody(body, type);
        }

        @Override public TypedOutput toBody(Object object) {
            //TODO decode
            return mGsonConverter.toBody(object);
        }
    }
}
