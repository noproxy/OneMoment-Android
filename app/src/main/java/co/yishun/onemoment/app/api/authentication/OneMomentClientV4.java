package co.yishun.onemoment.app.api.authentication;

import android.util.Base64;

import com.qiniu.android.dns.util.Hex;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.Util;
import retrofit.client.Header;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedOutput;

import static co.yishun.onemoment.app.LogUtil.i;

/**
 * Created by Jinge on 2016/1/26.
 */
public class OneMomentClientV4 extends OneMomentClient {

    public static final int DEFAULT_EXPIRE_TIME = 15;
    private static final String TAG = "OneMomentClientV4";
    protected static OneMomentClientV4 mInstance = new OneMomentClientV4();

    private OneMomentClientV4() {
        super();
    }

    public static OneMomentClientV4 getCachedClient() {
        return mInstance;
    }

    public static String HmacSHA256Encode(String key, String data) {
        String result = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            result = Hex.encodeHexString(sha256_HMAC.doFinal(data.getBytes("UTF-8")));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return result;
    }

    public static String getAuthStr() {
        String authUser = "android_d547a48206418eba571875c8395309d4";
        String authKey = "cc60e89190e7cddc1c2d405c9b482f290e05532670330f58f85a6488bf7ab751";
        String staticPw = "c73d0a8c053199ecb9714c225bbb85b1f19e6b5088c6f73540de7debcfc0b586";
        int expireTime = (int) (Util.unixTimeStamp() + DEFAULT_EXPIRE_TIME);

        String username = authUser + "-" + expireTime;
        String password = HmacSHA256Encode(authKey, staticPw + "-" + username);
        String authStr = username + ":" + password;
        String authStrbase64 = Base64.encodeToString(authStr.getBytes(), Base64.NO_WRAP);
        LogUtil.d(TAG, expireTime + "  " + authStrbase64);
        return authStrbase64.trim();
    }

    @Override protected byte[] getFakeBody() {
        return OneMomentV4.FAKE_RESPONSE.getBytes(Charset.forName("UTF-8"));
    }

    @Override protected byte[] encode(byte[] body) throws IOException {
        return body;
    }

    @Override public Response execute(Request request) throws IOException {
        List<Header> immutableHeaders = request.getHeaders();// this list is immutable
        ArrayList<Header> headers = new ArrayList<>(immutableHeaders);

        TypedOutput body = request.getBody();

        headers.add(new Header("Authentication", "Basic " + getAuthStr()));
        LogUtil.d(TAG, System.getProperty("http.agent"));
        headers.add(new Header("UserAgent", System.getProperty("http.agent")));

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
}
