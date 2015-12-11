package co.yishun.onemoment.app.data;

import java.io.File;
import java.io.FileInputStream;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;

import co.yishun.onemoment.app.data.model.Moment;

/**
 * Created by Carlos on 12/10/15.
 */
public class MomentLock {
    private HashMap<String, FileLock> mLockMap = new HashMap<>();


    private void lock(String lock) throws Exception {
        File lockFile = new File(lock);
        if (!lockFile.exists()) {
            if (!lockFile.createNewFile()) {
                throw new Exception();
            }
        }
        FileInputStream inputStream = new FileInputStream(lockFile);
        FileChannel channel = inputStream.getChannel();
        FileLock fileLock = channel.lock();
        mLockMap.put(lock, fileLock);
    }

    private void unLock(String lock) throws Exception {
        FileLock fileLock = mLockMap.get(lock);
        fileLock.release();
    }

    public void lockMoment(Moment moment) throws Exception {
        lock(moment.getTime());
    }

    public void unlockMoment(Moment moment) throws Exception {
        unLock(moment.getTime());
    }
}
