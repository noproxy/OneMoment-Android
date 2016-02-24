package co.yishun.onemoment.app.api.authentication;

import android.support.annotation.Nullable;

import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.api.model.ApiModel;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedOutput;

import static co.yishun.onemoment.app.LogUtil.i;


/**
 * Custom client to replace request with double token verified one. And encrypted body if have. <p>
 * Created by Carlos on 2015/8/5.
 */
public class OneMomentClientV3 extends OneMomentClient {
    public static final String TAG = "OneMomentClientV3";
    public static final int DEFAULT_EXPIRE_TIME = 10;

    private static final OneMomentClientV3 mCacheInstance = new OneMomentClientV3(mCacheOkHttpClient, ApiModel.CacheType.NORMAL);
    private static final OneMomentClientV3 mCacheOnlyInstance = new OneMomentClientV3(mCacheOnlyOkHttpClient, ApiModel.CacheType.CACHE_ONLY);

    private OneMomentClientV3(OkHttpClient client, ApiModel.CacheType cacheType) {
        super(client, cacheType);
    }

    public static OneMomentClientV3 getCacheClient() {
        return mCacheInstance;
    }

    public static OneMomentClientV3 getCacheOnlyClient() {
        return mCacheOnlyInstance;
    }

    public static OneMomentClientV3 newNoCacheClient() {
        return new OneMomentClientV3(new OkHttpClient(), ApiModel.CacheType.NORMAL);
    }

    @Override
    protected byte[] getFakeBody() {
        return OneMomentV3.FAKE_RESPONSE.getBytes(Charset.forName("UTF-8"));
    }

    @Override
    protected byte[] encode(byte[] body) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(body);
        return OneMomentEncoding.encodingStream(outputStream);
    }

    @Override
    public Response execute(Request request) throws IOException {
        List<Header> immutableHeaders = request.getHeaders();// this list is immutable
        ArrayList<Header> headers = new ArrayList<>(immutableHeaders);
        headers.add(new Header("User-Agent", Util.getUserAgent()));

        Token token1 = generateOmToken1();
        i(TAG, token1.toString());
        TypedOutput body = request.getBody() == null ? null : new OneMomentTypedOut(request.getBody());


        headers.add(new Header("Om-token1", token1.value()));
        long expiredTime = Util.unixTimeStamp() + DEFAULT_EXPIRE_TIME;

        Token token2 = generateOmToken2(token1, request.getUrl(), body, expiredTime);
        i(TAG, token2.toString());

        headers.add(new Header("Om-token2", token2.value()));
        headers.add(new Header("Om-et", String.valueOf(expiredTime)));
        headers.add(new Header("Om-tz", TimeZone.getDefault().getID()));

        switch (mCacheType) {
            case CACHE_ONLY:
                // one month cached
                headers.add(new Header("Cache-Control", "max-age=" + 60 * 60 * 24 * 30 + ",max-stale"));
                break;
            case NORMAL:
                // 5 seconds
                headers.add(new Header("Cache-Control", "max-age=" + 5 + ",max-stale=" + 5));// token expired in 60 minutes
                break;
            default:
                break;
        }


        Request verifiedRequest = new Request(request.getMethod(), request.getUrl(), headers, body);// be null if method is GET

        HostnameVerifier hostnameVerifier = org.apache.http.conn.ssl.SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER;
        HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);

        try {
            Response response = super.execute(verifiedRequest);
            int statusCode = response.getStatus();
            // fake 200 response to log error and store in ApiModel
            if (statusCode < 200 || statusCode >= 300) {// error
                response = new Response(response.getUrl(), 200, "OK", response.getHeaders(), new FakeTypeInput());
                i(TAG, "http error! " + statusCode + " " + response.getReason());
            }
            return response;
        } catch (Exception e) {
            LogUtil.e(TAG, "Exception in network request ! ", e);
            return new Response(request.getUrl(), 200, "OK", headers, new FakeTypeInput());
        }
    }

    private Token generateOmToken1() {
        return new OmToken1();
    }

    private Token generateOmToken2(Token token1, String url,
                                   @Nullable TypedOutput data, long expireTime) throws IOException {
        int urlEndIndex = url.indexOf('?');
        if (urlEndIndex == -1)
            urlEndIndex = url.length();
        return new OmToken2(token1, url.substring(0, urlEndIndex), data, expireTime);
    }

    private class OneMomentTypedOut implements TypedOutput {
        private final TypedOutput mTypedOutput;
        private byte[] mData;
        private IOException mException;

        public OneMomentTypedOut(TypedOutput typedOutput) {
            this.mTypedOutput = typedOutput;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            try {
                mTypedOutput.writeTo(outputStream);
                mData = OneMomentEncoding.encodingStream(outputStream);
            } catch (IOException e) {
                mException = e;
                e.printStackTrace();
            }

        }

        @Override
        public String fileName() {
            return mTypedOutput.fileName();
        }

        @Override
        public String mimeType() {
            return "text/plain; charset=UTF-8";
        }

        @Override
        public long length() {
            return mData.length;
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {
            if (mData == null)
                throw new IOException(mException);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(mData);
            stream.writeTo(out);
        }
    }

}