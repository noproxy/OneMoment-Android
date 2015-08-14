package co.yishun.onemoment.app.api.authentication;

import android.util.Log;

import com.google.common.base.Charsets;
import com.google.common.base.Joiner;
import com.google.common.io.BaseEncoding;
import com.google.common.io.CharStreams;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import co.yishun.onemoment.app.config.Constants;
import retrofit.converter.ConversionException;
import retrofit.mime.TypedInput;

/**
 * Created by Carlos on 2015/8/5.
 */
public class OneMomentEncoding {
    public static final String TAG = "OneMomentEncoding";
    private static final RandomString mStringGenerator = new RandomString(16);


    public static byte[] encodingStream(ByteArrayOutputStream out) throws IOException {
        try {
            byte[] key = BaseEncoding.base64().decode(Constants.AES_KEY);
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");

            byte[] data = out.toString().getBytes(Charsets.UTF_8);
            int length = data.length / 16 * 16 + 16;
            byte[] dataPadding = new byte[length];
            Arrays.fill(dataPadding, ((byte) 0));
            System.arraycopy(data, 0, dataPadding, 0, data.length);
            String test = new String(dataPadding);

            byte[] ivT = mStringGenerator.nextString().getBytes(Charsets.UTF_8);

            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(ivT));
            byte[] encoded = cipher.doFinal(dataPadding);


//            byte[] ivT = mStringGenerator.nextString().getBytes(Charsets.UTF_8);
//
//
//            byte[] key = BaseEncoding.base64().decode(Constants.AES_KEY);
//            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
//            Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
//            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new IvParameterSpec(ivT));
//

//
//            byte[] encoded = cipher.doFinal(dataPadding);


            String iv = BaseEncoding.base64().encode(ivT);
            String re = Joiner.on(':').join(iv, BaseEncoding.base64().encode(encoded));

            Cipher cipher2 = Cipher.getInstance("AES/CBC/NoPadding");
            cipher2.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivT));
            String de = new String(cipher2.doFinal(encoded));


            Log.i(TAG, "de: " + de);
            byte[] deA = de.getBytes(Charsets.UTF_8);
            Log.i(TAG, "deArray: " + deA);

            return re.getBytes(Charsets.UTF_8);
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    public static String decode(TypedInput body) throws ConversionException {
        String string;
        try {
            string = CharStreams.toString(new InputStreamReader(body.in(), Charsets.UTF_8));
            return decode(string);
        } catch (Exception e) {
            Log.e(TAG, "decode error", e);
            return OneMomentV3.FAKE_RESPONSE;
        }
    }

    private static String decode(String string) throws Exception {
        if (string == null) return null;
        Log.v(TAG, "origin text: " + string);
        int split = string.indexOf(':');
        String iv = string.substring(0, split);
        String etext = string.substring(split + 1, string.length());
        byte[] ivT = BaseEncoding.base64().decode(iv);
        byte[] etextT = BaseEncoding.base64().decode(etext);


        byte[] key = BaseEncoding.base64().decode(Constants.AES_KEY);
        Log.d(TAG, "key: " + new String(key));
        SecretKeySpec secretKeySpec = new SecretKeySpec(key, "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new IvParameterSpec(ivT));

        byte[] re = cipher.doFinal(etextT);
        String s = new String(re);
        String json = s.substring(0, s.lastIndexOf('}') + 1);
        Log.v(TAG, "json: " + json);
        return json;
    }
}