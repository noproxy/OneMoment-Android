package co.yishun.onemoment.app.api;

import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.TimeZone;

import co.yishun.onemoment.app.Util;
import co.yishun.onemoment.app.config.Constants;
import retrofit.mime.TypedOutput;

/**
 * The first token in Double Token Verification.
 * Created by Carlos on 2015/8/5.
 */
public class OmToken2 implements Token {
    public static final int DEFAULT_EXPIRE_TIME = 10;
    final private Token mToken1;
    private final String mValue;
    private final String mOrigin;
    private final String mRaw;
    private final String mKey = Constants.API_KEY;
    private final String mUrl;
    private final byte[] mData;

    OmToken2(Token token1, String url, @Nullable TypedOutput body) throws IOException {
        mToken1 = token1;
        mRaw = mToken1.origin();
        mUrl = url;

        byte[] data = null;
        if (body != null) {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            body.writeTo(out);
            data = out.toByteArray();
            out.close();
        }
        mData = data;
        mOrigin = Joiner.on(":").useForNull("").join(mRaw, mKey, mUrl, mData, Util.unixTimeStamp() + DEFAULT_EXPIRE_TIME, TimeZone.getDefault().getID());
        mValue = BaseEncoding.base64().encode(Hashing.sha256().hashBytes(mOrigin.getBytes(Charset.forName("UTF-8"))).asBytes());
    }

    @Override
    public String value() {
        return mValue;
    }

    @Override
    public String origin() {
        return mOrigin;
    }

    @Override public String toString() {
        return "OmToken2{" +
                "mToken1=" + mToken1 +
                ", mValue='" + mValue + '\'' +
                ", mOrigin='" + mOrigin + '\'' +
                ", mRaw='" + mRaw + '\'' +
                ", mKey='" + mKey + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mData=" + Arrays.toString(mData) +
                '}';
    }


}