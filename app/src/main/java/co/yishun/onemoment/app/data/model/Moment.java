package co.yishun.onemoment.app.data.model;

import android.content.Context;
import android.util.Log;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

import java.io.File;
import java.io.Serializable;
import java.nio.channels.FileLock;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import co.yishun.onemoment.app.account.AccountHelper;
import co.yishun.onemoment.app.api.model.QiniuKeyProvider;
import co.yishun.onemoment.app.config.Constants;
import co.yishun.onemoment.app.data.FileUtil;
import co.yishun.onemoment.app.data.RealmHelper;
import co.yishun.onemoment.app.data.compat.Contract;


/**
 * This bean is used in Ormlite and OrmliteProvider.
 * <p>
 * Created by Carlos on 2/13/15.
 */
@DatabaseTable(tableName = Contract.Moment.TABLE_NAME)
public class Moment implements Serializable, QiniuKeyProvider {
    private static final String TAG = "Moment";
    //    private static FileChannel channel;

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

    public Moment() {
        fixTimeStampAndTime();
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

    public String getOwnerID() {
        return owner;
    }

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

    /**
     * To fix TimeStamp from millisecond to second
     *
     * @return whether timestamp is wrong
     */
    public boolean fixTimeStampAndTime() {
        if (String.valueOf(timeStamp).length() > 10) {
            timeStamp = timeStamp / 1000;
            time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(timeStamp * 1000);
            return true;
        } else return false;
    }

    @Override public String getName() {
        return this.getTime() + Constants.URL_HYPHEN + this.getUnixTimeStamp();
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

    public interface MomentProvider {
        String getPath();

        String getTime();

        String getOwnerID();

        long getUnixTimeStamp();
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
                Log.e(TAG, "mv file failed");
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
            m.owner = AccountHelper.getAccountId(mContext);
            m.timeStamp = FileUtil.parseTimeStamp(mPath);
            m.time = new SimpleDateFormat(Constants.TIME_FORMAT, Locale.getDefault()).format(m.getUnixTimeStamp() * 1000);
            return m;
        }

        private void check() {
            if (mPath == null)
                throw new IllegalStateException("field mPath is error");
        }
    }
}
