package co.yishun.onemoment.app.api;

import android.support.annotation.Nullable;

import com.google.common.base.Joiner;
import com.google.common.hash.Hashing;
import com.google.common.io.BaseEncoding;

import java.nio.charset.Charset;

import co.yishun.onemoment.app.config.Constants;

/**
 * The first token in Double Token Verification.
 * Created by Carlos on 2015/8/5.
 */
public class OmToken2 implements Token {
    final private Token mToken1;
    private final String mValue;
    private final String mOrigin;
    private final String mRaw;
    private final String mKey = Constants.API_KEY;
    private final String mUrl;
    private final String mData;

    OmToken2(Token token1, String url, @Nullable String data) {
        mToken1 = token1;
        mRaw = mToken1.origin();
        mUrl = url;
        mData = data;
        mOrigin = Joiner.on(":").join(mRaw, mKey, mUrl, mData, System.currentTimeMillis(), "Asia/Shanghai");
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

    @Override
    public String toString() {
        return "OmToken2{" +
                "mToken1=" + mToken1 +
                ", mValue='" + mValue + '\'' +
                ", mOrigin='" + mOrigin + '\'' +
                ", mRaw='" + mRaw + '\'' +
                ", mKey='" + mKey + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mData='" + mData + '\'' +
                '}';
    }
}