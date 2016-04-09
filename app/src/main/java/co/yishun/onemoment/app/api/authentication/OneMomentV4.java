package co.yishun.onemoment.app.api.authentication;

import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.OMApplication;
import co.yishun.onemoment.app.config.Constants;
import retrofit.RestAdapter;

/**
 * One ApiMoment Server Api version 4.0 <p> Created by Jinge on 2016/1/21.
 */
public class OneMomentV4 {
    public static final String API_BASE_URL = Constants.API_V4_URL;
    public static final String FAKE_RESPONSE = "{\"msg\": \"fake success\",\n    \"error\": \"error:fake\"}";

    public static RestAdapter createAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(API_BASE_URL)
                .setLogLevel(BuildConfig.DEBUG ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .setClient(OneMomentClientV4.getCacheClient())
                .setRequestInterceptor(request -> {
                    request.addHeader("Om-encrypted", "1");
                    String c = OMApplication.getChannel();
                    if (c != null) {
                        request.addHeader(Constants.MARKER_HEADER, c);
                    }
                })
                .setConverter(new OneMomentConverterV4())
                .build();
    }
}
