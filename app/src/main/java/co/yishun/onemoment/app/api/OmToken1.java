package co.yishun.onemoment.app.api;

import com.google.common.io.BaseEncoding;

import java.nio.charset.Charset;

/**
 * The first token in Double Token Verification.
 * <p>
 * Created by Carlos on 2015/8/5.
 */
class OmToken1 implements Token {
    private final String mOrigin;
    private final String mValue;

    OmToken1(String token) {
        if (token.length() != 33) {
            throw new AssertionError("String length incorrect!");
        }
        this.mOrigin = token;
        mValue = BaseEncoding.base64().encode(mOrigin.getBytes(Charset.forName("UTF-8")));
    }


    @Override public String value() {
        return mValue;
    }

    public String origin() {
        return mOrigin;
    }

    @Override public String toString() {
        return "OmToken1{" +
                "mOrigin='" + mOrigin + '\'' +
                ", mValue='" + mValue + '\'' +
                '}';
    }
}
