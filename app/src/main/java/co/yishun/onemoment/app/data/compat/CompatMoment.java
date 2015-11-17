package co.yishun.onemoment.app.data.compat;

import android.support.annotation.Nullable;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;
import java.io.Serializable;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

import co.yishun.onemoment.app.data.model.Moment;


/**
 * This bean is used in Ormlite and OrmliteProvider.
 * <p>
 * Created by Carlos on 2/13/15.
 */
@DatabaseTable(tableName = Contract.Moment.TABLE_NAME)
public class CompatMoment implements Serializable, Moment.MomentProvider {
    private static final String TAG = "CompatMoment";
    private static FileChannel channel;

    //    public final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static FileLock lock;
    @DatabaseField String path;
    @DatabaseField String thumbPath;
    @DatabaseField String largeThumbPath;
    @DatabaseField(columnName = Contract.Moment._ID, generatedId = true) private int id;
    /**
     * add at database version 2.0
     */
    @DatabaseField private String owner;
    @DatabaseField private String time;
    @DatabaseField private long timeStamp;

    public CompatMoment() {
//        fixTimeStampAndTime();
    /*keep for ormlite*/
    }

    public String getOwner() {
        return owner;
    }

    /**
     * Set the owner of the moment, null to set it public.
     *
     * @param owner id of the owner
     */
    public void setOwner(@Nullable String owner) {
        if (owner == null) {
            this.owner = "LOC";
        } else this.owner = owner;
    }

    public boolean isPublic() {
        return owner.startsWith("LOC");
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public File getFile() {
        return new File(path);
    }

    @Override
    public String getTime() {
        return time;
    }

    @Override
    public String getOwnerID() {
        return owner;
    }

    @Override
    public long getUnixTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "CompatMoment{" +
                "path='" + path + '\'' +
                ", owner='" + owner + '\'' +
                ", time='" + time + '\'' +
                '}';
    }


}
