package co.yishun.onemoment.app.data.model;

import io.realm.RealmObject;

/**
 * This is local moment stored at Database.
 * <p>
 * Created by Carlos on 2015/10/3.
 */
public class Moment extends RealmObject {
    private static final String TAG = "Moment";
    String path;
    String thumbPath;
    String largeThumbPath;
//    @PrimaryKey private int id;// old
    /**
     * add at database version 2.0
     */
    private String owner;
    private String time;
    private long timeStamp;

    public static Moment fromMomentProvider(MomentProvider momentProvider) {
        Moment moment = new Moment();

        moment.timeStamp = momentProvider.getUnixTimeStamp();
        moment.time = momentProvider.getTime();
        moment.owner = momentProvider.getOwnerID();

        return moment;
    }

    public interface MomentProvider {
        String getTime();

        String getOwnerID();

        long getUnixTimeStamp();
    }
}
