package co.yishun.onemoment.app;

/**
 * Created by Carlos on 2015/8/5.
 */
public class Util {
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
}
