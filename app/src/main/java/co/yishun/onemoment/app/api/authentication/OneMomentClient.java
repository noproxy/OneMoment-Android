package co.yishun.onemoment.app.api.authentication;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import co.yishun.onemoment.app.data.FileUtil;
import retrofit.client.OkClient;
import retrofit.mime.TypedInput;

/**
 * Created by Jinge on 2016/1/27.
 */
public abstract class OneMomentClient extends OkClient {

    public static final int CACHE_SIZE = 1024 * 1024 * 10;
    protected static final OkHttpClient mOkHttpClient = new OkHttpClient();

    protected OneMomentClient() {
        super(mOkHttpClient);
    }

    public static void setUpCache(Context context) {
        Cache cache = new Cache(FileUtil.getCacheDirectory(context, true), CACHE_SIZE);
        mOkHttpClient.setCache(cache);
    }

    protected abstract byte[] getFakeBody();

    protected abstract byte[] encode(byte[] body) throws IOException;

    protected class FakeTypeInput implements TypedInput {
        private byte[] mFakeBody = getFakeBody();

        @Override public String mimeType() {
            return "text/plain charset=UTF-8";
        }

        @Override public long length() {
            return mFakeBody.length;
        }

        @Override public InputStream in() throws IOException {
            // we should encrypt the fake body
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            outputStream.write(mFakeBody);
            return new ByteArrayInputStream(encode(mFakeBody));
        }
    }

}