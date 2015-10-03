package co.yishun.onemoment.app.data.model;

import io.realm.RealmObject;

/**
 * This is local moment stored at Database.
 * <p>
 * Created by Carlos on 2015/10/3.
 */
public class Moment extends RealmObject {
    private static final String TAG = "CompatMoment";
    private String thumbPath;
    private String largeThumbPath;
    private String path;
//    @PrimaryKey private int id;// old
    /**
     * add at database version 2.0
     */
    private String owner;
    private String time;
    private long timeStamp;

    public static Moment fromMomentProvider(MomentProvider momentProvider) {
        Moment moment = new Moment();
        moment.path = momentProvider.getPath();
        moment.timeStamp = momentProvider.getUnixTimeStamp();
        moment.time = momentProvider.getTime();
        moment.owner = momentProvider.getOwnerID();

        return moment;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public String getLargeThumbPath() {
        return largeThumbPath;
    }

    public void setLargeThumbPath(String largeThumbPath) {
        this.largeThumbPath = largeThumbPath;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    //TODO handle thumb path

    public interface MomentProvider {
        String getPath();

        String getTime();

        String getOwnerID();

        long getUnixTimeStamp();
    }
}
