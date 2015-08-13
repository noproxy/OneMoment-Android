package co.yishun.onemoment.app.api.authentication;

import com.google.common.io.BaseEncoding;

import java.nio.charset.Charset;

/**
 * The first token in Double Token Verification.
 * <p>
 * Created by Carlos on 2015/8/5.
 */
class OmToken1 implements Token {
    private static final RandomString mStringGenerator = new RandomString(33);
    private final String mOrigin;
    private final String mValue;

    OmToken1() {
        // "AC5 2T575 DCV6U PX7K5 1HZ6J 5S125 8NZIZ"
//        mOrigin = "AC52T575DCV6UPX7K51HZ6J5S1258NZIZ";
        this.mOrigin = mStringGenerator.nextString();
        if (mOrigin.length() != 33) {
            throw new AssertionError("String length incorrect!");
        }
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
