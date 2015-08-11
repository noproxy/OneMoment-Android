package co.yishun.onemoment.app.authentication;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.common.base.Charsets;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;

import co.yishun.onemoment.app.Util;
import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedInput;
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
        TypedOutput body = request.getBody() == null ? null : new OneMomentTypedOut(request.getBody());


        headers.add(new Header("Om-token1", token1.value()));
        long expiredTime = Util.unixTimeStamp() + DEFAULT_EXPIRE_TIME;

        Token token2 = generateOmToken2(token1, request.getUrl(), body, expiredTime);
        Log.i(TAG, token2.toString());

        headers.add(new Header("Om-token2", token2.value()));
        headers.add(new Header("Om-et", String.valueOf(expiredTime)));
        headers.add(new Header("Om-tz", TimeZone.getDefault().getID()));

        Request verifiedRequest = new Request(request.getMethod(), request.getUrl(), headers, body);// be null if method is GET
        Response response = super.execute(verifiedRequest);
        int statusCode = response.getStatus();
        // fake 200 response to log error and store in ApiModel
        if (statusCode < 200 || statusCode >= 300) {// error
            new Response(response.getUrl(), 200, "OK", response.getHeaders(), new FakeTypeInput());
            Log.e(TAG, "http error! " + statusCode + " " + response.getReason());
        }
        return response;
    }

    private Token generateOmToken1() {
        return new OmToken1();
    }

    private Token generateOmToken2(Token token1, String url, @Nullable TypedOutput data, long expireTime) throws IOException {
        int urlEndIndex = url.indexOf('?');
        if (urlEndIndex == -1) urlEndIndex = url.length();
        return new OmToken2(token1, url.substring(0, urlEndIndex), data, expireTime);
    }

    private static class OneMomentTypedOut implements TypedOutput {
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
            if (mData == null) throw new IOException(mException);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(mData);
            stream.writeTo(out);
        }
    }

    private static class FakeTypeInput implements TypedInput {
        private byte[] mFakeBody = "{\"msg\": \"fake success\",\n    \"code\": -99}".getBytes(Charsets.UTF_8);

        @Override
        public String mimeType() {
            return "text/plain charset=UTF-8";
        }

        @Override
        public long length() {
            return mFakeBody.length;
        }

        @Override
        public InputStream in() throws IOException {
            return new ByteArrayInputStream(mFakeBody);
        }
    }
}