package co.yishun.onemoment.app;

import android.os.Build;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.util.Base64;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by Carlos on 2015/8/5.
 */
public class Util {
    private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;

    public static long unixTimeStamp() {
        return System.currentTimeMillis() / 1000L;
    }

    // http://stackoverflow.com/questions/6810336/is-there-a-library-or-utility-in-java-to-convert-an-integer-to-its-ordinal
    public static String ordinal(int i) {
        String[] sufixes = new String[]{"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        switch (i % 100) {
            case 11:
            case 12:
            case 13:
                return i + "th";
            default:
                return i + sufixes[i % 10];
        }
    }

    public static String joinString(@NonNull final String forNull, final @NonNull String divider, @NonNull String... values) {
        StringBuilder buffer = new StringBuilder();
        buffer.append(values[0]);
        for (int i = 1; i < values.length; i++) {
            String value = values[i];
            buffer.append(divider);
            buffer.append(value == null ? forNull : value);
        }
        return buffer.toString();
    }

    public static byte[] sha256(String s) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(s.getBytes(Charset.forName("UTF-8")));
            return digest.digest();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return new byte[0];
    }

    public static String base64(byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    //TODO need test
    public static List<String> split(@NonNull char divider, @NonNull String s) {
        List<String> list = new ArrayList<>();
        int last = 0;
        int index = 0;
        while ((index = s.indexOf(divider, last)) != -1) {
            list.add(s.substring(last, index));
            last = index + 1;
        }
        if (s.length() < last)
            list.add(s.substring(last));
        return list;
    }

    // from apache IOUtils
    public static String toString(InputStream input, String encoding) throws IOException {
        StringWriter sw = new StringWriter();
        copy(input, sw, encoding);
        return sw.toString();
    }

    public static void copy(InputStream input, Writer output, String encoding) throws IOException {
        if (encoding == null) {
            copy(input, output);
        } else {
            InputStreamReader in = new InputStreamReader(input, encoding);
            copy(in, output);
        }
    }

    public static int copy(Reader input, Writer output) throws IOException {
        long count = copyLarge(input, output);
        if (count > Integer.MAX_VALUE) {
            return -1;
        }
        return (int) count;
    }

    public static void copy(InputStream input, Writer output) throws IOException {
        InputStreamReader in = new InputStreamReader(input);
        copy(in, output);
    }

    public static long copyLarge(Reader input, Writer output) throws IOException {
        char[] buffer = new char[DEFAULT_BUFFER_SIZE];
        long count = 0;
        int n = 0;
        while (-1 != (n = input.read(buffer))) {
            output.write(buffer, 0, n);
            count += n;
        }
        return count;
    }

    public static @NonNull String getUserAgent() {
        String userAgent = System.getProperty("http.agent");
        if (TextUtils.isEmpty(userAgent)) {
            String release = Build.VERSION.RELEASE;
            String arch = System.getProperty("os.arch");
            String language = Locale.getDefault().getDisplayName();
            userAgent = String.format("OneMoment/2.0 (Linux; U; Android %s; %s; %s)", release, arch, language);
        }
        return userAgent;
    }
}
