package co.yishun.onemoment.app.api.authentication;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import co.yishun.onemoment.app.api.model.ApiModel.CacheType;
import co.yishun.onemoment.app.data.FileUtil;
import retrofit.client.OkClient;
import retrofit.mime.TypedInput;


/**
 * Custom client to replace request with double token verified one. And encrypted body if have. <p>
 * Created by Carlos on 2015/8/5.
 */
public abstract class OneMomentClient extends OkClient {
    public static final String TAG = "OneMomentClient";
    public static final int DEFAULT_EXPIRE_TIME = 10;
    public static final int CACHE_SIZE = 1024 * 1024 * 10;
    protected static final OkHttpClient mCacheOkHttpClient = new OkHttpClient();
    protected static final OkHttpClient mCacheOnlyOkHttpClient = new OkHttpClient();
    protected final CacheType mCacheType;

    protected OneMomentClient(OkHttpClient client, CacheType cacheType) {
        super(client);
        mCacheType = cacheType;
    }

    public static void setUpCache(Context context) {
        Cache cacheOnly = new Cache(FileUtil.getLongCacheDirectory(context, true), CACHE_SIZE);
        mCacheOnlyOkHttpClient.setCache(cacheOnly);
        Cache cacheNormal = new Cache(FileUtil.getCacheDirectory(context, true), CACHE_SIZE);
        mCacheOkHttpClient.setCache(cacheNormal);
    }

    protected abstract byte[] getFakeBody();

    protected abstract byte[] encode(byte[] body) throws IOException;

    protected class FakeTypeInput implements TypedInput {
        private byte[] mFakeBody = getFakeBody();

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
            // we should encrypt the fake body
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            outputStream.write(mFakeBody);
            return new ByteArrayInputStream(encode(mFakeBody));
        }
    }
}