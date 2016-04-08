package co.yishun.onemoment.app.data;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.io.File;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;

import co.yishun.onemoment.app.data.model.Moment;

/**
 * Created by Carlos on 12/10/15.
 */
public class MomentLock {
    private static HashMap<String, FileLock> mLockMap = new HashMap<>();


    private static void lock(Context context, String lock) throws Exception {
        File lockFile = new File(FileUtil.getCacheDirectory(context, false), lock);
        if (!lockFile.exists()) {
            if (!lockFile.createNewFile()) {
                throw new Exception();
            }
        }
        RandomAccessFile randomAccessFile = new RandomAccessFile(lockFile, "rw");
        FileChannel channel = randomAccessFile.getChannel();
        FileLock fileLock = channel.lock();
        mLockMap.put(lock, fileLock);
    }

    private static void unLock(Context context, @NonNull String lock) throws Exception {
        FileLock fileLock = mLockMap.get(lock);
        if (fileLock != null) {
            fileLock.release();
        }
    }

    /**
     * Lock moments by their {@link Moment#getTime()}. Any moment having the same time will be
     * locked.
     *
     * @param moment to provide time to lock.
     */
    public static void lockMoment(Context context, @NonNull Moment moment) throws Exception {
        lock(context, moment.getTime());
    }

    /**
     * Unlock the time of the moment.
     *
     * @param moment to provide time to lock. Do nothing if null.
     */
    public static void unlockMomentIfLocked(Context context, @Nullable Moment moment) throws Exception {
        if (moment != null) {
            unLock(context, moment.getTime());
        }
    }
}
