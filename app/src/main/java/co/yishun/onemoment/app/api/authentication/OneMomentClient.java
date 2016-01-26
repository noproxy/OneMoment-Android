package co.yishun.onemoment.app.api.authentication;

import android.content.Context;

import com.squareup.okhttp.Cache;
import com.squareup.okhttp.OkHttpClient;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

import co.yishun.onemoment.app.data.FileUtil;
import retrofit.client.OkClient;
import retrofit.mime.TypedInput;
import retrofit.mime.TypedOutput;

/**
 * Created by Jinge on 2016/1/27.
 */
public class OneMomentClient extends OkClient {

    public static final int CACHE_SIZE = 1024 * 1024 * 10;
    protected static final OkHttpClient mOkHttpClient = new OkHttpClient();

    protected OneMomentClient() {
        super(mOkHttpClient);
    }

    public static void setUpCache(Context context) {
        Cache cache = new Cache(FileUtil.getCacheDirectory(context, true), CACHE_SIZE);
        mOkHttpClient.setCache(cache);
    }

    protected static class OneMomentTypedOut implements TypedOutput {
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

        @Override public String fileName() {
            return mTypedOutput.fileName();
        }

        @Override public String mimeType() {
            return "text/plain; charset=UTF-8";
        }

        @Override public long length() {
            return mData.length;
        }

        @Override public void writeTo(OutputStream out) throws IOException {
            if (mData == null)
                throw new IOException(mException);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            stream.write(mData);
            stream.writeTo(out);
        }
    }

    protected static class FakeTypeInput implements TypedInput {
        private byte[] mFakeBody = OneMomentV3.FAKE_RESPONSE.getBytes(Charset.forName("UTF-8"));

        @Override public String mimeType() {
            return "text/plain charset=UTF-8";
        }

        @Override public long length() {
            return mFakeBody.length;
        }

        @Override public InputStream in() throws IOException {
            // we should encrypt the fake body
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            outputStream.write(mFakeBody);
            return new ByteArrayInputStream(OneMomentEncoding.encodingStream(outputStream));
        }
    }

}
