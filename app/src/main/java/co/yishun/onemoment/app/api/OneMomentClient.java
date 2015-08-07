package co.yishun.onemoment.app.api;

import android.support.annotation.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import retrofit.client.Header;
import retrofit.client.OkClient;
import retrofit.client.Request;
import retrofit.client.Response;
import retrofit.mime.TypedOutput;


/**
 * Custom client to replace request with double token verified one.
 * <p>
 * Created by Carlos on 2015/8/5.
 */
class OneMomentClient extends OkClient {
    public static final String TAG = "OneMomentClient";
    private static final RandomString mStringGenerator = new RandomString(33);

    @Override
    public Response execute(Request request) throws IOException {
        List<Header> immutableHeaders = request.getHeaders();// this list is immutable
        ArrayList<Header> headers = new ArrayList<>(immutableHeaders);
        Token token1 = generateOmToken1();
        headers.add(new Header("Om-token1", token1.value()));
        headers.add(new Header("Om-token2", generateOmToken2(token1, request.getUrl(), request.getBody()).value()));
        Request verifiedRequest = new Request(request.getMethod(), request.getUrl(), headers, request.getBody() == null ? null : new OneMomentTypedOut(request.getBody()));// be null if method is GET
        return super.execute(verifiedRequest);
    }

    private Token generateOmToken1() {
        return new OmToken1(mStringGenerator.nextString());
    }

    private Token generateOmToken2(Token token1, String url, @Nullable TypedOutput data) throws IOException {
        return new OmToken2(token1, url, data);
    }

    private static class OneMomentTypedOut implements TypedOutput {
        private final TypedOutput mTypedOutput;

        public OneMomentTypedOut(TypedOutput typedOutput) {
            this.mTypedOutput = typedOutput;
        }

        @Override public String fileName() {
            return mTypedOutput.fileName();
        }

        @Override public String mimeType() {
            return "application/json; charset=UTF-8";
        }

        @Override public long length() {
            return mTypedOutput.length();
        }

        @Override public void writeTo(OutputStream out) throws IOException {

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            mTypedOutput.writeTo(outputStream);
            ByteArrayOutputStream encodingStream = OneMomentEncoding.encodingStream(outputStream);
            encodingStream.writeTo(out);

        }
    }


}