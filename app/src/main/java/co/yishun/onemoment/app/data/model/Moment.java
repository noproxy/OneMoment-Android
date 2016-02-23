package co.yishun.onemoment.app.data.model;

import android.content.Context;
import android.support.annotation.NonNull;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;
import java.io.Serializable;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import co.yishun.onemoment.app.LogUtil;
import co.yishun.onemoment.app.account.AccountManager;
import co.yishun.onemoment.app.api.model.QiniuKeyProvider;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.compat.Contract;
import co.yishun.onemoment.app.data.realm.RealmHelper;


/**
 * This bean is used in Ormlite and OrmliteProvider. <p> Created by Carlos on 2/13/15.
 */
@DatabaseTable(tableName = Contract.Moment.TABLE_NAME)
public class Moment implements Serializable, QiniuKeyProvider, Comparable {
    private static final String TAG = "Moment";
    //    private static FileChannel channel;

    //    public final static ReentrantReadWriteLock lock = new ReentrantReadWriteLock();
    private static FileLock lock;
    @DatabaseField
    String path;
    @DatabaseField
    String thumbPath;
    @DatabaseField
    String largeThumbPath;
    @DatabaseField(columnName = Contract.Moment._ID, generatedId = true)
    private int id;
    /**
     * add at database version 2.0
     */
    @DatabaseField
    private String owner;
    @DatabaseField
    private String time;
    @DatabaseField
    private String timeStamp;

    public Moment() {
    /*keep for ormlite*/
    }

    public static List<OMLocalVideoTag> readTags(Moment moment) {
        return RealmHelper.getTags(moment.getTime());
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getOwnerID() {
        return owner;
    }

    public String getUnixTimeStamp() {
        return timeStamp;
    }

    @Override
    public String toString() {
        return "Moment{" +
                "path='" + path + '\'' +
                ", thumbPath='" + thumbPath + '\'' +
                ", largeThumbPath='" + largeThumbPath + '\'' +
                ", id=" + id +
                ", owner='" + owner + '\'' +
                ", time='" + time + '\'' +
                ", timeStamp=" + timeStamp +
                '}';
    }

    @Override
    public String getKey() {
        return this.getOwnerID() + Constants.URL_HYPHEN + this.getTime() + Constants.URL_HYPHEN + this.getUnixTimeStamp() + Constants.VIDEO_FILE_SUFFIX;
    }

    public String getThumbPath() {
        return thumbPath;
    }

    public void setThumbPath(String thumbPath) {
        this.thumbPath = thumbPath;
    }

    public File getThumbPathFile() {
        return new File(getThumbPath());
    }

    public String getLargeThumbPath() {
        return largeThumbPath;
    }

    public void setLargeThumbPath(String largeThumbPath) {
        this.largeThumbPath = largeThumbPath;
    }

    public File getLargeThumbPathFile() {
        return new File(largeThumbPath);
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    /**
     * Compare time of the Moment, use {@link java.util.Collections#sort(List)} to make a list of
     * moment in time order.
     *
     * @return a negative integer if this instance is less than {@code another}; a positive integer
     * if this instance is greater than {@code another}; 0 if this instance has the same order as
     * {@code another}.
     */
    @Override
    public int compareTo(@NonNull Object another) {
        if (!(another instanceof Moment)) {
            return 1;
        }
        Moment anotherMoment = (Moment) another;
        int thisTime = Integer.valueOf(this.getTime());
        int anotherTime = Integer.valueOf(anotherMoment.getTime());
        if (thisTime > anotherTime) {
            return 1;
        } else {
            return -1;
        }
    }

    public interface MomentProvider {
        String getPath();

        String getTime();

        String getOwnerID();

        String getUnixTimeStamp();
    }

    public static class MomentBuilder {
        private static final String TAG = "MomentBuilder";
        private String mPath;
        private Context mContext;

        public MomentBuilder(Context context) {
            mContext = context;
        }

        public MomentBuilder fromPath(String filePath) {
            return fromFile(new File(filePath));
        }

        public MomentBuilder fromFile(File cacheFile) {
            File file = FileUtil.getMomentStoreFile(mContext);
            if (!cacheFile.renameTo(file)) {
                LogUtil.e(TAG, "mv file failed");
                throw new UnsupportedOperationException("Unable to move video");
            }
            mPath = file.getPath();
            return this;
        }

        public MomentBuilder setPath(String path) {
            mPath = path;
            return this;
        }

        public Moment build() {
            check();
            Moment m = new Moment();
            m.path = mPath;
            m.owner = AccountManager.getAccountId(mContext);
            m.timeStamp = FileUtil.parseTimeStamp(mPath);
            m.time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(Long.parseLong(m.getUnixTimeStamp()) * 1000);
            return m;
        }

        private void check() {
            if (mPath == null)
                throw new IllegalStateException("field mPath is error");
        }
    }
}
