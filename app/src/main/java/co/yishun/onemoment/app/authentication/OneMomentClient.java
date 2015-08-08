package co.yishun.onemoment.app.authentication;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import co.yishun.onemoment.app.Util;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedOutput;


/**
 * Custom client to replace request with double token verified one. And encrypted body if have.
 * <p>
 * Created by Carlos on 2015/8/5.
 */
public class OneMomentClient extends OkClient {
    public static final String TAG = "OneMomentClient";
    public static final int DEFAULT_EXPIRE_TIME = 10;


    @Override
    public Response execute(Request request) throws IOException {
        List<Header> immutableHeaders = request.getHeaders();// this list is immutable
        ArrayList<Header> headers = new ArrayList<>(immutableHeaders);

        Token token1 = generateOmToken1();
        Log.i(TAG, token1.toString());

        headers.add(new Header("Om-token1", token1.value()));
        long expiredTime = Util.unixTimeStamp() + DEFAULT_EXPIRE_TIME;

        Token token2 = generateOmToken2(token1, request.getUrl(), request.getBody(), expiredTime);
        Log.i(TAG, token2.toString());

        headers.add(new Header("Om-token2", token2.value()));
        headers.add(new Header("Om-et", String.valueOf(expiredTime)));
        headers.add(new Header("Om-tz", TimeZone.getDefault().getID()));

        Request verifiedRequest = new Request(request.getMethod(), request.getUrl(), headers, request.getBody() == null ? null : new OneMomentTypedOut(request.getBody()));// be null if method is GET
        return super.execute(verifiedRequest);
    }

    private Token generateOmToken1() {
        return new OmToken1();
    }

    private Token generateOmToken2(Token token1, String url, @Nullable TypedOutput data, long expireTime) throws IOException {
        return new OmToken2(token1, url, data, expireTime);
    }

    private static class OneMomentTypedOut implements TypedOutput {
        private final TypedOutput mTypedOutput;

        public OneMomentTypedOut(TypedOutput typedOutput) {
            this.mTypedOutput = typedOutput;
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
            return mTypedOutput.length();
        }

        @Override
        public void writeTo(OutputStream out) throws IOException {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mTypedOutput.writeTo(outputStream);
            ByteArrayOutputStream encodingStream = OneMomentEncoding.encodingStream(outputStream);
            encodingStream.writeTo(out);

        }
    }


}