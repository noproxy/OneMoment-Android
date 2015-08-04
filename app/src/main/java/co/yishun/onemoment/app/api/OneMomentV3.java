package co.yishun.onemoment.app.api;

import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.config.Constants;
import retrofit.RestAdapter;

/**
 * One Moment Server Api version 3.0
 * <p>
 * Created by Carlos on 2015/8/4.
 */
public class OneMomentV3 {
    public static final String API_BASE_URL = Constants.API_V3_URL;

    RestAdapter createAdapter() {
        return new RestAdapter.Builder().setEndpoint(API_BASE_URL)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .setClient(new OneMomentClient())
//                .setConverter(new Converter() {
//                    @Override
//                    public Object fromBody(TypedInput body, Type type) throws ConversionException {
//
//                        return null;
//                    }
//
//                    @Override
//                    public TypedOutput toBody(Object object) {
//                        return null;
//                    }
//                })
                .build();
    }
}
