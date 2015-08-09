package co.yishun.onemoment.app.api.model;

import java.util.List;

/**
 * Created by Carlos on 2015/8/9.
 */
public class Video extends ApiModel {
    public String fileName;
    public String accountId;
    public String type;
    public List<VideoTag> tags;

    public static class Type {
        public static final String PUBLIC = "public";
        public static final String PRIVATE = "private";
    }

    public static class VideoTag {
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
}
