package co.yishun.onemoment.app.api;

import android.support.annotation.Nullable;
import android.util.Log;

import java.io.IOException;
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
    private static final RandomString mStringGenerator = new RandomString(33);

    @Override
    public Response execute(Request request) throws IOException {
        List<Header> headers = request.getHeaders();
        Token token1 = generateOmToken1();
        headers.add(new Header("Om-token1", token1.value()));

        TypedOutput body = request.getBody();
        Log.d("body", String.valueOf(body));
        headers.add(new Header("Om-token2", generateOmToken2(token1, request.getUrl(), request.getBody().toString()).value()));

        Request verifiedRequest = new Request(request.getMethod(), request.getUrl(), headers, request.getBody());
        return super.execute(verifiedRequest);
    }

    private Token generateOmToken1() {
        return new OmToken1(mStringGenerator.nextString());
    }

    private Token generateOmToken2(Token token1, String url, @Nullable String data) {
        return new OmToken2(token1, url, null);
    }

}