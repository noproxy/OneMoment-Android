package co.yishun.onemoment.app.api.authentication;

import co.yishun.onemoment.app.BuildConfig;
import co.yishun.onemoment.app.api.model.ApiModel;
import co.yishun.onemoment.app.config.Constants;
import retrofit.RestAdapter;

/**
 * One ApiMoment Server Api version 3.0 <p> Created by Carlos on 2015/8/4.
 */
public class OneMomentV3 {
    public static final String API_BASE_URL = Constants.API_V3_URL;
    public static final String FAKE_RESPONSE = "{\"msg\": \"fake success\",\n    \"code\": -99}";
    private static final RestAdapter mNonCacheRetrofit;
    private static final RestAdapter mCacheOnlyRetrofit;

    static {
        mNonCacheRetrofit = new RestAdapter.Builder()
                .setEndpoint(API_BASE_URL).setLogLevel(BuildConfig.DEBUG
                        ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .setClient(OMCYyzBackup.newNoCacheClient())
                .setRequestInterceptor(request -> request.addHeader("Om-encrypted", "1"))
                .setConverter(new OneMomentConverter(ApiModel.CacheType.NETWORK_ONLY))
                .build();

        mCacheOnlyRetrofit = new RestAdapter.Builder()
                .setEndpoint(API_BASE_URL).setLogLevel(BuildConfig.DEBUG
                        ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .setClient(OMCYyzBackup.getCacheOnlyClient())
                .setRequestInterceptor(request -> request.addHeader("Om-encrypted", "1"))
                .setConverter(new OneMomentConverter(ApiModel.CacheType.CACHE_ONLY))
                .build();
    }

    public static RestAdapter createAdapter() {
        return new RestAdapter.Builder()
                .setEndpoint(API_BASE_URL).setLogLevel(BuildConfig.DEBUG
                        ? RestAdapter.LogLevel.FULL : RestAdapter.LogLevel.BASIC)
                .setClient(OMCYyzBackup.getCacheClient())
                .setRequestInterceptor(request -> request.addHeader("Om-encrypted", "1"))
                .setConverter(new OneMomentConverter(ApiModel.CacheType.NORMAL))
                .build();
    }

    public static RestAdapter getCacheOnlyRetrofit() {
        return mCacheOnlyRetrofit;
    }

    @Deprecated
    public static RestAdapter getNetworkOnlyRetrofit() {
        return mNonCacheRetrofit;
    }
}
