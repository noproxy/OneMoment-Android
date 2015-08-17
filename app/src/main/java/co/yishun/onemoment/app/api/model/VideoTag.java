package co.yishun.onemoment.app.api.model;

/**
 * Created by Carlos on 2015/8/17.
 */
public class VideoTag {
    public String name;
    public String type;
    public float x;
    public float y;

    public static class Type {
        public static final String LOCATION = "location";
        public static final String TEXT = "words";
        public static final String TIME = "time";
    }
}
